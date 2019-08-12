package uni.sasjonge.heuristics.communitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLAxiom;

import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.LeidenAlgorithm;
import cwts.networkanalysis.Network;

public class CommunityDetectionManager {
	
	int WEIGHT_FOR_NON_AXIOM_EDGES = 3;
	private boolean lazy_clustering = false;
	
	private Graph<String, DefaultEdge> graph;
	private Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms;
	private Network network;

	private LeidenAlgorithm leiden;

	private Clustering clustering;
	private Map<String, Integer> verticesToId;
	private Map<Integer, String> idToVertices;
	private List<Set<String>> vertexClusters;
	private Set<DefaultEdge> bridges = null;

	public CommunityDetectionManager(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> edgesToAxioms) {
		this.graph = g;
		this.edgeToAxioms = edgesToAxioms;
		this.network = translateGraphToLeidenNetwork();
	}
	
	public List<Set<String>> getClusters() {
		lazyClustering();
		
		return vertexClusters; 
		
	}
	
	public Set<DefaultEdge> getBridges() {
		lazyClustering();
		if(bridges == null) {
			bridges = new HashSet<>();
			for (DefaultEdge e : graph.edgeSet()) {
				int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
				int targetV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
				
				int[] clusters = clustering.getClusters();
				
				if (clusters[sourceV] != clusters[targetV]) {
					bridges.add(e);
				}
				
			}
		}
		return bridges;
	}
	
	public void startLeidenCommunityDetection() {
		this.leiden = new LeidenAlgorithm();
		this.clustering = leiden.findClustering(this.network);
	}
	
	private void lazyClustering() {
		if (!lazy_clustering) {
			// Calculate the clustering
			startLeidenCommunityDetection();
			lazy_clustering = true;
			
			// Save the clustering in a compatible format for out graphs
			int[] clusters = clustering.getClusters();
			int numOfClusters = clustering.getNClusters();
			
			vertexClusters = new ArrayList<Set<String>>(numOfClusters);
			
			for (int id = 0; id < clusters.length; id++) {
				vertexClusters.get(clusters[id]).add(idToVertices.get(id));
			}
		}
		
	}
	
	private Network translateGraphToLeidenNetwork() {
		initIDsForVertices();
		
		// Create the node weights
		double[] nodeWeights = new double[verticesToId.size()];
		Arrays.fill(nodeWeights, 1.0);
		
		// Create the edges and edge weights
		int[][] edges = new int[2][this.graph.edgeSet().size()];
		double[] edgeWeights = new double[this.graph.edgeSet().size()];
		int i = 0;
		for (DefaultEdge e : graph.edgeSet()) {
			int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
			int targetV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
			
			edges[0][i] = sourceV;
			edges[1][i] = targetV;
			
			if (edgeToAxioms.containsKey(e)) {
				edgeWeights[i] = 1;
			} else {
				edgeWeights[i] = WEIGHT_FOR_NON_AXIOM_EDGES;
			}
		}

		return new Network(nodeWeights, edges, edgeWeights, false, true);
	}

	private void initIDsForVertices() {
	
		int id = 0;
		for (String vertex : this.graph.vertexSet()) {
			verticesToId.put(vertex, id);
			idToVertices.put(id, vertex);
		}
		
	}
}
