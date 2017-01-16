package backend.analysis.dynamicsim.hierarchical.states;

import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;

public abstract class HierarchicalState
{
  protected double  value;
  protected int arrayIndex;

  public enum StateType
  {
    DENSE, SPARSE, SCALAR, VECTOR
  };

  /**
   * 
   * @return
   */
  public abstract double getStateValue();

  public abstract double getStateValue(int index);
  
  /**
   * 
   * @param index
   * @param value
   */
  public abstract void setStateValue(int index, double value);
  
  public abstract void setStateValue(double value);

  /**
   * 
   * @param index
   * @param value
   */
  public abstract void addState(int index);

  /**
   * 
   * @param index
   * @param value
   */
  public abstract void addState(int index, double value);

  /**
   * 
   * @param index
   * @return
   */
  public abstract HierarchicalState getState(int index);

  public int getArrayIndex()
  {
    return this.arrayIndex;
  }
  
  public void setArrayIndex(int index)
  {
    this.arrayIndex = index;
  }

}
