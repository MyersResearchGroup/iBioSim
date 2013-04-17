package sbol;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;

import biomodel.util.GlobalConstants;

public class SBOLAssembler {
	
	private SBOLAssemblyGraph assemblyGraph;
	private SequenceTypeValidator seqValidator;
//	boolean validate = Preferences.userRoot().get(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "").equals("True");
	
	public SBOLAssembler(SBOLAssemblyGraph assemblyGraph, SequenceTypeValidator seqValidator) {
		this.assemblyGraph = assemblyGraph;
		this.seqValidator = seqValidator;
	}
	
	public DnaComponent exportDnaComponent(String exportFilePath, String saveDirectory) {
		DnaComponent assemblyComp = null;
		SBOLDocument sbolDoc = SBOLFactory.createDocument();
		SBOLUtility.addDNAComponent(assemblyComp, sbolDoc);
		SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
		return assemblyComp;
	}
	
	public DnaComponent assembleDNAComponent() {	
		// Orders list of subcomponents (to be assembled into composite component) 
		// by walking synthesis nodes
		List<SBOLAssemblyNode> orderedNodes = orderAssemblyNodes(new LinkedList<SBOLAssemblyNode>(assemblyGraph.getStartNodes()), 
				new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(), 
				new HashSet<SBOLAssemblyNode>(), new HashSet<SBOLAssemblyNode>());
	
		if (assemblyGraph.getFlatGraph() != null) {
			SBOLAssemblyGraph temp = assemblyGraph;
			assemblyGraph = assemblyGraph.getFlatGraph();
			List<SBOLAssemblyNode> flatOrderedNodes = orderAssemblyNodes(new LinkedList<SBOLAssemblyNode>(assemblyGraph.getStartNodes()), 
					new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(), 
					new HashSet<SBOLAssemblyNode>(), new HashSet<SBOLAssemblyNode>());
			assemblyGraph = temp;
			if (orderedNodes != null && SBOLUtility.loadNodeTypes(orderedNodes).size() > 0 
					&& flatOrderedNodes != null && SBOLUtility.loadNodeTypes(flatOrderedNodes).size() > 0)
				orderedNodes = compareOrderedNodes(orderedNodes, flatOrderedNodes);
			else
				orderedNodes = flatOrderedNodes;
		}
//		assemblyGraph.print(orderedNodes);
		if (orderedNodes == null || SBOLUtility.loadNodeTypes(orderedNodes).size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble composite DNA component with valid ordering of sequence types among its subcomponents.\n" +
					"(No orderings matching regular expressions for complete or partial genetic constructs were found.)", 
					"No Valid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
			return null;
		} else { 
			// Create composite component and its sequence
			DnaComponent assembledComp = new DnaComponentImpl();	
			DnaSequence synthSeq = new DnaSequenceImpl();
			synthSeq.setNucleotides("");
			assembledComp.setDnaSequence(synthSeq);

			int position = 1;
			LinkedList<String> subCompTypes = new LinkedList<String>();
			for (SBOLAssemblyNode assemblyNode : orderedNodes) 
				for (DnaComponent subComp : assemblyNode.getDNAComponents()) {
					position = addSubComponent(position, subComp, assembledComp);
					if (position == -1)
						return null;
					subCompTypes.addAll(SBOLUtility.loadDNAComponentTypes(subComp));
				}
			if (Preferences.userRoot().get(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "").equals("True") &&
					Preferences.userRoot().get(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "").equals("True") &&
					!seqValidator.validateCompleteConstruct(subCompTypes, false)) {
				Object[] options = { "OK", "Cancel" };
				int choice = JOptionPane.showOptionDialog(null, "Assembled Composite DNA component has incomplete ordering of sequence types among its subcomponents.\n" +
						"(Ordering does not match preferred regular expression for complete genetic construct.)", 
						"Incomplete Sequence Type Order", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (choice != JOptionPane.OK_OPTION)
					return null;
			}
			return assembledComp;
		}
	}
	
	private List<SBOLAssemblyNode> compareOrderedNodes(List<SBOLAssemblyNode> orderedNodes, List<SBOLAssemblyNode> flatOrderedNodes) {
		seqValidator.resetTerminalConstructValidator();
		List<List<SBOLAssemblyNode>> constructs = new LinkedList<List<SBOLAssemblyNode>>();
		int i = 0;
		while (i < orderedNodes.size()) {
			List<SBOLAssemblyNode> construct = new LinkedList<SBOLAssemblyNode>();
			int j = i;
			seqValidator.validateTerminalConstruct(SBOLUtility.loadNodeTypes(orderedNodes.get(j)), true);
			List<String> nextNodeTypes;
			do {
				construct.add(orderedNodes.get(j));
				seqValidator.validateTerminalConstruct(SBOLUtility.loadNodeTypes(orderedNodes.get(j)), true);
				j++;
				if (j < orderedNodes.size()) {
					nextNodeTypes = SBOLUtility.loadNodeTypes(orderedNodes.get(j));
					if (nextNodeTypes.size() > 1)
						nextNodeTypes = nextNodeTypes.subList(0, 1);
				} else 
					nextNodeTypes = new LinkedList<String>();
			} while (j < orderedNodes.size() 
					&& (!seqValidator.isTerminalConstructValid()
					|| seqValidator.validateTerminalConstruct(nextNodeTypes, false)));
			constructs.add(construct);
			i = j;
		}
		seqValidator.resetTerminalConstructValidator();
		List<List<SBOLAssemblyNode>> flatConstructs = new LinkedList<List<SBOLAssemblyNode>>();
		i = 0;
		while (i < flatOrderedNodes.size()) {
			List<SBOLAssemblyNode> flatConstruct = new LinkedList<SBOLAssemblyNode>();
			int j = i;
			seqValidator.validateTerminalConstruct(SBOLUtility.loadNodeTypes(flatOrderedNodes.get(j)), true);
			List<String> nextNodeTypes;
			do {
				flatConstruct.add(flatOrderedNodes.get(j));
				seqValidator.validateTerminalConstruct(SBOLUtility.loadNodeTypes(flatOrderedNodes.get(j)), true);
				j++;
				if (j < flatOrderedNodes.size()) {
					nextNodeTypes = SBOLUtility.loadNodeTypes(flatOrderedNodes.get(j));
					if (nextNodeTypes.size() > 1)
						nextNodeTypes = nextNodeTypes.subList(0, 1);
				} else 
					nextNodeTypes = new LinkedList<String>();
			} while (j < flatOrderedNodes.size() 
					&& (!seqValidator.isTerminalConstructValid()
					|| seqValidator.validateTerminalConstruct(nextNodeTypes, false)));
			flatConstructs.add(flatConstruct);
			i = j;
		}
		int initialJ = 0;
		if (constructs.size() > 1 && !seqValidator.validateCompleteConstruct(SBOLUtility.loadNodeTypes(constructs.get(initialJ)), false))
			initialJ++;
		int finalJ = constructs.size();
		if (constructs.size() > 1 && !seqValidator.validateCompleteConstruct(SBOLUtility.loadNodeTypes(constructs.get(finalJ - 1)), false))
			finalJ--;
		String flatConstructURISig = "";
		for (List<SBOLAssemblyNode> flatConstruct : flatConstructs) {
			List<URI> flatConstructURIs = SBOLUtility.loadNodeURIs(flatConstruct);
			for (URI flatConstructURI : flatConstructURIs)
				flatConstructURISig = flatConstructURISig + flatConstructURI.toString();
		}
		for (int j = initialJ; j < finalJ; j++) 
			for (int k = j; k < constructs.size(); k++) {
				constructs.add(finalJ - 1, constructs.remove(j));
				String constructURISig = "";
				for (List<SBOLAssemblyNode> construct : constructs) {
					List<URI> constructURIs = SBOLUtility.loadNodeURIs(construct);
					for (URI constructURI : constructURIs)
						constructURISig = constructURISig + constructURI.toString();
				}
				if (constructURISig.equals(flatConstructURISig))
					return orderedNodes;
			}
		return flatOrderedNodes;
	}
	
	private int addSubComponent(int position, DnaComponent subComp, DnaComponent synthComp) {	
		if (subComp.getDnaSequence() != null && subComp.getDnaSequence().getNucleotides() != null 
				&& subComp.getDnaSequence().getNucleotides().length() >= 1) {
			SequenceAnnotation annot = new SequenceAnnotationImpl();
			annot.setBioStart(position);
			position += subComp.getDnaSequence().getNucleotides().length() - 1;
			annot.setBioEnd(position);
			annot.setStrand(StrandType.POSITIVE);
			annot.setSubComponent(subComp);
			synthComp.addAnnotation(annot);
			position++;
			synthComp.getDnaSequence().setNucleotides(synthComp.getDnaSequence().getNucleotides() + subComp.getDnaSequence().getNucleotides());
		} else {
			JOptionPane.showMessageDialog(Gui.frame, "DNA Component " + subComp.getDisplayId() + " has no DNA sequence.", 
					"Missing DNA Sequence", JOptionPane.ERROR_MESSAGE);
			return -1;
		}	
		return position;
	}
	
//	private List<DnaComponent> orderSubComponents(List<String> startNodeIDs, List<String> currentNodeIDs, LinkedSet<String> localNodeIDs, 
//			Set<String> globalNodeIDs) {
//		List<DnaComponent> subComps = new LinkedList<DnaComponent>();
//		while (startNodeIDs.size() > 0 || currentNodeIDs.size() > 0) {
//			if (currentNodeIDs.size() == 0)
//				currentNodeIDs.add(startNodeIDs.remove(0));
//			List<DnaComponent> localComps = new LinkedList<DnaComponent>();
//			while (currentNodeIDs.size() > 0) {
////				List<DnaComponent> nodeComps = assemblyGraph.getNode(currentNodeIDs.get(0)).getDNAComponents();
//				List<String> nodeTypes = SBOLUtility.loadNodeTypes(assemblyGraph.getNode(currentNodeIDs.get(0)));
//				if (!seqValidator.validatePartialConstruct(nodeTypes, true)) {
//					JOptionPane.showMessageDialog(Gui.frame, "Composite DNA component assembled from model has invalid ordering of sequence types among its subcomponents.\n" +
//							"(Ordering does not match regular expression for complete or partial genetic construct.)", 
//							"Invalid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
//					return null;
//				} else {
////					localComps.addAll(nodeComps);
//					localNodeIDs.add(currentNodeIDs.get(0));
//					seqValidator.validateTerminalConstruct(nodeTypes, true);
//					List<String> nextNodeIDs = new LinkedList<String>();
//					for (String nextNodeID : assemblyGraph.getNextNodeIDs(currentNodeIDs.get(0))) {
//						List<String> nextNodeTypes = SBOLUtility.loadNodeTypes(assemblyGraph.getNode(nextNodeID));
//						if (nextNodeTypes.size() > 1)
//							nextNodeTypes = nextNodeTypes.subList(0, 1);
//						if ((seqValidator.isTerminalConstructValid() && !seqValidator.validateTerminalConstruct(nextNodeTypes, false)) 
//								|| !seqValidator.validatePartialConstruct(nextNodeTypes, false)) {
//							if (!globalNodeIDs.contains(nextNodeID)) {
//								startNodeIDs.add(nextNodeID);
//								globalNodeIDs.add(nextNodeID);
//							}
//						} else if (!localNodeIDs.contains(nextNodeID)) {
//							nextNodeIDs.add(nextNodeID);
//						}
//					}
//					if (nextNodeIDs.size() > 1) {
//						int i = 0;
//						List<DnaComponent> branchComps = null;	
//						while (i < nextNodeIDs.size()) {
//							i++;
//							List<String> copyStartNodeIDs = new LinkedList<String>(startNodeIDs);
//							List<String> copyCurrentNodeIDs = new LinkedList<String>(currentNodeIDs);
//							copyCurrentNodeIDs.addAll(0, nextNodeIDs);
//							Set<String> copyGlobalNodeIDs = new HashSet<String>(globalNodeIDs);
//							Set<String> copyLocalNodeIDs = new HashSet<String>(localNodeIDs);
//							branchComps = orderSubComponents(copyStartNodeIDs, copyCurrentNodeIDs, 
//									copyGlobalNodeIDs, copyLocalNodeIDs);
//							if (branchComps == null)
//								nextNodeIDs.add(nextNodeIDs.remove(0));
//							else
//								i = nextNodeIDs.size();
//						} 
//						return branchComps;
//					} else {
//						currentNodeIDs.remove(0);
//						currentNodeIDs.addAll(0, nextNodeIDs);
//					}
//				}
//			}
//			seqValidator.resetTerminalConstructValidator();
//			seqValidator.resetPartialConstructValidator();
//			List<String> localTypes = SBOLUtility.loadDNAComponentTypes(localComps);
//			List<String> subCompTypes = SBOLUtility.loadDNAComponentTypes(subComps);
//			if (seqValidator.validateStartConstruct(localTypes, false) 
//					&& seqValidator.validateTerminalConstruct(subCompTypes, false))
//				subComps.addAll(localComps);
//			else if (seqValidator.validateTerminalConstruct(localTypes, false)
//					&& seqValidator.validateStartConstruct(subCompTypes, false))
//				subComps.addAll(0, localComps);
//			else if (subComps.size() > 0 && localComps.size() > 0) {
//				JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//						"without introducing potentially unintended component interactions.", 
//						"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
//				return null;
//			} else
//				subComps.addAll(localComps);
//			globalNodeIDs.addAll(localNodeIDs);
//			localNodeIDs.clear();
//		}
//		return subComps;
//	}
	
	private List<SBOLAssemblyNode> orderAssemblyNodes(List<SBOLAssemblyNode> startNodes, List<SBOLAssemblyNode> currentNodes, 
			List<SBOLAssemblyNode> walkNodes, List<SBOLAssemblyNode> orderedNodes, 
			Set<SBOLAssemblyNode> localVisitedNodes, Set<SBOLAssemblyNode> globalVisitedNodes) {
		while (startNodes.size() > 0) {
			if (currentNodes.size() == 0)
				currentNodes.add(startNodes.get(0));
			while (currentNodes.size() > 0) {		
				List<String> currentNodeTypes = SBOLUtility.loadNodeTypes(currentNodes.get(0));
				if (currentNodeTypes.size() == 0 || seqValidator.validatePartialConstruct(currentNodeTypes, true)) {
					localVisitedNodes.add(currentNodes.get(0));
					seqValidator.validateTerminalConstruct(currentNodeTypes, true);
					List<SBOLAssemblyNode> nextNodes = new LinkedList<SBOLAssemblyNode>();
					for (SBOLAssemblyNode nextNode : assemblyGraph.getNextNodes(currentNodes.get(0))) {
						Set<SBOLAssemblyNode> previousNodes = new HashSet<SBOLAssemblyNode>(assemblyGraph.getPreviousNodes(nextNode));
						previousNodes.remove(currentNodes.get(0));
						if (previousNodes.size() == 0 || (seqValidator.isPartialConstructStarted() && !seqValidator.isTerminalConstructValid()) 
								|| (globalVisitedNodes.containsAll(previousNodes) && !globalVisitedNodes.contains(nextNode))
								|| (localVisitedNodes.containsAll(previousNodes) && !localVisitedNodes.contains(nextNode))) {
							List<String> nextNodeTypes = SBOLUtility.loadNodeTypes(nextNode);
							if (nextNodeTypes.size() > 1)
								nextNodeTypes = nextNodeTypes.subList(0, 1);
							if (!seqValidator.validatePartialConstruct(nextNodeTypes, false) 
									|| (!seqValidator.isPartialConstructStarted()) 
									|| (seqValidator.isTerminalConstructValid() && !seqValidator.validateTerminalConstruct(nextNodeTypes, false))) 
								startNodes.add(nextNode);
							else if (!localVisitedNodes.contains(nextNode))
								nextNodes.add(nextNode);
						}
					}
					walkNodes.add(currentNodes.remove(0));
					if (nextNodes.size() == 0)
						while (currentNodes.size() > 0 && localVisitedNodes.contains(currentNodes.get(0)))
							currentNodes.remove(0);
					else if (nextNodes.size() == 1) 
						currentNodes.add(0, nextNodes.get(0));
					else if (nextNodes.size() > 1) {
						for (int i = 0; i < nextNodes.size(); i++) 
							for (int j = i; j < nextNodes.size(); j++) {
								nextNodes.add(nextNodes.remove(i));
								List<SBOLAssemblyNode> copyStartNodes = new LinkedList<SBOLAssemblyNode>(startNodes);
								List<SBOLAssemblyNode> copyCurrentNodes = new LinkedList<SBOLAssemblyNode>(currentNodes);
								copyCurrentNodes.addAll(0, nextNodes);
								List<SBOLAssemblyNode> copyWalkNodes = new LinkedList<SBOLAssemblyNode>(walkNodes);
								List<SBOLAssemblyNode> copyOrderedNodes = new LinkedList<SBOLAssemblyNode>(orderedNodes);
								Set<SBOLAssemblyNode> copyLocalVisitedNodes = new HashSet<SBOLAssemblyNode>(localVisitedNodes);
								Set<SBOLAssemblyNode> copyGlobalVisitedNodes = new HashSet<SBOLAssemblyNode>(globalVisitedNodes);
								seqValidator.savePartialConstructValidator();
								seqValidator.saveTerminalConstructValidator();
								List<SBOLAssemblyNode> branchSolution = orderAssemblyNodes(copyStartNodes, copyCurrentNodes,
										copyWalkNodes, copyOrderedNodes, copyLocalVisitedNodes, copyGlobalVisitedNodes);
								if (branchSolution == null) {
									seqValidator.loadPartialConstructValidator();
									seqValidator.loadTerminalConstructValidator();
								} else
									return branchSolution;
							} 
						return null;
					}
				} else {
//					JOptionPane.showMessageDialog(Gui.frame, "Composite DNA component assembled from model has invalid ordering of sequence types among its subcomponents.\n" +
//					"(Ordering does not match regular expression for complete or partial genetic construct.)", 
//					"Invalid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} 
			seqValidator.resetTerminalConstructValidator();
			seqValidator.resetPartialConstructValidator();
			List<String> walkNodeTypes = SBOLUtility.loadNodeTypes(walkNodes);
			List<String> orderedNodeTypes = SBOLUtility.loadNodeTypes(orderedNodes);
			if (seqValidator.validateStartConstruct(walkNodeTypes, false) 
					&& seqValidator.validateTerminalConstruct(orderedNodeTypes, false))
				orderedNodes.addAll(walkNodes);
			else if (seqValidator.validateTerminalConstruct(walkNodeTypes, false)
					&& seqValidator.validateStartConstruct(orderedNodeTypes, false))
				orderedNodes.addAll(0, walkNodes);
			else if (walkNodeTypes.size() > 0 && orderedNodeTypes.size() > 0) {
//				JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//						"without introducing potentially unintended component interactions.", 
//						"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
				return null;
			} else
				orderedNodes.addAll(walkNodes);
			walkNodes.clear();
			globalVisitedNodes.addAll(localVisitedNodes);
			localVisitedNodes.clear();
			while (startNodes.size() > 0 && globalVisitedNodes.contains(startNodes.get(0)))
				startNodes.remove(0);
		}
		// Order nodes that belong to cycles
		if (globalVisitedNodes.size() < assemblyGraph.size()) {
			Set<SBOLAssemblyNode> cycleNodes = new HashSet<SBOLAssemblyNode>(assemblyGraph.getNodes());
			cycleNodes.removeAll(globalVisitedNodes);
			Set<String> startNodeTypes = seqValidator.getStartTypes();
			for (SBOLAssemblyNode cycleNode : cycleNodes) {
				List<String> cycleNodeTypes = SBOLUtility.loadNodeTypes(cycleNode);
				if (cycleNodeTypes.size() > 0 && startNodeTypes.contains(cycleNodeTypes.get(0))) 
					startNodes.add(cycleNode);
			}
			List<SBOLAssemblyNode> cycleSolution;
			for (SBOLAssemblyNode startNode : startNodes) {
				List<SBOLAssemblyNode> copyStartNode = new LinkedList<SBOLAssemblyNode>();
				copyStartNode.add(startNode);
				Set<SBOLAssemblyNode> copyGlobalVisitedNodes = new HashSet<SBOLAssemblyNode>(globalVisitedNodes);
				List<SBOLAssemblyNode> copyOrderedNodes = new LinkedList<SBOLAssemblyNode>(orderedNodes);
				seqValidator.savePartialConstructValidator();
				seqValidator.saveTerminalConstructValidator();
				cycleSolution = orderAssemblyNodes(copyStartNode, new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(),
						copyOrderedNodes, new HashSet<SBOLAssemblyNode>(), copyGlobalVisitedNodes);
				if (cycleSolution == null) {
					seqValidator.loadPartialConstructValidator();
					seqValidator.loadTerminalConstructValidator();
				} else
					return cycleSolution;
			}
			startNodes.clear();
			cycleNodes.removeAll(startNodes);
			startNodes.addAll(cycleNodes);
			for (SBOLAssemblyNode startNode : startNodes) {
				List<SBOLAssemblyNode> copyStartNode = new LinkedList<SBOLAssemblyNode>();
				copyStartNode.add(startNode);
				Set<SBOLAssemblyNode> copyGlobalVisitedNodes = new HashSet<SBOLAssemblyNode>(globalVisitedNodes);
				List<SBOLAssemblyNode> copyOrderedNodes = new LinkedList<SBOLAssemblyNode>(orderedNodes);
				seqValidator.savePartialConstructValidator();
				seqValidator.saveTerminalConstructValidator();
				cycleSolution = orderAssemblyNodes(copyStartNode, new LinkedList<SBOLAssemblyNode>(), new LinkedList<SBOLAssemblyNode>(),
						copyOrderedNodes, new HashSet<SBOLAssemblyNode>(), copyGlobalVisitedNodes);
				if (cycleSolution == null) {
					seqValidator.loadPartialConstructValidator();
					seqValidator.loadTerminalConstructValidator();
				} else
					return cycleSolution;
			}
			return null;
		} else
			return orderedNodes;
	}
	
//	private List<DnaComponent> orderSubComponents(boolean validate) {
//		List<DnaComponent> subComps = new LinkedList<DnaComponent>();
//		List<DnaComponent> subCompsWithoutTerminal = new LinkedList<DnaComponent>();
//		List<DnaComponent> subCompsWithoutStart = new LinkedList<DnaComponent>();
//		List<DnaComponent> subCompsWithoutStartOrTerminal = new LinkedList<DnaComponent>();
//		Set<String> startTypes = seqValidator.getStartTypes();
//		Set<String> terminalTypes = seqValidator.getTerminalTypes();
//		for (String startNodeID : assemblyGraph.getStartNodeIDs()) {
//			List<DnaComponent> dnaComps = gatherDNAComponents(startNodeID, terminalTypes, validate);
//			if (dnaComps == null) 
//				return null;
//			else if (dnaComps.size() > 0) {
//				if (subCompsWithoutStartOrTerminal.size() == 0 || !validate) {
//					List<String> leftTypes = SBOLUtility.loadLowestSOTypes(dnaComps.get(0));
//					boolean dnaCompsHaveStart = startTypes.contains(leftTypes.get(0));
//					List<String> rightTypes = SBOLUtility.loadLowestSOTypes(dnaComps.get(dnaComps.size() - 1));
//					boolean dnaCompsHaveTerminal = terminalTypes.contains(rightTypes.get(rightTypes.size() - 1));
//					if (dnaCompsHaveStart && dnaCompsHaveTerminal) 
//						subComps.addAll(dnaComps);
//					else if (!dnaCompsHaveStart && dnaCompsHaveTerminal) 
//						if (subCompsWithoutStart.size() == 0 || !validate)
//							subCompsWithoutStart.addAll(dnaComps);
//						else {
//							JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//									"without introducing potentially unintended component interactions.", 
//									"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
//							return null;
//						}
//					else if (dnaCompsHaveStart && !dnaCompsHaveTerminal)
//						if (subCompsWithoutTerminal.size() == 0 || !validate)
//							subCompsWithoutTerminal.addAll(dnaComps);
//						else {
//							JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//									"without introducing potentially unintended component interactions.", 
//									"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
//							return null;
//						}
//					else
//						if (!validate || (subComps.size() == 0 && subCompsWithoutStart.size() == 0 
//								&& subCompsWithoutTerminal.size() == 0 && subCompsWithoutStartOrTerminal.size() == 0))
//							subCompsWithoutStartOrTerminal.addAll(dnaComps);
//						else {
//							JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//									"without introducing potentially unintended component interactions.", 
//									"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
//							return null;
//						}
//				} else {
//					JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble DNA components associated with model into single sequence\n" +
//							"without introducing potentially unintended component interactions.", 
//							"Invalid SBOL Assembly", JOptionPane.ERROR_MESSAGE);
//					return null;
//				}
//			}
//		}
//		if (subCompsWithoutStartOrTerminal.size() > 0 && validate)
//			return subCompsWithoutStartOrTerminal;
//		else {
//			subComps.addAll(0, subCompsWithoutStart);
//			subComps.addAll(subCompsWithoutTerminal);
//			if (!validate)
//				subComps.addAll(subCompsWithoutStartOrTerminal);
//			return subComps;
//		}	
//	}
	
//	private List<DnaComponent> gatherDNAComponents(String startNodeID, Set<String> terminalTypes, boolean validate) {
//		List<String> currentNodeIDs = new LinkedList<String>();
//		List<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
//		currentNodeIDs.add(0, startNodeID);
//		seqValidator.resetPartialConstructValidator();
//		while (currentNodeIDs.size() > 0) {
//			SBOLAssemblyNode currentNode = assemblyGraph.getNode(currentNodeIDs.get(0));
//			if (!validate || isValidNode(currentNode))
//				dnaComps.addAll(currentNode.getDNAComponents());
//			else 
//				return null;
//			if (assemblyGraph.getNextNodeIDs(currentNodeIDs.get(0)).size() > 1);
//				orderNonTerminalBranchesFirst(currentNodeIDs.get(0), terminalTypes);
//			currentNodeIDs.addAll(0, assemblyGraph.getNextNodeIDs(currentNodeIDs.remove(0)));
//		}
//		return dnaComps;
//	}
	
//	private boolean isValidNode(SBOLAssemblyNode assemblyNode) {
//		List<String> soTypes = new LinkedList<String>();
//		for (DnaComponent dnaComp : assemblyNode.getDNAComponents())
//			soTypes.addAll(SBOLUtility.loadLowestSOTypes(dnaComp));
//		if (soTypes.size() > 0)
//			if (seqValidator.validatePartialConstruct(soTypes, true))
//				return true;
//			else {
//				JOptionPane.showMessageDialog(Gui.frame, "Composite DNA component assembled from model has invalid ordering of sequence types among its subcomponents.\n" +
//						"(Ordering does not match regular expression for complete or partial genetic construct.)", 
//						"Invalid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
//				return false;
//			}
//		return true;
//	}
	
//	private boolean orderNonTerminalBranchesFirst(String branchingNodeID, Set<String> terminalTypes) {
//		List<String> orderedBranchNodeIDs = new LinkedList<String>();
//		boolean isTerminalBranch = false;
//		for (String branchNodeID : assemblyGraph.getNextNodeIDs(branchingNodeID)) {
//			String currentNodeID = branchNodeID;
//			SBOLAssemblyNode currentNode = assemblyGraph.getNode(branchNodeID);
//			boolean terminalDetected = false;
//			while (currentNodeID.length() > 0) {
//				Iterator<DnaComponent> dnaCompIterator = currentNode.getDNAComponents().iterator();
//				while (!terminalDetected && dnaCompIterator.hasNext()) {
//					List<String> soTypes = SBOLUtility.loadDNAComponentTypes(dnaCompIterator.next());
//					Iterator<String> terminalIterator = terminalTypes.iterator();
//					while (!terminalDetected && terminalIterator.hasNext()) 
//						if (soTypes.contains(terminalIterator.next())) {
//							terminalDetected = true;
//							isTerminalBranch = true;
//						}
//				}
//				if (terminalDetected) {
//					orderedBranchNodeIDs.add(branchNodeID);
//					currentNodeID = "";
//				} else if (assemblyGraph.getNextNodeIDs(currentNodeID).size() == 0) {
//					orderedBranchNodeIDs.add(0, branchNodeID);
//					currentNodeID = "";
//				} else if (assemblyGraph.getNextNodeIDs(currentNodeID).size() == 1) {
//					currentNodeID =  assemblyGraph.getNextNodeIDs(currentNodeID).iterator().next();
//					currentNode = assemblyGraph.getNode(currentNodeID);
//				} else { // more than one next node
//					if (orderNonTerminalBranchesFirst(currentNodeID, terminalTypes)) 
//						orderedBranchNodeIDs.add(branchNodeID);
//					else 
//						orderedBranchNodeIDs.add(0, branchNodeID);
//					currentNodeID = "";
//				}
//			}			
//		}
//		assemblyGraph.getEdges().put(branchingNodeID, orderedBranchNodeIDs);
//		return isTerminalBranch;
//	}
	
}
