package analysis.dynamicsim.hierarchical.util.math;

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

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

/**
 * This class is used to evaluate math functions.
 * 
 * @author Leandro Watanabe
 * 
 */
public final class Evaluator
{

	public static HierarchicalNode evaluateArraysSelector(ModelState modelstate, HierarchicalNode node)
	{
		if (node.getNumOfChild() > 0)
		{
			HierarchicalNode child = node.getChild(0).getArrayNode();
			for (int i = node.getNumOfChild() - 1; i >= 1; i--)
			{
				int index = (int) evaluateExpressionRecursive(node.getChild(i));
				child = child.getChild(index);
			}

			return child;
		}
		return null;
	}

	public static double evaluateExpressionRecursive(HierarchicalNode node)
	{
		return evaluateExpressionRecursive(node, true);
	}

	/**
	 * This is a math evaluator for HierarchicalNode objects in the context of
	 * the hierarchical simulator.
	 * 
	 * @param checkSubstance
	 *            TODO
	 * 
	 */
	public static double evaluateExpressionRecursive(HierarchicalNode node, boolean checkSubstance)
	{
		if (node.isBoolean())
		{
			return evaluateBoolean(node, checkSubstance);
		}
		else if (node.isConstant())
		{
			return evaluateConstant(node);
		}
		else if (node.isNumber())
		{
			return evaluateNumber(node);
		}
		else if (node.isName())
		{
			return evaluateName(node, checkSubstance);
		}
		else if (node.isOperator())
		{
			return evaluateOperator(node, checkSubstance);
		}
		else
		{
			return evaluateFunction(node, checkSubstance);
		}
	}

