/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ArrayNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ArrayNode.ArraysType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ModelContainer;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ArraysSetup
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


	public static void setupDimensions(HierarchicalModel modelstate, SBase sbase, VariableNode node, ArraysType type)
	{
		setupDimensions(modelstate, sbase, node, null, type);
	}

	public static void setupDimensions(HierarchicalModel modelstate, SBase sbase, HierarchicalNode node, ArraysType type)
	{
		setupDimensions(modelstate, sbase, node, null, type);
	}

	public static HierarchicalNode setupDimensions(HierarchicalModel modelstate, ModelContainer container, SBase sbase, ASTNode math, HierarchicalNode parent, ArraysType type)
	{
	  Model model = container.getModel();
	  
		HierarchicalNode refObj = null;
		EventNode eventNode = null;
		FunctionNode eventAssignNode = null;
		
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
				eventAssignNode = new FunctionNode(refObj);
				eventAssignNode.setArrayNode(arrayNode);
				return eventAssignNode;
			default:
				refObj = MathInterpreter.parseASTNode(math,  modelstate.getVariableToNodeMap(), arrayNode.getDimensionMap());
				refObj.setArrayNode(arrayNode);
				return refObj;
			}

		}
		return null;
	}

	public static void setupDimensions(HierarchicalModel modelstate, SBase sbase, VariableNode node, HierarchicalNode parent, ArraysType type)
	{
		ArrayNode arrayNode = setupArrayDimensions(modelstate, sbase, parent, type);

		if (arrayNode != null)
		{
			modelstate.addArray(node);
			node.setArrayNode(arrayNode);
		}

	}

	public static void setupDimensions(HierarchicalModel modelstate, SBase sbase, HierarchicalNode node, HierarchicalNode parent, ArraysType type)
	{
		ArrayNode arrayNode = setupArrayDimensions(modelstate, sbase, parent, type);

		if (arrayNode != null)
		{
			// modelstate.addArray(node);
			node.setArrayNode(arrayNode);
		}

	}

	private static ArrayNode setupArrayDimensions(HierarchicalModel modelstate, SBase sbase, HierarchicalNode parent, ArraysType type)
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

	public static void linkDimensionSize(HierarchicalModel modelstate)
	{
		for (VariableNode var : modelstate.getArrays())
		{
		  ArrayNode node = var.getArrayNode();
			for (int j = node.getNumDimensions(); j >= 0; j--)
			{
				node.setDimensionSize(j, modelstate.getNode(node.getDimensionSizeId(j)));
			}
		}
	}

	public static void setupIndices(HierarchicalModel modelstate, ModelContainer container, SBase sbase, ArrayNode arrayNode, ArraysType type)
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
				math = convertIndex(modelstate, container, arrayNode, species.getCompartment(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case ASSIGNRULE:
			case RATERULE:
				ExplicitRule rule = (ExplicitRule) sbase;
				attribute = variableAttr;
				math = convertIndex(modelstate, container, arrayNode, rule.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case INITASSIGNMENT:
				InitialAssignment initAssignment = (InitialAssignment) sbase;
				attribute = symbolAttr;
				math = convertIndex(modelstate,container, arrayNode, initAssignment.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case REACTANT:
			case PRODUCT:
				SpeciesReference specRef = (SpeciesReference) sbase;
				attribute = speciesAttr;
				math = convertIndex(modelstate, container, arrayNode, specRef.getSpecies(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			case EVENTASSIGNMENT:
				EventAssignment eventAssignment = (EventAssignment) sbase;
				attribute = variableAttr;
				math = convertIndex(modelstate, container,arrayNode, eventAssignment.getVariable(), plugin, attribute);
				arrayNode.addIndex(attribute, math);
				break;
			}
		}
	}

	public static void expandArrays(HierarchicalModel modelstate)
	{
		for (VariableNode node : modelstate.getArrays())
		{
			expandArray(modelstate, node);
		}

	}

	public static void expandArray(HierarchicalModel modelstate, HierarchicalNode node)
	{
		ArrayNode arrayNode = node.getArrayNode();
		setupArrays(arrayNode.getNumDimensions() - 1, modelstate, node, null, arrayNode, new int[arrayNode.getNumDimensions()], arrayNode.getArraysType());
	}

	public static void expandArray(HierarchicalModel modelstate, HierarchicalNode node, HierarchicalNode parent)
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

	private static HierarchicalNode convertIndex(HierarchicalModel modelstate, ModelContainer container, ArrayNode arrayNode, String referencedNode, ArraysSBasePlugin plugin, String attribute)
	{
	  Model model = container.getModel(); 
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

	private static void setupArrays(int arrayDim, HierarchicalModel modelstate, HierarchicalNode refNode, HierarchicalNode objParent, HierarchicalNode arrayParent, int[] dimValues, ArraysType type)
	{
		// ArrayNode arrayNode = refNode.getArrayNode();
		// HierarchicalNode sizeNode = arrayNode.getDimensionSize(arrayDim);
		// int size = (int) sizeNode.getValue();
		// String newId;
		// for (int i = 0; i < size; i++)
		// {
		// int[] copy = dimValues.clone();
		// copy[arrayDim] = i;
		//
		// if (arrayDim == 0)
		// {
		// VariableNode variableNode;
		// ReactionNode reactionNode;
		// EventNode eventNode;
		// HierarchicalNode clone;
		// switch (type)
		// {
		// case SPECIES:
		// SpeciesNode speciesNode = (SpeciesNode) refNode;
		// newId = getArrayedId(speciesNode.getName(), copy);
		// SpeciesNode specChild = new SpeciesNode(speciesNode);
		// specChild.setName(newId);
		// arrayParent.addChild(specChild);
		// if (arrayNode.hasIndexReference(compartmentAttr))
		// {
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, dimValues[j]);
		// }
		// specChild.setCompartment((VariableNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(compartmentAttr)));
		// }
		// if (speciesNode.isConstant())
		// {
		// modelstate.addVariable(speciesNode);
		// }
		// else
		// {
		// modelstate.addConstant(speciesNode);
		// }
		// break;
		// case REACTION:
		// reactionNode = (ReactionNode) refNode;
		// newId = getArrayedId(reactionNode.getName(), copy);
		// ReactionNode reacChild = new ReactionNode(reactionNode);
		// reacChild.setName(newId);
		// arrayParent.addChild(reacChild);
		// modelstate.addVariable(reacChild);
		// break;
		// case COMPARTMENT:
		// case PARAMETER:
		// variableNode = (VariableNode) refNode;
		// newId = getArrayedId(variableNode.getName(), copy);
		// VariableNode varChild = variableNode.clone();
		// varChild.setName(newId);
		// arrayParent.addChild(varChild);
		// if (varChild.isConstant())
		// {
		// modelstate.addVariable(varChild);
		// }
		// else
		// {
		// modelstate.addConstant(varChild);
		// }
		// break;
		// case INITASSIGNMENT:
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, dimValues[j]);
		// }
		// clone = MathInterpreter.copyMath(refNode,
		// arrayNode.getDimensionMap(), true);
		// if (arrayNode.hasIndexReference(symbolAttr))
		// {
		// variableNode = (VariableNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(symbolAttr));
		// variableNode.setInitialAssignment(clone);
		// }
		// break;
		// case ASSIGNRULE:
		//
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, dimValues[j]);
		// }
		// clone = MathInterpreter.copyMath(refNode,
		// arrayNode.getDimensionMap(), true);
		// if (arrayNode.hasIndexReference(variableAttr))
		// {
		// variableNode = (VariableNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(variableAttr));
		// variableNode.setAssignmentRule(clone);
		// }
		// break;
		// case RATERULE:
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, dimValues[j]);
		// }
		// clone = MathInterpreter.copyMath(refNode,
		// arrayNode.getDimensionMap(), true);
		// if (arrayNode.hasIndexReference(variableAttr))
		// {
		// variableNode = (VariableNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(variableAttr));
		// variableNode.setAssignmentRule(clone);
		// }
		// break;
		// case REACTANT:
		// SpeciesReferenceNode reactantNode = (SpeciesReferenceNode) refNode;
		// SpeciesReferenceNode reactantChild = new
		// SpeciesReferenceNode(reactantNode);
		// if (reactantNode.getName() != null)
		// {
		// newId = getArrayedId(reactantNode.getName(), copy);
		// reactantChild.setName(newId);
		// }
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, dimValues[j]);
		// }
		// if (arrayNode.hasIndexReference(speciesAttr))
		// {
		// speciesNode = (SpeciesNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(speciesAttr));
		// reactantChild.setSpecies(speciesNode);
		// }
		// reactionNode = (ReactionNode) getIndexedNode(objParent);
		// reactionNode.addReactant(reactantChild);
		// break;
		// case PRODUCT:
		// SpeciesReferenceNode productNode = (SpeciesReferenceNode) refNode;
		// SpeciesReferenceNode productChild = new
		// SpeciesReferenceNode(productNode);
		// if (productNode.getName() != null)
		// {
		// newId = getArrayedId(productNode.getName(), copy);
		// productChild.setName(newId);
		// }
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, copy[j]);
		// }
		// reactionNode = (ReactionNode) getIndexedNode(objParent);
		// break;
		// case EVENT:
		// eventNode = (EventNode) refNode;
		// clone = MathInterpreter.copyMath(eventNode,
		// arrayNode.getDimensionMap(), true);
		// EventNode eventChild = new EventNode(clone);
		// arrayParent.addChild(eventChild);
		// modelstate.addEvent(eventChild);
		//
		// break;
		// case EVENTASSIGNMENT:
		// EventAssignmentNode eventAssignNode = (EventAssignmentNode) refNode;
		// EventAssignmentNode eventAssignChild = new
		// EventAssignmentNode(eventAssignNode);
		//
		// for (int j = dimValues.length - 1; j >= 0; j--)
		// {
		// arrayNode.setDimensionValue(j, copy[j]);
		// }
		// if (arrayNode.hasIndexReference(variableAttr))
		// {
		// variableNode = (VariableNode)
		// Evaluator.evaluateArraysSelector(modelstate,
		// arrayNode.getIndexMap().get(variableAttr));
		// eventAssignChild.setVariable(variableNode);
		// }
		// eventNode = (EventNode) getIndexedNode(objParent);
		// eventNode.addEventAssignment(eventAssignChild);
		// break;
		// case CONSTRAINT:
		// // ConstraintNode constraintNode = (ConstraintNode) refNode;
		// // newId = getArrayedId(constraintNode.getName(), copy);
		// // ConstraintNode constraintChild = new
		// // ConstraintNode(newId, constraintNode);
		// break;
		// }
		//
		// }
		// else
		// {
		// HierarchicalNode child = new HierarchicalNode(Type.VECTOR);
		// arrayParent.addChild(child);
		// setupArrays(arrayDim - 1, modelstate, refNode, objParent, child,
		// copy, type);
		// }
		//
		// copy = null;
		// }
	}

	private static HierarchicalNode getIndexedNode(HierarchicalNode node)
	{
		// if (node.getArrayNode() != null)
		// {
		// ArrayNode arrayNode = node.getArrayNode();
		// HierarchicalNode child = arrayNode;
		// for (int i = 0; i < arrayNode.getNumDimensions(); i++)
		// {
		// child = child.getChild((int) arrayNode.getDimension(i).getValue());
		// }
		//
		// return child;
		// }

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