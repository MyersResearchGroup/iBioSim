package lmoore.zone;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import lmoore.zone.matrix.MatrixVer2;


import com.carrotsearch.hppc.IntIntOpenHashMap;


public class DBM {
	private int[][] matrix;
	private int hashVal;
	
	DBM(int dim) {
		matrix = new int[dim][dim];
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++)
				matrix[x][y] = 0;
		hashVal = 0;
	}
	
    public DBM(DBM other) {
        this.hashVal = other.hashVal;
        int dim = other.dimension();
        this.matrix = new int[dim][dim];
        for(int x = 0; x < dim; x++)
            for(int y = 0; y < dim; y++)
            	this.matrix[x][y] = other.matrix[x][y];
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
					if(matrix[x][i]==platu.Common.INFINITY || matrix[i][y]==platu.Common.INFINITY)
						continue;
					if(matrix[x][y] > matrix[x][i] + matrix[i][y])
						matrix[x][y] = matrix[x][i] + matrix[i][y];
				}
			}
		
		hashVal = 0;
	}
	
	public void restrict(int x, int value) {
		this.matrix[x][0] = -value;
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
        		if(this.matrix[x][y] == platu.Common.INFINITY)
        			strOut  += "\tINF";
        		else
        			strOut += "\t" + this.matrix[x][y];
        	}
        	strOut += "\n";
    	}
		
		return strOut + "\n";
	}
}
