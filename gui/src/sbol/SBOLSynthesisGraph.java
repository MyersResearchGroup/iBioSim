package sbol;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbolstandard.core.DnaComponent;

import biomodel.annotation.AnnotationUtility;
import biomodel.parser.BioModel;

public class SBOLSynthesisGraph {

	private String modelID;
	private String submodelID;
	private HashMap<SBOLSynthesisNode, List<SBOLSynthesisNode>> edges;
	private int nucleotideCount;
	private Set<String> signals;
	private SBOLSynthesisNode output;
	private List<SBOLSynthesisNode> inputs;
	private List<String> paths;
	
	public SBOLSynthesisGraph(BioModel biomodel, SBOLFileManager fileManager) {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		modelID = sbmlModel.getId();
		Set<SBOLSynthesisNode> nodes = constructGraph(sbmlModel, fileManager);
		output = identifyOutput(nodes);
		paths = buildPaths(getOutput());
//		print();
	}

	private Set<SBOLSynthesisNode> constructGraph(Model sbmlModel, SBOLFileManager fileManager) {
		HashMap<String, SBOLSynthesisNode> idToNode = new HashMap<String, SBOLSynthesisNode>();
		edges = new HashMap<SBOLSynthesisNode, List<SBOLSynthesisNode>>();
		nucleotideCount = 0;
		signals = new HashSet<String>();
		for (long i = 0; i < sbmlModel.getNumReactions(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			if (BioModel.isProductionReaction(sbmlReaction)) {
				SBOLSynthesisNode promoterNode = new SBOLSynthesisNode();
				int numRepressors = 0;
				for (long j = 0; j < sbmlReaction.getNumModifiers(); j++) {
					ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
					if (BioModel.isRepressor(sbmlModifier)) {
						numRepressors++;
						SBOLSynthesisNode repressorNode;
						if (idToNode.containsKey(sbmlModifier.getSpecies())) 
							repressorNode = idToNode.get(sbmlModifier.getSpecies());
						else {
							Species sbmlSpecies = sbmlModel.getSpecies(sbmlModifier.getSpecies());
							repressorNode = new SBOLSynthesisNode("s", sbmlSpecies, fileManager);
							nucleotideCount += repressorNode.getNucleotideCount();
							if (repressorNode.getSignal().length() > 0)
								signals.add(repressorNode.getSignal());
							idToNode.put(sbmlModifier.getSpecies(), repressorNode);
						}
						if (!edges.containsKey(promoterNode))
							edges.put(promoterNode, new LinkedList<SBOLSynthesisNode>());
						edges.get(promoterNode).add(repressorNode);	
					}
					else if (BioModel.isPromoter(sbmlModifier)) {
						idToNode.put(sbmlModifier.getSpecies(), promoterNode);
						promoterNode.setID(sbmlModifier.getSpecies());
					}
				}
				if (numRepressors == 0)
					promoterNode.setType("c");
				else if (numRepressors == 1)
					promoterNode.setType("i");
				else if (numRepressors == 2)
					promoterNode.setType("n");
				promoterNode.processDNAComponents(sbmlReaction, fileManager);
				nucleotideCount += promoterNode.getNucleotideCount();
				if (promoterNode.getSignal().length() > 0)
					signals.add(promoterNode.getSignal());
				for (long j = 0; j < sbmlReaction.getNumProducts(); j++) {
					SpeciesReference sbmlProduct = sbmlReaction.getProduct(j);
					SBOLSynthesisNode productNode;
					if (idToNode.containsKey(sbmlProduct.getSpecies())) 
						productNode = idToNode.get(sbmlProduct.getSpecies());
					else {
						Species sbmlSpecies = sbmlModel.getSpecies(sbmlProduct.getSpecies());
						productNode = new SBOLSynthesisNode("s", sbmlSpecies, fileManager);
						nucleotideCount += productNode.getNucleotideCount();
						if (productNode.getSignal().length() > 0)
							signals.add(productNode.getSignal());
						idToNode.put(sbmlProduct.getSpecies(), productNode);
					}
					if (!edges.containsKey(productNode))
						edges.put(productNode, new LinkedList<SBOLSynthesisNode>());
					edges.get(productNode).add(promoterNode);	
				}
			}
		}
		return new HashSet<SBOLSynthesisNode>(idToNode.values());
	}
	
	private SBOLSynthesisNode identifyOutput(Set<SBOLSynthesisNode> nodes) {
		HashMap<SBOLSynthesisNode, SBOLSynthesisNode> reverseEdges = new HashMap<SBOLSynthesisNode, SBOLSynthesisNode>();
		for (SBOLSynthesisNode node : nodes) {
			if (edges.containsKey(node))
				for (SBOLSynthesisNode childNode : edges.get(node))
					reverseEdges.put(childNode, node);
		}
		for (SBOLSynthesisNode node : nodes) {
			if (!reverseEdges.containsKey(node))
				return node;
		}
		return null;
	}
	
	public String getModelID() {
		return modelID;
	}
	
	public void setSubmodelID(String submodelID) {
		this.submodelID = submodelID;
	}
	
	public String getSubmodelID() {
		if (submodelID == null)
			submodelID = "";
		return submodelID;
	}
	
	public SBOLSynthesisNode getOutput() {
		if (output == null)
			output = new SBOLSynthesisNode("r");
		return output;
	}
	
//	public List<String> getInputSignals() {
//		List<String> inputSignals = new LinkedList<String>();
//		for (SBOLSynthesisNode input : inputs)
//			inputSignals.add(input.getSignal());
//		return inputSignals;
//	}
//	
//	public List<String> getInputIDs() {
//		List<String> inputIDs = new LinkedList<String>();
//		for (SBOLSynthesisNode input : inputs)
//			inputIDs.add(input.getID());
//		return inputIDs;
//	}
	
	public List<SBOLSynthesisNode> getInputs() {
		return inputs;
	}
	
	public SBOLSynthesisNode getInput(int inputIndex) {
		if (inputIndex < inputs.size())
			return inputs.get(inputIndex);
		else
			return null;
	}
	
	public List<SBOLSynthesisNode> postOrderNodes() {
		List<SBOLSynthesisNode> orderedNodes = new LinkedList<SBOLSynthesisNode>();
		List<SBOLSynthesisNode> currentNodes = new LinkedList<SBOLSynthesisNode>();
		currentNodes.add(getOutput());
		while (currentNodes.size() > 0) {
			orderedNodes.add(0, currentNodes.remove(0));
			if (edges.containsKey(orderedNodes.get(0))) {
				currentNodes.addAll(edges.get(orderedNodes.get(0)));
			}
		}
		return orderedNodes;
	}

	public List<SBOLSynthesisNode> getChildren(SBOLSynthesisNode node) {
		if (edges.containsKey(node))
			return edges.get(node);
		else
			return new LinkedList<SBOLSynthesisNode>();
	}
	
	public int getNucleotideCount() {
		return nucleotideCount;
	}
	
	public Set<String> getSignals() {
		return signals;
	}

	private void print() {
		System.out.println(modelID);
		for (String path : paths)
			System.out.println(path);
		System.out.println();
	}
	
	public List<String> getPaths(SBOLSynthesisNode node) {
		return buildPaths(node);
	}
	
	public List<String> getPaths() {
		if (paths == null)
			paths = buildPaths(getOutput());
		return paths;
	}
	
	private List<String> buildPaths(SBOLSynthesisNode node) {
		List<String> paths = new LinkedList<String>();
		if (node.equals(getOutput())) {
			inputs = new LinkedList<SBOLSynthesisNode>();
			buildPathsHelper(node, "r1", paths, true);
		} else 
			buildPathsHelper(node, "r1", paths, false);
		return paths;
	}
	
	private void buildPathsHelper(SBOLSynthesisNode node, String path, List<String> paths, boolean identifyInputs) {
		path = path + node.getType();
		if (edges.containsKey(node)) {
			int childNum = 0;
			for (SBOLSynthesisNode childNode : edges.get(node)) {
				childNum++;
				buildPathsHelper(childNode, path + childNum, paths, identifyInputs);
			}
		} else {
			paths.add(path);
			if (identifyInputs)
				inputs.add(node);
		}
	}
	
	public List<SBOLSynthesisNode> walkPaths(SBOLSynthesisNode startNode, List<String> paths) {
		List<SBOLSynthesisNode> destinationNodes = new LinkedList<SBOLSynthesisNode>();
		for (String path : paths) {
			path = path.substring(2);
			SBOLSynthesisNode currentNode = startNode;
			boolean validPath = true;
			while (path.length() > 1 && validPath) {
				int index = Integer.parseInt(path.substring(1,2)) - 1;
				if (edges.containsKey(currentNode) && index < edges.get(currentNode).size()) {
					currentNode = edges.get(currentNode).get(index);
					path = path.substring(2);
				} else
					validPath = false;
			}
			destinationNodes.add(currentNode);
		}
		return destinationNodes;
	}

}
