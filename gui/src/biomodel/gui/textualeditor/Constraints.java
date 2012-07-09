package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.XMLNode;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


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

	private BioModel gcm;

	private MutableBoolean dirty;

	/* Create initial assignment panel */
	public Constraints(BioModel gcm, MutableBoolean dirty) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.dirty = dirty;
		Model model = gcm.getSBMLDocument().getModel();
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
				while (gcm.isSIdInUse(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
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
	public String constraintEditor(String option,String selected) {
		JPanel constraintPanel = new JPanel();
		JPanel consPanel = new JPanel(new BorderLayout());
		JPanel IDPanel = new JPanel();
		JPanel mathPanel = new JPanel(new BorderLayout());
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel IDLabel = new JLabel("ID:");
		JLabel mathLabel = new JLabel("Constraint:");
		JLabel messageLabel = new JLabel("Messsage:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
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
		
		JCheckBox onPort = new JCheckBox();

		String selectedID = "";
		int Cindex = -1;
		if (option.equals("OK")) {
			ListOf c = gcm.getSBMLDocument().getModel().getListOfConstraints();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumConstraints(); i++) {
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
					if (gcm.getPortByMetaIdRef(c.get(i).getMetaId())!=null) {
						onPort.setSelected(true);
					} else {
						onPort.setSelected(false);
					}
				}
			}
		}
		else {
			String constraintId = "constraint0";
			int cn = 0;
			while (gcm.getSBMLDocument().getElementByMetaId(constraintId)!=null) {
				cn++;
				constraintId = "constraint" + cn;
			}
			consID.setText(constraintId);
		}
		IDPanel.add(IDLabel);
		IDPanel.add(consID);
		IDPanel.add(onPortLabel);
		IDPanel.add(onPort);
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
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), consID.getText().trim(), selectedID, false, true);
			if (!error) {
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(gcm.getSBMLDocument(), consMath.getText().trim(), "", false);
				if (consMath.getText().trim().equals("") || SBMLutilities.myParseFormula(consMath.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!SBMLutilities.isBoolean(gcm.getSBMLDocument(), SBMLutilities.myParseFormula(consMath.getText().trim()))) {
					JOptionPane.showMessageDialog(Gui.frame, "Constraint formula must be of type Boolean.", "Enter Valid Formula",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (SBMLutilities.checkNumFunctionArguments(gcm.getSBMLDocument(), SBMLutilities.myParseFormula(consMath.getText().trim()))) {
					error = true;
				}
				else {
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
						Constraint c = (Constraint) (gcm.getSBMLDocument().getModel().getListOfConstraints()).get(Cindex);
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
						Port port = gcm.getPortByMetaIdRef(selectedID);
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
								port.setMetaIdRef(c.getMetaId());
							} else {
								port.removeFromParentAndDelete();
							}
						} else {
							if (onPort.isSelected()) {
								port = gcm.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
								port.setMetaIdRef(c.getMetaId());
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
						Constraint c = gcm.getSBMLDocument().getModel().createConstraint();
						c.setMath(SBMLutilities.myParseFormula(consMath.getText().trim()));
						c.setMetaId(consID.getText().trim());
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
									+ consMessage.getText().trim() + "</p></message>");
							c.setMessage(xmlNode);
						}
						if (onPort.isSelected()) {
							Port port = gcm.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
							port.setMetaIdRef(c.getMetaId());
						}
						Object[] adding = { c.getMetaId() };
						add.setListData(adding);
						add.setSelectedIndex(0);
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(cons, constraints, add, null, null, null, null, null, Gui.frame);
						cons = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							cons[i] = (String) adding[i];
						}
						Utility.sort(cons);
						constraints.setListData(cons);
						constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (gcm.getSBMLDocument().getModel().getNumConstraints() == 1) {
							constraints.setSelectedIndex(0);
						}
						else {
							constraints.setSelectedIndex(index);
						}
					}
					dirty.setValue(true);
					gcm.makeUndoPoint();
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, constraintPanel, "Constraint Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return selected;
		}
		return consID.getText().trim();
	}
	
	/**
	 * Refresh constraints panel
	 */
	public void refreshConstraintsPanel() {
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfConstraints = model.getListOfConstraints();
		String[] cons = new String[(int) model.getNumConstraints()];
		for (int i = 0; i < model.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) listOfConstraints.get(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "constraint0";
				int cn = 0;
				while (gcm.isSIdInUse(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
				constraint.setMetaId(constraintId);
			}
			cons[i] = constraint.getMetaId();
		}
		Utility.sort(cons);
		constraints.setListData(cons);
		constraints.setSelectedIndex(0);
	}

	/**
	 * Remove a constraint
	 */
	public void removeConstraint(String selected) {
		ListOf c = gcm.getSBMLDocument().getModel().getListOfConstraints();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumConstraints(); i++) {
			if ((((Constraint) c.get(i)).getMetaId()).equals(selected)) {
				c.remove(i);
				break;
			}
		}
		for (long i = 0; i < gcm.getSBMLCompModel().getNumPorts(); i++) {
			Port port = gcm.getSBMLCompModel().getPort(i);
			if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(selected)) {
				gcm.getSBMLCompModel().removePort(i);
				break;
			}
		}
		if (gcm.getSBMLLayout().getLayout("iBioSim") != null) {
			Layout layout = gcm.getSBMLLayout().getLayout("iBioSim"); 
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+selected)!=null) {
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+selected);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected);
			}
		}
		dirty.setValue(true);
		gcm.makeUndoPoint();
	}

	public void actionPerformed(ActionEvent e) {
		// if the add constraint button is clicked
		if (e.getSource() == addConstraint) {
			constraintEditor("Add","");
		}
		// if the edit constraint button is clicked
		else if (e.getSource() == editConstraint) {
			if (constraints.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No constraint selected.", "Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) constraints.getSelectedValue());
			constraintEditor("OK",selected);
		}
		// if the remove constraint button is clicked
		else if (e.getSource() == removeConstraint) {
			int index = constraints.getSelectedIndex();
			if (index != -1) {
				String selected = ((String) constraints.getSelectedValue());
				removeConstraint(selected);
				constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(constraints);
				constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < constraints.getModel().getSize()) {
					constraints.setSelectedIndex(index);
				}
				else {
					constraints.setSelectedIndex(index - 1);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == constraints) {
				if (constraints.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No constraint selected.", "Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) constraints.getSelectedValue());
				constraintEditor("OK",selected);
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
