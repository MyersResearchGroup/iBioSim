package main;

import lpn.gui.*;
import lpn.parser.LhpnFile;
import lpn.parser.Lpn2verilog;
import lpn.parser.Translator;
import graph.Graph;
//import lpn.parser.properties.BuildProperty;







import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.xml.stream.XMLStreamException;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;

import analysis.main.AnalysisView;
import analysis.main.Run;
import lpn.parser.properties.BuildProperty;
import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.movie.MovieContainer;
import biomodel.gui.sbmlcore.ElementsPanel;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.parser.GCM2SBML;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;

import java.io.Writer;

import learn.AMSModel.LearnLHPN;
import learn.GCM.LearnGCM;
import learn.datamanager.DataManager;
import main.util.EditPreferences;
import main.util.FileTree;
import main.util.MutableBoolean;
import main.util.Utility;
import main.util.tabs.CloseAndMaxTabbedPane;
import synthesis.async.Synthesis;
import synthesis.genetic.SynthesisView;
import verification.*;
import verification.platu.lpn.io.PlatuGrammarLexer;
import verification.platu.lpn.io.PlatuGrammarParser;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.jlibsedml.Curve;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Model;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.SedMLError;
import org.jlibsedml.Task;
//import org.antlr.runtime.TokenStream;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.comp.*;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbolstandard.core.SBOLDocument;

//import lpn.parser.properties.*;







import sbol.browser.SBOLBrowser;
import sbol.util.SBOLUtility;

import java.net.*;

import uk.ac.ebi.biomodels.*;
import virtualparts.ModelBuilder;
import virtualparts.PartsHandler;
import virtualparts.SBML.SBMLHandler;
import virtualparts.entity.Interaction;
import virtualparts.entity.Interactions;
import virtualparts.entity.Summary;
import virtualparts.entity.Parts;
import virtualparts.entity.Part;

/**
 * This class creates a GUI for the Tstubd program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * are selected.
 * 
 * @author Curtis Madsen
 */

public class Gui implements MouseListener, ActionListener, MouseMotionListener, MouseWheelListener {

	public static JFrame frame; // Frame where components of the GUI are
	// displayed

	private JMenuBar menuBar;

	private JMenu file, openRecent, edit, view, tools, help, importMenu, exportMenu, newMenu, viewModel;
	private JMenuItem newProj; // The new menu item
	private JMenuItem newSBMLModel; // The new menu item	
	private JMenuItem newGridModel;
	private JMenuItem newVhdl; // The new vhdl menu item
	private JMenuItem newS; // The new assembly file menu item
	private JMenuItem newInst; // The new instruction file menu item
	private JMenuItem newLhpn; // The new lhpn menu item
	private JMenuItem newProperty; // The new lhpn menu item				DK
	private JMenuItem newG; // The new petri net menu item
	private JMenuItem newCsp; // The new csp menu item
	private JMenuItem newHse; // The new handshaking extension menu item
	private JMenuItem newUnc; // The new extended burst mode menu item
	private JMenuItem newRsg; // The new rsg menu item
	private JMenuItem newSpice; // The new spice circuit item
	private JMenuItem exit; // The exit menu item
	private JMenuItem importSbol;
	private JMenuItem importSedml;
	private JMenuItem importSbml; // The import sbml menu item
	private JMenuItem importBioModel; // The import sbml menu item
	private JMenuItem importVirtualPart;
	//private JMenuItem importDot; // The import dot menu item
	private JMenuItem importVhdl; // The import vhdl menu item
	private JMenuItem importS; // The import assembly file menu item
	private JMenuItem importInst; // The import instruction file menu item
	private JMenuItem importLpn; // The import lpn menu item
	private JMenuItem importG; // The import .g file menu item
	private JMenuItem importCsp; // The import csp menu item
	private JMenuItem importHse; // The import handshaking extension menu
	private JMenuItem importUnc; // The import extended burst mode menu item
	private JMenuItem importRsg; // The import rsg menu item
	private JMenuItem importSpice; // The import spice circuit item
	private JMenuItem manual; // The manual menu item
	private JMenuItem bugReport; // The manual menu item
	private JMenuItem about; // The about menu item
	private JMenuItem openProj; // The open menu item
	private JMenuItem clearRecent; // Clear recent project list
	private JMenuItem pref; // The preferences menu item
	private JMenuItem graph; // The graph menu item
	private JMenuItem probGraph, exportCsv, exportDat, exportEps, exportJpg, exportPdf, exportPng, exportSvg, exportTsd, 
	exportSBML, exportFlatSBML, exportSBOL, exportAvi, exportMp4;
	
	private JMenu exportDataMenu, exportMovieMenu, exportImageMenu;

	private String root; // The root directory

	private FileTree tree; // FileTree

	private CloseAndMaxTabbedPane tab; // JTabbedPane for different tools

	private JToolBar toolbar; // Tool bar for common options

	private JButton saveButton, runButton, refreshButton, saveasButton, checkButton, exportButton; // Tool

	// Bar
	// options

	private JPanel mainPanel; // the main panel

	private JSplitPane topSplit;
	
	private JSplitPane mainSplit;
	
	public static Boolean libsbmlFound = true;

	public Log log; // the log

	private JPopupMenu popup; // popup menu

	public static String separator;

	private KeyEventDispatcher dispatcher;

	private JMenuItem recentProjects[];

	private String recentProjectPaths[];

	private int numberRecentProj;

	private int ShortCutKey;

	public static String SBMLLevelVersion;

	private Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");

	private boolean async;
	// treeSelected
	// = false;

	public boolean atacs, lema;

	private String viewer;
	
	private boolean runGetNames;
	
	private boolean showParts = false;
	
	private Thread getPartsThread = null;

	private String[] BioModelIds = null;
	
	private Parts allVirtualParts = null;

	private JMenuItem addCompartment, addSpecies, addReaction, addComponent, addPromoter, addVariable, addBoolean, addPlace,
		addTransition, addRule, addConstraint, addEvent, addSelfInfl, cut, select,undo, redo, copy, rename, delete,
		moveLeft, moveRight, moveUp, moveDown;
	
	private JMenuItem save, saveAs, saveSBOL, check, run, refresh, viewCircuit, viewRules, viewTrace, viewLog, viewCoverage,
			viewLHPN, saveModel, saveAsVerilog, viewSG, viewModGraph, viewLearnedModel, viewModBrowser, createAnal, createLearn, 
			createSbml,	createSynth, createVer, close, closeAll, saveAll, convertToLPN;
	
	public String ENVVAR;

	public static int SBML_LEVEL = 3;

	public static int SBML_VERSION = 1;

	public static final Object[] OPTIONS = { "Yes", "No", "Yes To All", "No To All", "Cancel" };

	public static final int YES_OPTION = JOptionPane.YES_OPTION;

	public static final int NO_OPTION = JOptionPane.NO_OPTION;

	public static final int YES_TO_ALL_OPTION = JOptionPane.CANCEL_OPTION;

	public static final int NO_TO_ALL_OPTION = 3;

	public static final int CANCEL_OPTION = 4;

	public static Object ICON_EXPAND = UIManager.get("Tree.expandedIcon");

	public static Object ICON_COLLAPSE = UIManager.get("Tree.collapsedIcon");

	private static final String[] bugReportTypes = new String[] { "BUG", "CHANGE", "FEATURE" };
	
	private static final String lemaVersion = "2.0";
	
	private static final String atacsVersion = "6.1";
	
	private static final String iBioSimVersion = "2.6";
		
	public static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		
		String message;
		
