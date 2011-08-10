package platu;

public class Pair<LEFT, RIGHT> {
	
	LEFT left;
	RIGHT right;
	
	public Pair() {
		this.left = null;
		this.right = null;
	}

	public Pair(LEFT l, RIGHT r) {
		this.left = l;
		this.right = r;
	}
	
	public LEFT getLeft() {
		return this.left;
	}
	
	public RIGHT getRight() {
		return this.right;
	}
	
	@Override
	public int hashCode() {
		return this.left.hashCode() ^ this.right.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		Pair<LEFT, RIGHT> otherPair = (Pair<LEFT,RIGHT>)other;
		return this.left.equals(otherPair.left) && this.right.equals(otherPair.right);
	}
}
