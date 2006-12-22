Readme.txt

To run GeneNet on the examples for the Bioinformatics paper found in
the GeneNet directory, cd to the examples directory and run 'make
GeneNet06.'  GeneNet can also be run individually by calling GeneNet
and giving it a directory with tsd files named run-[1->inf].tsd.


* To run fim-switch model using reb2sac:
First, add ${BMS_HOME}/lib to your LD_LIBRARY_PATH environment variable
where ${BMS_HOME} is the path to BioModelSim.  
Second, add ${BMS_HOME}/bin to your PATH environment variable.
Type reb2sac to see if reb2sac is set up right.
Then, go to the example directory (i.e., ${BMS_HOME}/example), and type
make fim.  After a successful run, plots are generated under fim-switch directory.
Note that this usually take more than 20 hours.

 
