package uni.sasjonge.partitioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;
import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasAxiomsByType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
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
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityByTypeProvider;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
import uni.sasjonge.heuristics.biconnectivity.BiconnectivityManager;
import uni.sasjonge.heuristics.communitydetection.CommunityDetectionManager;
import uni.sasjonge.utils.OntologyDescriptor;

/**
 * 
 * Core partitioning algorithm based on the following paper: Jongebloed, Sascha,
 * and Thomas Schneider. "Ontology Partitioning Using E-Connections Revisited."
 * MedRACER+ WOMoCoE@ KR. 2018. Link to paper:
 * http://www.informatik.uni-bremen.de/tdki/research/papers/2018/JS-DL18.pdf
 * 
 * @author Sascha Jongebloed
 *
 */ 
public class PartitioningCore {

	public Graph<String, DefaultEdge> g = new DefaultUndirectedGraph<>(DefaultEdge.class);
	// Labelling of edges by axioms 
	public Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms = new HashMap<>();
	// Save one of the edges of a given edge to have an easier way of recall
	// We only save edges that are labelled with axioms
	public Map<DefaultEdge, String> edgeToVertex = new HashMap<>();
	// Map of Axioms to the edges it created
	public Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms = new HashMap<>();
	Map<OWLAnnotationProperty, Set<OWLAxiom>> annotToAxioms;
	
