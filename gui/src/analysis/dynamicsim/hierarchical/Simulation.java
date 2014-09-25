package analysis.dynamicsim.hierarchical;

/**
 * The interface to simulators in iBioSim.
 * 
 * @author Leandro Watanabe
 *
 */
public interface Simulation {

	public abstract void simulate();

	public abstract void cancel();

	public abstract void clear();

	public abstract void setupForNewRun(int newRun);
}
