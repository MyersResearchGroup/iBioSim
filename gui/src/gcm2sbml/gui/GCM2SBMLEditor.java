package gcm2sbml.gui;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Reaction;
import gcm2sbml.network.SpeciesInterface;
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
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
		System.out.println(o);
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
		GCMNameTextField.addActionListener(this);
		JLabel GCMNameLabel = new JLabel("GCM Id:");
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
		parameters.addAllItem(gcm.getDefaultParameters().keySet());
		initPanel = Utility.createPanel(this, "Parameters", parameters, null,
				null, editInit);
		
				
		mainPanelCenterCenter.add(initPanel);
	}

	private class PropertyList extends JList implements Runnable, NamedObject {
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
			return this.name;
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
			gcm.save(path + File.separator + fieldNameField.getText() + ".gcm");

			if (getName().contains("SBML")) {
				// Then read in the file with the GCMParser
				GCMParser parser = new GCMParser(path + File.separator
						+ fieldNameField.getText() + ".gcm");
				GeneticNetwork network = parser.buildNetwork();
				network.loadProperties(gcm);
				// Finally, output to a file
				network.outputSBML(path + File.separator
						+ fieldNameField.getText() + ".sbml");
				biosim.refreshTree();
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
			} else if (getName().contains("Promoter")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				PromoterPanel panel = new PromoterPanel(selected, list);
			}
			else if (getName().contains("Parameter")) {
				String selected = null;
				if (list.getSelectedValue() != null
						&& getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
				}
				ParameterPanel panel = new ParameterPanel(selected, list);
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;
		private PropertyList list = null;
	}

	private class ParameterPanel extends JPanel {
		public ParameterPanel(String selected, PropertyList parameterList) {
			super(new GridLayout(1, 2));
			this.selected = selected;
			this.parameterList = parameterList;
			JLabel nameLabel = new JLabel(selected);
			JTextField valueInput = new JTextField(40);
			this.add(nameLabel);
			this.add(valueInput);

			// JLabel kcoopLabel = new JLabel("kcoop:");
			// JTextField kcoopInput = new JTextField(40);
			// JLabel kbioLabel = new JLabel("kbio:");
			// JTextField kbioInput = new JTextField(40);
			// JLabel kdimerLabel = new JLabel("kdimer:");
			// JTextField kdimerInput = new JTextField(40);
			// JLabel krepLabel = new JLabel("krep:");
			// JTextField krepInput = new JTextField(40);
			// JLabel promotersLabel = new JLabel("promoters:");
			// JTextField promotersInput = new JTextField(40);
			// JLabel kactLabel = new JLabel("kact:");
			// JTextField kactInput = new JTextField(40);
			// JLabel ocrLabel = new JLabel("ocr:");
			// JTextField ocrInput = new JTextField(40);
			// JLabel stocLabel = new JLabel("stoc:");
			// JTextField stocInput = new JTextField(40);
			// JLabel activatedLabel = new JLabel("activated:");
			// JTextField activatedInput = new JTextField(40);
			// JLabel basalLabel = new JLabel("basal:");
			// JTextField basalInput = new JTextField(40);
			// JLabel RNAPLabel = new JLabel("RNAP:");
			// JTextField RNAPInput = new JTextField(40);
			// JLabel decayLabel = new JLabel("decay:");
			// JTextField decayInput = new JTextField(40);
			// JLabel rnap_bindingLabel = new JLabel("rnap_binding:");
			// JTextField rnap_bindingInput = new JTextField(40);
			//									
			// this.add(kcoopLabel);
			// this.add(kcoopInput);
			// this.add(kbioLabel);
			// this.add(kbioInput);
			// this.add(kdimerLabel);
			// this.add(kdimerInput);
			// this.add(krepLabel);
			// this.add(krepInput);
			// this.add(promotersLabel);
			// this.add(promotersInput);
			// this.add(kactLabel);
			// this.add(kactInput);
			// this.add(ocrLabel);
			// this.add(ocrInput);
			// this.add(stocLabel);
			// this.add(stocInput);
			// this.add(activatedLabel);
			// this.add(activatedInput);
			// this.add(basalLabel);
			// this.add(basalInput);
			// this.add(RNAPLabel);
			// this.add(RNAPInput);
			// this.add(decayLabel);
			// this.add(decayInput);
			// this.add(rnap_bindingLabel);
			// this.add(rnap_bindingInput);

			valueInput.setText(gcm.getParameter(nameLabel.getText()) + "");

			int value = JOptionPane.showOptionDialog(new JFrame(), this,
					"Parameter Editor", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				gcm.setParameter(nameLabel.getText(), valueInput.getText());
			} else if (value == JOptionPane.NO_OPTION) {
				System.out.println();
			}
		}

		private String selected = "";
		private PropertyList parameterList = null;
	}

	private class SpeciesPanel extends JPanel {
		public SpeciesPanel(String selected, PropertyList speciesList) {
			super(new GridLayout(6, 2));
			this.selected = selected;
			this.speciesList = speciesList;

			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel typeLabel = new JLabel("Type:");
			JLabel decayLabel = new JLabel("Degradation:");
			JLabel dimerizationLabel = new JLabel("Dimerization Value:");
			JLabel rateLabel = new JLabel("Association Constant:");

			JTextField idInput = new JTextField(40);
			JTextField nameInput = new JTextField(40);
			JComboBox typeInput = new JComboBox(new String[] { "normal",
					"constant", "spastic" });
			typeInput.setSelectedItem("normal");
			JTextField decayInput = new JTextField(40);
			JTextField dimerizationInput = new JTextField(40);
			JTextField rateInput = new JTextField(40);

			this.add(idLabel);
			this.add(idInput);
			this.add(nameLabel);
			this.add(nameInput);
			this.add(typeLabel);
			this.add(typeInput);
			this.add(decayLabel);
			this.add(decayInput);
			this.add(dimerizationLabel);
			this.add(dimerizationInput);
			this.add(rateLabel);
			this.add(rateInput);

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
				if (prop.containsKey(SpeciesInterface.MAX_DIMER)) {
					dimerizationInput.setText(prop.getProperty(
							SpeciesInterface.MAX_DIMER).replace("\"", ""));
				} else {
					dimerizationInput.setText(Utility.DIMER);
				}
				if (prop.containsKey(SpeciesInterface.DECAY)) {
					decayInput.setText(prop.getProperty(SpeciesInterface.DECAY)
							.replace("\"", ""));
				} else {
					decayInput.setText(Utility.DECAY);
				}
				if (prop.containsKey(SpeciesInterface.DIMER_CONST)) {
					rateInput.setText(prop.getProperty(
							SpeciesInterface.DIMER_CONST).replace("\"", ""));
				} else {
					rateInput.setText(Utility.KDIMER);
				}
			} else {
				decayInput.setText(gcm.getParameter(SpeciesInterface.DECAY)
						.replace("\"", ""));
				dimerizationInput.setText("0");
				rateInput.setText(gcm.getParameter(GeneticNetwork.KCOOP));
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
				property.put("label", "\"" + name + "\"");
				property.put(SpeciesInterface.MAX_DIMER, "\""
						+ dimerizationInput.getText() + "\"");
				property.put(SpeciesInterface.DIMER_CONST, "\""
						+ rateInput.getText() + "\"");
				property.put(SpeciesInterface.DECAY, "\""
						+ decayInput.getText() + "\"");
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
			super(new GridLayout(9, 2));
			this.selected = selected;
			this.list = list;

			JLabel idLabel = new JLabel("Name:");
			JLabel inputLabel = new JLabel("Input:");
			JLabel outputLabel = new JLabel("Output:");
			JLabel promoterLabel = new JLabel("Promoter:");
			JLabel typeLabel = new JLabel("Type:");
			JLabel bioLabel = new JLabel("Biochemical:");
			JLabel coopLabel = new JLabel("Cooperativity:");
			JLabel dimerLabel = new JLabel("Dimer:");
			JLabel bindingLabel = new JLabel("Binding Rate:");

			JTextField idInput = new JTextField("");
			idInput.setEditable(false);
			JComboBox inputInput = new JComboBox(new Vector<String>(gcm
					.getSpecies().keySet()));
			JComboBox outputInput = new JComboBox(new Vector<String>(gcm
					.getSpecies().keySet()));
			JComboBox promoterInput = new JComboBox(gcm.getPromotersAsArray());
			promoterInput.setSelectedItem("none");
			JComboBox typeInput = new JComboBox(new String[] { "activation",
					"repression" });

			typeInput.setSelectedItem("repression");
			JComboBox bioInput = new JComboBox(new String[] { "yes", "no" });
			bioInput.setSelectedItem("no");
			JTextField coopInput = new JTextField("1");
			JTextField dimerInput = new JTextField("1");
			JTextField bindingInput = new JTextField("1");

			this.add(idLabel);
			this.add(idInput);
			this.add(inputLabel);
			this.add(inputInput);
			this.add(outputLabel);
			this.add(outputInput);
			this.add(promoterLabel);
			this.add(promoterInput);
			this.add(typeLabel);
			this.add(typeInput);
			this.add(bioLabel);
			this.add(bioInput);
			this.add(coopLabel);
			this.add(coopInput);
			this.add(dimerLabel);
			this.add(dimerInput);
			this.add(bindingLabel);
			this.add(bindingInput);
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
				if (prop.containsKey(Reaction.KBINDING)) {
					bindingInput.setText(prop.getProperty(Reaction.KBINDING)
							.replace("\"", ""));
				}
				if (prop.containsKey(Reaction.PROMOTER)) {
					promoterInput.setSelectedItem(prop.getProperty(
							Reaction.PROMOTER).replace("\"", ""));
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
				property.put(Reaction.KBINDING, "\"" + bindingInput.getText()
						+ "\"");
				if (!promoterInput.getSelectedItem().equals("none")) {
					property.put(Reaction.PROMOTER, "\""
							+ promoterInput.getSelectedItem() + "\"");
				}
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

	private class PromoterPanel extends JPanel {
		public PromoterPanel(String selected, PropertyList list) {
			super(new GridLayout(6, 2));
			this.selected = selected;
			this.list = list;

			JLabel idLabel = new JLabel("Name:");
			JLabel productionLabel = new JLabel("Production Rate:");
			JLabel RNAPLabel = new JLabel("RNAP Binding Rate:");
			JLabel stoichiometryLabel = new JLabel("stoichiometry:");
			JLabel activatedLabel = new JLabel("Activated Production Rate:");
			JLabel basalLabel = new JLabel("Basal Production Rate:");

			JTextField idInput = new JTextField("");
			JTextField productionInput = new JTextField(".25");
			JTextField RNAPInput = new JTextField(".033");
			JTextField stoichiometryInput = new JTextField("10");
			JTextField activatedInput = new JTextField(".25");
			JTextField basalInput = new JTextField(".0001");

			this.add(idLabel);
			this.add(idInput);
			this.add(productionLabel);
			this.add(productionInput);
			this.add(RNAPLabel);
			this.add(RNAPInput);
			this.add(stoichiometryLabel);
			this.add(stoichiometryInput);
			this.add(activatedLabel);
			this.add(activatedInput);
			this.add(basalLabel);
			this.add(basalInput);
			String oldName = null;
			if (selected != null) {
				oldName = selected;
				Properties prop = gcm.getPromoters().get(selected);
				idInput.setText(selected);
			}

			int value = JOptionPane.showOptionDialog(new JFrame(), this,
					"Promoter Editor", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				String name = idInput.getText();

				// Check to see if we need to add or edit
				Properties property = new Properties();
				String string = "";

				if (oldName != null && !oldName.equals(name)) {
					gcm.changePromoterName(oldName, name);
					list.removeItem(oldName);
					list.addItem(name);
					gcm.addPromoter(name, property);
				} else if (oldName == null) {
					list.addItem(name);
					gcm.addPromoter(name, property);
				} else {
					gcm.addPromoter(name, property);
				}
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
	private PropertyList promoters = null;
	private PropertyList parameters = null;
	private String path = null;
	private BioSim biosim = null;
}
