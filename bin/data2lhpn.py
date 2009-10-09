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
##   LIABLE FOR ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGESp
##   OF ANY KIND, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
##   OR PROFITS, WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON
##   ANY THEORY OF LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE
##   OR PERFORMANCE OF THIS SOFTWARE.
##
##############################################################################
## DESCRIPTION: This script takes as input .dat files that have been
## preprocessed by cir2data.py, a .bins file that gives threshold
## information for the data as well as configuration information for
## the graph generation, and an optional file containing a property to
## verify.  The script processes the data files and generates a graph.
## This graph is output as an LHPN, VHDL-AMS, and Verilog-A file.  The
## Verilog-A file is deterministic and therefore potentially contains
## fewer behaviors.
##############################################################################

### TODO ###
#Property Parsing
##scale property values?
##check property to see if it contains valid variables or at least munge those variable names similar to other variables?
##Add property output to the VHDL-AMS output
#Think about dealing w/ initial conditions that aren't the same (check for multiple tokens in a loop on initialization?)
#devise some units tests
#determine how to handle normalization overflow/underflow
#allow epsilon values on a per variable basis?
#consider potentially breaking the state or creating a special start and/or end state instead of removing "spurious" start and end information
#add code to extract the proper places and check for cycles after the graph has been built
#update coverage information for non-input DMVC variables
#what about autodetecting items like pathLength?
#determine how to calculate delay for non-input DMVC variable assignments
#write a range of variable assignments as it is now supported by the g parser
#Remove expansion places and push the assignments to the proper transition
#Check for consistent starting state
#Add rate assignment removal when an assignment won't change the rate.
#Add a places and transitions count for the LHPN...add it to the comments
#Add a debug level option to turn off temporary file generation
#Allow the sliding window to be time based as well?
#Ensure that rates are in a place for a specified length of time (fix normalization)

import re, os.path, fpformat, math, sys, copy, cText
from optparse import OptionParser

#Regular expressions
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
lQuoteR = re.compile("\"+")
tQuoteR = re.compile("\"+")

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
# A class to hold the lists of places and transitions in the graph.
##############################################################################
class Coverage:
	"The coverage statistics generated by the model generation."
	def __init__(self,filesL):
		self.index = -1 #an index for the file currently being processed
		self.filesL = filesL #a list of the file names to be processed
		self.placesL = [] #a list of the number of new places created by each file
		self.transitionsL = [] #a list of the number of new transistions created by each file
		self.ratesL = [] #a list of the number of new rates created by each file
		self.delaysL = [] #a list of the number of new delays created by each file
		for i in range(len(filesL)):
			self.placesL.append(0)
			self.transitionsL.append(0)
			self.ratesL.append(0)
			self.delaysL.append(0)
	def __str__(self):
		retStr = ""
		for i in range(len(self.filesL)):
			retStr += self.filesL[i]+":\n"
			retStr += "\tPlaces:"+str(self.placesL[i])+"\n"
			retStr += "\tTransitions:"+str(self.transitionsL[i])+"\n"
			retStr += "\tRates:"+str(self.ratesL[i])+"\n"
			retStr += "\tDelays:"+str(self.delaysL[i])+"\n"
		return retStr
	############################################################################
	# Increment the rate list if a new rate is found..
	############################################################################
	def isNewRate(self,t1,t2):
		newRate = False
		if t1[0] != t2[0]:
			self.ratesL[self.index] += 1
			newRate = True
		if t1[1] != t2[1]:
			self.ratesL[self.index] += 1
			newRate = True
		return newRate
	############################################################################
	# Increment the delay list if a new delay is found..
	############################################################################
	def isNewDelay(self,t1,t2):
		if t1[0] != t2[0]:
			self.delaysL[self.index] += 1
		if t1[1] != t2[1]:
			self.delaysL[self.index] += 1
## End Class Coverage ########################################################

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
# A class to store data regarding non-input DMVC assignments.
##############################################################################
class AsgnPart:
	"Information about an individual assignment that will be made based on predicate values."
	def __init__(self):
		self.var = None #the variable that is being assigned
		self.valL = [] #a list of the values for the variable collected via DMVC analysis
		self.delayL = [] #a list of the delays values for the variable collected via DMVC analysis.  This is the delay from the previous bin change to the bin where the assignment has happened.
	def __str__(self):
		retStr = "Var:"+str(self.var)+"\n"
		retStr += "\tValues:"+str(self.valL)
		retStr += "\tDelays:"+str(self.delayL)
		return retStr
	############################################################################
	# Calculate the minimum delay.
  ############################################################################
	def minDelay(self):
		return min(self.delayL)
	############################################################################
	# Calculate the minimum delay and return a properly rounded integer string.
  ############################################################################
	def minDelayInt(self):
		return str(int(math.floor(min(self.delayL))))
	############################################################################
	# Calculate the minimum delay.
  ############################################################################
	def maxDelay(self):
		return max(self.delayL)
	############################################################################
	# Calculate the maximum delay and return a properly rounded integer string.
  ############################################################################
	def maxDelayInt(self):
		return str(int(math.ceil(max(self.delayL))))
	############################################################################
	# Calculate the average delay.
  ############################################################################
	def avgDelay(self):
		total = 0
		for val in self.delayL:
			total += val
		return total/len(self.delayL)
	############################################################################
	# Calculate the minimum value.
  ############################################################################
	def minValue(self):
		return min(self.valL)
	############################################################################
	# Calculate the maximum value.
  ############################################################################
	def maxValue(self):
		return max(self.valL)
	############################################################################
	# Calculate the average value.
  ############################################################################
	def avgValue(self):
		total = 0
		for val in self.valL:
			total += val
		return total/len(self.valL)
## End Class AsgnPart ########################################################

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
# A class to hold the lists of places and transitions in the graph.
##############################################################################
class Graph:
	"A graph containing the places, transitions, and rates required to generate an analog model."
	def __init__(self,varsL,failProp):
		self.placeD = {} #a dictionary of places where the key is a string of binEncodingL joined with ""
		self.transitionD = {} #a dictionary of transitions where the key is a tuple of (incomingP, outgoingP)
		self.initRateL = [] #a list of rates for the initial place encoding
		for i in range(len(varsL)):
			self.initRateL.append([])
		self.initValL = [] #a list of initial values in the initial place
		for i in range(len(varsL)):
			self.initValL.append([])
		self.initMarkingL = []
		self.delayScaleFactor = -1
		self.varScaleFactor = -1
		self.failProp = failProp
		#Generate the place and transition for the failure property
		if failProp:
			self.failProp = True
			place = Place(varsL,PROP)
			place.binEncodingL = ["f1"]
			place.property = failProp
			self.placeD[place.keyStr()] = place
			self.initMarkingL.append(place)
			transition = Transition(len(varsL),None,place)
			self.transitionD[(None,place.keyStr())] = transition
			place.outgoingL.append(transition)
	def __str__(self):
		retStr = ""
		joinStr = ""
		retStr = retStr + "Places:\n"
		placeL = self.placeD.values()
		placeL.sort()
		for p in placeL:
			retStr = retStr + str(p)
		retStr = retStr + "Transitions:\n"
		transitionL = self.transitionD.values()
		transitionL.sort()
		for t in transitionL:
			retStr = retStr + str(t)
		return retStr
	############################################################################
	# Return a string containg the minimum initial rate for the given variable.
	# The string is a conservatively rounded integer.
	############################################################################
	def minInitRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.initRateL[i])):
			if self.initRateL[i][j] != '-':
				cmpL.append(self.initRateL[i][j])
		if len(cmpL) > 0:
			return str(int(math.floor(min(cmpL))))
		else:
			return "-"
	############################################################################
	# Return a string containing the maximum initial rate for the given
	# variable.  The string is a conservatively rounded integer.
	############################################################################
	def maxInitRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.initRateL[i])):
			if self.initRateL[i][j] != '-':
				cmpL.append(self.initRateL[i][j])
		if len(cmpL) > 0:
			return str(int(math.ceil(max(cmpL))))
		else:
			return "-"
	############################################################################
	# Return a string containg the minimum initial value for the given variable.
  # The string is a conservatively rounded integer.
	############################################################################
	def minInitValInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.initValL[i])):
			if self.initValL[i][j] != '-':
				cmpL.append(self.initValL[i][j])
		if len(cmpL) > 0:
			return str(int(math.floor(min(cmpL))))
		else:
			return "-"
	############################################################################
	# Return a string containing the maximum initial value for the given
	# variable.  The string is a conservatively rounded integer.
	############################################################################
	def maxInitValInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.initValL[i])):
			if self.initValL[i][j] != '-':
				cmpL.append(self.initValL[i][j])
		if len(cmpL) > 0:
			return str(int(math.ceil(max(cmpL))))
		else:
			return "-"
	############################################################################
	# Calculate the minimum non-zero delay for the graph.
	############################################################################
	def minDelay(self):
		minDelay = None
		for p in self.placeD.values():
			if p.isDmvcP():
				if not minDelay and p.minTime() != "-" and p.minTime != 0:
					minDelay = p.minTime()
				elif p.minTime() != "-" and p.minTime() < minDelay and p.minTime() != 0:
					minDelay = p.minTime()
			elif p.isAsgnP():
				for a in p.asg:
					if a.valL:
						if not minDelay and a.minDelay() != "-" and a.minDelay() != 0:
							minDelay = a.minDelay()
						elif a.minDelay() != "-" and a.minDelay() < minDelay and a.minDelay() != 0:
							minDelay = a.minDelay()
		return minDelay
	############################################################################
	# Calculate the maximum delay for the graph.
	############################################################################
	def maxDelay(self):
		maxDelay = None
		for p in self.placeD.values():
			if p.isDmvcP():
				if not maxDelay and p.maxTime() != "-":
					maxDelay = p.maxTime()
				elif p.maxTime() != "-" and p.maxTime() > maxDelay:
					maxDelay = p.maxTime()
			elif p.isAsgnP():
				for a in p.asg:
					if a.valL:
						if not maxDelay and a.maxDelay() != "-":
							maxDelay = a.maxDelay()
						elif a.maxDelay() != "-" and a.maxDelay() > maxDelay:
							maxDelay = a.maxDelay()
		return maxDelay
	############################################################################
	# Calculate the minimum non-zero rate for the graph.
	############################################################################
	def minRate(self):
		minRate = None
		for p in self.placeD.values():
			if p.isRateP():
				for i in range(1,len(p.ratesL)):
					if not minRate and p.minRate(i) != "-" and p.minRate(i) != 0:
							minRate = p.minRate(i)
					elif p.minRate(i) != "-" and p.minRate(i) < minRate and p.minRate(i) != 0:
						minRate = p.minRate(i)
		return minRate
	############################################################################
	# Calculate the maximum rate for the graph.
	############################################################################
	def maxRate(self):
		maxRate = None
		for p in self.placeD.values():
			if p.isRateP():
				for i in range(1,len(p.ratesL)):
					if not maxRate and p.maxRate(i) != "-":
							maxRate = p.maxRate(i)
					elif p.maxRate(i) != "-" and p.maxRate(i) > maxRate:
						maxRate = p.maxRate(i)
		return maxRate
	############################################################################
	# Scale the delay values in the graph by scaleFactor.
	############################################################################
	def scaleDelay(self,scaleFactor,squash):
		for p in self.placeD.values():
			if p.isDmvcP():
				for i in range(len(p.dmvcTimeL[p.dmvcVar])):
					p.dmvcTimeL[p.dmvcVar][i] *= scaleFactor
			if p.isAsgnP():
				for a in p.asg:
					for i in range(len(a.delayL)):
						a.delayL[i] *= scaleFactor
			if p.isRateP():
				for i in range(1,len(p.ratesL)):
					for j in range(len(p.ratesL[i])):
						if p.ratesL[i][j] != "-":
							p.ratesL[i][j] /= scaleFactor
							if(squash):
								if(abs(p.ratesL[i][j]) < 10):
									p.ratesL[i][j] = 0
		for i in range(1,len(self.initRateL)):
			for j in range(len(self.initRateL[i])):
				if self.initRateL[i][j] != "-":
					self.initRateL[i][j] /= scaleFactor
					if(squash):
						if(abs(self.initRateL[i][j]) < 10):
							self.initRateL[i][j] = 0
	############################################################################
	# Scale the variable values in the graph by scaleFactor.
	############################################################################
	def scaleVariable(self,scaleFactor,divisionsL):
		for p in self.placeD.values():
			if p.isRateP():
				for i in range(1,len(p.ratesL)):
					for j in range(len(p.ratesL[i])):
						if p.ratesL[i][j] != "-":
							p.ratesL[i][j] *= scaleFactor
			elif p.isDmvcP():
				p.dmvcVal *= scaleFactor
			elif p.isAsgnP():
				for a in p.asg:
					for i in range(len(a.valL)):
						a.valL[i] *= scaleFactor
	  #Scale initial values, initial rates, and thresholds.
		for i in range(1,len(self.initValL)):
			for j in range(len(self.initValL[i])):
				self.initValL[i][j] *= scaleFactor
				if self.initRateL[i][j] != "-":
					self.initRateL[i][j] *= scaleFactor
			for j in range(len(divisionsL[i])):
				divisionsL[i][j] *=  scaleFactor
