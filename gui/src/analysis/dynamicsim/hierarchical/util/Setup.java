package analysis.dynamicsim.hierarchical.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalObjects.ModelState;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public class Setup
{

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	public static void setupLocalParameters(ModelState modelstate, KineticLaw kineticLaw, Reaction reaction)
	{

		String reactionID = reaction.getId();

		for (int i = 0; i < kineticLaw.getLocalParameterCount(); i++)
		{

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);

			String id = localParameter.getId();

			if (localParameter.isSetId() && modelstate.isDeletedBySID(localParameter.getId()))
			{
				continue;
			}
			if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaID(localParameter.getMetaId()))
			{
				continue;
			}

			String parameterID = reactionID + "_" + id;

			modelstate.addVariableToValueMap(parameterID, localParameter.getValue());

			HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), id, parameterID);
		}
	}

	public static void setupSingleCompartment(ModelState modelstate, Compartment compartment, String compartmentID, Map<String, Double> replacements)
	{

		if (compartment.isSetId() && modelstate.isDeletedBySID(compartment.getId()))
		{
			return;
		}
		else if (compartment.isSetMetaId() && modelstate.isDeletedByMetaID(compartment.getMetaId()))
		{
			return;
		}

		modelstate.addCompartment(compartmentID);

		modelstate.addVariableToValueMap(compartmentID, compartment.getSize());

		if (Double.isNaN(compartment.getSize()))
		{
			modelstate.setVariableToValue(replacements, compartmentID, 1.0);
		}

		if (compartment.getConstant())
		{
			modelstate.addVariableToIsConstant(compartmentID);
		}

		if (!compartment.getConstant())
		{
			modelstate.addVariableToPrint(compartmentID);
		}

		if (modelstate.getNumRules() > 0)
		{
			modelstate.addVariableToIsInAssignmentRule(compartmentID, false);
		}

		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.addVariableToIsInConstraint(compartmentID, false);
		}
	}

	public static void setupSingleConstraint(ModelState modelstate, ASTNode math, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, models, iBioSimFunctionDefinitions);

		for (ASTNode constraintNode : math.getListOfNodes())
		{

			if (constraintNode.isName())
			{

				String nodeName = constraintNode.getName();
				modelstate.getVariableToAffectedConstraintSetMap().put(nodeName, new HashSet<ASTNode>());
				modelstate.getVariableToAffectedConstraintSetMap().get(nodeName).add(math);
				modelstate.getVariableToIsInConstraintMap().put(nodeName, true);
			}
		}

	}

	public static void setupSinglePriority(ModelState modelstate, String eventID, String priorityID, ASTNode math, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions)
	{

		if (priorityID != null && modelstate.isDeletedByMetaID(priorityID))
		{
			return;
		}

		modelstate.getEventToPriorityMap().put(eventID, HierarchicalUtilities.inlineFormula(modelstate, math, models, iBioSimFunctionDefinitions));
	}

	public static void setupSingleDelay(ModelState modelstate, String eventID, String delayID, ASTNode math, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions)
	{

		if (delayID != null && modelstate.isDeletedByMetaID(delayID))
		{
			return;
		}
		else
		{
			modelstate.getEventToDelayMap().put(eventID, HierarchicalUtilities.inlineFormula(modelstate, math, models, iBioSimFunctionDefinitions));
			modelstate.getEventToHasDelayMap().add(eventID);
		}

	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	public static void setupSingleEvent(ModelState modelstate, String eventID, ASTNode trigger, boolean useFromTrigger, boolean initValue,
			boolean persistent, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions)
	{
		trigger = HierarchicalUtilities.inlineFormula(modelstate, trigger, models, iBioSimFunctionDefinitions);
		modelstate.getEventToTriggerMap().put(eventID, trigger);
		modelstate.getEventToTriggerInitiallyTrueMap().put(eventID, initValue);
		modelstate.getEventToPreviousTriggerValueMap().put(eventID, initValue);
		modelstate.getEventToTriggerPersistenceMap().put(eventID, persistent);
		modelstate.getEventToUseValuesFromTriggerTimeMap().put(eventID, useFromTrigger);
		modelstate.getEventToAssignmentSetMap().put(eventID, new HashMap<String, ASTNode>());
		modelstate.getEventToAffectedReactionSetMap().put(eventID, new HashSet<String>());
		modelstate.getUntriggeredEventSet().add(eventID);
	}

	public static void setupEventAssignment(ModelState modelstate, String variableID, String eventID, ASTNode math, EventAssignment assignment,
			Map<String, Model> models, Set<String> iBioSimFunctionDefinitions)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, models, iBioSimFunctionDefinitions);

		modelstate.addEventAssignment(eventID, variableID, math);

		if (modelstate.getVariableToEventSetMap().containsKey(variableID) == false)
		{
			modelstate.getVariableToEventSetMap().put(variableID, new HashSet<String>());
		}

		modelstate.getVariableToEventSetMap().get(variableID).add(eventID);

		// if the variable is a species, add the reactions it's in
		// to the event to affected reaction hashmap, which is used
		// for updating propensities after an event fires
		if (modelstate.getSpeciesToAffectedReactionSetMap().containsKey(variableID))
		{

			modelstate.getEventToAffectedReactionSetMap().get(eventID).addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(variableID));
		}
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	public static void setupSingleParameter(ModelState modelstate, Parameter parameter, String parameterID)
	{
		modelstate.getVariableToValueMap().put(parameterID, parameter.getValue());

		if (parameter.isConstant())
		{
			modelstate.addVariableToIsConstant(parameterID);
		}
		if (!parameter.getConstant())
		{
			modelstate.getVariablesToPrint().add(parameterID);
		}
		if (parameter.getConstant() == false)
		{
			modelstate.getNonconstantParameterIDSet().add(parameterID);
		}

		if (modelstate.getNumRules() > 0)
		{
			modelstate.getVariableToIsInAssignmentRuleMap().put(parameterID, false);
		}

		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.getVariableToIsInConstraintMap().put(parameterID, false);
		}

	}

	/**
	 * calculates the initial propensity of a single reaction also does some
	 * initialization stuff
	 * 
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	public static void setupSingleReaction(ModelState modelstate, Reaction reaction, String reactionID, ASTNode reactionFormula, boolean reversible,
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList,
			Map<String, Model> models, Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{
		if (reversible)
		{
			setupSingleRevReaction(modelstate, reaction, reactionID, reactionFormula, reactantsList, productsList, modifiersList, models,
					iBioSimFunctionDefinitions, replacements, currentTime);
		}
		else
		{
			setupSingleNonRevReaction(modelstate, reaction, reactionID, reactionFormula, reactantsList, productsList, modifiersList, models,
					iBioSimFunctionDefinitions, replacements, currentTime);
		}

	}

	public static void setupSingleReactionPropensity(ModelState modelstate, String reactionID, ASTNode reactionFormula,
			boolean notEnoughMoleculesFlag, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements,
			double currentTime)
	{

		double propensity;

		if (notEnoughMoleculesFlag)
		{
			propensity = 0.0;
		}
		else
		{

			propensity = Evaluator.evaluateExpressionRecursive(modelstate,
					HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions), false, currentTime, null,
					null, replacements);
			if (propensity < 0.0)
			{
				propensity = 0.0;
			}

		}

		setPropensity(modelstate, reactionID, propensity);

	}

	public static void setupSingleReactionPropensity(ModelState modelstate, String reactionID, ASTNode reactionFormula,
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, boolean notEnoughMoleculesFlagFd,
			boolean notEnoughMoleculesFlagRv, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements,
			double currentTime)
	{

		double propensity;
		String forward = reactionID + "_fd";
		String reverse = reactionID + "_rv";

		modelstate.getReactionToHasEnoughMolecules().put(forward, notEnoughMoleculesFlagFd);
		modelstate.getReactionToHasEnoughMolecules().put(reverse, notEnoughMoleculesFlagRv);

		if (productsList.getChildCount() > 0 && reactantsList.getChildCount() > 0)
		{

			modelstate.getReactionToFormulaMap().put(reverse,
					HierarchicalUtilities.inlineFormula(modelstate, reactionFormula.getRightChild(), models, iBioSimFunctionDefinitions));
			modelstate.getReactionToFormulaMap().put(forward,
					HierarchicalUtilities.inlineFormula(modelstate, reactionFormula.getLeftChild(), models, iBioSimFunctionDefinitions));

			if (notEnoughMoleculesFlagFd == true)
			{
				propensity = 0.0;
			}
			else
			{
				propensity = Evaluator.evaluateExpressionRecursive(modelstate,
						HierarchicalUtilities.inlineFormula(modelstate, reactionFormula.getLeftChild(), models, iBioSimFunctionDefinitions), false,
						currentTime, null, null, replacements);

				// if (reactionID.contains("_Diffusion_") &&
				// isStoichAmpBoolean() == true)
				// {
				// propensity *= (1.0 / getStoichAmpGridValue());
				// }

				setPropensity(modelstate, reactionID, propensity);

				modelstate.setPropensity(modelstate.getPropensity() + propensity);
			}

			modelstate.getReactionToPropensityMap().put(forward, propensity);

			if (notEnoughMoleculesFlagRv == true)
			{
				propensity = 0.0;
			}
			else
			{

				propensity = Evaluator.evaluateExpressionRecursive(modelstate,
						HierarchicalUtilities.inlineFormula(modelstate, reactionFormula.getRightChild(), models, iBioSimFunctionDefinitions), false,
						currentTime, null, null, replacements);

				if (propensity < 0.0)
				{
					propensity = 0.0;
				}

			}

			setPropensity(modelstate, reverse, propensity);
		}
		else
		{
			if (reactantsList.getChildCount() > 0)
			{
				modelstate.getReactionToFormulaMap().put(forward,
						HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions));
				if (notEnoughMoleculesFlagRv == true)
				{
					propensity = 0.0;
				}
				else
				{
					propensity = Evaluator.evaluateExpressionRecursive(modelstate,
							HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions), false, currentTime,
							null, null, replacements);

					if (propensity < 0.0)
					{
						propensity = 0.0;
					}

				}

				setPropensity(modelstate, forward, propensity);

			}
			else if (productsList.getChildCount() > 0)
			{
				modelstate.getReactionToFormulaMap().put(forward,
						HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions));
				if (notEnoughMoleculesFlagFd == true)
				{
					propensity = 0.0;
				}
				else
				{
					propensity = Evaluator.evaluateExpressionRecursive(modelstate,
							HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions), false, currentTime,
							null, null, replacements);

					// if (reactionID.contains("_Diffusion_") &&
					// isStoichAmpBoolean() == true)
					// {
					// propensity *= (1.0 / getStoichAmpGridValue());
					// }

				}

				setPropensity(modelstate, forward, propensity);
			}

		}
	}

	public static void setupSingleAssignmentRule(ModelState modelstate, String variable, ASTNode math, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, models, iBioSimFunctionDefinitions);

		modelstate.getAssignmentRulesList().put(variable, math);

		ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

		if (math.getChildCount() == 0)
		{
			formulaChildren.add(math);
		}
		else
		{
			HierarchicalUtilities.getAllASTNodeChildren(math, formulaChildren);
		}

		for (ASTNode ruleNode : formulaChildren)
		{

			if (ruleNode.isName())
			{

				String nodeName = ruleNode.getName();
				if (ruleNode.getType() == ASTNode.Type.NAME_TIME)
				{
					nodeName = "_time";
				}
				if (!modelstate.getVariableToAffectedAssignmentRuleSetMap().containsKey(nodeName))
				{
					modelstate.getVariableToAffectedAssignmentRuleSetMap().put(nodeName, new HashSet<String>());
					modelstate.getVariableToIsInAssignmentRuleMap().put(nodeName, true);
				}

				modelstate.getVariableToAffectedAssignmentRuleSetMap().get(nodeName).add(variable);

			}

		}

	}

	public static void setupSingleRateRule(ModelState modelstate, String variable, ASTNode math, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions)
	{
		modelstate.getRateRulesList().put(variable, math);
	}

	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	public static void setupSingleSpecies(ModelState modelstate, Species species, String speciesID, Map<String, Model> models,
			Map<String, Double> replacements)
	{

		double initValue = 0;
		if (modelstate.getSpeciesIDSet().contains(speciesID))
		{
			return;
		}

		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.getVariableToIsInConstraintMap().put(speciesID, false);
		}

		if (modelstate.isHierarchical(speciesID))
		{
			if (species.isSetInitialConcentration())
			{
				initValue = modelstate.getVariableToValue(replacements, speciesID)
						* modelstate.getVariableToValue(replacements, species.getCompartment());
				modelstate.setVariableToValue(replacements, speciesID, initValue);
			}
		}
		else if (species.isSetInitialAmount())
		{
			initValue = species.getInitialAmount();
			modelstate.getVariableToValueMap().put(speciesID, initValue);
		}

		else if (species.isSetInitialConcentration())
		{
			initValue = species.getInitialConcentration() * models.get(modelstate.getModel()).getCompartment(species.getCompartment()).getSize();
			modelstate.getVariableToValueMap().put(speciesID, initValue);
		}
		else
		{
			modelstate.getVariableToValueMap().put(speciesID, initValue);
		}

		if (species.getHasOnlySubstanceUnits() == false)
		{
			modelstate.getSpeciesToCompartmentNameMap().put(speciesID, species.getCompartment());
		}
		if (modelstate.getNumRules() > 0)
		{
			modelstate.getVariableToIsInAssignmentRuleMap().put(speciesID, false);
		}

		modelstate.getSpeciesToAffectedReactionSetMap().put(speciesID, new HashSet<String>(20));

		modelstate.getSpeciesToIsBoundaryConditionMap().put(speciesID, species.getBoundaryCondition());

		if (species.getConstant())
		{
			modelstate.addVariableToIsConstant(speciesID);
		}
		modelstate.getSpeciesToHasOnlySubstanceUnitsMap().put(speciesID, species.getHasOnlySubstanceUnits());
		modelstate.getSpeciesIDSet().add(speciesID);

	}

	private static double getStoichiometry(ModelState modelstate, String reactionID, SpeciesReference reactant, Map<String, Double> replacements)
	{
		double reactantStoichiometry;

		if (modelstate.getVariableToValueMap().containsKey(reactant.getId()))
		{
			reactantStoichiometry = modelstate.getVariableToValue(replacements, reactant.getId());
		}
		else
		{
			reactantStoichiometry = reactant.getStoichiometry();
		}

		return reactantStoichiometry;
	}

	private static void setPropensity(ModelState modelstate, String reactionID, double propensity)
	{
		if (!modelstate.isArrayedObject(reactionID))
		{
			if (propensity < 0.0)
			{
				propensity = 0.0;
			}

			if (propensity < modelstate.getMinPropensity() && propensity > 0)
			{
				modelstate.setMinPropensity(propensity);
			}

			if (propensity > modelstate.getMaxPropensity())
			{
				modelstate.setMaxPropensity(propensity);
			}

			modelstate.setPropensity(modelstate.getPropensity() + propensity);

			modelstate.getReactionToPropensityMap().put(reactionID, propensity);
		}

	}

	private static ASTNode setupMath(ModelState modelstate, ASTNode reactionFormula, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions)
	{
		// TODO:fix this
		if (reactionFormula.getType().equals(ASTNode.Type.TIMES))
		{
			ASTNode distributedNode = new ASTNode();

			reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions);
			ASTNode temp = new ASTNode(1);
			if (reactionFormula.getChildCount() == 2 && reactionFormula.getChild(1).getType().equals(ASTNode.Type.PLUS))
			{
				distributedNode = ASTNode.sum(ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()),
						ASTNode.times(new ASTNode(-1), reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));
			}
			else if (reactionFormula.getChildCount() == 2 && reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
			{
				distributedNode = ASTNode.diff(ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()),
						ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));
			}
			else if (reactionFormula.getChildCount() >= 2)
			{
				for (ASTNode node : reactionFormula.getListOfNodes())
				{
					if (node.getChildCount() >= 2)
					{
						if (reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
						{
							distributedNode = ASTNode.sum(ASTNode.times(temp, node.getLeftChild()),
									ASTNode.times(temp, node.getRightChild().getRightChild()));
						}
						else
						{
							distributedNode = ASTNode.diff(ASTNode.times(temp, node.getLeftChild()),
									ASTNode.times(temp, node.getRightChild().getRightChild()));
						}

					}
					else
					{
						temp = ASTNode.times(temp, node);
					}
				}
			}

			if (distributedNode.isUnknown())
			{
				reactionFormula = temp;
			}
			else
			{
				reactionFormula = distributedNode;
			}
		}
		else if (reactionFormula.getType().equals(ASTNode.Type.MINUS))
		{

			ASTNode distributedNode = new ASTNode();
			ASTNode temp = new ASTNode(1);
			reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions);

			if (reactionFormula.getChildCount() == 1)
			{
				for (ASTNode node : reactionFormula.getChild(0).getListOfNodes())
				{
					if (node.getChildCount() >= 2)
					{
						if (reactionFormula.getChild(0).getType().equals(ASTNode.Type.MINUS))
						{
							distributedNode = ASTNode.sum(ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()),
									ASTNode.times(temp, node.getRightChild()));
						}
						else
						{
							distributedNode = ASTNode.diff(ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()),
									ASTNode.times(temp, node.getRightChild()));
						}

					}
					else
					{
						temp = ASTNode.times(temp, node);
					}
				}

				reactionFormula = distributedNode;
			}
		}
		else
		{
			reactionFormula = ASTNode.sum(reactionFormula, new ASTNode(0));
		}

		return reactionFormula;
	}

	private static void setupSingleModifier(ModelState modelstate, String reactionID, ModifierSpeciesReference modifier)
	{

		String modifierID = modifier.getSpecies();

		if (modelstate.getIsHierarchical().contains(modifierID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(reactionID);
	}

	private static void setupSingleNonRevReaction(ModelState modelstate, Reaction reaction, String reactionID, ASTNode reactionFormula,
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList,
			Map<String, Model> models, Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{
		boolean notEnoughMoleculesFlag;
		notEnoughMoleculesFlag = false;

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID, new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID, new HashSet<HierarchicalStringDoublePair>());

		for (SpeciesReference reactant : reactantsList)
		{
			notEnoughMoleculesFlag = setupSingleReactant(modelstate, reactionID, reactant, replacements);
		}

		for (SpeciesReference product : productsList)
		{
			setupSingleProduct(modelstate, reactionID, product, replacements);
		}

		for (ModifierSpeciesReference modifier : modifiersList)
		{
			setupSingleModifier(modelstate, reactionID, modifier);
		}
		reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, models, iBioSimFunctionDefinitions);
		modelstate.getReactionToFormulaMap().put(reactionID, reactionFormula);
		modelstate.getReactionToHasEnoughMolecules().put(reactionID, notEnoughMoleculesFlag);

		setupSingleReactionPropensity(modelstate, reactionID, reactionFormula, notEnoughMoleculesFlag, models, iBioSimFunctionDefinitions,
				replacements, currentTime);

	}

	private static void setupSingleProduct(ModelState modelstate, String reactionID, SpeciesReference product, Map<String, Double> replacements)
	{
		double productStoichiometry = getStoichiometry(modelstate, reactionID, product, replacements);

		String productID = product.getSpecies();

		if (modelstate.getIsHierarchical().contains(productID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(productID, productStoichiometry));

		if (!product.getConstant())
		{

			if (product.getId().length() > 0)
			{
				if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(reactionID) == false)
				{
					modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID, new HashSet<HierarchicalStringPair>());
				}

				modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID)
						.add(new HierarchicalStringPair(productID, product.getId()));

				if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
				{
					modelstate.setVariableToValue(replacements, product.getId(), productStoichiometry);
				}
			}
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(productID).add(reactionID);
	}

	private static boolean setupSingleReactant(ModelState modelstate, String reactionID, SpeciesReference reactant, Map<String, Double> replacements)
	{
		double reactantStoichiometry;
		boolean notEnoughMoleculesFlag;

		String reactantID = reactant.getSpecies();
		notEnoughMoleculesFlag = false;

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		reactantStoichiometry = getStoichiometry(modelstate, reactionID, reactant, replacements);

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(reactantID, -reactantStoichiometry));

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(reactionID);
		modelstate.getReactionToReactantStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));

		if (modelstate.getVariableToValue(replacements, reactantID) < reactantStoichiometry)
		{
			notEnoughMoleculesFlag = true;
		}

		if (!reactant.getConstant() && reactant.getId().length() > 0)
		{

			if (!modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(reactionID))
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID, new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID)
					.add(new HierarchicalStringPair(reactantID, "-" + reactant.getId()));
			if (!modelstate.getVariableToValueMap().containsKey(reactant.getId()))
			{
				modelstate.setVariableToValue(replacements, reactant.getId(), reactantStoichiometry);
			}
		}

		return notEnoughMoleculesFlag;
	}

	private static void setupSingleRevModifier(ModelState modelstate, String reactionID, ASTNode reactionFormula, ModifierSpeciesReference modifier)
	{
		String modifierID = modifier.getSpecies();

		String forward = reactionID + "_fd";
		String reverse = reactionID + "_rv";

		if (modelstate.getIsHierarchical().contains(modifierID))
		{
			modelstate.getHierarchicalReactions().add(forward);
			modelstate.getHierarchicalReactions().add(reverse);
		}
		modifierID = modifierID.replace("_negative_", "-");

		String forwardString = "", reverseString = "";

		try
		{
			forwardString = ASTNode.formulaToString(reactionFormula.getLeftChild());
			reverseString = ASTNode.formulaToString(reactionFormula.getRightChild());
		}
		catch (SBMLException e)
		{
			e.printStackTrace();
		}
		if (forwardString.contains(modifierID))
		{
			modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(forward);
		}

		if (reverseString.contains(modifierID))
		{
			modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(reverse);
		}
	}

	private static boolean setupSingleRevProduct(ModelState modelstate, String reactionID, SpeciesReference product,
			List<SpeciesReference> reactantsList, Map<String, Double> replacements)
	{
		String reactantID = product.getSpecies();

		String forward = reactionID + "_fd";
		String reverse = reactionID + "_rv";

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(forward);
			modelstate.getHierarchicalReactions().add(reverse);
		}

		double productStoichiometry = getStoichiometry(modelstate, reactionID, product, replacements);

		if (product.getConstant() == false && product.getId().length() > 0)
		{

			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(forward) == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(forward, new HashSet<HierarchicalStringPair>());
			}
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(reverse) == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reverse, new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(forward).add(new HierarchicalStringPair(reactantID, product.getId()));
			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reverse).add(new HierarchicalStringPair(reactantID, product.getId()));
			if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
			{
				modelstate.setVariableToValue(replacements, product.getId(), productStoichiometry);
			}
		}
		else if (reactantsList.size() == 0)
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(forward)
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));

		}
		else
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(forward)
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reverse)
					.add(new HierarchicalStringDoublePair(reactantID, -productStoichiometry));
			modelstate.getReactionToReactantStoichiometrySetMap().get(reverse)
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(reverse);
		if (modelstate.getVariableToValue(replacements, reactantID) < productStoichiometry)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean setupSingleRevReactant(ModelState modelstate, String reactionID, SpeciesReference reactant,
			List<SpeciesReference> productsList, Map<String, Double> replacements)
	{
		String reactantID = reactant.getSpecies();

		String forward = reactionID + "_fd";
		String reverse = reactionID + "_rv";

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(forward);
			modelstate.getHierarchicalReactions().add(reverse);
		}

		double reactantStoichiometry = getStoichiometry(modelstate, reactionID, reactant, replacements);

		if (reactant.getConstant() == false && reactant.getId().length() > 0)
		{

			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(forward) == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(forward, new HashSet<HierarchicalStringPair>());
			}
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(reverse) == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reverse, new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(forward).add(new HierarchicalStringPair(reactantID, reactant.getId()));
			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reverse).add(new HierarchicalStringPair(reactantID, reactant.getId()));
			if (modelstate.getVariableToValueMap().containsKey(reactant.getId()) == false)
			{
				modelstate.setVariableToValue(replacements, reactant.getId(), reactantStoichiometry);
			}
		}
		else if (productsList.size() == 0)
		{
			modelstate.getReactionToReactantStoichiometrySetMap().get(forward)
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));

		}
		else
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(forward)
					.add(new HierarchicalStringDoublePair(reactantID, -reactantStoichiometry));
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reverse)
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));
			modelstate.getReactionToReactantStoichiometrySetMap().get(forward)
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(forward);

		if (modelstate.getVariableToValue(replacements, reactantID) < reactantStoichiometry)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static void setupSingleRevReaction(ModelState modelstate, Reaction reaction, String reactionID, ASTNode reactionFormula,
			ListOf<SpeciesReference> reactantsList, ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList,
			Map<String, Model> models, Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{

		String forward = reactionID + "_fd";
		String reverse = reactionID + "_rv";
		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(forward, new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reverse, new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(forward, new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(reverse, new HashSet<HierarchicalStringDoublePair>());

		reactionFormula = setupMath(modelstate, reactionFormula, models, iBioSimFunctionDefinitions);

		for (SpeciesReference reactant : reactantsList)
		{
			notEnoughMoleculesFlagFd = setupSingleRevReactant(modelstate, reactionID, reactant, productsList, replacements);
		}

		for (SpeciesReference product : productsList)
		{
			notEnoughMoleculesFlagRv = setupSingleRevProduct(modelstate, reactionID, product, reactantsList, replacements);
		}

		for (ModifierSpeciesReference modifier : modifiersList)
		{
			setupSingleRevModifier(modelstate, reactionID, reactionFormula, modifier);
		}

		setupSingleReactionPropensity(modelstate, reactionID, reactionFormula, reactantsList, productsList, notEnoughMoleculesFlagFd,
				notEnoughMoleculesFlagRv, models, iBioSimFunctionDefinitions, replacements, currentTime);

	}

	public static boolean calcAssignmentRules(ModelState modelstate, Map<String, ASTNode> affectedAssignmentRuleSet, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{

		boolean changed = false;
		boolean temp = false;
		double newResult, oldResult;
		for (String variable : affectedAssignmentRuleSet.keySet())
		{

			ASTNode math = affectedAssignmentRuleSet.get(variable);

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (modelstate.isConstant(variable) == false)
			{

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
				{

					oldResult = modelstate.getVariableToValue(replacements, variable);
					newResult = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements)
							* modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable));
					if (oldResult != newResult)
					{
						modelstate.setVariableToValue(replacements, variable, newResult);
						temp = true;
					}
				}
				else
				{
					oldResult = modelstate.getVariableToValue(replacements, variable);
					newResult = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);

					if (oldResult != newResult)
					{
						modelstate.setVariableToValue(replacements, variable, newResult);
						temp = true;
					}
				}

				changed |= temp;
			}
		}

		return changed;
	}

	public static boolean calcCompInitAssign(ModelState modelstate, String variable, ASTNode initialAssignment, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{
		double newResult = Evaluator.evaluateExpressionRecursive(modelstate, initialAssignment, false, currentTime, null, null, replacements);
		double oldResult = modelstate.getVariableToValue(replacements, variable);

		if (newResult != oldResult)
		{
			if (oldResult == Double.NaN)
			{
				oldResult = 1.0;
			}

			modelstate.setVariableToValue(replacements, variable, newResult);
			if (modelstate.getNumRules() > 0)
			{
				HashSet<String> rules = modelstate.getVariableToAffectedAssignmentRuleSetMap().get(variable);

				HierarchicalUtilities.performAssignmentRules(modelstate, rules, replacements, currentTime);
			}

			return true;
		}

		return false;
	}

	public static boolean calcParamInitAssign(ModelState modelstate, String variable, ASTNode initialAssignment, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{
		double newResult = Evaluator.evaluateExpressionRecursive(modelstate, initialAssignment, false, currentTime, null, null, replacements);
		double oldResult = modelstate.getVariableToValue(replacements, variable);

		if (newResult != oldResult)
		{
			modelstate.setVariableToValue(replacements, variable, newResult);

			return true;
		}

		return false;
	}

	public static boolean calcSpeciesInitAssign(ModelState modelstate, String variable, ASTNode initialAssignment, Map<String, Model> models,
			Set<String> iBioSimFunctionDefinitions, Map<String, Double> replacements, double currentTime)
	{
		double newResult;
		if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
				&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
		{

			newResult = Evaluator.evaluateExpressionRecursive(modelstate, initialAssignment, false, currentTime, null, null, replacements)
					* modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable));
			if (newResult != modelstate.getVariableToValue(replacements, variable))
			{
				modelstate.setVariableToValue(replacements, variable, newResult);
				return true;
			}

		}
		else
		{
			newResult = Evaluator.evaluateExpressionRecursive(modelstate, initialAssignment, false, currentTime, null, null, replacements);

			if (newResult != modelstate.getVariableToValue(replacements, variable))
			{
				modelstate.setVariableToValue(replacements, variable, newResult);
				return true;
			}
		}
		return false;
	}

	public static void calculateInitAssignments(ModelState modelstate, Map<String, ASTNode> allInitAssignment,
			Map<String, ASTNode> allAssignmentRules, Map<String, Model> models, Set<String> iBioSimFunctionDefinitions,
			Map<String, Double> replacements, double currentTime)
	{
		long maxIterations = 1000;
		long numIterations = 0;
		double newResult = 0;
		boolean changed = true, temp = false;

		while (changed)
		{
			if (numIterations > maxIterations)
			{
				System.out.println("Error: not converging");
				return;
			}

			changed = false;
			temp = false;
			numIterations++;
			for (String variable : allInitAssignment.keySet())
			{
				ASTNode math = allInitAssignment.get(variable);

				if (models.get(modelstate.getModel()).containsSpecies(variable))
				{
					temp = calcSpeciesInitAssign(modelstate, variable, math, models, iBioSimFunctionDefinitions, replacements, currentTime);
				}
				else if (models.get(modelstate.getModel()).containsCompartment(variable))
				{
					temp = calcCompInitAssign(modelstate, variable, math, models, iBioSimFunctionDefinitions, replacements, currentTime);
				}
				else if (models.get(modelstate.getModel()).containsParameter(variable))
				{
					temp = calcParamInitAssign(modelstate, variable, math, models, iBioSimFunctionDefinitions, replacements, currentTime);
				}
				else
				{
					newResult = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);
					if (newResult != modelstate.getVariableToValue(replacements, variable))
					{
						modelstate.setVariableToValue(replacements, variable, newResult);
						temp = true;
					}
				}

				changed |= temp;
			}

			changed |= calcAssignmentRules(modelstate, allAssignmentRules, models, iBioSimFunctionDefinitions, replacements, currentTime);
		}
	}
}
