package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class ObjectPropertyRestrictionTest extends PartitioningTest {

    @Test
    void  objectSomeValuesFromPreTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);

        ontology.add(alpha, beta, gamma, delta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(4, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
    }
    
    @Test
    void  objectSomeValuesFromTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, b));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectAllValuesFromTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectAllValuesFrom(r, b));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectMinCardinalityTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMinCardinality(1, r, b));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectMaxCardinalityTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMaxCardinality(1, r, b));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectExactCardinalityTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectExactCardinality(1, r, b));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectMinCardinalityNoClassExpressionPreTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);

        ontology.add(alpha, beta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
    }
    
    @Test
    void  objectMinCardinalityNoClassExpressionTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMinCardinality(2, r));

        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));

    }
    
    @Test
    void  objectMaxCardinalityNoClassExpressionTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMaxCardinality(4, r));

        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));

    }
    
    @Test
    void  objectExactCardinalityNoClassExpressionTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectExactCardinality(1, r));

        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));

    }
    
    @Test
    void  objectHasValuePreTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDashInd = factory.getOWLNamedIndividual(base + "b'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(bInd, bDashInd);

        ontology.add(alpha, beta, gamma, delta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(4, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
    }
    
    @Test
    void  objectHasValueTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass aDashDash = factory.getOWLClass(base + "A''");
		OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
		OWLIndividual bDashInd = factory.getOWLNamedIndividual(base + "b'");
        OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLObjectPropertyDomainAxiom beta = factory.getOWLObjectPropertyDomainAxiom(r, aDashDash);
		OWLInverseObjectPropertiesAxiom gamma = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		OWLSameIndividualAxiom delta = factory.getOWLSameIndividualAxiom(bInd, bDashInd);

		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasValue(r, bInd));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
        
		for (OWLOntology part : partitions) {
			System.out.println(part.toString());

			switch (part.getLogicalAxiomCount()) {
			case 3:
				assertTrue(part.containsAxiom(alpha));
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(epsilon));
				break;
			case 2:
				assertTrue(part.containsAxiom(gamma));
				assertTrue(part.containsAxiom(delta));
				break;
			default:
				fail("The partitons need to have 2 or 3 logicalAxioms. This partition has " + part.getLogicalAxiomCount() + " logical axioms");
				break;
			}
		}
    }
    
    @Test
    void  objectHasSelfPreTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLObjectPropertyDomainAxiom alpha = factory.getOWLObjectPropertyDomainAxiom(r, a);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);

        ontology.add(alpha, beta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
    }
    
    @Test
    void  objectHasSelfTest() throws IOException, ExportException {
        
		OWLClass a = factory.getOWLClass(base + "A");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
        OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
            
		OWLObjectPropertyDomainAxiom alpha = factory.getOWLObjectPropertyDomainAxiom(r, a);
		OWLInverseObjectPropertiesAxiom beta = factory.getOWLInverseObjectPropertiesAxiom(rDash, r);
		
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectHasSelf(r));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
}
