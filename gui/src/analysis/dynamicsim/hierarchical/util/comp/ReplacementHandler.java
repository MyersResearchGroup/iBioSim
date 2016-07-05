package analysis.dynamicsim.hierarchical.util.comp;

import analysis.dynamicsim.hierarchical.states.ModelState;

public class ReplacementHandler
{
	private ModelState	fromModelState;
	private String		fromVariable;
	private ModelState	toModelState;
	private String		toVariable;

	public ReplacementHandler(ModelState fromModelState, String fromVariable, ModelState toModelState, String toVariable)
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

	public ModelState getFromModelState()
	{
		return fromModelState;
	}

	public String getFromVariable()
	{
		return fromVariable;
	}

	public ModelState getToModelState()
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
