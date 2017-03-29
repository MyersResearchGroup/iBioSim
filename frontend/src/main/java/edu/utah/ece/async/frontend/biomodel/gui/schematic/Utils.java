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
package edu.utah.ece.async.frontend.biomodel.gui.schematic;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.util.ArraysMath;
import org.sbml.jsbml.ext.arrays.validator.ArraysValidator;
import org.sbml.jsbml.validator.SBMLValidator;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.libsbmlConstants;

import edu.utah.ece.async.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.frontend.main.Gui;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Utils {

	/**
	 * Sets up the button passed in.
	 */
	private static void setupButton(AbstractButton button, URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener){
		button.setActionCommand(actionCommand);
		
		
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		
		
		if(icon == null){
			// No icon, just set the text to the tooltip
			button.setText(tooltip);
		}else{
			Utils.setIcon(button, icon);
			// set a selected icon if it exists
			//String selectedPath = path.replaceAll(".png", "_selected.png");
			//if(new File(selectedPath).exists())
			if(selectedIcon!=null)
				button.setSelectedIcon(new ImageIcon(selectedIcon));
		}
	}
	
	/**
	 * Sets the button's icon and returns the path to it.
	 * @param button
	 * @param icon
	 * @return
	 */
	public static void setIcon(AbstractButton button, URL icon){

		// set the icon
		button.setIcon(new ImageIcon(icon));
	}
	
	public static JRadioButton makeRadioToolButton(URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener, final ButtonGroup buttonGroup){
		final JRadioButton button = new JRadioButton();
		buttonGroup.add(button);
		Utils.setupButton(button, icon, selectedIcon, actionCommand, tooltip, listener);
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setBorderPainted(true);
		
		return button;
	}
	
	
	public static JButton makeToolButton(URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener){
		JButton button = new JButton();
		
		Utils.setupButton(button, icon, selectedIcon, actionCommand, tooltip, listener);
		return button;
	}
	
	/**
   * Check that ID is valid and unique
   */
  public static boolean checkID(SBMLDocument document, String ID, String selectedID, boolean isReacParam)
  {
    Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");
    if (ID.equals("") && !isReacParam)
    {
      JOptionPane.showMessageDialog(Gui.frame, "An ID is required.", "Enter an ID", JOptionPane.ERROR_MESSAGE);
      return true;
    }
    if (!(IDpat.matcher(ID).matches()))
    {
      JOptionPane.showMessageDialog(Gui.frame, "An ID can only contain letters, numbers, and underscores.", "Invalid ID",
          JOptionPane.ERROR_MESSAGE);
      return true;
    }
    if (ID.equals("t") || ID.equals("time") || ID.equals("true") || ID.equals("false") || ID.equals("notanumber") || ID.equals("pi")
        || ID.equals("infinity") || ID.equals("exponentiale") || ID.equals("abs") || ID.equals("arccos") || ID.equals("arccosh")
        || ID.equals("arcsin") || ID.equals("arcsinh") || ID.equals("arctan") || ID.equals("arctanh") || ID.equals("arccot")
        || ID.equals("arccoth") || ID.equals("arccsc") || ID.equals("arccsch") || ID.equals("arcsec") || ID.equals("arcsech")
        || ID.equals("acos") || ID.equals("acosh") || ID.equals("asin") || ID.equals("asinh") || ID.equals("atan") || ID.equals("atanh")
        || ID.equals("acot") || ID.equals("acoth") || ID.equals("acsc") || ID.equals("acsch") || ID.equals("asec") || ID.equals("asech")
        || ID.equals("cos") || ID.equals("cosh") || ID.equals("cot") || ID.equals("coth") || ID.equals("csc") || ID.equals("csch")
        || ID.equals("ceil") || ID.equals("factorial") || ID.equals("exp") || ID.equals("floor") || ID.equals("ln") || ID.equals("log")
        || ID.equals("sqr") || ID.equals("log10") || ID.equals("pow") || ID.equals("sqrt") || ID.equals("root") || ID.equals("piecewise")
        || ID.equals("sec") || ID.equals("sech") || ID.equals("sin") || ID.equals("sinh") || ID.equals("tan") || ID.equals("tanh")
        || ID.equals("and") || ID.equals("or") || ID.equals("xor") || ID.equals("not") || ID.equals("eq") || ID.equals("geq")
        || ID.equals("leq") || ID.equals("gt") || ID.equals("neq") || ID.equals("lt") || ID.equals("delay")
        || ((document.getLevel() > 2) && (ID.equals("avogadro"))))
    {
      JOptionPane.showMessageDialog(Gui.frame, "ID cannot be a reserved word.", "Illegal ID", JOptionPane.ERROR_MESSAGE);
      return true;
    }
    if (!ID.equals(selectedID) && (document.getElementBySId(ID) != null || document.findSBase(ID) != null))
    {
      if (isReacParam)
      {
        JOptionPane.showMessageDialog(Gui.frame, "ID shadows a global ID.", "Not a Unique ID", JOptionPane.WARNING_MESSAGE);
      }
      else
      {
        JOptionPane.showMessageDialog(Gui.frame, "ID is not unique.", "Enter a Unique ID", JOptionPane.ERROR_MESSAGE);
        return true;
      }
    }
    return false;
  }
	
  /**
   * Checks the validity of parameters
   * 
   * @param document
   *            The document to get the list of parameters
   * @param entryText
   *            The parameters that are being tested
   * @return If the parameters are on the list, scalar, and constant.
   */
  public static String[] checkSizeParameters(SBMLDocument document, String entryText, boolean idNotRequired)
  {
    String id = "";
    String dims = "";
    if (entryText.contains("["))
    {
      id = entryText.substring(0, entryText.indexOf('['));
      dims = entryText.substring(entryText.indexOf('['));
    }
    else
    {
      id = entryText;
      dims = "";
    }
    // If this function is not called on reaction reactant, product, or
    // modifier
    if (!idNotRequired)
    {
      if (id.isEmpty())
      {
        JOptionPane.showMessageDialog(Gui.frame, "Need ID.", "Mismatching Brackets", JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }
    else
    {
      if (entryText.isEmpty())
      {
        return new String[] { "" };
      }
    }
    int pendingOpens = 0;
    int numDims = 0;
    char[] pieces = dims.toCharArray();
    String forRet = ";";
    String tester = "";
    for (int i = 0; i < pieces.length; i++)
    {
      if (pieces[i] == '[')
      {
        pendingOpens++;
      }
      else if (pieces[i] == ']')
      {
        pendingOpens--;
        if (pendingOpens < 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Too many closing brackets in the dimensions.", "Mismatching Brackets",
              JOptionPane.ERROR_MESSAGE);
          return null;
        }
      }
      else if (pendingOpens == 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "There is text outside brackets in the dimensions.", "Mismatching Brackets",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (pendingOpens != 0)
      {
        tester += pieces[i];
        continue;
      }
      tester = tester.substring(1);
      if (tester.isEmpty())
      {
        JOptionPane.showMessageDialog(Gui.frame, "A pair of brackets are blank in the dimensions.", "Invalid Size Parameter",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (document.getElementBySId(tester) == null)
      {
        JOptionPane.showMessageDialog(Gui.frame, tester + " is not a parameter.", "Invalid Size Parameter", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      Parameter p = (Parameter) document.getElementBySId(tester);
      ArraysSBasePlugin ABP = SBMLutilities.getArraysSBasePlugin(p);
      if (!p.isConstant())
      {
        JOptionPane.showMessageDialog(Gui.frame, p.getId() + " is not constant.", "Invalid Size Parameter", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (p.getValue() % 1 != 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, p.getId() + " does not have an integer value.", "Invalid Size Parameter",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (p.getValue() < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, p.getId() + " is negative.", "Invalid Size Parameter", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (ABP.getDimensionCount() != 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, p.getId() + " is not a scalar.", "Invalid Size Parameter", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      forRet += tester + ";";
      tester = "";
      numDims++;
    }
    if (pendingOpens > 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Too many open brackets in the dimensions.", "Mismatching Brackets", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    String[] retText = new String[numDims + 1];
    String[] extract = forRet.split(";");
    retText[0] = id;
    for (int i = 1; i < numDims + 1; i++)
    {
      retText[i] = extract[i];
    }
    String[] reverseRet = new String[retText.length];
    reverseRet[0] = retText[0];
    for (int i = 1; i < reverseRet.length; i++)
    {
      reverseRet[i] = retText[reverseRet.length - i];
    }
    return reverseRet;
  }
  
  /**
   * Checks the validity of a set of indices
   * 
   * @param index
   *            The indices that are being tested
   * @param variable
   *            The variable that dictates how many indices there should be
   *            based on its number of dimensions
   * @return If the number of indices matches the dimension count of the
   *         variable
   */
  public static String[] checkIndices(String index, SBase variable, SBMLDocument document, String[] dimensionIds, String attribute,
      String[] dimSizeIds, String[] topDimensionIds, String[] topDimSizeIds)
  {
    if (attribute.equals("conversionFactor"))
    {
      attribute = "conversion factor";
    }
    ArrayList<String> meshDimensionIds = new ArrayList<String>();
    if (dimensionIds != null)
    {
      meshDimensionIds.addAll(Arrays.asList(dimensionIds));
    }
    if (topDimensionIds != null)
    {
      meshDimensionIds.addAll(Arrays.asList(topDimensionIds));
    }
    ArrayList<String> meshDimSizeIds = new ArrayList<String>();
    if (dimSizeIds != null)
    {
      ArrayList<String> dimSizeIdsList = new ArrayList<String>(Arrays.asList(dimSizeIds));
      dimSizeIdsList.remove(0);
      meshDimSizeIds.addAll(dimSizeIdsList);
    }
    if (topDimSizeIds != null)
    {
      ArrayList<String> topDimSizeIdsList = new ArrayList<String>(Arrays.asList(topDimSizeIds));
      topDimSizeIdsList.remove(0);
      meshDimSizeIds.addAll(topDimSizeIdsList);
    }
    HashMap<String, String> dimNSize = new HashMap<String, String>();
    for (int i = 0; i < meshDimSizeIds.size(); i++)
    {
      dimNSize.put(meshDimensionIds.get(i), meshDimSizeIds.get(i));
    }
    ArraysSBasePlugin ABV = SBMLutilities.getArraysSBasePlugin(variable);
    int varDimCount = ABV.getDimensionCount();
    if (index.trim().equals(""))
    {
      if (varDimCount == 0)
      {
        return new String[] { "" };
      }
      JOptionPane.showMessageDialog(Gui.frame, "The " + attribute + " needs indices.", "Invalid Indices", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    if (varDimCount == 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The " + attribute + " does not need indices.", "Invalid Indices", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    if (!index.trim().endsWith("]"))
    {
      JOptionPane.showMessageDialog(Gui.frame, "Index must end with a closing bracket for the " + attribute + ".", "Mismatching Brackets",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    if (!index.trim().startsWith("["))
    {
      JOptionPane.showMessageDialog(Gui.frame, "Index must start with an opening bracket for the " + attribute + ".", "Mismatching Brackets",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    int pendingOpens = 0;
    int numIndices = 0;
    char[] pieces = index.toCharArray();
    String forRet = ";";
    String tester = "";
    for (int i = 0; i < pieces.length; i++)
    {
      if (pieces[i] == '[')
      {
        pendingOpens++;
      }
      else if (pieces[i] == ']')
      {
        pendingOpens--;
        if (pendingOpens < 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Too many closing brackets for the " + attribute + " index.", "Mismatching Brackets",
              JOptionPane.ERROR_MESSAGE);
          return null;
        }
      }
      else if (pendingOpens == 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "There is text outside brackets for the " + attribute + " index.", "Mismatching Brackets",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (pendingOpens != 0)
      {
        tester += pieces[i];
        continue;
      }
      tester = tester.substring(1);
      if (tester.isEmpty())
      {
        JOptionPane.showMessageDialog(Gui.frame, "A pair of brackets are blank for the " + attribute + " index.", "Invalid Indices",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      ASTNode math = SBMLutilities.myParseFormula(tester);
      if (math == null)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Invalid index math for the " + attribute + ".", "Invalid Indices",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (displayinvalidVariables("Indices", document, meshDimensionIds.toArray(new String[meshDimensionIds.size()]), tester, "", false))
      {
        return null;
      }
      if (ArraysMath.isStaticallyComputable(document.getModel(), math, meshDimensionIds.toArray(new String[meshDimensionIds.size()])) == false)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Index math must consist of constants and valid dimension size ids only.",
            "Invalid Indices", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      Map<String, Double> dimSize = SBMLutilities.getDimensionSize(document.getModel(), dimNSize);
      if (dimSize == null)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Dimension objects should have size of type SIdRef and point to a valid Parameter.",
            "Invalid Indices", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      if (ArraysMath.evaluateIndexBounds(document.getModel(), variable, (varDimCount - 1) - numIndices, math, dimSize) == false)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Index math must evaluate to values within possible bounds.", "Invalid Indices",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      forRet += tester + ";";
      tester = "";
      numIndices++;
    }
    if (pendingOpens > 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Too many open brackets for the " + attribute + " index.", "Mismatching Brackets",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    if (ABV.getDimensionCount() != numIndices)
    {
      if (ABV.getDimensionCount() > numIndices)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Too few indices for the " + attribute + ".", "Invalid Indices", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      JOptionPane.showMessageDialog(Gui.frame, "Too many indices for the " + attribute + ".", "Invalid Indices", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    String[] reverseRet = new String[forRet.split(";").length];
    for (int i = 1; i < reverseRet.length; i++)
    {
      reverseRet[i] = forRet.split(";")[reverseRet.length - i];
    }
    return reverseRet;
  }

  /**
   * Displays the invalid variables
   * 
   * @param object
   *            This is the prefix of the string if it is not a function
   */
  public static boolean displayinvalidVariables(String object, SBMLDocument document, String[] dimensionIds, String formula, String arguments,
      boolean isFunction)
  {
    if (!isFunction)
    {
      ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(document, dimensionIds, formula, arguments, isFunction);
      if (invalidVars.size() > 0)
      {
        String invalid = "";
        for (int i = 0; i < invalidVars.size(); i++)
        {
          if (i == invalidVars.size() - 1)
          {
            invalid += invalidVars.get(i);
          }
          else
          {
            invalid += invalidVars.get(i) + "\n";
          }
        }
        String message;
        message = object + " contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
        JTextArea messageArea = new JTextArea(message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        JScrollPane scrolls = new JScrollPane();
        scrolls.setMinimumSize(new java.awt.Dimension(300, 300));
        scrolls.setPreferredSize(new java.awt.Dimension(300, 300));
        scrolls.setViewportView(messageArea);
        JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
        return true;
      }
    }
    else
    {
      ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(document, dimensionIds, formula, arguments, isFunction);
      if (invalidVars.size() > 0)
      {
        String invalid = "";
        for (int i = 0; i < invalidVars.size(); i++)
        {
          if (i == invalidVars.size() - 1)
          {
            invalid += invalidVars.get(i);
          }
          else
          {
            invalid += invalidVars.get(i) + "\n";
          }
        }
        String message;
        message = "Function can only contain the arguments or other function calls.\n\n" + "Illegal variables:\n" + invalid;
        JTextArea messageArea = new JTextArea(message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        JScrollPane scrolls = new JScrollPane();
        scrolls.setMinimumSize(new java.awt.Dimension(300, 300));
        scrolls.setPreferredSize(new java.awt.Dimension(300, 300));
        scrolls.setViewportView(messageArea);
        JOptionPane.showMessageDialog(Gui.frame, scrolls, "Illegal Variables", JOptionPane.ERROR_MESSAGE);
        return true;
      }
    }
    return false;
  }

  /**
   * Variable that is updated by a rule or event cannot be constant
   */
  public static boolean checkConstant(SBMLDocument document, String varType, String val)
  {
  	for (int i = 0; i < document.getModel().getRuleCount(); i++)
  	{
  		Rule rule = document.getModel().getRule(i);
  		if (SBMLutilities.getVariable(rule) != null && SBMLutilities.getVariable(rule).equals(val))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, varType + " cannot be constant if updated by a rule.", varType + " Cannot Be Constant",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  	}
  	for (int i = 0; i < document.getModel().getEventCount(); i++)
  	{
  		org.sbml.jsbml.Event event = document.getModel().getListOfEvents().get(i);
  		for (int j = 0; j < event.getEventAssignmentCount(); j++)
  		{
  			EventAssignment ea = event.getListOfEventAssignments().get(j);
  			if (ea.getVariable().equals(val))
  			{
  				JOptionPane.showMessageDialog(Gui.frame, varType + " cannot be constant if updated by an event.",
  						varType + " Cannot Be Constant", JOptionPane.ERROR_MESSAGE);
  				return true;
  			}
  		}
  	}
  	return false;
  }

  /**
   * Checks consistency of the sbml file.
   */
  public static boolean checkUnits(SBMLDocument document)
  {
  	long numErrors = 0;
  	String message = "Change in unit definition causes unit errors in the following elements:\n";
  	for (int i = 0; i < document.getModel().getReactionCount(); i++)
  	{
  		Reaction reaction = document.getModel().getReaction(i);
  		if (!reaction.isSetKineticLaw())
  		{
  			continue;
  		}
  		KineticLaw law = reaction.getKineticLaw();
  		if (law != null)
  		{
  			if (SBMLutilities.checkUnitsInKineticLaw(document, law))
  			{
  				message += "Reaction: " + reaction.getId() + "\n";
  				numErrors++;
  			}
  		}
  	}
  	for (int i = 0; i < document.getModel().getInitialAssignmentCount(); i++)
  	{
  		InitialAssignment init = document.getModel().getInitialAssignment(i);
  		if (SBMLutilities.checkUnitsInInitialAssignment(document, init))
  		{
  			message += "Initial assignment on variable: " + init.getVariable() + "\n";
  			numErrors++;
  		}
  	}
  	for (int i = 0; i < document.getModel().getRuleCount(); i++)
  	{
  		Rule rule = document.getModel().getRule(i);
  		if (rule.isAssignment())
  		{
  			if (SBMLutilities.checkUnitsInAssignmentRule(document, rule))
  			{
  				message += "Assignment rule on variable: " + SBMLutilities.getVariable(rule) + "\n";
  				numErrors++;
  			}
  		}
  		else if (rule.isRate())
  		{
  			if (SBMLutilities.checkUnitsInRateRule(document, rule))
  			{
  				message += "Rate rule on variable: " + SBMLutilities.getVariable(rule) + "\n";
  				numErrors++;
  			}
  		}
  	}
  	for (int i = 0; i < document.getModel().getEventCount(); i++)
  	{
  		Event event = document.getModel().getEvent(i);
  		Delay delay = event.getDelay();
  		if (delay != null)
  		{
  			if (SBMLutilities.checkUnitsInEventDelay(delay))
  			{
  				message += "Delay on event: " + event.getId() + "\n";
  				numErrors++;
  			}
  		}
  		for (int j = 0; j < event.getEventAssignmentCount(); j++)
  		{
  			EventAssignment assign = event.getListOfEventAssignments().get(j);
  			if (SBMLutilities.checkUnitsInEventAssignment(document, assign))
  			{
  				message += "Event assignment for event " + event.getId() + " on variable: " + assign.getVariable() + "\n";
  				numErrors++;
  			}
  		}
  	}
  
  	/*
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY,
  	 * false);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY
  	 * , false);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY,
  	 * true);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY,
  	 * false);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY,
  	 * false);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE,
  	 * false);
  	 * document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL
  	 * , false); long numErrorsWarnings = document.checkConsistency(); for
  	 * (long i = 0; i < numErrorsWarnings; i++) { if
  	 * (!document.getError(i).isWarning()) { String error =
  	 * document.getError(i).getMessage(); message += i + ":" + error + "\n";
  	 * numErrors++; } }
  	 */
  	if (numErrors > 0)
  	{
  		JTextArea messageArea = new JTextArea(message);
  		messageArea.setLineWrap(true);
  		messageArea.setEditable(false);
  		JScrollPane scroll = new JScrollPane();
  		scroll.setMinimumSize(new java.awt.Dimension(600, 600));
  		scroll.setPreferredSize(new java.awt.Dimension(600, 600));
  		scroll.setViewportView(messageArea);
  		JOptionPane.showMessageDialog(Gui.frame, scroll, "Unit Errors in Model", JOptionPane.ERROR_MESSAGE);
  		return true;
  	}
  	return false;
  }

  /**
   * Checks consistency of the sbml file.
   */
  public static boolean check(String file, SBMLDocument doc, boolean overdeterminedOnly)
  {
  	String message = "";
  	long numErrors = 0;
  	Preferences biosimrc = Preferences.userRoot();
  	boolean warnings = biosimrc.get("biosim.general.warnings", "").equals("true");
  	if (biosimrc.get("biosim.general.validate", "").equals("libsbml") && Gui.isLibsbmlFound())
  	{
  		message += "Validation Problems Found by libsbml\n";
  		org.sbml.libsbml.SBMLDocument document = null;
  		// TODO: temporary hack because otherwise it hangs
  		if (doc == null)
  		{
  			document = new org.sbml.libsbml.SBMLReader().readSBML(file);
  		}
  		else
  		{
  			try
  			{
  				document = new org.sbml.libsbml.SBMLReader().readSBMLFromString(new SBMLWriter().writeSBMLToString(doc));
  			}
  			catch (SBMLException e)
  			{
  				JOptionPane.showMessageDialog(Gui.frame, "Invalid SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
  				return false;
  			}
  			catch (XMLStreamException e)
  			{
  				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
  				return false;
  			}
  		}
  		if (document == null)
  		{
  			return false;
  		}
  		if (overdeterminedOnly)
  		{
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_INTERNAL_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
  		}
  		else
  		{
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_INTERNAL_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
  		}
  		if (warnings && !overdeterminedOnly)
  		{
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MATHML_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_SBO_CONSISTENCY, true);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MODELING_PRACTICE, true);
  		}
  		else
  		{
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MATHML_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_SBO_CONSISTENCY, false);
  			document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MODELING_PRACTICE, false);
  		}
  
  		long numberOfErrors = document.checkConsistency();
  		for (int i = 0; i < numberOfErrors; i++)
  		{
  			String error = document.getError(i).getMessage();
  			if (error.startsWith("Due to the need to instantiate models"))
  			{
  				continue;
  			}
  			if (error.startsWith("The CompFlatteningConverter has encountered a required package"))
  			{
  				continue;
  			}
  			message += numErrors + ":" + error + "\n";
  			numErrors++;
  		}
  		if (!overdeterminedOnly)
  		{
  			List<SBMLError> arraysErrors = ArraysValidator.validate(doc);
  			for (int i = 0; i < arraysErrors.size(); i++)
  			{
  				String error = arraysErrors.get(i).getMessage();
  				message += numErrors + ":" + error + "\n";
  				numErrors++;
  			}
  		}
  	}
  	else
  	{
  		message += "Validation Problems Found by Webservice\n";
  		SBMLDocument document = doc;
  		if (document == null)
  		{
  			try {
          document = SBMLutilities.readSBML(file);
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
  		}
  		if (document == null)
  		{
  			return false;
  		}
  		if (overdeterminedOnly)
  		{
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, false);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, false);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
  		}
  		else
  		{
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
  		}
  		if (warnings && !overdeterminedOnly)
  		{
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, true);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, true);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, true);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, true);
  		}
  		else
  		{
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, false);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, false);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, false);
  			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, false);
  		}
  		long numberOfErrors = document.checkConsistency(); 
  		for (int i = 0; i < numberOfErrors; i++)
  		{
  			String error = document.getError(i).getMessage();
  			if (error.startsWith("Due to the need to instantiate models"))
  			{
  				continue;
  			}
  			if (error.startsWith("The CompFlatteningConverter has encountered a required package"))
  			{
  				continue;
  			}
  			message += numErrors + ":" + error + "\n";
  			numErrors++;
  		}
  		if (!overdeterminedOnly)
  		{
  			List<SBMLError> arraysErrors = ArraysValidator.validate(document);
  			for (int i = 0; i < arraysErrors.size(); i++)
  			{
  				String error = arraysErrors.get(i).getMessage();
  				message += numErrors + ":" + error + "\n";
  				numErrors++;
  			}
  		}
  	}
  
  	if (numErrors > 0)
  	{
  		JTextArea messageArea = new JTextArea(message);
  		messageArea.setLineWrap(true);
  		messageArea.setEditable(false);
  		JScrollPane scroll = new JScrollPane();
  		scroll.setMinimumSize(new java.awt.Dimension(600, 600));
  		scroll.setPreferredSize(new java.awt.Dimension(600, 600));
  		scroll.setViewportView(messageArea);
  		JOptionPane.showMessageDialog(Gui.frame, scroll, "SBML Errors and Warnings", JOptionPane.ERROR_MESSAGE);
  		return false;
  	}
  	return true;
  }

  /**
   * Check the types of the arguments to a function
   */
  public static boolean checkFunctionArgumentTypes(SBMLDocument document, ASTNode node)
  {
  	switch (node.getType())
  	{
  	case FUNCTION_ABS:
  	case FUNCTION_ARCCOS:
  	case FUNCTION_ARCCOSH:
  	case FUNCTION_ARCSIN:
  	case FUNCTION_ARCSINH:
  	case FUNCTION_ARCTAN:
  	case FUNCTION_ARCTANH:
  	case FUNCTION_ARCCOT:
  	case FUNCTION_ARCCOTH:
  	case FUNCTION_ARCCSC:
  	case FUNCTION_ARCCSCH:
  	case FUNCTION_ARCSEC:
  	case FUNCTION_ARCSECH:
  	case FUNCTION_COS:
  	case FUNCTION_COSH:
  	case FUNCTION_SIN:
  	case FUNCTION_SINH:
  	case FUNCTION_TAN:
  	case FUNCTION_TANH:
  	case FUNCTION_COT:
  	case FUNCTION_COTH:
  	case FUNCTION_CSC:
  	case FUNCTION_CSCH:
  	case FUNCTION_SEC:
  	case FUNCTION_SECH:
  	case FUNCTION_CEILING:
  	case FUNCTION_FACTORIAL:
  	case FUNCTION_EXP:
  	case FUNCTION_FLOOR:
  	case FUNCTION_LN:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument for " + SBMLutilities.myFormulaToString(node) + " function must evaluate to a number.",
  					"Number Expected", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case LOGICAL_NOT:
  		if (!node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument for not function must be of type Boolean.", "Boolean Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case LOGICAL_AND:
  	case LOGICAL_OR:
  	case LOGICAL_XOR:
  		for (int i = 0; i < node.getChildCount(); i++)
  		{
  			if (!node.getChild(i).isBoolean())
  			{
  				JOptionPane.showMessageDialog(Gui.frame, "Argument " + i + " for " + SBMLutilities.myFormulaToString(node)
  						+ " function is not of type Boolean.", "Boolean Expected", JOptionPane.ERROR_MESSAGE);
  				return true;
  			}
  		}
  		break;
  	case PLUS:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for + operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getChild(1).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for + operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case MINUS:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for - operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if ((node.getChildCount() > 1) && (node.getChild(1).isBoolean()))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for - operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case TIMES:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for * operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getChild(1).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for * operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case DIVIDE:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for / operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getChild(1).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for / operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case POWER:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for ^ operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getChild(1).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for ^ operator must evaluate to a number.", "Number Expected",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case FUNCTION_DELAY:
  	case FUNCTION_POWER:
  	case FUNCTION_ROOT:
  	case RELATIONAL_GEQ:
  	case RELATIONAL_LEQ:
  	case RELATIONAL_LT:
  	case RELATIONAL_GT:
  	case FUNCTION_LOG:
  		if (node.getChild(0).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for " + SBMLutilities.myFormulaToString(node) + " function must evaluate to a number.",
  					"Number Expected", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getChild(1).isBoolean())
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for " + SBMLutilities.myFormulaToString(node) + " function must evaluate to a number.",
  					"Number Expected", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case RELATIONAL_EQ:
  	case RELATIONAL_NEQ:
  		if ((node.getChild(0).isBoolean() && !node.getChild(1).isBoolean()) || (!node.getChild(0).isBoolean() && node.getChild(1).isBoolean()))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Arguments for " + SBMLutilities.myFormulaToString(node)
  					+ " function must either both be numbers or Booleans.", "Argument Mismatch", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case FUNCTION_PIECEWISE:
  		for (int i = 1; i < node.getChildCount(); i += 2)
  		{
  			if (!node.getChild(i).isBoolean())
  			{
  				JOptionPane.showMessageDialog(Gui.frame, "Even arguments of piecewise function must be of type Boolean.", "Boolean Expected",
  						JOptionPane.ERROR_MESSAGE);
  				return true;
  			}
  		}
  		int pieceType = -1;
  		for (int i = 0; i < node.getChildCount(); i += 2)
  		{
  			if (node.getChild(i).isBoolean())
  			{
  				if (pieceType == 2)
  				{
  					JOptionPane.showMessageDialog(Gui.frame, "All odd arguments of a piecewise function must agree.", "Type Mismatch",
  							JOptionPane.ERROR_MESSAGE);
  					return true;
  				}
  				pieceType = 1;
  			}
  			else
  			{
  				if (pieceType == 1)
  				{
  					JOptionPane.showMessageDialog(Gui.frame, "All odd arguments of a piecewise function must agree.", "Type Mismatch",
  							JOptionPane.ERROR_MESSAGE);
  					return true;
  				}
  				pieceType = 2;
  			}
  		}
  		break;
  	case FUNCTION:
  		break;
  	case NAME:
  		break;
  	default:
  	}
  	for (int c = 0; c < node.getChildCount(); c++)
  	{
  		if (checkFunctionArgumentTypes(document, node.getChild(c)))
  		{
  			return true;
  		}
  	}
  	return false;
  }

  public static Boolean isSpecialFunction(Model model,String functionId)
  {
  	if (functionId.equals("uniform"))
  	{
  		return true;
  	}
  	else if (functionId.equals("normal"))
  	{
  		return true;
  	}
  	else if (functionId.equals("exponential"))
  	{
  		return true;
  	}
  	else if (functionId.equals("gamma"))
  	{
  		return true;
  	}
  	else if (functionId.equals("lognormal"))
  	{
  		return true;
  	}
  	else if (functionId.equals("chisq"))
  	{
  		return true;
  	}
  	else if (functionId.equals("laplace"))
  	{
  		return true;
  	}
  	else if (functionId.equals("cauchy"))
  	{
  		return true;
  	}
  	else if (functionId.equals("poisson"))
  	{
  		return true;
  	}
  	else if (functionId.equals("binomial"))
  	{
  		return true;
  	}
  	else if (functionId.equals("bernoulli"))
  	{
  		return true;
  	}
  	else if (functionId.equals("St"))
  	{
  		return true;
  	}
  	else if (functionId.equals("PSt"))
  	{
  		SBMLutilities.createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
  		return true;
  	}
  	else if (functionId.equals("PG"))
  	{
  		SBMLutilities.createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
  		return true;
  	}
  	else if (functionId.equals("PF"))
  	{
  		SBMLutilities.createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
  		return true;
  	}
  	else if (functionId.equals("PU"))
  	{
  		SBMLutilities.createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
  		return true;
  	}
  	else if (functionId.equals("G"))
  	{
  		return true;
  	}
  	else if (functionId.equals("F"))
  	{
  		return true;
  	}
  	else if (functionId.equals("U"))
  	{
  		return true;
  	}
  	return false;
  }

  /**
   * Checks consistency of the sbml file.
   */
  public static void checkOverDetermined(SBMLDocument document)
  {
  	Preferences biosimrc = Preferences.userRoot();
  	if (biosimrc.get("biosim.general.validate", "").equals("libsbml") && Gui.isLibsbmlFound())
  	{
  		try
  		{
  			org.sbml.libsbml.SBMLDocument doc = new org.sbml.libsbml.SBMLReader()
  					.readSBMLFromString(new SBMLWriter().writeSBMLToString(document));
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MATHML_CONSISTENCY, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_SBO_CONSISTENCY, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MODELING_PRACTICE, false);
  			doc.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
  			long numErrors = doc.checkConsistency();
  			if (numErrors > 0)
  			{
  				JOptionPane.showMessageDialog(Gui.frame, "Algebraic rules make model overdetermined.", "Model is Overdetermined",
  						JOptionPane.WARNING_MESSAGE);
  			}
  		}
  		catch (SBMLException e)
  		{
  			e.printStackTrace();
  		}
  		catch (XMLStreamException e)
  		{
  			e.printStackTrace();
  		}
  	}
  	else
  	{
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, false);
  		document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
  		long numErrors = document.checkConsistency();
  		if (numErrors > 0)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Algebraic rules make model overdetermined.", "Model is Overdetermined",
  					JOptionPane.WARNING_MESSAGE);
  		}
  	}
  }

  /**
   * Create check if species used in reaction
   */
  public static boolean usedInReaction(SBMLDocument document, String id)
  {
  	for (int i = 0; i < document.getModel().getReactionCount(); i++)
  	{
  		for (int j = 0; j < document.getModel().getReaction(i).getReactantCount(); j++)
  		{
  			if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id))
  			{
  				return true;
  			}
  		}
  		for (int j = 0; j < document.getModel().getReaction(i).getProductCount(); j++)
  		{
  			if (document.getModel().getReaction(i).getProduct(j).getSpecies().equals(id))
  			{
  				return true;
  			}
  		}
  	}
  	return false;
  }

  /**
   * Check if compartment is in use.
   */
  public static boolean compartmentInUse(SBMLDocument document, String compartmentId)
  {
  	boolean remove = true;
  	ArrayList<String> speciesUsing = new ArrayList<String>();
  	for (int i = 0; i < document.getModel().getSpeciesCount(); i++)
  	{
  		Species species = document.getModel().getListOfSpecies().get(i);
  		if (species.isSetCompartment())
  		{
  			if (species.getCompartment().equals(compartmentId))
  			{
  				remove = false;
  				speciesUsing.add(species.getId());
  			}
  		}
  	}
  	ArrayList<String> reactionsUsing = new ArrayList<String>();
  	for (int i = 0; i < document.getModel().getReactionCount(); i++)
  	{
  		Reaction reaction = document.getModel().getReaction(i);
  		if (reaction.isSetCompartment())
  		{
  			if (reaction.getCompartment().equals(compartmentId))
  			{
  				remove = false;
  				reactionsUsing.add(reaction.getId());
  			}
  		}
  	}
  	ArrayList<String> outsideUsing = new ArrayList<String>();
  	for (int i = 0; i < document.getModel().getCompartmentCount(); i++)
  	{
  		Compartment compartment = document.getModel().getCompartment(i);
  		if (compartment.isSetOutside())
  		{
  			if (compartment.getOutside().equals(compartmentId))
  			{
  				remove = false;
  				outsideUsing.add(compartment.getId());
  			}
  		}
  	}
  	if (!remove)
  	{
  		String message = "Unable to remove the selected compartment.";
  		if (speciesUsing.size() != 0)
  		{
  			message += "\n\nIt contains the following species:\n";
  			String[] vars = speciesUsing.toArray(new String[0]);
  			Utility.sort(vars);
  			for (int i = 0; i < vars.length; i++)
  			{
  				if (i == vars.length - 1)
  				{
  					message += vars[i];
  				}
  				else
  				{
  					message += vars[i] + "\n";
  				}
  			}
  		}
  		if (reactionsUsing.size() != 0)
  		{
  			message += "\n\nIt contains the following reactions:\n";
  			String[] vars = reactionsUsing.toArray(new String[0]);
  			Utility.sort(vars);
  			for (int i = 0; i < vars.length; i++)
  			{
  				if (i == vars.length - 1)
  				{
  					message += vars[i];
  				}
  				else
  				{
  					message += vars[i] + "\n";
  				}
  			}
  		}
  		if (outsideUsing.size() != 0)
  		{
  			message += "\n\nIt outside the following compartments:\n";
  			String[] vars = outsideUsing.toArray(new String[0]);
  			Utility.sort(vars);
  			for (int i = 0; i < vars.length; i++)
  			{
  				if (i == vars.length - 1)
  				{
  					message += vars[i];
  				}
  				else
  				{
  					message += vars[i] + "\n";
  				}
  			}
  		}
  		JTextArea messageArea = new JTextArea(message);
  		messageArea.setEditable(false);
  		JScrollPane scroll = new JScrollPane();
  		scroll.setMinimumSize(new java.awt.Dimension(300, 300));
  		scroll.setPreferredSize(new java.awt.Dimension(300, 300));
  		scroll.setViewportView(messageArea);
  		JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Compartment", JOptionPane.ERROR_MESSAGE);
  	}
  	return !remove;
  }

  /**
   * Check the number of arguments to a function
   */
  public static boolean checkNumFunctionArguments(SBMLDocument document, ASTNode node)
  {
  	ListOf<FunctionDefinition> sbml = document.getModel().getListOfFunctionDefinitions();
  	if (node == null)
  	{
  		JOptionPane.showMessageDialog(Gui.frame, "Parse error in math formula.", "Parse Error", JOptionPane.ERROR_MESSAGE);
  		return true;
  	}
  	switch (node.getType())
  	{
  	case FUNCTION_ABS:
  	case FUNCTION_ARCCOS:
  	case FUNCTION_ARCCOSH:
  	case FUNCTION_ARCSIN:
  	case FUNCTION_ARCSINH:
  	case FUNCTION_ARCTAN:
  	case FUNCTION_ARCTANH:
  	case FUNCTION_ARCCOT:
  	case FUNCTION_ARCCOTH:
  	case FUNCTION_ARCCSC:
  	case FUNCTION_ARCCSCH:
  	case FUNCTION_ARCSEC:
  	case FUNCTION_ARCSECH:
  	case FUNCTION_COS:
  	case FUNCTION_COSH:
  	case FUNCTION_SIN:
  	case FUNCTION_SINH:
  	case FUNCTION_TAN:
  	case FUNCTION_TANH:
  	case FUNCTION_COT:
  	case FUNCTION_COTH:
  	case FUNCTION_CSC:
  	case FUNCTION_CSCH:
  	case FUNCTION_SEC:
  	case FUNCTION_SECH:
  	case FUNCTION_CEILING:
  	case FUNCTION_FACTORIAL:
  	case FUNCTION_EXP:
  	case FUNCTION_FLOOR:
  	case FUNCTION_LN:
  		if (node.getChildCount() != 1)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + SBMLutilities.myFormulaToString(node) + " but found " + node.getChildCount()
  					+ ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case LOGICAL_NOT:
  		if (node.getChildCount() != 1)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + SBMLutilities.myFormulaToString(node) + " but found " + node.getChildCount()
  					+ ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case LOGICAL_AND:
  	case LOGICAL_OR:
  	case LOGICAL_XOR:
  	case PLUS:
  	case MINUS:
  	case TIMES:
  	case DIVIDE:
  	case POWER:
  		break;
  	case FUNCTION_DELAY:
  	case FUNCTION_POWER:
  	case FUNCTION_ROOT:
  	case RELATIONAL_GEQ:
  	case RELATIONAL_LEQ:
  	case RELATIONAL_LT:
  	case RELATIONAL_GT:
  	case FUNCTION_LOG:
  		if (node.getChildCount() != 2)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + SBMLutilities.myFormulaToString(node) + " but found " + node.getChildCount()
  					+ ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case RELATIONAL_EQ:
  	case RELATIONAL_NEQ:
  		if (node.getChildCount() != 2)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + SBMLutilities.myFormulaToString(node) + " but found " + node.getChildCount()
  					+ ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case FUNCTION_PIECEWISE:
  		if (node.getChildCount() < 1)
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		break;
  	case FUNCTION:
  		for (int i = 0; i < document.getModel().getFunctionDefinitionCount(); i++)
  		{
  			if (sbml.get(i).getId().equals(node.getName()))
  			{
  				long numArgs = sbml.get(i).getArgumentCount();
  				if (numArgs != node.getChildCount())
  				{
  					JOptionPane.showMessageDialog(Gui.frame, "Expected " + numArgs + " argument(s) for " + SBMLutilities.myFormulaToString(node)
  							+ " but found " + node.getChildCount() + ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  					return true;
  				}
  				break;
  			}
  		}
  		break;
  	case NAME:
  		if (node.getName().equals("abs") || node.getName().equals("arccos") || node.getName().equals("arccosh")
  				|| node.getName().equals("arcsin") || node.getName().equals("arcsinh") || node.getName().equals("arctan")
  				|| node.getName().equals("arctanh") || node.getName().equals("arccot") || node.getName().equals("arccoth")
  				|| node.getName().equals("arccsc") || node.getName().equals("arccsch") || node.getName().equals("arcsec")
  				|| node.getName().equals("arcsech") || node.getName().equals("acos") || node.getName().equals("acosh")
  				|| node.getName().equals("asin") || node.getName().equals("asinh") || node.getName().equals("atan")
  				|| node.getName().equals("atanh") || node.getName().equals("acot") || node.getName().equals("acoth")
  				|| node.getName().equals("acsc") || node.getName().equals("acsch") || node.getName().equals("asec")
  				|| node.getName().equals("asech") || node.getName().equals("cos") || node.getName().equals("cosh")
  				|| node.getName().equals("cot") || node.getName().equals("coth") || node.getName().equals("csc") || node.getName().equals("csch")
  				|| node.getName().equals("ceil") || node.getName().equals("factorial") || node.getName().equals("exp")
  				|| node.getName().equals("floor") || node.getName().equals("ln") || node.getName().equals("log") || node.getName().equals("sqr")
  				|| node.getName().equals("log10") || node.getName().equals("sqrt") || node.getName().equals("sec")
  				|| node.getName().equals("sech") || node.getName().equals("sin") || node.getName().equals("sinh") || node.getName().equals("tan")
  				|| node.getName().equals("tanh") || node.getName().equals("not"))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found 0.",
  					"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getName().equals("and") || node.getName().equals("or") || node.getName().equals("xor") || node.getName().equals("pow")
  				|| node.getName().equals("eq") || node.getName().equals("geq") || node.getName().equals("leq") || node.getName().equals("gt")
  				|| node.getName().equals("neq") || node.getName().equals("lt") || node.getName().equals("delay") || node.getName().equals("root"))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found 0.",
  					"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		if (node.getName().equals("piecewise"))
  		{
  			JOptionPane.showMessageDialog(Gui.frame, "Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
  					JOptionPane.ERROR_MESSAGE);
  			return true;
  		}
  		for (int i = 0; i < document.getModel().getFunctionDefinitionCount(); i++)
  		{
  			if (sbml.get(i).getId().equals(node.getName()))
  			{
  				long numArgs = sbml.get(i).getArgumentCount();
  				JOptionPane.showMessageDialog(Gui.frame, "Expected " + numArgs + " argument(s) for " + SBMLutilities.myFormulaToString(node) + " but found 0.",
  						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
  				return true;
  			}
  		}
  		break;
  	default:
  	}
  	for (int c = 0; c < node.getChildCount(); c++)
  	{
  		if (checkNumFunctionArguments(document, node.getChild(c)))
  		{
  			return true;
  		}
  	}
  	return false;
  }
}
