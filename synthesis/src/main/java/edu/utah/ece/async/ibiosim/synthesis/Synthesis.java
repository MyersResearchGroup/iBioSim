package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.IOException;

import edu.utah.ece.async.ibiosim.dataModels.util.Executables;

public class Synthesis {
	private final Runtime atacs;
	
	
	public Synthesis(){
		atacs = Runtime.getRuntime();
	}
	
	public int runSynthesis(String lpnFile, String outputDirectory) throws IOException, InterruptedException {
		String[] command = new String[] {"atacs", "-tLllyssV", "-oddn", lpnFile};
		File outDir = new File(outputDirectory);
	    Process synthesizer = atacs.exec(command, Executables.envp, outDir);
	    atacs.exec(command, null, outDir);
	    
	    int exitValue = 0;
	    if (synthesizer != null) {
	    	exitValue = synthesizer.waitFor();
	    }
	    return exitValue;
	}
	
}
