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
	private boolean generateSBOL, generateSBML, generateLPN, exportOutput, flatModel;

	
	public CompilerOptions() {
		this.verilogFiles = new ArrayList<>();
		this.generateSBOL = false;
		this.generateSBML = false;
		this.generateLPN = false;
		this.exportOutput = false;
		this.flatModel = false;
	}
	
	public void addVerilogFile(File file){
		if(file.isFile()) {
			this.verilogFiles.add(file);
		}
	}
	
	public File addVerilogFile(String path) throws FileNotFoundException {
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException();
		}
		addVerilogFile(file);
		return file; 
	}

	private void search(final String pattern, final File folder, List<String> result) {
		for (final File f : folder.listFiles()) {
			if (f.isDirectory()) {
				search(pattern, f, result);
			}
			if (f.isFile()) {
				if (f.getName().matches(pattern)) {
					result.add(f.getAbsolutePath());
				}
			}
		}
	}
	
	public void addVerilogPath(String path) throws FileNotFoundException {
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException();
		}
		else if(file.isDirectory()) {
			List<String> result = new ArrayList<>();
			search(".*\\.v", file, result);

			for (String s : result) {
				System.out.println(s);
				addVerilogFile(new File(s));
			}
		}
		else {
			addVerilogFile(file);
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
	
	public void setGenerateSBOL(boolean isSBOL) {
		this.generateSBOL = isSBOL;
	}
	
	public void setGenerateSBML(boolean isSBML) {
		this.generateSBML = isSBML;
	}
	
	public void setGenerateLPN(boolean isLPN) {
		this.generateLPN = isLPN;
	}
	
	public void setFlatModel(boolean isFlatModel) {
		this.flatModel = isFlatModel;
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
	
	public String getTestbenchModuleId() throws VerilogCompilerException {
		if(!isTestbenchModuleIdSet()) {
			throw new VerilogCompilerException("Testbench module identifier was not set.");
		}
		return this.tbModuleId;
	}
	
	public String getImplementationModuleId() throws VerilogCompilerException {
		if(!isImplementatonModuleIdSet()) {
			throw new VerilogCompilerException("Implementation module identifier was not set.");
		}
		return this.impModuleId;
	}
	
	public String getOutputFileName() throws VerilogCompilerException {
		if(!isOutputFileNameSet()) {
			throw new VerilogCompilerException("Output file name was not set.");
		}
		return this.outputFileName;
	}
	
	public String getOutputDirectory() throws VerilogCompilerException{
		if(!isOutputDirectorySet()) {
			throw new VerilogCompilerException("The output directory was not set");
		}
		return this.outDir;
	}
	
	public void exportCompiler(boolean turnOn) {
		this.exportOutput = true;
	}
	
	public boolean isExportOn() {
		return this.exportOutput;
	}
	
	public boolean isGenerateSBOL() {
		return this.generateSBOL;
	}

	public boolean isOutputFlatModel() {
		return this.flatModel;
	}
	
	public boolean isGenerateSBML() {
		return this.generateSBML;
	}
	
	public boolean isGenerateLPN() {
		return this.generateLPN;
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