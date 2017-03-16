/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.ext.comp.Submodel;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.comp.ModelContainer;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReplacementSetup
{

	private static void setupDeletion(ModelContainer container)
	{
		CompModelPlugin topCompModel = container.getCompModel();
		HierarchicalModel top = container.getHierarchicalModel();
		if (topCompModel.isSetListOfSubmodels())
		{
			for (Submodel submodel : topCompModel.getListOfSubmodels())
			{
				if (submodel.isSetListOfDeletions())
				{
					for (Deletion deletion : submodel.getListOfDeletions())
					{
						HierarchicalModel sub = top.getSubmodel(submodel.getId());
						if (deletion.isSetIdRef())
						{
							if (sub.containsSubmodel(deletion.getIdRef()))
							{
								sub = top.getSubmodel(deletion.getIdRef());
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
							CompModelPlugin subModel = container.getChild(submodel.getId()).getCompModel();
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
		}
	}


	public static void setupReplacement(AbstractNamedSBase sbase, VariableNode node, ModelContainer container)
	{
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);

		if (sbasePlugin != null)
		{
			if (sbasePlugin.isSetReplacedBy())
			{
				setupReplacedBy(sbasePlugin.getReplacedBy(), node,container);
			}

			if (sbasePlugin.isSetListOfReplacedElements())
			{
				for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
				{
					setupReplacedElement(element, node, container);
				}
			}
		}

	}

	private static void setupReplacedElement(ReplacedElement element, VariableNode node, ModelContainer container)
	{
		String subModelId = element.getSubmodelRef();
		HierarchicalModel top = container.getHierarchicalModel();
		HierarchicalModel sub = top.getSubmodel(subModelId);
		CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

		if (element.isSetIdRef())
		{
			if (sub.containsSubmodel(element.getIdRef()))
			{
				sub = sub.getSubmodel(element.getIdRef());
			}
			if (element.isSetSBaseRef())
			{
				SBaseRef ref = element.getSBaseRef();
				while (ref.isSetSBaseRef())
				{
					sub = sub.getSubmodel(element.getIdRef());
				}

				String subId = ref.getIdRef();
				sub.addMappingNode(subId, node);
				sub.addDeletedBySid(subId);
			}
			else
			{
				String subId = element.getIdRef();
				sub.addMappingNode(subId, node);
				sub.addDeletedBySid(subId);
			}
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subId = port.getIdRef();
			sub.addMappingNode(subId, node);
			sub.addDeletedBySid(subId);
		}
	}

	private static void setupReplacedBy(ReplacedBy element, VariableNode node, ModelContainer container)
	{
		String subModelId = element.getSubmodelRef();
		HierarchicalModel top = container.getHierarchicalModel();
		HierarchicalModel sub = top.getSubmodel(subModelId);
		CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

		if (element.isSetIdRef())
		{
			if (sub.containsSubmodel(element.getIdRef()))
			{
				sub = sub.getSubmodel(element.getIdRef());
			}
			if (element.isSetSBaseRef())
			{
				SBaseRef ref = element.getSBaseRef();
				while (ref.isSetSBaseRef())
				{
					sub = sub.getSubmodel(element.getIdRef());
				}

				String subId = ref.getIdRef();
				sub.addMappingNode(subId, node);
			}
			else
			{
				String subId = element.getIdRef();
				sub.addMappingNode(subId, node);
			}
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subId = port.getIdRef();
			sub.addMappingNode(subId, node);
		}
	}


	//  public static void setupReplacement(SBase sbase, HierarchicalModel top, CompModelPlugin topCompModel, String prefix, List<ReplacementHandler> listOfHandlers, List<HierarchicalModel> listOfModules, List<Model> listOfModels, Map<String, Integer> mapOfModels)
	//  {
	//    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
	//
	//    if (sbasePlugin != null)
	//    {
	//      if (sbasePlugin.isSetReplacedBy())
	//      {
	//        setupReplacedBy(sbasePlugin.getReplacedBy(), prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
	//      }
	//
	//      if (sbasePlugin.isSetListOfReplacedElements())
	//      {
	//        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
	//        {
	//          setupReplacedElement(element, prefix, top, topCompModel, listOfHandlers, listOfModules, listOfModels, mapOfModels);
	//        }
	//      }
	//    }
	//
	//  }
	//  
	//  private static void setupReplacedBy(ReplacedBy replacement, HierarchicalModel top, CompModelPlugin topCompModel)
	//  {
	//    String subModelId = prefix + replacement.getSubmodelRef();
	//    int subIndex = mapOfModels.get(subModelId);
	//
	//    if (replacement.isSetIdRef())
	//    {
	//      String tmp = subModelId + "__" + replacement.getIdRef();
	//      if (mapOfModels.containsKey(tmp) && replacement.isSetSBaseRef())
	//      {
	//        subIndex = mapOfModels.get(tmp);
	//        listOfModules.get(subIndex);
	//        listOfModels.get(subIndex);
	//      }
	//      String subId = replacement.getSBaseRef().getMetaIdRef();
	//      top.addDeletedBySid(subId);
	//
	//    }
	//    else if (replacement.isSetMetaIdRef())
	//    {
	//      String subId = replacement.getMetaIdRef();
	//      top.addDeletedByMetaId(subId);
	//    }
	//    else if (replacement.isSetPortRef())
	//    {
	//      Port port = topCompModel.getListOfPorts().get(replacement.getPortRef());
	//      String subId = port.getMetaIdRef();
	//      top.addDeletedByMetaId(subId);
	//    }
	//  }
	//
	//  private static void setupReplacedElement(ReplacedElement element, HierarchicalModel top, CompModelPlugin topCompModel)
	//  {
	//    String subModelId = prefix + element.getSubmodelRef();
	//    int subIndex = mapOfModels.get(subModelId);
	//    HierarchicalModel sub = listOfModules.get(subIndex);
	//    Model submodel = listOfModels.get(subIndex);
	//
	//    CompModelPlugin compModel = (CompModelPlugin) submodel.getExtension("comp");
	//
	//    if (element.isSetIdRef())
	//    {
	//      String tmp = subModelId + "__" + element.getIdRef();
	//      if (mapOfModels.containsKey(tmp) && element.isSetSBaseRef())
	//      {
	//        subIndex = mapOfModels.get(tmp);
	//        sub = listOfModules.get(subIndex);
	//        submodel = listOfModels.get(subIndex);
	//        String subId = element.getSBaseRef().getMetaIdRef();
	//        sub.addDeletedByMetaId(subId);
	//      }
	//    }
	//    else if (element.isSetMetaIdRef())
	//    {
	//      String subId = element.getMetaIdRef();
	//      sub.addDeletedByMetaId(subId);
	//    }
	//    else if (element.isSetPortRef())
	//    {
	//      Port port = compModel.getListOfPorts().get(element.getPortRef());
	//      String subId = port.getMetaIdRef();
	//      sub.addDeletedByMetaId(subId);
	//    }
	//  }

}
