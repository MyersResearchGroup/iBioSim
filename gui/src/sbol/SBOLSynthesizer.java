package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.*;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.network.SynthesisNode;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

public class SBOLSynthesizer {
	
	private Collection<SynthesisNode> synNodes;
	private HashSet<String> sbolFiles;
	private HashMap<String, DnaComponent> compMap;
	private HashSet<String> sourceCompURISet;
	private boolean synthesizerOn;
	private String localPath;
	private String time;
	
	public SBOLSynthesizer(Collection<SynthesisNode> synNodes) {
		this.synNodes = synNodes;
	}
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
	public boolean loadSbolFiles(HashSet<String> sbolFiles) {
		this.sbolFiles = new HashSet<String>();
		compMap = new HashMap<String, DnaComponent>();
		for (String filePath : sbolFiles) {
			this.sbolFiles.add(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) 
				for (DnaComponent dnac : SBOLUtility.loadDNAComponents(sbolDoc).values())
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
	
	public DnaComponent synthesizeDnaComponent() {	
		setTime();
		synthesizerOn = true;
		// Create DNA component for synthesis
		DnaComponent synthComp = new DnaComponentImpl();
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
			SequenceTypeValidator validator = new SequenceTypeValidator("(p(rc)+t+)*");
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
				if (localPath == null) 
					labelWithUserInput(synthComp);
				else 
					labelAndLocalSave(synthComp);
				if (synthesizerOn) 
					return synthComp;
			}	
		}
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
			if (option != JOptionPane.YES_OPTION) {
				break;
			}
		} while (!isSourceIdValid(idText.getText()));
		if (option == JOptionPane.YES_OPTION) {
			
			synthComp.setDisplayId(idText.getText());
			synthComp.setName(nameText.getText());
			synthComp.setDescription(descripText.getText());
		} else
			synthesizerOn = false;
	}
	
	private void labelAndLocalSave(DnaComponent synthComp) {
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
		Set<String> localURIs = null;
		String[] options = { "Ok", "Cancel" };
		int option;
		do {
			option = JOptionPane.showOptionDialog(Gui.frame, inputPanel,
					"Save SBOL DNA Component", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option != JOptionPane.YES_OPTION) {
				break;
			}
			String chosenFileId = fileBox.getSelectedItem().toString();
			if (!localFileId.equals(chosenFileId)) {
				localFileId = chosenFileId;
				localDoc = SBOLUtility.loadSBOLFile(localPath + File.separator + localFileId);
				if (localDoc == null) {
					synthesizerOn = false;
					break;
				}
			}
		} while (!isSourceIdValid(idText.getText()));
		if (synthesizerOn && option == JOptionPane.YES_OPTION) {
			synthComp.setDisplayId(idText.getText());
			synthComp.setName(nameText.getText());
			synthComp.setDescription(descripText.getText());
			SBOLUtility.mergeDNAComponent(synthComp, localDoc, SBOLUtility.loadDNAComponents(localDoc));
			SBOLUtility.exportSBOLDocument(localPath + File.separator + localFileId, localDoc);
		} else
			synthesizerOn = false;
	}
	
	// Recursively walks synthesis node graph and loads associated SBOL DNA component URIs (no preference when graph branches)
	// Starts at synthesis nodes containing URIs for SBOL promoters
	// Stops at nodes containing URIs for other promoters or previously visited nodes
	private LinkedList<String> loadSourceCompURIs() {
		Set<String> filter = SBOLUtility.soSynonyms(GlobalConstants.SBOL_PROMOTER);
		Set<SynthesisNode> promoterNodes = new HashSet<SynthesisNode>();
		Set<String> promoterNodeIds = new HashSet<String>();
		
		int nodesSBOL = 0;
		for (SynthesisNode synNode : synNodes) {
			LinkedList<String> sbolURIs = synNode.getSbolURIs();
			if (sbolURIs.size() > 0) {
				nodesSBOL++;
				for (String uri : sbolURIs) {
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
		}
		LinkedList<String> sourceCompURIs = new LinkedList<String>();
		if (synthesizerOn) {
			Set<String> visitedNodeIds;
			int nodesSBOLVisited = 0;
			for (SynthesisNode promoterNode : promoterNodes) {
				visitedNodeIds = new HashSet<String>(promoterNodeIds);
				nodesSBOLVisited = nodesSBOLVisited + loadSourceCompURIsHelper(promoterNode, sourceCompURIs, visitedNodeIds);
			}
			if (nodesSBOLVisited != nodesSBOL) {
				Object[] options = { "OK", "Cancel" };
				int choice = JOptionPane.showOptionDialog(null, 
						"Some SBOL DNA components are not connected to a promoter and will not be included in synthesis.  Proceed with synthesis?", 
						"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (choice != JOptionPane.OK_OPTION)
					synthesizerOn = false;
			}
		}
		return sourceCompURIs;
	}
	
	// Recursive helper method for walking synthesis node graph and loading associated SBOL DNA component URIs
	private int loadSourceCompURIsHelper(SynthesisNode synNode, LinkedList<String> sourceCompURIs, Set<String> visitedNodeIds) {
		int nodesSBOLVisited = 0;
		LinkedList<String> sbolURIs = synNode.getSbolURIs();
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
		if (compMap.containsKey(sourceCompURI))
			sourceComp = compMap.get(sourceCompURI);
		else {
			synthesizerOn = false;
			JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + sourceCompURI + " is not found in project SBOL files.",
					"DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		}
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
