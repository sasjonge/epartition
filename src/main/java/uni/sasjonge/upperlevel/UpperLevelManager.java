package uni.sasjonge.upperlevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Manages and removes upper level ontology. They can be removed
 * as a heuristic in the partitioner
 * 
 * @author sascha
 *
 */
public class UpperLevelManager {
	
	// Stores the upper level ontologies
	Map<String, Set<String>> upperLevelOntologies;
	
	OWLOntologyManager manager;

	private static final Type MAP_TYPE = new TypeToken<Map<String, Set<String>>>() {
	}.getType();
	
	String path;
	
	public UpperLevelManager(String pathToUpperLevel) {
		// Load the current upperlevel-json file 
		Gson gson = new Gson();
		manager = OWLManager.createOWLOntologyManager();
		path = pathToUpperLevel;
		try {
			JsonReader reader = new JsonReader(new FileReader(pathToUpperLevel));
			upperLevelOntologies = gson.fromJson(reader, MAP_TYPE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			upperLevelOntologies = new HashMap<>();
		}
	}
	
	/**
	 * Given an ontology, check how much of the known upper level ontologies it contains
	 * 
	 * @param ontology
	 * @return Map of upper level ontologies to it portion in the given ontology
	 */
	public Map<String, Double> checkForUpperLevel(OWLOntology ontology) {
		// Map to return
		Map<String, Double> toReturn = new HashMap<>();
		
		// Save the names of all axioms of the ontology in a set of strings
		Set<String> ontologyAxioms = new HashSet<>();
		ontology.axioms().forEach(ax -> {
			ontologyAxioms.add(ax.toString());
		});
		
		// For all upper level ontologies
		for (Entry<String, Set<String>> upper : upperLevelOntologies.entrySet()) {
			// Counter for the upper level axioms contained in the given ontology
			double contained = 0;
			
			// For each axiom in the upper level ontology
			for (String ax : upper.getValue()) {
				// if it's contained in the iven ontology
				if (ontologyAxioms.contains(ax)) {
					// count it
					contained = contained + 1.0;
				}
			};
			
			// Calculate the percentage of contained axioms and save it
			toReturn.put(upper.getKey(), contained/upper.getValue().size());
		}
		
		return toReturn;
	}
	
	/**
	 * Remove a known upper level ontology from the given ontology
	 * 
	 * @param ontology
	 * @param upperLevelName
	 * @return Filtered ontology
	 */
	public OWLOntology removeUpperLevel(OWLOntology ontology, String upperLevelName) {

		// Get the upper level ontology
		Set<String> upperLevelOntology = upperLevelOntologies.get(upperLevelName);
		
		// List to save the axiom we want to remove
		List<OWLAxiom> axiomsToRemove = new ArrayList<>();
		
		// For each axiom in the ontology
		ontology.axioms().forEach(ax -> {
			// If the upper level ontology contains it
			if(upperLevelOntology.contains(ax.toString())) {
				// add it to the list of axioms which we want to remove
				axiomsToRemove.add(ax);
			}
		});
		
		// Remove the axioms
		manager.removeAxioms(ontology, axiomsToRemove.stream());
		return ontology;
	}
	
	/**
	 * Given an ontology containing an upper level ontology,
	 * at it to the known upper level ontologies
	 * 
	 * @param upper Upper level ontology
	 */
	public void addUpperLevelOntology(OWLOntology upper) {
		// If we already know the ontology, remove it (this will allow updates)
		if (upperLevelOntologies.containsKey(upper.getOntologyID().toString())) {
			upperLevelOntologies.remove(upper.getOntologyID().toString());
		}
		
		// List of axiom names for the ulo
		Set<String> listofAxioms = new HashSet<>();
		upper.axioms().forEach(e -> {
			listofAxioms.add(e.toString());
		});
		
		// Add it to the map of known upper level ontologies
		upperLevelOntologies.put(upper.getOntologyID().toString(), listofAxioms);
		
	}
	
	/**
	 * Simple wrapper
	 */
	public void saveToJSONFile() {
		saveToJSONFile(path);
	}
	
	/**
	 * Save the known upper level ontologies to the file in json format
	 * @param outputPath
	 */
	public void saveToJSONFile (String outputPath) {
		System.out.println(upperLevelOntologies.entrySet().iterator().next().getValue().size());
		String[] path = outputPath.split(File.separator);
		(new File(path[path.length-1])).mkdirs();
		File file = new File(outputPath);
		if(file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try (Writer writer = new FileWriter(file.getAbsoluteFile())) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(upperLevelOntologies,MAP_TYPE,writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
