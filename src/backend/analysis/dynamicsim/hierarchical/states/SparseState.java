package backend.analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.Map;

public class SparseState extends TreeState
{
  private Map<Integer, HierarchicalState> mapOfStates;

  public SparseState()
  {
    mapOfStates = new HashMap<Integer, HierarchicalState>();
  }
  
  @Override
  public double getStateValue()
  {
    return value;
  }

  @Override
  public void setStateValue(double value)
  {
    this.value = value;

  }

  @Override
  public void addState(int index, double value)
  {
    mapOfStates.put(index, new ValueState(value));
  }

  @Override
  public HierarchicalState getState(int index)
  {
    return mapOfStates.get(index);
  }

  @Override
  public void addState(int index)
  {
    mapOfStates.put(index, new SparseState());
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if(mapOfStates != null)
    {
      for(Integer integer : mapOfStates.keySet())
      {
        builder.append("{" + integer + ":" + mapOfStates.get(integer) + "}");
      }
    }
    return builder.toString();
  }


}
