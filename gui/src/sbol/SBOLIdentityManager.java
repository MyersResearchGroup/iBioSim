package sbol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.sbml.libsbml.Model;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.util.SBOLDeepEquality;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.parser.BioModel;

public class SBOLIdentityManager {

	private List<URI> modelURIs;
	private URI bioSimURI;
	private DnaComponent bioSimComp;
	private String uriAuthority;
	private String time;
	
	public SBOLIdentityManager(BioModel biomodel) {
		setAuthority();
		modelURIs = AnnotationUtility.parseSBOLAnnotation(biomodel.getSBMLDocument().getModel());
		if (modelURIs.size() == 0)
			try {
				bioSimURI = new URI(uriAuthority + "#iBioSimPlaceHolder");
				modelURIs.add(bioSimURI);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		else {
			Iterator<URI> iterate = modelURIs.iterator();
			while (iterate.hasNext() && bioSimURI == null) {
				URI modelURI = iterate.next();
				if (modelURI.toString().endsWith("iBioSim") || modelURI.toString().endsWith("iBioSimPlaceHolder"))
					bioSimURI = modelURI;
			} 
			if (bioSimURI == null)
				try {
					bioSimURI = new URI(uriAuthority + "#iBioSimNull");
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
		}
	}
	
	public List<URI> getModelURIs() {
		return modelURIs;
	}
	
	public URI getBioSimURI() {
		return bioSimURI;
	}
	
	public DnaComponent getBioSimComp() {
		return bioSimComp;
	}
	
	public boolean containsBioSimURI() {
		return bioSimURI.toString().endsWith("iBioSim") || bioSimURI.toString().endsWith("iBioSimPlaceHolder");
	}
	
	private void loadBioSimComponent(SBOLFileManager fileManager) {
		if (bioSimURI.toString().endsWith("iBioSim"))
			bioSimComp = fileManager.resolveURI(bioSimURI);
	}
	
	public void removeBioSimURI(BioModel biomodel) {
		int removeIndex = modelURIs.indexOf(bioSimURI);
		if (removeIndex >= 0) {
			modelURIs.remove(removeIndex);
		}
	}
	
	public void replaceBioSimURI (BioModel biomodel, URI replacementURI) {
		int replaceIndex = modelURIs.indexOf(bioSimURI);
		if (replaceIndex >= 0) {
			modelURIs.remove(replaceIndex);
			modelURIs.add(replaceIndex, replacementURI);
		}
	}
	
	public void describeAndIdentifyDNAComponent(BioModel biomodel, DnaComponent dnaComp, SBOLFileManager fileManager) {
		loadBioSimComponent(fileManager);
		describeDNAComponent(biomodel, dnaComp);
		identifyDNAComponent(dnaComp);
	}
	
	// Loads SBOL descriptors such as display ID, name, and description for newly synthesized iBioSim composite component 
	// from model or previously synthesized component
	// Also determines target file for saving newly synthesized component and checks for match with previously synthesized 
	// component (latter affects save of new component and the construction of its URIs)
	private void describeDNAComponent(BioModel biomodel, DnaComponent dnaComp) {
		String[] descriptors = biomodel.getSBOLDescriptors();
		if (descriptors != null) {
			dnaComp.setDisplayId(descriptors[0]);
			dnaComp.setName(descriptors[1]);
			dnaComp.setDescription(descriptors[2]);
		} else if (bioSimComp != null) {
			dnaComp.setDisplayId(bioSimComp.getDisplayId());
			if (bioSimComp.getName() != null)
				dnaComp.setName(bioSimComp.getName());
			if (bioSimComp.getDescription() != null)
				dnaComp.setDescription(bioSimComp.getDescription());
		} else {
			dnaComp.setDisplayId(biomodel.getSBMLDocument().getModel().getId());
		}
		dnaComp.addType(SequenceOntology.type("SO_0000804"));
	}
			
	// Constructs URIs for newly synthesized component, its DNA sequence, and sequence annotations
	// Also replaces URI of previously synthesized component or placeholder URI among component URIs previously annotating model
	private void identifyDNAComponent(DnaComponent dnaComp) {
		if (bioSimComp != null) {
			List<SequenceAnnotation> synthAnnos = dnaComp.getAnnotations();
			List<SequenceAnnotation> existingAnnos = bioSimComp.getAnnotations();
			if (synthAnnos.size() == existingAnnos.size()) {
				for (int i = 0; i < synthAnnos.size(); i++)
					synthAnnos.get(i).setURI(existingAnnos.get(i).getURI());
				dnaComp.setURI(bioSimURI);
				if (bioSimComp.getDnaSequence() != null)
					dnaComp.getDnaSequence().setURI(bioSimComp.getDnaSequence().getURI());
				if (SBOLDeepEquality.isDeepEqual(dnaComp, bioSimComp)) 
					try {
						bioSimURI = new URI(bioSimURI.toString() + "Identical");
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			} 
		}
		if (!bioSimURI.toString().endsWith("Identical")) {
			setTime();
			// URI authority and time are set for creating new URIs
			try {
				dnaComp.setURI(new URI(uriAuthority + "#comp" + time + "_iBioSim"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			try {
				dnaComp.getDnaSequence().setURI(new URI(uriAuthority + "#seq" + time + "_iBioSim"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			List<SequenceAnnotation> synthAnnos = dnaComp.getAnnotations();
			for (int i = 0; i < synthAnnos.size(); i++) {
				try {
					synthAnnos.get(i).setURI(new URI(uriAuthority + "#anno" + i + time + "_iBioSim"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void annotateBioModel(BioModel biomodel) {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		if (modelURIs.size() > 0) {
			SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), modelURIs);
			AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
			biomodel.setModelSBOLAnnotationFlag(true);
		} else
			AnnotationUtility.removeSBOLAnnotation(sbmlModel);
	}

	private void setAuthority() {
		uriAuthority = Preferences.userRoot().get("biosim.synthesis.uri", "");
	}
	
	private void setTime() {
		Calendar now = Calendar.getInstance();
		time = "_" + now.get(Calendar.MONTH) + "_" 
				+ now.get(Calendar.DATE) + "_" + now.get(Calendar.YEAR) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" 
				+ now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + "_" + now.get(Calendar.MILLISECOND);
	}
}
