package sbol;

import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;

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
	
	public void synthesizeDnaComponent(HashSet<String> filePaths, String exportPath) {	
		DnaComponent comp = new DnaComponent();
		// Name DNA component
		Library exportLib = loadRDF(exportPath);
		boolean dupe = false;
		String compId = "";
		do {
			compId = JOptionPane.showInputDialog(Gui.frame, "Enter DNA Component ID:",
					"Display ID", JOptionPane.PLAIN_MESSAGE);
			dupe = duplicateCheck(compId, exportLib);
			if (dupe) {
				JOptionPane.showMessageDialog(Gui.frame, "Library already contains DNA component with chosen ID.",
						"Invalid ID", JOptionPane.ERROR_MESSAGE);
			}
		} while (dupe);
		comp.setDisplayId(compId);
		// Load libraries
		HashMap<String, Library> libMap = new HashMap<String, Library>();
		for (String fp : filePaths) {
			Library lib = loadRDF(fp);
			libMap.put(lib.getDisplayId(), lib);
		}	
		// Assemble DNA component for export
		int position = 1;
		String sequence = "";
		for (Promoter p : promoters.values()) {
			SequenceFeature feat;
			if (p.getProperty(GlobalConstants.SBOL_PROMOTER) != null) {
				feat = loadFeature(p.getProperty(GlobalConstants.SBOL_PROMOTER), libMap);
				position = addFeature(feat, comp, position);
				sequence = sequence + feat.getDnaSequence().getDnaSequence();
			}
			for (SpeciesInterface s : p.getOutputs()) {
				if (s.getProperty(GlobalConstants.SBOL_RBS) != null) {
					feat = loadFeature(s.getProperty(GlobalConstants.SBOL_RBS), libMap);
					position = addFeature(feat, comp, position);
					sequence = sequence + feat.getDnaSequence().getDnaSequence();
				}
				if (s.getProperty(GlobalConstants.SBOL_ORF) != null) {
					feat = loadFeature(s.getProperty(GlobalConstants.SBOL_ORF), libMap);
					position = addFeature(feat, comp, position);
					sequence = sequence + feat.getDnaSequence().getDnaSequence();
				}
			}
			if (p.getProperty(GlobalConstants.SBOL_TERMINATOR) != null) {
				feat = loadFeature(p.getProperty(GlobalConstants.SBOL_TERMINATOR), libMap);
				position = addFeature(feat, comp, position);
				sequence = sequence + feat.getDnaSequence().getDnaSequence();
			}
		}
		DnaSequence compSeq = new DnaSequence();
		compSeq.setDnaSequence(sequence);
		comp.setDnaSequence(compSeq);
		// Export DNA component
		exportLib.addComponent(comp);
		String rdf = IOTools.toRdfXml(exportLib);
		try {
			FileOutputStream out = new FileOutputStream(exportPath);
			out.write(rdf.getBytes());
			out.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error exporting to SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private Library loadRDF(String filePath) {
		String rdfString = "";
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				rdfString = rdfString.concat(token) + "\n";
			}
			scanIn.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error opening SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		SbolService factory = IOTools.fromRdfXml(rdfString);
		Library lib = factory.getLibrary();
		return lib;
	}
	
	private SequenceFeature loadFeature(String sbolIds, HashMap<String, Library> libMap) {
		String libId = sbolIds.split("/")[0];
		String featId = sbolIds.split("/")[1];
		if (libMap.containsKey(libId)) {
			for (SequenceFeature sf : libMap.get(libId).getFeatures()) {
				if (sf.getDisplayId().equals(featId))
					return sf;
			}
		}
		SequenceFeature emptyFeat = new SequenceFeature();
		emptyFeat.setDisplayId(featId);
		DnaSequence emptySeq = new DnaSequence();
		emptySeq.setDnaSequence("");
		emptyFeat.setDnaSequence(emptySeq);
		return emptyFeat;
	}
	
	private int addFeature(SequenceFeature feat, DnaComponent comp, int position) {
		if (feat.getDnaSequence().getDnaSequence().length() >=1 ) {
			SequenceAnnotation annot = new SequenceAnnotation();
			annot.setStart(position);
			position += feat.getDnaSequence().getDnaSequence().length() - 1;
			annot.setStop(position);
			annot.setStrand("+");
			annot.addFeature(feat);
			comp.addAnnotation(annot);
			annot.generateId(comp);
			position++;
		}
		return position;
	}
	
	private boolean duplicateCheck(String compId, Library lib) {
		for (DnaComponent dnac : lib.getComponents()) {
			if(dnac.getDisplayId().equals(compId))
				return true;
		}
		return false;
	}
	
	
}
