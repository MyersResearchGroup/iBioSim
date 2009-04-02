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

### TODO ###
#Provide a method to specify different numbers of thresholds for different variables?
#Use properties to aid in the threshold generation?
#Update genBins to use the partial results list
#Add a brief comment regarding how to add optimization and cost functions

import re, os.path, cText, copy, sys
from optparse import OptionParser

#Regular expressions
lQuoteR = re.compile("\"+")
tQuoteR = re.compile("\"+")
numVarsR = re.compile("Variables: ")
numPointsR = re.compile("Points: ")
spaceR = re.compile("\s+")
lSpaceR = re.compile("^\s+")
tSpaceR = re.compile("\s+$")
lineSpaceR = re.compile("^\s+$")
newLR = re.compile("\n+")
lDotR = re.compile("^\.\w")
epsilonR = re.compile(".epsilon")
lengthR = re.compile(".length")
timeR = re.compile(".time")
absoluteTimeR = re.compile(".absoluteTime")
percentR = re.compile(".percent")
inputR = re.compile(".inputs")
outputR = re.compile(".outputs")
dmvcR = re.compile(".dmvc")
rateSamplingR = re.compile(".rateSampling")
pathLengthR = re.compile(".pathLength")
vaRateUpdateIntervalR = re.compile(".vaRateUpdateInterval")
minDelayValR = re.compile(".minDelayVal")
minRateValR = re.compile(".minRateVal")
minDivisionValR = re.compile(".minDivisionVal")
decPercentR = re.compile(".decPercent")
minVarValR = re.compile(".minVarVal")
maxVarValR = re.compile(".maxVarVal")
falseR = re.compile("false",re.I) #pass the I flag to be case insensitive
trueR = re.compile("true",re.I) #pass the I flag to be case insensitive
binCommentR = re.compile("^\#")
lParenR = re.compile("\\(+")
rParenR = re.compile("\\)+")
rowsR = re.compile("\\(.*?\\)")

##############################################################################
# A class to hold the lists of places and transitions in the graph.
##############################################################################
class Variable:
	"A continuous variable in the system being modeled."
	def __init__(self,nameStr):
		self.name = nameStr #the name of the variable
		self.dmvc = None #Boolean denoting the status of the variable as a discrete multi-valued continuous (DMVC) variable
		self.input = None #Boolean denoting that the variable is a model input
		self.output = None #Boolean denoting that the variable is a model output
		self.type = None #Describes the type of the variable using an enumerated type (VOLTAGE, CURRENT) which is needed by Verilog-A.
	def __str__(self):
		retStr = self.name+"["
		if self.dmvc:
			retStr = retStr + "1]"
		else:
			retStr = retStr + "0]"
		return retStr
## End Class Variable ########################################################

##############################################################################
# A class to hold the parameters specified in the threshold (.bins) file.
##############################################################################
class ThresholdParameters:
	"The parameters possibly specified in the thresholds (.bins) file."
	def __init__(self,numVars):
		#Default values
		self.epsilon = 0.1 #What is the +/- epsilon where signals are considered to be equivalent
		self.length = 15 #the number of time points that a value must persist to be considered constant
		self.time = 5e-6 #the amount of time that must pass to be considered constant when using absoluteTime
		self.absoluteTime = False #when False time points are used to determine DMVC and when true absolutime time is used to determine DMVC
		self.percent = 0.8 #a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var
		self.numValuesL = [] #the number of constant values for each variable...-1 indicates that the variable isn't considered a DMVC variable
		self.vaRateUpdateInterval = 1e-6 #how often the rate is added to the continuous variable in the Verilog-A model output
		for i in range(numVars):
			self.numValuesL.append(-1)
	def __str__(self):
		retStr = "epsilon:"+str(self.epsilon)+" length:"+str(self.length)+" numValuesL:"+str(self.numValuesL)
		return retStr
	############################################################################
	# Determine if two values are equal within the given epsilon.
	############################################################################
	def epsilonEquiv(self,v1,v2):
		if abs(v1-v2) <= self.epsilon:
			return True
		else:
			return False
## End Class ThresholdParameters #############################################

