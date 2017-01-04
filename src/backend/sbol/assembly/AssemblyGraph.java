package backend.sbol.assembly;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AbstractMathContainer;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbolstandard.core.DnaComponent;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.parser.BioModel;
import backend.biomodel.util.GlobalConstants;
import backend.biomodel.util.SBMLutilities;
import backend.sbol.util.SBOLFileManager;
import backend.sbol.util.SBOLUtility;

public class AssemblyGraph {

	private Set<AssemblyNode> assemblyNodes;
	private HashMap<AssemblyNode, Set<AssemblyNode>> assemblyEdges;
	private HashMap<AssemblyNode, Set<AssemblyNode>> reverseAssemblyEdges;
	private Set<AssemblyNode> startNodes;
	private AssemblyGraph flatAssemblyGraph;
	private boolean containsSBOL = false;
	//private boolean minusFlag = true;
	
	public AssemblyGraph(BioModel biomodel) {
		assemblyNodes = new HashSet<AssemblyNode>(); // Initialize map of SBML element meta IDs to assembly nodes they identify
		assemblyEdges = new HashMap<AssemblyNode, Set<AssemblyNode>>(); // Initialize map of assembly node IDs to sets of node IDs (node IDs are SBML meta IDs)
		SBMLDocument sbmlDoc = biomodel.getSBMLDocument();
		HashMap<String, AssemblyNode> idToNode = new HashMap<String, AssemblyNode>();
//		 Creates assembly nodes for submodels and connect them to nodes for species
		if (/*sbmlDoc.getExtensionPackages().containsKey(CompConstants.namespaceURI)
				&& */parseSubModelSBOL(sbmlDoc, biomodel.getPath(), idToNode)) {
			// Creates flattened assembly graph in case hierarchy of SBOL can't be preserved
			SBMLDocument flatDoc;
			try {
				flatDoc = biomodel.flattenModel(true);
			} catch (Exception e){
				e.printStackTrace();
				flatDoc = null;
			}
			// TODO: Hack to prevent null returned by flattenModel to crash assembly code
			if (flatDoc==null) {
				flatDoc = sbmlDoc;
			}
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
		parseReactionSBOL(sbmlModel, idToNode);

		// Creates assembly nodes for rules and connects them to nodes for reactions and rules
		// on the basis of shared parameters
		parseRuleSBOL(sbmlModel, idToNode);
		
		parseEventSBOL(sbmlModel, idToNode);
		
		constructReverseEdges();
		selectStartNodes();
	}
	
	private AssemblyNode constructNode(SBase sbmlElement, String sbmlID) {
		AssemblyNode node = constructNode(sbmlElement);
		node.setID(sbmlID);
		return node;
	}
	
	private AssemblyNode constructNode(SBase sbmlElement) {
		List<URI> sbolURIs = new LinkedList<URI>(); 
		String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlElement, sbolURIs);
		AssemblyNode node = new AssemblyNode(sbolURIs, sbolStrand);
		assemblyNodes.add(node);
		return node;
	}
	
	private void constructEdge(AssemblyNode sourceNode, AssemblyNode sinkNode) {
		if (!assemblyEdges.containsKey(sourceNode))
			assemblyEdges.put(sourceNode, new HashSet<AssemblyNode>());
		assemblyEdges.get(sourceNode).add(sinkNode);
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
				Submodel sbmlSubModel = compSBMLModel.getListOfSubmodels().get(i);
				AssemblyNode subModelNode = constructNode(sbmlSubModel, sbmlSubModel.getId());
				if (subModelNode.getURIs().size() > 0) {
					containsSBOL = true;
					canFlatten = false;
					idToNode.put(sbmlSubModel.getId(), subModelNode);
				} else {
					assemblyNodes.remove(subModelNode);
					String extSBMLFileID = compSBMLDoc.getListOfExternalModelDefinitions().get(sbmlSubModel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
					BioModel extBioModel = new BioModel(path);
					extBioModel.load(extSBMLFileID);
					Model extSBMLModel = extBioModel.getSBMLDocument().getModel();
					AssemblyNode extModelNode = constructNode(extSBMLModel, extSBMLModel.getId());
					if (subModelNode.getStrand().equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
						if (extModelNode.getStrand().equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
							extModelNode.setStrand(GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
						else
							extModelNode.setStrand(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND);
					if (extModelNode.getURIs().size() > 0)
						containsSBOL = true;
					Iterator<URI> uriIterator = extModelNode.getURIs().iterator();
					while (canFlatten && uriIterator.hasNext()) {
						canFlatten = uriIterator.next().toString().endsWith("iBioSim");
					}
					idToNode.put(sbmlSubModel.getId(), extModelNode);
				}
			}
			return canFlatten;
		}
		return false;
	}

	// Creates assembly nodes for species and maps their metaIDs to the nodes
	private void parseSpeciesSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getSpeciesCount(); i++) {
			Species sbmlSpecies = sbmlModel.getListOfSpecies().get(i);
			AssemblyNode speciesNode = constructNode(sbmlSpecies, sbmlSpecies.getId());
			if (speciesNode.getURIs().size() > 0)
				containsSBOL = true;
			idToNode.put(sbmlSpecies.getId(), speciesNode);
			if (sbmlSpecies.getExtensionPackages().containsKey(CompConstants.namespaceURI))
				parsePortMappings(sbmlSpecies, speciesNode, idToNode);
			
		}
	}
	
