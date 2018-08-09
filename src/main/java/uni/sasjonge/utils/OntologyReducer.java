package uni.sasjonge.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class OntologyReducer {

	public static OWLOntology removeHighestLevelConc(OWLOntologyManager manager, OWLOntology ontology, int i) {
		if (i > 0) {

			// Returns all objects on the level < i
			List<OWLClass> toRemove = findHighestLevelConcepts(ontology);

			// Remove them from the ontology
			// ontology.remove(toRemove);
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
			toRemove.stream().forEach(e -> {e.accept(remover);System.out.println(e.toString());});
			manager.applyChanges(remover.getChanges());
			remover.reset(); // TODO: Remove if not needed
			return removeHighestLevelConc(manager, ontology, i-1);
		}
		return ontology;
	}

	private static Map<OWLClass,Integer> classToSubClsLevel = new HashMap<>();
	
	private static List<OWLClass> findHighestLevelConcepts(OWLOntology ontology) {
		List<OWLClass> classesToRemove = new ArrayList<>();
		
		// Find all OWLClasses with no super class but with subclasses
		ontology.classesInSignature().forEach(cls -> {
			if(!ontology.getSubClassAxiomsForSuperClass(cls).isEmpty()
					&& ontology.getSubClassAxiomsForSubClass(cls).isEmpty()) {
				System.out.println(cls);
				classesToRemove.add(cls);
			}
		});
		
		return classesToRemove;
	}

}
