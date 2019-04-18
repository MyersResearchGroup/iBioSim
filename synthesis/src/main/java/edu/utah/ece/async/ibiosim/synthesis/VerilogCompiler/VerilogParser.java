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
 * Class to parse a verilog file 
 * 
 * @author Tramy Nguyen
 */
public class VerilogParser {
	

	public VerilogParser() {
	}
	
	public VerilogModule parseVerilogFile(File file) throws XMLStreamException, IOException, BioSimException {
		Source_textContext verilogFile = parseTextContext(file); 
		VerilogListener verilogListener = new VerilogListener();
		ParseTreeWalker.DEFAULT.walk(verilogListener, verilogFile);

		return verilogListener.getVerilogModule();
	}
	
	private Source_textContext parseTextContext(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);
		Lexer lexer = new Verilog2001Lexer(CharStreams.fromStream(inputStream));
		TokenStream tokenStream = new CommonTokenStream(lexer);
		Verilog2001Parser parser = new Verilog2001Parser(tokenStream);
		Source_textContext parsedVerilog = parser.source_text();
		inputStream.close();
		return parsedVerilog;
	}
	
}