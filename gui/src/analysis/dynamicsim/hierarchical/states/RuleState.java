package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

public abstract class RuleState extends EventState
{

	private Map<String, ASTNode>	rateRules;
	private Map<String, ASTNode>	assignmentRules;
	private Map<String, ASTNode>	initAssignment;

	public RuleState(Map<String, Model> models, String bioModel, String submodelID)
	{

		super(models, bioModel, submodelID);

		rateRules = new HashMap<String, ASTNode>();
		assignmentRules = new HashMap<String, ASTNode>();
		initAssignment = new HashMap<String, ASTNode>();

		if (getNumRules() > 0)
		{

			setVariableToAffectedAssignmentRuleSetMap(new HashMap<String, HashSet<String>>((int) getNumRules()));
			setVariableToIsInAssignmentRuleMap(new HashMap<String, Boolean>((int) (getNumRules() + getNumParameters())));
			setVariableToIsInRateRuleMap(new HashMap<String, Boolean>((int) (getNumRules() + getNumParameters())));
		}

	}

	public RuleState(RuleState state)
	{
		super(state);
		rateRules = state.rateRules;
	}

	public Map<String, ASTNode> getRateRulesList()
	{
		return rateRules;
	}

	public void setRateRulesList(Map<String, ASTNode> rateRules)
	{
		this.rateRules = rateRules;
	}

	public Map<String, ASTNode> getAssignmentRulesList()
	{
		return assignmentRules;
	}

	public void setAssignmentRulesList(Map<String, ASTNode> assignmentRules)
	{
		this.assignmentRules = assignmentRules;
	}

	public Map<String, ASTNode> getInitAssignment()
	{
		return initAssignment;
	}

	public void setInitAssignment(Map<String, ASTNode> initAssignment)
	{
		this.initAssignment = initAssignment;
	}
}
