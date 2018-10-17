package uni.sasjonge.Reducer;

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

public class UpperLevelRemover {
	
	Map<String, Set<String>> upperLevelOntologies;
	
	OWLOntologyManager manager;

	private static final Type MAP_TYPE = new TypeToken<Map<String, Set<String>>>() {
	}.getType();
	
	String path;
	
	public UpperLevelRemover(String pathToUpperLevel) {
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
	
	public Map<String, Double> checkForUpperLevel(OWLOntology ontology) {
		Map<String, Double> toReturn = new HashMap<>();
		
		Set<String> ontologyAxioms = new HashSet<>();
		ontology.axioms().forEach(ax -> {
			ontologyAxioms.add(ax.toString());
		});
		
		for (Entry<String, Set<String>> upper : upperLevelOntologies.entrySet()) {
			double contained = 0;
			
			for (String ax : upper.getValue()) {
				if (ontologyAxioms.contains(ax)) {
					contained = contained + 1.0;
				}
			};
			
			toReturn.put(upper.getKey(), contained/upper.getValue().size());
		}
		
		return toReturn;
	}
	
	public OWLOntology removeUpperLevel(OWLOntology ontology, String upperLevelName) {
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		
		Set<String> upperLevelOntology = upperLevelOntologies.get(upperLevelName);
		
		List<OWLAxiom> axiomsToRemove = new ArrayList<>();
		
		ontology.axioms().forEach(ax -> {
			if(upperLevelOntology.contains(ax.toString())) {
				axiomsToRemove.add(ax);
			}
		});
		
		manager.removeAxioms(ontology, axiomsToRemove.stream());
		return ontology;
	}
	
	public void addUpperLevelOntology(OWLOntology ontology) {
		if (upperLevelOntologies.containsKey(ontology.getOntologyID().toString())) {
			upperLevelOntologies.remove(ontology.getOntologyID().toString());
		}
		
		Set<String> listofAxioms = new HashSet<>();
		
		ontology.axioms().forEach(e -> {
			listofAxioms.add(e.toString());
		});
		
		upperLevelOntologies.put(ontology.getOntologyID().toString(), listofAxioms);
		
	}
	
	public void saveToJSONFile() {
		saveToJSONFile(path);
	}
	
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
