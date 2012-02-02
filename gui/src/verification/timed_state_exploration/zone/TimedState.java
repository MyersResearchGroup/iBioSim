package verification.timed_state_exploration.zone;

import lpn.parser.LhpnFile;
import verification.platu.stategraph.State;

/**
 * Adds timing to a State.
 * 
 * @author Andrew N. Fisher
 *
 */
public class TimedState extends State{
	
	/* A Zone for keeping track timing information. */
	Zone _zone;

	
	public TimedState(LhpnFile lpn, int[] new_marking, int[] new_vector,
			boolean[] new_isTranEnabled) {
		super(lpn, new_marking, new_vector, new_isTranEnabled);
		// TODO Auto-generated constructor stub
	}

	public TimedState(State other) {
		super(other);
		// TODO Auto-generated constructor stub
	}

}
