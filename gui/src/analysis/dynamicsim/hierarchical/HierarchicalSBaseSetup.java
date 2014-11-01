package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
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

public abstract class HierarchicalSBaseSetup extends HierarchicalReplacemenHandler {

	public HierarchicalSBaseSetup(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval,
			double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType) throws IOException, XMLStreamException {
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep,
				minTimeStep, progress, printInterval, stoichAmpValue, running,
				interestingSpecies, quantityType);
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private static void setupSingleParameter(ModelState modelstate,
			Parameter parameter, boolean vector, boolean matrix, int i, int j) {

		String parameterID = parameter.getId();

		if (vector)
			parameterID = parameterID + "__" + i;
		else if (matrix)
			parameterID = parameterID + "__" + i + "__" + j;

		modelstate.getVariableToValueMap().put(parameterID, parameter.getValue());
		modelstate.getVariableToIsConstantMap().put(parameterID,
				parameter.getConstant());
		if (!parameter.getConstant())
			modelstate.getVariablesToPrint().add(parameterID);
		if (parameter.getConstant() == false)
			modelstate.getNonconstantParameterIDSet().add(parameterID);

		if (modelstate.getNumRules() > 0)
			modelstate.getVariableToIsInAssignmentRuleMap().put(parameterID, false);

		if (modelstate.getNumConstraints() > 0)
			modelstate.getVariableToIsInConstraintMap().put(parameterID, false);
	}

	protected boolean calcAssignmentRules(ModelState modelstate,
			HashSet<AssignmentRule> affectedAssignmentRuleSet) {

		boolean changed = false;
		boolean temp = false;
		double newResult, oldResult;
		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {

			String variable = assignmentRule.getVariable();

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (modelstate.getVariableToIsConstantMap().containsKey(variable)
					&& modelstate.getVariableToIsConstantMap().get(variable) == false
					|| modelstate.getVariableToIsConstantMap().containsKey(variable) == false) {

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap()
						.containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap()
								.get(variable) == false) {

					oldResult = modelstate.getVariableToValue(getReplacements(),variable);
					newResult = evaluateExpressionRecursive(modelstate,
							assignmentRule.getMath())
							* modelstate
									.getVariableToValue(getReplacements(),modelstate.getSpeciesToCompartmentNameMap()
											.get(variable));
					if (oldResult != newResult) {
						modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
						temp = true;
					}
				} else {
					oldResult = modelstate.getVariableToValue(getReplacements(),variable);
					newResult = evaluateExpressionRecursive(modelstate,
							assignmentRule.getMath());

					if (oldResult != newResult) {
						modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
						temp = true;
					}
				}

				changed |= temp;
			}
		}

