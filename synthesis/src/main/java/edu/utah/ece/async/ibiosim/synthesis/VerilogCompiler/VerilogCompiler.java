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

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Lexer;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Source_textContext;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;
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
	
	/**
	 * Load the verilog files into a binary tree to begin parsing.
	 * @throws VerilogCompilerException 
	 */
	public VerilogCompiler(List<File> verilogFiles) throws VerilogCompilerException {
	
		this.stcList = new ArrayList<>();
		this.verilogModules = new HashMap<>();
		this.vmoduleToSBML = new HashMap<>();
		this.vmoduleToSBOL = new HashMap<>();
		
		for(File file : verilogFiles) {
			parseFile(file);
		}
	}
	
	
	/**
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws BioSimException
	 */
	public void parseVerilog() throws XMLStreamException, IOException, BioSimException {
		for(Source_textContext vFile : this.stcList) {
			
			VerilogParser v = new VerilogParser();
			ParseTreeWalker.DEFAULT.walk(v, vFile);
			
			VerilogModule verilogModule = v.getVerilogModule();
			this.verilogModules.put(verilogModule.getModuleId(), verilogModule);
		}
	}
	
	public void compileVerilogOutputData(boolean generateFlatModel) throws SBOLException, SBOLValidationException, ParseException, VerilogCompilerException {
		for(VerilogModule verilogModule : this.verilogModules.values()) {
			if(verilogModule.getNumContinousAssignments() > 0 && verilogModule.getNumInitialBlock() == 0 && verilogModule.getNumAlwaysBlock() == 0) {
				generateSBOL(generateFlatModel, verilogModule);
			}
			else {
				generateSBML(verilogModule);
			}
		}
	}
	
	public boolean containsSBMLData() {
		return this.vmoduleToSBML.isEmpty()? false : true;
	}
	
	public boolean containsSBOLData() {
		return this.vmoduleToSBOL.isEmpty()? false : true;
	}
	
	public void generateSBOL(boolean generateFlatModel, VerilogModule verilogModule) throws SBOLException, SBOLValidationException, ParseException, VerilogCompilerException {
		VerilogToSBOL v2sbol_conv = new VerilogToSBOL(generateFlatModel); 
		WrappedSBOL sbolData = v2sbol_conv.convertVerilog2SBOL(verilogModule);
		this.vmoduleToSBOL.put(verilogModule.getModuleId(), sbolData);

	}
	
	public void generateSBML(VerilogModule verilogModule) throws ParseException {
		VerilogToSBML v2sbml = null;
		if(verilogModule.getNumSubmodules() > 0) {
			Map<String, VerilogModule> referredModules = new HashMap<>();
			for(VerilogModuleInstance subModule : verilogModule.getSubmodules()) {
				String moduleId = subModule.getModuleReference();
				VerilogModule module = verilogModules.get(moduleId);
				referredModules.put(moduleId, module);
			}
			v2sbml = new VerilogToSBML(referredModules);
		}
		else { 
			v2sbml = new VerilogToSBML();
		}
		WrappedSBML	sbmlModel = v2sbml.convertVerilogToSBML(verilogModule);
		String verilogModuleId = verilogModule.getModuleId();
		this.vmoduleToSBML.put(verilogModuleId, sbmlModel);

	}
	
	public SBMLDocument flattenSBML(String outputDirectory, String fileFullPath) throws BioSimException, XMLStreamException, IOException{
		//Flatten the SBML models and then export to the same directory 
		BioModel sbmlDoc = new BioModel(outputDirectory); 
		if(fileFullPath == null) {
			throw new FileNotFoundException("fileFullPath was not provided to perform SBML flattening.");
		}
		
		//Loading the testbench file will also load the implementation file as well since externalModelDefinition is used.
		boolean isDocumentLoaded = sbmlDoc.load(fileFullPath);
		if(!isDocumentLoaded) {
			throw new BioSimException("Unable to perform flattening for the following SBML file " + fileFullPath, "Error Flattening SBML Files");
		}
		SBMLDocument flattenDoc = sbmlDoc.flattenModel(true);
		return flattenDoc;
	}
	
	public void generateLPN(String implementationModuleId, String testbenchModuleId, String outputDirectory) throws SBMLException, XMLStreamException, ParseException, BioSimException, IOException {
		//iBioSim's flattening method will not perform flattening if the hierarchical SBML models are not exported into a file.
		exportSBML(getMappedSBMLWrapper(), outputDirectory);	
		String hierModelFullPath = outputDirectory + File.separator + testbenchModuleId + ".xml";
		SBMLDocument flattenDoc = flattenSBML(outputDirectory, hierModelFullPath);
		generateLPN(implementationModuleId, testbenchModuleId, flattenDoc);
	}
	
	public void generateLPN(String implementationModuleId, String testbenchModuleId, SBMLDocument sbmlDoc) {
		WrappedSBML tbWrapper = getSBMLWrapper(testbenchModuleId);
		WrappedSBML impWrapper = getSBMLWrapper(implementationModuleId);
		this.lpn = SBMLToLPN.convertSBMLtoLPN(tbWrapper, impWrapper, sbmlDoc);
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
	
	public Map<String, WrappedSBML> getMappedSBMLWrapper(){
		return this.vmoduleToSBML;
	}
	
	public Map<String, WrappedSBOL> getMappedSBOLWrapper(){
		return this.vmoduleToSBOL;
	}

	public WrappedSBOL getSBOLWrapper(String verilogModule_id) {
		return this.vmoduleToSBOL.get(verilogModule_id);
	}
	
	public LPN getLPN(){
		return this.lpn;
	}
	
	public void exportSBML(Map<String, WrappedSBML> sbmlMapper, String outputDirectory) throws SBMLException, FileNotFoundException, XMLStreamException{
		for (Map.Entry<String, WrappedSBML> entry : sbmlMapper.entrySet()) {
			String verilogModuleId = entry.getKey();
			SBMLDocument sbmlDocument = entry.getValue().getSBMLDocument();
			exportSBML(sbmlDocument, outputDirectory + File.separator + verilogModuleId + ".xml");
		}
	}
	
	public void exportLPN(String outputDirectory, String outputFileName){
		exportLPN(this.lpn, outputDirectory + File.separator + outputFileName + ".lpn");
	}
	
	public void exportSBOL(Map<String, WrappedSBOL> sbolMapper, String outputDirectory) throws IOException, SBOLConversionException{
		for (Map.Entry<String, WrappedSBOL> entry : sbolMapper.entrySet()) {
			String verilogModuleId = entry.getKey();
			SBOLDocument sbolDocument = entry.getValue().getSBOLDocument();
			exportSBOL(sbolDocument, outputDirectory + File.separator + verilogModuleId + ".xml");
		}
	}
	
	private void exportLPN(LPN lpn, String fullPath) {
		lpn.save(fullPath);
	}
	
	public void exportSBML(SBMLDocument document, String fullPath) throws SBMLException, FileNotFoundException, XMLStreamException { 
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBMLToFile(document, fullPath);
	}

	public void exportSBOL(SBOLDocument document, String fullPath) throws IOException, SBOLConversionException {
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