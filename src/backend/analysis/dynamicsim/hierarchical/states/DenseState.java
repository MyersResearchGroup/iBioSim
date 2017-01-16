package backend.analysis.dynamicsim.hierarchical.states;

import java.util.ArrayList;
import java.util.List;

public class DenseState extends TreeState
{

  private List<HierarchicalState>  listOfStates;

  public DenseState()
  {
    this.listOfStates = new ArrayList<HierarchicalState>(1);
  }

  public DenseState(int capacity)
  {
    this.listOfStates = new ArrayList<HierarchicalState>(capacity);
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
    if (listOfStates == null)
    {
      listOfStates = new ArrayList<HierarchicalState>();
    }

    while (index > listOfStates.size())
    {
      listOfStates.add(new ValueState());
    }

  }

  @Override
  public HierarchicalState getState(int index)
  {
    return listOfStates.get(index);
  }

  @Override
  public void addState(int index)
  {
    if (listOfStates == null)
    {
      listOfStates = new ArrayList<HierarchicalState>();
    }

    while (index > listOfStates.size())
    {
      listOfStates.add(new DenseState());
    }

  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if(listOfStates != null)
    {
      for(int i = 0; i < listOfStates.size(); i++)
      {
        builder.append("{" + i + ":" + listOfStates.get(i) + "}");
      }
    }
    return builder.toString();
  }

}
