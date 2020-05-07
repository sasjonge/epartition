# EPartition

This project is work in progress. It's an implementation and extension / improvement of (Jongebloed S., Schneider T. 2018) for my master thesis.

## Structure

The src files are structured as follows

    ├── ...
    ├── src/main/java
    │   ├── /uni/sasjonge                  
    │   │   ├── Partitioner.java    # The main class to run the project
    │   │   ├── Settings.java       # Contains all important settings for this project
    │   │   │ 				        # visualization options, heuristic options etc.  
    │   │   ├── heuristic           # Contains heuristics to reduce the coarseness of the results 
    │   │   ├── partitioning        # Contains the main partitioning algortihm
    │   │   └── utils               # Contains utility tools, especially for creating an output graph and for labelling
    │   └── /cwts				    # Data structures and algorithms for network analysis
    ├── src/test/java               # Unit-Tests
    └── ...

/cwts contains a copy of the source files of [this implementation](https://github.com/CWTSLeiden/networkanalysis) by the CWTS Leiden.

## References

S. Jongebloed and T. Schneider: Ontology Partitioning Using E-Connections Revisited. In Proc. 31st DL, vol. 2211 of CEUR, 2018. [PDF](http://www.informatik.uni-bremen.de/tdki/research/papers/2018/JS-DL18.pdf)

