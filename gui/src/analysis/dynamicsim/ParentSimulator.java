package analysis.dynamicsim;

public interface ParentSimulator
{
	/**
	 * 
	 */
	public abstract void simulate();

	/**
	 * 
	 */
	public abstract void cancel();

	/**
	 * 
	 */
	public abstract void clear();

	/**
	 * 
	 * @param newRun
	 */
	public abstract void setupForNewRun(int newRun);

	/**
	 * 
	 */
	public abstract void printStatisticsTSD();
}
