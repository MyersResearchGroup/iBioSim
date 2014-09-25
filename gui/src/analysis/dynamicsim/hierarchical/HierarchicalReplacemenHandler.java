package analysis.dynamicsim.hierarchical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Quantity;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
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
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.layout.LayoutConstants;

import analysis.dynamicsim.hierarchical.HierarchicalSim.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import biomodel.util.SBMLutilities;

public abstract class HierarchicalReplacemenHandler extends HierarchicalSim {

	public HierarchicalReplacemenHandler(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, 
			JFrame running, String[] interestingSpecies, String quantityType) throws IOException, XMLStreamException 
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);
		
		setTopmodel(new ModelState(getModels(), getDocument().getModel().getId(), "topmodel"));

		setNumSubmodels ((int)setupSubmodels(getDocument()));
		getComponentPortMap(getDocument());
	}
	
	/**
	 * Initializes the modelstate array
	 */
	protected long setupSubmodels(SBMLDocument document)
	{
		String path = getRootDirectory();
		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getExtension(CompConstants.namespaceURI);
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getExtension(CompConstants.namespaceURI);

		if(sbmlCompModel == null)
		{
			setSubmodels(new HashMap<String, ModelState>(0));
			return 0;
		}

		setSubmodels(new HashMap<String, ModelState>(sbmlCompModel.getListOfSubmodels().size()));


		int index = 0;
		//HierarchicalUtilities.extractModelDefinitions(sbmlComp, sbmlCompModel, alternativePath, separator);
		for (Submodel submodel : sbmlCompModel.getListOfSubmodels()) {


			if(!getModels().containsKey(submodel.getModelRef()))
			{

				String filename = path+getSeparator()+submodel.getModelRef()+".xml";

				if(sbmlComp.getListOfExternalModelDefinitions() != null &&
						sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
				{
					SBMLDocument extDoc = null;
					try {
						//						if(checkFileExists(alternativePath+separator+sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource()))
						//						{
						//							extDoc = SBMLReader.read(new File(alternativePath+separator+sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource()));
						//
						//						}
						//						else
						//						{
						//							extDoc = SBMLReader.read(new File(filename));
						//						}
						extDoc = SBMLReader.read(new File(filename));

					}
					catch (XMLStreamException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(extDoc.getModel());
					CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(extDoc);

					for (Submodel sub : documentCompModel.getListOfSubmodels())
						extractModelDefintion(path,  document,  sub,  sbmlComp);

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
					while (documentComp.getListOfModelDefinitions().size() > 0) {
						documentComp.removeModelDefinition(0);
					}
					for (int i = 0; i < documentComp.getListOfExternalModelDefinitions().size(); i++) {
						ExternalModelDefinition extModel = documentComp.getListOfExternalModelDefinitions().get(i);
						if (extModel.isSetModelRef()) {
							String oldId = extModel.getId();
							extModel.setSource("file:" + extModel.getModelRef() + ".xml");
							extModel.setId(extModel.getModelRef());
							extModel.unsetModelRef();
							for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
								Submodel sub = sbmlCompModel.getListOfSubmodels().get(j);
								if (sub.getModelRef().equals(oldId)) {
									sub.setModelRef(extModel.getId());
								}
							}
						}
					}
					SBMLWriter writer = new SBMLWriter();

					//updateReplacementsDeletions(extDoc, documentComp, documentCompModel, path);
					filename = path+getSeparator()+submodel.getModelRef()+"_new.xml";
					try {
						writer.writeSBMLToFile(extDoc, filename);

					} catch (SBMLException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
				}
				else if(sbmlComp.getListOfModelDefinitions() != null &&
						sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
				{
					//extractModelDefinitions(path, document, submodel, sbmlComp, sbmlCompModel);
					extractModelDefintion(path,  document,  submodel,  sbmlComp);
				}

				Model flattenModel = HierarchicalUtilities.flattenModel(path, filename);

				getModels().put(submodel.getModelRef(), flattenModel);

			}

			if(isGrid())
			{
				//String annotation = submodel.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
				//int copies = getArraySize(annotation);

				//int copies = biomodel.annotation.AnnotationUtility.parseArraySizeAnnotation(submodel);

				String[] ids = biomodel.annotation.AnnotationUtility.parseArrayAnnotation(document.getModel().getParameter(submodel.getModelRef()+ "__locations"));
				for(String s : ids)
				{
					if(s.isEmpty())
						continue;
					String getID = s.replaceAll("[=].*", "");
					getSubmodels().put(getID, new ModelState(getModels(), submodel.getModelRef(), getID));

				}

				/*LinkedList<String> ids = getArrayIDs(document.getModel().getParameter(submodel.getModelRef()+ "__locations").getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim());
				for(int i = 0; i < copies; i++)
				{
					submodels.put(ids.getFirst(), new ModelState(submodel.getModelRef(), ids.getFirst()));
					ids.removeFirst();
					index++;
				}*/
			}
			else
			{
				ModelState modelstate =  new ModelState(getModels(), submodel.getModelRef(), submodel.getId());
				getSubmodels().put(submodel.getId(), modelstate);
				performDeletions(modelstate, submodel);
				index++;
			}

		}


		//updateReplacementsDeletions(path, document, sbmlComp, sbmlCompModel);
		return index;
	}

	private void extractModelDefintion(String path, SBMLDocument document, Submodel submodel, CompSBMLDocumentPlugin sbmlComp)
	{
		ModelDefinition md = sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef());

		if(md == null)
			return;

		String extId = md.getId();
		org.sbml.jsbml.Model model = new org.sbml.jsbml.Model(md);

		model.getDeclaredNamespaces().clear();

		SBMLDocument newDoc = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);

		newDoc.setModel(model);
		newDoc.enablePackage(LayoutConstants.namespaceURI);
		newDoc.enablePackage(CompConstants.namespaceURI);
		newDoc.enablePackage(FBCConstants.namespaceURI);

		newDoc.enablePackage(ArraysConstants.namespaceURI);

		CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(newDoc);
		CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(model);

		model.unsetNamespace();
		newDoc.setModel(model);

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
		//updateReplacementsDeletions(path, document, documentComp, documentCompModel);
		SBMLWriter writer = new SBMLWriter();

		try {
			writer.writeSBMLToFile(newDoc, path + getSeparator() + extId + ".xml");
			getFilesCreated().add(path + getSeparator() + extId + ".xml");

		} catch (SBMLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}


	}

	private void performDeletions(ModelState modelstate, Submodel instance) {


		if (instance == null)
			return;

		for(Deletion deletion : instance.getListOfDeletions()){

			if (deletion.isSetPortRef()) 
			{
				ListOf<Port> ports = ((CompModelPlugin) getModels().get(modelstate.getModel()).getExtension(CompConstants.namespaceURI)).getListOfPorts();
				Port port = ports.get(deletion.getPortRef());
				if (port!=null) 
				{
					if (port.isSetIdRef())
					{
						modelstate.getDeletedElementsById().add(port.getIdRef());
					}
					else if (port.isSetMetaIdRef()) 
					{
						modelstate.getDeletedElementsByMetaId().add(port.getMetaIdRef());
					}
					else if (port.isSetUnitRef())
					{
						modelstate.getDeletedElementsByUId().add(port.getIdRef());
					}
				}

			}
			else if (deletion.isSetIdRef()) {
				modelstate.getDeletedElementsById().add(deletion.getIdRef());
			}
			else if (deletion.isSetMetaIdRef()) 
			{
				modelstate.getDeletedElementsByMetaId().add(deletion.getMetaIdRef());
			}
			else if (deletion.isSetUnitRef()) 
			{
				modelstate.getDeletedElementsByUId().add(deletion.getUnitRef());
			}
		}
	}

	protected void getComponentPortMap(SBMLDocument sbml) 
	{
		CompModelPlugin sbmlCompModel = (CompModelPlugin)sbml.getModel().getExtension(CompConstants.namespaceURI);
		setupReplacement(sbml, sbml.getModel(), sbmlCompModel);	
	}

	private void setupReplacement(Quantity sbase, CompModelPlugin sbmlCompModel)
	{
		CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);

		String p = sbase.getId();
		
		if(sbmlSBase != null)
		{
			if(sbmlSBase.getListOfReplacedElements() != null)
			{
				getReplacements().put(p, sbase.getValue());	
				getInitReplacementState().put(p, sbase.getValue());


				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					String submodel = element.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin)getModels().get(getSubmodels().get(submodel).getModel()).getExtension(CompConstants.namespaceURI);
					if(element.isSetIdRef())
					{
						String subParameter = element.getIdRef();
						getTopmodel().getIsHierarchical().add(p);
						getTopmodel().getReplacementDependency().put(p, p);
						getModel(submodel).getIsHierarchical().add(subParameter);
						getModel(submodel).getReplacementDependency().put(subParameter, p);
					}
					if(element.isSetMetaIdRef())
					{
						String subParameter = element.getMetaIdRef();
						getTopmodel().getIsHierarchical().add(p);
						getTopmodel().getReplacementDependency().put(p, p);
						getModel(submodel).getIsHierarchical().add(subParameter);
						getModel(submodel).getReplacementDependency().put(subParameter, p);
					}
					else if(element.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
						String subParameter = port.getIdRef();
						if(port.isSetMetaIdRef() && subParameter.length() == 0)
							subParameter = port.getMetaIdRef();
						getTopmodel().getIsHierarchical().add(p);
						getTopmodel().getReplacementDependency().put(p, p);
						getModel(submodel).getIsHierarchical().add(subParameter);
						getModel(submodel).getReplacementDependency().put(subParameter, p);
					}
					else
					{
						continue;
					}
				}
			}


			if(sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				String submodel = replacement.getSubmodelRef();
				sbmlCompModel = (CompModelPlugin)getModels().get(getSubmodels().get(submodel).getModel()).getExtension(CompConstants.namespaceURI);
				if(replacement.isSetIdRef())
				{
					String subParameter = replacement.getIdRef();
					ModelState temp = getModel(submodel);
					Model sub = getModels().get(temp.getModel()).getModel();
					getReplacements().put(p, sub.getParameter(subParameter).getValue());
					getInitReplacementState().put(p, getModels().get(temp.getModel()).getModel().getParameter(subParameter).getValue());
					getTopmodel().getIsHierarchical().add(p);
					getTopmodel().getReplacementDependency().put(p, p);
					getModel(submodel).getIsHierarchical().add(subParameter);
					getModel(submodel).getReplacementDependency().put(subParameter, p);
				}
				else if(replacement.isSetPortRef())
				{
					Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
					String subParameter = port.getIdRef();
					ModelState temp = getModel(submodel);
					getReplacements().put(p, getModels().get(temp.getModel()).getModel().getParameter(subParameter).getValue());
					getInitReplacementState().put(p, getModels().get(temp.getModel()).getParameter(subParameter).getValue());
					getTopmodel().getIsHierarchical().add(p);
					getTopmodel().getReplacementDependency().put(p, p);
					getModel(submodel).getIsHierarchical().add(subParameter);
					getModel(submodel).getReplacementDependency().put(subParameter, p);
				}
			}
		}
	}
	
	private void setupReplacement(SBase sbase, CompModelPlugin sbmlCompModel)
	{
		
		CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);

		if(sbmlSBase != null)
		{
			if(sbmlSBase.getListOfReplacedElements() != null)
			{
				for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
				{
					String submodel = element.getSubmodelRef();
					ModelState modelstate = getSubmodels().get(submodel);
					CompModelPlugin subCompModel = (CompModelPlugin)getModels().get(getSubmodels().get(submodel).getModel()).getExtension(CompConstants.namespaceURI);
					
					if(element.isSetPortRef())
					{
						Port port = subCompModel.getListOfPorts().get(element.getPortRef());
						String subRule = port.getMetaIdRef();
						modelstate.getDeletedElementsById().add(subRule);
					}
					else if(element.isSetMetaIdRef())
					{
						modelstate.getDeletedElementsByMetaId().add(element.getMetaIdRef());
					}
				}
			}


			if(sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				if(replacement.isSetIdRef())
				{
					getTopmodel().getDeletedElementsById().add(replacement.getIdRef());
				}
				else if(replacement.isSetPortRef())
				{
					Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
					String subCompartment = port.getIdRef();
					getTopmodel().getDeletedElementsById().add(subCompartment);
				}
				else if(replacement.isSetMetaIdRef())
				{
					getTopmodel().getDeletedElementsByMetaId().add(replacement.getMetaIdRef());
				}	

			}
		}
	}
	
	private void setupReplacement(SBMLDocument sbml, Model model, CompModelPlugin sbmlCompModel)
	{
		for (int i = 0; i < model.getChildCount(); i++) {

			TreeNode node = model.getChildAt(i);
			if(!(node instanceof SBase))
				continue;
			SBase sbase = (SBase) node;
			if(sbase instanceof Quantity)
			{
				Quantity q = (Quantity)sbase;
				setupReplacement(q, sbmlCompModel);

			}
			else
			{
				setupReplacement(sbase, sbmlCompModel);
			}

		}

	}
}
