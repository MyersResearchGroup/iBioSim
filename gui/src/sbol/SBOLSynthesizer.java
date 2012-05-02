package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;
import org.sbolstandard.core.util.SequenceOntology;

//import biomodel.network.Promoter;
//import biomodel.network.SpeciesInterface;
import biomodel.network.SynthesisNode;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

public class SBOLSynthesizer {
	
	private Collection<SynthesisNode> synNodes;
	private HashSet<String> sbolFiles;
	private HashMap<String, DnaComponent> compMap;
	private HashSet<String> targetURISet;
	private HashSet<String> sourceCompURISet;
	private SBOLDocument targetDoc;
	private boolean synthesizerOn;
	private String time;
	
	public SBOLSynthesizer(Collection<SynthesisNode> synNodes) {
		this.synNodes = synNodes;
	}
	
	public boolean loadSbolFiles(HashSet<String> sbolFiles) {
		this.sbolFiles = new HashSet<String>();
		compMap = new HashMap<String, DnaComponent>();
		for (String filePath : sbolFiles) {
			this.sbolFiles.add(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) 
				for (DnaComponent dnac : SBOLUtility.loadDNAComponents(sbolDoc))
					if (dnac.getDisplayId() != null)
						compMap.put(dnac.getURI().toString(), dnac);
			else
				return false;
		}
		if (sbolFiles.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}	
	
	public void saveSbol(String targetPath) {
//		Object[] targets = sbolFiles.toArray();
//		synthesizeDnaComponent(targetPath, targets);
	}
	
//	public void exportSbol() {
		
//		if (!new File(targetFilePath).exists()) {
//			SBOLDocument sbolExportDoc = SBOLFactory.createDocument();
//			SBOLUtility.exportSBOLDocument(targetFilePath, sbolExportDoc);
//		}
//		synthesizeDnaComponent(targetPath, targets);
//	}
	
	public DnaComponent synthesizeDnaComponent() {	
		setTime();
		synthesizerOn = true;
		// Get user input
		String[] input = getUserInput();
		if (synthesizerOn) {
			// Create DNA component
//			String targetFileId = input[0];
			DnaComponent synthComp = new DnaComponentImpl();
			DnaSequence compSeq = new DnaSequenceImpl();
			compSeq.setNucleotides("");
			synthComp.setDnaSequence(compSeq);
			synthComp.setDisplayId(input[0]);
			synthComp.setName(input[1]);
			synthComp.setDescription(input[2]);
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
			// Assemble component annotations
			LinkedList<String> sourceCompURIs = loadSourceCompURIs();
			this.sourceCompURISet = new HashSet<String>();
			if (synthesizerOn) {
				for (String sourceCompURI : sourceCompURIs)
					sourceCompURISet.add(sourceCompURI);
				int position = 1;
				int addCount = 0;
				LinkedList<String> types = new LinkedList<String>();
				for (String sourceCompURI : sourceCompURIs) 
					if (synthesizerOn) {
						addCount++;
						position = addSubComponent(position, sourceCompURI, synthComp, addCount, types);
					}
				SequenceTypeValidator validator = new SequenceTypeValidator("(p(rc)+t+)+");
				if (!validator.validateSequenceTypes(types)) {
					Object[] options = { "OK", "CANCEL" };
					int choice = JOptionPane.showOptionDialog(null, 
							"Ordering of SBOL DNA components associated to SBML does not match preferred grammar.  Proceed with synthesis?", 
							"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					if (choice != JOptionPane.OK_OPTION)
						synthesizerOn = false;
				}
				if (synthesizerOn) 
					// Export DNA component
					return synthComp;
//					targetDoc.addContent(synthComp);
//					SBOLUtility.exportSBOLDocument(targetPath + File.separator + targetFileId, targetDoc);
				
			}
		}
		return null;
	}
	
//	private String[] getUserInput(String targetPath, Object[] targets) {
	private String[] getUserInput() {
		String[] input = new String[3];
		
		JPanel inputPanel;
		inputPanel = new JPanel(new GridLayout(3,2));
		
//		JComboBox fileBox = new JComboBox(targets);
		JTextField idText = new JTextField(20);
		JTextField nameText = new JTextField(20);
		JTextField descripText = new JTextField(20);

//		inputPanel.add(new JLabel("Save to File"));
//		inputPanel.add(fileBox);
		inputPanel.add(new JLabel("Display ID"));
		inputPanel.add(idText);
		inputPanel.add(new JLabel("Name"));
		inputPanel.add(nameText);
		inputPanel.add(new JLabel("Description"));
		inputPanel.add(descripText);

//		String targetFileId = "";
		for (int i = 0; i < 3; i++)
			input[i] = "";
		String[] options = { "Ok", "Cancel" };
		int option;
		do {
			option = JOptionPane.showOptionDialog(Gui.frame, inputPanel,
					"Save DNA Component", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option != JOptionPane.YES_OPTION) {
				break;
			}
//			String userFileId = fileBox.getSelectedItem().toString();
//			if (!targetFileId.equals(userFileId)) {
//				targetFileId = userFileId;
//				targetDoc = SBOLUtility.loadSBOLFile(targetPath + File.separator + targetFileId);
//				if (targetDoc != null) {
//					targetURISet = new HashSet<String>();
//					for (DnaComponent dnac : SBOLUtility.loadDNAComponents(targetDoc)) 
//						targetURISet.add(dnac.getURI().toString());
//				} 
////				else
////					targetDoc = SBOLFactory.createDocument();
//				else {
//					synthesizerOn = false;
//					break;
//				}
//			}
		} while (!isSourceIdValid(idText.getText()));
		if (synthesizerOn && option == JOptionPane.YES_OPTION) {
//			input[0] = targetFileId;
			input[0] = idText.getText();
			input[1] = nameText.getText();
			input[2] = descripText.getText();
		} else
			synthesizerOn = false;
		return input;
	}
	
	// Recursively walks synthesis node graph and loads associated SBOL DNA component URIs (no preference when graph branches)
	// Starts at synthesis nodes containing URIs for SBOL promoters
	// Stops at nodes containing URIs for other promoters or previously visited nodes
	private LinkedList<String> loadSourceCompURIs() {
		Set<String> filter = SBOLUtility.soSynonyms(GlobalConstants.SBOL_PROMOTER);
		Set<SynthesisNode> promoterNodes = new HashSet<SynthesisNode>();
		Set<String> promoterNodeIds = new HashSet<String>();
		
		for (SynthesisNode synNode : synNodes) {
			for (String uri : synNode.getSbolURIs()) {
				if (synthesizerOn) 
					if (compMap.containsKey(uri)) {
						DnaComponent sourceComp = compMap.get(uri);
						for (URI typeURI : sourceComp.getTypes())
							if (filter.contains(typeURI.toString())) {
								promoterNodes.add(synNode);
								promoterNodeIds.add(synNode.getId());
							}
					} else if (uri != null) {
						synthesizerOn = false;
						JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + uri +
								" is not found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
					}
			}
		}
		LinkedList<String> sourceCompURIs = new LinkedList<String>();
		Set<String> visitedNodeIds;
		for (SynthesisNode promoterNode : promoterNodes) {
			visitedNodeIds = new HashSet<String>(promoterNodeIds);
			loadSourceCompURIsHelper(promoterNode, sourceCompURIs, visitedNodeIds);
		}
		return sourceCompURIs;
	}
	
	// Recursive helper method for walking synthesis node graph and loading associated SBOL DNA component URIs
	private void loadSourceCompURIsHelper(SynthesisNode synNode, LinkedList<String> sourceCompURIs, Set<String> visitedNodeIds) {
		sourceCompURIs.addAll(synNode.getSbolURIs());
		for (SynthesisNode nextNode : synNode.getNextNodes())
			if (!visitedNodeIds.contains(nextNode.getId())) {
				visitedNodeIds.add(nextNode.getId());
				loadSourceCompURIsHelper(nextNode, sourceCompURIs, visitedNodeIds);
			}
	}
	
	private int addSubComponent(int position, String sourceCompURI, DnaComponent synthComp, int addCount, LinkedList<String> types) {
		DnaComponent sourceComp = new DnaComponentImpl();
		if (compMap.containsKey(sourceCompURI))
			sourceComp = compMap.get(sourceCompURI);
		else {
			synthesizerOn = false;
			JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + sourceCompURI + " is not found in project SBOL files.",
					"DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		}
		if (synthesizerOn) {
			// Adds source component and all its subcomponents to target document
//			addSubComponentHelper(sourceComp);
			// Annotates newly synthesized DNA component with source component
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
	
	// Recursively adds DNA component and annotated subcomponents to the target document
//	private void addSubComponentHelper(DnaComponent dnac) {
//		if (!targetURISet.contains(dnac.getURI().toString()))
//			targetDoc.addContent(dnac);
//		if (dnac.getAnnotations() != null) 
//			for (SequenceAnnotation sa : dnac.getAnnotations()) 
//				if (sa.getSubComponent() != null)
//					addSubComponentHelper(sa.getSubComponent());
//			
//	}
	
	private boolean isSourceIdValid(String sourceId) {
		if (sourceId.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!Utility.isValid(sourceId, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
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
