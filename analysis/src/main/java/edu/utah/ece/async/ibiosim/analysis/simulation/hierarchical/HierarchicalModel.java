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
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.DeletionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ReplacementNode;
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

  private final String ID;
  protected int index;
  private final FunctionNode propensity;
  private ModelType type;

  private List<FunctionNode> assigmentnRules;
  private List<ConstraintNode> constraints;
  private List<EventNode> events;
  private List<FunctionNode> initialAssignments;
  private List<FunctionNode> rateRules;
  private List<SpeciesConcentration> initialConcentrations;
  private List<ReactionNode> reactions;
  private List<VariableNode> variables;
  private List<VariableNode> constants;

  private Map<String, HierarchicalNode> idToNode;
  private Map<String, HierarchicalNode> metaidToNode;
  private Map<String, HierarchicalModel> idToSubmodel;

  private List<ReplacementNode> replacements;
  private List<DeletionNode> deletions;

  public HierarchicalModel(String submodelID, int index) {
    this.ID = submodelID;
    this.index = index;

    this.idToNode = new HashMap<>();
    this.metaidToNode = new HashMap<>();

    this.variables = new ArrayList<>();
    this.constants = new ArrayList<>();
    this.events = new LinkedList<>();
    this.constraints = new ArrayList<>();
    this.reactions = new ArrayList<>();
    this.replacements = new ArrayList<>();
    this.deletions = new ArrayList<>();

    this.propensity = new FunctionNode(new VariableNode("propensity", StateType.SCALAR), new HierarchicalNode(Type.PLUS));
  }

  /**
   * Adds an assignment rule.
   *
   * @param node
   *          - the assignment rule.
   */
  public void addAssignRule(FunctionNode node) {
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

  public void addDeletion(DeletionNode node) {
    deletions.add(node);
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
  public void addInitAssignment(FunctionNode node) {
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
   *
   * @param node
   */
  public void addReplacement(ReplacementNode node) {
    replacements.add(node);
  }

  public void addRateRule(FunctionNode node) {
    if (rateRules == null) {
      rateRules = new ArrayList<>();
    }
    rateRules.add(node);
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

  /**
   * Adds a constant to the model.
   *
   * @param node
   *          - variable node.
   */
  public void addConstant(VariableNode node) {
    constants.add(node);
  }

  /**
   * Compute the total model propensity.
   *
   * @return if the propensity has changed.
   */
  public boolean computePropensities() {
    boolean hasChanged = false;
    for (ReactionNode node : reactions) {
      hasChanged = hasChanged | node.computePropensity(index);
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
   * Initializes the model as a copy of another model.
   *
   */
  public void initializeFromCopy(HierarchicalModel copy) {

    int copyIndex = copy.index;

    this.type = copy.type;
    this.assigmentnRules = copy.assigmentnRules;
    this.constraints = copy.constraints;
    this.events = copy.events;
    this.initialAssignments = copy.initialAssignments;
    this.rateRules = copy.rateRules;

    this.idToNode = copy.idToNode;
    this.metaidToNode = copy.metaidToNode;
    this.replacements = copy.replacements;
    this.deletions = copy.deletions;

    this.reactions = copy.reactions;
    this.variables = copy.variables;
    this.constants = copy.constants;

    for (HierarchicalNode variable : variables) {
      HierarchicalState state = variable.getState();
      HierarchicalState copyState = state.getChild(copyIndex);
      state.addState(index, copyState.clone());
    }

    for (HierarchicalNode reaction : reactions) {
      HierarchicalState state = reaction.getState();
      HierarchicalState copyState = state.getChild(copyIndex);
      state.addState(index, copyState.clone());
    }

    for (HierarchicalNode constant : constants) {
      HierarchicalState state = constant.getState();
      HierarchicalState copyState = state.getChild(copyIndex);
      state.addState(index, copyState.clone());
    }

    if (copy.initialConcentrations != null) {
      this.initialConcentrations = new ArrayList<>();
      for (int i = 0; i < copy.initialConcentrations.size(); i++) {
        SpeciesConcentration concentration = copy.initialConcentrations.get(i);
        initialConcentrations.add(new SpeciesConcentration(concentration, index));
      }
    }
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
  public List<FunctionNode> getListOfAssignmentRules() {
    return assigmentnRules;
  }

  /**
   *
   * @return
   */
  public List<VariableNode> getListOfConstants() {
    return constants;
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
   *
   * @return
   */
  public List<DeletionNode> getListOfDeletions() {
    return deletions;
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
  public List<FunctionNode> getListOfInitialAssignments() {
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
   * Gets the list of assignment rules.
   *
   * @return the list of assignment rules.
   */
  public List<FunctionNode> getListOfRateRules() {
    return rateRules;
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
   *
   * @return
   */
  public List<ReplacementNode> getListOfReplacements() {
    return replacements;
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
  public FunctionNode getPropensity() {
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