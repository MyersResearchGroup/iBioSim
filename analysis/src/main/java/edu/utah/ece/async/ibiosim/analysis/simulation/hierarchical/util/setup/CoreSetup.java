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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Trigger;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.LocalVariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesReferenceNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.DenseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.EventState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.SparseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.RateSplitterInterpreter;

/**
 * Sets up the hierarchical simulator by handling the core SBML components.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class CoreSetup {

  static void initializeCore(HierarchicalSimulation sim, List<ModelContainer> listOfContainers, VariableNode time, VectorWrapper wrapper, MathInterpreter mathInterpreter) throws IOException {
    Map<ModelContainer, HierarchicalModel> initializedModel = new HashMap<>();

    for (ModelContainer container : listOfContainers) {
      if (initializedModel.containsKey(container)) {
        HierarchicalModel model = container.getHierarchicalModel();
        HierarchicalModel existingModel = initializedModel.get(container);
        model.initializeFromCopy(existingModel);
      } else {
        ReplacementSetup.setupDeletion(container);
        container.getHierarchicalModel().addMappingNode("_time", time);
        setupParameters(sim, container, wrapper, mathInterpreter);
        setupCompartments(sim, container, wrapper, mathInterpreter);
        setupSpecies(sim, container, wrapper, mathInterpreter);
        setupReactions(sim, container, wrapper, mathInterpreter);
        setupEvents(sim, container, wrapper, mathInterpreter);
        setupConstraints(sim, container, mathInterpreter);
        setupRules(sim, container, mathInterpreter);
        setupInitialAssignments(sim, container, mathInterpreter);
        initializedModel.put(container, container.getHierarchicalModel());
      }
    }
  }

  static HierarchicalState createState(StateType type, VectorWrapper wrapper) {
    HierarchicalState state = null;

    if (type == StateType.VECTOR) {
      state = new VectorState(wrapper);
    } else if (type == StateType.SPARSE) {
      state = new SparseState();
    } else if (type == StateType.DENSE) {
      state = new DenseState();
    } else if (type == StateType.SCALAR) {
      state = new ValueState();
    }
    return state;
  }

  private static void setupCompartments(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();

    int index = hierarchicalModel.getIndex();

    for (Compartment compartment : model.getListOfCompartments()) {
      String compartmentID = compartment.getId();
      VariableNode node = new VariableNode(compartmentID);
      node.setState(createState(sim.getCollectionType(), wrapper));
      hierarchicalModel.addMappingNode(compartmentID, node);

      ReplacementSetup.setupVariableReplacement(sim, compartment, wrapper, container);
      ArraysSetup.setupArrays(hierarchicalModel, container, compartment, node, mathInterpreter);

      if (node.isArray()) {
        node.getState().addState(index, createState(StateType.DENSE, wrapper));
      } else if (!compartment.isConstant()) {
        node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
      } else {
        node.getState().addState(index, createState(StateType.SCALAR, wrapper));
      }

      if (!compartment.isConstant()) {
        hierarchicalModel.addVariable(node);
      } else {
        hierarchicalModel.addConstant(node);
      }

      if (Double.isNaN(compartment.getSize())) {
        node.getState().getChild(index).setInitialValue(1);
      } else {
        node.getState().getChild(index).setInitialValue(compartment.getSize());
      }

    }
  }

  private static void setupParameters(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();

    VariableNode node;

    for (Parameter parameter : model.getListOfParameters()) {

      String parameterId = parameter.getId();
      node = new VariableNode(parameterId);
      hierarchicalModel.addMappingNode(parameter.getId(), node);
      node.setState(createState(sim.getCollectionType(), wrapper));

      ReplacementSetup.setupVariableReplacement(sim, parameter, wrapper, container);
      ArraysSetup.setupArrays(hierarchicalModel, container, parameter, node, mathInterpreter);

      if (node.isArray()) {
        node.getState().addState(index, createState(StateType.DENSE, wrapper));
      } else if (!parameter.isConstant()) {
        node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
      } else {
        node.getState().addState(index, createState(StateType.SCALAR, wrapper));
      }

      if (!parameter.isConstant()) {
        hierarchicalModel.addVariable(node);
      } else {
        hierarchicalModel.addConstant(node);
      }

      if (parameter.isSetValue()) {
        node.getState().getChild(index).setInitialValue(parameter.getValue());
      }

    }
  }

  /**
   * puts species-related information into data structures
   *
   */
  private static void setupSpecies(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    for (Species species : model.getListOfSpecies()) {
      String speciesId = species.getId();
      SpeciesNode node = new SpeciesNode(speciesId);
      hierarchicalModel.addMappingNode(speciesId, node);

      ReplacementSetup.setupVariableReplacement(sim, species, wrapper, container);
      ArraysSetup.setupArrays(hierarchicalModel, container, species, node, mathInterpreter);

      boolean isBoundary = species.getBoundaryCondition();
      boolean isOnlySubstance = species.getHasOnlySubstanceUnits();
      node.setState(createState(sim.getCollectionType(), wrapper));

      if (node.isArray()) {
        node.getState().addState(index, createState(StateType.DENSE, wrapper));
      } else if (!species.isConstant()) {
        node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
      } else {
        node.getState().addState(index, createState(StateType.SCALAR, wrapper));
      }

      if (!species.isConstant()) {
        hierarchicalModel.addVariable(node);
      } else {
        hierarchicalModel.addConstant(node);
      }

      node.getState().getChild(index).setBoundaryCondition(isBoundary);
      node.getState().getChild(index).setHasOnlySubstance(isOnlySubstance);

      VariableNode compartment = (VariableNode) hierarchicalModel.getNode(species.getCompartment());
      node.setCompartment(compartment);

      if (species.isSetInitialAmount()) {
        node.getState().getChild(index).setInitialValue(species.getInitialAmount());
      } else if (species.isSetInitialConcentration()) {
        hierarchicalModel.addInitialConcentration(node, species.getInitialConcentration(), index);
      }
    }
  }

  private static void setupProduct(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String productID, SpeciesReference product, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    String id = product.isSetId() ? product.getId() : null;
    String metaid = product.isSetMetaId() ? product.getMetaId() : null;
    int index = hierarchicalModel.getIndex();

    double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

    SpeciesNode species = (SpeciesNode) hierarchicalModel.getNode(product.getSpecies());
    SpeciesReferenceNode node = new SpeciesReferenceNode(species);
    node.inheritDimensionsFromParent(reaction);

    ReplacementSetup.setupObjectReplacement(sim, product, id, metaid, container);
    ArraysSetup.setupArrays(hierarchicalModel, container, product, node, mathInterpreter);

    node.setState(createState(sim.getCollectionType(), wrapper));

    if (node.isArray()) {
      node.getState().addState(index, createState(StateType.DENSE, wrapper));
    } else if (!product.isConstant()) {
      node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
    } else {
      node.getState().addState(index, createState(StateType.SCALAR, wrapper));
    }

    if (!product.isConstant()) {
      hierarchicalModel.addVariable(node);
    } else {
      hierarchicalModel.addConstant(node);
    }

    node.getState().getChild(hierarchicalModel.getIndex()).setInitialValue(stoichiometryValue);

    reaction.addProduct(node);

    if (id != null) {
      node.setName(product.getId());
      if (!product.getConstant()) {
        hierarchicalModel.addVariable(node);
        node.getState().getChild(index).setConstant(false);
      }
      hierarchicalModel.addMappingNode(id, node);
    }
  }

  private static void setupReactant(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String reactantID, SpeciesReference reactant, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    String id = reactant.isSetId() ? reactant.getId() : null;
    String metaid = reactant.isSetMetaId() ? reactant.getMetaId() : null;
    int index = hierarchicalModel.getIndex();
    double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();

    SpeciesNode species = (SpeciesNode) hierarchicalModel.getNode(reactant.getSpecies());
    SpeciesReferenceNode node = new SpeciesReferenceNode(species);
    node.inheritDimensionsFromParent(reaction);
    ReplacementSetup.setupObjectReplacement(sim, reactant, id, metaid, container);
    ArraysSetup.setupArrays(hierarchicalModel, container, reactant, node, mathInterpreter);

    node.setState(createState(sim.getCollectionType(), wrapper));

    if (node.isArray()) {
      node.getState().addState(index, createState(StateType.DENSE, wrapper));
    } else if (!reactant.isConstant()) {
      node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
    } else {
      node.getState().addState(index, createState(StateType.SCALAR, wrapper));
    }

    if (!reactant.isConstant()) {
      hierarchicalModel.addVariable(node);
    } else {
      hierarchicalModel.addConstant(node);
    }

    node.getState().getChild(hierarchicalModel.getIndex()).setInitialValue(stoichiometryValue);
    reaction.addReactant(node);

    if (id != null) {
      node.setName(reactant.getId());
      if (!reactant.getConstant()) {
        node.getState().getChild(index).setConstant(false);
        hierarchicalModel.addVariable(node);
      }
      hierarchicalModel.addMappingNode(reactant.getId(), node);
    }
  }

  private static void setupConstraints(HierarchicalSimulation sim, ModelContainer container, MathInterpreter mathInterpreter) {

    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    for (Constraint constraint : model.getListOfConstraints()) {
      String id = constraint.isSetId() ? constraint.getId() : null;
      String metaid = constraint.isSetMetaId() ? constraint.getMetaId() : null;
      String name = id != null ? id : metaid != null ? metaid : constraint.toString();
      ReplacementSetup.setupObjectReplacement(sim, constraint, id, metaid, container);

      ASTNode math = constraint.getMath();

      if (math != null) {
        HierarchicalNode constraintNode = mathInterpreter.parseASTNode(math, null, hierarchicalModel.getVariableToNodeMap(), null, index);
        hierarchicalModel.addConstraint(name, constraintNode);
      }
    }
  }

  private static void setupEventAssignments(HierarchicalSimulation sim, ModelContainer container, EventNode eventNode, Event event, MathInterpreter mathInterpreter) {
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
      String id = eventAssignment.getId();
      String metaid = eventAssignment.getMetaId();
      if (eventAssignment.isSetMath()) {

        FunctionNode eventAssignmentNode = new FunctionNode();
        eventAssignmentNode.inheritDimensionsFromParent(eventNode);
        ReplacementSetup.setupObjectReplacement(sim, eventAssignment, id, metaid, container);
        ArraysSetup.setupArrays(hierarchicalModel, container, eventAssignment, eventAssignmentNode, mathInterpreter);

        ASTNode math = eventAssignment.getMath();
        HierarchicalNode variableNode = hierarchicalModel.getNode(eventAssignment.getVariable());
        HierarchicalNode assignmentNode = mathInterpreter.parseASTNode(math, null, hierarchicalModel.getVariableToNodeMap(), eventAssignmentNode.getDimensionMapping(), index);
        eventNode.addEventAssignment(eventAssignmentNode);
        eventAssignmentNode.setVariable(variableNode);
        eventAssignmentNode.setMath(assignmentNode);

        if (id != null) {
          hierarchicalModel.addMappingNode(id, assignmentNode);
        }
        if (metaid != null) {
          hierarchicalModel.addMappingNodeByMetaId(metaid, eventAssignmentNode);
        }
      }

    }
  }

  /**
   * puts event-related information into data structures
   */
  private static void setupEvents(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    hierarchicalModel.getVariableToNodeMap();
    int index = hierarchicalModel.getIndex();
    for (Event event : model.getListOfEvents()) {

      String id = event.getId();
      String metaid = event.getMetaId();
      ReplacementSetup.setupObjectReplacement(sim, event, id, metaid, container);

      if (event.isSetTrigger() && event.getTrigger().isSetMath()) {

        Trigger trigger = event.getTrigger();
        ASTNode triggerMath = trigger.getMath();

        EventNode node = hierarchicalModel.createEvent();
        ArraysSetup.setupArrays(hierarchicalModel, container, event, node, mathInterpreter);

        HierarchicalNode triggerNode = mathInterpreter.parseASTNode(triggerMath, null, hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), index);
        node.setTrigger(triggerNode);
        sim.setHasEvents(true);

        node.setState(createState(sim.getCollectionType(), wrapper));

        if (node.isArray()) {
          node.getState().addState(index, createState(StateType.DENSE, wrapper));
        } else {
          node.getState().addState(index, createState(StateType.SCALAR, wrapper));
        }

        EventState eventState = node.addEventState(index);
        if (!trigger.getInitialValue()) {
          eventState.setMaxDisabledTime(0);
        }
        node.getState().getChild(hierarchicalModel.getIndex()).setInitialValue(0);

        boolean useValuesFromTrigger = event.getUseValuesFromTriggerTime();
        boolean isPersistent = trigger.isSetPersistent() ? trigger.getPersistent() : false;
        boolean initValue = trigger.isSetInitialValue() ? trigger.getInitialValue() : false;
        node.getState().getChild(index).setUseTriggerValue(useValuesFromTrigger);
        node.getState().getChild(index).setPersistent(isPersistent);
        eventState.setInitialTrue(initValue);

        if (id != null) {
          hierarchicalModel.addMappingNode(id, node);
        }
        if (event.isSetPriority()) {
          Priority priority = event.getPriority();
          ASTNode math = priority.getMath();
          if (math != null) {
            HierarchicalNode priorityMath = mathInterpreter.parseASTNode(math, null, hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), index);
            FunctionNode priorityNode = new FunctionNode();
            node.setPriority(priorityNode);
            priorityNode.setMath(priorityMath);
            if (priority.isSetMetaId()) {
              hierarchicalModel.addMappingNodeByMetaId(priority.getMetaId(), priorityNode);
            }
          }
        }
        if (event.isSetDelay()) {
          Delay delay = event.getDelay();
          ASTNode math = delay.getMath();
          if (math != null) {
            HierarchicalNode delayMath = mathInterpreter.parseASTNode(math, null, hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), index);
            FunctionNode delayNode = new FunctionNode();
            node.setDelay(delayNode);
            delayNode.setMath(delayMath);
            if (delay.isSetMetaId()) {
              hierarchicalModel.addMappingNodeByMetaId(delay.getMetaId(), delayNode);
            }
          }
        }
        setupEventAssignments(sim, container, node, event, mathInterpreter);
      }
    }
  }

  private static void setupInitialAssignments(HierarchicalSimulation sim, ModelContainer container, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    for (InitialAssignment initAssignment : model.getListOfInitialAssignments()) {
      String id = initAssignment.isSetId() ? initAssignment.getId() : null;
      String metaid = initAssignment.isSetMetaId() ? initAssignment.getMetaId() : null;
      if (initAssignment.isSetMath()) {
        String variable = initAssignment.getVariable();

        FunctionNode node = new FunctionNode();
        if (initAssignment.isSetMetaId()) {
          ReplacementSetup.setupObjectReplacement(sim, initAssignment, id, metaid, container);
          hierarchicalModel.addMappingNodeByMetaId(metaid, node);
        }
        ArraysSetup.setupArrays(hierarchicalModel, container, initAssignment, node, mathInterpreter);

        HierarchicalNode variableNode = hierarchicalModel.getNode(variable);
        node.setVariable(variableNode);

        ASTNode math = initAssignment.getMath();
        HierarchicalNode initAssignNode = mathInterpreter.parseASTNode(math, null, hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), index);
        node.setMath(initAssignNode);
        variableNode.getState().getChild(index).setHasInitRule(true);
        node.setIsInitAssignment(true);
        hierarchicalModel.addInitAssignment(node);

      }
    }

  }

  private static void setupReactions(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    boolean split = hierarchicalModel.getModelType() == ModelType.HSSA;
    for (Reaction reaction : model.getListOfReactions()) {
      String reactionId = reaction.getId();
      ReactionNode node = hierarchicalModel.createReaction(reactionId);

      ReplacementSetup.setupVariableReplacement(sim, reaction, wrapper, container);
      ArraysSetup.setupArrays(hierarchicalModel, container, reaction, node, mathInterpreter);

      for (SpeciesReference reactant : reaction.getListOfReactants()) {
        setupReactant(sim, container, node, reactant.getSpecies(), reactant, wrapper, mathInterpreter);
      }
      for (SpeciesReference product : reaction.getListOfProducts()) {
        setupProduct(sim, container, node, product.getSpecies(), product, wrapper, mathInterpreter);
      }

      node.setState(createState(sim.getCollectionType(), wrapper));

      if (node.isArray()) {
        node.getState().addState(index, createState(StateType.DENSE, wrapper));
      } else {
        node.getState().addState(index, createState(StateType.SCALAR, wrapper));
      }

      node.getState().getChild(hierarchicalModel.getIndex()).setInitialValue(0);

      KineticLaw kineticLaw = reaction.getKineticLaw();
      if (kineticLaw != null) {
        for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters()) {
          setupLocalParameters(sim, hierarchicalModel, localParameter, node, wrapper, mathInterpreter);
        }
        if (kineticLaw.isSetMath()) {
          ASTNode reactionFormula = kineticLaw.getMath();
          if (reaction.isReversible() && split) {
            ASTNode[] splitMath = splitMath(reactionFormula);
            if (splitMath == null) {
              HierarchicalNode math = mathInterpreter.parseASTNode(reactionFormula, node.getLocalParameters(), hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), hierarchicalModel.getIndex());
              node.setForwardRate(math);
            } else {
              HierarchicalNode forwardRate = mathInterpreter.parseASTNode(splitMath[0], node.getLocalParameters(), hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), hierarchicalModel.getIndex());
              node.setForwardRate(forwardRate);
              HierarchicalNode reverseRate = mathInterpreter.parseASTNode(splitMath[1], node.getLocalParameters(), hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), hierarchicalModel.getIndex());
              node.setReverseRate(reverseRate);
            }
          } else {
            HierarchicalNode math = mathInterpreter.parseASTNode(reactionFormula, node.getLocalParameters(), hierarchicalModel.getVariableToNodeMap(), node.getDimensionMapping(), hierarchicalModel.getIndex());
            node.setForwardRate(math);
          }
        }
      }
    }
  }

  private static void setupLocalParameters(HierarchicalSimulation sim, HierarchicalModel hierarchicalModel, LocalParameter localParameter, ReactionNode node, VectorWrapper wrapper, MathInterpreter mathInterpreter) {
    int index = hierarchicalModel.getIndex();
    String id = localParameter.getId();
    LocalVariableNode localParam = new LocalVariableNode(id);
    localParam.setState(createState(sim.getCollectionType(), wrapper));
    localParam.getState().addState(index, createState(StateType.SCALAR, wrapper));
    localParam.setValue(index, localParameter.getValue());
    node.addLocalParameter(id, localParam);

    hierarchicalModel.addConstant(localParam);
    if (localParameter.isSetMetaId()) {
      hierarchicalModel.addMappingNodeByMetaId(localParameter.getMetaId(), localParam);
    }

    HierarchicalNode globalParameter = hierarchicalModel.getNode(id);
    if (globalParameter != null) {
      localParam.setGlobalVariable(globalParameter);
    }
  }

  private static void setupRules(HierarchicalSimulation sim, ModelContainer container, MathInterpreter mathInterpreter) {
    Model model = container.getModel();
    HierarchicalModel hierarchicalModel = container.getHierarchicalModel();
    int index = hierarchicalModel.getIndex();
    Map<String, HierarchicalNode> variableToNodes = hierarchicalModel.getVariableToNodeMap();
    for (Rule rule : model.getListOfRules()) {
      String id = rule.isSetId() ? rule.getId() : null;
      String metaid = rule.isSetMetaId() ? rule.getMetaId() : null;
      if (rule.isAssignment()) {
        AssignmentRule assignRule = (AssignmentRule) rule;
        if (assignRule.isSetMath()) {
          FunctionNode node = new FunctionNode();
          if (assignRule.isSetMetaId()) {
            ReplacementSetup.setupObjectReplacement(sim, rule, id, metaid, container);
            hierarchicalModel.addMappingNodeByMetaId(metaid, node);
          }
          ArraysSetup.setupArrays(hierarchicalModel, container, assignRule, node, mathInterpreter);

          ASTNode math = assignRule.getMath();
          HierarchicalNode variableNode = variableToNodes.get(assignRule.getVariable());
          HierarchicalNode assignRuleNode = mathInterpreter.parseASTNode(math, null, variableToNodes, node.getDimensionMapping(), index);

          hierarchicalModel.addAssignRule(node);
          variableNode.getState().getChild(index).setHasRule(true);
          node.setVariable(variableNode);
          node.setMath(assignRuleNode);

        }
      } else if (rule.isRate()) {
        RateRule rateRule = (RateRule) rule;
        if (rateRule.isSetMath()) {
          ASTNode math = rateRule.getMath();
          VariableNode variableNode = (VariableNode) variableToNodes.get(rateRule.getVariable());

          FunctionNode node = new FunctionNode();
          ArraysSetup.setupArrays(hierarchicalModel, container, rateRule, node, mathInterpreter);

          hierarchicalModel.addRateRule(node);
          variableNode.getState().getChild(index).setHasRate(true);

          HierarchicalNode rateNode = mathInterpreter.parseASTNode(math, null, variableToNodes, node.getDimensionMapping(), index);

          node.setVariable(variableNode);
          node.setMath(rateNode);
          if (rateRule.isSetMetaId()) {
            ReplacementSetup.setupObjectReplacement(sim, rule, id, metaid, container);
            hierarchicalModel.addMappingNodeByMetaId(metaid, node);
          }
        }
      }

    }
  }

  private static ASTNode[] splitMath(ASTNode math) {
    ASTNode plus = new ASTNode(ASTNode.Type.PLUS);
    ASTNode minus = new ASTNode(ASTNode.Type.PLUS);
    ASTNode[] result = new ASTNode[] { plus, minus };
    List<ASTNode> nodes = RateSplitterInterpreter.parseASTNode(math);
    for (ASTNode node : nodes) {
      if (node.getType() == ASTNode.Type.MINUS) {
        minus.addChild(node.getChild(0));
      } else {
        plus.addChild(node);
      }
    }

    return plus.getChildCount() > 0 && minus.getChildCount() > 0 ? result : null;
  }
}
