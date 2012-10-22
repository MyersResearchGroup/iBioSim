package verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import lpn.parser.Variable;

import verification.platu.lpn.DualHashMap;
import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.State;


/**
 * This class is for storing and manipulating timing zones via difference bound matrices.
 * The underlying structure is backed by a two dimensional array. A difference bound 
 * matrix has the form
 *     t0  t1  t2  t3
 * t0 m00 m01 m02 m03
 * t1 m10 m11 m12 m13
 * t2 m20 m21 m22 m23
 * t3 m30 m31 m32 m33
 * where  tj - ti<= mij. In particular, m0k is an upper bound for tk and -mk is a lower
 * bound for tk.
 * 
 * The timers are referred to by an index. 
 * 
 * This class also contains a public nested class DiagonalNonZeroException which extends
 * java.lang.RuntimeException. This exception may be thrown if the diagonal entries of a 
 * zone become nonzero.
 * 
 * @author Andrew N. Fisher
 *
 */
public class Zone{
	
	/*Abstraction Function : 
	* The difference bound matrix is represented by int[][].
	*
	* In order to keep track of the upper and lower bounds of timers from when they are first
	* enabled, the matrix will be augmented by a row and a column. The first row will contain
	* the upper bounds and the first column will contain the negative of the lower bounds.
	* For one timer t1 that is between 2 and 3, we might have
	*    lb t0 t1
	* ub  x  0  3
	* t0  0  m  m
	* t1  -2 m  m
	* where x is not important (and will be given a zero value), 3 is the upper bound on t1
	* and -2 is the negative of the lower bound. The m values represent the actual difference
	* bound matrix. They are stored according to tj-ti <= mij.
	* Also note that the column heading are not part of the stored representation
	* lb stands for lower bound while ub stands for upper bound.
	* This upper and lower bound information is called the Delay for a Transition object.
	* Since a timer is tied directly to a Transition, the timers are index by the corresponding
	* Transition's index in a LPNTranslator.
	* The timers are named by an integer referred to as the index. The _indexToTimer array
	* connects the index in the DBM sub-matrix to the index of the timer. It does this
	* via the use of LPNTransitionPair objects. These objects store the index of the LPN
	* the transition is from as well as the index of the transition in that LPN.
	* 
	* The zone also stores the continuous variables. Continuous variables are
	* referenced by an LPNContinuousPair that stores the index of the LPN
	* that the continuous variable is apart of as well as the index of the 
	* continuous variable. The current rate of the continuous variable is
	* also stored in the LPNContinuousPair. When the rate of the continuous
	* variable is zero, it is stored in the _rateZeroContinuous member field along
	* with a VariableRangePair that gives the range of the continuous variable.
	* When the rate is nonzero, the continuous variable is store in the DBM.
	* A continuous variable is stored in the zone in the same way. The current
	* upper bounds are stored in the zeroth row and the negative of the current
	* lower bounds are stored in the zeroth column of the DBM. The upper and
	* lower bounds of the rate are stored in the ub and lb columns (where the 
	* upper and lower bounds of the timers are stored).
	*/ 
	
	/* Representation invariant :
	* Zones are immutable.
	* Integer.MAX_VALUE is used to logically represent infinity.
	* The lb and ub values for a timer should be set when the timer is enabled.
	* A negative hash code indicates that the hash code has not been set.
	* The index of the timer in _indexToTimer is the index in the DBM and should contain
	* 	the zeroth timer.
	* The array _indexToTimerPair should always be sorted.
	* The index of the LPN should match where it is in the _lpnList, that is, if lpn is
	* 	and LhpnFile object in _lpnList, then _lpnList[getLpnIndex()] == lpn.
	* The LPNTransitionPair in the _indexToTimer array should be an LPNContinuousPair
	*   when it stores the index to a continuous variable. Testing that the index
	*   is an LPNContinuousPair is used to determined if the indexing object points
	*   to a continuous variable.
	* 
	*/
	
	/*
	 * Resource List :
	 * TODO : Create a list reference where the algorithms can be found that this class
	 * depends on.
	 */
	
	public static final int INFINITY = Integer.MAX_VALUE;
	
	/* The lower and upper bounds of the times as well as the dbm. */
	private int[][] _matrix;
	
	/* Maps the index to the timer. The index is row/column of the DBM sub-matrix.
	 * Logically the zero timer is given index -1.
	 *  */
	//private int[] _indexToTimer;
	
	private LPNTransitionPair[] _indexToTimerPair;
	
	/* The hash code. */
	private int _hashCode;
	
	/* A lexicon between a transitions index and its name. */
	//private static HashMap<Integer, Transition> _indexToTransition;
	
	/* Set if a failure in the testSplit method has fired already. */
	//private static boolean _FAILURE = false;
	
	/* Hack to pass a parameter to the equals method though a variable */
	//private boolean subsetting = false;
	
	/* Stores the continuous variables that have rate zero */
//	HashMap<LPNTransitionPair, Variable> _rateZeroContinuous;
	//DualHashMap<RangeAndPairing, Variable> _rateZeroContinuous;
	DualHashMap<LPNTransitionPair, VariableRangePair> _rateZeroContinuous;
	
	/* Records the largest zone that occurs. */
	public static int ZoneSize = 0;
	
	private void checkZoneMaxSize(){
		if(dbmSize() > ZoneSize){
			ZoneSize = dbmSize();
		}
	}
	
	private LhpnFile[] _lpnList;
	
	/* 
	 * Turns on and off subsets for the zones.
	 * True means subset will be considered.
	 * False means subsets will not be considered.
	 */
	private static boolean _subsetFlag = true;
	
	/* 
	 * Turns on and off supersets for zones.
	 * True means that supersets will be considered.
	 * False means that supersets will not be considered.
	 */
	private static boolean _supersetFlag = true;
	
	
	/**
	 * Gets the value of the subset flag.
	 * @return
	 * 		True if subsets are requested, false otherwise.
	 */
	public static boolean getSubsetFlag(){
		return _subsetFlag;
	}
	
	/**
	 * Sets the value of the subset flag.
	 * @param useSubsets
	 * 			The value for the subset flag. Set to true if
	 * 			supersets are to be considered, false otherwise.
	 */
	public static void setSubsetFlag(boolean useSubsets){
		_subsetFlag = useSubsets;
	}
	
	/**
	 * Gets the value of the superset flag.
	 * @return
	 * 		True if supersets are to be considered, false otherwise.
	 */
	public static boolean getSupersetFlag(){
		return _supersetFlag;
	}
	
	/**
	 * Sets the superset flag.
	 * @param useSupersets
	 * 		The value of the superset flag. Set to true if
	 * 		supersets are to be considered, false otherwise.
	 */
	public static void setSupersetFlag(boolean useSupersets){
		_supersetFlag = useSupersets;
	}
	
	/**
	 * Construct a zone that has the given timers.
	 * @param timers 
	 * 				The ith index of the array is the index of the timer. For example,
	 * 					if timers = [1, 3, 5], then the zeroth row/column of the DBM is the
	 * 					timer of the transition with index 1, the first row/column of the 
	 * 					DBM is the timer of the transition with index 3, and the 2nd 
	 * 					row/column is the timer of the transition with index 5. Do not
	 * 					include the zero timer.
	 * @param matrix 
	 * 				The DBM augmented with the lower and upper bounds of the delays for the
	 * 					transitions. For example, suppose a zone has timers [1, 3, 5] (as
	 * 					described in the timers parameters). The delay for transition 1 is
	 * 					[1, 3], the delay for transition 3 is [2,5], and the delay for
	 * 					transition 5 is [4,6]. Also suppose the DBM is
	 * 					    t0 t1 t3 t5
	 * 					t0 | 0, 3, 3, 3 |
	 * 					t1 | 0, 0, 0, 0 |
	 * 					t3 | 0, 0, 0, 0 |
	 * 					t5 | 0, 0, 0, 0 |
	 * 					Then the matrix that should be passed is
	 * 					   lb t0 t1 t3 t5
	 * 					ub| 0, 0, 3, 5, 6|
	 * 					t0| 0, 0, 3, 3, 3|
	 * 					t1|-1, 0, 0, 0, 0|
	 * 					t3|-2, 0, 0, 0, 0|
	 * 					t5|-4, 0, 0, 0, 0|
	 * 					The matrix should be non-null and the zero timer should always be the
	 * 					first timer, even when there are no other timers.
	 */
	public Zone(int[] timers, int[][] matrix)
	{
		// A negative number indicates that the hash code has not been set.
		_hashCode = -1;
		
		// Make a copy to reorder the timers.
//		_indexToTimer = Arrays.copyOf(timers, timers.length);
		
		// Make a copy to reorder the timers.
		_indexToTimerPair = new LPNTransitionPair[timers.length];
		for(int i=0; i<timers.length; i++){
//			_indexToTimerPair[i] = new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN,
//					timers[i], true);
			_indexToTimerPair[i] = new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN,
					timers[i]);
		}
		
		// Sorting the array.
//		Arrays.sort(_indexToTimer);
		
		// Sorting the array.
		Arrays.sort(_indexToTimerPair);
		
		//if(_indexToTimer[0] != 0)
//		if(_indexToTimer[0] != -1)
//		{
//			// Add the zeroth timer.
//			int[] newIndexToTimer = new int[_indexToTimer.length+1];
//			for(int i=0; i<_indexToTimer.length; i++)
//			{
//				newIndexToTimer[i+1] = _indexToTimer[i];
//			}
//			
//			_indexToTimer = newIndexToTimer;
//			_indexToTimer[0] = -1;
//		}
		
		if(_indexToTimerPair[0].get_transitionIndex() != -1){
			// Add the zeroth timer.
			LPNTransitionPair[] newIndexToTimerPair = 
					new LPNTransitionPair[_indexToTimerPair.length];
			for(int i=0; i<_indexToTimerPair.length; i++){
				newIndexToTimerPair[i+1] = _indexToTimerPair[i];
			}
			
			_indexToTimerPair = newIndexToTimerPair;
//			_indexToTimerPair[0] = new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN, -1, true);
			_indexToTimerPair[0] = new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN, -1);
		}
		
//		if(_indexToTimer[0] < 0)
//		{
//			throw new IllegalArgumentException("Timers must be non negative.");
//		}
//		// Add a zero timer.
//		else if(_indexToTimer[0] > 0)
//		{
//			int[] newTimerIndex = new int[_indexToTimer.length+1];
//			for(int i=0; i<_indexToTimer.length; i++)
//			{
//				newTimerIndex[i+1] = _indexToTimer[i];
//			}
//		}
		
		// Map the old index of the timer to the new index of the timer.
		HashMap<Integer, Integer> newIndex = new HashMap<Integer, Integer>();
		
		// For the old index, find the new index.
		for(int i=0; i<timers.length; i++)
		{
			// Since the zeroth timer is not included in the timers passed
			// to the index in the DBM is 1 more than the index of the timer
			// in the timers array.
			//newIndex.put(i+1, Arrays.binarySearch(_indexToTimer, timers[i]));
//			LPNTransitionPair searchValue =
//					new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN, timers[i], true);
			LPNTransitionPair searchValue =
					new LPNTransitionPair(LPNTransitionPair.SINGLE_LPN, timers[i]);
			newIndex.put(i+1, Arrays.binarySearch(_indexToTimerPair, searchValue));
		}
		
		// Add the zero timer index.
		newIndex.put(0, 0);
		
		// Initialize the matrix.
		_matrix = new int[matrixSize()][matrixSize()];

		// Copy the DBM
		for(int i=0; i<dbmSize(); i++)
		{
			for(int j=0; j<dbmSize(); j++)
			{
				// Copy the passed in matrix to _matrix.
				setDbmEntry(newIndex.get(i), newIndex.get(j), 
						matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)]);
				// In the above, changed setDBMIndex to setdbm
			}
		}
		
		// Copy in the upper and lower bounds. The zero time does not have an upper or lower bound
		// so the index starts at i=1, the first non-zero timer.
		for(int i=1; i< dbmSize(); i++)
		{
			setUpperBoundbydbmIndex(newIndex.get(i), matrix[0][dbmIndexToMatrixIndex(i)]);
			
			// Note : The method setLowerBoundbydbmIndex, takes the value of the lower bound
			// and the matrix stores the negative of the lower bound. So the matrix value
			// must be multiplied by -1.
			setLowerBoundbydbmIndex(newIndex.get(i), -1*matrix[dbmIndexToMatrixIndex(i)][0]);
		}
		
		recononicalize();
	}
	
	/**
	 * Initializes a zone according to the markings of state.
	 * @param currentState
	 * 			The zone is initialized as if all enabled timers 
	 * 			have just been enabled.
	 */
	public Zone(State initialState)
	{
		// Extract the associated LPN.
		LhpnFile lpn = initialState.getLpn();
		
		int LPNIndex = lpn.getLpnIndex();
		
		if(_lpnList == null){
			// If no LPN exists yet, create it and put lpn in it.
			_lpnList = new LhpnFile[LPNIndex+1];
			_lpnList[LPNIndex] = lpn;
		}
		else if(_lpnList.length <= LPNIndex){
			// The list does not contain the lpn.
			
			LhpnFile[] tmpList = _lpnList;
			
			_lpnList = new LhpnFile[LPNIndex+1];
			_lpnList[LPNIndex] = lpn;
			
			// Copy any that exist already.
			for(int i=0; i<_lpnList.length; i++){
				_lpnList[i] = tmpList[i];
			}
		}
		else if(_lpnList[LPNIndex] != lpn){
			// This checks that the appropriate lpn is in the right spot.
			// If not (which gets you in this block), then this fixes it.
			_lpnList[LPNIndex] = lpn;
		}
		
		// Default value for the hash code indicating that the hash code has not
		// been set yet.
		_hashCode = -1;
		
		// Get the list of currently enabled Transitions by their index.
		boolean[] enabledTran = initialState.getTranVector();
		
		ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
				new ArrayList<LPNTransitionPair>();
		
//		LPNTransitionPair zeroPair = new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1, true);
		LPNTransitionPair zeroPair = new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1);
		
		// Add the zero timer first.
		enabledTransitionsArrayList.add(zeroPair);
		
		// The index of the boolean value corresponds to the index of the Transition.
		for(int i=0; i<enabledTran.length; i++){
			if(enabledTran[i]){
//				enabledTransitionsArrayList.add(new LPNTransitionPair(LPNIndex, i, true));
				enabledTransitionsArrayList.add(new LPNTransitionPair(LPNIndex, i));
			}
		}
		
		_indexToTimerPair = enabledTransitionsArrayList.toArray(new LPNTransitionPair[0]);
		
		_matrix = new int[matrixSize()][matrixSize()];
		
		for(int i=1; i<dbmSize(); i++)
		{
			// Get the name for the timer in the i-th column/row of DBM
			String tranName = 
					lpn.getTransition(_indexToTimerPair[i].get_transitionIndex()).getName();
			ExprTree delay = lpn.getDelayTree(tranName);
			
			// Get the values of the variables for evaluating the ExprTree.
			HashMap<String, String> varValues = 
				lpn.getAllVarsWithValuesAsString(initialState.getVector());
			
			// Set the upper and lower bound.
			int upper, lower;
			if(delay.getOp().equals("uniform"))
			{
				ExprTree lowerDelay = delay.getLeftChild();
				ExprTree upperDelay = delay.getRightChild();
				
				lower = (int) lowerDelay.evaluateExpr(varValues);
				upper = (int) upperDelay.evaluateExpr(varValues);
			}
			else
			{
				lower = (int) delay.evaluateExpr(varValues);
				
				upper = lower;
			}
			
			setLowerBoundbydbmIndex(i, lower);
			setUpperBoundbydbmIndex(i, upper);
		}
		
		// Advance the time and tighten the bounds.
		advance();
		recononicalize();
		
		checkZoneMaxSize();
	}
	
	/**
	 * Creates a Zone based on the local states.
	 * @param localStates
	 * 			The current state (or initial) of the LPNs.
	 */
	public Zone(State[] localStates){
		
		// Extract the local states.
		//State[] localStates = tps.toStateArray();
		
		// Initialize hash code to -1 (indicating nothing cached).
		_hashCode = -1;
		
		// Initialize the LPN list.
		initialize_lpnList(localStates);
		
		// Get the enabled transitions. This initializes the _indexTotimerPair
		// which stores the relevant information.
		// This method will also initialize the _rateZeroContinuous
		initialize_indexToTimerPair(localStates);
		
		// Initialize the matrix.
		_matrix = new int[matrixSize()][matrixSize()];
		
		// Set the lower bound/ upper bounds of the timers and the rates.
		initializeLowerUpperBounds(getAllNames(), localStates);
		
		// Initialize the row and column entries for the continuous variables.
		initializeRowColumnContVar();
		
		// Advance Time
		//advance();
		advance(localStates);
		
		// Re-canonicalize
		recononicalize();
		
		// Check the size of the DBM.
		checkZoneMaxSize();
	}
	
	/**
	 * Creates a Zone based on the local states.
	 * @param localStates
	 * 			The current state (or initial) of the LPNs.
	 */
	public Zone(State[] localStates, boolean init){
		
		// Extract the local states.
		//State[] localStates = tps.toStateArray();
		
		// Initialize hash code to -1 (indicating nothing cached).
		_hashCode = -1;
		
		// Initialize the LPN list.
		initialize_lpnList(localStates);
		
		// Get the enabled transitions. This initializes the _indexTotimerPair
		// which stores the relevant information.
		// This method will also initialize the _rateZeroContinuous
		initialize_indexToTimerPair(localStates);
		
		// Initialize the matrix.
		_matrix = new int[matrixSize()][matrixSize()];
		
		// Set the lower bound/ upper bounds of the timers and the rates.
		initializeLowerUpperBounds(getAllNames(), localStates);
		
		// Initialize the row and column entries for the continuous variables.
		initializeRowColumnContVar();
		
		if(init){
			return;
		}
		
		// Advance Time
		//advance();
		advance(localStates);
		
		// Re-canonicalize
		recononicalize();
		
		// Check the size of the DBM.
		checkZoneMaxSize();
	}
	
