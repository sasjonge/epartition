package uni.sasjonge;

public class Settings {
	
	// The output path for the graph
	static final String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/test.graphml";
	
	// The input ontology
	//static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/knowrob_merged.owl";
	static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/pto.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/workspace/java_ws/partitioner/res/partitioner_test.owl";
	// static final String INPUT_ONTOLOGY = "file:/home/sascha/pepper_dialog/ros_dep/src/knowrob/knowrob_household/owl/kitchen_items.owl";
	// SNOMED

	public static final int LAYERS_TO_REMOVE = 4;
	
	public static int CLASS_DEPTH_TO_REMOVE = 3;
	
	public static int NUM_OF_CLASS_LABELS = 7;
	
	public static int NUM_OF_PROPERTY_LABELS_EDGE = 4;
	
	public static int NUM_OF_PROPERTY_LABELS_Node = 3;
	
	
}
