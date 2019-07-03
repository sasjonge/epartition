package uni.sasjonge.heuristics.biconnectivity;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

public class BiconnectivityManager {

	public static Graph<String, DefaultEdge> removeConnectingVertexes(Graph<String, DefaultEdge> g, Set<String> classes) {

		for (int i = 0; i <= 3; i++) {
			System.out.println("----------------removal"+i+"------------------");
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);

			Iterator<String> iter = ci.getCutpoints().iterator();
			while (iter.hasNext()) {
				String next = iter.next();
				if (classes.contains(next)) {
					System.out.println("removed " + next);
					System.out.println("Yeah");
					g.removeVertex(next);
				}
			}	
			System.out.println("---------------------------------------------------");
		}


		return g;
	}
	
	public static Graph<String, DefaultEdge> removeConnectingVertexes2(Graph<String, DefaultEdge> g, Set<String> classes) {

		for (int i = 0; i <= 3; i++) {
			System.out.println("----------------removal"+i+"------------------");
			BiconnectivityInspector<String, DefaultEdge> ci = new BiconnectivityInspector<>(g);

			Iterator<String> iter = ci.getCutpoints().iterator();
			while (iter.hasNext()) {
				String next = iter.next();
				if (classes.contains(next)) {
					System.out.println("removed " + next);
					System.out.println("Yeah");
					g.removeVertex(next);
				}
			}	
			System.out.println("---------------------------------------------------");
		}


		return g;
	}
}
