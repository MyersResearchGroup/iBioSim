package backend.sbol.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.sbml.jsbml.Model;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.util.SBOLDeepEquality;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.annotation.SBOLAnnotation;
import backend.biomodel.parser.BioModel;
import backend.biomodel.util.GlobalConstants;
import backend.biomodel.util.SBMLutilities;
import frontend.main.Gui;

public class SBOLIdentityManager {

	private BioModel biomodel;
	private List<URI> modelURIs;
	private String modelStrand;
	private List<DnaComponent> modelComps;
	private int indexOfBioSimURI = -1;
	private DnaComponent bioSimComp;
	private String saveFilePath;
	private String uriAuthority;
	private String time;
	
	public SBOLIdentityManager(BioModel biomodel) {
		this.biomodel = biomodel;
		loadAuthority();
		modelURIs = new LinkedList<URI>();
		modelStrand = AnnotationUtility.parseSBOLAnnotation(biomodel.getSBMLDocument().getModel(), modelURIs);
		if (modelURIs.size() == 0) {
			try {
				modelURIs.add(new URI(uriAuthority + "#iBioSimPlaceHolder"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			indexOfBioSimURI = 0;
		} else {
			int indexOfModelURI = -1;
			Iterator<URI> iterate = modelURIs.iterator();
			while (iterate.hasNext() && indexOfBioSimURI < 0) {
				indexOfModelURI++;
				URI modelURI = iterate.next();
				if (modelURI.toString().endsWith("iBioSim") || modelURI.toString().endsWith("iBioSimPlaceHolder"))
					indexOfBioSimURI = indexOfModelURI;
			} 
		}
	}
	
	public BioModel getBioModel() {
		return biomodel;
	}
	
	public List<URI> getModelURIs() {
		return modelURIs;
	}
	
	public List<DnaComponent> getModelComponents() {
		return modelComps;
	}
	
	public URI getBioSimURI() {
		return modelURIs.get(indexOfBioSimURI);
	}
	
	public DnaComponent getBioSimComponent() {
		return bioSimComp;
	}
	
	public String getSaveFilePath() {
		return saveFilePath;
	}
	
	public boolean containsBioSimURI() {
		return (indexOfBioSimURI >= 0 && 
				(modelURIs.get(indexOfBioSimURI).toString().endsWith("iBioSim") || 
						modelURIs.get(indexOfBioSimURI).toString().endsWith("iBioSimPlaceHolder")));
	}
	
	public boolean containsPlaceHolderURI() {
		return (indexOfBioSimURI >= 0 && 
				modelURIs.get(indexOfBioSimURI).toString().endsWith("iBioSimPlaceHolder"));
	}
	
	public boolean containsModelURIs() {
		if (modelURIs.size() > 0 && !(modelURIs.size() == 1 && containsPlaceHolderURI()))
			return true;
		JOptionPane.showMessageDialog(Gui.frame, "There is no SBOL associated with the model itself.\n" +
				"To associate SBOL with the model itself, you must associate SBOL\n" + 
				"with its elements and save or associate SBOL directly via the model panel.",
				"No SBOL Associated with Model", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	public void removeBioSimURI() {
		modelURIs.remove(indexOfBioSimURI);
	}
	
	public void replaceBioSimURI (URI replacementURI) {
		modelURIs.remove(indexOfBioSimURI);
		modelURIs.add(indexOfBioSimURI, replacementURI);
	}
	
//	public void insertPlaceHolderURI() {
//		try {
//			modelURIs.add(indexOfBioSimURI, new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//	}
	
	public boolean loadBioSimComponent(SBOLFileManager fileManager) {
		bioSimComp = fileManager.resolveURI(modelURIs.get(indexOfBioSimURI));
		if (bioSimComp == null) {
			String[] options = new String[]{"Ok", "Cancel"};
			int choice = JOptionPane.showOptionDialog(null, 
					"Previously synthesized composite DNA component and its descriptors could not be loaded." +
							"  Would you like to overwrite?", "Warning", JOptionPane.DEFAULT_OPTION, 
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			return (choice == 0);
		}
		return true;
	}
	
	public boolean loadAndLocateBioSimComponent(SBOLFileManager fileManager) {
		bioSimComp = fileManager.resolveAndLocateTopLevelURI(modelURIs.get(indexOfBioSimURI));
		if (bioSimComp == null) {
			String[] options = new String[]{"Ok", "Cancel"};
			int choice = JOptionPane.showOptionDialog(null, 
					"Previously synthesized composite DNA component and its descriptors could not be loaded." +
							"  Would you like to overwrite?", "Warning", JOptionPane.DEFAULT_OPTION, 
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			return (choice == 0);
		}
		saveFilePath = fileManager.getLocatedFilePath();
		return true;
	}
	
	public boolean loadModelComponents(SBOLFileManager fileManager) {
		List<URI> exportURIs = new LinkedList<URI>(modelURIs);
		if (containsPlaceHolderURI())
			exportURIs.remove(indexOfBioSimURI);
		if (exportURIs.size() > 0) {
			modelComps = fileManager.resolveURIs(exportURIs);
			return modelComps.size() > 0;
		}
		return false;
	}
	
	// Loads SBOL descriptors such as display ID, name, and description for newly synthesized iBioSim composite component 
	// from model or previously synthesized component
	// Also determines target file for saving newly synthesized component and checks for match with previously synthesized 
	// component (latter affects save of new component and the construction of its URIs)
	public void describeDNAComponent(DnaComponent dnaComp) {
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
//		dnaComp.addType(SequenceOntology.type("SO_0000804"));
	}
			
	// Constructs URIs for newly synthesized component, its DNA sequence, and sequence annotations
	// Also replaces URI of previously synthesized component or placeholder URI among component URIs previously annotating model
	public void identifyDNAComponent(DnaComponent dnaComp) {
		boolean identical = false;
		if (bioSimComp != null) {
			List<SequenceAnnotation> annos = dnaComp.getAnnotations();
			List<SequenceAnnotation> synthAnnos = bioSimComp.getAnnotations();
			if (annos.size() == synthAnnos.size()) {
				for (int i = 0; i < annos.size(); i++)
					annos.get(i).setURI(synthAnnos.get(i).getURI());
				dnaComp.setURI(modelURIs.get(indexOfBioSimURI));
				if (bioSimComp.getDnaSequence() != null)
					dnaComp.getDnaSequence().setURI(bioSimComp.getDnaSequence().getURI());
				identical = SBOLDeepEquality.isDeepEqual(dnaComp, bioSimComp);
//				if (SBOLDeepEquality.isDeepEqual(dnaComp, synthesizedComp)) 
//					try {
//						synthesizedURI = new URI(synthesizedURI.toString() + "Identical");
//					} catch (URISyntaxException e) {
//						e.printStackTrace();
//					}
			} 
		}
		if (!identical) {
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
	
	public void annotateBioModel() {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		if (modelURIs.size() > 0) {
			if (!sbmlModel.isSetMetaId() || sbmlModel.getMetaId().equals(""))
				SBMLutilities.setDefaultMetaID(biomodel.getSBMLDocument(), sbmlModel, 
						biomodel.getMetaIDIndex());
			SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), modelURIs, modelStrand);
			AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
		} else
			AnnotationUtility.removeSBOLAnnotation(sbmlModel);
	}

	private void loadAuthority() {
		uriAuthority = Preferences.userRoot().get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, "");
	}
	
	private void setTime() {
		Calendar now = Calendar.getInstance();
		time = "_" + now.get(Calendar.MONTH) + "_" 
				+ now.get(Calendar.DATE) + "_" + now.get(Calendar.YEAR) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" 
				+ now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + "_" + now.get(Calendar.MILLISECOND);
	}
}
