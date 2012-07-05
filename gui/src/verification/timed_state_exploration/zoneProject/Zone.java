package verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Transition;

import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;
import verification.timed_state_exploration.zone.TimedPrjState;


/**
 * This class is for storing and manipulating timing zones via difference bound matrices.
 * The underlying structure is backed by linked lists which yields the name
 * DBMLL (difference bound matrix linked list). A difference bound matrix has the form
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
	
	// Abstraction Function : 
	// The difference bound matrix is represented by int[][].
	// In order to keep track of the upper and lower bounds of timers from when they are first
	// enabled, the matrix will be augmented by a row and a column. The first row will contain
	// the upper bounds and the first column will contain the negative of the lower bounds.
	// For one timer t1 that is between 2 and 3, we might have
	//    lb t0 t1
	// ub  x  0  3
	// t0  0  m  m
	// t1  -2 m  m
	// where x is not important (and will be given a zero value), 3 is the upper bound on t1
	// and -2 is the negative of the lower bound. The m values represent the actual difference
	// bound matrix. Also note that the column heading are not part of the stored representation
	// lb stands for lower bound while ub stands for upper bound.
	// This upper and lower bound information is called the Delay for a Transition object.
	// Since a timer is tied directly to a Transition, the timers are index by the corresponding
	// Transition's index in a LPNTranslator.
	// The timers are named by an integer referred to as the index. The _indexToTimer array
	// connects the index in the DBM sub-matrix to the index of the timer. For example,
	// a the timer t1
	
	// Representation invariant :
	// Zones are immutable.
	// Integer.MAX_VALUE is used to logically represent infinity.
	// The lb and ub values for a timer should be set when the timer is enabled.
	// A negative hash code indicates that the hash code has not been set.
	// The index of the timer in _indexToTimer is the index in the DBM and should contain
	// 	the zeroth timer.
	// The array _indexToTimer should always be sorted.
	// The index of the LPN should match where it is in the _lpnList, that is, if lpn is
	// and LhpnFile object in _lpnList, then _lpnList[getLpnIndex()] == lpn.
	
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
		
		_hashCode = -1;
		
		boolean[] enabledTran = initialState.getTranVector();
		
		ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
				new ArrayList<LPNTransitionPair>();
		
		LPNTransitionPair zeroPair = new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1);
		
		// Add the zero timer first.
		enabledTransitionsArrayList.add(zeroPair);
		
		// The index of the boolean value corresponds to the index of the Transition.
		for(int i=0; i<enabledTran.length; i++){
			if(enabledTran[i]){
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
		
	}
	
	public Zone(State[] localStates){
		
		// Extract the local states.
		//State[] localStates = tps.toStateArray();
		
		// Initialize hash code to -1 (indicating nothing cached).
		_hashCode = -1; 
		
		// Initialize the LPN list.
		initialize_lpnList(localStates);
		
		// Get the enabled transitions. This initializes the _indexTotimerPair
		// which stores the relevant information.
		initialize_indexToTimerPair(localStates);
		
		// Initialize the matrix.
		_matrix = new int[matrixSize()][matrixSize()];
		
		// Set the lower bound/ upper bounds.
		initializeLowerUpperBounds(getTransitionNames(), localStates);
		
		// Advance Time
		advance();
		
		// Re-canonicalize
		recononicalize();
	}
	
	/**
	 * Gives the names of all the transitions that are represented by the zone.
	 * @return
	 * 		The names of the transitions that are represented by the zone.
	 */
	public String[] getTransitionNames(){

		String[] transitionNames = new String[_indexToTimerPair.length];
		
		transitionNames[0] = "The zero timer.";
		
		for(int i=1; i<transitionNames.length; i++){
			
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			transitionNames[i] = _lpnList[ltPair.get_lpnIndex()]
					.getTransition(ltPair.get_transitionIndex()).getName();
		}
		return transitionNames;
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
	 * Initializes the _indexToTimerPair from the local states.
	 * @param localStates
	 * 		The local states.
	 * @return
	 * 		The names of the transitions stored in the _indexToTimerPair (in the same order).
	 */
	private void initialize_indexToTimerPair(State[] localStates){
		
		// This list accumulates the transition pairs.
		ArrayList<LPNTransitionPair> enabledTransitionsArrayList =
				new ArrayList<LPNTransitionPair>();
				
		// Put in the zero timer.
		enabledTransitionsArrayList
			.add(new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1));
		
		// Get the rest of the transitions.
		for(int i=0; i<localStates.length; i++){
			
			// Extract the enabled transition vector.
			boolean[] enabledTran = localStates[i].getTranVector();
			
			// Accumulates the transition pairs for one LPN.
			ArrayList<LPNTransitionPair> singleLPN = new ArrayList<LPNTransitionPair>();
			
			// The index of the boolean value corresponds to the index of the Transition.
			for(int j=0; j<enabledTran.length; j++){
				if(enabledTran[j]){
					// Add the transition pair.
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
	 * Sets the lower and upper bounds for the transitions.
	 * @param transitionNames
	 * 			The names of the transitions in _indexToTimerPair.
	 */
	private void initializeLowerUpperBounds(String[] transitionNames, State[] localStates){
		
		// Traverse the entire length of the DBM submatrix except the zero row/column.
		// This is the same length as the _indexToTimerPair.length-1. The DBM is used to
		// match the idea of setting the value for each row.
		for(int i=1; i<dbmSize(); i++){
			// Get the current LPN and transition pairing.
			LPNTransitionPair ltPair = _indexToTimerPair[i];
			
			// Get the expression tree.
			ExprTree delay = _lpnList[ltPair.get_lpnIndex()].getDelayTree(transitionNames[i]);
			
			// Get the values of the variables for evaluating the ExprTree.
			HashMap<String, String> varValues = 
					_lpnList[ltPair.get_lpnIndex()]
					.getAllVarsWithValuesAsString(localStates[ltPair.get_lpnIndex()].getVector());
			
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
		
		LPNTransitionPair ltPair = 
				new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return getUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#getUpperBoundbydbmIndex(int)
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
		
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return -1*getLowerBoundbydbmIndex(
				Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#getLowerBoundbydbmIndex(int)
	 */
	public int getLowerBoundbydbmIndex(int index)
	{
		return _matrix[dbmIndexToMatrixIndex(index)][0];
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
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#getdbm(int, int)
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
	 * Returns the index of the the transition in the DBM given a LPNTransitionPair pairing
	 * the transition index and associated LPN index.
	 * @param ltPair
	 * 		The pairing comprising the index of the transition and the index of the associated
	 * 		LPN.
	 * @return
	 * 		The row/column of the DBM associated with the ltPair.
	 */
	private int timerIndexToDBMIndex(LPNTransitionPair ltPair)
	{
		return Arrays.binarySearch(_indexToTimerPair, ltPair);
	}
	
	
	/**
	 * The matrix labeled with 'ti' where i is the transition index associated with the timer.
	 */
	public String toString()
	{
		String result = "Timer and delay.\n";
		
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
				// Get the name of the transition.
				Transition tran = _lpnList[_indexToTimerPair[i].get_lpnIndex()].
						getTransition(_indexToTimerPair[i].get_transitionIndex());
				
				result += " " +  tran.getName() + ":";
			}
			result += "[ " + -1*getLowerBoundbydbmIndex(i) + ", " + getUpperBoundbydbmIndex(i) + " ]";
			
			if(count > 9)
			{
				result += "\n";
				count = 0;
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
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#subset(Zone)
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
		
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		return exceedsLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimerPair, ltPair));
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#exceedsLowerBoundbydbmIndex(int)
	 */
	public boolean exceedsLowerBoundbydbmIndex(int index)
	{
		// TODO: Check if finished.
		
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
	
	public Zone fire(Transition t, LpnTranList enabledTran, State[] localStates){
		
		// TODO : Finish this method. It uses the fireTransitionbydbmIndex.
		// However, change the signature of the fireTransitionbydbmIndex to a more
		// appropriate one, namely take into account the need for the LPN.
		
		// Create the LPNTransitionPair to check if the Transitions is in the zone and to 
		// find the index.
		LhpnFile lpn = t.getLpn();
		
		int lpnIndex = lpn.getLpnIndex();
		
		int transitionIndex = t.getIndex();
		
		LPNTransitionPair ltPair = new LPNTransitionPair(lpnIndex, transitionIndex);
		
		int dbmIndex = Arrays.binarySearch(_indexToTimerPair, ltPair);
		
		if(dbmIndex <= 0){
			return this;
		}
		
		return fireTransitionbydbmIndex(dbmIndex, enabledTran, localStates);
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#fireTransitionbydbmIndex(int, int[], verification.platu.stategraph.State)
	 */
	public Zone fireTransitionbydbmIndex(int index, LpnTranList enabledTimers,
			State[] localStates)
	{
		
		// TODO: Check for correctness.
		Zone newZone = new Zone();
		
		// Copy the LPNs over.
		newZone._lpnList = new LhpnFile[this._lpnList.length];
		for(int i=0; i<this._lpnList.length; i++){
			newZone._lpnList[i] = this._lpnList[i];
		}
		
		// Extract the pairing information for the enabled timers.
		// Using the enabledTimersList should be faster than calling the get method
		// several times.
		newZone._indexToTimerPair = new LPNTransitionPair[enabledTimers.size() + 1];
		int count = 0;
		newZone._indexToTimerPair[count++] = 
				new LPNTransitionPair(LPNTransitionPair.ZERO_TIMER, -1);
		for(Transition t : enabledTimers){
			newZone._indexToTimerPair[count++] = 
					new LPNTransitionPair(t.getLpn().getLpnIndex(), t.getIndex());
		}
		
		Arrays.sort(newZone._indexToTimerPair);
		
		HashSet<LPNTransitionPair> newTimers = new HashSet<LPNTransitionPair>();
		HashSet<LPNTransitionPair> oldTimers = new HashSet<LPNTransitionPair>();
		
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
		
		tempZone.restrict(index);
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

			newZone.setLowerBoundByLPNTransitionPair(pair, lower);
			newZone.setUpperBoundByLPNTransitionPair(pair, upper);

		}
		
		newZone.advance();
		newZone.recononicalize();
		
		
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
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#clone()
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
		
		return clonedZone;
	}
	
	/**
	 * Restricts the lower bound of a timer.
	 * 
	 * @param timer
	 * 			The timer to tighten the lower bound.
	 */
	private void restrict(int timer)
	{
		//int dbmIndex = Arrays.binarySearch(_indexToTimer, timer);
		
		_matrix[dbmIndexToMatrixIndex(timer)][dbmIndexToMatrixIndex(0)]
		                                            = getLowerBoundbydbmIndex(timer);
	}
	
	/* (non-Javadoc)
	 * @see verification.timed_state_exploration.zone.Zone#getEnabledTransitions()
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
	public class IncompatibleZoneException extends java.lang.RuntimeException
	{

		/**
		 * Generated serialVersionUID
		 */
		private static final long serialVersionUID = -2453680267411313227L;
		
		
		public IncompatibleZoneException(String Message)
		{
			super(Message);
		}
	}
	
	
	/**
	 * Clears out the lexicon.
	 */
//	public static void clearLexicon(){
//		_indexToTransition = null;
//	}
}
