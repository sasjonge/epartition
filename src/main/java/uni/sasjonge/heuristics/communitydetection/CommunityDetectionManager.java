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

/**
 * Manages the translation and usage of memory detection algorithm
 * 
 * @author Sascha Jongebloed
 */
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

	/**
	 * Calculate the network
	 * 
	 * @param g
	 * @param createdByAxioms
	 */
	public CommunityDetectionManager(Graph<String, DefaultEdge> g, Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms) {
		this.graph = g;
		this.createdByAxioms = createdByAxioms;
		this.network = translateGraphToLeidenNetwork();
	}

	/**
	 * Starts the leiden algorithm on the network
	 */
	public void startLeidenCommunityDetection() {
		// Get the leiden Algorithm, and set it's number of iterations
		// and the resultion
		this.leiden = new LeidenAlgorithm();
		this.leiden.setNIterations(10);
		this.leiden.setResolution(resolutionForLeiden);
		// Start the leiden algorithm to find the cluster
		this.clustering = this.leiden.findClustering(this.network);

		// Save the clustering in a compatible format for out graphs
		int[] clusters = clustering.getClusters();
		int numOfClusters = clustering.getNClusters();

		// Create a List of the clusters
		vertexClusters = new ArrayList<Set<String>>(Collections.nCopies(numOfClusters, null));
		for (int id = 0; id < idToVertices.size(); id++) {
			if (vertexClusters.get(clusters[id]) == null) {
				vertexClusters.add(clusters[id], new HashSet<>());
			}
			vertexClusters.get(clusters[id]).add(idToVertices.get(id));
		}
		
		// Reset bridges
		if (bridges == null) {
			bridges = new HashSet<>();
		} else {
			bridges.clear();
		}
		// Calculate bridges by going trouhg all edges
		for (DefaultEdge e : graph.edgeSet()) {
			// Get the source and target of the edge
			int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
			int targetV = verticesToId.get(this.graph.getEdgeTarget(e)).intValue();

			// And if the source and target of the edge are in different clusters
			if (clusters[sourceV] != clusters[targetV]) {
				// Add the edge as a bridge
				bridges.add(e);
			}

		}
	}

	/**
	 * Returns the clusters calculated by the leiden algorithm
	 * @return
	 */
	public List<Set<String>> getClusters() {
		lazyClustering();
		return vertexClusters;
	}

	/**
	 * Returns the bridges between the clusters calculated by the leiden algorithm
	 * 
	 * @return Bridges
	 */
	public Set<DefaultEdge> getBridges() {
		lazyClustering();
		return bridges;
	}

	/**
	 * Removes the bridges that where calculated by the Leiden algorithm
	 * 
	 * @param createdByAxioms Map from Edges to the Axioms they where created by
	 * @param labels Map of edge labels
	 * @return New graph
	 */
	public Graph<String, DefaultEdge> removeBridges(Map<DefaultEdge, Set<OWLAxiom>> createdByAxioms,
			Map<DefaultEdge, Set<OWLAxiom>> labels) {
		//Get the bridges between the clusters
		Set<DefaultEdge> bridgeSet = getBridges();
		// if the bridges contain bridges that weren't created by axioms (and bridgeSet is not empty)
		while (!bridgeSet.isEmpty() && !containsOnlyAxiomEdges(bridgeSet, createdByAxioms)) {
			// Decrease the resolution for leiden
			resolutionForLeiden = resolutionForLeiden * Settings.RESOLUTION_DECREASE;
			// Restart the translation and leiden
			translateGraphToLeidenNetwork();
			startLeidenCommunityDetection();
			System.out.println("Resolution: " + resolutionForLeiden + "###There are " + bridgeSet.size() + " bridges" + "### There are " + vertexClusters.size() + " Clusters");
		}
		// Remove the choosen bridges between the cluster
		removeAxiomEdgesOfNoSingleton(graph, createdByAxioms, labels, getBridges().iterator());
		return graph;
	}

	/**
	 * Helper method that calculates if birdgeSet does only contain edges
	 * the where created by axioms
	 * 
	 * @param bridgeSet
	 * @param createdByAxioms
	 * @return true if bridgeset only contains axiom edges, else false
	 */
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

	/**
	 * Translates our constraint graph into a Leiden Network
	 * 
	 * @return Network
	 */
	private Network translateGraphToLeidenNetwork() {
		// Create ID's for the vertices
		initIDsForVertices();

		// Create the node weights (simply set them all to 1.0)
		double[] nodeWeights = new double[verticesToId.size()];
		Arrays.fill(nodeWeights, 1.0);

		// Create the edges and edge weight arrays
		int[][] edges = new int[2][this.graph.edgeSet().size()];
		double[] edgeWeights = new double[this.graph.edgeSet().size()];
		int i = 0;
		// For each edge
		for (DefaultEdge e : graph.edgeSet()) {
			// Get the id of the source and target
			int sourceV = verticesToId.get(this.graph.getEdgeSource(e)).intValue();
			int targetV = verticesToId.get(this.graph.getEdgeTarget(e)).intValue();

			// to save it in the edges for the leiden algorithm
			edges[0][i] = sourceV;
			edges[1][i] = targetV;

			// Put higher weights on edges that weren't created by axioms (so that
			// the algorithm has a higher probability to keep them)
			if (createdByAxioms.containsKey(e)) {
				edgeWeights[i] = 1;
			} else {
				edgeWeights[i] = Settings.WEIGHT_FOR_NON_AXIOM_EDGES;
			}
			i++;
		}

		return new Network(nodeWeights, edges, edgeWeights, false, true);
	}

	/**
	 *  Helper method to start the clustering first when it's needed
	 */
	private void lazyClustering() {
		if (!lazy_clustering) {
			// Calculate the clustering
			startLeidenCommunityDetection();
			lazy_clustering = true;
		}

	}

	/**
	 *  Creates ID's vor the vertices
	 */
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
