package analysis.dynamicsim.hierarchical.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.interpreter.RateSplitterInterpreter;
import analysis.dynamicsim.hierarchical.util.math.ConstraintNode;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class HierarchicalUtilities
{

	public static final String		separator					= (File.separator.equals("\\")) ? "\\\\" : File.separator;

	public static final Set<String>	ibiosimFunctionDefinitions	= new HashSet<String>(Arrays.asList("uniform", "exponential", "gamma", "chisq", "lognormal", "laplace", "cauchy", "poisson", "binomial", "bernoulli", "normal"));

	public static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		ASTNode child;
		long size = node.getChildCount();

		if (node.getChildCount() == 0)
		{
			nodeChildrenList.add(node);
		}

		for (int i = 0; i < size; i++)
		{
			// TODO:check this
			child = node.getChild(i);
			nodeChildrenList.add(child);
			getAllASTNodeChildren(child, nodeChildrenList);
		}
	}

	public static boolean getBooleanFromDouble(double value)
	{

		if (value == 0.0)
		{
			return false;
		}
		return true;
	}

	public static double getDoubleFromBoolean(boolean value)
	{

		if (value == true)
		{
			return 1.0;
		}
		return 0.0;
	}

	public static int getPercentage(int totalRuns, int currentRun, double currentTime, double timeLimit)
	{

		if (totalRuns == 1)
		{
			double timePerc = currentTime / timeLimit;
			return (int) (timePerc * 100);
		}
		else
		{
			double runPerc = 1.0 * currentRun / totalRuns;
			return (int) (runPerc * 100);
		}
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	public static ASTNode inlineFormula(HierarchicalModel state, ASTNode formula, Model model)
	{
		// TODO: Avoid calling this method
		if (formula.isFunction() == false || formula.isOperator()/*
																 * ||
																 * formula.isLeaf
																 * () == false
																 */)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(state, formula.getChild(i), model));// .clone()));
			}
		}

		// TODO: need to check isOperator here because new ASTNode says they are
		// FUNCTIONS but we can't getName of operators.
		if (formula.isFunction() && !formula.isOperator() && model.getFunctionDefinition(formula.getName()) != null)
		{

			if (ibiosimFunctionDefinitions != null && ibiosimFunctionDefinitions.contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = model.getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			Map<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);
				if ((child.getChildCount() == 0) && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(), oldFormula.getChild(index));

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

	public static void replaceArgument(ASTNode formula, String bvar, ASTNode arg)
	{
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar))
			{
				formula.replaceChild(i, arg.clone());
			}
			else if (child.getChildCount() > 0)
			{
				replaceArgument(child, bvar, arg);
			}
		}
	}

	public static void replaceArgument(ASTNode formula, String bvar, int arg)
	{
		if (formula.isString() && formula.getName().equals(bvar))
		{
			formula.setValue(arg);
		}
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);

			replaceArgument(child, bvar, arg);

		}
	}

	public static void alterLocalParameter(ASTNode math, String id, String parameterID)
	{
		if (math.isName() && math.getName().equals(id))
		{
			// math.setVariable(null);
			math.setName(parameterID);
		}
		else
		{
			for (ASTNode child : math.getChildren())
			{
				alterLocalParameter(child, id, parameterID);
			}
		}
	}

	/**
	 * Calculate fixed-point of initial assignments
	 * 
	 * @param modelstate
	 * @param variables
	 * @param math
	 */
	public static void computeFixedPoint(List<VariableNode> variableNodes, List<ReactionNode> reactionNodes)
	{
		boolean changed = true;

		while (changed)
		{
			changed = false;
			if (variableNodes != null)
			{
				for (VariableNode node : variableNodes)
				{
					changed = changed | node.computeInitialValue();
					changed = changed | node.computeAssignmentValue();
				}
			}
			if (reactionNodes != null)
			{
				for (ReactionNode node : reactionNodes)
				{
					changed = changed | node.computePropensity();
				}
			}
		}

	}

	public static void computeAssignmentRules(List<VariableNode> nodes)
	{
		if (nodes == null)
		{
			return;
		}

		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (int i = 0; i < nodes.size(); i++)
			{
				VariableNode node = nodes.get(i);
				changed = changed | node.computeAssignmentValue();
			}
		}
	}

	public static void computeAssignmentRules(double[] state, List<VariableNode> nodes)
	{
		if (nodes == null)
		{
			return;
		}

		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (int i = 0; i < nodes.size(); i++)
			{
				VariableNode node = nodes.get(i);
				changed = changed | node.computeAssignmentValue();
				state[i] = node.getValue();
			}
		}
	}

	public static void triggerAndFireEvents(List<EventNode> eventList, PriorityQueue<EventNode> triggeredEventList, double time)
	{
		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (int i = eventList.size() - 1; i >= 0; i--)
			{
				EventNode event = eventList.get(i);
				if (event.computeEnabled(time))
				{
					eventList.remove(i);
					triggeredEventList.add(event);
				}
			}

			while (triggeredEventList != null && !triggeredEventList.isEmpty())
			{
				EventNode event = triggeredEventList.peek();

				if (event.getFireTime() <= time)
				{
					triggeredEventList.poll();
					event.fireEvent(time);
					eventList.add(event);
					changed = true;
				}
				else
				{
					break;
				}
			}
		}
	}

	public static void computeReactionPropensities(List<ReactionNode> reactionList)
	{
		for (ReactionNode reaction : reactionList)
		{
			reaction.computePropensity();
		}
	}

	public static ASTNode[] splitMath(ASTNode math)
	{
		ASTNode plus = new ASTNode(Type.PLUS);
		ASTNode minus = new ASTNode(Type.PLUS);
		ASTNode[] result = new ASTNode[] { plus, minus };
		List<ASTNode> nodes = RateSplitterInterpreter.parseASTNode(math);
		for (ASTNode node : nodes)
		{
			if (node.getType() == ASTNode.Type.MINUS)
			{
				minus.addChild(node.getChild(0));
			}
			else
			{
				plus.addChild(node);
			}
		}

		return plus.getChildCount() > 0 && minus.getChildCount() > 0 ? result : null;
	}

	public static boolean evaluateConstraints(List<ConstraintNode> listOfConstraints)
	{
		boolean hasSuccess = true;
		for (ConstraintNode constraintNode : listOfConstraints)
		{
			hasSuccess = hasSuccess && constraintNode.evaluateConstraint();
		}
		return hasSuccess;
	}
}
