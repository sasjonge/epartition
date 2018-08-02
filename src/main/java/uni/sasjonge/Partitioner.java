package uni.sasjonge;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.xml.sax.SAXException;

import uni.sasjonge.partitioning.PartitioningCore;
import uni.sasjonge.utils.GraphExporter;

public class Partitioner {
	
	// The output path for the graph
	static final String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/test.graphml";
	
	// The input ontology
	static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/koala2.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/partitioner_test.owl";

	
	public static void main(String[] args) {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// Initizalize the partioning algorithm
		PartitioningCore pc = new PartitioningCore();
		
		try {
			// Load the input ontology
			OWLOntology ontology = manager.loadOntology(IRI.create(INPUT_ONTOLOGY));
			
			// Call the partitioning algorithm
			List<OWLOntology> partitionedOntologies = pc.partition(ontology);
			
			// Export the onotologys
			partitionedOntologies.stream().forEach(t -> {
				try {
					manager.saveOntology(t);
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				}
			});
			
			// Export the graph
			GraphExporter.exportCCStructureGraph(pc.g, pc.edgeToAxioms, GRAPH_OUTPUT_PATH);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
