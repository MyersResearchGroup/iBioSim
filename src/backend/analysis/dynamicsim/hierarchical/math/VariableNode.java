package backend.analysis.dynamicsim.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

import backend.analysis.dynamicsim.hierarchical.states.ValueState;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

public class VariableNode extends HierarchicalNode
{

  protected boolean			isVariableConstant;
  protected String			name;
  protected boolean hasRule;

  private List<ReactionNode>	reactionDependents;
  protected HierarchicalNode rateRule;

  public VariableNode(String name)
  {
    super(Type.NAME);
    this.name = name;
  }

  public VariableNode(String name, StateType type)
  {
    super(Type.NAME);
    this.name = name;
    this.state = new ValueState();
  }

  public VariableNode(VariableNode copy)
  {
    super(copy);
    this.name = copy.name;
    this.isVariableConstant = copy.isVariableConstant;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public List<ReactionNode> getReactionDependents()
  {
    return reactionDependents;
  }

  public void addReactionDependency(ReactionNode dependency)
  {
    if (reactionDependents == null)
    {
      reactionDependents = new ArrayList<ReactionNode>();
    }
    reactionDependents.add(dependency);
  }

  public void setIsVariableConstant(boolean isConstant)
  {
    this.isVariableConstant = isConstant;
  }

  public boolean isVariableConstant()
  {
    return isVariableConstant;
  }



  public double computeRateOfChange(int index, double time)
  {
    if (rateRule == null)
    {
      return 0;
    }
    return Evaluator.evaluateExpressionRecursive(rateRule, false, index);
  }

  public void setRateRule( HierarchicalNode rateRule)
  {
    this.rateRule = rateRule;
  }

  public HierarchicalNode getRateRule()
  {
    return rateRule;
  }


  @Override
  public String toString()
  {
    return name;
  }

  @Override
  public VariableNode clone()
  {
    return new VariableNode(this);
  }

  public void setHasRule(boolean hasRule)
  {
    this.hasRule = hasRule;
  }

  public boolean hasRule()
  {
    return hasRule;
  }


}
