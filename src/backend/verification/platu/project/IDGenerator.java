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
package backend.verification.platu.project;

import java.util.HashMap;

	/**
	 * 
	 * @author ldmtwo
	 */
public class IDGenerator<T> extends HashMap<T, Integer> {
	private static final long serialVersionUID = 98976418277654L;

	private int next = 0;

	public IDGenerator() {
		super(100000);
	}

	public IDGenerator(int size) {
		super(size);
	}

	// @Override
	// public int size() {
	// return next;
	// }

	public boolean add(T item) {
		return next == tryInsert(item);
	}

	/**
	 * If the item does not exist, then insert. Return the unique ID for that
	 * item.
	 * 
	 * @param item
	 * @return
	 */
	public int tryInsert(T item) {
		if (containsKey(item)) {
			return get(item);
		}
		int ID = next++;
		put(item, ID);
		return ID;
	}

	public int test(T item) {
		if (containsKey(item)) {
			return get(item);
		}
		return next;
	}
}
