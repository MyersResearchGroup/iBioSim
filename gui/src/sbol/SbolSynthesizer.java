package sbol;

import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

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
	
	
	public SbolSynthesizer(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}
	
	public void synthesizeDnaComponent(HashSet<String> sbolFiles, String exportFilePath, String exportLibId) {	
		// Create DNA component
		DnaComponent comp = new DnaComponent();
		Library exportLib = SbolUtility.loadRDF(exportFilePath);
		String compId = getUserInput("Enter Display ID:", "Display ID", exportLib);
		String compName = null;
		if (compId != null) {
			comp.setDisplayId(compId);
			compName = getUserInput("Enter Name:", "Name", exportLib);
		}
		String compDescription = null;
		if (compName != null) {
			comp.setName(compName);
			compDescription = getUserInput("Enter Description:", "Description", exportLib);
		}
		if (compId != null && compName != null && compDescription != null) {
			comp.setDescription(compDescription);
			// Load libraries
			HashMap<String, Library> libMap = new HashMap<String, Library>();
			for (String filePath : sbolFiles) {
				Library lib = SbolUtility.loadRDF(filePath);
				String[] splitPath = filePath.split(File.separator);
				libMap.put(splitPath[splitPath.length - 1] + "/" + lib.getDisplayId(), lib);
			}	
			exportLib.addComponent(comp);
			// Assemble DNA component annotations
			int position = 1;
			String sequence = "";
			for (Promoter p : promoters.values()) {
				SequenceFeature feat;
				if (p.getProperty(GlobalConstants.SBOL_PROMOTER) != null) {
					feat = loadFeature(p.getProperty(GlobalConstants.SBOL_PROMOTER), libMap);
					if (checkOverwrite(p.getProperty(GlobalConstants.SBOL_PROMOTER), exportFilePath, exportLib))
						exportLib = removeFeature(feat.getDisplayId(), exportLib);
					position = addFeature(feat, comp, exportLib, position);
					sequence = sequence + feat.getDnaSequence().getDnaSequence();
				}
				for (SpeciesInterface s : p.getOutputs()) {
					if (s.getProperty(GlobalConstants.SBOL_RBS) != null) {
						feat = loadFeature(s.getProperty(GlobalConstants.SBOL_RBS), libMap);
						if (checkOverwrite(s.getProperty(GlobalConstants.SBOL_RBS), exportFilePath, exportLib))
							exportLib = removeFeature(feat.getDisplayId(), exportLib);
						position = addFeature(feat, comp, exportLib, position);
						sequence = sequence + feat.getDnaSequence().getDnaSequence();
					}
					if (s.getProperty(GlobalConstants.SBOL_ORF) != null) {
						feat = loadFeature(s.getProperty(GlobalConstants.SBOL_ORF), libMap);
						if (checkOverwrite(s.getProperty(GlobalConstants.SBOL_ORF), exportFilePath, exportLib))
							exportLib = removeFeature(feat.getDisplayId(), exportLib);
						position = addFeature(feat, comp, exportLib, position);
						sequence = sequence + feat.getDnaSequence().getDnaSequence();
					}
				}
				if (p.getProperty(GlobalConstants.SBOL_TERMINATOR) != null) {
					feat = loadFeature(p.getProperty(GlobalConstants.SBOL_TERMINATOR), libMap);
					if (checkOverwrite(p.getProperty(GlobalConstants.SBOL_TERMINATOR), exportFilePath, exportLib))
						exportLib = removeFeature(feat.getDisplayId(), exportLib);
					position = addFeature(feat, comp, exportLib, position);
					sequence = sequence + feat.getDnaSequence().getDnaSequence();
				}
			}
			DnaSequence compSeq = new DnaSequence();
			compSeq.setDnaSequence(sequence);
			comp.setDnaSequence(compSeq);
			// Export DNA component
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
	
	private SequenceFeature loadFeature(String featProperty, HashMap<String, Library> libMap) {
		String libId = featProperty.split("/")[0] + "/" + featProperty.split("/")[1];
		String featId = featProperty.split("/")[2];
		if (libMap.containsKey(libId)) {
			for (SequenceFeature sf : libMap.get(libId).getFeatures()) {
				if (sf.getDisplayId().equals(featId))
					return sf;
			}
		}
		JOptionPane.showMessageDialog(Gui.frame, featId + " not found in project libraries.", "Warning", JOptionPane.WARNING_MESSAGE);
		SequenceFeature emptyFeat = new SequenceFeature();
		emptyFeat.setDisplayId(featId);
		DnaSequence emptySeq = new DnaSequence();
		emptySeq.setDnaSequence("");
		emptyFeat.setDnaSequence(emptySeq);
		return emptyFeat;
	}
	
	private int addFeature(SequenceFeature feat, DnaComponent comp, Library exportLib, int position) {
		if (feat.getDnaSequence().getDnaSequence().length() >= 1) {
			SequenceAnnotation annot = new SequenceAnnotation();
			annot.setStart(position);
			position += feat.getDnaSequence().getDnaSequence().length() - 1;
			annot.setStop(position);
			annot.setStrand("+");
			annot.addFeature(feat);
			comp.addAnnotation(annot);
			annot.generateId(comp);
			position++;
			SbolService factory = new SbolService();
			factory.addSequenceFeatureToLibrary(feat, exportLib);
		}
		return position;
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
				JOptionPane.showMessageDialog(Gui.frame, "Library already contains DNA component with chosen ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			} else if (inputType.equals("Display ID") && input.equals("")) {
				inputValid = false;
				JOptionPane.showMessageDialog(Gui.frame, "Blank ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			}
		} while (!inputValid);
		return input;
	}
	
	private boolean checkOverwrite(String featProperty, String exportFilePath, Library exportLib)  {
		String libId = featProperty.split("/")[0] + "/" + featProperty.split("/")[1];
		String[] splitPath = exportFilePath.split(File.separator);
		String exportLibId = splitPath[splitPath.length - 1] + "/" + exportLib.getDisplayId();
		String featId = featProperty.split("/")[2];
		if (SbolUtility.idClash(featId, exportLib) && !libId.equals(exportLibId)) {
			int option = JOptionPane.showConfirmDialog(Gui.frame, libId + " already contains " + featId + ". Would you like to overwrite?", "ID Clash", 
					JOptionPane.YES_NO_OPTION);
			return (option == JOptionPane.YES_OPTION);
		} else
			return false;
	}
	
	private Library removeFeature(String featId, Library lib) {
		Library editedLib = new Library();
		editedLib.setDisplayId(lib.getDisplayId());
		editedLib.setName(lib.getName());
		editedLib.setDescription(lib.getDescription());
		for (DnaComponent dnac : lib.getComponents()) {
			boolean target = false;
			for (SequenceAnnotation sa : dnac.getAnnotations()) {
				for (SequenceFeature sf : sa.getFeatures()) {
					if (sf.getDisplayId().equals(featId))
						target = true;
				}
			}
			if (!target)
				editedLib.addComponent(dnac);
			else {
				DnaComponent editedComp = new DnaComponent();
				editedComp.setDisplayId(dnac.getDisplayId());
				editedComp.setName(dnac.getName());
				editedComp.setDescription(dnac.getDescription());
				editedComp.setDnaSequence(dnac.getDnaSequence());
				for (SequenceAnnotation sa : dnac.getAnnotations()) {
					editedComp.addAnnotation(sa);
					for (SequenceFeature sf : sa.getFeatures()) {
						if (!sf.getDisplayId().equals(featId))
							sa.addFeature(sf);
					}
				}
			}
		}
		for (SequenceFeature sf : lib.getFeatures()) {
			if (!sf.getDisplayId().equals(featId)) {
				SbolService factory = new SbolService();
				factory.addSequenceFeatureToLibrary(sf, editedLib);
			}
		}
		return editedLib;
	}
	
	
}
