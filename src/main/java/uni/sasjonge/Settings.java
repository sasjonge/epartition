package uni.sasjonge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

/**
 * Helper class to store settings like pathes, output format, parameters for heuristics, etc.
 * @author sascha
 *
 */
public class Settings {
	
	// -------------------------- INPUT ONTOLOGIES -----------------------------
	// The input directory where the .owl files should be
	public static String INPUT_DIRECTORY = "/home/sascha/Desktop/onts";

	// ---------------------- Evaluation Data Output --------------------------
	// Is this a evaluation run?
	public static boolean EVALUATE = true;
	// Output path for the evaluation statistics
	public static String EVALUATION_OUTPUT_FILE = INPUT_DIRECTORY + "_statistics.xml";

	// If we want multiple runs for each ontolgy
	// We take the average of the statistics the runs (removing the highest and lowest statistic)
	public static boolean EVAL_RUN = false;
	public static int NUMBER_OF_RUNS = 5;

	// For the OLH statistic: Start the evaluation new with an increased number of layers
	//that will beremoved
	public static int MAX_OLH_LAYER = 8;

	// ----------------------- GRAPH OUTPUT ------------------------------------
	// The output path for the graph
	public static String GRAPH_OUTPUT_PATH = "/home/sascha/Desktop/graphs/";

	// The type of the output graph:
	// 0 = Partition structure graph
	// 1 = Constraint graph
	public static int OUTPUT_GRAPH_TYPE = 0;
	
	// For the constraint graph
	// Should axioms be shown in the labels?
	public static boolean SHOW_AXIOMS = true;
	// If yes, how many?
	public static int AXIOM_COUNT = 3;
	
	// How many individual labels should be shown
	public static int NUM_OF_INDIV_LABELS = 3;
	// Number of properties on a edge
	public static int NUM_OF_PROPERTY_LABELS_EDGE = 34;

	// Number of class labels on a node
	// Max number of "Groups" to be shown
	public static int NUM_OF_CLASS_LABELS_TOPLEVEL = 13;
	// Max number of classes per group
	public static int NUM_OF_CLASS_LABELS_SUBLEVEL = 4;
	
	// Number of property labels on a node
	// Max number of "Groups" to be shown
	public static int NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = 33;
	// Max number of classes per group
	public static int NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = 3;

	// ----------------------- ONTOLOGY OUTPUT ---------------------------------
	// The output path for the graph
	public static String ONOTOLOGY_OUTPUT_PATH = "/home/sascha/Desktop/out/";

	// Should the created partitions exported as owl?
	public static boolean EXPORT_ONTOLOGIES = true;

	// ------- Settings for the default partitioner ----------
	
	// Handling of RDF labels as names and the used lang 
	public static boolean USE_RDF_LABEL = true;
	public static String lang = "en";
	
	// Designators for R (so r1 would be r<PROPERTY_1_DESIGNATOR>
	public static String PROPERTY_0_DESIGNATOR = "[0]";
	public static String PROPERTY_1_DESIGNATOR = "[1]";
	
	// How to handle universal roles
	// - do we even want to handle them? if false, the tool refuses universal roles
	public static boolean HANDLE_UNIVERSAL_ROLES = true;
	// - if true, we can set a treshold to which point the program 
	public static int UNIVERAL_ROLES_TRESHOLD = 3;

	// Ignore safety check
	public static boolean IGNORE_SAFETY_CHECK = false;

	// Should the removed axioms be printed?
	public static boolean PRINT_REMOVED_AXIOMS = true;
	
	// -------------- Settings for heuristics -----------------
	// ----------- Ignore Properties Heuristic (IPH) ----------
	public static boolean USE_IPH = true;

