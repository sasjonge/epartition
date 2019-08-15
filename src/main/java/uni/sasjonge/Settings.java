package uni.sasjonge;

import java.net.URI;

/**
 * Helper class to store settings like pathes, output format, parameters for heuristics, etc.
 * @author sascha
 *
 */
public class Settings {
	
	// The input directory where the .owl files should be
	public static final String ONTOLOGIES_DIRECTORY = "/home/sascha/Desktop/onts";
	
	// The output path for the graph
	public static final String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/graphs/";
	
	// The type of the output graph:
	// 0 = Partition structure graph
	// 1 = Constraint graph
	public static final int OUTPUT_GRAPH_TYPE = 0;	
	
	// The loading mode: Loading a single ontology or all ontologies containing in a directory
	public static final boolean USE_RDF_LABEL = true;
	public static final String lang = "en";
	
	// Designators for R (so r1 would be r<PROPERTY_1_DESIGNATOR>
	public static final String PROPERTY_0_DESIGNATOR = "[0]";
	public static final String PROPERTY_1_DESIGNATOR = "[1]";
	
	// How to handle universal roles
	// - do we even want to handle them? if false, the tool refuses universal roles
	public static final boolean HANDLE_UNIVERSAL_ROLES = true;
	// - if true, we can set a treshold to which point the program 
	public static final int UNIVERAL_ROLES_TRESHOLD = 8;
	
	// -------- Ontology Level Reducer Heuristic (OLH) --------
	public static final boolean USE_OLH = false;
	// Number of "layers" to remove in the OntologyLevelReducer
	public static final int OLH_LAYERS_TO_REMOVE = 1;
	
	// ----------- Biconnectivity Heuristic (BH) --------------
	// Max number of axioms of labels to remove
	public static final boolean USE_BH = false;
	public static final int BH_NUM_OF_AXIOM_LABELS = 1;
	public static final int BH_NUM_OF_REPETITION_OF_HEURISTIC = 1;

	
	// -------- Community Detection Heuristic (CD) -----------
	public static final boolean USE_CD = true;
	// Weight of non axiom edges in the network
	public static int WEIGHT_FOR_NON_AXIOM_EDGES = 3;
	// Resolution start-valaue and decrease for leiden
	public static double RESOLUTION_AT_START = 1d;
	public static double RESOLUTION_DECREASE = 0.75d;

	// -------- Upper level remover heuristic(ULH) -----------
	public static final boolean USE_ULH = false;
	// File used in the UpperLevelRemover. Should contain upper level ontologies
	public static final String UPPER_LEVEL_FILE = "/home/sascha/workspace/java_ws/partitioner/res/upperlevels/upperlevels.json";
	// Treshhold for how many percent of the upper level ontology the given ontology can contain,
	// before it's removed
	public static final double ULH_REMOVAL_TRESHHOLD = 1.00;

	// --------------- Graph visualization -------------------
	// How many individual labels should be shown
	public static final int NUM_OF_INDIV_LABELS = 3;
	// Number of properties on a edge
	public static final int NUM_OF_PROPERTY_LABELS_EDGE = 4;

	// Number of class labels on a node
	// Max number of "Groups" to be shown
	public static final int NUM_OF_CLASS_LABELS_TOPLEVEL = 6;
	// Max number of classes per group
	public static final int NUM_OF_CLASS_LABELS_SUBLEVEL = 4;
	
	// Number of property labels on a node
	// Max number of "Groups" to be shown
	public static final int NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = 3;
	// Max number of classes per group
	public static final int NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = 3;
	
	// Should axioms be shown in the labels?
	public static final boolean SHOW_AXIOMS = true;
	// If yes, how many?
	public static final int AXIOM_COUNT = 6;
}
