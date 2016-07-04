package analysis.dynamicsim.hierarchical.util.math;

public class ArrayDimensionNode extends VariableNode
{

	private VariableNode	size;

	public ArrayDimensionNode(String name, double value)
	{
		super(name, value);
	}

	public void setSize(VariableNode size)
	{
		this.size = size;
	}

	public VariableNode getSize()
	{
		return size;
	}

}
