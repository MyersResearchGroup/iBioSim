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
		 * The next bifurcation is whether the inequality is true or false. I 
		 * will refer to this bifurcation as i=1 or i=0, for true and false,
		 * respectively.
		 * 
		 * And finally, the last question is what the upper bound is in relation
		 * to the constant. This will determine how far the upper bound can be
		 * set. These will be stated explicitly.
		 * 
		 * The method also checks for some types of inconsistencies To illustrate
		 * these checks, suppose I have the inequality x>3. The first check is 
		 * illustrated by supposing that the inequality is marked 'false' by
		 * the state and that the lower bound on x is 5. This leads to an
		 * inconsistency since the lower bound being 5 means the inequality would
		 * have to be true. For the second type of check, I will consider again the
		 * situation where x>3. Further I will suppose that the lower bound is 1
		 * and the upper bound is 5. This is the so called 'straddle' case
		 * listed in the atacs code. This leads to an inconsistency since for
		 * part of the zone, the inequality is true and for the other part the
		 * inequality is false.
		 * 
		 * For the following, let zu be the DBM(0, i) entry where i is the 
		 * index of the continuous variable x. Also let zl be the DBM(i,0) entry
		 * where i is the index of the continuous variable x.
		 * 
		 * x>a
		 * 		r > 0
		 * 			i=1
		 * 				if(zu < a/r){
		 * 					zu < a/r implies (upper bound)/r < a/r. Thus
		 * 					upper bound < a.
		 * 					The inequality has been erroneously marked true since
		 * 					lower bound < upper bound < a. To match atacs let
		 * 					newMin = upper bound.
		 * 					It doesn't matter what I set this value to since
		 * 					this cas should never happen. in these cases, atacs
		 * 					tends to take the path of setting the value to what it
		 * 					is already.
		 * 					This case should not happen.
		 * 				}
		 * 				else if (-1 * zl > a/r){
		 * 					
		 * 					Since the lower bound is greater than the constant
		 * 					every value in the range of x is greater than the 
		 * 					constant. With the rate positive, x will be able
		 * 					to continue to increase without the inequality changing.
		 * 					Thus the inequality puts no constraints on the variable
		 * 					and the upper bound can be anything (bigger than the lower
		 * 					bound of course). So the new minimum is 
		 * 					newMin = INFINITY
		 * 				}
		 * 				else{
		 * 					This is the straddle case. Since the upper bound of
		 * 					x is greater than a, it can be increase with out bound
		 * 					and the inequality would still be true (as far as the
		 * 					portion of the Zone above a is concerned). The upper
		 * 					bound is still unconstrained, so set the minimum to 
		 * 					newMin = INFINITY.
		 * 					This case should not happen.
		 * 				}
		 * 
		 * 			i=0
		 * 				if(upper bound/r < a/r){
		 * 					Again, lower bound < upper bound < a. So the values
		 * 					of x match marking the inequality as false. the 
		 * 					value of x can then continue to increase until it
		 * 					reaches the constant. Thus the new minimum is
		 * 					newMin = a
		 * 				}
		 * 				else if (lower bound/r > a/r){
		 * 					In this case, a < lower bound < upper bound. Thus 
		 * 					the inequality is true but has been erroneously marked
		 * 					false. Again, the upper bound is larger than it should
		 * 					be since the inequality is false. So set the minimum to
		 * 					newMin = upperBound.
		 * 					This case should not happen.
		 * 				}
		 * 				else{
		 * 					This is again the straddle case. In this case atacs
		 * 					sets the value to
		 * 					newMin = upper bound.
		 * 					Again, it doesn't matter since
		 * 					This case should not happen.
		 * 				}
		 * 
		 * 		r<0 (Note these are warped space below here.)
		 * 			i=1
		 * 				if(-1 * lower bound/r < -1 * a/r){
		 * 					OK, in this case x > a, x is decreasing, and 
		 * 					the inequality is true.
		 * 					Since the rate is negative, the test inequality implies
		 * 					lower bound < a.
		 * 				}
		 */
				
		return 0;
	}
}
