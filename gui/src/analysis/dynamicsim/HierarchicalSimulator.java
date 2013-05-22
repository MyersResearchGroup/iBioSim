package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;


import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.CompModelPlugin;
import org.sbml.libsbml.CompSBMLDocumentPlugin;
import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ListOfSpeciesReferences;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Replacing;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLErrorLog;
import org.sbml.libsbml.SBMLReader;

import main.Gui;
import main.util.MutableBoolean;
import main.util.dataparser.DataParser;
import main.util.dataparser.TSDParser;

import odk.lang.FastMath;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

public abstract class HierarchicalSimulator {
	
	//SBML Models
	protected ModelState topmodel; // Top Level Module
	protected ModelState [] submodels; // Submodels
	protected HashMap<String, Double> replacements;
	protected HashMap<String, Double> initReplacementState;
	protected int numSubmodels;
	protected double totalPropensity;
	
	//generates random numbers based on the xorshift method
	protected XORShiftRandom randomNumberGenerator = null;
	
	protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();
	
	//file writing variables
	protected FileWriter TSDWriter = null;
	protected BufferedWriter bufferedTSDWriter = null;
	
	//boolean flags
	protected boolean cancelFlag = false;
	protected boolean constraintFailureFlag = false;
	protected boolean sbmlHasErrorsFlag = false;
	protected boolean noConstraintsFlag = true;
	protected boolean stopDueConstraint = false;
	
	protected double currentTime;
	protected String SBMLFileName;
	protected double timeLimit;
	protected double maxTimeStep;
	protected double minTimeStep;
	protected JProgressBar progress;
	protected double printInterval;
	protected int currentRun;
	protected String outputDirectory;
	protected String separator;
	
	protected boolean stoichAmpBoolean = false;
	protected double stoichAmpGridValue = 1.0;
	
	protected boolean printConcentrations = false;
	
	protected JFrame running = new JFrame();
	
	PsRandom prng = new PsRandom();
	
	/**
	 * does lots of initialization
	 * 
	 * @param SBMLFileName
	 * @param outputDirectory
	 * @param timeLimit
	 * @param maxTimeStep
	 * @param randomSeed
	 * @param progress
	 * @param printInterval
	 * @param initializationTime
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public HierarchicalSimulator(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			Long initializationTime, double stoichAmpValue, JFrame running, String[] interestingSpecies, 
			String quantityType) 
	throws IOException, XMLStreamException {
		
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.outputDirectory = outputDirectory;
		this.running = running;

		replacements = new HashMap<String,Double>();
		initReplacementState = new HashMap<String, Double>();
		
		if (quantityType != null && quantityType.equals("concentration"))
			this.printConcentrations = true;
		
		if (stoichAmpValue <= 1.0)
			stoichAmpBoolean = false;
		else {
			stoichAmpBoolean = true;
			stoichAmpGridValue = stoichAmpValue;
		}
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);
		
		SBMLErrorLog errors = document.getErrorLog();
		
		//if the sbml document has errors, tell the user and don't simulate
		if (document.getNumErrors() > 0) 
		{	
			String errorString = "";
			
			for (int i = 0; i < errors.getNumErrors(); i++) {
				errorString += errors.getError(i);
			}
			
			JOptionPane.showMessageDialog(Gui.frame, 
			"The SBML file contains " + document.getNumErrors() + " error(s):\n" + errorString,
			"SBML Error", JOptionPane.ERROR_MESSAGE);
			
			sbmlHasErrorsFlag = true;
		}
		
		
		if (File.separator.equals("\\")) 
		{
			separator = "\\\\";
		}
		else 
		{
			separator = File.separator;
		}
		

		topmodel = new ModelState(document.getModel(), true, "");
		numSubmodels = (int)setupSubmodels(document);

		getComponentPortMap(document);
		
		ibiosimFunctionDefinitions.add("uniform");
		ibiosimFunctionDefinitions.add("exponential");
		ibiosimFunctionDefinitions.add("gamma");
		ibiosimFunctionDefinitions.add("chisq");
		ibiosimFunctionDefinitions.add("lognormal");
		ibiosimFunctionDefinitions.add("laplace");
		ibiosimFunctionDefinitions.add("cauchy");
		ibiosimFunctionDefinitions.add("poisson");
		ibiosimFunctionDefinitions.add("binomial");
		ibiosimFunctionDefinitions.add("bernoulli");
		ibiosimFunctionDefinitions.add("normal");
	}
	
	protected String getPath(String path)
	{
		String temp = path.substring(0, path.length()-1);
		
		while(temp != "" && !temp.endsWith("/"))
		{
			temp = path.substring(0, temp.length()-1);
		}
		
		return temp;
	}
	
	/**
	 * cancels the current run
	 */
	protected abstract void cancel();
	
