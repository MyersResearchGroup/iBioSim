package synthesis.genetic;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SpeciesReference;

import sbol.util.SBOLFileManager;

import biomodel.parser.BioModel;
import biomodel.util.SBMLutilities;

public class SynthesisGraph {

	private String projectPath;
	private String modelFileID;
	private String submodelID;
	private HashMap<SynthesisNode, List<SynthesisNode>> edges;
	private int nucleotideCount;
	private Set<String> signals;
	private Set<URI> compURIs;
	private SynthesisNode output;
	private List<SynthesisNode> inputs;
	private List<String> paths;
	
	public SynthesisGraph(BioModel biomodel, SBOLFileManager fileManager) {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		projectPath = biomodel.getPath();
		modelFileID = biomodel.getSBMLFile();
		Set<SynthesisNode> nodes = constructGraph(sbmlModel, fileManager);
		decomposeGraph(nodes);
		output = identifyOutput(nodes);
		paths = buildPaths(getOutput());
//		print();
	}
	
	private void decomposeGraph(Set<SynthesisNode> nodes) {
		Set<SynthesisNode> typedSpeciesNodes = new HashSet<SynthesisNode>();
		Set<SynthesisNode> interNodes = new HashSet<SynthesisNode>();
		for (SynthesisNode node : nodes)
			if (node.getType().equals("p")) {
				if (edges.containsKey(node)) {
					int numActivators = 0;
					int numRepressors = 0;
					for (SynthesisNode tfNode : edges.get(node))
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
		for (SynthesisNode typedSpeciesNode : typedSpeciesNodes) {
			if (edges.containsKey(typedSpeciesNode)) {
				List<SynthesisNode> inputNodes = edges.remove(typedSpeciesNode);
				typedSpeciesNode.setType("s");
				edges.put(typedSpeciesNode, inputNodes);
			} else
				typedSpeciesNode.setType("s");
		}
		nodes.addAll(interNodes);
	}
	
	private void decomposeInverterMotif(SynthesisNode promoterNode) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeNorMotif(SynthesisNode promoterNode) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("n");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeYesMotif(SynthesisNode promoterNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SynthesisNode interSpeciesNode = new SynthesisNode("s");
		nodes.add(interSpeciesNode);
		SynthesisNode interPromoterNode = new SynthesisNode("i");
		nodes.add(interPromoterNode);
		edges.put(promoterNode, new LinkedList<SynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif1(SynthesisNode promoterNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SynthesisNode interSpeciesNode = new SynthesisNode("s");
		nodes.add(interSpeciesNode);
		SynthesisNode interPromoterNode = new SynthesisNode("n");
		nodes.add(interPromoterNode);
		edges.put(promoterNode, new LinkedList<SynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif2(SynthesisNode productNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputPromoterNodes = edges.get(productNode);
		List<SynthesisNode> interPromoterNodes = new LinkedList<SynthesisNode>();
		interPromoterNodes.add(new SynthesisNode("i"));
		interPromoterNodes.add(new SynthesisNode("n"));
		nodes.addAll(interPromoterNodes);
		List<SynthesisNode> interSpeciesNodes = new LinkedList<SynthesisNode>();
		for (int i = 0; i < 3; i++)
			interSpeciesNodes.add(new SynthesisNode("s"));
		nodes.addAll(interSpeciesNodes);
		edges.put(productNode, interPromoterNodes.subList(0, 1));
		edges.put(interPromoterNodes.get(0), interSpeciesNodes.subList(0, 1));
		edges.put(interSpeciesNodes.get(0), interPromoterNodes.subList(1, 2));
		edges.put(interPromoterNodes.get(1), interSpeciesNodes.subList(1, 3));
		for (int i = 1; i < interSpeciesNodes.size(); i++)
			edges.put(interSpeciesNodes.get(i), inputPromoterNodes.subList(i - 1, i));
	}
	
	private void decomposeAndMotif(SynthesisNode complexNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputSpeciesNodes = edges.get(complexNode);
		List<SynthesisNode> interPromoterNodes = new LinkedList<SynthesisNode>();
		interPromoterNodes.add(new SynthesisNode("n"));
		for (int i = 1; i < 3; i++)
			interPromoterNodes.add(i, new SynthesisNode("i"));
		nodes.addAll(interPromoterNodes);
		List<SynthesisNode> interSpeciesNodes = new LinkedList<SynthesisNode>();
		for (int i = 0; i < 2; i++)
			interSpeciesNodes.add(new SynthesisNode("s"));
		nodes.addAll(interSpeciesNodes);
		edges.put(complexNode, interPromoterNodes.subList(0, 1));
		edges.put(interPromoterNodes.get(0), interSpeciesNodes);
		for (int i = 0; i < interSpeciesNodes.size(); i++) 
			edges.put(interSpeciesNodes.get(i), interPromoterNodes.subList(i + 1, i + 2));
		for (int i = 1; i < interPromoterNodes.size(); i++) 
			edges.put(interPromoterNodes.get(i), inputSpeciesNodes.subList(i - 1, i));
	}

	private Set<SynthesisNode> constructGraph(Model sbmlModel, SBOLFileManager fileManager) {
		HashMap<String, SynthesisNode> idToNode = new HashMap<String, SynthesisNode>();
		edges = new HashMap<SynthesisNode, List<SynthesisNode>>();
		nucleotideCount = 0;
		compURIs = new HashSet<URI>();
		signals = new HashSet<String>();
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			if (sbmlReaction.getProductCount() > 0) 
				if (BioModel.isProductionReaction(sbmlReaction)) {
					constructTranscriptionMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				} else if ((BioModel.isComplexReaction(sbmlReaction))) {
					constructComplexationMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				}
		}
		return new HashSet<SynthesisNode>(idToNode.values());
	}
	
	private void constructTranscriptionMotif(Reaction sbmlReaction, HashMap<String, SynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) {
		SynthesisNode promoterNode = null;
		for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isPromoter(sbmlModifier))
				promoterNode = constructNode("p", sbmlReaction, sbmlModifier.getSpecies(),
						idToNode, fileManager);
		}
		if (promoterNode == null) 
			promoterNode = constructNode("p", sbmlReaction, sbmlReaction.getId(), idToNode, fileManager);
		for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isRepressor(sbmlModifier) || BioModel.isActivator(sbmlModifier)) {
				SynthesisNode tfNode;
				if (BioModel.isRepressor(sbmlModifier))
					tfNode = constructNode("r", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							sbmlModifier.getSpecies(), idToNode, fileManager);
				else
					tfNode = constructNode("a", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							sbmlModifier.getSpecies(), idToNode, fileManager);
				if (!edges.containsKey(promoterNode))
					edges.put(promoterNode, new LinkedList<SynthesisNode>());
				edges.get(promoterNode).add(tfNode);	
			} 
		}
		for (int j = 0; j < sbmlReaction.getProductCount(); j++) {
			SpeciesReference sbmlProduct = sbmlReaction.getProduct(j);
			SynthesisNode productNode = constructNode("s", sbmlModel.getSpecies(sbmlProduct.getSpecies()), 
					sbmlProduct.getSpecies(), idToNode, fileManager);
			if (!edges.containsKey(productNode))
				edges.put(productNode, new LinkedList<SynthesisNode>());
			edges.get(productNode).add(promoterNode);	
		}
	}
	
	private void constructComplexationMotif(Reaction sbmlReaction, HashMap<String, SynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) {
		SpeciesReference sbmlProduct = sbmlReaction.getProduct(0);
		SynthesisNode complexNode = constructNode("x", sbmlModel.getSpecies(sbmlProduct.getSpecies()), 
				sbmlProduct.getSpecies(), idToNode, fileManager);
		edges.put(complexNode, new LinkedList<SynthesisNode>());
		for (int j = 0; j < sbmlReaction.getReactantCount(); j++) {
			SpeciesReference sbmlReactant = sbmlReaction.getReactant(j);
			SynthesisNode speciesNode = constructNode("v", sbmlModel.getSpecies(sbmlReactant.getSpecies()),
					sbmlReactant.getSpecies(), idToNode, fileManager);
			edges.get(complexNode).add(speciesNode);	
		}
	}
	
	private SynthesisNode constructNode(String type, SBase sbmlElement, String id, 
			HashMap<String, SynthesisNode> idToNode, SBOLFileManager fileManager) { 
		SynthesisNode node;
		if (idToNode.containsKey(SBMLutilities.getId(sbmlElement))) {
			node = idToNode.get(SBMLutilities.getId(sbmlElement));
			if (edges.containsKey(node)) {
				List<SynthesisNode> destinationNodes = edges.remove(node);
				if (type.equals("s")) 
					node.setType(node.getType() + "m");
				else
					node.setType(type + node.getType());
				edges.put(node, destinationNodes);
			} else if (!type.equals("s"))
				node.setType(node.getType() + type);
		} else {
			node = new SynthesisNode(type, sbmlElement, fileManager);
			nucleotideCount += node.getNucleotideCount();
			if (node.getSignal().length() > 0)
				signals.add(node.getSignal());
			compURIs.addAll(node.getCompURIs());
			idToNode.put(SBMLutilities.getId(sbmlElement), node);
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
	
	private SynthesisNode identifyOutput(Set<SynthesisNode> nodes) {
		HashMap<SynthesisNode, SynthesisNode> reverseEdges = new HashMap<SynthesisNode, SynthesisNode>();
		for (SynthesisNode node : nodes) {
			if (edges.containsKey(node))
				for (SynthesisNode childNode : edges.get(node))
					reverseEdges.put(childNode, node);
		}
		for (SynthesisNode node : nodes) {
			if (!reverseEdges.containsKey(node))
				return node;
		}
		return null;
	}
	
	public String getProjectPath() {
		return projectPath;
	}
	
	public void setModelFileID(String modelFileID) {
		this.modelFileID = modelFileID;
	}
	
	public String getModelFileID() {
		return modelFileID;
	}
	
	public void setSubmodelID(String submodelID) {
		this.submodelID = submodelID;
	}
	
	public String getSubmodelID() {
		if (submodelID == null)
			submodelID = "";
		return submodelID;
	}
	
	public SynthesisNode getOutput() {
		if (output == null)
			output = new SynthesisNode("t");
		return output;
	}
	
	public List<SynthesisNode> getInputs() {
		return inputs;
	}
	
	public SynthesisNode getInput(int inputIndex) {
		if (inputIndex < inputs.size())
			return inputs.get(inputIndex);
		else
			return null;
	}
	
	public List<SynthesisNode> postOrderNodes() {
		List<SynthesisNode> orderedNodes = new LinkedList<SynthesisNode>();
		List<SynthesisNode> currentNodes = new LinkedList<SynthesisNode>();
		currentNodes.add(getOutput());
		while (currentNodes.size() > 0) {
			orderedNodes.add(0, currentNodes.remove(0));
			if (edges.containsKey(orderedNodes.get(0))) {
				currentNodes.addAll(edges.get(orderedNodes.get(0)));
			}
		}
		return orderedNodes;
	}

	public List<SynthesisNode> getChildren(SynthesisNode node) {
		if (edges.containsKey(node))
			return edges.get(node);
		else
			return new LinkedList<SynthesisNode>();
	}
	
	public int getNucleotideCount() {
		return nucleotideCount;
	}
	
	public Set<String> getSignals() {
		return signals;
	}
	
	public Set<URI> getCompURIs() {
		return compURIs;
	}

	private void print() {
		System.out.println(modelFileID);
		for (String path : paths)
			System.out.println(path);
		System.out.println();
	}
	
	public List<String> getPaths(SynthesisNode node) {
		return buildPaths(node);
	}
	
	public List<String> getPaths() {
		if (paths == null)
			paths = buildPaths(getOutput());
		return paths;
	}
	
	private List<String> buildPaths(SynthesisNode node) {
		List<String> paths = new LinkedList<String>();
		if (node.equals(getOutput())) {
			inputs = new LinkedList<SynthesisNode>();
			buildPathsHelper(node, "t1", paths, true);
		} else 
			buildPathsHelper(node, "t1", paths, false);
		return paths;
	}
	
	private void buildPathsHelper(SynthesisNode node, String path, List<String> paths, boolean identifyInputs) {
		path = path + node.getType();
		if (edges.containsKey(node)) {
			int childNum = 0;
			for (SynthesisNode childNode : edges.get(node)) {
				childNum++;
				buildPathsHelper(childNode, path + childNum, paths, identifyInputs);
			}
		} else {
			paths.add(path);
			if (identifyInputs)
				inputs.add(node);
		}
	}
	
	public List<SynthesisNode> walkPaths(SynthesisNode startNode, List<String> paths) {
		List<SynthesisNode> destinationNodes = new LinkedList<SynthesisNode>();
		for (String path : paths) {
			path = path.substring(2);
			SynthesisNode currentNode = startNode;
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
	
	public String toString() {
		return modelFileID;
	}

}
