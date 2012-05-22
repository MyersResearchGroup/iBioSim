LATEX = latex
BIBTEX = bibtex
DOT2PS = dot -T ps
DVI2PS = dvips
DVI2PDF = dvipdf
DVIFLAGS = -t letter
PDFLATEX = pdflatex

all: iBioSim.html iBioSim_Tutorial.html iBioSim_Grid_Tutorial.html iBioSim_Install.html LEMA.html ATACS.html

iBioSim.pdf: iBioSim.tex
	$(PDFLATEX) iBioSim
	$(PDFLATEX) iBioSim

iBioSim.html: iBioSim.pdf
	tth -u -e2 -LiBioSim < iBioSim.tex > iBioSim.html

LEMA.pdf: LEMA.tex
	$(PDFLATEX) LEMA
	$(PDFLATEX) LEMA

LEMA.html: LEMA.pdf
	tth -u -e2 -LLEMA < LEMA.tex > LEMA.html

ATACS.pdf: ATACS.tex
	$(PDFLATEX) ATACS
	$(PDFLATEX) ATACS

ATACS.html: ATACS.pdf
	tth -u -e2 -LATACS < ATACS.tex > ATACS.html

iBioSim_Tutorial.pdf: iBioSim_Tutorial.tex
	$(PDFLATEX) iBioSim_Tutorial
	$(PDFLATEX) iBioSim_Tutorial

iBioSim_Tutorial.html: iBioSim_Tutorial.pdf
	tth -u -e2 -LiBioSim_Tutorial < iBioSim_Tutorial.tex > iBioSim_Tutorial.html

iBioSim_Grid_Tutorial.pdf: iBioSim_Grid_Tutorial.tex
	$(PDFLATEX) iBioSim_Grid_Tutorial
	$(PDFLATEX) iBioSim_Grid_Tutorial

iBioSim_Grid_Tutorial.html: iBioSim_Grid_Tutorial.pdf
	tth -u -e2 -LiBioSim_Grid_Tutorial < iBioSim_Grid_Tutorial.tex > iBioSim_Grid_Tutorial.html

iBioSim_install.pdf: iBioSim_install.tex
	$(PDFLATEX) iBioSim_Install
	$(PDFLATEX) iBioSim_Install

iBioSim_install.html: iBioSim_install.pdf
	tth -u -e2 -LiBioSim_Install < iBioSim_Install.tex > iBioSim_Install.html

clean:
	rm *.pdf *.html *.aux *.toc *.dvi *.log *.ps 

