package frontend.main;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;
import javax.xml.stream.XMLStreamException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.ArchiveComponents;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.DataSet;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Plot3D;
import org.jlibsedml.RepeatedTask;
import org.jlibsedml.Report;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.SedMLError;
import org.jlibsedml.Simulation;
import org.jlibsedml.Surface;
import org.jlibsedml.Task;
import org.jlibsedml.Variable;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.ArchiveModelResolver;
import org.jlibsedml.modelsupport.BioModelsModelsRetriever;
import org.jlibsedml.modelsupport.URLResourceRetriever;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse; 
import com.clarkparsia.sbol.editor.SBOLDesignerPlugin;

import backend.analysis.util.SEDMLutilities;
import backend.sbol.util.SBOLUtility2;
import backend.verification.platu.platuLpn.io.PlatuGrammarLexer;
import backend.verification.platu.platuLpn.io.PlatuGrammarParser;
import conversion.SBOL2SBML;
import dataModels.biomodel.annotation.AnnotationUtility;
import dataModels.biomodel.annotation.SBOLAnnotation;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.parser.GCM2SBML;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Lpn2verilog;
import dataModels.lpn.parser.Translator;
import dataModels.lpn.parser.properties.BuildProperty;
import dataModels.util.GlobalConstants;
import dataModels.util.Message;
import dataModels.util.exceptions.BioSimException;
import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import de.unirostock.sems.cbarchive.CombineArchive;
import frontend.analysis.AnalysisThread;
import frontend.analysis.AnalysisView;
import frontend.analysis.Run;
import frontend.biomodel.gui.movie.MovieContainer;
import frontend.biomodel.gui.sbmlcore.ElementsPanel;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.biomodel.gui.schematic.Utils;
import frontend.graph.Graph;
import frontend.learn.AMSModel.LearnLPN;
import frontend.learn.GCM.LearnGCM;
import frontend.learn.datamanager.DataManager;
import frontend.lpn.gui.LHPNEditor;
import frontend.main.util.EditPreferences;
import frontend.main.util.FileTree;
import frontend.main.util.Utility;
import frontend.main.util.tabs.CloseAndMaxTabbedPane;
import frontend.sbol.browser.SBOLBrowser2;
import frontend.synthesis.async.Synthesis;
import frontend.synthesis.genetic.SynthesisView;
import frontend.verification.AbstPane;
import frontend.verification.Verification;

/**
 * This class creates a GUI for the Tstubd program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * are selected.
 * 
 * @author Curtis Madsen
 */

public class Gui implements Observer, MouseListener, ActionListener, MouseMotionListener, MouseWheelListener
{

	public static JFrame			frame;	// Frame where components of the GUI are displayed
	private JMenuBar				menuBar;
	private JMenu					file, openRecent, edit, view, tools, help, importMenu, exportMenu, newMenu, viewModel;
	private JMenuItem				newProj,newSBMLModel,newSBOL,newGridModel,newVhdl,newS,newInst,newLhpn,newProperty,newG;
	private JMenuItem				newCsp,newHse,newUnc,newRsg,newSpice;
	private JMenuItem				exit;
	private JMenuItem				importSbol,importGenBank,importFasta;
	private JMenuItem				importSedml,importSbml,importBioModel,importVirtualPart,importVhdl;
	private JMenuItem				importS,importInst,importLpn,importG,importCsp,importHse,importUnc;	
	private JMenuItem				importRsg,importSpice,importProperty;
	private JMenuItem				manual;	
	private JMenuItem				bugReport;	
	private JMenuItem				about;	
	private JMenuItem				openProj;
	private JMenuItem				clearRecent;	
	private JMenuItem				pref;
	private JMenuItem				graph;
	private JMenuItem				probGraph, exportCsv, exportDat, exportEps, exportJpg, exportPdf;
	private JMenuItem				exportPng, exportSvg, exportTsd, exportSBML,exportFlatSBML;
	private JMenuItem				exportSBOL1, exportSBOL2, exportSynBioHub, exportGenBank, exportFasta, exportArchive, exportAvi, exportMp4;
	private JMenu					exportDataMenu, exportMovieMenu, exportImageMenu;
	private String					root;
	private String 					currentProjectId;
	private FileTree				tree;
	private CloseAndMaxTabbedPane	tab;
	private JToolBar				toolbar;
	private JButton					saveButton, runButton, refreshButton, saveasButton, checkButton, exportButton;			
	private JPanel					mainPanel;																				
	private JSplitPane				topSplit;
	private JSplitPane				mainSplit;

	public static String			reb2sacExecutable;
	
	public static String[]			envp;
	
	public static String			geneNetExecutable;

	public Log						log;																					// the
																															// log

	private JPopupMenu				popup;																					// popup
																															// menu

	private KeyEventDispatcher		dispatcher;

	private JMenuItem				recentProjects[];

	private String					recentProjectPaths[];

	private int						numberRecentProj;

	private int						ShortCutKey;

	public static String			SBMLLevelVersion;

	private Pattern					IDpat				= Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");

	private boolean					async;
	// treeSelected
	// = false;

	public boolean					atacs, lema, lpn;

	private String					viewer;

	private boolean					runGetNames;

//	private boolean					showParts			= false;

//	private Thread					getPartsThread		= null;

	private String[]				BioModelIds			= null;

//	private Parts					allVirtualParts		= null;

	private JMenuItem				addCompartment, addSpecies, addReaction, addModule, addPromoter, addVariable, addBoolean, addPlace,
			addTransition, addRule, addConstraint, addEvent, addSelfInfl, cut, select, undo, redo, copy, rename, delete, moveLeft, moveRight, moveUp,
			moveDown;

	private JMenuItem				save, saveAs, /*saveSBOL,*/ check, run, refresh, viewCircuit, viewRules, viewTrace, viewLog, viewCoverage, viewLHPN,
			saveModel, saveAsVerilog, viewSG, viewModGraph, viewLearnedModel, viewModBrowser, createAnal, createLearn, createSbml, createSynth, createMapTech,
			createVer, close, closeAll, saveAll, convertToLPN;

	public String					ENVVAR;

	public static final Object[]	OPTIONS				= { "Yes", "No", "Yes To All", "No To All", "Cancel" };

	public static final int			YES_OPTION			= JOptionPane.YES_OPTION;

	public static final int			NO_OPTION			= JOptionPane.NO_OPTION;

	public static final int			YES_TO_ALL_OPTION	= JOptionPane.CANCEL_OPTION;

	public static final int			NO_TO_ALL_OPTION	= 3;

	public static final int			CANCEL_OPTION		= 4;

	public static Object			ICON_EXPAND			= UIManager.get("Tree.expandedIcon");

	public static Object			ICON_COLLAPSE		= UIManager.get("Tree.collapsedIcon");

	private static final String		lemaVersion			= "2.9.5";

	private static final String		atacsVersion		= "6.1";

	private static final String		iBioSimVersion		= "2.9.5";	
	
	private SEDMLDocument 			sedmlDocument		= null;
	
	private SBOLDocument			sbolDocument		= null;
	
	public void OSXSetup() {
		Application app = Application.getApplication();

		app.setAboutHandler(new AboutHandler() {
		    public void handleAbout(AboutEvent ae) {
				about();
		    }
		});

		app.setPreferencesHandler(new PreferencesHandler() {
		    public void handlePreferences(PreferencesEvent pe) {
				EditPreferences editPreferences = new EditPreferences(frame, async, tree);
				editPreferences.preferences();
				if (sbolDocument!=null) {
					Preferences biosimrc = Preferences.userRoot();
					sbolDocument.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
				}
		    }
		});
		
		app.setQuitHandler(new QuitHandler() {
			public void handleQuitRequestWith(QuitEvent event, QuitResponse response)  {
		    	exit();
		    }
		});
   }

	/**
	 * This is the constructor for the Proj class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 * 
	 * @throws Exception
	 */
	public Gui(boolean lema, boolean atacs, boolean libsbmlFound, boolean lpn)
	{
		this.lema = lema;
		this.atacs = atacs;
		this.lpn = lpn;
		SBMLutilities.libsbmlFound = libsbmlFound;
		async = lema || atacs;
		Thread.setDefaultUncaughtExceptionHandler(new Utility.UncaughtExceptionHandler());
		/*
		 * if (File.separator.equals("\\")) { separator = "\\\\"; } else {
		 * separator = File.separator; }
		 */
		if (atacs)
		{
			ENVVAR = System.getenv("ATACSGUI");
			System.setProperty("software.running", "ATACS Version " + atacsVersion);
		}
		else if (lema)
		{
			ENVVAR = System.getenv("LEMA");
			System.setProperty("software.running", "LEMA Version " + lemaVersion);
		}
		else
		{
			ENVVAR = System.getenv("BIOSIM");
			System.setProperty("software.running", "iBioSim Version " + iBioSimVersion);
		}

		// Creates a new frame
		if (lema)
		{
			frame = new JFrame("LEMA");
			frame.setIconImage(new ImageIcon(getClass().getResource("/icons/LEMA.png")).getImage());
		}
		else if (atacs)
		{
			frame = new JFrame("ATACS");
			frame.setIconImage(new ImageIcon(getClass().getResource("/icons/ATACS.png")).getImage());
		}
		else
		{
			frame = new JFrame("iBioSim");
			frame.setIconImage(new ImageIcon(getClass().getResource("/icons/iBioSim.png")).getImage());
		}

		// Makes it so that clicking the x in the corner closes the program
		WindowListener w = new WindowListener()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				exit.doClick();
			}

			@Override
			public void windowOpened(WindowEvent arg0)
			{
			}

			@Override
			public void windowClosed(WindowEvent arg0)
			{
			}

			@Override
			public void windowIconified(WindowEvent arg0)
			{
			}

			@Override
			public void windowDeiconified(WindowEvent arg0)
			{
			}

			@Override
			public void windowActivated(WindowEvent arg0)
			{
			}

