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
	private boolean outputSBOL, outputSBML, outputLPN, exportOutput;

	public CompilerOptions() {
		this.verilogFiles = new ArrayList<>();
		this.outputSBOL = false;
		this.outputSBML = false;
		this.outputLPN = false;
		this.exportOutput = false;
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
	
	public String getOutputDirectory(){
		return this.outDir;
	}
	
	public void exportCompiler(boolean turnOn) {
		this.exportOutput = true;
	}
	
	public boolean isExportOn() {
		return this.exportOutput;
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
	
	public void verifyCompilerSetup() throws VerilogCompilerException {
		
		if(isOutputLPN()){
			if(!isImplementatonModuleIdSet() && !isTestbenchModuleIdSet()){
				throw new VerilogCompilerException("Both the implementation module identifier and the testbench module identifier field must be set to produce and LPN model.");
			}
		}
		
		//if(!isOutputDirectorySet()){
		//	//user want to export result from compiler
		//	if(isOutputSBML() || isOutputSBOL() || isOutputLPN()){
		//		throw new VerilogCompilerException("The output directory was not set");
		//	}
		//}
		
		//if(!isOutputFileNameSet() && isOutputLPN()) {
		//	throw new VerilogCompilerException("You must provide an output file name to export an LPN model.");
		//}
	}

}