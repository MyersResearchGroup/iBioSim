package buttons.core.gui;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * This class contains static methods that perform tasks based on which buttons
 * are pressed.
 * 
 * @author Curtis Madsen
 */
public class Buttons {

	/**
	 * Returns the pathname of the selected file in the file chooser.
	 */
	public static String browse(Component browse, File file, JTextField text, int i, String approve) {
		String filename = "";
		JFileChooser fc = new JFileChooser();
		if (file != null) {
			fc.setSelectedFile(file);
		}
		fc.setFileSelectionMode(i);
		int retValue;
		if (approve.equals("Save")) {
			retValue = fc.showSaveDialog(browse);
		} else if (approve.equals("Open")) {
			retValue = fc.showOpenDialog(browse);
		} else {
			retValue = fc.showDialog(browse, approve);
		}
		if (retValue == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			if (text != null) {
				text.setText(file.getPath());
			}
			filename = file.getPath();
		}
		return filename;
	}

	/**
	 * Removes the selected values of the given JList from the given list and
	 * updates the JList.
	 */
	public static void remove(JList currentList, Object[] list) {
		Object[] removeSelected = currentList.getSelectedValues();
		int[] select = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			select[i] = i;
		}
		currentList.setSelectedIndices(select);
		Object[] getAll = currentList.getSelectedValues();
		ArrayList<Object> remove = new ArrayList<Object>();
		for (int i = 0; i < getAll.length; i++) {
			remove.add(getAll[i]);
		}
		for (int i = 0; i < removeSelected.length; i++) {
			remove.remove(removeSelected[i]);
		}
		String[] keep = new String[remove.size()];
		for (int i = 0; i < remove.size(); i++) {
			keep[i] = (String) remove.get(i);
		}
		currentList.setListData(keep);
	}

	/**
	 * Adds the selected values in the add JList to the list JList. Stores all
	 * these values into the currentList array and returns this array.
	 */
	public static Object[] add(Object[] currentList, JList list, JList add, boolean isTermCond,
			JTextField amountTerm, JRadioButton ge, JRadioButton gt, JRadioButton eq,
			JRadioButton lt, JRadioButton le, JComboBox quantity, Component component) {
		int[] select = new int[currentList.length];
		for (int i = 0; i < currentList.length; i++) {
			select[i] = i;
		}
		list.setSelectedIndices(select);
		currentList = list.getSelectedValues();
		Object[] newSelected = add.getSelectedValues();
		if (isTermCond) {
			for (int i = 0; i < newSelected.length; i++) {
				String temp = (String) newSelected[i];
				double amount = 0.0;
				try {
					amount = Double.parseDouble(amountTerm.getText().trim());
				} catch (Exception except) {
					JOptionPane.showMessageDialog(component,
							"Must Enter A Double In The Termination Condition Field.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return currentList;
				}
				if (ge.isSelected()) {
					newSelected[i] = temp + "." + quantity.getSelectedItem() + ".ge." + amount;
				} else if (gt.isSelected()) {
					newSelected[i] = temp + "." + quantity.getSelectedItem() + ".gt." + amount;
				} else if (eq.isSelected()) {
					newSelected[i] = temp + "." + quantity.getSelectedItem() + ".eq." + amount;
				} else if (lt.isSelected()) {
					newSelected[i] = temp + "." + quantity.getSelectedItem() + ".lt." + amount;
				} else if (le.isSelected()) {
					newSelected[i] = temp + "." + quantity.getSelectedItem() + ".le." + amount;
				}
			}
		}
		Object[] temp = currentList;
		currentList = new Object[newSelected.length + temp.length];
		for (int i = 0; i < temp.length; i++) {
			currentList[i] = temp[i];
		}
		for (int i = 0; i < newSelected.length; i++) {
			currentList[i + temp.length] = newSelected[i];
		}
		list.setListData(currentList);
		return currentList;
	}

	/**
	 * Returns a list of all the objects in the given JList.
	 */
	public static String[] getList(Object[] size, JList objects) {
		String[] list;
		if (size.length == 0) {
			list = new String[0];
		} else {
			int[] select = new int[size.length];
			for (int i = 0; i < size.length; i++) {
				select[i] = i;
			}
			objects.setSelectedIndices(select);
			size = objects.getSelectedValues();
			list = new String[size.length];
			for (int i = 0; i < size.length; i++) {
				list[i] = (String) size[i];
			}
		}
		return list;
	}
}