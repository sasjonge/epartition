package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class TopKeyAxiomsTest extends PartitioningTest {
	
	@Test
	void keyWithTopTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubDataPropertyOfAxiom gamma = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLHasKeyAxiom delta = factory.getOWLHasKeyAxiom(factory.getOWLThing(), r, p);
		
		ontology.add(alpha, beta, gamma, delta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
	}
}
