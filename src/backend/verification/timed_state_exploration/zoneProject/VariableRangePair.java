package backend.verification.timed_state_exploration.zoneProject;

import backend.lpn.parser.Variable;

/**
 * A Variable and Range class packages an lpn.parser.Variable with an
 * IntervalPair. This allows one to store the Variable along with its range.
 * @author Andrew N. Fisher
 *
 */
public class VariableRangePair {

	// The variable to store.
	private Variable _variable;
	
	
	// The range of the variable.
	private IntervalPair _range;

	
	
	public VariableRangePair(Variable _variable, IntervalPair _range) {
		super();
		this._variable = _variable;
		this._range = _range;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_variable == null) ? 0 : _variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VariableRangePair))
			return false;
		VariableRangePair other = (VariableRangePair) obj;
		if (_variable == null) {
			if (other._variable != null)
				return false;
		} else if (!_variable.equals(other._variable))
			return false;
		return true;
	}



	public Variable get_variable() {
		return _variable;
	}

	public void set_variable(Variable _variable) {
		this._variable = _variable;
	}

	public IntervalPair get_range() {
		return _range;
	}

	public void set_range(IntervalPair _range) {
		this._range = _range;
	}
	
	@Override
	public String toString(){
		return "" + _variable + " = " + _range ;
	}
}