##############################################################################
# A class for data required for the discovery of discrete multi-valued
# continuous variables.
##############################################################################
class DMVCpart:
	"Information about a single run of a constant value."
	numDMVCparts = 0
	def __init__(self):
		self.id = DMVCpart.numDMVCparts #unique numeric ID for each run
		DMVCpart.numDMVCparts = DMVCpart.numDMVCparts + 1
		self.varInd = -1 #an index into the varsL array denoting which variable owns the run
		self.valueL = [] #the list of values for this run
		self.startPoint = -1 #an index into datL for the start point of the run
		self.endPoint = -1 #an index into datL for the end point of the run
		self.nextRun = None #a reference to the next sequential run
	def __str__(self):
		retStr = "Part:"+str(self.id)+" Start:"+str(self.startPoint)+" End:"+str(self.endPoint)+" Val:"+str(self.constVal())
		if self.nextRun:
			retStr += " Next:"+str(self.nextRun.id)
		else:
			retStr += " Next:None"
		return retStr
	############################################################################
	# Calculate the constant value from the value list.  Currently it is an
	# average of all list values.
	############################################################################
	def constVal(self):
		total = 0
		for i in self.valueL:
			total = total + i
		return total/float(len(self.valueL))
  ############################################################################
	# Calculate the delay for a given DMVC run.
  ############################################################################
	def calcDelay(self,datL):
		ind1 = self.startPoint
		ind2 = self.endPoint
		delay = datL[ind2][0]-datL[ind1][0]
	  #Assuming that there is some time between runs we want to account for that time.  If we assume a constant rate of change we can just split the difference
		if self.nextRun:
			ind3 = self.nextRun.startPoint
			delay += ((datL[ind3][0]-datL[ind2][0])/2)
		return delay
## End Class DMVCpart ########################################################

##############################################################################
# Remove leading & trailing space as well as trailing new line characters.
##############################################################################
def cleanLine(line):
	lineNS = re.sub(lSpaceR,"",line)
	lineNL = re.sub(newLR,"",lineNS)
	lineTS = re.sub(tSpaceR,"",lineNL)
	return lineTS

##############################################################################
# Remove leading & trailing space as well as trailing new line characters.
##############################################################################
def cleanName(name):
	nameNL = re.sub(lQuoteR,"",name)
	nameTS = re.sub(tQuoteR,"",nameNL)
	return nameTS

##############################################################################
# Creates a 2 dimensional array of lists rows x cols with each value
# initialized to initVal. 
##############################################################################
def create2Darray(rows,cols,initVal):
	newL = []
	for i in range(rows):
		initL = []
		for j in range(cols):
			initL.append(initVal)
		newL.append(initL)
	return newL

##############################################################################
# Create the list of variables.  All data files must have the same variables
# in the same order.
##############################################################################
def extractVars(datFile):
	varsL = []
	line = ""
	inputF = open(datFile, 'r')
	rowsL = inputF.read()
	rowsM = rowsR.match(rowsL)
	row = rowsM.group()
	varNames = cleanRow(row)
	varNamesL = varNames.split(",")
	for varStr in varNamesL:
		varStr = cleanName(varStr)
		varsL.append(Variable(varStr))
	varsL[0].dmvc = False
	inputF.close()
	return varsL

##############################################################################
# Parse a .dat file ensuring that the varsL matches the global list.
##############################################################################
def parseDatFile(datFile,varsL):
	inputF = open(datFile, 'r')
	linesL = inputF.read()
	rowsL = rowsR.findall(linesL)
	for i in range(len(rowsL)):
		rowsL[i] = cleanRow(rowsL[i])
	numPoints = -1
	varNames = cleanRow(rowsL[0])
	varNamesL = []
	varNamesL = varNames.split(",")
	for i in range(len(varNamesL)):
		varNamesL[i] = cleanName(varNamesL[i])
	numPoints = len(varNamesL)
	if len(varNamesL) == len(varsL):
		for i in range(len(varNamesL)):
			if varNamesL[i] != varsL[i].name:
				cStr = cText.cSetFg(cText.RED)
				cStr += "ERROR:"
				cStr += cText.cSetAttr(cText.NONE)
				print cStr+" Expected "+varsL[i].name+" in position "+str(i)+" but received "+varNamesL[i]+" in file: "+datFile
				sys.exit()
	else:
		cStr = cText.cSetFg(cText.RED)
		cStr += "ERROR:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr + " Expected "+str(len(varsL))+" variables but received "+str(len(varNamesL))+" in file: "+datFile
		sys.exit()
	
	datL = []
	for i in range(1,len(rowsL)):
		valStrL = cleanRow(rowsL[i]).split(",")
		valL = []
		for s in valStrL:
			valL.append(float(s))
		datL.append(valL)
	inputF.close()
	return datL, numPoints

