package uni.sasjonge.heuristics.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;

import uni.sasjonge.utils.GraphRemovalUndo;
import uni.sasjonge.utils.OntologyDescriptor;

public abstract class AxiomLabelledBridgesRemover {

	/**
	 * Removes all axiom labelled edges given by the iterator. If the removal will create
	 * a singleton component (containing only one vertex) the removal will be undone
	 * @param g
	 * @param edgeToAxioms
	 * @param axiomToEdges
	 * @param edgeIterator
	 * @return
	 */
	protected boolean removeAxiomEdgesOfNoSingleton(Graph<String, DefaultEdge> g,
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
	private GraphRemovalUndo removeAxiomEdgesOf(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges,
			DefaultEdge edge) {
		
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
	
	/**
	 * Given a map of edges to axioms calculates the inverse map of axioms to edges
	 * 
	 * @param edgeToAxioms
	 * @return
	 */
	protected Map<OWLAxiom, Set<DefaultEdge>> getAxiomToEdges(Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms) {

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
}
