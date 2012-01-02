package sbol;


import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
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
	private HashSet<String> sbolFiles;
	private HashMap<String, DnaComponent> compMap;
	private HashSet<String> targetURISet;
	private HashSet<String> sourceCompURISet;
	private CollectionImpl targetLib;
	private boolean synthesizerOn;
	private String time;
	
	public SbolSynthesizer(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}
	
	public boolean loadSbolFiles(HashSet<String> sbolFiles) {
		this.sbolFiles = new HashSet<String>();
		compMap = new HashMap<String, DnaComponent>();
		for (String filePath : sbolFiles) {
			this.sbolFiles.add(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
			org.sbolstandard.core.Collection lib = SbolUtility.loadXML(filePath);
			if (lib != null) 
				for (DnaComponent dnac : lib.getComponents())
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
		setTime();
		Object[] targets = sbolFiles.toArray();
		synthesizeDnaComponent(targetPath, targets);
	}
	
	public void exportSbol(String targetFilePath) {
		setTime();
		String targetFileId = targetFilePath.substring(targetFilePath.lastIndexOf(File.separator) + 1);
		String targetPath = targetFilePath.substring(0, targetFilePath.lastIndexOf(File.separator));
		Object[] targets = new Object[1];
		targets[0] = targetFileId;
		if (!new File(targetFilePath).exists()) {
			CollectionImpl exportLib = new CollectionImpl();
			exportLib.setDisplayId(targetFileId.substring(0, targetFileId.indexOf(".")));
			try {
				exportLib.setURI(new URI("http://www.async.ece.utah.edu#col" + time));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
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
			// Set component type
			try {
				synthComp.addType(new URI("http://sbols.org/sbol.owl#engineered_region"));
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
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
				for (String sourceCompURI : sourceCompURIs) 
					if (synthesizerOn) {
						addCount++;
						position = addSubComponent(position, sourceCompURI, synthComp, addCount);
					}
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

//		String targetLibId = "";
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
			String userFileId = fileBox.getSelectedItem().toString();
			if (!targetFileId.equals(userFileId)) {
				targetFileId = userFileId;
				targetLib = SbolUtility.loadXML(targetPath + File.separator + targetFileId);
				if (targetLib != null) {
					targetURISet = new HashSet<String>();
					for (DnaComponent dnac : targetLib.getComponents()) 
						targetURISet.add(dnac.getURI().toString());
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
	
	private LinkedList<String> loadSourceCompURIs() {
		LinkedList<String> sourceCompURIs = new LinkedList<String>();
		for (Promoter p : promoters.values()) {
			if (synthesizerOn) {
				String sbolPromoter = p.getPromoter();
				if (sbolPromoter != null && !sbolPromoter.equals(""))
					sourceCompURIs.add(sbolPromoter);
				else {
					synthesizerOn = false;
					JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL promoter assocation.",
							"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
				}
				for (SpeciesInterface s : p.getOutputs()) {
					if (synthesizerOn) {
						String sbolRbs = s.getRBS();
						if (sbolRbs != null && !sbolRbs.equals(""))
							sourceCompURIs.add(sbolRbs);
						else {
							synthesizerOn = false;
							JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL RBS assocation.",
									"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (synthesizerOn) {
						String sbolOrf = s.getORF();
						if (sbolOrf != null && !sbolOrf.equals(""))
							sourceCompURIs.add(sbolOrf);
						else {
							synthesizerOn = false;
							JOptionPane.showMessageDialog(Gui.frame, "Species " + s.getId() + " has no SBOL ORF assocation.",
									"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				if (synthesizerOn) {
					String sbolTerminator = p.getTerminator();
					if (sbolTerminator != null && !sbolTerminator.equals(""))
						sourceCompURIs.add(sbolTerminator);
					else {
						synthesizerOn = false;
						JOptionPane.showMessageDialog(Gui.frame, "Promoter " + p.getId() + " has no SBOL terminator assocation.",
								"Invalid GCM to SBOL Association", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		return sourceCompURIs;
	}
	
	private int addSubComponent(int position, String sourceCompURI, DnaComponent synthComp, int addCount) {
		DnaComponent sourceComp = new DnaComponentImpl();
		if (compMap.containsKey(sourceCompURI))
			sourceComp = compMap.get(sourceCompURI);
		else {
			synthesizerOn = false;
			JOptionPane.showMessageDialog(Gui.frame, "Component with URI " + sourceCompURI + " is not found in project SBOL files.",
					"DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		}
		if (synthesizerOn) {
			// Adds source component and all its subcomponents to target collection
			addSubComponentHelper(sourceComp);
			// Annotates newly synthesized DNA component with source component
			if (sourceComp.getDnaSequence() != null && sourceComp.getDnaSequence().getNucleotides() != null 
					&& sourceComp.getDnaSequence().getNucleotides().length() >= 1) {
				SequenceAnnotation annot = new SequenceAnnotationImpl();
				annot.setBioStart(position);
				position += sourceComp.getDnaSequence().getNucleotides().length() - 1;
				annot.setBioEnd(position);
				annot.setStrand("+");
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
	
	// Recursively adds DNA component and annotated subcomponents to the target collection
	private void addSubComponentHelper(DnaComponent dnac) {
		if (!targetURISet.contains(dnac.getURI().toString()))
			targetLib.addComponent(dnac);
		if (dnac.getAnnotations() != null) 
			for (SequenceAnnotation sa : dnac.getAnnotations()) 
				if (sa.getSubComponent() != null)
					addSubComponentHelper(sa.getSubComponent());
			
	}
	
//	private DnaComponent loadComponent(String sourceCompId, String sourceLibId, String sourceFileId) {
//		HashSet<String> fileSet = new HashSet<String>();
//		HashSet<String> libSet = new HashSet<String>();
//		for (String s : fileLibMap.keySet()) {
//			fileSet.add(s.split("/")[0]);
//			libSet.add(s.split("/")[1]);
//		}
//		if (fileLibMap.containsKey(sourceFileId + "/" + sourceLibId)) {
//			for (DnaComponent dnac : fileLibMap.get(sourceFileId + "/" + sourceLibId).getComponents()) {
//				if (dnac.getDisplayId().equals(sourceCompId)) 
//					return dnac;
//			}
//		}
//		synthesizerOn = false;
//		if (!fileSet.contains(sourceFileId))
//			JOptionPane.showMessageDialog(Gui.frame, "File " + sourceFileId + " is not found in project.", 
//					"File Not Found", JOptionPane.ERROR_MESSAGE);
//		else if (!libSet.contains(sourceLibId))
//			JOptionPane.showMessageDialog(Gui.frame, "Collection " + sourceLibId + " is not found in file "+ sourceFileId + ".", 
//					"Collection Not Found", JOptionPane.ERROR_MESSAGE);
//		else
//			JOptionPane.showMessageDialog(Gui.frame, "DNA component " + sourceCompId + " is not found in collection " + sourceLibId
//			        + " from file " + sourceFileId + ".", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
//		return null;
//	}
	
//	private DnaComponent resolveIdClash(DnaComponent sourceComp) {
//		int option;
//		String sourceCompId = sourceComp.getDisplayId();
//		boolean overwriteMode = false;
//		if (isAnnotation(sourceCompId)) {
//			String[] options = {"Change ID", "Use Existing", "Cancel"};
//			option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() + " already contains DNA component " 
//					+ sourceCompId + ".  Would you like to change display ID for incoming " + sourceCompId + " or use existing " + sourceCompId + "?", 
//					"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//		} else {
//			overwriteMode = true;
//			String[] options = {"Change ID", "Overwrite", "Cancel"};
//			option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() + " already contains DNA component " 
//					+ sourceCompId + ".  Would you like to change display ID for incoming " + sourceCompId + " or overwrite existing " 
//					+ sourceCompId + "?", 
//					"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//		}
//		if (option == 0) {
//			sourceComp = renameId(sourceComp);
//		} else if (option == 1) {
//			if (overwriteMode)
//				deleteComponent(sourceCompId);
//			else {
//				for (DnaComponent dnac : targetLib.getComponents()) {
//					if (dnac.getDisplayId().equals(sourceCompId)) {
//						sourceComp = dnac;
//					}
//				}
//			}
//		} else 
//			synthesizerOn = false;
//		return sourceComp;
//	}
	
//	private DnaComponent renameId(DnaComponent sourceComp) {
//		String renameId;
//		do {
//			renameId = JOptionPane.showInputDialog(Gui.frame, "Enter new display ID:", "Display ID", JOptionPane.PLAIN_MESSAGE);
//			if (renameId == null)
//				break;
//			if (!sourceComp.getDisplayId().equals(renameId) && sourceCompURISet.contains(renameId))
//				JOptionPane.showMessageDialog(Gui.frame, "Collection would contain another DNA component with the chosen ID.",
//						"Invalid ID", JOptionPane.ERROR_MESSAGE);
//		} while ((!sourceComp.getDisplayId().equals(renameId) && sourceCompURISet.contains(renameId)) || !isSourceIdValid(renameId));
//		if (renameId != null) {
//			sourceCompURISet.remove(sourceComp.getDisplayId());
//			sourceCompURISet.add(renameId);
//			DnaComponent renameComp = new DnaComponentImpl();
//			renameComp.setDisplayId(renameId);
//			if (sourceComp.getName() != null)
//				renameComp.setName(sourceComp.getName());
//			if (sourceComp.getDescription() != null)
//				renameComp.setDescription(sourceComp.getDescription());
//			if (sourceComp.getDnaSequence() != null)
//				renameComp.setDnaSequence(sourceComp.getDnaSequence());
//			for (URI uri : sourceComp.getTypes())
//				renameComp.addType(uri);
//			return renameComp;
//		} else {
//			synthesizerOn = false;
//			return sourceComp;
//		}
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
	
//	private boolean checkOverwrite(String sourceId) {
//		if (isAnnotation(sourceId)) {
//			JOptionPane.showMessageDialog(Gui.frame, "Collection " + targetLib.getDisplayId() 
//					+ " already contains DNA component " + sourceId + ".", "ID Clash", JOptionPane.ERROR_MESSAGE);
//			return false;
//		} else {
//			String[] options = { "Ok", "Cancel" };
//			int option = JOptionPane.showOptionDialog(Gui.frame, "Collection " + targetLib.getDisplayId() 
//					+ " already contains DNA component " + sourceId + ".  Would you like to overwrite?",
//					"ID Clash", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//			if (option == JOptionPane.YES_OPTION) {
//				deleteComponent(sourceId);
//				return true;
//			} else
//				return false;
//		}
//	}
	
//	private boolean isAnnotation(String compId) {
//		for (DnaComponent dnac : targetLib.getComponents())
//			for (SequenceAnnotation sa : dnac.getAnnotations())
//				if (sa.getSubComponent().getDisplayId().equals(compId))
//					return true;
//		return false;
//	}
//	
//	private void deleteComponent(String compId) {
//		CollectionImpl editedLib = new CollectionImpl();
//		editedLib.setDisplayId(targetLib.getDisplayId());
//		if (targetLib.getName() != null)
//			editedLib.setName(targetLib.getName());
//		if (targetLib.getDescription() != null)
//			editedLib.setDescription(targetLib.getDescription());
//		for (DnaComponent dnac : targetLib.getComponents())
//			if (!dnac.getDisplayId().equals(compId))
//				editedLib.addComponent(dnac);
//		targetLib = editedLib;
//	}
	
}
