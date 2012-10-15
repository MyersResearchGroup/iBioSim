package sbol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	
	// Adds given DNA component and all its subcomponents to top level of SBOL Document if not already present
	public static void addDNAComponent(DnaComponent dnac, SBOLDocumentImpl sbolDoc, boolean replace) {
		if (replace)
			dnac = replaceDNAComponent(dnac, sbolDoc);
		UriResolver<DnaComponent> resolver = sbolDoc.getComponentUriResolver();
		if (resolver.resolve(dnac.getURI()) == null) 
			sbolDoc.addContent(dnac);
		List<SequenceAnnotation> annos = dnac.getAnnotations();
		if (annos != null && annos.size() > 0)
			addSubComponents(annos, sbolDoc, resolver);
	}
	
	// Recursively adds annotation subcomponents to top level of SBOL document if not already present
	private static void addSubComponents(List<SequenceAnnotation> annos, SBOLDocument sbolDoc, UriResolver<DnaComponent> resolver) {
		for (SequenceAnnotation anno : annos) {
			DnaComponent subDnac = anno.getSubComponent();
			if (subDnac != null) {
				if (resolver.resolve(subDnac.getURI()) == null)
					sbolDoc.addContent(subDnac);
				List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
				if (subAnnos != null && subAnnos.size() > 0)
					addSubComponents(subAnnos, sbolDoc, resolver);
			}
		}
	}
	
	// Replaces DNA component and its subcomponents with components from SBOL document if their URIs match
	// Used to avoid conflict of multiple data structures with same URI in a single SBOL document
	public static DnaComponent replaceDNAComponent(DnaComponent dnac, SBOLDocument sbolDoc) {
		SBOLDocumentImpl flattenedDoc = flattenDocument(sbolDoc);
		UriResolver<DnaComponent> flattenedResolver = flattenedDoc.getComponentUriResolver();
		DnaComponent resolvedDnac = flattenedResolver.resolve(dnac.getURI());
		if (resolvedDnac != null)
			return resolvedDnac;
		else {
			List<SequenceAnnotation> annos = dnac.getAnnotations();
			if (annos != null && annos.size() > 0)
				replaceSubComponents(annos, flattenedResolver);
			return dnac;
		}
	}
	
	private static void replaceSubComponents(List<SequenceAnnotation> annos, UriResolver<DnaComponent> flattenedResolver) {
		for (SequenceAnnotation anno : annos) {
			DnaComponent subDnac = anno.getSubComponent();
			if (subDnac != null) {
				DnaComponent resolvedSubDnac = flattenedResolver.resolve(subDnac.getURI());
				if (resolvedSubDnac != null)
					anno.setSubComponent(resolvedSubDnac);
				else {
					List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
						replaceSubComponents(subAnnos, flattenedResolver);
				}
			}
		}
	}
	
	// Creates SBOL document in which every DNA component is accessible from the top level
	public static SBOLDocumentImpl flattenDocument(SBOLDocument sbolDoc) {
		SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLFactory.createDocument();
		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof DnaComponent) 
				addDNAComponent((DnaComponent) sbolObj, flattenedDoc, false);
			 else if (sbolObj instanceof org.sbolstandard.core.Collection) {
				flattenedDoc.addContent(sbolObj);
				for (DnaComponent dnac : ((org.sbolstandard.core.Collection) sbolObj).getComponents())
					addDNAComponent(dnac, flattenedDoc, false);
			}
		return flattenedDoc;
	}
	
	// Replaces all DNA components that have the same URI as the given DNA component with the given DNA component
	public static void mergeDNAComponent(DnaComponent mergingDnac, SBOLDocument sbolDoc) {
		SBOLDocumentImpl flattenedDoc = flattenDocument(sbolDoc);
		UriResolver<DnaComponent> flattenedResolver = flattenedDoc.getComponentUriResolver();
		replaceSubComponents(mergingDnac.getAnnotations(), flattenedResolver);
		List<DnaComponent> intersections = intersectDNAComponent(mergingDnac.getURI(), sbolDoc);
		for (DnaComponent intersectedDnac : intersections) {
			if (intersectedDnac.getURI().toString().equals(mergingDnac.getURI().toString())) {
				sbolDoc.removeContent(intersectedDnac);
				sbolDoc.addContent(mergingDnac);
			} else {
				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					DnaComponent subDnac = anno.getSubComponent();
					if (subDnac != null && subDnac.getURI().toString().equals(mergingDnac.getURI().toString())) 
						intersectedAnnos.add(anno);
				}
				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
					intersectedAnno.setSubComponent(mergingDnac);
				DnaSequence intersectedSeq = intersectedDnac.getDnaSequence();
				String nucleotides = "";
				int position = 0;
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					String subNucleotides = anno.getSubComponent().getDnaSequence().getNucleotides();
					nucleotides = nucleotides + subNucleotides;
					anno.setBioStart(position + 1);
					position = position + subNucleotides.length();
					anno.setBioEnd(position);
				}
				intersectedSeq.setNucleotides(nucleotides);
			}
		}
	}
	
	// Deletes all DNA components that have URIs matching the given URI and updates the annotations
	// and DNA sequences of composite components using the deleted components
	public static void deleteDNAComponent(URI deletingURI, SBOLDocument sbolDoc) {
		List<DnaComponent> intersections = intersectDNAComponent(deletingURI, sbolDoc);
		for (DnaComponent intersectedDnac : intersections) {
			if (intersectedDnac.getURI().toString().equals(deletingURI.toString())) 
				sbolDoc.removeContent(intersectedDnac);
			else {
				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					DnaComponent subDnac = anno.getSubComponent();
					if (subDnac != null && subDnac.getURI().toString().equals(deletingURI.toString())) 
						intersectedAnnos.add(anno);
				}
				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
					intersectedDnac.removeAnnotation(intersectedAnno);
				DnaSequence intersectedSeq = intersectedDnac.getDnaSequence();
				String nucleotides = "";
				int position = 0;
				for (SequenceAnnotation anno : intersectedDnac.getAnnotations()) {
					String subNucleotides = anno.getSubComponent().getDnaSequence().getNucleotides();
					nucleotides = nucleotides + subNucleotides;
					anno.setBioStart(position + 1);
					position = position + subNucleotides.length();
					anno.setBioEnd(position);
				}
				intersectedSeq.setNucleotides(nucleotides);
			}
		}
	}
	
	// Returns all DNA components from SBOL document that have URIs matching the given URI
	// or that have subcomponents matching the given URI
	public static List<DnaComponent> intersectDNAComponent(URI intersectingURI, SBOLDocument sbolDoc) {
		List<DnaComponent> intersections = new LinkedList<DnaComponent>();
		for (SBOLRootObject sbolObj : sbolDoc.getContents()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof DnaComponent) 
				if (sbolObj.getURI().toString().equals(intersectingURI.toString()) ) 
					intersections.add((DnaComponent) sbolObj);
				else {
					List<SequenceAnnotation> annos = ((DnaComponent) sbolObj).getAnnotations();
					if (annos != null && annos.size() > 0)
						intersections.addAll(intersectWithSubComponents(intersectingURI, (DnaComponent) sbolObj, annos));
				}
		return intersections;
	}
	
	private static List<DnaComponent> intersectWithSubComponents(URI intersectingURI, DnaComponent intersectedDnac, List<SequenceAnnotation> intersectedAnnos) {
		List<DnaComponent> subIntersections = new LinkedList<DnaComponent>();
		Iterator<SequenceAnnotation> annoIterator = intersectedAnnos.iterator();
		while (subIntersections.size() == 0 && annoIterator.hasNext()) {
			DnaComponent subDnac = annoIterator.next().getSubComponent();
			if (subDnac != null && subDnac.getURI().toString().equals(intersectingURI.toString()))
				subIntersections.add(intersectedDnac);
		}
		if (subIntersections.size() == 0) {
			for (SequenceAnnotation intersectedAnno : intersectedAnnos) {
				DnaComponent subDnac = intersectedAnno.getSubComponent();
				if (subDnac != null) {
					List<SequenceAnnotation> subAnnos = subDnac.getAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
						subIntersections.addAll(intersectWithSubComponents(intersectingURI, subDnac, subAnnos));
				}
			}
			if (subIntersections.size() > 0)
				subIntersections.add(intersectedDnac);
		}
		return subIntersections;
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
	
	public static String convertURIToSOType(URI uri) {
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
	
	public static String convertSOTypeToNum(String type) {
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
