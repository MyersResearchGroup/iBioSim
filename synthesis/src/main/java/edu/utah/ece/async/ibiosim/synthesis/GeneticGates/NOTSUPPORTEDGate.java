package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode.NodeInteractionType;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogParser;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogToSBOL;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;

public class NOTSUPPORTEDGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedNotSupported;
	private ModuleDefinition md;
	private ArrayList<FunctionalComponent> inputs, outputs;
	
	public NOTSUPPORTEDGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.md = md;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
	}
	
	public void setVerilogForGate(File verilogFile) throws XMLStreamException, IOException, BioSimException, SBOLValidationException, ParseException, VerilogCompilerException {
		VerilogParser verilogParser = new VerilogParser();
		VerilogModule verilogModule = verilogParser.parseVerilogFile(verilogFile);
		
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
		this.md = decomposedSBOLGate;
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
		if (decomposedNotSupported == null && md != null) {
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
	
	
	private DecomposedGraphNode getNodeFromParticipationRole(DecomposedGraph graph, Interaction interaction, URI participationType) {
		List<DecomposedGraphNode> nodes = new ArrayList<>();
		for(Participation participation : interaction.getParticipations()) {
			if(participation.containsRole(participationType)) {
				FunctionalComponent participant = participation.getParticipant();
				DecomposedGraphNode node = graph.getNode(participant);
				nodes.add(node);
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
	
	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph graph = new DecomposedGraph();
		for(FunctionalComponent fc : md.getFunctionalComponents()) {
			DecomposedGraphNode newNode = new DecomposedGraphNode(fc);
			graph.addNode(newNode);
			
			if(fc.getDirection().equals(DirectionType.IN)) {
				graph.setNodeAsLeaf(newNode);
			}
			else if(fc.getDirection().equals(DirectionType.OUT)) {
				graph.setNodeAsOutput(newNode);
			}
		}
		for(Interaction interaction : md.getInteractions()) {
			if(interaction.containsType(SystemsBiologyOntology.INHIBITION)) {
				DecomposedGraphNode childNode = getNodeFromParticipationRole(graph, interaction, SystemsBiologyOntology.INHIBITOR);
				DecomposedGraphNode parentNode = getNodeFromParticipationRole(graph, interaction, SystemsBiologyOntology.INHIBITED);
				graph.addNodeRelationship(parentNode, childNode, NodeInteractionType.REPRESSION);
			}
			else if(interaction.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				DecomposedGraphNode parentNode = getNodeFromParticipationRole(graph, interaction, SystemsBiologyOntology.PRODUCT);
				DecomposedGraphNode childNode = getNodeFromParticipationRole(graph, interaction, SystemsBiologyOntology.TEMPLATE);
				graph.addNodeRelationship(parentNode, childNode, NodeInteractionType.PRODUCTION);
			}
			
		}
		
		return graph;
	}

	@Override
	public List<ComponentDefinition> getListOfInputsAsComponentDefinition() {
		List<ComponentDefinition> cdList = new ArrayList<>();
		for(FunctionalComponent fc : inputs) {
			cdList.add(fc.getDefinition());
		}
		return cdList;
	}

	@Override
	public List<ComponentDefinition> getListOfOutputsAsComponentDefinition() {
		List<ComponentDefinition> cdList = new ArrayList<>();
		for(FunctionalComponent fc : outputs) {
			cdList.add(fc.getDefinition());
		}
		return cdList;
	}

}
