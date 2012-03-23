package sbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.xml.*;

import biomodel.util.GlobalConstants;

public class SBOLUtility {

	public static CollectionImpl loadXML(String filePath) {
		String fileId = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		Parser p = new Parser();
		CollectionImpl lib = null;
//		try {
//			lib = p.parse(new FileInputStream(new File(filePath)));
//		} catch (FileNotFoundException e) {
//			JOptionPane.showMessageDialog(Gui.frame, "SBOL file is not found.", "File Not Found",
//					JOptionPane.ERROR_MESSAGE);
//			return null;
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(new File(filePath)));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file " + fileId + " is not found.", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		try {
			while (scanner.hasNextLine()){
				text.append(scanner.nextLine() + NL);
			}
		} finally {
			scanner.close();
		}
		try {
			lib = p.parse(text.toString());
		} catch (JAXBException e) {
			JOptionPane.showMessageDialog(Gui.frame, "There was an error parsing SBOL file " + fileId + ".", "Parsing Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (isLibraryValid(lib, fileId))
			return lib;
		else
			return null;
	}
	
	public static void exportLibrary(String filePath, CollectionImpl lib) {
		try {
			if (!new File(filePath).exists()) 
				new File(filePath).createNewFile();
			Parser p = new Parser();
			String xml = p.serialize(lib);
			FileOutputStream out = new FileOutputStream(filePath);
			out.write(xml.getBytes());
			out.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file is not found.", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// Use to check if newly created collection is valid
	public static boolean isLibraryValid(org.sbolstandard.core.Collection lib) {
		return isLibraryValid(lib, "");
	}
	
	// Use to check if collection loaded from file is valid
	public static boolean isLibraryValid(org.sbolstandard.core.Collection lib, String fileId) {
		String libMessage = "";
		String compMessage = "";
		String annoMessage = "";
		String seqMessage = "";
		String errorType = "";
		if (fileId.equals("")) {
			libMessage = "Collection is missing URI.";
			compMessage =  "DNA component is missing URI.";
			annoMessage = "Sequence annotation is missing URI.";
			seqMessage = "DNA sequence is missing URI.";
			errorType = "Invalid SBOL";
		} else {
			libMessage = "Collection in file " + fileId + " is missing URI.";
			compMessage =  "DNA component in file " + fileId + " is missing URI.";
			annoMessage = "Sequence annotation in file " + fileId + " is missing URI.";
			seqMessage = "DNA sequence in file " + fileId + " is missing URI.";
			errorType = "Invalid SBOL File";
		}
		if (lib.getURI() == null || lib.getURI().toString().equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, libMessage, errorType, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (lib.getComponents() != null)
			for (DnaComponent dnac : lib.getComponents()) {
				if (dnac.getURI() == null || dnac.getURI().toString().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, compMessage, errorType, JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (dnac.getAnnotations() != null) {
					for (SequenceAnnotation sa : dnac.getAnnotations())
						if (sa.getURI() == null || sa.getURI().toString().equals("")) {
							JOptionPane.showMessageDialog(Gui.frame, annoMessage, errorType, JOptionPane.ERROR_MESSAGE);
							return false;
						}
				}
				if (dnac.getDnaSequence() != null && (dnac.getDnaSequence().getURI() == null || dnac.getDnaSequence().getURI().toString().equals(""))) {
					JOptionPane.showMessageDialog(Gui.frame, seqMessage, errorType, JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		return true;
	}
	
	// Converts global constant SBOL type to corresponding set of SO types
	public static Set<String> typeConverter(String sbolType) {
		Set<String> types = new HashSet<String>();
		if (sbolType.equals(GlobalConstants.SBOL_ORF)) {
			types.add("SO_0000316"); // CDS
		} else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER)) {
			types.add("SO_0000167"); // promoter
			types.add("SO_0001203"); // RNA_polymerase_promoter
		} else if (sbolType.equals(GlobalConstants.SBOL_RBS)) {
			types.add("SO_0000139"); // ribosome_entry_site
		} else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR)) {
			types.add("SO_0000141"); // terminator
			types.add("SO_0000614"); // bacterial_terminator
			
		}
		return types;
	}
	
}
