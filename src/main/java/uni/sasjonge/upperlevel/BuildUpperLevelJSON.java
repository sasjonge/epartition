package uni.sasjonge.upperlevel;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uni.sasjonge.Settings;

/**
 * Tool to create a JSON file representing one or more upper level ontology (the JSON file 
 * will be extended if you rerun this code on it)
 * 
 * @author sascha
 */
public class BuildUpperLevelJSON {
	
	// AN upper level ontology to add
	static final String UPPER_LEVEL_ONT_TO_ADD = "file:/home/sascha/workspace/java_ws/partitioner/res/upperlevels/pto_upper.owl";

	public static void main(String[] args) {
		// Create the upper level manager
		UpperLevelManager remover = new UpperLevelManager(Settings.UPPER_LEVEL_FILE);

		// load the upper level ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			OWLOntology loadedOnt = manager.loadOntology(IRI.create(UPPER_LEVEL_ONT_TO_ADD));
			// Add it to the upper level manager
			remover.addUpperLevelOntology(loadedOnt);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("saving");
		
		remover.saveToJSONFile();
	}

}
