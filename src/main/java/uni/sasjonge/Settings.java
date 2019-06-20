package uni.sasjonge;

public class Settings {
	
	// The output path for the graph
	static final String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/graphs/";
	
	// The input ontology
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/knowrob_merged.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/pto.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/test_output.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/partitioner_test.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/pepper_dialog/ros_dep/src/knowrob/knowrob_household/owl/kitchen_items.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/koala.owl";
	// static final String INPUT_ONTOLOGY = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
	// static final String INPUT_ONTOLOGY = "file:///home/sascha/workspace/java_ws/partitioner/res/ncit.national-cancer-institute-thesaurus.47.owl.xml";
	//static final String INPUT_ONTOLOGY = "file:///home/sascha/workspace/java_ws/partitioner/res/sct-stated-form-2010-01-31.owl";

	public static final int LAYERS_TO_REMOVE = 0;

	public static final int NUM_OF_INDIVIDUAL_LABELS = 4;
	
	public static final int CLASS_DEPTH_TO_REMOVE = 3;

	public static final int NUM_OF_CLASS_LABELS_TOPLEVEL = 6;
	public static final int NUM_OF_CLASS_LABELS_SUBLEVEL = 4;
	
	public static final int NUM_OF_INDIV_LABELS = 3;
	
	public static final int NUM_OF_PROPERTY_LABELS_EDGE = 4;
	
	public static final int NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = 3;
	public static final int NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = 3;

	static final String UPPER_LEVEL_FILE = "/home/sascha/workspace/java_ws/partitioner/res/upperlevels/upperlevels.json";

	// Should axioms be shown?
	public static final boolean SHOW_AXIOMS = false;
	// If yes, how many?
	public static final int AXIOM_COUNT = 4;

	public static final boolean USE_RDF_LABEL = false;
	
	public static final String PROPERTY_0_DESIGNATOR = "[0]";
	public static final String PROPERTY_1_DESIGNATOR = "[1]";
	
	// How to handle universal roles
	// - do we even want to handle them? if false, the tool refuses universal roles
	public static final boolean HANDLE_UNIVERSAL_ROLES = true;
	// - if true, we can set a treshold to which point the program 
	public static final int UNIVERAL_ROLES_TRESHOLD = 5;
	
}
