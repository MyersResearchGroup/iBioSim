package analysis.dynamicsim.hierarchical.util;

import java.util.Map;

import odk.lang.FastMath;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.states.ArraysState;

/**
 * This class is used to evaluate math functions.
 * 
 * @author Leandro Watanabe
 * 
 */
public final class Evaluator
{

	/**
	 * This is a math evaluator for ASTNode objects in the context of the
	 * hierarchical simulator.
	 * 
	 * @param modelstate
	 *            - the state of a certain model
	 * @param node
	 *            - the math to be evaluated
	 * @param evaluateState
	 *            - flag to indicate that math is to be evaluated in the context
	 *            of the y vector
	 * @param t
	 *            - current time
	 * @param y
	 *            - vector that is holding a temporary state
	 * @param variableToIndexMap
	 *            - maps variables to index in y
	 * @param replacements
	 *            - dependencies due to hierarchy
	 * @return the evaluated value
	 */
	public static double evaluateExpressionRecursive(ArraysState modelstate, ASTNode node, boolean evaluateState, double t, double[] y, Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		if (node.isBoolean())
		{
			return evaluateBoolean(modelstate, node, evaluateState, t, y, variableToIndexMap, replacements);
		}
		else if (node.isConstant())
		{
			return evaluateConstant(node);
		}
		else if (node.isInteger())
		{
			return evaluateInteger(node);
		}

		else if (node.isReal())
		{
			return evaluateReal(node);
		}
		else if (node.isName())
		{
			return evaluateName(modelstate, node, evaluateState, t, y, variableToIndexMap, replacements);
		}
		else if (node.isOperator())
		{
			return evaluateOperator(modelstate, node, evaluateState, t, y, variableToIndexMap, replacements);
		}
		else
		{
			return evaluateFunction(modelstate, node, evaluateState, t, y, variableToIndexMap, replacements);
		}
	}

	/*
	 * Evaluates boolean functions
	 */
	private static double evaluateBoolean(ArraysState modelstate, ASTNode node, boolean evaluateState, double t, double[] y, Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		switch (node.getType())
		{

		case CONSTANT_TRUE:
		{
			return 1.0;
		}
		case CONSTANT_FALSE:
		{
			return 0.0;
		}
		case LOGICAL_NOT:
		{
			return HierarchicalUtilities.getDoubleFromBoolean(!(HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements))));
		}
		case LOGICAL_AND:
		{

			boolean andResult = true;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				andResult = andResult && HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(andResult);
		}

		case LOGICAL_OR:
		{

			boolean orResult = false;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				orResult = orResult || HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(orResult);
		}

