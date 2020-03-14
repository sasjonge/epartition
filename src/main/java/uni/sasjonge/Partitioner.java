package uni.sasjonge;

import org.jgrapht.io.ExportException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import uni.sasjonge.heuristics.levelreducer.OntologyLevelReducer;
import uni.sasjonge.heuristics.upperlevel.UpperLevelManager;
import uni.sasjonge.partitioning.PartitioningCore;
import uni.sasjonge.partitioning.SafetyChecker;
import uni.sasjonge.utils.GraphExporter;
import uni.sasjonge.utils.OntologyDescriptor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Partioning of an ontology based on the following paper: Jongebloed, Sascha,
 * and Thomas Schneider. "Ontology Partitioning Using E-Connections Revisited."
 * MedRACER+ WOMoCoE@ KR. 2018. Link to paper:
 * http://www.informatik.uni-bremen.de/tdki/research/papers/2018/JS-DL18.pdf
 *
 * @author Sascha Jongebloed
 */
public class Partitioner {

    OWLReasonerFactory reasonerFactory;
    OWLReasoner reasoner;
    OWLDataFactory df;
    StringBuilder builder;
    List<OWLOntology> partitionedOntologies;
    int sizeOfOriginalOntology = 0;

    /**
     * Loads the input ontology and partitons it
     *
     * @param input_ontology
     */
    public void loadOntology(String input_ontology) {

        System.out.println("-----------------------------------------");
        System.out.println(input_ontology);

        // A string builder to save the evaluation statistics in a string
        builder = new StringBuilder();

        long startTime = System.nanoTime();
        // Create a owl manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        df = manager.getOWLDataFactory();

        // Initizalize the partioning algorithm
        PartitioningCore pc = new PartitioningCore();

        // Save old ontology
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();

        OntologyDescriptor.init();

        long endInitTime = System.nanoTime();
        System.out.println("Initiating classes took " + (endInitTime - startTime) / 1000000 + "ms");
        if (Settings.EVALUATE) {
            builder.append(getFileName(input_ontology).substring(1) + ", ");
        }

        try {
            // Load the input ontology
            long loadStartTime = System.nanoTime();
            OWLOntology loadedOnt = manager.loadOntology(IRI.create("file:" + input_ontology));
            sizeOfOriginalOntology = loadedOnt.getLogicalAxiomCount();

            // if rdfs:labels are used as name, map them
            if (Settings.USE_RDF_LABEL) {
                OntologyDescriptor.initRDFSLabel(loadedOnt);
            }

            long loadEndTime = System.nanoTime();
            System.out.println("Loading the ontology took " + (loadEndTime - loadStartTime) / 1000000 + "ms");
            if (Settings.EVALUATE) {
                builder.append(loadedOnt.getAxiomCount() + ", ");
                builder.append((endInitTime - startTime) / 1000000 + ", ");
                builder.append((loadEndTime - loadStartTime) / 1000000 + ", ");
            }
            long expressStartTime = System.nanoTime();

            // Check expressivity of the ontology and either stop or handle the cases
            List<OWLAxiom> axiomsContainingUniversalRole = loadedOnt.referencingAxioms(df.getOWLTopObjectProperty())
                    .collect(Collectors.toList());

            // If we have universal roles we remove them to a specific treshhold
            if (axiomsContainingUniversalRole.size() > 0) {
                if (Settings.HANDLE_UNIVERSAL_ROLES) {

                    System.out.println(getFileName(input_ontology) + " has " + axiomsContainingUniversalRole.size());

                    if (axiomsContainingUniversalRole.size() <= Settings.UNIVERAL_ROLES_TRESHOLD) {
                        // Remove all axioms containing the universal role
                        System.out.println("Removing " + axiomsContainingUniversalRole.size()
                                + " axioms containing universal roles");
                        loadedOnt.remove(axiomsContainingUniversalRole);
                    } else {
                        builder = new StringBuilder(input_ontology + " contains more than the allowed number of axioms with univeral roles");
                        return;
                    }
                } else {
                    builder = new StringBuilder(input_ontology + " contains universal roles and cannot be handled");
                    return;
                }
            }

            // Check for safety
            if (!Settings.IGNORE_SAFETY_CHECK) {
                if (!SafetyChecker.isSafe(loadedOnt)) {
                    builder = new StringBuilder(input_ontology + " is not safe");
                    return;
                }
            }

            long expressEndTime = System.nanoTime();
            System.out.println("Checking expressivity took " + (expressEndTime - expressStartTime) / 1000000 + "ms");
            if (Settings.EVALUATE) {
                builder.append((expressEndTime - expressStartTime) / 1000000 + ", ");
            }

            // Copying ontology
            long copyStartTime = System.nanoTime();
            OWLOntology oldOntology = manager2.copyOntology(loadedOnt, OntologyCopy.DEEP);

            long copyEndTime = System.nanoTime();
            System.out.println("Copying the ontology took " + (copyEndTime - copyStartTime) / 1000000 + "ms");
            if (Settings.EVALUATE) {
                builder.append((copyEndTime - copyStartTime) / 1000000 + ", ");
            }

            long reduceStartTime = System.nanoTime();
            OWLOntology ontology = null;
            // ---------------- Ontology level reducer heuristic ----------------------
            if (Settings.USE_OLH) {
                reasonerFactory = new StructuralReasonerFactory();
                reasoner = reasonerFactory.createNonBufferingReasoner(oldOntology);
                // Remove the "Top-Layer" (the classes highest in the hierachy)
                ontology = OntologyLevelReducer.removeHighestLevelConc(manager, loadedOnt, reasoner, df,
                        Settings.OLH_LAYERS_TO_REMOVE);
            } else {
                ontology = loadedOnt;
            }

            // -------------- Upper level remover heuristic ------------------
            if (Settings.USE_ULH) {
                // Instantiate the upper level manager
                UpperLevelManager ulRemove = new UpperLevelManager(Settings.UPPER_LEVEL_FILE);
                // Remove all upper level where more than Settings.ULH_REMOVAL_TRESHHOLD percent
                // of the upper level ontology is contained
                for (Entry<String, Double> entry : ulRemove.checkForUpperLevel(loadedOnt).entrySet()) {
                    System.out.println(entry.getValue().doubleValue() + "% of the upper level ontology "
                            + entry.getKey() + " is in this ontology");
                    if (entry.getValue().doubleValue() >= Settings.ULH_REMOVAL_TRESHHOLD) {
                        ontology = ulRemove.removeUpperLevel(loadedOnt, entry.getKey());
                    }
                }
            }

            // ---------------- Upper level remover version 2 ------------------
            // -- Removes the version
            if (Settings.USE_OLH_AFTER) {

                long startPreHeuristicTime = System.nanoTime();
                // This part belongs to heuristic
                long sumOfHeuristicTime = 0;
                // Create an structural reasoner
                reasonerFactory = new StructuralReasonerFactory();
                // Get the siz of the old ontology
                int sizeOfOldOnt = oldOntology.getLogicalAxiomCount();

                // Falg if we want to repeat the step
                boolean repeat = true;

                // Number of the step
                int step = 0;

                long sumOfPartTime = System.nanoTime() - startPreHeuristicTime;

                // While we want to repeat
                while (repeat) {

                    long startPartTime = System.nanoTime();

                    // Partition the ontology
                    partitionedOntologies = pc.partition(ontology);

                    sumOfPartTime += System.nanoTime() - startPartTime;

                    long startHeuristicTime = System.nanoTime();

                    // Sorty the components by size
                    Collections.sort(partitionedOntologies, new Comparator<OWLOntology>() {
                        @Override
                        public int compare(OWLOntology ontology, OWLOntology t1) {
                            return t1.getLogicalAxiomCount() - ontology.getLogicalAxiomCount();
                        }
                    });

                    // Get the biggest ontology
                    OWLOntology biggest = partitionedOntologies.iterator().next();

                    // If the biggest ontology exceeds the treshold and we haven't done enough steps till now
                    if (sizeOfOldOnt * Settings.OLH_AFTER_TRESHHOLD >= biggest.getLogicalAxiomCount() || step >= Settings.OLH_AFTER_REPETITIONS) {
                        // If the biggest ontology exceeds the treshold
                        if (sizeOfOldOnt * Settings.OLH_AFTER_TRESHHOLD >= biggest.getLogicalAxiomCount()) {
                            System.out.println(sizeOfOldOnt + " bigger as " + biggest.getLogicalAxiomCount());
                        }

                        repeat = false;
                    } else {
                        reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
                        // Remove the "Top-Layer" (the classes highest in the hierachy)
                        ontology = OntologyLevelReducer.removeHighestLevelLimited(manager, ontology, reasoner, df, biggest
                        );

                        if (!OntologyLevelReducer.changedSomething) {
                            repeat = false;
                        }
                    }

                    step++;
                    sumOfHeuristicTime += System.nanoTime() - startHeuristicTime;

                }
                long reduceEndTime = System.nanoTime();
                System.out.println("Reducing the ontology with the choosen heuristics took " + sumOfHeuristicTime / 1000000 + "ms");
                if (Settings.EVALUATE) {
                    builder.append(sumOfHeuristicTime / 1000000 + ",");
                }
                System.out.println("Partitioning took " + sumOfPartTime / 1000000 + "ms");

                if (Settings.EVALUATE) {
                    builder.append(sumOfPartTime / 1000000 + ", ");
                }
            } else {

                long reduceEndTime = System.nanoTime();
                System.out.println("Reducing the ontology with the choosen heuristics took " + (reduceEndTime - reduceStartTime) / 1000000 + "ms");
                if (Settings.EVALUATE) {
                    builder.append((reduceEndTime - reduceStartTime) / 1000000 + ",");
                }

                long startPartTime = System.nanoTime();
                // Call the partitioning algorithm
                partitionedOntologies = pc.partition(ontology);
                long endPartTime = System.nanoTime();
                System.out.println("Partitioning took " + (endPartTime - startPartTime) / 1000000 + "ms");

                if (Settings.EVALUATE) {
                    builder.append((endPartTime - startPartTime) / 1000000 + ", ");
                }
            }

            // Export the graph
            long startGraphTime = System.nanoTime();
            // Initiate the graphexporter (create the hierachy and descriptors)
            GraphExporter.init(oldOntology);

            // Choose type of output graph
            switch (Settings.OUTPUT_GRAPH_TYPE) {

                // Alternative: Create the complex graph created by the algorithm
                case 2:
                    break;
                case 1:
                    GraphExporter.exportConstraintGraph(pc.g,
                            Settings.GRAPH_OUTPUT_PATH + getFileName(input_ontology) + ".graphml");
                    break;
                // Create the output graph in form of the cc structure
                case 0:
                default:
                    String graphStructure = GraphExporter.exportCCStructureGraph(pc.g, oldOntology, pc.edgeToAxioms,
                            pc.edgeToVertex, Settings.GRAPH_OUTPUT_PATH + getFileName(input_ontology) + ".graphml");
                    break;
            }

            long endGraphTime = System.nanoTime();
            System.out.println("Graph building took " + (endGraphTime - startGraphTime) / 1000000 + "ms");

            long startExportTime = System.nanoTime();
            // If labels are used we will also add the labels to all ontologies that "use" the classes and properties
            if (Settings.EXPORT_ONTOLOGIES && Settings.USE_RDF_LABEL) {
                // Collect all annotation assertions
                Map<OWLEntity, OWLAxiom> entityToLabel = new HashMap<>();
                oldOntology.classesInSignature().forEach(cls -> {
                    oldOntology.annotationAssertionAxioms(cls.getIRI()).forEach(ax -> {
                        if (ax.getProperty().isLabel()) {
                            entityToLabel.put(cls,ax);
                        }
                    });
                });
                oldOntology.objectPropertiesInSignature().forEach(prop -> {
                    oldOntology.annotationAssertionAxioms(prop.getIRI()).forEach(ax -> {
                        if (ax.getProperty().isLabel()) {
                            entityToLabel.put(prop,ax);
                        }
                    });
                });
                oldOntology.dataPropertiesInSignature().forEach(prop -> {
                    oldOntology.annotationAssertionAxioms(prop.getIRI()).forEach(ax -> {
                        if (ax.getProperty().isLabel()) {
                            entityToLabel.put(prop,ax);
                        }
                    });
                });

                // Now add the label axioms to the ontologies
                for (OWLOntology partOnt : partitionedOntologies) {
                    Set<OWLAxiom> collectedAnnotations = new HashSet<>();
                    oldOntology.classesInSignature().forEach(cls -> {
                        if (entityToLabel.containsKey(cls)) {
                            collectedAnnotations.add(entityToLabel.get(cls));
                        }
                    });
                    oldOntology.objectPropertiesInSignature().forEach(prop -> {
                        if (entityToLabel.containsKey(prop)) {
                            collectedAnnotations.add(entityToLabel.get(prop));
                        }
                    });
                    oldOntology.dataPropertiesInSignature().forEach(prop -> {
                        oldOntology.annotationAssertionAxioms(prop.getIRI()).forEach(ax -> {
                            if (entityToLabel.containsKey(prop)) {
                                collectedAnnotations.add(entityToLabel.get(prop));
                            }
                        });
                    });
                    partOnt.add(collectedAnnotations);
                }
            }

            if (Settings.EXPORT_ONTOLOGIES) {
                // Get the name (last part of the iri) to save the file (use the input)
                String fName = input_ontology.substring(input_ontology.lastIndexOf("/")).replaceAll(".owl", "");
                // Format for the ontologies
                OWLDocumentFormat format = new OWLXMLDocumentFormat();
                // Export the ontologys
                int fCounter = 1;
                for (OWLOntology part : partitionedOntologies) {
                    try (FileOutputStream output = new FileOutputStream(
                            Settings.ONOTOLOGY_OUTPUT_PATH + fName + "_" + fCounter + ".owl")) {
                        manager.saveOntology(part, format, output);
                        fCounter++;
                    } catch (OWLOntologyStorageException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                long endExportTime = System.nanoTime();
                System.out.println("Exporting ontologies took " + (endExportTime - startExportTime) / 1000000 + "ms");
                if (Settings.EVALUATE) {
                    builder.append((endExportTime - startExportTime) / 1000000 + ", ");
                }

            }

            if (Settings.EVALUATE) {
                builder.append((endGraphTime - startGraphTime) / 1000000 + ", ");
            }

            long endTime = System.nanoTime();
            System.out.println("Overall: " + (endTime - startTime) / 1000000 + "ms");
            if (Settings.EVALUATE) {
                builder.append((endTime - startTime) / 1000000 + ", ");
                if (Settings.USE_OLH || Settings.USE_OLH_AFTER || Settings.USE_BH || Settings.USE_CD || Settings.USE_ULH) {
                    builder.append(((int) getHeuristicsAxiomValues(partitionedOntologies)[0]) + ", "
                            + getHeuristicsAxiomValues(partitionedOntologies)[1] + ", ");
                }
                double[] biggestPartStats = getPercentageOfLargestPart(partitionedOntologies);
                builder.append((biggestPartStats[0]) + ", ");
                builder.append(String.format("%.2f", (biggestPartStats[1] * 100)) + ", ");
                builder.append(partitionedOntologies.size());
            }

            // Add the output graph to the builder
            // builder.append(graphStructure);
        } catch (OWLOntologyCreationException | IOException | ExportException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the filename for a given input ontology
     *
     * @param input_ontology
     * @return Filename
     */
    private String getFileName(String input_ontology) {
        String pre = input_ontology.substring(input_ontology.lastIndexOf("/"));
        return pre.substring(0, pre.lastIndexOf('.')).replace(", ]", "]");
    }

    /**
     * Return the statistics saved in the builder as a string
     *
     * @return String conatining the statistics of this run
     */
    public String getStatistics() {
        return builder != null ? builder.toString() : null;
    }

    /**
     * Calculates the percentage of the biggest partition
     *
     * @param partitionedOntologies
     * @return A array of [Size of biggest part.,percentage of biggest part]
     */
    public double[] getPercentageOfLargestPart(List<OWLOntology> partitionedOntologies) {
        int biggestSize = 0;
        int overallAxioms = 0;
        for (OWLOntology ont : partitionedOntologies) {
            int logAxiomsOfThisOnt = ont.getLogicalAxiomCount();
            if (biggestSize <= logAxiomsOfThisOnt) {
                biggestSize = logAxiomsOfThisOnt;
            }
            overallAxioms = overallAxioms + logAxiomsOfThisOnt;
        }

        return new double[]{(double) biggestSize, overallAxioms > 0 ? ((double) biggestSize) / ((double) overallAxioms) : 1.0d};
    }

    /**
     * @param partitionedOntologies
     * @return Array of the form [Removed Axioms, Original Size]
     */
    public double[] getHeuristicsAxiomValues(List<OWLOntology> partitionedOntologies) {
        int numOfAxiomsAfterHeuristics = 0;
        for (OWLOntology ont : partitionedOntologies) {
            numOfAxiomsAfterHeuristics += ont.getLogicalAxiomCount();
        }

        int removedAxiomCount = sizeOfOriginalOntology - numOfAxiomsAfterHeuristics;

        return new double[]{removedAxiomCount, ((double) removedAxiomCount) / ((double) sizeOfOriginalOntology)};
    }

    /**
     * Runs the partitioner for several different layer level of the OLH and a specific number of times
     * The highest and lowest result are removed from the statistic, and then they are averaged
     * @param args
     */
    public static void evalRun(String[] args) {
        // Read all files in the given directory
        try (Stream<Path> paths = Files.walk(Paths.get(Settings.INPUT_DIRECTORY))) {
            List<Path> listOfPaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            List<String> failed = new LinkedList<>();

            for (int i = Settings.OLH_LAYERS_TO_REMOVE; i < Settings.MAX_OLH_LAYER; i++) {
                // Set OLH Layer
                Settings.OLH_LAYERS_TO_REMOVE = i;
                // Save the statistics for the partitioning in a file
                File fout = new File(Settings.EVALUATION_OUTPUT_FILE.replace(".xml", "") + "_" + i + ".csv");
                FileOutputStream fos = new FileOutputStream(fout);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("Name, Num. of Axioms, Initiating classes (ms), Loading the Ontology (ms), " +
                        "Handle Universal Roles (ms), Copy Ontology (ms), Heuristics (ms), Partitioning Algorithm (ms), " +
                        (Settings.EXPORT_ONTOLOGIES ? "Exporting Ontologies (ms)," : "") + "Export Graph (ms), Overall (ms), " +
                        ((Settings.USE_OLH || Settings.USE_OLH_AFTER || Settings.USE_BH || Settings.USE_CD || Settings.USE_ULH) ?
                                "Removed Axioms, Perc. of removed Axioms, " : "") +
                        "Size of biggest Part., Perc. of biggest Part., Number of Part\n");

                for (Path e : listOfPaths) {
                    try {
                        // For each file
                        // Create a partitioner
                        Partitioner part = new Partitioner();
                        // Load and partition the ontology
                        if (e.toAbsolutePath().toString().endsWith(".owl") || e.toAbsolutePath().toString().endsWith(".xml")) {
                            part.loadOntology(e.toAbsolutePath().toString());

                            double[][] arrayOfStatistics = new double[Settings.NUMBER_OF_RUNS][];
                            try {
                                // Save the statistics of this partition in a string
                                // Save name of ont and remove it from string
                                String[] statsAsArrayStr = part.getStatistics().split(",");
                                String nameOfOnt = statsAsArrayStr[0];
                                String[] filteredStatsAsArrayStr = Arrays.copyOfRange(statsAsArrayStr, 1, statsAsArrayStr.length);

                                for (int runCount = 0; runCount < Settings.NUMBER_OF_RUNS; runCount++) {
                                    double[] statsOfThisRun = Arrays.stream(filteredStatsAsArrayStr)
                                            .map(String::trim)
                                            .mapToDouble(Double::parseDouble).toArray();
                                    arrayOfStatistics[runCount] = statsOfThisRun;
                                }

                                int lengOfStatistics = arrayOfStatistics[0].length;
                                double[] statsArray = new double[lengOfStatistics];

                                for (int statCount = 0; statCount < lengOfStatistics; statCount++) {
                                    double[] thisStats = new double[arrayOfStatistics.length];
                                    for (int j = 0; j < arrayOfStatistics.length; j++) {
                                        thisStats[j] = arrayOfStatistics[j][statCount];
                                    }

                                    Arrays.sort(thisStats);
                                    double[] filteredStats = Arrays.copyOfRange(thisStats, 1, thisStats.length - 1);

                                    // Take average
                                    OptionalDouble avg = Arrays.stream(filteredStats).average();

                                    statsArray[statCount] = avg.isPresent() ? avg.getAsDouble() : 0.0;
                                }

                                String statsArrayAsString = "" + statsArray[0];
                                for (int k = 1; k < statsArray.length; k++) {
                                    statsArrayAsString = statsArrayAsString + ", " + statsArray[k];
                                }

                                String stats = nameOfOnt + ", " + statsArrayAsString;
                                if (stats != null && Settings.EVALUATE) {
                                    // And write it in the output file
                                    bw.write(stats + "\n");
                                    bw.flush();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        } else {
                            System.err.println("The file " + e.toAbsolutePath().toString()
                                    + " isn't a .owl file, therefore, it wont be loaded");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        failed.add(e.toAbsolutePath().toString() + " failed\n");
                    }
                    OntologyDescriptor.clearMemory();
                    //GraphExporter.clearMemory();
                    System.gc();
                }
                bw.write("\n");
                for (String failure : failed) {
                    bw.write(failure);
                }
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * One simple run of the partitioner, without complex evaluation
     * @param args
     */
    public static void nonEvalRun(String[] args) {
        // Read all files in the given directory
        try (Stream<Path> paths = Files.walk(Paths.get(Settings.INPUT_DIRECTORY))) {

            List<String> failed = new LinkedList<>();

            // Save the statistics for the partitioning in a file
            File fout = new File(Settings.EVALUATION_OUTPUT_FILE);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("Name, Num. of Axioms, Initiating classes (ms), Loading the Ontology (ms), " +
                    "Handle Universal Roles (ms), Copy Ontology (ms), Heuristics (ms), Partitioning Algorithm (ms), " +
                    (Settings.EXPORT_ONTOLOGIES ? "Exporting Ontologies (ms)," : "") + "Export Graph (ms), Overall (ms), " +
                    ((Settings.USE_OLH || Settings.USE_OLH_AFTER || Settings.USE_BH || Settings.USE_CD || Settings.USE_ULH) ?
                            "Removed Axioms, Perc. of removed Axioms, " : "") +
                    "Size of biggest Part., Perc. of biggest Part., Number of Part\n");

            paths.filter(Files::isRegularFile).forEach(e -> {
                try {
                    // For each file
                    // Create a partitioner
                    Partitioner part = new Partitioner();
                    // Load and partition the ontology
                    if (e.toAbsolutePath().toString().endsWith(".owl") || e.toAbsolutePath().toString().endsWith(".xml")) {
                        part.loadOntology(e.toAbsolutePath().toString());

                        try {
                            // Save the statistics of this partition in a string
                            String stats = part.getStatistics();
                            if (stats != null && Settings.EVALUATE) {
                                // And write it in the output file
                                bw.write(stats + "\n");
                                bw.flush();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    } else {
                        System.err.println("The file " + e.toAbsolutePath().toString()
                                + " isn't a .owl file, therefore, it wont be loaded");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    failed.add(e.toAbsolutePath().toString() + " failed\n");
                }
                OntologyDescriptor.clearMemory();
                //GraphExporter.clearMemory();
                System.gc();
            });
            bw.write("\n");
            for (String failure : failed) {
                bw.write(failure);
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // Split for cmd options and proceed accordingly
        for(String arg : args) {
            String[] splittedArg = arg.substring(2,arg.length()).split("=");
            if (splittedArg.length == 1) {
                switch (splittedArg[0]) {
                    case "evaluate":
                        Settings.EVALUATE = true;
                        break;
                    case "eval_runs":
                        Settings.EVAL_RUN = true;
                        break;
                    case "show_axioms":
                        Settings.SHOW_AXIOMS = true;
                        break;
                    case "export_ontologies":
                        Settings.EXPORT_ONTOLOGIES = true;
                        break;
                    case "use_rdf_label":
                        Settings.USE_RDF_LABEL = true;
                        break;
                    case "ignore_safety_check":
                        Settings.IGNORE_SAFETY_CHECK = true;
                        break;
                    case "print_removed_axioms":
                        Settings.PRINT_REMOVED_AXIOMS = true;
                        break;
                    case "use_iph":
                        Settings.USE_IPH = true;
                        break;
                    case "use_olh":
                        Settings.USE_OLH = true;
                        break;
                    case "use_olh_after":
                        Settings.USE_OLH_AFTER = true;
                        break;
                    case "use_bh":
                        Settings.USE_BH = true;
                        break;
                    case "cd_leiden":
                        Settings.CD_LEIDEN = true;
                        break;
                    case "use_ulh":
                        Settings.USE_ULH = true;
                    default:
                        System.err.println(arg + " is an invalid argument");
                        return;
                }
            } else if (splittedArg.length == 2) {
                switch (splittedArg[0]) {
                    case "input_directory":
                        Settings.INPUT_DIRECTORY = splittedArg[1];
                        break;
                    case "evaluation_output_file":
                        Settings.EVALUATION_OUTPUT_FILE = splittedArg[1];
                        break;
                    case "number_of_runs":
                        Settings.NUMBER_OF_RUNS = Integer.parseInt(splittedArg[1]);
                        break;
                    case "max_olh_layer":
                        Settings.MAX_OLH_LAYER = Integer.parseInt(splittedArg[1]);
                    case "graph_output_path":
                        Settings.INPUT_DIRECTORY = splittedArg[1];
                        break;
                    case "axiom_count":
                        Settings.AXIOM_COUNT = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_indiv_labels":
                        Settings.NUM_OF_INDIV_LABELS = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_property_labels_edge":
                        Settings.NUM_OF_PROPERTY_LABELS_EDGE = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_class_labels_toplevel":
                        Settings.NUM_OF_CLASS_LABELS_TOPLEVEL = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_class_labels_sublevel":
                        Settings.NUM_OF_CLASS_LABELS_SUBLEVEL = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_property_labels_vertex_toplevel":
                        Settings.NUM_OF_PROPERTY_LABELS_NODE_TOPLEVEL = Integer.parseInt(splittedArg[1]);
                        break;
                    case "number_of_property_labels_vertex_sublevel":
                        Settings.NUM_OF_PROPERTY_LABELS_NODE_SUBLEVEL = Integer.parseInt(splittedArg[1]);
                        break;
                    case "ontology_output_path":
                        Settings.ONOTOLOGY_OUTPUT_PATH = splittedArg[1];
                        break;
                    case "rdf_label_lang":
                        Settings.lang = splittedArg[1];
                        break;
                    case "property_0_designator":
                        Settings.PROPERTY_0_DESIGNATOR = splittedArg[1];
                        break;
                    case "property_1_designator":
                        Settings.PROPERTY_1_DESIGNATOR = splittedArg[1];
                        break;
                    case "universal_role_treshhold":
                        Settings.UNIVERAL_ROLES_TRESHOLD = Integer.parseInt(splittedArg[1]);
                        break;
                    case "global_properties":
                        Settings.GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(splittedArg[1].substring(1, splittedArg[1].length() - 1).split(", ")));
                        break;
                    case "domain_global_properties":
                        Settings.DOMAIN_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(splittedArg[1].substring(1, splittedArg[1].length() - 1).split(", ")));
                        break;
                    case "range_global_properties":
                        Settings.RANGE_GLOBAL_PROPERTIES = new HashSet<String>(Arrays.asList(splittedArg[1].substring(1, splittedArg[1].length() - 1).split(", ")));
                        break;
                    case "olh_after_repetitions":
                        Settings.OLH_AFTER_REPETITIONS = Integer.parseInt(splittedArg[1]);
                        break;
                    case "olh_after_treshhold":
                        Settings.OLH_AFTER_TRESHHOLD = Double.parseDouble(splittedArg[1]);
                        break;
                    case "bh_number_of_axiom_labels":
                        Settings.BH_NUM_OF_AXIOM_LABELS = Integer.parseInt(splittedArg[1]);
                        break;
                    case "bh_number_of_repetitions_of_heuristic":
                        Settings.BH_NUM_OF_REPETITION_OF_HEURISTIC = Integer.parseInt(splittedArg[1]);
                        break;
                    case "cd_weight_for_non_axiom_edges":
                        Settings.CD_WEIGHT_FOR_NON_AXIOM_EDGES = Integer.parseInt(splittedArg[1]);
                        break;
                    case "cd_resolution_at_start":
                        Settings.CD_RESOLUTION_AT_START = Double.parseDouble(splittedArg[1]);
                        break;
                    case "cd_resolution_decrease":
                        Settings.CD_RESOLUTION_DECREASE = Double.parseDouble(splittedArg[1]);
                        break;
                    case "upper_level_file":
                        Settings.UPPER_LEVEL_FILE = splittedArg[1];
                        break;
                    case "ulh_removal_treshhold":
                        Settings.ULH_REMOVAL_TRESHHOLD = Double.parseDouble(splittedArg[1]);
                    default:
                        System.err.println(arg + " is an invalid argument");
                        return;
                }
            } else {
                System.err.println(arg + " is an invalid argument");
                return;
            }
        }
        // Split if we want to do several evaluation runs (to average the statistics)
        if (Settings.EVAL_RUN) {
            evalRun(args);
        } else {
            nonEvalRun(args);
        }

    }
}
