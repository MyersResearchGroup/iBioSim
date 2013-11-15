package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesType;

import biomodel.parser.BioModel;


/**
 * This is a class for creating SBML species types
 * 
 * @author Chris Myers
 * 
 */
public class SpeciesTypes extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addSpecType, removeSpecType, editSpecType;

	private JList specTypes; // JList of species types

	private BioModel gcm;

	private MutableBoolean dirty;

	private Gui biosim;

	public SpeciesTypes(Gui biosim, BioModel gcm, MutableBoolean dirty) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.biosim = biosim;
		this.dirty = dirty;
		Model model = gcm.getSBMLDocument().getModel();
		addSpecType = new JButton("Add Type");
		removeSpecType = new JButton("Remove Type");
		editSpecType = new JButton("Edit Type");
		specTypes = new JList();
		ListOf listOfSpeciesTypes = model.getListOfSpeciesTypes();
		String[] spTyp = new String[(int) model.getSpeciesTypeCount()];
		for (int i = 0; i < model.getSpeciesTypeCount(); i++) {
			SpeciesType specType = (SpeciesType) listOfSpeciesTypes.get(i);
			spTyp[i] = specType.getId();
		}
		JPanel addRem = new JPanel();
		addRem.add(addSpecType);
		addRem.add(removeSpecType);
		addRem.add(editSpecType);
		addSpecType.addActionListener(this);
		removeSpecType.addActionListener(this);
		editSpecType.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Species Types:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(specTypes);
		Utility.sort(spTyp);
		specTypes.setListData(spTyp);
		specTypes.setSelectedIndex(0);
		specTypes.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit species types or create new ones.
	 */
	private void specTypeEditor(String option) {
		if (option.equals("OK") && specTypes.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No species type selected.", "Must Select a Species Type", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel specTypePanel = new JPanel();
		JPanel spTypPanel = new JPanel(new GridLayout(2, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JTextField specTypeID = new JTextField(12);
		JTextField specTypeName = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				SpeciesType specType = gcm.getSBMLDocument().getModel().getSpeciesType((((String) specTypes.getSelectedValue()).split(" ")[0]));
				specTypeID.setText(specType.getId());
				selectedID = specType.getId();
				specTypeName.setText(specType.getName());
			}
			catch (Exception e) {
			}
		}
		spTypPanel.add(idLabel);
		spTypPanel.add(specTypeID);
		spTypPanel.add(nameLabel);
		spTypPanel.add(specTypeName);
		specTypePanel.add(spTypPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, specTypePanel, "Species Type Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), specTypeID.getText().trim(), selectedID, false, false);
			if (!error) {
				if (option.equals("OK")) {
					String[] spTyp = new String[specTypes.getModel().getSize()];
					for (int i = 0; i < specTypes.getModel().getSize(); i++) {
						spTyp[i] = specTypes.getModel().getElementAt(i).toString();
					}
					int index = specTypes.getSelectedIndex();
					String val = ((String) specTypes.getSelectedValue()).split(" ")[0];
					specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					spTyp = Utility.getList(spTyp, specTypes);
					specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					SpeciesType s = gcm.getSBMLDocument().getModel().getSpeciesType(val);
					s.setId(specTypeID.getText().trim());
					s.setName(specTypeName.getText().trim());
					spTyp[index] = specTypeID.getText().trim();
					Utility.sort(spTyp);
					specTypes.setListData(spTyp);
					specTypes.setSelectedIndex(index);
					for (int i = 0; i < gcm.getSBMLDocument().getModel().getSpeciesCount(); i++) {
						Species species = gcm.getSBMLDocument().getModel().getSpecies(i);
						if (species.getSpeciesType().equals(val)) {
							species.setSpeciesType(specTypeID.getText().trim());
						}
					}
				}
				else {
					String[] spTyp = new String[specTypes.getModel().getSize()];
					for (int i = 0; i < specTypes.getModel().getSize(); i++) {
						spTyp[i] = specTypes.getModel().getElementAt(i).toString();
					}
					int index = specTypes.getSelectedIndex();
					SpeciesType s = gcm.getSBMLDocument().getModel().createSpeciesType();
					s.setId(specTypeID.getText().trim());
					s.setName(specTypeName.getText().trim());
					JList add = new JList();
					Object[] adding = { specTypeID.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(spTyp, specTypes, add, null, null, null, null, null, Gui.frame);
					spTyp = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						spTyp[i] = (String) adding[i];
					}
					Utility.sort(spTyp);
					specTypes.setListData(spTyp);
					specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (gcm.getSBMLDocument().getModel().getSpeciesTypeCount() == 1) {
						specTypes.setSelectedIndex(0);
					}
					else {
						specTypes.setSelectedIndex(index);
					}
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, specTypePanel, "Species Type Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a species type
	 */
	private void removeSpecType() {
		int index = specTypes.getSelectedIndex();
		if (index != -1) {
			boolean remove = true;
			ArrayList<String> speciesUsing = new ArrayList<String>();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getSpeciesCount(); i++) {
				Species species = (Species) gcm.getSBMLDocument().getModel().getListOfSpecies().get(i);
				if (species.isSetSpeciesType()) {
					if (species.getSpeciesType().equals(((String) specTypes.getSelectedValue()).split(" ")[0])) {
						remove = false;
						speciesUsing.add(species.getId());
					}
				}
			}
			if (remove) {
				SpeciesType tempSpecType = gcm.getSBMLDocument().getModel().getSpeciesType(((String) specTypes.getSelectedValue()).split(" ")[0]);
				ListOf s = gcm.getSBMLDocument().getModel().getListOfSpeciesTypes();
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getSpeciesTypeCount(); i++) {
					if (((SpeciesType) s.get(i)).getId().equals(tempSpecType.getId())) {
						s.remove(i);
					}
				}
				specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(specTypes);
				specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < specTypes.getModel().getSize()) {
					specTypes.setSelectedIndex(index);
				}
				else {
					specTypes.setSelectedIndex(index - 1);
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
			else {
				String species = "";
				String[] specs = speciesUsing.toArray(new String[0]);
				Utility.sort(specs);
				for (int i = 0; i < specs.length; i++) {
					if (i == specs.length - 1) {
						species += specs[i];
					}
					else {
						species += specs[i] + "\n";
					}
				}
				String message = "Unable to remove the selected species type.";
				if (speciesUsing.size() != 0) {
					message += "\n\nIt is used by the following species:\n" + species;
				}
				JTextArea messageArea = new JTextArea(message);
				messageArea.setEditable(false);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(300, 300));
				scroll.setPreferredSize(new Dimension(300, 300));
				scroll.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Species Type", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		if (e.getSource() == addSpecType) {
			specTypeEditor("Add");
		}
		// if the edit species type button is clicked
		else if (e.getSource() == editSpecType) {
			specTypeEditor("OK");
		}
		// if the remove species type button is clicked
		else if (e.getSource() == removeSpecType) {
			removeSpecType();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == specTypes) {
				specTypeEditor("OK");
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