## End Class Graph ###########################################################

##############################################################################
# A class to hold a place and its corresponding information.
##############################################################################
class Place:
	"A place in the generated LHPN."
	numPlaces = 0
	varsL = []
	def __init__(self,varsL,pType):
		self.placeNum = Place.numPlaces #unique numeric ID for each place
		Place.numPlaces = Place.numPlaces + 1
		self.type = pType #a string giving the place an enumerated type (RATE,DMVC, or PROP)
		self.binEncodingL = [] #an encoding for the place based on bins or for DMVC places d_varsL index_id#
		self.ratesL = [] #a list of lists (numVars long) containing rates that correspond to the place
		for i in range(len(varsL)):
			self.ratesL.append([])
		self.dmvcTimeL = [] #a list of the times for each dmvc run that composes the place
		for i in range(len(varsL)):
			self.dmvcTimeL.append([])
		self.dmvcVal = None #the DMVC constant value for the place
		self.dmvcVar = None #an index into varsL for the DMVC variable
		self.dmvcInfoL = [] #a list that is indexed by divisions and contains value and delay information for places representing non-input DMVC variables
		self.asg = [] #a list of assignments to be made in this place
		self.asgBinL = [] #the bin encoding for the place from which the asgn place is expanded
		self.preExGrandParentBinL = None #the bin encoding for the pre-expansion grandparent place
		self.property = "" #the property for PROP places
		self.color = 0 #color for use is cycle detection (0=white,1=grey,2=black)
		self.outgoingL = [] #a list of outgoing transitions
		self.incomingL = [] #a list of incoming transitions
		Place.varsL = varsL
	def __str__(self):
		retStr = "p" + str(self.placeNum) + " ("+self.keyStr()+") "
		if self.isRateP():
			retStr += "[rate]\n"
			for i in range(1,len(Place.varsL)):
				if not Place.varsL[i].dmvc:
					retStr += "\t" + Place.varsL[i].name + ":rate[" + str(self.minRate(i)) + "," + str(self.maxRate(i)) + "]\n"
		elif self.isDmvcP():
			retStr += "[dmvc]\n"
			for i in range(1,len(Place.varsL)):
				if Place.varsL[i].dmvc:
					retStr += "\t" + Place.varsL[i].name + ":time[" + str(self.minTime()) + "," + str(self.maxTime()) + "]\n"
		elif self.isPropP():
			retStr += "[prop]\n"
			retStr += "\t" + self.property + "\n"
		elif self.isAsgnP():
			retStr += "[asgn]\n"
		elif self.isTraceP():
			retStr += "[trace]\n"
		outL = []
		for outgoing in self.outgoingL:
			if outgoing:
				outL.append(str(outgoing.transitionNum))
		inL = []
		for incoming in self.incomingL:
			if incoming:
				inL.append(str(incoming.transitionNum))
		joinStr = ","
		retStr += "\tIncoming t: " + joinStr.join(inL) + "\n" + "\tOutgoing t: " + joinStr.join(outL) + "\n"
		return retStr
	def __eq__(self,other):
		if self.keyStr() == other.keyStr():
			return True
		else:
			return False
	def __ne__(self,other):
		return not (self == other)
	def __cmp__(self,other):
		if self.placeNum < other.placeNum:
			return -1
		elif self.placeNum == other.placeNum:
			return 0
		else:
			return 1
	############################################################################
	# Return a list of variables where the bin differs between the give places.
	############################################################################
	def diff(self,other,varsL):
		diffL = []
		if self.isAsgnP():
			selfL = self.asgBinL
		else:
			selfL = self.binEncodingL
		if other.isAsgnP():
			otherL = other.asgBinL
		else:
			otherL = other.binEncodingL
		for i in range(min((len(selfL),len(otherL)))):
			if selfL[i] != otherL[i]:
				diffL.append(i+1) #This is i+1 b/c binEncoding omits the time variable (varsL[0]) and this list should consistently index into varsL
		if(len(diffL)>1):
			#Verilog-A supports multiple changes on DMVC variables, but not on continuous rate variables.
			warn = False
			for j in diffL:
				if not varsL[j].dmvc:
					warn = True
			if warn:
				cStr = cText.cSetFg(cText.YELLOW)
				cStr += "WARNING:"
				cStr += cText.cSetAttr(cText.NONE)
				print cStr+" a transition connects places (p"+str(self.placeNum)+",p"+str(other.placeNum)+") where more than one bin changes."
		return diffL
	############################################################################
	# Return an integer that describes the order of meta-bins.  If the order is 
	# 1 the bin is reachable from a core bin in 1 step, etc.
	############################################################################
	def orderMetaBin(self,divisionsL):
		order = 0
		for i in range(len(self.binEncodingL)):
			if self.binEncodingL[i] == -1 or self.binEncodingL[i] == (len(divisionsL[i+1])+1):
				order = order + 1
		return order
	############################################################################
	# Remove all values greater than zero in the rate list for the given 
	# variable.
	############################################################################
	def rmRateGtZero(self,i):
		newRatesL = []
		if int(self.maxRateInt(i)) > 0:
			newRatesL.append(0)
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] <= 0:
				newRatesL.append(self.ratesL[i][j])
		if len(newRatesL) == 0:
			newRatesL.append(0)
		self.ratesL[i] = newRatesL
	############################################################################
	# Remove all values greater than zero in the rate list for the given 
	# variable.
	############################################################################
	def rmRateLtZero(self,i):
		newRatesL = []
		if int(self.minRateInt(i)) < 0:
			newRatesL.append(0)
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] >= 0:
				newRatesL.append(self.ratesL[i][j])
		if len(newRatesL) == 0:
			newRatesL.append(0)
		self.ratesL[i] = newRatesL
	############################################################################
	# Return a string containg the minimum rate for the given place.  The
	# string is a conservatively rounded integer.
	############################################################################
	def minRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return str(int(math.floor(min(cmpL))))
		else:
			return "-"
	############################################################################
	# Return a string containing the maximum rate for the given place.  The
	# string is a conservatively rounded integer.
	############################################################################
	def maxRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return str(int(math.ceil(max(cmpL))))
		else:
			return "-"
	############################################################################
	# Return the minimum rate for the given place.
	############################################################################
	def minRate(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return min(cmpL)
		else:
			return "-"
	############################################################################
	# Return the maximum rate for the given place.
	############################################################################
	def maxRate(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return max(cmpL)
		else:
			return "-"
	############################################################################
	# Return the average rate for the given place.
	############################################################################
	def avgRate(self,i):
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			total = 0
			for val in cmpL:
				total += val
			return total/float(len(cmpL))
		else:
			return "-"
	############################################################################
	# Return the minimum timing bound  for the place as a properly rounded
	# integer string.
	############################################################################
	def minTimeInt(self):
		if self.dmvcTimeL[self.dmvcVar]:
			return str(int(math.floor(min(self.dmvcTimeL[self.dmvcVar]))))
		else:
			return "-"
	############################################################################
	# Return the maximum timing bound for the place as a properly rounded
	# integer string.
	############################################################################
	def maxTimeInt(self):
		if self.dmvcTimeL[self.dmvcVar]:
			return str(int(math.ceil(max(self.dmvcTimeL[self.dmvcVar]))))
		else:
			return "-"
	############################################################################
	# Return the minimum timing bound  for the place.
	############################################################################
	def minTime(self):
		if self.dmvcTimeL[self.dmvcVar]:
			return min(self.dmvcTimeL[self.dmvcVar])
		else:
			return "-"
	############################################################################
	# Return the maximum timing bound for the place.
	############################################################################
	def maxTime(self):
		if self.dmvcTimeL[self.dmvcVar]:
			return max(self.dmvcTimeL[self.dmvcVar])
		else:
			return "-"
	############################################################################
	# Return the avg timing bound for the place.
	############################################################################
	def avgTime(self):
		totVal = 0
		for val in self.dmvcTimeL[self.dmvcVar]:
			totVal += val
		return totVal/float(len(self.dmvcTimeL[self.dmvcVar]))
	############################################################################
	# Return a string representing the bin encoding for the place.
	############################################################################
	def keyStr(self):
		keyStr = ""
		for i in range(len(self.binEncodingL)):
			keyStr += str(self.binEncodingL[i])
		return keyStr
	############################################################################
	# Return True if the place is along the edge of the thresholds and may
	# require limiting places and false otherwise.
	############################################################################
	def isEdgeP(self,divisionsL):
		for i in range(len(self.binEncodingL)):
			if self.binEncodingL[i] == 0 or self.binEncodingL[i] == (len(divisionsL[i+1])):
				return True
		return False
	############################################################################
	# Return True if the place is not a meta-bin.
	############################################################################
	def isCoreP(self,divisionsL):
		for i in range(len(self.binEncodingL)):
			if self.binEncodingL == -1 or self.binEncodingL[i] == len(divisionsL[i+1])+1:
				return False
		return True
	############################################################################
	# Return True if the place is representing a RATE place and false otherwise.
	############################################################################
	def isRateP(self):
		if self.type == RATE:
			return True
		else:
			return False
	############################################################################
	# Return True if the place is representing a DMVC place and false otherwise.
	############################################################################
	def isDmvcP(self):
		if self.type == DMVC:
			return True
		else:
			return False
	############################################################################
	# Return True if the place is representing a PROP place and false otherwise.
	############################################################################
	def isPropP(self):
		if self.type == PROP:
			return True
		else:
			return False
	############################################################################
	# Return True if the place is representing a ASGN place and false otherwise.
	############################################################################
	def isAsgnP(self):
		if self.type == ASGN:
			return True
		else:
			return False
	############################################################################
	# Return True if the place represents a TRACE place and false otherwise.
	############################################################################
	def isTraceP(self):
		if self.type == TRACE:
			return True
		else:
			return False
## End Class Place ###########################################################

##############################################################################
# A class to hold a transition and its required data items.
##############################################################################
class Transition:
	"A transition in the generated LHPN."
	numTransitions = 0
	def __init__(self,numVars):
		self.transitionNum = Transition.numTransitions #unique numeric ID for each transition
		Transition.numTransitions = Transition.numTransitions + 1
		self.ratesL = [] #a list of lists (numVars long) containing rates that
		                 #correspond to the rates in the outgoingP
		for i in range(numVars):
			self.ratesL.append([])
		self.outgoingP = None #Place in the postset of the transition
		self.incomingP = None #Place in the preset of the transition
		self.core = False
	def __init__(self,numVars,outgoingP,incomingP):
		self.transitionNum = Transition.numTransitions #unique numeric ID for each transition
		Transition.numTransitions = Transition.numTransitions + 1
		self.ratesL = [] #a list of lists (numVars long) containing rates that
		                 #correspond to the rates in the outgoingP
		for i in range(numVars):
			self.ratesL.append([])
		self.outgoingP = outgoingP #Place in the postset of the transition
		self.incomingP = incomingP #Place in the preset of the transition
		self.core = False
	def __str__(self):
		retStr = "t" + str(self.transitionNum) + "\n"
		minRateStr = "-"
		maxRateStr = "-"
		maxRateL = []
		minRateL = []
		for j in range(1,len(self.ratesL)):
			maxRateL.append(self.maxRateInt(j))
			minRateL.append(self.minRateInt(j))
		retStr += "\tmaxRate: " + str(maxRateL) + " minRate: " + str(minRateL) + "\n"
		if self.outgoingP:
			retStr += "\tOutgoing p: " + str(self.outgoingP.placeNum) + "\n"
		if self.incomingP:
			retStr += "\tIncoming p: " + str(self.incomingP.placeNum) + "\n"
		return retStr
	def __eq__(self,other):
		if self.outgoingP != other.outgoingP:
			return False
		if self.incomingP != other.incomingP:
			return False
		return True
	def __ne__(self,ohter):
		return not(self == other)
	def __cmp__(self,other):
		if self.transitionNum < other.transitionNum:
			return -1
		elif self.transitionNum == other.transitionNum:
			return 0
		else:
			return 1
	############################################################################
	# Return a string containing the maximum rate for the given transition
	############################################################################
	def maxRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return str(int(math.ceil(max(cmpL))))
		else:
			return "-"
	############################################################################
	# Return a string containing the maximum rate for the given transition
	############################################################################
	def minRateInt(self,i):
		#Remove the characters from the list before doing the comparison
		cmpL = []
		for j in range(len(self.ratesL[i])):
			if self.ratesL[i][j] != '-':
				cmpL.append(self.ratesL[i][j])
		if len(cmpL) > 0:
			return str(int(math.floor(min(cmpL))))
		else:
			return "-"
## End Class Transition ######################################################

##############################################################################
# Given a place this function does a DFS of the graph.  When a place
# is first visited it is colored grey.  If during the DFS another grey
# place is encountered this indicates a cycle.  When all outgoing
# transitions for a node have been explored that place is colored
# black.
##############################################################################
def colorGraph(p):
	#print "colorGraph(p"+str(p.placeNum)+":"+str(p.color)+")"
	if p.color == 1:
		return True
	elif p.color == 2:
		return False
	else:
		#print "Coloring: p"+str(p.placeNum)+":gray"
		p.color = 1
	for out in p.outgoingL:
		if colorGraph(out.outgoingP):
			return True
	#print "Coloring: p"+str(p.placeNum)+":black"
	p.color = 2
	return False

##############################################################################
# Given a list of places determine if there is a cycle.
##############################################################################
def isCycle(placeL):
	for p in placeL:
		if colorGraph(p):
			return True
	return False

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
# Return true if a DMVC variable exists and false otherwise.
##############################################################################
def dmvcVarExists(varsL):
	for var in varsL:
		if var.dmvc:
			return True
	return False

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
	numPoints = len(varNamesL)
	if len(varNamesL) == len(varsL):
		for i in range(len(varNamesL)):
			varNamesL[i] = cleanName(varNamesL[i])
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
# Remove leading and trailing parantheses
##############################################################################
def cleanRow(row):
    rowNS = re.sub(lParenR,"",row)
    rowTS = re.sub(rParenR,"",rowNS)
    return rowTS

##############################################################################
# Parse the .bins (thresholds) file.
##############################################################################
def parseBinsFile(binsFile,varsL,trace):
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
		if trace:
			cStr = cText.cSetFg(cText.GREEN)
			cStr += "NOTE:"
			cStr += cText.cSetAttr(cText.NONE)
			print cStr+" a .bins file was not read."
			return
		else:
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
					print cStr+" Attemptd to set .absoluteTime with"+absoluteTimeL[i]+" which is unrecognized.  It was not set.  Please use True or False."
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
				for i in range(1,len(dmvcL)):
					found = False
					for j in range(1,len(varsL)):
						if dmvcL[i] == varsL[j].name:
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
				cStr = cText.cSetFg(cText.RED)
				cStr += "ERROR:"
				cStr += cText.cSetAttr(cText.NONE)
				print cStr+" Unparseable line in the thresholds file."
				print "Line: "+linesL[i]
				sys.exit()
	divisionsL = [[]]
	for sL in divisionsStrL:
		fL = []
		for s in sL:
			fL.append(float(s))
		divisionsL.append(fL)
	inputF.close()
	if numDivisions != len(varsL)-1 and not trace:
		cStr = cText.cSetFg(cText.RED)
		cStr += "Warning:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" There is not a threshold for every variable in the dat file."
		#sys.exit()
	print "divisionsL:"+str(divisionsL)
	return divisionsL, tParam

##############################################################################
# Parse the .prop (property) file.
##############################################################################
def parsePropFile(propFile):
	inputF = open(propFile, 'r')
	rawProperty = inputF.readline()
	return cleanLine(rawProperty)

##############################################################################
# Generate the bin encoding for each data point given the divisions.
##############################################################################
def genBins(varsL,datL,divisionsL):
	binsL = create2Darray(len(datL),len(varsL),-1)
	#print "divisionsL:"+str(divisionsL)
	for i in range(len(datL)):
		#loop through each variable except time
		for j in range(1,len(varsL)):
			for k in range(len(divisionsL[j])):
				#print "Compare:  "+str(datL[i][j])+"<="+str(divisionsL[j][k])
				if (datL[i][j] <= divisionsL[j][k]):
					#print "binsL["+str(i)+"]["+str(j)+"] = "+str(k)
					binsL[i][j] = k
					#print str(binsL[i][j]),
					break
				else:
					#handles the case when the datum is in the highest bin
					#i.e. for 2 boundary numbers 3 bins are required
					#print "binsL["+str(i)+"]["+str(j)+"] = "+str(k+1)
					binsL[i][j] = k+1
			#print str(binsL[i][j]),
		#print "\n"
	return binsL

##############################################################################
# Generate the rates for each data point given the bin encodings.
##############################################################################
def genRates(varsL,datL,binsL,rateSampling):
	#Function notes: Rates can be calculated based on transitions or places.  If rates are calculated based on places they are calculated based on the change in the bin for the entire line.  If rates are calculated based on transitions they are calculated based on the change in the bin for each variable.  These two methods have different results and it appears that place based rates are more stable.  To help "smooth" out the rates there are several ways to modify the rate calcualation.  One way is to change the rateSampling variable.  This variable determines how long the bin must remain constant before a rate is calculated for that bin.  It can be a numerical value or "inf."  The "inf" setting only calculates the rate once per bin change.  You can invalidate bin changes of short length using the pathLength variable.  Any run of consecutive bins shorter than pathLength will not have its rate calculated.  The rate is also only calculated if the time values differ for the two points as I have seen examples where this is a problem.
	ratesL = create2Darray(len(datL),len(varsL),'-')
	if placeRates:
		#Place based rate calculation
		if rateSampling == "inf":
			#inf means that the window size is equal to the bin size
			mark = 0
			for i in range(len(datL)):
				if i < mark:
					continue
				while mark < len(datL) and binsL[i] == binsL[mark]:
					mark += 1
				if datL[mark-1][0] != datL[i][0] and (mark-i) >= pathLength:
					for j in range(1,len(varsL)):
						ratesL[i][j] = (datL[mark-1][j]-datL[i][j])/(datL[mark-1][0]-datL[i][0])
					#print str(binsL[mark])
					#print "\n"
		else:
			#Calculate the rates for each data point until the window would move the second point outside the bin
			for i in range(len(datL)-rateSampling):
				calcRate = True
				#Check to make sure the bin persists for at least one window size
				for k in range(rateSampling):
					if binsL[i] != binsL[i+k]:
						calcRate = False
						break
				if calcRate and datL[i+rateSampling][0] != datL[i][0]:
					for j in range(1,len(varsL)):
						ratesL[i][j] = (datL[i+rateSampling][j]-datL[i][j])/(datL[i+rateSampling][0]-datL[i][0])
	#Transition-based rates
	else:
		#Transition based rate calculation
		if rateSampling == "inf":
			for j in range(1,len(varsL)):
				mark = 0
				for i in range(len(datL)):
					if i < mark:
						continue
					while mark < len(datL) and binsL[i][j] == binsL[mark][j]:
						mark = mark + 1
					if datL[mark-1][0] != datL[i][0]:
						ratesL[i][j] = (datL[mark-1][j]-datL[i][j])/(datL[mark-1][0]-datL[i][0])
		else:
			for i in range(len(datL)-rateSampling):
				for j in range(1,len(varsL)):
					calcRate = True
					for k in range(rateSampling):
						if binsL[i][j] != binsL[i+k][j]:
							calcRate = False
							break
					if calcRate and datL[i+rateSampling][0] != datL[i][0]:
						ratesL[i][j] = (datL[i+rateSampling][j]-datL[i][j])/(datL[i+rateSampling][0]-datL[i][0])
	return ratesL

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
# Update the rate information in the graph.
##############################################################################
def updateRateInfo(g,varsL,datL,binsL,ratesL,cvg):
	prevTranKey = "" #previous place key used for generating transitions
	place = None #current place
	prevPlace = None #previous place (required to build outgoingL)
	transition = None
	newRate = False
	ratePlaceL = []
	for i in range(len(datL)-1):
		#only generate graph nodes for places for data which have calculated rates
		if ratesL[i][1] != "-":
			key = ""
			for j in range(1,len(varsL)):
				key += str(binsL[i][j])
			prevPlace = place
			#Find or create the place for the given key
			if g.placeD.has_key(key):
				place = g.placeD.get(key)
			else:
				place = Place(varsL,RATE)
				ratePlaceL.append(place)
				cvg.placesL[cvg.index] += 1
				binEncodingL = []
				for j in range(1,len(varsL)):
					binEncodingL.append(binsL[i][j])
				place.binEncodingL = binEncodingL
				g.placeD[key] = place
			#Add the rate for the time point to the place's rate list
			for j in range(1,len(varsL)):
				if varsL[j].dmvc:
				  #Don't add rates for DMVC vars
					continue
				oldR = (place.minRate(j),place.maxRate(j))
				place.ratesL[j].append(ratesL[i][j])
				newR = (place.minRate(j),place.maxRate(j))
				#print "New rate?"+str(oldR)+":"+str(newR)
				newRate = False
				newRate = cvg.isNewRate(oldR,newR)
				#if(newRate):
					#print "New rate:p"+str(place.binEncodingL)+":"+str(newR)+"--"+str(oldR)
			#If the bin encoding for the place has changed then find & update or add the corresponding transition
			if prevTranKey != key:
				if g.transitionD.has_key((prevTranKey,key)):
					transition = g.transitionD.get((prevTranKey,key))
				elif prevPlace:
					#print "Creating new transition (p"+str(prevPlace.placeNum)+"->p"+str(place.placeNum)+") for i value:"+str(i)+" key:"+key
					transition = Transition(len(varsL),place,prevPlace)
					transition.core = True
					cvg.transitionsL[cvg.index] += 1
					g.transitionD[(prevTranKey,key)] = transition
				if transition:
					if prevPlace:
						if transition not in prevPlace.outgoingL:
							prevPlace.outgoingL.append(transition)
					if transition not in place.incomingL:
						place.incomingL.append(transition)
				prevTranKey = key
			#Add the rate for the time point to the transition's rate list
			if transition:
				for j in range(1,len(varsL)):
					if varsL[j].dmvc:
				    #Don't add rates for DMVC vars
						continue
					transition.ratesL[j].append(ratesL[i][j])

##############################################################################
# Update the time information in the graph.
##############################################################################
def updateTimeInfo(g,varsL,datL,dmvcRunL,tParam,cvg,divisionsL):
	prevTranKey = "" #previous place key used for generating transitions
	place = None #current place
	prevPlace = None #previous place (required to build outgoingL)
	transition = None
	for i in range(len(varsL)):
		#Only produce time based loops for DMVC input variables
		if varsL[i].dmvc and varsL[i].input:
			#print "Working on DMVC variable: "+varsL[i].name
			dmvcCnt = 0
			dmvcPlaceL = []
			prevPlace = None
			place = None
			transition = None
			for runPart in dmvcRunL[i]:
				#print "runPart:"+str(runPart)+" Time:"+str(datL[runPart.endPoint][0]-datL[runPart.startPoint][0])
				exists = False
				#check to see if a place already exists for that value
				placeL = g.placeD.values()
				if len(placeL) > 1:
					for p in placeL:
						#print "p:"+str(p)
						if p.isDmvcP():
							if tParam.epsilonEquiv(runPart.constVal(),p.dmvcVal) and p.dmvcVar == runPart.varInd:
								oldD = (p.minTime(),p.maxTime())
								p.dmvcTimeL[i].append(runPart.calcDelay(datL))
								newD = (p.minTime(),p.maxTime())
								#print "New delay1?"+str(oldD)+":"+str(newD)
								cvg.isNewDelay(oldD,newD)
								exists = True
								prevPlace = place
								place = p
				#if the place doesn't already exist create it
				if not exists:
					#print "The place doesn't exist...creating it for constVal:"+str(runPart.constVal())
					prevPlace = place
					place = Place(varsL,DMVC)
					cvg.placesL[cvg.index] += 1
					place.binEncodingL = ["d_",str(i),"_"+str(dmvcCnt)]
					dmvcCnt = dmvcCnt + 1
					place.dmvcVar = i
					oldD = (place.minTime(),place.maxTime())
					place.dmvcTimeL[i].append(runPart.calcDelay(datL))
					newD = (place.minTime(),place.maxTime())
					#print "New delay2?"+str(oldD)+":"+str(newD)
					cvg.isNewDelay(oldD,newD)
					place.dmvcVal = runPart.constVal()
					dmvcPlaceL.append(place)
					g.placeD[place.keyStr()] = place
# 				else:
					#print "The place exists...updating it for constVal:"+str(runPart.constVal())
					#update the place with additional timing values
# 					oldD = (place.minTime(),place.maxTime())
# 					place.dmvcTimeL[i].append(runPart.calcDelay(datL))
# 					newD = (place.minTime(),place.maxTime())
# 					#print "New delay3?"+str(oldD)+":"+str(newD)
# 					cvg.isNewDelay(oldD,newD)
				#Create new transitions as needed and connect them to the appropriate places
				if prevPlace:
					if g.transitionD.has_key((prevPlace.keyStr(),place.keyStr())):
						transition = g.transitionD.get((prevPlace.keyStr(),place.keyStr()))
					else:
						transition = Transition(len(varsL),place,prevPlace)
						transition.core = True
						cvg.transitionsL[cvg.index] += 1
						g.transitionD[(prevPlace.keyStr(),place.keyStr())] = transition
					if transition not in prevPlace.outgoingL:
						prevPlace.outgoingL.append(transition)
					if transition not in place.incomingL:
						place.incomingL.append(transition)
			for p in dmvcPlaceL:
				if len(p.dmvcTimeL[p.dmvcVar]) > 1:
					#print "Checking remove for p"+str(p.placeNum)+"["+varsL[p.dmvcVar].name+"]"
				  #print "List: "+str(p.dmvcTimeL[p.dmvcVar])
					#print "Checking first item: "+str(p.dmvcTimeL[p.dmvcVar][0])
					if p.dmvcTimeL[p.dmvcVar][0] > p.avgTime():
					  #print "max()1:"+str(max(p.dmvcTimeL[p.dmvcVar][1:]))
					  #print "percent:"+str(max(p.dmvcTimeL[p.dmvcVar][1:])*decPercent)+" difference:"+str(p.dmvcTimeL[p.dmvcVar][0]-max(p.dmvcTimeL[p.dmvcVar][1:]))
						if p.dmvcTimeL[p.dmvcVar][0] > max(p.dmvcTimeL[p.dmvcVar][1:]):
							if p.dmvcTimeL[p.dmvcVar][0]-max(p.dmvcTimeL[p.dmvcVar][1:]) > decPercent*max(p.dmvcTimeL[p.dmvcVar][1:]):
								#print "Removing:"+str(p.dmvcTimeL[p.dmvcVar][0])
								p.dmvcTimeL[p.dmvcVar].pop(0)
					else:
					  #print "min()2:"+str(min(p.dmvcTimeL[p.dmvcVar][1:]))
					  #print "percent:"+str(min(p.dmvcTimeL[p.dmvcVar][1:])*decPercent)+" difference:"+str(min(p.dmvcTimeL[p.dmvcVar][1:])-p.dmvcTimeL[p.dmvcVar][0])
						if p.dmvcTimeL[p.dmvcVar][0] < min(p.dmvcTimeL[p.dmvcVar][1:]):
							if (min(p.dmvcTimeL[p.dmvcVar][1:])-p.dmvcTimeL[p.dmvcVar][0]) > decPercent*min(p.dmvcTimeL[p.dmvcVar][1:]):
								#print "Removing:"+str(p.dmvcTimeL[p.dmvcVar][0])
								p.dmvcTimeL[p.dmvcVar].pop(0)

					#print "Checking last item: "+str(p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1])
					if p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1] > p.avgTime():
					  #print "max()3:"+str(max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1]))
					  #print "percent:"+str(max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])*decPercent)+" difference:"+str(p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL)-1]-max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1]))
						if p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1] > max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1]):
							if (p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL)-1]-max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])) > max(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])*decPercent:
								#print "Removing:"+str(p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1])
								p.dmvcTimeL[p.dmvcVar].pop(len(p.dmvcTimeL[p.dmvcVar])-1)
					else:
					  #print "min()4:"+str(min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1]))
					  #print "percent:"+str(min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])*decPercent)+" difference:"+str(min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])-p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1])
						if p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1] < min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1]):
							if (min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])-p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1]) > min(p.dmvcTimeL[p.dmvcVar][:len(p.dmvcTimeL)-1])*decPercent:
								#print "Removing:"+str(p.dmvcTimeL[p.dmvcVar][len(p.dmvcTimeL[p.dmvcVar])-1])
								p.dmvcTimeL[p.dmvcVar].pop(len(p.dmvcTimeL[p.dmvcVar])-1)
				  #print "List: "+str(p.dmvcTimeL[p.dmvcVar])
		elif varsL[i].dmvc:
			#process non-input DMVC variables
			if g.placeD.has_key("d_"+str(i)+"_0"):
				place = g.placeD.get("d_"+str(i)+"_0")
			else:
				place = Place(varsL,DMVC)
				cvg.placesL[cvg.index] += 1
				place.binEncodingL = ["d_",str(i),"_0"]
				place.dmvcVar = i
				place.dmvcVal = dmvcRunL[i][0].constVal()
				g.placeD[place.keyStr()] = place
				for j in range(len(divisionsL[i])+1):
					place.dmvcInfoL.append(AsgnPart())
					place.dmvcInfoL[j].var = i
			for runPart in dmvcRunL[i]:
				foundBin = False
				for j in range(len(divisionsL[i])):
					if runPart.constVal() <= divisionsL[i][j]:
						#add a delay of 0 for now
						#print "updateTIME1 "+varsL[i].name+"["+place.keyStr()+"]"+" p"+str(place.placeNum)+".dmvcInfoL["+str(j)+"]:"+str(runPart.constVal()) 
						place.dmvcInfoL[j].valL.append(runPart.constVal())
						place.dmvcInfoL[j].delayL.append(0.0)
						foundBin = True
						break
				if not foundBin:
					#print "updateTIME2 "+varsL[i].name+"["+place.keyStr()+"]"+" p"+str(place.placeNum)+".dmvcInfoL["+str(len(divisionsL[i]))+"]:"+str(runPart.constVal()) 
					place.dmvcInfoL[len(divisionsL[i])].valL.append(runPart.constVal())
					place.dmvcInfoL[len(divisionsL[i])].delayL.append(0.0)
					
