package backend.verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;

import backend.lpn.parser.LhpnFile;
import backend.lpn.parser.Variable;
import backend.verification.platu.lpn.DualHashMap;
import backend.verification.platu.stategraph.State;
import backend.verification.timed_state_exploration.octagon.Equivalence;

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
	public static int maxAdvance(Equivalence z, LPNTransitionPair contVar,
			State[] localStates){
		
		/*
		 * This method first extracts the list of continuous variables
		 * that contain contVar. These inequalities are contained in
		 * the Variable, so the first this is to extract that Variable
		 * followed by extracting the list.
		 * 
		 * Next the main work is done. The basic idea is to find how far
		 * the variable can be advanced without changing the truth value of 
		 * inequality. This is accomplished by seeing which inequalities put a constraint
		 * on how far the variable can be advance, then taking the minimum of all
		 * these constraints.
		 * 
		 * To determine whether the inequality does provide a constraint, the 
		 * algorithm determines whether the entire range of the variable lies below
		 * the constant, lies above the constant, or contains the constant. The third
		 * case is referred to as the 'straddle' case. Then it is determined if there is
		 * a constraint by taking into account three things: the specifically inequality
		 * in question ('<', '>'), the rate of the variable, and the current truth value of 
		 * the inequality.
		 * 
		 * In each of these cases is structured as an if-else block. The order is as follows.
		 * 
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
		 * The last bifurcation is whether the inequality is true or false. I 
		 * will refer to this bifurcation as i=1 or i=0, for true and false,
		 * respectively.
		 * 
		 * The method also checks for some types of inconsistencies To illustrate
		 * these checks, suppose I have the inequality x>3. The first check is 
		 * illustrated by supposing that the inequality is marked 'false' by
		 * the state and that the lower bound on x is 5. This leads to an
		 * inconsistency since the lower bound being 5 means the inequality would
		 * have to be true. For the second type of check, I will consider again the
		 * situation where x>3. Further I will suppose that the lower bound is 1
		 * and the upper bound is 5. This leads to an inconsistency since for
		 * part of the zone, the inequality is true and for the other part the
		 * inequality is false.
		 * 
		 * For the following, let zu be the DBM(0, i) entry where i is the 
		 * index of the continuous variable x. Also let zl be the DBM(i,0) entry
		 * where i is the index of the continuous variable x. Note that since
		 * the zone is warped, zu = (upper bound)/r and zl = -1*(lower bound)/r when
		 * r > 0. When r<0, the upper and lower bounds are swapped. Thus 
		 * zu = (lower bound)/r and zl = -1*(upper bound)/r.
		 * 
		 * Note : in the warped zone, raising the upper bound on a variable with
		 * r<0 corresponds to lowering the lower bound of the variable when the 
		 * variable is not warped.
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
		 * 					Since zl = -1* lower bound this inequality implies
		 * 					lower bound > a.
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
		 * 				}
		 * 
		 * 			i=0
		 * 				if(zu < a/r){
		 * 					zu < a/r implies that 
		 * 					upper bound < a.
		 * 					Again, lower bound < upper bound < a. So the values
		 * 					of x match marking the inequality as false. the 
		 * 					value of x can then continue to increase until it
		 * 					reaches the constant. Thus the new minimum is
		 * 					newMin = a/r
		 * 				}
		 * 				else if (-1 * zl > a/r){
		 * 					Since zl = -1*lower bound this inequality implies
		 * 					lower bound > a.
		 * 					In this case, a < lower bound < upper bound. Thus 
		 * 					the inequality is true but has been erroneously marked
		 * 					false. Again, the upper bound is larger than it should
		 * 					be since the inequality is false. So the variable should not
		 * 					be allowed to advance further than it has. So set the minimum to
		 * 					newMin = zu.
		 * 					This case should not happen.
		 * 				}
		 * 				else{
		 * 					This is again the straddle case. Since the inequality
		 * 					is false, the variable should not be allowed to advance
		 * 					any further. In this case atacs sets the value to
		 * 					newMin = zu.
		 * 				}
		 * 
		 * 		r<0 (Note these are warped space below here.)
		 * 			i=1
		 * 				if(zl < -1 * a/r){
		 * 					OK, in this case x > a, x is decreasing, and 
		 * 					the inequality is true. Further more recall that zl = -1*(upper bound)/r.
		 * 					So we have -1*(upper bound)/r < -1 * a/r which implies
		 * 					upper bound < a.
		 * 					So the inequality should be false and it isn't. We set the variables to it's
		 * 					current (warped) upper bound to stop it from advancing. So the new minimum is
		 * 					newMin = zu.
		 * 					This case should not happen.
		 * 				}
		 * 				else if(-1*zu > -1 *a/r){
		 * 					Since zu = (lower bound)/r, the inequality becomes
		 * 					lower bound > a.
		 * 					Thus the entire range of the variable is above a and since x is decreasing, x
		 * 					is constrained by a. Thus set the new minimum to the (warped version) of a.
		 * 					newMin = a/r
		 * 				}
		 * 				else{
		 * 					This is again the straddle case and the new minimum is set to the current largest
		 * 					value to stop the variable from advancing. So
		 * 					newMin = zu.
		 * 				}
		 * 			i=0
		 * 				if(zl < -1 * a/r){
		 * 					Since r<0, zl = -1*(upper bound)/r. The inequality thus become
		 * 					-1*(upper bound)/r < -1*a/r which implies
		 * 					upper bound < a.
		 * 					The entire range of x lies below the inequality since 
		 * 					lower bound < upper bound < a. Since the variable is
		 * 					decreasing, it is unconstrained. The new minimum is then
		 * 					newMin = INFINITY.
		 * 				}
		 * 				else if (-1*zu > -1*a/r){
		 * 					Since r<0, zu = (lower bound)/r, the inequality becomes
		 * 					lower bound > a.
		 * 					Thus the entire range of the variable is above a. This
		 * 					means the inequality is true and has been erroneously 
		 * 					marked false so let's not let it advance any further.
		 * 					newMin = zu.
		 * 				}
		 * 				else{
		 * 					This is the straddle case. Since decreasing the variable does
		 * 					not change the inequality, the variable is left unconstrained.
		 * 					So set
		 * 					newMin = INFINITY;
		 * 				}
		 * x<a
		 * 		r>0
		 * 			i=1
		 * 				if(zu < a/r){
		 * 					Since zu = upper bound /r, the inequality is becomes
		 * 					upper bound < a.
		 * 					Thus the whole range of x is less than a, which is consistent with the 
		 * 					x<a being currently marked true. Now x is increasing so it is constrained by 
		 * 					(the warped value) of a. So the new minimum is 
		 * 					newMin = a/r
		 * 				}
		 * 				else if(-1*zl > a/r){
		 * 					Since zl = -1 * (lower bound)/r, the inequality becomes
		 * 					lower bound > a.
		 * 					Thus the entire range of x is above a which implies x<a is false. But it has
		 * 					been erroneously marked 'true'. Thus something has already gone wrong. In this
		 * 					case atacs let the variable continue as if it were unconstrained.
		 * 					newMin = INFINITY.
		 * 					This case should not happen.
		 * 				else{
		 * 					The straddle case again. Letting the variable increase past a changes the sign
		 * 					of the inequality, so don't let the variable advance any further. Set the 
		 * 					new minimum to
		 * 					newMin = zu.
		 * 				}
		 * 			i=0
		 * 				if(zu < a/r){
		 * 					Since zu = (upper bound)/r, this is the same as 
		 * 					upper bound < a.
		 * 					Thus the entire range is below the constant. So the inequality is true,
		 *                  but it is erroneously marked false. Constrain the variable from moving any further.
		 *                  newMIn = zu.
		 *                  This case should not happen.
		 *                  }
		 *                  
		 * 				else if (-1 * zl > a/r){
		 * 					Since zl = -1* lower bound, the inequality is
		 * 					lower bound > a.
		 * 					Thus the entire range lies above the constant. The inequality x<a is false and since
		 * 					x is increasing, the inequality will always remain false. So there is no constraInt.
		 * 					newMin = INFINITY.
		 * 				}
		 * 				else{
		 * 					This is the straddle case again. Allowing the inequality to decrease further
		 * 					does not change the inequality. So set the new minimum to
		 * 					newMin = INFINITY.
		 * 				}
		 * 		r<0 (Note these are the warped cases.)
		 * 			i=1
		 * 				if(zl < -1 *a/r){
		 * 					When r<0, zl = -1 * (upper bound)/r. Thus the inequality
		 * 					becomes -1*(upper bound)/r < -1*a/r. This implies that
		 * 					upper bound < a.
		 * 					So the entire range is less than 'a'. Since the variable
		 * 					is decreasing, it will always remain below 'a'. Thus
		 * 					the inequality does not constrain the variable. So
		 * 					the minimum value would be -INIFINITY; however, taking
		 * 					into account warping, the new minimum is
		 * 					newMin = INFINITY.
		 * 					Another way to look at it is a decreasing variable 
		 * 					corresponds to an increasing variable in the warped
		 * 					space since we are dividing by a negative value.
		 * 				}
		 * 				else if (-1*zu > -1*a/r){
		 * 					When the rate is negative, zu=(lower bound)/r. Thus the
		 * 					inequality becomes -1*(lower bound)/r > -1*a/r or
		 * 					lower bound > a.
		 * 					So the entire range is greater than the constant. But
		 * 					then the inequality is false and has been erroneously
		 * 					marked 'true'. The value has already advanced too far.
		 * 					newMin = zu.
		 * 					This case should not happen.
		 * 				}
		 * 				else{
		 * 					This is the straddle case. Letting the inequality continue to
		 * 					decrease does not change the sign of the inequality, so 
		 * 					we do not constrain the variable. Thus set the new minimum to
		 * 					newMin = INFINITY.
		 * 				}
		 * 			i=0
		 * 				if(zl < -1 * a/r){
		 * 					Again when the rate is negative, zl=-1*(upper bound)/r.
		 * 					So the inequality becomes -1*(upper bound)/r < -1 a/r or
		 * 					upper bound < a.
		 * 					So the entire range of the variable is below 'a' which
		 * 					makes the inequality true; however, it has been 
		 * 					erroneously marked false. So freeze the variable at
		 * 					its current largest value
		 * 					newMin = zu.
		 * 					This case should not happen.
		 * 				}
		 * 				else if(-1*zu > -1 * a/r){
		 * 					When the rate is negative, zu = (lower bound)/r. The 
		 * 					inequality then becomes -1*(lower bound)/r > -1 * a/r or
		 * 					lower bound > a.
		 * 					So the entire range of the continuous variable is
		 * 					above the constant 'a'. Thus the variable can decrease
		 * 					until it reaches the constant. When we divide by the 
		 * 					a negative rate, this corresponds to increasing to 
		 * 					a/r. Thus the new minimum value is
		 * 					newMin = a/r.
		 * 				}
		 * 				else{
		 * 					This is the final straddle case. Since the variable decreasing
		 * 					changes the sign the variable, the inequality constrains the variable.
		 * 					So freeze the variable at
		 * 					newMin = zu.
		 * 				}
		 */
				
		
		// Get the continuous variable in question.
		int lpnIndex = contVar.get_lpnIndex();
		int varIndex = contVar.get_transitionIndex();

		Variable variable = z.get_lpnList()[lpnIndex].getContVar(varIndex);
		
		// Get the zone index of the continuous variable.
		int contDBMIndex = z.timerIndexToDBMIndex(contVar);

		// Initially set the value time to advance at INFINITY. This will
		// be lowered if any inequalities force a lower value.
		int min = Zone.INFINITY;
		int newMin = Zone.INFINITY;

		// Get all the inequalities that reference the variable of interest.
		ArrayList<InequalityVariable> inequalities = variable.getInequalities();

		if(inequalities == null){
			return Zone.INFINITY;
		}
		
		for(InequalityVariable ineq : inequalities){
			
			// Update the inequality variable.
			//int ineqValue = ineq.evaluate(localStates[varIndex], z);
			
			// Get the current value of the inequality.
			int ineqValue = localStates[ineq.get_lpn().getLpnIndex()].getCurrentValue(ineq.get_index());


			/* Working on a > or >= ineq */
			if(ineq.get_op().equals(">") || ineq.get_op().equals(">=")){

				// If the rate is positive.
				if(z.getCurrentRate(contVar) > 0){

					// If the inequality is marked true.
					if(ineqValue != 0){
						
						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(0, contDBMIndex)
						if(z.getUpperBoundTrue(contDBMIndex)
								< chkDiv(ineq.getConstant(), z.getCurrentRate(contVar), false)){
//							newMin = z.getDbmEntry(0, contVar.get_transitionIndex());
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("maxAdvance: Impossible case 1.");
						}

						// Check if the entire range lies above the constant.
						//*else if ((-1)*z.getDbmEntry(contDBMIndex,0)
						else if ((-1)*z.getLowerBoundTrue(contDBMIndex)
								> chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							newMin = Zone.INFINITY;

						}

						else{
							// Straddle case
							newMin = Zone.INFINITY;
						}
					}
					else{
						// The inequality is marked false.
						
						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(0, contDBMIndex)
						if(z.getUpperBoundTrue(contDBMIndex)
								< chkDiv(ineq.getConstant(), z.getCurrentRate(contVar), false)){
							
							newMin = chkDiv(ineq.getConstant(), 
									z.getCurrentRate(contVar), false);
						}
						
						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(contDBMIndex, 0)
						else if((-1)*z.getLowerBoundTrue(contDBMIndex)
								> chkDiv(ineq.getConstant(), z.getCurrentRate(contVar), false)){
							
//							newMin = z.getDbmEntry(0, contVar.get_transitionIndex());
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.print("maxAdvance : Impossible case 3.");
						}

						else{
							// straddle case
							//*newMin = z.getDbmEntry(0,contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
						}
					}
				}

				else{
					// The rate is negative.
					// warp <= 0.
					
					if( ineqValue != 0){
						// The inequality is marked true.

						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(contDBMIndex,0)
						if(z.getLowerBoundTrue(contDBMIndex)
								< (-1)*chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							//*newMin = z.getDbmEntry(0,contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("Warning: my impossible case 2.");
						}

						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(0, contDBMIndex)
						else if((-1)*z.getUpperBoundTrue(contDBMIndex)	
								> (-1)*chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							newMin = chkDiv(ineq.getConstant(), 
									z.getCurrentRate(contVar), false);
//							System.err.println("Warning: impossible case 8a found.");
						}

						else{
							// straddle case
//							newMin = z.getDbmEntry(0, contVar.get_transitionIndex());
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
						}

					}
					
					else{
						// The inequality is marked false.
						
						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(contDBMIndex,0)
						if(z.getLowerBoundTrue(contDBMIndex)
								< (-1)*chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							newMin = Zone.INFINITY;
						}

						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(0, contDBMIndex)
						else if((-1)*z.getUpperBoundTrue(contDBMIndex)
								> (-1)*chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							//*newMin = z.getDbmEntry(0,contDBMIndex);
							//int tmp = z.getUpperBoundTrue(contDBMIndex);
							
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("maxAdvance : Impossible case 4.");
						}

						else{
							// straddle case
							newMin = Zone.INFINITY;
						}
					}
				}	
			}

			else{
				// Working on a < or <= ineq

				// Check if the rate is positive.
				//if(z.getUpperBoundForRate(contVar) > 0){
				if(z.getCurrentRate(contVar) > 0){

					if(ineqValue != 0){
						// The inequality is marked true.

						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(0, contDBMIndex)
						if(z.getUpperBoundTrue(contDBMIndex)
								< chkDiv(ineq.getConstant(), 
										z.getCurrentRate(contVar), false)){
							
							newMin = chkDiv(ineq.getConstant(),
									z.getCurrentRate(contVar), false);
						}

						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(contDBMIndex, 0)
						else if((-1)*z.getLowerBoundTrue(contDBMIndex)
								> chkDiv(ineq.getConstant(), z.getCurrentRate(contVar),false)){
							
							newMin = Zone.INFINITY;
							System.err.println("Warning : Impossible case 5.");
						}

						else{
							//straddle case
							//*newMin = z.getDbmEntry(0,contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
						}

					}

					else{
						// The inequality is marked false.
						
						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(0, contDBMIndex)
						if(z.getUpperBoundTrue(contDBMIndex)
								< chkDiv(ineq.getConstant(), 
										z.getCurrentRate(contVar), false)){
							
//							newMin = z.getDbmEntry(0, contVar.get_transitionIndex());
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("maxAdvance : Impossible case 7.");
						}

						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(contDBMIndex, 0)
						else if((-1)*z.getLowerBoundTrue(contDBMIndex)
								> chkDiv(ineq.getConstant(), 
										z.getCurrentRate(contVar), false)){
							
							newMin = Zone.INFINITY;
//							System.err.println("Warning: impossible case 8a found.");
						}

						else{
							// straddle case
							
							newMin = Zone.INFINITY;
						}
					}	

				}

				else {
					// The rate is negative.
					// warp <=0 

					if(ineqValue != 0){
						// The inequality is marked true.

						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(contDBMIndex, 0)
						if(z.getLowerBoundTrue(contDBMIndex)
								< (-1)*chkDiv(ineq.getConstant(), 
										z.getCurrentRate(contVar), false)){
							
							newMin = Zone.INFINITY;
						}
						
						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(0, contDBMIndex)
						else if ((-1)*z.getUpperBoundTrue(contDBMIndex)
								> (-1)*chkDiv(ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("Warning : Impossible case 8.");
						}


						else {
							// straddle case
							newMin = Zone.INFINITY;
						}

					}


					else {
						// The inequality is marked false.

						// Check if the entire range lies below the constant.
						//*if(z.getDbmEntry(contDBMIndex,0)
						if(z.getLowerBoundTrue(contDBMIndex)
								< chkDiv((-1)*ineq.getConstant(),
										z.getCurrentRate(contVar), false)){
							
							//*newMin = z.getDbmEntry(0,contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
							System.err.println("Warning : Impossible case 6");
						}

						// Check if the entire range lies above the constant.
						//*else if((-1)*z.getDbmEntry(0,contDBMIndex)
						else if((-1)*z.getUpperBoundTrue(contDBMIndex)
								> (-1)*chkDiv(ineq.getConstant(),
										(-1)*z.getCurrentRate(contVar),false)){
							
							newMin = chkDiv(ineq.getConstant(), z.getCurrentRate(contVar),false);
						}



						else {
							// straddle case
							
//							newMin = z.getDbmEntry(0, contVar.get_transitionIndex());
							//*newMin = z.getDbmEntry(0, contDBMIndex);
							newMin = z.getUpperBoundTrue(contDBMIndex);
						}


					}
				}	
			}
			// Check if the value can be lowered.
			if(newMin < min){
				min = newMin;
			}
		}


		return min;
	}
	
	/**
	 * Determines whether time has advanced far enough for an inequality to change
	 * truth value.
	 * @param ineq
	 * 		The inequality to test whether its truth value can change.
	 * @param localState
	 * 		The state associated with the inequality.
	 * @return
	 * 		True if the inequality can change truth value, false otherwise.
	 */
	public static boolean inequalityCanChange(Equivalence z, InequalityVariable ineq, State localState){
		
		/*
		 * The Inequality is assumed to be of the form 
		 * (expression with variable) inequality (expression evaluating to constant).
		 * Let the inequality be expression as x>a or x<a. (The case '<=' is
		 * considered the same as '>' and '>=' is considered the same as '>'.)
		 * An inequality can change sign in four ways. One way for both positive
		 * and negative rates for each type of inequality x>a and x<a.
		 * 
		 * For the following, let zu be the DBM(0, i) entry where i is the 
		 * index of the continuous variable x. Also let zl be the DBM(i,0) entry
		 * where i is the index of the continuous variable x. Note that since
		 * the zone is warped, zu = (upper bound)/r and zl = -1*(lower bound)/r when
		 * r > 0. When r<0, the upper and lower bounds are swapped. Thus 
		 * zu = (lower bound)/r and zl = -1*(upper bound)/r.
		 * 
		 * 
		 * x > a
		 * 		r < 0
		 * 			if(-1* zu < -1*a/r && inequality true){
		 * 				This case covers the situation where the variable x
		 * 				starts above the constant and decreases until it crosses
		 * 				the constant turnin the inequality from true to false.
		 * 				Since zu = -1*(lower bound)/r, the inequality becomes
		 * 				-1*(lower bound)/r < -1*a/r or
		 * 				lower bound < a.
		 * 				Since the lower bound exceeds the constant, the inequality
		 * 				can change.
		 * 			}
		 * 		r > 0
		 * 			if(zu > a/r && inequality false){
		 * 				This is the case where the variable x is below the constant
		 * 				and the variable then exceeds the constant.
		 * 				Since zu = (upper bound)/r the inequality becomes
		 * 				upper bound > a.
		 * 				Since the upper bound exceeds the constant, the inequality
		 * 				can change.
		 * 			}
		 * x < a
		 * 		r < 0
		 * 			if(-1*zu < -1*a/r && inequality false){
		 * 				This case again has the variable x above is again above the
		 * 				constant 'a' and is going to cross it, but this time the 
		 * 				the inequality becomes true when it was false.
		 * 				Since zu = -1*(lower bound)/r, the inequality becomes
		 * 				-1*(lower bound)/r < -1*a/r or
		 * 				lower bound < a.
		 * 				Since the lower bound exceeds the constant, the inequality
		 * 				can change.
		 * 			}
		 * 		r > 0
		 * 			if(zu > a/r && inequality true){
		 * 				This case has the variable x below the constant and the
		 * 				variable reaches the point where it will exceed the constant
		 * 				thus turing the truth value of the inequality from 
		 * 				true to false.
		 * 				Since zu = (upper bound)/r the inequality becomes
		 * 				upper bound > a.
		 * 				Since the upper bound exceeds the constant, the inequality
		 * 				can change.
		 * 			}
		 */
		
		// Find the index of the continuous variable this inequality refers to.
		// I'm assuming there is a single variable.
		LhpnFile lpn = ineq.get_lpn();
		Variable contVar = ineq.getContVariables().get(0);
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.getValue(contVar.getName());

		// Package up the information into a the index. Note the current rate doesn't matter.
		LPNContinuousPair index = new LPNContinuousPair(lpn.getLpnIndex(), contIndex, 0);

		// Get the current rate.
		int currentRate = z.getCurrentRate(index);

		// Get the current value of the inequality. This requires looking into the current state.
		int currentValue = localState.getCurrentValue(ineq.getName());
		
		// Get the Zone index of the variable.
		int zoneIndex = z.timerIndexToDBMIndex(index);

		// > or >=
		if(ineq.get_op().contains(">")){
			
			// First checking cases when the rate is negative.
			if(currentRate < 0 && currentValue != 0){
				
				// Inequality is x>a. This is the case
				// x lies above the and decreases below it making
				// the inequality turn from true to false.
				//if((-1) * z.getDbmEntry(0, zoneIndex) <=
				if((-1)*z.getUpperBoundTrue(zoneIndex) <=
						(-1)*chkDiv(ineq.getConstant(), currentRate, false)){	
					return true;
				}
				return false;
			}
			
			// Inequality is x>a. This is the case
			// x lies below the constant and rises until it exceeds
			// the constant thus causing the inequality to go from
			// false to true.
			else if(currentRate > 0 && currentValue == 0){
				//if(z.getDbmEntry(0, zoneIndex) >= 
				if(z.getUpperBoundTrue(zoneIndex) >=
						chkDiv(ineq.getConstant(), currentRate, false)){
					return true;
				}
				return false;
			}
		}
		/* < or <= */
		else if(ineq.get_op().contains("<")){
			
			// Inequality is x<a. This is the case where
			// the variable is above the constant and decreases until
			// it is below the constant thus causing the constant to
			// go from false to true.
			if(currentRate < 0 && currentValue == 0){
				//if((-1) * z.getDbmEntry(0, zoneIndex) <=
				if((-1)*z.getUpperBoundTrue(zoneIndex) <=
						(-1)*chkDiv(ineq.getConstant(), currentRate, false)){
					return true;
				}
				return false;
			}
			
			// Inequality is x<a. This is the case where the variable
			// is below the constant and rises until it exceeds the constant
			// thus changing the inequality from true to false.
			else if (currentRate > 0 && 
					currentValue != 0){
				//if(z.getDbmEntry(0, zoneIndex) >=
				if(z.getUpperBoundTrue(zoneIndex) >=
						chkDiv(ineq.getConstant(), currentRate, false)){
					return true;
				}
				return false;
			}
		
			else {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Performs a division of two integers and either takes the ceiling or the floor. Note :
	 * The integers are converted to doubles for the division so the choice of ceiling or floor is
	 * meaningful.
	 * @param top
	 * 		The numerator.
	 * @param bottom
	 * 		The denominator.
	 * @param ceil
	 * 		True indicates return the ceiling and false indicates return the floor.
	 * @return
	 * 		Returns the ceiling of top/bottom if ceil is true and the floor of top/bottom otherwise.
	 */
	public static int chkDiv(int top, int bottom, Boolean ceil){
		/*
		 * This method was taken from atacs/src/hpnrsg.c
		 */
		int res = 0;
		  if(top == Zone.INFINITY ||
		     top == Zone.INFINITY * -1) {
		    if(bottom < 0) {
		      return top * -1;
		    }
		    return top;
		  }
		  if(bottom == Zone.INFINITY) {
			  return 0;
		  }
		  if(bottom == 0) {
			  System.out.println("Warning: Divided by zero.");
			  bottom = 1;
		  }

		  double Dres,Dtop,Dbottom;
		  Dtop = top;
		  Dbottom = bottom;
		  Dres = Dtop/Dbottom;
		  if(ceil) {
			  res = (int)Math.ceil(Dres);
		  }
		  else if(!ceil) {
			  res = (int)Math.floor(Dres);
		  }
		  return res;
	}
	
	/**
	 * Updates the inequalities variables in the state. Note : this
	 * method will change the current state.
	 * @param z
	 * 		The zone containing information about the current continuous 
	 * 		variables.
	 * @param s
	 * 		The state to update the inequalities in.
	 */
	//*public static void updateInitialInequalities(Zone z, State s){
	public static void updateInitialInequalities(Equivalence z, State s){
		// Extract the LPN.
		LhpnFile lpn = s.getLpn();

		// Get the state vector to update.
//		int[] vector = s.getVector();

		// The variables are not stored in the state, so get them from the LPN.
		String[] variables = lpn.getVariables();

		// Find the inequality variables.
		for(int i=0; i<variables.length; i++){

			// A name starting with '$' indicates a name of an InequalityVariable.
			if(variables[i].startsWith("$")){

				// Get the variable for using its evaluator.
				InequalityVariable var = (InequalityVariable) lpn.getVariable(variables[i]);
				//vector[i] = var.evaluateInequality(s, this._zones[0]);

//				// Get the new value.
//				//vector[i] = var.evaluateInequality(s, _zones[0]).equals("true") ? 1 : 0;
//				vector[i] = var.evaluate(s, z);
				String value = var.evaluate(s, z) == 0 ? "false" : "true";
				var.addInitValue(value);
			}
		}

	}
	
	/**
	 * Updates the inequalities variables in the state. Note : this
	 * method will change the current state.
	 * @param z
	 * 		The zone containing information about the current continuous 
	 * 		variables.
	 * @param s
	 * 		The state to update the inequalities in.
	 */
	public static void updateInequalities(Zone z, State s){
		// Extract the LPN.
		LhpnFile lpn = s.getLpn();

		// Get the state vector to update.
		int[] vector = s.getVariableVector();

		// The variables are not stored in the state, so get them from the LPN.
		String[] variables = lpn.getVariables();

		// Find the inequality variables.
		for(int i=0; i<variables.length; i++){

			// A name starting with '$' indicates a name of an InequalityVariable.
			if(variables[i].startsWith("$")){

				// Get the variable for using its evaluator.
				InequalityVariable var = (InequalityVariable) lpn.getVariable(variables[i]);
				//vector[i] = var.evaluateInequality(s, this._zones[0]);

//				// Get the new value.
//				//vector[i] = var.evaluateInequality(s, _zones[0]).equals("true") ? 1 : 0;
//				vector[i] = var.evaluate(s, z);
				int value = var.evaluate(s, z);
				vector[i] = value;
			}
		}

	}
}
