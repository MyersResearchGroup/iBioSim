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
package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public interface ExpressionNode {
    public int evaluate(int[] stateVector);
	public void getVariables(HashSet<VarNode> variables);
	@Override
	public String toString();
	
	/**
     * Returns a copy of the top level node and all subsequent nodes.
     * Variable nodes are replaced with the VarNode indexed at it's name
     * in the variables HashMap, otherwise a new object is created with
     * the same attributes.  Constant nodes are not copied.
     * @param variables - HashMap of variable nodes keyed with their name
     * @return ExpressionNode - New ExpressionNode object
     */
	public ExpressionNode copy(HashMap<String, VarNode> variables);
}

