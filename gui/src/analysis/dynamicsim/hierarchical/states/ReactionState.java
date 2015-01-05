package analysis.dynamicsim.hierarchical.states;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;

public abstract class ReactionState extends SpeciesState
{

	private final LinkedHashSet<String>										nonconstantParameterIDSet;
	private final HashSet<String>											nonConstantStoichiometry;
	private final HashMap<String, ASTNode>									reactionToFormulaMap;
	private final HashMap<String, HashSet<HierarchicalStringPair>>			reactionToNonconstantStoichiometriesSetMap;
	private final TObjectDoubleHashMap<String>								reactionToPropensityMap;
	private final HashMap<String, HashSet<HierarchicalStringDoublePair>>	reactionToReactantStoichiometrySetMap;
	private final HashMap<String, HashSet<HierarchicalStringDoublePair>>	reactionToSpeciesAndStoichiometrySetMap;

	public ReactionState(HashMap<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);

		nonConstantStoichiometry = new HashSet<String>();

		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (getNumReactions() * 1.5));

		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<HierarchicalStringDoublePair>>(
				(int) (getNumReactions() * 1.5));

		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<HierarchicalStringDoublePair>>(
				(int) (getNumReactions() * 1.5));

		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (getNumReactions() * 1.5));

		nonconstantParameterIDSet = new LinkedHashSet<String>();
		reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<HierarchicalStringPair>>();

	}

	public ReactionState(ReactionState state)
	{

		super(state);
		nonConstantStoichiometry = state.nonConstantStoichiometry;
		reactionToPropensityMap = new TObjectDoubleHashMap<String>(state.reactionToPropensityMap);
		reactionToSpeciesAndStoichiometrySetMap = state.reactionToSpeciesAndStoichiometrySetMap;
		reactionToReactantStoichiometrySetMap = state.reactionToReactantStoichiometrySetMap;
		reactionToFormulaMap = state.reactionToFormulaMap;
		nonconstantParameterIDSet = state.nonconstantParameterIDSet;
		reactionToNonconstantStoichiometriesSetMap = state.reactionToNonconstantStoichiometriesSetMap;

	}

	public LinkedHashSet<String> getNonconstantParameterIDSet()
	{
		return nonconstantParameterIDSet;
	}

	public HashSet<String> getNonConstantStoichiometry()
	{
		return nonConstantStoichiometry;
	}

	public HashMap<String, ASTNode> getReactionToFormulaMap()
	{
		return reactionToFormulaMap;
	}

	public HashMap<String, HashSet<HierarchicalStringPair>> getReactionToNonconstantStoichiometriesSetMap()
	{
		return reactionToNonconstantStoichiometriesSetMap;
	}

	public TObjectDoubleHashMap<String> getReactionToPropensityMap()
	{
		return reactionToPropensityMap;
	}

	public HashMap<String, HashSet<HierarchicalStringDoublePair>> getReactionToReactantStoichiometrySetMap()
	{
		return reactionToReactantStoichiometrySetMap;
	}

	public HashMap<String, HashSet<HierarchicalStringDoublePair>> getReactionToSpeciesAndStoichiometrySetMap()
	{
		return reactionToSpeciesAndStoichiometrySetMap;
	}

}
