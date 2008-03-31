package gcm2sbml;

import java.io.File;

import junit.framework.TestCase;
import gcm2sbml.scripts.GCMScript;

import org.junit.After;
import org.junit.Before;

public class ScriptTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void testThresholdCoop() {
		System.out.println("\nCoop:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + File.separator + "coop", "C",
				3500, "coop", 5);
	}
	
	public void testThresholdRatio() {
		System.out.println("\nRatio:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + File.separator + "ratio", "C",
				3500, "ratio", 5);
	}

	public void ThresholdPromoter() {
		System.out.println("\nPromoter:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + File.separator + "promoter", "C",
				3500, "promoter", 5);
	}

	public void testThresholdDecay() {
		System.out.println("\nDecay:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + File.separator + "decay", "C",
				3500, "decay", 5);
	}
	
	public void testThresholdRep() {
		System.out.println("\nRep:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + File.separator + "rep", "C",
				3500, "rep", 6);
	}

	private static final String directory = "/home/shang/namphuon/muller";
}
