package gcm2sbml.gui;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AlgebraicRule;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Delay;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.StoichiometryMath;
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.libsbml;

import reb2sac.Reb2Sac;
import biomodelsim.BioSim;
import biomodelsim.Log;
import buttons.Buttons;

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
	private GCMFile gcm = null;

	public GCM2SBMLEditor() {
		this(null);
	}

	public GCM2SBMLEditor(String filename) {
		super();
		gcm = new GCMFile();
		if (filename != null) {
			gcm.load(filename);
			this.filename = filename;
		} else {
			this.filename = "";
		}
		buildGui();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Object o = e.getSource();
			System.out.println(o);
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

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof RunnableGui) {
			((RunnableGui) o).run();
		}
		System.out.println(o);
	}

	private void buildGui() {
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterDown = new JPanel();
		mainPanelCenter.add(mainPanelCenterUp, "North");
		mainPanelCenter.add(mainPanelCenterDown, "South");
		JTextField GCMNameTextField = new JTextField(filename, 50);
		GCMNameTextField.addActionListener(this);
		JLabel GCMNameLabel = new JLabel("GCM Name:");
		mainPanelNorth.add(GCMNameLabel);
		mainPanelNorth.add(GCMNameTextField);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		add(mainPanel);

		SaveButton saveButton = new SaveButton("Save GCM", GCMNameTextField);
		saveButton.addActionListener(this);
		mainPanelCenterDown.add(saveButton);
		saveButton = new SaveButton("Save as SBML", GCMNameTextField);
		saveButton.addActionListener(this);
		mainPanelCenterDown.add(saveButton);

		species = new PropertyList("Species List");
		EditButton addInit = new EditButton("Add Specie", species);
		RemoveButton removeInit = new RemoveButton("Remove Specie", species);
		EditButton editInit = new EditButton("Edit Specie", species);
		species.addAllItem(gcm.getSpecies().keySet());

		JPanel initPanel = Utility.createPanel(this, "Species", species,
				addInit, removeInit, editInit);
		mainPanelCenter.add(initPanel, "West");

		influences = new PropertyList("Influence List");
		addInit = new EditButton("Add Influence", influences);
		removeInit = new RemoveButton("Remove Influence", influences);
		editInit = new EditButton("Edit Influence", influences);
		influences.addAllItem(gcm.getInfluences().keySet());

		initPanel = Utility.createPanel(this, "Influences", influences,
				addInit, removeInit, editInit);
		mainPanelCenter.add(initPanel, "East");
	}

	private class PropertyList extends JList implements RunnableGui,
			NamedObject {
		public PropertyList(String name) {
			super();
			model = new DefaultListModel();
			setModel(model);
			this.name = name;
		}

		public void removeItem(String item) {
			model.removeElement(item);
		}

		public void addItem(String item) {
			if (model.size() == 0) {
				model.addElement(item);
			} else {
				model.ensureCapacity(model.getSize() + 1);
				for (int i = 0; i < model.size(); i++) {
					if (item.compareTo(model.get(i).toString()) < 0) {
						model.add(i, item);
						return;
					}
				}
				model.addElement(item);
			}
		}

		public void addAllItem(Collection<String> items) {
			for (String s : items) {
				addItem(s);
			}
		}

		public String getName() {
			return name;
		}

		public void run() {
		}

		public void changed() {
			model.clear();
			addAllItem(gcm.getInfluences().keySet());
		}

		private String name = "";
		private DefaultListModel model = null;
	}

	// Internal private classes used only by the gui
	private class SaveButton extends AbstractRunnableNamedButton {
		public SaveButton(String name, JTextField fieldNameField) {
			super(name);
			this.fieldNameField = fieldNameField;
		}

		public void run() {
			// Write out species and influences to a gcm file
			gcm.save(fieldNameField.getText());

			if (getName().contains("SBML")) {
				// Then read in the file with the GCMParser
				GCMParser parser = new GCMParser(fieldNameField.getText());
				GeneticNetwork network = parser.buildNetwork();
				// Finally, output to a file
				network.outputSBML(fieldNameField.getText().replace("dot",
						"sbml"));
			}
		}

		private JTextField fieldNameField = null;
	}

	private class RemoveButton extends AbstractRunnableNamedButton {
		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
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
			if (getName().contains("Specie")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				SpeciesPanel panel = new SpeciesPanel(selected, list);
			} else if (getName().contains("Influence")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				InfluencePanel panel = new InfluencePanel(selected, list);
			}

		}

		private PropertyList list = null;
	}

	private class SpeciesPanel extends JPanel {
		public SpeciesPanel(String selected, PropertyList speciesList) {
			super(new GridLayout(3, 3));
			this.selected = selected;
			this.speciesList = speciesList;

			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel typeLabel = new JLabel("Type:");

			JTextField idInput = new JTextField("");
			JTextField nameInput = new JTextField("");
			JComboBox typeInput = new JComboBox(new String[] { "normal",
					"constant", "spastic" });
			typeInput.setSelectedItem("normal");

			this.add(idLabel);
			this.add(idInput);
			this.add(nameLabel);
			this.add(nameInput);
			this.add(typeLabel);
			this.add(typeInput);
			String oldName = null;
			if (selected != null) {
				oldName = selected;
				Properties prop = gcm.getSpecies().get(selected);
				idInput.setText(selected);
				if (prop.containsKey("type")
						&& prop.getProperty("type").equals("spastic")) {
					typeInput.setSelectedItem("spastic");
				} else if (prop.containsKey("const")
						&& prop.getProperty("const").equals("true")) {
					typeInput.setSelectedItem("constant");
				}
				if (prop.containsKey("label")) {
					nameInput.setText(prop.getProperty("label").replace("\"",
							""));
				}

			}

			int value = JOptionPane.showOptionDialog(new JFrame(), this,
					"Species Editor", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				String id = idInput.getText();
				String name = nameInput.getText();

				// Check to see if we need to add or edit
				Properties property = new Properties();
				String string = "";
				if (typeInput.getSelectedItem().equals("constant")) {
					property.put("const", "true");
				} else if (typeInput.getSelectedItem().equals("spastic")) {
					name = "spastic_" + name;
				} 
				property.put("label", "\""+nameInput.getText()+"\"");

				if (selected != null && !oldName.equals(id)) {
					gcm.changeSpeciesName(oldName, id);	
					influences.changed();
				}
				gcm.addSpecies(id, property);
				speciesList.removeItem(oldName);
				speciesList.addItem(id);

			} else if (value == JOptionPane.NO_OPTION) {
				System.out.println();
			}
		}

		private String selected = "";
		private PropertyList speciesList = null;
	}

	// TODO: Refactor this class, can OOP this class out to be more general
	// very easily
	private class InfluencePanel extends JPanel {
		public InfluencePanel(String selected, PropertyList list) {
			super(new GridLayout(8, 2));
			this.selected = selected;
			this.list = list;

			JLabel idLabel = new JLabel("Name:");
			JLabel inputLabel = new JLabel("Type:");
			JLabel outputLabel = new JLabel("Output:");
			JLabel typeLabel = new JLabel("Type:");
			JLabel bioLabel = new JLabel("Biochemical:");
			JLabel coopLabel = new JLabel("Cooperativity:");
			JLabel dimerLabel = new JLabel("Dimer:");

			JTextField idInput = new JTextField("");
			idInput.setEditable(false);
			JComboBox inputInput = new JComboBox(new Vector<String>(gcm
					.getSpecies().keySet()));
			JComboBox outputInput = new JComboBox(new Vector<String>(gcm
					.getSpecies().keySet()));
			JComboBox typeInput = new JComboBox(new String[] { "activation",
					"repression" });
			typeInput.setSelectedItem("repression");
			JComboBox bioInput = new JComboBox(new String[] { "yes", "no" });
			bioInput.setSelectedItem("no");
			JTextField coopInput = new JTextField("1");
			JTextField dimerInput = new JTextField("1");

			this.add(idLabel);
			this.add(idInput);
			this.add(inputLabel);
			this.add(inputInput);
			this.add(outputLabel);
			this.add(outputInput);
			this.add(typeLabel);
			this.add(typeInput);
			this.add(bioLabel);
			this.add(bioInput);
			this.add(coopLabel);
			this.add(coopInput);
			this.add(dimerLabel);
			this.add(dimerInput);
			String oldName = null;
			if (selected != null) {
				oldName = gcm.getInput(selected) + " -> "
						+ gcm.getOutput(selected);
				Properties prop = gcm.getInfluences().get(selected);
				idInput.setText(gcm.getInput(selected) + " -> "
						+ gcm.getOutput(selected));
				inputInput.setSelectedItem(gcm.getInput(selected));
				outputInput.setSelectedItem(gcm.getOutput(selected));
				if (prop.get("arrowhead").equals("vee")) {
					typeInput.setSelectedItem("activation");
				} else {
					typeInput.setSelectedItem("repression");
				}
				if (prop.containsKey("type")
						&& prop.getProperty("constant").equals("biochemical")) {
					bioInput.setSelectedItem("yes");
				} else {
					bioInput.setSelectedItem("no");
				}
				if (prop.containsKey("coop")) {
					coopInput.setText(prop.getProperty("coop")
							.replace("\"", ""));
				}
				if (prop.containsKey("label")) {
					dimerInput.setText(prop.getProperty("label").replace("\"",
							""));
				}

			}

			int value = JOptionPane.showOptionDialog(new JFrame(), this,
					"Influence Editor", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				String name = inputInput.getSelectedItem() + " -> "
						+ outputInput.getSelectedItem();

				// Check to see if we need to add or edit
				Properties property = new Properties();
				String string = "";
				if (typeInput.getSelectedItem().equals("activation")) {
					string = "vee";
				} else {
					string = "tee";
				}
				property.put("arrowhead", string);
				if (bioInput.getSelectedItem().equals("yes")) {
					string = "biochemical";
					property.put("type", value);
				}
				property.put("coop", "\"" + coopInput.getText() + "\"");
				property.put("label", "\"" + dimerInput.getText() + "\"");

				gcm.removeInfluence(oldName);
				gcm.addInfluences(name, property);
				list.removeItem(oldName);
				list.addItem(name);

			} else if (value == JOptionPane.NO_OPTION) {
				System.out.println();
			}
		}

		private String selected = "";
		private PropertyList list = null;
	}

	private String[] options = { "Okay", "Cancel" };
	private PropertyList species = null;
	private PropertyList influences = null;
}
