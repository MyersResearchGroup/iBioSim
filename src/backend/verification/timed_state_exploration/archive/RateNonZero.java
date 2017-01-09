package backend.verification.timed_state_exploration.archive;


/**
 * Continuous variables whose current rate is not zero.
 * @author Andrew N. Fisher
 *
 */
public class RateNonZero extends PreContinuous {

	/*
	 * Abstraction Function: This class represents a continuous
	 * variable that has a non-zero current rate. In this representation,
	 * super._range stores the cached range of rates.
	 */
	
	/*
	 * Representation Invariant:
	 * super._range -> current range of rates.
	 * 
	 */
	
	/**
	 * 
	 * @return
	 */
	public Interval getRangeOfRates(){
		return super.get_range();
	}
	
}
