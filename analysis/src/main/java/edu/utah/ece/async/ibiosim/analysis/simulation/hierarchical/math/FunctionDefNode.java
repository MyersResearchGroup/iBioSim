package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;


public class FunctionDefNode extends HierarchicalNode{

  public FunctionDefNode(String name) {
    super(Type.FUNCTION);
    this.name = name;
  }
}
