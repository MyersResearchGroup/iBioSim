package analysis.dynamicsim.hierarchical.util.setup;

import analysis.dynamicsim.hierarchical.util.math.Evaluator;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;

public class ConstraintNode extends HierarchicalNode
{

	private String	id;
	private int		failures;

	public ConstraintNode(String id, HierarchicalNode copy)
	{
		super(copy);
		this.id = id;
		this.failures = 0;
	}

	public String getName()
	{
		return id;
	}

	public int getNumberOfFailures()
	{
		return failures;
	}

	public boolean evaluateConstraint()
	{
		boolean value = Evaluator.evaluateExpressionRecursive(this) > 0;
		if (!value)
		{
			failures++;
		}
		return value;
	}
}
