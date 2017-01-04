package backend.verification.platu.common;


import java.util.*;

import backend.verification.platu.MDD.*;
import backend.verification.platu.main.Options;

//public class MddTable extends SetIntTuple {
//
//	protected Mdd mddMgr = null;
//	mddNode ReachSet[];
//	mddNode buffer[];
//
//	long nextMemUpBound;
//	int Size;
//	static boolean UseBuffer = true;
//	static boolean MDDBUF_MODE = true;
//	static int gcIntervalMin = 0;
//
//	public MddTable(int TupleLength) {
//		mddMgr = new Mdd(TupleLength);
//
//		this.ReachSet = new mddNode[4];
//		this.buffer = new mddNode[4];
//		
//		for (int i = 0; i < 4; i++) {
//			this.ReachSet[i] = null;
//
//			if (UseBuffer == true)
//				this.buffer[i] = mddMgr.newNode();
//			else
//				this.buffer[i] = null;
//		}
//
//		nextMemUpBound = 500000000;
//		this.Size = 0;
//		this.MDDBUF_MODE = Options.getStateFormat() == Options.StateFormatDef.MDDBUF;
//	}
//
//	public int add(int[] IntArray) {
//		System.out.println("adding vector " + Arrays.toString(IntArray) + "-------------");
//
//		long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//		int[][] byteVec = toByteArray(IntArray);
//
//		if (MddTable.UseBuffer == true && MddTable.MDDBUF_MODE == true) {
//			for (int i = 0; i < 4; i++) {
//				mddMgr.add(this.buffer[i], byteVec[i], false);
//			}
//			boolean overThreshold = (Options.getMemUpperBound() - curUsedMem / 1000000) < 200;
//			if (overThreshold) {
//				for (int i = 0; i < 4; i++) {
//					mddMgr.compress(this.buffer[i]);
//					if (this.ReachSet[i] == null) {
//						this.ReachSet[i] = this.buffer[i];
//					} else {
//						mddNode newReachSet = mddMgr.union(this.ReachSet[i], this.buffer[i]);
//
//						if (newReachSet != this.ReachSet[i]) {
//							mddMgr.remove(this.ReachSet[i]);
//							this.ReachSet[i] = newReachSet;
//						}
//
//						mddMgr.remove(this.buffer[i]);
//						this.buffer[i] = mddMgr.newNode();
//					}
//				}
//				Runtime.getRuntime().gc();
//				MddTable.UseBuffer = false;
//				System.out.println("*** stop buffering");
//			}
//		} 
//		else {
//			for (int i = 0; i < 4; i++) {
//				if (this.ReachSet[i] == null)
//					this.ReachSet[i] = mddMgr.newNode();
//				mddMgr.add(this.ReachSet[i], byteVec[i], true);
//			}
//			if ((Options.getMemUpperBound() - curUsedMem / 1000000) > 400) {
//				MddTable.UseBuffer = true;
//				// System.out.println("*** restart buffering");
//			}
//		}
//
//		this.Size++;
//		return 0;
//	}
//
//	public boolean contains(int[] IntArray) {
//		System.out.println("checking vector " + Arrays.toString(IntArray));
//
//		int[][] byteVec = toByteArray(IntArray);
//		for (int i = 0; i < 4; i++) {
//			System.out.println(Arrays.toString(byteVec[i]));
//			boolean existing = mddMgr.contains(this.ReachSet[i], byteVec[i]);
//			System.out.print(i + "  Exists in ReachSet ?" + (existing ? "yes" : "no") + ",   ");
//			if (existing == true)
//				continue;
//			
//			existing = mddMgr.contains(this.buffer[i], byteVec[i]);
//			System.out.print("Exists in buffer ?" + (existing ? "yes" : "no") + "\n\n   ");
//
//			if(existing==false)
//				return false;
//		}
//
//		return true;
//	}
//
//	public int size() {
//		return this.Size;
//	}
//
//	public String stats() {
//		return "State count: " + this.Size + ",  " + "MDD node count: " + this.mddMgr.nodeCnt();
//
//	}
//
//	/*
//	 * Utilities for search functions
//	 */
//	private int[] toIntArray(int i) {
//		int[] charArray = new int[4];
//
//		int mask = 0x000000FF;
//
//		for (int iter = 0; iter < 4; iter++) {
//			charArray[3 - iter] = (i & mask);
//			i = i >> 8;
//		}
//		return charArray;
//	}
//
//	private int[][] toByteArray(int[] intVec) {		
//		int[][] byteArray = new int[4][intVec.length];
//		int offset = intVec.length;
//		int curPos = 0;
//
//		for (int i = 0; i < intVec.length; i++) {
//			int[] result = toIntArray(intVec[i]);
//			byteArray[0][i] = result[0];
//			curPos++;
//			byteArray[1][i] = result[1];
//			curPos++;
//			byteArray[2][i] = result[2];
//			curPos++;
//			byteArray[3][i] = result[3];
//			curPos = 0;
//			;
//		}
//		
//		// System.out.println(Arrays.toString(intVec) + "\n--------------" +
//		// Arrays.toString(byteArray));
//		return byteArray;
//	}
//
//	private int[] simplify(int[] IntArray) {
//		int[] result = new int[IntArray.length];
//		for (int i = IntArray.length - 1; i > 0; i--) {
//			int cur = IntArray[i];
//			int nxt = IntArray[i - 1];
//			int diff = cur - nxt;
//			boolean neg = diff < 0;
//			diff = diff << 1;
//			if (neg == true) {
//				diff = 0 - diff;
//				diff += 1;
//			}
//			result[i] = diff;
//		}
//		result[0] = IntArray[0];
//
//		return result;
//	}
//}


//** A different version below ***********************************************

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