##############################################################################
# Generate the graph based on the time series data and bin encodings.
##############################################################################
def updateGraph(g,varsL,datL,binsL,ratesL,dmvcRunL,tParam,failProp,cvg,divisionsL):
	prevTranKey = "" #previous place key used for generating transitions
	place = None #current place
	prevPlace = None #previous place (required to build outgoingL)
	transition = None
	#Generate rate based places & transitions
	updateRateInfo(g,varsL,datL,binsL,ratesL,cvg)
	#Generate time based places and transitions
	updateTimeInfo(g,varsL,datL,dmvcRunL,tParam,cvg,divisionsL)
	#Assign the proper initial conditions and markings
	initMark = 0
	for i in range(1,len(varsL)):
		if not (varsL[i].dmvc and varsL[i].input):
			#rate based  and noninput DMVC places
			for j in range(len(datL)):
				if ratesL[j][i] != "-":
					g.initValL[i].append(datL[j][i])
					g.initRateL[i].append(ratesL[j][i])
					initMark = j
					break
			key = ""
			for k in range(1,len(varsL)):
				key = key + str(binsL[initMark][k])
			if not (g.placeD.get(key) in g.initMarkingL):
				g.initMarkingL.append(g.placeD.get(key))
		else:
			#dmvc input places
			place = g.placeD.get("d_"+str(i)+"_0")
			if place:
				g.initValL[i].append(place.dmvcVal)
				g.initRateL[i].append("-")
				if not(place in g.initMarkingL) and varsL[i].input:
				  #print "Adding DMVC place p"+str(place.placeNum)+" to initMarking."
					g.initMarkingL.append(place)
			else:
				cStr = cText.cSetFg(cText.RED)
				cStr += "ERROR:"
				cStr += cText.cSetAttr(cText.NONE)
				print cStr+" No initial place was found for "+varsL[i].name+"."
				sys.exit()

