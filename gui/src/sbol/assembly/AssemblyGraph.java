package sbol.assembly;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.comp.CompConstant;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbolstandard.core.DnaComponent;

import sbol.util.SBOLFileManager;
import sbol.util.SBOLUtility;

import biomodel.annotation.AnnotationUtility;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;

public class AssemblyGraph {

	private Set<AssemblyNode> assemblyNodes;
	private HashMap<AssemblyNode, Set<AssemblyNode>> assemblyEdges;
	private HashMap<AssemblyNode, Set<AssemblyNode>> reverseAssemblyEdges;
	private Set<AssemblyNode> startNodes;
	private AssemblyGraph flatAssemblyGraph;
	private boolean containsSBOL = false;
	private boolean minusFlag = true;
	
	public AssemblyGraph(BioModel biomodel) {
		assemblyNodes = new HashSet<AssemblyNode>(); // Initialize map of SBML element meta IDs to assembly nodes they identify
		assemblyEdges = new HashMap<AssemblyNode, Set<AssemblyNode>>(); // Initialize map of assembly node IDs to sets of node IDs (node IDs are SBML meta IDs)
		SBMLDocument sbmlDoc = biomodel.getSBMLDocument();
		HashMap<String, AssemblyNode> idToNode = new HashMap<String, AssemblyNode>();
//		 Creates assembly nodes for submodels and connect them to nodes for species
		if (sbmlDoc.getExtensionPackages().containsKey(CompConstant.namespaceURI)
				&& parseSubModelSBOL(sbmlDoc, biomodel.getPath(), idToNode)) {
			// Creates flattened assembly graph in case hierarchy of SBOL can't be preserved
			SBMLDocument flatDoc = biomodel.flattenModel();
			flatAssemblyGraph = new AssemblyGraph(flatDoc);
		}
		constructGraph(sbmlDoc, idToNode);
	}
	
	private AssemblyGraph (SBMLDocument sbmlDoc) {
		assemblyNodes = new HashSet<AssemblyNode>(); // Initialize map of SBML element meta IDs to assembly nodes they identify
		assemblyEdges = new HashMap<AssemblyNode, Set<AssemblyNode>>(); // Initialize map of assembly node IDs to sets of node IDs (node IDs are SBML meta IDs)
		HashMap<String, AssemblyNode> idToNode = new HashMap<String, AssemblyNode>();
		constructGraph(sbmlDoc, idToNode);
	}

	private void constructGraph(SBMLDocument sbmlDoc, HashMap<String, AssemblyNode> idToNode) {
		Model sbmlModel = sbmlDoc.getModel();

		// Creates assembly nodes for species and maps their metaIDs to the nodes
		parseSpeciesSBOL(sbmlModel, idToNode);
		
		// Creates assembly nodes for global parameters and maps their metaIDs to the nodes
		parseParameterSBOL(sbmlModel, idToNode);

		// Creates assembly nodes for reactions and connects them to nodes for species
		// Maps reaction parameters to reactions
		parseReactionSbol(sbmlModel, idToNode);

		// Creates assembly nodes for rules and connects them to nodes for reactions and rules
		// on the basis of shared parameters
		parseRuleSbol(sbmlModel, idToNode);
		
		constructReverseEdges();
		selectStartNodes();
	}
	
