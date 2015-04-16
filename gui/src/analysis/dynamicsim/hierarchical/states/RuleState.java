package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;

public abstract class RuleState extends EventState
{

	private List<RateRule>			rateRulesList;
	private List<AssignmentRule>	assignmentRulesList;

	public RuleState(Map<String, Model> models, String bioModel, String submodelID)
	{

		super(models, bioModel, submodelID);

		rateRulesList = new LinkedList<RateRule>();
		assignmentRulesList = new LinkedList<AssignmentRule>();

		if (getNumRules() > 0)
		{

			setVariableToAffectedAssignmentRuleSetMap(new HashMap<String, HashSet<AssignmentRule>>(
					(int) getNumRules()));
			setVariableToIsInAssignmentRuleMap(new HashMap<String, Boolean>(
					(int) (getNumRules() + getNumParameters())));
			setVariableToIsInRateRuleMap(new HashMap<String, Boolean>(
					(int) (getNumRules() + getNumParameters())));
		}

	}

	public RuleState(RuleState state)
	{
		super(state);
		rateRulesList = state.rateRulesList;
	}

	public List<RateRule> getRateRulesList()
	{
		return rateRulesList;
	}

	public void setRateRulesList(List<RateRule> rateRulesList)
	{
		this.rateRulesList = rateRulesList;
	}

	public List<AssignmentRule> getAssignmentRulesList()
	{
		return assignmentRulesList;
	}

	public void setAssignmentRulesList(List<AssignmentRule> assignmentRulesList)
	{
		this.assignmentRulesList = assignmentRulesList;
	}

}
