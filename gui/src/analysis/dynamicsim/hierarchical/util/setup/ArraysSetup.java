package analysis.dynamicsim.hierarchical.util.setup;

import java.util.List;

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.util.math.ArrayDimensionNode;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class ArraysSetup implements Setup
{

	public static boolean checkArray(SBase sbase)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

		if (plugin == null)
		{
			return false;
		}

		return plugin.getNumDimensions() > 0;
	}

	public static void setupArray(ModelState modelstate, SBase sbase, VariableNode node, VariableNode parent)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

		if (plugin == null)
		{
			return;
		}

		if (plugin.getNumDimensions() > 0)
		{
			for (int i = 0; i < plugin.getDimensionCount(); i++)
			{
				Dimension dim = plugin.getDimensionByArrayDimension(i);
				ArrayDimensionNode dimNode = new ArrayDimensionNode(dim.getId());
				dimNode.setSizeId(dim.getSize());
				node.addDimension(dim.getId(), dimNode);
			}
			modelstate.addArray(node);
		}

		if (plugin.getNumIndices() > 0)
		{

		}
	}

	public static void setupArrays(ModelState modelstate, Symbol symbol, VariableNode variableNode)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) symbol.getExtension(ArraysConstants.shortLabel);

		if (plugin == null)
		{
			return;
		}

		setupArrays(plugin, plugin.getDimensionCount() - 1, symbol.getId(), "", modelstate, variableNode, variableNode);
	}

	private static void setupArrays(ArraysSBasePlugin plugin, int arrayDim, String id, String suffix, ModelState modelstate, VariableNode variableNode, HierarchicalNode parent)
	{

		Dimension dimension = plugin.getDimensionByArrayDimension(arrayDim);
		String sizeId = dimension.getSize();

		int size = (int) modelstate.getNode(sizeId).getValue();
		List<HierarchicalNode> children = parent.createChildren();

		for (int i = 0; i < size; i++)
		{
			String newSuffix = suffix + "[" + i + "]";
			if (arrayDim == 0)
			{
				String newId = id + newSuffix;
				if (variableNode.isSpecies())
				{
					SpeciesNode child = new SpeciesNode((SpeciesNode) variableNode);
					child.setName(newId);
					children.add(child);
					modelstate.addVariable(child);
				}
				else if (variableNode.isReaction())
				{
					ReactionNode child = new ReactionNode((ReactionNode) variableNode);
					child.setName(newId);
					children.add(child);
					modelstate.addVariable(child);
				}
				else
				{
					VariableNode child = new VariableNode(variableNode);
					child.setName(newId);
					children.add(child);
					modelstate.addVariable(child);
				}
			}
			else
			{
				HierarchicalNode child = new HierarchicalNode(Type.VECTOR);
				children.add(child);
				setupArrays(plugin, arrayDim - 1, id, newSuffix, modelstate, variableNode, child);
				// setupArrays(plugin, arrayDim - 1, id, newSuffix, modelstate,
				// variableNode);
			}
		}
	}

}