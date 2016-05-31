package learn.datamanager;

import lpn.parser.LhpnFile;
import main.*;
import main.util.*;
import main.util.dataparser.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import biomodel.parser.BioModel;
import biomodel.util.SBMLutilities;


public class DataManager extends JPanel implements ActionListener, TableModelListener, ListSelectionListener {

	private static final long serialVersionUID = -2669704247953218544L;

	private String directory, lrnName;

	private JList files;

	private JTable table;

	private JButton saveData, addData, removeData, copyData;

	private JButton add, remove, rename, copy, copyFromView, importFile;

	private Gui biosim;

	private String separator;

	private String[] species;

	private JScrollPane scroll1;

	private JPanel dataButtons;

	private JPanel dataPanel;

	private JPanel filesPanel;

	private boolean dirty;

	private String previous;

	private String[] list;

	public DataManager(String directory, Gui biosim) {
		separator = Gui.separator;
		
		this.biosim = biosim;
		this.directory = directory;
		this.lrnName = directory.split(separator)[directory.split(separator).length - 1];
		list = new String[0];
		previous = null;
		try {
			Properties p = new Properties();
			FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
			p.load(load);
			load.close();
			ArrayList<String> getValues = new ArrayList<String>();
			for (String key : p.keySet().toArray(new String[0])) {
				if (key.contains(".tsd")) {
					getValues.add(p.getProperty(key));
				}
			}
			String[] s = getValues.toArray(new String[0]);
			sort(s);
			files = new JList(s);
			list = s;
		}
		catch (Exception e) {
			files = new JList();
		}
		updateSpecies();
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(files);
		files.addListSelectionListener(this);
		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		rename = new JButton("Rename");
		rename.addActionListener(this);
		copy = new JButton("Copy");
		copy.addActionListener(this);
		copyFromView = new JButton("Copy From View");
		copyFromView.addActionListener(this);
		importFile = new JButton("Import");
		importFile.addActionListener(this);
		JPanel buttons = new JPanel(new BorderLayout());
		JPanel topButtons = new JPanel();
		JPanel bottomButtons = new JPanel();
		topButtons.add(add);
		topButtons.add(remove);
		topButtons.add(rename);
		bottomButtons.add(copy);
		bottomButtons.add(copyFromView);
		bottomButtons.add(importFile);
		buttons.add(topButtons, "North");
		buttons.add(bottomButtons, "South");
		table = new JTable();
		scroll1 = new JScrollPane();
		scroll1.setMinimumSize(new Dimension(260, 200));
		scroll1.setPreferredSize(new Dimension(276, 132));
		scroll1.setViewportView(table);
		saveData = new JButton("Save");
		saveData.addActionListener(this);
		addData = new JButton("Add Data Point");
		addData.addActionListener(this);
		removeData = new JButton("Remove Data Point");
		removeData.addActionListener(this);
		copyData = new JButton("Copy Data Point");
		copyData.addActionListener(this);
		dataButtons = new JPanel();
		dataButtons.add(saveData);
		dataButtons.add(addData);
		dataButtons.add(removeData);
		dataButtons.add(copyData);
		filesPanel = new JPanel(new BorderLayout());
		filesPanel.add(scroll, "Center");
		filesPanel.add(buttons, "South");
		dataPanel = new JPanel(new BorderLayout());
		dataPanel.add(scroll1, "Center");
		dataPanel.add(dataButtons, "South");
		this.setLayout(new BorderLayout());
		this.add(filesPanel, "West");
		this.add(dataPanel, "Center");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == add) {
			String dataFile = JOptionPane.showInputDialog(Gui.frame, "Enter Data File ID:", "Data File ID", JOptionPane.PLAIN_MESSAGE);
			if (dataFile != null && !dataFile.trim().equals("")) {
				dataFile = dataFile.trim();
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
					p.load(load);
					load.close();
					for (String s : p.keySet().toArray(new String[0])) {
						if (p.getProperty(s).equals(dataFile)) {
							JOptionPane.showMessageDialog(Gui.frame, "A file with that description already exists.", "Description Must Be Unique",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					int run = 0;
					String[] list = new File(directory).list();
					for (int i = 0; i < list.length; i++) {
						if (!(new File(directory + separator + list[i]).isDirectory()) && list[i].length() > 4) {
							String end = "";
							for (int j = 1; j < 5; j++) {
								end = list[i].charAt(list[i].length() - j) + end;
							}
							if (end.equals(".tsd")) {
								if (list[i].contains("run-")) {
									int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
									if (tempNum > run) {
										run = tempNum;
									}
								}
							}
						}
					}
					String end = "run-" + (run + 1) + ".tsd";
					ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
					ArrayList<String> specs = new ArrayList<String>();
					specs.add("time");
					data.add(new ArrayList<Double>());
					for (int i = 0; i < species.length; i++) {
						specs.add(species[i]);
						data.add(new ArrayList<Double>());
					}
					DataParser parser = new DataParser(specs, data);
					parser.outputTSD(directory + separator + end);
					p.setProperty(end, dataFile);
					FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
					p.store(store, "Learn File Data");
					store.close();
					ArrayList<String> getValues = new ArrayList<String>();
					for (String key : p.keySet().toArray(new String[0])) {
						if (key.contains(".tsd")) {
							getValues.add(p.getProperty(key));
						}
					}
					String[] s = getValues.toArray(new String[0]);
					sort(s);
					files.setListData(s);
					this.list = s;
					files.setSelectedValue(dataFile, true);
					biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to add new file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getSource() == remove) {
			if (files.getSelectedIndices().length > 0) {
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
					p.load(load);
					load.close();
					Object[] delete = files.getSelectedValuesList().toArray();
					for (Object file : delete) {
						int run = 0;
						String[] list = new File(directory).list();
						for (int i = 0; i < list.length; i++) {
							if (!(new File(directory + separator + list[i]).isDirectory()) && list[i].length() > 4) {
								String end = "";
								for (int j = 1; j < 5; j++) {
									end = list[i].charAt(list[i].length() - j) + end;
								}
								if (end.equals(".tsd")) {
									if (list[i].contains("run-")) {
										int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
										if (tempNum > run) {
											run = tempNum;
										}
									}
								}
							}
						}
						for (String s : p.keySet().toArray(new String[0])) {
							if (p.getProperty(s).equals(file)) {
								if (s.equals("run-" + run + ".tsd")) {
									new File(directory + separator + s).delete();
									p.remove(s);
								}
								else {
									new File(directory + separator + s).delete();
									new File(directory + separator + "run-" + run + ".tsd").renameTo(new File(directory + separator + s));
									p.setProperty(s, p.getProperty("run-" + run + ".tsd"));
									p.remove("run-" + run + ".tsd");
								}
								break;
							}
						}
					}
					FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
					p.store(store, "Learn File Data");
					store.close();
					ArrayList<String> getValues = new ArrayList<String>();
					for (String key : p.keySet().toArray(new String[0])) {
						if (key.contains(".tsd")) {
							getValues.add(p.getProperty(key));
						}
					}
					String[] s = getValues.toArray(new String[0]);
					sort(s);
					files.setListData(s);
					list = s;
					biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
					this.removeAll();
					table = new JTable();
					scroll1 = new JScrollPane();
					scroll1.setMinimumSize(new Dimension(260, 200));
					scroll1.setPreferredSize(new Dimension(276, 132));
					scroll1.setViewportView(table);
					scroll1.revalidate();
					scroll1.repaint();
					dataPanel = new JPanel(new BorderLayout());
					dataPanel.add(scroll1, "Center");
					dataPanel.add(dataButtons, "South");
					this.setLayout(new BorderLayout());
					this.add(filesPanel, "West");
					this.add(dataPanel, "Center");
					this.revalidate();
					this.repaint();
					dirty = false;
					biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to remove selected files.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getSource() == rename) {
			if (files.getSelectedIndices().length > 1) {
				JOptionPane.showMessageDialog(Gui.frame, "You can only select one file to rename at a time.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (files.getSelectedIndices().length < 1) {
				JOptionPane.showMessageDialog(Gui.frame, "You must select a file to rename.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				String rename = JOptionPane.showInputDialog(Gui.frame, "Please Enter New Name:", "Rename", JOptionPane.PLAIN_MESSAGE);
				if (rename != null && !rename.trim().equals("")) {
					rename = rename.trim();
				}
				else {
					return;
				}
				Properties p = new Properties();
				FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
				p.load(load);
				load.close();
				for (String s : list) {
					if (s.equals(rename)) {
						JOptionPane.showMessageDialog(Gui.frame, "A file with that description already exists.", "Description Must Be Unique",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				for (String s : p.keySet().toArray(new String[0])) {
					if (p.getProperty(s).equals(files.getSelectedValue())) {
						p.setProperty(s, rename);
					}
				}
				FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
				p.store(store, "Learn File Data");
				store.close();
				ArrayList<String> getValues = new ArrayList<String>();
				for (String key : p.keySet().toArray(new String[0])) {
					if (key.contains(".tsd")) {
						getValues.add(p.getProperty(key));
					}
				}
				String[] s = getValues.toArray(new String[0]);
				sort(s);
				files.setListData(s);
				list = s;
				previous = rename;
				files.setSelectedValue(rename, true);
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to rename selected file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == copy) {
			if (files.getSelectedIndices().length > 1) {
				JOptionPane.showMessageDialog(Gui.frame, "You can only select one file to copy at a time.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (files.getSelectedIndices().length < 1) {
				JOptionPane.showMessageDialog(Gui.frame, "You must select a file to copy.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				String copy = JOptionPane.showInputDialog(Gui.frame, "Please Enter New Name:", "Copy", JOptionPane.PLAIN_MESSAGE);
				if (copy != null && !copy.trim().equals("")) {
					copy = copy.trim();
				}
				else {
					return;
				}
				saveChanges(null);
				int run = 0;
				String[] list = new File(directory).list();
				for (int i = 0; i < list.length; i++) {
					if (!(new File(directory + separator + list[i]).isDirectory()) && list[i].length() > 4) {
						String end = "";
						for (int j = 1; j < 5; j++) {
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals(".tsd")) {
							if (list[i].contains("run-")) {
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
								if (tempNum > run) {
									run = tempNum;
								}
							}
						}
					}
				}
				Properties p = new Properties();
				FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
				p.load(load);
				load.close();
				for (String s : this.list) {
					if (s.equals(copy)) {
						JOptionPane.showMessageDialog(Gui.frame, "A file with that description already exists.", "Description Must Be Unique",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				for (String s : p.keySet().toArray(new String[0])) {
					if (p.getProperty(s).equals(files.getSelectedValue())) {
						String end = "run-" + (run + 1) + ".tsd";
						FileOutputStream out = new FileOutputStream(new File(directory + separator + end));
						FileInputStream in = new FileInputStream(new File(directory + separator + s));
						int read = in.read();
						while (read != -1) {
							out.write(read);
							read = in.read();
						}
						in.close();
						out.close();
						p.setProperty(end, copy);
					}
				}
				FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
				p.store(store, "Learn File Data");
				store.close();
				ArrayList<String> getValues = new ArrayList<String>();
				for (String key : p.keySet().toArray(new String[0])) {
					if (key.contains(".tsd")) {
						getValues.add(p.getProperty(key));
					}
				}
				String[] s = getValues.toArray(new String[0]);
				sort(s);
				files.setListData(s);
				this.list = s;
				previous = copy;
				files.setSelectedValue(copy, true);
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to copy selected file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == copyFromView) {
			String root = directory.substring(0, directory.length() - directory.split(separator)[directory.split(separator).length - 1].length());
			ArrayList<String> list = new ArrayList<String>();
			for (String s : new File(root).list()) {
				if (new File(root + separator + s).isDirectory() && !s.equals(directory.split(separator)[directory.split(separator).length - 1])) {
					boolean add = false;
					for (String ss : new File(root + separator + s).list()) {
						if (new File(root + separator + s + separator + ss).isDirectory()) {
							boolean add2 = false;
							for (String sss : new File(root + separator + s + separator + ss).list()) {
								if (sss.endsWith(".tsd") && sss.startsWith("run-")) {
									add2 = true;
								}
							}
							if (add2) {
								list.add(s + separator + ss);
							}
						}
						else if (ss.endsWith(".tsd") && ss.startsWith("run-")) {
							add = true;
						}
					}
					if (add) {
						list.add(s);
					}
				}
			}
			if (list.size() > 0) {
				String[] s = list.toArray(new String[0]);
				sort(s);
				JList sims = new JList(s);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(260, 200));
				scroll.setPreferredSize(new Dimension(276, 132));
				scroll.setViewportView(sims);
				sims.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				sims.setSelectedIndex(0);
				Object[] options = { "Select", "Cancel" };
				int value = JOptionPane.showOptionDialog(Gui.frame, scroll, "Select View", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
				if (value == JOptionPane.YES_OPTION) {
					saveChanges(null);
					int run = 0;
					String[] lists = new File(directory).list();
					for (int i = 0; i < lists.length; i++) {
						if (!(new File(directory + separator + lists[i]).isDirectory()) && lists[i].length() > 4) {
							String end = "";
							for (int j = 1; j < 5; j++) {
								end = lists[i].charAt(lists[i].length() - j) + end;
							}
							if (end.equals(".tsd")) {
								if (lists[i].contains("run-")) {
									int tempNum = Integer.parseInt(lists[i].substring(4, lists[i].length() - end.length()));
									if (tempNum > run) {
										run = tempNum;
									}
								}
							}
						}
					}
					String[] list1 = new File(root + separator + sims.getSelectedValue()).list();
					Properties p = new Properties();
					try {
						FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
						p.load(load);
						load.close();
					}
					catch (IOException e1) {
					}
					for (int i = 0; i < list1.length; i++) {
						if (!(new File(root + separator + sims.getSelectedValue() + separator + list1[i]).isDirectory()) && list1[i].length() > 4) {
							String end = "";
							for (int j = 1; j < 5; j++) {
								end = list1[i].charAt(list1[i].length() - j) + end;
							}
							if (end.equals(".tsd") && list1[i].startsWith("run-")) {
								try {
									String last = "run-" + (run + 1) + ".tsd";
									TSDParser tsd = new TSDParser(root + separator + sims.getSelectedValue() + separator + list1[i], false);
									ArrayList<String> specs = tsd.getSpecies();
									ArrayList<ArrayList<Double>> data = tsd.getData();
									int a, b;
									String index;
									ArrayList<Double> index2;
									for (a = 2; a < specs.size(); a++) {
										index = specs.get(a);
										index2 = data.get(a);
										b = a;
										while ((b > 1) && specs.get(b - 1).compareToIgnoreCase(index) > 0) {
											specs.set(b, specs.get(b - 1));
											data.set(b, data.get(b - 1));
											b = b - 1;
										}
										specs.set(b, index);
										data.set(b, index2);
									}
									tsd.setSpecies(specs);
									tsd.setData(data);
									tsd.outputTSD(directory + separator + last);
									p.setProperty(last, sims.getSelectedValue() + separator + list1[i]);
									run++;
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(Gui.frame, "Unable to copy from view.", "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					}
					try {
						ArrayList<String> getValues = new ArrayList<String>();
						for (String key : p.keySet().toArray(new String[0])) {
							if (key.contains(".tsd")) {
								getValues.add(p.getProperty(key));
							}
						}
						String[] ss = getValues.toArray(new String[0]);
						sort(ss);
						files.setListData(ss);
						this.list = ss;
						this.removeAll();
						table = new JTable();
						scroll1 = new JScrollPane();
						scroll1.setMinimumSize(new Dimension(260, 200));
						scroll1.setPreferredSize(new Dimension(276, 132));
						scroll1.setViewportView(table);
						scroll1.revalidate();
						scroll1.repaint();
						dataPanel = new JPanel(new BorderLayout());
						dataPanel.add(scroll1, "Center");
						dataPanel.add(dataButtons, "South");
						this.setLayout(new BorderLayout());
						this.add(filesPanel, "West");
						this.add(dataPanel, "Center");
						this.revalidate();
						this.repaint();
						dirty = false;
						FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
						p.store(store, "Learn File Data");
						store.close();
						if (ss.length > 0) {
							biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
						}
					}
					catch (IOException e1) {
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "There are no views to copy data from.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == importFile) {
			File file;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
				file = null;
			}
			else {
				file = new File(biosimrc.get("biosim.general.import_dir", ""));
			}
			String importFile = Utility.browse(Gui.frame, file, null, JFileChooser.FILES_AND_DIRECTORIES, "Import", -1);
			if (importFile != null && !importFile.trim().equals("")) {
				saveChanges(null);
				importFile = importFile.trim();
				biosimrc.put("biosim.general.import_dir", importFile);
				int run = 0;
				String[] list = new File(directory).list();
				for (int i = 0; i < list.length; i++) {
					if (!(new File(directory + separator + list[i]).isDirectory()) && list[i].length() > 4) {
						String end = "";
						for (int j = 1; j < 5; j++) {
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals(".tsd")) {
							if (list[i].contains("run-")) {
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
								if (tempNum > run) {
									run = tempNum;
								}
							}
						}
					}
				}
				if (new File(importFile).isDirectory()) {
					String[] list1 = new File(importFile).list();
					Properties p = new Properties();
					try {
						FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
						p.load(load);
						load.close();
					}
					catch (IOException e1) {
					}
					for (int i = 0; i < list1.length; i++) {
						if (!(new File(importFile + separator + list1[i]).isDirectory()) && list1[i].length() > 4) {
							String end = "";
							for (int j = 1; j < 5; j++) {
								end = list1[i].charAt(list1[i].length() - j) + end;
							}
							if (end.equals(".tsd")) {
								// try {
								String last = "run-" + (run + 1) + ".tsd";
								TSDParser tsd = new TSDParser(importFile + separator + list1[i], false);
								ArrayList<String> specs = tsd.getSpecies();
								ArrayList<ArrayList<Double>> data = tsd.getData();
								int a, b;
								String index;
								ArrayList<Double> index2;
								for (a = 2; a < specs.size(); a++) {
									index = specs.get(a);
									index2 = data.get(a);
									b = a;
									while ((b > 1) && specs.get(b - 1).compareToIgnoreCase(index) > 0) {
										specs.set(b, specs.get(b - 1));
										data.set(b, data.get(b - 1));
										b = b - 1;
									}
									specs.set(b, index);
									data.set(b, index2);
								}
								tsd.setSpecies(specs);
								tsd.setData(data);
								tsd.outputTSD(directory + separator + last);
								p.setProperty(last, importFile + separator + list1[i]);
								run++;
								// }
								// catch (IOException e1) {
								// JOptionPane.showMessageDialog(BioSim.frame,
								// "Unable to import file.", "Error",
								// JOptionPane.ERROR_MESSAGE);
								// }
							}
						}
					}
					try {
						ArrayList<String> getValues = new ArrayList<String>();
						for (String key : p.keySet().toArray(new String[0])) {
							if (key.contains(".tsd")) {
								getValues.add(p.getProperty(key));
							}
						}
						String[] s = getValues.toArray(new String[0]);
						sort(s);
						files.setListData(s);
						this.list = s;
						this.removeAll();
						table = new JTable();
						scroll1 = new JScrollPane();
						scroll1.setMinimumSize(new Dimension(260, 200));
						scroll1.setPreferredSize(new Dimension(276, 132));
						scroll1.setViewportView(table);
						scroll1.revalidate();
						scroll1.repaint();
						dataPanel = new JPanel(new BorderLayout());
						dataPanel.add(scroll1, "Center");
						dataPanel.add(dataButtons, "South");
						this.setLayout(new BorderLayout());
						this.add(filesPanel, "West");
						this.add(dataPanel, "Center");
						this.revalidate();
						this.repaint();
						dirty = false;
						FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
						p.store(store, "Learn File Data");
						store.close();
						if (s.length > 0) {
							biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
						}
					}
					catch (IOException e1) {
					}
				}
				else {
					if (importFile.length() > 3
							&& (importFile.substring(importFile.length() - 4, importFile.length()).equals(".tsd")
									|| importFile.substring(importFile.length() - 4, importFile.length()).equals(".csv") || importFile.substring(
									importFile.length() - 4, importFile.length()).equals(".dat"))) {
						try {
							String end = "run-" + (run + 1) + ".tsd";
							DataParser parse;
							if (importFile.substring(importFile.length() - 4, importFile.length()).equals(".csv")) {
								parse = new CSVParser(importFile, false);
							}
							else if (importFile.substring(importFile.length() - 4, importFile.length()).equals(".dat")) {
								parse = new DATParser(importFile, false);
							}
							else {
								parse = new TSDParser(importFile, false);
							}
							ArrayList<String> specs = parse.getSpecies();
							ArrayList<ArrayList<Double>> data = parse.getData();
							if (specs.size() > 0) {
								int a, b;
								String index;
								ArrayList<Double> index2;
								for (a = 2; a < specs.size(); a++) {
									index = specs.get(a);
									index2 = data.get(a);
									b = a;
									while ((b > 1) && specs.get(b - 1).compareToIgnoreCase(index) > 0) {
										specs.set(b, specs.get(b - 1));
										data.set(b, data.get(b - 1));
										b = b - 1;
									}
									specs.set(b, index);
									data.set(b, index2);
								}
								parse.setSpecies(specs);
								parse.setData(data);
								parse.outputTSD(directory + separator + end);
								// ADDED BY SB. DIRTY WAY
								TSDParser extractVars;
								ArrayList<String> datFileVars = new ArrayList<String>();
								ArrayList<String> allVars = new ArrayList<String>();
								Boolean varPresent = false;
								// Finding the intersection of all the variables
								// present in all data files.
								for (int i = 1; (new File(directory + separator + "run-" + i + ".tsd")).exists(); i++) {
									extractVars = new TSDParser(directory + separator + "run-" + i + ".tsd", false);
									datFileVars = extractVars.getSpecies();
									if (i == 1) {
										allVars.addAll(datFileVars);
									}
									for (String s : allVars) {
										varPresent = false;
										for (String t : datFileVars) {
											if (s.equalsIgnoreCase(t)) {
												varPresent = true;
												break;
											}
										}
										if (!varPresent) {
											allVars.remove(s);
										}
									}
								}
								species = allVars.toArray(new String[0]);
								// ADDED END SB
								Properties p = new Properties();
								FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
								p.load(load);
								load.close();
								p.setProperty(end, importFile);
								FileOutputStream store = new FileOutputStream(new File(directory + separator + lrnName + ".lrn"));
								p.store(store, "Learn File Data");
								store.close();
								ArrayList<String> getValues = new ArrayList<String>();
								for (String key : p.keySet().toArray(new String[0])) {
									if (key.contains(".tsd")) {
										getValues.add(p.getProperty(key));
									}
								}
								String[] s = getValues.toArray(new String[0]);
								sort(s);
								files.setListData(s);
								this.list = s;
								previous = "";
								files.setSelectedValue(importFile, true);
								if (s.length > 0) {
									biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
								}
							}
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(Gui.frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
						}
						catch (ArrayIndexOutOfBoundsException e1) {
						}
					}
					else {
						JOptionPane.showMessageDialog(Gui.frame, "Unable to import file." + "\nImported file must be a tsd, csv, or dat file.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else if (e.getSource() == saveData) {
			if (files.getSelectedIndex() != -1) {
				TableModel m = table.getModel();
				String[][] sort = sortData(m);
				ArrayList<String> species = new ArrayList<String>();
				ArrayList<ArrayList<Double>> data = convertData(sort);
				for (int i = 0; i < m.getColumnCount(); i++) {
					species.add(m.getColumnName(i));
				}
				DataParser parser = new DataParser(species, data);
				try {
					String file = (String) files.getSelectedValue();
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
					p.load(load);
					load.close();
					for (String s : p.keySet().toArray(new String[0])) {
						if (p.getProperty(s).equals(file)) {
							parser.outputTSD(directory + separator + s);
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to save file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
			}
			dirty = false;
		}
		else if (e.getSource() == addData) {
			if (files.getSelectedIndex() != -1) {
				TableModel m = table.getModel();
				String[][] sort = sortData(m);
				String[][] dat = new String[m.getRowCount() + 1][m.getColumnCount()];
				for (int i = 0; i < m.getColumnCount(); i++) {
					for (int j = 0; j < m.getRowCount(); j++) {
						dat[j + 1][i] = sort[j][i];
					}
				}
				for (int i = 0; i < m.getColumnCount(); i++) {
					dat[0][i] = "0.0";
				}
				String[] spec = new String[m.getColumnCount()];
				for (int i = 0; i < m.getColumnCount(); i++) {
					spec[i] = m.getColumnName(i);
				}
				createTable(dat, spec);
				table.addRowSelectionInterval(0, 0);
			}
			dirty = true;
		}
		else if (e.getSource() == removeData) {
			int removeRow = table.getSelectedRow();
			if (removeRow != -1) {
				boolean removed = false;
				TableModel m = table.getModel();
				String[][] sort = sortData(m);
				String[][] dat = new String[m.getRowCount() - 1][m.getColumnCount()];
				for (int j = 0; j < m.getRowCount() - 1; j++) {
					for (int i = 0; i < m.getColumnCount(); i++) {
						if (j == removeRow) {
							removed = true;
						}
						if (removed) {
							dat[j][i] = sort[j + 1][i];
						}
						else {
							dat[j][i] = sort[j][i];
						}
					}
				}
				String[] spec = new String[m.getColumnCount()];
				for (int i = 0; i < m.getColumnCount(); i++) {
					spec[i] = m.getColumnName(i);
				}
				createTable(dat, spec);
				if (table.getRowCount() != 0) {
					if (removeRow > table.getRowCount() - 1) {
						table.addRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
					}
					else {
						table.addRowSelectionInterval(removeRow, removeRow);
					}
				}
			}
			dirty = true;
		}
		else if (e.getSource() == copyData) {
			int copyRow = table.getSelectedRow();
			if (copyRow != -1) {
				TableModel m = table.getModel();
				String[][] sort = sortData(m);
				String[][] dat = new String[m.getRowCount() + 1][m.getColumnCount()];
				for (int i = 0; i < m.getColumnCount(); i++) {
					for (int j = 0; j < m.getRowCount(); j++) {
						dat[j + 1][i] = sort[j][i];
					}
				}
				for (int i = 0; i < m.getColumnCount(); i++) {
					dat[0][i] = sort[copyRow][i];
				}
				String[] spec = new String[m.getColumnCount()];
				for (int i = 0; i < m.getColumnCount(); i++) {
					spec[i] = m.getColumnName(i);
				}
				createTable(dat, spec);
				table.addRowSelectionInterval(copyRow, copyRow);
			}
			dirty = true;
		}
	}

	public void mouseClicked() {
	}

	private static void sort(String[] sort) {
		int i, j;
		String index;
		for (i = 1; i < sort.length; i++) {
			index = sort[i];
			j = i;
			while ((j > 0) && sort[j - 1].compareToIgnoreCase(index) > 0) {
				sort[j] = sort[j - 1];
				j = j - 1;
			}
			sort[j] = index;
		}
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		this.lrnName = directory.split(separator)[directory.split(separator).length - 1];
	}

	private void createTable(String[][] dat, String[] spec) {
		this.removeAll();
		table = new JTable(dat, spec);
		TableSorter sorter = new TableSorter(table.getModel(), species);
		/*
		 * TableRowSorter<TableModel> sorter = new
		 * TableRowSorter<TableModel>(table.getModel()); Comparator<String>
		 * comparator = new Comparator<String>() { public int compare(String s1,
		 * String s2) { double d1 = Double.parseDouble(s1); double d2 =
		 * Double.parseDouble(s2); if (d1 > d2) { return 1; } else if (d2 > d1)
		 * { return -1; } else { return 0; } } }; sorter.setComparator(0,
		 * comparator); sorter.setSortsOnUpdates(true); for (int i = 0; i <
		 * spec.length; i++) { sorter.setSortable(i, false); }
		 * ArrayList<RowSorter.SortKey> sortKeys = new
		 * ArrayList<RowSorter.SortKey>(); sortKeys.add(new RowSorter.SortKey(0,
		 * SortOrder.ASCENDING)); sorter.setSortKeys(sortKeys);
		 * table.setRowSorter(sorter);
		 */
		table = new JTable(sorter);
		sorter.setTable(table);
		ArrayList<String> specs = new ArrayList<String>();
		for (String s : species) {
			specs.add(s);
		}
		for (int i = table.getModel().getColumnCount() - 1; i > 0; i--) {
			if (!specs.contains(table.getModel().getColumnName(i))) {
				table.removeColumn(table.getColumnModel().getColumn(i));
			}
		}
		table.getModel().addTableModelListener(this);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scroll1 = new JScrollPane();
		scroll1.setMinimumSize(new Dimension(260, 200));
		scroll1.setPreferredSize(new Dimension(276, 132));
		scroll1.setViewportView(table);
		scroll1.revalidate();
		scroll1.repaint();
		dataPanel = new JPanel(new BorderLayout());
		dataPanel.add(scroll1, "Center");
		dataPanel.add(dataButtons, "South");
		this.setLayout(new BorderLayout());
		this.add(filesPanel, "West");
		this.add(dataPanel, "Center");
		this.revalidate();
		this.repaint();
	}

	private static String[][] sortData(TableModel m) {
		String[][] dat = new String[m.getRowCount()][m.getColumnCount()];
		for (int i = 0; i < m.getColumnCount(); i++) {
			for (int j = 0; j < m.getRowCount(); j++) {
				dat[j][i] = (String) m.getValueAt(j, i);
			}
		}
		double[] d = new double[m.getRowCount()];
		for (int i = 0; i < m.getRowCount(); i++) {
			try {
				d[i] = Double.parseDouble(dat[i][0]);
			}
			catch (Exception e) {
				d[i] = 0;
			}
		}
		int i, j;
		double index;
		String[] index2;
		for (i = 1; i < d.length; i++) {
			index = d[i];
			index2 = dat[i];
			j = i;
			while ((j > 0) && d[j - 1] > index) {
				d[j] = d[j - 1];
				dat[j] = dat[j - 1];
				j = j - 1;
			}
			d[j] = index;
			dat[j] = index2;
		}
		return dat;
	}

	private ArrayList<ArrayList<Double>> convertData(String[][] data) {
		boolean warning = false;
		ArrayList<ArrayList<Double>> newData = new ArrayList<ArrayList<Double>>();
		try {
			for (int i = 0; i < data[0].length; i++) {
				newData.add(new ArrayList<Double>());
				for (int j = 0; j < data.length; j++) {
					try {
						newData.get(i).add(Double.parseDouble(data[j][i]));
					}
					catch (Exception e) {
						if (data[j][i].equals("inf")) {
							newData.get(i).add(Double.MAX_VALUE);
						}
						else {
							newData.get(i).add(0.0);
							if (!warning) {
								JOptionPane.showMessageDialog(Gui.frame, "Data entered into the data manager must be real numbers."
										+ "\nReplacing invalid " + "entries with 0.0.", "Invalid Entries In Data", JOptionPane.WARNING_MESSAGE);
								warning = true;
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			for (int i = 0; i < species.length; i++) {
				newData.add(new ArrayList<Double>());
			}
		}
		return newData;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		dirty = true;
	}

	public void saveChanges(String descriptor) {
		if (previous != null) {
			if (dirty) {
				Object[] options = { "Yes", "No" };
				int value;
				if (descriptor == null) {
					value = JOptionPane.showOptionDialog(Gui.frame, "Do you want to save" + " changes to the current file?", "Save",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				}
				else {
					value = JOptionPane.showOptionDialog(Gui.frame, "Do you want to save" + " changes to the current file for " + descriptor + "?",
							"Save", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				}
				if (value == JOptionPane.YES_OPTION) {
					TableModel m = table.getModel();
					String[][] sort = sortData(m);
					ArrayList<String> species = new ArrayList<String>();
					ArrayList<ArrayList<Double>> data = convertData(sort);
					for (int i = 0; i < m.getColumnCount(); i++) {
						species.add(m.getColumnName(i));
					}
					DataParser parser = new DataParser(species, data);
					try {
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
						p.load(load);
						load.close();
						for (String s : p.keySet().toArray(new String[0])) {
							if (p.getProperty(s).equals(previous)) {
								parser.outputTSD(directory + separator + s);
							}
						}
					}
					catch (IOException e1) {
						JOptionPane.showMessageDialog(Gui.frame, "Unable to save file.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					biosim.refreshLearn(directory.split(separator)[directory.split(separator).length - 1]);
				}
				dirty = false;
			}
		}
	}

	public void updateSpecies() {
		String background;
		try {
			Properties p = new Properties();
			FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
			p.load(load);
			load.close();
			if (p.containsKey("genenet.file")) {
				String[] getProp = p.getProperty("genenet.file").split(separator);
				background = directory.substring(0, directory.length() - lrnName.length()) + separator + getProp[getProp.length - 1];
			}
			else if (p.containsKey("learn.file")) {
				String[] getProp = p.getProperty("learn.file").split(separator);
				background = directory.substring(0, directory.length() - lrnName.length()) + separator + getProp[getProp.length - 1];
			}
			else {
				background = null;
			}
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
			background = null;
		}
		species = null;
		if (background != null) {
			if (background.contains(".gcm")) {
				ArrayList<String> getSpecies = new ArrayList<String>();
				BioModel gcm = new BioModel(biosim.getRoot());
				gcm.load(background);
				getSpecies = gcm.getSpecies();
				species = getSpecies.toArray(new String[0]);
			}
			else if (background.contains(".lpn")) {
				ArrayList<String> getSpecies = new ArrayList<String>();
				LhpnFile lhpn = new LhpnFile(biosim.log);
				// System.out.println(background);
				lhpn.load(background);
				HashMap<String, Properties> speciesMap = lhpn.getContinuous();
				for (String s : speciesMap.keySet()) {
					getSpecies.add(s);
				}
				// species = getSpecies.toArray(new String[0]); // COMMENTED SB
				// ADDED BY SB.
				TSDParser extractVars;
				ArrayList<String> datFileVars = new ArrayList<String>();
				ArrayList<String> allVars = new ArrayList<String>();
				Boolean varPresent = false;
				// Finding the intersection of all the variables present in all
				// data files.
				for (int i = 1; (new File(directory + separator + "run-" + i + ".tsd")).exists(); i++) {
					extractVars = new TSDParser(directory + separator + "run-" + i + ".tsd", false);
					datFileVars = extractVars.getSpecies();
					if (i == 1) {
						allVars.addAll(datFileVars);
					}
					for (String s : allVars) {
						varPresent = false;
						for (String t : datFileVars) {
							if (s.equalsIgnoreCase(t)) {
								varPresent = true;
								break;
							}
						}
						if (!varPresent) {
							allVars.remove(s);
						}
					}
				}
				species = allVars.toArray(new String[0]);
				// END ADDED BY SB.
			}
			else {
				SBMLDocument document = SBMLutilities.readSBML(background);
				Model model = document.getModel();
				ArrayList<String> getSpecies = new ArrayList<String>();
				for (int i = 0; i < model.getSpeciesCount(); i++) {
					if (BioModel.isPromoterSpecies(model.getSpecies(i))) continue;
					getSpecies.add(model.getSpecies(i).getId());
				}
				for (int i = 0; i < model.getParameterCount(); i++) {
					getSpecies.add(model.getParameter(i).getId());
				}
				species = getSpecies.toArray(new String[0]);
			}
		}
		if (species != null) {
			sort(species);
		}
		if (files.getSelectedValue() != null) {
			try {
				TableModel m = table.getModel();
				String[][] sort = sortData(m);
				ArrayList<String> specs = new ArrayList<String>();
				ArrayList<ArrayList<Double>> data = convertData(sort);
				for (int i = 0; i < m.getColumnCount(); i++) {
					specs.add(m.getColumnName(i));
				}
				for (String sp : species) {
					if (!specs.contains(sp)) {
						specs.add(sp);
						ArrayList<Double> dat = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							dat.add(0.0);
						}
						data.add(dat);
					}
				}
				int a, b;
				String index;
				ArrayList<Double> index2;
				for (a = 2; a < specs.size(); a++) {
					index = specs.get(a);
					index2 = data.get(a);
					b = a;
					while ((b > 1) && specs.get(b - 1).compareToIgnoreCase(index) > 0) {
						specs.set(b, specs.get(b - 1));
						data.set(b, data.get(b - 1));
						b = b - 1;
					}
					specs.set(b, index);
					data.set(b, index2);
				}
				String[] spec = specs.toArray(new String[0]);
				String[][] dat = new String[data.get(0).size()][data.size()];
				for (int i = 0; i < data.size(); i++) {
					for (int j = 0; j < data.get(i).size(); j++) {
						dat[j][i] = "" + data.get(i).get(j);
					}
				}
				createTable(dat, spec);
			}
			catch (Exception e) {
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (files.getSelectedValue() != null) {
			String file = (String) files.getSelectedValue();
			boolean check = false;
			try {
				check = file.equals(previous);
			}
			catch (Exception e1) {
				check = false;
			}
			if (!check) {
				try {
					saveChanges(null);
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(directory + separator + lrnName + ".lrn"));
					p.load(load);
					load.close();
					for (String s : p.keySet().toArray(new String[0])) {
						if (p.getProperty(s).equals(file)) {
							TSDParser tsd = new TSDParser(directory + separator + s, false);
							ArrayList<String> specs = tsd.getSpecies();
							ArrayList<ArrayList<Double>> data = tsd.getData();
							for (String sp : species) {
								if (!specs.contains(sp)) {
									specs.add(sp);
									ArrayList<Double> dat = new ArrayList<Double>();
									for (int i = 0; i < data.get(0).size(); i++) {
										dat.add(0.0);
									}
									data.add(dat);
								}
							}
							int a, b;
							String index;
							ArrayList<Double> index2;
							for (a = 2; a < specs.size(); a++) {
								index = specs.get(a);
								index2 = data.get(a);
								b = a;
								while ((b > 1) && specs.get(b - 1).compareToIgnoreCase(index) > 0) {
									specs.set(b, specs.get(b - 1));
									data.set(b, data.get(b - 1));
									b = b - 1;
								}
								specs.set(b, index);
								data.set(b, index2);
							}
							String[] spec = specs.toArray(new String[0]);
							String[][] dat = new String[data.get(0).size()][data.size()];
							for (int i = 0; i < data.size(); i++) {
								for (int j = 0; j < data.get(i).size(); j++) {
									dat[j][i] = "" + data.get(i).get(j);
								}
							}
							createTable(dat, spec);
						}
					}
					previous = file;
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to display file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}

class TableSorter extends TableMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6557335239755106286L;

	int indexes[] = new int[0];

	Vector<Integer> sortingColumns = new Vector<Integer>();

	boolean ascending = true;

	String[] species;

	JTable table;

	public TableSorter() {
	}

	public TableSorter(TableModel model, String[] species) {
		setModel(model);
		this.species = species;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		reallocateIndexes();
		sortByColumn(0);
		fireTableDataChanged();
	}

	public int compareRowsByColumn(int row1, int row2, int column) {
		Class type = model.getColumnClass(column);
		TableModel data = model;

		// Check for nulls

		Object o1 = data.getValueAt(row1, column);
		Object o2 = data.getValueAt(row2, column);

		// If both values are null return 0
		if (o1 == null && o2 == null) {
			return 0;
		}
		else if (o1 == null) { // Define null less than everything.
			return -1;
		}
		else if (o2 == null) {
			return 1;
		}

		if (type.getSuperclass() == Number.class) {
			Number n1 = (Number) data.getValueAt(row1, column);
			double d1 = n1.doubleValue();
			Number n2 = (Number) data.getValueAt(row2, column);
			double d2 = n2.doubleValue();

			if (d1 < d2)
				return -1;
			else if (d1 > d2)
				return 1;
			else
				return 0;
		}
		else if (type == String.class) {
			String s1 = (String) data.getValueAt(row1, column);
			String s2 = (String) data.getValueAt(row2, column);
			int result = s1.compareTo(s2);

			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			else
				return 0;
		}
		else if (type == java.util.Date.class) {
			Date d1 = (Date) data.getValueAt(row1, column);
			long n1 = d1.getTime();
			Date d2 = (Date) data.getValueAt(row2, column);
			long n2 = d2.getTime();

			if (n1 < n2)
				return -1;
			else if (n1 > n2)
				return 1;
			else
				return 0;
		}
		else if (type == Boolean.class) {
			Boolean bool1 = (Boolean) data.getValueAt(row1, column);
			boolean b1 = bool1.booleanValue();
			Boolean bool2 = (Boolean) data.getValueAt(row2, column);
			boolean b2 = bool2.booleanValue();

			if (b1 == b2)
				return 0;
			else if (b1) // Define false < true
				return 1;
			else
				return -1;
		}
		else {
			Object v1 = data.getValueAt(row1, column);
			String s1 = v1.toString();
			Object v2 = data.getValueAt(row2, column);
			String s2 = v2.toString();
			double d1 = 0;
			double d2 = 0;
			try {
				d1 = Double.parseDouble(s1);
			}
			catch (Exception e) {
				if (s1.equals("inf")) {
					d1 = Double.MAX_VALUE;
				}
				else {
					d1 = 0;
				}
			}
			try {
				d2 = Double.parseDouble(s2);
			}
			catch (Exception e) {
				if (s2.equals("inf")) {
					d2 = Double.MAX_VALUE;
				}
				else {
					d2 = 0;
				}
			}
			if (d1 > d2) {
				return 1;
			}
			else if (d2 > d1) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}

	public int compare(int row1, int row2) {
		for (int level = 0, n = sortingColumns.size(); level < n; level++) {
			Integer column = sortingColumns.elementAt(level);
			int result = compareRowsByColumn(row1, row2, column.intValue());
			if (result != 0) {
				return (ascending ? result : -result);
			}
		}
		return 0;
	}

	public void reallocateIndexes() {
		int rowCount = model.getRowCount();
		indexes = new int[rowCount];
		for (int row = 0; row < rowCount; row++) {
			indexes[row] = row;
		}
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent) {
		super.tableChanged(tableModelEvent);
		reallocateIndexes();
		sortByColumn(0);
		fireTableStructureChanged();
		ArrayList<String> specs = new ArrayList<String>();
		for (String s : species) {
			specs.add(s);
		}
		for (int i = model.getColumnCount() - 1; i > 0; i--) {
			if (!specs.contains(model.getColumnName(i))) {
				table.removeColumn(table.getColumnModel().getColumn(i));
			}
		}
	}

	public void checkModel() {
		if (indexes.length != model.getRowCount()) {
			System.err.println("Sorter not informed of a change in model.");
		}
	}

	public void sort() {
		checkModel();
		shuttlesort(indexes.clone(), indexes, 0, indexes.length);
		fireTableDataChanged();
	}

	public void shuttlesort(int from[], int to[], int low, int high) {
		if (high - low < 2) {
			return;
		}
		int middle = (low + high) / 2;
		shuttlesort(to, from, low, middle);
		shuttlesort(to, from, middle, high);

		int p = low;
		int q = middle;

		for (int i = low; i < high; i++) {
			if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
				to[i] = from[p++];
			}
			else {
				to[i] = from[q++];
			}
		}
	}

	@Override
	public Object getValueAt(int row, int column) {
		checkModel();
		return model.getValueAt(indexes[row], column);
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		checkModel();
		model.setValueAt(aValue, indexes[row], column);
	}

	public void sortByColumn(int column) {
		sortByColumn(column, true);
	}

	public void sortByColumn(int column, boolean ascending) {
		this.ascending = ascending;
		sortingColumns.removeAllElements();
		sortingColumns.addElement(new Integer(column));
		sort();
		super.tableChanged(new TableModelEvent(this));
	}
}

class TableHeaderSorter extends MouseAdapter {

	private TableSorter sorter;

	private JTable table;

	private TableHeaderSorter() {
	}

	public static void install(TableSorter sorter, JTable table) {
		TableHeaderSorter tableHeaderSorter = new TableHeaderSorter();
		tableHeaderSorter.sorter = sorter;
		tableHeaderSorter.table = table;
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.addMouseListener(tableHeaderSorter);
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		TableColumnModel columnModel = table.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
		int column = table.convertColumnIndexToModel(viewColumn);
		if (mouseEvent.getClickCount() == 1 && column != -1) {
			System.out.println("Sorting ...");
			int shiftPressed = (mouseEvent.getModifiers() & InputEvent.SHIFT_MASK);
			boolean ascending = (shiftPressed == 0);
			sorter.sortByColumn(column, ascending);
		}
	}
}

class TableMap extends AbstractTableModel implements TableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5121573144973091171L;

	TableModel model;

	public TableModel getModel() {
		return model;
	}

	public void setModel(TableModel model) {
		if (this.model != null) {
			this.model.removeTableModelListener(this);
		}
		this.model = model;
		if (this.model != null) {
			this.model.addTableModelListener(this);
		}
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return model.getColumnClass(column);
	}

	@Override
	public int getColumnCount() {
		return ((model == null) ? 0 : model.getColumnCount());
	}

	@Override
	public String getColumnName(int column) {
		return model.getColumnName(column);
	}

	@Override
	public int getRowCount() {
		return ((model == null) ? 0 : model.getRowCount());
	}

	@Override
	public Object getValueAt(int row, int column) {
		return model.getValueAt(row, column);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		model.setValueAt(value, row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return model.isCellEditable(row, column);
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent) {
		fireTableChanged(tableModelEvent);
	}
}
