package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;


public class NumberNode extends HierarchicalNode
{

  private double value;
  public NumberNode(double value) {
    super(Type.NUMBER);
    this.value = value;
  }
  
  @Override
  public double getValue()
  {
    return value;
  }
  
  @Override
  public double getValue(int index)
  {
    return value;
  }
  
  @Override
  public String toString()
  {
    return String.valueOf(value);
  }
}
