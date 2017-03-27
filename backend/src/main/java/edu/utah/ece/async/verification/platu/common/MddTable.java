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
package main.java.edu.utah.ece.async.verification.platu.common;


import java.util.*;

import main.java.edu.utah.ece.async.verification.platu.MDD.*;
import main.java.edu.utah.ece.async.verification.platu.main.Options;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class MddTable extends SetIntTuple {

	protected Mdd mddMgr = null;
	mddNode ReachSet;
	mddNode buffer;

	long nextMemUpBound;
	int Size;
	static boolean UseBuffer = true;
	static boolean MDDBUF_MODE = true;
	static int gcIntervalMin = 0;

	public MddTable(int TupleLength) {
		mddMgr = new Mdd(TupleLength*4);
		this.ReachSet = null;
		if (UseBuffer == true)
			this.buffer = Mdd.newNode();
		else
			this.buffer = null;

		nextMemUpBound = 500000000;
		this.Size = 0;
		MddTable.MDDBUF_MODE = Options.getStateFormat() == "mddbuf";
	}

	@Override
	public int add(int[] IntArray) {	
		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		//int[] byteVec = toByteArray(IntArray);
		int[] byteVec = MddTable.encode(IntArray);
		
		if(MddTable.UseBuffer == true && MddTable.MDDBUF_MODE==true) {
			mddMgr.add(this.buffer, byteVec, false);
			
			boolean overThreshold = (Options.getMemUpperBound() - curUsedMem / 1000000) < 200;
			if (overThreshold) {
				mddMgr.compress(this.buffer);
				if (this.ReachSet == null) 
					this.ReachSet = this.buffer;
				else {
					mddNode newReachSet = mddMgr.union(this.ReachSet, this.buffer);
					if (newReachSet != this.ReachSet) {
						mddMgr.remove(this.ReachSet);
						this.ReachSet = newReachSet;
					}	
					
					if (newReachSet != this.ReachSet) {
						mddMgr.remove(this.ReachSet);
						this.ReachSet = newReachSet;
					}
				}
				mddMgr.remove(this.buffer);
				this.buffer = Mdd.newNode();
				Runtime.getRuntime().gc();
				MddTable.UseBuffer = false;
				System.out.println("*** stop buffering");
			}
		}
		else {
			if(this.ReachSet==null)
				this.ReachSet = Mdd.newNode();
			mddMgr.add(this.ReachSet, byteVec, true);
			if((Options.getMemUpperBound() - curUsedMem / 1000000) > 400) 
				MddTable.UseBuffer = true;
			
		}
			
		this.Size++;
		return  0;
	}

	@Override
	public boolean contains(int[] IntArray) {
		//int[] byteVec = toByteArray(IntArray);
		int[] byteVec = MddTable.encode(IntArray);

		boolean existing = Mdd.contains(this.ReachSet, byteVec);
		if (existing == true)
			return true;

		if (this.buffer != null)
			return Mdd.contains(this.buffer, byteVec);

		return false;
	}

	@Override
	public int size() {
		return this.Size;
	}

	@Override
	public String stats() {
		return "State count: " + this.Size + ",  " + "MDD node count: " + this.mddMgr.nodeCnt();

	}

	/*
	 * Utilities for search functions
	 */
	private static int[] toIntArray(int i) {
		int[] charArray = new int[4];

		int mask = 0x000000FF;

		for (int iter = 0; iter < 4; iter++) {
			charArray[3 - iter] = (i & mask);
			i = i >> 8;
		}
		return charArray;
	}

	@SuppressWarnings("unused")
	private static int[] toByteArray(int[] intVec) {
		//System.out.println(Arrays.toString(intVec));
		int[] byteArray = new int[intVec.length*4];
		int offset = intVec.length;
		int zeros = 0;
		for (int i = 0; i < intVec.length; i++) {
			int[] result = toIntArray(intVec[i]);
			byteArray[i] = result[0];
			if(byteArray[i] == 0)
				zeros++;
			byteArray[offset+i] = result[1];
			byteArray[offset*2 + i] = result[2];
			byteArray[offset*3 + i] = result[3];
		}

		int firstNonZero = 0;
		for(int i = 0; i < intVec.length*4; i++)
			if(byteArray[i] != 0) {
				firstNonZero = i;
				break;
			}

		 int[] byteArray1 = byteArray;
		if(firstNonZero > 0) {
			int[] result = new int[intVec.length*4 - firstNonZero];
			for(int i = 0; i < result.length; i++)
				result[i] = byteArray[i+firstNonZero];
			byteArray1 = result;
		}
		 //System.out.println(Arrays.toString(byteArray1)+"\n-----------------------------------------------");
		return byteArray;
	}
	
	private static int[] encode(int[] intVec) {
		int[] codeArray = new int[intVec.length*2];
		int offset = intVec.length;

		for (int i = 0; i < intVec.length; i++) {
			int remainder = intVec[i] & 0x000003FF;
			int quotient = intVec[i] >> 10;
			codeArray[i] = quotient;
			codeArray[offset+i] = remainder;
		}

//		 System.out.println(Arrays.toString(intVec) + "\n--------------" +
//		 Arrays.toString(codeArray));
		return codeArray;
	}

	@SuppressWarnings("unused")
	private static HashSet<IntArrayObj> decompose(int[] IntArray) {
		HashSet<IntArrayObj> result = new HashSet<IntArrayObj>();
		
		for (int i = 1; i < IntArray.length-1; i++) {
			for (int ii = i+1; ii < IntArray.length; ii++) {
				int[] tmp = new int[3];
				tmp[0] = IntArray[0];
				tmp[1] = IntArray[i];
				tmp[2] = IntArray[ii];
				result.add(new IntArrayObj(tmp));
			}
		}
		return result;
	}
}
