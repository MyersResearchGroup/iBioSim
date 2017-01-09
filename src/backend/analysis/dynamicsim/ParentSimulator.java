package backend.analysis.dynamicsim;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

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

	public abstract void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException;
}
