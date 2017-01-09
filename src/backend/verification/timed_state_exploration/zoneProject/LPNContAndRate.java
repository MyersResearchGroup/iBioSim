package backend.verification.timed_state_exploration.zoneProject;

/**
 * An LPNContAndRate object is a pairing of an LPNcontinuousPair (for referencing)
 * and an IntervalPair (for storing the rate). The purpose of the object
 * is to store a variable with its associated rate.
 * 
 * @author Andrew N. Fisher
 *
 */
public class LPNContAndRate {

	// The continuous variables reference information.
	LPNContinuousPair _lcPair;
	
	// The possible rates for the variable.
	IntervalPair _rateInterval;

	/**
	 * Combines an LPNContinuousPair with the continuous variables range of 
	 * rates.
	 * @param _lcPair
	 * 		The LPNContinuousPair for the continuous variable.
	 * @param _rateInterval
	 * 		The range of possible rates.
	 */
	public LPNContAndRate(LPNContinuousPair _lcPair, IntervalPair _rateInterval) {
		this._lcPair = _lcPair;
		this._rateInterval = _rateInterval;
	}
	
	/**
	 * Constructs an LPNContAndRate with no set rate information. The intent is
	 * to set the rate later.
	 * @param _lcPair
	 * 		The reference for the variable.
	 */
	public LPNContAndRate(LPNContinuousPair _lcPair){
		this._lcPair = _lcPair;
		this._rateInterval = new IntervalPair();
	}

	/**
	 * Gets the LPNContinuousPair reference for this LPNContAndRate.
	 * @return
	 */
	public LPNContinuousPair get_lcPair() {
		return _lcPair;
	}

	
	/**
	 * Sets the LPNContinuousPair reference for this LPNContAndRate.
	 * @param _lcPair
	 */
	public void set_lcPair(LPNContinuousPair _lcPair) {
		this._lcPair = _lcPair;
	}

	/**
	 * Gets the rates.
	 * @return
	 * 		Returns an IntervalPair object that contains the upper and lower bounds
	 * 		for the rate.
	 */
	public IntervalPair get_rateInterval() {
		return _rateInterval;
	}

	/**
	 * Sets the rate interval.
	 * @param _rateInterval
	 */
	public void set_rateInterval(IntervalPair _rateInterval) {
		this._rateInterval = _rateInterval;
	}
	
	/**
	 * An LPNContAndRAte variable is equal to an LPNContinuouPair or another
	 * LPNContAndRate variable if they refer to the same variable as defined
	 * by the LPNContinuousPair portion of the LPNContAndRate variable. It is
	 * not equal to objects that are not one of these two types.
	 */
	@Override
	public boolean equals(Object other){
		
		if(other instanceof LPNContinuousPair){
			LPNContinuousPair otherLCPair = (LPNContinuousPair) other;
			return _lcPair.equals(otherLCPair);
		}
		else if(other instanceof LPNContAndRate){
			LPNContAndRate otherLCAR = (LPNContAndRate) other;
			return _lcPair.equals(otherLCAR._lcPair);
		}
		
		return false;
	}
	
	@Override
	public String toString(){
		return _lcPair.toString() + " " +_rateInterval.toString();
	}
	
	@Override
	public int hashCode(){
		return _lcPair.hashCode();
	}
}
