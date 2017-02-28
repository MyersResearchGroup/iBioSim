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
package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class InitAssignmentSetup
{
	public static void setupInitialAssignments(HierarchicalModel modelstate, Model model)
	{

		for (InitialAssignment initAssignment : model.getListOfInitialAssignments())
		{
			if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(initAssignment.getMetaId()))
			{
				continue;
			}
			String variable = initAssignment.getVariable();
			VariableNode variableNode = modelstate.getNode(variable);
			
			ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, initAssignment.getMath(), model);
			HierarchicalNode initAssignNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap());

			if (variableNode.isSpecies())
			{
				SpeciesNode node = (SpeciesNode) variableNode;

				if (!node.hasOnlySubstance(modelstate.getIndex()))
				{
					HierarchicalNode amountNode = new HierarchicalNode(Type.TIMES);
					amountNode.addChild(initAssignNode);
					amountNode.addChild(node.getCompartment());
					initAssignNode = amountNode;
				}
			}

			FunctionNode node = new FunctionNode(variableNode, initAssignNode);
			node.setIsInitAssignment(true);
			modelstate.addInitAssignment(node);

		}

	}

}
