package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to get and set the necessary information needed to perform the Verilog compiler.
 * @author Tramy Nguyen
 *
 */
public class CompilerOptions {
	private List<File> verilogFiles;
	private String tbModuleId, impModuleId, outputFileName, outDir;
	private boolean outputSBOL, outputSBML, outputLPN, generateOutput, runSynth;

	public CompilerOptions() {
		this.verilogFiles = new ArrayList<>();
		this.outputSBOL = false;
		this.outputSBML = false;
		this.outputLPN = false;
		this.generateOutput = false;
		this.runSynth = false;
	}

	public void addVerilogFile(String fullFileName) throws FileNotFoundException {
		File verilogFile = new File(fullFileName);
		if(!verilogFile.exists()) {
			throw new FileNotFoundException();
		}
		if(verilogFile.isDirectory()) {
			addDirectoryVerilogFiles(verilogFile);
		}
		if(verilogFile.isFile()) {
			this.verilogFiles.add(verilogFile);
		}
	}
	
	public void addDirectoryVerilogFiles(File file) {
		for(File f : file.listFiles()) {
			this.verilogFiles.add(f);
		}
	}
	
	public void setTestbenchModuleId(String id) {
		this.tbModuleId = id;
	}
	
	public void setImplementationModuleId(String id) {
		this.impModuleId = id;
	}
	
	public void setSynthesis(boolean isSynth) {
		this.runSynth = isSynth;
	}
	
	public void setOutputSBOL(boolean isSBOL) {
		this.outputSBOL = isSBOL;
	}
	
	public void setOutputSBML(boolean isFlatSBML) {
		this.outputSBML = isFlatSBML;
	}
	
	public void setOutputLPN(boolean isLPN) {
		this.outputLPN = isLPN;
	}
	
	public void setOutputFileName(String fileName) {
		this.outputFileName = fileName;
	}
	
	public void setOutputDirectory(String directory) {
		this.outDir = directory;
	}
	
	public List<File> getVerilogFiles() {
		return this.verilogFiles;
	}
	
	public String getTestbenchModuleId() {
		return this.tbModuleId;
	}
	
	public String getImplementationModuleId() {
		return this.impModuleId;
	}
	
	public String getOutputFileName() {
		return this.outputFileName;
	}
	
	public String getOutputDirectory() {
		return this.outDir;
	}
	
	public void setOutputOn(boolean turnOn) {
		this.generateOutput = true;
	}
	
	public boolean hasOutput() {
		return this.generateOutput;
	}
	
	public boolean isSynthOn() {
		return this.runSynth;
	}
	
	public boolean isOutputSBOL() {
		return this.outputSBOL;
	}
	
	public boolean isOutputSBML() {
		return this.outputSBML;
	}
	
	public boolean isOutputLPN() {
		return this.outputLPN;
	}
	
	public boolean isTestbenchModuleIdSet() {
		return this.tbModuleId != null && !this.tbModuleId.isEmpty() ? true : false;
	}
	
	public boolean isImplementatonModuleIdSet() {
		return this.impModuleId != null && !this.impModuleId.isEmpty() ? true : false;
	}
	
	public boolean isOutputDirectorySet() {
		return this.outDir != null && !this.outDir.isEmpty() ? true : false;
	}
	
	public boolean isOutputFileNameSet() {
		return this.outputFileName != null && !this.outputFileName.isEmpty() ? true : false;
	}
	

}