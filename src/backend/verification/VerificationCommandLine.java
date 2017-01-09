package backend.verification;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;

import backend.lpn.parser.LPN;
import backend.util.GlobalConstants;
import backend.verification.platu.main.Options;
import backend.verification.platu.project.Project;



/**
 * This class provides script to run depth-first search and partial order reduction (in the platu package)
 * without the need for a GUI.
 * @author Zhen Zhang
 *
 */

public class VerificationCommandLine {
	
	static String separator = GlobalConstants.separator;
	
	public static void main (String[] args) {
		if (args.length == 0) {
			System.err.println("Error: Missing arguments.");
			System.exit(0);
		}
		String directory = null;
		File dir = null;
		ArrayList<LPN> lpnList = new ArrayList<LPN>();
		ArrayList<String> lpnNames = new ArrayList<String>();
		boolean allLPNs = false;
		for (int i=0; i<args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':  // options
				if (args[i].length() < 2)
		                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
				if (args[i].equals("-portb")) {
					Options.setPOR("tb");
					Options.setCycleClosingMthd("behavioral");					
					Options.setCycleClosingStrongStubbornMethd("cctb");				
				}
				else if (args[i].equals("-porbehavioral")) {
					Options.setPOR("behavioral");
					Options.setCycleClosingMthd("behavioral");
					Options.setCycleClosingStrongStubbornMethd("cctboff");
				}
				else if (args[i].equals("-portboff")) {
					Options.setPOR("tboff");
					Options.setCycleClosingMthd("behavioral");
					Options.setCycleClosingStrongStubbornMethd("cctboff");
				}
				// Directory should be provided as an argument starting with -dir.
				else if (args[i].contains("-dir=")) { 
					directory = args[i].trim().substring(5);
				}
				// Runtime, memory usage, and state count etc are written in the log file specified here.
				else if (args[i].contains("-log=")) { 
					Options.setLogName(args[i].trim().substring(5));
				}
				//			else if (args[i].contains("-memlim=")) {
				//				Options.setMemUpperBoundFlag();
				//				String memUpperBound = args[i].trim().replace("-memlim=", "");
				//				if(memUpperBound.contains("G")) {
				//					memUpperBound = memUpperBound.replace("G", "");					
				//					Options.setMemoryUpperBound((long)(Float.parseFloat(memUpperBound) * 1000000000));
				//				}
				//				if(memUpperBound.contains("M")) {
				//					memUpperBound = memUpperBound.replace("M", "");
				//					Options.setMemoryUpperBound((long)(Float.parseFloat(memUpperBound) * 1000000));
				//				}
				//			}
				// 
				else if (args[i].contains("-allLPNs")) {
					allLPNs = true;
				}
				else if (args[i].contains("-db")) {
					Options.setDebugMode(true);
					System.out.println("Debug mode is ON.");
				}
				//			else if (args[i].contains("-depQueue")) {
				//				Options.setUseDependentQueue();
				//				System.out.println("Use dependent queue.");
				//			}
				else if (args[i].contains("-disableDisablingError")) {
					Options.disableDisablingError();
					System.out.println("Disabling error was diabled.");
				}
				else if (args[i].contains("-sg")) {
					Options.setOutputSgFlag(true);
					System.out.println("Gererate state graphs.");
				}
				else if (args[i].contains("-prob")) {
					Options.setMarkovianModelFlag();
					System.out.println("Probabilistic LPNs.");
				}
				break;
			default: // input LPN file(s)
				if (!args[i].endsWith(".lpn")) {
					throw new IllegalArgumentException("Not a valid input LPN: "+args[i]);
				}
				lpnNames.add(args[i]);
				break;
			}			
		}
		if (directory != null) {
			dir = new File(directory);
			if (!dir.exists()) {
				System.err.println("Invalid direcotry. Exit.");
				System.exit(0);
			}
		}
		else {
			directory = System.getProperty("user.dir");
			dir = new File(directory);
			System.out.println(directory);
		}
		Options.setPrjSgPath(directory);
		// Options for printing the final numbers from search_dfs or search_dfsPOR. 
		Options.setOutputLogFlag(true);
		// If the "-allLPNs" option exists, then all LPNs under a directory (either specified by "-dir" or
		// the current directory by default) are considered. If this option is followed by user specified LPNs, they
		// get ignored.
		if (allLPNs) {
			File[] lpns = dir.listFiles(new FileExtentionFilter(".lpn"));
			lpnList.clear();
			for (int i=0; i < lpns.length; i++) {
				String curLPNname = lpns[i].getName();
				LPN curLPN = new LPN();
				curLPN.load(directory + separator + curLPNname);
				lpnList.add(curLPN);
			}
		}
		else {
			for (int i=0; i < lpnNames.size(); i++) {
				LPN curLPN = new LPN();
				curLPN.load(directory + separator + lpnNames.get(i));//load(directory + curLPNname);
				lpnList.add(curLPN);
			}
		}
		System.out.println("====== LPN loading order ========");
		for (int i=0; i<lpnList.size(); i++) {
			System.out.println(lpnList.get(i).getLabel());
		}
		Project untimed_dfs = new Project(lpnList);
		untimed_dfs.search();
	}
}