//	/**
//	 * Sets ups a zone containing continuous variables only.
//	 * @param continuousValues
//	 * 		The values to populate the zone with.
//	 */
//	public Zone(HashMap<LPNContinuousPair, IntervalPair> continuousValues){
//		
//		Set<LPNContinuousPair> pairSet = continuousValues.keySet();
//		
//		// Copy the LPNContinuousPairs over
//		_indexToTimerPair = new LPNTransitionPair[pairSet.size()+1];
//		
//		int count = 0; // The position in the _indexToTimerPair for the next value.
//		_indexToTimerPair[count++] = LPNTransitionPair.ZERO_TIMER_PAIR;
//		
//		for(LPNContinuousPair lcPair: pairSet){
//			_indexToTimerPair[count++] = lcPair;
//		}
//		
//		Arrays.sort(_indexToTimerPair);
//		
//		_matrix = new int[matrixSize()][matrixSize()];
//		
//		for(int i=1; i<dbmSize(); i++){
//			setDbmEntry(i, j, value)
//		}
//		
//	}
	
	/**
	 * Gives the names of all the transitions and continuous variables that 
	 * are represented by the zone.
	 * @return
	 * 		The names of the transitions and continuous variables that are 
	 * 		represented by the zone.
	 */
	public String[] getAllNames(){

//		String[] transitionNames = new String[_indexToTimerPair.length];
//		
//		transitionNames[0] = "The zero timer.";
//		
//		for(int i=1; i<transitionNames.length; i++){
//			
//			LPNTransitionPair ltPair = _indexToTimerPair[i];
//			
//			transitionNames[i] = _lpnList[ltPair.get_lpnIndex()]
//					.getTransition(ltPair.get_transitionIndex()).getName();
//		}
//		return transitionNames;
		
		// Get the continuous variable names.
		String[] contVar = getContVarNames();
		
		// Get the transition names.
		String[] trans = getTranNames();
		
		// Create an array large enough for all the names.
		String[] names = new String[contVar.length + trans.length + 1];
		
		// Add the zero timer.
		names[0] = "The zero timer.";
		
		// Add the continuous variables.
		for(int i=0; i<contVar.length; i++){
			names[i+1] = contVar[i];
		}
		
		// Add the timers.
		for(int i=0; i<trans.length; i++){
			// Already the zero timer has been added and the elements of contVar.
			// That's a total of 'contVar.length + 1' elements. The last index was
			// thus 'contVar.length' So the first index to add to is 
			// 'contVar.length +1'.
			names[1+contVar.length + i] = trans[i];
		}
		
		return names;
	}
	
	/**
	 * Get the names of the continuous variables that this zone uses.
	 * @return
	 * 		The names of the continuous variables that are part of this zone.
	 */
	public String[] getContVarNames(){
		
		// List for accumulating the names.
		ArrayList<String> contNames = new ArrayList<String>();
		
		// Find the pairs that represent the continuous variables. Loop starts at
		// i=1 since the i=0 is the zero timer.
		for(int i=1; i<_indexToTimerPair.length; i++){
			
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			// If the isTimer value is false, then this pair represents a continuous
			// variable.
			//if(!ltPair.get_isTimer()){
			// If pair is LPNContinuousPair.
			if(ltPair instanceof LPNContinuousPair){
				// Get the LPN that this pairing references and find the name of
				// the continuous variable whose index is given by this pairing.
				contNames.add(_lpnList[ltPair.get_lpnIndex()]
						.getContVarName(ltPair.get_transitionIndex()));
			}
		}
		
		return contNames.toArray(new String[0]);
	}
	
	/**
	 * Gets the names of the transitions that are associated with the timers in the
	 * zone. Does not return the zero timer.
	 * @return
	 * 		The names of the transitions whose timers are in the zone except the zero
	 * 		timer.
	 */
	public String[] getTranNames(){
		
		// List for accumulating the names.
		ArrayList<String> transitionNames = new ArrayList<String>();
		
		// Find the pairs that represent the transition timers.
		for(int i=1; i<_indexToTimerPair.length; i++){
			
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			// If the isTimer value is true, then this pair represents a timer.
			//if(ltPair.get_isTimer()){
			// If this is an LPNTransitionPair and not an LPNContinuousPair
			if(!(ltPair instanceof LPNContinuousPair)){
				// Get the LPN that this pairing references and find the name of the
				// transition whose index is given by this pairing.
				transitionNames.add(_lpnList[ltPair.get_lpnIndex()]
						.getTransition(ltPair.get_transitionIndex()).getName());
			}
		}
		
		return transitionNames.toArray(new String[0]);
	}
	
	/**
	 * Initializes the _lpnList using information from the local states.
	 * @param localStates
	 * 		The local states.
	 * @return
	 * 		The enabled transitions.
	 */
	private void initialize_lpnList(State[] localStates){
		
		// Create the LPN list.
		_lpnList = new LhpnFile[localStates.length];
		
		// Get the LPNs.
		for(int i=0; i<localStates.length; i++){
			_lpnList[i] = localStates[i].getLpn();
		}
	}
	
	/**
	 * Initializes the _indexToTimerPair from the local states. This includes
	 * adding the zero timer, the continuous variables and the set of 
	 * enabled timers.
	 * @param localStates
	 * 		The local states.
	 * @return
	 * 		The names of the transitions stored in the _indexToTimerPair (in the same order).
	 */
	private void initialize_indexToTimerPair(State[] localStates){
		
		/*
		 * The populating of the _indexToTimerPair is done in three stages.
		 * The first is to add the zero timer which is at the beginning of the zone.
		 * The second is to add the continuous variables. And the third is to add
		 * the other timers. Since the continuous variables are added before the
		 * timers and the variables and timers are added in the order of the LPNs,
		 * the elements in an accumulating list (enabledTransitionsArrayList) are
		 * already in order up to the elements added for a particular LPN. Thus the
		 * only sorting that needs to take place is the sorting for a particular LPN.
		 * Correspondingly, elements are first found for an LPN and sort, then added
		 * to the main list.
		 */
		
		// This method will also initialize the _rateZeroContinuous
		//_rateZeroContinuous = new DualHashMap<RangeAndPairing, Variable>();
		_rateZeroContinuous = 
				new DualHashMap<LPNTransitionPair, VariableRangePair>();
		
		// This list accumulates the transition pairs (ie timers) and the continuous
		// variables.
		ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
				new ArrayList<LPNTransitionPair>();
				
		// Put in the zero timer.
//		enabledTransitionsArrayList
//			.add(new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1, true));
		enabledTransitionsArrayList
			.add(new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1));
		
		// Get the continuous variables.
		for(int i=0; i<localStates.length; i++){
			
			// Accumulates the changing continuous variables for a single LPN.
			ArrayList<LPNTransitionPair> singleLPN = 
								new ArrayList<LPNTransitionPair>();
			
			// Get the associated LPN.
			LhpnFile lpn = localStates[i].getLpn();
			
			// Get the continuous variables for this LPN.
			String[] continuousVariables = lpn.getContVars();
			
			// Get the variable, index map.
			DualHashMap<String, Integer> variableIndex = lpn.getContinuousIndexMap();
			
			// Find which have a nonzero rate.
			for(int j=0; j<continuousVariables.length; j++){
				// Get the Variables with this name.
				Variable contVar = lpn.getVariable(continuousVariables[j]);
				
				// Get the rate.
				//int rate = (int) Double.parseDouble(contVar.getInitRate());
				IntervalPair rate = parseRate(contVar.getInitRate());
				
				// Get the LPN index for the variable
				int lpnIndex = lpn.getLpnIndex();
				
				// Get the index as a variable for the LPN.
				int contVariableIndex = variableIndex.get(continuousVariables[j]);
//				
//				LPNTransitionPair newPair = 
//						new LPNTransitionPair(lpnIndex, contVariableIndex, false);

				LPNContinuousPair newPair = 
						new LPNContinuousPair(lpnIndex, contVariableIndex,
								rate.get_LowerBound());
				
				// If the rate is non-zero, then the variables needs to be tracked
				// by matrix part of the Zone.
				//if(rate !=0){
				if(!rate.equals(new IntervalPair(0,0))){
					// Temporary exception guaranteeing only unit rates.
					//if(rate != -1 && rate != 1){
					if(rate.get_LowerBound() != 1 && rate.get_UpperBound() != 1){
						throw new IllegalArgumentException("Current development " +
								"only supports positive unit rates. The variable " + contVar +
								" has a rate of " + rate);
					}
					
//					// Get the LPN index for the variable
//					int lpnIndex = lpn.getLpnIndex();
//					
//					// Get the index as a variable for the LPN. This index matches
//					// the index in the vector stored by platu.State.
//					int contVariableIndex = variableIndex.get(continuousVariables[j]);
					
					// The continuous variable reference.
//					singleLPN.add(
//							new LPNTransitionPair(lpnIndex, contVariableIndex, false));
					
					singleLPN.add(newPair);
				}
				else{
					// If the rate is zero, then the Zone keeps track of this variable
					// in a list.
//					_rateZeroContinuous.put(newPair, cpontVar);
//					_rateZeroContinuous.
//						put(new RangeAndPairing(newPair, parseRate(contVar.getInitValue())),
//								contVar);
					_rateZeroContinuous.
						put(newPair, new VariableRangePair(contVar,
								parseRate(contVar.getInitValue())));
				}
			}
			
			// Sort the list.
			Collections.sort(singleLPN);
			
			// Add the list to the total accumulating list.
			for(int j=0; j<singleLPN.size(); j++){
				enabledTransitionsArrayList.add(singleLPN.get(j));
			}
		}
		
		// Get the transitions.
		for(int i=0; i<localStates.length; i++){
			
			// Extract the enabled transition vector.
			boolean[] enabledTran = localStates[i].getTranVector();
			
			// Accumulates the transition pairs for one LPN.
			ArrayList<LPNTransitionPair> singleLPN = new ArrayList<LPNTransitionPair>();
			
			// The index of the boolean value corresponds to the index of the Transition.
			for(int j=0; j<enabledTran.length; j++){
				if(enabledTran[j]){
					// Add the transition pair.
//					singleLPN.add(new LPNTransitionPair(i, j, true));
					singleLPN.add(new LPNTransitionPair(i, j));
				}
			}
			
			// Sort the transitions for the current LPN.
			Collections.sort(singleLPN);
			
			// Add the collection to the enabledTransitionsArrayList
			for(int j=0; j<singleLPN.size(); j++){
				enabledTransitionsArrayList.add(singleLPN.get(j));
			}
		}
		
		// Extract out the array portion of the enabledTransitionsArrayList.
		_indexToTimerPair = enabledTransitionsArrayList.toArray(new LPNTransitionPair[0]);
	}
	
	/**
	 * Sets the lower and upper bounds for the transitions and continuous variables.
	 * @param varNames
	 * 			The names of the transitions in _indexToTimerPair.
	 */
	private void initializeLowerUpperBounds(String[] varNames, State[] localStates){
		
		// Traverse the entire length of the DBM sub-matrix except the zero row/column.
		// This is the same length as the _indexToTimerPair.length-1. The DBM is used to
		// match the idea of setting the value for each row.
		for(int i=1; i<dbmSize(); i++){
			// Get the current LPN and transition pairing.
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			//int upper, lower;
			
			IntervalPair range;
			
//			if(!ltPair.get_isTimer()){
			if(ltPair instanceof LPNContinuousPair){
				// If the pairing represents a continuous variable, then the 
				// upper and lower bound are the initial value or infinity depending
				// on whether the initial rate is positive or negative.
				
				// If the value is a constant, then assign the upper and lower bounds
				// to be constant. If the value is a range then assign the upper and
				// lower bounds to be a range.
				Variable v = _lpnList[ltPair.get_lpnIndex()]
						.getContVar(ltPair.get_transitionIndex());
				
//				int initialRate = (int) Double.parseDouble(v.getInitRate());
//				upper = initialRate;
//				lower = initialRate;
				String rate = v.getInitRate();
				
				// Parse the rate. Should be in the form of [x,y] where x
				// and y are integers.
				//IntervalPair range = parseRate(rate);
				range = parseRate(rate);
			
				// Set the upper and lower bound (in the matrix) for the 
				// continuous variables.
				
				// TODO : Check if correct.
				String contValue = v.getInitValue();
				IntervalPair bound = parseRate(contValue);
				// Set upper bound (DBM entry (0, x) where x is the index of the variable v).
				setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, ltPair, bound.get_UpperBound());
				
				// Set lower bound (DBM entry (x, 0) where x is the index of the variable v).
				setDbmEntryByPair(ltPair, LPNTransitionPair.ZERO_TIMER_PAIR, -1*bound.get_LowerBound());
				
				
				
//				lower = range.get_LowerBound();
//				upper = range.get_UpperBound();
				
			}
			else{
			
				// Get the expression tree.
				ExprTree delay = _lpnList[ltPair.get_lpnIndex()].getDelayTree(varNames[i]);

				// Get the values of the variables for evaluating the ExprTree.
				HashMap<String, String> varValues = 
						_lpnList[ltPair.get_lpnIndex()]
								.getAllVarsWithValuesAsString(localStates[ltPair.get_lpnIndex()].getVector());

				// Set the upper and lower bound.
				// Passing the zone as null since it should not be needed.
//				if(delay.getOp().equals("uniform")){
//					IntervalPair lowerRange = delay.getLeftChild()
//							.evaluateExprBound(varValues, null);
//					IntervalPair upperRange = delay.getRightChild()
//							.evaluateExprBound(varValues, null);
//					
//					// The lower and upper bounds should evaluate to a single
//					// value. Yell if they don't.
//					if(!lowerRange.singleValue() || !upperRange.singleValue()){
//						throw new IllegalStateException("When evaulating the delay, " +
//								"the lower or the upper bound evaluated to a range " +
//								"instead of a single value.");
//					}
//					
//					range = new IntervalPair(lowerRange.get_LowerBound(),
//							upperRange.get_UpperBound());
//					
//				}
//				else{
//					range = delay.evaluateExprBound(varValues, null);
//				}
				
				range = delay.evaluateExprBound(varValues, this, null);
				
//				int upper, lower;
//				if(delay.getOp().equals("uniform"))
//				{
//					ExprTree lowerDelay = delay.getLeftChild();
//					ExprTree upperDelay = delay.getRightChild();
//
//					lower = (int) lowerDelay.evaluateExpr(varValues);
//					upper = (int) upperDelay.evaluateExpr(varValues);
//				}
//				else
//				{
//					lower = (int) delay.evaluateExpr(varValues);
//
//					upper = lower;
//				}
			}
//			setLowerBoundbydbmIndex(i, lower);
//			setUpperBoundbydbmIndex(i, upper);
			
			setLowerBoundbydbmIndex(i, range.get_LowerBound());
			setUpperBoundbydbmIndex(i, range.get_UpperBound());
		}
		
	}
	
	/**
	 * Initialize the rows and columns for the continuous variables.
	 */
	private void initializeRowColumnContVar(){
		
		/*
		 * TODO : Describe the idea behind the following algorithm.
		 */
		
//		for(int row=2; row<_indexToTimerPair.length; row++){
//			// Note: row is indexing the row of the DBM matrix.
//			
//			LPNTransitionPair ltRowPair = _indexToTimerPair[row];
//			
//			if(ltRowPair.get_isTimer()){
//				// If we reached the timers, stop.
//				break;
//			}
//			
//			for(int col=1; col<row; col++){
//				// Note: col is indexing the column of the DBM matrix. 
//				
//				// The new (row, col) entry. The entry is given by col-row<= m_(row,col). Since 
//				// col <= m_(0,col) (its upper bound) and -row <= m_(row,0) (the negative of its lower
//				// bound), the entry is given by col-row <= m(0,col) + m_(row,0) = m_(row,col);
//				int rowCol = getDbmEntry(row,0) + getDbmEntry(0, col);
//				
//				// The new (col, row) entry.
//				int colRow = getDbmEntry(col, 0) + getDbmEntry(0, row);
//						
//				setDbmEntry(row, col, rowCol);
//				setDbmEntry(col, row, colRow);
//			}
//		}
		
		// The only entries that do not need to be checked are the ones where both variables 
		// represent timers.
		
		for(int row=2; row<_indexToTimerPair.length; row++){
			// Note: row is indexing the row of the DBM matrix.
			
			LPNTransitionPair ltRowPair = _indexToTimerPair[row];
			
//			if(ltRowPair.get_isTimer()){
//				// If we reached the timers, stop.
//				break;
//			}
			
			for(int col=1; col<row; col++){
				// Note: col is indexing the column of the DBM matrix. 
				
				LPNTransitionPair ltColPair = _indexToTimerPair[col];
				
				// If we've reached the part of the zone involving only timers, then break out
				// of this row.
//				if(ltRowPair.get_isTimer() && ltColPair.get_isTimer()){
				if(!(ltRowPair instanceof LPNContinuousPair) && 
						!(ltColPair instanceof LPNContinuousPair)){
					break;
				}
				
				// The new (row, col) entry. The entry is given by col-row<= m_(row,col). Since 
				// col <= m_(0,col) (its upper bound) and -row <= m_(row,0) (the negative of its lower
				// bound), the entry is given by col-row <= m(0,col) + m_(row,0) = m_(row,col);
				int rowCol = getDbmEntry(row,0) + getDbmEntry(0, col);
				
				// The new (col, row) entry.
				int colRow = getDbmEntry(col, 0) + getDbmEntry(0, row);
						
				setDbmEntry(row, col, rowCol);
				setDbmEntry(col, row, colRow);
			}
		}
	}
	
	/**
	 * Zero argument constructor for use in methods that create Zones where the members
	 * variables will be set by the method.
	 */
	private Zone()
	{
		_matrix = new int[0][0];
		_indexToTimerPair = new LPNTransitionPair[0];
		_hashCode = -1;
		_lpnList = new LhpnFile[0];
		_rateZeroContinuous = new DualHashMap<LPNTransitionPair, VariableRangePair>();
	}
	
	/**
	 * Gets the upper bound of a Transition from the zone.
	 * @param t
	 * 		The transition whose upper bound is wanted.
	 * @return
	 * 		The upper bound of Transition t.
	 */
	public int getUpperBoundbyTransition(Transition t)
	{
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = 
//				new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = 
				new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return getUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/**
	 * Returns the upper bound of the continuous variable.
	 * @param var
	 * 		The variable of interest.
	 * @return
	 * 		The (0,var) entry of the zone, that is the maximum as recored by
	 * 		the zone.
	 */
	public int getUpperBoundbyContinuousVariable(String contVar, LhpnFile lpn){
		
		// TODO : Finish.
		
//		// Determine whether the variable is in the zone or rate zero.
//		RangeAndPairing indexAndRange = _rateZeroContinuous.getKey(var);
//		
//		
//		// If a RangeAndPairing is returned, then get the information from here.
//		if(indexAndRange != null){
//			return indexAndRange.get_range().get_UpperBound();
//		}
//		
//		// If indexAndRange is null, then try to get the value from the zone.
//		int i=-1;
//		for(i=0; i<_indexToTimerPair.length; i++){
//			if(_indexToTimerPair[i].equals(var)){
//				break;
//			}
//		}
//		
//		if(i < 0){
//			throw new IllegalStateException("Atempted to find the upper bound for "
//					+ "a non-rate zero continuous variable that was not found in the "
//					+ "zone.");
//		}
//		
//		return getUpperBoundbydbmIndex(i);
		
		// Extract the necessary indecies.
		int lpnIndex = lpn.getLpnIndex();

		//int contVarIndex = lpn.get
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.get(contVar);

		// Package the indecies with false indicating not a timer.
//		LPNTransitionPair index = new LPNTransitionPair(lpnIndex, contIndex, false);
		// Note : setting the rate is not necessary here since this is only 
		// being used as an index.
//		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex, 0);
		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex);

		//Search for the continuous variable in the rate zero variables.
		VariableRangePair pairing = _rateZeroContinuous.get(index);

		// If Pairing is not null, the variable was found and return the result.
		if(pairing != null){
			return pairing.get_range().get_UpperBound();
		}

		// If Pairing was null, the variable was not found. Search for the variable
		// in the zone portion.
		int i = Arrays.binarySearch(_indexToTimerPair, index);

		// If i < 0, the search was unsuccessful, so scream.
		if(i < 0){
			throw new IllegalArgumentException("Atempted to find the lower bound for "
					+ "a non-rate zero continuous variable that was not found in the "
					+ "zone.");
		}

		//return getUpperBoundbydbmIndex(i);
		return getDbmEntry(0, i);
	}
	
	public int getUpperBoundForRate(LPNTransitionPair contVar){
		// TODO : finish.
		
		// Check if the contVar is in the zone.
		int i = Arrays.binarySearch(_indexToTimerPair, contVar);
		
		if(i > 0){
			// The continuous variable is in the zone.
			// The upper and lower bounds are stored in the same
			// place as the delays, so the same method of 
			// retrieval will work.
			//return getUpperBoundbydbmIndex(contVar.get_transitionIndex());
			
			// Grab the current rate from the LPNContinuousPair.
			return ((LPNContinuousPair)_indexToTimerPair[i]).getCurrentRate();
		}
		
		
		// Assume the rate is zero. This covers the case if conVar
		// is in the rate zero as well as if its not in the state at all.
		return 0;
	}
	
	/**
	 * Get the value of the upper bound for the delay. If the index refers
	 * to a timer, otherwise get the upper bound for the continuous 
	 * variables rate.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public int getUpperBoundbydbmIndex(int index)
	{
		return _matrix[0][dbmIndexToMatrixIndex(index)];
	}
	
	/**
	 * Set the value of the upper bound for the delay.
	 * @param t
	 * 			The transition whose upper bound is being set.
	 * @param value
	 * 			The value of the upper bound.
	 */
	public void setUpperBoundbyTransition(Transition t, int value)
	{
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		setUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair), value);
	}
	
	/**
	 * Set the value of the upper bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @param value
	 * 			The value of the upper bound.
	 */
	public void setUpperBoundbydbmIndex(int index, int value)
	{
		_matrix[0][dbmIndexToMatrixIndex(index)] = value;
	}
	
	/**
	 * Sets the upper bound for a transition described by an LPNTransitionPair.
	 * @param ltPair
	 * 			The index of the transition and the index of the associated LPN for 
	 * 			the timer to set the upper bound.
	 * @param value
	 * 			The value for setting the upper bound.
	 */
	private void setUpperBoundByLPNTransitionPair(LPNTransitionPair ltPair, int value){
		
		setUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair), value);
	}
	
	/**
	 * Gets the lower bound of a Transition from the zone.
	 * @param t
	 * 		The transition whose upper bound is wanted.
	 * @return
	 * 		The lower bound of Transition t.
	 */
	public int getLowerBoundbyTransition(Transition t)
	{
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return -1*getLowerBoundbydbmIndex(
				Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/**
	 * Returns the lower bound of the continuous variable.
	 * @param var
	 * 		The variable of interest.
	 * @return
	 * 		The (0,var) entry of the zone, that is the minimum as recored by
	 * 		the zone.
	 */
	public int getLowerBoundbyContinuousVariable(String contVar, LhpnFile lpn){

		// Extract the necessary indecies.
		int lpnIndex = lpn.getLpnIndex();
		
		//int contVarIndex = lpn.get
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.get(contVar);
		
		// Package the indecies with false indicating not a timer.
//		LPNTransitionPair index = new LPNTransitionPair(lpnIndex, contIndex, false);
		// Note: Setting the rate is not necessary since this is only being used
		// as an index.
//		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex, 0);
		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex);
		
		//Search for the continuous variable in the rate zero variables.
		VariableRangePair pairing = _rateZeroContinuous.get(index);
		
		// If Pairing is not null, the variable was found and return the result.
		if(pairing != null){
			return pairing.get_range().get_LowerBound();
		}
		
		// If Pairing was null, the variable was not found. Search for the variable
		// in the zone portion.
		int i = Arrays.binarySearch(_indexToTimerPair, index);
		
		// If i < 0, the search was unsuccessful, so scream.
		if(i < 0){
			throw new IllegalArgumentException("Atempted to find the lower bound for "
					+ "a non-rate zero continuous variable that was not found in the "
					+ "zone.");
		}
		
		//return getLowerBoundbydbmIndex(i);
		return getDbmEntry(i, 0);
	}
	
	/**
	 * Get the value of the lower bound for the delay if the index refers
	 * to a timer, otherwise get the lower bound for the continuous variables
	 * rate.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The value of the lower bound.
	 */
	public int getLowerBoundbydbmIndex(int index)
	{
		return _matrix[dbmIndexToMatrixIndex(index)][0];
	}
	
	public int getLowerBoundForRate(LPNTransitionPair contVar){
		// TODO : finish. 
		
		
		// Check if the contVar is in the zone.
		int i = Arrays.binarySearch(_indexToTimerPair, contVar);

		if(i > 0){
			// The continuous variable is in the zone.
			// The upper and lower bounds are stored in the same
			// place as the delays, so the same method of 
			// retrieval will work.
			return getLowerBoundbydbmIndex(contVar.get_transitionIndex());
		}


		// Assume the rate is zero. This covers the case if conVar
		// is in the rate zero as well as if its not in the state at all.
		return 0;
	}
	
	/**
	 * Set the value of the lower bound for the delay.
	 * @param t
	 * 			The transition whose lower bound is being set.
	 * @param value
	 * 			The value of the lower bound.
	 */
	public void setLowerBoundbyTransition(Transition t, int value)
	{
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		setLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair,ltPair), value);
	}
	
	/**
	 * Set the value of the upper bound for the delay.
	 * @param t
	 * 			The transition whose upper bound is being set.
	 * @param value
	 * 			The value of the upper bound.
	 */
	private void setLowerBoundByLPNTransitionPair(LPNTransitionPair ltPair, int value){
		setLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair,ltPair), value);
	}
	
	/**
	 * Set the value of the lower bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @param value
	 * 			The value of the lower bound.
	 */
	public void setLowerBoundbydbmIndex(int index, int value)
	{
		_matrix[dbmIndexToMatrixIndex(index)][0] = -1*value;
	}
	
	/**
	 * Give the upper and lower bounds for a continuous variable.
	 * @param contVar
	 * 		The variable of interest.
	 * @return
	 * 		The upper and lower bounds according to the Zone.
	 */
	public IntervalPair getContinuousBounds(String contVar, LhpnFile lpn){
		
		/*
		 * Need to determine whether this is suppose to be a rate zero variable or a non-zero 
		 * rate variable. One method is to check the rate of the passed variable. The other is
		 * to just check if the variable is present in either place.
		 */
		
		// Extract the necessary indecies.
		int lpnIndex = lpn.getLpnIndex();
		
		// Get the index of the continuous variable.
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.get(contVar);
		
		// Package the indecies with false indicating not a timer.
//		LPNTransitionPair index = new LPNTransitionPair(lpnIndex, contIndex, false);
		// Note: setting the current rate is not necessary here since the
		// LPNContinuousPair is only being used as an index.
//		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex, 0);
		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex);
		
		// Search for the continuous variable in the rate zero variables.
		VariableRangePair pairing = _rateZeroContinuous.get(index);
		
		// If Pairing is not null, the variable was found and return the result.
		if(pairing != null){
			return pairing.get_range();
		}
		
		// If Pairing was null, the variable was not found. Search for the variable
		// in the zone portion.
		int i = Arrays.binarySearch(_indexToTimerPair, index);
		
		// If i < 0, the search was unsuccessful, so scream.
		if(i < 0){
			throw new IllegalArgumentException("Atempted to find the bounds for "
					+ "a non-rate zero continuous variable that was not found in the "
					+ "zone.");
		}
		
		// Else find the upper and lower bounds.
