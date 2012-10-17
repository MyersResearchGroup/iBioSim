package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbml.libsbml.Model;
import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core.util.SBOLDeepEquality;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.network.SynthesisNode;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

public class SBOLSynthesizer {
	
	private BioModel bioModel;
	private LinkedList<URI> modelURIs;
	private LinkedHashMap<String, SynthesisNode> synMap;
	private Set<String> sbolFiles;
	private Set<String> sbolFilePaths;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	private HashMap<String, Resolver<DnaComponent, URI>> fileResolverMap = new HashMap<String, Resolver<DnaComponent, URI>>();
//	private Set<DnaComponent> localComps;
//	private boolean localMatch;
	private String targetFile;
	private String uriAuthority;
	private String time;
//	private URI existingBioSimURI;
	
	public SBOLSynthesizer(BioModel biomodel, LinkedList<URI> modelURIs, LinkedHashMap<String, SynthesisNode> synMap) {
		this.bioModel = biomodel;
		this.modelURIs = modelURIs;
		this.synMap = synMap;
	}
	
	public boolean loadSbolFiles(Set<String> sbolFilePaths) {
		if (sbolFilePaths.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		this.sbolFiles = new HashSet<String>();
		this.sbolFilePaths = sbolFilePaths;
//		localComps = new HashSet<DnaComponent>();

		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		for (String filePath : sbolFilePaths) {
			String sbolFile = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
			this.sbolFiles.add(sbolFile);
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
				Resolver<DnaComponent, URI> compResolver = flattenedDoc.getComponentUriResolver();
				compResolvers.add(compResolver);
				fileResolverMap.put(sbolFile, compResolver);
//				for (SBOLRootObject sbolObj : flattenedDoc.getContents()) {
//					if (sbolObj instanceof DnaComponent)
//						localComps.add((DnaComponent) sbolObj);
//				}
			} else
				return false;
		}
		aggregateCompResolver.setResolvers(compResolvers);
		
		return true;
	}
	
	public DnaComponent exportDnaComponent(String exportFilePath, String saveDirectory) {
		DnaComponent synthComp = saveDnaComponent(saveDirectory, true);
		SBOLDocument sbolDoc = SBOLFactory.createDocument();
		SBOLUtility.addDNAComponent(synthComp, sbolDoc);
		SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
		return synthComp;
	}
	
