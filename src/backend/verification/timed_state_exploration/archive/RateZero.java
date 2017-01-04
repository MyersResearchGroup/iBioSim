package backend.verification.timed_state_exploration.archive;


/**
 * Continuous variable whose current rate is zero.
 * @author Andrew N. Fisher
 *
 */
public class RateZero extends PreContinuous {
	
	
	public Interval getValues(){
		return super.get_range();
	}

}
