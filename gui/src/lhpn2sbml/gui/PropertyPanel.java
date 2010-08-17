package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.Constraint;

import com.sun.org.apache.bcel.internal.generic.ISUB;

import sbmleditor.SBML_Editor;

import biomodelsim.BioSim;

public class PropertyPanel extends JPanel implements ActionListener {

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };

	private LhpnFile lhpn;

	private PropertyField field;
	
	private BioSim biosim;

	private PropertyList propertyList;

	public PropertyPanel(String selected, PropertyList propertyList, LhpnFile lhpn, BioSim biosim) {
		super(new GridLayout(1, 1));
		this.selected = selected;
		this.propertyList = propertyList;
		this.lhpn = lhpn;
		this.biosim = biosim;

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

	private boolean checkValues() {
		String prop = field.getValue();
		boolean goodProperty = false;
		// Parse the string property and check to see if it is valid and set
		// goodProperty flag
		String symbol = "@";
		if (prop!=null && !prop.equals("") && !prop.contains(" ")){
//			System.out.println("property:" + prop);
			// detect temporal operators: AU EU EG EF AG AF PG PF PU
			boolean temporalOpsFlag = prop.contains("AU") |
								   prop.contains("AG") |
								   prop.contains("AF") |
								   prop.contains("EU") |
								   prop.contains("EG") |
								   prop.contains("EF") |
								   prop.contains("PU") |
								   prop.contains("PG") |
								   prop.contains("PF");
			boolean PUflag = prop.contains("PU");
			boolean PFFlag = prop.contains("PF");
			boolean PGFlag = prop.contains("PG");
			boolean MultiTempFlag= false;  // flag for multiple temporal operators 
			String propTemp = null;
			propTemp = prop;
			// test if the property specification contains temporal logic
			if (temporalOpsFlag){
//				 Constraint constraintFail = m.createConstraint();	
//				 Constraint constraintSucc = m.createConstraint();
				 String[] allTemporalOps={"AU", "AG", "AF","EU", "EG", "EF", "PU","PG", "PF",};
				 String leftPropTemp = null;
				 String rightPropTemp = null;
				 String BoundPropTemp = null;
				 String lowerBoundPropTemp = null;
				 String upperBoundPropTemp = null;
				 for (int i=0; i < allTemporalOps.length; i++){
					 if (prop.contains(allTemporalOps[i])){   
						 // if property contains any temporal operators
						 propTemp = propTemp.replace(allTemporalOps[i], symbol);
					 }
				 }
				 // check multiple temporal logic operators
				 if(propTemp.indexOf(symbol)!= propTemp.lastIndexOf(symbol)){ 
					 MultiTempFlag = true;
				 }
				 if (!MultiTempFlag){
					// Strip off the temporal operator and its time bound
					 if (PUflag){   // if it is PU.
						 // Current version only deals with PU. 
						 // The atacs parser requires PU to have a pair of outermost brackets 
						 // Example: ((q_max=q_max)PU[2,3](q=q_max))
						 if (propTemp.startsWith("(") && propTemp.endsWith(")")){
							// remove the outermost brackets
							 propTemp = propTemp.substring(1, propTemp.lastIndexOf(")"));
							 // obtain the logic BEFORE the temporal operator
							 leftPropTemp= propTemp.substring(0, propTemp.indexOf(symbol));
							 boolean isLeftValid = isValidExpr(lhpn, leftPropTemp);
							 if (isLeftValid){
								// obtain the logic AFTER the temporal operator
								 rightPropTemp= propTemp.substring(propTemp.indexOf("]")+1, propTemp.length());
								 boolean isRightValid = isValidExpr(lhpn, rightPropTemp);
								 if(isRightValid){
									// obtain the time bound
									 BoundPropTemp= propTemp.substring(propTemp.indexOf("["), propTemp.indexOf("]")+1);
									 // bound: [<= upper]
									 if(BoundPropTemp.contains("<=")){
										// upper bound
										 upperBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf("<")+2, BoundPropTemp.indexOf("]"));
										 boolean isUpperValid = isValidExpr(lhpn, upperBoundPropTemp);
										 if(isUpperValid) goodProperty = true;
										 else{
											 JOptionPane.showMessageDialog(null, "Invalid expression in upper time bound.",
														"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
										 }
									 }
									 // bound: [lower, upper]
									 else if (BoundPropTemp.contains(",")){ 
										 lowerBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf("[")+1, BoundPropTemp.indexOf(","));
										 boolean isLowerValid = isValidExpr(lhpn, lowerBoundPropTemp);
										 upperBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf(",")+1, BoundPropTemp.indexOf("]"));
										 boolean isUpperValid = isValidExpr(lhpn, upperBoundPropTemp);	 
										 if(isLowerValid && isUpperValid) goodProperty = true;
										 else 
											 JOptionPane.showMessageDialog(null, "Invalid expression in lower/upper bound.",
														"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
									 }
									 // invalid expression for the time bound
									 else {
										 JOptionPane.showMessageDialog(null, "Invalid format in time bound. It should be either [<= upper] or [lower, upper].",
													"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
									 } 
								 }
								 else{
									 JOptionPane.showMessageDialog(null, "Invalid logical expression after the until operator",
												"Error in Property", JOptionPane.ERROR_MESSAGE);
								 }
							 }
							 else {
								 JOptionPane.showMessageDialog(null, "Invalid logical expression before the until operator",
											"Error in Property", JOptionPane.ERROR_MESSAGE);
							 }
						 }
						 else {
							 JOptionPane.showMessageDialog(null, "Please add parenthese around the property specification.",
										"Error in Property", JOptionPane.ERROR_MESSAGE);
						 }
					 }
					 else if(PFFlag | PGFlag){ 
						 // if the temporal operator is one of these : PF and PG
						// Current version only supports PF and PG
//						// Examples: PF([4,5]q=q_max), PG([<=7]q=q_max)
						 
						// obtain the logic AFTER the temporal operator
						 rightPropTemp= propTemp.substring(propTemp.indexOf("]")+1, propTemp.length());
						 boolean isRightValid = isValidExpr(lhpn, rightPropTemp);
						 if(isRightValid){
							// obtain the time bound
							 BoundPropTemp= propTemp.substring(propTemp.indexOf("["), propTemp.indexOf("]")+1);
							 // bound: [<= upper]
							 if (BoundPropTemp.contains("<=")){
								 upperBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf("<")+2, BoundPropTemp.indexOf("]"));
								 boolean isUpperValid = isValidExpr(lhpn, upperBoundPropTemp);
								 if(isUpperValid) goodProperty = true;
								 else{
									 JOptionPane.showMessageDialog(null, "Invalid expression in upper time bound.",
												"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
								 }
									 
							 }
							 else if (BoundPropTemp.contains(",")){
								 lowerBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf("[")+1, BoundPropTemp.indexOf(","));
								 upperBoundPropTemp = BoundPropTemp.substring(BoundPropTemp.indexOf(",")+1, BoundPropTemp.indexOf("]"));
								 boolean isLowerValid = isValidExpr(lhpn, lowerBoundPropTemp);
								 boolean isUpperValid = isValidExpr(lhpn, upperBoundPropTemp);
								 if(isLowerValid && isUpperValid) goodProperty = true;
								 else {
									 JOptionPane.showMessageDialog(null, "Invalid format in time bound. It should be either [<= upper] or [lower, upper].",
												"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
								 }	 
							 }
							 else {
								 JOptionPane.showMessageDialog(null, "Invalid format in time bound. It should be either [<= upper] or [lower, upper].",
											"Error in Time Bound", JOptionPane.ERROR_MESSAGE);
							 }
							 
						 }
						 else {
							 JOptionPane.showMessageDialog(null, "Invalid logical expression after the until operator",
										"Error in Property", JOptionPane.ERROR_MESSAGE);
						 }
						
					 }
					 else {		// AF,AG,AU,EF,EG,EU
						 JOptionPane.showMessageDialog(null, "Currently the editor does not support one of the following temporal operators: AU,AF,AG,EU,EF,EG.",
									"Error in Property", JOptionPane.ERROR_MESSAGE);
					 } 
				}
				 else {    // MultiTempFlag = true
					 	JOptionPane.showMessageDialog(null, "Property does not allow nested temporal operators.",
								"Error in Property", JOptionPane.ERROR_MESSAGE);
				 }
			}
			else {    // property does not include temporal operators
				     JOptionPane.showMessageDialog(null, "Property without temporal logic is can only be checked in their intial states. Please include a temporal logic operator.",
						"Error in Property", JOptionPane.ERROR_MESSAGE);
					 ExprTree propTempTree = String2ExprTree(lhpn, propTemp);
					 String propTempSBML = propTempTree.toString("SBML");
				}
		}		
		return goodProperty;
	}

	private boolean openGui(String oldProperty) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				//Utility.createErrorMessage("Error", "Invalid property entered.");
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
	public ExprTree String2ExprTree(LhpnFile lhpn, String str) {
		boolean retVal;
		ExprTree result = new ExprTree(lhpn);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(str);
		retVal = expr.intexpr_L(str);
		if (retVal) {
			result = expr;
		}
		return result;
	}
	public boolean isValidExpr(LhpnFile lhpn, String str) {
		boolean retVal;
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(str);
		retVal = expr.intexpr_L(str);
		return retVal;
	}

}
