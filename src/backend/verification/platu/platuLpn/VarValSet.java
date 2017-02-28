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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author ldtwo
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VarValSet extends HashSet<VarVal> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VarValSet(String list) {
        super();
        list = list.replace(" ", "");
        String[] tmp = list.split(",\\(");
        String[] vv;
        for (String s : tmp) {
            if (s.length() == 0) {
                continue;
            }
            // System.out.println("VV="+s.replace("(", "").replace(")", ""));
            vv = s.replace("(", "").replace(")", "").split(",");
            add(new VarVal(vv[0], new Integer(vv[1])));
        }
    }

    public VarValSet() {//empty set
        super();
    }

    public VarValSet(HashSet<String> retIn) {
        super();
        String[] vv;
        for (String s : retIn) {
            if (s.length() == 0) {
                continue;
            }
            System.err.println("VV=" + s.replace("(", "").replace(")", ""));
            vv = s.replace("(", "").replace(")", "").split(",");
            add(new VarVal(vv[0], new Integer(vv[1])));
        }
    }

    VarValSet(VarValSet vvs) {
        super(vvs);
    }

//    VarValSet(VarSet inputs, StateVector initState) {
//        super(inputs.size());
//        Iterator<String> it = inputs.iterator();
//        String var;
//        double i=Double.NEGATIVE_INFINITY;
//        while (it.hasNext()) {
//            var = it.next();
//            i = (double)(int)initState.get(var);
//            if (i == Double.NEGATIVE_INFINITY) {
//                System.err.println(" VarValSet(VarSet inputs, StateVector initState):\n\t"
//                        + "var=" + var + "\n\ti=-1");
//            }
//            //if( Main.ALL_VARS.contains(var))
//        Main.ALL_VARS.add(var);
//            add(new VarVal(var, i));
//        }
//    }

    public double  get(String s) {
        for (VarVal vv : this) {
            if (vv.getVariable().compareTo(s) == 0) {
                return vv.getIntValue();
            }
        }
        return (0);
    }

    public VarVal get(VarVal otherVV) {
//        if(contains(otherVV)){
//            add(otherVV);return otherVV;
//        }
        for (VarVal someVV : this) {
            if (someVV.getVariable().compareTo(otherVV.getVariable()) == 0 && someVV.getIntValue() == otherVV.getIntValue()) {
                return someVV;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        Iterator<VarVal> it = iterator();
        String ret = "";
        VarVal v;
        while (it.hasNext()) {
            v = it.next();
            if (it.hasNext()) {
                ret += v.getVariable() + " = " + v.getIntValue() + ";";
            } else {
                ret += v.getVariable() + " = " + v.getIntValue();
            }
        }//x=this.size()-1 ;
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends VarVal> vvc) {
        Iterator<? extends VarVal> it = vvc.iterator();
        VarVal vv;
        boolean ret = true;
        while (it.hasNext()) {
            vv = it.next();
            ret &= add(vv);
        }
        return ret;
    }

    @Override
    public boolean add(VarVal vv) {
        //if (null == get(vv))
        {
            return super.add(vv);
        }
        //return false;
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(get((VarVal) o));
    }

    @Override
    public boolean removeAll(Collection<?> vvc) {
        Iterator<?> it = vvc.iterator();
        Object vv;
        boolean ret = true;
        while (it.hasNext()) {
            vv = it.next();
            ret &= remove(vv);
        }
        return ret;
    }

//    public StateVector toStateVector() {
//        StateVector sv = new StateVector();
//        for (VarVal vv : this) {
//            sv.put(vv.getVariable(), (int)vv.getIntValue());
//        }
//        return sv;
//    }
    
    @Override
    public VarValSet clone(){
        return new VarValSet(this);
    }
}
