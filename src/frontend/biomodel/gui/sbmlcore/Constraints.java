/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package frontend.biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.xml.XMLNode;

import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.biomodel.gui.schematic.Utils;
import frontend.main.Gui;
import frontend.main.util.SpringUtilities;
import frontend.main.util.Utility;


/**
 * This is a class for creating SBML constraints
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Constraints extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addConstraint, removeConstraint, editConstraint;

	private JList constraints; // JList of initial assignments

	private BioModel bioModel;

	private ModelEditor modelEditor;
	
	private JComboBox SBOTerms;

	/* Create initial assignment panel */
	public Constraints(BioModel bioModel, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.modelEditor = modelEditor;
		Model model = bioModel.getSBMLDocument().getModel();
		addConstraint = new JButton("Add Constraint");
		removeConstraint = new JButton("Remove Constraint");
		editConstraint = new JButton("Edit Constraint");
		constraints = new JList();
		ListOf<Constraint> listOfConstraints = model.getListOfConstraints();
		String[] cons = new String[model.getConstraintCount()];
		for (int i = 0; i < model.getConstraintCount(); i++) {
			Constraint constraint = listOfConstraints.get(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "c0";
				int cn = 0;
				while (bioModel.isSIdInUse(constraintId)) {
					cn++;
					constraintId = "c" + cn;
				}
				SBMLutilities.setMetaId(constraint, constraintId);
			}
			cons[i] = constraint.getMetaId();
			cons[i] += SBMLutilities.getDimensionString(constraint);
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
		dataModels.biomodel.util.Utility.sort(cons);
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
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel IDPanel = new JPanel();
		JLabel IDLabel = new JLabel("ID:");
		JLabel mathLabel = new JLabel("Constraint:");
		JLabel messageLabel = new JLabel("Messsage:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JCheckBox onPort = new JCheckBox();
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_MATHEMATICAL_EXPRESSION));
		JTextField consID = new JTextField(12);		
		JTextField consMath = new JTextField(85);
		JTextField consMessage = new JTextField(85);

		String selectedID = "";
		int Cindex = -1;
		if (option.equals("OK")) {
			ListOf<Constraint> c = bioModel.getSBMLDocument().getModel().getListOfConstraints();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getConstraintCount(); i++) {
				if ((c.get(i).getMetaId()).equals(selected)) {
					Cindex = i;
					consMath.setText(bioModel.removeBooleans(c.get(i).getMath()));
					if (c.get(i).isSetMetaId()) {
						selectedID = c.get(i).getMetaId();
						String dimInID = SBMLutilities.getDimensionString(c.get(Cindex));
						consID.setText(selectedID+dimInID);
					}
					if (c.get(i).isSetSBOTerm()) {
						SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(c.get(i).getSBOTermID()));
					}
					if (c.get(i).isSetMessage()) {
						String message;
						try {
							message = c.get(i).getMessageString();
							// XMLNode.convertXMLNodeToString(((Constraint)
							// c.get(i)).getMessage());
							message = message.substring(message.indexOf("xhtml\">") + 7, message.indexOf("</p>"));
							consMessage.setText(message);
						} catch (XMLStreamException e) {
							e.printStackTrace();
						}
						
					}
					if (bioModel.getPortByMetaIdRef(c.get(i).getMetaId())!=null) {
						onPort.setSelected(true);
					} else {
						onPort.setSelected(false);
					}
					break;
				}
			}
		}
		else {
			String constraintId = "c0";
			int cn = 0;
			while (SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), constraintId)!=null) {
				cn++;
				constraintId = "c" + cn;
			}
			consID.setText(constraintId);
		}
		IDPanel.add(IDLabel);
		IDPanel.add(consID);
		IDPanel.add(onPortLabel);
		IDPanel.add(onPort);
		JPanel sboPanel = new JPanel(new SpringLayout());
		sboPanel.add(sboTermLabel);
		sboPanel.add(SBOTerms);
		SpringUtilities.makeCompactGrid(sboPanel, 1, 2, 6, 6, 6, 6);
		JPanel mathPanel = new JPanel(new SpringLayout());
		mathPanel.add(mathLabel);
		mathPanel.add(consMath);
		SpringUtilities.makeCompactGrid(mathPanel, 1, 2, 6, 6, 6, 6);
		JPanel messagePanel = new JPanel(new SpringLayout());
		messagePanel.add(messageLabel);
		messagePanel.add(consMessage);
		SpringUtilities.makeCompactGrid(messagePanel, 1, 2, 6, 6, 6, 6);
		consPanel.add(IDPanel,"North");
		consPanel.add(sboPanel,"South");
		southPanel.add(consPanel,"North");
		southPanel.add(mathPanel,"Center");
		southPanel.add(messagePanel,"South");
		constraintPanel.add(southPanel);
		
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, constraintPanel, "Constraint Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		String[] dimID = new String[]{""};
		String[] dimensionIds = new String[]{""};
		String constraintId = "";
		while (error && value == JOptionPane.YES_OPTION) {
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), consID.getText(), false);
			if(dimID!=null){
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = Utils.checkID(bioModel.getSBMLDocument(), dimID[0].trim(), selectedID, false);
				constraintId = dimID[0].trim();
			} else {
				error = true;
			}
			if (!error) {
				if (consMath.getText().trim().equals("") || SBMLutilities.myParseFormula(consMath.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!SBMLutilities.returnsBoolean(bioModel.addBooleans(consMath.getText().trim()),bioModel.getSBMLDocument().getModel())) {
					JOptionPane.showMessageDialog(Gui.frame, "Constraint formula must be of type Boolean.", "Enter Valid Formula",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(consMath.getText().trim()))) {
					error = true;
				}
				else if (Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), bioModel.addBooleans(consMath.getText().trim()))) {
					error = true;
				}
				else {
					error = Utils.displayinvalidVariables("Constraint", bioModel.getSBMLDocument(), dimensionIds, consMath.getText().trim(), "", false);
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
						Constraint c = (bioModel.getSBMLDocument().getModel().getListOfConstraints()).get(Cindex);
						c.setMath(bioModel.addBooleans(consMath.getText().trim()));
						SBMLutilities.setMetaId(c, constraintId);
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							c.unsetSBOTerm();
						} else {
							c.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode;
							try {
								xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
										+ consMessage.getText().trim() + "</p></message>");
								c.setMessage(xmlNode);
							} catch (XMLStreamException e) {
								e.printStackTrace();
							}
							
						}
						else if (c.isSetMessage()){
							c.unsetMessage();
						}
						SBMLutilities.createDimensions(c, dimensionIds, dimID);
						Port port = bioModel.getPortByMetaIdRef(selectedID);
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
								port.setMetaIdRef(c.getMetaId());
								SBMLutilities.cloneDimensionAddIndex(c,port,"comp:metaIdRef");
							} else {
								bioModel.getSBMLCompModel().removePort(port);
							}
						} else {
							if (onPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
								port.setMetaIdRef(c.getMetaId());
								SBMLutilities.cloneDimensionAddIndex(c,port,"comp:metaIdRef");
							}
						}
						cons[index] = c.getMetaId();
						if (dimID!=null) {
							for (int i = 1; i < dimID.length; i++) {
								cons[index] += "[" + dimID[i] + "]";
							}
						}
						dataModels.biomodel.util.Utility.sort(cons);
						constraints.setListData(cons);
						constraints.setSelectedIndex(index);
						modelEditor.makeUndoPoint();
					}
					else {
						String[] cons = new String[constraints.getModel().getSize()];
						for (int i = 0; i < constraints.getModel().getSize(); i++) {
							cons[i] = constraints.getModel().getElementAt(i).toString();
						}
						JList add = new JList();
						int index = constraints.getSelectedIndex();
						Constraint c = bioModel.getSBMLDocument().getModel().createConstraint();
						c.setMath(bioModel.addBooleans(consMath.getText().trim()));
						SBMLutilities.setMetaId(c, constraintId);
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							c.unsetSBOTerm();
						} else {
							c.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode;
							try {
								xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
										+ consMessage.getText().trim() + "</p></message>");

								c.setMessage(xmlNode);
							} catch (XMLStreamException e) {
								e.printStackTrace();
							}
						}
						SBMLutilities.createDimensions(c, dimensionIds, dimID);
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.CONSTRAINT+"__"+c.getMetaId());
							port.setMetaIdRef(c.getMetaId());
							SBMLutilities.cloneDimensionAddIndex(c,port,"comp:metaIdRef");
						}
						String constraintEntry = c.getMetaId();
						if (dimID!=null) {
							for (int i = 1; i < dimID.length; i++) {
								constraintEntry += "[" + dimID[i] + "]";
							}
						}
						Object[] adding = { constraintEntry };
						add.setListData(adding);
						add.setSelectedIndex(0);
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(cons, constraints, add);
						cons = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							cons[i] = (String) adding[i];
						}
						dataModels.biomodel.util.Utility.sort(cons);
						constraints.setListData(cons);
						constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (bioModel.getSBMLDocument().getModel().getConstraintCount() == 1) {
							constraints.setSelectedIndex(0);
						}
						else {
							constraints.setSelectedIndex(index);
						}
					}
					modelEditor.setDirty(true);
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
		return constraintId;
	}
	
	/**
	 * Refresh constraints panel
	 */
	public void refreshConstraintsPanel() {
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<Constraint> listOfConstraints = model.getListOfConstraints();
		String[] cons = new String[model.getConstraintCount()];
		for (int i = 0; i < model.getConstraintCount(); i++) {
			Constraint constraint = listOfConstraints.get(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "c0";
				int cn = 0;
				while (bioModel.isSIdInUse(constraintId)) {
					cn++;
					constraintId = "c" + cn;
				}
				SBMLutilities.setMetaId(constraint, constraintId);
			}
			cons[i] = constraint.getMetaId() + SBMLutilities.getDimensionString(constraint);
		}
		dataModels.biomodel.util.Utility.sort(cons);
		constraints.setListData(cons);
		constraints.setSelectedIndex(0);
	}

	/**
	 * Remove a constraint
	 */
	public void removeConstraint(String selected) {
		ListOf<Constraint> c = bioModel.getSBMLDocument().getModel().getListOfConstraints();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getConstraintCount(); i++) {
			if ((c.get(i).getMetaId()).equals(selected)) {
				c.remove(i);
				break;
			}
		}
		for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
			Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
			if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(selected)) {
				bioModel.getSBMLCompModel().getListOfPorts().remove(i);
				break;
			}
		}
		Layout layout = bioModel.getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
			layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
		}
		modelEditor.setDirty(true);
		modelEditor.makeUndoPoint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add constraint button is clicked
		if (e.getSource() == addConstraint) {
			constraintEditor("Add","");
			modelEditor.makeUndoPoint();
		}
		// if the edit constraint button is clicked
		else if (e.getSource() == editConstraint) {
			if (constraints.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No constraint selected.", "Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) constraints.getSelectedValue()).split("\\[")[0];
			constraintEditor("OK",selected);
		}
		// if the remove constraint button is clicked
		else if (e.getSource() == removeConstraint) {
			int index = constraints.getSelectedIndex();
			if (index != -1) {
				String selected = ((String) constraints.getSelectedValue()).split("\\[")[0];
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == constraints) {
				if (constraints.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No constraint selected.", "Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) constraints.getSelectedValue()).split("\\[")[0];
				constraintEditor("OK",selected);
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