##############################################################################
# Add places and transitions to the graph to do non-input DMVC variable
# assignments.
##############################################################################
def expandGraph(g,varsL):
	#print "Initial graph:"+str(g)
	exG = copy.deepcopy(g)
	#return exG
	transitionL = exG.transitionD.values()
	transitionL.sort()
	nonInDmvcL = []
	for i in range(len(varsL)):
		if varsL[i].dmvc and not varsL[i].input:
			nonInDmvcL.append(i)
	for t in transitionL:
		if t.outgoingP and t.incomingP and t.incomingP.isRateP() and t.outgoingP.isRateP():
			#print "Working on t"+str(t.transitionNum)
			diffL = t.outgoingP.diff(t.incomingP,varsL)
			prevPlaceNum = t.incomingP.placeNum
			curPlaceNum = t.outgoingP.placeNum
			for var in diffL:
				if var in nonInDmvcL:
					srcP = exG.placeD.get("d_"+str(var)+"_0")
					#print "Creating/modifying ex_"+str(prevPlaceNum)+"_"+str(curPlaceNum)
					if exG.placeD.has_key("ex_"+str(prevPlaceNum)+"_"+str(curPlaceNum)):
						pEx = exG.placeD.get("ex_"+str(prevPlaceNum)+"_"+str(curPlaceNum))
					else:
						#Create a new place that is spliced into the graph by
						#point t to the new place and pointing the new transition
						#to curPlace
						pEx = Place(varsL,ASGN)
						pEx.binEncodingL = ["ex_",str(prevPlaceNum),"_",str(curPlaceNum)]
						pEx.asgBinL = t.incomingP.binEncodingL
						pEx.incomingL.append(t)
						tEx = Transition(len(varsL),t.outgoingP,pEx)
						tEx.core = True
						pEx.outgoingL.append(tEx)
						preExP = g.placeD.get(t.incomingP.keyStr())
						pEx.preExGrandParentBinL = preExP.incomingL[0].incomingP
						t.outgoingP.incomingL.remove(t)
						t.outgoingP.incomingL.append(tEx)
						t.outgoingP = pEx
						exG.placeD[pEx.keyStr()] = pEx
						exG.transitionD[(t.outgoingP.keyStr(),pEx.keyStr())] = tEx
					nextT = pEx.outgoingL[0]
					#Use a deep copy here b/c the future of these lists requires
					#them to actually be separate objects that can be modified
					#independently
					pEx.asg.append(copy.deepcopy(srcP.dmvcInfoL[nextT.outgoingP.binEncodingL[var-1]]))
	return exG