			@Override
			public void windowDeactivated(WindowEvent arg0)
			{
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
		String imgName = "/icons/save.png";
		saveButton = makeToolButton(imgName, "save", "Save");
		// toolButton = new JButton("Save");
		toolbar.add(saveButton);
		imgName = "/icons/saveas.png";
		saveasButton = makeToolButton(imgName, "saveas", "Save As");
		toolbar.add(saveasButton);
		imgName = "/icons/savecheck.png";
		checkButton = makeToolButton(imgName, "check", "Save and Check");
		toolbar.add(checkButton);
		imgName = "/icons/export.jpg";
		exportButton = makeToolButton(imgName, "export", "Export");
		toolbar.add(exportButton);
		imgName = "/icons/run-icon.jpg";
		runButton = makeToolButton(imgName, "run", "Save and Run");
		toolbar.add(runButton);
		imgName = "/icons/refresh.jpg";
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
		if (lema)
		{
			menuBar.add(view);
		}
		menuBar.add(tools);
		menuBar.add(help);
		select = new JMenuItem("Select Mode");
		cut = new JMenuItem("Delete");
		addCompartment = new JMenuItem("Add Compartment");
		addSpecies = new JMenuItem("Add Species");
		addReaction = new JMenuItem("Add Reaction");
		addModule = new JMenuItem("Add Module");
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
		newSBOL = new JMenuItem("Part");
		newGridModel = new JMenuItem("Grid Model");
		newSpice = new JMenuItem("Spice Circuit");
		newVhdl = new JMenuItem("VHDL Model");
		newS = new JMenuItem("Assembly File");
		newInst = new JMenuItem("Instruction File");
		newLhpn = new JMenuItem("LPN Model");
		newProperty = new JMenuItem("Property"); // DK
		newG = new JMenuItem("Petri Net");
		newCsp = new JMenuItem("CSP Model");
		newHse = new JMenuItem("Handshaking Expansion");
		newUnc = new JMenuItem("Extended Burst Mode Machine");
		newRsg = new JMenuItem("Reduced State Graph");
		graph = new JMenuItem("TSD Graph");
		probGraph = new JMenuItem("Histogram");
		importSbol = new JMenuItem("SBOL File");
		importGenBank = new JMenuItem("GenBank File");
		importFasta = new JMenuItem("Fasta File");
		importSedml = new JMenuItem("SED-ML File");
		importSbml = new JMenuItem("SBML Model");
		importBioModel = new JMenuItem("BioModel");
		importVirtualPart = new JMenuItem("Virtual Part");
		convertToLPN = new JMenuItem("Convert To LPN"); // convert
		// importDot = new JMenuItem("iBioSim Model");
		importG = new JMenuItem("Petri Net");
		importLpn = new JMenuItem("LPN Model");
		importVhdl = new JMenuItem("VHDL Model");
		importS = new JMenuItem("Assembly File");
		importInst = new JMenuItem("Instruction File");
		importProperty = new JMenuItem("Property File");
		importSpice = new JMenuItem("Spice Circuit");
		importCsp = new JMenuItem("CSP Model");
		importHse = new JMenuItem("Handshaking Expansion");
		importUnc = new JMenuItem("Extended Burst Mode Machine");
		importRsg = new JMenuItem("Reduced State Graph");
		exportSBML = new JMenuItem("SBML");
		exportFlatSBML = new JMenuItem("Flat SBML");
		exportSBOL1 = new JMenuItem("SBOL 1.1");
		exportSBOL2 = new JMenuItem("SBOL 2.0");
		exportSynBioHub = new JMenuItem("SynBioHub");
		exportGenBank = new JMenuItem("GenBank");
		exportFasta = new JMenuItem("Fasta");
		exportArchive = new JMenuItem("Archive");
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
		if (async)
		{
			saveModel = new JMenuItem("Save Learned LPN");
		}
		else
		{
			saveModel = new JMenuItem("Save Learned Model");
		}
		saveAsVerilog = new JMenuItem("Save as Verilog");
		saveAsVerilog.addActionListener(this);
		saveAsVerilog.setActionCommand("saveAsVerilog");
		saveAsVerilog.setEnabled(false);
		saveAs = new JMenuItem("Save As");
		run = new JMenuItem("Save and Run");
		check = new JMenuItem("Save and Check");
		//saveSBOL = new JMenuItem("Save SBOL");
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
		createMapTech = new JMenuItem("Mapping Technology Tool");
		createVer = new JMenuItem("Verification Tool");
		exit = new JMenuItem("Exit");
		select.addActionListener(this);
		cut.addActionListener(this);
		addCompartment.addActionListener(this);
		addSpecies.addActionListener(this);
		addReaction.addActionListener(this);
		addModule.addActionListener(this);
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
		newSBOL.addActionListener(this);
		newGridModel.addActionListener(this);
		newVhdl.addActionListener(this);
		newS.addActionListener(this);
		newInst.addActionListener(this);
		newLhpn.addActionListener(this);
		newProperty.addActionListener(this); // DK
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
		importGenBank.addActionListener(this);
		importFasta.addActionListener(this);
		importSedml.addActionListener(this);
		importSbml.addActionListener(this);
		importBioModel.addActionListener(this);
		importVirtualPart.addActionListener(this);
		// importDot.addActionListener(this);
		importVhdl.addActionListener(this);
		importS.addActionListener(this);
		importInst.addActionListener(this);
		importProperty.addActionListener(this);
		importLpn.addActionListener(this);
		importG.addActionListener(this);
		importCsp.addActionListener(this);
		importHse.addActionListener(this);
		importUnc.addActionListener(this);
		importRsg.addActionListener(this);
		importSpice.addActionListener(this);
		exportSBML.addActionListener(this);
		exportFlatSBML.addActionListener(this);
		exportSBOL1.addActionListener(this);
		exportSBOL2.addActionListener(this);
		exportSynBioHub.addActionListener(this);
		exportGenBank.addActionListener(this);
		exportFasta.addActionListener(this);
		exportArchive.addActionListener(this);
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
		//saveSBOL.addActionListener(this);
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
		createMapTech.addActionListener(this);
		createVer.addActionListener(this);
		save.setActionCommand("save");
		saveAs.setActionCommand("saveas");
		//saveSBOL.setActionCommand("saveSBOL");
		run.setActionCommand("run");
		check.setActionCommand("check");
		refresh.setActionCommand("refresh");
		if (atacs)
		{
			viewModGraph.setActionCommand("viewModel");
		}
		else
		{
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
		if (lema)
		{
			newSBMLModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
		}
		else
		{
			check.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ShortCutKey));
			//saveSBOL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ShortCutKey | InputEvent.ALT_MASK));
			refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
			newSBMLModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
			newSBOL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ShortCutKey | InputEvent.ALT_MASK));
			//newSBOL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ShortCutKey));
			newGridModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ShortCutKey | InputEvent.ALT_MASK));
			createAnal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ShortCutKey | InputEvent.SHIFT_MASK));
			createSynth.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ShortCutKey | InputEvent.SHIFT_MASK));
			createMapTech.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ShortCutKey | InputEvent.SHIFT_MASK));
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
		addModule.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
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
		importGenBank.setEnabled(false);
		importFasta.setEnabled(false);
		importSedml.setEnabled(false);
		importSbml.setEnabled(false);
		importBioModel.setEnabled(false);
		importVirtualPart.setEnabled(false);
		importVhdl.setEnabled(false);
		importS.setEnabled(false);
		importInst.setEnabled(false);
		importProperty.setEnabled(false);
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
		exportSBOL1.setEnabled(false);
		exportSBOL2.setEnabled(false);
		exportSynBioHub.setEnabled(false);
		exportGenBank.setEnabled(false);
		exportFasta.setEnabled(false);
		exportArchive.setEnabled(false);
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
		newSBOL.setEnabled(false);
		newGridModel.setEnabled(false);
		newVhdl.setEnabled(false);
		newS.setEnabled(false);
		newInst.setEnabled(false);
		newLhpn.setEnabled(false);
		newProperty.setEnabled(false); // DK
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
		//saveSBOL.setEnabled(false);
		run.setEnabled(false);
		check.setEnabled(false);
		refresh.setEnabled(false);
		cut.setEnabled(false);
		select.setEnabled(false);
		addCompartment.setEnabled(false);
		addSpecies.setEnabled(false);
		addReaction.setEnabled(false);
		addModule.setEnabled(false);
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
		createMapTech.setEnabled(false);
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
		if (!async)
		{
			edit.add(addCompartment);
			edit.add(addSpecies);
			edit.add(addPromoter);
			edit.add(addReaction);
		}
		edit.add(addModule);
		edit.add(addVariable);
		edit.add(addBoolean);
		edit.add(addPlace);
		edit.add(addTransition);
		edit.add(addRule);
		edit.add(addConstraint);
		if (!async)
		{
			edit.add(addEvent);
			edit.add(addSelfInfl);
		}
		edit.addSeparator();
		edit.add(copy);
		edit.add(rename);
		edit.add(delete);
		file.add(newMenu);
		newMenu.add(newProj);
		if (!async)
		{
			newMenu.add(newSBMLModel);
			newMenu.add(newGridModel);
			newMenu.add(newSBOL);
		}
		else if (lema)
		{
			newMenu.add(newSBMLModel);
		}
		newMenu.add(graph);
		newMenu.add(probGraph);
		if (atacs)
		{
			newMenu.add(newVhdl);
			newMenu.add(newG);
			newMenu.add(newLhpn);
			newMenu.add(newCsp);
			newMenu.add(newHse);
			newMenu.add(newUnc);
			newMenu.add(newRsg);
			newMenu.add(newProperty);
		}
		else if (lema)
		{
			newMenu.add(newVhdl);
			newMenu.add(newProperty);
			newMenu.add(newS);
			newMenu.add(newInst);
			if (lpn)
			{
				newMenu.add(newLhpn);
			}
			// newMenu.add(newSpice);
		}
		file.add(openProj);
		file.add(openRecent);
		// openMenu.add(openProj);
		file.addSeparator();
		file.add(close);
		file.add(closeAll);
		file.addSeparator();
		file.add(save);
		if (!async)
		{
			//file.add(saveSBOL);
			file.add(check);
		}
		file.add(run);
		file.add(saveAs);
		if (lema)
		{
			file.add(saveAsVerilog);
		}
		file.add(saveAll);
		if (!async)
		{
			file.addSeparator();
			file.add(refresh);
		}
		/*
		 * if (lema) { file.add(saveModel); }
		 */
		file.addSeparator();
		file.add(importMenu);
		if (!async)
		{
			// importMenu.add(importDot);
			importMenu.add(importSbml);
			importMenu.add(importBioModel);
			// TODO: Removed due to issues with JParts
			//importMenu.add(importVirtualPart);
			importMenu.add(importLpn);
			importMenu.add(importSbol);
			importMenu.add(importGenBank);
			importMenu.add(importFasta);
			importMenu.add(importSedml);
		}
		else if (atacs)
		{
			importMenu.add(importVhdl);
			importMenu.add(importG);
			importMenu.add(importLpn);
			importMenu.add(importCsp);
			importMenu.add(importHse);
			importMenu.add(importUnc);
			importMenu.add(importRsg);
		}
		else
		{
			importMenu.add(importVhdl);
			importMenu.add(importSbml);
			importMenu.add(importS);
			importMenu.add(importInst);
			importMenu.add(importProperty);
			importMenu.add(importLpn);
			// importMenu.add(importSpice);
		}
		file.add(exportMenu);
		exportMenu.add(exportDataMenu);
		exportMenu.add(exportImageMenu);
		exportMenu.add(exportMovieMenu);
		exportMenu.add(exportFlatSBML);
		exportMenu.add(exportSBML);
		exportMenu.add(exportSBOL1);
		exportMenu.add(exportSBOL2);
		exportMenu.add(exportSynBioHub);
		exportMenu.add(exportGenBank);
		exportMenu.add(exportFasta);
		// Removed for now since not working
		exportMenu.add(exportArchive);

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
		// file.addSeparator();
		help.add(manual);
		help.add(bugReport);
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
		{
			OSXSetup();
		}
		else
		{
			edit.addSeparator();
			edit.add(pref);
			file.add(exit);
			// file.addSeparator();
			help.add(about);
		}
		if (lema)
		{
			// view.add(viewVHDL);
			// view.add(viewVerilog);
			view.add(viewLHPN);
			view.addSeparator();
			view.add(viewLearnedModel);
			view.add(viewCoverage);
			view.add(viewLog);
			view.add(viewTrace);

		}
		else if (atacs)
		{
			view.add(viewModGraph);
			view.add(viewCircuit);
			view.add(viewRules);
			view.add(viewTrace);
			view.add(viewLog);
		}
		else
		{
			view.add(viewModGraph);
			// view.add(viewModBrowser);
			view.add(viewLearnedModel);
			view.add(viewSG);
			view.add(viewLog);
			// view.addSeparator();
			// view.add(refresh);
		}
		tools.add(createAnal);
		if (!atacs)
		{
			tools.add(createLearn);
		}
		if (!lema)
		{
			tools.add(createSynth);
		}
		if (!lema)
		{
			tools.add(createMapTech);
		}
		if (async)
		{
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
		for (int i = 0; i < 10; i++)
		{
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
		for (int i = 0; i < 10; i++)
		{
			if (atacs)
			{
				recentProjects[i].setText(biosimrc.get("atacs.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("atacs.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals(""))
				{
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				}
				else
				{
					break;
				}
			}
			else if (lema)
			{
				recentProjects[i].setText(biosimrc.get("lema.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("lema.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals(""))
				{
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				}
				else
				{
					break;
				}
			}
			else
			{
				recentProjects[i].setText(biosimrc.get("biosim.recent.project." + i, ""));
				recentProjectPaths[i] = biosimrc.get("biosim.recent.project.path." + i, "");
				if (!recentProjects[i].getText().trim().equals("") && !recentProjectPaths[i].trim().equals(""))
				{
					openRecent.add(recentProjects[i]);
					numberRecentProj = i + 1;
				}
				else
				{
					break;
				}
			}
		}
		openRecent.addSeparator();
		openRecent.add(clearRecent);
		SBMLLevelVersion = "L3V1";
		GlobalConstants.SBML_LEVEL = 3;
		GlobalConstants.SBML_VERSION = 1;

		// Packs the frame and displays it
		mainPanel = new JPanel(new BorderLayout());
		tree = new FileTree(null, this, lema, atacs, lpn);

		EditPreferences editPreferences = new EditPreferences(frame, async, tree);
		editPreferences.setDefaultPreferences();

		log = new Log();
		tab = new CloseAndMaxTabbedPane(false, this);
		tab.setPreferredSize(new Dimension(1100, 550));
		// tab.getPaneUI().addMouseListener(this);

		topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, tab);
		mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, log);

		// mainPanel.add(tree, "West");
		mainPanel.add(mainSplit, "Center");
		// mainPanel.add(log, "South");
		mainPanel.add(toolbar, "North");
		frame.setContentPane(mainPanel);
		frame.setJMenuBar(menuBar);
		frame.addMouseListener(this);
		menuBar.addMouseListener(this);
		frame.pack();
		Dimension screenSize;
		try
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe)
		{
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = frame.getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
			frame.setSize(frameSize);
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
			frame.setSize(frameSize);
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		frame.setLocation(x, y);
		frame.setVisible(true);
		dispatcher = new KeyEventDispatcher()
		{
			@Override
			public boolean dispatchKeyEvent(KeyEvent e)
			{
				if (e.getID() == KeyEvent.KEY_TYPED)
				{
					if (e.getKeyChar() == '')
					{
						if (tab.getTabCount() > 0)
						{
							KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
							if (save(tab.getSelectedIndex(), 0) != 0)
							{
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

	public String getTitleAt(int i)
	{
		return tab.getTitleAt(i).replace("*", "");
	}

	public static boolean getCheckUndeclared()
	{
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.check.undeclared", "").equals("false"))
		{
			return false;
		}
		return true;
	}

	public static boolean getCheckUnits()
	{
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.check.units", "").equals("false"))
		{
			return false;
		}
		return true;
	}

	private void about()
	{
		final JFrame f = new JFrame("About");
		JLabel name;
		JLabel version;
		final String developers;
		if (lema)
		{
			name = new JLabel("LEMA", SwingConstants.CENTER);
			version = new JLabel("Version " + lemaVersion, SwingConstants.CENTER);
			developers = "Satish Batchu\nAndrew Fisher\nKevin Jones\nDhanashree Kulkarni\nScott Little\nCurtis Madsen\nChris Myers\nNicholas Seegmiller\n"
					+ "Robert Thacker\nDavid Walter\nZhen Zhang";
		}
		else if (atacs)
		{
			name = new JLabel("ATACS", SwingConstants.CENTER);
			version = new JLabel("Version " + atacsVersion, SwingConstants.CENTER);
			developers = "Wendy Belluomini\nJeff Cuthbert\nHans Jacobson\nKevin Jones\nSung-Tae Jung\n"
					+ "Christopher Krieger\nScott Little\nCurtis Madsen\nEric Mercer\nChris Myers\n"
					+ "Curt Nelson\nEric Peskin\nNicholas Seegmiller\nDavid Walter\nHao Zheng";
		}
		else
		{
			name = new JLabel("iBioSim", SwingConstants.CENTER);
			version = new JLabel("Version " + iBioSimVersion, SwingConstants.CENTER);
			developers = "Nathan Barker\nScott Glass\nKevin Jones\nHiroyuki Kuwahara\n"
					+ "Curtis Madsen\nChris Myers\nNam Nguyen\nTramy Nguyen\nTyler Patterson\nNicholas Roehner\nJason Stevens\nLeandro Watanabe\nZhen Zhang";
		}
		Font font = name.getFont();
		font = font.deriveFont(Font.BOLD, 36.0f);
		name.setFont(font);
		JLabel uOfU = new JLabel("University of Utah", SwingConstants.CENTER);
		JButton credits = new JButton("Credits");
		credits.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object[] options = { "Close" };
				JOptionPane.showOptionDialog(f, developers, "Credits", JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		});
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
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
		if (lema)
		{
			aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(getClass().getResource("/icons/LEMA.png"))), "North");
		}
		else if (atacs)
		{
			aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(getClass().getResource("/icons/ATACS.png"))), "North");
		}
		else
		{
			aboutPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(getClass().getResource("/icons/iBioSim.png"))), "North");
		}
		// aboutPanel.add(bioSim, "North");
		aboutPanel.add(uOfUPanel, "Center");
		aboutPanel.add(buttons, "South");
		f.setContentPane(aboutPanel);
		f.pack();
		Dimension screenSize;
		try
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe)
		{
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = f.getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		f.setLocation(x, y);
		f.setVisible(true);
	}

	public boolean exit()
	{
		int autosave = 0;
		for (int i = 0; tab != null && i < tab.getTabCount(); i++)
		{
			int save = save(i, autosave);
			if (save == 0)
			{
				return false;
			}
			else if (save == 2)
			{
				autosave = 1;
			}
			else if (save == 3)
			{
				autosave = 2;
			}
		}
		Preferences biosimrc = Preferences.userRoot();
		for (int i = 0; i < numberRecentProj; i++)
		{
			if (atacs)
			{
				biosimrc.put("atacs.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("atacs.recent.project.path." + i, recentProjectPaths[i]);
			}
			else if (lema)
			{
				biosimrc.put("lema.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("lema.recent.project.path." + i, recentProjectPaths[i]);
			}
			else
			{
				biosimrc.put("biosim.recent.project." + i, recentProjects[i].getText());
				biosimrc.put("biosim.recent.project.path." + i, recentProjectPaths[i]);
			}
		}
		for (int i = numberRecentProj; i < 10; i++)
		{
			if (atacs)
			{
				biosimrc.put("atacs.recent.project." + i, "");
				biosimrc.put("atacs.recent.project.path." + i, "");
			}
			else if (lema)
			{
				biosimrc.put("lema.recent.project." + i, "");
				biosimrc.put("lema.recent.project.path." + i, "");
			}
			else
			{
				biosimrc.put("biosim.recent.project." + i, "");
				biosimrc.put("biosim.recent.project.path," + i, "");
			}
		}
		System.exit(1);
		return true;
	}
	
	public String getCurrentProjectId() {
		return currentProjectId; 
	}
	
	private void createProject(ActionEvent e) {
		int autosave = 0;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			int save = save(i, autosave);
			if (save == 0)
			{
				return;
			}
			else if (save == 2)
			{
				autosave = 1;
			}
			else if (save == 3)
			{
				autosave = 2;
			}
		}
		File file;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.project_dir", "").equals(""))
		{
			file = null;
		}
		else
		{
			file = new File(biosimrc.get("biosim.general.project_dir", ""));
		}
		String filename;

		if (e.getActionCommand().startsWith(GlobalConstants.SBOL_SYNTH_COMMAND))
		{
			filename = identifySBOLSynthesisPath(e.getActionCommand());
		}
		else
		{
			filename = Utility.browse(frame, file, null, JFileChooser.DIRECTORIES_ONLY, "New", -1);
		}
		if (!filename.trim().equals(""))
		{
			filename = filename.trim();
			biosimrc.put("biosim.general.project_dir", filename);
			File f = new File(filename);
			if (f.exists())
			{
				Object[] options = { "Overwrite", "Cancel" };
				int value = JOptionPane.showOptionDialog(frame, "File already exists." + "\nDo you want to overwrite?", "Overwrite",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (value == JOptionPane.YES_OPTION)
				{
					File dir = new File(filename);
					if (dir.isDirectory())
					{
						deleteDir(dir);
					}
					else
					{
						System.gc();
						dir.delete();
					}
				}
				else
				{
					return;
				}
			}
			new File(filename).mkdir();
			try
			{
				if (lema)
				{
					new FileWriter(new File(filename + GlobalConstants.separator + "LEMA.prj")).close();
				}
				else if (atacs)
				{
					new FileWriter(new File(filename + GlobalConstants.separator + "ATACS.prj")).close();
				}
				else
				{
					new FileWriter(new File(filename + GlobalConstants.separator + "BioSim.prj")).close();
				}
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to create a new project.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			root = filename;
			currentProjectId = root.split(GlobalConstants.separator)[root.split(GlobalConstants.separator).length-1];
			
			sedmlDocument = new SEDMLDocument(1,2);
			writeSEDMLDocument();
			
			sbolDocument = new SBOLDocument();
			sbolDocument.setCreateDefaults(true);
			sbolDocument.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
			writeSBOLDocument();		

			refresh();
			tab.removeAll();
			addRecentProject(filename);

			addToTree(currentProjectId+".sbol");

			// importDot.setEnabled(true);
			importMenu.setEnabled(true);
			importSbol.setEnabled(true);
			importGenBank.setEnabled(true);
			importFasta.setEnabled(true);
			importSedml.setEnabled(true);
			importSbml.setEnabled(true);
			importBioModel.setEnabled(true);
			importVirtualPart.setEnabled(true);
			importVhdl.setEnabled(true);
			importS.setEnabled(true);
			importInst.setEnabled(true);
			importProperty.setEnabled(true);
			importLpn.setEnabled(true);
			importG.setEnabled(true);
			importCsp.setEnabled(true);
			importHse.setEnabled(true);
			importUnc.setEnabled(true);
			importRsg.setEnabled(true);
			importSpice.setEnabled(true);
			newSBMLModel.setEnabled(true);
			newSBOL.setEnabled(true);
			newGridModel.setEnabled(true);
			newVhdl.setEnabled(true);
			newProperty.setEnabled(true); // DK
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

	private void openProject(ActionEvent e) {
		int autosave = 0;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			int save = save(i, autosave);
			if (save == 0)
			{
				return;
			}
			else if (save == 2)
			{
				autosave = 1;
			}
			else if (save == 3)
			{
				autosave = 2;
			}
		}
		Preferences biosimrc = Preferences.userRoot();
		String projDir = "";
		if (e.getSource() == openProj)
		{
			File file;
			if (biosimrc.get("biosim.general.project_dir", "").equals(""))
			{
				file = null;
			}
			else
			{
				file = new File(biosimrc.get("biosim.general.project_dir", ""));
			}
			projDir = Utility.browse(frame, file, null, JFileChooser.DIRECTORIES_ONLY, "Open", -1);
			if (projDir.endsWith(".prj"))
			{
				biosimrc.put("biosim.general.project_dir", projDir);
				String[] tempArray = projDir.split(GlobalConstants.separator);
				projDir = "";
				for (int i = 0; i < tempArray.length - 1; i++)
				{
					projDir = projDir + tempArray[i] + GlobalConstants.separator;
				}
			}
		}
		else if (e.getSource() == recentProjects[0])
		{
			projDir = recentProjectPaths[0];
		}
		else if (e.getSource() == recentProjects[1])
		{
			projDir = recentProjectPaths[1];
		}
		else if (e.getSource() == recentProjects[2])
		{
			projDir = recentProjectPaths[2];
		}
		else if (e.getSource() == recentProjects[3])
		{
			projDir = recentProjectPaths[3];
		}
		else if (e.getSource() == recentProjects[4])
		{
			projDir = recentProjectPaths[4];
		}
		else if (e.getSource() == recentProjects[5])
		{
			projDir = recentProjectPaths[5];
		}
		else if (e.getSource() == recentProjects[6])
		{
			projDir = recentProjectPaths[6];
		}
		else if (e.getSource() == recentProjects[7])
		{
			projDir = recentProjectPaths[7];
		}
		else if (e.getSource() == recentProjects[8])
		{
			projDir = recentProjectPaths[8];
		}
		else if (e.getSource() == recentProjects[9])
		{
			projDir = recentProjectPaths[9];
		}
		// log.addText(projDir);
		if (!projDir.equals(""))
		{
			biosimrc.put("biosim.general.project_dir", projDir);
			if (new File(projDir).isDirectory())
			{
				boolean isProject = false;
				for (String temp : new File(projDir).list())
				{
					if (temp.equals(".prj"))
					{
						isProject = true;
					}
					if (lema && temp.equals("LEMA.prj"))
					{
						isProject = true;
					}
					else if (atacs && temp.equals("ATACS.prj"))
					{
						isProject = true;
					}
					else if (temp.equals("BioSim.prj"))
					{
						isProject = true;
					}
				}
				if (isProject)
				{
					root = projDir;
					currentProjectId = root.split(GlobalConstants.separator)[root.split(GlobalConstants.separator).length-1];
					readSEDMLDocument();
					readSBOLDocument();
					refresh();
					addToTree(currentProjectId+".sbol");
					tab.removeAll();
					addRecentProject(projDir);
					
					// importDot.setEnabled(true);
					importMenu.setEnabled(true);
					importSbol.setEnabled(true);
					importGenBank.setEnabled(true);
					importFasta.setEnabled(true);
					importSedml.setEnabled(true);
					importSbml.setEnabled(true);
					importBioModel.setEnabled(true);
					importVirtualPart.setEnabled(true);
					importVhdl.setEnabled(true);
					importS.setEnabled(true);
					importInst.setEnabled(true);
					importProperty.setEnabled(true);
					importLpn.setEnabled(true);
					importG.setEnabled(true);
					importCsp.setEnabled(true);
					importHse.setEnabled(true);
					importUnc.setEnabled(true);
					importRsg.setEnabled(true);
					importSpice.setEnabled(true);
					newSBMLModel.setEnabled(true);
					newSBOL.setEnabled(true);
					newGridModel.setEnabled(true);
					newVhdl.setEnabled(true);
					newS.setEnabled(true);
					newInst.setEnabled(true);
					newLhpn.setEnabled(true);
					newProperty.setEnabled(true); // DK
					newG.setEnabled(true);
					newCsp.setEnabled(true);
					newHse.setEnabled(true);
					newUnc.setEnabled(true);
					newRsg.setEnabled(true);
					newSpice.setEnabled(true);
					graph.setEnabled(true);
					probGraph.setEnabled(true);
				}
				else
				{
					JOptionPane.showMessageDialog(frame, "You must select a valid project.", "Error", JOptionPane.ERROR_MESSAGE);
					removeRecentProject(projDir);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(frame, "You must select a valid project.", "Error", JOptionPane.ERROR_MESSAGE);
				removeRecentProject(projDir);
			}
		}
	}
	
	/**
	 * This method performs different functions depending on what menu items are
	 * selected.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == viewCircuit)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane)
			{
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof LearnGCM)
				{
					((LearnGCM) component).viewModel();
				}
				else if (component instanceof LearnLPN)
				{
					((LearnLPN) component).viewLPN();
				}
			}
			else if (comp instanceof LHPNEditor)
			{
				((LHPNEditor) comp).viewLhpn();
			}
			else if (comp instanceof JPanel)
			{
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Verification)
				{
					((Verification) array[0]).viewCircuit();
				}
				else if (array[0] instanceof Synthesis)
				{
					((Synthesis) array[0]).viewCircuit();
				}
			}
		}
		else if (e.getSource() == viewLog)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Verification)
			{
				((Verification) comp).viewLog();
			}
			else if (comp instanceof JPanel)
			{
				Component[] array = ((JPanel) comp).getComponents();
				if (array[0] instanceof Synthesis)
				{
					((Synthesis) array[0]).viewLog();
				}
			}
			else if (comp instanceof JTabbedPane)
			{
				for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++)
				{
					Component component = ((JTabbedPane) comp).getComponent(i);
					if (component instanceof LearnGCM)
					{
						((LearnGCM) component).viewLog();
						return;
					}
					else if (component instanceof LearnLPN)
					{
						((LearnLPN) component).viewLog();
						return;
					}
				}
			}
		}
		else if (e.getSource() == viewCoverage)
		{
			Component comp = tab.getSelectedComponent();
			for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++)
			{
				Component component = ((JTabbedPane) comp).getComponent(i);
				if (component instanceof LearnLPN)
				{
					((LearnLPN) component).viewCoverage();
					return;
				}
			}
		}
		else if (e.getSource() == saveModel)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane)
			{
				for (Component component : ((JTabbedPane) comp).getComponents())
				{
					if (component instanceof LearnGCM)
					{
						((LearnGCM) component).saveModel();
					}
					else if (component instanceof LearnLPN)
					{
						((LearnLPN) component).saveLPN();
					}
				}
			}
		}
		else if (e.getSource() == saveAsVerilog)
		{
			Lpn2verilog.convert(tree.getFile());
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".lpn", ".sv"));
		}
		else if (e.getSource() == close && tab.getSelectedComponent() != null)
		{
			Component comp = tab.getSelectedComponent();
			Point point = comp.getLocation();
			tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(), point.x, point.y, 0, false), tab.getSelectedIndex());
		}
		else if (e.getSource() == saveAll)
		{
			int autosave = 0;
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				int save = save(i, autosave);
				if (save == 0)
				{
					break;
				}
				else if (save == 2)
				{
					autosave = 1;
				}
				else if (save == 3)
				{
					autosave = 2;
				}
				markTabClean(i);
			}
		}
		else if (e.getSource() == closeAll)
		{
			while (tab.getSelectedComponent() != null)
			{
				int index = tab.getSelectedIndex();
				Component comp = tab.getComponent(index);
				Point point = comp.getLocation();
				tab.fireCloseTabEvent(new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(), point.x, point.y, 0, false), index);
			}
		}
		else if (e.getSource() == viewRules)
		{
			Component comp = tab.getSelectedComponent();
			Component[] array = ((JPanel) comp).getComponents();
			((Synthesis) array[0]).viewRules();
		}
		else if (e.getSource() == viewTrace)
		{
			Component comp = tab.getSelectedComponent();
			if (comp.getName().equals("Verification"))
			{
				Component[] array = ((JPanel) comp).getComponents();
				((Verification) array[0]).viewTrace();
			}
			else if (comp.getName().equals("Synthesis"))
			{
				Component[] array = ((JPanel) comp).getComponents();
				((Synthesis) array[0]).viewTrace();
			}
		}
		else if (e.getSource() == exportFlatSBML)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportFlatSBML();
			}
		}
		else if (e.getSource() == exportSBML)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBML();
			}
		}
		else if (e.getSource() == exportSBOL1)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBOL2("SBOL1");
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				exportSBOL((SBOLDesignerPlugin) comp,"SBOL1");
			}
		}
		else if (e.getSource() == exportSBOL2)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBOL2("SBOL");
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				exportSBOL((SBOLDesignerPlugin) comp,"SBOL");
			}
		}
		else if (e.getSource() == exportSynBioHub)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSynBioHub();
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				// TODO: change to upload for SBOLDesigner
				exportSBOL((SBOLDesignerPlugin) comp,"SBOL");
			}
		}
		else if (e.getSource() == exportGenBank)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBOL2("GenBank");
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				exportSBOL((SBOLDesignerPlugin) comp,"GenBank");
			}
		}
		else if (e.getSource() == exportFasta)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBOL2("Fasta");
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				exportSBOL((SBOLDesignerPlugin) comp,"Fasta");
			}
		}
		else if (e.getSource() == exportArchive)
		{
//			exportSEDML();
			exportCombineArchive();
		}
//		else if (e.getSource() == saveSBOL)
//		{
//			Component comp = tab.getSelectedComponent();
//			if (comp instanceof ModelEditor)
//			{	
//				log.addText("Converting SBML into SBOL and saving into the project's SBOL library.");
//				((ModelEditor) comp).saveAsSBOL2();
//			}
//		}
		else if (e.getSource() == exportCsv)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(5);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(5);
			}
		}
		else if (e.getSource() == exportDat)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(6);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export();
			}
		}
		else if (e.getSource() == exportEps)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(3);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(3);
			}
		}
		else if (e.getSource() == exportJpg)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(0);
			}
			else if (comp instanceof JTabbedPane)
			{

				if (((JTabbedPane) comp).getSelectedComponent().getName().equals("ModelViewMovie"))
				{
					((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputJPG(-1, false);
				}
				else
				{
					((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(0);
				}
			}
			else if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).saveSchematic();
			}
		}
		else if (e.getSource() == exportPdf)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(2);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(2);
			}
		}
		else if (e.getSource() == exportPng)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(1);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(1);
			}
		}
		else if (e.getSource() == exportSvg)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(4);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(4);
			}
		}
		else if (e.getSource() == exportTsd)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export(7);
			}
			else if (comp instanceof JTabbedPane)
			{
				((Graph) ((JTabbedPane) comp).getSelectedComponent()).export(7);
			}
		}
		else if (e.getSource() == exportAvi)
		{

			Component comp = tab.getSelectedComponent();

			if (comp instanceof JTabbedPane)
			{

				((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputMovie("avi");
			}
		}
		else if (e.getSource() == exportMp4)
		{

			Component comp = tab.getSelectedComponent();

			if (comp instanceof JTabbedPane)
			{

				((MovieContainer) ((JTabbedPane) comp).getSelectedComponent()).outputMovie("mp4");
			}
		}
		else if (e.getSource() == about)
		{
			about();
		}
		else if (e.getSource() == bugReport)
		{
			Utility.submitBugReport("");
		}
		else if (e.getSource() == manual)
		{
			try
			{
				String directory = "";
				String theFile = "";
				if (!async)
				{
					theFile = "iBioSim.html";
				}
				else if (atacs)
				{
					theFile = "ATACS.html";
				}
				else
				{
					theFile = "LEMA.html";
				}
				Preferences biosimrc = Preferences.userRoot();
				String command = biosimrc.get("biosim.general.browser", "");
				if (System.getProperty("os.name").contentEquals("Linux") || System.getProperty("os.name").toLowerCase().startsWith("mac os"))
				{
					directory = ENVVAR + "/docs/";
				}
				else
				{
					directory = ENVVAR + "\\docs\\";
				}
				File work = new File(directory);
				log.addText("Executing:\n" + command + " " + directory + theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				exec.exec(command + " " + theFile, null, work);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to open manual.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the exit menu item is selected
		else if (e.getSource() == exit)
		{
			exit();
		}
		// if the open popup menu is selected on a sim directory
		else if (e.getActionCommand().equals("openSim"))
		{
			try
			{
				openAnalysisView(tree.getFile());
			}
			catch (Exception e0)
			{
			}
		}
		else if (e.getActionCommand().equals("openLearn"))
		{
			if (lema)
			{
				openLearnLHPN();
			}
			else
			{
				openLearn();
			}
		}
		else if (e.getActionCommand().equals("openSynth"))
		{
			openSynth();
		}
		else if (e.getActionCommand().equals("openVerification"))
		{
			openVerify();
		}
		else if (e.getActionCommand().equals("convertToSBML"))
		{
			Translator t1 = new Translator();
			try {
        t1.convertLPN2SBML(tree.getFile(), "");
      } catch (BioSimException e1) {
        e1.printStackTrace();
      }
			t1.outputSBML();
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			// updateOpenSBML(theFile.replace(".lpn", ".xml"));
			addToTree(theFile.replace(".lpn", ".xml"));
		}
		else if (e.getActionCommand().equals("convertToVerilog"))
		{
			Lpn2verilog.convert(tree.getFile());
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".lpn", ".sv"));
		}

		else if (e.getActionCommand().equals("convertToLPN"))
		{
			// new BuildProperty();
			try
			{
				BuildProperty.buildProperty(tree.getFile());
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			catch (RecognitionException e1)
			{
				e1.printStackTrace();
			} catch (BioSimException e1) {
        e1.printStackTrace();
      }
			String theFile = "";
			if (tree.getFile().lastIndexOf('/') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('/') + 1);
			}
			if (tree.getFile().lastIndexOf('\\') >= 0)
			{
				theFile = tree.getFile().substring(tree.getFile().lastIndexOf('\\') + 1);
			}
			addToTree(theFile.replace(".prop", ".xml"));
		}
		else if (e.getActionCommand().equals("createAnalysis"))
		{
			try
			{
				createAnalysisView(tree.getFile());
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "You must select a valid model file for analysis.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the synthesis popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createSynthesis"))
		{
			if (root != null)
			{
				for (int i = 0; i < tab.getTabCount(); i++)
				{
					if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
					{
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0)
						{
							return;
						}
						break;
					}
				}
				if (!async)
				{
					createSBOLSynthesisView();
				}
				else
				{
					String synthName = JOptionPane.showInputDialog(frame, "Enter Synthesis ID:", "Synthesis View ID", JOptionPane.PLAIN_MESSAGE);
					if (synthName != null && !synthName.trim().equals(""))
					{
						synthName = synthName.trim();
						try
						{
							if (overwrite(root + GlobalConstants.separator + synthName, synthName))
							{
								new File(root + GlobalConstants.separator + synthName).mkdir();
								String sbmlFile = tree.getFile();
								String[] getFilename = sbmlFile.split(GlobalConstants.separator);
								String circuitFileNoPath = getFilename[getFilename.length - 1];
								try
								{
									FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + synthName.trim() + GlobalConstants.separator
											+ synthName.trim() + ".syn"));
									out.write(("synthesis.file=" + circuitFileNoPath + "\n").getBytes());
									out.close();
								}
								catch (IOException e1)
								{
									JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File",
											JOptionPane.ERROR_MESSAGE);
								}
								try
								{
									FileInputStream in = new FileInputStream(new File(root + GlobalConstants.separator + circuitFileNoPath));
									FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + synthName.trim() + GlobalConstants.separator
											+ circuitFileNoPath));
									int read = in.read();
									while (read != -1)
									{
										out.write(read);
										read = in.read();
									}
									in.close();
									out.close();
								}
								catch (Exception e1)
								{
									JOptionPane.showMessageDialog(frame, "Unable to copy circuit file!", "Error Saving File",
											JOptionPane.ERROR_MESSAGE);
								}
								addToTree(synthName.trim());
								String work = root + GlobalConstants.separator + synthName;
								String circuitFile = root + GlobalConstants.separator + synthName.trim() + GlobalConstants.separator + circuitFileNoPath;
								JPanel synthPane = new JPanel();
								Synthesis synth = new Synthesis(work, circuitFile, log, this);
								synthPane.add(synth);
								addTab(synthName, synthPane, "Synthesis");
							}
						}
						catch (Exception e1)
						{
							JOptionPane.showMessageDialog(frame, "Unable to create Synthesis View directory.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the verify popup menu is selected on a vhdl or lhpn file
		else if (e.getActionCommand().equals("createVerify"))
		{
			if (root != null)
			{
				for (int i = 0; i < tab.getTabCount(); i++)
				{
					if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
					{
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0)
						{
							return;
						}
						break;
					}
				}
				String verName = JOptionPane.showInputDialog(frame, "Enter Verification ID:", "Verification View ID", JOptionPane.PLAIN_MESSAGE);
				if (verName != null && !verName.trim().equals(""))
				{
					verName = verName.trim();
					// try {
					if (overwrite(root + GlobalConstants.separator + verName, verName))
					{
						new File(root + GlobalConstants.separator + verName).mkdir();
						String sbmlFile = tree.getFile();
						String[] getFilename = sbmlFile.split(GlobalConstants.separator);
						String circuitFileNoPath = getFilename[getFilename.length - 1];
						try
						{
							FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + verName.trim() + GlobalConstants.separator + verName.trim()
									+ ".ver"));
							out.write(("verification.file=" + circuitFileNoPath + "\n").getBytes());
							out.close();
						}
						catch (IOException e1)
						{
							JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
						}
						addToTree(verName.trim());
						Verification verify = new Verification(root + GlobalConstants.separator + verName, verName, circuitFileNoPath, log, this, lema, atacs);
						verify.save();
						addTab(verName, verify, "Verification");
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the delete popup menu is selected
		else if (e.getActionCommand().contains("delete") || e.getSource() == delete)
		{
			for (String file : tree.getFiles()) {
				delete(file);
			}
		}
		else if (e.getActionCommand().equals("openLPN"))
		{
			openLPN();
		}
		else if (e.getActionCommand().equals("browseSbol"))
		{
			openSBOL();
		}
//		else if (e.getActionCommand().equals("sbolToSBML"))
//		{
//			generateSBMLFromSBOL(tree.getFile());
//		}
		else if (e.getActionCommand().equals("SBOLDesigner"))
		{
			openSBOLDesigner();
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("modelEditor"))
		{
			openModelEditor(false);
		}
		// if the edit popup menu is selected on a dot file
		else if (e.getActionCommand().equals("modelTextEditor"))
		{
			openModelEditor(true);
		}
		// if the edit popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("sbmlEditor"))
		{
			openSBML(tree.getFile());
		}
		else if (e.getActionCommand().equals("stateGraph"))
		{
			try
			{
				String directory = root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex());
				File work = new File(directory);
				for (String f : new File(directory).list())
				{
					if (f.contains("_sg.dot"))
					{
						Runtime exec = Runtime.getRuntime();
						if (System.getProperty("os.name").contentEquals("Linux"))
						{
							log.addText("Executing:\ndotty " + directory + GlobalConstants.separator + f + "\n");
							exec.exec("dotty " + f, null, work);
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
						{
							log.addText("Executing:\nopen " + directory + GlobalConstants.separator + f + "\n");
							exec.exec("open " + f, null, work);
						}
						else
						{
							log.addText("Executing:\ndotty " + directory + GlobalConstants.separator + f + "\n");
							exec.exec("dotty " + f, null, work);
						}
						return;
					}
				}
				JOptionPane.showMessageDialog(frame, "State graph file has not been generated.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("graphTree"))
		{
			String directory = "";
			String theFile = "";
			String filename = tree.getFile();
			if (filename.lastIndexOf('/') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			File work = new File(directory);
			String out = theFile;
			try
			{
				if (out.contains(".lpn"))
				{
					String file = theFile;
					String[] findTheFile = file.split("\\.");
					theFile = findTheFile[0] + ".dot";
					File dot = new File(root + GlobalConstants.separator + theFile);
					dot.delete();
					LPN lhpn = new LPN();
					lhpn.addObserver(this);
					lhpn.load(directory + GlobalConstants.separator + theFile);
					lhpn.printDot(directory + GlobalConstants.separator + file);
					// String cmd = "atacs -cPllodpl " + file;
					Runtime exec = Runtime.getRuntime();
					// Process ATACS = exec.exec(cmd, null, work);
					// ATACS.waitFor();
					// log.addText("Executing:\n" + cmd);
					if (dot.exists())
					{
						Preferences biosimrc = Preferences.userRoot();
						String command = biosimrc.get("biosim.general.graphviz", "");
						log.addText(command + " " + root + GlobalConstants.separator + theFile + "\n");
						exec.exec(command + theFile, null, work);
					}
					else
					{
						File log = new File(root + GlobalConstants.separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null)
						{
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
				if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml"))
				{
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml"))
				{
					out = out.substring(0, out.length() - 4);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".gcm"))
				{
					try
					{
						if (System.getProperty("os.name").contentEquals("Linux"))
						{
							log.addText("Executing:\ndotty " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("dotty " + theFile, null, work);
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
						{
							log.addText("Executing:\nopen " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("cp " + theFile + " " + theFile + ".dot", null, work);
							exec = Runtime.getRuntime();
							exec.exec("open " + theFile + ".dot", null, work);
						}
						else
						{
							log.addText("Executing:\ndotty " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("dotty " + theFile, null, work);
						}
						return;
					}
					catch (Exception e1)
					{
						JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				// new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				JList empty = new JList();
				// JRadioButton emptyButton = new JRadioButton();
				Run.createProperties(0, 0, 0, "Print Interval", 1, 1, 1, 1, 0, directory, 314159, 1, 1, new String[0], "tsd.printer", "amount", "false",
						(directory + theFile).split(GlobalConstants.separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty, empty,
						null, false, false, false);
				log.addText("Executing:\n" + reb2sacExecutable + " --target.encoding=dot --out=" + directory + out + ".dot " + directory + theFile
						+ "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec(reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + theFile, envp, work);
				String error = "";
				String output = "";
				InputStream reb = graph.getErrorStream();
				int read = reb.read();
				while (read != -1)
				{
					error += (char) read;
					read = reb.read();
				}
				reb.close();
				reb = graph.getInputStream();
				read = reb.read();
				while (read != -1)
				{
					output += (char) read;
					read = reb.read();
				}
				reb.close();
				if (!output.equals(""))
				{
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals(""))
				{
					log.addText("Errors:\n" + error + "\n");
				}
				graph.waitFor();
				if (error.equals(""))
				{
					if (System.getProperty("os.name").contentEquals("Linux"))
					{
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
					{
						log.addText("Executing:\nopen " + directory + out + ".dot\n");
						exec.exec("open " + out + ".dot", null, work);
					}
					else
					{
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
				}
				String remove;
				if (theFile.substring(theFile.length() - 4).equals("sbml"))
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (InterruptedException e1)
			{
				JOptionPane.showMessageDialog(frame, "Error graphing SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Error graphing SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == viewLearnedModel)
		{
			Component comp = tab.getSelectedComponent();
			for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++)
			{
				Component component = ((JTabbedPane) comp).getComponent(i);
				if (component instanceof LearnGCM)
				{
					((LearnGCM) component).viewModel();
					return;
				}
				else if (component instanceof LearnLPN)
				{
					((LearnLPN) component).viewLPN();
					return;
				}
			}
		}
		// if the graph popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("graph"))
		{
			String directory = "";
			String theFile = "";
			String filename = tree.getFile();
			if (filename.lastIndexOf('/') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			// }
			File work = new File(directory);
			String out = theFile;
			try
			{
				if (out.contains(".lpn"))
				{
					String file = theFile;
					String[] findTheFile = file.split("\\.");
					theFile = findTheFile[0] + ".dot";
					File dot = new File(root + GlobalConstants.separator + theFile);
					dot.delete();
					LPN lhpn = new LPN();
					lhpn.addObserver(this);
					lhpn.load(root + GlobalConstants.separator + file);
					lhpn.printDot(root + GlobalConstants.separator + theFile);
					// String cmd = "atacs -cPllodpl " + file;
					Runtime exec = Runtime.getRuntime();
					// Process ATACS = exec.exec(cmd, null, work);
					// ATACS.waitFor();
					// log.addText("Executing:\n" + cmd);
					if (dot.exists())
					{
						Preferences biosimrc = Preferences.userRoot();
						String command = biosimrc.get("biosim.general.graphviz", "");
						log.addText(command + " " + root + GlobalConstants.separator + theFile + "\n");
						exec.exec(command + " " + theFile, null, work);
					}
					else
					{
						File log = new File(root + GlobalConstants.separator + "atacs.log");
						BufferedReader input = new BufferedReader(new FileReader(log));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null)
						{
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
				if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml"))
				{
					out = out.substring(0, out.length() - 5);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml"))
				{
					out = out.substring(0, out.length() - 4);
				}
				else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".gcm"))
				{
					try
					{
						if (System.getProperty("os.name").contentEquals("Linux"))
						{
							log.addText("Executing:\ndotty " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("dotty " + theFile, null, work);
						}
						else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
						{
							log.addText("Executing:\nopen " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("cp " + theFile + " " + theFile + ".dot", null, work);
							exec = Runtime.getRuntime();
							exec.exec("open " + theFile + ".dot", null, work);
						}
						else
						{
							log.addText("Executing:\ndotty " + directory + theFile + "\n");
							Runtime exec = Runtime.getRuntime();
							exec.exec("dotty " + theFile, null, work);
						}
						return;
					}
					catch (Exception e1)
					{
						JOptionPane.showMessageDialog(frame, "Unable to view this gcm file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				// new Run(null);
				JCheckBox dummy = new JCheckBox();
				dummy.setSelected(false);
				JList empty = new JList();
				// JRadioButton emptyButton = new JRadioButton();
				Run.createProperties(0,0, 0, "Print Interval", 1, 1, 1, 1, 0, directory, 314159, 1, 1, new String[0], "tsd.printer", "amount", "false",
						(directory + theFile).split(GlobalConstants.separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty, empty,
						null, false, false, false);
				log.addText("Executing:\n" + reb2sacExecutable + " --target.encoding=dot --out=" + directory + out + ".dot " + directory + theFile
						+ "\n");
				Runtime exec = Runtime.getRuntime();
				Process graph = exec.exec(reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + theFile, null, work);
				String error = "";
				String output = "";
				InputStream reb = graph.getErrorStream();
				int read = reb.read();
				while (read != -1)
				{
					error += (char) read;
					read = reb.read();
				}
				reb.close();
				reb = graph.getInputStream();
				read = reb.read();
				while (read != -1)
				{
					output += (char) read;
					read = reb.read();
				}
				reb.close();
				if (!output.equals(""))
				{
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals(""))
				{
					log.addText("Errors:\n" + error + "\n");
				}
				graph.waitFor();
				if (error.equals(""))
				{
					if (System.getProperty("os.name").contentEquals("Linux"))
					{
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
					{
						log.addText("Executing:\nopen " + directory + out + ".dot\n");
						exec.exec("open " + out + ".dot", null, work);
					}
					else
					{
						log.addText("Executing:\ndotty " + directory + out + ".dot\n");
						exec.exec("dotty " + out + ".dot", null, work);
					}
				}
				String remove;
				if (theFile.substring(theFile.length() - 4).equals("sbml"))
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (InterruptedException e1)
			{
				JOptionPane.showMessageDialog(frame, "Error graphing sbml file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the browse popup menu is selected on an sbml file
		else if (e.getActionCommand().equals("browse"))
		{
			String directory;
			String theFile;
			String filename = tree.getFile();
			directory = "";
			theFile = "";
			if (filename.lastIndexOf('/') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			// }
			File work = new File(directory);
			String out = theFile;
			if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml"))
			{
				out = out.substring(0, out.length() - 5);
			}
			else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml"))
			{
				out = out.substring(0, out.length() - 4);
			}
			try
			{
				// new Run(null);
				JCheckBox dummy = new JCheckBox();
				JList empty = new JList();
				dummy.setSelected(false);
				Run.createProperties(0,0,0.0, "Print Interval", 1.0, 1.0, 1.0, 1.0, 0, directory, 314159L, 1, 1, new String[0], "tsd.printer", "amount",
						"false", (directory + theFile).split(GlobalConstants.separator), "none", frame, directory + theFile, 0.1, 0.1, 0.1, 15, 2.0, empty, empty,
						empty, null, false, false, false);
				log.addText("Executing:\n" + reb2sacExecutable + " --target.encoding=xhtml --out=" + directory + out + ".xhtml " + directory
						+ theFile + "\n");
				Runtime exec = Runtime.getRuntime();
				Process browse = exec.exec(reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + theFile, envp, work);
				String error = "";
				String output = "";
				InputStream reb = browse.getErrorStream();
				int read = reb.read();
				while (read != -1)
				{
					error += (char) read;
					read = reb.read();
				}
				reb.close();
				reb = browse.getInputStream();
				read = reb.read();
				while (read != -1)
				{
					output += (char) read;
					read = reb.read();
				}
				reb.close();
				if (!output.equals(""))
				{
					log.addText("Output:\n" + output + "\n");
				}
				if (!error.equals(""))
				{
					log.addText("Errors:\n" + error + "\n");
				}
				browse.waitFor();

				Preferences biosimrc = Preferences.userRoot();
				String command = biosimrc.get("biosim.general.browser", "");
				if (error.equals(""))
				{
					log.addText("Executing:\n" + command + " " + directory + out + ".xhtml\n");
					exec.exec(command + " " + out + ".xhtml", null, work);
				}
				String remove;
				if (theFile.substring(theFile.length() - 4).equals("sbml"))
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + "properties";
				}
				else
				{
					remove = (directory + theFile).substring(0, (directory + theFile).length() - 4) + ".properties";
				}
				System.gc();
				new File(remove).delete();
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(frame, "Error viewing SBML file in a browser.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the save button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("save"))
		{
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor)
			{
				((LHPNEditor) comp).save();
			}
			else if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).save(false);
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				try {
					((SBOLDesignerPlugin) comp).saveSBOL();
					readSBOLDocument();
					log.addText("Saving SBOL file: " + ((SBOLDesignerPlugin) comp).getFileName() + "\n");
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (comp instanceof Graph)
			{
				((Graph) comp).save();
			}
			else if (comp instanceof Verification)
			{
				((Verification) comp).save();
			}
			else if (comp instanceof JTabbedPane)
			{
				if (comp instanceof SynthesisView)
				{
					((SynthesisView) comp).save();
				}
				else
				{
					for (Component component : ((JTabbedPane) comp).getComponents())
					{
						int index = ((JTabbedPane) comp).getSelectedIndex();
						if (component instanceof Graph)
						{
							((Graph) component).save();
						}
						else if (component instanceof LearnGCM)
						{
							((LearnGCM) component).save();
						}
						else if (component instanceof LearnLPN)
						{
							((LearnLPN) component).save();
						}
						else if (component instanceof DataManager)
						{
							((DataManager) component).saveChanges(((JTabbedPane) comp).getTitleAt(index));
						}
						/*
						 * else if (component instanceof SBML_Editor) {
						 * ((SBML_Editor) component).save(false, "", true,
						 * true); }
						 */
						else if (component instanceof ModelEditor)
						{
							((ModelEditor) component).saveParams(false, "", true, null);
						}
						else if (component instanceof AnalysisView)
						{
							((AnalysisView) component).save("");
						}
						else if (component instanceof MovieContainer)
						{
							((MovieContainer) component).savePreferences();
						}
					}
				}
			}
			if (comp instanceof JPanel)
			{
				if (comp.getName().equals("Synthesis"))
				{
					// ((Synthesis) tab.getSelectedComponent()).save();
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
				}
			}
			else if (comp instanceof JScrollPane)
			{
				String fileName = getTitleAt(tab.getSelectedIndex());
				try
				{
					File output = new File(root + GlobalConstants.separator + fileName);
					output.createNewFile();
					FileOutputStream outStream = new FileOutputStream(output);
					Component[] array = ((JScrollPane) comp).getComponents();
					array = ((JViewport) array[0]).getComponents();
					if (array[0] instanceof JTextArea)
					{
						String text = ((JTextArea) array[0]).getText();
						char[] chars = text.toCharArray();
						for (int j = 0; j < chars.length; j++)
						{
							outStream.write(chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + GlobalConstants.separator + fileName);
					this.updateAsyncViews(fileName);
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Error saving file " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the save as button is pressed on the Tool Bar
		else if (e.getActionCommand().equals("saveas"))
		{
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof LHPNEditor)
			{
				String newName = JOptionPane.showInputDialog(frame, "Enter LPN name:", "LPN Name", JOptionPane.PLAIN_MESSAGE);
				if (newName == null)
				{
					return;
				}
				if (!newName.endsWith(".lpn") && !newName.endsWith(".xml"))
				{
					newName = newName + ".lpn";
				}
				((LHPNEditor) comp).saveAs(newName);
				if (newName.endsWith(".lpn"))
				{
					tab.setTitleAt(tab.getSelectedIndex(), newName);
				}
			}
			else if (comp instanceof ModelEditor)
			{
				String newName = JOptionPane.showInputDialog(frame, "Enter model name:", "Model Name", JOptionPane.PLAIN_MESSAGE); 
				if (newName == null)
				{
					return;
				}
				if (newName.contains(".gcm"))
				{
					newName = newName.replace(".gcm", "");
				}
				if (newName.contains(".xml"))
				{
					newName = newName.replace(".xml", "");
				}
				if (newName.endsWith(".lpn"))
				{
					((ModelEditor) comp).saveAsLPN(newName);
				}
				else if (newName.endsWith(".sv"))
				{
					((ModelEditor) comp).saveAsVerilog(newName);
				}
				else
				{
					((ModelEditor) comp).saveAs(newName);
				}
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				String oldName = ((SBOLDesignerPlugin) comp).getFileName();
				String newName = JOptionPane.showInputDialog(frame, "Enter SBOL file name:", "SBOL File Name", JOptionPane.PLAIN_MESSAGE); 
				if (!newName.endsWith(".sbol")) newName += ".sbol";
				((SBOLDesignerPlugin) comp).setFileName(newName);
				try {
					((SBOLDesignerPlugin) comp).saveSBOL();
					readSBOLDocument();
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				addToTree(newName);
				updateTabName(oldName.replace(".sbol", ""),newName.replace(".sbol", ""));
				log.addText("Saving SBOL file: " + ((SBOLDesignerPlugin) comp).getFileName() + "\n");
			}
			else if (comp instanceof Graph)
			{
				((Graph) comp).saveAs();
			}
			else if (comp instanceof Verification)
			{
				((Verification) comp).saveAs();
			}
			else if (comp instanceof JTabbedPane)
			{
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph)
				{
					((Graph) component).saveAs();
				}
			}
			else if (comp instanceof JPanel)
			{
				if (comp.getName().equals("Synthesis"))
				{
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).saveAs();
				}
			}
			else if (comp instanceof JScrollPane)
			{
				String fileName = getTitleAt(tab.getSelectedIndex());
				String newName = "";
				if (fileName.endsWith(".vhd"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter VHDL name:", "VHDL Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".vhd"))
					{
						newName = newName + ".vhd";
					}
				}
				if (fileName.endsWith(".prop"))
				{ // DK
					newName = JOptionPane.showInputDialog(frame, "Enter Property name:", "Property Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".prop"))
					{
						newName = newName + ".prop";
					}
				}
				else if (fileName.endsWith(".s"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter Assembly File Name:", "Assembly File Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".s"))
					{
						newName = newName + ".s";
					}
				}
				else if (fileName.endsWith(".inst"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter Instruction File Name:", "Instruction File Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".inst"))
					{
						newName = newName + ".inst";
					}
				}
				else if (fileName.endsWith(".g"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter Petri net name:", "Petri net Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".g"))
					{
						newName = newName + ".g";
					}
				}
				else if (fileName.endsWith(".csp"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter CSP name:", "CSP Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".csp"))
					{
						newName = newName + ".csp";
					}
				}
				else if (fileName.endsWith(".hse"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter HSE name:", "HSE Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".hse"))
					{
						newName = newName + ".hse";
					}
				}
				else if (fileName.endsWith(".unc"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter UNC name:", "UNC Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".unc"))
					{
						newName = newName + ".unc";
					}
				}
				else if (fileName.endsWith(".rsg"))
				{
					newName = JOptionPane.showInputDialog(frame, "Enter RSG name:", "RSG Name", JOptionPane.PLAIN_MESSAGE);
					if (newName == null)
					{
						return;
					}
					if (!newName.endsWith(".rsg"))
					{
						newName = newName + ".rsg";
					}
				}
				try
				{
					File output = new File(root + GlobalConstants.separator + newName);
					output.createNewFile();
					FileOutputStream outStream = new FileOutputStream(output);
					Component[] array = ((JScrollPane) comp).getComponents();
					array = ((JViewport) array[0]).getComponents();
					if (array[0] instanceof JTextArea)
					{
						String text = ((JTextArea) array[0]).getText();
						char[] chars = text.toCharArray();
						for (int j = 0; j < chars.length; j++)
						{
							outStream.write(chars[j]);
						}
					}
					outStream.close();
					log.addText("Saving file:\n" + root + GlobalConstants.separator + newName);
					File oldFile = new File(root + GlobalConstants.separator + fileName);
					oldFile.delete();
					tab.setTitleAt(tab.getSelectedIndex(), newName);
					addToTree(newName);
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Error saving file " + newName, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the run button is selected on the tool bar
		else if (e.getActionCommand().equals("run"))
		{
			Component comp = tab.getSelectedComponent();
			// int index = tab.getSelectedIndex();
			if (comp instanceof JTabbedPane)
			{
				// int index = -1;
				if (comp instanceof SynthesisView)
				{
					synthesizeSBOL((SynthesisView) comp);
				}
				else
				{
					for (int i = 0; i < ((JTabbedPane) comp).getTabCount(); i++)
					{
						Component component = ((JTabbedPane) comp).getComponent(i);
						if (component instanceof AnalysisView)
						{
							((AnalysisView) component).executeRun();
							break;
						}
						else if (component instanceof LearnGCM)
						{
							((LearnGCM) component).save();
							new Thread((LearnGCM) component).start();
							break;
						}
						else if (component instanceof LearnLPN)
						{
							((LearnLPN) component).save();
							((LearnLPN) component).learn();
							break;
						}
					}
				}
			}
			else if (comp.getName().equals("Verification"))
			{
				if (comp instanceof Verification)
				{
					((Verification) comp).save();
					new Thread((Verification) comp).start();
				}
				else
				{
					// Not sure if this is necessary anymore
					Component[] array = ((JPanel) comp).getComponents();
					((Verification) array[0]).save();
					new Thread((Verification) array[0]).start();
				}
			}
			else if (comp.getName().equals("Synthesis"))
			{
				if (comp instanceof Synthesis)
				{
					((Synthesis) comp).save();
					new Thread((Synthesis) comp).start();
				}
				else
				{
					// Not sure if this is necessary anymore
					Component[] array = ((JPanel) comp).getComponents();
					((Synthesis) array[0]).save();
					new Thread((Synthesis) array[0]).start();
				}
			}
		}
		else if (e.getActionCommand().equals("refresh"))
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof JTabbedPane)
			{
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph)
				{
					((Graph) component).refresh();
				}
			}
			else if (comp instanceof Graph)
			{
				((Graph) comp).refresh();
			}
		}
		else if (e.getActionCommand().equals("check"))
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).save(true);
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				log.addText("Saving SBOL file: " + ((SBOLDesignerPlugin) comp).getFileName() + "\n");	
				try {
					((SBOLDesignerPlugin) comp).saveSBOL();
					readSBOLDocument();
				}
				catch (Exception e2) {
					JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				SBOLDocument sbolDoc;
				try {
					SBOLReader.setKeepGoing(true);
					sbolDoc = SBOLReader.read(root + GlobalConstants.separator + ((SBOLDesignerPlugin) comp).getFileName());
					checkSBOL(sbolDoc,true);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error Validating SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getActionCommand().equals("export"))
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof Graph)
			{
				((Graph) comp).export();
			}
			else if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).exportSBML();
				// TODO: should give choice of SBML or SBOL
			}
			else if (comp instanceof SBOLDesignerPlugin)
			{
				exportSBOL((SBOLDesignerPlugin) comp, "SBOL");
			}
			else if (comp instanceof JTabbedPane)
			{
				Component component = ((JTabbedPane) comp).getSelectedComponent();
				if (component instanceof Graph)
				{
					((Graph) component).export();
				}
				// TODO: what about export of movie?
			}
		}
		// if the new menu item is selected
		else if (e.getSource() == newProj)
		{
			createProject(e);
		}
		// if the open project menu item is selected
		else if (e.getSource() == pref)
		{
			EditPreferences editPreferences = new EditPreferences(frame, async, tree);
			editPreferences.preferences();
			if (sbolDocument!=null) {
				Preferences biosimrc = Preferences.userRoot();
				sbolDocument.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
			}
		}
		else if (e.getSource() == clearRecent)
		{
			removeAllRecentProjects();
		}
		else if ((e.getSource() == openProj) || (e.getSource() == recentProjects[0]) || (e.getSource() == recentProjects[1])
				|| (e.getSource() == recentProjects[2]) || (e.getSource() == recentProjects[3]) || (e.getSource() == recentProjects[4])
				|| (e.getSource() == recentProjects[5]) || (e.getSource() == recentProjects[6]) || (e.getSource() == recentProjects[7])
				|| (e.getSource() == recentProjects[8]) || (e.getSource() == recentProjects[9]))
		{
			openProject(e);
		}
		// if the new circuit model menu item is selected
		else if (e.getSource() == newSBMLModel)
		{
			createModel(false);
		}
		else if (e.getSource() == newSBOL)
		{
			createPart();
		}
		else if (e.getSource() == newGridModel)
		{
			createModel(true);
		}
		// if the new vhdl menu item is selected
		else if (e.getSource() == newVhdl)
		{
			newModel("VHDL", ".vhd");
		}
		// if the new assembly menu item is selected
		else if (e.getSource() == newS)
		{
			newModel("Assembly", ".s");
		}
		// if the new instruction file menu item is selected
		else if (e.getSource() == newInst)
		{
			newModel("Instruction", ".inst");
		}
		// if the new petri net menu item is selected
		else if (e.getSource() == newG)
		{
			newModel("Petri Net", ".g");
		}
		// if the new lhpn menu item is selected
		else if (e.getSource() == newLhpn)
		{
			createLPN();
		}
		else if (e.getSource() == newProperty)
		{ // DK
			newModel("Property", ".prop");
		}
		// if the new csp menu item is selected
		else if (e.getSource() == newCsp)
		{
			newModel("CSP", ".csp");
		}
		// if the new hse menu item is selected
		else if (e.getSource() == newHse)
		{
			newModel("Handshaking Expansion", ".hse");
		}
		// if the new unc menu item is selected
		else if (e.getSource() == newUnc)
		{
			newModel("Extended Burst Mode Machine", ".unc");
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newRsg)
		{
			newModel("Reduced State Graph", ".rsg");
		}
		// if the new rsg menu item is selected
		else if (e.getSource() == newSpice)
		{
			newModel("Spice Circuit", ".cir");
		}
		else if (e.getSource().equals(importSbol))
		{
			importSBOL("Import SBOL");
		}
		else if (e.getSource().equals(importGenBank))
		{
			importSBOL("Import GenBank");
		}
		else if (e.getSource().equals(importFasta))
		{
			importSBOL("Import Fasta");
		}
		else if (e.getSource().equals(importSedml))
		{
			importSEDML();
		}
		// if the import sbml menu item is selected
		else if (e.getSource() == importSbml)
		{
			importSBML(null);
		}
		else if (e.getSource() == importBioModel)
		{
			importBioModel();
		}
		else if (e.getSource() == importVirtualPart)
		{
//			importVirtualPart();
		}
		// if the import dot menu item is selected
		/*
		 * else if (e.getSource() == importDot) { importGCM(); }
		 */
		// if the import vhdl menu item is selected
		else if (e.getSource() == importVhdl)
		{
			importFile("VHDL", ".vhd", ".vhd");
		}
		else if (e.getSource() == importProperty)
		{
			importFile("Property", ".prop", ".prop");
		}
		else if (e.getSource() == importS)
		{
			importFile("Assembly", ".s", ".s");
		}
		else if (e.getSource() == importInst)
		{
			importFile("Instruction", ".inst", ".inst");
		}
		else if (e.getSource() == importLpn)
		{
			importLPN();
		}
		else if (e.getSource() == importG)
		{
			importFile("Petri Net", ".g", ".g");
		}
		// if the import csp menu item is selected
		else if (e.getSource() == importCsp)
		{
			importFile("CSP", ".csp", ".csp");
		}
		// if the import hse menu item is selected
		else if (e.getSource() == importHse)
		{
			importFile("Handshaking Expansion", ".hse", ".hse");
		}
		// if the import unc menu item is selected
		else if (e.getSource() == importUnc)
		{
			importFile("Extended Burst State Machine", ".unc", ".unc");
		}
		// if the import rsg menu item is selected
		else if (e.getSource() == importRsg)
		{
			importFile("Reduced State Graph", ".rsg", ".rsg");
		}
		// if the import spice menu item is selected
		else if (e.getSource() == importSpice)
		{
			importFile("Spice Circuit", ".cir", ".cir");
		}
		// if the Graph data menu item is clicked
		else if (e.getSource() == graph)
		{
			createGraph();
		}
		else if (e.getSource() == probGraph)
		{
			createHistogram();
		}
		else if (e.getActionCommand().equals("createLearn"))
		{
			try
			{
				createLearn(tree.getFile());
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "You must select a valid model file for learning.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("viewModel"))
		{
			viewModel();
		}
		else if (e.getSource() == select)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).select();
			}
		}
		else if (e.getSource() == cut)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).cut();
			}
		}
		else if (e.getSource() == addCompartment)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addCompartment();
			}
		}
		else if (e.getSource() == addSpecies)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addSpecies();
			}
		}
		else if (e.getSource() == addReaction)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addReaction();
			}
		}
		else if (e.getSource() == addModule)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addComponent();
			}
		}
		else if (e.getSource() == addPromoter)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addPromoter();
			}
		}
		else if (e.getSource() == addVariable)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addVariable();
			}
		}
		else if (e.getSource() == addBoolean)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addBoolean();
			}
		}
		else if (e.getSource() == addPlace)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addPlace();
			}
		}
		else if (e.getSource() == addTransition)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addTransition();
			}
		}
		else if (e.getSource() == addRule)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addRule();
			}
		}
		else if (e.getSource() == addConstraint)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addConstraint();
			}
		}
		else if (e.getSource() == addEvent)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addEvent();
			}
		}
		else if (e.getSource() == addSelfInfl)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).addSelfInfluence();
			}
		}
		else if (e.getSource() == moveLeft)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).moveLeft();
			}
		}
		else if (e.getSource() == moveRight)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).moveRight();
			}
		}
		else if (e.getSource() == moveUp)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).moveUp();
			}
		}
		else if (e.getSource() == moveDown)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).moveDown();
			}
		}
		else if (e.getSource() == undo)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).undo();
			}
		}
		else if (e.getSource() == redo)
		{
			Component comp = tab.getSelectedComponent();
			if (comp instanceof ModelEditor)
			{
				((ModelEditor) comp).redo();
			}
		}
		else if (e.getActionCommand().equals("copy") || e.getSource() == copy)
		{
			copy();
		}
		else if (e.getActionCommand().equals("rename") || e.getSource() == rename)
		{
			rename();
		}
		else if (e.getActionCommand().equals("openGraph"))
		{
			openGraph();
		}
		else if (e.getActionCommand().equals("openHistogram"))
		{
			openHistogram();
		}
		enableTabMenu(tab.getSelectedIndex());
		enableTreeMenu();
	}
	
	public void exportSBOL(SBOLDesignerPlugin sbolDesignerPlugin,String fileType) {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = frontend.main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, 
				"Export " + fileType.replace("1", ""), -1);
		if (!exportPath.equals("")) {
			String dir = exportPath.substring(0, exportPath.lastIndexOf(GlobalConstants.separator));
			biosimrc.put("biosim.general.export_dir",dir);
			log.addText("Exporting " + fileType + " file:\n" + exportPath + "\n");
			try {
				sbolDesignerPlugin.exportSBOL(exportPath,fileType);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Unable to export file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void deleteFromSEDML(String fileName) {
		SedML sedml = sedmlDocument.getSedMLModel();
		if (fileName.endsWith(".prb") || fileName.endsWith(".grf")) {
			String outputId = fileName.replace(".prb","").replace(".grf", "");
			Output output = sedml.getOutputWithId(outputId);
			if (output != null) {
				sedml.removeOutput(output);
			}
		} else if (fileName.endsWith(".xml")) {
			ArrayList<String> remove = new ArrayList<String>();
			for (org.jlibsedml.Model model : sedml.getModels()) {
				if (model.getSource().equals(fileName)) {
					remove.add(model.getId());
				}
			}
			int size;
			do {
				size = remove.size();
				for (org.jlibsedml.Model model : sedml.getModels()) {
					if (!remove.contains(model.getId()) && 
							remove.contains(model.getSource())) {
						remove.add(model.getId());
					}
				}
			} while (size != remove.size());
			for (String modelId : remove) {
				sedml.removeModel(sedml.getModelWithId(modelId));
			}
		} else {
			AbstractTask task = sedml.getTaskWithId(fileName);
			if (task != null) {
				sedml.removeTask(task);
				Simulation simulation = sedml.getSimulation(fileName+"_sim");
				if (simulation != null) {
					sedml.removeSimulation(simulation);
				}
				org.jlibsedml.Model model = sedml.getModelWithId(fileName+"_model");
				if (model != null) {
					sedml.removeModel(model);
				}
				Output output = sedml.getOutputWithId(fileName+"__graph");
				if (output != null) {
					sedml.removeOutput(output);
				}
				output = sedml.getOutputWithId(fileName+"__report");
				if (output != null) {
					sedml.removeOutput(output);
				}
				SEDMLutilities.removeDataGeneratorsByTaskId(sedml,fileName);
				ArrayList<AbstractTask> subTasks = new ArrayList<AbstractTask>();
				for (AbstractTask subTask : sedml.getTasks()) {
					if (subTask.getId().startsWith(fileName+"__")) {
						subTasks.add(subTask);
					}
				}
				ArrayList<DataGenerator> remove = new ArrayList<DataGenerator>();
				for (AbstractTask subTask : subTasks) {
					remove.clear();
					for (DataGenerator dg : sedml.getDataGenerators()) {
						for (Variable var : dg.getListOfVariables()) {
							if (var.getReference().equals(subTask.getId())) {
								remove.add(dg);
								break;
							}
						}
					}
					for (DataGenerator dg : remove) {
						sedml.removeDataGenerator(dg);
					}
					simulation = sedml.getSimulation(subTask.getId()+"_sim");
					if (simulation != null) {
						sedml.removeSimulation(simulation);
					}
					model = sedml.getModelWithId(subTask.getId()+"_model");
					if (model != null) {
						sedml.removeModel(model);
					}
					sedml.removeTask(subTask);
				}
				for (Output out : sedml.getOutputs()) {
					if (out instanceof Plot2D) {
						ArrayList<Curve> removeCurves = new ArrayList<Curve>();
						Plot2D plot2d = (Plot2D)out;
						for (Curve curve : plot2d.getListOfCurves()) {
							if (sedml.getDataGeneratorWithId(curve.getXDataReference())==null ||
									sedml.getDataGeneratorWithId(curve.getYDataReference())==null) {
								removeCurves.add(curve);
							}
						
						}
						for (Curve curve : removeCurves) {
							plot2d.removeCurve(curve);
						}
					} else if (out instanceof Plot3D) {
						ArrayList<Surface> removeSurfaces = new ArrayList<Surface>();
						Plot3D plot3d = (Plot3D)out;
						for (Surface surface : plot3d.getListOfSurfaces()) {
							if (sedml.getDataGeneratorWithId(surface.getXDataReference())==null ||
									sedml.getDataGeneratorWithId(surface.getYDataReference())==null ||
									sedml.getDataGeneratorWithId(surface.getZDataReference())==null) {
								removeSurfaces.add(surface);
							}
						
						}
						for (Surface surface : removeSurfaces) {
							plot3d.removeSurface(surface);
						}
					} else if (out instanceof Report) {
						ArrayList<DataSet> removeDataSets = new ArrayList<DataSet>();
						Report report = (Report)out;
						for (DataSet dataset : report.getListOfDataSets()) {
							if (sedml.getDataGeneratorWithId(dataset.getDataReference())==null) {
								removeDataSets.add(dataset);
							}
						
						}
						for (DataSet dataset : removeDataSets) {
							report.removeDataSet(dataset);
						}						
					}
				}
			}
			
		}
		// Prune unnecessary simulations
		ArrayList<Simulation> remove = new ArrayList<Simulation>();
		for (Simulation sim : sedml.getSimulations()) {
			remove.add(sim);
			for (AbstractTask task : sedml.getTasks()) {
				if (task instanceof RepeatedTask) continue;
				if (task.getSimulationReference().equals(sim.getId())) {
					remove.remove(sim);
					break;
				}
			}
		}
		for (Simulation sim : remove) {
			sedml.removeSimulation(sim);
		}
		writeSEDMLDocument();
	}

	private void delete(String fullPath)
	{
		if (!fullPath.equals(root))
		{
			int value = JOptionPane.YES_OPTION;
			if (new File(fullPath).isDirectory())
			{
				String dirName = fullPath.split(GlobalConstants.separator)[fullPath.split(GlobalConstants.separator).length - 1];
				for (int i = 0; i < tab.getTabCount(); i++)
				{
					if (getTitleAt(i).equals(dirName))
					{
						tab.remove(i);
					}
				}
				File dir = new File(fullPath);
				if (dir.isDirectory())
				{
					deleteDir(dir);
				}
				else
				{
					System.gc();
					dir.delete();
				}
				deleteFromTree(dirName);
				deleteFromSEDML(dirName);
			}
			else
			{
				String filename = fullPath.split(GlobalConstants.separator)[fullPath.split(GlobalConstants.separator).length - 1];
				String[] views = canDelete(filename);
				if (views.length != 0)
				{
					String view = "";
					String gcms = "";
					for (int i = 0; i < views.length; i++)
					{
						if (views[i].endsWith(".xml"))
						{
							gcms += views[i] + "\n";
						}
						else
						{
							view += views[i] + "\n";
						}
					}
					String message = "Unable to delete the selected file.\n";
					if (!views.equals(""))
					{
						message += "\nIt is linked to the following views:\n" + view;
					}
					if (!gcms.equals(""))
					{
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
					value = JOptionPane.showOptionDialog(frame, scroll, "Unable to Delete File", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
				}
				else
				{
					Preferences biosimrc = Preferences.userRoot();
					if (biosimrc.get("biosim.general.delete", "").equals("confirm"))
					{
						Object[] options = { "Yes", "No" };
						value = JOptionPane.showOptionDialog(frame, "Are you sure you want to delete " + filename + "?", "Confirm Delete",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
				if (value == JOptionPane.YES_OPTION)
				{
					for (int i = 0; i < views.length; i++)
					{
						delete(root + GlobalConstants.separator + views[i]);
					}
					String fileName = fullPath.split(GlobalConstants.separator)[fullPath.split(GlobalConstants.separator).length - 1];
					for (int i = 0; i < tab.getTabCount(); i++)
					{
						if (getTitleAt(i).equals(fileName))
						{
							tab.remove(i);
						}
					}
					System.gc();
					if (fullPath.endsWith(".xml"))
					{
						// This code removes generated SBOL from SBOL file when SBML file is deleted
						// Not really necessary
//						SBMLDocument document = SBMLutilities.readSBML(fullPath);
//						List<URI> sbolURIs = new LinkedList<URI>();
//						AnnotationUtility.parseSBOLAnnotation(document.getModel(), sbolURIs);
//						Iterator<URI> sbolIterator = sbolURIs.iterator();
//						while (sbolIterator != null && sbolIterator.hasNext())
//						{
//							URI sbolURI = sbolIterator.next();
//							if (sbolURI.toString().endsWith("iBioSim"))
//							{
//								sbolIterator = null;
//								for (String filePath : getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION))
//								{
//									SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
//									try {
//										SBOLUtility2.deleteDNAComponent(sbolURI, sbolDoc);
//									}
//									catch (SBOLValidationException e) {
//										e.printStackTrace();
//									}
//									SBOLUtility2.writeSBOLDocument(filePath, sbolDoc);
//								}
//							}
//						}
						new File(fullPath.replace(".xml", ".gcm")).delete();
					}
					new File(fullPath).delete();
					deleteFromTree(fileName);
					deleteFromSEDML(fileName);
				}
			}
		}
	}

	private void importBioModel()
	{
		final BioModelsWSClient client = new BioModelsWSClient();
		if (BioModelIds == null)
		{
			try
			{
				BioModelIds = client.getAllCuratedModelsId();
			}
			catch (BioModelsWSException e2)
			{
				JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		final JPanel BioModelsPanel = new JPanel(new BorderLayout());
		final JList ListOfBioModels = new JList();
		dataModels.biomodel.util.Utility.sort(BioModelIds);
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
		final JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		runGetNames = true;
		final Thread getNamesThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Preferences biosimrc = Preferences.userRoot();
				for (int i = 0; i < BioModelIds.length && runGetNames; i++)
				{
					try
					{
						progressBar.setValue(100 * i / BioModelIds.length);
						if (!BioModelIds[i].contains(" "))
						{
							if (!biosimrc.get(BioModelIds[i], "").equals(""))
							{
								BioModelIds[i] += " " + biosimrc.get(BioModelIds[i], "");
							}
							else
							{
								String name = client.getModelNameById(BioModelIds[i]);
								biosimrc.put(BioModelIds[i], name);
								BioModelIds[i] += " " + name;
							}
							ListOfBioModels.setListData(BioModelIds);
							ListOfBioModels.revalidate();
							ListOfBioModels.repaint();
						}
					}
					catch (BioModelsWSException e1)
					{
						JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
						runGetNames = false;
					}
				}
				progressBar.setValue(100);
				ListOfBioModels.setListData(BioModelIds);
				runGetNames = false;
			}
		});
		GetNames.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (runGetNames && !getNamesThread.isAlive())
				{
					getNamesThread.start();
				}
			}
		});
		GetDescription.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (ListOfBioModels.isSelectionEmpty())
				{
					return;
				}
				String SelectedModel = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
				Preferences biosimrc = Preferences.userRoot();
				String command = biosimrc.get("biosim.general.browser", "");
				command = command + " http://www.ebi.ac.uk/compneur-srv/biomodels-main/" + SelectedModel;
				log.addText("Executing:\n" + command + "\n");
				Runtime exec = Runtime.getRuntime();
				try
				{
					exec.exec(command);
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(frame, "Unable to open model description.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		GetReference.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (ListOfBioModels.isSelectionEmpty())
				{
					return;
				}
				String SelectedModel = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
				try
				{
					String Pub = (client.getSimpleModelById(SelectedModel)).getPublicationId();
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.browser", "");
					command = command + " http://www.ncbi.nlm.nih.gov/pubmed/?term=" + Pub;
					log.addText("Executing:\n" + command + "\n");
					Runtime exec = Runtime.getRuntime();
					exec.exec(command);
				}
				catch (BioModelsWSException e2)
				{
					JOptionPane.showMessageDialog(frame, "Error Contacting BioModels Database", "Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException e1)
				{
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
		if (value == JOptionPane.YES_OPTION && ListOfBioModels.getSelectedValue() != null)
		{
			String ModelId = ((String) ListOfBioModels.getSelectedValue()).split(" ")[0];
			String filename = ModelId + ".xml";
			try
			{
				if (overwrite(root + GlobalConstants.separator + filename, filename))
				{
					String model = client.getModelSBMLById(ModelId);
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(root + GlobalConstants.separator + filename), "UTF-8"));
					out.write(model);
					out.close();
					String[] file = filename.trim().split(GlobalConstants.separator);
					SBMLDocument document = SBMLutilities.readSBML(root + GlobalConstants.separator + filename.trim());
					Utils.check(root + GlobalConstants.separator + filename.trim(), document, false);
					SBMLWriter writer = new SBMLWriter();
					writer.writeSBMLToFile(document, root + GlobalConstants.separator + file[file.length - 1]);
					addToTree(file[file.length - 1]);
					openSBML(root + GlobalConstants.separator + file[file.length - 1]);
				}
			}
			catch (MalformedURLException e1)
			{
				JOptionPane.showMessageDialog(frame, e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				filename = "";
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, filename + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
				filename = "";
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


//	private void importVirtualPart()
//	{
//		final PartsHandler partsHandler = new PartsHandler("http://sbol.ncl.ac.uk:8081/");
//		if (!showParts && getPartsThread == null)
//		{
//			getPartsThread = new Thread(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					try
//					{
//						final JButton cancel = new JButton("Cancel");
//						final MutableBoolean stop = new MutableBoolean(false);
//						cancel.addActionListener(new ActionListener()
//						{
//							@Override
//							public void actionPerformed(ActionEvent arg0)
//							{
//								stop.setValue(true);
//							}
//						});
//						final JFrame running = new JFrame("Progress");
//						WindowListener w = new WindowListener()
//						{
//							@Override
//							public void windowClosing(WindowEvent arg0)
//							{
//								running.setCursor(null);
//								cancel.doClick();
//								running.dispose();
//							}
//
//							@Override
//							public void windowOpened(WindowEvent arg0)
//							{
//							}
//
//							@Override
//							public void windowClosed(WindowEvent arg0)
//							{
//							}
//
//							@Override
//							public void windowIconified(WindowEvent arg0)
//							{
//							}
//
//							@Override
//							public void windowDeiconified(WindowEvent arg0)
//							{
//							}
//
//							@Override
//							public void windowActivated(WindowEvent arg0)
//							{
//							}
//
//							@Override
//							public void windowDeactivated(WindowEvent arg0)
//							{
//							}
//						};
//						running.addWindowListener(w);
//						JPanel text = new JPanel();
//						JPanel progBar = new JPanel();
//						JPanel button = new JPanel();
//						JPanel all = new JPanel(new BorderLayout());
//						JLabel label = new JLabel("Retrieving Virtual Parts");
//						Summary summary = partsHandler.GetPartsSummary();
//						int pageCount = summary.getPageCount();
//						JProgressBar progress = new JProgressBar(0, pageCount);
//						progress.setStringPainted(true);
//						progress.setValue(0);
//						text.add(label);
//						progBar.add(progress);
//						button.add(cancel);
//						all.add(text, "North");
//						all.add(progBar, "Center");
//						all.add(button, "South");
//						running.setContentPane(all);
//						running.pack();
//						Dimension screenSize;
//						try
//						{
//							Toolkit tk = Toolkit.getDefaultToolkit();
//							screenSize = tk.getScreenSize();
//						}
//						catch (AWTError awe)
//						{
//							screenSize = new Dimension(640, 480);
//						}
//						Dimension frameSize = running.getSize();
//
//						if (frameSize.height > screenSize.height)
//						{
//							frameSize.height = screenSize.height;
//						}
//						if (frameSize.width > screenSize.width)
//						{
//							frameSize.width = screenSize.width;
//						}
//						int x = screenSize.width / 2 - frameSize.width / 2;
//						int y = screenSize.height / 2 - frameSize.height / 2;
//						running.setLocation(x, y);
//						running.setVisible(true);
//						running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//						allVirtualParts = new Parts();
//						for (int i = 1; i <= pageCount && !stop.booleanValue(); i++)
//						{
//							Parts parts = partsHandler.GetParts(i);
//							allVirtualParts.getParts().addAll(parts.getParts());
//							progress.setValue(i);
//						}
//						running.setCursor(null);
//						running.dispose();
//						if (!stop.booleanValue())
//						{
//							showParts = true;
//							importVirtualPart();
//						}
//						else
//						{
//							getPartsThread = null;
//						}
//					}
//					catch (IOException e)
//					{
//						e.printStackTrace();
//					}
//				}
//			});
//			getPartsThread.start();
//		}
//		else if (showParts)
//		{
//			final List<Part> list = allVirtualParts.getParts();
//			final JPanel virtualPartsPanel = new JPanel(new BorderLayout());
//			final JPanel labelPanel = new JPanel(new BorderLayout());
//			TableModel dataModel = new AbstractTableModel()
//			{
//
//				private static final long	serialVersionUID	= 1L;
//
//				@Override
//				public int getColumnCount()
//				{
//					return 6;
//				}
//
//				@Override
//				public int getRowCount()
//				{
//					return list.size();
//				}
//
//				@Override
//				public Object getValueAt(int row, int col)
//				{
//					Part p = list.get(row);
//					switch (col)
//					{
//					case 0:
//						return p.getName();
//					case 1:
//						return p.getType();
//					case 2:
//						return p.getDisplayName();
//					case 3:
//						return p.getOrganism();
//					case 4:
//						return p.getDescription();
//					default:
//						return row;
//					}
//				}
//
//				@Override
//				public String getColumnName(int col)
//				{
//					switch (col)
//					{
//					case 0:
//						return "ID";
//					case 1:
//						return "Type";
//					case 2:
//						return "Name";
//					case 3:
//						return "Organism";
//					case 4:
//						return "Description";
//					default:
//						return "Entry";
//					}
//				}
//			};
//			final JTable tableOfVirtualParts = new JTable(dataModel);
//			tableOfVirtualParts.setAutoCreateRowSorter(true);
//			tableOfVirtualParts.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//			tableOfVirtualParts.getColumnModel().getColumn(0).setPreferredWidth(150);
//			tableOfVirtualParts.getColumnModel().getColumn(1).setPreferredWidth(150);
//			tableOfVirtualParts.getColumnModel().getColumn(2).setPreferredWidth(150);
//			tableOfVirtualParts.getColumnModel().getColumn(3).setPreferredWidth(150);
//			tableOfVirtualParts.getColumnModel().getColumn(4).setPreferredWidth(150);
//			tableOfVirtualParts.getColumnModel().getColumn(5).setMinWidth(0);
//			tableOfVirtualParts.getColumnModel().getColumn(5).setMaxWidth(0);
//			tableOfVirtualParts.getColumnModel().getColumn(5).setWidth(0);
//			tableOfVirtualParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//			tableOfVirtualParts.addMouseListener(new MouseListener()
//			{
//				@Override
//				public void mouseClicked(MouseEvent arg0)
//				{
//					try
//					{
//						if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2)
//						{
//							int selected = tableOfVirtualParts.getSelectedRow();
//							if (selected >= 0)
//							{
//								Part part = list.get((Integer) tableOfVirtualParts.getModel().getValueAt(
//										tableOfVirtualParts.convertRowIndexToModel(selected), 5));
//								final Interactions interactions = partsHandler.GetInteractions(part);
//								if (interactions != null && interactions.getInteractions() != null)
//								{
//									TableModel dataModel = new AbstractTableModel()
//									{
//
//										private static final long	serialVersionUID	= 1L;
//
//										@Override
//										public int getColumnCount()
//										{
//											return 4;
//										}
//
//										@Override
//										public int getRowCount()
//										{
//											return interactions.getInteractions().size();
//										}
//
//										@Override
//										public Object getValueAt(int row, int col)
//										{
//											Interaction i = interactions.getInteractions().get(row);
//											switch (col)
//											{
//											case 0:
//												return i.getName();
//											case 1:
//												String parts = "";
//												for (String p : i.getParts())
//												{
//													parts += p + ", ";
//												}
//												return parts.substring(0, parts.length() - 2);
//											case 2:
//												return i.getInteractionType();
//											default:
//												return i.getDescription();
//											}
//										}
//
//										@Override
//										public String getColumnName(int col)
//										{
//											switch (col)
//											{
//											case 0:
//												return "ID";
//											case 1:
//												return "Parts";
//											case 2:
//												return "Type";
//											default:
//												return "Description";
//											}
//										}
//									};
//									JTable tableOfInteractions = new JTable(dataModel);
//									tableOfInteractions.setAutoCreateRowSorter(true);
//									tableOfInteractions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//									tableOfInteractions.getColumnModel().getColumn(0).setPreferredWidth(150);
//									tableOfInteractions.getColumnModel().getColumn(1).setPreferredWidth(150);
//									tableOfInteractions.getColumnModel().getColumn(2).setPreferredWidth(150);
//									tableOfInteractions.getColumnModel().getColumn(3).setPreferredWidth(150);
//									JScrollPane ScrollInteractions = new JScrollPane();
//									ScrollInteractions.setMinimumSize(new Dimension(520, 150));
//									ScrollInteractions.setPreferredSize(new Dimension(552, 150));
//									ScrollInteractions.setViewportView(tableOfInteractions);
//									JOptionPane.showMessageDialog(frame, ScrollInteractions, "Interactions for Part " + part.getName(),
//											JOptionPane.PLAIN_MESSAGE);
//								}
//								else
//								{
//									JOptionPane.showMessageDialog(frame, "There are no interactions associated with this part in the repository.",
//											"No Interactions", JOptionPane.ERROR_MESSAGE);
//								}
//							}
//						}
//					}
//					catch (IOException e)
//					{
//						e.printStackTrace();
//					}
//					catch (XMLStreamException e)
//					{
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void mouseEntered(MouseEvent arg0)
//				{
//				}
//
//				@Override
//				public void mouseExited(MouseEvent arg0)
//				{
//				}
//
//				@Override
//				public void mousePressed(MouseEvent arg0)
//				{
//				}
//
//				@Override
//				public void mouseReleased(MouseEvent arg0)
//				{
//				}
//			});
//			JLabel TextVirtualParts = new JLabel("List of Virtual Parts:");
//			JLabel DoubleClick = new JLabel("Double click on a part to view its interactions.");
//			JScrollPane ScrollVirtualParts = new JScrollPane();
//			ScrollVirtualParts.setMinimumSize(new Dimension(520, 250));
//			ScrollVirtualParts.setPreferredSize(new Dimension(552, 250));
//			ScrollVirtualParts.setViewportView(tableOfVirtualParts);
//			labelPanel.add(TextVirtualParts, "North");
//			labelPanel.add(DoubleClick, "Center");
//			virtualPartsPanel.add(labelPanel, "North");
//			virtualPartsPanel.add(ScrollVirtualParts, "Center");
//			Object[] options = { "Import Part", "Import Part's Interactions", "Cancel" };
//			showVirtualPartImportOption(virtualPartsPanel, options, tableOfVirtualParts, partsHandler);
//		}
//	}
//
//	private void showVirtualPartImportOption(JPanel virtualPartsPanel, Object[] options, JTable tableOfVirtualParts, PartsHandler partsHandler)
//	{
//		try
//		{
//			List<Part> list = allVirtualParts.getParts();
//			int value = JOptionPane.showOptionDialog(frame, virtualPartsPanel, "List of Virtual Parts", JOptionPane.YES_NO_CANCEL_OPTION,
//					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//			int selected = tableOfVirtualParts.getSelectedRow();
//			if (selected >= 0)
//			{
//				Part part = list.get((Integer) tableOfVirtualParts.getModel().getValueAt(tableOfVirtualParts.convertRowIndexToModel(selected), 5));
//				if (value == JOptionPane.YES_OPTION)
//				{
//					SBMLDocument sbmlDocument = partsHandler.GetModel(part);
//					if (sbmlDocument != null)
//					{
//						SBMLWriter writer = new SBMLWriter();
//						writer.writeSBMLToFile(sbmlDocument, root + separator + part.getName() + ".xml.temp");
//						SBMLDocument document = SBMLutilities.readSBML(root + separator + part.getName() + ".xml.temp");
//						SBMLutilities.checkModelCompleteness(document);
//						SBMLutilities.check(root + separator + part.getName() + ".xml.temp", document, false);
//						String newFile = part.getName() + ".xml";
//						newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
//						if (Character.isDigit(newFile.charAt(0)))
//						{
//							newFile = "M" + newFile;
//						}
//						if (document != null)
//						{
//							if (document.getModel().isSetId())
//							{
//								newFile = document.getModel().getId();
//							}
//							else
//							{
//								document.getModel().setId(newFile.replace(".xml", ""));
//							}
//							document.enablePackage(LayoutConstants.namespaceURI);
//							document.enablePackage(CompConstants.namespaceURI);
//							document.enablePackage(FBCConstants.namespaceURI);
//							FBCModelPlugin fbc = SBMLutilities.getFBCModelPlugin(document.getModel());
//							fbc.setStrict(false);
//							document.enablePackage(ArraysConstants.namespaceURI);
//
//							CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
//							CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
//
//							if (documentComp.getListOfModelDefinitions().size() > 0 || documentComp.getListOfExternalModelDefinitions().size() > 0)
//							{
//								if (!extractModelDefinitions(documentComp, documentCompModel))
//								{
//									JOptionPane.showMessageDialog(frame, "Unable to extract model definitions from the model.",
//											"Unable to Extract Model Definitions", JOptionPane.ERROR_MESSAGE);
//								}
//							}
//							SBMLutilities.updateReplacementsDeletions(root, document, documentComp, documentCompModel);
//							if (document.getModel().getId() == null || document.getModel().getId().equals(""))
//							{
//								document.getModel().setId(newFile.replace(".xml", ""));
//							}
//							else
//							{
//								newFile = document.getModel().getId() + ".xml";
//							}
//							if (overwrite(root + separator + newFile, newFile))
//							{
//								writer.writeSBMLToFile(document, root + separator + newFile);
//								addToTree(newFile);
//								openSBML(root + separator + newFile);
//							}
//							new File(root + separator + part.getName() + ".xml.temp").delete();
//						}
//					}
//					else
//					{
//						JOptionPane.showMessageDialog(frame, "There is no SBML model associated with this part in the repository.", "No SBML Model",
//								JOptionPane.ERROR_MESSAGE);
//						showVirtualPartImportOption(virtualPartsPanel, options, tableOfVirtualParts, partsHandler);
//					}
//				}
//				if (value == JOptionPane.NO_OPTION)
//				{
//					Interactions interactions = partsHandler.GetInteractions(part);
//					if (interactions != null && interactions.getInteractions() != null)
//					{
//						for (Interaction interaction : interactions.getInteractions())
//						{
//							SBMLHandler sbmlHandler = new SBMLHandler();
//							SBMLDocument sbmlContainer = sbmlHandler.GetSBMLTemplateModel(interaction.getName() + "_model");
//							ModelBuilder modelBuilder = new ModelBuilder(sbmlContainer);
//							SBMLDocument sbmlDocumentPart = partsHandler.GetModel(part);
//							modelBuilder.Add(sbmlDocumentPart);
//							for (String p : interaction.getParts())
//							{
//								if (!p.equals(part.getName()))
//								{
//									for (Part tempPart : list)
//									{
//										if (p.equals(tempPart.getName()))
//										{
//											SBMLDocument sbmlDocument = partsHandler.GetModel(tempPart);
//											if (sbmlDocument != null)
//											{
//												modelBuilder.Add(sbmlDocument);
//											}
//										}
//									}
//								}
//							}
//							SBMLDocument sbmlDocument = partsHandler.GetInteractionModel(interaction);
//							if (sbmlDocument != null)
//							{
//								modelBuilder.Add(sbmlDocument);
//								SBMLWriter writer = new SBMLWriter();
//								writer.writeSBMLToFile(modelBuilder.GetModel(), root + separator + interaction.getName() + ".xml.temp");
//								SBMLDocument document = SBMLutilities.readSBML(root + separator + interaction.getName() + ".xml.temp");
//								String newFile = interaction.getName() + ".xml";
//								newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
//								if (Character.isDigit(newFile.charAt(0)))
//								{
//									newFile = "M" + newFile;
//								}
//								if (document != null)
//								{
//									if (document.getModel().isSetId())
//									{
//										newFile = document.getModel().getId();
//									}
//									else
//									{
//										document.getModel().setId(newFile.replace(".xml", ""));
//									}
//									document.enablePackage(LayoutConstants.namespaceURI);
//									document.enablePackage(CompConstants.namespaceURI);
//									document.enablePackage(FBCConstants.namespaceURI);
//									CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
//									CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
//									if (documentComp.getListOfModelDefinitions().size() > 0
//											|| documentComp.getListOfExternalModelDefinitions().size() > 0)
//									{
//										if (!extractModelDefinitions(documentComp, documentCompModel))
//										{
//											JOptionPane.showMessageDialog(frame, "Unable to extract model definitions from the model.",
//													"Unable to Extract Model Definitions", JOptionPane.ERROR_MESSAGE);
//										}
//									}
//									SBMLutilities.updateReplacementsDeletions(root, document, documentComp, documentCompModel);
//									if (document.getModel().getId() == null || document.getModel().getId().equals(""))
//									{
//										document.getModel().setId(newFile.replace(".xml", ""));
//									}
//									else
//									{
//										newFile = document.getModel().getId() + ".xml";
//									}
//									if (overwrite(root + separator + newFile, newFile))
//									{
//										writer.writeSBMLToFile(document, root + separator + newFile);
//										addToTree(newFile);
//										openSBML(root + separator + newFile);
//									}
//									new File(root + separator + interaction.getName() + ".xml.temp").delete();
//								}
//							}
//						}
//					}
//					else
//					{
//						JOptionPane.showMessageDialog(frame, "There are no interactions associated with this part in the repository.",
//								"No Interactions", JOptionPane.ERROR_MESSAGE);
//					}
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}

	private void createLPN()
	{
		try
		{
			String lhpnName = JOptionPane.showInputDialog(frame, "Enter LPN Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
			if (lhpnName != null && !lhpnName.trim().equals(""))
			{
				lhpnName = lhpnName.trim();
				if (lhpnName.length() > 3)
				{
					if (!lhpnName.substring(lhpnName.length() - 4).equals(".lpn"))
					{
						lhpnName += ".lpn";
					}
				}
				else
				{
					lhpnName += ".lpn";
				}
				String modelID = "";
				if (lhpnName.length() > 3)
				{
					if (lhpnName.substring(lhpnName.length() - 4).equals(".lpn"))
					{
						modelID = lhpnName.substring(0, lhpnName.length() - 4);
					}
					else
					{
						modelID = lhpnName.substring(0, lhpnName.length() - 3);
					}
				}
				if (!(IDpat.matcher(modelID).matches()))
				{
					JOptionPane.showMessageDialog(frame,
							"A model ID can only contain letters, digits, and underscores.\nIt also cannot start with a digit.", "Invalid ID",
							JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					if (overwrite(root + GlobalConstants.separator + lhpnName, lhpnName))
					{
						File f = new File(root + GlobalConstants.separator + lhpnName);
						f.createNewFile();
						LPN lpn = new LPN();
						lpn.addObserver(this);
						lpn.save(f.getAbsolutePath());
						int i = getTab(f.getName());
						if (i != -1)
						{
							tab.remove(i);
						}
						LHPNEditor lhpn = new LHPNEditor(root + GlobalConstants.separator, f.getName(), null, this);
						// lhpn.addMouseListener(this);
						addTab(f.getName(), lhpn, "LHPN Editor");
						addToTree(f.getName());
					}
				}
			}
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void importLPN()
	{
		File importFile;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.import_dir", "").equals(""))
		{
			importFile = null;
		}
		else
		{
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import LPN", -1);
		if ((filename.length() > 1 && !filename.substring(filename.length() - 2, filename.length()).equals(".g"))
				&& (filename.length() > 3 && !filename.substring(filename.length() - 4, filename.length()).equals(".lpn")))
		{
			JOptionPane.showMessageDialog(frame, "You must select a valid LPN file to import.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (!filename.equals(""))
		{
			biosimrc.put("biosim.general.import_dir", filename);
			String[] file = filename.split(GlobalConstants.separator);
			try
			{
				if (new File(filename).exists())
				{
					file[file.length - 1] = file[file.length - 1].replaceAll("[^a-zA-Z0-9_.]+", "_");
					if (Character.isDigit(file[file.length - 1].charAt(0)))
					{
						file[file.length - 1] = "M" + file[file.length - 1];
					}
					if (checkFiles(root + GlobalConstants.separator + file[file.length - 1], filename.trim()))
					{
						if (overwrite(root + GlobalConstants.separator + file[file.length - 1], file[file.length - 1]))
						{
							// Identify which LPN format is imported.
							BufferedReader input = new BufferedReader(new FileReader(filename));
							String str;
							boolean lpnUSF = false;
							while ((str = input.readLine()) != null)
							{
								if (str.startsWith("."))
								{
									break;
								}
								else if (str.startsWith("<"))
								{
									lpnUSF = true;
									break;
								}/*
								else
								{
									JOptionPane.showMessageDialog(frame, "LPN file format is not valid.", "Error", JOptionPane.ERROR_MESSAGE);
									break;
								}*/

							}
							input.close();
							if (!lpnUSF)
							{
								String outFileName = file[file.length - 1];

								if (/*!lema &&*/ !atacs)
								{
									Translator t1 = new Translator();
									t1.convertLPN2SBML(filename, "");
									t1.setFilename(root + GlobalConstants.separator + outFileName.replace(".lpn", ".xml"));
									t1.outputSBML();
									outFileName = outFileName.replace(".lpn", ".xml");
								}
								else
								{
									FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + outFileName));
									FileInputStream in = new FileInputStream(new File(filename));
									// log.addText(filename);
									int read = in.read();
									while (read != -1)
									{
										out.write(read);
										read = in.read();
									}
									in.close();
									out.close();
								}
								addToTree(outFileName);
							}
							else
							{
								ANTLRFileStream in = new ANTLRFileStream(filename);
								PlatuGrammarLexer lexer = new PlatuGrammarLexer(in);
								TokenStream tokenStream = new CommonTokenStream(lexer);
								PlatuGrammarParser antlrParser = new PlatuGrammarParser(tokenStream);
								Set<LPN> lpnSet = antlrParser.lpn();
								for (LPN lpn : lpnSet)
								{
									lpn.save(root + GlobalConstants.separator + lpn.getLabel() + ".lpn");
									addToTree(lpn.getLabel() + ".lpn");
								}
							}
						}
					}
				}
				if (filename.substring(filename.length() - 2, filename.length()).equals(".g"))
				{
					// log.addText(filename + file[file.length - 1]);
					File work = new File(root);
					String oldName = root + GlobalConstants.separator + file[file.length - 1];
					// String newName = oldName.replace(".lpn",
					// "_NEW.g");
					Process atacs = Runtime.getRuntime().exec("atacs -lgsl " + oldName, null, work);
					atacs.waitFor();
					String lpnName = oldName.replace(".g", ".lpn");
					String newName = oldName.replace(".g", "_NEW.lpn");
					atacs = Runtime.getRuntime().exec("atacs -llsl " + lpnName, null, work);
					atacs.waitFor();
					lpnName = lpnName.replaceAll("[^a-zA-Z0-9_.]+", "_");
					if (Character.isDigit(lpnName.charAt(0)))
					{
						lpnName = "M" + lpnName;
					}
					FileOutputStream out = new FileOutputStream(new File(lpnName));
					FileInputStream in = new FileInputStream(new File(newName));
					int read = in.read();
					while (read != -1)
					{
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
					new File(newName).delete();
					addToTree(file[file.length - 1].replace(".g", ".lpn"));
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createModel(boolean grid)
	{
		if (root != null)
		{
			try
			{

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
				if (okCancel == JOptionPane.OK_OPTION)
				{

					modelId = modelChooser.getText();
				}
				else
				{
					return;
				}

				// String simName = JOptionPane.showInputDialog(frame,
				// "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
				if (modelId != null && !modelId.trim().equals(""))
				{
					modelId = modelId.trim();
					if (modelId.length() > 3)
					{
						if (!modelId.substring(modelId.length() - 4).equals(".xml"))
						{
							modelId += ".xml";
						}
					}
					else
					{
						modelId += ".xml";
					}
					if (!(IDpat.matcher(modelId.replace(".xml", "")).matches()))
					{
						JOptionPane.showMessageDialog(frame,
								"A model ID can only contain letters, digits, and underscores.\nIt also cannot start with a digit.", "Invalid ID",
								JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						if (overwrite(root + GlobalConstants.separator + modelId, modelId))
						{
							BioModel bioModel = new BioModel(root);
							bioModel.createSBMLDocument(modelId.replace(".xml", ""), grid, lema);
							bioModel.save(root + GlobalConstants.separator + modelId);
							int i = getTab(modelId);
							if (i != -1)
							{
								tab.remove(i);
							}
							ModelEditor modelEditor = new ModelEditor(root + GlobalConstants.separator, modelId, this, log, false, null, null, null, false, grid);
							modelEditor.save(false);
							addTab(modelId, modelEditor, "Model Editor");
							addToTree(modelId);
						}
					}
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private String importSBMLDocument(String file,SBMLDocument document) throws SBMLException, FileNotFoundException, XMLStreamException {
		String newFile = null;
		SBMLutilities.checkModelCompleteness(document,true);
		Utils.check(null, document, false);
		newFile = file;
		newFile = newFile.replaceAll("[^a-zA-Z0-9_.]+", "_");
		if (Character.isDigit(newFile.charAt(0))) {
			newFile = "M" + newFile;
		}
		if (document != null) {
			if (document.getModel().isSetId()) {
				newFile = document.getModel().getId();
			} else {
				document.getModel().setId(newFile.replace(".xml", ""));
			}
			document.enablePackage(LayoutConstants.namespaceURI);
			document.enablePackage(CompConstants.namespaceURI);
			document.enablePackage(FBCConstants.namespaceURI);
			SBMLutilities.getFBCModelPlugin(document.getModel(),true);
			document.enablePackage(ArraysConstants.namespaceURI);
			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
			CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
			if (documentComp.getListOfModelDefinitions().size() > 0 || 
					documentComp.getListOfExternalModelDefinitions().size() > 0) {
				if (!extractModelDefinitions(documentComp, documentCompModel)) {
					return null;
				}
			}
			try {
        SBMLutilities.updateReplacementsDeletions(root, document, documentComp, documentCompModel);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
			SBMLWriter writer = new SBMLWriter();
			if (document.getModel().getId() == null || document.getModel().getId().equals("")) {
				document.getModel().setId(newFile.replace(".xml", ""));
			} else {
				newFile = document.getModel().getId() + ".xml";
			}
			if (overwrite(root + GlobalConstants.separator + newFile, newFile))	{
				writer.writeSBMLToFile(document, root + GlobalConstants.separator + newFile);
				addToTree(newFile);
				openSBML(root + GlobalConstants.separator + newFile);
			}
		}
		return newFile;
	}

	private String importSBML(String filename)
	{
		String newFile = null;
		Preferences biosimrc = Preferences.userRoot();
		if (filename == null)
		{
			File importFile;
			if (biosimrc.get("biosim.general.import_dir", "").equals("")) {
				importFile = null;
			} else {
				importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
			}
			filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_AND_DIRECTORIES, "Import SBML", -1);
		}
		if (!filename.trim().equals(""))
		{
			biosimrc.put("biosim.general.import_dir", filename.trim());
			if (new File(filename.trim()).isDirectory())
			{
				for (String s : new File(filename.trim()).list())
				{
					if (s.endsWith(".xml") || s.endsWith(".sbml"))
					{
						try
						{
							SBMLDocument document = SBMLutilities.readSBML(filename.trim() + GlobalConstants.separator + s);
							SBMLutilities.checkModelCompleteness(document,true);
							if (overwrite(root + GlobalConstants.separator + s, s))
							{
								Utils.check(filename.trim(), document, false);
								SBMLWriter writer = new SBMLWriter();
								s = s.replaceAll("[^a-zA-Z0-9_.]+", "_");
								writer.writeSBMLToFile(document, root + GlobalConstants.separator + s);
							}
						}
						catch (Exception e1)
						{
							e1.printStackTrace();
							JOptionPane.showMessageDialog(frame, "Unable to import files.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					addToTree(s);
				}
			}
			else
			{
				try
				{
					SBMLDocument document = SBMLutilities.readSBML(filename.trim());
					if (document==null) return null;
					String[] file = filename.trim().split(GlobalConstants.separator);
					newFile = importSBMLDocument(file[file.length-1],document);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return newFile;
	}

	private void performAnalysis(String modelId, String simName) throws Exception
	{
		String sbmlFile = root + GlobalConstants.separator + modelId + ".xml";
		String modelFileName = sbmlFile.split(GlobalConstants.separator)[sbmlFile.split(GlobalConstants.separator).length - 1];
		String sbmlFileProp;
		sbmlFileProp = root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFileName;
		new FileOutputStream(new File(sbmlFileProp)).close();
		try
		{
			FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".sim"));
			out.write((modelFileName + "\n").getBytes());
			out.close();
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		addToTree(simName);
		JTabbedPane simTab = new JTabbedPane();
		simTab.addMouseListener(this);
		AnalysisView analysisView = new AnalysisView(this, log, simTab, null, root, simName, modelFileName);
		simTab.addTab("Simulation Options", analysisView);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
		simTab.addTab("Advanced Options", analysisView.getAdvanced());
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		String gcmFile = modelFileName.replace(".xml", ".gcm");
		ModelEditor modelEditor = new ModelEditor(root + GlobalConstants.separator, gcmFile, this, log, true, simName, root + GlobalConstants.separator + simName + GlobalConstants.separator
				+ simName + ".sim", analysisView, false, false);
		analysisView.setModelEditor(modelEditor);
		ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(),
				sedmlDocument,simName);
		modelEditor.setElementsPanel(elementsPanel);
		addModelViewTab(analysisView, simTab, modelEditor);
		simTab.addTab("Parameters", modelEditor);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Model Editor");
		modelEditor.createSBML("", ".", "");
		new AnalysisThread(analysisView).start(".", true);
		Graph tsdGraph;
		if (new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".grf").exists())
		{
			tsdGraph = analysisView.createGraph(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".grf");
		}
		else
		{
			tsdGraph = analysisView.createGraph(null);
		}
		simTab.addTab("TSD Graph", tsdGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
		Graph probGraph;
		if (new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".prb").exists())
		{
			probGraph = analysisView.createProbGraph(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".prb");
		}
		else
		{
			probGraph = analysisView.createProbGraph(null);
		}
		simTab.addTab("Histogram", probGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Histogram");
		addTab(simName, simTab, null);
	}
	
	private HashMap<String,String> importModels(String path,SedML sedml,ArchiveComponents ac) 
			throws SBMLException, FileNotFoundException, XMLStreamException, URISyntaxException {
		HashMap<String, String> modelMap = new HashMap<String, String>();
		for (org.jlibsedml.Model model : sedml.getModels())
		{
			if (modelMap.get(model.getSource())!=null) {
				//model.setSource(modelMap.get(model.getSource()));
				modelMap.put(model.getId(), modelMap.get(model.getSource()));
			} else {
				String newFile = null;
				if (model.getSource().startsWith("urn:miriam:biomodels.db")) {
					BioModelsModelsRetriever retriever = new BioModelsModelsRetriever();
					String docStr = retriever.getModelXMLFor(URI.create(model.getSource()));
					SBMLDocument doc = SBMLReader.read(docStr);
					newFile = importSBMLDocument("model",doc);
				} else if (model.getSource().startsWith("http://")) {
					URLResourceRetriever retriever = new URLResourceRetriever();
					String docStr = retriever.getModelXMLFor(URI.create(model.getSource()));
					SBMLDocument doc = SBMLReader.read(docStr);
					newFile = importSBMLDocument("model",doc);
				} else {
					if (ac==null) {
						String sbmlFile = path + model.getSource();
						newFile = importSBML(sbmlFile);
					} else {
						ArchiveModelResolver archiveModelResolver = new ArchiveModelResolver(ac);
						String docStr = archiveModelResolver.getModelXMLFor(model.getSourceURI());
//						for (IModelContent mod : ac.getModelFiles()) {
//							System.out.println(mod.getName());
//							System.out.println(mod.getContents());
//						}
//						System.out.println("Reading "+model.getSourceURI());
//						System.out.println(docStr);
						SBMLDocument doc = SBMLReader.read(docStr);
						newFile = importSBMLDocument("model",doc);	
					}
				}
				if (newFile == null)
				{
					continue;
				}
				sedmlDocument.getSedMLModel().getModelWithId(model.getId()).setSource(newFile);
				model.setSource(newFile);
				modelMap.put(model.getId(), newFile);
			}
		}
		return modelMap;
	}
	
	private void importTasks(SedML sedml,HashMap<String,String> modelMap) throws Exception {
		for (AbstractTask task : sedml.getTasks())
		{
			org.jlibsedml.Model model = sedml.getModelWithId(task.getModelReference());
			if (!model.getId().equals(task.getId()+"_model")) {
				org.jlibsedml.Model oldModel = sedml.getModelWithId(task.getId()+"_model");
				if (oldModel!=null) {
					sedml.removeModel(oldModel);
				}
				model = SEDMLutilities.copyModel(model,task.getId()+"_model");
			}
			sedmlDocument.getSedMLModel().addModel(model);
			Task t = (Task)task;
			modelMap.put(model.getId(), modelMap.get(task.getModelReference()));
			t.setModelReference(model.getId());
			if (modelMap.containsKey(task.getModelReference()))
			{
				String modelId = modelMap.get(task.getModelReference()).replace(".xml", "");
				String analysisId = task.getId();
				if (overwrite(root + GlobalConstants.separator + analysisId, analysisId))
				{
					new File(root + GlobalConstants.separator + analysisId).mkdir();
					performAnalysis(modelId, analysisId);
				}
			}
		}
	}
	
	private void importOutputs(SedML sedml) throws Exception {
		for (Output output : sedml.getOutputs()) {
			if (output.isPlot2d())
			{
				Plot2D plot = (Plot2D) output;
				Properties graph = new Properties();
				try
				{
					FileOutputStream store = new FileOutputStream(new File(root + GlobalConstants.separator + plot.getId() + ".grf"));
					graph.store(store, "Graph Data");
					store.close();
					log.addText("Creating graph file:\n" + root + GlobalConstants.separator + plot.getId() + ".grf" + "\n");
					String graphFile = plot.getId() + ".grf";
					addToTree(graphFile);
					//								}
				}
				catch (Exception except)
				{
					JOptionPane.showMessageDialog(Gui.frame, "Unable To Save Graph!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (output.isReport())
			{
				Report report = (Report) output;
				Properties graph = new Properties();
				try
				{
					FileOutputStream store = new FileOutputStream(new File(root + GlobalConstants.separator + report.getId() + ".prb"));
					graph.store(store, "Probability Data");
					store.close();
					log.addText("Creating probability file:\n" + root + GlobalConstants.separator + report.getId() + ".prb"
							+ "\n");
					String graphFile = report.getId() + ".prb";
					addToTree(graphFile);

				}
				catch (Exception except)
				{
					JOptionPane.showMessageDialog(Gui.frame, "Unable To Save Histogram!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		for (Output output : sedml.getOutputs()) {
			if (output.isPlot2d()) {
				Plot2D plot = (Plot2D) output;
				String graphFile = plot.getId() + ".grf";
				for (int j = 0; j < tab.getTabCount(); j++)
				{
					if (getTitleAt(j).equals(graphFile))
					{
						removeTab(tab.getComponentAt(j));
					}
				}
				addTab(graphFile, new Graph(null, "Number of molecules", "title", "tsd.printer", root, 
						"Time", this, root + GlobalConstants.separator + graphFile, log, graphFile, true, false), "TSD Graph");
			} else if (output.isReport()) {
				Report report = (Report) output;
				String graphFile = report.getId() + ".prb";
				for (int j = 0; j < tab.getTabCount(); j++)
				{
					if (getTitleAt(j).equals(graphFile))
					{
						removeTab(tab.getComponentAt(j));
					}
				}
				addTab(graphFile, new Graph(null, "Percent", "title", "tsd.printer",
						root, "Time", this, root + GlobalConstants.separator + graphFile, log, graphFile, false, false), "Histogram");
			}
		}
	}
	
	public SBOLDocument getSBOLDocument() {
		readSBOLDocument();
		return sbolDocument;
	}
	
	private void readSBOLDocument() {
		String sbolFilename = root + GlobalConstants.separator + currentProjectId + ".sbol";
		File sbolFile = new File(sbolFilename);
		if (sbolFile.exists()) {
			try {
				sbolDocument = SBOLReader.read(sbolFilename);
				sbolDocument.setCreateDefaults(true);
				Preferences biosimrc = Preferences.userRoot();
				sbolDocument.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Unable to open project's SBOL library.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			sbolDocument = new SBOLDocument();
			sbolDocument.setCreateDefaults(true);
			Preferences biosimrc = Preferences.userRoot();
			sbolDocument.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
			writeSBOLDocument();
		}
	}
	
	public void writeSBOLDocument() {
		String sbolFilename = root + GlobalConstants.separator + currentProjectId + ".sbol";
		try {
			sbolDocument.write(sbolFilename);
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unable to write SBOL file.", "Error", JOptionPane.ERROR_MESSAGE);

		}
		catch (SBOLConversionException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unable to write SBOL file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public SEDMLDocument getSEDMLDocument() {
		//readSEDMLDocument();
		return sedmlDocument;
	}
	
	private void readSEDMLDocument() {
		String sedmlFilename = root + GlobalConstants.separator + currentProjectId + ".sedml";
		File sedmlFile = new File(sedmlFilename);
		if (sedmlFile.exists())	{
			try {
				long time1 = System.nanoTime();
				sedmlDocument = Libsedml.readDocument(sedmlFile);
				long time2 = System.nanoTime();
				String time = Run.createTimeString(time1, time2);
				System.out.println(time);
			}
			catch (XMLException exception) {
				JOptionPane.showMessageDialog(frame, "Unable to open project's SED-ML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			sedmlDocument = new SEDMLDocument(1,2);
			writeSEDMLDocument();
		}
	}
	
	public void writeSEDMLDocument() {
		String sedmlFilename = root + GlobalConstants.separator + currentProjectId + ".sedml";
		sedmlDocument.writeDocument(new File(sedmlFilename));
	}
	
	private void importSEDMLDocument(String path,SEDMLDocument sedmlDoc,ArchiveComponents ac) throws Exception {
 		SedML sedml = sedmlDoc.getSedMLModel();
		SedML sedmlModel = sedmlDocument.getSedMLModel();
		HashMap<String,String> modelMap = null;
		for (org.jlibsedml.Model model : sedml.getModels()) {
			if (sedmlModel.getModelWithId(model.getId())!=null) {
				sedmlModel.removeModel(sedmlModel.getModelWithId(model.getId()));
			}
			sedmlModel.addModel(SEDMLutilities.copyModel(model,model.getId()));
		}
		modelMap = importModels(path,sedml,ac);
		for (Simulation simulation : sedml.getSimulations()) {
			if (sedmlModel.getSimulation(simulation.getId())!=null) {
				sedmlModel.removeSimulation(sedmlModel.getSimulation(simulation.getId()));
			}
			sedmlModel.addSimulation(SEDMLutilities.copySimulation(simulation,simulation.getId()));
 		}
		for (AbstractTask task : sedml.getTasks()) {
			if (sedmlModel.getTaskWithId(task.getId())!=null) {
				sedmlModel.removeTask(sedmlModel.getTaskWithId(task.getId()));
			}
			sedmlModel.addTask(SEDMLutilities.copyTask(task,task.getId()));
		}
		importTasks(sedml,modelMap);
		for (DataGenerator dataGenerator : sedml.getDataGenerators()) {
			if (sedmlModel.getDataGeneratorWithId(dataGenerator.getId())!=null) {
				sedmlModel.removeDataGenerator(sedmlModel.getDataGeneratorWithId(dataGenerator.getId()));
			}
			sedmlModel.addDataGenerator(SEDMLutilities.copyDataGenerator(dataGenerator,dataGenerator.getId()));
		}
		for (Output output : sedml.getOutputs()) {
			if (sedmlModel.getOutputWithId(output.getId())!=null) {
				sedmlModel.removeOutput(sedmlModel.getOutputWithId(output.getId()));
			}
			sedmlModel.addOutput(SEDMLutilities.copyOutput(output,output.getId()));
		}
		importOutputs(sedml);
		writeSEDMLDocument();
	}

	private void importSEDML()
	{
		Preferences biosimrc = Preferences.userRoot();
		File importFile;
		if (biosimrc.get("biosim.general.import_dir", "").equals(""))
		{
			importFile = null;
		}
		else
		{
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import SED-ML", -1);
		if (!filename.trim().equals(""))
		{
			biosimrc.put("biosim.general.import_dir", filename.trim());
			try
			{
				SEDMLDocument sedmlDoc = null;
				ArchiveComponents ac = null;
				if (filename.trim().endsWith(".sedx")) { 
					ac = Libsedml.readSEDMLArchive(new FileInputStream(filename.trim()));
					sedmlDoc = ac.getSedmlDocument();
				} else {
					File sedmlFile = new File(filename.trim());
					sedmlDoc = Libsedml.readDocument(sedmlFile);
				}
				sedmlDoc.validate();
				if (sedmlDoc.hasErrors())
				{
					List<SedMLError> errors = sedmlDoc.getErrors();
					//final JFrame f = new JFrame("SED-ML Errors and Warnings");
					JTextArea messageArea = new JTextArea();
					messageArea.append("Imported SED-ML file contains the errors listed below. ");
					messageArea.append("It is recommended that you fix them before performing this analysis or you may get unexpected results.\n\n");
					for (int i = 0; i < errors.size(); i++)
					{
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
					JOptionPane.showMessageDialog(Gui.frame, scroll, "SED-ML Errors and Warnings", JOptionPane.ERROR_MESSAGE);
				}
				String path = filename.substring(0, filename.lastIndexOf(GlobalConstants.separator));
				importSEDMLDocument(path,sedmlDoc,ac);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import SED-ML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
//	public void exportSEDML() {
//		File lastFilePath;
//		Preferences biosimrc = Preferences.userRoot();
//		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
//			lastFilePath = null;
//		}
//		else {
//			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
//		}
//		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export SED-ML", -1);
//		if (!exportPath.equals("")) {
//			String dir = exportPath.substring(0, exportPath.lastIndexOf(Gui.separator));
//			biosimrc.put("biosim.general.export_dir",dir);
//			log.addText("Exporting SED-ML file:\n" + exportPath + "\n");
//			List<IModelContent> models = new ArrayList<IModelContent>();
//			for (String s : new File(root).list()) {
//				if (s.endsWith(".xml")) {
//					File modelFile = new File(root + separator + s); 
//					FileModelContent fmc = new FileModelContent(modelFile);
//					//System.out.println(fmc.getName());
//					//System.out.println(fmc.getContents());
//					models.add(fmc);
//				}
//			}
//			try {
//				byte [] sedx = Libsedml.writeSEDMLArchive(new ArchiveComponents(models,sedmlDocument),
//						root.split(Gui.separator)[root.split(Gui.separator).length-1]);
//				File file = new File(exportPath);
//				FileOutputStream fos = new FileOutputStream(file);
//				fos.write(sedx);
//				fos.flush();
//				fos.close();
//			}
//			catch (Exception e) {
//				JOptionPane.showMessageDialog(frame, "Unable to export SED-ML file.", "Error", JOptionPane.ERROR_MESSAGE);
//			}
//		}
//	}

	public void exportCombineArchive() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = frontend.main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export Archive", -1);
		if (!exportPath.equals("")) {
			String dir = exportPath.substring(0, exportPath.lastIndexOf(GlobalConstants.separator));
			biosimrc.put("biosim.general.export_dir",dir);
			log.addText("Exporting COMBINE archive file:\n" + exportPath + "\n");
			File file = new File(exportPath);
			if (file.exists()) {
				file.delete();
			}
			try {
				CombineArchive archive = new CombineArchive(file);
				File baseDir = new File(root);
				for (String s : new File(root).list()) {
					if (s.endsWith(".xml")) {
						File modelFile = new File(root + GlobalConstants.separator + s); 
						archive.addEntry(baseDir, modelFile, 
								URI.create("http://identifiers.org/combine.specifications/sbml.level-3.version-1.core"));
					}
					// TODO: add other file types
				}
				File sedmlFile = new File(root + GlobalConstants.separator + root.split(GlobalConstants.separator)[root.split(GlobalConstants.separator).length-1]+".sedml");
				archive.addEntry(baseDir, sedmlFile,
						URI.create("http://identifiers.org/combine.specifications/sed-ml.level-1.version-2"),
						true);
				archive.pack();
				archive.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to export SED-ML file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private boolean checkSBOL(SBOLDocument sbolDoc,boolean bestPractice) 
	{
		SBOLValidate.validateSBOL(sbolDoc,true,true,bestPractice);
		if (SBOLReader.getNumErrors()>0 || SBOLValidate.getNumErrors()>0)
		{
			JTextArea messageArea = new JTextArea();
			messageArea.append("SBOL contains the errors listed below. ");
			messageArea.append("These need to be fixed before SBOL can be added to your project.\n\n");
			for (String error : SBOLReader.getErrors()) 
			{
				messageArea.append(error + "\n");
			}
			for (String error : SBOLValidate.getErrors()) 
			{
				messageArea.append(error + "\n");
			}
			messageArea.setLineWrap(true);
			messageArea.setEditable(false);
			messageArea.setSelectionStart(0);
			messageArea.setSelectionEnd(0);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(600, 600));
			scroll.setPreferredSize(new Dimension(600, 600));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(Gui.frame, scroll, "SBOL Errors and Warnings", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	

	private void importSBOL(String fileType)
	{
		Preferences biosimrc = Preferences.userRoot();
		File importFile;
		if (biosimrc.get("biosim.general.import_dir", "").equals(""))
		{
			importFile = null;
		}
		else
		{
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, fileType, -1);
		if (!filename.trim().equals(""))
		{
			biosimrc.put("biosim.general.import_dir", filename.trim());
//			String[] file = filename.trim().split(separator);
			try
			{
				File sbolFile = new File(filename.trim());
				SBOLReader.setKeepGoing(true);
				SBOLReader.setURIPrefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
				SBOLDocument sbolDoc = SBOLReader.read(sbolFile);
				sbolDoc.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
				if (!checkSBOL(sbolDoc,false)) return;
				log.addText("Importing " + sbolFile + " into the project's SBOL library.");
				generateSBMLFromSBOL(filename.trim());
				getSBOLDocument().read(sbolFile);
				writeSBOLDocument();
//				String newFile = file[file.length-1].replace(".xml", "").replace(".rdf", "").replace(".gb", "").replace(".fasta", "")+".sbol";
//				SBOLWriter.write(sbolDoc, root + separator + newFile);
//				addToTree(newFile);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (SBOLConversionException e) {
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (SBOLValidationException e) {
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importFile(String fileType, String extension1, String extension2)
	{
		File importFile;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.import_dir", "").equals(""))
		{
			importFile = null;
		}
		else
		{
			importFile = new File(biosimrc.get("biosim.general.import_dir", ""));
		}
		String filename = Utility.browse(frame, importFile, null, JFileChooser.FILES_ONLY, "Import " + fileType, -1);
		if (filename.length() > 1 && !filename.endsWith(extension1) && !filename.endsWith(extension2))
		{
			JOptionPane.showMessageDialog(frame, "You must select a valid " + fileType + " file to import.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (!filename.equals(""))
		{
			biosimrc.put("biosim.general.import_dir", filename);
			String[] file = filename.split(GlobalConstants.separator);
			try
			{
				file[file.length - 1] = file[file.length - 1].replaceAll("[^a-zA-Z0-9_.]+", "_");
				file[file.length - 1] = file[file.length - 1].replaceAll(extension2, extension1);
				if (checkFiles(root + GlobalConstants.separator + file[file.length - 1], filename.trim()))
				{
					if (overwrite(root + GlobalConstants.separator + file[file.length - 1], file[file.length - 1]))
					{
						FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + file[file.length - 1]));
						FileInputStream in = new FileInputStream(new File(filename));
						int read = in.read();
						while (read != -1)
						{
							out.write(read);
							read = in.read();
						}
						in.close();
						out.close();
						addToTree(file[file.length - 1]);
					}
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Unable to import file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createGraph()
	{
		String graphName = JOptionPane.showInputDialog(frame, "Enter A Name For The TSD Graph:", "TSD Graph Name", JOptionPane.PLAIN_MESSAGE);
		if (graphName != null && !graphName.trim().equals(""))
		{
			graphName = graphName.trim();
			if (graphName.length() > 3)
			{
				if (!graphName.substring(graphName.length() - 4).equals(".grf"))
				{
					graphName += ".grf";
				}
			}
			else
			{
				graphName += ".grf";
			}
			if (graphName.contains("__"))
			{
				JOptionPane.showMessageDialog(frame, "TSD Graph ID's are not allowed to include two consecutive underscores.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (overwrite(root + GlobalConstants.separator + graphName, graphName))
			{
				Graph g = new Graph(null, "Number of molecules", graphName.trim().substring(0, graphName.length() - 4), "tsd.printer", root, "Time",
						this, null, log, graphName.trim(), true, false);
				addTab(graphName.trim(), g, "TSD Graph");
				g.save();
				addToTree(graphName.trim());
			}
		}
	}

	private void createHistogram()
	{
		String graphName = JOptionPane.showInputDialog(frame, "Enter A Name For The Histogram:", "Histogram Name", JOptionPane.PLAIN_MESSAGE);
		if (graphName != null && !graphName.trim().equals(""))
		{
			graphName = graphName.trim();
			if (graphName.length() > 3)
			{
				if (!graphName.substring(graphName.length() - 4).equals(".prb"))
				{
					graphName += ".prb";
				}
			}
			else
			{
				graphName += ".prb";
			}
			if (graphName.contains("__"))
			{
				JOptionPane.showMessageDialog(frame, "Histogram ID's are not allowed to include two consecutive underscores.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (overwrite(root + GlobalConstants.separator + graphName, graphName))
			{
				Graph g = new Graph(null, "Number of Molecules", graphName.trim().substring(0, graphName.length() - 4), "tsd.printer", root, "Time",
						this, null, log, graphName.trim(), false, false);
				addTab(graphName.trim(), g, "Histogram");
				g.save();
				addToTree(graphName.trim());
			}
		}
	}

	private void createLearn(String modelFile)
	{
		if (root != null)
		{
			String modelFileName = modelFile.split(GlobalConstants.separator)[modelFile.split(GlobalConstants.separator).length - 1];
			String modelId = modelFileName.replace(".xml", "").replace(".lpn", "");
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			String lrnName = JOptionPane.showInputDialog(frame, "Enter Learn ID (default=" + modelId + "):", "Learn ID", JOptionPane.PLAIN_MESSAGE);
			if (lrnName == null)
			{
				return;
			}
			if (lrnName.equals(""))
			{
				lrnName = modelId;
			}
			lrnName = lrnName.trim();
			// try {
			if (overwrite(root + GlobalConstants.separator + lrnName, lrnName))
			{
				new File(root + GlobalConstants.separator + lrnName).mkdir();
				// new FileWriter(new File(root + separator +
				// lrnName + separator
				// +
				// ".lrn")).close();
				String sbmlFile = tree.getFile();
				String[] getFilename = sbmlFile.split(GlobalConstants.separator);
				String sbmlFileNoPath = getFilename[getFilename.length - 1];
				if (sbmlFileNoPath.endsWith(".vhd"))
				{
					try
					{
						File work = new File(root);
						Runtime.getRuntime().exec("atacs -lvsl " + sbmlFileNoPath, null, work);
						sbmlFileNoPath = sbmlFileNoPath.replace(".vhd", ".lpn");
						log.addText("atacs -lvsl " + sbmlFileNoPath + "\n");
					}
					catch (IOException e1)
					{
						JOptionPane.showMessageDialog(frame, "Unable to generate LPN from VHDL file!", "Error Generating File",
								JOptionPane.ERROR_MESSAGE);
					}
				}
				try
				{
					FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + lrnName.trim() + GlobalConstants.separator + lrnName.trim() + ".lrn"));
					if (lema)
					{
						out.write(("learn.file=" + sbmlFileNoPath + "\n").getBytes());
					}
					else
					{
						out.write(("genenet.file=" + sbmlFileNoPath + "\n").getBytes());
					}
					out.close();
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(frame, "Unable to save parameter file!", "Error Saving File", JOptionPane.ERROR_MESSAGE);
				}
				addToTree(lrnName);
				JTabbedPane lrnTab = new JTabbedPane();
				lrnTab.addMouseListener(this);
				DataManager data = new DataManager(root + GlobalConstants.separator + lrnName, this);
				lrnTab.addTab("Data Manager", data);
				lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
				if (lema)
				{
					LearnLPN learn = new LearnLPN(root + GlobalConstants.separator + lrnName, log, this);
					lrnTab.addTab("Learn Options", learn);
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
					lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
				}
				else
				{
					LearnGCM learn = new LearnGCM(root + GlobalConstants.separator + lrnName, log, this);
					lrnTab.addTab("Learn Options", learn);
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
					lrnTab.addTab("Parameter Estimator Options", learn.getParamEstimator());
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Parameter Estimator Options");
					lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
					lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
				}
				Graph tsdGraph;
				tsdGraph = new Graph(null, "Number of molecules", lrnName + " data", "tsd.printer", root + GlobalConstants.separator + lrnName, "Time", this, null,
						log, null, true, false);
				lrnTab.addTab("TSD Graph", tsdGraph);
				lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
				addTab(lrnName, lrnTab, null);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(frame, "You must open or create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewModel()
	{
		try
		{
			if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String[] findTheFile = filename.split("\\.");
				String theFile = findTheFile[0] + ".dot";
				File dot = new File(root + GlobalConstants.separator + theFile);
				dot.delete();
				LPN lhpn = new LPN();
				lhpn.addObserver(this);
				lhpn.load(tree.getFile());
				lhpn.printDot(root + GlobalConstants.separator + theFile);
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				if (dot.exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + GlobalConstants.separator + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String[] findTheFile = filename.split("\\.");
				String theFile = findTheFile[0] + ".dot";
				File dot = new File(root + GlobalConstants.separator + theFile);
				dot.delete();
				String cmd = "atacs -cPlgodpe " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process ATACS = exec.exec(cmd, null, work);
				ATACS.waitFor();
				log.addText("Executing:\n" + cmd);
				if (dot.exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + GlobalConstants.separator + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd"))
			{
				try
				{
					String vhdFile = tree.getFile();
					if (new File(vhdFile).exists())
					{
						File vhdlAmsFile = new File(vhdFile);
						BufferedReader input = new BufferedReader(new FileReader(vhdlAmsFile));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null)
						{
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
					else
					{
						JOptionPane.showMessageDialog(frame, "VHDL model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Unable to view VHDL model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams"))
			{
				try
				{
					String vamsFileName = tree.getFile();
					if (new File(vamsFileName).exists())
					{
						File vamsFile = new File(vamsFileName);
						BufferedReader input = new BufferedReader(new FileReader(vamsFile));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null)
						{
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
					else
					{
						JOptionPane.showMessageDialog(frame, "Verilog-AMS model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Unable to view Verilog-AMS model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 3 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv"))
			{
				try
				{
					String svFileName = tree.getFile();
					if (new File(svFileName).exists())
					{
						File svFile = new File(svFileName);
						BufferedReader input = new BufferedReader(new FileReader(svFile));
						String line = null;
						JTextArea messageArea = new JTextArea();
						while ((line = input.readLine()) != null)
						{
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
					else
					{
						JOptionPane.showMessageDialog(frame, "SystemVerilog model does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(frame, "Unable to view SystemVerilog model.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".csp"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String cmd = "atacs -lcslllodpl " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process view = exec.exec(cmd, null, work);
				log.addText("Executing:\n" + cmd);
				view.waitFor();
				String[] findTheFile = filename.split("\\.");
				// String directory = "";
				String theFile = findTheFile[0] + ".dot";
				if (new File(root + GlobalConstants.separator + theFile).exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".hse"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String cmd = "atacs -lhslllodpl " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process view = exec.exec(cmd, null, work);
				log.addText("Executing:\n" + cmd);
				view.waitFor();
				String[] findTheFile = filename.split("\\.");
				// String directory = "";
				String theFile = findTheFile[0] + ".dot";
				if (new File(root + GlobalConstants.separator + theFile).exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".unc"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String cmd = "atacs -lxodps " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process view = exec.exec(cmd, null, work);
				log.addText("Executing:\n" + cmd);
				view.waitFor();
				String[] findTheFile = filename.split("\\.");
				// String directory = "";
				String theFile = findTheFile[0] + ".dot";
				if (new File(root + GlobalConstants.separator + theFile).exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg"))
			{
				String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
				String cmd = "atacs -lsodps " + filename;
				File work = new File(root);
				Runtime exec = Runtime.getRuntime();
				Process view = exec.exec(cmd, null, work);
				log.addText("Executing:\n" + cmd);
				view.waitFor();
				String[] findTheFile = filename.split("\\.");
				// String directory = "";
				String theFile = findTheFile[0] + ".dot";
				if (new File(root + GlobalConstants.separator + theFile).exists())
				{
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "");
					log.addText(command + " " + root + theFile + "\n");
					exec.exec(command + " " + theFile, null, work);
				}
				else
				{
					File log = new File(root + GlobalConstants.separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null)
					{
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
		catch (IOException e1)
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "File cannot be read", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (InterruptedException e2)
		{
			e2.printStackTrace();
		}
	}
	
	private void copySEDML(String oldName,String newName) {
		SedML sedml = sedmlDocument.getSedMLModel();
		if (oldName.endsWith(".prb") || oldName.endsWith(".grf")) {
			String outputId = oldName.replace(".prb","").replace(".grf", "");
			String newOutputId = newName.replace(".prb","").replace(".grf", "");
			Output output = sedml.getOutputWithId(outputId);
			if (output != null) {
				Output newOutput = SEDMLutilities.copyOutput(output,newOutputId);
				sedml.addOutput(newOutput);
			}
		} else if (oldName.endsWith(".xml")) {
			// Do nothing
		} else {
			AbstractTask task = sedml.getTaskWithId(oldName);
			if (task != null) {
				AbstractTask newTask = SEDMLutilities.copyTask(task, newName);
				sedml.addTask(newTask);
				Output output = sedml.getOutputWithId(oldName+"__graph");
				Plot2D newGraph = null;
				if (output != null) {
					newGraph = (Plot2D)SEDMLutilities.copyOutput(output,newName+"__graph");
					sedml.addOutput(newGraph);
				}
				output = sedml.getOutputWithId(oldName+"__report");
				Report newReport = null;
				if (output != null) {
					newReport = (Report)SEDMLutilities.copyOutput(output,newName+"__report");
					sedml.addOutput(newReport);
				}
				ArrayList<DataGenerator> copy = new ArrayList<DataGenerator>();
				for (DataGenerator dg : sedml.getDataGenerators()) {
					for (Variable var : dg.getListOfVariables()) {
						if (var.getReference().equals(oldName)) {
							copy.add(dg);
							break;
						} 
					}
				}
				for (DataGenerator dg : copy) {
					String newDGid;
					if (dg.getId().contains(oldName)) {
						newDGid = dg.getId().replace(oldName, newName);
					} else {
						newDGid = newName + "_" + dg.getId();
					}
					DataGenerator newDG = SEDMLutilities.copyDataGenerator(dg,newDGid);
					for (Variable var : newDG.getListOfVariables()) {
						if (var.getReference().equals(oldName)) {
							var.setReference(newName);
						} 
					}
					sedml.addDataGenerator(newDG);
					if (newGraph!=null) {
						for (Curve curve : newGraph.getListOfCurves()) {
							if (curve.getXDataReference().equals(dg.getId())) {
								curve.setxDataReference(newDG.getId());
							}
							if (curve.getYDataReference().equals(dg.getId())) {
								curve.setyDataReference(newDG.getId());
							}
						}
					}
					if (newReport!=null) {
						for (DataSet dataSet : newReport.getListOfDataSets()) {
							if (dataSet.getDataReference().equals(dg.getId())) {
								dataSet.setDataReference(newDG.getId());
							}
						}
					}
				}
				ArrayList<AbstractTask> subTasks = new ArrayList<AbstractTask>();
				for (AbstractTask subTask : sedml.getTasks()) {
					if (subTask.getId().startsWith(oldName+"__")) {
						subTasks.add(subTask);
					}
				}
				for (AbstractTask subTask : subTasks) {
					String newId = subTask.getId().replace(oldName+"__", newName+"__");
					newTask = SEDMLutilities.copyTask(subTask, newId);
					sedml.addTask(newTask);
					copy = new ArrayList<DataGenerator>();
					for (DataGenerator dg : sedml.getDataGenerators()) {
						for (Variable var : dg.getListOfVariables()) {
							if (var.getReference().equals(subTask.getId())) {
								copy.add(dg);
								break;
							} 
						}
					}
					for (DataGenerator dg : copy) {
						String newDGid;
						if (dg.getId().contains(oldName+"__")) {
							newDGid = dg.getId().replace(oldName+"__", newName+"__");
						} else {
							newDGid = newName + "_" + dg.getId();
						}
						DataGenerator newDG = SEDMLutilities.copyDataGenerator(dg,newDGid);
						for (Variable var : newDG.getListOfVariables()) {
							if (var.getReference().equals(subTask.getId())) {
								var.setReference(newTask.getId());
							} 
						}
						sedml.addDataGenerator(newDG);
						if (newGraph!=null) {
							for (Curve curve : newGraph.getListOfCurves()) {
								if (curve.getXDataReference().equals(dg.getId())) {
									curve.setxDataReference(newDG.getId());
								}
								if (curve.getYDataReference().equals(dg.getId())) {
									curve.setyDataReference(newDG.getId());
								}
							}
						}
						if (newReport!=null) {
							for (DataSet dataSet : newReport.getListOfDataSets()) {
								if (dataSet.getDataReference().equals(dg.getId())) {
									dataSet.setDataReference(newDG.getId());
								}
							}
						}
					}
				}
			}
			
		}
		writeSEDMLDocument();		
	}
	
	private void copyDirectory(String srcDir,String destDir,String copyName) throws SBMLException, XMLStreamException, IOException {
		new File(destDir).mkdir();
		String[] files = new File(srcDir).list();
		for (String file : files)
		{
			if (file.endsWith(".sbml") || file.equals(".xml"))
			{
				SBMLDocument document = SBMLutilities.readSBML(srcDir + GlobalConstants.separator + file);
				SBMLWriter writer = new SBMLWriter();
				writer.writeSBMLToFile(document, destDir + GlobalConstants.separator + file);
			}
			else if (new File(srcDir + GlobalConstants.separator + file).isFile()) {
				FileOutputStream out = new FileOutputStream(new File(destDir + GlobalConstants.separator + file));
				if (file.endsWith(".sim")||file.endsWith(".grf")||file.endsWith(".prb")||file.endsWith(".lrn")) {
					String ext = file.substring(file.lastIndexOf("."));
					out = new FileOutputStream(new File(destDir + GlobalConstants.separator + copyName + ext));
				}
				FileInputStream in = new FileInputStream(new File(srcDir + GlobalConstants.separator + file));
				int read = in.read();
				while (read != -1)
				{
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			} else if (new File(srcDir + GlobalConstants.separator + file).isDirectory()) {
				copyDirectory(srcDir + GlobalConstants.separator + file,destDir + GlobalConstants.separator + file, copyName+"__"+file);
			}
		}
	}


	private void copy()
	{
		if (!tree.getFile().equals(root))
		{
			String oldName = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(oldName))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			String copy = JOptionPane.showInputDialog(frame, "Enter a New Filename:", "Copy", JOptionPane.PLAIN_MESSAGE);
			if (copy == null || copy.equals(""))
			{
				return;
			}
			copy = copy.trim();
			if (tree.getFile().contains("."))
			{
				String extension = tree.getFile().substring(tree.getFile().lastIndexOf("."), tree.getFile().length());
				if (!copy.endsWith(extension))
				{
					copy += extension;
				}
			}
			if (copy.equals(oldName))
			{
				JOptionPane.showMessageDialog(frame, "Unable to copy file." + "\nNew filename must be different than old filename.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try
			{
				if (checkFiles(oldName, copy))
				{
					if (overwrite(root + GlobalConstants.separator + copy, copy))
					{
						if (copy.endsWith(".xml"))
						{
							SBMLDocument document = SBMLutilities.readSBML(tree.getFile());
							List<URI> sbolURIs = new LinkedList<URI>();
							String sbolStrand = AnnotationUtility.parseSBOLAnnotation(document.getModel(), sbolURIs);
							Iterator<URI> sbolIterator = sbolURIs.iterator();
							while (sbolIterator != null && sbolIterator.hasNext())
							{
								if (sbolIterator.next().toString().endsWith("iBioSim"))
								{
									sbolIterator.remove();
									sbolIterator = null;
									if (sbolURIs.size() > 0)
									{
										AnnotationUtility.setSBOLAnnotation(document.getModel(), new SBOLAnnotation(document.getModel().getMetaId(),
												sbolURIs, sbolStrand));
									}
									else
									{
										AnnotationUtility.removeSBOLAnnotation(document.getModel());
									}
								}
							}
							document.getModel().setId(copy.substring(0, copy.lastIndexOf(".")));
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + GlobalConstants.separator + copy);
						}
						else if (copy.contains("."))
						{
							FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + copy));
							FileInputStream in = new FileInputStream(new File(tree.getFile()));
							int read = in.read();
							while (read != -1)
							{
								out.write(read);
								read = in.read();
							}
							in.close();
							out.close();
						}
						else
						{
							copyDirectory(tree.getFile(), root + GlobalConstants.separator + copy, copy);
						}
						addToTree(copy);
						copySEDML(oldName,copy);
					}
				}
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to copy file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void renameTabs(String oldName,String rename) {
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				if (tree.getFile().length() > 3
						&& (tree.getFile().substring(tree.getFile().length() - 4).equals(".grf") || tree.getFile()
								.substring(tree.getFile().length() - 4).equals(".prb")))
				{
					((Graph) tab.getComponentAt(i)).setGraphName(rename);
					tab.setTitleAt(i, rename);
				}
				else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".xml"))
				{
					((ModelEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
					tab.setTitleAt(i, rename);
				}
				else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm"))
				{
					((ModelEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
					tab.setTitleAt(i, rename);
				}
				else if (tree.getFile().length() > 4 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol"))
				{
					tab.setTitleAt(i, rename);
				}
				else if (tree.getFile().length() > 3 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn"))
				{
					((LHPNEditor) tab.getComponentAt(i)).reload(rename.substring(0, rename.length() - 4));
					tab.setTitleAt(i, rename);
				}
				else if (tab.getComponentAt(i) instanceof JTabbedPane)
				{
					if (tab.getComponentAt(i) instanceof SynthesisView)
					{
						((SynthesisView) tab.getComponentAt(i)).renameView(rename);
					}
					else
					{
						JTabbedPane t = new JTabbedPane();
						t.addMouseListener(this);
						int selected = ((JTabbedPane) tab.getComponentAt(i)).getSelectedIndex();
						boolean analysis = false;
						ArrayList<Component> comps = new ArrayList<Component>();
						for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getTabCount(); j++)
						{
							Component c = ((JTabbedPane) tab.getComponentAt(i)).getComponent(j);
							comps.add(c);
						}
						for (Component c : comps)
						{
							if (analysis)
							{
								if (c instanceof MovieContainer)
								{
									t.addTab("Schematic", c);
									t.getComponentAt(t.getComponents().length - 1).setName("ModelViewMovie");
								}
								else if (c instanceof ModelEditor)
								{
									((ModelEditor) c).setParamFile(root + GlobalConstants.separator + rename + GlobalConstants.separator + rename + ".sim");
									t.addTab("Parameters", c);
									t.getComponentAt(t.getComponents().length - 1).setName("Model Editor");
								}
								else if (c instanceof Graph)
								{
									((Graph) c).setDirectory(root + GlobalConstants.separator + rename);
									if (((Graph) c).isTSDGraph())
									{
										t.addTab("TSD Graph", c);
										t.getComponentAt(t.getComponents().length - 1).setName("TSD Graph");
									}
									else
									{
										t.addTab("Histogram", c);
										t.getComponentAt(t.getComponents().length - 1).setName("Histogram");
									}
								}
								else if (c instanceof JScrollPane)
								{
									// Do nothing
								}
								else
								{
									t.addTab("Advanced Options", c);
									t.getComponentAt(t.getComponents().length - 1).setName("");
								}
							}
							else
							{
								if (c instanceof AnalysisView)
								{
									((AnalysisView) c).setSim(rename);
									t.addTab("Simulation Options", c);
									t.getComponentAt(t.getComponents().length - 1).setName("Simulate");
									analysis = true;
								}
								else if (c instanceof Graph)
								{
									Graph g = ((Graph) c);
									g.setDirectory(root + GlobalConstants.separator + rename);
									if (g.isTSDGraph())
									{
										g.setGraphName(rename + ".grf");
									}
									else
									{
										g.setGraphName(rename + ".prb");
									}
								}
								else if (c instanceof LearnGCM)
								{
									LearnGCM l = ((LearnGCM) c);
									l.setDirectory(root + GlobalConstants.separator + rename);
								}
								else if (c instanceof DataManager)
								{
									DataManager d = ((DataManager) c);
									d.setDirectory(root + GlobalConstants.separator + rename);
								}
							}
						}
						if (analysis)
						{
							t.setSelectedIndex(selected);
							tab.setComponentAt(i, t);
						}
					}
					tab.setTitleAt(i, rename);
					tab.getComponentAt(i).setName(rename);
				}
				else
				{
					tab.setTitleAt(i, rename);
					tab.getComponentAt(i).setName(rename);
				}
			}
			else if (tab.getComponentAt(i) instanceof JTabbedPane)
			{
				if (tab.getComponentAt(i) instanceof SynthesisView)
				{
					((SynthesisView) tab.getComponentAt(i)).changeSpecFile(rename);
				}
				else
				{
					ArrayList<Component> comps = new ArrayList<Component>();
					for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getTabCount(); j++)
					{
						Component c = ((JTabbedPane) tab.getComponentAt(i)).getComponent(j);
						comps.add(c);
					}
					for (Component c : comps)
					{
						if (c instanceof AnalysisView && ((AnalysisView) c).getBackgroundFile().equals(oldName))
						{
							((AnalysisView) c).updateBackgroundFile(rename);
						}
						else if (c instanceof LearnGCM && ((LearnGCM) c).getBackgroundFile().equals(oldName))
						{
							((LearnGCM) c).updateBackgroundFile(rename);
						}
					}
				}
			}
		}		
	}
	
	private void renameSEDML(String oldName,String newName) {
		SedML sedml = sedmlDocument.getSedMLModel();
		if (oldName.endsWith(".prb") || oldName.endsWith(".grf")) {
			String outputId = oldName.replace(".prb","").replace(".grf", "");
			String newOutputId = newName.replace(".prb","").replace(".grf", "");
			Output output = sedml.getOutputWithId(outputId);
			if (output != null) {
				Output newOutput = SEDMLutilities.copyOutput(output,newOutputId);
				sedml.addOutput(newOutput);
				sedml.removeOutput(output);
			}
		} else if (oldName.endsWith(".xml")) {
			for (org.jlibsedml.Model model : sedml.getModels()) {
				if (model.getSource().equals(oldName)) {
					model.setSource(newName);
				}
			}
		} else {
			AbstractTask task = sedml.getTaskWithId(oldName);
			if (task != null) {
				AbstractTask newTask = SEDMLutilities.copyTask(task, newName);
				sedml.addTask(newTask);
				sedml.removeTask(task);
				Output output = sedml.getOutputWithId(oldName+"__graph");
				if (output != null) {
					Output newOutput = SEDMLutilities.copyOutput(output,newName+"__graph");
					sedml.addOutput(newOutput);
					sedml.removeOutput(output);
				}
				output = sedml.getOutputWithId(oldName+"__report");
				if (output != null) {
					Output newOutput = SEDMLutilities.copyOutput(output,newName+"__report");
					sedml.addOutput(newOutput);
					sedml.removeOutput(output);
				}
				for (DataGenerator dg : sedml.getDataGenerators()) {
					for (Variable var : dg.getListOfVariables()) {
						if (var.getReference().equals(oldName)) {
							var.setReference(newName);
						} 
					}
				}
				ArrayList<AbstractTask> subTasks = new ArrayList<AbstractTask>();
				for (AbstractTask subTask : sedml.getTasks()) {
					if (subTask.getId().startsWith(oldName+"__")) {
						subTasks.add(subTask);
					}
				}
				for (AbstractTask subTask : subTasks) {
					String newId = subTask.getId().replace(oldName+"__", newName+"__");
					newTask = SEDMLutilities.copyTask(subTask, newId);
					sedml.addTask(newTask);
					for (DataGenerator dg : sedml.getDataGenerators()) {
						for (Variable var : dg.getListOfVariables()) {
							if (var.getReference().equals(subTask.getId())) {
								var.setReference(newId);
								break;
							}
						}
					}
					sedml.removeTask(task);
				}
			}
			
		}
		writeSEDMLDocument();		
	}

	private void rename()
	{
		if (!tree.getFile().equals(root))
		{

			if (!new File(tree.getFile()).isDirectory())
			{
				String[] views = canDelete(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]);
				if (views.length != 0)
				{
					String view = "";
					String gcms = "";
					for (int i = 0; i < views.length; i++)
					{
						if (views[i].endsWith(".xml"))
						{
							gcms += views[i] + "\n";
						}
						else
						{
							view += views[i] + "\n";
						}
					}
					String message;
					if (gcms.equals(""))
					{
						message = "Unable to rename the selected file." + "\nIt is linked to the following views:\n" + view
								+ "\nDelete these views first.";
					}
					else if (view.equals(""))
					{
						message = "Unable to rename the selected file." + "\nIt is linked to the following models:\n" + gcms
								+ "\nDelete these models first.";
					}
					else
					{
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

			String oldName = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			String newName = JOptionPane.showInputDialog(frame, "Enter a New Filename:", "Rename", JOptionPane.PLAIN_MESSAGE);
			if (newName == null || newName.equals(""))
			{
				return;
			}
			newName = newName.trim();
			if (tree.getFile().contains("."))
			{
				String extension = tree.getFile().substring(tree.getFile().lastIndexOf("."), tree.getFile().length());
				if (!newName.endsWith(extension))
				{
					newName += extension;
				}
			}
			if (newName.equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				JOptionPane.showMessageDialog(frame, "Unable to rename file." + "\nNew filename must be different than old filename.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int index = newName.lastIndexOf(".");
			String modelID = newName;
			if (index != -1)
			{
				modelID = newName.substring(0, newName.lastIndexOf("."));
			}
			try
			{
				if (checkFiles(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], newName))
				{
					if (overwrite(root + GlobalConstants.separator + newName, newName))
					{
						if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml") || tree.getFile().endsWith(".gcm")
								|| tree.getFile().endsWith(".lpn") || tree.getFile().endsWith(".vhd") || tree.getFile().endsWith(".csp")
								|| tree.getFile().endsWith(".hse") || tree.getFile().endsWith(".unc") || tree.getFile().endsWith(".rsg")
								|| tree.getFile().endsWith(".prop"))
						{
							reassignViews(oldName, newName);
						}
						if (tree.getFile().endsWith(".xml"))
						{
							new File(tree.getFile()).renameTo(new File(root + GlobalConstants.separator + newName));
							SBMLDocument document = SBMLutilities.readSBML(root + GlobalConstants.separator + newName);
							document.getModel().setId(modelID);
							SBMLWriter writer = new SBMLWriter();
							writer.writeSBMLToFile(document, root + GlobalConstants.separator + newName);
						}
						else
						{
							new File(tree.getFile()).renameTo(new File(root + GlobalConstants.separator + newName));
						}
						if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml") || tree.getFile().endsWith(".gcm")
								|| tree.getFile().endsWith(".lpn") || tree.getFile().endsWith(".vhd") || tree.getFile().endsWith(".csp")
								|| tree.getFile().endsWith(".hse") || tree.getFile().endsWith(".unc") || tree.getFile().endsWith(".rsg")
								|| tree.getFile().endsWith(".prop"))
						{
							updateAsyncViews(newName);
						}
						if (new File(root + GlobalConstants.separator + newName).isDirectory())
						{
							String subFilePath = root + GlobalConstants.separator + newName + GlobalConstants.separator
									+ tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
							String renamedSubFilePath = root + GlobalConstants.separator + newName + GlobalConstants.separator + newName;
							if (new File(subFilePath + ".sim").exists())
							{
								new File(subFilePath + ".sim").renameTo(new File(renamedSubFilePath + ".sim"));
							}
							else if (new File(subFilePath + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).exists())
							{
								new File(subFilePath + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).renameTo(new File(renamedSubFilePath
										+ GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION));
							}
							else if (new File(subFilePath + ".pms").exists())
							{
								new File(subFilePath + ".pms").renameTo(new File(renamedSubFilePath + ".sim"));
							}
							if (new File(subFilePath + ".lrn").exists())
							{
								new File(subFilePath + ".lrn").renameTo(new File(renamedSubFilePath + ".lrn"));
							}
							if (new File(subFilePath + ".ver").exists())
							{
								new File(subFilePath + ".ver").renameTo(new File(renamedSubFilePath + ".ver"));
							}
							if (new File(subFilePath + ".grf").exists())
							{
								new File(subFilePath + ".grf").renameTo(new File(renamedSubFilePath + ".grf"));
							}
							if (new File(subFilePath + ".prb").exists())
							{
								new File(subFilePath + ".prb").renameTo(new File(renamedSubFilePath + ".prb"));
							}
							if (new File(subFilePath + ".prop").exists())
							{
								new File(subFilePath + ".prop").renameTo(new File(renamedSubFilePath + ".prop"));
							}
						}
						renameTabs(oldName,newName);
						updateViewNames(tree.getFile(), newName);
						deleteFromTree(oldName);
						addToTree(newName);
						renameSEDML(oldName,newName);
					}
				}
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to rename selected file.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (SBMLException e)
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void openModelEditor(boolean textBased)
	{
		String filename = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
		openModelEditor(filename, textBased);
	}

	public void openModelEditor(String filename, boolean textBased)
	{
		File work = new File(root);
		int i = getTab(filename);
		if (i != -1)
		{
			if (((ModelEditor) tab.getComponentAt(i)).isTextBased() != textBased)
			{
				((ModelEditor) tab.getComponentAt(i)).setTextBased(textBased);
				((ModelEditor) tab.getComponentAt(i)).rebuildGui();
			}
			tab.setSelectedIndex(i);
		}
		else
		{
			String path = work.getAbsolutePath();
			try
			{
				ModelEditor gcm = new ModelEditor(path, filename, this, log, false, null, null, null, textBased, false);
				addTab(filename, gcm, "Model Editor");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void openSBML(String fullPath)
	{
		try
		{
			boolean done = false;
			String theSBMLFile = fullPath.split(GlobalConstants.separator)[fullPath.split(GlobalConstants.separator).length - 1];
			String theGCMFile = theSBMLFile.replace(".xml", ".gcm");
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(theSBMLFile) || getTitleAt(i).equals(theGCMFile))
				{
					tab.setSelectedIndex(i);
					done = true;
					break;
				}
			}
			if (!done)
			{
				// createGCMFromSBML(root, fullPath, theSBMLFile, theGCMFile,
				// false);
				// addToTree(theGCMFile);
				ModelEditor gcm = new ModelEditor(root + GlobalConstants.separator, theGCMFile, this, log, false, null, null, null, false, false);
				addTab(theSBMLFile, gcm, "Model Editor");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "You must select a valid SBML file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void openSBOL()
	{
		String filePath = tree.getFile();
		String fileName = "";
		fileName = filePath.substring(filePath.lastIndexOf(GlobalConstants.separator) + 1);
		int i = getTab(fileName);
		if (i != -1)
		{
			tab.setSelectedIndex(i);
		}
		else
		{
			SBOLBrowser2 sbolBrowser2 = new SBOLBrowser2(this, filePath);
			sbolBrowser2.open();
		}
	}

	// private void openSBOL2() {
	// String filePath = tree.getFile();
	// String fileName = "";
	// fileName = filePath.substring(filePath.lastIndexOf(Gui.separator) + 1);
	// int i = getTab(fileName);
	// if (i != -1) {
	// tab.setSelectedIndex(i);
	// }
	// else {
	// // SBOL2Browser sbol2Browser = new SBOL2Browser(this, filePath);
	// // sbol2Browser.open();
	// // SBMLtoSBOL sbolDoc = new SBMLtoSBOL(filePath, this.);
	// }
	// }
	
//	private boolean sbolDesignerOpen() {
//		for (int i = 0; i < tab.getTabCount(); i++)
//		{
//			if (this.tab.getComponentAt(i).getName().equals("SBOL Designer"))
//			{
//				JOptionPane.showMessageDialog(Gui.frame, "Currently only one SBOL Designer tab can be open at a time.", 
//						"Unable to Open SBOL Designer", JOptionPane.ERROR_MESSAGE);		
//				return true;
//			}
//		}
//		return false;
//	}
	
	private void openSBOLDesigner() {
		//if (sbolDesignerOpen()) return;
		String fileName = tree.getFile().substring(tree.getFile().lastIndexOf(GlobalConstants.separator) + 1);
		try {
			if (getSBOLDocument().getComponentDefinitions().size()==0) {
				createPart();
			} else {
				SBOLDesignerPlugin sbolDesignerPlugin = new SBOLDesignerPlugin(root+GlobalConstants.separator,fileName,null,getSBOLDocument().getDefaultURIprefix());
				//if (sbolDesignerPlugin.getRootDisplayId().equals("NewDesign")) return;
				if (sbolDesignerPlugin.getRootDisplayId()==null) return;
				addTab(sbolDesignerPlugin.getRootDisplayId(),sbolDesignerPlugin,"SBOL Designer");
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file at " + fileName + " is invalid.", 
					"Invalid SBOL", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void createPart() {
		//if (sbolDesignerOpen()) return;
		String partId = null;
		JTextField partChooser = new JTextField("");
		partChooser.setColumns(20);
		JPanel partPanel = new JPanel(new GridLayout(2, 1));
		partPanel.add(new JLabel("Enter Part ID: "));
		partPanel.add(partChooser);
		frame.add(partPanel);
		String[] options = { GlobalConstants.OK, GlobalConstants.CANCEL };
		int okCancel = JOptionPane.showOptionDialog(frame, partPanel, "Part ID", 
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		// if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			partId = partChooser.getText();
		} else {
			return;
		}
		if (partId != null && !partId.trim().equals("")) {
			if (!(IDpat.matcher(partId).matches()))	{
				JOptionPane.showMessageDialog(frame,
					"A part ID can only contain letters, digits, and underscores.\nIt also cannot start with a digit.", "Invalid ID",
					JOptionPane.ERROR_MESSAGE);
			} else {
				SBOLDesignerPlugin sbolDesignerPlugin;
				try {
					ComponentDefinition cd = getSBOLDocument().createComponentDefinition(partId, "1", ComponentDefinition.DNA);
					cd.addRole(SequenceOntology.ENGINEERED_REGION);
					writeSBOLDocument();
					sbolDesignerPlugin = new SBOLDesignerPlugin(root+GlobalConstants.separator,currentProjectId+".sbol",cd.getIdentity(),sbolDocument.getDefaultURIprefix());
					addTab(partId,sbolDesignerPlugin,"SBOL Designer");
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Gui.frame, "Unable to create new part.", 
							"Invalid Part", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void generateSBMLFromSBOL(String filePath)
	{
		//String projectDirectory = filePath.substring(0, filePath.lastIndexOf(File.separator));
		try
		{
			org.sbolstandard.core2.SBOLDocument sbolDoc = SBOLReader.read(new FileInputStream(filePath));
			for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
			{
				//BioModel targetModel = new BioModel(projectDirectory);
				//if (!targetModel.load(projectDirectory + File.separator + ModelGenerator.getDisplayID(moduleDef) + ".xml"))
				//{
				List<BioModel> models;
        try {
          models = SBOL2SBML.generateModel(root, moduleDef, sbolDoc);
          for (BioModel model : models)
          {
            if (overwrite(root + File.separator + model.getSBMLDocument().getModel().getId() + ".xml", 
                model.getSBMLDocument().getModel().getId() + ".xml"))
            {
              model.save(root + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
              addToTree(model.getSBMLDocument().getModel().getId() + ".xml");
            }
          }
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } 
				
				//}
			}
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file not found at " + filePath + ".", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		}
		catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file at " + filePath + " is invalid.", 
					"Invalid SBOL", JOptionPane.ERROR_MESSAGE);
		}
		catch (SBOLConversionException e) {
			JOptionPane.showMessageDialog(Gui.frame, "SBOL file at " + filePath + " is invalid.", 
					"Invalid SBOL", JOptionPane.ERROR_MESSAGE);
		}
	}

	public HashSet<String> getFilePaths(String fileExtension)
	{
		HashSet<String> filePaths = new HashSet<String>();
		TreeModel tree = getFileTree().tree.getModel();
		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++)
		{
			String fileName = tree.getChild(tree.getRoot(), i).toString();
			if (fileName.endsWith(fileExtension))
			{
				filePaths.add(getRoot() + GlobalConstants.separator + fileName);
			}
		}
		return filePaths;
	}

	private void openGraph()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
			addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], new Graph(null, "Number of molecules", "title",
					"tsd.printer", root, "Time", this, tree.getFile(), log,
					tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], true, false), "TSD Graph");
		}
	}

	private void openHistogram()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
			addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], new Graph(null, "Percent", "title", "tsd.printer",
					root, "Time", this, tree.getFile(), log, tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], false,
					false), "Histogram");
		}
	}

	private void openLPN()
	{
		try
		{
			String filename = tree.getFile();
			String directory = "";
			String theFile = "";
			if (filename.lastIndexOf('/') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			LPN lhpn = new LPN();
			lhpn.addObserver(this);
			File work = new File(directory);
			int i = getTab(theFile);
			if (i != -1)
			{
				tab.setSelectedIndex(i);
			}
			else
			{
				LHPNEditor editor = new LHPNEditor(work.getAbsolutePath(), theFile, lhpn, this);
				addTab(theFile, editor, "LHPN Editor");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Unable to view this LPN file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void newModel(String fileType, String extension)
	{
		if (root != null)
		{
			try
			{
				String modelID = JOptionPane.showInputDialog(frame, "Enter " + fileType + " Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
				if (modelID != null && !modelID.trim().equals(""))
				{
					String fileName;
					modelID = modelID.trim();
					if (modelID.length() >= extension.length())
					{
						if (!modelID.substring(modelID.length() - extension.length()).equals(extension))
						{
							fileName = modelID + extension;
						}
						else
						{
							fileName = modelID;
							modelID = modelID.substring(0, modelID.length() - extension.length());
						}
					}
					else
					{
						fileName = modelID + extension;
					}
					if (!(IDpat.matcher(modelID).matches()))
					{
						JOptionPane.showMessageDialog(frame,
								"A model ID can only contain letters, digits, and underscores.\nIt also cannot start with a digit.", "Invalid ID",
								JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						File f = new File(root + GlobalConstants.separator + fileName);
						f.createNewFile();
						addToTree(fileName);
						if (!viewer.equals(""))
						{
							String command = viewer + " " + root + GlobalConstants.separator + fileName;
							Runtime exec = Runtime.getRuntime();
							try
							{
								exec.exec(command);
							}
							catch (Exception e1)
							{
								JOptionPane.showMessageDialog(frame, "Unable to open external editor.", "Error Opening Editor",
										JOptionPane.ERROR_MESSAGE);
							}
						}
						else
						{
							JTextArea text = new JTextArea("");
							text.setEditable(true);
							text.setLineWrap(true);
							JScrollPane scroll = new JScrollPane(text);
							addTab(fileName, scroll, fileType + " Editor");
						}
					}
				}
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void openModel(String fileType)
	{
		try
		{
			String filename = tree.getFile();
			String directory = "";
			String theFile = "";
			if (filename.lastIndexOf('/') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0)
			{
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			File work = new File(directory);
			int i = getTab(theFile);
			if (i != -1)
			{
				tab.setSelectedIndex(i);
			}
			else
			{
				if (!viewer.equals(""))
				{
					String command = viewer + " " + directory + GlobalConstants.separator + theFile;
					Runtime exec = Runtime.getRuntime();
					try
					{
						exec.exec(command);
					}
					catch (Exception e1)
					{
						JOptionPane.showMessageDialog(frame, "Unable to open external editor.", "Error Opening Editor", JOptionPane.ERROR_MESSAGE);
					}
				}
				else
				{
					File file = new File(work + GlobalConstants.separator + theFile);
					String input = "";
					FileReader in = new FileReader(file);
					int read = in.read();
					while (read != -1)
					{
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
		catch (Exception e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to view this " + fileType + " file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public int getTab(String name)
	{
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(name))
			{
				return i;
			}
		}
		return -1;
	}

	public void deleteDir(File dir)
	{
		int count = 0;
		do
		{
			File[] list = dir.listFiles();
			System.gc();
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].isDirectory())
				{
					deleteDir(list[i]);
				}
				else
				{
					list[i].delete();
				}
			}
			count++;
		}
		while (!dir.delete() && count != 100);
		if (count == 100)
		{
			JOptionPane.showMessageDialog(frame, "Unable to delete.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method adds a new project to recent list
	 */
	public void addRecentProject(String projDir)
	{
		// boolean newOne = true;
		for (int i = 0; i < numberRecentProj; i++)
		{
			if (recentProjectPaths[i].equals(projDir))
			{
				for (int j = 0; j <= i; j++)
				{
					String save = recentProjectPaths[j];
					recentProjects[j].setText(projDir.split(GlobalConstants.separator)[projDir.split(GlobalConstants.separator).length - 1]);
					openRecent.insert(recentProjects[j], j);
					recentProjectPaths[j] = projDir;
					projDir = save;
				}
				for (int j = i + 1; j < numberRecentProj; j++)
				{
					openRecent.insert(recentProjects[j], j);
				}
				return;
			}
		}
		if (numberRecentProj < 10)
		{
			numberRecentProj++;
		}
		for (int i = 0; i < numberRecentProj; i++)
		{
			String save = recentProjectPaths[i];
			recentProjects[i].setText(projDir.split(GlobalConstants.separator)[projDir.split(GlobalConstants.separator).length - 1]);
			openRecent.insert(recentProjects[i], i);
			recentProjectPaths[i] = projDir;
			projDir = save;
		}
	}

	/**
	 * This method removes a project from the recent list
	 */
	public void removeRecentProject(String projDir)
	{
		for (int i = 0; i < numberRecentProj; i++)
		{
			if (recentProjectPaths[i].equals(projDir))
			{
				for (int j = i; j < numberRecentProj - 1; j++)
				{
					recentProjects[j].setText(recentProjects[j + 1].getText());
					recentProjectPaths[j] = recentProjectPaths[j + 1];
				}
				openRecent.remove(recentProjects[numberRecentProj - 1]);
				recentProjectPaths[numberRecentProj - 1] = "";
				numberRecentProj--;
				return;
			}
		}
	}

	/**
	 * This method removes all projects from the recent list
	 */
	public void removeAllRecentProjects()
	{
		for (int i = 0; i < numberRecentProj; i++)
		{
			openRecent.remove(recentProjects[i]);
			recentProjectPaths[i] = "";
		}
		numberRecentProj = 0;
	}

	/**
	 * This method refreshes the menu.
	 */
	public void refresh()
	{
		mainPanel.remove(tree);
		tree = new FileTree(new File(root), this, lema, atacs, lpn);
		topSplit.setLeftComponent(tree);
		// mainPanel.add(tree, "West");
		mainPanel.validate();
	}

//	/**
//	 * This method refreshes the tree.
//	 */
//	private void refreshTree()
//	{
//		mainPanel.remove(tree);
//		tree = new FileTree(new File(root), this, lema, atacs, lpn);
//		topSplit.setLeftComponent(tree);
//		// mainPanel.add(tree, "West");
//		// updateGCM();
//		mainPanel.validate();
//	}

	public void addToTree(String item)
	{
		tree.addToTree(item, root);
		// updateGCM();
		mainPanel.validate();
	}

	public void addToTreeNoUpdate(String item)
	{
		tree.addToTree(item, root);
		mainPanel.validate();
	}

	public void deleteFromTree(String item)
	{
		tree.deleteFromTree(item);
		// updateGCM();
		mainPanel.validate();
	}

	public FileTree getFileTree()
	{
		return tree;
	}

	public void markTabDirty(boolean dirty)
	{
		int i = tab.getSelectedIndex();
		if (dirty)
		{
			if (i >= 0 && !tab.getTitleAt(i).endsWith("*"))
			{
				tab.setTitleAt(i, tab.getTitleAt(i) + "*");
			}
		}
		else
		{
			if (i >= 0 && tab.getTitleAt(i).endsWith("*"))
			{
				tab.setTitleAt(i, tab.getTitleAt(i).replace("*", ""));
			}
		}
	}

	public void markTabClean(int i)
	{
		if (i >= 0 && tab.getTitleAt(i).endsWith("*"))
		{
			tab.setTitleAt(i, tab.getTitleAt(i).replace("*", ""));
		}
	}

	/**
	 * This method adds the given Component to a tab.
	 */
	public void addTab(String name, Component panel, String tabName)
	{
		tab.addTab(name, panel);
		// panel.addMouseListener(this);
		if (tabName != null)
		{
			tab.getComponentAt(tab.getTabCount() - 1).setName(tabName);
		}
		else
		{
			tab.getComponentAt(tab.getTabCount() - 1).setName(name);
		}
		tab.setSelectedIndex(tab.getTabCount() - 1);
	}

	/**
	 * This method removes the given component from the tabs.
	 */
	public void removeTab(Component component)
	{
		tab.remove(component);
		if (tab.getTabCount() > 0)
		{
			tab.setSelectedIndex(tab.getTabCount() - 1);
			enableTabMenu(tab.getSelectedIndex());
		}
		else
		{
			enableTreeMenu();
		}
	}

	public void refreshTabListeners()
	{
		for (ChangeListener l : tab.getChangeListeners())
		{
			tab.removeChangeListener(l);
		}
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (tab.getComponent(i) instanceof ModelEditor)
			{
				((ModelEditor) tab.getComponent(i)).getSchematic().addChangeListener();
			}
		}
	}

	public JTabbedPane getTab()
	{
		return tab;
	}

	/**
	 * Prompts the user to save work that has been done.
	 */
	public int save(int index, int autosave)
	{
		if (tab.getComponentAt(index).getName().contains(("Model Editor")) || tab.getComponentAt(index).getName().contains("LHPN"))
		{
			if (tab.getComponentAt(index) instanceof ModelEditor)
			{
				ModelEditor editor = (ModelEditor) tab.getComponentAt(index);
				if (editor.isDirty())
				{
					if (autosave == 0)
					{
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?", "Save Changes",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION)
						{
							editor.save(false);
							return 1;
						}
						else if (value == NO_OPTION)
						{
							return 1;
						}
						else if (value == CANCEL_OPTION)
						{
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION)
						{
							editor.save(false);
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION)
						{
							return 3;
						}
					}
					else if (autosave == 1)
					{
						editor.save(false);
						return 2;
					}
					else
					{
						return 3;
					}
				}
			}
			else if (tab.getComponentAt(index) instanceof LHPNEditor)
			{
				LHPNEditor editor = (LHPNEditor) tab.getComponentAt(index);
				if (editor.isDirty())
				{
					if (autosave == 0)
					{
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?", "Save Changes",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION)
						{
							editor.save();
							return 1;
						}
						else if (value == NO_OPTION)
						{
							return 1;
						}
						else if (value == CANCEL_OPTION)
						{
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION)
						{
							editor.save();
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION)
						{
							return 3;
						}
					}
					else if (autosave == 1)
					{
						editor.save();
						return 2;
					}
					else
					{
						return 3;
					}
				}
			}
			if (autosave == 0)
			{
				return 1;
			}
			else if (autosave == 1)
			{
				return 2;
			}
			else
			{
				return 3;
			}
		}
		else if (tab.getComponentAt(index).getName().contains("SBOL Designer"))
		{
			SBOLDesignerPlugin editor = (SBOLDesignerPlugin) tab.getComponentAt(index);
			if (editor.isModified())
			{
				if (autosave == 0)
				{
					int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?", "Save Changes",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
					if (value == YES_OPTION)
					{
						try {
							editor.saveSBOL();
							readSBOLDocument();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
						}
						log.addText("Saving SBOL file: " + editor.getFileName() + "\n");
						return 1;
					}
					else if (value == NO_OPTION)
					{
						return 1;
					}
					else if (value == CANCEL_OPTION)
					{
						return 0;
					}
					else if (value == YES_TO_ALL_OPTION)
					{
						try {
							editor.saveSBOL();
							readSBOLDocument();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
						}
						log.addText("Saving SBOL file: " + editor.getFileName() + "\n");
						return 2;
					}
					else if (value == NO_TO_ALL_OPTION)
					{
						return 3;
					}
				}
				else if (autosave == 1)
				{
					try {
						editor.saveSBOL();
						readSBOLDocument();
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(frame, "Error Saving SBOL File.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					log.addText("Saving SBOL file: " + editor.getFileName() + "\n");
					return 2;
				}
				else
				{
					return 3;
				}
			}
			if (autosave == 0)
			{
				return 1;
			}
			else if (autosave == 1)
			{
				return 2;
			}
			else
			{
				return 3;
			}
		}
		else if (tab.getComponentAt(index).getName().contains("Graph") || tab.getComponentAt(index).getName().equals("Histogram"))
		{
			if (tab.getComponentAt(index) instanceof Graph)
			{
				if (((Graph) tab.getComponentAt(index)).hasChanged())
				{
					if (autosave == 0)
					{
						int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + getTitleAt(index) + "?", "Save Changes",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
						if (value == YES_OPTION)
						{
							((Graph) tab.getComponentAt(index)).save();
							return 1;
						}
						else if (value == NO_OPTION)
						{
							return 1;
						}
						else if (value == CANCEL_OPTION)
						{
							return 0;
						}
						else if (value == YES_TO_ALL_OPTION)
						{
							((Graph) tab.getComponentAt(index)).save();
							return 2;
						}
						else if (value == NO_TO_ALL_OPTION)
						{
							return 3;
						}
					}
					else if (autosave == 1)
					{
						((Graph) tab.getComponentAt(index)).save();
						return 2;
					}
					else
					{
						return 3;
					}
				}
			}
			if (autosave == 0)
			{
				return 1;
			}
			else if (autosave == 1)
			{
				return 2;
			}
			else
			{
				return 3;
			}
		}

		else
		{
			if (tab.getComponentAt(index) instanceof JTabbedPane)
			{
				if (tab.getComponentAt(index) instanceof SynthesisView)
				{
					SynthesisView synthView = (SynthesisView) tab.getComponentAt(index);
					Set<Integer> saveIndices = new HashSet<Integer>();
					for (int i = 0; i < synthView.getTabCount(); i++)
					{
						JPanel synthTab = (JPanel) synthView.getComponentAt(i);
						if (synthView.tabChanged(i))
						{
							if (autosave == 0)
							{
								int value = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + synthTab.getName() + "?",
										"Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION)
								{
									saveIndices.add(i);
								}
								else if (value == CANCEL_OPTION)
								{
									return 0;
								}
								else if (value == YES_TO_ALL_OPTION)
								{
									saveIndices.add(i);
									autosave = 1;
								}
								else if (value == NO_TO_ALL_OPTION)
								{
									autosave = 2;
								}
							}
							else if (autosave == 1)
							{
								saveIndices.add(i);
							}
						}
					}
					synthView.saveTabs(saveIndices);
				}
				else
				{
					for (int i = 0; i < ((JTabbedPane) tab.getComponentAt(index)).getTabCount(); i++)
					{
						if (((JTabbedPane) tab.getComponentAt(index)).getComponentAt(i).getName() != null)
						{
							if (((JTabbedPane) tab.getComponentAt(index)).getComponentAt(i).getName().equals("Simulate"))
							{
								if (((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged())
								{
									if (autosave == 0)
									{
										int value = JOptionPane.showOptionDialog(frame, "Do you want to save simulation option changes for "
												+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
										if (value == YES_OPTION)
										{
											((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save("");
										}
										else if (value == CANCEL_OPTION)
										{
											return 0;
										}
										else if (value == YES_TO_ALL_OPTION)
										{
											((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save("");
											autosave = 1;
										}
										else if (value == NO_TO_ALL_OPTION)
										{
											autosave = 2;
										}
									}
									else if (autosave == 1)
									{
										((AnalysisView) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save("");
									}
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof MovieContainer)
							{
								if (((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).getGCM2SBMLEditor().isDirty()
										|| ((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).getIsDirty())
								{
									if (autosave == 0)
									{
										int value = JOptionPane.showOptionDialog(frame, "Do you want to save parameter changes for "
												+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
										if (value == YES_OPTION)
										{
											((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
										}
										else if (value == CANCEL_OPTION)
										{
											return 0;
										}
										else if (value == YES_TO_ALL_OPTION)
										{
											((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
											autosave = 1;
										}
										else if (value == NO_TO_ALL_OPTION)
										{
											autosave = 2;
										}
									}
									else if (autosave == 1)
									{
										((MovieContainer) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).savePreferences();
									}
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals("Learn Options"))
							{
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM)
								{
									if (((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged())
									{
										if (autosave == 0)
										{
											int value = JOptionPane.showOptionDialog(frame, "Do you want to save learn option changes for "
													+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
													JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM)
												{
													((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
											}
											else if (value == CANCEL_OPTION)
											{
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM)
												{
													((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION)
											{
												autosave = 2;
											}
										}
										else if (autosave == 1)
										{
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnGCM)
											{
												((LearnGCM) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
											}
										}
									}
								}
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLPN)
								{
									if (((LearnLPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged())
									{
										if (autosave == 0)
										{
											int value = JOptionPane.showOptionDialog(frame, "Do you want to save learn option changes for "
													+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
													JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLPN)
												{
													((LearnLPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
											}
											else if (value == CANCEL_OPTION)
											{
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLPN)
												{
													((LearnLPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION)
											{
												autosave = 2;
											}
										}
										else if (autosave == 1)
										{
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof LearnLPN)
											{
												((LearnLPN) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).save();
											}
										}
									}
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().equals("Data Manager"))
							{
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof DataManager)
								{
									((DataManager) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).saveChanges(getTitleAt(index));
								}
							}
							else if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i).getName().contains("Graph"))
							{
								if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph)
								{
									if (((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i)).hasChanged())
									{
										if (autosave == 0)
										{
											int value = JOptionPane.showOptionDialog(frame, "Do you want to save graph changes for "
													+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
													JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0]);
											if (value == YES_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph)
												{
													Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i));
													g.save();
												}
											}
											else if (value == CANCEL_OPTION)
											{
												return 0;
											}
											else if (value == YES_TO_ALL_OPTION)
											{
												if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph)
												{
													Graph g = ((Graph) ((JTabbedPane) tab.getComponentAt(index)).getComponent(i));
													g.save();
												}
												autosave = 1;
											}
											else if (value == NO_TO_ALL_OPTION)
											{
												autosave = 2;
											}
										}
										else if (autosave == 1)
										{
											if (((JTabbedPane) tab.getComponentAt(index)).getComponent(i) instanceof Graph)
											{
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
			else if (tab.getComponentAt(index) instanceof JPanel)
			{
				if ((tab.getComponentAt(index)).getName().equals("Synthesis"))
				{
					Component[] array = ((JPanel) tab.getComponentAt(index)).getComponents();
					if (array[0] instanceof Synthesis)
					{
						if (((Synthesis) array[0]).hasChanged())
						{
							if (autosave == 0)
							{
								int value = JOptionPane.showOptionDialog(frame, "Do you want to save synthesis option changes for "
										+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
										OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION)
								{
									if (array[0] instanceof Synthesis)
									{
										((Synthesis) array[0]).save();
									}
								}
								else if (value == CANCEL_OPTION)
								{
									return 0;
								}
								else if (value == YES_TO_ALL_OPTION)
								{
									if (array[0] instanceof Synthesis)
									{
										((Synthesis) array[0]).save();
									}
									autosave = 1;
								}
								else if (value == NO_TO_ALL_OPTION)
								{
									autosave = 2;
								}
							}
							else if (autosave == 1)
							{
								if (array[0] instanceof Synthesis)
								{
									((Synthesis) array[0]).save();
								}
							}
						}
					}
				}
				else if (tab.getComponentAt(index).getName().equals("Verification"))
				{
					Component[] array = ((JPanel) tab.getComponentAt(index)).getComponents();
					if (array[0] instanceof Verification)
					{
						if (((Verification) array[0]).hasChanged())
						{
							if (autosave == 0)
							{
								int value = JOptionPane.showOptionDialog(frame, "Do you want to save verification option changes for "
										+ getTitleAt(index) + "?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
										OPTIONS, OPTIONS[0]);
								if (value == YES_OPTION)
								{
									((Verification) array[0]).save();
								}
								else if (value == CANCEL_OPTION)
								{
									return 0;
								}
								else if (value == YES_TO_ALL_OPTION)
								{
									((Verification) array[0]).save();
									autosave = 1;
								}
								else if (value == NO_TO_ALL_OPTION)
								{
									autosave = 2;
								}
							}
							else if (autosave == 1)
							{
								((Verification) array[0]).save();
							}
						}
					}
				}
			}
			if (autosave == 0)
			{
				return 1;
			}
			else if (autosave == 1)
			{
				return 2;
			}
			else
			{
				return 3;
			}
		}
	}

	/**
	 * Saves a circuit from a learn view to the project view
	 */
	public void saveGCM(String filename, String path)
	{
		try
		{
			if (overwrite(root + GlobalConstants.separator + filename, filename))
			{
				FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + filename));
				FileInputStream in = new FileInputStream(new File(path));
				int read = in.read();
				while (read != -1)
				{
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();

				BioModel bioModel = new BioModel(root);
				try {
          bioModel.load(root + GlobalConstants.separator + filename);
          GCM2SBML gcm2sbml = new GCM2SBML(bioModel);
          gcm2sbml.load(root + GlobalConstants.separator + filename);
          gcm2sbml.convertGCM2SBML(root, filename);
          String sbmlFile = filename.replace(".gcm", ".xml");
          bioModel.save(root + GlobalConstants.separator + sbmlFile);
          addToTree(sbmlFile);
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
				
			}
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to save genetic circuit.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves a circuit from a learn view to the project view
	 */
	public void saveLPN(String filename, String path)
	{
		try
		{
			if ((lpn && overwrite(root + GlobalConstants.separator + filename, filename))
					|| (!lpn && overwrite(root + GlobalConstants.separator + filename.replace(".lpn", ".xml"), filename.replace(".lpn", ".xml"))))
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(root + GlobalConstants.separator + filename));
				BufferedReader in = new BufferedReader(new FileReader(path));
				String str;
				while ((str = in.readLine()) != null)
				{
					out.write(str + "\n");
				}
				in.close();
				out.close();
				if (lpn)
				{
					addToTree(filename);
				}
				Translator t1 = new Translator();
				try {
          t1.convertLPN2SBML(root + GlobalConstants.separator + filename, "");
          t1.setFilename(root + GlobalConstants.separator + filename.replace(".lpn", ".xml"));
          t1.outputSBML();
          addToTree(filename.replace(".lpn", ".xml"));
        } catch (BioSimException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
				
			}
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to save LPN.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void copySFiles(String filename, String directory)
	{
		final String SFILELINE = "input (\\S+?)\n";

		StringBuffer data = new StringBuffer();

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(directory + GlobalConstants.separator + filename));
			String str;
			while ((str = in.readLine()) != null)
			{
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		Pattern sLinePattern = Pattern.compile(SFILELINE);
		Matcher sLineMatcher = sLinePattern.matcher(data);
		while (sLineMatcher.find())
		{
			String sFilename = sLineMatcher.group(1);
			try
			{
				File newFile = new File(directory + GlobalConstants.separator + sFilename);
				newFile.createNewFile();
				FileOutputStream copyin = new FileOutputStream(newFile);
				FileInputStream copyout = new FileInputStream(new File(root + GlobalConstants.separator + sFilename));
				int read = copyout.read();
				while (read != -1)
				{
					copyin.write(read);
					read = copyout.read();
				}
				copyin.close();
				copyout.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Cannot copy file " + sFilename, "Copy Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void updateMenu(boolean logEnabled, boolean othersEnabled)
	{
		viewLearnedModel.setEnabled(othersEnabled);
		viewCoverage.setEnabled(othersEnabled);
		save.setEnabled(othersEnabled);
		saveAll.setEnabled(othersEnabled);
		viewLog.setEnabled(logEnabled);
		// Do saveas & save button too
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		executePopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		executePopupMenu(e);
	}

	public void executePopupMenu(MouseEvent e)
	{
		if (e.getSource() instanceof JTree && tree.getFile() != null && e.isPopupTrigger())
		{
			// frame.getGlassPane().setVisible(false);
			popup.removeAll();
			if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml"))
			{
				JMenuItem create = new JMenuItem("Create Analysis View");
				create.addActionListener(this);
				create.addMouseListener(this);
				create.setActionCommand("createAnalysis");
				JMenuItem createSynthesis = new JMenuItem("Create Synthesis View");
				createSynthesis.addActionListener(this);
				createSynthesis.addMouseListener(this);
				createSynthesis.setActionCommand("createSynthesis");
				JMenuItem createLearn = new JMenuItem("Create Learn View");
				createLearn.addActionListener(this);
				createLearn.addMouseListener(this);
				createLearn.setActionCommand("createLearn");
				JMenuItem createVerification = new JMenuItem("Create Verification View");
				createVerification.addActionListener(this);
				createVerification.addMouseListener(this);
				createVerification.setActionCommand("createVerify");
				JMenuItem edit = new JMenuItem("View/Edit (graphical)");
				edit.addActionListener(this);
				edit.addMouseListener(this);
				edit.setActionCommand("modelEditor");
				JMenuItem editText = new JMenuItem("View/Edit (tabular)");
				editText.addActionListener(this);
				editText.addMouseListener(this);
				editText.setActionCommand("modelTextEditor");
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
				if (!lema)
				{
					popup.add(createSynthesis);
				}
				popup.add(createLearn);
				popup.add(createVerification);
				popup.addSeparator();
				popup.add(edit);
				popup.add(editText);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}
			else if (tree.getFile().endsWith(".sbol"))
			{
				JMenuItem view = new JMenuItem("View");
				view.addActionListener(this);
				view.addMouseListener(this);
				view.setActionCommand("SBOLDesigner");
//				JMenuItem copy = new JMenuItem("Copy");
//				copy.addActionListener(this);
//				copy.addMouseListener(this);
//				copy.setActionCommand("copy");
//				JMenuItem rename = new JMenuItem("Rename");
//				rename.addActionListener(this);
//				rename.addMouseListener(this);
//				rename.setActionCommand("rename");
//				JMenuItem delete = new JMenuItem("Delete");
//				delete.addActionListener(this);
//				delete.addMouseListener(this);
//				delete.setActionCommand("delete");
//				JMenuItem generate = new JMenuItem("Generate SBML");
//				generate.addActionListener(this);
//				generate.addMouseListener(this);
//				generate.setActionCommand("sbolToSBML");
				JMenuItem openSBOLDesigner = new JMenuItem("Open in Browser");
				openSBOLDesigner.addActionListener(this);
				openSBOLDesigner.addMouseListener(this);
				openSBOLDesigner.setActionCommand("browseSbol");
				popup.add(view);
//				popup.add(copy);
//				popup.add(rename);
//				popup.add(delete);
//				popup.add(generate);
				popup.add(openSBOLDesigner);
			}
			else if (tree.getFile().endsWith(".vhd"))
			{
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
				if (atacs)
				{
					popup.add(createSynthesis);
				}
				// popup.add(createAnalysis);
				if (lema)
				{
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
			else if (tree.getFile().endsWith(".vams"))
			{
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
				if (lema)
				{
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
			}
			else if (tree.getFile().endsWith(".sv"))
			{
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
				if (lema)
				{
					popup.add(viewModel);
					popup.addSeparator();
					popup.add(copy);
					popup.add(rename);
					popup.add(delete);
				}
			}
			else if (tree.getFile().endsWith(".g"))
			{
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
				if (atacs)
				{
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
			else if (tree.getFile().endsWith(".lpn"))
			{
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
				if (atacs)
				{
					popup.add(createSynthesis);
				}
				popup.add(createAnalysis);
				popup.add(createVerification);
				if (lema)
				{
					popup.add(createLearn);
					popup.addSeparator();
				}
				if (atacs || lema)
				{
					popup.add(convertToVerilog);
				}
				popup.add(viewModel);
				popup.addSeparator();
				popup.add(view);
				popup.add(copy);
				popup.add(rename);
				popup.add(delete);
			}

			else if (tree.getFile().endsWith(".prop"))
			{

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

				if (lema)
				{
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

			else if (tree.getFile().endsWith(".s"))
			{
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
			else if (tree.getFile().endsWith(".inst"))
			{
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
			else if (tree.getFile().endsWith(".csp"))
			{
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
				if (atacs)
				{
					popup.add(createSynthesis);
				}
				// popup.add(createAnalysis);
				if (lema)
				{
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
			else if (tree.getFile().endsWith(".hse"))
			{
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
				if (atacs)
				{
					popup.add(createSynthesis);
				}
				// popup.add(createAnalysis);
				if (lema)
				{
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
			else if (tree.getFile().endsWith(".unc"))
			{
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
			else if (tree.getFile().endsWith(".rsg"))
			{
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
			else if (tree.getFile().endsWith(".grf"))
			{
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
			else if (tree.getFile().endsWith(".prb"))
			{
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
			else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root))
			{
				boolean sim = false;
				boolean synth = false;
				boolean ver = false;
				boolean learn = false;
				for (String s : new File(tree.getFile()).list())
				{
					if (s.endsWith(".sim"))
					{
						sim = true;
					}
					if ((s.endsWith(".syn")) || s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION))
					{
						synth = true;
					}
					if (s.endsWith(".ver"))
					{
						ver = true;
					}
					if (s.endsWith(".lrn"))
					{
						learn = true;
					}
				}
				JMenuItem open;
				if (sim)
				{
					open = new JMenuItem("Open Analysis View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openSim");
					popup.add(open);
				}
				else if (synth)
				{
					open = new JMenuItem("Open Synthesis View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openSynth");
					popup.add(open);
				}
				else if (ver)
				{
					open = new JMenuItem("Open Verification View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openVerification");
					popup.add(open);
				}
				else if (learn)
				{
					open = new JMenuItem("Open Learn View");
					open.addActionListener(this);
					open.addMouseListener(this);
					open.setActionCommand("openLearn");
					popup.add(open);
				}
				if (sim || ver || synth || learn)
				{
					popup.addSeparator();
				}
				if (sim || ver || learn)
				{
					JMenuItem copy = new JMenuItem("Copy");
					copy.addActionListener(this);
					copy.addMouseListener(this);
					copy.setActionCommand("copy");
					popup.add(copy);
				}
				if (sim || ver || synth || learn)
				{
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
			if (popup.getComponentCount() != 0)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void executeMouseClickEvent(MouseEvent e)
	{
		if (!(e.getSource() instanceof JTree))
		{
			enableTabMenu(tab.getSelectedIndex());
			// frame.getGlassPane().setVisible(true);
		}
		else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && e.getSource() instanceof JTree && tree.getFile() != null)
		{
			if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbml") || tree.getFile().length() >= 4
					&& tree.getFile().substring(tree.getFile().length() - 4).equals(".xml"))
			{
				openSBML(tree.getFile());
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".gcm"))
			{
				openModelEditor(false);
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".sbol"))
			{
				openSBOLDesigner();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".vhd"))
			{
				openModel("VHDL");
			}
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".s"))
			{
				openModel("Assembly File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".inst"))
			{
				openModel("Instruction File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".prop"))
			{ // Dhanashree
				openModel("Property File");
			}
			else if (tree.getFile().length() >= 5 && tree.getFile().substring(tree.getFile().length() - 5).equals(".vams"))
			{
				openModel("Verilog-AMS");
			}
			else if (tree.getFile().length() >= 3 && tree.getFile().substring(tree.getFile().length() - 3).equals(".sv"))
			{
				openModel("SystemVerilog");
			}
			else if (tree.getFile().length() >= 2 && tree.getFile().substring(tree.getFile().length() - 2).equals(".g"))
			{
				openModel("Petri Net");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".lpn"))
			{
				openLPN();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".csp"))
			{
				openModel("CSP");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".hse"))
			{
				openModel("Handshaking Expansion");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".unc"))
			{
				openModel("Extended Burst-Mode Machine");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".rsg"))
			{
				openModel("Reduced State Graph");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".cir"))
			{
				openModel("Spice Circuit");
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".grf"))
			{
				openGraph();
			}
			else if (tree.getFile().length() >= 4 && tree.getFile().substring(tree.getFile().length() - 4).equals(".prb"))
			{
				openHistogram();
			}
			else if (new File(tree.getFile()).isDirectory() && !tree.getFile().equals(root))
			{
				boolean sim = false;
				boolean synth = false;
				boolean ver = false;
				boolean learn = false;
				for (String s : new File(tree.getFile()).list())
				{
					if (s.length() > 3 && s.substring(s.length() - 4).equals(".sim"))
					{
						sim = true;
					}
					else if ((s.length() > 3 && s.substring(s.length() - 4).equals(".syn"))
							|| s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION))
					{
						synth = true;
					}
					else if (s.length() > 3 && s.substring(s.length() - 4).equals(".ver"))
					{
						ver = true;
					}
					else if (s.length() > 3 && s.substring(s.length() - 4).equals(".lrn"))
					{
						learn = true;
					}
				}
				if (sim)
				{
					try
					{
						openAnalysisView(tree.getFile());
					}
					catch (Exception e0)
					{
						e0.printStackTrace();
					}
				}
				else if (synth)
				{
					openSynth();
				}
				else if (ver)
				{
					openVerify();
				}
				else if (learn)
				{
					if (lema)
					{
						openLearnLHPN();
					}
					else
					{
						openLearn();
					}
				}
			}
			else if (new File(tree.getFile()).isDirectory() && tree.getFile().equals(root))
			{
				tree.expandPath(tree.getRoot());
			}
		}
		else
		{
			enableTreeMenu();
			return;
		}
		enableTabMenu(tab.getSelectedIndex());
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame.getContentPane());
		if (containerPoint.y < 0)
		{ // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0)
			{
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
				component.dispatchEvent(new MouseWheelEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y,
						e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else
		{
			Component deepComponent = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
			Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, deepComponent);
			// if (deepComponent instanceof ScrollableTabPanel) {
			// deepComponent = tab.findComponentAt(componentPoint);
			// }
			deepComponent.dispatchEvent(new MouseWheelEvent(deepComponent, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x,
					componentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
		}
	}

	private String identifySBOLSynthesisPath(String actionCommand)
	{
		String[] splitCommand = actionCommand.split("_");
		String synthFilePath = root.replace(new File(root).getName(), splitCommand[0]);
		String synthFileID = "";
		for (int i = 2; i < splitCommand.length; i++)
		{
			synthFileID = synthFileID + "_" + splitCommand[i];
		}
		synthFilePath = synthFilePath + synthFileID;
		int synthIndex = 1;
		while (new File(synthFilePath + "_" + synthIndex).exists())
		{
			synthIndex++;
		}
		synthFilePath = synthFilePath + "_" + synthIndex;
		return synthFilePath;
	}

	private void synthesizeSBOL(SynthesisView synthView)
	{
		synthView.save();
		ActionEvent projectSynthesized = new ActionEvent(newProj, ActionEvent.ACTION_PERFORMED, GlobalConstants.SBOL_SYNTH_COMMAND + "_"
				+ synthView.getSpecFileID().replace(".xml", ""));
		actionPerformed(projectSynthesized);
		if (!synthView.getRootDirectory().equals(root))
		{
			// String outputFileID = synthView.getSpecFileID();
			// int version = 1;
			// while(!overwrite(root + separator + outputFileID, outputFileID))
			// {
			// outputFileID = synthView.getSpecFileID().replace(".xml", "") +
			// "_" + version + ".xml";
			// version++;
			// }
			List<String> solutionFileIDs = synthView.run(root);
			if (solutionFileIDs.size() > 0)
			{
				for (String solutionFileID : solutionFileIDs)
				{
					addToTree(solutionFileID);
				}
				ModelEditor modelEditor;
				try
				{
					modelEditor = new ModelEditor(root + GlobalConstants.separator, solutionFileIDs.get(0), this, log, false, null, null, null, false, false);
					ActionEvent applyLayout = new ActionEvent(synthView, ActionEvent.ACTION_PERFORMED, "layout_verticalHierarchical");
					modelEditor.getSchematic().actionPerformed(applyLayout);
					addTab(solutionFileIDs.get(0), modelEditor, "Model Editor");
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	private void createSBOLSynthesisView()
	{
		String specFileID = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
		String defaultSynthID = specFileID.replace(".xml", "");
		String synthID = JOptionPane.showInputDialog(frame, "Enter synthesis ID (default = " + defaultSynthID + "):", "Synthesis ID",
				JOptionPane.PLAIN_MESSAGE);
		if (synthID != null)
		{
			if (synthID.length() == 0)
			{
				synthID = defaultSynthID;
			}
			else
			{
				synthID = synthID.trim();
			}
			if (overwrite(root + GlobalConstants.separator + synthID, synthID))
			{
				SynthesisView synthView = new SynthesisView(synthID, GlobalConstants.separator, root, log);
				synthView.loadDefaultSynthesisProperties(specFileID);
				addTab(synthID, synthView, null);
				addToTree(synthID);
			}
		}
	}

	private void openSBOLSynthesisView()
	{
		Properties synthProps = SBOLUtility2.loadSBOLSynthesisProperties(tree.getFile(), GlobalConstants.separator, frame);
		if (synthProps != null)
		{
			String synthID = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
			SynthesisView synthView = new SynthesisView(synthID, GlobalConstants.separator, root, log);
			synthView.loadSynthesisProperties(synthProps);
			addTab(synthID, synthView, null);
		}
	}

	private void createAnalysisView(String modelFile) throws Exception
	{
		String modelFileName = modelFile.split(GlobalConstants.separator)[modelFile.split(GlobalConstants.separator).length - 1];
		String modelId = modelFileName.replace(".xml", "").replace(".lpn", "");
		// If model file is open, save if needed.
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(modelFileName))
			{
				tab.setSelectedIndex(i);
				if (save(i, 0) == 0)
				{
					return;
				}
				break;
			}
		}
		String simName = JOptionPane.showInputDialog(frame, "Enter analysis ID (default=" + modelId + "):", "Analysis ID", JOptionPane.PLAIN_MESSAGE);
		if (simName == null)
		{
			return;
		}
		if (simName.equals(""))
		{
			simName = modelId;
		}
		if (simName.contains("__"))
		{
			JOptionPane.showMessageDialog(frame, "Analysis view ID's are not allowed to include two consecutive underscores.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");
		if (!(IDpat.matcher(simName).matches()))
		{
			JOptionPane.showMessageDialog(Gui.frame, "An ID can only contain letters, numbers, and underscores.", "Invalid ID",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		simName = simName.trim();
		if (!overwrite(root + GlobalConstants.separator + simName, simName))
		{
			return;
		}
		new File(root + GlobalConstants.separator + simName).mkdir();
		if (modelFile.endsWith(".lpn"))
		{
			Translator t1 = new Translator();
			t1.convertLPN2SBML(modelFile, "");
			t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelId + ".xml");
			t1.outputSBML();
		}
		else
		{
			new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFileName).createNewFile();
		}
		try
		{
			FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".sim"));
			out.write((modelFileName + "\n").getBytes());
			out.close();
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(frame, "Unable to create analysis view!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		addToTree(simName);
		openAnalysisView(root + GlobalConstants.separator + simName);
	}

	private void openLearn()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
			JTabbedPane lrnTab = new JTabbedPane();
			lrnTab.addMouseListener(this);
			// String graphFile = "";
			String open = null;
			if (new File(tree.getFile()).isDirectory())
			{
				String[] list = new File(tree.getFile()).list();
				int run = 0;
				for (int i = 0; i < list.length; i++)
				{
					if (!(new File(list[i]).isDirectory()) && list[i].length() > 4)
					{
						String end = "";
						for (int j = 1; j < 5; j++)
						{
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
						{
							if (list[i].contains("run-"))
							{
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
								if (tempNum > run)
								{
									run = tempNum;
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
						else if (end.equals(".grf"))
						{
							open = tree.getFile() + GlobalConstants.separator + list[i];
						}
					}
				}
			}

			String lrnFile = tree.getFile() + GlobalConstants.separator + tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1] + ".lrn";
			String lrnFile2 = tree.getFile() + GlobalConstants.separator + ".lrn";
			Properties load = new Properties();
			String learnFile = "";
			try
			{
				if (new File(lrnFile2).exists())
				{
					FileInputStream in = new FileInputStream(new File(lrnFile2));
					load.load(in);
					in.close();
					new File(lrnFile2).delete();
				}
				if (new File(lrnFile).exists())
				{
					FileInputStream in = new FileInputStream(new File(lrnFile));
					load.load(in);
					in.close();
					if (load.containsKey("genenet.file"))
					{
						learnFile = load.getProperty("genenet.file");
						learnFile = learnFile.split(GlobalConstants.separator)[learnFile.split(GlobalConstants.separator).length - 1];
						if (learnFile.endsWith(".gcm"))
						{
							learnFile = learnFile.replace(".gcm", ".xml");
							load.setProperty("genenet.file", learnFile);
						}
					}
				}
				FileOutputStream out = new FileOutputStream(new File(lrnFile));
				load.store(out, learnFile);
				out.close();

			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(learnFile))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			if (!(new File(root + GlobalConstants.separator + learnFile).exists()))
			{
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
			lrnTab.addTab("Parameter Estimator Options", learn.getParamEstimator());
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Parameter Estimator Options");
			lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
			Graph tsdGraph = new Graph(null, "Number of molecules", tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "Time", this, open, log, null, true, true);
			// tsdGraph.addMouseListener(this);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], lrnTab, null);
		}
	}

	private void openLearnLHPN()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
			JTabbedPane lrnTab = new JTabbedPane();
			lrnTab.addMouseListener(this);
			// String graphFile = "";
			String open = null;
			if (new File(tree.getFile()).isDirectory())
			{
				String[] list = new File(tree.getFile()).list();
				int run = 0;
				for (int i = 0; i < list.length; i++)
				{
					if (!(new File(list[i]).isDirectory()) && list[i].length() > 4)
					{
						String end = "";
						for (int j = 1; j < 5; j++)
						{
							end = list[i].charAt(list[i].length() - j) + end;
						}
						if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
						{
							if (list[i].contains("run-"))
							{
								int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
								if (tempNum > run)
								{
									run = tempNum;
									// graphFile = tree.getFile() + separator +
									// list[i];
								}
							}
						}
						else if (end.equals(".grf"))
						{
							open = tree.getFile() + GlobalConstants.separator + list[i];
						}
					}
				}
			}

			String lrnFile = tree.getFile() + GlobalConstants.separator + tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1] + ".lrn";
			String lrnFile2 = tree.getFile() + GlobalConstants.separator + ".lrn";
			Properties load = new Properties();
			String learnFile = "";
			try
			{
				if (new File(lrnFile2).exists())
				{
					FileInputStream in = new FileInputStream(new File(lrnFile2));
					load.load(in);
					in.close();
					new File(lrnFile2).delete();
				}
				if (new File(lrnFile).exists())
				{
					FileInputStream in = new FileInputStream(new File(lrnFile));
					load.load(in);
					in.close();
					if (load.containsKey("genenet.file"))
					{
						learnFile = load.getProperty("genenet.file");
						learnFile = learnFile.split(GlobalConstants.separator)[learnFile.split(GlobalConstants.separator).length - 1];
					}
				}
				FileOutputStream out = new FileOutputStream(new File(lrnFile));
				load.store(out, learnFile);
				out.close();

			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(learnFile))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			if (!(new File(root + GlobalConstants.separator + learnFile).exists()))
			{
				JOptionPane.showMessageDialog(frame, "Unable to open view because " + learnFile + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			DataManager data = new DataManager(tree.getFile(), this);
			lrnTab.addTab("Data Manager", data);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Data Manager");
			LearnLPN learn = new LearnLPN(tree.getFile(), log, this);
			lrnTab.addTab("Learn Options", learn);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Learn Options");
			lrnTab.addTab("Advanced Options", learn.getAdvancedOptionsPanel());
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("Advanced Options");
			Graph tsdGraph = new Graph(null, "Number of molecules", tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]
					+ " data", "tsd.printer", tree.getFile(), "Time", this, open, log, null, true, true);
			lrnTab.addTab("TSD Graph", tsdGraph);
			lrnTab.getComponentAt(lrnTab.getComponents().length - 1).setName("TSD Graph");
			addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], lrnTab, null);
		}
	}

	private void openSynth()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
			boolean sbolSynth = false;
			if (new File(tree.getFile()).isDirectory())
			{
				String[] fileIDs = new File(tree.getFile()).list();
				for (int i = 0; i < fileIDs.length; i++)
				{
					if (fileIDs[i].endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION))
					{
						i = fileIDs.length;
						sbolSynth = true;
					}
				}
			}
			if (sbolSynth)
			{
				openSBOLSynthesisView();
			}
			else
			{
				JPanel synthPanel = new JPanel();
				// String graphFile = "";
				if (new File(tree.getFile()).isDirectory())
				{
					String[] list = new File(tree.getFile()).list();
					int run = 0;
					for (int i = 0; i < list.length; i++)
					{
						if (!(new File(list[i]).isDirectory()) && list[i].length() > 4)
						{
							String end = "";
							for (int j = 1; j < 5; j++)
							{
								end = list[i].charAt(list[i].length() - j) + end;
							}
							if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
							{
								if (list[i].contains("run-"))
								{
									int tempNum = Integer.parseInt(list[i].substring(4, list[i].length() - end.length()));
									if (tempNum > run)
									{
										run = tempNum;
										// graphFile = tree.getFile() +
										// separator +
										// list[i];
									}
								}
							}
						}
					}
				}

				String synthFile = tree.getFile() + GlobalConstants.separator + tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1] + ".syn";
				String synthFile2 = tree.getFile() + GlobalConstants.separator + ".syn";
				Properties load = new Properties();
				String synthesisFile = "";
				try
				{
					if (new File(synthFile2).exists())
					{
						FileInputStream in = new FileInputStream(new File(synthFile2));
						load.load(in);
						in.close();
						new File(synthFile2).delete();
					}
					if (new File(synthFile).exists())
					{
						FileInputStream in = new FileInputStream(new File(synthFile));
						load.load(in);
						in.close();
						if (load.containsKey("synthesis.file"))
						{
							synthesisFile = load.getProperty("synthesis.file");
							synthesisFile = synthesisFile.split(GlobalConstants.separator)[synthesisFile.split(GlobalConstants.separator).length - 1];
						}
					}
					// FileOutputStream out = new FileOutputStream(new
					// File(synthesisFile));
					// load.store(out, synthesisFile);
					// out.close();

				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
				}
				for (int i = 0; i < tab.getTabCount(); i++)
				{
					if (getTitleAt(i).equals(synthesisFile))
					{
						tab.setSelectedIndex(i);
						if (save(i, 0) == 0)
						{
							return;
						}
						break;
					}
				}
				if (!(new File(root + GlobalConstants.separator + synthesisFile).exists()))
				{
					JOptionPane.showMessageDialog(frame, "Unable to open view because " + synthesisFile + " is missing.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// if (!graphFile.equals("")) {
				Synthesis synth = new Synthesis(tree.getFile(), "flag", log, this);
				// synth.addMouseListener(this);
				synthPanel.add(synth);
				addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], synthPanel, "Synthesis");
			}
		}
	}

	private void openVerify()
	{
		boolean done = false;
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				done = true;
			}
		}
		if (!done)
		{
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

			String verName = tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1];
			String verFile = tree.getFile() + GlobalConstants.separator + verName + ".ver";
			Properties load = new Properties();
			String verifyFile = "";
			try
			{
				if (new File(verFile).exists())
				{
					FileInputStream in = new FileInputStream(new File(verFile));
					load.load(in);
					in.close();
					if (load.containsKey("verification.file"))
					{
						verifyFile = load.getProperty("verification.file");
						verifyFile = verifyFile.split(GlobalConstants.separator)[verifyFile.split(GlobalConstants.separator).length - 1];
					}
				}
				// FileOutputStream out = new FileOutputStream(new
				// File(verifyFile));
				// load.store(out, verifyFile);
				// out.close();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
			}
			for (int i = 0; i < tab.getTabCount(); i++)
			{
				if (getTitleAt(i).equals(verifyFile))
				{
					tab.setSelectedIndex(i);
					if (save(i, 0) == 0)
					{
						return;
					}
					break;
				}
			}
			if (!(new File(verFile).exists()))
			{
				JOptionPane
						.showMessageDialog(frame, "Unable to open view because " + verifyFile + " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// if (!graphFile.equals("")) {
			Verification ver = new Verification(root + GlobalConstants.separator + verName, verName, "flag", log, this, lema, atacs);
			// ver.addMouseListener(this);
			// verPanel.add(ver);
			// AbstPane abst = new AbstPane(root + separator + verName, ver,
			// "flag", log, this, lema,
			// atacs);
			// abstPanel.add(abst);
			// verTab.add("verify", verPanel);
			// verTab.add("abstract", abstPanel);
			addTab(tree.getFile().split(GlobalConstants.separator)[tree.getFile().split(GlobalConstants.separator).length - 1], ver, "Verification");
		}
	}

	// If .sim file exists, remove it as file is now called <analysisName>.sim.
	// If <analysisName>.pms file exists, move it to <analysisName.sim> unless
	// that file already exits
	private void updateSimulationFile(String fileName, String simFile)
	{
		if (new File(fileName + GlobalConstants.separator + ".sim").exists())
		{
			new File(fileName + GlobalConstants.separator + ".sim").delete();
		}
		if (new File(simFile.replace(".sim", ".pms")).exists())
		{
			if (new File(simFile).exists())
			{
				new File(simFile.replace(".sim", ".pms")).delete();
			}
			else
			{
				new File(simFile.replace(".sim", ".pms")).renameTo(new File(simFile));
			}
		}
	}

	/* Obtain model file name from sim file. */
	private String loadModelFileName(String simFile)
	{
		String modelFileName = "";
		if (new File(simFile).exists())
		{
			Scanner s;
			try
			{
				s = new Scanner(new File(simFile));
			}
			catch (FileNotFoundException e)
			{
				// JOptionPane.showMessageDialog(frame,
				// "Unable to load SBML file.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				return modelFileName;
			}
			if (s.hasNextLine())
			{
				modelFileName = s.nextLine();
				modelFileName = modelFileName.split(GlobalConstants.separator)[modelFileName.split(GlobalConstants.separator).length - 1];
			}
			s.close();
		}
		if (modelFileName.endsWith(".gcm"))
		{
			modelFileName = modelFileName.replace(".gcm", ".xml");
		}
		return modelFileName;
	}

	private String findSBMLLoadFile(String simFile, String modelFileName, String analysisName, String analysisModelFile)
	{
		String sbmlLoadFile = "";
		if (new File(simFile).exists())
		{
			sbmlLoadFile = modelFileName;
			// if (sbmlLoadFile.endsWith(".gcm")) sbmlLoadFile =
			// sbmlLoadFile.replace(".gcm", ".xml");
			if (sbmlLoadFile.equals(""))
			{
				JOptionPane.showMessageDialog(frame, "Unable to open analysis view because there is no SBML file linked to this view.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return "";
			}
			else if (!(new File(root + GlobalConstants.separator + sbmlLoadFile).exists()))
			{
				JOptionPane.showMessageDialog(frame, "Unable to open analysis view because " + sbmlLoadFile + " is missing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return "";
			}
			if (sbmlLoadFile.contains(".lpn"))
			{
				sbmlLoadFile = root + GlobalConstants.separator + analysisName + GlobalConstants.separator + sbmlLoadFile.replace(".lpn", ".xml");
			}
			else
			{
				sbmlLoadFile = root + GlobalConstants.separator + sbmlLoadFile;
			}
			/*
			 * File f = new File(sbmlLoadFile); if (!f.exists()) { sbmlLoadFile
			 * = root + separator + f.getName(); }
			 */
		}
		else
		{
			sbmlLoadFile = root + GlobalConstants.separator + analysisModelFile.split(GlobalConstants.separator)[analysisModelFile.split(GlobalConstants.separator).length - 1];
			if (!new File(sbmlLoadFile).exists())
			{
				sbmlLoadFile = analysisModelFile;
			}
		}
		if (!new File(sbmlLoadFile).exists())
		{
			JOptionPane.showMessageDialog(frame,
					"Unable to open analysis view because " + sbmlLoadFile.split(GlobalConstants.separator)[sbmlLoadFile.split(GlobalConstants.separator).length - 1]
							+ " is missing.", "Error", JOptionPane.ERROR_MESSAGE);
			return "";
		}
		return sbmlLoadFile;
	}

	private String findAnalysisModelFile(String fileName)
	{
		String analysisModelFile = "";
		String[] list = new File(fileName).list();
		for (int i = 0; i < list.length; i++)
		{
			if (!(new File(list[i]).isDirectory()))
			{
				if (list[i].endsWith(".xml"))
				{
					analysisModelFile = fileName + GlobalConstants.separator + list[i];
				}
				else if (list[i].endsWith("sbml") && analysisModelFile.equals(""))
				{
					analysisModelFile = fileName + GlobalConstants.separator + list[i];
				}
			}
		}
		if (analysisModelFile.equals(""))
		{
			JOptionPane.showMessageDialog(frame, "Unable to open analysis view because there is no model file.", "Error", JOptionPane.ERROR_MESSAGE);
			return "";
		}
		return analysisModelFile;
	}

	private void openAnalysisView(String fileName) throws Exception
	{
		if (fileName == null || fileName.equals(""))
		{
			return;
		}
		if (!((new File(fileName)).isDirectory()))
		{
			return;
		}
		String analysisName = fileName.split(GlobalConstants.separator)[fileName.split(GlobalConstants.separator).length - 1];
		// If already open, make it the selected tab and return.
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(analysisName))
			{
				tab.setSelectedIndex(i);
				return;
			}
		}
		String simFile = fileName + GlobalConstants.separator + analysisName + ".sim";
		updateSimulationFile(fileName, simFile);
		String analysisModelFile = findAnalysisModelFile(fileName);
		if (analysisModelFile.equals(""))
		{
			return;
		}

		String modelFileName = loadModelFileName(simFile);
		String sbmlLoadFile = findSBMLLoadFile(simFile, modelFileName, analysisName, analysisModelFile);
		if (sbmlLoadFile.equals(""))
		{
			return;
		}
		// If currently open and dirty, then save
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(sbmlLoadFile.split(GlobalConstants.separator)[sbmlLoadFile.split(GlobalConstants.separator).length - 1]))
			{
				tab.setSelectedIndex(i);
				if (save(i, 0) == 0)
				{
					return;
				}
				break;
			}
		}
		// Create the analysis view
		JTabbedPane simTab = new JTabbedPane();
		simTab.addMouseListener(this);
		AnalysisView analysisView;
		AbstPane lhpnAbstraction = null;
		if (modelFileName.contains(".lpn"))
		{
			lhpnAbstraction = new AbstPane(root, modelFileName, log);
			analysisView = new AnalysisView(this, log, simTab, lhpnAbstraction, root, analysisName, modelFileName);
		}
		else
		{
			analysisView = new AnalysisView(this, log, simTab, null, root, analysisName, modelFileName);
		}
		simTab.addTab("Simulation Options", analysisView);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Simulate");
		if (modelFileName.contains(".lpn"))
		{
			simTab.addTab("Advanced Options", lhpnAbstraction);
		}
		else
		{
			simTab.addTab("Advanced Options", analysisView.getAdvanced());
		}
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("");
		if (modelFileName.contains(".xml"))
		{
			ModelEditor modelEditor = new ModelEditor(root + GlobalConstants.separator, modelFileName, this, log, true, analysisName, root + GlobalConstants.separator + analysisName
					+ GlobalConstants.separator + analysisName + ".sim", analysisView, false, false);
			analysisView.setModelEditor(modelEditor);
			ElementsPanel elementsPanel = new ElementsPanel(modelEditor.getBioModel().getSBMLDocument(), 
					sedmlDocument,analysisName);
					//root + separator + analysisName + separator
					//+ analysisName + ".sim");
			modelEditor.setElementsPanel(elementsPanel);
			addModelViewTab(analysisView, simTab, modelEditor);
			simTab.addTab("Parameters", modelEditor);
			simTab.getComponentAt(simTab.getComponents().length - 1).setName("Model Editor");
		}
		Graph tsdGraph = analysisView.createGraph(fileName + GlobalConstants.separator + analysisName + ".grf");
		simTab.addTab("TSD Graph", tsdGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("TSD Graph");
		Graph probGraph = analysisView.createProbGraph(fileName + GlobalConstants.separator + analysisName + ".prb");
		simTab.addTab("Histogram", probGraph);
		simTab.getComponentAt(simTab.getComponents().length - 1).setName("Histogram");
		addTab(analysisName, simTab, null);
	}

	/**
	 * adds the tab for the modelview and the correct listener.
	 * 
	 * @return
	 */
	private void addModelViewTab(AnalysisView reb2sac, JTabbedPane tabPane, ModelEditor modelEditor)
	{

		// Add the modelview tab
		MovieContainer movieContainer = new MovieContainer(reb2sac, modelEditor.getBioModel(), this, modelEditor, lema);

		tabPane.addTab("Schematic", movieContainer);
		tabPane.getComponentAt(tabPane.getComponents().length - 1).setName("ModelViewMovie");
		// When the Graphical View panel gets clicked on, tell it to display
		// itself.
		tabPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{

				JTabbedPane selectedTab = (JTabbedPane) (e.getSource());
				if (selectedTab.getSelectedIndex() < 0)
				{
					return;
				}
				if (!(selectedTab.getComponent(selectedTab.getSelectedIndex()) instanceof JScrollPane))
				{
					JPanel selectedPanel = (JPanel) selectedTab.getComponent(selectedTab.getSelectedIndex());
					String className = selectedPanel.getClass().getName();

					// The new Schematic
					if (className.indexOf("MovieContainer") >= 0)
					{
						((MovieContainer) selectedPanel).display();
					}
				}
			}
		});
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		executeMouseClickEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{

	}

	@Override
	public void mouseExited(MouseEvent e)
	{

	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		Component glassPane = frame.getGlassPane();
		Point glassPanePoint = e.getPoint();
		// Component component = e.getComponent();
		Container container = frame.getContentPane();
		Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, frame.getContentPane());
		if (containerPoint.y < 0)
		{ // we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0)
			{
				Component component = menuBar.getComponentAt(glassPanePoint);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
				component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e
						.getClickCount(), e.isPopupTrigger()));
				frame.getGlassPane().setVisible(false);
			}
		}
		else
		{
			try
			{
				Component deepComponent = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
				Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, deepComponent);
				deepComponent.dispatchEvent(new MouseEvent(deepComponent, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x,
						componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
			}
			catch (Exception e1)
			{
			}
		}
	}

	public void windowLostFocus()
	{
	}

	public JMenuItem getExitButton()
	{
		return exit;
	}

	/**
	 * This is the main method. It excecutes the BioSim GUI FrontEnd program.
	 */
	public static void main(String args[])
	{
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			if (System.getenv("DDLD_LIBRARY_PATH")==null) {
				System.out.println("DDLD_LIBRARY_PATH is missing");
			}
		}

		boolean lemaFlag = false, atacsFlag = false, libsbmlFound = true, lpnFlag = false;
		if (args.length > 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-lema"))
				{
					lemaFlag = true;
				}
				else if (args[i].equals("-atacs"))
				{
					atacsFlag = true;
				}
				else if (args[i].equals("-lpn"))
				{
					lpnFlag = true;
				}
			}
		}
		try
		{
			System.loadLibrary("sbmlj");
			// For extra safety, check that the jar file is in the classpath.
			Class.forName("org.sbml.libsbml.libsbml");
		}
		catch (UnsatisfiedLinkError e)
		{
			libsbmlFound = false;
		}
		catch (ClassNotFoundException e)
		{
			libsbmlFound = false;
		}
		catch (SecurityException e)
		{
			libsbmlFound = false;
		}
		Runtime.getRuntime();
		int exitValue = 1;
		try
		{
			if (System.getProperty("os.name").contentEquals("Linux"))
			{
				reb2sacExecutable = "reb2sac.linux64";
			}
			else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
			{
				reb2sacExecutable = "reb2sac.mac64";
			}
			else
			{
				reb2sacExecutable = "reb2sac.exe";
			}
			ProcessBuilder ps = new ProcessBuilder(reb2sacExecutable, "");
			Map<String, String> env = ps.environment();
			if (System.getenv("BIOSIM")!=null) {
				env.put("BIOSIM", System.getenv("BIOSIM"));
			}
			if (System.getenv("LEMA")!=null) {
				env.put("LEMA", System.getenv("LEMA"));
			}
			if (System.getenv("ATACSGUI")!=null) {
				env.put("ATACSGUI", System.getenv("ATACSGUI"));
			}
			if (System.getenv("LD_LIBRARY_PATH")!=null) {
				env.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
			}
			if (System.getenv("DDLD_LIBRARY_PATH")!=null) {
				env.put("DYLD_LIBRARY_PATH", System.getenv("DDLD_LIBRARY_PATH"));
			}
			if (System.getenv("PATH")!=null) {
				env.put("PATH", System.getenv("PATH"));
			}
			envp = new String[env.size()];
			int i = 0;
			for (String envVar : env.keySet()) {
				envp[i] = envVar + "=" + env.get(envVar);
				i++;
			}
			ps.redirectErrorStream(true);
			Process reb2sac = ps.start();
			if (reb2sac != null)
			{
				exitValue = reb2sac.waitFor();
			}
			if (exitValue != 255 && exitValue != -1)
			{
				SBMLutilities.reb2sacFound = false;
			}
		}
		catch (IOException e)
		{
			SBMLutilities.reb2sacFound = false;
		}
		catch (InterruptedException e)
		{
			SBMLutilities.reb2sacFound = false;
		}
		exitValue = 1;
		try
		{
			if (System.getProperty("os.name").contentEquals("Linux"))
			{
				geneNetExecutable = "GeneNet.linux64";
			}
			else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
			{
				geneNetExecutable = "GeneNet.mac64";
			}
			else
			{
				geneNetExecutable = "GeneNet.exe";
			}
			ProcessBuilder ps = new ProcessBuilder(geneNetExecutable, "");
			Map<String, String> env = ps.environment();
			if (System.getenv("BIOSIM")!=null) {
				env.put("BIOSIM", System.getenv("BIOSIM"));
			}
			if (System.getenv("LEMA")!=null) {
				env.put("LEMA", System.getenv("LEMA"));
			}
			if (System.getenv("ATACSGUI")!=null) {
				env.put("ATACSGUI", System.getenv("ATACSGUI"));
			}
			if (System.getenv("LD_LIBRARY_PATH")!=null) {
				env.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
			}
			if (System.getenv("DDLD_LIBRARY_PATH")!=null) {
				env.put("DYLD_LIBRARY_PATH", System.getenv("DDLD_LIBRARY_PATH"));
			}
			if (System.getenv("PATH")!=null) {
				env.put("PATH", System.getenv("PATH"));
			}
			ps.redirectErrorStream(true);
			Process geneNet = ps.start();
			if (geneNet != null)
			{
				exitValue = geneNet.waitFor();
			}
			if (exitValue != 255 && exitValue != 134 && exitValue != -1)
			{
				SBMLutilities.geneNetFound = false;
			}
		}
		catch (IOException e)
		{
			SBMLutilities.geneNetFound = false;
		}
		catch (InterruptedException e)
		{
			SBMLutilities.geneNetFound = false;
		}
		new Gui(lemaFlag, atacsFlag, libsbmlFound, lpnFlag);
	}

	public static boolean isLibsbmlFound()
	{
		return SBMLutilities.libsbmlFound;
	}

	public static boolean isReb2sacFound()
	{
		return SBMLutilities.reb2sacFound;
	}

	public static String getReb2sacExecutable()
	{
		return reb2sacExecutable;
	}
	
	public static boolean isGeneNetFound()
	{
		return SBMLutilities.geneNetFound;
	}

	public static String getGeneNetExecutable()
	{
		return geneNetExecutable;
	}

	public void refreshLearn(String learnName)
	{
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (getTitleAt(i).equals(learnName))
			{
				for (int j = 0; j < ((JTabbedPane) tab.getComponentAt(i)).getComponentCount(); j++)
				{
					if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName().equals("TSD Graph"))
					{
						// if (data) {
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof Graph)
						{
							((Graph) ((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j)).refresh();
						}
						else
						{
							((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, new Graph(null, "Number of molecules", learnName + " data",
									"tsd.printer", root + GlobalConstants.separator + learnName, "Time", this, null, log, null, true, true));
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).setName("TSD Graph");
						}
					}
					else if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).getName().equals("Learn Options"))
					{
						if (((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j) instanceof LearnGCM)
						{
						}
						else
						{
							if (lema)
							{
								LearnLPN learn = new LearnLPN(root + GlobalConstants.separator + learnName, log, this);
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, learn);
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j + 1, learn.getAdvancedOptionsPanel());
							}
							else
							{
								LearnGCM learn = new LearnGCM(root + GlobalConstants.separator + learnName, log, this);
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j, learn);
								((JTabbedPane) tab.getComponentAt(i)).setComponentAt(j + 1, learn.getAdvancedOptionsPanel());
							}
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j).setName("Learn Options");
							((JTabbedPane) tab.getComponentAt(i)).getComponentAt(j + 1).setName("Advanced Options");
						}
					}
				}
			}
		}
	}

	public boolean updateOpenModelEditor(String modelName)
	{

		for (int i = 0; i < tab.getTabCount(); i++)
		{
			String tab = this.getTitleAt(i);
			if (modelName.equals(tab))
			{
				if (this.tab.getComponentAt(i) instanceof ModelEditor)
				{
					((ModelEditor) this.tab.getComponentAt(i)).reload(modelName.replace(".xml", ""));
					((ModelEditor) this.tab.getComponentAt(i)).refresh();
					((ModelEditor) this.tab.getComponentAt(i)).getSchematic().getGraph().buildGraph();
					return true;
				}
			}
		}
		return false;
	}

//	private void renameOpenModelEditors(String modelName, String oldname, String newName)
//	{
//		for (int i = 0; i < tab.getTabCount(); i++)
//		{
//			String tab = this.getTitleAt(i);
//			if (modelName.equals(tab))
//			{
//				if (this.tab.getComponentAt(i) instanceof ModelEditor)
//				{
//					((ModelEditor) this.tab.getComponentAt(i)).renameComponents(oldname, newName);
//					return;
//				}
//			}
//		}
//	}

	public void updateAsyncViews(String updatedFile)
	{
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			String tab = this.getTitleAt(i);
			String properties = root + GlobalConstants.separator + tab + GlobalConstants.separator + tab + ".ver";
			String properties1 = root + GlobalConstants.separator + tab + GlobalConstants.separator + tab + ".synth";
			String properties2 = root + GlobalConstants.separator + tab + GlobalConstants.separator + tab + ".lrn";
			if (new File(properties).exists())
			{
				Verification verify = ((Verification) (this.tab.getComponentAt(i)));
				verify.reload();
			}
			if (new File(properties1).exists())
			{
				JTabbedPane sim = ((JTabbedPane) (this.tab.getComponentAt(i)));
				for (int j = 0; j < sim.getTabCount(); j++)
				{
					if (sim.getComponentAt(j).getName().equals("Synthesis"))
					{
						((Synthesis) (sim.getComponentAt(j))).reload(updatedFile);
					}
				}
			}
			// }
			if (new File(properties2).exists())
			{
				String check = "";
				try
				{
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("learn.file"))
					{
						String[] getProp = p.getProperty("learn.file").split(GlobalConstants.separator);
						check = getProp[getProp.length - 1];
					}
					else
					{
						check = "";
					}
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
				if (check.equals(updatedFile))
				{
					JTabbedPane learn = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < learn.getTabCount(); j++)
					{
						if (learn.getComponentAt(j).getName().equals("Data Manager"))
						{
							((DataManager) (learn.getComponentAt(j))).updateSpecies();
						}
						else if (learn.getComponentAt(j).getName().equals("Learn Options"))
						{
							((LearnLPN) (learn.getComponentAt(j))).updateSpecies(root + GlobalConstants.separator + updatedFile);
							((LearnLPN) (learn.getComponentAt(j))).reload(updatedFile);
						}
						else if (learn.getComponentAt(j).getName().contains("Graph"))
						{
							((Graph) (learn.getComponentAt(j))).refresh();
						}
					}
				}
			}
		}
	}

	public void updateViews(String updatedFile)
	{

		for (int i = 0; i < tab.getTabCount(); i++)
		{
			String tab = this.getTitleAt(i);

			if (this.tab.getComponentAt(i).getName().equals("Model Editor"))
			{

				// this is so that the grid species list gets updated if there's
				// a diffusibility change
				ModelEditor modelEditor = (ModelEditor) this.tab.getComponentAt(i);
				modelEditor.getBioModel().updateGridSpecies(updatedFile.replace(".gcm", ""));
				modelEditor.getSpeciesPanel().refreshSpeciesPanel(modelEditor.getBioModel());
			}

			if (this.tab.getComponentAt(i).getName().equals("SBOL Browser"))
			{
				((SBOLBrowser2) this.tab.getComponentAt(i)).reload(this, tab);
			}

			String properties = root + GlobalConstants.separator + tab + GlobalConstants.separator + tab + ".sim";
			String properties2 = root + GlobalConstants.separator + tab + GlobalConstants.separator + tab + ".lrn";
			if (new File(properties).exists())
			{

				String check = "";
				try
				{
					Scanner s = new Scanner(new File(properties));
					if (s.hasNextLine())
					{
						check = s.nextLine();
						check = check.split(GlobalConstants.separator)[check.split(GlobalConstants.separator).length - 1];
					}
					s.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (check.equals(updatedFile))
				{
					JTabbedPane sim = ((JTabbedPane) (this.tab.getComponentAt(i)));

					for (int j = 0; j < sim.getTabCount(); j++)
					{

						if (sim.getComponentAt(j) instanceof AnalysisView)
						{
							((AnalysisView) sim.getComponentAt(j)).updateProperties();
						}
						else if (sim.getComponentAt(j).getName().equals("Model Editor"))
						{

							new File(properties).renameTo(new File(properties.replace(".sim", ".temp")));
							try
							{
								boolean dirty = ((ModelEditor) (sim.getComponentAt(j))).isDirty();
								((ModelEditor) (sim.getComponentAt(j))).saveParams(false, "", true, null);
								((ModelEditor) (sim.getComponentAt(j))).reload(check.replace(".gcm", "").replace(".xml", ""));
								((ModelEditor) (sim.getComponentAt(j))).refresh();
								((ModelEditor) (sim.getComponentAt(j))).loadParams();
								((ModelEditor) (sim.getComponentAt(j))).setDirty(dirty);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							new File(properties).delete();
							new File(properties.replace(".sim", ".temp")).renameTo(new File(properties));
							ElementsPanel elementsPanel = new ElementsPanel(((ModelEditor) (sim.getComponentAt(j))).getBioModel().getSBMLDocument(),
									sedmlDocument,tab);
									//root + separator + tab + separator + tab + ".sim");
							((ModelEditor) (sim.getComponentAt(j))).setElementsPanel(elementsPanel);

							for (int k = 0; k < sim.getTabCount(); k++)
							{

								if (sim.getComponentAt(k) instanceof MovieContainer)
								{

									// display the schematic and reload the grid
									((MovieContainer) (sim.getComponentAt(k))).display();
									((MovieContainer) (sim.getComponentAt(k))).reloadGrid();
								}
							}
						}
					}
				}
			}
			if (new File(properties2).exists())
			{

				String check = "";
				try
				{
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("genenet.file"))
					{
						String[] getProp = p.getProperty("genenet.file").split(GlobalConstants.separator);
						check = getProp[getProp.length - 1];
					}
					else
					{
						check = "";
					}
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
				if (check.equals(updatedFile))
				{
					JTabbedPane learn = ((JTabbedPane) (this.tab.getComponentAt(i)));
					for (int j = 0; j < learn.getTabCount(); j++)
					{
						if (learn.getComponentAt(j).getName().equals("Data Manager"))
						{
							((DataManager) (learn.getComponentAt(j))).updateSpecies();
						}
						else if (learn.getComponentAt(j).getName().equals("Learn Options"))
						{
							((LearnGCM) (learn.getComponentAt(j))).updateSpecies(root + GlobalConstants.separator + updatedFile);
						}
						else if (learn.getComponentAt(j).getName().contains("Graph"))
						{
							((Graph) (learn.getComponentAt(j))).refresh();
						}
					}
				}
			}
		}
	}

	private void updateViewNames(String oldname, String newname)
	{
		File work = new File(root);
		String[] fileList = work.list();
		String[] temp = oldname.split(GlobalConstants.separator);
		oldname = temp[temp.length - 1];
		for (int i = 0; i < fileList.length; i++)
		{
			String tabTitle = fileList[i];
			String properties = root + GlobalConstants.separator + tabTitle + GlobalConstants.separator + tabTitle + ".ver";
			String properties1 = root + GlobalConstants.separator + tabTitle + GlobalConstants.separator + tabTitle + ".synth";
			String properties2 = root + GlobalConstants.separator + tabTitle + GlobalConstants.separator + tabTitle + ".lrn";
			if (new File(properties).exists())
			{
				String check;
				Properties p = new Properties();
				try
				{
					FileInputStream load = new FileInputStream(new File(properties));
					p.load(load);
					load.close();
					if (p.containsKey("verification.file"))
					{
						String[] getProp = p.getProperty("verification.file").split(GlobalConstants.separator);
						check = getProp[getProp.length - 1];
					}
					else
					{
						check = "";
					}
					if (check.equals(oldname))
					{
						p.setProperty("verification.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties));
						p.store(out, properties);
					}
				}
				catch (Exception e)
				{
					// log.addText("verification");
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
			if (new File(properties1).exists())
			{
				String check;
				try
				{
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties1));
					p.load(load);
					load.close();
					if (p.containsKey("synthesis.file"))
					{
						String[] getProp = p.getProperty("synthesis.file").split(GlobalConstants.separator);
						check = getProp[getProp.length - 1];
					}
					else
					{
						check = "";
					}
					if (check.equals(oldname))
					{
						p.setProperty("synthesis.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties1));
						p.store(out, properties1);
					}
				}
				catch (Exception e)
				{
					// log.addText("synthesis");
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
			if (new File(properties2).exists())
			{
				String check = "";
				try
				{
					Properties p = new Properties();
					FileInputStream load = new FileInputStream(new File(properties2));
					p.load(load);
					load.close();
					if (p.containsKey("learn.file"))
					{
						String[] getProp = p.getProperty("learn.file").split(GlobalConstants.separator);
						check = getProp[getProp.length - 1];
					}
					else
					{
						check = "";
					}
					if (check.equals(oldname))
					{
						p.setProperty("learn.file", newname);
						FileOutputStream out = new FileOutputStream(new File(properties2));
						p.store(out, properties2);
					}
				}
				catch (Exception e)
				{
					// e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Unable to load background file.", "Error", JOptionPane.ERROR_MESSAGE);
					check = "";
				}
			}
		}
		updateAsyncViews(newname);
	}

	public void enableTabMenu(int selectedTab)
	{
		saveButton.setEnabled(false);
		saveasButton.setEnabled(false);
		runButton.setEnabled(false);
		refreshButton.setEnabled(false);
		checkButton.setEnabled(false);
		exportButton.setEnabled(false);
		save.setEnabled(false);
		saveAs.setEnabled(false);
		//saveSBOL.setEnabled(false);
		saveModel.setEnabled(false);
		saveAll.setEnabled(false);
		close.setEnabled(false);
		closeAll.setEnabled(false);
		run.setEnabled(false);
		check.setEnabled(false);
		exportMenu.setEnabled(true);
		exportArchive.setEnabled(true);
		exportSBML.setEnabled(false);
		exportFlatSBML.setEnabled(false);
		exportSBOL1.setEnabled(false);
		exportSBOL2.setEnabled(false);
		exportSynBioHub.setEnabled(false);
		exportGenBank.setEnabled(false);
		exportFasta.setEnabled(false);
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
		addModule.setEnabled(false);
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
		if (selectedTab != -1)
		{
			tab.setSelectedIndex(selectedTab);
		}
		Component comp = tab.getSelectedComponent();
		if (comp instanceof ModelEditor)
		{
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			checkButton.setEnabled(true);
			exportButton.setEnabled(true);
			save.setEnabled(true);
			saveAs.setEnabled(true);
			//saveSBOL.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			check.setEnabled(true);
			select.setEnabled(true);
			cut.setEnabled(true);
			if (!((ModelEditor) comp).isGridEditor())
			{
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
			addModule.setEnabled(true);
			undo.setEnabled(true);
			redo.setEnabled(true);
			exportMenu.setEnabled(true);
			exportSBML.setEnabled(true);
			exportFlatSBML.setEnabled(true);
			exportSBOL1.setEnabled(true);
			exportSBOL2.setEnabled(true);
			exportSynBioHub.setEnabled(true);
			exportGenBank.setEnabled(true);
			exportFasta.setEnabled(true);
			exportImageMenu.setEnabled(true);
			exportJpg.setEnabled(true);
		}
		else if (comp instanceof SBOLDesignerPlugin)
		{
			saveButton.setEnabled(true);
			checkButton.setEnabled(true);
			exportButton.setEnabled(true);
			save.setEnabled(true);
			//saveSBOL.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			check.setEnabled(true);
			exportMenu.setEnabled(true);
			exportSBOL1.setEnabled(true);
			exportSBOL2.setEnabled(true);
			exportSynBioHub.setEnabled(true);
			exportGenBank.setEnabled(true);
			exportFasta.setEnabled(true);
		}
		else if (comp instanceof SBOLBrowser2)
		{
			// save.setEnabled(true);
		}
		else if (comp instanceof LHPNEditor)
		{
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
		else if (comp instanceof Graph)
		{
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
			if (((Graph) comp).isTSDGraph())
			{
				exportDataMenu.setEnabled(true);
				exportCsv.setEnabled(true);
				exportDat.setEnabled(true);
				exportTsd.setEnabled(true);
			}
			exportEps.setEnabled(true);
			exportJpg.setEnabled(true);
			exportPdf.setEnabled(true);
			exportPng.setEnabled(true);
			exportSvg.setEnabled(true);
		}
		else if (comp instanceof JTabbedPane)
		{
			Component component = ((JTabbedPane) comp).getSelectedComponent();
			Component learnComponent = null;
			Boolean learn = false;
			Boolean learnLHPN = false;
			for (String s : new File(root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex())).list())
			{
				if (s.contains("_sg.dot"))
				{
					viewSG.setEnabled(true);
				}
			}
			for (Component c : ((JTabbedPane) comp).getComponents())
			{
				if (c instanceof LearnGCM)
				{
					learn = true;
					learnComponent = c;
				}
				else if (c instanceof LearnLPN)
				{
					learnLHPN = true;
					learnComponent = c;
				}
			}
			if (component instanceof Graph)
			{
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
				if (learn && learnComponent != null)
				{
					if (new File(root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex()) + GlobalConstants.separator + "method.gcm").exists())
					{
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					saveAsVerilog.setEnabled(false);
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null)
				{
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					saveAsVerilog.setEnabled(false);
					viewLearnedModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLPN) learnComponent).getViewCoverageEnabled());
				}
				saveAs.setEnabled(true);
				refresh.setEnabled(true);
				exportMenu.setEnabled(true);
				exportImageMenu.setEnabled(true);
				if (((Graph) component).isTSDGraph())
				{
					exportDataMenu.setEnabled(true);
					exportCsv.setEnabled(true);
					exportDat.setEnabled(true);
					exportTsd.setEnabled(true);
				}
				exportEps.setEnabled(true);
				exportJpg.setEnabled(true);
				exportPdf.setEnabled(true);
				exportPng.setEnabled(true);
				exportSvg.setEnabled(true);
			}
			else if (component instanceof AnalysisView)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
			else if (component instanceof MovieContainer)
			{
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
			else if (component instanceof ModelEditor)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
			else if (component instanceof LearnGCM)
			{
				if (new File(root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex()) + GlobalConstants.separator + "method.gcm").exists())
				{
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
			else if (component instanceof LearnLPN)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewLearnedModel.setEnabled(((LearnLPN) component).getViewLhpnEnabled());
				viewCircuit.setEnabled(((LearnLPN) component).getViewLhpnEnabled());
				viewLog.setEnabled(((LearnLPN) component).getViewLogEnabled());
				viewCoverage.setEnabled(((LearnLPN) component).getViewCoverageEnabled());
				saveModel.setEnabled(((LearnLPN) component).getViewLhpnEnabled());
			}
			else if (component instanceof DataManager)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAs.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				if (learn && learnComponent != null)
				{
					if (new File(root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex()) + GlobalConstants.separator + "method.gcm").exists())
					{
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null)
				{
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewLearnedModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLPN) learnComponent).getViewCoverageEnabled());
				}
			}
			else if (component instanceof JPanel)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				if (learn && learnComponent != null)
				{
					if (new File(root + GlobalConstants.separator + getTitleAt(tab.getSelectedIndex()) + GlobalConstants.separator + "method.gcm").exists())
					{
						viewLearnedModel.setEnabled(true);
					}
					run.setEnabled(true);
					saveModel.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewCircuit.setEnabled(((LearnGCM) learnComponent).getViewModelEnabled());
					viewLog.setEnabled(((LearnGCM) learnComponent).getViewLogEnabled());
				}
				else if (learnLHPN && learnComponent != null)
				{
					run.setEnabled(true);
					saveModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewLearnedModel.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewCircuit.setEnabled(((LearnLPN) learnComponent).getViewLhpnEnabled());
					viewLog.setEnabled(((LearnLPN) learnComponent).getViewLogEnabled());
					viewCoverage.setEnabled(((LearnLPN) learnComponent).getViewCoverageEnabled());
				}
			}
			else if (component instanceof JScrollPane)
			{
				saveButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
			}
		}
		else if (comp instanceof JPanel)
		{
			if (comp.getName().equals("Verification"))
			{
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
			else if (comp.getName().equals("Synthesis"))
			{
				saveButton.setEnabled(true);
				saveasButton.setEnabled(true);
				runButton.setEnabled(true);
				save.setEnabled(true);
				saveAll.setEnabled(true);
				close.setEnabled(true);
				closeAll.setEnabled(true);
				run.setEnabled(true);
				viewRules.setEnabled(true/*
										 * ((Synthesis)
										 * comp).getViewRulesEnabled()
										 */);
				viewTrace.setEnabled(true/*
										 * ((Synthesis)
										 * comp).getViewTraceEnabled()
										 */);
				viewCircuit.setEnabled(true/*
											 * ((Synthesis)
											 * comp).getViewCircuitEnabled()
											 */);
				viewLog.setEnabled(true/*
										 * ((Synthesis)
										 * comp).getViewLogEnabled()
										 */);
			}
		}
		else if (comp instanceof JScrollPane)
		{
			saveButton.setEnabled(true);
			saveasButton.setEnabled(true);
			save.setEnabled(true);
			saveAll.setEnabled(true);
			close.setEnabled(true);
			closeAll.setEnabled(true);
			saveAs.setEnabled(true);
		}
	}

	private void enableTreeMenu()
	{
		viewModGraph.setEnabled(false);
		viewModBrowser.setEnabled(false);
		createAnal.setEnabled(false);
		createSynth.setEnabled(false);
		createMapTech.setEnabled(false);
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
		if (tree.getFile() != null)
		{
			if (tree.getFile().equals(root))
			{
			}
			else if (tree.getFile().endsWith(".sbol")) {
			}
			else if (tree.getFile().endsWith(".sbml") || tree.getFile().endsWith(".xml"))
			{
				viewModGraph.setEnabled(true);
				viewModGraph.setActionCommand("graph");
				viewModBrowser.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createAnalysis");
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createMapTech.setEnabled(true);
				createMapTech.setActionCommand("createMapTech");
				createLearn.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewModel.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".grf"))
			{
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".vams"))
			{
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".sv"))
			{
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".g"))
			{
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createMapTech.setEnabled(true);
				createMapTech.setActionCommand("createMapTech");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".lpn"))
			{
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createAnal.setEnabled(true);
				createAnal.setActionCommand("createAnalysis");
				if (lema)
				{
					createLearn.setEnabled(true);
				}
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createMapTech.setEnabled(true);
				createMapTech.setActionCommand("createMapTech");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
				saveAsVerilog.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".hse") || tree.getFile().endsWith(".unc")
							|| tree.getFile().endsWith(".csp") || tree.getFile().endsWith(".vhd") 
							|| tree.getFile().endsWith(".rsg"))
			{
				viewModel.setEnabled(true);
				viewModGraph.setEnabled(true);
				createSynth.setEnabled(true);
				createSynth.setActionCommand("createSynthesis");
				createMapTech.setEnabled(true);
				createMapTech.setActionCommand("createMapTech");
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (tree.getFile().endsWith(".s") || tree.getFile().endsWith(".inst"))
			{
				createAnal.setEnabled(true);
				createVer.setEnabled(true);
				copy.setEnabled(true);
				rename.setEnabled(true);
				delete.setEnabled(true);
				viewLHPN.setEnabled(true);
			}
			else if (new File(tree.getFile()).isDirectory())
			{
				boolean sim = false;
				boolean synth = false;
				boolean ver = false;
				boolean learn = false;
				for (String s : new File(tree.getFile()).list())
				{
					if (s.endsWith(".sim"))
					{
						sim = true;
					}
					else if ((s.endsWith(".syn"))
							|| (s.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)))
					{
						synth = true;
					}
					else if (s.endsWith(".ver"))
					{
						ver = true;
					}
					else if (s.endsWith(".lrn"))
					{
						learn = true;
					}
				}
				if (sim || synth || ver || learn)
				{
					copy.setEnabled(true);
					rename.setEnabled(true);
					delete.setEnabled(true);
				}
			}
		}
	}

	public String getRoot()
	{
		return root;
	}

	public static boolean checkFiles(String input, String output)
	{
		input = input.replaceAll("//", "/");
		output = output.replaceAll("//", "/");
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
		{
			if (input.toLowerCase().equals(output.toLowerCase()))
			{
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(frame, "Files are the same.", "Files Same", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
				return false;
			}
		}
		else
		{
			if (input.equals(output))
			{
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(frame, "Files are the same.", "Files Same", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
				return false;
			}
		}
		return true;
	}

	public boolean overwrite(String fullPath, String name)
	{
		if (new File(fullPath).exists())
		{
			String[] views = canDelete(name);
			Object[] options = { "Overwrite", "Cancel" };
			int value;
			if (views.length == 0)
			{
				value = JOptionPane.showOptionDialog(frame, name + " already exists." + "\nDo you want to overwrite?", "Overwrite",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
			else
			{
				String view = "";
				String gcms = "";
				for (int i = 0; i < views.length; i++)
				{
					if (views[i].endsWith(".gcm"))
					{
						gcms += views[i] + "\n";
					}
					else
					{
						view += views[i] + "\n";
					}
				}
				String message;
				if (gcms.equals(""))
				{
					message = "The file, " + name + ", already exists, and\nit is linked to the following views:\n\n" + view
							+ "\n\nAre you sure you want to overwrite?";
				}
				else if (view.equals(""))
				{
					message = "The file, " + name + ", already exists, and\nit is linked to the following gcms:\n\n" + gcms
							+ "\n\nAre you sure you want to overwrite?";
				}
				else
				{
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
				// value = JOptionPane.NO_OPTION;
			}
			if (value == JOptionPane.YES_OPTION)
			{
				for (int i = 0; i < tab.getTabCount(); i++)
				{
					if (getTitleAt(i).equals(name))
					{
						tab.remove(i);
					}
				}
				File dir = new File(fullPath);
				if (dir.isDirectory())
				{
					deleteDir(dir);
				}
				else
				{
					System.gc();
					dir.delete();
				}
				return true;
			}
			return false;
		}
		return true;
	}

	public void updateTabName(String oldName, String newName)
	{
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			String tab = this.getTitleAt(i);
			if (oldName.equals(tab))
			{
				this.tab.setTitleAt(i, newName);
			}
		}
	}

	public boolean updateOpenLHPN(String lhpnName)
	{
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			String tab = this.getTitleAt(i);
			if (lhpnName.equals(tab))
			{
				if (this.tab.getComponentAt(i) instanceof LHPNEditor)
				{
					LHPNEditor newLHPN = new LHPNEditor(root, lhpnName, null, this);
					this.tab.setComponentAt(i, newLHPN);
					this.tab.getComponentAt(i).setName("LHPN Editor");
					return true;
				}
			}
		}
		return false;
	}

	private String[] canDelete(String filename)
	{
		ArrayList<String> views = new ArrayList<String>();
		String[] files = new File(root).list();
		for (String s : files)
		{
			if (new File(root + GlobalConstants.separator + s).isDirectory())
			{
				String check = "";
				if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sim").exists())
				{
					try
					{
						Scanner scan = new Scanner(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sim"));
						if (scan.hasNextLine())
						{
							check = scan.nextLine();
							check = check.split(GlobalConstants.separator)[check.split(GlobalConstants.separator).length - 1];
						}
						scan.close();
					}
					catch (Exception e)
					{
					}
				}
				else if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn").exists())
				{
					try
					{
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file"))
						{
							String[] getProp = p.getProperty("genenet.file").split(GlobalConstants.separator);
							check = getProp[getProp.length - 1];
						}
						else if (p.containsKey("learn.file"))
						{
							String[] getProp = p.getProperty("learn.file").split(GlobalConstants.separator);
							check = getProp[getProp.length - 1];
						}
						else
						{
							check = "";
						}
					}
					catch (Exception e)
					{
						check = "";
					}
				}
				else if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".ver").exists())
				{
					try
					{
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("verification.file"))
						{
							String[] getProp = p.getProperty("verification.file").split(GlobalConstants.separator);
							check = getProp[getProp.length - 1];
						}
						else
						{
							check = "";
						}
					}
					catch (Exception e)
					{
						check = "";
					}
				}
				else if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".synth").exists())
				{
					try
					{
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("synthesis.file"))
						{
							String[] getProp = p.getProperty("synthesis.file").split(GlobalConstants.separator);
							check = getProp[getProp.length - 1];
						}
						else
						{
							check = "";
						}
					}
					catch (Exception e)
					{
						check = "";
					}
				}
				else if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sbolsynth.properties").exists())
				{
					Properties synthProps = SBOLUtility2.loadSBOLSynthesisProperties(root + GlobalConstants.separator + s, GlobalConstants.separator, Gui.frame);
					if (synthProps != null)
					{
						if (synthProps.containsKey(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY))
						{
							check = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY);
						}
						else
						{
							JOptionPane.showMessageDialog(frame, "Synthesis specification property is missing.", "Missing Property",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					else
					{
						check = "";
					}
				}
				check = check.replace(".gcm", ".xml");
				if (check.equals(filename))
				{
					views.add(s);
				}
			}
			else if (s.endsWith(".xml") && filename.endsWith(".xml"))
			{
				BioModel gcm = new BioModel(root);
				try
				{
					gcm.load(root + GlobalConstants.separator + s);
					if (gcm.getSBMLComp() != null)
					{
						for (int i = 0; i < gcm.getSBMLComp().getListOfExternalModelDefinitions().size(); i++)
						{
							ExternalModelDefinition extModel = gcm.getSBMLComp().getListOfExternalModelDefinitions().get(i);
							if (extModel.getSource().equals("file:" + filename))
							{
								views.add(s);
								break;
							}
						}
					}
				}
				catch (Exception e)
				{
				}
			}
		}
		String[] usingViews = views.toArray(new String[0]);
		dataModels.biomodel.util.Utility.sort(usingViews);
		return usingViews;
	}

	private void reassignViews(String oldName, String newName)
	{
		String[] files = new File(root).list();
		for (String s : files)
		{
			if (new File(root + GlobalConstants.separator + s).isDirectory())
			{
				String check = "";
				if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sim").exists())
				{
					try
					{
						ArrayList<String> copy = new ArrayList<String>();
						Scanner scan = new Scanner(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sim"));
						if (scan.hasNextLine())
						{
							check = scan.nextLine();
							check = check.split(GlobalConstants.separator)[check.split(GlobalConstants.separator).length - 1];
							if (check.equals(oldName))
							{
								while (scan.hasNextLine())
								{
									copy.add(scan.nextLine());
								}
								scan.close();
								FileOutputStream out = new FileOutputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".sim"));
								out.write((newName + "\n").getBytes());
								for (String cop : copy)
								{
									out.write((cop + "\n").getBytes());
								}
								out.close();
							}
							else
							{
								scan.close();
							}
						}
					}
					catch (Exception e)
					{
					}
				}
				else if (new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn").exists())
				{
					try
					{
						Properties p = new Properties();
						FileInputStream load = new FileInputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn"));
						p.load(load);
						load.close();
						if (p.containsKey("genenet.file"))
						{
							String[] getProp = p.getProperty("genenet.file").split(GlobalConstants.separator);
							check = getProp[getProp.length - 1];
							if (check.equals(oldName))
							{
								p.setProperty("genenet.file", newName);
								FileOutputStream store = new FileOutputStream(new File(root + GlobalConstants.separator + s + GlobalConstants.separator + s + ".lrn"));
								p.store(store, "Learn File Data");
								store.close();
							}
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
	}

	protected JButton makeToolButton(String imageName, String actionCommand, String toolTipText)
	{
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(new ImageIcon(getClass().getResource(imageName)));
		return button;
	}

	private boolean extractModelDefinitions(CompSBMLDocumentPlugin sbmlComp, CompModelPlugin sbmlCompModel)
	{
		for (int i = 0; i < sbmlComp.getListOfModelDefinitions().size(); i++)
		{
			ModelDefinition md = sbmlComp.getListOfModelDefinitions().get(i);
			String extId = md.getId();
			if (overwrite(root + GlobalConstants.separator + extId + ".xml", extId + ".xml"))
			{
				Model model = new Model(md);
				model.unsetNamespace();
				SBMLDocument document = new SBMLDocument(GlobalConstants.SBML_LEVEL, GlobalConstants.SBML_VERSION);
				document.setModel(model);
				
				document.enablePackage(LayoutConstants.namespaceURI);
				document.enablePackage(CompConstants.namespaceURI);
				document.enablePackage(FBCConstants.namespaceURI);
				SBMLutilities.getFBCModelPlugin(document.getModel(),true);
				document.enablePackage(ArraysConstants.namespaceURI);
				CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
				CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(model);

				ArrayList<String> comps = new ArrayList<String>();
				for (int j = 0; j < documentCompModel.getListOfSubmodels().size(); j++)
				{
					String subModelType = documentCompModel.getListOfSubmodels().get(j).getModelRef();
					if (!comps.contains(subModelType))
					{
						ExternalModelDefinition extModel = documentComp.createExternalModelDefinition();
						extModel.setId(subModelType);
						extModel.setSource(subModelType + ".xml");
						comps.add(subModelType);
					}
				}
				try {
          SBMLutilities.updateReplacementsDeletions(root, document, documentComp, documentCompModel);
        } catch (XMLStreamException e) {
          JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
				SBMLutilities.checkModelCompleteness(document,true);
				SBMLWriter writer = new SBMLWriter();
				try
				{
					writer.writeSBMLToFile(document, root + GlobalConstants.separator + extId + ".xml");
				}
				catch (SBMLException e)
				{
					e.printStackTrace();
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (XMLStreamException e)
				{
					e.printStackTrace();
				}
				addToTree(extId + ".xml");
				if (sbmlComp.getListOfExternalModelDefinitions().get(extId) == null)
				{
					for (int j = 0; j < sbmlCompModel.getListOfSubmodels().size(); j++)
					{
						Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
						if (submodel.getModelRef().equals(extId))
						{
							ExternalModelDefinition extModel = sbmlComp.createExternalModelDefinition();
							extModel.setSource(extId + ".xml");
							extModel.setId(extId);
							break;
						}
					}
				}
			}
			else
			{
				return false;
			}
		}
		while (sbmlComp.getListOfModelDefinitions().size() > 0)
		{
			sbmlComp.removeModelDefinition(0);
		}
		for (int i = 0; i < sbmlComp.getListOfExternalModelDefinitions().size(); i++)
		{
			ExternalModelDefinition extModel = sbmlComp.getListOfExternalModelDefinitions().get(i);
			if (extModel.isSetModelRef())
			{
				String oldId = extModel.getId();
				extModel.setSource(extModel.getModelRef() + ".xml");
				extModel.setId(extModel.getModelRef());
				extModel.unsetModelRef();
				for (int j = 0; j < sbmlCompModel.getListOfSubmodels().size(); j++)
				{
					Submodel submodel = sbmlCompModel.getListOfSubmodels().get(j);
					if (submodel.getModelRef().equals(oldId))
					{
						submodel.setModelRef(extModel.getId());
					}
				}
			}
		}
		return true;
	}

  @Override
  public void update(Observable o, Object arg) {
    Message message = (Message) arg;
    
    if(message.isConsole())
    {
      System.out.println(message.getMessage());
    }
    else if(message.isErrorDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.ERROR_MESSAGE);
    }
    else if(message.isDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }
    else if(message.isLog())
    {
      log.addText(message.getMessage());
    }
  }

}
