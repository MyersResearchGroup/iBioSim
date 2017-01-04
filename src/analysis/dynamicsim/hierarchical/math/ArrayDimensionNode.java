package analysis.dynamicsim.hierarchical.math;

public class ArrayDimensionNode extends VariableNode
{

	private VariableNode	size;
	private String			sizeId;

	public ArrayDimensionNode(String name)
	{
		super(name);
	}

	public VariableNode getSize()
	{
		return size;
	}

	public void setSize(VariableNode size)
	{
		this.size = size;
	}

	public String getSizeId()
	{
		return sizeId;
	}

	public void setSizeId(String sizeId)
	{
		this.sizeId = sizeId;
	}

}