##############################################################################
# Replace transitions with multiple bin changes with new transitions that are # only single bin changes.
##############################################################################
def rmMultipleBinChange(g,varsL):
	sbcG = copy.deepcopy(g)
	transitionL = sbcG.transitionD.values()
	transitionL.sort()
	for t in transitionL:
		if t.outgoingP and t.incomingP:
			diffL = t.outgoingP.diff(t.incomingP,varsL)
			if len(diffL) > 1:
				print "t" + str(t.transitionNum) + " has multiple bin changes...(p" + str(t.incomingP.placeNum) + ",p" + str(t.outgoingP.placeNum) + ")"
				
	return sbcG

##############################################################################
# Return the minimum division value.
##############################################################################
def minDiv(divisionsL):
	minDivision = None
	for i in range(1,len(divisionsL)):
		for j in range(len(divisionsL[i])):
			if not minDivision:
				minDivision = divisionsL[i][j]
			elif divisionsL[i][j] < minDivision:
				minDivision = divisionsL[i][j]
	return minDivision

##############################################################################
# Return the maximum division value.
##############################################################################
def maxDiv(divisionsL):
	maxDivision = None
	for i in range(1,len(divisionsL)):
		for j in range(len(divisionsL[i])):
			if not maxDivision:
				maxDivision = divisionsL[i][j]
			elif divisionsL[i][j] > maxDivision:
				maxDivision = divisionsL[i][j]
	return maxDivision

##############################################################################
# Adjust the values of rates, delay values, and thresholds so that both can
# be represnted as integers of sufficient accuracy in the LHPN model.
##############################################################################
def normalizeValues(g,varsL,divisionsL):
	normG = copy.deepcopy(g)
	normDivisionsL = copy.deepcopy(divisionsL)
	placeL = normG.placeD.values()
	placeL.sort()
	transitionL = normG.transitionD.values()
	transitionL.sort()

	minDelay = g.minDelay()
	maxDelay = g.maxDelay()
	print "minDelay:"+str(minDelay)+" maxDelay:"+str(maxDelay)
	scaleFactor = 1.0
  #Determine the scaling factor (using factors of 10) to bring the
	#smallest delay value above minDelayVal
	if minDelay:
		for i in range(18):
			if scaleFactor > (minDelayVal/minDelay):
				break
			scaleFactor *= 10.0
		print "minDelay is: "+str(minDelay*scaleFactor)+" after scaling by "+str(scaleFactor)
	#check for overflow then scale the appropriate graph values
	if maxDelay and int(maxDelay*scaleFactor) > sys.maxint:
		cStr = cText.cSetFg(cText.YELLOW)
		cStr += "WARNING:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" Delay scaling has caused an overflow."
	normG.delayScaleFactor = scaleFactor
	normG.scaleDelay(scaleFactor,False)
	
	minRate = normG.minRate()
	maxRate = normG.maxRate()
	print "minRate:"+str(minRate)+" maxRate:"+str(maxRate)
	scaleFactor = 1.0
	#Determine the scaling factor (using factors of 10) to bring the
	#smallest rate value above minRateVal
	if minRate:
		for i in range(14):
			if scaleFactor > abs(minRateVal/minRate):
				break
			scaleFactor *= 10.0
		for i in range(14):
			#TODO: Make this number configurable?
			if abs(maxRate*scaleFactor) < (sys.maxint/1000):
				break
			scaleFactor /= 10.0
		print "minRate is: "+str(minRate*scaleFactor)+" after scaling by "+str(scaleFactor)
	else:
		print "No minimum rate."
	#check for overflow then scale the appropriate graph values
	if maxRate and int(maxRate*scaleFactor) > sys.maxint:
		cStr = cText.cSetFg(cText.YELLOW)
		cStr += "WARNING:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" Rate scaling has caused an overflow."
	normG.varScaleFactor = scaleFactor
	normG.scaleVariable(scaleFactor,normDivisionsL)
			
	#Divisions need to be scaled so that they are larger than
	#minDivisionVal.  Yes, it is possible this is accomplished w/ rate
	#scaling unless there aren't any rates based variables in the
	#system.  In that case this code should move the divisions
	#appropriately.
	minDivision = minDiv(normDivisionsL)
	maxDivision = maxDiv(normDivisionsL)
	print "minDivision:"+str(minDivision)+" maxDivision:"+str(maxDivision)
	scaleFactor = 1.0
	print "minDivision: "+str(minDivision)
	if minDivision and minDivision != 0:
		for i in range(14):
			if abs(scaleFactor * minDivision) > minDivisionVal:
				break
			scaleFactor *= 10
	print "minDivision is: "+str(minDivision*scaleFactor)+" after scaling by "+str(scaleFactor)
	#check for overflow then scale the appropriate graph values
	if int(maxDivision*scaleFactor) > sys.maxint:
		cStr = cText.cSetFg(cText.YELLOW)
		cStr += "WARNING:"
		cStr += cText.cSetAttr(cText.NONE)
		print cStr+" Division scaling has caused an overflow."
	normG.varScaleFactor *= scaleFactor
	normG.scaleVariable(scaleFactor,normDivisionsL)
	#normG.scaleDelay(1e15,False)
	return normG, normDivisionsL

##############################################################################
# Write out a *.sort file containing the derived bin encodings.
##############################################################################
def writeSortFile(varsL,numPoints,datL,binsL,sortFile):
	outputF = open(sortFile, 'w')
	outputF.write("Variables: "+str(len(varsL))+"\n")
	outputF.write("Points: "+str(numPoints)+"\n")
	for i in range(len(varsL)):
		outputF.write(varsL[i].name+" ")
	outputF.write("\n")
	for i in range(len(datL)):
		for j in range(len(datL[i])):
			outputF.write(str(datL[i][j])+" ")
		for j in range(1,len(binsL[i])):
			outputF.write(str(binsL[i][j])+" ")
		outputF.write("\n")
	outputF.close()

##############################################################################
# Write out a *.rate file containing the derived bin encodings and calculated
# rates.
##############################################################################
def writeRateFile(varsL,numPoints,datL,binsL,ratesL,rateFile):
	outputF = open(rateFile, 'w')
	outputF.write("Variables: "+str(len(varsL))+"\n")
	outputF.write("Points: "+str(numPoints)+"\n")
	for i in range(len(varsL)):
		outputF.write(varsL[i].name+" ")
	outputF.write("\n")
	for i in range(len(datL)):
		for j in range(len(datL[i])):
			outputF.write(str(datL[i][j])+" ")
		for j in range(1,len(binsL[i])):
			outputF.write(str(binsL[i][j])+" ")
		for j in range (1,len(binsL[i])):
			outputF.write(str(ratesL[i][j])+" ")
		outputF.write("\n")
	outputF.close()

##############################################################################
# Write out an informational header for the output file given a string for
# comments (cStr).
##############################################################################
def writeInformationalHeader(outputF,cStr,tParam,g):
	outputF.write(cStr+"\n")
	outputF.write(cStr+"Delay scale factor: "+str(g.delayScaleFactor)+"\n")
	outputF.write(cStr+"Variable scale factor: "+str(g.varScaleFactor)+"\n")
	outputF.write(cStr+"\n")

##############################################################################
# Add places and transitions to support the meta-bins for limited variables.
##############################################################################
def addMetaBins(g,varsL,divisionsL):
	print "addMetaBins:begin"
	placeL = g.placeD.values()
	placeL.sort()
	for p in placeL:
		if p.isRateP():
			print "Working on:" + str(p)
			for i in range(len(p.binEncodingL)):
				if int(p.minRateInt(i+1)) < 0:
					syncBinEncodingL = copy.deepcopy(p.binEncodingL)
					foundBin = False
					while not foundBin:
						syncBinEncodingL[i] = syncBinEncodingL[i] - 1
						if syncBinEncodingL[i] == -1 and not g.placeD.has_key(binEncoding2Str(syncBinEncodingL)):
							#Create a new meta-bin for the place
							foundBin = True
							metaP = Place(varsL,RATE)
							placeL.append(metaP) #new
							metaP.ratesL = copy.deepcopy(p.ratesL)
							metaP.rmRateLtZero(i+1)
							metaP.binEncodingL = syncBinEncodingL
							g.placeD[metaP.keyStr()] = metaP
							t = Transition(len(varsL),metaP,p)
							g.transitionD[(p.keyStr(),metaP.keyStr())] = t
							p.outgoingL.append(t)
							metaP.incomingL.append(t)
						elif g.placeD.has_key(binEncoding2Str(syncBinEncodingL)):
							foundBin = True
							syncP = g.placeD.get(binEncoding2Str(syncBinEncodingL))
							if not g.transitionD.has_key((p.keyStr(),syncP.keyStr())):
								t = Transition(len(varsL),syncP,p)
								g.transitionD[(p.keyStr(),syncP.keyStr())] = t
								p.outgoingL.append(t)
								syncP.incomingL.append(t)
				if int(p.maxRateInt(i+1)) > 0:
					syncBinEncodingL = copy.deepcopy(p.binEncodingL)
					foundBin = False
					while not foundBin:
						syncBinEncodingL[i] = syncBinEncodingL[i] + 1
						if syncBinEncodingL[i] == len(divisionsL[i+1])+1 and not g.placeD.has_key(binEncoding2Str(syncBinEncodingL)):
							#Create a new meta-bin for the place
							foundBin = True
							metaP = Place(varsL,RATE)
							placeL.append(metaP) #new
							metaP.ratesL = copy.deepcopy(p.ratesL)
							metaP.rmRateGtZero(i+1)
							metaP.binEncodingL = syncBinEncodingL
							g.placeD[metaP.keyStr()] = metaP
							t = Transition(len(varsL),metaP,p)
							g.transitionD[(p.keyStr(),metaP.keyStr())] = t
							p.outgoingL.append(t)
							metaP.incomingL.append(t)
						elif g.placeD.has_key(binEncoding2Str(syncBinEncodingL)):
							foundBin = True
							syncP = g.placeD.get(binEncoding2Str(syncBinEncodingL))
							if not g.transitionD.has_key((p.keyStr(),syncP.keyStr())):
								t = Transition(len(varsL),syncP,p)
								g.transitionD[(p.keyStr(),syncP.keyStr())] = t
								p.outgoingL.append(t)
								syncP.incomingL.append(t)
	print "addMetaBins:end"

