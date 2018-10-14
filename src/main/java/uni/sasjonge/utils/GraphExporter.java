package uni.sasjonge.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
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
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.google.common.hash.HashCode;

/**
 * Utility class for different graph exporting methods
 * 
 * @author Sascha Jongebloed
 *
 */
public class GraphExporter {

	static OntologyHierarchy ontHierachy;
	static OntologyDescriptor ontDescriptor;

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
	 * @throws ExportException
	 */
	public static void exportCCStructureGraph(Graph<String, DefaultEdge> g, OWLOntology ontology,
			Map<String, OWLAxiom> vertexToAxiom, String outputPath) throws ExportException {

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
		final Map<String, Set<String>> vertexToAxioms = getAxiomsToVertex(ci.connectedSets(), vertexToAxiom);

		// Map vertexes to classes
		// Map vertexes to individuals
		for (Set<String> cc : ci.connectedSets()) {

			// classes and individuals
			// start with same baseset (alle elements of the cc)
			Set<String> classesForThisCC = cc.stream().map(e -> 
				OntologyDescriptor.getCleanName(e)
			).collect(Collectors.toSet());		
			Set<String> individualsForThisCC = new HashSet<>(classesForThisCC);

			classesForThisCC.retainAll(classes);

			if (classesForThisCC.size() > 0) {
				vertexToClasses.put(cc.hashCode() + "", classesForThisCC);
			}

			// individuals
			individualsForThisCC.retainAll(individuals);

			if (individualsForThisCC.size() > 0) {
				vertexToIndividuals.put(cc.hashCode() + "", individualsForThisCC);
			}
		}

		// Add vertexes for all connected components
		ci.connectedSets().stream().forEach(cc -> {
			// System.out
			//		.println("Number of axioms for " + cc.hashCode() + " is " + vertexToAxioms.get(cc.hashCode() + ""));
			if (vertexToAxioms.get(cc.hashCode() + "") != null) {
				ccToVertexName.put(cc, cc.hashCode() + "");
				ccGraph.addVertex(cc.hashCode() + "");
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
		Map<DefaultEdge, Set<String>> nameForEdge = new HashMap<>();
		Map<String, Set<String>> vertexToProperties = new HashMap<>();
		ccWithAxioms.stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				if (subcon.endsWith("0")) {
					Set<String> correspondingCC = subToCC.get(subcon.substring(0, subcon.length() - 1) + "1");
					if (correspondingCC != null) {
						if (!ccToVertexName.get(cc).equals(ccToVertexName.get(correspondingCC))) {
							DefaultEdge edge = null;
							Set<DefaultEdge> edgeList = ccGraph.getAllEdges(ccToVertexName.get(cc),
									ccToVertexName.get(correspondingCC));
							if (edgeList.isEmpty()) {
								// If there are no edges of this type, add one
								edge = ccGraph.addEdge(ccToVertexName.get(cc), ccToVertexName.get(correspondingCC));

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
									.add(OntologyDescriptor.getCleanName(subcon.substring(0, subcon.length() - 1)));

						} else {
							String ccName = ccToVertexName.get(cc);
							if (vertexToProperties.get(ccName) == null) {
								vertexToProperties.put(ccName, new HashSet<>());
							}
							vertexToProperties.get(ccName).add(OntologyDescriptor.getCleanName(subcon.substring(0, subcon.length() - 1)));
						}
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
				return ontDescriptor.getLabelForConnectedComponent(vertexToAxioms.get(vertex).size(),
						vertexToClasses.get(vertex), vertexToProperties.get(vertex), vertexToIndividuals.get(vertex));
			}
		});
		// Register additional name attribute for edges
		exporter.setEdgeLabelProvider(new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge edge) {
				return ontDescriptor.getFilteredPropertyString(nameForEdge.get(edge), 3);
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

	private static Map<String, Set<String>> getAxiomsToVertex(List<Set<String>> connectedSets,
			Map<String, OWLAxiom> vertexToAxiom) {

		Map<String, Set<String>> vertexToAxioms = new HashMap<>();
		for (Set<String> cc : connectedSets) {
			for (String vert : cc) {
				if (vertexToAxiom.get(vert) != null) {
					if (!vertexToAxioms.containsKey(cc.hashCode() + "")) {
						vertexToAxioms.put(cc.hashCode() + "", new HashSet<>());
					}
					vertexToAxioms.get(cc.hashCode() + "").add(vertexToAxiom.get(vert).toString());
				}
			}
		}
		return vertexToAxioms;
	}

	private static Set<String> getClassesForOntology(OWLOntology ontology) {
		return ontology.classesInSignature().map(e -> OntologyDescriptor.getCleanName(e.toString()))
				.collect(Collectors.toSet());
	}

	private static Set<String> getIndividualsForOntology(OWLOntology ontology) {
		return ontology.individualsInSignature().map(e -> OntologyDescriptor.getCleanName(e.toString()))
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
