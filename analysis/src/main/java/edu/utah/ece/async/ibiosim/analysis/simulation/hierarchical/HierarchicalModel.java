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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ConstraintNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.Function;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.SpeciesConcentration;

/**
 * This class represents an SBML model.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class HierarchicalModel {

  public static enum ModelType {
    HSSA, HODE, HFBA, NONE;
  }

  private String ID;
  protected int index;
  private Function propensity;
  private ModelType type;

  private List<Function> assigmentnRules;
  private List<ConstraintNode> constraints;
  private List<EventNode> events;
  private List<Function> initialAssignments;
  private List<SpeciesConcentration> initialConcentrations;
  private List<ReactionNode> reactions;
  private List<VariableNode> variables;

  private Map<String, HierarchicalNode> idToNode;
  private Map<String, HierarchicalNode> metaidToNode;
  private Map<String, HierarchicalModel> idToSubmodel;

  public HierarchicalModel(HierarchicalModel state) {
    // TODO: implement this
  }

  public HierarchicalModel(String submodelID, int index) {
    this.ID = submodelID;
    this.index = index;

    this.idToNode = new HashMap<>();
    this.metaidToNode = new HashMap<>();

    this.variables = new ArrayList<>();
    this.events = new LinkedList<>();
    this.constraints = new ArrayList<>();
    this.reactions = new ArrayList<>();
    this.propensity = new Function(new VariableNode("propensity", StateType.SCALAR), new HierarchicalNode(Type.PLUS));
  }

  /**
   * Adds an assignment rule.
   *
   * @param node
   *          - the assignment rule.
   */
  public void addAssignRule(Function node) {
    if (assigmentnRules == null) {
      assigmentnRules = new ArrayList<>();
    }
    assigmentnRules.add(node);
  }

  /**
   * Adds a constraint to the model.
   *
   * @param id
   *          - the id of the constraint.
   * @param node
   *          - the constraint node.
   */
  public void addConstraint(String id, HierarchicalNode node) {
    ConstraintNode constraintNode = new ConstraintNode(id, node);
    constraints.add(constraintNode);
  }

  /**
   * Add event node to the model.
   *
   * @param node
   *          - the event node.
   */
  public void addEvent(EventNode node) {
    events.add(node);
  }

  /**
   * Adds an initial assignment.
   *
   * @param node
   *          - the assignment node.
   */
  public void addInitAssignment(Function node) {
    if (initialAssignments == null) {
      initialAssignments = new ArrayList<>();
    }
    initialAssignments.add(node);
  }

  /**
   * Adds an initial assignment to compute the initial concentration of a species.
   *
   * @param node
   *          - the assignment node.
   */
  public void addInitialConcentration(SpeciesNode species, double concentration, int index) {
    if (initialConcentrations == null) {
      initialConcentrations = new ArrayList<>();
    }
    initialConcentrations.add(new SpeciesConcentration(species, concentration, index));
  }

  /**
   * Maps a variable id to its corresponding node.
   *
   * @param variable
   *          - the variable id.
   * @param node
   *          - the variable node.
   */
  public void addMappingNode(String variable, HierarchicalNode node) {
    idToNode.put(variable, node);
  }

  /**
   * Maps a variable metaid to its corresponding node.
   *
   * @param metaId
   *          - the variable meta id.
   * @param node
   *          - the variable node.
   */
  public void addMappingNodeByMetaId(String metaId, HierarchicalNode node) {
    metaidToNode.put(metaId, node);
  }

  /**
   * Adds a reaction to the model.
   *
   * @param node
   *          - the reaction node.
   */
  public void addReaction(ReactionNode node) {
    reactions.add(node);
    propensity.getMath().addChild(node);
    idToNode.put(node.getName(), node);
  }

  /**
   * Adds a {@link HierarchicalModel} as a submodel.
   *
   * @param submodel
   *          - a hierarchical model.
   */
  public void addSubmodel(HierarchicalModel submodel) {
    if (idToSubmodel == null) {
      idToSubmodel = new HashMap<>();
    }
    idToSubmodel.put(submodel.getID(), submodel);
  }

  /**
   * Adds a variable to the model.
   *
   * @param node
   *          - variable node.
   */
  public void addVariable(VariableNode node) {
    variables.add(node);
  }

  @Override
  public HierarchicalModel clone() {
    return new HierarchicalModel(this);
  }

  /**
   * Compute the total model propensity.
   *
   * @return if the propensity has changed.
   */
  public boolean computePropensities() {
    boolean hasChanged = false;
    for (ReactionNode node : reactions) {
      double oldValue = node.getValue(index);
      node.computePropensity(index);
      double newValue = node.getValue(index);

      hasChanged = hasChanged | oldValue != newValue;
    }

    return hasChanged;
  }

  /**
   * Checks if the model has a variable with the given id.
   *
   * @param variable
   *          - the id of the variable.
   * @return true if the model has a variable with the given id. False otherwise.
   */
  public boolean containsNode(String variable) {
    return idToNode.containsKey(variable);
  }

  /**
   * Checks if the model has a submodel by id.
   *
   * @param id
   *          - the id of the submodel.
   * @return true if the model contains the submodel. False otherwise.
   */
  public boolean containsSubmodel(String id) {
    return idToSubmodel != null ? idToSubmodel.containsKey(id) : false;
  }

  /**
   * Creates an event and adds to the model.
   *
   * @param triggerNode
   *          - the trigger node.
   * @return the created event.
   */
  public EventNode createEvent(HierarchicalNode triggerNode) {
    EventNode node = new EventNode(triggerNode);
    addEvent(node);
    return node;
  }

  /**
   * Creates a reaction and adds to the model.
   *
   * @param variable
   *          - the id of the reaction.
   *
   * @return the created reaction node.
   */
  public ReactionNode createReaction(String variable) {
    ReactionNode node = new ReactionNode(variable);
    addReaction(node);
    return node;
  }

  /**
   * Gets the id of the model.
   *
   * @return the id.
   */
  public String getID() {
    return ID;
  }

  /**
   * Gets the index of the model.
   *
   * @return the index of the model.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Gets the list of assignment rules.
   *
   * @return the list of assignment rules.
   */
  public List<Function> getListOfAssignmentRules() {
    return assigmentnRules;
  }

  /**
   * Gets the list of constraints of the model.
   *
   * @return the list of constraints.
   */
  public List<ConstraintNode> getListOfConstraints() {
    return constraints;
  }

  /**
   * Gets the list of events of the model.
   *
   * @return the list of events.
   */
  public List<EventNode> getListOfEvents() {
    return events;
  }

  /**
   * Gets the list of initial assignments.
   *
   * @return the list of initial assignments.
   */
  public List<Function> getListOfInitialAssignments() {
    return initialAssignments;
  }

  /**
   * Gets the list of initial concentrations.
   *
   * @return the list of initial concentrations.
   */
  public List<SpeciesConcentration> getListOfInitialConcentrations() {
    return initialConcentrations;
  }

  /**
   * Gets the list of reactions of the model.
   *
   * @return the list of reactions.
   */
  public List<ReactionNode> getListOfReactions() {
    return reactions;
  }

  /**
   * Gets the list of variables.
   *
   * @return the list of variables.
   */
  public List<VariableNode> getListOfVariables() {
    return variables;
  }

  /**
   * Gets the representation type of the model.
   *
   * @return the representation type of the model.
   */
  public ModelType getModelType() {
    return type;
  }

  /**
   * Gets a node from an id.
   *
   * @param variable
   *          - the id of the node.
   * @return the node with the given id.
   */
  public HierarchicalNode getNode(String variable) {
    return idToNode.get(variable);
  }

  /**
   * Gets a node from a meta id.
   *
   * @param variable
   *          - the id of the node.
   * @return the node with the given id.
   */
  public HierarchicalNode getNodeByMetaId(String metaid) {
    return metaidToNode.get(metaid);
  }

  /**
   * Gets the total propensity of the model.
   *
   * @return the total model propensity.
   */
  public Function getPropensity() {
    return propensity;
  }

  /**
   * Gets a {@link HierarchicalModel} by id.
   *
   * @param id
   *          - the id of the submodel.
   * @return the submodel.
   */
  public HierarchicalModel getSubmodel(String id) {
    HierarchicalModel submodel = null;
    if (idToSubmodel != null) {
      submodel = idToSubmodel.get(id);
    }
    return submodel;
  }

  /**
   * Gets a map of id to variable nodes.
   *
   * @return the map of variable nodes.
   */
  public Map<String, HierarchicalNode> getVariableToNodeMap() {
    return idToNode;
  }

  /**
   * Remove a submodel by id.
   *
   * @param id
   *          - the id of the submodel.
   */
  public void removeSubmodel(String id) {
    idToSubmodel.remove(id);
  }

  /**
   * Sets the representation type of the model
   *
   * @param type
   *          - the representation type.
   */
  public void setModelType(ModelType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "ModelState [ID=" + getID() + "]";
  }

}