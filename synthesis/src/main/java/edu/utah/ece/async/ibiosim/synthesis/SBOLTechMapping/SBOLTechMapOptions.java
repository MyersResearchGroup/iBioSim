package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * Setup class to list different options for running SBOL technology mapping. 
 * @author Tramy Nguyen
 */
public class SBOLTechMapOptions {

	private String libFilePath, specFilePath, outputFileName, outDir;
	private boolean outputDot, outputSBOL, printCoveredGates;
	private SBOLUtility sbolUtility;
	
	public SBOLTechMapOptions(){
		this.sbolUtility = SBOLUtility.getInstance();
		
	}
	
	public void setOutputFileName(String fileName) {
		this.outputFileName = fileName;
	}
	
	public void setOutputDirectory(String dirPath) {
		this.outDir = dirPath;
	}
	
	public void setLibraryFile(String fileFullPath) {
		this.libFilePath = fileFullPath;
	}
	
	public void setSpecificationFile(String fileFullPath) {
		this.specFilePath = fileFullPath;
	}
	
	public void setOutputDotFile(boolean outputDot) {
		this.outputDot = outputDot;
	}
	
	public void setOutputSBOL(boolean outputSBOL) {
		this.outputSBOL = outputSBOL;
	}
	
	public void setPrintToTerminalCoveredGates(boolean coverGates) {
		this.printCoveredGates = coverGates;
	}
	
	public boolean printCoveredGates() {
		return this.printCoveredGates;
	}

	public boolean isOutputDOT() {
		return this.outputDot;
	}
	
	public boolean isOutputSBOL() {
		return this.outputSBOL;
	}
	
	public SBOLDocument getSpeficationFile() throws SBOLValidationException, IOException, SBOLConversionException {
		return sbolUtility.loadSBOLFile(specFilePath);
		
	}
	
	public SBOLDocument getLibraryFile() throws SBOLValidationException, IOException, SBOLConversionException {

		File libFile = sbolUtility.getFile(libFilePath);
		if(libFile.isFile()) {
			return sbolUtility.parseSBOLFile(libFile);
		}
		
		ArrayList<SBOLDocument> libDocs = sbolUtility.loadSBOLDir(libFilePath);
		libDocs = sbolUtility.loadSBOLDir(libFilePath);
		return sbolUtility.mergeSBOLDocuments(libDocs);

	}

	public String getOuputFileName() {
		return this.outputFileName;
	}
	
	public String getOutputFileDir() {
		return this.outDir;
	}
	
}
