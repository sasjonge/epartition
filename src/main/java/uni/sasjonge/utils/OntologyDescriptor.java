package uni.sasjonge.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Create a label for a partition
 * 
 * @author sascha
 *
 */
public class OntologyDescriptor {
	
	public static ManchesterOWLSyntaxOWLObjectRendererImpl manchester = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	static Map<String, String> mapVertexToManchester;
	
	private OntologyHierarchy ontHierachy;
	
	public OntologyDescriptor(OntologyHierarchy ontHierachy, OWLOntology ontology) {
		this.ontHierachy = ontHierachy;
		
		mapVertexToManchester = mapVertexToManchester(ontology);
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
	public String getAxiomString(Set<String> cc, Graph<String, DefaultEdge> g,
			Map<String, OWLAxiom> vertexToAxiom) {

		// Build the string
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("\n--------\nAxioms:\n");
		cc.stream().forEach(vertex -> {
			if (vertexToAxiom.containsKey(vertex)) {
				toReturn.append(OntologyDescriptor.getCleanName(getManchesterSyntax(vertexToAxiom.get(vertex))));
				toReturn.append("\n");
			}
		});

		return toReturn.toString();
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
					if (cc.contains(cls.toString())) {
						className = getCleanName(cls.toString());
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

					if (cc.contains(getCleanName(cls.toString()))) {
						roleName = getCleanName(cls.toString());

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
	
	public String getLabelForConnectedComponent(Map<String, Set<String>> ccToProperties, String cc) {
		Map<String,String> vertexToPropertiesString = createPropertyStringForVertex(ccToProperties);

		String propName = vertexToPropertiesString.get(cc);
		// System.out.println("pName: " + vertexToPropertiesString.toString());
		return OntologyDescriptor.getCleanName(cc) + (propName == null? "" : propName);
	}
	
	private Map<String, String> createPropertyStringForVertex(Map<String, Set<String>> ccToProperties) {
		Map<String,String> toReturn = new HashMap<>();
		
		StringBuilder builder = new StringBuilder();
		for(Entry<String,Set<String>> entry : ccToProperties.entrySet()) {
			builder.append("\n--" + entry.getValue().size() + " properties--\n");
			
			for(String prop : entry.getValue()) {
				builder.append(OntologyDescriptor.getCleanName(prop.substring(0,prop.length()-1)) + "\n");
			}
			
			toReturn.put(entry.getKey(), builder.toString());
		}
		
		return toReturn;
	}
	
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
}
