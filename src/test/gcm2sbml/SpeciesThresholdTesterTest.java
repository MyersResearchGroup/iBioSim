package test.gcm2sbml;

import gcm2sbml.scripts.GCMScript;
import gcm2sbml.scripts.SpeciesThresholdTester;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;

public class SpeciesThresholdTesterTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	public void Threshold() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("C");
		SpeciesThresholdTester tester = new SpeciesThresholdTester(
				"/home/shang/namphuon/muller/promoter1", "maj", list, new ArrayList<String>());		
	}
	
	public void testResults() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("C");
		SpeciesThresholdTester tester = new SpeciesThresholdTester(
				"/home/shang/namphuon/workspace/BioSim/gcm", "tog", list, new ArrayList<String>());	
		GCMScript script = new GCMScript();
		double[][] results = script.generateStatistics("/home/shang/namphuon/workspace/BioSim/gcm/tog-h-low/", tester);
		for (int i = 0; i < results[0].length; i++) {
			System.out.println(results[0][i] + " " + results[1][i] + " " + results[2][i]);
		}
	}
}
