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
 * Manages labels and descriptors for a given ontology
 * 
 * @author sascha
 */
public class OntologyDescriptor {

	public static ManchesterOWLSyntaxOWLObjectRendererImpl manchester = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	// Maps the OWLObjects to a name
	public static Map<OWLObject, String> owlObjectToString = new HashMap<>();

	// How many classes are represented through a [ClassLabel]
	private Map<String, Integer> numberOfRepresentedClasses;

	static Map<String, String> mapVertexToManchester;

	private OntologyHierarchy ontHierachy;

	// Symbols to mark groups with e.g. {cat, dog, cow,...},{car, plane, ...}
	final String GROUP_OPENER = "{";
	final String GROUP_CLOSER = "}";

	// Flag if we found a label for initRDFSLabel
	static boolean isLabelled = false;

	public OntologyDescriptor(OntologyHierarchy ontHierachy, OWLOntology ontology) {
		// Save the given Ontology hierachy
		this.ontHierachy = ontHierachy;

		// mapVertexToManchester = mapVertexToManchester(ontology);

		// Create
		numberOfRepresentedClasses = new HashMap<>();
	}

	/**
	 * 
	 * Saves for all classes, properties and individuals with rdfs:labels the mapped
	 * name into the map owlObjectToString (
	 * 
	 * @param ont
	 */
	public static void initRDFSLabel(OWLOntology ont) {

		// For each class in the signature
		ont.classesInSignature().forEach(c -> {
			// Flag if this class is labelled
			isLabelled = false;
			// Get the annotation
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				// if the property is a label
				if (a.getProperty().isLabel()) {
					// and the annotation value is a literal
					if (a.getValue() instanceof OWLLiteral) {
						// Get the literal
						OWLLiteral literal = (OWLLiteral) a.getValue();
						// Check if the language is the one we want
						if (!literal.hasLang() || literal.hasLang(Settings.lang)) {
							// get the value. Map the class to the literal name
							owlObjectToString.put(c, getCleanName(literal.getLiteral().toString()));
							isLabelled = true;
						}
					}
				}
			});
			// If the class wasnt labelled, save the cleaned toString result
			if (!isLabelled) {
				owlObjectToString.put(c, getCleanName(c.toString()));
			}
		});

		// Do the same for the objectproperties,...
		ont.objectPropertiesInSignature().forEach(c -> {
			isLabelled = false;
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						owlObjectToString.put(c, ((OWLLiteral) a.getValue()).getLiteral());
						isLabelled = true;
					}
				}
			});
			if (!isLabelled) {
				owlObjectToString.put(c, getCleanName(c.toString()));
			}
		});

		// ... dataproperties and ...
		ont.dataPropertiesInSignature().forEach(c -> {
			isLabelled = false;
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						owlObjectToString.put(c, ((OWLLiteral) a.getValue()).getLiteral());
						isLabelled = true;
					}
				}
			});
			if (!isLabelled) {
				owlObjectToString.put(c, getCleanName(c.toString()));
			}
		});

		// ... individuals
		ont.individualsInSignature().forEach(c -> {
			isLabelled = false;
			ont.annotationAssertionAxioms(c.getIRI()).forEach(a -> {
				if (a.getProperty().isLabel()) {
					if (a.getValue() instanceof OWLLiteral) {
						owlObjectToString.put(c, ((OWLLiteral) a.getValue()).getLiteral());
						isLabelled = true;
					}
				}
			});
			if (!isLabelled) {
				owlObjectToString.put(c, getCleanName(c.toString()));
			}
		});

		// and subconcepts
		ont.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(c -> {
				if (!owlObjectToString.containsKey(c)) {
					owlObjectToString.put(c, c.toString());
				}
			});
		});

	}

	/**
	 * Returns a clean and simple name for the owl object string
	 * 
	 * @param owlName
	 * @return
	 */
	public static String getCleanName(String owlName) {
		return owlName.replaceAll("http[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*#|<|>", "");
	}

	/**
	 * 
	 * Returns the "clean" name for the given object. It also saves the name into
	 * the map owlObjectToString to reduce calls to getCleanName
	 * 
	 * @param owlOb A OWLObject
	 * @return The cleaned name for the given Object
	 */
	public static String getCleanNameOWLObj(OWLObject owlOb) {

		// Try to get the name of the object from the map
		String toReturn = owlObjectToString.get(owlOb);
		// If there is no name mapped
		if (toReturn == null) {
			// Calculate the clean name
			toReturn = getCleanName(owlOb.toString());
			// And save it in the map
			owlObjectToString.put(owlOb, toReturn);
		}
		// Return the clean name
		return toReturn;

	}

	/**
	 * Returns a label for a given connected component
	 * 
	 * @param numOfAxioms
	 * @param classesOfCC
	 * @param propertiesOfCC
	 * @param individualsOfCC
	 * @return Label
	 */
	public String getLabelForConnectedComponent(int numOfLogicalAxioms, int numOfOtherAxioms, Set<String> classesOfCC,
			Set<String> propertiesOfCC, Set<String> individualsOfCC) {

		// Create a Stringbuilder to save the label
		StringBuilder builder = new StringBuilder();

		// Create Header of form AXIOMS / CLASSES / PROPERTIES / INDIVIDUALS
		builder.append(
				numOfLogicalAxioms + "(" + numOfOtherAxioms + ") / " + (classesOfCC != null ? classesOfCC.size() : 0)
						+ " / " + (propertiesOfCC != null ? propertiesOfCC.size() : 0) + " / "
						+ (individualsOfCC != null ? individualsOfCC.size() : 0) + "\n");

		// Add classes label
		if (classesOfCC != null) {
			builder.append("-- Classes --\n");
			builder.append(getClassesStringForCC(classesOfCC));
			builder.append("\n");
		}

		// Add properties label
		if (propertiesOfCC != null) {
			builder.append("-- Properties --\n");
			builder.append(getPropertiesStringForCC(propertiesOfCC));
			builder.append("\n");
		}

		// Add individuals label
		if (individualsOfCC != null) {
			builder.append("-- Individuals --\n");
			builder.append(getIndivStringForCC(individualsOfCC));
			builder.append("\n");
		}

		// Return the created label
		return builder.toString();
	}

	/**
	 * Returns a String representing the given classes in the ontology hierachy
	 * 
	 * @param classesOfCC
	 * @return Label
	 */
	private String getClassesStringForCC(Set<String> classesOfCC) {
		// Get the maps from the ontology hierachy
		Map<String, String> nodeToParent = ontHierachy.getClassToParentString();
		Map<String, Set<String>> parentsToCollecteddNodes = ontHierachy.getParentToClassesString();

		// Returns a summarized and grouped structure for the classes in the cc
		// groupStrings is a list of groups of classes. The groups have the same
		// superclass
		List<Collection<String>> groupStrings = new ArrayList<Collection<String>>(groupNodesByParents(nodeToParent,
				filterNodesByLevel(nodeToParent, parentsToCollecteddNodes, classesOfCC)));

		// Sorts the groupStrings and uses it to create a well formed label for
		// classesOfCC
		return createStringForGroupedStrings(sortGroupString(groupStrings), Settings.NUM_OF_CLASS_LABELS_TOPLEVEL,
				Settings.NUM_OF_CLASS_LABELS_SUBLEVEL);
	}

	/**
	 * Returns a String representing the given properties in the ontology hierachy
	 * 
	 * @param propertiesOfCC
	 * @return Label
	 */
	String getPropertiesStringForCC(Set<String> propertiesOfCC) {
		// Get the maps from the ontology hierachy
		Map<String, String> nodeToParent = ontHierachy.getPropertyToParentString();
		Map<String, Set<String>> parentsToCollecteddNodes = ontHierachy.getParentToPropertiesString();

		// Returns a summarized and grouped structure for the properties in the cc
		// groupStrings is a list of groups of properties. The groups have the same
		// super property
		List<Collection<String>> groupStrings = new ArrayList<Collection<String>>(groupNodesByParents(nodeToParent,
				filterNodesByLevel(nodeToParent, parentsToCollecteddNodes, propertiesOfCC)));

		// Sorts the groupStrings and uses it to create a well formed label for
		// propertiesOfCC
		return createStringForGroupedStrings(sortGroupString(groupStrings),
				Settings.NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL, Settings.NUM_OF_CLASS_LABELS_SUBLEVEL);
	}

	/**
	 * Returns a String representing the given individuals in the ontology hierachy
	 * 
	 * @param propertiesOfCC
	 * @return Label
	 */
	private String getIndivStringForCC(Set<String> indivOfCC) {
		// Get the maps from the ontology hierachy
		List<String> indivOfCCList = new ArrayList<>(indivOfCC);
		Collections.sort(indivOfCCList, String.CASE_INSENSITIVE_ORDER);

		// Creates a well formed lebel out of the list of individuals
		return createStringForGroupedStrings(
				indivOfCCList.stream().map(e -> Arrays.asList(e)).collect(Collectors.toSet()),
				Settings.NUM_OF_INDIV_LABELS, Settings.NUM_OF_INDIV_LABELS);
	}

	/**
	 * Creates a well formed label for the given group of strings, considering the
	 * parameters
	 * 
	 * @param groupedStrings
	 * @param numberOfTopLevelGroups    Max number of top level groups to be shown
	 * @param numberOfValuesPerSubGroup Max number of values per group to be shown
	 * @return A well formed label
	 */
	public String createStringForGroupedStrings(Collection<Collection<String>> groupedStrings,
			int numberOfTopLevelGroups, int numberOfValuesPerSubGroup) {
		if (numberOfValuesPerSubGroup < 1) {
			throw new IllegalArgumentException("createNameForGroupedStrings needs atleast 1 allowed value per group");
		}

		// Create the string builder to save the string in
		StringBuilder builder = new StringBuilder();

		// Get the iterator for the groupedStrings
		Iterator<Collection<String>> groupIter = groupedStrings.iterator();
		// Current group
		Collection<String> group = null;
		// Counter for how many groups where added for the top level group
		int addedForTLGroup = 0;

		while (addedForTLGroup < numberOfTopLevelGroups && groupIter.hasNext()) {
			// Increase the counter for the current TL Group
			addedForTLGroup++;

			// And get the next group
			group = groupIter.next();

			// If the group has more than one member use the opening bracket, else use
			// nothing
			builder.append(group.size() > 1 ? GROUP_OPENER : "");

			// Counter for how many strings where added for this sub group
			int addedForThisSubGroup = 0;

			// Get an iterator for the current sub group
			Iterator<String> iter = group.iterator();

			// While we have added less then allowed strings per this subgroup and while
			// the are still strings
			while (addedForThisSubGroup < numberOfValuesPerSubGroup - 1 && iter.hasNext()) {
				// Add the string
				builder.append(iter.next());
				if (iter.hasNext()) {
					builder.append("\n");
				}
				// Increase the counter
				addedForThisSubGroup++;
			}

			// if there is another element in this group
			if (iter.hasNext()) {

				// add "...\n NAMEOFLASTELEMENT}\n" to the string
				builder.append("...\n");
				String lastElement = "";
				while (iter.hasNext()) {
					lastElement = iter.next();
				}
				builder.append(lastElement + GROUP_CLOSER + "\n");
			} else {
				// Else simply close the group string
				builder.append(group.size() > 1 ? GROUP_CLOSER : "");
			}

			// If there is another top level group add a line break
			if (groupIter.hasNext()) {
				builder.append("\n");
			}
		}

		// If there are more than numberOfTopLevelGroups groups add "..."
		if (groupIter.hasNext()) {
			builder.append("...");
		}

		return builder.toString();

	}

	/**
	 * Group all nodes in the cc by their parents
	 * 
	 * @param nodeToParent
	 * @param cc
	 * @return
	 */
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

	/**
	 * Sum up child groups in the cc to a parent (saved as [parent])
	 * 
	 * E.g. if the cc contains cat, dog and horse and cat, dog and horse are the
	 * only subclasses of animal in our ontology, simply save [animal] as a
	 * representative
	 * 
	 * @param nodeToParent
	 * @param parentsToCollectedNodes
	 * @param cc
	 * @return Cleaned groupes
	 */
	public Set<String> filterNodesByLevel(Map<String, String> nodeToParent,
			Map<String, Set<String>> parentsToCollectedNodes, Set<String> cc) {
		// System.out.println(cc);
		Set<String> newCCDescriptor = new HashSet<>();

		for (Entry<String, Set<String>> entry : parentsToCollectedNodes.entrySet()) {
			// Flags that shows if all childs of a parent are in the cc
			boolean containsAll = true;

			// If all childs of the parents are in the cc add the parent:
			// Go through all childs of the parent
			for (String subNode : entry.getValue()) {
				// If the child isn't in the cc
				if (!cc.contains(subNode) && !cc.contains("[" + subNode + "]")) {
					// Set the flag that not all childs are contained in the cc
					containsAll = false;
					// And stop the loop
					break;
				}
			}

			// If all childs are contained
			if (containsAll) {
				// Add the parent to the new descriptor. The brackets shows
				// that this parent is a representative for it childs
				newCCDescriptor.add("[" + entry.getKey() + "]");

				// Save the number of childs represented by the parent
				numberOfRepresentedClasses.put("[" + entry.getKey() + "]",
						calculateCurrentValues("[" + entry.getKey() + "]", entry.getValue()));
			}
		}

		// For every element of the given cc
		for (String elementOfCC : cc) {
			// If this element is not the created cc descriptor
			if (!newCCDescriptor.contains("[" + elementOfCC + "]")
					// and there are no ancestors of him in the new and old descriptor
					&& !containsAncestor(nodeToParent, newCCDescriptor, elementOfCC.replaceAll("^\\[|\\]$", ""))
					&& !containsAncestor(nodeToParent, cc, elementOfCC.replaceAll("^\\[|\\]$", ""))) {
				newCCDescriptor.add(elementOfCC);
			}
		}

		// If the new descriptor equals the old one,
		if (newCCDescriptor.equals(cc)) {
			// return it, because we finished
			return newCCDescriptor;
		} else {
			// else recursivly try to created a even more condense number of descriptors
			// by summing ups child in parents
			return filterNodesByLevel(nodeToParent, parentsToCollectedNodes, newCCDescriptor);
		}
	}

	/**
	 * Calculate how many childs nameOfRepresentant will represent in the map
	 * numberOfRepresentedClasses
	 * 
	 * @param nameOfRepresentant
	 * @param value
	 * @return Namber of represented childs
	 */
	private Integer calculateCurrentValues(String nameOfRepresentant, Set<String> value) {
		// Set the return value to 0
		int toReturn = 0;

		// For all childs
		for (String subClassName : value) {
			// If they are already in the map
			if (numberOfRepresentedClasses.containsKey(subClassName)) {
				// This means, they also represent their own childs
				// Add their number
				toReturn = toReturn + numberOfRepresentedClasses.get(subClassName);
			} else {
				// Else increase by one (because it's just this one child)
				toReturn++;
			}
		}

		// If the representant is already in the map and the classes he represents till
		// now
		// Return this number
		if (numberOfRepresentedClasses.containsKey(nameOfRepresentant)
				&& numberOfRepresentedClasses.get(nameOfRepresentant).intValue() > toReturn) {
			return numberOfRepresentedClasses.get(nameOfRepresentant).intValue();
		}

		return toReturn;
	}

	/**
	 * Is the parent of child already in the cc
	 * 
	 * @param nodeToParent
	 * @param cc
	 * @param child
	 * @return
	 */
	private boolean containsAncestor(Map<String, String> nodeToParent, Set<String> cc, String child) {
		return cc.contains("[" + nodeToParent.get(child.replaceAll("^\\[|\\]$", "")) + "]");
	}

	/**
	 * Sorts the list of groups by the number of represented classes
	 * 
	 * @param groupStrings
	 * @return
	 */
	private Collection<Collection<String>> sortGroupString(List<Collection<String>> groupStrings) {

		// Save the input groupstrings into a List of List
		List<List<String>> newGroupCollection = groupStrings.stream().map(e -> new ArrayList<String>(e))
				.collect(Collectors.toList());

		// For each group in this list
		for (List<String> group : newGroupCollection) {

			// Sort the strings alphabetically
			Collections.sort(group, String.CASE_INSENSITIVE_ORDER);

			// sort them by the number of represented classes
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

		// Now sort the whole list of groups by the sum of represented classes
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

		// Create the list to return
		Collection<Collection<String>> toReturn = new ArrayList<>(newGroupCollection.size());
		for (List<String> element : newGroupCollection) {
			toReturn.add(element);
		}

		return toReturn;

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
			toReturn.append("--------\nAxioms:\n");
			int i = 0;
			for (OWLAxiom ax : axioms) {
				// toReturn.append(OntologyDescriptor.getCleanName(getManchesterSyntax(ax)));
				String man = manchester.render(ax);
				String name = OntologyDescriptor.getCleanName(man);
				toReturn.append(name.length() > 100 ? name.subSequence(0, 100) : name);
				toReturn.append("\n");
				if (i > Settings.AXIOM_COUNT) {
					toReturn.append("...");
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

}
