package biomodelsim;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.GlobalConstants;
import lhpn2sbml.parser.LHPNFile;
import lhpn2sbml.gui.*;
import graph.Graph;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener; //import java.awt.event.FocusEvent;
//import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;
import java.util.regex.Pattern; //import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JViewport;

import tabs.CloseAndMaxTabbedPane;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;

import learn.Learn;
import learn.LearnLHPN;

import synthesis.Synthesis;

import verification.Verification;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;

import reb2sac.Reb2Sac;
import reb2sac.Run;
import sbmleditor.SBML_Editor;
import buttons.Buttons;
import datamanager.DataManager;

//import datamanager.DataManagerLHPN;

/**
 * This class creates a GUI for the Tstubd program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * are selected.
 * 
 * @author Curtis Madsen
 */

public class BioSim implements MouseListener, ActionListener, MouseMotionListener,
		MouseWheelListener {

	private JFrame frame; // Frame where components of the GUI are displayed

	private JMenuBar menuBar;

	private JMenu file, edit, view, tools, help, saveAsMenu, importMenu, exportMenu, newMenu,
			viewModel; // The file menu

	private JMenuItem newProj; // The new menu item

	private JMenuItem newModel; // The new menu item

	private JMenuItem newCircuit; // The new menu item

	private JMenuItem newVhdl; // The new vhdl menu item

	private JMenuItem newLhpn; // The new lhpn menu item

	private JMenuItem newCsp; // The new csp menu item

	private JMenuItem newHse; // The new handshaking extension menu item

	private JMenuItem newUnc; // The new extended burst mode menu item

	private JMenuItem newRsg; // The new rsg menu item

	private JMenuItem newSpice; // The new spice circuit item

	private JMenuItem exit; // The exit menu item

	private JMenuItem importSbml; // The import sbml menu item

	private JMenuItem importDot; // The import dot menu item

	private JMenuItem importVhdl; // The import vhdl menu item

	private JMenuItem importLhpn; // The import lhpn menu item

	private JMenuItem importCsp; // The import csp menu item

	private JMenuItem importHse; // The import handshaking extension menu

	// item

	private JMenuItem importUnc; // The import extended burst mode menu item

	private JMenuItem importRsg; // The import rsg menu item

	private JMenuItem importSpice; // The import spice circuit item

	private JMenuItem manual; // The manual menu item

	private JMenuItem about; // The about menu item

	private JMenuItem openProj; // The open menu item

	private JMenuItem pref; // The preferences menu item

	private JMenuItem graph; // The graph menu item

	private JMenuItem probGraph, exportCsv, exportDat, exportEps, exportJpg, exportPdf, exportPng,
			exportSvg, exportTsd;

	private String root; // The root directory

	private FileTree tree; // FileTree

	private CloseAndMaxTabbedPane tab; // JTabbedPane for different tools

	private JToolBar toolbar; // Tool bar for common options

	private JButton saveButton, runButton, refreshButton, saveasButton, checkButton, exportButton; // Tool

	// Bar
	// options

	private JPanel mainPanel; // the main panel

	public Log log; // the log

	private JPopupMenu popup; // popup menu

	private String separator;

	private KeyEventDispatcher dispatcher;

	private JMenuItem recentProjects[];

	private String recentProjectPaths[];

	private int numberRecentProj;

	private int ShortCutKey;

	public boolean checkUndeclared, checkUnits;

	private JCheckBox Undeclared, Units, viewerCheck;

	private JTextField viewerField;

	private JLabel viewerLabel;

	private Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");

	private boolean lema, atacs, async, externView, treeSelected = false;

	private String viewer;

	private JMenuItem copy, rename, delete, save, saveAs, saveAsGcm, saveAsGraph, saveAsSbml,
			saveAsTemplate, saveAsLhpn, check, run, export, refresh, viewCircuit, viewRules,
			viewTrace, viewLog, saveParam, saveSbml, saveTemp, viewModGraph, viewModBrowser,
			createAnal, createLearn, createSbml, createSynth, createVer, close, closeAll;

	public class MacOSAboutHandler extends Application {

		public MacOSAboutHandler() {
			addApplicationListener(new AboutBoxHandler());
		}

		class AboutBoxHandler extends ApplicationAdapter {
			public void handleAbout(ApplicationEvent event) {
				about();
				event.setHandled(true);
			}
		}
	}

	public class MacOSPreferencesHandler extends Application {

		public MacOSPreferencesHandler() {
			addApplicationListener(new PreferencesHandler());
		}

		class PreferencesHandler extends ApplicationAdapter {
			public void handlePreferences(ApplicationEvent event) {
				preferences();
				event.setHandled(true);
			}
		}
	}

	public class MacOSQuitHandler extends Application {

		public MacOSQuitHandler() {
			addApplicationListener(new QuitHandler());
		}

		class QuitHandler extends ApplicationAdapter {
			public void handleQuit(ApplicationEvent event) {
				exit();
				event.setHandled(true);
			}
		}

	}

	/**
	 * This is the constructor for the Proj class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 * 
	 * @throws Exception
	 */
	public BioSim(boolean lema, boolean atacs) {
		this.lema = lema;
		this.atacs = atacs;
		async = lema || atacs;
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// Creates a new frame
		if (lema) {
			frame = new JFrame("LEMA");
			frame.setIconImage(new ImageIcon(System.getenv("BIOSIM") + separator + "gui"
					+ separator + "icons" + separator + "iBioSim.png").getImage());
		}
		else if (atacs) {
			frame = new JFrame("ATACS");
			frame.setIconImage(new ImageIcon(System.getenv("BIOSIM") + separator + "gui"
					+ separator + "icons" + separator + "iBioSim.png").getImage());
		}
		else {
			frame = new JFrame("iBioSim");
			frame.setIconImage(new ImageIcon(System.getenv("BIOSIM") + separator + "gui"
					+ separator + "icons" + separator + "iBioSim.png").getImage());
		}

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
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		popup = new JPopupMenu();

		// Sets up the Tool Bar
		toolbar = new JToolBar();
		String imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator
				+ "icons" + File.separator + "save.png";
		saveButton = makeToolButton(imgName, "save", "Save", "Save");
		// toolButton = new JButton("Save");
		toolbar.add(saveButton);
		imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator + "icons"
				+ File.separator + "saveas.png";
		saveasButton = makeToolButton(imgName, "saveas", "Save As", "Save As");
		toolbar.add(saveasButton);
		imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator + "icons"
				+ File.separator + "run-icon.jpg";
		runButton = makeToolButton(imgName, "run", "Save and Run", "Run");
		// toolButton = new JButton("Run");
		toolbar.add(runButton);
		imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator + "icons"
				+ File.separator + "refresh.jpg";
		refreshButton = makeToolButton(imgName, "refresh", "Refresh", "Refresh");
		toolbar.add(refreshButton);
		imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator + "icons"
				+ File.separator + "savecheck.png";
		checkButton = makeToolButton(imgName, "check", "Save and Check", "Save and Check");
		toolbar.add(checkButton);
		imgName = System.getenv("BIOSIM") + File.separator + "gui" + File.separator + "icons"
				+ File.separator + "export.jpg";
		exportButton = makeToolButton(imgName, "export", "Export", "Export");
		toolbar.add(exportButton);
		saveButton.setEnabled(false);
		runButton.setEnabled(false);
		refreshButton.setEnabled(false);
		saveasButton.setEnabled(false);
		checkButton.setEnabled(false);
		exportButton.setEnabled(false);

		// Creates a menu for the frame
		menuBar = new JMenuBar();
		file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		importMenu = new JMenu("Import");
		exportMenu = new JMenu("Export");
		newMenu = new JMenu("New");
		saveAsMenu = new JMenu("Save As");
		view = new JMenu("View");
		viewModel = new JMenu("Model");
		tools = new JMenu("Tools");
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(view);
		menuBar.add(tools);
		menuBar.add(help);
		// menuBar.addFocusListener(this);
		// menuBar.addMouseListener(this);
		// file.addMouseListener(this);
		// edit.addMouseListener(this);
		// view.addMouseListener(this);
		// tools.addMouseListener(this);
		// help.addMouseListener(this);
		copy = new JMenuItem("Copy");
		rename = new JMenuItem("Rename");
		delete = new JMenuItem("Delete");
		manual = new JMenuItem("Manual");
		about = new JMenuItem("About");
		openProj = new JMenuItem("Open Project");
		close = new JMenuItem("Close");
		closeAll = new JMenuItem("Close All");
		pref = new JMenuItem("Preferences");
		newProj = new JMenuItem("Project");
		newCircuit = new JMenuItem("Genetic Circuit Model");
		newModel = new JMenuItem("SBML Model");
		newSpice = new JMenuItem("Spice Circuit");
		if (lema) {
			newVhdl = new JMenuItem("VHDL-AMS Model");
			newLhpn = new JMenuItem("Labeled Hybrid Petri Net");
		}
		else {
			newVhdl = new JMenuItem("VHDL Model");
			newLhpn = new JMenuItem("Petri Net");
		}
		newCsp = new JMenuItem("CSP Model");
		newHse = new JMenuItem("Handshaking Expansion");
		newUnc = new JMenuItem("Extended Burst Mode Machine");
		newRsg = new JMenuItem("Reduced State Graph");
		graph = new JMenuItem("TSD Graph");
		probGraph = new JMenuItem("Histogram");
		importSbml = new JMenuItem("SBML Model");
		importDot = new JMenuItem("Genetic Circuit Model");
		if (lema) {
			importVhdl = new JMenuItem("VHDL-AMS Model");
			importLhpn = new JMenuItem("Labeled Hybrid Petri Net");
		}
		else {
			importVhdl = new JMenuItem("VHDL Model");
			importLhpn = new JMenuItem("Petri Net");
		}
		importSpice = new JMenuItem("Spice Circuit");
		importCsp = new JMenuItem("CSP Model");
		importHse = new JMenuItem("Handshaking Expansion");
		importUnc = new JMenuItem("Extended Burst Mode Machine");
		importRsg = new JMenuItem("Reduced State Graph");
		exportCsv = new JMenuItem("Comma Separated Values (csv)");
		exportDat = new JMenuItem("Tab Delimited Data (dat)");
		exportEps = new JMenuItem("Encapsulated Postscript (eps)");
		exportJpg = new JMenuItem("JPEG (jpg)");
		exportPdf = new JMenuItem("Portable Document Format (pdf)");
		exportPng = new JMenuItem("Portable Network Graphics (png)");
		exportSvg = new JMenuItem("Scalable Vector Graphics (svg)");
		exportTsd = new JMenuItem("Time Series Data (tsd)");
		save = new JMenuItem("Save");
		saveAs = new JMenuItem("Save As");
		saveAsGcm = new JMenuItem("Genetic Circuit Model");
		saveAsGraph = new JMenuItem("Graph");
		saveAsSbml = new JMenuItem("Save SBML Model");
		saveAsTemplate = new JMenuItem("Save SBML Template");
		saveAsLhpn = new JMenuItem("LHPN");
		run = new JMenuItem("Save and Run");
		check = new JMenuItem("Save and Check");
		saveSbml = new JMenuItem("Save as SBML");
		saveTemp = new JMenuItem("Save as SBML Template");
		saveParam = new JMenuItem("Save Parameters");
		refresh = new JMenuItem("Refresh");
		export = new JMenuItem("Export");
		viewCircuit = new JMenuItem("Circuit");
		viewRules = new JMenuItem("Rules");
		viewTrace = new JMenuItem("Trace");
		viewLog = new JMenuItem("Log");
		viewModGraph = new JMenuItem("Using GraphViz");
		viewModBrowser = new JMenuItem("Using Browser");
		createAnal = new JMenuItem("Analysis Tool");
		createLearn = new JMenuItem("Learn Tool");
		createSbml = new JMenuItem("Create SBML File");
		createSynth = new JMenuItem("Synthesis Tool");
		createVer = new JMenuItem("Verification Tool");
		exit = new JMenuItem("Exit");
		copy.addActionListener(this);
		rename.addActionListener(this);
		delete.addActionListener(this);
		openProj.addActionListener(this);
		close.addActionListener(this);
		closeAll.addActionListener(this);
		pref.addActionListener(this);
		manual.addActionListener(this);
		newProj.addActionListener(this);
		newCircuit.addActionListener(this);
		newModel.addActionListener(this);
		newVhdl.addActionListener(this);
		newLhpn.addActionListener(this);
		newCsp.addActionListener(this);
		newHse.addActionListener(this);
		newUnc.addActionListener(this);
		newRsg.addActionListener(this);
		newSpice.addActionListener(this);
		exit.addActionListener(this);
		about.addActionListener(this);
		importSbml.addActionListener(this);
		importDot.addActionListener(this);
		importVhdl.addActionListener(this);
		importLhpn.addActionListener(this);
		importCsp.addActionListener(this);
		importHse.addActionListener(this);
		importUnc.addActionListener(this);
		importRsg.addActionListener(this);
		importSpice.addActionListener(this);
		exportCsv.addActionListener(this);
		exportDat.addActionListener(this);
		exportEps.addActionListener(this);
		exportJpg.addActionListener(this);
		exportPdf.addActionListener(this);
		exportPng.addActionListener(this);
		exportSvg.addActionListener(this);
		exportTsd.addActionListener(this);
		graph.addActionListener(this);
		probGraph.addActionListener(this);
		save.addActionListener(this);
		saveAs.addActionListener(this);
		saveAsSbml.addActionListener(this);
		saveAsTemplate.addActionListener(this);
		run.addActionListener(this);
		check.addActionListener(this);
		saveSbml.addActionListener(this);
		saveTemp.addActionListener(this);
		saveParam.addActionListener(this);
		export.addActionListener(this);
		viewCircuit.addActionListener(this);
		viewRules.addActionListener(this);
		viewTrace.addActionListener(this);
		viewLog.addActionListener(this);
		viewModGraph.addActionListener(this);
		viewModBrowser.addActionListener(this);
		createAnal.addActionListener(this);
		createLearn.addActionListener(this);
		createSbml.addActionListener(this);
		createSynth.addActionListener(this);
		createVer.addActionListener(this);
		save.setActionCommand("save");
		saveAs.setActionCommand("saveas");
		run.setActionCommand("run");
		check.setActionCommand("check");
		refresh.setActionCommand("refresh");
		export.setActionCommand("export");
		if (lema) {
			viewModGraph.setActionCommand("viewModel");
		}
		else {
			viewModGraph.setActionCommand("graph");
		}
		viewModBrowser.setActionCommand("browse");
		createLearn.setActionCommand("createLearn");
		createSbml.setActionCommand("createSBML");
		createSynth.setActionCommand("openSynth");
		createVer.setActionCommand("openVerify");
		ShortCutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ShortCutKey));
		rename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ShortCutKey));
		// newProj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		// ShortCutKey));
		openProj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ShortCutKey));
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ShortCutKey));
		closeAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ShortCutKey
				| KeyEvent.SHIFT_MASK));
		// saveAsMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		// ShortCutKey | KeyEvent.ALT_DOWN_MASK));
		// importMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
		// ShortCutKey | KeyEvent.ALT_DOWN_MASK));
		// exportMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
		// ShortCutKey | KeyEvent.ALT_DOWN_MASK));
		// viewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
		// ShortCutKey | KeyEvent.ALT_DOWN_MASK));
		// newCircuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
		// ShortCutKey));
		// newModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		// ShortCutKey));
		// newVhdl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
		// ShortCutKey));
		// newLhpn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
		// ShortCutKey));
		// about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
		// ShortCutKey));
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey));
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ShortCutKey));
		check.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ShortCutKey));
		pref.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ShortCutKey));
		viewCircuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		viewLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		createAnal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ShortCutKey));
		createLearn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ShortCutKey));
		Action newAction = new NewAction();
		Action saveAction = new SaveAction();
		Action importAction = new ImportAction();
		Action exportAction = new ExportAction();
		Action modelAction = new ModelAction();
		newMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ShortCutKey | KeyEvent.ALT_DOWN_MASK), "new");
		newMenu.getActionMap().put("new", newAction);
		saveAsMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey | KeyEvent.ALT_DOWN_MASK),
						"save");
		saveAsMenu.getActionMap().put("save", saveAction);
		importMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ShortCutKey | KeyEvent.ALT_DOWN_MASK),
				"import");
		importMenu.getActionMap().put("import", importAction);
		exportMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ShortCutKey | KeyEvent.ALT_DOWN_MASK),
				"export");
		exportMenu.getActionMap().put("export", exportAction);
		viewModel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey | KeyEvent.ALT_DOWN_MASK),
				"model");
		viewModel.getActionMap().put("model", modelAction);
		// graph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
		// ShortCutKey));
		// probGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
		// ShortCutKey));
		// if (!lema) {
		// importDot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		// ShortCutKey));
		// }
		// else {
		// importLhpn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		// ShortCutKey));
		// }
		// importSbml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
		// ShortCutKey));
		// importVhdl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
		// ShortCutKey));
		newMenu.setMnemonic(KeyEvent.VK_N);
		saveAsMenu.setMnemonic(KeyEvent.VK_A);
		importMenu.setMnemonic(KeyEvent.VK_I);
		exportMenu.setMnemonic(KeyEvent.VK_E);
		viewModel.setMnemonic(KeyEvent.VK_M);
		copy.setMnemonic(KeyEvent.VK_C);
		rename.setMnemonic(KeyEvent.VK_R);
		delete.setMnemonic(KeyEvent.VK_D);
		exit.setMnemonic(KeyEvent.VK_X);
		newProj.setMnemonic(KeyEvent.VK_P);
		openProj.setMnemonic(KeyEvent.VK_O);
		close.setMnemonic(KeyEvent.VK_W);
		newCircuit.setMnemonic(KeyEvent.VK_G);
		newModel.setMnemonic(KeyEvent.VK_S);
		newVhdl.setMnemonic(KeyEvent.VK_V);
		newLhpn.setMnemonic(KeyEvent.VK_L);
		newSpice.setMnemonic(KeyEvent.VK_P);
		about.setMnemonic(KeyEvent.VK_A);
		manual.setMnemonic(KeyEvent.VK_M);
		graph.setMnemonic(KeyEvent.VK_T);
		probGraph.setMnemonic(KeyEvent.VK_H);
		if (!async) {
			importDot.setMnemonic(KeyEvent.VK_G);
		}
		else {
			importLhpn.setMnemonic(KeyEvent.VK_L);
		}
		importSbml.setMnemonic(KeyEvent.VK_S);
		importVhdl.setMnemonic(KeyEvent.VK_V);
		importSpice.setMnemonic(KeyEvent.VK_P);
		save.setMnemonic(KeyEvent.VK_S);
		run.setMnemonic(KeyEvent.VK_R);
		check.setMnemonic(KeyEvent.VK_K);
		exportCsv.setMnemonic(KeyEvent.VK_C);
		exportEps.setMnemonic(KeyEvent.VK_E);
		exportDat.setMnemonic(KeyEvent.VK_D);
		exportJpg.setMnemonic(KeyEvent.VK_J);
		exportPdf.setMnemonic(KeyEvent.VK_F);
		exportPng.setMnemonic(KeyEvent.VK_G);
		exportSvg.setMnemonic(KeyEvent.VK_S);
		exportTsd.setMnemonic(KeyEvent.VK_T);
		pref.setMnemonic(KeyEvent.VK_P);
		viewModGraph.setMnemonic(KeyEvent.VK_G);
		viewModBrowser.setMnemonic(KeyEvent.VK_B);
		createAnal.setMnemonic(KeyEvent.VK_A);
		createLearn.setMnemonic(KeyEvent.VK_L);
		importDot.setEnabled(false);
		importSbml.setEnabled(false);
		importVhdl.setEnabled(false);
		importLhpn.setEnabled(false);
		importCsp.setEnabled(false);
		importHse.setEnabled(false);
		importUnc.setEnabled(false);
		importRsg.setEnabled(false);
		importSpice.setEnabled(false);
		exportMenu.setEnabled(false);
		// exportCsv.setEnabled(false);
		// exportDat.setEnabled(false);
		// exportEps.setEnabled(false);
		// exportJpg.setEnabled(false);
		// exportPdf.setEnabled(false);
		// exportPng.setEnabled(false);
		// exportSvg.setEnabled(false);
		// exportTsd.setEnabled(false);
		newCircuit.setEnabled(false);
		newModel.setEnabled(false);
		newVhdl.setEnabled(false);
		newLhpn.setEnabled(false);
		newCsp.setEnabled(false);
		newHse.setEnabled(false);
		newUnc.setEnabled(false);
		newRsg.setEnabled(false);
		newSpice.setEnabled(false);
		graph.setEnabled(false);
		probGraph.setEnabled(false);
		save.setEnabled(false);
		saveAs.setEnabled(false);
		saveAsMenu.setEnabled(false);
		run.setEnabled(false);
		check.setEnabled(false);
		saveSbml.setEnabled(false);
		saveTemp.setEnabled(false);
		saveParam.setEnabled(false);
		refresh.setEnabled(false);
		export.setEnabled(false);
		copy.setEnabled(false);
		rename.setEnabled(false);
		delete.setEnabled(false);
		viewCircuit.setEnabled(false);
		viewRules.setEnabled(false);
		viewTrace.setEnabled(false);
		viewLog.setEnabled(false);
		viewModel.setEnabled(false);
		viewModGraph.setEnabled(false);
		viewModBrowser.setEnabled(false);
		createAnal.setEnabled(false);
		createLearn.setEnabled(false);
		createSbml.setEnabled(false);
		createSynth.setEnabled(false);
		createVer.setEnabled(false);
		edit.add(copy);
		edit.add(rename);
		// edit.add(refresh);
		edit.add(delete);
		// edit.addSeparator();
		// edit.add(pref);
		file.add(newMenu);
		newMenu.add(newProj);
		if (!async) {
			newMenu.add(newCircuit);
			newMenu.add(newModel);
		}
		else if (atacs) {
			newMenu.add(newVhdl);
			newMenu.add(newLhpn);
			newMenu.add(newCsp);
			newMenu.add(newHse);
			newMenu.add(newUnc);
			newMenu.add(newRsg);
		}
		else {
			newMenu.add(newVhdl);
			newMenu.add(newLhpn);
			newMenu.add(newSpice);
		}
		newMenu.add(graph);
		newMenu.add(probGraph);
		file.add(openProj);
		// openMenu.add(openProj);
		file.addSeparator();
		file.add(close);
		file.add(closeAll);
		file.addSeparator();
		file.add(save);
		// file.add(saveAsMenu);
		if (!async) {
			saveAsMenu.add(saveAsGcm);
			saveAsMenu.add(saveAsGraph);
			saveAsMenu.add(saveAsSbml);
			saveAsMenu.add(saveAsTemplate);
		}
		else {
			saveAsMenu.add(saveAsLhpn);
			saveAsMenu.add(saveAsGraph);
		}
		file.add(saveAs);
		if (!async) {
			file.add(saveAsSbml);
			file.add(saveAsTemplate);
		}
		file.add(saveParam);
		file.add(run);
		if (!async) {
			file.add(check);
		}
		file.addSeparator();
		file.add(importMenu);
		if (!async) {
			importMenu.add(importDot);
			importMenu.add(importSbml);
		}
		else if (atacs) {
			importMenu.add(importVhdl);
			importMenu.add(importLhpn);
			importMenu.add(importCsp);
			importMenu.add(importHse);
			importMenu.add(importUnc);
			importMenu.add(importRsg);
		}
		else {
			importMenu.add(importVhdl);
			importMenu.add(importLhpn);
			importMenu.add(importSpice);
		}
		file.add(exportMenu);
		exportMenu.add(exportCsv);
		exportMenu.add(exportDat);
		exportMenu.add(exportEps);
		exportMenu.add(exportJpg);
		exportMenu.add(exportPdf);
		exportMenu.add(exportPng);
		exportMenu.add(exportSvg);
		exportMenu.add(exportTsd);
		file.addSeparator();
		// file.add(save);
		// file.add(saveAs);
		// file.add(run);
		// file.add(check);
		// if (!lema) {
		// file.add(saveParam);
		// }
		// file.addSeparator();
		// file.add(export);
		// if (!lema) {
		// file.add(saveSbml);
		// file.add(saveTemp);
		// }
		help.add(manual);
		externView = false;
		viewer = "";
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			new MacOSAboutHandler();
			new MacOSPreferencesHandler();
			new MacOSQuitHandler();
			Application application = new Application();
			application.addPreferencesMenuItem();
			application.setEnabledPreferencesMenu(true);
		}
		else {
			edit.addSeparator();
			edit.add(pref);
			file.add(exit);
			file.addSeparator();
			help.add(about);
		}
		view.add(viewCircuit);
		view.add(viewLog);
		if (async) {
			view.addSeparator();
			view.add(viewRules);
			view.add(viewTrace);
		}
		view.addSeparator();
		view.add(viewModel);
		viewModel.add(viewModGraph);
		viewModel.add(viewModBrowser);
		view.addSeparator();
		view.add(refresh);
		if (!async) {
			tools.add(createAnal);
		}
		if (!atacs) {
			tools.add(createLearn);
		}
		if (atacs) {
			tools.add(createSynth);
		}
		if (async) {
			tools.add(createVer);
		}
		// else {
		// tools.add(createSbml);
		// }
		root = null;

		// Create recent project menu items
		numberRecentProj = 0;
		recentProjects = new JMenuItem[5];
		recentProjectPaths = new String[5];
		for (int i = 0; i < 5; i++) {
			recentProjects[i] = new JMenuItem();
			recentProjects[i].addActionListener(this);
			recentProjectPaths[i] = "";
		}
		recentProjects[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ShortCutKey));
		recentProjects[0].setMnemonic(KeyEvent.VK_1);
		recentProjects[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ShortCutKey));
		recentProjects[1].setMnemonic(KeyEvent.VK_2);
		recentProjects[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ShortCutKey));
		recentProjects[2].setMnemonic(KeyEvent.VK_3);
		recentProjects[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ShortCutKey));
		recentProjects[3].setMnemonic(KeyEvent.VK_4);
		recentProjects[4].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ShortCutKey));
		recentProjects[4].setMnemonic(KeyEvent.VK_5);
		Preferences biosimrc = Preferences.userRoot();
		for (int i = 0; i < 5; i++) {
			recentProjects[i].setText(biosimrc.get("biosim.recent.project." + i, ""));
			recentProjectPaths[i] = biosimrc.get("biosim.recent.project.path." + i, "");
			if (!recentProjectPaths[i].equals("")) {
				file.add(recentProjects[i]);
				numberRecentProj = i + 1;
			}
		}
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			new MacOSAboutHandler();
			new MacOSPreferencesHandler();
			new MacOSQuitHandler();
			Application application = new Application();
			application.addPreferencesMenuItem();
			application.setEnabledPreferencesMenu(true);
		}
		else {
			// file.add(pref);
			// file.add(exit);
			help.add(about);
		}
		if (biosimrc.get("biosim.check.undeclared", "").equals("false")) {
			checkUndeclared = false;
		}
		else {
			checkUndeclared = true;
		}
		if (biosimrc.get("biosim.check.units", "").equals("false")) {
			checkUnits = false;
		}
		else {
			checkUnits = true;
		}
		if (biosimrc.get("biosim.general.file_browser", "").equals("")) {
			biosimrc.put("biosim.general.file_browser", "JFileChooser");
		}
		if (biosimrc.get("biosim.gcm.KREP_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KREP_VALUE", ".5");
		}
		if (biosimrc.get("biosim.gcm.KACT_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KACT_VALUE", ".0033");
		}
		if (biosimrc.get("biosim.gcm.KBIO_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KBIO_VALUE", ".05");
		}
		if (biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.PROMOTER_COUNT_VALUE", "2");
		}
		if (biosimrc.get("biosim.gcm.KASSOCIATION_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KASSOCIATION_VALUE", ".05");
		}
		if (biosimrc.get("biosim.gcm.KBASAL_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KBASAL_VALUE", ".0001");
		}
		if (biosimrc.get("biosim.gcm.OCR_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.OCR_VALUE", ".05");
		}
		if (biosimrc.get("biosim.gcm.KDECAY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KDECAY_VALUE", ".0075");
		}
		if (biosimrc.get("biosim.gcm.RNAP_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.RNAP_VALUE", "30");
		}
		if (biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.RNAP_BINDING_VALUE", ".033");
		}
		if (biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.STOICHIOMETRY_VALUE", "10");
		}
		if (biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.COOPERATIVITY_VALUE", "2");
		}
		if (biosimrc.get("biosim.gcm.ACTIVED_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.ACTIVED_VALUE", ".25");
		}
		if (biosimrc.get("biosim.gcm.MAX_DIMER_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.MAX_DIMER_VALUE", "1");
		}
		if (biosimrc.get("biosim.gcm.INITIAL_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.INITIAL_VALUE", "0");
		}
		if (biosimrc.get("biosim.sim.abs", "").equals("")) {
			biosimrc.put("biosim.sim.abs", "None");
		}
		if (biosimrc.get("biosim.sim.type", "").equals("")) {
			biosimrc.put("biosim.sim.type", "ODE");
		}
		if (biosimrc.get("biosim.sim.sim", "").equals("")) {
			biosimrc.put("biosim.sim.sim", "rkf45");
		}
		if (biosimrc.get("biosim.sim.limit", "").equals("")) {
			biosimrc.put("biosim.sim.limit", "100.0");
		}
		if (biosimrc.get("biosim.sim.useInterval", "").equals("")) {
			biosimrc.put("biosim.sim.useInterval", "Print Interval");
		}
		if (biosimrc.get("biosim.sim.interval", "").equals("")) {
			biosimrc.put("biosim.sim.interval", "1.0");
		}
		if (biosimrc.get("biosim.sim.step", "").equals("")) {
			biosimrc.put("biosim.sim.step", "inf");
		}
		if (biosimrc.get("biosim.sim.error", "").equals("")) {
			biosimrc.put("biosim.sim.error", "1.0E-9");
		}
		if (biosimrc.get("biosim.sim.seed", "").equals("")) {
			biosimrc.put("biosim.sim.seed", "314159");
		}
		if (biosimrc.get("biosim.sim.runs", "").equals("")) {
			biosimrc.put("biosim.sim.runs", "1");
		}
		if (biosimrc.get("biosim.sim.rapid1", "").equals("")) {
			biosimrc.put("biosim.sim.rapid1", "0.1");
		}
		if (biosimrc.get("biosim.sim.rapid2", "").equals("")) {
			biosimrc.put("biosim.sim.rapid2", "0.1");
		}
		if (biosimrc.get("biosim.sim.qssa", "").equals("")) {
			biosimrc.put("biosim.sim.qssa", "0.1");
		}
		if (biosimrc.get("biosim.sim.concentration", "").equals("")) {
			biosimrc.put("biosim.sim.concentration", "15");
		}
		if (biosimrc.get("biosim.learn.tn", "").equals("")) {
			biosimrc.put("biosim.learn.tn", "2");
		}
		if (biosimrc.get("biosim.learn.tj", "").equals("")) {
			biosimrc.put("biosim.learn.tj", "2");
		}
		if (biosimrc.get("biosim.learn.ti", "").equals("")) {
			biosimrc.put("biosim.learn.ti", "0.5");
		}
		if (biosimrc.get("biosim.learn.bins", "").equals("")) {
			biosimrc.put("biosim.learn.bins", "4");
		}
		if (biosimrc.get("biosim.learn.equaldata", "").equals("")) {
			biosimrc.put("biosim.learn.equaldata", "Equal Data Per Bins");
		}
		if (biosimrc.get("biosim.learn.autolevels", "").equals("")) {
			biosimrc.put("biosim.learn.autolevels", "Auto");
		}
		if (biosimrc.get("biosim.learn.ta", "").equals("")) {
			biosimrc.put("biosim.learn.ta", "1.15");
		}
		if (biosimrc.get("biosim.learn.tr", "").equals("")) {
			biosimrc.put("biosim.learn.tr", "0.75");
		}
		if (biosimrc.get("biosim.learn.tm", "").equals("")) {
			biosimrc.put("biosim.learn.tm", "0.0");
		}
		if (biosimrc.get("biosim.learn.tt", "").equals("")) {
			biosimrc.put("biosim.learn.tt", "0.025");
		}
		if (biosimrc.get("biosim.learn.debug", "").equals("")) {
			biosimrc.put("biosim.learn.debug", "0");
		}
		if (biosimrc.get("biosim.learn.succpred", "").equals("")) {
			biosimrc.put("biosim.learn.succpred", "Successors");
		}
		if (biosimrc.get("biosim.learn.findbaseprob", "").equals("")) {
			biosimrc.put("biosim.learn.findbaseprob", "False");
		}

		// Open .biosimrc here

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null, this, lema, atacs);
		log = new Log();
		tab = new CloseAndMaxTabbedPane(false, this);
		tab.setPreferredSize(new Dimension(1100, 550));
		tab.getPaneUI().addMouseListener(this);
		mainPanel.add(tree, "West");
		mainPanel.add(tab, "Center");
		mainPanel.add(log, "South");
		mainPanel.add(toolbar, "North");
		frame.setContentPane(mainPanel);
		frame.setJMenuBar(menuBar);
		frame.getGlassPane().setVisible(true);
		frame.getGlassPane().addMouseListener(this);
		frame.getGlassPane().addMouseMotionListener(this);
		frame.getGlassPane().addMouseWheelListener(this);
		frame.addMouseListener(this);
		menuBar.addMouseListener(this);
		frame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = frame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
			frame.setSize(frameSize);
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
			frame.setSize(frameSize);
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		frame.setLocation(x, y);
		frame.setVisible(true);
		dispatcher = new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_TYPED) {
					if (e.getKeyChar() == '') {
						if (tab.getTabCount() > 0) {
							KeyboardFocusManager.getCurrentKeyboardFocusManager()
									.removeKeyEventDispatcher(dispatcher);
							if (save(tab.getSelectedIndex()) != 0) {
								tab.remove(tab.getSelectedIndex());
							}
							KeyboardFocusManager.getCurrentKeyboardFocusManager()
									.addKeyEventDispatcher(dispatcher);
						}
					}
				}
				return false;
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
	}

	public void preferences() {
		if (!async) {
			Undeclared = new JCheckBox("Check for undeclared units in SBML");
			if (checkUndeclared) {
				Undeclared.setSelected(true);
			}
			else {
				Undeclared.setSelected(false);
			}
			Units = new JCheckBox("Check units in SBML");
			if (checkUnits) {
				Units.setSelected(true);
			}
			else {
				Units.setSelected(false);
			}
			Preferences biosimrc = Preferences.userRoot();
			JCheckBox dialog = new JCheckBox("Use File Dialog");
			if (biosimrc.get("biosim.general.file_browser", "").equals("FileDialog")) {
				dialog.setSelected(true);
			}
			else {
				dialog.setSelected(false);
			}
			final JTextField ACTIVED_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.ACTIVED_VALUE", ""));
			final JTextField KACT_VALUE = new JTextField(biosimrc.get("biosim.gcm.KACT_VALUE", ""));
			final JTextField KBASAL_VALUE = new JTextField(biosimrc.get("biosim.gcm.KBASAL_VALUE",
					""));
			final JTextField KBIO_VALUE = new JTextField(biosimrc.get("biosim.gcm.KBIO_VALUE", ""));
			final JTextField KDECAY_VALUE = new JTextField(biosimrc.get("biosim.gcm.KDECAY_VALUE",
					""));
			final JTextField COOPERATIVITY_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.COOPERATIVITY_VALUE", ""));
			final JTextField KASSOCIATION_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.KASSOCIATION_VALUE", ""));
			final JTextField RNAP_VALUE = new JTextField(biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
			final JTextField PROMOTER_COUNT_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.PROMOTER_COUNT_VALUE", ""));
			final JTextField INITIAL_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.INITIAL_VALUE", ""));
			final JTextField MAX_DIMER_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.MAX_DIMER_VALUE", ""));
			final JTextField OCR_VALUE = new JTextField(biosimrc.get("biosim.gcm.OCR_VALUE", ""));
			final JTextField RNAP_BINDING_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.RNAP_BINDING_VALUE", ""));
			final JTextField KREP_VALUE = new JTextField(biosimrc.get("biosim.gcm.KREP_VALUE", ""));
			final JTextField STOICHIOMETRY_VALUE = new JTextField(biosimrc.get(
					"biosim.gcm.STOICHIOMETRY_VALUE", ""));
			JPanel labels = new JPanel(new GridLayout(15, 1));
			labels
					.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.ACTIVED_STRING)
							+ " (" + CompatibilityFixer.getSBMLName(GlobalConstants.ACTIVED_STRING)
							+ "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KACT_STRING) + " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.KACT_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KBASAL_STRING)
					+ " (" + CompatibilityFixer.getSBMLName(GlobalConstants.KBASAL_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KBIO_STRING) + " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.KBIO_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KDECAY_STRING)
					+ " (" + CompatibilityFixer.getSBMLName(GlobalConstants.KDECAY_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer
					.getGuiName(GlobalConstants.COOPERATIVITY_STRING)
					+ " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.COOPERATIVITY_STRING)
					+ "):"));
			labels.add(new JLabel(CompatibilityFixer
					.getGuiName(GlobalConstants.KASSOCIATION_STRING)
					+ " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.KASSOCIATION_STRING)
					+ "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.RNAP_STRING) + " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.RNAP_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer
					.getGuiName(GlobalConstants.PROMOTER_COUNT_STRING)
					+ " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.PROMOTER_COUNT_STRING)
					+ "):"));
			labels
					.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.INITIAL_STRING)
							+ " (" + CompatibilityFixer.getSBMLName(GlobalConstants.INITIAL_STRING)
							+ "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.MAX_DIMER_STRING)
					+ " (" + CompatibilityFixer.getSBMLName(GlobalConstants.MAX_DIMER_STRING)
					+ "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.OCR_STRING) + " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.OCR_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer
					.getGuiName(GlobalConstants.RNAP_BINDING_STRING)
					+ " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.RNAP_BINDING_STRING)
					+ "):"));
			labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KREP_STRING) + " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.KREP_STRING) + "):"));
			labels.add(new JLabel(CompatibilityFixer
					.getGuiName(GlobalConstants.STOICHIOMETRY_STRING)
					+ " ("
					+ CompatibilityFixer.getSBMLName(GlobalConstants.STOICHIOMETRY_STRING)
					+ "):"));
			JPanel fields = new JPanel(new GridLayout(15, 1));
			fields.add(ACTIVED_VALUE);
			fields.add(KACT_VALUE);
			fields.add(KBASAL_VALUE);
			fields.add(KBIO_VALUE);
			fields.add(KDECAY_VALUE);
			fields.add(COOPERATIVITY_VALUE);
			fields.add(KASSOCIATION_VALUE);
			fields.add(RNAP_VALUE);
			fields.add(PROMOTER_COUNT_VALUE);
			fields.add(INITIAL_VALUE);
			fields.add(MAX_DIMER_VALUE);
			fields.add(OCR_VALUE);
			fields.add(RNAP_BINDING_VALUE);
			fields.add(KREP_VALUE);
			fields.add(STOICHIOMETRY_VALUE);
			JPanel gcmPrefs = new JPanel(new GridLayout(1, 2));
			gcmPrefs.add(labels);
			gcmPrefs.add(fields);
			String[] choices = { "None", "Abstraction", "Logical Abstraction" };
			final JComboBox abs = new JComboBox(choices);
			abs.setSelectedItem(biosimrc.get("biosim.sim.abs", ""));
			if (abs.getSelectedItem().equals("None")) {
				choices = new String[] { "ODE", "Monte Carlo", "SBML", "Network", "Browser" };
			}
			else if (abs.getSelectedItem().equals("Abstraction")) {
				choices = new String[] { "ODE", "Monte Carlo", "SBML", "Network", "Browser" };
			}
			else {
				choices = new String[] { "Monte Carlo", "Markov", "SBML", "Network", "Browser" };
			}
			final JComboBox type = new JComboBox(choices);
			type.setSelectedItem(biosimrc.get("biosim.sim.type", ""));
			if (type.getSelectedItem().equals("ODE")) {
				choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45" };
			}
			else if (type.getSelectedItem().equals("Monte Carlo")) {
				choices = new String[] { "gillespie", "emc-sim", "bunker", "nmc" };
			}
			else if (type.getSelectedItem().equals("Markov")) {
				choices = new String[] { "atacs", "ctmc-transient" };
			}
			else {
				choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45" };
			}
			final JComboBox sim = new JComboBox(choices);
			sim.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
			abs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (abs.getSelectedItem().equals("None")) {
						Object o = type.getSelectedItem();
						type.removeAllItems();
						type.addItem("ODE");
						type.addItem("Monte Carlo");
						type.addItem("SBML");
						type.addItem("Network");
						type.addItem("Browser");
						type.setSelectedItem(o);
					}
					else if (abs.getSelectedItem().equals("Abstraction")) {
						Object o = type.getSelectedItem();
						type.removeAllItems();
						type.addItem("ODE");
						type.addItem("Monte Carlo");
						type.addItem("SBML");
						type.addItem("Network");
						type.addItem("Browser");
						type.setSelectedItem(o);
					}
					else {
						Object o = type.getSelectedItem();
						type.removeAllItems();
						type.addItem("Monte Carlo");
						type.addItem("Markov");
						type.addItem("SBML");
						type.addItem("Network");
						type.addItem("Browser");
						type.setSelectedItem(o);
					}
				}
			});
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (type.getSelectedItem() == null) {
					}
					else if (type.getSelectedItem().equals("ODE")) {
						Object o = sim.getSelectedItem();
						sim.removeAllItems();
						sim.addItem("euler");
						sim.addItem("gear1");
						sim.addItem("gear2");
						sim.addItem("rk4imp");
						sim.addItem("rk8pd");
						sim.addItem("rkf45");
						sim.setSelectedIndex(5);
						sim.setSelectedItem(o);
					}
					else if (type.getSelectedItem().equals("Monte Carlo")) {
						Object o = sim.getSelectedItem();
						sim.removeAllItems();
						sim.addItem("gillespie");
						sim.addItem("emc-sim");
						sim.addItem("bunker");
						sim.addItem("nmc");
						sim.setSelectedItem(o);
					}
					else if (type.getSelectedItem().equals("Markov")) {
						Object o = sim.getSelectedItem();
						sim.removeAllItems();
						sim.addItem("atacs");
						sim.addItem("ctmc-transient");
						sim.setSelectedItem(o);
					}
					else {
						Object o = sim.getSelectedItem();
						sim.removeAllItems();
						sim.addItem("euler");
						sim.addItem("gear1");
						sim.addItem("gear2");
						sim.addItem("rk4imp");
						sim.addItem("rk8pd");
						sim.addItem("rkf45");
						sim.setSelectedIndex(5);
						sim.setSelectedItem(o);
					}
				}
			});
			JTextField limit = new JTextField(biosimrc.get("biosim.sim.limit", ""));
			JTextField interval = new JTextField(biosimrc.get("biosim.sim.interval", ""));
			JTextField step = new JTextField(biosimrc.get("biosim.sim.step", ""));
			JTextField error = new JTextField(biosimrc.get("biosim.sim.error", ""));
			JTextField seed = new JTextField(biosimrc.get("biosim.sim.seed", ""));
			JTextField runs = new JTextField(biosimrc.get("biosim.sim.runs", ""));
			JTextField rapid1 = new JTextField(biosimrc.get("biosim.sim.rapid1", ""));
			JTextField rapid2 = new JTextField(biosimrc.get("biosim.sim.rapid2", ""));
			JTextField qssa = new JTextField(biosimrc.get("biosim.sim.qssa", ""));
			JTextField concentration = new JTextField(biosimrc.get("biosim.sim.concentration", ""));
			choices = new String[] { "Print Interval", "Number Of Steps" };
			JComboBox useInterval = new JComboBox(choices);
			useInterval.setSelectedItem(biosimrc.get("biosim.sim.useInterval", ""));
			JPanel analysisLabels = new JPanel(new GridLayout(13, 1));
			analysisLabels.add(new JLabel("Abstraction:"));
			analysisLabels.add(new JLabel("Simulation Type:"));
			analysisLabels.add(new JLabel("Possible Simulators/Analyzers:"));
			analysisLabels.add(new JLabel("Time Limit:"));
			analysisLabels.add(useInterval);
			analysisLabels.add(new JLabel("Maximum Time Step:"));
			analysisLabels.add(new JLabel("Absolute Error:"));
			analysisLabels.add(new JLabel("Random Seed:"));
			analysisLabels.add(new JLabel("Runs:"));
			analysisLabels.add(new JLabel("Rapid Equilibrium Condition 1:"));
			analysisLabels.add(new JLabel("Rapid Equilibrium Cojdition 2:"));
			analysisLabels.add(new JLabel("QSSA Condition:"));
			analysisLabels.add(new JLabel("Max Concentration Threshold:"));
			JPanel analysisFields = new JPanel(new GridLayout(13, 1));
			analysisFields.add(abs);
			analysisFields.add(type);
			analysisFields.add(sim);
			analysisFields.add(limit);
			analysisFields.add(interval);
			analysisFields.add(step);
			analysisFields.add(error);
			analysisFields.add(seed);
			analysisFields.add(runs);
			analysisFields.add(rapid1);
			analysisFields.add(rapid2);
			analysisFields.add(qssa);
			analysisFields.add(concentration);
			JPanel analysisPrefs = new JPanel(new GridLayout(1, 2));
			analysisPrefs.add(analysisLabels);
			analysisPrefs.add(analysisFields);
			final JTextField tn = new JTextField(biosimrc.get("biosim.learn.tn", ""));
			final JTextField tj = new JTextField(biosimrc.get("biosim.learn.tj", ""));
			final JTextField ti = new JTextField(biosimrc.get("biosim.learn.ti", ""));
			choices = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
			final JComboBox bins = new JComboBox(choices);
			bins.setSelectedItem(biosimrc.get("biosim.learn.bins", ""));
			choices = new String[] { "Equal Data Per Bins", "Equal Spacing Of Bins" };
			final JComboBox equaldata = new JComboBox(choices);
			equaldata.setSelectedItem(biosimrc.get("biosim.learn.equaldata", ""));
			choices = new String[] { "Auto", "User" };
			final JComboBox autolevels = new JComboBox(choices);
			autolevels.setSelectedItem(biosimrc.get("biosim.learn.autolevels", ""));
			final JTextField ta = new JTextField(biosimrc.get("biosim.learn.ta", ""));
			final JTextField tr = new JTextField(biosimrc.get("biosim.learn.tr", ""));
			final JTextField tm = new JTextField(biosimrc.get("biosim.learn.tm", ""));
			final JTextField tt = new JTextField(biosimrc.get("biosim.learn.tt", ""));
			choices = new String[] { "0", "1", "2", "3" };
			final JComboBox debug = new JComboBox(choices);
			debug.setSelectedItem(biosimrc.get("biosim.learn.debug", ""));
			choices = new String[] { "Successors", "Predecessors", "Both" };
			final JComboBox succpred = new JComboBox(choices);
			succpred.setSelectedItem(biosimrc.get("biosim.learn.succpred", ""));
			choices = new String[] { "True", "False" };
			final JComboBox findbaseprob = new JComboBox(choices);
			findbaseprob.setSelectedItem(biosimrc.get("biosim.learn.findbaseprob", ""));
			JPanel learnLabels = new JPanel(new GridLayout(13, 1));
			learnLabels.add(new JLabel("Minimum Number Of Initial Vectors (Tn):"));
			learnLabels.add(new JLabel("Maximum Influence Vector Size (Tj):"));
			learnLabels.add(new JLabel("Score For Empty Influence Vector (Ti):"));
			learnLabels.add(new JLabel("Number Of Bins:"));
			learnLabels.add(new JLabel("Divide Bins:"));
			learnLabels.add(new JLabel("Generate Levels:"));
			learnLabels.add(new JLabel("Ratio For Activation (Ta):"));
			learnLabels.add(new JLabel("Ratio For Repression (Tr):"));
			learnLabels.add(new JLabel("Merge Influence Vectors Delta (Tm):"));
			learnLabels.add(new JLabel("Relax Thresholds Delta (Tt):"));
			learnLabels.add(new JLabel("Debug Level:"));
			learnLabels.add(new JLabel("Successors Or Predecessors:"));
			learnLabels.add(new JLabel("Basic FindBaseProb:"));
			JPanel learnFields = new JPanel(new GridLayout(13, 1));
			learnFields.add(tn);
			learnFields.add(tj);
			learnFields.add(ti);
			learnFields.add(bins);
			learnFields.add(equaldata);
			learnFields.add(autolevels);
			learnFields.add(ta);
			learnFields.add(tr);
			learnFields.add(tm);
			learnFields.add(tt);
			learnFields.add(debug);
			learnFields.add(succpred);
			learnFields.add(findbaseprob);
			JPanel learnPrefs = new JPanel(new GridLayout(1, 2));
			learnPrefs.add(learnLabels);
			learnPrefs.add(learnFields);
			JPanel generalPrefs = new JPanel(new BorderLayout());
			generalPrefs.add(dialog, "North");
			JPanel sbmlPrefsBordered = new JPanel(new BorderLayout());
			JPanel sbmlPrefs = new JPanel();
			sbmlPrefsBordered.add(Undeclared, "North");
			sbmlPrefsBordered.add(Units, "Center");
			sbmlPrefs.add(sbmlPrefsBordered);
			((FlowLayout) sbmlPrefs.getLayout()).setAlignment(FlowLayout.LEFT);
			JTabbedPane prefTabs = new JTabbedPane();
			prefTabs.addTab("General Preferences", dialog);
			prefTabs.addTab("SBML Preferences", sbmlPrefs);
			prefTabs.addTab("GCM Preferences", gcmPrefs);
			prefTabs.addTab("Analysis Preferences", analysisPrefs);
			prefTabs.addTab("Learn Preferences", learnPrefs);
			Object[] options = { "Save", "Cancel" };
			int value = JOptionPane
					.showOptionDialog(frame, prefTabs, "Preferences", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				if (dialog.isSelected()) {
					biosimrc.put("biosim.general.file_browser", "FileDialog");
				}
				else {
					biosimrc.put("biosim.general.file_browser", "JFileChooser");
				}
				if (Undeclared.isSelected()) {
					checkUndeclared = true;
					biosimrc.put("biosim.check.undeclared", "true");
				}
				else {
					checkUndeclared = false;
					biosimrc.put("biosim.check.undeclared", "false");
				}
				if (Units.isSelected()) {
					checkUnits = true;
					biosimrc.put("biosim.check.units", "true");
				}
				else {
					checkUnits = false;
					biosimrc.put("biosim.check.units", "false");
				}
				try {
					Double.parseDouble(KREP_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KREP_VALUE", KREP_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(KACT_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KACT_VALUE", KACT_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(KBIO_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KBIO_VALUE", KBIO_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(PROMOTER_COUNT_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.PROMOTER_COUNT_VALUE", PROMOTER_COUNT_VALUE.getText()
							.trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(KASSOCIATION_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KASSOCIATION_VALUE", KASSOCIATION_VALUE.getText()
							.trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(KBASAL_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KBASAL_VALUE", KBASAL_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(OCR_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.OCR_VALUE", OCR_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(KDECAY_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.KDECAY_VALUE", KDECAY_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(RNAP_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.RNAP_VALUE", RNAP_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(RNAP_BINDING_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.RNAP_BINDING_VALUE", RNAP_BINDING_VALUE.getText()
							.trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(STOICHIOMETRY_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.STOICHIOMETRY_VALUE", STOICHIOMETRY_VALUE.getText()
							.trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(COOPERATIVITY_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.COOPERATIVITY_VALUE", COOPERATIVITY_VALUE.getText()
							.trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(ACTIVED_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.ACTIVED_VALUE", ACTIVED_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(MAX_DIMER_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.MAX_DIMER_VALUE", MAX_DIMER_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(INITIAL_VALUE.getText().trim());
					biosimrc.put("biosim.gcm.INITIAL_VALUE", INITIAL_VALUE.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(limit.getText().trim());
					biosimrc.put("biosim.sim.limit", limit.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(interval.getText().trim());
					biosimrc.put("biosim.sim.interval", interval.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					if (step.getText().trim().equals("inf")) {
						biosimrc.put("biosim.sim.step", step.getText().trim());
					}
					else {
						Double.parseDouble(step.getText().trim());
						biosimrc.put("biosim.sim.step", step.getText().trim());
					}
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(error.getText().trim());
					biosimrc.put("biosim.sim.error", error.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Long.parseLong(seed.getText().trim());
					biosimrc.put("biosim.sim.seed", seed.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Integer.parseInt(runs.getText().trim());
					biosimrc.put("biosim.sim.runs", runs.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(rapid1.getText().trim());
					biosimrc.put("biosim.sim.rapid1", rapid1.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(rapid2.getText().trim());
					biosimrc.put("biosim.sim.rapid2", rapid2.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(qssa.getText().trim());
					biosimrc.put("biosim.sim.qssa", qssa.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Integer.parseInt(concentration.getText().trim());
					biosimrc.put("biosim.sim.concentration", concentration.getText().trim());
				}
				catch (Exception e1) {
				}
				biosimrc.put("biosim.sim.useInterval", (String) useInterval.getSelectedItem());
				biosimrc.put("biosim.sim.abs", (String) abs.getSelectedItem());
				biosimrc.put("biosim.sim.type", (String) type.getSelectedItem());
				biosimrc.put("biosim.sim.sim", (String) sim.getSelectedItem());
				try {
					Integer.parseInt(tn.getText().trim());
					biosimrc.put("biosim.learn.tn", tn.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Integer.parseInt(tj.getText().trim());
					biosimrc.put("biosim.learn.tj", tj.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(ti.getText().trim());
					biosimrc.put("biosim.learn.ti", ti.getText().trim());
				}
				catch (Exception e1) {
				}
				biosimrc.put("biosim.learn.bins", (String) bins.getSelectedItem());
				biosimrc.put("biosim.learn.equaldata", (String) equaldata.getSelectedItem());
				biosimrc.put("biosim.learn.autolevels", (String) autolevels.getSelectedItem());
				try {
					Double.parseDouble(ta.getText().trim());
					biosimrc.put("biosim.learn.ta", ta.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(tr.getText().trim());
					biosimrc.put("biosim.learn.tr", tr.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(tm.getText().trim());
					biosimrc.put("biosim.learn.tm", tm.getText().trim());
				}
				catch (Exception e1) {
				}
				try {
					Double.parseDouble(tt.getText().trim());
					biosimrc.put("biosim.learn.tt", tt.getText().trim());
				}
				catch (Exception e1) {
				}
				biosimrc.put("biosim.learn.debug", (String) debug.getSelectedItem());
				biosimrc.put("biosim.learn.succpred", (String) succpred.getSelectedItem());
				biosimrc.put("biosim.learn.findbaseprob", (String) findbaseprob.getSelectedItem());
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab.getTitleAt(i).contains(".gcm")) {
						((GCM2SBMLEditor) tab.getComponentAt(i)).getGCM().loadDefaultParameters();
						((GCM2SBMLEditor) tab.getComponentAt(i)).reloadParameters();
					}
				}
			}
			else {
			}
		}
		else {
			JPanel prefPanel = new JPanel(new GridLayout(0, 2));
			viewerLabel = new JLabel("External Editor for non-LHPN files:");
			viewerField = new JTextField("");
			viewerCheck = new JCheckBox("Use External Viewer");
			viewerCheck.addActionListener(this);
			viewerCheck.setSelected(externView);
			viewerField.setText(viewer);
			viewerLabel.setEnabled(externView);
			viewerField.setEnabled(externView);
			prefPanel.add(viewerLabel);
			prefPanel.add(viewerField);
			prefPanel.add(viewerCheck);
			// Preferences biosimrc = Preferences.userRoot();
			// JPanel vhdlPrefs = new JPanel();
			// JPanel lhpnPrefs = new JPanel();
			// JTabbedPane prefTabsNoLema = new JTabbedPane();
			// prefTabsNoLema.addTab("VHDL Preferences", vhdlPrefs);
			// prefTabsNoLema.addTab("LHPN Preferences", lhpnPrefs);
			Object[] options = { "Save", "Cancel" };
			int value = JOptionPane
					.showOptionDialog(frame, prefPanel, "Preferences", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				viewer = viewerField.getText();
			}
		}
	}

	public void about() {
		final JFrame f = new JFrame("About");
		// frame.setIconImage(new ImageIcon(System.getenv("BIOSIM") +
		// File.separator
		// + "gui"
		// + File.separator + "icons" + File.separator +
		// "iBioSim.png").getImage());
		JLabel bioSim = new JLabel("iBioSim", JLabel.CENTER);
		Font font = bioSim.getFont();
		font = font.deriveFont(Font.BOLD, 36.0f);
		bioSim.setFont(font);
		JLabel version = new JLabel("Version 1.02", JLabel.CENTER);
		JLabel uOfU = new JLabel("University of Utah", JLabel.CENTER);
		JButton credits = new JButton("Credits");
		credits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Close" };
				JOptionPane.showOptionDialog(f, "Nathan Barker\nKevin Jones\nHiroyuki Kuwahara\n"
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
		JPanel uOfUPanel = new JPanel(new BorderLayout());
		uOfUPanel.add(bioSim, "North");
		uOfUPanel.add(version, "Center");
		uOfUPanel.add(uOfU, "South");
		aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(System.getenv("BIOSIM")
				+ File.separator + "gui" + File.separator + "icons" + File.separator
				+ "iBioSim.png")), "North");
		// aboutPanel.add(bioSim, "North");
		aboutPanel.add(uOfUPanel, "Center");
		aboutPanel.add(buttons, "South");
		f.setContentPane(aboutPanel);
		f.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
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
	}

	public void exit() {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (save(i) == 0) {
				return;
			}
		}
		Preferences biosimrc = Preferences.userRoot();
		for (int i = 0; i < numberRecentProj; i++) {
			biosimrc.put("biosim.recent.project." + i, recentProjects[i].getText());
			biosimrc.put("biosim.recent.project.path." + i, recentProjectPaths[i]);
		}
		System.exit(1);
	}

	/**
	 * This method performs different functions depending on what menu items are
	 * selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == viewerCheck) {
			externView = viewerCheck.isSelected();
			viewerLabel.setEnabled(viewerCheck.isSelected());
			viewerField.setEnabled(viewerCheck.isSelected());
		}
		if (e.getSource() == viewCircuit) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Learn) {
					((Learn) component).viewGcm();
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).viewLhpn();
				}
			}
			else if (comp instanceof LHPNEditor) {
				((LHPNEditor) comp).viewLhpn();
			}
			else if (comp instanceof JPanel) {
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Verification) {
					((Verification) array[0]).viewCircuit();
				}
				else if (array[0] instanceof Synthesis) {
					((Synthesis) array[0]).viewCircuit();
				}
			}
		}
		else if (e.getSource() == viewLog) {
			Component comp = tab.getSelectedComponent();
			if (treeSelected) {
				try {
					if (new File(root + separator + "atacs.log").exists()) {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(frame(), "No log exists.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame(), "Unable to view log.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (comp instanceof JPanel) {
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Verification) {
					((Verification) array[0]).viewLog();
				}
				else if (array[0] instanceof Synthesis) {
					((Synthesis) array[0]).viewLog();
				}
			}
			else if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Learn) {
					((Learn) component).viewLog();
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).viewLog();
				}
			}
		}
		else if (e.getSource() == saveParam) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Learn) {
					((Learn) component).save();
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).save();
				}
				else {
					((Reb2Sac) ((JTabbedPane) comp).getComponentAt(2)).save();
				}
			}
		}
		else if (e.getSource() == saveSbml) {
			Component comp = tab.getSelectedComponent();
			((GCM2SBMLEditor) comp).save("SBML");
		}
		else if (e.getSource() == saveTemp) {
			Component comp = tab.getSelectedComponent();
			((GCM2SBMLEditor) comp).save("template");
		}
		else if (e.getSource() == saveAsGcm) {
			Component comp = tab.getSelectedComponent();
			((GCM2SBMLEditor) comp).save("GCM");
		}
		else if (e.getSource() == saveAsLhpn) {
			Component comp = tab.getSelectedComponent();
			((LHPNEditor) comp).save();
		}
		else if (e.getSource() == saveAsGraph) {
			Component comp = tab.getSelectedComponent();
			((Graph) comp).save();
		}
		else if (e.getSource() == saveAsSbml) {
			Component comp = tab.getSelectedComponent();
			((GCM2SBMLEditor) comp).save("Save as SBML");
		}
		else if (e.getSource() == saveAsTemplate) {
			Component comp = tab.getSelectedComponent();
			((GCM2SBMLEditor) comp).save("Save as SBML template");
		}
		else if (e.getSource() == close && tab.getSelectedComponent() != null) {
			Component comp = tab.getSelectedComponent();
			Point point = comp.getLocation();
			tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(),
					point.x, point.y, 0, false), tab.getSelectedIndex());
		}
		else if (e.getSource() == closeAll) {
			while (tab.getSelectedComponent() != null) {
				int index = tab.getSelectedIndex();
				Component comp = tab.getComponent(index);
				Point point = comp.getLocation();
				tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(),
						e.getModifiers(), point.x, point.y, 0, false), index);
			}
		}
		else if (e.getSource() == viewRules) {
			Component comp = tab.getSelectedComponent();
			Component[] array = ((JPanel) comp).getComponents();
			((Synthesis) array[0]).viewRules();
		}
		else if (e.getSource() == viewTrace) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JPanel) {
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Synthesis) {
					((Synthesis) array[0]).viewTrace();
				}
				else {
					((Verification) array[0]).viewTrace();
				}
			}
		}
		else if (e.getSource() == exportCsv) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(5);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(5);
			}
		}
		else if (e.getSource() == exportDat) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(6);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export();
			}
		}
		else if (e.getSource() == exportEps) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(3);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(3);
			}
		}
		else if (e.getSource() == exportJpg) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(0);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(0);
			}
		}
		else if (e.getSource() == exportPdf) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(2);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(2);
			}
		}
		else if (e.getSource() == exportPng) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(1);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(1);
			}
		}
		else if (e.getSource() == exportSvg) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(4);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(4);
			}
		}
		else if (e.getSource() == exportTsd) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export(7);
			}
			else if (comp instanceof JTabbedPane) {
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(7);
			}
		}
		else if (e.getSource() == about) {
			about();
		}
		else if (e.getSource() == manual) {
			try {
				String directory = "";
				String theFile = "";
				if (!async) {
					theFile = "iBioSim.html";
				}
				else if (atacs) {
					theFile = "ATACS.html";
				}
				else {
					theFile = "LEMA.html";
				}
				String command = "";
				if (System.getProperty("os.name").contentEquals("Linux")) {
					directory = System.getenv("BIOSIM") + "/docs/";
					command = "gnome-open ";
				}
				else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					directory = System.getenv("BIOSIM") + "/docs/";
					command = "open ";
				}
				else {
					directory = System.getenv("BIOSIM") + "\\docs\\";
					command = "cmd /c start ";
				}
				File work = new File(directory);
				log.addText("Executing:\n" + command + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec(command + theFile, null, work);
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open manual.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the exit menu item is selected
		else if (e.getSource() == exit) {
			exit();
		}
		// if the open popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("openSim")) {
			openSim();
		}
		else if (e.getActionCommand().equals("openLearn")) {
			if (lema) {
				openLearnLHPN();
			}
			else {
				openLearn();
			}
		}
		else if (e.getActionCommand().equals("openSynth")) {
			openSynth();
		}
		else if (e.getActionCommand().equals("openVerification")) {
			openVerify();
		}
		// if the create simulation popup menu is selected on a dot file
		else if (e.getActionCommand().equals("createSim")) {
			try {
				simulate(true);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame,
						"You must select a valid gcm file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the simulate popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("simulate")) {
			try {
				simulate(false);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"You must select a valid sbml file for simulation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the synthesis popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createSynthesis")) {
			if (root != null) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				String synthName = JOptionPane.showInputDialog(frame, "Enter Synthesis ID:",
						"Synthesis View ID", JOptionPane.PLAIN_MESSAGE);
				if (synthName != null && !synthName.trim().equals("")) {
					synthName = synthName.trim();
					try {
						if (overwrite(root + separator + synthName, synthName)) {
							new File(root + separator + synthName).mkdir();
							// new FileWriter(new File(root + separator +
							// synthName + separator
							// +
							// ".lrn")).close();
							String sbmlFile = tree.getFile();
							String[] getFilename = sbmlFile.split(separator);
							String circuitFileNoPath = getFilename[getFilename.length - 1];
							try {
								FileOutputStream out = new FileOutputStream(new File(root
										+ separator + synthName.trim() + separator
										+ synthName.trim() + ".syn"));
								out
										.write(("synthesis.file=" + circuitFileNoPath + "\n")
												.getBytes());
								out.close();
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame,
										"Unable to save parameter file!", "Error Saving File",
										JOptionPane.ERROR_MESSAGE);
							}
							try {
								FileInputStream in = new FileInputStream(new File(root + separator
										+ circuitFileNoPath));
								FileOutputStream out = new FileOutputStream(new File(root
										+ separator + synthName.trim() + separator
										+ circuitFileNoPath));
								int read = in.read();
								while (read != -1) {
									out.write(read);
									read = in.read();
								}
								in.close();
								out.close();
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame,
										"Unable to copy circuit file!", "Error Saving File",
										JOptionPane.ERROR_MESSAGE);
							}
							refreshTree();
							String work = root + separator + synthName;
							String circuitFile = root + separator + synthName.trim() + separator
									+ circuitFileNoPath;
							JPanel synthPane = new JPanel();
							Synthesis synth = new Synthesis(work, circuitFile, log, this);
							// synth.addMouseListener(this);
							synthPane.add(synth);
							/*
							 * JLabel noData = new JLabel("No data available");
							 * Font font = noData.getFont(); font =
							 * font.deriveFont(Font.BOLD, 42.0f);
							 * noData.setFont(font);
							 * noData.setHorizontalAlignment
							 * (SwingConstants.CENTER); lrnTab.addTab("Learn",
							 * noData);
							 * lrnTab.getComponentAt(lrnTab.getComponents
							 * ().length - 1).setName("Learn"); JLabel noData1 =
							 * new JLabel("No data available"); font =
							 * noData1.getFont(); font =
							 * font.deriveFont(Font.BOLD, 42.0f);
							 * noData1.setFont(font);
							 * noData1.setHorizontalAlignment
							 * (SwingConstants.CENTER); lrnTab.addTab("TSD
							 * Graph", noData1); lrnTab.getComponentAt
							 * (lrnTab.getComponents().length - 1).setName("TSD
							 * Graph");
							 */
							addTab(synthName, synthPane, "Synthesis");
						}
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame,
								"Unable to create Synthesis View directory.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the verify popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createVerify")) {
			if (root != null) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				String verName = JOptionPane.showInputDialog(frame, "Enter Verification ID:",
						"Verification View ID", JOptionPane.PLAIN_MESSAGE);
				if (verName != null && !verName.trim().equals("")) {
					verName = verName.trim();
					//try {
						if (overwrite(root + separator + verName, verName)) {
							new File(root + separator + verName).mkdir();
							// new FileWriter(new File(root + separator +
							// synthName + separator
							// +
							// ".lrn")).close();
							String sbmlFile = tree.getFile();
							String[] getFilename = sbmlFile.split(separator);
							String circuitFileNoPath = getFilename[getFilename.length - 1];
							try {
								FileOutputStream out = new FileOutputStream(new File(root
										+ separator + verName.trim() + separator + verName.trim()
										+ ".ver"));
								out.write(("verification.file=" + circuitFileNoPath + "\n")
										.getBytes());
								out.close();
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame,
										"Unable to save parameter file!", "Error Saving File",
										JOptionPane.ERROR_MESSAGE);
							}
							/*
							 * try { FileInputStream in = new
							 * FileInputStream(new File(root + separator +
							 * circuitFileNoPath)); FileOutputStream out = new
							 * FileOutputStream(new File(root + separator +
							 * verName.trim() + separator + circuitFileNoPath));
							 * int read = in.read(); while (read != -1) {
							 * out.write(read); read = in.read(); } in.close();
							 * out.close(); } catch (Exception e1) {
							 * JOptionPane.showMessageDialog(frame, "Unable to
							 * copy circuit file!", "Error Saving File",
							 * JOptionPane.ERROR_MESSAGE); }
							 */
							refreshTree();
							//String work = root + separator + verName;
							// log.addText(circuitFile);
							JPanel verPane = new JPanel();
							Verification verify = new Verification(root + separator + verName, verName, circuitFileNoPath, log, this,
									lema, atacs);
							// verify.addMouseListener(this);
							verify.save();
							verPane.add(verify);
							/*
							 * JLabel noData = new JLabel("No data available");
							 * Font font = noData.getFont(); font =
							 * font.deriveFont(Font.BOLD, 42.0f);
							 * noData.setFont(font);
							 * noData.setHorizontalAlignment
							 * (SwingConstants.CENTER); lrnTab.addTab("Learn",
							 * noData);
							 * lrnTab.getComponentAt(lrnTab.getComponents
							 * ().length - 1).setName("Learn"); JLabel noData1 =
							 * new JLabel("No data available"); font =
							 * noData1.getFont(); font =
							 * font.deriveFont(Font.BOLD, 42.0f);
							 * noData1.setFont(font);
							 * noData1.setHorizontalAlignment
							 * (SwingConstants.CENTER); lrnTab.addTab("TSD
							 * Graph", noData1); lrnTab.getComponentAt
							 * (lrnTab.getComponents().length - 1).setName("TSD
							 * Graph");
							 */
							addTab(verName, verPane, "Verification");
						}
					//}
					//catch (Exception e1) {
					//	JOptionPane.showMessageDialog(frame,
					//			"Unable to create Verification View directory.", "Error",
					//			JOptionPane.ERROR_MESSAGE);
					//}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the delete popup menu is selected
		else if (e.getActionCommand().contains("delete") || e.getSource() == delete) {
			if (!tree.getFile().equals(root)) {
				if (new File(tree.getFile()).isDirectory()) {
					for (int i = 0; i < tab.getTabCount(); i++) {
						if (tab.getTitleAt(i)
								.equals(
										tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1])) {
							tab.remove(i);
						}
					}
					File dir = new File(tree.getFile());
					if (dir.isDirectory()) {
						deleteDir(dir);
					}
					else {
						System.gc();
						dir.delete();
					}
					refreshTree();
				}
				else {
					String[] views = canDelete(tree.getFile().split(separator)[tree.getFile()
							.split(separator).length - 1]);
					if (views.length == 0) {
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (tab.getTitleAt(i)
									.equals(
											tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1])) {
								tab.remove(i);
							}
						}
						System.gc();
						new File(tree.getFile()).delete();
						refreshTree();
					}
					else {
						String view = "";
						for (int i = 0; i < views.length; i++) {
							if (i == views.length - 1) {
								view += views[i];
							}
							else {
								view += views[i] + "\n";
							}
						}
						String message = "Unable to delete the selected file."
								+ "\nIt is linked to the following views:\n" + view
								+ "\nDelete these views first.";
						JTextArea messageArea = new JTextArea(message);
						messageArea.setEditable(false);
						JScrollPane scroll = new JScrollPane();
						scroll.setMinimumSize(new Dimension(300, 300));
						scroll.setPreferredSize(new Dimension(300, 300));
						scroll.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame, scroll, "Unable To Delete File",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("createSBML")) {
			try {
				String[] dot = tree.getFile().split(separator);
				String sbmlFile = dot[dot.length - 1]
						.substring(0, dot[dot.length - 1].length() - 3)
						+ "sbml";
				// log.addText("Executing:\ngcm2sbml.pl " + tree.getFile() + " "
				// + root
				// + separator + sbmlFile
				// + "\n");
				// Runtime exec = Runtime.getRuntime();
				// String filename = tree.getFile();
				// String directory = "";
				// String theFile = "";
				// if (filename.lastIndexOf('/') >= 0) {
				// directory = filename.substring(0,
				// filename.lastIndexOf('/') + 1);
				// theFile = filename.substring(filename.lastIndexOf('/') + 1);
				// }
				// if (filename.lastIndexOf('\\') >= 0) {
				// directory = filename.substring(0, filename
				// .lastIndexOf('\\') + 1);
				// theFile = filename
				// .substring(filename.lastIndexOf('\\') + 1);
				// }
				// File work = new File(directory);

				GCMParser parser = new GCMParser(tree.getFile());
				GeneticNetwork network = parser.buildNetwork();
				GeneticNetwork.setRoot(root + File.separator);
				network.mergeSBML(root + separator + sbmlFile);
				refreshTree();
				boolean done = false;
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab.getTitleAt(i).equals(sbmlFile)) {
						updateOpenSBML(sbmlFile);
						tab.setSelectedIndex(i);
						done = true;
					}
				}
				if (!done) {
					SBML_Editor sbml = new SBML_Editor(root + separator + sbmlFile, null, log,
							this, null, null);
					addTab(sbmlFile, sbml, "SBML Editor");
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to create SBML file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("dotEditor")) {
			try {
				String directory = "";
				String theFile = "";
				String filename = tree.getFile();
				if (filename.lastIndexOf('/') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('/') + 1);
					theFile = filename.substring(filename.lastIndexOf('/') + 1);
				}
				if (filename.lastIndexOf('\\') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
					theFile = filename.substring(filename.lastIndexOf('\\') + 1);
				}
				int i = getTab(theFile);
				if (i != -1) {
					tab.setSelectedIndex(i);
				}
				else {
					File work = new File(directory);
					GCM2SBMLEditor gcm = new GCM2SBMLEditor(work.getAbsolutePath(), theFile, this,
							log, false, null);
					// gcm.addMouseListener(this);
					addTab(theFile, gcm, "GCM Editor");
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open gcm file editor.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		// if the edit popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("sbmlEditor")) {
			try {
				boolean done = false;
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						done = true;
					}
				}
				if (!done) {
					addTab(
							tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
							new SBML_Editor(tree.getFile(), null, log, this, null, null),
							"SBML Editor");
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "You must select a valid sbml file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the graph popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("graph")) {
			try {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				Run run = new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				run.createProperties(0, "Print Interval", 1, 1, 1, tree.getFile()
						.substring(
								0,
								tree.getFile().length()
										- (tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1].length())), 314159, 1,
						new String[0], new String[0], "tsd.printer", "amount", tree.getFile()
								.split(separator), "none", frame, tree.getFile(), 0.1, 0.1, 0.1,
						15, dummy, "", dummy, null);
				String filename = tree.getFile();
				String directory = "";
				String theFile = "";
				if (filename.lastIndexOf('/') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('/') + 1);
					theFile = filename.substring(filename.lastIndexOf('/') + 1);
				}
				if (filename.lastIndexOf('\\') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
					theFile = filename.substring(filename.lastIndexOf('\\') + 1);
				}
				File work = new File(directory);
				String out = theFile;
				if (out.length() > 4
						&& out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3
						&& out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4);
				}
				log.addText("Executing:\nreb2sac --target.encoding=dot --out=" + directory + out
						+ ".dot " + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec("reb2sac --target.encoding=dot --out=" + out + ".dot "
						+ theFile, null, work);
				String error = "";
				String output = "";
				InputStream reb = graph.getErrorStream();
				int read = reb.read();
				while (read != -1) {
					error += (char) read;
					read = reb.read();
				}
				reb.close();
				reb = graph.getInputStream();
				read = reb.read();
				while (read != -1) {
					output += (char) read;
					read = reb.read();
				}
				reb.close();
				if (!output.equals("")) {
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals("")) {
					log.addText("Errors:\n" + error + "\n");
				}
				graph.waitFor();
				if (error.equals("")) {
					if (System.getProperty("os.name").contentEquals("Linux")) {
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						log.addText("Executing:\nopen " + directory + out + ".dot\n");
						exec.exec("open " + out + ".dot", null, work);
					}
					else {
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
				}
				String remove;
				if (tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")) {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ "properties";
				}
				else {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ ".properties";
				}
				System.gc();
				new File(remove).delete();
				refreshTree();
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the browse popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("browse")) {
			try {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				Run run = new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				run.createProperties(0, "Print Interval", 1, 1, 1, tree.getFile()
						.substring(
								0,
								tree.getFile().length()
										- (tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1].length())), 314159, 1,
						new String[0], new String[0], "tsd.printer", "amount", tree.getFile()
								.split(separator), "none", frame, tree.getFile(), 0.1, 0.1, 0.1,
						15, dummy, "", dummy, null);
				String filename = tree.getFile();
				String directory = "";
				String theFile = "";
				if (filename.lastIndexOf('/') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('/') + 1);
					theFile = filename.substring(filename.lastIndexOf('/') + 1);
				}
				if (filename.lastIndexOf('\\') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
					theFile = filename.substring(filename.lastIndexOf('\\') + 1);
				}
				File work = new File(directory);
				String out = theFile;
				if (out.length() > 4
						&& out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3
						&& out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4);
				}
				log.addText("Executing:\nreb2sac --target.encoding=xhtml --out=" + directory + out
						+ ".xhtml " + tree.getFile() + "\n");
				Runtime exec = Runtime.getRuntime();
				Process browse = exec.exec("reb2sac --target.encoding=xhtml --out=" + out
						+ ".xhtml " + theFile, null, work);
				String error = "";
				String output = "";
				InputStream reb = browse.getErrorStream();
				int read = reb.read();
				while (read != -1) {
					error += (char) read;
					read = reb.read();
				}
				reb.close();
				reb = browse.getInputStream();
				read = reb.read();
				while (read != -1) {
					output += (char) read;
					read = reb.read();
				}
				reb.close();
				if (!output.equals("")) {
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals("")) {
					log.addText("Errors:\n" + error + "\n");
				}
				browse.waitFor();

				String command = "";
				if (error.equals("")) {
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						command = "open ";
					}
					else {
						command = "cmd /c start ";
					}
					log.addText("Executing:\n" + command + directory + out + ".xhtml\n");
					exec.exec(command + out + ".xhtml", null, work);
				}
				String remove;
				if (tree.getFile().substring(tree.getFile().length() - 4).equals("sbml")) {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ "properties";
				}
				else {
					remove = tree.getFile().substring(0, tree.getFile().length() - 4)
							+ ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error viewing sbml file in a browser.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the graph dot popup menu is selected
		else if (e.getActionCommand().equals("graphDot")) {
			try {
				String filename = tree.getFile();
				String directory = "";
				String theFile = "";
				if (filename.lastIndexOf('/') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('/') + 1);
					theFile = filename.substring(filename.lastIndexOf('/') + 1);
				}
				if (filename.lastIndexOf('\\') >= 0) {
					directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
					theFile = filename.substring(filename.lastIndexOf('\\') + 1);
				}
				File work = new File(directory);
				if (System.getProperty("os.name").contentEquals("Linux")) {
					log.addText("Executing:\ndotty " + directory + theFile + "\n");
					Runtime exec = Runtime.getRuntime();
					exec.exec("dotty " + theFile, null, work);
				}
				else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					log.addText("Executing:\nopen " + directory + theFile + "\n");
					Runtime exec = Runtime.getRuntime();
					exec.exec("cp " + theFile + " " + theFile + ".dot", null, work);
					exec = Runtime.getRuntime();
					exec.exec("open " + theFile + ".dot", null, work);
				}
				else {
					log.addText("Executing:\ndotty " + directory + theFile + "\n");
					Runtime exec = Runtime.getRuntime();
					exec.exec("dotty " + theFile, null, work);
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the save button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("save")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor) {
				((LHPNEditor) comp).save();
			}
			else if (comp instanceof GCM2SBMLEditor) {
				((GCM2SBMLEditor) comp).save("Save GCM");
			}
			else if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).save(false, "", true);
			}
			else if (comp instanceof Graph) {
				((Graph) comp).save();
			}
			else if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				int index = ((JTabbedPane) comp).getSelectedIndex();
				if (component instanceof Graph) {
					((Graph) component).save();
				}
				else if (component instanceof Learn) {
					((Learn) component).saveGcm();
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).saveLhpn();
				}
				else if (component instanceof DataManager) {
					((DataManager) component).saveChanges(((JTabbedPane) comp).getTitleAt(index));
				}
				else if (component instanceof SBML_Editor) {
					((SBML_Editor) component).save(false, "", true);
				}
				else if (component instanceof Reb2Sac) {
					((Reb2Sac) component).save();
				}
			}
			if (comp instanceof JPanel) {
				if (comp.getName().equals("Verification")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Verification) array[0]).save();
				}
				else if (comp.getName().equals("Synthesis")) {
					// ((Synthesis) tab.getSelectedComponent()).save();
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
				}
			}
			else if (comp instanceof JScrollPane) {
				String fileName = tab.getTitleAt(tab.getSelectedIndex());
				try {
					File output = new File(root + separator + fileName);
					output.createNewFile();
					FileOutputStream outStream = new FileOutputStream(output);
					Component[] array = ((JScrollPane) comp).getComponents();
					array = ((JViewport) array[0]).getComponents();
					if (array[0] instanceof JTextArea) {
						String text = ((JTextArea) array[0]).getText();
						char[] chars = text.toCharArray();
						for (int j = 0; j < chars.length; j++) {
							outStream.write((int) chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + separator + fileName);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error saving file " + fileName, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the save as button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("saveas")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor) {
				String newName = JOptionPane.showInputDialog(frame(), "Enter LHPN name:",
						"LHPN Name", JOptionPane.PLAIN_MESSAGE);
				if (newName == null) {
					return;
				}
				if (!newName.endsWith(".g")) {
					newName = newName + ".g";
				}
				((LHPNEditor) comp).saveAs(newName);
			}
			else if (comp instanceof GCM2SBMLEditor) {
				String newName = JOptionPane.showInputDialog(frame(), "Enter GCM name:",
						"GCM Name", JOptionPane.PLAIN_MESSAGE);
				if (newName == null) {
					return;
				}
				if (newName.contains(".gcm")) {
					newName = newName.replace(".gcm", "");
				}
				((GCM2SBMLEditor) comp).saveAs(newName);
			}
			else if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).saveAs();
			}
			else if (comp instanceof Graph) {
				((Graph) comp).saveAs();
			}
			else if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph) {
					((Graph) component).saveAs();
				}
			}
			else if (comp instanceof JPanel) {
				if (comp.getName().equals("Verification")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Verification) array[0]).saveAs();
				}
				else if (comp.getName().equals("Synthesis")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).saveAs();
				}
			}
			else if (comp instanceof JScrollPane) {
				String fileName = tab.getTitleAt(tab.getSelectedIndex());
				String newName = "";
				if (fileName.endsWith(".vhd")) {
					newName = JOptionPane.showInputDialog(frame(), "Enter VHDL name:", "VHDL Name",
							JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".vhd")) {
						newName = newName + ".vhd";
					}
				}
				else if (fileName.endsWith(".csp")) {
					newName = JOptionPane.showInputDialog(frame(), "Enter CSP name:", "CSP Name",
							JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".csp")) {
						newName = newName + ".csp";
					}
				}
				else if (fileName.endsWith(".hse")) {
					newName = JOptionPane.showInputDialog(frame(), "Enter HSE name:", "HSE Name",
							JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".hse")) {
						newName = newName + ".hse";
					}
				}
				else if (fileName.endsWith(".unc")) {
					newName = JOptionPane.showInputDialog(frame(), "Enter UNC name:", "UNC Name",
							JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".unc")) {
						newName = newName + ".unc";
					}
				}
				else if (fileName.endsWith(".rsg")) {
					newName = JOptionPane.showInputDialog(frame(), "Enter RSG name:", "RSG Name",
							JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".rsg")) {
						newName = newName + ".rsg";
					}
				}
				try {
					File output = new File(root + separator + newName);
					output.createNewFile();
					FileOutputStream outStream = new FileOutputStream(output);
					Component[] array = ((JScrollPane) comp).getComponents();
					array = ((JViewport) array[0]).getComponents();
					if (array[0] instanceof JTextArea) {
						String text = ((JTextArea) array[0]).getText();
						char[] chars = text.toCharArray();
						for (int j = 0; j < chars.length; j++) {
							outStream.write((int) chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + separator + newName);
					File oldFile = new File(root + separator + fileName);
					oldFile.delete();
					tab.setTitleAt(tab.getSelectedIndex(), newName);
					refreshTree();
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error saving file " + newName, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the run button is selected on the tool bar
		else if (e.getActionCommand().equals("run")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				int index = -1;
				for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++) {
					if (((JTabbedPane) comp).getComponent(i) instanceof Reb2Sac) {
						index = i;
						break;
					}
				}
				if (component instanceof Graph) {
					if (index != -1) {
						((Reb2Sac) (((JTabbedPane) comp).getComponent(index))).getRunButton()
								.doClick();
					}
					else {
						((Graph) component).save();
						((Graph) component).run();
					}
				}
				else if (component instanceof Learn) {
					((Learn) component).save();
					new Thread((Learn) component).start();
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).save();
					((LearnLHPN) component).learn();
				}
				else if (component instanceof SBML_Editor) {
					((Reb2Sac) (((JTabbedPane) comp).getComponent(index))).getRunButton().doClick();
				}
				else if (component instanceof Reb2Sac) {
					((Reb2Sac) (((JTabbedPane) comp).getComponent(index))).getRunButton().doClick();
				}
				else if (component instanceof JPanel) {
					((Reb2Sac) (((JTabbedPane) comp).getComponent(index))).getRunButton().doClick();
				}
				else if (component instanceof JScrollPane) {
					((Reb2Sac) (((JTabbedPane) comp).getComponent(index))).getRunButton().doClick();
				}
			}
			else if (comp instanceof JPanel) {
				if (comp.getName().equals("Verification")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Verification) array[0]).save();
					((Verification) array[0]).run();
				}
				else if (comp.getName().equals("Synthesis")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
					((Synthesis) array[0]).run();
				}
			}
		}
		else if (e.getActionCommand().equals("refresh")) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph) {
					((Graph) component).refresh();
				}
			}
		}
		else if (e.getActionCommand().equals("check")) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).save(true, "", true);
				((SBML_Editor) comp).check();
			}
		}
		else if (e.getActionCommand().equals("export")) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export();
			}
			else if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph) {
					((Graph) component).export();
				}
			}
		}
		// if the new menu item is selected
		else if (e.getSource() == newProj) {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (save(i) == 0) {
					return;
				}
			}
			String filename = Buttons.browse(frame, null, null, JFileChooser.DIRECTORIES_ONLY,
					"New", -1);
			if (!filename.trim().equals("")) {
				filename = filename.trim();
				File f = new File(filename);
				if (f.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame, "File already exists."
							+ "\nDo you want to overwrite?", "Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						File dir = new File(filename);
						if (dir.isDirectory()) {
							deleteDir(dir);
						}
						else {
							System.gc();
							dir.delete();
						}
					}
					else {
						return;
					}
				}
				new File(filename).mkdir();
				try {
					new FileWriter(new File(filename + separator + ".prj")).close();
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable create a new project.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				root = filename;
				refresh();
				tab.removeAll();
				addRecentProject(filename);

				importDot.setEnabled(true);
				importSbml.setEnabled(true);
				importVhdl.setEnabled(true);
				importLhpn.setEnabled(true);
				importCsp.setEnabled(true);
				importHse.setEnabled(true);
				importUnc.setEnabled(true);
				importRsg.setEnabled(true);
				importSpice.setEnabled(true);
				newCircuit.setEnabled(true);
				newModel.setEnabled(true);
				newVhdl.setEnabled(true);
				newLhpn.setEnabled(true);
				newCsp.setEnabled(true);
				newHse.setEnabled(true);
				newUnc.setEnabled(true);
				newRsg.setEnabled(true);
				newSpice.setEnabled(true);
				graph.setEnabled(true);
				probGraph.setEnabled(true);
			}
		}
		// if the open project menu item is selected
		else if (e.getSource() == pref) {
			preferences();
		}
		else if ((e.getSource() == openProj) || (e.getSource() == recentProjects[0])
				|| (e.getSource() == recentProjects[1]) || (e.getSource() == recentProjects[2])
				|| (e.getSource() == recentProjects[3]) || (e.getSource() == recentProjects[4])) {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (save(i) == 0) {
					return;
				}
			}
			File f;
			if (root == null) {
				f = null;
			}
			else {
				f = new File(root);
			}
			String projDir = "";
			if (e.getSource() == openProj) {
				projDir = Buttons.browse(frame, f, null, JFileChooser.DIRECTORIES_ONLY, "Open", -1);
			}
			else if (e.getSource() == recentProjects[0]) {
				projDir = recentProjectPaths[0];
			}
			else if (e.getSource() == recentProjects[1]) {
				projDir = recentProjectPaths[1];
			}
			else if (e.getSource() == recentProjects[2]) {
				projDir = recentProjectPaths[2];
			}
			else if (e.getSource() == recentProjects[3]) {
				projDir = recentProjectPaths[3];
			}
			else if (e.getSource() == recentProjects[4]) {
				projDir = recentProjectPaths[4];
			}
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
						addRecentProject(projDir);
						importDot.setEnabled(true);
						importSbml.setEnabled(true);
						importVhdl.setEnabled(true);
						importLhpn.setEnabled(true);
						importCsp.setEnabled(true);
						importHse.setEnabled(true);
						importUnc.setEnabled(true);
						importRsg.setEnabled(true);
						importSpice.setEnabled(true);
						newCircuit.setEnabled(true);
						newModel.setEnabled(true);
						newVhdl.setEnabled(true);
						newLhpn.setEnabled(true);
						newCsp.setEnabled(true);
						newHse.setEnabled(true);
						newUnc.setEnabled(true);
						newRsg.setEnabled(true);
						newSpice.setEnabled(true);
						graph.setEnabled(true);
						probGraph.setEnabled(true);
					}
					else {
						JOptionPane.showMessageDialog(frame, "You must select a valid project.",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(frame, "You must select a valid project.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the new circuit model menu item is selected
		else if (e.getSource() == newCircuit) {
			if (root != null) {
				try {
					String simName = JOptionPane.showInputDialog(frame, "Enter GCM Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (simName != null && !simName.trim().equals("")) {
						simName = simName.trim();
						if (simName.length() > 4) {
							if (!simName.substring(simName.length() - 4).equals(".gcm")) {
								simName += ".gcm";
							}
						}
						else {
							simName += ".gcm";
						}
						String modelID = "";
						if (simName.length() > 3) {
							if (simName.substring(simName.length() - 4).equals(".gcm")) {
								modelID = simName.substring(0, simName.length() - 4);
							}
							else {
								modelID = simName.substring(0, simName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							if (overwrite(root + separator + simName, simName)) {
								File f = new File(root + separator + simName);
								f.createNewFile();
								new GCMFile().save(f.getAbsolutePath());
								int i = getTab(f.getName());
								if (i != -1) {
									tab.remove(i);
								}
								GCM2SBMLEditor gcm = new GCM2SBMLEditor(root + separator, f
										.getName(), this, log, false, null);
								// gcm.addMouseListener(this);
								addTab(f.getName(), gcm, "GCM Editor");
								refreshTree();
							}
						}
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the new SBML model menu item is selected
		else if (e.getSource() == newModel) {
			if (root != null) {
				try {
					String simName = JOptionPane.showInputDialog(frame, "Enter SBML Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (simName != null && !simName.trim().equals("")) {
						simName = simName.trim();
						if (simName.length() > 4) {
							if (!simName.substring(simName.length() - 5).equals(".sbml")
									&& !simName.substring(simName.length() - 4).equals(".xml")) {
								simName += ".xml";
							}
						}
						else {
							simName += ".xml";
						}
						String modelID = "";
						if (simName.length() > 4) {
							if (simName.substring(simName.length() - 5).equals(".sbml")) {
								modelID = simName.substring(0, simName.length() - 5);
							}
							else {
								modelID = simName.substring(0, simName.length() - 4);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							if (overwrite(root + separator + simName, simName)) {
								String f = new String(root + separator + simName);
								SBMLDocument document = new SBMLDocument();
								document.createModel();
								// document.setLevel(2);
								document.setLevelAndVersion(2, 3);
								Compartment c = document.getModel().createCompartment();
								c.setId("default");
								c.setSize(1.0);
								document.getModel().setId(modelID);
								SBMLWriter writer = new SBMLWriter();
								writer.writeSBML(document, root + separator + simName);
								SBML_Editor sbml = new SBML_Editor(f, null, log, this, null, null);
								// sbml.addMouseListener(this);
								addTab(f.split(separator)[f.split(separator).length - 1], sbml,
										"SBML Editor");
								refreshTree();
							}
						}
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new vhdl menu item is selected
		else if (e.getSource() == newVhdl) {
			if (root != null) {
				try {
					String vhdlName = JOptionPane.showInputDialog(frame, "Enter VHDL Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (vhdlName != null && !vhdlName.trim().equals("")) {
						vhdlName = vhdlName.trim();
						if (vhdlName.length() > 4) {
							if (!vhdlName.substring(vhdlName.length() - 5).equals(".vhd")) {
								vhdlName += ".vhd";
							}
						}
						else {
							vhdlName += ".vhd";
						}
						String modelID = "";
						if (vhdlName.length() > 3) {
							if (vhdlName.substring(vhdlName.length() - 4).equals(".vhd")) {
								modelID = vhdlName.substring(0, vhdlName.length() - 4);
							}
							else {
								modelID = vhdlName.substring(0, vhdlName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + vhdlName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ vhdlName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(vhdlName, scroll, "VHDL Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}
		// if the new lhpn menu item is selected
		else if (e.getSource() == newLhpn) {
			if (root != null) {
				try {
					String lhpnName = JOptionPane.showInputDialog(frame, "Enter LHPN Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (lhpnName != null && !lhpnName.trim().equals("")) {
						lhpnName = lhpnName.trim();
						if (lhpnName.length() > 1) {
							if (!lhpnName.substring(lhpnName.length() - 2).equals(".g")) {
								lhpnName += ".g";
							}
						}
						else {
							lhpnName += ".g";
						}
						String modelID = "";
						if (lhpnName.length() > 1) {
							if (lhpnName.substring(lhpnName.length() - 2).equals(".g")) {
								modelID = lhpnName.substring(0, lhpnName.length() - 2);
							}
							else {
								modelID = lhpnName.substring(0, lhpnName.length() - 1);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							if (overwrite(root + separator + lhpnName, lhpnName)) {
								File f = new File(root + separator + lhpnName);
								f.delete();
								f.createNewFile();
								new LHPNFile(log).save(f.getAbsolutePath());
								int i = getTab(f.getName());
								if (i != -1) {
									tab.remove(i);
								}
								addTab(f.getName(), new LHPNEditor(root + separator, f.getName(),
										null, this, log), "LHPN Editor");
								refreshTree();
							}
							if (overwrite(root + separator + lhpnName, lhpnName)) {
								File f = new File(root + separator + lhpnName);
								f.createNewFile();
								new LHPNFile(log).save(f.getAbsolutePath());
								int i = getTab(f.getName());
								if (i != -1) {
									tab.remove(i);
								}
								LHPNEditor lhpn = new LHPNEditor(root + separator, f.getName(),
										null, this, log);
								// lhpn.addMouseListener(this);
								addTab(f.getName(), lhpn, "LHPN Editor");
								refreshTree();
							}
							// File f = new File(root + separator + lhpnName);
							// f.createNewFile();
							// String[] command = { "emacs", f.getName() };
							// Runtime.getRuntime().exec(command);
							// refreshTree();
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new csp menu item is selected
		else if (e.getSource() == newCsp) {
			if (root != null) {
				try {
					String cspName = JOptionPane.showInputDialog(frame, "Enter CSP Model ID:",
							"Model ID", JOptionPane.PLAIN_MESSAGE);
					if (cspName != null && !cspName.trim().equals("")) {
						cspName = cspName.trim();
						if (cspName.length() > 3) {
							if (!cspName.substring(cspName.length() - 4).equals(".csp")) {
								cspName += ".csp";
							}
						}
						else {
							cspName += ".csp";
						}
						String modelID = "";
						if (cspName.length() > 3) {
							if (cspName.substring(cspName.length() - 4).equals(".csp")) {
								modelID = cspName.substring(0, cspName.length() - 4);
							}
							else {
								modelID = cspName.substring(0, cspName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + cspName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ cspName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(cspName, scroll, "CSP Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new hse menu item is selected
		else if (e.getSource() == newHse) {
			if (root != null) {
				try {
					String hseName = JOptionPane.showInputDialog(frame,
							"Enter Handshaking Expansion Model ID:", "Model ID",
							JOptionPane.PLAIN_MESSAGE);
					if (hseName != null && !hseName.trim().equals("")) {
						hseName = hseName.trim();
						if (hseName.length() > 3) {
							if (!hseName.substring(hseName.length() - 4).equals(".hse")) {
								hseName += ".hse";
							}
						}
						else {
							hseName += ".hse";
						}
						String modelID = "";
						if (hseName.length() > 3) {
							if (hseName.substring(hseName.length() - 4).equals(".hse")) {
								modelID = hseName.substring(0, hseName.length() - 4);
							}
							else {
								modelID = hseName.substring(0, hseName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + hseName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ hseName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(hseName, scroll, "HSE Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new unc menu item is selected
		else if (e.getSource() == newUnc) {
			if (root != null) {
				try {
					String uncName = JOptionPane.showInputDialog(frame,
							"Enter Extended Burst Mode Machine ID:", "Model ID",
							JOptionPane.PLAIN_MESSAGE);
					if (uncName != null && !uncName.trim().equals("")) {
						uncName = uncName.trim();
						if (uncName.length() > 3) {
							if (!uncName.substring(uncName.length() - 4).equals(".unc")) {
								uncName += ".unc";
							}
						}
						else {
							uncName += ".unc";
						}
						String modelID = "";
						if (uncName.length() > 3) {
							if (uncName.substring(uncName.length() - 4).equals(".unc")) {
								modelID = uncName.substring(0, uncName.length() - 4);
							}
							else {
								modelID = uncName.substring(0, uncName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + uncName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ uncName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(uncName, scroll, "UNC Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newRsg) {
			if (root != null) {
				try {
					String rsgName = JOptionPane.showInputDialog(frame,
							"Enter Reduced State Graph Model ID:", "Model ID",
							JOptionPane.PLAIN_MESSAGE);
					if (rsgName != null && !rsgName.trim().equals("")) {
						rsgName = rsgName.trim();
						if (rsgName.length() > 3) {
							if (!rsgName.substring(rsgName.length() - 4).equals(".rsg")) {
								rsgName += ".rsg";
							}
						}
						else {
							rsgName += ".rsg";
						}
						String modelID = "";
						if (rsgName.length() > 3) {
							if (rsgName.substring(rsgName.length() - 4).equals(".rsg")) {
								modelID = rsgName.substring(0, rsgName.length() - 4);
							}
							else {
								modelID = rsgName.substring(0, rsgName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + rsgName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ rsgName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(rsgName, scroll, "RSG Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newSpice) {
			if (root != null) {
				try {
					String spiceName = JOptionPane.showInputDialog(frame,
							"Enter Spice Circuit ID:", "Circuit ID", JOptionPane.PLAIN_MESSAGE);
					if (spiceName != null && !spiceName.trim().equals("")) {
						spiceName = spiceName.trim();
						if (spiceName.length() > 3) {
							if (!spiceName.substring(spiceName.length() - 4).equals(".cir")) {
								spiceName += ".cir";
							}
						}
						else {
							spiceName += ".cir";
						}
						String modelID = "";
						if (spiceName.length() > 3) {
							if (spiceName.substring(spiceName.length() - 4).equals(".cir")) {
								modelID = spiceName.substring(0, spiceName.length() - 4);
							}
							else {
								modelID = spiceName.substring(0, spiceName.length() - 3);
							}
						}
						if (!(IDpat.matcher(modelID).matches())) {
							JOptionPane
									.showMessageDialog(
											frame,
											"A model ID can only contain letters, numbers, and underscores.",
											"Invalid ID", JOptionPane.ERROR_MESSAGE);
						}
						else {
							File f = new File(root + separator + spiceName);
							f.createNewFile();
							if (externView) {
								String command = viewerField.getText() + " " + root + separator
										+ spiceName;
								Runtime exec = Runtime.getRuntime();
								try {
									exec.exec(command);
								}
								catch (Exception e1) {
									JOptionPane.showMessageDialog(frame,
											"Unable to open external editor.",
											"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								JTextArea text = new JTextArea("");
								text.setEditable(true);
								text.setLineWrap(true);
								JScrollPane scroll = new JScrollPane(text);
								// gcm.addMouseListener(this);
								addTab(spiceName, scroll, "Spice Editor");
							}
						}
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import sbml menu item is selected
		else if (e.getSource() == importSbml) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_AND_DIRECTORIES, "Import SBML", -1);
				if (!filename.trim().equals("")) {
					if (new File(filename.trim()).isDirectory()) {
						JTextArea messageArea = new JTextArea();
						messageArea.append("Imported SBML files contain the errors listed below. ");
						messageArea
								.append("It is recommended that you fix them before using these models or you may get unexpected results.\n\n");
						boolean display = false;
						for (String s : new File(filename.trim()).list()) {
							try {
								SBMLReader reader = new SBMLReader();
								SBMLDocument document = reader.readSBML(filename.trim() + separator
										+ s);
								if (document.getNumErrors() == 0) {
									if (overwrite(root + separator + s, s)) {
										long numErrors = document.checkConsistency();
										if (numErrors > 0) {
											display = true;
											messageArea
													.append("--------------------------------------------------------------------------------------\n");
											messageArea.append(s);
											messageArea
													.append("\n--------------------------------------------------------------------------------------\n\n");
											for (long i = 0; i < numErrors; i++) {
												String error = document.getError(i).getMessage(); // .
												// replace
												// (
												// "."
												// ,
												// ".\n"
												// )
												// ;
												messageArea.append(i + ":" + error + "\n");
											}
										}
										// FileOutputStream out = new
										// FileOutputStream(new File(root
										// + separator + s));
										SBMLWriter writer = new SBMLWriter();
										writer.writeSBML(document, root + separator + s);
										// String doc =
										// writer.writeToString(document);
										// byte[] output = doc.getBytes();
										// out.write(output);
										// out.close();
									}
								}
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame, "Unable to import files.",
										"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
						refreshTree();
						if (display) {
							final JFrame f = new JFrame("SBML Errors and Warnings");
							messageArea.setLineWrap(true);
							messageArea.setEditable(false);
							messageArea.setSelectionStart(0);
							messageArea.setSelectionEnd(0);
							JScrollPane scroll = new JScrollPane();
							scroll.setMinimumSize(new Dimension(600, 600));
							scroll.setPreferredSize(new Dimension(600, 600));
							scroll.setViewportView(messageArea);
							JButton close = new JButton("Dismiss");
							close.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									f.dispose();
								}
							});
							JPanel consistencyPanel = new JPanel(new BorderLayout());
							consistencyPanel.add(scroll, "Center");
							consistencyPanel.add(close, "South");
							f.setContentPane(consistencyPanel);
							f.pack();
							Dimension screenSize;
							try {
								Toolkit tk = Toolkit.getDefaultToolkit();
								screenSize = tk.getScreenSize();
							}
							catch (AWTError awe) {
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
						}
					}
					else {
						String[] file = filename.trim().split(separator);
						try {
							SBMLReader reader = new SBMLReader();
							SBMLDocument document = reader.readSBML(filename.trim());
							if (document.getNumErrors() > 0) {
								JOptionPane.showMessageDialog(frame, "Invalid SBML file.", "Error",
										JOptionPane.ERROR_MESSAGE);
							}
							else {
								if (overwrite(root + separator + file[file.length - 1],
										file[file.length - 1])) {
									long numErrors = document.checkConsistency();
									if (numErrors > 0) {
										final JFrame f = new JFrame("SBML Errors and Warnings");
										JTextArea messageArea = new JTextArea();
										messageArea
												.append("Imported SBML file contains the errors listed below. ");
										messageArea
												.append("It is recommended that you fix them before using this model or you may get unexpected results.\n\n");
										for (long i = 0; i < numErrors; i++) {
											String error = document.getError(i).getMessage(); // .
											// replace
											// (
											// "."
											// ,
											// ".\n"
											// )
											// ;
											messageArea.append(i + ":" + error + "\n");
										}
										messageArea.setLineWrap(true);
										messageArea.setEditable(false);
										messageArea.setSelectionStart(0);
										messageArea.setSelectionEnd(0);
										JScrollPane scroll = new JScrollPane();
										scroll.setMinimumSize(new Dimension(600, 600));
										scroll.setPreferredSize(new Dimension(600, 600));
										scroll.setViewportView(messageArea);
										JButton close = new JButton("Dismiss");
										close.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												f.dispose();
											}
										});
										JPanel consistencyPanel = new JPanel(new BorderLayout());
										consistencyPanel.add(scroll, "Center");
										consistencyPanel.add(close, "South");
										f.setContentPane(consistencyPanel);
										f.pack();
										Dimension screenSize;
										try {
											Toolkit tk = Toolkit.getDefaultToolkit();
											screenSize = tk.getScreenSize();
										}
										catch (AWTError awe) {
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
									}
									// FileOutputStream out = new
									// FileOutputStream(new File(root
									// + separator + file[file.length - 1]));
									SBMLWriter writer = new SBMLWriter();
									writer.writeSBML(document, root + separator
											+ file[file.length - 1]);
									// String doc =
									// writer.writeToString(document);
									// byte[] output = doc.getBytes();
									// out.write(output);
									// out.close();
									refreshTree();
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import dot menu item is selected
		else if (e.getSource() == importDot) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_AND_DIRECTORIES, "Import Genetic Circuit", -1);
				if (new File(filename.trim()).isDirectory()) {
					for (String s : new File(filename.trim()).list()) {
						if (!(filename.trim() + separator + s).equals("")
								&& (filename.trim() + separator + s).length() > 3
								&& (filename.trim() + separator + s).substring(
										(filename.trim() + separator + s).length() - 4,
										(filename.trim() + separator + s).length()).equals(".gcm")) {
							try {
								// GCMParser parser =
								new GCMParser((filename.trim() + separator + s));
								if (overwrite(root + separator + s, s)) {
									FileOutputStream out = new FileOutputStream(new File(root
											+ separator + s));
									FileInputStream in = new FileInputStream(new File((filename
											.trim()
											+ separator + s)));
									int read = in.read();
									while (read != -1) {
										out.write(read);
										read = in.read();
									}
									in.close();
									out.close();
									refreshTree();
								}
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame, "Unable to import file.",
										"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
				else {
					if (filename.trim().length() > 3
							&& !filename.trim().substring(filename.trim().length() - 4,
									filename.trim().length()).equals(".gcm")) {
						JOptionPane.showMessageDialog(frame,
								"You must select a valid gcm file to import.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					else if (!filename.trim().equals("")) {
						String[] file = filename.trim().split(separator);
						try {
							// GCMParser parser =
							new GCMParser(filename.trim());
							if (overwrite(root + separator + file[file.length - 1],
									file[file.length - 1])) {
								FileOutputStream out = new FileOutputStream(new File(root
										+ separator + file[file.length - 1]));
								FileInputStream in = new FileInputStream(new File(filename.trim()));
								int read = in.read();
								while (read != -1) {
									out.write(read);
									read = in.read();
								}
								in.close();
								out.close();
								refreshTree();
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import vhdl menu item is selected
		else if (e.getSource() == importVhdl) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import VHDL Model", -1);
				if (filename.length() > 3
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".vhd")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid vhdl file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import lhpn menu item is selected
		else if (e.getSource() == importLhpn) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import LHPN", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 2, filename.length()).equals(
								".g")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid lhpn file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import csp menu item is selected
		else if (e.getSource() == importCsp) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import CSP", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".csp")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid csp file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import hse menu item is selected
		else if (e.getSource() == importHse) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import HSE", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".hse")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid handshaking expansion file to import.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import unc menu item is selected
		else if (e.getSource() == importUnc) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import UNC", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".unc")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid expanded burst mode machine file to import.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import rsg menu item is selected
		else if (e.getSource() == importRsg) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import RSG", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".rsg")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid rsg file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the import spice menu item is selected
		else if (e.getSource() == importSpice) {
			if (root != null) {
				String filename = Buttons.browse(frame, new File(root), null,
						JFileChooser.FILES_ONLY, "Import Spice Circuit", -1);
				if (filename.length() > 1
						&& !filename.substring(filename.length() - 4, filename.length()).equals(
								".cir")) {
					JOptionPane.showMessageDialog(frame,
							"You must select a valid spice circuit file to import.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (!filename.equals("")) {
					String[] file = filename.split(separator);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
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
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the Graph data menu item is clicked
		else if (e.getSource() == graph) {
			if (root != null) {
				String graphName = JOptionPane.showInputDialog(frame,
						"Enter A Name For The TSD Graph:", "TSD Graph Name",
						JOptionPane.PLAIN_MESSAGE);
				if (graphName != null && !graphName.trim().equals("")) {
					graphName = graphName.trim();
					if (graphName.length() > 3) {
						if (!graphName.substring(graphName.length() - 4).equals(".grf")) {
							graphName += ".grf";
						}
					}
					else {
						graphName += ".grf";
					}
					if (overwrite(root + separator + graphName, graphName)) {
						Graph g = new Graph(null, "amount", graphName.trim().substring(0,
								graphName.length() - 4), "tsd.printer", root, "time", this, null,
								log, graphName.trim(), true, false);
						addTab(graphName.trim(), g, "TSD Graph");
						g.save();
						refreshTree();
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == probGraph) {
			if (root != null) {
				String graphName = JOptionPane.showInputDialog(frame,
						"Enter A Name For The Probability Graph:", "Probability Graph Name",
						JOptionPane.PLAIN_MESSAGE);
				if (graphName != null && !graphName.trim().equals("")) {
					graphName = graphName.trim();
					if (graphName.length() > 3) {
						if (!graphName.substring(graphName.length() - 4).equals(".prb")) {
							graphName += ".prb";
						}
					}
					else {
						graphName += ".prb";
					}
					if (overwrite(root + separator + graphName, graphName)) {
						Graph g = new Graph(null, "amount", graphName.trim().substring(0,
								graphName.length() - 4), "tsd.printer", root, "time", this, null,
								log, graphName.trim(), false, false);
						addTab(graphName.trim(), g, "Probability Graph");
						g.save();
						refreshTree();
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("createLearn")) {
			if (root != null) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				String lrnName = JOptionPane.showInputDialog(frame, "Enter Learn ID:",
						"Learn View ID", JOptionPane.PLAIN_MESSAGE);
				if (lrnName != null && !lrnName.trim().equals("")) {
					lrnName = lrnName.trim();
					// try {
					if (overwrite(root + separator + lrnName, lrnName)) {
						new File(root + separator + lrnName).mkdir();
						// new FileWriter(new File(root + separator +
						// lrnName + separator
						// +
						// ".lrn")).close();
						String sbmlFile = tree.getFile();
						String[] getFilename = sbmlFile.split(separator);
						String sbmlFileNoPath = getFilename[getFilename.length - 1];
						if (sbmlFileNoPath.endsWith(".vhd")) {
							try {
								File work = new File(root);
								Runtime.getRuntime().exec("atacs -lvsl " + sbmlFileNoPath, null,
										work);
								sbmlFileNoPath = sbmlFileNoPath.replace(".vhd", ".g");
							}
							catch (IOException e1) {
								JOptionPane.showMessageDialog(frame,
										"Unable to generate LHPN from VHDL file!",
										"Error Generating File", JOptionPane.ERROR_MESSAGE);
							}
						}
						try {
							FileOutputStream out = new FileOutputStream(new File(root + separator
									+ lrnName.trim() + separator + lrnName.trim() + ".lrn"));
							if (lema) {
								out.write(("learn.file=" + sbmlFileNoPath + "\n").getBytes());
							}
							else {
								out.write(("genenet.file=" + sbmlFileNoPath + "\n").getBytes());
							}
							out.close();
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to save parameter file!",
									"Error Saving File", JOptionPane.ERROR_MESSAGE);
						}
						refreshTree();
						JTabbedPane lrnTab = new JTabbedPane();
						DataManager data = new DataManager(root + separator + lrnName, this, lema);
						lrnTab.addTab("Data Manager", data);
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName(
								"Data Manager");
						if (lema) {
							LearnLHPN learn = new LearnLHPN(root + separator + lrnName, log, this);
							lrnTab.addTab("Learn", learn);
						}
						else {
							Learn learn = new Learn(root + separator + lrnName, log, this);
							lrnTab.addTab("Learn", learn);
						}
						// learn.addMouseListener(this);
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn");
						Graph tsdGraph;
						tsdGraph = new Graph(null, "amount", lrnName + " data", "tsd.printer", root
								+ separator + lrnName, "time", this, null, log, null, true, false);
						// tsdGraph.addMouseListener(this);
						lrnTab.addTab("TSD Graph", tsdGraph);
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName(
								"TSD Graph");
						/*
						 * JLabel noData = new JLabel("No data available"); Font
						 * font = noData.getFont(); font =
						 * font.deriveFont(Font.BOLD, 42.0f);
						 * noData.setFont(font); noData.setHorizontalAlignment
						 * (SwingConstants.CENTER); lrnTab.addTab("Learn",
						 * noData); lrnTab.getComponentAt(lrnTab.getComponents
						 * ().length - 1).setName("Learn"); JLabel noData1 = new
						 * JLabel("No data available"); font =
						 * noData1.getFont(); font = font.deriveFont(Font.BOLD,
						 * 42.0f); noData1.setFont(font);
						 * noData1.setHorizontalAlignment
						 * (SwingConstants.CENTER); lrnTab.addTab("TSD Graph",
						 * noData1); lrnTab.getComponentAt
						 * (lrnTab.getComponents().length - 1).setName("TSD
						 * Graph");
						 */
						addTab(lrnName, lrnTab, null);
					}
					// }
					// catch (Exception e1) {
					// JOptionPane.showMessageDialog(frame,
					// "Unable to create Learn View directory.", "Error",
					// JOptionPane.ERROR_MESSAGE);
					// }
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("viewModel")) {
			try {
				if (tree.getFile().length() >= 2
						&& tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String[] findTheFile = filename.split("\\.");
					String theFile = findTheFile[0] + ".dot";
					File dot = new File(root + separator + theFile);
					dot.delete();
					String cmd = "atacs -cPllodpl " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process ATACS = exec.exec(cmd, null, work);
					ATACS.waitFor();
					log.addText("Executing:\n" + cmd);
					if (dot.exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + separator + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String cmd = "atacs -lvslllodpl " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process view = exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
					String[] findTheFile = filename.split("\\.");
					view.waitFor();
					// String directory = "";
					String theFile = findTheFile[0] + ".dot";
					if (new File(root + separator + theFile).exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String cmd = "atacs -lcslllodpl " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process view = exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
					view.waitFor();
					String[] findTheFile = filename.split("\\.");
					// String directory = "";
					String theFile = findTheFile[0] + ".dot";
					if (new File(root + separator + theFile).exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String cmd = "atacs -lhslllodpl " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process view = exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
					view.waitFor();
					String[] findTheFile = filename.split("\\.");
					// String directory = "";
					String theFile = findTheFile[0] + ".dot";
					if (new File(root + separator + theFile).exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String cmd = "atacs -lxodps " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process view = exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
					view.waitFor();
					String[] findTheFile = filename.split("\\.");
					// String directory = "";
					String theFile = findTheFile[0] + ".dot";
					if (new File(root + separator + theFile).exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if (tree.getFile().length() >= 4
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
					String filename = tree.getFile().split(separator)[tree.getFile().split(
							separator).length - 1];
					String cmd = "atacs -lsodps " + filename;
					File work = new File(root);
					Runtime exec = Runtime.getRuntime();
					Process view = exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
					view.waitFor();
					String[] findTheFile = filename.split("\\.");
					// String directory = "";
					String theFile = findTheFile[0] + ".dot";
					if (new File(root + separator + theFile).exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = System.getenv("BIOSIM") + "/docs/";
							command = "open ";
						}
						else {
							// directory = System.getenv("BIOSIM") + "\\docs\\";
							command = "cmd /c start ";
						}
						log.addText(command + root + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else {
						File log = new File(root + separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null) {
							messageArea.append(line);
							messageArea.append(System.getProperty("line.separator"));
						}
						input.close();
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(500, 500));
						scrolls.setPreferredSize(new Dimension(500, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame(), scrolls, "Log",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "File cannot be read", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		else if (e.getActionCommand().equals("copy") || e.getSource() == copy) {
			if (!tree.getFile().equals(root)) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab
							.getTitleAt(i)
							.equals(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i) != 1) {
							return;
						}
						break;
					}
				}
				String modelID = null;
				String copy = JOptionPane.showInputDialog(frame, "Enter A New Filename:", "Copy",
						JOptionPane.PLAIN_MESSAGE);
				if (copy != null) {
					copy = copy.trim();
				}
				else {
					return;
				}
				try {
					if (!copy.equals("")) {
						if (tree.getFile().length() >= 5
								&& tree.getFile().substring(tree.getFile().length() - 5).equals(
										".sbml")
								|| tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".xml")) {
							if (copy.length() > 4) {
								if (!copy.substring(copy.length() - 5).equals(".sbml")
										&& !copy.substring(copy.length() - 4).equals(".xml")) {
									copy += ".xml";
								}
							}
							else {
								copy += ".xml";
							}
							if (copy.length() > 4) {
								if (copy.substring(copy.length() - 5).equals(".sbml")) {
									modelID = copy.substring(0, copy.length() - 5);
								}
								else {
									modelID = copy.substring(0, copy.length() - 4);
								}
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".gcm")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".gcm")) {
									copy += ".gcm";
								}
							}
							else {
								copy += ".gcm";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".vhd")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".vhd")) {
									copy += ".vhd";
								}
							}
							else {
								copy += ".vhd";
							}
						}
						else if (tree.getFile().length() >= 2
								&& tree.getFile().substring(tree.getFile().length() - 2).equals(
										".g")) {
							if (copy.length() > 1) {
								if (!copy.substring(copy.length() - 2).equals(".g")) {
									copy += ".g";
								}
							}
							else {
								copy += ".g";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".csp")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".csp")) {
									copy += ".csp";
								}
							}
							else {
								copy += ".csp";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".hse")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".hse")) {
									copy += ".hse";
								}
							}
							else {
								copy += ".hse";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".unc")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".unc")) {
									copy += ".unc";
								}
							}
							else {
								copy += ".unc";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".rsg")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".rsg")) {
									copy += ".rsg";
								}
							}
							else {
								copy += ".rsg";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".grf")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".grf")) {
									copy += ".grf";
								}
							}
							else {
								copy += ".grf";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".prb")) {
							if (copy.length() > 3) {
								if (!copy.substring(copy.length() - 4).equals(".prb")) {
									copy += ".prb";
								}
							}
							else {
								copy += ".prb";
							}
						}
					}
					if (copy
							.equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						JOptionPane.showMessageDialog(frame, "Unable to copy file."
								+ "\nNew filename must be different than old filename.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (overwrite(root + separator + copy, copy)) {
						if (modelID != null) {
							SBMLReader reader = new SBMLReader();
							SBMLDocument document = new SBMLDocument();
							document = reader.readSBML(tree.getFile());
							document.getModel().setId(modelID);
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBML(document, root + separator + copy);
						}
						else if ((tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".gcm")
								|| tree.getFile().substring(tree.getFile().length() - 4).equals(
										".grf")
								|| tree.getFile().substring(tree.getFile().length() - 4).equals(
										".vhd")
								|| tree.getFile().substring(tree.getFile().length() - 4).equals(
										".csp")
								|| tree.getFile().substring(tree.getFile().length() - 4).equals(
										".hse")
								|| tree.getFile().substring(tree.getFile().length() - 4).equals(
										".unc") || tree.getFile().substring(
								tree.getFile().length() - 4).equals(".rsg"))
								|| (tree.getFile().length() >= 2 && tree.getFile().substring(
										tree.getFile().length() - 2).equals(".g"))) {
							FileOutputStream out = new FileOutputStream(new File(root + separator
									+ copy));
							FileInputStream in = new FileInputStream(new File(tree.getFile()));
							int read = in.read();
							while (read != -1) {
								out.write(read);
								read = in.read();
							}
							in.close();
							out.close();
						}
						else {
							boolean sim = false;
							for (String s : new File(tree.getFile()).list()) {
								if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
									sim = true;
								}
							}
							if (sim) {
								new File(root + separator + copy).mkdir();
								// new FileWriter(new File(root + separator +
								// copy +
								// separator +
								// ".sim")).close();
								String[] s = new File(tree.getFile()).list();
								for (String ss : s) {
									if (ss.length() > 4
											&& ss.substring(ss.length() - 5).equals(".sbml")
											|| ss.length() > 3
											&& ss.substring(ss.length() - 4).equals(".xml")) {
										SBMLReader reader = new SBMLReader();
										SBMLDocument document = reader.readSBML(tree.getFile()
												+ separator + ss);
										SBMLWriter writer = new SBMLWriter();
										writer.writeSBML(document, root + separator + copy
												+ separator + ss);
									}
									else if (ss.length() > 10
											&& ss.substring(ss.length() - 11).equals(".properties")) {
										FileOutputStream out = new FileOutputStream(new File(root
												+ separator + copy + separator + ss));
										FileInputStream in = new FileInputStream(new File(tree
												.getFile()
												+ separator + ss));
										int read = in.read();
										while (read != -1) {
											out.write(read);
											read = in.read();
										}
										in.close();
										out.close();
									}
									else if (ss.length() > 3
											&& (ss.substring(ss.length() - 4).equals(".tsd")
													|| ss.substring(ss.length() - 4).equals(".dat")
													|| ss.substring(ss.length() - 4).equals(".sad")
													|| ss.substring(ss.length() - 4).equals(".pms") || ss
													.substring(ss.length() - 4).equals(".sim"))
											&& !ss.equals(".sim")) {
										FileOutputStream out;
										if (ss.substring(ss.length() - 4).equals(".pms")) {
											out = new FileOutputStream(new File(root + separator
													+ copy + separator + copy + ".sim"));
										}
										else if (ss.substring(ss.length() - 4).equals(".sim")) {
											out = new FileOutputStream(new File(root + separator
													+ copy + separator + copy + ".sim"));
										}
										else {
											out = new FileOutputStream(new File(root + separator
													+ copy + separator + ss));
										}
										FileInputStream in = new FileInputStream(new File(tree
												.getFile()
												+ separator + ss));
										int read = in.read();
										while (read != -1) {
											out.write(read);
											read = in.read();
										}
										in.close();
										out.close();
									}
								}
							}
							else {
								new File(root + separator + copy).mkdir();
								String[] s = new File(tree.getFile()).list();
								for (String ss : s) {
									if (ss.length() > 3
											&& (ss.substring(ss.length() - 4).equals(".tsd") || ss
													.substring(ss.length() - 4).equals(".lrn"))) {
										FileOutputStream out;
										if (ss.substring(ss.length() - 4).equals(".lrn")) {
											out = new FileOutputStream(new File(root + separator
													+ copy + separator + copy + ".lrn"));
										}
										else {
											out = new FileOutputStream(new File(root + separator
													+ copy + separator + ss));
										}
										FileInputStream in = new FileInputStream(new File(tree
												.getFile()
												+ separator + ss));
										int read = in.read();
										while (read != -1) {
											out.write(read);
											read = in.read();
										}
										in.close();
										out.close();
									}
								}
							}
						}
						refreshTree();
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to copy file.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getActionCommand().equals("rename") || e.getSource() == rename) {
			if (!tree.getFile().equals(root)) {
				try {
					for (int i = 0; i < tab.getTabCount(); i++) {
						if (tab.getTitleAt(i)
								.equals(
										tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1])) {
							tab.setSelectedIndex(i);
							if (save(i) != 1) {
								return;
							}
							break;
						}
					}
					String modelID = null;
					String rename = JOptionPane.showInputDialog(frame, "Enter A New Filename:",
							"Rename", JOptionPane.PLAIN_MESSAGE);
					if (rename != null) {
						rename = rename.trim();
					}
					else {
						return;
					}
					if (!rename.equals("")) {
						if (tree.getFile().length() >= 5
								&& tree.getFile().substring(tree.getFile().length() - 5).equals(
										".sbml")
								|| tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".xml")) {
							if (rename.length() > 4) {
								if (!rename.substring(rename.length() - 5).equals(".sbml")
										&& !rename.substring(rename.length() - 4).equals(".xml")) {
									rename += ".xml";
								}
							}
							else {
								rename += ".xml";
							}
							if (rename.length() > 4) {
								if (rename.substring(rename.length() - 5).equals(".sbml")) {
									modelID = rename.substring(0, rename.length() - 5);
								}
								else {
									modelID = rename.substring(0, rename.length() - 4);
								}
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".gcm")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".gcm")) {
									rename += ".gcm";
								}
							}
							else {
								rename += ".gcm";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".vhd")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".vhd")) {
									rename += ".vhd";
								}
							}
							else {
								rename += ".vhd";
							}
						}
						else if (tree.getFile().length() >= 2
								&& tree.getFile().substring(tree.getFile().length() - 2).equals(
										".g")) {
							if (rename.length() > 1) {
								if (!rename.substring(rename.length() - 2).equals(".g")) {
									rename += ".g";
								}
							}
							else {
								rename += ".g";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".csp")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".csp")) {
									rename += ".csp";
								}
							}
							else {
								rename += ".csp";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".hse")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".hse")) {
									rename += ".hse";
								}
							}
							else {
								rename += ".hse";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".unc")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".unc")) {
									rename += ".unc";
								}
							}
							else {
								rename += ".unc";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".rsg")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".rsg")) {
									rename += ".rsg";
								}
							}
							else {
								rename += ".rsg";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".grf")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".grf")) {
									rename += ".grf";
								}
							}
							else {
								rename += ".grf";
							}
						}
						else if (tree.getFile().length() >= 4
								&& tree.getFile().substring(tree.getFile().length() - 4).equals(
										".prb")) {
							if (rename.length() > 3) {
								if (!rename.substring(rename.length() - 4).equals(".prb")) {
									rename += ".prb";
								}
							}
							else {
								rename += ".prb";
							}
						}
						if (rename.equals(tree.getFile().split(separator)[tree.getFile().split(
								separator).length - 1])) {
							JOptionPane.showMessageDialog(frame, "Unable to rename file."
									+ "\nNew filename must be different than old filename.",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (overwrite(root + separator + rename, rename)) {
							if (tree.getFile().length() >= 5
									&& tree.getFile().substring(tree.getFile().length() - 5)
											.equals(".sbml")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".xml")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".gcm")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".vhd")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".csp")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".hse")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".unc")
									|| tree.getFile().length() >= 4
									&& tree.getFile().substring(tree.getFile().length() - 4)
											.equals(".rsg")) {
								String oldName = tree.getFile().split(separator)[tree.getFile()
										.split(separator).length - 1];
								reassignViews(oldName, rename);
							}
							new File(tree.getFile()).renameTo(new File(root + separator + rename));
							if (modelID != null) {
								SBMLReader reader = new SBMLReader();
								SBMLDocument document = new SBMLDocument();
								document = reader.readSBML(root + separator + rename);
								document.getModel().setId(modelID);
								SBMLWriter writer = new SBMLWriter();
								writer.writeSBML(document, root + separator + rename);
							}
							if (rename.length() >= 5
									&& rename.substring(rename.length() - 5).equals(".sbml")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".xml")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".gcm")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".vhd")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".csp")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".hse")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".unc")
									|| rename.length() >= 4
									&& rename.substring(rename.length() - 4).equals(".rsg")) {
								updateViews(rename);
							}
							if (new File(root + separator + rename).isDirectory()) {
								if (new File(root
										+ separator
										+ rename
										+ separator
										+ tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1] + ".sim").exists()) {
									new File(root
											+ separator
											+ rename
											+ separator
											+ tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1] + ".sim")
											.renameTo(new File(root + separator + rename
													+ separator + rename + ".sim"));
								}
								else if (new File(root
										+ separator
										+ rename
										+ separator
										+ tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1] + ".pms").exists()) {
									new File(root
											+ separator
											+ rename
											+ separator
											+ tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1] + ".pms")
											.renameTo(new File(root + separator + rename
													+ separator + rename + ".sim"));
								}
								if (new File(root
										+ separator
										+ rename
										+ separator
										+ tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1] + ".lrn").exists()) {
									new File(root
											+ separator
											+ rename
											+ separator
											+ tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1] + ".lrn")
											.renameTo(new File(root + separator + rename
													+ separator + rename + ".lrn"));
								}
								if (new File(root
										+ separator
										+ rename
										+ separator
										+ tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1] + ".grf").exists()) {
									new File(root
											+ separator
											+ rename
											+ separator
											+ tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1] + ".grf")
											.renameTo(new File(root + separator + rename
													+ separator + rename + ".grf"));
								}
								if (new File(root
										+ separator
										+ rename
										+ separator
										+ tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1] + ".prb").exists()) {
									new File(root
											+ separator
											+ rename
											+ separator
											+ tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1] + ".prb")
											.renameTo(new File(root + separator + rename
													+ separator + rename + ".prb"));
								}
							}
							for (int i = 0; i < tab.getTabCount(); i++) {
								if (tab.getTitleAt(i).equals(
										tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1])) {
									if (tree.getFile().length() > 4
											&& tree.getFile()
													.substring(tree.getFile().length() - 5).equals(
															".sbml")
											|| tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".xml")) {
										((SBML_Editor) tab.getComponentAt(i)).setModelID(modelID);
										((SBML_Editor) tab.getComponentAt(i)).setFile(root
												+ separator + rename);
										tab.setTitleAt(i, rename);
									}
									else if (tree.getFile().length() > 3
											&& (tree.getFile().substring(
													tree.getFile().length() - 4).equals(".grf") || tree
													.getFile().substring(
															tree.getFile().length() - 4).equals(
															".prb"))) {
										((Graph) tab.getComponentAt(i)).setGraphName(rename);
										tab.setTitleAt(i, rename);
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".gcm")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".vhd")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else if (tree.getFile().length() > 1
											&& tree.getFile()
													.substring(tree.getFile().length() - 2).equals(
															".g")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 2));
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".csp")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".hse")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".unc")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else if (tree.getFile().length() > 3
											&& tree.getFile()
													.substring(tree.getFile().length() - 4).equals(
															".rsg")) {
										((GCM2SBMLEditor) tab.getComponentAt(i)).reload(rename
												.substring(0, rename.length() - 4));
									}
									else {
										JTabbedPane t = new JTabbedPane();
										int selected = ((JTabbedPane) tab.getComponentAt(i))
												.getSelectedIndex();
										boolean analysis = false;
										ArrayList<Component> comps = new ArrayList<Component>();
										for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i))
												.getTabCount(); j++) {
											Component c = ((JTabbedPane) tab.getComponentAt(i))
													.getComponent(j);
											comps.add(c);
										}
										for (Component c : comps) {
											if (c instanceof Reb2Sac) {
												((Reb2Sac) c).setSim(rename);
												analysis = true;
											}
											else if (c instanceof SBML_Editor) {
												String properties = root + separator + rename
														+ separator + rename + ".sim";
												new File(properties).renameTo(new File(properties
														.replace(".sim", ".temp")));
												boolean dirty = ((SBML_Editor) c).isDirty();
												((SBML_Editor) c).setParamFileAndSimDir(properties,
														root + separator + rename);
												((SBML_Editor) c).save(false, "", true);
												((SBML_Editor) c).updateSBML(i, 0);
												((SBML_Editor) c).setDirty(dirty);
												new File(properties).delete();
												new File(properties.replace(".sim", ".temp"))
														.renameTo(new File(properties));
											}
											else if (c instanceof Graph) {
												// c.addMouseListener(this);
												Graph g = ((Graph) c);
												g.setDirectory(root + separator + rename);
												if (g.isTSDGraph()) {
													g.setGraphName(rename + ".grf");
												}
												else {
													g.setGraphName(rename + ".prb");
												}
											}
											else if (c instanceof Learn) {
												Learn l = ((Learn) c);
												l.setDirectory(root + separator + rename);
											}
											else if (c instanceof DataManager) {
												DataManager d = ((DataManager) c);
												d.setDirectory(root + separator + rename);
											}
											if (analysis) {
												if (c instanceof Reb2Sac) {
													t.addTab("Simulation Options", c);
													t.getComponentAt(t.getComponents().length - 1)
															.setName("Simulate");
												}
												else if (c instanceof SBML_Editor) {
													t.addTab("Parameter Editor", c);
													t.getComponentAt(t.getComponents().length - 1)
															.setName("SBML Editor");
												}
												else if (c instanceof Graph) {
													if (((Graph) c).isTSDGraph()) {
														t.addTab("TSD Graph", c);
														t.getComponentAt(
																t.getComponents().length - 1)
																.setName("TSD Graph");
													}
													else {
														t.addTab("Probability Graph", c);
														t.getComponentAt(
																t.getComponents().length - 1)
																.setName("ProbGraph");
													}
												}
												else {
													t.addTab("Abstraction Options", c);
													t.getComponentAt(t.getComponents().length - 1)
															.setName("");
												}
											}
										}
										if (analysis) {
											t.setSelectedIndex(selected);
											tab.setComponentAt(i, t);
										}
										tab.setTitleAt(i, rename);
										tab.getComponentAt(i).setName(rename);
									}
								}
							}
							refreshTree();
						}
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to rename selected file.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getActionCommand().equals("openGraph")) {
			boolean done = false;
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab
						.getTitleAt(i)
						.equals(
								tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					done = true;
				}
			}
			if (!done) {
				if (tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
						.contains(".grf")) {
					addTab(
							tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
							new Graph(null, "amount", "title", "tsd.printer", root, "time", this,
									tree.getFile(), log, tree.getFile().split(separator)[tree
											.getFile().split(separator).length - 1], true, false),
							"TSD Graph");
				}
				else {
					addTab(
							tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
							new Graph(null, "amount", "title", "tsd.printer", root, "time", this,
									tree.getFile(), log, tree.getFile().split(separator)[tree
											.getFile().split(separator).length - 1], false, false),
							"Probability Graph");
				}
			}
		}
	}

	public int getTab(String name) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public void deleteDir(File dir) {
		int count = 0;
		do {
			File[] list = dir.listFiles();
			System.gc();
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					deleteDir(list[i]);
				}
				else {
					list[i].delete();
				}
			}
			count++;
		}
		while (!dir.delete() && count != 100);
		if (count == 100) {
			JOptionPane.showMessageDialog(frame, "Unable to delete.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method adds a new project to recent list
	 */
	public void addRecentProject(String projDir) {
		// boolean newOne = true;
		for (int i = 0; i < numberRecentProj; i++) {
			if (recentProjectPaths[i].equals(projDir)) {
				for (int j = 0; j <= i; j++) {
					String save = recentProjectPaths[j];
					recentProjects[j]
							.setText(projDir.split(separator)[projDir.split(separator).length - 1]);
					if (file.getItem(file.getItemCount() - 1) == exit) {
						file.insert(recentProjects[j], file.getItemCount() - 3 - numberRecentProj);
					}
					else {
						file.add(recentProjects[j]);
					}
					recentProjectPaths[j] = projDir;
					projDir = save;
				}
				for (int j = i + 1; j < numberRecentProj; j++) {
					if (file.getItem(file.getItemCount() - 1) == exit) {
						file.insert(recentProjects[j], file.getItemCount() - 3 - numberRecentProj);
					}
					else {
						file.add(recentProjects[j]);
					}
				}
				return;
			}
		}
		if (numberRecentProj < 5) {
			numberRecentProj++;
		}
		for (int i = 0; i < numberRecentProj; i++) {
			String save = recentProjectPaths[i];
			recentProjects[i]
					.setText(projDir.split(separator)[projDir.split(separator).length - 1]);
			if (file.getItem(file.getItemCount() - 1) == exit) {
				file.insert(recentProjects[i], file.getItemCount() - 3 - numberRecentProj);
			}
			else {
				file.add(recentProjects[i]);
			}
			recentProjectPaths[i] = projDir;
			projDir = save;
		}
	}

	/**
	 * This method refreshes the menu.
	 */
	public void refresh() {
		mainPanel.remove(tree);
		tree = new FileTree(new File(root), this, lema, atacs);
		mainPanel.add(tree, "West");
		mainPanel.validate();
	}

	/**
	 * This method refreshes the tree.
	 */
	public void refreshTree() {
		tree.fixTree();
		mainPanel.validate();
		updateGCM();
	}

	/**
	 * This method adds the given Component to a tab.
	 */
	public void addTab(String name, Component panel, String tabName) {
		tab.addTab(name, panel);
		// panel.addMouseListener(this);
		if (tabName != null) {
			tab.getComponentAt(tab.getTabCount() - 1).setName(tabName);
		}
		else {
			tab.getComponentAt(tab.getTabCount() - 1).setName(name);
		}
		tab.setSelectedIndex(tab.getTabCount() - 1);
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
	 */
	public int save(int index) {
		if (tab.getComponentAt(index).getName().contains(("GCM"))
				|| tab.getComponentAt(index).getName().contains("LHPN")) {
			if (tab.getComponentAt(index) instanceof GCM2SBMLEditor) {
				GCM2SBMLEditor editor = (GCM2SBMLEditor) tab.getComponentAt(index);
				if (editor.isDirty()) {
					Object[] options = { "Yes", "No", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame,
							"Do you want to save changes to " + tab.getTitleAt(index) + "?",
							"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						editor.save("gcm");
						return 1;
					}
					else if (value == JOptionPane.NO_OPTION) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}
			else if (tab.getComponentAt(index) instanceof LHPNEditor) {
				LHPNEditor editor = (LHPNEditor) tab.getComponentAt(index);
				if (editor.isDirty()) {
					Object[] options = { "Yes", "No", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame,
							"Do you want to save changes to " + tab.getTitleAt(index) + "?",
							"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						editor.save();
						return 1;
					}
					else if (value == JOptionPane.NO_OPTION) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}
			return 1;
		}
		else if (tab.getComponentAt(index).getName().equals("SBML Editor")) {
			if (tab.getComponentAt(index) instanceof SBML_Editor) {
				if (((SBML_Editor) tab.getComponentAt(index)).isDirty()) {
					Object[] options = { "Yes", "No", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame,
							"Do you want to save changes to " + tab.getTitleAt(index) + "?",
							"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						((SBML_Editor) tab.getComponentAt(index)).save(false, "", true);
						return 1;
					}
					else if (value == JOptionPane.NO_OPTION) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}
			return 1;
		}
		else if (tab.getComponentAt(index).getName().contains("Graph")) {
			if (tab.getComponentAt(index) instanceof Graph) {
				if (((Graph) tab.getComponentAt(index)).hasChanged()) {
					Object[] options = { "Yes", "No", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame,
							"Do you want to save changes to " + tab.getTitleAt(index) + "?",
							"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (value == JOptionPane.YES_OPTION) {
						((Graph) tab.getComponentAt(index)).save();
						return 1;
					}
					else if (value == JOptionPane.NO_OPTION) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}
			return 1;
		}
		else {
			if (tab.getComponentAt(index) instanceof JTabbedPane) {
				for (int i = 0; i < ((JTabbedPane) tab.getComponentAt(index)).getTabCount(); i++) {
					if (((JTabbedPane) tab.getComponentAt(index)).getComponentAt(i).getName()
							.equals("Simulate")) {
						if (((Reb2Sac) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i))
								.hasChanged()) {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save simulation option changes for "
											+ tab.getTitleAt(index) + "?", "Save Changes",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								((Reb2Sac) ((JTabbedPane) tab.getComponentAt(index))
										.getComponent(i)).save();
							}
							else if (value == JOptionPane.CANCEL_OPTION) {
								return 0;
							}
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
							.equals("SBML Editor")) {
						if (((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index))
								.getComponent(i)).isDirty()) {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save parameter changes for "
											+ tab.getTitleAt(index) + "?", "Save Changes",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index))
										.getComponent(i)).save(false, "", true);
							}
							else if (value == JOptionPane.CANCEL_OPTION) {
								return 0;
							}
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
							.equals("Learn")) {
						if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Learn) {
							if (((Learn) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i))
									.hasChanged()) {
								Object[] options = { "Yes", "No", "Cancel" };
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save learn option changes for "
												+ tab.getTitleAt(index) + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
								if (value == JOptionPane.YES_OPTION) {
									if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Learn) {
										((Learn) ((JTabbedPane) tab.getComponentAt(index))
												.getComponent(i)).save();
									}
								}
								else if (value == JOptionPane.CANCEL_OPTION) {
									return 0;
								}
							}
						}
						if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
							if (((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index))
									.getComponent(i)).hasChanged()) {
								Object[] options = { "Yes", "No", "Cancel" };
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save learn option changes for "
												+ tab.getTitleAt(index) + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
								if (value == JOptionPane.YES_OPTION) {
									if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
										((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index))
												.getComponent(i)).save();
									}
								}
								else if (value == JOptionPane.CANCEL_OPTION) {
									return 0;
								}
							}
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
							.equals("Data Manager")) {
						if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof DataManager) {
							((DataManager) ((JTabbedPane) tab.getComponentAt(index))
									.getComponent(i)).saveChanges(tab.getTitleAt(index));
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName()
							.contains("Graph")) {
						if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
							if (((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i))
									.hasChanged()) {
								Object[] options = { "Yes", "No", "Cancel" };
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save graph changes for "
												+ tab.getTitleAt(index) + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
								if (value == JOptionPane.YES_OPTION) {
									if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
										Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index))
												.getComponent(i));
										g.save();
									}
								}
								else if (value == JOptionPane.CANCEL_OPTION) {
									return 0;
								}
							}
						}
					}
				}
			}
			else if (tab.getComponentAt(index) instanceof JPanel) {
				if ((tab.getComponentAt(index)).getName().equals("Synthesis")) {
					Component[] array = ((JPanel) tab.getComponentAt(index)).getComponents();
					if (array[0] instanceof Synthesis) {
						if (((Synthesis) array[0]).hasChanged()) {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save synthesis option changes for "
											+ tab.getTitleAt(index) + "?", "Save Changes",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								if (array[0] instanceof Synthesis) {
									((Synthesis) array[0]).save();
								}
							}
							else if (value == JOptionPane.CANCEL_OPTION) {
								return 0;
							}
						}
					}
				}
				else if (tab.getComponentAt(index).getName().equals("Verification")) {
					Component[] array = ((JPanel) tab.getComponentAt(index)).getComponents();
					if (array[0] instanceof Verification) {
						if (((Verification) array[0]).hasChanged()) {
							Object[] options = { "Yes", "No", "Cancel" };
							int value = JOptionPane.showOptionDialog(frame,
									"Do you want to save verification option changes for "
											+ tab.getTitleAt(index) + "?", "Save Changes",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								((Verification) array[0]).save();
							}
							else if (value == JOptionPane.CANCEL_OPTION) {
								return 0;
							}
						}
					}
				}
			}
			return 1;
		}
	}

	/**
	 * Saves a circuit from a learn view to the project view
	 */
	public void saveGcm(String filename, String path) {
		try {
			if (overwrite(root + separator + filename, filename)) {
				FileOutputStream out = new FileOutputStream(new File(root + separator + filename));
				FileInputStream in = new FileInputStream(new File(path));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
				refreshTree();
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save genetic circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves a circuit from a learn view to the project view
	 */
	public void saveLhpn(String filename, String path) {
		try {
			if (overwrite(root + separator + filename, filename)) {
				FileOutputStream out = new FileOutputStream(new File(root + separator + filename));
				FileInputStream in = new FileInputStream(new File(path));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
				refreshTree();
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save LHPN.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the frame.
	 */
	public JFrame frame() {
		return frame;
	}

	public void mousePressed(MouseEvent e) {
		// log.addText(e.getSource().toString());
		if (e.getSource() == frame.getGlassPane()) {
			Component glassPane = frame.getGlassPane();
			Point glassPanePoint = e.getPoint();
			// Component component = e.getComponent();
			Container container = frame.getContentPane();
			Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame
					.getContentPane());
			if (containerPoint.y < 0) { // we're not in the content pane
				if (containerPoint.y + menuBar.getHeight() >= 0) {
					Component component = menuBar.getComponentAt(glassPanePoint);
					Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
							component);
					component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e
							.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(),
							e.isPopupTrigger()));
					frame.getGlassPane().setVisible(false);
				}
			}
			else {
				Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
						containerPoint.x, containerPoint.y);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						deepComponent);
				deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger()));
			}
		}
		else {
			if (e.isPopupTrigger() && tree.getFile() != null) {
				frame.getGlassPane().setVisible(false);
				popup.removeAll();
				if (tree.getFile().length() > 4
						&& tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml")
						|| tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
					JMenuItem edit = new JMenuItem("View/Edit");
					edit.addActionListener(this);
					edit.setActionCommand("sbmlEditor");
					JMenuItem graph = new JMenuItem("View Network");
					graph.addActionListener(this);
					graph.setActionCommand("graph");
					JMenuItem browse = new JMenuItem("View in Browser");
					browse.addActionListener(this);
					browse.setActionCommand("browse");
					JMenuItem simulate = new JMenuItem("Create Analysis View");
					simulate.addActionListener(this);
					simulate.setActionCommand("simulate");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(simulate);
					popup.add(createLearn);
					popup.addSeparator();
					popup.add(graph);
					popup.add(browse);
					popup.addSeparator();
					popup.add(edit);
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
					JMenuItem create = new JMenuItem("Create Analysis View");
					create.addActionListener(this);
					create.setActionCommand("createSim");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem createSBML = new JMenuItem("Create SBML File");
					createSBML.addActionListener(this);
					createSBML.setActionCommand("createSBML");
					JMenuItem edit = new JMenuItem("View/Edit");
					edit.addActionListener(this);
					edit.setActionCommand("dotEditor");
					JMenuItem graph = new JMenuItem("View Genetic Circuit");
					graph.addActionListener(this);
					graph.setActionCommand("graphDot");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(create);
					popup.add(createLearn);
					popup.add(createSBML);
					popup.addSeparator();
					popup.add(graph);
					popup.addSeparator();
					popup.add(edit);
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
					createAnalysis.addActionListener(this);
					createAnalysis.setActionCommand("createSim");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem createVerification = new JMenuItem("Create Verification View");
					createVerification.addActionListener(this);
					createVerification.setActionCommand("createVerify");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					if (atacs) {
						popup.add(createSynthesis);
					}
					// popup.add(createAnalysis);
					if (lema) {
						popup.add(createLearn);
					}
					popup.add(createVerification);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 1
						&& tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
					createAnalysis.addActionListener(this);
					createAnalysis.setActionCommand("createSim");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem createVerification = new JMenuItem("Create Verification View");
					createVerification.addActionListener(this);
					createVerification.setActionCommand("createVerify");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					if (atacs) {
						popup.add(createSynthesis);
					}
					// popup.add(createAnalysis);
					if (lema) {
						popup.add(createLearn);
					}
					popup.add(createVerification);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
					createAnalysis.addActionListener(this);
					createAnalysis.setActionCommand("createSim");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem createVerification = new JMenuItem("Create Verification View");
					createVerification.addActionListener(this);
					createVerification.setActionCommand("createVerify");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					if (atacs) {
						popup.add(createSynthesis);
					}
					// popup.add(createAnalysis);
					if (lema) {
						popup.add(createLearn);
					}
					popup.add(createVerification);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
					createAnalysis.addActionListener(this);
					createAnalysis.setActionCommand("createSim");
					JMenuItem createLearn = new JMenuItem("Create Learn View");
					createLearn.addActionListener(this);
					createLearn.setActionCommand("createLearn");
					JMenuItem createVerification = new JMenuItem("Create Verification View");
					createVerification.addActionListener(this);
					createVerification.setActionCommand("createVerify");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					if (atacs) {
						popup.add(createSynthesis);
					}
					// popup.add(createAnalysis);
					if (lema) {
						popup.add(createLearn);
					}
					popup.add(createVerification);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(createSynthesis);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
					JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
					createSynthesis.addActionListener(this);
					createSynthesis.setActionCommand("createSynthesis");
					JMenuItem viewModel = new JMenuItem("View Model");
					viewModel.addActionListener(this);
					viewModel.setActionCommand("viewModel");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(createSynthesis);
					popup.addSeparator();
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
					JMenuItem edit = new JMenuItem("View/Edit");
					edit.addActionListener(this);
					edit.setActionCommand("openGraph");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(edit);
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (tree.getFile().length() > 3
						&& tree.getFile().substring(tree.getFile().length() - 4).equals(".prb")) {
					JMenuItem edit = new JMenuItem("View/Edit");
					edit.addActionListener(this);
					edit.setActionCommand("openGraph");
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("delete");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(edit);
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
					boolean sim = false;
					boolean synth = false;
					boolean ver = false;
					for (String s : new File(tree.getFile()).list()) {
						if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
							sim = true;
						}
						if (s.length() > 3 && s.substring(s.length() - 4).equals(".syn")) {
							synth = true;
						}
						if (s.length() > 3 && s.substring(s.length() - 4).equals(".ver")) {
							ver = true;
						}
					}
					JMenuItem open;
					if (sim) {
						open = new JMenuItem("Open Analysis View");
						open.addActionListener(this);
						open.setActionCommand("openSim");
					}
					else if (synth) {
						open = new JMenuItem("Open Synthesis View");
						open.addActionListener(this);
						open.setActionCommand("openSynth");
					}
					else if (ver) {
						open = new JMenuItem("Open Verification View");
						open.addActionListener(this);
						open.setActionCommand("openVerification");
					}
					else {
						open = new JMenuItem("Open Learn View");
						open.addActionListener(this);
						open.setActionCommand("openLearn");
					}
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.setActionCommand("deleteSim");
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.setActionCommand("copy");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.setActionCommand("rename");
					popup.add(open);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
				if (popup.getComponentCount() != 0) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				if (tree.getFile() != null) {
					int index = tab.getSelectedIndex();
					enableTabMenu(index);
					if (tree.getFile().length() >= 5
							&& tree.getFile().substring(tree.getFile().length() - 5)
									.equals(".sbml") || tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
						try {
							boolean done = false;
							for (int i = 0; i < tab.getTabCount(); i++) {
								if (tab.getTitleAt(i).equals(
										tree.getFile().split(separator)[tree.getFile().split(
												separator).length - 1])) {
									tab.setSelectedIndex(i);
									done = true;
								}
							}
							if (!done) {
								SBML_Editor sbml = new SBML_Editor(tree.getFile(), null, log, this,
										null, null);
								// sbml.addMouseListener(this);
								addTab(tree.getFile().split(separator)[tree.getFile().split(
										separator).length - 1], sbml, "SBML Editor");
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame,
									"You must select a valid sbml file.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
						try {

							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								GCM2SBMLEditor gcm = new GCM2SBMLEditor(work.getAbsolutePath(),
										theFile, this, log, false, null);
								// gcm.addMouseListener(this);
								addTab(theFile, gcm, "GCM Editor");
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "VHDL Editor");
								}
							}
							// String[] command = { "emacs", filename };
							// Runtime.getRuntime().exec(command);
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this vhdl file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 2
							&& tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							LHPNFile lhpn = new LHPNFile(log);
							if (new File(directory + theFile).length() > 0) {
								// log.addText("here");
								lhpn.load(directory + theFile);
								// log.addText("there");
							}
							// log.addText("load completed");
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								// log.addText("make Editor");
								LHPNEditor editor = new LHPNEditor(work.getAbsolutePath(), theFile,
										lhpn, this, log);
								// editor.addMouseListener(this);
								addTab(theFile, editor, "LHPN Editor");
								// log.addText("Editor made");
							}
							// String[] cmd = { "emacs", filename };
							// Runtime.getRuntime().exec(cmd);
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this lhpn file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "CSP Editor");
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this csp file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "HSE Editor");
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this hse file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "UNC Editor");
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this unc file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "RSG Editor");
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this rsg file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".cir")) {
						try {
							String filename = tree.getFile();
							String directory = "";
							String theFile = "";
							if (filename.lastIndexOf('/') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('/') + 1);
								theFile = filename.substring(filename.lastIndexOf('/') + 1);
							}
							if (filename.lastIndexOf('\\') >= 0) {
								directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
								theFile = filename.substring(filename.lastIndexOf('\\') + 1);
							}
							File work = new File(directory);
							int i = getTab(theFile);
							if (i != -1) {
								tab.setSelectedIndex(i);
							}
							else {
								if (externView) {
									String command = viewerField.getText() + " " + directory
											+ separator + theFile;
									Runtime exec = Runtime.getRuntime();
									try {
										exec.exec(command);
									}
									catch (Exception e1) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open external editor.",
												"Error Opening Editor", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									File file = new File(work + separator + theFile);
									String input = "";
									FileReader in = new FileReader(file);
									int read = in.read();
									while (read != -1) {
										input += (char) read;
										read = in.read();
									}
									in.close();
									JTextArea text = new JTextArea(input);
									text.setEditable(true);
									text.setLineWrap(true);
									JScrollPane scroll = new JScrollPane(text);
									// gcm.addMouseListener(this);
									addTab(theFile, scroll, "Spice Editor");
								}
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to view this spice file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
						boolean done = false;
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (tab.getTitleAt(i)
									.equals(
											tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1])) {
								tab.setSelectedIndex(i);
								done = true;
							}
						}
						if (!done) {
							addTab(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
									new Graph(null, "amount", "title", "tsd.printer", root, "time",
											this, tree.getFile(), log,
											tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1], true, false),
									"TSD Graph");
						}
					}
					else if (tree.getFile().length() >= 4
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".prb")) {
						boolean done = false;
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (tab.getTitleAt(i)
									.equals(
											tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1])) {
								tab.setSelectedIndex(i);
								done = true;
							}
						}
						if (!done) {
							addTab(
									tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
									new Graph(null, "amount", "title", "tsd.printer", root, "time",
											this, tree.getFile(), log,
											tree.getFile().split(separator)[tree.getFile().split(
													separator).length - 1], false, false),
									"Probability Graph");
						}
					}
					else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
						boolean sim = false;
						boolean synth = false;
						boolean ver = false;
						for (String s : new File(tree.getFile()).list()) {
							if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
								sim = true;
							}
							else if (s.length() > 3 && s.substring(s.length() - 4).equals(".syn")) {
								synth = true;
							}
							else if (s.length() > 3 && s.substring(s.length() - 4).equals(".ver")) {
								ver = true;
							}
						}
						if (sim) {
							openSim();
						}
						else if (synth) {
							openSynth();
						}
						else if (ver) {
							openVerify();
						}
						else {
							if (lema) {
								openLearnLHPN();
							}
							else {
								openLearn();
							}
						}
					}
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == frame.getGlassPane()) {
			Component glassPane = frame.getGlassPane();
			Point glassPanePoint = e.getPoint();
			// Component component = e.getComponent();
			Container container = frame.getContentPane();
			Point containerPoint = SwingUtilities
					.convertPoint(glassPane, glassPanePoint, container);
			if (containerPoint.y < 0) { // we're not in the content pane
				if (containerPoint.y + menuBar.getHeight() >= 0) {
					Component component = menuBar.getComponentAt(glassPanePoint);
					Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
							component);
					component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e
							.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(),
							e.isPopupTrigger()));
					frame.getGlassPane().setVisible(false);
				}
			}
			else {
				try {
					Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
							containerPoint.x, containerPoint.y);
					Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
							deepComponent);
					if (e != null) {
						deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e
								.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e
								.getClickCount(), e.isPopupTrigger()));
					}
					if ((deepComponent instanceof JTree) && (e.getClickCount() != 2)) {
						enableTreeMenu();
					}
					else {
						enableTabMenu(tab.getSelectedIndex());
					}
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		else {
			if (tree.getFile() != null) {
				if (e.isPopupTrigger() && tree.getFile() != null) {
					frame.getGlassPane().setVisible(false);
					popup.removeAll();
					if (tree.getFile().length() > 4
							&& tree.getFile().substring(tree.getFile().length() - 5)
									.equals(".sbml") || tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("sbmlEditor");
						JMenuItem graph = new JMenuItem("View Network");
						graph.addActionListener(this);
						graph.setActionCommand("graph");
						JMenuItem browse = new JMenuItem("View in Browser");
						browse.addActionListener(this);
						browse.setActionCommand("browse");
						JMenuItem simulate = new JMenuItem("Create Analysis View");
						simulate.addActionListener(this);
						simulate.setActionCommand("simulate");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(simulate);
						popup.add(createLearn);
						popup.addSeparator();
						popup.add(graph);
						popup.add(browse);
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
						JMenuItem create = new JMenuItem("Create Analysis View");
						create.addActionListener(this);
						create.setActionCommand("createSim");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem createSBML = new JMenuItem("Create SBML File");
						createSBML.addActionListener(this);
						createSBML.setActionCommand("createSBML");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem graph = new JMenuItem("View Genetic Circuit");
						graph.addActionListener(this);
						graph.setActionCommand("graphDot");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(create);
						popup.add(createLearn);
						popup.add(createSBML);
						popup.addSeparator();
						popup.add(graph);
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("openGraph");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
						createAnalysis.addActionListener(this);
						createAnalysis.setActionCommand("createSim");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						if (atacs) {
							popup.add(createSynthesis);
						}
						// popup.add(createAnalysis);
						if (lema) {
							popup.add(createLearn);
						}
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 1
							&& tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
						createAnalysis.addActionListener(this);
						createAnalysis.setActionCommand("createSim");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						if (atacs) {
							popup.add(createSynthesis);
						}
						// popup.add(createAnalysis);
						if (lema) {
							popup.add(createLearn);
						}
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
						createAnalysis.addActionListener(this);
						createAnalysis.setActionCommand("createSim");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						if (atacs) {
							popup.add(createSynthesis);
						}
						// popup.add(createAnalysis);
						if (lema) {
							popup.add(createLearn);
						}
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
						createAnalysis.addActionListener(this);
						createAnalysis.setActionCommand("createSim");
						JMenuItem createLearn = new JMenuItem("Create Learn View");
						createLearn.addActionListener(this);
						createLearn.setActionCommand("createLearn");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						if (atacs) {
							popup.add(createSynthesis);
						}
						// popup.add(createAnalysis);
						if (lema) {
							popup.add(createLearn);
						}
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(createSynthesis);
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (tree.getFile().length() > 3
							&& tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
						JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
						createSynthesis.addActionListener(this);
						createSynthesis.setActionCommand("createSynthesis");
						JMenuItem edit = new JMenuItem("View/Edit");
						edit.addActionListener(this);
						edit.setActionCommand("dotEditor");
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("delete");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(createSynthesis);
						popup.addSeparator();
						popup.add(edit);
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
						boolean sim = false;
						boolean synth = false;
						boolean ver = false;
						for (String s : new File(tree.getFile()).list()) {
							if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
								sim = true;
							}
							else if (s.length() > 4 && s.substring(s.length() - 4).equals(".syn")) {
								synth = true;
							}
							else if (s.length() > 4 && s.substring(s.length() - 4).equals(".ver")) {
								ver = true;
							}
						}
						JMenuItem open;
						if (sim) {
							open = new JMenuItem("Open Analysis View");
							open.addActionListener(this);
							open.setActionCommand("openSim");
						}
						else if (synth) {
							open = new JMenuItem("Open Synthesis View");
							open.addActionListener(this);
							open.setActionCommand("openSynth");
						}
						else if (ver) {
							open = new JMenuItem("Open Verification View");
							open.addActionListener(this);
							open.setActionCommand("openVerification");
						}
						else {
							open = new JMenuItem("Open Learn View");
							open.addActionListener(this);
							open.setActionCommand("openLearn");
						}
						JMenuItem delete = new JMenuItem("Delete");
						delete.addActionListener(this);
						delete.setActionCommand("deleteSim");
						JMenuItem copy = new JMenuItem("Copy");
						copy.addActionListener(this);
						copy.setActionCommand("copy");
						JMenuItem rename = new JMenuItem("Rename");
						rename.addActionListener(this);
						rename.setActionCommand("rename");
						popup.add(open);
						popup.addSeparator();
						popup.add(copy);
						popup.add(rename);
						popup.add(delete);
					}
					if (popup.getComponentCount() != 0) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				else if (!popup.isVisible()) {
					frame.getGlassPane().setVisible(true);
				}
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame
				.getContentPane());
		if (containerPoint.y < 0) { // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) {
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						component);
				component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else {
			Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
					containerPoint.x, containerPoint.y);
			Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
					deepComponent);
			// if (deepComponent instanceof ScrollableTabPanel) {
			// deepComponent = tab.findComponentAt(componentPoint);
			// }
			deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e
					.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
					.isPopupTrigger()));
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame
				.getContentPane());
		if (containerPoint.y < 0) { // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) {
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						component);
				component.dispatchEvent(new MouseWheelEvent(component, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e
						.getWheelRotation()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else {
			Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
					containerPoint.x, containerPoint.y);
			Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
					deepComponent);
			// if (deepComponent instanceof ScrollableTabPanel) {
			// deepComponent = tab.findComponentAt(componentPoint);
			// }
			deepComponent.dispatchEvent(new MouseWheelEvent(deepComponent, e.getID(), e.getWhen(),
					e.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
							.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e
							.getWheelRotation()));
		}
	}

	private void simulate(boolean isDot) throws Exception {
		if (isDot) {
			String simName = JOptionPane.showInputDialog(frame, "Enter Analysis ID:",
					"Analysis ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.trim().equals("")) {
				simName = simName.trim();
				if (overwrite(root + separator + simName, simName)) {
					new File(root + separator + simName).mkdir();
					// new FileWriter(new File(root + separator + simName +
					// separator +
					// ".sim")).close();
					String[] dot = tree.getFile().split(separator);
					String sbmlFile = /*
										 * root + separator + simName +
										 * separator +
										 */(dot[dot.length - 1].substring(0, dot[dot.length - 1].length() - 3) + "sbml");
					GCMParser parser = new GCMParser(tree.getFile());
					GeneticNetwork network = parser.buildNetwork();
					GeneticNetwork.setRoot(root + File.separator);
					network.mergeSBML(root + separator + simName + separator + sbmlFile);
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
								+ simName.trim() + separator + simName.trim() + ".sim"));
						out.write((dot[dot.length - 1] + "\n").getBytes());
						out.close();
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to save parameter file!",
								"Error Saving File", JOptionPane.ERROR_MESSAGE);
					}
					// network.outputSBML(root + separator + sbmlFile);
					refreshTree();
					sbmlFile = root
							+ separator
							+ simName
							+ separator
							+ (dot[dot.length - 1].substring(0, dot[dot.length - 1].length() - 3) + "sbml");
					JTabbedPane simTab = new JTabbedPane();
					Reb2Sac reb2sac = new Reb2Sac(sbmlFile, sbmlFile, root, this, simName.trim(),
							log, simTab, null);
					// reb2sac.addMouseListener(this);
					simTab.addTab("Simulation Options", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					JPanel abstraction = reb2sac.getAdvanced();
					// abstraction.addMouseListener(this);
					simTab.addTab("Abstraction Options", abstraction);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
					// simTab.addTab("Advanced Options",
					// reb2sac.getProperties());
					// simTab.getComponentAt(simTab.getComponents().length -
					// 1).setName("");
					SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac, log, this, root
							+ separator + simName.trim(), root + separator + simName.trim()
							+ separator + simName.trim() + ".sim");
					reb2sac.setSbml(sbml);
					// sbml.addMouseListener(this);
					simTab.addTab("Parameter Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					simTab.addTab("SBML Elements", sbml.getElementsPanel());
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
					Graph tsdGraph = reb2sac.createGraph(null);
					// tsdGraph.addMouseListener(this);
					simTab.addTab("TSD Graph", tsdGraph);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
					Graph probGraph = reb2sac.createProbGraph(null);
					// probGraph.addMouseListener(this);
					simTab.addTab("Probability Graph", probGraph);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
					/*
					 * JLabel noData = new JLabel("No data available"); Font
					 * font = noData.getFont(); font =
					 * font.deriveFont(Font.BOLD, 42.0f); noData.setFont(font);
					 * noData.setHorizontalAlignment(SwingConstants.CENTER);
					 * simTab.addTab("TSD Graph", noData);
					 * simTab.getComponentAt(simTab.getComponents().length -
					 * 1).setName("TSD Graph"); JLabel noData1 = new JLabel("No
					 * data available"); Font font1 = noData1.getFont(); font1 =
					 * font1.deriveFont(Font.BOLD, 42.0f);
					 * noData1.setFont(font1);
					 * noData1.setHorizontalAlignment(SwingConstants.CENTER);
					 * simTab.addTab("Probability Graph", noData1);
					 * simTab.getComponentAt(simTab.getComponents().length -
					 * 1).setName("ProbGraph");
					 */
					addTab(simName, simTab, null);
				}
			}
		}
		else {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab
						.getTitleAt(i)
						.equals(
								tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i) != 1) {
						return;
					}
					break;
				}
			}
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = reader.readSBML(tree.getFile());
			// document.setLevel(2);
			document.setLevelAndVersion(2, 3);
			String simName = JOptionPane.showInputDialog(frame, "Enter analysis id:",
					"Analysis ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.trim().equals("")) {
				simName = simName.trim();
				if (overwrite(root + separator + simName, simName)) {
					new File(root + separator + simName).mkdir();
					// new FileWriter(new File(root + separator + simName +
					// separator +
					// ".sim")).close();
					String sbmlFile = tree.getFile();
					String[] sbml1 = tree.getFile().split(separator);
					String sbmlFileProp = root + separator + simName + separator
							+ sbml1[sbml1.length - 1];
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator
								+ simName.trim() + separator + simName.trim() + ".sim"));
						out.write((sbml1[sbml1.length - 1] + "\n").getBytes());
						out.close();
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to save parameter file!",
								"Error Saving File", JOptionPane.ERROR_MESSAGE);
					}
					new FileOutputStream(new File(sbmlFileProp)).close();
					/*
					 * try { FileOutputStream out = new FileOutputStream(new
					 * File(sbmlFile)); SBMLWriter writer = new SBMLWriter();
					 * String doc = writer.writeToString(document); byte[]
					 * output = doc.getBytes(); out.write(output); out.close(); }
					 * catch (Exception e1) {
					 * JOptionPane.showMessageDialog(frame, "Unable to copy sbml
					 * file to output location.", "Error",
					 * JOptionPane.ERROR_MESSAGE); }
					 */
					refreshTree();
					JTabbedPane simTab = new JTabbedPane();
					Reb2Sac reb2sac = new Reb2Sac(sbmlFile, sbmlFileProp, root, this, simName
							.trim(), log, simTab, null);
					// reb2sac.addMouseListener(this);
					simTab.addTab("Simulation Options", reb2sac);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
					JPanel abstraction = reb2sac.getAdvanced();
					// abstraction.addMouseListener(this);
					simTab.addTab("Abstraction Options", abstraction);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
					// simTab.addTab("Advanced Options",
					// reb2sac.getProperties());
					// simTab.getComponentAt(simTab.getComponents().length -
					// 1).setName("");
					SBML_Editor sbml = new SBML_Editor(sbmlFile, reb2sac, log, this, root
							+ separator + simName.trim(), root + separator + simName.trim()
							+ separator + simName.trim() + ".sim");
					reb2sac.setSbml(sbml);
					// sbml.addMouseListener(this);
					simTab.addTab("Parameter Editor", sbml);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
					simTab.addTab("SBML Elements", sbml.getElementsPanel());
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
					Graph tsdGraph = reb2sac.createGraph(null);
					// tsdGraph.addMouseListener(this);
					simTab.addTab("TSD Graph", tsdGraph);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
					Graph probGraph = reb2sac.createProbGraph(null);
					// probGraph.addMouseListener(this);
					simTab.addTab("Probability Graph", probGraph);
					simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
					/*
					 * JLabel noData = new JLabel("No data available"); Font
					 * font = noData.getFont(); font =
					 * font.deriveFont(Font.BOLD, 42.0f); noData.setFont(font);
					 * noData.setHorizontalAlignment(SwingConstants.CENTER);
					 * simTab.addTab("TSD Graph", noData);
					 * simTab.getComponentAt(simTab.getComponents().length -
					 * 1).setName("TSD Graph"); JLabel noData1 = new JLabel("No
					 * data available"); Font font1 = noData1.getFont(); font1 =
					 * font1.deriveFont(Font.BOLD, 42.0f);
					 * noData1.setFont(font1);
					 * noData1.setHorizontalAlignment(SwingConstants.CENTER);
					 * simTab.addTab("Probability Graph", noData1);
					 * simTab.getComponentAt(simTab.getComponents().length -
					 * 1).setName("ProbGraph");
					 */
					addTab(simName, simTab, null);
				}
			}
		}
	}

	private void openLearn() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(
					tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JTabbedPane lrnTab = new JTabbedPane();
			// String graphFile = "";
			String open = null;
			if (new File(tree.getFile()).isDirectory()) {
				String[] list = new File(tree.getFile()).list();
				int run = 0;
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
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
						else if (end.equals(".grf")) {
							open = tree.getFile() + separator + list[i];
						}
					}
				}
			}

			String lrnFile = tree.getFile() + separator
					+ tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
					+ ".lrn";
			String lrnFile2 = tree.getFile() + separator + ".lrn";
			Properties load = new Properties();
			String learnFile = "";
			try {
				if (new File(lrnFile2).exists()) {
					FileInputStream in = new FileInputStream(new File(lrnFile2));
					load.load(in);
					in.close();
					new File(lrnFile2).delete();
				}
				if (new File(lrnFile).exists()) {
					FileInputStream in = new FileInputStream(new File(lrnFile));
					load.load(in);
					in.close();
					if (load.containsKey("genenet.file")) {
						learnFile = load.getProperty("genenet.file");
						learnFile = learnFile.split(separator)[learnFile.split(separator).length - 1];
					}
				}
				FileOutputStream out = new FileOutputStream(new File(lrnFile));
				load.store(out, learnFile);
				out.close();

			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame(), "Unable to load properties file!",
						"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab.getTitleAt(i).equals(learnFile)) {
					tab.setSelectedIndex(i);
					if (save(i) != 1) {
						return;
					}
					break;
				}
			}
			if (!(new File(root + separator + learnFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + learnFile
						+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			DataManager data = new DataManager(tree.getFile(), this, lema);
			// data.addMouseListener(this);
			lrnTab.addTab("Data Manager", data);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
			Learn learn = new Learn(tree.getFile(), log, this);
			// learn.addMouseListener(this);
			lrnTab.addTab("Learn", learn);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn");
			Graph tsdGraph = new Graph(null, "amount", tree.getFile().split(separator)[tree
					.getFile().split(separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "time", this, open, log, null, true,
					true);
			// tsdGraph.addMouseListener(this);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			// }
			/*
			 * else { lrnTab.addTab("Data Manager", new
			 * DataManager(tree.getFile(), this));
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("Data Manager"); JLabel noData = new JLabel("No data
			 * available"); Font font = noData.getFont(); font =
			 * font.deriveFont(Font.BOLD, 42.0f); noData.setFont(font);
			 * noData.setHorizontalAlignment(SwingConstants.CENTER);
			 * lrnTab.addTab("Learn", noData);
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("Learn"); JLabel noData1 = new JLabel("No data
			 * available"); font = noData1.getFont(); font =
			 * font.deriveFont(Font.BOLD, 42.0f); noData1.setFont(font);
			 * noData1.setHorizontalAlignment(SwingConstants.CENTER);
			 * lrnTab.addTab("TSD Graph", noData1);
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("TSD Graph"); }
			 */
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
					lrnTab, null);
		}
	}

	private void openLearnLHPN() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(
					tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JTabbedPane lrnTab = new JTabbedPane();
			// String graphFile = "";
			String open = null;
			if (new File(tree.getFile()).isDirectory()) {
				String[] list = new File(tree.getFile()).list();
				int run = 0;
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
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
						else if (end.equals(".grf")) {
							open = tree.getFile() + separator + list[i];
						}
					}
				}
			}

			String lrnFile = tree.getFile() + separator
					+ tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
					+ ".lrn";
			String lrnFile2 = tree.getFile() + separator + ".lrn";
			Properties load = new Properties();
			String learnFile = "";
			try {
				if (new File(lrnFile2).exists()) {
					FileInputStream in = new FileInputStream(new File(lrnFile2));
					load.load(in);
					in.close();
					new File(lrnFile2).delete();
				}
				if (new File(lrnFile).exists()) {
					FileInputStream in = new FileInputStream(new File(lrnFile));
					load.load(in);
					in.close();
					if (load.containsKey("genenet.file")) {
						learnFile = load.getProperty("genenet.file");
						learnFile = learnFile.split(separator)[learnFile.split(separator).length - 1];
					}
				}
				FileOutputStream out = new FileOutputStream(new File(lrnFile));
				load.store(out, learnFile);
				out.close();

			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame(), "Unable to load properties file!",
						"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab.getTitleAt(i).equals(learnFile)) {
					tab.setSelectedIndex(i);
					if (save(i) != 1) {
						return;
					}
					break;
				}
			}
			if (!(new File(root + separator + learnFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + learnFile
						+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			DataManager data = new DataManager(tree.getFile(), this, lema);
			// data.addMouseListener(this);
			lrnTab.addTab("Data Manager", data);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
			LearnLHPN learn = new LearnLHPN(tree.getFile(), log, this);
			// learn.addMouseListener(this);
			lrnTab.addTab("Learn", learn);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn");
			Graph tsdGraph = new Graph(null, "amount", tree.getFile().split(separator)[tree
					.getFile().split(separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "time", this, open, log, null, true,
					false);
			// tsdGraph.addMouseListener(this);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			// }
			/*
			 * else { lrnTab.addTab("Data Manager", new
			 * DataManager(tree.getFile(), this));
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("Data Manager"); JLabel noData = new JLabel("No data
			 * available"); Font font = noData.getFont(); font =
			 * font.deriveFont(Font.BOLD, 42.0f); noData.setFont(font);
			 * noData.setHorizontalAlignment(SwingConstants.CENTER);
			 * lrnTab.addTab("Learn", noData);
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("Learn"); JLabel noData1 = new JLabel("No data
			 * available"); font = noData1.getFont(); font =
			 * font.deriveFont(Font.BOLD, 42.0f); noData1.setFont(font);
			 * noData1.setHorizontalAlignment(SwingConstants.CENTER);
			 * lrnTab.addTab("TSD Graph", noData1);
			 * lrnTab.getComponentAt(lrnTab.getComponents().length -
			 * 1).setName("TSD Graph"); }
			 */
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
					lrnTab, null);
		}
	}

	private void openSynth() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(
					tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JPanel synthPanel = new JPanel();
			// String graphFile = "";
			if (new File(tree.getFile()).isDirectory()) {
				String[] list = new File(tree.getFile()).list();
				int run = 0;
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
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
					}
				}
			}

			String synthFile = tree.getFile() + separator
					+ tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
					+ ".syn";
			String synthFile2 = tree.getFile() + separator + ".syn";
			Properties load = new Properties();
			String synthesisFile = "";
			try {
				if (new File(synthFile2).exists()) {
					FileInputStream in = new FileInputStream(new File(synthFile2));
					load.load(in);
					in.close();
					new File(synthFile2).delete();
				}
				if (new File(synthFile).exists()) {
					FileInputStream in = new FileInputStream(new File(synthFile));
					load.load(in);
					in.close();
					if (load.containsKey("synthesis.file")) {
						synthesisFile = load.getProperty("synthesis.file");
						synthesisFile = synthesisFile.split(separator)[synthesisFile
								.split(separator).length - 1];
					}
				}
				FileOutputStream out = new FileOutputStream(new File(synthesisFile));
				load.store(out, synthesisFile);
				out.close();

			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame(), "Unable to load properties file!",
						"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab.getTitleAt(i).equals(synthesisFile)) {
					tab.setSelectedIndex(i);
					if (save(i) != 1) {
						return;
					}
					break;
				}
			}
			if (!(new File(root + separator + synthesisFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + synthesisFile
						+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			Synthesis synth = new Synthesis(tree.getFile(), "flag", log, this);
			// synth.addMouseListener(this);
			synthPanel.add(synth);
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
					synthPanel, "Synthesis");
		}
	}

	private void openVerify() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(
					tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JPanel verPanel = new JPanel();
			// String graphFile = "";
/*			if (new File(tree.getFile()).isDirectory()) {
				String[] list = new File(tree.getFile()).list();
				int run = 0;
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
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
					}
				}
			}*/

		    String verName = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
			String verFile = tree.getFile() + separator + verName + ".ver";
			Properties load = new Properties();
			String verifyFile = "";
			try {
				if (new File(verFile).exists()) {
					FileInputStream in = new FileInputStream(new File(verFile));
					load.load(in);
					in.close();
					if (load.containsKey("verification.file")) {
						verifyFile = load.getProperty("verification.file");
						verifyFile = verifyFile.split(separator)[verifyFile.split(separator).length - 1];
					}
				}
				FileOutputStream out = new FileOutputStream(new File(verifyFile));
				load.store(out, verifyFile);
				out.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame(), "Unable to load properties file!",
						"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (tab.getTitleAt(i).equals(verifyFile)) {
					tab.setSelectedIndex(i);
					if (save(i) != 1) {
						return;
					}
					break;
				}
			}
			if (!(new File(verFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + verifyFile
						+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			Verification ver = new Verification(root + separator + verName, verName, "flag", log, this, lema, atacs);
			// ver.addMouseListener(this);
			verPanel.add(ver);
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1],
					verPanel, "Verification");
		}
	}

	private void openSim() {
		String filename = tree.getFile();
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(
					filename.split(separator)[filename.split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			if (filename != null && !filename.equals("")) {
				if (new File(filename).isDirectory()) {
					if (new File(filename + separator + ".sim").exists()) {
						new File(filename + separator + ".sim").delete();
					}
					String[] list = new File(filename).list();
					String getAFile = "";
					// String probFile = "";
					String openFile = "";
					// String graphFile = "";
					String open = null;
					String openProb = null;
					int run = 0;
					for (int i = 0; i < list.length; i++) {
						if (!(new File(list[i]).isDirectory()) && list[i].length() > 4) {
							String end = "";
							for (int j = 1; j < 5; j++) {
								end = list[i].charAt(list[i].length() - j) + end;
							}
							if (end.equals("sbml")) {
								getAFile = filename + separator + list[i];
							}
							else if (end.equals(".xml") && getAFile.equals("")) {
								getAFile = filename + separator + list[i];
							}
							else if (end.equals(".txt") && list[i].contains("sim-rep")) {
								// probFile = filename + separator + list[i];
							}
							else if (end.equals("ties") && list[i].contains("properties")
									&& !(list[i].equals("species.properties"))) {
								openFile = filename + separator + list[i];
							}
							else if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")
									|| end.contains("=")) {
								if (list[i].contains("run-")) {
									int tempNum = Integer.parseInt(list[i].substring(4, list[i]
											.length()
											- end.length()));
									if (tempNum > run) {
										run = tempNum;
										// graphFile = filename + separator +
										// list[i];
									}
								}
								else if (list[i].contains("euler-run.")
										|| list[i].contains("gear1-run.")
										|| list[i].contains("gear2-run.")
										|| list[i].contains("rk4imp-run.")
										|| list[i].contains("rk8pd-run.")
										|| list[i].contains("rkf45-run.")) {
									// graphFile = filename + separator +
									// list[i];
								}
								else if (end.contains("=")) {
									// graphFile = filename + separator +
									// list[i];
								}
							}
							else if (end.equals(".grf")) {
								open = filename + separator + list[i];
							}
							else if (end.equals(".prb")) {
								openProb = filename + separator + list[i];
							}
						}
						else if (new File(filename + separator + list[i]).isDirectory()) {
							String[] s = new File(filename + separator + list[i]).list();
							for (int j = 0; j < s.length; j++) {
								if (s[j].contains("sim-rep")) {
									// probFile = filename + separator + list[i]
									// + separator +
									// s[j];
								}
								else if (s[j].contains(".tsd")) {
									// graphFile = filename + separator +
									// list[i] + separator +
									// s[j];
								}
							}
						}
					}
					if (!getAFile.equals("")) {
						String[] split = filename.split(separator);
						String simFile = root + separator + split[split.length - 1].trim()
								+ separator + split[split.length - 1].trim() + ".sim";
						String pmsFile = root + separator + split[split.length - 1].trim()
								+ separator + split[split.length - 1].trim() + ".pms";
						if (new File(pmsFile).exists()) {
							if (new File(simFile).exists()) {
								new File(pmsFile).delete();
							}
							else {
								new File(pmsFile).renameTo(new File(simFile));
							}
						}
						String sbmlLoadFile = "";
						if (new File(simFile).exists()) {
							try {
								Scanner s = new Scanner(new File(simFile));
								if (s.hasNextLine()) {
									sbmlLoadFile = s.nextLine();
									sbmlLoadFile = sbmlLoadFile.split(separator)[sbmlLoadFile
											.split(separator).length - 1];
									if (sbmlLoadFile.equals("")) {
										JOptionPane
												.showMessageDialog(
														frame,
														"Unable to open view because "
																+ "the sbml linked to this view is missing.",
														"Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
									else if (!(new File(root + separator + sbmlLoadFile).exists())) {
										JOptionPane.showMessageDialog(frame,
												"Unable to open view because " + sbmlLoadFile
														+ " is missing.", "Error",
												JOptionPane.ERROR_MESSAGE);
										return;
									}
									if (sbmlLoadFile.contains(".gcm")) {
										GCMParser parser = new GCMParser(root + separator
												+ sbmlLoadFile);
										GeneticNetwork network = parser.buildNetwork();
										GeneticNetwork.setRoot(root + File.separator);
										sbmlLoadFile = root + separator
												+ split[split.length - 1].trim() + separator
												+ sbmlLoadFile.replace(".gcm", ".sbml");
										network.mergeSBML(sbmlLoadFile);
									}
									else {
										sbmlLoadFile = root + separator + sbmlLoadFile;
									}
								}
								while (s.hasNextLine()) {
									s.nextLine();
								}
								s.close();
								File f = new File(sbmlLoadFile);
								if (!f.exists()) {
									sbmlLoadFile = root + separator + f.getName();
								}
							}
							catch (Exception e) {
								JOptionPane.showMessageDialog(frame, "Unable to load sbml file.",
										"Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						else {
							sbmlLoadFile = root
									+ separator
									+ getAFile.split(separator)[getAFile.split(separator).length - 1];
							if (!new File(sbmlLoadFile).exists()) {
								sbmlLoadFile = getAFile;
								/*
								 * JOptionPane.showMessageDialog(frame, "Unable
								 * to load sbml file.", "Error",
								 * JOptionPane.ERROR_MESSAGE); return;
								 */
							}
						}
						if (!new File(sbmlLoadFile).exists()) {
							JOptionPane.showMessageDialog(frame,
									"Unable to open view because "
											+ sbmlLoadFile.split(separator)[sbmlLoadFile
													.split(separator).length - 1] + " is missing.",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (tab.getTitleAt(i)
									.equals(
											sbmlLoadFile.split(separator)[sbmlLoadFile
													.split(separator).length - 1])) {
								tab.setSelectedIndex(i);
								if (save(i) != 1) {
									return;
								}
								break;
							}
						}
						JTabbedPane simTab = new JTabbedPane();
						Reb2Sac reb2sac = new Reb2Sac(sbmlLoadFile, getAFile, root, this,
								split[split.length - 1].trim(), log, simTab, openFile);
						simTab.addTab("Simulation Options", reb2sac);
						simTab.getComponentAt(simTab.getComponents().length - 1)
								.setName("Simulate");
						simTab.addTab("Abstraction Options", reb2sac.getAdvanced());
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
						// simTab.addTab("Advanced Options",
						// reb2sac.getProperties());
						// simTab.getComponentAt(simTab.getComponents().length -
						// 1).setName("");
						SBML_Editor sbml = new SBML_Editor(sbmlLoadFile, reb2sac, log, this, root
								+ separator + split[split.length - 1].trim(), root + separator
								+ split[split.length - 1].trim() + separator
								+ split[split.length - 1].trim() + ".sim");
						reb2sac.setSbml(sbml);
						// sbml.addMouseListener(this);
						simTab.addTab("Parameter Editor", sbml);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName(
								"SBML Editor");
						simTab.addTab("SBML Elements", sbml.getElementsPanel());
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
						// if (open != null) {
						Graph tsdGraph = reb2sac.createGraph(open);
						// tsdGraph.addMouseListener(this);
						simTab.addTab("TSD Graph", tsdGraph);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName(
								"TSD Graph");
						/*
						 * } else if (!graphFile.equals("")) {
						 * simTab.addTab("TSD Graph",
						 * reb2sac.createGraph(open));
						 * simTab.getComponentAt(simTab.getComponents().length -
						 * 1).setName("TSD Graph"); } / else { JLabel noData =
						 * new JLabel("No data available"); Font font =
						 * noData.getFont(); font = font.deriveFont(Font.BOLD,
						 * 42.0f); noData.setFont(font);
						 * noData.setHorizontalAlignment(SwingConstants.CENTER);
						 * simTab.addTab("TSD Graph", noData);
						 * simTab.getComponentAt(simTab.getComponents().length -
						 * 1).setName("TSD Graph"); }
						 */
						// if (openProb != null) {
						Graph probGraph = reb2sac.createProbGraph(openProb);
						// probGraph.addMouseListener(this);
						simTab.addTab("Probability Graph", probGraph);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName(
								"ProbGraph");
						/*
						 * } else if (!probFile.equals("")) {
						 * simTab.addTab("Probability Graph",
						 * reb2sac.createProbGraph(openProb));
						 * simTab.getComponentAt(simTab.getComponents().length -
						 * 1).setName("ProbGraph"); } else { JLabel noData1 =
						 * new JLabel("No data available"); Font font1 =
						 * noData1.getFont(); font1 =
						 * font1.deriveFont(Font.BOLD, 42.0f);
						 * noData1.setFont(font1);
						 * noData1.setHorizontalAlignment
						 * (SwingConstants.CENTER); simTab.addTab("Probability
						 * Graph", noData1);
						 * simTab.getComponentAt(simTab.getComponents().length -
						 * 1).setName("ProbGraph"); }
						 */
						addTab(split[split.length - 1], simTab, null);
					}
				}
			}
		}
	}

	private class NewAction extends AbstractAction {
		NewAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			popup.add(newProj);
			if (!async) {
				popup.add(newCircuit);
				popup.add(newModel);
			}
			else if (atacs) {
				popup.add(newVhdl);
				popup.add(newLhpn);
				popup.add(newCsp);
				popup.add(newHse);
				popup.add(newUnc);
				popup.add(newRsg);
			}
			else {
				popup.add(newVhdl);
				popup.add(newLhpn);
				popup.add(newSpice);
			}
			popup.add(graph);
			popup.add(probGraph);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x,
						mainPanel.getMousePosition().y);
			}
		}
	}

	private class SaveAction extends AbstractAction {
		SaveAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			if (!lema) {
				popup.add(saveAsGcm);
			}
			else {
				popup.add(saveAsLhpn);
			}
			popup.add(saveAsGraph);
			if (!lema) {
				popup.add(saveAsSbml);
				popup.add(saveAsTemplate);
			}
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x,
						mainPanel.getMousePosition().y);
			}
		}
	}

	private class ImportAction extends AbstractAction {
		ImportAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			if (!lema) {
				popup.add(importDot);
				popup.add(importSbml);
			}
			else if (atacs) {
				popup.add(importVhdl);
				popup.add(importLhpn);
				popup.add(importCsp);
				popup.add(importHse);
				popup.add(importUnc);
				popup.add(importRsg);
			}
			else {
				popup.add(importVhdl);
				popup.add(importLhpn);
				popup.add(importSpice);
			}
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x,
						mainPanel.getMousePosition().y);
			}
		}
	}

	private class ExportAction extends AbstractAction {
		ExportAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			popup.add(exportCsv);
			popup.add(exportDat);
			popup.add(exportEps);
			popup.add(exportJpg);
			popup.add(exportPdf);
			popup.add(exportPng);
			popup.add(exportSvg);
			popup.add(exportTsd);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x,
						mainPanel.getMousePosition().y);
			}
		}
	}

	private class ModelAction extends AbstractAction {
		ModelAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			popup.add(viewModGraph);
			popup.add(viewModBrowser);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x,
						mainPanel.getMousePosition().y);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == frame.getGlassPane()) {
			Component glassPane = frame.getGlassPane();
			Point glassPanePoint = e.getPoint();
			// Component component = e.getComponent();
			Container container = frame.getContentPane();
			Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame
					.getContentPane());
			if (containerPoint.y < 0) { // we're not in the content pane
				if (containerPoint.y + menuBar.getHeight() >= 0) {
					Component component = menuBar.getComponentAt(glassPanePoint);
					Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
							component);
					component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e
							.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(),
							e.isPopupTrigger()));
					frame.getGlassPane().setVisible(false);
				}
			}
			else {
				Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
						containerPoint.x, containerPoint.y);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						deepComponent);
				// if (deepComponent instanceof ScrollableTabPanel) {
				// deepComponent = tab.findComponentAt(componentPoint);
				// }
				deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger()));
				if ((deepComponent instanceof JTree) && (e.getClickCount() != 2)) {
					enableTreeMenu();
				}
				else {
					enableTabMenu(tab.getSelectedIndex());
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame
				.getContentPane());
		if (containerPoint.y < 0) { // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) {
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						component);
				component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else {
			try {
				Component deepComponent = SwingUtilities.getDeepestComponentAt(container,
						containerPoint.x, containerPoint.y);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint,
						deepComponent);
				// if (deepComponent instanceof ScrollableTabPanel) {
				// deepComponent = tab.findComponentAt(componentPoint);
				// }
				deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e
						.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
						.isPopupTrigger()));
			}
			catch (Exception e1) {
			}
		}
	}

	public JMenuItem getExitButton() {
		return exit;
	}

	/**
	 * This is the main method. It excecutes the BioSim GUI FrontEnd program.
	 */
	public static void main(String args[]) {
		String varname;

		if (System.getProperty("mrj.version") != null)
			varname = "DYLD_LIBRARY_PATH"; // We're on a Mac.
		else
			varname = "LD_LIBRARY_PATH"; // We're not on a Mac.
		try {
			System.loadLibrary("sbmlj");
			// For extra safety, check that the jar file is in the classpath.
			Class.forName("org.sbml.libsbml.libsbml");
		}
		catch (UnsatisfiedLinkError e) {
			System.err.println("Error: could not link with the libSBML library."
					+ "  It is likely\nyour " + varname
					+ " environment variable does not include\nthe"
					+ " directory containing the libsbml library file.");
			System.exit(1);
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error: unable to load the file libsbmlj.jar."
					+ "  It is likely\nyour " + varname + " environment"
					+ " variable or CLASSPATH variable\ndoes not include"
					+ " the directory containing the libsbmlj.jar file.");
			System.exit(1);
		}
		catch (SecurityException e) {
			System.err.println("Could not load the libSBML library files due to a"
					+ " security exception.");
			System.exit(1);
		}
		boolean lemaFlag = false, atacsFlag = false;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-lema")) {
					lemaFlag = true;
				}
				else if (args[i].equals("-atacs")) {
					atacsFlag = true;
				}
			}
		}
		new BioSim(lemaFlag, atacsFlag);
	}

	public void copySim(String newSim) {
		try {
			new File(root + separator + newSim).mkdir();
			// new FileWriter(new File(root + separator + newSim + separator +
			// ".sim")).close();
			String oldSim = tab.getTitleAt(tab.getSelectedIndex());
			String[] s = new File(root + separator + oldSim).list();
			String sbmlFile = "";
			String propertiesFile = "";
			String sbmlLoadFile = null;
			for (String ss : s) {
				if (ss.length() > 4 && ss.substring(ss.length() - 5).equals(".sbml")
						|| ss.length() > 3 && ss.substring(ss.length() - 4).equals(".xml")) {
					SBMLReader reader = new SBMLReader();
					SBMLDocument document = reader.readSBML(root + separator + oldSim + separator
							+ ss);
					SBMLWriter writer = new SBMLWriter();
					writer.writeSBML(document, root + separator + newSim + separator + ss);
					sbmlFile = root + separator + newSim + separator + ss;
				}
				else if (ss.length() > 10 && ss.substring(ss.length() - 11).equals(".properties")) {
					FileOutputStream out = new FileOutputStream(new File(root + separator + newSim
							+ separator + ss));
					FileInputStream in = new FileInputStream(new File(root + separator + oldSim
							+ separator + ss));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
					propertiesFile = root + separator + newSim + separator + ss;
				}
				else if (ss.length() > 3
						&& (ss.substring(ss.length() - 4).equals(".dat")
								|| ss.substring(ss.length() - 4).equals(".sad")
								|| ss.substring(ss.length() - 4).equals(".pms") || ss.substring(
								ss.length() - 4).equals(".sim")) && !ss.equals(".sim")) {
					FileOutputStream out;
					if (ss.substring(ss.length() - 4).equals(".pms")) {
						out = new FileOutputStream(new File(root + separator + newSim + separator
								+ newSim + ".sim"));
					}
					else if (ss.substring(ss.length() - 4).equals(".sim")) {
						out = new FileOutputStream(new File(root + separator + newSim + separator
								+ newSim + ".sim"));
					}
					else {
						out = new FileOutputStream(new File(root + separator + newSim + separator
								+ ss));
					}
					FileInputStream in = new FileInputStream(new File(root + separator + oldSim
							+ separator + ss));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
					if (ss.substring(ss.length() - 4).equals(".pms")) {
						if (new File(root + separator + newSim + separator
								+ ss.substring(0, ss.length() - 4) + ".sim").exists()) {
							new File(root + separator + newSim + separator + ss).delete();
						}
						else {
							new File(root + separator + newSim + separator + ss).renameTo(new File(
									root + separator + newSim + separator
											+ ss.substring(0, ss.length() - 4) + ".sim"));
						}
						ss = ss.substring(0, ss.length() - 4) + ".sim";
					}
					if (ss.substring(ss.length() - 4).equals(".sim")) {
						try {
							Scanner scan = new Scanner(new File(root + separator + newSim
									+ separator + ss));
							if (scan.hasNextLine()) {
								sbmlLoadFile = scan.nextLine();
								sbmlLoadFile = sbmlLoadFile.split(separator)[sbmlLoadFile
										.split(separator).length - 1];
								if (sbmlLoadFile.contains(".gcm")) {
									GCMParser parser = new GCMParser(root + separator
											+ sbmlLoadFile);
									GeneticNetwork network = parser.buildNetwork();
									GeneticNetwork.setRoot(root + File.separator);
									sbmlLoadFile = root + separator + newSim + separator
											+ sbmlLoadFile.replace(".gcm", ".sbml");
									network.mergeSBML(sbmlLoadFile);
								}
								else {
									sbmlLoadFile = root + separator + sbmlLoadFile;
								}
							}
							while (scan.hasNextLine()) {
								scan.nextLine();
							}
							scan.close();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(frame, "Unable to load sbml file.",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			refreshTree();
			JTabbedPane simTab = new JTabbedPane();
			Reb2Sac reb2sac = new Reb2Sac(sbmlLoadFile, sbmlFile, root, this, newSim, log, simTab,
					propertiesFile);
			simTab.addTab("Simulation Options", reb2sac);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
			simTab.addTab("Abstraction Options", reb2sac.getAdvanced());
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
			// simTab.addTab("Advanced Options", reb2sac.getProperties());
			// simTab.getComponentAt(simTab.getComponents().length -
			// 1).setName("");
			SBML_Editor sbml = new SBML_Editor(sbmlLoadFile, reb2sac, log, this, root + separator
					+ newSim, root + separator + newSim + separator + newSim + ".sim");
			reb2sac.setSbml(sbml);
			// sbml.addMouseListener(this);
			simTab.addTab("Parameter Editor", sbml);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("SBML Editor");
			simTab.addTab("SBML Elements", sbml.getElementsPanel());
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
			Graph tsdGraph = reb2sac.createGraph(null);
			// tsdGraph.addMouseListener(this);
			simTab.addTab("TSD Graph", tsdGraph);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
			Graph probGraph = reb2sac.createProbGraph(null);
			// probGraph.addMouseListener(this);
			simTab.addTab("Probability Graph", probGraph);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
			/*
			 * JLabel noData = new JLabel("No data available"); Font font =
			 * noData.getFont(); font = font.deriveFont(Font.BOLD, 42.0f);
			 * noData.setFont(font);
			 * noData.setHorizontalAlignment(SwingConstants.CENTER);
			 * simTab.addTab("TSD Graph", noData);
			 * simTab.getComponentAt(simTab.getComponents().length -
			 * 1).setName("TSD Graph"); JLabel noData1 = new JLabel("No data
			 * available"); Font font1 = noData1.getFont(); font1 =
			 * font1.deriveFont(Font.BOLD, 42.0f); noData1.setFont(font1);
			 * noData1.setHorizontalAlignment(SwingConstants.CENTER);
			 * simTab.addTab("Probability Graph", noData1);
			 * simTab.getComponentAt(simTab.getComponents().length -
			 * 1).setName("ProbGraph");
			 */
			tab.setComponentAt(tab.getSelectedIndex(), simTab);
			tab.setTitleAt(tab.getSelectedIndex(), newSim);
			tab.getComponentAt(tab.getSelectedIndex()).setName(newSim);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Unable to copy simulation.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void refreshLearn(String learnName, boolean data) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).equals(learnName)) {
				for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getComponentCount(); j++) {
					if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName().equals(
							"TSD Graph")) {
						// if (data) {
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof Graph) {
							((Graph) ((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j))
									.refresh();
						}
						else {
							((JTabbedPane) tab.getComponentAt(i))
									.setComponentAt(j, new Graph(null, "amount", learnName
											+ " data", "tsd.printer", root + separator + learnName,
											"time", this, null, log, null, true, true));
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).setName(
									"TSD Graph");
						}
						/*
						 * } else { JLabel noData1 = new JLabel("No data
						 * available"); Font font = noData1.getFont(); font =
						 * font.deriveFont(Font.BOLD, 42.0f);
						 * noData1.setFont(font); noData1.setHorizontalAlignment
						 * (SwingConstants.CENTER); ((JTabbedPane)
						 * tab.getComponentAt(i)).setComponentAt(j, noData1);
						 * ((JTabbedPane)
						 * tab.getComponentAt(i)).getComponentAt(j
						 * ).setName("TSD Graph"); }
						 */
					}
					else if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName()
							.equals("Learn")) {
						// if (data) {
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof Learn) {
						}
						else {
							if (lema) {
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j,
										new LearnLHPN(root + separator + learnName, log, this));
							}
							else {
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, new Learn(
										root + separator + learnName, log, this));
							}
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j)
									.setName("Learn");
						}
						/*
						 * } else { JLabel noData = new JLabel("No data
						 * available"); Font font = noData.getFont(); font =
						 * font.deriveFont(Font.BOLD, 42.0f);
						 * noData.setFont(font);
						 * noData.setHorizontalAlignment(SwingConstants.CENTER);
						 * ((JTabbedPane)
						 * tab.getComponentAt(i)).setComponentAt(j, noData);
						 * ((JTabbedPane)
						 * tab.getComponentAt(i)).getComponentAt(j
						 * ).setName("Learn"); }
						 */
					}
				}
			}
		}
	}

	private void updateGCM() {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (tab.getTitleAt(i).contains(".gcm")) {
				((GCM2SBMLEditor) tab.getComponentAt(i)).reloadFiles();
				tab.setTitleAt(i, ((GCM2SBMLEditor) tab.getComponentAt(i)).getFilename());
			}
		}
	}

	public void updateViews(String updatedFile) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.tab.getTitleAt(i);
			String properties = root + separator + tab + separator + tab + ".sim";
			String properties2 = root + separator + tab + separator + tab + ".lrn";
			if (new File(properties).exists()) {
				String check = "";
				try {
					Scanner s = new Scanner(new File(properties));
					if (s.hasNextLine()) {
						check = s.nextLine();
						check = check.split(separator)[check.split(separator).length - 1];
					}
					s.close();
				}
				catch (Exception e) {
				}
				if (check.equals(updatedFile)) {
					JTabbedPane sim = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < sim.getTabCount(); j++) {
						if (sim.getComponentAt(j).getName().equals("SBML Editor")) {
							new File(properties).renameTo(new File(properties.replace(".sim",
									".temp")));
							boolean dirty = ((SBML_Editor) (sim.getComponentAt(j))).isDirty();
							((SBML_Editor) (sim.getComponentAt(j))).save(false, "", true);
							if (updatedFile.contains(".gcm")) {
								GCMParser parser = new GCMParser(root + separator + updatedFile);
								GeneticNetwork network = parser.buildNetwork();
								GeneticNetwork.setRoot(root + File.separator);
								network.mergeSBML(root + separator + tab + separator
										+ updatedFile.replace(".gcm", ".sbml"));
								((SBML_Editor) (sim.getComponentAt(j))).updateSBML(i, j, root
										+ separator + tab + separator
										+ updatedFile.replace(".gcm", ".sbml"));
							}
							else {
								((SBML_Editor) (sim.getComponentAt(j))).updateSBML(i, j, root
										+ separator + updatedFile);
							}
							((SBML_Editor) (sim.getComponentAt(j))).setDirty(dirty);
							new File(properties).delete();
							new File(properties.replace(".sim", ".temp")).renameTo(new File(
									properties));
							sim.setComponentAt(j + 1, ((SBML_Editor) (sim.getComponentAt(j)))
									.getElementsPanel());
							sim.getComponentAt(j + 1).setName("");
						}
					}
				}
			}
			if (new File(properties2).exists()) {
				String check = "";
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("genenet.file")) {
						String[] getProp = p.getProperty("genenet.file").split(separator);
						check = getProp[getProp.length - 1];
					}
					else {
						check = "";
					}
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(frame, "Unable to load background file.",
							"Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
				if (check.equals(updatedFile)) {
					JTabbedPane learn = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < learn.getTabCount(); j++) {
						if (learn.getComponentAt(j).getName().equals("Data Manager")) {
							((DataManager) (learn.getComponentAt(j))).updateSpecies();
						}
						else if (learn.getComponentAt(j).getName().equals("Learn")) {
							((Learn) (learn.getComponentAt(j))).updateSpecies(root + separator
									+ updatedFile);
						}
						else if (learn.getComponentAt(j).getName().contains("Graph")) {
							((Graph) (learn.getComponentAt(j))).refresh();
						}
					}
				}
			}
			ArrayList<String> saved = new ArrayList<String>();
			if (this.tab.getComponentAt(i) instanceof GCM2SBMLEditor) {
				saved.add(this.tab.getTitleAt(i));
				GCM2SBMLEditor gcm = (GCM2SBMLEditor) this.tab.getComponentAt(i);
				if (gcm.getSBMLFile().equals(updatedFile)) {
					gcm.save("save");
				}
			}
			String[] files = new File(root).list();
			for (String s : files) {
				if (s.contains(".gcm") && !saved.contains(s)) {
					GCMFile gcm = new GCMFile();
					gcm.load(root + separator + s);
					if (gcm.getSBMLFile().equals(updatedFile)) {
						updateViews(s);
					}
				}
			}
		}
	}

	private void enableTabMenu(int selectedTab) {
		treeSelected = false;
		// log.addText("tab menu");
		if (selectedTab != -1) {
			tab.setSelectedIndex(selectedTab);
		}
		Component comp = tab.getSelectedComponent();
		// if (comp != null) {
		// log.addText(comp.toString());
		// }
		viewModel.setEnabled(false);
		viewModGraph.setEnabled(false);
		viewModBrowser.setEnabled(false);
		createAnal.setEnabled(false);
		createLearn.setEnabled(false);
		createSbml.setEnabled(false);
		createSynth.setEnabled(false);
		createVer.setEnabled(false);
		if (comp instanceof GCM2SBMLEditor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			runButton.setEnabled(false);
			refreshButton.setEnabled(false);
			checkButton.setEnabled(false);
			exportButton.setEnabled(false);
			save.setEnabled(true);
			run.setEnabled(false);
			saveAs.setEnabled(true);
			saveAsMenu.setEnabled(true);
			saveAsGcm.setEnabled(true);
			saveAsLhpn.setEnabled(false);
			saveAsGraph.setEnabled(false);
			saveAsSbml.setEnabled(true);
			saveAsTemplate.setEnabled(true);
			refresh.setEnabled(false);
			check.setEnabled(false);
			export.setEnabled(false);
			exportMenu.setEnabled(false);
			viewCircuit.setEnabled(false);
			viewLog.setEnabled(false);
			saveParam.setEnabled(false);
			saveSbml.setEnabled(true);
			saveTemp.setEnabled(true);
		}
		else if (comp instanceof LHPNEditor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			runButton.setEnabled(false);
			refreshButton.setEnabled(false);
			checkButton.setEnabled(false);
			exportButton.setEnabled(false);
			save.setEnabled(true);
			run.setEnabled(false);
			saveAs.setEnabled(true);
			saveAsMenu.setEnabled(true);
			saveAsGcm.setEnabled(false);
			saveAsLhpn.setEnabled(true);
			saveAsGraph.setEnabled(false);
			saveAsSbml.setEnabled(false);
			saveAsTemplate.setEnabled(false);
			refresh.setEnabled(false);
			check.setEnabled(false);
			export.setEnabled(false);
			exportMenu.setEnabled(false);
			viewRules.setEnabled(false);
			viewTrace.setEnabled(false);
			viewCircuit.setEnabled(true);
			viewLog.setEnabled(false);
			saveParam.setEnabled(false);
		}
		else if (comp instanceof SBML_Editor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			runButton.setEnabled(false);
			refreshButton.setEnabled(false);
			checkButton.setEnabled(true);
			exportButton.setEnabled(false);
			save.setEnabled(true);
			run.setEnabled(false);
			saveAs.setEnabled(true);
			saveAsMenu.setEnabled(true);
			saveAsGcm.setEnabled(false);
			saveAsLhpn.setEnabled(false);
			saveAsGraph.setEnabled(false);
			saveAsSbml.setEnabled(true);
			saveAsTemplate.setEnabled(false);
			refresh.setEnabled(false);
			check.setEnabled(false);
			export.setEnabled(false);
			exportMenu.setEnabled(false);
			viewCircuit.setEnabled(false);
			viewLog.setEnabled(false);
			saveParam.setEnabled(false);
			saveSbml.setEnabled(true);
			saveTemp.setEnabled(true);
		}
		else if (comp instanceof Graph) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			runButton.setEnabled(false);
			refreshButton.setEnabled(true);
			checkButton.setEnabled(false);
			exportButton.setEnabled(true);
			save.setEnabled(true);
			saveAsMenu.setEnabled(true);
			saveAsGcm.setEnabled(false);
			saveAsLhpn.setEnabled(false);
			saveAsGraph.setEnabled(true);
			saveAsSbml.setEnabled(false);
			saveAsTemplate.setEnabled(false);
			run.setEnabled(false);
			saveAs.setEnabled(true);
			refresh.setEnabled(true);
			check.setEnabled(false);
			export.setEnabled(true);
			exportMenu.setEnabled(true);
			if (((Graph) comp).isTSDGraph()) {
				exportCsv.setEnabled(true);
				exportDat.setEnabled(true);
				exportTsd.setEnabled(true);
			}
			else {
				exportCsv.setEnabled(false);
				exportDat.setEnabled(false);
				exportTsd.setEnabled(false);
			}
			exportEps.setEnabled(true);
			exportJpg.setEnabled(true);
			exportPdf.setEnabled(true);
			exportPng.setEnabled(true);
			exportSvg.setEnabled(true);
			viewCircuit.setEnabled(false);
			viewLog.setEnabled(false);
			saveParam.setEnabled(false);
			saveSbml.setEnabled(false);
			saveTemp.setEnabled(false);
		}
		else if (comp instanceof JTabbedPane) {
			Component component = ((JTabbedPane) comp).getSelectedComponent();
			Boolean learn = false;
			for (Component c : ((JTabbedPane) comp).getComponents()) {
				if (c instanceof Learn) {
					learn = true;
				}
			}
			// int index = tab.getSelectedIndex();
			if (component instanceof Graph) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				if (learn) {
					runButton.setEnabled(false);
				}
				else {
					runButton.setEnabled(true);
				}
				refreshButton.setEnabled(true);
				checkButton.setEnabled(false);
				exportButton.setEnabled(true);
				save.setEnabled(true);
				if (learn) {
					run.setEnabled(false);
				}
				else {
					run.setEnabled(true);
				}
				saveAs.setEnabled(true);
				saveAsMenu.setEnabled(true);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(true);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(true);
				check.setEnabled(false);
				export.setEnabled(true);
				exportMenu.setEnabled(true);
				if (((Graph) component).isTSDGraph()) {
					exportCsv.setEnabled(true);
					exportDat.setEnabled(true);
					exportTsd.setEnabled(true);
				}
				else {
					exportCsv.setEnabled(false);
					exportDat.setEnabled(false);
					exportTsd.setEnabled(false);
				}
				exportEps.setEnabled(true);
				exportJpg.setEnabled(true);
				exportPdf.setEnabled(true);
				exportPng.setEnabled(true);
				exportSvg.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof Reb2Sac) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(false);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof SBML_Editor) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(false);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof Learn) {
				if (((Learn) component).isComboSelected()) {
					frame.getGlassPane().setVisible(false);
				}
				saveButton.setEnabled(((Learn) component).getSaveGcmEnabled());
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(((Learn) component).getSaveGcmEnabled());
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewModel.setEnabled(true);
				viewCircuit.setEnabled(((Learn) component).getViewGcmEnabled());
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewLog.setEnabled(((Learn) component).getViewLogEnabled());
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof LearnLHPN) {
				if (((LearnLHPN) component).isComboSelected()) {
					frame.getGlassPane().setVisible(false);
				}
				saveButton.setEnabled(((LearnLHPN) component).getSaveLhpnEnabled());
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(((LearnLHPN) component).getSaveLhpnEnabled());
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewModel.setEnabled(true);
				viewCircuit.setEnabled(((LearnLHPN) component).getViewLhpnEnabled());
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewLog.setEnabled(((LearnLHPN) component).getViewLogEnabled());
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof DataManager) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(false);
				runButton.setEnabled(false);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(true);
				run.setEnabled(false);
				saveAs.setEnabled(true);
				saveAsMenu.setEnabled(true);
				saveAsGcm.setEnabled(true);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof JPanel) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(false);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (component instanceof JScrollPane) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(false);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(false);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(true);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
		}
		else if (comp instanceof JPanel) {
			if (comp.getName().equals("Verification")) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(true);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewModel.setEnabled(true);
				viewRules.setEnabled(false);
				// viewTrace.setEnabled(((Verification)
				// comp).getViewTraceEnabled());
				viewTrace.setEnabled(true);
				viewCircuit.setEnabled(true);
				// viewLog.setEnabled(((Verification)
				// comp).getViewLogEnabled());
				viewLog.setEnabled(true);
				saveParam.setEnabled(true);
			}
			else if (comp.getName().equals("Synthesis")) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				refreshButton.setEnabled(false);
				checkButton.setEnabled(false);
				exportButton.setEnabled(false);
				save.setEnabled(true);
				run.setEnabled(true);
				saveAs.setEnabled(false);
				saveAsMenu.setEnabled(false);
				saveAsGcm.setEnabled(false);
				saveAsLhpn.setEnabled(false);
				saveAsGraph.setEnabled(false);
				saveAsSbml.setEnabled(false);
				saveAsTemplate.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				exportMenu.setEnabled(false);
				viewModel.setEnabled(true);
				// viewRules.setEnabled(((Synthesis)
				// comp).getViewRulesEnabled());
				// viewTrace.setEnabled(((Synthesis)
				// comp).getViewTraceEnabled());
				// viewCircuit.setEnabled(((Synthesis)
				// comp).getViewCircuitEnabled());
				// viewLog.setEnabled(((Synthesis) comp).getViewLogEnabled());
				viewRules.setEnabled(true);
				viewTrace.setEnabled(true);
				viewCircuit.setEnabled(true);
				viewLog.setEnabled(true);
				saveParam.setEnabled(false);
			}
		}
		else if (comp instanceof JScrollPane) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			runButton.setEnabled(false);
			refreshButton.setEnabled(false);
			checkButton.setEnabled(false);
			exportButton.setEnabled(false);
			save.setEnabled(true);
			run.setEnabled(false);
			saveAs.setEnabled(true);
			saveAsMenu.setEnabled(false);
			saveAsGcm.setEnabled(false);
			saveAsLhpn.setEnabled(false);
			saveAsGraph.setEnabled(false);
			saveAsSbml.setEnabled(false);
			saveAsTemplate.setEnabled(false);
			refresh.setEnabled(false);
			check.setEnabled(false);
			export.setEnabled(false);
			exportMenu.setEnabled(false);
			viewRules.setEnabled(false);
			viewTrace.setEnabled(false);
			viewCircuit.setEnabled(false);
			viewLog.setEnabled(false);
			saveParam.setEnabled(true);
			saveSbml.setEnabled(false);
			saveTemp.setEnabled(false);
		}
		else {
			saveButton.setEnabled(false);
			saveasButton.setEnabled(false);
			runButton.setEnabled(false);
			refreshButton.setEnabled(false);
			checkButton.setEnabled(false);
			exportButton.setEnabled(false);
			save.setEnabled(false);
			run.setEnabled(false);
			saveAs.setEnabled(false);
			saveAsMenu.setEnabled(false);
			saveAsGcm.setEnabled(false);
			saveAsLhpn.setEnabled(false);
			saveAsGraph.setEnabled(false);
			saveAsSbml.setEnabled(false);
			saveAsTemplate.setEnabled(false);
			refresh.setEnabled(false);
			check.setEnabled(false);
			export.setEnabled(false);
			exportMenu.setEnabled(false);
			viewCircuit.setEnabled(false);
			viewRules.setEnabled(false);
			viewTrace.setEnabled(false);
			viewLog.setEnabled(false);
			saveParam.setEnabled(false);
			saveSbml.setEnabled(false);
			saveTemp.setEnabled(false);
		}
		copy.setEnabled(false);
		rename.setEnabled(false);
		delete.setEnabled(false);
	}

	private void enableTreeMenu() {
		treeSelected = true;
		// log.addText(tree.getFile());
		saveButton.setEnabled(false);
		saveasButton.setEnabled(false);
		runButton.setEnabled(false);
		refreshButton.setEnabled(false);
		checkButton.setEnabled(false);
		exportButton.setEnabled(false);
		exportMenu.setEnabled(false);
		save.setEnabled(false);
		run.setEnabled(false);
		saveAs.setEnabled(false);
		saveAsMenu.setEnabled(false);
		saveAsGcm.setEnabled(false);
		saveAsLhpn.setEnabled(false);
		saveAsGraph.setEnabled(false);
		saveAsSbml.setEnabled(false);
		saveAsTemplate.setEnabled(false);
		if (tree.getFile() != null) {
			if (tree.getFile().length() > 4
					&& tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml")
					|| tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
				viewModGraph.setEnabled(true);
				viewModGraph.setActionCommand("graph");
				viewModBrowser.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("simulate");
				createLearn.setEnabled(true);
				createSbml.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewModel.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
				viewModGraph.setEnabled(true);
				viewModGraph.setActionCommand("graphDot");
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createSim");
				createLearn.setEnabled(true);
				createSbml.setEnabled(true);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewModel.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
				viewModel.setEnabled(false);
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSbml.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createSim");
				createLearn.setEnabled(true);
				createSynth.setEnabled(true);
				createVer.setEnabled(true);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 1
					&& tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createSim");
				createLearn.setEnabled(true);
				createSynth.setEnabled(true);
				createVer.setEnabled(true);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				if (new File(root + separator + "atacs.log").exists()) {
					viewLog.setEnabled(true);
				}
				else {
					viewLog.setEnabled(false);
				}
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSynth.setEnabled(false);
				createVer.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSynth.setEnabled(false);
				createVer.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSynth.setEnabled(false);
				createVer.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSynth.setEnabled(false);
				createVer.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
			else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root)) {
				boolean sim = false;
				boolean synth = false;
				boolean ver = false;
				boolean learn = false;
				for (String s : new File(tree.getFile()).list()) {
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
						sim = true;
					}
					else if (s.length() > 4 && s.substring(s.length() - 4).equals(".syn")) {
						synth = true;
					}
					else if (s.length() > 4 && s.substring(s.length() - 4).equals(".ver")) {
						ver = true;
					}
					else if (s.length() > 4 && s.substring(s.length() - 4).equals(".lrn")) {
						learn = true;
					}
				}
				if (sim || synth || ver || learn) {
					viewModGraph.setEnabled(false);
					viewModBrowser.setEnabled(false);
					createAnal.setEnabled(false);
					createLearn.setEnabled(false);
					createSbml.setEnabled(false);
					refresh.setEnabled(false);
					check.setEnabled(false);
					export.setEnabled(false);
					copy.setEnabled(true);
					rename.setEnabled(true);
					delete.setEnabled(true);
					viewRules.setEnabled(false);
					viewTrace.setEnabled(false);
					viewCircuit.setEnabled(false);
					viewLog.setEnabled(false);
					saveParam.setEnabled(false);
					saveSbml.setEnabled(false);
					saveTemp.setEnabled(false);
				}
			}
			else {
				viewModGraph.setEnabled(false);
				viewModBrowser.setEnabled(false);
				createAnal.setEnabled(false);
				createLearn.setEnabled(false);
				createSbml.setEnabled(false);
				refresh.setEnabled(false);
				check.setEnabled(false);
				export.setEnabled(false);
				copy.setEnabled(false);
				rename.setEnabled(false);
				delete.setEnabled(false);
				viewRules.setEnabled(false);
				viewTrace.setEnabled(false);
				viewCircuit.setEnabled(false);
				viewLog.setEnabled(false);
				saveParam.setEnabled(false);
				saveSbml.setEnabled(false);
				saveTemp.setEnabled(false);
			}
		}
	}

	public String getRoot() {
		return root;
	}

	public void setGlassPane(boolean visible) {
		frame.getGlassPane().setVisible(visible);
	}

	public boolean overwrite(String fullPath, String name) {
		if (new File(fullPath).exists()) {
			Object[] options = { "Overwrite", "Cancel" };
			// int value = JOptionPane.showOptionDialog(frame, name + " already
			// exists."
			// + "\nDo you want to overwrite?", "Overwrite",
			// JOptionPane.YES_NO_OPTION,
			// JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			// if (value == JOptionPane.YES_OPTION) {
			String[] views = canDelete(name);
			if (views.length == 0) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab.getTitleAt(i).equals(name)) {
						tab.remove(i);
					}
				}
				File dir = new File(fullPath);
				if (dir.isDirectory()) {
					deleteDir(dir);
				}
				else {
					System.gc();
					dir.delete();
				}
				return true;
			}
			else {
				String view = "";
				for (int i = 0; i < views.length; i++) {
					if (i == views.length - 1) {
						view += views[i];
					}
					else {
						view += views[i] + "\n";
					}
				}
				String message = "Unable to overwrite file."
						+ "\nIt is linked to the following views:\n" + view
						+ "\nDelete these views first.";
				JTextArea messageArea = new JTextArea(message);
				messageArea.setEditable(false);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(300, 300));
				scroll.setPreferredSize(new Dimension(300, 300));
				scroll.setViewportView(messageArea);
				JOptionPane.showMessageDialog(frame, scroll, "Unable To Overwrite File",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// }
			// else {
			// return false;
			// }
		}
		else {
			return true;
		}
	}

	public void updateOpenSBML(String sbmlName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.tab.getTitleAt(i);
			if (sbmlName.equals(tab)) {
				if (this.tab.getComponentAt(i) instanceof SBML_Editor) {
					SBML_Editor newSBML = new SBML_Editor(root + separator + sbmlName, null, log,
							this, null, null);
					this.tab.setComponentAt(i, newSBML);
					this.tab.getComponentAt(i).setName("SBML Editor");
					newSBML.save(false, "", false);
				}
			}
		}
	}

	private String[] canDelete(String filename) {
		ArrayList<String> views = new ArrayList<String>();
		String[] files = new File(root).list();
		for (String s : files) {
			if (new File(root + separator + s).isDirectory()) {
				String check = "";
				if (new File(root + separator + s + separator + s + ".sim").exists()) {
					try {
						Scanner scan = new Scanner(new File(root + separator + s + separator + s
								+ ".sim"));
						if (scan.hasNextLine()) {
							check = scan.nextLine();
							check = check.split(separator)[check.split(separator).length - 1];
						}
						scan.close();
					}
					catch (Exception e) {
					}
				}
				else if (new File(root + separator + s + separator + s + ".lrn").exists()) {
					try {
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + separator + s
								+ separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file")) {
							String[] getProp = p.getProperty("genenet.file").split(separator);
							check = getProp[getProp.length - 1];
						}
						else {
							check = "";
						}
					}
					catch (Exception e) {
						check = "";
					}
				}
				if (check.equals(filename)) {
					views.add(s);
				}
			}
		}
		String[] usingViews = views.toArray(new String[0]);
		sort(usingViews);
		return usingViews;
	}

	private void sort(String[] sort) {
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

	private void reassignViews(String oldName, String newName) {
		String[] files = new File(root).list();
		for (String s : files) {
			if (new File(root + separator + s).isDirectory()) {
				String check = "";
				if (new File(root + separator + s + separator + s + ".sim").exists()) {
					try {
						ArrayList<String> copy = new ArrayList<String>();
						Scanner scan = new Scanner(new File(root + separator + s + separator + s
								+ ".sim"));
						if (scan.hasNextLine()) {
							check = scan.nextLine();
							check = check.split(separator)[check.split(separator).length - 1];
							if (check.equals(oldName)) {
								while (scan.hasNextLine()) {
									copy.add(scan.nextLine());
								}
								scan.close();
								FileOutputStream out = new FileOutputStream(new File(root
										+ separator + s + separator + s + ".sim"));
								out.write((newName + "\n").getBytes());
								for (String cop : copy) {
									out.write((cop + "\n").getBytes());
								}
								out.close();
							}
							else {
								scan.close();
							}
						}
					}
					catch (Exception e) {
					}
				}
				else if (new File(root + separator + s + separator + s + ".lrn").exists()) {
					try {
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + separator + s
								+ separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file")) {
							String[] getProp = p.getProperty("genenet.file").split(separator);
							check = getProp[getProp.length - 1];
							if (check.equals(oldName)) {
								p.setProperty("genenet.file", newName);
								FileOutputStream store = new FileOutputStream(new File(root
										+ separator + s + separator + s + ".lrn"));
								p.store(store, "Learn File Data");
								store.close();
							}
						}
					}
					catch (Exception e) {
					}
				}
			}
		}
	}

	protected JButton makeToolButton(String imageName, String actionCommand, String toolTipText,
			String altText) {

		// URL imageURL = BioSim.class.getResource(imageName);
		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(new ImageIcon(imageName));

		// if (imageURL != null) { //image found
		// button.setIcon(new ImageIcon(imageURL, altText));
		// } else { //no image found
		// button.setText(altText);
		// System.err.println("Resource not found: "
		// + imageName);
		// }

		return button;
	}
}
