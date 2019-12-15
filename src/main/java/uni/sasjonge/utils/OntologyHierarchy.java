package uni.sasjonge.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

/**
 * This class creates maps to save the hierachy structure of the ontology
 * 
 * @author sascha
 */
public class OntologyHierarchy {

	// Save the depth of the classes and properties in the ontology
	Map<Integer, Set<OWLClass>> depthToClasses = new HashMap<>();
	Map<Integer, Set<OWLObjectProperty>> depthToProperties = new HashMap<>();

	// Map classes and properties to their superclass or parent
	Map<OWLClass, OWLClass> classToParent = new HashMap<>();
	Map<OWLObjectProperty, OWLObjectProperty> propertyToParent = new HashMap<>();

	// same as above, but using their cleaned names
	Map<String, String> classToParentString = new HashMap<>();
	Map<String, String> propertyToParentString = new HashMap<>();

	// reverse of the above map
	Map<String, Set<String>> parentToClassesString = new HashMap<>();
	Map<String, Set<String>> parentToPropertiesString = new HashMap<>();

	boolean somethingChanged = false;

	OWLReasonerFactory reasonerFactory;
	OWLReasoner reasoner;
	OWLDataFactory df;

	public OntologyHierarchy(OWLOntology ontology) throws OWLOntologyCreationException {

		// Create a manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology copydOnt = manager.copyOntology(ontology, OntologyCopy.DEEP);

		// Create a structural reasoner
		reasonerFactory = new StructuralReasonerFactory();
		reasoner = reasonerFactory.createNonBufferingReasoner(copydOnt);
		df = manager.getOWLDataFactory();

		// Init the depth structure for classes
		initDepthToClass(manager, copydOnt, 0);

		// Init the depth structure for properties
		initDepthToRoles(manager, copydOnt, 0);

		// Reverse the string maps classToParentString and propertyToParentString
		parentToClassesString = collectStringMapByValue(classToParentString);
		parentToPropertiesString = collectStringMapByValue(propertyToParentString);

	}

	/**
	 * Init the depth structure for classes
	 * 
	 * @param manager
	 * @param ont
	 * @param depth
	 */
	private void initDepthToClass(OWLOntologyManager manager, OWLOntology ont, int depth) {

		// Are there lower "level"
		boolean areThereLower = true;

		// Save the current top classes (subclasses of top)
		Set<OWLClass> classes = reasoner.getSubClasses(df.getOWLThing(), true).entities().collect(Collectors.toSet());

		// as long as there are lower level
		while (areThereLower) {

			// Save this new subclasses as the classes for this depth
			depthToClasses.put(depth, classes);

			// Create a List for the next level
			Set<OWLClass> newClasses = new HashSet<>();

			// For all classes of the current level
			for (OWLClass cls : classes) {

				// Get all subclasses
				List<OWLClass> subForCls = reasoner.getSubClasses(cls, true).entities().collect(Collectors.toList());

				// Add them to the next level
				newClasses.addAll(subForCls);

				// save parent structure
				subForCls.stream().forEach(subCls -> {
					classToParent.put(subCls, cls);
					classToParentString.put(OntologyDescriptor.getCleanNameOWLObj(subCls),
							OntologyDescriptor.getCleanNameOWLObj(cls));
				});

			}

			// Set the new level as the current level
			classes = newClasses;

			// Next level index
			depth++;

			// If there are less than 1 new class stop this loop
			if (newClasses.size() < 1) {
				areThereLower = false;
			}

		}
	}

