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
	
	public int runGeneralizedCGateSynthesis(String lpnFile, String outputDirectory) throws IOException, InterruptedException {
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
	
	public int runAtomicGateSynthesis(String lpnFile, String outputDirectory) throws InterruptedException, IOException {
		String[] command = new String[] {"atacs", "-ot", "-ov", "-tL", "-llys", "-sV", "-oddn", lpnFile};
		File outDir = new File(outputDirectory);
	    Process synthesizer = atacs.exec(command, Executables.envp, outDir);
	    atacs.exec(command, null, outDir);
	    
	    int exitValue = 0;
	    if (synthesizer != null) {
	    	exitValue = synthesizer.waitFor();
	    }
	    return exitValue;
	}
	
	public int runSynthesis (String outputDirectory, String[] cmd) throws IOException, InterruptedException {
		File outDir = new File(outputDirectory);
		
	    Process decomposer = yosys.exec(cmd, Executables.envp, outDir);
	    yosys.exec(cmd, null, outDir);
	    
	    int exitValue = 0;
	    if (decomposer != null) {
	    	exitValue = decomposer.waitFor();
	    }
	    return exitValue;
	}
	
}