##############################################################################
# Parse the .bins (thresholds) file.
##############################################################################
def parseBinsFile(binsFile,varsL):
	global pathLength
	global rateSampling
	global minDelayVal
	global minRateVal
	global minDivisionVal
	global decPercent
	global minVarValL
	global maxVarValL
	global limitExists
	
	minVarValL = []
	maxVarValL = []
	limitExists = False

	for i in range(len(varsL)):
		minVarValL.append(None)
		maxVarValL.append(None)

	if not os.path.isfile(binsFile):
		cStr = cText.cSetFg(cText.RED)
		cStr += "ERROR:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" the .bins file, "+binsFile+" was not found."
		sys.exit()
	inputF = open(binsFile, 'r')
	linesL = inputF.readlines()
	tParam = ThresholdParameters(len(varsL))
	divisionsStrL = []
	numDivisions = 0
	for i in range(1,len(varsL)):
		divisionsStrL.append([])
	for i in range(len(linesL)):
		#Allow blank lines and comments
		if lineSpaceR.match(linesL[i]) or binCommentR.match(linesL[i]):
			continue
		if lDotR.match(linesL[i]):
			if epsilonR.match(linesL[i]):
				epsilonL = spaceR.split(linesL[i])
				tParam.epsilon = abs(float(epsilonL[1]))
			elif lengthR.match(linesL[i]):
				lengthL = spaceR.split(linesL[i])
				tParam.length = float(lengthL[1])
			elif timeR.match(linesL[i]):
				timeL = spaceR.split(linesL[i])
				tParam.time = float(timeL[1])
			elif vaRateUpdateIntervalR.match(linesL[i]):
				vaRateUpdateIntervalL = vaRateUpdateIntervalR.split(linesL[i])
				tParam.vaRateUpdateInterval = float(vaRateUpdateIntervalL[1])
			elif absoluteTimeR.match(linesL[i]):
				absoluteTimeL = spaceR.split(linesL[i])
				if trueR.match(absoluteTimeL[1]):
					tParam.absoluteTime = True
				elif falseR.match(absoluteTimeL[1]):
					tParam.absoluteTime = False
				else:
					cStr = cText.cSetFg(cText.RED)
					cStr += "ERROR:"
					cStr += cText.cSetAttr(cText.NONE)
					print cStr+" Attempted to set .absoluteTime with"+absoluteTimeL[i]+" which is unrecognized.  It was not set.  Please use True or False."
					sys.exit()
			elif percentR.match(linesL[i]):
				percentL = spaceR.split(linesL[i])
				tParam.percent = float(percentL[1])
			elif inputR.match(linesL[i]):
				cLine = cleanLine(linesL[i])
				inputL = spaceR.split(cLine)
				for i in range(1,len(inputL)):
					found = False
					for j in range(1,len(varsL)):
						if inputL[i] == varsL[j].name:
							#print varsL[j].name+" is an input."
							varsL[j].input = True
							found = True
							break
					if not found:
						cStr = cText.cSetFg(cText.RED)
						cStr += "ERROR:"
						cStr += cText.cSetAttr(cText.NONE)
						print cStr+" "+inputL[i]+" was specified as an input in the .bins file, but wasn't found in the variable list."
						sys.exit()
			elif outputR.match(linesL[i]):
				cLine = cleanLine(linesL[i])
				outputL = spaceR.split(cLine)
				for i in range(1,len(outputL)):
					found = False
					for j in range(1,len(varsL)):
						if outputL[i] == varsL[j].name:
							#print varsL[j].name+" is an output."
							varsL[j].output = True
							found = True
							break
					if not found:
						cStr = cText.cSetFg(cText.RED)
						cStr += "ERROR:"
						cStr += cText.cSetAttr(cText.NONE)
						print cStr+" "+outputL[i]+" was specified as an output in the .bins file, but wasn't found in the variable list."
						sys.exit()
			elif dmvcR.match(linesL[i]):
				cLine = cleanLine(linesL[i])
				dmvcL = spaceR.split(cLine)
				outputL = spaceR.split(cLine)
				for i in range(1,len(dmvcL)):
					found = False
					for j in range(1,len(varsL)):
						if outputL[i] == varsL[j].name:
							#print varsL[j].name+" is dmvc."
							varsL[j].dmvc = True
							found = True
							break
					if not found:
						cStr = cText.cSetFg(cText.RED)
						cStr += "ERROR:"
						cStr += cText.cSetAttr(cText.NONE)
						print cStr+" "+outputL[i]+" was specified as dmvc in the .bins file, but wasn't found in the variable list."
						sys.exit()
			elif rateSamplingR.match(linesL[i]):
				rateSamplingL = spaceR.split(linesL[i])
				rateSampling = int(rateSamplingL[1])
			elif pathLengthR.match(linesL[i]):
				pathLengthL = spaceR.split(linesL[i])
				pathLength = int(pathLengthL[1])
			elif minDelayValR.match(linesL[i]):
				minDelayValL = spaceR.split(linesL[i])
				minDelayVal = int(minDelayValL[1])
			elif minRateValR.match(linesL[i]):
				minRateValL = spaceR.split(linesL[i])
				minRateVal = int(minDelayValL[1])
			elif minDivisionValR.match(linesL[i]):
				minDivisionValL = spaceR.split(linesL[i])
				minDivisionVal = int(minDelayValL[1])
			elif decPercentR.match(linesL[i]):
				decPercentL = spaceR.split(linesL[i])
				decPercent = int(decPercentL[1])
			elif minVarValR.match(linesL[i]):
				limitExists = True
				cLine = cleanLine(linesL[i])
				inputL = spaceR.split(cLine)
				found = False
				for i in range(1,len(varsL)):
					if inputL[2] == varsL[i].name:
						minVarValL[i] = inputL[1]
						found = True
						break
				if not found:
					cStr = cText.cSetFg(cText.RED)
					cStr += "ERROR:"
					cStr += cText.cSetAttr(cText.NONE)
					print cStr+" "+inputL[2]+" was specified in a target for .minVarVal in the .bins file, but wasn't found in the variable list."
					sys.exit()
			elif maxVarValR.match(linesL[i]):
				limitExists = True
				cLine = cleanLine(linesL[i])
				inputL = spaceR.split(cLine)
				found = False
				for i in range(1,len(varsL)):
					if inputL[2] == varsL[i].name:
						maxVarValL[i] = inputL[1]
						found = True
						break
				if not found:
					cStr = cText.cSetFg(cText.RED)
					cStr += "ERROR:"
					cStr += cText.cSetAttr(cText.NONE)
					print cStr+" "+inputL[2]+" was specified in a target for .maxVarVal in the .bins file, but wasn't found in the variable list."
					sys.exit()
			else:
				cStr = cText.cSetFg(cText.RED)
				cStr += "ERROR:"
				cStr += cText.cSetAttr(cText.NONE)
				print cStr+" Unparseable dot option in the thresholds file: "+linesL[i]
				sys.exit()
		else:
			numDivisions += 1
			cLineL = cleanLine(linesL[i]).split(" ")
			found = False
			for j in range(1,len(varsL)):
				if cLineL[0] == varsL[j].name:
					divisionsStrL[j-1] = cLineL[1:]
					found = True
					break
			if not found:
				#cStr = cText.cSetFg(cText.RED)
				cStr = "ERROR:"
				#cStr += cText.cSetAttr(cText.NONE)
				print cStr+" Variable not included in the data file."
				print "Line: "+linesL[i]
				sys.exit()
	divisionsL = [[]]
	for sL in divisionsStrL:
		fL = []
		for s in sL:
			if (s.find("?") == -1):
				fL.append(float(s))
			else:
				fL.append(s)
		divisionsL.append(fL)
		#print len(fL)
	inputF.close()
	if numDivisions != len(varsL)-1:
		cStr = cText.cSetFg(cText.RED)
		cStr += "WARNING:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" There is not a threshold for every variable in the dat file."
		#sys.exit()
	#print "divisionsL:"+str(divisionsL)
	return divisionsL, tParam

