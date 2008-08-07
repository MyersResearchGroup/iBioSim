package gcm2sbml.gui;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import biomodelsim.BioSim;
import biomodelsim.Log;

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
public class GCM2SBMLEditor extends JPanel implements ActionListener,
		MouseListener {

	private String filename = "";
	private String gcmname = "";

	private GCMFile gcm = null;

	public GCM2SBMLEditor(String path) {
		this(path, null, null, null);
	}

	public GCM2SBMLEditor(String path, String filename, BioSim biosim, Log log) {
		super();
		this.biosim = biosim;
		this.log = log;
		this.path = path;
		gcm = new GCMFile();
		if (filename != null) {
			gcm.load(path + File.separator + filename);
			this.filename = filename;
			this.gcmname = filename.replace(".gcm", "");
		} else {
			this.filename = "";
		}
		buildGui(this.filename);
	}
	
	public String getFilename() {
		return filename;
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
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean isDirty() {
		return dirty;
	}
	
	public GCMFile getGCM() {
		return gcm;
	}

	public String getSBMLFile() {
		return (String) sbmlFiles.getSelectedItem();
	}

	public void save(String command) {
		dirty = false;

		if (!sbmlFiles.getSelectedItem().equals(none)) {
			gcm.setSBMLFile(sbmlFiles.getSelectedItem().toString());
		} else {
			gcm.setSBMLFile("");
		}
		if (dimAbs.isSelected()) {
			gcm.setDimAbs(true);
		} else {
			gcm.setDimAbs(false);
		}
		if (bioAbs.isSelected()) {
			gcm.setBioAbs(true);
		} else {
			gcm.setBioAbs(false);
		}
		GeneticNetwork.setRoot(path + File.separator);

		if (command.contains("GCM as")) {
			String newName = JOptionPane.showInputDialog(biosim.frame(), "Enter GCM name:", "GCM Name",
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
		gcm.save(path + File.separator + gcmname + ".gcm");
		log.addText("Saving GCM file:\n" + path + File.separator + gcmname + ".gcm\n");

		if (command.contains("template")) {
			GCMParser parser = new GCMParser(path + File.separator + gcmname
					+ ".gcm");
			try {
				parser.buildNetwork();
			} catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(biosim.frame(), e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			GeneticNetwork network = new GeneticNetwork();
			
			String templateName = JOptionPane.showInputDialog(biosim.frame(), "Enter SBML template name:", "SBML Template Name",
					JOptionPane.PLAIN_MESSAGE);
			if (!templateName.contains(".sbml") || !templateName.contains(".xml")) {
				templateName = templateName + ".sbml";
			}
			if (new File(path + File.separator + templateName).exists()) {
				int value = JOptionPane.showOptionDialog(biosim.frame(), templateName
						+ " already exists.  Overwrite file?",
						"Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.buildTemplate(parser.getSpecies(), parser.getPromoters(), gcmname + ".gcm", path + File.separator + templateName);
					log.addText("Saving GCM file as SBML template:\n" + path + File.separator + templateName + "\n");
					biosim.refreshTree();
					biosim.updateOpenSBML(templateName);
				} else {
					// Do nothing
				}
			} else {
				network.buildTemplate(parser.getSpecies(), parser.getPromoters(), gcmname + ".gcm", path + File.separator + templateName);
				log.addText("Saving GCM file as SBML template:\n" + path + File.separator + templateName + "\n");
				biosim.refreshTree();
			}
		}
		else if (command.contains("SBML")) {
			// Then read in the file with the GCMParser
			GCMParser parser = new GCMParser(path + File.separator + gcmname
					+ ".gcm");
			GeneticNetwork network = null;
			try {
				network = parser.buildNetwork();
			} catch (IllegalStateException e) {
				JOptionPane.showMessageDialog(biosim.frame(), e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			network.loadProperties(gcm);
			// Finally, output to a file
			if (new File(path + File.separator + gcmname + ".sbml").exists()) {
				int value = JOptionPane.showOptionDialog(biosim.frame(), gcmname
						+ ".sbml already exists.  Overwrite file?",
						"Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.mergeSBML(path + File.separator + gcmname	+ ".sbml");
					log.addText("Saving GCM file as SBML file:\n" + path + File.separator + gcmname + ".sbml\n");
					biosim.refreshTree();
					biosim.updateOpenSBML(gcmname	+ ".sbml");
				} else {
					// Do nothing
				}
			} else {
				network.mergeSBML(path + File.separator + gcmname + ".sbml");
				log.addText("Saving GCM file as SBML file:\n" + path + File.separator + gcmname + ".sbml\n");
				biosim.refreshTree();
			}
		}
		biosim.updateViews(gcmname + ".gcm");

	}
	
	public void saveAs(String newName) {
		if (new File(path + File.separator + newName + ".gcm").exists()) {
			int value = JOptionPane.showOptionDialog(biosim.frame(), newName
					+ " already exists.  Overwrite file?",
					"Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				gcm.save(path + File.separator + newName + ".gcm");
				log.addText("Saving GCM file as:\n" + path + File.separator + newName + ".gcm\n");
			} else {
				// Do nothing				
				return;
			}			
		}
		else {
			gcm.save(path + File.separator + newName + ".gcm");
			log.addText("Saving GCM file as:\n" + path + File.separator + newName + ".gcm\n");
		}
		filename = newName+".gcm";
		gcmname = newName;
		gcm.load(path + File.separator + newName + ".gcm");
		GCMNameTextField.setText(newName);
		biosim.refreshTree();
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
		else if (o instanceof JComboBox && !lock && !gcm.getSBMLFile().equals(sbmlFiles.getSelectedItem())) {			
			dirty = true;
		}
		else if (o instanceof JCheckBox && !lock && ((gcm.getDimAbs() != dimAbs.isSelected())||
				(gcm.getBioAbs() != bioAbs.isSelected()))) {
			dirty = true;
		}
		// System.out.println(o);
	}
	
	public synchronized void lock() { 
		lock = true;
	}
	
	public synchronized void unlock() {
		lock = false;
	}

	private void buildGui(String filename) {
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		JPanel mainPanelCenterDown = new JPanel(new BorderLayout());
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
		mainPanelCenter.add(mainPanelCenterDown, BorderLayout.SOUTH);
		GCMNameTextField = new JTextField(filename.replace(".gcm",
				""), 15);
		GCMNameTextField.setEditable(false);
		GCMNameTextField.addActionListener(this);
		JLabel GCMNameLabel = new JLabel("GCM Id:");
		mainPanelNorth.add(GCMNameLabel);
		mainPanelNorth.add(GCMNameTextField);

		JLabel sbmlFileLabel = new JLabel("SBML File:");
		sbmlFiles = new JComboBox();
		sbmlFiles.addActionListener(this);
		reloadFiles();
		mainPanelNorth.add(sbmlFileLabel);
		mainPanelNorth.add(sbmlFiles);

		JLabel bioAbsLabel = new JLabel("Biochemical abstraction:");
		bioAbs = new JCheckBox();
		bioAbs.addActionListener(this);
		mainPanelNorth.add(bioAbsLabel);
		mainPanelNorth.add(bioAbs);
		if (gcm.getBioAbs()) {
			bioAbs.setSelected(true);
		}

		JLabel dimAbsLabel = new JLabel("Dimerization abstraction:");
		dimAbs = new JCheckBox();
		dimAbs.addActionListener(this);
		mainPanelNorth.add(dimAbsLabel);
		mainPanelNorth.add(dimAbs);
		if (gcm.getDimAbs()) {
			dimAbs.setSelected(true);
		}
			
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel();
		SaveButton saveButton = new SaveButton("Save GCM", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		saveButton = new SaveButton("Save GCM as", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		// mainPanelCenterDown.add(saveButton);
		saveButton = new SaveButton("Save as SBML", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		saveButton = new SaveButton("Save as SBML template", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);		
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null,
				buttons);
		pane.setDividerSize(2);
		mainPanelCenterDown.add(pane, BorderLayout.CENTER);
		
		promoters = new PropertyList("Promoter List");
		EditButton addInit = new EditButton("Add Promoter", promoters);
		RemoveButton removeInit = new RemoveButton("Remove Promoter", promoters);
		EditButton editInit = new EditButton("Edit Promoter", promoters);
		promoters.addAllItem(gcm.getPromoters().keySet());

		JPanel initPanel = Utility.createPanel(this, "Promoters", promoters, addInit,
				removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);


		species = new PropertyList("Species List");
		addInit = new EditButton("Add Species", species);
		removeInit = new RemoveButton("Remove Species", species);
		editInit = new EditButton("Edit Species", species);
		species.addAllItem(gcm.getSpecies().keySet());

		initPanel = Utility.createPanel(this, "Species", species,
				addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);

		influences = new PropertyList("Influence List");
		addInit = new EditButton("Add Influence", influences);
		removeInit = new RemoveButton("Remove Influence", influences);
		editInit = new EditButton("Edit Influence", influences);
		influences.addAllItem(gcm.getInfluences().keySet());

		initPanel = Utility.createPanel(this, "Influences", influences,
				addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);

		parameters = new PropertyList("Parameter List");
		editInit = new EditButton("Edit Parameter", parameters);
		// parameters.addAllItem(gcm.getParameters().keySet());
		parameters.addAllItem(generateParameters());
		initPanel = Utility.createPanel(this, "Parameters", parameters, null,
				null, editInit);

		mainPanelCenterCenter.add(initPanel);
	}

	public void reloadFiles() {
		lock();
		//sbmlFiles.removeAll();
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
		if (!gcm.getSBMLFile().equals("")
				&& sbmlFiles.getSelectedItem().equals(none)) {
			Utility.createErrorMessage("Warning: Missing File",
					"Unable to find SBML file " + gcm.getSBMLFile()
							+ ".  Setting default SBML file to none");
			gcm.setSBMLFile("");
		}
		unlock();
	}

	private Set<String> generateParameters() {
		HashSet<String> results = new HashSet<String>();
		for (String s : gcm.getParameters().keySet()) {
			if (gcm.getGlobalParameters().containsKey(s)) {
				results.add(CompatibilityFixer.getGuiName(s) + " ("+CompatibilityFixer.getSBMLName(s)+"), Custom, " + gcm.getParameter(s));
			} else {
				results.add(CompatibilityFixer.getGuiName(s) + " ("+CompatibilityFixer.getSBMLName(s)+"), Default, " + gcm.getParameter(s));
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
			dirty = true;
			if (getName().contains("Influence")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removeInfluenceCheck(name)) {
						gcm.removeInfluence(name);
						list.removeItem(name);
					}
				}
			} else if (getName().contains("Species")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removeSpeciesCheck(name)) {
						gcm.removeSpecies(name);
						list.removeItem(name);
					} else {
						JOptionPane
								.showMessageDialog(
										biosim.frame(),
										"Cannot remove species "
												+ name
												+ " because it is currently in other reactions");
					}
				}
			} else if (getName().contains("Promoter")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removePromoterCheck(name)) {
						gcm.removePromoter(name);
						list.removeItem(name);
					} else {
						JOptionPane
								.showMessageDialog(
										biosim.frame(),
										"Cannot remove promoter "
												+ name
												+ " because it is currently in other reactions");
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
			if (list.getSelectedValue() == null
			    && getName().contains("Edit")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			dirty = true;
			if (getName().contains("Species")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				SpeciesPanel panel = new SpeciesPanel(selected, list,
						influences, gcm);
			} else if (getName().contains("Influence")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				InfluencePanel panel = new InfluencePanel(selected, list, gcm);
			} else if (getName().contains("Promoter")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				PromoterPanel panel = new PromoterPanel(selected, list,
						influences, gcm);
			} else if (getName().contains("Parameter")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				ParameterPanel panel = new ParameterPanel(selected, list, gcm);
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}
	
	private boolean lock = false;
	
	private JTextField GCMNameTextField = null;

	private String[] options = { "Ok", "Cancel" };

	private PropertyList species = null;

	private PropertyList influences = null;

	private PropertyList promoters = null;

	private PropertyList parameters = null;

	private JComboBox sbmlFiles = null;
	
	private JCheckBox bioAbs = null;

	private JCheckBox dimAbs = null;

	private String path = null;

	private static final String none = "--none--";

	private BioSim biosim = null;
	
	private Log log = null;

	private boolean dirty = false;
}
