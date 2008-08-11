LATEX = latex
BIBTEX = bibtex
DOT2PS = dot -T ps
DVI2PS = dvips
DVI2PDF = dvipdf
DVIFLAGS = -t letter
PDFLATEX = pdflatex

all: iBioSim.html tutorial.html install.html

iBioSim.pdf: iBioSim.tex
	latex iBioSim
#	bibtex main
#	latex main
	latex iBioSim
	dvips -Ppdf -o iBioSim.ps -t letter iBioSim
	ps2pdf iBioSim.ps

iBioSim.html: iBioSim.pdf
	tth -u -e2 -LiBioSim < iBioSim.tex > iBioSim.html

tutorial.pdf: tutorial.tex
	latex tutorial
#	bibtex main
#	latex main
	latex tutorial
	dvips -Ppdf -o tutorial.ps -t letter tutorial
	ps2pdf tutorial.ps

tutorial.html: tutorial.pdf
	tth -u -e2 -Ltutorial < tutorial.tex > tutorial.html

install.pdf: install.tex
	latex install
#	bibtex main
#	latex main
	latex install
	dvips -Ppdf -o install.ps -t letter install
	ps2pdf install.ps

install.html: install.pdf
	tth -u -e2 -Linstall < install.tex > install.html

clean:
	rm *.dvi *.ps

