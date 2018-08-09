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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;

/**
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim
 *         Contributors </a>
 * @version %I%
 */
public abstract class AbstractHierarchicalNode {

  /**
   * Operator types
   */
  public static enum Type {
    CONSTANT_E, CONSTANT_FALSE, CONSTANT_PI, CONSTRUCTOR_PIECE, CONSTRUCTOR_OTHERWISE, CONSTANT_TRUE, DIVIDE, FUNCTION, FUNCTION_ABS, FUNCTION_ARCCOS, FUNCTION_ARCCOSH, FUNCTION_ARCCOT, FUNCTION_ARCCOTH, FUNCTION_ARCCSC, FUNCTION_ARCCSCH, FUNCTION_ARCSEC, FUNCTION_ARCSECH, FUNCTION_ARCSIN, FUNCTION_ARCSINH, FUNCTION_ARCTAN, FUNCTION_ARCTANH, FUNCTION_CEILING, FUNCTION_COS, FUNCTION_COSH, FUNCTION_COT, FUNCTION_COTH, FUNCTION_CSC, FUNCTION_CSCH, FUNCTION_DELAY, FUNCTION_EXP, FUNCTION_FACTORIAL, FUNCTION_FLOOR, FUNCTION_LN, FUNCTION_LOG, FUNCTION_MAX, FUNCTION_MIN, FUNCTION_QUOTIENT, FUNCTION_PIECEWISE, FUNCTION_POWER, FUNCTION_RATEOF, FUNCTION_REM, FUNCTION_ROOT, FUNCTION_SEC, FUNCTION_SECH, FUNCTION_SELECTOR, FUNCTION_SIN, FUNCTION_SINH, FUNCTION_TAN, FUNCTION_TANH, LAMBDA, LOGICAL_AND, LOGICAL_IMPLIES, LOGICAL_NOT, LOGICAL_OR, LOGICAL_XOR, MINUS, NAME, NAME_AVOGADRO, NAME_TIME, NUMBER, PLUS, POWER, PRODUCT, QUALIFIER_BVAR, QUALIFIER_DEGREE, QUALIFIER_LOGBASE, RATIONAL, REAL_E, RELATIONAL_EQ, RELATIONAL_GEQ, RELATIONAL_GT, RELATIONAL_LEQ, RELATIONAL_LT, RELATIONAL_NEQ, SEMANTICS, SUM, TIMES, UNKNOWN, VECTOR;
  };

  /**
   * Node types
   */
  public static enum VariableType {
    SPECIES, REACTION, VARIABLE, NONE
  };

  /**
   * Node types
   */
  public static enum IndexType {
    COMPARTMENT, VARIABLE
  };

  private final Type type;
  private HashSet<Integer> deletedElements;
  private String metaId;

  protected Map<String, ArrayDimensionNode> mapOfDimensions;
  protected List<ArrayDimensionNode> listOfDimensions;
  protected String name;
  protected VariableType varType;
  protected Map<IndexType, List<HierarchicalNode>> indexMap;

  AbstractHierarchicalNode(Type type) {
    this.type = type;
    varType = VariableType.NONE;
  }

  AbstractHierarchicalNode(AbstractHierarchicalNode copy) {
    this.type = copy.type;
    this.varType = VariableType.NONE;
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
  public void addDimension(String dimensionId, int arrayDimension, String size) {
    if (listOfDimensions == null) {
      listOfDimensions = new ArrayList<>();
    }

    ArrayDimensionNode dimensionNode = new ArrayDimensionNode(size);

    dimensionNode.setState(new ValueState());

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
   * Gets the type of the node.
   *
   * @return the node type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Checks if the node is of type array.
   *
   * @return if the node is boolean.
   */
  public boolean isArray() {
    return listOfDimensions != null;
  }

  /**
   * Checks if the node is of type boolean.
   *
   * @return if the node is boolean.
   */
  public boolean isBoolean() {
    return type == Type.CONSTANT_FALSE || type == Type.CONSTANT_TRUE || isLogical() || isRelational();
  }

  /**
   * Checks if the node is of type logical.
   *
   * @return if the node is logical.
   */
  public boolean isLogical() {
    return type.toString().startsWith("LOGICAL_");
  }

  /**
   * Checks if the node is of type name.
   *
   * @return if the node is name.
   */
  public boolean isName() {
    return (type == Type.NAME) || (type == Type.NAME_TIME) || (type == Type.NAME_AVOGADRO);
  }

  /**
   * Checks if the node is of type constant.
   *
   * @return if the node is a constant.
   */
  public boolean isConstant() {
    return type.toString().startsWith("CONSTANT") || type == Type.NAME_AVOGADRO;
  }

  /**
   * Checks if the node is of type relational.
   *
   * @return if the node is relation.
   */
  public boolean isRelational() {
    return type == Type.RELATIONAL_EQ || type == Type.RELATIONAL_GEQ || type == Type.RELATIONAL_GT || type == Type.RELATIONAL_LEQ || type == Type.RELATIONAL_LT || type == Type.RELATIONAL_NEQ;
  }

  /**
   * Checks if the node is of type number.
   *
   * @return if the node is a number.
   */
  public boolean isNumber() {
    return type == Type.NUMBER;
  }

  /**
   * Checks if the node is of type operator.
   *
   * @return if the node is operator.
   */
  public boolean isOperator() {
    return type == Type.PLUS || type == Type.MINUS || type == Type.TIMES || type == Type.DIVIDE || type == Type.POWER;
  }

  /**
   * Checks if the node is of type time.
   *
   * @return if the node is time.
   */
  public boolean isTime() {
    return type == Type.NAME_TIME;
  }

  /**
   * Checks if the node is of type species.
   *
   * @return if the node is species.
   */
  public boolean isSpecies() {
    return varType == VariableType.SPECIES;
  }

  /**
   * Checks if the node is of type reaction.
   *
   * @return if the node is reaction.
   */
  public boolean isReaction() {
    return varType == VariableType.REACTION;
  }

  /**
   * Sets the meta id of the node.
   *
   * @param metaId
   *          - the new value of the metaid
   */
  public void setMetaId(String metaId) {
    this.metaId = metaId;
  }

  /**
   * Gets the meta id of the node.
   *
   * @return the meta id.
   */
  public String getMetaId() {
    return metaId;
  }

  /**
   * Gets the name of the node.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the name of the node.
   *
   * @return the name.
   */
  public String getDimensionedName(int index) {

    if (listOfDimensions != null) {
      String[] dimensions = new String[listOfDimensions.size()];
      int n = listOfDimensions.size();
      for (int i = listOfDimensions.size() - 1; i >= 0; i--) {
        dimensions[n - i - 1] = String.valueOf((int) listOfDimensions.get(i).getValue(index));
      }

      return String.join("__", dimensions);
    }

    return null;
  }

  /**
   * Sets the name of the node.
   *
   * @param name
   *          - the name of the node.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Mark an element from the given submodel index as deleted.
   *
   * @param index
   *          - the index of the model.
   */
  public void deleteElement(int index) {
    if (deletedElements == null) {
      this.deletedElements = new HashSet<>();
    }
    this.deletedElements.add(index);
  }

  /**
   * Checks if the element has been deleted.
   *
   * @param index
   *          - the index of the model.
   * @return true if element is deleted.
   */
  public boolean isDeleted(int index) {
    return deletedElements != null ? deletedElements.contains(index) : false;
  }

  public List<ArrayDimensionNode> getListOfDimensions() {
    return listOfDimensions;
  }
}
