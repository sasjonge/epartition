package uni.sasjonge.utils;

import java.io.FileWriter;
import java.io.IOException;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;

/**
 * Utility class for different graph exporting methods
 * 
 * @author sascha
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
		
		// Register additional name attribute for vertices and edges
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
	 * Every node and edge is shown as is in the graph
	 * @param g The graph to export
	 * @param outputPath Parth to output to
	 * @throws ExportException
	 */
	public static void exportCCStructureGraph(Graph<String, DefaultEdge> g, String outputPath) {
		// TODO: Implement
	}
}
