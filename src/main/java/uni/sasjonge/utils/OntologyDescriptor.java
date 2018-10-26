package uni.sasjonge.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

import uni.sasjonge.Settings;

/**
 * Create a label for a partition
 * 
 * @author sascha
 *
 */
public class OntologyDescriptor {

	public static ManchesterOWLSyntaxOWLObjectRendererImpl manchester = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	// How many classes are represented through a [ClassLabel]
	private Map<String, Integer> numberOfRepresentedClasses;

	static Map<String, String> mapVertexToManchester;

	private OntologyHierarchy ontHierachy;

	public OntologyDescriptor(OntologyHierarchy ontHierachy, OWLOntology ontology) {
		this.ontHierachy = ontHierachy;

		// mapVertexToManchester = mapVertexToManchester(ontology);
		numberOfRepresentedClasses = new HashMap<>();
	}

	public static Map<OWLObject, String> owlObjectToString = new HashMap<>();

	public static String getCleanNameOWLObj(OWLObject owlOb) {
		if (!Settings.USE_RDF_LABEL) {
			String toReturn = owlObjectToString.get(owlOb);
			if (toReturn == null) {
				toReturn = getCleanName(owlOb.toString());
				owlObjectToString.put(owlOb, toReturn);
			}
			return toReturn;
		} else {
			return owlObjectToString.get(owlOb);
		}
	}

