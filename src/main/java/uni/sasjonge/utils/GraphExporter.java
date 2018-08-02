package uni.sasjonge.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;

/**
 * Utility class for different graph exporting methods
 * 
 * @author Sascha Jongebloed
 *
 */
public class GraphExporter {

	/**
	 * Exports g in graphML to outputPath
	 * Every node and edge is shown as is in the graph
	 * @param g The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void  exportComplexGraph(Graph<String, DefaultEdge> g, String outputPath) throws ExportException {
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
		
		// Register additional name attribute for vertices
        exporter.setVertexLabelProvider(new ComponentNameProvider<String>()
        {
            @Override
            public String getName(String vertex)
            {
                return vertex.toString();
            }
        });
        exporter.setVertexIDProvider(new ComponentNameProvider<String>()
        {
            @Override
            public String getName(String vertex)
            {
                return vertex.toString();
            }
        });
        //exporter.setVertexLabelAttributeName("custom_vertex_label");

        // Initizalize Filewriter and export the corresponding graph
		FileWriter fw;
		try {
			fw = new FileWriter(outputPath);
			exporter.exportGraph(g,fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();			
		}	
	}
	
	/**
	 * Exports g in graphML to outputPath
	 * Every node is a connected component, every edge stands for a ij-property
	 * 
	 * @param g The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportCCStructureGraph(Graph<String, DefaultEdge> g, Map<DefaultEdge,String> edgeToAxiomName, String outputPath) throws ExportException {
		
		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);
		
		// Create the graph we want to output
		Graph<String, DefaultEdge> ccGraph = new SimpleGraph<>(DefaultEdge.class);
		
		// Maps a name to each cc
		Map<Set<String>,String> ccToVertexName = new HashMap<>();
				
		// Add vertexes for all connected components
		ci.connectedSets().stream().forEach(cc -> {
			String name = "Subconcepts:\n"+String.join("\n", cc) + getAxiomString(cc,g,edgeToAxiomName);
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
		
		// Go through each cc and find corresponding property nodes (same prefix, ending with 0 or 1)
		// remember the name of properties for the edges
		Map<DefaultEdge, String> nameForEdge = new HashMap<>();
		ci.connectedSets().stream().forEach(cc -> {
			cc.stream().forEach(subcon -> {
				if (subcon.endsWith("0")) {
					Set<String> correspondingCC = subToCC.get(subcon.substring(0,subcon.length()-1)+"1");
					if (correspondingCC != null) {
						DefaultEdge edge = ccGraph.addEdge(ccToVertexName.get(cc), ccToVertexName.get(correspondingCC));
						nameForEdge.put(edge, subcon.substring(0,subcon.length()-1));
					}
				}
			});
		});
		
		/////////////////////////////////////////////////////////////////////////
		// Export the Graph
		GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>();
		
		// Register additional name attribute for vertices
        exporter.setVertexLabelProvider(new ComponentNameProvider<String>()
        {
            @Override
            public String getName(String vertex)
            {
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
        //exporter.setVertexLabelAttributeName("custom_vertex_label");blob:https://web.whatsapp.com/c30b1c56-9eec-468a-8ff3-c942d79089ec

        // Initizalize Filewriter and export the corresponding graph
		FileWriter fw;
		try {
			fw = new FileWriter(outputPath);
			exporter.exportGraph(ccGraph,fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();			
		}	
	}

	/**
	 * Returns a String of all Axioms corresponding to the given connected component
	 * 
	 * @param cc Connected Component, List of Vertex Names
	 * @param g Graph
	 * @param edgeToAxiomName A map from edges to axioms
	 * @return A String in form of a list of all axioms
	 */
	private static String getAxiomString(Set<String> cc, Graph<String, DefaultEdge> g,
			Map<DefaultEdge, String> edgeToAxiomName) {
		
		// First create a map that maps vertexes to all edges for which they are the source
		Map<String, Set<DefaultEdge>> vertexToEdges = new HashMap<>();
		g.edgeSet().stream().forEach(edge -> {
			String source = g.getEdgeSource(edge);
			if(vertexToEdges.containsKey(source)) {
				vertexToEdges.get(source).add(edge);
			} else {
				Set<DefaultEdge> newSet = new HashSet<>();
				newSet.add(edge);
				vertexToEdges.put(source, newSet);
			}
		});
		
		// Build the string
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("\n--------\nAxioms.\n");
		cc.stream().forEach(vertex -> {
			System.out.println(vertexToEdges.get(vertex));
			if (vertexToEdges.containsKey(vertex)) {
				vertexToEdges.get(vertex).stream().forEach(edge -> {
					// Here check wether the edge is "labelled
					if (edgeToAxiomName.containsKey(edge)) {
						toReturn.append(edgeToAxiomName.get(edge));
						toReturn.append("\n");
					}
				});;
			}
		});
		
		return toReturn.toString();
	}
}
