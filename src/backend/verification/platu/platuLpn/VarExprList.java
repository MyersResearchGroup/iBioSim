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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.verification.platu.platuLpn;

import java.util.*;

import backend.verification.platu.expression.Expression;
import backend.verification.platu.expression.VarNode;

/**
 * @author ldtwo
 */
public class VarExprList extends ArrayList<VarExpr> {

//    public VarExprList(String[] list,DualHashMap dhm) {
//        super();
//        for (String s : list) {
//            if (s.length() > 0) {
//                add(new VarExpr(s,dhm));
//            }
//        }
//    }

//    public VarExprList(VarExprList l,DualHashMap dhm) {
//       
//        for(VarExpr ve:l){
//            add(new VarExpr(ve.getVar(), ve.getExpr().getExpression(),dhm));
//        }
//    }

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VarExprList() {
        super();
    }

    @Override
    public String toString() {
        String ret = "";
        int x = 0;
        try {
            for (; x < this.size() - 1; x++) {
                ret += this.get(x).getVar() + " = " + this.get(x).getExpr() + ";";
            }//x=this.size()-1 ;
            if (size() > 0) {
                ret += this.get(x).getVar() + " = " + this.get(x).getExpr();
            }
        } catch (Exception e) {
            System.err.print("ret=" + ret + "\tx=" + x + "\tsize=" + size() + "\tget(x)=");
            try {
                System.err.println(get(x));
            } catch (Exception e2) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedList)) {
            return false;
        }
        if ((o != this)) {
            return false;
        }
        if (((LinkedList) o).size() != size()) {
            return false;
        }
        @SuppressWarnings("unchecked")
		LinkedList<VarExpr> st = ((LinkedList<VarExpr>) o);
        for (VarExpr s : this) {
            if (!st.contains(s)) {
                return false;
            }
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return size();
    }

    public Expression get(VarNode var){
        for(VarExpr ve:this){
            if(ve.getVar() == var)return ve.getExpr();
        }
            return null;
    }
    
    public VarExprList copy(HashMap<String, VarNode> variables){
    	VarExprList copy = new VarExprList();
    	for(VarExpr v : this){
    		copy.add(v.copy(variables));
    	}
    	
    	return copy;
    }
    
    @Override
    public VarExprList clone(){
    	return new VarExprList();
    }
    
//    public VarExprList clone(DualHashMap dhm) {
//    	return new VarExprList(this,dhm);
//    }

}
