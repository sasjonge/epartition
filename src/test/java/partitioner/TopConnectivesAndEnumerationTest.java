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

class TopConnectivesAndEnumerationTest extends PartitioningTest {
    
    @Test
    void  objectIntersectionWithTopTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectIntersectionOf(factory.getOWLThing(), bDashDash));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void  objectUnionWithTopTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectUnionOf(factory.getOWLThing(), bDashDash));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
    
    @Test
    void  objectComplementOfWithTopTest() throws IOException, ExportException {
        
    	OWLClass a = factory.getOWLClass(base + "A");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
            
		OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(bDash, aInd);
		OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(bDashDash, bInd);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(bDash, factory.getOWLObjectComplementOf(factory.getOWLThing()));
		
        ontology.add(alpha, beta, gamma);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(2, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
    }
}
