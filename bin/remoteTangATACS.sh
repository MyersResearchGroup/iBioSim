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
scp $2.lpn tang.ece.utah.edu:/tmp/.
ssh tang.ece.utah.edu "/home/tang/myers/BioSim/bin/atacs $1 /tmp/$2.lpn"
scp tang.ece.utah.edu:"atacs.log" .
scp tang.ece.utah.edu:/tmp/$2.prg .
ssh tang.ece.utah.edu "rm /tmp/$2.prg"
