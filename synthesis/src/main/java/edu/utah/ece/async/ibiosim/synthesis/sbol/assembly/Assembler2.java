/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.synthesis.sbol.assembly;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.sbol.util.SBOLUtility2;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.sbolstandard.core2.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Assembler2 
{
	
	private AssemblyGraph2 assemblyGraph;
	private SequenceTypeValidator seqValidator;
////	boolean validate = Preferences.userRoot().get(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "").equals("True");
//	
	public Assembler2(AssemblyGraph2 assemblyGraph, SequenceTypeValidator seqValidator) 
	{
		this.assemblyGraph = assemblyGraph;
		this.seqValidator = seqValidator;
	}
	
//	public static ComponentDefinition exportDnaComponent(String exportFilePath) {
//		ComponentDefinition assemblyComp = null;
////		SBOLDocument sbolDoc = SBOLFactory.createDocument(); 
//		SBOLDocument sbolDoc = new SBOLDocument();
//		//		SBOLUtility2.addDNAComponent(assemblyComp, sbolDoc, false);
//		SBOLUtility2.addDNAComponent(assemblyComp, sbolDoc, false); 
//		SBOLUtility2.writeSBOLDocument(exportFilePath, sbolDoc);
//		return assemblyComp;
//	}
//	
	public ComponentDefinition assembleDNAComponent(SBOLDocument sbolDoc, String defaultURIPrefix) throws SBOLValidationException, SBOLException 
	{	
		// Orders list of subcomponents (to be assembled into composite component) 
		// by walking synthesis nodes
		List<AssemblyNode2> orderedNodes = orderAssemblyNodes(new LinkedList<AssemblyNode2>(assemblyGraph.getStartNodes()), 
				new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(), 
				new HashSet<AssemblyNode2>(), new HashSet<AssemblyNode2>());
	
		if (assemblyGraph.getFlatGraph() != null) {
			AssemblyGraph2 temp = assemblyGraph;
			assemblyGraph = assemblyGraph.getFlatGraph();
			List<AssemblyNode2> flatOrderedNodes = orderAssemblyNodes(new LinkedList<AssemblyNode2>(assemblyGraph.getStartNodes()), 
					new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(), 
					new HashSet<AssemblyNode2>(), new HashSet<AssemblyNode2>());
			assemblyGraph = temp;
			if (orderedNodes != null && loadNodeTypes(orderedNodes).size() > 0 
					&& flatOrderedNodes != null && loadNodeTypes(flatOrderedNodes).size() > 0)
				orderedNodes = compareOrderedNodes(orderedNodes, flatOrderedNodes);
			else
				orderedNodes = flatOrderedNodes;
		}
//		assemblyGraph.print(orderedNodes);
		if (orderedNodes == null || loadNodeTypes(orderedNodes).size() == 0) {
			
//			JOptionPane.showMessageDialog(Gui.frame, "Failed to assemble composite DNA component with valid ordering of sequence types among its subcomponents.\n" +
//					"(No orderings matching regular expressions for complete or partial genetic constructs were found.)", 
//					"No Valid Sequence Type Order", JOptionPane.ERROR_MESSAGE);
			
			String message = "Failed to assemble composite DNA component with valid ordering of sequence types among its subcomponents.\n" +
					"(No orderings matching regular expressions for complete or partial genetic constructs were found.)";
			String messageTitle = "No Valid Sequence Type Order";
			throw new SBOLException(message, messageTitle);
//			return null;
		}
		// Create composite component and its sequence
//		DnaComponent assembledComp = new DnaComponentImpl();
//		assembledComp.addType(SequenceOntology.type("SO_0000804")); 
//		DnaSequence synthSeq = new DnaSequenceImpl();
//		synthSeq.setNucleotides(""); 
//		assembledComp.setDnaSequence(synthSeq); 
		
//		ComponentDefinition  assembledComp = new ComponentDefinition(GlobalConstants.SBOL_AUTHORITY_DEFAULT, "assembledComp", "", new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA)));
		ComponentDefinition  assembledComp = sbolDoc.createComponentDefinition(defaultURIPrefix, "assembledComp", "", new HashSet<URI>(Arrays.asList(ComponentDefinition.DNA)));
		assembledComp.addRole(SequenceOntology.ENGINEERED_REGION);
		Sequence synthSeq = sbolDoc.createSequence(defaultURIPrefix, "synthSeq", "", "", Sequence.IUPAC_DNA);
		assembledComp.addSequence(synthSeq);
		
		//Assemble the model to generate graph
		int position = 1;
		LinkedList<String> subCompTypes = new LinkedList<String>();
		for (AssemblyNode2 assemblyNode : orderedNodes) {
//			List<DnaComponent> subComps = assemblyNode.getDNAComponents();
			List<ComponentDefinition> subComps = assemblyNode.getDNAComponents();
			
			if (assemblyNode.getStrand().equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
				for (int i = subComps.size() - 1; i >= 0; i--) {
					position = addSubComponent(position, subComps.get(i), assembledComp, assemblyNode.getStrand(), synthSeq);
					if (position == -1)
						return null;
					subCompTypes.addAll(0, loadAssemblyTypes(subComps.get(i)));
				}
			else
				for (int i = 0; i < subComps.size(); i++) {
					position = addSubComponent(position, subComps.get(i), assembledComp, assemblyNode.getStrand(), synthSeq);
					if (position == -1)
						return null;
					subCompTypes.addAll(loadAssemblyTypes(subComps.get(i)));
				}
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
	
	private List<AssemblyNode2> compareOrderedNodes(List<AssemblyNode2> orderedNodes, List<AssemblyNode2> flatOrderedNodes) {
		seqValidator.resetTerminalConstructValidator();
		List<List<AssemblyNode2>> constructs = new LinkedList<List<AssemblyNode2>>();
		int i = 0;
		while (i < orderedNodes.size()) {
			List<AssemblyNode2> construct = new LinkedList<AssemblyNode2>();
			int j = i;
			seqValidator.validateTerminalConstruct(loadNodeTypes(orderedNodes.get(j)), true);
			List<String> nextNodeTypes;
			do {
				construct.add(orderedNodes.get(j));
				seqValidator.validateTerminalConstruct(loadNodeTypes(orderedNodes.get(j)), true);
				j++;
				if (j < orderedNodes.size()) {
					nextNodeTypes = loadNodeTypes(orderedNodes.get(j));
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
		List<List<AssemblyNode2>> flatConstructs = new LinkedList<List<AssemblyNode2>>();
		i = 0;
		while (i < flatOrderedNodes.size()) {
			List<AssemblyNode2> flatConstruct = new LinkedList<AssemblyNode2>();
			int j = i;
			seqValidator.validateTerminalConstruct(loadNodeTypes(flatOrderedNodes.get(j)), true);
			List<String> nextNodeTypes;
			do {
				flatConstruct.add(flatOrderedNodes.get(j));
				seqValidator.validateTerminalConstruct(loadNodeTypes(flatOrderedNodes.get(j)), true);
				j++;
				if (j < flatOrderedNodes.size()) {
					nextNodeTypes = loadNodeTypes(flatOrderedNodes.get(j));
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
		if (constructs.size() > 1 && !seqValidator.validateCompleteConstruct(loadNodeTypes(constructs.get(initialJ)), false))
			initialJ++;
		int finalJ = constructs.size();
		if (constructs.size() > 1 && !seqValidator.validateCompleteConstruct(loadNodeTypes(constructs.get(finalJ - 1)), false))
			finalJ--;
		String flatConstructURISig = "";
		for (List<AssemblyNode2> flatConstruct : flatConstructs) {
			List<URI> flatConstructURIs = loadNodeURIs(flatConstruct);
			for (URI flatConstructURI : flatConstructURIs)
				flatConstructURISig = flatConstructURISig + flatConstructURI.toString();
		}
		for (int j = initialJ; j < finalJ; j++) 
			for (int k = j; k < constructs.size(); k++) {
				constructs.add(finalJ - 1, constructs.remove(j));
				String constructURISig = "";
				for (List<AssemblyNode2> construct : constructs) {
					List<URI> constructURIs = loadNodeURIs(construct);
					for (URI constructURI : constructURIs)
						constructURISig = constructURISig + constructURI.toString();
				}
				if (constructURISig.equals(flatConstructURISig))
					return orderedNodes;
			}
		return flatOrderedNodes;
	}

	private static int addSubComponent(int position, ComponentDefinition subComp, ComponentDefinition parentComp, String strand,Sequence parentSeq) throws SBOLValidationException, SBOLException 
	{
		//Assume that each CompDef given only has 1 Sequence and 1 element?
//		if (subComp.getSequences() != null && subComp.getSequences().iterator().next().getElements() != null 
//				&& subComp.getSequences().iterator().next().getElements().length() >= 1) {
		if (subComp.getSequences() != null
				&& subComp.getSequences().size() >= 1) {
//			SequenceAnnotation annot = new SequenceAnnotationImpl();
//			annot.setBioStart(position);
//			position += subComp.getDnaSequence().getNucleotides().length() - 1;
//			annot.setBioEnd(position);
//			if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
//				annot.setStrand(StrandType.NEGATIVE);
//			else
//				annot.setStrand(StrandType.POSITIVE);
//			annot.setSubComponent(subComp);
//			parentComp.addAnnotation(annot);
//			position++;
			
			int start = position;
			position += subComp.getSequences().iterator().next().getElements().length() - 1;
			int end = position;
			OrientationType orient; 
			if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
				orient = OrientationType.REVERSECOMPLEMENT;
			else
				orient = OrientationType.INLINE;
			SequenceAnnotation annot = parentComp.createSequenceAnnotation("parentComp_annot"+position, "annot_range", start, end, orient);
			parentComp.createComponent(subComp.getDisplayId(), AccessType.PRIVATE, subComp.getIdentity());
			annot.setComponent(subComp.getDisplayId());
			position++;
			
//			DnaSequenceImpl subSeq = (DnaSequenceImpl) subComp.getDnaSequence();
//			if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
//				parentComp.getDnaSequence().setNucleotides(parentComp.getDnaSequence().getNucleotides() 
//					+ subSeq.getReverseComplementaryNucleotides());
//			else
//				parentComp.getDnaSequence().setNucleotides(parentComp.getDnaSequence().getNucleotides() 
//						+ subSeq.getNucleotides());
			
			Sequence subSeq = subComp.getSequences().iterator().next();
			if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
			{
				//TODO: How to handle this getReverseComplementaryNucleotides()?
				parentSeq.setElements(parentSeq.getElements()
						+ getReverseComplementaryNucleotides(subSeq.getElements()));
			}
			else
			{
				parentSeq.setElements(parentSeq.getElements()
						+ subSeq.getElements());
			}
			
		} 
		else 
		{
//			JOptionPane.showMessageDialog(Gui.frame, "DNA Component " + subComp.getDisplayId() + " has no DNA sequence.", 
//					"Missing DNA Sequence", JOptionPane.ERROR_MESSAGE);
			String message = "DNA Component " + subComp.getDisplayId() + " has no DNA sequence.";
			String messageTitle = "Missing DNA Sequence";
			throw new SBOLException(message, messageTitle);
//			return -1;
		}	
		return position;
	}
	
	public static String getReverseComplementaryNucleotides(String nucleotides) {
		StringBuilder complementary = new StringBuilder(nucleotides.length());
		for (int i = nucleotides.length() - 1; i >= 0; i--) {
			char nucleotide = nucleotides.charAt(i);
			if (nucleotide == 'a')
				complementary.append('t');
			else if (nucleotide == 't')
				complementary.append('a');
			else if (nucleotide == 'g')
				complementary.append('c');
			else if (nucleotide == 'c')
				complementary.append('g');
			else if (nucleotide == 'm')
				complementary.append('k');
			else if (nucleotide == 'r')
				complementary.append('y');
			else if (nucleotide == 'w')
				complementary.append('w');
			else if (nucleotide == 's')
				complementary.append('s');
			else if (nucleotide == 'y')
				complementary.append('r');
			else if (nucleotide == 'k')
				complementary.append('m');
			else if (nucleotide == 'v')
				complementary.append('b');
			else if (nucleotide == 'h')
				complementary.append('d');
			else if (nucleotide == 'd')
				complementary.append('h');
			else if (nucleotide == 'b')
				complementary.append('v');
			else if (nucleotide == 'n')
				complementary.append('n');
		}
		return complementary.toString();
	}
	
	private List<AssemblyNode2> orderAssemblyNodes(List<AssemblyNode2> startNodes, List<AssemblyNode2> currentNodes, 
			List<AssemblyNode2> walkNodes, List<AssemblyNode2> orderedNodes, 
			Set<AssemblyNode2> localVisitedNodes, Set<AssemblyNode2> globalVisitedNodes) {
		while (startNodes.size() > 0) {
			if (currentNodes.size() == 0)
				currentNodes.add(startNodes.get(0));
			while (currentNodes.size() > 0) {		
				List<String> currentNodeTypes = loadNodeTypes(currentNodes.get(0));
//				currentNodeTypes.size() == 0 || 
				if (seqValidator.validatePartialConstruct(currentNodeTypes, true)) {
					localVisitedNodes.add(currentNodes.get(0));
					seqValidator.validateTerminalConstruct(currentNodeTypes, true);
					List<AssemblyNode2> nextNodes = new LinkedList<AssemblyNode2>();
					for (AssemblyNode2 nextNode : assemblyGraph.getNextNodes(currentNodes.get(0))) {
//						Set<SBOLAssemblyNode> previousNodes = new HashSet<SBOLAssemblyNode>(assemblyGraph.getPreviousNodes(nextNode));
//						previousNodes.remove(currentNodes.get(0));
//						if (previousNodes.size() == 0 || (seqValidator.isPartialConstructStarted() && !seqValidator.isTerminalConstructValid()) 
//								|| (globalVisitedNodes.containsAll(previousNodes) && !globalVisitedNodes.contains(nextNode))
//								|| (localVisitedNodes.containsAll(previousNodes) && !localVisitedNodes.contains(nextNode))) {
//							List<String> nextNodeTypes = SBOLUtility.loadNodeTypes(nextNode);
//							if (nextNodeTypes.size() > 1)
//								nextNodeTypes = nextNodeTypes.subList(0, 1);
//							if (nextNodeTypes.size() > 0 && 
//									(!seqValidator.validatePartialConstruct(nextNodeTypes, false) 
//									|| !seqValidator.isPartialConstructStarted() 
//									|| (seqValidator.isTerminalConstructValid() 
//											&& !seqValidator.validateTerminalConstruct(nextNodeTypes, false)))) 
//								startNodes.add(nextNode);
//							else if (!localVisitedNodes.contains(nextNode))
//								nextNodes.add(nextNode);
//						}
						if (!localVisitedNodes.contains(nextNode)) {
							boolean nextValid = false;
							if (seqValidator.isPartialConstructStarted() && !seqValidator.isTerminalConstructValid())
								nextValid = true;
							else if (!globalVisitedNodes.contains(nextNode)) {
								Set<AssemblyNode2> previousNodes = new HashSet<AssemblyNode2>(assemblyGraph.getPreviousNodes(nextNode));
								previousNodes.remove(currentNodes.get(0));
								if (previousNodes.size() == 0 || localVisitedNodes.containsAll(previousNodes)
										|| globalVisitedNodes.containsAll(previousNodes))
									nextValid = true;
							}
							if (nextValid) {
								List<String> nextNodeTypes = loadNodeTypes(nextNode);
								if (nextNodeTypes.size() > 2)
									nextNodeTypes = nextNodeTypes.subList(0, 2);
								if (nextNodeTypes.size() > 0 && 
										(!seqValidator.validatePartialConstruct(nextNodeTypes, false) 
										|| !seqValidator.isPartialConstructStarted() 
										|| (seqValidator.isTerminalConstructValid() 
												&& !seqValidator.validateTerminalConstruct(nextNodeTypes, false)))) 
									startNodes.add(nextNode);
								else if (!localVisitedNodes.contains(nextNode))
									nextNodes.add(nextNode);
							}
								
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
								List<AssemblyNode2> copyStartNodes = new LinkedList<AssemblyNode2>(startNodes);
								List<AssemblyNode2> copyCurrentNodes = new LinkedList<AssemblyNode2>(currentNodes);
								copyCurrentNodes.addAll(0, nextNodes);
								List<AssemblyNode2> copyWalkNodes = new LinkedList<AssemblyNode2>(walkNodes);
								List<AssemblyNode2> copyOrderedNodes = new LinkedList<AssemblyNode2>(orderedNodes);
								Set<AssemblyNode2> copyLocalVisitedNodes = new HashSet<AssemblyNode2>(localVisitedNodes);
								Set<AssemblyNode2> copyGlobalVisitedNodes = new HashSet<AssemblyNode2>(globalVisitedNodes);
								seqValidator.savePartialConstructValidator();
								seqValidator.saveTerminalConstructValidator();
								List<AssemblyNode2> branchSolution = orderAssemblyNodes(copyStartNodes, copyCurrentNodes,
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
			List<String> walkNodeTypes = loadNodeTypes(walkNodes);
			List<String> orderedNodeTypes = loadNodeTypes(orderedNodes);
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
			Set<AssemblyNode2> cycleNodes = new HashSet<AssemblyNode2>(assemblyGraph.getNodes());
			cycleNodes.removeAll(globalVisitedNodes);
			Set<String> startNodeTypes = seqValidator.getStartTypes();
			for (AssemblyNode2 cycleNode : cycleNodes) {
				List<String> cycleNodeTypes = loadNodeTypes(cycleNode);
				if (cycleNodeTypes.size() > 1 && startNodeTypes.contains(cycleNodeTypes.get(1))) 
					startNodes.add(cycleNode);
			}
			List<AssemblyNode2> cycleSolution;
			for (AssemblyNode2 startNode : startNodes) {
				List<AssemblyNode2> copyStartNode = new LinkedList<AssemblyNode2>();
				copyStartNode.add(startNode);
				Set<AssemblyNode2> copyGlobalVisitedNodes = new HashSet<AssemblyNode2>(globalVisitedNodes);
				List<AssemblyNode2> copyOrderedNodes = new LinkedList<AssemblyNode2>(orderedNodes);
				seqValidator.savePartialConstructValidator();
				seqValidator.saveTerminalConstructValidator();
				cycleSolution = orderAssemblyNodes(copyStartNode, new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(),
						copyOrderedNodes, new HashSet<AssemblyNode2>(), copyGlobalVisitedNodes);
				if (cycleSolution == null) {
					seqValidator.loadPartialConstructValidator();
					seqValidator.loadTerminalConstructValidator();
				} else
					return cycleSolution;
			}
			startNodes.clear();
			cycleNodes.removeAll(startNodes);
			startNodes.addAll(cycleNodes);
			for (AssemblyNode2 startNode : startNodes) {
				List<AssemblyNode2> copyStartNode = new LinkedList<AssemblyNode2>();
				copyStartNode.add(startNode);
				Set<AssemblyNode2> copyGlobalVisitedNodes = new HashSet<AssemblyNode2>(globalVisitedNodes);
				List<AssemblyNode2> copyOrderedNodes = new LinkedList<AssemblyNode2>(orderedNodes);
				seqValidator.savePartialConstructValidator();
				seqValidator.saveTerminalConstructValidator();
				cycleSolution = orderAssemblyNodes(copyStartNode, new LinkedList<AssemblyNode2>(), new LinkedList<AssemblyNode2>(),
						copyOrderedNodes, new HashSet<AssemblyNode2>(), copyGlobalVisitedNodes);
				if (cycleSolution == null) {
					seqValidator.loadPartialConstructValidator();
					seqValidator.loadTerminalConstructValidator();
				} else
					return cycleSolution;
			}
			return null;
		}
		return orderedNodes;
	}
	
	private List<String> loadAssemblyTypes(ComponentDefinition dnaComp, String strand) {
		List<String> types = new LinkedList<String>();
//		if (dnaComp.getAnnotations().size() == 0) {
		if (dnaComp.getSequenceAnnotations().size() == 0) {
			if (SBOLUtility2.loadSONumber(dnaComp) != null) {
				String soNum = SBOLUtility2.loadSONumber(dnaComp).replace(":", "_");
				if (soNum != null) {
					types.add(strand);
					types.add(soNum);

				}
			}
		} else {
//			List<SequenceAnnotation> annos = SBOLUtility2.orderSequenceAnnotations(dnaComp.getAnnotations());
//			List<SequenceAnnotation> annos = SBOLUtility2.orderSequenceAnnotations(dnaComp.getSequenceAnnotations());
			List<SequenceAnnotation> annos = dnaComp.getSortedSequenceAnnotations(); 
			String prevSubStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
			int minusIndex = 0;
			for (SequenceAnnotation anno : annos) {
				String subStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//				if (anno.getStrand() == null) {
				for (Location location : anno.getLocations()) {
					if(location.isSetOrientation()) {
						if (location.getOrientation().equals(OrientationType.INLINE))
							subStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
						else 
							subStrand = GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
						break;
					}
				}
				List<String> nextTypes;
				if (strand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
					if (subStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND))
					{
//						nextTypes = loadAssemblyTypes(anno.getSubComponent(), 
//								GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
						nextTypes = loadAssemblyTypes(anno.getComponentDefinition(), 
								GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
					}
					else 
					{
//						nextTypes = loadAssemblyTypes(anno.getSubComponent(), 
//								GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND);
						nextTypes = loadAssemblyTypes(anno.getComponentDefinition(), 
								GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND);
					}
				else
				{
//					nextTypes = loadAssemblyTypes(anno.getSubComponent(), subStrand);
					nextTypes = loadAssemblyTypes(anno.getComponentDefinition(), subStrand);
				}
				if (subStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND)) {
					if (!subStrand.equals(prevSubStrand))
						minusIndex = types.size();
					types.addAll(minusIndex, nextTypes);
				} else
					types.addAll(nextTypes);
				prevSubStrand = subStrand;
			}
		}
		return types;
	}
	
	private List<String> loadAssemblyTypes(ComponentDefinition dnaComp) {
		return loadAssemblyTypes(dnaComp, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}

	private List<String> loadAssemblyTypes(List<ComponentDefinition> dnaComps) {
		List<String> types = new LinkedList<String>();
		for (ComponentDefinition dnaComp : dnaComps) {
			types.addAll(loadAssemblyTypes(dnaComp, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND));
		}
		return types;
	}
	
	private List<String> loadNodeTypes(AssemblyNode2 assemblyNode) {
		return loadAssemblyTypes(assemblyNode.getDNAComponents());
	}
	
	private List<String> loadNodeTypes(List<AssemblyNode2> assemblyNodes) {
		List<String> types = new LinkedList<String>();
		for (AssemblyNode2 assemblyNode : assemblyNodes)
			types.addAll(loadNodeTypes(assemblyNode));
		return types;
	}
	
	private static List<URI> loadNodeURIs(AssemblyNode2 assemblyNode) {
		return SBOLUtility2.loadDNAComponentURIs(assemblyNode.getDNAComponents());
	}
	
	private static List<URI> loadNodeURIs(List<AssemblyNode2> assemblyNodes) {
		List<URI> uris = new LinkedList<URI>();
		for (AssemblyNode2 assemblyNode : assemblyNodes)
			uris.addAll(loadNodeURIs(assemblyNode));
		return uris;
	}
	
}
