package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.html.HTMLDocument.Iterator;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.network.SynthesisNode;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

public class SBOLSynthesizer {
	
	private LinkedHashMap<String, SynthesisNode> synMap;
	private HashSet<String> sbolFiles;
	private HashSet<String> sbolFilePaths;
	private HashMap<String, DnaComponent> compMap;
	private boolean synthesizerOn;
	private String localMatchURI;
	private String localPath;
	private String time;
	
	public SBOLSynthesizer(LinkedHashMap<String, SynthesisNode> synMap) {
		this.synMap = synMap;
	}
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
	public boolean loadSbolFiles(HashSet<String> sbolFilePaths) {
		this.sbolFiles = new HashSet<String>();
		this.sbolFilePaths = sbolFilePaths;
		compMap = new HashMap<String, DnaComponent>();
		for (String filePath : sbolFilePaths) {
			this.sbolFiles.add(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) 
				compMap = SBOLUtility.loadDNAComponents(sbolDoc);
			else
				return false;
		}
		if (sbolFilePaths.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}	
	
	public DnaComponent synthesizeDnaComponent() {	
		setTime();
		synthesizerOn = true;
		// Assemble component annotations
		LinkedList<String> sourceCompURIs = loadSourceCompURIs();
		// Create DNA component for synthesis
		DnaComponent synthComp = new DnaComponentImpl();
		if (localMatchURI != null) {
			try {
				synthComp.setURI(new URI(localMatchURI));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			DnaSequence compSeq = new DnaSequenceImpl();
			compSeq.setNucleotides("");
			synthComp.setDnaSequence(compSeq);
			// Set component type
			synthComp.addType(SequenceOntology.type("SO_0000804"));
			// Set component URI
			try {
				synthComp.setURI(new URI("http://www.async.ece.utah.edu#comp" + time));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			// Set component sequence URI
			try {
				compSeq.setURI(new URI("http://www.async.ece.utah.edu#seq" + time));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (synthesizerOn) {
			int position = 1;
			int addCount = 0;
			LinkedList<String> types = new LinkedList<String>();
			for (String sourceCompURI : sourceCompURIs) 
				if (synthesizerOn) {
					addCount++;
					position = addSubComponent(position, sourceCompURI, synthComp, addCount, types);
				}
			SequenceTypeValidator validator = new SequenceTypeValidator("((p(rc)+t+)*)|(e*)");
			if (!validator.validateSequenceTypes(types)) {
				Object[] options = { "OK", "Cancel" };
				int choice = JOptionPane.showOptionDialog(null, 
						"Ordering of SBOL DNA components associated to SBML does not match preferred regular expression.  Proceed with synthesis?", 
						"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (choice != JOptionPane.OK_OPTION)
					synthesizerOn = false;
			}
			if (synthesizerOn) {
				// Label synthesized DNA component with user input and save locally if local file path set
				if (localPath != null) 
					labelAndSaveLocally(synthComp);
				else 
					labelWithUserInput(synthComp);
				if (synthesizerOn) 
					return synthComp;
			}	
		}
		if (localMatchURI != null)
			return synthComp;
		else
			return null;
	}
	
	private void labelWithUserInput(DnaComponent synthComp) {
		JPanel inputPanel;
		inputPanel = new JPanel(new GridLayout(3,2));
		
		JTextField idText = new JTextField(20);
		JTextField nameText = new JTextField(20);
		JTextField descripText = new JTextField(20);

		inputPanel.add(new JLabel("Display ID"));
		inputPanel.add(idText);
		inputPanel.add(new JLabel("Name"));
		inputPanel.add(nameText);
		inputPanel.add(new JLabel("Description"));
		inputPanel.add(descripText);

		String[] options = { "Ok", "Cancel" };
		int option;
		do {
			option = JOptionPane.showOptionDialog(Gui.frame, inputPanel,
					"Save DNA Component", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option != JOptionPane.YES_OPTION)
				synthesizerOn = false;
		} while (synthesizerOn && !isSourceIdValid(idText.getText(), new HashSet<String>()));
		if (option == JOptionPane.YES_OPTION) {
			
			synthComp.setDisplayId(idText.getText());
			synthComp.setName(nameText.getText());
			synthComp.setDescription(descripText.getText());
		} else
			synthesizerOn = false;
	}
	
	private void labelAndSaveLocally(DnaComponent synthComp) {
		Object[] targets = sbolFiles.toArray();
		
		JPanel inputPanel;
		inputPanel = new JPanel(new GridLayout(4,2));
		
		JComboBox fileBox = new JComboBox(targets);
		JTextField idText = new JTextField(20);
		JTextField nameText = new JTextField(20);
		JTextField descripText = new JTextField(20);

		inputPanel.add(new JLabel("Save to File"));
		inputPanel.add(fileBox);
		inputPanel.add(new JLabel("Display ID"));
		inputPanel.add(idText);
		inputPanel.add(new JLabel("Name"));
		inputPanel.add(nameText);
		inputPanel.add(new JLabel("Description"));
		inputPanel.add(descripText);

		String localFileId = "";
		SBOLDocument localDoc = null;
		String[] options = { "Ok", "Cancel" };
		int option;
		Set<String> localIDSet = new HashSet<String>();
		do {
			option = JOptionPane.showOptionDialog(Gui.frame, inputPanel,
					"Save SBOL DNA Component", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option != JOptionPane.YES_OPTION) 
				synthesizerOn = false;
			String chosenFileId = fileBox.getSelectedItem().toString();
			if (!localFileId.equals(chosenFileId)) {
				localFileId = chosenFileId;
				localDoc = SBOLUtility.loadSBOLFile(localPath + File.separator + localFileId);
				localIDSet = new HashSet<String>();
				for (DnaComponent dnac : SBOLUtility.loadDNAComponents(localDoc).values())
					localIDSet.add(dnac.getDisplayId());
			}
		} while (synthesizerOn && (localDoc == null || !isSourceIdValid(idText.getText(), localIDSet)));
		if (synthesizerOn) {
			synthComp.setDisplayId(idText.getText());
			synthComp.setName(nameText.getText());
			synthComp.setDescription(descripText.getText());
			SBOLUtility.mergeDNAComponent(synthComp, localDoc, SBOLUtility.loadDNAComponents(localDoc));
			SBOLUtility.exportSBOLDocument(localPath + File.separator + localFileId, localDoc);
		} 
	}
	
	// Recursively walks synthesis node graph and loads associated SBOL DNA component URIs (no preference when graph branches)
	// Starts at synthesis nodes containing URIs for SBOL promoters
	// Stops at nodes containing URIs for other promoters or previously visited nodes
	private LinkedList<String> loadSourceCompURIs() {
		Set<String> filter = SBOLUtility.soSynonyms(GlobalConstants.SBOL_PROMOTER);
		// TO DO add engineered region to soSynonyms method or eliminate method
		filter.add(SequenceOntology.type("SO_0000804").toString());
		LinkedHashSet<SynthesisNode> startNodes = new LinkedHashSet<SynthesisNode>();
		Set<String> startNodeIds = new HashSet<String>();
		
		int nodesSBOL = 0;
		for (SynthesisNode synNode : synMap.values()) {
			LinkedList<String> sbolURIs = synNode.getSbolURIs();
			if (sbolURIs.size() > 0) {
				nodesSBOL++;
				for (String uri : sbolURIs) {
					if (synthesizerOn) 
						if (compMap.containsKey(uri)) {
							DnaComponent sourceComp = compMap.get(uri);
							for (URI typeURI : sourceComp.getTypes())
								if (filter.contains(typeURI.toString())) {
									startNodes.add(synNode);
									startNodeIds.add(synNode.getId());
								}
//							if (checkStartCompType(sourceComp, filter)) {
//								startNodes.add(synNode);
//								startNodeIds.add(synNode.getId());
//							}
						} else {
							synthesizerOn = false;
							JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + uri +
									" is not found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
						}
				}
			}
		}
		LinkedList<String> sourceCompURIs = new LinkedList<String>();
		if (synthesizerOn) {
			Set<String> visitedNodeIds;
			int nodesSBOLVisited = 0;
			for (SynthesisNode promoterNode : startNodes) {
				visitedNodeIds = new HashSet<String>(startNodeIds);
				nodesSBOLVisited = nodesSBOLVisited + loadSourceCompURIsHelper(promoterNode, sourceCompURIs, visitedNodeIds);
			}
			if (nodesSBOLVisited != nodesSBOL) {
				Object[] options = { "OK", "Cancel" };
				int choice = JOptionPane.showOptionDialog(null, 
						"Some SBOL DNA components are not connected to a promoter and will not be included in synthesis.  Proceed with synthesis?", 
						"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (choice != JOptionPane.OK_OPTION) {
					synthesizerOn = false;
					return null;
				}
			}
			// Check if DNA components to be assembled already exist as composite component in local SBOL files
			//If so, turn off synthesizer and mark local composite component for return
			if (localPath != null)
				for (String uri : compMap.keySet()) {
					LinkedList<String> subCompURIs = new LinkedList<String>();
					for (SequenceAnnotation sa : compMap.get(uri).getAnnotations())
						subCompURIs.add(sa.getSubComponent().getURI().toString());
					if (subCompURIs.equals(sourceCompURIs)) {
						localMatchURI = uri;
						synthesizerOn = false;
						return null;
					}
				}
				
		}
		return sourceCompURIs;
	}
	
//	private boolean checkStartCompType(DnaComponent comp, Set<String> filter) {
//		for (URI typeURI : comp.getTypes())
//			if (filter.contains(typeURI.toString()))
//				return true;
//		if (comp.getAnnotations().size() > 0)
//			return checkStartCompType(comp.getAnnotations().get(0).getSubComponent(), filter);
//		else
//			return false;
//	}
	
	// Recursive helper method for walking synthesis node graph and loading associated SBOL DNA component URIs
	private int loadSourceCompURIsHelper(SynthesisNode synNode, LinkedList<String> sourceCompURIs, Set<String> visitedNodeIds) {
		int nodesSBOLVisited = 0;
		SBOLSynthesizer subSynthesizer = synNode.getSynthesizer();
		LinkedList<String> sbolURIs = synNode.getSbolURIs();
		if (subSynthesizer != null) {
			subSynthesizer.loadSbolFiles(sbolFilePaths);
			if (localPath != null)
				subSynthesizer.setLocalPath(localPath);
			DnaComponent synthComp = subSynthesizer.synthesizeDnaComponent();
			if (synthComp != null) {
				sbolURIs.clear();
				sbolURIs.add(synthComp.getURI().toString());
			}
		}
		if (sbolURIs.size() > 0) {
			nodesSBOLVisited++;
			sourceCompURIs.addAll(sbolURIs);
		}
		for (SynthesisNode nextNode : synNode.getNextNodes())
			if (!visitedNodeIds.contains(nextNode.getId())) {
				visitedNodeIds.add(nextNode.getId());
				nodesSBOLVisited = nodesSBOLVisited + loadSourceCompURIsHelper(nextNode, sourceCompURIs, visitedNodeIds);
			}
		return nodesSBOLVisited;
	}
	
	private int addSubComponent(int position, String sourceCompURI, DnaComponent synthComp, int addCount, LinkedList<String> types) {
		DnaComponent sourceComp = new DnaComponentImpl();
//		if (compMap.containsKey(sourceCompURI))
			sourceComp = compMap.get(sourceCompURI);
//		else {
//			synthesizerOn = false;
//			JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + sourceCompURI + " is not found in project SBOL files.",
//					"DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
//		}
		if (synthesizerOn) {
			types.add(SBOLUtility.uriToSOTypeConverter(sourceComp.getTypes().iterator().next()));
			if (sourceComp.getDnaSequence() != null && sourceComp.getDnaSequence().getNucleotides() != null 
					&& sourceComp.getDnaSequence().getNucleotides().length() >= 1) {
				SequenceAnnotation annot = new SequenceAnnotationImpl();
				annot.setBioStart(position);
				position += sourceComp.getDnaSequence().getNucleotides().length() - 1;
				annot.setBioEnd(position);
				annot.setStrand(StrandType.POSITIVE);
				annot.setSubComponent(sourceComp);
				synthComp.addAnnotation(annot);
				position++;
				try {
					annot.setURI(new URI("http://www.async.ece.utah.edu#anno" + addCount + time));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} else {
				synthesizerOn = false;
				JOptionPane.showMessageDialog(Gui.frame, "DNA Component " + sourceComp.getDisplayId() + " has no DNA sequence.", 
						"Invalid DNA Sequence", JOptionPane.ERROR_MESSAGE);
			}	
			if (synthesizerOn)
				synthComp.getDnaSequence().setNucleotides(synthComp.getDnaSequence().getNucleotides() + sourceComp.getDnaSequence().getNucleotides());
		}
		return position;
	}
	
	private boolean isSourceIdValid(String sourceID, Set<String> targetIDSet) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (targetIDSet.contains(sourceID)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void setTime() {
		Calendar now = Calendar.getInstance();
		time = "_" + now.get(Calendar.MONTH) + "_" 
				+ now.get(Calendar.DATE) + "_" + now.get(Calendar.YEAR) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" 
				+ now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + "_" + now.get(Calendar.MILLISECOND);
	}
	
}
