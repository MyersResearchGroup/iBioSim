package analysis.dynamicsim.hierarchical.states;


public abstract class TreeState extends HierarchicalState{
  
  @Override
  public double getStateValue(int index) {
    return getState(index).getStateValue();
  }

  @Override
  public void setStateValue(int index, double value) {
    getState(index).setStateValue(value);
  }
  
}