	// Synthesizes new composite DNA component from graph of annotated model elements, loads/constructs component descriptors and identifiers, 
	// saves component to local SBOL files, and uses component to annotate model itself 
	public DnaComponent saveDnaComponent(String saveDirectory, boolean saveModel) {
		DnaComponent synthComp = null;
		if (loadNodeDNAComponents("save", saveDirectory)) {
			// Synthesize DNA component
			synthComp = synthesizeDnaComponent();
			if (synthComp != null) {
				// Load/construct component descriptors and identifiers
				URI existingBioSimURI = loadSBOLDescriptors(synthComp);
				if (existingBioSimURI.toString().endsWith("iBioSimNull") || existingBioSimURI.toString().endsWith("iBioSim")
						|| existingBioSimURI.toString().endsWith("iBioSimPlaceHolder"))
					constructURIs(synthComp, existingBioSimURI);
				// Save component to local SBOL files
				for (String sbolFile : sbolFiles) {
					if (existingBioSimURI.toString().endsWith("iBioSim") || sbolFile.equals(targetFile)) {
						SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(saveDirectory + File.separator + sbolFile);
						if (existingBioSimURI.toString().endsWith("iBioSim"))
							SBOLUtility.mergeDNAComponent(existingBioSimURI, synthComp, sbolDoc);
						if (sbolFile.equals(targetFile))
							SBOLUtility.addDNAComponent(synthComp, sbolDoc);
						SBOLUtility.writeSBOLDocument(saveDirectory + File.separator + sbolFile, sbolDoc);
					}
				}
				// Use component to annotate model itself
				Model sbmlModel = bioModel.getSBMLDocument().getModel();
				SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), modelURIs);
				AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
				bioModel.setModelSBOLAnnotationFlag(true);
				if (saveModel)
					bioModel.save(saveDirectory + File.separator + sbmlModel.getId() + ".gcm");
				
			}
		}
		return synthComp;
	}
	
	// Loads DNA components for synthesis nodes by resolving URIs and/or running sbolSynthesizers from nodes
	private boolean loadNodeDNAComponents(String subCommand, String saveDirectory) {
		for (SynthesisNode synNode : synMap.values()) {
//			boolean resolveURIs = false;
			LinkedList<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
			LinkedList<URI> sbolURIs = synNode.getSbolURIs();
			SBOLSynthesizer subSynthesizer = synNode.getSynthesizer();
			DnaComponent subSynthComp = null;
			String subSynthURI = "";
			if (subSynthesizer != null) {
				subSynthesizer.loadSbolFiles(sbolFilePaths);
//				if (subCommand.equals("save"))
					subSynthComp = subSynthesizer.saveDnaComponent(saveDirectory, true);
//				else if (subCommand.equals("export"))
//					subSynthComp = subSynthesizer.exportDnaComponent(filePath);
				if (subSynthComp != null) {
//					dnaComps.add(subSynthComp);
//					synNode.setDNAComponents(dnaComps);
					sbolURIs = subSynthesizer.getModelURIs();
					subSynthURI = subSynthComp.getURI().toString();
				} else if (sbolURIs.size() > 0) {
					Object[] options = { "OK", "Cancel" };
					int choice = JOptionPane.showOptionDialog(null, 
							"Failed to synthesize SBOL that annotates elements within submodel.  Proceed with synthesis " +
							"using SBOL that annotates submodel itself?", "Warning", JOptionPane.DEFAULT_OPTION, 
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					if (choice != JOptionPane.OK_OPTION)
						return false;
//					else
//						resolveURIs = true;
				} else {
					JOptionPane.showMessageDialog(Gui.frame, "Failed to synthesize SBOL for submodel", 
							"SBOL Synthesis Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
					
			} 
//			else if (sbolURIs.size() > 0)
//				resolveURIs = true;
			if (sbolURIs.size() > 0) {
				for (URI sbolURI : sbolURIs) {
					if (sbolURI.toString().equals(subSynthURI))
						dnaComps.add(subSynthComp);
					else {
						DnaComponent dnaComp = aggregateCompResolver.resolve(sbolURI);
						if (dnaComp != null)
							dnaComps.add(aggregateCompResolver.resolve(sbolURI));
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + sbolURI +
									" is not found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
				}
				synNode.setDNAComponents(dnaComps);
			}
		}
		return true;
	}
	
	public DnaComponent synthesizeDnaComponent() {	
		// Orders list of subcomponents (to be assembled into composite component) 
		// by walking synthesis nodes
		String regex = Preferences.userRoot().get("biosim.synthesis.regex", "");
		SequenceTypeValidator validator = new SequenceTypeValidator(regex);
		Set<String> startTypes = new HashSet<String>();
		if (validator.getStartTypes().size() > 0)
			startTypes.addAll(validator.getStartTypes());
		else
			startTypes.add("promoter");
		
		LinkedList<DnaComponent> subComps = orderSubComponents(startTypes);
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
			//				types.add(SBOLUtility.uriToSOTypeConverter(subComp.getTypes().iterator().next()));
			types.addAll(getLowestSequenceTypes(subComp));
		}

		if (validator != null && !validator.validateSequenceTypes(types)) {
			Object[] options = { "OK", "Cancel" };
			int choice = JOptionPane.showOptionDialog(null, 
					"Ordering of SBOL DNA components associated to SBML does not match preferred regular expression.  Proceed with synthesis?", 
					"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (choice != JOptionPane.OK_OPTION)
				return null;
		}
		return synthComp;
//		}
	}
	
	// Gets sequence ontology types for DNA components at the lowest level in the hierarchy of components
	private LinkedList<String> getLowestSequenceTypes(DnaComponent comp) {
		LinkedList<String> types = new LinkedList<String>();
		List<SequenceAnnotation> annots = comp.getAnnotations();
		if (annots.size() > 0)
			for (SequenceAnnotation anno : annots)
				types.addAll(getLowestSequenceTypes(anno.getSubComponent()));
		else
			types.add(SBOLUtility.convertURIToSOType(comp.getTypes().iterator().next()));
		return types;
	}
	
	// Recursively walks synthesis node graph and orders associated SBOL DNA components (no preference when graph branches)
	// Starts at synthesis nodes with DNA components of the SO type "promoter"
	// Stops at nodes with other promoters or previously visited nodes
	private LinkedList<DnaComponent> orderSubComponents(Set<String> startTypes) {
		LinkedHashSet<SynthesisNode> startNodes = new LinkedHashSet<SynthesisNode>();
		Set<String> startNodeIDs = new HashSet<String>();
		
		// Determines start nodes and counts total number of nodes with DNA components
		int nodesSBOL = determineStartNodes(startNodes, startNodeIDs, startTypes);

		// Walks and orders subcomponents
		LinkedList<DnaComponent> subComps = new LinkedList<DnaComponent>();
		int nodesSBOLVisited = 0;
		for (SynthesisNode startNode : startNodes) {
			Set<String> locallyVisitedNodeIDs = new HashSet<String>();
			nodesSBOLVisited = nodesSBOLVisited + walkSynthesisNodes(startNode, subComps, startNodeIDs, locallyVisitedNodeIDs);
		}
		
		// Orders leftover subcomponents that did not follow the subcomponents matching the beginning of the regex
		if (nodesSBOLVisited < nodesSBOL) {
			subComps.addAll(orderLeftoverSubComponents(new HashSet(synMap.values()), nodesSBOLVisited, nodesSBOL));
		}
		return subComps;
	}
	
	// Populates sets of start nodes and their IDs (1st URI of start node is for DNA component of SO type specified by filter
	// or for DNA component whose recursively 1st subcomponent is of the correct type)
	// Returns total number of nodes visited that had URIs (start nodes or not)
	private int determineStartNodes(LinkedHashSet<SynthesisNode> startNodes, Set<String> startNodeIDs, Set<String> startTypes) {
		int nodesSBOL = 0;
		for (SynthesisNode synNode : synMap.values()) {
			LinkedList<DnaComponent> dnaComps = synNode.getDNAComponents();
			if (dnaComps.size() > 0) {
				nodesSBOL++;
				DnaComponent startComp = dnaComps.get(0);
				if (checkStartCompType(startComp, startTypes)) {
					synNode.setVisited(true);
					startNodes.add(synNode);
					startNodeIDs.add(synNode.getID());
				}
			}
		}
		return nodesSBOL;
	}
	
	// Recursively checks whether DNA component or its 1st subcomponent has SO type in filter
	private boolean checkStartCompType(DnaComponent startComp, Set<String> startTypes) {
		for (URI uri : startComp.getTypes())
			if (startTypes.contains(SBOLUtility.convertURIToSOType(uri)))
				return true;
		if (startComp.getAnnotations().size() > 0)
			return checkStartCompType(startComp.getAnnotations().get(0).getSubComponent(), startTypes);
		else
			return false;
	}
	
	// Recursive helper method for walking synthesis node graph and loading associated SBOL DNA component URIs
	private int walkSynthesisNodes(SynthesisNode synNode, LinkedList<DnaComponent> subComps, Set<String> startNodeIDs,
			Set<String> locallyVisitedNodeIDs) {
		int nodesSBOLVisited = 0;
		LinkedList<DnaComponent> dnaComps = synNode.getDNAComponents();
		if (dnaComps.size() > 0) {
			nodesSBOLVisited++;
			subComps.addAll(dnaComps);
		}
		for (SynthesisNode nextNode : synNode.getNextNodes())
			if (!startNodeIDs.contains(nextNode.getID()) && !locallyVisitedNodeIDs.contains(nextNode.getID())) {
				nextNode.setVisited(true);
				locallyVisitedNodeIDs.add(nextNode.getID());
				nodesSBOLVisited = nodesSBOLVisited + walkSynthesisNodes(nextNode, subComps, startNodeIDs, locallyVisitedNodeIDs);
			}
		return nodesSBOLVisited;
	}
	
	// Orders leftover subcomponents that did not follow the subcomponents matching the beginning of the regex
	private LinkedList<DnaComponent> orderLeftoverSubComponents(Set<SynthesisNode> synNodes, int nodesSBOLVisited, int nodesSBOL) {
		LinkedList<DnaComponent> leftoverSubComponents = new LinkedList<DnaComponent>();
		Set<SynthesisNode> startNodes = new HashSet<SynthesisNode>();
		Set<String> startNodeIDs = new HashSet<String>();
		Set<SynthesisNode> leftoverNodes = new HashSet<SynthesisNode>();
		Set<String> nextIDs = new HashSet<String>();
		for (SynthesisNode synNode : synNodes)
			if (!synNode.isVisited()) {
				leftoverNodes.add(synNode);
				for (SynthesisNode nextNode : synNode.getNextNodes())
					nextIDs.add(nextNode.getID());
			}
		for (SynthesisNode leftover : leftoverNodes)
			if (!nextIDs.contains(leftover.getID())) {
				startNodes.add(leftover);
				startNodeIDs.add(leftover.getID());
			}
		if (startNodes.size() == 0)
			startNodes.add(leftoverNodes.iterator().next());
		for (SynthesisNode startNode : startNodes) {
			Set<String> locallyVisitedNodeIDs = new HashSet<String>();
			nodesSBOLVisited = nodesSBOLVisited + walkSynthesisNodes(startNode, leftoverSubComponents, startNodeIDs, 
					locallyVisitedNodeIDs);
		}
		if (nodesSBOLVisited < nodesSBOL)
			leftoverSubComponents.addAll(orderLeftoverSubComponents(leftoverNodes, nodesSBOLVisited, nodesSBOL));
		return leftoverSubComponents;
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
//			try {
//				annot.setURI(new URI(uriAuthority + "#anno" + addCount + time + "_iBioSim"));
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
			synthComp.getDnaSequence().setNucleotides(synthComp.getDnaSequence().getNucleotides() + subComp.getDnaSequence().getNucleotides());
		} else {
			JOptionPane.showMessageDialog(Gui.frame, "DNA Component " + subComp.getDisplayId() + " has no DNA sequence.", 
					"Invalid DNA Sequence", JOptionPane.ERROR_MESSAGE);
			return -1;
		}	
		return position;
	}
	
	// Loads SBOL descriptors such as display ID, name, and description for newly synthesized iBioSim composite component 
	// from model or previously synthesized component
	// Also determines target file for saving newly synthesized component and checks for match with previously synthesized 
	// component (latter affects save of new component and the construction of its URIs)
	private URI loadSBOLDescriptors(DnaComponent synthComp) {
		URI existingBioSimURI = null;
		DnaComponent existingBioSimComp = null;
		if (modelURIs.size() > 0) {
			Iterator<URI> uriIterator = modelURIs.iterator();
			do {
				existingBioSimURI = uriIterator.next();
			} while (uriIterator.hasNext() && !existingBioSimURI.toString().endsWith("iBioSim") 
					&& !existingBioSimURI.toString().endsWith("iBioSimPlaceHolder"));
			if (existingBioSimURI.toString().endsWith("iBioSim")) {
				Iterator<String> fileIterator = fileResolverMap.keySet().iterator();
				do { 
					Resolver<DnaComponent, URI> compResolver = fileResolverMap.get(fileIterator.next());
					existingBioSimComp = compResolver.resolve(existingBioSimURI);
				} while (existingBioSimComp == null && fileIterator.hasNext());
			}
		} else
			try {
				existingBioSimURI = new URI(uriAuthority + "#iBioSimNull");
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		String[] descriptors = bioModel.getSBOLDescriptors();
		if (descriptors != null) {
			synthComp.setDisplayId(descriptors[0]);
			synthComp.setName(descriptors[1]);
			synthComp.setDescription(descriptors[2]);
			targetFile = descriptors[3];
		} else if (existingBioSimComp != null) {
			synthComp.setDisplayId(existingBioSimComp.getDisplayId());
			if (existingBioSimComp.getName() != null)
				synthComp.setName(existingBioSimComp.getName());
			if (existingBioSimComp.getDescription() != null)
				synthComp.setDescription(existingBioSimComp.getDescription());
			targetFile = "";
		} else {
			synthComp.setDisplayId(bioModel.getSBMLDocument().getModel().getId());
			targetFile = sbolFiles.iterator().next();
		}
		synthComp.addType(SequenceOntology.type("SO_0000804"));
		if (existingBioSimComp != null) {
			List<SequenceAnnotation> synthAnnos = synthComp.getAnnotations();
			List<SequenceAnnotation> existingAnnos = existingBioSimComp.getAnnotations();
			if (synthAnnos.size() == existingAnnos.size()) {
				for (int i = 0; i < synthAnnos.size(); i++)
					synthAnnos.get(i).setURI(existingAnnos.get(i).getURI());
				synthComp.setURI(existingBioSimURI);
				if (existingBioSimComp.getDnaSequence() != null)
					synthComp.getDnaSequence().setURI(existingBioSimComp.getDnaSequence().getURI());
				if (SBOLDeepEquality.isDeepEqual(synthComp, existingBioSimComp))
					try {
						existingBioSimURI = new URI(existingBioSimURI.toString() + "Identical");
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			} 
		}
		return existingBioSimURI;
	}
	
	// Constructs URIs for newly synthesized component, its DNA sequence, and sequence annotations
	// Also replaces URI of previously synthesized component or placeholder URI among component URIs previously annotating model
	private void constructURIs(DnaComponent synthComp, URI existingBioSimURI) {
		// URI authority and time are set for creating new URIs
		setAuthorityAndTime();
		try {
			synthComp.setURI(new URI(uriAuthority + "#comp" + time + "_iBioSim"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (existingBioSimURI.toString().endsWith("iBioSim") || existingBioSimURI.toString().endsWith("iBioSimPlaceHolder")) {
			int replaceIndex = modelURIs.indexOf(existingBioSimURI);
			modelURIs.remove(replaceIndex);
			modelURIs.add(replaceIndex, synthComp.getURI());
		} else if (existingBioSimURI.toString().endsWith("iBioSimNull"))
			modelURIs.add(synthComp.getURI());
		try {
			synthComp.getDnaSequence().setURI(new URI(uriAuthority + "#seq" + time + "_iBioSim"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		List<SequenceAnnotation> synthAnnos = synthComp.getAnnotations();
		for (int i = 0; i < synthAnnos.size(); i++) {
			try {
				synthAnnos.get(i).setURI(new URI(uriAuthority + "#anno" + i + time + "_iBioSim"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean isSourceIdValid(String sourceID, SBOLDocument targetDoc) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (targetDoc != null) {
			SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(targetDoc);
			if (flattenedDoc.getComponentDisplayIdResolver().resolve(sourceID) != null) {
				JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
	
	private LinkedList<URI> getModelURIs() {
		return modelURIs;
	}
	
	private void setAuthorityAndTime() {
		uriAuthority = Preferences.userRoot().get("biosim.synthesis.uri", "");
		Calendar now = Calendar.getInstance();
		time = "_" + now.get(Calendar.MONTH) + "_" 
				+ now.get(Calendar.DATE) + "_" + now.get(Calendar.YEAR) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" 
				+ now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + "_" + now.get(Calendar.MILLISECOND);
	}
	
}
