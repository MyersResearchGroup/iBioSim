package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.XMLNode;


/**
 * This is a class for creating SBML constraints
 * 
 * @author Chris Myers
 * 
 */
public class Constraints extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addConstraint, removeConstraint, editConstraint;

	private JList constraints; // JList of initial assignments

	private SBMLDocument document;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	/* Create initial assignment panel */
	public Constraints(SBMLDocument document, ArrayList<String> usedIDs, MutableBoolean dirty) {
		super(new BorderLayout());
		this.document = document;
		this.usedIDs = usedIDs;
		this.dirty = dirty;
		Model model = document.getModel();
		addConstraint = new JButton("Add Constraint");
		removeConstraint = new JButton("Remove Constraint");
		editConstraint = new JButton("Edit Constraint");
		constraints = new JList();
		ListOf listOfConstraints = model.getListOfConstraints();
		String[] cons = new String[(int) model.getNumConstraints()];
		for (int i = 0; i < model.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) listOfConstraints.get(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "constraint0";
				int cn = 0;
				while (usedIDs.contains(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
				usedIDs.add(constraintId);
				constraint.setMetaId(constraintId);
			}
			cons[i] = constraint.getMetaId();
		}
		JPanel addRem = new JPanel();
		addRem.add(addConstraint);
		addRem.add(removeConstraint);
		addRem.add(editConstraint);
		addConstraint.addActionListener(this);
		removeConstraint.addActionListener(this);
		editConstraint.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Constraints:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(constraints);
		Utility.sort(cons);
		constraints.setListData(cons);
		constraints.setSelectedIndex(0);
		constraints.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit constraints or create new ones.
	 */
	private void constraintEditor(String option) {
		if (option.equals("OK") && constraints.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No constraint selected.", "Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel constraintPanel = new JPanel();
		JPanel consPanel = new JPanel(new BorderLayout());
		JPanel IDPanel = new JPanel();
		JPanel mathPanel = new JPanel(new BorderLayout());
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel IDLabel = new JLabel("ID:");
		JLabel mathLabel = new JLabel("Constraint:");
		JLabel messageLabel = new JLabel("Messsage:");
		JTextField consID = new JTextField(12);
		
		JTextArea consMath = new JTextArea(3,30);
		consMath.setLineWrap(true);
		consMath.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(100, 100));
		scroll.setPreferredSize(new Dimension(100, 100));
		scroll.setViewportView(consMath);
		
		JTextArea consMessage = new JTextArea(3,30);
		consMessage.setLineWrap(true);
		consMessage.setWrapStyleWord(true);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(100, 100));
		scroll2.setPreferredSize(new Dimension(100, 100));
		scroll2.setViewportView(consMessage);

		String selectedID = "";
		int Cindex = -1;
		if (option.equals("OK")) {
			String selected = ((String) constraints.getSelectedValue());
			ListOf c = document.getModel().getListOfConstraints();
			for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
				if ((((Constraint) c.get(i)).getMetaId()).equals(selected)) {
					Cindex = i;
					consMath.setText(SBMLutilities.myFormulaToString(((Constraint) c.get(i)).getMath()));
					if (((Constraint) c.get(i)).isSetMetaId()) {
						selectedID = ((Constraint) c.get(i)).getMetaId();
						consID.setText(selectedID);
					}
					if (((Constraint) c.get(i)).isSetMessage()) {
						String message = ((Constraint) c.get(i)).getMessageString();
						// XMLNode.convertXMLNodeToString(((Constraint)
						// c.get(i)).getMessage());
						message = message.substring(message.indexOf("xhtml\">") + 7, message.indexOf("</p>"));
						consMessage.setText(message);
					}
				}
			}
		}
		else {
			String constraintId = "constraint0";
			int cn = 0;
			while (usedIDs.contains(constraintId)) {
				cn++;
				constraintId = "constraint" + cn;
			}
			consID.setText(constraintId);
		}
		IDPanel.add(IDLabel);
		IDPanel.add(consID);
		mathPanel.add(mathLabel,"North");
		mathPanel.add(scroll,"Center");
		messagePanel.add(messageLabel,"North");
		messagePanel.add(scroll2,"Center");
		consPanel.add(IDPanel,"North");
		consPanel.add(mathPanel,"Center");
		consPanel.add(messagePanel,"South");
		constraintPanel.add(consPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, constraintPanel, "Constraint Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(document, usedIDs, consID.getText().trim(), selectedID, false);
			if (!error) {
				if (consMath.getText().trim().equals("") || SBMLutilities.myParseFormula(consMath.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!SBMLutilities.myParseFormula(consMath.getText().trim()).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Constraint formula must be of type Boolean.", "Enter Valid Formula",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(consMath.getText().trim()))) {
					error = true;
				}
				else {
					ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(document, consMath.getText().trim(), "", false);
					if (invalidVars.size() > 0) {
						String invalid = "";
						for (int i = 0; i < invalidVars.size(); i++) {
							if (i == invalidVars.size() - 1) {
								invalid += invalidVars.get(i);
							}
							else {
								invalid += invalidVars.get(i) + "\n";
							}
						}
						String message;
						message = "Constraint contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					if (option.equals("OK")) {
						String[] cons = new String[constraints.getModel().getSize()];
						for (int i = 0; i < constraints.getModel().getSize(); i++) {
							cons[i] = constraints.getModel().getElementAt(i).toString();
						}
						int index = constraints.getSelectedIndex();
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						cons = Utility.getList(cons, constraints);
						constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Constraint c = (Constraint) (document.getModel().getListOfConstraints()).get(Cindex);
						c.setMath(SBMLutilities.myParseFormula(consMath.getText().trim()));
						c.setMetaId(consID.getText().trim());
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
									+ consMessage.getText().trim() + "</p></message>");
							c.setMessage(xmlNode);
						}
						else {
							c.unsetMessage();
						}
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(selectedID)) {
								usedIDs.set(i, consID.getText().trim());
							}
						}
						cons[index] = c.getMetaId();
						Utility.sort(cons);
						constraints.setListData(cons);
						constraints.setSelectedIndex(index);
					}
					else {
						String[] cons = new String[constraints.getModel().getSize()];
						for (int i = 0; i < constraints.getModel().getSize(); i++) {
							cons[i] = constraints.getModel().getElementAt(i).toString();
						}
						JList add = new JList();
						int index = constraints.getSelectedIndex();
						Constraint c = document.getModel().createConstraint();
						c.setMath(SBMLutilities.myParseFormula(consMath.getText().trim()));
						c.setMetaId(consID.getText().trim());
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
									+ consMessage.getText().trim() + "</p></message>");
							c.setMessage(xmlNode);
						}
						usedIDs.add(consID.getText().trim());
						Object[] adding = { c.getMetaId() };
						add.setListData(adding);
						add.setSelectedIndex(0);
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(cons, constraints, add, false, null, null, null, null, null, null, Gui.frame);
						cons = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							cons[i] = (String) adding[i];
						}
						Utility.sort(cons);
						constraints.setListData(cons);
						constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (document.getModel().getNumConstraints() == 1) {
							constraints.setSelectedIndex(0);
						}
						else {
							constraints.setSelectedIndex(index);
						}
					}
					dirty.setValue(true);
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, constraintPanel, "Constraint Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a constraint
	 */
	private void removeConstraint() {
		int index = constraints.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) constraints.getSelectedValue());
			ListOf c = document.getModel().getListOfConstraints();
			for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
				if ((((Constraint) c.get(i)).getMetaId()).equals(selected)) {
					usedIDs.remove(((Constraint) c.get(i)).getMetaId());
					c.remove(i);
					break;
				}
			}
			constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(constraints);
			constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < constraints.getModel().getSize()) {
				constraints.setSelectedIndex(index);
			}
			else {
				constraints.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		// if the add constraint button is clicked
		if (e.getSource() == addConstraint) {
			constraintEditor("Add");
		}
		// if the edit constraint button is clicked
		else if (e.getSource() == editConstraint) {
			constraintEditor("OK");
		}
		// if the remove constraint button is clicked
		else if (e.getSource() == removeConstraint) {
			removeConstraint();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == constraints) {
				constraintEditor("OK");
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
