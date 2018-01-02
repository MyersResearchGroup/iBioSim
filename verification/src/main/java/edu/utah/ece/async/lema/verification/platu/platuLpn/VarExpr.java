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
package edu.utah.ece.async.lema.verification.platu.platuLpn;

import java.util.HashMap;

import edu.utah.ece.async.lema.verification.platu.expression.Expression;
import edu.utah.ece.async.lema.verification.platu.expression.VarNode;

/**
 * Assignment data structure.
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VarExpr {
    private VarNode var;
    private Expression expr;

    public VarExpr(VarNode var, Expression expr){
    	this.var = var;
    	this.expr = expr;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VarExpr) {
            VarExpr other = (VarExpr) obj;
            return getVar() == other.getVar() && expr.toString().compareTo(other.expr.toString()) == 0;
        }
        
        return false;
    }

    public VarExpr copy(HashMap<String, VarNode> variables){
    	return new VarExpr((VarNode) this.var.copy(variables), this.expr.copy(variables));
    }

    /**
     * @return The variable assigned.
     */
    public VarNode getVar() {
        return var;
    }

    /**
     * @return The assignment expression.
     */
    public Expression getExpr() {
        return expr;
    }

    /**
     * @param expr The assignment expression.
     */
    public void setExpr(Expression expr) {
        this.expr = expr;
    }
    
    /**
     * @param var The assigned variable.
     */
    public void setVar(VarNode var) {
        this.var = var;
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
