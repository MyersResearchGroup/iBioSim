package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import analysis.dynamicsim.XORShiftRandom;
import analysis.dynamicsim.hierarchical.states.ArraysState;
import analysis.dynamicsim.hierarchical.util.HierarchicalEventToFire;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import flanagan.math.Fmath;

public abstract class HierarchicalObjects extends HierarchicalSimState
{

	private ArrayList<String>			filesCreated;
	private HashSet<String>				ibiosimFunctionDefinitions;
	private HashMap<String, Double>		initReplacementState;
	private boolean						isGrid;
	private HashMap<String, Model>		models;
	private int							numSubmodels;
	private XORShiftRandom				randomNumberGenerator;
	private HashMap<String, Double>		replacements;
	private HashMap<String, ModelState>	submodels;
	private ModelState					topmodel;

	protected static boolean checkGrid(Model model)
	{
		if (model.getCompartment("Grid") != null)
		{
			return true;
		}
		return false;
	}

	// EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the
	 * priority queue
	 */
	protected class HierarchicalEventComparator implements Comparator<HierarchicalEventToFire>
	{

		/**
		 * compares two events based on their fire times and priorities
		 */
		@Override
		public int compare(HierarchicalEventToFire event1, HierarchicalEventToFire event2)
		{

			if (event1.getFireTime() > event2.getFireTime())
			{
				return 1;
			}
			else if (event1.getFireTime() < event2.getFireTime())
			{
				return -1;
			}
			else
			{
				ModelState state1;
				ModelState state2;
				if (event1.getModelID().equals("topmodel"))
				{
					state1 = topmodel;
				}
				else
				{
					state1 = submodels.get(event1.getModelID());
				}
				if (event2.getModelID().equals("topmodel"))
				{
					state2 = topmodel;
				}
				else
				{
					state2 = submodels.get(event2.getModelID());
				}

				if (state1.getEventToPriorityMap().get(event1.getEventID()) == null)
				{
					if (state2.getEventToPriorityMap().get(event2.getEventID()) != null)
					{
						return -1;
					}
					if ((Math.random() * 100) > 50)
					{
						return -1;
					}
					return 1;
				}

				if (evaluateExpressionRecursive(state1,
						state1.getEventToPriorityMap().get(event1.getEventID()), false,
						getCurrentTime(), null, null) > evaluateExpressionRecursive(state2, state2
						.getEventToPriorityMap().get(event2.getEventID()), false, getCurrentTime(),
						null, null))
				{
					return -1;
				}
				else if (evaluateExpressionRecursive(state1,
						state1.getEventToPriorityMap().get(event1.getEventID()), false,
						getCurrentTime(), null, null) < evaluateExpressionRecursive(state2, state2
						.getEventToPriorityMap().get(event2.getEventID()), false, getCurrentTime(),
						null, null))
				{
					return 1;
				}
				else
				{
					if ((Math.random() * 100) > 50)
					{
						return -1;
					}
					return 1;
				}
			}
		}
	}

	protected class ModelState extends ArraysState
	{
		private HierarchicalEventComparator	eventComparator;

		public ModelState(HashMap<String, Model> models, String bioModel, String submodelID)
		{
			super(models, bioModel, submodelID);

			if (getNumEvents() > 0)
			{
				setTriggeredEventQueue(new PriorityQueue<HierarchicalEventToFire>(
						(int) getNumEvents(), getEventComparator()));
			}
		}

		/**
		 * @return the eventToPreviousTriggerValueMap
		 */
		public void addEventToPreviousTriggerValueMap(String id, boolean value)
		{
			getEventToPreviousTriggerValueMap().put(id, value);
		}

