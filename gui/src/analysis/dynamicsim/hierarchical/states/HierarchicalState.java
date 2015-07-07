package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public abstract class HierarchicalState extends RuleState
{
	private final HashSet<String>									deletedElementsById;
	private final HashSet<String>									deletedElementsByMetaId;
	private final HashSet<String>									deletedElementsByUId;
	private final HashSet<String>									hierarchicalReactions;
	private final HashSet<String>									isHierarchical;
	private final HashMap<String, HashSet<HierarchicalStringPair>>	speciesToReplacement;
	private final HashMap<String, String>							replacementDependency;

	public HierarchicalState(Map<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
		speciesToReplacement = new HashMap<String, HashSet<HierarchicalStringPair>>();
		isHierarchical = new HashSet<String>();
		replacementDependency = new HashMap<String, String>();
		deletedElementsById = new HashSet<String>();
		deletedElementsByMetaId = new HashSet<String>();
		deletedElementsByUId = new HashSet<String>();
		hierarchicalReactions = new HashSet<String>();
	}

	public HierarchicalState(HierarchicalState state)
	{
		super(state);
		speciesToReplacement = state.speciesToReplacement;
		isHierarchical = state.isHierarchical;
		replacementDependency = state.replacementDependency;
		deletedElementsById = state.deletedElementsById;
		deletedElementsByMetaId = state.deletedElementsByMetaId;
		deletedElementsByUId = state.deletedElementsByUId;
		hierarchicalReactions = state.hierarchicalReactions;
	}

	public HashSet<String> getDeletedElementsById()
	{
		return deletedElementsById;
	}

	public HashSet<String> getDeletedElementsByMetaId()
	{
		return deletedElementsByMetaId;
	}

	public HashSet<String> getDeletedElementsByUId()
	{
		return deletedElementsByUId;
	}

	public HashSet<String> getHierarchicalReactions()
	{
		return hierarchicalReactions;
	}

	public HashSet<String> getIsHierarchical()
	{
		return isHierarchical;
	}

	public HashMap<String, HashSet<HierarchicalStringPair>> getSpeciesToReplacement()
	{
		return speciesToReplacement;
	}

	public HashMap<String, String> getReplacementDependency()
	{
		return replacementDependency;
	}

	public boolean isHierarchical(String id)
	{
		return isHierarchical.contains(id);
	}
}
