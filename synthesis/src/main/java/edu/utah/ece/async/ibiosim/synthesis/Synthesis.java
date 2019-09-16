package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.IOException;

import edu.utah.ece.async.ibiosim.dataModels.util.Executables;

public class Synthesis {
	private final Runtime atacs, yosys;
	
	
	public Synthesis(){
		atacs = Runtime.getRuntime();
		yosys = Runtime.getRuntime();
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
	
	public int runDecomposition(String outputDirectory, boolean decomposedToNand) throws IOException, InterruptedException {
		String yosysScript = "";
		String[] command = new String[] {"yosys", "-s", yosysScript};
		File outDir = new File(outputDirectory);
	    Process decomposer = yosys.exec(command, Executables.envp, outDir);
	    yosys.exec(command, null, outDir);
	    
	    int exitValue = 0;
	    if (decomposer != null) {
	    	exitValue = decomposer.waitFor();
	    }
	    return exitValue;
	}
	
}
