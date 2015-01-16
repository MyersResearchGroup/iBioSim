package analysis.dynamicsim.hierarchical.util;

import java.util.HashMap;

import org.sbml.jsbml.ASTNode;

public class IndexObject
{
	private final HashMap<String, HashMap<Integer, ASTNode>>	attributes;

	public IndexObject()
	{
		attributes = new HashMap<String, HashMap<Integer, ASTNode>>();
	}

	public void addDimToAttribute(String attribute, int dim, ASTNode math)
	{
		if (attributes.containsKey(attribute))
		{
			attributes.get(attribute).put(dim, math);
		}
		else
		{
			HashMap<Integer, ASTNode> dimIdToMath = new HashMap<Integer, ASTNode>();
			dimIdToMath.put(dim, math);
			attributes.put(attribute, dimIdToMath);
		}
	}

	public HashMap<String, HashMap<Integer, ASTNode>> getAttributes()
	{
		return attributes;
	}
}
