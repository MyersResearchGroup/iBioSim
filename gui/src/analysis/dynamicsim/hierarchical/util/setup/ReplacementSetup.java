package analysis.dynamicsim.hierarchical.util.setup;

import java.util.List;
import java.util.Map;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.comp.ReplacementHandler;

public class ReplacementSetup implements Setup
{

	public static void setupReplacements(List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, List<String> listOfPrefix, Map<String, Integer> mapOfModels)
	{

		for (int i = 0; i < listOfModels.size(); i++)
		{
			Model model = listOfModels.get(i);
			ModelState modelstate = listOfModules.get(i);
			CompModelPlugin topCompModel = (CompModelPlugin) model.getExtension(CompConstants.shortLabel);

			String prefix = listOfPrefix.get(i);
			setupDeletion(topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);

			for (Parameter parameter : model.getListOfParameters())
			{
				setupReplacement(parameter, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
			for (Compartment compartment : model.getListOfCompartments())
			{
				setupReplacement(compartment, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
			for (Species species : model.getListOfSpecies())
			{
				setupReplacement(species, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
			for (Reaction reaction : model.getListOfReactions())
			{
				// if (reaction.isSetKineticLaw())
				// {
				// KineticLaw law = reaction.getKineticLaw();
				// for (LocalParameter local : law.getListOfLocalParameters())
				// {
				// setupReplacement(local, modelstate, topCompModel, prefix,
				// listOfHandlers, listOfModules, listOfModels, mapOfModels);
				// }
				// }
				setupReplacement(reaction, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					setupReplacement(reactant, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
				for (SpeciesReference product : reaction.getListOfProducts())
				{
					setupReplacement(product, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					setupReplacement(modifier, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
			}
			for (Event event : model.getListOfEvents())
			{
				setupReplacement(event, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				if (event.isSetDelay())
				{
					setupReplacement(event.getDelay(), modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
				if (event.isSetPriority())
				{
					setupReplacement(event.getPriority(), modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
			}
			for (Constraint constraint : model.getListOfConstraints())
			{
				setupReplacement(constraint, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
			for (Rule rule : model.getListOfRules())
			{
				setupReplacement(rule, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
			for (InitialAssignment initAssignment : model.getListOfInitialAssignments())
			{
				setupReplacement(initAssignment, modelstate, topCompModel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
		}
	}

	private static void setupDeletion(CompModelPlugin topCompModel, String prefix, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		if (topCompModel.isSetListOfSubmodels())
		{
			for (Submodel submodel : topCompModel.getListOfSubmodels())
			{
				setupDeletion(submodel, prefix, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}
		}

	}

	private static void setupDeletion(Submodel submodel, String prefix, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		if (submodel.isSetListOfDeletions())
		{

			String subModelId = prefix + submodel.getId();

			for (Deletion deletion : submodel.getListOfDeletions())
			{

				int subIndex = mapOfModels.get(subModelId);
				ModelState sub = listOfModules.get(subIndex);
				Model model = listOfModels.get(subIndex);
				if (deletion.isSetIdRef())
				{
					String tmp = subModelId + "__" + deletion.getIdRef();
					if (mapOfModels.containsKey(tmp))
					{
						subIndex = mapOfModels.get(tmp);
						sub = listOfModules.get(subIndex);
					}
					if (deletion.isSetSBaseRef())
					{
						if (deletion.getSBaseRef().isSetIdRef())
						{
							String subId = deletion.getSBaseRef().getIdRef();
							sub.addDeletedBySid(subId);
						}
						else if (deletion.getSBaseRef().isSetMetaIdRef())
						{
							String subId = deletion.getSBaseRef().getMetaIdRef();
							sub.addDeletedByMetaId(subId);
						}

					}
					else
					{
						String subId = deletion.getIdRef();
						sub.addDeletedBySid(subId);
					}
				}
				else if (deletion.isSetMetaIdRef())
				{
					String subId = deletion.getMetaIdRef();
					sub.addDeletedByMetaId(subId);
				}
				else if (deletion.isSetPortRef())
				{
					CompModelPlugin subModel = (CompModelPlugin) model.getExtension("comp");
					Port port = subModel.getListOfPorts().get(deletion.getPortRef());
					if (port.isSetIdRef())
					{

						String subId = port.getIdRef();
						sub.addDeletedBySid(subId);
					}
					else if (port.isSetMetaIdRef())
					{

						String subId = port.getMetaIdRef();
						sub.addDeletedByMetaId(subId);
					}
					else if (port.isSetSBaseRef())
					{

						SBaseRef ref = port.getSBaseRef();
						if (ref.isSetIdRef())
						{
							sub.addDeletedBySid(ref.getIdRef());
						}
						else if (ref.isSetMetaIdRef())
						{
							sub.addDeletedByMetaId(ref.getMetaIdRef());
						}
					}
				}
			}
		}
	}

	private static void setupReplacement(AbstractNamedSBase sbase, ModelState top, CompModelPlugin topCompModel, String prefix, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
		String id = sbase.getId();

		if (sbasePlugin != null)
		{
			if (sbasePlugin.isSetReplacedBy())
			{
				ReplacementHandler handler = setupReplacedBy(sbasePlugin.getReplacedBy(), id, prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				if (handler != null)
				{
					id = handler.getFromVariable();
					top = handler.getFromModelState();
				}
			}

			if (sbasePlugin.isSetListOfReplacedElements())
			{
				for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
				{
					setupReplacedElement(element, id, prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
			}
		}

	}

	private static void setupReplacement(SBase sbase, ModelState top, CompModelPlugin topCompModel, String prefix, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);

		if (sbasePlugin != null)
		{
			if (sbasePlugin.isSetReplacedBy())
			{
				setupReplacedBy(sbasePlugin.getReplacedBy(), prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
			}

			if (sbasePlugin.isSetListOfReplacedElements())
			{
				for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
				{
					setupReplacedElement(element, prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
				}
			}
		}

	}

	private static ReplacementHandler setupReplacedBy(ReplacedBy replacement, String id, String prefix, ModelState top, CompModelPlugin topCompModel, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		String subModelId = prefix + replacement.getSubmodelRef();
		int subIndex = mapOfModels.get(subModelId);
		ModelState sub = listOfModules.get(subIndex);
		Model submodel = listOfModels.get(subIndex);
		ReplacementHandler handler = null;
		if (replacement.isSetIdRef())
		{
			String tmp = subModelId + "__" + replacement.getIdRef();
			if (mapOfModels.containsKey(tmp))
			{
				subIndex = mapOfModels.get(tmp);
				sub = listOfModules.get(subIndex);
				submodel = listOfModels.get(subIndex);
			}
			if (replacement.isSetSBaseRef())
			{
				String subId = replacement.getSBaseRef().getIdRef();
				handler = new ReplacementHandler(sub, subId, top, id);
				listOfHandlers.add(handler);
				top.addDeletedBySid(id);
			}
			else
			{
				String subId = replacement.getIdRef();

				handler = new ReplacementHandler(sub, subId, top, id);
				listOfHandlers.add(handler);
				top.addDeletedBySid(id);
			}
		}
		else if (replacement.isSetPortRef())
		{
			CompModelPlugin subModel = (CompModelPlugin) submodel.getExtension("comp");
			Port port = subModel.getListOfPorts().get(replacement.getPortRef());
			String subId = port.getIdRef();

			handler = new ReplacementHandler(sub, subId, top, id);
			listOfHandlers.add(handler);
			top.addDeletedBySid(id);
		}

		return handler;
	}

	private static void setupReplacedElement(ReplacedElement element, String id, String prefix, ModelState top, CompModelPlugin topCompModel, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		String subModelId = prefix + element.getSubmodelRef();
		int subIndex = mapOfModels.get(subModelId);
		ModelState sub = listOfModules.get(subIndex);
		Model submodel = listOfModels.get(subIndex);

		CompModelPlugin compModel = (CompModelPlugin) submodel.getExtension("comp");

		if (element.isSetIdRef())
		{
			String tmp = subModelId + "__" + element.getIdRef();
			if (mapOfModels.containsKey(tmp))
			{
				subIndex = mapOfModels.get(tmp);
				sub = listOfModules.get(subIndex);
				submodel = listOfModels.get(subIndex);
			}
			if (element.isSetSBaseRef())
			{
				SBaseRef ref = element.getSBaseRef();
				while (ref.isSetSBaseRef())
				{
					tmp = tmp + "__" + ref.getIdRef();
					ref = ref.getSBaseRef();
					subIndex = mapOfModels.get(tmp);
					sub = listOfModules.get(subIndex);
				}

				String subId = ref.getIdRef();
				listOfHandlers.add(new ReplacementHandler(top, id, sub, subId));
				sub.addDeletedBySid(subId);
			}
			else
			{
				String subId = element.getIdRef();
				listOfHandlers.add(new ReplacementHandler(top, id, sub, subId));
				sub.addDeletedBySid(subId);
			}
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subId = port.getIdRef();
			listOfHandlers.add(new ReplacementHandler(top, id, sub, subId));
			sub.addDeletedBySid(subId);
		}
	}

	private static void setupReplacedBy(ReplacedBy replacement, String prefix, ModelState top, CompModelPlugin topCompModel, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		String subModelId = prefix + replacement.getSubmodelRef();
		int subIndex = mapOfModels.get(subModelId);

		if (replacement.isSetIdRef())
		{
			String tmp = subModelId + "__" + replacement.getIdRef();
			if (mapOfModels.containsKey(tmp) && replacement.isSetSBaseRef())
			{
				subIndex = mapOfModels.get(tmp);
				listOfModules.get(subIndex);
				listOfModels.get(subIndex);
			}
			String subId = replacement.getSBaseRef().getMetaIdRef();
			top.addDeletedBySid(subId);

		}
		else if (replacement.isSetMetaIdRef())
		{
			String subId = replacement.getMetaIdRef();
			top.addDeletedByMetaId(subId);
		}
		else if (replacement.isSetPortRef())
		{
			Port port = topCompModel.getListOfPorts().get(replacement.getPortRef());
			String subId = port.getMetaIdRef();
			top.addDeletedByMetaId(subId);
		}
	}

	private static void setupReplacedElement(ReplacedElement element, String prefix, ModelState top, CompModelPlugin topCompModel, List<ReplacementHandler> listOfHandlers, List<ModelState> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	{
		String subModelId = prefix + element.getSubmodelRef();
		int subIndex = mapOfModels.get(subModelId);
		ModelState sub = listOfModules.get(subIndex);
		Model submodel = listOfModels.get(subIndex);

		CompModelPlugin compModel = (CompModelPlugin) submodel.getExtension("comp");

		if (element.isSetIdRef())
		{
			String tmp = subModelId + "__" + element.getIdRef();
			if (mapOfModels.containsKey(tmp) && element.isSetSBaseRef())
			{
				subIndex = mapOfModels.get(tmp);
				sub = listOfModules.get(subIndex);
				submodel = listOfModels.get(subIndex);
				String subId = element.getSBaseRef().getMetaIdRef();
				sub.addDeletedByMetaId(subId);
			}
		}
		else if (element.isSetMetaIdRef())
		{
			String subId = element.getMetaIdRef();
			sub.addDeletedByMetaId(subId);
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subId = port.getMetaIdRef();
			sub.addDeletedByMetaId(subId);
		}
	}

}
