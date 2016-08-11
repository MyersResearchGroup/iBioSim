package analysis.dynamicsim.hierarchical.util.comp;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;

public class ReplacementHandler
{
	private HierarchicalModel	fromModelState;
	private String		fromVariable;
	private HierarchicalModel	toModelState;
	private String		toVariable;

	public ReplacementHandler(HierarchicalModel fromModelState, String fromVariable, HierarchicalModel toModelState, String toVariable)
	{
		this.fromModelState = fromModelState;
		this.fromVariable = fromVariable;
		this.toModelState = toModelState;
		this.toVariable = toVariable;
	}

	public void copyNodeTo()
	{
		toModelState.addMappingNode(toVariable, fromModelState.getNode(fromVariable));
	}

	public HierarchicalModel getFromModelState()
	{
		return fromModelState;
	}

	public String getFromVariable()
	{
		return fromVariable;
	}

	public HierarchicalModel getToModelState()
	{
		return toModelState;
	}

	public String getToVariable()
	{
		return toVariable;
	}

	@Override
	public String toString()
	{
		return "copy " + fromVariable + " of " + fromModelState.getID() + " to " + toVariable + " of " + toModelState.getID();
	}

}
