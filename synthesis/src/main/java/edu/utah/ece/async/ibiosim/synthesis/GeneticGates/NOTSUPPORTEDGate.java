package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogParser;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogToSBOL;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;

public class NOTSUPPORTEDGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedNotSupported;
	private ModuleDefinition md;
	private VerilogModule verilogModule;
	private ArrayList<FunctionalComponent> inputs, outputs;
	
	public NOTSUPPORTEDGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.md = md;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
	}
	
	public void setVerilogForGate(File verilogFile) throws XMLStreamException, IOException, BioSimException, SBOLValidationException, ParseException, VerilogCompilerException {
		VerilogParser verilogParser = new VerilogParser();
		verilogModule = verilogParser.parseVerilogFile(verilogFile);
	}
	
	
	@Override
	public GateType getType() {
		return GateType.NOTSUPPORTED;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

	@Override
	public void addInputMolecule(FunctionalComponent inputMolecule) {
		inputs.add(inputMolecule);	
	}

	@Override
	public void addOutputMolecule(FunctionalComponent outputMolecule) {
		outputs.add(outputMolecule);	
	}

	@Override
	public ModuleDefinition getModuleDefinition() {
		return this.md; 
	}

	@Override
	public List<FunctionalComponent> getListOfInputs() {
		return this.inputs;
	}

	@Override
	public List<FunctionalComponent> getListOfOutputs() {
		return this.outputs;
	}

	@Override
	public boolean containsInput(FunctionalComponent fc) {
		if(inputs.isEmpty()) {
			return false;
		}
		return inputs.contains(fc);
	}

	@Override
	public boolean containsOutput(FunctionalComponent fc) {
		if(outputs.isEmpty()) {
			return false;
		}
		return outputs.contains(fc);
	}

	@Override
	public DecomposedGraph getDecomposedGraph() {
		if (decomposedNotSupported == null && verilogModule != null) {
			decomposedNotSupported = createDecomposedGate(); 
		}
		return decomposedNotSupported;
	}
	
	private void setInputAndOutputForGate(ModuleDefinition md) {
		for(FunctionalComponent fc : md.getFunctionalComponents()) {
			if(fc.getDirection().equals(DirectionType.IN)) {
				addInputMolecule(fc);
			}
			else if(fc.getDirection().equals(DirectionType.OUT)){
				addOutputMolecule(fc);
			}
		}
	}
	
	private DecomposedGraph createDecomposedGate() {
		VerilogToSBOL converter = new VerilogToSBOL(true);
		SBOLDocument decomposedSBOLDoc = null;
		try {
			decomposedSBOLDoc = converter.convertVerilog2SBOL(verilogModule).getSBOLDocument();
		} 
		catch (SBOLException | SBOLValidationException | ParseException | VerilogCompilerException e) {
			e.printStackTrace();
		}
		assert(decomposedSBOLDoc.getRootModuleDefinitions().size() == 1);
		ModuleDefinition decomposedSBOLGate = decomposedSBOLDoc.getRootModuleDefinitions().iterator().next();
		setInputAndOutputForGate(decomposedSBOLGate);
		
		DecomposedGraph graph = new DecomposedGraph();
		graph.createDecomposedGraph(decomposedSBOLGate);
		return graph;
	}

}
