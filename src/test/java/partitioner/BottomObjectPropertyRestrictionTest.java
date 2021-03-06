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

class BottomObjectPropertyRestrictionTest extends PartitioningTest {
    
    @Test
    void  objectSomeValuesFromWithBottomTest() throws IOException, ExportException {
        
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
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectSomeValuesFrom(r, factory.getOWLNothing()));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(3, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
    }
    
    @Test
    void  objectAllValuesFromWithBottomTest() throws IOException, ExportException {
        
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
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectAllValuesFrom(r, factory.getOWLNothing()));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(3, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
    }
    
    @Test
    void  objectMinCardinalityWithBottomTest() throws IOException, ExportException {
        
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
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMinCardinality(1, r, factory.getOWLNothing()));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(3, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));

    }
    
    @Test
    void  objectMaxCardinalityWithBottomTest() throws IOException, ExportException {
        
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
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectMaxCardinality(1, r, factory.getOWLNothing()));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(3, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
    }
    
    @Test
    void  objectExactCardinalityWithBottomTest() throws IOException, ExportException {
        
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
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectExactCardinality(1, r, factory.getOWLNothing()));

        ontology.add(alpha, beta, gamma, delta, epsilon);
        
        List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
        
        assertEquals(3, partitions.size());
        
        areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
   
    }

}