##############################################################################
# Remove leading and trailing parantheses
##############################################################################
def cleanRow(row):
    rowNS = re.sub(lParenR,"",row)
    rowTS = re.sub(rParenR,"",rowNS)
    return rowTS

##############################################################################
# Reorder the datL so each row is a list of data values for the ith
# variable.  Also build a list of the extreme values for each
# variable.
##############################################################################
def reorderDatL(varsL):
	datValsL = []
	datValsExtremaL = []
	for i in range(len(varsL)):
		datValsL.append([])
		datValsExtremaL.append([])

	i = 1
	while 0==0:
		try:
			datFile = "run-" + str(i) + ".tsd"
			datL,numPoints = parseDatFile(datFile,varsL)
			for j in range(len(varsL)):
				for k in range(len(datL)):
					datValsL[j].append(datL[k][j])
			for j in range(1,len(varsL)):
				datValsExtremaL[j] = (min(datValsL[j]),max(datValsL[j]))
		except:
			break
		i += 1
	return datValsL, datValsExtremaL

##############################################################################
# Explore a potential DVMC run.  If the run is valid (currently this
# means long enough) then return the run.  Else return None.
##############################################################################
def exploreRun(datL,i,j,tParam):
	run = DMVCpart()
	run.startPoint = i
	run.varInd = j
	run.valueL.append(datL[i][j])
	while i+1 < len(datL) and tParam.epsilonEquiv(datL[run.startPoint][j],datL[i+1][j]):
		run.valueL.append(datL[i+1][j])
		i = i+1
		#print "i:"+str(i)+" j:"+str(j)
	run.endPoint = i
	if not tParam.absoluteTime:
		if ((run.endPoint-run.startPoint)+1) < tParam.length:
			#print "Run is too short from "+str(run.startPoint)+" to "+str(run.endPoint)+" ["+str((run.endPoint-run.startPoint)+1)+"]"
			return None, i
		else:
			#print "Found a run from "+str(run.startPoint)+" to "+str(run.endPoint)+"["+str((run.endPoint-run.startPoint)+1)+"]"
			return run, i
	else:
		if run.calcDelay(datL) < tParam.time:
			#print "Run is too short from "+str(run.startPoint)+" to "+str(run.endPoint)+" ["+str(run.calcDelay(datL))+"]"
			return None, i
		else:
			#print "Found a run from "+str(run.startPoint)+" to "+str(run.endPoint)+" ["+str(run.calcDelay(datL))+"]"
			return run, i

