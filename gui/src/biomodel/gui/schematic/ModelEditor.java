package biomodel.gui.schematic;


import java.awt.BorderLayout;
//import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
//import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import lpn.parser.Lpn2verilog;
import main.Gui;
import main.Log;
import main.util.MutableBoolean;

import org.jlibsedml.AbstractTask;
import org.jlibsedml.Change;
import org.jlibsedml.ChangeAttribute;
import org.jlibsedml.Model;
import org.jlibsedml.RemoveXML;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.jlibsedml.XPathTarget;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.jlibsedml.modelsupport.SUPPORTED_LANGUAGE;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import analysis.main.AnalysisThread;
import analysis.main.AnalysisView;
import analysis.main.ConstraintTermThread;
import analysis.main.SEDMLutilities;
import biomodel.annotation.AnnotationUtility;
import biomodel.gui.comp.ComponentsPanel;
import biomodel.gui.comp.GridPanel;
import biomodel.gui.gcm.InfluencePanel;
import biomodel.gui.gcm.PromoterPanel;
import biomodel.gui.movie.MovieContainer;
import biomodel.gui.movie.SchemeChooserPanel;
import biomodel.gui.sbmlcore.Compartments;
import biomodel.gui.sbmlcore.Constraints;
import biomodel.gui.sbmlcore.ElementsPanel;
import biomodel.gui.sbmlcore.Events;
import biomodel.gui.sbmlcore.Functions;
import biomodel.gui.sbmlcore.InitialAssignments;
import biomodel.gui.sbmlcore.ModelPanel;
import biomodel.gui.sbmlcore.MySpecies;
import biomodel.gui.sbmlcore.Parameters;
import biomodel.gui.sbmlcore.Reactions;
import biomodel.gui.sbmlcore.Rules;
import biomodel.gui.sbmlcore.SpeciesPanel;
import biomodel.gui.sbmlcore.Units;
import biomodel.gui.sbol.SBMLtoSBOL;
import biomodel.gui.schematic.Schematic;
import biomodel.gui.util.AbstractRunnableNamedButton;
import biomodel.gui.util.PropertyList;
import biomodel.gui.util.Runnable;
import biomodel.network.GeneticNetwork;
import biomodel.parser.BioModel;
import biomodel.parser.GCMParser;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.Utility;
import sbol.assembly.Assembler2;
//import sbol.assembly.Assembler2;
import sbol.assembly.AssemblyGraph2;
//import sbol.assembly.AssemblyGraph2;
import sbol.assembly.SequenceTypeValidator;
import sbol.util.SBOLFileManager2;
//import sbol.util.SBOLFileManager2;
import sbol.util.SBOLIdentityManager2;
//import sbol.util.SBOLIdentityManager2;
import sbol.util.SBOLUtility2;

/**
 * This is the GCM2SBMLEditor class. It takes in a gcm file and allows the user
 * to edit it by changing different fields displayed in a frame. It also
 * implements the ActionListener class, the MouseListener class, and the
 * KeyListener class which allows it to perform certain actions when buttons are
 * clicked on the frame, when one of the JList's items is double clicked, or
 * when text is entered into the model's ID.
 * 
 * @author Nam Nguyen
 */
public class ModelEditor extends JPanel implements ActionListener, MouseListener, ChangeListener {

	private static final long serialVersionUID = 1L;

	private String filename = "";

	private String modelId = "";

	private BioModel biomodel = null;

	private boolean paramsOnly;

	private ArrayList<String> parameterChanges;
	
	private ArrayList<String> getParams;
	
	private String paramFile, refFile, simName;

	private AnalysisView analysisView;
	
	private ElementsPanel elementsPanel;
	
	private String separator;
	
	private ModelPanel modelPanel;
	
	private Schematic schematic;

	private Compartments compartmentPanel;

	private Functions functionPanel;
	
	private MySpecies speciesPanel;
	
	private Parameters parametersPanel;
	
	private Reactions reactionPanel;

	private Units unitPanel;
	
	private Rules rulesPanel;
	
	private Events eventPanel;
	
	private Constraints consPanel;
		
	private JTabbedPane tab = null;

	private String[] options = { "Ok", "Cancel" };

	private PropertyList species = null;

	private PropertyList components = null;

	private String path = null;

	private Gui biosim = null;

	private Log log = null;
	
	private boolean textBased = false;

	private MutableBoolean dirty = new MutableBoolean(false);
	
	public ModelEditor(String path) throws Exception {
		this(path, null, null, null, false, null, null, null, false, false);
	}

