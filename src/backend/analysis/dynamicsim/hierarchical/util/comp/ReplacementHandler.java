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
package backend.analysis.dynamicsim.hierarchical.util.comp;

import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReplacementHandler
{
	private HierarchicalModel	fromModelState;
	private String		fromVariable;
	private HierarchicalModel	toModelState;
	private String		toVariable;

	public ReplacementHandler(HierarchicalModel fromModelState, String fromVariable, HierarchicalModel toModelState, String toVariable)
	{
		this.fromModelState = fromModelState;
		this.fromVariable = fromVariable;
		this.toModelState = toModelState;
		this.toVariable = toVariable;
	}

	public void copyNodeTo()
	{
		toModelState.addMappingNode(toVariable, fromModelState.getNode(fromVariable));
	}

	public HierarchicalModel getFromModelState()
	{
		return fromModelState;
	}

	public String getFromVariable()
	{
		return fromVariable;
	}

	public HierarchicalModel getToModelState()
	{
		return toModelState;
	}

	public String getToVariable()
	{
		return toVariable;
	}

	@Override
	public String toString()
	{
		return "copy " + fromVariable + " of " + fromModelState.getID() + " to " + toVariable + " of " + toModelState.getID();
	}

}
