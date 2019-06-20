package uni.sasjonge.partitioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasAxiomsByType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntityByTypeProvider;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLNaryClassAxiom;
import org.semanticweb.owlapi.model.OWLNaryIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.axiomproviders.PropertyChainAxiomProvider;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNaryIndividualAxiomImpl;
import uni.sasjonge.Settings;
import uni.sasjonge.utils.OntologyDescriptor;

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
	public Map<String, Set<OWLAxiom>> vertexToAxiom = new HashMap<>();

	/**
	 * E-part Algorithm: Partitions the given ontology in several smaller ontologies
	 * and returns them
	 * 
	 * @param ontology The input ontology
	 * @return Partitioning of the input ontology
	 * @throws TransformerConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ExportException
	 */
	public List<OWLOntology> partition(OWLOntology ontology)
			throws TransformerConfigurationException, SAXException, IOException, ExportException {
		// The partitions
		ArrayList<OWLOntology> toReturn = new ArrayList<>();

		long addVertexStartTime = System.nanoTime();
		
		// Add the Vertexes to our defined algorithm
		// Vertex: ObjectProperties
		ontology.objectPropertiesInSignature().forEach(objProp -> {
			if (!objProp.isOWLTopObjectProperty() && !objProp.isTopEntity()) {
				g.addVertex(OntologyDescriptor.getCleanNameOWLObj(objProp) + Settings.PROPERTY_0_DESIGNATOR);
				g.addVertex(OntologyDescriptor.getCleanNameOWLObj(objProp) + Settings.PROPERTY_1_DESIGNATOR);
			}
		});

		// Vertex: DataProperties
		ontology.dataPropertiesInSignature().forEach(dataProp -> {
			if (!dataProp.isOWLTopDataProperty() && !dataProp.isTopEntity()) {
				g.addVertex(OntologyDescriptor.getCleanNameOWLObj(dataProp)+ Settings.PROPERTY_0_DESIGNATOR);
			}
		});

		// Vertex: SubConcepts
		ontology.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(nested -> {
				if (!nested.isOWLThing()) {
					g.addVertex(OntologyDescriptor.getCleanNameOWLObj(nested));
				}
			});
		});
		// Vertex: Individuals
		ontology.individualsInSignature().forEach(indiv -> {
			g.addVertex(OntologyDescriptor.getCleanNameOWLObj(indiv));
		});

		long addVertexEndTime = System.nanoTime();
		System.out.println("Adding vertexes took " + (addVertexEndTime - addVertexStartTime) / 1000000 + "ms");

		long addSubEdgeStartTime = System.nanoTime();
		
		// Add the edges according to our defined algorithm
		// Edge: All sub concepts (corresponding to line 3 of the algorithm in the paper)
		// in the paper
		ontology.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(nested -> {
				if (!nested.isOWLThing()) {
					this.addSubConceptEdges(nested);
				}
			});
		});
		long addSubEdgeEndTime = System.nanoTime();
		System.out
				.println("Adding subconcept edges took " + (addSubEdgeEndTime - addSubEdgeStartTime) / 1000000 + "ms");

		// Edge: All axioms
		// Edge: All sub concepts (corresponding to line 4 of the algorithm in the paper)
		long addAxiomEdgeStartTime = System.nanoTime();
		ontology.logicalAxioms().forEach(this::addAxiomEdges);
		long addAxiomEdgeEndTime = System.nanoTime();

		System.out.println("Adding axiom edges took " + (addAxiomEdgeEndTime - addAxiomEdgeStartTime) / 1000000 + "ms");

		long ccStartTime = System.nanoTime();
		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);
		long ccEndTime = System.nanoTime();
		// ci.connectedSets().stream().forEach(System.out::println);
		System.out.println("Finding the cc's took " + (ccEndTime - ccStartTime) / 1000000 + "ms");
		System.out.println("CCs: " + ci.connectedSets().size());

		// Create the new ontologies
		ci.connectedSets().stream().forEach(cc -> {
			// somehow create the new ontologies
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

		switch (expr.getClassExpressionType()) {

		// ------------------------------ Object Restrictions
		// ---------------------------

		// ObjectComplementOf
		case OBJECT_COMPLEMENT_OF:
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(((OWLObjectComplementOf) expr).getOperand()));
			break;

		// 2ary logical operators
		case OBJECT_INTERSECTION_OF:
		case OBJECT_UNION_OF:
			((OWLNaryBooleanClassExpression) expr).getOperandsAsList().stream().forEach(sub -> {
				if (!expr.isOWLThing() && !sub.isOWLThing()) {
					g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(sub));
				}
			});
			break;

		// Object Restrictions
		case OBJECT_SOME_VALUES_FROM:
		case OBJECT_ALL_VALUES_FROM:
		case OBJECT_EXACT_CARDINALITY:
		case OBJECT_MAX_CARDINALITY:
		case OBJECT_MIN_CARDINALITY:
			OWLQuantifiedObjectRestriction restriction = (OWLQuantifiedObjectRestriction) expr;
			OWLObjectPropertyExpression property = restriction.getProperty();
			OWLClassExpression con = restriction.getFiller();
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), getPropertyVertex(property, 0));
			if (!((con.isOWLThing() || con.isOWLNothing()))) {
				g.addEdge(getPropertyVertex(property, 1), OntologyDescriptor.getCleanNameOWLObj(con));
			}
			break;

		// ObjectOneOf
		case OBJECT_ONE_OF:
			((OWLObjectOneOf) expr).operands().forEach(indiv -> {
				g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(indiv));
			});
			break;

		// Self restriction
		case OBJECT_HAS_SELF:
			OWLObjectPropertyExpression propertySelf = ((OWLObjectHasSelf) expr).getProperty();
			g.addEdge(getPropertyVertex(propertySelf, 0), getPropertyVertex(propertySelf, 1));
			break;

		// ObjectHasValue
		case OBJECT_HAS_VALUE:
			OWLObjectHasValue hasVal = (OWLObjectHasValue) expr;
			g.addEdge(getPropertyVertex(hasVal.getProperty(), 1), OntologyDescriptor.getCleanNameOWLObj(hasVal.getFiller()));
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), getPropertyVertex(hasVal.getProperty(), 0));
			break;

		// ------------------------------ Data Restrictions ---------------------------
		case DATA_SOME_VALUES_FROM:
		case DATA_ALL_VALUES_FROM:
		case DATA_EXACT_CARDINALITY:
		case DATA_MAX_CARDINALITY:
		case DATA_MIN_CARDINALITY:
			OWLQuantifiedDataRestriction dataRestriction = (OWLQuantifiedDataRestriction) expr;
			OWLPropertyExpression dProperty = dataRestriction.getProperty();
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(dProperty) + Settings.PROPERTY_0_DESIGNATOR);
			break;

		case DATA_HAS_VALUE:
			OWLDataHasValue dHasVal = (OWLDataHasValue) expr;
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(dHasVal.getProperty()) + Settings.PROPERTY_0_DESIGNATOR);
			break;

		default:
			if (!(expr instanceof OWLClass)) {
				System.err.println("Missing OWLClassExpression: " + expr.getClassExpressionType() + ". Form: " + expr);
			}
			break;
		}
	}

	/**
	 * Adds edges for all axioms
	 * 
	 * @param ax The axiom
	 */
	public void addAxiomEdges(OWLAxiom ax) {

		String vertex = null;

		switch (ax.getAxiomType().toString()) {

		// Order inspired by https://www.w3.org/TR/owl2-syntax
		// -------------------- Class Expression Axioms ------------------

		case "SubClassOf":
			OWLSubClassOfAxiom subCOf = (OWLSubClassOfAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(subCOf.getSubClass());
			if (!subCOf.getSuperClass().isOWLThing()) {
				g.addEdge(vertex, OntologyDescriptor.getCleanNameOWLObj(subCOf.getSuperClass()));
			}
			break;

		case "EquivalentClasses":
		case "DisjointClasses":
			OWLNaryClassAxiom naryaxiom = (OWLNaryClassAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(naryaxiom.getOperandsAsList().get(0));
			List<String> vertexList = new ArrayList<>();
			for (OWLClassExpression cexp : naryaxiom.getOperandsAsList()) {
				vertexList.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
			}
			connect_vertexes_stepwise(vertexList);
			break;

		case "DisjointUnion":
			OWLDisjointUnionAxiom duax = (OWLDisjointUnionAxiom) ax;
			List<String> vertexList2 = new ArrayList<>();
			duax.getOWLDisjointClassesAxiom().classExpressions().forEach(cexp -> {
				vertexList2.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
			});
			duax.getOWLEquivalentClassesAxiom().classExpressions().forEach(cexp -> {
				vertexList2.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
			});
			connect_vertexes_stepwise(vertexList2);
			vertex = vertexList2.get(0);
			break;

		// ----------------- Object Property Axioms -------------------
		case "SubObjectPropertyOf": // Chain suchen
			OWLSubObjectPropertyOfAxiom subObjectPropAx = (OWLSubObjectPropertyOfAxiom) ax;
			OWLObjectPropertyExpression subProp = subObjectPropAx.getSubProperty();
			OWLObjectPropertyExpression superProp = subObjectPropAx.getSuperProperty();
			vertex = getPropertyVertex(subProp, 0);
			if (!superProp.isOWLTopObjectProperty()) {
				g.addEdge(vertex, getPropertyVertex(superProp, 0));
				g.addEdge(getPropertyVertex(subProp, 1), getPropertyVertex(superProp, 1));
			}
			break;

		case "SubPropertyChainOf":
			OWLSubPropertyChainOfAxiom propChainAx = (OWLSubPropertyChainOfAxiom) ax;
			List<OWLObjectPropertyExpression> propertyChain = propChainAx.getPropertyChain();
			int chainLength = propertyChain.size();
			vertex = getPropertyVertex(propertyChain.get(0), 0);
			// Link first with superproperty
			String superProp2 = getPropertyVertex(propChainAx.getSuperProperty(), 0);
			if (!vertex.equals(superProp2)) {
				g.addEdge(vertex, superProp2);
			}
			// Link last with superproperty
			g.addEdge(getPropertyVertex(propertyChain.get(chainLength - 1), 1),
					getPropertyVertex(propChainAx.getSuperProperty(), 1));
			for (int k = 0; k < chainLength - 1; k++) {
				g.addEdge(getPropertyVertex(propertyChain.get(k), 1), getPropertyVertex(propertyChain.get(k + 1), 0));
			}
			break;

		case "EquivalentObjectProperties":
		case "DisjointObjectProperties":
			OWLNaryPropertyAxiom<OWLObjectPropertyExpression> naryPropAx = (OWLNaryPropertyAxiom<OWLObjectPropertyExpression>) ax;
			List<String> object_prop_zero = new ArrayList<>();
			List<String> object_prop_one = new ArrayList<>();
			naryPropAx.properties().forEach(prop -> {
				object_prop_zero.add(getPropertyVertex(prop, 0));
				object_prop_one.add(getPropertyVertex(prop, 1));
			});
			connect_vertexes_stepwise(object_prop_zero);
			connect_vertexes_stepwise(object_prop_one);
			vertex = object_prop_zero.get(0);
			break;

		case "InverseObjectProperties":
			OWLInverseObjectPropertiesAxiom iopax = (OWLInverseObjectPropertiesAxiom) ax;
			OWLObjectPropertyExpression firstProp = iopax.getFirstProperty();
			OWLObjectPropertyExpression secondProp = iopax.getSecondProperty();

			vertex = getPropertyVertex(firstProp, 0);
			g.addEdge(vertex, getPropertyVertex(secondProp, 1));
			g.addEdge(getPropertyVertex(secondProp, 0), getPropertyVertex(firstProp, 1));
			break;

		case "ObjectPropertyRange":
			OWLObjectPropertyRangeAxiom objPropRangeAx = (OWLObjectPropertyRangeAxiom) ax;
			vertex = getPropertyVertex(objPropRangeAx.getProperty(), 0);
			if (!objPropRangeAx.getRange().isOWLThing()) {
				g.addEdge(getPropertyVertex(objPropRangeAx.getProperty(), 1), OntologyDescriptor.getCleanNameOWLObj(objPropRangeAx.getRange()));
			}
			break;

		case "ObjectPropertyDomain":
			OWLObjectPropertyDomainAxiom objPropDomAx = (OWLObjectPropertyDomainAxiom) ax;
			vertex = getPropertyVertex(objPropDomAx.getProperty(), 0);
			if (!objPropDomAx.getDomain().isOWLThing()) {
				g.addEdge(vertex, OntologyDescriptor.getCleanNameOWLObj(objPropDomAx.getDomain()));
			}
			break;

		case "FunctionalObjectProperty":
		case "AsymmetricObjectProperty":
		case "InverseFunctionalObjectProperty":
		case "IrrefexiveObjectProperty":
			vertex = getPropertyVertex(((OWLObjectPropertyCharacteristicAxiom) ax).getProperty(), 0);
			break;
		case "ReflexiveObjectProperty":
		case "SymmetricObjectProperty":
		case "TransitiveObjectProperty":
			OWLObjectPropertyCharacteristicAxiom propax = (OWLObjectPropertyCharacteristicAxiom) ax;
			vertex = getPropertyVertex(propax.getProperty(), 0);
			g.addEdge(vertex, getPropertyVertex(propax.getProperty(), 1));
			break;

		// ------------------- Data Property Axioms --------------------
		case "SubDataPropertyOf":
			OWLSubDataPropertyOfAxiom subDataPropAx = (OWLSubDataPropertyOfAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(subDataPropAx.getSubProperty()) + Settings.PROPERTY_0_DESIGNATOR;

			if (!subDataPropAx.getSuperProperty().isOWLTopDataProperty()) {
				g.addEdge(vertex, OntologyDescriptor.getCleanNameOWLObj(subDataPropAx.getSuperProperty()) + Settings.PROPERTY_0_DESIGNATOR);
			}
			break;

		case "EquivalentDataProperties":
		case "DisjointDataProperties":
			OWLNaryPropertyAxiom<OWLDataPropertyExpression> naryDataPropAx = (OWLNaryPropertyAxiom<OWLDataPropertyExpression>) ax;
			List<String> naryDataPropAxNames = naryDataPropAx.properties().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
					.collect(Collectors.toList());
			vertex = naryDataPropAxNames.get(0);
			connect_vertexes_stepwise(naryDataPropAxNames);
			break;

		case "DataPropertyRange":
			OWLDataPropertyRangeAxiom dataPropRangeAx = (OWLDataPropertyRangeAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(dataPropRangeAx.getProperty());
			break;

		case "DataPropertyDomain":
			OWLDataPropertyDomainAxiom dataPropDomAx = (OWLDataPropertyDomainAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(dataPropDomAx.getProperty()) + Settings.PROPERTY_0_DESIGNATOR;
			if (!dataPropDomAx.getDomain().isOWLThing()) {
				g.addEdge(vertex, OntologyDescriptor.getCleanNameOWLObj(dataPropDomAx.getDomain()));
			}
			break;

		case "FunctionalDataProperty":
			OWLFunctionalDataPropertyAxiom funcDataPropAx = (OWLFunctionalDataPropertyAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(funcDataPropAx.getProperty()) + Settings.PROPERTY_0_DESIGNATOR;
			break;

		// ------------------- Assertions ---------------------

		case "SameIndividual":
		case "DifferentIndividuals":
			OWLNaryIndividualAxiom sameIndivAx = (OWLNaryIndividualAxiomImpl) ax;
			List<String> listOfIndiv = sameIndivAx.individuals().map(e -> OntologyDescriptor.getCleanNameOWLObj(e)).collect(Collectors.toList());
			vertex = listOfIndiv.get(0);
			connect_vertexes_stepwise(listOfIndiv);
			break;

		case "ClassAssertion":
			OWLClassAssertionAxiom classAssert = (OWLClassAssertionAxiom) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(classAssert.getIndividual());
			if (!classAssert.getClassExpression().isTopEntity()) {
				g.addEdge(vertex, OntologyDescriptor.getCleanNameOWLObj(classAssert.getClassExpression()));
			}
			break;

		case "ObjectPropertyAssertion":
		case "NegativeObjectPropertyAssertion":
			OWLPropertyAssertionAxiom<OWLObjectPropertyExpression, OWLIndividual> objectPropAss = (OWLPropertyAssertionAxiom<OWLObjectPropertyExpression, OWLIndividual>) ax;
			OWLObjectPropertyExpression property = objectPropAss.getProperty();
			OWLIndividual subject = objectPropAss.getSubject();
			vertex = OntologyDescriptor.getCleanNameOWLObj(subject);
			g.addEdge(vertex, getPropertyVertex(property, 0));
			g.addEdge(getPropertyVertex(property, 1), OntologyDescriptor.getCleanNameOWLObj(objectPropAss.getObject()));
			break;

		case "DataPropertyAssertion":
		case "NegativeDataPropertyAssertion":
			OWLPropertyAssertionAxiom<OWLDataPropertyExpression, OWLLiteral> dataPropAss = (OWLPropertyAssertionAxiom<OWLDataPropertyExpression, OWLLiteral>) ax;
			vertex = OntologyDescriptor.getCleanNameOWLObj(dataPropAss.getProperty());
			break;

		default:
			if (!(ax instanceof OWLAnnotationAssertionAxiom)) {
				System.err.println("Missing axiom: " + ax.getAxiomType() + " Form: " + ax);
			}
			break;
		}

		// FunctionalObjectProperty
		// } else if (ax instanceof OWLFunctionalObjectPropertyAxiom) {
		// edge = g.addEdge(property, property)

		if (vertex != null) {
			if(!vertexToAxiom.containsKey(vertex)) {
				vertexToAxiom.put(vertex, new HashSet<OWLAxiom>());
			}
			vertexToAxiom.get(vertex).add(ax);
		}
	}

	Map<OWLObjectPropertyExpression, String[]> propertyToName = new HashMap<>();

	/**
	 * Returns the corresponding vertex while abiding the potential inverses
	 * 
	 * @param property
	 * @param i
	 * @return String of the form <Property>0 or <Property>1
	 */
	private String getPropertyVertex(OWLObjectPropertyExpression property, int i) {
		if (!(propertyToName.containsKey(property) && propertyToName.get(property)[i] != null)) {
			if (i != 0 && i != 1) {
				throw new IllegalArgumentException("Only 0 or 1 as parameter allowed");
			}

			String designator = i == 0 ? Settings.PROPERTY_0_DESIGNATOR : Settings.PROPERTY_1_DESIGNATOR;
			if (property instanceof OWLObjectProperty) {
				return OntologyDescriptor.getCleanNameOWLObj(property) + designator;
			}
			int j = i == 1 ? 0 : 1;
			if (!propertyToName.containsKey(property)) {
				propertyToName.put(property, new String[2]);
			}
			propertyToName.get(property)[i] = getPropertyVertex(property.getInverseProperty(), j);
		}
		return propertyToName.get(property)[i];

	}

	private void connect_vertexes_stepwise(List<String> vertList) {
		int numOfVert = vertList.size();
		if (numOfVert > 1) {
			for (int i = 0; i < numOfVert - 1; i++) {
				g.addEdge(vertList.get(i), vertList.get(i + 1));
			}
		}

	}

}
