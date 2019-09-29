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
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class ConnectivesAndEnumerationTest extends PartitioningTest {

    @Test
    void  objectIntersectionPreTest() throws IOException, ExportException {
        
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);

        ontology.add(alpha, beta);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
    }
    
    @Test
    void  objectIntersectionTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectIntersectionOf(bDash, bDashDash));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void  objectUnionTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectUnionOf(bDash, bDashDash));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void  objectComplementOfTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(bDash, factory.getOWLObjectComplementOf(bDashDash));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void  objectOneOfTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectOneOf(aInd, bInd));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(1, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
}
