# ConUp
ConUp is a prototype framework that allows for safe, timely, and low-disruptive dynamic updates of components on top of the SCA (Service Component Architecture) component model. ConUp extends [Apache Tuscany](http://tuscany.apache.org/), a well-known lightweight implementation of the SCA specification. 

ConUp supports Quiescence, Tranquility, or Version Consistency, as possible approaches for updating components at runtime, and BF, CV, or WF as strategies for achieving freeness. The idea was originally published as a paper titled ["Version-consistent Dynamic Reconfiguration of Component-based Distributed Systems"](docs/paper/ESEC-FSE11.pdf), but [this paper](docs/paper/journal.pdf) gives an updated and more comprehensive description. The algorithms used were provided in the appendix of an early [technical report](docs/paper/vcdu-TR.pdf).   

We formalized the idea of version consistency by means of a graph transformation system, and use [GROOVE](http://groove.cs.utwente.nl/about/) as graph transformation modeling and verification tool to prove the correctness of our approach and to implement VCC (Version Consistency Checker), a viable solution for reasoning on dynamic updates; The complete GROOVE specification, the associated proofs, and the version consistency checking tool can be found at [here](docs/GraphGrammar/vc-modelchecking.zip).


[This document](docs/experimental_results/UpdateEachComponent.pdf) presents the experimental result for the dynamic update of each component in the travel sample system. 
