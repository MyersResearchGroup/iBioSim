package analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

import analysis.dynamicsim.hierarchical.states.DenseState;
import analysis.dynamicsim.hierarchical.states.HierarchicalState;
import analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import analysis.dynamicsim.hierarchical.states.SparseState;
import analysis.dynamicsim.hierarchical.states.ValueState;

public class HierarchicalNode extends AbstractHierarchicalNode
{

	private List<HierarchicalNode>	children;
	private ArrayNode				arrayNode;
	protected HierarchicalState		state;
	protected List<Integer>			indexToSubmodel;

	public HierarchicalNode(Type type)
	{
		super(type);
	}

	public HierarchicalNode(double value)
	{
		super(Type.NUMBER);
		state = new ValueState(value);
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

	public HierarchicalState createState(StateType type)
	{
		if (type == StateType.DENSE)
		{
			state = new ValueState();
		}
		else if (type == StateType.SPARSE)
		{
			state = new SparseState();
		}
		else
		{
			state = new DenseState();
		}
		return state;
	}

	public void setValue(double value)
  {
    state.setStateValue( value);
  }
	
	public void setValue(int index, double value)
	{
	  if(state.getState(index) == null)
	  {
	    state.addState(index);
	  }
		state.getState(index).setStateValue( value);
	}
	public double getValue()
  {
    return state.getStateValue();
  }
	
	public double getValue(int index)
	{
	  
		return state.getState(index).getStateValue();
	}

	public HierarchicalState getState()
	{
		return state;
	}

	
	public void setIndexToSubmodel(List<Integer> indexToSubmodel)
	{
		this.indexToSubmodel = indexToSubmodel;
	}

	public int getSubmodelIndex(int index)
	{
		return indexToSubmodel.get(index);
	}

	@Override
	public String report()
	{
		return toString();
	}

}
