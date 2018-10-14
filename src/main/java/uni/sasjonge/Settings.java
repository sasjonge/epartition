package uni.sasjonge;

public class Settings {
	
	// The output path for the graph
	static final String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/test.graphml";
	
	// The input ontology
	//static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/knowrob_merged.owl";
	static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/pto.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/test_output.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/partitioner_test.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/pepper_dialog/ros_dep/src/knowrob/knowrob_household/owl/kitchen_items.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/koala.owl";
	// SNOMED

	public static final int LAYERS_TO_REMOVE = 3;

	public static final int NUM_OF_INDIVIDUAL_LABELS = 4;
	
	public static final int CLASS_DEPTH_TO_REMOVE = 3;

	public static final int NUM_OF_CLASS_LABELS_TOPLEVEL = 6;
	public static final int NUM_OF_CLASS_LABELS_SUBLEVEL = 4;
	
	public static final int NUM_OF_INDIV_LABELS = 3;
	
	public static final int NUM_OF_PROPERTY_LABELS_EDGE = 4;
	
	public static final int NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = 3;
	public static final int NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = 3;

	
}
