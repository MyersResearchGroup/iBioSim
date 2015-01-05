package analysis.dynamicsim.hierarchical.util;

import java.util.HashMap;

import org.sbml.jsbml.ASTNode;

public class IndexObject
{
	private final HashMap<String, HashMap<String, ASTNode>>	attributes;

	public IndexObject()
	{
		attributes = new HashMap<String, HashMap<String, ASTNode>>();
	}

	public void addDimToAttribute(String attribute, String dimId, ASTNode math)
	{
		if (attributes.containsKey(attribute))
		{
			attributes.get(attribute).put(dimId, math);
		}
		else
		{
			HashMap<String, ASTNode> dimIdToMath = new HashMap<String, ASTNode>();
			dimIdToMath.put(dimId, math);
			attributes.put(attribute, dimIdToMath);
		}
	}
}
