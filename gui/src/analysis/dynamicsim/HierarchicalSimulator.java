package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;


import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ext.comp.CompConstant;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;

import main.Gui;
import main.util.MutableBoolean;
import main.util.dataparser.DataParser;
import main.util.dataparser.TSDParser;

import odk.lang.FastMath;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.sun.org.apache.xpath.internal.operations.Variable;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

public abstract class HierarchicalSimulator {

	// Top Level Module
	protected ModelState topmodel; 
	// Submodel states
	protected ModelState [] submodels; 
	// Associates species, parameters with a value
	protected HashMap<String, Double> replacements; 
	// Keeps track which submodels are involved in a replacement
	protected HashMap<String, HashSet<String>> replacementSubModels;
	// Initial replacement state for multiple runs
	protected HashMap<String, Double> initReplacementState;
	// Associates submodel ID with index of model state
	protected HashMap<String, Integer> IDtoIndex;
	// Keeps track which model contains the replacing values
	protected HashMap<String, String> replacingModel; 
	// Number of submodel states
	protected int numSubmodels;
	// Total propensity including all model states
	protected double totalPropensity;

	//SBML Info.
	final protected int SBML_LEVEL = 3;
	final protected int SBML_VERSION = 1;

	//generates random numbers based on the xorshift method
	protected XORShiftRandom randomNumberGenerator = null;

	protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();

	//file writing variables
	protected FileWriter TSDWriter = null;
	protected BufferedWriter bufferedTSDWriter = null;

	//compares two events based on fire time and priority
	protected EventComparator eventComparator = new EventComparator();

	//boolean flags
	protected boolean cancelFlag = false;
	protected boolean constraintFailureFlag = false;
	protected boolean sbmlHasErrorsFlag = false;
	//protected boolean noConstraintsFlag = true;
	//	protected boolean noRuleFlag = true;
	protected boolean constraintFlag = true;

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
		replacementSubModels = new HashMap<String, HashSet<String>>();
		replacingModel = new HashMap<String,String>();
		IDtoIndex = new HashMap<String, Integer>();

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


		topmodel = new ModelState(document.getModel(), true, "topmodel");
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

	/**
	 * abstract simulate method
	 * each simulator needs a simulate method
	 */
	protected abstract void simulate();

	/**
	 * cancels the current run
	 */
	protected abstract void cancel();

	/**
	 * clears data structures for new run
	 */
	protected abstract void clear();

	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	protected abstract void setupForNewRun(int newRun);

	/**
	 * Get path to submodels xml files
	 */
	protected String getPath(String path)
	{
		String separator = path.substring(path.length()-1, path.length());

		if (File.separator.equals("\\")) 
		{
			separator = "\\\\";
		}

		String temp = path.substring(0, path.length()-1);

		while(temp != "" && !temp.endsWith(separator))
		{
			temp = path.substring(0, temp.length()-1);
		}

		return temp;
	}


	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException 
	{

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

		for (String noConstantParam : topmodel.nonconstantParameterIDSet)
		{
			if(replacements.containsKey(noConstantParam))
			{
				bufferedTSDWriter.write(commaSpace + replacements.get(noConstantParam));
				commaSpace = ", ";
			}
			else
			{
				bufferedTSDWriter.write(commaSpace + topmodel.variableToValueMap.get(noConstantParam));
				commaSpace = ", ";
			}
		}

		for (String compartment : topmodel.compartmentIDSet)
		{
			bufferedTSDWriter.write(commaSpace + topmodel.getVariableToValue(compartment));
			commaSpace = ", ";
		}

		for (ModelState models : submodels)
		{
			speciesIDSet = models.speciesIDSet;
			//loop through the speciesIDs and print their current value to the file
			for (String speciesID : speciesIDSet)
			{		
				/*
				if(replacements.containsKey(speciesID) && this.replacementSubModels.get(speciesID).contains(models.ID))
				{
					bufferedTSDWriter.write(commaSpace + replacements.get(speciesID));
					commaSpace = ", ";
				}
				else
				{
					bufferedTSDWriter.write(commaSpace + models.variableToValueMap.get(speciesID));
					commaSpace = ", ";
				}
				 */
				bufferedTSDWriter.write(commaSpace + models.getVariableToValue(speciesID));
				commaSpace = ", ";

			}

			for (String noConstantParam : models.nonconstantParameterIDSet)
			{
				if(replacements.containsKey(noConstantParam) && this.replacementSubModels.get(noConstantParam).contains(models.ID))
				{
					bufferedTSDWriter.write(commaSpace + replacements.get(noConstantParam));
					commaSpace = ", ";
				}
				else
				{
					bufferedTSDWriter.write(commaSpace + models.variableToValueMap.get(noConstantParam));
					commaSpace = ", ";
				}
			}

			for (String compartment : topmodel.compartmentIDSet)
			{
				bufferedTSDWriter.write(commaSpace + models.getVariableToValue(compartment));
				commaSpace = ", ";
			}
		}

		bufferedTSDWriter.write(")");
		bufferedTSDWriter.flush();
	}

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

	/**
	 * Initializes the modelstate array
	 */
	protected long setupSubmodels(SBMLDocument document)
	{
		String path = getPath(outputDirectory);
		CompModelPlugin sbmlCompModel = (CompModelPlugin)document.getModel().getExtension(CompConstant.namespaceURI);
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin)document.getExtension(CompConstant.namespaceURI);
		submodels = new ModelState[(int)sbmlCompModel.getListOfSubmodels().size()];
	
		IDtoIndex.put("topmodel", -1);
		
		/*
		for (Submodel submodel : sbmlCompModel.getListOfSubmodels()) {
			BioModel subBioModel = new BioModel(path);
			String extModelFile = sbmlComp.getExternalModelDefinition(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subBioModel.load(path + extModelFile);

			performDeletions(subBioModel, submodel);
			submodels[i] = new ModelState(subBioModel.getSBMLDocument().getModel(), false, submodel.getId());
			IDtoIndex.put(submodel.getId(), i);
		}
		 */
		int index = 0;
		for (Submodel submodel : sbmlCompModel.getListOfSubmodels()) {
			performDeletions(submodel);
			submodels[index] = new ModelState(submodel.getModel(), false, submodel.getId());
			IDtoIndex.put(submodel.getId(), index);
			index++;
		}
		