##############################################################################
# Determine which variables should be considered multi-valued
# continuous variables.  Marks varsL[i].dmvc for DMVC variables and
# returns a list of valid DMVC runs varsL long.  Empty lists exist for
# non-DMVC places and lists of valid runs are present for DMVC
# variables.
##############################################################################
def findDMVC(datL,varsL,tParam):
	tempRun = None
	prevRun = None
	runL = []
	for j in range(len(varsL)):
		runL.append([])
		if varsL[j].dmvc != False:
			#print "Examining variable["+str(j)+"]: "+varsL[j].name
			mark = 0
			for i in range(len(datL)-1):
				if i < mark:
					continue
				if tParam.epsilonEquiv(datL[i][j],datL[i+1][j]):
					#print "Exploring from:"+str(i)
					tempRun,mark = exploreRun(datL,i,j,tParam)
					#print "Returning at:"+str(mark)
					if tempRun != None:
						if len(runL[j]) > 1:
							prevRun.nextRun = tempRun
						prevRun = tempRun
						runL[j].append(tempRun)
			#determine if a high enough percentage of the run is constant
			if not tParam.absoluteTime:
				numPoints = 0
				for run in runL[j]:
					#print "run:"+str(run)
					#print "runDelay:"+str(run.calcDelay(datL))
					numPoints += (run.endPoint-run.startPoint) + 1
				if (numPoints/float(len(datL))) < tParam.percent:
					#print "Clearing runs for "+varsL[j].name+" ["+str(numPoints/float(len(datL)))+"]"+str(numPoints)+"/"+str(len(datL))
					runL[j] = [] #clear the runs if they don't meet the percentage requirement
				else:
					#print varsL[j].name+" is a DMVC. ["+str(numPoints/float(len(datL)))+"]"
					varsL[j].dmvc = True
			else:
				absTime = 0.0
				for run in runL[j]:
					#print "run:"+str(run)
					#print "runDelay:"+str(run.calcDelay(datL))
					absTime += run.calcDelay(datL)
				if (absTime/(datL[len(datL)-1][0]-datL[0][0])) < tParam.percent:
					#print "Clearing runs for "+varsL[j].name+" ["+str(absTime/(datL[len(datL)-1][0]-datL[0][0]))+"]"+str(absTime)+"/"+str(datL[len(datL)-1][0]-datL[0][0])
					runL[j] = []
				else:
					#print varsL[j].name+" is a DMVC. ["+str(absTime/(datL[len(datL)-1][0]-datL[0][0]))+"]"
					varsL[j].dmvc = True
	#return runL for processing during the graph building
	return runL	
			
##############################################################################
# Create an initial set of divisions based upon the number of bins and
# the extreme values for each variable.  These initial bins are
# evenly spaced.
##############################################################################
def initDivisionsL(datValsExtremaL,varsL,divisionsL):
	#divisionsL = []
	#for i in range(len(varsL)):
	#	divisionsL.append([])
	for i in range(1,len(varsL)):
		print varsL[i]
		#print "i:"+str(i)+" "+str(datValsExtremaL[i])
		interval = float(abs(datValsExtremaL[i][1]-datValsExtremaL[i][0]) / (numThresholds+1))
		#print "interval:"+str(interval)
		for j in range(0,len(divisionsL[i])):
			if (divisionsL[i][j] == "?"):
				divisionsL[i][j] = datValsExtremaL[i][0]+(interval*j)
	return divisionsL