	// Counter for the special case in the addition of haskey axioms with owlthing as subject
	int counter = 0;

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
	public List<OWLOntology> partition(OWLOntology ontology) throws IOException, ExportException {

		// The partitions
		List<OWLOntology> toReturn = new ArrayList<>(); 

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
				g.addVertex(OntologyDescriptor.getCleanNameOWLObj(dataProp) + Settings.PROPERTY_0_DESIGNATOR);
			}
		});

		// Vertex: Individuals
		ontology.individualsInSignature().forEach(indiv -> {
			g.addVertex(OntologyDescriptor.getCleanNameOWLObj(indiv));
		});

		// Save all vertexes that will later label the cc structure
		Set<String> labellingVertexes = ontology.classesInSignature().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
				.collect(Collectors.toSet());
		labellingVertexes.addAll(g.vertexSet());

		// Vertex: SubConcepts
		ontology.logicalAxioms().forEach(a -> {
			a.nestedClassExpressions().forEach(nested -> {
				if (!nested.isOWLThing()) {
					g.addVertex(OntologyDescriptor.getCleanNameOWLObj(nested));
				}
			});
			
		});
		
		// Also add the vertices for the equivalentclasses part
		// of disjoint union
		// TODO: Is this the right way to get the ObjectUnionOf of the
		// Disjoint Union Axioms?
		ontology.axioms(AxiomType.DISJOINT_UNION).forEach(ax -> {
			OWLEquivalentClassesAxiom eax = ax.getOWLEquivalentClassesAxiom();
			eax.nestedClassExpressions().forEach(nested -> {
				if (!nested.isOWLThing()) {
					g.addVertex(OntologyDescriptor.getCleanNameOWLObj(nested));
				}
			});
		});

		long addVertexEndTime = System.nanoTime();
		System.out.println("Adding vertexes took " + (addVertexEndTime - addVertexStartTime) / 1000000 + "ms");

		// For every annotation assertion add the corresponding
		// sub_SubAnnotationPropertyOfAxioms,
		// AnnotationPropertyRangeAxiom and AnnotationPropertyDomainAxioms
		annotToAxioms = getAnnotationAxioms(ontology);

		long addSubEdgeStartTime = System.nanoTime();

		// Add the edges according to our defined algorithm
		// Edge: All sub concepts (corresponding to line 3 of the algorithm in the
		// paper)
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

		// Add edges for all axioms (corresponding to line 4 of the algorithm in the
		// paper)
		long addAxiomEdgeStartTime = System.nanoTime();
		ontology.logicalAxioms().forEach(this::addAxiomEdges);
		long addAxiomEdgeEndTime = System.nanoTime();

		System.out.println("Adding axiom edges took " + (addAxiomEdgeEndTime - addAxiomEdgeStartTime) / 1000000 + "ms");

		// ********************Biconnectivity heuristic********************
		if (Settings.USE_BH) {
			g = (new BiconnectivityManager()).removeAxiomLabelledBridgesNoSingletons(g, edgeToAxioms, createdByAxioms);
			System.out.println("Finished biconnectivity heuristic");
		}

		// ***************** Community detection heuristic ****************
		if (Settings.USE_CD) {
			CommunityDetectionManager cdm = new CommunityDetectionManager(g, createdByAxioms);
			g = cdm.removeBridges(createdByAxioms, edgeToAxioms);
			System.out.println("Finished community detection heuristic");
		}

		// ******************* Remove labels of removed edges *************
		if (Settings.USE_CD || Settings.USE_BH) {
			edgeToAxioms = edgeToAxioms.entrySet().stream().filter(t -> g.containsEdge(t.getKey()))
					.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		}

		// ****************************************************************
		// Search for components without axiom labels and connect them to a partiton
		ConnectivityInspector<String, DefaultEdge> ciOld = new ConnectivityInspector<>(g);
		// Collection of all cc with labels
		Set<Set<String>> ccWithLabel = new HashSet<>();
		// Collection of all cc without labels
		Set<Set<String>> ccWithoutLabel = new HashSet<>();
		// Collect the ccs into their respective collection
		ciOld.connectedSets().stream().forEach(cc -> {
			boolean hasLabel = false;
			for (String ver : cc) {
				for (DefaultEdge e : g.edgesOf(ver)) {
					if (edgeToAxioms.containsKey(e)) {
						ccWithLabel.add(cc);
						hasLabel = true;
						break;
					}
				}
			}
			;
			if (!hasLabel) {
				ccWithoutLabel.add(cc);
			}
		});
		
		// If we have no cc with a label, then the heuristic removed everything. Show a error message 
		// and return the original ontology
		if (ccWithLabel.isEmpty()) {
			System.err.println("All axioms where removed. The reason is probably a too strong heuristic!");
			toReturn.clear();
			toReturn.add(ontology);
			return toReturn;
		}
		
		// Connect the cc's without a label to cc's with a label (Could be replaced
		// by a UI that let users choose to which cc a cc without label should be
		// assigned
		// to)
		connectUnLabelledCCToBiggest(g, ccWithLabel, ccWithoutLabel);

		long ccStartTime = System.nanoTime();
		// Find the connected components
		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(g);
		long ccEndTime = System.nanoTime();
		// ci.connectedSets().stream().forEach(System.out::println);
		System.out.println("Finding the cc's took " + (ccEndTime - ccStartTime) / 1000000 + "ms");
		System.out.println("CCs: " + ci.connectedSets().size());

		// Add Annotations and Declarations as labels to the graph
		ontology.axioms().filter(ax -> {
			return !ax.isLogicalAxiom();
		}).forEach(ax -> {
			addNonLogicalAxiomEdges(ontology, ax);
		});

		// Create the new ontologies (if flag is set, that we need them for the output)
		if (Settings.EXPORT_ONTOLOGIES) {
			toReturn = createOntologyFromCC(ci.connectedSets(), ontology);
		}

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
			// Edge between expr and it's complement
			OWLClassExpression op = ((OWLObjectComplementOf) expr).getOperand();
			if (!op.isOWLThing()) {
				g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr),
					OntologyDescriptor.getCleanNameOWLObj(op));
			}
			break;

		// 2ary logical operators
		case OBJECT_INTERSECTION_OF:
		case OBJECT_UNION_OF:
			((OWLNaryBooleanClassExpression) expr).getOperandsAsList().stream().forEach(sub -> {
				if (!expr.isOWLThing() && !sub.isOWLThing()) {
					// Edgen between expr and all Operands
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
			// Edge between expr and the right property node
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), getPropertyVertex(property, 0));
			if (!((con.isOWLThing() || con.isOWLNothing()))) {
				// Edge between property and filler
				g.addEdge(getPropertyVertex(property, 1), OntologyDescriptor.getCleanNameOWLObj(con));
			}
			break;

		// ObjectOneOf
		case OBJECT_ONE_OF:
			((OWLObjectOneOf) expr).operands().forEach(indiv -> {
				// Edge between expr and the individual
				g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), OntologyDescriptor.getCleanNameOWLObj(indiv));
			});
			break;

		// Self restriction
		case OBJECT_HAS_SELF:
			OWLObjectPropertyExpression propertySelf = ((OWLObjectHasSelf) expr).getProperty();
			// Connect expr with r0 and r1 for property r
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), getPropertyVertex(propertySelf, 0));
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr), getPropertyVertex(propertySelf, 1));
			break;

		// ObjectHasValue
		case OBJECT_HAS_VALUE:
			OWLObjectHasValue hasVal = (OWLObjectHasValue) expr;
			// connect expr to r0 and r1 to the value
			g.addEdge(getPropertyVertex(hasVal.getProperty(), 1),
					OntologyDescriptor.getCleanNameOWLObj(hasVal.getFiller()));
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
			// connect expr to the data property
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr),
					OntologyDescriptor.getCleanNameOWLObj(dProperty) + Settings.PROPERTY_0_DESIGNATOR);
			break;

		case DATA_HAS_VALUE:
			OWLDataHasValue dHasVal = (OWLDataHasValue) expr;
			// Connect expr. to data property
			g.addEdge(OntologyDescriptor.getCleanNameOWLObj(expr),
					OntologyDescriptor.getCleanNameOWLObj(dHasVal.getProperty()) + Settings.PROPERTY_0_DESIGNATOR);
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

		// Vertex for labelling
		DefaultEdge labelledEdge = null;

		switch (ax.getAxiomType().toString()) {

		// Order inspired by https://www.w3.org/TR/owl2-syntax
		// -------------------- Class Expression Axioms ------------------

		case "SubClassOf":
			OWLSubClassOfAxiom subCOf = (OWLSubClassOfAxiom) ax;
			if (!subCOf.getSuperClass().isOWLThing() && !subCOf.getSubClass().isOWLThing()) {
				// Add an edge between the sub- and super class
				labelledEdge = addEdgeHelp(g, OntologyDescriptor.getCleanNameOWLObj(subCOf.getSubClass()),
						OntologyDescriptor.getCleanNameOWLObj(subCOf.getSuperClass()));
			} else if (!subCOf.getSuperClass().isOWLThing()) {
				labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(subCOf.getSuperClass()));
			} else {
				labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(subCOf.getSubClass()));
			}
			break;

		case "EquivalentClasses":
		case "DisjointClasses":
			OWLNaryClassAxiom naryaxiom = (OWLNaryClassAxiom) ax;

			// collect all class expressions in this axiom
			List<String> vertexList = new ArrayList<>();
			naryaxiom.operands().forEach(cexp -> {
				if (!cexp.isOWLThing( )) {
					vertexList.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
				}
			});
			// Add edges between all equivalent classes
			if (vertexList.size() != 1) {
			connect_vertexes_stepwise_labelled(vertexList, ax);
			} else {
				labelledEdge = createLoopEdge(g, vertexList.iterator().next());
			}
			break;

		case "DisjointUnion":
			OWLDisjointUnionAxiom duax = (OWLDisjointUnionAxiom) ax;
			List<String> vertexList2 = new ArrayList<>();
			// Add the class which is equivalent to the disjoint union
			if (!duax.getOWLClass().isOWLThing()) {
				vertexList2.add(OntologyDescriptor.getCleanNameOWLObj(duax.getOWLClass()));
			}
			// Collect all equivalent classes
			duax.getOWLEquivalentClassesAxiom().classExpressions().forEach(cexp -> {
				if (!cexp.isOWLThing()) {
					vertexList2.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
				}
			});
			duax.getOWLDisjointClassesAxiom().classExpressions().forEach(cexp -> {
				if (!cexp.isOWLThing()) {
					vertexList2.add(OntologyDescriptor.getCleanNameOWLObj(cexp));
				}
			});

			// Connect this classes
			// Add edges between all equivalent classes
			if (vertexList2.size() != 1) {
			connect_vertexes_stepwise_labelled(vertexList2, ax);
			} else {
				labelledEdge = createLoopEdge(g, vertexList2.iterator().next());
			}
			break;

		// ----------------- Object Property Axioms -------------------
		case "SubObjectPropertyOf":
			OWLSubObjectPropertyOfAxiom subObjectPropAx = (OWLSubObjectPropertyOfAxiom) ax;
			OWLObjectPropertyExpression subProp = subObjectPropAx.getSubProperty();
			OWLObjectPropertyExpression superProp = subObjectPropAx.getSuperProperty();
			String vertexSubPropZero = getPropertyVertex(subProp, 0);
			if (!superProp.isOWLTopObjectProperty()) {
				labelledEdge = addEdgeHelp(g, vertexSubPropZero, getPropertyVertex(superProp, 0));
				DefaultEdge edge = addEdgeHelp(g, getPropertyVertex(subProp, 1), getPropertyVertex(superProp, 1));
				if (!createdByAxioms.containsKey(edge)) {
					createdByAxioms.put(edge, new HashSet<OWLAxiom>());
				}
				createdByAxioms.get(edge).add(ax);
			} else {
				labelledEdge = createLoopEdge(g, vertexSubPropZero);
			}
			break;

		case "SubPropertyChainOf":
			OWLSubPropertyChainOfAxiom propChainAx = (OWLSubPropertyChainOfAxiom) ax;
			List<OWLObjectPropertyExpression> propertyChain = propChainAx.getPropertyChain();
			int chainLength = propertyChain.size();
			String vertexPropChain0 = getPropertyVertex(propertyChain.get(0), 0);
			// Link first Property0 with Superproperty0
			String superProp2 = getPropertyVertex(propChainAx.getSuperProperty(), 0);
			labelledEdge = addEdgeHelp(g, vertexPropChain0, superProp2);
			// Link last Property1 with Superproperty1
			DefaultEdge edge = addEdgeHelp(g, getPropertyVertex(propertyChain.get(chainLength - 1), 1),
					getPropertyVertex(propChainAx.getSuperProperty(), 1));
			if (!createdByAxioms.containsKey(edge)) {
				createdByAxioms.put(edge, new HashSet<OWLAxiom>());
			}
			createdByAxioms.get(edge).add(ax);
			// Link all other properties in the chain, by connecting the R1 to S0 iff
			// propertychain has the form (...,R,S,...)
			for (int k = 0; k < chainLength - 1; k++) {
				DefaultEdge edge1 = addEdgeHelp(g, getPropertyVertex(propertyChain.get(k), 1),
						getPropertyVertex(propertyChain.get(k + 1), 0));
				if (!createdByAxioms.containsKey(edge1)) {
					createdByAxioms.put(edge1, new HashSet<OWLAxiom>());
				}
				createdByAxioms.get(edge1).add(ax);
			}
			break;

		case "EquivalentObjectProperties":
		case "DisjointObjectProperties":
			OWLNaryPropertyAxiom<OWLObjectPropertyExpression> naryPropAx = (OWLNaryPropertyAxiom<OWLObjectPropertyExpression>) ax;
			List<String> object_prop_zero = new ArrayList<>();
			List<String> object_prop_one = new ArrayList<>();

			// get the R0 and R1 for all object properties R in the axiom
			naryPropAx.properties().forEach(prop -> {
				object_prop_zero.add(getPropertyVertex(prop, 0));
				object_prop_one.add(getPropertyVertex(prop, 1));
			});

			// Connect all R0
			connect_vertexes_stepwise_labelled(object_prop_zero, ax);

			// Connect all R1
			connect_vertexes_stepwise(object_prop_one, ax);

			break;

		case "InverseObjectProperties":
			OWLInverseObjectPropertiesAxiom iopax = (OWLInverseObjectPropertiesAxiom) ax;
			// Get the properties R and S with R being an inverse of S
			OWLObjectPropertyExpression firstProp = iopax.getFirstProperty();
			OWLObjectPropertyExpression secondProp = iopax.getSecondProperty();

			// connect R0 with S1 and R1 with S0
			labelledEdge = addEdgeHelp(g, getPropertyVertex(firstProp, 0), getPropertyVertex(secondProp, 1));
			DefaultEdge edge1 = addEdgeHelp(g, getPropertyVertex(secondProp, 0), getPropertyVertex(firstProp, 1));
			if (!createdByAxioms.containsKey(edge1)) {
				createdByAxioms.put(edge1, new HashSet<OWLAxiom>());
			}
			createdByAxioms.get(edge1).add(ax);
			break;

		case "ObjectPropertyDomain":
			OWLObjectPropertyDomainAxiom objPropDomAx = (OWLObjectPropertyDomainAxiom) ax;
			if (!objPropDomAx.getDomain().isOWLThing()) {
				// Connect R0 to the domain
				labelledEdge = addEdgeHelp(g, getPropertyVertex(objPropDomAx.getProperty(), 0),
						OntologyDescriptor.getCleanNameOWLObj(objPropDomAx.getDomain()));
			} else {
				labelledEdge = createLoopEdge(g, getPropertyVertex(objPropDomAx.getProperty(), 0));
			}
			break;

		case "ObjectPropertyRange":
			OWLObjectPropertyRangeAxiom objPropRangeAx = (OWLObjectPropertyRangeAxiom) ax;
			if (!objPropRangeAx.getRange().isOWLThing()) {
				// Connect R1 to the range
				labelledEdge = addEdgeHelp(g, getPropertyVertex(objPropRangeAx.getProperty(), 0),
						OntologyDescriptor.getCleanNameOWLObj(objPropRangeAx.getRange()));
			} else {
				labelledEdge = createLoopEdge(g, getPropertyVertex(objPropRangeAx.getProperty(), 0));
			}
			break;

		case "FunctionalObjectProperty":
		case "InverseFunctionalObjectProperty":
			// In this case we only need to label a vertex with this axiom
			labelledEdge = createLoopEdge(g,
					getPropertyVertex(((OWLObjectPropertyCharacteristicAxiom) ax).getProperty(), 0));
			break;

		case "ReflexiveObjectProperty":
		case "IrrefexiveObjectProperty":
		case "SymmetricObjectProperty":
		case "AsymmetricObjectProperty":
		case "TransitiveObjectProperty":
			OWLObjectPropertyCharacteristicAxiom propax = (OWLObjectPropertyCharacteristicAxiom) ax;
			// In this cases we need to connect R0 and R1
			labelledEdge = addEdgeHelp(g, getPropertyVertex(propax.getProperty(), 0),
					getPropertyVertex(propax.getProperty(), 1));
			break;

		// ------------------- Data Property Axioms --------------------
		// For convenience we use p0 as the node for the dataproperty
		// We don't handle the range for the dataproperty, because it's not needed to
		// partition the ontology

		case "SubDataPropertyOf":
			OWLSubDataPropertyOfAxiom subDataPropAx = (OWLSubDataPropertyOfAxiom) ax;

			if (!subDataPropAx.getSuperProperty().isOWLTopDataProperty()) {
				labelledEdge = addEdgeHelp(g,
						OntologyDescriptor.getCleanNameOWLObj(subDataPropAx.getSubProperty())
								+ Settings.PROPERTY_0_DESIGNATOR,
						OntologyDescriptor.getCleanNameOWLObj(subDataPropAx.getSuperProperty())
								+ Settings.PROPERTY_0_DESIGNATOR);
			} else {
				labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(subDataPropAx.getSubProperty())
						+ Settings.PROPERTY_0_DESIGNATOR);
			}
			break;

		case "EquivalentDataProperties":
		case "DisjointDataProperties":
			OWLNaryPropertyAxiom<OWLDataPropertyExpression> naryDataPropAx = (OWLNaryPropertyAxiom<OWLDataPropertyExpression>) ax;
			// Collect all properties
			List<String> naryDataPropAxNames = naryDataPropAx.properties()
					.map(e -> OntologyDescriptor.getCleanNameOWLObj(e)+ Settings.PROPERTY_0_DESIGNATOR).collect(Collectors.toList());

			// Connect all properties
			connect_vertexes_stepwise_labelled(naryDataPropAxNames, ax);
			break;

		case "DataPropertyRange":
			OWLDataPropertyRangeAxiom dataPropRangeAx = (OWLDataPropertyRangeAxiom) ax;
			// Just save the axiom (we don't handle the "datapartition" in this algorithm,
			// because
			// it's not necessary for the partitioning)
			labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(dataPropRangeAx.getProperty())
					+ Settings.PROPERTY_0_DESIGNATOR);
			break;

		case "DataPropertyDomain":
			OWLDataPropertyDomainAxiom dataPropDomAx = (OWLDataPropertyDomainAxiom) ax;
			if (!dataPropDomAx.getDomain().isOWLThing()) {
				labelledEdge = addEdgeHelp(g,
						OntologyDescriptor.getCleanNameOWLObj(dataPropDomAx.getProperty())
								+ Settings.PROPERTY_0_DESIGNATOR,
						OntologyDescriptor.getCleanNameOWLObj(dataPropDomAx.getDomain()));
			} else {
				labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(dataPropDomAx.getProperty())
						+ Settings.PROPERTY_0_DESIGNATOR);
			}
			break;

		case "FunctionalDataProperty":
			OWLFunctionalDataPropertyAxiom funcDataPropAx = (OWLFunctionalDataPropertyAxiom) ax;
			System.out.println(funcDataPropAx.getProperty().toString());
			// Similarly to FunctionalObjectProperty: We only need to add the axiom to a
			// label
			labelledEdge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(funcDataPropAx.getProperty())
					+ Settings.PROPERTY_0_DESIGNATOR);
			break;

		// ---------------------- Key -------------------------
		case "HasKey":
			OWLHasKeyAxiom hKeyAx = (OWLHasKeyAxiom) ax;
		
			OWLClassExpression cExp = hKeyAx.getClassExpression();
			String cExpString = cExp.isOWLThing() ? "topRepresentant#" + counter : OntologyDescriptor.getCleanNameOWLObj(cExp);
			
			// Special case if cExp is OWL Thing/Top: Add a new node representing top
			if (cExp.isOWLThing()) {
				g.addVertex("topRepresentant#" + counter++);
			}
			
			AtomicReference<DefaultEdge> eA = new AtomicReference<>();
						
			hKeyAx.objectPropertyExpressions().forEach(oProp -> {
				DefaultEdge oEdge = addEdgeHelp(g, cExpString, OntologyDescriptor.getCleanNameOWLObj(oProp)+ Settings.PROPERTY_0_DESIGNATOR);
				if (eA.get() == null) {
					eA.set(oEdge);
				}
			});
			
			hKeyAx.dataPropertyExpressions().forEach(dProp -> {
				DefaultEdge pEdge = addEdgeHelp(g, cExpString, OntologyDescriptor.getCleanNameOWLObj(dProp)+ Settings.PROPERTY_0_DESIGNATOR);
				if (eA.get() == null) {
					eA.set(pEdge);
				}
			});
			
			labelledEdge = eA.get();
			
			break;
			
			
		// ------------------- Assertions ---------------------

		case "SameIndividual":
		case "DifferentIndividuals":
			OWLNaryIndividualAxiom sameIndivAx = (OWLNaryIndividualAxiomImpl) ax;

			// Collect all individuals in this axiom
			List<String> listOfIndiv = sameIndivAx.individuals().map(e -> OntologyDescriptor.getCleanNameOWLObj(e))
					.collect(Collectors.toList());

			// Connect them
			connect_vertexes_stepwise_labelled(listOfIndiv, ax);
			break;

		case "ClassAssertion":
			OWLClassAssertionAxiom classAssert = (OWLClassAssertionAxiom) ax;
			String vertex = OntologyDescriptor.getCleanNameOWLObj(classAssert.getIndividual());
			String vertex2 = OntologyDescriptor.getCleanNameOWLObj(classAssert.getClassExpression());

			// Simply connect the individual and the Class
			if (!classAssert.getClassExpression().isTopEntity()) {
				labelledEdge = addEdgeHelp(g, vertex, vertex2);
			} else {
				labelledEdge = createLoopEdge(g, vertex);
			}
			break;

		case "ObjectPropertyAssertion":
		case "NegativeObjectPropertyAssertion":
			OWLPropertyAssertionAxiom<OWLObjectPropertyExpression, OWLIndividual> objectPropAss = (OWLPropertyAssertionAxiom<OWLObjectPropertyExpression, OWLIndividual>) ax;

			// Get the property and the indivdual (the subject)
			OWLObjectPropertyExpression property = objectPropAss.getProperty();
			OWLIndividual subject = objectPropAss.getSubject();
			OWLIndividual object = objectPropAss.getObject();

			// Connect the Subject to R0 and the object to R1
			labelledEdge = addEdgeHelp(g, OntologyDescriptor.getCleanNameOWLObj(subject),
					getPropertyVertex(property, 0));
			DefaultEdge edge2 = addEdgeHelp(g, getPropertyVertex(property, 1),
					OntologyDescriptor.getCleanNameOWLObj(object));
			if (!createdByAxioms.containsKey(edge2)) {
				createdByAxioms.put(edge2, new HashSet<OWLAxiom>());
			}
			createdByAxioms.get(edge2).add(ax);
			break;

		case "DataPropertyAssertion":
		case "NegativeDataPropertyAssertion":
			OWLPropertyAssertionAxiom<OWLDataPropertyExpression, OWLLiteral> dataPropAss = (OWLPropertyAssertionAxiom<OWLDataPropertyExpression, OWLLiteral>) ax;
			labelledEdge = addEdgeHelp(g,
					OntologyDescriptor.getCleanNameOWLObj(dataPropAss.getProperty()) + Settings.PROPERTY_0_DESIGNATOR,
					OntologyDescriptor.getCleanNameOWLObj(dataPropAss.getSubject()));
			break;

		default:
			// Print a message if the axiomtype is not handled.
			System.err.println("Missing axiom: " + ax.getAxiomType() + " Form: " + ax);

			break;
		}

		if (labelledEdge != null) {
			// Add the labelling
			if (!edgeToAxioms.containsKey(labelledEdge)) {
				edgeToAxioms.put(labelledEdge, new HashSet<OWLAxiom>());
			}
			edgeToAxioms.get(labelledEdge).add(ax); 

			// Save it creating axiom
			if (!createdByAxioms.containsKey(labelledEdge)) {
				createdByAxioms.put(labelledEdge, new HashSet<OWLAxiom>());
			}
			createdByAxioms.get(labelledEdge).add(ax);
		}

	}

	/**
	 * Adds edges for all non-logical axioms
	 * 
	 * @param ax  The axiom
	 * @param ont The given ontology
	 */
	public void addNonLogicalAxiomEdges(OWLOntology ont, OWLAxiom ax) {

		// Vertex for labelling
		DefaultEdge edge = null;

		switch (ax.getAxiomType().toString()) {

		// --------------------- Non-logical axioms ----------------------
		case "AnnotationAssertion":
			OWLAnnotationAssertionAxiom annot = (OWLAnnotationAssertionAxiom) ax;
			// AtomicReferences as Wrapper for the Stream
			AtomicReference<DefaultEdge> edgeReference = new AtomicReference<>();

			if (annot.getSubject().asIRI().isPresent()) {
				ont.entitiesInSignature(annot.getSubject().asIRI().get()).forEach(subjectAsEntity -> {
					if (subjectAsEntity.isOWLObjectProperty() || subjectAsEntity.isOWLDataProperty()) {
						edgeReference.set(createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(subjectAsEntity)
								+ Settings.PROPERTY_0_DESIGNATOR));
						if (edgeReference.get() == null) {
							System.err.println("Edgerefernce is null");
						}
					} else if (subjectAsEntity.isOWLClass() || subjectAsEntity.isOWLNamedIndividual()) {
						if (g.containsVertex(OntologyDescriptor.getCleanNameOWLObj(subjectAsEntity))) {
							edgeReference
									.set(createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(subjectAsEntity)));
						} 
					} else {
						System.err.println("Missing annotation subject: " + subjectAsEntity.toString());
					}
					
					// Also save all all SubAnnotationPropertyOf, AnnotationPropertyRangeOf
					// and AnnotationPropertyDomainOf axioms containing the used
					// annotation property
					// If the edgeReference is null, then the property belongs to a heuristic-removed
					// entity
					if (edgeReference.get() != null && annotToAxioms.containsKey(annot.getProperty())) {
						for (OWLAxiom annoAx : annotToAxioms.get(annot.getProperty())) {
							if (!edgeToAxioms.containsKey(edgeReference.get())) {
								edgeToAxioms.put(edgeReference.get(), new HashSet<OWLAxiom>());
							}
							edgeToAxioms.get(edgeReference.get()).add(annoAx);
						}
					}

				});
			}

			edge = edgeReference.get();
			break;

		case "Declaration":
			OWLDeclarationAxiom decl = (OWLDeclarationAxiom) ax;
			OWLEntity entity = decl.getEntity();
			if (!entity.isTopEntity() && (entity.isOWLObjectProperty() || entity.isOWLDataProperty())) {
				edge = createLoopEdge(g,
						OntologyDescriptor.getCleanNameOWLObj(decl.getEntity()) + Settings.PROPERTY_0_DESIGNATOR);
			} else if (!entity.isTopEntity() && (entity.isOWLClass() || entity.isOWLNamedIndividual())) {
				if (g.containsVertex(OntologyDescriptor.getCleanNameOWLObj(decl.getEntity()))) {
					edge = createLoopEdge(g, OntologyDescriptor.getCleanNameOWLObj(decl.getEntity()));
				}
			} else if (entity.isOWLAnnotationProperty()) {

			} else {
				System.err.println("Missing declaration type: " + decl.toString());
			}
			break;

		// We already handled this axioms in the annotationAssertion case
		case "AnnotationPropertyRangeOf":
		case "AnnotationPropertyDomain":
		case "SubAnnotationPropertyOf":
			break;

		default:
			// Print a message if the axiomtype is not handled.
			System.err.println("Missing axiom: " + ax.getAxiomType() + " Form: " + ax);

			break;
		}

		if (edge != null) {
			if (!edgeToAxioms.containsKey(edge)) {
				edgeToAxioms.put(edge, new HashSet<OWLAxiom>());
			}
			edgeToAxioms.get(edge).add(ax);
		}
	}

	Map<OWLObjectPropertyExpression, String[]> propertyToName = new HashMap<>();

	private void connectUnLabelledCCToBiggest(Graph<String, DefaultEdge> g2, Set<Set<String>> ccWithLabel,
			Set<Set<String>> ccWithoutLabel) {
		// Sort the ccWithLabel by size
		Set<Set<String>> ccWithLabelSorted = ccWithLabel.stream().sorted((o1, o2) -> {
			return o1.size() - o2.size();
		}).collect(Collectors.toSet());
		;

		// Get a vertex of the biggest labelled cc
		String vertexOfLabeledCC = ccWithLabelSorted.iterator().next().iterator().next();

		// Connect every cc without a label to the biggest by adding
		// a edge
		ccWithoutLabel.forEach(cc -> {
			g.addEdge(cc.iterator().next(), vertexOfLabeledCC);
		});
	}

	/**
	 * Returns the corresponding vertex while abiding the potential inverses
	 * 
	 * @param property
	 * @param i
	 * @return String of the form <Property>0 or <Property>1
	 */
	private String getPropertyVertex(OWLObjectPropertyExpression property, int i) {
		// Only r1 and r0 are allowed
		if (i != 0 && i != 1) {
			throw new IllegalArgumentException("Only 0 or 1 as parameter allowed");
		}

		// propertiesToName are saved to improve the recall time. Check if they are
		// already stored:
		if (!(propertyToName.containsKey(property) && propertyToName.get(property)[i] != null)) {

			// If not, and the properety isn't event stored, store a String array of length
			// 2 (for 0 and 1) with the property as key
			if (!propertyToName.containsKey(property)) {
				propertyToName.put(property, new String[2]);
			}

			// If the property is an OWLObjectProperty store the name + i
			if (property instanceof OWLObjectProperty) {
				propertyToName.get(property)[i] = OntologyDescriptor.getCleanNameOWLObj(property)
						+ (i == 0 ? Settings.PROPERTY_0_DESIGNATOR : Settings.PROPERTY_1_DESIGNATOR);
			} else {
				// in this case property is an InverseObjectProperty
				// Therefore swap 0 and 1
				int j = i == 1 ? 0 : 1;
				// Recursive call to this method with the inverse property and j
				propertyToName.get(property)[i] = getPropertyVertex(property.getInverseProperty(), j);
			}
		}

		// Return the stored name for the property
		return propertyToName.get(property)[i];

	}

	/**
	 * A helper methode that wraps abot the addEdge method, but in case of that the
	 * edge already exists it returns the edge and not null
	 * 
	 * @param g
	 * @param vertex
	 * @param cleanNameOWLObj
	 * @return
	 */
	private DefaultEdge addEdgeHelp(Graph<String, DefaultEdge> g, String vertex, String vertex2) {
		// Add a edge between vertex and vertex2 if possible
		DefaultEdge edge = g.addEdge(vertex, vertex2);

		// If the edge already exists addEdge returns null. In this case
		// get the already existing edge
		if (edge == null) {
			edge = g.getEdge(vertex, vertex2);
		}

		// Save one of the vertexes to the edge
		edgeToVertex.put(edge, vertex);

		// And return it
		return edge;
	}

	/**
	 * Help method: If we want to label a axiom that doesn't belong to a specific
	 * edge
	 * 
	 * @param g2
	 * @param vertex
	 * @return
	 */
	private DefaultEdge createLoopEdge(Graph<String, DefaultEdge> g2, String vertex) {
		return addEdgeHelp(g2, vertex, vertex);
	}

	/**
	 * Connects a list of vertexes stepwise (so it connects the ith vertex with the
	 * i+1th vertex
	 * 
	 * @param vertList A list of vertexes
	 * @param ax
	 */
	private void connect_vertexes_stepwise_labelled(List<String> vertList, OWLAxiom ax) {
		if (vertList.isEmpty()) {
			throw new IllegalArgumentException("The vertices-list must be have atleast one element");
		}
		boolean labelled = false;
		if (vertList.size() > 1) {
			for (String vert1 : vertList) {
				for (String vert2 : vertList) {
					if (!vert1.equals(vert2)) {
						DefaultEdge edge = addEdgeHelp(g, vert1, vert2);
						// Save the label of this edge
						if (!labelled) {
							if (!edgeToAxioms.containsKey(edge)) {
								edgeToAxioms.put(edge, new HashSet<>());
							}
							edgeToAxioms.get(edge).add(ax);
							labelled = true;
						}
						// Save it creating axiom
						if (!createdByAxioms.containsKey(edge)) {
							createdByAxioms.put(edge, new HashSet<OWLAxiom>());
						}
						createdByAxioms.get(edge).add(ax);
					}
				}
			}
		} else {
			DefaultEdge edge = createLoopEdge(g, vertList.iterator().next());
			if (!edgeToAxioms.containsKey(edge)) {
				edgeToAxioms.put(edge, new HashSet<>());
			}
			edgeToAxioms.get(edge).add(ax);
		}

	}

	/**
	 * Connects a list of vertexes stepwise (so it connects the ith vertex with the
	 * i+1th vertex
	 * 
	 * @param vertList A list of vertexes
	 * @param ax
	 */
	private void connect_vertexes_stepwise(List<String> vertList, OWLAxiom ax) {
		if (vertList.size() > 1) {
			for (String vert1 : vertList) {
				for (String vert2 : vertList) {
					if (!vert1.equals(vert2)) {
						DefaultEdge edge = addEdgeHelp(g, vert1, vert2);
						// Save it creating axiom
						if (!createdByAxioms.containsKey(edge)) {
							createdByAxioms.put(edge, new HashSet<OWLAxiom>());
						}
						createdByAxioms.get(edge).add(ax);
					}
				}
			}
		}

	}

	/**
	 * Maps all annotation properties to all SubAnnotationPropertyOf,
	 * AnnotationPropertyRangeOf and AnnotationPropertyDomainOf Axioms containt the
	 * property
	 * 
	 * @param ontology
	 * @return Map from annotation properties to SubAnnotationPropertyOf,
	 *         AnnotationPropertyRangeOf and AnnotationPropertyDomainOf Axioms
	 */
	private Map<OWLAnnotationProperty, Set<OWLAxiom>> getAnnotationAxioms(OWLOntology ontology) {
		Map<OWLAnnotationProperty, Set<OWLAxiom>> toReturn = new HashMap<>();

		// For each annotation property...
		ontology.annotationPropertiesInSignature().forEach(aProp -> {
			// ... add all domain axioms ...
			ontology.annotationPropertyDomainAxioms(aProp).forEach(aDomain -> {
				if (!toReturn.containsKey(aProp)) {
					toReturn.put(aProp, new HashSet<>());
				}
				toReturn.get(aProp).add(aDomain);
			});
			// ... add all range axioms ...
			ontology.annotationPropertyRangeAxioms(aProp).forEach(aRange -> {
				if (!toReturn.containsKey(aProp)) {
					toReturn.put(aProp, new HashSet<>());
				}
				toReturn.get(aProp).add(aRange);
			});
			// ... and all SubAnnotationPropertyOf Axioms with the property as subproperty
			ontology.subAnnotationPropertyOfAxioms(aProp).forEach(subAnnoAx -> {
				if (!toReturn.containsKey(aProp)) {
					toReturn.put(aProp, new HashSet<>());
				}
				toReturn.get(aProp).add(subAnnoAx);
			});

			// Also save the declarations
			ontology.declarationAxioms(aProp).forEach(decAx -> {
				if (!toReturn.containsKey(aProp)) {
					toReturn.put(aProp, new HashSet<>());
				}
				toReturn.get(aProp).add(decAx);
			});

		});

		// Also get the SubAnnotationPropertyOf Axioms with the property as the
		// superproperty
		// For each annotation property...
		ontology.annotationPropertiesInSignature().forEach(aProp -> {
			ontology.subAnnotationPropertyOfAxioms(aProp).forEach(subAnnoAx -> {
				OWLAnnotationProperty superAnnoProp = subAnnoAx.getSuperProperty();
				if (!toReturn.containsKey(superAnnoProp)) {
					toReturn.put(superAnnoProp, new HashSet<>());
				}
				toReturn.get(superAnnoProp).add(subAnnoAx);
			});
		});

		return toReturn;
	}

	/**
	 * Creates an owl ontology out of a cc
	 * 
	 * @param cc
	 * @param cc
	 * @return
	 */
	private List<OWLOntology> createOntologyFromCC(List<Set<String>> cc, OWLOntology parentOntology) {
		// The list of ontologies to return
		List<OWLOntology> toReturn = new ArrayList<>();

		if (cc.size() > 1) {
			// Get a map of cc's to set of their axioms
			Map<String, Set<OWLAxiom>> ccToAxioms = getCCToAxioms(g, cc, edgeToAxioms, edgeToVertex);

			// Create a owl manager
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			// Save name of parent ontology
			String parentIRIString = null;
			Optional<IRI> parentIRI = parentOntology.getOntologyID().getOntologyIRI();
			if (parentIRI.isPresent()) {
				parentIRIString = parentIRI.get().getIRIString();
			}

			// A counterr for the number of the partition
			int partitionNum = 1;
			// For each axiomset
			for (Set<OWLAxiom> axs : ccToAxioms.values()) {
				try {
					// TODO: Give ontology names. How to handle econnection?
					OWLOntology ont = null;
					if (parentIRIString == null) {
						ont = manager.createOntology(axs);

					} else {
						ont = manager.createOntology(axs, IRI.create(parentIRIString + partitionNum + ".ow"));
					}
					toReturn.add(ont);
					partitionNum++;
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
			}
		} else {
			// If there is only one patition, just return the parent ontology
			toReturn.add(parentOntology);
		}

		return toReturn;
	}

	/**
	 * Given a List of connected components and a map mapping edges to sets of
	 * axioms return a map, that maps the cc to their axioms
	 * 
	 * @param connectedSets
	 * @param axiomToEdges
	 * @return Mapping from cc to axioms
	 */
	public static Map<String, Set<OWLAxiom>> getCCToAxioms(Graph<String, DefaultEdge> g,
			List<Set<String>> connectedSets, Map<DefaultEdge, Set<OWLAxiom>> edgeToAxiom,
			Map<DefaultEdge, String> edgeToVertex) {

		// Create the map to map cc to axioms
		Map<String, Set<OWLAxiom>> ccToAxioms = new HashMap<>();

		// Get a map from all vertices to the String representing the CC
		Map<String, String> vertexToCCString = getVertexToCCString(connectedSets);

		// For all Entries that Map a edge to a set of axioms
		for (Entry<DefaultEdge, Set<OWLAxiom>> e : edgeToAxiom.entrySet()) {
			// Get the name of the CC
			String ccName = vertexToCCString.get(edgeToVertex.get(e.getKey()));
			// And save all axioms of the edge to it
			if (!ccToAxioms.containsKey(ccName)) {
				ccToAxioms.put(ccName, new HashSet<>());
			}
			ccToAxioms.get(ccName).addAll(e.getValue());
		}

		return ccToAxioms;
	}

	/**
	 * Calculates a map mapping all vertices to the name of the cc
	 * 
	 * @param connectedSets
	 * @return
	 */
	private static Map<String, String> getVertexToCCString(List<Set<String>> connectedSets) {
		Map<String, String> toReturn = new HashMap<>();

		// This steps should be limited linearly by the number of vertices
		for (Set<String> cc : connectedSets) {
			String ccName = cc.toString() + "";
			for (String vert : cc) {
				toReturn.put(vert, ccName);
			}
		}

		return toReturn;
	}
}