	/*
	 * Evaluates boolean functions
	 */
	private static double evaluateBoolean(HierarchicalNode node, boolean checkSubstance)
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
			double value = HierarchicalUtilities.getDoubleFromBoolean(!(HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(0), checkSubstance))));
			return value;
		}
		case LOGICAL_AND:
		{

			boolean andResult = true;

			for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
			{
				andResult = andResult && HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter), checkSubstance));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(andResult);
		}

		case LOGICAL_OR:
		{

			boolean orResult = false;

			for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
			{
				orResult = orResult || HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter), checkSubstance));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(orResult);
		}

		case LOGICAL_XOR:
		{

			boolean xorResult = (node.getNumOfChild() == 0) ? false : HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(0), checkSubstance));

			for (int childIter = 1; childIter < node.getNumOfChild(); ++childIter)
			{
				xorResult = xorResult ^ HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter), checkSubstance));
			}

			return HierarchicalUtilities.getDoubleFromBoolean(xorResult);
		}

		case RELATIONAL_EQ:
		{
			double lhs, rhs;
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
			lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance);

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
	private static double evaluateConstant(HierarchicalNode node)
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
		default:
			return 0;
		}
	}

	private static double evaluateFunction(HierarchicalNode node, boolean checkSubstance)
	{
		switch (node.getType())
		{
		case FUNCTION:
		{
			String nodeName = ((VariableNode) node).getName();

			if (nodeName.equals("uniform"))
			{
				double leftChildValue = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double rightChildValue = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
				double lowerBound = Math.min(leftChildValue, rightChildValue);
				double upperBound = Math.max(leftChildValue, rightChildValue);
				UniformRealDistribution distrib = new UniformRealDistribution(lowerBound, upperBound);
				return distrib.sample();
			}
			else if (nodeName.equals("exponential"))
			{
				double mean = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				ExponentialDistribution distrib = new ExponentialDistribution(mean);
				return distrib.sample();
			}
			else if (nodeName.equals("gamma"))
			{
				double shape = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double scale = evaluateExpressionRecursive(node.getChild(1), checkSubstance);

				GammaDistribution distrib = new GammaDistribution(shape, scale);
				return distrib.sample();
			}
			else if (nodeName.equals("chisq"))
			{
				double deg = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				ChiSquaredDistribution distrib = new ChiSquaredDistribution(deg);
				return distrib.sample();
			}
			else if (nodeName.equals("lognormal"))
			{
				double scale = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double shape = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
				LogNormalDistribution distrib = new LogNormalDistribution(scale, shape);
				return distrib.sample();
			}
			else if (nodeName.equals("laplace"))
			{
				double mu = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double beta = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
				LaplaceDistribution distrib = new LaplaceDistribution(mu, beta);
				return distrib.sample();
			}
			else if (nodeName.equals("cauchy"))
			{
				double median = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double scale = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
				CauchyDistribution distrib = new CauchyDistribution(median, scale);
				return distrib.sample();
			}
			else if (nodeName.equals("poisson"))
			{
				double p = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				PoissonDistribution distrib = new PoissonDistribution(p);
				return distrib.sample();
			}
			else if (nodeName.equals("binomial"))
			{
				int p = (int) evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double b = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
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
				double mean = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
				double std = evaluateExpressionRecursive(node.getChild(1), checkSubstance);

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
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.abs(value);
		}
		case FUNCTION_ARCCOS:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.acos(value);
		}
		case FUNCTION_ARCSIN:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.asin(value);
		}
		case FUNCTION_ARCTAN:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.atan(value);
		}
		case FUNCTION_CEILING:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.ceil(value);
		}
		case FUNCTION_COS:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.cos(value);
		}
		case FUNCTION_COSH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.cosh(value);
		}
		case FUNCTION_EXP:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.exp(value);
		}
		case FUNCTION_FLOOR:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.floor(value);
		}
		case FUNCTION_LN:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.log(value);
		}
		case FUNCTION_LOG:
		{
			double base = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double var = evaluateExpressionRecursive(node.getChild(1), checkSubstance);
			double value = Math.log(var) / Math.log(base);
			return value;
		}
		case FUNCTION_SIN:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.sin(value);
		}
		case FUNCTION_SINH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.sinh(value);
		}
		case FUNCTION_TAN:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.tan(value);
		}
		case FUNCTION_TANH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			return Math.tanh(value);
		}
		case FUNCTION_PIECEWISE:
		{

			int childIter = 0;
			for (; childIter < node.getNumOfChild() - 1; childIter += 2)
			{
				boolean condition = HierarchicalUtilities.getBooleanFromDouble(evaluateExpressionRecursive(node.getChild(childIter + 1), checkSubstance));
				if (condition)
				{
					return evaluateExpressionRecursive(node.getChild(childIter), checkSubstance);
				}

			}
			if (node.getNumOfChild() % 2 == 1)
			{
				return evaluateExpressionRecursive(node.getChild(node.getNumOfChild() - 1), checkSubstance);
			}

			return 0;
		}

		case FUNCTION_ROOT:
		{
			return FastMath.pow(evaluateExpressionRecursive(node.getChild(1), checkSubstance), 1 / evaluateExpressionRecursive(node.getChild(0), checkSubstance));
		}
		case FUNCTION_SEC:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 1 / Math.cos(value);
			return result;
		}
		case FUNCTION_SECH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 1 / Math.cosh(value);
			return result;
		}
		case FUNCTION_FACTORIAL:
		{
			HierarchicalNode leftChild = node.getChild(0);
			int leftValue = (int) evaluateExpressionRecursive(leftChild, checkSubstance);
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
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 1 / Math.tan(value);
			return result;
		}
		case FUNCTION_COTH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.sinh(value) / Math.cosh(value);
			return result;
		}
		case FUNCTION_CSC:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 1 / Math.sin(value);
			return result;
		}
		case FUNCTION_CSCH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
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
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 0.5 * (Math.log(value + 1) - Math.log(1 - value));
			return result;
		}
		case FUNCTION_ARCSINH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.log(value + Math.sqrt(value * value + 1));
			return result;
		}
		case FUNCTION_ARCCOSH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.log(value + Math.sqrt(value + 1) * Math.sqrt(value - 1));
			return result;
		}
		case FUNCTION_ARCCOT:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.atan(1 / value);
			return result;
		}
		case FUNCTION_ARCCOTH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = 0.5 * (Math.log(1 + 1 / value) - Math.log(1 - 1 / value));
			return result;
		}
		case FUNCTION_ARCCSC:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.asin(1 / value);
			return result;
		}

		case FUNCTION_ARCCSCH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.log(1 / value + Math.sqrt(1 / (value * value) + 1));
			return result;
		}
		case FUNCTION_ARCSEC:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.acos(1 / value);
			return result;
		}

		case FUNCTION_ARCSECH:
		{
			double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance);
			double result = Math.log(1 / value + Math.sqrt(1 / value + 1) * Math.sqrt(1 / value - 1));
			return result;
		}
		case FUNCTION_POWER:
		{
			HierarchicalNode leftChild = node.getChild(0);
			HierarchicalNode rightChild = node.getChild(1);
			double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance);
			double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance);
			double result = Math.pow(leftValue, rightValue);
			return result;

		}
		case FUNCTION_SELECTOR:
		{
			HierarchicalNode array = node.getChild(0);

			for (int i = 1; i < node.getNumOfChild(); i++)
			{
				int index = (int) evaluateExpressionRecursive(node.getChild(i), checkSubstance);
				array = array.getChild(index);
			}

			return ((VariableNode) array).getValue();
		}

		default:
			return 0.0;
		}
	}

	private static double evaluateNumber(HierarchicalNode node)
	{
		return ((ValueNode) node).getValue();
	}

	private static double evaluateName(HierarchicalNode node, boolean checkSubstance)
	{
		if (node.isSpecies() && checkSubstance)
		{
			SpeciesNode species = (SpeciesNode) node;

			double value = 0;

			if (!species.hasOnlySubstance())
			{
				value = species.getConcentration();
			}
			else
			{
				value = species.getValue();
			}

			return value;
		}
		else if (node.isReaction())
		{
			ReactionNode reaction = (ReactionNode) node;

			if (reaction.hasEnoughMolecules())
			{
				return reaction.getValue();
			}
		}

		return ((VariableNode) node).getValue();
	}

	private static double evaluateOperator(HierarchicalNode node, boolean checkSubstance)
	{
		double result;
		switch (node.getType())
		{
		case PLUS:
		{

			result = 0.0;

			for (int childIter = 0; childIter < node.getNumOfChild(); childIter++)
			{
				result += evaluateExpressionRecursive(node.getChild(childIter), checkSubstance);
			}

			break;
		}

		case MINUS:
		{
			HierarchicalNode leftChild = node.getChild(0);

			result = evaluateExpressionRecursive(leftChild, checkSubstance);

			if (node.getNumOfChild() == 1)
			{
				result = -result;
			}
			else
			{
				for (int childIter = 1; childIter < node.getNumOfChild(); ++childIter)
				{
					result -= evaluateExpressionRecursive(node.getChild(childIter), checkSubstance);
				}
			}
			break;
		}

		case TIMES:
		{

			result = 1.0;

			for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
			{
				result *= evaluateExpressionRecursive(node.getChild(childIter), checkSubstance);
			}

			break;
		}

		case DIVIDE:
		{
			HierarchicalNode leftChild = node.getChild(0);
			HierarchicalNode rightChild = node.getChild(1);
			double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance);
			double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance);
			result = leftValue / rightValue;
			break;
		}
		case POWER:
		{
			HierarchicalNode leftChild = node.getChild(0);
			HierarchicalNode rightChild = node.getChild(1);
			double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance);
			double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance);
			result = Math.pow(leftValue, rightValue);
			break;

		}
		default:
			result = 0.0;
		}

		return result;

	}

}