##############################################################################
# Generate the bin encoding for each data point given the divisions.
##############################################################################
def genBins(datL,divisionsL):
	#print "datL:"+str(datL)
	#print "divisionsL:"+str(divisionsL)
	binsL = create2Darray(len(divisionsL),len(datL[0]),-1)
	for i in range(1,len(divisionsL)):
		for j in range(len(datL[0])):
			for k in range(len(divisionsL[i])):
				if (datL[i][j] <= divisionsL[i][k]):
					binsL[i][j] = k
					break
				else:
					#handles the case when the datum is in the highest bin
					#i.e. for 2 boundary numbers 3 bins are required
					#print "binsL["+str(i)+"]["+str(j)+"] = "+str(k+1)
					binsL[i][j] = k+1
	#print "binsL:"+str(binsL)
	return binsL

##############################################################################
# Determine if two bins are equivalent and return a Boolean.
##############################################################################
def equalBins(a,b,binsL,divisionsL):
	for i in range(1,len(divisionsL)):
		if binsL[i][a] != binsL[i][b]:
			return False
	return True

##############################################################################
# Generate the rates for each data point given the bin encodings.
##############################################################################
def genRates(divisionsL,datL,binsL):
	#Function notes: Rates can be calculated based on transitions or places.  If rates are calculated based on places they are calculated based on the change in the bin for the entire line.  If rates are calculated based on transitions they are calculated based on the change in the bin for each variable.  These two methods have different results and it appears that place based rates are more stable.  To help "smooth" out the rates there are several ways to modify the rate calcualation.  One way is to change the rateSampling variable.  This variable determines how long the bin must remain constant before a rate is calculated for that bin.  It can be a numerical value or "inf."  The "inf" setting only calculates the rate once per bin change.  You can invalidate bin changes of short length using the pathLength variable.  Any run of consecutive bins shorter than pathLength will not have its rate calculated.  The rate is also only calculated if the time values differ for the two points as I have seen examples where this is a problem.
	ratesL = create2Darray(len(divisionsL),len(datL[0]),'-')
	if placeRates:
		#Place based rate calculation
		if rateSampling == "inf":
			mark = 0
			for i in range(len(datL[0])):
				if i < mark:
					continue
				while mark < len(datL[0]) and equalBins(i,mark,binsL,divisionsL):
					mark += 1
				if datL[0][mark-1] != datL[0][i] and (mark-i) >= pathLength:
					for j in range(1,len(divisionsL)):
						ratesL[j][i] = (datL[j][mark-1]-datL[j][i])/(datL[0][mark-1]-datL[0][i])
		else:
			for i in range(len(datL[0])-rateSampling):
				calcRate = True
				for k in range(rateSampling):
					if not equalBins(i,i+k,binsL,divisionsL):
						calcRate = False
						break
				if calcRate and datL[0][i+rateSampling] != datL[0][i]:
					for j in range(1,len(divisionsL)):
						ratesL[j][i] = (datL[j][i+rateSampling]-datL[j][i])/(datL[0][i+rateSampling]-datL[0][i])
	else:
		cStr = cText.cSetFg(cText.YELLOW)
		cStr += "WARNING:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+"this feature has not been tested."
		#Transition based rate calculation
		if rateSampling == "inf":
			for j in range(1,len(divisionsL)):
				mark = 0
				for i in range(len(datL[0])):
					if i < mark:
						continue
					while mark < len(datL[0]) and equalBins(i,mark,binsL,divisionsL):
						mark = mark + 1
					if datL[0][mark-1] != datL[0][i]:
						ratesL[j][i] = (datL[j][mark-1]-datL[j][i])/(datL[0][mark-1]-datL[0][i])
		else:
			for i in range(len(datL[0])-rateSampling):
				for j in range(1,len(divisionsL)):
					calcRate = True
					for k in range(rateSampling):
						if not equalBins(i,i+k,binsL,divisionsL):
							calcRate = False
							break
					if calcRate and datL[0][i+rateSampling] != datL[0][i]:
						ratesL[j][i] = (datL[j][i+rateSampling]-datL[j][i])/(datL[0][i+rateSampling]-datL[0][i])
	return ratesL

##############################################################################
# Return the minimum rate for a given rate list.
##############################################################################
def minRate(ratesL):
 	#Remove the characters from the list before doing the comparison
	cmpL = []
	for i in range(len(ratesL)):
		if ratesL[i] != '-':
			cmpL.append(ratesL[i])
	if len(cmpL) > 0:
		return min(cmpL)
	else:
		return "-"
	
