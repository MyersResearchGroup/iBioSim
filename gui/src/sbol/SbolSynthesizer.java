package sbol;

import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.libSBOLj.DnaComponent;
import org.sbolstandard.libSBOLj.DnaSequence;
import org.sbolstandard.libSBOLj.IOTools;
import org.sbolstandard.libSBOLj.Library;
import org.sbolstandard.libSBOLj.SbolService;
import org.sbolstandard.libSBOLj.SequenceAnnotation;
import org.sbolstandard.libSBOLj.SequenceFeature;

public class SbolSynthesizer {
	
	private HashMap<String, Promoter> promoters;
	private HashMap<String, Library> libMap;
	private HashSet<String> targetIdSet;
	private HashSet<String> sourceIdSet;
	private Library targetLib;
	private boolean synthesizerOn;
	
	public SbolSynthesizer(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}
	
	public boolean loadLibraries(HashSet<String> sbolFiles) {
		libMap = new HashMap<String, Library>();
		for (String filePath : sbolFiles) {
			Library lib = SbolUtility.loadRDF(filePath);
			String mySeparator = File.separator;
			if (mySeparator.equals("\\"))
				mySeparator = "\\\\";
			String[] splitPath = filePath.split(mySeparator);
			libMap.put(splitPath[splitPath.length - 1] + "/" + lib.getDisplayId(), lib);
		}
		if (libMap.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL collections in project.", 
					"No Collections", JOptionPane.ERROR_MESSAGE);
			return false;
		} else
			return true;
	}	
	
	public void synthesizeDnaComponent(String targetPath) {	
		synthesizerOn = true;
		// Get user input
		Object[] targets;
		if (targetPath.endsWith(".rdf")) {
			String mySeparator = File.separator;
			if (mySeparator.equals("\\"))
				mySeparator = "\\\\";
			String[] splitPath = targetPath.split(mySeparator);
			targets = new Object[1];
			targets[0] = splitPath[splitPath.length - 1];
			targetPath = targetPath.substring(0, targetPath.lastIndexOf(mySeparator));
		} else 
			targets = libMap.keySet().toArray();
		String[] input = getUserInput(targetPath, targets);
		if (synthesizerOn) {
			// Create DNA component
			String targetFileId = input[0];
			DnaComponent synthComp = new DnaComponent();
			DnaSequence compSeq = new DnaSequence();
			compSeq.setDnaSequence("");
			synthComp.setDnaSequence(compSeq);
			synthComp.setDisplayId(input[1]);
			synthComp.setName(input[2]);
			synthComp.setDescription(input[3]);
			// Assemble component annotations
			LinkedHashSet<String> sourceFeatProperties = getSourceFeatProperties();
			if (synthesizerOn) {
				sourceIdSet = new HashSet<String>();
				for (String importFeatProperty : sourceFeatProperties)
					sourceIdSet.add(importFeatProperty.split("/")[2]);
				int position = 1;
				for (String sourceFeatProperty : sourceFeatProperties)
					position = addFeature(position, sourceFeatProperty, synthComp, targetFileId);
				if (synthesizerOn) {
					// Export DNA component
					targetLib.addComponent(synthComp);
					String rdf = IOTools.toRdfXml(targetLib);
					try {
						FileOutputStream out = new FileOutputStream(targetPath + File.separator + targetFileId);
						out.write(rdf.getBytes());
						out.close();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(Gui.frame, "Error exporting to SBOL file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
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
				targetLib = SbolUtility.loadRDF(targetPath + File.separator + targetFileId);
				targetIdSet = new HashSet<String>();
				for (DnaComponent dnac : targetLib.getComponents()) 
					targetIdSet.add(dnac.getDisplayId());
				for (SequenceFeature sf : targetLib.getFeatures()) 
					targetIdSet.add(sf.getDisplayId());
			}
		} while (!isSourceIdValid(idText.getText()));
		if (option == JOptionPane.YES_OPTION) {
			input[0] = targetFileId;
			input[1] = idText.getText();
			input[2] = nameText.getText();
			input[3] = descripText.getText();
		} else
			synthesizerOn = false;
		return input;
	}
	
	private LinkedHashSet<String> getSourceFeatProperties() {
		LinkedHashSet<String> sourceFeatProperties = new LinkedHashSet<String>();
		for (Promoter p : promoters.values()) {
			if (synthesizerOn) {
				String sbolPromoter = p.getProperty(GlobalConstants.SBOL_PROMOTER);
				if (sbolPromoter != null)
					sourceFeatProperties.add(sbolPromoter);
				else {
					synthesizerOn = false;
					JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL promoter assocation.",
							"Missing SBOL Association", JOptionPane.ERROR_MESSAGE);
				}
			}
			for (SpeciesInterface s : p.getOutputs()) {
				if (synthesizerOn) {
					String sbolRbs = s.getProperty(GlobalConstants.SBOL_RBS);
					if (sbolRbs != null)
						sourceFeatProperties.add(sbolRbs);
					else {
						synthesizerOn = false;
						JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL RBS assocation.",
								"Missing SBOL Association", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (synthesizerOn) {
					String sbolOrf = s.getProperty(GlobalConstants.SBOL_ORF);
					if (sbolOrf != null)
						sourceFeatProperties.add(sbolOrf);
					else {
						synthesizerOn = false;
						JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL ORF assocation.",
								"Missing SBOL Association", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if (synthesizerOn) {
				String sbolTerminator = p.getProperty(GlobalConstants.SBOL_TERMINATOR);
				if (sbolTerminator != null)
					sourceFeatProperties.add(sbolTerminator);
				else {
					synthesizerOn = false;
					JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL terminator assocation.",
							"Missing SBOL Association", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return sourceFeatProperties;
	}
	
	private int addFeature(int position, String sourceFeatProperty, DnaComponent synthComp, String targetFileId) {
		if (synthesizerOn) {
			String sourceLibId = sourceFeatProperty.split("/")[0] + "/" + sourceFeatProperty.split("/")[1];
			String sourceFeatId = sourceFeatProperty.split("/")[2];
			SequenceFeature sourceFeat = loadFeature(sourceFeatId, sourceLibId);
			if (sourceFeat != null) {
				String targetLibId = targetFileId + "/" + targetLib.getDisplayId();
				int option = 99;
				if (targetIdSet.contains(sourceFeatId) && !sourceLibId.equals(targetLibId)) {
					Object[] options = {"Change ID", "Use Existing", "Cancel"};
					option = JOptionPane.showOptionDialog(Gui.frame, targetLibId + " already contains " + sourceFeatId 
							+ ".  Would you like to change display ID for incoming " + sourceFeatId + " or use existing " + sourceFeatId + "?", 
							"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				}
				if (option == JOptionPane.YES_OPTION) {
					sourceFeat = renameId(sourceFeat);
				} else if (option == JOptionPane.NO_OPTION) {
					for (SequenceFeature sf : targetLib.getFeatures()) {
						if (sf.getDisplayId().equals(sourceFeatId)) {
							sourceFeat = sf;
						}
					}
				} else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
					synthesizerOn = false;
				if (synthesizerOn) {
					SbolService factory = new SbolService();
					factory.addSequenceFeatureToLibrary(sourceFeat, targetLib);
					position = addFeatureToComponent(sourceFeat, synthComp, position);
					synthComp.getDnaSequence().setDnaSequence(synthComp.getDnaSequence().getDnaSequence() + sourceFeat.getDnaSequence().getDnaSequence());
				}
			}
		}
		return position;
	}
	
	private SequenceFeature loadFeature(String sourceFeatId, String sourceLibId) {
		if (libMap.containsKey(sourceLibId)) {
			for (SequenceFeature sf : libMap.get(sourceLibId).getFeatures()) {
				if (sf.getDisplayId().equals(sourceFeatId)) 
					return sf;
			}
		}
		synthesizerOn = false;
		JOptionPane.showMessageDialog(Gui.frame, sourceFeatId + " not found in " + sourceLibId + ".", 
				"Component Not Found", JOptionPane.ERROR_MESSAGE);
		return null;
	}
	
	private SequenceFeature renameId(SequenceFeature sourceFeat) {
		String renameId;
		do {
			renameId = JOptionPane.showInputDialog(Gui.frame, "Enter new display ID:", "Display ID", JOptionPane.PLAIN_MESSAGE);
			if (renameId == null)
				break;
			if (sourceIdSet.contains(renameId))
				JOptionPane.showMessageDialog(Gui.frame, "Collection would contain another DNA component with the chosen ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
		} while (!isSourceIdValid(renameId) || sourceIdSet.contains(renameId));
		if (renameId != null) {
			SequenceFeature renameFeat = new SequenceFeature();
			renameFeat.setDisplayId(renameId);
			if (sourceFeat.getName() != null)
				renameFeat.setName(sourceFeat.getName());
			if (sourceFeat.getDescription() != null)
				renameFeat.setDescription(sourceFeat.getDescription());
			if (sourceFeat.getDnaSequence() != null)
				renameFeat.setDnaSequence(sourceFeat.getDnaSequence());
			for (URI uri : sourceFeat.getTypes())
				renameFeat.addType(uri);
			return renameFeat;
		} else {
			synthesizerOn = false;
			return sourceFeat;
		}
	}
	
	private int addFeatureToComponent(SequenceFeature sourceFeat, DnaComponent synthComp, int position) {
		if (sourceFeat.getDnaSequence().getDnaSequence().length() >= 1) {
			SequenceAnnotation annot = new SequenceAnnotation();
			annot.setStart(position);
			position += sourceFeat.getDnaSequence().getDnaSequence().length() - 1;
			annot.setStop(position);
			annot.setStrand("+");
			annot.addFeature(sourceFeat);
			synthComp.addAnnotation(annot);
			annot.generateId(synthComp);
			position++;
		}
		return position;
	}
	
	private boolean isSourceIdValid(String sourceId) {
		if (sourceId.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Blank ID.",
					"Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!Utility.isValid(sourceId, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Non-alphanumeric ID.",
					"Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (targetIdSet.contains(sourceId)) {
			JOptionPane.showMessageDialog(Gui.frame, "Collection already contains DNA component with chosen ID.",
					"Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} 
		return true;
	}
	
	private String mungeId(String importFeatProperty) {
		String mungedId = importFeatProperty.replace("/", "_");
		mungedId = mungedId.replace(".rdf", "");
		return mungedId;
	}
	
}
