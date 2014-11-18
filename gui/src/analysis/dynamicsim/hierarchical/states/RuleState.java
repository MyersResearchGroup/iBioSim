package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;

public abstract class RuleState extends EventState
{

	private List<RateRule> rateRulesList;

	public RuleState(HashMap<String, Model> models, String bioModel,
			String submodelID)
	{
		super(models, bioModel, submodelID);
		if (getNumRules() > 0)
		{

			rateRulesList = new LinkedList<RateRule>();
			setVariableToAffectedAssignmentRuleSetMap(new HashMap<String, HashSet<AssignmentRule>>(
					(int) getNumRules()));
			setVariableToIsInAssignmentRuleMap(new HashMap<String, Boolean>(
					(int) (getNumRules() + getNumParameters())));
			setVariableToIsInRateRuleMap(new HashMap<String, Boolean>(
					(int) (getNumRules() + getNumParameters())));
		}

	}

	public List<RateRule> getRateRulesList()
	{
		return rateRulesList;
	}

	public void setRateRulesList(List<RateRule> rateRulesList)
	{
		this.rateRulesList = rateRulesList;
	}

}
