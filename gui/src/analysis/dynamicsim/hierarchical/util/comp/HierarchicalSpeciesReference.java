package analysis.dynamicsim.hierarchical.util.comp;

public class HierarchicalSpeciesReference
{
	private String	string;
	private double	doub;
	private String	type;

	public HierarchicalSpeciesReference(String s, double d, String t)
	{
		type = t;
		string = s;
		doub = d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HierarchicalStringDoublePair [string=" + string + ", doub=" + doub + "]";
	}

	public String getString()
	{
		return string;
	}

	public double getDoub()
	{
		return doub;
	}

	public String getType()
	{
		return type;
	}
}
