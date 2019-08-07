package uni.sasjonge.heuristics.biconnectivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;

import uni.sasjonge.Settings;
import uni.sasjonge.utils.GraphRemovalUndo;
import uni.sasjonge.utils.OntologyDescriptor;

public class BiconnectivityManager {

	/**
	 * Removes all bridges according to the biconnectivtyinspector of jgrapht. The
	 * graphes need to be labelled by axioms and their removal shouldn't create a
	 * singleton partiton (a partition with only one vertex)
	 * 
	 * @param g
	 * @param edgeToAxioms
	 * @param createdByAxioms
	 * @return
	 */
	public static Graph<String, DefaultEdge> removeAxiomLabelledBridgesNoSingletons(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {

		// Get a map of axioms to their edges
		Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges = getAxiomToEdges(createdByAxioms);

		for (int i = 0; i < Settings.BH_NUM_OF_REPETITION_OF_HEURISTIC; i++) {
			System.out.println("----------------removal" + i + "------------------");
			// Create the BiconnectivityInspector and get all bridges
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);
			Set<DefaultEdge> bridgesOfThisStep = ci.getBridges();
			System.out.println(".." + bridgesOfThisStep.size());
			// Remove the bridges that have no axiom label
			bridgesOfThisStep.removeIf(p -> !edgeToAxioms.containsKey(p));
			System.out.println("??" + bridgesOfThisStep.size());

			// Save the new size
			// Remove all label with more than X number of axiom labels
			List<DefaultEdge> bridgesOfThisStepFiltered = bridgesOfThisStep.stream()
					.filter(p -> (edgeToAxioms.get(p).size() < Settings.BH_NUM_OF_AXIOM_LABELS))
					.collect(Collectors.toList());
			// Make sure, that at least some labels survive the filter
			int num_of_axiom_labels = Settings.BH_NUM_OF_AXIOM_LABELS;
			while (bridgesOfThisStepFiltered.isEmpty()) {
				num_of_axiom_labels++;
				final int filterNum = num_of_axiom_labels;
				bridgesOfThisStepFiltered = bridgesOfThisStep.stream()
						.filter(p -> (edgeToAxioms.get(p).size() < filterNum)).collect(Collectors.toList());
			}

			System.out.println("!!" + bridgesOfThisStepFiltered.size());
			// Remove it and all edges that where only created by the same edges
			removeAxiomEdgesOfNoSingleton(g, edgeToAxioms, axiomToEdges, bridgesOfThisStepFiltered.iterator());

			System.out.println("---------------------------------------------------");
		}

		return g;
	}

	/**
	 * Given a map of edges to axioms calculates the inverse map of axioms to edges
	 * 
	 * @param edgeToAxioms
	 * @return
	 */
	private static Map<OWLAxiom, Set<DefaultEdge>> getAxiomToEdges(Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms) {

		// The Map to Return
		Map<OWLAxiom, Set<DefaultEdge>> axiomsToEdges = new HashMap<>();

		// For each entry of the given map
		for (Entry<DefaultEdge, Set<OWLAxiom>> e : edgeToAxioms.entrySet()) {
			// For each axiom of this edge
			for (OWLAxiom ax : e.getValue()) {
				// Add the axiom and corresponding edge to the output 
				if (!axiomsToEdges.containsKey(ax)) {
					axiomsToEdges.put(ax, new HashSet<>());
				}
				axiomsToEdges.get(ax).add(e.getKey());
			}
		}

		return axiomsToEdges;
	}

	/**
	 * Removes all axiom labelled edges given by the iterator. If the removal will create
	 * a singleton component (containing only one vertex) the removal will be undone
	 * @param g
	 * @param edgeToAxioms
	 * @param axiomToEdges
	 * @param edgeIterator
	 * @return
	 */
	private static boolean removeAxiomEdgesOfNoSingleton(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges,
			Iterator<DefaultEdge> edgeIterator) {
		// A flag if atleast one axiom labelled edge was removed
		boolean removedAxiomEdge = false;

		// Create a connectivity inspector, to count the singletons before the removals
		ConnectivityInspector<String, DefaultEdge> ciOld = new ConnectivityInspector<>(g);
		int oldNumOfSingletons = 0;
		for (Set<String> cc : ciOld.connectedSets()) {
			if (cc.size() < 2) {
				oldNumOfSingletons++;
			}
		}
		
		// Counter for the singletons after the removal
		int newNumOfSingletons = 0;

		// For each edge in the iterator
		while (edgeIterator.hasNext()) {
			// Remove the edge
			GraphRemovalUndo undoer = removeAxiomEdgesOf(g, edgeToAxioms, axiomToEdges, edgeIterator.next());

			// Count the number of singletons
			ConnectivityInspector<String, DefaultEdge> ciNew = new ConnectivityInspector<>(g);
			for (Set<String> cc : ciNew.connectedSets()) {
				if (cc.size() < 2) {
					newNumOfSingletons++;
				}
			}

			// If there are more singletons than before undo the removal
			if (oldNumOfSingletons != newNumOfSingletons) {
				undoer.undo();
			} else {
				// Else print the removed axiom
				for (OWLAxiom ax : undoer.getAxiom()) {
					System.out.println(OntologyDescriptor.getCleanNameOWLObj(ax));
				}
				removedAxiomEdge = true;
			}
			// Reset the counter
			newNumOfSingletons = 0;
		}

		return removedAxiomEdge;
	}

	/**
	 * Remove edge, and all other edges that came from the same axiom as the edge
	 * 
	 * @param g
	 * @param edgeToAxioms
	 * @param axiomToEdges
	 * @param next
	 */
	private static GraphRemovalUndo removeAxiomEdgesOf(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges,
			DefaultEdge edge) {
		if (!edgeToAxioms.containsKey(edge)) {
			throw new IllegalArgumentException("The edge must be a axiom edge");
		}
		
		// Instantiate a graphremoval class (which allows to undo the removal9
		GraphRemovalUndo remover = new GraphRemovalUndo(g);

		// For each axiom of the edge
		for (OWLAxiom ax : edgeToAxioms.get(edge)) {
			// For each edge that was created by this axiom
			for (DefaultEdge e : axiomToEdges.get(ax)) {
				// remove the axiom from the label
				edgeToAxioms.get(e).remove(ax);
				// If this was the last axiom of the edge, remove it
				if (edgeToAxioms.get(e).size() < 1) {
					edgeToAxioms.remove(e);
					remover.removeEdge(e);
				}
			}
			// Remove all nested class expressions of the axiom
			ax.nestedClassExpressions().forEach(nested -> {
				remover.removeVertex(OntologyDescriptor.getCleanNameOWLObj(nested));
			});
			
			// Remember which axioms where removed
			remover.saveAxiom(ax);
		}

		return remover;

	}

}