##############################################################################
# Return the maximum rate for a given rate list.
##############################################################################
def maxRate(ratesL):
  #Remove the characters from the list before doing the comparison
	cmpL = []
	for i in range(len(ratesL)):
		if ratesL[i] != '-':
			cmpL.append(ratesL[i])
	if len(cmpL) > 0:
		return max(cmpL)
	else:
		return "-"

##############################################################################
# Give a score for the even distribution of points for all
# variables. 0 is the optimal score.
##############################################################################
def pointDistCost(datValsL,divisionsL,resL=[],updateVar=-1):
	total = 0
	if updateVar == 0:
		for i in range(len(divisionsL)):
			resL.append(0)
		#Fill up resL
		for i in range(1,len(divisionsL)):
			points = pointDistCostVar(datValsL[i],divisionsL[i])
			total += points
			resL[i] = points
	elif updateVar > 0:
		#Incrementally calculate a total change
		resL[updateVar] = pointDistCostVar(datValsL[updateVar],
																			 divisionsL[updateVar])
		for i in resL:
			total += i
	else:
		#Do a full calculation from scratch
		for i in range(1,len(divisionsL)):
			total += pointDistCostVar(datValsL[i],divisionsL[i])
	return total

##############################################################################
# Give a score for the even distribution of points for an individual
# variable. 0 is the optimal score.
##############################################################################
def pointDistCostVar(datValsL,divisionsL):
	optPointsPerBin = len(datValsL)/(len(divisionsL)+1)
	#print "optPointsPerBin:"+str(optPointsPerBin)
	pointsPerBinL = []
	for i in range(len(divisionsL)+1):
		pointsPerBinL.append(0)
	for i in range(len(datValsL)):
		top = True
		for j in range(len(divisionsL)):
			if datValsL[i] <= divisionsL[j]:
				pointsPerBinL[j] += 1
				top = False
				break
		if top:
			pointsPerBinL[len(divisionsL)] += 1

	#print "pointsPerBinL:"+str(pointsPerBinL)
	score = 0
	for points in pointsPerBinL:
		score += abs(points - optPointsPerBin)
	return score

##############################################################################
# Give a score for the range of rates for all variables.  0 is the
# optimal score.
##############################################################################
def rateRangeCost(datValsL,divisionsL,resL=[],updateVar=-1):
	total = 0
	binsL = genBins(datValsL,divisionsL)
	ratesL = genRates(divisionsL,datValsL,binsL)
	#print "ratesL:"+str(ratesL)
	for i in range(1,len(divisionsL)):
		maxR = maxRate(ratesL[i])
		minR = minRate(ratesL[i])	
		total += abs(maxR-minR)
	return total

##############################################################################
# Look for the optimal thresholds using a greedy algorithm.
##############################################################################
def greedyOpt(divisionsL,datValsL,datValsExtremaL,initDivL):
	resL = [] #Used to keep partial results for cost functions
	updateVar = 0 #The variable that was updated to help optimize cost function recalculation
	bestDivisionsL = copy.deepcopy(divisionsL)
	bestCost = costFunc(datValsL,divisionsL,resL,updateVar)
	numMoves = 0
	print "Starting optimization..."
	while numMoves < iterations:
		for i in range(1,len(divisionsL)):
			for j in range(len(divisionsL[i])):
				if (initDivL[i][j] != "?"):
					#move left
					if (j == 0):
						if divisionsL[i][j] != "?":
							distance = abs(divisionsL[i][j] - datValsExtremaL[i][0])/2
						else:
							distance = abs(divisionsL[i][j] - divisionsL[i][j-1])/2
					else:
						distance = abs(divisionsL[i][j] - divisionsL[i][j-1])/2
					newDivisionsL = copy.deepcopy(divisionsL)
					newDivisionsL[i][j] -= distance
					newCost = costFunc(datValsL,newDivisionsL,resL,i)
					numMoves += 1
					if numMoves % 500 == 0:
					    	print str(numMoves)+"/"+str(iterations)
					if newCost < bestCost:
						bestCost = newCost
						divisionsL = newDivisionsL
					else:
						#move right
						if j == len(divisionsL[i])-1:
							distance = abs(datValsExtremaL[i][1] - divisionsL[i][j])/2
						else:
							distance = abs(divisionsL[i][j+1] - divisionsL[i][j])/2
						newDivisionsL = copy.deepcopy(divisionsL)
						newDivisionsL[i][j] += distance
						newCost = costFunc(datValsL,newDivisionsL,resL,i)
						numMoves += 1
						if numMoves % 500 == 0:
							print str(numMoves)+"/"+str(iterations)
						if newCost < bestCost:
							bestCost = newCost
							divisionsL = newDivisionsL
						if numMoves > iterations:
							return divisionsL
	return divisionsL

