package backend.analysis.dynamicsim.hierarchical.states;

public class ValueState extends HierarchicalState
{

	public ValueState()
	{
		this.value = 0;
	}

	public ValueState(double value)
	{
		this.value = value;
	}

	@Override
	public double getStateValue()
	{
		return value;
	}

	@Override
	public void setStateValue(double value)
	{
		this.value = value;
	}

	@Override
	public void addState(int index, double value)
	{
		// can't have children
	}

	@Override
	public HierarchicalState getState(int index)
	{
		// can't have children
		return null;
	}

	@Override
	public void addState(int index)
	{

	}
	
  @Override
  public String toString() {
    
    return String.valueOf(value);
  }

}
