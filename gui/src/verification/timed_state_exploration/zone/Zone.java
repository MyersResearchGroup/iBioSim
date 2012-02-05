package verification.timed_state_exploration.zone;

import java.util.HashMap;
import java.util.Arrays;


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
public class Zone {
	
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
	// A negative has code indicates that the hash code has not been set.
	// The smallest 
	
	/* The lower and upper bounds of the times as well as the dbm. */
	private int[][] _matrix;
	
	/* Maps the index to the timer. The index is row/column of the DBM sub-matrix.
	 * Logically the zero timer is given index -1.
	 *  */
	private int[] _indexToTimer;		
	
	/* Infinity is represented by the maximum integer value. */
	public static final int INFINITY = Integer.MAX_VALUE;
	
	/* The hash code. */
	private int _hashCode;
	
	/**
	 * Construct a zone that has the given timers.
	 * @param timers 
	 * 				The ith index of the array is the index of the timer. For example,
	 * 					if timers = [1, 3, 5], then the zeroth row/column of the DBM is the
	 * 					timer of the transition with index 1, the first row/column of the 
	 * 					DBM is the timer of the transition with index 3, and the 2nd 
	 * 					row/column is the timer of the transition with index 5.
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
		_indexToTimer = Arrays.copyOf(timers, timers.length);
		
		// Sorting the array.
		Arrays.sort(_indexToTimer);
		
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
		
		// TODO: Remove a zero timer if it is passed.
		
		// Map the new index of the timer to the old timer.
		HashMap<Integer, Integer> newIndex = new HashMap<Integer, Integer>();
		
		// For the old index, find the new index.
		for(int i=0; i<timers.length; i++)
		{
			newIndex.put(i, Arrays.binarySearch(_indexToTimer, timers[i]));
		}
		
		// Add the zero index for ease.
		newIndex.put(-1, -1);
		
		// Initialize the matrix.
		_matrix = new int[matrixSize()][matrixSize()];

		// Copy the DBM
		for(int i=-1; i<dbmSize()-1; i++)
		{
			for(int j=-1; j<dbmSize()-1; j++)
			{
				// Copy the passed in matrix to _matrix.
				setDBMIndex(newIndex.get(i), newIndex.get(j), 
						matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)]);
			}
		}
		
		// Copy in the upper and lower bounds.
		for(int i=0; i<timers.length; i++)
		{
			setUpperBoundbydbmIndex(newIndex.get(i),_matrix[0][timerIndexToMatrixIndex(i)]);
		}
		
		recononicalize();
	}
	
	/**
	 * Zero argument constructor for use in methods that create Zones where the members
	 * variables will be set by the method.
	 */
	private Zone()
	{
			// TODO : Finish by initializing the member variables.
		_matrix = new int[0][0];
		_indexToTimer = new int[0];
	}
	
	/**
	 * Logically the DBM is the sub-matrix of _matrix obtained by removing the zeroth
	 * row and column. This method retrieves the (i,j) element of the DBM.
	 * @param i 
	 * 			The ith row of the DBM.
	 * @param j 
	 * 			The jth column of the DBM.
	 * @return 
	 * 			The (i,j) element of the DBM.
	 */
	public int getDBMIndex(int i, int j)
	{
		return _matrix[i+1][j+1];
	}
	
	/**
	 * Logically the DBM is the sub-matrix of _matrix obtained by removing the zeroth
	 * row and column. This method sets the (i,j) element of the DBM.
	 * @param i 
	 * 			The ith row of the DBM.
	 * @param j 
	 * 			The jth column of the DBM.
	 * @param value 
	 * 			The value of the matrix.
	 */
	private void setDBMIndex(int i, int j, int value)
	{
		_matrix[i+1][j+1] = value;
	}
	
