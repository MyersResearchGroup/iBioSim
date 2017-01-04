package backend.verification.timed_state_exploration.archive;

import backend.lpn.parser.LhpnFile;

/**
 * Base class for the member variables of the octagons.
 * 
 * @author Andrew N. Fisher
 *
 */
public abstract class OctMember {
	
	/*
	 * Abstraction Function: Gathers together common elements of the octagon
	 * variables timer and continuous variables.
	 */
	
	/*
	 *  The associated LPN for this variable.
	 */
	protected LhpnFile _lpn;
	
	/*
	 * The variable number taking into account all variables for the
	 * LPN.
	 */
	protected int _absReference;
	
	/*
	 *  An interval reference. Timers can use this for caching the delay.
	 *  Rate zero continuous variable can use this for caching the current
	 *  range of values. Non-rate zero variables can use this for caching
	 *  range of rates.
	 */
	protected Interval _range;

	
	/**
	 * Get the associated LPN.
	 * @return The associated LPN.
	 */
	public LhpnFile get_Lpn() {
		return _lpn;
	}

	/**
	 * Set the associated LPN.
	 * @param _lpn
	 */
	public void set_Lpn(LhpnFile _lpn) {
		this._lpn = _lpn;
	}

	/**
	 * Gets the number of the variable.
	 * @return The number of the variable.
	 */
	public int get_absReference() {
		return _absReference;
	}

	/**
	 * Sets the number of the variable.
	 * @param _absReference The number of the variable.
	 */
	public void set_absReference(int _absReference) {
		this._absReference = _absReference;
	}

	/**
	 * Gets the range.
	 * @return The range.
	 */
	public Interval get_range() {
		return _range;
	}

	/**
	 * Sets the range.
	 * @param _range The range.
	 */
	public void set_range(Interval _range) {
		this._range = _range;
	}
	
	
	
}
