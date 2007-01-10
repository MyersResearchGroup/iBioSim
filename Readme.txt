Set the environment variable BIOSIM to the directory where you have installed
the program.  For example,

setenv BIOSIM <path>/BioModelSim

Put the BioModelSim/bin directory in your path.  For example,

set path = ($path $BIOSIM/bin)

Add ${BIOSIM}/lib to your LD_LIBRARY_PATH environment variable.  For example,

setenv LD_LIBRARY_PATH ${BIOSIM}/lib:$LD_LIBRARY_PATH

Make sure that JAVA_HOME is set to point to a 1.5 version of java.

The command BioSim will start the gui.

You can generate results for our Bioinformatics papers from the command line
as follows:

1) To run GeneNet on the examples for the Bioinformatics paper found in
the GeneNet directory, cd to the $BIOSIM/examples directory and run 'make
GeneNet06.'  GeneNet can also be run individually by calling GeneNet
and giving it a directory with tsd files named run-[1->inf].tsd.

2) To run fim-switch model using reb2sac:
Type reb2sac to see if reb2sac is set up right.
Then, go to the example directory (i.e., ${BIOSIM}/examples), and type
make fim.  After a successful run, plots are generated under fim-switch 
directory.  Note that this may take more than 20 hours, depending on your
computation environment.

 
