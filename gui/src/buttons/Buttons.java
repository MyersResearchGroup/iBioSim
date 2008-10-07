package buttons;

import java.io.*;
import java.awt.Component;
import java.awt.FileDialog;
import java.util.*;
import java.util.prefs.Preferences;
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
	public static String browse(JFrame frame, File file, JTextField text, int i, String approve) {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.file_browser", "").equals("FileDialog")) {
			FileDialog fd;
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				if (approve.equals("Save") || approve.equals("New")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
				}
				else if (approve.equals("Open")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
				}
				else {
					fd = new FileDialog(frame, approve);
				}
			}
			else {
				if (approve.equals("Save") || approve.equals("New")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
				}
				else if (approve.equals("Open")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
				}
				else if (approve.equals("Export TSD")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.contains(".csv") || name.contains(".dat") || name.contains(".eps")
									|| name.contains(".jpg") || name.contains(".pdf") || name.contains(".png")
									|| name.contains(".svg") || name.contains(".tsd");
						}
					});
				}
				else if (approve.equals("Export Probability")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.contains(".eps") || name.contains(".jpg") || name.contains(".pdf")
									|| name.contains(".png") || name.contains(".svg");
						}
					});
				}
				else if (approve.equals("Import SBML")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.contains(".sbml") || name.contains(".xml");
						}
					});
				}
				else if (approve.equals("Import Genetic Circuit")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.contains(".gcm");
						}
					});
				}
				else if (approve.equals("Import")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.contains(".csv") || name.contains(".dat") || name.contains(".tsd");
						}
					});
				}
				else {
					fd = new FileDialog(frame, approve);
				}
			}
			if (file != null) {
				if (file.isDirectory()) {
					fd.setDirectory(file.getPath());
				}
				else {
					fd.setFile(file.getPath());
				}
			}
			fd.setVisible(true);
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				if (fd.getDirectory() != null) {
					return fd.getDirectory();
				}
				else {
					return "";
				}
			}
			else {
				if (fd.getFile() != null) {
					return fd.getFile();
				}
				else {
					return "";
				}
			}
			/*
			 * String open; Display display = new Display(); Shell shell = new
			 * Shell(display); shell.setImage(new Image(display,
			 * System.getenv("BIOSIM") + separator + "gui" + separator + "icons" +
			 * separator + "iBioSim.png")); if (i == JFileChooser.DIRECTORIES_ONLY) {
			 * DirectoryDialog dd = null; if (approve.equals("Save") ||
			 * approve.equals("New")) { dd = new DirectoryDialog(shell, SWT.SAVE);
			 * dd.setText(approve); } else if (approve.equals("Open")) { dd = new
			 * DirectoryDialog(shell, SWT.OPEN); dd.setText(approve); } else { dd =
			 * new DirectoryDialog(shell); dd.setText(approve); } if (file != null) {
			 * dd.setFilterPath(file.getPath()); } open = dd.open(); } else {
			 * FileDialog fd = null; if (approve.equals("Save") ||
			 * approve.equals("New")) { fd = new FileDialog(shell, SWT.SAVE);
			 * fd.setText(approve); } else if (approve.equals("Open")) { fd = new
			 * FileDialog(shell, SWT.OPEN); fd.setText(approve); } else if
			 * (approve.equals("Export TSD")) { fd = new FileDialog(shell, SWT.SAVE);
			 * fd.setText(approve); fd.setFilterNames(new String[] { "Comma Separated
			 * Values (*.csv)", "Tab Delimited Data (*.dat)", "Encapsulated Postscript
			 * (*.eps)", "JPEG (*.jpg)", "Portable Document Format (*.pdf)", "Portable
			 * Network Graphics (*.png)", "Scalable Vector Graphics (*.svg)", "Time
			 * Series Data (*.tsd)" }); fd.setFilterExtensions(new String[] { "*.csv",
			 * "*.dat", "*.eps", "*.jpg", "*.pdf", "*.png", "*.svg", "*.tsd" });
			 * fd.setFilterIndex(4); } else if (approve.equals("Export Probability")) {
			 * fd = new FileDialog(shell, SWT.SAVE); fd.setText(approve);
			 * fd.setFilterNames(new String[] { "Encapsulated Postscript (*.eps)",
			 * "JPEG (*.jpg)", "Portable Document Format (*.pdf)", "Portable Network
			 * Graphics (*.png)", "Scalable Vector Graphics (*.svg)" });
			 * fd.setFilterExtensions(new String[] { "*.eps", "*.jpg", "*.pdf",
			 * "*.png", "*.svg" }); fd.setFilterIndex(2); } else if
			 * (approve.equals("Import SBML")) { fd = new FileDialog(shell, SWT.OPEN);
			 * fd.setText(approve); fd.setFilterNames(new String[] { "Systems Biology
			 * Markup Language (*.sbml)", "Extensible Markup Language (*.xml)" });
			 * fd.setFilterExtensions(new String[] { "*.sbml", "*.xml" });
			 * fd.setFilterIndex(1); } else if (approve.equals("Import Genetic
			 * Circuit")) { fd = new FileDialog(shell, SWT.OPEN); fd.setText(approve);
			 * fd.setFilterNames(new String[] { "Genetic Circuit Model (*.gcm)" });
			 * fd.setFilterExtensions(new String[] { "*.gcm" }); } else if
			 * (approve.equals("Import")) { fd = new FileDialog(shell, SWT.OPEN);
			 * fd.setText(approve); fd.setFilterNames(new String[] { "Comma Separated
			 * Values (*.csv)", "Tab Delimited Data (*.dat)", "Time Series Data
			 * (*.tsd)" }); fd.setFilterExtensions(new String[] { "*.csv", "*.dat",
			 * "*.tsd" }); fd.setFilterIndex(2); } else { fd = new FileDialog(shell);
			 * fd.setText(approve); } if (file != null) {
			 * fd.setFilterPath(file.getParentFile().getPath()); } open = fd.open(); }
			 * shell.dispose(); display.dispose(); if (open != null) { return open; }
			 * else { return ""; }
			 */
		}
		else {
			String filename = "";
			JFileChooser fc = new JFileChooser();

			/* This sets the default directory to the one where BioSim is executed */
			/*
			 * String startDir = System.getProperty("user.dir"); File curDir = new
			 * File(startDir); fc.setCurrentDirectory(curDir);
			 */

			ExampleFileFilter csvFilter = new ExampleFileFilter();
			csvFilter.addExtension("csv");
			csvFilter.setDescription("Comma Separated Values");
			ExampleFileFilter datFilter = new ExampleFileFilter();
			datFilter.addExtension("dat");
			datFilter.setDescription("Tab Delimited Data");
			ExampleFileFilter tsdFilter = new ExampleFileFilter();
			tsdFilter.addExtension("tsd");
			tsdFilter.setDescription("Time Series Data");
			ExampleFileFilter epsFilter = new ExampleFileFilter();
			epsFilter.addExtension("eps");
			epsFilter.setDescription("Encapsulated Postscript");
			ExampleFileFilter jpgFilter = new ExampleFileFilter();
			jpgFilter.addExtension("jpg");
			jpgFilter.setDescription("JPEG");
			ExampleFileFilter pdfFilter = new ExampleFileFilter();
			pdfFilter.addExtension("pdf");
			pdfFilter.setDescription("Portable Document Format");
			ExampleFileFilter pngFilter = new ExampleFileFilter();
			pngFilter.addExtension("png");
			pngFilter.setDescription("Portable Network Graphics");
			ExampleFileFilter svgFilter = new ExampleFileFilter();
			svgFilter.addExtension("svg");
			svgFilter.setDescription("Scalable Vector Graphics");
			ExampleFileFilter sbmlFilter = new ExampleFileFilter();
			sbmlFilter.addExtension("sbml");
			sbmlFilter.setDescription("Systems Biology Markup Language");
			ExampleFileFilter xmlFilter = new ExampleFileFilter();
			xmlFilter.addExtension("xml");
			xmlFilter.setDescription("Extensible Markup Language");
			ExampleFileFilter gcmFilter = new ExampleFileFilter();
			gcmFilter.addExtension("gcm");
			gcmFilter.setDescription("Genetic Circuit Model");
			if (file != null) {
				fc.setSelectedFile(file);
			}
			fc.setFileSelectionMode(i);
			int retValue;
			if (approve.equals("Save")) {
				retValue = fc.showSaveDialog(frame);
			}
			else if (approve.equals("Open")) {
				retValue = fc.showOpenDialog(frame);
			}
			else if (approve.equals("Export TSD")) {
				fc.addChoosableFileFilter(csvFilter);
				fc.addChoosableFileFilter(datFilter);
				fc.addChoosableFileFilter(epsFilter);
				fc.addChoosableFileFilter(jpgFilter);
				fc.addChoosableFileFilter(pdfFilter);
				fc.addChoosableFileFilter(pngFilter);
				fc.addChoosableFileFilter(svgFilter);
				fc.addChoosableFileFilter(tsdFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(pdfFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Export Probability")) {
				fc.addChoosableFileFilter(epsFilter);
				fc.addChoosableFileFilter(jpgFilter);
				fc.addChoosableFileFilter(pdfFilter);
				fc.addChoosableFileFilter(pngFilter);
				fc.addChoosableFileFilter(svgFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(pdfFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Import SBML")) {
				fc.addChoosableFileFilter(sbmlFilter);
				fc.addChoosableFileFilter(xmlFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(xmlFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Import Genetic Circuit")) {
				fc.addChoosableFileFilter(gcmFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(gcmFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else {
				retValue = fc.showDialog(frame, approve);
			}
			if (retValue == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				if (text != null) {
					text.setText(file.getPath());
				}
				filename = file.getPath();
				if (approve.equals("Export TSD")) {
					if ((filename.length() < 4)
							|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".jpg"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".png"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".pdf"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".eps"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".svg"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".dat"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".tsd")) && !(filename.substring((filename.length() - 4), filename
									.length()).equals(".csv")))) {
						ExampleFileFilter selectedFilter = (ExampleFileFilter) fc.getFileFilter();
						if (selectedFilter == jpgFilter) {
							filename += ".jpg";
						}
						else if (selectedFilter == pngFilter) {
							filename += ".png";
						}
						else if (selectedFilter == pdfFilter) {
							filename += ".pdf";
						}
						else if (selectedFilter == epsFilter) {
							filename += ".eps";
						}
						else if (selectedFilter == svgFilter) {
							filename += ".svg";
						}
						else if (selectedFilter == datFilter) {
							filename += ".dat";
						}
						else if (selectedFilter == tsdFilter) {
							filename += ".tsd";
						}
						else if (selectedFilter == csvFilter) {
							filename += ".csv";
						}
					}
				}
				else if (approve.equals("Export Probability")) {
					if ((filename.length() < 4)
							|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".jpg"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".png"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".pdf"))
									&& !(filename.substring((filename.length() - 4), filename.length())
											.equals(".eps")) && !(filename.substring((filename.length() - 4), filename
									.length()).equals(".svg")))) {
						ExampleFileFilter selectedFilter = (ExampleFileFilter) fc.getFileFilter();
						if (selectedFilter == jpgFilter) {
							filename += ".jpg";
						}
						else if (selectedFilter == pngFilter) {
							filename += ".png";
						}
						else if (selectedFilter == pdfFilter) {
							filename += ".pdf";
						}
						else if (selectedFilter == epsFilter) {
							filename += ".eps";
						}
						else if (selectedFilter == svgFilter) {
							filename += ".svg";
						}
					}
				}
			}
			return filename;
		}
	}

	/**
	 * Removes the selected values of the given JList from the given list and
	 * updates the JList.
	 */
	public static Object[] remove(JList currentList, Object[] list) {
		Object[] removeSelected = currentList.getSelectedValues();
		int[] select = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			select[i] = i;
		}
		currentList.setSelectedIndices(select);
		Object[] getAll = currentList.getSelectedValues();
		currentList.removeSelectionInterval(0, list.length - 1);
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
		list = keep;
		return list;
	}

	/**
	 * Adds the selected values in the add JList to the list JList. Stores all
	 * these values into the currentList array and returns this array.
	 */
	public static Object[] add(Object[] currentList, JList list, JList add, boolean isTermCond,
			JTextField amountTerm, JRadioButton ge, JRadioButton gt, JRadioButton eq, JRadioButton lt,
			JRadioButton le, Component component) {
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
				}
				catch (Exception except) {
					JOptionPane.showMessageDialog(component,
							"Must Enter A Real Number Into The Termination Condition Field.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return currentList;
				}
				if (ge.isSelected()) {
					newSelected[i] = temp + ".amount.ge." + amount;
				}
				else if (gt.isSelected()) {
					newSelected[i] = temp + ".amount.gt." + amount;
				}
				else if (eq.isSelected()) {
					newSelected[i] = temp + ".amount.eq." + amount;
				}
				else if (lt.isSelected()) {
					newSelected[i] = temp + ".amount.lt." + amount;
				}
				else if (le.isSelected()) {
					newSelected[i] = temp + ".amount.le." + amount;
				}
			}
		}
		Object[] temp = currentList;
		int newLength = temp.length;
		for (int i = 0; i < newSelected.length; i++) {
			int j = 0;
			for (j = 0; j < temp.length; j++) {
				if (temp[j].equals(newSelected[i])) {
					break;
				}
			}
			if (j == temp.length)
				newLength++;
		}
		currentList = new Object[newLength];
		for (int i = 0; i < temp.length; i++) {
			currentList[i] = temp[i];
		}
		int num = temp.length;
		for (int i = 0; i < newSelected.length; i++) {
			int j = 0;
			for (j = 0; j < temp.length; j++)
				if (temp[j].equals(newSelected[i]))
					break;
			if (j == temp.length) {
				currentList[num] = newSelected[i];
				num++;
			}
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
		}
		else {
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
