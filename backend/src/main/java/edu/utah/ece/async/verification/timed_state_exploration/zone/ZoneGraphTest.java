/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zone;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ZoneGraphTest {

	Zone zone1, zone2, zone3, zone4, zone5, zone6;
	
	public static int INF = ZoneType.INFINITY;
	
	@Before
	public void setUp() throws Exception {

		int[] timers1 = new int[]{2, 3, 5};
		int[][] matrix1 = new int[][]{
				{0, 0, 0, 0, 0},
				{0, 0, 2, 2, 2},
				{0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0}
		};
		
		zone1 = new Zone(timers1, matrix1);
		
		int[] timers2 = new int[]{1, 2, 3};
		
		int[][] matrix2 = new int[][]{
				{0,   0,   0,   0,   0},
				{0,   0, INF, INF,   5},
				{0,   3,   0,  10,   2},
				{0, INF,  -4,   0, INF},
				{0, INF, INF,   2,   0}
		};
		
		zone2 = new Zone(timers2, matrix2);
		
		int[] timer3 = new int[]{0, 1, 6, 9};
		
		int[][] matrix3 = new int[][]{
				{0, 0, 3, 2, 1, 6},
				{0, 0, 3, 0, 1, 1},
				{-2, -2, 0, -2, -1, -1},
				{-1, 0, 3, 0, 1, 1},
				{0, -1, 2, -1, 0, 0},
				{-5, -1, 2, -1, 0, 0}
		};
		
		zone3 = new Zone(timer3, matrix3);
		
		int[] timer4 = new int[]{1, 6, 9, 0};
		
		int[][] matrix4 = new int[][]{
				{0,   0,   0,   0,   0,   0},
				{0,   0,   0, INF, INF,   3},
				{0, INF,   0,   1, INF, INF},
				{0, INF, INF,   0,   0, INF},
				{0,  -1, INF, INF,   0, INF},
				{0,  -2, INF, INF, INF,   0}
		};
		
		zone4 = new Zone(timer4, matrix4);
		

		// Set up fifth zone.
		int[] timers5 = new int[]{1};
		int[][] matrix5 = new int[][]{
				{0,  0, 2},
				{0,  0, 2},
				{0, -2, 0}
		};
		
		zone5 = new Zone(timers5, matrix5);
				
		// Set up sixth zone.
		  int[] timers6 = new int[]{1, 2, 3, 4};
		 int[][] matrix6 = new int[][]{
				 { 0, 0, 2, 4, 6, 2},
				 { 0, 0, 2, 2, 2, 2},
				 {-1, 0, 0, 0, 0, 0},
				 { 0, 0, 2, 0, 2, 0},
				 {-5, 0, 0, 0, 0, 0},
				 {-1, 0, 2, 0, 2, 0}
		 };
		 
		 zone6 = new Zone(timers6, matrix6);
	}

	@Test
	public void testToDot1() {
		try{
			PrintStream zone1DotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zone1.dot"));
			
			PrintStream zoneGraphDotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zoneGraph1.dot"));
			
			zone1.toDot(zone1DotFile);
			
			ZoneGraph g = ZoneGraph.extractZoneGraph(zone1);
			
			g.toDot(zoneGraphDotFile);
			
			Zone actual = g.extractZone();
			
			assertTrue(zone1.equals(actual));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testToDot2(){
		try{
			PrintStream zone1DotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zone2.dot"));
			
			PrintStream zoneGraphDotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zoneGraph2.dot"));
			
			zone2.toDot(zone1DotFile);
			
			ZoneGraph g = ZoneGraph.extractZoneGraph(zone2);
			
			g.toDot(zoneGraphDotFile);
			
			Zone actual = g.extractZone();
			
			assertTrue(zone2.equals(actual));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testToDot3(){
		try{
			PrintStream zone1DotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zone3.dot"));
			
			PrintStream zoneGraphDotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zoneGraph3.dot"));
			
			zone3.toDot(zone1DotFile);
			
			ZoneGraph g = ZoneGraph.extractZoneGraph(zone3);
			
			g.toDot(zoneGraphDotFile);
			
			Zone actual = g.extractZone();
			
			assertTrue(zone3.equals(actual));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testToDot5(){
		try{
			PrintStream zone1DotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zone5.dot"));
			
			PrintStream zoneGraphDotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zoneGraph5.dot"));
			
			zone5.toDot(zone1DotFile);
			
			ZoneGraph g = ZoneGraph.extractZoneGraph(zone5);
			
			g.toDot(zoneGraphDotFile);
			
			Zone actual = g.extractZone();
			
			assertTrue(zone5.equals(actual));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testToDot6(){
		try{
			PrintStream zone1DotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zone6.dot"));
			
			PrintStream zoneGraphDotFile = new PrintStream(new File("gui\\src\\verification" +
					"\\timed_state_exploration\\zone\\zoneGraph6.dot"));
			
			zone6.toDot(zone1DotFile);
			
			ZoneGraph g = ZoneGraph.extractZoneGraph(zone6);
			
			g.toDot(zoneGraphDotFile);
			
			Zone actual = g.extractZone();
			
			assertTrue(zone6.equals(actual));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEquals1(){
		ZoneGraph z1a = ZoneGraph.extractZoneGraph(zone1);
		ZoneGraph z1b = ZoneGraph.extractZoneGraph(zone1);
		
		assertTrue(z1a.equals(z1b));
	}
	
	@Test
	public void testEquals2(){
		ZoneGraph z = ZoneGraph.extractZoneGraph(zone2);
		
		assertTrue(z.equals(z));
	}
	
	@Test
	public void testEquals3(){
		ZoneGraph z3 = ZoneGraph.extractZoneGraph(zone3);
		ZoneGraph z5 = ZoneGraph.extractZoneGraph(zone5);
		
		assertFalse(z3.equals(z5));
	}
}
