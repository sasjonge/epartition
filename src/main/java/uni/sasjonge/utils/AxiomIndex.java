package uni.sasjonge.utils;

import org.semanticweb.owlapi.model.AxiomType;

public abstract class AxiomIndex {
	
    /** Declaration. */                     public static final int                      DECLARATION = AxiomType.DECLARATION.getIndex();
    /** EquivalentClasses. */               public static final int                EQUIVALENT_CLASSES = AxiomType.EQUIVALENT_CLASSES.getIndex();
    /** SubClassOf. */                      public static final int                       SUBCLASS_OF = AxiomType.SUBCLASS_OF.getIndex();
    /** DisjointClasses. */                 public static final int                  DISJOINT_CLASSES = AxiomType.DISJOINT_CLASSES.getIndex();
    /** DisjointUnion. */                   public static final int                    DISJOINT_UNION = AxiomType.DISJOINT_UNION.getIndex();
    /** ClassAssertion. */                  public static final int                   CLASS_ASSERTION = AxiomType.CLASS_ASSERTION.getIndex();
    /** SameIndividual. */                  public static final int                   SAME_INDIVIDUAL = AxiomType.SAME_INDIVIDUAL.getIndex();
    /** DifferentIndividuals. */            public static final int             DIFFERENT_INDIVIDUALS = AxiomType.DIFFERENT_INDIVIDUALS.getIndex();
    /** ObjectPropertyAssertion. */         public static final int          OBJECT_PROPERTY_ASSERTION = AxiomType.OBJECT_PROPERTY_ASSERTION.getIndex();
    /** NegativeObjectPropertyAssertion. */ public static final int  NEGATIVE_OBJECT_PROPERTY_ASSERTION = AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION.getIndex();
    /** DataPropertyAssertion. */           public static final int            DATA_PROPERTY_ASSERTION = AxiomType.DATA_PROPERTY_ASSERTION.getIndex();
    /** NegativeDataPropertyAssertion. */   public static final int    NEGATIVE_DATA_PROPERTY_ASSERTION = AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION.getIndex();
    /** EquivalentObjectProperties. */      public static final int       EQUIVALENT_OBJECT_PROPERTIES = AxiomType.EQUIVALENT_OBJECT_PROPERTIES.getIndex();
    /** SubObjectPropertyOf. */             public static final int              SUB_OBJECT_PROPERTY = AxiomType.SUB_OBJECT_PROPERTY.getIndex();
    /** InverseObjectProperties. */         public static final int          INVERSE_OBJECT_PROPERTIES = AxiomType.INVERSE_OBJECT_PROPERTIES.getIndex();
    /** FunctionalObjectProperty. */        public static final int         FUNCTIONAL_OBJECT_PROPERTY = AxiomType.FUNCTIONAL_OBJECT_PROPERTY.getIndex();
    /** InverseFunctionalObjectProperty. */ public static final int  INVERSE_FUNCTIONAL_OBJECT_PROPERTY = AxiomType.SUB_OBJECT_PROPERTY.getIndex();
    /** SymmetricObjectProperty. */         public static final int          SYMMETRIC_OBJECT_PROPERTY = AxiomType.SYMMETRIC_OBJECT_PROPERTY.getIndex();
    /** AsymmetricObjectProperty. */        public static final int         ASYMMETRIC_OBJECT_PROPERTY = AxiomType.ASYMMETRIC_OBJECT_PROPERTY.getIndex();
    /** TransitiveObjectProperty. */        public static final int         TRANSITIVE_OBJECT_PROPERTY = AxiomType.TRANSITIVE_OBJECT_PROPERTY.getIndex();
    /** ReflexiveObjectProperty. */         public static final int          REFLEXIVE_OBJECT_PROPERTY = AxiomType.REFLEXIVE_OBJECT_PROPERTY.getIndex();
    /** IrreflexiveObjectProperty. */       public static final int        IRREFLEXIVE_OBJECT_PROPERTY = AxiomType.IRREFLEXIVE_OBJECT_PROPERTY.getIndex();
    /** ObjectPropertyDomain. */            public static final int             OBJECT_PROPERTY_DOMAIN = AxiomType.OBJECT_PROPERTY_DOMAIN.getIndex();
    /** ObjectPropertyRange. */             public static final int              OBJECT_PROPERTY_RANGE = AxiomType.OBJECT_PROPERTY_RANGE.getIndex();
    /** DisjointObjectProperties. */        public static final int         DISJOINT_OBJECT_PROPERTIES = AxiomType.DISJOINT_OBJECT_PROPERTIES.getIndex();
    /** SubPropertyChainOf. */              public static final int               SUB_PROPERTY_CHAIN_OF = AxiomType.SUB_PROPERTY_CHAIN_OF.getIndex();
    /** EquivalentDataProperties. */        public static final int         EQUIVALENT_DATA_PROPERTIES = AxiomType.EQUIVALENT_DATA_PROPERTIES.getIndex();
    /** SubDataPropertyOf. */               public static final int                SUB_DATA_PROPERTY = AxiomType.SUB_DATA_PROPERTY.getIndex();
    /** FunctionalDataProperty. */          public static final int           FUNCTIONAL_DATA_PROPERTY = AxiomType.FUNCTIONAL_DATA_PROPERTY.getIndex();
    /** DataPropertyDomain. */              public static final int               DATA_PROPERTY_DOMAIN = AxiomType.DATA_PROPERTY_DOMAIN.getIndex();
    /** DataPropertyRange. */               public static final int                DATA_PROPERTY_RANGE = AxiomType.DATA_PROPERTY_RANGE.getIndex();
    /** DisjointDataProperties. */          public static final int           DISJOINT_DATA_PROPERTIES = AxiomType.DISJOINT_DATA_PROPERTIES.getIndex();
    /** DatatypeDefinition. */              public static final int               DATATYPE_DEFINITION = AxiomType.DATATYPE_DEFINITION.getIndex();
    /** HasKey. */                          public static final int                           HAS_KEY = AxiomType.HAS_KEY.getIndex();
    /** Rule. */                            public static final int                                 SWRL_RULE = AxiomType.SWRL_RULE.getIndex();
    /** AnnotationAssertion. */             public static final int              ANNOTATION_ASSERTION = AxiomType.ANNOTATION_ASSERTION.getIndex();
    /** SubAnnotationPropertyOf. */         public static final int          SUB_ANNOTATION_PROPERTY_OF = AxiomType.SUB_ANNOTATION_PROPERTY_OF.getIndex();
    /** AnnotationPropertyRangeOf. */       public static final int          ANNOTATION_PROPERTY_RANGE = AxiomType.ANNOTATION_PROPERTY_RANGE.getIndex();
    /** AnnotationPropertyDomain. */        public static final int         ANNOTATION_PROPERTY_DOMAIN = AxiomType.ANNOTATION_PROPERTY_DOMAIN.getIndex();

}
