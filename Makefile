#Makefile for tools

all: geneNet Reb2Sac

geneNet:
	make -C GeneNet

Reb2Sac:
	cd reb2sac; ./install-reb2sac-linux.sh

.PHONY: clean

clean:
	rm $(shell cat .cvsignore)
	make -C GeneNet clean