		return index;
	}

	/**
	 * Stores replacing values in a global map
	 */
	protected void getComponentPortMap(SBMLDocument sbml) 
	{
		for (int i = 0; i < topmodel.numSpecies; i++) {
			Species species = sbml.getModel().getSpecies(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getExtension(CompConstant.namespaceURI);

			String s = species.getId();
			if(sbmlSBase != null && sbmlSBase.getListOfReplacedElements() != null)
			{
				replacements.put(s, species.getInitialAmount());	
				initReplacementState.put(s, species.getInitialAmount());

				if(!replacementSubModels.containsKey(s))
					replacementSubModels.put(s, new HashSet<String>());

				replacementSubModels.get(s).add("topmodel");
				replacingModel.put(s, "topmodel");

				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					replacementSubModels.get(s).add(element.getSubmodelRef());
				}
			}


			if(sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				String submodel = replacement.getSubmodelRef();
				if(!replacementSubModels.containsKey(s))
					replacementSubModels.put(s, new HashSet<String>());

				replacementSubModels.get(s).add("topmodel");

				replacingModel.put(s, submodel);

				int index = IDtoIndex.get(submodel);
				ModelState temp = getModel(index);

				replacementSubModels.get(s).add(submodel);
				replacements.put(s, temp.model.getModel().getSpecies(i).getInitialAmount());
				initReplacementState.put(s, temp.model.getModel().getSpecies(i).getInitialAmount());


			}
		}

		for (int i = 0; i < topmodel.numParameters; i++) {
			Parameter parameter = sbml.getModel().getParameter(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)parameter.getExtension(CompConstant.namespaceURI);

			String s = parameter.getId();
			if(sbmlSBase != null && sbmlSBase.getListOfReplacedElements() != null)
			{
				replacements.put(s, parameter.getValue());	
				initReplacementState.put(s, parameter.getValue());
				if(!replacementSubModels.containsKey(s))
					replacementSubModels.put(s, new HashSet<String>());

				replacementSubModels.get(s).add("topmodel");

				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					replacementSubModels.get(s).add(element.getSubmodelRef());
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					String submodel = replacement.getSubmodelRef();
					if(!replacementSubModels.containsKey(s))
						replacementSubModels.put(s, new HashSet<String>());

					replacementSubModels.get(s).add("topmodel");

					int index = IDtoIndex.get(submodel);
					ModelState temp = getModel(index);

					replacementSubModels.get(s).add(submodel);
					replacements.put(s, temp.model.getModel().getParameter(i).getValue());
					initReplacementState.put(s, temp.model.getModel().getParameter(i).getValue());


				}
			}
		}


		for (int i = 0; i < topmodel.model.getCompartmentCount(); i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getExtension(CompConstant.namespaceURI);

			String c = compartment.getId();
			if(sbmlSBase != null && sbmlSBase.getListOfReplacedElements() != null)
			{
				replacements.put(c, compartment.getSize());	
				initReplacementState.put(c, compartment.getSize());
				if(!replacementSubModels.containsKey(c))
					replacementSubModels.put(c, new HashSet<String>());

				replacementSubModels.get(c).add("topmodel");

				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					replacementSubModels.get(c).add(element.getSubmodelRef());
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					String submodel = replacement.getSubmodelRef();
					if(!replacementSubModels.containsKey(c))
						replacementSubModels.put(c, new HashSet<String>());

					replacementSubModels.get(c).add("topmodel");

					int index = IDtoIndex.get(submodel);
					ModelState temp = getModel(index);

					replacementSubModels.get(c).add(submodel);
					replacements.put(c, temp.model.getModel().getParameter(i).getValue());
					initReplacementState.put(c, temp.model.getModel().getParameter(i).getValue());


				}
			}
		}

		for (int i = 0; i < topmodel.numReactions; i++) {
			Reaction reaction = sbml.getModel().getReaction(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)reaction.getExtension(CompConstant.namespaceURI);

			String r = reaction.getId();
			if(sbmlSBase != null && sbmlSBase.getListOfReplacedElements() != null)
			{
				if(!replacementSubModels.containsKey(r))
					replacementSubModels.put(r, new HashSet<String>());

				//replacementSubModels.get(r).add("topmodel");

				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					replacementSubModels.get(r).add(element.getSubmodelRef());
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();

					String submodel = replacement.getSubmodelRef();
					if(!replacementSubModels.containsKey(r))
						replacementSubModels.put(r, new HashSet<String>());

					replacementSubModels.get(r).add("topmodel");
					//replacementSubModels.get(r).add(submodel);
				}
			}
		}

	}




	/**
	 * Gets state from index
	 */
	private ModelState getModel(int index)
	{
		if(index == -1)
			return topmodel;
		else
			return submodels[index];
	}

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

	/**
	 * Perform deletion on comp model
	 */
	private void performDeletions(Submodel instance) {

		Model subModel = instance.getModel();
		CompModelPlugin sbmlCompModel = (CompModelPlugin)subModel.getModel().getExtension("comp");
		if (instance == null)
			return;
	/*
		for(Deletion deletion : instance.getListOfDeletions()){

			if (deletion.isSetPortRef()) {
				Port port = sbmlCompModel.getPort(deletion.getPortRef());
				if (port!=null) {
					if (port.isSetIdRef()) {
						if (subModel.getElementBySId(port.getIdRef())!=null) {
							SBase sbase =  subModel.getElementBySId(port.getIdRef());
							sbase.removeFromParentAndDelete();
						}
					} else if (port.isSetMetaIdRef()) {
						if (subModel.getElementByMetaId(port.getMetaIdRef())!=null) {
							SBase sbase =  subModel.getElementByMetaId(port.getMetaIdRef());
							sbase.removeFromParentAndDelete();
						}
					} else if (port.isSetUnitRef()) {
						if (subModel.getUnitDefinition(port.getUnitRef())!=null) {
							SBase sbase = subModel.getUnitDefinition(port.getUnitRef());
							sbase.removeFromParentAndDelete();
						}
					}
				}
			} else if (deletion.isSetIdRef()) {
				if (subModel.getElementBySId(deletion.getIdRef())!=null) {
					SBase sbase =  subModel.getElementBySId(deletion.getIdRef());
					sbase.removeFromParentAndDelete();
				}
			} else if (deletion.isSetMetaIdRef()) {
				if (subModel.getElementByMetaId(deletion.getMetaIdRef())!=null) {
					SBase sbase =  subModel.getElementByMetaId(deletion.getMetaIdRef());
					sbase.removeFromParentAndDelete();
				}
			} else if (deletion.isSetUnitRef()) {
				if (subModel.getUnitDefinition(deletion.getUnitRef())!=null) {
					SBase sbase = subModel.getUnitDefinition(deletion.getUnitRef());
					sbase.removeFromParentAndDelete();
				}
			}
		}
		*/
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

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getLeftChild()))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0)));

				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) == evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) != evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GEQ:
			{
				//System.out.println("Node: " + libsbml.formulaToString(node.getRightChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));
				//System.out.println("Node: " + evaluateExpressionRecursive(modelstate, node.getLeftChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));

				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) >= evaluateExpressionRecursive(modelstate, node.getRightChild()));
			}
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) <= evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) > evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_LT:
			{
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) < evaluateExpressionRecursive(modelstate, node.getRightChild()));			
			}

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
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

			if (node.getType()== org.sbml.jsbml.ASTNode.Type.NAME_TIME) {

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
					//value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
					value = (modelstate.getVariableToValue(name) / modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(name)));
				}
				else	
				{	
					value = modelstate.getVariableToValue(name);
				}
				return value;
			}
		}

		//operators/functions with two children
		else {

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case MINUS: {

				double sum = evaluateExpressionRecursive(modelstate, leftChild);

				for (int childIter = 1; childIter < node.getNumChildren(); ++childIter)
					sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getNumChildren(); ++childIter)
					product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter));

				return product;
			}

			case DIVIDE:
				return (evaluateExpressionRecursive(modelstate, leftChild) / evaluateExpressionRecursive(modelstate, rightChild));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateExpressionRecursive(modelstate, leftChild), evaluateExpressionRecursive(modelstate, rightChild)));

			case FUNCTION: {
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

			case FUNCTION_ABS:
				return FastMath.abs(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpressionRecursive(modelstate, node.getChild(0)));				

			case FUNCTION_COS:
				return FastMath.cos(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COSH:
				return FastMath.cosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_EXP:
				return FastMath.exp(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_LN:
				return FastMath.log(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_LOG:
				return FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_SIN:
				return FastMath.sin(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_SINH:
				return FastMath.sinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_TAN:
				return FastMath.tan(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_TANH:		
				return FastMath.tanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_PIECEWISE: {

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

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(modelstate, node.getRightChild()), 
						1 / evaluateExpressionRecursive(modelstate, node.getLeftChild()));

			case FUNCTION_SEC:
				return Fmath.sec(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_SECH:
				return Fmath.sech(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COT:
				return Fmath.cot(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COTH:
				return Fmath.coth(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CSC:
				return Fmath.csc(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CSCH:
				return Fmath.csch(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSECH:
				return Fmath.asech(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			} //end switch

		}
		return 0.0;
	}


	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID the reaction to perform
	 */
	protected void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//these are sets of things that need to be re-evaluated or tested due to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : modelstate.reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) 
		{

			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;

			//this means the stoichiometry isn't constant, so look to the variableToValue map
			if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(selectedReactionID)) {

				for (StringStringPair doubleID : modelstate.reactionToNonconstantStoichiometriesSetMap.get(selectedReactionID)) {

					//string1 is the species ID; string2 is the speciesReference ID
					if (doubleID.string1.equals(speciesID)) {

						stoichiometry = modelstate.variableToValueMap.get(doubleID.string2);

						//this is to get the plus/minus correct, as the variableToValueMap has
						//a stoichiometry without the reactant/product plus/minus data
						stoichiometry *= (int)(speciesAndStoichiometry.doub/Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			boolean cond1 = modelstate.speciesToIsBoundaryConditionMap.get(speciesID);
			boolean cond2 = modelstate.variableToIsConstantMap.get(speciesID);
			if (!cond1 && !cond2) {
				if(replacements.containsKey(speciesID) && this.replacementSubModels.get(speciesID).contains(modelstate.ID))
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

			//if this variable that was just updated is part of an assignment rule (RHS)
			//then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(speciesID) == true)
				affectedAssignmentRuleSet.addAll(modelstate.variableToAffectedAssignmentRuleSetMap.get(speciesID));

			if (noConstraintsFlag == false && modelstate.variableToIsInConstraintMap.get(speciesID) == true)
				affectedConstraintSet.addAll(modelstate.variableToAffectedConstraintSetMap.get(speciesID));
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(modelstate, affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0) 
			//if (testConstraints(modelstate, affectedConstraintSet) == false)
			//	constraintFailureFlag = true;
			//else
			constraintFlag = testConstraints(modelstate, affectedConstraintSet);

	}

	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList) {

		ASTNode child;
		long size = node.getChildCount();

		for (int i = 0; i < size; i++) {
			child = node.getChild(i);
			if (child.getChildCount() == 0)
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

			//if the species is involved in an assignment rule then it its changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(speciesID)) {

				//this assignment rule is going to be evaluated, so the rule's variable's value will change
				for (AssignmentRule assignmentRule : modelstate.variableToAffectedAssignmentRuleSetMap.get(speciesID)) {
					if (modelstate.speciesToAffectedReactionSetMap.get(assignmentRule.getVariable())!=null) {
						affectedReactionSet.addAll(modelstate.speciesToAffectedReactionSetMap
								.get(assignmentRule.getVariable()));
					}
				}
			}
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

			ASTNode inlinedFormula = modelstate.model.getFunctionDefinition(formula.getName()).getBody().clone();
			ASTNode oldFormula = formula.clone();

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
					replaceArgument(inlinedFormula,child.toFormula(), oldFormula.getChild(index));
					
					if (inlinedFormula.getChildCount() == 0)
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
	 * This method is used to update the values involving a species in the global
	 * map. Without this method, species with rules involving replacing species
	 * would not be updated.
	 */
	protected void updateRules()
	{
		ModelState model;
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		for(String species : replacementSubModels.keySet())
			for(String submodel : replacementSubModels.get(species))
			{
				model = getModel(IDtoIndex.get(submodel));


				if (model.noRuleFlag == false && model.variableToIsInAssignmentRuleMap.get(species) == true)
					affectedAssignmentRuleSet.addAll(model.variableToAffectedAssignmentRuleSetMap.get(species));
				if (affectedAssignmentRuleSet.size() > 0)
					performAssignmentRules(model, affectedAssignmentRuleSet);
				if (model.noConstraintsFlag == false && model.variableToIsInConstraintMap.get(species) == true)
					affectedConstraintSet.addAll(model.variableToAffectedConstraintSetMap.get(species));
				if (affectedConstraintSet.size() > 0) 
					constraintFlag = testConstraints(model, affectedConstraintSet);
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
				return true;
		}

		return false;
	}

	/**
	 * replaceArgument() doesn't work when you're replacing a localParameter, so this
	 * does that -- finds the oldString within node and replaces it with the local parameter
	 * specified by newString
	 * 
	 * @param node
	 * @param reactionID
	 * @param oldString
	 * @param newString
	 */
	private void alterLocalParameter(ASTNode node, Reaction reaction, String oldString, String newString) 
	{
		String reactionID = reaction.getId();
		if (node.isName() && node.getName().equals(oldString)) {
			//node.setValue(reaction.getKineticLaw().getLocalParameter(newString));
		}
		else {
			ASTNode childNode;
			for(int i = 0; i < node.getNumChildren(); i++)
			{
				childNode = node.getChild(i);
				alterLocalParameter(childNode, reaction, oldString, newString);
			}
		}
	}

	public void replaceArgument(ASTNode formula,String bvar, ASTNode arg) {
		int n = 0;
		for (int i = 0; i < formula.getChildCount(); i++) {
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar)) {
				formula.replaceChild(n, arg.clone());
			} else if (child.getChildCount() > 0) {
				replaceArgument(child, bvar, arg);
			}
			n++;
		}
	}

	/**
	 * performs assignment rules that may have changed due to events or reactions firing
	 * 
	 * @param affectedAssignmentRuleSet the set of assignment rules that have been affected
	 */
	protected HashSet<String> performAssignmentRules(ModelState modelstate, HashSet<AssignmentRule> affectedAssignmentRuleSet) {

		HashSet<String> affectedVariables = new HashSet<String>();

		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {

			String variable = assignmentRule.getVariable();

			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (modelstate.variableToIsConstantMap.containsKey(variable) && modelstate.variableToIsConstantMap.get(variable) == false
					|| modelstate.variableToIsConstantMap.containsKey(variable) == false) {

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
					if(replacements.containsKey(variable))
						replacements.put(variable, 
								evaluateExpressionRecursive(modelstate, assignmentRule.getMath()) * 
								modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
					//modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(variable)));
					else
						modelstate.variableToValueMap.put(variable, 
								evaluateExpressionRecursive(modelstate, assignmentRule.getMath()) * 
								//modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(variable)));
								modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
				}
				else {
					if(replacements.containsKey(variable))
						replacements.put(variable, evaluateExpressionRecursive(modelstate, assignmentRule.getMath()));
					else
						modelstate.variableToValueMap.put(variable, evaluateExpressionRecursive(modelstate, assignmentRule.getMath()));
				}

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

	/**
	 * updates the event queue and fires events and so on
	 * @param currentTime the current time in the simulation
	 */
	protected void handleEvents(ModelState modelstate, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		HashSet<String> triggeredEvents = new HashSet<String>();

		//loop through all untriggered events
		//if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : modelstate.untriggeredEventSet) {
			//if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(untriggeredEventID))) == true) {

				//skip the event if it's initially true and this is time == 0
				if (currentTime == 0.0 && modelstate.eventToTriggerInitiallyTrueMap.get(untriggeredEventID) == true)
					continue;

				//switch from false to true must happen
				if (modelstate.eventToPreviousTriggerValueMap.get(untriggeredEventID) == true)
					continue;

				triggeredEvents.add(untriggeredEventID);

				//if assignment is to be evaluated at trigger time, evaluate it and replace the ASTNode assignment
				if (modelstate.eventToUseValuesFromTriggerTimeMap.get(untriggeredEventID) == true)	{

					//temporary hashset of evaluated assignments
					HashSet<Object> evaluatedAssignments = new HashSet<Object>();

					for (Object evAssignment : modelstate.eventToAssignmentSetMap.get(untriggeredEventID)) {

						EventAssignment eventAssignment = (EventAssignment) evAssignment;
						evaluatedAssignments.add(new StringDoublePair(
								eventAssignment.getVariable(), evaluateExpressionRecursive(modelstate, eventAssignment.getMath())));
					}

					double fireTime = currentTime;

					if (modelstate.eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.eventToDelayMap.get(untriggeredEventID));

					modelstate.triggeredEventQueue.add(new EventToFire(IDtoIndex.get(modelstate.ID),
							untriggeredEventID, evaluatedAssignments, fireTime));
				}
				else {

					double fireTime = currentTime;

					if (modelstate.eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.eventToDelayMap.get(untriggeredEventID));
					modelstate.triggeredEventQueue.add(new EventToFire(IDtoIndex.get(modelstate.ID),
							untriggeredEventID, modelstate.eventToAssignmentSetMap.get(untriggeredEventID), fireTime));
				}			
			}
			else {

				modelstate.eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
			}
		}

		//remove recently triggered events from the untriggered set
		//when they're fired, they get put back into the untriggered set
		modelstate.untriggeredEventSet.removeAll(triggeredEvents);
	}



	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(ModelState modelstate, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();

		//loop through all triggered events
		//if the trigger is no longer true
		//remove from triggered queue and put into untriggered set
		for (EventToFire triggeredEvent : modelstate.triggeredEventQueue)
		{
			String triggeredEventID = triggeredEvent.eventID;

			//if the trigger evaluates to false
			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {

				untriggeredEvents.add(triggeredEventID);
				modelstate.eventToPreviousTriggerValueMap.put(triggeredEventID, false);
			}

			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {
				modelstate.untriggeredEventSet.add(triggeredEventID);
			}
		}

		//copy the triggered event queue -- except the events that are now untriggered
		//this is done because the remove function can't work with just a string; it needs to match events
		//this also re-evaluates the priorities in case they have changed
		//LinkedList<EventToFire> newTriggeredEventQueue = new LinkedList<EventToFire>();

		PriorityQueue<EventToFire> newTriggeredEventQueue = new PriorityQueue<EventToFire>((int)modelstate.numEvents, eventComparator);


		while (modelstate.triggeredEventQueue.size() > 0) {

			EventToFire event = modelstate.triggeredEventQueue.poll();
			EventToFire eventToAdd = new EventToFire(IDtoIndex.get(modelstate.ID), event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

			if (untriggeredEvents.contains(event.eventID) == false)
				newTriggeredEventQueue.add(eventToAdd);
			else
				modelstate.untriggeredEventSet.add(event.eventID);
		}

		modelstate.triggeredEventQueue = newTriggeredEventQueue;

		//loop through untriggered events
		//if the trigger is no longer true
		//set the previous trigger value to false
		for (String untriggeredEventID : modelstate.untriggeredEventSet) {

			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(untriggeredEventID))) == false)
				modelstate.eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
		}

		//these are sets of things that need to be re-evaluated or tested due to the event firing
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//set of fired events to add to the untriggered set
		HashSet<String> firedEvents = new HashSet<String>();


		//fire all events whose fire time is less than the current time	
		while (modelstate.triggeredEventQueue.size() > 0 && modelstate.triggeredEventQueue.peek().fireTime <= currentTime) {

			EventToFire eventToFire = modelstate.triggeredEventQueue.poll();
			String eventToFireID = eventToFire.eventID;

			//System.err.println("firing " + eventToFireID);

			if (modelstate.eventToAffectedReactionSetMap.get(eventToFireID) != null)
				affectedReactionSet.addAll(modelstate.eventToAffectedReactionSetMap.get(eventToFireID));

			firedEvents.add(eventToFireID);
			modelstate.eventToPreviousTriggerValueMap.put(eventToFireID, true);


			//execute all assignments for this event
			for (Object eventAssignment : eventToFire.eventAssignmentSet) {

				String variable;
				double assignmentValue;


				if (modelstate.eventToUseValuesFromTriggerTimeMap.get(eventToFireID) == true)	
				{
					variable = ((StringDoublePair) eventAssignment).string;
					assignmentValue = ((StringDoublePair) eventAssignment).doub;
				}
				else
				{
					variable = ((EventAssignment) eventAssignment).getVariable();
					assignmentValue = evaluateExpressionRecursive(modelstate, ((EventAssignment) eventAssignment).getMath());
				}

				//update the species, but only if it's not a constant (bound. cond. is fine)
				if (modelstate.variableToIsConstantMap.get(variable) == false) {

					if(replacements.containsKey(variable) && this.replacementSubModels.get(variable).contains(modelstate.ID))
						replacements.put(variable, assignmentValue);
					else
						modelstate.variableToValueMap.put(variable, assignmentValue);
				}

				if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(variable) == true) 
					affectedAssignmentRuleSet.addAll(modelstate.variableToAffectedAssignmentRuleSetMap.get(variable));
				if (noConstraintsFlag == false && modelstate.variableToIsInConstraintMap.get(variable) == true)
					affectedConstraintSet.addAll(modelstate.variableToAffectedConstraintSetMap.get(variable));

			} //end loop through assignments

			//after an event fires, need to make sure the queue is updated
			untriggeredEvents.clear();

			//loop through all triggered events
			//if they aren't persistent and the trigger is no longer true
			//remove from triggered queue and put into untriggered set
			for (EventToFire triggeredEvent : modelstate.triggeredEventQueue) {

				String triggeredEventID = triggeredEvent.eventID;

				//if the trigger evaluates to false and the trigger isn't persistent
				if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {

					untriggeredEvents.add(triggeredEventID);
					modelstate.eventToPreviousTriggerValueMap.put(triggeredEventID, false);
				}

				if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false)
					modelstate.untriggeredEventSet.add(triggeredEventID);
			}

			//copy the triggered event queue -- except the events that are now untriggered
			//this is done because the remove function can't work with just a string; it needs to match events
			//this also re-evaluates the priorities in case they have changed
			//newTriggeredEventQueue = new LinkedList<EventToFire>();
			newTriggeredEventQueue = new PriorityQueue<EventToFire>((int)modelstate.numEvents, eventComparator);

			while (modelstate.triggeredEventQueue.size() > 0) {

				EventToFire event = modelstate.triggeredEventQueue.poll();
				EventToFire eventToAdd = 
						new EventToFire(IDtoIndex.get(modelstate.ID), event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

				if (untriggeredEvents.contains(event.eventID) == false)
					newTriggeredEventQueue.add(eventToAdd);
				else
					modelstate.untriggeredEventSet.add(event.eventID);
			}

			modelstate.triggeredEventQueue = newTriggeredEventQueue;

			//some events might trigger after this
			handleEvents(modelstate, noAssignmentRulesFlag, noConstraintsFlag);
		}//end loop through event queue

		//add the fired events back into the untriggered set
		//this allows them to trigger/fire again later


		modelstate.untriggeredEventSet.addAll(firedEvents);

		return affectedReactionSet;
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate) {

		if (modelstate.numConstraints > 0)
			modelstate.noConstraintsFlag = false;
		
		//loop through all constraints to find out which variables affect which constraints
		//this is stored in a hashmap, as is whether the variable is in a constraint
		for (Constraint constraint : modelstate.model.getListOfConstraints()) {

			constraint.setMath(inlineFormula(modelstate, constraint.getMath()));
			for (ASTNode constraintNode : constraint.getMath().getListOfNodes()) {

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

		if (modelstate.numConstraints > 0)
			modelstate.variableToIsInConstraintMap.put(speciesID, false);

		if (species.isSetInitialAmount())
		{
			if(replacements.containsKey(speciesID) && this.replacementSubModels.get(speciesID).contains(modelstate.ID))
				modelstate.variableToValueMap.put(speciesID, replacements.get(speciesID));
			else
				modelstate.variableToValueMap.put(speciesID, species.getInitialAmount());
		}

		else if (species.isSetInitialConcentration()) 
		{

			modelstate.variableToValueMap.put(speciesID, species.getInitialConcentration() 
					* modelstate.model.getCompartment(species.getCompartment()).getSize());
		}


		if (species.getHasOnlySubstanceUnits() == false) {

			//modelstate.speciesToCompartmentSizeMap.put(speciesID, modelstate.model.getCompartment(species.getCompartment()).getSize());
			modelstate.speciesToCompartmentNameMap.put(speciesID, species.getCompartment());

			//if (Double.isNaN(modelstate.model.getCompartment(species.getCompartment()).getSize()))
			//modelstate.speciesToCompartmentSizeMap.put(speciesID, 1.0);
		}	
		if (modelstate.numRules > 0)
			modelstate.variableToIsInAssignmentRuleMap.put(speciesID, false);

		if(!replacements.containsKey(speciesID) || replacingModel.get(speciesID).equals(modelstate.ID))
		{
			modelstate.speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
			modelstate.speciesToIsBoundaryConditionMap.put(speciesID, species.getBoundaryCondition());
			modelstate.variableToIsConstantMap.put(speciesID, species.getConstant());
			modelstate.speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.getHasOnlySubstanceUnits());
			modelstate.speciesIDSet.add(speciesID);
		}

	}

	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupReplacingSpecies() 
	{

		for(String speciesID : replacingModel.keySet())
		{
			String ID = replacingModel.get(speciesID);
			ModelState replacingModelState = getModel(IDtoIndex.get(ID));
			ModelState replacedModelState;
			for(String modelID : replacementSubModels.get(speciesID))
			{
				replacedModelState = getModel(IDtoIndex.get(modelID));
				replacedModelState.speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));
				replacedModelState.speciesToIsBoundaryConditionMap.put(speciesID, replacingModelState.speciesToIsBoundaryConditionMap.get(speciesID));
				replacedModelState.variableToIsConstantMap.put(speciesID, replacingModelState.variableToIsConstantMap.get(speciesID));
				replacedModelState.speciesToHasOnlySubstanceUnitsMap.put(speciesID, replacingModelState.speciesToHasOnlySubstanceUnitsMap.get(speciesID));
				replacedModelState.speciesIDSet.add(speciesID);
			}
		}
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

		setupReplacingSpecies();
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
			//	else 
			parameterID = localParameter.getId();

			String oldParameterID = localParameter.getId();
			modelstate.variableToValueMap.put(parameterID, localParameter.getValue());

			//alter the local parameter ID so that it goes to the local and not global value
			if (localParameter.getId() != parameterID) {
				localParameter.setId(parameterID);
				localParameter.setMetaId(parameterID);
			}
			alterLocalParameter(kineticLaw.getMath(), reaction, oldParameterID, parameterID);
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

		if (parameter.getConstant() == false)
			modelstate.nonconstantParameterIDSet.add(parameterID);

		if (modelstate.numRules > 0)
			modelstate.variableToIsInAssignmentRuleMap.put(parameterID, false);

		if (modelstate.numConstraints > 0)
			modelstate.variableToIsInConstraintMap.put(parameterID, false);
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate) {

		//add local parameters
		Reaction reaction;
		Parameter parameter;
		long size;


		size = modelstate.numReactions;
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


		//add compartment sizes in
		size = modelstate.model.getNumCompartments();
		for (int i = 0; i < size; i++) 
		{
			Compartment compartment = modelstate.model.getCompartment(i);
			String compartmentID = compartment.getId();

			modelstate.compartmentIDSet.add(compartmentID);
			modelstate.variableToValueMap.put(compartmentID, compartment.getSize());

			if (Double.isNaN(compartment.getSize()))
				modelstate.variableToValueMap.put(compartmentID, 1.0);

			modelstate.variableToIsConstantMap.put(compartmentID, compartment.getConstant());

			if (modelstate.numRules > 0)
				modelstate.variableToIsInAssignmentRuleMap.put(compartmentID, false);

			if (modelstate.numConstraints > 0)
				modelstate.variableToIsInConstraintMap.put(compartmentID, false);
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
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, 
			ListOf<ModifierSpeciesReference> modifiersList) {
		reactionID = reactionID.replace("_negative_","-");

		long size;
		boolean notEnoughMoleculesFlag = false;
		modelstate.reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
		modelstate.reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());



		size = reactantsList.size();

		if(replacementSubModels.get(reactionID) != null && replacementSubModels.get(reactionID).contains(modelstate.ID))
			return;

		for (int i = 0; i < size; i++)
		{

			SpeciesReference reactant = (SpeciesReference)reactantsList.get(i);

			String reactantID = reactant.getSpecies().replace("_negative_","-");
			double reactantStoichiometry;

			//if there was an initial assignment for the speciesref id
			if(replacements.containsKey(reactant.getId()) && this.replacementSubModels.get(reactant.getId()).contains(modelstate.ID))
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
			//System.out.println("Node: " + libsbml.formulaToString(reactionFormula));
			//System.out.println("Node: " + evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula).getLeftChild()));

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

		for (int i = 0;  i < modelstate.numReactions; i++) 
		{
			reaction = modelstate.model.getReaction(i);
			String reactionID = reaction.getId();
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();

			setupSingleReaction(modelstate, reactionID, reactionFormula, reaction.getReversible(), 
					reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(ModelState modelstate, Event event) {

		String eventID = event.getId();


		if (event.isSetPriority())
			modelstate.eventToPriorityMap.put(eventID, inlineFormula(modelstate, event.getPriority().getMath()));

		if (event.isSetDelay()) {

			modelstate.eventToDelayMap.put(eventID, inlineFormula(modelstate, event.getDelay().getMath()));
			modelstate.eventToHasDelayMap.put(eventID, true);
		}
		else
			modelstate.eventToHasDelayMap.put(eventID, false);

		event.getTrigger().setMath(inlineFormula(modelstate, event.getTrigger().getMath()));

		modelstate.eventToTriggerMap.put(eventID, event.getTrigger().getMath());
		modelstate.eventToTriggerInitiallyTrueMap.put(eventID, event.getTrigger().getInitialValue());
		modelstate.eventToPreviousTriggerValueMap.put(eventID, event.getTrigger().getInitialValue());
		modelstate.eventToTriggerPersistenceMap.put(eventID, event.getTrigger().getPersistent());
		modelstate.eventToUseValuesFromTriggerTimeMap.put(eventID, event.getUseValuesFromTriggerTime());
		modelstate.eventToAssignmentSetMap.put(eventID, new HashSet<Object>());
		modelstate.eventToAffectedReactionSetMap.put(eventID, new HashSet<String>());

		modelstate.untriggeredEventSet.add(eventID);

		for (EventAssignment assignment : event.getListOfEventAssignments()) {


			String variableID = assignment.getVariable();

			assignment.setMath(inlineFormula(modelstate, assignment.getMath()));

			modelstate.eventToAssignmentSetMap.get(eventID).add(assignment);

			if (modelstate.variableToEventSetMap.containsKey(variableID) == false)
				modelstate.variableToEventSetMap.put(variableID, new HashSet<String>());

			modelstate.variableToEventSetMap.get(variableID).add(eventID);

			//if the variable is a species, add the reactions it's in
			//to the event to affected reaction hashmap, which is used
			//for updating propensities after an event fires
			if (modelstate.speciesToAffectedReactionSetMap.containsKey(variableID)) {

				modelstate.eventToAffectedReactionSetMap.get(eventID).addAll(
						modelstate.speciesToAffectedReactionSetMap.get(variableID));
			}					
		}
	}

	/**
	 * puts initial assignment-related information into data structures
	 */
	protected void setupInitialAssignments(ModelState modelstate) {

		HashSet<String> affectedVariables = new HashSet<String>();
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();

		//perform all assignment rules
		for (Rule rule : modelstate.model.getListOfRules()) {

			if (rule.isAssignment())
				allAssignmentRules.add((AssignmentRule)rule);
		}

		performAssignmentRules(modelstate, allAssignmentRules);

		//calculate initial assignments a lot of times in case there are dependencies
		//running it the number of initial assignments times will avoid problems
		//and all of them will be fully calculated and determined
		for (int i = 0; i < modelstate.numInitialAssignments; ++i) {
			for (InitialAssignment initialAssignment : modelstate.model.getListOfInitialAssignments()) {

				String variable = initialAssignment.getVariable().replace("_negative_","-");				
				initialAssignment.setMath(inlineFormula(modelstate, initialAssignment.getMath()));

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {

					modelstate.variableToValueMap.put(variable, 
							evaluateExpressionRecursive(modelstate, initialAssignment.getMath()) * 
							modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(variable)));
				}
				else {
					if(replacements.containsKey(variable) && this.replacementSubModels.get(variable).contains(modelstate.ID))
						modelstate.variableToValueMap.put(variable, replacements.get(variable));
					else
						modelstate.variableToValueMap.put(variable, evaluateExpressionRecursive(modelstate, initialAssignment.getMath()));
				}

				affectedVariables.add(variable);
			}			
		}

		//perform assignment rules again for variable that may have changed due to the initial assignments
		//they aren't set up yet, so just perform them all
		performAssignmentRules(modelstate, allAssignmentRules);


	}

	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents(ModelState modelstate) {

		//add event information to hashmaps for easy/fast access
		//this needs to happen after calculating initial propensities
		//so that the speciesToAffectedReactionSetMap is populated

		long size = modelstate.model.getNumEvents();

		for (int i = 0; i < size; i++)
		{
			Event event = modelstate.model.getEvent(i);

			setupSingleEvent(modelstate, event);
		}
	}

	protected void setupRules(ModelState modelstate)
	{


		//modelstate.numAssignmentRules = 0;
		//modelstate.numRateRules = 0;

		//NOTE: assignmentrules are performed in setupinitialassignments

		//loop through all assignment rules
		//store which variables (RHS) affect the rule variable (LHS)
		//so when those RHS variables change, we know to re-evaluate the rule
		//and change the value of the LHS variable
		long size = modelstate.model.getListOfRules().size();

		if(size > 0)
			modelstate.noRuleFlag = false;


		for(Rule rule : modelstate.model.getListOfRules())
		{
			if (rule.isAssignment()) {

				//Rules don't have a getVariable method, so this needs to be cast to an ExplicitRule
				rule.setMath(inlineFormula(modelstate, rule.getMath()));
				AssignmentRule assignmentRule = (AssignmentRule) rule;

				//list of all children of the assignmentRule math
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (assignmentRule.getMath().getChildCount() == 0)
					formulaChildren.add(assignmentRule.getMath());
				else
					getAllASTNodeChildren(assignmentRule.getMath(), formulaChildren);

				for (ASTNode ruleNode : formulaChildren) {

					if (ruleNode.isName()) {

						String nodeName = ruleNode.getName();

						modelstate.variableToAffectedAssignmentRuleSetMap.put(nodeName, new HashSet<AssignmentRule>());
						modelstate.variableToAffectedAssignmentRuleSetMap.get(nodeName).add(assignmentRule);
						modelstate.variableToIsInAssignmentRuleMap.put(nodeName, true);
					}
				}

				//++numAssignmentRules;				
			}
			else if (rule.isRate()) {

				//Rules don't have a getVariable method, so this needs to be cast to an ExplicitRule
				rule.setMath(inlineFormula(modelstate, rule.getMath()));
				RateRule rateRule = (RateRule) rule;

				//list of all children of the assignmentRule math
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (rateRule.getMath().getNumChildren() == 0)
					formulaChildren.add(rateRule.getMath());
				else
					getAllASTNodeChildren(rateRule.getMath(), formulaChildren);

				for (ASTNode ruleNode : formulaChildren) {

					if (ruleNode.isName()) {

						String nodeName = ruleNode.getName();

						modelstate.variableToIsInRateRuleMap.put(nodeName, true);
					}
				}

				//++numRateRules;	
			}
		}


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

	//EVENT TO FIRE INNER CLASS
	/**
	 * easy way to store multiple data points for events that are firing
	 */
	protected class EventToFire {

		public String eventID = "";
		public HashSet<Object> eventAssignmentSet = null;
		public double fireTime = 0.0;
		public int index;

		public EventToFire(int index, String eventID, HashSet<Object> eventAssignmentSet, double fireTime) {

			this.eventID = eventID;
			this.eventAssignmentSet = eventAssignmentSet;
			this.fireTime = fireTime;	
			this.index = index;
		}
	}

	//EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the priority queue
	 */
	protected class EventComparator implements Comparator<EventToFire> {

		/**
		 * compares two events based on their fire times and priorities
		 */
		public int compare(EventToFire event1, EventToFire event2) {

			if (event1.fireTime > event2.fireTime)
				return 1;
			else if (event1.fireTime < event2.fireTime)
				return -1;
			else {
				ModelState state1;
				ModelState state2;
				if(event1.index == -1)
					state1 = topmodel;
				else
					state1 = submodels[event1.index];
				if(event2.index == -1)
					state2 = topmodel;
				else
					state2 = submodels[event2.index];

				if (state1.eventToPriorityMap.get(event1.eventID) == null) {
					if (state2.eventToPriorityMap.get(event2.eventID) != null)
						return -1;
					else {

						if ((Math.random() * 100) > 50) {
							return -1;
						}
						else {
							return 1;
						}
					}
				}

				if (evaluateExpressionRecursive(state1, state1.eventToPriorityMap.get(event1.eventID)) >  
				evaluateExpressionRecursive(state2, state2.eventToPriorityMap.get(event2.eventID)))
					return -1;
				else if ( evaluateExpressionRecursive(state1, state1.eventToPriorityMap.get(event1.eventID)) <  
						evaluateExpressionRecursive(state2, state2.eventToPriorityMap.get(event2.eventID)))
					return 1;
				else {
					if ((Math.random() * 100) > 50) {
						return -1;
					}
					else {
						return 1;
					}
				}
			}
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
		protected long numEvents;
		protected long numConstraints;
		protected long numRules;
		protected String ID;
		protected boolean noEventsFlag = true;

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


		//hashmaps that allow for access to event information from the event's id
		protected HashMap<String, ASTNode> eventToPriorityMap = null;
		protected HashMap<String, ASTNode> eventToDelayMap = null;
		protected HashMap<String, Boolean> eventToHasDelayMap = null;
		protected HashMap<String, Boolean> eventToTriggerPersistenceMap = null;
		protected HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap = null;
		protected HashMap<String, ASTNode> eventToTriggerMap = null;
		protected HashMap<String, Boolean> eventToTriggerInitiallyTrueMap = null;
		protected HashMap<String, Boolean> eventToPreviousTriggerValueMap = null;
		protected HashMap<String, HashSet<Object> > eventToAssignmentSetMap = null;
		protected HashMap<String, HashSet<String> > variableToEventSetMap = null;

		//allows for access to the reactions whose propensity changes when an event fires
		protected HashMap<String, HashSet<String> > eventToAffectedReactionSetMap = null;

		protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();

		//propensity variables
		protected double propensity = 0.0;
		protected double minPropensity = Double.MAX_VALUE / 10.0;
		protected double maxPropensity = Double.MIN_VALUE / 10.0;

		//file writing variables
		protected FileWriter TSDWriter = null;
		protected BufferedWriter bufferedTSDWriter = null;

		protected boolean printConcentrations = false;
		protected boolean noConstraintsFlag = true;
		protected boolean noRuleFlag = true;
		protected JFrame running = new JFrame();


		PsRandom prng = new PsRandom();

		//stores events in order of fire time and priority
		//protected LinkedList<EventToFire> triggeredEventQueue = null;
		protected PriorityQueue<EventToFire> triggeredEventQueue;
		protected HashSet<String> untriggeredEventSet = null;


		//allows for access to the set of constraints that a variable affects
		protected HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap = null;

		protected HashMap<String, Boolean> variableToIsInConstraintMap = null;

		//allows to access to whether or not a variable is in an assignment or rate rule rule (RHS)
		protected HashMap<String, Boolean> variableToIsInAssignmentRuleMap = null;
		protected HashMap<String, Boolean> variableToIsInRateRuleMap = null;

		//allows for access to the set of assignment rules that a variable (rhs) in an assignment rule affects
		protected HashMap<String, HashSet<AssignmentRule> > variableToAffectedAssignmentRuleSetMap = null;
		protected LinkedHashSet<String> nonconstantParameterIDSet;
		protected HashMap<String, HashSet<StringStringPair> > reactionToNonconstantStoichiometriesSetMap = null;


		//protected TObjectDoubleHashMap<String> speciesToCompartmentSizeMap = null;
		protected LinkedHashSet<String> compartmentIDSet = new LinkedHashSet<String>();

		public ModelState(Model bioModel, boolean isCopy, String submodelID)
		{
			this.model = bioModel;
			this.numSpecies = this.model.getNumSpecies();
			this.numParameters = this.model.getNumParameters();
			this.numReactions = this.model.getNumReactions();
			this.numInitialAssignments = (int)this.model.getNumInitialAssignments();
			this.ID = submodelID;
			this.numEvents = this.model.getNumEvents();
			this.numRules = this.model.getNumRules();
			this.numConstraints= this.model.getNumConstraints();
			//this.isCopy = isCopy;

			//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
			speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
			speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
			variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
			//speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
			speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
			speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
			variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);

			reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
			reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
			reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
			reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));


			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int) model.getNumConstraints());		
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));

			if (numEvents > 0) {
				noEventsFlag = false;
				triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
				untriggeredEventSet = new HashSet<String>((int) numEvents);
				eventToPriorityMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
				eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) numEvents);
				eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
				eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
				eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
				eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
				eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) numEvents);
				variableToEventSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
			}

			if (numRules > 0) {

				variableToAffectedAssignmentRuleSetMap = new HashMap<String, HashSet<AssignmentRule> >((int) numRules);
				variableToIsInAssignmentRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
				variableToIsInRateRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			}
			nonconstantParameterIDSet = new LinkedHashSet<String>();
			reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<StringStringPair> >();

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

		protected double getVariableToValue(String variable)
		{
			if(replacements.containsKey(variable) && replacementSubModels.get(variable).contains(this.ID))
				return replacements.get(variable);
			else
				return variableToValueMap.get(variable);
		}

		protected void setvariableToValueMap(String variable, double value)
		{
			if(replacements.containsKey(variable) && replacementSubModels.get(variable).contains(this.ID))
				replacements.put(variable, value);
			else
				variableToValueMap.put(variable, value);
		}

	}
}



