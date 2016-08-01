package analysis.dynamicsim.hierarchical.util.math;

public class EventAssignmentNode extends HierarchicalNode
{

	private VariableNode	variable;

	public EventAssignmentNode(VariableNode variable, HierarchicalNode math)
	{
		super(math);
		this.variable = variable;
	}

	public EventAssignmentNode(HierarchicalNode math)
	{
		super(math);
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
