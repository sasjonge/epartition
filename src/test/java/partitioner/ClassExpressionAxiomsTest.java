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

class ClassExpressionAxiomsTest extends PartitioningTest {

	@Test
	void subClassOfPreTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
	
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(2, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}

	@Test
	void subClassOfTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}
	
	@Test
	void equivalentClassesTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLEquivalentClassesAxiom gamma = factory.getOWLEquivalentClassesAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	} 
	
	@Test
	void disjointClassesTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLDisjointClassesAxiom gamma = factory.getOWLDisjointClassesAxiom(a, b);
	
		ontology.add(alpha, beta,gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1,partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	} 
	
	@Test
	void disjointUnionPreTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
		OWLClass bDashDashDash = factory.getOWLClass(base + "B'''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, bDashDashDash);
	
		ontology.add(alpha, beta, gamma);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(3, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));	
	}
	
	@Test
	void disJointUnionTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass bDashDash = factory.getOWLClass(base + "B''");
		OWLClass bDashDashDash = factory.getOWLClass(base + "B'''");
			
		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, bDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDashDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, bDashDashDash);
		OWLDisjointUnionAxiom delta = factory.getOWLDisjointUnionAxiom(a, Arrays.asList(b, c));
	
		ontology.add(alpha, beta, gamma, delta); 
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
	}

	@Test
	void equivalentClassesManyPreTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass d = factory.getOWLClass(base + "D");
		OWLClass e = factory.getOWLClass(base + "E");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass cDash = factory.getOWLClass(base + "C'");
		OWLClass dDash = factory.getOWLClass(base + "D'");
		OWLClass eDash = factory.getOWLClass(base + "E'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, cDash);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(d, dDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(e, eDash);

		ontology.add(alpha, beta,gamma, delta, epsilon);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(5,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon));
	}

	@Test
	void equivalentClassesManyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass d = factory.getOWLClass(base + "D");
		OWLClass e = factory.getOWLClass(base + "E");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass cDash = factory.getOWLClass(base + "C'");
		OWLClass dDash = factory.getOWLClass(base + "D'");
		OWLClass eDash = factory.getOWLClass(base + "E'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, cDash);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(d, dDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(e, eDash);

		OWLEquivalentClassesAxiom zeta = factory.getOWLEquivalentClassesAxiom(a, b, c, d, e);

		ontology.add(alpha, beta,gamma, delta, epsilon, zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon, zeta));
	}

	@Test
	void disjointClassesManyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass d = factory.getOWLClass(base + "D");
		OWLClass e = factory.getOWLClass(base + "E");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass cDash = factory.getOWLClass(base + "C'");
		OWLClass dDash = factory.getOWLClass(base + "D'");
		OWLClass eDash = factory.getOWLClass(base + "E'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, cDash);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(d, dDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(e, eDash);

		OWLDisjointClassesAxiom zeta = factory.getOWLDisjointClassesAxiom(a, b, c, d, e);

		ontology.add(alpha, beta,gamma, delta, epsilon, zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon, zeta));
	}

	@Test
	void disjointUnionManyTest() throws IOException, ExportException {

		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLClass c = factory.getOWLClass(base + "C");
		OWLClass d = factory.getOWLClass(base + "D");
		OWLClass e = factory.getOWLClass(base + "E");
		OWLClass aDash = factory.getOWLClass(base + "A'");
		OWLClass bDash = factory.getOWLClass(base + "B'");
		OWLClass cDash = factory.getOWLClass(base + "C'");
		OWLClass dDash = factory.getOWLClass(base + "D'");
		OWLClass eDash = factory.getOWLClass(base + "E'");

		OWLSubClassOfAxiom alpha = factory.getOWLSubClassOfAxiom(a, aDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(b, bDash);
		OWLSubClassOfAxiom gamma = factory.getOWLSubClassOfAxiom(c, cDash);
		OWLSubClassOfAxiom delta = factory.getOWLSubClassOfAxiom(d, dDash);
		OWLSubClassOfAxiom epsilon = factory.getOWLSubClassOfAxiom(e, eDash);

		OWLDisjointUnionAxiom zeta = factory.getOWLDisjointUnionAxiom(a, Arrays.asList(b, c, d, e));

		ontology.add(alpha, beta,gamma, delta, epsilon, zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1,partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma, delta, epsilon, zeta));
	}
}
