#Makefile for tools

GENENET_EXE = bin/GeneNet_fake


all: $(GENENET_EXE)

$(GENENET_EXE):
	make -C GeneNet

.PHONY: clean

clean:
	rm $(shell cat .cvsignore)
	make -C GeneNet clean


