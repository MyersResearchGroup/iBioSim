package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayNode extends HierarchicalNode
{
	private List<ArrayDimensionNode>		dimensionList;
	private Map<String, VariableNode>		dimensionMap;
	private Map<String, HierarchicalNode>	indexMap;
	private ArraysType						arraysType;

	public static enum ArraysType
	{
		COMPARTMENT, PARAMETER, SPECIES, REACTION, ASSIGNRULE, RATERULE, REACTANT, PRODUCT, EVENT, EVENTASSIGNMENT, CONSTRAINT, INITASSIGNMENT
	};

	public ArrayNode(ArraysType arraysType)
	{
		super(Type.VECTOR);
		this.arraysType = arraysType;
	}

	public ArraysType getArraysType()
	{
		return arraysType;
	}

	public Map<String, VariableNode> getDimensionMap()
	{
		return dimensionMap;
	}

	public void addDimension(ArrayDimensionNode dimNode)
	{
		if (dimensionList == null)
		{
			dimensionList = new ArrayList<ArrayDimensionNode>();
		}
		if (dimensionMap == null)
		{
			dimensionMap = new HashMap<String, VariableNode>();
		}
		dimensionList.add(dimNode);
		dimensionMap.put(dimNode.getName(), dimNode);
	}

	public void addDimension(String dim)
	{

		ArrayDimensionNode dimNode = new ArrayDimensionNode(dim);
		addDimension(dimNode);
	}

	public void setDimensionValue(int arrayDim, int value)
	{
		if (arrayDim < dimensionList.size())
		{
			dimensionList.get(arrayDim).setValue(value);
		}
	}

	public void addIndex(String attribute, HierarchicalNode math)
	{
		if (indexMap == null)
		{
			indexMap = new HashMap<String, HierarchicalNode>();
		}
		indexMap.put(attribute, math);
	}

	public int getNumDimensions()
	{
		return dimensionList == null ? 0 : dimensionList.size();
	}

	public void setDimensionSize(int index, VariableNode size)
	{
		if (index < dimensionList.size())
		{
			dimensionList.get(index).setSize(size);
		}
	}

	public void setDimensionSizeId(int index, String sizeId)
	{
		if (index < dimensionList.size())
		{
			dimensionList.get(index).setSizeId(sizeId);
		}
	}

	public ArrayDimensionNode getArrayDimensionNode(int index)
	{
		if (index < dimensionList.size())
		{
			return dimensionList.get(index);
		}
		return null;
	}

	public VariableNode getDimensionSize(int index)
	{
		if (index < dimensionList.size())
		{
			return dimensionList.get(index).getSize();
		}
		return null;
	}

	public Map<String, HierarchicalNode> getIndexMap()
	{
		return indexMap;
	}

	public String getDimensionSizeId(int index)
	{
		if (index < dimensionList.size())
		{
			return dimensionList.get(index).getSizeId();
		}
		return null;
	}

	public ArrayDimensionNode getDimension(int index)
	{
		if (index < dimensionList.size())
		{
			return dimensionList.get(index);
		}
		return null;
	}

	public boolean hasIndexReference(String attribute)
	{
		return indexMap == null ? false : indexMap.containsKey(attribute);
	}

}
