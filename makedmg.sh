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
cp bin/*.pl /Applications/iBioSim.app/Contents/Resources/bin/.
cp bin/reb2sac /Applications/iBioSim.app/Contents/Resources/bin/.
cp bin/GeneNet /Applications/iBioSim.app/Contents/Resources/bin/.
cp lib/*.dylib /Applications/iBioSim.app/Contents/Resources/lib/.
cp lib/*.jnilib /Applications/iBioSim.app/Contents/Resources/lib/.
cp docs/*.html /Applications/iBioSim.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/iBioSim.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/iBioSim.app/Contents/Resources/docs/screenshots/.
cp gui/lib/* /Applications/iBioSim.app/Contents/Resources/gui/lib/.
cp gui/icons/* /Applications/iBioSim.app/Contents/Resources/gui/icons/.
cp gui/icons/modelview/* /Applications/iBioSim.app/Contents/Resources/gui/icons/modelview/.
cp gui/icons/modelview/movie/* /Applications/iBioSim.app/Contents/Resources/gui/icons/modelview/movie/.
cp gui/bin/* /Applications/iBioSim.app/Contents/Resources/gui/bin/.
#cp gui/dist/lib/* /Applications/iBioSim.app/Contents/Resources/gui/dist/lib/.
cp -r gui/dist/classes/* /Applications/iBioSim.app/Contents/Resources/gui/dist/classes/.