##############################################################################
# Convert a binEncodingL to a string.
##############################################################################
def binEncoding2Str(binEncodingL):
	keyStr = ""
	for i in range(len(binEncodingL)):
		keyStr += str(binEncodingL[i])
	return keyStr

##############################################################################
# 
##############################################################################
def writePSfile(g,varsL,psFile):
	outputF = open(psFile, 'w')
	#writePSfile(outputF)
	placeL = g.placeD.values()
	placeL.sort()
	for p in placeL:
		for i in range(len(p.binEncodingL)):
			outputF.write(str(p.binEncodingL[i]+1)+" ")
		outputF.write("(p" + str(p.placeNum)+ ") labelbox\n")
	outputF.write("\n")
	transitionL = g.transitionD.values()
	transitionL.sort()
	for t in transitionL:
		pSrc = t.incomingP
		pSync = t.outgoingP
		diffL = pSrc.diff(pSync,varsL)
		if diffL[0] == 1 and len(diffL) == 1:
			#outputF.write("t"+str(t.transitionNum)+" ")
			outputF.write(str(pSrc.binEncodingL[1]+1) + " " + str(pSrc.binEncodingL[0]+1) + " " + str(pSync.binEncodingL[0]+1) + " ")
			if pSync.binEncodingL[0] > pSrc.binEncodingL[0]:
				if t.core:
					outputF.write("drawArrowRight\n")
				else:
					outputF.write("drawDashArrowRight\n")
			else:
				if t.core:
					outputF.write("drawArrowLeft\n")
				else:
					outputF.write("drawDashArrowLeft\n")
		elif diffL[0] == 2 and len(diffL) == 1:
			outputF.write(str(pSrc.binEncodingL[0]+1) + " " + str(pSrc.binEncodingL[1]+1) + " " + str(pSync.binEncodingL[1]+1) + " ")
			if pSync.binEncodingL[1] > pSrc.binEncodingL[1]:
				if t.core:
					outputF.write("drawArrowUp\n")
				else:
					outputF.write("drawDashArrowUp\n")
			else:
				if t.core:
					outputF.write("drawArrowDown\n")
				else:
					outputF.write("drawDashArrowDown\n")

##############################################################################
# Write out a *.g file for derived graph.
##############################################################################
def writeGfile(varsL,datL,binsL,ratesL,divisionsL,tParam,g,gFile):
	outputF = open(gFile, 'w')
	writeInformationalHeader(outputF,"#",tParam,g)
	outputF.write(".outputs fail")
	outputF.write("\n")
	outputF.write(".dummy")
	placeL = g.placeD.values()
	placeL.sort()
	transitionL = g.transitionD.values()
	transitionL.sort()
	for t in transitionL:
		outputF.write(" t" + str(t.transitionNum))
	outputF.write("\n")
	outputF.write("#@@.variables")
	for i in range(1,len(varsL)):
		outputF.write(" "+varsL[i].name)
	outputF.write("\n")
	outputF.write("#@@.init_state [0")
	outputF.write("]\n")
	#write out the graph of places and transitions
	outputF.write(".graph\n")
	for p in placeL:
		if p.incomingL:
			for t in p.incomingL:
				outputF.write("t"+str(t.transitionNum)+" p"+str(p.placeNum)+"\n")
		if p.outgoingL:
			for t in p.outgoingL:
				outputF.write("p"+str(p.placeNum)+" t"+str(t.transitionNum)+"\n")
	outputF.write(".marking {")
	for p in g.initMarkingL:
		outputF.write("p"+str(p.placeNum)+" ")
	outputF.write("}\n")
	outputF.write("#@@.init_vals {")
	for i in range(1,len(varsL)):
		outputF.write("<" + varsL[i].name + "=[" + g.minInitValInt(i) + "," + g.maxInitValInt(i) + "]>")
	outputF.write("}\n")
	outputF.write("#@@.init_rates {")
	for i in range(1,len(varsL)):
		if g.initRateL[i][0] == "-" or varsL[i].dmvc:
			outputF.write("<"+varsL[i].name+"=0>")
		else:
			outputF.write("<" + varsL[i].name + "=[" + g.minInitRateInt(i) + "," + g.maxInitRateInt(i) + "]>")
	outputF.write("}\n")
	outputF.write("#@@.enablings {")
	if g.failProp:
		enFailAnd = "&~fail"
		enFail = "~fail"
	else:
		enFailAnd = ""
		enFail = ""
	for t in transitionL:
		if t.outgoingP and t.incomingP and t.incomingP.isRateP() and t.outgoingP.isRateP():
			diffL = t.outgoingP.diff(t.incomingP,varsL)
			#print "t"+str(t.transitionNum)+" - len(diffL):"+str(len(diffL))
			condStr = ""
			for ind in range(len(diffL)):
				if ind > 0:
					condStr += "&"
				i = diffL[ind]
				#Note: the i-1 floating around here are to compensate for the fact that binEncoding doesn't include time (varsL[0]), so to index into binEncoding using a varsL index you must subtract 1
				if (t.incomingP.binEncodingL[i-1] < t.outgoingP.binEncodingL[i-1]):
					print "Incoming place:" + t.incomingP.keyStr() + " Outgoing place:" + t.outgoingP.keyStr()
					if t.incomingP.binEncodingL[i-1] == -1:
						val = minVarValL[i]
						condStr += "(" + varsL[i].name + ">=" + val + ")"
					elif t.incomingP.binEncodingL[i-1] == len(divisionsL[i]):
						val = maxVarValL[i]
						condStr += "(" + varsL[i].name + ">=" + val + ")"
					else:
						print "binEncodingL:" + str(t.incomingP.binEncodingL[i-1])
						val = str(int(divisionsL[i][t.incomingP.binEncodingL[i-1]]))
						condStr += "(" + varsL[i].name + ">=" + val + ")"
				else:
					print "Incoming place:" + t.incomingP.keyStr() + " Outgoing place:" + t.outgoingP.keyStr()
					if t.outgoingP.binEncodingL[i-1] == -1:
						val = minVarValL[i]
						condStr += "~(" + varsL[i].name + ">=" + val + ")"
					elif t.outgoingP.binEncodingL[i-1] == len(divisionsL[i]):
						val = maxVarValL[i]
						condStr += "~(" + varsL[i].name + ">=" + val + ")"
					else:
						val = str(int(divisionsL[i][t.outgoingP.binEncodingL[i-1]]))
						condStr += "~(" + varsL[i].name + ">=" + val + ")"
			t.enabling = condStr + enFailAnd
			outputF.write("<t" + str(t.transitionNum) + "=[" + condStr + enFailAnd + "]>")
		if t.outgoingP and t.incomingP and t.incomingP.isDmvcP() and t.outgoingP.isDmvcP() and enFail:
			t.enabling = enFail
			outputF.write("<t" + str(t.transitionNum) + "=[" + enFail + "]>")
		if t.incomingP and t.incomingP.isPropP():
			t.enabling = t.incomingP.property
			outputF.write("<t" + str(t.transitionNum) + "=[" + t.incomingP.property + "]>")
		if t.outgoingP and t.incomingP and t.incomingP.isAsgnP() and t.outgoingP.isRateP():
			diffL = t.outgoingP.diff(t.incomingP,varsL)
			condStr = ""
			for ind in range(len(diffL)):
				if ind > 0:
					condStr += "&"
				i = diffL[ind]
				#Note: the i-1 floating around here are to compensate for the fact that binEncoding doesn't include time (varsL[0]), so to index into binEncoding using a varsL index you must subtract 1
				if (t.incomingP.asgBinL[i-1] < t.outgoingP.binEncodingL[i-1]):
					val = str(int(divisionsL[i][t.incomingP.asgBinL[i-1]]))
					condStr += "(" + varsL[i].name + ">=" + val + ")"
				else:
					val = str(int(divisionsL[i][t.outgoingP.binEncodingL[i-1]]))
					condStr += "~(" + varsL[i].name + ">=" + val + ")"
			t.enabling = condStr + enFailAnd
			outputF.write("<t" + str(t.transitionNum) + "=[" + condStr + enFailAnd + "]>")
	outputF.write("}\n")
	hasRates = False
	for i in range(1,len(varsL)):
		if not varsL[i].dmvc:
			hasRates = True
			break
	if hasRates:
		outputF.write("#@@.rate_assignments {")
		if placeRates:
		  #Place based rate generation output
			for p in placeL:
				if p.isRateP():
					for t in p.incomingL:
						for i in range(1,len(varsL)):
							if not varsL[i].dmvc:
								outputF.write("<t" + str(t.transitionNum) + "=[" + varsL[i].name + ":=[" + p.minRateInt(i) + "," + p.maxRateInt(i) + "]]>")
		else:
		  #Transition based rate generation output
			for t in transitionL:
				if t.outgoingP and t.incomingP:
					diffL = t.outgoingP.diff(t.incomingP,varsL)
					for i in diffL:
						if t.incomingP.isRateP():
							outputF.write("<t" + str(t.transitionNum) + "=[" + varsL[i].name + ":=[" + t.minRateInt(i) + "," + t.maxRateInt(i) + "]]>")
		outputF.write("}\n")
	if dmvcVarExists(varsL):
		flag = 0
		#outputF.write("#@@.assignments {")
		for p in placeL:
			if p.isDmvcP():
				for t in p.incomingL:
					if flag == 0:
						outputF.write("#@@.assignments {")
					flag = 1
					outputF.write("<t" + str(t.transitionNum) + "=[" + varsL[p.dmvcVar].name + ":=" + str(int(p.dmvcVal)) + "]>")
			if p.isAsgnP():
				for a in p.asg:
					if a.valL:
						for t in p.incomingL:
							if flag == 0:
								outputF.write("#@@.assignments {")
							flag = 1
							outputF.write("<t" + str(t.transitionNum) + "=[" + varsL[a.var].name + ":=" + str(int(a.avgValue())) + "]>")
		if flag == 1:
			outputF.write("}\n")
		flag = 0
		#outputF.write("#@@.delay_assignments {")
		for p in placeL:
			if p.isDmvcP():
				for t in p.outgoingL:
					if flag == 0:
						outputF.write("#@@.delay_assignments {")
					flag = 1
					outputF.write("<t" + str(t.transitionNum) + "=[" + p.minTimeInt() + "," + p.maxTimeInt() + "]>")
		if flag == 1:
			outputF.write("}\n")
	if g.failProp:
		outputF.write("#@@.boolean_assignments {")
		for p in placeL:
			if p.isPropP():
				for t in p.outgoingL:
					outputF.write("<t"+str(t.transitionNum)+"=[fail:=TRUE]>")
		outputF.write("}\n")
	outputF.write("#@@.continuous")
	for i in range(1,len(varsL)):
		outputF.write(" "+varsL[i].name)
	outputF.write("\n")
	outputF.write(".end\n")
	outputF.close()

