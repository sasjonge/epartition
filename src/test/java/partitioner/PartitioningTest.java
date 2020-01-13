package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uni.sasjonge.Settings;
import uni.sasjonge.utils.OntologyDescriptor;

abstract class PartitioningTest {

	static OWLOntologyManager manager;
	
	static OWLDataFactory factory;	
	
	OWLOntology ontology;
	
	String base = "http://org.epartition.exampletest#";

	@BeforeAll
	static void initAll() {
		// Create a owl manager
		manager = OWLManager.createOWLOntologyManager();
		Settings.EXPORT_ONTOLOGIES = true;
	}
	
	@BeforeEach
	void init() throws OWLOntologyCreationException {
		deactivateHeuristics();
		factory = manager.getOWLDataFactory();
		ontology = manager.createOntology();

		OntologyDescriptor.init();
	}
	
	public void deactivateHeuristics() {
		Settings.USE_BH = false;
		Settings.USE_CD = false;
		Settings.USE_OLH = false;
		Settings.USE_ULH = false;
	}

	public void areAllAxiomsInExactlyOnePartition (List<OWLOntology> partitions,List<OWLLogicalAxiom> axioms) {
		Map<OWLLogicalAxiom,Boolean> isInOnePartition = new HashMap<>();
		
		for(OWLLogicalAxiom ax : axioms) {
			isInOnePartition.put(ax, false);
		}
		for (OWLOntology part : partitions) {
			part.logicalAxioms().forEach(ax -> {
				if (!isInOnePartition.containsKey(ax)) {
					fail(ax.toString() + " is in no partition");
				} else if (!isInOnePartition.get(ax).booleanValue()) {
					isInOnePartition.put(ax, true);
				} else if (isInOnePartition.get(ax).booleanValue()) {
					fail(ax.toString() + " is already in a partition");
				} 
			});
		}
		 
		// Test if all values are true
		Set<Boolean> values = new HashSet<Boolean>(isInOnePartition.values());
		
		assertTrue(values.size() == 1 && values.iterator().next().booleanValue());

	}

}
