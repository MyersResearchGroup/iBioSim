package test.conversion;

import org.junit.Test;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public abstract class ConversionAbstractTests {
	
	public abstract void roundtripSBOLFile(final String fileName);
	public abstract void roundtripSBMLFile(final String fileName);
	
}
