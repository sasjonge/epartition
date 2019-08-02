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

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;

import uni.sasjonge.utils.OntologyDescriptor;

public class BiconnectivityManager {

	/**
	 * Remove bridges that are labelled with axioms. 
	 * If a bridge with the axiom a is removed, all other axiom edges
	 * that where only created by a are removed 
	 *
	 * @param g
	 * @param labellingVertexes
	 * @param edgeToAxioms
	 * @param createdByAxioms 
	 * @return
	 */
	public static Graph<String, DefaultEdge> removeAxiomLabelledBridges(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {
		
		long startTime = System.nanoTime();

		Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges = getAxiomToEdges(createdByAxioms);
		
		long endTime = System.nanoTime();
		System.out.println("Inversing edgeToAxioms took " + (endTime - startTime)/1000000 + "ms");	

		for (int i = 0; i < 1; i++) {
			System.out.println("----------------removal" + i + "------------------");
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);

			Iterator<DefaultEdge> iter = ci.getBridges().iterator();
			while (iter.hasNext()) {
				DefaultEdge next = iter.next();
				// If the edge is labelled
				if (edgeToAxioms.containsKey(next)) {
					System.out.println("removed " + OntologyDescriptor.getCleanName(next.toString()));
					// Remove it and all edges that where only created by the same edges
					removeAxiomEdgesOf(g, edgeToAxioms, axiomToEdges, next);
				}
			}
			System.out.println("---------------------------------------------------");
		}

		return g;
	}
	
	public static Graph<String, DefaultEdge> removeAxiomLabelledBridgesNew(Graph<String, DefaultEdge> g,
			Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {

		long startTime = System.nanoTime();

		Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges = getAxiomToEdges(createdByAxioms);

		long endTime = System.nanoTime();
		System.out.println("Inversing edgeToAxioms took " + (endTime - startTime) / 1000000 + "ms");

		for (int i = 0; i < 3; i++) {
			System.out.println("----------------removal" + i + "------------------");
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);
			System.out.println("THERE ARE " + ci.getConnectedComponents().size() + " CC's");
			List<DefaultEdge> bridgesOfThisStep = new ArrayList<>(ci.getBridges().size());
			bridgesOfThisStep.addAll(ci.getBridges());
			bridgesOfThisStep.removeIf(p -> !edgeToAxioms.containsKey(p));
			bridgesOfThisStep.sort(new Comparator<DefaultEdge>() {

				@Override
				public int compare(DefaultEdge o1, DefaultEdge o2) {
					int edge1Num = edgeToAxioms.get(o1).size();
					int edge2Num = edgeToAxioms.get(o2).size();
					return edge1Num - edge2Num;
				}
			});

			DefaultEdge next = bridgesOfThisStep.iterator().next();
			// If the edge is labelled
			if (edgeToAxioms.containsKey(next)) {
				System.out.println("removed " + OntologyDescriptor.getCleanName(next.toString()));
				// Remove it and all edges that where only created by the same edges
				removeAxiomEdgesOf(g, edgeToAxioms, axiomToEdges, next);
			}

			System.out.println("---------------------------------------------------");
		}

		return g;
	}

	private static Map<OWLAxiom, Set<DefaultEdge>> getAxiomToEdges(Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms) {
		
		Map<OWLAxiom, Set<DefaultEdge>> axiomsToEdges = new HashMap<>();
		
		for (Entry<DefaultEdge, Set<OWLAxiom>> e : edgeToAxioms.entrySet()) {
			for (OWLAxiom ax : e.getValue()) {
				if (!axiomsToEdges.containsKey(ax)) {
					axiomsToEdges.put(ax, new HashSet<>());
				}
				axiomsToEdges.get(ax).add(e.getKey());
			}
		}
		
		return axiomsToEdges;
	}

	/**
	 * Remove edge, and all other edges that came from the same axiom as the edge
	 * @param g
	 * @param edgeToAxioms
	 * @param axiomToEdges 
	 * @param next
	 */
	private static void removeAxiomEdgesOf(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms,
			Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges, DefaultEdge edge) {
		if (!edgeToAxioms.containsKey(edge)) {
			throw new IllegalArgumentException("The edge must be a axiom edge");
		}
				
		for (OWLAxiom ax : edgeToAxioms.get(edge)) {
			for(DefaultEdge e : axiomToEdges.get(ax)) {
				edgeToAxioms.get(e).remove(ax);
				if (edgeToAxioms.get(e).size() < 1) {
					edgeToAxioms.remove(e);
					g.removeEdge(e);
					if (e.equals(edge)) {
					}
				}
			}
		}
		
	}

}
