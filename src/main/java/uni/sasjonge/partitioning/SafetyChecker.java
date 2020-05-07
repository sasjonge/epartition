package uni.sasjonge.partitioning;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Iterator;
import java.util.Objects;

public class SafetyChecker {

    /**
     * Class implementing the locality checker
     * Bernardo Cuenca Grau, Bijan Parsia, Evren Sirin, Aditya Kalyanpur.
     * "Automatic Partitioning of OWL Ontologies Using E-Connections". Description Logics 2005
     * https://www.research.manchester.ac.uk/portal/en/publications/automatic-partitioning-of-owl-ontologies-using-econnections(93290743-02b7-454d-9d85-739d625826ec).html
     * @author Sascha Jongebloed
     */
    public static class SafetyVisitor implements OWLAxiomVisitor {

        boolean isSafe = true;

        public boolean isSafe() {
            return isSafe;
        }


        public void SafetyVisitor(OWLAxiom ax) {
            ax.accept(this);
        }


        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            isSafe = !(new LocalityVisitor(axiom.getSuperClass())).isLocal || (new LocalityVisitor(axiom.getSubClass())).isLocal;
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            Iterator<OWLClassExpression> iter = axiom.getOperandsAsList().iterator();

            // This axiom is safe if all operands are either not local or local
            boolean localityOfFirst = (new LocalityVisitor(iter.next())).isLocal;

            while (iter.hasNext()) {
                if ((new LocalityVisitor(iter.next())).isLocal != localityOfFirst) {
                    isSafe = false;
                    break;
                }
            }
        }

        @Override
        public void visit(SWRLRule node) {
            throw new UnsupportedOperationException("SWRLRules aren't implemented in this version");
        }

        @Override
        public void getDefaultReturnValue(Object object) {
            isSafe = true;
        }

        @Override
        public void doDefault(Object object) {
            isSafe = true;
        }

        @Override
        public void handleDefault(Object c) {
            isSafe = true;
        }
    }

    /**
     * A visitor to deice if a concept is local
     */
    private static class LocalityVisitor implements OWLClassExpressionVisitor {

        boolean isLocal = true;

        public LocalityVisitor(OWLClassExpression cE) {
            cE.accept(this);
        }

        @Override
        public void visit(OWLObjectIntersectionOf ce) {
            boolean atLeastOneLocal = false;
            for (OWLClassExpression opCe : ce.getOperandsAsList()) {
                if ((new LocalityVisitor(opCe)).isLocal) {
                    atLeastOneLocal = true;
                    break;
                }
            }
            isLocal = atLeastOneLocal;
        }

        @Override
        public void visit(OWLObjectUnionOf ce) {
            for (OWLClassExpression opCe : ce.getOperandsAsList()) {
                if (!(new LocalityVisitor(opCe)).isLocal) {
                    isLocal = false;
                    break;
                }
            }
        }

        @Override
        public void visit(OWLObjectComplementOf ce) {
            LocalityVisitor compl = new LocalityVisitor(ce.getOperand());
            isLocal = !compl.isLocal;
        }

        @Override
        public void visit(OWLObjectSomeValuesFrom ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLObjectAllValuesFrom ce) {
            isLocal = false;
        }

        @Override
        public void visit(OWLObjectHasValue ce) {
            isLocal = true;
        }

        // TODO: Min und Max Cardinality: Cardinality 0 -> non-local
        @Override
        public void visit(OWLObjectMinCardinality ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLObjectExactCardinality ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLObjectMaxCardinality ce) {
            isLocal = false;
        }

        @Override
        public void visit(OWLObjectHasSelf ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLObjectOneOf ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLDataSomeValuesFrom ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLDataAllValuesFrom ce) {
            isLocal = false;
        }

        @Override
        public void visit(OWLDataHasValue ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLDataMinCardinality ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLDataExactCardinality ce) {
            isLocal = true;
        }

        @Override
        public void visit(OWLDataMaxCardinality ce) {
            isLocal = false;
        }

        @Override
        public void visit(OWLClass ce) {
            isLocal = true;
        }

        @Override
        public void getDefaultReturnValue(Object object) {
            isLocal = true;
        }

        @Override
        public void doDefault(Object object) {
            isLocal = true;
        }

        @Override
        public void handleDefault(Object c) {
            isLocal = true;
        }
    }

    /**
     * Decide if an ontology is safe
     * @param ontology The input ontoloy
     * @return true if the ontology is safe
     */
    public static boolean isSafe(OWLOntology ontology) {

        if (ontology.getLogicalAxiomCount() > 0) {

            return !ontology.logicalAxioms(Imports.INCLUDED).anyMatch(ax -> {
                SafetyVisitor sv = new SafetyVisitor();
                ax.accept(sv);
                return !sv.isSafe;
            });
        } else {
            return true;
        }
    }
}