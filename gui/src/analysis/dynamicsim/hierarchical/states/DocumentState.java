package analysis.dynamicsim.hierarchical.states;

import analysis.dynamicsim.hierarchical.util.math.ValueNode;

public abstract class DocumentState implements State
{
	private String		ID;
	private double		maxPropensity;
	private double		minPropensity;
	private boolean		noConstraintsFlag;
	private boolean		noEventsFlag;
	private boolean		noRuleFlag;
	protected ValueNode	propensity;
	protected double	initPropensity;

	public DocumentState(String submodelID)
	{
		this.ID = submodelID;
		minPropensity = Double.MAX_VALUE / 10.0;
		maxPropensity = Double.MIN_VALUE / 10.0;
		noConstraintsFlag = true;
		noRuleFlag = true;
		noEventsFlag = true;
	}

	public DocumentState(DocumentState state)
	{
		this.ID = state.ID;
		this.minPropensity = state.minPropensity;
		this.maxPropensity = state.maxPropensity;
		this.noConstraintsFlag = state.noConstraintsFlag;
		this.noRuleFlag = state.noRuleFlag;
		this.noEventsFlag = state.noEventsFlag;
	}

	public String getID()
	{
		return ID;
	}

	public double getMaxPropensity()
	{
		return maxPropensity;
	}

	public double getMinPropensity()
	{
		return minPropensity;
	}

	public double getPropensity()
	{
		return propensity.getValue();
	}

	public boolean isNoConstraintsFlag()
	{
		return noConstraintsFlag;
	}

	public boolean isNoEventsFlag()
	{
		return noEventsFlag;
	}

	public boolean isNoRuleFlag()
	{
		return noRuleFlag;
	}

	public void setID(String iD)
	{
		ID = iD;
	}

	public void setMaxPropensity(double maxPropensity)
	{
		this.maxPropensity = maxPropensity;
	}

	public void setMinPropensity(double minPropensity)
	{
		this.minPropensity = minPropensity;
	}

	public void setNoConstraintsFlag(boolean noConstraintsFlag)
	{
		this.noConstraintsFlag = noConstraintsFlag;
	}

	public void setNoEventsFlag(boolean noEventsFlag)
	{
		this.noEventsFlag = noEventsFlag;
	}

	public void setNoRuleFlag(boolean noRuleFlag)
	{
		this.noRuleFlag = noRuleFlag;
	}

	public ValueNode createPropensity()
	{
		this.propensity = new ValueNode(0);
		return propensity;
	}

	public void setInitPropensity()
	{
		this.initPropensity = propensity.getValue();
	}

	public void restoreInitPropensity()
	{
		this.propensity.setValue(initPropensity);
	}
}
