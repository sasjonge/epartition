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
            if (Settings.USE_OLH_AFTER) {

                long startPreHeuristicTime = System.nanoTime();
                // This part belongs to heuristic
                long sumOfHeuristicTime = 0;
                reasonerFactory = new StructuralReasonerFactory();
                int sizeOfOldOnt = oldOntology.getLogicalAxiomCount();

                boolean repeat = true;

                int step = 0;

                long sumOfPartTime = System.nanoTime() - startPreHeuristicTime;
                while (repeat) {

                    long startPartTime = System.nanoTime();

                    partitionedOntologies = pc.partition(ontology);

                    sumOfPartTime += System.nanoTime() - startPartTime;

                    long startHeuristicTime = System.nanoTime();
                    Collections.sort(partitionedOntologies, new Comparator<OWLOntology>() {
                        @Override
                        public int compare(OWLOntology ontology, OWLOntology t1) {
                            return t1.getLogicalAxiomCount() - ontology.getLogicalAxiomCount();
                        }
                    });

                    OWLOntology biggest = partitionedOntologies.iterator().next();

                    if (sizeOfOldOnt * Settings.OLH_AFTER_TRESHHOLD >= biggest.getLogicalAxiomCount() || step >= Settings.OLH_AFTER_REPETITIONS) {
                        if (sizeOfOldOnt * Settings.OLH_AFTER_TRESHHOLD >= biggest.getLogicalAxiomCount()) {
                            System.out.println(sizeOfOldOnt + " bigger as " + biggest.getLogicalAxiomCount());
                        }
                        System.out.println("!!!!!!!!!!!!! " + partitionedOntologies.size());

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

    public static void evalRun(String[] args) {
        // Read all files in the given directory
        try (Stream<Path> paths = Files.walk(Paths.get(Settings.INPUT_DIRECTORY))) {
            List<Path> listOfPaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            List<String> failed = new LinkedList<>();

            for (int i = 5; i < Settings.MAX_OLH_LAYER; i++) {
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

        // Split if we want to do several evaluation runs (to average the statistics)
        if (Settings.EVAL_RUN) {
            evalRun(args);
        } else {
            nonEvalRun(args);
        }

    }
}
