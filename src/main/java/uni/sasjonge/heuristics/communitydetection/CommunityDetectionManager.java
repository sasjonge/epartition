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
import uni.sasjonge.Settings;
import uni.sasjonge.heuristics.util.AxiomLabelledBridgesRemover;

public class CommunityDetectionManager extends AxiomLabelledBridgesRemover {

	private boolean lazy_clustering = false;

	private Graph<String, DefaultEdge> graph;
	private Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms;
	private Network network;

	private LeidenAlgorithm leiden;

	private Clustering clustering;
	private Map<String, Integer> verticesToId;
	private Map<Integer, String> idToVertices;
	private List<Set<String>> vertexClusters;
	private Set<DefaultEdge> bridges = null;

	private double resolutionForLeiden = Settings.RESOLUTION_AT_START;

	public CommunityDetectionManager(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {
		this.graph = g;
		this.createdByAxioms = createdByAxioms;
		this.network = translateGraphToLeidenNetwork();
	}

	public void startLeidenCommunityDetection() {
		this.leiden = new LeidenAlgorithm();
		this.leiden.setNIterations(10);
		this.leiden.setResolution(resolutionForLeiden);
		this.clustering = this.leiden.findClustering(this.network);

		// Save the clustering in a compatible format for out graphs
		int[] clusters = clustering.getClusters();
		int numOfClusters = clustering.getNClusters();

		vertexClusters = new ArrayList<Set<String>>(Collections.nCopies(numOfClusters, null));

		for (int id = 0; id < idToVertices.size(); id++) {
			if (vertexClusters.get(clusters[id]) == null) {
				vertexClusters.add(clusters[id], new HashSet<>());
			}
			vertexClusters.get(clusters[id]).add(idToVertices.get(id));
		}
		
		if (bridges == null) {
			bridges = new HashSet<>();
		} else {
			bridges.clear();
		}
		for (DefaultEdge e : graph.edgeSet()) {
			int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
			int targetV = verticesToId.get(this.graph.getEdgeTarget(e)).intValue();

			if (clusters[sourceV] != clusters[targetV]) {
				bridges.add(e);
			}

		}
	}

	public List<Set<String>> getClusters() {
		lazyClustering();

		return vertexClusters;
	}

	public Set<DefaultEdge> getBridges() {
		lazyClustering();
		return bridges;
	}

	public Graph<String, DefaultEdge> removeBridges(Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms,
			Map<DefaultEdge, Set<OWLAxiom>> labels) {
		Set<DefaultEdge> bridgeSet = getBridges();
		while (!bridgeSet.isEmpty() && !containsOnlyAxiomEdges(bridgeSet, createdByAxioms)) {
			resolutionForLeiden = resolutionForLeiden * Settings.RESOLUTION_DECREASE;
			translateGraphToLeidenNetwork();
			startLeidenCommunityDetection();
			System.out.println("Resolution: " + resolutionForLeiden + "###There are " + bridgeSet.size() + " bridges" + "### There are " + vertexClusters.size() + " Clusters");
		}
		removeAxiomEdgesOfNoSingleton(graph, createdByAxioms, labels, getBridges().iterator());
		return graph;
	}

	private boolean containsOnlyAxiomEdges(Set<DefaultEdge> bridgeSet,
			Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {
		boolean toReturn = true;
		for (DefaultEdge e : bridgeSet) {
			if (!createdByAxioms.containsKey(e)) {
				toReturn = false;
				break;
			}
		}
		return toReturn;
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
			int targetV = verticesToId.get(this.graph.getEdgeTarget(e)).intValue();

			edges[0][i] = sourceV;
			edges[1][i] = targetV;

			if (createdByAxioms.containsKey(e)) {
				edgeWeights[i] = 1;
			} else {
				edgeWeights[i] = Settings.WEIGHT_FOR_NON_AXIOM_EDGES;
			}
			i++;
		}

		return new Network(nodeWeights, edges, edgeWeights, false, true);
	}

	private void lazyClustering() {
		if (!lazy_clustering) {
			// Calculate the clustering
			startLeidenCommunityDetection();
			lazy_clustering = true;
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