	// Creates assembly nodes for submodels and connects them to the nodes for their input/output species
	private boolean parseSubModelSBOL(SBMLDocument sbmlDoc, String path, HashMap<String, AssemblyNode> idToNode) {
		CompModelPlugin compSBMLModel = SBMLutilities.getCompModelPlugin(sbmlDoc.getModel());
		CompSBMLDocumentPlugin compSBMLDoc = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
		if (compSBMLModel.getListOfSubmodels().size() > 0) {
			boolean canFlatten = true;
			for (int i = 0; i < compSBMLModel.getListOfSubmodels().size(); i++) {
				// Stores SBOL annotating the submodel instantiation if present
				// If not then stores SBOL annotating the model referenced by the submodel instantiation
				Submodel sbmlSubmodel = compSBMLModel.getListOfSubmodels().get(i);
				List<URI> sbolURIs = new LinkedList<URI>();
				String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlSubmodel, sbolURIs);
				if (sbolURIs.size() > 0) {
					containsSBOL = true;
					canFlatten = false;
				} else {
					String subSBMLFileID = compSBMLDoc.getListOfExternalModelDefinitions().get(sbmlSubmodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
					BioModel subBioModel = new BioModel(path);
					subBioModel.load(subSBMLFileID);
					Model subSBMLModel = subBioModel.getSBMLDocument().getModel();
					String subSBOLStrand = AnnotationUtility.parseSBOLAnnotation(subSBMLModel, sbolURIs);
					if (sbolStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND) && 
							subSBOLStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
						sbolStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
					else if (subSBOLStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
						sbolStrand = GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
					Iterator<URI> uriIterator = sbolURIs.iterator();
					while (canFlatten && uriIterator.hasNext()) {
						canFlatten = uriIterator.next().toString().endsWith("iBioSim");
					}
					if (sbolURIs.size() > 0)
						containsSBOL = true;
				}
				AssemblyNode assemblyNode = new AssemblyNode(sbolURIs, sbolStrand);
//				if (minusFlag){
//					assemblyNode.setStrand(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND);
//					minusFlag = false;
//				} else
//					minusFlag = true;
				assemblyNodes.add(assemblyNode);
				idToNode.put(sbmlSubmodel.getId(), assemblyNode);
				assemblyNode.setID(sbmlSubmodel.getId());
			}
			return canFlatten;
		} else
			return false;
	}

	// Creates assembly nodes for species and maps their metaIDs to the nodes
	private void parseSpeciesSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getSpeciesCount(); i++) {
			Species sbmlSpecies = sbmlModel.getListOfSpecies().get(i);
			List<URI> sbolURIs = new LinkedList<URI>(); 
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlSpecies, sbolURIs);
			AssemblyNode assemblyNode = new AssemblyNode(sbolURIs, sbolStrand);
			assemblyNodes.add(assemblyNode);
			idToNode.put(sbmlSpecies.getId(), assemblyNode);
			assemblyNode.setID(sbmlSpecies.getId());
			if (sbmlSpecies.getExtensionPackages().containsKey(CompConstant.namespaceURI)) {
				CompSBasePlugin compSBMLSpecies = SBMLutilities.getCompSBasePlugin(sbmlSpecies);
				Set<AssemblyNode> submodelNodes = new HashSet<AssemblyNode>();
				for (int j = 0; j < compSBMLSpecies.getListOfReplacedElements().size(); j++) {
					String submodelID = compSBMLSpecies.getListOfReplacedElements().get(j).getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				if (compSBMLSpecies.isSetReplacedBy()) {
					String submodelID = compSBMLSpecies.getReplacedBy().getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				for (AssemblyNode submodelNode : submodelNodes) {
					if (!assemblyEdges.containsKey(submodelNode))
						assemblyEdges.put(submodelNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(submodelNode).add(assemblyNode);
				}
				if (submodelNodes.size() > 0) {
					if (!assemblyEdges.containsKey(assemblyNode))
						assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(assemblyNode).addAll(submodelNodes);
				}
			}
			if (sbolURIs.size() > 0)
				containsSBOL = true;
		}
	}
	
	// Creates assembly nodes for global parameters and maps their metaIDs to the nodes
	private void parseParameterSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getParameterCount(); i++) {
			Parameter sbmlParameter = sbmlModel.getParameter(i);
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlParameter, sbolURIs);
			AssemblyNode assemblyNode = new AssemblyNode(sbolURIs, sbolStrand);
			assemblyNodes.add(assemblyNode);
			idToNode.put(sbmlParameter.getId(), assemblyNode);
			assemblyNode.setID(sbmlParameter.getId());
			if (sbmlParameter.getExtensionPackages().containsKey(CompConstant.namespaceURI)) {
				CompSBasePlugin compSBMLParameter = SBMLutilities.getCompSBasePlugin(sbmlParameter);
				Set<AssemblyNode> submodelNodes = new HashSet<AssemblyNode>();
				for (int j = 0; j < compSBMLParameter.getListOfReplacedElements().size(); j++) {
					String submodelID = compSBMLParameter.getListOfReplacedElements().get(j).getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				if (compSBMLParameter.isSetReplacedBy()) {
					String submodelID = compSBMLParameter.getReplacedBy().getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				for (AssemblyNode submodelNode : submodelNodes) {
					if (!assemblyEdges.containsKey(submodelNode))
						assemblyEdges.put(submodelNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(submodelNode).add(assemblyNode);
				}
				if (submodelNodes.size() > 0) {
					if (!assemblyEdges.containsKey(assemblyNode))
						assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(assemblyNode).addAll(submodelNodes);
				}
			}
			if (sbolURIs.size() > 0)
				containsSBOL = true;
		}
	}
	
	// Creates assembly nodes for reactions and connects them to nodes for species
	// Maps reaction parameters to reactions
	private void parseReactionSbol(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			// Creates assembly node for reaction
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlReaction, sbolURIs);
			AssemblyNode assemblyNode = new AssemblyNode(sbolURIs, sbolStrand);
			assemblyNodes.add(assemblyNode);
			idToNode.put(sbmlReaction.getId(), assemblyNode);
			assemblyNode.setID(sbmlReaction.getId());
			if (sbolURIs.size() > 0)
				containsSBOL = true;
		}
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			AssemblyNode assemblyNode = idToNode.get(sbmlReaction.getId());
			// Connects assembly nodes for reactants and modifiers to node for reaction
			for (int j = 0; j < sbmlReaction.getReactantCount(); j++) {
				String reactantID = sbmlReaction.getReactant(j).getSpecies();
				if (idToNode.containsKey(reactantID)) {
					AssemblyNode reactantNode = idToNode.get(reactantID);
					if (!assemblyEdges.containsKey(reactantNode))
						assemblyEdges.put(reactantNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(reactantNode).add(assemblyNode);
				}
			}
			for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
				String modifierID = sbmlReaction.getModifier(j).getSpecies();
				if (idToNode.containsKey(modifierID)) {
					AssemblyNode modifierNode = idToNode.get(modifierID);
					if (!assemblyEdges.containsKey(modifierNode))
						assemblyEdges.put(modifierNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(modifierNode).add(assemblyNode);
				}
			}
			// Connects assembly node for reaction to nodes for its products
			for (int j = 0; j < sbmlReaction.getProductCount(); j++) {
				 String productID = sbmlReaction.getProduct(j).getSpecies();
				if (idToNode.containsKey(productID)) {
					AssemblyNode productNode = idToNode.get(productID);
					if (!assemblyEdges.containsKey(assemblyNode))
						assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(assemblyNode).add(productNode);
				}
			}
			// Connects assembly nodes for parameters and reactions rates appearing in kinetic law of reaction
			// to assembly node for reaction
			KineticLaw kl = sbmlReaction.getKineticLaw();
			if (kl != null && kl.isSetMath()) {
				for (String input : parseInputHelper(kl.getMath())) {
					if (idToNode.containsKey(input)) {
						AssemblyNode inputNode = idToNode.get(input);
						if (!assemblyEdges.containsKey(inputNode))
							assemblyEdges.put(inputNode, new HashSet<AssemblyNode>());
						assemblyEdges.get(inputNode).add(assemblyNode);
					}
				}
			}
			if (sbmlReaction.getExtensionPackages().containsKey(CompConstant.namespaceURI)) {
				CompSBasePlugin compSBMLReaction = SBMLutilities.getCompSBasePlugin(sbmlReaction);
				Set<AssemblyNode> submodelNodes = new HashSet<AssemblyNode>();
				for (int j = 0; j < compSBMLReaction.getListOfReplacedElements().size(); j++) {
					String submodelID = compSBMLReaction.getListOfReplacedElements().get(j).getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				if (compSBMLReaction.isSetReplacedBy()) {
					String submodelID = compSBMLReaction.getReplacedBy().getSubmodelRef();
					if (idToNode.containsKey(submodelID)) 
						submodelNodes.add(idToNode.get(submodelID));
				}
				for (AssemblyNode submodelNode : submodelNodes) {
					if (!assemblyEdges.containsKey(submodelNode))
						assemblyEdges.put(submodelNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(submodelNode).add(assemblyNode);
				}
				if (submodelNodes.size() > 0) {
					if (!assemblyEdges.containsKey(assemblyNode))
						assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
					assemblyEdges.get(assemblyNode).addAll(submodelNodes);
				}
			}
		}
	}
	
	// Creates assembly nodes for rules and connects them to nodes for reactions and rules
	// on the basis of shared parameters
	private void parseRuleSbol(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getRuleCount(); i++) {
			Rule sbmlRule = sbmlModel.getRule(i);
			// Creates assembly node for rule
			if (sbmlRule.isAssignment() || sbmlRule.isRate()) {
				List<URI> sbolURIs = new LinkedList<URI>();
				String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlRule, sbolURIs);
				AssemblyNode assemblyNode = new AssemblyNode(sbolURIs, sbolStrand);
				assemblyNodes.add(assemblyNode);
				// Connects assembly nodes for input species and parameters to node for rule
				for (String input : parseInputHelper(sbmlRule.getMath())) {
					if (idToNode.containsKey(input)) {
						AssemblyNode inputNode = idToNode.get(input);
						if (!assemblyEdges.containsKey(inputNode))
							assemblyEdges.put(inputNode, new HashSet<AssemblyNode>());
						assemblyEdges.get(inputNode).add(assemblyNode);
					} 
				}
				// Connects assembly node for rule to node for its output species or parameter
				String output = SBMLutilities.getVariable(sbmlRule);
				if (output != null) {
					if (idToNode.containsKey(output)) {
						AssemblyNode outputNode = idToNode.get(output);
						if (!assemblyEdges.containsKey(assemblyNode))
							assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
						assemblyEdges.get(assemblyNode).add(outputNode);
					} 
				}
				if (sbmlRule.getExtensionPackages().containsKey(CompConstant.namespaceURI)) {
					CompSBasePlugin compSBMLRule = SBMLutilities.getCompSBasePlugin(sbmlRule);
					Set<AssemblyNode> submodelNodes = new HashSet<AssemblyNode>();
					for (int j = 0; j < compSBMLRule.getListOfReplacedElements().size(); j++) {
						String submodelID = compSBMLRule.getListOfReplacedElements().get(j).getSubmodelRef();
						if (idToNode.containsKey(submodelID)) 
							submodelNodes.add(idToNode.get(submodelID));
					}
					if (compSBMLRule.isSetReplacedBy()) {
						String submodelID = compSBMLRule.getReplacedBy().getSubmodelRef();
						if (idToNode.containsKey(submodelID)) 
							submodelNodes.add(idToNode.get(submodelID));
					}
					for (AssemblyNode submodelNode : submodelNodes) {
						if (!assemblyEdges.containsKey(submodelNode))
							assemblyEdges.put(submodelNode, new HashSet<AssemblyNode>());
						assemblyEdges.get(submodelNode).add(assemblyNode);
					}
					if (submodelNodes.size() > 0) {
						if (!assemblyEdges.containsKey(assemblyNode))
							assemblyEdges.put(assemblyNode, new HashSet<AssemblyNode>());
						assemblyEdges.get(assemblyNode).addAll(submodelNodes);
					}
				}
				if (sbolURIs.size() > 0)
					containsSBOL = true;
			}
		}
	}
	
	private LinkedList<String> parseInputHelper(ASTNode astNode) {
		LinkedList<String> inputs = new LinkedList<String>();
		if (!astNode.isOperator() && !astNode.isNumber())
			inputs.add(astNode.getName());
		for (int i = 0; i < astNode.getChildCount(); i++) {
			inputs.addAll(parseInputHelper(astNode.getListOfNodes().get(i)));
		}
		return inputs;
	}
	
	public boolean loadDNAComponents(SBOLFileManager fileManager) {
		boolean error = false;
		for (AssemblyNode assemblyNode : assemblyNodes) {
			List<DnaComponent> dnaComps = fileManager.resolveURIs(assemblyNode.getURIs());
			assemblyNode.setDNAComponents(dnaComps);
			if (!error)
				error = (dnaComps.size() == 0 && assemblyNode.getURIs().size() > 0);
		}
		return !error && (flatAssemblyGraph == null || flatAssemblyGraph.loadDNAComponents(fileManager));
	}
	
	private void constructReverseEdges() {
		reverseAssemblyEdges = new HashMap<AssemblyNode, Set<AssemblyNode>>();
		for (AssemblyNode originNode : assemblyEdges.keySet())
			for (AssemblyNode destinationNode : assemblyEdges.get(originNode)) {
				if (!reverseAssemblyEdges.containsKey(destinationNode))
					reverseAssemblyEdges.put(destinationNode, new HashSet<AssemblyNode>());
				reverseAssemblyEdges.get(destinationNode).add(originNode);
			}
	}
	
	private void selectStartNodes() {
		startNodes = new HashSet<AssemblyNode>();
		startNodes.addAll(assemblyNodes);
		for (Set<AssemblyNode> nextNodes : assemblyEdges.values())
			startNodes.removeAll(nextNodes);
	}
	
	public boolean containsSBOL() {
		return containsSBOL || (flatAssemblyGraph != null && flatAssemblyGraph.containsSBOL());
	}
	
	public Set<AssemblyNode> getNodes() {
		return assemblyNodes;
	}
	
	public HashMap<AssemblyNode, Set<AssemblyNode>> getEdges() {
		return assemblyEdges;
	}
	
	public Set<AssemblyNode> getNextNodes(AssemblyNode assemblyNode) {
		if (assemblyEdges.containsKey(assemblyNode))
			return assemblyEdges.get(assemblyNode);
		else
			return new HashSet<AssemblyNode>();
	}
	
	public Set<AssemblyNode> getPreviousNodes(AssemblyNode assemblyNode) {
		if (reverseAssemblyEdges.containsKey(assemblyNode))
			return reverseAssemblyEdges.get(assemblyNode);
		else
			return new HashSet<AssemblyNode>();
	}
	
	public AssemblyGraph getFlatGraph() {
		return flatAssemblyGraph;
	}
	
	public Set<AssemblyNode> getStartNodes() {
		return startNodes;
	}
	
	public int size() {
		return assemblyNodes.size();
	}
	
	public void print() {
		HashMap<AssemblyNode, String> nodeToState = new HashMap<AssemblyNode, String>();
		int stateIndex = 0;
		for (AssemblyNode assemblyNode : assemblyNodes) {
			nodeToState.put(assemblyNode, "S" + stateIndex);
			stateIndex++;
		}
		System.out.println("digraph G {");
		for (AssemblyNode assemblyNode : assemblyNodes) {
			String compURIs = "";
			for (URI uri : SBOLUtility.loadDNAComponentURIs(assemblyNode.getDNAComponents()))
				compURIs = compURIs + ", " + uri.toString();
			String state = nodeToState.get(assemblyNode);
			System.out.println(state + " [label=\"" + assemblyNode.getID() + compURIs + "\"]");
			if (assemblyEdges.containsKey(assemblyNode))
				for (AssemblyNode nextNode : assemblyEdges.get(assemblyNode))
					System.out.println(state + " -> " + nodeToState.get(nextNode));
		}
		System.out.println("}");
	}
	
