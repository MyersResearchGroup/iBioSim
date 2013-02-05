package biomodel.gui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.Gui;
import main.Log;
import main.util.ExampleFileFilter;
import main.util.MutableBoolean;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ExternalModelDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.Submodel;
import org.sbolstandard.core.DnaComponent;

import analysis.ConstraintTermThread;
import analysis.AnalysisView;
import analysis.AnalysisThread;
import biomodel.annotation.AnnotationUtility;
import biomodel.gui.movie.MovieContainer;
import biomodel.gui.movie.SchemeChooserPanel;
import biomodel.gui.schematic.Schematic;
import biomodel.gui.textualeditor.CompartmentTypes;
import biomodel.gui.textualeditor.Compartments;
import biomodel.gui.textualeditor.Constraints;
import biomodel.gui.textualeditor.ElementsPanel;
import biomodel.gui.textualeditor.Events;
import biomodel.gui.textualeditor.Functions;
import biomodel.gui.textualeditor.InitialAssignments;
import biomodel.gui.textualeditor.ModelPanel;
import biomodel.gui.textualeditor.MySpecies;
import biomodel.gui.textualeditor.Parameters;
import biomodel.gui.textualeditor.Reactions;
import biomodel.gui.textualeditor.Rules;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.gui.textualeditor.SpeciesTypes;
import biomodel.gui.textualeditor.Units;
import biomodel.network.GeneticNetwork;
import biomodel.parser.BioModel;
import biomodel.parser.GCMParser;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLFileManager;
import sbol.SBOLIdentityManager;
import sbol.SBOLAssemblyGraph;
import sbol.SBOLAssembler;
import sbol.SequenceTypeValidator;

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
public class ModelEditor extends JPanel implements ActionListener, MouseListener, ChangeListener {

	private static final long serialVersionUID = 1L;

	private String filename = "";

	private String modelId = "";

	private BioModel biomodel = null;

	private boolean paramsOnly;

	private ArrayList<String> parameterChanges;
	
	private ArrayList<String> getParams;
	
	private String paramFile, refFile, simName;

	private AnalysisView reb2sac;
	
	private ElementsPanel elementsPanel;
	
	private String separator;
	
	private ModelPanel modelPanel;
	
	private Schematic schematic;

	private Compartments compartmentPanel;

	private Functions functionPanel;
	
	private MySpecies speciesPanel;
	
	private Parameters parametersPanel;
	
	private Reactions reactionPanel;

	private Units unitPanel;
	
	private Rules rulesPanel;
	
	private Events eventPanel;
	
	private Constraints consPanel;
	
	private boolean lema;
	
	public ModelEditor(String path) throws Exception {
		this(path, null, null, null, false, null, null, null, false, false, false);
	}

	public ModelEditor(String path, String filename, Gui biosim, Log log, boolean paramsOnly,
			String simName, String paramFile, AnalysisView reb2sac, boolean textBased, boolean grid, boolean lema) throws Exception {
		super();
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
		this.textBased = textBased;
		this.lema = lema;
		elementsPanel = null;
		getParams = new ArrayList<String>();
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
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
		biomodel = new BioModel(path);
		if (filename != null) {
			biomodel.load(path + separator + filename);
			this.filename = filename;
			this.modelId = filename.replace(".gcm", "").replace(".xml", "");
		}
		else {
			this.filename = "";
		}
		if (paramsOnly) {
			loadParams();
		}
		if (grid) {
			launchGridPanel();
			rebuildGui();
		} else {
			buildGui();
		}
	}

	public String getFilename() {
		return filename;
	}
	
	public String getPath() {
		return path;
	}

	public void reload(String newName) {
		filename = newName + ".xml";
		modelId = newName;
		biomodel.load(path + separator + newName + ".xml");
		if (paramsOnly) {
			/*
			GCMFile refGCM = new GCMFile(path);
			refGCM.load(path + separator + refFile);
			HashMap<String, String> params = refGCM.getGlobalParameters();
			for (String key : params.keySet()) {
				gcm.setDefaultParameter(key, params.get(key));
			}
			 */
		}
		//GCMNameTextField.setText(newName);
	}
	
	public void renameComponents(String oldname, String newName) {
		for (long i = 0; i < biomodel.getSBMLComp().getNumExternalModelDefinitions(); i++) {
			ExternalModelDefinition extModel = biomodel.getSBMLComp().getExternalModelDefinition(i);
			if (extModel.getId().equals(oldname)) {
				extModel.setId(newName);
				extModel.setSource("file://"+newName+".xml");
			}
		}
		for (long i = 0; i < biomodel.getSBMLCompModel().getNumSubmodels(); i++) {
			Submodel submodel = biomodel.getSBMLCompModel().getSubmodel(i);
			if (submodel.getModelRef().equals(oldname)) {
				submodel.setModelRef(newName);
			}
		}
		ArrayList<String> comps = new ArrayList<String>();
		for (long i = 0; i < biomodel.getSBMLCompModel().getNumSubmodels(); i++) {
			Submodel submodel = biomodel.getSBMLCompModel().getSubmodel(i);
			comps.add(submodel.getId() + " " + submodel.getModelRef() + " " + biomodel.getComponentPortMap(submodel.getId()));
		}
		components.removeAllItem();
		components.addAllItem(comps);
		schematic.getGraph().buildGraph();
	}
	
