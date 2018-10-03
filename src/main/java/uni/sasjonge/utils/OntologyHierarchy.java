package uni.sasjonge.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class OntologyHierarchy {

	static Map<Integer, List<OWLClass>> depthToClasses = new HashMap<>();
	static Map<Integer, List<OWLObjectProperty>> depthToProperties = new HashMap<>();
	static boolean somethingChanged = false;

	static Map<OWLClass, OWLClass> parentClassOf = new HashMap<>();
	static Map<OWLObjectProperty, OWLObjectProperty> parentPropertyOf = new HashMap<>();

	public OntologyHierarchy(OWLOntology ontology) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		initDepthToClass(manager, manager.copyOntology(ontology, OntologyCopy.DEEP), 0);
		initDepthToRoles(manager2, manager2.copyOntology(ontology, OntologyCopy.DEEP), 0);
	}

	private void initDepthToClass(OWLOntologyManager manager, OWLOntology ont, int depth) {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
		OWLDataFactory df = manager.getOWLDataFactory();

		List<OWLClass> classes = reasoner.getSubClasses(df.getOWLThing(), true).entities().collect(Collectors.toList());

		// save parent structure
		for (OWLClass cls : classes) {
			reasoner.getSubClasses(cls, true).entities().forEach(subCls -> {
				parentClassOf.put(subCls, cls);
			});
		}

		if (!classes.isEmpty()) {
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ont));
			classes.stream().forEach(e -> {
				if (!e.isOWLNothing() && !reasoner.getSubClasses(e).isBottomSingleton()) {
					somethingChanged = true;
					e.accept(remover);
				}
			});
			manager.applyChanges(remover.getChanges());
			remover.reset(); // TODO: Remove if not needed

			depthToClasses.put(depth, classes);
			if (somethingChanged) {
				somethingChanged = false;
				initDepthToClass(manager, ont, depth + 1);
			}
		}
	}

	private void initDepthToRoles(OWLOntologyManager manager, OWLOntology ont, int depth) {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
		OWLDataFactory df = manager.getOWLDataFactory();

		List<OWLObjectPropertyExpression> properties = reasoner
				.getSubObjectProperties(df.getOWLTopObjectProperty(), true).entities().collect(Collectors.toList());

		// save parent role structure
		for (OWLObjectPropertyExpression property : properties) {
			if (!property.getInverseProperty().isOWLObjectProperty()) {

				reasoner.getSubObjectProperties(property, true).entities().forEach(subCls -> {
					if (!subCls.getInverseProperty().isOWLObjectProperty()) {
						parentPropertyOf.put(subCls.asOWLObjectProperty(), property.asOWLObjectProperty());
					}
				});

			}
		}

		List<OWLObjectPropertyExpression> rolesToSave = new ArrayList<>();

		if (properties.size() != 2) {
			System.out.println(properties.toString());
			rolesToSave.clear();
			OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ont));
			properties.stream().forEach(e -> {
				if (!e.isOWLBottomObjectProperty()) {
					somethingChanged = true;
					if (!e.getInverseProperty().isOWLObjectProperty()) {
						e.asOWLObjectProperty().accept(remover);
						rolesToSave.add(e);
					}
				}
			});
			manager.applyChanges(remover.getChanges());
			remover.reset(); // TODO: Remove if not needed

			depthToProperties.put(depth,
					rolesToSave.stream().map((x) -> x.asOWLObjectProperty()).collect(Collectors.toList()));
			if (somethingChanged) {
				somethingChanged = false;
				initDepthToRoles(manager, ont, depth + 1);
			}
		}
	}

	public List<OWLClass> getClassesOfDepth(int depth) {
		return depthToClasses.get(depth);
	}

	public List<OWLObjectProperty> getPropertiesOfDepth(int depth) {
		return depthToProperties.get(depth);
	}

	public int getHighestPropertyDepth() {
		return depthToProperties.size() - 1;
	}

	public int getHighestClassDepth() {
		return depthToClasses.size() - 1;
	}
	
	public OWLClass getOWLClassParent(OWLClass cls) {
		return parentClassOf.get(cls);
	}
	
	public OWLObjectProperty getOWLPropertyParent(OWLObjectProperty cls) {
		return parentPropertyOf.get(cls);
	}

}
