package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class ObjectPropertyAxiomsTest extends PartitioningTest {

	@Test
	void subObjectPropertyOfPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(rDash, sDash);
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void subObjectPropertyOfTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(rDash, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}
	
	@Test
	void equivalentObjectPropertiesPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLEquivalentObjectPropertiesAxiom alpha = factory.getOWLEquivalentObjectPropertiesAxiom(r, s);
		OWLEquivalentObjectPropertiesAxiom beta = factory.getOWLEquivalentObjectPropertiesAxiom(rDash, sDash);
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void equivalentObjectPropertiesTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLEquivalentObjectPropertiesAxiom alpha = factory.getOWLEquivalentObjectPropertiesAxiom(r, s);
		OWLEquivalentObjectPropertiesAxiom beta = factory.getOWLEquivalentObjectPropertiesAxiom(rDash, sDash);
		OWLEquivalentObjectPropertiesAxiom gamma = factory.getOWLEquivalentObjectPropertiesAxiom(r, rDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}
	
	@Test
	void disjointObjectPropertiesPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLDisjointObjectPropertiesAxiom alpha = factory.getOWLDisjointObjectPropertiesAxiom(r, s);
		OWLDisjointObjectPropertiesAxiom beta = factory.getOWLDisjointObjectPropertiesAxiom(rDash, sDash);
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void disjointObjectPropertiesTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLDisjointObjectPropertiesAxiom alpha = factory.getOWLDisjointObjectPropertiesAxiom(r, s);
		OWLDisjointObjectPropertiesAxiom beta = factory.getOWLDisjointObjectPropertiesAxiom(rDash, sDash);
		OWLDisjointObjectPropertiesAxiom gamma = factory.getOWLDisjointObjectPropertiesAxiom(r, rDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}
	
	@Test
	void inverseObjectPropertiesPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLInverseObjectPropertiesAxiom alpha = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(rDash, sDash);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void inverseObjectPropertiesTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLInverseObjectPropertiesAxiom alpha = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(rDash, sDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(r, sDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}

	@Test
	void objectPropertyDomainPreTest() throws IOException, ExportException {
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, b);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void objectPropertyDomainTest() throws IOException, ExportException {
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, b);
		OWLObjectPropertyDomainAxiom gamma = factory.getOWLObjectPropertyDomainAxiom(r, a);
	
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	@Test
	void objectPropertyRangeTest() throws IOException, ExportException {
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, b);
		OWLObjectPropertyRangeAxiom gamma = factory.getOWLObjectPropertyRangeAxiom(s, a);
	
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	@Test
	void functionalObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(rDash, sDash);
		OWLFunctionalObjectPropertyAxiom gamma = factory.getOWLFunctionalObjectPropertyAxiom(r);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(2, partitions.size());
		
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 2:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(gamma));
				break;
			case 1:
				assertTrue(part.containsAxiom(beta));
				break;
			default:
				fail("The partitons need to have 1 or 3 logicalAxioms");
				break;
			}
		}
	}

	@Test
	void reflexiveObjectPropertyPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void reflexiveObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLReflexiveObjectPropertyAxiom gamma = factory.getOWLReflexiveObjectPropertyAxiom(s);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}
	
	@Test
	void irreflexiveObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLIrreflexiveObjectPropertyAxiom gamma = factory.getOWLIrreflexiveObjectPropertyAxiom(s);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}
	
	@Test
	void symmetricObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLSymmetricObjectPropertyAxiom gamma = factory.getOWLSymmetricObjectPropertyAxiom(s);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}
	
	@Test
	void asymmetricObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLAsymmetricObjectPropertyAxiom gamma = factory.getOWLAsymmetricObjectPropertyAxiom(s);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}
	
	@Test
	void transitiveObjectPropertyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(s, t);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLTransitiveObjectPropertyAxiom gamma = factory.getOWLTransitiveObjectPropertyAxiom(s);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}
	
}
