package backend.analysis.dynamicsim.hierarchical.states;

public class VectorState extends HierarchicalState{

  private int vectorIndex;
  private final VectorWrapper vectorState;

  public VectorState(VectorWrapper vectorState)
  {
    this.vectorState = vectorState;
    this.vectorIndex = vectorState.incrementSize();
  }

  @Override
  public double getStateValue() {
    if(vectorState.isSet())
    {
      return vectorState.getValues()[vectorIndex];
    }
    else
    {
      return vectorState.getInitValues().get(vectorIndex);
    }
  }

  @Override
  public void setStateValue(double value) {
    if(vectorState.isSet())
    {
      vectorState.getValues()[vectorIndex] = value;
    }
    else
    {
      vectorState.getInitValues().set(vectorIndex, value);
    }
    
  }

  @Override
  public void addState(int index) {
  }

  @Override
  public void addState(int index, double value) {
  }

  @Override
  public HierarchicalState getState(int index) {
    return null;
  }

  @Override
  public double getStateValue(int index) {
    return getStateValue();
  }

  @Override
  public void setStateValue(int index, double value) {
    setStateValue(value);
  }
  
  @Override
  public double getRateValue() {
    if(vectorState.isSet())
    {
      return vectorState.getRates()[vectorIndex];
    }
    else
    {
      return 0;
    }
  }

  @Override
  public double getRateValue(int index) {
    return getRateValue();
  }

}