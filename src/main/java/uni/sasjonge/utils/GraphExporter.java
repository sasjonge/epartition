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

	static OntologyHierarchy ontHierachy;
	static OntologyDescriptor ontDescriptor;

	static Map<String, Integer> vertexToAxiomsCount = null;

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
		
		StringBuilder builder = new StringBuilder();

		// Classes and individuals of the given ontology
		Set<String> classes = getClassesForOntology(ontology);

		Set<String> individuals = getIndividualsForOntology(ontology);

		// Find the connected components
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

		Map<String, Set<OWLAxiom>> vertexToAxioms = null;
		if (Settings.SHOW_AXIOMS) {
			vertexToAxioms = getAxiomsToVertex(ci.connectedSets(), vertexToAxiom);
			GraphExporter.vertexToAxiomsCount = new HashMap<>();
			for (Entry<String, Set<OWLAxiom>> e : vertexToAxioms.entrySet()) {
				vertexToAxiomsCount.put(e.getKey(), e.getValue().size());
			}
		} else {
			vertexToAxiomsCount = getAxiomsToVertexCount(ci.connectedSets(), vertexToAxiom);
		}
		
		// Save how many axioms there are per vertex

		// Map vertexes to classes
		// Map vertexes to individuals
		for (Set<String> cc : ci.connectedSets()) {

			// classes and individuals
			// start with same baseset (alle elements of the cc)
			Set<String> classesForThisCC = cc.stream().map(e -> OntologyDescriptor.getCleanName(e))
					.collect(Collectors.toSet());
			Set<String> individualsForThisCC = new HashSet<>(classesForThisCC);

			classesForThisCC.retainAll(classes);

			if (classesForThisCC.size() > 0) {
				vertexToClasses.put(cc.toString() + "", classesForThisCC);
			}

			// individuals
			individualsForThisCC.retainAll(individuals);

			if (individualsForThisCC.size() > 0) {
				vertexToIndividuals.put(cc.toString() + "", individualsForThisCC);
			}
		}

		builder.append("[");
		// Add vertexes for all connected components
		for (Set<String> cc : ci.connectedSets()) {
			// System.out
			// .println("Number of axioms for " + cc.toString() + " is " +
			// vertexToAxioms.get(cc.toString() + ""));
			if (vertexToAxiomsCount.get(cc.toString() + "") != null) {
				builder.append(vertexToAxiomsCount.get(cc.toString() + "").toString() + ", ");
				ccToVertexName.put(cc, cc.toString() + "");
				ccGraph.addVertex(cc.toString() + "");
				ccWithAxioms.add(cc);
				i++;
			}
		}
		;
		builder.append("], ");
		
		// Create a map of map as a matrix for connectedness
		Map<String, Map<String, Integer>> matrix = new HashMap<>();
		for (Set<String> cc : ci.connectedSets()) {
			matrix.put(cc.toString() + "", new HashMap<>());
			for (Set<String> cc2 : ci.connectedSets()) {
				matrix.get(cc.toString() + "").put(cc2.toString() + "", new Integer(0));
			}
		}

		// Create a map from subconcepts to the cc that contains it
		Map<String, Set<String>> role0ToCC = new HashMap<>();
		Map<String, Set<String>> role1ToCC = new HashMap<>();
		ccWithAxioms.stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
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
		
		for (Entry<String, Set<String>> roleToCC : role0ToCC.entrySet()) {
			String role0 = roleToCC.getKey();
			Set<String> ccOfRole1 = role1ToCC.get(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length()) + Settings.PROPERTY_1_DESIGNATOR);
			
			// If there is a "partner-role"
			if (ccOfRole1 != null && !ccOfRole1.equals(roleToCC.getValue())) {
				
				// if the partner-role is in another cc, add the name to the
				// edge between the corresponding cc's
				
				DefaultEdge edge = null;
				Set<DefaultEdge> edgeList = ccGraph.getAllEdges(ccToVertexName.get(roleToCC.getValue()),
						ccToVertexName.get(ccOfRole1));
				if (edgeList.isEmpty()) {
					// If there are no edges of this type, add one
					edge = ccGraph.addEdge(ccToVertexName.get(roleToCC.getValue()), ccToVertexName.get(ccOfRole1));
					matrix.get(roleToCC.getValue() + "").put(ccOfRole1.toString() + "",new Integer(1));
				} else if (edgeList.size() == 1) {
					// If the edgelist is not empty it should only contain one element.
					edge = edgeList.iterator().next();
				}

				// Add the hashset if it doesn't exist already
				if (nameForEdge.get(edge) == null) {
					nameForEdge.put(edge, new HashSet<String>());
				}

				// Then add the name of the edge
				// System.out.println(getCleanName(subcon.substring(0, subcon.length() - 1)));
				nameForEdge.get(edge)
						.add(OntologyDescriptor.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_1_DESIGNATOR.length())));

				
			} else {
				// if there is no "partner" role role0 is a dataproperty
				// if role0 and role1 have the same cc, they are in the same partition
				String ccName = ccToVertexName.get(roleToCC.getValue());
				if (vertexToProperties.get(ccName) == null) {
					vertexToProperties.put(ccName, new HashSet<>());
				}
				vertexToProperties.get(ccName)
						.add(OntologyDescriptor.getCleanName(role0.substring(0, role0.length() - Settings.PROPERTY_0_DESIGNATOR.length())));

			}
		}
		
		// Save matrix
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
				return ontDescriptor.getLabelForConnectedComponent(vertexToAxiomsCount.get(vertex),
						vertexToClasses.get(vertex), vertexToProperties.get(vertex), vertexToIndividuals.get(vertex))
						+ (Settings.SHOW_AXIOMS ? "\n" + ontDescriptor.getAxiomString(vertexToAxioms.get(vertex)) : "");
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

		// exporter.setVertexLabelAttributeName("custom_vertex_label");
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

	private static Map<String, Set<OWLAxiom>> getAxiomsToVertex(List<Set<String>> connectedSets,
			Map<String, Set<OWLAxiom>> vertexToAxiom) {

		Map<String, Set<OWLAxiom>> vertexToAxioms = new HashMap<>();
		for (Set<String> cc : connectedSets) {
			for (String vert : cc) {
				if (vertexToAxiom.get(vert) != null) {
					if (!vertexToAxioms.containsKey(cc.toString() + "")) {
						vertexToAxioms.put(cc.toString() + "", new HashSet<>());
					}
					vertexToAxioms.get(cc.toString() + "").addAll(vertexToAxiom.get(vert));

				}
			}
		}

		return vertexToAxioms;
	}

	private static Map<String, Integer> getAxiomsToVertexCount(List<Set<String>> connectedSets,
			Map<String, Set<OWLAxiom>> vertexToAxiom) {
		Map<String, Integer> vertexToAxioms = new HashMap<>();
		for (Set<String> cc : connectedSets) {
			int count = 0;
			for (String vert : cc) {

				count = vertexToAxiom.get(vert) != null ? count + vertexToAxiom.get(vert).size() : count;

			}
			vertexToAxioms.put(cc.toString() + "",count);
		}

		return vertexToAxioms;
	}

	private static Set<String> getClassesForOntology(OWLOntology ontology) {
		return ontology.classesInSignature().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
				.collect(Collectors.toSet());
	}

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
			ontHierachy = new OntologyHierarchy(ontology);

			ontDescriptor = new OntologyDescriptor(ontHierachy, ontology);
			// System.out.println(depthToClasses);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
