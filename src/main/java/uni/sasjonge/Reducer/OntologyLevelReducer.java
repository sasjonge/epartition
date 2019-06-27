package uni.sasjonge.Reducer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

/**
 * Removes a given number of "top-levels" (classes without other classes as superclass)
 * 
 * @author sascha
 */
public class OntologyLevelReducer {

	/**
	 * Removes the i highest "levels" of classes of the ontology. Needs the corresponding manager,
	 * datafactory and a reasoner to decide what the top level classes are (structuralreasoner is recommended)
	 * 
	 * @param manager
	 * @param ontology
	 * @param reasoner
	 * @param df
	 * @param i
	 * @return The reduced ontology
	 */
	public static OWLOntology removeHighestLevelConc(OWLOntologyManager manager, OWLOntology ontology,
			OWLReasoner reasoner, OWLDataFactory df, int i) {
		if (i > 0) {

			// Create a list of the classes to remove
			Set<OWLClass> toRemove = new HashSet<>();

			// Save the to level class (which are only subclasses to owlthing)
			Set<OWLClass> currentClassLevel = reasoner.getSubClasses(df.getOWLThing(), true).entities()
					.collect(Collectors.toSet());
			
			// Remove i layers
			while (i > 0) {

				// Add the current level to the list of classes that needs to be removed
				toRemove.addAll(currentClassLevel);
				
				// Calculate the next level
				// Creating the set to save the next level
				Set<OWLClass> newCurrentClassLevel = new HashSet<>();
				// For all classes in the current level
				for (OWLClass cls : currentClassLevel) {
					// Save all subclasses
					newCurrentClassLevel
							.addAll(reasoner.getSubClasses(cls, true).entities().collect(Collectors.toList()));
				}

				// If the next level isn't empty
				if (!newCurrentClassLevel.isEmpty()) {
					// set the current classes to the calculated next level
					currentClassLevel = newCurrentClassLevel;
				}
				i--;
			}

			// Create a ontology removes
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
			// For all classes in the ontology remover
			toRemove.stream().forEach(e -> {
				// if they are not nothing and have no subclasses
				if (!e.isOWLNothing() && !reasoner.getSubClasses(e).isBottomSingleton()) {
					// remove them
					e.accept(remover);
				}
			});
			// apply the changes
			manager.applyChanges(remover.getChanges());
			remover.reset(); 
		}
		return ontology;
	}

}