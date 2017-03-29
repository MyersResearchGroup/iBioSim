/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.backend.analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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

	public void setDimensionValue(int index, int arrayDim, int value)
	{
		if (arrayDim < dimensionList.size())
		{
			dimensionList.get(arrayDim).setValue(index, value);
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
