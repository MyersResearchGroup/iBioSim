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
		decomposeGraph(nodes);
		output = identifyOutput(nodes);
		paths = buildPaths(getOutput());
//		print();
	}
	
	private void decomposeGraph(Set<SBOLSynthesisNode> nodes) {
		Set<SBOLSynthesisNode> typedSpeciesNodes = new HashSet<SBOLSynthesisNode>();
		Set<SBOLSynthesisNode> interNodes = new HashSet<SBOLSynthesisNode>();
		for (SBOLSynthesisNode node : nodes)
			if (node.getType().equals("p")) {
				if (edges.containsKey(node)) {
					int numActivators = 0;
					int numRepressors = 0;
					for (SBOLSynthesisNode tfNode : edges.get(node))
						if (tfNode.getType().startsWith("r")) {
							numRepressors++;
							typedSpeciesNodes.add(tfNode);
						} else if (tfNode.getType().startsWith("a")) {
							numActivators++;
							typedSpeciesNodes.add(tfNode);
						}
					if (numRepressors > 0 && numActivators == 0) {
						if (numRepressors == 1) {
							decomposeInverterMotif(node);
						} else if (numRepressors == 2)
							decomposeNorMotif(node);
					} else if (numRepressors == 0 && numActivators > 0) {
						if (numActivators == 1)
							decomposeYesMotif(node, interNodes);
						else if (numActivators == 2)
							decomposeOrMotif1(node, interNodes);
					} 
				} else
					node.setType("c");
			} else if (node.getType().endsWith("m")) {
				decomposeOrMotif2(node, interNodes);
				typedSpeciesNodes.add(node);
			} else if (node.getType().endsWith("x")) {
				decomposeAndMotif(node, interNodes);
				typedSpeciesNodes.add(node);
			} else if (node.getType().startsWith("v"))
				typedSpeciesNodes.add(node);
		for (SBOLSynthesisNode typedSpeciesNode : typedSpeciesNodes) {
			if (edges.containsKey(typedSpeciesNode)) {
				List<SBOLSynthesisNode> inputNodes = edges.remove(typedSpeciesNode);
				typedSpeciesNode.setType("s");
				edges.put(typedSpeciesNode, inputNodes);
			} else
				typedSpeciesNode.setType("s");
		}
		nodes.addAll(interNodes);
	}
	
	private void decomposeInverterMotif(SBOLSynthesisNode promoterNode) {
		List<SBOLSynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeNorMotif(SBOLSynthesisNode promoterNode) {
		List<SBOLSynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("n");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeYesMotif(SBOLSynthesisNode promoterNode, Set<SBOLSynthesisNode> nodes) {
		List<SBOLSynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SBOLSynthesisNode interSpeciesNode = new SBOLSynthesisNode("s");
		nodes.add(interSpeciesNode);
		SBOLSynthesisNode interPromoterNode = new SBOLSynthesisNode("i");
		nodes.add(interPromoterNode);
		edges.put(promoterNode, new LinkedList<SBOLSynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SBOLSynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif1(SBOLSynthesisNode promoterNode, Set<SBOLSynthesisNode> nodes) {
		List<SBOLSynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SBOLSynthesisNode interSpeciesNode = new SBOLSynthesisNode("s");
		nodes.add(interSpeciesNode);
		SBOLSynthesisNode interPromoterNode = new SBOLSynthesisNode("n");
		nodes.add(interPromoterNode);
		edges.put(promoterNode, new LinkedList<SBOLSynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SBOLSynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif2(SBOLSynthesisNode productNode, Set<SBOLSynthesisNode> nodes) {
		List<SBOLSynthesisNode> inputPromoterNodes = edges.get(productNode);
		List<SBOLSynthesisNode> interPromoterNodes = new LinkedList<SBOLSynthesisNode>();
		interPromoterNodes.add(new SBOLSynthesisNode("i"));
		interPromoterNodes.add(new SBOLSynthesisNode("n"));
		nodes.addAll(interPromoterNodes);
		List<SBOLSynthesisNode> interSpeciesNodes = new LinkedList<SBOLSynthesisNode>();
		for (int i = 0; i < 3; i++)
			interSpeciesNodes.add(new SBOLSynthesisNode("s"));
		nodes.addAll(interSpeciesNodes);
		edges.put(productNode, interPromoterNodes.subList(0, 1));
		edges.put(interPromoterNodes.get(0), interSpeciesNodes.subList(0, 1));
		edges.put(interSpeciesNodes.get(0), interPromoterNodes.subList(1, 2));
		edges.put(interPromoterNodes.get(1), interSpeciesNodes.subList(1, 3));
		for (int i = 1; i < interSpeciesNodes.size(); i++)
			edges.put(interSpeciesNodes.get(i), inputPromoterNodes.subList(i - 1, i));
	}
	
	private void decomposeAndMotif(SBOLSynthesisNode complexNode, Set<SBOLSynthesisNode> nodes) {
		List<SBOLSynthesisNode> inputSpeciesNodes = edges.get(complexNode);
		List<SBOLSynthesisNode> interPromoterNodes = new LinkedList<SBOLSynthesisNode>();
		interPromoterNodes.add(new SBOLSynthesisNode("n"));
		for (int i = 1; i < 3; i++)
			interPromoterNodes.add(i, new SBOLSynthesisNode("i"));
		nodes.addAll(interPromoterNodes);
		List<SBOLSynthesisNode> interSpeciesNodes = new LinkedList<SBOLSynthesisNode>();
		for (int i = 0; i < 2; i++)
			interSpeciesNodes.add(new SBOLSynthesisNode("s"));
		nodes.addAll(interSpeciesNodes);
		edges.put(complexNode, interPromoterNodes.subList(0, 1));
		edges.put(interPromoterNodes.get(0), interSpeciesNodes);
		for (int i = 0; i < interSpeciesNodes.size(); i++) 
			edges.put(interSpeciesNodes.get(i), interPromoterNodes.subList(i + 1, i + 2));
		for (int i = 1; i < interPromoterNodes.size(); i++) 
			edges.put(interPromoterNodes.get(i), inputSpeciesNodes.subList(i - 1, i));
	}

	private Set<SBOLSynthesisNode> constructGraph(Model sbmlModel, SBOLFileManager fileManager) {
		HashMap<String, SBOLSynthesisNode> idToNode = new HashMap<String, SBOLSynthesisNode>();
		edges = new HashMap<SBOLSynthesisNode, List<SBOLSynthesisNode>>();
		nucleotideCount = 0;
		signals = new HashSet<String>();
		for (long i = 0; i < sbmlModel.getNumReactions(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			if (sbmlReaction.getNumProducts() > 0) 
				if (BioModel.isProductionReaction(sbmlReaction)) {
					constructTranscriptionMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				} else if ((BioModel.isComplexReaction(sbmlReaction))) {
					constructComplexationMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				}
		}
		return new HashSet<SBOLSynthesisNode>(idToNode.values());
	}
	
	private void constructTranscriptionMotif(Reaction sbmlReaction, HashMap<String, SBOLSynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) {
		SBOLSynthesisNode promoterNode = null;
		for (long j = 0; j < sbmlReaction.getNumModifiers(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isPromoter(sbmlModifier))
				promoterNode = constructNode("p", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
						idToNode, fileManager);
		}
		if (promoterNode == null) 
			promoterNode = constructNode("p", sbmlReaction, idToNode, fileManager);
		for (long j = 0; j < sbmlReaction.getNumModifiers(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isRepressor(sbmlModifier) || BioModel.isActivator(sbmlModifier)) {
				SBOLSynthesisNode tfNode;
				if (BioModel.isRepressor(sbmlModifier))
					tfNode = constructNode("r", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							idToNode, fileManager);
				else
					tfNode = constructNode("a", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							idToNode, fileManager);
				if (!edges.containsKey(promoterNode))
					edges.put(promoterNode, new LinkedList<SBOLSynthesisNode>());
				edges.get(promoterNode).add(tfNode);	
			} 
		}
		for (long j = 0; j < sbmlReaction.getNumProducts(); j++) {
			SpeciesReference sbmlProduct = sbmlReaction.getProduct(j);
			SBOLSynthesisNode productNode = constructNode("s", 
					sbmlModel.getSpecies(sbmlProduct.getSpecies()), idToNode, fileManager);
			if (!edges.containsKey(productNode))
				edges.put(productNode, new LinkedList<SBOLSynthesisNode>());
			edges.get(productNode).add(promoterNode);	
		}
	}
	
	private void constructComplexationMotif(Reaction sbmlReaction, HashMap<String, SBOLSynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) {
		SpeciesReference sbmlProduct = sbmlReaction.getProduct(0);
		SBOLSynthesisNode complexNode = constructNode("x", 
				sbmlModel.getSpecies(sbmlProduct.getSpecies()), idToNode, fileManager);
		edges.put(complexNode, new LinkedList<SBOLSynthesisNode>());
		for (long j = 0; j < sbmlReaction.getNumReactants(); j++) {
			SpeciesReference sbmlReactant = sbmlReaction.getReactant(j);
			SBOLSynthesisNode speciesNode = constructNode("v", 
						sbmlModel.getSpecies(sbmlReactant.getSpecies()), idToNode, fileManager);
			edges.get(complexNode).add(speciesNode);	
		}
	}
	
	private SBOLSynthesisNode constructNode(String type, SBase sbmlElement,  
			HashMap<String, SBOLSynthesisNode> idToNode, SBOLFileManager fileManager) { 
		SBOLSynthesisNode node;
		if (idToNode.containsKey(sbmlElement.getId())) {
			node = idToNode.get(sbmlElement.getId());
			if (edges.containsKey(node)) {
				List<SBOLSynthesisNode> destinationNodes = edges.remove(node);
				if (type.equals("s")) 
					node.setType(node.getType() + "m");
				else
					node.setType(type + node.getType());
				edges.put(node, destinationNodes);
			} else if (!type.equals("s"))
				node.setType(node.getType() + type);
		} else {
			node = new SBOLSynthesisNode(type, sbmlElement, fileManager);
			nucleotideCount += node.getNucleotideCount();
			if (node.getSignal().length() > 0)
				signals.add(node.getSignal());
			idToNode.put(sbmlElement.getId(), node);
		}
		return node;
	}
	
//	private SBOLSynthesisNode constructPromoterNode(String promoterID, int numRepressors, int numActivators, 
//			Reaction sbmlReaction, SBOLFileManager fileManager) { 
////		if (numRepressors > 0 && numActivators == 0) {
////			if (numRepressors == 1)
////				promoterNode.setType("i");
////			else if (numRepressors == 2)
////				promoterNode.setType("n");
////		} else if (numRepressors == 0 && numActivators > 0) {
////			if (numActivators == 1)
////				promoterNode.setType("y");
////			else if (numActivators == 2)
////				promoterNode.setType("o");
////		} else
////			promoterNode.setType("c");
//		SBOLSynthesisNode promoterNode;
//		if (idToNode.containsKey(promoterID)) {
//			promoterNode = idToNode.get(promoterID);
//		} else {
//
//			promoterNode.setID(sbmlModifier.getSpecies());
//			promoterNode.setType("p");
//			promoterNode.processDNAComponents(sbmlReaction, fileManager);
//			nucleotideCount += promoterNode.getNucleotideCount();
//			if (promoterNode.getSignal().length() > 0)
//				signals.add(promoterNode.getSignal());
//			idToNode.put(sbmlModifier.getSpecies(), promoterNode);
//		}
//	}
	
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
			output = new SBOLSynthesisNode("t");
		return output;
	}
	
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
			buildPathsHelper(node, "t1", paths, true);
		} else 
			buildPathsHelper(node, "t1", paths, false);
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
