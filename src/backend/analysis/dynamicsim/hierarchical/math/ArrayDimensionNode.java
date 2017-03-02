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
package backend.analysis.dynamicsim.hierarchical.math;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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