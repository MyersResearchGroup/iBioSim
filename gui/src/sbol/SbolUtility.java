package sbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.xml.*;

public class SbolUtility {

	public static CollectionImpl loadXML(String filePath) {
		Parser p = new Parser();
		CollectionImpl lib = null;
		try {
			lib = p.parse(new FileInputStream(new File(filePath)));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file is not found.", "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		String mySeparator = File.separator;
		if (mySeparator.equals("\\"))
			mySeparator = "\\\\";
		String fileId = filePath.substring(filePath.lastIndexOf(mySeparator) + 1, filePath.length());
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
	
	//Use to check if newly created collection is valid
	public static boolean isLibraryValid(org.sbolstandard.core.Collection lib) {
		return isLibraryValid(lib, "");
	}
	
	//Use to check if collection loaded from file is valid
	public static boolean isLibraryValid(org.sbolstandard.core.Collection lib, String fileId) {
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
		return true;
	}
	
}
