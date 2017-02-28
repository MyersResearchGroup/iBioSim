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
cp bin/reb2sac.mac64 /Applications/iBioSim.app/Contents/Resources/bin/reb2sac
cp bin/GeneNet.mac64 /Applications/iBioSim.app/Contents/Resources/bin/GeneNet
cp lib64/*.dylib /Applications/iBioSim.app/Contents/Resources/lib/.
cp lib64/*.jnilib /Applications/iBioSim.app/Contents/Resources/lib/.
cp docs/*.html /Applications/iBioSim.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/iBioSim.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/iBioSim.app/Contents/Resources/docs/screenshots/.
cp -r docs/SynBioTutorial/SBOL /Applications/iBioSim.app/Contents/Resources/docs/SynBioTutorial/.
cp -r docs/SynBioTutorial/SBMLTestSuite/0000?  /Applications/iBioSim.app/Contents/Resources/docs/SynBioTutorial/SBMLTestSuite/.
cp -r docs/SynBioTutorial/SBMLTestSuite/011??  /Applications/iBioSim.app/Contents/Resources/docs/SynBioTutorial/SBMLTestSuite/.
cp gui/lib/* /Applications/iBioSim.app/Contents/Resources/gui/lib/.
cp gui/lib/libsbmlj.jar /Applications/iBioSim.app/Contents/Resources/gui/lib/libsbmlj.jar
cp gui/icons/* /Applications/iBioSim.app/Contents/Resources/gui/icons/.
cp gui/icons/modelview/* /Applications/iBioSim.app/Contents/Resources/gui/icons/modelview/.
cp gui/icons/modelview/movie/* /Applications/iBioSim.app/Contents/Resources/gui/icons/modelview/movie/.
#cp gui/bin/* /Applications/iBioSim.app/Contents/Resources/gui/bin/.
#cp gui/dist/lib/* /Applications/iBioSim.app/Contents/Resources/gui/dist/lib/.
cp -r gui/dist/classes/* /Applications/iBioSim.app/Contents/Resources/gui/dist/classes/.


