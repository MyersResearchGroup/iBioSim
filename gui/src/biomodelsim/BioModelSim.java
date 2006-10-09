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
public class BioModelSim implements ActionListener {

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

	private JMenuItem open; // The open menu item

	private JMenuItem editor; // The sbml editor menu item

	private JMenuItem graph; // The graph menu item

	private String root; // The root directory

	private FileTree tree; // FileTree

	private JTabbedPane tab; // JTabbedPane for different tools

	private JPanel mainPanel; // the main panel

	private Log log; // the log

	/**
	 * This is the constructor for the Tstubd class. It initializes all the
	 * input fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public BioModelSim() {
		// Creates a new frame
		frame = new JFrame("BioModelSim");

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

		// Creates a menu for the frame
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		JMenu tools = new JMenu("Tools");
		tools.setMnemonic(KeyEvent.VK_T);
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		JMenu importMenu = new JMenu("Import");
		menuBar.add(file);
		menuBar.add(tools);
		menuBar.add(help);
		simulate = new JMenuItem("Simulate");
		learn = new JMenuItem("Learn");
		synthesize = new JMenuItem("Synthesize");
		_abstract = new JMenuItem("Abstract");
		manual = new JMenuItem("Manual");
		about = new JMenuItem("About");
		save = new JMenuItem("Save");
		open = new JMenuItem("Open");
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
		open.addActionListener(this);
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
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		newTstubd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		display.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
		editor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
		graph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		exit.setMnemonic(KeyEvent.VK_X);
		open.setMnemonic(KeyEvent.VK_O);
		newTstubd.setMnemonic(KeyEvent.VK_N);
		save.setMnemonic(KeyEvent.VK_S);
		print.setMnemonic(KeyEvent.VK_P);
		display.setMnemonic(KeyEvent.VK_D);
		about.setMnemonic(KeyEvent.VK_A);
		manual.setMnemonic(KeyEvent.VK_M);
		editor.setMnemonic(KeyEvent.VK_E);
		graph.setMnemonic(KeyEvent.VK_G);
		file.add(newTstubd);
		file.add(open);
		file.addSeparator();
		file.add(save);
		file.addSeparator();
		file.add(importMenu);
		importMenu.add(importSbml);
		file.addSeparator();
		file.add(display);
		file.add(print);
		file.addSeparator();
		file.add(exit);
		help.add(manual);
		help.add(about);
		tools.add(simulate);
		tools.add(learn);
		tools.add(editor);
		tools.add(graph);
		tools.add(synthesize);
		tools.add(_abstract);
		root = null;

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null);
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
		// if the simulate menu item is selected
		else if (e.getSource() == simulate) {
			try {
				SBMLReader reader = new SBMLReader();
				SBMLDocument document = reader.readSBML(tree.getFile());
				document.getModel().getName();
				String simName = JOptionPane.showInputDialog(frame, "Enter simulation id:",
						"Simulation ID", JOptionPane.PLAIN_MESSAGE);
				addTab("Simulate", new Reb2Sac(tree.getFile(), root, this, simName.trim(), log));
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
		// if the open menu item is selected
		else if (e.getSource() == open) {
			String filename = Buttons.browse(frame, null, null, JFileChooser.DIRECTORIES_ONLY,
					"Open");
			if (!filename.equals("")) {
				if (new File(filename).isDirectory()) {
					if (!(new File(root + File.separator + "work").exists())) {
						new File(root + File.separator + "work").mkdir();
					}
					root = filename;
					refresh();
				} else {
					JOptionPane.showMessageDialog(frame, "You must select a valid project.",
							"Error", JOptionPane.ERROR_MESSAGE);
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
				addTab("SBML Editor", new SBML_Editor(tree.getFile()));
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
										null, "time", this));
							} else {
								button.setSelected(false);
								addTab("Graph", new Graph(filename, frame, "amount",
										"read-in-data simulation results", button, "read-in-data",
										id, first, 1, new String[0], -1, null, "time", this));
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
									"time", this));
						} else {
							button.setSelected(false);
							addTab("Graph", new Graph(filename, frame, "amount",
									"read-in-data simulation results", button, "read-in-data", id,
									first, 1, new String[0], -1, null, "time", this));
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

	/**
	 * This method refreshes the menu.
	 */
	public void refresh() {
		mainPanel.remove(tree);
		tree = new FileTree(new File(root));
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
	 * This method adds the given JPanel to a tab.
	 */
	public void addTab(String name, JPanel panel) {
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
						tabPane.remove(tabIndex);
					}
				}
			}
		}
	}
}
