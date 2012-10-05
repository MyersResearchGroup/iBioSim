package verification.platu.common;


import verification.platu.BinaryTree.*;


public class BinTreeTable extends SetIntTuple {
	
	BinaryTree StateTable = null;
	int Size = 0;

	public BinTreeTable() {
		this.StateTable = new BinaryTree(false);
		this.Size = 0;
	}
	
	public int add(int[] IntArray) {
		int newEle = StateTable.add(IntArray);
		if(newEle != 0)
			this.Size++;
		return newEle==-1 ? 0 : 1;
	}
	
	public boolean contains(int[] IntArray) {
		return this.StateTable.contains(IntArray);
	}
	
	public int size() {
		return this.Size;
	}
	
	public String stats() {
		return "Element count = "+ this.StateTable.elementCount() + ",  Tree node count = " + this.StateTable.nodeCount();
	}
}
