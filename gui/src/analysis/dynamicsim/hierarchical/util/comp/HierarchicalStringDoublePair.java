package analysis.dynamicsim.hierarchical.util.comp;

public class HierarchicalStringDoublePair {
	public String string;
	public double doub;

	public HierarchicalStringDoublePair(String s, double d) {

		string = s;
		doub = d;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HierarchicalStringDoublePair [string=" + string + ", doub="
				+ doub + "]";
	}
}
