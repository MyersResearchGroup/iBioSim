package analysis.dynamicsim.hierarchical.util.arrays;

import java.util.ArrayList;
import java.util.List;

public class DimensionObject
{
	private List<ArraysObject>	dimensions;

	public DimensionObject()
	{
		dimensions = new ArrayList<ArraysObject>();
	}

	public void addDimension(String id, String size, int arrayDim)
	{
		dimensions.add(new ArraysObject(size, arrayDim));
	}

	public void addArraysObject(ArraysObject obj)
	{
		dimensions.add(obj);
	}

	public List<ArraysObject> getDimensions()
	{
		return dimensions;
	}

	public int getDimensionCount()
	{
		return dimensions.size();
	}
}
