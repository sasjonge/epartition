package partitioner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class DataPropertyAxiomsTest extends PartitioningTest {

	@Test
	void subDataPropertyOfPreTest() throws IOException, ExportException {
		
		OWLDataProperty a = factory.getOWLDataProperty(base + "A");
		OWLDataProperty b = factory.getOWLDataProperty(base + "B");
		OWLDataProperty bDash = factory.getOWLDataProperty(base + "B'");
		OWLDataProperty bDashDash = factory.getOWLDataProperty(base + "B''");
			
		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(a, bDash);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(b, bDashDash);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}

	@Test
	void subDataPropertyOfTest() throws IOException, ExportException {
		
		OWLDataProperty a = factory.getOWLDataProperty(base + "A");
		OWLDataProperty b = factory.getOWLDataProperty(base + "B");
		OWLDataProperty bDash = factory.getOWLDataProperty(base + "B'");
		OWLDataProperty bDashDash = factory.getOWLDataProperty(base + "B''");
			
		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(a, bDash);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(b, bDashDash);
		OWLSubDataPropertyOfAxiom gamma = factory.getOWLSubDataPropertyOfAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	@Test
	void equivalentDataPropertiesTest() throws IOException, ExportException {
		
		OWLDataProperty a = factory.getOWLDataProperty(base + "A");
		OWLDataProperty b = factory.getOWLDataProperty(base + "B");
		OWLDataProperty bDash = factory.getOWLDataProperty(base + "B'");
		OWLDataProperty bDashDash = factory.getOWLDataProperty(base + "B''");
			
		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(a, bDash);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(b, bDashDash);
		OWLEquivalentDataPropertiesAxiom gamma = factory.getOWLEquivalentDataPropertiesAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	} 
	
	@Test
	void disjointDataPropertiesTest() throws IOException, ExportException {
			
		OWLDataProperty a = factory.getOWLDataProperty(base + "A");
		OWLDataProperty b = factory.getOWLDataProperty(base + "B");
		OWLDataProperty bDash = factory.getOWLDataProperty(base + "B'");
		OWLDataProperty bDashDash = factory.getOWLDataProperty(base + "B''");
			
		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(a, bDash);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(b, bDashDash);
		OWLDisjointDataPropertiesAxiom gamma = factory.getOWLDisjointDataPropertiesAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	} 
	
	@Test
	void dataPropertyDomainPreTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}

	@Test
	void dataPropertyDomainTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLDataPropertyDomainAxiom gamma = factory.getOWLDataPropertyDomainAxiom(p,a);
		
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}

	@Test
	void rangeFunctionalDataTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLDataPropertyRangeAxiom gamma = factory.getOWLDataPropertyRangeAxiom(p, factory.getIntegerOWLDatatype());
		OWLFunctionalDataPropertyAxiom delta = factory.getOWLFunctionalDataPropertyAxiom(pDash);
		
		ontology.add(alpha, beta,gamma,delta);
		
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
				fail("The partitons need to have 1 or 3 logicalAxioms");
				break;
			}
		}
	}
}
