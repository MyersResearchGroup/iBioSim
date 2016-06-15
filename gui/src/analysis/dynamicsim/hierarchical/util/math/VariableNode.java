package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.List;

public class VariableNode extends ValueNode
{

	private boolean				isVariableConstant;
	private String				name;
	private List<ReactionNode>	reactionDependents;

	public VariableNode(String name, double value)
	{
		super(Type.NAME, value);
		this.name = name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public List<ReactionNode> getReactionDependents()
	{
		return reactionDependents;
	}

	public void addReactionDependency(ReactionNode dependency)
	{
		if (reactionDependents == null)
		{
			reactionDependents = new ArrayList<ReactionNode>();
		}
		reactionDependents.add(dependency);
	}

	public void setIsVariableConstant(boolean isConstant)
	{
		this.isVariableConstant = isConstant;
	}

	public boolean isVariableConstant()
	{
		return isVariableConstant;
	}

	public boolean computeInitialValue()
	{
		if (initAssign == null)
		{
			return false;
		}

		double oldValue = value;
		double newValue = Evaluator.evaluateExpressionRecursive(initAssign);
		this.value = newValue;
		return oldValue != newValue;
	}

	public boolean computeAssignmentValue()
	{
		if (assignRule == null)
		{
			return false;
		}
		double oldValue = value;
		double newValue = Evaluator.evaluateExpressionRecursive(assignRule, false);
		this.value = newValue;

		return oldValue != newValue;
	}

	public double computeRateOfChange(double time)
	{
		if (rateRule == null)
		{
			return 0;
		}
		return Evaluator.evaluateExpressionRecursive(rateRule);
	}

	@Override
	public String toString()
	{
		return name + "=" + value;
	}

}
