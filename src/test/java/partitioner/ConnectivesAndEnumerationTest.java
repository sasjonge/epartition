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

    @Test
    void  objectIntersectionManyPreTest() throws IOException, ExportException {

        OWLClass a = factory.getOWLClass(base + "A");
        OWLClass b = factory.getOWLClass(base + "B");
        OWLClass c = factory.getOWLClass(base + "C");
        OWLClass d = factory.getOWLClass(base + "D");
        OWLClass e = factory.getOWLClass(base + "E");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual cInd = factory.getOWLNamedIndividual(base + "c");
        OWLIndividual dInd = factory.getOWLNamedIndividual(base + "d");
        OWLIndividual eInd = factory.getOWLNamedIndividual(base + "e");

        OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(a, aInd);
        OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(b, bInd);
        OWLClassAssertionAxiom gamma = factory.getOWLClassAssertionAxiom(c, cInd);
        OWLClassAssertionAxiom delta = factory.getOWLClassAssertionAxiom(d, dInd);
        OWLClassAssertionAxiom epsilon = factory.getOWLClassAssertionAxiom(e, eInd);

        ontology.add(alpha, beta, gamma, delta, epsilon);

        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

        assertEquals(5, partitions.size());

        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
    }

    @Test
    void  objectIntersectionManyTest() throws IOException, ExportException {
        OWLClass aDash = factory.getOWLClass(base + "A'");

        OWLClass a = factory.getOWLClass(base + "A");
        OWLClass b = factory.getOWLClass(base + "B");
        OWLClass c = factory.getOWLClass(base + "C");
        OWLClass d = factory.getOWLClass(base + "D");
        OWLClass e = factory.getOWLClass(base + "E");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual cInd = factory.getOWLNamedIndividual(base + "c");
        OWLIndividual dInd = factory.getOWLNamedIndividual(base + "d");
        OWLIndividual eInd = factory.getOWLNamedIndividual(base + "e");

        OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(a, aInd);
        OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(b, bInd);
        OWLClassAssertionAxiom gamma = factory.getOWLClassAssertionAxiom(c, cInd);
        OWLClassAssertionAxiom delta = factory.getOWLClassAssertionAxiom(d, dInd);
        OWLClassAssertionAxiom epsilon = factory.getOWLClassAssertionAxiom(e, eInd);
        OWLSubClassOfAxiom zeta = factory.getOWLSubClassOfAxiom(aDash, factory.getOWLObjectIntersectionOf(a,b,c,d,e));

        ontology.add(alpha, beta, gamma, delta, epsilon, zeta);

        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

        assertEquals(1, partitions.size());

        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon,zeta));
    }

    @Test
    void  objectUnionManyTest() throws IOException, ExportException {
        OWLClass aDash = factory.getOWLClass(base + "A'");

        OWLClass a = factory.getOWLClass(base + "A");
        OWLClass b = factory.getOWLClass(base + "B");
        OWLClass c = factory.getOWLClass(base + "C");
        OWLClass d = factory.getOWLClass(base + "D");
        OWLClass e = factory.getOWLClass(base + "E");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual cInd = factory.getOWLNamedIndividual(base + "c");
        OWLIndividual dInd = factory.getOWLNamedIndividual(base + "d");
        OWLIndividual eInd = factory.getOWLNamedIndividual(base + "e");

        OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(a, aInd);
        OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(b, bInd);
        OWLClassAssertionAxiom gamma = factory.getOWLClassAssertionAxiom(c, cInd);
        OWLClassAssertionAxiom delta = factory.getOWLClassAssertionAxiom(d, dInd);
        OWLClassAssertionAxiom epsilon = factory.getOWLClassAssertionAxiom(e, eInd);
        OWLSubClassOfAxiom zeta = factory.getOWLSubClassOfAxiom(aDash, factory.getOWLObjectUnionOf(a,b,c,d,e));

        ontology.add(alpha, beta, gamma, delta, epsilon, zeta);

        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

        assertEquals(1, partitions.size());

        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon,zeta));
    }

    @Test
    void  objectOneOfManyTest() throws IOException, ExportException {
        OWLClass aDash = factory.getOWLClass(base + "A'");

        OWLClass a = factory.getOWLClass(base + "A");
        OWLClass b = factory.getOWLClass(base + "B");
        OWLClass c = factory.getOWLClass(base + "C");
        OWLClass d = factory.getOWLClass(base + "D");
        OWLClass e = factory.getOWLClass(base + "E");

        OWLIndividual aInd = factory.getOWLNamedIndividual(base + "a");
        OWLIndividual bInd = factory.getOWLNamedIndividual(base + "b");
        OWLIndividual cInd = factory.getOWLNamedIndividual(base + "c");
        OWLIndividual dInd = factory.getOWLNamedIndividual(base + "d");
        OWLIndividual eInd = factory.getOWLNamedIndividual(base + "e");

        OWLClassAssertionAxiom alpha = factory.getOWLClassAssertionAxiom(a, aInd);
        OWLClassAssertionAxiom beta = factory.getOWLClassAssertionAxiom(b, bInd);
        OWLClassAssertionAxiom gamma = factory.getOWLClassAssertionAxiom(c, cInd);
        OWLClassAssertionAxiom delta = factory.getOWLClassAssertionAxiom(d, dInd);
        OWLClassAssertionAxiom epsilon = factory.getOWLClassAssertionAxiom(e, eInd);
        OWLSubClassOfAxiom zeta = factory.getOWLSubClassOfAxiom(aDash, factory.getOWLObjectOneOf(aInd,bInd,cInd,dInd,eInd));

        ontology.add(alpha, beta, gamma, delta, epsilon, zeta);

        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

        assertEquals(1, partitions.size());

        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon,zeta));
    }
}
