package gcm2sbml;


import java.util.Properties;

import gcm2sbml.parser.GCMFile;
import junit.framework.TestCase;

import org.junit.Before;

public class GCMFileTest extends TestCase{

	@Before
	public void setUp() throws Exception {
	}
	
	public void testLoadandSave() { 
		GCMFile file = new GCMFile();
		file.load(filename);
		file.save("nand2.dot");
	}
	
	public void testAddProperty() {
		GCMFile file = new GCMFile();
		file.load(filename);
		Properties property = new Properties();
		property.put("hi", "fun");
		file.addInfluences("g -> a", property);
		file.save("nand3.dot");
	}
	
	String filename = "nand.dot";

}
