package analysis.dynamicsim.hierarchical.util.math;


public class ValueNode extends HierarchicalNode
{
	protected double			value;

	protected HierarchicalNode	assignRule;
	protected HierarchicalNode	rateRule;
	protected HierarchicalNode	initAssign;

	protected ValueNode(Type type, double value)
	{
		super(type);
		this.value = value;
	}

	protected ValueNode(ValueNode copy)
	{
		super(copy);
		this.value = copy.value;
	}

	public ValueNode(double value)
	{
		this(Type.NUMBER, value);
	}

	public double getValue()
	{
		return value;
	}

	public void setValue(double value)
	{

		this.value = value;

	}

	public void setAssignmentRule(HierarchicalNode assignRule)
	{
		this.initAssign = null;
		this.assignRule = assignRule;
	}

	public HierarchicalNode getAssignmentRule()
	{
		return assignRule;
	}

	public void setRateRule(HierarchicalNode rateRule)
	{
		this.rateRule = rateRule;
	}

	public HierarchicalNode getRateRule()
	{
		return rateRule;
	}

	public void setInitialAssignment(HierarchicalNode initAssign)
	{
		this.initAssign = initAssign;
	}

	public HierarchicalNode getInitialAssignment()
	{
		return initAssign;
	}

	public boolean hasAssignmentRule()
	{
		return assignRule != null;
	}

	public boolean hasInitAssignment()
	{
		return initAssign != null;
	}

	@Override
	public String toString()
	{
		return String.valueOf(value);
	}
}
