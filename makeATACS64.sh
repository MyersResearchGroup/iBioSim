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
cp bin/*.py /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/*.pyc /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/atacs /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/parg /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/reb2sac.mac64 /Applications/iBioSim.app/Contents/Resources/bin/reb2sac
cp lib64/*.dylib /Applications/ATACS.app/Contents/Resources/lib/.
cp lib64/*.jnilib /Applications/ATACS.app/Contents/Resources/lib/.
cp docs/*.html /Applications/ATACS.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/ATACS.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/ATACS.app/Contents/Resources/docs/screenshots/.
cp gui/lib/* /Applications/ATACS.app/Contents/Resources/gui/lib/.
cp gui/icons/* /Applications/ATACS.app/Contents/Resources/gui/icons/.
cp gui/icons/modelview/* /Applications/ATACS.app/Contents/Resources/gui/icons/modelview/.
cp gui/icons/modelview/movie/* /Applications/ATACS.app/Contents/Resources/gui/icons/modelview/movie/.
cp gui/bin/* /Applications/ATACS.app/Contents/Resources/gui/bin/.
cp -r gui/dist/classes/* /Applications/ATACS.app/Contents/Resources/gui/dist/classes/.


