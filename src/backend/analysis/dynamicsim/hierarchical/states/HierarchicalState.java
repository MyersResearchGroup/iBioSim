
package backend.analysis.dynamicsim.hierarchical.states;

public abstract class HierarchicalState
{
  protected double  value;
  
  protected int arrayIndex;

  public enum StateType
  {
    DENSE, SPARSE, SCALAR, VECTOR
  };

  public abstract double getRateValue();

  public abstract double getRateValue(int index);
  
  public abstract double getStateValue();

  public abstract double getStateValue(int index);
  
  public abstract void setStateValue(int index, double value);
  
  public abstract void setStateValue(double value);

  public abstract void addState(int index);

  public abstract void addState(int index, double value);

  public abstract HierarchicalState getState(int index);


}
