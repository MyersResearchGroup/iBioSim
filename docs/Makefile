LATEX = latex
BIBTEX = bibtex
DOT2PS = dot -T ps
DVI2PS = dvips
DVI2PDF = dvipdf
DVIFLAGS = -t letter
PDFLATEX = pdflatex

all: iBioSim.html tutorial.html install.html LEMA.html ATACS.html

iBioSim.pdf: iBioSim.tex
	latex iBioSim
#	bibtex main
#	latex main
	latex iBioSim
	dvips -Ppdf -o iBioSim.ps -t letter iBioSim
	ps2pdf iBioSim.ps

iBioSim.html: iBioSim.pdf
	tth -u -e2 -LiBioSim < iBioSim.tex > iBioSim.html

LEMA.pdf: LEMA.tex
	latex LEMA
#	bibtex main
#	latex main
	latex LEMA
	dvips -Ppdf -o LEMA.ps -t letter LEMA
	ps2pdf LEMA.ps

LEMA.html: LEMA.pdf
	tth -u -e2 -LLEMA < LEMA.tex > LEMA.html

ATACS.pdf: ATACS.tex
	latex ATACS
#	bibtex main
#	latex main
	latex ATACS
	dvips -Ppdf -o ATACS.ps -t letter ATACS
	ps2pdf ATACS.ps

ATACS.html: ATACS.pdf
	tth -u -e2 -LATACS < ATACS.tex > ATACS.html

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

