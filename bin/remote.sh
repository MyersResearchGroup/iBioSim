#!/bin/sh
scp $2.xml $1:/tmp/.
scp $2.properties $1:/tmp/.
ssh $1 "reb2sac --target.encoding=$3 /tmp/$2.xml"
scp $1:"*.tsd" .
ssh $1 "rm *.tsd"
if [ "scp $1:sim-rep.txt ." ]; then
    echo "No sim-rep.txt file"
else 
    ssh $1 "rm sim-rep.txt"
fi
