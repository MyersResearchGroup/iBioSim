package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * Setup class to list different options for running SBOL technology mapping. 
 * @author Tramy Nguyen
 */
public class SBOLTechMapOptions {

	private String outputFileName, outDir;
	private boolean outputSBOL, isBranchBound, isExhaustive, isGreedy, listSpecNodes; 
	private int numOfSol;
	private SBOLUtility sbolUtility;
	private List<SBOLDocument> libFiles;
	private SBOLDocument specification;
	private Map<String, String> preselectedInfo;
	
	public SBOLTechMapOptions(){
		this.sbolUtility = SBOLUtility.getSBOLUtility();
		this.libFiles = new ArrayList<>();
		this.numOfSol = 0;
		this.preselectedInfo = new HashMap<>();
	}
	
	public void setOutputFileName(String fileName) {
		this.outputFileName = fileName;
	}
	
	public void setOutputDirectory(String dirPath) {
		this.outDir = dirPath;
	}
	
	public void addPreselection(String nodeId, String cdId) {
		if(!preselectedInfo.containsKey(nodeId)) {
			preselectedInfo.put(nodeId, cdId);
		}
	}
	
	public boolean hasPreselection() {
		return !preselectedInfo.isEmpty();
	}

	public void addLibraryFile(String path) throws SBOLValidationException, IOException, SBOLConversionException {
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException();
		}
		else if(file.isDirectory()) {
			List<String> result = new ArrayList<>();
			search(".*[.]xml", file, result);

			for (String s : result) {
				System.out.println(s);
				addLibFile(new File(s));
			}
		}
		else {
			addLibFile(file);
		}
	}
	
	public void addLibFile(File file) throws SBOLValidationException, IOException, SBOLConversionException {
		if(file.isFile()) {
			SBOLDocument libFile = sbolUtility.parseSBOLFile(file);
			this.libFiles.add(libFile);
		}
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
	
	public void setSpecificationFile(String fileFullPath) {
		try {
			File specFile = sbolUtility.getFile(fileFullPath);
			if(specFile.isFile()) {
				 this.specification = sbolUtility.parseSBOLFile(specFile);
			}
		} 
		catch (SBOLValidationException | IOException | SBOLConversionException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean  listSpecNodeIds(){
		return this.listSpecNodes;
	}
	
	public void setSpecNodeId() {
		this.listSpecNodes = true;
	}
	
	public void setNumOfSolutions(int value) {
		this.numOfSol = value;
	}

	public void setBranchBound(boolean isBranchBound) {
		this.isBranchBound = isBranchBound;
	}
	
	public void setExhaustive(boolean isExhaustive) {
		this.isExhaustive = isExhaustive;
	}
	
	public void setGreedy(boolean isGreedy) {
		this.isGreedy = isGreedy;
	}
	
	public void setOutputSBOL(boolean outputSBOL) {
		this.outputSBOL = outputSBOL;
	}
	
	public Map<String, String> getPreselection(){
		return this.preselectedInfo;
	}
	
	public int getNumOfSolutions() {
		return this.numOfSol;
	}
	
	public boolean isOutputSBOL() {
		return this.outputSBOL;
	}
	
	public boolean isBranchBound() {
		return this.isBranchBound;
	}
	public boolean isExhaustive() {
		return this.isExhaustive;
	}
	
	public boolean isGreedy() {
		return this.isGreedy;
	}
	
	public SBOLDocument getSpefication() throws SBOLValidationException, IOException, SBOLConversionException {
		return specification;
	}
	
	public List<SBOLDocument> getLibrary() throws SBOLValidationException, IOException, SBOLConversionException {
		return this.libFiles;
	}
	
	public String getOuputFileName() {
		return this.outputFileName;
	}
	
	public String getOutputDir() {
		return this.outDir;
	}
	
}
