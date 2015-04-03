package verification.timed_state_exploration.zoneProject;

import java.io.File;
import java.util.ArrayList;

import verification.platu.main.Options;
import verification.platu.project.Project;
import verification.timed_state_exploration.zoneProject.Zone;
import lpn.parser.LhpnFile;
import main.Gui;

public class Conolse7_26_2012 {
	
	/**
	 * Parameters are 
	 * 		'b' = no subsets
	 * 		'p' = no supersets
	 * @param args
	 */
	public static void main(String[] args) {

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
		
		LhpnFile lpn = new LhpnFile();

		lpn.load(directory + Gui.separator + lpnList[0]);
		Options.set_TimingLogFile(directory + Gui.separator
				+ lpnList[0] + ".tlog");

		
		
		ArrayList<LhpnFile> selectedLPNs = new ArrayList<LhpnFile>();
		// Add the current LPN to the list.
		selectedLPNs.add(lpn);
		for (int i=1; i < lpnList.length; i++) {
			 String curLPNname = lpnList[i];
			 LhpnFile curLPN = new LhpnFile();
			 curLPN.load(directory + Gui.separator + curLPNname);
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