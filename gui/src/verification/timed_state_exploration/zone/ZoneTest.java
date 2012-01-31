package verification.timed_state_exploration.zone;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

public class ZoneTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testHashCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testZone() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDBMIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUpperBoundbyTransitionIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUpperBoundbydbmIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLowerBoundbyTransitionIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLowerBoundbydbmIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsZone() {
		fail("Not yet implemented");
	}
	
	/**
	 * Reads in zones for testing.
	 * @param zoneFile
	 * 			The file containing the test zones.
	 * @return
	 * 			An array of zones for testing.
	 * 			
	 */
	private Zone[] readTestZones(File zoneFile)
	{
		try {
			Scanner read = new Scanner(zoneFile);
			
			while(read.hasNextLine())
			{
				String line = read.nextLine();
				line.trim();
				
				if(line.equals("start"))
				{
					
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.print("File not found.");
		}
		
		return null;
	}
}
