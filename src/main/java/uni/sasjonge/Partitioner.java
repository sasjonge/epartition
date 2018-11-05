package uni.sasjonge;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.xml.sax.SAXException;

import uni.sasjonge.Reducer.OntologyLevelReducer;
import uni.sasjonge.Reducer.UpperLevelRemover;
import uni.sasjonge.partitioning.PartitioningCore;
import uni.sasjonge.utils.GraphExporter;
import uni.sasjonge.utils.OntologyDescriptor;

public class Partitioner {
	
	
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// Initizalize the partioning algorithm
		PartitioningCore pc = new PartitioningCore();
		
		// Save old ontology
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		
		try {
			// Load the input ontology
			long loadStartTime = System.nanoTime();
			OWLOntology loadedOnt = manager.loadOntology(IRI.create(Settings.INPUT_ONTOLOGY));
			// if rdfs:labels are used as name, map them
			if (Settings.USE_RDF_LABEL) {
				OntologyDescriptor.initRDFSLabel(loadedOnt);
			}
			
			OWLOntology oldOntology = manager2.copyOntology(loadedOnt,OntologyCopy.DEEP);

			long loadEndTime = System.nanoTime();
			System.out.println("Loading the ontology took " + (loadEndTime - loadStartTime)/1000000 + "ms");

			long reduceStartTime = System.nanoTime();
			OWLOntology ontology = OntologyLevelReducer.removeHighestLevelConc(manager,loadedOnt,Settings.LAYERS_TO_REMOVE);
			//OWLOntology ontology = (new OntologyReducer(manager,loadedOnt)).removeHighestLevelConc(3);

//			UpperLevelRemover ulRemove = new UpperLevelRemover(Settings.UPPER_LEVEL_FILE);
//			System.out.println(ulRemove.checkForUpperLevel(loadedOnt));
//			OWLOntology ontology = loadedOnt;
//			for(Entry<String, Double> entry : ulRemove.checkForUpperLevel(loadedOnt).entrySet()) {
//				if (entry.getValue().doubleValue() >= 1.0d) {
//					ontology = ulRemove.removeUpperLevel(loadedOnt, entry.getKey());
//				}
//			}
			
			long reduceEndTime = System.nanoTime();
			System.out.println("Reducing the ontology took " + (reduceEndTime - reduceStartTime)/1000000 + "ms");
			
			long startPartTime = System.nanoTime();
			// Call the partitioning algorithm
			System.out.println(ontology);
			List<OWLOntology> partitionedOntologies = pc.partition(ontology);
			long endPartTime = System.nanoTime();
			System.out.println("Partitioning took " + (endPartTime - startPartTime)/1000000 + "ms");
			
			// Export the onotologys
			//partitionedOntologies.stream().forEach(t -> {
			//partitionedOntologies.stream().forEach(t -> {
			//	try {
			//		manager.saveOntology(t);
			//	} catch (OWLOntologyStorageException e) {
			//		e.printStackTrace();
			//	}
			//});
			
			// Export the graph
			long startGraphTime = System.nanoTime();
			GraphExporter.init(oldOntology);

			GraphExporter.exportCCStructureGraph(pc.g, oldOntology, pc.vertexToAxiom, Settings.GRAPH_OUTPUT_PATH);

			//GraphExporter.exportComplexGraph(pc.g, Settings.GRAPH_OUTPUT_PATH);
			long endGraphTime = System.nanoTime();
			System.out.println("Graph building took " + (endGraphTime - startGraphTime)/1000000 + "ms");
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
		long endTime = System.nanoTime();
		System.out.println("Overall: " + (endTime - startTime)/1000000 + "ms");

	}
}
