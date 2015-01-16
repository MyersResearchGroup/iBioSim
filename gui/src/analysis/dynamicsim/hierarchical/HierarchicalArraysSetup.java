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
import org.sbml.jsbml.InitialAssignment;

import analysis.dynamicsim.hierarchical.util.ArraysObject;
import analysis.dynamicsim.hierarchical.util.IndexObject;

public abstract class HierarchicalArraysSetup extends HierarchicalReplacemenHandler
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
			node = vector;
			return;
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			replaceSelector(modelstate, node.getChild(i));
		}
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
		if (sizes[sizes.length - 1] < indices[indices.length - 1])
		{
			return;
		}

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
		setupArraysInitialAssignment(modelstate, rule, metaID, sizes, indices);
	}

	private void setupArraysInitialAssignment(ModelState modelstate, InitialAssignment rule,
			String metaId, int[] indices)
	{
		InitialAssignment clone = rule.clone();
		clone.setMetaId(metaId);
		replaceDimensionID(clone.getMath(), indices);
		replaceSelector(modelstate, clone.getMath());
		clone.setVariable(getIndexedObject(modelstate, rule.getMetaId(), rule.getVariable(),
				"variable", indices));
		arrayedInitAssignment.add(clone);
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
		if (sizes[sizes.length - 1] < indices[indices.length - 1])
		{
			return;
		}

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
		setupArraysRule(modelstate, rule, metaID, sizes, indices);
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
}
