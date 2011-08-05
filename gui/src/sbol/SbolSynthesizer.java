package sbol;

import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JOptionPane;
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
	
	public void synthesizeDnaComponent(String exportFilePath, String exportLibId) {	
		synthesizerOn = true;
		// Create DNA component
		DnaComponent synthComp = new DnaComponent();
		DnaSequence compSeq = new DnaSequence();
		compSeq.setDnaSequence("");
		synthComp.setDnaSequence(compSeq);
		Library exportLib = SbolUtility.loadRDF(exportFilePath);
		String compId = getUserInput("Enter Display ID:", "Display ID", exportLib);
		String compName = null;
		if (compId != null) {
			synthComp.setDisplayId(compId);
			compName = getUserInput("Enter Name:", "Name", exportLib);
		}
		String compDescription = null;
		if (compName != null) {
			synthComp.setName(compName);
			compDescription = getUserInput("Enter Description:", "Description", exportLib);
		}
		if (compId != null && compName != null && compDescription != null) {
			synthComp.setDescription(compDescription);
			// Assemble DNA component annotations
			String mySeparator = File.separator;
			if (mySeparator.equals("\\"))
				mySeparator = "\\\\";
			String[] splitPath = exportFilePath.split(mySeparator);
			String exportFileId = splitPath[splitPath.length - 1];
			int position = 1;
			for (Promoter p : promoters.values()) {
				String importFeatProperty = p.getProperty(GlobalConstants.SBOL_PROMOTER);
				position = addFeature(position, importFeatProperty, synthComp, exportLib, exportFileId);
				for (SpeciesInterface s : p.getOutputs()) {
					importFeatProperty = s.getProperty(GlobalConstants.SBOL_RBS);
					position = addFeature(position, importFeatProperty, synthComp, exportLib, exportFileId);
					importFeatProperty = s.getProperty(GlobalConstants.SBOL_ORF);
					position = addFeature(position, importFeatProperty, synthComp, exportLib, exportFileId);
				}
				importFeatProperty = p.getProperty(GlobalConstants.SBOL_TERMINATOR);
				position = addFeature(position, importFeatProperty, synthComp, exportLib, exportFileId);
			}
			if (synthesizerOn) {
				// Export DNA component
				exportLib.addComponent(synthComp);
				String rdf = IOTools.toRdfXml(exportLib);
				try {
					FileOutputStream out = new FileOutputStream(exportFilePath);
					out.write(rdf.getBytes());
					out.close();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Error exporting to SBOL file.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private String getUserInput(String message, String inputType, Library exportLib) {
		boolean inputValid;
		String input;
		do {
			inputValid = true;
			input = JOptionPane.showInputDialog(Gui.frame, message, inputType, JOptionPane.PLAIN_MESSAGE);
			if (input == null)
				break;
			else if (inputType.equals("Display ID") && !Utility.isValid(input, Utility.IDstring)) {
				inputValid = false;
				JOptionPane.showMessageDialog(Gui.frame, "Non-alphanumeric ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			} else if (inputType.equals("Display ID") && SbolUtility.idClash(input, exportLib)) {
				inputValid = false;
				JOptionPane.showMessageDialog(Gui.frame, "Collection already contains DNA component with chosen ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			} else if (inputType.equals("Display ID") && input.equals("")) {
				inputValid = false;
				JOptionPane.showMessageDialog(Gui.frame, "Blank ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			}
		} while (!inputValid);
		return input;
	}
	
	private int addFeature(int position, String importFeatProperty, DnaComponent synthComp, Library exportLib, String exportFileId) {
		if (importFeatProperty != null && synthesizerOn) {
			String importLibId = importFeatProperty.split("/")[0] + "/" + importFeatProperty.split("/")[1];
			String importFeatId = importFeatProperty.split("/")[2];
			SequenceFeature importFeat = loadFeature(importFeatId, importLibId);
			if (importFeat != null) {
				int option = checkIdClash(importFeatId, importLibId, exportFileId, exportLib);
				if (option == JOptionPane.YES_OPTION) {
					String renameId = getUserInput("Enter new display ID:", "Display ID", exportLib);
					if (renameId == null)
						synthesizerOn = false;
					else {
						importFeat = kludgyRename(importFeat, renameId);
					}
				} else if (option == JOptionPane.NO_OPTION) {
					for (SequenceFeature sf : exportLib.getFeatures()) {
						if (sf.getDisplayId().equals(importFeatId)) {
							importFeat = sf;
						}
					}
				} else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
					synthesizerOn = false;
				if (synthesizerOn) {
					SbolService factory = new SbolService();
					factory.addSequenceFeatureToLibrary(importFeat, exportLib);
					position = addFeatureToComponent(importFeat, synthComp, position);
					synthComp.getDnaSequence().setDnaSequence(synthComp.getDnaSequence().getDnaSequence() + importFeat.getDnaSequence().getDnaSequence());
				}
			}
		}
		return position;
	}
	
	private SequenceFeature loadFeature(String importFeatId, String importLibId) {
		if (libMap.containsKey(importLibId)) {
			for (SequenceFeature sf : libMap.get(importLibId).getFeatures()) {
				if (sf.getDisplayId().equals(importFeatId)) 
					return sf;
			}
		}
		JOptionPane.showMessageDialog(Gui.frame, importFeatId + " not found in project libraries.", "Warning", JOptionPane.WARNING_MESSAGE);
		return null;
	}
	
	private int addFeatureToComponent(SequenceFeature importFeat, DnaComponent synthComp, int position) {
		if (importFeat.getDnaSequence().getDnaSequence().length() >= 1) {
			SequenceAnnotation annot = new SequenceAnnotation();
			annot.setStart(position);
			position += importFeat.getDnaSequence().getDnaSequence().length() - 1;
			annot.setStop(position);
			annot.setStrand("+");
			annot.addFeature(importFeat);
			synthComp.addAnnotation(annot);
			annot.generateId(synthComp);
			position++;
		}
		return position;
	}
	
	private int checkIdClash(String importFeatId, String importLibId, String exportFileId, Library exportLib)  {
		String exportLibId = exportFileId + "/" + exportLib.getDisplayId();
		if (SbolUtility.idClash(importFeatId, exportLib) && !importLibId.equals(exportLibId)) {
			Object[] options = {"Change ID", "Use Existing", "Cancel"};
			int n = JOptionPane.showOptionDialog(Gui.frame, exportLibId + " already contains " + importFeatId 
					+ ".  Would you like to change display ID for incoming " + importFeatId + " or use existing " + importFeatId + "?", 
					"ID Clash", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			return n;
		} else
			return 3;
	}
	
	private String mungeId(String importFeatProperty) {
		String mungedId = importFeatProperty.replace("/", "_");
		mungedId = mungedId.replace(".rdf", "");
		return mungedId;
	}
	
	private SequenceFeature kludgyRename(SequenceFeature oldFeat, String renameId) {
		SequenceFeature renameFeat = new SequenceFeature();
		renameFeat.setDisplayId(renameId);
		if (oldFeat.getName() != null)
			renameFeat.setName(oldFeat.getName());
		if (oldFeat.getDescription() != null)
			renameFeat.setDescription(oldFeat.getDescription());
		if (oldFeat.getDnaSequence() != null)
			renameFeat.setDnaSequence(oldFeat.getDnaSequence());
		for (URI uri : oldFeat.getTypes())
			renameFeat.addType(uri);
		return renameFeat;
	}
	
//	private void overwriteFeature(SequenceFeature replacement, Library lib) {
//		for (DnaComponent dnac : lib.getComponents()) {
//			String compSeq = "";
//			for (SequenceAnnotation sa : dnac.getAnnotations()) {
//				for (SequenceFeature sf : sa.getFeatures()) {
//					if (sf.getDisplayId().equals(replacement.getDisplayId())) {
//						sa.getFeatures().remove(sf);
//						sa.addFeature(replacement);
//						int seqLength = sf.getDnaSequence().getDnaSequence().length();
//						if (sa.getStop() - sa.getStart() + 1 
//								< seqLength)
//							sa.setStop(sa.getStart + sf.getDnaSequence);
//						sf.setDisplayId(replacement.getDisplayId());
//						if (replacement.getName() != null)
//							sf.setName(replacement.getName());
//						else
//							sf.setName("");
//						if (replacement.getDescription() != null)
//							sf.setDescription(replacement.getDescription());
//						else
//							sf.setDescription("");
//						if (replacement.getDnaSequence() != null 
//								&& replacement.getDnaSequence().getDnaSequence() != null)
//							sf.getDnaSequence().setDnaSequence(replacement.getDnaSequence().getDnaSequence());
//						else
//							sf.getDnaSequence().setDnaSequence("");
//						for (URI type : sf.getTypes())
//							sf.getTypes().remove(type);
//						if (replacement.getTypes() != null) {
//							for (URI type : replacement.getTypes())
//								sf.addType(type);
//						}
//					} 
//					compSeq = compSeq + sf.getDnaSequence().getDnaSequence();
//				}
//			}
//			dnac.getDnaSequence().setDnaSequence(compSeq);
//		}
//	}
	
}
