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
package backend.verification.timed_state_exploration.zoneProject;

import java.io.File;
import java.util.ArrayList;

import backend.verification.platu.main.Options;
import backend.verification.platu.project.Project;
import backend.verification.timed_state_exploration.zoneProject.Zone;
import main.java.edu.utah.ece.async.lpn.parser.LPN;
import main.java.edu.utah.ece.async.util.GlobalConstants;
import main.java.edu.utah.ece.async.util.exceptions.BioSimException;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Conolse7_26_2012 {
	
	/**
	 * Parameters are 
	 * 		'b' = no subsets
	 * 		'p' = no supersets
	 * @param args
	 * @throws BioSimException 
	 */
	public static void main(String[] args) throws BioSimException {

		String lpnFileDirectory = "";
		boolean subset = true;
		boolean superset = true;
		boolean rateOptimization = false; // rate optimization is off by default
		boolean octagons = false; // Determines whether the method is zones or octagons.
		boolean flags = false;
		if(args.length > 3 || args.length == 0){
			System.out.println("Incorrect number of parameters");
			return;
		}
		else if (args.length == 1){
			lpnFileDirectory = args[0];
		}
		else{
			String commands = args[0];
			lpnFileDirectory = args[1];
			flags = commands.contains("-");
			subset = !commands.contains("b") & flags;
			superset = !commands.contains("p") & flags;
			rateOptimization = commands.contains("r") & flags;
			octagons = commands.contains("o") & flags;
			System.out.println("Subset is : " + subset);
			System.out.println("Superset is : " + superset);
			System.out.println("Octagons is : " + octagons);
			System.out.println("The input string was : " + commands);
			System.out.println("The file directory was: " + lpnFileDirectory);
		}
		
		File directory = new File(lpnFileDirectory);
		
		if(!directory.exists()){
			System.out.println("Directory does not exist!");
			return;
		}

		if(!directory.isDirectory()){
			System.out.println("Must pass a directory");
			return;
		}
		
		String[] lpnList = directory.list();
		
		if(lpnList == null || lpnList.length == 0){
			System.out.println("Directory is emtpy!");
			return;
		}
		
		LPN lpn = new LPN();

		lpn.load(directory + GlobalConstants.separator + lpnList[0]);
		Options.set_TimingLogFile(directory + GlobalConstants.separator
				+ lpnList[0] + ".tlog");

		
		
		ArrayList<LPN> selectedLPNs = new ArrayList<LPN>();
		// Add the current LPN to the list.
		selectedLPNs.add(lpn);
		for (int i=1; i < lpnList.length; i++) {
			 String curLPNname = lpnList[i];
			 LPN curLPN = new LPN();
			 curLPN.load(directory + GlobalConstants.separator + curLPNname);
			 selectedLPNs.add(curLPN);
		}
		

		// Extract boolean variables from continuous variable inequalities.
		for(int i=0; i<selectedLPNs.size(); i++){
			selectedLPNs.get(i).parseBooleanInequalities();
		}
		
		
		/**
		 * This is what selects the timing analysis.
		 * The method setTimingAnalysisType sets a static variable
		 * in the Options class that is queried by the search method.
		 */
		if(octagons){
			Options.setTimingAnalsysisType("octagon");
		}
		else{
			Options.setTimingAnalsysisType("zone");
		}

		Options.set_resetOnce(false);
		
		Zone.setSubsetFlag(subset);
		Zone.setSupersetFlag(superset);
		Options.set_rateOptimization(rateOptimization);

		Project timedStateSearch = new Project(selectedLPNs);

		timedStateSearch.search();

		Options.setTimingAnalsysisType("off");
	}

}