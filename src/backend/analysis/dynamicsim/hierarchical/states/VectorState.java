package backend.analysis.dynamicsim.hierarchical.states;


public class VectorState extends HierarchicalState{
  
  private int vectorIndex;
  private VectorWrapper vectorState;
  
  public VectorState(int vectorIndex)
  {
    this.vectorIndex = vectorIndex;
  }
  
  @Override
  public double getStateValue() {
    return vectorState.getVector()[vectorIndex];
  }

  @Override
  public void setStateValue(double value) {
    // TODO Auto-generated method stub
    vectorState.getVector()[vectorIndex] = value;
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

  
}
