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
package edu.utah.ece.async.verification.platu.common;

import java.util.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HashTable extends SetIntTuple{
	
	HashSet<IntArrayObj> Table;
	
	public HashTable() {
		this.Table = new HashSet<IntArrayObj>();
	}

	@Override
	public int add(int[] IntArray) {
		boolean existing = this.Table.add(new IntArrayObj(IntArray));
		return existing ? 1 : 0;
	}
	
	@Override
	public boolean contains(int[] IntArray) {
		return this.Table.contains(new IntArrayObj(IntArray));
	}
	
	@Override
	public int size() {
		return this.Table.size();
	}
	
	@Override
	public String stats() {
		return "States in state table: " + this.size();
	}
}
