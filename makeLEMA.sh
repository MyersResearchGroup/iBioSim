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
cp bin/*.py /Applications/LEMA.app/Contents/Resources/bin/.
cp bin/*.pyc /Applications/LEMA.app/Contents/Resources/bin/.
cp bin/atacs /Applications/LEMA.app/Contents/Resources/bin/.
cp lib/*.dylib /Applications/LEMA.app/Contents/Resources/lib/.
cp lib/*.jnilib /Applications/LEMA.app/Contents/Resources/lib/.
cp docs/*.html /Applications/LEMA.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/LEMA.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/LEMA.app/Contents/Resources/docs/screenshots/.
cp gui/lib/* /Applications/LEMA.app/Contents/Resources/gui/lib/.
cp gui/icons/* /Applications/LEMA.app/Contents/Resources/gui/icons/.
cp gui/bin/* /Applications/LEMA.app/Contents/Resources/gui/bin/.
cp gui/dist/lib/* /Applications/LEMA.app/Contents/Resources/gui/dist/lib/.
cp -r gui/dist/classes/* /Applications/LEMA.app/Contents/Resources/gui/dist/classes/.


