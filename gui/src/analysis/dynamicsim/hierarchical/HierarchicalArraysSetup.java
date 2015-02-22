package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import analysis.dynamicsim.hierarchical.util.IndexObject;

public abstract class HierarchicalArraysSetup extends HierarchicalSingleSBaseSetup
{

	private final List<InitialAssignment>	arrayedInitAssignment;
	private final List<AssignmentRule>		arrayedAssignmentRule;

	public HierarchicalArraysSetup(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

		arrayedInitAssignment = new ArrayList<InitialAssignment>();
		arrayedAssignmentRule = new ArrayList<AssignmentRule>();
	}

	public List<InitialAssignment> getArrayedInitAssignment()
	{
		return arrayedInitAssignment;
	}

	public List<AssignmentRule> getArrayedAssignmentRule()
	{
		return arrayedAssignmentRule;
	}

	protected String getArrayedID(ModelState modelstate, String id, int[] indices)
	{
		ASTNode vector = modelstate.getValues().get(id);

		if (vector == null)
		{
			for (int i = indices.length - 1; i >= 0; i--)
			{
				id = id + "_" + indices[i];
			}
			return id;
		}

		for (int index : indices)
		{
			vector = vector.getChild(index);
			if (vector == null)
			{
				return null;
			}
		}

		return vector.isName() ? vector.getName() : null;
	}

	private String getIndexedObject(ModelState modelstate, String id, String var, String attribute,
			int[] indices)
	{
		int index;
		ASTNode node;
		ASTNode vector = modelstate.getValues().get(var);

		if (vector == null)
		{
			return null;
		}

		IndexObject obj = modelstate.getIndexObjects().get(id);

		HashMap<Integer, ASTNode> dimToMath = obj.getAttributes().get(attribute);

		if (dimToMath == null)
		{
			return null;
		}

		for (int i = dimToMath.size() - 1; i >= 0; i--)
		{
			node = dimToMath.get(i).clone();

			if (node == null)
			{
				return null;
			}

			replaceDimensionID(node, indices);

			index = (int) evaluateExpressionRecursive(modelstate, node, false, getCurrentTime(),
					null, null);

			vector = index < vector.getChildCount() ? vector.getChild(index) : null;

			if (vector == null)
			{
				return null;
			}
		}

		return vector.isName() ? vector.getName() : null;
	}

	private void replaceArgument(ASTNode node, String var, int value)
	{
		if (node.isName() && node.getName().equals(var))
		{
			node.setValue(value);
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			replaceArgument(node.getChild(i), var, value);
		}
	}

	// private void replaceDimensionID(ASTNode node, int[] parentIndices, int[]
	// indices)
	// {
	// String dimId;
	// for (int i = 0; i < indices.length; i++)
	// {
	// dimId = "d" + i;
	// replaceArgument(node, dimId, parentIndices[i]);
	// }
	// for (int i = 0; i < indices.length; i++)
	// {
	// dimId = "rd" + i;
	// replaceArgument(node, dimId, indices[i]);
	// }
	// }

	private void replaceDimensionID(ASTNode node, int[] indices)
	{
		String dimId;
		for (int i = 0; i < indices.length; i++)
		{
			dimId = "d" + i;
			replaceArgument(node, dimId, indices[i]);
		}
	}

