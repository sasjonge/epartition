package uni.sasjonge.Reducer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class OntologyLevelReducer {

	public static OWLOntology removeHighestLevelConc(OWLOntologyManager manager, OWLOntology ontology, int i) {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		OWLDataFactory df = manager.getOWLDataFactory();

		if (i > 0) {

			// Returns all objects on the level < i
			List<OWLClass> toRemove = reasoner.getSubClasses(df.getOWLThing(), true).entities()
					.collect(Collectors.toList());
			;

			// Remove them from the ontology
			// ontology.remove(toRemove);
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
			toRemove.stream().forEach(e -> {
				if (!e.isOWLNothing() && !reasoner.getSubClasses(e).isBottomSingleton()) {
					e.accept(remover);
					System.out.println(e.toString());
				}
			});
			manager.applyChanges(remover.getChanges());
			remover.reset(); // TODO: Remove if not needed
			return removeHighestLevelConc(manager, ontology, i - 1);
		}
		return ontology;
	}

}