##############################################################################
# Compare DMVC places by the dmvcVar.
##############################################################################
def placeVarCmp(a,b):
	if a.dmvcVar < b.dmvcVar:
		return -1
	elif a.dmvcVar == b.dmvcVar:
		return 0
	else:
		return 1

##############################################################################
# Compare RATE places according to their bins.
##############################################################################
def placeBinCmp(a,b):
	for i in range(len(a.binEncodingL)):
		if a.binEncodingL[i] < b.binEncodingL[i]:
			return -1
		elif a.binEncodingL[i] > b.binEncodingL[i]:
			return 1
	return 0

##############################################################################
# Write out a *.va file for derived graph.
##############################################################################
def writeVerilogAfile(varsL,datL,binsL,ratesL,divisionsL,tParam,g,vaFile):
	outputF = open(vaFile, 'w')
	outputF.write("`include \"disciplines.h\"\n\n")
	baseFileL = os.path.splitext(vaFile)
	outputF.write("module "+baseFileL[0]+"(")
	for i in range(1,len(varsL)):
		if i != 1:
			outputF.write(",")
		outputF.write(varsL[i].name+"_io")
	outputF.write(");\n")
	
  #Create electrical io variables for the inputs/outputs of the system
	for i in range(1,len(varsL)):
		outputF.write("\tinout "+varsL[i].name+"_io;\n")
		outputF.write("\telectrical "+varsL[i].name+"_io;\n")
	outputF.write("\n")

	#create real variables for each variable in the system
	for i in range(1,len(varsL)):
		#print varsL[i].name+" "+str(varsL[i].input)+" "+str(varsL[i].dmvc)
		if not varsL[i].input:
			outputF.write("\treal "+varsL[i].name+"_var;\n")
	outputF.write("\n")

	#create real variables for the rate of each continuous variable
	#(DMVC variables and handled with time and as such don't need a
	#variable
	rateVarL = []
	for i in range(1,len(varsL)):
		if not varsL[i].dmvc and not varsL[i].input:
			rateVarL.append(i)
			outputF.write("\treal rate_"+varsL[i].name+";\n")
	outputF.write("\n")
	
	outputF.write("\n\tanalog begin\n")

	#Setup the initial values and rates
	outputF.write("\t\t@@(initial_step) begin\n")
	for i in range(1,len(varsL)):
		if not varsL[i].input:
			outputF.write("\t\t\t"+varsL[i].name+"_var = "+str(g.initValL[i][0])+";\n")
		if not varsL[i].dmvc and not varsL[i].input:
			outputF.write("\t\t\trate_"+varsL[i].name+" = "+str(g.initRateL[i][0]*tParam.vaRateUpdateInterval)+";\n")
	outputF.write("\t\tend\n\n")

	#Create lists of appropriately sorted places.
	placeL = g.placeD.values()
	placeL.sort()
	ratePlaceL = []
	dmvcPlaceL = []
	asgnPlaceL = []
	for p in placeL:
		if p.isRateP():
			ratePlaceL.append(p)
		elif p.isDmvcP():
			if varsL[p.dmvcVar].input:
				dmvcPlaceL.append(p)
		elif p.isAsgnP():
			asgnPlaceL.append(p)
	dmvcPlaceL.sort(placeVarCmp)
	dmvcPlaceVarL = []
	for i in range(len(varsL)):
		dmvcPlaceVarL.append([])
	for p in dmvcPlaceL:
		dmvcPlaceVarL[p.dmvcVar].append(p)

	#Generate @@(cross) statements for continuous rate variables.  This
	#should work as the probability of two of these changing at the same
	#time is unlikely
	for p in ratePlaceL:
		tL = p.incomingL
		for t in tL:
			if t.outgoingP and t.incomingP and t.incomingP.isRateP() and t.outgoingP.isRateP():
				diffL = t.outgoingP.diff(t.incomingP,varsL)
				for i in diffL:
					#print "diff:"+str(varsL[i].name)
					hasRate = False
					for j in range(1,len(varsL)):
						if not varsL[j].dmvc and not varsL[j].input:
							hasRate = True
							break
				  #Note: the i-1 floating around here is to compensate for the
				  #fact that binEncoding doesn't include time (varsL[0]), so
				  #to index into binEncoding using a varsL index you must
				  #subtract 1
					if hasRate:
						if (t.incomingP.binEncodingL[i-1] < t.outgoingP.binEncodingL[i-1]):
							direction = "1"
							val = divisionsL[i][t.incomingP.binEncodingL[i-1]]
						else:
							direction = "-1"
							val = divisionsL[i][t.outgoingP.binEncodingL[i-1]]
						if varsL[i].input:
							outputF.write("\t\t@@(cross(V("+varsL[i].name+"_io) - "+str(val)+","+direction+")) begin\n")
						else:
							outputF.write("\t\t@@(cross("+varsL[i].name+"_var - "+str(val)+","+direction+")) begin\n")
						for j in range(1,len(varsL)):
							if not varsL[j].dmvc and not varsL[j].input:
								outputF.write("\t\t\trate_"+varsL[j].name+" = "+str(p.avgRate(j)*tParam.vaRateUpdateInterval)+";\n")
						outputF.write("\t\tend\n")
	outputF.write("\n")
	
	#Generate the if statements used for assignment places
	#print "Asg places...["+str(len(asgnPlaceL))+"]"
	for p in asgnPlaceL:
		#print "p"+str(p.placeNum)+"["+p.keyStr()+"]--["+p.preExGrandParentBinL.keyStr()+"]->["+p.incomingL[0].incomingP.keyStr()+"]->["+p.outgoingL[0].outgoingP.keyStr()+"]"
		outputF.write("\t\tif(")
		condStr = ""
		cnt = 0
		diffL = p.outgoingL[0].outgoingP.diff(p.incomingL[0].incomingP,varsL)
		#print "diffL:"+str(diffL)
		for i in range(1,len(varsL)):
			#print "divisionsL["+str(i)+"]:"+str(divisionsL[i])+" len:"+str(len(divisionsL[i]))
			#print "p.incomingL[0].incomingP.incomingL[0].incomingP.binEncodingL["+str(i-1)+"]"+str(p.incomingL[0].incomingP.incomingL[0].incomingP.binEncodingL)
			if cnt > 0:
				condStr = " && "
			cnt += 1
			if i in diffL and varsL[i].input:
				#case for the zero bin
				if (p.outgoingL[0].outgoingP.binEncodingL[i-1] == 0):
					val = str(divisionsL[i][p.outgoingL[0].outgoingP.binEncodingL[i-1]])
					outputF.write(condStr+"!(V("+varsL[i].name+"_io) >= "+val+")")
				#case for the high order bin
				elif (p.outgoingL[0].outgoingP.binEncodingL[i-1] == len(divisionsL[i])):
				  #print "p.incomingL[0].incomingP.binEncodingL["+str(len(divisionsL[i])-1)+"]"+str(p.incomingL[0].incomingP.binEncodingL)
					val = str(divisionsL[i][len(divisionsL[i])-1])
					outputF.write(condStr+"(V("+varsL[i].name+"_io) >= "+val+")")
				#case for everything else
				else:
					val = str(divisionsL[i][p.outgoingL[0].outgoingP.binEncodingL[i-1]])
					outputF.write(condStr+"(V("+varsL[i].name+"_io) >= "+val+")")
					val = str(divisionsL[i][p.outgoingL[0].outgoingP.binEncodingL[i]])
					outputF.write(condStr+"!(V("+varsL[i].name+"_io) >= "+val+")")
			else:
				#case for the zerio bin
				if (p.incomingL[0].incomingP.binEncodingL[i-1] == 0):
					val = str(divisionsL[i][p.incomingL[0].incomingP.binEncodingL[i-1]])
					outputF.write(condStr+"!(V("+varsL[i].name+"_io) >= "+val+")")
				#case for the high order bin
				elif (p.incomingL[0].incomingP.binEncodingL[i-1] == len(divisionsL[i])):
			    #print "p.incomingL[0].incomingP.binEncodingL["+str(len(divisionsL[i])-1)+"]"+str(p.incomingL[0].incomingP.binEncodingL)
					val = str(divisionsL[i][len(divisionsL[i])-1])
					outputF.write(condStr+"(V("+varsL[i].name+"_io) >= "+val+")")
				#case for everything else
				else:
					val = str(divisionsL[i][p.incomingL[0].incomingP.binEncodingL[i-1]])
					outputF.write(condStr+"(V("+varsL[i].name+"_io) >= "+val+")")
					val = str(divisionsL[i][p.incomingL[0].incomingP.binEncodingL[i]])
					outputF.write(condStr+"(V("+varsL[i].name+"_io) >= "+val+")")
		outputF.write(") begin\n")
		for a in p.asg:
			#print "Assignment:"+str(a)
			if a.valL:
				outputF.write("\t\t\t"+varsL[a.var].name+"_var = "+str(a.avgValue())+";\n")
		outputF.write("\t\tend\n")
	outputF.write("\n")	
		
	#Create a periodic timing loop for each DMVC variable to set its value
	#ASSUMPTION: all dmvc input variable graphs are loops
	for i in range(len(dmvcPlaceVarL)):
		period = 0.0
		for p in dmvcPlaceVarL[i]:
			period += p.avgTime()
		initVal = 0.0
		for p in dmvcPlaceVarL[i]:
			if not varsL[p.dmvcVar].input:
				outputF.write("\t\t@@(timer("+str(initVal)+","+str(period)+")) begin\n")
				outputF.write("\t\t\t"+varsL[p.dmvcVar].name+"_var = "+str(p.dmvcVal)+";\n")
				outputF.write("\t\tend\n")
				initVal += p.avgTime()
	outputF.write("\n")

	#Setup a timer to add the appropriate value to each continuous
	#variable at the proper time
	hasRate = False
	for i in range(1,len(varsL)):
		if not varsL[i].dmvc and not varsL[i].input:
			hasRate = True
			break
	if hasRate:
		outputF.write("\t\t@@(timer(0.0,"+str(tParam.vaRateUpdateInterval)+")) begin\n")
		for i in range(1,len(varsL)):
			if not varsL[i].dmvc and not varsL[i].input:
				outputF.write("\t\t\t"+varsL[i].name+"_var = "+varsL[i].name+"_var + rate_"+varsL[i].name+";\n")
		outputF.write("\t\tend\n\n")

	#Ensure that the values on the variables get pushed to the output
	#ports quickly
	for i in range(1,len(varsL)):
		if not varsL[i].input:
			outputF.write("\t\tV("+varsL[i].name+"_io) <+ transition("+varsL[i].name+"_var,1p,1p,1p);\n")
	
	outputF.write("\tend\n")
	outputF.write("endmodule\n")
	outputF.close()
	
