package analysis.dynamicsim.hierarchical.simulator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.Setup;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysObject;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;

public abstract class HierarchicalArrays extends HierarchicalReplacement
{

	private final String	eventAssignmentPrefix	= "ed";
	private final String	regularPrefix			= "d";
	private final String	speciesRefPrefix		= "d";

	protected enum SetupType
	{
		PARAMETER, SPECIES, COMPARTMENT, ASSIGNMENT_RULE, RATE_RULE, EVENT, CONSTRAINT, INITIAL_ASSIGNMENT, REACTION, EVENT_ASSIGNMENT, SPECIES_REFERENCE;
	}

	public HierarchicalArrays(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);
	}

	/**
	 * 
	 */
	protected void setupArrayedModels()
	{
		ArraysSBasePlugin arrays;

		Model model = getDocument().getModel();

		CompModelPlugin comp = (CompModelPlugin) model.getPlugin("comp");

		for (Submodel sub : comp.getListOfSubmodels())
		{

			arrays = (ArraysSBasePlugin) sub.getExtension("arrays");

			if (arrays != null)
			{
				addValue(arrays, model, sub.getId());

				ModelState arrayedState = getSubmodels().remove(sub.getId());

				getArrayModels().put(sub.getId(), arrayedState);
			}
		}
	}

	protected void setupArrays(ModelState modelstate, String id, SBase sbase, SetupType type)
	{

		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

		if (plugin.isSetListOfDimensions())
		{

			modelstate.addArrayedObject(id);

			for (Dimension dimension : plugin.getListOfDimensions())
			{
				modelstate.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
			}
		}

		for (Index index : plugin.getListOfIndices())
		{
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(), index.getMath());
		}

		if (type == SetupType.EVENT)
		{
			String eventAssignmentId;
			Event event = (Event) sbase;
			for (EventAssignment assignment : event.getListOfEventAssignments())
			{
				eventAssignmentId = id + "_" + assignment.getVariable();
				setupArrays(modelstate, eventAssignmentId, assignment, SetupType.EVENT_ASSIGNMENT);
			}
		}

		if (type == SetupType.REACTION)
		{
			Reaction reaction = (Reaction) sbase;
			for (SpeciesReference reactant : reaction.getListOfReactants())
			{
				setupArrays(modelstate, reaction.getId(), reactant);
			}
			for (SpeciesReference product : reaction.getListOfProducts())
			{
				setupArrays(modelstate, reaction.getId(), product);
			}
			for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
			{
				setupArrays(modelstate, reaction.getId(), modifier);
			}
		}
	}

	protected void setupArrayValue(ModelState modelstate, Variable variable, SetupType type)
	{
		int size = modelstate.getDimensionCount(variable.getId());

		if (size <= 0)
		{
			return;
		}

		double value = modelstate.getVariableToValue(getReplacements(), variable.getId());
		int[] sizes = new int[size];
		int[] indices = new int[sizes.length];

		for (ArraysObject obj : modelstate.getDimensionObjects().get(variable.getId()))
		{
			sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(), obj.getSize()) - 1;
		}

		setupArrayValue(modelstate, variable, variable.getId(), value, sizes, indices, type);
	}

	protected void setupArrayObject(ModelState modelstate, String id, String parent, SBase sbase, int[] parentIndices, SetupType type)
	{
		int size = modelstate.getDimensionCount(id);

		if (size <= 0)
		{
			if (parentIndices != null)
			{
				setupArrayObject(modelstate, sbase, id, parent, null, null, parentIndices, type);
			}
			return;
		}

		int[] sizes = new int[size];
		int[] indices = new int[sizes.length];

		for (ArraysObject obj : modelstate.getDimensionObjects().get(id))
		{
			sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(), obj.getSize()) - 1;
		}

		setupArrayObject(modelstate, sbase, id, parent, sizes, indices, null, type);

	}

	/**
	 * 
	 * @param arrays
	 * @param model
	 * @param id
	 */
	private void addValue(ArraysSBasePlugin arrays, Model model, String id)
	{
		ModelState state = getSubmodels().get(id);
		int[] sizes = new int[arrays.getDimensionCount()];
		for (Dimension dim : arrays.getListOfDimensions())
		{
			if (model.getParameter(dim.getSize()) == null)
			{
				return;
			}
			sizes[dim.getArrayDimension()] = (int) model.getParameter(dim.getSize()).getValue() - 1;
		}

		setupArrayValue(state, id, sizes, new int[sizes.length]);
	}

	private void setupArrays(ModelState modelstate, String reactionId, SimpleSpeciesReference specRef)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) specRef.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

		String id = specRef.isSetId() ? specRef.getId() : reactionId + "__" + specRef.getSpecies();

		for (Dimension dimension : plugin.getListOfDimensions())
		{
			modelstate.addArrayedObject(id);
			modelstate.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
		}

		for (Index index : plugin.getListOfIndices())
		{
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(), index.getMath());
		}
	}

	private void setupArrayObject(ModelState modelstate, SBase sbase, String id, String parent, int[] sizes, int[] indices, int[] parentIndices,
			SetupType type)
	{
		ASTNode clone;
		String variable;

		if (sizes == null)
		{
			switch (type)
			{
			case EVENT_ASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				clone = eventAssignment.getMath().clone();
				variable = HierarchicalUtilities.getIndexedObject(modelstate, id, eventAssignment.getVariable(), "ed", "variable", parentIndices,
						getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, parentIndices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupEventAssignment(modelstate, variable, parent, clone, eventAssignment, getModels(), getIbiosimFunctionDefinitions());
				break;
			}

			return;
		}

		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			switch (type)
			{
			case CONSTRAINT:
				Constraint constraint = (Constraint) sbase;
				clone = constraint.getMath().clone();
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupSingleConstraint(modelstate, clone, getModels(), getIbiosimFunctionDefinitions());
				break;
			case RATE_RULE:
				RateRule rateRule = (RateRule) sbase;
				clone = rateRule.getMath().clone();
				variable = HierarchicalUtilities.getIndexedObject(modelstate, id, rateRule.getVariable(), regularPrefix, "variable", indices,
						getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupSingleRateRule(modelstate, variable, clone, getModels(), getIbiosimFunctionDefinitions());
				break;
			case ASSIGNMENT_RULE:
				AssignmentRule assignRule = (AssignmentRule) sbase;
				clone = assignRule.getMath().clone();
				variable = HierarchicalUtilities.getIndexedObject(modelstate, id, assignRule.getVariable(), regularPrefix, "variable", indices,
						getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupSingleAssignmentRule(modelstate, variable, clone, getModels(), getIbiosimFunctionDefinitions());
				break;
			case INITIAL_ASSIGNMENT:
				InitialAssignment init = (InitialAssignment) sbase;
				clone = init.getMath().clone();
				variable = HierarchicalUtilities.getIndexedObject(modelstate, id, init.getVariable(), regularPrefix, "symbol", indices,
						getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				modelstate.getInitAssignment().put(variable, clone);
				break;
			case EVENT:
				Event event = (Event) sbase;
				String newId = HierarchicalUtilities.getArrayedID(modelstate, event.getId(), indices);
				clone = event.getTrigger().getMath().clone();
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupSingleEvent(modelstate, newId, clone, event.getUseValuesFromTriggerTime(), event.getTrigger().getInitialValue(), event
						.getTrigger().getPersistent(), getModels(), getIbiosimFunctionDefinitions());
				if (event.isSetPriority())
				{
					clone = event.getPriority().getMath().clone();
					HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
					Setup.setupSinglePriority(modelstate, newId, event.getPriority().getMetaId(), clone, getModels(), getIbiosimFunctionDefinitions());
				}
				if (event.isSetDelay())
				{
					clone = event.getDelay().getMath().clone();
					HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
					Setup.setupSingleDelay(modelstate, newId, event.getDelay().getMetaId(), clone, getModels(), getIbiosimFunctionDefinitions());
				}
				for (EventAssignment eventAssignment : event.getListOfEventAssignments())
				{
					String eventAssignmentId = event.getId() + "_" + eventAssignment.getVariable();
					setupArrayObject(modelstate, eventAssignmentId, newId, eventAssignment, indices, SetupType.EVENT_ASSIGNMENT);
				}
				break;
			case EVENT_ASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				clone = eventAssignment.getMath().clone();
				variable = HierarchicalUtilities.getIndexedObject(modelstate, id, eventAssignment.getVariable(), eventAssignmentPrefix, "variable",
						indices, getReplacements());
				if (parentIndices != null)
				{
					HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, parentIndices);
				}
				HierarchicalUtilities.replaceDimensionIds(clone, eventAssignmentPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupEventAssignment(modelstate, variable, parent, clone, eventAssignment, getModels(), getIbiosimFunctionDefinitions());
				break;
			case REACTION:
				String arrayedId = HierarchicalUtilities.getArrayedID(modelstate, id, indices);

				Set<HierarchicalStringDoublePair> speciesSet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(id);

				if (modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(arrayedId) == null)
				{
					modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(arrayedId, new HashSet<HierarchicalStringDoublePair>());
				}

				for (HierarchicalStringDoublePair speciesAndStoichiometry : speciesSet)
				{
					String speciesID = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, id, speciesAndStoichiometry.string, indices,
							getCurrentTime(), getReplacements());
					modelstate.getSpeciesToAffectedReactionSetMap().get(speciesID).add(arrayedId);

					modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(arrayedId)
							.add(new HierarchicalStringDoublePair(speciesID, speciesAndStoichiometry.doub));
				}
				HierarchicalUtilities.updatePropensity(modelstate, id, getCurrentTime(), indices, getReplacements());
				break;
			}

			indices[0]++;
			for (int i = 0; i < indices.length - 1; i++)
			{
				if (indices[i] > sizes[i])
				{
					indices[i] = 0;
					indices[i + 1]++;
				}
			}
		}
	}

	// TODO: instead of setting up variable, just store the value and check
	// attributes using reference object
	private void setupArrayValue(ModelState modelstate, Variable variable, String id, double value, int[] sizes, int[] indices, SetupType type)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			String newId = HierarchicalUtilities.getArrayedID(modelstate, id, indices);
			modelstate.addVariableToValueMap(newId, value);
			switch (type)
			{
			case SPECIES:
				modelstate.getSpeciesToAffectedReactionSetMap().put(newId, new HashSet<String>(20));
				modelstate.getSpeciesIDSet().add(newId);
				// TODO:species need index for compartment
				break;
			}
			indices[0]++;
			for (int i = 0; i < indices.length - 1; i++)
			{
				if (indices[i] > sizes[i])
				{
					indices[i] = 0;
					indices[i + 1]++;
				}
			}
		}
	}

	private void setupArrayValue(ModelState state, String id, int[] sizes, int[] indices)
	{
		String newId = id;

		for (int i = indices.length - 1; i >= 0; i--)
		{
			newId = newId + "_" + indices[i];
		}
		ModelState newState = state.clone();
		newState.setID(newId);
		getSubmodels().put(newId, newState);

		if (Arrays.equals(sizes, indices))
		{
			return;
		}

		indices[0]++;
		for (int i = 0; i < indices.length - 1; i++)
		{
			if (indices[i] > sizes[i])
			{
				indices[i] = 0;
				indices[i + 1]++;
			}
		}
		setupArrayValue(state, id, sizes, indices);
	}
}
