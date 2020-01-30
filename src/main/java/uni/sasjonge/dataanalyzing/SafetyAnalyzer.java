package uni.sasjonge.dataanalyzing;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uni.sasjonge.Settings;
import uni.sasjonge.partitioning.SafetyChecker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class SafetyAnalyzer {
    public static void main(String[] args) {
        // Read all files in the given directory
        try (Stream<Path> paths = Files.walk(Paths.get(Settings.ONTOLOGIES_DIRECTORY))) {

            List<String> failed = new LinkedList<>();

            // Save the statistics for the partitioning in a file
            File fout = new File(Settings.ONTOLOGIES_DIRECTORY + "_unsafe.xml");
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            Map<String, int[]> ontToData = new HashMap<>();

            paths.filter(Files::isRegularFile).forEach(e -> {
                try {
                    // Load and partition the ontology
                    if (e.toAbsolutePath().toString().endsWith(".owl") || e.toAbsolutePath().toString().endsWith(".xml")) {
                        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                        OWLDataFactory df = manager.getOWLDataFactory();
                        OWLOntology ontology = manager.loadOntology(IRI.create("file:" + e.toAbsolutePath().toString()));

                        // int numOfUniversalRoleAxioms = ontology.referencingAxioms(df.getOWLTopObjectProperty())
                        //        .collect(Collectors.toList()).size();


                        if(ontology.getAxiomCount(AxiomType.SWRL_RULE) <=  0) {
                            if (!SafetyChecker.isSafe(ontology)) {
                                bw.write(getFileName("file:" + e.toAbsolutePath().toString()).substring(1) + "\n");
                                bw.flush();
                                System.out.println(getFileName("file:" + e.toAbsolutePath().toString()).substring(1) + " is not safe");


                            }
                        } else {
                            System.out.println(getFileName("file:" + e.toAbsolutePath().toString()).substring(1) + " contains SWRL Rules and is therefore not handled");
                        }

                    } else {
                        System.err.println("The file " + e.toAbsolutePath().toString()
                                + " isn't a .owl file, therefore, it wont be loaded");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    failed.add(e.toAbsolutePath().toString() + " failed\n");
                }
            });


            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the filename for a given input ontology
     *
     * @param input_ontology
     * @return Filename
     */
    private static String getFileName(String input_ontology) {
        String pre = input_ontology.substring(input_ontology.lastIndexOf("/"));
        return pre.substring(0, pre.lastIndexOf('.')).replace(", ]", "]");
    }

}
