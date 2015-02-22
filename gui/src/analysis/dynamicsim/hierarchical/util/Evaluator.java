package analysis.dynamicsim.hierarchical.util;

import java.util.Map;

import org.sbml.jsbml.ASTNode;

public class Evaluator
{

	public static double evaluate(ASTNode node, Map<String, Double> idToValue)
	{

		if (node.isBoolean())
		{
			return evaluateBoolean(node, idToValue);
		}
		else if (node.isConstant())
		{
			return evaluateConstant(node, idToValue);
		}
		else if (node.isInteger())
		{
			return evaluateInteger(node, idToValue);
		}

		else if (node.isReal())
		{
			return evaluateReal(node, idToValue);
		}
		else if (node.isName())
		{
			return evaluateName(node, idToValue);
		}
		else if (node.isOperator())
		{
			return evaluateOperator(node, idToValue);
		}
		else
		{
			return evaluateFunction(node, idToValue);
		}
	}

	private static double evaluateBoolean(ASTNode node, Map<String, Double> idToValue)
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

			double value = evaluate(node, idToValue);

			if (value == 1)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
		case LOGICAL_AND:
		{
			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				double value = evaluate(node.getChild(childIter), idToValue);

				if (value == 0)
				{
					return 0;
				}
			}

			return 1;
		}

		case LOGICAL_OR:
		{
			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				double value = evaluate(node.getChild(childIter), idToValue);

				if (value == 1)
				{
					return 1;
				}
			}

			return 0;
		}

		case LOGICAL_XOR:
		{
			int count_0 = 0;
			int count_1 = 0;
			for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
			{
				double value = evaluate(node, idToValue);

				if (value == 0)
				{
					count_0++;
				}
				else
				{
					count_1++;
				}
			}

			return count_0 == count_1 ? 0 : 1;
		}

		case RELATIONAL_EQ:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left == right ? 1 : 0;
		}
		case RELATIONAL_NEQ:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left != right ? 1 : 0;
		}

		case RELATIONAL_GEQ:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left >= right ? 1 : 0;
		}
		case RELATIONAL_LEQ:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left <= right ? 1 : 0;
		}

		case RELATIONAL_GT:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left > right ? 1 : 0;
		}

		case RELATIONAL_LT:
		{
			double left = evaluate(node.getLeftChild(), idToValue);
			double right = evaluate(node.getRightChild(), idToValue);
			return left < right ? 1 : 0;
		}

		default:
			return 0.0;

		}
	}

	private static double evaluateConstant(ASTNode node, Map<String, Double> idToValue)
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
			return 0.0;
		}
	}

	private static double evaluateFunction(ASTNode node, Map<String, Double> idToValue)
	{
		switch (node.getType())
		{
		// TODO: distribution

		case FUNCTION_ABS:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.abs(value);
		}
		case FUNCTION_ARCCOS:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.acos(value);
		}
		case FUNCTION_ARCSIN:
		{
			double value = evaluate(node, idToValue);
			return Math.asin(value);
		}
		case FUNCTION_ARCTAN:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.atan(value);
		}
		case FUNCTION_CEILING:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.ceil(value);
		}
		case FUNCTION_COS:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.cos(value);
		}
		case FUNCTION_COSH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.cosh(value);
		}
		case FUNCTION_EXP:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.exp(value);
		}
		case FUNCTION_FLOOR:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.floor(value);
		}
		case FUNCTION_LN:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.log(value);
		}
		case FUNCTION_LOG:
		{

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			double LHS = evaluate(leftChild, idToValue);
			double RHS = evaluate(rightChild, idToValue);

			double base = Math.log(LHS);
			double var = Math.log(RHS);
			return var / base;
		}
		case FUNCTION_SIN:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.sin(value);
		}
		case FUNCTION_SINH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.sinh(value);
		}
		case FUNCTION_TAN:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.tan(value);
		}
		case FUNCTION_TANH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.tanh(value);
		}
		case FUNCTION_PIECEWISE:
		{

			for (int childIter = 0; childIter < node.getChildCount(); childIter += 3)
			{

				// TODO;
			}

			return 0;
		}

		case FUNCTION_ROOT:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.pow(value, 0.5);
		}

		case FUNCTION_SEC:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 1 / Math.cos(value);
		}

		case FUNCTION_SECH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 1 / Math.cosh(value);
		}
		case FUNCTION_FACTORIAL:
		{
			double value = evaluate(node, idToValue);
			return Math.abs(value);
		}
		case FUNCTION_COT:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return Math.tan(value);
		}
		case FUNCTION_COTH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 1 / Math.tanh(value);
		}
		case FUNCTION_CSC:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 1 / Math.sin(value);
		}
		case FUNCTION_CSCH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 1 / Math.sinh(value);
		}
		case FUNCTION_DELAY:
			// NOT PLANNING TO SUPPORT THIS
			return 0;

		case FUNCTION_ARCTANH:
		{
			double value = evaluate(node.getChild(0), idToValue);
			return 0.5 * (Math.log(1 + value) - Math.log(1 - value));
		}
		case FUNCTION_ARCSINH:
		{
			// TODO
		}
		case FUNCTION_ARCCOSH:
		{
			// TODO
		}
		case FUNCTION_ARCCOT:
		{
			// TODO
		}
		case FUNCTION_ARCCOTH:
		{
			// TODO
		}
		case FUNCTION_ARCCSC:
		{
			// TODO
		}
		case FUNCTION_ARCCSCH:
		{
			// TODO
		}
		// return Fmath.acsch(evaluateExpressionRecursive(modelstate,
		// node.getChild(0)));

		case FUNCTION_ARCSEC:
		{
			// TODO
		}

		case FUNCTION_ARCSECH:
		{
			// TODO
		}
		case FUNCTION_POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			double LHS = evaluate(leftChild, idToValue);
			double RHS = evaluate(rightChild, idToValue);

			return Math.pow(LHS, RHS);
		}

		default:
			return 0.0;

		}

	}

	private static double evaluateInteger(ASTNode node, Map<String, Double> idToValue)
	{
		return node.getInteger();
	}

	private static double evaluateName(ASTNode node, Map<String, Double> idToValue)
	{
		if (idToValue.containsKey(node.getName()))
		{
			return idToValue.get(node.getName());
		}

		return 0;
	}

	private static double evaluateOperator(ASTNode node, Map<String, Double> idToValue)
	{
		switch (node.getType())
		{
		case PLUS:
		{

			double sum = 0.0;
			int numChildren = node.getChildCount();

			for (int childIter = 0; childIter < numChildren; childIter++)
			{
				double value = evaluate(node.getChild(childIter), idToValue);
				sum = sum + value;
			}

			return sum;
		}

		case MINUS:
		{
			int numChildren = node.getChildCount();
			double sum = evaluate(node.getLeftChild(), idToValue);

			for (int childIter = 1; childIter < numChildren; ++childIter)
			{
				double value = evaluate(node.getChild(childIter), idToValue);
				sum = sum - value;
			}

			return sum;
		}

		case TIMES:
		{

			int numChildren = node.getChildCount();
			double product = 1.0;

			for (int childIter = 0; childIter < numChildren; ++childIter)
			{
				double value = evaluate(node.getChild(childIter), idToValue);
				product = product * value;
			}

			return product;
		}

		case DIVIDE:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			double LHS = evaluate(leftChild, idToValue);
			double RHS = evaluate(rightChild, idToValue);

			return LHS / RHS;

		}
		case POWER:
		{
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			double LHS = evaluate(leftChild, idToValue);
			double RHS = evaluate(rightChild, idToValue);

			return Math.pow(LHS, RHS);
		}
		default:
			return 0.0;
		}
	}

	private static double evaluateReal(ASTNode node, Map<String, Double> idToValue)
	{
		return node.getReal();
	}
}
