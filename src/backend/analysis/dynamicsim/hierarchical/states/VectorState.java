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
    return vectorState.getValues()[vectorIndex];
  }

  @Override
  public void setStateValue(double value) {
    // TODO Auto-generated method stub
    vectorState.getValues()[vectorIndex] = value;
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
  
}
