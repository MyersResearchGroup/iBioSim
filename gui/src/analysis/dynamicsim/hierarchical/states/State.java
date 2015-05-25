package analysis.dynamicsim.hierarchical.states;

import java.util.Map;

public interface State
{
	public abstract double getVariableToValue(Map<String, Double> replacements, String variable);

	public abstract void setVariableToValue(Map<String, Double> replacements, String variable, double value);

}
