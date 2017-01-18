package backend.analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode extends HierarchicalNode
{

  private VariableNode	variable;
  private boolean isInitialAssignment;

  public FunctionNode(VariableNode variable, HierarchicalNode math)
  {
    this(math);
    this.variable = variable;
  }

  public FunctionNode(Type type)
  {
    super(type);
  }

  public FunctionNode(HierarchicalNode math)
  {
    super(Type.PLUS);
    this.addChild(math);

  }

  public FunctionNode(FunctionNode math)
  {
    super(math);
    this.variable = math.variable;
  }

  public VariableNode getVariable()
  {
    return variable;
  }

  public void setVariable(VariableNode variable)
  {
    this.variable = variable;
  }


  public void setIsInitAssignment(boolean initAssign)
  {
    this.isInitialAssignment = initAssign;
  }

  public boolean isInitAssignment()
  {
    return this.isInitialAssignment;
  }

  public boolean computeFunction(int index)
  {
    boolean changed = false;

    if(!(this.isInitialAssignment && variable.hasRule))
    {
      double oldValue = variable.getValue(index);
      double newValue = Evaluator.evaluateExpressionRecursive(this, false, index);
      variable.setValue(index, newValue);
      boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
      changed = !isNaN && oldValue != newValue;
    }

    return changed;
  }

}