	// Creates assembly nodes for global parameters and maps their metaIDs to the nodes
	private void parseParameterSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getParameterCount(); i++) {
			Parameter sbmlParameter = sbmlModel.getParameter(i);
			AssemblyNode parameterNode = constructNode(sbmlParameter, sbmlParameter.getId());
			if (parameterNode.getURIs().size() > 0)
				containsSBOL = true;
			idToNode.put(sbmlParameter.getId(), parameterNode);
			if (sbmlParameter.getExtensionPackages().containsKey(CompConstants.namespaceURI))
				parsePortMappings(sbmlParameter, parameterNode, idToNode);
		}
	}
	
	// Creates assembly nodes for reactions and connects them to nodes for species
	// Maps reaction parameters to reactions
	private void parseReactionSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			// Creates assembly node for reaction
			AssemblyNode reactionNode = constructNode(sbmlReaction, sbmlReaction.getId());
			if (reactionNode.getURIs().size() > 0)
				containsSBOL = true;
			idToNode.put(sbmlReaction.getId(), reactionNode);
		}
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			if (!sbmlReaction.isSetKineticLaw()) continue;
			AssemblyNode reactionNode = idToNode.get(sbmlReaction.getId());
			// Connects assembly nodes for reactants and modifiers to node for reaction
			for (int j = 0; j < sbmlReaction.getReactantCount(); j++) {
				String reactantID = sbmlReaction.getReactant(j).getSpecies();
				if (idToNode.containsKey(reactantID)) {
					AssemblyNode reactantNode = idToNode.get(reactantID);
					constructEdge(reactantNode, reactionNode);
				}
			}
			for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
				String modifierID = sbmlReaction.getModifier(j).getSpecies();
				if (idToNode.containsKey(modifierID)) {
					AssemblyNode modifierNode = idToNode.get(modifierID);
					constructEdge(modifierNode, reactionNode);
				}
			}
			// Connects assembly node for reaction to nodes for its products
			for (int j = 0; j < sbmlReaction.getProductCount(); j++) {
				 String productID = sbmlReaction.getProduct(j).getSpecies();
				if (idToNode.containsKey(productID)) {
					AssemblyNode productNode = idToNode.get(productID);
					constructEdge(reactionNode, productNode);
				}
			}
			// Connects assembly nodes for parameters and reaction rates appearing in kinetic law of reaction
			// to assembly node for reaction
			parseMath(sbmlReaction.getKineticLaw(), reactionNode, idToNode);
			if (sbmlReaction.getExtensionPackages().containsKey(CompConstants.namespaceURI))
				parsePortMappings(sbmlReaction, reactionNode, idToNode);
		}
	}
	
	// Creates assembly nodes for rules and connects them to nodes for input/output species,
	// reaction rates, and parameters
	private void parseRuleSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getRuleCount(); i++) {
			Rule sbmlRule = sbmlModel.getRule(i);
			// Creates assembly node for rule
			if (sbmlRule.isAssignment() || sbmlRule.isRate()) {
				AssemblyNode ruleNode = constructNode(sbmlRule);
				if (ruleNode.getURIs().size() > 0)
					containsSBOL = true;
				// Connects assembly nodes for input species, reaction rates, and parameters to node for rule
				parseMath(sbmlRule, ruleNode, idToNode);
				// Connects assembly node for rule to node for its output species or parameter
				String output = SBMLutilities.getVariable(sbmlRule);
				if (output != null && idToNode.containsKey(output)) {
					AssemblyNode outputNode = idToNode.get(output);
					constructEdge(ruleNode, outputNode);
				}
				if (sbmlRule.getExtensionPackages().containsKey(CompConstants.namespaceURI))
					parsePortMappings(sbmlRule, ruleNode, idToNode);
			}
		}
	}
	
	// Creates assembly nodes for events and connects them to nodes for input/output species,
	// reaction rates, and parameters
	private void parseEventSBOL(Model sbmlModel, HashMap<String, AssemblyNode> idToNode) {
		for (int i = 0; i < sbmlModel.getEventCount(); i++) {
			Event sbmlEvent = sbmlModel.getListOfEvents().get(i);
			AssemblyNode eventNode = constructNode(sbmlEvent, sbmlEvent.getId());
			if (eventNode.getURIs().size() > 0)
				containsSBOL = true;
			idToNode.put(sbmlEvent.getId(), eventNode);
			if (sbmlEvent.getTrigger() != null)
				parseMath(sbmlEvent.getTrigger(), eventNode, idToNode);
			if (sbmlEvent.getDelay() != null)
				parseMath(sbmlEvent.getDelay(), eventNode, idToNode);
			if (sbmlEvent.getPriority() != null)
				parseMath(sbmlEvent.getPriority(), eventNode, idToNode);
			for (int j = 0; j < sbmlEvent.getEventAssignmentCount(); j++) {
				parseMath(sbmlEvent.getEventAssignment(j), eventNode, idToNode);
				String output = sbmlEvent.getEventAssignment(j).getVariable();
				if (output != null && idToNode.containsKey(output)) {
					AssemblyNode outputNode = idToNode.get(output);
					constructEdge(eventNode, outputNode);
				}
			}
			if (sbmlEvent.getExtensionPackages().containsKey(CompConstants.namespaceURI))
				parsePortMappings(sbmlEvent, eventNode, idToNode);
		}
	}

	private void parsePortMappings(SBase sbmlElement, AssemblyNode node, 
			HashMap<String, AssemblyNode> idToNode) {
		CompSBasePlugin compSBMLElement = SBMLutilities.getCompSBasePlugin(sbmlElement);
		Set<AssemblyNode> submodelNodes = new HashSet<AssemblyNode>();
		for (int j = 0; j < compSBMLElement.getListOfReplacedElements().size(); j++) {
			String submodelID = compSBMLElement.getListOfReplacedElements().get(j).getSubmodelRef();
			if (idToNode.containsKey(submodelID)) 
				submodelNodes.add(idToNode.get(submodelID));
		}
		if (compSBMLElement.isSetReplacedBy()) {
			String submodelID = compSBMLElement.getReplacedBy().getSubmodelRef();
			if (idToNode.containsKey(submodelID)) 
				submodelNodes.add(idToNode.get(submodelID));
		}
		for (AssemblyNode submodelNode : submodelNodes) {
			constructEdge(submodelNode, node);
			constructEdge(node, submodelNode);
		}
	}
	
	private void parseMath(AbstractMathContainer sbmlMathContainer, AssemblyNode sinkNode,
			HashMap<String, AssemblyNode> idToNode) {
		if (sbmlMathContainer.isSetMath())
			for (String input : parseMathHelper(sbmlMathContainer.getMath()))
				if (idToNode.containsKey(input)) {
					AssemblyNode inputNode = idToNode.get(input);
					constructEdge(inputNode, sinkNode);
				}
	}
	
	private LinkedList<String> parseMathHelper(ASTNode astNode) {
		LinkedList<String> inputs = new LinkedList<String>();
		if (!astNode.isOperator() && !astNode.isNumber())
			inputs.add(astNode.getName());
		for (int i = 0; i < astNode.getChildCount(); i++) {
			inputs.addAll(parseMathHelper(astNode.getListOfNodes().get(i)));
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
		return new HashSet<AssemblyNode>();
	}
	
	public Set<AssemblyNode> getPreviousNodes(AssemblyNode assemblyNode) {
		if (reverseAssemblyEdges.containsKey(assemblyNode))
			return reverseAssemblyEdges.get(assemblyNode);
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
	
	@SuppressWarnings("unused")
	private void idNodes() {
		String id = "node";
		int i = 0;
		for (AssemblyNode assemblyNode : assemblyNodes) {
			assemblyNode.setID(id + "_" + i);
			i++;
		}
	}
}
