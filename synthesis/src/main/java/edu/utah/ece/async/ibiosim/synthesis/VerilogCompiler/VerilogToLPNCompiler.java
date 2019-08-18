package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.text.parser.ParseException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * Compile Verilog to LPN
 * 
 * @author Tramy Nguyen
 */
public class VerilogToLPNCompiler {
	private Map<String, VerilogModule> verilogModuleMap; //Map the name of a Verilog module ID to its VerilogModule java object
	private Map<String, WrappedSBML> verilogModuleToSBMLMap;
	
	public VerilogToLPNCompiler() {
		this.verilogModuleMap = new HashMap<>();
		this.verilogModuleToSBMLMap = new HashMap<>();
	}
	
	public VerilogModule parseVerilogFile(File file) throws XMLStreamException, IOException, BioSimException {
		VerilogParser parser = new VerilogParser();
		VerilogModule verilogModule = parser.parseVerilogFile(file);
		verilogModuleMap.put(verilogModule.getModuleId(), verilogModule);
		return verilogModule;
	}
	
	public void addVerilog(VerilogModule verilogModule) throws VerilogCompilerException {
		if(verilogModule == null) {
			throw new VerilogCompilerException("verilog is null");
		}
		verilogModuleMap.put(verilogModule.getModuleId(), verilogModule);
	}
	
	public VerilogModule getVerilogModule(String verilogModuleId) {
		return verilogModuleMap.get(verilogModuleId);
	}
	
	public LPN compileToLPN(VerilogModule specification, VerilogModule testbench, String outputDirForCompilation) throws ParseException, VerilogCompilerException, BioSimException, XMLStreamException, IOException {
		WrappedSBML	specModel = generateSBMLFromVerilog(specification);
		WrappedSBML	tbModel = generateSBMLFromVerilog(testbench);
		
		SBMLDocument flattenSBMLDoc = flattenSBML(testbench.getModuleId(), outputDirForCompilation);
		LPN lpn = SBMLToLPN.convertSBMLtoLPN(tbModel, specModel, flattenSBMLDoc);
		return lpn;
	}
	
	public LPN compileToLPN(VerilogModule verilogModule) throws XMLStreamException, IOException, BioSimException, ParseException, VerilogCompilerException {
		WrappedSBML	sbmlModel = generateSBMLFromVerilog(verilogModule);
		LPN lpn = SBMLToLPN.convertSBMLtoLPN(sbmlModel.getSBMLDocument());
		return lpn;
	}
	
	
	
	public WrappedSBML generateSBMLFromVerilog(VerilogModule verilogModule) throws ParseException, VerilogCompilerException {
		VerilogToSBML v2sbml = null;
		if(verilogModule.getNumSubmodules() > 0) {
			Map<String, VerilogModule> referredModules = new HashMap<>();
			for(VerilogModuleInstance subModule : verilogModule.getSubmodules()) {
				String moduleId = subModule.getModuleReference();
				VerilogModule module = verilogModuleMap.get(moduleId);
				if(module == null) {
					throw new VerilogCompilerException("Unable to find the referenced Verilog Module " + moduleId);
				}
				referredModules.put(moduleId, module);
			}
			v2sbml = new VerilogToSBML(referredModules);
		}
		else { 
			v2sbml = new VerilogToSBML();
		}
		WrappedSBML	sbmlModel = v2sbml.convertVerilogToSBML(verilogModule);
		String verilogModuleId = verilogModule.getModuleId();
		this.verilogModuleToSBMLMap.put(verilogModuleId, sbmlModel);
		return sbmlModel;
	}
	
	public void exportSBML(SBMLDocument document, String fullPath) throws SBMLException, FileNotFoundException, XMLStreamException { 
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBMLToFile(document, fullPath);
	}
	
	public void exportLPN(LPN lpn, String fullPath) {
		lpn.save(fullPath);
	}
	
	public SBMLDocument flattenSBML(String verilogModuleId, String outputDirectory) throws BioSimException, XMLStreamException, IOException{
		if(verilogModuleId == null || verilogModuleId.isEmpty()) {
			throw new FileNotFoundException("The given verilogModuleId was not provided to perform SBML flattening.");
		}
		
		//iBioSim's flattening method will not perform flattening if the hierarchical SBML models are not exported into a file.
		WrappedSBML tbWrapper = verilogModuleToSBMLMap.get(verilogModuleId);
		String hierModelFullPath = outputDirectory + File.separator + verilogModuleId + ".xml";
		exportSBML(tbWrapper.getSBMLDocument(), hierModelFullPath);
		
		for(VerilogModuleInstance submodule : verilogModuleMap.get(verilogModuleId).getSubmodules()) {
			String refModule = submodule.getModuleReference();
			WrappedSBML sbmlModel = verilogModuleToSBMLMap.get(submodule.getModuleReference());
			exportSBML(sbmlModel.getSBMLDocument(), outputDirectory + File.separator + refModule + ".xml"); 
		}
				
		BioModel sbmlDoc = new BioModel(outputDirectory); 
		
		//Loading the testbench file will also load the implementation file as well since externalModelDefinition is used.
		boolean isDocumentLoaded = sbmlDoc.load(hierModelFullPath);
		if(!isDocumentLoaded) {
			throw new BioSimException("Unable to perform flattening for the following SBML file " + hierModelFullPath, "Error Flattening SBML Files");
		}
		SBMLDocument flattenDoc = sbmlDoc.flattenModel(true);
		return flattenDoc;
	}
}
