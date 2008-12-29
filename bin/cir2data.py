#!/usr/bin/python

##############################################################################
##   Copyright (c) 2007 by Scott R. Little
##   University of Utah
##
##   Permission to use, copy, modify and/or distribute, but not sell, this
##   software and its documentation for any purpose is hereby granted
##   without fee, subject to the following terms and conditions:
##
##   1.  The above copyright notice and this permission notice must
##   appear in all copies of the software and related documentation.
##
##   2.  The name of University of Utah may not be used in advertising or
##   publicity pertaining to distribution of the software without the
##   specific, prior written permission of University of Utah.
##
##   3.  THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND,
##   EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY
##   WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
##
##   IN NO EVENT SHALL UNIVERSITY OF UTAH OR THE AUTHORS OF THIS SOFTWARE BE
##   LIABLE FOR ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES
##   OF ANY KIND, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
##   OR PROFITS, WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON
##   ANY THEORY OF LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE
##   OR PERFORMANCE OF THIS SOFTWARE.
##
##############################################################################
## DESCRIPTION: This script takes a circuit or SPICE output data and
## processes it in preparation for the data2lhpn script.  Given a
## circuit file the script will run the specified simulations and
## conver the data.  Given an ASCII SPICE RAW file the script will
## process the file.
##############################################################################

### TODO ###
#Add Spectre support?
#Add support for *.cir files to run multiple simulations
#clean-up code that modifies loop variables (b/c the modification isn't really working)
#allow blank lines in the .bins file
#check to see if ngspice/ngsconvert exist and return an nice error message if they don't
#Add gnuplot script generation

import re, os, sys
from optparse import OptionParser

#Regular expressions
ngsNumVarsR = re.compile("No. Variables: ")
ngsNumPointsR = re.compile("No. Points: ")
ngsVarsR = re.compile("Variables:")
ngsValsR = re.compile("Values:")
micaDataInitR = re.compile("Index ")
spaceR = re.compile("\s+")
lSpaceR = re.compile("^\s+")
tSpaceR = re.compile("\s+$")
newLR = re.compile("\n+")
lNumR = re.compile("^\d")
numR = re.compile("\d")
varCleanR = re.compile("\(|\)|\#|:")

##############################################################################
# Clean-up the given line by removing leading and trailing space as well as
# trailing new line characters.
##############################################################################
def cleanLine(line):
	lineNS = re.sub(lSpaceR,"",line)
	lineNL = re.sub(newLR,"",lineNS)
	lineTS = re.sub(tSpaceR,"",lineNL)
	return lineTS

##############################################################################
# Clean up variable names (to make them compatible w/ the .g file format of 
# ATACS) by removing any parens.
##############################################################################
def cleanVarName(line):
	lineNP = re.sub(varCleanR,"",line)
	return lineNP

##############################################################################
# Convert ngspice data into the dat output format.
##############################################################################
def ngspiceConvert(asciiFile,datFile,nodesL):
	print "Converting: "+asciiFile
	numVars = -1
	numPoints = -1
	varNamesL = []
	valsL = []
	inputF = open(asciiFile, 'r')
	outputF = open(datFile, 'w')
	linesL = inputF.readlines()
	for i in range(len(linesL)):
		if ngsNumVarsR.match(linesL[i]):
			varStrL = ngsNumVarsR.split(linesL[i])
			numVars = int(varStrL[1])
			print "Number of variables:"+str(numVars)
		if ngsNumPointsR.match(linesL[i]):
			pointStrL = ngsNumPointsR.split(linesL[i])
			numPoints = int(pointStrL[1])
			print "Number of points:"+str(numPoints)
		if ngsVarsR.match(linesL[i]):
			varHeaderL = spaceR.split(linesL[i])
			if varHeaderL[1] != "0":
				i = i+1
			for j in range(numVars):
				varName = linesL[i]
				varNameNS = cleanLine(varName)
				varNameL = spaceR.split(varNameNS)
				i = i+1
				if len(nodesL) > 1:
					if j in nodesL:
						if varNameL[1] == "0":
							#print "varName["+str(j)+"]: "+varNameL[2]
							varNamesL.append(cleanVarName(varNameL[2]))
						else:
							#print "varName["+str(j)+"]: "+varNameL[1]
							varNamesL.append(cleanVarName(varNameL[1]))
				else:
					#print "varName: "+varNameL[1]
					varNamesL.append(cleanVarName(varNameL[1]))
		if ngsValsR.match(linesL[i]):
			i = i+1
			for j in range(numPoints):
				varsL = []
				for k in range(numVars):
			    #print "i: "+str(i)+" j: "+str(j)+" k: "+str(k)
					varVal = linesL[i]
					varValNS = cleanLine(varVal)
					#print "k:"+str(k)
					if k == 0:
						#print "["+str(k)+"] "+"varValNS:"+varValNS
						varValL = spaceR.split(varValNS)
						valStr = varValL[1]
					else:
						valStr = varValNS
						#print "nodesL:"+str(len(nodesL))+":"
						#print nodesL
					if len(nodesL) > 1:
						if k in nodesL:
							varsL.append(valStr)
					else:
						varsL.append(valStr)
					i = i+1
					#print "varsL:"
					#print varsL
				valsL.append(varsL)
				#print "i:"+str(i)+" len(linesL):"+str(len(linesL))+" linesL[i+1]:"+str(linesL[i+1])
				if (i < len(linesL)) and not(numR.search(linesL[i])):
					#print "Empty line"
					i = i+1 #account for the empty line
			break
	if len(nodesL) > 1:
		if len(nodesL) <= numVars:
			numVars = len(nodesL)
		else:
			print "Error: the number of variables is less than the number of nodes provided on the command line."
			return
	outputF.write("Variables: "+str(numVars)+"\n")
	outputF.write("Points: "+str(numPoints)+"\n")
	for name in varNamesL:
		outputF.write(name+" ")
	outputF.write("\n")
	for valL in valsL:
		for val in valL:
			outputF.write(val+" ")
		outputF.write("\n")
	inputF.close()
	outputF.close()

