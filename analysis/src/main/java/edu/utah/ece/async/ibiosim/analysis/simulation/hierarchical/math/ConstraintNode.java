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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ConstraintNode extends HierarchicalNode
{

	private String	id;

	public ConstraintNode(String id, HierarchicalNode copy)
	{
		super(Type.PLUS);
		this.addChild(copy);
		this.id = id;
	}

	private ConstraintNode(ConstraintNode constraintNode)
	{
		super(constraintNode);
		this.id = constraintNode.id;
	}

	public String getName()
	{
		return id;
	}

	public double getNumberOfFailures(int index)
	{
		return state.getState(index).getStateValue();
	}

	public boolean evaluateConstraint(int index)
	{

		boolean value = Evaluator.evaluateExpressionRecursive(this, index) > 0;
		if (!value)
		{
			state.setStateValue(index, state.getStateValue(index) + 1);
		}
		return value;
	}

	@Override
	public ConstraintNode clone()
	{
		return new ConstraintNode(this);
	}
}
