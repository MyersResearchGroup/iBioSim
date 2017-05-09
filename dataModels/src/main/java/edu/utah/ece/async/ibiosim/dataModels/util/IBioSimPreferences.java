/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import java.util.prefs.Preferences;

import edu.utah.ece.async.ibiosim.dataModels.util.Infos;
import edu.utah.ece.async.ibiosim.dataModels.util.PersonInfo;

public enum IBioSimPreferences {
	INSTANCE;

	private PersonInfo userInfo = null;

	public PersonInfo getUserInfo() {
		if (userInfo == null) {
			Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("user");
			String name = prefs.get("name", "");
			String email = prefs.get("email", "");
			String uri = prefs.get("uri", "http://www.dummy.org");
			userInfo = Infos.forPerson(uri, name, email);
		}

		return userInfo;
	}

	public void saveUserInfo(PersonInfo userInfo) {
		this.userInfo = userInfo;

		Preferences prefs = Preferences.userNodeForPackage(IBioSimPreferences.class).node("user");

		try {
			if (userInfo == null) {
				prefs.removeNode();
			} else {
				prefs.put("uri", userInfo.getURI().toString());
				prefs.put("name", userInfo.getName());
				if (userInfo.getEmail() != null) {
					prefs.put("email", userInfo.getEmail().toString());
				} else {
					prefs.put("email", "");
				}
			}

			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
	
	public boolean isLibSBMLValidateEnabled() {
		if (enableLibSBMLValidate == null) {
			enableLibSBMLValidate = Preferences.userRoot().get("biosim.general.validate", "").equals("libsbml");
		}

		return enableLibSBMLValidate;
	}

	public void setLibSBMLValidateEnabled(boolean enableLibSBMLValidate) {
		this.enableLibSBMLValidate = enableLibSBMLValidate;
		if (enableLibSBMLValidate) {
			Preferences.userRoot().put("biosim.general.validate", "libsbml");
		} else {
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
