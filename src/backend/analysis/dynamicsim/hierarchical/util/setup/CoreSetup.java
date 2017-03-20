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
package backend.analysis.dynamicsim.hierarchical.util.setup;

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

import backend.analysis.dynamicsim.hierarchical.math.EventNode;
import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.ReactionNode;
import backend.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import backend.analysis.dynamicsim.hierarchical.math.SpeciesReferenceNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import backend.analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import backend.analysis.dynamicsim.hierarchical.states.EventState;
import backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.comp.ModelContainer;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

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

	static void initializeModel(HierarchicalSimulation sim, ModelContainer container, StateType type, VariableNode time, VectorWrapper wrapper,  boolean split) throws IOException
	{
		container.getHierarchicalModel().addMappingNode("_time", time);
		sim.addPrintVariable(time.getName(), time.getState());
		setupParameters(sim, container, type, wrapper);
		setupCompartments(sim, container, type, wrapper);
		setupSpecies(sim, container, type, wrapper);
		setupReactions(sim, container, type, wrapper);
	  setupEvents(sim, container);
    setupConstraints(sim, container);
    setupRules(sim, container);
    setupInitialAssignments(sim, container);
    //ArraysSetup.linkDimensionSize(modelstate);
    //ArraysSetup.expandArrays(modelstate);
	}

	private static void setupCompartments(HierarchicalSimulation sim, ModelContainer container,  StateType type,  VectorWrapper wrapper)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		VariableNode node;
		for (Compartment compartment : model.getListOfCompartments())
		{
			String compartmentID = compartment.getId();
			if (modelstate.isDeletedBySId(compartmentID))
			{
				continue;
			}
			node = modelstate.getNode(compartmentID);

			if(node == null)
			{
				node = new VariableNode(compartmentID);
				if (compartment.getConstant())
				{
					node.createState(StateType.SCALAR, wrapper);
					modelstate.addMappingNode(compartmentID, node);
					
				}
				else
				{
					node.createState(type, wrapper);
					modelstate.addVariable(node);
					sim.addPrintVariable(compartmentID, node.getState());
				}
				

				if (Double.isNaN(compartment.getSize()))
				{
					node.setValue(modelstate.getIndex(), 1);
				}
				else
				{
					node.setValue(modelstate.getIndex(), compartment.getSize());
				}
			}

			if (Double.isNaN(compartment.getSize()))
			{
				node.setValue(modelstate.getIndex(), 1);
			}
			else
			{
				node.setValue(modelstate.getIndex(), compartment.getSize());
			}
		}
	}

	/**
	 * puts constraint-related information into data structures
	 */
	private static void setupConstraints(HierarchicalSimulation sim, ModelContainer container)
	{

		int count = 0;
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		for (Constraint constraint : model.getListOfConstraints())
		{
			String id = null;
			if (constraint.isSetMetaId())
			{
				id = constraint.getMetaId();
			}
			else
			{
				id = "constraint " + count++;
			}

			if (modelstate.isDeletedByMetaId(constraint.getMetaId()))
			{
				continue;
			}

			ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, constraint.getMath(), model);

			HierarchicalNode constraintNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap());

			modelstate.addConstraint(id, constraintNode);
		}

	}

	private static void setupEventAssignments(HierarchicalSimulation sim, HierarchicalModel modelstate, EventNode eventNode, Event event, Model model, Map<String, VariableNode> variableToNodeMap)
	{
		for (EventAssignment eventAssignment : event.getListOfEventAssignments())
		{
			if (eventAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(eventAssignment.getMetaId()) || !eventAssignment.isSetMath())
			{
				continue;
			}

			ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, eventAssignment.getMath(), model);
			HierarchicalNode assignmentNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
			VariableNode variable = variableToNodeMap.get(eventAssignment.getVariable());
			FunctionNode eventAssignmentNode = new FunctionNode(variable, assignmentNode);
			eventNode.addEventAssignment(eventAssignmentNode);
		}
	}

	/**
	 * puts event-related information into data structures
	 */
	private static void setupEvents(HierarchicalSimulation sim, ModelContainer container)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		Map<String, VariableNode> variableToNodeMap = modelstate.getVariableToNodeMap();
		int index = modelstate.getIndex();
		for (Event event : model.getListOfEvents())
		{
			if (modelstate.isDeletedBySId(event.getId()))
			{
				continue;
			}

			sim.setHasEvents(true);
			
			if(event.isSetTrigger() && event.getTrigger().isSetMath())
			{

				Trigger trigger = event.getTrigger();
				ASTNode triggerMath = HierarchicalUtilities.inlineFormula(modelstate, trigger.getMath(), model);
				HierarchicalNode triggerNode = MathInterpreter.parseASTNode(triggerMath, variableToNodeMap);

				EventNode node = modelstate.addEvent(triggerNode);
				EventState state = node.addEventState(index);
				
				boolean useValuesFromTrigger = event.getUseValuesFromTriggerTime();
				boolean isPersistent = trigger.isSetPersistent() ? trigger.getPersistent() : false;
				boolean initValue = trigger.isSetInitialValue() ? trigger.getInitialValue() : false;
				node.setUseTriggerValue(useValuesFromTrigger);
				node.setPersistent(isPersistent);

				if (!initValue)
				{
					state.setMaxDisabledTime(0);
					if (node.computeTrigger(modelstate.getIndex()))
					{
						state.setMinEnabledTime(0);
					}
				}

				if (event.isSetPriority())
				{
					Priority priority = event.getPriority();

					if (!(priority.isSetMetaId() && modelstate.isDeletedByMetaId(priority.getMetaId())) && priority.isSetMath())
					{
						ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, priority.getMath(), model);
						HierarchicalNode priorityNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
						node.setPriorityValue(priorityNode);
					}

				}
				if (event.isSetDelay())
				{
					Delay delay = event.getDelay();

					if (!(delay.isSetMetaId() && modelstate.isDeletedByMetaId(delay.getMetaId())) && delay.isSetMath())
					{
						ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, delay.getMath(), model);
						HierarchicalNode delayNode = MathInterpreter.parseASTNode(math, variableToNodeMap);
						node.setDelayValue(delayNode);
					}
				}

				setupEventAssignments(sim, modelstate, node, event, model, variableToNodeMap);

				
			}
		}
	}


	private static void setupInitialAssignments(HierarchicalSimulation sim, ModelContainer container)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		for (InitialAssignment initAssignment : model.getListOfInitialAssignments())
		{
			if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(initAssignment.getMetaId()))
			{
				continue;
			}
			String variable = initAssignment.getVariable();
			VariableNode variableNode = modelstate.getNode(variable);

			if(initAssignment.isSetMath())
			{
				ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, initAssignment.getMath(), model);
				HierarchicalNode initAssignNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap());

				if (variableNode.isSpecies())
				{
					SpeciesNode node = (SpeciesNode) variableNode;

					if (!node.hasOnlySubstance())
					{
						HierarchicalNode amountNode = new HierarchicalNode(Type.TIMES);
						amountNode.addChild(initAssignNode);
						amountNode.addChild(node.getCompartment());
						initAssignNode = amountNode;
					}
				}

				FunctionNode node = new FunctionNode(variableNode, initAssignNode);
				node.setIsInitAssignment(true);
				modelstate.addInitAssignment(node);
			}
		}

	}

	/**
	 * puts parameter-related information into data structures
	 */
	private static void setupParameters(HierarchicalSimulation sim, ModelContainer container, StateType type,  VectorWrapper wrapper)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		VariableNode node;

		for (Parameter parameter : model.getListOfParameters())
		{
			if (modelstate.isDeletedBySId(parameter.getId()))
			{
				continue;
			}
			else if (ArraysSetup.checkArray(parameter))
			{
				continue;
			}
			node = modelstate.getNode(parameter.getId());

			if(node == null)
			{
				node = new VariableNode(parameter.getId());
				if (parameter.isConstant())
				{
					node.createState(StateType.SCALAR, wrapper);
					 modelstate.addMappingNode(parameter.getId(), node);
				}
				else
				{
					node.createState(type, wrapper);
					modelstate.addVariable(node);
					sim.addPrintVariable(parameter.getId(), node.getState());
				}

			
			}
			node.setValue(modelstate.getIndex(), parameter.getValue());
		}
	}


	private static void setupProduct(HierarchicalSimulation sim, HierarchicalModel modelstate, ReactionNode reaction, String productID, SpeciesReference product, StateType type, VectorWrapper wrapper)
	{

		if (product.isSetId() && modelstate.isDeletedBySId(product.getId()))
		{
			return;
		}
		if (product.isSetMetaId() && modelstate.isDeletedByMetaId(product.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
		if(!product.isConstant())
		{
			speciesReferenceNode.createState(type, wrapper);
		}
		else
		{
			speciesReferenceNode.createState(StateType.SCALAR, wrapper);
		}
		speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);

		SpeciesNode species = (SpeciesNode) modelstate.getNode(productID);
		speciesReferenceNode.setSpecies(species);
		reaction.addProduct(speciesReferenceNode);

		if (product.isSetId() && product.getId().length() > 0)
		{
			speciesReferenceNode.setName(product.getId());
			if (!product.getConstant())
			{
				speciesReferenceNode.setIsVariableConstant(false);
				modelstate.addVariable(speciesReferenceNode );
			}
			else
			{
			  modelstate.addMappingNode(product.getId(), speciesReferenceNode);
			}			
		}
		species.addODERate(reaction, speciesReferenceNode);

	}

	private static void setupReactant(HierarchicalSimulation sim, HierarchicalModel modelstate, ReactionNode reaction, String reactantID, SpeciesReference reactant, StateType type, VectorWrapper wrapper)
	{

		if (reactant.isSetId() && modelstate.isDeletedBySId(reactant.getId()))
		{
			return;
		}
		if (reactant.isSetMetaId() && modelstate.isDeletedByMetaId(reactant.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode();
		if(!reactant.isConstant())
		{
			speciesReferenceNode.createState(type, wrapper);
		}
		else
		{

			speciesReferenceNode.createState(StateType.SCALAR, wrapper);
		}
		speciesReferenceNode.setValue(modelstate.getIndex(), stoichiometryValue);
		SpeciesNode species = (SpeciesNode) modelstate.getNode(reactantID);
		speciesReferenceNode.setSpecies(species);
		reaction.addReactant(speciesReferenceNode);

		if (reactant.isSetId() && reactant.getId().length() > 0)
		{
			speciesReferenceNode.setName(reactant.getId());

			if (!reactant.getConstant())
			{
				speciesReferenceNode.setIsVariableConstant(false);
				modelstate.addVariable(speciesReferenceNode );
			}
			else
			{
			  modelstate.addMappingNode(reactant.getId(), speciesReferenceNode);
			}
		}
		species.subtractODERate(reaction, speciesReferenceNode);

	}

	private static void setupReactions(HierarchicalSimulation sim, ModelContainer container, StateType type, VectorWrapper wrapper)
	{
		Reaction reaction;
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		boolean split = modelstate.getModelType() == ModelType.HSSA;
		for (int i = 0; i < model.getNumReactions(); i++)
		{
			reaction = model.getReaction(i);

			if (modelstate.isDeletedBySId(reaction.getId()))
			{
				continue;
			}
			else if (ArraysSetup.checkArray(reaction))
			{
				continue;
			}

			if(modelstate.getNode(reaction.getId()) != null)
			{
				continue;
			}

			ReactionNode reactionNode = modelstate.addReaction(reaction.getId());
			//TODO: change scalar to other types too
			reactionNode.createState(StateType.SCALAR, wrapper);
			reactionNode.setValue(modelstate.getIndex(), 0);

			for (SpeciesReference reactant : reaction.getListOfReactants())
			{
				setupReactant(sim, modelstate, reactionNode, reactant.getSpecies(), reactant, type, wrapper);
			}
			for (SpeciesReference product : reaction.getListOfProducts())
			{
				setupProduct(sim, modelstate, reactionNode, product.getSpecies(), product, type, wrapper);
			}
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
					reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, model);

					if (reaction.isReversible() && split)
					{
						setupSingleRevReaction(sim, modelstate, reactionNode, reactionFormula);
					}
					else
					{
						setupSingleNonRevReaction(sim, modelstate, reactionNode, reactionFormula, model);
					}
				}
			}
		}

	}

	private static void setupRules(HierarchicalSimulation sim, ModelContainer container)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		Map<String, VariableNode> variableToNodes = modelstate.getVariableToNodeMap();
		for (Rule rule : model.getListOfRules())
		{
			if (rule.isSetMetaId() && modelstate.isDeletedByMetaId(rule.getMetaId()) || !rule.isSetMath())
			{
				continue;
			}

			if (rule.isAssignment())
			{
				AssignmentRule assignRule = (AssignmentRule) rule;
				ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, assignRule.getMath(), model);
				VariableNode variableNode = variableToNodes.get(assignRule.getMath());
				HierarchicalNode assignRuleNode = MathInterpreter.parseASTNode(math, variableToNodes, variableNode);
				FunctionNode node = new FunctionNode(variableNode, assignRuleNode);
				modelstate.addAssignRule(node);
				variableNode.setHasRule(true);
			}
			else if (rule.isRate())
			{
				RateRule rateRule = (RateRule) rule;
				//TODO: fix
				if(rateRule.isSetMath())
				{
					ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, rateRule.getMath(), model);
					VariableNode variableNode = variableToNodes.get(rateRule.getVariable());
					HierarchicalNode rateNode = MathInterpreter.parseASTNode(math, variableToNodes);
					variableNode.setRateRule(rateNode);
				}
			}
		}
	}

	private static void setupSingleNonRevReaction(HierarchicalSimulation sim, HierarchicalModel modelstate, ReactionNode reactionNode, ASTNode reactionFormula, Model model)
	{
		HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(), reactionNode);
		reactionNode.setForwardRate(math);
		//reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
	}

	private static void setupSingleRevReaction(HierarchicalSimulation sim, HierarchicalModel modelstate, ReactionNode reactionNode, ASTNode reactionFormula)
	{
		ASTNode[] splitMath = HierarchicalUtilities.splitMath(reactionFormula);
		if (splitMath == null)
		{
			HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode.getLocalParameters(),  reactionNode);
			reactionNode.setForwardRate(math);
			//reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
		}
		else
		{
			HierarchicalNode forwardRate = MathInterpreter.parseASTNode(splitMath[0], modelstate.getVariableToNodeMap(), reactionNode);
			HierarchicalNode reverseRate = MathInterpreter.parseASTNode(splitMath[1], modelstate.getVariableToNodeMap(), reactionNode);
			reactionNode.setForwardRate(forwardRate);
			reactionNode.setReverseRate(reverseRate);
			//reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
		}
	}

	/**
	 * puts species-related information into data structures
	 * 
	 */
	private static void setupSpecies(HierarchicalSimulation sim, ModelContainer container,  StateType type, VectorWrapper wrapper)
	{
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		SpeciesNode node;
		for (Species species : model.getListOfSpecies())
		{
			if (modelstate.isDeletedBySId(species.getId()))
			{
				continue;
			}
			if (ArraysSetup.checkArray(species))
			{
				continue;
			}
			node = (SpeciesNode) modelstate.getNode(species.getId());

			if(node == null)
			{
				node = new SpeciesNode(species.getId());
				node.createSpeciesTemplate();

				if(species.getConstant())
				{
					node.createState(StateType.SCALAR, wrapper);
					modelstate.addMappingNode(node.getName(), node);
				}
				else
				{
					node.createState(type, wrapper);
					modelstate.addVariable(node);
					sim.addPrintVariable(species.getId(), node.getState());
				}
			}
			node.setValue(index, 0);
			node.setBoundaryCondition(species.getBoundaryCondition());
			node.setHasOnlySubstance(species.getHasOnlySubstanceUnits());
			VariableNode compartment = modelstate.getNode(species.getCompartment());
			node.setCompartment(compartment);
			if (species.isSetInitialAmount())
			{
				node.setValue(modelstate.getIndex(), species.getInitialAmount());
			}
			else if (species.isSetInitialConcentration())
			{
				HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
				initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
				initConcentration.addChild(compartment);
				FunctionNode functionNode = new FunctionNode(node, initConcentration);
				modelstate.addInitAssignment(functionNode);
				functionNode.setIsInitAssignment(true);
			}
		}
	}
}