	/**
	 * clears data structures for new run
	 */
	protected abstract void clear();

	
	protected boolean getNoConstraintsFlag()
	{
		return this.noConstraintsFlag;
	}
	
	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException {
		
		String commaSpace = "";
		
		bufferedTSDWriter.write("(");
		
		commaSpace = "";
		
		//print the current time
		bufferedTSDWriter.write(printTime + ", ");
		
		LinkedHashSet<String> speciesIDSet = topmodel.speciesIDSet;
		//loop through the speciesIDs and print their current value to the file
		for (String speciesID : speciesIDSet)
		{		
			if(replacements.containsKey(speciesID))
			{
				bufferedTSDWriter.write(commaSpace + replacements.get(speciesID));
				commaSpace = ", ";
			}
			else
			{
				bufferedTSDWriter.write(commaSpace + topmodel.variableToValueMap.get(speciesID));
				commaSpace = ", ";
			}
		}
		for (ModelState models : submodels)
		{
			speciesIDSet = models.speciesIDSet;
			//loop through the speciesIDs and print their current value to the file
			for (String speciesID : speciesIDSet)
			{		
				if(replacements.containsKey(speciesID))
				{
					bufferedTSDWriter.write(commaSpace + replacements.get(speciesID));
					commaSpace = ", ";
				}
				else
				{
					bufferedTSDWriter.write(commaSpace + models.variableToValueMap.get(speciesID));
					commaSpace = ", ";
				}
			}
		}
		
		bufferedTSDWriter.write(")");
		bufferedTSDWriter.flush();
	}
	
	
	
	
	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	protected abstract void setupForNewRun(int newRun);
	
	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun) {
		
		this.currentRun = currentRun;
		
		randomNumberGenerator = new XORShiftRandom(randomSeed);
		
		try {
			
			String extension = ".tsd";
			
			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(TSDWriter);
			bufferedTSDWriter.write('(');
			
			if (currentRun > 1) {
			
				bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected long setupSubmodels(SBMLDocument document)
	{
		String path = getPath(outputDirectory);
		CompModelPlugin sbmlCompModel = (CompModelPlugin)document.getModel().getPlugin("comp");
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin)document.getPlugin("comp");
		submodels = new ModelState[(int)sbmlCompModel.getNumSubmodels()];
		long size = sbmlCompModel.getNumSubmodels();
		
		for (int i = 0; i < size; i++) {
			Submodel submodel = sbmlCompModel.getSubmodel(i);
			BioModel subBioModel = new BioModel(path);		
			String extModelFile = sbmlComp.getExternalModelDefinition(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subBioModel.load(path + extModelFile);
			
			submodels[i] = new ModelState(subBioModel.getSBMLDocument().getModel(), false, submodel.getId());
			}
		
		return size;
	}
	
	protected void getComponentPortMap(SBMLDocument sbml) {
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
			String s = species.getId();
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) 
			{
					replacements.put(s, species.getInitialAmount());	
					initReplacementState.put(s, species.getInitialAmount());
			}
			if(sbmlSBase.isSetReplacedBy())
			{
				Replacing replacement = sbmlSBase.getReplacedBy();
				String submodel = replacement.getSubmodelRef();
				for(ModelState model : submodels)
					if(submodel.equals(model.ID))
					{
						replacements.put(s, model.model.getModel().getSpecies(i).getInitialAmount());
						initReplacementState.put(s, model.model.getModel().getSpecies(i).getInitialAmount());
						break;
					}
						
			}
			}
		}
		

	/**
	 * calculates an expression using a recursive algorithm
	 * 
	 * @param node the AST with the formula
	 * @return the evaluated expression
	 */
	protected double evaluateExpressionRecursive(ModelState modelstate, ASTNode node) {
		if (node.isBoolean()) {
			
			switch (node.getType()) {
			
			case libsbmlConstants.AST_CONSTANT_TRUE:
				return 1.0;
				
			case libsbmlConstants.AST_CONSTANT_FALSE:
				return 0.0;
				
			case libsbmlConstants.AST_LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getLeftChild()))));
				
			case libsbmlConstants.AST_LOGICAL_AND: {
				
				boolean andResult = true;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));
				
				return getDoubleFromBoolean(andResult);
			}
				
			case libsbmlConstants.AST_LOGICAL_OR: {
				
				boolean orResult = false;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));
				
				return getDoubleFromBoolean(orResult);				
			}
				
			case libsbmlConstants.AST_LOGICAL_XOR: {
				
				boolean xorResult = getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				
				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));
				
				return getDoubleFromBoolean(xorResult);
			}
			
			case libsbmlConstants.AST_RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) == evaluateExpressionRecursive(modelstate, node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) != evaluateExpressionRecursive(modelstate, node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_GEQ:
			{
				//System.out.println("Node: " + libsbml.formulaToString(node.getRightChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));
				//System.out.println("Node: " + evaluateExpressionRecursive(modelstate, node.getLeftChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));
				
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) >= evaluateExpressionRecursive(modelstate, node.getRightChild()));
			}
			case libsbmlConstants.AST_RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) <= evaluateExpressionRecursive(modelstate, node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) > evaluateExpressionRecursive(modelstate, node.getRightChild()));
				
			case libsbmlConstants.AST_RELATIONAL_LT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) < evaluateExpressionRecursive(modelstate, node.getRightChild()));			
			}			
		}
		
		//if it's a mathematical constant
		else if (node.isConstant()) {
			
			switch (node.getType()) {
			
			case libsbmlConstants.AST_CONSTANT_E:
				return Math.E;
				
			case libsbmlConstants.AST_CONSTANT_PI:
				return Math.PI;
			}
		}
		else if (node.isInteger())
			return node.getInteger();
		
		//if it's a number
		else if (node.isReal())
			return node.getReal();
		
		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {
			
			String name = node.getName().replace("_negative_","-");
				
			if (node.getType()==libsbmlConstants.AST_NAME_TIME) {
				
				return currentTime;
			}
			//if it's a reaction id return the propensity
			else if (modelstate.reactionToPropensityMap.keySet().contains(node.getName())) {
				return modelstate.reactionToPropensityMap.get(node.getName());
			}
			else {
				
				double value;
				
				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(name) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(name) == false) {
					value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
				}
				else	
				{
					if(replacements.containsKey(name))
						value = replacements.get(name);
					else	
						value = modelstate.variableToValueMap.get(name);
				}
				return value;
			}
		}
		
		//operators/functions with two children
		else {
			
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			
			switch (node.getType()) {
			
			case libsbmlConstants.AST_PLUS: {
				
				double sum = 0.0;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter));					
					
				return sum;
			}
				
			case libsbmlConstants.AST_MINUS: {
				
				double sum = evaluateExpressionRecursive(modelstate, leftChild);
				
				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter));					
					
				return sum;
			}
				
			case libsbmlConstants.AST_TIMES: {
				
				double product = 1.0;
				
				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter));
				
				return product;
			}
				
			case libsbmlConstants.AST_DIVIDE:
				return (evaluateExpressionRecursive(modelstate, leftChild) / evaluateExpressionRecursive(modelstate, rightChild));
				
			case libsbmlConstants.AST_FUNCTION_POWER:
				return (FastMath.pow(evaluateExpressionRecursive(modelstate, leftChild), evaluateExpressionRecursive(modelstate, rightChild)));
				
			case libsbmlConstants.AST_FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();
								
				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {
					
					double leftChildValue = evaluateExpressionRecursive(modelstate, node.getLeftChild());
					double rightChildValue = evaluateExpressionRecursive(modelstate, node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);
					
					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential")) {
					
					return prng.nextExponential(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("gamma")) {
					
					return prng.nextGamma(1, evaluateExpressionRecursive(modelstate, node.getLeftChild()), 
							evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("chisq")) {
					
					return prng.nextChiSquare((int) evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("lognormal")) {
					
					return prng.nextLogNormal(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 
							evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("laplace")) {
					
					//function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy")) {
					
					return prng.nextLorentzian(0, evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("poisson")) {
					
					return prng.nextPoissonian(evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("binomial")) {
					
					return prng.nextBinomial(evaluateExpressionRecursive(modelstate, node.getLeftChild()),
							(int) evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("bernoulli")) {
					
					return prng.nextBinomial(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("normal")) {
					
					return prng.nextGaussian(evaluateExpressionRecursive(modelstate, node.getLeftChild()),
							evaluateExpressionRecursive(modelstate, node.getRightChild()));	
				}

				
				break;
			}
			
			case libsbmlConstants.AST_FUNCTION_ABS:
				return FastMath.abs(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpressionRecursive(modelstate, node.getChild(0)));				
			
			case libsbmlConstants.AST_FUNCTION_COS:
				return FastMath.cos(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_COSH:
				return FastMath.cosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_EXP:
				return FastMath.exp(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_LN:
				return FastMath.log(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_LOG:
				return FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SIN:
				return FastMath.sin(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SINH:
				return FastMath.sinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_TAN:
				return FastMath.tan(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_TANH:		
				return FastMath.tanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				
			case libsbmlConstants.AST_FUNCTION_PIECEWISE: {
				
				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getNumChildren(); childIter += 3) {
					
					if ((childIter + 1) < node.getNumChildren() && 
							getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter + 1)))) {
						return evaluateExpressionRecursive(modelstate, node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getNumChildren()) {
						return evaluateExpressionRecursive(modelstate, node.getChild(childIter + 2));
					}
				}
				
				return 0;
			}
			
			case libsbmlConstants.AST_FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(modelstate, node.getRightChild()), 
						1 / evaluateExpressionRecursive(modelstate, node.getLeftChild()));
			
			case libsbmlConstants.AST_FUNCTION_SEC:
				return Fmath.sec(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_SECH:
				return Fmath.sech(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				
			case libsbmlConstants.AST_FUNCTION_COT:
				return Fmath.cot(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_COTH:
				return Fmath.coth(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CSC:
				return Fmath.csc(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_CSCH:
				return Fmath.csch(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;
				
			case libsbmlConstants.AST_FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(modelstate, node.getChild(0)));
			
			case libsbmlConstants.AST_FUNCTION_ARCSECH:
				return Fmath.asech(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				
			} //end switch
			
		}
		return 0.0;
	}


	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList) {
		
		ASTNode child;
		long size = node.getNumChildren();
		
		for (int i = 0; i < size; i++) {
			child = node.getChild(i);
			if (child.getNumChildren() == 0)
				nodeChildrenList.add(child);
			else {
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}			
	}
	
	/**
	 * returns a set of all the reactions that the recently performed reaction affects
	 * "affect" means that the species updates will change the affected reaction's propensity
	 * 
	 * @param selectedReactionID the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the propensity of
	 */
	protected HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, boolean noAssignmentRulesFlag) {
		
		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);
		
		//loop through the reaction's reactants and products
		for (StringDoublePair speciesAndStoichiometry : modelstate.reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
			
			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.speciesToAffectedReactionSetMap.get(speciesID));
		
		}
		
		return affectedReactionSet;
	}
	
	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the double to be translated to a boolean
	 * @return the translated boolean value
	 */
	protected boolean getBooleanFromDouble(double value) {
		
		if (value == 0.0) 
			return false;
		else 
			return true;
	}
	
	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the boolean to be translated to a double
	 * @return the translated double value
	 */
	protected double getDoubleFromBoolean(boolean value) {
		
		if (value == true)
			return 1.0;
		else 
			return 0.0;
	}

	
	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ModelState modelstate, ASTNode formula) {
		
	
		
		if (formula.isFunction() == false ||
				(formula.getLeftChild() == null && formula.getRightChild() == null)) {
			
			for (int i = 0; i < formula.getNumChildren(); ++i)
				formula.replaceChild(i, inlineFormula(modelstate, formula.getChild(i)));//.clone()));
		}
		
		if (formula.isFunction() && modelstate.model.getFunctionDefinition(formula.getName()) != null) {
			
			if (ibiosimFunctionDefinitions.contains(formula.getName()))
				return formula;
			
			ASTNode inlinedFormula = modelstate.model.getFunctionDefinition(formula.getName()).getBody().deepCopy();
			ASTNode oldFormula = formula.deepCopy();
			
			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			this.getAllASTNodeChildren(inlinedFormula, inlinedChildren);
			
			if (inlinedChildren.size() == 0)
				inlinedChildren.add(inlinedFormula);
			
			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();
			
			for (int i = 0; i < modelstate.model.getFunctionDefinition(formula.getName()).getNumArguments(); ++i) {
				inlinedChildToOldIndexMap.put(modelstate.model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}
			
			for (int i = 0; i < inlinedChildren.size(); ++i) {
				
				ASTNode child = inlinedChildren.get(i);
				
				if ((child.getLeftChild() == null && child.getRightChild() == null) && child.isName()) {
					
					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula,libsbml.formulaToString(child), oldFormula.getChild(index));
					
					if (inlinedFormula.getNumChildren() == 0)
						inlinedFormula = oldFormula.getChild(index);
				}
			}
			
			return inlinedFormula;
		}
		else {
			return formula;
		}
	}
	
	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID the reaction to perform
	 */
	protected void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {
		
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();
		
		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : modelstate.reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) 
		{
			
			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;
			
			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			boolean cond1 = modelstate.speciesToIsBoundaryConditionMap.get(speciesID);
			boolean cond2 = modelstate.variableToIsConstantMap.get(speciesID);
			if (!cond1 && !cond2) {
				if(replacements.containsKey(speciesID))
				{
					double val = replacements.get(speciesID) + stoichiometry;
					if(val >= 0)
						replacements.put(speciesID, val);
				}
				else
				{
					modelstate.variableToValueMap.adjustValue(speciesID, stoichiometry);
				}
			}
			
			if (!noConstraintsFlag && modelstate.variableToIsInConstraintMap.get(speciesID))
				affectedConstraintSet.addAll(modelstate.variableToAffectedConstraintSetMap.get(speciesID));
		}
		
	if (affectedConstraintSet.size() > 0) 
	{
		stopDueConstraint = testConstraints(modelstate, affectedConstraintSet);
	}
	
	}
	
	/**
	 * this evaluates a set of constraints that have been affected by an event or reaction firing
	 * and returns the OR'd boolean result
	 * 
	 * @param affectedConstraintSet the set of constraints affected
	 * @return the boolean result of the constraints' evaluation
	 */
	protected boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet) {
		
		//check all of the affected constraints
		//if one evaluates to true, then the simulation halts
		for (ASTNode constraint : affectedConstraintSet) {
			//System.out.println("Node: " + libsbml.formulaToString(constraint));
			
			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, constraint)))
				return false;
		}
		
		return true;
	}
	
	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate) {
		
		//loop through all constraints to find out which variables affect which constraints
		//this is stored in a hashmap, as is whether the variable is in a constraint
		
		long size = modelstate.model.getNumConstraints();
		long numNodes;
		
		if (size > 0)
			modelstate.noConstraintsFlag = false;

		for (long i = 0; i < size; i++)
		{
			
			Constraint constraint = modelstate.model.getConstraint(i);
			ASTNode formula = constraint.getMath();
	
			constraint.setMath(inlineFormula(modelstate, formula));
			
			//System.out.println("Node" + libsbml.formulaToString(formula));
			
			numNodes = constraint.getMath().getNumChildren();
			
			for (long j = 0; j < numNodes; j++)
			{
					
				ASTNode constraintNode = constraint.getMath().getChild(j);
				if (constraintNode.isName()) {
					
					String nodeName = constraintNode.getName();					
					modelstate.variableToAffectedConstraintSetMap.put(nodeName, new HashSet<ASTNode>());
					modelstate.variableToAffectedConstraintSetMap.get(nodeName).add(constraint.getMath());
					modelstate.variableToIsInConstraintMap.put(nodeName, true);
				}
			}
		}
	}
	
	
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(ModelState modelstate, Species species, String speciesID) {
		if (modelstate.speciesIDSet.contains(speciesID))
			return;
		
		if (modelstate.model.getNumConstraints() > 0)
			modelstate.variableToIsInConstraintMap.put(speciesID, false);
		
		if (species.isSetInitialAmount())
		{
			if(replacements.containsKey(speciesID))
				modelstate.variableToValueMap.put(speciesID, replacements.get(speciesID));
			else
				modelstate.variableToValueMap.put(speciesID, species.getInitialAmount());
		}
		modelstate.speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
		modelstate.speciesToIsBoundaryConditionMap.put(speciesID, species.getBoundaryCondition());
		modelstate.variableToIsConstantMap.put(speciesID, species.getConstant());
		modelstate.speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.getHasOnlySubstanceUnits());
		modelstate.speciesIDSet.add(speciesID);
	}
	
	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies(ModelState modelstate) throws IOException {
		
		//add values to hashmap for easy access to species amounts
		Species species;
		long size = modelstate.model.getListOfSpecies().size();
		for (int i = 0; i < size; i++) 
		{
			species = modelstate.model.getSpecies(i);
			setupSingleSpecies(modelstate, species, species.getId());
		}
	}

	
	
	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private void setupLocalParameters(ModelState modelstate, KineticLaw kineticLaw, Reaction reaction) {
		
		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_","-");
		
		for (int i = 0; i < kineticLaw.getNumParameters(); i++) {

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);
			
			String parameterID = "";
			
			//the parameters don't get reset after each run, so don't re-do this prepending
			//if (localParameter.getId().contains(reactionID + "_") == false)					
				//parameterID = reactionID + "_" + localParameter.getId();
			//else 
				parameterID = localParameter.getId();
							
			modelstate.variableToValueMap.put(parameterID, localParameter.getValue());
						
			//alter the local parameter ID so that it goes to the local and not global value
			if (localParameter.getId() != parameterID) {
				localParameter.setId(parameterID);
				localParameter.setMetaId(parameterID);
			}
			
		}
	}
	
	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private void setupSingleParameter(ModelState modelstate, Parameter parameter) {
		
		String parameterID = parameter.getId();
		modelstate.variableToValueMap.put(parameterID, parameter.getValue());
		modelstate.variableToIsConstantMap.put(parameterID, parameter.getConstant());
		
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate) {
		
		//add local parameters
		Reaction reaction;
		Parameter parameter;
		long size;
		
		
		size = modelstate.model.getListOfReactions().size();
		for (int i = 0; i < size; i++) 
		{
			reaction = modelstate.model.getReaction(i);
			KineticLaw kineticLaw = reaction.getKineticLaw();
			setupLocalParameters(modelstate, kineticLaw, reaction);
		}
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		
		size = modelstate.model.getListOfParameters().size();
		for (int i = 0; i < size; i++) 
		{
			parameter = modelstate.model.getParameter(i);
			setupSingleParameter(modelstate, parameter);
		}
		
	}
	
	
	/**
	 * calculates the initial propensity of a single reaction
	 * also does some initialization stuff
	 * 
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	private void setupSingleReaction(ModelState modelstate, String reactionID, ASTNode reactionFormula, boolean reversible, 
			ListOfSpeciesReferences reactantsList, ListOfSpeciesReferences productsList, 
			ListOfSpeciesReferences modifiersList) {
	reactionID = reactionID.replace("_negative_","-");
		
		long size;
		boolean notEnoughMoleculesFlag = false;
		modelstate.reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
		modelstate.reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
		

		
		size = reactantsList.size();
		for (int i = 0; i < size; i++)
		{
					
			SpeciesReference reactant = (SpeciesReference)reactantsList.get(i);
			
			String reactantID = reactant.getSpecies().replace("_negative_","-");
				double reactantStoichiometry;
				
				//if there was an initial assignment for the speciesref id
				if(replacements.containsKey(reactant.getId()))
					reactantStoichiometry = replacements.get(reactant.getId());
				else if (modelstate.variableToValueMap.containsKey(reactant.getId()))
					reactantStoichiometry = modelstate.variableToValueMap.get(reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();
				
				modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, -reactantStoichiometry));
				modelstate.reactionToReactantStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, reactantStoichiometry));
					

				//as a reactant, this species affects the reaction's propensity
				modelstate.speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);
				
				//make sure there are enough molecules for this species
				if (modelstate.variableToValueMap.get(reactantID) < reactantStoichiometry)
					notEnoughMoleculesFlag = true;
			}
			
			size = productsList.size();
			for (int i = 0; i < size; i ++) {
				SpeciesReference product = (SpeciesReference)productsList.get(i); 
				
				String productID = product.getSpecies().replace("_negative_","-");
				double productStoichiometry;
				
				//if there was an initial assignment for the speciesref id
				if(replacements.containsKey(product.getId()))
					productStoichiometry = replacements.get(product.getId());
				else if (modelstate.variableToValueMap.containsKey(product.getId()))
					productStoichiometry = modelstate.variableToValueMap.get(product.getId());
				else
					productStoichiometry = product.getStoichiometry();
				
				modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(productID, productStoichiometry));
			}
			
			modelstate.reactionToFormulaMap.put(reactionID, inlineFormula(modelstate, reactionFormula));
			
			double propensity;
			
	
			if (notEnoughMoleculesFlag == true)
				propensity = 0.0;
			else {//calculate propensity
				propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula));
				if(propensity < 0.0)
					propensity = 0.0;
				
				if (propensity < modelstate.minPropensity && propensity > 0) 
					modelstate.minPropensity = propensity;
				if (propensity > modelstate.maxPropensity)
					modelstate.maxPropensity = propensity;
				
				modelstate.propensity += propensity;
			
				//this.totalPropensity += propensity;
			}
			
			modelstate.reactionToPropensityMap.put(reactionID, propensity);
		}	
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	protected void setupReactions(ModelState modelstate) {
		
		//loop through all reactions and calculate their propensities
		Reaction reaction;
		
		for (int i = 0;  i < modelstate.numReactions; i++) {
			reaction = modelstate.model.getReaction(i);
			String reactionID = reaction.getId();
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();
						
			setupSingleReaction(modelstate, reactionID, reactionFormula, reaction.getReversible(), 
					reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}
	
	public void replaceArgument(ASTNode formula,String bvar, ASTNode arg) {
		int n = 0;
		for (int i = 0; i < formula.getNumChildren(); i++) {
			ASTNode child = formula.getChild(i);
			if (child.isName() && child.getName().equals(bvar)) {
				formula.replaceChild(n, arg.deepCopy());
			} else if (child.getNumChildren() > 0) {
				replaceArgument(child, bvar, arg);
			}
			n++;
		}
	}
	
	/**
	 * abstract simulate method
	 * each simulator needs a simulate method
	 */
	protected abstract void simulate();
	
	


	protected double getTotalPropensity()
	{
		double totalPropensity = 0;
		totalPropensity += topmodel.propensity;
		
		for(ModelState model : submodels)
		{
			totalPropensity += model.propensity;
		}
		
		return totalPropensity;
	}
	
	//STRING DOUBLE PAIR INNER CLASS	
	/**
	 * class to combine a string and a double
	 */
	protected class StringDoublePair {
		
		public String string;
		public double doub;
		
		StringDoublePair(String s, double d) {
			
			string = s;
			doub = d;
		}
	}
	
	//STRING STRING PAIR INNER CLASS	
	/**
	 * class to combine a string and a string
	 */
	protected class StringStringPair {
		
		public String string1;
		public String string2;
		
		StringStringPair(String s1, String s2) {
			
			string1 = s1;
			string2 = s2;
		}
		
	}
	
	
	protected class ModelState
	{
		protected Model model;
		protected long numSpecies;
		protected long numParameters;
		protected long numReactions;
		protected int numInitialAssignments;
		protected int numRateRules;
		protected String ID;
		
		//generates random numbers based on the xorshift method
		protected XORShiftRandom randomNumberGenerator = null;
		
		//allows for access to a propensity from a reaction ID
		protected TObjectDoubleHashMap<String> reactionToPropensityMap = null;
		
		//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
		//note that species and stoichiometries need to be thought of as unique for each reaction
		protected HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
		
		//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
		protected HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
		
		//allows for access to a kinetic formula tree from a reaction
		protected HashMap<String, ASTNode> reactionToFormulaMap = null;
		
		//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
		protected HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
		
		//allows for access to species booleans from a species ID
		protected HashMap<String, Boolean> speciesToIsBoundaryConditionMap = null;
		protected HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap = null;
		protected HashMap<String, String> speciesToCompartmentNameMap = null;
		
		//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
		protected LinkedHashSet<String> speciesIDSet = null;
		
		//allows for access to species and parameter values from a variable ID
		protected TObjectDoubleHashMap<String> variableToValueMap = null;
		
		
			
		protected HashMap<String, Boolean> variableToIsConstantMap = null;
		
		protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();
		
		//propensity variables
		protected double propensity = 0.0;
		protected double minPropensity = Double.MAX_VALUE / 10.0;
		protected double maxPropensity = Double.MIN_VALUE / 10.0;
		
		//file writing variables
		protected FileWriter TSDWriter = null;
		protected BufferedWriter bufferedTSDWriter = null;
		
		protected boolean printConcentrations = false;
		protected boolean isCopy;
		protected boolean noConstraintsFlag = true;
		
		protected JFrame running = new JFrame();
		
		final protected int SBML_LEVEL = 3;
		final protected int SBML_VERSION = 1;
		
		PsRandom prng = new PsRandom();
		
		
		
		//allows for access to the set of constraints that a variable affects
		protected HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap = null;
		
		protected HashMap<String, Boolean> variableToIsInConstraintMap = null;
		
		public ModelState(Model bioModel, boolean isCopy, String submodelID)
		{
			this.model = bioModel;
			this.numSpecies = this.model.getNumSpecies();
			this.numParameters = this.model.getNumParameters();
			this.numReactions = this.model.getNumReactions();
			this.numInitialAssignments = (int)this.model.getNumInitialAssignments();
			this.ID = submodelID;
			//this.isCopy = isCopy;
			
			//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
			speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
			speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
			variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
			speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
			speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
			variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
			
			reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
			reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
			reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
			reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
			
			
			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int) model.getNumConstraints());		
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
		
		}
		
		protected void clear()
		{
			speciesToAffectedReactionSetMap.clear();
			speciesToIsBoundaryConditionMap.clear();
			variableToIsConstantMap.clear();
			speciesToHasOnlySubstanceUnitsMap.clear();
			speciesToCompartmentNameMap.clear();
			speciesIDSet.clear();
			variableToValueMap.clear();
			noConstraintsFlag = true;
			reactionToPropensityMap.clear();
			reactionToSpeciesAndStoichiometrySetMap.clear();
			reactionToReactantStoichiometrySetMap.clear();
			reactionToFormulaMap.clear();

			propensity = 0.0;
			minPropensity = Double.MAX_VALUE / 10.0;
			maxPropensity = Double.MIN_VALUE / 10.0;
		}
	}
}



