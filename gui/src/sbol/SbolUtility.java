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
import org.sbolstandard.libSBOLj.SequenceFeature;

public class SbolUtility {

	public static Library loadRDF(String filePath) {
		boolean libValid = true;
		String rdfString = "";
		FileInputStream in = null;
		try {
			in = new FileInputStream(filePath);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file is not found.", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		Scanner scanIn = new Scanner(in).useDelimiter("\n");
		while (scanIn.hasNext()) {
			String token = scanIn.next();
			rdfString = rdfString.concat(token) + "\n";
		}
		scanIn.close();
		SbolService factory = IOTools.fromRdfXml(rdfString);
		Library lib = factory.getLibrary();
		String mySeparator = File.separator;
		if (mySeparator.equals("\\"))
			mySeparator = "\\\\";
		String fileId = filePath.substring(filePath.lastIndexOf(mySeparator) + 1, filePath.length());
		if (isLibraryValid(lib, fileId))
			return lib;
		else
			return null;
	}
	
	public static void exportLibrary(String filePath, Library lib) {
		try {
			if (!new File(filePath).exists()) 
				new File(filePath).createNewFile();
			String rdf = IOTools.toRdfXml(lib);
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(rdf.getBytes());
			out.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file is not found.", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Use to check if newly created collection is valid
	public static boolean isLibraryValid(Library lib) {
		return isLibraryValid(lib, "");
	}
	
	//Use to check if collection loaded from file is valid
	public static boolean isLibraryValid(Library lib, String fileId) {
		String libMessage = "";
		String compMessage = "";
		String errorType = "";
		if (fileId.equals("")) {
			libMessage = "Collection is missing display ID.";
			compMessage =  "DNA component from collection " + lib.getDisplayId() +" is missing display ID.";
			errorType = "Invalid Collection";
		} else {
			libMessage = "Collection in file " + fileId + " may be missing display ID.";
			compMessage =  "DNA component from collection " + lib.getDisplayId() + " in file " + fileId + " may be missing display ID.";
			errorType = "Invalid File";
		}
		if (lib.getDisplayId() == null) {
			JOptionPane.showMessageDialog(Gui.frame, libMessage, errorType, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		for (DnaComponent dnac : lib.getComponents())
			if (dnac.getDisplayId() == null) {
				JOptionPane.showMessageDialog(Gui.frame, compMessage, errorType, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		for (SequenceFeature sf : lib.getFeatures())
			if (sf.getDisplayId() == null) {
				JOptionPane.showMessageDialog(Gui.frame, compMessage, errorType, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		return true;
	}
	
}
