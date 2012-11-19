package verification.timed_state_exploration.zoneProject;

public class LPNContAndRate {

	LPNContinuousPair _lcPair;
	
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
	
	public LPNContAndRate(LPNContinuousPair _lcPair){
		this._lcPair = _lcPair;
		this._rateInterval = new IntervalPair();
	}

	public LPNContinuousPair get_lcPair() {
		return _lcPair;
	}

	public void set_lcPair(LPNContinuousPair _lcPair) {
		this._lcPair = _lcPair;
	}

	public IntervalPair get_rateInterval() {
		return _rateInterval;
	}

	public void set_rateInterval(IntervalPair _rateInterval) {
		this._rateInterval = _rateInterval;
	}
	
	public int hashcode(){
		return _lcPair.hashCode();
	}
	
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
	
	public String toString(){
		return _lcPair.toString() + " " +_rateInterval.toString();
	}
	
	public int hashCode(){
		return _lcPair.hashCode();
	}
}
