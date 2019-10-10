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

class KeyAxiomsTest extends PartitioningTest {

	@Test
	void keyPreTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubDataPropertyOfAxiom gamma = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
	
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(3, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	@Test
	void keyTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubDataPropertyOfAxiom gamma = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLHasKeyAxiom delta = factory.getOWLHasKeyAxiom(a, r, p);
		
		ontology.add(alpha, beta, gamma, delta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
	}

	@Test
	void keyManyPreTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
		OWLDataProperty pTwo = factory.getOWLDataProperty(base + "P2");
		OWLDataProperty pTwoDash = factory.getOWLDataProperty(base + "P2'");
		OWLDataProperty pThree = factory.getOWLDataProperty(base + "P3");
		OWLDataProperty pThreeDash = factory.getOWLDataProperty(base + "P3'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubDataPropertyOfAxiom epsilon = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubDataPropertyOfAxiom zeta = factory.getOWLSubDataPropertyOfAxiom(pTwo, pTwoDash);
		OWLSubDataPropertyOfAxiom eta = factory.getOWLSubDataPropertyOfAxiom(pThree, pThreeDash);

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta, eta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(7, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta, epsilon, zeta, eta));
	}

	@Test
	void keyManyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
		OWLDataProperty pTwo = factory.getOWLDataProperty(base + "P2");
		OWLDataProperty pTwoDash = factory.getOWLDataProperty(base + "P2'");
		OWLDataProperty pThree = factory.getOWLDataProperty(base + "P3");
		OWLDataProperty pThreeDash = factory.getOWLDataProperty(base + "P3'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubDataPropertyOfAxiom epsilon = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubDataPropertyOfAxiom zeta = factory.getOWLSubDataPropertyOfAxiom(pTwo, pTwoDash);
		OWLSubDataPropertyOfAxiom eta = factory.getOWLSubDataPropertyOfAxiom(pThree, pThreeDash);

		OWLHasKeyAxiom theta = factory.getOWLHasKeyAxiom(a, Arrays.asList(r,s,t,p, pTwo, pThree));

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta, eta, theta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta, epsilon, zeta, eta, theta));
	}
}
