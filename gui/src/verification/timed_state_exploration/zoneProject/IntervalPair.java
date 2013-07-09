package verification.timed_state_exploration.zoneProject;


/**
 * An interval pair is a pair of integers representing a range of values by giving
 * the upper and lower bounds for that range. They should be thought of as the 
 * interval [a,b] where a and b are integers.
 * 
 * @author Andrew N. Fisher
 *
 */
public class IntervalPair {
	
	// The lower bound of the interval.
	private int _lowerBound;
	
	// The upper bound of the interval.
	private int _upperBound;
	
	/**
	 * Define an interval with the appropriate upper and lower bounds.
	 * @param lowerBound
	 * @param upperBound
	 */
	public IntervalPair(int lowerBound, int upperBound) {
		this._lowerBound = lowerBound;
		this._upperBound = upperBound;
	}
	
	/**
	 * Defines an interval with upper and lower bounds set to zero, thus representing
	 * the point {0}.
	 */
	public IntervalPair(){
		this._lowerBound = 0;
		this._upperBound = 0;
	}
	
	/**
	 * Gets the lower bound for the interval.
	 * @return
	 */
	public int get_LowerBound() {
		return _lowerBound;
	}
	
	/**
	 * Sets the lower bound for the interval.
	 * @param lowerBound
	 */
	public void set_LowerBound(int lowerBound) {
		this._lowerBound = lowerBound;
	}
	
	/**
	 * Gets the upper bound for the interval.
	 * @return
	 */
	public int get_UpperBound() {
		return _upperBound;
	}
	
	/**
	 * Set the upper bound for the interval.
	 * @param upperBound
	 */
	public void set_UpperBound(int upperBound) {
		this._upperBound = upperBound;
	}
	
	/**
	 * Determines whether this IntervalPair represents a single value.
	 * @return
	 * 		True if lower bound is equal to upper bound.
	 */
	public boolean singleValue(){
		return _lowerBound == _upperBound;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lowerBound;
		result = prime * result + _upperBound;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntervalPair other = (IntervalPair) obj;
		if (_lowerBound != other._lowerBound)
			return false;
		if (_upperBound != other._upperBound)
			return false;
		return true;
	}
	
	public String toString(){
		return "[" + _lowerBound + "," + _upperBound + "]";
	}
	
	/**
	 * Returns the rate in the range with the smallest absolute value. 
	 * @return
	 * 		Let [a,b] be the range of rates. Returns 0 if a<0<b, a if
	 * 		0<a<b and b if a<b<0.
	 */
	public int getSmallestRate(){
		
		if(_lowerBound < 0 && _upperBound >0){
			return 0;
		}
		
		return Math.abs(_lowerBound)<Math.abs(_upperBound) ?
				_lowerBound : _upperBound;
	}
	
	/**
	 * Returns the rate in the range with the largest absolute value.
	 * @return
	 * 		Let [a,b] be the rage of rates.
	 */		
	public int getLargestRate(){
		return Math.abs(_lowerBound)>Math.abs(_upperBound)?
				_lowerBound : _upperBound;
	}
	
	/**
	 * Determines if zero is this range.
	 * @return True if the lower bound is less than zero and the upper bound is more
	 * 			than zero; false otherwise.
	 */
	public boolean containsZero(){
		return _lowerBound<0 && _upperBound>0;
	}
}
