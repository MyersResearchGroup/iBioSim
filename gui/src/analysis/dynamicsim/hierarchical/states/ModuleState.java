package analysis.dynamicsim.hierarchical.states;

import java.util.HashSet;
import java.util.Map;

import org.sbml.jsbml.Model;

public abstract class ModuleState
{
	private String			ID;
	private double			maxPropensity;
	private double			minPropensity;
	private final String	model;
	private boolean			noConstraintsFlag;
	private boolean			noEventsFlag;
	private boolean			noRuleFlag;
	private long			numCompartments;
	private long			numConstraints;
	private long			numEvents;
	private int				numInitialAssignments;
	private long			numParameters;
	private int				numRateRules;
	private long			numReactions;
	private long			numRules;
	private long			numSpecies;
	private double			propensity;
	private HashSet<String>	variablesToPrint;

	public ModuleState(Map<String, Model> models, String bioModel, String submodelID)
	{
		this.model = bioModel;
		this.ID = submodelID;
		setCountVariables(models.get(getModel()));
		minPropensity = Double.MAX_VALUE / 10.0;
		maxPropensity = Double.MIN_VALUE / 10.0;
		noConstraintsFlag = true;
		noRuleFlag = true;
		noEventsFlag = true;
		variablesToPrint = new HashSet<String>();
	}

	public ModuleState(ModuleState state)
	{
		this.ID = state.ID;
		this.model = state.model;
		this.minPropensity = state.minPropensity;
		this.maxPropensity = state.maxPropensity;
		this.noConstraintsFlag = state.noConstraintsFlag;
		this.noRuleFlag = state.noRuleFlag;
		this.noEventsFlag = state.noEventsFlag;
		this.variablesToPrint = state.variablesToPrint;
		this.numSpecies = state.numSpecies;
		this.numParameters = state.numParameters;
		this.numReactions = state.numReactions;
		this.numInitialAssignments = state.numInitialAssignments;
		this.numEvents = state.numEvents;
		this.numRules = state.numRules;
		this.numConstraints = state.numConstraints;
		this.numCompartments = state.numCompartments;
	}

	public String getID()
	{
		return ID;
	}

	public double getMaxPropensity()
	{
		return maxPropensity;
	}

	public double getMinPropensity()
	{
		return minPropensity;
	}

	public String getModel()
	{
		return model;
	}

	public long getNumCompartments()
	{
		return numCompartments;
	}

	public long getNumConstraints()
	{
		return numConstraints;
	}

	public long getNumEvents()
	{
		return numEvents;
	}

	public int getNumInitialAssignments()
	{
		return numInitialAssignments;
	}

	public long getNumParameters()
	{
		return numParameters;
	}

	public int getNumRateRules()
	{
		return numRateRules;
	}

	public long getNumReactions()
	{
		return numReactions;
	}

	public long getNumRules()
	{
		return numRules;
	}

	public long getNumSpecies()
	{
		return numSpecies;
	}

	public double getPropensity()
	{
		return propensity;
	}

	public HashSet<String> getVariablesToPrint()
	{
		return variablesToPrint;
	}

	public boolean isNoConstraintsFlag()
	{
		return noConstraintsFlag;
	}

	public boolean isNoEventsFlag()
	{
		return noEventsFlag;
	}

	public boolean isNoRuleFlag()
	{
		return noRuleFlag;
	}

	public void setCountVariables(Model model)
	{
		this.numSpecies = model.getSpeciesCount();
		this.numParameters = model.getParameterCount();
		this.numReactions = model.getReactionCount();
		this.numInitialAssignments = model.getInitialAssignmentCount();
		this.numEvents = model.getEventCount();
		this.numRules = model.getRuleCount();
		this.numConstraints = model.getConstraintCount();
		this.numCompartments = model.getCompartmentCount();
	}

	public void setID(String iD)
	{
		ID = iD;
	}

	public void setMaxPropensity(double maxPropensity)
	{
		this.maxPropensity = maxPropensity;
	}

	public void setMinPropensity(double minPropensity)
	{
		this.minPropensity = minPropensity;
	}

	public void setNoConstraintsFlag(boolean noConstraintsFlag)
	{
		this.noConstraintsFlag = noConstraintsFlag;
	}

	public void setNoEventsFlag(boolean noEventsFlag)
	{
		this.noEventsFlag = noEventsFlag;
	}

	public void setNoRuleFlag(boolean noRuleFlag)
	{
		this.noRuleFlag = noRuleFlag;
	}

	public void setNumCompartments(long numCompartments)
	{
		this.numCompartments = numCompartments;
	}

	public void setNumConstraints(long numConstraints)
	{
		this.numConstraints = numConstraints;
	}

	public void setNumEvents(long numEvents)
	{
		this.numEvents = numEvents;
	}

	public void setNumInitialAssignments(int numInitialAssignments)
	{
		this.numInitialAssignments = numInitialAssignments;
	}

	public void setNumParameters(long numParameters)
	{
		this.numParameters = numParameters;
	}

	public void setNumRateRules(int numRateRules)
	{
		this.numRateRules = numRateRules;
	}

	public void setNumReactions(long numReactions)
	{
		this.numReactions = numReactions;
	}

	public void setNumRules(long numRules)
	{
		this.numRules = numRules;
	}

	public void setNumSpecies(long numSpecies)
	{
		this.numSpecies = numSpecies;
	}

	public void setPropensity(double propensity)
	{
		this.propensity = propensity;
	}

	public void setVariablesToPrint(HashSet<String> variablesToPrint)
	{
		this.variablesToPrint = variablesToPrint;
	}
}
