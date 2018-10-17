package uni.sasjonge;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uni.sasjonge.Reducer.UpperLevelRemover;

public class BuildUpperLevelJSON {
	
	
	static final String UPPER_LEVEL_ONT_TO_ADD = "file:/home/sascha/workspace/java_ws/partitioner/res/upperlevels/pto_upper.owl";

	public static void main(String[] args) {
		UpperLevelRemover remover = new UpperLevelRemover(Settings.UPPER_LEVEL_FILE);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			OWLOntology loadedOnt = manager.loadOntology(IRI.create(UPPER_LEVEL_ONT_TO_ADD));
			remover.addUpperLevelOntology(loadedOnt);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("saving");
		
		remover.saveToJSONFile();
	}

}
