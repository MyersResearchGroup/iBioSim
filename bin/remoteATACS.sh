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
scp $3.lpn $1:/tmp/.
ssh $1 "atacs $2 /tmp/$3.lpn"
scp $1:"atacs.log" .
scp $1:/tmp/$3.prg .
ssh $1 "rm /tmp/$3.prg"
