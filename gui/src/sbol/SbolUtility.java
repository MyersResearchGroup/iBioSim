package sbol;

import java.io.FileInputStream;
import java.util.Scanner;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbolstandard.libSBOLj.DnaComponent;
import org.sbolstandard.libSBOLj.IOTools;
import org.sbolstandard.libSBOLj.Library;
import org.sbolstandard.libSBOLj.SbolService;
import org.sbolstandard.libSBOLj.SequenceAnnotation;
import org.sbolstandard.libSBOLj.SequenceFeature;

public class SbolUtility {

	public static Library loadRDF(String filePath) {
		boolean error = false;
		String rdfString = "";
		Library lib = null;
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				rdfString = rdfString.concat(token) + "\n";
			}
			scanIn.close();
		} catch (Exception e1) {
			error = true;
			JOptionPane.showMessageDialog(Gui.frame, "Error opening SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		if (!error) {
			SbolService factory = IOTools.fromRdfXml(rdfString);
			lib = factory.getLibrary();
		}
		return lib;
	}
	
	// Checks if compId clashes with the display id for a DnaComponent or SequenceFeature in Library lib
	public static boolean idClash(String compId, Library lib) {
		for (DnaComponent dnac : lib.getComponents()) {
			if (dnac.getDisplayId().equals(compId))
				return true;
		}
		for (SequenceFeature sf : lib.getFeatures()) {
			if (sf.getDisplayId().equals(compId))
				return true;
		}
		return false;
	}
	
}
