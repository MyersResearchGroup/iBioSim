package analysis.dynamicsim;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.layout.LayoutConstants;

import biomodel.parser.BioModel;
import biomodel.util.SBMLutilities;

public class HierarchicalUtilities {

	public static Model flattenModel(String path, String filename)
	{

		BioModel biomodel = new BioModel(path);

		biomodel.load(filename);
		//SBMLDocument sbml = biomodel.getSBMLDocument();
		SBMLDocument sbml = biomodel.flattenModel(false);		
		//GCMParser parser = new GCMParser(biomodel);
		//GeneticNetwork network = parser.buildNetwork(sbml);
		//sbml = network.getSBML();
		//network.mergeSBML(filename, sbml);

		return sbml.getModel();
	}

	private static String changeIdToPortRef(SBaseRef sbaseRef,BioModel bioModel, String root, String separator) {
		String id = "";
		if (sbaseRef.isSetSBaseRef()) {
			BioModel subModel = new BioModel(root);
			Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(sbaseRef.getIdRef());
			String extModel = bioModel.getSBMLComp().getListOfExternalModelDefinitions().get(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(root + separator + extModel);
			id += changeIdToPortRef(sbaseRef.getSBaseRef(),subModel, root, separator);
			subModel.save(root + separator + extModel);
		}
		if (sbaseRef.isSetIdRef()) {
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), sbaseRef.getIdRef());
			if (sbase!=null) {
				if (id.equals("")) {
					id = sbase.getElementName() + "__" + sbaseRef.getIdRef();
				} else {
					id = id + "__" + sbaseRef.getIdRef();
				}
				if (port == null) {
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(id);
					port.setIdRef(sbaseRef.getIdRef());
					port.setSBaseRef(sbaseRef.getSBaseRef());
				} 
				sbaseRef.unsetIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
			return "";
		} 
		if (sbaseRef.isSetMetaIdRef()) {
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), sbaseRef.getMetaIdRef());
			if (id.equals("")) {
				id = sbase.getElementName() + "__" + sbaseRef.getMetaIdRef();
			} else {
				id = id + "__" + sbaseRef.getMetaIdRef();
			}
			if (sbase!=null) { 
				if (port == null) {
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(id);
					port.setMetaIdRef(sbaseRef.getMetaIdRef());
					port.setSBaseRef(sbaseRef.getSBaseRef());
				}
				sbaseRef.unsetMetaIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
		} 
		return "";
	}

	private static boolean updatePortMap(CompSBasePlugin sbmlSBase,BioModel subModel,String subModelId, String root, String separator) {
		boolean updated = false;
		if (sbmlSBase.isSetListOfReplacedElements()) {
			for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
				if (replacement.getSubmodelRef().equals(subModelId)) {
					changeIdToPortRef(replacement,subModel, root, separator);
					updated = true;
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId)) {
				changeIdToPortRef(replacement,subModel, root, separator);
				updated = true;
			}
		}
		return updated;
	}

	private static boolean updateReplacementsDeletions(SBMLDocument document, CompSBMLDocumentPlugin sbmlComp, 
			CompModelPlugin sbmlCompModel, String root, String separator) {
		for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
			BioModel subModel = new BioModel(root);
			Submodel submodel = sbmlCompModel.getListOfSubmodels().get(i);
			String extModel = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(root + separator + extModel);
			ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(document.getModel());
			for (int j = 0; j < elements.size(); j++) {
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
				if (sbmlSBase!=null) {
					if (updatePortMap(sbmlSBase,subModel,submodel.getId(), root, separator)) {
						elements = SBMLutilities.getListOfAllElements(document.getModel());
					}
				}
			}
			for (int j = 0; j < submodel.getListOfDeletions().size(); j++) {
				Deletion deletion = submodel.getListOfDeletions().get(j);
				changeIdToPortRef(deletion,subModel, root, separator);
			}
			subModel.save(root + separator + extModel);
		}

		return true;
	}

	public static boolean extractModelDefinitions(CompSBMLDocumentPlugin sbmlComp,CompModelPlugin sbmlCompModel,
			String root, String separator) {
		for (int i=0; i < sbmlComp.getListOfModelDefinitions().size(); i++) {
			ModelDefinition md = sbmlComp.getListOfModelDefinitions().get(i);
			String extId = md.getId();


			Model model = new Model(md);
			model.getDeclaredNamespaces().clear();
			SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			document.enablePackage(LayoutConstants.namespaceURI);
			document.enablePackage(CompConstants.namespaceURI);
			document.enablePackage(FBCConstants.namespaceURI);
			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
			CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(model);
			document.setModel(model);
			ArrayList<String> comps = new ArrayList<String>();
			for (int j=0; j < documentCompModel.getListOfSubmodels().size(); j++) {
				String subModelType = documentCompModel.getListOfSubmodels().get(j).getModelRef();
				if (!comps.contains(subModelType)) {
					ExternalModelDefinition extModel = documentComp.createExternalModelDefinition();
					extModel.setId(subModelType);
					extModel.setSource("file:" + subModelType + ".xml");
					comps.add(subModelType);
				}
			}
			// Make compartment enclosing
			if (document.getModel().getCompartmentCount()==0) {
				Compartment c = document.getModel().createCompartment();
				c.setId("default");
				c.setSize(1);
				c.setSpatialDimensions(3);
				c.setConstant(true);
			}
			updateReplacementsDeletions(document, documentComp, documentCompModel, root, separator);
			SBMLWriter writer = new SBMLWriter();
			try {
				writer.writeSBMLToFile(document, root + separator + extId + ".xml");
			} catch (SBMLException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			if (sbmlComp.getListOfExternalModelDefinitions().get(extId) == null) {
				for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
					Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
					if (submodel.getModelRef().equals(extId)) {
						ExternalModelDefinition extModel = sbmlComp.createExternalModelDefinition();
						extModel.setSource("file:" + extId + ".xml");
						extModel.setId(extId);
						break;
					}
				}
			}

		}
		while (sbmlComp.getListOfModelDefinitions().size() > 0) {
			sbmlComp.removeModelDefinition(0);
		}
		for (int i = 0; i < sbmlComp.getListOfExternalModelDefinitions().size(); i++) {
			ExternalModelDefinition extModel = sbmlComp.getListOfExternalModelDefinitions().get(i);
			if (extModel.isSetModelRef()) {
				String oldId = extModel.getId();
				extModel.setSource("file:" + extModel.getModelRef() + ".xml");
				extModel.setId(extModel.getModelRef());
				extModel.unsetModelRef();
				for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
					Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
					if (submodel.getModelRef().equals(oldId)) {
						submodel.setModelRef(extModel.getId());
					}
				}
			}
		}
		return true;
	}
}
