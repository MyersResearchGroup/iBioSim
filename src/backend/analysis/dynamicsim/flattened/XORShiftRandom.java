package backend.analysis.dynamicsim.flattened;

/*
 * adapted from http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml
 * this uses the XORShift method published by George Marsaglia in 2003 in the journal of statistical software
 */
public class XORShiftRandom {

	private long seed;

	public XORShiftRandom(long randomSeed) {

		seed = randomSeed;
		
		if (seed == 0) seed = System.nanoTime();
	}  
	
	/**
	 * generates a random long using the xorshift method
	 * 
	 * @param numBits the number of requested bits for the number
	 * @return a random number with the requested number of bits
	 */
	public long next(int numBits) {
		  
		long rand = seed;
		
		rand ^= (rand << 21);
		rand ^= (rand >>> 35);
		rand ^= (rand << 4);
		
		seed = rand;
		
		rand &= ((1L << numBits) - 1);
		 
		return rand;		
	}
	
	/**
	 * this is basically the Java.util class
	 * 
	 * @return a random number uniformly distributed between 0 and 1
	 */
	public double nextDouble() {
		
		return ((next(26) << 27) + next(27)) / (double)(1L << 53);
	}
}
