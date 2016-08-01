package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.util.math.ArrayNode;
import analysis.dynamicsim.hierarchical.util.math.ArrayNode.ArraysType;
import analysis.dynamicsim.hierarchical.util.math.Evaluator;
import analysis.dynamicsim.hierarchical.util.math.EventAssignmentNode;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesReferenceNode;
import analysis.dynamicsim.hierarchical.util.math.ValueNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class ArraysSetup implements Setup
{

	private static final String	compartmentAttr	= "compartment";
	private static final String	symbolAttr		= "symbol";
	private static final String	variableAttr	= "variable";
	private static final String	speciesAttr		= "species";

	public static boolean checkArray(SBase sbase)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

		if (plugin == null)
		{
			return false;
		}

		return plugin.getNumDimensions() > 0;
	}

	public static HierarchicalNode setupDimensions(ModelState modelstate, SBase sbase, ASTNode math, ArraysType type)
	{
		return setupDimensions(modelstate, sbase, math, null, type);
	}

	public static void setupDimensions(ModelState modelstate, SBase sbase, VariableNode node, ArraysType type)
	{
		setupDimensions(modelstate, sbase, node, null, type);
	}

	public static void setupDimensions(ModelState modelstate, SBase sbase, HierarchicalNode node, ArraysType type)
	{
		setupDimensions(modelstate, sbase, node, null, type);
	}

	public static HierarchicalNode setupDimensions(ModelState modelstate, SBase sbase, ASTNode math, HierarchicalNode parent, ArraysType type)
	{
		HierarchicalNode refObj = null;
		EventNode eventNode = null;
		EventAssignmentNode eventAssignNode = null;

		ArrayNode arrayNode = setupArrayDimensions(modelstate, sbase, parent, type);
		if (arrayNode != null)
		{
			switch (type)
			{
			case EVENT:
				refObj = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), arrayNode.getDimensionMap());
				eventNode = new EventNode(refObj);
				eventNode.setArrayNode(arrayNode);
				return eventNode;
			case EVENTASSIGNMENT:
				refObj = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), arrayNode.getDimensionMap());
				eventAssignNode = new EventAssignmentNode(refObj);
				eventAssignNode.setArrayNode(arrayNode);
				return eventAssignNode;
			default:
				refObj = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap(), arrayNode.getDimensionMap());
				refObj.setArrayNode(arrayNode);
				return refObj;
			}

		}
		return null;
	}

	public static void setupDimensions(ModelState modelstate, SBase sbase, VariableNode node, HierarchicalNode parent, ArraysType type)
	{
		ArrayNode arrayNode = setupArrayDimensions(modelstate, sbase, parent, type);

		if (arrayNode != null)
		{
			modelstate.addArray(node);
			node.setArrayNode(arrayNode);
		}

	}

	public static void setupDimensions(ModelState modelstate, SBase sbase, HierarchicalNode node, HierarchicalNode parent, ArraysType type)
	{
		ArrayNode arrayNode = setupArrayDimensions(modelstate, sbase, parent, type);

		if (arrayNode != null)
		{
			// modelstate.addArray(node);
			node.setArrayNode(arrayNode);
		}

	}

	private static ArrayNode setupArrayDimensions(ModelState modelstate, SBase sbase, HierarchicalNode parent, ArraysType type)
	{
		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

		if (plugin != null)
		{

			if (plugin.getNumDimensions() > 0)
			{
				ArrayNode arrayNode = new ArrayNode(type);
				int offset = addImplicitDimensions(parent, arrayNode);

				for (int i = plugin.getDimensionCount() - 1; i >= 0; i--)
				{
					Dimension dim = plugin.getDimensionByArrayDimension(i);
					arrayNode.addDimension(dim.getId());
					switch (type)
					{
					case COMPARTMENT:
					case PARAMETER:
						arrayNode.setDimensionSizeId(i + offset, dim.getSize());
						break;
					default:
						arrayNode.setDimensionSize(i + offset, modelstate.getNode(dim.getSize()));
					}
				}

				return arrayNode;
			}
		}
		return null;
	}

	private static int addImplicitDimensions(HierarchicalNode parent, ArrayNode child)
	{
		if (parent != null && parent.getArrayNode() != null)
		{
			ArrayNode arrayNode = parent.getArrayNode();
			for (int i = 0; i < arrayNode.getNumDimensions(); i++)
			{
				child.addDimension(arrayNode.getArrayDimensionNode(i));
			}
			return arrayNode.getNumDimensions();
		}
		return 0;
	}

	public static void linkDimensionSize(ModelState modelstate)
	{
		for (int i = modelstate.getNumOfArrays() - 1; i >= 0; i--)
		{
			ArrayNode node = modelstate.getArrays().get(i).getArrayNode();

			for (int j = node.getNumDimensions(); j >= 0; j--)
			{
				node.setDimensionSize(j, modelstate.getNode(node.getDimensionSizeId(j)));
			}
		}
	}

	public static void setupIndices(ModelState modelstate, SBase sbase, ArrayNode arrayNode, ArraysType type)
	{

		ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

		if (plugin == null)
		{
			return;
		}

		if (plugin.getNumIndices() > 0)
		{

			String attribute;
			HierarchicalNode math;
			switch (type)
			{
			case SPECIES:
				Species species = (Species) sbase;
				attribute = compartmentAttr;
				math = convertIndex(modelstate, arrayNode, species.getCompartment(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case ASSIGNRULE:
			case RATERULE:
				ExplicitRule rule = (ExplicitRule) sbase;
				attribute = variableAttr;
				math = convertIndex(modelstate, arrayNode, rule.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case INITASSIGNMENT:
				InitialAssignment initAssignment = (InitialAssignment) sbase;
				attribute = symbolAttr;
				math = convertIndex(modelstate, arrayNode, initAssignment.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case REACTANT:
			case PRODUCT:
				SpeciesReference specRef = (SpeciesReference) sbase;
				attribute = speciesAttr;
				math = convertIndex(modelstate, arrayNode, specRef.getSpecies(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case EVENTASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				attribute = variableAttr;
				math = convertIndex(modelstate, arrayNode, eventAssignment.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			}
		}
	}

	public static void expandArrays(ModelState modelstate)
	{
		for (int i = modelstate.getNumOfArrays() - 1; i >= 0; i--)
		{
			HierarchicalNode node = modelstate.getArrays().get(i);
			expandArray(modelstate, node);
		}

	}

	public static void expandArray(ModelState modelstate, HierarchicalNode node)
	{
		ArrayNode arrayNode = node.getArrayNode();
		setupArrays(arrayNode.getNumDimensions() - 1, modelstate, node, null, arrayNode, new int[arrayNode.getNumDimensions()], arrayNode.getArraysType());
	}

	public static void expandArray(ModelState modelstate, HierarchicalNode node, HierarchicalNode parent)
	{
		ArrayNode arrayNode = node.getArrayNode();
		setupArrays(arrayNode.getNumDimensions() - 1, modelstate, node, parent, arrayNode, new int[arrayNode.getNumDimensions()], arrayNode.getArraysType());
	}

	private static int getMaxArrayDim(ArraysSBasePlugin plugin, String attribute)
	{
		int maxIndex = -1;
		for (Index index : plugin.getListOfIndices())
		{
			if (index.isSetArrayDimension() && index.getArrayDimension() > maxIndex && index.isSetReferencedAttribute() && index.getReferencedAttribute().equals(attribute))
			{
				maxIndex = index.getArrayDimension();
			}
		}

		return maxIndex;
	}

	private static HierarchicalNode convertIndex(ModelState modelstate, ArrayNode arrayNode, String referencedNode, ArraysSBasePlugin plugin, String attribute)
	{
		int maxIndex = getMaxArrayDim(plugin, attribute);

		HierarchicalNode selector = new HierarchicalNode(Type.FUNCTION_SELECTOR);
		selector.addChild(modelstate.getNode(referencedNode));

		for (int i = maxIndex; i >= 0; i--)
		{
			Index index = plugin.getIndex(i, attribute);
			HierarchicalNode indexMath = MathInterpreter.parseASTNode(index.getMath(), modelstate.getVariableToNodeMap(), arrayNode.getDimensionMap());
			selector.addChild(indexMath);
		}

		return selector;
	}

	private static void setupArrays(int arrayDim, ModelState modelstate, HierarchicalNode refNode, HierarchicalNode objParent, HierarchicalNode arrayParent, int[] dimValues, ArraysType type)
	{
		ArrayNode arrayNode = refNode.getArrayNode();
		ValueNode sizeNode = arrayNode.getDimensionSize(arrayDim);
		int size = (int) sizeNode.getValue();
		String newId;
		for (int i = 0; i < size; i++)
		{
			int[] copy = dimValues.clone();
			copy[arrayDim] = i;

			if (arrayDim == 0)
			{
				VariableNode variableNode;
				ReactionNode reactionNode;
				EventNode eventNode;
				HierarchicalNode clone;
				switch (type)
				{
				case SPECIES:
					SpeciesNode speciesNode = (SpeciesNode) refNode;
					newId = getArrayedId(speciesNode.getName(), copy);
					SpeciesNode specChild = new SpeciesNode(speciesNode);
					specChild.setName(newId);
					arrayParent.addChild(specChild);
					modelstate.addVariable(specChild);
					if (arrayNode.hasIndexReference(compartmentAttr))
					{
						for (int j = dimValues.length - 1; j >= 0; j--)
						{
							arrayNode.setDimensionValue(j, dimValues[j]);
						}
						specChild.setCompartment((VariableNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(compartmentAttr)));
					}
					break;
				case REACTION:
					reactionNode = (ReactionNode) refNode;
					newId = getArrayedId(reactionNode.getName(), copy);
					ReactionNode reacChild = new ReactionNode(reactionNode);
					reacChild.setName(newId);
					arrayParent.addChild(reacChild);
					modelstate.addVariable(reacChild);
					break;
				case COMPARTMENT:
				case PARAMETER:
					variableNode = (VariableNode) refNode;
					newId = getArrayedId(variableNode.getName(), copy);
					VariableNode varChild = variableNode.clone();
					varChild.setName(newId);
					arrayParent.addChild(varChild);
					if (varChild.isConstant())
					{
						modelstate.addVariable(varChild);
					}
					else
					{
						modelstate.addConstant(varChild);
					}
					break;
				case INITASSIGNMENT:
					clone = refNode.clone();
					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, dimValues[j]);
					}
					MathInterpreter.replaceNameNodes(clone, arrayNode.getDimensionMap());
					if (arrayNode.hasIndexReference(symbolAttr))
					{
						variableNode = (VariableNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(symbolAttr));
						variableNode.setInitialAssignment(clone);
					}
					break;
				case ASSIGNRULE:

					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, dimValues[j]);
					}
					clone = MathInterpreter.replaceNameNodes(refNode, arrayNode.getDimensionMap());
					if (arrayNode.hasIndexReference(variableAttr))
					{
						variableNode = (VariableNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(variableAttr));
						variableNode.setAssignmentRule(clone);
					}
					break;
				case RATERULE:
					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, dimValues[j]);
					}
					clone = MathInterpreter.replaceNameNodes(refNode, arrayNode.getDimensionMap());
					if (arrayNode.hasIndexReference(variableAttr))
					{
						variableNode = (VariableNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(variableAttr));
						variableNode.setAssignmentRule(clone);
					}
					break;
				case REACTANT:
					SpeciesReferenceNode reactantNode = (SpeciesReferenceNode) refNode;
					SpeciesReferenceNode reactantChild = new SpeciesReferenceNode(reactantNode);
					if (reactantNode.getName() != null)
					{
						newId = getArrayedId(reactantNode.getName(), copy);
						reactantChild.setName(newId);
					}
					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, dimValues[j]);
					}
					if (arrayNode.hasIndexReference(speciesAttr))
					{
						speciesNode = (SpeciesNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(speciesAttr));
						reactantChild.setSpecies(speciesNode);
					}
					reactionNode = (ReactionNode) getIndexedNode(objParent);
					reactionNode.addReactant(reactantChild);
					break;
				case PRODUCT:
					SpeciesReferenceNode productNode = (SpeciesReferenceNode) refNode;
					SpeciesReferenceNode productChild = new SpeciesReferenceNode(productNode);
					if (productNode.getName() != null)
					{
						newId = getArrayedId(productNode.getName(), copy);
						productChild.setName(newId);
					}
					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, copy[j]);
					}
					reactionNode = (ReactionNode) getIndexedNode(objParent);
					break;
				case EVENT:
					eventNode = (EventNode) refNode;
					EventNode eventChild = new EventNode(eventNode);
					arrayParent.addChild(eventChild);
					modelstate.addEvent(eventChild);

					break;
				case EVENTASSIGNMENT:
					EventAssignmentNode eventAssignNode = (EventAssignmentNode) refNode;
					EventAssignmentNode eventAssignChild = new EventAssignmentNode(eventAssignNode);

					for (int j = dimValues.length - 1; j >= 0; j--)
					{
						arrayNode.setDimensionValue(j, copy[j]);
					}
					if (arrayNode.hasIndexReference(variableAttr))
					{
						variableNode = (VariableNode) Evaluator.evaluateArraysSelector(modelstate, arrayNode.getIndexMap().get(variableAttr));
						eventAssignChild.setVariable(variableNode);
					}
					eventNode = (EventNode) getIndexedNode(objParent);
					eventNode.addEventAssignment(eventAssignChild);
					break;
				}

			}
			else
			{
				HierarchicalNode child = new HierarchicalNode(Type.VECTOR);
				arrayParent.addChild(child);
				setupArrays(arrayDim - 1, modelstate, refNode, objParent, child, copy, type);
			}

			copy = null;
		}
	}

	private static HierarchicalNode getIndexedNode(HierarchicalNode node)
	{
		if (node.getArrayNode() != null)
		{
			ArrayNode arrayNode = node.getArrayNode();
			HierarchicalNode child = arrayNode;
			for (int i = 0; i < arrayNode.getNumDimensions(); i++)
			{
				child = child.getChild((int) arrayNode.getDimension(i).getValue());
			}

			return child;
		}

		return null;
	}

	private static String getArrayedId(String id, int[] dimValues)
	{
		StringBuilder build = new StringBuilder(id);

		for (int i = dimValues.length - 1; i >= 0; i--)
		{
			build.append("[");
			build.append(dimValues[i]);
			build.append("]");
		}

		return build.toString();
	}

}