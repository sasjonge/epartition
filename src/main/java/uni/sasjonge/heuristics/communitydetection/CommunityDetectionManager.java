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
import uni.sasjonge.heuristics.util.AxiomLabelledBridgesRemover;

public class CommunityDetectionManager extends AxiomLabelledBridgesRemover {
	
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

	public CommunityDetectionManager(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms) {
		this.graph = g;
		this.edgeToAxioms = edgeToAxioms;
		this.network = translateGraphToLeidenNetwork();
		System.out.println("!!! The network has " + network.getEdges().length + " edges");
	}
	
	public void startLeidenCommunityDetection() {
		this.leiden = new LeidenAlgorithm();
		
		this.leiden.setResolution(0);
		this.leiden.setNIterations(100);
		this.clustering = leiden.findClustering(this.network);
	}

	public List<Set<String>> getClusters() {
		lazyClustering();
		
		return vertexClusters; 
	}
	
	public Set<DefaultEdge> getBridges() {
		lazyClustering();
		if(bridges == null) {
			int[] clusters = clustering.getClusters();

			bridges = new HashSet<>();
			for (DefaultEdge e : graph.edgeSet()) {
				int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
				int targetV = verticesToId.get(this.graph.getEdgeTarget(e)).intValue();
								
				if (clusters[sourceV] != clusters[targetV]) {
					bridges.add(e);
				}
				
			}
		}
		return bridges;
	}
	
	public Graph<String, DefaultEdge> removeBridges(Map<DefaultEdge, Set<OWLAxiom>> edgeToAxioms, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {
		// Get a map of axioms to their edges
		System.out.println("There are " + getBridges().size() + "bridges");
		Map<OWLAxiom, Set<DefaultEdge>> axiomToEdges = getAxiomToEdges(createdByAxioms);
		removeAxiomEdgesOfNoSingleton(graph, edgeToAxioms, axiomToEdges, getBridges().iterator());
		return graph;
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

	private void lazyClustering() {
		if (!lazy_clustering) {
			// Calculate the clustering
			startLeidenCommunityDetection();
			lazy_clustering = true;
			
			// Save the clustering in a compatible format for out graphs
			int[] clusters = clustering.getClusters();
			int numOfClusters = clustering.getNClusters();
						
			vertexClusters = new ArrayList<Set<String>>(Collections.nCopies(numOfClusters, null));
			
			for (int id = 0; id < idToVertices.size(); id++) {
				if (vertexClusters.get(clusters[id]) == null) {
					vertexClusters.add(clusters[id],new HashSet<>());
				}
				vertexClusters.get(clusters[id]).add(idToVertices.get(id));
			}
		}
		
	}

	private void initIDsForVertices() {
		verticesToId = new HashMap<>();
		idToVertices = new HashMap<>();
		int id = 0;
		for (String vertex : this.graph.vertexSet()) {
			verticesToId.put(vertex, id);
			idToVertices.put(id, vertex);
			id++;
		}
		
	}
}
