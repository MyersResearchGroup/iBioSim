package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VCompiler_TestEnvironment {

	public VerilogCompiler runCompiler(String[] args) throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		
		VerilogCompiler compiledVerilog = null;
			CommandLine cmds = VerilogRunner.parseCommandLine(args);
			CompilerOptions setupOpt = VerilogRunner.createCompilerOptions(cmds);
			compiledVerilog = VerilogRunner.runVerilogCompiler(setupOpt.getVerilogFiles());
			
			if(setupOpt.isGenerateSBOL()){
				compiledVerilog.generateSBOL(setupOpt.isOutputFlatModel());
			}
			else {
				compiledVerilog.generateSBML();
				if(setupOpt.isGenerateLPN()){
					//iBioSim's flattening method will not perform flattening if the hierarchical SBML models are not exported into a file.
					String outputDirectory = setupOpt.getOutputDirectory();
					compiledVerilog.exportSBML(outputDirectory);
					String hierModelFullPath = outputDirectory + File.separator + setupOpt.getTestbenchModuleId() + ".xml";
					SBMLDocument flattenSBML = compiledVerilog.flattenSBML(outputDirectory, hierModelFullPath);
					compiledVerilog.generateLPN(setupOpt.getImplementationModuleId(), setupOpt.getTestbenchModuleId(), flattenSBML);
				}
			}
			
			if(setupOpt.isExportOn()) {
				String outputDirectory = setupOpt.getOutputDirectory();
				if(setupOpt.isGenerateSBOL()) {
					compiledVerilog.exportSBOL(outputDirectory);
				}
				if(setupOpt.isGenerateSBML()){
					compiledVerilog.exportSBML(outputDirectory);
				}
				if(setupOpt.isGenerateLPN()) {
					compiledVerilog.exportLPN(outputDirectory, setupOpt.getOutputFileName());
				}
			}
		return compiledVerilog;
	}

}