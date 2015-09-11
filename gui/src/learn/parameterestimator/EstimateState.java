package learn.parameterestimator;

import java.util.Map;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.states.ArraysState;

public class EstimateState extends ArraysState
{

	public EstimateState(Map<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
	}

	@Override
	public double getVariableToValue(Map<String, Double> replacements, String variable)
	{
		return getVariableToValueMap().get(variable);
	}

	@Override
	public void setVariableToValue(Map<String, Double> replacements, String variable, double value)
	{
		this.getVariableToValueMap().put(variable, value);
	}
}