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
import uni.sasjonge.heuristics.util.AxiomCreatedBridgesRemoverHeuristic;
import uni.sasjonge.utils.GraphRemovalUndo;
import uni.sasjonge.utils.OntologyDescriptor;

public class BiconnectivityManager extends AxiomCreatedBridgesRemoverHeuristic {

	/**
	 * Removes all bridges according to the biconnectivtyinspector of jgrapht. The
	 * graphes need to be labelled by axioms and their removal shouldn't create a
	 * singleton partiton (a partition with only one vertex)
	 * 
	 * @param g
	 * @param labels
	 * @param createdByAxioms
	 * @return
	 */
	public Graph<String, DefaultEdge> removeAxiomLabelledBridgesNoSingletons(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> labels, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {

		for (int i = 0; i < Settings.BH_NUM_OF_REPETITION_OF_HEURISTIC; i++) {
			System.out.println("----------------removal" + i + "------------------");
			// Create the BiconnectivityInspector and get all bridges
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);
			Set<DefaultEdge> bridgesOfThisStep = ci.getBridges();
			// Remove the bridges that weren't created by axioms
			bridgesOfThisStep.removeIf(p -> !createdByAxioms.containsKey(p));

			// Remove all edges with more than X number of axioms that created them (if possible
			// we want to remove only edges with X labels, to reduce the overall
			// removal of axioms)
			List<DefaultEdge> bridgesOfThisStepFiltered = bridgesOfThisStep.stream()
					.filter(p -> (createdByAxioms.get(p).size() < Settings.BH_NUM_OF_AXIOM_LABELS))
					.collect(Collectors.toList());
			// Make sure, that at least some edges survive the filter
			int num_of_axiom_labels = Settings.BH_NUM_OF_AXIOM_LABELS;
			while (bridgesOfThisStepFiltered.isEmpty()) {
				num_of_axiom_labels++;
				final int filterNum = num_of_axiom_labels;
				bridgesOfThisStepFiltered = bridgesOfThisStep.stream()
						.filter(p -> (createdByAxioms.get(p).size() < filterNum)).collect(Collectors.toList());
			}

			// Remove it and all edges that where only created by the same axioms
			this.removeAxiomEdgesOfNoSingleton(g, createdByAxioms, labels, bridgesOfThisStepFiltered.iterator());

			System.out.println("---------------------------------------------------");
		}

		return g;
	}

}
