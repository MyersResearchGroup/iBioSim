package backend.verification.platu.common;


import java.util.Arrays;

import backend.verification.platu.common.PlatuObj;


public class IntArrayObj extends PlatuObj {
	int index;
	int[] IntArray;

	public IntArrayObj(int[] intArray) {
		super();
		index = 0;
		IntArray = intArray;
	}
	
	public int[] toArray() {
		return this.IntArray;
	}
	
	@Override
	public void setIndex(int Idx) {
		this.index = Idx;
	}
	
	@Override
	public int getIndex() {
		return this.index;
	}
	
	@Override
	public void setLabel(String lbl) {}

	@Override
	public String getLabel() { return null; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(IntArray);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntArrayObj other = (IntArrayObj) obj;
		if (!Arrays.equals(IntArray, other.IntArray))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if(IntArray == null)
			return "()";
		
		String result = "(";
		for(int i = 0; i < this.IntArray.length; i++)
			result += this.IntArray[i] + ",";
		result += ")";
		return result;
	}

}
