package uni.sasjonge.partitioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.xml.sax.SAXException;

/**
 * 
 * Core partitioning algorithm based on the following paper:
 * 
 * 
 * @author Sascha Jongebloed
 *
 */
public class PartitioningCore {
	
	public Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
	public Map<DefaultEdge,String> edgeToAxioms = new HashMap<>();

	/**
	 * E-part Algorithm: Partitions the given ontology in several smaller ontologies and returns them
	 * 
	 * @param ontology The input ontology
	 * @return Partitioning of the input ontology
	 * @throws TransformerConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ExportException
	 */
	public List<OWLOntology> partition(OWLOntology ontology) throws TransformerConfigurationException, SAXException, IOException, ExportException {
		// The partitions
		ArrayList<OWLOntology> toReturn = new ArrayList<>();
		
		// Add the Vertexes to our defined algorithm
		// Vertex: ObjectProperties
		ontology.objectPropertiesInSignature().forEach(op -> 
			{if(!op.isOWLTopObjectProperty() && !op.isTopEntity()) {g.addVertex(getCleanName(op) + "0");g.addVertex(getCleanName(op) + "1");}});
		// Vertex: Logical Axioms
		ontology.logicalAxioms().forEach(a -> {a.nestedClassExpressions().forEach(nested -> 
			{if(!nested.isOWLThing()) 
				{
					g.addVertex(getCleanName(nested));
				}});});
		//Vertex: Individuals
		ontology.individualsInSignature().forEach(indiv -> {g.addVertex(getCleanName(indiv));});
		
		// Add the edges according to our defined algorithm
		// Edge: All sub concepts
		ontology.logicalAxioms().forEach(a -> {a.nestedClassExpressions().forEach(nested -> 
			{
				if(!nested.isOWLThing()) {
					this.addSubConceptEdges(nested);
				}
			});});
		// Edge: All axioms
		ontology.logicalAxioms().forEach(this::addAxiomEdges);
		//Needed? Delete if not: ontology.individualsInSignature().forEach(indiv -> {ontology.classAssertionAxioms(indiv).forEach(this::addAxiomEdges);});
		
		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);
		ci.connectedSets().stream().forEach(System.out::println);
		System.out.println("CCs: " + ci.connectedSets().size());
		
		// Create the new ontologies
		ci.connectedSets().stream().forEach(cc -> {
			//somehow create the new ontologies
		});
		
		// Return the created new ontologies
		return toReturn;
	}
	
	/**
	 * Adds edges for all subconcepts
	 * 
	 * @param expr The subconcept
	 */
	public void addSubConceptEdges(OWLClassExpression expr) {
		// Get a clean name for the OWLClassExpression without URL and < or >
		String exprName = getCleanName(expr);
		
		// ObjectComplementOf
		if(expr instanceof OWLObjectComplementOf) {
			g.addEdge(exprName, getCleanName(((OWLObjectComplementOf) expr).getOperand()));
		}
		
		// ObjectIntersectionOf
		else if(expr instanceof OWLObjectIntersectionOf) {
			((OWLObjectIntersectionOf) expr).getOperandsAsList().stream().forEach(sub -> {g.addEdge(exprName, getCleanName(sub));});
		} 
		
		// QuantifiedRestrictio
		else if (expr instanceof OWLQuantifiedRestriction) {
			OWLQuantifiedRestriction restriction = (OWLQuantifiedRestriction) expr;
			OWLPropertyExpression role = restriction.getProperty();
			g.addEdge(exprName, getCleanName(role) + "0");
			if(!(((OWLQuantifiedRestriction) expr).getFiller().toString().equals("owl:Thing"))){
				g.addEdge(getCleanName(role) + "1", getCleanName(((OWLQuantifiedRestriction) expr).getFiller()));
			}
			
		// ObjectOneOf
		} else if (expr instanceof OWLObjectOneOf) {
			((OWLObjectOneOf) expr).operands().forEach(indiv ->{
				g.addEdge(exprName, getCleanName(indiv));
			});
			
		// ObjectHasValue
		} else if(expr instanceof OWLObjectHasValue) {
			OWLObjectHasValue hasVal = (OWLObjectHasValue) expr;
			g.addEdge(getCleanName(hasVal.getProperty()) + "1", getCleanName(hasVal.getFiller()));
			g.addEdge(exprName, getCleanName(hasVal.getProperty()) + "0");
		
		// Default Case
		} else {
			if(!(expr instanceof OWLClass)) {
				System.err.println("Missing OWLClassExpression: " + expr.getClassExpressionType() + ". Form: " + expr);
			}
		}
	}
	
	/**
	 * Adds edges for all axioms
	 * 
	 * @param ax The axiom 
	 */
	public void addAxiomEdges(OWLAxiom ax) {
		
		DefaultEdge edge = null;
		
		// ClassAssertion
		if(ax instanceof OWLClassAssertionAxiom) {
			edge = g.addEdge(getCleanName(((OWLClassAssertionAxiom) ax).getIndividual()), getCleanName(((OWLClassAssertionAxiom) ax).getClassExpression()));
		} else
			
		// Subclass
		if (ax instanceof OWLSubClassOfAxiom) {
			edge = g.addEdge(getCleanName(((OWLSubClassOfAxiom) ax).getSubClass()), getCleanName(((OWLSubClassOfAxiom) ax).getSuperClass()));
		} 
		
		// Equivalence
		else if (ax instanceof OWLEquivalentClassesAxiom) {
			edge = g.addEdge(getCleanName(((OWLEquivalentClassesAxiom) ax).getOperandsAsList().get(0)), getCleanName(((OWLEquivalentClassesAxiom) ax).getOperandsAsList().get(1)));
		
			
		// FunctionalObjectProperty	
		//} else if (ax instanceof OWLFunctionalObjectPropertyAxiom) {
			//edge = g.addEdge(sourceVertex, targetVertex)
		
		// Disjointness
		} else if (ax instanceof OWLDisjointClassesAxiom) {
			edge = g.addEdge(getCleanName(((OWLDisjointClassesAxiom) ax).getOperandsAsList().get(0)), getCleanName(((OWLDisjointClassesAxiom) ax).getOperandsAsList().get(1)));

		// ObjectPropertyRange
		} else if (ax instanceof OWLObjectPropertyRangeAxiom) {
			edge = g.addEdge(getCleanName(((OWLObjectPropertyRangeAxiom) ax).getRange()),getCleanName(((OWLObjectPropertyRangeAxiom) ax).getProperty())+"1");
		
		// ObjectPropertyDomain
		} else if (ax instanceof OWLObjectPropertyDomainAxiom) {
			edge = g.addEdge(getCleanName(((OWLObjectPropertyDomainAxiom) ax).getDomain()),getCleanName(((OWLObjectPropertyDomainAxiom) ax).getProperty())+"0");
		
		// Default case
		} else {
			if(!(ax instanceof OWLDeclarationAxiom | ax instanceof OWLAnnotationAssertionAxiom)) {
				System.err.println("Missing axiom: " + ax.getAxiomType() + " Form: " + ax);
			}
		}
		
		if(edge != null) {
			edgeToAxioms.put(edge, getCleanName(ax));
		}
		
	}
	
	public String getCleanName(OWLObject object) {
		return object.toString().replaceAll("http[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*#|<|>", "");
	}
	

}
