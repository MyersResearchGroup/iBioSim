package main.util;

import java.io.*;
import java.awt.Component;
import java.awt.FileDialog;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;

import biomodel.util.GlobalConstants;

/**
 * This class contains static methods that perform tasks based on which buttons
 * are pressed.
 * 
 * @author Curtis Madsen
 */
public class Utility {

	/**
	 * Returns the pathname of the selected file in the file chooser.
	 */
	public static String browse(JFrame frame, File file, JTextField text, int i, String approve, int fileType) {
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
				fd.setFilenameFilter(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return false;
					}
				});
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
							return name.endsWith(".csv") || name.endsWith(".dat") || name.endsWith(".eps") || name.endsWith(".jpg")
									|| name.endsWith(".pdf") || name.endsWith(".png") || name.endsWith(".svg") || name.endsWith(".tsd");
						}
					});
				}
				else if (approve.equals("Export Probability")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".eps") || name.endsWith(".jpg") || name.endsWith(".pdf") || name.endsWith(".png")
									|| name.endsWith(".svg");
						}
					});
				}
				else if (approve.equals("Import SBOL") || approve.equals("Export DNA Component")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml") || name.endsWith(GlobalConstants.SBOL_FILE_EXTENSION)
									|| name.endsWith(GlobalConstants.RDF_FILE_EXTENSION);
						}
					});
				}
				else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith("-sedml.xml");
						}
					});
				}
