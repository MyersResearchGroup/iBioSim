package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class PropertyPanel extends JPanel implements ActionListener {

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };

	private LhpnFile lhpn;

	private PropertyField field;

	private PropertyList propertyList;

	public PropertyPanel(String selected, PropertyList propertyList, LhpnFile lhpn) {
		super(new GridLayout(1, 1));
		this.selected = selected;
		this.propertyList = propertyList;
		this.lhpn = lhpn;

		// Property field
		field = new PropertyField("Property", "", null, null,
				Utility.NAMEstring);
		add(field);	
		

		String oldProperty = null;
		if (selected != null) {
			oldProperty = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			field.setValue(selected);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldProperty);
		}
	}
	public boolean parseProperty() {
		boolean goodProperty = false;
		String propertyTemp = field.getValue();
		if(!propertyTemp.equals("") && propertyTemp!=null){
			// check the balance of parentheses and square brackets
			Parser p = new Parser(propertyTemp);
			goodProperty = p.parseProperty();
			return goodProperty;
		}
		else
		{
			goodProperty = true;
			return goodProperty;
		}
		
	}

	private boolean openGui(String oldProperty) {
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!parseProperty()) {
				return false;
			}
			String property = field.getValue();

			if (selected != null) {
				if (!oldProperty.equals(property)) {
					lhpn.removeProperty(oldProperty);
					lhpn.addProperty(property);
				}
			} else {
				lhpn.addProperty(property);
			}
			
			for (String s : propertyList.getItems()) {
				if (oldProperty != null) {
					if (s.equals(oldProperty)) {
						propertyList.removeItem(s);
					}
				}
			}
			propertyList.addItem(property);
			propertyList.setSelectedValue(property, true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(initBox.getSelectedItem().toString());
		}
	}

}
