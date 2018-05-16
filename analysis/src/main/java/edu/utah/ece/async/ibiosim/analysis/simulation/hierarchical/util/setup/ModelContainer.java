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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import java.util.HashMap;
import java.util.Map;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

/**
 * Model container is used to aggregate objects related to a particular model.
 * This is necessary when setting up hierarchical models.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ModelContainer {
	private final Model model;
	private final HierarchicalModel hierarchicalModel;
	private CompSBMLDocumentPlugin compDoc;
	private final CompModelPlugin compModel;
	private final ModelContainer parent;
	private Map<String, ModelContainer> children;
	private String prefix;

	ModelContainer (Model model, HierarchicalModel hierarchicalModel, ModelContainer parent, ModelType type) {
		this.model = model;
		this.hierarchicalModel = hierarchicalModel;
		this.compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

		if (model.getSBMLDocument() != null) {
			compDoc = (CompSBMLDocumentPlugin) model.getSBMLDocument().getPlugin(CompConstants.namespaceURI);
		}

		this.parent = parent;
		setPrefix();
		addChild();
		setModelType(hierarchicalModel, model, type);
	}

	Model getModel() {
		return model;
	}

	HierarchicalModel getHierarchicalModel() {
		return hierarchicalModel;
	}

	CompSBMLDocumentPlugin getCompDoc() {
		return compDoc;
	}

	ModelContainer getChild(String id) {
		if (children != null) { return children.get(id); }
		return null;
	}

	CompModelPlugin getCompModel() {
		return compModel;
	}

	ModelContainer getParent() {
		return parent;
	}

	String getPrefix() {
		return prefix;
	}

	private void addChild() {
		if (parent != null) {
			if (parent.children == null) {
				parent.children = new HashMap<>();
			}
			parent.children.put(hierarchicalModel.getID(), this);
			parent.hierarchicalModel.addSubmodel(hierarchicalModel);
		}
	}

	private void setModelType(HierarchicalModel modelstate, Model model, ModelType type) {
		if (model.isSetSBOTerm()) {
			int sboTerm = model.getSBOTerm();
			if (sboTerm == GlobalConstants.SBO_FLUX_BALANCE) {
				modelstate.setModelType(ModelType.HFBA);
			} else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_DISCRETE) {
				modelstate.setModelType(ModelType.HSSA);
			} else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_CONTINUOUS) {
				modelstate.setModelType(ModelType.HODE);
			} else {
				modelstate.setModelType(ModelType.NONE);
			}
		} else {
			modelstate.setModelType(type);
		}

	}

	private void setPrefix() {
		if (parent != null) {
			this.prefix = parent.prefix + hierarchicalModel.getID() + "__";
		} else {
			this.prefix = "";
		}
	}

}