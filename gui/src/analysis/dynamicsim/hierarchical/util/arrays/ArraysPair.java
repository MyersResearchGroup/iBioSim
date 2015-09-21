package analysis.dynamicsim.hierarchical.util.arrays;

public class ArraysPair
{
	private IndexObject		index;
	private DimensionObject	dim;

	public ArraysPair(IndexObject index, DimensionObject dim)
	{
		this.index = index;
		this.dim = dim;
	}

	public IndexObject getIndex()
	{
		return index;
	}

	public DimensionObject getDim()
	{
		return dim;
	}

}
