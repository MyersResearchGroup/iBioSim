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
package main.java.edu.utah.ece.async.analysis.dynamicsim.flattened;


/**
 * Adapted from http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml
 * this uses the XORShift method published by George Marsaglia in 2003 in the journal of statistical software
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class XORShiftRandom {

	private long seed;

	public XORShiftRandom(long randomSeed) {

		seed = randomSeed;
		
		if (seed == 0) seed = System.nanoTime();
	}  
	
	/**
	 * generates a random long using the xorshift method
	 * 
	 * @param numBits the number of requested bits for the number
	 * @return a random number with the requested number of bits
	 */
	public long next(int numBits) {
		  
		long rand = seed;
		
		rand ^= (rand << 21);
		rand ^= (rand >>> 35);
		rand ^= (rand << 4);
		
		seed = rand;
		
		rand &= ((1L << numBits) - 1);
		 
		return rand;		
	}
	
	/**
	 * this is basically the Java.util class
	 * 
	 * @return a random number uniformly distributed between 0 and 1
	 */
	public double nextDouble() {
		
		return ((next(26) << 27) + next(27)) / (double)(1L << 53);
	}
}