		//Implements Thread.UncaughtExceptionHandler.uncaughtException()
		@Override
		public void uncaughtException(Thread th, Throwable ex) {
			final JFrame exp = new JFrame("Unhandled Exception");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			ex.printStackTrace();
			message = sw.toString(); // stack trace as a string
			JLabel error = new JLabel("Program has thrown an exception of the type:");
			JLabel errMsg = new JLabel(ex.toString());
			JButton details = new JButton("Details");
			details.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object[] options = { "Close" };
					JOptionPane.showOptionDialog(exp, message, "Details", JOptionPane.YES_OPTION, 
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				}
			});
			JButton report = new JButton("Send Bug Report");
			report.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					submitBugReport("\n\nStack trace:\n"+message);
				}
			});
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exp.dispose();
				}
			});
			JPanel errMessage = new JPanel();
			errMessage.add(error);
			JPanel errMsgPanel = new JPanel();
			errMsgPanel.add(errMsg);
			JPanel buttons = new JPanel();
			buttons.add(details);
			buttons.add(report);
			buttons.add(close);
			JPanel expPanel = new JPanel(new BorderLayout());
			expPanel.add(errMessage,"North");
			expPanel.add(errMsgPanel,"Center");
			expPanel.add(buttons,"South");
			exp.setContentPane(expPanel);
			exp.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			}
			catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = exp.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			exp.setLocation(x, y);
			exp.setVisible(true);
		}
	}
	
	private static void submitBugReport(String message) {
		JPanel reportBugPanel = new JPanel(new GridLayout(4,1));
		JLabel typeLabel = new JLabel("Type of Report:");
		JComboBox reportType = new JComboBox(bugReportTypes);
		if (!message.equals("")) {
			typeLabel.setEnabled(false);
			reportType.setEnabled(false);
		}
 		JPanel typePanel = new JPanel(new GridLayout(1,2));
		typePanel.add(typeLabel);
		typePanel.add(reportType);
		JLabel emailLabel = new JLabel("Email address:");
		JTextField emailAddr = new JTextField(30);
		JPanel emailPanel = new JPanel(new GridLayout(1,2));
		emailPanel.add(emailLabel);
		emailPanel.add(emailAddr);
		JLabel bugSubjectLabel = new JLabel("Brief Description:");
		JTextField bugSubject = new JTextField(30);
		JPanel bugSubjectPanel = new JPanel(new GridLayout(1,2));
		bugSubjectPanel.add(bugSubjectLabel);
		bugSubjectPanel.add(bugSubject);
		JLabel bugDetailLabel = new JLabel("Detailed Description:");
		JTextArea bugDetail = new JTextArea(5,30);
		bugDetail.setLineWrap(true);
		bugDetail.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(100, 100));
		scroll.setPreferredSize(new Dimension(100, 100));
		scroll.setViewportView(bugDetail);
		JPanel bugDetailPanel = new JPanel(new GridLayout(1,2));
		bugDetailPanel.add(bugDetailLabel);
		bugDetailPanel.add(scroll);
		reportBugPanel.add(typePanel);
		reportBugPanel.add(emailPanel);
		reportBugPanel.add(bugSubjectPanel);
		reportBugPanel.add(bugDetailPanel);
		Object[] options = { "Send", "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, reportBugPanel, "Bug Report",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value==0) {
			String to = "atacs-bugs@vlsigroup.ece.utah.edu";
			Properties props = new Properties();
			props.put("mail.smtp.user", "ibiosim@gmail.com");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.debug", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", 
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			MyAuthenticator authentication = new MyAuthenticator("ibiosim@gmail.com","lambda123");
			Session session = Session.getDefaultInstance(props,authentication);
			MimeMessage mimeMessage = new MimeMessage(session);
			try {
				mimeMessage.setFrom(new InternetAddress(emailAddr.getText().trim()));
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				mimeMessage.setSubject(reportType.getSelectedItem() + ": "+bugSubject.getText().trim());
				mimeMessage.setText(System.getProperty("software.running") + "\n\nOperating system: " + 
						System.getProperty("os.name") + "\n\nBug reported by: " + emailAddr.getText().trim() + 
						"\n\nDescription:\n"+bugDetail.getText().trim()+message);
				Transport.send(mimeMessage);
			} catch (MessagingException mex) {
				mex.printStackTrace();
			}
		}
	}
	
	   
	public static class MyAuthenticator extends javax.mail.Authenticator {
		String User;
		String Password;
		public MyAuthenticator (String user, String password) {
			User = user;
			Password = password;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(User, Password);
		}
	}

 
	public class MacOSAboutHandler extends Application {

		public MacOSAboutHandler() {
			addApplicationListener(new AboutBoxHandler());
		}

		class AboutBoxHandler extends ApplicationAdapter {
			@Override
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
			@Override
			public void handlePreferences(ApplicationEvent event) {
				EditPreferences editPreferences = new EditPreferences(frame,async,tree);
				editPreferences.preferences();
				event.setHandled(true);
			}
		}
	}

	public class MacOSQuitHandler extends Application {

		public MacOSQuitHandler() {
			addApplicationListener(new QuitHandler());
		}

		class QuitHandler extends ApplicationAdapter {
			@Override
			public void handleQuit(ApplicationEvent event) {
				if (exit())
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
	public Gui(boolean lema, boolean atacs, boolean libsbmlFound) {
		this.lema = lema;
		this.atacs = atacs;
		Gui.libsbmlFound = libsbmlFound;
		async = lema || atacs;
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		if (atacs) {
			ENVVAR = System.getenv("ATACSGUI");
			System.setProperty("software.running", "ATACS Version " + atacsVersion);
		}
		else if (lema) {
			ENVVAR = System.getenv("LEMA");
			System.setProperty("software.running", "LEMA Version " + lemaVersion);
		}
		else {
			ENVVAR = System.getenv("BIOSIM");
			System.setProperty("software.running", "iBioSim Version " + iBioSimVersion);
		}

		// Creates a new frame
		if (lema) {
			frame = new JFrame("LEMA");
			frame.setIconImage(new ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator + "LEMA.png").getImage());
		}
		else if (atacs) {
			frame = new JFrame("ATACS");
			frame.setIconImage(new ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator + "ATACS.png").getImage());
		}
		else {
			frame = new JFrame("iBioSim");
			frame.setIconImage(new ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator + "iBioSim.png").getImage());
		}

		// Makes it so that clicking the x in the corner closes the program
		WindowListener w = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				exit.doClick();
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}
		};
		frame.addWindowListener(w);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		popup = new JPopupMenu();
		popup.addMouseListener(this);
		// popup.addFocusListener(this);
		// popup.addComponentListener(this);

		// Sets up the Tool Bar
		toolbar = new JToolBar();
		String imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "save.png";
		saveButton = makeToolButton(imgName, "save", "Save");
		// toolButton = new JButton("Save");
		toolbar.add(saveButton);
		imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "saveas.png";
		saveasButton = makeToolButton(imgName, "saveas", "Save As");
		toolbar.add(saveasButton);
		imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "savecheck.png";
		checkButton = makeToolButton(imgName, "check", "Save and Check");
		toolbar.add(checkButton);
		imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "export.jpg";
		exportButton = makeToolButton(imgName, "export", "Export");
		toolbar.add(exportButton);
		imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "run-icon.jpg";
		runButton = makeToolButton(imgName, "run", "Save and Run");
		toolbar.add(runButton);
		imgName = ENVVAR + separator + "gui" + separator + "icons" + separator + "refresh.jpg";
		refreshButton = makeToolButton(imgName, "refresh", "Refresh");
		toolbar.add(refreshButton);
		saveButton.setEnabled(false);
		runButton.setEnabled(false);
		refreshButton.setEnabled(false);
		saveasButton.setEnabled(false);
		checkButton.setEnabled(false);
		exportButton.setEnabled(false);

		// Creates a menu for the frame
		menuBar = new JMenuBar();
		file = new JMenu("File");
		help = new JMenu("Help");
		edit = new JMenu("Edit");
		openRecent = new JMenu("Open Recent");
		importMenu = new JMenu("Import");
		exportMenu = new JMenu("Export");
		exportDataMenu = new JMenu("Data");
		exportImageMenu = new JMenu("Image");
		exportMovieMenu = new JMenu("Movie");
		newMenu = new JMenu("New");
		view = new JMenu("View");
		viewModel = new JMenu("Model");
		tools = new JMenu("Tools");
		menuBar.add(file);
		menuBar.add(edit);
		if (lema) {
			menuBar.add(view);
		}
		menuBar.add(tools);
		menuBar.add(help);
		select = new JMenuItem("Select Mode");
		cut = new JMenuItem("Delete");
		addCompartment = new JMenuItem("Add Compartment");
		addSpecies = new JMenuItem("Add Species");
		addReaction = new JMenuItem("Add Reaction");
		addComponent = new JMenuItem("Add Component");
		addPromoter = new JMenuItem("Add Promoter");
		addVariable = new JMenuItem("Add Variable");
		addBoolean = new JMenuItem("Add Boolean");
		addPlace = new JMenuItem("Add Place");
		addTransition = new JMenuItem("Add Transition");
		addRule = new JMenuItem("Add Rule");
		addConstraint = new JMenuItem("Add Constraint");
		addEvent = new JMenuItem("Add Event");
		addSelfInfl = new JMenuItem("Add Self Influence");
		moveLeft = new JMenuItem("Move Left");
		moveRight = new JMenuItem("Move Right");
		moveUp = new JMenuItem("Move Up");
		moveDown = new JMenuItem("Move Down");
		undo = new JMenuItem("Undo");
		redo = new JMenuItem("Redo");
		copy = new JMenuItem("Copy File");
		rename = new JMenuItem("Rename File");
		delete = new JMenuItem("Delete File");
		manual = new JMenuItem("Manual");
		bugReport = new JMenuItem("Submit Bug Report");
		about = new JMenuItem("About");
		openProj = new JMenuItem("Open Project");
		clearRecent = new JMenuItem("Clear Recent");
		close = new JMenuItem("Close");
		closeAll = new JMenuItem("Close All");
		saveAll = new JMenuItem("Save All");
		pref = new JMenuItem("Preferences");
		newProj = new JMenuItem("Project");
		newSBMLModel = new JMenuItem("Model");
		newGridModel = new JMenuItem("Grid Model");
		newSpice = new JMenuItem("Spice Circuit");
		newVhdl = new JMenuItem("VHDL Model");
		newS = new JMenuItem("Assembly File");
		newInst = new JMenuItem("Instruction File");
		newLhpn = new JMenuItem("LPN Model");
		newProperty = new JMenuItem("Property");				//DK
		newG = new JMenuItem("Petri Net");
		newCsp = new JMenuItem("CSP Model");
		newHse = new JMenuItem("Handshaking Expansion");
		newUnc = new JMenuItem("Extended Burst Mode Machine");
		newRsg = new JMenuItem("Reduced State Graph");
		graph = new JMenuItem("TSD Graph");
		probGraph = new JMenuItem("Histogram");
		importSbol = new JMenuItem("SBOL File");
		importSedml = new JMenuItem("SED-ML File");
		importSbml = new JMenuItem("SBML Model");
		importBioModel = new JMenuItem("BioModel");
		importVirtualPart = new JMenuItem("Virtual Part");
		convertToLPN= new JMenuItem("Convert To LPN");   //convert
		//importDot = new JMenuItem("iBioSim Model");
		importG = new JMenuItem("Petri Net");
		importLpn = new JMenuItem("LPN Model");
		importVhdl = new JMenuItem("VHDL Model");
		importS = new JMenuItem("Assembly File");
		importInst = new JMenuItem("Instruction File");
		importSpice = new JMenuItem("Spice Circuit");
		importCsp = new JMenuItem("CSP Model");
		importHse = new JMenuItem("Handshaking Expansion");
		importUnc = new JMenuItem("Extended Burst Mode Machine");
		importRsg = new JMenuItem("Reduced State Graph");
		exportSBML = new JMenuItem("SBML");
		exportFlatSBML = new JMenuItem("Flat SBML");
		exportSBOL = new JMenuItem("SBOL");
		exportCsv = new JMenuItem("CSV");
		exportDat = new JMenuItem("DAT");
		exportEps = new JMenuItem("EPS");
		exportJpg = new JMenuItem("JPG");
		exportPdf = new JMenuItem("PDF");
		exportPng = new JMenuItem("PNG");
		exportSvg = new JMenuItem("SVG");
		exportTsd = new JMenuItem("TSD");
		exportAvi = new JMenuItem("AVI");
		exportMp4 = new JMenuItem("MP4");
		save = new JMenuItem("Save");
		if (async) {
			saveModel = new JMenuItem("Save Learned LPN");
		}
		else {
			saveModel = new JMenuItem("Save Learned Model");
		}
		saveAsVerilog = new JMenuItem("Save as Verilog");
		saveAsVerilog.addActionListener(this);
		saveAsVerilog.setActionCommand("saveAsVerilog");
		saveAsVerilog.setEnabled(false);
		saveAs = new JMenuItem("Save As");
		run = new JMenuItem("Save and Run");
		check = new JMenuItem("Save and Check");
		saveSBOL = new JMenuItem("Save SBOL");
		refresh = new JMenuItem("Refresh");
		viewCircuit = new JMenuItem("Circuit");
		viewRules = new JMenuItem("Production Rules");
		viewTrace = new JMenuItem("Error Trace");
		viewLog = new JMenuItem("Log");
		viewCoverage = new JMenuItem("Coverage Report");
		viewLHPN = new JMenuItem("Model");
		viewModGraph = new JMenuItem("Model");
		viewLearnedModel = new JMenuItem("Learned Model");
		viewModBrowser = new JMenuItem("Model in Browser");
		viewSG = new JMenuItem("State Graph");
		createAnal = new JMenuItem("Analysis Tool");
		createLearn = new JMenuItem("Learn Tool");
		createSbml = new JMenuItem("Create SBML File");
		createSynth = new JMenuItem("Synthesis Tool");
		createVer = new JMenuItem("Verification Tool");
		exit = new JMenuItem("Exit");
		select.addActionListener(this);
		cut.addActionListener(this);
		addCompartment.addActionListener(this);
		addSpecies.addActionListener(this);
		addReaction.addActionListener(this);
		addComponent.addActionListener(this);
		addPromoter.addActionListener(this);
		addVariable.addActionListener(this);
		addBoolean.addActionListener(this);
		addPlace.addActionListener(this);
		addTransition.addActionListener(this);
		addRule.addActionListener(this);
		addConstraint.addActionListener(this);
		addEvent.addActionListener(this);
		addSelfInfl.addActionListener(this);
		moveLeft.addActionListener(this);
		moveRight.addActionListener(this);
		moveUp.addActionListener(this);
		moveDown.addActionListener(this);
		undo.addActionListener(this);
		redo.addActionListener(this);
		copy.addActionListener(this);
		rename.addActionListener(this);
		delete.addActionListener(this);
		openProj.addActionListener(this);
		clearRecent.addActionListener(this);
		close.addActionListener(this);
		closeAll.addActionListener(this);
		saveAll.addActionListener(this);
		pref.addActionListener(this);
		manual.addActionListener(this);
		bugReport.addActionListener(this);
		newProj.addActionListener(this);
		newSBMLModel.addActionListener(this);
		newGridModel.addActionListener(this);
		newVhdl.addActionListener(this);
		newS.addActionListener(this);
		newInst.addActionListener(this);
		newLhpn.addActionListener(this);
		newProperty.addActionListener(this);      //DK
		convertToLPN.addActionListener(this); 
		newG.addActionListener(this);
		newCsp.addActionListener(this);
		newHse.addActionListener(this);
		newUnc.addActionListener(this);
		newRsg.addActionListener(this);
		newSpice.addActionListener(this);
		exit.addActionListener(this);
		about.addActionListener(this);
		importSbol.addActionListener(this);
		importSedml.addActionListener(this);
		importSbml.addActionListener(this);
		importBioModel.addActionListener(this);
		importVirtualPart.addActionListener(this);
		//importDot.addActionListener(this);
		importVhdl.addActionListener(this);
		importS.addActionListener(this);
		importInst.addActionListener(this);
		importLpn.addActionListener(this);
		importG.addActionListener(this);
		importCsp.addActionListener(this);
		importHse.addActionListener(this);
		importUnc.addActionListener(this);
		importRsg.addActionListener(this);
		importSpice.addActionListener(this);
		exportSBML.addActionListener(this);
		exportFlatSBML.addActionListener(this);
		exportSBOL.addActionListener(this);
		exportCsv.addActionListener(this);
		exportDat.addActionListener(this);
		exportEps.addActionListener(this);
		exportJpg.addActionListener(this);
		exportPdf.addActionListener(this);
		exportPng.addActionListener(this);
		exportSvg.addActionListener(this);
		exportTsd.addActionListener(this);
		exportAvi.addActionListener(this);
		exportMp4.addActionListener(this);
		graph.addActionListener(this);
		probGraph.addActionListener(this);
		save.addActionListener(this);
		saveAs.addActionListener(this);
		saveSBOL.addActionListener(this);
		run.addActionListener(this);
		check.addActionListener(this);
		refresh.addActionListener(this);
		saveModel.addActionListener(this);
		viewCircuit.addActionListener(this);
		viewRules.addActionListener(this);
		viewTrace.addActionListener(this);
		viewLog.addActionListener(this);
		viewCoverage.addActionListener(this);
		viewLHPN.addActionListener(this);
		viewModGraph.addActionListener(this);
		viewLearnedModel.addActionListener(this);
		viewModBrowser.addActionListener(this);
		viewSG.addActionListener(this);
		createAnal.addActionListener(this);
		createLearn.addActionListener(this);
		createSbml.addActionListener(this);
		createSynth.addActionListener(this);
		createVer.addActionListener(this);
		save.setActionCommand("save");
		saveAs.setActionCommand("saveas");
		saveSBOL.setActionCommand("saveSBOL");
		run.setActionCommand("run");
		check.setActionCommand("check");
		refresh.setActionCommand("refresh");
		if (atacs) {
			viewModGraph.setActionCommand("viewModel");
		}
		else {
			viewModGraph.setActionCommand("graph");
		}
		viewLHPN.setActionCommand("viewModel");
		viewModBrowser.setActionCommand("browse");
		viewSG.setActionCommand("stateGraph");
		createLearn.setActionCommand("createLearn");
		createSbml.setActionCommand("createSBML");
		createVer.setActionCommand("createVerify");

		ShortCutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		newProj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ShortCutKey));
		openProj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ShortCutKey));
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ShortCutKey));
		closeAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ShortCutKey | InputEvent.SHIFT_MASK));
		saveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey | InputEvent.ALT_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey));
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey | InputEvent.SHIFT_MASK));
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ShortCutKey));
		if (lema) {
			newSBMLModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
		}
		else {
			check.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ShortCutKey));
			saveSBOL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey | InputEvent.ALT_MASK));
			refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
			newSBMLModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
			newGridModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ShortCutKey | InputEvent.ALT_MASK));
			createAnal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ShortCutKey | InputEvent.SHIFT_MASK));
			createLearn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ShortCutKey | InputEvent.SHIFT_MASK));
		}
		newLhpn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ShortCutKey));
		graph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ShortCutKey));
		probGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ShortCutKey | InputEvent.SHIFT_MASK));
		select.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
		addCompartment.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
		addSpecies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
		addReaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
		addComponent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
		addPromoter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK));
		addVariable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
		addBoolean.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));
		addPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
		addTransition.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
		addRule.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
		addConstraint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK));
		addEvent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));
		addSelfInfl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
		moveLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK));
		moveRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK));
		moveUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK));
		moveDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK));
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ShortCutKey));
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ShortCutKey | InputEvent.SHIFT_MASK));
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ShortCutKey | InputEvent.SHIFT_MASK));
		rename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ShortCutKey | InputEvent.SHIFT_MASK));
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ShortCutKey | InputEvent.SHIFT_MASK));
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey | InputEvent.SHIFT_MASK));
		bugReport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ShortCutKey));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ShortCutKey));
		pref.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ShortCutKey));

		importSbol.setEnabled(false);
		importSedml.setEnabled(false);
		importSbml.setEnabled(false);
		importBioModel.setEnabled(false);
		importVirtualPart.setEnabled(false);
		importVhdl.setEnabled(false);
		importS.setEnabled(false);
		importInst.setEnabled(false);
		importLpn.setEnabled(false);
		importG.setEnabled(false);
		importCsp.setEnabled(false);
		importHse.setEnabled(false);
		importUnc.setEnabled(false);
		importRsg.setEnabled(false);
		importSpice.setEnabled(false);
		exportMenu.setEnabled(false);
		exportSBML.setEnabled(false);
		exportFlatSBML.setEnabled(false);
		exportSBOL.setEnabled(false);
		exportCsv.setEnabled(false);
		exportDat.setEnabled(false);
		exportEps.setEnabled(false);
		exportJpg.setEnabled(false);
		exportPdf.setEnabled(false);
		exportPng.setEnabled(false);
		exportSvg.setEnabled(false);
		exportTsd.setEnabled(false);
		exportAvi.setEnabled(false);
		exportMp4.setEnabled(false);
		newSBMLModel.setEnabled(false);
		newGridModel.setEnabled(false);
		newVhdl.setEnabled(false);
		newS.setEnabled(false);
		newInst.setEnabled(false);
		newLhpn.setEnabled(false);
		newProperty.setEnabled(false);      // DK
		convertToLPN.setEnabled(false);
		newG.setEnabled(false);
		newCsp.setEnabled(false);
		newHse.setEnabled(false);
		newUnc.setEnabled(false);
		newRsg.setEnabled(false);
		newSpice.setEnabled(false);
		graph.setEnabled(false);
		probGraph.setEnabled(false);
		save.setEnabled(false);
		saveModel.setEnabled(false);
		saveAs.setEnabled(false);
		saveAll.setEnabled(false);
		close.setEnabled(false);
		closeAll.setEnabled(false);
		importMenu.setEnabled(false);
		saveSBOL.setEnabled(false);
		run.setEnabled(false);
		check.setEnabled(false);
		refresh.setEnabled(false);
		cut.setEnabled(false);
		select.setEnabled(false);
		addCompartment.setEnabled(false);
		addSpecies.setEnabled(false);
		addReaction.setEnabled(false);
		addComponent.setEnabled(false);
		addPromoter.setEnabled(false);
		addVariable.setEnabled(false);
		addBoolean.setEnabled(false);
		addPlace.setEnabled(false);
		addTransition.setEnabled(false);
		addRule.setEnabled(false);
		addConstraint.setEnabled(false);
		addEvent.setEnabled(false);
		addSelfInfl.setEnabled(false);
		moveLeft.setEnabled(false);
		moveRight.setEnabled(false);
		moveUp.setEnabled(false);
		moveDown.setEnabled(false);
		undo.setEnabled(false);
		redo.setEnabled(false);
		copy.setEnabled(false);
		rename.setEnabled(false);
		delete.setEnabled(false);
		viewCircuit.setEnabled(false);
		viewRules.setEnabled(false);
		viewTrace.setEnabled(false);
		viewLog.setEnabled(false);
		viewCoverage.setEnabled(false);
		viewLHPN.setEnabled(false);
		viewModel.setEnabled(false);
		viewModGraph.setEnabled(false);
		viewLearnedModel.setEnabled(false);
		viewModBrowser.setEnabled(false);
		viewSG.setEnabled(false);
		createAnal.setEnabled(false);
		createLearn.setEnabled(false);
		createSbml.setEnabled(false);
		createSynth.setEnabled(false);
		createVer.setEnabled(false);
		edit.add(undo);
		edit.add(redo);
		edit.addSeparator();
		edit.add(select);
		edit.add(cut);
		edit.add(moveLeft);
		edit.add(moveRight);
		edit.add(moveUp);
		edit.add(moveDown);
		if (!async) {
			edit.add(addCompartment);
			edit.add(addSpecies);
			edit.add(addPromoter);
			edit.add(addReaction);
		}
		edit.add(addComponent);
		edit.add(addVariable);
		edit.add(addBoolean);
		edit.add(addPlace);
		edit.add(addTransition);
		edit.add(addRule);
		edit.add(addConstraint);
		if (!async) {
			edit.add(addEvent);
			edit.add(addSelfInfl);
		}		
		edit.addSeparator();
		edit.add(copy);
		edit.add(rename);
		edit.add(delete);
		file.add(newMenu);
		newMenu.add(newProj);
		if (!async) {
			newMenu.add(newSBMLModel);
			newMenu.add(newGridModel);
			//newMenu.add(newLhpn);
		}
		else if (atacs) {
			newMenu.add(newVhdl);
			newMenu.add(newG);
			newMenu.add(newLhpn);
			newMenu.add(newCsp);
			newMenu.add(newHse);
			newMenu.add(newUnc);
			newMenu.add(newRsg);
			newMenu.add(newProperty);
		}
		else {
			newMenu.add(newVhdl);
			newMenu.add(newProperty);
			newMenu.add(newS);
			newMenu.add(newInst);
			newMenu.add(newSBMLModel);
			newMenu.add(newLhpn);
			// newMenu.add(newSpice);
		}
		newMenu.add(graph);
		newMenu.add(probGraph);
		file.add(openProj);
		file.add(openRecent);
		// openMenu.add(openProj);
		file.addSeparator();
		file.add(close);
		file.add(closeAll);
		file.addSeparator();
		file.add(save);
		file.add(saveAs);
		file.add(saveAll);
		if (!async) {
			//file.add(saveSBOL);
			file.add(check);
		}
		file.add(run);
		if (lema) {
			file.add(saveAsVerilog);
		}
		else {
			file.addSeparator();
			file.add(refresh);
		}
		if (lema) {
			file.add(saveModel);
		}
		file.addSeparator();
		file.add(importMenu);
		if (!async) {
			//importMenu.add(importDot);
			importMenu.add(importSbml);
			importMenu.add(importBioModel);
			importMenu.add(importVirtualPart);
			importMenu.add(importLpn);
			importMenu.add(importSbol);
			importMenu.add(importSedml);
		}
		else if (atacs) {
			importMenu.add(importVhdl);
			importMenu.add(importG);
			importMenu.add(importLpn);
			importMenu.add(importCsp);
			importMenu.add(importHse);
			importMenu.add(importUnc);
			importMenu.add(importRsg);
		}
		else {
			importMenu.add(importVhdl);
			importMenu.add(importS);
			importMenu.add(importInst);
			importMenu.add(importLpn);
			// importMenu.add(importSpice);
		}
		file.add(exportMenu);
		exportMenu.add(exportDataMenu);
		exportMenu.add(exportImageMenu);
		exportMenu.add(exportMovieMenu);
		exportMenu.add(exportFlatSBML);
		exportMenu.add(exportSBML);
		exportMenu.add(exportSBOL);
		exportMenu.addSeparator();
		
		exportDataMenu.add(exportTsd);
		exportDataMenu.add(exportCsv);
		exportDataMenu.add(exportDat);
		exportImageMenu.add(exportEps);
		exportImageMenu.add(exportJpg);
		exportImageMenu.add(exportPdf);
		exportImageMenu.add(exportPng);
		exportImageMenu.add(exportSvg);
		exportMovieMenu.add(exportAvi);
		exportMovieMenu.add(exportMp4);
		//file.addSeparator();
		help.add(manual);
		help.add(bugReport);
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			MacOSAboutHandler macOSAboutHandler = new MacOSAboutHandler();
			MacOSPreferencesHandler macOSPreferencesHandler = new MacOSPreferencesHandler();
			MacOSQuitHandler macOSQuitHandler = new MacOSQuitHandler();
			Application application = new Application();
			application.addPreferencesMenuItem();
			application.setEnabledPreferencesMenu(true);
		}
		else {
			edit.addSeparator();
			edit.add(pref);
			file.add(exit);
			//file.addSeparator();
			help.add(about);
		}
		if (lema) {
			// view.add(viewVHDL);
			// view.add(viewVerilog);
			view.add(viewLHPN);
			view.addSeparator();
			view.add(viewLearnedModel);
			view.add(viewCoverage);
			view.add(viewLog);
			view.add(viewTrace);
			
		}
		else if (atacs) {
			view.add(viewModGraph);
			view.add(viewCircuit);
			view.add(viewRules);
			view.add(viewTrace);
			view.add(viewLog);
		}
		else {
			view.add(viewModGraph);
			// view.add(viewModBrowser);
			view.add(viewLearnedModel);
			view.add(viewSG);
			view.add(viewLog);
			// view.addSeparator();
			// view.add(refresh);
		}
		if (libsbmlFound) {
			tools.add(createAnal);
			tools.add(createSynth);
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
		recentProjects = new JMenuItem[10];
		recentProjectPaths = new String[10];
		for (int i = 0; i < 10; i++) {
			recentProjects[i] = new JMenuItem();
			recentProjects[i].addActionListener(this);
			recentProjects[i].setActionCommand("recent" + i);
			recentProjectPaths[i] = "";
		}
		recentProjects[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, ShortCutKey));
		recentProjects[0].setMnemonic(KeyEvent.VK_0);
		recentProjects[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ShortCutKey));
		recentProjects[1].setMnemonic(KeyEvent.VK_1);
		recentProjects[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ShortCutKey));
		recentProjects[2].setMnemonic(KeyEvent.VK_2);
		recentProjects[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ShortCutKey));
		recentProjects[3].setMnemonic(KeyEvent.VK_3);
		recentProjects[4].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ShortCutKey));
		recentProjects[4].setMnemonic(KeyEvent.VK_4);
		recentProjects[5].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ShortCutKey));
		recentProjects[5].setMnemonic(KeyEvent.VK_5);
		recentProjects[6].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, ShortCutKey));
		recentProjects[6].setMnemonic(KeyEvent.VK_6);
		recentProjects[7].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, ShortCutKey));
		recentProjects[7].setMnemonic(KeyEvent.VK_7);
		recentProjects[8].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ShortCutKey));
		recentProjects[8].setMnemonic(KeyEvent.VK_8);
		recentProjects[9].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ShortCutKey));
		recentProjects[9].setMnemonic(KeyEvent.VK_9);

		Preferences biosimrc = Preferences.userRoot();
		viewer = biosimrc.get("biosim.general.viewer", "");
		for (int i = 0; i < 10; i++) {
			if (atacs) {
				recentProjects[i].setText(biosimrc.get("atacs.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("atacs.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals("")) {
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				} else {
					break;
				}
			}
			else if (lema) {
				recentProjects[i].setText(biosimrc.get("lema.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("lema.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals("")) {
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				} else {
					break;
				}
			}
			else {
				recentProjects[i].setText(biosimrc.get("biosim.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("biosim.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals("")) {
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				} else {
					break;
				}
			}
		}
		openRecent.addSeparator();
		openRecent.add(clearRecent);
//		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
//			new MacOSAboutHandler();
//			new MacOSPreferencesHandler();
//			new MacOSQuitHandler();
//			Application application = new Application();
//			application.addPreferencesMenuItem();
//			application.setEnabledPreferencesMenu(true);
//		}
//		else {
//			// file.add(pref);
//			// file.add(exit);
//			help.add(about);
//		}
		/*if (biosimrc.get("biosim.sbml.level_version", "").equals("L2V4")) {
			SBMLLevelVersion = "L2V4";
			SBML_LEVEL = 2;
			SBML_VERSION = 4;
		}
		else {*/
		SBMLLevelVersion = "L3V1";
		SBML_LEVEL = 3;
		SBML_VERSION = 1;
		//}
		// Open .biosimrc here

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null, this, lema, atacs);
		
		EditPreferences editPreferences = new EditPreferences(frame,async,tree);
		editPreferences.setDefaultPreferences();

		log = new Log();
		tab = new CloseAndMaxTabbedPane(false, this);
		tab.setPreferredSize(new Dimension(1100, 550));
		// tab.getPaneUI().addMouseListener(this);
		
		topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, tab);
		mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, log);
		
		//mainPanel.add(tree, "West");
		mainPanel.add(mainSplit, "Center");
		//mainPanel.add(log, "South");
		mainPanel.add(toolbar, "North");
		frame.setContentPane(mainPanel);
		frame.setJMenuBar(menuBar);
		// frame.getGlassPane().setVisible(true);
		// frame.getGlassPane().addMouseListener(this);
		// frame.getGlassPane().addMouseMotionListener(this);
		// frame.getGlassPane().addMouseWheelListener(this);
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
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_TYPED) {
					if (e.getKeyChar() == '') {
						if (tab.getTabCount() > 0) {
							KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
							if (save(tab.getSelectedIndex(), 0) != 0) {
								tab.remove(tab.getSelectedIndex());
							}
							KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
						}
					}
				}
				return false;
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
	}
	
	public String getTitleAt(int i) {
		return tab.getTitleAt(i).replace("*","");
	}
	
	public static boolean getCheckUndeclared() {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.check.undeclared", "").equals("false")) {
			return false;
		}
		return true;
	}

	public static boolean getCheckUnits() {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.check.units", "").equals("false")) {
			return false;
		}
		return true;
	}

	public void about() {
		final JFrame f = new JFrame("About");
		// frame.setIconImage(new ImageIcon(ENVVAR +
		// File.separator
		// + "gui"
		// + File.separator + "icons" + File.separator +
		// "iBioSim.png").getImage());
		JLabel name;
		JLabel version;
		final String developers;
		if (lema) {
			name = new JLabel("LEMA", SwingConstants.CENTER);
			version = new JLabel("Version "+lemaVersion, SwingConstants.CENTER);
			developers = "Satish Batchu\nAndrew Fisher\nKevin Jones\nDhanashree Kulkarni\nScott Little\nCurtis Madsen\nChris Myers\nNicholas Seegmiller\n"
					+ "Robert Thacker\nDavid Walter\nZhen Zhang";
		}
		else if (atacs) {
			name = new JLabel("ATACS", SwingConstants.CENTER);
			version = new JLabel("Version "+atacsVersion, SwingConstants.CENTER);
			developers = "Wendy Belluomini\nJeff Cuthbert\nHans Jacobson\nKevin Jones\nSung-Tae Jung\n"
					+ "Christopher Krieger\nScott Little\nCurtis Madsen\nEric Mercer\nChris Myers\n"
					+ "Curt Nelson\nEric Peskin\nNicholas Seegmiller\nDavid Walter\nHao Zheng";
		}
		else {
			name = new JLabel("iBioSim", SwingConstants.CENTER);
			version = new JLabel("Version "+iBioSimVersion, SwingConstants.CENTER);
			developers = "Nathan Barker\nScott Glass\nKevin Jones\nHiroyuki Kuwahara\n"
					+ "Curtis Madsen\nChris Myers\nNam Nguyen\nTyler Patterson\nNicholas Roehner\nJason Stevens\nLeandro Watanabe";
		}
		Font font = name.getFont();
		font = font.deriveFont(Font.BOLD, 36.0f);
		name.setFont(font);
		JLabel uOfU = new JLabel("University of Utah", SwingConstants.CENTER);
		JButton credits = new JButton("Credits");
		credits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Close" };
				JOptionPane.showOptionDialog(f, developers, "Credits", JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		});
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		JPanel buttons = new JPanel();
		buttons.add(credits);
		buttons.add(close);
		JPanel aboutPanel = new JPanel(new BorderLayout());
		JPanel uOfUPanel = new JPanel(new BorderLayout());
		uOfUPanel.add(name, "North");
		uOfUPanel.add(version, "Center");
		uOfUPanel.add(uOfU, "South");
		if (lema) {
			aboutPanel.add(new javax.swing.JLabel(
					new javax.swing.ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator + "LEMA.png")), "North");
		}
		else if (atacs) {
			aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator
					+ "ATACS.png")), "North");
		}
		else {
			aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(ENVVAR + separator + "gui" + separator + "icons" + separator
					+ "iBioSim.png")), "North");
		}
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

	public boolean exit() {
		int autosave = 0;
		for (int i = 0; i < tab.getTabCount(); i++) {
			int save = save(i, autosave);
			if (save == 0) {
				return false;
			}
			else if (save == 2) {
				autosave = 1;
			}
			else if (save == 3) {
				autosave = 2;
			}
		}
		Preferences biosimrc = Preferences.userRoot();
		for (int i = 0; i < numberRecentProj; i++) {
			if (atacs) {
				biosimrc.put("atacs.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("atacs.recent.project.path." + i, recentProjectPaths[i]);
			}
			else if (lema) {
				biosimrc.put("lema.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("lema.recent.project.path." + i, recentProjectPaths[i]);
			}
			else {
				biosimrc.put("biosim.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("biosim.recent.project.path." + i, recentProjectPaths[i]);
			}
		}
		for (int i = numberRecentProj; i < 10 ; i++) {
			if (atacs) {
				biosimrc.put("atacs.recent.project." + i,"");
				biosimrc.put("atacs.recent.project.path." + i,"");
			} else if (lema) {
				biosimrc.put("lema.recent.project." + i,"");
				biosimrc.put("lema.recent.project.path." + i,"");
			} else {
				biosimrc.put("biosim.recent.project." + i,"");
				biosimrc.put("biosim.recent.project.path," + i,"");
			}
		}
		System.exit(1);
		return true;
	}

	/**
	 * This method performs different functions depending on what menu items are
	 * selected.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == viewCircuit) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof LearnGCM) {
					((LearnGCM) component).viewModel();
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
			if (comp instanceof Verification) {
				((Verification) comp).viewLog();
			}
			else if (comp instanceof JPanel) {
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Synthesis) {
					((Synthesis) array[0]).viewLog();
				}
			}
			else if (comp instanceof JTabbedPane) {
				for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++) {
					Component component = ((JTabbedPane) comp).getComponent(i);
					if (component instanceof LearnGCM) {
						((LearnGCM) component).viewLog();
						return;
					}
					else if (component instanceof LearnLHPN) {
						((LearnLHPN) component).viewLog();
						return;
					}
				}
			}
		}
		else if (e.getSource() == viewCoverage) {
			Component comp = tab.getSelectedComponent();
			for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++) {
				Component component = ((JTabbedPane) comp).getComponent(i);
				if (component instanceof LearnLHPN) {
					((LearnLHPN) component).viewCoverage();
					return;
				}
			}
		}
		else if (e.getSource() == saveModel) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane) {
				for (Component component : ((JTabbedPane) comp).getComponents()) {
					if (component instanceof LearnGCM) {
						((LearnGCM) component).saveModel();
					}
					else if (component instanceof LearnLHPN) {
						((LearnLHPN) component).saveLhpn();
					}
				}
			}
		}
		else if (e.getSource() == saveAsVerilog) {
			Lpn2verilog lpn2verilog = new Lpn2verilog(tree.getFile());
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".lpn", ".sv"));
		}
		else if (e.getSource() == close && tab.getSelectedComponent() != null) {
			Component comp = tab.getSelectedComponent();
			Point point = comp.getLocation();
			tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(), point.x, point.y, 0, false), tab.getSelectedIndex());
		}
		else if (e.getSource() == saveAll) {
			int autosave = 0;
			for (int i = 0; i < tab.getTabCount(); i++) {
				int save = save(i, autosave);
				if (save == 0) {
					break;
				}
				else if (save == 2) {
					autosave = 1;
				}
				else if (save == 3) {
					autosave = 2;
				}
				markTabClean(i);
			}
		}
		else if (e.getSource() == closeAll) {
			while (tab.getSelectedComponent() != null) {
				int index = tab.getSelectedIndex();
				Component comp = tab.getComponent(index);
				Point point = comp.getLocation();
				tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(), point.x, point.y, 0, false), index);
			}
		}
		else if (e.getSource() == viewRules) {
			Component comp = tab.getSelectedComponent();
			Component[] array = ((JPanel) comp).getComponents();
			((Synthesis) array[0]).viewRules();
		}
		else if (e.getSource() == viewTrace) {
			Component comp = tab.getSelectedComponent();
			if (comp.getName().equals("Verification")) {
				Component[] array = ((JPanel) comp).getComponents();
				((Verification) array[0]).viewTrace();
			}
			else if (comp.getName().equals("Synthesis")) {
				Component[] array = ((JPanel) comp).getComponents();
				((Synthesis) array[0]).viewTrace();
			}
		}
		else if (e.getSource() == exportFlatSBML) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).exportFlatSBML();
			}
		}
		else if (e.getSource() == exportSBML) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).exportSBML();
			}
		}
		else if (e.getSource() == exportSBOL) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).exportSBOL();
			}
		}
		else if (e.getSource() == saveSBOL) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).saveSBOL();
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
				
				if (((JTabbedPane) comp).getSelectedComponent().getName().equals("ModelViewMovie"))
					((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputJPG(-1, false);
				else				
					((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(0);
			}
			else if (comp instanceof ModelEditor) {				
				((ModelEditor) comp).saveSchematic();
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
		else if (e.getSource() == exportAvi) {
			
			Component comp = tab.getSelectedComponent();
			
			if (comp instanceof JTabbedPane) {
				
				((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputMovie("avi");
			}
		}
		else if (e.getSource() == exportMp4) {
			
			Component comp = tab.getSelectedComponent();
			
			if (comp instanceof JTabbedPane) {
				
				((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputMovie("mp4");
			}
		}
		else if (e.getSource() == about) {
			about();
		}
		else if (e.getSource() == bugReport) {
			submitBugReport("");
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
					directory = ENVVAR + "/docs/";
					command = "gnome-open ";
				}
				else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					directory = ENVVAR + "/docs/";
					command = "open ";
				}
				else {
					directory = ENVVAR + "\\docs\\";
					command = "cmd /c start ";
				}
				File work = new File(directory);
				log.addText("Executing:\n" + command + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec(command + theFile, null, work);
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Unable to open manual.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the exit menu item is selected
		else if (e.getSource() == exit) {
			exit();
		}
		// if the open popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("openSim")) {
			try {
				openSim();
			}
			catch (Exception e0) {
			}
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
		else if (e.getActionCommand().equals("convertToSBML")) {
			Translator t1 = new Translator();
			t1.convertLPN2SBML(tree.getFile(), "");
			t1.outputSBML();
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			//updateOpenSBML(theFile.replace(".lpn", ".xml"));
			addToTree(theFile.replace(".lpn", ".xml"));
		}
		else if (e.getActionCommand().equals("convertToVerilog")) {
			Lpn2verilog lpn2verilog = new Lpn2verilog(tree.getFile());
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".lpn", ".sv"));
		}
		
		else if (e.getActionCommand().equals("convertToLPN")) {
			BuildProperty prop = new BuildProperty();
			try {
				BuildProperty.buildProperty(tree.getFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (RecognitionException e1) {
				e1.printStackTrace();
			}
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0) {
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".prop", ".lpn"));
		}
		
		else if (e.getActionCommand().equals("createAnalysis")) {
			try {
				createAnalysisView(2);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "You must select a valid lpn file for simulation.", "Error", JOptionPane.ERROR_MESSAGE);
			}

		}
		// if the create simulation popup menu is selected on a dot file
		else if (e.getActionCommand().equals("createSim")) {
			try {
				createAnalysisView(1);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "You must select a valid model file for simulation.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the simulate popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("simulate")) {
			try {
				createAnalysisView(0);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "You must select a valid sbml file for simulation.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the synthesis popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createSynthesis")) {
			if (root != null) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0) {
							return;
						}
						break;
					}
				}
				if (!async) {
					createSBOLSynthesisView();
				} else {
					String synthName = JOptionPane.showInputDialog(frame, "Enter Synthesis ID:", "Synthesis View ID", JOptionPane.PLAIN_MESSAGE);
					if (synthName != null && !synthName.trim().equals("")) {
						synthName = synthName.trim();
						try {
							if (overwrite(root + separator + synthName, synthName)) {
								new File(root + separator + synthName).mkdir();
								String sbmlFile = tree.getFile();
								String[] getFilename = sbmlFile.split(separator);
								String circuitFileNoPath = getFilename[getFilename.length - 1];
								try {
									FileOutputStream out = new FileOutputStream(new File(root + separator + synthName.trim() + separator
											+ synthName.trim() + ".syn"));
									out.write(("synthesis.file=" + circuitFileNoPath + "\n").getBytes());
									out.close();
								}
								catch (IOException e1) {
									JOptionPane
									.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
								}
								try {
									FileInputStream in = new FileInputStream(new File(root + separator + circuitFileNoPath));
									FileOutputStream out = new FileOutputStream(new File(root + separator + synthName.trim() + separator
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
									JOptionPane.showMessageDialog(frame, "Unable to copy circuit file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
								}
								addToTree(synthName.trim());
								String work = root + separator + synthName;
								String circuitFile = root + separator + synthName.trim() + separator + circuitFileNoPath;
								JPanel synthPane = new JPanel();
								Synthesis synth = new Synthesis(work, circuitFile, log, this);
								synthPane.add(synth);
								addTab(synthName, synthPane, "Synthesis");
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(frame, "Unable to create Synthesis View directory.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the verify popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createVerify")) {
			if (root != null) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0) {
							return;
						}
						break;
					}
				}
				String verName = JOptionPane.showInputDialog(frame, "Enter Verification ID:", "Verification View ID", JOptionPane.PLAIN_MESSAGE);
				if (verName != null && !verName.trim().equals("")) {
					verName = verName.trim();
					// try {
					if (overwrite(root + separator + verName, verName)) {
						new File(root + separator + verName).mkdir();
						String sbmlFile = tree.getFile();
						String[] getFilename = sbmlFile.split(separator);
						String circuitFileNoPath = getFilename[getFilename.length - 1];
						try {
							FileOutputStream out = new FileOutputStream(new File(root + separator + verName.trim() + separator + verName.trim()
									+ ".ver"));
							out.write(("verification.file=" + circuitFileNoPath + "\n").getBytes());
							out.close();
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
						}
						addToTree(verName.trim());
						Verification verify = new Verification(root + separator + verName, verName, circuitFileNoPath, log, this, lema, atacs);
						verify.save();
						addTab(verName, verify, "Verification");
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the delete popup menu is selected
		else if (e.getActionCommand().contains("delete") || e.getSource() == delete) {
			delete(tree.getFile());
		}
		else if (e.getActionCommand().equals("openLPN")) {
			openLPN();
		}
		else if (e.getActionCommand().equals("browseSbol")) {
			openSBOL();
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("gcmEditor")) {
			openGCM(false);
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("gcmTextEditor")) {
			openGCM(true);
		}
		// if the edit popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("sbmlEditor")) {
			openSBML(tree.getFile());
		}
		else if (e.getActionCommand().equals("stateGraph")) {
			try {
				String directory = root + separator + getTitleAt(tab.getSelectedIndex());
				File work = new File(directory);
				for (String f : new File(directory).list()) {
					if (f.contains("_sg.dot")) {
						Runtime exec = Runtime.getRuntime();
						if (System.getProperty("os.name").contentEquals("Linux")) {
							log.addText("Executing:\ndotty " + directory + separator + f + "\n");
							exec.exec("dotty " + f, null, work);
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							log.addText("Executing:\nopen " + directory + separator + f + "\n");
							exec.exec("open " + f, null, work);
						}
						else {
							log.addText("Executing:\ndotty " + directory + separator + f + "\n");
							exec.exec("dotty " + f, null, work);
						}
						return;
					}
				}
				JOptionPane.showMessageDialog(frame, "State graph file has not been generated.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("graphTree")) {
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
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			File work = new File(directory);
			String out = theFile;
			try {
				if (out.contains(".lpn")) {
					String file = theFile;
					String[] findTheFile = file.split("\\.");
					theFile = findTheFile[0] + ".dot";
					File dot = new File(root + separator + theFile);
					dot.delete();
					LhpnFile lhpn = new LhpnFile(log);
					lhpn.load(directory + separator + theFile);
					lhpn.printDot(directory + separator + file);
					// String cmd = "atacs -cPllodpl " + file;
					Runtime exec = Runtime.getRuntime();
					// Process ATACS = exec.exec(cmd, null, work);
					// ATACS.waitFor();
					// log.addText("Executing:\n" + cmd);
					if (dot.exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = ENVVAR + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = ENVVAR + "/docs/";
							command = "open ";
						}
						else {
							// directory = ENVVAR + "\\docs\\";
							command = "dotty start ";
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
						JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
					}
					return;
				}
				if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".gcm")) {
					try {
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
						return;
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				Run run = new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				JList empty = new JList();
				//JRadioButton emptyButton = new JRadioButton();
				Run.createProperties(0, "Print Interval", 1, 1, 1, 1, directory, 314159, 1, 1, new String[0], "tsd.printer", "amount", "false",
						(directory + theFile).split(separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty, empty, 
						null, false, false, false);
				log.addText("Executing:\nreb2sac --target.encoding=dot --out=" + directory + out + ".dot " + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec("reb2sac --target.encoding=dot --out=" + out + ".dot " + theFile, null, work);
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
				if (theFile.substring(theFile.length() - 4).equals("sbml")) {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == viewLearnedModel) {
			Component comp = tab.getSelectedComponent();
			for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++) {
				Component component = ((JTabbedPane) comp).getComponent(i);
				if (component instanceof LearnGCM) {
					((LearnGCM) component).viewModel();
					return;
				}
				else if (component instanceof LearnLHPN) {
					((LearnLHPN) component).viewLhpn();
					return;
				}
			}
		}
		// if the graph popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("graph")) {
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
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			// }
			File work = new File(directory);
			String out = theFile;
			try {
				if (out.contains(".lpn")) {
					String file = theFile;
					String[] findTheFile = file.split("\\.");
					theFile = findTheFile[0] + ".dot";
					File dot = new File(root + separator + theFile);
					dot.delete();
					LhpnFile lhpn = new LhpnFile(log);
					lhpn.load(root + separator + file);
					lhpn.printDot(root + separator + theFile);
					// String cmd = "atacs -cPllodpl " + file;
					Runtime exec = Runtime.getRuntime();
					// Process ATACS = exec.exec(cmd, null, work);
					// ATACS.waitFor();
					// log.addText("Executing:\n" + cmd);
					if (dot.exists()) {
						String command = "";
						if (System.getProperty("os.name").contentEquals("Linux")) {
							// directory = ENVVAR + "/docs/";
							command = "gnome-open ";
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
							// directory = ENVVAR + "/docs/";
							command = "open ";
						}
						else {
							// directory = ENVVAR + "\\docs\\";
							command = "dotty start ";
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
						JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
					}
					return;
				}
				if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml")) {
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml")) {
					out = out.substring(0, out.length() - 4);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".gcm")) {
					try {
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
						return;
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				Run run = new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				JList empty = new JList();
				//JRadioButton emptyButton = new JRadioButton();
				Run.createProperties(0, "Print Interval", 1, 1, 1, 1, directory, 314159, 1, 1, new String[0], "tsd.printer", "amount", "false",
						(directory + theFile).split(separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty, empty, 
						null, false, false, false);
				log.addText("Executing:\nreb2sac --target.encoding=dot --out=" + directory + out + ".dot " + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec("reb2sac --target.encoding=dot --out=" + out + ".dot " + theFile, null, work);
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
				if (theFile.substring(theFile.length() - 4).equals("sbml")) {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the browse popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("browse")) {
			String directory;
			String theFile;
			String filename = tree.getFile();
			directory = "";
			theFile = "";
			if (filename.lastIndexOf('/') >= 0) {
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0) {
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			// }
			File work = new File(directory);
			String out = theFile;
			if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml")) {
				out = out.substring(0, out.length() - 5);
			}
			else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml")) {
				out = out.substring(0, out.length() - 4);
			}
			try {
				Run run = new Run(null);
				JCheckBox dummy = new JCheckBox();
				JList empty = new JList();
				dummy.setSelected(false);
				Run.createProperties(0.0, "Print Interval", 1.0, 1.0, 1.0, 1.0, directory, 314159L, 1, 1, new String[0], "tsd.printer", "amount", "false",
						(directory + theFile).split(separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty, empty, 
						null, false, false, false);
				log.addText("Executing:\nreb2sac --target.encoding=xhtml --out=" + directory + out + ".xhtml " + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process browse = exec.exec("reb2sac --target.encoding=xhtml --out=" + out + ".xhtml " + theFile, null, work);
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
				if (theFile.substring(theFile.length() - 4).equals("sbml")) {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else {
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Error viewing SBML file in a browser.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the save button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("save")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor) {
				((LHPNEditor) comp).save();
			}
			else if (comp instanceof ModelEditor) {
				((ModelEditor) comp).save("Save GCM");
			}
			/*
			else if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).save(false, "", true, true);
			}
			*/
			else if (comp instanceof Graph) {
				((Graph) comp).save();
			}
			else if (comp instanceof Verification) {
				((Verification) comp).save();
			}
			else if (comp instanceof JTabbedPane) {
				if (comp instanceof SynthesisView)
					((SynthesisView) comp).save();
				else {
					for (Component component : ((JTabbedPane) comp).getComponents()) {
						int index = ((JTabbedPane) comp).getSelectedIndex();
						if (component instanceof Graph) {
							((Graph) component).save();
						}
						else if (component instanceof LearnGCM) {
							((LearnGCM) component).save();
						}
						else if (component instanceof LearnLHPN) {
							((LearnLHPN) component).save();
						}
						else if (component instanceof DataManager) {
							((DataManager) component).saveChanges(((JTabbedPane) comp).getTitleAt(index));
						}
						/*
					else if (component instanceof SBML_Editor) {
						((SBML_Editor) component).save(false, "", true, true);
					}
						 */
						else if (component instanceof ModelEditor) {
							((ModelEditor) component).saveParams(false, "", true, null);
						}
						else if (component instanceof AnalysisView) {
							((AnalysisView) component).save();
						}
						else if (component instanceof MovieContainer) {
							((MovieContainer) component).savePreferences();
						}
					}
				}
			}
			if (comp instanceof JPanel) {
				if (comp.getName().equals("Synthesis")) {
					// ((Synthesis) tab.getSelectedComponent()).save();
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
				}
			}
			else if (comp instanceof JScrollPane) {
				String fileName = getTitleAt(tab.getSelectedIndex());
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
							outStream.write(chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + separator + fileName);
					this.updateAsyncViews(fileName);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error saving file " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the save as button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("saveas")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor) {
				String newName = JOptionPane.showInputDialog(frame, "Enter LPN name:", "LPN Name", JOptionPane.PLAIN_MESSAGE);
				if (newName == null) {
					return;
				}
				if (!newName.endsWith(".lpn") && !newName.endsWith(".xml")) {
					newName = newName + ".lpn";
				}
				((LHPNEditor) comp).saveAs(newName);
				if (newName.endsWith(".lpn")) {
					tab.setTitleAt(tab.getSelectedIndex(), newName);
				}
			}
			else if (comp instanceof ModelEditor) {
				String newName = JOptionPane.showInputDialog(frame, "Enter model name:", "Model Name", JOptionPane.PLAIN_MESSAGE);
				if (newName == null) {
					return;
				}
				if (newName.contains(".gcm")) {
					newName = newName.replace(".gcm", "");
				}
				if (newName.contains(".xml")) {
					newName = newName.replace(".xml", "");
				}
				if (newName.endsWith(".lpn")) {
					((ModelEditor) comp).saveAsLPN(newName);
				} else {
					((ModelEditor) comp).saveAs(newName);
				}
			}
			/*
			else if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).saveAs();
			}
			*/
			else if (comp instanceof Graph) {
				((Graph) comp).saveAs();
			}
			else if (comp instanceof Verification) {
				((Verification) comp).saveAs();
			}
			else if (comp instanceof JTabbedPane) {
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph) {
					((Graph) component).saveAs();
				}
			}
			else if (comp instanceof JPanel) {
				if (comp.getName().equals("Synthesis")) {
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).saveAs();
				}
			}
			else if (comp instanceof JScrollPane) {
				String fileName = getTitleAt(tab.getSelectedIndex());
				String newName = "";
				if (fileName.endsWith(".vhd")) {
					newName = JOptionPane.showInputDialog(frame, "Enter VHDL name:", "VHDL Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".vhd")) {
						newName = newName + ".vhd";
					}
				}
				if (fileName.endsWith(".prop")) {     //DK
					newName = JOptionPane.showInputDialog(frame, "Enter Property name:", "Property Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".prop")) {
						newName = newName + ".prop";
					}
				}
				else if (fileName.endsWith(".s")) {
					newName = JOptionPane.showInputDialog(frame, "Enter Assembly File Name:", "Assembly File Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".s")) {
						newName = newName + ".s";
					}
				}
				else if (fileName.endsWith(".inst")) {
					newName = JOptionPane.showInputDialog(frame, "Enter Instruction File Name:", "Instruction File Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".inst")) {
						newName = newName + ".inst";
					}
				}
				else if (fileName.endsWith(".g")) {
					newName = JOptionPane.showInputDialog(frame, "Enter Petri net name:", "Petri net Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".g")) {
						newName = newName + ".g";
					}
				}
				else if (fileName.endsWith(".csp")) {
					newName = JOptionPane.showInputDialog(frame, "Enter CSP name:", "CSP Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".csp")) {
						newName = newName + ".csp";
					}
				}
				else if (fileName.endsWith(".hse")) {
					newName = JOptionPane.showInputDialog(frame, "Enter HSE name:", "HSE Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".hse")) {
						newName = newName + ".hse";
					}
				}
				else if (fileName.endsWith(".unc")) {
					newName = JOptionPane.showInputDialog(frame, "Enter UNC name:", "UNC Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null) {
						return;
					}
					if (!newName.endsWith(".unc")) {
						newName = newName + ".unc";
					}
				}
				else if (fileName.endsWith(".rsg")) {
					newName = JOptionPane.showInputDialog(frame, "Enter RSG name:", "RSG Name", JOptionPane.PLAIN_MESSAGE);
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
							outStream.write(chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + separator + newName);
					File oldFile = new File(root + separator + fileName);
					oldFile.delete();
					tab.setTitleAt(tab.getSelectedIndex(), newName);
					addToTree(newName);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error saving file " + newName, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the run button is selected on the tool bar
		else if (e.getActionCommand().equals("run")) {
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof JTabbedPane) {
				// int index = -1;
				if (comp instanceof SynthesisView) {
					synthesizeSBOL((SynthesisView) comp);
				} else {
					for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++) {
						Component component = ((JTabbedPane) comp).getComponent(i);
						if (component instanceof AnalysisView) {
							((AnalysisView) component).getRunButton().doClick();
							break;
						}
						else if (component instanceof LearnGCM) {
							((LearnGCM) component).save();
							new Thread((LearnGCM) component).start();
							break;
						}
						else if (component instanceof LearnLHPN) {
							((LearnLHPN) component).save();
							((LearnLHPN) component).learn();
							break;
						}
					}
				}
			}
			else if (comp.getName().equals("Verification")) {
				if (comp instanceof Verification) {
					((Verification) comp).save();
					new Thread((Verification) comp).start();
				}
				else {
					// Not sure if this is necessary anymore
					Component[] array = ((JPanel) comp).getComponents();
					((Verification) array[0]).save();
					new Thread((Verification) array[0]).start();
				}
			}
			else if (comp.getName().equals("Synthesis")) {
				if (comp instanceof Synthesis) {
					((Synthesis) comp).save();
					new Thread((Synthesis) comp).start();
				}
				else {
					// Not sure if this is necessary anymore
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
					new Thread((Synthesis) array[0]).start();
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
			else if (comp instanceof Graph) {
				((Graph) comp).refresh();
			}
		}
		else if (e.getActionCommand().equals("check")) {
			Component comp = tab.getSelectedComponent();
			/*if (comp instanceof SBML_Editor) {
				((SBML_Editor) comp).save(true, "", true, true);
				((SBML_Editor) comp).check();
			}
			else*/ if (comp instanceof ModelEditor) {
				((ModelEditor) comp).save("Save and Check GCM");
			}
		}
		else if (e.getActionCommand().equals("export")) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph) {
				((Graph) comp).export();
			}
			else if (comp instanceof ModelEditor) {
				((ModelEditor) comp).exportSBML();
				// TODO: should give choice of SBML or SBOL
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
			int autosave = 0;
			for (int i = 0; i < tab.getTabCount(); i++) {
				int save = save(i, autosave);
				if (save == 0) {
					return;
				}
				else if (save == 2) {
					autosave = 1;
				}
				else if (save == 3) {
					autosave = 2;
				}
			}
			File file;
			Preferences biosimrc = Preferences.userRoot();
			if (biosimrc.get("biosim.general.project_dir", "").equals("")) {
				file = null;
			}
			else {
				file = new File(biosimrc.get("biosim.general.project_dir", ""));
			}
			String filename;
			
			if (e.getActionCommand().startsWith(GlobalConstants.SBOL_SYNTH_COMMAND)) {
				filename = identifySBOLSynthesisPath(e.getActionCommand());
			} else
				filename = Utility.browse(frame, file, null, JFileChooser.DIRECTORIES_ONLY, "New", -1);
			if (!filename.trim().equals("")) {
				filename = filename.trim();
				biosimrc.put("biosim.general.project_dir", filename);
				File f = new File(filename);
				if (f.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(frame, "File already exists." + "\nDo you want to overwrite?", "Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
					if (lema) {
						new FileWriter(new File(filename + separator + "LEMA.prj")).close();
					}
					else if (atacs) {
						new FileWriter(new File(filename + separator + "ATACS.prj")).close();
					}
					else {
						new FileWriter(new File(filename + separator + "BioSim.prj")).close();
					}
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to create a new project.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				root = filename;
				refresh();
				tab.removeAll();
				addRecentProject(filename);

				//importDot.setEnabled(true);
				importMenu.setEnabled(true);
				importSbol.setEnabled(true);
				importSedml.setEnabled(true);
				importSbml.setEnabled(true);
				importBioModel.setEnabled(true);
				importVirtualPart.setEnabled(true);
				importVhdl.setEnabled(true);
				importS.setEnabled(true);
				importInst.setEnabled(true);
				importLpn.setEnabled(true);
				importG.setEnabled(true);
				importCsp.setEnabled(true);
				importHse.setEnabled(true);
				importUnc.setEnabled(true);
				importRsg.setEnabled(true);
				importSpice.setEnabled(true);
				newSBMLModel.setEnabled(true);
				newGridModel.setEnabled(true);
				newVhdl.setEnabled(true);
				newProperty.setEnabled(true);   //DK
				newS.setEnabled(true);
				newInst.setEnabled(true);
				newLhpn.setEnabled(true);
				newG.setEnabled(true);
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
			EditPreferences editPreferences = new EditPreferences(frame,async,tree);
			editPreferences.preferences();
		}
		else if (e.getSource() == clearRecent) {
			removeAllRecentProjects();
		}
		else if ((e.getSource() == openProj) || (e.getSource() == recentProjects[0]) || (e.getSource() == recentProjects[1])
				|| (e.getSource() == recentProjects[2]) || (e.getSource() == recentProjects[3]) || (e.getSource() == recentProjects[4])
				|| (e.getSource() == recentProjects[5]) || (e.getSource() == recentProjects[6]) || (e.getSource() == recentProjects[7])
				|| (e.getSource() == recentProjects[8]) || (e.getSource() == recentProjects[9])) {
			int autosave = 0;
			for (int i = 0; i < tab.getTabCount(); i++) {
				int save = save(i, autosave);
				if (save == 0) {
					return;
				}
				else if (save == 2) {
					autosave = 1;
				}
				else if (save == 3) {
					autosave = 2;
				}
			}
			Preferences biosimrc = Preferences.userRoot();
			String projDir = "";
			if (e.getSource() == openProj) {
				File file;
				if (biosimrc.get("biosim.general.project_dir", "").equals("")) {
					file = null;
				}
				else {
					file = new File(biosimrc.get("biosim.general.project_dir", ""));
				}
				projDir = Utility.browse(frame, file, null, JFileChooser.DIRECTORIES_ONLY, "Open", -1);
				if (projDir.endsWith(".prj")) {
					biosimrc.put("biosim.general.project_dir", projDir);
					String[] tempArray = projDir.split(separator);
					projDir = "";
					for (int i = 0; i < tempArray.length - 1; i++) {
						projDir = projDir + tempArray[i] + separator;
					}
				}
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
			else if (e.getSource() == recentProjects[5]) {
				projDir = recentProjectPaths[5];
			}
			else if (e.getSource() == recentProjects[6]) {
				projDir = recentProjectPaths[6];
			}
			else if (e.getSource() == recentProjects[7]) {
				projDir = recentProjectPaths[7];
			}
			else if (e.getSource() == recentProjects[8]) {
				projDir = recentProjectPaths[8];
			}
			else if (e.getSource() == recentProjects[9]) {
				projDir = recentProjectPaths[9];
			}
			// log.addText(projDir);
			if (!projDir.equals("")) {
				biosimrc.put("biosim.general.project_dir", projDir);
				if (new File(projDir).isDirectory()) {
					boolean isProject = false;
					for (String temp : new File(projDir).list()) {
						if (temp.equals(".prj")) {
							isProject = true;
						}
						if (lema && temp.equals("LEMA.prj")) {
							isProject = true;
						}
						else if (atacs && temp.equals("ATACS.prj")) {
							isProject = true;
						}
						else if (temp.equals("BioSim.prj")) {
							isProject = true;
						}
					}
					if (isProject) {
						root = projDir;
						refresh();
						tab.removeAll();
						addRecentProject(projDir);
						//importDot.setEnabled(true);
						importMenu.setEnabled(true);
						importSbol.setEnabled(true);
						importSedml.setEnabled(true);
						importSbml.setEnabled(true);
						importBioModel.setEnabled(true);
						importVirtualPart.setEnabled(true);
						importVhdl.setEnabled(true);
						importS.setEnabled(true);
						importInst.setEnabled(true);
						importLpn.setEnabled(true);
						importG.setEnabled(true);
						importCsp.setEnabled(true);
						importHse.setEnabled(true);
						importUnc.setEnabled(true);
						importRsg.setEnabled(true);
						importSpice.setEnabled(true);
						newSBMLModel.setEnabled(true);
						newGridModel.setEnabled(true);
						newVhdl.setEnabled(true);
						newS.setEnabled(true);
						newInst.setEnabled(true);
						newLhpn.setEnabled(true);
						newProperty.setEnabled(true);  //DK
						newG.setEnabled(true);
						newCsp.setEnabled(true);
						newHse.setEnabled(true);
						newUnc.setEnabled(true);
						newRsg.setEnabled(true);
						newSpice.setEnabled(true);
						graph.setEnabled(true);
						probGraph.setEnabled(true);
					}
					else {
						JOptionPane.showMessageDialog(frame, "You must select a valid project.", "Error", JOptionPane.ERROR_MESSAGE);
						removeRecentProject(projDir);
					}
				}
				else {
					JOptionPane.showMessageDialog(frame, "You must select a valid project.", "Error", JOptionPane.ERROR_MESSAGE);
					removeRecentProject(projDir);
				}
			}
		}
		// if the new circuit model menu item is selected
		else if (e.getSource() == newSBMLModel) {
			createModel(false);
		}
		else if (e.getSource() == newGridModel) {
			createModel(true);
		}
		// if the new vhdl menu item is selected
		else if (e.getSource() == newVhdl) {
			newModel("VHDL", ".vhd");
		}
		// if the new assembly menu item is selected
		else if (e.getSource() == newS) {
			newModel("Assembly", ".s");
		}
		// if the new instruction file menu item is selected
		else if (e.getSource() == newInst) {
			newModel("Instruction", ".inst");
		}
		// if the new petri net menu item is selected
		else if (e.getSource() == newG) {
			newModel("Petri Net", ".g");
		}
		// if the new lhpn menu item is selected
		else if (e.getSource() == newLhpn) {
			createLPN();
		}
		else if (e.getSource() == newProperty) {  //DK
			newModel("Property", ".prop");
		} 
		// if the new csp menu item is selected
		else if (e.getSource() == newCsp) {
			newModel("CSP", ".csp");
		}
		// if the new hse menu item is selected
		else if (e.getSource() == newHse) {
			newModel("Handshaking Expansion", ".hse");
		}
		// if the new unc menu item is selected
		else if (e.getSource() == newUnc) {
			newModel("Extended Burst Mode Machine", ".unc");
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newRsg) {
			newModel("Reduced State Graph", ".rsg");
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newSpice) {
			newModel("Spice Circuit", ".cir");
		}
		else if (e.getSource().equals(importSbol)) {
			importFile("SBOL", ".sbol", ".xml");
		}
		else if (e.getSource().equals(importSedml)) {
			importSEDML();
		}
		// if the import sbml menu item is selected
		else if (e.getSource() == importSbml) {
			importSBML(null);
		}
		else if (e.getSource() == importBioModel) {
			importBioModel();
		}
		else if (e.getSource() == importVirtualPart) {
			importVirtualPart();
		}
		// if the import dot menu item is selected
		/*
		else if (e.getSource() == importDot) {
			importGCM();
		}
		*/
		// if the import vhdl menu item is selected
		else if (e.getSource() == importVhdl) {
			importFile("VHDL", ".vhd", ".vhd");
		}
		else if (e.getSource() == importS) {
			importFile("Assembly", ".s", ".s");
		}
		else if (e.getSource() == importInst) {
			importFile("Instruction", ".inst", ".inst");
		}
		else if (e.getSource() == importLpn) {
			importLPN();
		}
		else if (e.getSource() == importG) {
			importFile("Petri Net", ".g", ".g");
		}
		// if the import csp menu item is selected
		else if (e.getSource() == importCsp) {
			importFile("CSP", ".csp", ".csp");
		}
		// if the import hse menu item is selected
		else if (e.getSource() == importHse) {
			importFile("Handshaking Expansion", ".hse", ".hse");
		}
		// if the import unc menu item is selected
		else if (e.getSource() == importUnc) {
			importFile("Extended Burst State Machine", ".unc", ".unc");
		}
		// if the import rsg menu item is selected
		else if (e.getSource() == importRsg) {
			importFile("Reduced State Graph", ".rsg", ".rsg");
		}
		// if the import spice menu item is selected
		else if (e.getSource() == importSpice) {
			importFile("Spice Circuit", ".cir", ".cir");
		}
		// if the Graph data menu item is clicked
		else if (e.getSource() == graph) {
			createGraph();
		}
		else if (e.getSource() == probGraph) {
			createHistogram();
		}
		else if (e.getActionCommand().equals("createLearn")) {
			createLearn();
		}
		else if (e.getActionCommand().equals("viewModel")) {
			viewModel();
		}
		else if (e.getSource() == select) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).select();
			}
		}
		else if (e.getSource() == cut) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).cut();
			}
		}
		else if (e.getSource() == addCompartment) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addCompartment();
			}
		}
		else if (e.getSource() == addSpecies) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addSpecies();
			}
		}
		else if (e.getSource() == addReaction) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addReaction();
			}
		}
		else if (e.getSource() == addComponent) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addComponent();
			}
		}
		else if (e.getSource() == addPromoter) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addPromoter();
			}
		}
		else if (e.getSource() == addVariable) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addVariable();
			}
		}
		else if (e.getSource() == addBoolean) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addBoolean();
			}
		}
		else if (e.getSource() == addPlace) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addPlace();
			}
		}
		else if (e.getSource() == addTransition) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addTransition();
			}
		}
		else if (e.getSource() == addRule) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addRule();
			}
		}
		else if (e.getSource() == addConstraint) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addConstraint();
			}
		}
		else if (e.getSource() == addEvent) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addEvent();
			}
		}
		else if (e.getSource() == addSelfInfl) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).addSelfInfluence();
			}
		}
		else if (e.getSource() == moveLeft) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).moveLeft();
			}
		}
		else if (e.getSource() == moveRight) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).moveRight();
			}
		}
		else if (e.getSource() == moveUp) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).moveUp();
			}
		}
		else if (e.getSource() == moveDown) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).moveDown();
			}
		}
		else if (e.getSource() == undo) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).undo();
			}
		}
		else if (e.getSource() == redo) {
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor) {
				((ModelEditor) comp).redo();
			}
		}
		else if (e.getActionCommand().equals("copy") || e.getSource() == copy) {
			copy();
		}
		else if (e.getActionCommand().equals("rename") || e.getSource() == rename) {
			rename();
		}
		else if (e.getActionCommand().equals("openGraph")) {
			openGraph();
		}
		else if (e.getActionCommand().equals("openHistogram")) {
			openHistogram();
		}
		enableTabMenu(tab.getSelectedIndex());
		enableTreeMenu();
	}
	
	private void delete(String fullPath) {
		if (!fullPath.equals(root)) {
			int value = JOptionPane.YES_OPTION;
			if (new File(fullPath).isDirectory()) {
				String dirName = fullPath.split(separator)[fullPath.split(separator).length - 1];
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (getTitleAt(i).equals(dirName)) {
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
				deleteFromTree(dirName);
			}
			else {
				String[] views = canDelete(fullPath.split(separator)[fullPath.split(separator).length - 1]);
				if (views.length != 0) {
					String view = "";
					String gcms = "";
					for (int i = 0; i < views.length; i++) {
						if (views[i].endsWith(".xml")) {
							gcms += views[i] + "\n";
						}
						else {
							view += views[i] + "\n";
						}
					}
					String message = "Unable to delete the selected file.\n";
					if (!views.equals("")) {
						message += "\nIt is linked to the following views:\n" + view;
					}
					if (!gcms.equals("")) {
						message += "\nIt is linked to the following models:\n" + gcms;
					}
					message += "\nDelete file and all files that reference it?";
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					Object[] options = { "Yes", "No" };
					value = JOptionPane.showOptionDialog(frame, scroll, "Unable to Delete File", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
							null, options, options[1]);
				}
				if (value == JOptionPane.YES_OPTION) {
					for (int i = 0; i < views.length; i++) {
						delete(root + separator + views[i]);
					}
					String fileName = fullPath.split(separator)[fullPath.split(separator).length - 1];
					for (int i = 0; i < tab.getTabCount(); i++) {
						if (getTitleAt(i).equals(fileName)) {
							tab.remove(i);
						}
					}
					System.gc();
					if (fullPath.endsWith(".xml")) {
						SBMLDocument document = SBMLutilities.readSBML(fullPath);
						List<URI> sbolURIs = new LinkedList<URI>();
						AnnotationUtility.parseSBOLAnnotation(document.getModel(), sbolURIs);
						Iterator<URI> sbolIterator = sbolURIs.iterator();
						while (sbolIterator != null && sbolIterator.hasNext()) {
							URI sbolURI = sbolIterator.next();
							if (sbolURI.toString().endsWith("iBioSim")) {
								sbolIterator = null;
								for (String filePath : getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
									SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
									SBOLUtility.deleteDNAComponent(sbolURI, sbolDoc);
									SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
								}
							}
						} 
						new File(fullPath.replace(".xml", ".gcm")).delete();
					}
					new File(fullPath).delete();
					deleteFromTree(fileName);
				}
			}
		}
	}

	private void importBioModel() {
		final BioModelsWSClient client = new BioModelsWSClient();
		if (BioModelIds == null) {
			try {
				BioModelIds = client.getAllCuratedModelsId();
			}
			catch (BioModelsWSException e2) {
				JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		final JPanel BioModelsPanel = new JPanel(new BorderLayout());
		final JList ListOfBioModels = new JList();
		sort(BioModelIds);
		ListOfBioModels.setListData(BioModelIds);
		ListOfBioModels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JLabel TextBioModels = new JLabel("List of BioModels");
		JScrollPane ScrollBioModels = new JScrollPane();
		ScrollBioModels.setMinimumSize(new Dimension(520, 250));
		ScrollBioModels.setPreferredSize(new Dimension(552, 250));
		ScrollBioModels.setViewportView(ListOfBioModels);
		JPanel GetButtons = new JPanel();
		JButton GetNames = new JButton("Get Names");
		JButton GetDescription = new JButton("Get Description");
		JButton GetReference = new JButton("Get Reference");
		final JProgressBar progressBar = new JProgressBar(0,100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		runGetNames = true;
		final Thread getNamesThread = new Thread(new Runnable() {
		    @Override
			public void run() {
		    	for (int i = 0; i < BioModelIds.length && runGetNames; i++) {
					try {
						progressBar.setValue(100 * i / BioModelIds.length);
						if (!BioModelIds[i].contains(" ")) {
							BioModelIds[i] += " " + client.getModelNameById(BioModelIds[i]);
							ListOfBioModels.setListData(BioModelIds);
							ListOfBioModels.revalidate();
							ListOfBioModels.repaint();
						}
					}
					catch (BioModelsWSException e1) {
						JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				ListOfBioModels.setListData(BioModelIds);
				runGetNames = false;
		    }
		});
		GetNames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (runGetNames && !getNamesThread.isAlive()) {
					getNamesThread.start();
				}
			}
		});
		GetDescription.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ListOfBioModels.isSelectionEmpty()) return;
				String SelectedModel = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
				String command = "";
				if (System.getProperty("os.name").contentEquals("Linux")) {
					command = "gnome-open http://www.ebi.ac.uk/compneur-srv/biomodels-main/" + SelectedModel;
				}
				else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					command = "open http://www.ebi.ac.uk/compneur-srv/biomodels-main/" + SelectedModel;
				}
				else {
					command = "cmd /c start http://www.ebi.ac.uk/compneur-srv/biomodels-main/" + SelectedModel;
				}
				log.addText("Executing:\n" + command + "\n");
				Runtime exec = Runtime.getRuntime();
				try {
					exec.exec(command);
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to open model description.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		GetReference.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ListOfBioModels.isSelectionEmpty()) return;
				String SelectedModel = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
				try {
					String Pub = (client.getSimpleModelById(SelectedModel)).getPublicationId();
					String command = "";
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=" + Pub;
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						command = "open http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=" + Pub;
					}
					else {
						command = "cmd /c start http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=" + Pub;
					}
					log.addText("Executing:\n" + command + "\n");
					Runtime exec = Runtime.getRuntime();
					exec.exec(command);
				}
				catch (BioModelsWSException e2) {
					JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Unable to open model description.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		GetButtons.add(GetNames);
		GetButtons.add(GetDescription);
		GetButtons.add(GetReference);
		GetButtons.add(progressBar);
		BioModelsPanel.add(TextBioModels, "North");
		BioModelsPanel.add(ScrollBioModels, "Center");
		BioModelsPanel.add(GetButtons, "South");
		Object[] options = { "OK", "Cancel" };
		int value = JOptionPane.showOptionDialog(frame, BioModelsPanel, "List of BioModels", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		runGetNames = false;
		if (value == JOptionPane.YES_OPTION && ListOfBioModels.getSelectedValue() != null) {
			String ModelId = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
			String filename = ModelId + ".xml";
			try {
				if (overwrite(root + separator + filename, filename)) {
					String model = client.getModelSBMLById(ModelId);
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(root + separator + filename), "UTF-8"));
					out.write(model);
					out.close();
					String[] file = filename.trim().split(separator);
					SBMLDocument document = SBMLutilities.readSBML(root + separator + filename.trim());
					SBMLutilities.check(root + separator + filename.trim(), document, false, false);
					SBMLWriter writer = new SBMLWriter();
					writer.writeSBMLToFile(document, root + separator + file[file.length - 1]);
					addToTree(file[file.length - 1]);
					openSBML(root + separator + file[file.length - 1]);
				}
			}
			catch (MalformedURLException e1) {
				JOptionPane.showMessageDialog(frame, e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				filename = "";
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, filename + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
				filename = "";
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void importVirtualPart() {
		final PartsHandler partsHandler = new PartsHandler("http://sbol.ncl.ac.uk:8081/");
		if (!showParts && getPartsThread == null) {
			getPartsThread = new Thread(new Runnable() {
			    @Override
				public void run() {
					try {
						final JButton cancel = new JButton("Cancel");
						final MutableBoolean stop = new MutableBoolean(false);
						cancel.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								stop.setValue(true);
							}
						});
						final JFrame running = new JFrame("Progress");
						WindowListener w = new WindowListener() {
							@Override
							public void windowClosing(WindowEvent arg0) {
								running.setCursor(null);
								cancel.doClick();
								running.dispose();
							}

							@Override
							public void windowOpened(WindowEvent arg0) {
							}

							@Override
							public void windowClosed(WindowEvent arg0) {
							}

							@Override
							public void windowIconified(WindowEvent arg0) {
							}

							@Override
							public void windowDeiconified(WindowEvent arg0) {
							}

							@Override
							public void windowActivated(WindowEvent arg0) {
							}

							@Override
							public void windowDeactivated(WindowEvent arg0) {
							}
						};
						running.addWindowListener(w);
						JPanel text = new JPanel();
						JPanel progBar = new JPanel();
						JPanel button = new JPanel();
						JPanel all = new JPanel(new BorderLayout());
						JLabel label = new JLabel("Retrieving Virtual Parts");
						Summary summary = partsHandler.GetPartsSummary();
						int pageCount = summary.getPageCount();
						JProgressBar progress = new JProgressBar(0, pageCount);
						progress.setStringPainted(true);
						progress.setValue(0);
						text.add(label);
						progBar.add(progress);
						button.add(cancel);
						all.add(text, "North");
						all.add(progBar, "Center");
						all.add(button, "South");
						running.setContentPane(all);
						running.pack();
						Dimension screenSize;
						try {
							Toolkit tk = Toolkit.getDefaultToolkit();
							screenSize = tk.getScreenSize();
						}
						catch (AWTError awe) {
							screenSize = new Dimension(640, 480);
						}
						Dimension frameSize = running.getSize();

						if (frameSize.height > screenSize.height) {
							frameSize.height = screenSize.height;
						}
						if (frameSize.width > screenSize.width) {
							frameSize.width = screenSize.width;
						}
						int x = screenSize.width / 2 - frameSize.width / 2;
						int y = screenSize.height / 2 - frameSize.height / 2;
						running.setLocation(x, y);
						running.setVisible(true);
						running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						allVirtualParts = new Parts();
						for (int i = 1; i <= pageCount && !stop.booleanValue(); i++) {
							Parts parts = partsHandler.GetParts(i);
							allVirtualParts.getParts().addAll(parts.getParts());
							progress.setValue(i);
						}
						running.setCursor(null);
						running.dispose();
						if (!stop.booleanValue()) {
							showParts = true;
							importVirtualPart();
						}
						else {
							getPartsThread = null;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			});
			getPartsThread.start();
		}
		else if (showParts) {
			final List<Part> list = allVirtualParts.getParts();
			final JPanel virtualPartsPanel = new JPanel(new BorderLayout());
			final JPanel labelPanel = new JPanel(new BorderLayout());
			TableModel dataModel = new AbstractTableModel() {
				public int getColumnCount() {
					return 6;
				}

				public int getRowCount() {
					return list.size();
				}

				public Object getValueAt(int row, int col) {
					Part p = list.get(row);
					switch (col) {
					case 0:
						return p.getName();
					case 1:
						return p.getType();
					case 2:
						return p.getDisplayName();
					case 3:
						return p.getOrganism();
					case 4:
						return p.getDescription();
					default:
						return row;
					}
				}

				public String getColumnName(int col) {
					switch (col) {
					case 0:
						return "ID";
					case 1:
						return "Type";
					case 2:
						return "Name";
					case 3:
						return "Organism";
					case 4:
						return "Description";
					default:
						return "Entry";
					}
				}
			};
			final JTable tableOfVirtualParts = new JTable(dataModel);
			tableOfVirtualParts.setAutoCreateRowSorter(true);
			tableOfVirtualParts.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tableOfVirtualParts.getColumnModel().getColumn(0).setPreferredWidth(150);
			tableOfVirtualParts.getColumnModel().getColumn(1).setPreferredWidth(150);
			tableOfVirtualParts.getColumnModel().getColumn(2).setPreferredWidth(150);
			tableOfVirtualParts.getColumnModel().getColumn(3).setPreferredWidth(150);
			tableOfVirtualParts.getColumnModel().getColumn(4).setPreferredWidth(150);
			tableOfVirtualParts.getColumnModel().getColumn(5).setMinWidth(0);
			tableOfVirtualParts.getColumnModel().getColumn(5).setMaxWidth(0);
			tableOfVirtualParts.getColumnModel().getColumn(5).setWidth(0);
			tableOfVirtualParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableOfVirtualParts.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					try {
						if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
							int selected = tableOfVirtualParts.getSelectedRow();
							if (selected >= 0) {
								Part part = list.get((Integer) tableOfVirtualParts.getModel().getValueAt(tableOfVirtualParts.convertRowIndexToModel(selected), 5));
								final Interactions interactions = partsHandler.GetInteractions(part);
								if (interactions != null && interactions.getInteractions() != null) {
									TableModel dataModel = new AbstractTableModel() {
										public int getColumnCount() {
											return 4;
										}

										public int getRowCount() {
											return interactions.getInteractions().size();
										}

										public Object getValueAt(int row, int col) {
											Interaction i = interactions.getInteractions().get(row);
											switch (col) {
											case 0:
												return i.getName();
											case 1:
												String parts = "";
												for (String p : i.getParts()) {
													parts += p + ", ";
												}
												return parts.substring(0, parts.length() - 2);
											case 2:
												return i.getInteractionType();
											default:
												return i.getDescription();
											}
										}

										public String getColumnName(int col) {
											switch (col) {
											case 0:
												return "ID";
											case 1:
												return "Parts";
											case 2:
												return "Type";
											default:
												return "Description";
											}
										}
									};
									JTable tableOfInteractions = new JTable(dataModel);
									tableOfInteractions.setAutoCreateRowSorter(true);
									tableOfInteractions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
									tableOfInteractions.getColumnModel().getColumn(0).setPreferredWidth(150);
									tableOfInteractions.getColumnModel().getColumn(1).setPreferredWidth(150);
									tableOfInteractions.getColumnModel().getColumn(2).setPreferredWidth(150);
									tableOfInteractions.getColumnModel().getColumn(3).setPreferredWidth(150);
									JScrollPane ScrollInteractions = new JScrollPane();
									ScrollInteractions.setMinimumSize(new Dimension(520, 150));
									ScrollInteractions.setPreferredSize(new Dimension(552, 150));
									ScrollInteractions.setViewportView(tableOfInteractions);
									JOptionPane.showMessageDialog(frame, ScrollInteractions, "Interactions for Part " + part.getName(),
											JOptionPane.PLAIN_MESSAGE);
								}
								else {
									JOptionPane.showMessageDialog(frame, "There are no interactions associated with this part in the repository.", "No Interactions",
											JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XMLStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				@Override
				public void mouseEntered(MouseEvent arg0) {						
				}
				@Override
				public void mouseExited(MouseEvent arg0) {
				}
				@Override
				public void mousePressed(MouseEvent arg0) {
				}
				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
			});
			JLabel TextVirtualParts = new JLabel("List of Virtual Parts:");
			JLabel DoubleClick = new JLabel("Double click on a part to view its interactions.");
			JScrollPane ScrollVirtualParts = new JScrollPane();
			ScrollVirtualParts.setMinimumSize(new Dimension(520, 250));
			ScrollVirtualParts.setPreferredSize(new Dimension(552, 250));
			ScrollVirtualParts.setViewportView(tableOfVirtualParts);
			labelPanel.add(TextVirtualParts, "North");
			labelPanel.add(DoubleClick, "Center");
			virtualPartsPanel.add(labelPanel, "North");
			virtualPartsPanel.add(ScrollVirtualParts, "Center");
			Object[] options = { "Import Part", "Import Part's Interactions", "Cancel" };
			showVirtualPartImportOption(virtualPartsPanel, options, tableOfVirtualParts, partsHandler);
		}
	}
	
	private void showVirtualPartImportOption(JPanel virtualPartsPanel, Object[] options, JTable tableOfVirtualParts, PartsHandler partsHandler) {
		try {
			List<Part> list = allVirtualParts.getParts();
			int value = JOptionPane.showOptionDialog(frame,	virtualPartsPanel, "List of Virtual Parts", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			int selected = tableOfVirtualParts.getSelectedRow();
			if (selected >= 0) {
				Part part = list.get((Integer) tableOfVirtualParts.getModel().getValueAt(tableOfVirtualParts.convertRowIndexToModel(selected), 5));
				if (value == JOptionPane.YES_OPTION) {
					SBMLDocument sbmlDocument = partsHandler.GetModel(part);
					if (sbmlDocument != null) {
						SBMLWriter writer = new SBMLWriter();
						writer.writeSBMLToFile(sbmlDocument, root + separator + part.getName() + ".xml.temp");
						SBMLDocument document = SBMLutilities.readSBML(root + separator + part.getName() + ".xml.temp");
						SBMLutilities.checkModelCompleteness(document);
						SBMLutilities.check(root + separator + part.getName() + ".xml.temp", document, false, false);
						String newFile = part.getName() + ".xml";
						newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
						if (Character.isDigit(newFile.charAt(0))) {
							newFile = "M" + newFile;
						}
						if (document != null) {
							if (document.getModel().isSetId()) {
								newFile = document.getModel().getId();
							} else {
								document.getModel().setId(newFile.replace(".xml",""));
							}
							document.enablePackage(LayoutConstants.namespaceURI);
							document.enablePackage(CompConstants.namespaceURI);
							document.enablePackage(FBCConstants.namespaceURI);
							// TODO: workaround
							document.getSBMLDocumentAttributes().put("fbc:required", "false");
							CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
							CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
							if (documentComp.getListOfModelDefinitions().size() > 0 ||
								documentComp.getListOfExternalModelDefinitions().size() > 0) {
								if (!extractModelDefinitions(documentComp,documentCompModel))
									JOptionPane.showMessageDialog(frame, "Unable to extract model definitions from the model.", "Unable to Extract Model Definitions",
											JOptionPane.ERROR_MESSAGE);;
							}
							updateReplacementsDeletions(document, documentComp, documentCompModel);
							if (document.getModel().getId()==null||document.getModel().getId().equals("")) {
								document.getModel().setId(newFile.replace(".xml",""));
							} else {
								newFile = document.getModel().getId()+".xml";
							}
							if (overwrite(root + separator + newFile, newFile)) {
								writer.writeSBMLToFile(document, root + separator + newFile);
								addToTree(newFile);
								openSBML(root + separator + newFile);
							}
							new File(root + separator + part.getName() + ".xml.temp").delete();
						}
					}
					else {
						JOptionPane.showMessageDialog(frame, "There is no SBML model associated with this part in the repository.", "No SBML Model",
								JOptionPane.ERROR_MESSAGE);
						showVirtualPartImportOption(virtualPartsPanel, options, tableOfVirtualParts, partsHandler);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					Interactions interactions=partsHandler.GetInteractions(part);
					if (interactions != null && interactions.getInteractions() != null) {
						for (Interaction interaction : interactions.getInteractions()) {
							SBMLHandler sbmlHandler = new SBMLHandler();
							SBMLDocument sbmlContainer=sbmlHandler.GetSBMLTemplateModel(interaction.getName() + "_model");
							ModelBuilder modelBuilder = new ModelBuilder(sbmlContainer);
							SBMLDocument sbmlDocumentPart = partsHandler.GetModel(part);
							modelBuilder.Add(sbmlDocumentPart);
							for (String p : interaction.getParts()) {
								if (!p.equals(part.getName())) {
									for (Part tempPart : list) {
										if (p.equals(tempPart.getName())) {
											SBMLDocument sbmlDocument = partsHandler.GetModel(tempPart);
											if (sbmlDocument != null) {
												modelBuilder.Add(sbmlDocument);
//												SBMLWriter writer = new SBMLWriter();
//												writer.writeSBMLToFile(sbmlDocument, root + separator + tempPart.getName() + ".xml.temp");
//												SBMLDocument document = SBMLutilities.readSBML(root + separator + tempPart.getName() + ".xml.temp");
//												String newFile = tempPart.getName() + ".xml";
//												newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
//												if (Character.isDigit(newFile.charAt(0))) {
//													newFile = "M" + newFile;
//												}
//												if (document != null) {
//													if (document.getModel().isSetId()) {
//														newFile = document.getModel().getId();
//													} else {
//														document.getModel().setId(newFile.replace(".xml",""));
//													}
//													document.addPackageDeclaration(LayoutConstants.shortLabel, LayoutConstants.namespaceURI, false);
//													document.addPackageDeclaration(CompConstants.shortLabel, CompConstants.namespaceURI, true);
//													document.addPackageDeclaration(FBCConstants.shortLabel, FBCConstants.namespaceURI, false);
//													CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
//													CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
//													if (documentComp.getListOfModelDefinitions().size() > 0 ||
//														documentComp.getListOfExternalModelDefinitions().size() > 0) {
//														if (!extractModelDefinitions(documentComp,documentCompModel))
//															JOptionPane.showMessageDialog(frame, "Unable to extract model definitions from the model.", "Unable to Extract Model Definitions",
//																	JOptionPane.ERROR_MESSAGE);;
//													}
//													updateReplacementsDeletions(document, documentComp, documentCompModel);
//													if (document.getModel().getId()==null||document.getModel().getId().equals("")) {
//														document.getModel().setId(newFile.replace(".xml",""));
//													} else {
//														newFile = document.getModel().getId()+".xml";
//													}
//													writer.writeSBMLToFile(document, root + separator + newFile);
//													addToTree(newFile);
//													new File(root + separator + tempPart.getName() + ".xml.temp").delete();
//												}
											}
										}
									}
								}
							}
							SBMLDocument sbmlDocument=partsHandler.GetInteractionModel(interaction);
							if (sbmlDocument != null) {
								modelBuilder.Add(sbmlDocument);
								SBMLWriter writer = new SBMLWriter();
								writer.writeSBMLToFile(modelBuilder.GetModel(), root + separator + interaction.getName() + ".xml.temp");
								SBMLDocument document = SBMLutilities.readSBML(root + separator + interaction.getName() + ".xml.temp");
								String newFile = interaction.getName() + ".xml";
								newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
								if (Character.isDigit(newFile.charAt(0))) {
									newFile = "M" + newFile;
								}
								if (document != null) {
									if (document.getModel().isSetId()) {
										newFile = document.getModel().getId();
									} else {
										document.getModel().setId(newFile.replace(".xml",""));
									}
									document.enablePackage(LayoutConstants.namespaceURI);
									document.enablePackage(CompConstants.namespaceURI);
									document.enablePackage(FBCConstants.namespaceURI);
									// TODO: workaround
									document.getSBMLDocumentAttributes().put("fbc:required", "false");
									CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
									CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
									if (documentComp.getListOfModelDefinitions().size() > 0 ||
										documentComp.getListOfExternalModelDefinitions().size() > 0) {
										if (!extractModelDefinitions(documentComp,documentCompModel))
											JOptionPane.showMessageDialog(frame, "Unable to extract model definitions from the model.", "Unable to Extract Model Definitions",
													JOptionPane.ERROR_MESSAGE);;
									}
									updateReplacementsDeletions(document, documentComp, documentCompModel);
									if (document.getModel().getId()==null||document.getModel().getId().equals("")) {
										document.getModel().setId(newFile.replace(".xml",""));
									} else {
										newFile = document.getModel().getId()+".xml";
									}
									if (overwrite(root + separator + newFile, newFile)) {
										writer.writeSBMLToFile(document, root + separator + newFile);
										addToTree(newFile);
										openSBML(root + separator + newFile);
									}
									new File(root + separator + interaction.getName() + ".xml.temp").delete();
								}
							}
						}
					}
					else {
						JOptionPane.showMessageDialog(frame, "There are no interactions associated with this part in the repository.", "No Interactions",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void createLPN() {
		try {
			String lhpnName = JOptionPane.showInputDialog(frame, "Enter LPN Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
			if (lhpnName != null && !lhpnName.trim().equals("")) {
				lhpnName = lhpnName.trim();
				if (lhpnName.length() > 3) {
					if (!lhpnName.substring(lhpnName.length() - 4).equals(".lpn")) {
						lhpnName += ".lpn";
					}
				}
				else {
					lhpnName += ".lpn";
				}
				String modelID = "";
				if (lhpnName.length() > 3) {
					if (lhpnName.substring(lhpnName.length() - 4).equals(".lpn")) {
						modelID = lhpnName.substring(0, lhpnName.length() - 4);
					}
					else {
						modelID = lhpnName.substring(0, lhpnName.length() - 3);
					}
				}
				if (!(IDpat.matcher(modelID).matches())) {
					JOptionPane.showMessageDialog(frame, "A model ID can only contain letters, numbers, and underscores.", "Invalid ID",
							JOptionPane.ERROR_MESSAGE);
				}
				else {
					if (overwrite(root + separator + lhpnName, lhpnName)) {
						File f = new File(root + separator + lhpnName);
						f.createNewFile();
						new LhpnFile(log).save(f.getAbsolutePath());
						int i = getTab(f.getName());
						if (i != -1) {
							tab.remove(i);
						}
						LHPNEditor lhpn = new LHPNEditor(root + separator, f.getName(), null, this);
						// lhpn.addMouseListener(this);
						addTab(f.getName(), lhpn, "LHPN Editor");
						addToTree(f.getName());
					}
				}
			}
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void importLPN() {
		File importFile;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
			importFile = null;
		}
		else {
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import LPN", -1);
		if ((filename.length() > 1 && !filename.substring(filename.length() - 2, filename.length()).equals(".g"))
				&& (filename.length() > 3 && !filename.substring(filename.length() - 4, filename.length()).equals(".lpn"))) {
			JOptionPane.showMessageDialog(frame, "You must select a valid LPN file to import.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (!filename.equals("")) {
			biosimrc.put("biosim.general.import_dir", filename);
			String[] file = filename.split(separator);
			try {
				if (new File(filename).exists()) {
					file[file.length - 1] = file[file.length - 1].replaceAll("[^a-zA-Z0-9_.]+", "_");
					if (Character.isDigit(file[file.length - 1].charAt(0))) {
						file[file.length - 1] = "M" + file[file.length - 1];
					}
					if (checkFiles(root + separator + file[file.length - 1], filename.trim())) {
						if (overwrite(root + separator + file[file.length - 1], file[file.length - 1])) {
							// Identify which LPN format is imported.
							BufferedReader input = new BufferedReader(new FileReader(filename));
							String str;
							boolean lpnUSF = false;
							while ((str = input.readLine()) != null) {
								if (str.startsWith(".")) {
									break;
								}
								else if (str.startsWith("<")) {
									lpnUSF = true;
									break;
								}
								else {
									JOptionPane.showMessageDialog(frame, "LPN file format is not valid.", "Error", JOptionPane.ERROR_MESSAGE);
									break;
								}
									
							}
							if (!lpnUSF) {
								String outFileName = file[file.length - 1];
								
								if (/*!lema &&*/ !atacs) { 
									Translator t1 = new Translator();
									t1.convertLPN2SBML(filename, "");
									t1.setFilename(root + separator + outFileName.replace(".lpn", ".xml"));
									t1.outputSBML();
									outFileName = outFileName.replace(".lpn", ".xml");
								} else {
									FileOutputStream out = new FileOutputStream(new File(root + separator + outFileName));
									FileInputStream in = new FileInputStream(new File(filename));
									// log.addText(filename);
									int read = in.read();							
									while (read != -1) {
										out.write(read);
										read = in.read();
									}
									in.close();
									out.close();
								}
								addToTree(outFileName);
							}
							else {
								ANTLRFileStream in = new ANTLRFileStream(filename);
								PlatuGrammarLexer lexer = new PlatuGrammarLexer(in);
								TokenStream tokenStream = new CommonTokenStream(lexer);
								PlatuGrammarParser antlrParser = new PlatuGrammarParser(tokenStream);
								Set<LhpnFile> lpnSet = antlrParser.lpn();
								for (LhpnFile lpn : lpnSet) {
									lpn.save(root + separator + lpn.getLabel() + ".lpn");
									addToTree(lpn.getLabel() + ".lpn");
								}
							}
						}
					}
				}
				if (filename.substring(filename.length() - 2, filename.length()).equals(".g")) {
					// log.addText(filename + file[file.length - 1]);
					File work = new File(root);
					String oldName = root + separator + file[file.length - 1];
					// String newName = oldName.replace(".lpn",
					// "_NEW.g");
					Process atacs = Runtime.getRuntime().exec("atacs -lgsl " + oldName, null, work);
					atacs.waitFor();
					String lpnName = oldName.replace(".g", ".lpn");
					String newName = oldName.replace(".g", "_NEW.lpn");
					atacs = Runtime.getRuntime().exec("atacs -llsl " + lpnName, null, work);
					atacs.waitFor();
					lpnName = lpnName.replaceAll("[^a-zA-Z0-9_.]+", "_");
					if (Character.isDigit(lpnName.charAt(0))) {
						lpnName = "M" + lpnName;
					}
					FileOutputStream out = new FileOutputStream(new File(lpnName));
					FileInputStream in = new FileInputStream(new File(newName));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
					new File(newName).delete();
					addToTree(file[file.length - 1].replace(".g", ".lpn"));
				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/*
	private void importGCM() {
		File importFile;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
			importFile = null;
		}
		else {
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_AND_DIRECTORIES, "Import Genetic Circuit", -1);
		if (filename != null && !filename.trim().equals("")) {
			biosimrc.put("biosim.general.import_dir", filename.trim());
		}
		if (new File(filename.trim()).isDirectory()) {
			for (String s : new File(filename.trim()).list()) {
				if (!(filename.trim() + separator + s).equals("")
						&& (filename.trim() + separator + s).length() > 3
						&& (filename.trim() + separator + s).substring((filename.trim() + separator + s).length() - 4,
								(filename.trim() + separator + s).length()).equals(".gcm")) {
					try {
						// GCMParser parser =
						new GCMParser((filename.trim() + separator + s));
						s = s.replaceAll("[^a-zA-Z0-9_.]+", "_");
						if (overwrite(root + separator + s, s)) {
							FileOutputStream out = new FileOutputStream(new File(root + separator + s));
							FileInputStream in = new FileInputStream(new File((filename.trim() + separator + s)));
							int read = in.read();
							while (read != -1) {
								out.write(read);
								read = in.read();
							}
							in.close();
							out.close();
							addToTree(s);
							importSBML(filename.trim().replace(".gcm", ".xml"));
						}
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else {
			if (filename.trim().length() > 3 && !filename.trim().substring(filename.trim().length() - 4, filename.trim().length()).equals(".gcm")) {
				JOptionPane.showMessageDialog(frame, "You must select a valid gcm file to import.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (!filename.trim().equals("")) {
				String[] file = filename.trim().split(separator);
				try {
					// GCMParser parser =
					new GCMParser(filename.trim());
					file[file.length - 1] = file[file.length - 1].replaceAll("[^a-zA-Z0-9_.]+", "_");
					if (checkFiles(root + separator + file[file.length - 1], filename.trim())) {
						if (overwrite(root + separator + file[file.length - 1], file[file.length - 1])) {
							FileOutputStream out = new FileOutputStream(new File(root + separator + file[file.length - 1]));
							FileInputStream in = new FileInputStream(new File(filename.trim()));
							int read = in.read();
							while (read != -1) {
								out.write(read);
								read = in.read();
							}
							in.close();
							out.close();
							addToTree(file[file.length - 1]);
							importSBML(filename.trim().replace(".gcm", ".xml"));
						}
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	*/
	
/*
	private void createSBML() {
		try {
			String simName = JOptionPane.showInputDialog(frame, "Enter SBML Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.trim().equals("")) {
				simName = simName.trim();
				if (simName.length() > 4) {
					if (!simName.substring(simName.length() - 5).equals(".sbml") && !simName.substring(simName.length() - 4).equals(".xml")) {
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
					JOptionPane.showMessageDialog(frame, "A model ID can only contain letters, numbers, and underscores.", "Invalid ID",
							JOptionPane.ERROR_MESSAGE);
				}
				else {
					if (overwrite(root + separator + simName, simName)) {
						String f = new String(root + separator + simName);
						SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
						document.createModel();
						Compartment c = document.getModel().createCompartment();
						c.setId("default");
						c.setSize(1.0);
						c.setSpatialDimensions(3);
						document.getModel().setId(modelID);
						SBMLWriter writer = new SBMLWriter();
						writer.writeSBML(document, root + separator + simName);
						SBML_Editor sbml = new SBML_Editor(f, null, log, this, null, null);
						addTab(f.split(separator)[f.split(separator).length - 1], sbml, "SBML Editor");
						addToTree(f.split(separator)[f.split(separator).length - 1]);
					}
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
*/
	
	private void createModel(boolean grid) {
		if (root != null) {
			try {

				String modelId = null;

				JTextField modelChooser = new JTextField("");
				modelChooser.setColumns(20);

				JPanel modelPanel = new JPanel(new GridLayout(2, 1));
				modelPanel.add(new JLabel("Enter Model ID: "));
				modelPanel.add(modelChooser);
				frame.add(modelPanel);

				String[] options = { GlobalConstants.OK, GlobalConstants.CANCEL };

				int okCancel = JOptionPane.showOptionDialog(frame, modelPanel, "Model ID", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);

				// if the user clicks "ok" on the panel
				if (okCancel == JOptionPane.OK_OPTION) {

					modelId = modelChooser.getText();
				}
				else
					return;

				// String simName = JOptionPane.showInputDialog(frame,
				// "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
				if (modelId != null && !modelId.trim().equals("")) {
					modelId = modelId.trim();
					if (modelId.length() > 3) {
						if (!modelId.substring(modelId.length() - 4).equals(".xml")) {
							modelId += ".xml";
						}
					}
					else {
						modelId += ".xml";
					}
					if (!(IDpat.matcher(modelId.replace(".xml","")).matches())) {
						JOptionPane.showMessageDialog(frame, "A model ID can only contain letters, numbers, and underscores.", "Invalid ID",
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						if (overwrite(root + separator + modelId, modelId)) {
							BioModel bioModel = new BioModel(root);
							bioModel.createSBMLDocument(modelId.replace(".xml", ""),grid,lema);
							bioModel.save(root + separator + modelId);
							int i = getTab(modelId);
							if (i != -1) {
								tab.remove(i);
							}
							ModelEditor modelEditor = new ModelEditor(root + separator, modelId, this, log, false, null, null, null, false, grid);
							modelEditor.save("Save GCM");
							addTab(modelId, modelEditor, "GCM Editor");
							addToTree(modelId);
						}
					}
				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private String importSBML(String filename) {
		String newFile = null;
		Preferences biosimrc = Preferences.userRoot();
		if (filename == null) {
			File importFile;
			if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
				importFile = null;
			}
			else {
				importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
			}
			filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_AND_DIRECTORIES, "Import SBML", -1);
		}
		if (!filename.trim().equals("")) {
			biosimrc.put("biosim.general.import_dir", filename.trim());
			if (new File(filename.trim()).isDirectory()) {
				for (String s : new File(filename.trim()).list()) {
					if (s.endsWith(".xml") || s.endsWith(".sbml")) {
						try {
							SBMLDocument document = SBMLutilities.readSBML(filename.trim() + separator + s);
							SBMLutilities.checkModelCompleteness(document);
							if (overwrite(root + separator + s, s)) {
								SBMLutilities.check(filename.trim(), document, false, false);
								SBMLWriter writer = new SBMLWriter();
								s = s.replaceAll("[^a-zA-Z0-9_.]+", "_");
								writer.writeSBMLToFile(document, root + separator + s);
							}
						}
						catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(frame, "Unable to import files.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					addToTree(s);
				}
			}
			else {
				String[] file = filename.trim().split(separator);
				try {
					SBMLDocument document = SBMLutilities.readSBML(filename.trim());
					if (overwrite(root + separator + file[file.length - 1], file[file.length - 1])) {
						SBMLutilities.checkModelCompleteness(document);
						SBMLutilities.check(filename.trim(), document, false, false);
						newFile = file[file.length - 1];
						newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
						if (Character.isDigit(newFile.charAt(0))) {
							newFile = "M" + newFile;
						}
						if (document != null) {
							if (document.getModel().isSetId()) {
								newFile = document.getModel().getId();
							} else {
								document.getModel().setId(newFile.replace(".xml",""));
							}
							document.enablePackage(LayoutConstants.namespaceURI);
							document.enablePackage(CompConstants.namespaceURI);
							document.enablePackage(FBCConstants.namespaceURI);
							// TODO: workaround
							document.getSBMLDocumentAttributes().put("fbc:required", "false");
							CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
							CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
							if (documentComp.getListOfModelDefinitions().size() > 0 ||
								documentComp.getListOfExternalModelDefinitions().size() > 0) {
								if (!extractModelDefinitions(documentComp,documentCompModel)) return null;
							}
							updateReplacementsDeletions(document, documentComp, documentCompModel);
							SBMLWriter writer = new SBMLWriter();
							if (document.getModel().getId()==null||document.getModel().getId().equals("")) {
								document.getModel().setId(newFile.replace(".xml",""));
							} else {
								newFile = document.getModel().getId()+".xml";
							}
							writer.writeSBMLToFile(document, root + separator + newFile);
							addToTree(newFile);
							openSBML(root + separator + newFile);
						}
					}
				}
				catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return newFile;
	}
	
	private void performAnalysis(String modelId, String simName, SEDMLDocument sedmlDoc) throws Exception {
		String sbmlFile = root + separator + modelId + ".xml";
		String[] sbml1 = null;
		File sedmlFile = new File(root + separator + simName + separator + modelId + "-sedml.xml");
		sedmlDoc.writeDocument(sedmlFile);
		sbml1 = sbmlFile.split(separator);
		String sbmlFileProp;
		sbmlFileProp = root + separator + simName + separator + sbml1[sbml1.length - 1];
		new FileOutputStream(new File(sbmlFileProp)).close();
		try {
			FileOutputStream out = new FileOutputStream(new File(root + separator + simName.trim() + separator + simName.trim() + ".sim"));
			out.write((sbml1[sbml1.length - 1] + "\n").getBytes());
			out.close();
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		addToTree(simName);
		JTabbedPane simTab = new JTabbedPane();
		simTab.addMouseListener(this);
		AnalysisView reb2sac = new AnalysisView(sbmlFile, sbmlFileProp, root, this, simName.trim(), log, simTab, null, sbml1[sbml1.length - 1], null);
		simTab.addTab("Simulation Options", reb2sac);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
		simTab.addTab("Advanced Options", reb2sac.getAdvanced());
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		String gcmFile = sbml1[sbml1.length - 1].replace(".xml", ".gcm");
		ModelEditor modelEditor = new ModelEditor(root + separator, gcmFile, this, log, true, simName.trim(), root
				+ separator + simName.trim() + separator + simName.trim() + ".sim", reb2sac, false, false);
		reb2sac.setGcm(modelEditor);
		ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
				root + separator + simName.trim() + separator + simName.trim() + ".sim");
		modelEditor.setElementsPanel(elementsPanel);
		addModelViewTab(reb2sac, simTab, modelEditor);
		simTab.addTab("Parameters", modelEditor);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("GCM Editor");
		//simTab.addTab("SBML Elements", elementsPanel);
		//simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		modelEditor.createSBML("",".", null);
		reb2sac.run(".", true);
		Graph tsdGraph;
		tsdGraph = reb2sac.createGraph(root + separator + simName + separator + simName + ".grf");
		simTab.addTab("TSD Graph", tsdGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
		Graph probGraph = reb2sac.createProbGraph(null);
		simTab.addTab("Histogram", probGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
		addTab(simName, simTab, null);
	}

	private void importSEDML() {
		Preferences biosimrc = Preferences.userRoot();
		File importFile;
		if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
			importFile = null;
		}
		else {
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import SED-ML", -1);
		if (!filename.trim().equals("")) {
			biosimrc.put("biosim.general.import_dir", filename.trim());
			String[] file = filename.trim().split(separator);
			try {
				File sedmlFile = new File(filename.trim());
				SEDMLDocument sedmlDoc = Libsedml.readDocument(sedmlFile);
				sedmlDoc.validate();
				if(sedmlDoc.hasErrors()) {
					List<SedMLError> errors = sedmlDoc.getErrors();
					final JFrame f = new JFrame("SED-ML Errors and Warnings");
					JTextArea messageArea = new JTextArea();
					messageArea.append("Imported SED-ML file contains the errors listed below. ");
					messageArea.append("It is recommended that you fix them before performing this analysis or you may get unexpected results.\n\n");
					for (int i = 0; i < errors.size(); i++) {
						SedMLError error = errors.get(i);
						messageArea.append(i + ":" + error.getMessage() + "\n");
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
						@Override
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
				SedML sedml = sedmlDoc.getSedMLModel();
				List<Model> models = sedml.getModels();
				HashMap<String,String> modelMap = new HashMap<String,String>();
				for (int i = 0; i < models.size(); i++) {
					Model model = models.get(i);
					String sbmlFile = filename.substring(0,filename.lastIndexOf(separator)) + model.getSource();
					String newFile = importSBML(sbmlFile);
					model.setSource(newFile);
					modelMap.put(model.getId(), newFile);
				}
				List<Task> tasks = sedml.getTasks();
				for (int i = 0; i < tasks.size(); i++) {
					Task task = tasks.get(i);
					if (modelMap.containsKey(task.getModelReference())) {
						String modelId = modelMap.get(task.getModelReference()).replace(".xml", "");
						String analysisId = modelId;
						if (tasks.size()>1) {
							analysisId += "_" + task.getId();
						}
						if (overwrite(root + separator + analysisId, analysisId)) {
							new File(root + separator + analysisId).mkdir();
							String newFile = file[file.length - 1];
							newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
							List<Output> outputs = sedml.getOutputs();
							if (outputs.size() > 0 && outputs.get(0).isPlot2d()) {
								Plot2D plot = (Plot2D)outputs.get(0);
								Properties graph = new Properties();
								graph.setProperty("title", plot.getName());
								graph.setProperty("chart.background.paint", "" + new java.awt.Color(238, 238, 238).getRGB());
								graph.setProperty("plot.background.paint", "" + java.awt.Color.WHITE.getRGB());
								graph.setProperty("plot.domain.grid.line.paint", "" + java.awt.Color.LIGHT_GRAY.getRGB());
								graph.setProperty("plot.range.grid.line.paint", "" + java.awt.Color.LIGHT_GRAY.getRGB());
								graph.setProperty("x.axis", "");
								graph.setProperty("y.axis", "");
								graph.setProperty("x.min", "0.0");
								graph.setProperty("x.max", "1.0");
								graph.setProperty("x.scale", "0.1");
								graph.setProperty("y.min", "0.0");
								graph.setProperty("y.max", "1.0");
								graph.setProperty("y.scale", "0.1");
								graph.setProperty("auto.resize", "true");
								graph.setProperty("LogX", "false");
								graph.setProperty("LogY", "false");
								graph.setProperty("visibleLegend", "true");
								List<Curve> curves = plot.getListOfCurves();
								for (int j = 0; j < curves.size(); j++) {
									Curve curve = curves.get(j);
									graph.setProperty("species.connected." + j, "true");
									graph.setProperty("species.filled." + j, "true");
									graph.setProperty("species.xnumber." + j, "0");
									graph.setProperty("species.number." + j, "" + j);
									graph.setProperty("species.run.number." + j, "run-" + 1);
									graph.setProperty("species.name." + j, curve.getName() + " (1)");
									graph.setProperty("species.id." + j, curve.getName() + " (1)");
									graph.setProperty("species.visible." + j, "true");
									graph.setProperty("species.paint." + j, "Black");
									graph.setProperty("species.shape." + j, "Circle");
									graph.setProperty("species.directory." + j, "");
								}
								try {
									FileOutputStream store = new FileOutputStream(new 
											File(root + separator + analysisId + separator + analysisId + ".grf"));
									graph.store(store, "Graph Data");
									store.close();
									log.addText("Creating graph file:\n" + root + separator + analysisId + separator + analysisId + ".grf" + "\n");
								}
								catch (Exception except) {
									JOptionPane.showMessageDialog(Gui.frame, "Unable To Save Graph!", "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
							performAnalysis(modelId,analysisId,sedmlDoc);
						}
					}
				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importFile(String fileType, String extension1, String extension2) {
		File importFile;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
			importFile = null;
		}
		else {
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import " + fileType, -1);
		if (filename.length() > 1 && !filename.endsWith(extension1) && !filename.endsWith(extension2)) {
			JOptionPane.showMessageDialog(frame, "You must select a valid " + fileType + " file to import.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (!filename.equals("")) {
			biosimrc.put("biosim.general.import_dir", filename);
			String[] file = filename.split(separator);
			try {
				file[file.length - 1] = file[file.length - 1].replaceAll("[^a-zA-Z0-9_.]+", "_");
				file[file.length - 1] = file[file.length - 1].replaceAll(extension2, extension1);
				if (checkFiles(root + separator + file[file.length - 1], filename.trim())) {
					if (overwrite(root + separator + file[file.length - 1], file[file.length - 1])) {
						FileOutputStream out = new FileOutputStream(new File(root + separator + file[file.length - 1]));
						FileInputStream in = new FileInputStream(new File(filename));
						int read = in.read();
						while (read != -1) {
							out.write(read);
							read = in.read();
						}
						in.close();
						out.close();
						addToTree(file[file.length - 1]);
					}
				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createGraph() {
		String graphName = JOptionPane.showInputDialog(frame, "Enter A Name For The TSD Graph:", "TSD Graph Name", JOptionPane.PLAIN_MESSAGE);
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
				Graph g = new Graph(null, "Number of molecules", graphName.trim().substring(0, graphName.length() - 4), "tsd.printer", root, "Time",
						this, null, log, graphName.trim(), true, false);
				addTab(graphName.trim(), g, "TSD Graph");
				g.save();
				addToTree(graphName.trim());
			}
		}
	}

	private void createHistogram() {
		String graphName = JOptionPane.showInputDialog(frame, "Enter A Name For The Histogram:", "Histogram Name", JOptionPane.PLAIN_MESSAGE);
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
				Graph g = new Graph(null, "Number of Molecules", graphName.trim().substring(0, graphName.length() - 4), "tsd.printer", root, "Time",
						this, null, log, graphName.trim(), false, false);
				addTab(graphName.trim(), g, "Histogram");
				g.save();
				addToTree(graphName.trim());
			}
		}
	}

	private void createLearn() {
		if (root != null) {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			String lrnName = JOptionPane.showInputDialog(frame, "Enter Learn ID:", "Learn View ID", JOptionPane.PLAIN_MESSAGE);
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
							Runtime.getRuntime().exec("atacs -lvsl " + sbmlFileNoPath, null, work);
							sbmlFileNoPath = sbmlFileNoPath.replace(".vhd", ".lpn");
							log.addText("atacs -lvsl " + sbmlFileNoPath + "\n");
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(frame, "Unable to generate LPN from VHDL file!", "Error Generating File",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					try {
						FileOutputStream out = new FileOutputStream(new File(root + separator + lrnName.trim() + separator + lrnName.trim() + ".lrn"));
						if (lema) {
							out.write(("learn.file=" + sbmlFileNoPath + "\n").getBytes());
						}
						else {
							out.write(("genenet.file=" + sbmlFileNoPath + "\n").getBytes());
						}
						out.close();
					}
					catch (IOException e1) {
						JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
					}
					addToTree(lrnName);
					JTabbedPane lrnTab = new JTabbedPane();
					lrnTab.addMouseListener(this);
					DataManager data = new DataManager(root + separator + lrnName, this);
					lrnTab.addTab("Data Manager", data);
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
					if (lema) {
						LearnLHPN learn = new LearnLHPN(root + separator + lrnName, log, this);
						lrnTab.addTab("Learn Options", learn);
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
						lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
					}
					else {
						LearnGCM learn = new LearnGCM(root + separator + lrnName, log, this);
						lrnTab.addTab("Learn Options", learn);
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
						lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
						lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
					}
					Graph tsdGraph;
					tsdGraph = new Graph(null, "Number of molecules", lrnName + " data", "tsd.printer", root + separator + lrnName, "Time", this,
							null, log, null, true, false);
					lrnTab.addTab("TSD Graph", tsdGraph);
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
					addTab(lrnName, lrnTab, null);
				}
			}
		}
		else {
			JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewModel() {
		try {
			if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
				String[] findTheFile = filename.split("\\.");
				String theFile = findTheFile[0] + ".dot";
				File dot = new File(root + separator + theFile);
				dot.delete();
				LhpnFile lhpn = new LhpnFile(log);
				lhpn.load(tree.getFile());
				lhpn.printDot(root + separator + theFile);
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				if (dot.exists()) {
					String command = "";
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						command = "open ";
					}
					else {
						command = "dotty start ";
						//command = "dotty ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
				String[] findTheFile = filename.split("\\.");
				String theFile = findTheFile[0] + ".dot";
				File dot = new File(root + separator + theFile);
				dot.delete();
				String cmd = "atacs -cPlgodpe " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process ATACS = exec.exec(cmd, null, work);
				ATACS.waitFor();
				log.addText("Executing:\n" + cmd);
				if (dot.exists()) {
					String command = "";
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						command = "open ";
					}
					else {
						command = "dotty start ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
				try {
					String vhdFile = tree.getFile();
					if (new File(vhdFile).exists()) {
						File vhdlAmsFile = new File(vhdFile);
						BufferedReader input = new BufferedReader(new FileReader(vhdlAmsFile));
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
						scrolls.setMinimumSize(new Dimension(800, 500));
						scrolls.setPreferredSize(new Dimension(800, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame, scrolls, "VHDL Model", JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(frame, "VHDL model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to view VHDL model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams")) {
				try {
					String vamsFileName = tree.getFile();
					if (new File(vamsFileName).exists()) {
						File vamsFile = new File(vamsFileName);
						BufferedReader input = new BufferedReader(new FileReader(vamsFile));
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
						scrolls.setMinimumSize(new Dimension(800, 500));
						scrolls.setPreferredSize(new Dimension(800, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame, scrolls, "Verilog-AMS Model", JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(frame, "Verilog-AMS model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to view Verilog-AMS model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 3 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv")) {
				try {
					String svFileName = tree.getFile();
					if (new File(svFileName).exists()) {
						File svFile = new File(svFileName);
						BufferedReader input = new BufferedReader(new FileReader(svFile));
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
						scrolls.setMinimumSize(new Dimension(800, 500));
						scrolls.setPreferredSize(new Dimension(800, 500));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(frame, scrolls, "SystemVerilog Model", JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(frame, "SystemVerilog model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Unable to view SystemVerilog model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
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
						// directory = ENVVAR + "/docs/";
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						// directory = ENVVAR + "/docs/";
						command = "open ";
					}
					else {
						// directory = ENVVAR + "\\docs\\";
						command = "dotty start ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
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
						// directory = ENVVAR + "/docs/";
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						// directory = ENVVAR + "/docs/";
						command = "open ";
					}
					else {
						// directory = ENVVAR + "\\docs\\";
						command = "dotty start ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
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
						// directory = ENVVAR + "/docs/";
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						// directory = ENVVAR + "/docs/";
						command = "open ";
					}
					else {
						// directory = ENVVAR + "\\docs\\";
						command = "dotty start ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
				String filename = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
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
						// directory = ENVVAR + "/docs/";
						command = "gnome-open ";
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						// directory = ENVVAR + "/docs/";
						command = "open ";
					}
					else {
						// directory = ENVVAR + "\\docs\\";
						command = "dotty start ";
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
					JOptionPane.showMessageDialog(frame, scrolls, "Log", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "File cannot be read", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	private void copy() {
		if (!tree.getFile().equals(root)) {
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			String copy = JOptionPane.showInputDialog(frame, "Enter a New Filename:", "Copy", JOptionPane.PLAIN_MESSAGE);
			if (copy == null || copy.equals("")) {
				return;
			}
			copy = copy.trim();
			if (tree.getFile().contains(".")) {
				String extension = tree.getFile().substring(tree.getFile().lastIndexOf("."), tree.getFile().length());
				if (!copy.endsWith(extension)) {
					copy += extension;
				}
			}
			if (copy.equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				JOptionPane.showMessageDialog(frame, "Unable to copy file." + "\nNew filename must be different than old filename.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				if (checkFiles(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], copy)) {
					if (overwrite(root + separator + copy, copy)) {
						if (copy.endsWith(".xml")) {
							SBMLDocument document = SBMLutilities.readSBML(tree.getFile());
							List<URI> sbolURIs = new LinkedList<URI>();
							String sbolStrand = AnnotationUtility.parseSBOLAnnotation(document.getModel(), sbolURIs);
							Iterator<URI> sbolIterator = sbolURIs.iterator();
							while (sbolIterator != null && sbolIterator.hasNext()) {
								if (sbolIterator.next().toString().endsWith("iBioSim")) {
									sbolIterator.remove();
									sbolIterator = null;
									if (sbolURIs.size() > 0)
										AnnotationUtility.setSBOLAnnotation(document.getModel(), 
											new SBOLAnnotation(document.getModel().getMetaId(), sbolURIs, sbolStrand));
									else
										AnnotationUtility.removeSBOLAnnotation(document.getModel());
								}
							} 
							document.getModel().setId(copy.substring(0, copy.lastIndexOf(".")));
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + separator + copy);
						}
						else if (copy.endsWith(".gcm")) {
							SBMLDocument document = SBMLutilities.readSBML(tree.getFile().replace(".gcm", ".xml"));
							document.getModel().setId(copy.substring(0, copy.lastIndexOf(".")));
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + separator + copy.replace(".gcm", ".xml"));
							addToTree(copy.replace(".gcm", ".xml"));
							BioModel gcm = new BioModel(root);
							gcm.load(tree.getFile());
							gcm.setSBMLFile(copy.replace(".gcm", ".xml"));
							gcm.save(root + separator + copy);
						}
						else if (copy.contains(".")) {
							FileOutputStream out = new FileOutputStream(new File(root + separator + copy));
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
								String[] s = new File(tree.getFile()).list();
								for (String ss : s) {
									if (ss.length() > 4 && ss.substring(ss.length() - 5).equals(".sbml") || ss.length() > 3
											&& ss.substring(ss.length() - 4).equals(".xml")) {
										SBMLDocument document = SBMLutilities.readSBML(tree.getFile() + separator + ss);
										SBMLWriter writer = new SBMLWriter();
										writer.writeSBMLToFile(document, root + separator + copy + separator + ss);
									}
									else if (ss.length() > 10 && ss.substring(ss.length() - 11).equals(".properties")) {
										FileOutputStream out = new FileOutputStream(new File(root + separator + copy + separator + ss));
										FileInputStream in = new FileInputStream(new File(tree.getFile() + separator + ss));
										int read = in.read();
										while (read != -1) {
											out.write(read);
											read = in.read();
										}
										in.close();
										out.close();
									}
									else if (ss.length() > 3
											&& (ss.substring(ss.length() - 4).equals(".tsd") || ss.substring(ss.length() - 4).equals(".dat")
													|| ss.substring(ss.length() - 4).equals(".sad") || ss.substring(ss.length() - 4).equals(".pms") || ss
													.substring(ss.length() - 4).equals(".sim")) && !ss.equals(".sim")) {
										FileOutputStream out;
										if (ss.substring(ss.length() - 4).equals(".pms")) {
											out = new FileOutputStream(new File(root + separator + copy + separator + copy + ".sim"));
										}
										else if (ss.substring(ss.length() - 4).equals(".sim")) {
											out = new FileOutputStream(new File(root + separator + copy + separator + copy + ".sim"));
										}
										else {
											out = new FileOutputStream(new File(root + separator + copy + separator + ss));
										}
										FileInputStream in = new FileInputStream(new File(tree.getFile() + separator + ss));
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
											&& (ss.substring(ss.length() - 4).equals(".tsd") || ss.substring(ss.length() - 4).equals(".lrn"))) {
										FileOutputStream out;
										if (ss.substring(ss.length() - 4).equals(".lrn")) {
											out = new FileOutputStream(new File(root + separator + copy + separator + copy + ".lrn"));
										}
										else {
											out = new FileOutputStream(new File(root + separator + copy + separator + ss));
										}
										FileInputStream in = new FileInputStream(new File(tree.getFile() + separator + ss));
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
						addToTree(copy);
					}
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, "Unable to copy file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void rename() {
		if (!tree.getFile().equals(root)) {
			
			if (!new File(tree.getFile()).isDirectory()) {
				String[] views = canDelete(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]);
				if (views.length != 0) {
					String view = "";
					String gcms = "";
					for (int i = 0; i < views.length; i++) {
						if (views[i].endsWith(".xml")) {
							gcms += views[i] + "\n";
						}
						else {
							view += views[i] + "\n";
						}
					}
					String message;
					if (gcms.equals("")) {
						message = "Unable to rename the selected file." + "\nIt is linked to the following views:\n" + view
								+ "\nDelete these views first.";
					}
					else if (view.equals("")) {
						message = "Unable to rename the selected file." + "\nIt is linked to the following models:\n" + gcms
								+ "\nDelete these models first.";
					}
					else {
						message = "Unable to rename the selected file." + "\nIt is linked to the following views:\n" + view
								+ "\nIt is also linked to the following models:\n" + gcms + "\nDelete these views and models first.";
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(frame, scroll, "Unable To Rename File", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			String oldName = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			String rename = JOptionPane.showInputDialog(frame, "Enter a New Filename:", "Rename", JOptionPane.PLAIN_MESSAGE);
			if (rename == null || rename.equals("")) {
				return;
			}
			rename = rename.trim();
			if (tree.getFile().contains(".")) {
				String extension = tree.getFile().substring(tree.getFile().lastIndexOf("."), tree.getFile().length());
				if (!rename.endsWith(extension)) {
					rename += extension;
				}
			}
			if (rename.equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				JOptionPane.showMessageDialog(frame, "Unable to rename file." + "\nNew filename must be different than old filename.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int index = rename.lastIndexOf(".");
			String modelID = rename;
			if (index != -1) {
				modelID = rename.substring(0, rename.lastIndexOf("."));
			}
			try {
				if (checkFiles(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], rename)) {
					if (overwrite(root + separator + rename, rename)) {
						if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml") || tree.getFile().endsWith(".gcm")
								|| tree.getFile().endsWith(".lpn") || tree.getFile().endsWith(".vhd") || tree.getFile().endsWith(".csp")
								|| tree.getFile().endsWith(".hse") || tree.getFile().endsWith(".unc") || tree.getFile().endsWith(".rsg") || tree.getFile().endsWith(".prop")) {
							reassignViews(oldName, rename);
						}
						if (tree.getFile().endsWith(".gcm")) {
							String newSBMLfile = rename.replace(".gcm", ".xml");
							new File(tree.getFile()).renameTo(new File(root + separator + rename));
							new File(tree.getFile().replace(".gcm", ".xml")).renameTo(new File(root + separator + newSBMLfile));
							BioModel gcm = new BioModel(root);
							gcm.load(root + separator + rename);
							gcm.setSBMLFile(newSBMLfile);
							gcm.save(root + separator + rename);
							SBMLDocument document = SBMLutilities.readSBML(root + separator + newSBMLfile);
							document.getModel().setId(modelID);
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + separator + newSBMLfile);
							deleteFromTree(oldName.replace(".gcm", ".xml"));
							addToTree(newSBMLfile);
						}
						else if (tree.getFile().endsWith(".xml")) {
							new File(tree.getFile()).renameTo(new File(root + separator + rename));
							SBMLDocument document = SBMLutilities.readSBML(root + separator + rename);
							document.getModel().setId(modelID);
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + separator + rename);
						}
						else {
							new File(tree.getFile()).renameTo(new File(root + separator + rename));
						}
						if (rename.length() >= 4 && rename.substring(rename.length() - 4).equals(".gcm")) {
							for (String s : new File(root).list()) {
								if (s.endsWith(".gcm")) {
									boolean update = false;
									BufferedReader in = new BufferedReader(new FileReader(root + separator + s));
									String line = null;
									String file = "";
									while ((line = in.readLine()) != null) {
										if (line.contains("gcm=" + oldName)) {
											line = line.replaceAll("gcm=" + oldName, "gcm=" + rename);
											update = true;
										}
										file += line;
										file += "\n";
									}
									in.close();
									BufferedWriter out = new BufferedWriter(new FileWriter(root + separator + s));
									out.write(file);
									out.close();
									if (update) {
										renameOpenGCMComponents(s, oldName, rename);
									}
								}
							}
						}
						if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml") || tree.getFile().endsWith(".gcm")
								|| tree.getFile().endsWith(".lpn") || tree.getFile().endsWith(".vhd") || tree.getFile().endsWith(".csp")
								|| tree.getFile().endsWith(".hse") || tree.getFile().endsWith(".unc") || tree.getFile().endsWith(".rsg")|| tree.getFile().endsWith(".prop")) {
							updateAsyncViews(rename);
						}
						if (new File(root + separator + rename).isDirectory()) {
							String subFilePath = root + separator + rename + separator 
									+ tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
							String renamedSubFilePath = root + separator + rename + separator + rename;
							if (new File(subFilePath + ".sim").exists()) {
								new File(subFilePath + ".sim").renameTo(
										new File(renamedSubFilePath + ".sim"));
							} else if (new File(subFilePath + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).exists()) {
								new File(subFilePath + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).renameTo(
										new File(renamedSubFilePath + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION));
							}
							else if (new File(subFilePath + ".pms").exists()) {
								new File(subFilePath + ".pms").renameTo(
										new File(renamedSubFilePath + ".sim"));
							}
							if (new File(subFilePath + ".lrn").exists()) {
								new File(subFilePath + ".lrn").renameTo(
										new File(renamedSubFilePath + ".lrn"));
							}
							if (new File(subFilePath + ".ver").exists()) {
								new File(subFilePath + ".ver").renameTo(
										new File(renamedSubFilePath + ".ver"));
							}
							if (new File(subFilePath + ".grf").exists()) {
								new File(subFilePath + ".grf").renameTo(
										new File(renamedSubFilePath + ".grf"));
							}
							if (new File(subFilePath + ".prb").exists()) {
								new File(subFilePath + ".prb").renameTo(
										new File(renamedSubFilePath + ".prb"));
							}
							if (new File(subFilePath + ".prop").exists()) {
								new File(subFilePath + ".prop").renameTo(
										new File(renamedSubFilePath + ".prop"));
							}
						}
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
								/*	
								if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml")
										|| tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
									((SBML_Editor) tab.getComponentAt(i)).setModelID(modelID);
									((SBML_Editor) tab.getComponentAt(i)).setFile(root + separator + rename);
									tab.setTitleAt(i, rename);
								}
								else */ if (tree.getFile().length() > 3
										&& (tree.getFile().substring(tree.getFile().length() - 4).equals(".grf") || tree.getFile()
												.substring(tree.getFile().length() - 4).equals(".prb"))) {
									((Graph) tab.getComponentAt(i)).setGraphName(rename);
									tab.setTitleAt(i, rename);
								}
								else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
									((ModelEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
									tab.setTitleAt(i, rename);
								}
								else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
									((ModelEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
									tab.setTitleAt(i, rename);
								}
								else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol")) {
									tab.setTitleAt(i, rename);
								}
								else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn")) {
									((LHPNEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
									tab.setTitleAt(i, rename);
								}
								else if (tab.getComponentAt(i) instanceof JTabbedPane) {
									if (tab.getComponentAt(i) instanceof SynthesisView) {
										((SynthesisView) tab.getComponentAt(i)).renameView(rename);
									} else {
										JTabbedPane t = new JTabbedPane();
										t.addMouseListener(this);
										int selected = ((JTabbedPane) tab.getComponentAt(i)).getSelectedIndex();
										boolean analysis = false;
										ArrayList<Component> comps = new ArrayList<Component>();
										for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getTabCount(); j++) {
											Component c = ((JTabbedPane) tab.getComponentAt(i)).getComponent(j);
											comps.add(c);
										}
										for (Component c : comps) {
											if (analysis) {
												if (c instanceof MovieContainer) {
													t.addTab("Schematic", c);
													t.getComponentAt(t.getComponents().length - 1).setName("ModelViewMovie");
												}
												else if (c instanceof ModelEditor) {
													((ModelEditor)c).setParamFile(root + separator + rename + separator + rename + ".sim");
													t.addTab("Parameters", c);
													t.getComponentAt(t.getComponents().length - 1).setName("GCM Editor");
												}
												else if (c instanceof Graph) {
													((Graph)c).setDirectory(root + separator + rename);
													if (((Graph) c).isTSDGraph()) {
														t.addTab("TSD Graph", c);
														t.getComponentAt(t.getComponents().length - 1).setName("TSD Graph");
													}
													else {
														t.addTab("Histogram", c);
														t.getComponentAt(t.getComponents().length - 1).setName("ProbGraph");
													}
												}
												else if (c instanceof JScrollPane) {
													//JScrollPane scroll = new JScrollPane();
													//scroll.setViewportView(new JPanel());
													//t.addTab("SBML Elements", scroll);
													//t.getComponentAt(t.getComponents().length - 1).setName("");
												}
												else {
													t.addTab("Advanced Options", c);
													t.getComponentAt(t.getComponents().length - 1).setName("");
												}
											} else {
												if (c instanceof AnalysisView) {
													((AnalysisView) c).setSim(rename);
													t.addTab("Simulation Options", c);
													t.getComponentAt(t.getComponents().length - 1).setName("Simulate");
													analysis = true;
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
												else if (c instanceof LearnGCM) {
													LearnGCM l = ((LearnGCM) c);
													l.setDirectory(root + separator + rename);
												}
												else if (c instanceof DataManager) {
													DataManager d = ((DataManager) c);
													d.setDirectory(root + separator + rename);
												}
											}
										}
										if (analysis) {
											t.setSelectedIndex(selected);
											tab.setComponentAt(i, t);
										}
									}
									tab.setTitleAt(i, rename);
									tab.getComponentAt(i).setName(rename);
								}
								else {
									tab.setTitleAt(i, rename);
									tab.getComponentAt(i).setName(rename);
								}
							}
							else if (tab.getComponentAt(i) instanceof JTabbedPane) {
								if (tab.getComponentAt(i) instanceof SynthesisView) {
									((SynthesisView) tab.getComponentAt(i)).changeSpecFile(rename);
								} else {
									ArrayList<Component> comps = new ArrayList<Component>();
									for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getTabCount(); j++) {
										Component c = ((JTabbedPane) tab.getComponentAt(i)).getComponent(j);
										comps.add(c);
									}
									for (Component c : comps) {
										if (c instanceof AnalysisView && ((AnalysisView) c).getBackgroundFile().equals(oldName)) {
											((AnalysisView) c).updateBackgroundFile(rename);
										}
										else if (c instanceof LearnGCM && ((LearnGCM) c).getBackgroundFile().equals(oldName)) {
											((LearnGCM) c).updateBackgroundFile(rename);
										}
									}
								}
							}
						}
						// updateAsyncViews(rename);
						updateViewNames(tree.getFile(), rename);
						deleteFromTree(oldName);
						addToTree(rename);
					}
				}
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Unable to rename selected file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (SBMLException e) {
				e.printStackTrace();
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}

	private void openGCM(boolean textBased) {
		String file = tree.getFile();
		String filename = "";
		if (file.lastIndexOf('/') >= 0) {
			filename = file.substring(file.lastIndexOf('/') + 1);
		}
		if (file.lastIndexOf('\\') >= 0) {
			filename = file.substring(file.lastIndexOf('\\') + 1);
		}
		openGCM(filename, textBased);
	}

	public void openGCM(String filename, boolean textBased) {
		try {
			File work = new File(root);
			int i = getTab(filename);
			if (i == -1) {
				i = getTab(filename.replace(".gcm", ".xml"));
			}
			if (i != -1) {
				if (((ModelEditor)tab.getComponentAt(i)).isTextBased()) {
					if (!textBased) {
						((ModelEditor)tab.getComponentAt(i)).setTextBased(textBased);
						((ModelEditor)tab.getComponentAt(i)).rebuildGui();
					} 
				} else {
					if (textBased) {
						((ModelEditor)tab.getComponentAt(i)).setTextBased(textBased);
						((ModelEditor)tab.getComponentAt(i)).rebuildGui();
					} 
				}
				tab.setSelectedIndex(i);
			}
			else {
				String path = work.getAbsolutePath();
				/*
				 * GCMFile gcmFile = new GCMFile(path); String gcmname =
				 * theFile.replace(".gcm", ""); gcmFile.load(filename); if
				 * ((gcmFile.getSBMLFile() == null ||
				 * !gcmFile.getSBMLFile().equals(gcmname + ".xml")) && new
				 * File(path + separator + gcmname + ".xml").exists()) {
				 * Object[] options = { "Overwrite", "Cancel" }; int value;
				 * value = JOptionPane.showOptionDialog(Gui.frame, gcmname +
				 * ".xml already exists." + "\nDo you want to overwrite?",
				 * "Overwrite", JOptionPane.YES_NO_OPTION,
				 * JOptionPane.PLAIN_MESSAGE, null, options, options[0]); if
				 * (value == JOptionPane.YES_OPTION) { GCM2SBMLEditor gcm = new
				 * GCM2SBMLEditor(path, theFile, this, log, false, null, null,
				 * null, textBased); addTab(theFile, gcm, "GCM Editor"); } }
				 * else {
				 */
				try {
					ModelEditor gcm = new ModelEditor(path, filename, this, log, false, null, null, null, textBased, false);
					addTab(filename, gcm, "GCM Editor");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unable to open this GCM file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void openSBML(String fullPath) {
		try {
			boolean done = false;
			String theSBMLFile = fullPath.split(separator)[fullPath.split(separator).length - 1];
			String theGCMFile = theSBMLFile.replace(".xml", ".gcm");
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(theSBMLFile) || getTitleAt(i).equals(theGCMFile)) {
					tab.setSelectedIndex(i);
					done = true;
					break;
				}
			}
			if (!done) {
				//createGCMFromSBML(root, fullPath, theSBMLFile, theGCMFile, false);
				//addToTree(theGCMFile);
				ModelEditor gcm = new ModelEditor(root + separator, theGCMFile, this, log, false, null, null, null, false, false);
				addTab(theSBMLFile, gcm, "GCM Editor");
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "You must select a valid SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	public static boolean createGCMFromSBML(String root, String fullPath, String theSBMLFile, String theGCMFile, boolean force) {
		if (force || !new File(fullPath.replace(".xml", ".gcm").replace(".sbml", ".gcm")).exists()) {
			SBMLDocument document = readSBML(fullPath);
			Model m = document.getModel();
			GCMFile gcmFile = new GCMFile(root);
			gcmFile.setSBMLDocument(document);
			gcmFile.setSBMLFile(theSBMLFile);
			document.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			document.setPackageRequired("layout", false); 
			document.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			document.setPackageRequired("comp", true); 
			gcmFile.setSBMLLayout((LayoutModelPlugin)document.getModel().getPlugin("layout"));
			gcmFile.setSBMLComp((CompSBMLDocumentPlugin)document.getExtension(CompConstants.namespaceURI));
			gcmFile.setSBMLCompModel((CompModelPlugin)document.getModel().getExtension(CompConstants.namespaceURI));
			int x = 50;
			int y = 50;
			if (m != null) {
				for (int i = 0; i < m.getSpeciesCount(); i++) {
					gcmFile.createSpecies(document.getModel().getSpecies(i).getId(), x, y);
					x += 50;
					y += 50;
				}
			}
			gcmFile.save(fullPath.replace(".xml", ".gcm").replace(".sbml", ".gcm"));
			return true;
		}
		else
			return false;
	}
	*/

	private void openSBOL() {
		String filePath = tree.getFile();
		String fileName = "";
		fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		int i = getTab(fileName);
		if (i != -1) {
			tab.setSelectedIndex(i);
		}
		else {
			//SBOLBrowser browser = new SBOLBrowser(this, filePath);
			SBOLBrowser sbolBrowser = new SBOLBrowser(this, filePath);
		}
	}
	
	public HashSet<String> getFilePaths(String fileExtension) {
		HashSet<String> filePaths = new HashSet<String>();
		TreeModel tree = getFileTree().tree.getModel();
		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++) {
			String fileName = tree.getChild(tree.getRoot(), i).toString();
			if (fileName.endsWith(fileExtension))
				filePaths.add(getRoot() + File.separator + fileName);
		}
		return filePaths;
	}
	
//	public HashSet<String> getSBOLFilePaths() {
//		HashSet<String> sbolFilePaths = new HashSet<String>();
//		TreeModel tree = getFileTree().tree.getModel();
//		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++) {
//			String fileName = tree.getChild(tree.getRoot(), i).toString();
//			if (fileName.endsWith(".sbol"))
//				sbolFilePaths.add(root + separator + fileName);
//		}
//		return sbolFilePaths;
//	}

	private void openGraph() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], new Graph(null, "Number of molecules", "title",
					"tsd.printer", root, "Time", this, tree.getFile(), log,
					tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], true, false), "TSD Graph");
		}
	}

	private void openHistogram() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], new Graph(null, "Percent", "title", "tsd.printer",
					root, "Time", this, tree.getFile(), log, tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], false,
					false), "Histogram");
		}
	}

	private void openLPN() {
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
			LhpnFile lhpn = new LhpnFile(log);
			File work = new File(directory);
			int i = getTab(theFile);
			if (i != -1) {
				tab.setSelectedIndex(i);
			}
			else {
				LHPNEditor editor = new LHPNEditor(work.getAbsolutePath(), theFile, lhpn, this);
				addTab(theFile, editor, "LHPN Editor");
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unable to view this LPN file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void newModel(String fileType, String extension) {
		if (root != null) {
			try {
				String modelID = JOptionPane.showInputDialog(frame, "Enter " + fileType + " Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
				if (modelID != null && !modelID.trim().equals("")) {
					String fileName;
					modelID = modelID.trim();
					if (modelID.length() >= extension.length()) {
						if (!modelID.substring(modelID.length() - extension.length()).equals(extension)) {
							fileName = modelID + extension;
						}
						else {
							fileName = modelID;
							modelID = modelID.substring(0, modelID.length() - extension.length());
						}
					}
					else {
						fileName = modelID + extension;
					}
					if (!(IDpat.matcher(modelID).matches())) {
						JOptionPane.showMessageDialog(frame, "A model ID can only contain letters, numbers, and underscores.", "Invalid ID",
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						File f = new File(root + separator + fileName);
						f.createNewFile();
						addToTree(fileName);
						if (!viewer.equals("")) {
							String command = viewer + " " + root + separator + fileName;
							Runtime exec = Runtime.getRuntime();
							try {
								exec.exec(command);
							}
							catch (Exception e1) {
								JOptionPane.showMessageDialog(frame, "Unable to open external editor.", "Error Opening Editor",
										JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							JTextArea text = new JTextArea("");
							text.setEditable(true);
							text.setLineWrap(true);
							JScrollPane scroll = new JScrollPane(text);
							addTab(fileName, scroll, fileType + " Editor");
						}
					}
				}
			}
			catch (IOException e1) {
				JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void openModel(String fileType) {
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
				if (!viewer.equals("")) {
					String command = viewer + " " + directory + separator + theFile;
					Runtime exec = Runtime.getRuntime();
					try {
						exec.exec(command);
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Unable to open external editor.", "Error Opening Editor", JOptionPane.ERROR_MESSAGE);
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
					addTab(theFile, scroll, fileType + " Editor");
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Unable to view this " + fileType + " file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public int getTab(String name) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(name)) {
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
			JOptionPane.showMessageDialog(frame, "Unable to delete.", "Error", JOptionPane.ERROR_MESSAGE);
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
					recentProjects[j].setText(projDir.split(separator)[projDir.split(separator).length - 1]);
					openRecent.insert(recentProjects[j], j);
					/*
					if (openRecent.getItem(openRecent.getItemCount() - 1) == exit) {
						openRecent.insert(recentProjects[j], openRecent.getItemCount() - 3 - numberRecentProj);
					}
					else {
						openRecent.add(recentProjects[j]);
					}
					*/
					recentProjectPaths[j] = projDir;
					projDir = save;
				}
				for (int j = i + 1; j < numberRecentProj; j++) {
					openRecent.insert(recentProjects[j], j);
					/*
					if (openRecent.getItem(openRecent.getItemCount() - 1) == exit) {
						openRecent.insert(recentProjects[j], openRecent.getItemCount() - 3 - numberRecentProj);
					}
					else {
						openRecent.add(recentProjects[j]);
					}
					*/
				}
				return;
			}
		}
		if (numberRecentProj < 10) {
			numberRecentProj++;
		}
		for (int i = 0; i < numberRecentProj; i++) {
			String save = recentProjectPaths[i];
			recentProjects[i].setText(projDir.split(separator)[projDir.split(separator).length - 1]);
			openRecent.insert(recentProjects[i], i);
			/*
			if (openRecent.getItem(openRecent.getItemCount() - 1) == exit) {
				openRecent.insert(recentProjects[i], openRecent.getItemCount() - 3 - numberRecentProj);
			}
			else {
				openRecent.add(recentProjects[i]);
			}
			*/
			recentProjectPaths[i] = projDir;
			projDir = save;
		}
	}

	/**
	 * This method removes a project from the recent list
	 */
	public void removeRecentProject(String projDir) {
		for (int i = 0; i < numberRecentProj; i++) {
			if (recentProjectPaths[i].equals(projDir)) {
				for (int j = i; j < numberRecentProj-1; j++) {
					recentProjects[j].setText(recentProjects[j+1].getText());
					recentProjectPaths[j] = recentProjectPaths[j+1];
				}
				openRecent.remove(recentProjects[numberRecentProj-1]);
				recentProjectPaths[numberRecentProj-1]="";
				numberRecentProj--;
				return;
			}
		}
	}

	/**
	 * This method removes all projects from the recent list
	 */
	public void removeAllRecentProjects() {
		for (int i = 0; i < numberRecentProj; i++) {
			openRecent.remove(recentProjects[i]);
			recentProjectPaths[i]="";
		}
		numberRecentProj=0;
	}
	
	/**
	 * This method refreshes the menu.
	 */
	public void refresh() {
		mainPanel.remove(tree);
		tree = new FileTree(new File(root), this, lema, atacs);
		topSplit.setLeftComponent(tree);
		//mainPanel.add(tree, "West");
		mainPanel.validate();
	}

	/**
	 * This method refreshes the tree.
	 */
	public void refreshTree() {
		mainPanel.remove(tree);
		tree = new FileTree(new File(root), this, lema, atacs);
		topSplit.setLeftComponent(tree);
		//mainPanel.add(tree, "West");
		// updateGCM();
		mainPanel.validate();
	}

	public void addToTree(String item) {
		tree.addToTree(item, root);
		// updateGCM();
		mainPanel.validate();
	}

	public void addToTreeNoUpdate(String item) {
		tree.addToTree(item, root);
		mainPanel.validate();
	}

	public void deleteFromTree(String item) {
		tree.deleteFromTree(item);
		// updateGCM();
		mainPanel.validate();
	}

	public FileTree getFileTree() {
		return tree;
	}
	
	public void markTabDirty(boolean dirty) {
		int i = tab.getSelectedIndex();
		if (dirty) {
			if (i>=0 && !tab.getTitleAt(i).endsWith("*")) {
				tab.setTitleAt(i, tab.getTitleAt(i)+"*");
			}
		} else {
			if (i>=0 && tab.getTitleAt(i).endsWith("*")) {
				tab.setTitleAt(i, tab.getTitleAt(i).replace("*",""));
			}
		}
	}
	
	public void markTabClean(int i) {
		if (i>=0 && tab.getTitleAt(i).endsWith("*")) {
			tab.setTitleAt(i, tab.getTitleAt(i).replace("*",""));
		}
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
		if (tab.getTabCount() > 0) {
			tab.setSelectedIndex(tab.getTabCount() - 1);
			enableTabMenu(tab.getSelectedIndex());
		}
		else {
			enableTreeMenu();
		}
	}
	
	public void refreshTabListeners() {
		for (ChangeListener l : tab.getChangeListeners()) {
			tab.removeChangeListener(l);
		}
		for (int i = 0; i < tab.getTabCount(); i ++) {
			if (tab.getComponent(i) instanceof ModelEditor) {
				((ModelEditor)tab.getComponent(i)).getSchematic().addChangeListener();
			}
		}
	}

	public JTabbedPane getTab() {
		return tab;
	}

	/**
	 * Prompts the user to save work that has been done.
	 */
	public int save(int index, int autosave) {
		if (tab.getComponentAt(index).getName().contains(("GCM")) || tab.getComponentAt(index).getName().contains("LHPN")) {
			if (tab.getComponentAt(index) instanceof ModelEditor) {
				ModelEditor editor = (ModelEditor) tab.getComponentAt(index);
				if (editor.isDirty()) {
					if (autosave == 0) {
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?",
								"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION) {
							editor.save("gcm");
							return 1;
						}
						else if (value == NO_OPTION) {
							return 1;
						}
						else if (value == CANCEL_OPTION) {
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION) {
							editor.save("gcm");
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION) {
							return 3;
						}
					}
					else if (autosave == 1) {
						editor.save("gcm");
						return 2;
					}
					else {
						return 3;
					}
				}
			}
			else if (tab.getComponentAt(index) instanceof LHPNEditor) {
				LHPNEditor editor = (LHPNEditor) tab.getComponentAt(index);
				if (editor.isDirty()) {
					if (autosave == 0) {
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?",
								"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION) {
							editor.save();
							return 1;
						}
						else if (value == NO_OPTION) {
							return 1;
						}
						else if (value == CANCEL_OPTION) {
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION) {
							editor.save();
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION) {
							return 3;
						}
					}
					else if (autosave == 1) {
						editor.save();
						return 2;
					}
					else {
						return 3;
					}
				}
			}
			if (autosave == 0) {
				return 1;
			}
			else if (autosave == 1) {
				return 2;
			}
			else {
				return 3;
			}
		}
		/*
		else if (tab.getComponentAt(index).getName().equals("SBML Editor")) {
			if (tab.getComponentAt(index) instanceof SBML_Editor) {
				if (((SBML_Editor) tab.getComponentAt(index)).isDirty()) {
					if (autosave == 0) {
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + tab.getTitleAt(index) + "?",
								"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION) {
							((SBML_Editor) tab.getComponentAt(index)).save(false, "", true, true);
							return 1;
						}
						else if (value == NO_OPTION) {
							return 1;
						}
						else if (value == CANCEL_OPTION) {
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION) {
							((SBML_Editor) tab.getComponentAt(index)).save(false, "", true, true);
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION) {
							return 3;
						}
					}
					else if (autosave == 1) {
						((SBML_Editor) tab.getComponentAt(index)).save(false, "", true, true);
						return 2;
					}
					else {
						return 3;
					}
				}
			}
			if (autosave == 0) {
				return 1;
			}
			else if (autosave == 1) {
				return 2;
			}
			else {
				return 3;
			}
		}
		*/
		else if (tab.getComponentAt(index).getName().contains("Graph") || tab.getComponentAt(index).getName().equals("Histogram")) {
			if (tab.getComponentAt(index) instanceof Graph) {
				if (((Graph) tab.getComponentAt(index)).hasChanged()) {
					if (autosave == 0) {
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?",
								"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION) {
							((Graph) tab.getComponentAt(index)).save();
							return 1;
						}
						else if (value == NO_OPTION) {
							return 1;
						}
						else if (value == CANCEL_OPTION) {
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION) {
							((Graph) tab.getComponentAt(index)).save();
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION) {
							return 3;
						}
					}
					else if (autosave == 1) {
						((Graph) tab.getComponentAt(index)).save();
						return 2;
					}
					else {
						return 3;
					}
				}
			}
			if (autosave == 0) {
				return 1;
			}
			else if (autosave == 1) {
				return 2;
			}
			else {
				return 3;
			}
		}
		
		
		else {
			if (tab.getComponentAt(index) instanceof JTabbedPane) {
				if (tab.getComponentAt(index) instanceof SynthesisView) {
					SynthesisView synthView = (SynthesisView) tab.getComponentAt(index);
					Set<Integer> saveIndices = new HashSet<Integer>();
					for (int i = 0; i < synthView.getTabCount(); i++) {
						JPanel synthTab = (JPanel) synthView.getComponentAt(i);
						if (synthView.tabChanged(i)) {
							if (autosave == 0) {
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save changes to " + synthTab.getName() + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION) 
									saveIndices.add(i);
								else if (value == CANCEL_OPTION)
									return 0;
								else if (value == YES_TO_ALL_OPTION) {
									saveIndices.add(i);
									autosave = 1;
								} else if (value == NO_TO_ALL_OPTION)
									autosave = 2;
							}
							else if (autosave == 1)
								saveIndices.add(i);
						}
					}
					synthView.saveTabs(saveIndices);
				} else {
					for (int i = 0; i < ((JTabbedPane) tab.getComponentAt(index)).getTabCount(); i++) {
						if (((JTabbedPane) tab.getComponentAt(index)).getComponentAt(i).getName() != null) {
							if (((JTabbedPane) tab.getComponentAt(index)).getComponentAt(i).getName().equals("Simulate")) {
								if (((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged()) {
									if (autosave == 0) {
										int value = JOptionPane.showOptionDialog(frame,
												"Do you want to save simulation option changes for " + getTitleAt(index) + "?", "Save Changes",
												JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
										if (value == YES_OPTION) {
											((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
										}
										else if (value == CANCEL_OPTION) {
											return 0;
										}
										else if (value == YES_TO_ALL_OPTION) {
											((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
											autosave = 1;
										}
										else if (value == NO_TO_ALL_OPTION) {
											autosave = 2;
										}
									}
									else if (autosave == 1) {
										((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
									}
								}
							}
							/*
						else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals("SBML Editor")) {
							if (((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).isDirty()) {
								if (autosave == 0) {
									int value = JOptionPane.showOptionDialog(frame,
											"Do you want to save parameter changes for " + tab.getTitleAt(index) + "?", "Save Changes",
											JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
									if (value == YES_OPTION) {
										((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save(false, "", true, true);
									}
									else if (value == CANCEL_OPTION) {
										return 0;
									}
									else if (value == YES_TO_ALL_OPTION) {
										((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save(false, "", true, true);
										autosave = 1;
									}
									else if (value == NO_TO_ALL_OPTION) {
										autosave = 2;
									}
								}
								else if (autosave == 1) {
									((SBML_Editor) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save(false, "", true, true);
								}
							}
						}
							 */
							/*
							 * else if (((JTabbedPane)
							 * tab.getComponentAt(index)).getComponent(i)
							 * .getName().equals("GCM Editor")) { if
							 * (((GCM2SBMLEditor) ((JTabbedPane)
							 * tab.getComponentAt(index))
							 * .getComponent(i)).isDirty()) { if (autosave == 0) {
							 * int value = JOptionPane.showOptionDialog(frame,
							 * "Do you want to save parameter changes for " +
							 * tab.getTitleAt(index) + "?", "Save Changes",
							 * JOptionPane.YES_NO_CANCEL_OPTION,
							 * JOptionPane.PLAIN_MESSAGE, null, OPTIONS,
							 * OPTIONS[0]); if (value == YES_OPTION) {
							 * ((GCM2SBMLEditor) ((JTabbedPane)
							 * tab.getComponentAt(index))
							 * .getComponent(i)).saveParams(false, ""); } else if
							 * (value == CANCEL_OPTION) { return 0; } else if (value
							 * == YES_TO_ALL_OPTION) { ((GCM2SBMLEditor)
							 * ((JTabbedPane) tab.getComponentAt(index))
							 * .getComponent(i)).saveParams(false, ""); autosave =
							 * 1; } else if (value == NO_TO_ALL_OPTION) { autosave =
							 * 2; } } else if (autosave == 1) { ((GCM2SBMLEditor)
							 * ((JTabbedPane) tab.getComponentAt(index))
							 * .getComponent(i)).saveParams(false, ""); } } }
							 */
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof MovieContainer) {
								if (((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).getGCM2SBMLEditor().isDirty()
										|| ((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).getIsDirty()) {
									if (autosave == 0) {
										int value = JOptionPane.showOptionDialog(frame,
												"Do you want to save parameter changes for " + getTitleAt(index) + "?", "Save Changes",
												JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
										if (value == YES_OPTION) {
											((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
										}
										else if (value == CANCEL_OPTION) {
											return 0;
										}
										else if (value == YES_TO_ALL_OPTION) {
											((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
											autosave = 1;
										}
										else if (value == NO_TO_ALL_OPTION) {
											autosave = 2;
										}
									}
									else if (autosave == 1) {
										((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
									}
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals("Learn Options")) {
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM) {
									if (((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged()) {
										if (autosave == 0) {
											int value = JOptionPane.showOptionDialog(frame,
													"Do you want to save learn option changes for " + getTitleAt(index) + "?", "Save Changes",
													JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM) {
													((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
											}
											else if (value == CANCEL_OPTION) {
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM) {
													((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION) {
												autosave = 2;
											}
										}
										else if (autosave == 1) {
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM) {
												((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
											}
										}
									}
								}
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
									if (((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged()) {
										if (autosave == 0) {
											int value = JOptionPane.showOptionDialog(frame,
													"Do you want to save learn option changes for " + getTitleAt(index) + "?", "Save Changes",
													JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
													((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
											}
											else if (value == CANCEL_OPTION) {
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
													((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION) {
												autosave = 2;
											}
										}
										else if (autosave == 1) {
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLHPN) {
												((LearnLHPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
											}
										}
									}
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals("Data Manager")) {
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof DataManager) {
									((DataManager) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).saveChanges(getTitleAt(index));
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().contains("Graph")) {
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
									if (((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged()) {
										if (autosave == 0) {
											int value = JOptionPane.showOptionDialog(frame,
													"Do you want to save graph changes for " + getTitleAt(index) + "?", "Save Changes",
													JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
													Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i));
													g.save();
												}
											}
											else if (value == CANCEL_OPTION) {
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION) {
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
													Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i));
													g.save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION) {
												autosave = 2;
											}
										}
										else if (autosave == 1) {
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph) {
												Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i));
												g.save();
											}
										}
									}
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
							if (autosave == 0) {
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save synthesis option changes for " + getTitleAt(index) + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION) {
									if (array[0] instanceof Synthesis) {
										((Synthesis) array[0]).save();
									}
								}
								else if (value == CANCEL_OPTION) {
									return 0;
								}
								else if (value == YES_TO_ALL_OPTION) {
									if (array[0] instanceof Synthesis) {
										((Synthesis) array[0]).save();
									}
									autosave = 1;
								}
								else if (value == NO_TO_ALL_OPTION) {
									autosave = 2;
								}
							}
							else if (autosave == 1) {
								if (array[0] instanceof Synthesis) {
									((Synthesis) array[0]).save();
								}
							}
						}
					}
				}
				else if (tab.getComponentAt(index).getName().equals("Verification")) {
					Component[] array = ((JPanel) tab.getComponentAt(index)).getComponents();
					if (array[0] instanceof Verification) {
						if (((Verification) array[0]).hasChanged()) {
							if (autosave == 0) {
								int value = JOptionPane.showOptionDialog(frame,
										"Do you want to save verification option changes for " + getTitleAt(index) + "?", "Save Changes",
										JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION) {
									((Verification) array[0]).save();
								}
								else if (value == CANCEL_OPTION) {
									return 0;
								}
								else if (value == YES_TO_ALL_OPTION) {
									((Verification) array[0]).save();
									autosave = 1;
								}
								else if (value == NO_TO_ALL_OPTION) {
									autosave = 2;
								}
							}
							else if (autosave == 1) {
								((Verification) array[0]).save();
							}
						}
					}
				}
			}
			if (autosave == 0) {
				return 1;
			}
			else if (autosave == 1) {
				return 2;
			}
			else {
				return 3;
			}
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
				
				BioModel gcm = new BioModel(root);
				gcm.load(root + separator + filename);
				GCM2SBML gcm2sbml = new GCM2SBML(gcm);
				gcm2sbml.load(root + separator + filename);
				gcm2sbml.convertGCM2SBML(root + separator + filename);
				String sbmlFile = filename.replace(".gcm", ".xml");
				gcm.save(root + separator + sbmlFile);
				addToTree(sbmlFile);
			}
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save genetic circuit.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves a circuit from a learn view to the project view
	 */
	public void saveLhpn(String filename, String path) {
		try {
			if (overwrite(root + separator + filename, filename)) {
				BufferedWriter out = new BufferedWriter(new FileWriter(root + separator + filename));
				BufferedReader in = new BufferedReader(new FileReader(path));
				String str;
				while ((str = in.readLine()) != null) {
					out.write(str + "\n");
				}
				in.close();
				out.close();
				addToTree(filename);
			}
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save LPN.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void copySFiles(String filename, String directory) {
		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(directory + separator + filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		Pattern sLinePattern = Pattern.compile(SFILELINE);
		Matcher sLineMatcher = sLinePattern.matcher(data);
		while (sLineMatcher.find()) {
			String sFilename = sLineMatcher.group(1);
			try {
				File newFile = new File(directory + separator + sFilename);
				newFile.createNewFile();
				FileOutputStream copyin = new FileOutputStream(newFile);
				FileInputStream copyout = new FileInputStream(new File(root + separator + sFilename));
				int read = copyout.read();
				while (read != -1) {
					copyin.write(read);
					read = copyout.read();
				}
				copyin.close();
				copyout.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Cannot copy file " + sFilename, "Copy Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void updateMenu(boolean logEnabled, boolean othersEnabled) {
		viewLearnedModel.setEnabled(othersEnabled);
		viewCoverage.setEnabled(othersEnabled);
		save.setEnabled(othersEnabled);
		saveAll.setEnabled(othersEnabled);
		viewLog.setEnabled(logEnabled);
		// Do saveas & save button too
	}

	@Override
	public void mousePressed(MouseEvent e) {
		executePopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		executePopupMenu(e);
	}

	public void executePopupMenu(MouseEvent e) {
		if (e.getSource() instanceof JTree && tree.getFile() != null && e.isPopupTrigger()) {
			// frame.getGlassPane().setVisible(false);
			popup.removeAll();
			if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml") || tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
				JMenuItem create = new JMenuItem("Create Analysis View");
				create.addActionListener(this);
				create.addMouseListener(this);
				create.setActionCommand("createSim");
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem edit = new JMenuItem("View/Edit (graphical)");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("gcmEditor");
				JMenuItem editText = new JMenuItem("View/Edit (tabular)");
				editText.addActionListener(this);
				editText.addMouseListener(this);
				editText.setActionCommand("gcmTextEditor");
				// JMenuItem graph = new JMenuItem("View Model");
				// graph.addActionListener(this);
				// graph.addMouseListener(this);
				// graph.setActionCommand("graphTree");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(create);
				popup.add(createSynthesis);
				popup.add(createLearn);
				popup.addSeparator();
				// popup.add(graph);
				// popup.addSeparator();
				popup.add(edit);
				popup.add(editText);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
				/*
				JMenuItem edit = new JMenuItem("View/Edit");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("sbmlEditor");
				JMenuItem graph = new JMenuItem("View Model");
				graph.addActionListener(this);
				graph.addMouseListener(this);
				graph.setActionCommand("graphTree");
				JMenuItem browse = new JMenuItem("View Model in Browser");
				browse.addActionListener(this);
				browse.addMouseListener(this);
				browse.setActionCommand("browse");
				JMenuItem simulate = new JMenuItem("Create Analysis View");
				simulate.addActionListener(this);
				simulate.addMouseListener(this);
				simulate.setActionCommand("simulate");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
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
				*/
			}
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol")) {
				JMenuItem view = new JMenuItem("View");
				view.addActionListener(this);
				view.addMouseListener(this);
				view.setActionCommand("browseSbol");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				popup.add(view);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
				JMenuItem create = new JMenuItem("Create Analysis View");
				create.addActionListener(this);
				create.addMouseListener(this);
				create.setActionCommand("createSim");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem edit = new JMenuItem("View/Edit (graphical)");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("gcmEditor");
				JMenuItem editText = new JMenuItem("View/Edit (tabular)");
				editText.addActionListener(this);
				editText.addMouseListener(this);
				editText.setActionCommand("gcmTextEditor");
				// JMenuItem graph = new JMenuItem("View Model");
				// graph.addActionListener(this);
				// graph.addMouseListener(this);
				// graph.setActionCommand("graphTree");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(create);
				popup.add(createLearn);
				popup.addSeparator();
				// popup.add(graph);
				// popup.addSeparator();
				popup.add(edit);
				popup.add(editText);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
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
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams")) {
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				if (lema) {
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
			}
			else if (tree.getFile().length() > 2 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv")) {
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				if (lema) {
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
			}
			else if (tree.getFile().length() > 1 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				if (atacs) {
					popup.add(createSynthesis);
				}
				// popup.add(createAnalysis);
				// if (lema) {
				// popup.add(createLearn);
				// }
				popup.add(createVerification);
				popup.addSeparator();
				popup.add(viewModel);
				popup.addSeparator();
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem convertToSBML = new JMenuItem("Convert To SBML");
				convertToSBML.addActionListener(this);
				convertToSBML.addMouseListener(this);
				convertToSBML.setActionCommand("convertToSBML");
				JMenuItem convertToVerilog = new JMenuItem("Save as Verilog");
				convertToVerilog.addActionListener(this);
				convertToVerilog.addMouseListener(this);
				convertToVerilog.setActionCommand("convertToVerilog");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem view = new JMenuItem("View/Edit");
				view.addActionListener(this);
				view.addMouseListener(this);
				view.setActionCommand("openLPN");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				if (atacs) {
					popup.add(createSynthesis);
				}
				if (libsbmlFound) {
					popup.add(createAnalysis);
				}
				popup.add(createVerification);
				if (lema) {
					popup.add(createLearn);
					popup.addSeparator();
				}
				if (atacs || lema) {
					popup.add(convertToVerilog);
				}
				popup.add(viewModel);
				popup.addSeparator();
				popup.add(view);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".prop")) {
				
				JMenuItem convertToLPN = new JMenuItem("Convert To LPN");
				convertToLPN.addActionListener(this);
				convertToLPN.addMouseListener(this);
				convertToLPN.setActionCommand("convertToLPN");
				
				
				JMenuItem view = new JMenuItem("View/Edit");
				view.addActionListener(this);
				view.addMouseListener(this);
				view.setActionCommand("openLPN");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				
				if (lema) {
					popup.add(createLearn);
					popup.addSeparator();
					popup.add(viewModel);
				}
				popup.addSeparator();
				popup.add(view);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
				popup.add(convertToLPN);
			}
			
			else if (tree.getFile().length() > 1 && tree.getFile().substring(tree.getFile().length() - 2).equals(".s")) {
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(createAnalysis);
				popup.add(createVerification);
				popup.addSeparator();
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".inst")) {
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
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
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createAnalysis = new JMenuItem("Create Analysis View");
				createAnalysis.addActionListener(this);
				createAnalysis.addMouseListener(this);
				createAnalysis.setActionCommand("createAnalysis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
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
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(createSynthesis);
				popup.addSeparator();
				popup.add(viewModel);
				popup.addSeparator();
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem viewModel = new JMenuItem("View Model");
				viewModel.addActionListener(this);
				viewModel.addMouseListener(this);
				viewModel.setActionCommand("viewModel");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(createSynthesis);
				popup.addSeparator();
				popup.add(viewModel);
				popup.addSeparator();
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
				JMenuItem edit = new JMenuItem("View/Edit");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("openGraph");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
				rename.setActionCommand("rename");
				popup.add(edit);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".prb")) {
				JMenuItem edit = new JMenuItem("View/Edit");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("openHistogram");
				JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(this);
				delete.addMouseListener(this);
				delete.setActionCommand("delete");
				JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(this);
				copy.addMouseListener(this);
				copy.setActionCommand("copy");
				JMenuItem rename = new JMenuItem("Rename");
				rename.addActionListener(this);
				rename.addMouseListener(this);
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
				boolean learn = false;
				for (String s : new File(tree.getFile()).list()) {
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
						sim = true;
					}
					if ((s.length() > 3 && s.substring(s.length() - 4).equals(".syn")) || 
							s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)) {
						synth = true;
					}
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".ver")) {
						ver = true;
					}
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".lrn")) {
						learn = true;
					}
				}
				JMenuItem open;
				if (sim) {
					open = new JMenuItem("Open Analysis View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openSim");
					popup.add(open);
				}
				else if (synth) {
					open = new JMenuItem("Open Synthesis View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openSynth");
					popup.add(open);
				}
				else if (ver) {
					open = new JMenuItem("Open Verification View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openVerification");
					popup.add(open);
				}
				else if (learn) {
					open = new JMenuItem("Open Learn View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openLearn");
					popup.add(open);
				}
				if (sim || ver || synth || learn)
					popup.addSeparator();
				if (sim || ver || learn) {
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.addMouseListener(this);
					copy.setActionCommand("copy");
					popup.add(copy);
				}
				if (sim || ver || synth || learn) {
					JMenuItem delete = new JMenuItem("Delete");
					delete.addActionListener(this);
					delete.addMouseListener(this);
					delete.setActionCommand("deleteSim");
					JMenuItem rename = new JMenuItem("Rename");
					rename.addActionListener(this);
					rename.addMouseListener(this);
					rename.setActionCommand("rename");
					popup.add(rename);
					popup.add(delete);
				}
			}
			if (popup.getComponentCount() != 0) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void executeMouseClickEvent(MouseEvent e) {
		if (!(e.getSource() instanceof JTree)) {
			enableTabMenu(tab.getSelectedIndex());
			// frame.getGlassPane().setVisible(true);
		}
		else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && e.getSource() instanceof JTree && tree.getFile() != null) {
			if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml") || tree.getFile().length() >= 4
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
				openSBML(tree.getFile());
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
				openGCM(false);
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol")) {
				openSBOL();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd")) {
				openModel("VHDL");
			}
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".s")) {
				openModel("Assembly File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".inst")) {
				openModel("Instruction File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".prop")) {   //Dhanashree
				openModel("Property File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams")) {
				openModel("Verilog-AMS");
			}
			else if (tree.getFile().length() >= 3 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv")) {
				openModel("SystemVerilog");
			}
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
				openModel("Petri Net");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn")) {
				openLPN();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")) {
				openModel("CSP");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")) {
				openModel("Handshaking Expansion");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")) {
				openModel("Extended Burst-Mode Machine");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg")) {
				openModel("Reduced State Graph");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".cir")) {
				openModel("Spice Circuit");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".grf")) {
				openGraph();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".prb")) {
				openHistogram();
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
					else if ((s.length() > 3 && s.substring(s.length() - 4).equals(".syn")) || 
							s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)) {
						synth = true;
					}
					else if (s.length() > 3 && s.substring(s.length() - 4).equals(".ver")) {
						ver = true;
					}
					else if (s.length() > 3 && s.substring(s.length() - 4).equals(".lrn")) {
						learn = true;
					}
				}
				if (sim) {
					try {
						openSim();
					}
					catch (Exception e0) {
						e0.printStackTrace();
					}
				}
				else if (synth) {
					openSynth();
				}
				else if (ver) {
					openVerify();
				}
				else if (learn) {
					if (lema) {
						openLearnLHPN();
					}
					else {
						openLearn();
					}
				}
			}
			else if (new File(tree.getFile()).isDirectory() && tree.getFile().equals(root)) {
				tree.expandPath(tree.getRoot());
			}
		}
		else {
			enableTreeMenu();
			return;
		}
		enableTabMenu(tab.getSelectedIndex());
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame.getContentPane());
		if (containerPoint.y < 0) { // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) {
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
				component.dispatchEvent(new MouseWheelEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y,
						e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else {
			Component deepComponent = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
			Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, deepComponent);
			// if (deepComponent instanceof ScrollableTabPanel) {
			// deepComponent = tab.findComponentAt(componentPoint);
			// }
			deepComponent.dispatchEvent(new MouseWheelEvent(deepComponent, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x,
					componentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
		}
	}
	
	private String identifySBOLSynthesisPath(String actionCommand) {
		String[] splitCommand = actionCommand.split("_");
		String synthFilePath = root.replace(new File(root).getName(), splitCommand[0]);
		String synthFileID = "";
		for (int i = 2; i < splitCommand.length; i++)
			synthFileID = synthFileID + "_" + splitCommand[i];
		synthFilePath = synthFilePath + synthFileID;
		int synthIndex = 1;
		while (new File(synthFilePath + "_" + synthIndex).exists())
			synthIndex++;
		synthFilePath = synthFilePath + "_" + synthIndex;
		return synthFilePath;
	}
	
	private void synthesizeSBOL(SynthesisView synthView) {
		synthView.save();
		ActionEvent projectSynthesized = new ActionEvent(newProj, ActionEvent.ACTION_PERFORMED, 
				GlobalConstants.SBOL_SYNTH_COMMAND + "_" + synthView.getSpecFileID().replace(".xml", ""));
		actionPerformed(projectSynthesized);
		if (!synthView.getRootDirectory().equals(root)) {
//			String outputFileID = synthView.getSpecFileID();
//			int version = 1;
//			while(!overwrite(root + separator + outputFileID, outputFileID)) {
//				outputFileID = synthView.getSpecFileID().replace(".xml", "") + "_" + version + ".xml";
//				version++;
//			}
			List<String> solutionFileIDs = synthView.run(root);
			if (solutionFileIDs.size() > 0) {
				for (String solutionFileID : solutionFileIDs)
					addToTree(solutionFileID);
				ModelEditor modelEditor;
				try {
					modelEditor = new ModelEditor(root + separator, solutionFileIDs.get(0), this, log, false, null, null, null, false, false);
					ActionEvent applyLayout = new ActionEvent(synthView, ActionEvent.ACTION_PERFORMED, "layout_verticalHierarchical");
					modelEditor.getSchematic().actionPerformed(applyLayout);
					addTab(solutionFileIDs.get(0), modelEditor, "GCM Editor");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void createSBOLSynthesisView() {
		String specFileID = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
		String defaultSynthID = specFileID.replace(".xml","");
		String synthID = JOptionPane.showInputDialog(frame, "Enter synthesis ID (default = " + defaultSynthID + "):", 
				"Synthesis ID", JOptionPane.PLAIN_MESSAGE);
		if (synthID != null) {
			if (synthID.length() == 0)
				synthID = defaultSynthID;
			else 
				synthID = synthID.trim();
			if (overwrite(root + separator + synthID, synthID)) {
				SynthesisView synthView = new SynthesisView(synthID, separator, root, log);
				synthView.loadDefaultSynthesisProperties(specFileID);
				addTab(synthID, synthView, null);
				addToTree(synthID);
			}
		}
	}
	
	private void openSBOLSynthesisView() {
		Properties synthProps = SBOLUtility.loadSBOLSynthesisProperties(tree.getFile(), separator, frame);
		if (synthProps != null) {
			String synthID = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1];
			SynthesisView synthView = new SynthesisView(synthID, separator, root, log);
			synthView.loadSynthesisProperties(synthProps);
			addTab(synthID, synthView, null);
		}
	}

	private void createAnalysisView(int fileType) throws Exception {
		String sbmlFile;
		String modelId = tree.getFile().split(separator)[tree.getFile().split(separator).length - 1].replace(".xml","").replace(".lpn","");
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				if (save(i, 0) == 0) {
					return;
				}
				break;
			}
		}
		if (fileType == 0) {
			SBMLutilities.readSBML(tree.getFile());
		}
		sbmlFile = tree.getFile();
		String[] sbml1 = null;
		String simName = JOptionPane.showInputDialog(frame, "Enter analysis ID (default=" + modelId + "):", 
				"Analysis ID", JOptionPane.PLAIN_MESSAGE);
		if (simName == null) return;
		if (simName.equals("")) simName = modelId;
		simName = simName.trim();
		if (!overwrite(root + separator + simName, simName)) return;
		new File(root + separator + simName).mkdir();
		sbml1 = tree.getFile().split(separator);
		String sbmlFileProp;
		if (fileType == 1) {
			sbmlFile = (sbml1[sbml1.length - 1].substring(0, sbml1[sbml1.length - 1].length() - 3) + "xml");
			new File(root + separator + simName + separator + sbmlFile).createNewFile();
			sbmlFileProp = root + separator + simName + separator
					+ (sbml1[sbml1.length - 1].substring(0, sbml1[sbml1.length - 1].length() - 3) + "xml");
			sbmlFile = sbmlFileProp;
		}
		else if (fileType == 2) {
			sbmlFile = (sbml1[sbml1.length - 1].substring(0, sbml1[sbml1.length - 1].length() - 3) + "xml");
			Translator t1 = new Translator();
			t1.convertLPN2SBML(tree.getFile(), "");
			t1.setFilename(root + separator + simName + separator + sbmlFile);
			t1.outputSBML();
			sbmlFileProp = root + separator + simName + separator
					+ (sbml1[sbml1.length - 1].substring(0, sbml1[sbml1.length - 1].length() - 3) + "xml");
			sbmlFile = sbmlFileProp;
		}
		else {
			sbmlFileProp = root + separator + simName + separator + sbml1[sbml1.length - 1];
			new FileOutputStream(new File(sbmlFileProp)).close();
		}
		try {
			FileOutputStream out = new FileOutputStream(new File(root + separator + simName.trim() + separator + simName.trim() + ".sim"));
			out.write((sbml1[sbml1.length - 1] + "\n").getBytes());
			out.close();
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		addToTree(simName);
		JTabbedPane simTab = new JTabbedPane();
		simTab.addMouseListener(this);
		AnalysisView reb2sac = new AnalysisView(sbmlFile, sbmlFileProp, root, this, simName.trim(), log, simTab, null, sbml1[sbml1.length - 1], null);
		simTab.addTab("Simulation Options", reb2sac);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
		if (fileType == 2) {
			simTab.addTab("Advanced Options", new AbstPane(root, sbml1[sbml1.length - 1], log));
		}
		else {
			simTab.addTab("Advanced Options", reb2sac.getAdvanced());
		}
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		if (sbml1[sbml1.length - 1].contains(".gcm")) {
			String gcmFile = sbml1[sbml1.length - 1];
			ModelEditor modelEditor = new ModelEditor(root + separator, gcmFile, this, log, true, simName.trim(), root
					+ separator + simName.trim() + separator + simName.trim() + ".sim", reb2sac, false, false);
			reb2sac.setGcm(modelEditor);
			ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
					root + separator + simName.trim() + separator + simName.trim() + ".sim");
			modelEditor.setElementsPanel(elementsPanel);
			addModelViewTab(reb2sac, simTab, modelEditor);
			simTab.addTab("Parameters", modelEditor);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("GCM Editor");
			//simTab.addTab("SBML Elements", elementsPanel);
			//simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		}
		else if (sbml1[sbml1.length - 1].contains(".sbml") || sbml1[sbml1.length - 1].contains(".xml")) {
			String gcmFile = sbml1[sbml1.length - 1].replace(".xml", ".gcm");
			ModelEditor modelEditor = new ModelEditor(root + separator, gcmFile, this, log, true, simName.trim(), root
					+ separator + simName.trim() + separator + simName.trim() + ".sim", reb2sac, false, false);
			reb2sac.setGcm(modelEditor);
			ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
					root + separator + simName.trim() + separator + simName.trim() + ".sim");
			modelEditor.setElementsPanel(elementsPanel);
			addModelViewTab(reb2sac, simTab, modelEditor);
			simTab.addTab("Parameters", modelEditor);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("GCM Editor");
			//simTab.addTab("SBML Elements", elementsPanel);
			//simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		}
		Graph tsdGraph;
		tsdGraph = reb2sac.createGraph(null);
		simTab.addTab("TSD Graph", tsdGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
		Graph probGraph = reb2sac.createProbGraph(null);
		simTab.addTab("Histogram", probGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
		addTab(simName, simTab, null);
	}

	private void openLearn() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JTabbedPane lrnTab = new JTabbedPane();
			lrnTab.addMouseListener(this);
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
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
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

			String lrnFile = tree.getFile() + separator + tree.getFile().split(separator)[tree.getFile().split(separator).length - 1] + ".lrn";
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
						if (learnFile.endsWith(".gcm")) {
							learnFile = learnFile.replace(".gcm", ".xml");
							load.setProperty("genenet.file", learnFile);
						}
					}
				}
				FileOutputStream out = new FileOutputStream(new File(lrnFile));
				load.store(out, learnFile);
				out.close();

			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(learnFile)) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			if (!(new File(root + separator + learnFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + learnFile + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			DataManager data = new DataManager(tree.getFile(), this);
			// data.addMouseListener(this);
			lrnTab.addTab("Data Manager", data);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
			LearnGCM learn = new LearnGCM(tree.getFile(), log, this);
			// learn.addMouseListener(this);
			lrnTab.addTab("Learn Options", learn);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
			lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
			Graph tsdGraph = new Graph(null, "Number of molecules", tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "Time", this, open, log, null, true, true);
			// tsdGraph.addMouseListener(this);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], lrnTab, null);
		}
	}

	private void openLearnLHPN() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			JTabbedPane lrnTab = new JTabbedPane();
			lrnTab.addMouseListener(this);
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
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
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

			String lrnFile = tree.getFile() + separator + tree.getFile().split(separator)[tree.getFile().split(separator).length - 1] + ".lrn";
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
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(learnFile)) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			if (!(new File(root + separator + learnFile).exists())) {
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + learnFile + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			DataManager data = new DataManager(tree.getFile(), this);
			lrnTab.addTab("Data Manager", data);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
			LearnLHPN learn = new LearnLHPN(tree.getFile(), log, this);
			lrnTab.addTab("Learn Options", learn);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
			lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
			Graph tsdGraph = new Graph(null, "Number of molecules", tree.getFile().split(separator)[tree.getFile().split(separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "Time", this, open, log, null, true, true);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], lrnTab, null);
		}
	}

	private void openSynth() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			boolean sbolSynth = false;
			if (new File(tree.getFile()).isDirectory()) {
				String[] fileIDs = new File(tree.getFile()).list();
				for (int i = 0; i < fileIDs.length; i++)
					if (fileIDs[i].endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)) {
						i = fileIDs.length;
						sbolSynth = true;
					}
			}
			if (sbolSynth) 
				openSBOLSynthesisView();
			else {
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
									int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
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

				String synthFile = tree.getFile() + separator + tree.getFile().split(separator)[tree.getFile().split(separator).length - 1] + ".syn";
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
							synthesisFile = synthesisFile.split(separator)[synthesisFile.split(separator).length - 1];
						}
					}
					//FileOutputStream out = new FileOutputStream(new File(synthesisFile));
					//load.store(out, synthesisFile);
					//out.close();

				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
				}
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (getTitleAt(i).equals(synthesisFile)) {
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0) {
							return;
						}
						break;
					}
				}
				if (!(new File(root + separator + synthesisFile).exists())) {
					JOptionPane.showMessageDialog(frame, "Unable to open view because " + synthesisFile + " is missing.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// if (!graphFile.equals("")) {
				Synthesis synth = new Synthesis(tree.getFile(), "flag", log, this);
				// synth.addMouseListener(this);
				synthPanel.add(synth);
				addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], synthPanel, "Synthesis");
			}
		}
	}

	private void openVerify() {
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1])) {
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done) {
			// JPanel verPanel = new JPanel();
			// JPanel abstPanel = new JPanel();
			// JPanel verTab = new JTabbedPane();
			// String graphFile = "";
			/*
			 * if (new File(tree.getFile()).isDirectory()) { String[] list = new
			 * File(tree.getFile()).list(); int run = 0; for (int i = 0; i <
			 * list.length; i++) { if (!(new File(list[i]).isDirectory()) &&
			 * list[i].length() > 4) { String end = ""; for (int j = 1; j < 5;
			 * j++) { end = list[i].charAt(list[i].length() - j) + end; } if
			 * (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
			 * { if (list[i].contains("run-")) { int tempNum =
			 * Integer.parseInt(list[i].substring(4, list[i] .length() -
			 * end.length())); if (tempNum > run) { run = tempNum; // graphFile
			 * = tree.getFile() + separator + // list[i]; } } } } } }
			 */

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
				// FileOutputStream out = new FileOutputStream(new
				// File(verifyFile));
				// load.store(out, verifyFile);
				// out.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++) {
				if (getTitleAt(i).equals(verifyFile)) {
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0) {
						return;
					}
					break;
				}
			}
			if (!(new File(verFile).exists())) {
				JOptionPane
						.showMessageDialog(frame, "Unable to open view because " + verifyFile + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			Verification ver = new Verification(root + separator + verName, verName, "flag", log, this, lema, atacs);
			// ver.addMouseListener(this);
			// verPanel.add(ver);
			// AbstPane abst = new AbstPane(root + separator + verName, ver,
			// "flag", log, this, lema,
			// atacs);
			// abstPanel.add(abst);
			// verTab.add("verify", verPanel);
			// verTab.add("abstract", abstPanel);
			addTab(tree.getFile().split(separator)[tree.getFile().split(separator).length - 1], ver, "Verification");
		}
	}

	private void openSim() throws Exception {
		String filename = tree.getFile();
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(filename.split(separator)[filename.split(separator).length - 1])) {
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
							if (end.equals(".xml")) {
								getAFile = filename + separator + list[i];
							}
							else if (end.equals("sbml") && getAFile.equals("")) {
								getAFile = filename + separator + list[i];
							}
							else if (end.equals(".txt") && list[i].contains("sim-rep")) {
								// probFile = filename + separator + list[i];
							}
							else if (end.equals("ties") && list[i].contains("properties") && !(list[i].equals("species.properties"))) {
								openFile = filename + separator + list[i];
							}
							else if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv") || end.contains("=")) {
								if (list[i].contains("run-")) {
									int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
									if (tempNum > run) {
										run = tempNum;
										// graphFile = filename + separator +
										// list[i];
									}
								}
								else if (list[i].contains("euler-run.") || list[i].contains("gear1-run.") || list[i].contains("gear2-run.")
										|| list[i].contains("rk4imp-run.") || list[i].contains("rk8pd-run.") || list[i].contains("rkf45-run.")) {
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
						String simFile = root + separator + split[split.length - 1].trim() + separator + split[split.length - 1].trim() + ".sim";
						String pmsFile = root + separator + split[split.length - 1].trim() + separator + split[split.length - 1].trim() + ".pms";
						if (new File(pmsFile).exists()) {
							if (new File(simFile).exists()) {
								new File(pmsFile).delete();
							}
							else {
								new File(pmsFile).renameTo(new File(simFile));
							}
						}
						String sbmlLoadFile = "";
						String gcmFile = "";
						// ArrayList<String> interestingSpecies = new
						// ArrayList<String>();
						if (new File(simFile).exists()) {
							try {
								Scanner s = new Scanner(new File(simFile));
								if (s.hasNextLine()) {
									sbmlLoadFile = s.nextLine();
									sbmlLoadFile = sbmlLoadFile.split(separator)[sbmlLoadFile.split(separator).length - 1];
									gcmFile = sbmlLoadFile;
									if (sbmlLoadFile.endsWith(".gcm"))
										sbmlLoadFile = sbmlLoadFile.replace(".gcm", ".xml");
									if (sbmlLoadFile.equals("")) {
										JOptionPane.showMessageDialog(frame, "Unable to open view because "
												+ "the sbml linked to this view is missing.", "Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
									else if (!(new File(root + separator + sbmlLoadFile).exists())) {
										JOptionPane.showMessageDialog(frame, "Unable to open view because " + sbmlLoadFile + " is missing.", "Error",
												JOptionPane.ERROR_MESSAGE);
										return;
									}
									if (sbmlLoadFile.contains(".gcm")) {
										// GCMParser parser = new GCMParser(root
										// + separator + sbmlLoadFile);
										// GeneticNetwork network =
										// parser.buildNetwork();
										// interestingSpecies.addAll(network.getInterestingSpecies());
										// GeneticNetwork.setRoot(root +
										// separator);
										sbmlLoadFile = root + separator + split[split.length - 1].trim() + separator
												+ sbmlLoadFile.replace(".gcm", ".xml");
										// network.mergeSBML(sbmlLoadFile);
									}
									else if (sbmlLoadFile.contains(".lpn")) {
										// Translator t1 = new Translator();
										// t1.BuildTemplate(root + separator +
										// sbmlLoadFile, "");
										sbmlLoadFile = root + separator + split[split.length - 1].trim() + separator
												+ sbmlLoadFile.replace(".lpn", ".xml");
										// t1.setFilename(sbmlLoadFile);
										// t1.outputSBML();
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
								e.printStackTrace();
								JOptionPane.showMessageDialog(frame, "Unable to load sbml file.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						else {
							sbmlLoadFile = root + separator + getAFile.split(separator)[getAFile.split(separator).length - 1];
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
									"Unable to open view because " + sbmlLoadFile.split(separator)[sbmlLoadFile.split(separator).length - 1]
											+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						for (int i = 0; i < tab.getTabCount(); i++) {
							if (getTitleAt(i).equals(sbmlLoadFile.split(separator)[sbmlLoadFile.split(separator).length - 1])) {
								tab.setSelectedIndex(i);
								if (save(i, 0) == 0) {
									return;
								}
								break;
							}
						}
						JTabbedPane simTab = new JTabbedPane();
						simTab.addMouseListener(this);
						AbstPane lhpnAbstraction = null;
						if (gcmFile.contains(".lpn")) {
							lhpnAbstraction = new AbstPane(root, gcmFile, log);
						}
						AnalysisView reb2sac;
						if (gcmFile.contains(".lpn")) {
							reb2sac = new AnalysisView(sbmlLoadFile, getAFile, root, this, split[split.length - 1].trim(), log, simTab, openFile, gcmFile,
									lhpnAbstraction);
						}
						else {
							reb2sac = new AnalysisView(sbmlLoadFile, getAFile, root, this, split[split.length - 1].trim(), log, simTab, openFile, gcmFile,
									null);
						}
						simTab.addTab("Simulation Options", reb2sac);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
						if (gcmFile.contains(".lpn")) {
							simTab.addTab("Advanced Options", lhpnAbstraction);
						}
						else {
							simTab.addTab("Advanced Options", reb2sac.getAdvanced());
						}
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
						// simTab.addTab("Advanced Options",
						// reb2sac.getProperties());
						// simTab.getComponentAt(simTab.getComponents().length -
						// 1).setName("");
						if (gcmFile.contains(".xml")||gcmFile.contains(".gcm")) {
							/*
							SBML_Editor sbml = new SBML_Editor(root + separator + gcmFile.replace(".gcm",".xml"), reb2sac, log, this, root + separator
									+ split[split.length - 1].trim(), root + separator + split[split.length - 1].trim() + separator
									+ split[split.length - 1].trim() + ".sim");
									*/
							ModelEditor modelEditor = new ModelEditor(root + separator, gcmFile, this, log, true, split[split.length - 1].trim(), root
									+ separator + split[split.length - 1].trim() + separator + split[split.length - 1].trim() + ".sim", reb2sac,
									false, false);
							reb2sac.setGcm(modelEditor);
							// sbml.addMouseListener(this);
							ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
									root + separator + split[split.length - 1].trim() + separator
									+ split[split.length - 1].trim() + ".sim");
							modelEditor.setElementsPanel(elementsPanel);
							addModelViewTab(reb2sac, simTab, modelEditor);
							simTab.addTab("Parameters", modelEditor);
							simTab.getComponentAt(simTab.getComponents().length - 1).setName("GCM Editor");
							//simTab.addTab("SBML Elements", elementsPanel);
							//simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
						}
						Graph tsdGraph = reb2sac.createGraph(open);
						simTab.addTab("TSD Graph", tsdGraph);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
						Graph probGraph = reb2sac.createProbGraph(openProb);
						simTab.addTab("Histogram", probGraph);
						simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
						addTab(split[split.length - 1], simTab, null);
					}
				}
			}
		}
	}

	/**
	 * adds the tab for the modelview and the correct listener.
	 * 
	 * @return
	 */
	private void addModelViewTab(AnalysisView reb2sac, JTabbedPane tabPane, ModelEditor modelEditor) {

		// Add the modelview tab
		MovieContainer movieContainer = new MovieContainer(reb2sac, modelEditor.getBioModel(), this, modelEditor, lema);

		tabPane.addTab("Schematic", movieContainer);
		tabPane.getComponentAt(tabPane.getComponents().length - 1).setName("ModelViewMovie");
		// When the Graphical View panel gets clicked on, tell it to display
		// itself.
		tabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				JTabbedPane selectedTab = (JTabbedPane) (e.getSource());
				if (selectedTab.getSelectedIndex()<0) return;
				if (!(selectedTab.getComponent(selectedTab.getSelectedIndex()) instanceof JScrollPane)) {
					JPanel selectedPanel = (JPanel) selectedTab.getComponent(selectedTab.getSelectedIndex());
					String className = selectedPanel.getClass().getName();

					// The new Schematic
					if (className.indexOf("MovieContainer") >= 0) {
						((MovieContainer) selectedPanel).display();
					}
				}
			}
		});
	}
/*
	private class NewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		NewAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			popup.add(newProj);
			if (!async) {
				popup.add(newSBMLModel);
				popup.add(newGridModel);
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
				popup.add(newProperty);
				popup.add(newLhpn);
				popup.add(newSpice);
			}
			popup.add(graph);
			popup.add(probGraph);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x, mainPanel.getMousePosition().y);
			}
		}
	}

	private class ImportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ImportAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			if (!lema) {
				//popup.add(importDot);
				popup.add(importSbol);
				popup.add(importSedml);
				popup.add(importSbml);
				popup.add(importBioModel);
			}
			else if (atacs) {
				popup.add(importVhdl);
				popup.add(importLpn);
				popup.add(importCsp);
				popup.add(importHse);
				popup.add(importUnc);
				popup.add(importRsg);
			}
			else {
				popup.add(importVhdl);
				popup.add(importS);
				popup.add(importInst);
				popup.add(importLpn);
				popup.add(importSpice);
			}
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x, mainPanel.getMousePosition().y);
			}
		}
	}

	private class ExportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ExportAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
//			popup.add(exportCsv);
//			popup.add(exportDat);
//			popup.add(exportEps);
//			popup.add(exportJpg);
//			popup.add(exportPdf);
//			popup.add(exportPng);
//			popup.add(exportSvg);
//			popup.add(exportTsd);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x, mainPanel.getMousePosition().y);
			}
		}
	}

	private class ModelAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ModelAction() {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			popup.add(viewModGraph);
			popup.add(viewModBrowser);
			if (popup.getComponentCount() != 0) {
				popup.show(mainPanel, mainPanel.getMousePosition().x, mainPanel.getMousePosition().y);
			}
		}
	}
*/
	@Override
	public void mouseClicked(MouseEvent e) {
		executeMouseClickEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame.getContentPane());
		if (containerPoint.y < 0) { // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) {
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
				component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e
						.getClickCount(), e.isPopupTrigger()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else {
			try {
				Component deepComponent = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, deepComponent);
				deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x,
						componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
			}
			catch (Exception e1) {
			}
		}
	}

	public void windowLostFocus() {
	}

	public JMenuItem getExitButton() {
		return exit;
	}

	/**
	 * This is the main method. It excecutes the BioSim GUI FrontEnd program.
	 */
	public static void main(String args[]) {
		boolean lemaFlag = false, atacsFlag = false, libsbmlFound = true;
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
		if (!lemaFlag && !atacsFlag) {
			/*
			String varname;
			if (System.getProperty("mrj.version") != null)
				varname = "DYLD_LIBRARY_PATH"; // We're on a Mac.
			else
				varname = "LD_LIBRARY_PATH"; // We're not on a Mac.
				*/
			try {
				System.loadLibrary("sbmlj");
				// For extra safety, check that the jar file is in the
				// classpath.
				Class.forName("org.sbml.libsbml.libsbml");
			}
			catch (UnsatisfiedLinkError e) {
				libsbmlFound = false;
			}
			catch (ClassNotFoundException e) {
				libsbmlFound = false;
			}
			catch (SecurityException e) {
				libsbmlFound = false;
			}
//			catch (UnsatisfiedLinkError e) {
//				e.printStackTrace();
//				System.err.println("Error: could not link with the libSBML library." + "  It is likely\nyour " + varname
//						+ " environment variable does not include\nthe" + " directory containing the libsbml library file.");
//				System.exit(1);
//			}
//			catch (ClassNotFoundException e) {
//				System.err.println("Error: unable to load the file libsbmlj.jar." + "  It is likely\nyour " + varname + " environment"
//						+ " variable or CLASSPATH variable\ndoes not include" + " the directory containing the libsbmlj.jar file.");
//				System.exit(1);
//			}
//			catch (SecurityException e) {
//				System.err.println("Could not load the libSBML library files due to a" + " security exception.");
//				System.exit(1);
//			}
		}
		else {
			/*
			 * String varname; if (System.getProperty("mrj.version") != null)
			 * varname = "DYLD_LIBRARY_PATH"; // We're on a Mac. else varname =
			 * "LD_LIBRARY_PATH"; // We're not on a Mac.
			 */
			try {
				System.loadLibrary("sbmlj");
				// For extra safety, check that the jar file is in the
				// classpath.
				Class.forName("org.sbml.jsbml.libsbml");
			}
			catch (UnsatisfiedLinkError e) {
				libsbmlFound = false;
			}
			catch (ClassNotFoundException e) {
				libsbmlFound = false;
			}
			catch (SecurityException e) {
				libsbmlFound = false;
			}
		}
		Gui gui = new Gui(lemaFlag, atacsFlag, libsbmlFound);
	}
	
	public static boolean isLibsbmlFound() {
		return libsbmlFound;
	}

	public void copySim(String newSim) {
		try {
			new File(root + separator + newSim).mkdir();
			// new FileWriter(new File(root + separator + newSim + separator +
			// ".sim")).close();
			String oldSim = getTitleAt(tab.getSelectedIndex());
			String[] s = new File(root + separator + oldSim).list();
			String sbmlFile = "";
			String propertiesFile = "";
			String sbmlLoadFile = null;
			for (String ss : s) {
				if (ss.length() > 4 && ss.substring(ss.length() - 5).equals(".sbml") || ss.length() > 3
						&& ss.substring(ss.length() - 4).equals(".xml")) {
					SBMLDocument document = SBMLutilities.readSBML(root + separator + oldSim + separator + ss);
					SBMLWriter writer = new SBMLWriter();
					writer.writeSBMLToFile(document, root + separator + newSim + separator + ss);
					sbmlFile = root + separator + newSim + separator + ss;
				}
				else if (ss.length() > 10 && ss.substring(ss.length() - 11).equals(".properties")) {
					FileOutputStream out = new FileOutputStream(new File(root + separator + newSim + separator + ss));
					FileInputStream in = new FileInputStream(new File(root + separator + oldSim + separator + ss));
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
						&& (ss.substring(ss.length() - 4).equals(".dat") || ss.substring(ss.length() - 4).equals(".sad")
								|| ss.substring(ss.length() - 4).equals(".pms") || ss.substring(ss.length() - 4).equals(".sim"))
						&& !ss.equals(".sim")) {
					FileOutputStream out;
					if (ss.substring(ss.length() - 4).equals(".pms")) {
						out = new FileOutputStream(new File(root + separator + newSim + separator + newSim + ".sim"));
					}
					else if (ss.substring(ss.length() - 4).equals(".sim")) {
						out = new FileOutputStream(new File(root + separator + newSim + separator + newSim + ".sim"));
					}
					else {
						out = new FileOutputStream(new File(root + separator + newSim + separator + ss));
					}
					FileInputStream in = new FileInputStream(new File(root + separator + oldSim + separator + ss));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
					if (ss.substring(ss.length() - 4).equals(".pms")) {
						if (new File(root + separator + newSim + separator + ss.substring(0, ss.length() - 4) + ".sim").exists()) {
							new File(root + separator + newSim + separator + ss).delete();
						}
						else {
							new File(root + separator + newSim + separator + ss).renameTo(new File(root + separator + newSim + separator
									+ ss.substring(0, ss.length() - 4) + ".sim"));
						}
						ss = ss.substring(0, ss.length() - 4) + ".sim";
					}
					if (ss.substring(ss.length() - 4).equals(".sim")) {
						try {
							Scanner scan = new Scanner(new File(root + separator + newSim + separator + ss));
							if (scan.hasNextLine()) {
								sbmlLoadFile = scan.nextLine();
								sbmlLoadFile = sbmlLoadFile.split(separator)[sbmlLoadFile.split(separator).length - 1];
								//gcmFile = sbmlLoadFile;
								if (sbmlLoadFile.contains(".gcm")) {
									sbmlLoadFile = root + separator + newSim + separator + sbmlLoadFile.replace(".gcm", ".sbml");
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
						catch (IOException e) {
							JOptionPane.showMessageDialog(frame, "Unable to analysis properties file.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
			}
			addToTree(newSim);
			JTabbedPane simTab = new JTabbedPane();
			simTab.addMouseListener(this);
			AnalysisView reb2sac = new AnalysisView(sbmlLoadFile, sbmlFile, root, this, newSim, log, simTab, propertiesFile, sbmlLoadFile, null);
			simTab.addTab("Simulation Options", reb2sac);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
			simTab.addTab("Advanced Options", reb2sac.getAdvanced());
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
//			if (gcmFile.contains(".gcm")) {
//				ModelEditor modelEditor = new ModelEditor(root + separator, gcmFile, this, log, true, newSim, root + separator + newSim + separator
//						+ newSim + ".sim", reb2sac, false, false);
//				reb2sac.setGcm(modelEditor);
//				ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
//						root + separator + newSim + separator + newSim + ".sim");
//				modelEditor.setElementsPanel(elementsPanel);
//				addModelViewTab(reb2sac, simTab, modelEditor);
//				simTab.addTab("Parameters", modelEditor);
//				simTab.getComponentAt(simTab.getComponents().length - 1).setName("GCM Editor");
//				//simTab.addTab("SBML Elements", elementsPanel);
//				//simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
//			}
			Graph tsdGraph = reb2sac.createGraph(null);
			simTab.addTab("TSD Graph", tsdGraph);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
			Graph probGraph = reb2sac.createProbGraph(null);
			simTab.addTab("Histogram", probGraph);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("ProbGraph");
			tab.setComponentAt(tab.getSelectedIndex(), simTab);
			tab.setTitleAt(tab.getSelectedIndex(), newSim);
			tab.getComponentAt(tab.getSelectedIndex()).setName(newSim);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Unable to copy simulation.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void refreshLearn(String learnName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).equals(learnName)) {
				for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getComponentCount(); j++) {
					if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName().equals("TSD Graph")) {
						// if (data) {
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof Graph) {
							((Graph) ((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j)).refresh();
						}
						else {
							((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, new Graph(null, "Number of molecules", learnName + " data",
									"tsd.printer", root + separator + learnName, "Time", this, null, log, null, true, true));
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).setName("TSD Graph");
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName().equals("Learn Options")) {
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof LearnGCM) {
						}
						else {
							if (lema) {
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, new LearnLHPN(root + separator + learnName, log, this));
							}
							else {
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, new LearnGCM(root + separator + learnName, log, this));
							}
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).setName("Learn Options");
						}
					}
				}
			}
		}
	}

	/*
	private void updateGCM() {
		for (int i = 0; i < tab.getTabCount(); i++) {
			if (getTitleAt(i).contains(".gcm")) {
				((ModelEditor) tab.getComponentAt(i)).reloadFiles();
				tab.setTitleAt(i, ((ModelEditor) tab.getComponentAt(i)).getFilename());
			}
		}
	}
	*/

	public boolean updateOpenGCM(String gcmName) {
		
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			if (gcmName.equals(tab)) {
				if (this.tab.getComponentAt(i) instanceof ModelEditor) {
					((ModelEditor) this.tab.getComponentAt(i)).reload(gcmName.replace(".gcm", "").replace(".xml", ""));
					((ModelEditor) this.tab.getComponentAt(i)).refresh();
					((ModelEditor) this.tab.getComponentAt(i)).getSchematic().getGraph().buildGraph();
					return true;
				}
			}
		}
		return false;
	}

	private void renameOpenGCMComponents(String gcmName, String oldname, String newName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			if (gcmName.equals(tab)) {
				if (this.tab.getComponentAt(i) instanceof ModelEditor) {
					((ModelEditor) this.tab.getComponentAt(i)).renameComponents(oldname, newName);
					return;
				}
			}
		}
	}

	public void updateAsyncViews(String updatedFile) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			String properties = root + separator + tab + separator + tab + ".ver";
			String properties1 = root + separator + tab + separator + tab + ".synth";
			String properties2 = root + separator + tab + separator + tab + ".lrn";
			if (new File(properties).exists()) {
				Verification verify = ((Verification) (this.tab.getComponentAt(i)));
				verify.reload();
			}
			if (new File(properties1).exists()) {
				JTabbedPane sim = ((JTabbedPane) (this.tab.getComponentAt(i)));
				for (int j = 0; j < sim.getTabCount(); j++) {
					if (sim.getComponentAt(j).getName().equals("Synthesis")) {
						((Synthesis) (sim.getComponentAt(j))).reload(updatedFile);
					}
				}
			}
			// }
			if (new File(properties2).exists()) {
				String check = "";
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("learn.file")) {
						String[] getProp = p.getProperty("learn.file").split(separator);
						check = getProp[getProp.length - 1];
					}
					else {
						check = "";
					}
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
				if (check.equals(updatedFile)) {
					JTabbedPane learn = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < learn.getTabCount(); j++) {
						if (learn.getComponentAt(j).getName().equals("Data Manager")) {
							((DataManager) (learn.getComponentAt(j))).updateSpecies();
						}
						else if (learn.getComponentAt(j).getName().equals("Learn Options")) {
							((LearnLHPN) (learn.getComponentAt(j))).updateSpecies(root + separator + updatedFile);
							((LearnLHPN) (learn.getComponentAt(j))).reload(updatedFile);
						}
						else if (learn.getComponentAt(j).getName().contains("Graph")) {
							((Graph) (learn.getComponentAt(j))).refresh();
						}
					}
				}
			}
		}
	}

	public void updateViews(String updatedFile) {
		
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);	
			
			if (this.tab.getComponentAt(i).getName().equals("GCM Editor")) {
			
				//this is so that the grid species list gets updated if there's a diffusibility change
				ModelEditor modelEditor = (ModelEditor) this.tab.getComponentAt(i);
				modelEditor.getBioModel().updateGridSpecies(updatedFile.replace(".gcm",""));				
				modelEditor.getSpeciesPanel().refreshSpeciesPanel(modelEditor.getBioModel());
			}
			
			if (this.tab.getComponentAt(i).getName().equals("SBOL Browser")) {
				((SBOLBrowser) this.tab.getComponentAt(i)).reload(this, tab);
			}
			
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
					e.printStackTrace();
				}
				if (check.equals(updatedFile)) {
					JTabbedPane sim = ((JTabbedPane) (this.tab.getComponentAt(i)));
					
					for (int j = 0; j < sim.getTabCount(); j++) {
						
						if (sim.getComponentAt(j) instanceof AnalysisView) {
							((AnalysisView) sim.getComponentAt(j)).updateProperties();
						}
						else if (sim.getComponentAt(j).getName().equals("GCM Editor")) {							
							
							new File(properties).renameTo(new File(properties.replace(".sim", ".temp")));
							try {
								boolean dirty = ((ModelEditor) (sim.getComponentAt(j))).isDirty();
								((ModelEditor) (sim.getComponentAt(j))).saveParams(false, "", true, null);
								((ModelEditor) (sim.getComponentAt(j))).reload(check.replace(".gcm", "").replace(".xml", ""));
								((ModelEditor) (sim.getComponentAt(j))).refresh();
								((ModelEditor) (sim.getComponentAt(j))).loadParams();
								((ModelEditor) (sim.getComponentAt(j))).setDirty(dirty);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							new File(properties).delete();
							new File(properties.replace(".sim", ".temp")).renameTo(new File(properties));
							ElementsPanel elementsPanel = new ElementsPanel(((ModelEditor) (sim.getComponentAt(j))).getBioModel().getSBMLDocument(),
									root + separator + tab + separator + tab + ".sim");
							((ModelEditor) (sim.getComponentAt(j))).setElementsPanel(elementsPanel);
							
							for (int k = 0; k < sim.getTabCount(); k++) {
								
								if (sim.getComponentAt(k) instanceof MovieContainer) {

									// display the schematic and reload the grid
									((MovieContainer) (sim.getComponentAt(k))).display();
									((MovieContainer) (sim.getComponentAt(k))).reloadGrid();
								}
							}
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
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
				if (check.equals(updatedFile)) {
					JTabbedPane learn = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < learn.getTabCount(); j++) {
						if (learn.getComponentAt(j).getName().equals("Data Manager")) {
							((DataManager) (learn.getComponentAt(j))).updateSpecies();
						}
						else if (learn.getComponentAt(j).getName().equals("Learn Options")) {
							((LearnGCM) (learn.getComponentAt(j))).updateSpecies(root + separator + updatedFile);
						}
						else if (learn.getComponentAt(j).getName().contains("Graph")) {
							((Graph) (learn.getComponentAt(j))).refresh();
						}
					}
				}
			}
		}
	}

	private void updateViewNames(String oldname, String newname) {
		File work = new File(root);
		String[] fileList = work.list();
		String[] temp = oldname.split(separator);
		oldname = temp[temp.length - 1];
		for (int i = 0; i < fileList.length; i++) {
			String tabTitle = fileList[i];
			String properties = root + separator + tabTitle + separator + tabTitle + ".ver";
			String properties1 = root + separator + tabTitle + separator + tabTitle + ".synth";
			String properties2 = root + separator + tabTitle + separator + tabTitle + ".lrn";
			if (new File(properties).exists()) {
				String check;
				Properties p = new Properties();
				try {
					FileInputStream load = new FileInputStream(new File(properties));
					p.load(load);
					load.close();
					if (p.containsKey("verification.file")) {
						String[] getProp = p.getProperty("verification.file").split(separator);
						check = getProp[getProp.length - 1];
					}
					else {
						check = "";
					}
					if (check.equals(oldname)) {
						p.setProperty("verification.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties));
						p.store(out, properties);
					}
				}
				catch (Exception e) {
					// log.addText("verification");
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
			if (new File(properties1).exists()) {
				String check;
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties1));
					p.load(load);
					load.close();
					if (p.containsKey("synthesis.file")) {
						String[] getProp = p.getProperty("synthesis.file").split(separator);
						check = getProp[getProp.length - 1];
					}
					else {
						check = "";
					}
					if (check.equals(oldname)) {
						p.setProperty("synthesis.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties1));
						p.store(out, properties1);
					}
				}
				catch (Exception e) {
					// log.addText("synthesis");
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
			if (new File(properties2).exists()) {
				String check = "";
				try {
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("learn.file")) {
						String[] getProp = p.getProperty("learn.file").split(separator);
						check = getProp[getProp.length - 1];
					}
					else {
						check = "";
					}
					if (check.equals(oldname)) {
						p.setProperty("learn.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties2));
						p.store(out, properties2);
					}
				}
				catch (Exception e) {
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
		}
		updateAsyncViews(newname);
	}

	public void enableTabMenu(int selectedTab) {
		saveButton.setEnabled(false);
		saveasButton.setEnabled(false);
		runButton.setEnabled(false);
		refreshButton.setEnabled(false);
		checkButton.setEnabled(false);
		exportButton.setEnabled(false);
		save.setEnabled(false);
		saveAs.setEnabled(false);
		saveSBOL.setEnabled(false);
		saveModel.setEnabled(false);
		saveAll.setEnabled(false);
		close.setEnabled(false);
		closeAll.setEnabled(false);
		run.setEnabled(false);
		check.setEnabled(false);
		exportMenu.setEnabled(false);
		exportSBML.setEnabled(false);
		exportFlatSBML.setEnabled(false);
		exportSBOL.setEnabled(false);
		exportDataMenu.setEnabled(false);
		exportImageMenu.setEnabled(false);
		exportMovieMenu.setEnabled(false);
		exportCsv.setEnabled(false);
		exportDat.setEnabled(false);
		exportEps.setEnabled(false);
		exportJpg.setEnabled(false);
		exportPdf.setEnabled(false);
		exportPng.setEnabled(false);
		exportSvg.setEnabled(false);
		exportTsd.setEnabled(false);
		exportAvi.setEnabled(false);
		exportMp4.setEnabled(false);
		saveAsVerilog.setEnabled(false);
		viewCircuit.setEnabled(false);
		viewSG.setEnabled(false);
		viewLog.setEnabled(false);
		viewCoverage.setEnabled(false);
		viewLearnedModel.setEnabled(false);
		refresh.setEnabled(false);
		select.setEnabled(false);
		cut.setEnabled(false);
		addCompartment.setEnabled(false);
		addSpecies.setEnabled(false);
		addReaction.setEnabled(false);
		addComponent.setEnabled(false);
		addPromoter.setEnabled(false);
		addVariable.setEnabled(false);
		addBoolean.setEnabled(false);
		addPlace.setEnabled(false);
		addTransition.setEnabled(false);
		addRule.setEnabled(false);
		addConstraint.setEnabled(false);
		addEvent.setEnabled(false);
		addSelfInfl.setEnabled(false);
		moveLeft.setEnabled(false);
		moveRight.setEnabled(false);
		moveUp.setEnabled(false);
		moveDown.setEnabled(false);
		undo.setEnabled(false);
		redo.setEnabled(false);
		if (selectedTab != -1) {
			tab.setSelectedIndex(selectedTab);
		}
		Component comp = tab.getSelectedComponent();
		if (comp instanceof ModelEditor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			checkButton.setEnabled(true);
			exportButton.setEnabled(true);
			save.setEnabled(true);
			saveAs.setEnabled(true);
			saveSBOL.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			check.setEnabled(true);
			select.setEnabled(true);
			cut.setEnabled(true);
			if (!((ModelEditor) comp).isGridEditor()) {
				addCompartment.setEnabled(true);
				addSpecies.setEnabled(true);
				addReaction.setEnabled(true);
				addPromoter.setEnabled(true);
				addVariable.setEnabled(true);
				addBoolean.setEnabled(true);
				addPlace.setEnabled(true);
				addTransition.setEnabled(true);
				addRule.setEnabled(true);
				addConstraint.setEnabled(true);
				addEvent.setEnabled(true);
				addSelfInfl.setEnabled(true);
				moveLeft.setEnabled(true);
				moveRight.setEnabled(true);
				moveUp.setEnabled(true);
				moveDown.setEnabled(true);
			}
			addComponent.setEnabled(true);
			undo.setEnabled(true);
			redo.setEnabled(true);
			exportMenu.setEnabled(true);
			exportSBML.setEnabled(true);
			exportFlatSBML.setEnabled(true);
			exportSBOL.setEnabled(true);
			exportImageMenu.setEnabled(true);
			exportJpg.setEnabled(true);
		}
		else if (comp instanceof SBOLBrowser) {
			// save.setEnabled(true);
		}
		else if (comp instanceof LHPNEditor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			save.setEnabled(true);
			saveAs.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			viewCircuit.setEnabled(true);
			exportMenu.setEnabled(true);
			exportSBML.setEnabled(true);
			exportFlatSBML.setEnabled(true);
		}
		/*
		else if (comp instanceof SBML_Editor) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			checkButton.setEnabled(true);
			save.setEnabled(true);
			saveAs.setEnabled(true);
			check.setEnabled(true);
		}
		*/
		else if (comp instanceof Graph) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			refreshButton.setEnabled(true);
			exportButton.setEnabled(true);
			save.setEnabled(true);
			saveAs.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			refresh.setEnabled(true);
			exportMenu.setEnabled(true);
			exportImageMenu.setEnabled(true);
			if (((Graph) comp).isTSDGraph()) {
				exportDataMenu.setEnabled(true);
				exportCsv.setEnabled(true);
				exportDat.setEnabled(true);
				exportTsd.setEnabled(true);
			}
//			else {
//				exportCsv.setEnabled(false);
//				exportDat.setEnabled(false);
//				exportTsd.setEnabled(false);
//			}
			exportEps.setEnabled(true);
			exportJpg.setEnabled(true);
			exportPdf.setEnabled(true);
			exportPng.setEnabled(true);
			exportSvg.setEnabled(true);
		}
		else if (comp instanceof JTabbedPane) {
			Component component = ((JTabbedPane) comp).getSelectedComponent();
			Component learnComponent = null;			
			Boolean learn = false;
			Boolean learnLHPN = false;
			for (String s : new File(root + separator + getTitleAt(tab.getSelectedIndex())).list()) {
				if (s.contains("_sg.dot")) {
					viewSG.setEnabled(true);
				}
			}
			for (Component c : ((JTabbedPane) comp).getComponents()) {
				if (c instanceof LearnGCM) {
					learn = true;
					learnComponent = c;
				}
				else if (c instanceof LearnLHPN) {
					learnLHPN = true;
					learnComponent = c;
				}
			}
			if (component instanceof Graph) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				refreshButton.setEnabled(true);
				exportButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				if (learn && learnComponent != null) {
					if (new File(root + separator + getTitleAt(tab.getSelectedIndex()) + separator + "method.gcm").exists()) {
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					saveAsVerilog.setEnabled(false);
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null) {
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					saveAsVerilog.setEnabled(false);
					viewLearnedModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLHPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLHPN) learnComponent).getViewCoverageEnabled());
				}
				saveAs.setEnabled(true);
				refresh.setEnabled(true);
				exportMenu.setEnabled(true);
				exportImageMenu.setEnabled(true);
				if (((Graph) component).isTSDGraph()) {
					exportDataMenu.setEnabled(true);
					exportCsv.setEnabled(true);
					exportDat.setEnabled(true);
					exportTsd.setEnabled(true);
				}
//				else {
//					exportCsv.setEnabled(false);
//					exportDat.setEnabled(false);
//					exportTsd.setEnabled(false);
//				}
				exportEps.setEnabled(true);
				exportJpg.setEnabled(true);
				exportPdf.setEnabled(true);
				exportPng.setEnabled(true);
				exportSvg.setEnabled(true);
			}
			else if (component instanceof AnalysisView) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
			/*
			else if (component instanceof SBML_Editor) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				run.setEnabled(true);				
			}
			*/
			else if (component instanceof MovieContainer) {
				exportMenu.setEnabled(true);
				exportMovieMenu.setEnabled(true);
				exportAvi.setEnabled(true);
				exportMp4.setEnabled(true);
				exportImageMenu.setEnabled(true);
				exportJpg.setEnabled(true);
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
			else if (component instanceof ModelEditor) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
			else if (component instanceof LearnGCM) {
				if (new File(root + separator + getTitleAt(tab.getSelectedIndex()) + separator + "method.gcm").exists()) {
					viewLearnedModel.setEnabled(true);
				}
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewCircuit.setEnabled(((LearnGCM) component).getViewModelEnabled());
				viewLog.setEnabled(((LearnGCM) component).getViewLogEnabled());
				saveModel.setEnabled(((LearnGCM) component).getViewModelEnabled());
			}
			else if (component instanceof LearnLHPN) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewLearnedModel.setEnabled(((LearnLHPN) component).getViewLhpnEnabled());
				viewCircuit.setEnabled(((LearnLHPN) component).getViewLhpnEnabled());
				viewLog.setEnabled(((LearnLHPN) component).getViewLogEnabled());
				viewCoverage.setEnabled(((LearnLHPN) component).getViewCoverageEnabled());
				saveModel.setEnabled(((LearnLHPN) component).getViewLhpnEnabled());
			}
			else if (component instanceof DataManager) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAs.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				if (learn && learnComponent != null) {
					if (new File(root + separator + getTitleAt(tab.getSelectedIndex()) + separator + "method.gcm").exists()) {
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null) {
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewLearnedModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLHPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLHPN) learnComponent).getViewCoverageEnabled());
				}
			}
			else if (component instanceof JPanel) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				if (learn && learnComponent != null) {
					if (new File(root + separator + getTitleAt(tab.getSelectedIndex()) + separator + "method.gcm").exists()) {
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null) {
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewLearnedModel.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLHPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLHPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLHPN) learnComponent).getViewCoverageEnabled());
				}
			}
			else if (component instanceof JScrollPane) {
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
		}
		else if (comp instanceof JPanel) {
			if (comp.getName().equals("Verification")) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewTrace.setEnabled(((Verification) comp).getViewTraceEnabled());
				viewLog.setEnabled(((Verification) comp).getViewLogEnabled());
			}
			else if (comp.getName().equals("Synthesis")) {
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewRules.setEnabled(true/*((Synthesis) comp).getViewRulesEnabled()*/);
				viewTrace.setEnabled(true/*((Synthesis) comp).getViewTraceEnabled()*/);
				viewCircuit.setEnabled(true/*((Synthesis) comp).getViewCircuitEnabled()*/);
				viewLog.setEnabled(true/*((Synthesis) comp).getViewLogEnabled()*/);
			}
		}
		else if (comp instanceof JScrollPane) {
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			save.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			saveAs.setEnabled(true);
		}
	}

	private void enableTreeMenu() {
		viewModGraph.setEnabled(false);
		viewModBrowser.setEnabled(false);
		createAnal.setEnabled(false);
		createSynth.setEnabled(false);
		createLearn.setEnabled(false);
		createVer.setEnabled(false);
		createSbml.setEnabled(false);
		check.setEnabled(false);
		copy.setEnabled(false);
		rename.setEnabled(false);
		delete.setEnabled(false);
		viewModel.setEnabled(false);
		viewRules.setEnabled(false);
		viewTrace.setEnabled(false);
		viewCircuit.setEnabled(false);
		viewLHPN.setEnabled(false);
		saveAsVerilog.setEnabled(false);
		if (tree.getFile() != null) {
			if (tree.getFile().equals(root)) {
			}
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml")
					|| tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".xml")) {
				viewModGraph.setEnabled(true);
				viewModGraph.setActionCommand("graph");
				viewModBrowser.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("simulate");
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createLearn.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewModel.setEnabled(true);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm")) {
				viewModGraph.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createSim");
				createLearn.setEnabled(true);
				createSbml.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewModel.setEnabled(true);
			}
			else if (tree.getFile().length() > 3
					&& tree.getFile().substring(tree.getFile().length() - 4)
							.equals(".grf")) {
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().length() > 4
					&& tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol")) {
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams")) {
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().length() > 2 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv")) {
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().length() > 1 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g")) {
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn")) {
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createAnalysis");
				if (lema) {
					createLearn.setEnabled(true);
				}
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
				saveAsVerilog.setEnabled(true);
			}
			else if (tree.getFile().length() > 3
					&& (tree.getFile().substring(tree.getFile().length() - 4).equals(".hse")
							|| tree.getFile().substring(tree.getFile().length() - 4).equals(".unc")
							|| tree.getFile().substring(tree.getFile().length() - 4).equals(".csp")
							|| tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd") || tree.getFile()
							.substring(tree.getFile().length() - 4).equals(".rsg"))) {
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (tree.getFile().length() > 1 && tree.getFile().substring(tree.getFile().length() - 2).equals(".s") || tree.getFile().length() > 4
					&& tree.getFile().substring(tree.getFile().length() - 5).equals(".inst")) {
				createAnal.setEnabled(true);
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (new File(tree.getFile()).isDirectory()) {
				boolean sim = false;
				boolean synth = false;
				boolean ver = false;
				boolean learn = false;
				for (String s : new File(tree.getFile()).list()) {
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim")) {
						sim = true;
					}
					else if ((s.length() > 4 && s.substring(s.length() - 4).equals(".syn")) || 
							(s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION))) {
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
					copy.setEnabled(true);
					rename.setEnabled(true);
					delete.setEnabled(true);
				}
			}
		}
	}

	public String getRoot() {
		return root;
	}

	public static boolean checkFiles(String input, String output) {
		input = input.replaceAll("//", "/");
		output = output.replaceAll("//", "/");
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			if (input.toLowerCase().equals(output.toLowerCase())) {
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(frame, "Files are the same.", "Files Same", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
				return false;
			}
		}
		else {
			if (input.equals(output)) {
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(frame, "Files are the same.", "Files Same", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
				return false;
			}
		}
		return true;
	}

	public boolean overwrite(String fullPath, String name) {
		if (new File(fullPath).exists()) {
			String[] views = canDelete(name);
			Object[] options = { "Overwrite", "Cancel" };
			int value;
			if (views.length == 0) {
				value = JOptionPane.showOptionDialog(frame, name + " already exists." + "\nDo you want to overwrite?", "Overwrite",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
			else {
				String view = "";
				String gcms = "";
				for (int i = 0; i < views.length; i++) {
					if (views[i].endsWith(".gcm")) {
						gcms += views[i] + "\n";
					}
					else {
						view += views[i] + "\n";
					}
				}
				String message;
				if (gcms.equals("")) {
					message = "The file, " + name + ", already exists, and\nit is linked to the following views:\n\n" + view
							+ "\n\nAre you sure you want to overwrite?";
				}
				else if (view.equals("")) {
					message = "The file, " + name + ", already exists, and\nit is linked to the following gcms:\n\n" + gcms
							+ "\n\nAre you sure you want to overwrite?";
				}
				else {
					message = "The file, " + name + ", already exists, and\nit is linked to the following views:\n\n" + views
							+ "\n\nand\nit is linked to the following gcms:\n\n" + gcms + "\n\nAre you sure you want to overwrite?";
				}

				JTextArea messageArea = new JTextArea(message);
				messageArea.setEditable(false);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(300, 300));
				scroll.setPreferredSize(new Dimension(300, 300));
				scroll.setViewportView(messageArea);
				value = JOptionPane.showOptionDialog(frame, scroll, "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				//value = JOptionPane.NO_OPTION;
			}
			if (value == JOptionPane.YES_OPTION) {
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (getTitleAt(i).equals(name)) {
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
			return false;
		}
		return true;
	}

	/*
	public boolean updateOpenSBML(String sbmlName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			if (sbmlName.equals(tab)) {
				if (this.tab.getComponentAt(i) instanceof SBML_Editor) {
					SBML_Editor newSBML = new SBML_Editor(root + separator + sbmlName, null, log, this, null, null);
					this.tab.setComponentAt(i, newSBML);
					this.tab.getComponentAt(i).setName("SBML Editor");
					newSBML.save(false, "", false, true);
					return true;
				}
			}
		}
		return false;
	}
	*/

	public void updateTabName(String oldName, String newName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			if (oldName.equals(tab)) {
				this.tab.setTitleAt(i, newName);
			}
		}
	}

	public boolean updateOpenLHPN(String lhpnName) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			String tab = this.getTitleAt(i);
			if (lhpnName.equals(tab)) {
				if (this.tab.getComponentAt(i) instanceof LHPNEditor) {
					LHPNEditor newLHPN = new LHPNEditor(root, lhpnName, null, this);
					this.tab.setComponentAt(i, newLHPN);
					this.tab.getComponentAt(i).setName("LHPN Editor");
					return true;
				}
			}
		}
		return false;
	}

	private String[] canDelete(String filename) {
		ArrayList<String> views = new ArrayList<String>();
		String[] files = new File(root).list();
		for (String s : files) {
			if (new File(root + separator + s).isDirectory()) {
				String check = "";
				if (new File(root + separator + s + separator + s + ".sim").exists()) {
					try {
						Scanner scan = new Scanner(new File(root + separator + s + separator + s + ".sim"));
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
						FileInputStream load = new FileInputStream(new File(root + separator + s + separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file")) {
							String[] getProp = p.getProperty("genenet.file").split(separator);
							check = getProp[getProp.length - 1];
						}
						else if (p.containsKey("learn.file")) {
							String[] getProp = p.getProperty("learn.file").split(separator);
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
				else if (new File(root + separator + s + separator + s + ".ver").exists()) {
					try {
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + separator + s + separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("verification.file")) {
							String[] getProp = p.getProperty("verification.file").split(separator);
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
				else if (new File(root + separator + s + separator + s + ".synth").exists()) {
					try {
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + separator + s + separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("synthesis.file")) {
							String[] getProp = p.getProperty("synthesis.file").split(separator);
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
				else if (new File(root + separator + s + separator + s + ".sbolsynth.properties").exists()) {
					Properties synthProps = SBOLUtility.loadSBOLSynthesisProperties(root + separator + s, separator, Gui.frame);
					if (synthProps != null)
						if (synthProps.containsKey(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY))
							check = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY);
						else
							JOptionPane.showMessageDialog(frame, "Synthesis specification property is missing.", "Missing Property", JOptionPane.ERROR_MESSAGE);
					else
						check = "";
				}
				check = check.replace(".gcm",".xml");
				if (check.equals(filename)) {
					views.add(s);
				}
			}
			else if (s.endsWith(".xml") && filename.endsWith(".xml")) {
				BioModel gcm = new BioModel(root);
				try {
					gcm.load(root + separator + s);
					if (gcm.getSBMLComp()!=null) {
						for (int i = 0; i < gcm.getSBMLComp().getListOfExternalModelDefinitions().size(); i++) {
							ExternalModelDefinition extModel = gcm.getSBMLComp().getListOfExternalModelDefinitions().get(i);
							if (extModel.getSource().equals("file:"+ filename)) {
								views.add(s);
								break;
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}
		String[] usingViews = views.toArray(new String[0]);
		sort(usingViews);
		return usingViews;
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

	private void reassignViews(String oldName, String newName) {
		String[] files = new File(root).list();
		for (String s : files) {
			if (new File(root + separator + s).isDirectory()) {
				String check = "";
				if (new File(root + separator + s + separator + s + ".sim").exists()) {
					try {
						ArrayList<String> copy = new ArrayList<String>();
						Scanner scan = new Scanner(new File(root + separator + s + separator + s + ".sim"));
						if (scan.hasNextLine()) {
							check = scan.nextLine();
							check = check.split(separator)[check.split(separator).length - 1];
							if (check.equals(oldName)) {
								while (scan.hasNextLine()) {
									copy.add(scan.nextLine());
								}
								scan.close();
								FileOutputStream out = new FileOutputStream(new File(root + separator + s + separator + s + ".sim"));
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
						FileInputStream load = new FileInputStream(new File(root + separator + s + separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file")) {
							String[] getProp = p.getProperty("genenet.file").split(separator);
							check = getProp[getProp.length - 1];
							if (check.equals(oldName)) {
								p.setProperty("genenet.file", newName);
								FileOutputStream store = new FileOutputStream(new File(root + separator + s + separator + s + ".lrn"));
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

	protected JButton makeToolButton(String imageName, String actionCommand, String toolTipText) {
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(new ImageIcon(imageName));
		return button;
	}
	
	private String changeIdToPortRef(SBaseRef sbaseRef,BioModel bioModel) {
		String id = "";
		if (sbaseRef.isSetSBaseRef()) {
			BioModel subModel = new BioModel(root);
			Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(sbaseRef.getIdRef());
			String extModel = bioModel.getSBMLComp().getListOfExternalModelDefinitions().get(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(root + separator + extModel);
			id += changeIdToPortRef(sbaseRef.getSBaseRef(),subModel);
			subModel.save(root + separator + extModel);
		}
		if (sbaseRef.isSetIdRef()) {
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), sbaseRef.getIdRef());
			if (sbase!=null) {
				if (id.equals("")) {
					id = sbase.getElementName() + "__" + sbaseRef.getIdRef();
				} else {
					id = id + "__" + sbaseRef.getIdRef();
				}
				if (port == null) {
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(id);
					port.setIdRef(sbaseRef.getIdRef());
					port.setSBaseRef(sbaseRef.getSBaseRef());
				} 
				sbaseRef.unsetIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
			return "";
		} 
		if (sbaseRef.isSetMetaIdRef()) {
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), sbaseRef.getMetaIdRef());
			if (id.equals("")) {
				id = sbase.getElementName() + "__" + sbaseRef.getMetaIdRef();
			} else {
				id = id + "__" + sbaseRef.getMetaIdRef();
			}
			if (sbase!=null) { 
				if (port == null) {
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(id);
					port.setMetaIdRef(sbaseRef.getMetaIdRef());
					port.setSBaseRef(sbaseRef.getSBaseRef());
				}
				sbaseRef.unsetMetaIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
		} 
		return "";
	}
	
	private boolean updatePortMap(CompSBasePlugin sbmlSBase,BioModel subModel,String subModelId) {
		boolean updated = false;
		if (sbmlSBase.isSetListOfReplacedElements()) {
			for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
				if (replacement.getSubmodelRef().equals(subModelId)) {
					changeIdToPortRef(replacement,subModel);
					updated = true;
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId)) {
				changeIdToPortRef(replacement,subModel);
				updated = true;
			}
		}
		return updated;
	}
	
	private boolean updateReplacementsDeletions(SBMLDocument document, CompSBMLDocumentPlugin sbmlComp, 
			CompModelPlugin sbmlCompModel) {
		for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
			BioModel subModel = new BioModel(root);
			Submodel submodel = sbmlCompModel.getListOfSubmodels().get(i);
			String extModel = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(root + separator + extModel);
			ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(document.getModel());
			for (int j = 0; j < elements.size(); j++) {
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
				if (sbmlSBase!=null) {
					if (updatePortMap(sbmlSBase,subModel,submodel.getId())) {
						elements = SBMLutilities.getListOfAllElements(document.getModel());
					}
				}
			}
			for (int j = 0; j < submodel.getListOfDeletions().size(); j++) {
				Deletion deletion = submodel.getListOfDeletions().get(j);
				changeIdToPortRef(deletion,subModel);
			}
			subModel.save(root + separator + extModel);
		}
		
		return true;
	}

	private boolean extractModelDefinitions(CompSBMLDocumentPlugin sbmlComp,CompModelPlugin sbmlCompModel) {
		for (int i=0; i < sbmlComp.getListOfModelDefinitions().size(); i++) {
			ModelDefinition md = sbmlComp.getListOfModelDefinitions().get(i);
			String extId = md.getId();
			if (overwrite(root + separator + extId + ".xml",extId + ".xml")) {
				org.sbml.jsbml.Model model = new org.sbml.jsbml.Model(md);
				model.getDeclaredNamespaces().clear();
				SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
				document.enablePackage(LayoutConstants.namespaceURI);
				document.enablePackage(CompConstants.namespaceURI);
				document.enablePackage(FBCConstants.namespaceURI);
				// TODO: workaround
				document.getSBMLDocumentAttributes().put("fbc:required", "false");
				CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
				CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(model);
				document.setModel(model);
				ArrayList<String> comps = new ArrayList<String>();
				for (int j=0; j < documentCompModel.getListOfSubmodels().size(); j++) {
					String subModelType = documentCompModel.getListOfSubmodels().get(j).getModelRef();
					if (!comps.contains(subModelType)) {
						ExternalModelDefinition extModel = documentComp.createExternalModelDefinition();
						extModel.setId(subModelType);
						extModel.setSource("file:" + subModelType + ".xml");
						comps.add(subModelType);
					}
				}
				// Make compartment enclosing
				if (document.getModel().getCompartmentCount()==0) {
					Compartment c = document.getModel().createCompartment();
					c.setId("default");
					c.setSize(1);
					c.setSpatialDimensions(3);
					c.setConstant(true);
				}
				updateReplacementsDeletions(document, documentComp, documentCompModel);
				SBMLWriter writer = new SBMLWriter();
				try {
					writer.writeSBMLToFile(document, root + separator + extId + ".xml");
				} catch (SBMLException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
				addToTree(extId+".xml");
				if (sbmlComp.getListOfExternalModelDefinitions().get(extId) == null) {
					for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
						Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
						if (submodel.getModelRef().equals(extId)) {
							ExternalModelDefinition extModel = sbmlComp.createExternalModelDefinition();
							extModel.setSource("file:" + extId + ".xml");
							extModel.setId(extId);
							break;
						}
					}
				}
			} else {
				return false;
			}
		}
		while (sbmlComp.getListOfModelDefinitions().size() > 0) {
			sbmlComp.removeModelDefinition(0);
		}
		for (int i = 0; i < sbmlComp.getListOfExternalModelDefinitions().size(); i++) {
			ExternalModelDefinition extModel = sbmlComp.getListOfExternalModelDefinitions().get(i);
			if (extModel.isSetModelRef()) {
				String oldId = extModel.getId();
				extModel.setSource("file:" + extModel.getModelRef() + ".xml");
				extModel.setId(extModel.getModelRef());
				extModel.unsetModelRef();
				for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
					Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
					if (submodel.getModelRef().equals(oldId)) {
						submodel.setModelRef(extModel.getId());
					}
				}
			}
		}
		return true;
	}
	
	static final String SFILELINE = "input (\\S+?)\n";
}