//	public void print(List<AssemblyNode> orderedNodes) {
//		idNodes();
//		HashMap<AssemblyNode, String> nodeToState = new HashMap<AssemblyNode, String>();
//		int stateIndex = 0;
//		for (AssemblyNode assemblyNode : assemblyNodes) {
//			nodeToState.put(assemblyNode, "S" + stateIndex);
//			stateIndex++;
//		}
//		System.out.println("digraph G {");
//		for (AssemblyNode assemblyNode : assemblyNodes) {
//			String soTypes = "";
//			for (DnaComponent dnaComp : assemblyNode.getDNAComponents())
//				for (String soType : SBOLUtility.loadDNAComponentTypes(dnaComp))
//					soTypes = soTypes + " " + soType;
//			String state = nodeToState.get(assemblyNode);
//			int order = 0;
//			while (order < orderedNodes.size() && !orderedNodes.get(order).equals(assemblyNode))
//				order++;
//			if (order == orderedNodes.size())
//				System.out.println(state + " [label=\" ! " + soTypes + "\"]");
//			else
//				System.out.println(state + " [label=\" " + order + " " + soTypes + "\"]");
//			if (assemblyEdges.containsKey(assemblyNode))
//				for (AssemblyNode nextNode : assemblyEdges.get(assemblyNode))
//					System.out.println(state + " -> " + nodeToState.get(nextNode));
//		}
//		System.out.println("}");
//	}
	
	private void idNodes() {
		String id = "node";
		int i = 0;
		for (AssemblyNode assemblyNode : assemblyNodes) {
			assemblyNode.setID(id + "_" + i);
			i++;
		}
	}
}
