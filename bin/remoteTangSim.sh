#!/bin/sh
scp $1.xml tang.ece.utah.edu:/tmp/.
scp $1.properties tang.ece.utah.edu:/tmp/.
ssh tang.ece.utah.edu "setenv LD_LIBRARY_PATH /home/tang/myers/BioSim/lib64;/home/tang/myers/BioSim/bin/reb2sac --target.encoding=$2 /tmp/$1.xml"
scp tang.ece.utah.edu:"*.tsd" .
ssh tang.ece.utah.edu "rm *.tsd"
if [ "scp tang.ece.utah.edu:sim-rep.txt ." ]; then
    echo "No sim-rep.txt file"
else 
    ssh tang.ece.utah.edu "rm sim-rep.txt"
fi
