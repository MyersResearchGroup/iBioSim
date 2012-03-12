package verification.timed_state_exploration.zone;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class ZoneTest {

	static Zone zone1, zone2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Set up first Zone.
		int [] timers1 = new int[]{7, 2, 6, 3};
		int[][] matrix1 = new int[6][6];
		matrix1[0] = new int[]{0, 0, 17, 12, 16, 13};
		matrix1[1] = new int[]{0, 0, 7, 2, 6, 3};
		matrix1[2] = new int[]{-7, 0, 0, 0, 0, 0};
		matrix1[3] = new int[]{-2, 0, 0, 0, 0, 0};
		matrix1[4] = new int[]{-6, 0, 0, 0, 0, 0};
		matrix1[5] = new int[]{-3, 0, 0, 0, 0, 0};
		
		zone1 = new Zone(timers1, matrix1);
		
		// Set up second Zone.
		int[] timers2 = new int[]{2, 3, 6, 7};
		int[][] matrix2 = new int[6][6];
		matrix2[0] = new int[]{0, 0, 12, 13, 16, 17};
		matrix2[1] = new int[]{0, 0, 7, 2, 6, 3};
		matrix2[2] = new int[]{-2, 0, 0, 0, 0, 0};
		matrix2[3] = new int[]{-3, 0, 0, 0, 0, 0};
		matrix2[4] = new int[]{-6, 0, 0, 0, 0, 0};
		matrix2[5] = new int[]{-7, 0, 0, 0, 0, 0};
		
		zone2 = new Zone(timers2, matrix2);
	}

	@Test
	public void testHashCode() { 
		
		assertEquals(zone1.hashCode(), zone2.hashCode());
	}

	@Test
	public void testZone() {
		
		assertTrue(zone1.equals(zone2));
		
	}

	@Test
	public void testGetDBMIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUpperBoundbyTransitionIndex() {
	
		int expected2 = 12;
		int expected3 = 13;
		int expected6 = 16;
		int expected7 = 17;
		int actual2 = zone1.getUpperBoundbyTransitionIndex(2);
		int actual3 = zone1.getUpperBoundbyTransitionIndex(3);
		int actual6 = zone1.getUpperBoundbyTransitionIndex(6);
		int actual7 = zone1.getUpperBoundbyTransitionIndex(7);
		
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected6, actual6);
		assertEquals(expected7, actual7);
	}

	@Test
	public void testGetUpperBoundbydbmIndex() {
		
		int expected2 = 12;
		int expected3 = 13;
		int expected6 = 16;
		int expected7 = 17;
		int actual2 = zone1.getUpperBoundbydbmIndex(1);
		int actual3 = zone1.getUpperBoundbydbmIndex(2);
		int actual6 = zone1.getUpperBoundbydbmIndex(3);
		int actual7 = zone1.getUpperBoundbydbmIndex(4);
		
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected6, actual6);
		assertEquals(expected7, actual7);
	}

	@Test
	public void testGetLowerBoundbyTransitionIndex() {
		int expected2 = -2;
		int expected3 = -3;
		int expected6 = -6;
		int expected7 = -7;
		int actual2 = zone1.getLowerBoundbyTransitionIndex(2);
		int actual3 = zone1.getLowerBoundbyTransitionIndex(3);
		int actual6 = zone1.getLowerBoundbyTransitionIndex(6);
		int actual7 = zone1.getLowerBoundbyTransitionIndex(7);
		
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected6, actual6);
		assertEquals(expected7, actual7);
	}

	@Test
	public void testGetLowerBoundbydbmIndex() {
		int expected2 = -2;
		int expected3 = -3;
		int expected6 = -6;
		int expected7 = -7;
		int actual2 = zone1.getLowerBoundbydbmIndex(1);
		int actual3 = zone1.getLowerBoundbydbmIndex(2);
		int actual6 = zone1.getLowerBoundbydbmIndex(3);
		int actual7 = zone1.getLowerBoundbydbmIndex(4);
		
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected6, actual6);
		assertEquals(expected7, actual7);
	}

	@Test
	public void testEqualsZone() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testMergeZone(){
		Zone z = zone1.mergeZones(zone2);
		
		System.out.print(z);
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
