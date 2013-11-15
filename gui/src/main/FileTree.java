package main;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import sbol.SBOLUtility;

import biomodel.parser.BioModel;
import biomodel.parser.GCM2SBML;
import biomodel.util.GlobalConstants;


/**
 * Display a file system in a JTree view
 */
public class FileTree extends JPanel implements MouseListener {

	private static final long serialVersionUID = -6799125543270861304L;

	private String fileLocation; // location of selected file

	private DefaultMutableTreeNode root; // root node

	private File dir; // root directory

	public JTree tree; // JTree

	private String separator;

	private boolean lema, atacs, async;

	public static ImageIcon ICON_VHDL;

	public static ImageIcon ICON_S;

	public static ImageIcon ICON_INST;

	public static ImageIcon ICON_LHPN;

	public static ImageIcon ICON_CSP;

	public static ImageIcon ICON_HSE;

	public static ImageIcon ICON_UNC;

	public static ImageIcon ICON_RSG;

	public static ImageIcon ICON_MODEL;

	public static ImageIcon ICON_DOT;

	public static ImageIcon ICON_SBOL;

	public static ImageIcon ICON_SBML;

	public static ImageIcon ICON_SIMULATION;

	public static ImageIcon ICON_SYNTHESIS;

	public static ImageIcon ICON_VERIFY;

	public static ImageIcon ICON_PROJECT;

	public static ImageIcon ICON_GRAPH;

	public static ImageIcon ICON_PROBGRAPH;

	public static ImageIcon ICON_LEARN;

	public static ImageIcon ICON_MINUS;

	public static ImageIcon ICON_PLUS;

	/**
	 * Construct a FileTree
	 */
	public FileTree(final File dir, Gui biomodelsim, boolean lema, boolean atacs) {
		this.lema = lema;
		this.atacs = atacs;
		async = lema || atacs;
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		ICON_VHDL = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconVHDL.png");

		ICON_S = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconS.png");

		ICON_INST = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconInst.png");

		ICON_LHPN = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "icon_pnlogo.gif");

		ICON_CSP = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconCSP.png");

		ICON_HSE = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconHSE.png");

		ICON_UNC = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconUNC.png");

