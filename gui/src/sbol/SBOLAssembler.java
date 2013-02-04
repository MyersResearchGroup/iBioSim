package sbol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;

public class SBOLAssembler {
	
	private SBOLAssemblyGraph synGraph;
	private SequenceTypeValidator seqValidator;
	
	public SBOLAssembler(SBOLAssemblyGraph synGraph, SequenceTypeValidator seqValidator) {
		this.synGraph = synGraph;
		this.seqValidator = seqValidator;
	}
	
	public DnaComponent exportDnaComponent(String exportFilePath, String saveDirectory) {
		DnaComponent synthComp = null;
		SBOLDocument sbolDoc = SBOLFactory.createDocument();
		SBOLUtility.addDNAComponent(synthComp, sbolDoc);
		SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
		return synthComp;
	}
	
	public DnaComponent synthesizeDNAComponent() {	
		// Orders list of subcomponents (to be assembled into composite component) 
		// by walking synthesis nodes
		List<DnaComponent> subComps = orderSubComponents();
		if (subComps == null)
			return null;
		// Create composite component and its sequence
		DnaComponent synthComp = new DnaComponentImpl();	
		DnaSequence synthSeq = new DnaSequenceImpl();
		synthSeq.setNucleotides("");
		synthComp.setDnaSequence(synthSeq);

		int position = 1;
		int addCount = 0;
		LinkedList<String> types = new LinkedList<String>();
		for (DnaComponent subComp : subComps) {
			position = addSubComponent(position, subComp, synthComp, addCount);
			if (position == -1)
				return null;
			addCount++;
			types.addAll(SBOLUtility.loadLowestSOTypes(subComp));
		}

		if (seqValidator != null && !seqValidator.validateConstruct(types)) {
			Object[] options = { "OK", "Cancel" };
			int choice = JOptionPane.showOptionDialog(null, 
					"Ordering of DNA components associated with model does not match\n preferred regular expression for a complete genetic construct.\n  Proceed with synthesis?", 
					"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (choice != JOptionPane.OK_OPTION)
				return null;
		}
		return synthComp;
	}
	
	private int addSubComponent(int position, DnaComponent subComp, DnaComponent synthComp, int addCount) {	
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
					"Invalid DNA Sequence", JOptionPane.ERROR_MESSAGE);
			return -1;
		}	
		return position;
	}
	
	private List<DnaComponent> orderSubComponents() {
		List<DnaComponent> subComps = new LinkedList<DnaComponent>();
		List<DnaComponent> subCompsWithoutTerminal = new LinkedList<DnaComponent>();
		List<DnaComponent> subCompsWithoutStart = new LinkedList<DnaComponent>();
		List<DnaComponent> subCompsWithoutStartOrTerminal = new LinkedList<DnaComponent>();
		Set<String> startTypes = seqValidator.getStartTypes();
		Set<String> terminalTypes = seqValidator.getTerminalTypes();
		for (SBOLAssemblyNode startNode : synGraph.getStartNodes()) {
			seqValidator.resetFragmentValidator();
			List<DnaComponent> dnaComps = gatherDNAComponents(startNode, seqValidator, terminalTypes);
			if (dnaComps == null) 
				return null;
			else if (dnaComps.size() > 0) {
				if (subCompsWithoutStartOrTerminal.size() == 0) {
					List<String> leftTypes = SBOLUtility.loadLowestSOTypes(dnaComps.get(0));
					boolean dnaCompsHaveStart = startTypes.contains(leftTypes.get(0));
					List<String> rightTypes = SBOLUtility.loadLowestSOTypes(dnaComps.get(dnaComps.size() - 1));
					boolean dnaCompsHaveTerminal = terminalTypes.contains(rightTypes.get(rightTypes.size() - 1));
					if (dnaCompsHaveStart && dnaCompsHaveTerminal) 
						subComps.addAll(dnaComps);
					else if (!dnaCompsHaveStart && dnaCompsHaveTerminal) 
						if (subCompsWithoutStart.size() == 0)
							subCompsWithoutStart.addAll(dnaComps);
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Failed to serialize DNA components into a single sequence without introducing potentially unintended component interactions.", 
									"Serialization Error", JOptionPane.ERROR_MESSAGE);
							return null;
						}
					else if (dnaCompsHaveStart && !dnaCompsHaveTerminal)
						if (subCompsWithoutTerminal.size() == 0)
							subCompsWithoutTerminal.addAll(dnaComps);
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Failed to serialize DNA components into a single sequence without introducing potentially unintended component interactions.", 
									"Serialization Error", JOptionPane.ERROR_MESSAGE);
							return null;
						}
					else
						if (subComps.size() == 0 && subCompsWithoutStart.size() == 0 
								&& subCompsWithoutTerminal.size() == 0 && subCompsWithoutStartOrTerminal.size() == 0)
							subCompsWithoutStartOrTerminal.addAll(dnaComps);
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Failed to serialize DNA components into a single sequence without introducing potentially unintended component interactions.", 
									"Serialization Error", JOptionPane.ERROR_MESSAGE);
							return null;
						}
				} else {
					JOptionPane.showMessageDialog(Gui.frame, "Failed to serialize DNA components into a single sequence without introducing potentially unintended component interactions.", 
							"Serialization Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		if (subCompsWithoutStartOrTerminal.size() > 0)
			return subCompsWithoutStartOrTerminal;
		else {
			subComps.addAll(0, subCompsWithoutStart);
			subComps.addAll(subCompsWithoutTerminal);
			return subComps;
		}
	}
	
	private List<DnaComponent> gatherDNAComponents(SBOLAssemblyNode startNode, SequenceTypeValidator seqValidator, 
			Set<String> terminalTypes) {
		List<SBOLAssemblyNode> currentNodes = new LinkedList<SBOLAssemblyNode>();
		List<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
		currentNodes.add(0, startNode);
		while (currentNodes.size() > 0) {
			List<String> soTypes = new LinkedList<String>();
			for (DnaComponent dnaComp : currentNodes.get(0).getDNAComponents())
				soTypes.addAll(SBOLUtility.loadLowestSOTypes(dnaComp));
			if (soTypes.size() > 0) 
				if (seqValidator.validateFragment(soTypes))
					dnaComps.addAll(currentNodes.get(0).getDNAComponents());
				else {
					JOptionPane.showMessageDialog(Gui.frame, "Serialized DNA components have an invalid ordering of sequence types.", 
							"Invalid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			if (currentNodes.get(0).getNextNodes().size() > 1)
				orderNonTerminalBranchesFirst(currentNodes.get(0), terminalTypes);
			currentNodes.addAll(0, currentNodes.remove(0).getNextNodes());
		}
		return dnaComps;
	}
	
	private boolean orderNonTerminalBranchesFirst(SBOLAssemblyNode branchingNode,
			Set<String> terminalTypes) {
		List<SBOLAssemblyNode> orderedBranchNodes = new LinkedList<SBOLAssemblyNode>();
		boolean isTerminalBranch = false;
		for (SBOLAssemblyNode branchNode : branchingNode.getNextNodes()) {
			SBOLAssemblyNode currentNode = branchNode;
			boolean terminalDetected = false;
			while (currentNode != null) {
				Iterator<DnaComponent> dnaCompIterator = currentNode.getDNAComponents().iterator();
				while (!terminalDetected && dnaCompIterator.hasNext()) {
					List<String> soTypes = SBOLUtility.loadLowestSOTypes(dnaCompIterator.next());
					Iterator<String> terminalIterator = terminalTypes.iterator();
					while (!terminalDetected && terminalIterator.hasNext()) {
						terminalDetected = soTypes.contains(terminalIterator.next());
						if (soTypes.contains(terminalIterator.next())) {
							terminalDetected = true;
							isTerminalBranch = true;
						}
					}
				}
				if (terminalDetected) {
					orderedBranchNodes.add(branchNode);
					currentNode = null;
				} else if (currentNode.getNextNodes().size() == 0) {
					orderedBranchNodes.add(0, branchNode);
					currentNode = null;
				} else if (currentNode.getNextNodes().size() == 1)
					currentNode = currentNode.getNextNodes().iterator().next();
				else { // more than one next node
					if (orderNonTerminalBranchesFirst(currentNode, terminalTypes)) 
						orderedBranchNodes.add(branchNode);
					else 
						orderedBranchNodes.add(0, branchNode);
					currentNode = null;
				}
			}			
		}
		branchingNode.setNextNodes(orderedBranchNodes);
		return isTerminalBranch;
	}
	
}
