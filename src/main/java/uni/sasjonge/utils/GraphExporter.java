package uni.sasjonge.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

/**
 * Utility class for different graph exporting methods
 * 
 * @author Sascha Jongebloed
 *
 */
public class GraphExporter {

	public static ManchesterOWLSyntaxOWLObjectRendererImpl manchester = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	static Map<String, String> mapVertexToManchester;

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
				return getCleanName(vertex);
			}
		});
		exporter.setVertexIDProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return getCleanName(vertex);
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
	 * @throws ExportException
	 */
	public static void exportCCStructureGraphSimple(Graph<String, DefaultEdge> g, OWLOntology ontology,
			String outputPath) throws ExportException {

		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);

		// Create the graph we want to output
		Graph<String, DefaultEdge> ccGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Maps a name to each cc
		Map<Set<String>, String> ccToVertexName = new HashMap<>();

		// Add vertexes for all connected components
		ci.connectedSets().stream().forEach(cc -> {
			String name = "Partition " + i + "//" + getFilteredSubConceptString(cc, ontology, 3);
			ccToVertexName.put(cc, name);
			ccGraph.addVertex(name);
			i++;
		});

		// Create a map from subconcepts to the cc that contains it
		Map<String, Set<String>> subToCC = new HashMap<>();
		ci.connectedSets().stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				subToCC.put(subcon, cc);
			});
		});

		// Go through each cc and find corresponding property nodes (same prefix, ending
		// with 0 or 1)
		// remember the name of properties for the edges
		Map<DefaultEdge, String> nameForEdge = new HashMap<>();
		ci.connectedSets().stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				if (subcon.endsWith("0")) {
					Set<String> correspondingCC = subToCC.get(subcon.substring(0, subcon.length() - 1) + "1");
					if (correspondingCC != null) {
						DefaultEdge edge = ccGraph.addEdge(ccToVertexName.get(cc), ccToVertexName.get(correspondingCC));
						nameForEdge.put(edge, getCleanName(subcon.substring(0, subcon.length() - 1)));
					}
				}
			});
		});

		/////////////////////////////////////////////////////////////////////////
		// Export the Graph
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();

		// Register additional name attribute for vertices
		exporter.setVertexLabelProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return getCleanName(vertex);
			}
		});
		// Register additional name attribute for edges
		exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge edge) {
				return nameForEdge.get(edge);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports g in graphML to outputPath Every node is a connected component, every
	 * edge stands for a ij-property
	 * 
	 * @param g          The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportCCStructureGraphSimpleHideAxiomLestt(Graph<String, DefaultEdge> g, OWLOntology ontology,
			Map<String, OWLAxiom> vertexToAxiom, String outputPath) throws ExportException {

		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);

		// Create the graph we want to output
		Graph<String, DefaultEdge> ccGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Maps a name to each cc
		Map<Set<String>, String> ccToVertexName = new HashMap<>();

		// CC's with axioms
		List<Set<String>> ccWithAxioms = new ArrayList<>();

		// Add vertexes for all connected components
		ci.connectedSets().stream().forEach(cc -> {
			if (hasAxioms(cc, vertexToAxiom)) {
				String name = "Partition " + i + "//" + getFilteredSubConceptString(cc, ontology, 3);
				ccToVertexName.put(cc, name);
				ccGraph.addVertex(name);
				ccWithAxioms.add(cc);
				i++;
			}
		});

		// Create a map from subconcepts to the cc that contains it
		Map<String, Set<String>> subToCC = new HashMap<>();
		ccWithAxioms.stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				subToCC.put(subcon, cc);
			});
		});

		// Go through each cc and find corresponding property nodes (same prefix, ending
		// with 0 or 1)
		// remember the name of properties for the edges
		Map<DefaultEdge, List<String>> nameForEdge = new HashMap<>();
		ccWithAxioms.stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				if (subcon.endsWith("0")) {
					Set<String> correspondingCC = subToCC.get(subcon.substring(0, subcon.length() - 1) + "1");
					if (correspondingCC != null) {
						DefaultEdge edge = null;
						Set<DefaultEdge> edgeList = ccGraph.getAllEdges(ccToVertexName.get(cc),
								ccToVertexName.get(correspondingCC));
						if (edgeList.isEmpty()) {
							edge = ccGraph.addEdge(ccToVertexName.get(cc), ccToVertexName.get(correspondingCC));
						} else {
							edge = edgeList.iterator().next();
						};
						
						if (nameForEdge.get(edge) == null) {
							nameForEdge.put(edge, new ArrayList<String>());
						}
						System.out.println(getCleanName(subcon.substring(0, subcon.length() - 1)));
						nameForEdge.get(edge).add(getCleanName(subcon.substring(0, subcon.length() - 1)));
					}
				}
			});
		});
		System.out.println("/////////" + nameForEdge.toString() + "/////");

		/////////////////////////////////////////////////////////////////////////
		// Export the Graph
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();

		// Register additional name attribute for vertices
		exporter.setVertexLabelProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return getCleanName(vertex);
			}
		});
		// Register additional name attribute for edges
		exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge edge) {
				return nameForEdge.get(edge).toString();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean hasAxioms(Set<String> cc, Map<String, OWLAxiom> vertexToAxiom) {

		for (String vert : cc) {
			if (vertexToAxiom.containsKey(vert)) {
				return true;
			}
		}
		return false;
	}

	static boolean hasClasses = true;

	private static String getFilteredSubConceptString(Set<String> cc, OWLOntology ontology) {
		StringBuilder builder = new StringBuilder();
		builder.append("Classes:\n");

		// Descend into the classes to find classnames
		int depth = 0;
		hasClasses = true;
		while (hasClasses) {
			if (depthToClasses.get(depth) != null) {
				depthToClasses.get(depth).stream().forEach(cls -> {
					if (cc.contains(cls.toString())) {
						builder.append(getCleanName(cls.toString()));
						builder.append("\n");
						hasClasses = false;
					}
				});
			} else {
				hasClasses = false;
			}
			depth++;
		}

		return builder.toString();
	}

	private static String getFilteredSubConceptString(Set<String> cc, OWLOntology ontology, int maxNumberOfConcepts) {
		StringBuilder builder = new StringBuilder();
		builder.append("Classes:\n");

		// Descend into the classes to find classnames
		int depth = 0;
		int classesInString = 0;
		boolean addLastClass = false;
		hasClasses = true;
		while (hasClasses) {
			if (depthToClasses.get(depth) != null) {
				for (OWLClass cls : depthToClasses.get(depth)) {
					if (cc.contains(cls.toString())) {
						builder.append(getCleanName(cls.toString()));
						builder.append("\n");
						classesInString++;
						// add dots and the last element
						if (classesInString > maxNumberOfConcepts - 2) {
							if (depthToClasses.get(depth).size() > maxNumberOfConcepts - 1) {
								addLastClass = true;
							}
							break;
						}
						hasClasses = false;
					}
				}
				;
			}

			if (addLastClass) {
				builder.append("...\n");
				List<OWLClass> classes = depthToClasses.get(depth);
				builder.append(classes.get(classes.size() - 1));
			}

			depth++;
		}

		return builder.toString();
	}

	/**
	 * Exports g in graphML to outputPath Every node is a connected component, every
	 * edge stands for a ij-property
	 * 
	 * @param g          The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportCCStructureGraphWithAllSubConceptsAndAx(Graph<String, DefaultEdge> g,
			Map<String, OWLAxiom> vertexToAxiom, String outputPath) throws ExportException {

		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);

		// Create the graph we want to output
		Graph<String, DefaultEdge> ccGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

		// Maps a name to each cc
		Map<Set<String>, String> ccToVertexName = new HashMap<>();

		// Add vertexes for all connected components
		ci.connectedSets().stream().forEach(cc -> {
			String name = getSubConceptString(cc) + getAxiomString(cc, g, vertexToAxiom);
			ccToVertexName.put(cc, name);
			ccGraph.addVertex(name);
		});

		// Create a map from subconcepts to the cc that contains it
		Map<String, Set<String>> subToCC = new HashMap<>();
		ci.connectedSets().stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				subToCC.put(subcon, cc);
			});
		});

		// Go through each cc and find corresponding property nodes (same prefix, ending
		// with 0 or 1)
		// remember the name of properties for the edges
		Map<DefaultEdge, String> nameForEdge = new HashMap<>();
		ci.connectedSets().stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				if (subcon.endsWith("0")) {
					Set<String> correspondingCC = subToCC.get(subcon.substring(0, subcon.length() - 1) + "1");
					if (correspondingCC != null) {
						DefaultEdge edge = ccGraph.addEdge(ccToVertexName.get(cc), ccToVertexName.get(correspondingCC));
						nameForEdge.put(edge, getCleanName(subcon.substring(0, subcon.length() - 1)));
					}
				}
			});
		});

		/////////////////////////////////////////////////////////////////////////
		// Export the Graph
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();

		// Register additional name attribute for vertices
		exporter.setVertexLabelProvider(new ComponentNameProvider<String>() {
			@Override
			public String getName(String vertex) {
				return vertex.toString();
			}
		});
		// Register additional name attribute for edges
		exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge edge) {
				return nameForEdge.get(edge);
			}
		});

		// exporter.setVertexLabelAttributeName("custom_vertex_label");blob:https://web.whatsapp.com/c30b1c56-9eec-468a-8ff3-c942d79089ec

		// Initizalize Filewriter and export the corresponding graph
		FileWriter fw;
		try {
			fw = new FileWriter(outputPath);
			exporter.exportGraph(ccGraph, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a String of all Axioms corresponding to the given connected component
	 * 
	 * @param cc              Connected Component, List of Vertex Names
	 * @param g               Graph
	 * @param edgeToAxiomName A map from edges to axioms
	 * @return A String in form of a list of all axioms
	 */
	private static String getAxiomString(Set<String> cc, Graph<String, DefaultEdge> g,
			Map<String, OWLAxiom> vertexToAxiom) {

		// Build the string
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("\n--------\nAxioms:\n");
		cc.stream().forEach(vertex -> {
			if (vertexToAxiom.containsKey(vertex)) {
				toReturn.append(getCleanName(manchester.render(vertexToAxiom.get(vertex))));
				toReturn.append("\n");
			}
		});

		return toReturn.toString();
	}

	/**
	 * Returns a String of all subConcepts corresponding to the given connected
	 * component
	 * 
	 * @param cc              Connected Component, List of Vertex Names
	 * @param g               Graph
	 * @param edgeToAxiomName A map from edges to sub concepts
	 * @return A String in form of a list of all sub concepts
	 */
	private static String getSubConceptString(Set<String> cc) {
		// Build the string
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("Subconcepts:\n");
		cc.stream().forEach(vertex -> {
			toReturn.append(getCleanName(mapVertexToManchester.get(vertex)));
			toReturn.append("\n");
		});

		return toReturn.toString();
	}

	public static String getCleanName(String owlName) {
		return owlName.replaceAll("http[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*#|<|>", "");
	}

	/**
	 * Creates a mapping from all vertexes in our graphs to their corresponding
	 * manchester syntax
	 * 
	 * @param ontology The corresponding ontology
	 */
	private static Map<String, String> mapVertexToManchester(OWLOntology ontology) {
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

	/**
	 * Initiates mappings
	 * 
	 * @param ontology
	 */
	public static void init(OWLOntology ontology) {
		mapVertexToManchester = mapVertexToManchester(ontology);

		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			initDepthToClass(manager, manager.copyOntology(ontology, OntologyCopy.DEEP), 0);

			// System.out.println(depthToClasses);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static Map<Integer, List<OWLClass>> depthToClasses = new HashMap<>();
	static boolean somethingChanged = false;

	private static void initDepthToClass(OWLOntologyManager manager, OWLOntology ont, int depth) {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
		OWLDataFactory df = manager.getOWLDataFactory();

		List<OWLClass> cls = reasoner.getSubClasses(df.getOWLThing(), true).entities().collect(Collectors.toList());

		if (!cls.isEmpty()) {
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ont));
			cls.stream().forEach(e -> {
				if (!e.isOWLNothing() && !reasoner.getSubClasses(e).isBottomSingleton()) {
					somethingChanged = true;
					e.accept(remover);
				}
			});
			manager.applyChanges(remover.getChanges());
			remover.reset(); // TODO: Remove if not needed

			depthToClasses.put(depth, cls);
			if (somethingChanged) {
				somethingChanged = false;
				initDepthToClass(manager, ont, depth + 1);
			}
		}
	}

}
