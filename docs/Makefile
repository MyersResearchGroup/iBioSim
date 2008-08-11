LATEX = latex
BIBTEX = bibtex
DOT2PS = dot -T ps
DVI2PS = dvips
DVI2PDF = dvipdf
DVIFLAGS = -t letter
PDFLATEX = pdflatex

all: iBioSim.html

iBioSim.pdf: iBioSim.tex
	latex iBioSim
#	bibtex main
#	latex main
	latex iBioSim
	dvips -Ppdf -o iBioSim.ps -t letter iBioSim
	ps2pdf iBioSim.ps

iBioSim.html: iBioSim.pdf
	tth -u -e2 -LiBioSim < iBioSim.tex > iBioSim.html

clean:
	rm *.dvi *.ps

