package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class AssertionAxiomsTest extends PartitioningTest {

	@Test
	void sameIndividualPreTest() throws IOException, ExportException {
		
		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
			
		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(a, aDash);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(b, bDash);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void sameIndividualTest() throws IOException, ExportException {
		
		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
			
		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(a, aDash);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(b, bDash);
		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(a, b);
		
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}

	@Test
	void sameIndividualManyPreTest() throws IOException, ExportException {

		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
		OWLIndividual c = factory.getOWLNamedIndividual(base + "c");
		OWLIndividual cDash = factory.getOWLNamedIndividual(base + "c'");
		OWLIndividual d = factory.getOWLNamedIndividual(base + "d");
		OWLIndividual dDash = factory.getOWLNamedIndividual(base + "d'");
		OWLIndividual e = factory.getOWLNamedIndividual(base + "e");
		OWLIndividual eDash = factory.getOWLNamedIndividual(base + "e'");

		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(a, aDash);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(b, bDash);
		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(c, cDash);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(d, dDash);
		OWLSameIndividualAxiom epsilon = factory.getOWLSameIndividualAxiom(e, eDash);

		ontology.add(alpha, beta, gamma, delta, epsilon);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(5, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma, delta, epsilon));
	}

	@Test
	void sameIndividualManyTest() throws IOException, ExportException {

		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
		OWLIndividual c = factory.getOWLNamedIndividual(base + "c");
		OWLIndividual cDash = factory.getOWLNamedIndividual(base + "c'");
		OWLIndividual d = factory.getOWLNamedIndividual(base + "d");
		OWLIndividual dDash = factory.getOWLNamedIndividual(base + "d'");
		OWLIndividual e = factory.getOWLNamedIndividual(base + "e");
		OWLIndividual eDash = factory.getOWLNamedIndividual(base + "e'");

		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(a, aDash);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(b, bDash);
		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(c, cDash);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(d, dDash);
		OWLSameIndividualAxiom epsilon = factory.getOWLSameIndividualAxiom(e, eDash);

		OWLSameIndividualAxiom zeta = factory.getOWLSameIndividualAxiom(a,b,c,d,e);

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma, delta, epsilon,zeta));
	}
	
    @Test
    void differentIndividualsPreTest() throws IOException, ExportException {
        
        OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
        OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
            
        OWLDifferentIndividualsAxiom alpha = factory.getOWLDifferentIndividualsAxiom(a, aDash);
        OWLDifferentIndividualsAxiom beta = factory.getOWLDifferentIndividualsAxiom(b, bDash);
    
        ontology.add(alpha, beta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
    }
    
    @Test
    void differentIndividualsTest() throws IOException, ExportException {

		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");

		OWLDifferentIndividualsAxiom alpha = factory.getOWLDifferentIndividualsAxiom(a, aDash);
		OWLDifferentIndividualsAxiom beta = factory.getOWLDifferentIndividualsAxiom(b, bDash);
		OWLDifferentIndividualsAxiom gamma = factory.getOWLDifferentIndividualsAxiom(a, b);

		ontology.add(alpha, beta, gamma);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma));
	}

	@Test
	void differentIndividualsManyTest() throws IOException, ExportException {

		OWLIndividual a = factory.getOWLNamedIndividual(base + "a");
		OWLIndividual aDash = factory.getOWLNamedIndividual(base + "a'");
		OWLIndividual b = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDash = factory.getOWLNamedIndividual(base + "b'");
		OWLIndividual c = factory.getOWLNamedIndividual(base + "c");
		OWLIndividual cDash = factory.getOWLNamedIndividual(base + "c'");
		OWLIndividual d = factory.getOWLNamedIndividual(base + "d");
		OWLIndividual dDash = factory.getOWLNamedIndividual(base + "d'");
		OWLIndividual e = factory.getOWLNamedIndividual(base + "e");
		OWLIndividual eDash = factory.getOWLNamedIndividual(base + "e'");

		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(a, aDash);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(b, bDash);
		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(c, cDash);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(d, dDash);
		OWLSameIndividualAxiom epsilon = factory.getOWLSameIndividualAxiom(e, eDash);

		OWLDifferentIndividualsAxiom zeta = factory.getOWLDifferentIndividualsAxiom(a,b,c,d,e);

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma, delta, epsilon,zeta));
	}
    
    @Test
    void classAssertionPreTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(aInd, bInd);
   
        ontology.add(alpha, beta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
    }
    
    @Test
    void classAssertionTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, b);
		OWLSameIndividualAxiom beta = factory.getOWLSameIndividualAxiom(aInd, bInd);
		OWLClassAssertionAxiom gamma = factory.getOWLClassAssertionAxiom(a, aInd);
   
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void objectPropertyAssertionPreTest() throws IOException, ExportException {
        
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual aDashInd = factory.getOWLNamedIndividual(base + "a'");
        OWLIndividual bDashInd = factory.getOWLNamedIndividual(base + "b'");
            
		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(t, s);

		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(aInd, aDashInd);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(bInd, bDashInd);
		
        ontology.add(alpha, beta, gamma, delta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
                
        assertEquals(4, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta));
    }
    
    @Test
    void objectPropertyAssertionTest() throws IOException, ExportException {
        
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual aDashInd = factory.getOWLNamedIndividual(base + "a'");
        OWLIndividual bDashInd = factory.getOWLNamedIndividual(base + "b'");
            
		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(t, s);

		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(aInd, aDashInd);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(bInd, bDashInd);
		
		OWLObjectPropertyAssertionAxiom epsilon = factory.getOWLObjectPropertyAssertionAxiom(r, aInd, bInd);
		
        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
                
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 1 or 3 logicalAxioms");
				break;
			}
		}
    }
    
    @Test
    void negativeObjectPropertyAssertionTest() throws IOException, ExportException {
        
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual aDashInd = factory.getOWLNamedIndividual(base + "a'");
        OWLIndividual bDashInd = factory.getOWLNamedIndividual(base + "b'");
            
		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(t, s);

		OWLSameIndividualAxiom gamma = factory.getOWLSameIndividualAxiom(aInd, aDashInd);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(bInd, bDashInd);
		
		OWLNegativeObjectPropertyAssertionAxiom epsilon = factory.getOWLNegativeObjectPropertyAssertionAxiom(r, aInd, bInd);
		
        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
                
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 1 or 3 logicalAxioms");
				break;
			}
		}
    }
    
	@Test
	void dataPropertyAssertionPreTest() throws IOException, ExportException {
		
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(aInd, bInd);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataPropertyAssertionTest() throws IOException, ExportException {
		
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(aInd, bInd);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLDataPropertyAssertionAxiom gamma = factory.getOWLDataPropertyAssertionAxiom(p, aInd, true);

		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	
	@Test
	void negativeDataPropertyAssertionTest() throws IOException, ExportException {
		
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");
			
		OWLSameIndividualAxiom alpha = factory.getOWLSameIndividualAxiom(aInd, bInd);
		OWLSubDataPropertyOfAxiom beta = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLNegativeDataPropertyAssertionAxiom gamma = factory.getOWLNegativeDataPropertyAssertionAxiom(p, aInd, factory.getOWLLiteral("test"));

		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
}
