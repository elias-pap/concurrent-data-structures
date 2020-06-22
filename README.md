# Concurrent Data Structures

## Original Implementations
### Persistent Non-Blocking Binary Search Tree (PNB-BST)
(LockFreePBSTMap.java)  
This is an implementation of PNB-BST algorithm described in the paper
"Persistent Non-Blocking Binary Search Trees Supporting Wait-Free Range Queries"
of Panagiota Fatourou, Elias Papavasileiou and Eric Ruppert.

### Batched Persistent Non-Blocking Binary Search Tree (BPNB-BST)
(LockFreeBPBSTMap.java)  
This is an optimized version of PNB-BST algorithm that supports key batching in the leafs.

# Benchmark suite

## Workflow
To launch an experiment:
1. `./compile`
1. `./run-experiments`

After the experiment:
1. `./scrarchive.sh`
1. `./scrmerge.sh`
1. `./scr.py`
1. Open the generated .csv file in "experiments" folder, and copy the generated table in "toplot.txt"
1. Tweak the appropriate plot\_script file (e.g. `nano plot_script_1.gp`)
1. Launch gnuplot and give the appropriate command (e.g. `load "plot_script_1.gp"`) 


## Contents
Many different versions of the same file are included in some cases. Usually, the last modified is the one currently used. Using `git diff -w --no-index file1 file2`, one can see the changes between versions.

	- src
	Java source code files.

	- adapters
	Adapters for concurrent data structures.

	- algorithms/published
	Contains all java source code files of our algorithms used in the experiments of the paper "Persistent Non-Blocking Binary Search Trees Supporting Wait-Free Range Queries".

	- main
	Main function and Globals file.

	- compile
	Compiles every .java source file.

	- merge
	Merges .csv files.

	- plot_script_*.gp
	Gnuplot scripts used to generate the graphs of the experiments.

	- plot_script_cm.gp
	Gnuplot script that plots the cache misses per operation.
	
	- plot_script_mem.gp
	Gnuplot script that plots the memory usage over time.

	- README.txt
	Useful README file provided by Trevor Brown. The original version can be found here: https://bitbucket.org/trbot86/implementations/src/master/java/README.txt

	- run-experiments*
	Scripts used to run the experiments. Memory usage and cache misses are measured with these scripts as well.

	- scr.py
	Constructs a new .csv file containing the throughput results of the experiment in a table format.

	- scrarchive.sh
	Makes a backup of all .csv and .txt files contained in build/ folder.

	- scrcm.py
	Constructs a new .csv file containing the cache-misses results of the experiment in a table format.

	- scrmem.py
		Constructs a new .csv file containing the memory usage results of the experiment in a table format.

	- scrmerge.sh
	Merges .csv files. Wrapper of ./merge.

	- toplot.txt
	A .txt file used as input to gnuplot, to generate a plot.

## Acknowledgements
[Trevor Brown](https://bitbucket.org/trbot86/) provided the implementation of [Non-Blocking Binary Search Tree](https://bitbucket.org/trbot86/implementations/src/6dbe554bbea072bdd5ec344a2653d93cd502d5a8/java/src/algorithms/published/LockFreeBSTMap.java) (NB-BST) on which the implementation of PNB-BST was based on. The benchmark suite is also based on his [benchmark code](https://bitbucket.org/trbot86/implementations/src/master/java/).
