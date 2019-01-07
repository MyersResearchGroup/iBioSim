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
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
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
	private Map<String, VerilogModule> verilogModules;
	private Map<String, WrappedSBML> vmoduleToSBML;
	private Map<String, WrappedSBOL> vmoduleToSBOL;
	private LPN lpn;
	private String outDir, outFileName, tb_id, imp_id, tb_fullPath;
	private SBMLDocument flatSBMLDocument;
	private boolean isFlatModel;
	
	/**
	 * Load the verilog files into a binary tree to begin parsing.
	 * @throws VerilogCompilerException 
	 */
	public VerilogCompiler(CompilerOptions compilerOptions) throws VerilogCompilerException {
	
		this.stcList = new ArrayList<>();
		this.verilogModules = new HashMap<>();
		this.vmoduleToSBML = new HashMap<>();
		this.vmoduleToSBOL = new HashMap<>();
		this.isFlatModel = compilerOptions.isOutputFlatModel();
		
		if(compilerOptions.isGenerateLPN()) {
			this.imp_id = compilerOptions.getImplementationModuleId();
			this.tb_id = compilerOptions.getTestbenchModuleId();
		}
		if(compilerOptions.isExportOn()) {
			this.outDir = compilerOptions.getOutputDirectory();
			if(compilerOptions.isGenerateLPN()) {
				this.outFileName = compilerOptions.getOutputFileName();
			}
		}
		
		for(File file : compilerOptions.getVerilogFiles()) {
			parseFile(file);
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
	
	public void generateSBOL() throws SBOLException, SBOLValidationException, ParseException, VerilogCompilerException { 
		for(VerilogModule verilogModule : this.verilogModules.values()) {
			VerilogToSBOL v2sbol_conv = new VerilogToSBOL(isFlatModel); 
			
			WrappedSBOL sbolData = v2sbol_conv.convertVerilog2SBOL(verilogModule);
			this.vmoduleToSBOL.put(verilogModule.getModuleId(), sbolData);
		}
	}
	
	public void generateSBML() throws ParseException { 
		for(VerilogModule verilogModule : this.verilogModules.values()) { 
			WrappedSBML sbmlModel = VerilogToSBML.convertVerilogToSBML(verilogModule);
			String verilogModuleId = verilogModule.getModuleId();
			this.vmoduleToSBML.put(verilogModuleId, sbmlModel);
			
			//If the user wants to generate an LPN model, then we have to remember one of the SBML file name that was exported.
			String filePath = verilogModuleId + ".xml";
			if(this.tb_id != null && this.tb_id.equals(verilogModuleId)) {
				this.tb_fullPath = this.outDir + File.separator + filePath;
			}
		}
	}
	
	public void flattenSBML() throws BioSimException, XMLStreamException, IOException{
		//Flatten the SBML models and then export to the same directory 
		BioModel sbmlDoc = new BioModel(this.outDir); 
		if(this.tb_fullPath == null) {
			throw new FileNotFoundException("No SBML file was provided that described the testbench to perform SBML flattening.");
		}
		
		//Loading the testbench file will also load the implementation file as well since externalModelDefinition is used.
		boolean isDocumentLoaded = sbmlDoc.load(this.tb_fullPath);
		if(!isDocumentLoaded) {
			throw new BioSimException("Unable to perform flattening for the following SBML file " + this.tb_fullPath, "Error converting SBML to LPN");
		}
		SBMLDocument flattenDoc = sbmlDoc.flattenModel(true);
		this.flatSBMLDocument = flattenDoc;
		String flatSBMLPath = this.outDir + File.separator + this.imp_id + "_" + this.tb_id + "_flattened.xml";
		exportSBML(flattenDoc, flatSBMLPath);
	}
	
	public void generateLPN() { 
		WrappedSBML tbWrapper = getSBMLWrapper(this.tb_id);
		WrappedSBML impWrapper = getSBMLWrapper(this.imp_id);
		this.lpn = SBMLToLPN.convertSBMLtoLPN(tbWrapper, impWrapper, this.flatSBMLDocument);

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
	
	public Map<String, WrappedSBML> getSBMLWrapperMapper(){
		return this.vmoduleToSBML;
	}

	public WrappedSBOL getSBOLWrapper(String verilogModule_id) {
		return this.vmoduleToSBOL.get(verilogModule_id);
	}
	
	public LPN getLPN(){
		return this.lpn;
	}
	
	public void exportSBML() throws SBMLException, FileNotFoundException, XMLStreamException{
		for (Map.Entry<String, WrappedSBML> entry : this.vmoduleToSBML.entrySet()) {
			String verilogModuleId = entry.getKey();
			SBMLDocument sbmlDocument = entry.getValue().getSBMLDocument();
			exportSBML(sbmlDocument, this.outDir + File.separator + verilogModuleId + ".xml");
		}
	}
	
	public void exportLPN(){
		exportLPN(this.lpn, this.outDir + File.separator + this.outFileName + ".lpn");
	}
	
	public void exportSBOL() throws IOException, SBOLConversionException{
		for (Map.Entry<String, WrappedSBOL> entry : this.vmoduleToSBOL.entrySet()) {
			String verilogModuleId = entry.getKey();
			SBOLDocument sbolDocument = entry.getValue().getSBOLDocument();
			exportSBOL(sbolDocument, this.outDir + File.separator + verilogModuleId + ".xml");
		}
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