# EPartition

This project is work in progress. It's an extension / improvement of (Jongebloed S., Schneider T. 2018) for my master thesis.

## Structure

The src files are structured as follows

    ├── ...
    ├── src                     
    │   ├── Partitioner.java    # The main class to run the project
    │   ├── Settings.java       # Contains all important settings for this project: In- and output files, output graph  
    │   │ 				   # visualization options, heuristic options etc.  
    │   ├── heuristic           # Contains heuristics to reduce the coarseness of the results 
    │   ├── partitioning        # Contains the main partitioning algortihm
    │   └── utils               # Contains utility tools, especially for creating an output graph and for labelling
    └── ...

## Usage

I'm currently not offering an executable for this project. Therefore you have to run this project. To set the input directory (containig the ontologie in .owl format) and the output file (.graphml graph) and other settings like the heuristics to use or the way the output is visualized, have a look at the Settings.java file.

## References

S. Jongebloed and T. Schneider: Ontology Partitioning Using E-Connections Revisited. In Proc. 31st DL, vol. 2211 of CEUR, 2018. [PDF](http://www.informatik.uni-bremen.de/tdki/research/papers/2018/JS-DL18.pdf)

