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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;

/**
 * HierarchicalNode is used to represent the most basic node in an abstract syntax tree for representing math in the
 * hierarchical simulator.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalNode extends AbstractHierarchicalNode {
  private List<HierarchicalNode> children;
  private Map<String, ArrayDimensionNode> mapOfDimensions;
  private List<ArrayDimensionNode> listOfDimensions;
  protected HierarchicalState state;

  public HierarchicalNode(Type type) {
    super(type);
  }

  public HierarchicalNode(double value) {
    super(Type.NUMBER);
    state = new ValueState(value);
  }

  public HierarchicalNode(HierarchicalNode copy) {
    super(copy);
    for (int i = 0; i < copy.getNumOfChild(); i++) {
      addChild(copy.getChild(i).clone());
    }
  }

  /**
   * Adds a child.
   *
   * @param node
   *          - the child node.
   */
  public void addChild(HierarchicalNode node) {
    if (node != null) {
      if (children == null) {
        children = new ArrayList<>();
      }
      children.add(node);
    }
  }

  @Override
  public HierarchicalNode clone() {
    return new HierarchicalNode(this);
  }

  /**
   * Adds a collection of child nodes.
   *
   * @param listOfNodes
   *          - a list of child nodes.
   */
  public void addChildren(List<HierarchicalNode> listOfNodes) {
    if (listOfNodes != null) {
      if (children == null) {
        children = new ArrayList<>();
      }
      children.addAll(listOfNodes);
    }
  }

  /**
   * Gets the child nodes.
   *
   * @return the list of child nodes.
   */
  public List<HierarchicalNode> getChildren() {
    return children;
  }

  /**
   * Gets a child from the given index.
   *
   * @param index
   *          - the index of the child.
   *
   * @return the child node if there is a node at the given index. Returns null otherwise.
   */
  public HierarchicalNode getChild(int index) {
    if (index >= 0 && index < children.size()) { return children.get(index); }
    return null;
  }

  /**
   * Gets the number of child nodes.
   *
   * @return the number of child nodes.
   */
  public int getNumOfChild() {
    return children == null ? 0 : children.size();
  }

  @Override
  public String toString() {
    String toString = "(" + getType().toString();
    if (children != null) {
      for (HierarchicalNode child : children) {
        toString = toString + " " + child.toString();
      }
    }
    toString = toString + ")";
    return toString;
  }

  /**
   * Adds a dimension object.
   *
   * @param dimensionId
   *          - the id of the dimension object.
   * @param arrayDimension
   *          - dimension index.
   * @param size
   *          - the parameter for size.
   */
  public void addDimension(String dimensionId, int arrayDimension, VariableNode size) {
    if (listOfDimensions == null) {
      listOfDimensions = new ArrayList<>();
    }

    ArrayDimensionNode dimensionNode = new ArrayDimensionNode(size);

    for (int i = listOfDimensions.size(); i <= arrayDimension; i++) {
      listOfDimensions.add(null);
    }

    listOfDimensions.set(arrayDimension, dimensionNode);

    if (dimensionId != null) {
      if (mapOfDimensions == null) {
        mapOfDimensions = new HashMap<>();
      }
      mapOfDimensions.put(dimensionId, dimensionNode);
    }

  }

  /**
   * Gets the state object.
   *
   * @return the state object.
   */
  public HierarchicalState getState() {
    return state;
  }

  /**
   * Sets the state object.
   *
   * @param state
   *          - the state object.
   */
  public void setState(HierarchicalState state) {
    if (this.state == null) {
      this.state = state;
    }
  }

  @Override
  public double computeRateOfChange(int index) {
    return 0;
  }

  /**
   * Get value from arrayed variable.
   *
   * @param modelIndex
   *          - index of submodel.
   * @param listOfIndices
   *          - the array indices.
   * @return the value of selected variable.
   */
  public double getValue(int modelIndex, List<Integer> listOfIndices) {
    HierarchicalState variableState = state.getChild(modelIndex);
    if (listOfIndices != null) {
      for (int i = listOfIndices.size() - 1; i >= 0; i--) {
        variableState = variableState.getChild(listOfIndices.get(i));
      }
    }

    return variableState.getValue();
  }

  /**
   * Get value from scalar variable.
   *
   * @param modelIndex
   *          - index of submodel.
   * @param listOfIndices
   *          - the array indices.
   * @return the value of selected variable.
   */
  public double getValue(int modelIndex) {
    return getValue(modelIndex, null);
  }

  /**
   * Get value from arrayed variable.
   *
   * @param modelIndex
   *          - index of submodel.
   * @param listOfIndices
   *          - the array indices.
   * @return if the value has changed.
   */
  public boolean setValue(int modelIndex, List<Integer> listOfIndices, double value) {
    HierarchicalState variableState = state.getChild(modelIndex);
    if (listOfIndices != null) {
      for (int i = listOfIndices.size() - 1; i >= 0; i--) {
        variableState = variableState.getChild(listOfIndices.get(i));
      }
    }
    double oldValue = variableState.getValue();
    variableState.setStateValue(value);

    return oldValue != value;
  }

  /**
   * Get value from arrayed variable.
   *
   * @param modelIndex
   *          - index of submodel.
   * @return if the value has changed.
   */
  public boolean setValue(int modelIndex, double value) {
    return setValue(modelIndex, null, value);
  }

}
