package verification;

import java.io.File;
import java.util.ArrayList;
import verification.platu.main.Options;
import verification.platu.project.Project;

import lpn.parser.LhpnFile;



/**
 * This class provides script to run depth-first search and partial order reduction (in the platu package)
 * without the need for a GUI.
 * @author Zhen Zhang
 *
 */

public class VerificationNoGui {
	
	public static void main (String[] args) {
		if (args.length == 0) {
			System.err.println("Error: Missing arguments.");
			System.exit(0);
		}
		//System.out.println("Enter the directory of all LPNs: ");
		//Scanner scanner = new Scanner(System.in);
		//String directory = scanner.nextLine();
		String directory = null; 		
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("-portb")) {
				Options.setPOR("tb");
				Options.setCycleClosingMthd("behavioral");
				Options.setCycleClosingAmpleMethd("cctb");				
			}
			else if (args[i].equals("-porbehavioral")) {
				Options.setPOR("behavioral");
				Options.setCycleClosingMthd("behavioral");
				Options.setCycleClosingAmpleMethd("none");
			}
			else if (args[i].equals("-porbehavioral")) {
				Options.setPOR("behavioral");
				Options.setCycleClosingMthd("behavioral");
				Options.setCycleClosingAmpleMethd("cctboff");
			}
			else if (args[i].equals("-portboff")) {
				Options.setPOR("tboff");
				Options.setCycleClosingMthd("behavioral");
				Options.setCycleClosingAmpleMethd("cctboff");
			}
			else if (args[i].contains("-dir=")) { // Directory should be provided as an argument starting with -dir:
				directory = args[i].trim().substring(5);
			}
			else if (args[i].contains("-log=")) { // Runtime, memory usage, and state count etc are written in the log file specified here.
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
			else if (args[i].contains("-disableDeadlockPreserve")) {
				Options.disablePORdeadlockPreserve();
			}
			else if (args[i].contains("-debugON")) {
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
			else if (args[i].contains("-generateStateGraph")) {
				Options.setOutputSgFlag(true);
				System.out.println("Gererate state graphs.");
			}
			else if (args[i].contains("-prob")) {
				Options.setMarkovianModelFlag();
				System.out.println("Probabilistic LPNs.");
			}
			
		}
		if (directory.trim().equals("") || directory == null) {
			System.out.println("Direcotry provided is empty. Exit.");
			System.exit(0);
		}
		File dir = new File(directory);
		if (!dir.exists()) {
			System.err.println("Invalid direcotry. Exit.");
			System.exit(0);
		}
		Options.setPrjSgPath(directory);
		// Options for printing the final numbers from search_dfs or search_dfsPOR. 
		Options.setOutputLogFlag(true);
		//Options.setDebugMode(false);
				
		File[] lpns = dir.listFiles(new FileExtentionFilter(".lpn"));
		ArrayList<LhpnFile> lpnList = new ArrayList<LhpnFile>();
		for (int i=0; i < lpns.length; i++) {
		 String curLPNname = lpns[i].getName();
		 LhpnFile curLPN = new LhpnFile();
		 curLPN.load(directory + curLPNname);
		 lpnList.add(curLPN);
		}		
		System.out.println("====== LPN loading order ========");
		for (int i=0; i<lpnList.size(); i++) {
			System.out.println(lpnList.get(i).getLabel());
		}
		Project untimed_dfs = new Project(lpnList);
		untimed_dfs.search();
	}
}
