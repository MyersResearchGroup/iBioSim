package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
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
public class VerilogReader {

	public VerilogCompiler runCompiler(String[] args) {
		
		VerilogCompiler compiledResult = null;
		try {
			CommandLine cmds = Main.parseCommandLine(args);
			CompilerOptions setupOpt = Main.setCompilerOptions(cmds);
			compiledResult = Main.runVerilogCompiler(setupOpt);
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
		return compiledResult;
	}

	protected String getFile(String name) {
		return this.getClass().getResource(File.separator + name).getFile();
	}

}