	public void refresh() {
		refreshComponentsList();		
		reloadParameters();
		if (paramsOnly) {
			/*
			GCMParser parser = new GCMParser(path + separator + refFile);
			GeneticNetwork network = parser.buildNetwork();
			GeneticNetwork.setRoot(path + separator);
			network.mergeSBML(path + separator + simName + separator + gcmname + ".xml");
			*/
			//reb2sac.updateSpeciesList();
			//biomodel.reloadSBMLFile();
			compartmentPanel.refreshCompartmentPanel(biomodel);
			speciesPanel.refreshSpeciesPanel(biomodel);
			parametersPanel.refreshParameterPanel(biomodel);
			reactionPanel.refreshReactionPanel(biomodel);
		} else {
			compartmentPanel.refreshCompartmentPanel(biomodel);
			parametersPanel.refreshParameterPanel(biomodel);
			speciesPanel.refreshSpeciesPanel(biomodel);
			reactionPanel.refreshReactionPanel(biomodel);
			rulesPanel.refreshRulesPanel();
			consPanel.refreshConstraintsPanel();
			eventPanel.refreshEventsPanel();
			functionPanel.refreshFunctionsPanel();
			unitPanel.refreshUnitsPanel();
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Object o = e.getSource();
			if (o instanceof PropertyList) {
				PropertyList list = (PropertyList) o;
				new EditCommand("Edit " + list.getName(), list).run();
			}
		}
		
		//System.err.println("reloading");
		schematic.reloadGrid();
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

	public MutableBoolean getDirty() {
		return dirty;
	}

	public boolean isParamsOnly() {
		return paramsOnly;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty.setValue(dirty);
		biosim.markTabDirty(dirty);
	}

	public BioModel getBioModel() {
		return biomodel;
	}
	
//	public HashSet<String> getSbolFiles() {
//		HashSet<String> filePaths = new HashSet<String>();
//		TreeModel tree = getGui().getFileTree().tree.getModel();
//		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++) {
//			String fileName = tree.getChild(tree.getRoot(), i).toString();
//			if (fileName.endsWith(".sbol"))
//				filePaths.add(getGui().getRoot() + File.separator + fileName);
//		}
//		return filePaths;
//	}

	public void save(String command) {
		//log.addText("save");
		setDirty(false);	
		
		speciesPanel.refreshSpeciesPanel(biomodel);

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

		// Annotate SBML model with synthesized SBOL DNA component and save component to local SBOL file
		if (!lema) {
			modelPanel.getSBOLField().deleteRemovedBioSimComponent();
			if (command.contains("Check")) {
				saveSBOL(true);
			} else {
				saveSBOL(false);
			}
		}
		
		// Write out species and influences to a gcm file
		//gcm.getSBMLDocument().getModel().setName(modelPanel.getModelName());
		biomodel.save(path + separator + modelId + ".xml");
		//log.addText("Saving GCM file:\n" + path + separator + gcmname + ".gcm\n");
		log.addText("Saving SBML file:\n" + path + separator + modelId  + ".xml\n");
		if (command.contains("Check")) {
			SBMLutilities.check(path + separator + modelId + ".xml");
		}	
		if (command.contains("LHPN")) {
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
					biomodel.createLogicalModel(path + separator + lpnName, log, biosim, lpnName);
				}
				else {
					// Do nothing
				}
			}
			else {
				biomodel.createLogicalModel(path + separator + lpnName, log, biosim, lpnName);
			}
		}
		else if (command.contains("SBML")) {
			// Then read in the file with the GCMParser
			GCMParser parser = new GCMParser(path + separator + modelId + ".gcm");
			GeneticNetwork network = null;
			BioModel bioModel = new BioModel(path);
			bioModel.load(path + separator + modelId + ".xml");
			SBMLDocument sbml = bioModel.flattenModel();		
			network = parser.buildNetwork(sbml);
			if (network == null) return;
			network.loadProperties(biomodel);
			// Finally, output to a file
			if (new File(path + separator + modelId + ".xml").exists()) {
				int value = JOptionPane.showOptionDialog(Gui.frame, modelId
						+ ".xml already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.mergeSBML(path + separator + modelId + ".xml");
					log.addText("Saving GCM file as SBML file:\n" + path + separator + modelId + ".xml\n");
					biosim.addToTree(modelId + ".xml");
					//biosim.updateOpenSBML(gcmname + ".xml");
				}
				else {
					// Do nothing
				}
			}
			else {
				network.mergeSBML(path + separator + modelId + ".xml");
				log.addText("Saving GCM file as SBML file:\n" + path + separator + modelId + ".xml\n");
				biosim.addToTree(modelId + ".xml");
			}
		}
		biosim.updateViews(modelId + ".xml");

	}

	// Annotate SBML model with synthesized SBOL DNA component and save component to local SBOL file
	public void saveSBOL(boolean check) {
		SBOLIdentityManager identityManager = new SBOLIdentityManager(biomodel);
		if (identityManager.containsBioSimURI()) {
			SBOLAssemblyGraph synGraph = new SBOLAssemblyGraph(biomodel);
			if (synGraph.containsSBOL()) {
				SBOLFileManager fileManager = new SBOLFileManager(biosim.getRoot(), biosim.getSbolFiles());
				if (fileManager.sbolFilesAreLoaded() && synGraph.loadDNAComponents(fileManager)) {
					SequenceTypeValidator seqValidator = 
							new SequenceTypeValidator(Preferences.userRoot().get("biosim.synthesis.regex", ""));
					synGraph.cutGraph(seqValidator.getStartTypes());
					if (synGraph.isLinear()) {
						SBOLAssembler synthesizer = new SBOLAssembler(synGraph, seqValidator);
						DnaComponent synthComp = synthesizer.synthesizeDNAComponent();
						if (synthComp != null) {
							if (identityManager.containsPlaceHolderURI() || identityManager.loadBioSimComponent(fileManager)) {
								identityManager.describeDNAComponent(synthComp);
								identityManager.identifyDNAComponent(synthComp);
								fileManager.saveDNAComponent(synthComp, identityManager);
								identityManager.replaceBioSimURI(synthComp.getURI());
								identityManager.annotateBioModel();
							}
						} else if (identityManager.containsPlaceHolderURI()) {
							identityManager.removeBioSimURI();
							identityManager.annotateBioModel();
						}
					} else if (identityManager.containsPlaceHolderURI()) {
						identityManager.removeBioSimURI();
						identityManager.annotateBioModel();
					}
				} else if (identityManager.containsPlaceHolderURI()) {
					identityManager.removeBioSimURI();
					identityManager.annotateBioModel();
				} 
			} else {
				if (!identityManager.containsPlaceHolderURI()) {
					SBOLFileManager fileManager = new SBOLFileManager(biosim.getRoot(), biosim.getSbolFiles());
					fileManager.deleteDNAComponent(identityManager.getBioSimURI());
				}
				identityManager.removeBioSimURI();
				identityManager.annotateBioModel();
			} 
		}
	}
	
	// Exports SBOL DNA components associated with model itself to a new or existing SBOL file
	public void exportSBOL() {
		SBOLIdentityManager identityManager = new SBOLIdentityManager(biomodel);
		if (identityManager.containsModelURIs()) {
			SBOLFileManager fileManager = new SBOLFileManager(biosim.getRoot(), biosim.getSbolFiles());
			if (fileManager.sbolFilesAreLoaded() && identityManager.loadModelComponents(fileManager)) {
				File lastFilePath;
				Preferences biosimrc = Preferences.userRoot();
				if (biosimrc.get("biosim.general.export_dir", "").equals(""))
					lastFilePath = null;
				else 
					lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
				String exportFilePath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export SBOL", -1);
				if (!exportFilePath.equals("")) {
					biosimrc.put("biosim.general.export_dir", exportFilePath);
					fileManager.exportDNAComponents(identityManager.getModelComponents(), exportFilePath);
				}
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
		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBML", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
			biomodel.exportSingleFile(exportPath);
			log.addText("Saving GCM file as SBML file:\n" + exportPath + "\n");
		}
	}
	
	public void exportFlatSBML() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBML", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
			GCMParser parser = new GCMParser(path + separator + modelId + ".xml");
			GeneticNetwork network = null;
			BioModel bioModel = new BioModel(path);
			bioModel.load(path + separator + modelId + ".xml");
			SBMLDocument sbml = bioModel.flattenModel();		
			network = parser.buildNetwork(sbml);
			if (network==null) return;
			network.loadProperties(biomodel);
			network.mergeSBML(exportPath);
			log.addText("Saving GCM file as SBML file:\n" + exportPath + "\n");
		}
	}
	
	public void saveAsLPN(String newName) {
		if (new File(path + separator + newName).exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
		log.addText("Saving SBML file as LPN file:\n" + path + separator + newName + "\n");
		if (biomodel.saveAsLPN(path + separator + newName)) {
			biosim.addToTree(newName);
		}
		//biosim.updateTabName(modelId + ".xml", newName + ".xml");
		//reload(newName);
	}		
	
	public void saveAs(String newName) {
		if (new File(path + separator + newName + ".xml").exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				biomodel.save(path + separator + newName + ".xml");
				log.addText("Saving SBML file as:\n" + path + separator + newName + ".xml\n");
				biosim.addToTree(newName + ".xml");
			}
			else {
				// Do nothing
				return;
			}
		}
		else {
			biomodel.save(path + separator + newName + ".xml");
			log.addText("Saving SBML file as:\n" + path + separator + newName + ".xml\n");
			biosim.addToTree(newName + ".xml");
		}
		biosim.updateTabName(modelId + ".xml", newName + ".xml");
		reload(newName);
	}

	/**
	 * user selects a file; schematic is printed there as a JPG file
	 */
	public void saveSchematic() {
		
		JFileChooser fc = new JFileChooser("Save Schematic");
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		ExampleFileFilter jpgFilter = new ExampleFileFilter();
		jpgFilter.addExtension("jpg");
		jpgFilter.setDescription("Image Files");		
		
		fc.addChoosableFileFilter(jpgFilter);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(jpgFilter);
		
		int returnVal = fc.showDialog(Gui.frame, "Save Schematic");
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
            File file = fc.getSelectedFile();
            schematic.outputFrame(file.getAbsoluteFile().toString(), false);
        }
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
		try {
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((refFile + "\n").getBytes());
			for (String s : parameterChanges) {
				if (!s.trim().equals("")) {
					out.write((s + "\n").getBytes());
				}
			}
			out.write(("\n").getBytes());
			if (elementsPanel != null) {
				for (String s : elementsPanel.getElementChanges()) {
					out.write((s + "\n").getBytes());
				}
			}
			out.close();
			setDirty(false);
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to save parameter file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		if (run) {
			ArrayList<String> sweepThese1 = new ArrayList<String>();
			ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
			ArrayList<String> sweepThese2 = new ArrayList<String>();
			ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
			for (String s : parameterChanges) {
				if (s.split(" ")[s.split(" ").length - 1].startsWith("(")) {
					if ((s.split(" ")[s.split(" ").length - 1]).split(",")[3].replace(")", "").trim().equals("1")) {
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
				ArrayList<AnalysisThread> threads = new ArrayList<AnalysisThread>();
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
							new File(path
									+ separator
									+ simName
									+ separator
									+ stem
									+ sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
											.replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
							createSBML(stem, sweepTwo);
							AnalysisThread thread = new AnalysisThread(reb2sac);
							thread.start(
									stem
											+ sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
													.replace("\"", "").replace(" ", "_").replace(",", ""), false);
							threads.add(thread);
							dirs.add(sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
									.replace("\"", "").replace(" ", "_").replace(",", ""));
							reb2sac.emptyFrames();
							if (ignoreSweep) {
								l = max2;
								j = max;
							}
						}
					}
					else {
						new File(path
								+ separator
								+ simName
								+ separator
								+ stem
								+ sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
										.replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
						createSBML(stem, sweep);
						AnalysisThread thread = new AnalysisThread(reb2sac);
						thread.start(
								stem
										+ sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
												.replace("\"", "").replace(" ", "_").replace(",", ""), false);
						threads.add(thread);
						dirs.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
								.replace("\"", "").replace(" ", "_").replace(",", ""));
						reb2sac.emptyFrames();
						if (ignoreSweep) {
							j = max;
						}
					}
					levelOne.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
							.replace("\"", "").replace(" ", "_").replace(",", ""));
				}
				new ConstraintTermThread(reb2sac).start(threads, dirs, levelOne, stem);
			}
			else {
				if (!stem.equals("")) {
					new File(path + separator + simName + separator + stem).mkdir();
				}
				if (createSBML(stem, ".")) {
					if (!stem.equals("")) {
						new AnalysisThread(reb2sac).start(stem, true);
					}
					else {
						new AnalysisThread(reb2sac).start(".", true);
					}
				}
				reb2sac.emptyFrames();
			}
		}
	}

	public void loadParams() {
		if (paramsOnly) {
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
							if (getParams.get(i).substring(0,getParams.get(i).lastIndexOf(" ")).equals(s.substring(0, s.lastIndexOf(" ")))) {
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
					if (prop.equals(GlobalConstants.INITIAL_STRING)) {
						Species species = biomodel.getSBMLDocument().getModel().getSpecies(id);
						if (species!=null) {
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(species, value);
							} else {
								if (value.startsWith("[")) {
									species.setInitialConcentration(Double.parseDouble(value.substring(1,value.length()-1)));
								} else {
									species.setInitialAmount(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.KDECAY_STRING)) {
						Reaction reaction = biomodel.getSBMLDocument().getModel().getReaction("Degradation_"+id);
						if (reaction != null) {
							LocalParameter kd = reaction.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
							if (kd == null) {
								kd = reaction.getKineticLaw().createLocalParameter();
								kd.setId(GlobalConstants.KDECAY_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kd, value);
							} else {
								kd.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.KCOMPLEX_STRING)) {
						Reaction reaction = biomodel.getComplexReaction(id);
						if (reaction != null) {
							LocalParameter kc_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
							if (kc_f == null) {
								kc_f = reaction.getKineticLaw().createLocalParameter();
								kc_f.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
							}
							LocalParameter kc_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
							if (kc_r == null) {
								kc_r = reaction.getKineticLaw().createLocalParameter();
								kc_r.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kc_f, value);
							} else {
								double [] Kc = Utility.getEquilibrium(value);
								kc_f.setValue(Kc[0]);
								kc_r.setValue(Kc[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.MEMDIFF_STRING)) {
						Reaction reaction = biomodel.getSBMLDocument().getModel().getReaction("MembraneDiffusion_"+id);
						if (reaction != null) {
							LocalParameter kmdiff_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
							if (kmdiff_f == null) {
								kmdiff_f = reaction.getKineticLaw().createLocalParameter();
								kmdiff_f.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
							}
							LocalParameter kmdiff_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
							if (kmdiff_r == null) {
								kmdiff_r = reaction.getKineticLaw().createLocalParameter();
								kmdiff_r.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kmdiff_f, value);
							} else {
								double [] Kmdiff = Utility.getEquilibrium(value);
								kmdiff_f.setValue(Kmdiff[0]);
								kmdiff_r.setValue(Kmdiff[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.PROMOTER_COUNT_STRING)) {
						Species species = biomodel.getSBMLDocument().getModel().getSpecies(id);
						if (species!=null) {
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(species, value);
							} else {
								species.setInitialAmount(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.RNAP_BINDING_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ko_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
							if (ko_f == null) {
								ko_f = reaction.getKineticLaw().createLocalParameter();
								ko_f.setId(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
							}
							LocalParameter ko_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
							if (ko_r == null) {
								ko_r = reaction.getKineticLaw().createLocalParameter();
								ko_r.setId(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ko_f, value);
							} else {
								double [] Ko = Utility.getEquilibrium(value);
								ko_f.setValue(Ko[0]);
								ko_r.setValue(Ko[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter kao_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
							if (kao_f == null) {
								kao_f = reaction.getKineticLaw().createLocalParameter();
								kao_f.setId(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
							}
							LocalParameter kao_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
							if (kao_r == null) {
								kao_r = reaction.getKineticLaw().createLocalParameter();
								kao_r.setId(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kao_f, value);
							} else {
								double [] Kao = Utility.getEquilibrium(value);
								kao_f.setValue(Kao[0]);
								kao_r.setValue(Kao[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.OCR_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ko = reaction.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
							if (ko == null) {
								ko = reaction.getKineticLaw().createLocalParameter();
								ko.setId(GlobalConstants.OCR_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ko, value);
							} else {
								ko.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.KBASAL_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter kb = reaction.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
							if (kb == null) {
								kb = reaction.getKineticLaw().createLocalParameter();
								kb.setId(GlobalConstants.KBASAL_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kb, value);
							} else {
								kb.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.ACTIVATED_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ka = reaction.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
							if (ka == null) {
								ka = reaction.getKineticLaw().createLocalParameter();
								ka.setId(GlobalConstants.ACTIVATED_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ka, value);
							} else {
								ka.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.STOICHIOMETRY_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter np = reaction.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
							if (np == null) {
								np = reaction.getKineticLaw().createLocalParameter();
								np.setId(GlobalConstants.STOICHIOMETRY_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(np, value);
							} else {
								np.setValue(Double.parseDouble(value));
							}
							for (int i = 0; i<reaction.getNumProducts(); i++) {
								if (value.startsWith("(")) {
									reaction.getProduct(i).setStoichiometry(1.0);
								} else {
									reaction.getProduct(i).setStoichiometry(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.COOPERATIVITY_STRING)) {
						String promoterId = null;
						String sourceId = null;
						String complexId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							if (id.contains("|"))
								promoterId = id.substring(id.indexOf("|")+1);
							else
								promoterId = id.substring(id.indexOf(">")+1);
						}
						if (id.contains("->")||id.contains("-|")) {
							sourceId = id.substring(0,id.indexOf("-"));
						} else {
							sourceId = id.substring(0,id.indexOf("+"));
							complexId = id.substring(id.indexOf(">")+1);
						}
						Reaction reaction = null;
						if (complexId==null) {
							reaction = biomodel.getProductionReaction(promoterId);
							if (reaction != null) {
								LocalParameter nc = null;
								if (id.contains("|")) {
									nc = reaction.getKineticLaw()
											.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_r"); 
									if (nc == null) {
										nc = reaction.getKineticLaw().createLocalParameter();
										nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_r");
									}
								} else {
									nc = reaction.getKineticLaw()
											.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_a"); 
									if (nc == null) {
										nc = reaction.getKineticLaw().createLocalParameter();
										nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_a");
									}
								}
								if (value.startsWith("(")) {
									AnnotationUtility.setSweepAnnotation(nc, value);
								} else {
									nc.setValue(Double.parseDouble(value));
								}
							}
						} else {
							reaction = biomodel.getComplexReaction(complexId);
							if (reaction != null) {
								LocalParameter nc = null;
								nc = reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId); 
								if (nc == null) {
									nc = reaction.getKineticLaw().createLocalParameter();
									nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId);
								}
								if (value.startsWith("(")) {
									AnnotationUtility.setSweepAnnotation(nc, value);
								} else {
									nc.setValue(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.KACT_STRING)) {
						String promoterId = null;
						String sourceId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							promoterId = id.substring(id.indexOf(">")+1);
						}
						sourceId = id.substring(0,id.indexOf("-"));
						Reaction reaction = null;
						reaction = biomodel.getProductionReaction(promoterId);
						if (reaction != null) {
							LocalParameter ka_f = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + sourceId + "_")); 
							if (ka_f == null) {
								ka_f = reaction.getKineticLaw().createLocalParameter();
								ka_f.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + sourceId + "_"));
							}
							LocalParameter ka_r = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + sourceId + "_")); 
							if (ka_r == null) {
								ka_r = reaction.getKineticLaw().createLocalParameter();
								ka_r.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + sourceId + "_"));
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ka_f, value);
							} else {
								double [] Ka = Utility.getEquilibrium(value);
								ka_f.setValue(Ka[0]);
								ka_r.setValue(Ka[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.KREP_STRING)) {
						String promoterId = null;
						String sourceId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							promoterId = id.substring(id.indexOf("|")+1);
						}
						sourceId = id.substring(0,id.indexOf("-"));
						Reaction reaction = null;
						reaction = biomodel.getProductionReaction(promoterId);
						if (reaction != null) {
							LocalParameter kr_f = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + sourceId + "_")); 
							if (kr_f == null) {
								kr_f = reaction.getKineticLaw().createLocalParameter();
								kr_f.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + sourceId + "_"));
							}
							LocalParameter kr_r = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + sourceId + "_")); 
							if (kr_r == null) {
								kr_r = reaction.getKineticLaw().createLocalParameter();
								kr_r.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + sourceId + "_"));
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kr_f, value);
							} else {
								double [] Kr = Utility.getEquilibrium(value);
								kr_f.setValue(Kr[0]);
								kr_r.setValue(Kr[1]);
							}
						}
					}
				}
				else {
					String [] splits = update.split(" ");
					id = splits[0];
					String value = splits[1].trim();
					if (splits[splits.length - 2].equals("Sweep")) {
						biomodel.setParameter(id, value, splits[splits.length-1]);
					} else {
						biomodel.setParameter(id, value, null);
					}
				}
			}
		}
	}

	public boolean createSBML(String stem, String direct) {
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
			}
			direct = direct.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", "");
			if (direct.equals(".") && !stem.equals("")) {
				direct = "";
			}
			SBMLDocument sbml = biomodel.flattenModel();		
			performModifications(sbml,dd);
			GCMParser parser = new GCMParser(biomodel, false);
			GeneticNetwork network = parser.buildNetwork(sbml);
			if (network==null) return false;
			if (reb2sac != null)
				network.loadProperties(biomodel, reb2sac.getGcmAbstractions(), reb2sac.getInterestingSpecies(), reb2sac.getProperty());
			else
				network.loadProperties(biomodel);
			SBMLDocument d = network.getSBML();
			//performModifications(d,dd);
			network.markAbstractable();
			network.mergeSBML(path + separator + simName + separator + stem + direct + separator + modelId + ".xml", d);
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create sbml file.",
					"Error Creating File", JOptionPane.ERROR_MESSAGE);
			return false;
			//e1.printStackTrace();
		}
		return true;
	}
	
	private void performModifications(SBMLDocument d,ArrayList<String> dd) {
		for (String s : elementsPanel.getElementChanges()) {
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
		for (String change : parameterChanges) {
			String[] splits = change.split(" ");
			String id = splits[0];
			String value = splits[splits.length-1];
			// Make sure it is not a sweep
			if (!value.contains("(")) {
				// Make sure not a special parameter
				if (!id.contains("/")) {
					updateValue(d,id,null,null,value,null);
				} else {
					String paramId = id.split("/")[1];
					id = id.split("/")[0];
					String factor = null;
					String type = null;
					if (id.contains("->")||id.contains("-|")) {
						if (id.contains("->")) type = "a";
						else type = "r";
						factor = id.substring(1,id.indexOf("-"));
						if (id.contains(",")) {
							id = id.substring(id.indexOf(",")+1,id.length()-1);
						} else if (id.contains(">")){
							id = id.substring(id.indexOf(">")+1,id.length()-1);
						} else {
							id = id.substring(id.indexOf("|")+1,id.length()-1);
						}
					} else if (id.contains("+")) {
						factor = id.substring(1,id.indexOf("+"));
						id = id.substring(id.indexOf(">")+1,id.length()-1);
					}
					updateValue(d,id,factor,paramId,value,type);
				}
			}
		}
		for (String di : dd) {
			String[] splits = di.split("=");
			String id = splits[0];
			String value = splits[1];
			// Make sure not a special parameter
			if (!id.contains("/")) {
				id = id.split(" ")[0];
				updateValue(d,id,null,null,value,null);
			} else {
				String paramId = id.split("/")[1];
				id = id.split("/")[0];
				String factor = null;
				String type = null;
				if (id.contains("->")||id.contains("-|")) {
					if (id.contains("->")) type = "a";
					else type = "r";
					factor = id.substring(1,id.indexOf("-"));
					if (id.contains(",")) {
						id = id.substring(id.indexOf(",")+1,id.length()-1);
					} else if (id.contains(">")){
						id = id.substring(id.indexOf(">")+1,id.length()-1);
					} else {
						id = id.substring(id.indexOf("|")+1,id.length()-1);
					}
				} else if (id.contains("+")) {
					factor = id.substring(1,id.indexOf("+"));
					id = id.substring(id.indexOf(">")+1,id.length()-1);
				}
				updateValue(d,id,factor,paramId,value,type);
			}
		}
	}
	
	private void updateValue(SBMLDocument d,String id,String factor,String paramId,String value,String type) {
		SBase sbase = d.getElementBySId(id);
		if (sbase != null) {
			if (sbase.getElementName().equals(GlobalConstants.COMPARTMENT)) {
				Compartment compartment = d.getModel().getCompartment(id);
				compartment.setSize(Double.parseDouble(value));
			} else if (sbase.getElementName().equals(GlobalConstants.SBMLSPECIES)) {
				if (paramId!=null) {
					if (paramId.equals(GlobalConstants.INITIAL_STRING)||
							paramId.equals(GlobalConstants.PROMOTER_COUNT_STRING)) {
						Species species = d.getModel().getSpecies(id);
						if (species.isSetInitialAmount()) {
							species.setInitialAmount(Double.parseDouble(value));
						} else {
							species.setInitialConcentration(Double.parseDouble(value));
						}
					} else if (paramId.equals(GlobalConstants.KDECAY_STRING)) {
						Reaction degradation = BioModel.getDegradationReaction(id, d.getModel());
						if (degradation!=null && degradation.getKineticLaw()!=null) {
							LocalParameter localparam = degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
							if (localparam==null) {
								localparam = degradation.getKineticLaw().createLocalParameter();
								localparam.setId(GlobalConstants.KDECAY_STRING);
							}
							localparam.setValue(Double.parseDouble(value));
						}
					} else if (paramId.equals(GlobalConstants.KCOMPLEX_STRING)) {
						Reaction complex = BioModel.getComplexReaction(id, d.getModel());
						if (complex!=null && complex.getKineticLaw()!=null) {
							BioModel.updateComplexParameters(complex, value);
						}
					} else if (paramId.equals(GlobalConstants.COOPERATIVITY_STRING)&&type==null) {
						Reaction complex = BioModel.getComplexReaction(id, d.getModel());
						if (complex!=null && complex.getKineticLaw()!=null) {
							BioModel.updateComplexCooperativity(d.getModel(), factor, id, value);
						}
					} else if (paramId.equals(GlobalConstants.MEMDIFF_STRING)) {
						Reaction diffusion = BioModel.getDiffusionReaction(id, d.getModel());
						if (diffusion!=null && diffusion.getKineticLaw()!=null) {
							BioModel.updateDiffusionParameters(id,diffusion, value);
						}
					} else {
						Reaction production = BioModel.getProductionReaction(id, d.getModel());
						if (production!=null && production.getKineticLaw()!=null) {
							if (factor!=null) {
								if (paramId.equals(GlobalConstants.COOPERATIVITY_STRING)) {
									BioModel.addProductionParameters(production, factor, value, null, null,type);
								} else if (paramId.equals(GlobalConstants.KACT_STRING)) {
									BioModel.addProductionParameters(production, factor, null, value, null,null);
								} else if (paramId.equals(GlobalConstants.KREP_STRING)) {
									BioModel.addProductionParameters(production, factor, null, null, value,null);
								}
							} else if (value.contains("/")) {
								double [] K = Utility.getEquilibrium(value);
								if (K[0] >= 0) { 	
									LocalParameter localparam = 
											production.getKineticLaw().getLocalParameter(paramId.replace("K","k")+"_f");
									if (localparam==null) {
										localparam = production.getKineticLaw().createLocalParameter();
										localparam.setId(paramId);
									}
									localparam.setValue(K[0]);
									localparam = production.getKineticLaw().getLocalParameter(paramId.replace("K","k")+"_r");
									if (localparam==null) {
										localparam = production.getKineticLaw().createLocalParameter();
										localparam.setId(paramId);
									}
									localparam.setValue(K[1]);
								}
							} else {
								LocalParameter localparam = production.getKineticLaw().getLocalParameter(paramId);
								if (localparam==null) {
									localparam = production.getKineticLaw().createLocalParameter();
									localparam.setId(paramId);
								}
								localparam.setValue(Double.parseDouble(value));
							}
						}

					}
				}
			} else if (sbase.getElementName().equals(GlobalConstants.PARAMETER)) {
				Parameter parameter = d.getModel().getParameter(id);
				parameter.setValue(Double.parseDouble(value));
			} else if (sbase.getElementName().equals(GlobalConstants.SBMLREACTION)) {
				Reaction reaction = d.getModel().getReaction(id);
				if (paramId!=null && reaction.getKineticLaw()!=null) {
					LocalParameter localparam = reaction.getKineticLaw().getLocalParameter(paramId);
					if (localparam!=null) {
						localparam.setValue(Double.parseDouble(value));
					}
				}
			} 
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
	}

	public synchronized void lock() {
		lock = true;
	}

	public synchronized void unlock() {
		lock = false;
	}
	
	public void rebuildGui() {
		removeAll();
		buildGui();
		revalidate();
	}

	private void refreshComponentsList() {
		components.removeAllItem();
		for (long i = 0; i < biomodel.getSBMLCompModel().getNumSubmodels(); i++) {
			
			Submodel submodel = biomodel.getSBMLCompModel().getSubmodel(i);
			String locationAnnotationString = "";
			
			//if the submodel is gridded, then get the component names from the locations parameter
			if (biomodel.getSBMLDocument().getModel().getParameter(submodel.getId().replace("GRID__","") + "__locations") != null) {
					
				locationAnnotationString = 
						biomodel.getSBMLDocument().getModel()
						.getParameter(submodel.getId().replace("GRID__","") + "__locations")
						.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
			
				String[] compIDs = locationAnnotationString.replace("\"","").split("array:");
				
				for (int j = 2; j < compIDs.length; ++j) {
					
					if (compIDs[j].contains("=(")) {
						
						compIDs[j] = compIDs[j].split("=")[0].trim();
						
						components.addItem(compIDs[j] + " " + 
								submodel.getModelRef() + " " + biomodel.getComponentPortMap(compIDs[j]));
					}
				}
			}
			else			
				components.addItem(submodel.getId() + " " + submodel.getModelRef() + " " + biomodel.getComponentPortMap(submodel.getId()));
		}	
	}
	
	private void buildGui() {
		
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
			
		// create the modelview2 (jgraph) panel
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		JPanel propPanel = new JPanel(new BorderLayout());
		propPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		tab = new JTabbedPane();
		
		String file = filename.replace(".gcm", ".xml");
		
		compartmentPanel = new Compartments(biosim,biomodel,this, paramsOnly,getParams, path + separator + file, parameterChanges,false);
		reactionPanel = new Reactions(biosim,biomodel,paramsOnly,getParams,path + separator + file,parameterChanges, this);
		speciesPanel = new MySpecies(biosim,biomodel,paramsOnly,getParams,path + separator + file,parameterChanges,
				biomodel.getGrid().isEnabled(),this);
		parametersPanel = new Parameters(biosim, biomodel,this, paramsOnly,getParams,path + separator + file,parameterChanges, 
				!paramsOnly && !biomodel.getGrid().isEnabled() && !textBased);
		rulesPanel = new Rules(biosim, biomodel, this);
		consPanel = new Constraints(biomodel, this);
		eventPanel = new Events(biosim,biomodel,this,textBased);

		JPanel compPanel = new JPanel(new BorderLayout());
		if (textBased) {
			modelPanel = new ModelPanel(biomodel, this);
			compPanel.add(modelPanel, "North");
		}
		compPanel.add(compartmentPanel,"Center");

		biomodel.setCompartmentPanel(compartmentPanel);
		biomodel.setSpeciesPanel(speciesPanel);
		biomodel.setReactionPanel(reactionPanel);
		biomodel.setRulePanel(rulesPanel);
		biomodel.setEventPanel(eventPanel);
		biomodel.setConstraintPanel(consPanel);
		biomodel.setParameterPanel(parametersPanel);
		
		components = new PropertyList("Component List");
		EditButton addInit = new EditButton("Add Component", components);
		RemoveButton removeInit = new RemoveButton("Remove Component", components);
		EditButton editInit = new EditButton("Edit Component", components);
		
		refreshComponentsList();
		
		this.getSpeciesPanel().refreshSpeciesPanel(biomodel);
		JPanel componentsPanel = Utility.createPanel(this, "Components", components, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(componentsPanel);
		
		this.schematic = new Schematic(biomodel, biosim, this, true, null,compartmentPanel,reactionPanel,rulesPanel,
				consPanel,eventPanel,parametersPanel,lema);
		if (textBased) {
			if (!biosim.lema) {
				tab.addTab("Compartments", compPanel);
				tab.addTab("Species", speciesPanel);
				tab.addTab("Reactions", reactionPanel);
			}
			tab.addTab("Parameters", parametersPanel);
			tab.addTab("Components", componentsPanel);
			tab.addTab("Rules", rulesPanel);
			tab.addTab("Constraints", propPanel);
			tab.addTab("Events", eventPanel);
		} 
		else {
			modelPanel = schematic.getModelPanel();
			tab.addTab("Schematic", schematic);
			if (biomodel.getGrid().isEnabled()) {
				tab.addTab("Grid Species", speciesPanel);
				tab.addTab("Parameters", parametersPanel);
			} else {
				tab.addTab("Constants", parametersPanel);
			}
			
			tab.addChangeListener(this);

		}
		
		functionPanel = new Functions(biomodel,this);
		unitPanel = new Units(biosim,biomodel,this);
		//JPanel defnPanel = new JPanel(new BorderLayout());
		//defnPanel.add(mainPanelNorth, "North");
		//defnPanel.add(functionPanel,"Center");
		//defnPanel.add(unitPanel,"South");
		//tab.addTab("Definitions", defnPanel);
		tab.addTab("Functions", functionPanel);
		tab.addTab("Units", unitPanel);
		
		/*
		if (biomodel.getSBMLDocument().getLevel() < 3) {
			CompartmentTypes compTypePanel = new CompartmentTypes(biosim,biomodel,this);
			SpeciesTypes specTypePanel = new SpeciesTypes(biosim,biomodel,this);
			JPanel typePanel = new JPanel(new BorderLayout());
			typePanel.add(mainPanelNorth, "North");
			typePanel.add(compTypePanel,"Center");
			typePanel.add(specTypePanel,"South");
			tab.addTab("Types", typePanel);
		}
		*/

		InitialAssignments initialsPanel = new InitialAssignments(biosim,biomodel,this);
		compartmentPanel.setPanels(initialsPanel, rulesPanel);
		functionPanel.setPanels(initialsPanel, rulesPanel);
		speciesPanel.setPanels(reactionPanel, initialsPanel, rulesPanel, parametersPanel);
		reactionPanel.setPanels(initialsPanel, rulesPanel);
		
		setLayout(new BorderLayout());
		if (paramsOnly) {
			add(parametersPanel, BorderLayout.CENTER);
		}
		else {
			add(tab, BorderLayout.CENTER);
		}

		species = new PropertyList("Species List");
		addInit = new EditButton("Add Species", species);
		removeInit = new RemoveButton("Remove Species", species);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
		}
		editInit = new EditButton("Edit Species", species);
		if (paramsOnly) {
			ArrayList<String> specs = biomodel.getSpecies();
			ArrayList<String> promoters = biomodel.getPromoters();
			for (String s : getParams) {
				if (s.contains("-|")||s.contains("->")) {
					String factor = s.substring(1,s.indexOf("-"));
					String promoter = null;
					if (s.contains(",")) {
						promoter = s.substring(s.indexOf(",")+1,s.indexOf("/")-1);
					} else if (s.contains(">")){
						promoter = s.substring(s.indexOf(">")+1,s.indexOf("/")-1);
					} else {
						promoter = s.substring(s.indexOf("|")+1,s.indexOf("/")-1);
					}
					if (specs.contains(factor) && promoters.contains(promoter)) {
						parameterChanges.add(s);
					} 
				} else if (s.contains("+")) {
					String factor = s.substring(1,s.indexOf("+"));
					String complex = s.substring(s.indexOf(">")+1,s.indexOf("/")-1);
					if (specs.contains(factor) && specs.contains(complex)) {
						parameterChanges.add(s);
					} 
				}
			}
			for (String s : getParams) {
				if (s.contains("/") && specs.contains(s.split("/")[0].trim())) {
					specs.add(s.split("/")[0].trim());
					parameterChanges.add(s);
				}
			}
			for (String s : getParams) {
				if (!s.contains("\"")) {
					if (s.contains("/") && promoters.contains(s.split("/")[0].trim())) {
						parameterChanges.add(s);
					}
				}
			}
			for (String s : getParams) {
				if (!parameterChanges.contains(s)) {
					System.out.println(s);
				}
			}
			species.addAllItem(specs);
		}
		else {
			species.addAllItem(biomodel.getSpecies());
		}
		JPanel initPanel = Utility.createPanel(this, "Species", species, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);
		
		parametersPanel.setPanels(initialsPanel, rulesPanel);
		propPanel.add(consPanel, "Center");
	}
	
	public void reloadParameters() {
		if (paramsOnly) {
			for (String update : parameterChanges) {
				String id;
				if (!update.contains("/")) {
					id = update.split(" ")[0];
					String value = update.split(" ")[1].trim();
					biomodel.setParameter(id, value, null);
				}
			}
		}
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
					if (biomodel.removeInfluenceCheck(name)) {
						biomodel.removeInfluence(name);
						list.removeItem(name);
					}
				}
			}
			else if (getName().contains("Species")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					biomodel.removeSpecies(name);
				}
			}
			else if (getName().contains("Promoter")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					biomodel.removePromoter(name);
					list.removeItem(name);
				}
			}
			else if (getName().contains("Property")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					//gcm.removeCondition(name);
					list.removeItem(name);
				}
			}
			else if (getName().contains("Component")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					String comp = name.split(" ")[0];
					biomodel.removeComponent(comp);
					list.removeItem(name);
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
				//ConditionsPanel panel = new ConditionsPanel(selected, list, gcm, paramsOnly,gcmEditor);
			}
			else if (getName().contains("Component")) {
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					displayChooseComponentDialog(true, list, false);
				} else {
					displayChooseComponentDialog(false, list, true);
				}
			}
			else if (getName().contains("Parameter")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				BioModel refGCM = null;
				if (paramsOnly) {
					refGCM = new BioModel(path);
					refGCM.load(path + separator + refFile);
				}
				/*
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
				 */
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
		BioModel refGCM = null;
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		PromoterPanel panel = new PromoterPanel(id, biomodel, paramsOnly, refGCM, this);	
		
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

	public SpeciesPanel launchSpeciesPanel(String id, boolean inTab) {
		
		BioModel refGCM = null;
		
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		
		SpeciesPanel panel = new SpeciesPanel(biosim, id, species, components, biomodel, paramsOnly, refGCM, this, inTab);
		
//		if (paramsOnly) {
//			String updates = panel.updates();
//			if (!updates.equals("")) {
//				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
//					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
//						parameterChanges.remove(i);
//					}
//				}
//				if (updates.contains(" ")) {
//					for (String s : updates.split("\n")) {
//						parameterChanges.add(s);
//					}
//				}
//			}
//		}
		
		return panel;
	}
	
	public InfluencePanel launchInfluencePanel(String id){
		BioModel refGCM = null;
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		InfluencePanel panel = new InfluencePanel(id, influences, biomodel, paramsOnly, refGCM, this);
		
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
		
		BioModel refGCM = null;
		
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		
		// TODO: This is a messy way to do things. We set the selected component in the list
		// and then call displayChooseComponentDialog(). This makes for tight coupling with the
		// component list.
		for (int i = 0; i < this.components.getModel().getSize(); i++) {
			
			String componentsListRow = this.components.getModel().getElementAt(i).toString();
			String componentsListId = componentsListRow.split(" ")[0];
			
			//System.err.println(componentsListId + "   " + id);
			
			if (componentsListId.equals(id)) {
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
		boolean created = GridPanel.showGridPanel(this, biomodel, false);

		//if the grid is built, then draw it and so on
		if (created) {
			
			this.setDirty(true);
			//this.refresh();
			//schematic.getGraph().buildGraph();
			//schematic.display();
			//biomodel.makeUndoPoint();
		}
	}
	
	public SchemeChooserPanel getSchemeChooserPanel(
			String cellID, MovieContainer movieContainer, boolean inTab) {
		
		return new SchemeChooserPanel(cellID, movieContainer, inTab);
	}
	
	
	public boolean checkNoComponentLoop(String gcm, String checkFile) {
		gcm = gcm.replace(".gcm", ".xml");
		boolean check = true;
		BioModel g = new BioModel(path);
		g.load(path + separator + checkFile);
		for (long i = 0; i < g.getSBMLComp().getNumExternalModelDefinitions(); i++) {
			String compGCM = g.getSBMLComp().getExternalModelDefinition(i).getSource().substring(7);
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
			if (s.endsWith(".xml") && !s.equals(filename) /*&& checkNoComponentLoop(filename, s)*/) {
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
	
	public ArrayList<String> getParameterChanges() {
		return parameterChanges;
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
			comp = selected.split(" ")[1] + ".xml";
		}
		else {
			ArrayList<String> components = getComponentsList();

			if (components.size() == 0) {
				comp = null;
				JOptionPane.showMessageDialog(Gui.frame,
						"There aren't any other models to use as components."
								+ "\nCreate a new model or import a model into the project first.",
						"Add Another Model To The Project", JOptionPane.ERROR_MESSAGE);
			}
			else {
				comp = (String) JOptionPane.showInputDialog(Gui.frame,
						"Choose a model to use as a component:", "Component Editor",
						JOptionPane.PLAIN_MESSAGE, null, components.toArray(new String[0]), null);
			}
		}
		if (comp != null && !comp.equals("")) {
			BioModel subBioModel = new BioModel(path);
			subBioModel.load(path + separator + comp);
			subBioModel.flattenBioModel();
			String oldPort = null;
			if (selected != null) {
				oldPort = selected.substring(selected.split(" ")[0].length()
						+ selected.split(" ")[1].length() + 2);
				selected = selected.split(" ")[0];
			}

			ArrayList<String> ports = subBioModel.getPorts();

			if(createUsingDefaults){
				SBMLWriter writer = new SBMLWriter();
				String SBMLstr = writer.writeSBMLToString(subBioModel.getSBMLDocument());
				String md5 = Utility.MD5(SBMLstr);
				// TODO: Is this correct?
				outID = biomodel.addComponent(null, comp, false, null, -1, -1, 0, 0, md5);
				list.addItem(outID + " " + comp.replace(".xml", "") + " ()");
			}else{
				new ComponentsPanel(selected, list, biomodel, subBioModel, ports, comp, oldPort, paramsOnly, this);
				outID = selected;
			}

		}
		return outID;
	}
	
	public boolean isGridEditor() {
		return biomodel.getGrid().isEnabled();
	}
	
	public void addCompartment() {
		schematic.addCompartment(-1,-1);
	}
	
	public void addSpecies() {
		schematic.addSpecies(-1,-1);
	}
	
	public void addReaction() {
		schematic.addReaction(-1,-1);
	}
	
	public void addComponent() {
		schematic.addComponent(-1,-1);
	}
	
	public void addPromoter() {
		schematic.addPromoter(-1,-1);
	}
	
	public void addVariable() {
		schematic.addVariable(-1,-1);
	}
	
	public void addBoolean() {
		schematic.addBoolean(-1,-1);
	}
	
	public void addPlace() {
		schematic.addPlace(-1,-1);
	}
	
	public void addTransition() {
		schematic.addTransition(-1,-1);
	}
	
	public void addRule() {
		schematic.addRule(-1,-1);
	}
	
	public void addConstraint() {
		schematic.addConstraint(-1,-1);
	}
	
	public void addEvent() {
		schematic.addEvent(-1,-1);
	}
	
	public void addSelfInfluence() {
		schematic.addSelfInfluence(-1,-1);
	}
	
	public void moveLeft() {
		schematic.moveCells(-5,0);
	}
	
	public void moveRight() {
		schematic.moveCells(5,0);
	}
	
	public void moveUp() {
		schematic.moveCells(0,-5);
	}
	
	public void moveDown() {
		schematic.moveCells(0,5);
	}
	
	public void cut() {
		schematic.cut();
	}
	
	public void select() {
		schematic.select();
	}
	
	public void undo() {
		biomodel.undo();
		schematic.refresh();
		this.refresh();
		this.setDirty(true);		
	}
	
	public void redo() {
		biomodel.redo();
		schematic.refresh();
		this.refresh();
		this.setDirty(true);		
	}
	
	public void setElementsPanel(ElementsPanel elementsPanel) {
		this.elementsPanel = elementsPanel;
	}
	
	public Gui getGui() {
		return biosim;
	}
	
	public Schematic getSchematic() {
		return schematic;
	}

	public AnalysisView getReb2Sac() {
		return reb2sac;
	}
	
	public boolean isTextBased() {
		return textBased;
	}

	public MySpecies getSpeciesPanel() {
		
		return speciesPanel;
	}
	
	public void setTextBased(boolean textBased) {
		this.textBased = textBased;
	}

	public void stateChanged(ChangeEvent arg0) {
		if (tab.getSelectedIndex()==0) {
			schematic.getGraph().buildGraph();
		}
	}
	
	private JTabbedPane tab = null;

	private boolean lock = false;

	private String[] options = { "Ok", "Cancel" };

	private PropertyList species = null;

	private PropertyList influences = null;

	private PropertyList promoters = null;

	private PropertyList components = null;
	
	private PropertyList conditions = null;

	private String path = null;

	private static final String none = "--none--";

	private Gui biosim = null;

	private Log log = null;
	
	private boolean textBased = false;

	private MutableBoolean dirty = new MutableBoolean(false);
}
