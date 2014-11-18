package analysis.dynamicsim.hierarchical.states;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;

public abstract class VariableState extends ModuleState
{
	private final LinkedHashSet<String> compartmentIDSet;
	private final LinkedHashSet<String> speciesIDSet;
	private HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap;
	private final HashMap<String, HashSet<ASTNode>> variableToAffectedConstraintSetMap;
	private HashMap<String, HashSet<String>> variableToEventSetMap;
	private final HashMap<String, Boolean> variableToIsConstantMap;
	private HashMap<String, Boolean> variableToIsInAssignmentRuleMap;
	private final HashMap<String, Boolean> variableToIsInConstraintMap;
	private HashMap<String, Boolean> variableToIsInRateRuleMap;
	private final TObjectDoubleHashMap<String> variableToValueMap;

	public VariableState(HashMap<String, Model> models, String bioModel,
			String submodelID)
	{
		super(models, bioModel, submodelID);
		compartmentIDSet = new LinkedHashSet<String>();
		speciesIDSet = new LinkedHashSet<String>((int) getNumSpecies());
		variableToIsConstantMap = new HashMap<String, Boolean>(
				(int) (getNumSpecies() + getNumParameters()));
		variableToValueMap = new TObjectDoubleHashMap<String>(
				(int) getNumSpecies() + (int) getNumParameters());
		variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode>>(
				(int) getNumConstraints());
		variableToIsInConstraintMap = new HashMap<String, Boolean>(
				(int) (getNumSpecies() + getNumParameters()));
	}

	public HashMap<String, HashSet<AssignmentRule>> getVariableToAffectedAssignmentRuleSetMap()
	{
		return variableToAffectedAssignmentRuleSetMap;
	}

	public void setVariableToAffectedAssignmentRuleSetMap(
			HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap)
	{
		this.variableToAffectedAssignmentRuleSetMap = variableToAffectedAssignmentRuleSetMap;
	}

	public HashMap<String, HashSet<String>> getVariableToEventSetMap()
	{
		return variableToEventSetMap;
	}

	public void setVariableToEventSetMap(
			HashMap<String, HashSet<String>> variableToEventSetMap)
	{
		this.variableToEventSetMap = variableToEventSetMap;
	}

	public HashMap<String, Boolean> getVariableToIsInAssignmentRuleMap()
	{
		return variableToIsInAssignmentRuleMap;
	}

	public void setVariableToIsInAssignmentRuleMap(
			HashMap<String, Boolean> variableToIsInAssignmentRuleMap)
	{
		this.variableToIsInAssignmentRuleMap = variableToIsInAssignmentRuleMap;
	}

	public HashMap<String, Boolean> getVariableToIsInRateRuleMap()
	{
		return variableToIsInRateRuleMap;
	}

	public void setVariableToIsInRateRuleMap(
			HashMap<String, Boolean> variableToIsInRateRuleMap)
	{
		this.variableToIsInRateRuleMap = variableToIsInRateRuleMap;
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

	public HashMap<String, Boolean> getVariableToIsConstantMap()
	{
		return variableToIsConstantMap;
	}

	public HashMap<String, Boolean> getVariableToIsInConstraintMap()
	{
		return variableToIsInConstraintMap;
	}

	public TObjectDoubleHashMap<String> getVariableToValueMap()
	{
		return variableToValueMap;
	}

}