//		int lower = getLowerBoundbydbmIndex(i);
//		int upper = getUpperBoundbydbmIndex(i);
		
		int lower = (-1)*getDbmEntry(i, 0);
		int upper = getDbmEntry(0, i);
				
		return new IntervalPair(lower, upper);
	}
	
	/** 
	 * Sets the bounds for a continuous variable.
	 * @param contVar
	 * 		The continuous variable to set the bounds on.
	 * @param lpn
	 * 		The LhpnFile object that contains the variable.
	 * @param range
	 * 		The new range of the continuous variable.
	 */
	public void setContinuousBounds(String contVar, LhpnFile lpn,
			IntervalPair range){

		// Extract the necessary indecies.
		int lpnIndex = lpn.getLpnIndex();

		// Get the index of the continuous variable.
		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
		int contIndex = variableIndecies.get(contVar);

		// Package the indecies with false indicating not a timer.
//		LPNTransitionPair index = new LPNTransitionPair(lpnIndex, contIndex, false);
		//Note : Setting the rate is not necessary since this only being used
		// as an index.
//		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex, 0);
		LPNContinuousPair index = new LPNContinuousPair(lpnIndex, contIndex);

		// Search for the continuous variable in the rate zero variables.
		VariableRangePair pairing = _rateZeroContinuous.get(index);

		// If Pairing is not null, the variable was found and make the new assignment.
		if(pairing != null){
			pairing.set_range(range);
			return;
		}

		// If Pairing was null, the variable was not found. Search for the variable
		// in the zone portion.
		int i = Arrays.binarySearch(_indexToTimerPair, index);

		// If i < 0, the search was unsuccessful, so scream.
		if(i < 0){
			throw new IllegalArgumentException("Atempted to find the bounds for "
					+ "a non-rate zero continuous variable that was not found in the "
					+ "zone.");
		}
		
		// Else find the upper and lower bounds.
//		setLowerBoundbydbmIndex(i, range.get_LowerBound());
//		setUpperBoundbydbmIndex(i, range.get_UpperBound());
		setDbmEntry(i, 0, (-1)*range.get_LowerBound());
		setDbmEntry(0, i, range.get_UpperBound());
		
	}
	
	/**
	 * Converts the index of the DBM to the index of _matrix.
	 * @param i
	 * 			The row/column index of the DBM.
	 * @return
	 * 			The row/column index of _matrix.
	 */
	private int dbmIndexToMatrixIndex(int i)
	{
		return i+1;
	}
	
	/**
	 * Retrieves an entry of the DBM using the DBM's addressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @return
	 * 			The value of the (i, j) element of the DBM.
	 */
	public int getDbmEntry(int i, int j)
	{
		return _matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)];
	}
	
	/**
	 * Sets an entry of the DBM using the DBM's addressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @param value
	 * 			The new value for the entry.
	 */
	private void setDbmEntry(int i, int j, int value)
	{
		_matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)] = value;
	}
	
	/**
	 * Sets the entry in the DBM using the LPNTransitionPair indexing.
	 * @param row
	 * 			The LPNTransitionPair for the row.
	 * @param col
	 * 			The LPNTransitionPair for the column.
	 * @param value
	 * 			The value to set the entry to.
	 */
	private void setDbmEntryByPair(LPNTransitionPair row, LPNTransitionPair col, int value){
		
		// The row index.
		int i = timerIndexToDBMIndex(row);
		
		// The column index.
		int j = timerIndexToDBMIndex(col);
		
		setDbmEntry(i, j, value);
	}
	
	/**
	 * Returns the index of the the transition in the DBM given a LPNTransitionPair pairing
	 * the transition index and associated LPN index.
	 * @param ltPair
	 * 		The pairing comprising the index of the transition and the index of the associated
	 * 		LPN.
	 * @return
	 * 		The row/column of the DBM associated with the ltPair.
	 */
	public int timerIndexToDBMIndex(LPNTransitionPair ltPair)
	{
		return Arrays.binarySearch(_indexToTimerPair, ltPair);
	}
	
	
	/**
	 * The matrix labeled with 'ti' where i is the transition index associated with the timer.
	 */
	public String toString()
	{
		// TODO : Fix the handling of continuous variables in the 
		// _lpnList == 0 case.
		
		String result = "Timer and delay or continuous and ranges.\n";
		
		int count = 0;
		
		// Print the timers.
		for(int i=1; i<_indexToTimerPair.length; i++, count++)
		{
			if(_lpnList.length == 0)
			{
				// If no LPN's are associated with this Zone, use the index of the timer.
				result += " t" + _indexToTimerPair[i].get_transitionIndex() + " : ";
			}
			else
			{
				String name;
				
				// If the current LPNTransitionPair is a timer, get the name
				// from the transitions.
//				if(_indexToTimerPair[i].get_isTimer()){
				// If the current timer is an LPNTransitionPair and not an LPNContinuousPair
				if(!(_indexToTimerPair[i] instanceof LPNContinuousPair)){
				
					// Get the name of the transition.
					Transition tran = _lpnList[_indexToTimerPair[i].get_lpnIndex()].
							getTransition(_indexToTimerPair[i].get_transitionIndex());
					
					name = tran.getName();
				}
				else{
					// If the current LPNTransitionPair is not a timer, get the
					// name as a continuous variable.
					Variable var = _lpnList[_indexToTimerPair[i].get_lpnIndex()]
							.getContVar(_indexToTimerPair[i].get_transitionIndex());
					
					name = var.getName() + 
							":[" + -1*getDbmEntry(i, 0) + "," + getDbmEntry(0, i) + "]\n" +
							"rate:";
				}
				
//				result += " " +  tran.getName() + ":";
				
				result += " " + name + ":";
			}
			result += "[ " + -1*getLowerBoundbydbmIndex(i) + ", " + getUpperBoundbydbmIndex(i) + " ]";
			
			if(count > 9)
			{
				result += "\n";
				count = 0;
			}
		}
		
		if(!_rateZeroContinuous.isEmpty()){
			result += "\nRate Zero Continuous : \n";
			for (LPNTransitionPair ltPair : _rateZeroContinuous.keySet()){
				result += "" + _rateZeroContinuous.get(ltPair);
			}
		}
		
		result += "\nDBM\n";
		

		// Print the DBM.
		for(int i=0; i<_indexToTimerPair.length; i++)
		{
			result += "| " + getDbmEntry(i, 0);

			for(int j=1; j<_indexToTimerPair.length; j++)
			{

				result += ", " + getDbmEntry(i, j);
			}
			
			result += " |\n";
		}
		

		return result;
	}
	
	/**
	 * Tests for equality. Overrides inherited equals method.
	 * @return True if o is equal to this object, false otherwise.
	 */
	public boolean equals(Object o)
	{
		// Check if the reference is null.
		if(o == null)
		{
			return false;
		}
		
		// Check that the type is correct.
		if(!(o instanceof Zone))
		{
			return false;
		}
		
		// Check for equality using the Zone equality.
		return equals((Zone) o);
	}
	
	
	/**
	 * Tests for equality.
	 * @param
	 * 		The Zone to compare.
	 * @return 
	 * 		True if the zones are non-null and equal, false otherwise.
	 */
	public boolean equals(Zone otherZone)
	{
		// Check if the reference is null first.
		if(otherZone == null)
		{
			return false;
		}
		
		// Check for reference equality.
		if(this == otherZone)
		{
			return true;
		}
		
		// If the hash codes are different, then the objects are not equal. 
		if(this.hashCode() != otherZone.hashCode())
		{
			return false;
		}
		
		// Check if the they have the same number of timers.
		if(this._indexToTimerPair.length != otherZone._indexToTimerPair.length){
			return false;
		}
		
		// Check if the timers are the same.		
		for(int i=0; i<this._indexToTimerPair.length; i++){
			if(!(this._indexToTimerPair[i].equals(otherZone._indexToTimerPair[i]))){
				return false;
			}
		}
		
		// Check if the matrix is the same 
		for(int i=0; i<_matrix.length; i++)
		{
			for(int j=0; j<_matrix[0].length; j++)
			{
				if(!(this._matrix[i][j] == otherZone._matrix[i][j]))
				{
					return false;
				}
			}
		}
		
		
		return true;
	}
	
	/**
	 * Determines if this zone is a subset of Zone otherZone.
	 * @param otherZone 
	 * 		The zone to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise.
	 */
	public boolean subset(Zone otherZone){
		// Check if the reference is null first.
				if(otherZone == null)
				{
					return false;
				}
				
				// Check for reference equality.
				if(this == otherZone)
				{
					return true;
				}
				
				// Check if the the same number of timers are present.
				if(this._indexToTimerPair.length != otherZone._indexToTimerPair.length){
					return false;
				}
				
				// Check if the transitions are the same.
				for(int i=0; i<this._indexToTimerPair.length; i++){
					if(!(this._indexToTimerPair[i].equals(otherZone._indexToTimerPair[i]))){
						return false;
					}
				}
				
				// Check if the entries of this Zone are less than or equal to the entries
				// of the other Zone.
				for(int i=0; i<_matrix.length; i++)
				{
					for(int j=0; j<_matrix[0].length; j++)
					{
						if(!(this._matrix[i][j] <= otherZone._matrix[i][j])){
							return false;
						}
					}
				}
				
				
				return true;
	}
	
	/**
	 * Determines if this zone is a superset of Zone otherZone.
	 * @param otherZone 
	 * 		The zone to compare against.
	 * @return
	 * 		True if this is a subset of other; false otherwise. More specifically it
	 *		gives the result of otherZone.subset(this). Thus it agrees with the subset method.
	 */
	public boolean superset(Zone otherZone){
		return otherZone.subset(this);
	}
	
	/**
	 * Overrides the hashCode.
	 */
	public int hashCode()
	{
		// Check if the hash code has been set.
		if(_hashCode <0)
		{
			_hashCode = createHashCode();
		}

		return _hashCode;
	}
	
	/**
	 * Creates a hash code for a Zone object.
	 * @return
	 * 		The hash code.
	 */
	private int createHashCode()
	{
		int newHashCode = Arrays.hashCode(_indexToTimerPair);
		
		for(int i=0; i<_matrix.length; i++)
		{
			newHashCode ^= Arrays.hashCode(_matrix[i]);
		}
		
		return Math.abs(newHashCode);
	}
	
	/**
	 * The size of the DBM sub matrix. This is calculated using the size of _indexToTimer.
	 * @return
	 * 			The size of the DBM.
	 */
	private int dbmSize()
	{
		return _indexToTimerPair.length;
	}
	
	/**
	 * The size of the matrix.
	 * @return
	 * 			The size of the matrix. This is calculated using the size of _indexToTimer.
	 */
	private int matrixSize()
	{
		return _indexToTimerPair.length + 1;
	}
	
	/**
	 * Performs the Floyd's least pairs algorithm to reduce the DBM.
	 */
	private void recononicalize()
	{
		for(int k=0; k<dbmSize(); k++)
		{
			for (int i=0; i<dbmSize(); i++)
			{
				for(int j=0; j<dbmSize(); j++)
				{
					if(getDbmEntry(i, k) != INFINITY && getDbmEntry(k, j) != INFINITY
							&& getDbmEntry(i, j) > getDbmEntry(i, k) + getDbmEntry(k, j))
					{
						setDbmEntry(i, j, getDbmEntry(i, k) + getDbmEntry(k, j));
					}
					
					if( (i==j) && getDbmEntry(i, j) != 0)
					{
						throw new DiagonalNonZeroException("Entry (" + i + ", " + j + ")" +
								" became " + getDbmEntry(i, j) + ".");
					}
				}
			}
		}
	}
	
	/**
	 * Determines if a timer associated with a given transitions has reached its lower bound.
	 * @param t
	 * 			The transition to consider.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public boolean exceedsLowerBoundbyTransitionIndex(Transition t)
	{
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return exceedsLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/**
	 * Determines if a timer has reached its lower bound.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public boolean exceedsLowerBoundbydbmIndex(int index)
	{

		// Note : Make sure that the lower bound is stored as a negative number
		// and that the inequality is correct.
		return _matrix[0][dbmIndexToMatrixIndex(index)] <=
			_matrix[1][dbmIndexToMatrixIndex(index)];
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#fireTransitionbyTransitionIndex(int, int[], verification.platu.stategraph.State)
	 */
