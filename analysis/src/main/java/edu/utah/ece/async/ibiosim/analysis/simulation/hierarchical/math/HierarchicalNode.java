/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.DenseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.SparseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalNode extends AbstractHierarchicalNode
{
  private List<HierarchicalNode>  children;
  private ArrayNode       arrayNode;
  protected HierarchicalState   state;
  protected String name;
  
  public HierarchicalNode(Type type)
  {
    super(type);
  }

  public HierarchicalNode(double value)
  {
    super(Type.NUMBER);
    state = new ValueState(value);
  }

  public HierarchicalNode(HierarchicalNode copy)
  {
    super(copy);
    for (int i = 0; i < copy.getNumOfChild(); i++)
    {
      addChild(copy.getChild(i).clone());
    }
  }

  public void addChild(HierarchicalNode node)
  {
    if (node != null)
    {
      if (children == null)
      {
        children = new ArrayList<HierarchicalNode>();
      }
      children.add(node);
    }
  }

  @Override
  public HierarchicalNode clone()
  {
    return new HierarchicalNode(this);
  }

  public void addChildren(List<HierarchicalNode> listOfNodes)
  {
    if (listOfNodes != null)
    {
      if (children == null)
      {
        children = new ArrayList<HierarchicalNode>();
      }
      children.addAll(listOfNodes);
    }
  }

  public List<HierarchicalNode> createChildren()
  {
    children = new ArrayList<HierarchicalNode>();
    return children;
  }

  public List<HierarchicalNode> getChildren()
  {
    return children;
  }

  public HierarchicalNode getChild(int index)
  {
    if (index < children.size())
    {
      return children.get(index);
    }
    return null;
  }

  public void setChild(int index, HierarchicalNode node)
  {
    children.set(index, node);
  }

  public int getNumOfChild()
  {
    return children == null ? 0 : children.size();
  }

  @Override
  public String toString()
  {
    String toString = "(" + getType().toString();
    if (children != null)
    {
      for (HierarchicalNode child : children)
      {
        toString = toString + " " + child.toString();
      }
    }
    toString = toString + ")";
    return toString;
  }

  public void setArrayNode(ArrayNode arrayNode)
  {
    this.arrayNode = arrayNode;
  }

  public ArrayNode getArrayNode()
  {
    return arrayNode;
  }

  public HierarchicalState createState(StateType type, VectorWrapper wrapper)
  {
    if(type == StateType.VECTOR)
    {
      state = new VectorState(wrapper);
    }
    else if (type == StateType.DENSE)
    {
      state = new ValueState();
    }
    else if (type == StateType.SPARSE)
    {
      state = new SparseState();
    }
    else if(type == StateType.DENSE)
    {
      state = new DenseState();
    }
    else if(type == StateType.SCALAR)
    {
      state = new ValueState();
    }
    return state;
  }

  public void setValue(double value)
  {
    state.setStateValue(value);
  }
  
  public void setValue(int index, double value)
  {
    state.getState(index).setStateValue(value);
  }
  
  public double getValue()
  {
    return state.getStateValue();
  }
  
  public double getValue(int index)
  {
    return state.getStateValue(index);
  }
  
  public double getRate()
  {
    return state.getRateValue();
  }
  
  public double getRate(int index)
  {
    return state.getRateValue();
  }

  public HierarchicalState getState()
  {
    return state;
  }
  
  public double computeRateOfChange(int index)
  {
    return 0;
  }
  
  @Override
  public String report()
  {
    return toString();
  }
  
  public String getName()
  {
    return name;
  }

}
