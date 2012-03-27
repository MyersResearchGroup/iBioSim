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

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;

import biomodel.parser.BioModel;


/**
 * This is a class for creating SBML compartment types
 * 
 * @author Chris Myers
 * 
 */
public class CompartmentTypes extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addCompType, removeCompType, editCompType;

	private JList compTypes; // JList of compartment types

	private BioModel gcm;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Gui biosim;

	/* Create initial assignment panel */
	public CompartmentTypes(Gui biosim, BioModel gcm, ArrayList<String> usedIDs, MutableBoolean dirty) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.usedIDs = usedIDs;
		this.biosim = biosim;
		this.dirty = dirty;
		Model model = gcm.getSBMLDocument().getModel();
		addCompType = new JButton("Add Type");
		removeCompType = new JButton("Remove Type");
		editCompType = new JButton("Edit Type");
		compTypes = new JList();
		ListOf listOfCompartmentTypes = model.getListOfCompartmentTypes();
		String[] cpTyp = new String[(int) model.getNumCompartmentTypes()];
		for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
			CompartmentType compType = (CompartmentType) listOfCompartmentTypes.get(i);
			cpTyp[i] = compType.getId();
		}
		JPanel addRem = new JPanel();
		addRem.add(addCompType);
		addRem.add(removeCompType);
		addRem.add(editCompType);
		addCompType.addActionListener(this);
		removeCompType.addActionListener(this);
		editCompType.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Compartment Types:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(compTypes);
		Utility.sort(cpTyp);
		compTypes.setListData(cpTyp);
		compTypes.setSelectedIndex(0);
		compTypes.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit compartment types or create new ones.
	 */
	private void compTypeEditor(String option) {
		if (option.equals("OK") && compTypes.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No compartment type selected.", "Must Select a Compartment Type", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel compTypePanel = new JPanel();
		JPanel cpTypPanel = new JPanel(new GridLayout(2, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JTextField compTypeID = new JTextField(12);
		JTextField compTypeName = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				CompartmentType compType = gcm.getSBMLDocument().getModel().getCompartmentType((((String) compTypes.getSelectedValue()).split(" ")[0]));
				compTypeID.setText(compType.getId());
				selectedID = compType.getId();
				compTypeName.setText(compType.getName());
			}
			catch (Exception e) {
			}
		}
		cpTypPanel.add(idLabel);
		cpTypPanel.add(compTypeID);
		cpTypPanel.add(nameLabel);
		cpTypPanel.add(compTypeName);
		compTypePanel.add(cpTypPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, compTypePanel, "Compartment Type Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), usedIDs, compTypeID.getText().trim(), selectedID, false);
			if (!error) {
				if (option.equals("OK")) {
					String[] cpTyp = new String[compTypes.getModel().getSize()];
					for (int i = 0; i < compTypes.getModel().getSize(); i++) {
						cpTyp[i] = compTypes.getModel().getElementAt(i).toString();
					}
					int index = compTypes.getSelectedIndex();
					String val = ((String) compTypes.getSelectedValue()).split(" ")[0];
					compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					cpTyp = Utility.getList(cpTyp, compTypes);
					compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					CompartmentType c = gcm.getSBMLDocument().getModel().getCompartmentType(val);
					c.setId(compTypeID.getText().trim());
					c.setName(compTypeName.getText().trim());
					for (int i = 0; i < usedIDs.size(); i++) {
						if (usedIDs.get(i).equals(val)) {
							usedIDs.set(i, compTypeID.getText().trim());
						}
					}
					cpTyp[index] = compTypeID.getText().trim();
					Utility.sort(cpTyp);
					compTypes.setListData(cpTyp);
					compTypes.setSelectedIndex(index);
					for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumCompartments(); i++) {
						Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment(i);
						if (compartment.getCompartmentType().equals(val)) {
							compartment.setCompartmentType(compTypeID.getText().trim());
						}
					}
				}
				else {
					String[] cpTyp = new String[compTypes.getModel().getSize()];
					for (int i = 0; i < compTypes.getModel().getSize(); i++) {
						cpTyp[i] = compTypes.getModel().getElementAt(i).toString();
					}
					int index = compTypes.getSelectedIndex();
					CompartmentType c = gcm.getSBMLDocument().getModel().createCompartmentType();
					c.setId(compTypeID.getText().trim());
					c.setName(compTypeName.getText().trim());
					usedIDs.add(compTypeID.getText().trim());
					JList add = new JList();
					Object[] adding = { compTypeID.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(cpTyp, compTypes, add, null, null, null, null, null, Gui.frame);
					cpTyp = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						cpTyp[i] = (String) adding[i];
					}
					Utility.sort(cpTyp);
					compTypes.setListData(cpTyp);
					compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (gcm.getSBMLDocument().getModel().getNumCompartmentTypes() == 1) {
						compTypes.setSelectedIndex(0);
					}
					else {
						compTypes.setSelectedIndex(index);
					}
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, compTypePanel, "Compartment Type Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a compartment type
	 */
	private void removeCompType() {
		int index = compTypes.getSelectedIndex();
		if (index != -1) {
			boolean remove = true;
			ArrayList<String> compartmentUsing = new ArrayList<String>();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) gcm.getSBMLDocument().getModel().getListOfCompartments().get(i);
				if (compartment.isSetCompartmentType()) {
					if (compartment.getCompartmentType().equals(((String) compTypes.getSelectedValue()).split(" ")[0])) {
						remove = false;
						compartmentUsing.add(compartment.getId());
					}
				}
			}
			if (remove) {
				CompartmentType tempCompType = gcm.getSBMLDocument().getModel().getCompartmentType(((String) compTypes.getSelectedValue()).split(" ")[0]);
				ListOf c = gcm.getSBMLDocument().getModel().getListOfCompartmentTypes();
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumCompartmentTypes(); i++) {
					if (((CompartmentType) c.get(i)).getId().equals(tempCompType.getId())) {
						c.remove(i);
					}
				}
				usedIDs.remove(((String) compTypes.getSelectedValue()).split(" ")[0]);
				compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(compTypes);
				compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < compTypes.getModel().getSize()) {
					compTypes.setSelectedIndex(index);
				}
				else {
					compTypes.setSelectedIndex(index - 1);
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
			else {
				String compartment = "";
				String[] comps = compartmentUsing.toArray(new String[0]);
				Utility.sort(comps);
				for (int i = 0; i < comps.length; i++) {
					if (i == comps.length - 1) {
						compartment += comps[i];
					}
					else {
						compartment += comps[i] + "\n";
					}
				}
				String message = "Unable to remove the selected compartment type.";
				if (compartmentUsing.size() != 0) {
					message += "\n\nIt is used by the following compartments:\n" + compartment;
				}
				JTextArea messageArea = new JTextArea(message);
				messageArea.setEditable(false);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(300, 300));
				scroll.setPreferredSize(new Dimension(300, 300));
				scroll.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Compartment Type", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		if (e.getSource() == addCompType) {
			compTypeEditor("Add");
		}
		// if the edit compartment type button is clicked
		else if (e.getSource() == editCompType) {
			compTypeEditor("OK");
		}
		// if the remove compartment type button is clicked
		else if (e.getSource() == removeCompType) {
			removeCompType();
		}

	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == compTypes) {
				compTypeEditor("OK");
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
