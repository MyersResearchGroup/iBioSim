package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalNode extends AbstractHierarchicalNode
{

	private List<HierarchicalNode>	children;

	public HierarchicalNode(Type type)
	{
		super(type);
	}

	public HierarchicalNode(HierarchicalNode copy)
	{
		super(copy);
	}

	public void addChild(HierarchicalNode node)
	{
		if (node != null)
		{
			if (children == null)
			{
				children = new ArrayList<HierarchicalNode>();
			}
			children.add(node);
		}
	}

	public List<HierarchicalNode> createChildren()
	{
		children = new ArrayList<HierarchicalNode>();
		return children;
	}

	public List<HierarchicalNode> getChildren()
	{
		return children;
	}

	public HierarchicalNode getChild(int index)
	{
		if (index < children.size())
		{
			return children.get(index);
		}
		return null;
	}

	public int getNumOfChild()
	{
		return children == null ? 0 : children.size();
	}

	@Override
	public String toString()
	{
		String toString = "(" + getType().toString();
		if (children != null)
		{
			for (HierarchicalNode child : children)
			{
				toString = toString + " " + child.toString();
			}
		}
		toString = toString + ")";
		return toString;
	}

}
