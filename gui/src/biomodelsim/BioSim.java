package biomodelsim.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import org.sbml.libsbml.*;
import reb2sac.core.gui.*;
import learn.core.gui.*;
import sbmleditor.core.gui.*;
import graph.core.gui.*;
import buttons.core.gui.*;

/**
 * This class creates a GUI for the Tstubd program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * are selected.
 * 
 * @author Curtis Madsen
 */
public class BioSim implements MouseListener, ActionListener {

	private JFrame frame; // Frame where components of the GUI are displayed

	private JMenuItem newTstubd; // The new menu item

	private JMenuItem newModel; // The new menu item

	private JMenuItem exit; // The exit menu item

	private JMenuItem importSbml; // The import sbml menu item

	private JMenuItem importDot; // The import dot menu item

	private JMenuItem manual; // The manual menu item

	private JMenuItem about; // The about menu item

	private JMenuItem openProj; // The open menu item

	private JMenuItem graph; // The graph menu item

	private String root; // The root directory

	private FileTree tree; // FileTree

	private JTabbedPane tab; // JTabbedPane for different tools

	private JPanel mainPanel; // the main panel

	private Log log; // the log

	private JPopupMenu popup; // popup menu

	/**
	 * This is the constructor for the Tstubd class. It initializes all the
	 * input fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 * 
	 * @throws Exception
	 */
	public BioSim() {
		// Creates a new frame
		frame = new JFrame("BioSim");

		// Makes it so that clicking the x in the corner closes the program
		WindowListener w = new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				exit.doClick();
			}

