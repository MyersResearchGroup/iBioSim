package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;

public class YosysScriptGenerator {

	private StringBuilder cmdBuilder;
	private String inputFilePath, outputDir, outputName;
	private String abc_cmd;

	public YosysScriptGenerator(String outputDirectory, String outputFileName) {
		this.inputFilePath = "";
		this.abc_cmd = "";
		this.cmdBuilder = new StringBuilder();
		this.outputDir = outputDirectory;
		this.outputName = outputFileName;
	}

	public void read_verilog(String verilogPath) {
		this.inputFilePath = verilogPath;
	}
	
	public void setAbc_cmd(String options, String selections) {
		abc_cmd = "abc " + "-" + options + " " + selections + "; ";
	}


	public String generateScript() {
			String outputFile = outputDir + File.separator + outputName;
			cmdBuilder.append("read_verilog " + inputFilePath + "; ");
			cmdBuilder.append("flatten; ");
			cmdBuilder.append("splitnets -ports; ");
			cmdBuilder.append("hierarchy -auto-top; ");
			cmdBuilder.append("proc; ");
			cmdBuilder.append("techmap; ");
			cmdBuilder.append("opt; ");
			cmdBuilder.append(abc_cmd);
			cmdBuilder.append("opt; ");
			cmdBuilder.append("hierarchy -auto-top; ");
			cmdBuilder.append("show -format pdf -prefix " + outputFile + "; ");
			cmdBuilder.append("write_edif " + outputFile + ".edif; ");
			cmdBuilder.append("write_json " + outputFile + ".json; ");
			cmdBuilder.append("write_verilog " + outputFile + ".v; ");
			return cmdBuilder.toString();
	}
	
}
