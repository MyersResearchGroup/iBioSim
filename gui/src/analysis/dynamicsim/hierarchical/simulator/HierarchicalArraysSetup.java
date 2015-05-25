package analysis.dynamicsim.hierarchical.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import analysis.dynamicsim.hierarchical.util.ArraysObject;
import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.IndexObject;

public abstract class HierarchicalArraysSetup extends HierarchicalSingleSBaseSetup
{

	private final List<AssignmentRule>		arrayedAssignmentRule;
	private final List<InitialAssignment>	arrayedInitAssignment;

	public HierarchicalArraysSetup(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

		arrayedInitAssignment = new ArrayList<InitialAssignment>();
		arrayedAssignmentRule = new ArrayList<AssignmentRule>();
	}

	protected List<AssignmentRule> getArrayedAssignmentRule()
	{
		return arrayedAssignmentRule;
	}

	protected List<InitialAssignment> getArrayedInitAssignment()
	{
		return arrayedInitAssignment;
	}

	protected String getArrayedID(ModelState modelstate, String id, int[] indices)
	{
		for (int i = indices.length - 1; i >= 0; i--)
		{
			id = id + "[" + indices[i] + "]";
		}
		return id;
	}

	protected String getIndexedSpeciesReference(ModelState modelstate, String reaction, String species, int[] reactionIndices)
	{

		String id = species;

		IndexObject index = modelstate.getIndexObjects().get(reaction + "__" + species);

		if (index == null)
		{
			return species;
		}

		Map<Integer, ASTNode> speciesAttribute = index.getAttributes().get("species");

		if (speciesAttribute == null)
		{
			return species;
		}

		Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();

		for (int i = 0; i < reactionIndices.length; i++)
		{
			dimensionIdMap.put("d" + i, reactionIndices[i]);
		}

		for (int i = 0; i < speciesAttribute.size(); i++)
		{
			id = id
					+ "["
					+ (int) Evaluator.evaluateExpressionRecursive(modelstate, speciesAttribute.get(i), false, getCurrentTime(), null, dimensionIdMap,
							getReplacements()) + "]";
		}

		return id;
	}

	protected void setupArrays(ModelState modelstate, Event sbase)
	{
		String id = sbase.getId();
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

		for (Dimension dimension : plugin.getListOfDimensions())
		{
			modelstate.addArrayedObject(id);
			modelstate.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
		}

		for (Index index : plugin.getListOfIndices())
		{
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(), index.getMath());
		}

		for (EventAssignment eventAssignment : sbase.getListOfEventAssignments())
		{
			plugin = (ArraysSBasePlugin) eventAssignment.getExtension("arrays");

			id = sbase.getId() + "__" + eventAssignment.getVariable();

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
	}

	protected void setupArrays(ModelState modelstate, NamedSBase sbase)
	{
		String id = sbase.getId();
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

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

	protected void setupArrays(ModelState modelstate, Reaction sbase)
	{
		String id = sbase.getId();
		setupSpeciesReferenceArrays(modelstate, sbase);

		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

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

	protected void setupArrays(ModelState modelstate, SBase sbase)
	{
		String id = sbase.getMetaId();

		if (id == null)
		{
			return;
		}

		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

		for (Dimension dimension : plugin.getListOfDimensions())
		{
			modelstate.addArrayedMetaObject(id);
			modelstate.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
		}

		for (Index index : plugin.getListOfIndices())
		{
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(), index.getMath());
		}

	}

	protected void setupArraysValues(ModelState modelstate)
	{
		for (String id : modelstate.getArrayedObjects())
		{
			double value = modelstate.getVariableToValue(getReplacements(), id);
			int[] sizes = new int[modelstate.getDimensionCount(id)];
			int[] indices = new int[sizes.length];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(id))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(), obj.getSize()) - 1;
			}

			if (modelstate.getSpeciesIDSet().contains(id))
			{
				Species species = getModels().get(modelstate.getModel()).getSpecies(id);

				setupArrayValue(modelstate, species, id, value, sizes, indices);
			}
			else if (modelstate.getCompartmentIDSet().contains(id))
			{
				Compartment compartment = getModels().get(modelstate.getModel()).getCompartment(id);

				setupArrayValue(modelstate, compartment, id, value, sizes, indices);
			}
			else if (modelstate.getVariablesToPrint().contains(id))
			{
				Parameter parameter = getModels().get(modelstate.getModel()).getParameter(id);

				setupArrayValue(modelstate, parameter, id, value, sizes, indices);
			}

			indices = null;

		}

	}

	protected void setupSpeciesReferenceArrays(ModelState modelstate, Reaction reaction)
	{
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

	private void setupArrayValue(ModelState modelstate, Compartment compartment, String id, double value, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			String newId = getArrayedID(modelstate, id, indices);
			setupSingleCompartment(modelstate, compartment, newId);

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

	private void setupArrayValue(ModelState modelstate, Parameter parameter, String id, double value, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			String newId = getArrayedID(modelstate, id, indices);
			setupSingleParameter(modelstate, parameter, newId);

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

	private void setupArrayValue(ModelState modelstate, Species species, String id, double value, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			String newId = getArrayedID(modelstate, id, indices);
			setupSingleSpecies(modelstate, species, newId);

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
}
