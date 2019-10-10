package partitioner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import uni.sasjonge.Settings;
import uni.sasjonge.partitioning.PartitioningCore;

public class RunThroughExampleTest extends PartitioningTest{

	@Test
	public void runThroughExampleTest() throws IOException, ExportException {
		
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
		
		ontology.add(alpha, beta, gamma, delta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 1 or 3 logicalAxioms");
				break;
			}
		}
	}
}
