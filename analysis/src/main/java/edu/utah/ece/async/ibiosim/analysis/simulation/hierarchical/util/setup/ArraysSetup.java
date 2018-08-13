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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ArrayDimensionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;

/**
 * Setups up arrays in the hierarchical simulator.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class ArraysSetup {

  static void setupArrays(HierarchicalModel modelstate, ModelContainer container, SBase sbase, HierarchicalNode node) {
    ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

    if (plugin == null) { return; }
    if (plugin.getNumDimensions() > 0) {
      modelstate.addArray(node);
      for (Dimension dimension : plugin.getListOfDimensions()) {
        String dimensionId = dimension.getId();
        String size = dimension.getSize();
        int arrayDimension = dimension.getArrayDimension();
        node.addDimension(dimensionId, arrayDimension, size);
      }
    }

    if (plugin.getNumIndices() > 0) {
      for (Index index : plugin.getListOfIndices()) {
        String referencedAttribute = index.getReferencedAttribute();
        int arrayDimension = index.getArrayDimension();
        ASTNode math = index.getMath();
        HierarchicalNode indexNode = MathInterpreter.parseASTNode(math, null, modelstate.getVariableToNodeMap(), node.getDimensionMapping(), modelstate.getIndex());
        node.addVariableIndex(referencedAttribute, arrayDimension, indexNode);
      }
    }

  }

  static void initializeArrays(List<ModelContainer> listOfContainers, StateType type, VectorWrapper wrapper) {
    for (ModelContainer container : listOfContainers) {
      HierarchicalModel model = container.getHierarchicalModel();

      if (model.getListOfArrays() != null) {
        for (HierarchicalNode node : model.getListOfArrays()) {
          initializeArraySize(model, node, type, wrapper);
        }
      }

    }

  }

  private static void initializeArraySize(HierarchicalModel model, HierarchicalNode node, StateType type, VectorWrapper wrapper) {
    int index = model.getIndex();
    if (node.getListOfDimensions() != null) {
      for (ArrayDimensionNode dimension : node.getListOfDimensions()) {
        HierarchicalNode variable = model.getNode(dimension.getSizeRef());
        int value = (int) variable.getState().getChild(index).getValue();
        dimension.setSize(value);
      }
      initializeArraysState(model, node, type, wrapper);
    }
  }

  private static void initializeArraysState(HierarchicalModel model, HierarchicalNode node, StateType type, VectorWrapper wrapper) {
    LinkedList<HierarchicalState> listOfStates = new LinkedList<>();
    List<ArrayDimensionNode> listOfDimensions = node.getListOfDimensions();
    int dimensionIndex = listOfDimensions.size() - 1;
    int modelIndex = model.getIndex();
    if (node.getState() != null) {
      HierarchicalState templateState = node.getState().getChild(modelIndex);
      listOfStates.add(templateState);
      while (dimensionIndex >= 0) {
        ArrayDimensionNode dimension = listOfDimensions.get(dimensionIndex);
        for (int i = listOfStates.size() - 1; i >= 0; i--) {
          HierarchicalState currentState = listOfStates.pop();
          for (int j = 0; j < dimension.getSize(); j++) {
            if (dimensionIndex == 0) {
              if (type == StateType.SCALAR || node.isReaction()) {
                HierarchicalState state = new ValueState(templateState);
                currentState.addState(j, state);
              } else if (type == StateType.VECTOR) {
                HierarchicalState state = new VectorState(templateState, wrapper);
                currentState.addState(j, state);
              }
            } else {
              HierarchicalState state = CoreSetup.createState(StateType.DENSE, wrapper);
              currentState.addState(j, state);
              listOfStates.add(state);
            }
          }
        }
        dimensionIndex--;
      }

    }

  }

}