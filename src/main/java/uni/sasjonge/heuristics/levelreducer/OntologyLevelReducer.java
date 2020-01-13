package uni.sasjonge.heuristics.levelreducer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import uni.sasjonge.Settings;
import uni.sasjonge.utils.OntologyDescriptor;

/**
 * Removes a given number of "top-levels" (classes without other classes as
 * superclass)
 * 
 * @author sascha
 */
public class OntologyLevelReducer {

	public static boolean changedSomething = false;

	/**
	 * Removes the i highest "levels" of classes of the ontology. Needs the
	 * corresponding manager, datafactory and a reasoner to decide what the top
	 * level classes are (structuralreasoner is recommended)
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

		// Save the list of logical axioms to calculate which axoms where removed
		List<OWLLogicalAxiom> prevLogicalAxioms = null;
		if (Settings.PRINT_REMOVED_AXIOMS) {
			prevLogicalAxioms = ontology.logicalAxioms().collect(Collectors.toList());
		}

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

		if (Settings.PRINT_REMOVED_AXIOMS) {
			// Print the removed axioms
			prevLogicalAxioms.removeAll(ontology.logicalAxioms().collect(Collectors.toSet()));
			System.out.println("------------------- removed ---------------------");
			prevLogicalAxioms.stream().forEach(ax -> System.out.println(OntologyDescriptor.getCleanNameOWLObj(ax)));
			System.out.println("-------------------------------------------------");
		}
		return ontology;
	}

	public static OWLOntology removeHighestLevelLimited(OWLOntologyManager manager, OWLOntology ontology, OWLReasoner reasoner, OWLDataFactory df, OWLOntology filterOntology) {
		changedSomething = false;

		// Save the list of logical axioms to calculate which axoms where removed
		List<OWLLogicalAxiom> prevLogicalAxioms = null;
		if (Settings.PRINT_REMOVED_AXIOMS) {
			prevLogicalAxioms = ontology.logicalAxioms().collect(Collectors.toList());
		}

		// Create a list of the classes to remove
		Set<OWLClass> toRemove = new HashSet<>();

		// Save the to level class (which are only subclasses to owlthing)
		Set<OWLClass> topLayerOfBiggest = reasoner.getSubClasses(df.getOWLThing(), true).entities()
				.filter(e -> filterOntology.containsClassInSignature(e.getIRI())).collect(Collectors.toSet());

		System.out.println(topLayerOfBiggest);

		// Create a ontology removes
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		// For all classes in the ontology remover
		topLayerOfBiggest.stream().forEach(e -> {
			// if they are not nothing and have no subclasses
			if (!e.isOWLNothing() && !reasoner.getSubClasses(e).isBottomSingleton()) {
				// remove them
				e.accept(remover);
			}
		});

		// apply the changes
		ChangeApplied changes = manager.applyChanges(remover.getChanges());

		// If changes where made flag ist
		if (changes.equals(ChangeApplied.SUCCESSFULLY)) {
			changedSomething = true;
		}

		if (Settings.PRINT_REMOVED_AXIOMS) {
			// Print the removed axioms
			prevLogicalAxioms.removeAll(ontology.logicalAxioms().collect(Collectors.toSet()));
			System.out.println("------------------- removed ---------------------");
			prevLogicalAxioms.stream().forEach(ax -> System.out.println(OntologyDescriptor.getCleanNameOWLObj(ax)));
			System.out.println("-------------------------------------------------");
		}
		return ontology;
	}
}