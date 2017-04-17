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
package edu.utah.ece.async.verification.platu.platuLpn;


/**
 *
 * @author ldtwo 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VarVal {

public static int [] counts=new int[10];
  private final String var;
    private double intValue;

    public VarVal(String strVariable, double intValue) {
        this.var = strVariable;
        this.intValue = intValue;
        counts[0]++;
    }

    final boolean compareTo(final VarVal other) {
        counts[1]++;
        return getIntValue() == other.getIntValue() && getVariable().compareTo(other.getVariable()) == 0;
    }

    void setVal(double val) {
        setIntValue(val);
        counts[2]++;
    }

    @Override
    final public String toString() {
        return "(" + getVariable() + ", " + getIntValue() + ")";
    }

    /**
     * @return the strVariable
     */
    public String getVariable() {
        return var;
    }

    /**
     * @return the intValue
     */
    public double getIntValue() {
        return intValue;
    }

    /**
     * @param intValue the intValue to set
     */
    public void setIntValue(double intValue) {
        this.intValue = intValue;
    }
    static public void printUsageStats(){
        System.out.printf("%-20s %11s\n",   "VarVal",  counts[0]);
//        System.out.printf("\t%-20s %11s\n",   "compareTo",  counts[1]);
//        System.out.printf("\t%-20s %11s\n",   "setVal",  counts[2]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[3]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[4]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[5]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[6]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[7]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[8]);
//        System.out.printf("\t%-20s %11s\n",   "",  counts[9]);
    }
}
