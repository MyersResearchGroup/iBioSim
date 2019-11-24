package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Lexer;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Source_textContext;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;

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