package uni.sasjonge.utils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uni.sasjonge.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UniversalRoleAnalyzer {

    public static void main(String[] args) {
        // Read all files in the given directory
        try (Stream<Path> paths = Files.walk(Paths.get(Settings.ONTOLOGIES_DIRECTORY))) {

            List<String> failed = new LinkedList<>();

            // Save the statistics for the partitioning in a file
            File fout = new File(Settings.EVALUATION_OUTPUT_FILE);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("\\begin{table} \\caption {Syntax and semantic of \\SROIQD constructors~\\cite{KSH13,HS01}} \\label{tab:synAndSemOfSubCon} " +
                    "\n \\begin{tabu} to \\textwidth {X[l]X[c]X[c2]} " +
                    "\n Name & Syntax & Semantics \\ \\tabucline[1pt]{1-3})");

            Map<String, int[]> ontToData = new HashMap<>();

            paths.filter(Files::isRegularFile).forEach(e -> {
                try {
                    // Load and partition the ontology
                    if (e.toAbsolutePath().toString().endsWith(".owl") || e.toAbsolutePath().toString().endsWith(".xml")) {
                        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                        OWLDataFactory df = manager.getOWLDataFactory();
                        OWLOntology ontology = manager.loadOntology(IRI.create("file:" + e.toAbsolutePath().toString()));

                        int numOfUniversalRoleAxioms = ontology.referencingAxioms(df.getOWLTopObjectProperty())
                                .collect(Collectors.toList()).size();

                        int numOfLogicalAxioms = ontology.getLogicalAxiomCount();

                        ontToData.put(getFileName("file:" + e.toAbsolutePath().toString()).substring(1), new int[]{numOfLogicalAxioms, numOfUniversalRoleAxioms});

                    } else {
                        System.err.println("The file " + e.toAbsolutePath().toString()
                                + " isn't a .owl file, therefore, it wont be loaded");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    failed.add(e.toAbsolutePath().toString() + " failed\n");
                }
            });

            List<Map.Entry<String, int[]>> list = new ArrayList<>(ontToData.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<String, int[]>>() {
                @Override
                public int compare(Map.Entry<String, int[]> stringEntry, Map.Entry<String, int[]> t1) {
                    return stringEntry.getValue()[0] - t1.getValue()[0];
                }
            });

            for (Map.Entry<String, int[]> e : list) {
                if (e.getValue()[1] > 0) {
                    bw.write(e.getKey() + " & " + e.getValue()[0] + " & " + e.getValue()[1] + " & " + e.getValue()[1] / e.getValue()[0] + "\\\\ \\hline" + "\n");
                    bw.flush();
                }
            }

            bw.write("\\end{tabu}" + "\\n" + "\\end{table}");
            bw.write("\n");
            for (String failure : failed) {
                bw.write(failure);
            }
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
