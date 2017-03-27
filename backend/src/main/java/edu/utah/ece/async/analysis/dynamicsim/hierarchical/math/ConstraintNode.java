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
package edu.utah.ece.async.analysis.dynamicsim.hierarchical.math;

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
	private int		failures;

	public ConstraintNode(String id, HierarchicalNode copy)
	{
		super(Type.PLUS);
		this.addChild(copy);
		this.id = id;
		this.failures = 0;
	}

	private ConstraintNode(ConstraintNode constraintNode)
	{
		super(constraintNode);
		this.id = constraintNode.id;
		this.failures = constraintNode.failures;
	}

	public String getName()
	{
		return id;
	}

	public int getNumberOfFailures()
	{
		return failures;
	}

	public boolean evaluateConstraint(int index)
	{

		boolean value = Evaluator.evaluateExpressionRecursive(this, index) > 0;
		if (!value)
		{
			failures++;
		}
		return value;
	}

	@Override
	public ConstraintNode clone()
	{
		return new ConstraintNode(this);
	}
}