	public static void initRDFSLabel(OWLOntology ont) {
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		ont.classesInSignature().forEach(c -> {
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) a.getValue();
						owlObjectToString.put(c, getCleanName(val.getLiteral().toString()));
					}
				}
			});
		});
		
		ont.objectPropertiesInSignature().forEach(c -> {
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) a.getValue();
						owlObjectToString.put(c, val.getLiteral());
					}
				}
			});
		});

		ont.dataPropertiesInSignature().forEach(c -> {
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) a.getValue();
						owlObjectToString.put(c, val.getLiteral());
					}
				}
			});
		});
		
		ont.individualsInSignature().forEach(c -> {
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) a.getValue();
						owlObjectToString.put(c, val.getLiteral());
					}
				}
			});
		});	
		
		// Vertex: SubConcepts
		ont.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(c -> {
				if (!owlObjectToString.containsKey(c)) {
				owlObjectToString.put(c, c.toString());
				}
			});
		});
		
		System.out.println("!!!!!!!!!! LAbels: " + owlObjectToString.entrySet().size());

	}

	public static String getCleanName(String owlName) {
		return owlName.replaceAll("http[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*#|<|>", "");
	}

	public String getManchesterSyntax(OWLObject owlObject) {
		return manchester.render(owlObject);
	}

	/**
	 * Returns a String of all Axioms corresponding to the given connected component
	 * 
	 * @param cc              Connected Component, List of Vertex Names
	 * @param g               Graph
	 * @param edgeToAxiomName A map from edges to axioms
	 * @return A String in form of a list of all axioms
	 */
	public String getAxiomString(Set<OWLAxiom> axioms) {
		if (axioms != null) {

			// Build the string
			StringBuilder toReturn = new StringBuilder();
			toReturn.append("\n--------\nAxioms:\n");
			int i = 0;
			for (OWLAxiom ax : axioms) {
				// toReturn.append(OntologyDescriptor.getCleanName(getManchesterSyntax(ax)));
				String man = manchester.render(ax);
				String name = OntologyDescriptor.getCleanName(man);
				toReturn.append(name.length() > 100 ? name.subSequence(0, 100) : name);
				toReturn.append("\n");
				if (i > Settings.AXIOM_COUNT) {
					break;
				}
				i++;
			}
			;

			return toReturn.toString();
		} else {
			return "";
		}
	}

	static boolean hasEntities = true;

	/**
	 * Returns a String of all subConcepts corresponding to the given connected
	 * component
	 * 
	 * @param cc              Connected Component, List of Vertex Names
	 * @param g               Graph
	 * @param edgeToAxiomName A map from edges to sub concepts
	 * @return A String in form of a list of all sub concepts
	 */
	public String getSubConceptString(Set<String> cc) {
		// Build the string
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("Subconcepts:\n");
		cc.stream().forEach(vertex -> {
			toReturn.append(OntologyDescriptor.getCleanName(mapVertexToManchester.get(vertex)));
			toReturn.append("\n");
		});

		return toReturn.toString();
	}

	public String getFilteredSubConceptString(Set<String> cc, int maxNumberOfConcepts) {
		StringBuilder builder = new StringBuilder();
		builder.append("--" + cc.size() + " classes--\n");

		// Descend into the classes to find classnames
		int depth = 0;
		int classesInString = 0;
		boolean addLastClass = false;
		boolean foundAnother = false;
		hasEntities = true;
		String className = "";
		while (hasEntities) {
			if (ontHierachy.getClassesOfDepth(depth) != null) {
				for (OWLClass cls : ontHierachy.getClassesOfDepth(depth)) {
					if (cc.contains(getCleanNameOWLObj(cls))) {
						className = getCleanNameOWLObj(cls);
						if (!addLastClass) {
							builder.append(className);
							builder.append("\n");
							classesInString++;
						} else {
							foundAnother = true;
						}
						// add dots and the last element
						if (classesInString > maxNumberOfConcepts - 2) {
							if (ontHierachy.getClassesOfDepth(depth).size() > maxNumberOfConcepts - 1) {
								addLastClass = true;
							}
						}
						hasEntities = false;
					}
				}
				;
			} else {
				hasEntities = false;
			}

			if (addLastClass && foundAnother) {
				builder.append("...\n");
				builder.append(className);
			}

			depth++;
		}

		return builder.toString();
	}

	public String getFilteredPropertyString(Set<String> cc, int maxNumberOfConcepts) {
		StringBuilder builder = new StringBuilder();

		// Descend into the classes to find classnames
		int depth = 0;
		int classesInString = 0;
		boolean addLastRoles = false;
		boolean foundAnother = false;
		hasEntities = true;
		String roleName = "";
		while (hasEntities && depth <= ontHierachy.getHighestPropertyDepth()) {
			// System.out.println(depth);
			if (ontHierachy.getPropertiesOfDepth(depth) != null) {
				// System.out.println("RoleList: " + cc.toString());
				for (OWLObjectProperty cls : ontHierachy.getPropertiesOfDepth(depth)) {
					// System.out.println("Testing for " + cls);

					if (cc.contains(getCleanNameOWLObj(cls))) {
						roleName = getCleanNameOWLObj(cls);

						if (!addLastRoles) {
							builder.append(roleName);
							builder.append("\n");
							classesInString++;
						} else {
							foundAnother = true;
						}
						// add dots and the last element
						if (classesInString > maxNumberOfConcepts - 2) {
							if (ontHierachy.getPropertiesOfDepth(depth).size() > maxNumberOfConcepts - 1) {
								addLastRoles = true;
							}
						}
						hasEntities = false;
					}
				}
				;
			}

			if (addLastRoles && foundAnother) {
				builder.append("...\n");
				builder.append(addLastRoles);
			}

			depth++;
		}

		return builder.toString();
	}

	public String getLabelForConnectedComponent(int numOfAxioms, Set<String> classesOfCC, Set<String> propertiesOfCC,
			Set<String> individualsOfCC) {

		StringBuilder builder = new StringBuilder();

		// Header of form AXIOMS / CLASSES / PROPERTIES / INDIVIDUALS
		builder.append(numOfAxioms + " / " + (classesOfCC != null ? classesOfCC.size() : 0) + " / "
				+ (propertiesOfCC != null ? propertiesOfCC.size() : 0) + " / "
				+ (individualsOfCC != null ? individualsOfCC.size() : 0) + "\n");

		// Add classes
		if (classesOfCC != null) {
			builder.append("-- Classes --\n");
			builder.append(getClassesStringForCC(classesOfCC));
			builder.append("\n");
		}

		// Add properties
		if (propertiesOfCC != null) {
			builder.append("-- Properties --\n");
			builder.append(getPropertiesStringForCC(propertiesOfCC));
			builder.append("\n");
		}

		// Add individuals
		if (individualsOfCC != null) {
			builder.append("-- Individuals --\n");
			builder.append(getIndivStringForCC(individualsOfCC));
			builder.append("\n");
		}

		// Return answer
//		System.out.println("-------------");
//		System.out.println("!!:" + classesOfCC);
//		System.out.println("!!:"+builder.toString());
//		System.out.println("-------------");

		return builder.toString();
	}

	private String getClassesStringForCC(Set<String> classesOfCC) {
		Map<String, String> nodeToParent = ontHierachy.getClassToParentString();
		Map<String, Set<String>> parentsToCollecteddNodes = ontHierachy.getParentToClassesString();

		List<Collection<String>> groupStrings = new ArrayList<Collection<String>>(groupNodesByParents(nodeToParent,
				filterNodesByLevel(nodeToParent, parentsToCollecteddNodes, classesOfCC)));

		return createStringForGroupedStrings(sortGroupString(groupStrings), Settings.NUM_OF_CLASS_LABELS_TOPLEVEL,
				Settings.NUM_OF_CLASS_LABELS_SUBLEVEL);
	}

	private Collection<Collection<String>> sortGroupString(List<Collection<String>> groupStrings) {

		List<List<String>> newGroupCollection = groupStrings.stream().map(e -> new ArrayList<String>(e))
				.collect(Collectors.toList());

		for (List<String> group : newGroupCollection) {

			Collections.sort(group, String.CASE_INSENSITIVE_ORDER);

			Collections.sort(group, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					if (numberOfRepresentedClasses.containsKey(o1) && numberOfRepresentedClasses.containsKey(o2)) {
						return numberOfRepresentedClasses.get(o2) - numberOfRepresentedClasses.get(o1);
					} else if (numberOfRepresentedClasses.containsKey(o1)) {
						return -1;
					} else if (numberOfRepresentedClasses.containsKey(o2)) {
						return 1;
					}
					return 0;
				}
			});

		}

		Collections.sort(newGroupCollection, new Comparator<Collection<String>>() {

			@Override
			public int compare(Collection<String> firstGroup, Collection<String> secondGroup) {
				int firstGroupValue = 0, secondGroupValue = 0;

				for (String group : firstGroup) {
					if (numberOfRepresentedClasses.containsKey(group)) {
						firstGroupValue = firstGroupValue + numberOfRepresentedClasses.get(group);
					} else {
						firstGroupValue++;
					}
				}

				for (String group : secondGroup) {
					if (numberOfRepresentedClasses.containsKey(group)) {
						secondGroupValue = secondGroupValue + numberOfRepresentedClasses.get(group);
					} else {
						secondGroupValue++;
					}
				}

				if (firstGroupValue == secondGroupValue) {
					return firstGroup.toString().compareTo(secondGroup.toString());
				}

				return secondGroupValue - firstGroupValue;

			}

		});

		Collection<Collection<String>> toReturn = new ArrayList<>(newGroupCollection.size());
		for (List<String> element : newGroupCollection) {
			toReturn.add(element);
		}

		return toReturn;

	}

	private String getIndivStringForCC(Set<String> classesOfCC) {

		List<String> classesOfCCList = new ArrayList<>(classesOfCC);
		Collections.sort(classesOfCCList, String.CASE_INSENSITIVE_ORDER);

		return createStringForGroupedStrings(
				classesOfCCList.stream().map(e -> Arrays.asList(e)).collect(Collectors.toSet()),
				Settings.NUM_OF_INDIV_LABELS, Settings.NUM_OF_INDIV_LABELS);
	}

	String getPropertiesStringForCC(Set<String> propertiesOfCC) {
		Map<String, String> nodeToParent = ontHierachy.getPropertyToParentString();
		Map<String, Set<String>> parentsToCollecteddNodes = ontHierachy.getParentToPropertiesString();

		List<Collection<String>> groupStrings = new ArrayList<Collection<String>>(groupNodesByParents(nodeToParent,
				filterNodesByLevel(nodeToParent, parentsToCollecteddNodes, propertiesOfCC)));

		return createStringForGroupedStrings(sortGroupString(groupStrings),
				Settings.NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL, Settings.NUM_OF_CLASS_LABELS_SUBLEVEL);
	}

