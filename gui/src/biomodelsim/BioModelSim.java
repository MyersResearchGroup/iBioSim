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
public class BioModelSim implements MouseListener, ActionListener {

	private JFrame frame; // Frame where components of the GUI are displayed

	private JMenuItem newTstubd; // The new menu item

	private JMenuItem exit; // The exit menu item

	private JMenuItem display; // The display menu item

	private JMenuItem print; // The print menu item

	private JMenuItem importSbml; // The import menu item

	private JMenuItem simulate; // The simulate menu item

	private JMenuItem learn; // The learn menu item

	private JMenuItem synthesize; // The synthesize menu item

	private JMenuItem _abstract; // The abstract menu item

	private JMenuItem manual; // The manual menu item

	private JMenuItem about; // The about menu item

	private JMenuItem save; // The save menu item

	private JMenuItem openProj, openSim; // The open menu item

	private JMenuItem editor; // The sbml editor menu item

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
	 */
	public BioModelSim() {
		// Creates a new frame
		frame = new JFrame("BioSim");

		// Makes it so that clicking the x in the corner closes the program
		WindowListener w = new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				System.exit(1);
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
		JMenu tools = new JMenu("Tools");
		tools.setMnemonic(KeyEvent.VK_T);
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		JMenu importMenu = new JMenu("Import");
		JMenu openMenu = new JMenu("Open");
		menuBar.add(file);
		menuBar.add(tools);
		// ***menuBar.add(help);***
		simulate = new JMenuItem("Simulate");
		learn = new JMenuItem("Learn");
		synthesize = new JMenuItem("Synthesize");
		_abstract = new JMenuItem("Abstract");
		manual = new JMenuItem("Manual");
		about = new JMenuItem("About");
		save = new JMenuItem("Save");
		openProj = new JMenuItem("Open Project");
		openSim = new JMenuItem("Open Simulation");
		newTstubd = new JMenuItem("New");
		display = new JMenuItem("Display");
		print = new JMenuItem("Print");
		importSbml = new JMenuItem("sbml");
		exit = new JMenuItem("Exit");
		editor = new JMenuItem("SBML Editor");
		graph = new JMenuItem("Graph Data");
		simulate.addActionListener(this);
		learn.addActionListener(this);
		save.addActionListener(this);
		openProj.addActionListener(this);
		openSim.addActionListener(this);
		synthesize.addActionListener(this);
		_abstract.addActionListener(this);
		manual.addActionListener(this);
		newTstubd.addActionListener(this);
		exit.addActionListener(this);
		about.addActionListener(this);
		display.addActionListener(this);
		print.addActionListener(this);
		importSbml.addActionListener(this);
		editor.addActionListener(this);
		graph.addActionListener(this);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		newTstubd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		display.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
		editor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
		graph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		exit.setMnemonic(KeyEvent.VK_X);
		newTstubd.setMnemonic(KeyEvent.VK_N);
		save.setMnemonic(KeyEvent.VK_S);
		print.setMnemonic(KeyEvent.VK_P);
		display.setMnemonic(KeyEvent.VK_D);
		about.setMnemonic(KeyEvent.VK_A);
		manual.setMnemonic(KeyEvent.VK_M);
		editor.setMnemonic(KeyEvent.VK_E);
		graph.setMnemonic(KeyEvent.VK_G);
		file.add(newTstubd);
		file.add(openMenu);
		openMenu.add(openProj);
		openMenu.add(openSim);
		file.addSeparator();
		file.add(save);
		file.addSeparator();
		file.add(importMenu);
		importMenu.add(importSbml);
		file.addSeparator();
		// ***file.add(display);***
		// ***file.add(print);***
		// ***file.addSeparator();***
		file.add(exit);
		help.add(manual);
		help.add(about);
		tools.add(simulate);
		// ***tools.add(learn);***
		tools.add(editor);
		tools.add(graph);
		// ***tools.add(synthesize);***
		// ***tools.add(_abstract);***
		root = null;

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null, this);
		tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(850, 650));
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
		// if the exit menu item is selected
		if (e.getSource() == exit) {
			System.exit(1);
		}
		// if the open popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("openSim")) {
			String filename = tree.getFile();
			if (filename != null && !filename.equals("")) {
				if (new File(filename).isDirectory()) {
					String[] list = new File(filename).list();
					String getAFile = "";
					String openFile = "";
					String graphFile = "";
					for (int i = 0; i < list.length; i++) {
						if (!(new File(list[i]).isDirectory())) {
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
							} else if (end.equals(".tsd") || end.equals(".dat")
									|| end.equals(".csv")) {
								if (end.contains("run-1")) {
									graphFile = filename + File.separator + list[i];
								}
							}
						}
					}
					if (graphFile.equals("")) {
						for (int i = 0; i < list.length; i++) {
							if (!(new File(list[i]).isDirectory())) {
								String end = "";
								for (int j = 1; j < 5; j++) {
									end = list[i].charAt(list[i].length() - j) + end;
								}
								if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
									graphFile = filename + File.separator + list[i];
								}
							}
						}
					}
					if (!getAFile.equals("")) {
						String[] split = filename.split(File.separator);
						JTabbedPane simTab = new JTabbedPane();
						Reb2Sac reb2sac = new Reb2Sac(getAFile, root, this, split[split.length - 1]
								.trim(), log, simTab, openFile);
						simTab.addTab("Options", reb2sac);
						simTab.getComponentAt(simTab.getComponents().length - 1)
								.setName("Simulate");
						SBML_Editor sbml = new SBML_Editor(getAFile, reb2sac);
						reb2sac.setSbml(sbml);
						simTab.addTab("SBML Editor", sbml);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName(
								"SBML Editor");
						if (!graphFile.equals("")) {
							simTab.addTab("Graph", reb2sac.createGraph(graphFile));
							simTab.getComponentAt(simTab.getComponents().length - 1).setName(
									"Graph");
						}
						addTab(split[split.length - 1], simTab);
					}
				}
			}
		}
		// if the create simulation popup menu is selected on a dot file
		else if (e.getActionCommand().equals("createSim")) {
			try {
				String simName = JOptionPane.showInputDialog(frame, "Enter simulation id:",
						"Simulation ID", JOptionPane.PLAIN_MESSAGE);
				if (simName != null && !simName.equals("")) {
					new File(root + File.separator + "work" + File.separator + simName).mkdir();
					String[] dot = tree.getFile().split(File.separator);
					String sbmlFile = root
							+ File.separator
							+ "work"
							+ File.separator
							+ simName
							+ File.separator
							+ (dot[dot.length - 1].substring(0, dot[dot.length - 1].length() - 3) + "sbml");
					Runtime exec = Runtime.getRuntime();
					Process dot2sbml = exec.exec("dot2sbml.pl " + tree.getFile() + " " + sbmlFile);
					dot2sbml.waitFor();
					refreshTree();
					JTabbedPane simTab = new JTabbedPane();
					Reb2Sac reb2sac = new Reb2Sac(sbmlFile, root, this, simName.trim(), log,
							simTab, null);
					simTab.addTab("Options", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac);
					reb2sac.setSbml(sbml);
					simTab.addTab("SBML Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					// simTab.addTab("Graph", new JPanel());
					// simTab.getComponentAt(simTab.getComponents().length -
					// 1).setName("Graph");
					addTab(simName, simTab);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame,
						"You must select a valid dot file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the learn from data popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("learnSim")) {
			try {
				Runtime exec = Runtime.getRuntime();
				Process learn = exec.exec("GeneNet --cpp_harshenBoundsOnTie"
						+ " --cpp_cmp_output_donotInvertSortOrder --cpp_seedParents"
						+ " --cmp_score_mustNotWinMajority " + tree.getFile() + " > run.log");
				learn.waitFor();
				exec.exec("dotty "
						+ new File(tree.getFile() + File.separator + "method.dot")
								.getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to laren from data.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("dotEditor")) {
			try {
				Runtime exec = Runtime.getRuntime();
				Process edit = exec.exec("emacs " + tree.getFile());
				edit.waitFor();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open dot file editor.", "Error",
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
		// if the edit popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("sbmlEditor")) {
			try {
				addTab("SBML Editor", new SBML_Editor(tree.getFile(), null));
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
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec("reb2sac --target.encoding=dot " + tree.getFile());
				graph.waitFor();
				File outDot = new File("out.dot");
				exec.exec("dotty " + outDot.getAbsolutePath());
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
				Runtime exec = Runtime.getRuntime();
				Process browse = exec.exec("reb2sac --target.encoding=xhtml " + tree.getFile());
				browse.waitFor();
				File outXhtml = new File("out.xhtml");
				exec.exec("firefox " + outXhtml.getAbsolutePath());
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
		// if the simulate popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("simulate")) {
			try {
				SBMLReader reader = new SBMLReader();
				SBMLDocument document = reader.readSBML(tree.getFile());
				document.getModel().getName();
				document.setLevel(2);
				String simName = JOptionPane.showInputDialog(frame, "Enter simulation id:",
						"Simulation ID", JOptionPane.PLAIN_MESSAGE);
				if (simName != null && !simName.equals("")) {
					new File(root + File.separator + "work" + File.separator + simName).mkdir();
					String[] sbml1 = tree.getFile().split(File.separator);
					String sbmlFile = root + File.separator + "work" + File.separator + simName
							+ File.separator + sbml1[sbml1.length - 1];
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
					Reb2Sac reb2sac = new Reb2Sac(sbmlFile, root, this, simName.trim(), log,
							simTab, null);
					simTab.addTab("Options", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac);
					reb2sac.setSbml(sbml);
					simTab.addTab("SBML Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					// simTab.addTab("Graph", new JPanel());
					// simTab.getComponentAt(simTab.getComponents().length -
					// 1).setName("Graph");
					addTab(simName, simTab);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"You must select a valid sbml file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the delete popup menu is selected
		else if (e.getActionCommand().equals("delete")) {
			new File(tree.getFile()).delete();
			tree.fixTree();
		}
		// if the graph dot popup menu is selected
		else if (e.getActionCommand().equals("graphDot")) {
			try {
				Runtime exec = Runtime.getRuntime();
				exec.exec("dotty " + new File(tree.getFile()).getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to view this dot file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the simulate menu item is selected
		else if (e.getSource() == simulate) {
			try {
				SBMLReader reader = new SBMLReader();
				SBMLDocument document = reader.readSBML(tree.getFile());
				document.getModel().getName();
				document.setLevel(2);
				String simName = JOptionPane.showInputDialog(frame, "Enter simulation id:",
						"Simulation ID", JOptionPane.PLAIN_MESSAGE);
				if (simName != null && !simName.equals("")) {
					new File(root + File.separator + "work" + File.separator + simName).mkdir();
					String[] sbml1 = tree.getFile().split(File.separator);
					String sbmlFile = root + File.separator + "work" + File.separator + simName
							+ File.separator + sbml1[sbml1.length - 1];
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
					Reb2Sac reb2sac = new Reb2Sac(sbmlFile, root, this, simName.trim(), log,
							simTab, null);
					simTab.addTab("Options", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac);
					reb2sac.setSbml(sbml);
					simTab.addTab("SBML Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					// simTab.addTab("Graph", new JPanel());
					// simTab.getComponentAt(simTab.getComponents().length -
					// 1).setName("Graph");
					addTab(simName, simTab);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame,
						"You must select a valid sbml file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the learn menu item is selected
		else if (e.getSource() == learn) {
			new Learn();
		}
		// if the open project menu item is selected
		else if (e.getSource() == openProj) {
			String filename = Buttons.browse(frame, null, null, JFileChooser.DIRECTORIES_ONLY,
					"Open");
			if (!filename.equals("")) {
				if (new File(filename).isDirectory()) {
					if (!(new File(root + File.separator + "work").exists())) {
						new File(root + File.separator + "work").mkdir();
					}
					root = filename;
					refresh();
					tab.removeAll();
				} else {
					JOptionPane.showMessageDialog(frame, "You must select a valid project.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the open simulation menu item is selected
		else if (e.getSource() == openSim) {
			String filename = tree.getFile();
			if (filename != null && !filename.equals("")) {
				if (new File(filename).isDirectory()) {
					String[] list = new File(filename).list();
					String getAFile = "";
					String openFile = "";
					for (int i = 0; i < list.length; i++) {
						if (!(new File(list[i]).isDirectory())) {
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
							}
						}
					}
					if (!getAFile.equals("")) {
						String[] split = filename.split(File.separator);
						JTabbedPane simTab = new JTabbedPane();
						Reb2Sac reb2sac = new Reb2Sac(getAFile, root, this, split[split.length - 1]
								.trim(), log, simTab, openFile);
						simTab.addTab("Options", reb2sac);
						simTab.getComponentAt(simTab.getComponents().length - 1)
								.setName("Simulate");
						SBML_Editor sbml = new SBML_Editor(getAFile, reb2sac);
						reb2sac.setSbml(sbml);
						simTab.addTab("SBML Editor", sbml);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName(
								"SBML Editor");
						// simTab.addTab("Graph", new JPanel());
						// simTab.getComponentAt(simTab.getComponents().length -
						// 1).setName("Graph");
						addTab(split[split.length - 1], simTab);
					}
				}
			}
		}
		// if the save menu is selected
		else if (e.getSource() == save) {
			if (tab.getSelectedComponent() != null) {
				if (tab.getSelectedComponent().getName().equals("Simulate")) {
					((Reb2Sac) tab.getSelectedComponent()).save();
				} else if (tab.getSelectedComponent().getName().equals("SBML Editor")) {
					((SBML_Editor) tab.getSelectedComponent()).save();
				} else if (tab.getSelectedComponent().getName().equals("Graph")) {
					Object[] options = { "Save As JPEG", "Save As PNG" };
					int value = JOptionPane.showOptionDialog(frame, "Which type would you like to"
							+ " save the graph as?", "Save Changes", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						((Graph) tab.getSelectedComponent()).save();
					} else {
						((Graph) tab.getSelectedComponent()).save();
					}
				} else {
					JTabbedPane simTab = (JTabbedPane) tab.getSelectedComponent();
					if (simTab.getSelectedComponent().getName().equals("Simulate")) {
						((Reb2Sac) simTab.getSelectedComponent()).save();
					} else if (simTab.getSelectedComponent().getName().equals("SBML Editor")) {
						((SBML_Editor) simTab.getSelectedComponent()).save();
					} else if (simTab.getSelectedComponent().getName().equals("Graph")) {
						Object[] options = { "Save As JPEG", "Save As PNG" };
						int value = JOptionPane.showOptionDialog(frame,
								"Which type would you like to" + " save the graph as?",
								"Save Changes", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
						if (value == JOptionPane.YES_OPTION) {
							((Graph) simTab.getSelectedComponent()).save();
						} else {
							((Graph) simTab.getSelectedComponent()).save();
						}
					}
				}
			}
		}
		// if the new menu item is selected
		else if (e.getSource() == newTstubd) {
			String filename = Buttons.browse(frame, null, null, JFileChooser.DIRECTORIES_ONLY,
					"New");
			if (!filename.equals("")) {
				root = filename;
				new File(root).mkdir();
				new File(root + File.separator + "work").mkdir();
				refresh();
				tab.removeAll();
			}
		}
		// if the import sbml menu item is selected
		else if (e.getSource() == importSbml) {
			if (root != null) {
				String filename = Buttons
						.browse(frame, null, null, JFileChooser.FILES_ONLY, "Open");
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
		// if the SBML Editor menu item is clicked
		else if (e.getSource() == editor) {
			try {
				addTab("SBML Editor", new SBML_Editor(tree.getFile(), null));
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "You must select a valid sbml file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the Graph data menu item is clicked
		else if (e.getSource() == graph) {
			// File file = null;
			// String filename = Buttons.browse(frame, file, null,
			// JFileChooser.FILES_ONLY, "Graph");
			String filename = tree.getFile();
			if (filename != null && !filename.equals("")) {
				if (new File(filename).isDirectory()) {
					String[] list = new File(filename).list();
					String getAFile = "";
					for (int i = 0; i < list.length; i++) {
						String end = "";
						for (int j = 1; j < 4; j++) {
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals("csv") || end.equals("dat") || end.equals("tsd")) {
							getAFile = filename + File.separator + list[i];
						}
					}
					if (!getAFile.equals("")) {
						filename = getAFile;
						String end = "";
						for (int j = 1; j < 4; j++) {
							end = filename.charAt(filename.length() - j) + end;
						}
						if (end.equals("csv") || end.equals("dat") || end.equals("tsd")) {
							String[] split = filename.split(File.separator);
							String last = split[split.length - 1];
							String first = filename.substring(0, filename.length() - last.length());
							String printer = filename.substring(filename.length() - 3);
							String id = printer + ".printer";
							JRadioButton button = new JRadioButton();
							if (last.substring(0, 3).equals("run")) {
								button.setSelected(true);
								String get = "";
								for (int i = 0; i < last.length(); i++) {
									if (Character.isDigit(last.charAt(i))) {
										get += last.charAt(i);
									}
								}
								int number = Integer.parseInt(get);
								int runs;
								int i = 1;
								try {
									while (true) {
										InputStream test = new FileInputStream(new File((first
												+ "run-" + i + "." + printer)));
										test.read();
										i++;
									}
								} catch (Exception e2) {
									runs = i - 1;
								}
								runs = Math.max(number, runs);
								addTab("Graph", new Graph(first + "run-1." + printer, frame,
										"amount", "read-in-data average" + " simulation results",
										button, "read-in-data", id, first, runs, new String[0], -1,
										null, "time", this, null));
							} else {
								button.setSelected(false);
								addTab("Graph", new Graph(filename, frame, "amount",
										"read-in-data simulation results", button, "read-in-data",
										id, first, 1, new String[0], -1, null, "time", this, null));
							}
						}
					} else {
						JOptionPane.showMessageDialog(frame,
								"This directory contains no simulation data.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					try {
						String[] split = filename.split(File.separator);
						String last = split[split.length - 1];
						String first = filename.substring(0, filename.length() - last.length());
						String printer = filename.substring(filename.length() - 3);
						String id = printer + ".printer";
						JRadioButton button = new JRadioButton();
						if (last.substring(0, 3).equals("run")) {
							button.setSelected(true);
							String get = "";
							for (int i = 0; i < last.length(); i++) {
								if (Character.isDigit(last.charAt(i))) {
									get += last.charAt(i);
								}
							}
							int number = Integer.parseInt(get);
							// int runs = Integer.parseInt((String)
							// JOptionPane.showInputDialog(frame,
							// "Please enter the number of output files in this
							// simulation:",
							// "Enter Number Of Runs",
							// JOptionPane.PLAIN_MESSAGE,
							// null, null,
							// number));
							int runs;
							int i = 1;
							try {
								while (true) {
									InputStream test = new FileInputStream(new File((first + "run-"
											+ i + "." + printer)));
									test.read();
									i++;
								}
								// for (i = number; i <= runs; i++) {
								// InputStream test = new FileInputStream(new
								// File((first + "run-" + i
								// + "." + printer)));
								// test.read();
								// }
							} catch (Exception e2) {
								runs = i - 1;
							}
							runs = Math.max(number, runs);
							addTab("Graph", new Graph(first + "run-1." + printer, frame, "amount",
									"read-in-data average" + " simulation results", button,
									"read-in-data", id, first, runs, new String[0], number, null,
									"time", this, null));
						} else {
							button.setSelected(false);
							addTab("Graph", new Graph(filename, frame, "amount",
									"read-in-data simulation results", button, "read-in-data", id,
									first, 1, new String[0], -1, null, "time", this, null));
						}
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Error reading in data!"
								+ "\nFile may be named incorrectly" + " or may be invalid.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(frame,
						"You must select a valid file or directory to graph data.", "Error",
						JOptionPane.ERROR_MESSAGE);
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
	public void addTab(String name, Component panel) {
		tab.addTab(name, panel);
		tab.getComponentAt(tab.getComponents().length - 1).setName(name);
	}

	/**
	 * This method removes the given component from the tabs.
	 */
	public void removeTab(Component component) {
		tab.remove(component);
	}

	/**
	 * Prompts the user to save work that has been done.
	 */
	public void save(int index) {
		if (tab.getComponentAt(index).getName().equals("Simulate")) {
			Object[] options = { "Save", "Cancel" };
			int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to the"
					+ " simulation?", "Save Changes", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				((Reb2Sac) tab.getComponentAt(index)).save();
			}
		} else if (tab.getComponentAt(index).getName().equals("SBML Editor")) {
			Object[] options = { "Save", "Cancel" };
			int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to the"
					+ " sbml file?", "Save Changes", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				((SBML_Editor) tab.getComponentAt(index)).save();
			}
		} else if (tab.getComponentAt(index).getName().equals("Graph")) {
			Object[] options = { "Save As JPEG", "Save As PNG", "Cancel" };
			int value = JOptionPane.showOptionDialog(frame, "Do you want to save the graph?",
					"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				((Graph) tab.getComponentAt(index)).save();
			} else if (value == JOptionPane.NO_OPTION) {
				((Graph) tab.getComponentAt(index)).save();
			}
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
						.equals("Graph")) {
					((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
				}
			}
		}
	}

	/**
	 * Returns the frame.
	 */
	public JFrame frame() {
		return frame;
	}

	/**
	 * This is the main method. It excecutes the Tstubd GUI FrontEnd program.
	 */
	public static void main(String args[]) {
		System.loadLibrary("sbmlj");
		new BioModelSim();
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
				JMenuItem simulate = new JMenuItem("Create A Simulation");
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
				JMenuItem create = new JMenuItem("Create A Simulation");
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
			} else if (new File(tree.getFile()).isDirectory()) {
				if (new File(tree.getFile()).getParentFile().getName().equals("work")) {
					JMenuItem open = new JMenuItem("Open Simulation");
					open.addActionListener(this);
					open.setActionCommand("openSim");
					JMenuItem learn = new JMenuItem("Learn From Data");
					learn.addActionListener(this);
					learn.setActionCommand("learnSim");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("deleteSim");
					popup.add(open);
					popup.add(learn);
					popup.add(delete);
				}
			}
			maybeShowPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		/*
		 * popup.removeAll();
		 * if(tree.getFile().substring(tree.getFile().length() -
		 * 4).equals("sbml") || tree.getFile().substring(tree.getFile().length() -
		 * 4).equals(".xml")) { JMenuItem edit = new JMenuItem("Edit");
		 * edit.addActionListener(this); edit.setActionCommand("sbmlEditor");
		 * popup.add(edit); } maybeShowPopup(e);
		 */
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger() && popup.getComponentCount() != 0) {
			popup.show(e.getComponent(), e.getX(), e.getY());
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
								|| tab.getComponentAt(tabIndex).getName().equals("Graph")) {
							save(tabIndex);
							tabPane.remove(tabIndex);
						} else {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save changes to the" + " simulation?",
									"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								save(tabIndex);
								tabPane.remove(tabIndex);
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
}
