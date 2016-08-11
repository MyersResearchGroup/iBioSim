package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalNode extends AbstractHierarchicalNode
{

	private List<HierarchicalNode>	children;
	private ArrayNode				arrayNode;

	public HierarchicalNode(Type type)
	{
		super(type);
	}

	public HierarchicalNode(HierarchicalNode copy)
	{
		super(copy);
		for (int i = 0; i < copy.getNumOfChild(); i++)
		{
			addChild(copy.getChild(i).clone());
		}
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

	@Override
	public HierarchicalNode clone()
	{
		return new HierarchicalNode(this);
	}

	public void addChildren(List<HierarchicalNode> listOfNodes)
	{
		if (listOfNodes != null)
		{
			if (children == null)
			{
				children = new ArrayList<HierarchicalNode>();
			}
			children.addAll(listOfNodes);
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

	public void setChild(int index, HierarchicalNode node)
	{
		children.set(index, node);
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

	public void setArrayNode(ArrayNode arrayNode)
	{
		this.arrayNode = arrayNode;
	}

	public ArrayNode getArrayNode()
	{
		return arrayNode;
	}

	@Override
	public String report()
	{
		return toString();
	}

}
