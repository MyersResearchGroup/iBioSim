package analysis.dynamicsim.hierarchical.util.arrays;

public class ArraysObject
{
	private String	size;
	private int		arrayDim;

	public ArraysObject(String size, int arrayDim)
	{
		this.size = size;
		this.arrayDim = arrayDim;
	}

	public String getSize()
	{
		return size;
	}

	public void setSize(String size)
	{
		this.size = size;
	}

	public int getArrayDim()
	{
		return arrayDim;
	}

	public void setArrayDim(int arrayDim)
	{
		this.arrayDim = arrayDim;
	}
}
