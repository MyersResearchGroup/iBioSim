package gcm2sbml;


import gui.src.gcm2sbml.util.Utility;

public class UtilityTest extends TestCase{

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testCalcAverage() {
		HashMap<String, double[]> values = Utility.calculateAverage("/home/shang/namphuon/workspace/BioSim/gcm");
		System.out.println();
	}
	
	public void testReadFile() {
		HashMap<String, double[]> values = Utility.readFile("/home/shang/namphuon/workspace/BioSim/gcm/run-1.tsd");
		System.out.println();		
	}
}
