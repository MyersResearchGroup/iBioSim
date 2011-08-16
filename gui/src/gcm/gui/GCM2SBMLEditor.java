package gcm.gui;

import gcm.gui.modelview.movie.MovieContainer;
import gcm.gui.modelview.movie.SchemeChooserPanel;
import gcm.gui.schematic.Schematic;
import gcm.network.GeneticNetwork;
import gcm.parser.CompatibilityFixer;
import gcm.parser.GCMFile;
import gcm.parser.GCMParser;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;

import main.Gui;
import main.Log;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;

import reb2sac.ConstraintTermThread;
import reb2sac.Reb2Sac;
import reb2sac.Reb2SacThread;
import sbmleditor.CompartmentTypes;
import sbmleditor.Compartments;
import sbmleditor.Constraints;
import sbmleditor.Events;
import sbmleditor.Functions;
import sbmleditor.InitialAssignments;
import sbmleditor.ModelPanel;
import sbmleditor.MySpecies;
import sbmleditor.Parameters;
import sbmleditor.Reactions;
import sbmleditor.Rules;
import sbmleditor.SBML_Editor;
import sbmleditor.SBMLutilities;
import sbmleditor.SpeciesTypes;
import sbmleditor.Units;
import sbol.SbolSynthesizer;
import util.MutableBoolean;

/**
 * This is the GCM2SBMLEditor class. It takes in a gcm file and allows the user
 * to edit it by changing different fields displayed in a frame. It also
 * implements the ActionListener class, the MouseListener class, and the
 * KeyListener class which allows it to perform certain actions when buttons are
 * clicked on the frame, when one of the JList's items is double clicked, or
 * when text is entered into the model's ID.
 * 
 * @author Nam Nguyen
 */
