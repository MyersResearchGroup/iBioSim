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

import org.sbolstandard.core.impl.SBOLDocumentImpl;
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
	
	public static void writeSBOLDocument(String filePath, SBOLDocument sbolDoc) {
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
	
	// Adds DNA component and all its subcomponents to top level of SBOL Document (if not already present)
	public static void addDNAComponent(DnaComponent dnac, SBOLDocumentImpl sbolDoc) {
		SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenDocument(sbolDoc);
		UriResolver<DnaComponent> flattenedResolver = flattenedDoc.getComponentUriResolver();
		UriResolver<DnaComponent> resolver = sbolDoc.getComponentUriResolver();
		DnaComponent resolvedDnac = flattenedResolver.resolve(dnac.getURI());
		if (resolvedDnac != null) {
			addSubComponents(resolvedDnac, sbolDoc, resolver);
			if (resolver.resolve(dnac.getURI()) == null)
				sbolDoc.addContent(resolvedDnac);
		} else {
			mergeSubComponents(dnac, sbolDoc, resolver, flattenedResolver);
			if (resolver.resolve(dnac.getURI()) == null)
				sbolDoc.addContent(dnac);
		}
	}
	
	// Creates SBOL document in which every DNA component is accessible from the top level
	public static SBOLDocument flattenDocument(SBOLDocument sbolDoc) {
		SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLFactory.createDocument();
		UriResolver<DnaComponent> resolver = flattenedDoc.getComponentUriResolver();
		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof DnaComponent) {
				addSubComponents((DnaComponent) sbolObj, flattenedDoc, resolver);
				if (resolver.resolve(sbolObj.getURI()) == null)
					flattenedDoc.addContent(sbolObj);
			} else if (sbolObj instanceof org.sbolstandard.core.Collection) {
				flattenedDoc.addContent((org.sbolstandard.core.Collection) sbolObj);
				for (DnaComponent dnac : ((org.sbolstandard.core.Collection) sbolObj).getComponents()) {
					addSubComponents(dnac, flattenedDoc, resolver);
					if (resolver.resolve(dnac.getURI()) == null)
						flattenedDoc.addContent(dnac);
				}
			}
		return flattenedDoc;
	}
	
	// Recursively adds subcomponents to top level of SBOL document (if not already present)
	public static void addSubComponents(DnaComponent dnac, SBOLDocument sbolDoc, UriResolver<DnaComponent> resolver) {
		if (dnac.getAnnotations() != null) 
			for (SequenceAnnotation sa : dnac.getAnnotations()) {
				DnaComponent subDnac = sa.getSubComponent();
				if (sa.getSubComponent() != null) {
					addSubComponents(subDnac, sbolDoc, resolver);
					if (resolver.resolve(subDnac.getURI()) == null)
						sbolDoc.addContent(subDnac);
				}
			}
	}
	
	// Recursively adds subcomponents to top level of SBOL document but also replaces them with DNA components from SBOL document
	// when they share an URI (avoids conflict of multiple data structures with same URI)
	public static void mergeSubComponents(DnaComponent dnac, SBOLDocument sbolDoc, UriResolver<DnaComponent> resolver, 
			UriResolver<DnaComponent> flattenedResolver) {
		if (dnac.getAnnotations() != null) 
			for (SequenceAnnotation sa : dnac.getAnnotations()) {
				DnaComponent subDnac = sa.getSubComponent();
				if (sa.getSubComponent() != null) {
					DnaComponent resolvedSubDnac = flattenedResolver.resolve(subDnac.getURI());
					if (resolvedSubDnac != null) {
						sa.setSubComponent(resolvedSubDnac);
						addSubComponents(resolvedSubDnac, sbolDoc, resolver);
						if (resolver.resolve(subDnac.getURI()) == null)
							sbolDoc.addContent(resolvedSubDnac);
					} else {
						mergeSubComponents(subDnac, sbolDoc, resolver, flattenedResolver);
						if (resolver.resolve(subDnac.getURI()) == null)
							sbolDoc.addContent(subDnac);
					}
				}
			}
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
	
	public static String uriToTypeConverter(URI uri) {
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
	
	public static String typeToNumConverter(String type) {
		if (type.equals("promoter"))
			return "SO_0000167";
		else if (type.equals("RNA polymerase promoter"))
			return "SO_0001203";
		else if (type.equals("ribosome entry site"))
			return "SO_0000139";
		else if (type.equals("coding sequence"))
			return "SO_0000316";
		else if (type.equals("terminator"))
			return "SO_0000141";
		else if (type.equals("bacterial terminator"))
			return "SO_0000614";
		else if (type.equals("engineered region"))
			return "SO_0000804";
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
