package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

import uni.sasjonge.partitioning.PartitioningCore;

class DataPropertyRestrictionTest extends PartitioningTest {

	@Test
	void dataSomeValuesFromPreTest() throws IOException, ExportException {
		
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
	
		ontology.add(alpha);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha));
	}
	
	@Test
	void dataSomeValuesFromTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataSomeValuesFrom(p, factory.getIntegerOWLDatatype()));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataAllValuesFromTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataAllValuesFrom(p, factory.getIntegerOWLDatatype()));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataMinCardinalityTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataMinCardinality(2, p, factory.getIntegerOWLDatatype()));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataMaxCardinalityTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataMaxCardinality(5, p, factory.getIntegerOWLDatatype()));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataExactCardinalityTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataExactCardinality(5, p, factory.getIntegerOWLDatatype()));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataMinCardinalityTestNoDataRange() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataMinCardinality(2, p));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataMaxCardinalityTestNoDataRange() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataMaxCardinality(2, p));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataExactCardinalityTestNoDataRange() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataExactCardinality(2, p));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
	
	@Test
	void dataHasValueTest() throws IOException, ExportException {
		
		OWLClass a = factory.getOWLClass("A");
		OWLDataProperty p = factory.getOWLDataProperty(base + "P");
		OWLDataProperty pDash = factory.getOWLDataProperty(base + "P'");

		OWLSubDataPropertyOfAxiom alpha = factory.getOWLSubDataPropertyOfAxiom(p, pDash);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, factory.getOWLDataHasValue(p, factory.getOWLLiteral("test")));
		
		ontology.add(alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta));
	}
}
