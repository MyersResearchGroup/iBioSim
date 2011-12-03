package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.xml.*;

import biomodel.network.Promoter;
import biomodel.network.SpeciesInterface;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

public class SbolSynthesizer {
	
	private HashMap<String, Promoter> promoters;
	private HashMap<String, org.sbolstandard.core.Collection> fileLibMap;
	private HashSet<String> targetIdSet;
	private HashSet<String> sourceIdSet;
	private CollectionImpl targetLib;
	private boolean synthesizerOn;
	
	public SbolSynthesizer(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}
	
	public boolean loadLibraries(HashSet<String> sbolFiles) {
		fileLibMap = new HashMap<String, org.sbolstandard.core.Collection>();
		for (String filePath : sbolFiles) {
			org.sbolstandard.core.Collection lib = SbolUtility.loadXML(filePath);
			if (lib == null)
				return false;
			String mySeparator = File.separator;
			if (mySeparator.equals("\\"))
				mySeparator = "\\\\";
			fileLibMap.put(filePath.substring(filePath.lastIndexOf(mySeparator) + 1, filePath.length()) + "/" + lib.getDisplayId(), lib);
		}
		if (fileLibMap.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}	
	
	public void saveSbol(String targetPath) {
		Object[] targets = fileLibMap.keySet().toArray();
		synthesizeDnaComponent(targetPath, targets);
	}
	
	public void exportSbol(String targetFilePath) {
		String mySeparator = File.separator;
		if (mySeparator.equals("\\"))
			mySeparator = "\\\\";
		String targetFileId = targetFilePath.substring(targetFilePath.lastIndexOf(mySeparator) + 1, targetFilePath.length());
		String targetPath = targetFilePath.substring(0, targetFilePath.lastIndexOf(mySeparator));
		Object[] targets = new Object[1];
		targets[0] = targetFileId;
		if (!new File(targetFilePath).exists()) {
			CollectionImpl exportLib = new CollectionImpl();
			exportLib.setDisplayId(targetFileId.substring(0, targetFileId.indexOf(".")));
			SbolUtility.exportLibrary(targetFilePath, exportLib);
		}
		synthesizeDnaComponent(targetPath, targets);
	}
	
	private void synthesizeDnaComponent(String targetPath, Object[] targets) {	
		synthesizerOn = true;
		// Get user input
		String[] input = getUserInput(targetPath, targets);
		if (synthesizerOn) {
			// Create DNA component
			String targetFileId = input[0];
			DnaComponent synthComp = new DnaComponentImpl();
			DnaSequence compSeq = new DnaSequenceImpl();
			compSeq.setNucleotides("");
			synthComp.setDnaSequence(compSeq);
			synthComp.setDisplayId(input[1]);
			synthComp.setName(input[2]);
			synthComp.setDescription(input[3]);
			// Assemble component annotations
			LinkedList<String> sourceFeatProperties = getSourceCompProperties();
			if (synthesizerOn) {
				sourceIdSet = new HashSet<String>();
				for (String importFeatProperty : sourceFeatProperties)
					sourceIdSet.add(importFeatProperty.split("/")[2]);
				int position = 1;
				for (String sourceFeatProperty : sourceFeatProperties)
					position = addSubComponent(position, sourceFeatProperty, synthComp, targetFileId);
				if (synthesizerOn) {
					// Export DNA component
					targetLib.addComponent(synthComp);
					SbolUtility.exportLibrary(targetPath + File.separator + targetFileId, targetLib);
				}
			}
		}
	}
	
	private String[] getUserInput(String targetPath, Object[] targets) {
		String[] input = new String[4];
		
		JPanel inputPanel;
		inputPanel = new JPanel(new GridLayout(4,2));
		
		JComboBox libBox = new JComboBox(targets);
		JTextField idText = new JTextField(20);
		JTextField nameText = new JTextField(20);
		JTextField descripText = new JTextField(20);

		inputPanel.add(new JLabel("Save to File/Collection"));
		inputPanel.add(libBox);
		inputPanel.add(new JLabel("Display ID"));
		inputPanel.add(idText);
		inputPanel.add(new JLabel("Name"));
		inputPanel.add(nameText);
		inputPanel.add(new JLabel("Description"));
		inputPanel.add(descripText);

		String targetLibId = "";
		String targetFileId = "";
		for (int i = 0; i < 4; i++)
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
			String userFileId = libBox.getSelectedItem().toString().split("/")[0];
			if (!targetFileId.equals(userFileId)) {
				targetFileId = userFileId;
				targetLib = SbolUtility.loadXML(targetPath + File.separator + targetFileId);
				if (targetLib != null) {
					targetIdSet = new HashSet<String>();
					for (DnaComponent dnac : targetLib.getComponents()) 
						targetIdSet.add(dnac.getDisplayId());
				} else {
					synthesizerOn = false;
					break;
				}
			}
		} while (!isSourceIdValid(idText.getText()));
		if (synthesizerOn && option == JOptionPane.YES_OPTION) {
			input[0] = targetFileId;
			input[1] = idText.getText();
			input[2] = nameText.getText();
			input[3] = descripText.getText();
		} else
			synthesizerOn = false;
		return input;
	}
	
