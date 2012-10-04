package verification.timed_state_exploration.zoneProject;

import verification.platu.stategraph.State;

/**
 * This class is meant as a utilities package. It contains static methods for
 * handling inequalities to separate some of the more complicated methods
 * from the zone.
 * 
 * @author Andrew N. Fisher
 *
 */
public class ContinuousUtilities {
	
	/*
	 * Abstraction Function : This class is merely a collection of static methods
	 * so there is no abstraction function.
	 */

	
	/*
	 * Representation Invariant : This class contains only static methods,
	 * so it should not contain state, that is have member fields, and 
	 * should not be instantiated.
	 */
	
	
	/**
	 * Determines the maximum amount time can advance for a given continuous
	 *  variable before an inequality must change truth value.
	 *   Note: It is assumed that the inequality is of the form 
	 * 'continuous variable' operator 'constant'.
	 * @param z
	 * 		The Zone containing the continuous variable.
	 * @param contVar
	 * 		The continuous variable whose upper bound on time will be determined.
	 * @param localStates
	 * 		The current state.
	 * @return
	 * 		The maximum that contVar can be set to before an inequality
	 * 		must change truth value.
	 */
	public static int maxAdvance(Zone z, LPNTransitionPair contVar,
			State[] localStates){
		
		/*
		 * This method first extracts the list of continuous variables
		 * that contain contVar. These inequalities are contained in
		 * the Variable, so the first this is to extract that Variable
		 * followed by extracting the list.
		 * 
		 * Next the main work is done. The checks occur in several stages.
		 * The first bifurcation is whether the inequality is 
		 * 'variable' < 'constant' or 'variable' > 'constant' ('<=' is treated
		 * in the same case as '<' and '>=' is treated in the same case as '>').
		 * For simplicity I will write the first case as 'x<a' and the second
		 * as 'x>a'.
		 * 
		 * The next bifurcation is whether the current rate of the variable is
		 * is positive or negative. The zero case will be consider if control falls
		 * all the way through the method. I will refer to these cases as 
		 * 'r>0', 'r<0' and 'r=0', respectively.
		 * 
		 * The method also checks for two types of inconsistencies pertaining
		 * to the relationship of the lower bound and the inequality. 
		 * 
		 * when x>a
		 * 		
		 * 
		 */
				
		return 0;
	}
}
