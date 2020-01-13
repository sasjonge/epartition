package uni.sasjonge.utils;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Iterator;
import java.util.Objects;

public class SafetyChecker {

    /**
     * Class implementing the locality checker of
     *
     * @author Sascha Jongebloed
     */
    public class SafetyVisitor implements OWLAxiomVisitor {

        boolean isSafe = true;

        public boolean isSafe() {
            return isSafe;
        }

        public SafetyVisitor(OWLOntology ontology) {
            // Check safety of all logical axioms
            ontology.logicalAxioms(Imports.INCLUDED).forEach(ax -> {

                ax.accept(this);


            });
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

    private class LocalityVisitor implements OWLClassExpressionVisitor {

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
            LocalityVisitor compl = new LocalityVisitor(ce);
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

    public boolean isSafe(OWLOntology ontology) {
        return (new SafetyVisitor(ontology)).isSafe();

    }
}