##############################################################################
# Write out a *.vhd file for derived graph.
##############################################################################
def writeVHDLAMSfile(varsL,datL,binsL,ratesL,divisionsL,tParam,g,vhdFile):
	outputF = open(vhdFile, 'w')
	outputF.write("library IEEE;\n")
	outputF.write("use IEEE.std_logic_1164.all;\n")
	outputF.write("use work.handshake.all;\n")
	outputF.write("use work.nondeterminism.all;\n\n")
	outputF.write("entity amsDesign is\n")
	outputF.write("end amsDesign;\n\n")
	
	baseFileL = os.path.splitext(vhdFile)
	outputF.write("architecture "+baseFileL[0]+" of amsDesign is\n")
	for i in range(1,len(varsL)):
		outputF.write("\tquantity "+varsL[i].name+":real;\n")
	outputF.write("\nbegin\n")
	
	for i in range(1,len(varsL)):
		outputF.write("\tbreak "+varsL[i].name+" => "+str(int(g.initValL[i][0]))+".0;\n")
	outputF.write("\n")
	
	for i in range(1,len(varsL)):
		if varsL[i].dmvc:
			outputF.write("\t"+varsL[i].name+"'dot == 0.0;\n")
	outputF.write("\n")
	
	placeL = g.placeD.values()
	placeL.sort()
	ratePlaceL = []
	dmvcPlaceL = []
	for p in placeL:
		if p.isRateP():
			ratePlaceL.append(p)
		elif p.isDmvcP():
			dmvcPlaceL.append(p)
	dmvcPlaceL.sort(placeVarCmp)
	dmvcPlaceVarL = []
	for i in range(len(varsL)):
		dmvcPlaceVarL.append([])
	for p in dmvcPlaceL:
		dmvcPlaceVarL[p.dmvcVar].append(p)

	ifL = []
	for i in varsL:
		ifL.append([])
	for p in ratePlaceL:
		tL = p.incomingL
		for t in tL:
			if t.outgoingP and t.incomingP and t.incomingP.isRateP() and t.outgoingP.isRateP():
				diffL = t.outgoingP.diff(t.incomingP,varsL)
				ifStr = ""
				for i in diffL:
				  #Note: the i-1 floating around here is to compensate for the fact that binEncoding doesn't include time (varsL[0]), so to index into binEncoding using a varsL index you must subtract 1
					if (t.incomingP.binEncodingL[i-1] < t.outgoingP.binEncodingL[i-1]):
						above = True
						val = divisionsL[i][t.incomingP.binEncodingL[i-1]]
					else:
						above = False
						val = divisionsL[i][t.outgoingP.binEncodingL[i-1]]
					if above:
						ifStr = varsL[i].name+"'above("+str(val)+")"
						#outputF.write("\tif "+varsL[i].name+"'above("+str(val)+") use\n")
					else:
						ifStr = "not "+varsL[i].name+"'above("+str(val)+")"
						#outputF.write("\tif not "+varsL[i].name+"'above("+str(val)+") use\n")
					for j in range(1,len(varsL)):
						rateStr = ""
						if not varsL[j].dmvc and not varsL[j].input:
							rateStr = varsL[j].name + "'dot == span(" + str(p.minRateInt(j))+".0,"+str(p.maxRateInt(j))+".0)"
							ifL[j].append((ifStr,rateStr))
							#outputF.write("\t\t" + varsL[j].name + "'dot == span(" + str(p.minRateInt(j))+".0,"+str(p.maxRateInt(j))+".0);\n")
					#outputF.write("\tend use;\n")
		#outputF.write("\n");
	for i in range(1,len(varsL)):
		for j in range(len(ifL[i])):
			if j == 0:
				outputF.write("\tif "+ifL[i][j][0]+" use\n")
			else:
				outputF.write("\telsif "+ifL[i][j][0]+" use\n")
			outputF.write("\t\t"+ifL[i][j][1]+";\n")
		if ifL[i]:
			outputF.write("\tend use;\n\n")
	
	outputF.write("\tprocess\n")
	outputF.write("\tbegin\n")
	for i in range(len(dmvcPlaceVarL)):
		for p in dmvcPlaceVarL[i]:
			outputF.write("\t\twait for delay("+str(p.maxTimeInt())+","+str(p.minTimeInt())+");\n")
			outputF.write("\t\tbreak "+varsL[p.dmvcVar].name+" => "+str(p.dmvcVal)+";\n")
	outputF.write("\tend process;\n\n")
	outputF.write("end "+baseFileL[0]+";\n")
	outputF.close()

##############################################################################
# Print statistics about the graph representing the model.  level is the
# verbosity level for the statistics.
##############################################################################
def printStatistics(level,g,divisionsL,varsL):
	print "Places:"+str(len(g.placeD))
	print "Transitions:"+str(len(g.transitionD))
	print "Rates:"
	placeL = g.placeD.values()
	ratePlaceL = []
	for p in placeL:
		if p.isRateP():
			ratePlaceL.append(p)
	ratePlaceL.sort(placeBinCmp)
	#The code below is an indexing nightmare due to the fact that binEncodingL is only as long as the number of continuous variable while all of the other arrays are as long as time+continuous variables.
	for p in ratePlaceL:
		#print "p"+str(p.placeNum)+"binEncodingL:"+str(p.binEncodingL)
		printStr = ""
		binLen = len(p.binEncodingL)
		if p.type == ASGN:
			binLen += -1
		for i in range(binLen):
			if i != 0:
				printStr += ", "
			if p.binEncodingL[i] == 0:
				printStr += varsL[i+1].name+"<"+str(divisionsL[i+1][int(p.binEncodingL[i])])
			elif p.binEncodingL[i] == len(divisionsL[i+1]):
				printStr += varsL[i+1].name+">="+str(divisionsL[i+1][int(p.binEncodingL[i])-1])
			else:
				printStr += varsL[i+1].name+">="+str(divisionsL[i+1][int(p.binEncodingL[i])-1])+", "+varsL[i+1].name+"<"+str(divisionsL[i+1][int(p.binEncodingL[i])])
		printStr += " "
		for i in range(len(p.binEncodingL)):
			if not varsL[i+1].dmvc:
				if intRates:
					printStr += varsL[i+1].name+":["+str(p.minRateInt(i+1))+","+str(p.maxRateInt(i+1))+"] "
				else:
					printStr += varsL[i+1].name+":["+str(p.minRate(i+1))+","+str(p.maxRate(i+1))+"] "
		print printStr
	if level > 1:
		for p in placeL:
			print "p"+str(p.placeNum)+" ["+p.keyStr()+"]"

##############################################################################
##############################################################################
def main():
	usage = "usage: %prog [options] datFile1 ... datFileN"
	parser = OptionParser(usage=usage)
	parser.set_defaults(binsFile="",propFile="",trace=False)
	parser.add_option("-b", "--bins", action="store", dest="binsFile", help="The name of the file containing the thresholds to be used.")
	parser.add_option("-p", "--prop", action="store", dest="propFile", help="The name of the file containing the property to be verified.")
	parser.add_option("-l", "--lhpn", action="store", dest="gFile", help="The name of the .g (LHPN) file to be created.")

	(options, args) = parser.parse_args()

	#if not len(args) > 0:
	#	cStr = cText.cSetFg(cText.RED)
	#	cStr += "ERROR:"
	#	cStr += cText.cSetAttr(cText.NONE)
	#	print cStr + " At least one data file is required."
	#	parser.print_help()
		#sys.exit()
	#print "args:"+str(args)
	datFileL = []
	i = 1
	while os.path.isfile("run-" + str(i) + ".tsd"):
		#print i
		datFileL.append("run-" + str(i) + ".tsd")
		i += 1
	#datFileL = [i-2]
	print "length " + str(i) + str(len(datFileL))
	#for i in range(len(datFileL)):
	#	datFileL[i] = tempDatL[i]
	
	#The thresholds, variables, and prop files are the same for all
	#dat files, so process them before processing the individual dat
	#files
	baseFileL = os.path.splitext("run-1.tsd")
	if options.binsFile == "":
		options.binsFile = baseFileL[0] + ".bins"
	#The variable names and ordering must be consistent across files,
	#so it is extracted from the first dat file and checked against
	#every other dat file
	varsL = extractVars("run-1.tsd")
	divisionsL, tParam = parseBinsFile(options.binsFile,varsL,options.trace)
	gFile = options.gFile
	psFile = baseFileL[0] + ".ps"
	vaFile = baseFileL[0] + ".va"
	vhdFile = baseFileL[0] + ".vhd"

	failProp = ""
	if options.propFile:
		failProp = parsePropFile(options.propFile)

	g = Graph(varsL,failProp)
	cvg = Coverage(datFileL)
	for i in range(len(datFileL)):
		#print "Working on: "+datFileL[i]
		cvg.index = i
		baseFileL = os.path.splitext(datFileL[i])
		sortFile = baseFileL[0] + ".sort"
		rateFile = baseFileL[0] + ".rate"

		datL,numPoints = parseDatFile(datFileL[i],varsL)
	
		binsL = genBins(varsL,datL,divisionsL)
		writeSortFile(varsL,numPoints,datL,binsL,sortFile)
		#print "print1 " + str(len(varsL)) + str(len(datFileL))
	
		ratesL = genRates(varsL,datL,binsL,rateSampling)
		writeRateFile(varsL,numPoints,datL,binsL,ratesL,rateFile)

		dmvcRunL = findDMVC(datL,varsL,tParam)
		updateGraph(g,varsL,datL,binsL,ratesL,dmvcRunL,tParam,failProp,cvg,divisionsL)
	print "print2 " + str(len(varsL))

	#Graph expansion is used for non-input DMVC places
	exG = expandGraph(g,varsL)
	print "Expanded graph:"
	print str(exG)
	#writeVerilogAfile(varsL,datL,binsL,ratesL,divisionsL,tParam,exG,vaFile)
	sbcG = rmMultipleBinChange(exG,varsL)
	#Values need to be normalized for the g file
	normG, normDivisionsL = normalizeValues(sbcG,varsL,divisionsL)
	if limitExists:
		addMetaBins(normG,varsL,divisionsL)
		print "Graph with meta-bins:\n" + str(normG)
	writeGfile(varsL,datL,binsL,ratesL,normDivisionsL,tParam,normG,gFile)
	#writePSfile(normG,varsL,psFile)
	writeVHDLAMSfile(varsL,datL,binsL,ratesL,normDivisionsL,tParam,normG,vhdFile)
	#printStatistics(9,normG,divisionsL,varsL)
	print "Coverage:\n"+str(cvg)
##############################################################################
##############################################################################

###########
# Globals #
###########
rateSampling = "inf" #How many points should exist between the sampling of different rates..."inf" samples once/threshold
pathLength = 15 #For "inf" rate sampling the number of time points that a "run" must persist for the rate to be calculated.  This is just another parameter to help with the data smoothing.
placeRates = True #When true the script calculates rates based on places.  When false it calculates rates based on transitions although there is very little infrastructure for transition based rates and it isn't well tested.
intRates = True #When true printStatistics prints the rates as integers.  When false the rates are printed as floats.
minDelayVal = 10 #delay values must be greater than minDelayVal after scaling
minRateVal = 10 #rate values must be greater than minDelayVal after scaling
minDivisionVal = 10 #division values must be greater than minDelayVal after scaling
decPercent = 0.15 #to remove perimiter effects remove the first or last time value if they are >decPercent*100% different than a non-first or non-last extreme value


#Place types
RATE  = 0
DMVC  = 1
PROP  = 2
ASGN  = 3
TRACE = 4
#Variable types
VOLTAGE = 10
CURRENT = 11

if __name__ == "__main__":
	main()
