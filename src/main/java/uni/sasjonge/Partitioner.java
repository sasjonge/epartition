package uni.sasjonge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.xml.sax.SAXException;

import uni.sasjonge.heuristics.levelreducer.OntologyLevelReducer;
import uni.sasjonge.heuristics.upperlevel.UpperLevelManager;
import uni.sasjonge.partitioning.PartitioningCore;
import uni.sasjonge.utils.GraphExporter;
import uni.sasjonge.utils.OntologyDescriptor;

/**
 * 
 * Partioning of an ontology based on the following paper:
 * Jongebloed, Sascha, and Thomas Schneider. "Ontology Partitioning Using E-Connections Revisited." MedRACER+ WOMoCoE@ KR. 2018.
 * Link to paper: http://www.informatik.uni-bremen.de/tdki/research/papers/2018/JS-DL18.pdf
 * 
 * @author sascha
 *
 */
public class Partitioner {
	
	OWLReasonerFactory reasonerFactory;
	OWLReasoner reasoner;
	OWLDataFactory df;
	StringBuilder builder;
	
	public void loadOntology(String input_ontology) {
		
		// A string builder to save the evaluation statistics in a string
		builder = new StringBuilder();

		long startTime = System.nanoTime();
		// Create a owl manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		df = manager.getOWLDataFactory();
		
		
		// Initizalize the partioning algorithm
		PartitioningCore pc = new PartitioningCore();
		
		// Save old ontology
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		
		try {
			// Load the input ontology
			long loadStartTime = System.nanoTime();
			OWLOntology loadedOnt = manager.loadOntology(IRI.create("file:"+input_ontology));
			
			// if rdfs:labels are used as name, map them 
			if (Settings.USE_RDF_LABEL) {
				OntologyDescriptor.initRDFSLabel(loadedOnt);
			}
			
			long loadEndTime = System.nanoTime();
			System.out.println("Loading the ontology took " + (loadEndTime - loadStartTime)/1000000 + "ms");
			
			builder.append(getFileName(input_ontology) + ", ");
			builder.append((loadEndTime - loadStartTime)/1000000 + ", ");
			
			// Check expressivity of the ontology and either stop or handle the cases 
			long expressStartTime = System.nanoTime();
			List<OWLAxiom> axiomsContainingUniveralRole 
				= loadedOnt.referencingAxioms(df.getOWLTopObjectProperty())
														.collect(Collectors.toList());
			
			// If we have universal roles we remove them to a specific treshhold
			if (axiomsContainingUniveralRole.size() > 0) {
				if (Settings.HANDLE_UNIVERSAL_ROLES) {
					
					System.out.println(getFileName(input_ontology) + " has " + axiomsContainingUniveralRole.size());
					
					if (axiomsContainingUniveralRole.size() <= Settings.UNIVERAL_ROLES_TRESHOLD) {
						// Remove all axioms containing the universal role
						loadedOnt.remove(axiomsContainingUniveralRole);
					} else {
						builder = null;
						return;
					}
				} else {
					builder = null;
					return;
				}
			} 
			
			long expressEndTime = System.nanoTime();
			System.out.println("Checking expressivity took " + (expressEndTime - expressStartTime)/1000000 + "ms");	
			
			// Copying ontology
			long copyStartTime = System.nanoTime();
			OWLOntology oldOntology = manager2.copyOntology(loadedOnt,OntologyCopy.DEEP);
			
			long copyEndTime = System.nanoTime();
			System.out.println("Copying the ontology took " + (copyEndTime - copyStartTime)/1000000 + "ms");	
			
			// Create a reasoner#
			long reasonerStartTime = System.nanoTime();
			reasonerFactory = new StructuralReasonerFactory();
			reasoner = reasonerFactory.createNonBufferingReasoner(oldOntology);
			long reasonerEndTime = System.nanoTime();
			System.out.println("Creating a structural readoner took took " + (reasonerEndTime - reasonerStartTime)/1000000 + "ms");
			builder.append((reasonerEndTime - reasonerStartTime)/1000000 + ", ");
	
			long reduceStartTime = System.nanoTime();
			// Remove the "Top-Layer" (the classes highest in the hierachy)
			OWLOntology ontology = OntologyLevelReducer.removeHighestLevelConc(manager,loadedOnt, reasoner, df, Settings.LAYERS_TO_REMOVE);
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
			builder.append((reduceEndTime - reduceStartTime)/1000000 + ",");
			
			long startPartTime = System.nanoTime();
			// Call the partitioning algorithm
			List<OWLOntology> partitionedOntologies = pc.partition(ontology);
			long endPartTime = System.nanoTime();
			System.out.println("Partitioning took " + (endPartTime - startPartTime)/1000000 + "ms");
			builder.append((endPartTime - startPartTime)/1000000 + ", ");
			
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
			// Initiate the graphexporter (create the hierachy and descriptors)
			GraphExporter.init(oldOntology);
			
			// Create the output graph in form of the cc structure
			String graphStructure = GraphExporter.exportCCStructureGraph(pc.g, oldOntology, pc.vertexToAxiom, Settings.GRAPH_OUTPUT_PATH + getFileName(input_ontology) 
			 + ".graphml");

			// Alternative: Create the complex graph created by the algorithm
			//GraphExporter.exportComplexGraph(pc.g, Settings.GRAPH_OUTPUT_PATH);
			
			long endGraphTime = System.nanoTime();
			System.out.println("Graph building took " + (endGraphTime - startGraphTime)/1000000 + "ms");
			
			builder.append((endGraphTime - startGraphTime)/1000000 + ", ");
			
			// Add the output graph to the builder
			builder.append(graphStructure);
		} catch (OWLOntologyCreationException|IOException|ExportException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Return the filename for a given input ontology 
	 * 
	 * @param input_ontology
	 * @return Filename
	 */
	private String getFileName(String input_ontology) {
		String pre = input_ontology.substring(input_ontology.lastIndexOf("/"));
		return pre.substring(0, pre.lastIndexOf('.')).replace(", ]", "]");
	}

	/**
	 * Return the statistics saved in the builder as a string
	 * @return String conatining the statistics of this run
	 */
	public String getStatistics( ) {
		return builder != null ? builder.toString() : null;
	}
	
	public static void main(String[] args) {
		// Read all files in the given directory
		try (Stream<Path> paths = Files.walk(Paths.get("/home/sascha/Desktop/onts"))) {
			
			// Save the statistics for the partitioning in a file
			File fout = new File("/home/sascha/Desktop/statistics.xml");
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		 
		    paths
		        .filter(Files::isRegularFile)
		        .forEach(e -> {
		        	// For each file
		        	// Create a partitioner
		        	Partitioner part = new Partitioner();
		        	// Load and partition the ontology
		        	part.loadOntology(e.toAbsolutePath().toString());		        
		        	try {
		        		// Save the statistics of this partition in a string
		        		String stats = part.getStatistics();
		        		if (stats != null) {
		        			// And write it in the output file
		        			bw.write(stats + "\n");
		        			bw.flush();
		        		}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		        });
		    bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
