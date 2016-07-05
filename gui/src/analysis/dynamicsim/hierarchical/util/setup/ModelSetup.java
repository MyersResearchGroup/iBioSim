package analysis.dynamicsim.hierarchical.util.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.methods.HierarchicalMixedSimulator;
import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.states.State.ModelType;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.ReplacementHandler;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;
import biomodel.util.GlobalConstants;

public class ModelSetup implements Setup
{
	/**
	 * Initializes the modelstate array
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static void setupModels(HierarchicalSimulation sim, boolean isSSA) throws XMLStreamException, IOException
	{
		SBMLDocument document = sim.getDocument();
		Model model = document.getModel();
		String rootPath = sim.getRootDirectory();
		List<ModelState> listOfModules = new ArrayList<ModelState>();
		List<Model> listOfModels = new ArrayList<Model>();
		List<String> listOfPrefix = new ArrayList<String>();
		List<ReplacementHandler> listOfHandlers = new ArrayList<ReplacementHandler>();
		Map<String, Integer> indexToModel = new HashMap<String, Integer>();

		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getPlugin(CompConstants.namespaceURI);

		CompModelPlugin sbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
		ModelState topmodel = new ModelState("topmodel");
		sim.setTopmodel(topmodel);
		setModelType(topmodel, model);
		indexToModel.put("topmodel", 0);
		listOfPrefix.add("");
		listOfModules.add(topmodel);
		listOfModels.add(model);

		if (sbmlCompModel != null)
		{
			setupSubmodels(sim, rootPath, "", sbmlComp, sbmlCompModel, listOfModules, listOfModels, listOfPrefix, indexToModel);
			ReplacementSetup.setupReplacements(listOfHandlers, listOfModules, listOfModels, listOfPrefix, indexToModel);
		}

		initializeModelStates(sim, listOfHandlers, listOfModules, listOfModels, sim.getCurrentTime(), isSSA);

		if (sim instanceof HierarchicalMixedSimulator)
		{
			initializeHybridSimulation((HierarchicalMixedSimulator) sim, listOfModels, listOfModules);
		}
	}

	private static void setupSubmodels(HierarchicalSimulation sim, String path, String prefix, CompSBMLDocumentPlugin sbmlComp, CompModelPlugin sbmlCompModel, List<ModelState> listOfModules, List<Model> listOfModels, List<String> listOfPrefix, Map<String, Integer> mapOfModels)
			throws XMLStreamException, IOException
	{

		for (Submodel submodel : sbmlCompModel.getListOfSubmodels())
		{

			String newPrefix = prefix + submodel.getId() + "__";
			Model model = null;
			CompModelPlugin compModel = null;
			CompSBMLDocumentPlugin compDoc = sbmlComp;
			if (sbmlComp.getListOfExternalModelDefinitions() != null && sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
			{
				ExternalModelDefinition ext = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef());
				String source = ext.getSource();
				String extDef = path + HierarchicalUtilities.separator + source;
				SBMLDocument extDoc = SBMLReader.read(new File(extDef));
				model = extDoc.getModel();
				compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
				compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

				while (ext.isSetModelRef())
				{
					if (compDoc.getExternalModelDefinition(ext.getModelRef()) != null)
					{
						ext = compDoc.getListOfExternalModelDefinitions().get(ext.getModelRef());
						source = ext.getSource().replace("file:", "");
						extDef = path + HierarchicalUtilities.separator + source;
						extDoc = SBMLReader.read(new File(extDef));
						model = extDoc.getModel();
						compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
						compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
					}
					else if (compDoc.getModelDefinition(ext.getModelRef()) != null)
					{
						model = compDoc.getModelDefinition(ext.getModelRef());
						compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
						break;
					}
					else
					{
						break;
					}
				}
			}
			else if (sbmlComp.getListOfModelDefinitions() != null && sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
			{
				model = sbmlComp.getModelDefinition(submodel.getModelRef());
				compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
			}

			if (model != null)
			{
				String id = prefix + submodel.getId();
				ModelState modelstate = new ModelState(id);
				sim.addSubmodel(id, modelstate);
				mapOfModels.put(id, mapOfModels.size());
				listOfPrefix.add(newPrefix);
				listOfModules.add(modelstate);
				listOfModels.add(model);
				setModelType(modelstate, model);
				setupSubmodels(sim, path, newPrefix, compDoc, compModel, listOfModules, listOfModels, listOfPrefix, mapOfModels);
			}
		}
	}

	private static void initializeModelStates(HierarchicalSimulation sim, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, VariableNode time, boolean isSSA) throws IOException
	{
		for (int i = 0; i < listOfModules.size(); i++)
		{
			CoreSetup.initializeVariables(listOfModules.get(i), listOfModels.get(i), time);
		}

		for (int i = listOfHandlers.size() - 1; i >= 0; i--)
		{
			listOfHandlers.get(i).copyNodeTo();
		}

		if (isSSA)
		{
			sim.linkPropensities();
		}

		for (int i = 0; i < listOfModules.size(); i++)
		{
			CoreSetup.initializeModel(listOfModules.get(i), listOfModels.get(i), time, isSSA);
		}

	}

	private static void initializeHybridSimulation(HierarchicalMixedSimulator sim, List<Model> listOfModels, List<ModelState> listOfModules) throws IOException
	{
		List<ModelState> listOfODEStates = new ArrayList<ModelState>();
		List<ModelState> listOfSSAStates = new ArrayList<ModelState>();
		List<ModelState> listOfFBAStates = new ArrayList<ModelState>();
		List<Model> listOfFBAModels = new ArrayList<Model>();

		for (int i = 0; i < listOfModels.size(); i++)
		{
			Model model = listOfModels.get(i);
			ModelState state = listOfModules.get(i);

			if (state.getModelType() == ModelType.HFBA)
			{
				listOfFBAStates.add(state);
				listOfFBAModels.add(model);
			}
			else if (state.getModelType() == ModelType.HSSA)
			{
				listOfSSAStates.add(state);
			}
			else
			{
				listOfODEStates.add(state);
			}
		}

		addSimulationMethod(sim, listOfODEStates, false);
		addSimulationMethod(sim, listOfSSAStates, true);
		addFBA(sim, listOfFBAModels, listOfFBAStates);
	}

	private static void addSimulationMethod(HierarchicalMixedSimulator sim, List<ModelState> listOfODEStates, boolean isSSA)
	{
		if (listOfODEStates.size() > 0)
		{
			ModelState topmodel = listOfODEStates.get(0);
			Map<String, ModelState> submodels = listOfODEStates.size() > 1 ? new HashMap<String, ModelState>() : null;

			for (int i = 1; i < listOfODEStates.size(); i++)
			{
				ModelState submodel = listOfODEStates.get(i);
				submodels.put(submodel.getID(), submodel);
			}

			if (isSSA)
			{
				sim.createODESim(topmodel, submodels);
			}
			else
			{
				sim.createODESim(topmodel, submodels);
			}
		}
	}

	// TODO: generalize this
	private static void addFBA(HierarchicalMixedSimulator sim, List<Model> listOfFBAModels, List<ModelState> listOfFBAStates)
	{
		if (listOfFBAModels.size() > 0)
		{
			ModelState state = listOfFBAStates.get(0);
			Model model = listOfFBAModels.get(0);
			sim.createFBASim(state, model);
		}
	}

	private static void setModelType(ModelState modelstate, Model model)
	{
		int sboTerm = model.isSetSBOTerm() ? model.getSBOTerm() : -1;
		if (sboTerm == GlobalConstants.SBO_FLUX_BALANCE)
		{
			modelstate.setModelType(ModelType.HFBA);
		}
		else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_DISCRETE)
		{
			modelstate.setModelType(ModelType.HSSA);
		}
		else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_CONTINUOUS)
		{
			modelstate.setModelType(ModelType.HODE);
		}
		else
		{
			modelstate.setModelType(ModelType.NONE);
		}
	}
}