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
package main.java.edu.utah.ece.async.verification.platu.expression;

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
public class TernaryNode implements ExpressionNode {
	ExpressionNode Condition = null;
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public TernaryNode(ExpressionNode condition, ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.Condition = condition;
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] stateVector){
		if(this.Condition.evaluate(stateVector) != 0)
			return LeftOperand.evaluate(stateVector);
		
		return RightOperand.evaluate(stateVector);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return Condition + "?" + LeftOperand.toString() + ":" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new TernaryNode(this.Condition.copy(variables), this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
