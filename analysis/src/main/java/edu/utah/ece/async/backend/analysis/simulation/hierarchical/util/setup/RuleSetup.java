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
package edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.setup;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.backend.analysis.simulation.hierarchical.util.interpreter.MathInterpreter;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class RuleSetup
{
	public static void setupRules(HierarchicalModel modelstate, Model model)
	{
		for (Rule rule : model.getListOfRules())
		{
			if (rule.isSetMetaId() && modelstate.isDeletedByMetaId(rule.getMetaId()) || !rule.isSetMath())
			{
				continue;
			}
			
			if (rule.isAssignment())
			{
				AssignmentRule assignRule = (AssignmentRule) rule;
				setupSingleAssignmentRule(modelstate, assignRule.getVariable(), assignRule.getMath(), model, modelstate.getVariableToNodeMap());
			}
			else if (rule.isRate())
			{
				RateRule rateRule = (RateRule) rule;
				setupSingleRateRule(modelstate, rateRule.getVariable(), rateRule.getMath(), model, modelstate.getVariableToNodeMap());
			}
		}
	}

	public static void setupSingleAssignmentRule(HierarchicalModel modelstate, String variable, ASTNode math, Model model, Map<String, VariableNode> variableToNodes)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		VariableNode variableNode = variableToNodes.get(variable);
		HierarchicalNode assignmentRule = MathInterpreter.parseASTNode(math, variableToNodes, variableNode);
		FunctionNode node = new FunctionNode(variableNode, assignmentRule);
		modelstate.addAssignRule(node);
		variableNode.setHasRule(true);

	}

	public static void setupSingleRateRule(HierarchicalModel modelstate, String variable, ASTNode math, Model model, Map<String, VariableNode> variableToNodes)
	{
	  //TODO: fix
		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		VariableNode variableNode = variableToNodes.get(variable);
		HierarchicalNode rateRule = MathInterpreter.parseASTNode(math, variableToNodes);
		variableNode.setRateRule(rateRule);
	}

}
