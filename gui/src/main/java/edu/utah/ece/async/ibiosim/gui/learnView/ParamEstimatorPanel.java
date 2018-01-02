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
package edu.utah.ece.async.ibiosim.gui.learnView;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;

/**
 *  
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ParamEstimatorPanel extends JPanel implements ActionListener, Runnable, ListSelectionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JComboBox			methods;

	private final String[]		methodNames	= { "None", "SRES" };

	private SBMLDocument		sbmlDocument;

	private JList				list, list2;

	JButton						add, remove, insertBound;

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public ParamEstimatorPanel(String filename, String directory, Gui biosim)
	{

		// TODO: refactor; this code is ugly
		try
		{
			this.sbmlDocument = SBMLReader.read(new File(filename));
		}
		catch (XMLStreamException e)
		{
			System.out.println("Could not parse file");
			return;
		}
		catch (IOException e)
		{
			System.out.println("Could not find file");
			return;
		}

		Model model = sbmlDocument.getModel();

		DefaultListModel listmodel1 = new DefaultListModel();

		DefaultListModel listmodel2 = new DefaultListModel();
		int count = 0;
		for (Parameter param : model.getListOfParameters())
		{
			listmodel1.add(count++, param.getId());
		}

		String[] fileArray = GlobalConstants.splitPath(filename);
		String file = fileArray[fileArray.length - 1];
		JPanel thresholdPanel1 = new JPanel(new GridLayout(2, 2));
		JLabel backgroundLabel = new JLabel("Model File:");
		JTextField backgroundField = new JTextField(file);
		backgroundField.setEditable(false);
		thresholdPanel1.add(backgroundLabel);
		thresholdPanel1.add(backgroundField);
		JLabel methodsLabel = new JLabel("Learn Method: ");
		methods = new JComboBox(methodNames);
		methods.addActionListener(this);
		thresholdPanel1.add(methodsLabel);
		thresholdPanel1.add(methods);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel thresholdPanel2 = new JPanel(layout);
		JLabel leftLabel = new JLabel("List of Parameters Available");
		JLabel rightLabel = new JLabel("List of Parameters To Estimate");
		JLabel middleLabel = new JLabel("");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		thresholdPanel2.add(leftLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		thresholdPanel2.add(middleLabel);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		thresholdPanel2.add(rightLabel);

		JPanel thresholdPanel3 = new JPanel(new GridLayout(2, 1));
		add = new JButton(">>>");
		remove = new JButton("<<<");
		add.addActionListener(this);
		remove.addActionListener(this);
		thresholdPanel3.add(add);
		thresholdPanel3.add(remove);

		list = new JList(listmodel1);
		list2 = new JList(listmodel2);
		list.addListSelectionListener(this);
		JScrollPane pane = new JScrollPane(list);
		JScrollPane pane2 = new JScrollPane(list2);

		DefaultListSelectionModel m = new DefaultListSelectionModel();
		m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m.setLeadAnchorNotificationEnabled(false);
		list.setSelectionModel(m);

		DefaultListSelectionModel m2 = new DefaultListSelectionModel();
		m2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m2.setLeadAnchorNotificationEnabled(false);
		list2.setSelectionModel(m2);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		thresholdPanel2.add(pane, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		thresholdPanel2.add(thresholdPanel3, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 1;
		thresholdPanel2.add(pane2, c);

		JPanel thresholdPanelHold1 = new JPanel();
		thresholdPanelHold1.add(thresholdPanel1);
		layout = new GridBagLayout();
		JPanel firstTab = new JPanel(layout);
		JPanel firstTab1 = new JPanel(new BorderLayout());
		firstTab1.add(thresholdPanelHold1);
		// firstTab.add(firstTab1);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		firstTab.add(firstTab1, c);

		JPanel middlePanel = new JPanel(new BorderLayout());

		middlePanel.add(thresholdPanel2);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		firstTab.add(middlePanel, c);
		this.add(firstTab);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{

		if (e.getSource() == add)
		{
			int index = list.getSelectedIndex();
			DefaultListModel lm1 = (DefaultListModel) list.getModel();
			DefaultListModel lm2 = (DefaultListModel) list2.getModel();

			if (index >= 0)
			{
				lm2.addElement(list.getSelectedValue());
				lm1.removeElement(list.getSelectedValue());
			}
		}
		else if (e.getSource() == remove)
		{
			int index = list2.getSelectedIndex();

			DefaultListModel lm1 = (DefaultListModel) list.getModel();
			DefaultListModel lm2 = (DefaultListModel) list2.getModel();

			if (index >= 0)
			{
				lm1.addElement(list2.getSelectedValue());
				lm2.removeElement(list2.getSelectedValue());
			}
		}

	}

	@Override
	public void run()
	{
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource() == list)
		{
			list2.clearSelection();
		}
		else if (e.getSource() == list2)
		{
			list.clearSelection();
		}
	}

	public Object getSelection()
	{
		return methods.getSelectedItem();
	}

	public List<String> getSelectedParameters()
	{
		DefaultListModel lm2 = (DefaultListModel) list2.getModel();
		List<String> parameters = new ArrayList<String>();

		for (int i = 0; i < lm2.getSize(); i++)
		{
			parameters.add((String) lm2.get(i));
		}

		return parameters;
	}

}
