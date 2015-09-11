package analysis.dynamicsim.hierarchical.simulator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Quantity;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public abstract class HierarchicalReplacement extends HierarchicalObjects
{

	private Set<String>	cache;

	public HierarchicalReplacement(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

		setTopmodel(new ModelState(getModels(), getDocument().getModel().getId(), "topmodel"));

		cache = new HashSet<String>();

		setupSubmodels(getDocument());

		setupReplacements();

		cache = null;
	}

	/**
	 * Initializes the modelstate array
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	protected void setupSubmodels(SBMLDocument document) throws XMLStreamException, IOException
	{
		String path = getRootDirectory();

		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getPlugin(CompConstants.namespaceURI);
		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(CompConstants.namespaceURI);

		if (sbmlCompModel == null)
		{
			setSubmodels(new HashMap<String, ModelState>(0));
			return;
		}

		setSubmodels(new HashMap<String, ModelState>(sbmlCompModel.getListOfSubmodels().size()));

		setupSubmodels(path, "", sbmlComp, sbmlCompModel);

	}

	private void setupSubmodels(String path, String prefix, CompSBMLDocumentPlugin sbmlComp, CompModelPlugin sbmlCompModel)
			throws XMLStreamException, IOException
	{
		for (Submodel submodel : sbmlCompModel.getListOfSubmodels())
		{

			String newPrefix = prefix + submodel.getId() + "__";
			if (!getModels().containsKey(submodel.getModelRef()))
			{
				if (sbmlComp.getListOfExternalModelDefinitions() != null
						&& sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
				{
					ExternalModelDefinition ext = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef());
					String source = ext.getSource().replace("file:", "");
					String extDef = path + getSeparator() + source;
					SBMLDocument extDoc = SBMLReader.read(new File(extDef));
					Model model = extDoc.getModel();
					CompSBMLDocumentPlugin extSbmlComp = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
					CompModelPlugin extSbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

					while (ext.isSetModelRef())
					{
						if (extSbmlComp.getExternalModelDefinition(ext.getModelRef()) != null)
						{
							ext = extSbmlComp.getListOfExternalModelDefinitions().get(ext.getModelRef());
							source = ext.getSource().replace("file:", "");
							extDef = path + getSeparator() + source;
							extDoc = SBMLReader.read(new File(extDef));
							model = extDoc.getModel();
							extSbmlComp = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
							extSbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
						}
						else if (extSbmlComp.getModelDefinition(ext.getModelRef()) != null)
						{
							model = extSbmlComp.getModelDefinition(ext.getModelRef());
							extSbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
							break;
						}
						else
						{
							break;
						}
					}

					getModels().put(submodel.getModelRef(), model);
					setupSubmodels(path, newPrefix, extSbmlComp, extSbmlCompModel);
				}
				else if (sbmlComp.getListOfModelDefinitions() != null && sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
				{
					Model model = sbmlComp.getModelDefinition(submodel.getModelRef());
					CompModelPlugin sbmlCompModelDef = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
					getModels().put(submodel.getModelRef(), model);
					setupSubmodels(path, newPrefix, sbmlComp, sbmlCompModelDef);
				}
			}
			else if (sbmlComp.getListOfModelDefinitions() != null && sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
			{
				Model model = sbmlComp.getModelDefinition(submodel.getModelRef());
				CompModelPlugin sbmlCompModelDef = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
				setupSubmodels(path, newPrefix, sbmlComp, sbmlCompModelDef);
			}

			if (isGrid())
			{
				setupGrid(submodel.getSBMLDocument(), submodel);
			}
			else
			{
				String id = prefix + submodel.getId();
				ModelState modelstate = new ModelState(getModels(), submodel.getModelRef(), id);
				getSubmodels().put(id, modelstate);
				performDeletions(modelstate, submodel);
			}
		}
	}

	private void setupExternalModelDefinition(ExternalModelDefinition ext, CompSBMLDocumentPlugin sbmlComp, CompModelPlugin sbmlCompModel,
			Submodel submodel, String path, String newPrefix) throws XMLStreamException, IOException
	{
		String source = ext.getSource().replace("file:", "");
		String extDef = path + getSeparator() + source;
		SBMLDocument extDoc = SBMLReader.read(new File(extDef));
		Model model = extDoc.getModel();
		CompSBMLDocumentPlugin extSbmlComp = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
		CompModelPlugin extSbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

		if (ext.isSetModelRef())
		{
			if (extSbmlComp.getExternalModelDefinition(ext.getModelRef()) != null)
			{
				ext = extSbmlComp.getExternalModelDefinition(ext.getModelRef());
				setupExternalModelDefinition(ext, sbmlComp, sbmlCompModel, submodel, path, newPrefix);
			}
			else if (extSbmlComp.getModelDefinition(ext.getModelRef()) != null)
			{
				model = extSbmlComp.getModelDefinition(submodel.getModelRef());
			}
		}

		getModels().put(submodel.getModelRef(), model);
		setupSubmodels(path, newPrefix, extSbmlComp, extSbmlCompModel);

	}

	private void setupGrid(SBMLDocument document, Submodel submodel)
	{
		String[] ids = biomodel.annotation.AnnotationUtility.parseArrayAnnotation(document.getModel().findQuantity(
				submodel.getModelRef() + "__locations"));
		for (String s : ids)
		{
			if (s.isEmpty())
			{
				continue;
			}
			String getID = s.replaceAll("[=].*", "");
			getSubmodels().put(getID, new ModelState(getModels(), submodel.getModelRef(), getID));

		}
	}

	private void performDeletions(ModelState modelstate, Submodel instance)
	{

		if (instance == null)
		{
			return;
		}

		for (Deletion deletion : instance.getListOfDeletions())
		{
			if (deletion.isSetPortRef())
			{
				ListOf<Port> ports = ((CompModelPlugin) getModels().get(modelstate.getModel()).getPlugin(CompConstants.namespaceURI))
						.getListOfPorts();
				Port port = ports.get(deletion.getPortRef());
				if (port != null)
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
			else if (deletion.isSetIdRef())
			{
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

	private void setupReplacements()
	{
		setupReplacements("", getTopmodel());
	}

	private void setupReplacements(String prefix, ModelState topState)
	{
		Model model = getModel(topState.getModel());

		CompModelPlugin topCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

		setupReplacement(prefix, model, topState, topCompModel);

		for (Submodel submodel : topCompModel.getListOfSubmodels())
		{
			ModelState subState = getModelState(prefix + submodel.getId());

			String newPrefix = prefix + submodel.getId() + "__";

			setupReplacements(newPrefix, subState);
		}
	}

	private void setupReplacement(String prefix, Model model, ModelState top, CompModelPlugin topCompModel)
	{
		for (int i = 0; i < model.getChildCount(); i++)
		{

			TreeNode node = model.getChildAt(i);
			if (!(node instanceof SBase))
			{
				continue;
			}

			SBase sbase = (SBase) node;
			if (sbase instanceof ListOf)
			{
				ListOf list = (ListOf) sbase;

				for (int j = 0; j < list.getChildCount(); j++)
				{
					sbase = list.get(j);
					if (sbase instanceof Quantity)
					{
						Quantity q = (Quantity) sbase;
						setupReplacement(prefix, q, top, topCompModel);
					}
					else
					{
						setupReplacement(sbase, topCompModel);
					}
				}
			}
		}

	}

	private void addReplacement(String name, String id, String subParameter, ModelState top, ModelState sub)
	{
		top.getIsHierarchical().add(id);
		top.getReplacementDependency().put(id, name);
		top.getSpeciesToReplacement(id).add(new HierarchicalStringPair(sub.getID(), subParameter));

		sub.getIsHierarchical().add(subParameter);
		sub.getReplacementDependency().put(subParameter, name);
		sub.getSpeciesToReplacement(subParameter).add(new HierarchicalStringPair(top.getID(), id));
	}

	private void addReplacedBy(String name, String id, String subId, ModelState top, ModelState sub)
	{
		top.getIsHierarchical().add(id);
		top.getReplacementDependency().put(id, name);
		top.getSpeciesToReplacement(id).add(new HierarchicalStringPair(sub.getID(), subId));

		sub.getIsHierarchical().add(subId);
		sub.getReplacementDependency().put(subId, name);
		sub.getSpeciesToReplacement(subId).add(new HierarchicalStringPair(top.getID(), id));
	}

	private void addReplacementValue(String name, double value)
	{
		getReplacements().put(name, value);
		getInitReplacementState().put(name, value);
	}

	private void setupReplacedElement(ReplacedElement element, String id, String name, String prefix, ModelState top)
	{
		ModelState sub = getModelState(prefix + element.getSubmodelRef());
		CompModelPlugin compModel = (CompModelPlugin) getModel(sub.getModel()).getExtension("comp");

		if (element.isSetIdRef())
		{

			if (getSubmodels().containsKey(element.getIdRef()) && element.isSetSBaseRef())
			{
				sub = getModelState(prefix + element.getSubmodelRef() + "__" + element.getIdRef());
				String subParameter = element.getSBaseRef().getIdRef();
				addReplacement(name, id, subParameter, top, sub);
			}
			else
			{
				String subParameter = element.getIdRef();
				addReplacement(name, id, subParameter, top, sub);
			}
		}
		else if (element.isSetMetaIdRef())
		{
			String subParameter = element.getMetaIdRef();
			addReplacement(name, id, subParameter, top, sub);
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subParameter = port.getIdRef();
			if (port.isSetMetaIdRef() && subParameter.length() == 0)
			{
				subParameter = port.getMetaIdRef();
			}
			addReplacement(name, id, subParameter, top, sub);
		}
	}

	private void setupReplacedBy(ReplacedBy replacement, String id, String name, String prefix, ModelState top, CompModelPlugin topCompModel)
	{
		if (replacement.isSetIdRef())
		{
			if (replacement.isSetSBaseRef())
			{
				ModelState sub = getModelState(prefix + replacement.getSubmodelRef() + "__" + replacement.getIdRef());
				Model submodel = getModel(sub.getModel());
				String subParameter = replacement.getSBaseRef().getIdRef();
				double value = submodel.findQuantity(subParameter).getValue();
				addReplacementValue(name, value);
				addReplacedBy(name, id, subParameter, top, sub);
			}
			else
			{
				ModelState sub = getModelState(prefix + replacement.getSubmodelRef());
				Model submodel = getModel(sub.getModel());
				String subParameter = replacement.getIdRef();
				double value = submodel.findQuantity(subParameter).getValue();
				addReplacementValue(name, value);
				addReplacedBy(name, id, subParameter, top, sub);
			}
		}
		else if (replacement.isSetPortRef())
		{
			ModelState sub = getModelState(prefix + replacement.getSubmodelRef());
			Model submodel = getModel(sub.getModel());
			Port port = topCompModel.getListOfPorts().get(replacement.getPortRef());
			String subParameter = port.getIdRef();

			double value = submodel.findQuantity(subParameter).getValue();
			addReplacementValue(name, value);

			addReplacedBy(name, id, subParameter, top, sub);
		}
	}

	private void setupReplacement(String prefix, Quantity sbase, ModelState top, CompModelPlugin topCompModel)
	{

		CompSBasePlugin sbmlSBase = (CompSBasePlugin) sbase.getExtension(CompConstants.namespaceURI);

		String id = sbase.getId();
		String name = prefix + id;

		if (sbmlSBase != null)
		{
			if (sbmlSBase.getListOfReplacedElements() != null)
			{
				addReplacementValue(name, sbase.getValue());

				for (ReplacedElement element : sbmlSBase.getListOfReplacedElements())
				{
					setupReplacedElement(element, id, name, prefix, top);
				}
			}

			if (sbmlSBase.isSetReplacedBy())
			{
				setupReplacedBy(sbmlSBase.getReplacedBy(), id, name, prefix, top, topCompModel);
			}
		}
	}

	private void setupReplacement(SBase sbase, CompModelPlugin sbmlCompModel)
	{

		CompSBasePlugin sbmlSBase = (CompSBasePlugin) sbase.getPlugin(CompConstants.namespaceURI);

		if (sbmlSBase != null)
		{
			if (sbmlSBase.getListOfReplacedElements() != null)
			{
				for (ReplacedElement element : sbmlSBase.getListOfReplacedElements())
				{
					String submodel = element.getSubmodelRef();
					ModelState modelstate = getSubmodels().get(submodel);
					CompModelPlugin subCompModel = (CompModelPlugin) getModels().get(getSubmodels().get(submodel).getModel()).getPlugin(
							CompConstants.namespaceURI);

					if (element.isSetPortRef())
					{
						Port port = subCompModel.getListOfPorts().get(element.getPortRef());
						if (port.isSetMetaIdRef())
						{
							String subParameter = port.getMetaIdRef();
							modelstate.getDeletedElementsByMetaId().add(subParameter);
						}
					}
					else if (element.isSetMetaIdRef())
					{
						modelstate.getDeletedElementsByMetaId().add(element.getMetaIdRef());
					}
				}
			}

			if (sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();

				if (replacement.isSetPortRef())
				{
					Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
					if (port.isSetMetaIdRef())
					{
						String subCompartment = port.getMetaIdRef();
						getTopmodel().getDeletedElementsByMetaId().add(subCompartment);
					}
					else if (port.isSetIdRef())
					{
						String subCompartment = port.getIdRef();
						getTopmodel().getDeletedElementsById().add(subCompartment);
					}

				}
				else if (replacement.isSetMetaIdRef())
				{
					getTopmodel().getDeletedElementsByMetaId().add(replacement.getMetaIdRef());
				}

			}
		}
	}

}
