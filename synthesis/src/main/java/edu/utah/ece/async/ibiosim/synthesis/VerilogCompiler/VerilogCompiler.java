package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import VerilogConstructs.VerilogModule;
//import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.Source_textContext;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Lexer;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Source_textContext;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * Compiles a list of Verilog modules to SBML/SBOL/LPN.
 * 
 * @author Tramy Nguyen
 */
public class VerilogCompiler {
	
	private List<Source_textContext> stcList;
	private Map<String, VerilogConstructs.VerilogModule> verilogModules;
	private Map<String, WrappedSBML> vmoduleToSBML;
	private Map<String, WrappedSBOL> vmoduleToSBOL;
	private String outputDirectory, outputFileName, testbench_id, implementation_id, testbenchFullpath;
	private SBMLDocument flatSBMLDocument;
	private boolean hasOutput, outputLPN;
	
	/**
	 * Load the verilog files into a binary tree to begin parsing.
	 * @param options: an instance of the CompilationOptions object to retrieve the Verilog file(s).
	 */
	public VerilogCompiler(CompilerOptions options) {
	
		this.stcList = new ArrayList<>();
		this.verilogModules = new HashMap<>();
		this.vmoduleToSBML = new HashMap<>();
		this.vmoduleToSBOL = new HashMap<>();
		this.hasOutput = options.hasOutput();
		this.outputLPN = options.isOutputLPN();
		
		if(options.isOutputDirectorySet()) {
			this.outputDirectory = options.getOutputDirectory();
		}
		if(options.isImplementatonModuleIdSet()) {
			this.implementation_id = options.getImplementationModuleId();
		}
		if(options.isTestbenchModuleIdSet()) {
			this.testbench_id = options.getTestbenchModuleId();
		}
		if(options.isOutputFileNameSet()) {
			this.outputFileName = options.getOutputFileName();
		}
		
		for(File file : options.getVerilogFiles()) {
			parseFile(file);
		}
	}
	