	private LinkedList<String> getSourceCompProperties() {
		LinkedList<String> sourceFeatProperties = new LinkedList<String>();
		for (Promoter p : promoters.values()) {
//			&& p.getOutputs().size() > 0
			if (synthesizerOn ) {
				String sbolPromoter = p.getPromoter();
				if (sbolPromoter != null)
					sourceFeatProperties.add(sbolPromoter);
				else {
					synthesizerOn = false;
					JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL promoter assocation.",
							"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
				}
				for (SpeciesInterface s : p.getOutputs()) {
					if (synthesizerOn) {
						String sbolRbs = s.getRBS();
						if (sbolRbs != null)
							sourceFeatProperties.add(sbolRbs);
						else {
							synthesizerOn = false;
							JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL RBS assocation.",
									"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (synthesizerOn) {
						String sbolOrf = s.getORF();
						if (sbolOrf != null)
							sourceFeatProperties.add(sbolOrf);
						else {
							synthesizerOn = false;
							JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL ORF assocation.",
									"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				if (synthesizerOn) {
					String sbolTerminator = p.getTerminator();
					if (sbolTerminator != null)
						sourceFeatProperties.add(sbolTerminator);
					else {
						synthesizerOn = false;
						JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL terminator assocation.",
								"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		return sourceFeatProperties;
	}
	
	private int addSubComponent(int position, String sourceFeatProperty, DnaComponent synthComp, String targetFileId) {
		if (synthesizerOn) {
			String sourceFileId = sourceFeatProperty.split("/")[0];
			String sourceLibId = sourceFeatProperty.split("/")[1];
			String sourceCompId = sourceFeatProperty.split("/")[2];
			DnaComponent sourceComp = loadComponent(sourceCompId, sourceLibId, sourceFileId);
			if (synthesizerOn) {
				String targetLibId = targetLib.getDisplayId();
				if (targetIdSet.contains(sourceCompId) && (!sourceFileId.equals(targetFileId) || !sourceLibId.equals(targetLibId))) 
					sourceComp = resolveIdClash(sourceComp);
				if (synthesizerOn) {
					if (!targetIdSet.contains(sourceCompId))
						targetLib.addComponent(sourceComp);
					position = annotate(sourceComp, synthComp, position);
					if (synthesizerOn)
						synthComp.getDnaSequence().setNucleotides(synthComp.getDnaSequence().getNucleotides() + sourceComp.getDnaSequence().getNucleotides());
				}
			}
		}
		return position;
	}
	
	private DnaComponent loadComponent(String sourceCompId, String sourceLibId, String sourceFileId) {
		HashSet<String> fileSet = new HashSet<String>();
		HashSet<String> libSet = new HashSet<String>();
		for (String s : fileLibMap.keySet()) {
			fileSet.add(s.split("/")[0]);
			libSet.add(s.split("/")[1]);
		}
		if (fileLibMap.containsKey(sourceFileId + "/" + sourceLibId)) {
			for (DnaComponent dnac : fileLibMap.get(sourceFileId + "/" + sourceLibId).getComponents()) {
				if (dnac.getDisplayId().equals(sourceCompId)) 
					return dnac;
			}
		}
		synthesizerOn = false;
		if (!fileSet.contains(sourceFileId))
			JOptionPane.showMessageDialog(Gui.frame, "File " + sourceFileId + " is not found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		else if (!libSet.contains(sourceLibId))
			JOptionPane.showMessageDialog(Gui.frame, "Collection " + sourceLibId + " is not found in file "+ sourceFileId + ".", 
					"Collection Not Found", JOptionPane.ERROR_MESSAGE);
		else
			JOptionPane.showMessageDialog(Gui.frame, "DNA component " + sourceCompId + " is not found in collection " + sourceLibId
			        + " from file " + sourceFileId + ".", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return null;
	}
	
	private DnaComponent resolveIdClash(DnaComponent sourceComp) {
		int option;
		String sourceCompId = sourceComp.getDisplayId();
		boolean overwriteMode = false;
		if (isAnnotation(sourceCompId)) {
			String[] options = {"Change ID", "Use Existing", "Cancel"};
			option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() + " already contains DNA component " 
					+ sourceCompId + ".  Would you like to change display ID for incoming " + sourceCompId + " or use existing " + sourceCompId + "?", 
					"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		} else {
			overwriteMode = true;
			String[] options = {"Change ID", "Overwrite", "Cancel"};
			option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() + " already contains DNA component " 
					+ sourceCompId + ".  Would you like to change display ID for incoming " + sourceCompId + " or overwrite existing " 
					+ sourceCompId + "?", 
					"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		}
		if (option == 0) {
			sourceComp = renameId(sourceComp);
		} else if (option == 1) {
			if (overwriteMode)
				deleteComponent(sourceCompId);
			else {
				for (DnaComponent dnac : targetLib.getComponents()) {
					if (dnac.getDisplayId().equals(sourceCompId)) {
						sourceComp = dnac;
					}
				}
			}
		} else 
			synthesizerOn = false;
		return sourceComp;
	}
	
	private DnaComponent renameId(DnaComponent sourceComp) {
		String renameId;
		do {
			renameId = JOptionPane.showInputDialog(Gui.frame, "Enter new display ID:", "Display ID", JOptionPane.PLAIN_MESSAGE);
			if (renameId == null)
				break;
			if (!sourceComp.getDisplayId().equals(renameId) && sourceIdSet.contains(renameId))
				JOptionPane.showMessageDialog(Gui.frame, "Collection would contain another DNA component with the chosen ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
		} while ((!sourceComp.getDisplayId().equals(renameId) && sourceIdSet.contains(renameId)) || !isSourceIdValid(renameId));
		if (renameId != null) {
			sourceIdSet.remove(sourceComp.getDisplayId());
			sourceIdSet.add(renameId);
			DnaComponent renameComp = new DnaComponentImpl();
			renameComp.setDisplayId(renameId);
			if (sourceComp.getName() != null)
				renameComp.setName(sourceComp.getName());
			if (sourceComp.getDescription() != null)
				renameComp.setDescription(sourceComp.getDescription());
			if (sourceComp.getDnaSequence() != null)
				renameComp.setDnaSequence(sourceComp.getDnaSequence());
			for (URI uri : sourceComp.getTypes())
				renameComp.addType(uri);
			return renameComp;
		} else {
			synthesizerOn = false;
			return sourceComp;
		}
	}
	
	private int annotate(DnaComponent sourceComp, DnaComponent synthComp, int position) {
		if (sourceComp.getDnaSequence() != null && sourceComp.getDnaSequence().getNucleotides() != null 
				&& sourceComp.getDnaSequence().getNucleotides().length() >= 1) {
			SequenceAnnotation annot = new SequenceAnnotationImpl();
			annot.setBioStart(position);
			position += sourceComp.getDnaSequence().getNucleotides().length() - 1;
			annot.setBioEnd(position);
			annot.setStrand("+");
			annot.setSubComponent(sourceComp);
			synthComp.addAnnotation(annot);
//			annot.generateId(synthComp);
			position++;
		} else {
			synthesizerOn = false;
			JOptionPane.showMessageDialog(Gui.frame, "DNA Component " + sourceComp.getDisplayId() + " has no DNA sequence.", 
					"Invalid DNA Sequence", JOptionPane.ERROR_MESSAGE);
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
		} else if (targetIdSet.contains(sourceId))
			return checkOverwrite(sourceId);
		return true;
	}
	
	private boolean checkOverwrite(String sourceId) {
		if (isAnnotation(sourceId)) {
			JOptionPane.showMessageDialog(Gui.frame, "Collection " + targetLib.getDisplayId() 
					+ " already contains DNA component " + sourceId + ".", "ID Clash", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			String[] options = { "Ok", "Cancel" };
			int option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() 
					+ " already contains DNA component " + sourceId + ".  Would you like to overwrite?",
					"ID Clash", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (option == JOptionPane.YES_OPTION) {
				deleteComponent(sourceId);
				return true;
			} else
				return false;
		}
	}
	
	private boolean isAnnotation(String compId) {
		for (DnaComponent dnac : targetLib.getComponents())
			for (SequenceAnnotation sa : dnac.getAnnotations())
				if (sa.getSubComponent().getDisplayId().equals(compId))
					return true;
		return false;
	}
	
	private void deleteComponent(String compId) {
		CollectionImpl editedLib = new CollectionImpl();
		editedLib.setDisplayId(targetLib.getDisplayId());
		if (targetLib.getName() != null)
			editedLib.setName(targetLib.getName());
		if (targetLib.getDescription() != null)
			editedLib.setDescription(targetLib.getDescription());
		for (DnaComponent dnac : targetLib.getComponents())
			if (!dnac.getDisplayId().equals(compId))
				editedLib.addComponent(dnac);
		targetLib = editedLib;
	}
	
//	private String mungeId(String importFeatProperty) {
//		String mungedId = importFeatProperty.replace("/", "_");
//		mungedId = mungedId.replace(".sbol", "");
//		return mungedId;
//	}
	
}
