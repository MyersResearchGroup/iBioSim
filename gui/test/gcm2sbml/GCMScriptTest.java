/**
 * 
 */
package gcm2sbml;


import java.io.File;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import junit.framework.TestCase;

import org.junit.Before;

/**
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMScriptTest extends TestCase{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreatePromoter() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 2; i <= numPromoters; i++) {
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GeneticNetwork.PROMOTERS, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "toggle.sbml");
						
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GeneticNetwork.PROMOTERS, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "si.sbml");

			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GeneticNetwork.PROMOTERS, ""+i);
			gcm.save(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + promoter + i + fileSerparator + "majority.sbml");

		}
	}
	
	/**
	 * Tests the effects of varying promoter count
	 */
	public void testCreateCoop() {
		System.loadLibrary("sbmlj");
		GCMFile gcm = null;
		GeneticNetwork network = null;
		GCMParser parser = null;
		for (int i = 1; i <= coop.length; i++) {
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + toggle);
			gcm.setParameter(GeneticNetwork.KREP, ""+coop[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.gcm");			
			network = parser.buildNetwork();			
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "toggle.sbml");
						
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + si);
			gcm.setParameter(GeneticNetwork.KREP, ""+coop[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "si.sbml");

			
			gcm = new GCMFile();
			gcm.load(directory + fileSerparator + majority);
			gcm.setParameter(GeneticNetwork.KREP, ""+coop[i-1]);
			gcm.save(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.gcm");
			parser = new GCMParser(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.gcm");			
			network = parser.buildNetwork();
			network.loadProperties(gcm);
			network.outputSBML(saveDirectory + fileSerparator + rep + i + fileSerparator + "majority.sbml");
		}
	}
	
	private double[] coop = new double[] {.125, .25, .5, 1, 2, 4};
	private int numPromoters = 5;
	private String directory = "gcm";	
	private char fileSerparator = File.separatorChar;
	private String saveDirectory = "/home/shang/namphuon/" + fileSerparator + "muller";
	private String toggle = "toggle.gcm";
	private String si = "si.gcm";
	private String majority = "majority.gcm";
	private String promoter = "promoter";
	private String rep = "coop";
	
}