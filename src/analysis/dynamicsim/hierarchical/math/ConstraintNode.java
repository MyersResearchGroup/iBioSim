package analysis.dynamicsim.hierarchical.math;

public class ConstraintNode extends HierarchicalNode
{

	private String	id;
	private int		failures;

	public ConstraintNode(String id, HierarchicalNode copy)
	{
		super(Type.PLUS);
		this.addChild(copy);
		this.id = id;
		this.failures = 0;
	}

	private ConstraintNode(ConstraintNode constraintNode)
	{
		super(constraintNode);
		this.id = constraintNode.id;
		this.failures = constraintNode.failures;
	}

	public String getName()
	{
		return id;
	}

	public int getNumberOfFailures()
	{
		return failures;
	}

	public boolean evaluateConstraint(int index)
	{

		boolean value = Evaluator.evaluateExpressionRecursive(this, index) > 0;
		if (!value)
		{
			failures++;
		}
		return value;
	}

	@Override
	public ConstraintNode clone()
	{
		return new ConstraintNode(this);
	}
}