//	private String getIndividualsStringForCC(Set<String> individualsOfCC) {
//		Map<String,String> nodeToParent = ontHierachy.getIndividualToParentString();
//		Map<String,Set<String>> parentsToCollecteddNodes = ontHierachy.getParentToIndividualsString();
//		
//		return createStringForGroupedStrings(
//				groupNodesByParents(nodeToParent, 
//						filterNodesByLevel(nodeToParent, parentsToCollecteddNodes, individualsOfCC)), 
//				Settings.NUM_OF_INDIVIDUAL_LABELS);
//	}

	/**
	 * Creates a mapping from all vertexes in our graphs to their corresponding
	 * manchester syntax
	 * 
	 * @param ontology The corresponding ontology
	 */
	private Map<String, String> mapVertexToManchester(OWLOntology ontology) {
		Map<String, String> toReturn = new HashMap<>();

		// Vertex: ObjectProperties
		ontology.objectPropertiesInSignature().forEach(objProp -> {
			if (!objProp.isOWLTopObjectProperty() && !objProp.isTopEntity()) {
				toReturn.put(objProp.toString() + "0", manchester.render(objProp) + "0");
				toReturn.put(objProp.toString() + "1", manchester.render(objProp) + "1");
			}
		});

		// Vertex: DataProperties
		ontology.dataPropertiesInSignature().forEach(dataProp -> {
			if (!dataProp.isOWLTopDataProperty() && !dataProp.isTopEntity()) {
				toReturn.put(dataProp.toString(), manchester.render(dataProp).toString());
			}
		});

		// Vertex: SubConcepts
		ontology.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(nested -> {
				if (!nested.isOWLThing()) {
					toReturn.put(nested.toString(), manchester.render(nested));
				}
			});
		});
		// Vertex: Individuals
		ontology.individualsInSignature().forEach(indiv -> {
			toReturn.put(indiv.toString(), manchester.render(indiv));
		});

		return toReturn;
	}

	public String createStringForGroupedStrings(Collection<Collection<String>> groupedStrings,
			int numberOfTopLevelGroups, int numberOfValuesPerSubGroup) {
		if (numberOfValuesPerSubGroup < 1) {
			throw new IllegalArgumentException("createNameForGroupedStrings needs atleast 1 allowed value per group");
		}

		final String GROUP_OPENER = "{";
		final String GROUP_CLOSER = "}";

		StringBuilder builder = new StringBuilder();

		Iterator<Collection<String>> groupIter = groupedStrings.iterator();
		Collection<String> group = null;
		int addedForTLGroup = 0;

		while (addedForTLGroup < numberOfTopLevelGroups && groupIter.hasNext()) {
			addedForTLGroup++;
			group = groupIter.next();

			builder.append(group.size() > 1 ? GROUP_OPENER : "");
			int addedForThisSubGroup = 0;

			Iterator<String> iter = group.iterator();
			while (addedForThisSubGroup < numberOfValuesPerSubGroup - 1 && iter.hasNext()) {
				builder.append(iter.next());
				if (iter.hasNext()) {
					builder.append("\n");
				}
				addedForThisSubGroup++;
			}

			if (iter.hasNext()) {
				builder.append("...\n");

				String lastElement = "";
				while (iter.hasNext()) {
					lastElement = iter.next();
				}
				builder.append(lastElement + GROUP_CLOSER + "\n");
			} else {
				builder.append(group.size() > 1 ? GROUP_CLOSER : "");
			}

			if (groupIter.hasNext()) {
				builder.append("\n");
			}
		}

		if (groupIter.hasNext()) {
			builder.append("...");
		}

		return builder.toString();

	}

	public Collection<Collection<String>> groupNodesByParents(Map<String, String> nodeToParent, Set<String> cc) {

		Map<String, Collection<String>> groupedByParent = new HashMap<>();

		// unique index
		int unique_index = 1;

		// First decide to which group each String belongs
		for (String node : cc) {
			// get the parent name
			String parent = nodeToParent.get(node.replaceAll("^\\[|\\]$", ""));

			// If the node has a parent

			if (parent != null) {
				// Add node to corresponding Group
				if (!groupedByParent.containsKey(parent)) {
					groupedByParent.put(parent, new HashSet<>());
				}
				groupedByParent.get(parent).add(node);
			} else {
				// if node has no parent add it with a unique index
				groupedByParent.put(node + ++unique_index, Arrays.asList(node));
			}
		}

		return groupedByParent.values();
	}

	public Set<String> filterNodesByLevel(Map<String, String> nodeToParent,
			Map<String, Set<String>> parentsToCollectedNodes, Set<String> cc) {
		// System.out.println(cc);
		Set<String> newCCDescriptor = new HashSet<>();

		for (Entry<String, Set<String>> entry : parentsToCollectedNodes.entrySet()) {
			boolean containsAll = true;

			// if all childs of the parents are in the cc add the parent
			for (String subNode : entry.getValue()) {

				if (!cc.contains(subNode) && !cc.contains("[" + subNode + "]")) {
//					if(cc.contains("SpecificHeatCapacity") && entry.getKey().contains("Quantity")) {
//						System.out.println("-----------------------");
//						System.out.println(entry.getKey());
//						System.out.println(subNode);
//						System.out.println(cc);
//						System.out.println("------------------------");
//					}
					containsAll = false;
					break;
				}
			}

			if (containsAll) {
				newCCDescriptor.add("[" + entry.getKey() + "]");

				numberOfRepresentedClasses.put("[" + entry.getKey() + "]",
						calculateCurrentValues("[" + entry.getKey() + "]", entry.getValue()));
			}
		}

		for (String elementOfCC : cc) {
			if (!newCCDescriptor.contains("[" + elementOfCC + "]")
					&& !containsAncestor(nodeToParent, newCCDescriptor, elementOfCC.replaceAll("^\\[|\\]$", ""))
					&& !containsAncestor(nodeToParent, cc, elementOfCC.replaceAll("^\\[|\\]$", ""))) {
				newCCDescriptor.add(elementOfCC);
				// System.out.println("!!!!! " + elementOfCC);
			}
		}

		if (newCCDescriptor.equals(cc)) {
			return newCCDescriptor;
		} else {
			return filterNodesByLevel(nodeToParent, parentsToCollectedNodes, newCCDescriptor);
		}
	}

	private Integer calculateCurrentValues(String nameOfRepresentant, Set<String> value) {
		int toReturn = 0;

		for (String subClassName : value) {
			if (numberOfRepresentedClasses.containsKey(subClassName)) {
				toReturn = toReturn + numberOfRepresentedClasses.get(subClassName);
			} else {
				toReturn++;
			}
		}

		if (numberOfRepresentedClasses.containsKey(nameOfRepresentant)
				&& numberOfRepresentedClasses.get(nameOfRepresentant).intValue() > toReturn) {
			return numberOfRepresentedClasses.get(nameOfRepresentant).intValue();
		}

		return toReturn;
	}

	private boolean containsAncestor(Map<String, String> nodeToParent, Set<String> cc, String child) {
		return cc.contains("[" + nodeToParent.get(child.replaceAll("^\\[|\\]$", "")) + "]");
	}

//	private boolean containsAncestor(Map<String, String> nodeToParent, Set<String> cc, String child) {
//		String parent = nodeToParent.get(child.replaceAll("^\\[|\\]$", ""));
//		while(parent != null) {
//			if (cc.contains("[" + parent + "]")) {
//				return true;
//			}
//			parent = nodeToParent.get(parent.replaceAll("^\\[|\\]$", ""));
//		}
//		return false;
//	}
}
