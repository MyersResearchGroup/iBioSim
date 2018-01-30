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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesReferenceNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.VariableType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ModelContainer;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter.InterpreterType;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CoreSetup
{

  static void initializeCore(HierarchicalSimulation sim, ModelContainer container, StateType type, VectorWrapper wrapper, boolean split) throws IOException
  {
    setupParameters(sim, container, wrapper);
    setupCompartments(sim, container, wrapper);
    setupSpecies(sim, container, wrapper);
    setupReactions(sim, container, wrapper);
    setupEvents(sim, container, wrapper);
    setupConstraints(sim, container);
    setupRules(sim, container);
    setupInitialAssignments(sim, container);
  }

  private static void setupCompartments(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();

    int index = modelstate.getIndex();

    for (Compartment compartment : model.getListOfCompartments())
    {
      ReplacementSetup.setupReplacement(sim, compartment, wrapper,VariableType.VARIABLE, container);

      String printVariable = container.getPrefix() + compartment.getId();
      String compartmentID = compartment.getId();
      VariableNode node =  new VariableNode(compartmentID);


      //TODO: change this
      if(sim.getProperties().getSimulationProperties().getIntSpecies() != null && sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable))
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
      }
      else if(sim.getProperties().getSimulationProperties().getIntSpecies() == null)
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
      }
      
      node.createState(sim.getAtomicType(), wrapper);
      modelstate.addMappingNode(compartmentID, node);
      node.getState().addState(index, 0);

      if (Double.isNaN(compartment.getSize()))
      {
        node.getState().getState(index).setInitialValue(1);
      }
      else
      {
        node.getState().getState(index).setInitialValue(compartment.getSize());
      }
    }
  }

  /**
   * puts parameter-related information into data structures
   */
  private static void setupParameters(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();

    VariableNode node;

    for (Parameter parameter : model.getListOfParameters())
    {

      ReplacementSetup.setupReplacement(sim, parameter, wrapper,VariableType.VARIABLE, container);
      String parameterId = parameter.getId();
      node = new VariableNode(parameterId);
      modelstate.addMappingNode(parameter.getId(), node);
      node.createState(sim.getAtomicType(), wrapper);
      node.getState().addState(index, 0);

      String printVariable = container.getPrefix() + parameter.getId();


      //TODO: change this
      if(sim.getProperties().getSimulationProperties().getIntSpecies() != null && sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable))
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
      }

      if(sim.getProperties().getSimulationProperties().getIntSpecies() == null)
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
      }

      if(parameter.isSetValue())
      {
        node.getState().getState(index).setInitialValue(parameter.getValue());
      }

    }
  }

  /**
   * puts species-related information into data structures
   * 
   */
  private static void setupSpecies(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    boolean isConcentration = false;
    for (Species species : model.getListOfSpecies())
    {
      ReplacementSetup.setupReplacement(sim, species, wrapper,VariableType.SPECIES, container);
      String speciesId = species.getId();
      String printVariable = container.getPrefix() + speciesId;
      SpeciesNode node = new SpeciesNode(speciesId);
      modelstate.addMappingNode(speciesId, node);
      if(sim.getProperties().getSimulationProperties().getPrinter_track_quantity().equals("concentration"))
      {
        isConcentration = true;
      }


      if(sim.getProperties().getSimulationProperties().getIntSpecies() != null && sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable))
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), isConcentration);
      }

      if(sim.getProperties().getSimulationProperties().getIntSpecies() == null)
      {
        sim.addPrintVariable(printVariable, node, modelstate.getIndex(), isConcentration);
      }

      boolean isBoundary = species.getBoundaryCondition();
      boolean isOnlySubstance = species.getHasOnlySubstanceUnits();
      node.createState(sim.getAtomicType(), wrapper);

      node.getState().getState(index).setBoundaryCondition(isBoundary);
      node.getState().getState(index).setHasOnlySubstance(isOnlySubstance);

      VariableNode compartment = modelstate.getNode(species.getCompartment());
      node.setCompartment(compartment);

      if (species.isSetInitialAmount())
      {
        node.getState().getState(index).setInitialValue(species.getInitialAmount());
      }
      else if (species.isSetInitialConcentration())
      {
        HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
        initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
        initConcentration.addChild(compartment);
        FunctionNode functionNode = new FunctionNode(node, initConcentration);
        modelstate.addInitConcentration(functionNode);
        node.getState().getState(index).setHasAmountUnits(false);
      }
    }
  }



  private static void setupProduct(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String productID, SpeciesReference product, VectorWrapper wrapper)
  {
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    String id = product.isSetMetaId() ? product.getMetaId() : product.toString();

    ReplacementSetup.setupReplacement(sim, product, id, container);

    double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

    SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
    if(!product.isConstant())
    {
      speciesReferenceNode.createState(sim.getAtomicType(), wrapper);
    }
    else
    {
      speciesReferenceNode.createState(StateType.SCALAR, wrapper);
    }

    speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);

    SpeciesNode species = (SpeciesNode) modelstate.getNode(product.getSpecies());
    speciesReferenceNode.setSpecies(species);
    reaction.addProduct(speciesReferenceNode);

    if (product.isSetId() && product.getId().length() > 0)
    {
      speciesReferenceNode.setName(product.getId());
      if (!product.getConstant())
      {

        speciesReferenceNode.getState().getState(index).setIsVariableConstant(false);
        modelstate.addVariable(speciesReferenceNode );
      } 

      modelstate.addMappingNode(product.getId(), speciesReferenceNode);
    }
    species.addODERate(reaction, speciesReferenceNode);
    if(!species.getState().getState(index).hasRate())
    {
      species.getState().getState(index).setHasRate(true);
      modelstate.addVariable(species);
    }
  }

  private static void setupReactant(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String reactantID, SpeciesReference reactant, VectorWrapper wrapper)
  {
    HierarchicalModel modelstate = container.getHierarchicalModel();
    String id = reactant.isSetMetaId() ? reactant.getMetaId() : reactant.toString();
    int index = modelstate.getIndex();
    ReplacementSetup.setupReplacement(sim, reactant,id, container);

    double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
    SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
    if(!reactant.isConstant())
    {
      speciesReferenceNode.createState(sim.getAtomicType(), wrapper);
    }
    else
    {

      speciesReferenceNode.createState(StateType.SCALAR, wrapper);
    }
    speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);
    SpeciesNode species = (SpeciesNode) modelstate.getNode(reactant.getSpecies());
    speciesReferenceNode.setSpecies(species);
    reaction.addReactant(speciesReferenceNode);

    if (reactant.isSetId() && reactant.getId().length() > 0)
    {
      speciesReferenceNode.setName(reactant.getId());

      if (!reactant.getConstant())
      {
        speciesReferenceNode.getState().getState(index).setIsVariableConstant(false);
        modelstate.addVariable(speciesReferenceNode );
      }

      modelstate.addMappingNode(reactant.getId(), speciesReferenceNode);
    }
    species.subtractODERate(reaction, speciesReferenceNode);
    if(!species.getState().getState(index).hasRate())
    {
      species.getState().getState(index).setHasRate(true);
      modelstate.addVariable(species);

      if(wrapper != null)
      {

      }
    }
  }

  //
  //
  //

  private static void setupConstraints(HierarchicalSimulation sim, ModelContainer container)
  {

    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    for (Constraint constraint : model.getListOfConstraints())
    {

      String id = constraint.isSetMetaId() ? constraint.getMetaId() : constraint.toString();
      ReplacementSetup.setupReplacement(sim, constraint, id, container);

      if (modelstate.isDeletedByMetaId(constraint.getMetaId()))
      {
        continue;
      }

      ASTNode math = constraint.getMath();

      if(math != null)
      {
        HierarchicalNode constraintNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), InterpreterType.OTHER, index);  
        modelstate.addConstraint(id, constraintNode);
      }
    }
  }

  private static void setupEventAssignments(HierarchicalSimulation sim, ModelContainer container, EventNode eventNode, Event event)
  {
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    for (EventAssignment eventAssignment : event.getListOfEventAssignments())
    {
      String id = eventAssignment.isSetMetaId() ? eventAssignment.getMetaId() : eventAssignment.toString();
      ReplacementSetup.setupReplacement(sim, eventAssignment, id, container);
      if (eventAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(eventAssignment.getMetaId()) || !eventAssignment.isSetMath())
      {
        continue;
      }

      if(eventAssignment.isSetMath())
      {
        ASTNode math = eventAssignment.getMath();
        VariableNode variableNode = modelstate.getNode(eventAssignment.getVariable());

        HierarchicalNode assignmentNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), InterpreterType.ASSIGNMENT, index);
        assignmentNode = convertConcentrationUnits(variableNode, assignmentNode, index);
        FunctionNode eventAssignmentNode = new FunctionNode(variableNode, assignmentNode);
        eventNode.addEventAssignment(eventAssignmentNode);
      }

    }
  }

  /**
   * puts event-related information into data structures
   */
  private static void setupEvents(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    Map<String, VariableNode> variableToNodeMap = modelstate.getVariableToNodeMap();
    int index = modelstate.getIndex();
    for (Event event : model.getListOfEvents())
    {

      String id = event.isSetId() ? event.getId() : event.isSetMetaId() ? event.getMetaId() : event.toString();
      ReplacementSetup.setupReplacement(sim, event, id, container);

      if (modelstate.isDeletedBySId(event.getId()))
      {
        continue;
      }

      sim.setHasEvents(true);

      if(event.isSetTrigger() && event.getTrigger().isSetMath())
      {

        Trigger trigger = event.getTrigger();
        ASTNode triggerMath = trigger.getMath();
        HierarchicalNode triggerNode = MathInterpreter.parseASTNode(triggerMath, variableToNodeMap, InterpreterType.OTHER, index);

        EventNode node = modelstate.addEvent(triggerNode);
        node.addEventState(index);

        node.createState(sim.getAtomicType(), wrapper);
        node.getState().addState(index, 0);

        boolean useValuesFromTrigger = event.getUseValuesFromTriggerTime();
        boolean isPersistent = trigger.isSetPersistent() ? trigger.getPersistent() : false;
        boolean initValue = trigger.isSetInitialValue() ? trigger.getInitialValue() : false;
        node.getState().getState(index).setUseTriggerValue(useValuesFromTrigger);
        node.getState().getState(index).setPersistent(isPersistent);

        if (!initValue)
        {
          node.setMaxDisabledTime(index, 0);
          if (node.computeTrigger(modelstate.getIndex()))
          {
            node.setMinEnabledTime(index, 0);
          }
        }
        if (event.isSetPriority())
        {
          Priority priority = event.getPriority();
          if (!(priority.isSetMetaId() && modelstate.isDeletedByMetaId(priority.getMetaId())) && priority.isSetMath())
          {
            ASTNode math = priority.getMath();
            HierarchicalNode priorityNode = MathInterpreter.parseASTNode(math,  variableToNodeMap, InterpreterType.OTHER, index);
            node.setPriorityValue(priorityNode);
          }
        }
        if (event.isSetDelay())
        {
          Delay delay = event.getDelay();
          if (!(delay.isSetMetaId() && modelstate.isDeletedByMetaId(delay.getMetaId())) && delay.isSetMath())
          {
            ASTNode math = delay.getMath();
            HierarchicalNode delayNode = MathInterpreter.parseASTNode(math, variableToNodeMap, InterpreterType.OTHER, index);
            node.setDelayValue(delayNode);
          }
        }
        setupEventAssignments(sim, container, node, event);
      }
    }
  }


  private static void setupInitialAssignments(HierarchicalSimulation sim, ModelContainer container)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    for (InitialAssignment initAssignment : model.getListOfInitialAssignments())
    {
      String id = initAssignment.isSetMetaId() ? initAssignment.getMetaId() : initAssignment.toString();
      ReplacementSetup.setupReplacement(sim, initAssignment, id, container);

      if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(initAssignment.getMetaId()))
      {
        continue;
      }

      if(initAssignment.isSetMath())
      {
        String variable = initAssignment.getVariable();
        VariableNode variableNode = modelstate.getNode(variable);
        ASTNode math = initAssignment.getMath();
        HierarchicalNode initAssignNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), InterpreterType.OTHER, index);

        if (variableNode.isSpecies())
        {
          SpeciesNode node = (SpeciesNode) variableNode;

          if (!node.getState().getState(index).hasOnlySubstance())
          {
            HierarchicalNode amountNode = new HierarchicalNode(Type.TIMES);
            amountNode.addChild(initAssignNode);
            amountNode.addChild(node.getCompartment());
            initAssignNode = amountNode;
          }
        }

        FunctionNode node = new FunctionNode(variableNode, initAssignNode);
        variableNode.getState().getState(index).setHasInitRule(true);
        node.setIsInitAssignment(true);
        modelstate.addInitAssignment(node);
      }
    }

  }

  private static void setupReactions(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper)
  { 
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    boolean split = modelstate.getModelType() == ModelType.HSSA;
    for (Reaction reaction : model.getListOfReactions())
    {
      ReplacementSetup.setupReplacement(sim, reaction, wrapper, VariableType.REACTION, container);

      ReactionNode reactionNode = modelstate.addReaction(reaction.getId());

      for (SpeciesReference reactant : reaction.getListOfReactants())
      {
        setupReactant(sim, container, reactionNode, reactant.getSpecies(), reactant, wrapper);
      }
      for (SpeciesReference product : reaction.getListOfProducts())
      {
        setupProduct(sim, container, reactionNode, product.getSpecies(), product, wrapper);
      }

      if(sim.getProperties().getSimulationProperties().getIntSpecies() == null)
      {
        String printVariable = container.getPrefix() + reaction.getId();
        sim.addPrintVariable(printVariable, reactionNode, modelstate.getIndex(), false);
      }

      reactionNode.createState(sim.getAtomicType(), wrapper);
      modelstate.insertPropensity(reactionNode);
      reactionNode.setValue(modelstate.getIndex(), 0);

      KineticLaw kineticLaw = reaction.getKineticLaw();
      if (kineticLaw != null)
      {
        for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters())
        {

          String id = localParameter.getId();

          if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaId(localParameter.getMetaId()))
          {
            continue;
          }

          VariableNode node = new VariableNode(id, StateType.SCALAR);
          node.setValue(localParameter.getValue());
          reactionNode.addLocalParameter(id, node);
        }
        if (kineticLaw.isSetMath())
        {
          ASTNode reactionFormula = kineticLaw.getMath();

          if (reaction.isReversible() && split)
          {
            ASTNode[] splitMath = HierarchicalUtilities.splitMath(reactionFormula);
            if (splitMath == null)
            {
              HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, null, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(),  reactionNode, InterpreterType.RATE, modelstate.getIndex());
              reactionNode.setForwardRate(math);
            }
            else
            {
              HierarchicalNode forwardRate = MathInterpreter.parseASTNode(splitMath[0], null, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(),  reactionNode, InterpreterType.RATE, modelstate.getIndex());
              reactionNode.setForwardRate(forwardRate);
              HierarchicalNode reverseRate = MathInterpreter.parseASTNode(splitMath[1],  null, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(),  reactionNode, InterpreterType.RATE, modelstate.getIndex());
              reactionNode.setReverseRate(reverseRate);
            }
          }
          else
          {
            HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, null, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(), reactionNode, InterpreterType.RATE, modelstate.getIndex());
            reactionNode.setForwardRate(math);
          }
        }
      }
    }

  }

  private static void setupRules(HierarchicalSimulation sim, ModelContainer container)
  {
    Model model = container.getModel();
    HierarchicalModel modelstate = container.getHierarchicalModel();
    int index = modelstate.getIndex();
    Map<String, VariableNode> variableToNodes = modelstate.getVariableToNodeMap();
    for (Rule rule : model.getListOfRules())
    {
      String id = rule.isSetMetaId() ? rule.getMetaId() : rule.toString();
      ReplacementSetup.setupReplacement(sim, rule, id, container);
      if (modelstate.isDeletedByMetaId(rule.getMetaId()) || !rule.isSetMath())
      {
        continue;
      }

      if (rule.isAssignment())
      {
        AssignmentRule assignRule = (AssignmentRule) rule;
        if(assignRule.isSetMath())
        {
          ASTNode math = assignRule.getMath();
          VariableNode variableNode = variableToNodes.get(assignRule.getVariable());
          HierarchicalNode assignRuleNode = MathInterpreter.parseASTNode(math, variableToNodes, variableNode, InterpreterType.ASSIGNMENT, index);
          assignRuleNode = convertConcentrationUnits(variableNode, assignRuleNode, index);
          FunctionNode node = new FunctionNode(variableNode, assignRuleNode);
          modelstate.addAssignRule(node);
          variableNode.getState().getState(index).setHasRule(true);
        }
      }
      else if (rule.isRate())
      {
        RateRule rateRule = (RateRule) rule;
        if(rateRule.isSetMath())
        {
          ASTNode math = rateRule.getMath();
          VariableNode variableNode = variableToNodes.get(rateRule.getVariable());
          HierarchicalNode rateNode = MathInterpreter.parseASTNode(math, variableToNodes, InterpreterType.RATE, index);
          rateNode = convertConcentrationUnits(variableNode, rateNode, index);
          variableNode.setRateRule(rateNode);
          if(!variableNode.getState().getState(index).hasRate())
          {
            variableNode.getState().getState(index).setHasRate(true);
            modelstate.addVariable(variableNode);
          }
        }
      }
    }
  }

  private static HierarchicalNode convertConcentrationUnits(VariableNode var, HierarchicalNode math, int index)
  {
    HierarchicalNode convert = math;
    if(var.isSpecies())
    {
      SpeciesNode speciesNode = (SpeciesNode) var;

      if(!speciesNode.getState().getState(index).hasOnlySubstance())
      {
        convert = new HierarchicalNode(Type.TIMES);
        convert.addChild(speciesNode.getCompartment());
        convert.addChild(math);
      }
    }

    return convert;
  }
}
