package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
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

	public VerilogCompiler runCompiler(String[] args) {
		
		VerilogCompiler compiledVerilog = null;
		try {
			CommandLine cmds = VerilogRunner.parseCommandLine(args);
			CompilerOptions setupOpt = VerilogRunner.createCompilerOptions(cmds);
			compiledVerilog = VerilogRunner.runVerilogCompiler(setupOpt);
			
			if(setupOpt.isGenerateSBOL()){
				compiledVerilog.generateSBOL();
			}
			else {
				compiledVerilog.generateSBML();
				if(setupOpt.isGenerateLPN()){
					compiledVerilog.flattenSBML();
					compiledVerilog.generateLPN();
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BioSimException e) {
			e.printStackTrace();
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
		} catch (VerilogCompilerException e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			e.printStackTrace();
		}
		return compiledVerilog;
	}

}