public class GCM2SBMLEditor extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private String filename = "";

	private String gcmname = "";

	private GCMFile gcm = null;

	private boolean paramsOnly;

	private ArrayList<String> parameterChanges;
	
	private ArrayList<String> getParams;
	
	private String paramFile, refFile, simName;

	private Reb2Sac reb2sac;
	
	private SBML_Editor sbmlParamFile;
	
	private String separator;
	
	private GCM2SBMLEditor gcmEditor;
	
	private ModelPanel modelPanel;
	
	private Schematic schematic;

	private Compartments compartmentPanel;
	
	private MySpecies speciesPanel;
	
	private Parameters parametersPanel;
	
	private Reactions reactionPanel;

	public GCM2SBMLEditor(String path) throws Exception {
		this(path, null, null, null, false, null, null, null, false);
	}

	public GCM2SBMLEditor(String path, String filename, Gui biosim, Log log, boolean paramsOnly,
			String simName, String paramFile, Reb2Sac reb2sac, boolean textBased) throws Exception {
		super();
		gcmEditor = this;
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.biosim = biosim;
		this.log = log;
		this.path = path;
		this.paramsOnly = paramsOnly;
		this.paramFile = paramFile;
		this.simName = simName;
		this.reb2sac = reb2sac;
		sbmlParamFile = null;
		getParams = new ArrayList<String>();
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
					if (refFile.contains(".xml"))
						refFile = refFile.replace(".xml",".gcm");
					getParams.add(refFile);
				}
				scan.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to read parameter file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				refFile = "";
			}
		}
		if (paramsOnly) {
			parameterChanges = new ArrayList<String>();
			filename = refFile;
		}
		gcm = new GCMFile(path);
		if (filename != null) {
			gcm.load(path + separator + filename);
			this.filename = filename;
			this.gcmname = filename.replace(".gcm", "");
			if ((gcm.getSBMLFile()==null || !gcm.getSBMLFile().equals(this.gcmname + ".xml")) &&
					new File(path + separator + this.gcmname + ".xml").exists()) {
				Object[] options = { "Overwrite", "Cancel" };
				int value;
				value = JOptionPane.showOptionDialog(Gui.frame, gcmname + ".xml already exists."
						+ "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.NO_OPTION) {
					throw new Exception();
				}
 			}
		}
		else {
			this.filename = "";
		}
		if (paramsOnly) {
			loadParams();
		}
		buildGui(this.filename, this.path, textBased);
	}

	public String getFilename() {
		return filename;
	}
	
	public String getPath() {
		return path;
	}

	public void reload(String newName) {
		filename = newName + ".gcm";
		gcmname = newName;
		gcm.load(path + separator + newName + ".gcm");
		if (paramsOnly) {
			GCMFile refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
			HashMap<String, String> params = refGCM.getGlobalParameters();
			for (String key : params.keySet()) {
				gcm.setDefaultParameter(key, params.get(key));
			}
		}
		// TODO
		modelPanel.setModelId(newName);
		//GCMNameTextField.setText(newName);
	}
	
	public void refresh() {
		Set<String> prom = gcm.getPromoters().keySet();
		ArrayList<String> proms = new ArrayList<String>();
		for (String s : prom) {
			proms.add(s);
		}
		if (paramsOnly) {
			for (String s : parameterChanges) {
				if (s.contains("/") && proms.contains(s.split("/")[0].trim())) {
					proms.remove(s.split("/")[0].trim());
					proms.add(s.split("/")[0].trim() + " Modified");
				}
			}
		}
		promoters.removeAllItem();
		promoters.addAllItem(proms);
		Set<String> spec = gcm.getSpecies().keySet();
		ArrayList<String> specs = new ArrayList<String>();
		for (String s : spec) {
			specs.add(s);
		}
		if (paramsOnly) {
			for (String s : parameterChanges) {
				if (s.contains("/") && specs.contains(s.split("/")[0].trim())) {
					specs.remove(s.split("/")[0].trim());
					specs.add(s.split("/")[0].trim() + " Modified");
				}
			}
		}
		species.removeAllItem();
		species.addAllItem(specs);
		Set<String> influe = gcm.getInfluences().keySet();
		ArrayList<String> influes = new ArrayList<String>();
		for (String s : influe) {
			influes.add(s);
		}
		if (paramsOnly) {
			for (String s : parameterChanges) {
				if (s.contains("\"") && influes.contains(s.split("\"")[1].trim())) {
					influes.remove(s.split("\"")[1].trim());
					influes.add(s.split("\"")[1].trim() + " Modified");
				}
			}
		}
		influences.removeAllItem();
		influences.addAllItem(influes);
		
		Set<String> comp = gcm.getComponents().keySet();
		ArrayList<String> comps = new ArrayList<String>();
		for (String c : comp) {
			String listVal = 	c + 
								" " + 
								gcm.getComponents().get(c).getProperty("gcm").replace(".gcm", "") + 
								" " + gcm.getComponentPortMap(c);
			comps.add(listVal);
		}
		components.removeAllItem();
		components.addAllItem(comps);	
		
		reloadParameters();
		if (paramsOnly) {
			GCMParser parser = new GCMParser(path + separator + refFile);
			GeneticNetwork network = parser.buildNetwork();
			GeneticNetwork.setRoot(path + separator);
			network.mergeSBML(path + separator + simName + separator + gcmname + ".xml");
			reb2sac.updateSpeciesList();
			gcm.reloadSBMLFile();
			compartmentPanel.refreshCompartmentPanel(gcm.getSBMLDocument());
			speciesPanel.refreshSpeciesPanel(gcm.getSBMLDocument());
			parametersPanel.refreshParameterPanel(gcm.getSBMLDocument());
			reactionPanel.refreshReactionPanel(gcm.getSBMLDocument());
		}
	}

	public String getGCMName() {
		return gcmname;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Object o = e.getSource();
			if (o instanceof PropertyList) {
				PropertyList list = (PropertyList) o;
				new EditCommand("Edit " + list.getName(), list).run();
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public boolean isDirty() {
		return dirty.booleanValue();
	}
	
	public void setDirty(boolean dirty) {
		this.dirty.setValue(dirty);
	}

	public GCMFile getGCM() {
		return gcm;
	}

	public String getSBMLFile() {
		return (String) sbmlFiles.getSelectedItem();
	}
	
	public HashSet<String> getSbolFiles() {
		HashSet<String> filePaths = new HashSet<String>();
		TreeModel tree = getGui().getFileTree().tree.getModel();
		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++) {
			String fileName = tree.getChild(tree.getRoot(), i).toString();
			if (fileName.endsWith("rdf"))
				filePaths.add(getGui().getRoot() + File.separator + fileName);
		}
		return filePaths;
	}

	public void save(String command) {
		//log.addText("save");
		dirty.setValue(false);

		/*
		if (!sbmlFiles.getSelectedItem().equals(none)) {
			gcm.setSBMLFile(sbmlFiles.getSelectedItem().toString());
		}
		else {
			gcm.setSBMLFile("");
		}
		*/
		GeneticNetwork.setRoot(path + separator);

		if (command.contains("GCM as")) {
			String newName = JOptionPane.showInputDialog(Gui.frame, "Enter GCM name:", "GCM Name",
					JOptionPane.PLAIN_MESSAGE);
			if (newName == null) {
				return;
			}
			if (newName.contains(".gcm")) {
				newName = newName.replace(".gcm", "");
			}
			saveAs(newName);
			return;
		}

		// Write out species and influences to a gcm file
		gcm.save(path + separator + gcmname + ".gcm");
		log.addText("Saving GCM file:\n" + path + separator + gcmname + ".gcm\n");
		if (!gcm.getSBMLFile().equals("")) {
			gcm.getSBMLDocument().getModel().setName(modelPanel.getModelName());
			SBMLWriter writer = new SBMLWriter();
			writer.writeSBML(gcm.getSBMLDocument(), path + separator + gcm.getSBMLFile());
			log.addText("Saving SBML file:\n" + path + separator + gcm.getSBMLFile() + "\n");
			if (command.contains("Check")) {
				SBMLutilities.check(path + separator + gcm.getSBMLFile());
			}
		}	
	
		if (command.contains("template")) {
			GCMParser parser = new GCMParser(path + separator + gcmname + ".gcm");
			try {
				parser.buildTopLevelNetwork(null);
			}
			catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			GeneticNetwork network = new GeneticNetwork();

			String templateName = JOptionPane.showInputDialog(Gui.frame,
					"Enter SBML template name:", "SBML Template Name", JOptionPane.PLAIN_MESSAGE);
			if (templateName != null) {
				if (!templateName.contains(".sbml") && !templateName.contains(".xml")) {
					templateName = templateName + ".xml";
				}
				if (new File(path + separator + templateName).exists()) {
					int value = JOptionPane.showOptionDialog(Gui.frame, templateName
							+ " already exists.  Overwrite file?", "Save file",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						network.buildTemplate(parser.getSpecies(), parser.getPromoters(), gcmname
								+ ".gcm", path + separator + templateName);
						log.addText("Saving GCM file as SBML template:\n" + path + separator
								+ templateName + "\n");
						biosim.addToTree(templateName);
						biosim.updateOpenSBML(templateName);
					}
					else {
						// Do nothing
					}
				}
				else {
					network.buildTemplate(parser.getSpecies(), parser.getPromoters(), gcmname
							+ ".gcm", path + separator + templateName);
					log.addText("Saving GCM file as SBML template:\n" + path + separator
							+ templateName + "\n");
					biosim.addToTree(templateName);
				}
			}
		}
		else if (command.contains("LHPN")) {
			String lpnName = JOptionPane.showInputDialog(Gui.frame,
					"Enter LPN name:", "LPN Name", JOptionPane.PLAIN_MESSAGE);
			if (!lpnName.trim().contains(".lpn")) {
				lpnName = lpnName.trim() + ".lpn";
			}
			if (new File(path + separator + lpnName).exists()) {
				int value = JOptionPane.showOptionDialog(Gui.frame, lpnName
						+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					gcm.createLogicalModel(path + separator + lpnName, log, biosim, lpnName);
				}
				else {
					// Do nothing
				}
			}
			else {
				gcm.createLogicalModel(path + separator + lpnName, log, biosim, lpnName);
			}
		}
		else if (command.contains("SBML")) {
			// Then read in the file with the GCMParser
			GCMParser parser = new GCMParser(path + separator + gcmname + ".gcm");
			GeneticNetwork network = null;
			try {
				network = parser.buildNetwork();
			}
			catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			network.loadProperties(gcm);
			// Finally, output to a file
			if (new File(path + separator + gcmname + ".xml").exists()) {
				int value = JOptionPane.showOptionDialog(Gui.frame, gcmname
						+ ".xml already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.mergeSBML(path + separator + gcmname + ".xml");
					log.addText("Saving GCM file as SBML file:\n" + path + separator + gcmname + ".xml\n");
					biosim.addToTree(gcmname + ".xml");
					biosim.updateOpenSBML(gcmname + ".xml");
				}
				else {
					// Do nothing
				}
			}
			else {
				network.mergeSBML(path + separator + gcmname + ".xml");
				log.addText("Saving GCM file as SBML file:\n" + path + separator + gcmname + ".xml\n");
				biosim.addToTree(gcmname + ".xml");
			}
		}
		biosim.updateViews(gcmname + ".gcm");

	}

	public void saveSBOL() {
		GCMParser parser = new GCMParser(gcm, false);
		SbolSynthesizer synthesizer = parser.buildSbolSynthesizer();
		HashSet<String> sbolFiles = getSbolFiles();
		if (synthesizer.loadLibraries(sbolFiles)) 
			synthesizer.saveSbol(getPath());
	}
	
	public void exportSBOL() {
		GCMParser parser = new GCMParser(gcm, false);
		SbolSynthesizer synthesizer = parser.buildSbolSynthesizer();
		if (synthesizer.loadLibraries(getSbolFiles())) {
			File lastFilePath;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
				lastFilePath = null;
			}
			else {
				lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
			}
			String targetFilePath = util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export DNA Component", -1);
			if (!targetFilePath.equals("")) {
				biosimrc.put("biosim.general.export_dir", targetFilePath);
				synthesizer.exportSbol(targetFilePath);
			}
		}
	}
	
	public void exportSBML() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBML", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
			GCMParser parser = new GCMParser(path + separator + gcmname + ".gcm");
			GeneticNetwork network = null;
			try {
				network = parser.buildNetwork();
			}
			catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			network.loadProperties(gcm);
			// Finally, output to a file
			/*
			if (new File(exportPath).exists()) {
				int value = JOptionPane.showOptionDialog(Gui.frame, exportPath + " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.mergeSBML(exportPath);
					log.addText("Saving GCM file as SBML file:\n" + exportPath +"\n");
					//biosim.addToTree(gcmname + ".xml");
					//biosim.updateOpenSBML(gcmname + ".xml");
				}
			}
			else {
			*/
			network.mergeSBML(exportPath);
			log.addText("Saving GCM file as SBML file:\n" + exportPath + "\n");
			//biosim.addToTree(gcmname + ".xml");
			//}
		}
	}
	
	public void saveAs(String newName) {
		if (new File(path + separator + newName + ".gcm").exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				gcm.save(path + separator + newName + ".gcm");
				log.addText("Saving GCM file as:\n" + path + separator + newName + ".gcm\n");
				biosim.addToTree(newName + ".gcm");
			}
			else {
				// Do nothing
				return;
			}
		}
		else {
			gcm.save(path + separator + newName + ".gcm");
			log.addText("Saving GCM file as:\n" + path + separator + newName + ".gcm\n");
			biosim.addToTree(newName + ".gcm");
		}
		biosim.updateTabName(gcmname + ".gcm", newName + ".gcm");
		reload(newName);
	}

	private void sweepHelper(ArrayList<ArrayList<Double>> sweep, String s) {
		double[] start = {0, 0};
		double[] stop = {0, 0};
		double[] step = {0, 0};
		
		String temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1).trim();
		String[] tempSlash = temp.split("/");
		start[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			start[1] = Double.parseDouble(tempSlash[1]);
			
		temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim();
		tempSlash = temp.split("/");
		stop[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			stop[1] = Double.parseDouble(tempSlash[1]);
		
		temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim();
		tempSlash = temp.split("/");
		step[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			step[1] = Double.parseDouble(tempSlash[1]);
		
		ArrayList<Double> kf = new ArrayList<Double>();
		ArrayList<Double> kr = new ArrayList<Double>();
		kf.add(start[0]);
		kr.add(start[1]);
		while (step[0] != 0 || step[1] != 0) {
			if (start[0] + step[0] > stop[0])
				step[0] = 0;
			if (start[1] + step[1] > stop[1])
				step[1] = 0;
			if (step[0] != 0 || step[1] != 0) {
				start[0] += step[0];
				start[1] += step[1];
				kf.add(start[0]);
				kr.add(start[1]);
			}
		}
		ArrayList<Double> Keq = new ArrayList<Double>();
		for (int i = 0; i < kf.size(); i++) {
			if (kr.get(i) != 0) {
				if (!Keq.contains(kf.get(i)/kr.get(i)))
						Keq.add(kf.get(i)/kr.get(i));
			} else if (!Keq.contains(kf.get(i)))
				Keq.add(kf.get(i));
		}
		sweep.add(Keq);
	}
	
	public void saveParams(boolean run, String stem, boolean ignoreSweep) {
		ArrayList<String> sweepThese1 = new ArrayList<String>();
		ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
		ArrayList<String> sweepThese2 = new ArrayList<String>();
		ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
		for (String s : parameterChanges) {
			if (s.split(" ")[s.split(" ").length - 1].startsWith("(")) {
				if ((s.split(" ")[s.split(" ").length - 1]).split(",")[3].replace(")", "").trim().equals(
						"1")) {
					sweepThese1.add(s.substring(0, s.lastIndexOf(" ")));
					sweepHelper(sweep1, s);
				}
				else {
					sweepThese2.add(s.substring(0, s.lastIndexOf(" ")));
					sweepHelper(sweep2, s);
				}
			}
		}
		if (sweepThese1.size() == 0 && (sweepThese2.size() > 0)) {
			sweepThese1 = sweepThese2;
			sweepThese2 = new ArrayList<String>();
			sweep1 = sweep2;
			sweep2 = new ArrayList<ArrayList<Double>>();
		}
		if (sweepThese1.size() > 0) {
			ArrayList<Reb2SacThread> threads = new ArrayList<Reb2SacThread>();
			ArrayList<String> dirs = new ArrayList<String>();
			ArrayList<String> levelOne = new ArrayList<String>();
			int max = 0;
			for (ArrayList<Double> d : sweep1) {
				max = Math.max(max, d.size());
			}
			for (int j = 0; j < max; j++) {
				String sweep = "";
				for (int i = 0; i < sweepThese1.size(); i++) {
					int k = j;
					if (k >= sweep1.get(i).size()) {
						k = sweep1.get(i).size() - 1;
					}
					if (sweep.equals("")) {
						sweep += sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
					}
					else {
						sweep += "_" + sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
					}
				}
				if (sweepThese2.size() > 0) {
					int max2 = 0;
					for (ArrayList<Double> d : sweep2) {
						max2 = Math.max(max2, d.size());
					}
					for (int l = 0; l < max2; l++) {
						String sweepTwo = sweep;
						for (int i = 0; i < sweepThese2.size(); i++) {
							int k = l;
							if (k >= sweep2.get(i).size()) {
								k = sweep2.get(i).size() - 1;
							}
							if (sweepTwo.equals("")) {
								sweepTwo += sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
							}
							else {
								sweepTwo += "_" + sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
							}
						}
						new File(path + separator + simName + separator + stem
								+ sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
						createSBML(stem, sweepTwo);
						if (run) {
							Reb2SacThread thread = new Reb2SacThread(reb2sac);
							thread.start(stem + sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", ""), false);
							threads.add(thread);
							dirs.add(sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", ""));
							reb2sac.emptyFrames();
							if (ignoreSweep) {
								l = max2;
								j = max;
							}
						}
					}
				}
				else {
					new File(path + separator + simName + separator + stem
							+ sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
					createSBML(stem, sweep);
					if (run) {
						Reb2SacThread thread = new Reb2SacThread(reb2sac);
						thread.start(stem + sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", ""), false);
						threads.add(thread);
						dirs.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", ""));
						reb2sac.emptyFrames();
						if (ignoreSweep) {
							j = max;
						}
					}
				}
				levelOne.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", ""));
			}
			if (run) {
				new ConstraintTermThread(reb2sac).start(threads, dirs, levelOne, stem);
			}
		}
		else {
			if (!stem.equals("")) {
				new File(path + separator + simName + separator + stem).mkdir();
			}
			createSBML(stem, ".");
			if (run) {
				if (!stem.equals("")) {
					new Reb2SacThread(reb2sac).start(stem, true);
				}
				else {
					new Reb2SacThread(reb2sac).start(".", true);
				}
				reb2sac.emptyFrames();
			}
		}
	}

	public void loadParams() {
		if (paramsOnly) {
			HashMap<String, Properties> elements = gcm.getSpecies();
			for (String key : elements.keySet()) {
				ArrayList<Object> remove = new ArrayList<Object>();
				for (Object prop : elements.get(key).keySet()) {
					if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.ID) && !prop.equals(GlobalConstants.TYPE) &&
							(!((String)prop).startsWith("graph"))) {
						remove.add(prop);
					}
				}
				for (Object prop : remove) {
					elements.get(key).remove(prop);
				}
				
			}
			elements = gcm.getInfluences();
			for (String key : elements.keySet()) {
				ArrayList<Object> remove = new ArrayList<Object>();
				for (Object prop : elements.get(key).keySet()) {
					if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.PROMOTER) && !prop.equals(GlobalConstants.BIO) && !prop.equals(GlobalConstants.TYPE) &&
							(!((String)prop).startsWith("graph"))) {
						remove.add(prop);
					}
				}
				for (Object prop : remove) {
					elements.get(key).remove(prop);
				}
			}
			elements = gcm.getPromoters();
			for (String key : elements.keySet()) {
				ArrayList<Object> remove = new ArrayList<Object>();
				for (Object prop : elements.get(key).keySet()) {
					if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.ID) &&
							(!((String)prop).startsWith("graph"))) {
						remove.add(prop);
					}
				}
				for (Object prop : remove) {
					elements.get(key).remove(prop);
				}
			}
			HashMap<String, String> params = gcm.getGlobalParameters();
			ArrayList<Object> remove = new ArrayList<Object>();
			for (String key : params.keySet()) {
				remove.add(key);
			}
			for (Object prop : remove) {
				params.remove(prop);
			}
			GCMFile refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
			params = refGCM.getGlobalParameters();
			for (String key : params.keySet()) {
				gcm.setDefaultParameter(key, params.get(key));
			}
			getParams = new ArrayList<String>();
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					scan.nextLine();
				}
				while (scan.hasNextLine()) {
					String s = scan.nextLine();
					if (!s.trim().equals("")) {
						boolean added = false;
						for (int i = 0; i < getParams.size(); i ++) {
							if (getParams
									.get(i)
									.substring(
											0,
											getParams.get(i)
													.lastIndexOf(" "))
									.equals(s.substring(0, s.lastIndexOf(" ")))) {
								getParams.set(i, s);
								added = true;
							}
						}
						if (!added) {
							getParams.add(s);
						}
					}
					else {
						break;
					}
				}
				scan.close();
			}
			catch (Exception e) {
			}
			for (String update : getParams) {
				String id;
				if (update.contains("/")) {
					id = update.split("/")[0];
					id = id.replace("\"", "");
					String prop = update.split("/")[1].substring(0, update.split("/")[1].indexOf(" ")).trim();
					String value = update.split(" ")[update.split(" ").length - 1].trim();
					Properties props = null;
					if(gcm.getSpecies().containsKey(id)) {
						props = gcm.getSpecies().get(id);
					}
					else if(gcm.getPromoters().containsKey(id)) {
						props = gcm.getPromoters().get(id);
					}
					else if(gcm.getInfluences().containsKey(id)) {
						props = gcm.getInfluences().get(id);
					}
					if (props != null) {
						props.put(prop, value);
					}
				}
				else {
					id = update.split(" ")[0];
					String value = update.split(" ")[1].trim();
					gcm.setParameter(id, value);
				}
			}
		}
	}

	public void createSBML(String stem, String direct) {
		GCMFile gcm = new GCMFile(path);
		gcm.load(path + separator + refFile);
		HashMap<String, Properties> elements = this.gcm.getSpecies();
		for (String key : elements.keySet()) {
			for (Object prop : elements.get(key).keySet()) {
				if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.ID) && !prop.equals(GlobalConstants.TYPE)) {
					if (gcm.getSpecies().containsKey(key)) {
						gcm.getSpecies().get(key).put(prop, elements.get(key).get(prop));
					}
				}
			}			
		}
		elements = this.gcm.getInfluences();
		for (String key : elements.keySet()) {
			for (Object prop : elements.get(key).keySet()) {
				if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.PROMOTER) && !prop.equals(GlobalConstants.BIO) && !prop.equals(GlobalConstants.TYPE)) {
					if (gcm.getInfluences().containsKey(key)) {
						gcm.getInfluences().get(key).put(prop, elements.get(key).get(prop));
					}
				}
			}
		}
		elements = this.gcm.getPromoters();
		for (String key : elements.keySet()) {
			for (Object prop : elements.get(key).keySet()) {
				if (!prop.equals(GlobalConstants.NAME) && !prop.equals(GlobalConstants.ID)) {
					if (gcm.getPromoters().containsKey(key)) {
						gcm.getPromoters().get(key).put(prop, elements.get(key).get(prop));
					}
				}
			}
		}
		HashMap<String, String> params = this.gcm.getGlobalParameters();
		ArrayList<Object> remove = new ArrayList<Object>();
		for (String key : params.keySet()) {
			gcm.setParameter(key, params.get(key));
			remove.add(key);
		}
		try {
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((refFile + "\n").getBytes());
			for (String s : parameterChanges) {
				if (!s.trim().equals("")) {
					out.write((s + "\n").getBytes());
				}
			}
			out.write(("\n").getBytes());
			if (sbmlParamFile != null) {
				for (String s : sbmlParamFile.getElementChanges()) {
					out.write((s + "\n").getBytes());
				}
			}
			out.close();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to save parameter file.",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		try {
			ArrayList<String> dd = new ArrayList<String>();
			if (!direct.equals(".")) {
				String[] d = direct.split("_");
				for (int i = 0; i < d.length; i++) {
					if (!d[i].contains("=")) {
						String di = d[i];
						while (!d[i].contains("=")) {
							i++;
							di += "_" + d[i];
						}
						dd.add(di);
					}
					else {
						dd.add(d[i]);
					}
				}
				for (String di : dd) {
					if (di.contains("/")) {
						if (gcm.getPromoters().containsKey(di.split("=")[0].split("/")[0])) {
							Properties promoterProps = gcm.getPromoters().get(di.split("=")[0].split("/")[0]);
							promoterProps.put(di.split("=")[0].split("/")[1],
									di.split("=")[1]);
						}
						if (gcm.getSpecies().containsKey(di.split("=")[0].split("/")[0])) {
							Properties speciesProps = gcm.getSpecies().get(di.split("=")[0].split("/")[0]);
							speciesProps.put(di.split("=")[0].split("/")[1],
									di.split("=")[1]);
						}
						if (gcm.getInfluences().containsKey(di.split("=")[0].split("/")[0].replace("\"", ""))) {
							Properties influenceProps = gcm.getInfluences().get(
									di.split("=")[0].split("/")[0].replace("\"", ""));
							influenceProps.put(di.split("=")[0].split("/")[1]
									.replace("\"", ""), di.split("=")[1]);
						}
					}
					else {
						if (gcm.getGlobalParameters().containsKey(di.split("=")[0])) {
							gcm.getGlobalParameters().put(di.split("=")[0], di.split("=")[1]);
						}
						if (gcm.getParameters().containsKey(di.split("=")[0])) {
							gcm.getParameters().put(di.split("=")[0], di.split("=")[1]);
						}
					}
				}
			}
			direct = direct.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", "");
			if (direct.equals(".") && !stem.equals("")) {
				direct = "";
			}
			//gcm.save(path + separator + gcmname + ".gcm.temporary");
			GCMParser parser = new GCMParser(gcm, false);//path + separator + gcmname + ".gcm.temporary");
			//new File(path + separator + gcmname + ".gcm.temporary").delete();
			GeneticNetwork network = null;
			try {
				network = parser.buildNetwork();
			}
			catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (reb2sac != null)
				network.loadProperties(gcm, reb2sac.getGcmAbstractions(), reb2sac.getInterestingSpecies(), reb2sac.getProperty());
			else
				network.loadProperties(gcm);
			network.markAbstractable();
			if (!getSBMLFile().equals(none)) {
				//SBMLDocument d = Gui.readSBML(path + separator + getSBMLFile());
				SBMLDocument d = network.getSBML();
				for (String s : sbmlParamFile.getElementChanges()) {
					for (long i = d.getModel().getNumInitialAssignments() - 1; i >= 0; i--) {
						if (s.contains("=")) {
							String formula = SBMLutilities.myFormulaToString(((InitialAssignment) d.getModel()
									.getListOfInitialAssignments().get(i)).getMath());
							String sFormula = s.substring(s.indexOf('=') + 1).trim();
							sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
							sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
							if ((((InitialAssignment) d.getModel().getListOfInitialAssignments().get(i))
									.getSymbol()
									+ " = " + formula).equals(sFormula)) {
								d.getModel().getListOfInitialAssignments().remove(i);
							}
						}
					}
					for (long i = d.getModel().getNumConstraints() - 1; i >= 0; i--) {
						if (d.getModel().getListOfConstraints().get(i).getMetaId().equals(s)) {
							d.getModel().getListOfConstraints().remove(i);
						}
					}
					for (long i = d.getModel().getNumEvents() - 1; i >= 0; i--) {
						if (d.getModel().getListOfEvents().get(i).getId().equals(s)) {
							d.getModel().getListOfEvents().remove(i);
						}
					}
					for (long i = d.getModel().getNumRules() - 1; i >= 0; i--) {
						if (s.contains("=")) {
							String formula = SBMLutilities.myFormulaToString(((Rule) d.getModel().getListOfRules().get(i)).getMath());
							String sFormula = s.substring(s.indexOf('=') + 1).trim();
							sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
							sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
							if ((((Rule) d.getModel().getListOfRules().get(i)).getVariable() + " = " + formula).equals(sFormula)) {
								d.getModel().getListOfRules().remove(i);
							}
						}
					}
				}
				for (int i = 0; i < d.getModel().getNumCompartments(); i++) {
					for (String change : parameterChanges) {
						if (change.split(" ")[0].equals(d.getModel().getCompartment(i).getId())) {
							String[] splits = change.split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								d.getModel().getCompartment(i).setSize(Double.parseDouble(value));
							}
						}
					}
					for (String di : dd) {
						if (di.split("=")[0].split(" ")[0].equals(d.getModel().getCompartment(i).getId())) {
							String value = di.split("=")[1];
							d.getModel().getCompartment(i).setSize(Double.parseDouble(value));
						}
					}
				}
				for (int i = 0; i < d.getModel().getNumSpecies(); i++) {
					for (String change : parameterChanges) {
						if (change.split(" ")[0].equals(d.getModel().getSpecies(i).getId())) {
							String[] splits = change.split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								if (d.getModel().getSpecies(i).isSetInitialAmount()) {
									d.getModel().getSpecies(i).setInitialAmount(Double.parseDouble(value));
								}
								else {
									d.getModel().getSpecies(i).setInitialConcentration(Double.parseDouble(value));
								}
							}
						}
					}
					for (String di : dd) {
						if (di.split("=")[0].split(" ")[0].equals(d.getModel().getSpecies(i).getId())) {
							String value = di.split("=")[1];
							if (d.getModel().getSpecies(i).isSetInitialAmount()) {
								d.getModel().getSpecies(i).setInitialAmount(Double.parseDouble(value));
							}
							else {
								d.getModel().getSpecies(i).setInitialConcentration(Double.parseDouble(value));
							}
						}
					}
				}
				for (int i = 0; i < d.getModel().getNumParameters(); i++) {
					for (String change : parameterChanges) {
						if (change.split(" ")[0].equals(d.getModel().getParameter(i).getId())) {
							String[] splits = change.split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								d.getModel().getParameter(i).setValue(Double.parseDouble(value));
							}
						}
					}
					for (String di : dd) {
						if (di.split("=")[0].split(" ")[0].equals(d.getModel().getParameter(i).getId())) {
							String value = di.split("=")[1];
							d.getModel().getParameter(i).setValue(Double.parseDouble(value));
						}
					}
				}
				for (int i = 0; i < d.getModel().getNumReactions(); i++) {
					Reaction reaction = d.getModel().getReaction(i);
					ListOf parameters = reaction.getKineticLaw().getListOfParameters();
					for (int j = 0; j < reaction.getKineticLaw().getNumParameters(); j++) {
						Parameter paramet = ((Parameter) (parameters.get(j)));
						for (String change : parameterChanges) {
							if (change.split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
								String[] splits = change.split(" ");
								if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
									String value = splits[splits.length - 1];
									paramet.setValue(Double.parseDouble(value));
								}
							}
						}
						for (String di : dd) {
							if (di.split("=")[0].split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
								String value = di.split("=")[1];
								paramet.setValue(Double.parseDouble(value));
							}
						}
					}
				}
				network.mergeSBML(path + separator + simName + separator + stem + direct + separator + gcmname + ".xml", d);
			}
			else {
				network.mergeSBML(path + separator + simName + separator + stem + direct
						+ separator + gcmname + ".xml");
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create sbml file.",
					"Error Creating File", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	public String getRefFile() {
		return refFile;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
		else if (o instanceof JComboBox && !lock
				&& !gcm.getSBMLFile().equals(sbmlFiles.getSelectedItem())) {
			dirty.setValue(true);
		}
		// System.out.println(o);
	}

	public synchronized void lock() {
		lock = true;
	}

	public synchronized void unlock() {
		lock = false;
	}

	private void buildGui(String filename, String path, boolean textBased) {
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		//JPanel mainPanelCenterDown = new JPanel(new BorderLayout());
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
			
		JLabel sbmlFileLabel = new JLabel("SBML File:");
		sbmlFiles = new JComboBox();
		sbmlFiles.addActionListener(this);
		if (paramsOnly) {
			sbmlFileLabel.setEnabled(false);
			sbmlFiles.setEnabled(false);
		}
		reloadFiles();
		//mainPanelNorth.add(sbmlFileLabel);
		//mainPanelNorth.add(sbmlFiles);
		
		// create the modelview2 (jgraph) panel
		modelPanel = new ModelPanel(gcm.getSBMLDocument(),dirty,paramsOnly);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		JPanel paramPanel = new JPanel(new BorderLayout());
		paramPanel.add(modelPanel, "North");
		JPanel propPanel = new JPanel(new BorderLayout());
		propPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		JTabbedPane tab = new JTabbedPane();
		ArrayList<String>usedIDs = gcm.getUsedIDs();
		
		String file = filename.replace(".gcm", ".xml");
		
		JComboBox compartmentList = MySpecies.createCompartmentChoices(gcm.getSBMLDocument());
		
		compartmentPanel = new Compartments(gcm.getSBMLDocument(),usedIDs,dirty, paramsOnly,getParams,file,
					parameterChanges,false,compartmentList);
		reactionPanel = new Reactions(biosim,gcm.getSBMLDocument(),usedIDs,dirty,
				paramsOnly,getParams,file,parameterChanges);
		speciesPanel = new MySpecies(biosim,gcm.getSBMLDocument(),usedIDs,dirty,
				paramsOnly,getParams,file,parameterChanges,true);

		gcm.setSpeciesPanel(speciesPanel);
		gcm.setReactionPanel(reactionPanel);

		if (textBased) {
			if (!gcm.getGrid().isEnabled()) 
				tab.addTab("Compartments", compartmentPanel);
			tab.addTab("Species", speciesPanel);
			tab.addTab("Reactions", reactionPanel);
		} 
		else {
			this.schematic = new Schematic(gcm, biosim, this, true, null,compartmentPanel,reactionPanel,compartmentList);
			tab.addTab("Schematic", schematic);
			//if (gcm.getSBMLDocument().getModel().getNumCompartments() > 1) {
				if (!gcm.getGrid().isEnabled()) 
					tab.addTab("Compartments", compartmentPanel);
			//}
			tab.addTab("Parameters", paramPanel);
		}
		
		Functions functionPanel = new Functions(gcm.getSBMLDocument(),usedIDs,dirty);
		Units unitPanel = new Units(biosim,gcm.getSBMLDocument(),usedIDs,dirty);
		JPanel defnPanel = new JPanel(new BorderLayout());
		defnPanel.add(mainPanelNorth, "North");
		defnPanel.add(functionPanel,"Center");
		defnPanel.add(unitPanel,"South");
		tab.addTab("Definitions", defnPanel);

		if (gcm.getSBMLDocument().getLevel() < 3) {
			CompartmentTypes compTypePanel = new CompartmentTypes(biosim,gcm.getSBMLDocument(),usedIDs,dirty);
			SpeciesTypes specTypePanel = new SpeciesTypes(biosim,gcm.getSBMLDocument(),usedIDs,dirty);
			JPanel typePanel = new JPanel(new BorderLayout());
			typePanel.add(mainPanelNorth, "North");
			typePanel.add(compTypePanel,"Center");
			typePanel.add(specTypePanel,"South");
			tab.addTab("Types", typePanel);
		}

		InitialAssignments initialsPanel = new InitialAssignments(biosim,gcm.getSBMLDocument(),dirty);
		Rules rulesPanel = new Rules(biosim,gcm.getSBMLDocument(),dirty);
		compartmentPanel.setPanels(initialsPanel, rulesPanel);
		functionPanel.setPanels(initialsPanel, rulesPanel);
		speciesPanel.setPanels(initialsPanel, rulesPanel);
		reactionPanel.setPanels(initialsPanel, rulesPanel);
		JPanel assignPanel = new JPanel(new BorderLayout());
		assignPanel.add(mainPanelNorth, "North");
		assignPanel.add(initialsPanel,"Center");
		assignPanel.add(rulesPanel,"South");
		tab.addTab("Assignments", assignPanel);

		Events eventPanel = new Events(biosim,gcm.getSBMLDocument(),usedIDs,dirty);
		tab.addTab("Properties", propPanel);
		tab.addTab("Events", eventPanel);
		//tab.addTab("Main Elements", mainPanel);
 		//tab.addTab("Model View", grappaPanel);
		setLayout(new BorderLayout());
		if (paramsOnly) {
			add(paramPanel, BorderLayout.CENTER);
		}
		else {
			add(tab, BorderLayout.CENTER);
		}
		//add(mainPanelCenterDown, BorderLayout.SOUTH);
		
		// When the Graphical View panel gets clicked on, tell it to display itself.
		/*
		tab.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JTabbedPane selectedTab = (JTabbedPane)(e.getSource());
				JPanel selectedPanel = (JPanel)selectedTab.getComponent(selectedTab.getSelectedIndex());
				String className = selectedPanel.getClass().getName();
				// The new Schematic
				if(className.indexOf("Schematic") >= 0){
					((Schematic)selectedPanel).display();
				}
			}
		});
        */
		promoters = new PropertyList("Promoter List");
		EditButton addInit = new EditButton("Add Promoter", promoters);
		RemoveButton removeInit = new RemoveButton("Remove Promoter", promoters);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
		}
		EditButton editInit = new EditButton("Edit Promoter", promoters);
		if (paramsOnly) {
			Set<String> prom = gcm.getPromoters().keySet();
			ArrayList<String> proms = new ArrayList<String>();
			for (String s : prom) {
				proms.add(s);
			}
			for (String s : getParams) {
				if (s.contains("/") && proms.contains(s.split("/")[0].trim())) {
					proms.remove(s.split("/")[0].trim());
					proms.add(s.split("/")[0].trim() + " Modified");
					parameterChanges.add(s);
				}
			}
			promoters.addAllItem(proms);
		}
		else {
			promoters.addAllItem(gcm.getPromoters().keySet());
		}

		JPanel initPanel = Utility.createPanel(this, "Promoters", promoters, addInit, removeInit,
				editInit);
		mainPanelCenterCenter.add(initPanel);

		species = new PropertyList("Species List");
		addInit = new EditButton("Add Species", species);
		removeInit = new RemoveButton("Remove Species", species);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
		}
		editInit = new EditButton("Edit Species", species);
		if (paramsOnly) {
			Set<String> spec = gcm.getSpecies().keySet();
			ArrayList<String> specs = new ArrayList<String>();
			for (String s : spec) {
				specs.add(s);
			}
			for (String s : getParams) {
				if (s.contains("/") && specs.contains(s.split("/")[0].trim())) {
					specs.remove(s.split("/")[0].trim());
					specs.add(s.split("/")[0].trim() + " Modified");
					parameterChanges.add(s);
				}
			}
			species.addAllItem(specs);
		}
		else {
			species.addAllItem(gcm.getSpecies().keySet());
		}

		initPanel = Utility.createPanel(this, "Species", species, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);

		influences = new PropertyList("Influence List");
		addInit = new EditButton("Add Influence", influences);
		removeInit = new RemoveButton("Remove Influence", influences);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
		}
		editInit = new EditButton("Edit Influence", influences);
		if (paramsOnly) {
			Set<String> influe = gcm.getInfluences().keySet();
			ArrayList<String> influes = new ArrayList<String>();
			for (String s : influe) {
				influes.add(s);
			}
			for (String s : getParams) {
				if (s.contains("\"") && influes.contains(s.split("\"")[1].trim())) {
					influes.remove(s.split("\"")[1].trim());
					influes.add(s.split("\"")[1].trim() + " Modified");
					parameterChanges.add(s);
				}
			}
			influences.addAllItem(influes);
		}
		else {
			influences.addAllItem(gcm.getInfluences().keySet());
		}

		initPanel = Utility.createPanel(this, "Influences", influences, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);

		components = new PropertyList("Component List");
		addInit = new EditButton("Add Component", components);
		removeInit = new RemoveButton("Remove Component", components);
		editInit = new EditButton("Edit Component", components);
		for (String s : gcm.getComponents().keySet()) {
			if (gcm.getComponents().get(s).getProperty("gcm") != null) {
				components.addItem(s + " "
						+ gcm.getComponents().get(s).getProperty("gcm").replace(".gcm", "") + " "
						+ gcm.getComponentPortMap(s));
			}
		}
		initPanel = Utility.createPanel(this, "Components", components, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);
		
		parameters = new PropertyList("Parameter List");
		editInit = new EditButton("Edit Parameter", parameters);
		// parameters.addAllItem(gcm.getParameters().keySet());
		parameters.addAllItem(generateParameters());
		initPanel = Utility.createPanel(this, "GCM Parameters", parameters, null, null, editInit);
		paramPanel.add(initPanel, "Center");
		parametersPanel = new Parameters(gcm.getSBMLDocument(),usedIDs,dirty,
				paramsOnly,getParams,path + separator + file,parameterChanges);
		parametersPanel.setPanels(initialsPanel, rulesPanel);
		paramPanel.add(parametersPanel, "South");
		
		conditions = new PropertyList("Property List");
		addInit = new EditButton("Add Property", conditions);
		removeInit = new RemoveButton("Remove Property", conditions);
		editInit = new EditButton("Edit Property", conditions);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
			editInit.setEnabled(false);
		}
		for (String s : gcm.getConditions()) {
			conditions.addItem(s);
		}
		initPanel = Utility.createPanel(this, "Properties", conditions, addInit, removeInit, editInit);
		propPanel.add(initPanel, "Center");
		Constraints consPanel = new Constraints(biosim,gcm.getSBMLDocument(),usedIDs,dirty);
		propPanel.add(consPanel, "South");
	}

	public void reloadFiles() {
		lock();
		// sbmlFiles.removeAll();
		String[] sbmlList = Utility.getFiles(path, ".sbml");
		String[] xmlList = Utility.getFiles(path, ".xml");

		String[] temp = new String[sbmlList.length + xmlList.length];
		System.arraycopy(sbmlList, 0, temp, 0, sbmlList.length);
		System.arraycopy(xmlList, 0, temp, sbmlList.length, xmlList.length);

		Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
		String[] allList = new String[temp.length + 1];
		System.arraycopy(temp, 0, allList, 1, temp.length);
		allList[0] = none;

		DefaultComboBoxModel model = new DefaultComboBoxModel(allList);

		sbmlFiles.setModel(model);

		sbmlFiles.setSelectedItem(none);

		sbmlFiles.setSelectedItem(gcm.getSBMLFile());
		if (!gcm.getSBMLFile().equals("")) {
			gcm.setSBMLDocument(Gui.readSBML(path + separator + gcm.getSBMLFile()));
			if (sbmlFiles.getSelectedItem().equals(none)) {
				Utility.createErrorMessage("Warning: Missing File", "Unable to find SBML file "
						+ gcm.getSBMLFile() + ".  Creating a default SBML file");
				SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
				Model m = document.createModel();
				document.setModel(m);
				m.setId(gcmname);
				Compartment c = m.createCompartment();
				c.setId("default");
				gcm.getUsedIDs().add("default");
				c.setSize(1);
				c.setSpatialDimensions(3);
				c.setConstant(true);
				String[] species = gcm.getSpeciesAsArray();
				for (int i = 0; i < species.length; i++) {
					Species s = m.createSpecies();
					s.setId(species[i]);
					s.setCompartment("default");
					s.setBoundaryCondition(false);
					s.setConstant(false);
					s.setInitialAmount(0);
					s.setHasOnlySubstanceUnits(true);
				}
				SBMLutilities.addRandomFunctions(document);
				SBMLWriter writer = new SBMLWriter();
				writer.writeSBML(document, path + separator + gcmname + ".xml");
				biosim.addToTreeNoUpdate(gcmname + ".xml");
				setDirty(true);
			} else {
				SBMLDocument document = Gui.readSBML(path + separator + gcm.getSBMLFile());
				SBMLutilities.addRandomFunctions(document);
				SBMLWriter writer = new SBMLWriter();
				writer.writeSBML(document, path + separator + gcmname + ".xml");
				biosim.addToTreeNoUpdate(gcmname + ".xml");
				setDirty(true);
			}
		} else {
			SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			Model m = document.createModel();
			document.setModel(m);
			m.setId(gcmname);
			Compartment c = m.createCompartment();
			c.setId("default");
			gcm.getUsedIDs().add("default");
			c.setSize(1);
			c.setSpatialDimensions(3);
			c.setConstant(true);
			String[] species = gcm.getSpeciesAsArray();
			for (int i = 0; i < species.length; i++) {
				Species s = m.createSpecies();
				s.setId(species[i]);
				gcm.getUsedIDs().add(species[i]);
				s.setCompartment("default");
				s.setBoundaryCondition(false);
				s.setConstant(false);
				s.setInitialAmount(0);
				s.setHasOnlySubstanceUnits(true);
			}
			SBMLutilities.addRandomFunctions(document);
			SBMLWriter writer = new SBMLWriter();
			writer.writeSBML(document, path + separator + gcmname + ".xml");
			biosim.addToTreeNoUpdate(gcmname + ".xml");
			setDirty(true);
		}
		gcm.setSBMLFile(gcmname+".xml");
		sbmlFiles.setSelectedItem(gcm.getSBMLFile());
		if (gcm.getSBMLDocument()==null) {
			gcm.setSBMLDocument(Gui.readSBML(path + separator + gcm.getSBMLFile()));
		} else {
			gcm.getSBMLDocument().setModel(Gui.readSBML(path + separator + gcm.getSBMLFile()).getModel());
		}
		
		unlock();
	}

	public void reloadParameters() {
		parameters.removeAllItem();
		parameters.addAllItem(generateParameters());
	}

	private Set<String> generateParameters() {
		HashSet<String> results = new HashSet<String>();
		if (paramsOnly) {
			HashMap<String, String> params = gcm.getGlobalParameters();
			ArrayList<Object> remove = new ArrayList<Object>();
			for (String key : params.keySet()) {
				remove.add(key);
			}
			for (Object prop : remove) {
				params.remove(prop);
			}
			for (String update : parameterChanges) {
				String id;
				if (!update.contains("/")) {
					id = update.split(" ")[0];
					String value = update.split(" ")[1].trim();
					gcm.setParameter(id, value);
				}
			}
		}
		for (String s : gcm.getParameters().keySet()) {
			if (!s.equals(GlobalConstants.KBIO_STRING) && !s.equals(GlobalConstants.KASSOCIATION_STRING)
					&& !s.equals(GlobalConstants.MAX_DIMER_STRING)) {
				if (gcm.getGlobalParameters().containsKey(s)) {
					if (gcm.getParameter(s).contains("(")) {
						results.add(CompatibilityFixer.getGuiName(s) + " ("
								+ s + "), Sweep, "
								+ gcm.getParameter(s));
					}
					else {
						if (paramsOnly) {
							results.add(CompatibilityFixer.getGuiName(s) + " ("
									+ s + "), Modified, "
									+ gcm.getParameter(s));
						}
						else {
							results.add(CompatibilityFixer.getGuiName(s) + " ("
									+ s + "), Custom, "
									+ gcm.getParameter(s));
						}
					}
				}
				else {
					if (paramsOnly) {
						GCMFile refGCM = new GCMFile(path);
						refGCM.load(path + separator + refFile);
						if (refGCM.getGlobalParameters().containsKey(s)) {
							results.add(CompatibilityFixer.getGuiName(s) + " ("
									+ s + "), Custom, "
									+ gcm.getParameter(s));
						}
						else {
							results.add(CompatibilityFixer.getGuiName(s) + " ("
									+ s + "), Default, "
									+ gcm.getParameter(s));
						}
					}
					else {
						results.add(CompatibilityFixer.getGuiName(s) + " ("
								+ s + "), Default, "
								+ gcm.getParameter(s));
					}
				}
			}
		}
		return results;
	}

	// Internal private classes used only by the gui
	private class SaveButton extends AbstractRunnableNamedButton {
		public SaveButton(String name, JTextField fieldNameField) {
			super(name);
			this.fieldNameField = fieldNameField;
		}

		public void run() {
			save(getName());
		}

		private JTextField fieldNameField = null;
	}

	private class RemoveButton extends AbstractRunnableNamedButton {
		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			//dirty = true;
			if (getName().contains("Influence")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removeInfluenceCheck(name)) {
						gcm.removeInfluence(name);
						list.removeItem(name);
					}
				}
			}
			else if (getName().contains("Species")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removeSpeciesCheck(name)) {
						gcm.removeSpecies(name);
						list.removeItem(name);
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "Cannot remove species " + name
								+ " because it is currently in other reactions and/or components.");
					}
				}
			}
			else if (getName().contains("Promoter")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removePromoterCheck(name)) {
						gcm.removePromoter(name);
						list.removeItem(name);
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "Cannot remove promoter " + name
								+ " because it is currently in other reactions");
					}
				}
			}
			else if (getName().contains("Property")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					gcm.removeCondition(name);
					list.removeItem(name);
				}
			}
			else if (getName().contains("Component")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					String comp = name.split(" ")[0];
					if (gcm.removeComponentCheck(comp)) {
						gcm.removeComponent(comp);
						list.removeItem(name);
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "Cannot remove component "
								+ name.split(" ")[0] + " because it is currently in other reactions");
					}
				}
			}
		}

		private PropertyList list = null;
	}

	private class EditButton extends AbstractRunnableNamedButton {
		public EditButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			new EditCommand(getName(), list).run();
		}

		private PropertyList list = null;
	}

	private class EditCommand implements Runnable {
		public EditCommand(String name, PropertyList list) {
			this.name = name;
			this.list = list;
		}

		public void run() {
			if (name == null || name.equals("")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			if (list.getSelectedValue() == null && getName().contains("Edit")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			//dirty = true;
			if (getName().contains("Species")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ").length > 1) {
						selected = selected.split(" ")[0];
					}
				}

				launchSpeciesPanel(selected, false);

			}
			else if (getName().contains("Influence")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ")[selected.split(" ").length - 1].equals("Modified")) {
						selected = selected.substring(0, selected.length() - 9);
					}
				}
				launchInfluencePanel(selected);
			}
			else if (getName().contains("Promoter")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ").length > 1) {
						selected = selected.split(" ")[0];
					}
				}
				launchPromoterPanel(selected);

			}
			else if (getName().contains("Property")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				ConditionsPanel panel = new ConditionsPanel(selected, list, gcm, paramsOnly,gcmEditor);
			}
			else if (getName().contains("Component")) {
				displayChooseComponentDialog(getName().contains("Edit"), list, false);
			}
			else if (getName().contains("Parameter")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				GCMFile refGCM = null;
				if (paramsOnly) {
					refGCM = new GCMFile(path);
					refGCM.load(path + separator + refFile);
				}
				ParameterPanel panel = new ParameterPanel(selected, list, gcm, paramsOnly, refGCM, gcmEditor);
				if (paramsOnly) {
					String updates = panel.updates();
					if (!updates.equals("")) {
						for (int i = parameterChanges.size() - 1; i >= 0; i--) {
							if (parameterChanges.get(i).startsWith(updates.split(" ")[0])) {
								parameterChanges.remove(i);
							}
						}
						if (updates.contains(" ")) {
							for (String s : updates.split("\n")) {
								parameterChanges.add(s);
							}
						}
					}
				}
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}
	
	/**
	 * launches the promoter panel to edit the promoter with the given id.
	 * If no id is given, then it edits a new promoter.
	 * @param id
	 * @return
	 */
	public PromoterPanel launchPromoterPanel(String id){
		GCMFile refGCM = null;
		if (paramsOnly) {
			refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
		}
		PromoterPanel panel = new PromoterPanel(id, promoters, influences, gcm, paramsOnly, refGCM, gcmEditor);	
		
		if (paramsOnly) {
			String updates = panel.updates();
			if (!updates.equals("")) {
				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
						parameterChanges.remove(i);
					}
				}
				if (updates.contains(" ")) {
					for (String s : updates.split("\n")) {
						parameterChanges.add(s);
					}
				}
			}
		}
		
		return panel;
	}

	public SpeciesPanel launchSpeciesPanel(String id, boolean inTab){
		GCMFile refGCM = null;
		if (paramsOnly) {
			refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
		}
		SpeciesPanel panel = new SpeciesPanel(id, species, influences, conditions, 
				components, gcm, paramsOnly, refGCM, this, inTab);
		
		if (paramsOnly) {
			String updates = panel.updates();
			if (!updates.equals("")) {
				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
						parameterChanges.remove(i);
					}
				}
				if (updates.contains(" ")) {
					for (String s : updates.split("\n")) {
						parameterChanges.add(s);
					}
				}
			}
		}
		
		return panel;
	}
	
	public InfluencePanel launchInfluencePanel(String id){
		GCMFile refGCM = null;
		if (paramsOnly) {
			refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
		}
		InfluencePanel panel = new InfluencePanel(id, influences, gcm, paramsOnly, refGCM, gcmEditor);
		
		if (paramsOnly) {
			String updates = panel.updates();
			if (!updates.equals("")) {
				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
						parameterChanges.remove(i);
					}
				}
				if (!updates.endsWith("/")) {
					for (String s : updates.split("\n")) {
						parameterChanges.add(s);
					}
				}
			}
		}
		return panel;
	}
	
	public String launchComponentPanel(String id){
		GCMFile refGCM = null;
		if (paramsOnly) {
			refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
		}
		// TODO: This is a messy way to do things. We set the selected component in the list
		// and then call displayChooseComponentDialog(). This makes for tight coupling with the
		// component list.
		for(int i=0; i<this.components.getModel().getSize(); i++){
			String componentsListRow = this.components.getModel().getElementAt(i).toString();
			String componentsListId = componentsListRow.split(" ")[0];
			if(componentsListId.equals(id)){
				this.components.setSelectedIndex(i);
				break;
			}
		}
		return displayChooseComponentDialog(true, this.components, false);
	}
	
	/**
	 * launches a panel for grid creation
	 */
	public void launchGridPanel() {
		
		//static method that builds the grid panel
		//the false field means to open the grid creation panel
		//and not the grid editing panel
		boolean created = GridPanel.showGridPanel(this, gcm, false);
		
		//if the grid is built, then draw it and so on
		if (created) {
			
			this.setDirty(true);
			this.refresh();
			schematic.getGraph().buildGraph();
			schematic.display();
			gcm.makeUndoPoint();
		}
	}
	
	public SchemeChooserPanel getSchemeChooserPanel(
			String cellID, MovieContainer movieContainer, boolean inTab) {
		
		return new SchemeChooserPanel(cellID, movieContainer, inTab);
	}
	
	
	public boolean checkNoComponentLoop(String gcm, String checkFile) {
		boolean check = true;
		GCMFile g = new GCMFile(path);
		g.load(path + separator + checkFile);
		for (String comp : g.getComponents().keySet()) {
			String compGCM = g.getComponents().get(comp).getProperty("gcm");
			if (compGCM.equals(gcm)) {
				return false;
			}
			else {
				check = checkNoComponentLoop(gcm, compGCM);
			}
		}
		return check;
	}
	
	
	/**
	 * @return a list of all gcm files that can be included.
	 */
	public ArrayList<String> getComponentsList(){

		// get a list of components
		ArrayList<String> components = new ArrayList<String>();
		for (String s : new File(path).list()) {
			if (s.endsWith(".gcm") && !s.equals(filename) && checkNoComponentLoop(filename, s)) {
				components.add(s);
			}
		}
		
		// I think this sorts them
		int i, j;
		String index;
		for (i = 1; i < components.size(); i++) {
			index = components.get(i);
			j = i;
			while ((j > 0) && components.get(j - 1).compareToIgnoreCase(index) > 0) {
				components.set(j, components.get(j - 1));
				j = j - 1;
			}
			components.set(j, index);
		}
		
		return components;
	}
	
	/*
	 * 
	 * Displays the "Choose Component" dialog and then adds the component afterward.
	 * @param tryEdit: if true then try to bring up the edit window if a component is selected.
	 * @param list: The PropertiesList. If left null then the gcm2sbmleditor's component
	 * 				list will be used.
	 * @param createUsingDefaults:  If true then a component will be created with a basic name and
	 * 				no port mappings. Otherwise the user will be asked for 
	 * 				the name and mappings.
	 * 
	 * @return: the id of the component that was edited or created.
	 */
	public String displayChooseComponentDialog(boolean tryEdit, PropertyList list, boolean createUsingDefaults){
		
		String outID = null;
		
		if(list == null)
			list = this.components;
		String selected = null;
		String comp = null;
		if (list.getSelectedValue() != null && tryEdit) {
			selected = list.getSelectedValue().toString();
			comp = selected.split(" ")[1] + ".gcm";
		}
		else {
			ArrayList<String> components = getComponentsList();

			if (components.size() == 0) {
				comp = null;
				JOptionPane.showMessageDialog(Gui.frame,
						"There aren't any other gcms to use as components."
								+ "\nCreate a new gcm or import a gcm into the project first.",
						"Add Another GCM To The Project", JOptionPane.ERROR_MESSAGE);
			}
			else {
				comp = (String) JOptionPane.showInputDialog(Gui.frame,
						"Choose a gcm to use as a component:", "Component Editor",
						JOptionPane.PLAIN_MESSAGE, null, components.toArray(new String[0]), null);
			}
		}
		if (comp != null && !comp.equals("")) {
			GCMFile getSpecs = new GCMFile(path);
			getSpecs.load(path + separator + comp);
			String oldPort = null;
			if (selected != null) {
				oldPort = selected.substring(selected.split(" ")[0].length()
						+ selected.split(" ")[1].length() + 2);
				selected = selected.split(" ")[0];
			}

			ArrayList<String> in = getSpecs.getInputSpecies();
			ArrayList<String> out = getSpecs.getOutputSpecies();
			String[] inputs = in.toArray(new String[0]);
			String[] outputs = out.toArray(new String[0]);
			int i, j;
			String index;
			for (i = 1; i < inputs.length; i++) {
				index = inputs[i];
				j = i;
				while ((j > 0) && inputs[j - 1].compareToIgnoreCase(index) > 0) {
					inputs[j] = inputs[j - 1];
					j = j - 1;
				}
				inputs[j] = index;
			}
			for (i = 1; i < outputs.length; i++) {
				index = outputs[i];
				j = i;
				while ((j > 0) && outputs[j - 1].compareToIgnoreCase(index) > 0) {
					outputs[j] = outputs[j - 1];
					j = j - 1;
				}
				outputs[j] = index;
			}
			
			if(createUsingDefaults){
				Properties properties = new Properties();
				properties.put("gcm", comp);
				outID = gcm.addComponent(null, properties);
			}else{
				new ComponentsPanel(selected, list, influences, gcm,
						inputs, outputs, comp, oldPort, paramsOnly, gcmEditor);
				outID = selected;
			}

		}
		return outID;
	}
	
	public void setSBMLParamFile(SBML_Editor sbmlParamFile) {
		this.sbmlParamFile = sbmlParamFile;
	}
	
	public Gui getGui() {
		return biosim;
	}

	public Reb2Sac getReb2Sac() {
		return reb2sac;
	}
	
	private boolean lock = false;

	private String[] options = { "Ok", "Cancel" };

	private PropertyList species = null;

	private PropertyList influences = null;

	private PropertyList promoters = null;

	private PropertyList parameters = null;

	private PropertyList components = null;
	
	private PropertyList conditions = null;

	private JComboBox sbmlFiles = null;

	private String path = null;

	private static final String none = "--none--";

	private Gui biosim = null;

	private Log log = null;

	private MutableBoolean dirty = new MutableBoolean(false);
}
