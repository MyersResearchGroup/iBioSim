package analysis.dynamicsim.hierarchical.simulator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.Setup;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysObject;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysPair;
import analysis.dynamicsim.hierarchical.util.arrays.DimensionObject;
import analysis.dynamicsim.hierarchical.util.arrays.IndexObject;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalSpeciesReference;

public abstract class HierarchicalArrays extends HierarchicalReplacement
{

	private final String	eventAssignmentPrefix	= "ed";
	private final String	regularPrefix			= "d";
	private final String	speciesRefPrefix		= "d";

	protected enum SetupType
	{
		PARAMETER, SPECIES, COMPARTMENT, ASSIGNMENT_RULE, RATE_RULE, EVENT, CONSTRAINT, INITIAL_ASSIGNMENT, REV_REACTION, REACTION, EVENT_ASSIGNMENT, SPECIES_REFERENCE, REACTANT, MODIFIER, PRODUCT;
	}

	public HierarchicalArrays(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType,
			String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction);
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
		DimensionObject dim = null;
		IndexObject obj = null;

		if (plugin == null)
		{
			return;
		}

		if (plugin.getDimensionCount() > 0)
		{
			modelstate.addArrayedObject(id);
			dim = new DimensionObject();
			for (Dimension dimension : plugin.getListOfDimensions())
			{
				dim.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
			}
			numOfArrays++;
		}

		if (plugin.getIndexCount() > 0)
		{
			obj = new IndexObject();
			for (Index index : plugin.getListOfIndices())
			{
				if (!obj.getAttributes().containsKey(index.getReferencedAttribute()))
				{
					obj.getAttributes().put(index.getReferencedAttribute(), new HashMap<Integer, ASTNode>());
				}
				obj.getAttributes().get(index.getReferencedAttribute()).put(index.getArrayDimension(), index.getMath());
			}
		}

