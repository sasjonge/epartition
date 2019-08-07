package uni.sasjonge.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Represents a removal of edges and vertices,and helps to undo it
 * @author sascha
 *
 */
public class GraphRemovalUndo {
	
	private Graph<String, DefaultEdge> graph;
	
	private Set<String> removedVertices = new HashSet<>();
	Map<String, String> removedEdgesMap = new HashMap<>();
	
	private Set<OWLAxiom> axs = new HashSet<>();

	public GraphRemovalUndo(Graph<String, DefaultEdge> g) {
		this.graph = g;
	}
	
	/**
	 * Remove the vertex from the graph
	 * 
	 * @param vert
	 */
	public void removeVertex(String vert) {
		graph.removeVertex(vert);
		removedVertices.add(vert);
	}
	
	/**
	 * Remove the edge from the graph
	 * @param edge
	 */
	public void removeEdge(DefaultEdge edge) {
		removedEdgesMap.put(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
		graph.removeEdge(edge);
		
	}
	
	/**
	 * Undo the removals done with this class
	 */
	public void undo() {
		for (String vert : removedVertices) {
			graph.addVertex(vert);
		}
		for (Entry<String, String> ent : removedEdgesMap.entrySet()) {
			graph.addEdge(ent.getKey(), ent.getValue());
		}
		removedVertices.clear();
		removedEdgesMap.clear();
	}

	// Helper methods to save the removed axioms
	public void saveAxiom(OWLAxiom ax) {
		axs.add(ax);
		
	}
	
	public Set<OWLAxiom> getAxiom() {
		return axs;
	}
	
	public void clearAxioms() {
		axs.clear();
	}

}