	public ModelEditor(String path, String filename, Gui biosim, Log log, boolean paramsOnly,
			String simName, String paramFile, AnalysisView analysisView, boolean textBased, boolean grid) throws Exception {
		super();
		separator = Gui.separator;
		this.biosim = biosim;
		this.log = log;
		this.path = path;
		this.paramsOnly = paramsOnly;
		this.paramFile = paramFile;
		this.simName = simName;
		this.analysisView = analysisView;
		this.textBased = textBased;
		elementsPanel = null;
		getParams = new ArrayList<String>();
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
					getParams.add(refFile);
				}
				scan.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to read parameter file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				refFile = "";
			}
		}
		if (paramsOnly) {
			parameterChanges = new ArrayList<String>();
			filename = refFile;
		}
		biomodel = new BioModel(path);
		if (filename != null) {
			biomodel.load(path + separator + filename);
			this.filename = filename;
			this.modelId = filename.replace(".gcm", "").replace(".xml", "");
		}
		else {
			this.filename = "";
		}
		if (paramsOnly) {
			loadParams();
		}
		if (grid) {
			if (launchGridPanel()) {
				rebuildGui();
			}
		} else {
			buildGui();
		}
	}

	public void setParamFile(String paramFile) {
		this.paramFile = paramFile;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public void reload(String newName) {
		filename = newName + ".xml";
		modelId = newName;
		biomodel.load(path + separator + newName + ".xml");
	}
	
	public void renameComponents(String oldname, String newName) {
		for (int i = 0; i < biomodel.getSBMLComp().getListOfExternalModelDefinitions().size(); i++) {
			ExternalModelDefinition extModel = biomodel.getSBMLComp().getListOfExternalModelDefinitions().get(i);
			if (extModel.getId().equals(oldname)) {
				extModel.setId(newName);
				extModel.setSource("file://"+newName+".xml");
			}
		}
		for (int i = 0; i < biomodel.getSBMLCompModel().getListOfSubmodels().size(); i++) {
			Submodel submodel = biomodel.getSBMLCompModel().getListOfSubmodels().get(i);
			if (submodel.getModelRef().equals(oldname)) {
				submodel.setModelRef(newName);
			}
		}
		ArrayList<String> comps = new ArrayList<String>();
		for (int i = 0; i < biomodel.getSBMLCompModel().getListOfSubmodels().size(); i++) {
			Submodel submodel = biomodel.getSBMLCompModel().getListOfSubmodels().get(i);
			comps.add(submodel.getId() + " " + submodel.getModelRef() + " " + biomodel.getComponentPortMap(submodel.getId()));
		}
		components.removeAllItem();
		components.addAllItem(comps);
		schematic.getGraph().buildGraph();
	}
	
	public void refresh() {
		refreshComponentsList();		
		reloadParameters();
		if (paramsOnly) {
			compartmentPanel.refreshCompartmentPanel(biomodel);
			speciesPanel.refreshSpeciesPanel(biomodel);
			parametersPanel.refreshParameterPanel(biomodel);
			reactionPanel.refreshReactionPanel(biomodel);
		} else {
			compartmentPanel.refreshCompartmentPanel(biomodel);
			parametersPanel.refreshParameterPanel(biomodel);
			speciesPanel.refreshSpeciesPanel(biomodel);
			reactionPanel.refreshReactionPanel(biomodel);
			rulesPanel.refreshRulesPanel();
			consPanel.refreshConstraintsPanel();
			eventPanel.refreshEventsPanel();
			functionPanel.refreshFunctionsPanel();
			unitPanel.refreshUnitsPanel();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Object o = e.getSource();
			if (o instanceof PropertyList) {
				PropertyList list = (PropertyList) o;
				new EditCommand("Edit " + list.getName(), list).run();
			}
		}
		
		//System.err.println("reloading");
		schematic.reloadGrid();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	public boolean isDirty() {
		return dirty.booleanValue();
	}

	public MutableBoolean getDirty() {
		return dirty;
	}

	public boolean isParamsOnly() {
		return paramsOnly;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty.setValue(dirty);
		biosim.markTabDirty(dirty);
	}

	public BioModel getBioModel() {
		return biomodel;
	}
	
//	public HashSet<String> getSbolFiles() {
//		HashSet<String> filePaths = new HashSet<String>();
//		TreeModel tree = getGui().getFileTree().tree.getModel();
//		for (int i = 0; i < tree.getChildCount(tree.getRoot()); i++) {
//			String fileName = tree.getChild(tree.getRoot(), i).toString();
//			if (fileName.endsWith(".sbol"))
//				filePaths.add(getGui().getRoot() + Gui.separator + fileName);
//		}
//		return filePaths;
//	}

	public void save(boolean check) {
		setDirty(false);	
		speciesPanel.refreshSpeciesPanel(biomodel);
		GeneticNetwork.setRoot(path + separator);

		// Annotate SBML model with synthesized SBOL DNA component and save component to local SBOL file
		if (!biosim.lema && !biomodel.isGridEnabled()) {
			try {
				modelPanel.getSBOLField().deleteRemovedBioSimComponent();
			}
			catch (SBOLValidationException e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Error removing SBOL.", 
						"SBOL Validation Error", JOptionPane.ERROR_MESSAGE);
			}
			if (check) {
//				saveSBOL(true);
			} else {
				if (Preferences.userRoot().get(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "False").equals("True")) {
					try {
						saveSBOL2();
					}
					catch (SBOLValidationException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(Gui.frame, "Error saving SBOL.", 
								"SBOL Assembly Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		biomodel.save(path + separator + modelId + ".xml");
		log.addText("Saving SBML file:\n" + path + separator + modelId  + ".xml");
		saveAsSBOL2();
		log.addText("Converting SBML into SBOL and saving into the project's SBOL library.");
		if (check) {
			SBMLutilities.check(path + separator + modelId + ".xml",biomodel.getSBMLDocument(),false);
		}	
		biosim.updateViews(modelId + ".xml");

	}

	// Annotate SBML model with synthesized SBOL DNA component and save component to local SBOL file
//	public void saveSBOL() {
//		SBOLIdentityManager identityManager = new SBOLIdentityManager(biomodel);
//		if (identityManager.containsBioSimURI()) {
//			AssemblyGraph assemblyGraph = new AssemblyGraph(biomodel);
//			if (assemblyGraph.containsSBOL()) {
//				SBOLFileManager fileManager = new SBOLFileManager(
//						biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
//				if (fileManager.sbolFilesAreLoaded() && assemblyGraph.loadDNAComponents(fileManager)) {
//					//assemblyGraph.print();
//					String regex = SBOLUtility.convertRegexSOTermsToNumbers(
//							Preferences.userRoot().get(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, ""));
//					SequenceTypeValidator seqValidator = new SequenceTypeValidator(regex);
//					Assembler assembler = new Assembler(assemblyGraph, seqValidator);
//					DnaComponent assembledComp = assembler.assembleDNAComponent();
//					if (assembledComp != null) {
//						if (identityManager.containsPlaceHolderURI() || identityManager.loadBioSimComponent(fileManager)) {
//							identityManager.describeDNAComponent(assembledComp);
//							identityManager.identifyDNAComponent(assembledComp);
//							fileManager.saveDNAComponent(assembledComp, identityManager);
//							identityManager.replaceBioSimURI(assembledComp.getURI());
//							identityManager.annotateBioModel();
//						}
//					} else if (identityManager.containsPlaceHolderURI()) {
//						identityManager.removeBioSimURI();
//						identityManager.annotateBioModel();
//					}
//
//				} else if (identityManager.containsPlaceHolderURI()) {
//					identityManager.removeBioSimURI();
//					identityManager.annotateBioModel();
//				} 
//			} else {
//				if (identityManager.containsBioSimURI() && !identityManager.containsPlaceHolderURI()) {
//					SBOLFileManager fileManager = new SBOLFileManager(
//							biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
//					if (fileManager.sbolFilesAreLoaded())
//						fileManager.deleteDNAComponent(identityManager.getBioSimURI());
//				}
//				identityManager.removeBioSimURI();
//				identityManager.annotateBioModel();
//			} 
//		}
//	}
	
	// Annotate SBML model with synthesized SBOL DNA component and save component to local SBOL file
		public void saveSBOL2() throws SBOLValidationException 
		{
//			SBOLIdentityManager identityManager = new SBOLIdentityManager(biomodel); 
			SBOLIdentityManager2 identityManager = new SBOLIdentityManager2(biomodel); 
			if (identityManager.containsBioSimURI()) {
//				AssemblyGraph assemblyGraph = new AssemblyGraph(biomodel);
				AssemblyGraph2 assemblyGraph = new AssemblyGraph2(biomodel);
				if (assemblyGraph.containsSBOL()) {
//					SBOLFileManager fileManager = new SBOLFileManager(
//							biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
					SBOLFileManager2 fileManager = new SBOLFileManager2(
							biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
					if (fileManager.sbolFilesAreLoaded() && assemblyGraph.loadDNAComponents(fileManager)) 
					{
						//assemblyGraph.print();
						String regex = SBOLUtility2.convertRegexSOTermsToNumbers(
								Preferences.userRoot().get(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, ""));
						SequenceTypeValidator seqValidator = new SequenceTypeValidator(regex); 
//						Assembler assembler = new Assembler(assemblyGraph, seqValidator);
						Assembler2 assembler = new Assembler2(assemblyGraph, seqValidator);
						
						SBOLDocument tempSbolDoc = new SBOLDocument();
						tempSbolDoc.setDefaultURIprefix(GlobalConstants.SBOL_AUTHORITY_DEFAULT);
						
//						DnaComponent assembledComp = assembler.assembleDNAComponent();
						ComponentDefinition assembledComp = assembler.assembleDNAComponent(tempSbolDoc);
						ComponentDefinition new_assembledComp = null;
						if (assembledComp != null) 
						{
							//NOTE: Check to see if the SBOL annotation could be loaded
							if (identityManager.containsPlaceHolderURI() || identityManager.loadBioSimComponent(fileManager)) 
							{
//								new_assembledComp = identityManager.describeDNAComponent(assembledComp);
								//described_CompDef[0] = displayId
								//described_CompDef[1] = name
								//described_CompDef[2] = description
								//described_CompDef[3] = identity
								String[] described_CompDef = identityManager.describeDNAComponent(assembledComp);
								
								ComponentDefinition removeCompDef = tempSbolDoc.getComponentDefinition(assembledComp.getIdentity());
								ComponentDefinition retrievedCompDef = fileManager.getComponentDefinition(described_CompDef[0], "");
								
								if( retrievedCompDef == null)
								{
									String version = "1.0";
									if(described_CompDef[3] != null)
									{
//										new_assembledComp = new ComponentDefinition(described_CompDef[3], described_CompDef[0], version, assembledComp.getTypes());
//										new_assembledComp.createSequenceAnnotation(removeCompDef.getSequenceAnnotations().iterator().next().getDisplayId(),
//												removeCompDef.getSequenceAnnotations().iterator().next().getLocations().iterator().next().getDisplayId(), 
//												at, 
//												orientation);
//										Range r = (Range) removeCompDef.getSequenceAnnotations().iterator().next().getLocations().iterator().next();
										new_assembledComp = (ComponentDefinition) tempSbolDoc.createCopy(removeCompDef, GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], version);
//										new_assembledComp = tempSbolDoc.createComponentDefinition(described_CompDef[3], described_CompDef[0], version, assembledComp.getTypes());
										
									}
									else
									{
//										new_assembledComp = new ComponentDefinition(GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], version, assembledComp.getTypes());
										new_assembledComp = (ComponentDefinition) tempSbolDoc.createCopy(removeCompDef, GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], version);
										
									}
									new_assembledComp.setTypes(assembledComp.getTypes());
									tempSbolDoc.removeComponentDefinition(removeCompDef);
									
									if(described_CompDef[1] != null)
										new_assembledComp.setName(described_CompDef[1]);
									if(described_CompDef[2] != null)
										new_assembledComp.setDescription(described_CompDef[2]);
									
									
									Sequence new_assembledSeq = (Sequence) tempSbolDoc.createCopy(removeCompDef.getSequences().iterator().next(), GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0]+"_seq", version);
									new_assembledComp.clearSequences();
									new_assembledComp.addSequence(new_assembledSeq);
									
								}
								else
								{
									Double compDef_version = Double.parseDouble(retrievedCompDef.getVersion()) + 1;
									if(described_CompDef[3] != null)
									{
//										new_assembledComp = new ComponentDefinition(described_CompDef[3], described_CompDef[0], compDef_version.toString(), assembledComp.getTypes());
										new_assembledComp = (ComponentDefinition)tempSbolDoc.createCopy(retrievedCompDef, GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], compDef_version.toString());
									}
									else
									{
//										new_assembledComp = new ComponentDefinition(GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], compDef_version.toString(), assembledComp.getTypes());
										new_assembledComp = (ComponentDefinition) tempSbolDoc.createCopy(removeCompDef, GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0], compDef_version.toString());
										
									}	
									System.out.println("new_assembledComp id: " + new_assembledComp.getIdentity());
									new_assembledComp.setTypes(assembledComp.getTypes());
									tempSbolDoc.removeComponentDefinition(removeCompDef);
									
									
									if(described_CompDef[1] != null)
										new_assembledComp.setName(described_CompDef[1]);
									if(described_CompDef[2] != null)
										new_assembledComp.setDescription(described_CompDef[2]);
									
									
									Sequence new_assembledSeq = (Sequence) tempSbolDoc.createCopy(removeCompDef.getSequences().iterator().next(), GlobalConstants.SBOL_AUTHORITY_DEFAULT, described_CompDef[0]+"_seq", compDef_version.toString());
									new_assembledComp.clearSequences();
									new_assembledComp.addSequence(new_assembledSeq);
								}
//								identityManager.identifyDNAComponent(assembledComp); 
								fileManager.saveDNAComponent(new_assembledComp, identityManager, tempSbolDoc);
								identityManager.replaceBioSimURI(new_assembledComp.getIdentity());
								identityManager.annotateBioModel();
							}
						} else if (identityManager.containsPlaceHolderURI()) {
							identityManager.removeBioSimURI();
							identityManager.annotateBioModel();
						}

					} else if (identityManager.containsPlaceHolderURI()) {
						identityManager.removeBioSimURI();
						identityManager.annotateBioModel();
					} 
				} else 
				{
					/*
					if (identityManager.containsBioSimURI() && !identityManager.containsPlaceHolderURI()) {
						SBOLFileManager fileManager = new SBOLFileManager(
								biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
						if (fileManager.sbolFilesAreLoaded())
							fileManager.deleteDNAComponent(identityManager.getBioSimURI());
					}
					*/
					identityManager.removeBioSimURI();
					identityManager.annotateBioModel();
				} 
			}
		}
	
	// Exports SBOL DNA components associated with model itself to a new or existing SBOL file
//	public void exportSBOL() {
//		SBOLIdentityManager identityManager = new SBOLIdentityManager(biomodel);
//		if (identityManager.containsModelURIs()) {
//			SBOLFileManager fileManager = new SBOLFileManager(
//					biosim.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
//			if (fileManager.sbolFilesAreLoaded() && identityManager.loadModelComponents(fileManager)) {
//				File lastFilePath;
//				Preferences biosimrc = Preferences.userRoot();
//				if (biosimrc.get("biosim.general.export_dir", "").equals(""))
//					lastFilePath = null;
//				else 
//					lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
//				String exportFilePath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export SBOL", -1);
//				if (!exportFilePath.equals("")) {
//					biosimrc.put("biosim.general.export_dir", exportFilePath);
//					SBOLFileManager.exportDNAComponents(identityManager.getModelComponents(), exportFilePath);
//				}
//			}
//		}
//	}
	
	public void saveAsSBOL2() 
	{
		SBMLtoSBOL sbmltosbol = new SBMLtoSBOL(biosim,path,biomodel);
		sbmltosbol.saveAsSBOL(biosim.getSBOLDocument());
		biosim.writeSBOLDocument();
	}
	
	public void exportSBOL2() {
		SBMLtoSBOL sbmltosbol = new SBMLtoSBOL(biosim,path,biomodel);
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals(""))
			lastFilePath = null;
		else 
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		String exportFilePath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export SBOL", -1);
		if (!exportFilePath.equals("")) {
			biosimrc.put("biosim.general.export_dir", exportFilePath);
			sbmltosbol.export(exportFilePath);
		}
	}
	
	public void exportSBML() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBML", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
			biomodel.exportSingleFile(exportPath);
			log.addText("Exporting SBML file:\n" + exportPath + "\n");
		}
	}
	
	public void exportFlatSBML() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "SBML", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
			GCMParser parser = new GCMParser(path + separator + modelId + ".xml");
			GeneticNetwork network = null;
			BioModel bioModel = new BioModel(path);
			bioModel.load(path + separator + modelId + ".xml");
			SBMLDocument sbml = bioModel.flattenModel(true);		
			network = parser.buildNetwork(sbml);
			if (network==null) return;
			network.loadProperties(biomodel);
			network.mergeSBML(exportPath);
			log.addText("Exporting flat SBML file:\n" + exportPath + "\n");
		}
	}
	
	public void saveAsVerilog(String newName) {
		if (new File(path + separator + newName).exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
		log.addText("Saving SBML file as SystemVerilog file:\n" + path + separator + newName + "\n");
		if (biomodel.saveAsLPN(path + separator + newName.replace(".sv", ".lpn"))) {
			Lpn2verilog.convert(path + separator + newName.replace(".sv", ".lpn"));
			biosim.addToTree(newName);
		}
	}
	
	public void saveAsLPN(String newName) {
		if (new File(path + separator + newName).exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
		log.addText("Saving SBML file as LPN file:\n" + path + separator + newName + "\n");
		if (biomodel.saveAsLPN(path + separator + newName)) {
			biosim.addToTree(newName);
		}
		//biosim.updateTabName(modelId + ".xml", newName + ".xml");
		//reload(newName);
	}		
	
	public void saveAs(String newName) {
		if (new File(path + separator + newName + ".xml").exists()) {
			int value = JOptionPane.showOptionDialog(Gui.frame, newName
					+ " already exists.  Overwrite file?", "Save file", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				biomodel.save(path + separator + newName + ".xml");
				log.addText("Saving SBML file as:\n" + path + separator + newName + ".xml\n");
				biosim.addToTree(newName + ".xml");
			}
			else {
				// Do nothing
				return;
			}
		}
		else {
			biomodel.save(path + separator + newName + ".xml");
			log.addText("Saving SBML file as:\n" + path + separator + newName + ".xml\n");
			biosim.addToTree(newName + ".xml");
		}
		biosim.updateTabName(modelId + ".xml", newName + ".xml");
		reload(newName);
	}

	/**
	 * user selects a file; schematic is printed there as a JPG file
	 */
	public void saveSchematic() {
		File lastFilePath;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.export_dir", "").equals("")) {
			lastFilePath = null;
		}
		else {
			lastFilePath = new File(biosimrc.get("biosim.general.export_dir", ""));
		}
		String exportPath = main.util.Utility.browse(Gui.frame, lastFilePath, null, JFileChooser.FILES_ONLY, "Export " + "Schematic", -1);
		if (!exportPath.equals("")) {
			biosimrc.put("biosim.general.export_dir",exportPath);
            schematic.outputFrame(exportPath, false);
			log.addText("Exporting schmeatic image:\n" + exportPath + "\n");
		}
//		JFileChooser fc = new JFileChooser("Save Schematic");
//		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		
//		ExampleFileFilter jpgFilter = new ExampleFileFilter();
//		jpgFilter.addExtension("jpg");
//		jpgFilter.setDescription("Image Files");		
//		
//		fc.addChoosableFileFilter(jpgFilter);
//		fc.setAcceptAllFileFilterUsed(false);
//		fc.setFileFilter(jpgFilter);
//		
//		int returnVal = fc.showDialog(Gui.frame, "Save Schematic");
//		
//		if (returnVal == JFileChooser.APPROVE_OPTION) {
//			
//            File file = fc.getSelectedFile();
//            schematic.outputFrame(file.getAbsoluteFile().toString(), false);
//        }
	}
	
	private static void sweepHelper(ArrayList<ArrayList<Double>> sweep, String s) {
		double[] start = {0, 0};
		double[] stop = {0, 0};
		double[] step = {0, 0};
		
		String temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1).trim();
		String[] tempSlash = temp.split("/");
		start[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			start[1] = Double.parseDouble(tempSlash[1]);
			
		temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim();
		tempSlash = temp.split("/");
		stop[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			stop[1] = Double.parseDouble(tempSlash[1]);
		
		temp = (s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim();
		tempSlash = temp.split("/");
		step[0] = Double.parseDouble(tempSlash[0]);
		if (tempSlash.length == 2)
			step[1] = Double.parseDouble(tempSlash[1]);
		
		ArrayList<Double> kf = new ArrayList<Double>();
		ArrayList<Double> kr = new ArrayList<Double>();
		kf.add(start[0]);
		kr.add(start[1]);
		while (step[0] != 0 || step[1] != 0) {
			if (start[0] + step[0] > stop[0])
				step[0] = 0;
			if (start[1] + step[1] > stop[1])
				step[1] = 0;
			if (step[0] != 0 || step[1] != 0) {
				start[0] += step[0];
				start[1] += step[1];
				kf.add(start[0]);
				kr.add(start[1]);
			}
		}
		ArrayList<Double> Keq = new ArrayList<Double>();
		for (int i = 0; i < kf.size(); i++) {
			if (kr.get(i) != 0) {
				if (!Keq.contains(kf.get(i)/kr.get(i)))
						Keq.add(kf.get(i)/kr.get(i));
			} else if (!Keq.contains(kf.get(i)))
				Keq.add(kf.get(i));
		}
		sweep.add(Keq);
	}
	
	// TODO: should use SED-ML model changes here
	public void saveParams(boolean run, String stem, boolean ignoreSweep, String analysisMethod) {
		try {
			SEDMLDocument sedmlDoc = biosim.getSEDMLDocument();
			SedML sedml = sedmlDoc.getSedMLModel();
			SBMLSupport sbmlSupport = new SBMLSupport();
			String taskId = analysisView.getSimName();
			if (stem!=null && !stem.equals("")) {
				taskId = taskId + "__" + stem;
			}
			AbstractTask task = sedml.getTaskWithId(taskId);
			Model model = sedml.getModelWithId(task.getModelReference());
			if (model!=null) {
				sedml.removeModel(model);
			}
			model = new Model(task.getModelReference(),"",SUPPORTED_LANGUAGE.SBML_GENERIC.getURN(),refFile);
			sedml.addModel(model);
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((refFile + "\n").getBytes());
			for (String s : parameterChanges) {
				if (!s.trim().equals("")) {
					//out.write((s + "\n").getBytes());
					String[] change = s.split(" ");
					if (change.length==4 && change[2].equals("Modified")) {
						String target = null;
						if (change[0].contains("/")) {
							String[] reactionParam = change[0].split("/"); 
							target = sbmlSupport.getXPathForKineticLawParameter(reactionParam[0], reactionParam[1], SBMLSupport.ParameterAttribute.valueOf("value"));
						} else {
							target = sbmlSupport.getXPathForGlobalParameter(change[0], SBMLSupport.ParameterAttribute.valueOf("value"));
						}
						ChangeAttribute changeAttribute = new ChangeAttribute(new XPathTarget(target),change[3]);
						model.addChange(changeAttribute);
					} else {
						out.write((s + "\n").getBytes());
					}
				}
			}
			out.write(("\n").getBytes());
			if (elementsPanel != null) {
				for (String s : elementsPanel.getElementChanges()) {
					SBase sbase = SBMLutilities.getElementByMetaId(biomodel.getSBMLDocument().getModel(), s);				
					if (sbase==null) {
						sbase = SBMLutilities.getElementBySId(biomodel.getSBMLDocument(), s);
					}
					String target = null;
					if (sbase instanceof Event) {
						target = "/sbml:sbml/sbml:model/sbml:listOfEvents/sbml:event[@id='"+s+"']";
					} else if (sbase instanceof Constraint) {
						target = "/sbml:sbml/sbml:model/sbml:listOfConstraints/sbml:constraint[@metaid='"+s+"']";
					} else if (sbase instanceof AssignmentRule) {
						target = "/sbml:sbml/sbml:model/sbml:listOfRules/sbml:assignmentRule[@metaid='"+s+"']";
					} else if (sbase instanceof AlgebraicRule) {
						target = "/sbml:sbml/sbml:model/sbml:listOfRules/sbml:algebraicRule[@metaid='"+s+"']";
					} else if (sbase instanceof RateRule) {
						target = "/sbml:sbml/sbml:model/sbml:listOfRules/sbml:rateRule[@metaid='"+s+"']";
					} 
					if (target != null && model != null) {
						RemoveXML removeXML = new RemoveXML(new XPathTarget(target));
						model.addChange(removeXML);
					}
				}
			}
			biosim.writeSEDMLDocument();
			out.close();
			setDirty(false);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Unable to save parameter file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		if (run) {
			ArrayList<String> sweepThese1 = new ArrayList<String>();
			ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
			ArrayList<String> sweepThese2 = new ArrayList<String>();
			ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
			for (String s : parameterChanges) {
				if (s.split(" ")[s.split(" ").length - 1].startsWith("(")) {
					if ((s.split(" ")[s.split(" ").length - 1]).split(",")[3].replace(")", "").trim().equals("1")) {
						sweepThese1.add(s.substring(0, s.lastIndexOf(" ")));
						sweepHelper(sweep1, s);
					}
					else {
						sweepThese2.add(s.substring(0, s.lastIndexOf(" ")));
						sweepHelper(sweep2, s);
					}
				}
			}
			if (sweepThese1.size() == 0 && (sweepThese2.size() > 0)) {
				sweepThese1 = sweepThese2;
				sweepThese2 = new ArrayList<String>();
				sweep1 = sweep2;
				sweep2 = new ArrayList<ArrayList<Double>>();
			}
			if (sweepThese1.size() > 0) {
				ArrayList<AnalysisThread> threads = new ArrayList<AnalysisThread>();
				ArrayList<String> dirs = new ArrayList<String>();
				ArrayList<String> levelOne = new ArrayList<String>();
				int max = 0;
				for (ArrayList<Double> d : sweep1) {
					max = Math.max(max, d.size());
				}
				for (int j = 0; j < max; j++) {
					String sweep = "";
					for (int i = 0; i < sweepThese1.size(); i++) {
						int k = j;
						if (k >= sweep1.get(i).size()) {
							k = sweep1.get(i).size() - 1;
						}
						if (sweep.equals("")) {
							sweep += sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
						}
						else {
							sweep += "_" + sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
						}
					}
					if (sweepThese2.size() > 0) {
						int max2 = 0;
						for (ArrayList<Double> d : sweep2) {
							max2 = Math.max(max2, d.size());
						}
						for (int l = 0; l < max2; l++) {
							String sweepTwo = sweep;
							for (int i = 0; i < sweepThese2.size(); i++) {
								int k = l;
								if (k >= sweep2.get(i).size()) {
									k = sweep2.get(i).size() - 1;
								}
								if (sweepTwo.equals("")) {
									sweepTwo += sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
								}
								else {
									sweepTwo += "_" + sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
								}
							}
							new File(path
									+ separator
									+ simName
									+ separator
									+ stem
									+ sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
											.replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
							createSBML(stem, sweepTwo, analysisMethod);
							AnalysisThread thread = new AnalysisThread(analysisView);
							thread.start(
									stem
											+ sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
													.replace("\"", "").replace(" ", "_").replace(",", ""), false);
							threads.add(thread);
							dirs.add(sweepTwo.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
									.replace("\"", "").replace(" ", "_").replace(",", ""));
							//analysisView.emptyFrames();
							if (ignoreSweep) {
								l = max2;
								j = max;
							}
						}
					}
					else {
						new File(path
								+ separator
								+ simName
								+ separator
								+ stem
								+ sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
										.replace("\"", "").replace(" ", "_").replace(",", "")).mkdir();
						createSBML(stem, sweep, analysisMethod);
						AnalysisThread thread = new AnalysisThread(analysisView);
						thread.start(
								stem
										+ sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
												.replace("\"", "").replace(" ", "_").replace(",", ""), false);
						threads.add(thread);
						dirs.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
								.replace("\"", "").replace(" ", "_").replace(",", ""));
						//analysisView.emptyFrames();
						if (ignoreSweep) {
							j = max;
						}
					}
					levelOne.add(sweep.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "")
							.replace("\"", "").replace(" ", "_").replace(",", ""));
				}
				new ConstraintTermThread(analysisView).start(threads, dirs, levelOne, stem);
			}
			else {
				if (!stem.equals("")) {
					new File(path + separator + simName + separator + stem).mkdir();
				}
				if (createSBML(stem, ".", analysisMethod)) {
					if (!stem.equals("")) {
						new AnalysisThread(analysisView).start(stem, true);
					}
					else {
						new AnalysisThread(analysisView).start(".", true);
					}
				}
				//analysisView.emptyFrames();
			}
		}
	}

	public void loadParams() {
		if (paramsOnly) {
			getParams = new ArrayList<String>();
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					scan.nextLine();
				}
				while (scan.hasNextLine()) {
					String s = scan.nextLine();
					if (!s.trim().equals("")) {
						boolean added = false;
						for (int i = 0; i < getParams.size(); i ++) {
							if (getParams.get(i).substring(0,getParams.get(i).lastIndexOf(" ")).equals(s.substring(0, s.lastIndexOf(" ")))) {
								getParams.set(i, s);
								added = true;
							}
						}
						if (!added) {
							getParams.add(s);
						}
					}
					else {
						break;
					}
				}
				scan.close();
			}
			catch (Exception e) {
			}
			SBMLSupport sbmlSupport = new SBMLSupport();
			SedML sedml = biosim.getSEDMLDocument().getSedMLModel();
			org.jlibsedml.Model model = sedml.getModelWithId(simName+"_model");
			if (model!=null) {
				for (Change change : model.getListOfChanges()) {
					if (change instanceof ChangeAttribute) {
						ChangeAttribute changeAttribute = (ChangeAttribute)change;
						String reactionId = SEDMLutilities.getReactionIdFromXPathIdentifer(changeAttribute.getTargetXPath().getTargetAsString());
						String target = sbmlSupport.getIdFromXPathIdentifer(changeAttribute.getTargetXPath().getTargetAsString());
						if (target!=null) {
							if (reactionId!=null) {
								Reaction r = biomodel.getSBMLDocument().getModel().getReaction(reactionId);
								if (r!=null) {
									LocalParameter p = r.getKineticLaw().getLocalParameter(target);
									if (p!=null) {
										getParams.add(reactionId+"/"+target+" "+p.getValue()+" Modified "+changeAttribute.getNewValue());
									}
								}
							} else {
								Parameter p = biomodel.getSBMLDocument().getModel().getParameter(target);
								if (p!=null) {
									getParams.add(target+" "+p.getValue()+" Modified "+changeAttribute.getNewValue());
								}
							}
						}
					}
				}
			}
			for (String update : getParams) {
				String id;
				if (update.contains("/")) {
					id = update.split("/")[0];
					id = id.replace("\"", "");
					String prop = update.split("/")[1].substring(0, update.split("/")[1].indexOf(" ")).trim();
					String value = update.split(" ")[update.split(" ").length - 1].trim();
					if (prop.equals(GlobalConstants.INITIAL_STRING)) {
						Species species = biomodel.getSBMLDocument().getModel().getSpecies(id);
						if (species!=null) {
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(species, value);
							} else {
								if (value.startsWith("[")) {
									species.setInitialConcentration(Double.parseDouble(value.substring(1,value.length()-1)));
								} else {
									species.setInitialAmount(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.KDECAY_STRING)) {
						Reaction reaction = biomodel.getDegradationReaction(id);
						if (reaction != null) {
							LocalParameter kd = reaction.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
							if (kd == null) {
								kd = reaction.getKineticLaw().createLocalParameter();
								kd.setId(GlobalConstants.KDECAY_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kd, value);
							} else {
								kd.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.KCOMPLEX_STRING)) {
						Reaction reaction = biomodel.getComplexReaction(id);
						if (reaction != null) {
							LocalParameter kc_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
							if (kc_f == null) {
								kc_f = reaction.getKineticLaw().createLocalParameter();
								kc_f.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
							}
							LocalParameter kc_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
							if (kc_r == null) {
								kc_r = reaction.getKineticLaw().createLocalParameter();
								kc_r.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kc_f, value);
							} else {
								double [] Kc = Utility.getEquilibrium(value);
								kc_f.setValue(Kc[0]);
								kc_r.setValue(Kc[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.MEMDIFF_STRING)) {
						Reaction reaction = biomodel.getSBMLDocument().getModel().getReaction("MembraneDiffusion_"+id);
						if (reaction != null) {
							LocalParameter kmdiff_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
							if (kmdiff_f == null) {
								kmdiff_f = reaction.getKineticLaw().createLocalParameter();
								kmdiff_f.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
							}
							LocalParameter kmdiff_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
							if (kmdiff_r == null) {
								kmdiff_r = reaction.getKineticLaw().createLocalParameter();
								kmdiff_r.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kmdiff_f, value);
							} else {
								double [] Kmdiff = Utility.getEquilibrium(value);
								kmdiff_f.setValue(Kmdiff[0]);
								kmdiff_r.setValue(Kmdiff[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.PROMOTER_COUNT_STRING)) {
						Species species = biomodel.getSBMLDocument().getModel().getSpecies(id);
						if (species!=null) {
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(species, value);
							} else {
								species.setInitialAmount(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.RNAP_BINDING_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ko_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
							if (ko_f == null) {
								ko_f = reaction.getKineticLaw().createLocalParameter();
								ko_f.setId(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
							}
							LocalParameter ko_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
							if (ko_r == null) {
								ko_r = reaction.getKineticLaw().createLocalParameter();
								ko_r.setId(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ko_f, value);
							} else {
								double [] Ko = Utility.getEquilibrium(value);
								ko_f.setValue(Ko[0]);
								ko_r.setValue(Ko[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter kao_f = reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
							if (kao_f == null) {
								kao_f = reaction.getKineticLaw().createLocalParameter();
								kao_f.setId(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
							}
							LocalParameter kao_r = reaction.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
							if (kao_r == null) {
								kao_r = reaction.getKineticLaw().createLocalParameter();
								kao_r.setId(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kao_f, value);
							} else {
								double [] Kao = Utility.getEquilibrium(value);
								kao_f.setValue(Kao[0]);
								kao_r.setValue(Kao[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.OCR_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ko = reaction.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
							if (ko == null) {
								ko = reaction.getKineticLaw().createLocalParameter();
								ko.setId(GlobalConstants.OCR_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ko, value);
							} else {
								ko.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.KBASAL_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter kb = reaction.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
							if (kb == null) {
								kb = reaction.getKineticLaw().createLocalParameter();
								kb.setId(GlobalConstants.KBASAL_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kb, value);
							} else {
								kb.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.ACTIVATED_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter ka = reaction.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
							if (ka == null) {
								ka = reaction.getKineticLaw().createLocalParameter();
								ka.setId(GlobalConstants.ACTIVATED_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ka, value);
							} else {
								ka.setValue(Double.parseDouble(value));
							}
						}
					} else if (prop.equals(GlobalConstants.STOICHIOMETRY_STRING)) {
						Reaction reaction = biomodel.getProductionReaction(id);
						if (reaction != null) {
							LocalParameter np = reaction.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
							if (np == null) {
								np = reaction.getKineticLaw().createLocalParameter();
								np.setId(GlobalConstants.STOICHIOMETRY_STRING);
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(np, value);
							} else {
								np.setValue(Double.parseDouble(value));
							}
							for (int i = 0; i<reaction.getProductCount(); i++) {
								if (value.startsWith("(")) {
									reaction.getProduct(i).setStoichiometry(1.0);
								} else {
									reaction.getProduct(i).setStoichiometry(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.COOPERATIVITY_STRING)) {
						String promoterId = null;
						String sourceId = null;
						String complexId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							if (id.contains("|"))
								promoterId = id.substring(id.indexOf("|")+1);
							else
								promoterId = id.substring(id.indexOf(">")+1);
						}
						if (id.contains("->")||id.contains("-|")) {
							sourceId = id.substring(0,id.indexOf("-"));
						} else {
							sourceId = id.substring(0,id.indexOf("+"));
							complexId = id.substring(id.indexOf(">")+1);
						}
						Reaction reaction = null;
						if (complexId==null) {
							reaction = biomodel.getProductionReaction(promoterId);
							if (reaction != null) {
								LocalParameter nc = null;
								if (id.contains("|")) {
									nc = reaction.getKineticLaw()
											.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_r"); 
									if (nc == null) {
										nc = reaction.getKineticLaw().createLocalParameter();
										nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_r");
									}
								} else {
									nc = reaction.getKineticLaw()
											.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_a"); 
									if (nc == null) {
										nc = reaction.getKineticLaw().createLocalParameter();
										nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId + "_a");
									}
								}
								if (value.startsWith("(")) {
									AnnotationUtility.setSweepAnnotation(nc, value);
								} else {
									nc.setValue(Double.parseDouble(value));
								}
							}
						} else {
							reaction = biomodel.getComplexReaction(complexId);
							if (reaction != null) {
								LocalParameter nc = null;
								nc = reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId); 
								if (nc == null) {
									nc = reaction.getKineticLaw().createLocalParameter();
									nc.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + sourceId);
								}
								if (value.startsWith("(")) {
									AnnotationUtility.setSweepAnnotation(nc, value);
								} else {
									nc.setValue(Double.parseDouble(value));
								}
							}
						}
					} else if (prop.equals(GlobalConstants.KACT_STRING)) {
						String promoterId = null;
						String sourceId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							promoterId = id.substring(id.indexOf(">")+1);
						}
						sourceId = id.substring(0,id.indexOf("-"));
						Reaction reaction = null;
						reaction = biomodel.getProductionReaction(promoterId);
						if (reaction != null) {
							LocalParameter ka_f = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + sourceId + "_")); 
							if (ka_f == null) {
								ka_f = reaction.getKineticLaw().createLocalParameter();
								ka_f.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + sourceId + "_"));
							}
							LocalParameter ka_r = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + sourceId + "_")); 
							if (ka_r == null) {
								ka_r = reaction.getKineticLaw().createLocalParameter();
								ka_r.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + sourceId + "_"));
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(ka_f, value);
							} else {
								double [] Ka = Utility.getEquilibrium(value);
								ka_f.setValue(Ka[0]);
								ka_r.setValue(Ka[1]);
							}
						}
					} else if (prop.equals(GlobalConstants.KREP_STRING)) {
						String promoterId = null;
						String sourceId = null;
						if (id.contains(",")) {
							promoterId = id.substring(id.indexOf(",")+1);
						} else {
							promoterId = id.substring(id.indexOf("|")+1);
						}
						sourceId = id.substring(0,id.indexOf("-"));
						Reaction reaction = null;
						reaction = biomodel.getProductionReaction(promoterId);
						if (reaction != null) {
							LocalParameter kr_f = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + sourceId + "_")); 
							if (kr_f == null) {
								kr_f = reaction.getKineticLaw().createLocalParameter();
								kr_f.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + sourceId + "_"));
							}
							LocalParameter kr_r = reaction.getKineticLaw()
									.getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + sourceId + "_")); 
							if (kr_r == null) {
								kr_r = reaction.getKineticLaw().createLocalParameter();
								kr_r.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + sourceId + "_"));
							}
							if (value.startsWith("(")) {
								AnnotationUtility.setSweepAnnotation(kr_f, value);
							} else {
								double [] Kr = Utility.getEquilibrium(value);
								kr_f.setValue(Kr[0]);
								kr_r.setValue(Kr[1]);
							}
						}
					}
				}
				else {
					String [] splits = update.split(" ");
					id = splits[0];
					String value = splits[1].trim();
					if (splits[splits.length - 2].equals("Sweep")) {
						biomodel.setParameter(id, value);
					} else {
						biomodel.setParameter(id, value);
					}
				}
			}
		}
	}
	
	public SBMLDocument applyChanges(SEDMLDocument sedmlDoc, SBMLDocument sbmlDoc, Model model) throws SBMLException, XPathExpressionException, XMLStreamException, XMLException {
		SedML sedml = sedmlDoc.getSedMLModel();
		if (sedml.getModelWithId(model.getSource())!=null) {
			sbmlDoc = applyChanges(sedmlDoc,sbmlDoc,sedml.getModelWithId(model.getSource()));
		}
		SBMLWriter Xwriter = new SBMLWriter();
		SBMLReader Xreader = new SBMLReader();		
		sbmlDoc = Xreader.readSBMLFromString(sedmlDoc.getChangedModel(model.getId(),Xwriter.writeSBMLToString(sbmlDoc)));
		return sbmlDoc;
	}
	
	public void performModelChanges(String stem) {
		SEDMLDocument sedmlDoc;
		sedmlDoc = biosim.getSEDMLDocument();
		SedML sedml = sedmlDoc.getSedMLModel();
		String taskId = simName;
		if (stem!=null && !stem.equals("")) {
			taskId = taskId + "__" + stem;
		}
		AbstractTask task = sedml.getTaskWithId(taskId);
		if (task==null) return;
		//Simulation simulation = sedml.getSimulation(task.getSimulationReference());
		Model model = sedml.getModelWithId(task.getModelReference());
		if (model.getListOfChanges().size()==0) return;
		SBMLWriter Xwriter = new SBMLWriter();
		try {
			SBMLDocument sbmlDoc = SBMLReader.read(new File(path + Gui.separator + filename));
			Xwriter.write(applyChanges(sedmlDoc,sbmlDoc,model), 
					path + Gui.separator + simName + Gui.separator + filename);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean createSBML(String stem, String direct, String analysisMethod) {
		ArrayList<String> dd = new ArrayList<String>();
		if (!direct.equals(".")) {
			String[] d = direct.split("_");
			for (int i = 0; i < d.length; i++) {
				if (!d[i].contains("=")) {
					String di = d[i];
					while (!d[i].contains("=")) {
						i++;
						di += "_" + d[i];
					}
					dd.add(di);
				}
				else {
					dd.add(d[i]);
				}
			}
		}
		direct = direct.replace("/", "-").replace("-> ", "").replace("+> ", "").replace("-| ", "").replace("x> ", "").replace("\"", "").replace(" ", "_").replace(",", "");
		if (direct.equals(".") && !stem.equals("")) {
			direct = "";
		}
		if(analysisMethod != null && !analysisMethod.contains("Hierarchical"))
		{
			SBMLDocument sbml = biomodel.flattenModel(true);		
			performModifications(sbml,dd);
			if (analysisView==null || !analysisView.noExpand()) {
				GCMParser parser = new GCMParser(biomodel);
				GeneticNetwork network = parser.buildNetwork(sbml);

				if (network==null)
					return false;
				if (analysisView != null)
					network.loadProperties(biomodel, analysisView.getGcmAbstractions(), analysisView.getProperty());
				else
					network.loadProperties(biomodel);

				SBMLDocument d = network.getSBML();
				network.markAbstractable();
				network.mergeSBML(path + separator + simName + separator + stem + direct + separator + modelId + ".xml", d);
			} else {
				GeneticNetwork.reformatArrayContent(biomodel, sbml, 
						path + separator + simName + separator + stem + direct + separator + modelId + ".xml");
				SBMLWriter writer = new SBMLWriter();
				PrintStream p;
				try {
					p = new PrintStream(new FileOutputStream(path + separator + simName + separator + stem + direct + separator + modelId + ".xml"),true,"UTF-8");
					p.print(writer.writeSBMLToString(sbml));
					p.close();
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (SBMLException e) {
					e.printStackTrace();
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
				}
				performModelChanges(stem);
			}
		}
		else
		{
			biomodel.save(path + separator + simName + separator + stem + direct + separator + modelId + ".xml");
		}
		return true;
	}
	
	public ArrayList<String> getElementChanges() {
		return elementsPanel.getElementChanges();
	}
	
	private void performModifications(SBMLDocument d,ArrayList<String> dd) {
		for (String s : elementsPanel.getElementChanges()) {
			for (int i = d.getModel().getInitialAssignmentCount() - 1; i >= 0; i--) {
				if (s.contains("=")) {
					String formula = SBMLutilities.myFormulaToString(d.getModel()
							.getListOfInitialAssignments().get(i).getMath());
					String sFormula = s.substring(s.indexOf('=') + 1).trim();
					sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
					sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
					if ((d.getModel().getListOfInitialAssignments().get(i).getVariable()
							+ " = " + formula).equals(sFormula)) {
						d.getModel().getListOfInitialAssignments().remove(i);
					}
				}
			}
			for (int i = d.getModel().getConstraintCount() - 1; i >= 0; i--) {
				if (d.getModel().getListOfConstraints().get(i).getMetaId().equals(s)) {
					d.getModel().getListOfConstraints().remove(i);
				}
			}
			for (int i = d.getModel().getEventCount() - 1; i >= 0; i--) {
				if (d.getModel().getListOfEvents().get(i).getId().equals(s)) {
					d.getModel().getListOfEvents().remove(i);
				}
			}
			for (int i = d.getModel().getRuleCount() - 1; i >= 0; i--) {
				if (s.contains("=")) {
					String formula = SBMLutilities.myFormulaToString(d.getModel().getListOfRules().get(i).getMath());
					String sFormula = s.substring(s.indexOf('=') + 1).trim();
					sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
					sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
					if ((SBMLutilities.getVariable(d.getModel().getListOfRules().get(i)) + " = " + formula).equals(sFormula)) {
						d.getModel().getListOfRules().remove(i);
					}
				} else if (d.getModel().getListOfRules().get(i).getMetaId().equals(s)) {
					d.getModel().getListOfRules().remove(i);
				}
			}
		}
		for (String change : parameterChanges) {
			String[] splits = change.split(" ");
			String id = splits[0];
			String value = splits[splits.length-1];
			// Make sure it is not a sweep
			if (!value.contains("(")) {
				// Make sure not a special parameter
				if (!id.contains("/")) {
					updateValue(d,id,null,null,value,null);
				} else {
					String paramId = id.split("/")[1];
					id = id.split("/")[0];
					String factor = null;
					String type = null;
					if (id.contains("->")||id.contains("-|")) {
						if (id.contains("->")) type = "a";
						else type = "r";
						factor = id.substring(1,id.indexOf("-"));
						if (id.contains(",")) {
							id = id.substring(id.indexOf(",")+1,id.length()-1);
						} else if (id.contains(">")){
							id = id.substring(id.indexOf(">")+1,id.length()-1);
						} else {
							id = id.substring(id.indexOf("|")+1,id.length()-1);
						}
					} else if (id.contains("+")) {
						factor = id.substring(1,id.indexOf("+"));
						id = id.substring(id.indexOf(">")+1,id.length()-1);
					}
					updateValue(d,id,factor,paramId,value,type);
				}
			}
		}
		for (String di : dd) {
			String[] splits = di.split("=");
			String id = splits[0];
			String value = splits[1];
			// Make sure not a special parameter
			if (!id.contains("/")) {
				id = id.split(" ")[0];
				updateValue(d,id,null,null,value,null);
			} else {
				String paramId = id.split("/")[1];
				id = id.split("/")[0];
				String factor = null;
				String type = null;
				if (id.contains("->")||id.contains("-|")) {
					if (id.contains("->")) type = "a";
					else type = "r";
					factor = id.substring(1,id.indexOf("-"));
					if (id.contains(",")) {
						id = id.substring(id.indexOf(",")+1,id.length()-1);
					} else if (id.contains(">")){
						id = id.substring(id.indexOf(">")+1,id.length()-1);
					} else {
						id = id.substring(id.indexOf("|")+1,id.length()-1);
					}
				} else if (id.contains("+")) {
					factor = id.substring(1,id.indexOf("+"));
					id = id.substring(id.indexOf(">")+1,id.length()-1);
				}
				updateValue(d,id,factor,paramId,value,type);
			}
		}
	}
	
	private static void updateValue(SBMLDocument d,String id,String factor,String paramId,String value,String type) {
		SBase sbase = SBMLutilities.getElementBySId(d, id);
		if (d.getModel().getInitialAssignment(id)!=null) {
			d.getModel().getListOfInitialAssignments().remove(id);
		}
		if (sbase != null) {
			if (sbase.getElementName().equals(GlobalConstants.COMPARTMENT)) {
				Compartment compartment = d.getModel().getCompartment(id);
				compartment.setSize(Double.parseDouble(value));
			} else if (sbase.getElementName().equals(GlobalConstants.SBMLSPECIES)) {
				if (paramId!=null) {
					if (paramId.equals(GlobalConstants.INITIAL_STRING)||
							paramId.equals(GlobalConstants.PROMOTER_COUNT_STRING)) {
						Species species = d.getModel().getSpecies(id);
						if (species.isSetInitialAmount()) {
							species.setInitialAmount(Double.parseDouble(value));
						} else {
							species.setInitialConcentration(Double.parseDouble(value));
						}
					} else if (paramId.equals(GlobalConstants.KDECAY_STRING)) {
						Reaction degradation = BioModel.getDegradationReaction(id, d.getModel());
						if (degradation!=null && degradation.getKineticLaw()!=null) {
							LocalParameter localparam = degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
							if (localparam==null) {
								localparam = degradation.getKineticLaw().createLocalParameter();
								localparam.setId(GlobalConstants.KDECAY_STRING);
							}
							localparam.setValue(Double.parseDouble(value));
						}
					} else if (paramId.equals(GlobalConstants.KCOMPLEX_STRING)) {
						Reaction complex = BioModel.getComplexReaction(id, d.getModel());
						if (complex!=null && complex.getKineticLaw()!=null) {
							BioModel.updateComplexParameters(complex, value);
						}
					} else if (paramId.equals(GlobalConstants.COOPERATIVITY_STRING)&&type==null) {
						Reaction complex = BioModel.getComplexReaction(id, d.getModel());
						if (complex!=null && complex.getKineticLaw()!=null) {
							BioModel.updateComplexCooperativity(factor, complex, value, d.getModel());
						}
					} else if (paramId.equals(GlobalConstants.MEMDIFF_STRING)) {
						Reaction diffusion = BioModel.getDiffusionReaction(id, d.getModel());
						if (diffusion!=null && diffusion.getKineticLaw()!=null) {
							BioModel.updateDiffusionParameters(diffusion, value);
						}
					} else {
						Reaction production = BioModel.getProductionReaction(id, d.getModel());
						if (production!=null && production.getKineticLaw()!=null) {
							if (factor!=null) {
								if (paramId.equals(GlobalConstants.COOPERATIVITY_STRING)) {
									BioModel.addProductionParameters(production, factor, value, null, null,type);
								} else if (paramId.equals(GlobalConstants.KACT_STRING)) {
									BioModel.addProductionParameters(production, factor, null, value, null,null);
								} else if (paramId.equals(GlobalConstants.KREP_STRING)) {
									BioModel.addProductionParameters(production, factor, null, null, value,null);
								}
							} else if (value.contains("/")) {
								double [] K = Utility.getEquilibrium(value);
								if (K[0] >= 0) { 	
									LocalParameter localparam = 
											production.getKineticLaw().getLocalParameter(paramId.replace("K","k")+"_f");
									if (localparam==null) {
										localparam = production.getKineticLaw().createLocalParameter();
										localparam.setId(paramId);
									}
									localparam.setValue(K[0]);
									localparam = production.getKineticLaw().getLocalParameter(paramId.replace("K","k")+"_r");
									if (localparam==null) {
										localparam = production.getKineticLaw().createLocalParameter();
										localparam.setId(paramId);
									}
									localparam.setValue(K[1]);
								}
							} else {
								LocalParameter localparam = production.getKineticLaw().getLocalParameter(paramId);
								if (localparam==null) {
									localparam = production.getKineticLaw().createLocalParameter();
									localparam.setId(paramId);
								}
								localparam.setValue(Double.parseDouble(value));
							}
						}

					}
				}
			} else if (sbase.getElementName().equals(GlobalConstants.PARAMETER)) {
				Parameter parameter = d.getModel().getParameter(id);
				parameter.setValue(Double.parseDouble(value));
			} else if (sbase.getElementName().equals(GlobalConstants.SBMLREACTION)) {
				Reaction reaction = d.getModel().getReaction(id);
				if (paramId!=null && reaction.getKineticLaw()!=null) {
					LocalParameter localparam = reaction.getKineticLaw().getLocalParameter(paramId);
					if (localparam!=null) {
						localparam.setValue(Double.parseDouble(value));
					}
				}
			} 
		} 
	}

	public String getRefFile() {
		return refFile;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
	}

	public void rebuildGui() {
		removeAll();
		buildGui();
		revalidate();
		schematic.getGraph().buildGraph();
	}

	private void refreshComponentsList() {
		components.removeAllItem();
		for (int i = 0; i < biomodel.getSBMLCompModel().getListOfSubmodels().size(); i++) {
			
			Submodel submodel = biomodel.getSBMLCompModel().getListOfSubmodels().get(i);
			
			//if the submodel is gridded, then get the component names from the locations parameter
			if (biomodel.getSBMLDocument().getModel().getParameter(submodel.getId().replace("GRID__","") + "__locations") != null) {
			
				String[] compIDs = AnnotationUtility.parseArrayAnnotation(biomodel.getSBMLDocument().getModel()
						.getParameter(submodel.getId().replace("GRID__","") + "__locations"));
				
				for (int j = 1; j < compIDs.length; ++j) {
					
					if (compIDs[j].contains("=(")) {
						
						compIDs[j] = compIDs[j].split("=")[0].trim();
						
						components.addItem(compIDs[j] + " " + 
								submodel.getModelRef() + " " + biomodel.getComponentPortMap(compIDs[j]));
					}
				}
			}
			else			
				components.addItem(submodel.getId() + " " + submodel.getModelRef() + " " + biomodel.getComponentPortMap(submodel.getId()));
		}	
	}
	
	private void buildGui() {
		
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterCenter = new JPanel(new GridLayout(2, 2));
		mainPanelCenter.add(mainPanelCenterUp, BorderLayout.NORTH);
		mainPanelCenter.add(mainPanelCenterCenter, BorderLayout.CENTER);
			
		// create the modelview2 (jgraph) panel
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		JPanel propPanel = new JPanel(new BorderLayout());
		propPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");
		tab = new JTabbedPane();
		
		String file = filename.replace(".gcm", ".xml");
		
		compartmentPanel = new Compartments(biomodel,this,paramsOnly, getParams,path + separator + file, parameterChanges, false);
		reactionPanel = new Reactions(biomodel,paramsOnly,getParams,path + separator + file,parameterChanges,this);
		speciesPanel = new MySpecies(biomodel,paramsOnly,getParams,path + separator + file,parameterChanges,biomodel.getGrid().isEnabled(),
				this);
		parametersPanel = new Parameters(biomodel, this,paramsOnly, getParams,path + separator + file,parameterChanges,(paramsOnly || !textBased) && !biomodel.getGrid().isEnabled());
		rulesPanel = new Rules(biomodel, this);
		consPanel = new Constraints(biomodel, this);
		eventPanel = new Events(biosim,biomodel,this,textBased);

		JPanel compPanel = new JPanel(new BorderLayout());
		if (textBased) {
			modelPanel = new ModelPanel(biomodel, this);
			compPanel.add(modelPanel, "North");
		}
		compPanel.add(compartmentPanel,"Center");

		biomodel.setCompartmentPanel(compartmentPanel);
		biomodel.setSpeciesPanel(speciesPanel);
		biomodel.setReactionPanel(reactionPanel);
		biomodel.setRulePanel(rulesPanel);
		biomodel.setEventPanel(eventPanel);
		biomodel.setConstraintPanel(consPanel);
		biomodel.setParameterPanel(parametersPanel);
		
		components = new PropertyList("Module List");
		EditButton addInit = new EditButton("Add Module", components);
		RemoveButton removeInit = new RemoveButton("Remove Module", components);
		EditButton editInit = new EditButton("Edit Module", components);
		
		refreshComponentsList();
		
		this.getSpeciesPanel().refreshSpeciesPanel(biomodel);
		JPanel componentsPanel = Utility.createPanel(this, "Modules", components, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(componentsPanel);
		
		this.schematic = new Schematic(biomodel, biosim, this, true, null,compartmentPanel,reactionPanel,rulesPanel,
				consPanel,eventPanel,parametersPanel,biosim.lema);
		if (textBased) {
			if (!biosim.lema) {
				tab.addTab("Compartments", compPanel);
				tab.addTab("Species", speciesPanel);
				tab.addTab("Reactions", reactionPanel);
			}
			tab.addTab("Parameters", parametersPanel);
			tab.addTab("Modules", componentsPanel);
			tab.addTab("Rules", rulesPanel);
			tab.addTab("Constraints", propPanel);
			if (!biosim.lema) { 	
				tab.addTab("Events", eventPanel);
			} else {
				tab.addTab("Transitions", eventPanel);
			}
		} 
		else {
			modelPanel = schematic.getModelPanel();
			tab.addTab("Schematic", schematic);
			if (biomodel.getGrid().isEnabled()) {
				tab.addTab("Grid Species", speciesPanel);
				tab.addTab("Parameters", parametersPanel);
			} else {
				tab.addTab("Constants", parametersPanel);
			}
			
			tab.addChangeListener(this);

		}
		
		functionPanel = new Functions(biomodel,this);
		unitPanel = new Units(biomodel,this);
		//JPanel defnPanel = new JPanel(new BorderLayout());
		//defnPanel.add(mainPanelNorth, "North");
		//defnPanel.add(functionPanel,"Center");
		//defnPanel.add(unitPanel,"South");
		//tab.addTab("Definitions", defnPanel);
		tab.addTab("Functions", functionPanel);
		tab.addTab("Units", unitPanel);
		
		/*
		if (biomodel.getSBMLDocument().getLevel() < 3) {
			CompartmentTypes compTypePanel = new CompartmentTypes(biosim,biomodel,this);
			SpeciesTypes specTypePanel = new SpeciesTypes(biosim,biomodel,this);
			JPanel typePanel = new JPanel(new BorderLayout());
			typePanel.add(mainPanelNorth, "North");
			typePanel.add(compTypePanel,"Center");
			typePanel.add(specTypePanel,"South");
			tab.addTab("Types", typePanel);
		}
		*/

		InitialAssignments initialsPanel = new InitialAssignments(biomodel,this);
		compartmentPanel.setPanels(initialsPanel, rulesPanel);
		functionPanel.setPanels(initialsPanel, rulesPanel);
		speciesPanel.setPanels(reactionPanel, initialsPanel, rulesPanel, parametersPanel);
		reactionPanel.setPanels(initialsPanel, rulesPanel);
		
		setLayout(new BorderLayout());
		if (paramsOnly) {
			add(parametersPanel, BorderLayout.CENTER);
		}
		else {
			add(tab, BorderLayout.CENTER);
		}

		species = new PropertyList("Species List");
		addInit = new EditButton("Add Species", species);
		removeInit = new RemoveButton("Remove Species", species);
		if (paramsOnly) {
			addInit.setEnabled(false);
			removeInit.setEnabled(false);
		}
		editInit = new EditButton("Edit Species", species);
		if (paramsOnly) {
			ArrayList<String> specs = biomodel.getSpecies();
			ArrayList<String> promoters = biomodel.getPromoters();
			for (String s : getParams) {
				if (s.contains("-|")||s.contains("->")) {
					String factor = s.substring(1,s.indexOf("-"));
					String promoter = null;
					if (s.contains(",")) {
						promoter = s.substring(s.indexOf(",")+1,s.indexOf("/")-1);
					} else if (s.contains(">")){
						promoter = s.substring(s.indexOf(">")+1,s.indexOf("/")-1);
					} else {
						promoter = s.substring(s.indexOf("|")+1,s.indexOf("/")-1);
					}
					if (specs.contains(factor) && promoters.contains(promoter)) {
						parameterChanges.add(s);
					} 
				} else if (s.contains("+")) {
					String factor = s.substring(1,s.indexOf("+"));
					String complex = s.substring(s.indexOf(">")+1,s.indexOf("/")-1);
					if (specs.contains(factor) && specs.contains(complex)) {
						parameterChanges.add(s);
					} 
				}
			}
			for (String s : getParams) {
				if (s.contains("/") && specs.contains(s.split("/")[0].trim())) {
					specs.add(s.split("/")[0].trim());
					parameterChanges.add(s);
				}
			}
			for (String s : getParams) {
				if (!s.contains("\"")) {
					if (s.contains("/") && promoters.contains(s.split("/")[0].trim())) {
						parameterChanges.add(s);
					}
				}
			}
			for (String s : getParams) {
				if (!parameterChanges.contains(s)) {
					//System.out.println(s);
				}
			}
			species.addAllItem(specs);
		}
		else {
			species.addAllItem(biomodel.getSpecies());
		}
		JPanel initPanel = Utility.createPanel(this, "Species", species, addInit, removeInit, editInit);
		mainPanelCenterCenter.add(initPanel);
		
		parametersPanel.setPanels(initialsPanel, rulesPanel);
		propPanel.add(consPanel, "Center");
	}
	
	public void reloadParameters() {
		if (paramsOnly) {
			for (String update : parameterChanges) {
				String id;
				if (!update.contains("/")) {
					id = update.split(" ")[0];
					String value = update.split(" ")[1].trim();
					biomodel.setParameter(id, value);
				}
			}
		}
	}

	// Internal private classes used only by the gui
//	private class SaveButton extends AbstractRunnableNamedButton {
//		public SaveButton(String name, JTextField fieldNameField) {
//			super(name);
//			this.fieldNameField = fieldNameField;
//		}
//
//		public void run() {
//			save(getName());
//		}
//
//		private JTextField fieldNameField = null;
//	}

	private class RemoveButton extends AbstractRunnableNamedButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		@Override
		public void run() {
			//dirty = true;
			if (getName().contains("Influence")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					if (BioModel.removeInfluenceCheck(name)) {
						biomodel.removeInfluence(name);
						list.removeItem(name);
					}
				}
			}
			else if (getName().contains("Species")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					biomodel.removeSpecies(name);
				}
			}
			else if (getName().contains("Promoter")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					biomodel.removePromoter(name);
					list.removeItem(name);
				}
			}
			else if (getName().contains("Property")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					//gcm.removeCondition(name);
					list.removeItem(name);
				}
			}
			else if (getName().contains("Module")) {
				String name = null;
				if (list.getSelectedValue() != null) {
					name = list.getSelectedValue().toString();
					String comp = name.split(" ")[0];
					biomodel.removeComponent(comp);
					list.removeItem(name);
				}
			}
		}

		private PropertyList list = null;
	}

	private class EditButton extends AbstractRunnableNamedButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		@Override
		public void run() {
			new EditCommand(getName(), list).run();
		}

		private PropertyList list = null;
	}

	private class EditCommand implements Runnable {
		public EditCommand(String name, PropertyList list) {
			this.name = name;
			this.list = list;
		}

		@Override
		public void run() {
			
			if (name == null || name.equals("")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			if (list.getSelectedValue() == null && getName().contains("Edit")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			//dirty = true;
			if (getName().contains("Species")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ").length > 1) {
						selected = selected.split(" ")[0];
					}
				}

				launchSpeciesPanel(selected, false);

			}
			else if (getName().contains("Influence")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ")[selected.split(" ").length - 1].equals("Modified")) {
						selected = selected.substring(0, selected.length() - 9);
					}
				}
				launchInfluencePanel(selected);
			}
			else if (getName().contains("Promoter")) {
				String selected = null;
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					selected = list.getSelectedValue().toString();
					if (selected.split(" ").length > 1) {
						selected = selected.split(" ")[0];
					}
				}
				launchPromoterPanel(selected);

			}
			else if (getName().contains("Property")) {
			}
			else if (getName().contains("Module")) {
				if (list.getSelectedValue() != null && getName().contains("Edit")) {
					displayChooseComponentDialog(list, false);
				} else {
					displayChooseComponentDialog(list, true);
				}
			}
			else if (getName().contains("Parameter")) {
			}
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}
	
	/**
	 * launches the promoter panel to edit the promoter with the given id.
	 * If no id is given, then it edits a new promoter.
	 * @param id
	 * @return
	 */
	public PromoterPanel launchPromoterPanel(String id){
		BioModel refGCM = null;
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		PromoterPanel panel = new PromoterPanel(id, biomodel, species, paramsOnly, refGCM, this);	
		
		if (paramsOnly) {
			String updates = panel.updates();
			if (!updates.equals("")) {
				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
						parameterChanges.remove(i);
					}
				}
				if (updates.contains(" ")) {
					for (String s : updates.split("\n")) {
						parameterChanges.add(s);
					}
				}
			}
		}
		
		return panel;
	}

	public SpeciesPanel launchSpeciesPanel(String id, boolean inTab) {
		BioModel refGCM = null;
		
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		
		SpeciesPanel panel = new SpeciesPanel(id, species, components, biomodel, paramsOnly, refGCM, this, inTab);
		
//		if (paramsOnly) {
//			String updates = panel.updates();
//			if (!updates.equals("")) {
//				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
//					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
//						parameterChanges.remove(i);
//					}
//				}
//				if (updates.contains(" ")) {
//					for (String s : updates.split("\n")) {
//						parameterChanges.add(s);
//					}
//				}
//			}
//		}
		
		return panel;
	}
	
	public InfluencePanel launchInfluencePanel(String id){
		BioModel refGCM = null;
		if (paramsOnly) {
			refGCM = new BioModel(path);
			refGCM.load(path + separator + refFile);
		}
		InfluencePanel panel = new InfluencePanel(id, biomodel, paramsOnly, refGCM, this);
		
		if (paramsOnly) {
			String updates = panel.updates();
			if (!updates.equals("")) {
				for (int i = parameterChanges.size() - 1; i >= 0; i--) {
					if (parameterChanges.get(i).startsWith(updates.split("/")[0])) {
						parameterChanges.remove(i);
					}
				}
				if (!updates.endsWith("/")) {
					for (String s : updates.split("\n")) {
						parameterChanges.add(s);
					}
				}
			}
		}
		return panel;
	}
	
	public void launchComponentPanel(String id){
		/*
		BioModel refBioModel = null;
		
		if (paramsOnly) {
			refBioModel = new BioModel(path);
			refBioModel.load(path + separator + refFile);
		}
		*/
		
		// TODO: This is a messy way to do things. We set the selected component in the list
		// and then call displayChooseComponentDialog(). This makes for tight coupling with the
		// component list.
		for (int i = 0; i < this.components.getModel().getSize(); i++) {
			String componentsListRow = this.components.getModel().getElementAt(i).toString();
			String componentsListId = componentsListRow.split(" ")[0];
			if (componentsListId.equals(id)) {
				this.components.setSelectedIndex(i);
				break;
			}
		}
		displayChooseComponentDialog(this.components, false);
	}
	
	/**
	 * launches a panel for grid creation
	 */
	public boolean launchGridPanel() {
		
		//static method that builds the grid panel
		//the false field means to open the grid creation panel
		//and not the grid editing panel
		GridPanel gridPanel = new GridPanel(this,biomodel);
		boolean created = gridPanel.showGridPanel(false);

		//if the grid is built, then draw it and so on
		if (created) {
			
			this.setDirty(true);
			return true;
			//this.refresh();
			//schematic.getGraph().buildGraph();
			//schematic.display();
			//biomodel.makeUndoPoint();
		}
		return false;
	}
	
	public static SchemeChooserPanel getSchemeChooserPanel(
			String cellID, MovieContainer movieContainer, boolean inTab) {
		
		return new SchemeChooserPanel(cellID, movieContainer, inTab);
	}
	
	
	public boolean checkNoComponentLoop(String gcm, String checkFile) {
		gcm = gcm.replace(".gcm", ".xml");
		boolean check = true;
		BioModel g = new BioModel(path);
		g.load(path + separator + checkFile);
		for (int i = 0; i < g.getSBMLComp().getListOfExternalModelDefinitions().size(); i++) {
			String compGCM = g.getSBMLComp().getListOfExternalModelDefinitions().get(i).getSource().substring(7);
			if (compGCM.equals(gcm)) {
				return false;
			}
			check = checkNoComponentLoop(gcm, compGCM);
		}
		return check;
	}
	
	
	/**
	 * @return a list of all gcm files that can be included.
	 */
	public ArrayList<String> getComponentsList(){

		// get a list of components
		ArrayList<String> components = new ArrayList<String>();
		for (String s : new File(path).list()) {
			if (s.endsWith(".xml") && !s.equals(filename) /*&& checkNoComponentLoop(filename, s)*/) {
				components.add(s);
			}
		}
		main.util.Utility.sort(components);
		
		return components;
	}
	
	public ArrayList<String> getParameterChanges() {
		return parameterChanges;
	}
	
	/*
	 * 
	 * Displays the "Choose Component" dialog and then adds the component afterward.
	 * @param tryEdit: if true then try to bring up the edit window if a component is selected.
	 * @param list: The PropertiesList. If left null then the gcm2sbmleditor's component
	 * 				list will be used.
	 * @param createUsingDefaults:  If true then a component will be created with a basic name and
	 * 				no port mappings. Otherwise the user will be asked for 
	 * 				the name and mappings.
	 * 
	 * @return: the id of the component that was edited or created.
	 */
	public void displayChooseComponentDialog(PropertyList list, boolean create){
		
		String outID = null;
		
		if(list == null)
			list = this.components;
		String selected = null;
		String comp = null;
		if (list.getSelectedValue() != null && !create) {
			selected = list.getSelectedValue().toString();
			comp = selected.split(" ")[1] + ".xml";
		}
		else {
			ArrayList<String> components = getComponentsList();

			if (components.size() == 0) {
				JOptionPane.showMessageDialog(Gui.frame,
						"There aren't any other models to use as modules."
								+ "\nCreate a new model or import a model into the project first.",
						"Add Another Model To The Project", JOptionPane.ERROR_MESSAGE);
			}
			else {
				comp = (String) JOptionPane.showInputDialog(Gui.frame,
						"Choose a model to use as a module:", "Module Editor",
						JOptionPane.PLAIN_MESSAGE, null, components.toArray(new String[0]), null);
			}
		}
		if (comp != null && !comp.equals("")) {
			BioModel subBioModel = new BioModel(path);
			subBioModel.load(path + separator + comp);
			subBioModel.flattenBioModel();
			String oldPort = null;
			if (selected != null) {
				oldPort = selected.substring(selected.split(" ")[0].length()
						+ selected.split(" ")[1].length() + 2);
				selected = selected.split(" ")[0];
			}

			ArrayList<String> ports = subBioModel.getPorts();

			if(create){
				String md5 = Utility.MD5(subBioModel.getSBMLDocument());
				outID = biomodel.addComponent(null, comp, false, subBioModel.getCompartmentPorts(), -1, -1, 0, 0, md5);
				list.addItem(outID + " " + comp.replace(".xml", "") + " ()");
			}else{
				ComponentsPanel componentsPanel = new ComponentsPanel(selected, list, biomodel, subBioModel, ports, comp, oldPort, paramsOnly, this);
				while (!componentsPanel.openGui(selected));
			}

		}
	}
	
	public boolean isGridEditor() {
		return biomodel.getGrid().isEnabled();
	}
	
	public void addCompartment() {
		schematic.addCompartment(-1,-1);
	}
	
	public void addSpecies() {
		schematic.addSpecies(-1,-1);
	}
	
	public void addReaction() {
		schematic.addReaction(-1,-1);
	}
	
	public void addComponent() {
		schematic.addComponent(-1,-1);
	}
	
	public void addPromoter() {
		schematic.addPromoter(-1,-1);
	}
	
	public void addVariable() {
		schematic.addVariable(-1,-1);
	}
	
	public void addBoolean() {
		schematic.addBoolean(-1,-1);
	}
	
	public void addPlace() {
		schematic.addPlace(-1,-1);
	}
	
	public void addTransition() {
		schematic.addTransition(-1,-1);
	}
	
	public void addRule() {
		schematic.addRule(-1,-1);
	}
	
	public void addConstraint() {
		schematic.addConstraint(-1,-1);
	}
	
	public void addEvent() {
		schematic.addEvent(-1,-1);
	}
	
	public void addSelfInfluence() {
		schematic.addSelfInfluence(-1,-1);
	}
	
	public void moveLeft() {
		schematic.moveCells(-5,0);
	}
	
	public void moveRight() {
		schematic.moveCells(5,0);
	}
	
	public void moveUp() {
		schematic.moveCells(0,-5);
	}
	
	public void moveDown() {
		schematic.moveCells(0,5);
	}
	
	public void cut() {
		schematic.cut();
	}
	
	public void select() {
		schematic.select();
	}
	
	public void undo() {
		biomodel.undo();
		schematic.refresh();
		this.refresh();
		this.setDirty(true);		
	}
	
	public void redo() {
		biomodel.redo();
		schematic.refresh();
		this.refresh();
		this.setDirty(true);		
	}
	
	public void setElementsPanel(ElementsPanel elementsPanel) {
		this.elementsPanel = elementsPanel;
	}
	
	public Gui getGui() {
		return biosim;
	}
	
	public Schematic getSchematic() {
		return schematic;
	}

	public AnalysisView getReb2Sac() {
		return analysisView;
	}
	
	public boolean isTextBased() {
		return textBased;
	}

	public MySpecies getSpeciesPanel() {
		
		return speciesPanel;
	}
	
	public void setTextBased(boolean textBased) {
		this.textBased = textBased;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (tab.getSelectedIndex()==0) {
			schematic.getGraph().buildGraph();
		}
	}
}
