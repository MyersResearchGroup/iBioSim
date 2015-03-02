package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import biomodel.util.SBMLutilities;

public abstract class HierarchicalSingleSBaseSetup extends HierarchicalReplacemenHandler
{

	public HierarchicalSingleSBaseSetup(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);
	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	protected void setupLocalParameters(ModelState modelstate, KineticLaw kineticLaw,
			Reaction reaction)
	{

		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_", "-");

		for (int i = 0; i < kineticLaw.getLocalParameterCount(); i++)
		{

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);

			if (localParameter.isSetId() && modelstate.isDeletedBySID(localParameter.getId()))
			{
				continue;
			}
			if (localParameter.isSetMetaId()
					&& modelstate.isDeletedByMetaID(localParameter.getMetaId()))
			{
				continue;
			}

			String parameterID = "";

			// the parameters don't get reset after each run, so don't re-do
			// this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)
			{
				parameterID = reactionID + "_" + localParameter.getId();
			}
			else
			{
				parameterID = localParameter.getId();
			}

			String oldParameterID = localParameter.getId();
			modelstate.getVariableToValueMap().put(parameterID, localParameter.getValue());

			// alter the local parameter ID so that it goes to the local and not
			// global value
			if (localParameter.getId() != parameterID)
			{
				localParameter.setId(parameterID);
				SBMLutilities.setMetaId(localParameter, parameterID);
			}
			HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), reaction,
					oldParameterID, parameterID);
		}
	}

	protected void setupSingleCompartment(ModelState modelstate, Compartment compartment)
	{
		String compartmentID = compartment.getId();

		if (compartment.isSetId() && modelstate.isDeletedBySID(compartment.getId()))
		{
			return;
		}
		else if (compartment.isSetMetaId() && modelstate.isDeletedByMetaID(compartment.getMetaId()))
		{
			return;
		}

		modelstate.getCompartmentIDSet().add(compartmentID);
		modelstate.getVariableToValueMap().put(compartmentID, compartment.getSize());

		if (Double.isNaN(compartment.getSize()))
		{
			modelstate.setvariableToValueMap(getReplacements(), compartmentID, 1.0);
		}

		modelstate.getVariableToIsConstantMap().put(compartmentID, compartment.getConstant());

		if (!compartment.getConstant())
		{
			modelstate.getVariablesToPrint().add(compartmentID);
		}

		if (modelstate.getNumRules() > 0)
		{
			modelstate.getVariableToIsInAssignmentRuleMap().put(compartmentID, false);
		}

		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.getVariableToIsInConstraintMap().put(compartmentID, false);
		}
	}

	protected void setupSingleConstraint(ModelState modelstate, Constraint constraint)
	{
		if (constraint.isSetMetaId() && modelstate.isDeletedByMetaID(constraint.getMetaId()))
		{
			return;
		}
		constraint.setMath(inlineFormula(modelstate, constraint.getMath()));
		for (ASTNode constraintNode : constraint.getMath().getListOfNodes())
		{

			if (constraintNode.isName())
			{

				String nodeName = constraintNode.getName();
				modelstate.getVariableToAffectedConstraintSetMap().put(nodeName,
						new HashSet<ASTNode>());
				modelstate.getVariableToAffectedConstraintSetMap().get(nodeName)
						.add(constraint.getMath());
				modelstate.getVariableToIsInConstraintMap().put(nodeName, true);
			}
		}

	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(ModelState modelstate, Event event)
	{

		String eventID = event.getId();

		if (event.isSetPriority())
		{
			modelstate.getEventToPriorityMap().put(eventID,
					inlineFormula(modelstate, event.getPriority().getMath()));
		}

		if (event.isSetDelay())
		{

			modelstate.getEventToDelayMap().put(eventID,
					inlineFormula(modelstate, event.getDelay().getMath()));
			modelstate.getEventToHasDelayMap().put(eventID, true);
		}
		else
		{
			modelstate.getEventToHasDelayMap().put(eventID, false);
		}

		event.getTrigger().setMath(inlineFormula(modelstate, event.getTrigger().getMath()));

		modelstate.getEventToTriggerMap().put(eventID, event.getTrigger().getMath());
		modelstate.getEventToTriggerInitiallyTrueMap().put(eventID,
				event.getTrigger().getInitialValue());
		modelstate.getEventToPreviousTriggerValueMap().put(eventID,
				event.getTrigger().getInitialValue());
		modelstate.getEventToTriggerPersistenceMap().put(eventID,
				event.getTrigger().getPersistent());
		modelstate.getEventToUseValuesFromTriggerTimeMap().put(eventID,
				event.getUseValuesFromTriggerTime());
		modelstate.getEventToAssignmentSetMap().put(eventID, new HashSet<Object>());
		modelstate.getEventToAffectedReactionSetMap().put(eventID, new HashSet<String>());

		modelstate.getUntriggeredEventSet().add(eventID);

		for (EventAssignment assignment : event.getListOfEventAssignments())
		{

			String variableID = assignment.getVariable();

			assignment.setMath(inlineFormula(modelstate, assignment.getMath()));

			modelstate.getEventToAssignmentSetMap().get(eventID).add(assignment);

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

				modelstate.getEventToAffectedReactionSetMap().get(eventID)
						.addAll(modelstate.getSpeciesToAffectedReactionSetMap().get(variableID));
			}

		}
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	protected void setupSingleParameter(ModelState modelstate, Parameter parameter, int i, int j)
	{

		String parameterID = parameter.getId();
		modelstate.getVariableToValueMap().put(parameterID, parameter.getValue());
		modelstate.getVariableToIsConstantMap().put(parameterID, parameter.getConstant());
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

	private void setupSingleNonRevReaction(ModelState modelstate, Reaction reaction,
			String reactionID, ASTNode reactionFormula, ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList)
	{
		boolean notEnoughMoleculesFlag;
		notEnoughMoleculesFlag = false;

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID,
				new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID,
				new HashSet<HierarchicalStringDoublePair>());

		for (SpeciesReference reactant : reactantsList)
		{
			notEnoughMoleculesFlag = setupSingleReactant(modelstate, reactionID, reactant);
		}

		for (SpeciesReference product : productsList)
		{
			setupSingleProduct(modelstate, reactionID, product);
		}

		for (ModifierSpeciesReference modifier : modifiersList)
		{
			setupSingleModifier(modelstate, reactionID, modifier);
		}
		reactionFormula = inlineFormula(modelstate, reactionFormula);
		modelstate.getReactionToFormulaMap().put(reactionID, reactionFormula);

		setupSingleReactionPropensity(modelstate, reactionID, reactionFormula,
				notEnoughMoleculesFlag);
	}

	private void setupSingleRevReaction(ModelState modelstate, Reaction reaction,
			String reactionID, ASTNode reactionFormula, ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList)
	{
		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID + "_fd",
				new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID + "_rv",
				new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID + "_fd",
				new HashSet<HierarchicalStringDoublePair>());
		modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID + "_rv",
				new HashSet<HierarchicalStringDoublePair>());

		setupMath(modelstate, reactionFormula);

		for (SpeciesReference reactant : reactantsList)
		{
			notEnoughMoleculesFlagFd = setupSingleRevReactant(modelstate, reactionID, reactant,
					productsList);
		}

		for (SpeciesReference product : productsList)
		{
			notEnoughMoleculesFlagRv = setupSingleRevProduct(modelstate, reactionID, product,
					reactantsList);
		}

		for (ModifierSpeciesReference modifier : modifiersList)
		{
			setupSingleRevModifier(modelstate, reactionID, reactionFormula, modifier);
		}

		setupSingleReactionPropensity(modelstate, reactionID, reactionFormula, reactantsList,
				productsList, notEnoughMoleculesFlagFd, notEnoughMoleculesFlagRv);
	}

	protected void setupSingleRule(ModelState modelstate, Rule rule)
	{
		if (rule.isSetMetaId() && modelstate.isDeletedByMetaID(rule.getMetaId()))
		{
			return;
		}

		if (rule.isAssignment())
		{
			rule.setMath(inlineFormula(modelstate, rule.getMath()));
			AssignmentRule assignmentRule = (AssignmentRule) rule;
			ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

			if (assignmentRule.getMath().getChildCount() == 0)
			{
				formulaChildren.add(assignmentRule.getMath());
			}
			else
			{
				HierarchicalUtilities.getAllASTNodeChildren(assignmentRule.getMath(),
						formulaChildren);
			}

			for (ASTNode ruleNode : formulaChildren)
			{

				if (ruleNode.isName())
				{

					String nodeName = ruleNode.getName();

					modelstate.getVariableToAffectedAssignmentRuleSetMap().put(nodeName,
							new HashSet<AssignmentRule>());
					modelstate.getVariableToAffectedAssignmentRuleSetMap().get(nodeName)
							.add(assignmentRule);
					modelstate.getVariableToIsInAssignmentRuleMap().put(nodeName, true);
				}
			}

			modelstate.getAssignmentRulesList().add(assignmentRule);
		}
		else if (rule.isRate())
		{
			RateRule rateRule = (RateRule) rule;
			modelstate.getRateRulesList().add(rateRule);
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
	protected void setupSingleReaction(ModelState modelstate, Reaction reaction, String reactionID,
			ASTNode reactionFormula, boolean reversible, ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList)
	{
		if (reversible)
		{
			setupSingleRevReaction(modelstate, reaction, reactionID, reactionFormula,
					reactantsList, productsList, modifiersList);
		}
		else
		{
			setupSingleNonRevReaction(modelstate, reaction, reactionID, reactionFormula,
					reactantsList, productsList, modifiersList);
		}

	}

	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	protected void setupSingleSpecies(ModelState modelstate, Species species, String speciesID)
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

		if (!modelstate.getIsHierarchical().contains(speciesID) && species.isSetInitialAmount())
		{
			initValue = species.getInitialAmount();
			modelstate.setvariableToValueMap(getReplacements(), speciesID,
					species.getInitialAmount());
		}

		else if (species.isSetInitialConcentration())
		{
			initValue = species.getInitialConcentration()
					* getModels().get(modelstate.getModel())
							.getCompartment(species.getCompartment()).getSize();
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

		modelstate.getSpeciesToIsBoundaryConditionMap().put(speciesID,
				species.getBoundaryCondition());
		modelstate.getVariableToIsConstantMap().put(speciesID, species.getConstant());
		modelstate.getSpeciesToHasOnlySubstanceUnitsMap().put(speciesID,
				species.getHasOnlySubstanceUnits());
		modelstate.getSpeciesIDSet().add(speciesID);

	}

	private void setupSingleProduct(ModelState modelstate, String reactionID,
			SpeciesReference product)
	{
		double productStoichiometry;

		String productID = product.getSpecies();

		if (modelstate.getIsHierarchical().contains(productID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		if (modelstate.getVariableToValueMap().containsKey(product.getId()))
		{
			productStoichiometry = modelstate
					.getVariableToValue(getReplacements(), product.getId());
		}
		else
		{
			productStoichiometry = product.getStoichiometry();
		}

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(productID, productStoichiometry));

		if (!product.getConstant())
		{

			if (product.getId().length() > 0)
			{
				modelstate.getVariableToIsConstantMap().put(product.getId(), false);
				if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(
						reactionID) == false)
				{
					modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID,
							new HashSet<HierarchicalStringPair>());
				}

				modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID)
						.add(new HierarchicalStringPair(productID, product.getId()));

				if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
				{
					modelstate.setvariableToValueMap(getReplacements(), product.getId(),
							productStoichiometry);
				}
			}
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(productID).add(reactionID);
	}

	private boolean setupSingleReactant(ModelState modelstate, String reactionID,
			SpeciesReference reactant)
	{
		double reactantStoichiometry;
		boolean notEnoughMoleculesFlag;

		String reactantID = reactant.getSpecies();
		notEnoughMoleculesFlag = false;

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		if (modelstate.getVariableToValueMap().containsKey(reactant.getId()))
		{
			reactantStoichiometry = modelstate.getVariableToValue(getReplacements(),
					reactant.getId());
		}
		else
		{
			reactantStoichiometry = reactant.getStoichiometry();
		}

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(reactantID, -reactantStoichiometry));

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(reactionID);
		modelstate.getReactionToReactantStoichiometrySetMap().get(reactionID)
				.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));

		if (modelstate.getVariableToValue(getReplacements(), reactantID) < reactantStoichiometry)
		{
			notEnoughMoleculesFlag = true;
		}

		if (!reactant.getConstant() && reactant.getId().length() > 0)
		{

			if (!modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(reactionID))
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID,
						new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID)
					.add(new HierarchicalStringPair(reactantID, "-" + reactant.getId()));
			if (!modelstate.getVariableToValueMap().containsKey(reactant.getId()))
			{
				modelstate.setvariableToValueMap(getReplacements(), reactant.getId(),
						reactantStoichiometry);
			}
		}

		return notEnoughMoleculesFlag;
	}

	private void setupSingleModifier(ModelState modelstate, String reactionID,
			ModifierSpeciesReference modifier)
	{

		String modifierID = modifier.getSpecies();

		if (modelstate.getIsHierarchical().contains(modifierID))
		{
			modelstate.getHierarchicalReactions().add(reactionID);
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(reactionID);
	}

	private void setupSingleReactionPropensity(ModelState modelstate, String reactionID,
			ASTNode reactionFormula, ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList, boolean notEnoughMoleculesFlagFd,
			boolean notEnoughMoleculesFlagRv)
	{
		double propensity;

		if (productsList.getChildCount() > 0 && reactantsList.getChildCount() > 0)
		{
			modelstate.getReactionToFormulaMap().put(reactionID + "_rv",
					inlineFormula(modelstate, reactionFormula.getRightChild()));
			modelstate.getReactionToFormulaMap().put(reactionID + "_fd",
					inlineFormula(modelstate, reactionFormula.getLeftChild()));
			if (notEnoughMoleculesFlagFd == true)
			{
				propensity = 0.0;
			}
			else
			{
				propensity = evaluateExpressionRecursive(modelstate,
						inlineFormula(modelstate, reactionFormula.getLeftChild()), false,
						getCurrentTime(), null, null);

				if (reactionID.contains("_Diffusion_") && isStoichAmpBoolean() == true)
				{
					propensity *= (1.0 / getStoichAmpGridValue());
				}

				if ((propensity < modelstate.getMinPropensity()) && (propensity > 0))
				{
					modelstate.setMinPropensity(propensity);
				}

				if (propensity > modelstate.getMaxPropensity())
				{
					modelstate.setMaxPropensity(propensity);
				}

				modelstate.setPropensity(modelstate.getPropensity() + propensity);
			}

			modelstate.getReactionToPropensityMap().put(reactionID + "_fd", propensity);

			if (notEnoughMoleculesFlagRv == true)
			{
				propensity = 0.0;
			}
			else
			{

				propensity = evaluateExpressionRecursive(modelstate,
						inlineFormula(modelstate, reactionFormula.getRightChild()), false,
						getCurrentTime(), null, null);

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

				modelstate.setMaxPropensity(modelstate.getMaxPropensity() + propensity);
			}

			modelstate.getReactionToPropensityMap().put(reactionID + "_rv", propensity);
		}
		else
		{
			if (reactantsList.getChildCount() > 0)
			{
				modelstate.getReactionToFormulaMap().put(reactionID + "_fd",
						inlineFormula(modelstate, reactionFormula));
				if (notEnoughMoleculesFlagRv == true)
				{
					propensity = 0.0;
				}
				else
				{
					propensity = evaluateExpressionRecursive(modelstate,
							inlineFormula(modelstate, reactionFormula), false, getCurrentTime(),
							null, null);

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
				}

				modelstate.getReactionToPropensityMap().put(reactionID + "_fd", propensity);
			}
			else if (productsList.getChildCount() > 0)
			{
				modelstate.getReactionToFormulaMap().put(reactionID + "_fd",
						inlineFormula(modelstate, reactionFormula));
				if (notEnoughMoleculesFlagFd == true)
				{
					propensity = 0.0;
				}
				else
				{
					propensity = evaluateExpressionRecursive(modelstate,
							inlineFormula(modelstate, reactionFormula), false, getCurrentTime(),
							null, null);

					if (reactionID.contains("_Diffusion_") && isStoichAmpBoolean() == true)
					{
						propensity *= (1.0 / getStoichAmpGridValue());
					}

					if ((propensity < modelstate.getMinPropensity()) && (propensity > 0))
					{
						modelstate.setMinPropensity(propensity);
					}

					if (propensity > modelstate.getMaxPropensity())
					{
						modelstate.setMaxPropensity(propensity);
					}

					modelstate.setPropensity(modelstate.getPropensity() + propensity);
					// totalPropensity += propensity;
				}

				modelstate.getReactionToPropensityMap().put(reactionID + "_fd", propensity);
			}

		}
	}

	private void setupSingleReactionPropensity(ModelState modelstate, String reactionID,
			ASTNode reactionFormula, boolean notEnoughMoleculesFlag)
	{

		double propensity;

		if (notEnoughMoleculesFlag)
		{
			propensity = 0.0;
		}
		else
		{

			propensity = evaluateExpressionRecursive(modelstate,
					inlineFormula(modelstate, reactionFormula), false, getCurrentTime(), null, null);
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

		}

		modelstate.getReactionToPropensityMap().put(reactionID, propensity);
	}

	private boolean setupSingleRevProduct(ModelState modelstate, String reactionID,
			SpeciesReference product, List<SpeciesReference> reactantsList)
	{
		String reactantID = product.getSpecies();

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(reactionID + "_fd");
			modelstate.getHierarchicalReactions().add(reactionID + "_rv");
		}

		double productStoichiometry;

		if (modelstate.getVariableToValueMap().contains(product.getId()))
		{
			productStoichiometry = modelstate
					.getVariableToValue(getReplacements(), product.getId());
		}
		else
		{
			productStoichiometry = product.getStoichiometry();
		}

		if (product.getConstant() == false && product.getId().length() > 0)
		{

			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(
					reactionID + "_fd") == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID + "_fd",
						new HashSet<HierarchicalStringPair>());
			}
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(
					reactionID + "_rv") == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID + "_rv",
						new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringPair(reactantID, product.getId()));
			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID + "_rv")
					.add(new HierarchicalStringPair(reactantID, product.getId()));
			if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
			{
				modelstate.setvariableToValueMap(getReplacements(), product.getId(),
						productStoichiometry);
			}
		}
		else if (reactantsList.size() == 0)
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));

		}
		else
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID + "_rv")
					.add(new HierarchicalStringDoublePair(reactantID, -productStoichiometry));
			modelstate.getReactionToReactantStoichiometrySetMap().get(reactionID + "_rv")
					.add(new HierarchicalStringDoublePair(reactantID, productStoichiometry));
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(reactionID + "_rv");
		if (modelstate.getVariableToValue(getReplacements(), reactantID) < productStoichiometry)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean setupSingleRevReactant(ModelState modelstate, String reactionID,
			SpeciesReference reactant, List<SpeciesReference> productsList)
	{
		String reactantID = reactant.getSpecies();

		if (modelstate.getIsHierarchical().contains(reactantID))
		{
			modelstate.getHierarchicalReactions().add(reactionID + "_fd");
			modelstate.getHierarchicalReactions().add(reactionID + "_rv");
		}

		double reactantStoichiometry;

		if (modelstate.getVariableToValueMap().contains(reactant.getId()))
		{
			reactantStoichiometry = modelstate.getVariableToValue(getReplacements(),
					reactant.getId());
		}
		else
		{
			reactantStoichiometry = reactant.getStoichiometry();
		}

		if (reactant.getConstant() == false && reactant.getId().length() > 0)
		{

			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(
					reactionID + "_fd") == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID + "_fd",
						new HashSet<HierarchicalStringPair>());
			}
			if (modelstate.getReactionToNonconstantStoichiometriesSetMap().containsKey(
					reactionID + "_rv") == false)
			{
				modelstate.getReactionToNonconstantStoichiometriesSetMap().put(reactionID + "_rv",
						new HashSet<HierarchicalStringPair>());
			}

			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringPair(reactantID, reactant.getId()));
			modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reactionID + "_rv")
					.add(new HierarchicalStringPair(reactantID, reactant.getId()));
			if (modelstate.getVariableToValueMap().containsKey(reactant.getId()) == false)
			{
				modelstate.setvariableToValueMap(getReplacements(), reactant.getId(),
						reactantStoichiometry);
			}
		}
		else if (productsList.size() == 0)
		{
			modelstate.getReactionToReactantStoichiometrySetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));

		}
		else
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringDoublePair(reactantID, -reactantStoichiometry));
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reactionID + "_rv")
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));
			modelstate.getReactionToReactantStoichiometrySetMap().get(reactionID + "_fd")
					.add(new HierarchicalStringDoublePair(reactantID, reactantStoichiometry));
		}

		modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(reactionID + "_fd");
		if (modelstate.getVariableToValue(getReplacements(), reactantID) < reactantStoichiometry)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private void setupSingleRevModifier(ModelState modelstate, String reactionID,
			ASTNode reactionFormula, ModifierSpeciesReference modifier)
	{
		String modifierID = modifier.getSpecies();
		if (modelstate.getIsHierarchical().contains(modifierID))
		{
			modelstate.getHierarchicalReactions().add(reactionID + "_fd");
			modelstate.getHierarchicalReactions().add(reactionID + "_rv");
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
			modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(reactionID + "_fd");
		}

		if (reverseString.contains(modifierID))
		{
			modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(reactionID + "_rv");
		}
	}

	private void setupMath(ModelState modelstate, ASTNode reactionFormula)
	{
		if (reactionFormula.getType().equals(ASTNode.Type.TIMES))
		{

			ASTNode distributedNode = new ASTNode();

			reactionFormula = inlineFormula(modelstate, reactionFormula);
			ASTNode temp = new ASTNode(1);
			if (reactionFormula.getChildCount() == 2
					&& reactionFormula.getChild(1).getType().equals(ASTNode.Type.PLUS))
			{
				distributedNode = ASTNode.sum(ASTNode.times(reactionFormula.getLeftChild(),
						reactionFormula.getRightChild().getLeftChild()), ASTNode.times(new ASTNode(
						-1), reactionFormula.getLeftChild(), reactionFormula.getRightChild()
						.getRightChild()));
			}
			else if (reactionFormula.getChildCount() == 2
					&& reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
			{
				distributedNode = ASTNode.diff(ASTNode.times(reactionFormula.getLeftChild(),
						reactionFormula.getRightChild().getLeftChild()), ASTNode.times(
						reactionFormula.getLeftChild(), reactionFormula.getRightChild()
								.getRightChild()));
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
							distributedNode = ASTNode.diff(
									ASTNode.times(temp, node.getLeftChild()),
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
			reactionFormula = inlineFormula(modelstate, reactionFormula);

			if (reactionFormula.getChildCount() == 1)
			{
				for (ASTNode node : reactionFormula.getChild(0).getListOfNodes())
				{
					if (node.getChildCount() >= 2)
					{
						if (reactionFormula.getChild(0).getType().equals(ASTNode.Type.MINUS))
						{
							distributedNode = ASTNode.sum(
									ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()),
									ASTNode.times(temp, node.getRightChild()));
						}
						else
						{
							distributedNode = ASTNode.diff(
									ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()),
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

	}
}
