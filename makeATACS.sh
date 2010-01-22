#!/bin/sh
cp bin/*.py /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/*.pyc /Applications/ATACS.app/Contents/Resources/bin/.
cp bin/atacs /Applications/ATACS.app/Contents/Resources/bin/.
cp lib/*.dylib /Applications/ATACS.app/Contents/Resources/lib/.
cp lib/*.jnilib /Applications/ATACS.app/Contents/Resources/lib/.
cp docs/*.html /Applications/ATACS.app/Contents/Resources/docs/.
cp docs/*.pdf /Applications/ATACS.app/Contents/Resources/docs/.
cp docs/screenshots /Applications/ATACS.app/Contents/Resources/docs/screenshots/.
cp gui/lib/* /Applications/ATACS.app/Contents/Resources/gui/lib/.
cp gui/icons/* /Applications/ATACS.app/Contents/Resources/gui/icons/.
cp gui/bin/* /Applications/ATACS.app/Contents/Resources/gui/bin/.
cp gui/dist/lib/* /Applications/ATACS.app/Contents/Resources/gui/dist/lib/.
cp -r gui/dist/classes/* /Applications/ATACS.app/Contents/Resources/gui/dist/classes/.


