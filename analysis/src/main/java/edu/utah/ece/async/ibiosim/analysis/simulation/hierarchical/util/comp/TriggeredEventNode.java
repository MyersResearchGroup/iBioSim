package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;

public class TriggeredEventNode 
{
  private double priority;
  private double fireTime;
  private double[] assignmentValues;
  private int index;
  private EventNode parent;
  private boolean hasFlipped;
  
  public TriggeredEventNode(int index, EventNode parent)
  {
    this.fireTime = Double.POSITIVE_INFINITY;
    this.parent = parent;
    this.index = index;
    this.hasFlipped = false;
  }

  public int getIndex()
  {
    return index;
  }
  
  public double getFireTime() {
    return fireTime;
  }

  
  public void setFireTime(double fireTime) {
    this.fireTime = fireTime;
  }

  public double getPriority() {
    return priority;
  }

  
  public void setPriority(double priority) {
    this.priority = priority;
  }
  
  public EventNode getParent() {
    return parent;
  }

  
  public void setParent(EventNode parent) {
    this.parent = parent;
  }

  
  public double[] getAssignmentValues() {
    return assignmentValues;
  }

  public void setFlipped()
  {
      hasFlipped = true;
  }
  
  public boolean getFlipped()
  {
    return hasFlipped;
  }
  
  public void setAssignmentValues(double[] assignmentValues) {
    this.assignmentValues = assignmentValues;
  }
  
  public void fireEvent(int index, double time)
  {
    
    if (fireTime <= time)
    {

      if (!parent.isPersistent())
      {
        if (hasFlipped)
        {
          return;
        }
      }

      if (parent.isUseTriggerValue())
      {
        if(assignmentValues != null)
        {
          for (int i = 0; i < parent.getEventAssignments().size(); i++)
          {
            FunctionNode eventAssignmentNode = parent.getEventAssignments().get(i);
            VariableNode variable = eventAssignmentNode.getVariable();
            variable.setValue(index, assignmentValues[i]);
          }
        }
      }
      else
      {
        if(parent.getEventAssignments() != null)
        {
          for(FunctionNode node : parent.getEventAssignments())
          {
            node.computeFunction(index);
          } 
        }
      }
    }

  }
}