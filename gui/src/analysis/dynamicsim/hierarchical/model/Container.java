package analysis.dynamicsim.hierarchical.model;

import analysis.dynamicsim.hierarchical.util.math.ValueNode;

public abstract class Container
{
	public static enum ModelType
	{
		HSSA, HODE, HFBA, NONE;
	}

	private String		ID;
	private double		maxPropensity;
	private double		minPropensity;
	protected ValueNode	propensity;
	protected double	initPropensity;

	public Container(String submodelID)
	{
		this.ID = submodelID;
		minPropensity = Double.MAX_VALUE / 10.0;
		maxPropensity = Double.MIN_VALUE / 10.0;
	}

	public Container(Container state)
	{
		this.ID = state.ID;
		this.minPropensity = state.minPropensity;
		this.maxPropensity = state.maxPropensity;
		if (state.propensity != null)
		{
			this.propensity = state.propensity.clone();
			this.initPropensity = state.initPropensity;
		}
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

	public ValueNode createPropensity()
	{
		this.propensity = new ValueNode(0);
		return propensity;
	}

	public void setInitPropensity()
	{
		if (propensity != null)
		{
			this.initPropensity = propensity.getValue();
		}
	}

	public void restoreInitPropensity()
	{
		this.propensity.setValue(initPropensity);
	}
}
