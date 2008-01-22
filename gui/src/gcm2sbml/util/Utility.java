package gcm2sbml.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Species;

/**
 * This is a utility class. The constructor is private so that only one instance
 * of the class exists at any time.
 * 
 * @author Nam
 * 
 */
public class Utility {
	private Utility() {
	};

	public static final Utility getInstance() {
		if (instance == null) {
			instance = new Utility();
		}
		return instance;
	}

	/**
	 * Creates a copy of a double array
	 * 
	 * @param toCopy
	 *            the array to copy
	 * @return a copy of a double array
	 */
	public static double[] createCopy(double[] toCopy) {
		double[] copy = new double[toCopy.length];
		System.arraycopy(toCopy, 0, copy, 0, toCopy.length);
		return copy;
	}

	public static void print(boolean debug, String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	public static String makeBindingReaction(String name,
			ArrayList<String> reactants, ArrayList<String> products) {
		return "";
	}
	
	public static Compartment makeCompartment(String id) {
		Compartment c = new Compartment("default");
		c.setConstant(true);
		c.setSpatialDimensions(3);
		return c;
	}
	
	public static Species makeSpecies(String id, String compartment, double amount) {
		Species specie = new Species(id, id);
		specie.setCompartment(compartment);
		specie.setInitialAmount(amount);
		return specie;
	}
	
	public static 	/* Create add/remove/edit panel */
	JPanel createPanel(ActionListener listener, String panelName, JList panelJList,
			JButton addButton, JButton removeButton, JButton editButton) {
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel addRem = new JPanel();
		addRem.add(addButton);
		addRem.add(removeButton);
		addRem.add(editButton);
		addButton.addActionListener(listener);
		removeButton.addActionListener(listener);
		editButton.addActionListener(listener);
		JLabel panelLabel = new JLabel("List of " + panelName + ":");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(panelJList);

		if (listener instanceof MouseListener) {
			panelJList.addMouseListener((MouseListener)listener);
		}		
		Panel.add(panelLabel, "North");
		Panel.add(scroll, "Center");
		Panel.add(addRem, "South");
		return Panel;
	}

	
	private static Utility instance = null;
}