		return changed;
	}

	private boolean calcCompInitAssign(ModelState modelstate, String variable,
			InitialAssignment initialAssignment) {
		double newResult = evaluateExpressionRecursive(modelstate,
				initialAssignment.getMath());
		double oldResult = modelstate.getVariableToValue(getReplacements(),variable);
		//double speciesVal = 0;
		if (newResult != oldResult) {
			if (oldResult == Double.NaN)
				oldResult = 1.0;

			modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
			if (modelstate.getNumRules() > 0) {
				HashSet<AssignmentRule> rules = modelstate.getVariableToAffectedAssignmentRuleSetMap()
						.get(variable);

				performAssignmentRules(modelstate, rules);
			}
			// TODO: NEED TO FIX THIS
			/*
			 * for(String species : modelstate.getSpeciesIDSet()) {
			 * 
			 * 
			 * if(modelstate.getSpeciesToCompartmentNameMap().get(species).equals(
			 * variable)) if
			 * (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey
			 * (species) &&
			 * modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(species) ==
			 * false) { speciesVal = modelstate.getVariableToValue(getReplacements(),species);
			 * 
			 * if(getModels().get(modelstate.getModel()).getSpecies(species).
			 * isSetInitialConcentration()) speciesVal =
			 * getModels().get(modelstate.getModel()
			 * ).getSpecies(species).getInitialConcentration();
			 * 
			 * newResult = (speciesVal) *
			 * modelstate.getVariableToValue(getReplacements(),modelstate
			 * .getSpeciesToCompartmentNameMap().get(species));
			 * modelstate.setvariableToValueMap(getReplacements(),species, newResult); } }
			 */
			return true;
		}

		return false;
	}

	private boolean calcParamInitAssign(ModelState modelstate, String variable,
			InitialAssignment initialAssignment) {
		double newResult = evaluateExpressionRecursive(modelstate,
				initialAssignment.getMath());
		double oldResult = modelstate.getVariableToValue(getReplacements(),variable);

		// if(Double.compare(newResult, oldResult) == 0)
		// return false;

		if (newResult != oldResult) {
			modelstate.setvariableToValueMap(getReplacements(),variable, newResult);

			return true;
		}

		return false;
	}

	private boolean calcSpeciesInitAssign(ModelState modelstate,
			String variable, InitialAssignment initialAssignment) {
		double newResult;
		if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
				&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false) {

			newResult = evaluateExpressionRecursive(modelstate,
					initialAssignment.getMath())
					* modelstate
							.getVariableToValue(getReplacements(),modelstate.getSpeciesToCompartmentNameMap()
									.get(variable));
			if (newResult != modelstate.getVariableToValue(getReplacements(),variable)) {
				modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
				return true;
			}

		} else {
			newResult = evaluateExpressionRecursive(modelstate,
					initialAssignment.getMath());

			if (newResult != modelstate.getVariableToValue(getReplacements(),variable)) {
				modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
				return true;
			}
		}
		return false;
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate) {
		if (modelstate.getNumConstraints() > 0)
			modelstate.setNoConstraintsFlag(false);
		for (Constraint constraint : getModels().get(modelstate.getModel())
				.getListOfConstraints()) {
			if (constraint.isSetMetaId()
					&& modelstate.isDeletedByMetaID(constraint.getMetaId()))
				continue;
			constraint.setMath(inlineFormula(modelstate, constraint.getMath()));
			for (ASTNode constraintNode : constraint.getMath().getListOfNodes()) {

				if (constraintNode.isName()) {

					String nodeName = constraintNode.getName();
					modelstate.getVariableToAffectedConstraintSetMap().put(nodeName,
							new HashSet<ASTNode>());
					modelstate.getVariableToAffectedConstraintSetMap().get(nodeName)
							.add(constraint.getMath());
					modelstate.getVariableToIsInConstraintMap().put(nodeName, true);
				}
			}
		}
	}

	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents(ModelState modelstate) {

		// add event information to hashmaps for easy/fast access
		// this needs to happen after calculating initial propensities
		// so that the getSpeciesToAffectedReactionSetMap() is populated

		long size = getModels().get(modelstate.getModel()).getEventCount();

		for (int i = 0; i < size; i++) {

			Event event = getModels().get(modelstate.getModel()).getEvent(i);
			if (event.isSetId() && modelstate.isDeletedBySID(event.getId()))
				continue;
			else if (event.isSetMetaId()
					&& modelstate.isDeletedByMetaID(event.getMetaId()))
				continue;
			setupSingleEvent(modelstate, event);
		}
	}

	protected void setupInitialAssignments(ModelState modelstate) {
		HashSet<String> affectedVariables = new HashSet<String>();
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();

		// perform all assignment rules
		for (Rule rule : getModels().get(modelstate.getModel()).getListOfRules()) {
			if (rule.isAssignment())
				allAssignmentRules.add((AssignmentRule) rule);
		}

		long maxIterations = modelstate.getNumParameters() + modelstate.getNumSpecies()
				+ modelstate.getNumCompartments();
		long numIterations = 0;
		double newResult = 0;
		boolean changed = true, temp = false;

		while (changed) {
			if (numIterations > maxIterations) {
				System.out.println("Error: circular dependency");
				return;
			}

			changed = false;
			numIterations++;
			for (InitialAssignment initialAssignment : getModels().get(
					modelstate.getModel()).getListOfInitialAssignments()) {
				String variable = initialAssignment.getVariable().replace(
						"_negative_", "-");
				initialAssignment.setMath(inlineFormula(modelstate,
						initialAssignment.getMath()));
				if (getModels().get(modelstate.getModel()).containsSpecies(variable)) {
					temp = calcSpeciesInitAssign(modelstate, variable,
							initialAssignment);
				} else if (getModels().get(modelstate.getModel()).containsCompartment(
						variable)) {
					temp = calcCompInitAssign(modelstate, variable,
							initialAssignment);
				} else if (getModels().get(modelstate.getModel()).containsParameter(
						variable)) {
					temp = calcParamInitAssign(modelstate, variable,
							initialAssignment);
				} else {
					newResult = evaluateExpressionRecursive(modelstate,
							initialAssignment.getMath());
					if (newResult != modelstate.getVariableToValue(getReplacements(),variable)) {
						modelstate.setvariableToValueMap(getReplacements(),variable, newResult);
						temp = true;
					}
				}

				changed |= temp;

				affectedVariables.add(variable);
			}

			changed |= calcAssignmentRules(modelstate, allAssignmentRules);
		}

	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private static void setupLocalParameters(ModelState modelstate,
			KineticLaw kineticLaw, Reaction reaction) {

		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_", "-");

		for (int i = 0; i < kineticLaw.getLocalParameterCount(); i++) {

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);

			if (localParameter.isSetId()
					&& modelstate.isDeletedBySID(localParameter.getId()))
				continue;
			if (localParameter.isSetMetaId()
					&& modelstate.isDeletedByMetaID(localParameter.getMetaId()))
				continue;

			String parameterID = "";

			// the parameters don't get reset after each run, so don't re-do
			// this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)
				parameterID = reactionID + "_" + localParameter.getId();
			else
				parameterID = localParameter.getId();

			String oldParameterID = localParameter.getId();
			modelstate.getVariableToValueMap().put(parameterID,
					localParameter.getValue());

			// alter the local parameter ID so that it goes to the local and not
			// global value
			if (localParameter.getId() != parameterID) {
				localParameter.setId(parameterID);
				SBMLutilities.setMetaId(localParameter, parameterID);
			}
			HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), reaction, oldParameterID,
					parameterID);
		}
	}

	protected void setupNonConstantSpeciesReferences(ModelState modelstate) {

		// loop through all reactions and calculate their propensities
		Reaction reaction;

		for (int i = 0; i < modelstate.getNumReactions(); i++) {
			reaction = getModels().get(modelstate.getModel()).getReaction(i);

			for (SpeciesReference reactant : reaction.getListOfReactants()) {
				if (reactant.isSetId()
						&& modelstate.isDeletedBySID(reactant.getId()))
					continue;
				else if (reactant.isSetMetaId()
						&& modelstate.isDeletedByMetaID(reactant.getMetaId()))
					continue;

				if (reactant.getId().length() > 0) {
					modelstate.getVariableToIsConstantMap().put(reactant.getId(),
							reactant.getConstant());
					if (reactant.getConstant() == false) {
						modelstate.getVariablesToPrint().add(reactant.getId());
						if (modelstate.getVariableToValueMap().containsKey(reactant
								.getId()) == false)
							modelstate.setvariableToValueMap(getReplacements(),reactant.getId(),
									reactant.getStoichiometry());
					}
				}
			}

			for (SpeciesReference product : reaction.getListOfProducts()) {
				if (product.isSetId()
						&& modelstate.isDeletedBySID(product.getId()))
					continue;
				else if (product.isSetMetaId()
						&& modelstate.isDeletedByMetaID(product.getMetaId()))
					continue;
				if (product.getId().length() > 0) {
					modelstate.getVariableToIsConstantMap().put(product.getId(),
							product.getConstant());
					if (product.getConstant() == false) {
						modelstate.getVariablesToPrint().add(product.getId());
						if (modelstate.getVariableToValueMap().containsKey(product
								.getId()) == false)
							modelstate.setvariableToValueMap(getReplacements(),product.getId(),
									product.getStoichiometry());
					}
				}
			}
		}
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate) {

		// add local parameters
		Reaction reaction;
		Parameter parameter;
		long size;

		size = modelstate.getNumReactions();
		for (int i = 0; i < size; i++) {
			reaction = getModels().get(modelstate.getModel()).getReaction(i);
			if (!reaction.isSetKineticLaw())
				continue;
			KineticLaw kineticLaw = reaction.getKineticLaw();

			if (kineticLaw.isSetMetaId()
					&& modelstate.isDeletedByMetaID(kineticLaw.getMetaId()))
				continue;
			setupLocalParameters(modelstate, kineticLaw, reaction);
		}

		// add values to hashmap for easy access to global parameter values
		// NOTE: the IDs for the parameters and species must be unique, so
		// putting them in the
		// same hashmap is okay

		size = getModels().get(modelstate.getModel()).getListOfParameters().size();
		for (int i = 0; i < size; i++) {
			parameter = getModels().get(modelstate.getModel()).getParameter(i);

			if (parameter.isSetId()
					&& modelstate.isDeletedBySID(parameter.getId()))
				continue;
			else if (parameter.isSetMetaId()
					&& modelstate.isDeletedByMetaID(parameter.getMetaId()))
				continue;
			// Check if it is a vector
			// TODO: no longer valid
			/*
			String vSize = biomodel.annotation.AnnotationUtility
					.parseVectorSizeAnnotation(parameter);
			if (vSize != null) {

				int n = (int) getModels().get(modelstate.getModel()).getParameter(vSize)
						.getValue();

				for (int j = 0; j < n; j++) {
					setupSingleParameter(modelstate, parameter, true, false, j,
							0);
				}

				continue;
			}
			// Check if it is a vector
			String[] mSize = biomodel.annotation.AnnotationUtility
					.parseMatrixSizeAnnotation(parameter);
			if (mSize != null && mSize.length == 2) {
				int n = (int) getModels().get(modelstate.getModel())
						.getParameter(mSize[0]).getValue();

				int m = (int) getModels().get(modelstate.getModel())
						.getParameter(mSize[1]).getValue();

				for (int j = 0; j < m; j++) {
					for (int k = 0; k < n; k++) {

						setupSingleParameter(modelstate, parameter, false,
								true, k, j);
					}
				}
				continue;
			}
*/
			setupSingleParameter(modelstate, parameter, false, false, 0, 0);
		}

		// add compartment sizes in
		size = getModels().get(modelstate.getModel()).getCompartmentCount();
		for (int i = 0; i < size; i++) {
			Compartment compartment = getModels().get(modelstate.getModel())
					.getCompartment(i);
			String compartmentID = compartment.getId();

			if (compartment.isSetId()
					&& modelstate.isDeletedBySID(compartment.getId()))
				continue;
			else if (compartment.isSetMetaId()
					&& modelstate.isDeletedByMetaID(compartment.getMetaId()))
				continue;

			modelstate.getCompartmentIDSet().add(compartmentID);
			modelstate.getVariableToValueMap().put(compartmentID,
					compartment.getSize());

			if (Double.isNaN(compartment.getSize()))
				modelstate.setvariableToValueMap(getReplacements(),compartmentID, 1.0);

			modelstate.getVariableToIsConstantMap().put(compartmentID,
					compartment.getConstant());

			if (!compartment.getConstant())
				modelstate.getVariablesToPrint().add(compartmentID);

			if (modelstate.getNumRules() > 0)
				modelstate.getVariableToIsInAssignmentRuleMap().put(compartmentID,
						false);

			if (modelstate.getNumConstraints() > 0)
				modelstate.getVariableToIsInConstraintMap()
						.put(compartmentID, false);
		}
	}

	/**
	 * calculates the initial propensities for each reaction in the getModel()
	 * 
	 * @param numReactions
	 *            the number of reactions in the getModel()
	 */
	protected void setupReactions(ModelState modelstate) {

		// loop through all reactions and calculate their propensities
		Reaction reaction;

		for (int i = 0; i < modelstate.getNumReactions(); i++) {
			reaction = getModels().get(modelstate.getModel()).getReaction(i);
			if (reaction.isSetId()
					&& modelstate.isDeletedBySID(reaction.getId()))
				continue;
			else if (reaction.isSetMetaId()
					&& modelstate.isDeletedByMetaID(reaction.getMetaId()))
				continue;
			if (!reaction.isSetKineticLaw())
				continue;

			String reactionID = reaction.getId();

			String species = reactionID.replace("Degradation_", "");

			if (reactionID.contains("Degradation")
					&& getReplacements().containsKey(species))
				if (modelstate.getIsHierarchical().contains(species)
						&& !modelstate.getID().equals("topmodel"))
					continue;

			ASTNode reactionFormula = reaction.getKineticLaw().getMath();

			setupSingleReaction(modelstate, reactionID, reactionFormula,
					reaction.getReversible(), reaction.getListOfReactants(),
					reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	protected void setupRules(ModelState modelstate) {
		long size = getModels().get(modelstate.getModel()).getListOfRules().size();

		if (size > 0) {
			modelstate.setNoRuleFlag(false);
		}

		for (Rule rule : getModels().get(modelstate.getModel()).getListOfRules()) {
			if (rule.isSetMetaId()
					&& modelstate.isDeletedByMetaID(rule.getMetaId()))
				continue;

			if (rule.isAssignment()) {
				rule.setMath(inlineFormula(modelstate, rule.getMath()));
				AssignmentRule assignmentRule = (AssignmentRule) rule;
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (assignmentRule.getMath().getChildCount() == 0)
					formulaChildren.add(assignmentRule.getMath());
				else
					HierarchicalUtilities.getAllASTNodeChildren(assignmentRule.getMath(),
							formulaChildren);

				for (ASTNode ruleNode : formulaChildren) {

					if (ruleNode.isName()) {

						String nodeName = ruleNode.getName();

						modelstate.getVariableToAffectedAssignmentRuleSetMap()
								.put(nodeName, new HashSet<AssignmentRule>());
						modelstate.getVariableToAffectedAssignmentRuleSetMap()
								.get(nodeName).add(assignmentRule);
						modelstate.getVariableToIsInAssignmentRuleMap().put(
								nodeName, true);
					}
				}
			} else if (rule.isRate()) {
				RateRule rateRule = (RateRule) rule;
				modelstate.getRateRulesList().add(rateRule);
			}
		}
	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(ModelState modelstate, Event event) {

		String eventID = event.getId();

		if (event.isSetPriority())
			modelstate.getEventToPriorityMap().put(eventID,
					inlineFormula(modelstate, event.getPriority().getMath()));

		if (event.isSetDelay()) {

			modelstate.getEventToDelayMap().put(eventID,
					inlineFormula(modelstate, event.getDelay().getMath()));
			modelstate.getEventToHasDelayMap().put(eventID, true);
		} else
			modelstate.getEventToHasDelayMap().put(eventID, false);

		event.getTrigger().setMath(
				inlineFormula(modelstate, event.getTrigger().getMath()));

		modelstate.getEventToTriggerMap().put(eventID, event.getTrigger().getMath());
		modelstate.getEventToTriggerInitiallyTrueMap().put(eventID, event
				.getTrigger().getInitialValue());
		modelstate.getEventToPreviousTriggerValueMap().put(eventID, event
				.getTrigger().getInitialValue());
		modelstate.getEventToTriggerPersistenceMap().put(eventID, event.getTrigger()
				.getPersistent());
		modelstate.getEventToUseValuesFromTriggerTimeMap().put(eventID,
				event.getUseValuesFromTriggerTime());
		modelstate.getEventToAssignmentSetMap().put(eventID, new HashSet<Object>());
		modelstate.getEventToAffectedReactionSetMap().put(eventID,
				new HashSet<String>());

		modelstate.getUntriggeredEventSet().add(eventID);

		for (EventAssignment assignment : event.getListOfEventAssignments()) {

			String variableID = assignment.getVariable();

			assignment.setMath(inlineFormula(modelstate, assignment.getMath()));

			modelstate.getEventToAssignmentSetMap().get(eventID).add(assignment);

			if (modelstate.getVariableToEventSetMap().containsKey(variableID) == false)
				modelstate.getVariableToEventSetMap().put(variableID,
						new HashSet<String>());

			modelstate.getVariableToEventSetMap().get(variableID).add(eventID);

			// if the variable is a species, add the reactions it's in
			// to the event to affected reaction hashmap, which is used
			// for updating propensities after an event fires
			if (modelstate.getSpeciesToAffectedReactionSetMap()
					.containsKey(variableID)) {

				modelstate.getEventToAffectedReactionSetMap().get(eventID).addAll(
						modelstate.getSpeciesToAffectedReactionSetMap()
								.get(variableID));
			}
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
	private void setupSingleReaction(ModelState modelstate, String reactionID,
			ASTNode reactionFormula, boolean reversible,
			ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList,
			ListOf<ModifierSpeciesReference> modifiersList) {
		
		reactionID = reactionID.replace("_negative_", "-");

		long size;
		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;
		boolean notEnoughMoleculesFlag = false;

		if (reversible == true) 
		{
			if (reactionFormula.getType().equals(ASTNode.Type.TIMES)) {

				ASTNode distributedNode = new ASTNode();

				reactionFormula = inlineFormula(modelstate, reactionFormula);
				ASTNode temp = new ASTNode(1);
				if (reactionFormula.getChildCount() == 2
						&& reactionFormula.getChild(1).getType()
								.equals(ASTNode.Type.PLUS))
					distributedNode = ASTNode.sum(ASTNode.times(reactionFormula
							.getLeftChild(), reactionFormula.getRightChild()
							.getLeftChild()), ASTNode.times(new ASTNode(-1),
							reactionFormula.getLeftChild(), reactionFormula
									.getRightChild().getRightChild()));
				else if (reactionFormula.getChildCount() == 2
						&& reactionFormula.getChild(1).getType()
								.equals(ASTNode.Type.MINUS))
					distributedNode = ASTNode.diff(ASTNode.times(
							reactionFormula.getLeftChild(), reactionFormula
									.getRightChild().getLeftChild()), ASTNode
							.times(reactionFormula.getLeftChild(),
									reactionFormula.getRightChild()
											.getRightChild()));

				else if (reactionFormula.getChildCount() >= 2) {
					for (ASTNode node : reactionFormula.getListOfNodes()) {
						if (node.getChildCount() >= 2) {
							if (reactionFormula.getChild(1).getType()
									.equals(ASTNode.Type.MINUS))
								distributedNode = ASTNode.sum(ASTNode.times(
										temp, node.getLeftChild()), ASTNode
										.times(temp, node.getRightChild()
												.getRightChild()));
							else
								distributedNode = ASTNode.diff(ASTNode.times(
										temp, node.getLeftChild()), ASTNode
										.times(temp, node.getRightChild()
												.getRightChild()));

						} else {
							temp = ASTNode.times(temp, node);
						}
					}
				}

				if (distributedNode.isUnknown())
					reactionFormula = temp;
				else
					reactionFormula = distributedNode;
			}
			else if (reactionFormula.getType().equals(ASTNode.Type.MINUS)) {

				ASTNode distributedNode = new ASTNode();
				ASTNode temp = new ASTNode(1);
				reactionFormula = inlineFormula(modelstate, reactionFormula);

				if (reactionFormula.getChildCount() == 1) {
					for (ASTNode node : reactionFormula.getChild(0)
							.getListOfNodes()) {
						if (node.getChildCount() >= 2) {
							if (reactionFormula.getChild(0).getType()
									.equals(ASTNode.Type.MINUS))
								distributedNode = ASTNode.sum(ASTNode.times(
										new ASTNode(-1), temp,
										node.getLeftChild()), ASTNode.times(
										temp, node.getRightChild()));
							else
								distributedNode = ASTNode.diff(ASTNode.times(
										new ASTNode(-1), temp,
										node.getLeftChild()), ASTNode.times(
										temp, node.getRightChild()));

						} else {
							temp = ASTNode.times(temp, node);
						}
					}

					reactionFormula = distributedNode;
				}

			}

			modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID
					+ "_fd", new HashSet<HierarchicalStringDoublePair>());
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID
					+ "_rv", new HashSet<HierarchicalStringDoublePair>());
			modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID
					+ "_fd", new HashSet<HierarchicalStringDoublePair>());
			modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID
					+ "_rv", new HashSet<HierarchicalStringDoublePair>());

			for (SpeciesReference reactant : reactantsList) {

				String reactantID = reactant.getSpecies().replace("_negative_",
						"-");

				if(modelstate.getIsHierarchical().contains(reactantID))
				{
					modelstate.getHierarchicalReactions().add(reactionID+ "_fd");
					modelstate.getHierarchicalReactions().add(reactionID+ "_rv");
				}
				
				double reactantStoichiometry;
				
				if (modelstate.getVariableToValueMap().contains(reactant.getId()))
					reactantStoichiometry = modelstate
							.getVariableToValue(getReplacements(),reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();

				if (productsList.size() == 0) {
					modelstate.getReactionToReactantStoichiometrySetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringDoublePair(reactantID,
									reactantStoichiometry));

				} else {
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringDoublePair(reactantID,
									-reactantStoichiometry));
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
							reactionID + "_rv").add(
							new HierarchicalStringDoublePair(reactantID,
									reactantStoichiometry));
					modelstate.getReactionToReactantStoichiometrySetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringDoublePair(reactantID,
									reactantStoichiometry));
				}
				if (reactant.getConstant() == false
						&& reactant.getId().length() > 0) {

					if (modelstate.getReactionToNonconstantStoichiometriesSetMap()
							.containsKey(reactionID + "_fd") == false)
						modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.put(reactionID + "_fd",
										new HashSet<HierarchicalStringPair>());
					if (modelstate.getReactionToNonconstantStoichiometriesSetMap()
							.containsKey(reactionID + "_rv") == false)
						modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.put(reactionID + "_rv",
										new HashSet<HierarchicalStringPair>());

					modelstate.getReactionToNonconstantStoichiometriesSetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringPair(reactantID + "_fd", reactant
									.getId()));
					modelstate.getReactionToNonconstantStoichiometriesSetMap().get(
							reactionID + "_rv").add(
							new HierarchicalStringPair(reactantID + "_rv", reactant
									.getId()));
					if (modelstate.getVariableToValueMap().containsKey(reactant
							.getId()) == false)
						modelstate.setvariableToValueMap(getReplacements(),reactant.getId(),
								reactantStoichiometry);
				}
				modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(
						reactionID + "_fd");
				if (modelstate.getVariableToValue(getReplacements(),reactantID) < reactantStoichiometry)
					notEnoughMoleculesFlagFd = true;
			}

			for (SpeciesReference product : productsList) {

				String productID = product.getSpecies().replace("_negative_",
						"-");

				if(modelstate.getIsHierarchical().contains(productID))
				{
					modelstate.getHierarchicalReactions().add(reactionID+ "_fd");
					modelstate.getHierarchicalReactions().add(reactionID+ "_rv");
				}
				
				double productStoichiometry;
				if (modelstate.getVariableToValueMap().containsKey(product.getId()))
					productStoichiometry = modelstate
							.getVariableToValue(getReplacements(),product.getId());
				else
					productStoichiometry = product.getStoichiometry();

				if (reactantsList.size() == 0) {
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringDoublePair(productID,
									productStoichiometry));

				} else {
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
							reactionID + "_fd").add(
							new HierarchicalStringDoublePair(productID,
									productStoichiometry));
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
							reactionID + "_rv").add(
							new HierarchicalStringDoublePair(productID,
									-productStoichiometry));
					modelstate.getReactionToReactantStoichiometrySetMap().get(
							reactionID + "_rv").add(
							new HierarchicalStringDoublePair(productID,
									productStoichiometry));
				}
				if (product.getConstant() == false) {

					if (product.getId().length() > 0) {
						modelstate.getVariableToIsConstantMap().put(product.getId(),
								false);
						if (modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.containsKey(reactionID) == false)
							modelstate.getReactionToNonconstantStoichiometriesSetMap()
									.put(reactionID,
											new HashSet<HierarchicalStringPair>());

						modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.get(reactionID).add(
										new HierarchicalStringPair(productID, product
												.getId()));

						if (modelstate.getVariableToValueMap().containsKey(product
								.getId()) == false)
							modelstate.setvariableToValueMap(getReplacements(),product.getId(),
									productStoichiometry);
					}
				}
				modelstate.getSpeciesToAffectedReactionSetMap().get(productID).add(
						reactionID + "_rv");

				if (modelstate.getVariableToValue(getReplacements(),productID) < productStoichiometry)
					notEnoughMoleculesFlagRv = true;
			}

			for (ModifierSpeciesReference modifier : modifiersList) {

				String modifierID = modifier.getSpecies();
				if(modelstate.getIsHierarchical().contains(modifierID))
				{
					modelstate.getHierarchicalReactions().add(reactionID+ "_fd");
					modelstate.getHierarchicalReactions().add(reactionID+ "_rv");
				}
				modifierID = modifierID.replace("_negative_", "-");

				String forwardString = "", reverseString = "";

				try {
					forwardString = ASTNode.formulaToString(reactionFormula
							.getLeftChild());
					reverseString = ASTNode.formulaToString(reactionFormula
							.getRightChild());
				} catch (SBMLException e) {
					e.printStackTrace();
				}
				if (forwardString.contains(modifierID))
					modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID)
							.add(reactionID + "_fd");

				if (reverseString.contains(modifierID))
					modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID)
							.add(reactionID + "_rv");
			}

			double propensity;

			if (productsList.getChildCount() > 0
					&& reactantsList.getChildCount() > 0) {
				modelstate.getReactionToFormulaMap().put(
						reactionID + "_rv",
						inlineFormula(modelstate,
								reactionFormula.getRightChild()));
				modelstate.getReactionToFormulaMap().put(
						reactionID + "_fd",
						inlineFormula(modelstate,
								reactionFormula.getLeftChild()));
				if (notEnoughMoleculesFlagFd == true)
					propensity = 0.0;
				else {
					propensity = evaluateExpressionRecursive(
							modelstate,
							inlineFormula(modelstate,
									reactionFormula.getLeftChild()));

					if (reactionID.contains("_Diffusion_")
							&& isStoichAmpBoolean() == true)
						propensity *= (1.0 / getStoichAmpGridValue());

					if ((propensity < modelstate.getMinPropensity())
							&& (propensity > 0))
						modelstate.setMinPropensity(propensity);

					if (propensity > modelstate.getMaxPropensity())
						modelstate.setMaxPropensity(propensity);

					modelstate.setPropensity(modelstate.getPropensity() + propensity);
				}

				modelstate.getReactionToPropensityMap().put(reactionID + "_fd",
						propensity);

				if (notEnoughMoleculesFlagRv == true)
					propensity = 0.0;
				else {

					propensity = evaluateExpressionRecursive(
							modelstate,
							inlineFormula(modelstate,
									reactionFormula.getRightChild()));

					if (propensity < 0.0)
						propensity = 0.0;

					if (propensity < modelstate.getMinPropensity() && propensity > 0)
						modelstate.setMinPropensity(propensity);

					if (propensity > modelstate.getMaxPropensity())
						modelstate.setMaxPropensity(propensity);

					modelstate.setMaxPropensity(modelstate.getMaxPropensity() + propensity);
				}

				modelstate.getReactionToPropensityMap().put(reactionID + "_rv",
						propensity);
			} else {
				if (reactantsList.getChildCount() > 0) {
					modelstate.getReactionToFormulaMap().put(reactionID + "_fd",
							inlineFormula(modelstate, reactionFormula));
					if (notEnoughMoleculesFlagRv == true)
						propensity = 0.0;
					else {
						propensity = evaluateExpressionRecursive(modelstate,
								inlineFormula(modelstate, reactionFormula));

						if (propensity < 0.0)
							propensity = 0.0;

						if (propensity < modelstate.getMinPropensity()
								&& propensity > 0)
							modelstate.setMinPropensity(propensity);

						if (propensity > modelstate.getMaxPropensity())
							modelstate.setMaxPropensity(propensity);

						modelstate.setPropensity(modelstate.getPropensity() + propensity);
					}

					modelstate.getReactionToPropensityMap().put(reactionID + "_fd",
							propensity);
				} else if (productsList.getChildCount() > 0) {
					modelstate.getReactionToFormulaMap().put(reactionID + "_fd",
							inlineFormula(modelstate, reactionFormula));
					if (notEnoughMoleculesFlagFd == true)
						propensity = 0.0;
					else {
						propensity = evaluateExpressionRecursive(modelstate,
								inlineFormula(modelstate, reactionFormula));

						if (reactionID.contains("_Diffusion_")
								&& isStoichAmpBoolean() == true)
							propensity *= (1.0 / getStoichAmpGridValue());

						if ((propensity < modelstate.getMinPropensity())
								&& (propensity > 0))
							modelstate.setMinPropensity(propensity);

						if (propensity > modelstate.getMaxPropensity())
							modelstate.setMaxPropensity(propensity);

						modelstate.setPropensity(modelstate.getPropensity() + propensity);
						// totalPropensity += propensity;
					}

					modelstate.getReactionToPropensityMap().put(reactionID + "_fd",
							propensity);
				}

			}
		}
		else {
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(reactionID,
					new HashSet<HierarchicalStringDoublePair>());
			modelstate.getReactionToReactantStoichiometrySetMap().put(reactionID,
					new HashSet<HierarchicalStringDoublePair>());

			size = reactantsList.size();
			for (int i = 0; i < size; i++) {

				SpeciesReference reactant = reactantsList.get(i);

				String reactantID = reactant.getSpecies().replace("_negative_",
						"-");

				if(modelstate.getIsHierarchical().contains(reactantID))
				{
					modelstate.getHierarchicalReactions().add(reactionID);
				}
				
				double reactantStoichiometry;

				if (modelstate.getVariableToValueMap().containsKey(reactant.getId()))
					reactantStoichiometry = modelstate
							.getVariableToValue(getReplacements(),reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();

				modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
						reactionID)
						.add(new HierarchicalStringDoublePair(reactantID,
								-reactantStoichiometry));

				modelstate.getSpeciesToAffectedReactionSetMap().get(reactantID).add(
						reactionID);
				modelstate.getReactionToReactantStoichiometrySetMap()
						.get(reactionID).add(
								new HierarchicalStringDoublePair(reactantID,
										reactantStoichiometry));
				
				if (modelstate.getVariableToValue(getReplacements(),reactantID) < reactantStoichiometry)
					notEnoughMoleculesFlag = true;

				if (reactant.getConstant() == false
						&& reactant.getId().length() > 0) {

					if (modelstate.getReactionToNonconstantStoichiometriesSetMap()
							.containsKey(reactionID) == false)
						modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.put(reactionID,
										new HashSet<HierarchicalStringPair>());

					modelstate.getReactionToNonconstantStoichiometriesSetMap().get(
							reactionID).add(
							new HierarchicalStringPair(reactantID, "-"
									+ reactant.getId()));
					if (modelstate.getVariableToValueMap().containsKey(reactant
							.getId()) == false)
						modelstate.setvariableToValueMap(getReplacements(),reactant.getId(),
								reactantStoichiometry);
				}
			}

			size = productsList.size();
			for (int i = 0; i < size; i++) {
				SpeciesReference product = productsList.get(i);

				String productID = product.getSpecies().replace("_negative_",
						"-");
				
				if(modelstate.getIsHierarchical().contains(productID))
				{
					modelstate.getHierarchicalReactions().add(reactionID);
				}
				
				double productStoichiometry;

				if (modelstate.getVariableToValueMap().containsKey(product.getId()))
					productStoichiometry = modelstate
							.getVariableToValue(getReplacements(),product.getId());
				else
					productStoichiometry = product.getStoichiometry();

				modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(
						reactionID).add(
						new HierarchicalStringDoublePair(productID, productStoichiometry));

				if (product.getConstant() == false) {

					if (product.getId().length() > 0) {
						modelstate.getVariableToIsConstantMap().put(product.getId(),
								false);
						if (modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.containsKey(reactionID) == false)
							modelstate.getReactionToNonconstantStoichiometriesSetMap()
									.put(reactionID,
											new HashSet<HierarchicalStringPair>());

						modelstate.getReactionToNonconstantStoichiometriesSetMap()
								.get(reactionID).add(
										new HierarchicalStringPair(productID, product
												.getId()));

						if (modelstate.getVariableToValueMap().containsKey(product
								.getId()) == false)
							modelstate.setvariableToValueMap(getReplacements(),product.getId(),
									productStoichiometry);
					}
				}

				modelstate.getSpeciesToAffectedReactionSetMap().get(productID).add(
						reactionID);

				if (modelstate.getVariableToValue(getReplacements(),productID) < productStoichiometry)
					notEnoughMoleculesFlagRv = true;

			}
			for (ModifierSpeciesReference modifier : modifiersList) {

				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_", "-");

				if(modelstate.getIsHierarchical().contains(modifierID))
				{
					modelstate.getHierarchicalReactions().add(reactionID);
				}
				
				modelstate.getSpeciesToAffectedReactionSetMap().get(modifierID).add(
						reactionID);
			}
			reactionFormula = inlineFormula(modelstate, reactionFormula);
			modelstate.getReactionToFormulaMap().put(reactionID, reactionFormula);

			double propensity;

			if (notEnoughMoleculesFlag == true)
				propensity = 0.0;
			else {
				
				propensity = evaluateExpressionRecursive(modelstate,
						inlineFormula(modelstate, reactionFormula));
				if (propensity < 0.0)
					propensity = 0.0;

				if (propensity < modelstate.getMinPropensity() && propensity > 0)
					modelstate.setMinPropensity(propensity);
				if (propensity > modelstate.getMaxPropensity())
					modelstate.setMaxPropensity(propensity);

				modelstate.setPropensity(modelstate.getPropensity() + propensity);

			}

			modelstate.getReactionToPropensityMap().put(reactionID, propensity);
		}
	}
	
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(ModelState modelstate, Species species,
			String speciesID) 
	{
		
		double initValue = 0;
		if (modelstate.getSpeciesIDSet().contains(speciesID))
			return;

		if (modelstate.getNumConstraints() > 0)
			modelstate.getVariableToIsInConstraintMap().put(speciesID, false);

		if (!modelstate.getIsHierarchical().contains(speciesID) && species.isSetInitialAmount()) {
			initValue = species.getInitialAmount();
			modelstate.setvariableToValueMap(getReplacements(),speciesID,
					species.getInitialAmount());
		}

		else if (species.isSetInitialConcentration()) {
			initValue = species.getInitialConcentration()
					* getModels().get(modelstate.getModel())
							.getCompartment(species.getCompartment()).getSize();
			modelstate.getVariableToValueMap().put(speciesID, initValue);
		} else {
			modelstate.getVariableToValueMap().put(speciesID, initValue);
		}

		if (species.getHasOnlySubstanceUnits() == false) {
			modelstate.getSpeciesToCompartmentNameMap().put(speciesID,
					species.getCompartment());
		}
		if (modelstate.getNumRules() > 0)
			modelstate.getVariableToIsInAssignmentRuleMap().put(speciesID, false);

		modelstate.getSpeciesToAffectedReactionSetMap().put(speciesID,
				new HashSet<String>(20));

		modelstate.getSpeciesToIsBoundaryConditionMap().put(speciesID,
				species.getBoundaryCondition());
		modelstate.getVariableToIsConstantMap()
				.put(speciesID, species.getConstant());
		modelstate.getSpeciesToHasOnlySubstanceUnitsMap().put(speciesID,
				species.getHasOnlySubstanceUnits());
		modelstate.getSpeciesIDSet().add(speciesID);

	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies(ModelState modelstate) throws IOException {

		// add values to hashmap for easy access to species amounts
		Species species;
		long size = getModels().get(modelstate.getModel()).getListOfSpecies().size();
		for (int i = 0; i < size; i++) {

			species = getModels().get(modelstate.getModel()).getSpecies(i);

			if (species.isSetId() && modelstate.isDeletedBySID(species.getId()))
				continue;
			else if (species.isSetMetaId()
					&& modelstate.isDeletedByMetaID(species.getMetaId()))
				continue;

			setupSingleSpecies(modelstate, species, species.getId());
		}

	}

}
