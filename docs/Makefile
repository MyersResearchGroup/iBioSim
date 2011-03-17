LATEX = latex
BIBTEX = bibtex
DOT2PS = dot -T ps
DVI2PS = dvips
DVI2PDF = dvipdf
DVIFLAGS = -t letter
PDFLATEX = pdflatex

all: iBioSim.html iBioSim_Tutorial.html install.html LEMA.html ATACS.html

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

install.pdf: install.tex
	$(PDFLATEX) install
	$(PDFLATEX) install

install.html: install.pdf
	tth -u -e2 -Linstall < install.tex > install.html

clean:
	rm *.pdf *.html *.aux *.toc *.dvi *.log *.ps 