		if (dim != null || obj != null)
		{
			modelstate.addArraysPair(id, new ArraysPair(obj, dim));
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
				setupArrays(modelstate, id, REACTANT, reactant);
			}
			for (SpeciesReference product : reaction.getListOfProducts())
			{
				setupArrays(modelstate, id, PRODUCT, product);
			}
			for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
			{
				setupArrays(modelstate, id, MODIFIER, modifier);
			}
		}
	}

	protected void setupArrayValue(ModelState modelstate, Variable variable, SetupType type)
	{
		String id = variable.getId();
		List<ArraysPair> listOfpairs = modelstate.getArrays().get(id);

		if (listOfpairs == null)
		{
			return;
		}

		if (listOfpairs.size() > 1)
		{
			System.out.println("Something has more than one set of dimensions");
		}

		ArraysPair pair = listOfpairs.get(0);
		DimensionObject dim = pair.getDim();
		int size = dim.getDimensionCount();

		if (size <= 0)
		{
			return;
		}

		int flattenedSize = HierarchicalUtilities.flattenedSize(modelstate, variable.getId(), getReplacements());
		modelstate.getArrayVariableToValue().put(variable.getId(), new double[flattenedSize]);

		double value = modelstate.getVariableToValue(getReplacements(), variable.getId());
		int[] sizes = new int[size];
		int[] indices = new int[sizes.length];

		for (ArraysObject obj : dim.getDimensions())
		{
			sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(), obj.getSize()) - 1;
		}

		setupArrayValue(modelstate, variable, variable.getId(), value, sizes, indices, type);
	}

	protected void setupArrayObject(ModelState modelstate, String id, String parent, SBase sbase, int[] parentIndices, SetupType type)
	{

		List<ArraysPair> listOfpairs = modelstate.getArrays().get(id);

		if (listOfpairs == null)
		{
			return;
		}

		ArraysPair pair = listOfpairs.get(0);
		DimensionObject dim = pair.getDim();

		if (dim == null)
		{
			if (parentIndices != null)
			{
				setupArrayObject(modelstate, sbase, id, parent, null, null, parentIndices, type);
			}
			return;
		}

		int size = dim.getDimensionCount();
		int[] sizes = new int[size];
		int[] indices = new int[sizes.length];

		for (ArraysObject obj : dim.getDimensions())
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

	// TODO: Need to be careful when doing species ref id
	private void setupArrays(ModelState modelstate, String reactionId, String type, SimpleSpeciesReference specRef)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) specRef.getExtension("arrays");
		DimensionObject dim = null;
		IndexObject obj = null;
		if (plugin == null)
		{
			return;
		}

		String id = reactionId + "__" + type + "__" + specRef.getSpecies();

		if (plugin.getDimensionCount() > 0)
		{
			modelstate.addArrayedObject(id);
			dim = new DimensionObject();
			for (Dimension dimension : plugin.getListOfDimensions())
			{
				dim.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
			}
			numOfArrays++;
		}

		if (plugin.getIndexCount() > 0)
		{
			obj = new IndexObject();
			for (Index index : plugin.getListOfIndices())
			{
				if (!obj.getAttributes().containsKey("species"))
				{
					obj.getAttributes().put("species", new HashMap<Integer, ASTNode>(5));
				}
				obj.getAttributes().get("species").put(index.getArrayDimension(), index.getMath());
			}
		}

		modelstate.addArraysPair(id, new ArraysPair(obj, dim));

	}

	private void setupArrayObject(ModelState modelstate, SBase sbase, String id, String parent, int[] sizes, int[] indices, int[] parentIndices, SetupType type)
	{
		ASTNode clone;
		List<String> variables;
		if (sizes == null)
		{
			switch (type)
			{
			case EVENT_ASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				clone = eventAssignment.getMath().clone();
				variables = HierarchicalUtilities.getIndexedObject(modelstate, id, eventAssignment.getVariable(), regularPrefix, "variable", parentIndices, getReplacements());

				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, parentIndices);

				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);

				for (String variable : variables)
				{
					Setup.setupEventAssignment(modelstate, variable, parent, clone, eventAssignment, getModels(), getIbiosimFunctionDefinitions());
				}
				break;
			case REACTANT:
				SpeciesReference reactant = (SpeciesReference) sbase;
				List<String> reactantIDs = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, id, reactant.getSpecies(), parentIndices, getCurrentTime(), getReplacements());
				for (String spefRefID : reactantIDs)
				{
					Setup.setupSingleReactant(modelstate, parent, spefRefID, reactant, getReplacements());
				}
				break;
			case PRODUCT:
				SpeciesReference product = (SpeciesReference) sbase;
				List<String> productIDs = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, id, product.getSpecies(), parentIndices, getCurrentTime(), getReplacements());
				for (String spefRefID : productIDs)
				{
					Setup.setupSingleProduct(modelstate, parent, spefRefID, product, getReplacements());
				}
				break;
			case MODIFIER:
				ModifierSpeciesReference modifier = (ModifierSpeciesReference) sbase;
				List<String> modIDs = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, id, modifier.getSpecies(), parentIndices, getCurrentTime(), getReplacements());
				for (String modID : modIDs)
				{
					Setup.setupSingleModifier(modelstate, parent, modID);
				}
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
				variables = HierarchicalUtilities.getIndexedObject(modelstate, id, rateRule.getVariable(), regularPrefix, "variable", indices, getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				for (String variable : variables)
				{
					Setup.setupSingleRateRule(modelstate, variable, clone, getModels(), getIbiosimFunctionDefinitions());
				}
				break;
			case ASSIGNMENT_RULE:
				AssignmentRule assignRule = (AssignmentRule) sbase;
				clone = assignRule.getMath().clone();
				variables = HierarchicalUtilities.getIndexedObject(modelstate, id, assignRule.getVariable(), regularPrefix, "variable", indices, getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				for (String variable : variables)
				{
					Setup.setupSingleAssignmentRule(modelstate, variable, clone, getModels(), getIbiosimFunctionDefinitions());
				}
				break;
			case INITIAL_ASSIGNMENT:
				InitialAssignment init = (InitialAssignment) sbase;
				clone = init.getMath().clone();
				variables = HierarchicalUtilities.getIndexedObject(modelstate, id, init.getVariable(), regularPrefix, "symbol", indices, getReplacements());
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				for (String variable : variables)
				{
					modelstate.getInitAssignment().put(variable, clone);
				}
				break;
			case EVENT:
				Event event = (Event) sbase;
				String newId = HierarchicalUtilities.getArrayedID(modelstate, event.getId(), indices);
				clone = event.getTrigger().getMath().clone();
				HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				Setup.setupSingleEvent(modelstate, newId, clone, event.getUseValuesFromTriggerTime(), event.getTrigger().getInitialValue(), event.getTrigger().getPersistent(), getModels(), getIbiosimFunctionDefinitions());
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
					if (modelstate.getArrays().get(eventAssignmentId) == null)
					{
						Setup.setupEventAssignment(modelstate, eventAssignment.getVariable(), newId, eventAssignment.getMath(), eventAssignment, getModels(), getIbiosimFunctionDefinitions());
					}
					else
					{
						setupArrayObject(modelstate, eventAssignmentId, newId, eventAssignment, indices, SetupType.EVENT_ASSIGNMENT);
					}
				}
				break;
			case EVENT_ASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				clone = eventAssignment.getMath().clone();
				variables = HierarchicalUtilities.getIndexedObject(modelstate, id, eventAssignment.getVariable(), eventAssignmentPrefix, "variable", indices, getReplacements());
				if (parentIndices != null)
				{
					HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, parentIndices);
				}
				HierarchicalUtilities.replaceDimensionIds(clone, eventAssignmentPrefix, indices);
				HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
				for (String variable : variables)
				{
					Setup.setupEventAssignment(modelstate, variable, parent, clone, eventAssignment, getModels(), getIbiosimFunctionDefinitions());
				}
				break;
			case REACTION:
				Reaction reaction = (Reaction) sbase;
				setupArrayReaction(modelstate, id, id, reaction, indices);
				break;
			case REV_REACTION:
				Reaction revReaction = (Reaction) sbase;
				setupArrayReaction(modelstate, id, id + "_fd", revReaction, indices);
				setupArrayReaction(modelstate, id, id + "_rv", revReaction, indices);
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
			modelstate.setVariableToValue(getReplacements(), newId, value);
			switch (type)
			{
			case SPECIES:
				modelstate.getSpeciesToAffectedReactionSetMap().put(newId, new HashSet<String>(20));
				modelstate.getSpeciesIDSet().add(newId);
				modelstate.getSpeciesToCompartmentNameMap().get(id);
				String indexedCompartment;
				List<String> compartments = HierarchicalUtilities.getIndexedObject(modelstate, id, ((Species) variable).getCompartment(), "d", "compartment", indices, getReplacements());

				if (compartments == null || compartments.size() == 0)
				{
					indexedCompartment = modelstate.getSpeciesToCompartmentNameMap().get(id);
				}
				else
				{
					indexedCompartment = compartments.get(0);
				}
				modelstate.getSpeciesToCompartmentNameMap().put(newId, indexedCompartment);
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

	private void setupArrayReaction(ModelState modelstate, String parentId, String id, Reaction revReaction, int[] indices)
	{
		String newId = HierarchicalUtilities.getArrayedID(modelstate, id, indices);
		ASTNode clone = modelstate.getReactionToFormulaMap().get(id).clone();
		HierarchicalUtilities.replaceDimensionIds(clone, regularPrefix, indices);
		HierarchicalUtilities.replaceSelector(modelstate, getReplacements(), clone);
		modelstate.getReactionToFormulaMap().put(newId, clone);

		if (!modelstate.getReactionToReactantStoichiometrySetMap().containsKey(newId))
		{
			modelstate.getReactionToReactantStoichiometrySetMap().put(newId, new HashSet<HierarchicalSpeciesReference>());
		}

		if (!modelstate.getReactionToSpeciesAndStoichiometrySetMap().containsKey(newId))
		{
			modelstate.getReactionToSpeciesAndStoichiometrySetMap().put(newId, new HashSet<HierarchicalSpeciesReference>());
		}

		Set<HierarchicalSpeciesReference> reactants = modelstate.getReactionToReactantStoichiometrySetMap().get(id);

		for (HierarchicalSpeciesReference pair : reactants)
		{
			String species = pair.getString();
			double stoic = pair.getDoub();
			String type = pair.getType();
			String arrayId = parentId + "__" + type + "__" + species;
			List<String> speciesRef = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, arrayId, species, indices, getCurrentTime(), getReplacements());
			for (String specRef : speciesRef)
			{
				modelstate.getReactionToReactantStoichiometrySetMap().get(newId).add(new HierarchicalSpeciesReference(specRef, stoic, type));
			}
		}

		reactants = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(id);

		for (HierarchicalSpeciesReference pair : reactants)
		{
			String species = pair.getString();
			double stoic = pair.getDoub();
			String type = pair.getType();
			String arrayId = parentId + "__" + type + "__" + species;
			List<String> speciesRef = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, arrayId, species, indices, getCurrentTime(), getReplacements());
			for (String specRef : speciesRef)
			{
				modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(newId).add(new HierarchicalSpeciesReference(specRef, stoic, type));

				if (!modelstate.getSpeciesToAffectedReactionSetMap().containsKey(specRef))
				{
					modelstate.getSpeciesToAffectedReactionSetMap().put(specRef, new HashSet<String>());
				}

				modelstate.getSpeciesToAffectedReactionSetMap().get(specRef).add(newId);
			}

		}

		for (ModifierSpeciesReference modifier : revReaction.getListOfModifiers())
		{
			String species = modifier.getSpecies();
			String arrayId = id + "__" + MODIFIER + "__" + species;

			List<String> arrayedIds = HierarchicalUtilities.getIndexedSpeciesReference(modelstate, arrayId, species, indices, getCurrentTime(), getReplacements());

			for (String refId : arrayedIds)
			{
				if (!modelstate.getSpeciesToAffectedReactionSetMap().containsKey(refId))
				{
					modelstate.getSpeciesToAffectedReactionSetMap().put(refId, new HashSet<String>());
				}

				modelstate.getSpeciesToAffectedReactionSetMap().get(refId).add(newId);
			}
		}
		// TODO: non constant stoichiometry

		boolean notEnoughMoleculesFlagFd = HierarchicalUtilities.getNotEnoughEnoughMolecules(modelstate, newId, getReplacements());
		modelstate.getReactionToHasEnoughMolecules().put(newId, notEnoughMoleculesFlagFd);

		double propensity = Evaluator.evaluateExpressionRecursive(modelstate, HierarchicalUtilities.inlineFormula(modelstate, clone, getModels(), getIbiosimFunctionDefinitions()), false, getCurrentTime(), null, null, getReplacements());

		modelstate.setPropensity(getReplacements(), newId, propensity);

	}

}
