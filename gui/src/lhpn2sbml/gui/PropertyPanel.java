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
		boolean goodProperty = false;
		String propertyTemp = field.getValue();
		if(!propertyTemp.equals("") && propertyTemp!=null){
			// check the balance of parentheses and square brackets
			String propertyTempTest=field.getValue();
			short numOPAR = 0;
			short numCPAR = 0;
			short numOSQUARE=0;
			short numCSQUARE=0;
//			propertyTempTest = propertyTempTest.replaceFirst("\\(", "");
//			if (propertyTempTest.contains(")")){
//				System.out.println("propertyTempTest = " + propertyTempTest);
//			}
			for (numOPAR = 0; propertyTempTest.contains("("); numOPAR++){
				propertyTempTest = propertyTempTest.replaceFirst("\\(", "");
			}
			for (numCPAR = 0; propertyTempTest.contains(")"); numCPAR++){
				propertyTempTest = propertyTempTest.replaceFirst("\\)", "");
			}
			for (numOSQUARE = 0; propertyTempTest.contains("["); numOSQUARE++){
				propertyTempTest = propertyTempTest.replaceFirst("\\[", "");
			}
			for (numCSQUARE = 0; propertyTempTest.contains("]"); numCSQUARE++){
				propertyTempTest = propertyTempTest.replaceFirst("\\]", "");
			}
			if((numOPAR==numCPAR) && (numOSQUARE==numCSQUARE)){
				boolean propsFlag = propertyTemp.contains("AU") |
				   propertyTemp.contains("AG") |
				   propertyTemp.contains("AF") |
				   propertyTemp.contains("EU") |
				   propertyTemp.contains("EG") |
				   propertyTemp.contains("EF") ;
				if (propertyTemp.contains("Pr")){
					propertyTemp = propertyTemp.replace("Pr", "%");
				}
				else if(propertyTemp.contains("St")){
					propertyTemp = propertyTemp.replace("St", "^");
				}
				String probpropertyTemp = propertyTemp;
				if (!propsFlag){
					// probproperty 
					// currently only allow ONE Pr operator in one property spec
					boolean MultiFlag= (propertyTemp.indexOf("%")!=propertyTemp.lastIndexOf("%"))
								|(propertyTemp.indexOf("^")!=propertyTemp.lastIndexOf("^")
								|(propertyTemp.contains("%") && propertyTemp.contains("^")));
		//			System.out.println("propertyTemp = " + propertyTemp);	
					if (!MultiFlag){
						if (propertyTemp.startsWith("%")){
							probpropertyTemp=probpropertyTemp.substring(1);
							boolean relopFlag = probpropertyTemp.startsWith(">")
												| probpropertyTemp.startsWith(">=")
												| probpropertyTemp.startsWith("<")
												| probpropertyTemp.startsWith("<=")
											    | (probpropertyTemp.startsWith("=") && !probpropertyTemp.contains("?"));
							if (relopFlag){
								// remove the relop
								if(probpropertyTemp.startsWith(">=") | probpropertyTemp.startsWith("<=")){
									probpropertyTemp=probpropertyTemp.substring(2);
								}
								else{
									probpropertyTemp=probpropertyTemp.substring(1);
								}
		//						// check the probability value after relop
								String probabilityValue = probpropertyTemp.substring(0,probpropertyTemp.indexOf("["));
								Pattern ProbabilityValuePattern = Pattern.compile(probValue);
								Matcher ProbabilityValueMatcher = ProbabilityValuePattern.matcher(probabilityValue);
								boolean correctProbabilityValue = ProbabilityValueMatcher.matches();
								if(correctProbabilityValue) {
									probpropertyTemp=probpropertyTemp.replaceFirst(probabilityValue, "");
									// propertyTemp should be in this format at this stage: '[' probprop ']'
									if (probpropertyTemp.startsWith("[")){
										String probprop = (String) probpropertyTemp.subSequence(1, probpropertyTemp.lastIndexOf("]"));
										goodProperty = Parseprobprop(probprop, goodProperty);
									}
									else{
										JOptionPane.showMessageDialog(null, "invalid format after the first probability value ",
												"Error in Property", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									JOptionPane.showMessageDialog(null, "invalid format of the probability value ",
											"Error in Property", JOptionPane.ERROR_MESSAGE);
								}
							}
							else if (probpropertyTemp.startsWith("=?")){
								probpropertyTemp=probpropertyTemp.substring(2);
								// propertyTemp should be in this format at this stage: '[' probprop ']'
								if (probpropertyTemp.startsWith("[")){
									String probprop = (String) probpropertyTemp.subSequence(1, probpropertyTemp.lastIndexOf("]"));
									goodProperty = Parseprobprop(probprop, goodProperty);
								}
								else{
									JOptionPane.showMessageDialog(null, "invalid format after the question mark ",
											"Error in Property", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JOptionPane.showMessageDialog(null, "Missing relational operator after Pr",
										"Error in Property", JOptionPane.ERROR_MESSAGE);
							}
						}
						else if (propertyTemp.startsWith("^")){
							probpropertyTemp=probpropertyTemp.substring(1);
							boolean relopFlag = probpropertyTemp.startsWith(">")
							| probpropertyTemp.startsWith(">=")
							| probpropertyTemp.startsWith("<")
							| probpropertyTemp.startsWith("<=")
							|(probpropertyTemp.startsWith("=") && !probpropertyTemp.contains("?"));
							if (relopFlag){
								// remove the relop
								if(probpropertyTemp.startsWith(">=") | probpropertyTemp.startsWith("<=")){
									probpropertyTemp=probpropertyTemp.substring(2);
								}
								else{
									probpropertyTemp=probpropertyTemp.substring(1);
								}
		//						// check the probability value after relop
								String probabilityValue = probpropertyTemp.substring(0,probpropertyTemp.indexOf("["));
								Pattern ProbabilityValuePattern = Pattern.compile(probValue);
								Matcher ProbabilityValueMatcher = ProbabilityValuePattern.matcher(probabilityValue);
								boolean correctProbabilityValue = ProbabilityValueMatcher.matches();
								if(correctProbabilityValue) {
									probpropertyTemp=probpropertyTemp.replaceFirst(probabilityValue, "");
									// propertyTemp should be in this format at this stage: '[' hsf ']'
									if (probpropertyTemp.startsWith("[")){
										String hsf = (String) probpropertyTemp.subSequence(1, probpropertyTemp.lastIndexOf("]"));
										boolean isHsfValid = isValidExpr(lhpn, hsf);
										if(isHsfValid) goodProperty = true;
										else{
											 JOptionPane.showMessageDialog(null, "Invalid expression inside the square brackets.",
														"Error in Property", JOptionPane.ERROR_MESSAGE);
										 }
									}
									else{
										JOptionPane.showMessageDialog(null, "invalid format after the first probability value ",
												"Error in Property", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									JOptionPane.showMessageDialog(null, "invalid format of the probability value ",
											"Error in Property", JOptionPane.ERROR_MESSAGE);
								}
							}
							else if (probpropertyTemp.startsWith("=?")){
								probpropertyTemp=probpropertyTemp.substring(2);
								// propertyTemp should be in this format at this stage: '[' hsf ']'
								if (probpropertyTemp.startsWith("[")){
									String hsf = (String) probpropertyTemp.subSequence(1, probpropertyTemp.lastIndexOf("]"));
									boolean isHsfValid = isValidExpr(lhpn, hsf);
									if(isHsfValid) goodProperty = true;
									 else{
										 JOptionPane.showMessageDialog(null, "Invalid expression inside the square brackets.",
													"Error in Property", JOptionPane.ERROR_MESSAGE);
									 }
								}
								else{
									JOptionPane.showMessageDialog(null, "invalid format after the question mark ",
											"Error in Property", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JOptionPane.showMessageDialog(null, "Missing relational operator after St",
										"Error in Property", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							// hsf
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "Nested probabilistic property is not supported",
								"Error in Property", JOptionPane.ERROR_MESSAGE);				
					}
				}
				else if (propsFlag){
					// props
					goodProperty = true;
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Unbalanced parentheses or square brackets",
						"Error in Property", JOptionPane.ERROR_MESSAGE);
			}
			return goodProperty;
		}
		else
		{
			goodProperty = true;
			return goodProperty;
		}
		
	}

	private boolean openGui(String oldProperty) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
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
	public boolean Parseprobprop(String probprop, boolean goodProperty){
//		boolean MultiTempFlag= false;  // flag for multiple temporal operators 
		String symbol="@";
		String probpropTemp = probprop;
			 String hsfLeft = null;
			 String hsfRight = null;
			 String BoundPropTemp = null;
			 String lowerBoundPropTemp = null;
			 String upperBoundPropTemp = null;
			 
			 // Strip off the temporal operator and its time bound
		     if (probpropTemp.startsWith("(") && probpropTemp.contains("PU")){
					 // Current version only deals with PU. 
					 // The atacs parser requires PU to have a pair of outermost brackets 
					 // Example: ((q_max=q_max)PU[2,3]q=q_max)
					 if (probpropTemp.startsWith("(") && probpropTemp.endsWith(")")){
						// remove the outermost brackets
						 probpropTemp = probpropTemp.substring(1, probpropTemp.lastIndexOf(")"));
						 // obtain the logic BEFORE the temporal operator
						 probpropTemp = probpropTemp.replace("PU", symbol);
						 hsfLeft= probpropTemp.substring(0, probpropTemp.indexOf(symbol));
						 boolean isLeftValid = isValidExpr(lhpn, hsfLeft);
						 if (isLeftValid){
							// obtain the logic AFTER the temporal operator
							 hsfRight= probpropTemp.substring(probpropTemp.indexOf("]")+1, probpropTemp.length());
							 boolean isRightValid = isValidExpr(lhpn, hsfRight);
							 if(isRightValid){
								// obtain the time bound
								 BoundPropTemp= probpropTemp.substring(probpropTemp.indexOf("["), probpropTemp.indexOf("]")+1);
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
						 JOptionPane.showMessageDialog(null, "Please add parenthese around the PU specification.",
									"Error in Property", JOptionPane.ERROR_MESSAGE);
					 }
				 }
				 else if(probpropTemp.startsWith("PF") | probpropTemp.startsWith("PG")){ 
					 // if the temporal operator is either PF or PG
					 // Examples: PF([4,5]q=q_max), PG([<=7]q=q_max)
					// obtain the logic AFTER the temporal operator
					 hsfRight= probpropTemp.substring(probpropTemp.indexOf("]")+1, probpropTemp.length());
					 boolean isRightValid = isValidExpr(lhpn, hsfRight);
					 if(isRightValid){
						// obtain the time bound
						 BoundPropTemp= probpropTemp.substring(probpropTemp.indexOf("["), probpropTemp.indexOf("]")+1);
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
				 else {
					 JOptionPane.showMessageDialog(null, "Temporal logic can only be one of the following: PU, PF and PG.",
								"Error in Property", JOptionPane.ERROR_MESSAGE);
				 }
		     
		     return goodProperty;
		
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
	private static final String probValue = "(0\\.[0-9]+)";
}
