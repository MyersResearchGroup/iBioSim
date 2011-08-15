package sbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
			SbolService factory = IOTools.fromRdfXml(rdfString);
			lib = factory.getLibrary();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error opening SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return lib;
	}
	
	public static void exportLibrary(String filePath, Library lib) {
		try {
			if (!new File(filePath).exists()) 
				new File(filePath).createNewFile();
			String rdf = IOTools.toRdfXml(lib);
			new File(filePath).createNewFile();
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(rdf.getBytes());
			out.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error exporting to SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
