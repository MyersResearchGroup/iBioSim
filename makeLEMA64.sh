#!/bin/sh
cp bin/*.py /Applications/LEMA.app/Contents/Resources/bin/.
cp bin/*.pyc /Applications/LEMA.app/Contents/Resources/bin/.
cp bin/reb2sac.mac64 /Applications/iBioSim.app/Contents/Resources/bin/reb2sac
cp bin/atacs /Applications/LEMA.app/Contents/Resources/bin/.
cp lib64/*.dylib /Applications/LEMA.app/Contents/Resources/lib/.
cp lib64/*.jnilib /Applications/LEMA.app/Contents/Resources/lib/.
cp docs/*.html /Applications/LEMA.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/LEMA.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/LEMA.app/Contents/Resources/docs/screenshots/.
cp gui/lib/* /Applications/LEMA.app/Contents/Resources/gui/lib/.
cp gui/icons/* /Applications/LEMA.app/Contents/Resources/gui/icons/.
cp gui/icons/modelview/* /Applications/ATACS.app/Contents/Resources/gui/icons/modelview/.
cp gui/icons/modelview/movie/* /Applications/ATACS.app/Contents/Resources/gui/icons/modelview/movie/.
cp gui/bin/* /Applications/LEMA.app/Contents/Resources/gui/bin/.
cp gui/dist/lib/* /Applications/LEMA.app/Contents/Resources/gui/dist/lib/.
cp -r gui/dist/classes/* /Applications/LEMA.app/Contents/Resources/gui/dist/classes/.


