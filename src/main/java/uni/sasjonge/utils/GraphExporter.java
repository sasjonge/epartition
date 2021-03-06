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
import uni.sasjonge.partitioning.PartitioningCore;

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
	static Map<String, Integer> ccToLogicalAxiomCount;
	private static Map<String, Integer> ccToOtherAxiomCount;
	static Map<String, Set<OWLAxiom>> ccToAxioms;

	/**
	 * Exports g in graphML to outputPath Every node and edge is shown as is in the
	 * graph
	 * 
	 * @param g          The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportConstraintGraph(Graph<String, DefaultEdge> g, String outputPath) throws ExportException {
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
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<DefaultEdge, String> edgeToVertex, String outputPath)
			throws ExportException {

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

		// Get the axioms for the cc
		ccToAxioms = PartitioningCore.getCCToAxioms(g, ci.connectedSets(), edgeToAxioms, edgeToVertex);

		// Create the vertexToAxiomsCount Hashmap
		GraphExporter.ccToLogicalAxiomCount = new HashMap<>();
		GraphExporter.ccToOtherAxiomCount = new HashMap<>();

		// To save the number of axioms for this cc
		for (Entry<String, Set<OWLAxiom>> e : ccToAxioms.entrySet()) {
			int logicalCount = 0, otherCount = 0;
			for (OWLAxiom ax : e.getValue()) {
				if (ax.isLogicalAxiom()) {
					logicalCount++;
				} else {
					otherCount++;
				}
			}
			ccToLogicalAxiomCount.put(e.getKey(), logicalCount);
			ccToOtherAxiomCount.put(e.getKey(), otherCount);
		}

		// For each connected component
		for (Set<String> cc : ci.connectedSets()) {

			// Create the set of all classes and all individuals for this cc
			// classes and individuals start with same baseset (alle elements of the cc)
			Set<String> classesForThisCC = cc.stream().map(e -> OntologyDescriptor.getCleanName(e))
					.collect(Collectors.toSet());
			Set<String> individualsForThisCC = new HashSet<>(classesForThisCC);

			// Take the intersection of classesForThisCC (which simply contains all vertexes
			// in the cc)
			// and all classes in the ontology to get the classes in this cc
			classesForThisCC.retainAll(classes);

			// If there are classes in this cc
			if (classesForThisCC.size() > 0) {
				vertexToClasses.put(cc.hashCode() + "", classesForThisCC);
			}

			// Repeat the same for the individuals
			individualsForThisCC.retainAll(individuals);

			if (individualsForThisCC.size() > 0) {
				vertexToIndividuals.put(cc.hashCode() + "", individualsForThisCC);
			}
		}

		// Add the opening bracket to the builder
		builder.append("[");

		// Add vertexes for all connected components
		for (Set<String> cc : ci.connectedSets()) {

			// If there is at least one axiom in this cc
			if (ccToLogicalAxiomCount.get(cc.hashCode() + "") != null) {

				// Add the number of axioms for this cc to the string
				builder.append(ccToLogicalAxiomCount.get(cc.hashCode() + "").toString() + ", ");

				// Save out name for the cc (used as a key)
				ccToVertexName.put(cc, cc.hashCode() + "");

				// Add the cc as a vertex
				ccGraph.addVertex(cc.hashCode() + "");

				// Save the cc as a cc with axioms
				ccWithAxioms.add(cc);
				i++;
			}
		}
		;

		// Close the string till her
		builder.append("], ");

		// Create a map of map as a matrix for connectedness
        Map<Integer,Set<String>> hashToCC = new HashMap<>();
		Map<Integer, Map<Integer, Integer>> matrix = new HashMap<>();
		// For each cc
		for (Set<String> cc : ci.connectedSets()) {
			// Add the cc to the maps
			matrix.put(cc.hashCode(), new HashMap<>());
			hashToCC.put(cc.hashCode(), cc);
			// For each cc2
			for (Set<String> cc2 : ci.connectedSets()) {
				// Use cc as a key in the matrix for cc2 and 0
				matrix.get(cc.hashCode()).put(cc2.hashCode(), new Integer(0));
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

		// find corresponding property nodes (same prefix, ending
		// with 0 or 1)
		// remember the name of properties for the edges
		Map<DefaultEdge, Set<String>> nameForEdge = new HashMap<>();
		Map<String, Set<String>> vertexToProperties = new HashMap<>();

		// For each role0 we saved (this CAN contain data properties)
		for (Entry<String, Set<String>> roleToCC : role0ToCC.entrySet()) {
			// Get the name of the role0
			String role0 = roleToCC.getKey();
			// And create the cc for role1
			Set<String> ccOfRole1 = role1ToCC
					.get(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length())
							+ Settings.PROPERTY_1_DESIGNATOR);

			// If there is a "partner-role"
			if (ccOfRole1 != null && !ccOfRole1.equals(roleToCC.getValue())) {

				// if the partner-role is in another cc, add the name to the
				// edge between the corresponding cc's

				DefaultEdge edge = null;
				// Get all edges that are between the cc of role0 and role 1
				Set<DefaultEdge> edgeList = ccGraph.getAllEdges(ccToVertexName.get(roleToCC.getValue()),
						ccToVertexName.get(ccOfRole1));
				if (edgeList == null || edgeList.isEmpty()) {
					// If there are no edges of this type, add one
					edge = ccGraph.addEdge(ccToVertexName.get(roleToCC.getValue()), ccToVertexName.get(ccOfRole1));
					matrix.get(roleToCC.getValue().hashCode()).put(ccOfRole1.hashCode(), new Integer(1));
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
				nameForEdge.get(edge).add(OntologyDescriptor
						.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_1_DESIGNATOR.length())));

			} else {
				// if there is no "partner" role role0 is a dataproperty
				// if role0 and role1 have the same cc, they are in the same partition
				String ccName = ccToVertexName.get(roleToCC.getValue());
				// Add the property to the vertex of the xx
				if (vertexToProperties.get(ccName) == null) {
					vertexToProperties.put(ccName, new HashSet<>());
				}
				vertexToProperties.get(ccName).add(OntologyDescriptor
						.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length())));

			}
		}

		// Save matrix to the string
		builder.append("[");

		for (Entry<Integer, Map<Integer, Integer>> row : matrix.entrySet()) {
			builder.append("[");
			for (Entry<Integer, Integer> e : row.getValue().entrySet()) {
				builder.append(hashToCC.get(e.getValue().intValue()) + ", ");
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
				return ontDescriptor.getLabelForConnectedComponent(ccToLogicalAxiomCount.get(vertex),
						ccToOtherAxiomCount.get(vertex), vertexToClasses.get(vertex), vertexToProperties.get(vertex),
						vertexToIndividuals.get(vertex))
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
				count = vertexToAxiom.get(vert) != null ? count + vertexToAxiom.get(vert).size() : count;

			}

			// Map the cc to the number of axioms
			ccToAxiomsCount.put(cc.hashCode() + "", count);
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
					if (!vertexToAxioms.containsKey(cc.hashCode() + "")) {
						vertexToAxioms.put(cc.hashCode() + "", new HashSet<>());
					}
					vertexToAxioms.get(cc.hashCode() + "").addAll(vertexToAxiom.get(vert));

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

	public static void clearMemory() {
		ontHierachy = null;
		ontDescriptor = null;
		ccToLogicalAxiomCount = null;
		ccToOtherAxiomCount = null;
		ccToAxioms = null;
		System.gc();
	}

}
