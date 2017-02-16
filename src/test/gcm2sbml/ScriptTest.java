package test.gcm2sbml;

import gcm2sbml.scripts.GCMScript;
import gcm2sbml.scripts.SpeciesThresholdTester;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

public class ScriptTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void ThresholdCoop() {
		System.out.println("\nCoop:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "coop", "C",
				3500, "coop", 5);
	}

	public void ThresholdRatio() {
		System.out.println("\nRatio:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "ratio", "C",
				3500, "ratio", 5);
	}

	public void testThresholdPromoter() {
		System.out.println("\nPromoter:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "promoter", "C",
				3500, "promoter", 5);
	}

	public void ThresholdDecay() {
		System.out.println("\nDecay:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "decay", "C",
				3500, "decay", 5);
	}

	public void ThresholdRep() {
		System.out.println("\nRep:");
		GCMScript script = new GCMScript();
		script.generateThresholds(directory + Gui.separator + "rep", "C",
				3500, "rep", 6);
	}

	private static final String directory = "/home/shang/namphuon/muller";
}
