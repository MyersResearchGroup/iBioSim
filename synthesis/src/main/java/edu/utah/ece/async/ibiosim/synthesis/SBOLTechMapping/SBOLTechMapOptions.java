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

	private String outputFileName, outDir;
	private boolean outputDot, outputSBOL, printCoveredGates;
	private SBOLUtility sbolUtility;
	private SBOLDocument library, specification;
	
	public SBOLTechMapOptions(){
		this.sbolUtility = SBOLUtility.getInstance();
		this.library = this.sbolUtility.createSBOLDocument();
		
	}
	
	public void setOutputFileName(String fileName) {
		this.outputFileName = fileName;
	}
	
	public void setOutputDirectory(String dirPath) {
		this.outDir = dirPath;
	}
	
	public void addLibraryFile(String fileFullPath) {

		try {
			File file = sbolUtility.getFile(fileFullPath);
			if(file.isFile()) {
				SBOLDocument libFile = sbolUtility.parseSBOLFile(file);
				library.createCopy(libFile);
			}
			else if(file.isDirectory()){
				ArrayList<SBOLDocument> libDocs = sbolUtility.loadSBOLDir(fileFullPath);
				SBOLDocument mergeDoc = sbolUtility.mergeSBOLDocuments(libDocs);
				library.createCopy(mergeDoc);

			}
		} 
		catch (SBOLValidationException | IOException | SBOLConversionException e) {
			e.printStackTrace();
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
	
	public SBOLDocument getSpefication() throws SBOLValidationException, IOException, SBOLConversionException {
		return specification;
	}
	
	public SBOLDocument getLibrary() throws SBOLValidationException, IOException, SBOLConversionException {
		return this.library;
	}

	public String getOuputFileName() {
		return this.outputFileName;
	}
	
	public String getOutputFileDir() {
		return this.outDir;
	}
	
}
