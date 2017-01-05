package backend.analysis.dynamicsim.hierarchical.util.setup;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

public class RuleSetup
{
	public static void setupRules(HierarchicalModel modelstate, Model model)
	{
		for (Rule rule : model.getListOfRules())
		{
			if (rule.isSetMetaId() && modelstate.isDeletedByMetaId(rule.getMetaId()))
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