		public void clear()
		{
			getSpeciesToAffectedReactionSetMap().clear();
			getSpeciesToIsBoundaryConditionMap().clear();
			getVariableToIsConstantMap().clear();
			getSpeciesToHasOnlySubstanceUnitsMap().clear();
			getSpeciesToCompartmentNameMap().clear();
			getSpeciesIDSet().clear();
			getVariableToValueMap().clear();
			getReactionToPropensityMap().clear();
			getReactionToSpeciesAndStoichiometrySetMap().clear();
			getReactionToReactantStoichiometrySetMap().clear();
			getReactionToFormulaMap().clear();
			setNoConstraintsFlag(true);
			setPropensity(0.0);
			setMinPropensity(Double.MAX_VALUE / 10.0);
			setMaxPropensity(Double.MIN_VALUE / 10.0);
		}

		/**
		 * @return the eventComparator
		 */
		public HierarchicalEventComparator getEventComparator()
		{
			if (eventComparator == null)
			{
				eventComparator = new HierarchicalEventComparator();
			}
			return eventComparator;
		}

		/**
		 * @return the speciesToReplacement
		 */
		public HashSet<HierarchicalStringPair> getSpeciesToReplacement(String species)
		{
			if (getSpeciesToReplacement().get(species) == null)
			{
				getSpeciesToReplacement().put(species, new HashSet<HierarchicalStringPair>());
			}

			return getSpeciesToReplacement().get(species);
		}

		public double getVariableToValue(HashMap<String, Double> replacements, String variable)
		{
			if (getIsHierarchical().contains(variable))
			{
				String dep = getReplacementDependency().get(variable);
				return replacements.get(dep);
			}
			return getVariableToValueMap().get(variable);
		}

