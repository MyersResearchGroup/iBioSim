#Makefile for tools

all: geneNet Reb2Sac Gui

geneNet: FORCE
	make -C GeneNet

Reb2Sac: FORCE
	cd reb2sac; ./install-reb2sac-linux.sh

Gui: FORCE
	cd gui; ant

.PHONY: clean

clean: FORCE
	make -C GeneNet clean
	make -C reb2sac distclean
	rm $(shell cat .cvsignore)

FORCE:




