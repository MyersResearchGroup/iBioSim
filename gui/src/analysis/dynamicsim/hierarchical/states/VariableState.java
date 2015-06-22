package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;

public abstract class VariableState extends DocumentState
{
	private final LinkedHashSet<String>					compartmentIDSet;
	private final LinkedHashSet<String>					speciesIDSet;
	private HashMap<String, HashSet<AssignmentRule>>	variableToAffectedAssignmentRuleSetMap;
	private final HashMap<String, HashSet<ASTNode>>		variableToAffectedConstraintSetMap;
	private HashMap<String, HashSet<String>>			variableToEventSetMap;
	private final Set<String>							variableToIsConstant;
	private HashMap<String, Boolean>					variableToIsInAssignmentRuleMap;
	private final HashMap<String, Boolean>				variableToIsInConstraintMap;
	private HashMap<String, Boolean>					variableToIsInRateRuleMap;
	private Map<String, Double>							variableToValueMap;

	public VariableState(Map<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
		compartmentIDSet = new LinkedHashSet<String>();
		speciesIDSet = new LinkedHashSet<String>((int) getNumSpecies());
		variableToIsConstant = new HashSet<String>((int) (getNumSpecies() + getNumParameters()));
		variableToValueMap = new HashMap<String, Double>((int) getNumSpecies() + (int) getNumParameters());
		variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode>>((int) getNumConstraints());
		variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (getNumSpecies() + getNumParameters()));
	}

	public VariableState(VariableState state)
	{
		super(state);
		compartmentIDSet = state.compartmentIDSet;
		speciesIDSet = state.speciesIDSet;
		variableToIsConstant = state.variableToIsConstant;
		variableToValueMap = new HashMap<String, Double>(state.variableToValueMap);
		variableToAffectedConstraintSetMap = state.variableToAffectedConstraintSetMap;
		variableToIsInConstraintMap = state.variableToIsInConstraintMap;
	}

	public boolean isSetVariableToAffectedAssignmentRule()
	{
		return variableToAffectedAssignmentRuleSetMap != null;
	}

	public HashMap<String, HashSet<AssignmentRule>> getVariableToAffectedAssignmentRuleSetMap()
	{
		return variableToAffectedAssignmentRuleSetMap;
	}

	public void setVariableToAffectedAssignmentRuleSetMap(HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap)
	{
		this.variableToAffectedAssignmentRuleSetMap = variableToAffectedAssignmentRuleSetMap;
	}

	public HashMap<String, HashSet<String>> getVariableToEventSetMap()
	{
		return variableToEventSetMap;
	}

	public void setVariableToEventSetMap(HashMap<String, HashSet<String>> variableToEventSetMap)
	{
		this.variableToEventSetMap = variableToEventSetMap;
	}

	public void addVariableToIsInAssignmentRule(String key, boolean value)
	{
		variableToIsInAssignmentRuleMap.put(key, value);
	}

	public HashMap<String, Boolean> getVariableToIsInAssignmentRuleMap()
	{
		return variableToIsInAssignmentRuleMap;
	}

	public void setVariableToIsInAssignmentRuleMap(HashMap<String, Boolean> variableToIsInAssignmentRuleMap)
	{
		this.variableToIsInAssignmentRuleMap = variableToIsInAssignmentRuleMap;
	}

	public HashMap<String, Boolean> getVariableToIsInRateRuleMap()
	{
		return variableToIsInRateRuleMap;
	}

	public void setVariableToIsInRateRuleMap(HashMap<String, Boolean> variableToIsInRateRuleMap)
	{
		this.variableToIsInRateRuleMap = variableToIsInRateRuleMap;
	}

	public void addCompartment(String id)
	{
		compartmentIDSet.add(id);
	}

	public LinkedHashSet<String> getCompartmentIDSet()
	{
		return compartmentIDSet;
	}

	public LinkedHashSet<String> getSpeciesIDSet()
	{
		return speciesIDSet;
	}

	public HashMap<String, HashSet<ASTNode>> getVariableToAffectedConstraintSetMap()
	{
		return variableToAffectedConstraintSetMap;
	}

	public boolean isConstant(String variable)
	{
		return variableToIsConstant.contains(variable);
	}

	public void addVariableToIsConstant(String id)
	{
		variableToIsConstant.add(id);
	}

	public void addVariableToIsInConstraint(String key, boolean value)
	{
		variableToIsInConstraintMap.put(key, value);
	}

	public HashMap<String, Boolean> getVariableToIsInConstraintMap()
	{
		return variableToIsInConstraintMap;
	}

	public void addVariableToValueMap(String key, double value)
	{
		variableToValueMap.put(key, value);
	}

	public Map<String, Double> getVariableToValueMap()
	{
		return variableToValueMap;
	}

	public void setVariableToValueMap(Map<String, Double> variableToValueMap)
	{
		this.variableToValueMap = variableToValueMap;
	}

}
