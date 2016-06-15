package analysis.dynamicsim.hierarchical.util.setup;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class RuleSetup
{
	public static void setupRules(ModelState modelstate, Model model)
	{
		long size = model.getListOfRules().size();

		if (size > 0)
		{
			modelstate.setNoRuleFlag(false);
		}

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

	public static void setupSingleAssignmentRule(ModelState modelstate, String variable, ASTNode math, Model model, Map<String, VariableNode> variableToNodes)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		VariableNode variableNode = variableToNodes.get(variable);
		HierarchicalNode assignmentRule = MathInterpreter.parseASTNode(math, variableToNodes, variableNode);
		variableNode.setAssignmentRule(assignmentRule);

	}

	public static void setupSingleRateRule(ModelState modelstate, String variable, ASTNode math, Model model, Map<String, VariableNode> variableToNodes)
	{
		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);
		VariableNode variableNode = variableToNodes.get(variable);
		HierarchicalNode rateRule = MathInterpreter.parseASTNode(math, variableToNodes);
		variableNode.setRateRule(rateRule);
	}

}
