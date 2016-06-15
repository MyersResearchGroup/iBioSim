package analysis.dynamicsim.hierarchical.util.math;

public class EventAssignmentNode
{

	private VariableNode		variable;
	private HierarchicalNode	math;

	public EventAssignmentNode(VariableNode variable, HierarchicalNode math)
	{
		this.variable = variable;
		this.math = math;
	}

	public VariableNode getVariable()
	{
		return variable;
	}

	public void setVariable(VariableNode variable)
	{
		this.variable = variable;
	}

	public HierarchicalNode getMath()
	{
		return math;
	}

	public void setMath(HierarchicalNode math)
	{
		this.math = math;
	}
}