	public void verifyCompilerSetup() throws VerilogCompilerException {
		if(this.stcList.isEmpty()) {
			//files were not parsed as Verilog
			throw new VerilogCompilerException("Input file(s) were not Verilog files that could be compiled.");
		}

		if(this.hasOutput) {
			if(this.outputDirectory == null || this.outputDirectory.isEmpty()) {
				//Require user to specify an output to export the result of the compiler into
				throw new VerilogCompilerException("No output directory was provided");
			}
			if(outputLPN) {
				//Make sure the user knows what the output file name is when exporting to LPN
				if(this.outputFileName == null || this.outputFileName.isEmpty()) {
					throw new VerilogCompilerException("The compiler cannot export an LPN model because the output file name was not provided."); 
				}
				//A testbench can contain more than one submodules for simulation. 
				//LPN conversion can only handle one submodule so make sure the user specify the identifier of the implementation verilog module and the identifier of the testbench module to output LPN
				if(this.testbench_id == null || this.testbench_id.isEmpty()) {
					throw new VerilogCompilerException("The testbench module identifier field was not provided to produce and LPN model.");
				}
				if(this.implementation_id == null || this.implementation_id.isEmpty()) {
					throw new VerilogCompilerException("The implementation module identifier field was not provided to produce and LPN model.");
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws BioSimException
	 */
	public void compile() throws XMLStreamException, IOException, BioSimException {
		
		for(Source_textContext vFile : this.stcList) {
			VerilogParser v = new VerilogParser();
			ParseTreeWalker.DEFAULT.walk(v, vFile);
			
			VerilogModule verilogModule = v.getVerilogModule();
			
			this.verilogModules.put(verilogModule.getModuleId(), verilogModule);
		}

			
	}
	
	public void generateSBOL() throws SBOLValidationException, ParseException, IOException, SBOLConversionException, VerilogCompilerException {
		for(VerilogModule verilogModule : this.verilogModules.values()) { 
			WrappedSBOL sbolData = VerilogToSBOL.convertVerilog2SBOL(verilogModule);
			this.vmoduleToSBOL.put(verilogModule.getModuleId(), sbolData);
			exportSBOL(sbolData.getSBOLDocument(), this.outputDirectory + File.separator + verilogModule);
		}
	}
	
	/**
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws BioSimException
	 * @throws ParseException: An SBML Exception has occurred while trying to convert Verilog expressions to SBML.
	 */
	public void generateSBML() throws XMLStreamException, IOException, BioSimException, ParseException {
		for(VerilogModule verilogModule : this.verilogModules.values()) { 
			WrappedSBML sbmlModel = VerilogToSBML.convertVerilogToSBML(verilogModule);
			this.vmoduleToSBML.put(verilogModule.getModuleId(), sbmlModel);
		}
		
		for (Map.Entry<String, WrappedSBML> entry : this.vmoduleToSBML.entrySet()) {
			String verilogModuleId = entry.getKey();
			SBMLDocument sbmlDocument = entry.getValue().getSBMLDocument();
			
			//If the user wants to generate an LPN model, we must store both the implementation and testbench verilog files.
			String filePath = verilogModuleId + ".xml";
			if(this.testbench_id != null && this.testbench_id.equals(verilogModuleId)) {
				this.testbenchFullpath = filePath;
			}
			exportSBML(sbmlDocument, this.outputDirectory + File.separator + filePath);
		}

	}
	
	public void generateLPN() throws XMLStreamException, IOException, BioSimException {
		//Flatten the SBML models and then export to the same directory 
		BioModel sbmlDoc = new BioModel(this.outputDirectory); 
		if(this.testbenchFullpath == null) {
			throw new FileNotFoundException("ERROR: Unable to locate the SBML file that describes the testbench.");
		}
		boolean isDocumentLoaded = sbmlDoc.load(this.testbenchFullpath);
		if(!isDocumentLoaded) {
			throw new BioSimException("Unable to load SBML file that will be used for flattening.", "Error when converting SBML to LPN");
		}
		SBMLDocument flattenDoc = sbmlDoc.flattenModel(true);
		this.flatSBMLDocument = flattenDoc;
		String flatSBMLPath = this.outputDirectory + File.separator + this.implementation_id + "_" + this.testbench_id + "_flattened.xml";
		exportSBML(flattenDoc, flatSBMLPath);
				
		WrappedSBML tbWrapper = getSBMLWrapper(this.testbench_id);
		WrappedSBML impWrapper = getSBMLWrapper(this.implementation_id);
		LPN lpn = SBMLToLPN.convertSBMLtoLPN(tbWrapper, impWrapper, this.flatSBMLDocument);
		
		exportLPN(lpn, this.outputDirectory + File.separator + this.outputFileName + ".lpn");
	}
	
	/**
	 * A mapping of the verilog modules that were parsed from the given verilog files.
	 * @return The name of each verilog module are mapped to a structured data object that represent the information found within the Verilog file(s). 
	 */
	public Map<String, VerilogModule> getVerilogModules() {
		return this.verilogModules;
	}
	
	/**
	 * Each verilog module that were parsed is stored into an SBMLWrapper object.
	 * This method will allow a user to access the SBMLWrapper based on the verilogModule_id that is assigned to the SBMLWrapper.
	 * 
	 * @param verilogModule_id : The Verilog Module identifier is used to locate the SBMLWrapper the the verilog module was converted into.
	 * @return The corresponding SBMLWrapper that is assigned to the given verilogModule_id
	 */
	public WrappedSBML getSBMLWrapper(String verilogModule_id) {
		return this.vmoduleToSBML.get(verilogModule_id);
	}
	
	public WrappedSBOL getSBOLWrapper(String verilogModule_id) {
		return this.vmoduleToSBOL.get(verilogModule_id);
	}
	
	private void exportLPN(LPN lpn, String fullPath) {
		lpn.save(fullPath);
	}
	
	private void exportSBML(SBMLDocument document, String fullPath) throws SBMLException, FileNotFoundException, XMLStreamException {
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBMLToFile(document, fullPath);
	}

	private void exportSBOL(SBOLDocument document, String fullPath) throws IOException, SBOLConversionException {
		SBOLWriter.write(document, fullPath);
	}
	
	private void parseFile(File file) {
		InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			Lexer lexer = new Verilog2001Lexer(CharStreams.fromStream(inputStream));
			TokenStream tokenStream = new CommonTokenStream(lexer);
			Verilog2001Parser parser = new Verilog2001Parser(tokenStream);

			this.stcList.add(parser.source_text());

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}