##############################################################################
# Look for the optimal thresholds using a greedy algorithm for the
##############################################################################
def writeBinsFile(varsL,divisionsL,binsFile):
	outputF = open(binsFile, 'w')
	flag = False
	for i in range(len(varsL)):
		if (varsL[i].dmvc == True):
			if (flag == False):
				outputF.write(".dmvc ")
				flag = True
			outputF.write(varsL[i].name + " ")
	if (flag == True):
		outputF.write("\n")
	for i in range(1,len(varsL)):
			if len(divisionsL[i]) > 0:
				outputF.write(varsL[i].name)
				for div in divisionsL[i]:
					outputF.write(" "+str(div))
				outputF.write("\n")
	outputF.close()

##############################################################################
##############################################################################
def main():
	global numThresholds
	global iterations
	global optFunc
	global costFunc
	
	usage = "usage: %prog [options] datFile1 ... datFileN"
	parser = OptionParser(usage=usage)
	parser.set_defaults(binsFile=None,numThresholds=None,costF="p",optF="g")
	parser.add_option("-b", "--bins", action="store", dest="binsFile", help="The name of the .bins file to be created.  If this is not provided the basename of the first input data file is used.")
	parser.add_option("-t", "--thresholds", action="store", dest="numThresholds", help="The number of thresholds to create during autogeneration.")
	parser.add_option("-i", "--iterations", action="store", dest="iterations", help="The number of iterations of the optimization algorithm to run.")
	parser.add_option("-c", "--cost", action="store", dest="costF", help="The cost function to use: r - Minimize the distance between rates; p - Average the number of points in each bin.")
	parser.add_option("-o", "--optimization", action="store", dest="optF", help="The optimization function to use: g - Greedy algorithm.")
	
	(options, args) = parser.parse_args()

	#if len(args) > 0:
	#	datFileL = args
	#else:
	#	print "At least one data file is required."
	#	parser.print_help()
	#	sys.exit()

	if not options.binsFile:
		baseFileL = os.path.splitext(datFileL[0])
		binsFile = baseFileL[0]+".bins"
	else:
		binsFile = options.binsFile

	if options.numThresholds:
		numThresholds = int(options.numThresholds)

	if options.iterations:
		iterations = int(options.iterations)

	if options.optF == "g":
		optFunc = greedyOpt
	else:
		cStr = cText.cSetFg(cText.RED)
		cStr += "ERROR:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr + options.optFunc + " is not a valid option for the optimization function."
		parser.print_help()
		sys.exit()

	if options.costF == "r":
		costFunc = rateRangeCost
	elif options.costF == "p":
		costFunc = pointDistCost
	else:
		cStr = cText.cSetFg(cText.RED)
		cStr += "ERROR:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr + options.costFunc + " is not a valid option for the cost function."
		parser.print_help()
		sys.exit()

	varsL = extractVars("run-1.tsd")
	datValsL, datValsExtremaL = reorderDatL(varsL)
	if os.path.isfile(options.binsFile):
		initDivL, tParam = parseBinsFile(options.binsFile,varsL)
	dmvcRunL = findDMVC(datValsL,varsL,tParam)
	divisionsL = initDivisionsL(datValsExtremaL,varsL,initDivL)
	print "Iterations: "+str(iterations)
	print "Optimization function: "+optFunc.func_name
	print "Cost function: "+costFunc.func_name
	print "Initial divisionsL:"+str(divisionsL)
	print "Initial score:"+str(costFunc(datValsL,divisionsL))
	divisionsL = optFunc(divisionsL,datValsL,datValsExtremaL,initDivL)
	print "Final divisionsL:"+str(divisionsL)
	print "Final score:"+str(costFunc(datValsL,divisionsL))
	writeBinsFile(varsL,divisionsL,binsFile)
		
##############################################################################
##############################################################################

###########
# Globals #
###########
numThresholds = 2 #the default number of thresholds to create...it can be overridden from the command line
iterations = 10000 #the default number of iterations
rateSampling = "inf" #How many points should exist between the sampling of different rates..."inf" samples once/threshold
pathLength = 10 #For "inf" rate sampling the number of time points that a "run" must persist for the rate to be calculated.  This is just another parameter to help with the data smoothing.
placeRates = True #When true the script calculates rates based on places.  When false it calculates rates based on transitions although there is very little infrastructure for transition based rates and it isn't well tested.
optFunc = None #The name of the optimization function that will be used.
costFunc = None #The name of the cost function that will be used in the optimization function.


if __name__ == "__main__":
	main()
