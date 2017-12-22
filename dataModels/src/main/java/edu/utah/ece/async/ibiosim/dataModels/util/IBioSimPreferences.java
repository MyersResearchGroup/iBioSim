/*
 * Copyright (c) 2012 - 2017, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.utah.ece.async.ibiosim.dataModels.util;

import java.util.HashMap;
import java.util.prefs.Preferences;

import edu.utah.ece.async.ibiosim.dataModels.util.Infos;
import edu.utah.ece.async.ibiosim.dataModels.util.PersonInfo;

public enum IBioSimPreferences {
	INSTANCE;

	private PersonInfo userInfo = null;

//	public PersonInfo getUserInfo() {
//		if (userInfo == null) {
//			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("user");
//			String name = prefs.get("name", "");
//			String email = prefs.get("email", "");
//			String uri = prefs.get("uri", "http://www.dummy.org");
//			userInfo = Infos.forPerson(uri, name, email);
//		}
//
//		return userInfo;
//	}
//
//	public void saveUserInfo(PersonInfo userInfo) {
//		this.userInfo = userInfo;
//
//		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("user");
//
//		try {
//			if (userInfo == null) {
//				prefs.removeNode();
//			} else {
//				prefs.put("uri", userInfo.getURI().toString());
//				prefs.put("name", userInfo.getName());
//				if (userInfo.getEmail() != null) {
//					prefs.put("email", userInfo.getEmail().toString());
//				} else {
//					prefs.put("email", "");
//				}
//			}
//
//			prefs.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public boolean getValidate() {
		return false;
	}

	private Boolean enableFileDialog = null;
	
	public boolean isFileDialogEnabled() {
		if (enableFileDialog == null) {
			enableFileDialog = Preferences.userRoot().get("biosim.general.file_browser", "").equals("FileDialog");
		}

		return enableFileDialog;
	}

	public void setFileDialogEnabled(boolean enableFileDialog) {
		this.enableFileDialog = enableFileDialog;
		if (enableFileDialog) {
			Preferences.userRoot().put("biosim.general.file_browser", "FileDialog");
		} else {
			Preferences.userRoot().put("biosim.general.file_browser", "JFileChooser");
		}
	}

	private Boolean enablePlusMinusIcons = null;
	
	public boolean isPlusMinusIconsEnabled() {
		if (enablePlusMinusIcons == null) {
			enablePlusMinusIcons = Preferences.userRoot().get("biosim.general.tree_icons", "").equals("plus_minus");
		}

		return enablePlusMinusIcons;
	}

	public void setPlusMinusIconsEnabled(boolean enablePlusMinusIcons) {
		this.enablePlusMinusIcons = enablePlusMinusIcons;
		if (enablePlusMinusIcons) {
			Preferences.userRoot().put("biosim.general.tree_icons", "plus_minus");
		} else {
			Preferences.userRoot().put("biosim.general.tree_icons", "default");
		}
	}
	
	private Boolean enableNoConfirm = null;
	
	public boolean isNoConfirmEnabled() {
		if (enableNoConfirm == null) {
			enableNoConfirm = Preferences.userRoot().get("biosim.general.delete", "").equals("noconfirm");
		}

		return enableNoConfirm;
	}

	public void setNoConfirmEnabled(boolean enableNoConfirm) {
		this.enableNoConfirm = enableNoConfirm;
		if (enableNoConfirm) {
			Preferences.userRoot().put("biosim.general.delete", "noconfirm");
		} else {
			Preferences.userRoot().put("biosim.general.delete", "confirm");
		}
	}
	
	private Boolean enableLibSBMLFlatten = null;
	
	public boolean isLibSBMLFlattenEnabled() {
		if (enableLibSBMLFlatten == null) {
			enableLibSBMLFlatten = Preferences.userRoot().get("biosim.general.flatten", "").equals("libsbml");
		}

		return enableLibSBMLFlatten;
	}

	public void setLibSBMLFlattenEnabled(boolean enableLibSBMLFlatten) {
		this.enableLibSBMLFlatten = enableLibSBMLFlatten;
		if (enableLibSBMLFlatten) {
			Preferences.userRoot().put("biosim.general.flatten", "libsbml");
		} else {
			Preferences.userRoot().put("biosim.general.flatten", "default");
		}
	}
	
	private Boolean enableLibSBMLValidate = null;
	private Boolean enableJSBMLValidate = null;
	
	public boolean isLibSBMLValidateEnabled() {
		if (enableLibSBMLValidate == null) {
			enableLibSBMLValidate = Preferences.userRoot().get("biosim.general.validate", "").equals("libsbml");
		}

		return enableLibSBMLValidate;
	}

	public boolean isJSBMLValidateEnabled() {
    if (enableJSBMLValidate == null) {
      enableJSBMLValidate = Preferences.userRoot().get("biosim.general.validate", "").equals("jsbml");
    }

    return enableJSBMLValidate;
  }
	
	public void setValidateEnabled(boolean enableLibSBMLValidate, boolean enableJsbmlValidate) {
		this.enableLibSBMLValidate = enableLibSBMLValidate;
		this.enableJSBMLValidate = enableJsbmlValidate;
		if (enableLibSBMLValidate) 
		{
			Preferences.userRoot().put("biosim.general.validate", "libsbml");
		} 
		if (enableJsbmlValidate) 
    {
      Preferences.userRoot().put("biosim.general.validate", "jsbml");
    } 
		else 
		{
			Preferences.userRoot().put("biosim.general.validate", "default");
		}
	}

	private Boolean enableWarnings = null;
	
	public boolean isWarningsEnabled() {
		if (enableWarnings == null) {
			enableWarnings = Preferences.userRoot().get("biosim.general.warnings", "").equals("true");
		}

		return enableWarnings;
	}

	public void setWarningsEnabled(boolean enableWarnings) {
		this.enableWarnings = enableWarnings;
		if (enableWarnings) {
			Preferences.userRoot().put("biosim.general.warnings", "true");
		} else {
			Preferences.userRoot().put("biosim.general.warnings", "false");
		}
	}

	private String xhtmlCmd = null;
	
	public String getXhtmlCmd() {
		if (xhtmlCmd == null) {
			xhtmlCmd = Preferences.userRoot().get("biosim.general.browser", "");
			if (xhtmlCmd.equals("")) {
				if (System.getProperty("os.name").contentEquals("Linux")) {
					xhtmlCmd = "xdg-open";
				} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					xhtmlCmd = "open";
				} else {
					xhtmlCmd = "cmd /c start";
				}
			}
		}
		return xhtmlCmd;
	}

	public void setXhtmlCmd(String xhtmlCmd) {
		this.xhtmlCmd = xhtmlCmd;
		Preferences.userRoot().put("biosim.general.browser", xhtmlCmd);
	}

	private String graphvizCmd = null;
	
	public String getGraphvizCmd() {
		if (graphvizCmd == null) {
			graphvizCmd = Preferences.userRoot().get("biosim.general.graphviz", "");
			if (graphvizCmd.equals("")) {
				if (System.getProperty("os.name").contentEquals("Linux")) {
					graphvizCmd = "xdg-open";
				} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					graphvizCmd = "open";
				} else {
					graphvizCmd = "dotty";
				}
			}
		}
		return graphvizCmd;
	}

	public void setGraphvizCmd(String graphvizCmd) {
		this.graphvizCmd = graphvizCmd;
		Preferences.userRoot().put("biosim.general.graphviz", graphvizCmd);
	}

	private String prismCmd = null;
	
	public String getPrismCmd() {
		if (prismCmd == null) {
			prismCmd = Preferences.userRoot().get("biosim.general.prism", "");
			if (prismCmd.equals("")) {
				prismCmd = "prism";
			}
		}
		return prismCmd;
	}

	public void setPrismCmd(String prismCmd) {
		this.prismCmd = prismCmd;
		Preferences.userRoot().put("biosim.general.prism", prismCmd);
	}
	
	private HashMap<String,String> schematicPreferences = new HashMap<String,String>();
	
	public String getSchematicPreference(String schematicPreference) {
		if (schematicPreferences.get(schematicPreference) == null) {
			schematicPreferences.put(schematicPreference, Preferences.userRoot().get(schematicPreference, ""));
			// TODO: default?
		}
		return schematicPreferences.get(schematicPreference);
	}

	public void setSchematicPreference(String schematicPreference, String value) {
		schematicPreferences.put(schematicPreference, value);
		Preferences.userRoot().put(schematicPreference, value);
	}

	private HashMap<String,String> modelPreferences = new HashMap<String,String>();

	public String getModelPreference(String modelPreference) {
		if (modelPreferences.get(modelPreference) == null) {
			modelPreferences.put(modelPreference, Preferences.userRoot().get(modelPreference, ""));
			if (modelPreferences.get(modelPreference).equals("")) {
				if (modelPreferences.getOrDefault("biosim.gcm.KREP_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KREP_VALUE", ".5");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KACT_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KACT_VALUE", ".0033");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.PROMOTER_COUNT_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.PROMOTER_COUNT_VALUE", "2");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KBASAL_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KBASAL_VALUE", ".0001");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.OCR_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.OCR_VALUE", ".05");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KDECAY_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KDECAY_VALUE", ".0075");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KECDECAY_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KECDECAY_VALUE", ".005");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.RNAP_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.RNAP_VALUE", "30");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.RNAP_BINDING_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.RNAP_BINDING_VALUE", ".033");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", "1");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.STOICHIOMETRY_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.STOICHIOMETRY_VALUE", "10");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KCOMPLEX_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KCOMPLEX_VALUE", "0.05");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.COOPERATIVITY_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.COOPERATIVITY_VALUE", "2");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.ACTIVED_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.ACTIVED_VALUE", ".25");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.FORWARD_MEMDIFF_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.FORWARD_MEMDIFF_VALUE", "1.0");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.REVERSE_MEMDIFF_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.REVERSE_MEMDIFF_VALUE", "0.01");
				}
				if (modelPreferences.getOrDefault("biosim.gcm.KECDIFF_VALUE", "").equals("")) {
					modelPreferences.put("biosim.gcm.KECDIFF_VALUE", "1.0");
				}
			}
		}
		return modelPreferences.get(modelPreference);
	}

	public void setModelPreference(String modelPreference, String value) {
		modelPreferences.put(modelPreference, value);
		Preferences.userRoot().put(modelPreference, value);
	}

	private HashMap<String,String> analysisPreferences = new HashMap<String,String>();

	public String getAnalysisPreference(String analysisPreference) {
		if (analysisPreferences.get(analysisPreference) == null) {
			analysisPreferences.put(analysisPreference, Preferences.userRoot().get(analysisPreference, ""));
			if (analysisPreferences.get(analysisPreference).equals("")) {
				if (analysisPreferences.getOrDefault("biosim.sim.abs", "").equals("")) {
					analysisPreferences.put("biosim.sim.abs", "None");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.type", "").equals("")) {
					analysisPreferences.put("biosim.sim.type", "ODE");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.sim", "").equals("")) {
					analysisPreferences.put("biosim.sim.sim", "rkf45");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.initial.time", "").equals("")) {
					analysisPreferences.put("biosim.sim.initial.time", "0.0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.output.start.time", "").equals("")) {
					analysisPreferences.put("biosim.sim.output.start.time", "0.0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.limit", "").equals("")) {
					analysisPreferences.put("biosim.sim.limit", "100.0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.useInterval", "").equals("")) {
					analysisPreferences.put("biosim.sim.useInterval", "Print Interval");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.interval", "").equals("")) {
					analysisPreferences.put("biosim.sim.interval", "1.0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.step", "").equals("")) {
					analysisPreferences.put("biosim.sim.step", "inf");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.min.step", "").equals("")) {
					analysisPreferences.put("biosim.sim.min.step", "0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.error", "").equals("")) {
					analysisPreferences.put("biosim.sim.error", "1.0E-9");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.relative.error", "").equals("")) {
					analysisPreferences.put("biosim.sim.relative.error", "0.0");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.seed", "").equals("")) {
					analysisPreferences.put("biosim.sim.seed", "314159");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.runs", "").equals("")) {
					analysisPreferences.put("biosim.sim.runs", "1");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.rapid1", "").equals("")) {
					analysisPreferences.put("biosim.sim.rapid1", "0.1");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.rapid2", "").equals("")) {
					analysisPreferences.put("biosim.sim.rapid2", "0.1");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.qssa", "").equals("")) {
					analysisPreferences.put("biosim.sim.qssa", "0.1");
				}
				if (analysisPreferences.getOrDefault("biosim.sim.concentration", "").equals("")) {
					analysisPreferences.put("biosim.sim.concentration", "15");
				}
			}
		}
		return analysisPreferences.get(analysisPreference);
	}

	public void setAnalysisPreference(String analysisPreference, String value) {
		analysisPreferences.put(analysisPreference, value);
		Preferences.userRoot().put(analysisPreference, value);
	}

	private HashMap<String,String> learnPreferences = new HashMap<String,String>();

	public String getLearnPreference(String learnPreference) {
		if (learnPreferences.get(learnPreference) == null) {
			learnPreferences.put(learnPreference, Preferences.userRoot().get(learnPreference, ""));
			if (learnPreferences.get(learnPreference).equals("")) {
				if (learnPreferences.getOrDefault("biosim.learn.tn", "").equals("")) {
					learnPreferences.put("biosim.learn.tn", "2");
				}
				if (learnPreferences.getOrDefault("biosim.learn.tj", "").equals("")) {
					learnPreferences.put("biosim.learn.tj", "2");
				}
				if (learnPreferences.getOrDefault("biosim.learn.ti", "").equals("")) {
					learnPreferences.put("biosim.learn.ti", "0.5");
				}
				if (learnPreferences.getOrDefault("biosim.learn.bins", "").equals("")) {
					learnPreferences.put("biosim.learn.bins", "4");
				}
				if (learnPreferences.getOrDefault("biosim.learn.equaldata", "").equals("")) {
					learnPreferences.put("biosim.learn.equaldata", "Equal Data Per Bins");
				}
				if (learnPreferences.getOrDefault("biosim.learn.autolevels", "").equals("")) {
					learnPreferences.put("biosim.learn.autolevels", "Auto");
				}
				if (learnPreferences.getOrDefault("biosim.learn.ta", "").equals("")) {
					learnPreferences.put("biosim.learn.ta", "1.15");
				}
				if (learnPreferences.getOrDefault("biosim.learn.tr", "").equals("")) {
					learnPreferences.put("biosim.learn.tr", "0.75");
				}
				if (learnPreferences.getOrDefault("biosim.learn.tm", "").equals("")) {
					learnPreferences.put("biosim.learn.tm", "0.0");
				}
				if (learnPreferences.getOrDefault("biosim.learn.tt", "").equals("")) {
					learnPreferences.put("biosim.learn.tt", "0.025");
				}
				if (learnPreferences.getOrDefault("biosim.learn.debug", "").equals("")) {
					learnPreferences.put("biosim.learn.debug", "0");
				}
				if (learnPreferences.getOrDefault("biosim.learn.succpred", "").equals("")) {
					learnPreferences.put("biosim.learn.succpred", "Successors");
				}
				if (learnPreferences.getOrDefault("biosim.learn.findbaseprob", "").equals("")) {
					learnPreferences.put("biosim.learn.findbaseprob", "False");
				}
			}
		}
		return learnPreferences.get(learnPreference);
	}

	public void setLearnPreference(String learnPreference, String value) {
		learnPreferences.put(learnPreference, value);
		Preferences.userRoot().put(learnPreference, value);
	}

	private HashMap<String,String> synthesisPreferences = new HashMap<String,String>();

	public String getSynthesisPreference(String synthesisPreference) {
		if (synthesisPreferences.get(synthesisPreference) == null) {
			synthesisPreferences.put(synthesisPreference, Preferences.userRoot().get(synthesisPreference, ""));
			if (synthesisPreferences.get(synthesisPreference).equals("")) {
				if (synthesisPreferences.getOrDefault(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, "").equals("")) {
					synthesisPreferences.put(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, GlobalConstants.GENETIC_CONSTRUCT_REGEX_DEFAULT);
				}
				if (synthesisPreferences.getOrDefault(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "").equals("")) {
					synthesisPreferences.put(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, GlobalConstants.CONSTRUCT_VALIDATION_DEFAULT);
				}
				if (synthesisPreferences.getOrDefault(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "").equals("")) {
					synthesisPreferences.put(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, GlobalConstants.CONSTRUCT_ASSEMBLY_DEFAULT);
				}
				if (synthesisPreferences.getOrDefault(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "").equals("")) {
					synthesisPreferences.put(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, GlobalConstants.CONSTRUCT_VALIDATION_WARNING_DEFAULT);
				}
			}
		}
		return synthesisPreferences.get(synthesisPreference);
	}

	public void setSynthesisPreference(String synthesisPreference, String value) {
		synthesisPreferences.put(synthesisPreference, value);
		Preferences.userRoot().put(synthesisPreference, value);
	}

	private Boolean enableBranching = null;
	private Boolean enableVersioning = null;

	public boolean isBranchingEnabled() {
		if (enableBranching == null) {
			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("versioning");
			enableBranching = prefs.getBoolean("enableBranching", false);
		}

		return enableBranching;
	}

	public void setBranchingEnabled(boolean enableBranching) {
		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("versioning");
		prefs.putBoolean("enableBranching", enableBranching);
	}

	public boolean isVersioningEnabled() {
		if (enableVersioning == null) {
			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("versioning");
			// versioning is no longer supported
			enableVersioning = prefs.getBoolean("enable", false);
		}

		return enableVersioning;
	}

	public void setVersioningEnabled(boolean enableVersioning) {
		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("versioning");
		prefs.putBoolean("enable", enableVersioning);
	}

	private Integer seqBehavior = null;

	/**
	 * askUser is 0, overwrite is 1, and keep is 2
	 */
	public Integer getSeqBehavior() {
		if (seqBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("settings");
			seqBehavior = prefs.getInt("seqBehavior", 2);
		}
		return seqBehavior;
	}

	/**
	 * askUser is 0, overwrite is 1, and keep is 2
	 */
	public void setSeqBehavior(int seqBehavior) {
		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("settings");
		prefs.putInt("seqBehavior", seqBehavior);
		this.seqBehavior = seqBehavior;
	}

	private Integer nameDisplayIdBehavior = null;

	/**
	 * show name is 0, show displayId is 1
	 */
	public Integer getNameDisplayIdBehavior() {
		if (nameDisplayIdBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("settings");
			nameDisplayIdBehavior = prefs.getInt("nameDisplayIdBehavior", 0);
		}
		return nameDisplayIdBehavior;
	}

	/**
	 * show name is 0, show displayId is 1
	 */
	public void setNameDisplayIdBehavior(int showNameOrDisplayId) {
		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("settings");
		prefs.putInt("nameDisplayIdBehavior", showNameOrDisplayId);
		this.nameDisplayIdBehavior = showNameOrDisplayId;
	}
}