	// Properties that are used global (in several topics) (in domain and range)
	public static HashSet<String> GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(new String[]{
			"Associated with (attribute)",
			"Causative agent (attribute)",
			"Due to (attribute)",
			"Temporally related to (attribute)",
			"After (attribute)",
			"Before (attribute)",
			"During (attribute)",
			"Characterizes (attribute)"

	}));

	// Properties that are used global in their domain
	public static HashSet<String> DOMAIN_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(new String[]{
			"Role group (attribute)"
	}));

	// Properties that are used global in their range
	public static HashSet<String> RANGE_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(new String[]{
			"Has focus (attribute)",
			"Component (attribute)",
			"Process output (attribute)",
			"Inheres in (attribute)",
			"Precondition (attribute)", // Connects Qualifier Value and Even
			"Specimen source identity (attribute)" // Occupation, environment, physical object
	}));

	// -------- Ontology Level Reducer Heuristic (OLH) --------
	public static boolean USE_OLH = false;
	// Number of "layers" to remove in the OntologyLevelReducer
	public static int OLH_LAYERS_TO_REMOVE = 1;

	// Alternative (or addition): OLH on biggest component after partitioning
	public static boolean USE_OLH_AFTER = false;
	// How many repetitions of this heuristic (one level is removed in every step)
	public static int OLH_AFTER_REPETITIONS = 1;
	// Minimal portion of ontology to repeat the heuristic
	public static double OLH_AFTER_TRESHHOLD = 0.9;
	
	// ----------- Biconnectivity Heuristic (BH) --------------
	public static boolean USE_BH = false;
	// Max number of axioms of labels to remove
	public static int BH_NUM_OF_AXIOM_LABELS = 1;
	// Number of repetition of this heuristic
	public static int BH_NUM_OF_REPETITION_OF_HEURISTIC = 1;

	// -------- Community Detection Heuristic (CD) -----------
	public static boolean USE_CD = false;
	// Flag stating if the louvain or leiden algorithm should be used
	// true = leiden, false = louvain
	public static boolean CD_LEIDEN = true;
	// Weight of non axiom edges in the network
	public static int CD_WEIGHT_FOR_NON_AXIOM_EDGES = 3;
	// Resolution start-valaue and decrease for leiden or louvain algorithm
	public static double CD_RESOLUTION_AT_START = 1d;
	public static double CD_RESOLUTION_DECREASE = 0.75d;

	// -------- Upper level remover heuristic(ULH) -----------
	public static boolean USE_ULH = false;
	// File used in the UpperLevelRemover. Should contain upper level ontologies
	public static String UPPER_LEVEL_FILE = "/home/sascha/workspace/java_ws/partitioner/res/upperlevels/upperlevels.json";
	// Treshhold for how many percent of the upper level ontology the given ontology can contain,
	// before it's removed
	public static double ULH_REMOVAL_TRESHHOLD = 0.9d;

	public static void readSettingsFile(String pathToSettings) {
		Properties props = new Properties();

		try {
			props.loadFromXML(new FileInputStream(pathToSettings));

			Settings.INPUT_DIRECTORY = props.getProperty("input_directory");
			Settings.EVALUATE = props.getProperty("evaluate").equals("true") ? true : false;
			Settings.EVALUATION_OUTPUT_FILE = props.getProperty("evaluation_output_file");
			Settings.EVAL_RUN = props.getProperty("eval_runs").equals("true") ? true : false;
			Settings.NUMBER_OF_RUNS = Integer.parseInt(props.getProperty("number_of_runs"));
			Settings.MAX_OLH_LAYER = Integer.parseInt(props.getProperty("max_olh_layer"));
			Settings.GRAPH_OUTPUT_PATH =props.getProperty("graph_output_path",GRAPH_OUTPUT_PATH);
			Settings.OUTPUT_GRAPH_TYPE = Integer.parseInt(props.getProperty("output_graph_type"));
			Settings.SHOW_AXIOMS = props.getProperty("show_axioms").equals("true") ? true : false;
			Settings.AXIOM_COUNT = Integer.parseInt(props.getProperty("axiom_count"));
			Settings.NUM_OF_INDIV_LABELS = Integer.parseInt(props.getProperty("number_of_indiv_labels"));
			Settings.NUM_OF_PROPERTY_LABELS_EDGE = Integer.parseInt(props.getProperty("number_of_property_labels_edge"));
			Settings.NUM_OF_CLASS_LABELS_TOPLEVEL = Integer.parseInt(props.getProperty("number_of_class_labels_toplevel"));
			Settings.NUM_OF_CLASS_LABELS_SUBLEVEL = Integer.parseInt(props.getProperty("number_of_class_labels_sublevel"));
			Settings.NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = Integer.parseInt(props.getProperty("number_of_property_labels_vertex_toplevel"));
			Settings.NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = Integer.parseInt(props.getProperty("number_of_property_labels_vertex_sublevel"));
			Settings.ONOTOLOGY_OUTPUT_PATH = props.getProperty("ontology_output_path");
			Settings.EXPORT_ONTOLOGIES = props.getProperty("export_ontologies").equals("true") ? true : false;
			Settings.USE_RDF_LABEL = props.getProperty("use_rdf_label").equals("true") ? true : false;
			Settings.lang = props.getProperty("rdf_label_lang");
			Settings.PROPERTY_0_DESIGNATOR = props.getProperty("property_0_designator");
			Settings.PROPERTY_1_DESIGNATOR = props.getProperty("property_1_designator");
			Settings.HANDLE_UNIVERSAL_ROLES = props.getProperty("handle_universal_roles").equals("true") ? true : false;
			Settings.UNIVERAL_ROLES_TRESHOLD = Integer.parseInt(props.getProperty("universal_role_treshhold"));
			Settings.PRINT_REMOVED_AXIOMS = props.getProperty("print_removed_axioms").equals("true") ? true : false;
			Settings.IGNORE_SAFETY_CHECK = props.getProperty("ignore_safety_check").equals("true") ? true : false;
			Settings.USE_IPH = props.getProperty("use_iph").equals("true") ? true : false;
			String preGlobal = props.getProperty("global_properties");
			Settings.GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(preGlobal.substring(1, preGlobal.length() - 1).split(", ")));
			String preDomainGlobal = props.getProperty("domain_global_properties");
			Settings.DOMAIN_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(preDomainGlobal.substring(1, preDomainGlobal.length() - 1).split(", ")));
			String preRangeGlobal = props.getProperty("range_global_properties");
			Settings.RANGE_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(preRangeGlobal.substring(1, preRangeGlobal.length() - 1).split(", ")));
			Settings.USE_OLH = props.getProperty("use_olh").equals("true") ? true : false;
			Settings.OLH_LAYERS_TO_REMOVE = Integer.parseInt(props.getProperty("olh_layers_to_remove"));
			Settings.USE_OLH_AFTER = props.getProperty("use_olh_after").equals("true") ? true : false;
			Settings.OLH_AFTER_REPETITIONS = Integer.parseInt(props.getProperty("olh_after_repetitions"));
			Settings.OLH_AFTER_TRESHHOLD = Double.parseDouble(props.getProperty("olh_after_treshhold"));
			Settings.USE_BH = props.getProperty("use_bh").equals("true") ? true : false;
			Settings.BH_NUM_OF_AXIOM_LABELS = Integer.parseInt(props.getProperty("bh_number_of_axiom_labels"));
			Settings.BH_NUM_OF_REPETITION_OF_HEURISTIC = Integer.parseInt(props.getProperty("bh_number_of_repetitions_of_heuristic"));
			Settings.USE_CD = props.getProperty("use_cd").equals("true") ? true : false;
			Settings.CD_LEIDEN = props.getProperty("cd_leiden").equals("true") ? true : false;
			Settings.CD_WEIGHT_FOR_NON_AXIOM_EDGES = Integer.parseInt(props.getProperty("cd_weight_for_non_axiom_edges"));
			Settings.CD_RESOLUTION_AT_START = Double.parseDouble(props.getProperty("cd_resolution_at_start"));
			Settings.CD_RESOLUTION_DECREASE = Double.parseDouble(props.getProperty("cd_resolution_decrease"));
			Settings.USE_ULH = props.getProperty("use_ulh").equals("true") ? true : false;
			Settings.UPPER_LEVEL_FILE = props.getProperty("upper_level_file");
			Settings.ULH_REMOVAL_TRESHHOLD = Double.parseDouble(props.getProperty("ulh_removal_treshhold"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
