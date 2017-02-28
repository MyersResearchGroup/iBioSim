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
public class ImplicationNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public ImplicationNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}

	@Override
	public int evaluate(int[] stateVector){
		if(LeftOperand.evaluate(stateVector) == 0 || RightOperand.evaluate(stateVector) != 0)
			return 1;
		
		return 0;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "->" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new ImplicationNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
