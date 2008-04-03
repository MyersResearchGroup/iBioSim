package gcm2sbml.gui;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Reaction;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import biomodelsim.BioSim;

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
		this(path, null, null);
	}

	public GCM2SBMLEditor(String path, String filename, BioSim biosim) {
		super();
		this.biosim = biosim;
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

	public void save(String command) {
		dirty = false;
		// Write out species and influences to a gcm file
		gcm.save(path + File.separator + gcmname + ".gcm");

		if (command.contains("SBML")) {
			// Then read in the file with the GCMParser
			GCMParser parser = new GCMParser(path + File.separator + gcmname
					+ ".gcm");
			GeneticNetwork network = parser.buildNetwork();
			network.loadProperties(gcm);
			// Finally, output to a file
			if (new File(path + File.separator + gcmname + ".sbml").exists()) {
				int value = JOptionPane.showOptionDialog(this, gcmname+".sbml already exists.  Overwrite file?" ,
						"Save file", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					network.outputSBML(path + File.separator + gcmname + ".sbml");
					biosim.refreshTree();					
				} else {
					//Do nothing
				}
			} else {
				network.outputSBML(path + File.separator + gcmname + ".sbml");
				biosim.refreshTree();					
			}			
		}
		biosim.updateViews(gcmname + ".gcm");
		
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
		// System.out.println(o);
	}

	private void buildGui(String filename) {
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		JPanel mainPanelCenterDown = new JPanel();
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
		mainPanelCenter.add(mainPanelCenterDown, BorderLayout.SOUTH);
		JTextField GCMNameTextField = new JTextField(filename.replace(".gcm",
				""), 50);
		GCMNameTextField.setEditable(false);
		GCMNameTextField.addActionListener(this);
		JLabel GCMNameLabel = new JLabel("GCM Id:");
		mainPanelNorth.add(GCMNameLabel);
		mainPanelNorth.add(GCMNameTextField);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");			
		add(mainPanel);

		JPanel buttons = new JPanel();				
		SaveButton saveButton = new SaveButton("Save GCM", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		//mainPanelCenterDown.add(saveButton);
		saveButton = new SaveButton("Save as SBML", GCMNameTextField);
		buttons.add(saveButton);
		saveButton.addActionListener(this);
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, buttons);
		pane.setDividerSize(2);
		mainPanelCenterDown.add(pane);
		

		species = new PropertyList("Species List");
		EditButton addInit = new EditButton("Add Specie", species);
		RemoveButton removeInit = new RemoveButton("Remove Specie", species);
		EditButton editInit = new EditButton("Edit Specie", species);
		species.addAllItem(gcm.getSpecies().keySet());

		JPanel initPanel = Utility.createPanel(this, "Species", species,
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

		promoters = new PropertyList("Promoter List");
		addInit = new EditButton("Add Promoter", promoters);
		removeInit = new RemoveButton("Remove Promoter", promoters);
		editInit = new EditButton("Edit Promoter", promoters);
		promoters.addAllItem(gcm.getPromoters().keySet());

		initPanel = Utility.createPanel(this, "Promoters", promoters, addInit,
				removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);

		parameters = new PropertyList("Parameter List");
		editInit = new EditButton("Edit Parameter", parameters);
		//parameters.addAllItem(gcm.getParameters().keySet());
		parameters.addAllItem(generateParameters());
		initPanel = Utility.createPanel(this, "Parameters", parameters, null,
				null, editInit);

		mainPanelCenterCenter.add(initPanel);
	}
	
	private Set<String> generateParameters() {
		HashSet<String> results = new HashSet<String>();
		for (String s : gcm.getParameters().keySet()) {
			if (gcm.getGlobalParameters().containsKey(s)) {
				results.add(s + ", Custom, " + gcm.getParameter(s));
			} else {
				results.add(s + ", Default, " + gcm.getParameter(s));
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
			} else if (getName().contains("Specie")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (gcm.removeSpeciesCheck(name)) {
						gcm.removeSpecies(name);
						list.removeItem(name);
					} else {
						JOptionPane
								.showMessageDialog(
										this,
										"Cannot remove specie "
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
										this,
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
			dirty = true;
			if (getName().contains("Specie")) {
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
				PromoterPanel panel = new PromoterPanel(selected, list, influences, gcm);
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

	private String[] options = { "Okay", "Cancel" };

	private PropertyList species = null;

	private PropertyList influences = null;

	private PropertyList promoters = null;

	private PropertyList parameters = null;

	private String path = null;

	private BioSim biosim = null;

	private boolean dirty = false;
}
