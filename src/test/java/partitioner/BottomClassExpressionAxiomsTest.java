package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDisjointUnionAxiomImpl;
import uni.sasjonge.partitioning.PartitioningCore;

class BottomClassExpressionAxiomsTest extends PartitioningTest {

	@Test
	void subClassOfWithBottomSubTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(factory.getOWLNothing(), b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	}
	
	@Test
	void subClassOfWithBottomSuperTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(b, factory.getOWLNothing());
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	}
	
	@Test
	void equivalentClassesWithBottomTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLEquivalentClassesAxiom gamma = factory.getOWLEquivalentClassesAxiom(factory.getOWLNothing(), b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	} 
	
	@Test
	void equivalentClassesWithBottomTest2() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLEquivalentClassesAxiom gamma = factory.getOWLEquivalentClassesAxiom(factory.getOWLNothing(), b, bDashDash);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	}
	
	@Test
	void disjointClassesWithBottomTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLDisjointClassesAxiom gamma = factory.getOWLDisjointClassesAxiom(factory.getOWLNothing(), b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	} 
	
	@Test
	void disJointUnionWithBottomAsClassTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
		OWLClass bDashDashDash = factory.getOWLClass(base + "B'''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, bDashDashDash);
		OWLDisjointUnionAxiom delta = factory.getOWLDisjointUnionAxiom(factory.getOWLNothing(), Arrays.asList(b, c));
	
		ontology.add(alpha, beta, gamma, delta); 
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			case 1:
				assertTrue(part.containsAxiom(alpha));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	}
	
	@Test
	void disJointUnionWithBottomInUnionTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
		OWLClass bDashDashDash = factory.getOWLClass(base + "B'''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, bDashDashDash);
		OWLDisjointUnionAxiom delta = factory.getOWLDisjointUnionAxiom(a, Arrays.asList(factory.getOWLNothing(), c));
	
		ontology.add(alpha, beta, gamma, delta); 
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			case 1:
				assertTrue(part.containsAxiom(beta));
				break;
			default:
				fail("The partitons need to have 1 or 2 logicalAxioms");
				break;
			}
		}
	}
}