		case LOGICAL_XOR:
		{

			boolean xorResult = (node.getChildCount() == 0) ? false : HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements));

			for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
			{
				xorResult = xorResult ^ HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(xorResult);
		}

		case RELATIONAL_EQ:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs != rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}

		case RELATIONAL_NEQ:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs == rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}
		case RELATIONAL_GEQ:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs < rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}
		case RELATIONAL_LEQ:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs > rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}
		case RELATIONAL_GT:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs <= rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}
		case RELATIONAL_LT:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);

			for (int i = 1; i < node.getChildCount(); i++)
			{
				rhs = evaluateExpressionRecursive(modelstate, node.getChild(i), evaluateState, t, y, variableToIndexMap, replacements);

				if (lhs >= rhs)
				{
					return 0;
				}

				lhs = rhs;
			}

			return 1;
		}

		default:
			return 0.0;

		}
	}

	/*
	 * 
	 */
	private static double evaluateConstant(ASTNode node)
	{

		switch (node.getType())
		{

		case CONSTANT_E:
		{
			return Math.E;
		}
		case CONSTANT_PI:
		{
			return Math.PI;
		}
		}
		return 0;
	}

	private static double evaluateFunction(ArraysState modelstate, ASTNode node, boolean evaluateState, double t, double[] y, Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		switch (node.getType())
		{
		case FUNCTION:
		{
			// use node name to determine function
			// i'm not sure what to do with completely user-defined functions,
			// though
			String nodeName = node.getName();

			if (nodeName.equals("uniform"))
			{
				double leftChildValue = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double rightChildValue = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double lowerBound = Math.min(leftChildValue, rightChildValue);
				double upperBound = Math.max(leftChildValue, rightChildValue);
				UniformRealDistribution distrib = new UniformRealDistribution(lowerBound, upperBound);
				return distrib.sample();
			}
			else if (nodeName.equals("exponential"))
			{
				double mean = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				ExponentialDistribution distrib = new ExponentialDistribution(mean);
				return distrib.sample();
			}
			else if (nodeName.equals("gamma"))
			{
				double shape = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double scale = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);

				GammaDistribution distrib = new GammaDistribution(shape, scale);
				return distrib.sample();
			}
			else if (nodeName.equals("chisq"))
			{
				double deg = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				ChiSquaredDistribution distrib = new ChiSquaredDistribution(deg);
				return distrib.sample();
			}
			else if (nodeName.equals("lognormal"))
			{
				double scale = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double shape = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);
				LogNormalDistribution distrib = new LogNormalDistribution(scale, shape);
				return distrib.sample();
			}
			else if (nodeName.equals("laplace"))
			{
				double mu = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double beta = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);
				LaplaceDistribution distrib = new LaplaceDistribution(mu, beta);
				return distrib.sample();
			}
			else if (nodeName.equals("cauchy"))
			{
				double median = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double scale = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);
				CauchyDistribution distrib = new CauchyDistribution(median, scale);
				return distrib.sample();
			}
			else if (nodeName.equals("poisson"))
			{
				double p = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				PoissonDistribution distrib = new PoissonDistribution(p);
				return distrib.sample();
			}
			else if (nodeName.equals("binomial"))
			{
				int p = (int) evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double b = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);
				BinomialDistribution distrib = new BinomialDistribution(p, b);
				return distrib.sample();
			}
			else if (nodeName.equals("bernoulli"))
			{
				// TODO:
				return 0;
			}
			else if (nodeName.equals("normal"))
			{
				double mean = evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements);
				double std = evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements);

				NormalDistribution distrib = new NormalDistribution(mean, std);
				return distrib.sample();
			}
			else
			{
				return 0;
			}
		}

		case FUNCTION_ABS:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.abs(value);
		}
		case FUNCTION_ARCCOS:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.acos(value);
		}
		case FUNCTION_ARCSIN:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.asin(value);
		}
		case FUNCTION_ARCTAN:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.atan(value);
		}
		case FUNCTION_CEILING:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.ceil(value);
		}
		case FUNCTION_COS:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.cos(value);
		}
		case FUNCTION_COSH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.cosh(value);
		}
		case FUNCTION_EXP:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.exp(value);
		}
		case FUNCTION_FLOOR:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.floor(value);
		}
		case FUNCTION_LN:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.log(value);
		}
		case FUNCTION_LOG:
		{
			double base = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double var = evaluateExpressionRecursive(modelstate, node.getChild(1), evaluateState, t, y, variableToIndexMap, replacements);
			double value = Math.log(var) / Math.log(base);
			return value;
		}
		case FUNCTION_SIN:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.sin(value);
		}
		case FUNCTION_SINH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.sinh(value);
		}
		case FUNCTION_TAN:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.tan(value);
		}
		case FUNCTION_TANH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			return Math.tanh(value);
		}
		case FUNCTION_PIECEWISE:
		{

			int childIter = 0;
			for (; childIter < node.getChildCount() - 1; childIter += 2)
			{
				boolean condition = HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter + 1), evaluateState, t, y, variableToIndexMap, replacements));
				if (condition)
				{
					return evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements);
				}

			}
			if (node.getChildCount() % 2 == 1)
			{
				return evaluateExpressionRecursive(modelstate, node.getChild(node.getChildCount() - 1), evaluateState, t, y, variableToIndexMap, replacements);
			}

			return 0;
		}

		case FUNCTION_ROOT:
		{
			return FastMath.pow(evaluateExpressionRecursive(modelstate, node.getRightChild(), evaluateState, t, y, variableToIndexMap, replacements), 1 / evaluateExpressionRecursive(modelstate, node.getLeftChild(), evaluateState, t, y, variableToIndexMap, replacements));
		}
		case FUNCTION_SEC:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1 / Math.cos(value);
			return result;
		}
		case FUNCTION_SECH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1 / Math.cosh(value);
			return result;
		}
		case FUNCTION_FACTORIAL:
		{
			ASTNode leftChild = node.getLeftChild();
			int leftValue = (int) evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1;
			while (leftValue > 0)
			{
				result = result * leftValue;
				leftValue--;
			}
			return result;

		}
		case FUNCTION_COT:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1 / Math.tan(value);
			return result;
		}
		case FUNCTION_COTH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.sinh(value) / Math.cosh(value);
			return result;
		}
		case FUNCTION_CSC:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1 / Math.sin(value);
			return result;
		}
		case FUNCTION_CSCH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 1 / Math.sinh(value);
			return result;
		}
		case FUNCTION_DELAY:
		{
			// TODO:
			return 0;
		}
		case FUNCTION_ARCTANH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 0.5 * (Math.log(value + 1) - Math.log(1 - value));
			return result;
		}
		case FUNCTION_ARCSINH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.log(value + Math.sqrt(value * value + 1));
			return result;
		}
		case FUNCTION_ARCCOSH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.log(value + Math.sqrt(value + 1) * Math.sqrt(value - 1));
			return result;
		}
		case FUNCTION_ARCCOT:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.atan(1 / value);
			return result;
		}
		case FUNCTION_ARCCOTH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = 0.5 * (Math.log(1 + 1 / value) - Math.log(1 - 1 / value));
			return result;
		}
		case FUNCTION_ARCCSC:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.asin(1 / value);
			return result;
		}

		case FUNCTION_ARCCSCH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.log(1 / value + Math.sqrt(1 / (value * value) + 1));
			return result;
		}
		case FUNCTION_ARCSEC:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.acos(1 / value);
			return result;
		}

		case FUNCTION_ARCSECH:
		{
			double value = evaluateExpressionRecursive(modelstate, node.getChild(0), evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.log(1 / value + Math.sqrt(1 / value + 1) * Math.sqrt(1 / value - 1));
			return result;
		}
		case FUNCTION_POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			double leftValue = evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y, variableToIndexMap, replacements);
			double rightValue = evaluateExpressionRecursive(modelstate, rightChild, evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.pow(leftValue, rightValue);
			return result;

		}
		case FUNCTION_SELECTOR:
		{
			if (node.getChild(0).isName())
			{
				String id = "";
				id = node.getChild(0).getName();
				for (int childIter = 1; childIter < node.getChildCount(); childIter++)
				{
					id = id + "[" + (int) evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements) + "]";
				}

				return modelstate.getVariableToValue(replacements, id);
			}
			else if (node.getChild(0).isVector())
			{
				ASTNode vector = node.getChild(0);

				for (int childIter = 1; childIter < node.getChildCount(); childIter++)
				{
					vector = vector.getChild((int) evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements));
				}

				if (vector.isNumber())
				{
					return vector.getReal();
				}
				else if (vector.isName())
				{
					return modelstate.getVariableToValue(replacements, vector.getName());
				}

			}
			else
			{
				return 0;
			}
		}

		default:
			return 0.0;
		}
	}

	private static double evaluateInteger(ASTNode node)
	{
		return node.getInteger();
	}

	private static double evaluateName(ArraysState modelstate, ASTNode node, boolean evaluateState, double t, double[] y, Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		String name = node.getName();

		if (node.getType() == org.sbml.jsbml.ASTNode.Type.NAME_TIME)
		{
			return t;
		}
		else if (modelstate.getReactionToHasEnoughMolecules().containsKey(node.getName()))
		{
			return modelstate.getPropensity(replacements, node.getName());
		}
		else
		{

			double value = 0;
			String referencedVariable = HierarchicalUtilities.getReferencedVariable(name);

			if (evaluateState)
			{
				int i, j;

				i = variableToIndexMap.get(name);
				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(referencedVariable) && modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(referencedVariable) == false)
				{
					j = variableToIndexMap.get(modelstate.getSpeciesToCompartmentNameMap().get(name));
					value = y[i] / y[j];
				}
				else
				{
					value = y[i];
				}
				return value;
			}
			else
			{
				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(referencedVariable) && modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(referencedVariable) == false)
				{
					value = (modelstate.getVariableToValue(replacements, name) / modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(name)));
				}
				else if (variableToIndexMap != null && variableToIndexMap.containsKey(name))
				{
					value = variableToIndexMap.get(name);
				}
				else if (modelstate.getVariableToValueMap().containsKey(name))
				{
					value = modelstate.getVariableToValue(replacements, name);
				}
				else if (modelstate.getArrayVariableToValue().containsKey(referencedVariable))
				{
					value = modelstate.getVariableToValue(replacements, name);
				}
				return value;
			}
		}
	}

	private static double evaluateOperator(ArraysState modelstate, ASTNode node, boolean evaluateState, double t, double[] y, Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		switch (node.getType())
		{
		case PLUS:
		{

			double sum = 0.0;

			for (int childIter = 0; childIter < node.getChildCount(); childIter++)
			{
				sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements);
			}

			return sum;
		}

		case MINUS:
		{
			ASTNode leftChild = node.getLeftChild();

			double sum = evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y, variableToIndexMap, replacements);

			if (node.getNumChildren() == 1)
			{
				return -sum;
			}

			for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
			{
				sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements);
			}

			return sum;
		}

		case TIMES:
		{

			double product = 1.0;

			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter), evaluateState, t, y, variableToIndexMap, replacements);
			}

			return product;
		}

		case DIVIDE:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			double leftValue = evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y, variableToIndexMap, replacements);
			double rightValue = evaluateExpressionRecursive(modelstate, rightChild, evaluateState, t, y, variableToIndexMap, replacements);
			double result = leftValue / rightValue;
			return result;

		}
		case POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			double leftValue = evaluateExpressionRecursive(modelstate, leftChild, evaluateState, t, y, variableToIndexMap, replacements);
			double rightValue = evaluateExpressionRecursive(modelstate, rightChild, evaluateState, t, y, variableToIndexMap, replacements);
			double result = Math.pow(leftValue, rightValue);
			return result;

		}
		default:
			return 0.0;
		}
	}

	private static double evaluateReal(ASTNode node)
	{
		return node.getReal();
	}

}
