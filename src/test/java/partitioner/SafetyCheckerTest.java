package partitioner;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;
import uni.sasjonge.partitioning.PartitioningCore;
import uni.sasjonge.partitioning.SafetyChecker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafetyCheckerTest extends PartitioningTest {

	@Test
	void easySafetyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");

		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLObjectSomeValuesFrom existRB = factory.getOWLObjectSomeValuesFrom(r, b);
		OWLObjectAllValuesFrom allSB = factory.getOWLObjectAllValuesFrom(s, b);

		OWLObjectIntersectionOf bDashAndbDashDash = factory.getOWLObjectIntersectionOf(bDash, bDashDash);

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, existRB);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(c, allSB);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDashAndbDashDash);

		OWLClass e = factory.getOWLClass(base + "A");
		OWLClass f = factory.getOWLClass(base + "B");
		OWLSubClassOfAxiom eta = factory.getOWLSubClassOfAxiom(factory.getOWLObjectComplementOf(e),f);
	
		ontology.add(alpha, beta, gamma, delta, eta);
		
		assertFalse(SafetyChecker.isSafe(ontology));
	}

	@Test
	void nestedSafetyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(factory.getOWLObjectComplementOf(a),b);

		ontology.add(alpha);

		assertFalse(SafetyChecker.isSafe(ontology));
	}

}
