package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public abstract class ReactionState extends SpeciesState
{

	private final LinkedHashSet<String>								nonconstantParameterIDSet;
	private final HashSet<String>									nonConstantStoichiometry;
	private final Map<String, ASTNode>								reactionToFormulaMap;
	private final Map<String, Set<HierarchicalStringPair>>			reactionToNonconstantStoichiometriesSetMap;
	private final Map<String, Double>								reactionToPropensityMap;
	private final Map<String, Set<HierarchicalStringDoublePair>>	reactionToReactantStoichiometrySetMap;
	private final Map<String, Set<HierarchicalStringDoublePair>>	reactionToSpeciesAndStoichiometrySetMap;
	private final Map<String, Boolean>								reactionToHasEnoughMoleculesMap;

	public ReactionState(Map<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);

		nonConstantStoichiometry = new HashSet<String>();
		reactionToPropensityMap = new HashMap<String, Double>((int) (getNumReactions() * 1.5));
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, Set<HierarchicalStringDoublePair>>((int) (getNumReactions() * 1.5));
		reactionToReactantStoichiometrySetMap = new HashMap<String, Set<HierarchicalStringDoublePair>>((int) (getNumReactions() * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (getNumReactions() * 1.5));
		reactionToHasEnoughMoleculesMap = new HashMap<String, Boolean>((int) (getNumReactions() * 1.5));
		nonconstantParameterIDSet = new LinkedHashSet<String>();
		reactionToNonconstantStoichiometriesSetMap = new HashMap<String, Set<HierarchicalStringPair>>();

	}

	public ReactionState(ReactionState state)
	{

		super(state);
		nonConstantStoichiometry = state.nonConstantStoichiometry;
		reactionToPropensityMap = new HashMap<String, Double>(state.reactionToPropensityMap);
		reactionToSpeciesAndStoichiometrySetMap = state.reactionToSpeciesAndStoichiometrySetMap;
		reactionToReactantStoichiometrySetMap = state.reactionToReactantStoichiometrySetMap;
		reactionToFormulaMap = state.reactionToFormulaMap;
		nonconstantParameterIDSet = state.nonconstantParameterIDSet;
		reactionToNonconstantStoichiometriesSetMap = state.reactionToNonconstantStoichiometriesSetMap;
		reactionToHasEnoughMoleculesMap = new HashMap<String, Boolean>((int) (getNumReactions() * 1.5));
	}

	public LinkedHashSet<String> getNonconstantParameterIDSet()
	{
		return nonconstantParameterIDSet;
	}

	public HashSet<String> getNonConstantStoichiometry()
	{
		return nonConstantStoichiometry;
	}

	public Map<String, ASTNode> getReactionToFormulaMap()
	{
		return reactionToFormulaMap;
	}

	public Map<String, Set<HierarchicalStringPair>> getReactionToNonconstantStoichiometriesSetMap()
	{
		return reactionToNonconstantStoichiometriesSetMap;
	}

	public Map<String, Double> getReactionToPropensityMap()
	{
		return reactionToPropensityMap;
	}

	public Map<String, Set<HierarchicalStringDoublePair>> getReactionToReactantStoichiometrySetMap()
	{
		return reactionToReactantStoichiometrySetMap;
	}

	public Map<String, Set<HierarchicalStringDoublePair>> getReactionToSpeciesAndStoichiometrySetMap()
	{
		return reactionToSpeciesAndStoichiometrySetMap;
	}

	public double getPropensity(String reaction)
	{
		return reactionToPropensityMap.containsKey(reaction) ? reactionToPropensityMap.get(reaction) : 0;
	}

	public Set<String> getSetOfReactions()
	{
		return reactionToFormulaMap == null ? null : reactionToFormulaMap.keySet();
	}

	public Map<String, Boolean> getReactionToHasEnoughMolecules()
	{
		return reactionToHasEnoughMoleculesMap;
	}

}
