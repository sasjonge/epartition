package uni.sasjonge.heuristics.biconnectivity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;

public class BiconnectivityManager {

	public static Graph<String, DefaultEdge> removeClassCutVertexes(Graph<String, DefaultEdge> g,
			Set<String> labellingVertexes, Set<String> classes) {

		for (int i = 0; i < 1; i++) {
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);

			Iterator<String> iter = ci.getCutpoints().iterator();
			while (iter.hasNext()) {
				String next = iter.next();
				if (classes.contains(next)) {
					System.out.println("removed " + next);
					removeVertexIfNoUnlabelledCC(g, labellingVertexes, next);
				}
			}
			System.out.println("---------------------------------------------------");
		}

		return g;
	}

	public static Graph<String, DefaultEdge> removeAxiomCutVertexes(Graph<String, DefaultEdge> g,
			Set<String> labellingVertexes, Map<String, Set<OWLAxiom>> vertexToAxiom) {

		for (int i = 0; i < 1; i++) {
			System.out.println("----------------removal" + i + "------------------");
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);

			Iterator<String> iter = ci.getCutpoints().iterator();
			while (iter.hasNext()) {
				String next = iter.next();
				if (vertexToAxiom.containsKey(next)) {
					System.out.println("removed " + next);
					removeVertexIfNoUnlabelledCC(g, labellingVertexes, next);
				}
			}
			System.out.println("---------------------------------------------------");
		}

		return g;
	}

	/**
	 * Remove the vertex labelled with next from the graph g, only if the removal will
	 * not create a component without a viable label, like class or individual
	 * 
	 * @param g
	 * @param labellingVertexes
	 * @param next
	 */
	private static void removeVertexIfNoUnlabelledCC(Graph<String, DefaultEdge> g, Set<String> labellingVertexes,
			String next) {

		ConnectivityInspector<String, DefaultEdge> ciOld = new ConnectivityInspector<>(g);

		int numberOfSmallComponents = 0;

		for (Set<String> cc : ciOld.connectedSets()) {
			boolean smallComponent = true;
			for (String v : cc) {
				if (labellingVertexes.contains(v)) {
					smallComponent = false;
					break;
				}
			}
			if (smallComponent) {
				numberOfSmallComponents++;
			}
		}
		;

		// Save edges for the possibilty to restor the graph
		Set<DefaultEdge> de = g.edgesOf(next);
		Set<String> sources = new HashSet<>();
		Set<String> targets = new HashSet<>();
		for (DefaultEdge e : de) {
			if (!g.getEdgeSource(e).equals(next)) {
				sources.add(g.getEdgeSource(e));
			}
			if (!g.getEdgeTarget(e).equals(next)) {
				targets.add(g.getEdgeTarget(e));
			}

		}

		// Remove the vertex
		g.removeVertex(next);

		// Test if the are new small components
		int newNumberOfSmallComponents = 0;

		ConnectivityInspector<String, DefaultEdge> ciNew = new ConnectivityInspector<>(g);
		for (Set<String> cc : ciNew.connectedSets()) {
			boolean smallComponent = true;
			for (String v : cc) {
				if (labellingVertexes.contains(v)) {
					smallComponent = false;
					break;
				}
			}
			if (smallComponent) {
				newNumberOfSmallComponents++;
			}
		}
		;

		if (newNumberOfSmallComponents > numberOfSmallComponents) {
			// System.out.println("Triggered!!!!!!!" + newNumberOfSmallComponents + "//df" +
			// numberOfSmallComponents);
			// Undo the removing
			g.addVertex(next);
			for (String source : sources) {
				g.addEdge(source, next);
			}
			for (String target : targets) {
				g.addEdge(next, target);
			}
		} 

	}

}
