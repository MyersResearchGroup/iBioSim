#Makefile for tools

all: geneNet Reb2Sac Gui

geneNet:
	make -C GeneNet

Reb2Sac:
	cd reb2sac; ./install-reb2sac-linux.sh

Gui:
	cd gui; ant

.PHONY: clean

clean:
	rm $(shell cat .cvsignore)
	make -C GeneNet clean
	make -C reb2sac clean



