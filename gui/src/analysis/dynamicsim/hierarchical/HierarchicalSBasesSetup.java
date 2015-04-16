package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public abstract class HierarchicalSBasesSetup extends HierarchicalArraysSetup
{

	public HierarchicalSBasesSetup(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

	}

	private boolean calcAssignmentRules(ModelState modelstate,
			HashSet<AssignmentRule> affectedAssignmentRuleSet)
	{

		boolean changed = false;
		boolean temp = false;
		double newResult, oldResult;
		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet)
		{

			String variable = assignmentRule.getVariable();

			// update the species count (but only if the species isn't constant)
			// (bound cond is fine)
			if (modelstate.getVariableToIsConstantMap().containsKey(variable)
					&& modelstate.getVariableToIsConstantMap().get(variable) == false
					|| modelstate.getVariableToIsConstantMap().containsKey(variable) == false)
			{

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
				{

					oldResult = modelstate.getVariableToValue(getReplacements(), variable);
					newResult = evaluateExpressionRecursive(modelstate, assignmentRule.getMath(),
							false, getCurrentTime(), null, null)
							* modelstate.getVariableToValue(getReplacements(), modelstate
									.getSpeciesToCompartmentNameMap().get(variable));
					if (oldResult != newResult)
					{
						modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
						temp = true;
					}
				}
				else
				{
					oldResult = modelstate.getVariableToValue(getReplacements(), variable);
					newResult = evaluateExpressionRecursive(modelstate, assignmentRule.getMath(),
							false, getCurrentTime(), null, null);

					if (oldResult != newResult)
					{
						modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
						temp = true;
					}
				}

				changed |= temp;
			}
		}

		return changed;
	}

	private boolean calcCompInitAssign(ModelState modelstate, String variable,
			InitialAssignment initialAssignment)
	{
		double newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath(),
				false, getCurrentTime(), null, null);
		double oldResult = modelstate.getVariableToValue(getReplacements(), variable);
		// double speciesVal = 0;
		if (newResult != oldResult)
		{
			if (oldResult == Double.NaN)
			{
				oldResult = 1.0;
			}

			modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
			if (modelstate.getNumRules() > 0)
			{
				HashSet<AssignmentRule> rules = modelstate
						.getVariableToAffectedAssignmentRuleSetMap().get(variable);

				performAssignmentRules(modelstate, rules);
			}
			// TODO: NEED TO FIX THIS
			/*
			 * for(String species : modelstate.getSpeciesIDSet()) {
			 * 
			 * 
			 * if(modelstate.getSpeciesToCompartmentNameMap().get(species).equals
			 * ( variable)) if
			 * (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey
			 * (species) &&
			 * modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(species) ==
			 * false) { speciesVal =
			 * modelstate.getVariableToValue(getReplacements(),species);
			 * 
			 * if(getModels().get(modelstate.getModel()).getSpecies(species).
			 * isSetInitialConcentration()) speciesVal =
			 * getModels().get(modelstate.getModel()
			 * ).getSpecies(species).getInitialConcentration();
			 * 
			 * newResult = (speciesVal) *
			 * modelstate.getVariableToValue(getReplacements(),modelstate
			 * .getSpeciesToCompartmentNameMap().get(species));
			 * modelstate.setvariableToValueMap(getReplacements(),species,
			 * newResult); } }
			 */
			return true;
		}

		return false;
	}

	private boolean calcParamInitAssign(ModelState modelstate, String variable,
			InitialAssignment initialAssignment)
	{
		double newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath(),
				false, getCurrentTime(), null, null);
		double oldResult = modelstate.getVariableToValue(getReplacements(), variable);

		// if(Double.compare(newResult, oldResult) == 0)
		// return false;

		if (newResult != oldResult)
		{
			modelstate.setvariableToValueMap(getReplacements(), variable, newResult);

			return true;
		}

		return false;
	}

	private boolean calcSpeciesInitAssign(ModelState modelstate, String variable,
			InitialAssignment initialAssignment)
	{
		double newResult;
		if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable)
				&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false)
		{

			newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath(), false,
					getCurrentTime(), null, null)
					* modelstate.getVariableToValue(getReplacements(), modelstate
							.getSpeciesToCompartmentNameMap().get(variable));
			if (newResult != modelstate.getVariableToValue(getReplacements(), variable))
			{
				modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
				return true;
			}

		}
		else
		{
			newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath(), false,
					getCurrentTime(), null, null);

			if (newResult != modelstate.getVariableToValue(getReplacements(), variable))
			{
				modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
				return true;
			}
		}
		return false;
	}

	protected void setupCompartments(ModelState modelstate)
	{

		for (Compartment compartment : getModels().get(modelstate.getModel())
				.getListOfCompartments())
		{
			setupSingleCompartment(modelstate, compartment, compartment.getId());

			setupArrays(modelstate, compartment);
		}
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate)
	{
		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.setNoConstraintsFlag(false);
		}

		for (Constraint constraint : getModels().get(modelstate.getModel()).getListOfConstraints())
		{
			setupSingleConstraint(modelstate, constraint);
		}
	}

	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents(ModelState modelstate)
	{

		// add event information to hashmaps for easy/fast access
		// this needs to happen after calculating initial propensities
		// so that the getSpeciesToAffectedReactionSetMap() is populated

		long size = getModels().get(modelstate.getModel()).getEventCount();

		for (int i = 0; i < size; i++)
		{

			Event event = getModels().get(modelstate.getModel()).getEvent(i);
			if (event.isSetId() && modelstate.isDeletedBySID(event.getId()))
			{
				continue;
			}
			else if (event.isSetMetaId() && modelstate.isDeletedByMetaID(event.getMetaId()))
			{
				continue;
			}

			setupArrays(modelstate, event);

			setupSingleEvent(modelstate, event);
		}
	}

	protected void setupInitialAssignments(ModelState modelstate)
	{
		HashSet<String> affectedVariables = new HashSet<String>();
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();
		HashSet<InitialAssignment> allInitAssignment = new HashSet<InitialAssignment>();

		// perform all assignment rules
		for (Rule rule : getModels().get(modelstate.getModel()).getListOfRules())
		{
			if (rule.isAssignment() && !modelstate.isArrayed(rule.getMetaId()))
			{
				allAssignmentRules.add((AssignmentRule) rule);
			}
		}

		for (AssignmentRule rule : getArrayedAssignmentRule())
		{
			allAssignmentRules.add(rule);
		}

		for (InitialAssignment initAssignment : getModels().get(modelstate.getModel())
				.getListOfInitialAssignments())
		{
			allInitAssignment.add(initAssignment);
			setupArrays(modelstate, initAssignment);
			// setupArraysInitialAssignment(modelstate,
			// initAssignment.getMetaId(), initAssignment);
		}

		allInitAssignment.addAll(getArrayedInitAssignment());

		long maxIterations = modelstate.getVariableToValueMap().size();
		long numIterations = 0;
		double newResult = 0;
		boolean changed = true, temp = false;

		while (changed)
		{
			if (numIterations > maxIterations)
			{
				System.out.println("Error: circular dependency");
				return;
			}

			changed = false;
			numIterations++;
			for (InitialAssignment initialAssignment : allInitAssignment)
			{
				if (initialAssignment.isSetMetaId()
						&& modelstate.isDeletedBySID(initialAssignment.getMetaId()))
				{
					continue;
				}
				String variable = initialAssignment.getVariable().replace("_negative_", "-");
				initialAssignment.setMath(inlineFormula(modelstate, initialAssignment.getMath()));
				if (getModels().get(modelstate.getModel()).containsSpecies(variable))
				{
					temp = calcSpeciesInitAssign(modelstate, variable, initialAssignment);
				}
				else if (getModels().get(modelstate.getModel()).containsCompartment(variable))
				{
					temp = calcCompInitAssign(modelstate, variable, initialAssignment);
				}
				else if (getModels().get(modelstate.getModel()).containsParameter(variable))
				{
					temp = calcParamInitAssign(modelstate, variable, initialAssignment);
				}
				else
				{
					newResult = evaluateExpressionRecursive(modelstate,
							initialAssignment.getMath(), false, getCurrentTime(), null, null);
					if (newResult != modelstate.getVariableToValue(getReplacements(), variable))
					{
						modelstate.setvariableToValueMap(getReplacements(), variable, newResult);
						temp = true;
					}
				}

				changed |= temp;

				affectedVariables.add(variable);
			}

			changed |= calcAssignmentRules(modelstate, allAssignmentRules);
		}

	}

	protected void setupNonConstantSpeciesReferences(ModelState modelstate)
	{

		// loop through all reactions and calculate their propensities
		Reaction reaction;

		for (int i = 0; i < modelstate.getNumReactions(); i++)
		{
			reaction = getModels().get(modelstate.getModel()).getReaction(i);

			for (SpeciesReference reactant : reaction.getListOfReactants())
			{
				if (reactant.isSetId() && modelstate.isDeletedBySID(reactant.getId()))
				{
					continue;
				}
				else if (reactant.isSetMetaId()
						&& modelstate.isDeletedByMetaID(reactant.getMetaId()))
				{
					continue;
				}

				if (reactant.getId().length() > 0)
				{
					modelstate.getVariableToIsConstantMap().put(reactant.getId(),
							reactant.getConstant());
					if (reactant.getConstant() == false)
					{
						modelstate.getVariablesToPrint().add(reactant.getId());
						if (modelstate.getVariableToValueMap().containsKey(reactant.getId()) == false)
						{
							modelstate.setvariableToValueMap(getReplacements(), reactant.getId(),
									reactant.getStoichiometry());
						}
					}
				}
			}

			for (SpeciesReference product : reaction.getListOfProducts())
			{
				if (product.isSetId() && modelstate.isDeletedBySID(product.getId()))
				{
					continue;
				}
				else if (product.isSetMetaId() && modelstate.isDeletedByMetaID(product.getMetaId()))
				{
					continue;
				}
				if (product.getId().length() > 0)
				{
					modelstate.getVariableToIsConstantMap().put(product.getId(),
							product.getConstant());
					if (product.getConstant() == false)
					{
						modelstate.getVariablesToPrint().add(product.getId());
						if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
						{
							modelstate.setvariableToValueMap(getReplacements(), product.getId(),
									product.getStoichiometry());
						}
					}
				}
			}
		}
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate)
	{

		// add local parameters
		Reaction reaction;
		Parameter parameter;
		long size;

		size = modelstate.getNumReactions();
		for (int i = 0; i < size; i++)
		{
			reaction = getModels().get(modelstate.getModel()).getReaction(i);
			if (!reaction.isSetKineticLaw())
			{
				continue;
			}
			KineticLaw kineticLaw = reaction.getKineticLaw();

			if (kineticLaw.isSetMetaId() && modelstate.isDeletedByMetaID(kineticLaw.getMetaId()))
			{
				continue;
			}
			setupLocalParameters(modelstate, kineticLaw, reaction);
		}

		// add values to hashmap for easy access to global parameter values
		// NOTE: the IDs for the parameters and species must be unique, so
		// putting them in the
		// same hashmap is okay

		size = getModels().get(modelstate.getModel()).getListOfParameters().size();
		for (int i = 0; i < size; i++)
		{
			parameter = getModels().get(modelstate.getModel()).getParameter(i);

			if (parameter.isSetId() && modelstate.isDeletedBySID(parameter.getId()))
			{
				continue;
			}
			else if (parameter.isSetMetaId() && modelstate.isDeletedByMetaID(parameter.getMetaId()))
			{
				continue;
			}

			setupSingleParameter(modelstate, parameter, parameter.getId());

			setupArrays(modelstate, parameter);
		}

	}

	/**
	 * calculates the initial propensities for each reaction in the getModel()
	 * 
	 * @param numReactions
	 *            the number of reactions in the getModel()
	 */
	protected void setupReactions(ModelState modelstate)
	{
		Reaction reaction;
		for (int i = 0; i < modelstate.getNumReactions(); i++)
		{
			reaction = getModels().get(modelstate.getModel()).getReaction(i);
			if (reaction.isSetId() && modelstate.isDeletedBySID(reaction.getId()))
			{
				continue;
			}
			else if (reaction.isSetMetaId() && modelstate.isDeletedByMetaID(reaction.getMetaId()))
			{
				continue;
			}
			if (!reaction.isSetKineticLaw())
			{
				continue;
			}

			String reactionID = reaction.getId();
			String species = reactionID;
			if (reactionID.contains("Degradation") && getReplacements().containsKey(species))
			{
				if (modelstate.getIsHierarchical().contains(species)
						&& !modelstate.getID().equals("topmodel"))
				{
					continue;
				}
			}

			ASTNode reactionFormula = reaction.getKineticLaw().getMath();
			setupArrays(modelstate, reaction);
			setupSingleReaction(modelstate, reaction, reactionID, reactionFormula,
					reaction.getReversible(), reaction.getListOfReactants(),
					reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	protected void setupRules(ModelState modelstate)
	{
		long size = getModels().get(modelstate.getModel()).getListOfRules().size();

		if (size > 0)
		{
			modelstate.setNoRuleFlag(false);
		}

		for (Rule rule : getModels().get(modelstate.getModel()).getListOfRules())
		{
			setupSingleRule(modelstate, rule);
		}
	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies(ModelState modelstate) throws IOException
	{
		Species species;
		long size = getModels().get(modelstate.getModel()).getListOfSpecies().size();
		for (int i = 0; i < size; i++)
		{

			species = getModels().get(modelstate.getModel()).getSpecies(i);

			if (species.isSetId() && modelstate.isDeletedBySID(species.getId()))
			{
				continue;
			}
			else if (species.isSetMetaId() && modelstate.isDeletedByMetaID(species.getMetaId()))
			{
				continue;
			}

			setupSingleSpecies(modelstate, species, species.getId());

			setupArrays(modelstate, species);
		}

	}

	// protected void setupArraysSBases(ModelState modelstate)
	// {
	// for (Constraint constraint :
	// getModels().get(modelstate.getModel()).getListOfConstraints())
	// {
	// setupArrays(modelstate, constraint);
	// }
	// for (Rule rule : getModels().get(modelstate.getModel()).getListOfRules())
	// {
	// if (rule.isAssignment())
	// {
	// setupArrays(modelstate, rule);
	// // setupArraysRule(modelstate, rule.getMetaId(),
	// // (AssignmentRule) rule);
	// }
	// }
	// setupInitialAssignments(modelstate);
	// for (Reaction reaction :
	// getModels().get(modelstate.getModel()).getListOfReactions())
	// {
	// setupArrays(modelstate, reaction);
	// setupSpeciesReferenceArrays(modelstate, reaction);
	// // setupArraysReaction(modelstate, reaction.getId(), reaction);
	// }
	//
	// }

}
