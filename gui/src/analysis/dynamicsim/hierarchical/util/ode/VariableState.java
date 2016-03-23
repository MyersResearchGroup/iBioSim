package analysis.dynamicsim.hierarchical.util.ode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalObjects.ModelState;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalSpeciesReference;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public class VariableState
{
	private List<Double>						values;
	private List<String>						variables;
	private Map<String, ModelState>				idToModelState;
	private Map<String, Map<Integer, String>>	indexToVariableMap;
	private Map<String, Map<String, Integer>>	variableToIndexMap;
	private Map<String, Map<String, ASTNode>>	dvariablesdtime;
	private Map<String, Map<String, Integer>>	dependencyToDependents;

	public VariableState()
	{
		this.idToModelState = new HashMap<String, ModelState>();
		this.variableToIndexMap = new HashMap<String, Map<String, Integer>>();
		this.indexToVariableMap = new HashMap<String, Map<Integer, String>>();
		this.variables = new ArrayList<String>();
		this.values = new ArrayList<Double>();
		this.dvariablesdtime = new HashMap<String, Map<String, ASTNode>>();
		this.dependencyToDependents = new HashMap<String, Map<String, Integer>>();

	}

	public void addState(ModelState modelstate, Map<String, Double> replacements)
	{
		idToModelState.put(modelstate.getID(), modelstate);
		variableToIndexMap.put(modelstate.getID(), new HashMap<String, Integer>());
		indexToVariableMap.put(modelstate.getID(), new HashMap<Integer, String>());
		dvariablesdtime.put(modelstate.getID(), new HashMap<String, ASTNode>());

		for (String variable : modelstate.getVariableToValueMap().keySet())
		{
			addVariable(modelstate, variable, replacements);
		}

		for (String variable : modelstate.getReplacementDependency().keySet())
		{
			addVariable(modelstate, variable, replacements);
		}

		for (String reaction : modelstate.getReactionToFormulaMap().keySet())
		{
			addReaction(modelstate, reaction);
		}
	}

	public int getDimensions()
	{
		return values.size();
	}

	public double[] getValuesArray()
	{
		double[] temp = new double[values.size()];
		for (int i = 0; i < temp.length; i++)
		{
			temp[i] = values.get(i);
		}
		return temp;
	}

	public String[] getVariablesArray()
	{
		return variables.toArray(new String[variables.size()]);
	}

	private void addVariable(ModelState modelstate, String variable, Map<String, Double> replacements)
	{
		int index = variables.size();

		if (variableToIndexMap.get(modelstate.getID()).containsKey(variable))
		{
			index = variableToIndexMap.get(modelstate.getID()).get(variable);
		}

		if (modelstate.isHierarchical(variable))
		{
			String dependency = modelstate.getReplacementDependency().get(variable);

			if (!dependencyToDependents.containsKey(dependency))
			{
				dependencyToDependents.put(dependency, new HashMap<String, Integer>());
			}

			dependencyToDependents.get(dependency).put(modelstate.getID(), index);
		}

		variables.add(variable);
		values.add(modelstate.getVariableToValue(replacements, variable));
		variableToIndexMap.get(modelstate.getID()).put(variable, index);
		indexToVariableMap.get(modelstate.getID()).put(index, variable);
		dvariablesdtime.get(modelstate.getID()).put(variable, new ASTNode(0));
	}

	private void addReaction(ModelState modelstate, String reaction)
	{
		String modelstateID = modelstate.getID();

		ASTNode formula = modelstate.getReactionToFormulaMap().get(reaction);
		Set<HierarchicalSpeciesReference> reactantAndStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(reaction);
		Set<HierarchicalSpeciesReference> speciesAndStoichiometrySet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reaction);
		Set<HierarchicalStringPair> nonConstantStoichiometrySet = modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reaction);

		if (reactantAndStoichiometrySet != null)
		{
			for (HierarchicalSpeciesReference reactantAndStoichiometry : reactantAndStoichiometrySet)
			{
				String reactant = reactantAndStoichiometry.getString();
				double stoichiometry = reactantAndStoichiometry.getDoub();
				ASTNode stoichNode = new ASTNode();
				stoichNode.setValue(-1 * stoichiometry);
				dvariablesdtime.get(modelstateID).put(reactant, ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
			}
		}

		if (speciesAndStoichiometrySet != null)
		{
			for (HierarchicalSpeciesReference speciesAndStoichiometry : speciesAndStoichiometrySet)
			{
				String species = speciesAndStoichiometry.getString();
				double stoichiometry = speciesAndStoichiometry.getDoub();

				if (stoichiometry > 0)
				{
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(stoichiometry);
					dvariablesdtime.get(modelstateID).put(species, ASTNode.sum(dvariablesdtime.get(modelstateID).get(species), ASTNode.times(formula, stoichNode)));
				}
			}
		}

		if (nonConstantStoichiometrySet != null)
		{
			for (HierarchicalStringPair reactantAndStoichiometry : nonConstantStoichiometrySet)
			{
				String reactant = reactantAndStoichiometry.string1;
				String stoichiometry = reactantAndStoichiometry.string2;
				if (stoichiometry.startsWith("-"))
				{
					ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
					dvariablesdtime.get(modelstateID).put(reactant, ASTNode.diff(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
				}
				else
				{
					ASTNode stoichNode = new ASTNode(stoichiometry);
					dvariablesdtime.get(modelstateID).put(reactant, ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
				}
			}
		}
	}

	public List<Double> getValues()
	{
		return values;
	}

	public List<String> getVariables()
	{
		return variables;
	}

	public Map<String, ModelState> getIdToModelState()
	{
		return idToModelState;
	}

	public Map<String, Map<Integer, String>> getIndexToVariableMap()
	{
		return indexToVariableMap;
	}

	public Map<String, Map<String, Integer>> getVariableToIndexMap()
	{
		return variableToIndexMap;
	}

	public Map<String, Map<String, ASTNode>> getDvariablesdtime()
	{
		return dvariablesdtime;
	}

	public Map<String, Map<String, Integer>> getDependencyToDependents()
	{
		return dependencyToDependents;
	}
}