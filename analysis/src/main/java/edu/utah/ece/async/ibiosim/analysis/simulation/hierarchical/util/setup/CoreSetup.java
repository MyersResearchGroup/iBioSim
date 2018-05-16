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
import java.util.ArrayList;
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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesReferenceNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.DenseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.SparseState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ModelContainer;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter.InterpreterType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.RateSplitterInterpreter;

/**
 * Sets up the hierarchical simulator by handling the core SBML components.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CoreSetup {

	static void initializeCore(HierarchicalSimulation sim, List<ModelContainer> listOfContainers, VariableNode time, VectorWrapper wrapper) throws IOException {
		List<NodeReplacement> listOfReplacements = new ArrayList<>();
		sim.addPrintVariable("time", time, 0, false);

		for (ModelContainer container : listOfContainers) {
			ReplacementSetup.setupDeletion(container);
			container.getHierarchicalModel().addMappingNode("_time", time);
			setupParameters(sim, container, wrapper, listOfReplacements);
			setupCompartments(sim, container, wrapper, listOfReplacements);
			setupSpecies(sim, container, wrapper, listOfReplacements);
			setupReactions(sim, container, wrapper, listOfReplacements);
		}

		for (int i = listOfReplacements.size() - 1; i >= 0; i--) {
			listOfReplacements.get(i);

		}

		for (ModelContainer container : listOfContainers) {
			setupEvents(sim, container, wrapper);
			setupConstraints(sim, container);
			setupRules(sim, container);
			setupInitialAssignments(sim, container);
		}

		if (wrapper != null) {
			wrapper.initStateValues();
		}
	}

	private static void setupCompartments(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();

		int index = modelstate.getIndex();

		for (Compartment compartment : model.getListOfCompartments()) {
			ReplacementSetup.setupReplacement(sim, compartment, wrapper, container, listOfReplacements);

			String printVariable = container.getPrefix() + compartment.getId();
			String compartmentID = compartment.getId();
			VariableNode node = new VariableNode(compartmentID);

			if (sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable)) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
			}
			if (sim.getProperties().getSimulationProperties().getIntSpecies().size() == 0) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
			}
			node.setState(createState(sim.getCollectionType(), wrapper));
			modelstate.addMappingNode(compartmentID, node);

			if (!compartment.isConstant()) {
				modelstate.addVariable(node);
				node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
			} else {
				node.getState().addState(index, createState(StateType.SCALAR, wrapper));
			}

			if (Double.isNaN(compartment.getSize())) {
				node.getState().getState(index).setInitialValue(1);
			} else {
				node.getState().getState(index).setInitialValue(compartment.getSize());
			}
		}
	}

	/**
	 * puts parameter-related information into data structures
	 */
	private static void setupParameters(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();

		VariableNode node;

		for (Parameter parameter : model.getListOfParameters()) {

			ReplacementSetup.setupReplacement(sim, parameter, wrapper, container, listOfReplacements);
			String parameterId = parameter.getId();
			node = new VariableNode(parameterId);
			modelstate.addMappingNode(parameter.getId(), node);
			node.setState(createState(sim.getCollectionType(), wrapper));

			String printVariable = container.getPrefix() + parameter.getId();

			if (sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable)) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
			}

			if (sim.getProperties().getSimulationProperties().getIntSpecies().size() == 0) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
			}

			if (!parameter.isConstant()) {
				modelstate.addVariable(node);
				node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
			} else {
				node.getState().addState(index, createState(StateType.SCALAR, wrapper));
			}

			if (parameter.isSetValue()) {
				node.getState().getState(index).setInitialValue(parameter.getValue());
			}
		}
	}

	/**
	 * puts species-related information into data structures
	 *
	 */
	private static void setupSpecies(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		boolean isConcentration = false;
		for (Species species : model.getListOfSpecies()) {
			ReplacementSetup.setupReplacement(sim, species, wrapper, container, listOfReplacements);

			String speciesId = species.getId();
			String printVariable = container.getPrefix() + speciesId;
			SpeciesNode node = new SpeciesNode(speciesId);
			modelstate.addMappingNode(speciesId, node);
			if (sim.getProperties().getSimulationProperties().getPrinter_track_quantity().equals("concentration")) {
				isConcentration = true;
			}

			if (sim.getProperties().getSimulationProperties().getIntSpecies().contains(printVariable)) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), isConcentration);
			} else if (sim.getProperties().getSimulationProperties().getIntSpecies().size() == 0) {
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), isConcentration);
			}

			boolean isBoundary = species.getBoundaryCondition();
			boolean isOnlySubstance = species.getHasOnlySubstanceUnits();
			node.setState(createState(sim.getCollectionType(), wrapper));

			if (!species.isConstant()) {
				modelstate.addVariable(node);
				node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
			} else {
				node.getState().addState(index, createState(StateType.SCALAR, wrapper));
			}
			node.getState().getState(index).setBoundaryCondition(isBoundary);
			node.getState().getState(index).setHasOnlySubstance(isOnlySubstance);

			VariableNode compartment = modelstate.getNode(species.getCompartment());
			node.setCompartment(compartment);

			if (species.isSetInitialAmount()) {
				node.getState().getState(index).setInitialValue(species.getInitialAmount());
			} else if (species.isSetInitialConcentration()) {
				HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
				initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
				initConcentration.addChild(compartment);
				FunctionNode functionNode = new FunctionNode(node, initConcentration);
				modelstate.addInitialConcentration(functionNode);
				node.getState().getState(index).setHasAmountUnits(false);
			}
		}
	}

	private static void setupProduct(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String productID, SpeciesReference product, VectorWrapper wrapper) {
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		String id = product.isSetMetaId() ? product.getMetaId() : product.toString();

		ReplacementSetup.setupReplacement(sim, product, id, container);

		double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

		SpeciesReferenceNode node = new SpeciesReferenceNode();

		node.setState(createState(sim.getCollectionType(), wrapper));

		if (!product.isConstant()) {
			modelstate.addVariable(node);
			node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
		} else {
			node.getState().addState(index, createState(StateType.SCALAR, wrapper));
		}
		node.getState().getState(modelstate.getIndex()).setInitialValue(stoichiometryValue);

		SpeciesNode species = (SpeciesNode) modelstate.getNode(product.getSpecies());
		node.setSpecies(species);
		reaction.addProduct(node);

		if (product.isSetId() && product.getId().length() > 0) {
			node.setName(product.getId());
			if (!product.getConstant()) {

				node.getState().getState(index).setConstant(false);
				modelstate.addVariable(node);
			}

			modelstate.addMappingNode(product.getId(), node);
		}
		species.addODERate(reaction, node);
		if (!species.getState().getState(index).hasRate()) {
			species.getState().getState(index).setHasRate(true);
			modelstate.addVariable(species);
		}
	}

	private static void setupReactant(HierarchicalSimulation sim, ModelContainer container, ReactionNode reaction, String reactantID, SpeciesReference reactant, VectorWrapper wrapper) {
		HierarchicalModel modelstate = container.getHierarchicalModel();
		String id = reactant.isSetMetaId() ? reactant.getMetaId() : reactant.toString();
		int index = modelstate.getIndex();
		ReplacementSetup.setupReplacement(sim, reactant, id, container);

		double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
		SpeciesReferenceNode node = new SpeciesReferenceNode();

		node.setState(createState(sim.getCollectionType(), wrapper));
		if (!reactant.isConstant()) {
			modelstate.addVariable(node);
			node.getState().addState(index, createState(sim.getAtomicType(), wrapper));
		} else {
			node.getState().addState(index, createState(StateType.SCALAR, wrapper));
		}
		node.getState().getState(modelstate.getIndex()).setInitialValue(stoichiometryValue);

		SpeciesNode species = (SpeciesNode) modelstate.getNode(reactant.getSpecies());
		node.setSpecies(species);
		reaction.addReactant(node);

		if (reactant.isSetId() && reactant.getId().length() > 0) {
			node.setName(reactant.getId());

			if (!reactant.getConstant()) {
				node.getState().getState(index).setConstant(false);
				modelstate.addVariable(node);
			}

			modelstate.addMappingNode(reactant.getId(), node);
		}
		species.subtractODERate(reaction, node);
		if (!species.getState().getState(index).hasRate()) {
			species.getState().getState(index).setHasRate(true);
			modelstate.addVariable(species);

			if (wrapper != null) {

			}
		}
	}

	private static void setupConstraints(HierarchicalSimulation sim, ModelContainer container) {

		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		for (Constraint constraint : model.getListOfConstraints()) {

			String id = constraint.isSetMetaId() ? constraint.getMetaId() : constraint.toString();
			ReplacementSetup.setupReplacement(sim, constraint, id, container);

			if (modelstate.isDeletedByMetaId(constraint.getMetaId())) {
				continue;
			}

			ASTNode math = constraint.getMath();

			if (math != null) {
				HierarchicalNode constraintNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), InterpreterType.OTHER, index);
				modelstate.addConstraint(id, constraintNode);
			}
		}
	}

	private static void setupEventAssignments(HierarchicalSimulation sim, ModelContainer container, EventNode eventNode, Event event) {
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
			String id = eventAssignment.isSetMetaId() ? eventAssignment.getMetaId() : eventAssignment.toString();
			ReplacementSetup.setupReplacement(sim, eventAssignment, id, container);
			if (eventAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(eventAssignment.getMetaId()) || !eventAssignment.isSetMath()) {
				continue;
			}

			if (eventAssignment.isSetMath()) {
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
	private static void setupEvents(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		Map<String, VariableNode> variableToNodeMap = modelstate.getVariableToNodeMap();
		int index = modelstate.getIndex();
		for (Event event : model.getListOfEvents()) {

			String id = event.isSetId() ? event.getId() : event.isSetMetaId() ? event.getMetaId() : event.toString();
			ReplacementSetup.setupReplacement(sim, event, id, container);

			if (modelstate.isDeletedBySId(event.getId())) {
				continue;
			}

			sim.setHasEvents(true);

			if (event.isSetTrigger() && event.getTrigger().isSetMath()) {

				Trigger trigger = event.getTrigger();
				ASTNode triggerMath = trigger.getMath();
				HierarchicalNode triggerNode = MathInterpreter.parseASTNode(triggerMath, variableToNodeMap, InterpreterType.OTHER, index);

				EventNode node = modelstate.createEvent(triggerNode);
				node.addEventState(index);

				node.setState(createState(sim.getCollectionType(), wrapper));
				node.getState().addState(index, createState(sim.getAtomicType(), wrapper));

				node.getState().getState(modelstate.getIndex()).setInitialValue(0);

				boolean useValuesFromTrigger = event.getUseValuesFromTriggerTime();
				boolean isPersistent = trigger.isSetPersistent() ? trigger.getPersistent() : false;
				boolean initValue = trigger.isSetInitialValue() ? trigger.getInitialValue() : false;
				node.getState().getState(index).setUseTriggerValue(useValuesFromTrigger);
				node.getState().getState(index).setPersistent(isPersistent);
				node.setInitialTrue(index, initValue);

				if (event.isSetPriority()) {
					Priority priority = event.getPriority();
					if (!(priority.isSetMetaId() && modelstate.isDeletedByMetaId(priority.getMetaId())) && priority.isSetMath()) {
						ASTNode math = priority.getMath();
						HierarchicalNode priorityNode = MathInterpreter.parseASTNode(math, variableToNodeMap, InterpreterType.OTHER, index);
						node.setPriority(priorityNode);
					}
				}
				if (event.isSetDelay()) {
					Delay delay = event.getDelay();
					if (!(delay.isSetMetaId() && modelstate.isDeletedByMetaId(delay.getMetaId())) && delay.isSetMath()) {
						ASTNode math = delay.getMath();
						HierarchicalNode delayNode = MathInterpreter.parseASTNode(math, variableToNodeMap, InterpreterType.OTHER, index);
						node.setDelay(delayNode);
					}
				}
				setupEventAssignments(sim, container, node, event);
			}
		}
	}

	private static void setupInitialAssignments(HierarchicalSimulation sim, ModelContainer container) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		for (InitialAssignment initAssignment : model.getListOfInitialAssignments()) {
			String id = initAssignment.isSetMetaId() ? initAssignment.getMetaId() : initAssignment.toString();
			ReplacementSetup.setupReplacement(sim, initAssignment, id, container);

			if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(initAssignment.getMetaId())) {
				continue;
			}

			if (initAssignment.isSetMath()) {
				String variable = initAssignment.getVariable();
				VariableNode variableNode = modelstate.getNode(variable);
				ASTNode math = initAssignment.getMath();
				HierarchicalNode initAssignNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), InterpreterType.OTHER, index);

				if (variableNode.isSpecies()) {
					SpeciesNode node = (SpeciesNode) variableNode;

					if (!node.getState().getState(index).hasOnlySubstance()) {
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

	private static void setupReactions(HierarchicalSimulation sim, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		boolean split = modelstate.getModelType() == ModelType.HSSA;
		for (Reaction reaction : model.getListOfReactions()) {
			ReplacementSetup.setupReplacement(sim, reaction, wrapper, container, listOfReplacements);

			ReactionNode node = modelstate.createReaction(reaction.getId());

			for (SpeciesReference reactant : reaction.getListOfReactants()) {
				setupReactant(sim, container, node, reactant.getSpecies(), reactant, wrapper);
			}
			for (SpeciesReference product : reaction.getListOfProducts()) {
				setupProduct(sim, container, node, product.getSpecies(), product, wrapper);
			}

			if (sim.getProperties().getSimulationProperties().getIntSpecies().size() == 0) {
				String printVariable = container.getPrefix() + reaction.getId();
				sim.addPrintVariable(printVariable, node, modelstate.getIndex(), false);
			}

			node.setState(createState(sim.getCollectionType(), wrapper));
			node.getState().addState(index, createState(sim.getAtomicType(), wrapper));

			node.getState().getState(modelstate.getIndex()).setInitialValue(0);

			KineticLaw kineticLaw = reaction.getKineticLaw();
			if (kineticLaw != null) {
				for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters()) {

					String id = localParameter.getId();

					if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaId(localParameter.getMetaId())) {
						continue;
					}

					VariableNode localParam = new VariableNode(id, StateType.SCALAR);
					node.getState().setStateValue(localParameter.getValue());
					node.addLocalParameter(id, localParam);
				}
				if (kineticLaw.isSetMath()) {
					ASTNode reactionFormula = kineticLaw.getMath();

					if (reaction.isReversible() && split) {
						ASTNode[] splitMath = splitMath(reactionFormula);
						if (splitMath == null) {
							HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, null, modelstate.getVariableToNodeMap(), node.getLocalParameters(), node, InterpreterType.RATE, modelstate.getIndex());
							node.setForwardRate(math);
						} else {
							HierarchicalNode forwardRate = MathInterpreter.parseASTNode(splitMath[0], null, modelstate.getVariableToNodeMap(), node.getLocalParameters(), node, InterpreterType.RATE, modelstate.getIndex());
							node.setForwardRate(forwardRate);
							HierarchicalNode reverseRate = MathInterpreter.parseASTNode(splitMath[1], null, modelstate.getVariableToNodeMap(), node.getLocalParameters(), node, InterpreterType.RATE, modelstate.getIndex());
							node.setReverseRate(reverseRate);
						}
					} else {
						HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, null, modelstate.getVariableToNodeMap(), node.getLocalParameters(), node, InterpreterType.RATE, modelstate.getIndex());
						node.setForwardRate(math);
					}
				}
			}
		}

	}

	private static void setupRules(HierarchicalSimulation sim, ModelContainer container) {
		Model model = container.getModel();
		HierarchicalModel modelstate = container.getHierarchicalModel();
		int index = modelstate.getIndex();
		Map<String, VariableNode> variableToNodes = modelstate.getVariableToNodeMap();
		for (Rule rule : model.getListOfRules()) {
			String id = rule.isSetMetaId() ? rule.getMetaId() : rule.toString();
			ReplacementSetup.setupReplacement(sim, rule, id, container);
			if (modelstate.isDeletedByMetaId(rule.getMetaId()) || !rule.isSetMath()) {
				continue;
			}

			if (rule.isAssignment()) {
				AssignmentRule assignRule = (AssignmentRule) rule;
				if (assignRule.isSetMath()) {
					ASTNode math = assignRule.getMath();
					VariableNode variableNode = variableToNodes.get(assignRule.getVariable());
					HierarchicalNode assignRuleNode = MathInterpreter.parseASTNode(math, variableToNodes, variableNode, InterpreterType.ASSIGNMENT, index);
					assignRuleNode = convertConcentrationUnits(variableNode, assignRuleNode, index);
					FunctionNode node = new FunctionNode(variableNode, assignRuleNode);
					modelstate.addAssignRule(node);
					variableNode.getState().getState(index).setHasRule(true);
				}
			} else if (rule.isRate()) {
				RateRule rateRule = (RateRule) rule;
				if (rateRule.isSetMath()) {
					ASTNode math = rateRule.getMath();
					VariableNode variableNode = variableToNodes.get(rateRule.getVariable());
					HierarchicalNode rateNode = MathInterpreter.parseASTNode(math, variableToNodes, InterpreterType.RATE, index);
					rateNode = convertConcentrationUnits(variableNode, rateNode, index);
					variableNode.setRateRule(rateNode);
					if (!variableNode.getState().getState(index).hasRate()) {
						variableNode.getState().getState(index).setHasRate(true);
						modelstate.addVariable(variableNode);
					}
				}
			}
		}
	}

	private static HierarchicalNode convertConcentrationUnits(VariableNode var, HierarchicalNode math, int index) {
		HierarchicalNode convert = math;
		if (var.isSpecies()) {
			SpeciesNode speciesNode = (SpeciesNode) var;

			if (!speciesNode.getState().getState(index).hasOnlySubstance()) {
				convert = new HierarchicalNode(Type.TIMES);
				convert.addChild(speciesNode.getCompartment());
				convert.addChild(math);
			}
		}

		return convert;
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

	private static HierarchicalState createState(StateType type, VectorWrapper wrapper) {
		HierarchicalState state = null;

		if (type == StateType.VECTOR) {
			state = new VectorState(wrapper);
		} else if (type == StateType.DENSE) {
			state = new ValueState();
		} else if (type == StateType.SPARSE) {
			state = new SparseState();
		} else if (type == StateType.DENSE) {
			state = new DenseState();
		} else if (type == StateType.SCALAR) {
			state = new ValueState();
		}
		return state;
	}
}
