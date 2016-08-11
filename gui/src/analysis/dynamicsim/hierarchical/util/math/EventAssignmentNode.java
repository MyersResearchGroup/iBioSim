package analysis.dynamicsim.hierarchical.util.math;

public class EventAssignmentNode extends HierarchicalNode
{

	private VariableNode	variable;

	public EventAssignmentNode(VariableNode variable, HierarchicalNode math)
	{
		this(math);
		this.variable = variable;
	}

	public EventAssignmentNode(Type type)
	{
		super(type);
	}

	public EventAssignmentNode(HierarchicalNode math)
	{
		super(Type.PLUS);
		this.addChild(math);
	}

	public EventAssignmentNode(EventAssignmentNode math)
	{
		super(math);
		this.variable = math.variable;
	}

	public VariableNode getVariable()
	{
		return variable;

	}

	public void setVariable(VariableNode variable)
	{
		this.variable = variable;
	}
}
