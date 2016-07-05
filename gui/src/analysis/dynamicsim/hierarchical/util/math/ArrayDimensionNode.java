package analysis.dynamicsim.hierarchical.util.math;

public class ArrayDimensionNode extends VariableNode
{

	private VariableNode	size;
	private String			sizeId;

	public ArrayDimensionNode(String name)
	{
		super(name, 0);
	}

	public ArrayDimensionNode(String name, double value)
	{
		super(name, value);
	}

	public void setSizeId(String sizeId)
	{
		this.sizeId = sizeId;
	}

	public String getSizeId()
	{
		return sizeId;
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
