package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.List;

public class VariableNode extends ValueNode
{

	protected boolean			isVariableConstant;
	protected String			name;
	private List<ReactionNode>	reactionDependents;

	public VariableNode(String name, double value)
	{
		super(Type.NAME, value);
		this.name = name;
	}

	public VariableNode(VariableNode copy)
	{
		super(copy);
		this.name = copy.name;
		this.isVariableConstant = copy.isVariableConstant;
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
		double newValue = Evaluator.evaluateExpressionRecursive(initAssign, true);
		this.value = newValue;
		boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
		return !isNaN && oldValue != newValue;
	}

	public boolean computeAssignmentValue()
	{
		if (assignRule == null)
		{
			return false;
		}
		double oldValue = value;
		double newValue = Evaluator.evaluateExpressionRecursive(assignRule, true);
		this.value = newValue;
		boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
		return !isNaN && oldValue != newValue;
	}

	public double computeRateOfChange(double time)
	{
		if (rateRule == null)
		{
			return 0;
		}
		return Evaluator.evaluateExpressionRecursive(rateRule, false);
	}

	@Override
	public String toString()
	{
		return name + "=" + value;
	}

	@Override
	public VariableNode clone()
	{
		return new VariableNode(this);
	}
}