##############################################################################
# Convert mica data into the dat output format.  This isn't really needed
# as Mica generates ASCII raw format which the standard script flow can read.
##############################################################################
def micaConvert(cirFile,datFile,nodesL):
	numVars = -1
	numPoints = -1
	varNamesL = []
	valsL = []
	inputF = open(cirFile, 'r')
	outputF = open(datFile, 'w')
	linesL = inputF.readlines()
	for i in range(len(linesL)):
		if micaDataInitR.match(linesL[i]):
			cleanLineStr = cleanLine(linesL[i])
			tempVarNamesL = spaceR.split(cleanLineStr)
			tempVarNamesL.pop(0)
			print "varNames:"+str(varNamesL)
			if len(nodesL) > 1:
				for j in range(tempVarNamesL):
					if j in nodesL:
						varNamesL.append(tempVarNameL[j])
			else:
				varNamesL = tempVarNamesL
			numVars = len(varNamesL)
			numPoints = 0
			i = i+2 #skip the final header line and position i at the
		        #beginning of the data
			while lNumR.match(linesL[i]):
				print "line["+str(i)+"]"+linesL[i]
				newValsL = spaceR.split(linesL[i])
				newValsL.pop(0) #remove the Index
				if len(nodesL) > 1:
					finalValsL = []
					for j in range(newValsL):
						if j in nodesL:
							finalValsL.append(newValsL[j])
					valsL.append(finalValsL)
				else:
					valsL.append(newValsL)
				i = i + 1
				numPoints = numPoints + 1
			break
	outputF.write("Variables: "+str(numVars)+"\n")
	outputF.write("Points: "+str(numPoints)+"\n")
	for name in varNamesL:
		outputF.write(name+" ")
	outputF.write("\n")
	for valL in valsL:
		for val in valL:
			outputF.write(val+" ")
		outputF.write("\n")
	inputF.close()
	outputF.close()

##############################################################################
##############################################################################
def main():
	usage = "usage: %prog [options] file1 ... fileN"
	parser = OptionParser(usage=usage)
	parser.set_defaults(doSpice=False, doRawConvert=False, simType="ngspice", nodes="")
	parser.add_option("-n", "--nodes", action="store", dest="nodes", help="A comma separated list of the nodes to output to the data file.  Time is row 0 and always output.")
	parser.add_option("-s", "--spice", action="store_true", dest="doSpice", help="Run ngspice on the provided file(s).")
	parser.add_option("-r", "--rawconvert", action="store_true", dest="doRawConvert", help="Run ngsconvert on the provided file(s).  Providing -s automatically enables -r.")
	parser.add_option("-t", "--type", action="store", dest="simType", help="Select the type of simulator used.")
	
	(options, args) = parser.parse_args()

	if len(args) > 0:
		fileL = args
	else:
		print "At least one data file is required."
		parser.print_help()
		sys.exit()
	
	nodesStrL = options.nodes.split(',')
	nodesL = []
	if (options.nodes != ""):
		for s in nodesStrL:
			nodesL.append(int(s))
    #Ensure that time gets output
		if not(0 in nodesL):
			nodesL.insert(0,0)
		#print "nodesL:"+str(nodesL)
	
	for i in range(len(fileL)):
		baseFileL = os.path.splitext(fileL[i])
		cirFile = fileL[i]
		rawFile = baseFileL[0] + ".raw"
		asciiFile = baseFileL[0] + ".ascii"
		datFile = baseFileL[0] + ".dat"

		if(options.doRawConvert or options.doSpice):
			convertFile = asciiFile
		else:
			convertFile = fileL[i]
		
		if(options.doSpice):
			spiceCmd = spicePath + " -b -r " + rawFile + " " + cirFile
			if(os.system(spiceCmd) != 0):
				print "An error occured with the following SPICE command: " + spiceCmd

		if(options.doRawConvert or options.doSpice):
			rawConvertCmd = rawConvertPath + " b " + rawFile + " a " + asciiFile
			if(os.system(rawConvertCmd) != 0):
				print "An error occured with the following raw convert command: " + rawConvertCmd
		
		if options.simType == "ngspice":
			ngspiceConvert(convertFile,datFile,nodesL)
		elif options.simType == "mica":
			micaConvert(convertFile,datFile,nodesL)

##############################################################################
##############################################################################

###########
# Globals #
###########
spicePath = "ngspice"
rawConvertPath = "ngsconvert"

if __name__ == "__main__":
	main()