			public void windowOpened(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}
		};
		frame.addWindowListener(w);

		popup = new JPopupMenu();

		// Creates a menu for the frame
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		JMenu importMenu = new JMenu("Import");
		JMenu openMenu = new JMenu("Open");
		JMenu newMenu = new JMenu("New");
		menuBar.add(file);
		menuBar.add(help);
		manual = new JMenuItem("Manual");
		about = new JMenuItem("About");
		openProj = new JMenuItem("Open Project");
		newTstubd = new JMenuItem("New Project");
		newModel = new JMenuItem("New Model");
		importSbml = new JMenuItem("SBML");
		importDot = new JMenuItem("Dot");
		exit = new JMenuItem("Exit");
		graph = new JMenuItem("New Graph");
		openProj.addActionListener(this);
		manual.addActionListener(this);
		newTstubd.addActionListener(this);
		newModel.addActionListener(this);
		exit.addActionListener(this);
		about.addActionListener(this);
		importSbml.addActionListener(this);
		importDot.addActionListener(this);
		graph.addActionListener(this);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		newTstubd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		openProj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		newModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		graph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		exit.setMnemonic(KeyEvent.VK_X);
		newTstubd.setMnemonic(KeyEvent.VK_N);
		openProj.setMnemonic(KeyEvent.VK_O);
		newModel.setMnemonic(KeyEvent.VK_M);
		about.setMnemonic(KeyEvent.VK_A);
		manual.setMnemonic(KeyEvent.VK_L);
		graph.setMnemonic(KeyEvent.VK_G);
		file.add(newMenu);
		newMenu.add(newTstubd);
		newMenu.add(newModel);
		newMenu.add(graph);
		file.add(openMenu);
		openMenu.add(openProj);
		file.addSeparator();
		file.add(importMenu);
		importMenu.add(importDot);
		importMenu.add(importSbml);
		file.addSeparator();
		file.add(exit);
		help.add(manual);
		help.add(about);
		root = null;

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null, this);
		tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(900, 650));
		tab.setUI(new TabbedPaneCloseButtonUI());
		mainPanel.add(tree, "West");
		mainPanel.add(tab, "Center");
		log = new Log();
		mainPanel.add(log, "South");
		frame.setContentPane(mainPanel);
		frame.setJMenuBar(menuBar);
		frame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		} catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = frame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		frame.setLocation(x, y);
		frame.setVisible(true);
	}

	/**
	 * This method performs different functions depending on what menu items are
	 * selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == about) {
			final JFrame f = new JFrame("About");
			JLabel bioSim = new JLabel("BioSim 0.2");
			Font font = bioSim.getFont();
			font = font.deriveFont(Font.BOLD, 36.0f);
			bioSim.setFont(font);
			JLabel uOfU = new JLabel("University of Utah");
			JButton credits = new JButton("Credits");
			credits.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] options = { "Close" };
					JOptionPane.showOptionDialog(f, "Nathan Barker\nHiroyuki Kuwahara\n"
							+ "Curtis Madsen\nChris Myers\nNam Nguyen", "Credits",
							JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
				}
			});
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					f.dispose();
				}
			});
			JPanel buttons = new JPanel();
			buttons.add(credits);
			buttons.add(close);
			JPanel aboutPanel = new JPanel(new BorderLayout());
			JPanel uOfUPanel = new JPanel();
			uOfUPanel.add(uOfU);
			aboutPanel.add(bioSim, "North");
			aboutPanel.add(uOfUPanel, "Center");
			aboutPanel.add(buttons, "South");
			f.setContentPane(aboutPanel);
			f.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = f.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			f.setLocation(x, y);
			f.setVisible(true);
		} else if (e.getSource() == manual) {
			try {
				Runtime exec = Runtime.getRuntime();
				exec.exec("firefox " + System.getenv("BIOSIM") + "/docs/BioSim.html");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open manual.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the exit menu item is selected
		else if (e.getSource() == exit) {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab.getComponentAt(i).getName().equals("Simulate")
						|| tab.getComponentAt(i).getName().equals("SBML Editor")
						|| tab.getComponentAt(i).getName().contains("Graph")) {
					save(i, false);
				} else {
					Object[] options = { "Yes", "No", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame,
							"Do you want to save changes to " + tab.getTitleAt(i) + "?",
							"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						save(i, true);
					}
				}
			}
			System.exit(1);
		}
		// if the open popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("openSim")) {
			openSim();
		}
		// if the create simulation popup menu is selected on a dot file
		else if (e.getActionCommand().equals("createSim")) {
			try {
				simulate(true);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame,
						"You must select a valid dot file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the simulate popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("simulate")) {
			try {
				simulate(false);
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"You must select a valid sbml file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the delete popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("deleteSim")) {
			File dir = new File(tree.getFile());
			if (dir.isDirectory()) {
				deleteDir(dir);
			} else {
				dir.delete();
			}
			tree.fixTree();
		}
		// if the delete popup menu is selected
		else if (e.getActionCommand().equals("delete")) {
			new File(tree.getFile()).delete();
			tree.fixTree();
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("dotEditor")) {
			try {
				log.addText("Executing:\nemacs " + tree.getFile() + "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec("emacs " + tree.getFile());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open dot file editor.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the edit popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("sbmlEditor")) {
			try {
				addTab(
						tree.getFile().split(File.separator)[tree.getFile().split(File.separator).length - 1],
						new SBML_Editor(tree.getFile(), null, log, this), "SBML Editor");
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "You must select a valid sbml file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the graph popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("graph")) {
			try {
				Run run = new Run();
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				run.createProperties(0, 1, 1, tree.getFile().substring(
						0,
						tree.getFile().length()
								- (tree.getFile().split(File.separator)[tree.getFile().split(
										File.separator).length - 1].length())), 314159, 1,
						new String[0], new String[0], "tsd.printer", "amount", tree.getFile()
								.split(File.separator), "none", frame, tree.getFile(), 0.1, 0.1,
						0.1, 15, dummy, "", "", null);
				String out = tree.getFile();
				if (out.length() > 4
						&& out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5) + ".dot";
				} else if (out.length() > 3
						&& out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4) + ".dot";
				}
				log.addText("Executing:\nreb2sac --target.encoding=dot --out=" + out + " "
						+ tree.getFile() + "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec("reb2sac --target.encoding=dot --out=" + out + " "
						+ tree.getFile());
				graph.waitFor();
				String error = "";
				String output = "";
				InputStream reb = graph.getErrorStream();
				int read = reb.read();
				while (read != -1) {
					error += (char) read;
					read = reb.read();
				}
				reb = graph.getInputStream();
				read = reb.read();
				while (read != -1) {
					output += (char) read;
					read = reb.read();
				}
				if (!output.equals("")) {
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals("")) {
					log.addText("Errors:\n" + error + "\n");
				}
				log.addText("Executing:\ndotty " + out + "\n");
				exec.exec("dotty " + out);
				String remove;
				if (tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")) {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ "properties";
				} else {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ ".properties";
				}
				new File(remove).delete();
				refreshTree();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the browse popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("browse")) {
			try {
				Run run = new Run();
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				run.createProperties(0, 1, 1, tree.getFile().substring(
						0,
						tree.getFile().length()
								- (tree.getFile().split(File.separator)[tree.getFile().split(
										File.separator).length - 1].length())), 314159, 1,
						new String[0], new String[0], "tsd.printer", "amount", tree.getFile()
								.split(File.separator), "none", frame, tree.getFile(), 0.1, 0.1,
						0.1, 15, dummy, "", "", null);
				String out = tree.getFile();
				if (out.length() > 4
						&& out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5) + ".xhtml";
				} else if (out.length() > 3
						&& out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4) + ".xhtml";
				}
				log.addText("Executing:\nreb2sac --target.encoding=xhtml --out=" + out + " "
						+ tree.getFile() + "\n");
				Runtime exec = Runtime.getRuntime();
				Process browse = exec.exec("reb2sac --target.encoding=xhtml --out=" + out + " "
						+ tree.getFile());
				browse.waitFor();
				String error = "";
				String output = "";
				InputStream reb = browse.getErrorStream();
				int read = reb.read();
				while (read != -1) {
					error += (char) read;
					read = reb.read();
				}
				reb = browse.getInputStream();
				read = reb.read();
				while (read != -1) {
					output += (char) read;
					read = reb.read();
				}
				if (!output.equals("")) {
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals("")) {
					log.addText("Errors:\n" + error + "\n");
				}
				log.addText("Executing:\nfirefox " + out + "\n");
				exec.exec("firefox " + out);
				String remove;
				if (tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")) {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ "properties";
				} else {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ ".properties";
				}
				new File(remove).delete();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error viewing sbml file in a browser.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the graph dot popup menu is selected
		else if (e.getActionCommand().equals("graphDot")) {
			try {
				log.addText("Executing:\ndotty " + new File(tree.getFile()).getAbsolutePath()
						+ "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec("dotty " + new File(tree.getFile()).getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to view this dot file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new menu item is selected
		else if (e.getSource() == newTstubd) {
			String filename = Buttons.browse(frame, null, null, JFileChooser.DIRECTORIES_ONLY,
					"New");
			if (!filename.equals("")) {
				new File(filename).mkdir();
				try {
					new FileWriter(new File(filename + File.separator + ".prj")).close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable create a new project.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				root = filename;
				refresh();
				tab.removeAll();
			}
		}
		// if the open project menu item is selected
		else if (e.getSource() == openProj) {
			File f;
			if (root == null) {
				f = null;
			} else {
				f = new File(root);
			}
			String projDir = Buttons.browse(frame, f, null, JFileChooser.DIRECTORIES_ONLY, "Open");
			if (!projDir.equals("")) {
				if (new File(projDir).isDirectory()) {
					boolean isProject = false;
					for (String temp : new File(projDir).list()) {
						if (temp.equals(".prj")) {
							isProject = true;
						}
					}
					if (isProject) {
						root = projDir;
						refresh();
						tab.removeAll();
					} else {
						JOptionPane.showMessageDialog(frame, "You must select a valid project.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(frame, "You must select a valid project.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the new model menu item is selected
		else if (e.getSource() == newModel) {
			if (root != null) {
				try {
					String simName = JOptionPane.showInputDialog(frame, "Enter Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (simName != null && !simName.equals("")) {
						if (simName.length() > 4) {
							if (!simName.substring(simName.length() - 5).equals(".sbml")
									&& !simName.substring(simName.length() - 4).equals(".xml")) {
								simName += ".sbml";
							}
						} else {
							simName += ".sbml";
						}
						String modelID = "";
						if (simName.length() > 4) {
							if (simName.substring(simName.length() - 5).equals(".sbml")) {
								modelID = simName.substring(0, simName.length() - 5);
							} else {
								modelID = simName.substring(0, simName.length() - 4);
							}
						}
						File f = new File(root + File.separator + simName);
						f.createNewFile();
						SBMLDocument document = new SBMLDocument();
						document.createModel();
						document.setLevel(2);
						Compartment c = document.getModel().createCompartment();
						c.setName("default");
						c.setId("default");
						document.getModel().setId(modelID);
						FileOutputStream out = new FileOutputStream(f);
						SBMLWriter writer = new SBMLWriter();
						String doc = writer.writeToString(document);
						byte[] output = doc.getBytes();
						out.write(output);
						out.close();
						addTab(f.getAbsolutePath().split(File.separator)[f.getAbsolutePath().split(
								File.separator).length - 1], new SBML_Editor(f.getAbsolutePath(),
								null, log, this), "SBML Editor");
						refreshTree();
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import sbml menu item is selected
		else if (e.getSource() == importSbml) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Open");
				if (!filename.equals("")) {
					String[] file = filename.split(File.separator);
					try {
						SBMLReader reader = new SBMLReader();
						SBMLDocument document = reader.readSBML(filename);
						document.getModel().getName();
						FileOutputStream out = new FileOutputStream(new File(root + File.separator
								+ file[file.length - 1]));
						SBMLWriter writer = new SBMLWriter();
						String doc = writer.writeToString(document);
						byte[] output = doc.getBytes();
						out.write(output);
						out.close();
						refreshTree();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import dot menu item is selected
		else if (e.getSource() == importDot) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Open");
				if (filename.length() > 3
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".dot")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid dot file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else if (!filename.equals("")) {
					String[] file = filename.split(File.separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + File.separator
								+ file[file.length - 1]));
						FileInputStream in = new FileInputStream(new File(filename));
						int read = in.read();
						while (read != -1) {
							out.write(read);
							read = in.read();
						}
						in.close();
						out.close();
						refreshTree();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the Graph data menu item is clicked
		else if (e.getSource() == graph) {
			if (root != null) {
				String filename = tree.getFile();
				if (filename != null && !filename.equals("")) {
					if (new File(filename).isDirectory()) {
						String[] list = new File(filename).list();
						String getAFile = "";
						boolean ode = false;
						int run = 1;
						for (int i = 0; i < list.length; i++) {
							if (!(new File(list[i]).isDirectory()) && list[i].length() > 4) {
								String end = "";
								for (int j = 1; j < 5; j++) {
									end = list[i].charAt(list[i].length() - j) + end;
								}
								if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
									if (list[i].contains("run-")) {
										int tempNum = Integer.parseInt(list[i].substring(4, list[i]
												.length()
												- end.length()));
										if (tempNum > run) {
											run = tempNum;
											getAFile = filename + File.separator + list[i];
											ode = false;
										}
									} else if (list[i].contains("euler-run.")
											|| list[i].contains("gear1-run.")
											|| list[i].contains("gear2-run.")
											|| list[i].contains("rk4imp-run.")
											|| list[i].contains("rk8pd-run.")
											|| list[i].contains("rkf45-run.")) {
										getAFile = filename + File.separator + list[i];
										ode = true;
									}
								}
							}
						}
						if (!getAFile.equals("")) {
							String end = "";
							for (int j = 1; j < 4; j++) {
								end = getAFile.charAt(getAFile.length() - j) + end;
							}
							if (end.equals("csv") || end.equals("dat") || end.equals("tsd")) {
								String[] split = getAFile.split(File.separator);
								String last = split[split.length - 1];
								String first = getAFile.substring(0, getAFile.length()
										- last.length());
								String printer = getAFile.substring(getAFile.length() - 3);
								String id = printer + ".printer";
								if (!ode) {
									addTab("Graph", new Graph(getAFile, "amount",
											filename.split(File.separator)[filename
													.split(File.separator).length - 1]
													+ " simulation results", id, first, run, -1,
											null, "time", this), null);
								} else {
									addTab("Graph", new Graph(getAFile, "amount",
											filename.split(File.separator)[filename
													.split(File.separator).length - 1]
													+ " simulation results", id, first, run, -1,
											null, "time", this), null);
								}
							}
						} else {
							JOptionPane.showMessageDialog(frame,
									"This directory contains no simulation data.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			} else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deleteDir(File dir) {
		File[] list = dir.listFiles();
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				deleteDir(list[i]);
			} else {
				list[i].delete();
			}
		}
		dir.delete();
	}

	/**
	 * This method refreshes the menu.
	 */
	public void refresh() {
		mainPanel.remove(tree);
		tree = new FileTree(new File(root), this);
		mainPanel.add(tree, "West");
		mainPanel.validate();
	}

	/**
	 * This method refreshes the tree.
	 */
	public void refreshTree() {
		tree.fixTree();
		mainPanel.validate();
	}

	/**
	 * This method adds the given Component to a tab.
	 */
	public void addTab(String name, Component panel, String tabName) {
		tab.addTab(name, panel);
		tab.setSelectedIndex(tab.getComponents().length - 1);
		if (tabName != null) {
			tab.getComponentAt(tab.getComponents().length - 1).setName(tabName);
		} else {
			tab.getComponentAt(tab.getComponents().length - 1).setName(name);
		}
	}

	/**
	 * This method removes the given component from the tabs.
	 */
	public void removeTab(Component component) {
		tab.remove(component);
	}

	public JTabbedPane getTab() {
		return tab;
	}

	/**
	 * Prompts the user to save work that has been done.
	 * 
	 * @param wait
	 */
	public int save(int index, boolean wait) {
		if (tab.getComponentAt(index).getName().equals("Simulate")) {
			Object[] options = { "Save", "Cancel" };
			int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to the"
					+ " simulation?", "Save Changes", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				((Reb2Sac) tab.getComponentAt(index)).save();
			}
			return 1;
		} else if (tab.getComponentAt(index).getName().equals("SBML Editor")) {
			if (((SBML_Editor) tab.getComponentAt(index)).hasChanged()) {
				Object[] options = { "Yes", "No", "Cancel" };
				int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to "
						+ tab.getTitleAt(index) + "?", "Save Changes",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (value == JOptionPane.YES_OPTION) {
					((SBML_Editor) tab.getComponentAt(index)).save();
					return 1;
				} else if (value == JOptionPane.NO_OPTION) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return 1;
			}
		} else if (tab.getComponentAt(index).getName().contains("Graph")) {
			Graph g = (Graph) tab.getComponentAt(index);
			Object[] options = { "Export", "Cancel" };
			Object[] saveOptions = { "JPEG", "PNG", "PDF", "EPS", "SVG" };
			JComboBox choice = new JComboBox(saveOptions);
			JPanel export = new JPanel();
			export.add(new JLabel("Select output filetype for exporting graph:"));
			export.add(choice);
			int value = JOptionPane
					.showOptionDialog(frame, export, "Export", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				if (choice.getSelectedItem().equals("JPEG")) {
					g.export(0);
				} else if (choice.getSelectedItem().equals("PNG")) {
					g.export(1);
				} else if (choice.getSelectedItem().equals("PDF")) {
					g.export(2);
				} else if (choice.getSelectedItem().equals("EPS")) {
					g.export(3);
				} else {
					g.export(4);
				}
			}
			return 1;
		} else {
			for (int i = 0; i < ((JTabbedPane) tab.getComponentAt(index)).getTabCount(); i++) {
				if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals(
						"Simulate")) {
					((Reb2Sac) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
				} else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
						.equals("SBML Editor")) {
					((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i))
							.save();
				} else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
						.contains("Graph")) {
					if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
						Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index))
								.getComponent(i));
						Object[] options = { "Export", "Cancel" };
						Object[] saveOptions = { "JPEG", "PNG", "PDF", "EPS", "SVG" };
						JComboBox choice = new JComboBox(saveOptions);
						JPanel export = new JPanel();
						export.add(new JLabel("Select output filetype for exporting graph:"));
						export.add(choice);
						int value = JOptionPane.showOptionDialog(frame, export, "Export",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
						if (value == JOptionPane.YES_OPTION) {
							if (choice.getSelectedItem().equals("JPEG")) {
								g.export(0);
							} else if (choice.getSelectedItem().equals("PNG")) {
								g.export(1);
							} else if (choice.getSelectedItem().equals("PDF")) {
								g.export(2);
							} else if (choice.getSelectedItem().equals("EPS")) {
								g.export(3);
							} else {
								g.export(4);
							}
						}
					}
				}
			}
			return 1;
		}
	}

	/**
	 * Returns the frame.
	 */
	public JFrame frame() {
		return frame;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			popup.removeAll();
			if (tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")
					|| tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
				JMenuItem edit = new JMenuItem("Edit");
				edit.addActionListener(this);
				edit.setActionCommand("sbmlEditor");
				JMenuItem graph = new JMenuItem("View As Graph");
				graph.addActionListener(this);
				graph.setActionCommand("graph");
				JMenuItem browse = new JMenuItem("View In Browser");
				browse.addActionListener(this);
				browse.setActionCommand("browse");
				JMenuItem simulate = new JMenuItem("Create Analysis View");
				simulate.addActionListener(this);
				simulate.setActionCommand("simulate");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.setActionCommand("delete");
				popup.add(edit);
				popup.add(graph);
				popup.add(browse);
				popup.add(simulate);
				popup.add(delete);
			} else if (tree.getFile().substring(tree.getFile().length() - 3).equals("dot")) {
				JMenuItem create = new JMenuItem("Create Analysis View");
				create.addActionListener(this);
				create.setActionCommand("createSim");
				JMenuItem edit = new JMenuItem("Edit Graph");
				edit.addActionListener(this);
				edit.setActionCommand("dotEditor");
				JMenuItem graph = new JMenuItem("View Graph");
				graph.addActionListener(this);
				graph.setActionCommand("graphDot");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.setActionCommand("delete");
				popup.add(create);
				popup.add(edit);
				popup.add(graph);
				popup.add(delete);
			} else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
				JMenuItem open = new JMenuItem("Open Analysis View");
				open.addActionListener(this);
				open.setActionCommand("openSim");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.setActionCommand("deleteSim");
				popup.add(open);
				popup.add(delete);
			}
			maybeShowPopup(e);
		} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if (tree.getFile() != null) {
				if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")
						|| tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
					try {
						addTab(tree.getFile().split(File.separator)[tree.getFile().split(
								File.separator).length - 1], new SBML_Editor(tree.getFile(), null,
								log, this), "SBML Editor");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "You must select a valid sbml file.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} else if (tree.getFile().length() >= 3
						&& tree.getFile().substring(tree.getFile().length() - 3).equals("dot")) {
					try {
						log.addText("Executing:\ndotty "
								+ new File(tree.getFile()).getAbsolutePath() + "\n");
						Runtime exec = Runtime.getRuntime();
						exec.exec("dotty " + new File(tree.getFile()).getAbsolutePath());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to view this dot file.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
					openSim();
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger() && popup.getComponentCount() != 0) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void simulate(boolean isDot) throws Exception {
		if (isDot) {
			String simName = JOptionPane.showInputDialog(frame, "Enter analysis id:",
					"Analysis ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.equals("")) {
				new File(root + File.separator + simName).mkdir();
				new FileWriter(new File(root + File.separator + simName + File.separator + ".sim"))
						.close();
				String[] dot = tree.getFile().split(File.separator);
				String sbmlFile = root
						+ File.separator
						+ simName
						+ File.separator
						+ (dot[dot.length - 1].substring(0, dot[dot.length - 1].length() - 3) + "sbml");
				log.addText("Executing:\ndot2sbml.pl " + tree.getFile() + " " + sbmlFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process dot2sbml = exec.exec("dot2sbml.pl " + tree.getFile() + " " + sbmlFile);
				dot2sbml.waitFor();
				String error = "";
				String output = "";
				InputStream reb = dot2sbml.getErrorStream();
				int read = reb.read();
				while (read != -1) {
					error += (char) read;
					read = reb.read();
				}
				reb = dot2sbml.getInputStream();
				read = reb.read();
				while (read != -1) {
					output += (char) read;
					read = reb.read();
				}
				if (!output.equals("")) {
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals("")) {
					log.addText("Errors:\n" + error + "\n");
				}
				refreshTree();
				JTabbedPane simTab = new JTabbedPane();
				Reb2Sac reb2sac = new Reb2Sac(sbmlFile, root, this, simName.trim(), log, simTab,
						null);
				simTab.addTab("Simulation", reb2sac);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
				SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac, log, this);
				reb2sac.setSbml(sbml);
				simTab.addTab("SBML Editor", sbml);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
				JLabel noDataLearn = new JLabel("No data available");
				Font font = noDataLearn.getFont();
				font = font.deriveFont(Font.BOLD, 42.0f);
				noDataLearn.setFont(font);
				noDataLearn.setHorizontalAlignment(SwingConstants.CENTER);
				simTab.addTab("Learn", noDataLearn);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Learn");
				addTab(simName, simTab, null);
				JLabel noData = new JLabel("No data available");
				font = noData.getFont();
				font = font.deriveFont(Font.BOLD, 42.0f);
				noData.setFont(font);
				noData.setHorizontalAlignment(SwingConstants.CENTER);
				simTab.addTab("Graph", noData);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Graph");
			}
		} else {
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = reader.readSBML(tree.getFile());
			document.getModel().getName();
			document.setLevel(2);
			String simName = JOptionPane.showInputDialog(frame, "Enter analysis id:",
					"Analysis ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.equals("")) {
				new File(root + File.separator + simName).mkdir();
				new FileWriter(new File(root + File.separator + simName + File.separator + ".sim"))
						.close();
				String[] sbml1 = tree.getFile().split(File.separator);
				String sbmlFile = root + File.separator + simName + File.separator
						+ sbml1[sbml1.length - 1];
				try {
					FileOutputStream out = new FileOutputStream(new File(sbmlFile));
					SBMLWriter writer = new SBMLWriter();
					String doc = writer.writeToString(document);
					byte[] output = doc.getBytes();
					out.write(output);
					out.close();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame,
							"Unable to copy sbml file to output location.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				refreshTree();
				JTabbedPane simTab = new JTabbedPane();
				Reb2Sac reb2sac = new Reb2Sac(sbmlFile, root, this, simName.trim(), log, simTab,
						null);
				simTab.addTab("Simulation", reb2sac);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
				SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac, log, this);
				reb2sac.setSbml(sbml);
				simTab.addTab("SBML Editor", sbml);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
				JLabel noDataLearn = new JLabel("No data available");
				Font font = noDataLearn.getFont();
				font = font.deriveFont(Font.BOLD, 42.0f);
				noDataLearn.setFont(font);
				noDataLearn.setHorizontalAlignment(SwingConstants.CENTER);
				simTab.addTab("Learn", noDataLearn);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Learn");
				addTab(simName, simTab, null);
				JLabel noData = new JLabel("No data available");
				font = noData.getFont();
				font = font.deriveFont(Font.BOLD, 42.0f);
				noData.setFont(font);
				noData.setHorizontalAlignment(SwingConstants.CENTER);
				simTab.addTab("Graph", noData);
				simTab.getComponentAt(simTab.getComponents().length - 1).setName("Graph");
			}
		}
	}

	private void openSim() {
		String filename = tree.getFile();
		if (filename != null && !filename.equals("")) {
			if (new File(filename).isDirectory()) {
				String[] list = new File(filename).list();
				String getAFile = "";
				String openFile = "";
				String graphFile = "";
				boolean ode = false;
				boolean stoch = false;
				int run = 1;
				for (int i = 0; i < list.length; i++) {
					if (!(new File(list[i]).isDirectory()) && list[i].length() > 4) {
						String end = "";
						for (int j = 1; j < 5; j++) {
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals("sbml")) {
							getAFile = filename + File.separator + list[i];
							openFile = getAFile.replace("sbml", "properties");
							if (!(new File(openFile).exists())) {
								openFile = null;
							}
						} else if (end.equals(".xml")) {
							getAFile = filename + File.separator + list[i];
							openFile = getAFile.replace("xml", "properties");
							if (!(new File(openFile).exists())) {
								openFile = null;
							}
						} else if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
							if (list[i].contains("run-")) {
								stoch = true;
								int tempNum = Integer.parseInt(list[i].substring(4, list[i]
										.length()
										- end.length()));
								if (tempNum > run) {
									run = tempNum;
									graphFile = filename + File.separator + list[i];
									ode = false;
								}
							} else if (list[i].contains("euler-run.")
									|| list[i].contains("gear1-run.")
									|| list[i].contains("gear2-run.")
									|| list[i].contains("rk4imp-run.")
									|| list[i].contains("rk8pd-run.")
									|| list[i].contains("rkf45-run.")) {
								graphFile = filename + File.separator + list[i];
								ode = true;
							}
						}
					}
				}
				if (!getAFile.equals("")) {
					String[] split = filename.split(File.separator);
					JTabbedPane simTab = new JTabbedPane();
					Reb2Sac reb2sac = new Reb2Sac(getAFile, root, this, split[split.length - 1]
							.trim(), log, simTab, openFile);
					simTab.addTab("Simulation", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					SBML_Editor sbml = new SBML_Editor(getAFile, reb2sac, log, this);
					reb2sac.setSbml(sbml);
					simTab.addTab("SBML Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					if (stoch) {
						Learn learn = new Learn(tree.getFile(), log);
						simTab.addTab("Learn", learn);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("Learn");
					} else {
						JLabel noDataLearn = new JLabel("No data available");
						Font font = noDataLearn.getFont();
						font = font.deriveFont(Font.BOLD, 42.0f);
						noDataLearn.setFont(font);
						noDataLearn.setHorizontalAlignment(SwingConstants.CENTER);
						simTab.addTab("Learn", noDataLearn);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("Learn");
					}
					if (!graphFile.equals("")) {
						if (ode) {
							simTab.addTab("Graph", reb2sac.createGraph(graphFile, run));
							simTab.getComponentAt(simTab.getComponents().length - 1).setName(
									"Graph");
						} else {
							simTab.addTab("Graph", reb2sac.createGraph(graphFile, run));
							simTab.getComponentAt(simTab.getComponents().length - 1).setName(
									"Graph");
						}
					} else {
						JLabel noData = new JLabel("No data available");
						Font font = noData.getFont();
						font = font.deriveFont(Font.BOLD, 42.0f);
						noData.setFont(font);
						noData.setHorizontalAlignment(SwingConstants.CENTER);
						simTab.addTab("Graph", noData);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("Graph");
					}
					addTab(split[split.length - 1], simTab, null);
				}
			}
		}
	}

	/**
	 * Embedded class that allows tabs to be closed.
	 */
	class TabbedPaneCloseButtonUI extends BasicTabbedPaneUI {
		public TabbedPaneCloseButtonUI() {
			super();
		}

		protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
				Rectangle iconRect, Rectangle textRect) {

			super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

			Rectangle rect = rects[tabIndex];
			g.setColor(Color.black);
			g.drawRect(rect.x + rect.width - 19, rect.y + 4, 13, 12);
			g.drawLine(rect.x + rect.width - 16, rect.y + 7, rect.x + rect.width - 10, rect.y + 13);
			g.drawLine(rect.x + rect.width - 10, rect.y + 7, rect.x + rect.width - 16, rect.y + 13);
			g.drawLine(rect.x + rect.width - 15, rect.y + 7, rect.x + rect.width - 9, rect.y + 13);
			g.drawLine(rect.x + rect.width - 9, rect.y + 7, rect.x + rect.width - 15, rect.y + 13);
		}

		protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
			return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 24;
		}

		protected MouseListener createMouseListener() {
			return new MyMouseHandler();
		}

		class MyMouseHandler extends MouseHandler {
			public MyMouseHandler() {
				super();
			}

			public void mouseReleased(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int tabIndex = -1;
				int tabCount = tabPane.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (rects[i].contains(x, y)) {
						tabIndex = i;
						break;
					}
				}
				if (tabIndex >= 0 && !e.isPopupTrigger()) {
					Rectangle tabRect = rects[tabIndex];
					y = y - tabRect.y;
					if ((x >= tabRect.x + tabRect.width - 18)
							&& (x <= tabRect.x + tabRect.width - 8) && (y >= 5) && (y <= 15)) {
						if (tab.getComponentAt(tabIndex).getName().equals("Simulate")
								|| tab.getComponentAt(tabIndex).getName().equals("SBML Editor")
								|| tab.getComponentAt(tabIndex).getName().contains("Graph")) {
							if (save(tabIndex, false) == 1) {
								tabPane.remove(tabIndex);
							}
						} else {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save changes to the analysis view?",
									"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								if (save(tabIndex, false) == 1) {
									tabPane.remove(tabIndex);
								}
							} else if (value == JOptionPane.NO_OPTION) {
								tabPane.remove(tabIndex);
							}
						}
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public JMenuItem getExitButton() {
		return exit;
	}

	/**
	 * This is the main method. It excecutes the Tstubd GUI FrontEnd program.
	 */
	public static void main(String args[]) {
		System.loadLibrary("sbmlj");
		new BioSim();
	}
}