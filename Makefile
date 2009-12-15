#Makefile for tools

all: geneNet Reb2Sac s2lpn Gui

geneNet: FORCE
	make -C GeneNet

s2lpn: FORCE
	make -C s2lpn/src

Reb2Sac: FORCE
	cd reb2sac; ./install-reb2sac-linux.sh

Gui: FORCE
	cd gui; ant

.PHONY: clean

clean: FORCE
	make -C GeneNet clean
	make -C s2lpn/src clean
	make -C reb2sac distclean
	rm $(shell cat .cvsignore)

FORCE:




