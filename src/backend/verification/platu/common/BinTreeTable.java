package backend.verification.platu.common;


import backend.verification.platu.BinaryTree.*;


public class BinTreeTable extends SetIntTuple {
	
	BinaryTree StateTable = null;
	int Size = 0;

	public BinTreeTable() {
		this.StateTable = new BinaryTree();
		this.Size = 0;
	}
	
	@Override
	public int add(int[] IntArray) {
		int newEle = StateTable.add(IntArray);
		if(newEle != 0)
			this.Size++;
		return newEle==-1 ? 0 : 1;
	}
	
	@Override
	public boolean contains(int[] IntArray) {
		return this.StateTable.contains(IntArray);
	}
	
	@Override
	public int size() {
		return this.Size;
	}
	
	@Override
	public String stats() {
		return "Element count = "+ this.StateTable.elementCount() + ",  Tree node count = " + this.StateTable.nodeCount();
	}
}
