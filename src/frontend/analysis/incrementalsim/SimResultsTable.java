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
package frontend.analysis.incrementalsim;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SimResultsTable extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimResultsTable(DefaultTableModel tableModel){
		super(new GridLayout(1,0));
		JTable table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 700));
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
	}
	
	public static void showTable(JFrame tableFrame, SimResultsTable simResultsTbl){
		//Create and set up the content pane.
	    tableFrame.setContentPane(simResultsTbl);
	    //Display the window.
	    tableFrame.pack();
	    tableFrame.setVisible(true);
	}
}
