package backend.verification.timed_state_exploration.zone;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

public class ZoneTest {

	static Zone zone1, zone2, zone3, zone4, zone5;
	
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
		
		// Set up third Zone.
		int[] timers3 = new int[]{2};
		int[][] matrix3 = new int[][]{
				{ 0,  0, 3},
				{ 0,  0, 3},
				{-1,  0, 0}
		};
		
		zone3 = new Zone(timers3, matrix3);
		
		// Set up fourth Zone.
		int[] timers4 = new int[]{1};
		int[][] matrix4 = new int[][]{
				{ 0,  0, 3},
				{ 0,  0, 3},
				{-1,  0, 0}
		};
		
		zone4 = new Zone(timers4, matrix4);
		
		// Set up fifth zone.
		int[] timers5 = new int[]{1, 2, 3};
		int[][] matrix5 = new int[][]{
				{ 0, 0, 2, 3, ZoneType.INFINITY},
				{ 0, 0, 2, 3, ZoneType.INFINITY},
				{-1, 0, 0, 0,             0},
				{-2, 0, 0, 0,             0},
				{ 0, 0, 0, 0,             0}
		};
		
		zone5 = new Zone(timers5, matrix5);
	}
	
	@Test
	public static void testInifinity()
	{
		int expected02 = 2;
		int expected03 = 2;
		int actual02 = zone5.getDbmEntry(0, 2);
		int actual03 = zone5.getDbmEntry(0, 3);
		
		assertEquals(expected02, actual02);
		assertEquals(expected03, actual03);
	}

	@Test
	public static void testHashCode() { 
		
		assertEquals(zone1.hashCode(), zone2.hashCode());
	}

	@Test
	public static void testZone() {
		
		assertTrue(zone1.equals(zone2));
		
	}

	@Test
	public static void testGetdbm() {
		int expected = 2;
		int actual = zone1.getDbmEntry(0, 1);
		assertEquals(expected, actual);
	}

	@Test
	public static void testGetUpperBoundbyTransitionIndex() {
	
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
	public static void testGetUpperBoundbydbmIndex() {
		
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
	public static void testGetLowerBoundbyTransitionIndex() {
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
	public static void testGetLowerBoundbydbmIndex() {
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
	public static void testEqualsZone() {
		assertFalse(zone3.equals(zone4));
	}
	
	@Test
	public static void testMergeZone(){
		ZoneType z = zone1.mergeZones(zone2);
		
		System.out.println("Merging zones 1 and 2 :");
		System.out.println(z);
	}
	
	@Test
	public static void testMergeZone2(){
		ZoneType z = zone3.mergeZones(zone4);
		
		
		System.out.println("Merging zones 3 and 4 :");
		System.out.println(z);
	}
	
	@SuppressWarnings("unused")
	@Test
	public static void testMergeZone3(){
		boolean exceptionThrown = false;
		
		try{
			ZoneType z = zone3.mergeZones(zone1);
		}
		catch(Exception e)
		{
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}
	
	/**
	 * Reads in zones for testing.
	 * @param zoneFile
	 * 			The file containing the test zones.
	 * @return
	 * 			An array of zones for testing.
	 * 			
	 */
	@SuppressWarnings("unused")
	private static ZoneType[] readTestZones(File zoneFile)
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
			
			read.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.print("File not found.");
		}
		
		
		return null;
	}
}
