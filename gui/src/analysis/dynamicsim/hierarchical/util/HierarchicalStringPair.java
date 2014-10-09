package analysis.dynamicsim.hierarchical.util;

public class HierarchicalStringPair {
	public String string1;
	public String string2;

	public HierarchicalStringPair(String s1, String s2) {

		string1 = s1;
		string2 = s2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HierarchicalStringPair [string1=" + string1 + ", string2="
				+ string2 + "]";
	}
}
