package verification.timed_state_exploration.zoneProject;

import java.io.File;
import java.util.ArrayList;

import verification.platu.main.Options;
import verification.platu.project.Project;
import verification.timed_state_exploration.zoneProject.Zone;
import lpn.parser.LhpnFile;

public class Conolse7_26_2012 {
	
	/**
	 * Parameters are 
	 * 		'b' = no subsets
	 * 		'p' = no supersets
	 * @param args
	 */
	public static void main(String[] args) {

		String lpnFileDirectory = "";
		File f = new File(lpnFileDirectory);
		boolean subset = true;
		boolean superset = true;

		if(args.length > 2 || args.length == 0){
			System.out.println("Incorrect number of parameters");
			return;
		}
		else if (args.length == 1){
			lpnFileDirectory = args[0];
		}
		else{
			String commands = args[0];
			lpnFileDirectory = args[1];
			subset = !commands.contains("b");
			superset = !commands.contains("p");
			System.out.println("Subset is : " + subset);
			System.out.println("Superset is : " + superset);
			System.out.println("The input string was : " + commands);
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
		
		if(lpnList == null | lpnList.length == 0){
			System.out.println("Directory is emtpy!");
			return;
		}
		
		LhpnFile lpn = new LhpnFile();

		lpn.load(directory + File.separator + lpnList[0]);
		
		ArrayList<LhpnFile> selectedLPNs = new ArrayList<LhpnFile>();
		// Add the current LPN to the list.
		selectedLPNs.add(lpn);
		for (int i=1; i < lpnList.length; i++) {
			 String curLPNname = (String) lpnList[i];
			 LhpnFile curLPN = new LhpnFile();
			 curLPN.load(directory + File.separator + curLPNname);
			 selectedLPNs.add(curLPN);
		}
		

		/**
		 * This is what selects the timing analysis.
		 * The method setTimingAnalysisType sets a static variable
		 * in the Options class that is queried by the search method.
		 */
		Options.setTimingAnalsysisType("zone");

		Zone.setSubsetFlag(subset);
		Zone.setSupersetFlag(superset);

		Project timedStateSearch = new Project(selectedLPNs);

		timedStateSearch.search();

		Options.setTimingAnalsysisType("off");
	}

}