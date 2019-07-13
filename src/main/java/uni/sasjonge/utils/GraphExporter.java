package uni.sasjonge.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uni.sasjonge.Settings;

/**
 * Utility class for different graph exporting methods
 * 
 * @author Sascha Jongebloed
 *
 */
public class GraphExporter {

	// Hierachy of the given ontology
	static OntologyHierarchy ontHierachy;
	// Manages the labels for the given ontology
	static OntologyDescriptor ontDescriptor;

	// Counts how many axioms belon to a cc
	static Map<String, Integer> ccToAxiomCount = null;
	static Map<String, Set<OWLAxiom>> ccToAxioms = null;

	/**
	 * Exports g in graphML to outputPath Every node and edge is shown as is in the
	 * graph
	 * 
	 * @param g          The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportComplexGraph(Graph<String, DefaultEdge> g, String outputPath) throws ExportException {
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();

		// Register additional name attribute for vertices
		exporter.setVertexLabelProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return OntologyDescriptor.getCleanName(vertex);
			}
		});
		exporter.setVertexIDProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return OntologyDescriptor.getCleanName(vertex);
			}
		});
		// exporter.setVertexLabelAttributeName("custom_vertex_label");

		// Initizalize Filewriter and export the corresponding graph
		FileWriter fw;
		try {
			// Simpy output the graph created by the algorithm
			fw = new FileWriter(outputPath);
			exporter.exportGraph(g, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int i = 1;

	/**
	 * Exports g in graphML to outputPath Every node is a connected component, every
	 * edge stands for a ij-property
	 * 
	 * @param g          The graph to export
	 * @param outputPath Parth to output to
	 * @return 
	 * @throws ExportException
	 */
	public static String exportCCStructureGraph(Graph<String, DefaultEdge> g, OWLOntology ontology,
			Map<String, Set<OWLAxiom>> vertexToAxiom, String outputPath) throws ExportException {
		
		// Create a string builder for the output
		StringBuilder builder = new StringBuilder();

		// Classes and individuals of the given ontology
		Set<String> classes = getClassesForOntology(ontology);
		Set<String> individuals = getIndividualsForOntology(ontology);

		// Used to find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);

		// Create the graph we want to output
		Graph<String, DefaultEdge> ccGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Maps a name to each cc
		Map<Set<String>, String> ccToVertexName = new HashMap<>();

		// CC's with axioms
		List<Set<String>> ccWithAxioms = new ArrayList<>();

		// Map the vertexes to the corresponding set of axioms, classes, individuals
		Map<String, Set<String>> vertexToClasses = new HashMap<>();
		Map<String, Set<String>> vertexToIndividuals = new HashMap<>();
		
		if (Settings.SHOW_AXIOMS) {
			ccToAxioms = getCCToAxioms(ci.connectedSets(), vertexToAxiom);
		}
		
		// If we want to show axioms in the output graph
		if (Settings.SHOW_AXIOMS) {
			
			// Get the axioms for the cc
			ccToAxioms = getCCToAxioms(ci.connectedSets(), vertexToAxiom);
			
			// Create the vertexToAxiomsCount Hashmap
			GraphExporter.ccToAxiomCount = new HashMap<>();
			
			// To save the number number of axioms for this cc
			for (Entry<String, Set<OWLAxiom>> e : ccToAxioms.entrySet()) {
				ccToAxiomCount.put(e.getKey(), e.getValue().size());
			}
		} else {
			// Else just save the axiom to vertex count
			ccToAxiomCount = getCCToAxiomsCount(ci.connectedSets(), vertexToAxiom);
		}
		

		// For each connected component
		for (Set<String> cc : ci.connectedSets()) {

			// Create the set of all classes and all individuals for this cc
			// classes and individuals start with same baseset (alle elements of the cc)
			Set<String> classesForThisCC = cc.stream().map(e -> OntologyDescriptor.getCleanName(e))
					.collect(Collectors.toSet());
			Set<String> individualsForThisCC = new HashSet<>(classesForThisCC);

			// Take the intersection of classesForThisCC (which simply contains all vertexes in the cc)
			// and all classes in the ontology to get the classes in this cc
			classesForThisCC.retainAll(classes);

			// If there are classes in this cc
			if (classesForThisCC.size() > 0) {
				vertexToClasses.put(cc.toString() + "", classesForThisCC);
			}

			// Repeat the same for the individuals
			individualsForThisCC.retainAll(individuals);

			if (individualsForThisCC.size() > 0) {
				vertexToIndividuals.put(cc.toString() + "", individualsForThisCC);
			}
		}

		// Add the opening bracket to the builder
		builder.append("[");
		
		// Add vertexes for all connected components
		for (Set<String> cc : ci.connectedSets()) {

			// If there is at least one axiom in this cc
			if (ccToAxiomCount.get(cc.toString() + "") != null) {
				
				// Add the number of axioms for this cc to the string
				builder.append(ccToAxiomCount.get(cc.toString() + "").toString() + ", ");
				
				// Save out name for the cc (used as a key)
				ccToVertexName.put(cc, cc.toString() + "");
				
				// Add the cc as a vertex
				ccGraph.addVertex(cc.toString() + "");
				
				// Save the cc as a cc with axioms
				ccWithAxioms.add(cc);
				i++;
			}
		};
		
		// Close the string till her
		builder.append("], ");
		
		// Create a map of map as a matrix for connectedness
		Map<String, Map<String, Integer>> matrix = new HashMap<>();
		//For each cc
		for (Set<String> cc : ci.connectedSets()) {
			// Add the cc to the map
			matrix.put(cc.toString() + "", new HashMap<>());
			// For each cc2
			for (Set<String> cc2 : ci.connectedSets()) {
				// Use cc as a key in the matrix for cc2 and 0
				matrix.get(cc.toString() + "").put(cc2.toString() + "", new Integer(0));
			}
		}

		// Create a map from property vertexes to the cc that contains it
		Map<String, Set<String>> role0ToCC = new HashMap<>();
		Map<String, Set<String>> role1ToCC = new HashMap<>();
		// For each cc with axioms
		ccWithAxioms.stream().forEach(cc -> {
			// For each vertex
			cc.stream().forEach(subcon -> {
				// If the vertex has the form of a property vertex save it
				if (subcon.endsWith(Settings.PROPERTY_0_DESIGNATOR)) {
					role0ToCC.put(subcon, cc);
				} else if (subcon.endsWith(Settings.PROPERTY_1_DESIGNATOR)) {
					role1ToCC.put(subcon, cc);
				}
			});
		});
		
		//find corresponding property nodes (same prefix, ending
				// with 0 or 1)
				// remember the name of properties for the edges
		Map<DefaultEdge, Set<String>> nameForEdge = new HashMap<>();
		Map<String, Set<String>> vertexToProperties = new HashMap<>();
		
		// For each role0 we saved (this CAN contain data properties)
		for (Entry<String, Set<String>> roleToCC : role0ToCC.entrySet()) {
			// Get the name of the role0
			String role0 = roleToCC.getKey();
			// And create the cc for role1
			Set<String> ccOfRole1 = role1ToCC.get(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length()) + Settings.PROPERTY_1_DESIGNATOR);
			
			// If there is a "partner-role"
			if (ccOfRole1 != null && !ccOfRole1.equals(roleToCC.getValue())) {
				
				// if the partner-role is in another cc, add the name to the
				// edge between the corresponding cc's
				
				DefaultEdge edge = null;
				// Get all edges that are between the cc of role0 and role 1
				Set<DefaultEdge> edgeList = ccGraph.getAllEdges(ccToVertexName.get(roleToCC.getValue()),
						ccToVertexName.get(ccOfRole1));
				if (edgeList.isEmpty()) {
					// If there are no edges of this type, add one
					edge = ccGraph.addEdge(ccToVertexName.get(roleToCC.getValue()), ccToVertexName.get(ccOfRole1));
					matrix.get(roleToCC.getValue() + "").put(ccOfRole1.toString() + "",new Integer(1));
				} else if (edgeList.size() == 1) {
					// If the edgelist is not empty it should only contain one element.
					// So do nothing here (beside increasing the iterator)
					edge = edgeList.iterator().next();
				}

				// Add the hashset if it doesn't exist already
				if (nameForEdge.get(edge) == null) {
					nameForEdge.put(edge, new HashSet<String>());
				}

				// Then add the name of the edge
				nameForEdge.get(edge)
						.add(OntologyDescriptor.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_1_DESIGNATOR.length())));

				
			} else {
				// if there is no "partner" role role0 is a dataproperty
				// if role0 and role1 have the same cc, they are in the same partition
				String ccName = ccToVertexName.get(roleToCC.getValue());
				// Add the property to the vertex of the xx
				if (vertexToProperties.get(ccName) == null) {
					vertexToProperties.put(ccName, new HashSet<>());
				}
				vertexToProperties.get(ccName)
						.add(OntologyDescriptor.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length())));

			}
		}
		
		// Save matrix to the string
		builder.append("[");
		
		for (Entry<String, Map<String, Integer>> row : matrix.entrySet()) {
			builder.append("[");
			for (Entry<String, Integer> e : row.getValue().entrySet()) {
				builder.append(e.getValue() + ", ");
			}
			builder.append("],");

		}
		builder.append("]");
		
		/////////////////////////////////////////////////////////////////////////
		// Export the Graph
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();

		// Register additional name attribute for vertices
		exporter.setVertexLabelProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				System.out.println(vertex);
				return ontDescriptor.getLabelForConnectedComponent(ccToAxiomCount.get(vertex),
						vertexToClasses.get(vertex), vertexToProperties.get(vertex), vertexToIndividuals.get(vertex))
						+ (Settings.SHOW_AXIOMS ? "\n" + ontDescriptor.getAxiomString(ccToAxioms.get(vertex)) : "");
				// + ((axioms.size() < 16) ? "_________\n CC: \n" + vertex : "");
			}
		});

		// Register additional name attribute for edges
		exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge edge) {
				return ontDescriptor.getPropertiesStringForCC(nameForEdge.get(edge));
				// return nameForEdge.get(edge).toString();
			}
		});

		// Initizalize Filewriter and export the corresponding graph
		FileWriter fw;
		try {
			fw = new FileWriter(outputPath);
			exporter.exportGraph(ccGraph, fw);
			fw.flush();
			fw.close();
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/**
	 * Given a List of connected components and a map mapping vertexes to sets of axioms
	 * return a map, that maps the cc to their axioms
	 * 
	 * @param connectedSets
	 * @param vertexToAxiom
	 * @return Mapping from cc to axioms
	 */
	private static Map<String, Set<OWLAxiom>> getCCToAxioms(List<Set<String>> connectedSets,
			Map<String, Set<OWLAxiom>> vertexToAxiom) {

		// Create the map to map cc to axioms
		Map<String, Set<OWLAxiom>> ccToAxioms = new HashMap<>();
		
		// For each cc
		for (Set<String> cc : connectedSets) {
			
			// For each vertex in this cc
			for (String vert : cc) {
				
				// If there are are axioms for this vertex
				if (vertexToAxiom.get(vert) != null) {
					// If the cc isn't already a key in the map create the entry
					if (!ccToAxioms.containsKey(cc.toString() + "")) {
						ccToAxioms.put(cc.toString() + "", new HashSet<>());
					}
					// and add the axioms
					ccToAxioms.get(cc.toString() + "").addAll(vertexToAxiom.get(vert));

				}
			}
		}

		return ccToAxioms;
	}

	/**
	 * Giiven a list of connected components and a map mapping vertexes to axioms
	 * return a map mapping the cc to the number of vertexes
	 * 
	 * @param connectedSets
	 * @param vertexToAxiom
	 * @return Mapping of cc's to their number of axioms
	 */
	private static Map<String, Integer> getCCToAxiomsCount(List<Set<String>> connectedSets,
			Map<String, Set<OWLAxiom>> vertexToAxiom) {
		
		// Create the map to retunr
		Map<String, Integer> ccToAxiomsCount = new HashMap<>();
		
		// For each connected component
		for (Set<String> cc : connectedSets) {
			
			// Counter for the number of axioms for this cc
			int count = 0;
			
			// For each vertex of the connected component
			for (String vert : cc) {

				// If there are axioms for this vertex add their number to the counter
				count = vertexToAxiom.get(vert) != null ?					
						count + vertexToAxiom.get(vert).size() : count;

			}
			
			// Map the cc to the number of axioms
			ccToAxiomsCount.put(cc.toString() + "",count);
		}

		return ccToAxiomsCount;
	}


	/**
	 * Returns a map from the string representation of the cc to the set of axioms
	 * that labels all vertexes in the cc
	 * 
	 * @param connectedSets
	 * @param vertexToAxiom
	 * @return Map from CC to Axioms
	 */
	private static Map<String, Set<OWLAxiom>> getAxiomsForCC(List<Set<String>> connectedSets,
			Map<String, Set<OWLAxiom>> vertexToAxiom) {

		// Create the map to return
		Map<String, Set<OWLAxiom>> vertexToAxioms = new HashMap<>();
		
		// For each cc
		for (Set<String> cc : connectedSets) {
			
			// For each vertex in the cc
			for (String vert : cc) {
				
				// If there are axioms labelling this vertex
				if (vertexToAxiom.get(vert) != null) {
					// Add the axioms to the map 
					if (!vertexToAxioms.containsKey(cc.toString() + "")) {
						vertexToAxioms.put(cc.toString() + "", new HashSet<>());
					}
					vertexToAxioms.get(cc.toString() + "").addAll(vertexToAxiom.get(vert));

				}
			}
		}

		return vertexToAxioms;
	}	
	
	/**
	 * Returns the named classes for the ontology
	 * 
	 * @param ontology
	 * @return Set of name of classes
	 */
	private static Set<String> getClassesForOntology(OWLOntology ontology) {
		return ontology.classesInSignature().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns the named individuals
	 * 
	 * @param ontology
	 * @return Set of name of individuals
	 */
	private static Set<String> getIndividualsForOntology(OWLOntology ontology) {
		return ontology.individualsInSignature().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
				.collect(Collectors.toSet());
	}

	/**
	 * Initiates mappings
	 * 
	 * @param ontology
	 */
	public static void init(OWLOntology ontology) {

		try {
			// Initialize a hierachy structure for the ontology
			ontHierachy = new OntologyHierarchy(ontology);

			// Initialized a object that manages the labels for the ontology
			ontDescriptor = new OntologyDescriptor(ontHierachy, ontology);
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

}