	/**
	 * Get the value of the upper bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The upper bound on the transitions delay.
	 */
	public int getUpperBoundbyTransitionIndex(int timer)
	{
		return getUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimer, timer));
	}
	
	/**
	 * Get the value of the upper bound for the delay.
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
	 * @param timer
	 * 			The timer's index.
	 * @param value
	 * 			The value of the upper bound.
	 */
	private void setUpperBoundbyTransitionIndex(int timer, int value)
	{
		setUpperBoundbydbmIndex(Arrays.binarySearch(_indexToTimer, timer), value);
	}
	
	/**
	 * Set the value of the upper bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @param value
	 * 			The value of the upper bound.
	 */
	private void setUpperBoundbydbmIndex(int index, int value)
	{
		_matrix[0][dbmIndexToMatrixIndex(index)] = value;
	}
	
	/**
	 * Get the value of the lower bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			The value of the lower bound.
	 */
	public int getLowerBoundbyTransitionIndex(int timer)
	{
		return getLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimer, timer));
	}
	
	/**
	 * Get the value of the lower bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @return
	 * 			The value of the lower bound.
	 */
	public int getLowerBoundbydbmIndex(int index)
	{
		return _matrix[0][dbmIndexToMatrixIndex(index)];
	}
	
	/**
	 * Set the value of the lower bound for the delay.
	 * @param timer
	 * 			The timer's index.
	 * @param value
	 * 			The value of the lower bound.
	 */
	private void setLowerBoundbyTransitionIndex(int timer, int value)
	{
		setLowerBoundbydbmIndex(timer, value);
	}
	
	/**
	 * Set the value of the lower bound for the delay.
	 * @param index
	 * 			The timer's row/column of the DBM matrix.
	 * @param value
	 * 			The value of the lower bound.
	 */
	private void setLowerBoundbydbmIndex(int index, int value)
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
	
	/**
	 * Retrieves an entry of the DBM using the DBM's addressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @return
	 * 			The value of the (i, j) element of the DBM.
	 */
	private int getdbm(int i, int j)
	{
		return _matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)];
	}
	
	/**
	 * Sets an entry of the DBM using the DBM's adressing.
	 * @param i
	 * 			The row of the DBM.
	 * @param j
	 * 			The column of the DBM.
	 * @param value
	 * 			The new value for the entry.
	 */
	private void setdbm(int i, int j, int value)
	{
		_matrix[dbmIndexToMatrixIndex(i)][dbmIndexToMatrixIndex(j)] = value;
	}
	
	/**
	 * Converts the index of the timer from the _indexToTimer array to the index of _matrix.
	 * @param i
	 * 			The index of the timer from the _indexToTimer array.
	 * @return
	 * 			The index in the _matrix.
	 */
	private int timerIndexToMatrixIndex(int i)
	{
		return i+2;
	}
	
	/**
	 * Converts the index of _matrix to the index of the DBM.
	 * @param i
	 * 			The row/column index of _matrix.
	 * @return
	 * 			The row/column index of the DBM.
	 */
	private int matrixIndexTodbmIndex(int i)
	{
		return i-1;
	}
	
	/**
	 * The matrix labeled with 'ti' where i is the transition index associated with the timer.
	 */
	public String toString()
	{
		String result = "Timer and delay.";
		
		int count = 0;
		
		// Print the timers.
		for(int i=0; i<_indexToTimer.length; i++, count++)
		{
			result += "t" + _indexToTimer + " : " + 
			"[ " + getLowerBoundbydbmIndex(i) + "," + getUpperBoundbydbmIndex(i) + " ]";
			
			if(count > 9)
			{
				result += "\n";
			}
		}
		
		result += "\nDBM\n";
		
		result += "|";
		
		// Print the DBM.
		// TODO: Fix this to print just the DBM.
		for(int i=0; i<_indexToTimer.length; i++)
		{
			result += " " + _matrix[i][0];
			
			for(int j=1; j<_indexToTimer.length; j++)
			{
				result += ", " + _matrix[i][j];
			}
			
			result += " |";
		}
		
		result += "|";
		
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
		
		// Check if the timers are the same.
		if(!Arrays.equals(this._indexToTimer, otherZone._indexToTimer))
		{
			return false;
		}
		
		// Check if the matrix is the same.
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
		int newHashCode = Arrays.hashCode(_indexToTimer);
		
		for(int i=0; i<_matrix.length; i++)
		{
			newHashCode ^= Arrays.hashCode(_matrix[i]);
		}
		
		return Math.abs(newHashCode);
	}
	
	/**
	 * Checks if the array has square dimensions.
	 * @param array
	 * 			The array to test.
	 * @return
	 * 			True if the arrays is square, non-zero, and non-null, false otherwise.
	 */
	private boolean checkSquare(int[][] array)
	{
		return false;
	}
	
	/**
	 * The size of the DBM sub matrix. This is calculated using the size of _indexToTimer.
	 * @return
	 * 			The size of the DBM.
	 */
	private int dbmSize()
	{
		return _indexToTimer.length + 1;
	}
	
	/**
	 * The size of the matrix.
	 * @return
	 * 			The size of the matrix. This is calculated using the size of _indexToTimer.
	 */
	private int matrixSize()
	{
		return _indexToTimer.length + 2;
	}
	
	/**
	 * Performs the Floyd's least pairs algorithm to reduce the DBM.
	 */
	private void recononicalize()
	{
		// TODO : Check if finished.
		
		for(int k=0; k<dbmSize(); k++)
		{
			for (int i=0; i<dbmSize(); i++)
			{
				for(int j=0; j<dbmSize(); j++)
				{
					if(getdbm(i, j) > getdbm(i, k) + getdbm(k, j))
					{
						setdbm(i, j, getdbm(i, k) + getdbm(k, j));
					}
					
					if( (i==j) && getdbm(i, j) != 0)
					{
						throw new DiagonalNonZeroException("Entry (" + i + ", " + j + ")" +
								" became " + getdbm(i, j) + ".");
					}
				}
			}
		}
	}
	
	/**
	 * Determines if a timer has reached its lower bound.
	 * @param timer
	 * 			The timer's index.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public boolean exceedsLowerBoundbyTransitionIndex(int timer)
	{
		// TODO : Check if finished.
		return exceedsLowerBoundbydbmIndex(Arrays.binarySearch(_indexToTimer, timer));
	}
	
	/**
	 * Determines if a timer has reached its lower bound.
	 * @param index
	 * 			The timer's row/column of the DBM.
	 * @return
	 * 			True if the timer has reached its lower bound, false otherwise.
	 */
	public boolean exceedsLowerBoundbydbmIndex(int index)
	{
		// TODO: Check if finished.
		
		// Note : Make sure that the lower bound is stored as a negative number
		// and that the inequality is correct.
		return _matrix[0][dbmIndexToMatrixIndex(index)] <=
			_matrix[1][dbmIndexToMatrixIndex(index)];
	}
	
	/**
	 * Updates the Zone according to a transition firing.
	 * @param timer
	 * 			The index of the transition that fired.
	 * @return
	 * 			The updated Zone.
	 */
	public Zone fireTransitionbyTransitionIndex(int timer)
	{
		// TODO: Check if finish.
		return fireTransitionbydbmIndex(Arrays.binarySearch(_indexToTimer, timer));
	}
	
	/**
	 * Updates the Zone according to the transition firing.
	 * @param index
	 * 			The row/column of the transition's timer in the DBM.
	 * @return
	 * 			The updated Zone.
	 */
	public Zone fireTransitionbydbmIndex(int index)
	{
		// TODO: Finish
		return null;
	}
	
	/**
	 * Merges this Zone with another Zone.
	 * @param otherZone
	 * 			The zone to merge with this Zone.
	 * @return
	 * 			The merged Zone.
	 */
	public Zone mergeZones(Zone otherZone)
	{
		// TODO: Finish.	
		
		Zone mergedZone = new Zone();
		
		mergedZone._indexToTimer = mergeTimers(this._indexToTimer, otherZone._indexToTimer);
		
		/* Maps the index of this Zone's timers to the mergeZone. */
		HashMap<Integer, Integer> thisNewIndex = new HashMap<Integer, Integer>();
		
		thisNewIndex = makeIndexMap(this._indexToTimer, mergedZone._indexToTimer);
		
		/* Maps the index of otherZone Zone's timers to the mergeZone. */
		HashMap<Integer, Integer> otherNewIndex = new HashMap<Integer, Integer>();
		
		otherNewIndex = makeIndexMap(otherZone._indexToTimer, mergedZone._indexToTimer);
		
		
		return mergedZone;
	}
	
	/**
	 * Merges the timers arrays to give a single sorted timer array.
	 * @param timer1
	 * 			The first array to merge.
	 * @param timer2
	 * 			The second array to merge.
	 * @return
	 * 			The merged array.
	 */
	private int[] mergeTimers(int[] timer1, int[] timer2)
	{
		/* These integers give the current index of the _indexToTimer. */
		int thisCurrentTimerIndex = 0;
		int otherCurrentTimerIndex = 0;
		
		// TODO: Finish.
		
		return null;
	}
	
	/**
	 * Merges two Zones.
	 * @param firstZone
	 * 				The first Zone to merge.
	 * @param secondZone
	 * 				The second Zone to merge.
	 * @return
	 * 				The merged Zone.
	 */
	public static Zone mergeZones(Zone firstZone, Zone secondZone)
	{
		return firstZone.mergeZones(secondZone);
	}
	
	/**
	 * Maps the indices of a value in one integer array to their indices in another
	 * integer array.
	 * Precondition: The elements in the baseTimers array should be in the newTimers
	 * array.
	 * @param baseTimers
	 * 				The old order for the integer array.
	 * @param newTimers
	 * 				The new order for the integer array.
	 * @return
	 * 				A HashMap that maps the index of a value in the baseTimers array
	 * 				to its index in the newTimers array.
	 */
	private HashMap<Integer, Integer> makeIndexMap(int[] baseTimers, int[] newTimers)
	{	
		// Map the new index of the timer to the old timer.
		HashMap<Integer, Integer> newIndex = new HashMap<Integer, Integer>();
		
		// For the old index, find the new index.
		for(int i=0; i<baseTimers.length; i++)
		{
			newIndex.put(i, Arrays.binarySearch(newTimers, baseTimers[i]));
		}
		
		// Add the zeroth map.
		newIndex.put(-1, -1);
		
		return newIndex;
	}
	
	/**
	 * Overrides the clone method from Object.
	 */
	public Zone clone()
	{
		// TODO: Check if finished.
		
		Zone clonedZone = new Zone();
		
		for(int i=0; i<this.matrixSize(); i++)
		{
			for(int j=0; j<this.matrixSize(); j++)
			{
				clonedZone._matrix[i][j] = this._matrix[i][j];
			}
		}
		
		clonedZone._indexToTimer = Arrays.copyOf(_indexToTimer, _indexToTimer.length);
		
		clonedZone._hashCode = this._hashCode;
		
		return clonedZone;
	}
	
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
}