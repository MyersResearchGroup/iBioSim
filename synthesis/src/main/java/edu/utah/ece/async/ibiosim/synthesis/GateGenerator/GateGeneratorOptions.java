package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * Setup class to run GateGeneration. 
 * @author Tramy Nguyen
 *
 */
public class GateGeneratorOptions {

	private SBOLUtility sbolUtility;
	private String outFileName, outDir, selectedSBHRepo;
	private List<File> tuFiles;
	private boolean allGates, outputNOTGates, outputNORGates, outputORGates, outputWiredORGates;
	private boolean outputNANDGates, outputANDGates, outputNOTSUPPORTEDGates;

	public GateGeneratorOptions() {
		this.sbolUtility = SBOLUtility.getSBOLUtility();
		this.tuFiles = new ArrayList<>();
	}

	public void addTUFile(String path) throws FileNotFoundException {
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException();
		}
		else if(file.isDirectory()) {
			List<String> result = new ArrayList<>();
			search(".*\\.xml", file, result);

			for (String s : result) {
				System.out.println(s);
				addTUFile(new File(s));
			}
		}
		else {
			addTUFile(file);
		}
	}
	
	public void addTUFile(File file) {
		if(file.isFile()) {
			this.tuFiles.add(file);
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

	public List<SBOLDocument> getTUSBOLDocumentList() throws SBOLValidationException, IOException, SBOLConversionException{
		List<SBOLDocument> tuList = new ArrayList<>();
		for(File file : this.tuFiles) {
			SBOLDocument tu = sbolUtility.parseSBOLFile(file);
			tuList.add(tu);
		}
		return tuList;
	}

	public String getOutputFileName() throws GateGenerationExeception {
		if(this.outFileName == null || this.outFileName.isEmpty()) {
			throw new GateGenerationExeception("No output file name was provided.");
		}
		return this.outFileName;
	}

	public String getOutputDirectory() throws GateGenerationExeception {
		if(this.outDir == null || this.outDir.isEmpty()) {
			throw new GateGenerationExeception("No output directory was provided.");
		}
		return this.outDir;
	}

	public void setOutputFileName(String fileName) {
		this.outFileName = fileName;
	}

	public void setOutputDirectory(String dirPath) {
		this.outDir = dirPath;
	}

	public void setOutputAllLibrary(boolean allLibrary) {
		this.allGates = allLibrary;
	}

	public void setOutputNOTLibrary(boolean outNOT) {
		this.outputNOTGates = outNOT;
	}

	public void setOutputNORLibrary(boolean outNOR) {
		this.outputNORGates = outNOR;
	}

	public void setOutputORLibrary(boolean outOR) {
		this.outputORGates = outOR;
	}

	public void setOutputWiredORLibrary(boolean outOR) {
		this.outputWiredORGates = outOR;
	}

	public void setOutputNANDLibrary(boolean outNAND) {
		this.outputNANDGates = outNAND;
	}

	public void setOutputANDLibrary(boolean outAND) {
		this.outputANDGates = outAND;
	}

	public void setOutputNOTSUPPORTEDLibrary(boolean outNotSupported) {
		this.outputNOTSUPPORTEDGates = outNotSupported;
	}

	public void setSelectedSBHRepo(String sbhRepository) {
		this.selectedSBHRepo = sbhRepository;
	}

	public boolean outputAllLibrary() {
		return this.allGates;
	}

	public boolean outputNOTSUPPORTEDLibrary() {
		return this.outputNOTSUPPORTEDGates;
	}

	public boolean outputANDLibrary() {
		return this.outputANDGates;
	}

	public boolean outputNANDLibrary() {
		return this.outputNANDGates;
	}

	public boolean outputORLibrary() {
		return this.outputORGates;
	}

	public boolean outputWiredORLibrary() {
		return this.outputWiredORGates;
	}

	public boolean outputNORLibrary() {
		return this.outputNORGates;
	}

	public boolean outputNOTLibrary() {
		return this.outputNOTGates;
	}

	public String getSelectedSBHRepo() throws GateGenerationExeception {
		if(this.selectedSBHRepo == null || this.selectedSBHRepo.isEmpty()) {
			throw new GateGenerationExeception("No SynBioHub repository was provided.");
		}
		return this.selectedSBHRepo;
	}
}