	/**
	 * Init the depth structure for properties
	 * 
	 * @param manager
	 * @param ont
	 * @param depth
	 */
	private void initDepthToRoles(OWLOntologyManager manager, OWLOntology ont, int depth) {

		// save parent role structure
		// For each property r
		ont.objectPropertiesInSignature().forEach(property -> {
			// If they aren't inverses
			if (!property.getInverseProperty().isOWLObjectProperty()) {
				// Get the sub properties of r. For each of these sub properties s
				reasoner.getSubObjectProperties(property, true).entities().forEach(subProp -> {
					// If they aren't inverses
					if (!subProp.getInverseProperty().isOWLObjectProperty()) {
						// Get r and s as OWLObjectProperty
						OWLObjectProperty subPropAsProp = subProp.asOWLObjectProperty();
						OWLObjectProperty propAsProp = property.asOWLObjectProperty();
						// Save them in the map propertyToParent by using s as key and r as value
						propertyToParent.put(subPropAsProp, propAsProp);
						// Do the same, but with their cleaned names
						propertyToParentString.put(OntologyDescriptor.getCleanNameOWLObj(subPropAsProp),
								OntologyDescriptor.getCleanNameOWLObj(propAsProp));
					}
				});

			}
		});

		// Get top properties
		Set<OWLObjectPropertyExpression> properties = reasoner
				.getSubObjectProperties(df.getOWLTopObjectProperty(), true).entities().collect(Collectors.toSet());

		// While there are lower level properties
		boolean isThereLowerLevel = true;
		while (isThereLowerLevel) {

			// Set for the current level properties as OWLObjectProperty
			Set<OWLObjectProperty> propertiesToSave = new HashSet<>();

			// For all current level properties
			properties.stream().forEach(e -> {
				if (!e.isOWLBottomObjectProperty() && !e.getInverseProperty().isOWLObjectProperty()) {
					// Save them as OWLObjectProperty, if they are not the BottomProperty or a Inverse
					propertiesToSave.add(e.asOWLObjectProperty());
				}
			});

			// Map the current depth to the saved properties
			depthToProperties.put(depth, propertiesToSave);

			// Set for the next level properties
			Set<OWLObjectPropertyExpression> nextLevelProperties = new HashSet<>();

			// Go through all current level properties
			for (OWLObjectPropertyExpression expr : properties) {
				// and save their sub properties to the Set of next level properties
				nextLevelProperties
						.addAll(reasoner.getSubObjectProperties(expr, true).entities().collect(Collectors.toSet()));
			}

			// Set the next level properties to the current level properties
			properties = nextLevelProperties;

			// Increase the depth
			depth++;

			// If properties is empty (so there are no properties at this level) stop
			if (properties.isEmpty()) {
				isThereLowerLevel = false;
			}
		}

	}

	/**
	 * Reverses the key and values of a Map with String keys and values
	 * 
	 * @param aToB A Map from string to string
	 * @return The reversed map
	 */
	public static Map<String, Set<String>> collectStringMapByValue(Map<String, String> aToB) {
		// The reversed list to return
		Map<String, Set<String>> bToAList = new HashMap<>();

		// Fore every entry in the input map
		for (Entry<String, String> entry : aToB.entrySet()) {
			// Get the value
			String b = entry.getValue();
			// Check if the value is already a key.
			if (!bToAList.containsKey(b)) {
				// If not add it as a key to a hashset (there can be more than one key for this
				// value)
				bToAList.put(b, new HashSet<>());
			}
			// Add the key to the set
			bToAList.get(b).add(entry.getKey());
		}

		return bToAList;
	}

	// ------------------- Simple Getters ---------------------------
	public Set<OWLClass> getClassesOfDepth(int depth) {
		return depthToClasses.get(depth);
	}

	public Set<OWLObjectProperty> getPropertiesOfDepth(int depth) {
		return depthToProperties.get(depth);
	}

	public int getHighestPropertyDepth() {
		return depthToProperties.size() - 1;
	}

	public int getHighestClassDepth() {
		return depthToClasses.size() - 1;
	}

	public OWLClass getOWLClassParent(OWLClass cls) {
		return classToParent.get(cls);
	}

	public OWLObjectProperty getOWLPropertyParent(OWLObjectProperty cls) {
		return propertyToParent.get(cls);
	}

	public Map<String, String> getClassToParentString() {
		return classToParentString;
	}

	public Map<String, String> getPropertyToParentString() {
		return propertyToParentString;
	}

	public Map<String, Set<String>> getParentToClassesString() {
		return parentToClassesString;
	}

	public Map<String, Set<String>> getParentToPropertiesString() {
		return parentToPropertiesString;
	}

}
