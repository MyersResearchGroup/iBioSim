package analysis.dynamicsim.hierarchical.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.Setup;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysPair;
import analysis.dynamicsim.hierarchical.util.arrays.IndexObject;

public abstract class HierarchicalSetup extends HierarchicalArrays
{

	public HierarchicalSetup(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType,
			String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction);
	}

	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(int currentRun)
	{
		setCurrentRun(currentRun);
		try
		{
			setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + ".tsd"));
			setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
			getBufferedTSDWriter().write('(');
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void setupCompartments(ModelState modelstate)
	{
		for (Compartment compartment : getModel(modelstate.getModel()).getListOfCompartments())
		{
			if (compartment.isSetId() && modelstate.isDeletedBySID(compartment.getId()))
			{
				continue;
			}
			else if (compartment.isSetMetaId() && modelstate.isDeletedByMetaID(compartment.getMetaId()))
			{
				continue;
			}

			Setup.setupSingleCompartment(modelstate, compartment, compartment.getId(), getReplacements());
			setupArrays(modelstate, compartment.getId(), compartment, SetupType.COMPARTMENT);
			setupArrayValue(modelstate, compartment, SetupType.COMPARTMENT);
		}
	}

	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate)
	{
		int count = 0;

		if (modelstate.getNumConstraints() > 0)
		{
			modelstate.setNoConstraintsFlag(false);
		}

		for (Constraint constraint : getModels().get(modelstate.getModel()).getListOfConstraints())
		{
			if (constraint.isSetMetaId() && modelstate.isDeletedByMetaID(constraint.getMetaId()))
			{
				continue;
			}
			String id = "constraint_" + count++;
			setupArrays(modelstate, id, constraint, SetupType.CONSTRAINT);
			setupArrayObject(modelstate, id, null, constraint, null, SetupType.CONSTRAINT);
			if (!modelstate.isArrayedObject(id))
			{
				Setup.setupSingleConstraint(modelstate, constraint.getMath(), getModels(), getIbiosimFunctionDefinitions());
			}
		}

	}

	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents(ModelState modelstate)
	{
		for (Event event : getModel(modelstate.getModel()).getListOfEvents())
		{
			String id = event.getId();

			if (event.isSetId() && modelstate.isDeletedBySID(id))
			{
				continue;
			}
			else if (event.isSetMetaId() && modelstate.isDeletedByMetaID(event.getMetaId()))
			{
				continue;
			}

			Setup.setupSingleEvent(modelstate, id, event.getTrigger().getMath(), event.getUseValuesFromTriggerTime(), event.getTrigger().getInitialValue(), event.getTrigger().getPersistent(), getModels(), getIbiosimFunctionDefinitions());

			if (event.isSetPriority())
			{
				Setup.setupSinglePriority(modelstate, id, event.getPriority().getMetaId(), event.getPriority().getMath(), getModels(), getIbiosimFunctionDefinitions());
			}
			if (event.isSetDelay())
			{
				Setup.setupSingleDelay(modelstate, id, event.getDelay().getMetaId(), event.getDelay().getMath(), getModels(), getIbiosimFunctionDefinitions());
			}

			setupEventAssignments(modelstate, event, id);

			setupArrays(modelstate, id, event, SetupType.EVENT);

			setupArrayObject(modelstate, id, null, event, null, SetupType.EVENT);

		}
	}

	protected void setupInitialAssignments(ModelState modelstate)
	{

		for (InitialAssignment initAssignment : getModel(modelstate.getModel()).getListOfInitialAssignments())
		{
			if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaID(initAssignment.getMetaId()))
			{
				continue;
			}
			String id = "initial_" + initAssignment.getVariable();
			setupArrays(modelstate, id, initAssignment, SetupType.INITIAL_ASSIGNMENT);
			setupArrayObject(modelstate, id, null, initAssignment, null, SetupType.INITIAL_ASSIGNMENT);
			if (!modelstate.isArrayedObject(id))
			{
				modelstate.getInitAssignment().put(initAssignment.getVariable(), initAssignment.getMath());
			}
		}
		Setup.calculateInitAssignments(modelstate, modelstate.getInitAssignment(), modelstate.getAssignmentRulesList(), getModels(), getIbiosimFunctionDefinitions(), getReplacements(), getCurrentTime());

	}

	protected void setupNonConstantSpeciesReferences(ModelState modelstate)
	{
		for (Reaction reaction : getModel(modelstate.getModel()).getListOfReactions())
		{
			for (SpeciesReference reactant : reaction.getListOfReactants())
			{
				if (reactant.isSetId() && modelstate.isDeletedBySID(reactant.getId()))
				{
					continue;
				}
				else if (reactant.isSetMetaId() && modelstate.isDeletedByMetaID(reactant.getMetaId()))
				{
					continue;
				}

				if (reactant.getId().length() > 0)
				{
					if (reactant.getConstant() == false)
					{
						modelstate.getVariablesToPrint().add(reactant.getId());
					}
					else
					{
						modelstate.addVariableToIsConstant(reactant.getId());
					}
					if (modelstate.getVariableToValueMap().containsKey(reactant.getId()) == false)
					{
						modelstate.setVariableToValue(getReplacements(), reactant.getId(), reactant.getStoichiometry());
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
					if (product.getConstant() == false)
					{
						modelstate.getVariablesToPrint().add(product.getId());
					}
					else
					{
						modelstate.addVariableToIsConstant(product.getId());
					}
					if (modelstate.getVariableToValueMap().containsKey(product.getId()) == false)
					{
						modelstate.setVariableToValue(getReplacements(), product.getId(), product.getStoichiometry());
					}
				}
			}
		}
	}

	protected void setupEventAssignments(ModelState modelstate, Event event, String eventId)
	{
		for (EventAssignment assignment : event.getListOfEventAssignments())
		{
			if (assignment.isSetMetaId() && modelstate.isDeletedByMetaID(assignment.getMetaId()))
			{
				continue;
			}
			String variable = assignment.getVariable();
			String assignmentId = event.getId() + "_" + assignment.getVariable();

			Setup.setupEventAssignment(modelstate, variable, event.getId(), assignment.getMath(), assignment, getModels(), getIbiosimFunctionDefinitions());
			setupArrays(modelstate, assignmentId, assignment, SetupType.EVENT_ASSIGNMENT);
			setupArrayObject(modelstate, assignmentId, event.getId(), assignment, null, SetupType.EVENT_ASSIGNMENT);
			if (!modelstate.isArrayedObject(assignmentId))
			{
				if (modelstate.getArrays().containsKey(assignmentId))
				{
					for (ArraysPair pair : modelstate.getArrays().get(assignmentId))
					{
						IndexObject object = pair.getIndex();
						if (object != null)
						{
							HashMap<Integer, ASTNode> indices = object.getAttributes().get("variable");
							int[] newIndices = new int[indices.size()];
							for (int i = 0; i < newIndices.length; i++)
							{
								newIndices[i] = (int) Evaluator.evaluateExpressionRecursive(modelstate, indices.get(i), false, getCurrentTime(), null, null, getReplacements());
							}
							variable = HierarchicalUtilities.getArrayedID(modelstate, variable, newIndices);
						}
					}
				}
				Setup.setupEventAssignment(modelstate, variable, event.getId(), assignment.getMath(), assignment, getModels(), getIbiosimFunctionDefinitions());
			}

		}
	}

	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate)
	{
		for (Reaction reaction : getModel(modelstate.getModel()).getListOfReactions())
		{
			if (!reaction.isSetKineticLaw())
			{
				continue;
			}
			KineticLaw kineticLaw = reaction.getKineticLaw();

			if (kineticLaw.isSetMetaId() && modelstate.isDeletedByMetaID(kineticLaw.getMetaId()))
			{
				continue;
			}
			Setup.setupLocalParameters(modelstate, kineticLaw, reaction);
		}

		for (Parameter parameter : getModel(modelstate.getModel()).getListOfParameters())
		{
			if (parameter.isSetId() && modelstate.isDeletedBySID(parameter.getId()))
			{
				continue;
			}
			else if (parameter.isSetMetaId() && modelstate.isDeletedByMetaID(parameter.getMetaId()))
			{
				continue;
			}

			Setup.setupSingleParameter(modelstate, parameter, parameter.getId());
			setupArrays(modelstate, parameter.getId(), parameter, SetupType.PARAMETER);
			setupArrayValue(modelstate, parameter, SetupType.PARAMETER);
		}
	}

	protected void setupPropensities(ModelState modelstate)
	{
		modelstate.resetPropensity();
		for (String reaction : modelstate.getSetOfReactions())
		{
			boolean notEnoughMoleculesFlagFd = HierarchicalUtilities.getNotEnoughEnoughMolecules(modelstate, reaction, getReplacements());
			Setup.setupSingleReactionPropensity(modelstate, reaction, modelstate.getReactionToFormulaMap().get(reaction), notEnoughMoleculesFlagFd, getModels(), getIbiosimFunctionDefinitions(), getReplacements(), getCurrentTime());
		}

	}

	protected void setupReactions(ModelState modelstate)
	{
		Reaction reaction;
		String reactionID;
		for (int i = 0; i < modelstate.getNumReactions(); i++)
		{
			reaction = getModels().get(modelstate.getModel()).getReaction(i);
			reactionID = reaction.getId();
			if (reaction.isSetId() && modelstate.isDeletedBySID(reactionID))
			{
				if (modelstate.isHierarchical(reactionID))
				{
					modelstate.getReactionToHasEnoughMolecules().put(reactionID, false);
				}
				continue;
			}
			else if (reaction.isSetMetaId() && modelstate.isDeletedByMetaID(reactionID))
			{
				continue;
			}

			if (!reaction.isSetKineticLaw())
			{
				if (modelstate.isHierarchical(reactionID))
				{
					modelstate.getReactionToHasEnoughMolecules().put(reactionID, false);
				}
				continue;
			}

			String species = reactionID;
			if (reactionID.contains("Degradation") && getReplacements().containsKey(species))
			{
				if (modelstate.getIsHierarchical().contains(species) && !modelstate.getID().equals("topmodel"))
				{
					continue;
				}
			}

			if (reaction.isReversible())
			{
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					Setup.setupSingleRevReactant(modelstate, reactionID, reactant.getSpecies(), reactant, reaction.getListOfProducts().size(), getReplacements());
				}

				for (SpeciesReference product : reaction.getListOfProducts())
				{
					Setup.setupSingleRevProduct(modelstate, reactionID, product.getSpecies(), product, reaction.getListOfReactants().size(), getReplacements());
				}

				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Setup.setupSingleModifier(modelstate, reactionID + "_fd", modifier.getSpecies());
					Setup.setupSingleModifier(modelstate, reactionID + "_rv", modifier.getSpecies());
				}
			}
			else
			{
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					Setup.setupSingleReactant(modelstate, reactionID, reactant.getSpecies(), reactant, getReplacements());
				}

				for (SpeciesReference product : reaction.getListOfProducts())
				{
					Setup.setupSingleProduct(modelstate, reactionID, product.getSpecies(), product, getReplacements());
				}

				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Setup.setupSingleModifier(modelstate, reactionID, modifier.getSpecies());
				}
			}
			ASTNode reactionFormula = reaction.getKineticLaw().getMath();

			setupArrays(modelstate, reactionID, reaction, SetupType.REACTION);

			Setup.setupSingleReaction(modelstate, reaction, reactionID, reactionFormula, reaction.getReversible(), getModels(), getIbiosimFunctionDefinitions(), getReplacements(), getCurrentTime());

			if (reaction.isReversible())
			{
				if (modelstate.isArrayedObject(reactionID))
				{
					modelstate.addArrayedObject(reactionID + "_fd");
					modelstate.addArrayedObject(reactionID + "_rv");
				}
				setupArrayObject(modelstate, reactionID, null, reaction, null, SetupType.REV_REACTION);
			}
			else
			{
				setupArrayObject(modelstate, reactionID, null, reaction, null, SetupType.REACTION);
			}

		}
	}

	protected void setupRules(ModelState modelstate)
	{
		long size = getModels().get(modelstate.getModel()).getListOfRules().size();

		if (size > 0)
		{
			modelstate.setNoRuleFlag(false);
		}

		for (Rule rule : getModel(modelstate.getModel()).getListOfRules())
		{
			if (rule.isSetMetaId() && modelstate.isDeletedByMetaID(rule.getMetaId()))
			{
				continue;
			}

			if (rule.isAssignment())
			{
				AssignmentRule assignRule = (AssignmentRule) rule;
				String id = "assignment_" + assignRule.getVariable();
				setupArrays(modelstate, id, assignRule, SetupType.ASSIGNMENT_RULE);
				setupArrayObject(modelstate, id, null, assignRule, null, SetupType.ASSIGNMENT_RULE);
				if (!modelstate.isArrayedObject(id))
				{
					Setup.setupSingleAssignmentRule(modelstate, assignRule.getVariable(), assignRule.getMath(), getModels(), getIbiosimFunctionDefinitions());
				}
			}
			else if (rule.isRate())
			{
				RateRule rateRule = (RateRule) rule;
				String id = "rate_" + rateRule.getVariable();
				setupArrays(modelstate, id, rateRule, SetupType.RATE_RULE);
				setupArrayObject(modelstate, id, null, rateRule, null, SetupType.RATE_RULE);
				if (!modelstate.isArrayedObject(id))
				{
					Setup.setupSingleRateRule(modelstate, rateRule.getVariable(), rateRule.getMath(), getModels(), getIbiosimFunctionDefinitions());
				}
			}
		}
	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies(ModelState modelstate) throws IOException
	{
		for (Species species : getModel(modelstate.getModel()).getListOfSpecies())
		{
			if (species.isSetId() && modelstate.isDeletedBySID(species.getId()))
			{
				continue;
			}
			else if (species.isSetMetaId() && modelstate.isDeletedByMetaID(species.getMetaId()))
			{
				continue;
			}
			Setup.setupSingleSpecies(modelstate, species, species.getId(), getModels(), getReplacements());
			setupArrays(modelstate, species.getId(), species, SetupType.SPECIES);
			setupArrayValue(modelstate, species, SetupType.SPECIES);
		}
	}
}
