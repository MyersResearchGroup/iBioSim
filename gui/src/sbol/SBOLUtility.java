package sbol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbolstandard.core.*;

import org.sbolstandard.core.util.*;

import biomodel.util.GlobalConstants;

public class SBOLUtility {

	public static SBOLDocument loadSBOLFile(String filePath) {
		SBOLDocument sbolDoc = null;
		try {
			sbolDoc = SBOLFactory.read(new FileInputStream(filePath));
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file at " + filePath + " is invalid.", 
					"Invalid SBOL", JOptionPane.ERROR_MESSAGE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file not found at " + filePath + ".", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Error reading SBOL file at " + filePath + ".", 
					"Input/Output Error", JOptionPane.ERROR_MESSAGE);
		}
		return sbolDoc;
	}
	
	public static void exportSBOLDocument(String filePath, SBOLDocument sbolDoc) {
		try {
			SBOLFactory.write(sbolDoc, new FileOutputStream(filePath));
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Recursively adds DNA component and all its sub components to SBOL document
	public static void addDNAComponent(DnaComponent dnac, SBOLDocument sbolDoc) {
		sbolDoc.addContent(dnac);
		if (dnac.getAnnotations() != null) 
			for (SequenceAnnotation sa : dnac.getAnnotations()) 
				if (sa.getSubComponent() != null)
					addDNAComponent(sa.getSubComponent(), sbolDoc);
	}
	
	// Adds DNA component to SBOL document and recursively merges all subcomponents,
	// i.e. replaces them with DNA component of identical URI in SBOL document if present
	public static void mergeDNAComponent(DnaComponent dnac, SBOLDocument sbolDoc, HashMap<String, DnaComponent> compMap) {
		sbolDoc.addContent(dnac);
		if (dnac.getAnnotations() != null) 
			for (SequenceAnnotation sa : dnac.getAnnotations()) {
				DnaComponent subDnac = sa.getSubComponent();
				if (subDnac != null) {
					String subURI = subDnac.getURI().toString();
					if (compMap.containsKey(subURI))
						sa.setSubComponent(compMap.get(subURI));
					else
						addDNAComponent(sa.getSubComponent(), sbolDoc);
				}
			}
	}
	
	public static HashMap<String, DnaComponent> loadDNAComponents(SBOLDocument sbolDoc) {
		HashMap<String, DnaComponent> dnacs = new HashMap<String, DnaComponent>();
		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof DnaComponent) {
				loadDNAComponentsHelper(dnacs, (DnaComponent) sbolObj);
			} else if (sbolObj instanceof org.sbolstandard.core.Collection) {
				org.sbolstandard.core.Collection collect = (org.sbolstandard.core.Collection) sbolObj;
				for (DnaComponent dnac : collect.getComponents())
					loadDNAComponentsHelper(dnacs, dnac);
			}
		return dnacs;
	}
	
	private static void loadDNAComponentsHelper(HashMap<String, DnaComponent> dnacs, DnaComponent dnac) {
		dnacs.put(dnac.getURI().toString(), dnac);
		for (SequenceAnnotation sa : dnac.getAnnotations()) {
			loadDNAComponentsHelper(dnacs, sa.getSubComponent());
		}
	}
	
	public static Set<org.sbolstandard.core.Collection> loadCollections(SBOLDocument sbolDoc) {
		HashSet<org.sbolstandard.core.Collection> collections = new HashSet<org.sbolstandard.core.Collection>();
		for (SBOLRootObject sbolObj : sbolDoc.getContents()) 
			if (sbolObj instanceof org.sbolstandard.core.Collection)
				collections.add((org.sbolstandard.core.Collection) sbolObj);
		return collections;
	}
	
	// Converts global constant SBOL type to corresponding set of SO types
	public static Set<String> soSynonyms(String sbolType) {
		Set<String> types = new HashSet<String>();
		if (sbolType.equals(GlobalConstants.SBOL_CDS)) {
			types.add(SequenceOntology.CDS.toString()); // CDS
		} else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER)) {
			types.add(SequenceOntology.PROMOTER.toString()); // promoter
			types.add(SequenceOntology.type("SO_0001203").toString()); // RNA_polymerase_promoter
		} else if (sbolType.equals(GlobalConstants.SBOL_RBS)) {
			types.add(SequenceOntology.type("SO_0000139").toString()); // ribosome_entry_site
		} else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR)) {
			types.add(SequenceOntology.TERMINATOR.toString()); // terminator
			types.add(SequenceOntology.type("SO_0000614").toString()); // bacterial_terminator
		}
		return types;
	}
	
	public static String uriToSOTypeConverter(URI uri) {
		String temp = uri.toString();
		if (temp.equals(SequenceOntology.PROMOTER.toString()))
			return "promoter";
		else if (temp.equals(SequenceOntology.type("SO_0001203").toString()))
			return "RNA polymerase promoter";
		else if (temp.equals(SequenceOntology.type("SO_0000139").toString()))
			return "ribosome entry site";
		else if (temp.equals(SequenceOntology.CDS.toString()))
			return "coding sequence";
		else if (temp.equals(SequenceOntology.TERMINATOR.toString()))
			return "terminator";
		else if (temp.equals(SequenceOntology.type("SO_0000614").toString()))
			return "bacterial terminator";
		else if (temp.equals(SequenceOntology.type("SO_0000804").toString()))
			return "engineered region";
		else
			return "N/A";
	}
	
	public static String soTypeToGrammarTerminal(String soType) {
		if (soType.equals("promoter") || soType.equals("RNA polymerase promoter"))
			return "p";
		else if (soType.equals("ribosome entry site"))
			return "r";
		else if (soType.equals("coding sequence"))
			return "c";
		else if (soType.equals("terminator") || soType.equals("bacterial terminator"))
			return "t";
		else if (soType.equals("engineered region"))
			return "e";
		else
			return "x";
	}
	
}
