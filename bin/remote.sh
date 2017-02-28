#!/bin/sh
#*******************************************************************************
#  
# This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
# for the latest version of iBioSim.
#
# Copyright (C) 2017 University of Utah
#
# This library is free software; you can redistribute it and/or modify it
# under the terms of the Apache License. A copy of the license agreement is provided
# in the file named "LICENSE.txt" included with this software distribution
# and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
#  
#*******************************************************************************
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