		ICON_RSG = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "iconRSG.png");

		ICON_MODEL = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "model.png");

		ICON_DOT = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "dot.jpg");

		ICON_SBOL = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "sbol.jpg");

		ICON_SBML = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "sbml.jpg");

		ICON_SIMULATION = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "simulation.jpg");

		ICON_SYNTHESIS = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "synth.png");

		ICON_VERIFY = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "check.png");

		ICON_PROJECT = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "project.jpg");

		ICON_GRAPH = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "graph.jpg");

		ICON_PROBGRAPH = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "probability.jpg");

		ICON_LEARN = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "learn.jpg");

		ICON_MINUS = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "treeMinus.gif");

		ICON_PLUS = new ImageIcon(biomodelsim.ENVVAR + separator + "gui" + separator + "icons" + separator + "treePlus.gif");

		setLayout(new BorderLayout());

		this.dir = dir;
		// Make a tree list with all the nodes, and make it a JTree
		if (dir != null) {
			if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				tree = new JTree(addNodes(null, dir, true, false),false);
			} else {
				tree = new JTree(addNodes(null, dir, true, false),false);
				//tree.expandPath(tree.getLeadSelectionPath());
			}
			TreeCellRenderer renderer = new IconCellRenderer();
			tree.setCellRenderer(renderer);
			// Add a listener
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
					fileLocation = "";
					while (node != null) {
						if (node.getParent() != null) {
							fileLocation = separator + node + fileLocation;
							String parentNode = node.getParent().toString();   //DK
							if (parentNode.endsWith(".xml") || parentNode.endsWith(".sbml") || parentNode.endsWith(".gcm")
									|| parentNode.endsWith(".vhd") ||  parentNode.endsWith(".prop") || parentNode.endsWith(".s") || parentNode.endsWith(".inst")
									|| parentNode.endsWith(".g") || parentNode.endsWith(".lpn") || parentNode.endsWith(".csp")
									|| parentNode.endsWith(".hse") || parentNode.endsWith(".unc") || parentNode.endsWith(".csp")
									|| parentNode.endsWith(".rsg") || parentNode.endsWith(".vams") || parentNode.endsWith(".sv")
									|| parentNode.endsWith(".grf") || parentNode.endsWith(".prb")) {
								node = (DefaultMutableTreeNode) node.getParent();
							}
						}
						node = (DefaultMutableTreeNode) node.getParent();
					}
					fileLocation = dir.getAbsolutePath() + fileLocation;
				}
			});
			tree.addMouseListener(this);
			tree.addMouseListener(biomodelsim);
		}
		else {
			tree = new JTree(new DefaultMutableTreeNode());
		}


		// Lastly, put the JTree into a JScrollPane.
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add(BorderLayout.CENTER, scrollpane);
	}

	public void setExpandibleIcons(boolean defaults) {
		if (defaults) {
			UIManager.put("Tree.expandedIcon", Gui.ICON_EXPAND);
			UIManager.put("Tree.collapsedIcon", Gui.ICON_COLLAPSE);
		}
		else {
			UIManager.put("Tree.expandedIcon", ICON_MINUS);
			UIManager.put("Tree.collapsedIcon", ICON_PLUS);
		}
		Runnable updateTree = new Runnable() {
			public void run() {
				tree.updateUI();
			}
		};
		SwingUtilities.invokeLater(updateTree);
	}

	/**
	 * Add nodes from under "dir" into curTop. Highly recursive.
	 */
	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir, boolean sim, boolean synth) {
		String curPath = dir.getPath();
		DefaultMutableTreeNode curDir;
		if (curTop == null) {
			curDir = new DefaultMutableTreeNode(new IconData(ICON_PROJECT, null, dir.getName()));
		}
		else {
			if (sim && synth) { // Verification node
				curDir = new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, dir.getName()));
			}
			else if (sim) {
				curDir = new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, dir.getName()));
			}
			else if (synth) {
				curDir = new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, dir.getName()));
			}
			else {
				curDir = new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, dir.getName()));
			}
		}
		if (curTop != null) { // should only be null at root
			if (curTop.getParent() == null) {
				curTop.add(curDir);
			}
		}
		else {
			root = curDir;
		}
		ArrayList<String> ol = new ArrayList<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i].charAt(0) != '.') {
				ol.add(tmp[i]);
			}
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<String> dirs = new ArrayList<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.get(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + separator + thisObject;
			if ((f = new File(newPath)).isDirectory() && !f.getName().equals("CVS")) {
				dirs.add(thisObject);
			}
			else if (!f.getName().equals("CVS"))
				/*
				if (!async && thisObject.toString().length() > 4
						&& thisObject.toString().substring(thisObject.toString().length() - 5).equals(".sbml") || !async
						&& thisObject.toString().length() > 3 && thisObject.toString().substring(thisObject.toString().length() - 4).equals(".xml")) {
					files.add(thisObject);
					if (Gui.createGCMFromSBML(curPath, curPath + separator + thisObject.toString(), thisObject.toString(), thisObject.toString()
							.replace(".xml", ".gcm").replace(".sbml", ".gcm"), false))
						files.add(thisObject.replace(".xml", ".gcm").replace(".sbml", ".gcm"));
				}
				else {
					files.add(thisObject);
				}
				*/
				if (!async && thisObject.toString().length() > 3 && 
						thisObject.toString().substring(thisObject.toString().length() - 4).equals(".gcm")) {
					String sbmlFile = thisObject.replace(".gcm",".xml");
					BioModel gcm = new BioModel(curPath);
					gcm.load(curPath + separator + sbmlFile);
					GCM2SBML gcm2sbml = new GCM2SBML(gcm);
					gcm2sbml.load(curPath + separator + thisObject.toString());
					gcm2sbml.convertGCM2SBML(curPath + separator + thisObject.toString());
					gcm.save(curPath + separator + sbmlFile);
					files.add(sbmlFile);
				}
				else {
					files.add(thisObject);
				}
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			DefaultMutableTreeNode file = null;
			if (curDir.getParent() == null) {
				if (!atacs && files.get(fnum).toString().length() > 4 && 
						files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbml") || 
					!atacs && files.get(fnum).toString().length() > 3 && 
						files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".xml")) { 
					file = new DefaultMutableTreeNode(new IconData(ICON_SBML, null, files.get(fnum))); 
				} else if (!async && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbol")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_SBOL, null, files.get(fnum)));
				}
				/*
				else if (!async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".gcm")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_DOT, null, files.get(fnum)));
				}
				*/
				else if (async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".vhd")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_VHDL, null, files.get(fnum)));
				}
				else if (async && files.get(fnum).toString().length() > 4  //DK
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".prop")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 1
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".s")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_S, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".inst")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_INST, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 1
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".g")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".lpn")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals("csp")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_CSP, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".hse")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_HSE, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".unc")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_UNC, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".rsg")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_RSG, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".vams")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 2
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 3).equals(".sv")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".grf")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_GRAPH, null, files.get(fnum)));
				}
				else if (files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".prb")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_PROBGRAPH, null, files.get(fnum)));
				}
			}
			else if (!(curDir.getParent().toString().equals(root.toString()))) {
				if (!atacs && files.get(fnum).toString().length() > 4 &&
						files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbml") || 
					!atacs && files.get(fnum).toString().length() > 3 &&
					 	files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".xml")) { 
					file = new DefaultMutableTreeNode(new IconData(ICON_SBML, null, files.get(fnum))); 
				} else if (!async && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbol")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_SBOL, null, files.get(fnum)));
				}
				/*
				else if (!async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".gcm")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_DOT, null, files.get(fnum)));
				}
				*/
				else if (async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".vhd")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_VHDL, null, files.get(fnum)));
				}  
				else if (async && files.get(fnum).toString().length() > 4  //DK
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".prop")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 1
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".s")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_S, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".inst")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_INST, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 1
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".g")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (async && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".lpn")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".csp")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_CSP, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".hse")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_HSE, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".unc")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_UNC, null, files.get(fnum)));
				}
				else if (atacs && files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".rsg")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_RSG, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 4
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".vams")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (lema && files.get(fnum).toString().length() > 2
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 3).equals(".sv")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
				}
				else if (files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".grf")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_GRAPH, null, files.get(fnum)));
				}
				else if (files.get(fnum).toString().length() > 3
						&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".prb")) {
					file = new DefaultMutableTreeNode(new IconData(ICON_PROBGRAPH, null, files.get(fnum)));
				}
			}
			if (file != null) {
				for (String d : dirs) {
					String newPath;
					if (curPath.equals("."))
						newPath = d;
					else
						newPath = curPath + separator + d;
					f = new File(newPath);
					if (new File(newPath + separator + d + ".sim").exists()) {
						try {
							Scanner scan = new Scanner(new File(newPath + separator + d + ".sim"));
							String refFile = scan.nextLine();
							if (refFile.equals(files.get(fnum)) || refFile.replace(".gcm", ".xml").equals(files.get(fnum))) {
								file.add(new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, d)));
							}
							scan.close();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					} else if (new File(newPath + separator + d + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).exists()) {
						try {
							Properties synthProps = SBOLUtility.loadSBOLSynthesisProperties(newPath, separator, Gui.frame);
							if (synthProps != null)
								if (synthProps.containsKey(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY)) {
									String refFile = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY);
									if (refFile.equals(files.get(fnum)) || refFile.replace(".gcm", ".xml").equals(files.get(fnum))) {
										file.add(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, d)));
									}
								} else
									JOptionPane.showMessageDialog(Gui.frame, "Synthesis specification property is missing.", "Missing Property", JOptionPane.ERROR_MESSAGE);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					} else if (new File(newPath + separator + d + ".lrn").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".lrn"));
							load.load(in);
							in.close();
							if (load.containsKey("genenet.file")) {
								String[] getProp = load.getProperty("genenet.file").split(separator);
								if (files.get(fnum).equals(getProp[getProp.length - 1])
										|| files.get(fnum).equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
									file.add(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)));
								}
							}
							else if (load.containsKey("learn.file")) {
								String[] getProp = load.getProperty("learn.file").split(separator);
								if (files.get(fnum).equals(getProp[getProp.length - 1])
										|| files.get(fnum).equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
									file.add(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)));
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (new File(newPath + separator + d + ".syn").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".syn"));
							load.load(in);
							in.close();
							if (load.containsKey("synthesis.file")) {
								String[] getProp = load.getProperty("synthesis.file").split(separator);
								if (files.get(fnum).equals(getProp[getProp.length - 1])) {
									file.add(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, d)));
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (new File(newPath + separator + d + ".ver").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".ver"));
							load.load(in);
							in.close();
							if (load.containsKey("verification.file")) {
								String[] getProp = load.getProperty("verification.file").split(separator);
								if (files.get(fnum).equals(getProp[getProp.length - 1])) {
									file.add(new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, d)));
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				curDir.add(file);
			}
		}
		return curDir;
	}

	public String getFile() {
		return fileLocation;
	}

	public void addToTree(String item, String dir) {
		deleteFromTree(item);
		String path = dir + separator + item;
		File file = new File(dir + separator + item);
		if (file.isDirectory()) {
			if (new File(path + separator + item + ".sim").exists()) {
				try {
					Scanner scan = new Scanner(new File(path + separator + item + ".sim"));
					String refFile = scan.nextLine();
					scan.close();
					for (int i = 0; i < root.getChildCount(); i++) {
						if (root.getChildAt(i).toString().equals(refFile) || root.getChildAt(i).toString().equals(refFile.replace(".gcm", ".xml"))) {
							int insert = 0;
							for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
								if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
									insert++;
								}
							}
							((DefaultMutableTreeNode) root.getChildAt(i)).insert(
									new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, item)), insert);
							Runnable updateTree = new Runnable() {
								public void run() {
									tree.updateUI();
								}
							};
							SwingUtilities.invokeLater(updateTree);
							return;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else if (new File(path + separator + item + GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION).exists()) {
				try {
					Properties synthProps = SBOLUtility.loadSBOLSynthesisProperties(path, separator, Gui.frame);
					if (synthProps != null) {
						if (synthProps.containsKey(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY)) {
							String refFile = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY);
							for (int i = 0; i < root.getChildCount(); i++) {
								if (root.getChildAt(i).toString().equals(refFile) || root.getChildAt(i).toString().equals(refFile.replace(".gcm", ".xml"))) {
									int insert = 0;
									for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
										if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
											insert++;
										}
									}
									((DefaultMutableTreeNode) root.getChildAt(i)).insert(
											new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, item)), insert);
									Runnable updateTree = new Runnable() {
										public void run() {
											tree.updateUI();
										}
									};
									SwingUtilities.invokeLater(updateTree);
									return;
								}
							}
						} else
							JOptionPane.showMessageDialog(Gui.frame, "Synthesis specification property is missing.", "Missing Property", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else if (new File(path + separator + item + ".lrn").exists()) {
				try {
					Properties load = new Properties();
					FileInputStream in = new FileInputStream(new File(path + separator + item + ".lrn"));
					load.load(in);
					in.close();
					if (load.containsKey("genenet.file")) {
						String[] getProp = load.getProperty("genenet.file").split(separator);
						for (int i = 0; i < root.getChildCount(); i++) {
							if (root.getChildAt(i).toString().equals(getProp[getProp.length - 1])
									|| root.getChildAt(i).toString().equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
								int insert = 0;
								for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
									if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
										insert++;
									}
								}
								((DefaultMutableTreeNode) root.getChildAt(i)).insert(
										new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, item)), insert);
								Runnable updateTree = new Runnable() {
									public void run() {
										tree.updateUI();
									}
								};
								SwingUtilities.invokeLater(updateTree);
								return;
							}
						}
					}
					else if (load.containsKey("learn.file")) {
						String[] getProp = load.getProperty("learn.file").split(separator);
						for (int i = 0; i < root.getChildCount(); i++) {
							if (root.getChildAt(i).toString().equals(getProp[getProp.length - 1])
									|| root.getChildAt(i).toString().equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
								int insert = 0;
								for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
									if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
										insert++;
									}
								}
								((DefaultMutableTreeNode) root.getChildAt(i)).insert(
										new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, item)), insert);
								Runnable updateTree = new Runnable() {
									public void run() {
										tree.updateUI();
									}
								};
								SwingUtilities.invokeLater(updateTree);
								return;
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (new File(path + separator + item + ".syn").exists()) {
				try {
					Properties load = new Properties();
					FileInputStream in = new FileInputStream(new File(path + separator + item + ".syn"));
					load.load(in);
					in.close();
					if (load.containsKey("synthesis.file")) {
						String[] getProp = load.getProperty("synthesis.file").split(separator);
						for (int i = 0; i < root.getChildCount(); i++) {
							if (root.getChildAt(i).toString().equals(getProp[getProp.length - 1])) {
								int insert = 0;
								for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
									if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
										insert++;
									}
								}
								((DefaultMutableTreeNode) root.getChildAt(i)).insert(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null,
										item)), insert);
								Runnable updateTree = new Runnable() {
									public void run() {
										tree.updateUI();
									}
								};
								SwingUtilities.invokeLater(updateTree);
								return;
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (new File(path + separator + item + ".ver").exists()) {
				try {
					Properties load = new Properties();
					FileInputStream in = new FileInputStream(new File(path + separator + item + ".ver"));
					load.load(in);
					in.close();
					if (load.containsKey("verification.file")) {
						String[] getProp = load.getProperty("verification.file").split(separator);
						for (int i = 0; i < root.getChildCount(); i++) {
							if (root.getChildAt(i).toString().equals(getProp[getProp.length - 1])) {
								int insert = 0;
								for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
									if (root.getChildAt(i).getChildAt(j).toString().compareToIgnoreCase(item) < 0) {
										insert++;
									}
								}
								((DefaultMutableTreeNode) root.getChildAt(i)).insert(
										new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, item)), insert);
								Runnable updateTree = new Runnable() {
									public void run() {
										tree.updateUI();
									}
								};
								SwingUtilities.invokeLater(updateTree);
								return;
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			DefaultMutableTreeNode node = null;
			int insert = 0;
			for (int i = 0; i < root.getChildCount(); i++) {
				if (root.getChildAt(i).toString().compareToIgnoreCase(item) < 0) {
					insert++;
				}
			}
			if (!atacs && item.endsWith(".sbml") || !atacs && item.endsWith(".xml")) { 
				node = new DefaultMutableTreeNode(new IconData(ICON_SBML, null, item)); 
			} else if (!async && item.endsWith(".sbol")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_SBOL, null, item));
			}
			/*
			else if (!async && item.endsWith(".gcm")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_DOT, null, item));
			}
			*/
			else if (async && item.endsWith(".vhd")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_VHDL, null, item));
			} 
			
			else if (async && item.endsWith(".prop")) {  //DK
				node = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, item));
			}
			else if (lema && item.endsWith(".s")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_S, null, item));
			}
			else if (lema && item.endsWith(".inst")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_INST, null, item));
			}
			else if (atacs && item.endsWith(".g")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, item));
			}
			else if (async && item.endsWith(".lpn")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, item));
			}
			else if (atacs && item.endsWith(".csp")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_CSP, null, item));
			}
			else if (atacs && item.endsWith(".hse")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_HSE, null, item));
			}
			else if (atacs && item.endsWith(".unc")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_UNC, null, item));
			}
			else if (atacs && item.endsWith(".rsg")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_RSG, null, item));
			}
			else if (lema && item.endsWith(".vams")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, item));
			}
			else if (lema && item.endsWith(".sv")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, item));
			}
			else if (item.endsWith(".grf")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_GRAPH, null, item));
			}
			else if (item.endsWith(".prb")) {
				node = new DefaultMutableTreeNode(new IconData(ICON_PROBGRAPH, null, item));
			}
			if (node != null) {
				String[] files = new File(dir).list();
				sort(files);
				for (String f : files) {
					path = dir + separator + f;
					if (new File(path).isDirectory()) {
						if (new File(path + separator + f + ".sim").exists()) {
							try {
								Scanner scan = new Scanner(new File(path + separator + f + ".sim"));
								String refFile = scan.nextLine();
								scan.close();
								if (item.equals(refFile) || item.equals(refFile.replace(".gcm", ".xml"))) {
									node.add(new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, f)));
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (new File(path + separator + f + ".lrn").exists()) {
							try {
								Properties load = new Properties();
								FileInputStream in = new FileInputStream(new File(path + separator + f + ".lrn"));
								load.load(in);
								in.close();
								if (load.containsKey("genenet.file")) {
									String[] getProp = load.getProperty("genenet.file").split(separator);
									if (item.equals(getProp[getProp.length - 1]) || item.equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
										node.add(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, f)));
									}
								}
								else if (load.containsKey("learn.file")) {
									String[] getProp = load.getProperty("learn.file").split(separator);
									if (item.equals(getProp[getProp.length - 1]) || item.equals(getProp[getProp.length - 1].replace(".gcm", ".xml"))) {
										node.add(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, f)));
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (new File(path + separator + f + ".syn").exists()) {
							try {
								Properties load = new Properties();
								FileInputStream in = new FileInputStream(new File(path + separator + f + ".syn"));
								load.load(in);
								in.close();
								if (load.containsKey("synthesis.file")) {
									String[] getProp = load.getProperty("synthesis.file").split(separator);
									if (item.equals(getProp[getProp.length - 1])) {
										node.add(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, f)));
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (new File(path + separator + f + ".ver").exists()) {
							try {
								Properties load = new Properties();
								FileInputStream in = new FileInputStream(new File(path + separator + f + ".ver"));
								load.load(in);
								in.close();
								if (load.containsKey("verification.file")) {
									String[] getProp = load.getProperty("verification.file").split(separator);
									if (item.equals(getProp[getProp.length - 1])) {
										node.add(new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, f)));
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				root.insert(node, insert);
				tree.expandPath(new TreePath(root.getPath()));
				Runnable updateTree = new Runnable() {
					public void run() {
						tree.updateUI();
					}
				};
				SwingUtilities.invokeLater(updateTree);
				return;
			}
		}
	}
	
	public void expandPath(DefaultMutableTreeNode node) {
		tree.expandPath(new TreePath(node.getPath()));
	}

	private void sort(Object[] sort) {
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

	public void deleteFromTree(String item) {
		for (int i = 0; i < root.getChildCount(); i++) {
			if (root.getChildAt(i).toString().equals(item)) {
				root.remove(i);
				Runnable updateTree = new Runnable() {
					public void run() {
						tree.updateUI();
					}
				};
				SwingUtilities.invokeLater(updateTree);
				return;
			}
			for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
				if (root.getChildAt(i).getChildAt(j).toString().equals(item)) {
					((DefaultMutableTreeNode) root.getChildAt(i)).remove(j);
					Runnable updateTree = new Runnable() {
						public void run() {
							tree.updateUI();
						}
					};
					SwingUtilities.invokeLater(updateTree);
					return;
				}
			}
		}
	}

	public void fixTree() {
		fixTree(null, root, dir, false);
		Runnable updateTree = new Runnable() {
			public void run() {
				tree.updateUI();
			}
		};
		SwingUtilities.invokeLater(updateTree);
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	private void fixTree(DefaultMutableTreeNode parent, DefaultMutableTreeNode current, File dir, boolean add) {
		String curPath = dir.getPath();
		if (add) {
			if (parent == null || parent.getParent() == null) {
				int insert = 0;
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (parent.getChildAt(i).toString().compareToIgnoreCase(current.toString()) > 0) {
						break;
					}  //DK
					if (parent.getChildAt(i).toString().contains(".sbml") || parent.getChildAt(i).toString().contains(".xml")
							|| parent.getChildAt(i).toString().contains(".gcm") || parent.getChildAt(i).toString().contains(".vhd")
							|| parent.getChildAt(i).toString().contains(".s") || parent.getChildAt(i).toString().contains(".inst")
							|| parent.getChildAt(i).toString().contains(".g") || parent.getChildAt(i).toString().contains(".lpn")
							|| parent.getChildAt(i).toString().contains(".csp") || parent.getChildAt(i).toString().contains(".hse")
							|| parent.getChildAt(i).toString().contains(".unc") || parent.getChildAt(i).toString().contains(".rsg")
							|| parent.getChildAt(i).toString().contains(".grf") || parent.getChildAt(i).toString().contains(".vams")
							|| parent.getChildAt(i).toString().contains(".sv") || parent.getChildAt(i).toString().contains(".prop")) { // SB
						break;
					}
					insert++;
				}
				parent.insert(current, insert);
			}
		}
		ArrayList<String> ol = new ArrayList<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i].charAt(0) != '.') {
				ol.add(tmp[i]);
			}
		}
		ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
		for (int i = 0; i < current.getChildCount(); i++) {
			nodes.add((DefaultMutableTreeNode) current.getChildAt(i));
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<String> dirs = new ArrayList<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < current.getChildCount(); i++) {
			boolean remove = true;
			for (int j = 0; j < ol.size(); j++) {
				String cur = "" + current.getChildAt(i);
				if (cur.equals(ol.get(j))) {
					remove = false;
				}
			}
			if (remove) {
				current.remove(i);
				i--;
			}
		}
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.get(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + separator + thisObject;
			if ((f = new File(newPath)).isDirectory() && !f.getName().equals("CVS")) {
				dirs.add(thisObject);
			}
			else if (!f.getName().equals("CVS"))
				files.add(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			String get = "";
			boolean doAdd = true;
			for (int j = 0; j < nodes.size(); j++) {
				get = "" + nodes.get(j);
				if (get.equals(files.get(fnum))) {
					doAdd = false;
				}
			}
			if (doAdd) {
				DefaultMutableTreeNode file = null;
				int insert = -1;
				if (parent == null) {  //DK
					insert = 0;
					for (int i = 0; i < current.getChildCount(); i++) {
						if (!current.getChildAt(i).toString().contains(".sbml") && !current.getChildAt(i).toString().contains(".xml")
								&& !current.getChildAt(i).toString().contains(".gcm") && !current.getChildAt(i).toString().contains(".vhd") && !current.getChildAt(i).toString().contains(".prop")
								&& !current.getChildAt(i).toString().contains(".s") && !current.getChildAt(i).toString().contains(".inst")
								&& !current.getChildAt(i).toString().contains(".g") && !current.getChildAt(i).toString().contains(".lpn")
								&& !current.getChildAt(i).toString().contains(".csp") && !current.getChildAt(i).toString().contains(".hse")
								&& !current.getChildAt(i).toString().contains(".unc") && !current.getChildAt(i).toString().contains(".rsg")
								&& !current.getChildAt(i).toString().contains(".grf") && !current.getChildAt(i).toString().contains(".prb")
								&& !current.getChildAt(i).toString().contains(".vams") && !current.getChildAt(i).toString().contains(".sv")) {
						}
						else if (current.getChildAt(i).toString().compareToIgnoreCase(files.get(fnum).toString()) > 0) {
							break;
						}
						insert++;
					}
					if (!atacs && files.get(fnum).toString().length() > 4 &&
							files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbml") || 
						!atacs && files.get(fnum).toString().length() > 3 &&
							files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".xml")) { 
						file = new DefaultMutableTreeNode(new IconData(ICON_SBML, null, files.get(fnum))); 
					} else if (!async && files.get(fnum).toString().length() > 4
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbol")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_SBOL, null, files.get(fnum)));
					}
					/*
					else if (!async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".gcm")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_DOT, null, files.get(fnum)));
					}
					*/
					else if (async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".vhd")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_VHDL, null, files.get(fnum)));
					}
					else if (async && files.get(fnum).toString().length() > 4    //DK
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".prop")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 1
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".s")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_S, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 4
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".inst")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_INST, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 1
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".g")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".lpn")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".csp")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_CSP, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".hse")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_HSE, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".unc")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_UNC, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".rsg")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_RSG, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 4
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".vams")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 2
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 3).equals(".sv")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".grf")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_GRAPH, null, files.get(fnum)));
					}
					else if (files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".prb")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_PROBGRAPH, null, files.get(fnum)));
					}
				}
				else if (!(parent.toString().equals(root.toString()))) {
					insert = 0;
					for (int i = 0; i < current.getChildCount(); i++) { //DK
						if (!current.getChildAt(i).toString().contains(".sbml") && !current.getChildAt(i).toString().contains(".xml")
								&& !current.getChildAt(i).toString().contains(".gcm") && !current.getChildAt(i).toString().contains(".vhd") && !!current.getChildAt(i).toString().contains(".prop")
								&& !current.getChildAt(i).toString().contains(".s") && !current.getChildAt(i).toString().contains(".inst")
								&& !current.getChildAt(i).toString().contains(".g") && !current.getChildAt(i).toString().contains(".lpn")
								&& !current.getChildAt(i).toString().contains(".csp") && !current.getChildAt(i).toString().contains(".hse")
								&& !current.getChildAt(i).toString().contains(".unc") && !current.getChildAt(i).toString().contains(".rsg")
								&& !current.getChildAt(i).toString().contains(".grf") && !current.getChildAt(i).toString().contains(".prb")
								&& !current.getChildAt(i).toString().contains(".vams") && !current.getChildAt(i).toString().contains(".sv")) {
						}
						else if (current.getChildAt(i).toString().compareToIgnoreCase(files.get(fnum).toString()) > 0) {
							break;
						}
						insert++;
					}
					if (!atacs && files.get(fnum).toString().length() > 4 &&
							files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".sbml") || 
						!atacs && files.get(fnum).toString().length() > 3 &&
							files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".xml")) { 
						file = new DefaultMutableTreeNode(new IconData(ICON_SBML, null, files.get(fnum)));
					}
					/*
					else if (!async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".gcm")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_DOT, null, files.get(fnum)));
					}
					*/
					else if (async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".vhd")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_VHDL, null, files.get(fnum)));
					}
					else if (async && files.get(fnum).toString().length() > 4   //DK
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".prop")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 1
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".s")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_S, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 4
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".inst")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_INST, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 1
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 2).equals(".g")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (async && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".lpn")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".csp")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_CSP, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".hse")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_HSE, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".unc")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_UNC, null, files.get(fnum)));
					}
					else if (atacs && files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".rsg")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_RSG, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 4
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 5).equals(".vams")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (lema && files.get(fnum).toString().length() > 2
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 3).equals(".sv")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_LHPN, null, files.get(fnum)));
					}
					else if (files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".grf")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_GRAPH, null, files.get(fnum)));
					}
					else if (files.get(fnum).toString().length() > 3
							&& files.get(fnum).toString().substring(files.get(fnum).toString().length() - 4).equals(".prb")) {
						file = new DefaultMutableTreeNode(new IconData(ICON_PROBGRAPH, null, files.get(fnum)));
					}
				}
				if (file != null && insert != -1) {
					current.insert(file, insert);
					nodes.add(file);
				}
			}
			for (DefaultMutableTreeNode file : nodes) {  //DK
				String fileNode = file.toString();
				if (fileNode.endsWith(".xml") || fileNode.endsWith(".sbml") || fileNode.endsWith(".gcm") || fileNode.endsWith(".vhd") || fileNode.endsWith(".prop")
						|| fileNode.endsWith(".s") || fileNode.endsWith(".inst") || fileNode.endsWith(".g") || fileNode.endsWith(".lpn")
						|| fileNode.endsWith(".csp") || fileNode.endsWith(".hse") || fileNode.endsWith(".unc") || fileNode.endsWith(".csp")
						|| fileNode.endsWith(".rsg") || fileNode.endsWith(".vams") || fileNode.endsWith(".sv") || fileNode.endsWith(".grf")
						|| fileNode.endsWith(".prb")) {
					for (int j = 0; j < file.getChildCount(); j++) {
						if (!dirs.contains(file.getChildAt(j).toString())) {
							file.remove(j);
							j--;
						}
					}
				}
				for (String d : dirs) {
					String newPath;
					if (curPath.equals("."))
						newPath = d;
					else
						newPath = curPath + separator + d;
					f = new File(newPath);
					if (new File(newPath + separator + d + ".sim").exists()) {
						try {
							Scanner scan = new Scanner(new File(newPath + separator + d + ".sim"));
							String refFile = scan.nextLine();
							if (refFile.equals(file.toString())) {
								boolean doAddIt = true;
								int getChild = 0;
								int insert = 0;
								for (int j = 0; j < file.getChildCount(); j++) {
									if (file.getChildAt(j).toString().compareToIgnoreCase(d) < 0) {
										insert++;
									}
									if (f.getName().equals("" + file.getChildAt(j))) {
										doAddIt = false;
										getChild = j;
									}
								}
								if (doAddIt) {
									file.insert(new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, d)), insert);
								}
								else {
									file.remove(getChild);
									doAddIt = true;
									file.insert(new DefaultMutableTreeNode(new IconData(ICON_SIMULATION, null, d)), insert);
								}
							}
							scan.close();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (new File(newPath + separator + d + ".lrn").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".lrn"));
							load.load(in);
							in.close();
							if (load.containsKey("genenet.file")) {
								String[] getProp = load.getProperty("genenet.file").split(separator);
								if (file.toString().equals(getProp[getProp.length - 1])) {
									boolean doAddIt = true;
									int getChild = 0;
									int insert = 0;
									for (int j = 0; j < file.getChildCount(); j++) {
										if (file.getChildAt(j).toString().compareToIgnoreCase(d) < 0) {
											insert++;
										}
										if (f.getName().equals("" + file.getChildAt(j))) {
											doAddIt = false;
											getChild = j;
										}
									}
									if (doAddIt) {
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)), insert);
									}
									else {
										file.remove(getChild);
										doAddIt = true;
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)), insert);
									}
								}
							}
							else if (load.containsKey("learn.file")) {
								String[] getProp = load.getProperty("learn.file").split(separator);
								if (file.toString().equals(getProp[getProp.length - 1])) {
									boolean doAddIt = true;
									int getChild = 0;
									int insert = 0;
									for (int j = 0; j < file.getChildCount(); j++) {
										if (file.getChildAt(j).toString().compareToIgnoreCase(d) < 0) {
											insert++;
										}
										if (f.getName().equals("" + file.getChildAt(j))) {
											doAddIt = false;
											getChild = j;
										}
									}
									if (doAddIt) {
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)), insert);
									}
									else {
										file.remove(getChild);
										doAddIt = true;
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_LEARN, null, d)), insert);
									}
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (new File(newPath + separator + d + ".syn").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".syn"));
							load.load(in);
							in.close();
							if (load.containsKey("synthesis.file")) {
								String[] getProp = load.getProperty("synthesis.file").split(separator);
								if (file.toString().equals(getProp[getProp.length - 1])) {
									boolean doAddIt = true;
									int getChild = 0;
									int insert = 0;
									for (int j = 0; j < file.getChildCount(); j++) {
										if (file.getChildAt(j).toString().compareToIgnoreCase(d) < 0) {
											insert++;
										}
										if (f.getName().equals("" + file.getChildAt(j))) {
											doAddIt = false;
											getChild = j;
										}
									}
									if (doAddIt) {
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, d)), insert);
									}
									else {
										file.remove(getChild);
										doAddIt = true;
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_SYNTHESIS, null, d)), insert);
									}
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (new File(newPath + separator + d + ".ver").exists()) {
						try {
							Properties load = new Properties();
							FileInputStream in = new FileInputStream(new File(newPath + separator + d + ".ver"));
							load.load(in);
							in.close();
							if (load.containsKey("verification.file")) {
								String[] getProp = load.getProperty("verification.file").split(separator);
								if (file.toString().equals(getProp[getProp.length - 1])) {
									boolean doAddIt = true;
									int getChild = 0;
									int insert = 0;
									for (int j = 0; j < file.getChildCount(); j++) {
										if (file.getChildAt(j).toString().compareToIgnoreCase(d) < 0) {
											insert++;
										}
										if (f.getName().equals("" + file.getChildAt(j))) {
											doAddIt = false;
											getChild = j;
										}
									}
									if (doAddIt) {
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, d)), insert);
									}
									else {
										file.remove(getChild);
										doAddIt = true;
										file.insert(new DefaultMutableTreeNode(new IconData(ICON_VERIFY, null, d)), insert);
									}
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public Dimension getMinimumSize() {
		return new Dimension(200, 400);
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 400);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;

		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		if (selRow < 0)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);
	}

	public void mouseReleased(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;

		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		if (selRow < 0)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	class IconCellRenderer extends JLabel implements TreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4060683095197368584L;

		protected Color m_textSelectionColor;

		protected Color m_textNonSelectionColor;

		protected Color m_bkSelectionColor;

		protected Color m_bkNonSelectionColor;

		protected Color m_borderSelectionColor;

		protected boolean m_selected;

		public IconCellRenderer() {
			super();
			m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
			m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
			m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
			m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
			m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
			setOpaque(false);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)

		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			setText(obj.toString());

			if (obj instanceof Boolean)
				setText("Retrieving data...");

			if (obj instanceof IconData) {
				IconData idata = (IconData) obj;
				if (expanded)
					setIcon(idata.getExpandedIcon());
				else
					setIcon(idata.getIcon());
			}
			else
				setIcon(null);

			setFont(tree.getFont());
			setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
			setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
			m_selected = sel;
			return this;
		}

		public void paintComponent(Graphics g) {
			Color bColor = getBackground();
			Icon icon = getIcon();

			g.setColor(bColor);
			int offset = 0;
			if (icon != null && getText() != null)
				offset = (icon.getIconWidth() + getIconTextGap());
			g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

			if (m_selected) {
				g.setColor(m_borderSelectionColor);
				g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
			}
			super.paintComponent(g);
		}
	}

	class IconData {
		protected Icon m_icon;

		protected Icon m_expandedIcon;

		protected Object m_data;

		public IconData(Icon icon, Object data) {
			m_icon = icon;
			m_expandedIcon = null;
			m_data = data;
		}

		public IconData(Icon icon, Icon expandedIcon, Object data) {
			m_icon = icon;
			m_expandedIcon = expandedIcon;
			m_data = data;
		}

		public Icon getIcon() {
			return m_icon;
		}

		public Icon getExpandedIcon() {
			return m_expandedIcon != null ? m_expandedIcon : m_icon;
		}

		public Object getObject() {
			return m_data;
		}

		public String toString() {
			return m_data.toString();
		}
	}
}