		public boolean isDeletedByMetaID(String metaid)
		{
			if (getDeletedElementsByMetaId().contains(metaid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public boolean isDeletedBySID(String sid)
		{
			if (getDeletedElementsById().contains(sid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public boolean isDeletedByUID(String uid)
		{
			if (getDeletedElementsByUId().contains(uid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		/**
		 * @param eventComparator
		 *            the eventComparator to set
		 */
		public void setEventComparator(HierarchicalEventComparator eventComparator)
		{
			this.eventComparator = eventComparator;
		}

		public void setvariableToValueMap(HashMap<String, Double> replacements, String variable,
				double value)
		{
			if (getIsHierarchical().contains(variable))
			{
				String dep = getReplacementDependency().get(variable);
				replacements.put(dep, value);
			}
			getVariableToValueMap().put(variable, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "ModelState [ID=" + getID() + "]";
		}

		/**
		 * @return the reactionToPropensityMap
		 */
		public void updateReactionToPropensityMap(String reaction, double value)
		{
			getReactionToPropensityMap().put(reaction, value);
		}
	}

	public HierarchicalObjects(String SBMLFileName, String rootDirectory, String outputDirectory,
			double timeLimit, double maxTimeStep, double minTimeStep, JProgressBar progress,
			double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);
		replacements = new HashMap<String, Double>();
		initReplacementState = new HashMap<String, Double>();
		models = new HashMap<String, Model>();
		SBMLDocument document = getDocument();
		isGrid = checkGrid(document.getModel());
		models.put(document.getModel().getId(), document.getModel().clone());
		filesCreated = new ArrayList<String>();

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
	}

	private double evaluateBoolean(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		switch (node.getType())
		{

		case CONSTANT_TRUE:
			return 1.0;

		case CONSTANT_FALSE:
			return 0.0;

		case LOGICAL_NOT:
			return HierarchicalUtilities.getDoubleFromBoolean(!(HierarchicalUtilities
					.getBooleanFromDouble(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap))));

		case LOGICAL_AND:
		{

			boolean andResult = true;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				andResult = andResult
						&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(
								modelstate, node.getChild(childIter), evaluateState, t, y,
								variableToIndexMap));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(andResult);
		}

		case LOGICAL_OR:
		{

			boolean orResult = false;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				orResult = orResult
						|| HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(
								modelstate, node.getChild(childIter), evaluateState, t, y,
								variableToIndexMap));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(orResult);
		}

		case LOGICAL_XOR:
		{

			boolean xorResult = (node.getChildCount() == 0) ? false : HierarchicalUtilities
					.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0),
							evaluateState, t, y, variableToIndexMap));

			for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
			{
				xorResult = xorResult
						^ HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(
								modelstate, node.getChild(childIter), evaluateState, t, y,
								variableToIndexMap));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(xorResult);
		}

		case RELATIONAL_EQ:
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) == evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));

		case RELATIONAL_NEQ:
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) != evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));

		case RELATIONAL_GEQ:
		{
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) >= evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));
		}
		case RELATIONAL_LEQ:
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) <= evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));

		case RELATIONAL_GT:
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) > evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));

		case RELATIONAL_LT:
		{
			return HierarchicalUtilities
					.getDoubleFromBoolean(evaluateExpressionRecursive(modelstate,
							node.getLeftChild(), evaluateState, t, y, variableToIndexMap) < evaluateExpressionRecursive(
							modelstate, node.getRightChild(), evaluateState, t, y,
							variableToIndexMap));
		}

		default:
			return 0.0;

		}
	}

	private double evaluateConstant(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{

		switch (node.getType())
		{

		case CONSTANT_E:
			return Math.E;

		case CONSTANT_PI:
			return Math.PI;

		default:
			return 0.0;
		}
	}

	protected double evaluateExpressionRecursive(ModelState modelstate, ASTNode node,
			boolean evaluateState, double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{

		if (node.isBoolean())
		{

			return evaluateBoolean(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}
		else if (node.isConstant())
		{
			return evaluateConstant(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}
		else if (node.isInteger())
		{
			return evaluateInteger(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}

		else if (node.isReal())
		{
			return evaluateReal(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}
		else if (node.isName())
		{
			return evaluateName(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}
		else if (node.isOperator())
		{
			return evaluateOperator(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}
		else if (node.isFunction())
		{
			return evaluateFunction(modelstate, node, evaluateState, t, y, variableToIndexMap);
		}

		return 0.0;
	}

	private double evaluateFunction(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		switch (node.getType())
		{
		case FUNCTION:
		{
			// use node name to determine function
			// i'm not sure what to do with completely user-defined functions,
			// though
			String nodeName = node.getName();

			// generates a uniform random number between the upper and lower
			// bound
			if (nodeName.equals("uniform"))
			{

				double leftChildValue = evaluateExpressionRecursive(modelstate,
						node.getLeftChild(), evaluateState, t, y, variableToIndexMap);
				double rightChildValue = evaluateExpressionRecursive(modelstate,
						node.getRightChild(), evaluateState, t, y, variableToIndexMap);
				double lowerBound = FastMath.min(leftChildValue, rightChildValue);
				double upperBound = FastMath.max(leftChildValue, rightChildValue);

				return getPrng().nextDouble(lowerBound, upperBound);
			}
			else if (nodeName.equals("exponential"))
			{

				return getPrng().nextExponential(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap), 1);
			}
			else if (nodeName.equals("gamma"))
			{

				return getPrng().nextGamma(
						1,
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap),
						evaluateExpressionRecursive(modelstate, node.getRightChild(),
								evaluateState, t, y, variableToIndexMap));
			}
			else if (nodeName.equals("chisq"))
			{

				return getPrng().nextChiSquare(
						(int) evaluateExpressionRecursive(modelstate, node.getLeftChild(),
								evaluateState, t, y, variableToIndexMap));
			}
			else if (nodeName.equals("lognormal"))
			{

				return getPrng().nextLogNormal(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap),
						evaluateExpressionRecursive(modelstate, node.getRightChild(),
								evaluateState, t, y, variableToIndexMap));
			}
			else if (nodeName.equals("laplace"))
			{

				// function doesn't exist in current libraries
				return 0;
			}
			else if (nodeName.equals("cauchy"))
			{

				return getPrng().nextLorentzian(
						0,
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap));
			}
			else if (nodeName.equals("poisson"))
			{

				return getPrng().nextPoissonian(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap));
			}
			else if (nodeName.equals("binomial"))
			{

				return getPrng().nextBinomial(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap),
						(int) evaluateExpressionRecursive(modelstate, node.getRightChild(),
								evaluateState, t, y, variableToIndexMap));
			}
			else if (nodeName.equals("bernoulli"))
			{

				return getPrng().nextBinomial(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap), 1);
			}
			else if (nodeName.equals("normal"))
			{

				return getPrng().nextGaussian(
						evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
								t, y, variableToIndexMap),
						evaluateExpressionRecursive(modelstate, node.getRightChild(),
								evaluateState, t, y, variableToIndexMap));
			}

			break;
		}

		case FUNCTION_ABS:
			return FastMath.abs(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCOS:
			return FastMath.acos(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCSIN:
			return FastMath.asin(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCTAN:
			return FastMath.atan(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_CEILING:
			return FastMath.ceil(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_COS:
			return FastMath.cos(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_COSH:
			return FastMath.cosh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_EXP:
			return FastMath.exp(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_FLOOR:
			return FastMath.floor(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_LN:
			return FastMath.log(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_LOG:
			double base = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));
			double var = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(1),
					evaluateState, t, y, variableToIndexMap));
			return var / base;

		case FUNCTION_SIN:

			return FastMath.sin(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_SINH:
			return FastMath.sinh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_TAN:
			return FastMath.tan(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_TANH:
			return FastMath.tanh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_PIECEWISE:
		{

			// loop through child triples
			// if child 1 is true, return child 0, else return child 2
			for (int childIter = 0; childIter < node.getChildCount(); childIter += 3)
			{

				if ((childIter + 1) < node.getChildCount()
						&& HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(
								modelstate, node.getChild(childIter + 1), evaluateState, t, y,
								variableToIndexMap)))
				{
					return evaluateExpressionRecursive(modelstate, node.getChild(childIter),
							evaluateState, t, y, variableToIndexMap);
				}
				else if ((childIter + 2) < node.getChildCount())
				{
					return evaluateExpressionRecursive(modelstate, node.getChild(childIter + 2),
							evaluateState, t, y, variableToIndexMap);
				}
			}

			return 0;
		}

		case FUNCTION_ROOT:
			return FastMath.pow(
					evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t,
							y, variableToIndexMap),
					1 / evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState,
							t, y, variableToIndexMap));

		case FUNCTION_SEC:
			return Fmath.sec(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_SECH:
			return Fmath.sech(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_FACTORIAL:
			return Fmath.factorial(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_COT:
			return Fmath.cot(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_COTH:
			return Fmath.coth(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_CSC:
			return Fmath.csc(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_CSCH:
			return Fmath.csch(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_DELAY:
			// NOT PLANNING TO SUPPORT THIS
			return 0;

		case FUNCTION_ARCTANH:
			return Fmath.atanh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCSINH:
			return Fmath.asinh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCOSH:
			return Fmath.acosh(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCOT:
			return Fmath.acot(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCOTH:
			return Fmath.acoth(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCSC:
			return Fmath.acsc(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));

		case FUNCTION_ARCCSCH:
		{
			double x = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t,
					y, variableToIndexMap);
			return FastMath.log(1 / x + FastMath.sqrt(1 + 1 / (x * x)));
		}
		// return Fmath.acsch(evaluateExpressionRecursive(modelstate,
		// node.getChild(0)));

		case FUNCTION_ARCSEC:
		{
			return Fmath.asec(evaluateExpressionRecursive(modelstate, node.getChild(0),
					evaluateState, t, y, variableToIndexMap));
		}

		case FUNCTION_ARCSECH:
		{
			double x = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t,
					y, variableToIndexMap);
			return FastMath.log((1 + FastMath.sqrt(1 - x * x)) / x);
		}

		case FUNCTION_POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			return (FastMath.pow(
					evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y,
							variableToIndexMap),
					evaluateExpressionRecursive(modelstate, rightChild, evaluateState, t, y,
							variableToIndexMap)));
		}

		default:
			return 0.0;

		}

		return 0;
	}

	private double evaluateInteger(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		return node.getInteger();
	}

	private double evaluateName(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		String name = node.getName().replace("_negative_", "-");

		if (node.getType() == org.sbml.jsbml.ASTNode.Type.NAME_TIME)
		{

			return t;
		}

		else if (modelstate.getReactionToPropensityMap().keySet().contains(node.getName()))
		{
			return modelstate.getReactionToPropensityMap().get(node.getName());
		}
		else
		{

			double value;

			if (evaluateState)
			{
				int i, j;
				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(name)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(name) == false)
				{
					// value = (modelstate.variableToValueMap.get(name) /
					// modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
					// value = (modelstate.getVariableToValue(name) /
					// modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(name)));
					i = variableToIndexMap.get(name);
					j = variableToIndexMap.get(modelstate.getSpeciesToCompartmentNameMap()
							.get(name));
					value = y[i] / y[j];
				}
				else
				{
					i = variableToIndexMap.get(name);
					value = y[i];
				}
				return value;
			}
			else
			{
				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(name)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(name) == false)
				{
					value = (modelstate.getVariableToValue(replacements, name) / modelstate
							.getVariableToValue(replacements, modelstate
									.getSpeciesToCompartmentNameMap().get(name)));
				}
				else
				{
					value = modelstate.getVariableToValue(replacements, name);
				}
				return value;
			}
		}
	}

	private double evaluateOperator(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		switch (node.getType())
		{
		case PLUS:
		{

			double sum = 0.0;

			for (int childIter = 0; childIter < node.getChildCount(); childIter++)
			{
				sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter),
						evaluateState, t, y, variableToIndexMap);
			}

			return sum;
		}

		case MINUS:
		{
			ASTNode leftChild = node.getLeftChild();
			double sum = evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y,
					variableToIndexMap);

			for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
			{
				sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter),
						evaluateState, t, y, variableToIndexMap);
			}

			return sum;
		}

		case TIMES:
		{

			double product = 1.0;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter),
						evaluateState, t, y, variableToIndexMap);
			}

			return product;
		}

		case DIVIDE:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			return (evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y,
					variableToIndexMap) / evaluateExpressionRecursive(modelstate, rightChild,
					evaluateState, t, y, variableToIndexMap));

		}
		case POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			return (FastMath.pow(
					evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y,
							variableToIndexMap),
					evaluateExpressionRecursive(modelstate, rightChild, evaluateState, t, y,
							variableToIndexMap)));
		}
		default:
			return 0.0;
		}
	}

	private double evaluateReal(ModelState modelstate, ASTNode node, boolean evaluateState,
			double t, double[] y, HashMap<String, Integer> variableToIndexMap)
	{
		return node.getReal();
	}

	/**
	 * @return the filesCreated
	 */
	public ArrayList<String> getFilesCreated()
	{
		return filesCreated;
	}

	/**
	 * @return the ibiosimFunctionDefinitions
	 */
	public HashSet<String> getIbiosimFunctionDefinitions()
	{
		return ibiosimFunctionDefinitions;
	}

	/**
	 * @return the initReplacementState
	 */
	public HashMap<String, Double> getInitReplacementState()
	{
		return initReplacementState;
	}

	protected ModelState getModel(String id)
	{
		if (id.equals("topmodel"))
		{
			return topmodel;
		}
		return submodels.get(id);
	}

	/**
	 * @return the models
	 */
	public HashMap<String, Model> getModels()
	{
		return models;
	}

	/**
	 * @return the numSubmodels
	 */
	public int getNumSubmodels()
	{
		return numSubmodels;
	}

	/**
	 * @return the randomNumberGenerator
	 */
	public XORShiftRandom getRandomNumberGenerator()
	{
		return randomNumberGenerator;
	}

	/**
	 * @return the replacements
	 */
	public HashMap<String, Double> getReplacements()
	{
		return replacements;
	}

	/**
	 * @return the submodels
	 */
	public HashMap<String, ModelState> getSubmodels()
	{
		return submodels;
	}

	/**
	 * @return the topmodel
	 */
	public ModelState getTopmodel()
	{
		return topmodel;
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ModelState modelstate, ASTNode formula)
	{

		if (formula.isFunction() == false || formula.isLeaf() == false)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(modelstate, formula.getChild(i)));// .clone()));
			}
		}

		if (formula.isFunction()
				&& models.get(modelstate.getModel()).getFunctionDefinition(formula.getName()) != null)
		{

			if (getIbiosimFunctionDefinitions().contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = models.get(modelstate.getModel())
					.getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < models.get(modelstate.getModel())
					.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(models.get(modelstate.getModel())
						.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);
				// if ((child.getLeftChild() == null && child.getRightChild() ==
				// null) && child.isName()) {
				if ((child.getChildCount() == 0) && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(),
							oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	/**
	 * @return the isGrid
	 */
	public boolean isGrid()
	{
		return isGrid;
	}

	protected HashSet<String> performAssignmentRules(ModelState modelstate,
			HashSet<AssignmentRule> affectedAssignmentRuleSet)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet)
		{

			String variable = assignmentRule.getVariable();

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (modelstate.getVariableToIsConstantMap().containsKey(variable)
					&& modelstate.getVariableToIsConstantMap().get(variable) == false
					|| modelstate.getVariableToIsConstantMap().containsKey(variable) == false)
			{

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
				{
					modelstate.setvariableToValueMap(
							replacements,
							variable,
							evaluateExpressionRecursive(modelstate, assignmentRule.getMath(),
									false, getCurrentTime(), null, null)
									* modelstate.getVariableToValue(replacements, modelstate
											.getSpeciesToCompartmentNameMap().get(variable)));
				}
				else
				{
					modelstate.setvariableToValueMap(
							replacements,
							variable,
							evaluateExpressionRecursive(modelstate, assignmentRule.getMath(),
									false, getCurrentTime(), null, null));
				}

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

	/**
	 * @param filesCreated
	 *            the filesCreated to set
	 */
	public void setFilesCreated(ArrayList<String> filesCreated)
	{
		this.filesCreated = filesCreated;
	}

	/**
	 * @param isGrid
	 *            the isGrid to set
	 */
	public void setGrid(boolean isGrid)
	{
		this.isGrid = isGrid;
	}

	/**
	 * @param ibiosimFunctionDefinitions
	 *            the ibiosimFunctionDefinitions to set
	 */
	public void setIbiosimFunctionDefinitions(HashSet<String> ibiosimFunctionDefinitions)
	{
		this.ibiosimFunctionDefinitions = ibiosimFunctionDefinitions;
	}

	/**
	 * @param initReplacementState
	 *            the initReplacementState to set
	 */
	public void setInitReplacementState(HashMap<String, Double> initReplacementState)
	{
		this.initReplacementState = initReplacementState;
	}

	/**
	 * @param models
	 *            the models to set
	 */
	public void setModels(HashMap<String, Model> models)
	{
		this.models = models;
	}

	/**
	 * @param numSubmodels
	 *            the numSubmodels to set
	 */
	public void setNumSubmodels(int numSubmodels)
	{
		this.numSubmodels = numSubmodels;
	}

	/**
	 * @param randomNumberGenerator
	 *            the randomNumberGenerator to set
	 */
	public void setRandomNumberGenerator(XORShiftRandom randomNumberGenerator)
	{
		this.randomNumberGenerator = randomNumberGenerator;
	}

	/**
	 * @param replacements
	 *            the replacements to set
	 */
	public void setReplacements(HashMap<String, Double> replacements)
	{
		this.replacements = replacements;
	}

	/**
	 * @param submodels
	 *            the submodels to set
	 */
	public void setSubmodels(HashMap<String, ModelState> submodels)
	{
		this.submodels = submodels;
	}

	/**
	 * @param topmodel
	 *            the topmodel to set
	 */
	public void setTopmodel(ModelState topmodel)
	{
		this.topmodel = topmodel;
	}
}