//				else if (approve.equals("Export DNA Component")) {
//					fd = new FileDialog(frame, approve, FileDialog.LOAD);
//					fd.setFilenameFilter(new FilenameFilter() {
//						public boolean accept(File dir, String name) {
//							return name.endsWith(".sbol");
//						}
//					});
//				}
				else if (approve.equals("Import SBML")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".sbml") || name.endsWith(".xml");
						}
					});
				}
				else if (approve.equals("Export SBML")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".sbml") || name.endsWith(".xml");
						}
					});
				}
				else if (approve.equals("Save AVI")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".avi");
						}
					});
				}
				else if (approve.equals("Save MP4")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".mp4");
						}
					});
				}
				else if (approve.equals("Import Genetic Circuit")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".gcm");
						}
					});
				}
				else if (approve.equals("Import")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".csv") || name.endsWith(".dat") || name.endsWith(".tsd");
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
					fd.setDirectory(file.getPath());
					fd.setFile(file.getName());
				}
			}
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
			}
			fd.setVisible(true);
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			}
			if (fd.getFile() != null) {
				if (fd.getDirectory() != null) {
					String selectedFile = fd.getFile();
					if (approve.equals("Export TSD")) {
						if (!selectedFile.endsWith(".csv") && !selectedFile.endsWith(".dat") && !selectedFile.endsWith(".eps")
								&& !selectedFile.endsWith(".jpg") && !selectedFile.endsWith(".pdf") && !selectedFile.endsWith(".png")
								&& !selectedFile.endsWith(".svg") && !selectedFile.endsWith(".tsd")) {
							selectedFile += ".pdf";
						}
					}
					else if (approve.equals("Export Probability")) {
						if (!selectedFile.endsWith(".eps") && !selectedFile.endsWith(".jpg") && !selectedFile.endsWith(".pdf")
								&& !selectedFile.endsWith(".png") && !selectedFile.endsWith(".svg")) {
							selectedFile += ".pdf";
						}
					}
					else if (approve.equals("Import SBOL") || approve.equals("Export DNA Component")) {
						if (!selectedFile.endsWith(".sbol") && !selectedFile.endsWith(".xml"))
							selectedFile += ".xml";
					}
					else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
						if (!selectedFile.endsWith("-sedml.xml"))
							selectedFile += "-sedml.xml";
					}
					else if (approve.equals("Import SBML") || (approve.equals("Export SMBL"))) {
						if (!selectedFile.endsWith(".sbml") && !selectedFile.endsWith(".xml")) {
							selectedFile += ".xml";
						}
					}
					else if (approve.equals("Import Genetic Circuit")) {
						if (!selectedFile.endsWith(".gcm")) {
							selectedFile += ".gcm";
						}
					}
					else if (approve.equals("Import")) {
						if (!selectedFile.endsWith(".csv") && !selectedFile.endsWith(".dat") && !selectedFile.endsWith(".tsd")) {
							selectedFile += ".tsd";
						}
					}
					if (File.separator.equals("\\")) {
						return fd.getDirectory() + "\\\\" + selectedFile;
					}
					else {
						return fd.getDirectory() + File.separator + selectedFile;
					}
				}
				else {
					return "";
				}
			}
			else if (fd.getDirectory() != null) {
				return ""; // fd.getDirectory();
			}
			else {
				return "";
			}
			/*
			 * String open; Display display = new Display(); Shell shell = new
			 * Shell(display); shell.setImage(new Image(display,
			 * System.getenv("BIOSIM") + separator + "gui" + separator + "icons"
			 * + separator + "iBioSim.png")); if (i ==
			 * JFileChooser.DIRECTORIES_ONLY) { DirectoryDialog dd = null; if
			 * (approve.equals("Save") || approve.equals("New")) { dd = new
			 * DirectoryDialog(shell, SWT.SAVE); dd.setText(approve); } else if
			 * (approve.equals("Open")) { dd = new DirectoryDialog(shell,
			 * SWT.OPEN); dd.setText(approve); } else { dd = new
			 * DirectoryDialog(shell); dd.setText(approve); } if (file != null)
			 * { dd.setFilterPath(file.getPath()); } open = dd.open(); } else {
			 * FileDialog fd = null; if (approve.equals("Save") ||
			 * approve.equals("New")) { fd = new FileDialog(shell, SWT.SAVE);
			 * fd.setText(approve); } else if (approve.equals("Open")) { fd =
			 * new FileDialog(shell, SWT.OPEN); fd.setText(approve); } else if
			 * (approve.equals("Export TSD")) { fd = new FileDialog(shell,
			 * SWT.SAVE); fd.setText(approve); fd.setFilterNames(new String[] {
			 * "Comma Separated Values (*.csv)", "Tab Delimited Data
			 * (*.dat)", "Encapsulated Postscript (*.eps)", "JPEG
			 * (*.jpg)", "Portable Document Format (*.pdf)", "Portable Network
			 * Graphics (*.png)", "Scalable Vector Graphics (*.svg)", "Time
			 * Series Data
			 * (*.tsd)" }); fd.setFilterExtensions(new String[] { "*.csv",
			 * "*.dat", "*.eps", "*.jpg", "*.pdf", "*.png", "*.svg", "*.tsd" });
			 * fd.setFilterIndex(4); } else if
			 * (approve.equals("Export Probability")) { fd = new
			 * FileDialog(shell, SWT.SAVE); fd.setText(approve);
			 * fd.setFilterNames(new String[] {
			 * "Encapsulated Postscript (*.eps)", "JPEG (*.jpg)",
			 * "Portable Document Format (*.pdf)", "Portable Network Graphics
			 * (*.png)", "Scalable Vector Graphics (*.svg)" });
			 * fd.setFilterExtensions(new String[] { "*.eps", "*.jpg", "*.pdf",
			 * "*.png", "*.svg" }); fd.setFilterIndex(2); } else if
			 * (approve.equals("Import SBML")) { fd = new FileDialog(shell,
			 * SWT.OPEN); fd.setText(approve); fd.setFilterNames(new String[] {
			 * "Systems Biology Markup Language (*.sbml)", "Extensible Markup
			 * Language (*.xml)" }); fd.setFilterExtensions(new String[] {
			 * "*.sbml", "*.xml" }); fd.setFilterIndex(1); } else if
			 * (approve.equals("Import Genetic Circuit")) { fd = new
			 * FileDialog(shell, SWT.OPEN); fd.setText(approve);
			 * fd.setFilterNames(new String[] { "Genetic Circuit Model (*.gcm)"
			 * }); fd.setFilterExtensions(new String[] { "*.gcm" }); } else if
			 * (approve.equals("Import")) { fd = new FileDialog(shell,
			 * SWT.OPEN); fd.setText(approve); fd.setFilterNames(new String[] {
			 * "Comma Separated Values (*.csv)", "Tab Delimited Data
			 * (*.dat)", "Time Series Data
			 * (*.tsd)" }); fd.setFilterExtensions(new String[] { "
			 * *.csv", "*.dat", "*.tsd" }); fd.setFilterIndex(2); } else { fd =
			 * new FileDialog(shell); fd.setText(approve); } if (file != null) {
			 * fd.setFilterPath(file.getParentFile().getPath()); } open =
			 * fd.open(); } shell.dispose(); display.dispose(); if (open !=
			 * null) { return open; } else { return ""; }
			 */
		}
		else {
			String filename = "";
			JFileChooser fc = new JFileChooser();

			/*
			 * This sets the default directory to the one where BioSim is
			 * executed
			 */
			/*
			 * String startDir = System.getProperty("user.dir"); File curDir =
			 * new File(startDir); fc.setCurrentDirectory(curDir);
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
			ExampleFileFilter sbolFilter = new ExampleFileFilter();
			sbolFilter.addExtension("sbol");
			sbolFilter.setDescription("Synthetic Biology Open Language");
			ExampleFileFilter sedmlFilter = new ExampleFileFilter();
			sedmlFilter.addExtension("xml");
			sedmlFilter.setDescription("Simulation Experiment Description");
			ExampleFileFilter sbmlFilter = new ExampleFileFilter();
			sbmlFilter.addExtension("sbml");
			sbmlFilter.setDescription("Systems Biology Markup Language");
			ExampleFileFilter mp4Filter = new ExampleFileFilter();
			mp4Filter.addExtension("mp4");
			mp4Filter.setDescription("Audio Visual Files");
			ExampleFileFilter aviFilter = new ExampleFileFilter();
			aviFilter.addExtension("avi");
			aviFilter.setDescription("Audio Visual Files");
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
				if (fileType == 5) {
					fc.setFileFilter(csvFilter);
				}
				if (fileType == 6) {
					fc.setFileFilter(datFilter);
				}
				if (fileType == 3) {
					fc.setFileFilter(epsFilter);
				}
				if (fileType == 0) {
					fc.setFileFilter(jpgFilter);
				}
				if (fileType == 2) {
					fc.setFileFilter(pdfFilter);
				}
				if (fileType == 1) {
					fc.setFileFilter(pngFilter);
				}
				if (fileType == 4) {
					fc.setFileFilter(svgFilter);
				}
				if (fileType == 7) {
					fc.setFileFilter(tsdFilter);
				}
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Export Probability")) {
				fc.addChoosableFileFilter(epsFilter);
				fc.addChoosableFileFilter(jpgFilter);
				fc.addChoosableFileFilter(pdfFilter);
				fc.addChoosableFileFilter(pngFilter);
				fc.addChoosableFileFilter(svgFilter);
				fc.setAcceptAllFileFilterUsed(false);
				if (fileType == 3) {
					fc.setFileFilter(epsFilter);
				}
				if (fileType == 0) {
					fc.setFileFilter(jpgFilter);
				}
				if (fileType == 2) {
					fc.setFileFilter(pdfFilter);
				}
				if (fileType == 1) {
					fc.setFileFilter(pngFilter);
				}
				if (fileType == 4) {
					fc.setFileFilter(svgFilter);
				}
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Import SBOL") || approve.equals("Export DNA Component")) {
				fc.addChoosableFileFilter(sbolFilter);
				fc.addChoosableFileFilter(xmlFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(xmlFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
				fc.addChoosableFileFilter(sedmlFilter);
				fc.addChoosableFileFilter(xmlFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(xmlFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Import SBML")) {
				fc.addChoosableFileFilter(sbmlFilter);
				fc.addChoosableFileFilter(xmlFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(xmlFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Export SBML")) {
				fc.addChoosableFileFilter(sbmlFilter);
				fc.addChoosableFileFilter(xmlFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(xmlFilter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Save MP4")) {
				fc.addChoosableFileFilter(mp4Filter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(mp4Filter);
				retValue = fc.showDialog(frame, approve);
			}
			else if (approve.equals("Save AVI")) {
				fc.addChoosableFileFilter(aviFilter);
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(aviFilter);
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
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".png"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".pdf"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".eps"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".svg"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".dat"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".tsd")) && !(filename.substring(
									(filename.length() - 4), filename.length()).equals(".csv")))) {
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
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".png"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".pdf"))
									&& !(filename.substring((filename.length() - 4), filename.length()).equals(".eps")) && !(filename.substring(
									(filename.length() - 4), filename.length()).equals(".svg")))) {
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
				else if (approve.equals("Export SBML")) {
					if ((filename.length() < 4)
							|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".xml")))) {
						filename += ".xml";
					}
				}
				else if (approve.equals("Save MP4")) {
					if ((filename.length() < 4)
							|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".mp4")))) {
						filename += ".mp4";
					}
				}
				else if (approve.equals("Save AVI")) {
					if ((filename.length() < 4)
							|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".avi")))) {
						filename += ".avi";
					}
				}
				else if (approve.equals("Import SBOL") || approve.equals("Export DNA Component")) {
					if (!filename.endsWith(".sbol") && !filename.endsWith(".xml"))
						filename += ".xml";
				}
				else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
					if (!filename.endsWith("-sedml.xml"))
						filename += "-sedml.xml";
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
	 * Removes the selected values of the given JList from the given list and
	 * updates the JList.
	 */
	public static void remove(JList currentList) {
		Object[] list = new Object[currentList.getModel().getSize()];
		for (int i = 0; i < currentList.getModel().getSize(); i++) {
			list[i] = currentList.getModel().getElementAt(i);
		}

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
	}

	/**
	 * Adds a new item to a JList
	 */
	public static void add(JList currentList, Object newItem) {
		Object[] list = new Object[currentList.getModel().getSize() + 1];
		int addAfter = currentList.getSelectedIndex();
		for (int i = 0; i <= currentList.getModel().getSize(); i++) {
			if (i <= addAfter) {
				list[i] = currentList.getModel().getElementAt(i);
			}
			else if (i == (addAfter + 1)) {
				list[i] = newItem;
			}
			else {
				list[i] = currentList.getModel().getElementAt(i - 1);
			}
		}
		currentList.setListData(list);
	}

	/**
	 * Adds the selected values in the add JList to the list JList. Stores all
	 * these values into the currentList array and returns this array.
	 */
	public static Object[] add(Object[] currentList, JList list, JList add, JRadioButton ge,
			JRadioButton gt, JRadioButton eq, JRadioButton lt, JRadioButton le, Component component) {
		int[] select = new int[currentList.length];
		for (int i = 0; i < currentList.length; i++) {
			select[i] = i;
		}
		list.setSelectedIndices(select);
		currentList = list.getSelectedValues();
		Object[] newSelected = add.getSelectedValues();
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
		sort(currentList);
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

	public static void sort(Object[] sort) {
		int i, j;
		String index;
		for (i = 1; i < sort.length; i++) {
			index = (String) sort[i];
			j = i;
			while ((j > 0) && ((String) sort[j - 1]).compareToIgnoreCase(index) > 0) {
				sort[j] = sort[j - 1];
				j = j - 1;
			}
			sort[j] = index;
		}
	}
}
