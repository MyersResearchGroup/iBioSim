package verification.platu.common;

import java.util.*;

public class HashTable extends SetIntTuple{
	
	HashSet<IntArrayObj> Table;
	
	public HashTable() {
		this.Table = new HashSet<IntArrayObj>();
	}

	public int add(int[] IntArray) {
		boolean existing = this.Table.add(new IntArrayObj(IntArray));
		return existing ? 1 : 0;
	}
	
	public boolean contains(int[] IntArray) {
		return this.Table.contains(new IntArrayObj(IntArray));
	}
	
	public int size() {
		return this.Table.size();
	}
	
	public String stats() {
		return "States in state table: " + this.size();
	}
}
