package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class YosysScriptGenerator {

	private final String yosysScriptName = "ibiosimYosysScript.sh";
	private String inputFilePath, outputDir, outputName;
	private String abc_cmd;

	public YosysScriptGenerator(String outputDirectory, String outputFileName) {
		this.inputFilePath = "";
		this.abc_cmd = "";
		this.outputDir = outputDirectory;
		this.outputName = outputFileName;
	}

	public void read_verilog(String verilogPath) {
		this.inputFilePath = verilogPath;
	}
	
	public void setAbc_cmd(String options, String selections) {
		abc_cmd = "abc " + options + " " + selections + " " + "\n";
	}


	public void generateScript() {
		try {
			FileWriter writer = new FileWriter(this.outputDir + File.separator + yosysScriptName);
			String outputFile = outputDir + File.separator + outputName;
			writer.write("#!/bin/bash" + "\n");
			writer.write("input_verilog " + inputFilePath + "\n");
			writer.write("flatten " + "\n");
			writer.write("splitnets -ports " + "\n");
			writer.write("hierarchy -auto-top " + "\n");
			writer.write("proc " + "\n");
			writer.write("techmap " + "\n");
			writer.write("opt " + "\n");
			writer.write(abc_cmd);
			writer.write("show -format pdf -prefix " + outputFile + "\n");
			writer.write("write_edif " + outputFile + ".edif" + "\n");
			writer.write("write_json " + outputFile + ".json" + "\n");
			writer.write("write_verilog " + outputFile + ".v" + "\n");

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getScriptLocation() {
		return this.outputDir + File.separator + yosysScriptName;
	}


}