	private void replaceSelector(ModelState modelstate, ASTNode node)
	{
		if (node == null)
		{
			return;
		}
		if (node.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			String nodeName = node.getChild(0).getName();
			ASTNode vector = modelstate.getValues().get(nodeName);
			for (int i = 1; i < node.getChildCount(); i++)
			{
				int index = (int) evaluateExpressionRecursive(modelstate, node.getChild(i), false,
						getCurrentTime(), null, null);
				vector = vector.getChild(index);
			}
			if (vector.isName())
			{
				node.setType(ASTNode.Type.NAME);
				node.setName(vector.getName());
			}
			return;
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			replaceSelector(modelstate, node.getChild(i));
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

	private void setupArrays(ModelState modelstate, String reactionId,
			SimpleSpeciesReference specRef)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) specRef.getExtension("arrays");

		if (plugin == null)
		{
			return;
		}

		String id = specRef.isSetId() ? specRef.getId() : reactionId + " " + specRef.getSpecies();

		for (Dimension dimension : plugin.getListOfDimensions())
		{
			modelstate.addArrayedObject(id);
			modelstate.addDimension(id, dimension.getSize(), dimension.getArrayDimension());
		}

		for (Index index : plugin.getListOfIndices())
		{
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(),
					index.getMath());
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
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(),
					index.getMath());
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
			modelstate.addIndex(id, index.getReferencedAttribute(), index.getArrayDimension(),
					index.getMath());
		}

	}

	protected void setupArraysValues(ModelState modelstate)
	{
		for (String id : modelstate.getArrayedObjects())
		{
			double value = modelstate.getVariableToValue(getReplacements(), id);
			int[] sizes = new int[modelstate.getDimensionCount(id)];

			for (ArraysObject obj : modelstate.getDimensionObjects().get(id))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}

			if (modelstate.getSpeciesIDSet().contains(id))
			{
				Species species = getModels().get(modelstate.getModel()).getSpecies(id);
				setupArrayValue(modelstate, species, id, value, sizes, new int[sizes.length]);
			}
			else if (modelstate.getCompartmentIDSet().remove(id))
			{
				Compartment compartment = getModels().get(modelstate.getModel()).getCompartment(id);

				setupArrayValue(modelstate, compartment, id, value, sizes, new int[sizes.length]);
			}
			else if (modelstate.getVariablesToPrint().remove(id))
			{
				Parameter parameter = getModels().get(modelstate.getModel()).getParameter(id);

				setupArrayValue(modelstate, parameter, id, value, sizes, new int[sizes.length]);
			}

		}

	}

	private void setupArrayValue(ModelState modelstate, Compartment compartment, String id,
			double value, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			Compartment clone = compartment.clone();
			String newId = modelstate.addValue(id, value, indices);
			clone.setId(newId);
			setupSingleCompartment(modelstate, clone);

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

	private void setupArrayValue(ModelState modelstate, Species species, String id, double value,
			int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			Species clone = species.clone();
			String newId = modelstate.addValue(id, value, indices);
			clone.setId(newId);
			setupSingleSpecies(modelstate, clone, newId);

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

	private void setupArrayValue(ModelState modelstate, Parameter parameter, String id,
			double value, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{
			Parameter clone = parameter.clone();
			String newId = modelstate.addValue(id, value, indices);
			clone.setId(newId);
			setupSingleParameter(modelstate, clone, 0, 0);

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

	protected void setupArraysConstraint(ModelState modelstate, String metaID, Constraint constraint)
	{
		if (modelstate.isArrayed(metaID))
		{
			int[] sizes = new int[modelstate.getDimensionCount(metaID)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(metaID))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysConstraint(modelstate, constraint, metaID, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysConstraint(ModelState modelstate, Constraint constraint, String metaID,
			int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, metaID, indices);

			if (arrayedId != null)
			{
				setupArraysConstraint(modelstate, constraint, arrayedId, indices);
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

	private void setupArraysConstraint(ModelState modelstate, Constraint constraint, String metaId,
			int[] indices)
	{
		Constraint clone = constraint.clone();
		clone.setMetaId(metaId);
		replaceDimensionID(clone.getMath(), indices);
		replaceSelector(modelstate, clone.getMath());
		setupSingleConstraint(modelstate, constraint);
	}

	protected void setupArraysInitialAssignment(ModelState modelstate, String metaID,
			InitialAssignment rule)
	{
		if (modelstate.isArrayed(metaID))
		{
			int[] sizes = new int[modelstate.getDimensionCount(metaID)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(metaID))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysInitialAssignment(modelstate, rule, metaID, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysInitialAssignment(ModelState modelstate, InitialAssignment rule,
			String metaID, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, metaID, indices);

			if (arrayedId != null)
			{
				setupArraysInitialAssignment(modelstate, rule, arrayedId, indices);
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

	private void setupArraysInitialAssignment(ModelState modelstate, InitialAssignment rule,
			String metaId, int[] indices)
	{
		InitialAssignment clone = rule.clone();
		clone.setMetaId(metaId);
		replaceDimensionID(clone.getMath(), indices);
		replaceSelector(modelstate, clone.getMath());
		clone.setVariable(getIndexedObject(modelstate, rule.getMetaId(), rule.getVariable(),
				"symbol", indices));
		arrayedInitAssignment.add(clone);
	}

	protected void setupArraysEvent(ModelState modelstate, String metaID, Event event)
	{
		if (modelstate.isArrayed(metaID))
		{
			int[] sizes = new int[modelstate.getDimensionCount(metaID)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(metaID))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysEvent(modelstate, event, metaID, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysEvent(ModelState modelstate, Event event, String metaID, int[] sizes,
			int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, metaID, indices);

			if (arrayedId != null)
			{
				setupArraysEvent(modelstate, event, arrayedId, indices);
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

	private void setupArraysEvent(ModelState modelstate, Event event, String metaId, int[] indices)
	{
	}

	protected void setupArraysEventAssignment(ModelState modelstate, String metaID,
			EventAssignment event)
	{
		if (modelstate.isArrayed(metaID))
		{
			int[] sizes = new int[modelstate.getDimensionCount(metaID)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(metaID))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysEventAssignment(modelstate, event, metaID, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysEventAssignment(ModelState modelstate, EventAssignment event,
			String metaID, int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, metaID, indices);

			if (arrayedId != null)
			{
				setupArraysEventAssignment(modelstate, event, arrayedId, indices);
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

	private void setupArraysEventAssignment(ModelState modelstate, EventAssignment event,
			String metaId, int[] indices)
	{
	}

	protected void setupArraysReaction(ModelState modelstate, String id, Reaction reaction)
	{
		if (modelstate.isArrayed(id))
		{
			int[] sizes = new int[modelstate.getDimensionCount(id)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(id))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysReaction(modelstate, reaction, id, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysReaction(ModelState modelstate, Reaction reaction, String id,
			int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, id, indices);

			if (arrayedId != null)
			{
				setupArraysReaction(modelstate, reaction, arrayedId, indices);
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

	private void setupArraysReaction(ModelState modelstate, Reaction reaction, String id,
			int[] indices)
	{
		Reaction clone = reaction.clone();
		clone.setId(id);
		replaceDimensionID(clone.getKineticLaw().getMath(), indices);
		replaceSelector(modelstate, clone.getKineticLaw().getMath());

		for (SpeciesReference speciesReference : clone.getListOfReactants())
		{
			setupArraysSpeciesReference(modelstate, reaction.getId(), speciesReference, indices);
		}

		for (SpeciesReference speciesReference : clone.getListOfProducts())
		{
			setupArraysSpeciesReference(modelstate, reaction.getId(), speciesReference, indices);
		}

		setupSingleReaction(modelstate, clone, clone.getId(), clone.getKineticLaw().getMath(),
				clone.getReversible(), clone.getListOfReactants(), clone.getListOfProducts(),
				clone.getListOfModifiers());
	}

	protected void setupArraysRule(ModelState modelstate, String metaID, AssignmentRule rule)
	{
		if (modelstate.isArrayed(metaID))
		{
			int[] sizes = new int[modelstate.getDimensionCount(metaID)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(metaID))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysRule(modelstate, rule, metaID, sizes, new int[sizes.length]);
		}
	}

	private void setupArraysRule(ModelState modelstate, AssignmentRule rule, String metaID,
			int[] sizes, int[] indices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, metaID, indices);

			if (arrayedId != null)
			{
				setupArrayedRule(modelstate, rule, arrayedId, indices);
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

	private void setupArrayedRule(ModelState modelstate, AssignmentRule rule, String metaId,
			int[] indices)
	{
		AssignmentRule clone = rule.clone();
		ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();
		clone.setMetaId(metaId);
		replaceDimensionID(clone.getMath(), indices);
		replaceSelector(modelstate, clone.getMath());
		clone.setVariable(getIndexedObject(modelstate, rule.getMetaId(), rule.getVariable(),
				"variable", indices));
		arrayedAssignmentRule.add(clone);
		for (ASTNode ruleNode : formulaChildren)
		{
			if (ruleNode.isName())
			{
				String nodeName = ruleNode.getName();

				if (!modelstate.getVariableToAffectedAssignmentRuleSetMap().containsKey(nodeName))
				{
					modelstate.getVariableToAffectedAssignmentRuleSetMap().put(nodeName,
							new HashSet<AssignmentRule>());
				}
				modelstate.getVariableToAffectedAssignmentRuleSetMap().get(nodeName).add(clone);
				modelstate.getVariableToIsInAssignmentRuleMap().put(nodeName, true);
			}
		}
	}

	private void setupArraysSpeciesReference(ModelState modelstate, String reactionId,
			SpeciesReference specRef, int[] parentIndices)
	{
		String id = specRef.isSetId() ? specRef.getId() : reactionId + " " + specRef.getSpecies();

		if (modelstate.isArrayed(id))
		{
			int[] sizes = new int[modelstate.getDimensionCount(id)];
			for (ArraysObject obj : modelstate.getDimensionObjects().get(id))
			{
				sizes[obj.getArrayDim()] = (int) modelstate.getVariableToValue(getReplacements(),
						obj.getSize()) - 1;
			}
			setupArraysSpeciesReference(modelstate, specRef, id, sizes, new int[sizes.length],
					parentIndices);
		}
		else
		{
			String reference = getIndexedObject(modelstate, id, specRef.getSpecies(), "species",
					parentIndices);
			if (reference != null)
			{
				specRef.setSpecies(reference);
			}
		}
	}

	private void setupArraysSpeciesReference(ModelState modelstate, SpeciesReference reaction,
			String id, int[] sizes, int[] indices, int[] parentIndices)
	{
		while (sizes[sizes.length - 1] >= indices[indices.length - 1])
		{

			String arrayedId = getArrayedID(modelstate, id, indices);

			if (arrayedId != null)
			{
				setupArraysSpeciesReference(modelstate, reaction, arrayedId, indices, parentIndices);
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

	private void setupArraysSpeciesReference(ModelState modelstate, SpeciesReference specRef,
			String id, int[] indices, int[] parentIndices)
	{
		// TODO
	}
}
