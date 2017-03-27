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
package edu.utah.ece.async.verification.platu.TimingAnalysis;

import java.util.*;

import edu.utah.ece.async.verification.platu.common.Pair;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DBM {
	
	private int[][] matrix;
	private int hashVal;
	private int refCount;
	
	DBM(int dim) {
		matrix = new int[dim][dim];
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++)
				matrix[x][y] = 0;
		this.hashVal = 0;
		this.refCount = 0;
	}
	
    public DBM(DBM other) {
        this.hashVal = other.hashVal;
        int dim = other.dimension();
        this.matrix = new int[dim][dim];
        for(int x = 0; x < dim; x++)
            for(int y = 0; y < dim; y++)
            	this.matrix[x][y] = other.matrix[x][y];
        this.refCount = 0;
    }

    /**
     *
     * @return
     */
    final public int dimension() {
        return this.matrix.length;
    }

    @Override
    public Object clone() {
        return new DBM(this);
    }

	public void assign(int x, int y, int val) {
		matrix[x][y] = val;
		this.hashVal = 0;
	}
	
	public int value(int x, int y) {
		return matrix[x][y];
	}
	
	public void canonicalize() {
		int dim = matrix.length;
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++) {
				if(x==y) continue;
				for(int i = 0; i < dim; i++) {
					if(i==x || i==y) continue;
					if(matrix[x][i]==edu.utah.ece.async.verification.platu.common.Common.INFINITY || matrix[i][y]==edu.utah.ece.async.verification.platu.common.Common.INFINITY)
						continue;
					if(matrix[x][y] > matrix[x][i] + matrix[i][y])
						matrix[x][y] = matrix[x][i] + matrix[i][y];
				}
			}
		
		hashVal = 0;
	}
	
	public void restrict(int x, int value) {
		this.matrix[x][0] = -value;
		this.hashVal = 0;
	}

	public DBM merge(DBM other) {
		int dim = this.dimension();
		
		DBM newDbm = new DBM(dim);
		
		for(int i = 0; i < dim; i++)
			for(int j = 0; j < dim; j++) {
				int maxVal_ij = this.value(i, j) > other.value(i, j) ? this.value(i, j) : other.value(i, j);
				int maxVal_ji = this.value(j, i) > other.value(j, i) ? this.value(j, i) : other.value(j, i);
				newDbm.assign(i, j, maxVal_ij);
				newDbm.assign(j, i, maxVal_ji);
			}
		
		return newDbm;
	}
	
	/*
	 * Check if this DBM is a subset of the other DBM.
	 */
	public boolean subset(DBM other) {
		int dim = this.dimension();
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++) {
				if(this.matrix[x][y] > other.matrix[x][y])
					return false;
			}
		return true;
	}
	
	/*
	 * Return a DBM with all redundant edges removed.
	 * @see 'Timed Automata: Semantics, Algorithms and Tools
	 */
	public HashMap<Pair<Integer,Integer>, Integer> getMinConstr() {
		int dim = matrix.length;
		
		HashMap<Pair<Integer,Integer>, Integer> constrSet = new HashMap<Pair<Integer,Integer>, Integer>();
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++) {
				if(x==y) continue;
				for(int i = 0; i < dim; i++) {
					if(i==x || i==y) continue;
					if(matrix[x][i]==edu.utah.ece.async.verification.platu.common.Common.INFINITY || matrix[i][y]==edu.utah.ece.async.verification.platu.common.Common.INFINITY)
						continue;
					if(matrix[x][i] + matrix[i][y] > matrix[x][y]) 
						constrSet.put(new Pair<Integer,Integer>(x,y), this.matrix[x][y]);
				}
			}
		return constrSet;
	}
	
	
    @Override
    final public int hashCode() {
        if(this.hashVal != 0)
        	return hashVal;
        
        int dim = matrix.length;
        int[] tmp_hash = new int[dim];
        for(int i = 0; i < dim; i++) 
        	tmp_hash[i] = Integer.rotateLeft(Arrays.hashCode(this.matrix[i]), i);
        	
        return Arrays.hashCode(tmp_hash);
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    final public boolean equals(Object other) {
    	DBM otherDbm = (DBM)other;
        if (this.matrix == otherDbm.matrix)
            return true;
       
        int dim = this.dimension();
        if(this.dimension() != otherDbm.dimension())
        	return false;
        
        for(int x = 0; x < dim; x++) 
            for(int y = 0; y < dim; y++) 
            	if(this.matrix[x][y] != otherDbm.matrix[x][y])
            		return false;
        
        return true;
    }

    @Override
	public String toString() {
    	int dim = this.dimension();
    	String strOut = new String();
    	for(int x = 0; x < dim; x++) {
        	for(int y = 0; y < dim; y++) {
        		if(this.matrix[x][y] == edu.utah.ece.async.verification.platu.common.Common.INFINITY)
        			strOut  += "\tINF";
        		else
        			strOut += "\t" + this.matrix[x][y];
        	}
        	strOut += "\n";
    	}
		
		return strOut + "\n";
	}
    
    public int[] signature() {
    	int dim = this.dimension();
    	int[] result = new int[this.dimension() * this.dimension()];
    	
    	for(int i = 0; i < this.dimension(); i++)
        	for(int j = 0; j < this.dimension(); j++) {
        		int pos = i * dim + j;
        		result[pos] = this.matrix[i][j];
        	}
    	
    	return result;
    }
    
    public void incRefCnt() {
    	this.refCount++;
    }
    
    public int decrRefCnt() {
    	this.refCount--;
    	return this.refCount;
    }
}
