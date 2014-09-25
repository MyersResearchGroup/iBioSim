package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.SBMLDocument;

import analysis.dynamicsim.XORShiftRandom;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import flanagan.math.Fmath;
import flanagan.math.PsRandom;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public abstract class HierarchicalSim extends HierarchicalSimState {

	private ArrayList<String> filesCreated; 
	private HashMap<String, Double> initReplacementState; 
	private boolean isGrid; 
	private HashMap<String, Model> models;
	private int numSubmodels;
	private PsRandom prng;
	private HashMap<String, Double> replacements;
	private HashMap<String, ModelState> submodels;
	private ModelState topmodel;
	private HierarchicalEventComparator eventComparator;
	private XORShiftRandom randomNumberGenerator;
	public HierarchicalSim(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, 
			JFrame running, String[] interestingSpecies, String quantityType) throws IOException, XMLStreamException 
			{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);
		replacements = new HashMap<String,Double>();
		initReplacementState = new HashMap<String, Double>();
		models = new HashMap<String, Model>();
		SBMLDocument document = getDocument();
		isGrid = checkGrid(document.getModel());
		models.put(document.getModel().getId(), document.getModel());
		
			}
	protected ModelState getModel(String id)
	{
		if(id.equals("topmodel"))
			return topmodel;
		return submodels.get(id);
	}
	protected static boolean checkGrid(Model model)
	{
		if(model.getCompartment("Grid") != null)
			return true;
		return false;
	}
	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ModelState modelstate, ASTNode formula) {

		if (formula.isFunction() == false || formula.isLeaf() == false) {

			for (int i = 0; i < formula.getChildCount(); ++i)
				formula.replaceChild(i,
						inlineFormula(modelstate, formula.getChild(i)));// .clone()));
		}

		if (formula.isFunction()
				&& models.get(modelstate.getModel()).getFunctionDefinition(
						formula.getName()) != null) {

			if (modelstate.getIbiosimFunctionDefinitions().contains(
					formula.getName()))
				return formula;

			ASTNode inlinedFormula = models.get(modelstate.getModel())
					.getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
				inlinedChildren.add(inlinedFormula);

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < models.get(modelstate.getModel())
					.getFunctionDefinition(formula.getName())
					.getArgumentCount(); ++i) {
				inlinedChildToOldIndexMap.put(models.get(modelstate.getModel())
						.getFunctionDefinition(formula.getName())
						.getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i) {

				ASTNode child = inlinedChildren.get(i);
				// if ((child.getLeftChild() == null && child.getRightChild() ==
				// null) && child.isName()) {
				if ((child.getChildCount() == 0) && child.isName()) {

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(),
							oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
						inlinedFormula = oldFormula.getChild(index);
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	protected double evaluateExpressionRecursive(ModelState modelstate, ASTNode node) {
		if (node.isBoolean()) {

			switch (node.getType()) {

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return HierarchicalUtilities.getDoubleFromBoolean(!(HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getLeftChild()))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return HierarchicalUtilities.getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return HierarchicalUtilities.getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = (node.getChildCount()==0) ? false:
					HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0)));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return HierarchicalUtilities.getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) == evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_NEQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) != evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GEQ:
			{
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) >= evaluateExpressionRecursive(modelstate, node.getRightChild()));
			}
			case RELATIONAL_LEQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) <= evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GT:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) > evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_LT:
			{
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) < evaluateExpressionRecursive(modelstate, node.getRightChild()));			
			}

			default:
				return 0.0;

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;


			default:
				return 0.0;
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

				return getCurrentTime();
			}
			//if it's a reaction id return the propensity
			else if (modelstate.getReactionToPropensityMap().keySet().contains(node.getName())) {
				return modelstate.getReactionToPropensityMap().get(node.getName());
			}
			else {

				double value;

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(name) &&
						modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(name) == false) {
					//value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
					value = (modelstate.getVariableToValue(replacements, name) / modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(name)));
				}
				else	
				{	
					value = modelstate.getVariableToValue(replacements, name);
				}
				return value;
			}
		}

		//operators/functions with two children
		else {

			//ASTNode leftChild = node.getLeftChild();
			//ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); childIter++)
					sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case MINUS: {
				ASTNode leftChild = node.getLeftChild();
				double sum = evaluateExpressionRecursive(modelstate, leftChild);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter));

				return product;
			}

			case DIVIDE:
			{
				ASTNode leftChild = node.getLeftChild();
				ASTNode rightChild = node.getRightChild();

				return (evaluateExpressionRecursive(modelstate, leftChild) / evaluateExpressionRecursive(modelstate, rightChild));

			}
			case FUNCTION_POWER:
			{
				ASTNode leftChild = node.getLeftChild();
				ASTNode rightChild = node.getRightChild();

				return (FastMath.pow(evaluateExpressionRecursive(modelstate, leftChild), evaluateExpressionRecursive(modelstate, rightChild)));
			}
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
				double base = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				double var = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(1)));
				return var/base;


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
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3) {

					if ((childIter + 1) < node.getChildCount() && 
							HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter + 1)))) {
						return evaluateExpressionRecursive(modelstate, node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getChildCount()) {
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
			{
				double x = evaluateExpressionRecursive(modelstate, node.getChild(0));
				return FastMath.log(1/x + FastMath.sqrt(1 + 1/(x*x)));
			}
			//return Fmath.acsch(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSECH:
			{
				double x = evaluateExpressionRecursive(modelstate, node.getChild(0));
				return FastMath.log((1 + FastMath.sqrt(1 - x*x))/x);
			}


			default:
				return 0.0;

				//return Fmath.asech(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			} //end switch

		}
		return 0.0;
	}


	protected HashSet<String> performAssignmentRules(ModelState modelstate, HashSet<AssignmentRule> affectedAssignmentRuleSet) {


		HashSet<String> affectedVariables = new HashSet<String>();

		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {

			String variable = assignmentRule.getVariable();

			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (modelstate.getVariableToIsConstantMap().containsKey(variable) && modelstate.getVariableToIsConstantMap().get(variable) == false
					|| modelstate.getVariableToIsConstantMap().containsKey(variable) == false) {

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable) &&
						modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false) {
					modelstate.setvariableToValueMap(replacements, variable, 
							evaluateExpressionRecursive(modelstate, assignmentRule.getMath()) * 
							//modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(variable)));
							modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable)));
				}
				else {
					modelstate.setvariableToValueMap(replacements, variable, evaluateExpressionRecursive(modelstate, assignmentRule.getMath()));
				}

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}


	protected class HierarchicalEventToFire {

		public String eventID = "";
		public HashSet<Object> eventAssignmentSet = null;
		public double fireTime = 0.0;
		public String modelID;

		public HierarchicalEventToFire(String modelID, String eventID, HashSet<Object> eventAssignmentSet, double fireTime) {

			this.eventID = eventID;
			this.eventAssignmentSet = eventAssignmentSet;
			this.fireTime = fireTime;	
			this.modelID = modelID;
		}

		/**
		 * @return the eventID
		 */
		public String getEventID() {
			return eventID;
		}

		/**
		 * @return the eventAssignmentSet
		 */
		public HashSet<Object> getEventAssignmentSet() {
			return eventAssignmentSet;
		}

		/**
		 * @return the fireTime
		 */
		public double getFireTime() {
			return fireTime;
		}

		/**
		 * @return the modelID
		 */
		public String getModelID() {
			return modelID;
		}

		/**
		 * @param eventID the eventID to set
		 */
		public void setEventID(String eventID) {
			this.eventID = eventID;
		}

		/**
		 * @param eventAssignmentSet the eventAssignmentSet to set
		 */
		public void setEventAssignmentSet(HashSet<Object> eventAssignmentSet) {
			this.eventAssignmentSet = eventAssignmentSet;
		}

		/**
		 * @param fireTime the fireTime to set
		 */
		public void setFireTime(double fireTime) {
			this.fireTime = fireTime;
		}

		/**
		 * @param modelID the modelID to set
		 */
		public void setModelID(String modelID) {
			this.modelID = modelID;
		}
	}

	//EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the priority queue
	 */
	protected class HierarchicalEventComparator implements Comparator<HierarchicalEventToFire> {

		/**
		 * compares two events based on their fire times and priorities
		 */
		@Override
		public int compare(HierarchicalEventToFire event1, HierarchicalEventToFire event2) {

			if (event1.fireTime > event2.fireTime)
				return 1;
			else if (event1.fireTime < event2.fireTime)
				return -1;
			else {
				ModelState state1;
				ModelState state2;
				if(event1.modelID.equals("topmodel"))
					state1 = topmodel;
				else
					state1 = submodels.get(event1.modelID);
				if(event2.modelID.equals("topmodel"))
					state2 = topmodel;
				else
					state2 = submodels.get(event2.modelID);

				if (state1.eventToPriorityMap.get(event1.eventID) == null) {
					if (state2.eventToPriorityMap.get(event2.eventID) != null)
						return -1;
					if ((Math.random() * 100) > 50) {
						return -1;
					}
					return 1;
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
					return 1;
				}
			}
		}
	}
	protected double evaluateStateExpressionRecursive(ModelState modelstate, ASTNode node, double t, double[] y, HashMap<String, Integer> variableToIndexMap) {
		if (node.isBoolean()) {

			switch (node.getType()) {

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return HierarchicalUtilities.getDoubleFromBoolean(!(HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return HierarchicalUtilities.getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return HierarchicalUtilities.getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return HierarchicalUtilities.getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) == evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_NEQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) != evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_GEQ:
			{
				//System.out.println("Node: " + libsbml.formulaToString(node.getRightChild()) + " " + evaluateStateExpressionRecursive(modelstate, node.getRightChild()));
				//System.out.println("Node: " + evaluateStateExpressionRecursive(modelstate, node.getLeftChild()) + " " + evaluateStateExpressionRecursive(modelstate, node.getRightChild()));

				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) >= evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
			}
			case RELATIONAL_LEQ:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) <= evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_GT:
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) > evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_LT:
			{
				return HierarchicalUtilities.getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) < evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));			
			}


			default:
				return 0.0;

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;


			default:
				return 0.0;
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

				return getCurrentTime();
			}
			double value;
			int i, j;
			if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(name) &&
					modelstate.speciesToHasOnlySubstanceUnitsMap.get(name) == false) {
				//value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
				//value = (modelstate.getVariableToValue(name) / modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(name)));
				i = variableToIndexMap.get(name);
				j = variableToIndexMap.get(modelstate.speciesToCompartmentNameMap.get(name));
				value =  y[i] / y[j];
			}
			else	
			{	
				i = variableToIndexMap.get(name);
				value = y[i];
			}
			return value;
		}

		//operators/functions with two children
		else {

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					sum += evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);					

				return sum;
			}

			case MINUS: {

				double sum = evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);

				return product;
			}

			case DIVIDE:
				return (evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap) / evaluateStateExpressionRecursive(modelstate, rightChild, t, y, variableToIndexMap));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap), evaluateStateExpressionRecursive(modelstate, rightChild, t, y, variableToIndexMap)));

			case FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();

				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {

					double leftChildValue = evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap);
					double rightChildValue = evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap);
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential")) {

					return prng.nextExponential(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 1);
				}
				else if (nodeName.equals("gamma")) {

					return prng.nextGamma(1, evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("chisq")) {

					return prng.nextChiSquare((int) evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("lognormal")) {

					return prng.nextLogNormal(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("laplace")) {

					//function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy")) {

					return prng.nextLorentzian(0, evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("poisson")) {

					return prng.nextPoissonian(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("binomial")) {

					return prng.nextBinomial(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap),
							(int) evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("bernoulli")) {

					return prng.nextBinomial(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 1);
				}
				else if (nodeName.equals("normal")) {

					return prng.nextGaussian(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap),
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));	
				}


				break;
			}

			case FUNCTION_ABS:
				return FastMath.abs(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));				

			case FUNCTION_COS:
				return FastMath.cos(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COSH:
				return FastMath.cosh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_EXP:
				return FastMath.exp(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_LN:
				return FastMath.log(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_LOG:
				return FastMath.log10(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SIN:
				return FastMath.sin(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SINH:
				return FastMath.sinh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_TAN:
				return FastMath.tan(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_TANH:		
				return FastMath.tanh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_PIECEWISE: {

				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3) {

					if ((childIter + 1) < node.getChildCount() && 
							HierarchicalUtilities.getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter + 1), t, y, variableToIndexMap))) {
						return evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);
					}
					else if ((childIter + 2) < node.getChildCount()) {
						return evaluateStateExpressionRecursive(modelstate, node.getChild(childIter + 2), t, y, variableToIndexMap);
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap), 
						1 / evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));

			case FUNCTION_SEC:
				return Fmath.sec(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SECH:
				return Fmath.sech(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COT:
				return Fmath.cot(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COTH:
				return Fmath.coth(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CSC:
				return Fmath.csc(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CSCH:
				return Fmath.csch(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSECH:
				return Fmath.asech(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			default:
				return 0.0;
			} //end switch

		}
		return 0.0;
	}
	public class ModelState {

		private String model;
		private long numSpecies;
		private long numParameters;
		private long numReactions;
		private int numInitialAssignments;
		private int numRateRules;
		private long numEvents;
		private long numConstraints;
		private long numRules;
		private long numCompartments;
		private String ID;
		private boolean noEventsFlag = true;
		private HierarchicalEventComparator eventComparator;
		private TObjectDoubleHashMap<String> reactionToPropensityMap;
		private HashMap<String, HashSet<HierarchicalStringDoublePair> > reactionToSpeciesAndStoichiometrySetMap;
		private HashMap<String, HashSet<HierarchicalStringDoublePair> > reactionToReactantStoichiometrySetMap;
		private HashMap<String, ASTNode> reactionToFormulaMap;
		private HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap;
		private HashMap<String, Boolean> speciesToIsBoundaryConditionMap;
		private HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap;
		private HashMap<String, String> speciesToCompartmentNameMap;
		private LinkedHashSet<String> speciesIDSet;
		private TObjectDoubleHashMap<String> variableToValueMap;
		private HashMap<String, Boolean> variableToIsConstantMap;
		private HashMap<String, ASTNode> eventToPriorityMap;
		private HashMap<String, ASTNode> eventToDelayMap;
		private HashMap<String, Boolean> eventToHasDelayMap;
		private HashMap<String, Boolean> eventToTriggerPersistenceMap;
		private HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap;
		private HashMap<String, ASTNode> eventToTriggerMap;
		private HashMap<String, Boolean> eventToTriggerInitiallyTrueMap;
		private HashMap<String, Boolean> eventToPreviousTriggerValueMap;
		private HashMap<String, HashSet<Object> > eventToAssignmentSetMap;
		private HashMap<String, HashSet<String> > variableToEventSetMap;
		private HashMap<String, HashSet<String> > eventToAffectedReactionSetMap;
		private HashSet<String> ibiosimFunctionDefinitions;
		private double propensity;
		private double minPropensity;
		private double maxPropensity;
		private boolean noConstraintsFlag;
		private boolean noRuleFlag;
		private PriorityQueue<HierarchicalEventToFire> triggeredEventQueue;
		private HashSet<String> untriggeredEventSet;
		private HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap;
		private HashMap<String, Boolean> variableToIsInConstraintMap;
		private HashMap<String, Boolean> variableToIsInAssignmentRuleMap;
		private HashMap<String, Boolean> variableToIsInRateRuleMap;
		private HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap;
		private LinkedHashSet<String> nonconstantParameterIDSet;
		private HashMap<String, HashSet<HierarchicalStringPair> > reactionToNonconstantStoichiometriesSetMap;
		private LinkedHashSet<String> compartmentIDSet;
		private List<RateRule> rateRulesList;
		private HashSet<String> nonConstantStoichiometry;
		private HashSet<String> isHierarchical;
		private HashMap<String, String> replacementDependency;
		private HashSet<String> variablesToPrint; 
		private HashSet<String> deletedElementsById; 
		private HashSet<String> deletedElementsByMetaId; 
		private HashSet<String> deletedElementsByUId; 

		public ModelState(HashMap<String, Model> models, String bioModel, String submodelID)
		{
			this.model = bioModel;
			this.ID = submodelID;

			setCountVariables(models.get(model));
			minPropensity = Double.MAX_VALUE / 10.0;
			maxPropensity = Double.MIN_VALUE / 10.0;
			noConstraintsFlag = true;
			noRuleFlag = true;
			 ibiosimFunctionDefinitions = new HashSet<String>();
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

			//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
			speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
			speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
			variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
			speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
			speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
			variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);

			reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
			reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<HierarchicalStringDoublePair> >((int) (numReactions * 1.5));	
			reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<HierarchicalStringDoublePair> >((int) (numReactions * 1.5));
			reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));


			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int)numConstraints);		
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));

			nonConstantStoichiometry = new HashSet<String>();

			 eventComparator = new HierarchicalEventComparator();
			 
			if (numEvents > 0) {
				noEventsFlag = false;
				triggeredEventQueue = new PriorityQueue<HierarchicalEventToFire>((int) numEvents, eventComparator);
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
			reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<HierarchicalStringPair> >();

			isHierarchical = new HashSet<String>();

			replacementDependency = new HashMap<String, String>();

			variablesToPrint = new HashSet<String>();

			deletedElementsById = new HashSet<String>();
			deletedElementsByMetaId = new HashSet<String>();
			deletedElementsByUId = new HashSet<String>();


			compartmentIDSet = new LinkedHashSet<String>();
			rateRulesList = new LinkedList<RateRule>();
		}

		public void clear()
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

		public void setCountVariables(Model model)
		{
			this.numSpecies = model.getSpeciesCount();
			this.numParameters = model.getParameterCount();
			this.numReactions = model.getReactionCount();
			this.numInitialAssignments = model.getInitialAssignmentCount();

			this.numEvents = model.getEventCount();
			this.numRules = model.getRuleCount();
			this.numConstraints= model.getConstraintCount();
			this.numCompartments = model.getCompartmentCount();
		}
		public double getVariableToValue(HashMap<String, Double> replacements, String variable)
		{
			if(isHierarchical.contains(variable))
			{
				String dep = replacementDependency.get(variable);
				return replacements.get(dep);
			}
			return variableToValueMap.get(variable);
		}

		public void setvariableToValueMap(HashMap<String, Double> replacements, String variable, double value)
		{
			if(isHierarchical.contains(variable))
			{
				String dep = replacementDependency.get(variable);
				replacements.put(dep, value);
			}
			variableToValueMap.put(variable, value);
		}

		public boolean isDeletedBySID(String sid)
		{
			if(deletedElementsById.contains(sid))
				return true;
			else
				return false;
		}


		public boolean isDeletedByMetaID(String metaid)
		{
			if(deletedElementsByMetaId.contains(metaid))
				return true;
			else
				return false;
		}

		public boolean isDeletedByUID(String uid)
		{
			if(deletedElementsByUId.contains(uid))
				return true;
			else
				return false;
		}

		/**
		 * @return the model
		 */
		public String getModel() {
			return model;
		}

		/**
		 * @param model the model to set
		 */
		public void setModel(String model) {
			this.model = model;
		}

		/**
		 * @return the numSpecies
		 */
		public long getNumSpecies() {
			return numSpecies;
		}

		/**
		 * @param numSpecies the numSpecies to set
		 */
		public void setNumSpecies(long numSpecies) {
			this.numSpecies = numSpecies;
		}

		/**
		 * @return the numParameters
		 */
		public long getNumParameters() {
			return numParameters;
		}

		/**
		 * @param numParameters the numParameters to set
		 */
		public void setNumParameters(long numParameters) {
			this.numParameters = numParameters;
		}

		/**
		 * @return the numReactions
		 */
		public long getNumReactions() {
			return numReactions;
		}

		/**
		 * @param numReactions the numReactions to set
		 */
		public void setNumReactions(long numReactions) {
			this.numReactions = numReactions;
		}

		/**
		 * @return the numInitialAssignments
		 */
		public int getNumInitialAssignments() {
			return numInitialAssignments;
		}

		/**
		 * @param numInitialAssignments the numInitialAssignments to set
		 */
		public void setNumInitialAssignments(int numInitialAssignments) {
			this.numInitialAssignments = numInitialAssignments;
		}

		/**
		 * @return the numRateRules
		 */
		public int getNumRateRules() {
			return numRateRules;
		}

		/**
		 * @param numRateRules the numRateRules to set
		 */
		public void setNumRateRules(int numRateRules) {
			this.numRateRules = numRateRules;
		}

		/**
		 * @return the numEvents
		 */
		public long getNumEvents() {
			return numEvents;
		}

		/**
		 * @param numEvents the numEvents to set
		 */
		public void setNumEvents(long numEvents) {
			this.numEvents = numEvents;
		}

		/**
		 * @return the numConstraints
		 */
		public long getNumConstraints() {
			return numConstraints;
		}

		/**
		 * @param numConstraints the numConstraints to set
		 */
		public void setNumConstraints(long numConstraints) {
			this.numConstraints = numConstraints;
		}

		/**
		 * @return the numRules
		 */
		public long getNumRules() {
			return numRules;
		}

		/**
		 * @param numRules the numRules to set
		 */
		public void setNumRules(long numRules) {
			this.numRules = numRules;
		}

		/**
		 * @return the numCompartments
		 */
		public long getNumCompartments() {
			return numCompartments;
		}

		/**
		 * @param numCompartments the numCompartments to set
		 */
		public void setNumCompartments(long numCompartments) {
			this.numCompartments = numCompartments;
		}

		/**
		 * @return the iD
		 */
		public String getID() {
			return ID;
		}

		/**
		 * @param iD the iD to set
		 */
		public void setID(String iD) {
			ID = iD;
		}

		/**
		 * @return the noEventsFlag
		 */
		public boolean isNoEventsFlag() {
			return noEventsFlag;
		}

		/**
		 * @param noEventsFlag the noEventsFlag to set
		 */
		public void setNoEventsFlag(boolean noEventsFlag) {
			this.noEventsFlag = noEventsFlag;
		}

		/**
		 * @return the eventComparator
		 */
		public HierarchicalEventComparator getEventComparator() {
			return eventComparator;
		}

		/**
		 * @param eventComparator the eventComparator to set
		 */
		public void setEventComparator(HierarchicalEventComparator eventComparator) {
			this.eventComparator = eventComparator;
		}

		/**
		 * @return the reactionToPropensityMap
		 */
		public TObjectDoubleHashMap<String> getReactionToPropensityMap() {
			return reactionToPropensityMap;
		}

		/**
		 * @param reactionToPropensityMap the reactionToPropensityMap to set
		 */
		public void setReactionToPropensityMap(
				TObjectDoubleHashMap<String> reactionToPropensityMap) {
			this.reactionToPropensityMap = reactionToPropensityMap;
		}

		/**
		 * @return the reactionToSpeciesAndStoichiometrySetMap
		 */
		public HashMap<String, HashSet<HierarchicalStringDoublePair>> getReactionToSpeciesAndStoichiometrySetMap() {
			return reactionToSpeciesAndStoichiometrySetMap;
		}

		/**
		 * @param reactionToSpeciesAndStoichiometrySetMap the reactionToSpeciesAndStoichiometrySetMap to set
		 */
		public void setReactionToSpeciesAndStoichiometrySetMap(
				HashMap<String, HashSet<HierarchicalStringDoublePair>> reactionToSpeciesAndStoichiometrySetMap) {
			this.reactionToSpeciesAndStoichiometrySetMap = reactionToSpeciesAndStoichiometrySetMap;
		}

		/**
		 * @return the reactionToReactantStoichiometrySetMap
		 */
		public HashMap<String, HashSet<HierarchicalStringDoublePair>> getReactionToReactantStoichiometrySetMap() {
			return reactionToReactantStoichiometrySetMap;
		}

		/**
		 * @param reactionToReactantStoichiometrySetMap the reactionToReactantStoichiometrySetMap to set
		 */
		public void setReactionToReactantStoichiometrySetMap(
				HashMap<String, HashSet<HierarchicalStringDoublePair>> reactionToReactantStoichiometrySetMap) {
			this.reactionToReactantStoichiometrySetMap = reactionToReactantStoichiometrySetMap;
		}

		/**
		 * @return the reactionToFormulaMap
		 */
		public HashMap<String, ASTNode> getReactionToFormulaMap() {
			return reactionToFormulaMap;
		}

		/**
		 * @param reactionToFormulaMap the reactionToFormulaMap to set
		 */
		public void setReactionToFormulaMap(
				HashMap<String, ASTNode> reactionToFormulaMap) {
			this.reactionToFormulaMap = reactionToFormulaMap;
		}

		/**
		 * @return the speciesToAffectedReactionSetMap
		 */
		public HashMap<String, HashSet<String>> getSpeciesToAffectedReactionSetMap() {
			return speciesToAffectedReactionSetMap;
		}

		/**
		 * @param speciesToAffectedReactionSetMap the speciesToAffectedReactionSetMap to set
		 */
		public void setSpeciesToAffectedReactionSetMap(
				HashMap<String, HashSet<String>> speciesToAffectedReactionSetMap) {
			this.speciesToAffectedReactionSetMap = speciesToAffectedReactionSetMap;
		}

		/**
		 * @return the speciesToIsBoundaryConditionMap
		 */
		public HashMap<String, Boolean> getSpeciesToIsBoundaryConditionMap() {
			return speciesToIsBoundaryConditionMap;
		}

		/**
		 * @param speciesToIsBoundaryConditionMap the speciesToIsBoundaryConditionMap to set
		 */
		public void setSpeciesToIsBoundaryConditionMap(
				HashMap<String, Boolean> speciesToIsBoundaryConditionMap) {
			this.speciesToIsBoundaryConditionMap = speciesToIsBoundaryConditionMap;
		}

		/**
		 * @return the speciesToHasOnlySubstanceUnitsMap
		 */
		public HashMap<String, Boolean> getSpeciesToHasOnlySubstanceUnitsMap() {
			return speciesToHasOnlySubstanceUnitsMap;
		}

		/**
		 * @param speciesToHasOnlySubstanceUnitsMap the speciesToHasOnlySubstanceUnitsMap to set
		 */
		public void setSpeciesToHasOnlySubstanceUnitsMap(
				HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap) {
			this.speciesToHasOnlySubstanceUnitsMap = speciesToHasOnlySubstanceUnitsMap;
		}

		/**
		 * @return the speciesToCompartmentNameMap
		 */
		public HashMap<String, String> getSpeciesToCompartmentNameMap() {
			return speciesToCompartmentNameMap;
		}

		/**
		 * @param speciesToCompartmentNameMap the speciesToCompartmentNameMap to set
		 */
		public void setSpeciesToCompartmentNameMap(
				HashMap<String, String> speciesToCompartmentNameMap) {
			this.speciesToCompartmentNameMap = speciesToCompartmentNameMap;
		}

		/**
		 * @return the speciesIDSet
		 */
		public LinkedHashSet<String> getSpeciesIDSet() {
			return speciesIDSet;
		}

		/**
		 * @param speciesIDSet the speciesIDSet to set
		 */
		public void setSpeciesIDSet(LinkedHashSet<String> speciesIDSet) {
			this.speciesIDSet = speciesIDSet;
		}

		/**
		 * @return the variableToValueMap
		 */
		public TObjectDoubleHashMap<String> getVariableToValueMap() {
			return variableToValueMap;
		}

		/**
		 * @param variableToValueMap the variableToValueMap to set
		 */
		public void setVariableToValueMap(
				TObjectDoubleHashMap<String> variableToValueMap) {
			this.variableToValueMap = variableToValueMap;
		}

		/**
		 * @return the variableToIsConstantMap
		 */
		public HashMap<String, Boolean> getVariableToIsConstantMap() {
			return variableToIsConstantMap;
		}

		/**
		 * @param variableToIsConstantMap the variableToIsConstantMap to set
		 */
		public void setVariableToIsConstantMap(
				HashMap<String, Boolean> variableToIsConstantMap) {
			this.variableToIsConstantMap = variableToIsConstantMap;
		}

		/**
		 * @return the eventToPriorityMap
		 */
		public HashMap<String, ASTNode> getEventToPriorityMap() {
			return eventToPriorityMap;
		}

		/**
		 * @param eventToPriorityMap the eventToPriorityMap to set
		 */
		public void setEventToPriorityMap(HashMap<String, ASTNode> eventToPriorityMap) {
			this.eventToPriorityMap = eventToPriorityMap;
		}

		/**
		 * @return the eventToDelayMap
		 */
		public HashMap<String, ASTNode> getEventToDelayMap() {
			return eventToDelayMap;
		}

		/**
		 * @param eventToDelayMap the eventToDelayMap to set
		 */
		public void setEventToDelayMap(HashMap<String, ASTNode> eventToDelayMap) {
			this.eventToDelayMap = eventToDelayMap;
		}

		/**
		 * @return the eventToHasDelayMap
		 */
		public HashMap<String, Boolean> getEventToHasDelayMap() {
			return eventToHasDelayMap;
		}

		/**
		 * @param eventToHasDelayMap the eventToHasDelayMap to set
		 */
		public void setEventToHasDelayMap(HashMap<String, Boolean> eventToHasDelayMap) {
			this.eventToHasDelayMap = eventToHasDelayMap;
		}

		/**
		 * @return the eventToTriggerPersistenceMap
		 */
		public HashMap<String, Boolean> getEventToTriggerPersistenceMap() {
			return eventToTriggerPersistenceMap;
		}

		/**
		 * @param eventToTriggerPersistenceMap the eventToTriggerPersistenceMap to set
		 */
		public void setEventToTriggerPersistenceMap(
				HashMap<String, Boolean> eventToTriggerPersistenceMap) {
			this.eventToTriggerPersistenceMap = eventToTriggerPersistenceMap;
		}

		/**
		 * @return the eventToUseValuesFromTriggerTimeMap
		 */
		public HashMap<String, Boolean> getEventToUseValuesFromTriggerTimeMap() {
			return eventToUseValuesFromTriggerTimeMap;
		}

		/**
		 * @param eventToUseValuesFromTriggerTimeMap the eventToUseValuesFromTriggerTimeMap to set
		 */
		public void setEventToUseValuesFromTriggerTimeMap(
				HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap) {
			this.eventToUseValuesFromTriggerTimeMap = eventToUseValuesFromTriggerTimeMap;
		}

		/**
		 * @return the eventToTriggerMap
		 */
		public HashMap<String, ASTNode> getEventToTriggerMap() {
			return eventToTriggerMap;
		}

		/**
		 * @param eventToTriggerMap the eventToTriggerMap to set
		 */
		public void setEventToTriggerMap(HashMap<String, ASTNode> eventToTriggerMap) {
			this.eventToTriggerMap = eventToTriggerMap;
		}

		/**
		 * @return the eventToTriggerInitiallyTrueMap
		 */
		public HashMap<String, Boolean> getEventToTriggerInitiallyTrueMap() {
			return eventToTriggerInitiallyTrueMap;
		}

		/**
		 * @param eventToTriggerInitiallyTrueMap the eventToTriggerInitiallyTrueMap to set
		 */
		public void setEventToTriggerInitiallyTrueMap(
				HashMap<String, Boolean> eventToTriggerInitiallyTrueMap) {
			this.eventToTriggerInitiallyTrueMap = eventToTriggerInitiallyTrueMap;
		}

		/**
		 * @return the eventToPreviousTriggerValueMap
		 */
		public HashMap<String, Boolean> getEventToPreviousTriggerValueMap() {
			return eventToPreviousTriggerValueMap;
		}

		/**
		 * @param eventToPreviousTriggerValueMap the eventToPreviousTriggerValueMap to set
		 */
		public void setEventToPreviousTriggerValueMap(
				HashMap<String, Boolean> eventToPreviousTriggerValueMap) {
			this.eventToPreviousTriggerValueMap = eventToPreviousTriggerValueMap;
		}

		/**
		 * @return the eventToAssignmentSetMap
		 */
		public HashMap<String, HashSet<Object>> getEventToAssignmentSetMap() {
			return eventToAssignmentSetMap;
		}

		/**
		 * @param eventToAssignmentSetMap the eventToAssignmentSetMap to set
		 */
		public void setEventToAssignmentSetMap(
				HashMap<String, HashSet<Object>> eventToAssignmentSetMap) {
			this.eventToAssignmentSetMap = eventToAssignmentSetMap;
		}

		/**
		 * @return the variableToEventSetMap
		 */
		public HashMap<String, HashSet<String>> getVariableToEventSetMap() {
			return variableToEventSetMap;
		}

		/**
		 * @param variableToEventSetMap the variableToEventSetMap to set
		 */
		public void setVariableToEventSetMap(
				HashMap<String, HashSet<String>> variableToEventSetMap) {
			this.variableToEventSetMap = variableToEventSetMap;
		}

		/**
		 * @return the eventToAffectedReactionSetMap
		 */
		public HashMap<String, HashSet<String>> getEventToAffectedReactionSetMap() {
			return eventToAffectedReactionSetMap;
		}

		/**
		 * @param eventToAffectedReactionSetMap the eventToAffectedReactionSetMap to set
		 */
		public void setEventToAffectedReactionSetMap(
				HashMap<String, HashSet<String>> eventToAffectedReactionSetMap) {
			this.eventToAffectedReactionSetMap = eventToAffectedReactionSetMap;
		}

		/**
		 * @return the ibiosimFunctionDefinitions
		 */
		public HashSet<String> getIbiosimFunctionDefinitions() {
			return ibiosimFunctionDefinitions;
		}

		/**
		 * @param ibiosimFunctionDefinitions the ibiosimFunctionDefinitions to set
		 */
		public void setIbiosimFunctionDefinitions(
				HashSet<String> ibiosimFunctionDefinitions) {
			this.ibiosimFunctionDefinitions = ibiosimFunctionDefinitions;
		}

		/**
		 * @return the propensity
		 */
		public double getPropensity() {
			return propensity;
		}

		/**
		 * @param propensity the propensity to set
		 */
		public void setPropensity(double propensity) {
			this.propensity = propensity;
		}

		/**
		 * @return the minPropensity
		 */
		public double getMinPropensity() {
			return minPropensity;
		}

		/**
		 * @param minPropensity the minPropensity to set
		 */
		public void setMinPropensity(double minPropensity) {
			this.minPropensity = minPropensity;
		}

		/**
		 * @return the maxPropensity
		 */
		public double getMaxPropensity() {
			return maxPropensity;
		}

		/**
		 * @param maxPropensity the maxPropensity to set
		 */
		public void setMaxPropensity(double maxPropensity) {
			this.maxPropensity = maxPropensity;
		}

		/**
		 * @return the noConstraintsFlag
		 */
		public boolean isNoConstraintsFlag() {
			return noConstraintsFlag;
		}

		/**
		 * @param noConstraintsFlag the noConstraintsFlag to set
		 */
		public void setNoConstraintsFlag(boolean noConstraintsFlag) {
			this.noConstraintsFlag = noConstraintsFlag;
		}

		/**
		 * @return the noRuleFlag
		 */
		public boolean isNoRuleFlag() {
			return noRuleFlag;
		}

		/**
		 * @param noRuleFlag the noRuleFlag to set
		 */
		public void setNoRuleFlag(boolean noRuleFlag) {
			this.noRuleFlag = noRuleFlag;
		}

		/**
		 * @return the triggeredEventQueue
		 */
		public PriorityQueue<HierarchicalEventToFire> getTriggeredEventQueue() {
			return triggeredEventQueue;
		}

		/**
		 * @param triggeredEventQueue the triggeredEventQueue to set
		 */
		public void setTriggeredEventQueue(
				PriorityQueue<HierarchicalEventToFire> triggeredEventQueue) {
			this.triggeredEventQueue = triggeredEventQueue;
		}

		/**
		 * @return the untriggeredEventSet
		 */
		public HashSet<String> getUntriggeredEventSet() {
			return untriggeredEventSet;
		}

		/**
		 * @param untriggeredEventSet the untriggeredEventSet to set
		 */
		public void setUntriggeredEventSet(HashSet<String> untriggeredEventSet) {
			this.untriggeredEventSet = untriggeredEventSet;
		}

		/**
		 * @return the variableToAffectedConstraintSetMap
		 */
		public HashMap<String, HashSet<ASTNode>> getVariableToAffectedConstraintSetMap() {
			return variableToAffectedConstraintSetMap;
		}

		/**
		 * @param variableToAffectedConstraintSetMap the variableToAffectedConstraintSetMap to set
		 */
		public void setVariableToAffectedConstraintSetMap(
				HashMap<String, HashSet<ASTNode>> variableToAffectedConstraintSetMap) {
			this.variableToAffectedConstraintSetMap = variableToAffectedConstraintSetMap;
		}

		/**
		 * @return the variableToIsInConstraintMap
		 */
		public HashMap<String, Boolean> getVariableToIsInConstraintMap() {
			return variableToIsInConstraintMap;
		}

		/**
		 * @param variableToIsInConstraintMap the variableToIsInConstraintMap to set
		 */
		public void setVariableToIsInConstraintMap(
				HashMap<String, Boolean> variableToIsInConstraintMap) {
			this.variableToIsInConstraintMap = variableToIsInConstraintMap;
		}

		/**
		 * @return the variableToIsInAssignmentRuleMap
		 */
		public HashMap<String, Boolean> getVariableToIsInAssignmentRuleMap() {
			return variableToIsInAssignmentRuleMap;
		}

		/**
		 * @param variableToIsInAssignmentRuleMap the variableToIsInAssignmentRuleMap to set
		 */
		public void setVariableToIsInAssignmentRuleMap(
				HashMap<String, Boolean> variableToIsInAssignmentRuleMap) {
			this.variableToIsInAssignmentRuleMap = variableToIsInAssignmentRuleMap;
		}

		/**
		 * @return the variableToIsInRateRuleMap
		 */
		public HashMap<String, Boolean> getVariableToIsInRateRuleMap() {
			return variableToIsInRateRuleMap;
		}

		/**
		 * @param variableToIsInRateRuleMap the variableToIsInRateRuleMap to set
		 */
		public void setVariableToIsInRateRuleMap(
				HashMap<String, Boolean> variableToIsInRateRuleMap) {
			this.variableToIsInRateRuleMap = variableToIsInRateRuleMap;
		}

		/**
		 * @return the variableToAffectedAssignmentRuleSetMap
		 */
		public HashMap<String, HashSet<AssignmentRule>> getVariableToAffectedAssignmentRuleSetMap() {
			return variableToAffectedAssignmentRuleSetMap;
		}

		/**
		 * @param variableToAffectedAssignmentRuleSetMap the variableToAffectedAssignmentRuleSetMap to set
		 */
		public void setVariableToAffectedAssignmentRuleSetMap(
				HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap) {
			this.variableToAffectedAssignmentRuleSetMap = variableToAffectedAssignmentRuleSetMap;
		}

		/**
		 * @return the nonconstantParameterIDSet
		 */
		public LinkedHashSet<String> getNonconstantParameterIDSet() {
			return nonconstantParameterIDSet;
		}

		/**
		 * @param nonconstantParameterIDSet the nonconstantParameterIDSet to set
		 */
		public void setNonconstantParameterIDSet(
				LinkedHashSet<String> nonconstantParameterIDSet) {
			this.nonconstantParameterIDSet = nonconstantParameterIDSet;
		}

		/**
		 * @return the reactionToNonconstantStoichiometriesSetMap
		 */
		public HashMap<String, HashSet<HierarchicalStringPair>> getReactionToNonconstantStoichiometriesSetMap() {
			return reactionToNonconstantStoichiometriesSetMap;
		}

		/**
		 * @param reactionToNonconstantStoichiometriesSetMap the reactionToNonconstantStoichiometriesSetMap to set
		 */
		public void setReactionToNonconstantStoichiometriesSetMap(
				HashMap<String, HashSet<HierarchicalStringPair>> reactionToNonconstantStoichiometriesSetMap) {
			this.reactionToNonconstantStoichiometriesSetMap = reactionToNonconstantStoichiometriesSetMap;
		}

		/**
		 * @return the compartmentIDSet
		 */
		public LinkedHashSet<String> getCompartmentIDSet() {
			return compartmentIDSet;
		}

		/**
		 * @param compartmentIDSet the compartmentIDSet to set
		 */
		public void setCompartmentIDSet(LinkedHashSet<String> compartmentIDSet) {
			this.compartmentIDSet = compartmentIDSet;
		}

		/**
		 * @return the rateRulesList
		 */
		public List<RateRule> getRateRulesList() {
			return rateRulesList;
		}

		/**
		 * @param rateRulesList the rateRulesList to set
		 */
		public void setRateRulesList(List<RateRule> rateRulesList) {
			this.rateRulesList = rateRulesList;
		}

		/**
		 * @return the nonConstantStoichiometry
		 */
		public HashSet<String> getNonConstantStoichiometry() {
			return nonConstantStoichiometry;
		}

		/**
		 * @param nonConstantStoichiometry the nonConstantStoichiometry to set
		 */
		public void setNonConstantStoichiometry(HashSet<String> nonConstantStoichiometry) {
			this.nonConstantStoichiometry = nonConstantStoichiometry;
		}

		/**
		 * @return the isHierarchical
		 */
		public HashSet<String> getIsHierarchical() {
			return isHierarchical;
		}

		/**
		 * @param isHierarchical the isHierarchical to set
		 */
		public void setIsHierarchical(HashSet<String> isHierarchical) {
			this.isHierarchical = isHierarchical;
		}

		/**
		 * @return the replacementDependency
		 */
		public HashMap<String, String> getReplacementDependency() {
			return replacementDependency;
		}

		/**
		 * @param replacementDependency the replacementDependency to set
		 */
		public void setReplacementDependency(
				HashMap<String, String> replacementDependency) {
			this.replacementDependency = replacementDependency;
		}

		/**
		 * @return the variablesToPrint
		 */
		public HashSet<String> getVariablesToPrint() {
			return variablesToPrint;
		}

		/**
		 * @param variablesToPrint the variablesToPrint to set
		 */
		public void setVariablesToPrint(HashSet<String> variablesToPrint) {
			this.variablesToPrint = variablesToPrint;
		}

		/**
		 * @return the deletedElementsById
		 */
		public HashSet<String> getDeletedElementsById() {
			return deletedElementsById;
		}

		/**
		 * @param deletedElementsById the deletedElementsById to set
		 */
		public void setDeletedElementsById(HashSet<String> deletedElementsById) {
			this.deletedElementsById = deletedElementsById;
		}

		/**
		 * @return the deletedElementsByMetaId
		 */
		public HashSet<String> getDeletedElementsByMetaId() {
			return deletedElementsByMetaId;
		}

		/**
		 * @param deletedElementsByMetaId the deletedElementsByMetaId to set
		 */
		public void setDeletedElementsByMetaId(HashSet<String> deletedElementsByMetaId) {
			this.deletedElementsByMetaId = deletedElementsByMetaId;
		}

		/**
		 * @return the deletedElementsByUId
		 */
		public HashSet<String> getDeletedElementsByUId() {
			return deletedElementsByUId;
		}

		/**
		 * @param deletedElementsByUId the deletedElementsByUId to set
		 */
		public void setDeletedElementsByUId(HashSet<String> deletedElementsByUId) {
			this.deletedElementsByUId = deletedElementsByUId;
		}
	}
	/**
	 * @return the filesCreated
	 */
	public ArrayList<String> getFilesCreated() {
		return filesCreated;
	}
	/**
	 * @return the initReplacementState
	 */
	public HashMap<String, Double> getInitReplacementState() {
		return initReplacementState;
	}
	/**
	 * @return the isGrid
	 */
	public boolean isGrid() {
		return isGrid;
	}
	/**
	 * @return the models
	 */
	public HashMap<String, Model> getModels() {
		return models;
	}
	/**
	 * @return the numSubmodels
	 */
	public int getNumSubmodels() {
		return numSubmodels;
	}
	/**
	 * @return the prng
	 */
	public PsRandom getPrng() {
		return prng;
	}
	/**
	 * @return the replacements
	 */
	public HashMap<String, Double> getReplacements() {
		return replacements;
	}
	/**
	 * @return the submodels
	 */
	public HashMap<String, ModelState> getSubmodels() {
		return submodels;
	}
	/**
	 * @return the topmodel
	 */
	public ModelState getTopmodel() {
		return topmodel;
	}
	/**
	 * @return the eventComparator
	 */
	public HierarchicalEventComparator getEventComparator() {
		if(eventComparator == null)
			eventComparator = new HierarchicalEventComparator();
		return eventComparator;
	}
	/**
	 * @param filesCreated the filesCreated to set
	 */
	public void setFilesCreated(ArrayList<String> filesCreated) {
		this.filesCreated = filesCreated;
	}
	/**
	 * @param initReplacementState the initReplacementState to set
	 */
	public void setInitReplacementState(HashMap<String, Double> initReplacementState) {
		this.initReplacementState = initReplacementState;
	}
	/**
	 * @param isGrid the isGrid to set
	 */
	public void setGrid(boolean isGrid) {
		this.isGrid = isGrid;
	}
	/**
	 * @param models the models to set
	 */
	public void setModels(HashMap<String, Model> models) {
		this.models = models;
	}
	/**
	 * @param numSubmodels the numSubmodels to set
	 */
	public void setNumSubmodels(int numSubmodels) {
		this.numSubmodels = numSubmodels;
	}
	/**
	 * @param prng the prng to set
	 */
	public void setPrng(PsRandom prng) {
		this.prng = prng;
	}
	/**
	 * @param replacements the replacements to set
	 */
	public void setReplacements(HashMap<String, Double> replacements) {
		this.replacements = replacements;
	}
	/**
	 * @param submodels the submodels to set
	 */
	public void setSubmodels(HashMap<String, ModelState> submodels) {
		this.submodels = submodels;
	}
	/**
	 * @param topmodel the topmodel to set
	 */
	public void setTopmodel(ModelState topmodel) {
		this.topmodel = topmodel;
	}
	/**
	 * @param eventComparator the eventComparator to set
	 */
	public void setEventComparator(HierarchicalEventComparator eventComparator) {
		this.eventComparator = eventComparator;
	}
	/**
	 * @return the randomNumberGenerator
	 */
	public XORShiftRandom getRandomNumberGenerator() {
		return randomNumberGenerator;
	}
	/**
	 * @param randomNumberGenerator the randomNumberGenerator to set
	 */
	public void setRandomNumberGenerator(XORShiftRandom randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}
}