//	public Zone fireTransitionbyTransitionIndex(int timer, int[] enabledTimers, 
//			State state)
//	{
//		// TODO: Check if finish.
//		int index = Arrays.binarySearch(_indexToTimer, timer);
//		
//		//return fireTransitionbydbmIndex(Arrays.binarySearch(_indexToTimer, timer), 
//				//enabledTimers, state);
//		
//		// Check if the value is in this zone to fire.
//		if(index < 0){
//			return this;
//		}
//		
//		return fireTransitionbydbmIndex(index, enabledTimers, state);
//	}
	
	/**
	 * Gives the Zone obtained by firing a given Transitions.
	 * @param t
	 * 		The transitions being fired.
	 * @param enabledTran
	 * 		The list of currently enabled Transitions.
	 * @param localStates
	 * 		The current local states.
	 * @return
	 * 		The Zone obtained by firing Transition t with enabled Transitions enabled
	 * 		enabledTran when the current state is localStates.
	 */
	public Zone fire(Transition t, LpnTranList enabledTran, HashMap<LPNContinuousPair,IntervalPair> newContValue,
			State[] localStates){
		
		// Create the LPNTransitionPair to check if the Transitions is in the zone and to 
		// find the index.
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
//		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex, true);
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		int dbmIndex = Arrays.binarySearch(_indexToTimerPair, ltPair);
		
		if(dbmIndex <= 0){
			return this;
		}
		
		// Get the new zone portion.
		Zone newZone = fireTransitionbydbmIndex(dbmIndex, enabledTran, localStates);
		
		// Update any assigned continuous variables.
		//newZone.updateContinuousAssignment(t, localStates[lpnIndex]);
		newZone.updateContinuousAssignment(newContValue);
		
		//return fireTransitionbydbmIndex(dbmIndex, enabledTran, localStates);
		
		
		return newZone;
	}
	
	/**
	 * Updates the Zone according to the transition firing.
	 * @param index
	 * 			The index of the timer.
	 * @return
	 * 			The updated Zone.
	 */
	public Zone fireTransitionbydbmIndex(int index, LpnTranList enabledTimers,
			State[] localStates)
	{
		/*
		 * For the purpose of adding the newly enabled transitions and removing
		 * the disable transitions, the continuous variables that still have
		 * a nonzero rate can be treated like still enbaled timers.
		 */
		
		// Initialize the zone.
		Zone newZone = new Zone();
		
		// These sets will defferentiate between the new timers and the
		// old timers, that is between the timers that are not already in the
		// zone and those that are already in the zone..
		HashSet<LPNTransitionPair> newTimers = new HashSet<LPNTransitionPair>();
		HashSet<LPNTransitionPair> oldTimers = new HashSet<LPNTransitionPair>();
		
		// Copy the LPNs over.
		newZone._lpnList = new LhpnFile[this._lpnList.length];
		for(int i=0; i<this._lpnList.length; i++){
			newZone._lpnList[i] = this._lpnList[i];
		}
		
		// Copy the rate zero continuous variables over.
		newZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		// Add the continuous variables to the enabled timers.
		for(int i=1; _indexToTimerPair[i] instanceof LPNContinuousPair; i++){
			oldTimers.add(_indexToTimerPair[i]);
		}
		
		// Extract the pairing information for the enabled timers.
		// Using the enabledTimersList should be faster than calling the get method
		// several times. Also add the non-zero rate continuous variables.
//		newZone._indexToTimerPair = new LPNTransitionPair[enabledTimers.size() + 1];
		newZone._indexToTimerPair = new LPNTransitionPair[enabledTimers.size() + oldTimers.size() + 1];
		// Currently the 'oldTimers' set contains only the continuous variables.
		
		int count = 0; // Count keeps track of where to place the next LPNTransitionPair into the _indexToTimer.
		
		// Copy in the continuous variables (and the zero timer).
		//for(int i=0; i<oldTimers.size(); i++){
		for(LPNTransitionPair ltPair : _indexToTimerPair){
			if(!(ltPair instanceof LPNContinuousPair) && !(ltPair.equals(LPNTransitionPair.ZERO_TIMER_PAIR))){
				// If we've reached the end of the continous variables, stop.
				break;
			}
			// This loop counts to the number of elements in the oldTimers set.
			// This corresponds to the number of continuous variables in
			// this._indexToTimerPair. But the continuous variables start at
			// i=1 after the zero timer pair.
//			newZone._indexToTimerPair[count++] = _indexToTimerPair[i].clone();
			newZone._indexToTimerPair[count++] = ltPair.clone();
			// I'm cloning these elements in case I need to change the 
			// rate. If the rate does not change, this can be a copy.
		}
		
//		newZone._indexToTimerPair[count++] = 
//				new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1, true);
//		newZone._indexToTimerPair[count++] = 
//				new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1);
		for(Transition t : enabledTimers){
//			newZone._indexToTimerPair[count++] = 
//					new LPNTransitionPair(t.getLpn().getLpnIndex(), t.getIndex(), true);
			newZone._indexToTimerPair[count++] = 
					new LPNTransitionPair(t.getLpn().getLpnIndex(), t.getIndex());
		}
		
		Arrays.sort(newZone._indexToTimerPair);
		
//		HashSet<LPNTransitionPair> newTimers = new HashSet<LPNTransitionPair>();
//		HashSet<LPNTransitionPair> oldTimers = new HashSet<LPNTransitionPair>();
		
		for(int i=0; i<newZone._indexToTimerPair.length; i++)
		{
			// Determine if each value is a new timer or old.
			if(Arrays.binarySearch(this._indexToTimerPair, newZone._indexToTimerPair[i])
					>= 0 )
			{
				// The timer was already present in the zone.
				oldTimers.add(newZone._indexToTimerPair[i]);
			}
			else
			{
				// The timer is a new timer.
				newTimers.add(newZone._indexToTimerPair[i]);
			}
		}
		
		// Create the new matrix.
		newZone._matrix = new int[newZone.matrixSize()][newZone.matrixSize()];
		
		// TODO: For simplicity, make a copy of the current zone and perform the
		// restriction and re-canonicalization. Later add a copy re-canonicalization
		// that does the steps together.
		
		Zone tempZone = this.clone();
		
		tempZone.restrictTimer(index);
		tempZone.recononicalize();
		
		// Copy the tempZone to the new zone.
		for(int i=0; i<tempZone.dbmSize(); i++)
		{
			if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			{
				continue;
			}
			
			// Get the new index of for the timer.
			int newIndexi = i==0 ? 0 : 
				Arrays.binarySearch(newZone._indexToTimerPair, tempZone._indexToTimerPair[i]);
			for(int j=0; j<tempZone.dbmSize(); j++)
			{
				if(!oldTimers.contains(tempZone._indexToTimerPair[j]))
				{
					continue;
				}
				int newIndexj = j==0 ? 0 : 
					Arrays.binarySearch(newZone._indexToTimerPair, tempZone._indexToTimerPair[j]);
				
				newZone._matrix[newZone.dbmIndexToMatrixIndex(newIndexi)]
						[newZone.dbmIndexToMatrixIndex(newIndexj)]
								= tempZone.getDbmEntry(i, j);
			}
		}
		
		// Copy the upper and lower bounds.
		for(int i=1; i<tempZone.dbmSize(); i++)
		{
			if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			{
				continue;
			}
			newZone.setLowerBoundByLPNTransitionPair(tempZone._indexToTimerPair[i], 
					-1*tempZone.getLowerBoundbydbmIndex(i));
			// The minus sign is because _matrix stores the negative of the lower bound.
			
			newZone.setUpperBoundByLPNTransitionPair(tempZone._indexToTimerPair[i],
					tempZone.getUpperBoundbydbmIndex(i));
		}
		
		// Copy in the new relations for the new timers.
		for(LPNTransitionPair timerNew : newTimers)
		{
			for(LPNTransitionPair timerOld : oldTimers)
			{	
				newZone.setDbmEntry(newZone.timerIndexToDBMIndex(timerNew),
						newZone.timerIndexToDBMIndex(timerOld),
						 tempZone.getDbmEntry(0, tempZone.timerIndexToDBMIndex(timerOld)));
//				
//				int newTimeIndex = newZone.timerIndexToDBMIndex(timerNew);
//				int oldTimeIndex = newZone.timerIndexToDBMIndex(timerOld);
//				int value = tempZone.getDbmEntry(0, oldTimeIndex);
				
				newZone.setDbmEntry(newZone.timerIndexToDBMIndex(timerOld),
						newZone.timerIndexToDBMIndex(timerNew),
						tempZone.getDbmEntry(tempZone.timerIndexToDBMIndex(timerOld), 0));
			}
		}
		
		// Set the upper and lower bounds for the new timers.
		for(LPNTransitionPair pair : newTimers){

			// Get all the upper and lower bounds for the new timers.
			// Get the name for the timer in the i-th column/row of DBM
			//String tranName = indexToTran.get(i).getName();
			String tranName = _lpnList[pair.get_lpnIndex()]
					.getTransition(pair.get_transitionIndex()).getName();
			ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);

			// Get the values of the variables for evaluating the ExprTree.
			HashMap<String, String> varValues = 
				_lpnList[pair.get_lpnIndex()]
						.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()].getVector());

			// Set the upper and lower bound.
			int upper, lower;
			if(delay.getOp().equals("uniform"))
			{
//				ExprTree lowerDelay = delay.getLeftChild();
//				ExprTree upperDelay = delay.getRightChild();

//				lower = (int) lowerDelay.evaluateExpr(varValues);
//				upper = (int) upperDelay.evaluateExpr(varValues);
				
				IntervalPair lowerRange = delay.getLeftChild()
						.evaluateExprBound(varValues, null, null);
				IntervalPair upperRange = delay.getRightChild()
						.evaluateExprBound(varValues, null, null);
				
				// The lower and upper bounds should evaluate to a single
				// value. Yell if they don't.
				if(!lowerRange.singleValue() || !upperRange.singleValue()){
					throw new IllegalStateException("When evaulating the delay, " +
							"the lower or the upper bound evaluated to a range " +
							"instead of a single value.");
				}
				
				lower = lowerRange.get_LowerBound();
				upper = upperRange.get_UpperBound();
				
			}
			else
			{
//				lower = (int) delay.evaluateExpr(varValues);
//
//				upper = lower;
				
				IntervalPair range = delay.evaluateExprBound(varValues, this, null);
				
				lower = range.get_LowerBound();
				upper = range.get_UpperBound();
			}

			newZone.setLowerBoundByLPNTransitionPair(pair, lower);
			newZone.setUpperBoundByLPNTransitionPair(pair, upper);

		}
		
		//newZone.advance();
		// Advance time.
		newZone.advance(localStates);
		
		// Recanonicalize.
		newZone.recononicalize();
		
		newZone.checkZoneMaxSize();
		
		return newZone;
	}
	
	/**
	 * Advances time.
	 */
	private void advance()
	{
		for(int i=0; i<dbmSize(); i++)
		{
			_matrix[dbmIndexToMatrixIndex(0)][dbmIndexToMatrixIndex(i)] = 
				getUpperBoundbydbmIndex(i);
		}
		
	}
	
	/**
	 * Advances time. (This method should replace advance().)
	 * @param localStates
	 */
	private void advance(State[] localStates){
			
		for(LPNTransitionPair ltPair : _indexToTimerPair){

			if(ltPair.equals(LPNTransitionPair.ZERO_TIMER_PAIR)){
				continue;
			}
			
			// Get the new value.
			int newValue = 0;

//			if(ltPair.get_isTimer()){
			if(!(ltPair instanceof LPNContinuousPair)){
				// If the pair is a timer, then simply get the stored largest value.
				
				int index = timerIndexToDBMIndex(ltPair);

				newValue = getUpperBoundbydbmIndex(index);
			}
			else{
				// I fthe pair is a continuous variable, then need to find the 
				// possible largest bound governed by the inequalities.
				newValue = ContinuousUtilities.maxAdvance(this,ltPair, localStates);
			}
			
			
			// In either case (timer or continuous), set the upper bound portion
			// of the DBM to the new value.
			setDbmEntryByPair(LPNTransitionPair.ZERO_TIMER_PAIR, ltPair, newValue);
		}
	}
	
	/**
	 * Copies in the new values needed to add a set of new times.
	 * @param newZone
	 * 			The zone that the values are going to be copied into.
	 * @param tempZone
	 * 			The zone to look up current values of timers.
	 * @param newTimers
	 * 			A collection of the new timers.
	 * @param oldTimers
	 * 			A collection of the older timers.
	 * @param localStates
	 * 			The current state.
	 */
	private void copyTransitions(Zone tempZone, Collection<LPNTransitionPair> newTimers, 
			Collection<LPNTransitionPair> oldTimers, State[] localStates){

		
		// Copy the tempZone to the new zone.
		for(int i=0; i<tempZone.dbmSize(); i++)
		{
			if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			{
				continue;
			}

			// Get the new index of for the timer.
			int newIndexi = i==0 ? 0 : 
				Arrays.binarySearch(_indexToTimerPair, tempZone._indexToTimerPair[i]);
			for(int j=0; j<tempZone.dbmSize(); j++)
			{
				if(!oldTimers.contains(tempZone._indexToTimerPair[j]))
				{
					continue;
				}
				int newIndexj = j==0 ? 0 : 
					Arrays.binarySearch(_indexToTimerPair, tempZone._indexToTimerPair[j]);

				_matrix[dbmIndexToMatrixIndex(newIndexi)]
						[dbmIndexToMatrixIndex(newIndexj)]
								= tempZone.getDbmEntry(i, j);
			}
		}

		// Copy the upper and lower bounds.
		for(int i=1; i<tempZone.dbmSize(); i++)
		{
			if(!oldTimers.contains(tempZone._indexToTimerPair[i]))
			{
				continue;
			}
			setLowerBoundByLPNTransitionPair(tempZone._indexToTimerPair[i], 
					-1*tempZone.getLowerBoundbydbmIndex(i));
			// The minus sign is because _matrix stores the negative of the lower bound.

			setUpperBoundByLPNTransitionPair(tempZone._indexToTimerPair[i],
					tempZone.getUpperBoundbydbmIndex(i));
		}

		// Copy in the new relations for the new timers.
		for(LPNTransitionPair timerNew : newTimers)
		{
			for(LPNTransitionPair timerOld : oldTimers)
			{	
				setDbmEntry(timerIndexToDBMIndex(timerNew),
						timerIndexToDBMIndex(timerOld),
						tempZone.getDbmEntry(0, tempZone.timerIndexToDBMIndex(timerOld)));

				setDbmEntry(timerIndexToDBMIndex(timerOld),
						timerIndexToDBMIndex(timerNew),
						tempZone.getDbmEntry(tempZone.timerIndexToDBMIndex(timerOld), 0));
			}
		}

		// Set the upper and lower bounds for the new timers.
		for(LPNTransitionPair pair : newTimers){

			// Get all the upper and lower bounds for the new timers.
			// Get the name for the timer in the i-th column/row of DBM
			//String tranName = indexToTran.get(i).getName();
			String tranName = _lpnList[pair.get_lpnIndex()]
					.getTransition(pair.get_transitionIndex()).getName();
			ExprTree delay = _lpnList[pair.get_lpnIndex()].getDelayTree(tranName);

			// Get the values of the variables for evaluating the ExprTree.
			HashMap<String, String> varValues = 
					_lpnList[pair.get_lpnIndex()]
							.getAllVarsWithValuesAsString(localStates[pair.get_lpnIndex()].getVector());

			// Set the upper and lower bound.
			int upper, lower;
			if(delay.getOp().equals("uniform"))
			{
				IntervalPair lowerRange = delay.getLeftChild()
						.evaluateExprBound(varValues, null, null);
				IntervalPair upperRange = delay.getRightChild()
						.evaluateExprBound(varValues, null, null);

				// The lower and upper bounds should evaluate to a single
				// value. Yell if they don't.
				if(!lowerRange.singleValue() || !upperRange.singleValue()){
					throw new IllegalStateException("When evaulating the delay, " +
							"the lower or the upper bound evaluated to a range " +
							"instead of a single value.");
				}

				lower = lowerRange.get_LowerBound();
				upper = upperRange.get_UpperBound();

			}
			else
			{
				IntervalPair range = delay.evaluateExprBound(varValues, this, null);

				lower = range.get_LowerBound();
				upper = range.get_UpperBound();
			}

			setLowerBoundByLPNTransitionPair(pair, lower);
			setUpperBoundByLPNTransitionPair(pair, upper);

		}
	}
	
	/**
	 * Finds the maximum amount that time cam advance.
	 * @return
	 * 		value.
	 * 		The maximum amount that time can advance before a timer expires or an inequality changes
	 */
