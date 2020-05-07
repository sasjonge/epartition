package partitioner;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.io.ExportException;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;

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
	void subObjectPropertyChainPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty rDashDash = factory.getOWLObjectProperty(base + "r''");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty sDashDash = factory.getOWLObjectProperty(base + "s''");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLObjectProperty tDashDash = factory.getOWLObjectProperty(base + "t''");
		OWLObjectProperty v = factory.getOWLObjectProperty(base + "v");
		OWLObjectProperty vDash = factory.getOWLObjectProperty(base + "v'");
		OWLObjectProperty vDashDash = factory.getOWLObjectProperty(base + "v''");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(v, vDash);

		OWLInverseObjectPropertiesAxiom epsilon = factory.getOWLInverseObjectPropertiesAxiom(rDashDash, r);
		OWLInverseObjectPropertiesAxiom zeta = factory.getOWLInverseObjectPropertiesAxiom(sDashDash, s);
		OWLInverseObjectPropertiesAxiom eta = factory.getOWLInverseObjectPropertiesAxiom(tDashDash, t);
		OWLInverseObjectPropertiesAxiom theta = factory.getOWLInverseObjectPropertiesAxiom(vDashDash, v);

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta, eta, theta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(8, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma, delta, epsilon, zeta, eta, theta));
	}

	@Test
	void subObjectPropertyChainTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty rDashDash = factory.getOWLObjectProperty(base + "r''");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty sDashDash = factory.getOWLObjectProperty(base + "s''");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLObjectProperty tDashDash = factory.getOWLObjectProperty(base + "t''");
		OWLObjectProperty v = factory.getOWLObjectProperty(base + "v");
		OWLObjectProperty vDash = factory.getOWLObjectProperty(base + "v'");
		OWLObjectProperty vDashDash = factory.getOWLObjectProperty(base + "v''");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(v, vDash);

		OWLInverseObjectPropertiesAxiom epsilon = factory.getOWLInverseObjectPropertiesAxiom(rDashDash, r);
		OWLInverseObjectPropertiesAxiom zeta = factory.getOWLInverseObjectPropertiesAxiom(sDashDash, s);
		OWLInverseObjectPropertiesAxiom eta = factory.getOWLInverseObjectPropertiesAxiom(tDashDash, t);
		OWLInverseObjectPropertiesAxiom theta = factory.getOWLInverseObjectPropertiesAxiom(vDashDash, v);

		OWLSubPropertyChainOfAxiom iota = factory.getOWLSubPropertyChainOfAxiom(Arrays.asList(s,t,v),r);

		ontology.add(alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(4, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota));

		for (OWLOntology part : partitions) {
			if(part.containsAxiom(alpha)) {
				assertTrue(part.containsAxiom(beta));
				assertTrue(part.containsAxiom(iota));
			} else if (part.containsAxiom(gamma)) {
				assertTrue(part.containsAxiom(zeta));
			} else if (part.containsAxiom(delta)) {
				assertTrue(part.containsAxiom(eta));
			} else if (part.containsAxiom(epsilon)) {
				assertTrue(part.containsAxiom(theta));
			} else {
				fail("The partition should contain at least one of the tested axioms");
			}
		}
	}
	
	@Test
	void equivalentObjectPropertiesTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(rDash, sDash);

		OWLEquivalentObjectPropertiesAxiom gamma = factory.getOWLEquivalentObjectPropertiesAxiom(r, rDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}
	
	@Test
	void disjointObjectPropertiesTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, s);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(rDash, sDash);
		OWLDisjointObjectPropertiesAxiom gamma = factory.getOWLDisjointObjectPropertiesAxiom(r, rDash);
	
		ontology.add(gamma, alpha, beta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta, gamma));
	}

	@Test
	void equivalentObjectPropertiesManyPreTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLObjectProperty v = factory.getOWLObjectProperty(base + "v");
		OWLObjectProperty vDash = factory.getOWLObjectProperty(base + "v'");
		OWLObjectProperty w = factory.getOWLObjectProperty(base + "w");
		OWLObjectProperty wDash = factory.getOWLObjectProperty(base + "w'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(v, vDash);
		OWLSubObjectPropertyOfAxiom epsilon = factory.getOWLSubObjectPropertyOfAxiom(w, wDash);

		ontology.add(alpha,beta,gamma,delta,epsilon);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(5, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon));
	}

	@Test
	void equivalentObjectPropertiesManyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLObjectProperty v = factory.getOWLObjectProperty(base + "v");
		OWLObjectProperty vDash = factory.getOWLObjectProperty(base + "v'");
		OWLObjectProperty w = factory.getOWLObjectProperty(base + "w");
		OWLObjectProperty wDash = factory.getOWLObjectProperty(base + "w'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(v, vDash);
		OWLSubObjectPropertyOfAxiom epsilon = factory.getOWLSubObjectPropertyOfAxiom(w, wDash);

		OWLEquivalentObjectPropertiesAxiom zeta = factory.getOWLEquivalentObjectPropertiesAxiom(r,s,t,v,w);

		ontology.add(alpha,beta,gamma,delta,epsilon,zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon,zeta));
	}

	@Test
	void disjointObjectPropertiesManyTest() throws IOException, ExportException {
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty rDash = factory.getOWLObjectProperty(base + "r'");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");
		OWLObjectProperty sDash = factory.getOWLObjectProperty(base + "s'");
		OWLObjectProperty t = factory.getOWLObjectProperty(base + "t");
		OWLObjectProperty tDash = factory.getOWLObjectProperty(base + "t'");
		OWLObjectProperty v = factory.getOWLObjectProperty(base + "v");
		OWLObjectProperty vDash = factory.getOWLObjectProperty(base + "v'");
		OWLObjectProperty w = factory.getOWLObjectProperty(base + "w");
		OWLObjectProperty wDash = factory.getOWLObjectProperty(base + "w'");

		OWLSubObjectPropertyOfAxiom alpha = factory.getOWLSubObjectPropertyOfAxiom(r, rDash);
		OWLSubObjectPropertyOfAxiom beta = factory.getOWLSubObjectPropertyOfAxiom(s, sDash);
		OWLSubObjectPropertyOfAxiom gamma = factory.getOWLSubObjectPropertyOfAxiom(t, tDash);
		OWLSubObjectPropertyOfAxiom delta = factory.getOWLSubObjectPropertyOfAxiom(v, vDash);
		OWLSubObjectPropertyOfAxiom epsilon = factory.getOWLSubObjectPropertyOfAxiom(w, wDash);

		OWLDisjointObjectPropertiesAxiom zeta = factory.getOWLDisjointObjectPropertiesAxiom(r,s,t,v,w);

		ontology.add(alpha,beta,gamma,delta,epsilon,zeta);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(1, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta,epsilon,zeta));
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
	void objectPropertyRangePreTest() throws IOException, ExportException {
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLInverseObjectPropertiesAxiom alpha = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, b);
		OWLObjectPropertyDomainAxiom gamma = factory.getOWLObjectPropertyDomainAxiom(s, b);

		ontology.add(alpha, beta,gamma);

		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);

		assertEquals(2, partitions.size());

		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma));
	}


	@Test
	void objectPropertyRangeTest() throws IOException, ExportException {
		OWLClass a = factory.getOWLClass(base + "A");
		OWLClass b = factory.getOWLClass(base + "B");
		OWLObjectProperty r = factory.getOWLObjectProperty(base + "r");
		OWLObjectProperty s = factory.getOWLObjectProperty(base + "s");

		OWLInverseObjectPropertiesAxiom alpha = factory.getOWLInverseObjectPropertiesAxiom(r, s);
		OWLSubClassOfAxiom beta = factory.getOWLSubClassOfAxiom(a, b);
		OWLObjectPropertyDomainAxiom gamma = factory.getOWLObjectPropertyDomainAxiom(s, b);
		OWLObjectPropertyRangeAxiom delta = factory.getOWLObjectPropertyRangeAxiom(s, a);
	
		ontology.add(alpha, beta, gamma, delta);
		
		List<OWLOntology> partitions = (new PartitioningCore()).partition(ontology);
		System.out.println(partitions.toString());
				
		assertEquals(1, partitions.size());
		
		areAllAxiomsInExactlyOnePartition(partitions, Arrays.asList(alpha,beta,gamma,delta));
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