//	private int maxAdvance(LPNTransitionPair contVar, State[] localStates){
//		/*
//		 * Several comments in this function may look like C code. That's because,
//		 * well it is C code from atacs/src/lhpnrsg.c. In particular the
//		 * lhpnCheckPreds method.
//		 */
//		
//		// Get the continuous variable in question.
//		int lpnIndex = contVar.get_lpnIndex();
//		int varIndex = contVar.get_transitionIndex();
//		
//		Variable variable = _lpnList[lpnIndex].getContVar(varIndex);
//		
////		int lhpnCheckPreds(int p,ineqList &ineqL,lhpnStateADT s,ruleADT **rules,
////                int nevents,eventADT *events)
////{
////#ifdef __LHPN_TRACE__
////printf("lhpnCheckPreds:begin()\n");
////#endif
////int min = INFIN;
////int newMin = INFIN;
//		
//		int min = INFINITY;
//		int newMin = INFINITY;
//		
////int zoneP = getIndexZ(s->z,-2,p);
////for(unsigned i=0;i<ineqL.size();i++) {
//// if(ineqL[i]->type > 4) {
////   continue;
//// }
////#ifdef __LHPN_PRED_DEBUG__
//// printf("Zone to check...\n");
//// printZ(s->z,events,nevents,s->r);
//// printf("Checking ...");
//// printI(ineqL[i],events);
//// printf("\n");
////#endif
//// if(ineqL[i]->place == p) {
//		
//		// Get all the inequalities that reference the variable of interest.
//		ArrayList<InequalityVariable> inequalities = variable.getInequalities();
//		
//		for(InequalityVariable ineq : inequalities){
//		
////   ineq_update(ineqL[i],s,nevents);
//			
//			// Update the inequality variable.
//			int ineqValue = ineq.evaluate(localStates[varIndex], this);
//			
////   if(ineqL[i]->type <= 1) {
////     /* Working on a > or >= ineq */
//			
//			if(ineq.get_op().equals(">") || ineq.get_op().equals(">=")){
//				// Working on a > or >= ineq
//			
////     if(s->r->bound[p-nevents].current > 0) {
//				
//				// If the rate is positive.
//				if(getCurrentRate(contVar) > 0){
//				
////       if(s->m->state[ineqL[i]->signal]=='1') {
//					if(ineqValue != 0){
////         if(s->z->matrix[zoneP][0] <
////            chkDiv(ineqL[i]->constant,
////                   s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 1a\n");
////#endif
////           
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 1.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
//						
//						if(getDbmEntry(0, contVar.get_transitionIndex())
//								< chkDiv(ineq.getConstant(), getCurrentRate(contVar), false)){
//							// CP: case 1a.
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//							System.err.println("maxAdvance: Impossible case 1.");
//						}
//							
////         }
////         else if((-1)*s->z->matrix[0][zoneP] >
////                 chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 2a\n");
////#endif
////           newMin = chkDiv(events[p]->urange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						else if ((-1)*getDbmEntry(contVar.get_transitionIndex(),0)
//								> chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// CP : case 2a
//							newMin = INFINITY;
//							
//						}
//							
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 3a\n");
////#endif
////           newMin = chkDiv(events[p]->urange,
////                           s->r->bound[p-nevents].current,'F');
////         }
////       }
//						else{
//							// Straddle case
//							// CP : case 3a
//							newMin = INFINITY;
//						}
//					}
//					else{
////       else {
////         if(s->z->matrix[zoneP][0] <
////            chkDiv(ineqL[i]->constant,
////                   s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 4a -- min: %d\n",chkDiv(ineqL[i]->constant,s->r->bound[p-nevents].current,'F'));
////#endif
////           newMin = chkDiv(ineqL[i]->constant,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						if(getDbmEntry(contVar.get_transitionIndex(), 0)
//								< chkDiv(ineq.getConstant(), getCurrentRate(contVar), false)){
//							// CP: case 4a -- min
//							newMin = chkDiv(ineq.getConstant(), 
//									getCurrentRate(contVar), false);
//						}
//						
////         else if((-1)*s->z->matrix[0][zoneP] >
////                 chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 5a\n");
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 3.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						else if((-1)*getDbmEntry(contVar.get_transitionIndex(),0)
//								< chkDiv(ineq.getConstant(), getCurrentRate(contVar), false)){
//							// Impossible case 3.
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//							System.err.print("maxAdvance : Impossible case 3.");
//						}
//						
////         else {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 6a -- min: %d\n",s->z->matrix[zoneP][0]);
////#endif
////           /* straddle case */
////           newMin = s->z->matrix[zoneP][0];
////         }
////       }
////     }
//						else{
//							// CP : cas 6a
//							// straddle case
//							newMin = getDbmEntry(0,contVar.get_transitionIndex());
//						}
//					}
//				}
//					
////     else {
////       /* warp <= 0 */
//				
//				else{
//					// warp <= 0.
//				
////       if(s->m->state[ineqL[i]->signal]=='1') {
//					
//					if( ineqValue != 1){
//						
////         if(s->z->matrix[0][zoneP] <
////            (-1)*chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 7a\n");
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 2.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						if(getDbmEntry(contVar.get_transitionIndex(),0)
//							< (-1)*chkDiv(ineq.getConstant(),
//									getCurrentRate(contVar), false)){
//							// CP: case 7a.
//							newMin = getDbmEntry(0,contVar.get_transitionIndex());
//							System.err.println("Warining: impossible case 2a found.");
//						}
//						
////         else if((-1)*s->z->matrix[zoneP][0] >
////                 (-1)*chkDiv(ineqL[i]->constant,
////                             s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 8a\n");
////#endif
////           newMin = chkDiv(ineqL[i]->constant,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						else if((-1)*getDbmEntry(0, contVar.get_transitionIndex())
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// Impossible case 8a.
//							newMin = chkDiv(ineq.getConstant(), 
//									getCurrentRate(contVar), false);
//						}
//						
////         else {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 9a\n");
////#endif
////           /* straddle case */
////           newMin = s->z->matrix[zoneP][0];
////         }
////       }
//						
//						else{
//							// straddle case
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//						}
//						
//					}
////       else {
//					
//					else{
//					
////         if(s->z->matrix[0][zoneP] <
////            (-1)*chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 10a\n");
////#endif
////           newMin = chkDiv(events[p]->lrange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						if(getDbmEntry(contVar.get_transitionIndex(),0)
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// CP: case 10a.
//							newMin = INFINITY;
//						}
//						
//						
////         else if((-1)*s->z->matrix[zoneP][0] >
////                 (-1)*chkDiv(ineqL[i]->constant,
////                             s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 11a\n");
////	      printf("z=%d c=%d b=%d\n",
////		     s->z->matrix[zoneP][0],
////		     ineqL[i]->constant,
////		     s->r->bound[p-nevents].current);
////		     
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 4.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						else if((-1)*getDbmEntry(0, contVar.get_transitionIndex())
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// CP: case 7a.
//							newMin = getDbmEntry(0,contVar.get_transitionIndex());
//							System.err.println("maxAdvance : Impossible case 4.");
//						}
//						
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 12a\n");
////#endif
////           newMin = chkDiv(events[p]->lrange,
////                           s->r->bound[p-nevents].current,'F');
////         }
////       }
////     }
////   }
//						else{
//							// straddle case
//							newMin = INFINITY;
//						}
//					}
//				}	
//			}
////   else {
////     /* Working on a < or <= ineq */
//			else{
//				// Working on a < or <= ineq
//			
////     if(s->r->bound[p-nevents].current > 0) {
//				
//				if(getUpperBoundForRate(contVar) > 0){
//				
////       if(s->m->state[ineqL[i]->signal]=='1') {
//					
//					if(ineqValue != 0){
//					
////         if(s->z->matrix[zoneP][0] <
////            chkDiv(ineqL[i]->constant,
////                   s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 1b -- min: %d\n",chkDiv(ineqL[i]->constant,s->r->bound[p-nevents].current,'F'));
////#endif
////           newMin = chkDiv(ineqL[i]->constant,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						if(getDbmEntry(0, contVar.get_transitionIndex())
//								< (-1)*chkDiv(ineq.getConstant(), 
//										getCurrentRate(contVar), false)){
//							// CP: case 1b -- min.
//							newMin = chkDiv(ineq.getConstant(),
//									getCurrentRate(contVar), false);
//						}
//						
//						
////         else if((-1)*s->z->matrix[0][zoneP] >
////                 chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 2b\n");
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 5.\n");
////#endif
////           newMin = chkDiv(events[p]->urange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						if((-1)*getDbmEntry(contVar.get_transitionIndex(), 0)
//								< chkDiv(ineq.getConstant(), getCurrentRate(contVar),false)){
//							// CP: case 2b.
//							newMin = INFINITY;
//							System.err.println("Warning : Impossible case 5.");
//						}
//						
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 3b -- min: %d\n",s->z->matrix[zoneP][0]);
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
////       }
//						else{
//							//straddle case
//							newMin = getDbmEntry(0,contVar.get_transitionIndex());
//						}
//						
//					}
////       else {
//					
//					else{
//						
////         if(s->z->matrix[zoneP][0] <
////            chkDiv(ineqL[i]->constant,
////                   s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 4b\n");
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 7.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						if(getDbmEntry(0, contVar.get_transitionIndex())
//								< chkDiv(ineq.getConstant(), 
//										getCurrentRate(contVar), false)){
//							// CP: case 4b.
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//							System.err.println("maxAdvance : Impossible case 7.");
//						}
//						
////         else if((-1)*s->z->matrix[0][zoneP] >
////                 chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 5b\n");
////#endif
////           newMin = chkDiv(events[p]->urange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						else if((-1)*getDbmEntry(contVar.get_transitionIndex(), 0)
//								< chkDiv(ineq.getConstant(), 
//										getCurrentRate(contVar), false)){
//							// CP: case 5b.
//							newMin = INFINITY;
//						}
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 6b\n");
////#endif
////           newMin = chkDiv(events[p]->urange,
////                           s->r->bound[p-nevents].current,'F');
////         }
////       }
////     }
//						else{
//							// straddle case
//							// CP : case 6b
//							newMin = INFINITY;
//						}
//					}	
//					
//				}
////     else {
////       /* warp <= 0 */
//				
//				else {
//					// warp <=0 
//					
////       if(s->m->state[ineqL[i]->signal]=='1') {
//					if(ineqValue != 0){
//					
////         if(s->z->matrix[0][zoneP] <
////            (-1)*chkDiv(ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 7b\n");
////#endif
////           newMin = chkDiv(events[p]->lrange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						
//						if(getDbmEntry(contVar.get_transitionIndex(), 0)
//								< (-1)*chkDiv(ineq.getConstant(), 
//										getCurrentRate(contVar), false)){
//							// CP: case 7b.
//							newMin = INFINITY;
//						}
//						
////         else if((-1)*s->z->matrix[zoneP][0] >
////                 (-1)*chkDiv(ineqL[i]->constant,
////                             s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 8b\n");
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 8.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						else if((-1)*getDbmEntry(0, contVar.get_transitionIndex())
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// CP: case 8b.
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//							System.err.println("Warning : Impossible case 8.");
//						}
//						
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 9b\n");
////#endif
////           newMin = chkDiv(events[p]->lrange,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						else {
//							// straddle case
//							// CP: case 9b.
//							newMin = INFINITY;
//						}
////       }
//						
//					}
//					
////       else {
//					
//					else {
//					
////         if(s->z->matrix[0][zoneP] <
////            chkDiv((-1)*ineqL[i]->constant,
////                        s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 10b\n");
////           printf("zone: %d const: %d warp: %d chkDiv: %d\n",s->z->matrix[0][zoneP],ineqL[i]->constant,s->r->bound[p-nevents].current,chkDiv((-1)*ineqL[i]->constant,s->r->bound[p-nevents].current,'F'));
////#endif
////#ifdef __LHPN_WARN__
////           warn("checkPreds: Impossible case 6.\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						if(getDbmEntry(contVar.get_transitionIndex(),0)
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar), false)){
//							// CP: case 10b.
//							newMin = getDbmEntry(0,contVar.get_transitionIndex());
//							System.err.println("Warning : Impossible case 6");
//						}
//						
////         else if((-1)*s->z->matrix[zoneP][0] >
////                 (-1)*chkDiv(ineqL[i]->constant,
////                             s->r->bound[p-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 11b\n");
////#endif
////           newMin = chkDiv(ineqL[i]->constant,
////                           s->r->bound[p-nevents].current,'F');
////         }
//						else if((-1)*getDbmEntry(0,contVar.get_transitionIndex())
//								< (-1)*chkDiv(ineq.getConstant(),
//										getCurrentRate(contVar),false)){
//							// CP: case 7b.
//							newMin = chkDiv(ineq.getConstant(), getCurrentRate(contVar),false);
//						}
//						
//						
////         else {
////           /* straddle case */
////#ifdef __LHPN_PRED_DEBUG__
////           printf("CP:case 12b\n");
////#endif
////           newMin = s->z->matrix[zoneP][0];
////         }
//						
//						
//						else {
//							// straddle case
//							// CP : case 12b
//							newMin = getDbmEntry(0, contVar.get_transitionIndex());
//						}
//						
////       }
////     }
////   }
//// }
//// if(newMin < min) {
////   min = newMin;
//// }
//						
//					}
//				}	
//			}
//			// Check if the value can be lowered.
//			if(newMin < min){
//				min = newMin;
//			}
//		}
////}
////
////#ifdef __LHPN_PRED_DEBUG__
////printf("Min leaving checkPreds for %s: %d\n",events[p]->event,min);
////#endif
////return min;
////}
//
//		return min;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Zone clone()
	{
		// TODO: Check if finished.
		
		Zone clonedZone = new Zone();
		
		clonedZone._matrix = new int[this.matrixSize()][this.matrixSize()];
		
		for(int i=0; i<this.matrixSize(); i++)
		{
			for(int j=0; j<this.matrixSize(); j++)
			{
				clonedZone._matrix[i][j] = this._matrix[i][j];
			}
		}
		
		clonedZone._indexToTimerPair = Arrays.copyOf(_indexToTimerPair, _indexToTimerPair.length);
		
		clonedZone._hashCode = this._hashCode;
		
		clonedZone._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		
		clonedZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		return clonedZone;
	}
	
	/**
	 * Restricts the lower bound of a timer.
	 * 
	 * @param timer
	 * 			The timer to tighten the lower bound.
	 */
	private void restrictTimer(int timer)
	{
		//int dbmIndex = Arrays.binarySearch(_indexToTimer, timer);
		
		_matrix[dbmIndexToMatrixIndex(timer)][dbmIndexToMatrixIndex(0)]
		                                            = getLowerBoundbydbmIndex(timer);
	}
	
	/**
	 * Restricts the lower bound of a continuous variable. Also checks fixes
	 * the upper bound to be at least as large if needed. This method
	 * is usually used as a result of an event firing.
	 * @param ltContPair
	 * 		The index of the continuous variable to restrict.
	 * @param constant
	 * 		The constant value of the inequality event that is being used to update
	 * 		the variable indexed by ltContPair.
	 * 
	 */
	private void restrictContinuous(LPNContinuousPair ltContPair, int constant){
		
		// It will be quicker to get the DBM index for the ltContPair one time.
		int variableIndex = timerIndexToDBMIndex(ltContPair);
		int zeroIndex = timerIndexToDBMIndex(LPNTransitionPair.ZERO_TIMER_PAIR);
		
		// Set the lower bound the variable (which is the DBM[variabl][0] entry.
		// Note : the lower bound in the zone is actually the negative of the lower
		// bound hence the -1 on the warpValue.
		setDbmEntry(variableIndex, zeroIndex, ContinuousUtilities.chkDiv(-1*constant, ltContPair.getCurrentRate(), true));
		
		// Check if the upper bound needs to be advanced and advance it if necessary.
		if(getDbmEntry(zeroIndex, variableIndex) < ContinuousUtilities.chkDiv(constant, ltContPair.getCurrentRate(), true)){
			// If the upper bound in the zones is less than the new restricting value, we
			// must advance it for the zone to remain consistent.
			setDbmEntry(zeroIndex, variableIndex, ContinuousUtilities.chkDiv(constant, ltContPair.getCurrentRate(), true));
		}
	}
	
	/**
	 * Restricts the continuous variables in the zone according to the inequalities in a set of events.
	 * @param eventSet
	 * 			A set of inequality events. Does nothing if the event set does not contian inequalities.
	 */
	private void restrictContinuous(EventSet eventSet){
		// Check that the eventSet is a set of Inequality events.
		if(!eventSet.isInequalities()){
			// If the eventSet is not a set of inequalities, do nothing.
			return;
		}
		
		// Restrict the variables according to each of the inequalities in the eventSet.
		for(Event e : eventSet){
			// Get the inequality.
			InequalityVariable iv = e.getInequalityVariable();
			
			// Extract the variable. I will assume the inequality only depends on a single 
			// variable.
			Variable x = iv.getContVariables().get(0);
			
			// Extract the index.
			int lpnIndex = iv.get_lpn().getLpnIndex();
			
			// Extract the variable index.
//			DualHashMap<String, Integer> variableIndexMap = _lpnList[lpnIndex].getVarIndexMap();
			DualHashMap<String, Integer> variableIndexMap = _lpnList[lpnIndex].getContinuousIndexMap();
			int  variableIndex = variableIndexMap.getValue(x.getName());
			
			// Package it up for referencing.
//			LPNContinuousPair ltContPair = new LPNContinuousPair(lpnIndex, variableIndex, 0);
			LPNContinuousPair ltContPair = new LPNContinuousPair(lpnIndex, variableIndex);
			
			// Need the current rate for the varaible, grab the stored LPNContinuousPair.
			int zoneIndex = Arrays.binarySearch(_indexToTimerPair, ltContPair);
			if(zoneIndex > 0){
				ltContPair = (LPNContinuousPair) _indexToTimerPair[zoneIndex];
			}
			
			
			//setDbmEntry(zoneIndex, 0, -ContinuousUtilities.chkDiv(iv.getConstant(), ltContPair.getCurrentRate(), true));
			
			// Perform the restricting.
			restrictContinuous(ltContPair, iv.getConstant());
		}
	}
	
	/**
	 * Returns a zone that is the result from resticting the this zone according to a list of firing event inequalities.
	 * @param eventSet
	 * 		The list of inequalities that are firing.
	 * @return
	 * 		The new zone that is the result of restricting this zone according to the firing of the inequalities
	 * 		in the eventSet.
	 */
	public Zone getContinuousRestrictedZone(EventSet eventSet){
		// Make a new copy of the zone.
		Zone z = this.clone();
		
		z.restrictContinuous(eventSet);
		
		z.recononicalize();
		
		return z;
	}
	
	/**
	 * The list of enabled timers.
	 * @return
	 * 		The list of all timers that have reached their lower bounds.
	 */
	public List<Transition> getEnabledTransitions()
	{
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
		
		// Check if the timer exceeds its lower bound staring with the first nonzero
		// timer.
		for(int i=1; i<_indexToTimerPair.length; i++)
		{
			if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i))
			{
				enabledTransitions.add(_lpnList[_indexToTimerPair[i].get_lpnIndex()]
						.getTransition(_indexToTimerPair[i].get_transitionIndex()));
			}
		}
		
		return enabledTransitions;
	}
	
	/**
	 * Gives the list of enabled transitions associated with a particular LPN.
	 * @param LpnIndex
	 * 			The Index of the LPN the Transitions are a part of.
	 * @return
	 * 			A List of the Transitions that are enabled in the LPN given by the index.
	 */
	public List<Transition> getEnabledTransitions(int LpnIndex){
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

		// Check if the timer exceeds its lower bound staring with the first nonzero
		// timer.
		for(int i=1; i<_indexToTimerPair.length; i++)
		{
			if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i))
			{
				LPNTransitionPair ltPair = _indexToTimerPair[i];
				
				if( ltPair.get_lpnIndex() == LpnIndex){

					enabledTransitions.add(_lpnList[ltPair.get_lpnIndex()]
							.getTransition(ltPair.get_transitionIndex()));
				}
			}
		}
		
		return enabledTransitions;
	}
	
	/**
	 * Find the next possible events.
	 * 
	 * @param LpnIndex
	 * 		The index of the LPN that is of interest.
	 * @param localState
	 * 		The state associated with the LPN indexed by LpnIndex.
	 * @return
	 * 		LpnTranList is populated with a list of 
	 * 		EventSets pertaining to the LPN with index LpnIndex. An EventSet can
	 * 		either contain a transition to
	 * 		fire or set of inequalities to change sign. 
	 */
	public LpnTranList getPossibleEvents(int LpnIndex, State localState){
		LpnTranList result = new LpnTranList();
		
		// Look through the timers and continuous variables. For the timers
		// determine if they are ready to fire. For the continuous variables,
		// look up the associated inequalities and see if any of them are ready
		// to fire.
//		for(LPNTransitionPair ltPair : _indexToTimerPair){
//			
//		}
		
		// We do not need to consider the zero timer, so start the 
		// for loop at i=1 and not i=0.
		for(int i=1; i<_indexToTimerPair.length; i++){
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			// The enabled events are grouped with the LPN that they affect. So if 
			// this pair does not belong to the current LPN under consideration, skip
			// processing it.
			if(ltPair.get_lpnIndex() != LpnIndex){
				continue;
			}
			
			// If the index refers to a timer (and not a continuous variable) and has exceeded its lower bound,
			// then add the transition.
			if(!(ltPair instanceof LPNContinuousPair)){
					//result.add(_lpnList[ltPair.get_lpnIndex()].getTransition(ltPair.get_transitionIndex()));
				
				// The index refers to a timer. Now check if time has advanced
				// far enough for the transition to fire.
				if(getDbmEntry(0, i) >= -1 * getLowerBoundbydbmIndex(i)){

					Event e = new Event(_lpnList[ltPair.get_lpnIndex()].getTransition(ltPair.get_transitionIndex()));
					result = addSetItem(result, e, localState);
				}
			}
			
			else{
				// The index refers to a continuous variable. So check all the inequalities for inclusion.
				Variable contVar = _lpnList[ltPair.get_lpnIndex()].getContVar(ltPair.get_transitionIndex());
				for(InequalityVariable iv : contVar.getInequalities()){
					
					// Check if the inequality can change.
					if(ContinuousUtilities.inequalityCanChange(this, iv, localState)){
						result = addSetItem(result, new Event(iv), localState);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Addes or removes items as appropriate to update the current
	 * lsit of possible events. Note the type LpnTranList extends
	 * LinkedList<Transition>. The type EventSet extends transition
	 * specifically so that objects of EventSet type can be place in
	 * this list.
	 * @param EventList
	 * 			The list of possible events.
	 */
	private LpnTranList addSetItem(LpnTranList E, Event e, State s){
//		void lhpnAddSetItem(eventSets &E,lhpnEventADT e,ineqList &ineqL,lhpnZoneADT z,
//                lhpnRateADT r,eventADT *events,int nevents,
//	    lhpnStateADT cur_state)
//{
//int rv1l,rv1u,rv2l,rv2u,iZ,jZ;
		
		// Note the LPNTranList plays the role of the eventSets.
		
		int rv1l=0, rv1u=0, rv2l=0, rv2u=0, iZ, jZ;
		
//
//#ifdef __LHPN_TRACE__
//printf("lhpnAddSetItem:begin()\n");
//#endif
//#ifdef __LHPN_ADD_ACTION__
//printf("Event sets entering:\n");
//printEventSets(E,events,ineqL);
//printf("Examining event: ");
//printLhpnEvent(e,events,ineqL);
//#endif
//eventSet* eSet = new eventSet();
//eventSets* newE = new eventSets();
//bool done = false;
//bool possible = true;
//eventSets::iterator i;
		
		// Create the new LpnTranlist for holding the events.
		EventSet eSet = new EventSet();
		LpnTranList newE = new LpnTranList();
		boolean done = false;
		boolean possible = true;
		
//
//if ((e->t == -1) && (e->ineq == -1)) {
		if(e.isRate()){
//eSet->insert(e);
			eSet.add(e);
//newE->push_back(*eSet);
			newE.addLast(eSet);
//E.clear();
//E = *newE;
			
			// The previous two commands act to pass the changes of E 
			// back out of the functions. So returning the new object
			// is suficient.
			
//#ifdef __LHPN_ADD_ACTION__
//printf("Event sets leaving:\n");
//printEventSets(E,events,ineqL);
//#endif
//#ifdef __LHPN_TRACE__
//printf("lhpnAddSetItem:end()\n");
//#endif
//return;
//}
			return newE;
			
		}
//if (e->t == -1) {
		if(e.isInequality()){
//ineq_update(ineqL[e->ineq],cur_state,nevents);
			
			// Is this necessary, or even correct to update the inequalities.
			System.out.println("Note the inequality is not being updated before in addSetItem");
			
			// In this case the Event e represents an inequality.
			InequalityVariable ineq = e.getInequalityVariable();
			
//rv2l = chkDiv(-1 * ineqL[e->ineq]->constant,
//              r->bound[ineqL[e->ineq]->place-nevents].current,'C');
//rv2u = chkDiv(ineqL[e->ineq]->constant,
//              r->bound[ineqL[e->ineq]->place-nevents].current,'C');
//iZ = getIndexZ(z,-2,ineqL[e->ineq]->place);
			
			// Need to extract the rate.
			// To do this, I'll create the indexing object.
			Variable v = ineq.getContVariables().get(0);
			// Find the LPN.
			int lpnIndex = ineq.get_lpn().getLpnIndex();
			
			int varIndex = _lpnList[lpnIndex].
					getContinuousIndexMap().getValue(v.getName());
			
			// Package it all up.
//			LPNTransitionPair ltPair = 
//					new LPNTransitionPair(lpnIndex, varIndex, false);
			// Note : setting the rate is not necessary since 
			// this is only being used as aan index.
//			LPNContinuousPair ltPair = 
//					new LPNContinuousPair(lpnIndex, varIndex, 0);
			LPNContinuousPair ltPair = new LPNContinuousPair(lpnIndex, varIndex);
			
			rv2l = ContinuousUtilities.chkDiv(-1*ineq.getConstant(),
					getCurrentRate(ltPair), true);
			rv2u = ContinuousUtilities.chkDiv(ineq.getConstant(),
					getCurrentRate(ltPair), true);
			iZ = Arrays.binarySearch(_indexToTimerPair, ltPair);
//} else {
		}
		else{
//iZ = getIndexZ(z,-1,e->t);
//}
			// In this case, the event is a transition.
			Transition t = e.getTransition();
			
			int lpnIndex = t.getLpn().getLpnIndex();
			
			int tranIndex = t.getIndex();
			
			// Package the results.
//			LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, tranIndex, true);
			LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, tranIndex);
			
			iZ = Arrays.binarySearch(_indexToTimerPair, ltPair);
		}
		
		
//for(i=E.begin();i!=E.end()&&!done;i++) {
		
		// Recall that E contains the events sets which are inherited from the Transition class
		// so they can be placed in an LpnTranList. So consider each event set.
		for(Transition es : E){
			
			if(!(es instanceof EventSet)){
				// This collection should contain event sets, not transitions.
				throw new IllegalArgumentException("The eventSet was a Transition object not an EventSet object.");
			}
		
			EventSet eventSet = (EventSet) es;
			
			if(done){
				// Copy any remaining sets into newE.
				newE.add(eventSet.clone());
				break;
			}
			
//eventSet* workSet = new eventSet();
//*workSet = copyEventSet(*i,events,ineqL);
			
			EventSet workSet = eventSet.clone();
			
//for(eventSet::iterator j=workSet->begin();j!=workSet->end();j++) {


			// Get an iterator for the elements in the workset.
			//Iterator<Event> workSetIterator = workSet.iterator();
			//for(Event oldEvent : workSet){
			//while(workSetIterator.hasNext()){
			for(Event oldEvent : eventSet){	
				//Event oldEvent = workSetIterator.next();
			
//  if (((*j)->t == -1) && ((*j)->ineq == -1)) continue;
				
				// For matching code with atacs, note that oldEvent is 'j'.
				if(oldEvent.isRate()){
					continue;
				}
				
//  if ((*j)->t == -1) {
				
				if(oldEvent.isInequality()){
				
				
//ineq_update(ineqL[(*j)->ineq],cur_state,nevents);
//    rv1l = chkDiv(-1 * ineqL[(*j)->ineq]->constant,
//                  r->bound[ineqL[(*j)->ineq]->place-nevents].current,'C');
//    rv1u = chkDiv(ineqL[(*j)->ineq]->constant,
//                  r->bound[ineqL[(*j)->ineq]->place-nevents].current,'C');
//    jZ = getIndexZ(z,-2,ineqL[(*j)->ineq]->place);
//  } else {
					
					// Again, is it necessary to do an update here?
					System.out.println("Note the inequality is not being updated before in addSetItem");
					
					// In this case the Event oldEvent represents an inequality.
					InequalityVariable ineq = oldEvent.getInequalityVariable();
					
					//Extract variable.
					Variable v = ineq.getContVariables().get(0);
					// Find the LPN.
					int lpnIndex = ineq.get_lpn().getLpnIndex();
					
					int varIndex = _lpnList[lpnIndex].
							getContinuousIndexMap().getValue(v.getName());
					
					// Package it all up.
//					LPNTransitionPair ltPair = 
//							new LPNTransitionPair(lpnIndex, varIndex, false);
					// Note : setting the rate is not necessary since this is
					// only being used as an index.
//					LPNContinuousPair ltPair = 
//							new LPNContinuousPair(lpnIndex, varIndex, 0);
					LPNContinuousPair ltPair = 
							new LPNContinuousPair(lpnIndex, varIndex);
					
					rv1l = ContinuousUtilities.chkDiv(
							-1 * oldEvent.getInequalityVariable().getConstant(), 
							getCurrentRate(ltPair), true);
					rv1u = ContinuousUtilities.chkDiv(
							oldEvent.getInequalityVariable().getConstant(), 
							getCurrentRate(ltPair), true);
					
					jZ = Arrays.binarySearch(_indexToTimerPair, ltPair);
					
				}
				else{
					
//    jZ = getIndexZ(z,-1,(*j)->t);
//  }
					// In this case, the event is a transition.
					Transition t = oldEvent.getTransition();
					
					// Create the indexing object. (See the Abstraction Function protion at the
					// top of the class for more details.)
					int lpnIndex = t.getLpn().getLpnIndex();
					
					int tranIndex = t.getIndex();
					
					// Package the results.
//					LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, tranIndex, true);
					LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, tranIndex);
					
					jZ = Arrays.binarySearch(_indexToTimerPair, ltPair);
				}
					
//  /* Both actions are predicate changes */
//  if((e->t == -1) && ((*j)->t == -1)) {
				
				// Both actions are predicate changes.
				if(e.isInequality() && oldEvent.isInequality()){
				
//    /* Both predicates are on the same variable */
//    if(ineqL[(*j)->ineq]->place == ineqL[e->ineq]->place) {
					
					// Extract the variable lists.
					ArrayList<Variable> newList = e.getInequalityVariable()
							.getContVariables();
					ArrayList<Variable> oldList = oldEvent.getInequalityVariable()
							.getContVariables();
					
					// Issue a warning if one of the lists has more than a single
					// variable.
					if(newList.size() > 1 && oldList.size() > 1){
						System.err.println("Warning: One of the inequalities " + e +
								" " + oldEvent + " refers to more than one variable");
					}
					
					// Both inequalities are on the same varaibles.
					if(newList.get(0).equals(oldList.get(0))){
						
//      if (rv1l > rv2l) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 1a\n");
//#endif
//        possible = false;
						
						if(rv1l > rv2l){
							possible = false;
						}
						
						
//      } else if (rv2l > rv1l) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 1b\n");
//        printf("Adding to erase list: \n");
//        printLhpnEvent(*j,events,ineqL);
//#endif
//        workSet->erase(*j);
						
						else if (rv2l > rv1l){
							workSet.remove(oldEvent);
						}
						
//      } else if (rv1u > rv2u) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 1c\n");
//        printf("Adding to erase list: \n");
//        printLhpnEvent(*j,events,ineqL);
//#endif
//        workSet->erase(*j);
						
						else if(rv1u > rv2u){
							workSet.remove(oldEvent);
						}
						
//      } else {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 1d\n");
//#endif
//        workSet->insert(e);
//        done = true;
						
						else{
							workSet.add(e);
							done = true;
						}
						
//      }
//
//    } 
						
					}
					
//    /* Predicates are on different variables */
//    else {
					
					// Inequalities are on different variables.
					else {
						
						// TODO : Check that the indecies must be reversed. I believe
						// they do since I think my representation is the transpose
						// of atacs. This will affect all the following statements.
						
					
//      if(rv1l > rv2l + z->matrix[iZ][jZ]) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 2\n");
//#endif
//        possible = false;
						
						if(rv1l > rv2l + getDbmEntry(jZ, iZ)){
							possible = false;
						}
//      }
//      else if(rv2l > rv1l + z->matrix[jZ][iZ]) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 3\n");
//        printf("Adding to erase list: ");
//        printLhpnEvent(*j,events,ineqL);
//#endif
//        workSet->erase(*j);
//        //	    workSet->insert(e);
						
						
						else if(rv2l > rv1l + getDbmEntry(iZ, jZ)){
							workSet.remove(oldEvent);
						}
						
//      }
//      else if(rv1u > z->matrix[iZ][0] + z->matrix[jZ][iZ]) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 4\n");
//        printf("Adding to erase list: \n");
//        printLhpnEvent(*j,events,ineqL);
//#endif
//        workSet->erase(*j);
//        //workSet->insert(e);
						
						else if(rv1u > getDbmEntry(0, iZ) + getDbmEntry(iZ, jZ)){
							workSet.remove(oldEvent);
						}
						
//      }
//      else if(rv2u > z->matrix[jZ][0] + z->matrix[iZ][jZ]) {
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 5\n");
//#endif
//        possible = false;
						
						else if (rv2u > getDbmEntry(0, jZ) + getDbmEntry(jZ, iZ)){
							possible = false;
						}
						
//      }
//      else if((rv1l == rv2l + z->matrix[iZ][jZ]) &&
//              (rv2l == rv1l + z->matrix[jZ][iZ]) &&
//              (rv1u == rv2u + z->matrix[jZ][iZ]) &&
//              (rv2u == rv1u + z->matrix[iZ][jZ])) {
//        workSet->insert(e);
//        done = true;
//#ifdef __LHPN_ADD_ACTION__
//        printf("Add action case 6\n");
						
						else if(rv1l == rv2l + getDbmEntry(jZ, iZ) &&
								rv2l == rv1l + getDbmEntry(iZ, jZ) &&
								rv1u == rv2u + getDbmEntry(iZ, jZ) &&
								rv2u == rv1u + getDbmEntry(jZ, iZ)){
							
							workSet.add(e);
							
						}
						
//#endif
//      }
//    }
						
					}
					
				}
//    /* New action is predicate change, old is transition firing (case 3) */
//  } else if((e->t == -1) && ((*j)->t != -1)) {
					
				// New action is an inequality and the old even tis a transition (case 3).
				else if (e.isInequality() && oldEvent.isTransition()){

//    if (rv2l > -events[(*j)->t]->lower + z->matrix[jZ][iZ]) {
//#ifdef __LHPN_ADD_ACTION__
//      printf("Add action case 6\n");
//      printf("Adding to erase list: \n");
//      printLhpnEvent(*j,events,ineqL);
//#endif
//      workSet->erase(*j);
					
					if(rv2l > -1* getLowerBoundbyTransition(e.getTransition()) + getDbmEntry(iZ, jZ)){
						//Probably can change this to refer directly to the zone.
						workSet.remove(oldEvent);
					}
					
//    } else if (rv2u > z->matrix[jZ][0] + z->matrix[iZ][jZ]) {
//#ifdef __LHPN_ADD_ACTION__
//      printf("Add action case 7\n");
//#endif
//      possible = false;
					
					else if(rv2u > getDbmEntry(0, jZ) + getDbmEntry(jZ, iZ)){
						possible = false;
					}
					
//    }
				}
				
				
//    /* TODO: One more ugly case, is it needed? */
//    /* New action is transition firing, old is predicate change (case 4) */
//  } else if((e->t != -1) && ((*j)->t == -1)) {
				
				// New event is a transition firing, old event is an inequality change (case 4).
				else if(e.isTransition() && oldEvent.isInequality()){
					
//    if (rv1l > (((-1)*(events[e->t]->lower)) + z->matrix[iZ][jZ])) {
//#ifdef __LHPN_ADD_ACTION__
//      printf("Add action case 8\n");
//#endif
//      possible = false;
					
					if(rv1l > (-1) * getLowerBoundbyTransition(e.getTransition()) + getDbmEntry(jZ, iZ)){
						possible = false;
					}
					
//    } else if (rv1u > z->matrix[iZ][0] + z->matrix[jZ][iZ]) {
//#ifdef __LHPN_ADD_ACTION__
//      printf("Add action case 9\n");
//      printf("Adding to erase list: \n");
//      printLhpnEvent(*j,events,ineqL);
//#endif
//      workSet->erase(*j);
					
					
					else if (rv1u > getDbmEntry(0, jZ) + getDbmEntry(iZ, jZ)){
						workSet.remove(oldEvent);
					}
					
//    } 
//    /* TODO: one more ugly case, is it needed? */
//  }
//}
					// I guess this one wasn't needed since it is not found in atacs.
					
				}			
			}
				
//if (!(workSet->empty())) {
//  newE->push_back(*workSet);
//}
//}
			
			if(!(workSet.isEmpty())){
				newE.add(workSet);
			}
			
		}
			
//#ifdef __LHPN_ADD_ACTION__
//printf("At new for loop...\n");
//#endif
//for(;i!=E.end();i++) {
//eventSet* addSet = new eventSet();
//*addSet = copyEventSet(*i,events,ineqL);
//newE->push_back(*addSet);
//}
		
		// This part of the code is essentially copying the all the old event set into the new event set.
		// There might be a way around doing this by working directly on the set to begin with.
		// Moved to being done at the begining of the previous block.
//		for(Transition T : E){
//			if(!(T instanceof EventSet)){
//				// This collection should contain event sets, not transitions.
//				throw new IllegalArgumentException("The eventSet was a Transition object not an EventSet object.");
//			}
//			EventSet es = (EventSet) T;
//			
//			newE.add(es.clone());
//		}
		
		
//#ifdef __LHPN_ADD_ACTION__
//printf("At done & possible...\n");
//#endif
//if(!done && possible) {
//eSet->insert(e);
//newE->push_back(*eSet);
//}
		
		if(!done && possible){
			eSet.add(e);
			newE.add((Transition) eSet);
		}
		
//E.clear();
//E = *newE;
		
		E = newE;
		
//#ifdef __LHPN_ADD_ACTION__
//printf("Event sets leaving:\n");
//printEventSets(E,events,ineqL);
//#endif
//#ifdef __LHPN_TRACE__
//printf("lhpnAddSetItem:end()\n");
//#endif
//}
		return E;
	}
	
	/**
	 * Updates the continuous variables that are set by firing a transition.
	 * @param firedTran
	 * 		The transition that fired.
	 * @param s
	 * 		The current (local) state.
	 */
	public void updateContinuousAssignment(Transition firedTran, State s){

		// Get the LPN.
		LhpnFile lpn = _lpnList[firedTran.getLpn().getLpnIndex()];
		
		// Get the current values of the (local) state.
		HashMap<String,String> currentValues = 
				lpn.getAllVarsWithValuesAsString(s.getVector());
		
		// Get all the continuous variable assignments.
		HashMap<String, ExprTree> assignTrees = firedTran.getContAssignTrees();
		
		for(String contVar : assignTrees.keySet()){
			
			// Get the bounds to assign the continuous variables.
			IntervalPair assignment = 
					assignTrees.get(contVar).evaluateExprBound(currentValues, this, null);
			
			// Make the assignment.
			setContinuousBounds(contVar, lpn, assignment);
		}
		
	}
	
	/**
	 * Updates the continuous variables according to the given values.
	 * @param newContValues
	 * 		The new values of the continuous variables.
	 */
	public void updateContinuousAssignment(HashMap<LPNContinuousPair, IntervalPair> newContValues){
		
		for(Entry<LPNContinuousPair, IntervalPair> pair : newContValues.entrySet()){
			
			if(pair.getKey().getCurrentRate() != 0){
				// Set the lower bound.
				setDbmEntryByPair(pair.getKey(), LPNTransitionPair.ZERO_TIMER_PAIR, (-1)*pair.getValue().get_LowerBound());

				// Set the upper bound.
				setDbmEntryByPair(pair.getKey(), LPNTransitionPair.ZERO_TIMER_PAIR, pair.getValue().get_UpperBound());
			}
			else{
				LPNTransitionPair ltpair = pair.getKey();
				VariableRangePair variablePair = new VariableRangePair(_lpnList[pair.getKey().get_lpnIndex()].getContVar(pair.getKey().get_transitionIndex()),
						pair.getValue());
				
				_rateZeroContinuous.put(ltpair, variablePair);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#getLexicon()
	 */
//	public HashMap<Integer, Transition> getLexicon(){
//		if(_indexToTransition == null){
//			return null;
//		}
//		
//		return new HashMap<Integer, Transition>(_indexToTransition);
//	}
//	
//	public void setLexicon(HashMap<Integer, Transition> lexicon){
//		_indexToTransition = lexicon;
//	}
	
	/**
	 * Gives an array that maps the index of a timer in the DBM to the timer's index.
	 * @return
	 * 		The array that maps the index of a timer in the DBM to the timer's index.
	 */
//	public int[] getIndexToTimer(){
//		return Arrays.copyOf(_indexToTimerPair, _indexToTimerPair.length);
//	}
	
	/**
	 * Calculates a warping value needed to warp a Zone. When a zone is being warped the form
	 * r1*z2 - r1*z1 + r2*z1 becomes important in finding the new values of the zone. For example,
	 * 
	 * @param z1
	 * 		Upper bound or negative lower bound.
	 * @param z2
	 * 		Relative value.
	 * @param r1
	 * 		First ratio.
	 * @param r2
	 * 		Second ratio.
	 * @return
	 * 		r1*z2 - r1*z1 + r2*z1
	 */
	public int warp(int z1, int z2, int r1, int r2){
		/*
		 *  See "Verification of Analog/Mixed-Signal Circuits Using Labeled Hybrid Petri Nets"
		 *  by S. Little, D. Walter, C. Myers, R. Thacker, S. Batchu, and T. Yoneda
		 *  Section III.C for details on how this function is used and where it comes
		 *  from.
		 */
		
		return r1*z2 - r1*z1 + r2*z1;
	}
	
	/**
	 * Warps this Zone with the aid of rate infomation from the previous Zone. 
	 * 
	 * @param oldZone
	 * 		The previous Zone.
	 * @return
	 * 		The warped Zone.
	 */
	public void dbmWarp(Zone oldZone){
		/*
		 *  See "Verification of Analog/Mixed-Signal Circuits Using Labeled Hybrid Petri Nets"
		 *  by S. Little, D. Walter, C. Myers, R. Thacker, S. Batchu, and T. Yoneda
		 *  Section III.C for details on how this function is used and where it comes
		 *  from.
		 */
		
//		return null;
		
//		void lhpnDbmWarp(lhpnStateADT s,eventADT *events,int nevents)
//		{
//		#ifdef __LHPN_TRACE__
//		  printf("lhpnDbmWarp:begin()\n");
//		#endif
//		  /* TODO: This appears to NOT work when INFIN is in the bounds?
//		     Should I have to worry about this case? */
//		  for(int i=1;i<s->z->dbmEnd;i++) {
//		    for(int j=i+1;j<s->z->dbmEnd;j++) {
//		      double iVal = 0.0;
//		      double jVal = 0.0;
//		      double iWarp = 0;
//		      double jWarp = 0;
//		      double iXDot = 0;
//		      double jXDot = 0;
//
		// According to atacs comments, this appears to NOT wark when
		// INFIN is in the bounds.
		// This portion of the code handles the warping of the relative
		// parts of the zone.
		for(int i=1; i< dbmSize(); i++){
			for(int j=i+1; j<dbmSize(); j++){
				double iVal, jVal, iWarp, jWarp, iXDot, jXDot;
		
				// Note : the iVal and the jVal correspond to the 
				// alpha and beta describe in Scott Little's thesis.
				
		
//		      /* deal w/ the fact that we might have continuous and discrete
//		         places */
//		#ifdef __LHPN_DEBUG_WARP__
//		      printf("Working on %d->%d\n",i,j);
//		#endif
//		      if(s->z->curClocks[i].enabling == -2) {
//		        iVal = fabs((double)s->r->oldBound[s->z->curClocks[i].enabled-nevents].current /
//		                    (double)s->r->bound[s->z->curClocks[i].enabled-nevents].current);
//		        iWarp = fabs((double)s->r->oldBound[s->z->curClocks[i].enabled-nevents].current);
//		        iXDot = fabs((double)s->r->bound[s->z->curClocks[i].enabled-nevents].current);
//		      }
		
				// Do some warping when dealing with the continuous variables.
				if(_indexToTimerPair[i] instanceof LPNContinuousPair){
					// Calcualte the alpha value.
					iVal = Math.floor(Math.abs(
							(double) oldZone.getCurrentRate(_indexToTimerPair[i]) /
							(double) this.getCurrentRate(_indexToTimerPair[i])));
					
					// The old rate the zone was warped by.
					iWarp = Math.floor(Math.abs(
							(double) oldZone.getCurrentRate(_indexToTimerPair[i])));
					
					// The current rate rate of this continuous variable.
					iXDot = iWarp = Math.floor(Math.abs(
							(double) this.getCurrentRate(_indexToTimerPair[i])));
				}
				
//		      else {
//		        iVal = 1.0;
//		        iWarp = 1.0;
//		        iXDot = 1.0;
//		      }
//		      
				else{
					// The current variable is a timer, so the new rate and old rate
					// are both 1. Hence we have
					iVal = 1.0;
					iWarp = 1.0;
					iXDot = 1.0;
				}
				
//		      if(s->z->curClocks[j].enabling == -2) {
//		        jVal = fabs((double)s->r->oldBound[s->z->curClocks[j].enabled-nevents].current /
//		                    (double)s->r->bound[s->z->curClocks[j].enabled-nevents].current);
//		        jWarp = fabs((double)s->r->oldBound[s->z->curClocks[j].enabled-nevents].current);
//		        jXDot = fabs((double)s->r->bound[s->z->curClocks[j].enabled-nevents].current);
//		      }
				
				// Do some warping of the second variable if it is a continuous variabl.
				if(_indexToTimerPair[j] instanceof LPNContinuousPair){
					// Calcualte the alpha value.
					jVal = Math.floor(Math.abs(
							(double) oldZone.getCurrentRate(_indexToTimerPair[j]) /
							(double) this.getCurrentRate(_indexToTimerPair[j])));
					
					// The old rate the zone was warped by.
					jWarp = Math.floor(Math.abs(
							(double) oldZone.getCurrentRate(_indexToTimerPair[j])));
					
					// The current rate rate of this continuous variable.
					jXDot = iWarp = Math.floor(Math.abs(
							(double) this.getCurrentRate(_indexToTimerPair[j])));
				}
				
//		      else {
//		        jVal = 1.0;
//		        jWarp = 1.0;
//		        jXDot = 1.0;
//		      }
//
				else{
					// The current variable is a timer, so the new rate and old rate
					// are both 1. Hence we have
					jVal = 1.0;
					jWarp = 1.0;
					jXDot = 1.0;
				}
				
//		#ifdef __LHPN_DEBUG_WARP__
//		      printf("iVal: %f, jVal: %f, iWarp: %f, jWarp: %f, iXDot: %f, jXDot: %f\n",iVal,jVal,iWarp,jWarp,iXDot,jXDot);
//		/*       printf("calc1- jWarp:%d * s->z->matrix[i][j]:%d / jXDot:%d + (-1 * jWarp:%d * s->z->matrix[i][0]:%d) / jXDot:%d + (iWarp:%d * s->z->matrix[i][0]:%d) / iXDot:%d = %d 1:%d 2:%d 3:%d -- %d\n", jWarp,s->z->matrix[i][j],jXDot,jWarp,s->z->matrix[i][0],jXDot,iWarp,s->z->matrix[i][0],iXDot,(chkDiv((jWarp * s->z->matrix[i][j]),jXDot,'C') + chkDiv((-1 * jWarp * s->z->matrix[i][0]),jXDot,'C') + chkDiv((iWarp * s->z->matrix[i][0]),iXDot,'C')),chkDiv((jWarp * s->z->matrix[i][j]),jXDot,'C'),chkDiv((-1 * jWarp * s->z->matrix[i][0]),jXDot,'C'),chkDiv((iWarp * s->z->matrix[i][0]),iXDot,'C'),(int)ceil(((jWarp * s->z->matrix[i][j])/jXDot) +((-1 * jWarp * s->z->matrix[i][0])/jXDot) + ((iWarp * s->z->matrix[i][0])/iXDot))); */
//		/*       printf("calc2-jWarp:%f * s->z->matrix[j][i]):%d/jXDot:%f) + ((-1 * jWarp:%f * s->z->matrix[0][i]:%d)/jXDot:%f) + ((iWarp:%f * s->z->matrix[0][i]):%d,iXDot:%f)) = %d 1:%f 2:%f 3:%f\n",jWarp,s->z->matrix[j][i],jXDot,jWarp,s->z->matrix[0][i],jXDot,iWarp,s->z->matrix[0][i],iXDot,(int) ceil(((jWarp * s->z->matrix[j][i])/jXDot) + ((-1 * jWarp * s->z->matrix[0][i])/jXDot) + ((iWarp * s->z->matrix[0][i]),iXDot)),((jWarp * (double)s->z->matrix[j][i])/jXDot),((-1 * jWarp * (double)s->z->matrix[0][i])/jXDot),(iWarp * (double)s->z->matrix[0][i])/iXDot); */
//		#endif
//		      
//		      if(iVal > jVal) {
//		/*         s->z->matrix[i][j] = */
//		/*           chkDiv((jWarp * s->z->matrix[i][j]),jXDot,'C') + */
//		/*           chkDiv((-1 * jWarp * s->z->matrix[i][0]),jXDot,'C') + */
//		/*           chkDiv((iWarp * s->z->matrix[i][0]),iXDot,'C'); */
//		/*         s->z->matrix[j][i] = */
//		/*           chkDiv((jWarp * s->z->matrix[j][i]),jXDot,'C') + */
//		/*           chkDiv((-1 * jWarp * s->z->matrix[0][i]),jXDot,'C') + */
//		/*           chkDiv((iWarp * s->z->matrix[0][i]),iXDot,'C'); */
//		        s->z->matrix[i][j] = (int)
//		          ceil(((jWarp * s->z->matrix[i][j])/jXDot) +
//		               ((-1 * jWarp * s->z->matrix[i][0])/jXDot) +
//		               ((iWarp * s->z->matrix[i][0])/iXDot));
//		        s->z->matrix[j][i] = (int)
//		          ceil(((jWarp * s->z->matrix[j][i])/jXDot) +
//		               ((-1 * jWarp * s->z->matrix[0][i])/jXDot) +
//		               ((iWarp * s->z->matrix[0][i])/iXDot));
//		      }
				
				// The zone is warped differently depending on which of rate is
				// larger. See Scott Little's Thesis for more details.
				if(iVal > jVal){
					setDbmEntry(j, i, (int)
							Math.ceil(((jWarp*getDbmEntry(j, i))/jXDot) +
							((-1*jWarp*getDbmEntry(0, i)/jXDot)) +
							((iWarp*getDbmEntry(0, i)/iXDot))));
					
					setDbmEntry(i, j, (int)
							Math.ceil(((jWarp*getDbmEntry(i, j))/jXDot) +
							((-1*jWarp*getDbmEntry(i, 0)/jXDot)) +
							((iWarp*getDbmEntry(i, 0)/iXDot))));
				}
				
//		      else {
//		/*         s->z->matrix[j][i] = */
//		/*           chkDiv((iWarp * s->z->matrix[j][i]),iXDot,'C') + */
//		/*           chkDiv((-1 * iWarp * s->z->matrix[j][0]),iXDot,'C') + */
//		/*           chkDiv((jWarp * s->z->matrix[j][0]),jXDot,'C'); */
//		/*         s->z->matrix[i][j] = */
//		/*           chkDiv((iWarp * s->z->matrix[i][j]),iXDot,'C') + */
//		/*           chkDiv((-1 * iWarp * s->z->matrix[0][j]),iXDot,'C') + */
//		/*           chkDiv((jWarp * s->z->matrix[0][j]),jXDot,'C'); */
//		        s->z->matrix[j][i] = (int)
//		          ceil(((iWarp * s->z->matrix[j][i])/iXDot) +
//		               ((-1 * iWarp * s->z->matrix[j][0])/iXDot) +
//		               ((jWarp * s->z->matrix[j][0])/jXDot));
//		        s->z->matrix[i][j] = (int)
//		          ceil(((iWarp * s->z->matrix[i][j])/iXDot) +
//		               ((-1 * iWarp * s->z->matrix[0][j])/iXDot) +
//		               ((jWarp * s->z->matrix[0][j])/jXDot));
//		      }
				
				else{
					setDbmEntry(i, j, (int)
							Math.ceil(((iWarp*getDbmEntry(i, j))/iXDot) +
							((-1*iWarp*getDbmEntry(0, j)/iXDot)) +
							((jWarp*getDbmEntry(0, j)/jXDot))));
					
					setDbmEntry(j, i, (int)
							Math.ceil(((iWarp*getDbmEntry(j, i))/iXDot) +
							((-1*iWarp*getDbmEntry(j, 0)/iXDot)) +
							((jWarp*getDbmEntry(j, 0)/jXDot))));
				}
//		    }
//		  }
//
			}
		}
//		#ifdef __LHPN_DEBUG_WARP__
//		  printf("After fixing up initial warp conditions.\n");
//		  printZ(s->z,events,nevents,s->r);
//		#endif
//		  
//		  for(int i=1;i<s->z->dbmEnd;i++) {
//		    if(s->z->curClocks[i].enabling == -2) {
		
		// Handle the warping of the bounds.
		for(int i=1; i<dbmSize(); i++){
			if(_indexToTimerPair[i] instanceof LPNContinuousPair){
		
//		#ifdef __LHPN_DEBUG_WARP__
//		      printf("old:%d new:%d v1:%d v2:%d\n",s->r->oldBound[s->z->curClocks[i].enabled-nevents].current,s->r->bound[s->z->curClocks[i].enabled-nevents].current,s->z->matrix[0][i],s->z->matrix[i][0]);
//		#endif
//		      if(abs(s->z->matrix[0][i]) != INFIN) {
//		      s->z->matrix[0][i] =
//		        chkDiv((abs(s->r->oldBound[s->z->curClocks[i].enabled-nevents].current)
//		                * s->z->matrix[0][i]),
//		               abs(s->r->bound[s->z->curClocks[i].enabled-nevents].current)
//		               ,'C');
//		      }
				
				if(Math.abs(getDbmEntry(i, 0)) != INFINITY){
					// Undo the old warping and introduce the new warping.
					// If the bound is infinite, then division does nothing.
					setDbmEntry(i, 0, ContinuousUtilities.chkDiv(
							Math.abs(oldZone.getCurrentRate(_indexToTimerPair[i]))
							* getDbmEntry(i, 0),
							Math.abs(getCurrentRate(_indexToTimerPair[i])), 
							true));
				}
				
//		      if(abs(s->z->matrix[i][0]) != INFIN) {
//		      s->z->matrix[i][0] =
//		        chkDiv((abs(s->r->oldBound[s->z->curClocks[i].enabled-nevents].current)
//		                * s->z->matrix[i][0]),
//		               abs(s->r->bound[s->z->curClocks[i].enabled-nevents].current)
//		               ,'C');
//		      }
				
				if(Math.abs(getDbmEntry(0, i)) != INFINITY){
					// Undo the old warping and introduce the new warping.
					// If the bound is inifite, then division does nothing.
					setDbmEntry(0, i, ContinuousUtilities.chkDiv(
							Math.abs(oldZone.getCurrentRate(_indexToTimerPair[i]))
							* getDbmEntry(0, i),
							Math.abs(getCurrentRate(_indexToTimerPair[i])), 
							true));
				}
//		    }
//		  }
//
			}
		}
			
			
//		#ifdef __LHPN_DEBUG_WARP__
//		  printf("After fixing up places.\n");
//		  printZ(s->z,events,nevents,s->r);
//		#endif
//
//		  for(int i=1;i<s->z->dbmEnd;i++) {
//		    if(s->z->curClocks[i].enabling == -2) {
		
		for(int i=1; i<dbmSize(); i++){
			if(_indexToTimerPair[i] instanceof LPNContinuousPair){
		
//		#ifdef __LHPN_DEBUG_WARP__
//		      printf("Warp: %d\n",s->r->oldBound[s->z->curClocks[i].enabled-nevents].current);
//		#endif
//		      if(((float)s->r->oldBound[s->z->curClocks[i].enabled-nevents].current /
//		          (float)s->r->bound[s->z->curClocks[i].enabled-nevents].current) < 0.0) {
//		        /* swap */
//		        int temp = s->z->matrix[0][i];
//		        s->z->matrix[0][i] = s->z->matrix[i][0];
//		        s->z->matrix[i][0] = temp;
//		      
//		        for(int j=1;j<s->z->dbmEnd;j++) {
//		          /* TBD: If i & j are both changing direction do we need to
//		             remove the warp info? */
//		          if(i != j) {
//		            s->z->matrix[j][i] = INFIN;
//		            s->z->matrix[i][j] = INFIN;
//		          }
//		        }
//		      }   
//		    }
//		  }
//
				// Handle the case when the warping takes us into negative space.
				if((double) oldZone.getCurrentRate(_indexToTimerPair[i])/
						(double) this.getCurrentRate(_indexToTimerPair[i]) < 0.0);
				/* We are warping into the negative space, so swap the upper and 
				 * lower bounds.
				 */
				
				int temp = getDbmEntry(i, 0);
				setDbmEntry(i,0, getDbmEntry(0, i));
				setDbmEntry(0, i, temp);
				
				
				// Set the relationships to Infinity since nothing else is known.
				for(int j=1; j<dbmSize(); j++){
					if(i != j){
						setDbmEntry(i, j, INFINITY);
						setDbmEntry(j, i, INFINITY);
					}
				}
			}		
		}
			
//		#ifdef __LHPN_DEBUG_WARP__
//		  printf("After handling negative warps.\n");
//		  printZ(s->z,events,nevents,s->r);
//		#endif
//
//		    for(int i=1;i<s->z->dbmEnd;i++) {
//		    if(s->z->curClocks[i].enabling == -2) {
		
		for(int i=1; i<dbmSize(); i++){
			if(_indexToTimerPair[i] instanceof LPNContinuousPair){
				
//		      int newCwarp = s->r->bound[s->z->curClocks[i].enabled-nevents].current;
//		      int newLwarp = s->r->bound[s->z->curClocks[i].enabled-nevents].lower;
//		      int newUwarp = s->r->bound[s->z->curClocks[i].enabled-nevents].upper;
//		      s->r->oldBound[s->z->curClocks[i].enabled-nevents].current = newCwarp;
//		      s->r->oldBound[s->z->curClocks[i].enabled-nevents].lower = newLwarp;
//		      s->r->oldBound[s->z->curClocks[i].enabled-nevents].upper = newUwarp;
//		        
//		#ifdef __LHPN_DEBUG_WARP__
//		      printf("New warp for %d: %d\n",i,s->r->oldBound[s->z->curClocks[i].enabled-nevents].current);
//		#endif
//		    }
//		  }
//
			/* Do the nature of how I store things, I do not think I need to do
			 * this portion.
			 */
			
				
			}
		}
			
//		#ifdef __LHPN_DEBUG_WARP__
//		  printf("Before recanon.\n");
//		  printZ(s->z,events,nevents,s->r);
//		#endif
//		  recanonZ(s->z);
//		#ifdef __LHPN_DEBUG_WARP__
//		  printf("After recanon.\n");
//		  printZ(s->z,events,nevents,s->r);
//		#endif
//		}
	}
	
	
	//----------------------Inner Classes-----------------------------------------------
	/**
	 * The DiagonalNonZeroException extends the java.lang.RuntimerExpcetion. 
	 * The intention is for this exception to be thrown is a Zone has a non zero
	 * entry appear on the diagonal.
	 * 
	 * @author Andrew N. Fisher
	 *
	 */
	public class DiagonalNonZeroException extends java.lang.RuntimeException
	{

		/**
		 * Generated serialVersionUID.
		 */
		private static final long serialVersionUID = -3857736741611605411L;

		/**
		 * Creates a DiagonalNonZeroException.
		 * @param Message
		 * 			The message to be displayed when the exception is thrown.
		 */
		public DiagonalNonZeroException(String Message)
		{
			super(Message);
		}
	}
	
	/**
	 * This exception is thrown when trying to merge two zones whose corresponding timers
	 * do not agree.
	 * @author Andrew N. Fisher
	 *
	 */
//	public class IncompatibleZoneException extends java.lang.RuntimeException
//	{
//
//		// TODO : Check if this class can be removed.
//		
//		/**
//		 * Generated serialVersionUID
//		 */
//		private static final long serialVersionUID = -2453680267411313227L;
//		
//		
//		public IncompatibleZoneException(String Message)
//		{
//			super(Message);
//		}
//	}
	
	
	/**
	 * Clears out the lexicon.
	 */
//	public static void clearLexicon(){
//		_indexToTransition = null;
//	}
	
	private IntervalPair parseRate(String rate){
		
		String rateNoSpaces = rate.trim();
		
		// First check if the string is a single number.
//		Integer i = Integer.parseInt(rate);
//		if(i != null){
//			// The string is a number, so set the upper and lower bounds equal.
//			return new IntervalPair(i,i);
//		}
		
		// First check for a comma (representing an interval input).
		int commaIndex = rateNoSpaces.indexOf(",");
		
		if(commaIndex < 0){
			// Assume that the string is a constant. A NumberFormatException
			// will be thrown otherwise.
			int i = Integer.parseInt(rate);
			return new IntervalPair(i,i);
		}
		
		String lowerString = rateNoSpaces.substring(1, commaIndex).trim();
		String upperString = rateNoSpaces.substring(commaIndex+1, 
				rateNoSpaces.length()-1).trim();
		
		return new IntervalPair(Integer.parseInt(lowerString),
				Integer.parseInt(upperString));
	}
	
	/**
	 * Get the list of LhpnFile objects that this Zone depends on.
	 * @return
	 * 		The lits of LhpnFile objects that this Zone depends on.
	 */
	public LhpnFile[] get_lpnList(){
		return _lpnList;
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
//	public int chkDiv(int top, int bottom, Boolean ceil){
//		/*
//		 * This method was taken from atacs/src/hpnrsg.c
//		 */
//		int res = 0;
//		  if(top == INFINITY ||
//		     top == INFINITY * -1) {
//		    if(bottom < 0) {
//		      return top * -1;
//		    }
//		    return top;
//		  }
//		  if(bottom == INFINITY) {
//			  return 0;
//		  }
//		  if(bottom == 0) {
//			  System.out.println("Warning: Divided by zero.");
//			  bottom = 1;
//		  }
//
//		  double Dres,Dtop,Dbottom;
//		  Dtop = top;
//		  Dbottom = bottom;
//		  Dres = Dtop/Dbottom;
//		  if(ceil) {
//			  res = (int)Math.ceil(Dres);
//		  }
//		  else if(!ceil) {
//			  res = (int)Math.floor(Dres);
//		  }
//		  return res;
//	}
	
	/**
	 * Returns the current rate of the variable.
	 * @param contVar
	 * 		The LPNTransitionPair referring to a continuous variable.
	 * @return
	 * 		The current rate of the continuous variable refenced by the LPNTransitionPair.
	 * @throws IllegalArgumentException
	 * 		If the LPNTransitionPair is not an instance of an LPNContinuousPair.
	 */
	public int getCurrentRate(LPNTransitionPair contVar){
		
		if(!(contVar instanceof LPNContinuousPair)){
			// The LPNTransitionsPair does not refer to a continuous variable, so yell.
			throw new IllegalArgumentException("Zone.getCurrentRate was called" +
					" on an LPNTransitionPair that was not an LPNContinuousPair.");
		}
			
		LPNContinuousPair cV = (LPNContinuousPair) contVar;
		
		// Search for the pair in the zone.
		int index = Arrays.binarySearch(_indexToTimerPair, cV);
		if(index >0){
			// The continuous variable was found amongst the non zero rate continuous variables.
			// Grab that indexing object instead since it has the rate.
			cV = (LPNContinuousPair) _indexToTimerPair[index];
		}
		
		return cV.getCurrentRate();
	}
	
	/**
	 * Sets the current rate for a continuous variable. It sets the rate regardless of 
	 * whether the variable is in the rate zero portion of the Zone or not.
	 * @param contVar
	 * 		The index of the variable whose rate is going to be set.
	 * @param currentRate
	 * 		The value of the rate.
	 */
	public void setCurrentRate(LPNTransitionPair contVar, int currentRate){
		
		if(!(contVar instanceof LPNContinuousPair)){
			// The LPNTransitionsPair does not refer to a continuous variable, so yell.
			throw new IllegalArgumentException("Zone.getCurrentRate was called" +
					" on an LPNTransitionPair that was not an LPNContinuousPair.");
		}
		
		
		LPNContinuousPair cV = (LPNContinuousPair) contVar;
		
		// Check for the current variable in the rate zero variables.
		
		VariableRangePair variableRange = _rateZeroContinuous.getValue(contVar);
		
		if(variableRange != null){
			LPNContinuousPair lcPair = (LPNContinuousPair)_rateZeroContinuous.getKey(variableRange);
			lcPair.setCurrentRate(currentRate);
			return;
		}
		
		// Check for the current variable in the Zone varaibles.
		int index = Arrays.binarySearch(_indexToTimerPair, contVar);
		
		if(index >= 0){
			// The variable was found, set the rate.
			LPNContinuousPair lcPair = (LPNContinuousPair) _indexToTimerPair[index];
			lcPair.setCurrentRate(currentRate);
		}
	}
	
	/**
	 * Adds a transition to a zone.
	 * @param newTransitions
	 * 			The newly enabled transitions.
	 * @return
	 * 			The result of adding the transition.
	 */
	public Zone addTransition(HashSet<LPNTransitionPair> newTransitions, State[] localStates){
		/*
		 * The zone will remain the same for all the continuous variables.
		 * The only thing that will change is a new transition will be added into the transitions.
		 */
		
		// Create a Zone to alter.
		Zone newZone = new Zone();
		
		// Create a copy of the LPN list.
		newZone._lpnList = Arrays.copyOf(this._lpnList, this._lpnList.length);
		
		// Copy the rate zero continuous variables.
		newZone._rateZeroContinuous = this._rateZeroContinuous.clone();
		
		// Create a copy of the current indexing pairs.
		//newZone._indexToTimerPair = Arrays.copyOf(_indexToTimerPair, _indexToTimerPair.length);
		newZone._indexToTimerPair = new LPNTransitionPair[_indexToTimerPair.length + newTransitions.size()];
		for(int i=0; i<_indexToTimerPair.length; i++){
			newZone._indexToTimerPair[i] = _indexToTimerPair[i];
		}
		
		
		// Add the new transitions to the _indexToTimerPair list.
//		for(int i=_indexToTimerPair.length; i<newZone._indexToTimerPair.length; i++){
//			// Set up the index for the newTransitions list.
//			int newTransitionIndex = i-_indexToTimerPair.length;
//			newZone._indexToTimerPair[i] = newTransitions[newTransitionIndex];
//		}
		
		int i = _indexToTimerPair.length;
		for(LPNTransitionPair ltPair : newTransitions){
			newZone._indexToTimerPair[i++] = ltPair;
		}
		
		// Sort the _indexToTimerPair list.
		Arrays.sort(newZone._indexToTimerPair);
		
		// Create matrix.
		newZone._matrix = new int[newZone._indexToTimerPair.length+1][newZone._indexToTimerPair.length+1];
		
		// Convert the current transitions to a collection of transitions.
		HashSet<LPNTransitionPair> oldTransitionSet = new HashSet<LPNTransitionPair>();
		for(LPNTransitionPair ltPair : _indexToTimerPair){
			oldTransitionSet.add(ltPair);
		}
		
		// Copy in the new transitions.
		newZone.copyTransitions(this, newTransitions, oldTransitionSet, localStates);
		
		newZone.advance(localStates);
		
		newZone.recononicalize();
		
		return newZone;
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
//	private boolean inequalityCanChange(InequalityVariable ineq, State[] localStates){
//	private boolean inequalityCanChange(InequalityVariable ineq, State localState){
//		
//		// Find the index of the continuous variable this inequality refers to.
//		// I'm assuming there is a single variable.
//		LhpnFile lpn = ineq.get_lpn();
//		Variable contVar = ineq.getInequalities().get(0);
//		DualHashMap<String, Integer> variableIndecies = lpn.getContinuousIndexMap();
//		int contIndex = variableIndecies.get(contVar);
//
//		// Package up the information into a the index. Note the current rate doesn't matter.
//		LPNContinuousPair index = new LPNContinuousPair(lpn.getLpnIndex(), contIndex, 0);
//
//		// Get the current rate.
//		int currentRate = getCurrentRate(index);
//
//		// Get the current value of the inequality. This requires looking into the current state.
////		int currentValue = localStates[lpn.getLpnIndex()].getCurrentValue(contIndex);
//		int currentValue = localState.getCurrentValue(contIndex);
//		
//		// Get the Zone index of the variable.
//		int zoneIndex = Arrays.binarySearch(_indexToTimerPair, index);
//		
////		bool lhpnPredCanChange(ineqADT ineq,lhpnZoneADT z,lhpnRateADT r,
////                lhpnMarkingADT m,eventADT *events,int nevents,
////	       lhpnStateADT s)
////{
////ineq_update(ineq,s,nevents);
////
////
////#ifdef __LHPN_TRACE__
////printf("lhpnPredCanChange:begin()\n");
////#endif
////#ifdef __LHPN_PRED_DEBUG__
////printf("lhpnPredCanChange Examining: ");
////printI(ineq,events);
////printf("signal = %c, %d",s->m->state[ineq->signal],r->bound[ineq->place-nevents].current);
////printf("\n");
////if (r->bound[ineq->place-nevents].current != 0)
////printf("divRes: %d\n",chkDiv(ineq->constant,
////			 r->bound[ineq->place-nevents].current,'F'));
////#endif
/////* > or >= */
////if(ineq->type == 0 || ineq->type == 1) {
//		
//		// > or >=
//		if(ineq.get_op().contains(">")){
//		
////int zoneP = getIndexZ(z,-2,ineq->place);
////if(zoneP == -1) {
////warn("An inequality produced a place not in the zone.");
////return false;
////}
//			
////if(r->bound[ineq->place-nevents].current < 0 &&
////m->state[ineq->signal] == '1') {
//			
//			// First check cases when the rate is negative.
//			if(currentRate < 0 && currentValue != 0){
//			
////if((-1)*z->matrix[zoneP][0] <=
////  (-1)*chkDiv(ineq->constant,r->bound[ineq->place-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCanChange:1\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return true;
//				
//				if((-1) * getDbmEntry(0, zoneIndex) <= 
//						(-1)*chkDiv(ineq.getConstant(), currentRate, false)){
//					return true;
//				}
//				
////} else {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCannotChange:1\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return false;
////}
////}
//				else{
//					return false;
//				}
//			}
////else if(r->bound[ineq->place-nevents].current > 0 &&
////     m->state[ineq->signal] == '0') {
//			
//			else if(currentRate > 0 && currentValue == 0){
//				
//				
////if(z->matrix[zoneP][0] >=
////  chkDiv(ineq->constant,r->bound[ineq->place-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCanChange:2\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return true;
//				
//
//				if(getDbmEntry(0, zoneIndex) <= 
//						chkDiv(ineq.getConstant(), currentRate, false)){
//					return true;
//				}
//				
////} else {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCannotChange:2\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
////return false;
//				
//				else{
//					return false;
//				}
//				
////}
////}
//				
//			}
////else {
////#ifdef __LHPN_PRED_DEBUG__
////printf("predCannotChange:3\n");
////printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
////return false;
////}
////}
//		}
/////* < or <= */
////else if(ineq->type == 2 || ineq->type == 3) {
//		
//		else if(ineq.get_op().contains("<")){
//		
////int zoneP = getIndexZ(z,-2,ineq->place);
////if(zoneP == -1) {
////warn("An inequality produced a place not in the zone.");
////return false;
////}
////if(r->bound[ineq->place-nevents].current < 0 &&
////m->state[ineq->signal] == '0') {
//			
//			if(currentRate < 0 && currentValue == 0){
//			
////if((-1)*z->matrix[zoneP][0] <=
////  (-1)*chkDiv(ineq->constant,r->bound[ineq->place-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCanChange:4\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return true;
//				
//				if((-1) * getDbmEntry(0, zoneIndex) <= 
//						(-1)*chkDiv(ineq.getConstant(), currentRate, false)){
//					return true;
//				}
//				
//				
////} else {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCannotChange:4\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return false;
//				
//				
//				else{
//					return false;
//				}
////}
////}
//			}
////else if(r->bound[ineq->place-nevents].current > 0 &&
////     m->state[ineq->signal] == '1') {
//			
//			else if (currentRate > 0 && 
//					currentValue != 0){
//			
//			
////if(z->matrix[zoneP][0] >=
////  chkDiv(ineq->constant,r->bound[ineq->place-nevents].current,'F')) {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCanChange:5\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return true;
//				
//				
//				if(getDbmEntry(0, zoneIndex) >= 
//						chkDiv(ineq.getConstant(), currentRate, false)){
//					return true;
//				}
//				
////} else {
////#ifdef __LHPN_PRED_DEBUG__
//// printf("predCannotChange:5\n");
//// printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
//// return false;
//				
//				
//				else {
//					return false;
//				}
////}
////}
//			}
////else {
////#ifdef __LHPN_PRED_DEBUG__
////printf("predCanChange:6\n");
////printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////      m->state[ineq->signal]);
////#endif
////return false;
////}
//			
//			else {
//				return false;
//			}
//			
////}
//		}
////#ifdef __LHPN_PRED_DEBUG__
////printf("predCanChange:7\n");
////printf("rate: %d state: %c\n",r->bound[ineq->place-nevents].current,
////  m->state[ineq->signal]);
////#endif
////return false;
////}
//		return false;
//	}
}
