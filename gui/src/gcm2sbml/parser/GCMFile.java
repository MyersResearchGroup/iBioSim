package gcm2sbml.parser;

import gcm2sbml.util.GlobalConstants;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.LHPNFile;

import biomodelsim.BioSim;
import biomodelsim.Log;
import buttons.Buttons;

/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMFile {

	public GCMFile() {
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		loadDefaultParameters();
	}

	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
	}

	public boolean getDimAbs() {
		return dimAbs;
	}

	public void setDimAbs(boolean dimAbs) {
		this.dimAbs = dimAbs;
	}

	public boolean getBioAbs() {
		return bioAbs;
	}

	public void setBioAbs(boolean bioAbs) {
		this.bioAbs = bioAbs;
	}

	public void createLogicalModel(final String filename, final Log log, final BioSim biosim,
			final String path, final String lpnName) {
		try {
			new File(filename + ".temp").createNewFile();
		}
		catch (IOException e2) {
		}
		final JFrame naryFrame = new JFrame("Thresholds");
		WindowListener w = new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				naryFrame.dispose();
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
		naryFrame.addWindowListener(w);

		JTabbedPane naryTabs = new JTabbedPane();
		ArrayList<JPanel> specProps = new ArrayList<JPanel>();
		final ArrayList<JTextField> texts = new ArrayList<JTextField>();
		final ArrayList<JList> consLevel = new ArrayList<JList>();
		final ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
		final ArrayList<String> specs = new ArrayList<String>();
		for (String spec : species.keySet()) {
			specs.add(spec);
			JPanel newPanel1 = new JPanel(new GridLayout(1, 2));
			JPanel newPanel2 = new JPanel(new GridLayout(1, 2));
			JPanel otherLabel = new JPanel();
			otherLabel.add(new JLabel(spec + " Amount:"));
			newPanel2.add(otherLabel);
			consLevel.add(new JList());
			conLevel.add(new Object[0]);
			consLevel.get(consLevel.size() - 1).setListData(new Object[0]);
			conLevel.set(conLevel.size() - 1, new Object[0]);
			JScrollPane scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(260, 100));
			scroll.setViewportView(consLevel.get(consLevel.size() - 1));
			JPanel area = new JPanel();
			area.add(scroll);
			newPanel2.add(area);
			JPanel addAndRemove = new JPanel();
			JTextField adding = new JTextField(15);
			texts.add(adding);
			JButton Add = new JButton("Add");
			JButton Remove = new JButton("Remove");
			Add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int number = Integer.parseInt(e.getActionCommand().substring(3,
							e.getActionCommand().length()));
					try {
						int get = Integer.parseInt(texts.get(number).getText().trim());
						if (get <= 0) {
							JOptionPane.showMessageDialog(naryFrame,
									"Amounts Must Be Positive Integers.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
						else {
							JList add = new JList();
							Object[] adding = { "" + get };
							add.setListData(adding);
							add.setSelectedIndex(0);
							Object[] sort = Buttons.add(conLevel.get(number),
									consLevel.get(number), add, false, null, null, null, null,
									null, null, naryFrame);
							int in;
							for (int out = 1; out < sort.length; out++) {
								int temp = Integer.parseInt((String) sort[out]);
								in = out;
								while (in > 0 && Integer.parseInt((String) sort[in - 1]) >= temp) {
									sort[in] = sort[in - 1];
									--in;
								}
								sort[in] = temp + "";
							}
							conLevel.set(number, sort);
						}
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(naryFrame,
								"Amounts Must Be Positive Integers.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			Add.setActionCommand("Add" + (consLevel.size() - 1));
			Remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int number = Integer.parseInt(e.getActionCommand().substring(6,
							e.getActionCommand().length()));
					conLevel.set(number, Buttons
							.remove(consLevel.get(number), conLevel.get(number)));
				}
			});
			Remove.setActionCommand("Remove" + (consLevel.size() - 1));
			addAndRemove.add(adding);
			addAndRemove.add(Add);
			addAndRemove.add(Remove);
			JPanel newnewPanel = new JPanel(new BorderLayout());
			newnewPanel.add(newPanel1, "North");
			newnewPanel.add(newPanel2, "Center");
			newnewPanel.add(addAndRemove, "South");
			specProps.add(newnewPanel);
			naryTabs.addTab(spec + " Properties", specProps.get(specProps.size() - 1));
		}

		JButton naryRun = new JButton("Create");
		JButton naryClose = new JButton("Cancel");
		naryRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				convertToLHPN(specs, conLevel).save(filename);
				log.addText("Saving GCM file as LHPN:\n" + path + File.separator + lpnName + "\n");
				biosim.refreshTree();
				naryFrame.dispose();
				new File(filename + ".temp").delete();
			}
		});
		naryClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				naryFrame.dispose();
				new File(filename + ".temp").delete();
			}
		});
		JPanel naryRunPanel = new JPanel();
		naryRunPanel.add(naryRun);
		naryRunPanel.add(naryClose);

		JPanel naryPanel = new JPanel(new BorderLayout());
		naryPanel.add(naryTabs, "Center");
		naryPanel.add(naryRunPanel, "South");

		naryFrame.setContentPane(naryPanel);
		naryFrame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = naryFrame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		naryFrame.setLocation(x, y);
		naryFrame.setResizable(false);
		naryFrame.setVisible(true);
	}

	private LHPNFile convertToLHPN(ArrayList<String> specs, ArrayList<Object[]> conLevel) {
		HashMap<String, ArrayList<String>> infl = new HashMap<String, ArrayList<String>>();
		for (String influence : influences.keySet()) {
			if (influences.get(influence).get(GlobalConstants.TYPE).equals(
					GlobalConstants.ACTIVATION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (influences.get(influence).contains(GlobalConstants.BIO)
						&& influences.get(influence).get(GlobalConstants.BIO).equals("yes")) {
					if (infl.containsKey(output)) {
						infl.get(output).add("bioAct:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("bioAct:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
				else {
					if (infl.containsKey(output)) {
						infl.get(output).add("act:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("act:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
			}
			else if (influences.get(influence).get(GlobalConstants.TYPE).equals(
					GlobalConstants.REPRESSION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (influences.get(influence).contains(GlobalConstants.BIO)
						&& influences.get(influence).get(GlobalConstants.BIO).equals("yes")) {
					if (infl.containsKey(output)) {
						infl.get(output).add("bioRep:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("bioRep:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
				else {
					if (infl.containsKey(output)) {
						infl.get(output).add("rep:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("rep:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
			}
		}
		LHPNFile LHPN = new LHPNFile();
		for (int i = 0; i < specs.size(); i++) {
			LHPN.addInteger(specs.get(i), "0");
		}
		for (int i = 0; i < specs.size(); i++) {
			int placeNum = 0;
			int transNum = 0;
			String previousPlaceName = specs.get(i) + placeNum;
			placeNum++;
			LHPN.addPlace(previousPlaceName, true);
			String number = "0";
			for (Object threshold : conLevel.get(i)) {
				LHPN.addPlace(specs.get(i) + placeNum, false);
				LHPN.addTransition(specs.get(i) + "_trans" + transNum);
				LHPN.addControlFlow(previousPlaceName, specs.get(i) + "_trans" + transNum);
				LHPN.addControlFlow(specs.get(i) + "_trans" + transNum, specs.get(i) + placeNum);
				LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i),
						(String) threshold);
				ArrayList<String> activators = new ArrayList<String>();
				ArrayList<String> repressors = new ArrayList<String>();
				ArrayList<String> bioActivators = new ArrayList<String>();
				ArrayList<String> bioRepressors = new ArrayList<String>();
				Double np = Double
						.parseDouble(parameters.get(GlobalConstants.STOICHIOMETRY_STRING));
				Double ng = Double.parseDouble(parameters
						.get(GlobalConstants.PROMOTER_COUNT_STRING));
				Double kb = Double.parseDouble(parameters.get(GlobalConstants.KBASAL_STRING));
				Double Kb = Double.parseDouble(parameters.get(GlobalConstants.KBIO_STRING));
				Double Ko = Double.parseDouble(parameters.get(GlobalConstants.RNAP_BINDING_STRING));
				Double ka = Double.parseDouble(parameters.get(GlobalConstants.ACTIVED_STRING));
				Double Ka = Double.parseDouble(parameters.get(GlobalConstants.KACT_STRING));
				Double ko = Double.parseDouble(parameters.get(GlobalConstants.OCR_STRING));
				Double Kr = Double.parseDouble(parameters.get(GlobalConstants.KREP_STRING));
				Double kd = Double.parseDouble(parameters.get(GlobalConstants.KDECAY_STRING));
				Double nc = Double
						.parseDouble(parameters.get(GlobalConstants.COOPERATIVITY_STRING));
				Double RNAP = Double.parseDouble(parameters.get(GlobalConstants.RNAP_STRING));
				if (infl.containsKey(specs.get(i))) {
					for (String in : infl.get(specs.get(i))) {
						String[] parse = in.split(":");
						String species = parse[1];
						String influence = parse[2];
						String promoter = influence.split(" ")[influence.split(" ").length - 1];
						if (parse[0].equals("act")) {
							activators.add(species);
						}
						else if (parse[0].equals("rep")) {
							repressors.add(species);
						}
						else if (parse[0].equals("bioAct")) {
							bioActivators.add(species);
						}
						else if (parse[0].equals("bioRep")) {
							bioRepressors.add(species);
						}
						Properties p = this.species.get(species);
						if (p.contains(GlobalConstants.KDECAY_STRING)) {
							kd = Double.parseDouble((String) p.get(GlobalConstants.KDECAY_STRING));
						}
						p = influences.get(influence);
						if (p.contains(GlobalConstants.COOPERATIVITY_STRING)) {
							nc = Double.parseDouble((String) p
									.get(GlobalConstants.COOPERATIVITY_STRING));
						}
						if (p.contains(GlobalConstants.KREP_STRING)) {
							Kr = Double.parseDouble((String) p.get(GlobalConstants.KREP_STRING));
						}
						if (p.contains(GlobalConstants.KACT_STRING)) {
							Ka = Double.parseDouble((String) p.get(GlobalConstants.KACT_STRING));
						}
						if (p.contains(GlobalConstants.KBIO_STRING)) {
							Kb = Double.parseDouble((String) p.get(GlobalConstants.KBIO_STRING));
						}
						if (promoters.containsKey(promoter)) {
							p = promoters.get(promoter);
							if (p.contains(GlobalConstants.PROMOTER_COUNT_STRING)) {
								ng = Double.parseDouble((String) p
										.get(GlobalConstants.PROMOTER_COUNT_STRING));
							}
							if (p.contains(GlobalConstants.RNAP_BINDING_STRING)) {
								Ko = Double.parseDouble((String) p
										.get(GlobalConstants.RNAP_BINDING_STRING));
							}
							if (p.contains(GlobalConstants.OCR_STRING)) {
								ko = Double.parseDouble((String) p.get(GlobalConstants.OCR_STRING));
							}
							if (p.contains(GlobalConstants.STOICHIOMETRY_STRING)) {
								np = Double.parseDouble((String) p
										.get(GlobalConstants.STOICHIOMETRY_STRING));
							}
							if (p.contains(GlobalConstants.KBASAL_STRING)) {
								kb = Double.parseDouble((String) p
										.get(GlobalConstants.KBASAL_STRING));
							}
							if (p.contains(GlobalConstants.ACTIVED_STRING)) {
								ka = Double.parseDouble((String) p
										.get(GlobalConstants.ACTIVED_STRING));
							}
						}
					}
				}
				String addBio = "" + Kb;
				for (String bioAct : bioActivators) {
					addBio += "*" + bioAct;
				}
				if (!addBio.equals("" + Kb)) {
					activators.add(addBio);
				}
				addBio = "" + Kb;
				for (String bioRep : bioRepressors) {
					addBio += "*" + bioRep;
				}
				if (!addBio.equals("" + Kb)) {
					repressors.add(addBio);
				}
				String rate = "";
				if (activators.size() != 0) {
					rate += "(" + np + "*" + ng + ")*((" + kb + "*" + Ko + "*" + RNAP + ")";
					for (String act : activators) {
						rate += "+(" + ka + "*" + Ka + "*" + RNAP + ")*(" + act + "^" + nc + ")";
					}
					rate += ")/((1+(" + Ko + "*" + RNAP + "))";
					for (String act : activators) {
						rate += "+(" + Ka + "*" + RNAP + ")*(" + act + "^" + nc + ")";
					}
					if (repressors.size() != 0) {
						for (String rep : repressors) {
							rate += "+" + Kr + "*(" + rep + "^" + nc + ")";
						}
					}
					rate += ")";
				}
				else {
					if (repressors.size() != 0) {
						rate += "(" + np + "*" + ko + "*" + ng + ")*((" + Ko + "*" + RNAP
								+ "))/((1+(" + Ko + "*" + RNAP + "))";
						for (String rep : repressors) {
							rate += "+" + Kr + "*(" + rep + "^" + nc + ")";
						}
						rate += ")";
					}
				}
				if (rate.equals("")) {
					rate = "0.0";
				}
				LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + rate + ")/" + "("
						+ threshold + "-" + number + ")");
				transNum++;
				LHPN.addTransition(specs.get(i) + "_trans" + transNum);
				LHPN.addControlFlow(specs.get(i) + placeNum, specs.get(i) + "_trans" + transNum);
				LHPN.addControlFlow(specs.get(i) + "_trans" + transNum, previousPlaceName);
				LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), number);
				LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + specs.get(i) + "*"
						+ kd + ")/" + "(" + threshold + "-" + number + ")");
				transNum++;
				previousPlaceName = specs.get(i) + placeNum;
				placeNum++;
				number = (String) threshold;
			}
		}
		return LHPN;
	}

	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer("digraph G {\n");
			for (String s : species.keySet()) {
				buffer.append(s + " [");
				Properties prop = species.get(s);
				for (Object propName : prop.keySet()) {
					if ((propName.toString().equals(GlobalConstants.NAME))
							|| (propName.toString().equals("label"))) {
						buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
								+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
					}
					else {
						buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
								+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
					}
				}
				if (!prop.containsKey("shape")) {
					buffer.append("shape=ellipse,");
				}
				if (!prop.containsKey("label")) {
					buffer.append("label=\"" + s + "\"");
				}
				else {
					buffer.deleteCharAt(buffer.lastIndexOf(","));
				}
				// buffer.deleteCharAt(buffer.length() - 1);
				buffer.append("]\n");
			}
			for (String s : components.keySet()) {
				buffer.append(s + " [");
				Properties prop = components.get(s);
				for (Object propName : prop.keySet()) {
					if (propName.toString().equals("gcm")) {
						buffer.append(checkCompabilitySave(propName.toString()) + "=\""
								+ prop.getProperty(propName.toString()).toString() + "\"");
					}
				}
				buffer.append("]\n");
			}
			for (String s : influences.keySet()) {
				buffer.append(getInput(s) + " -> "// + getArrow(s) + " "
						+ getOutput(s) + " [");
				Properties prop = influences.get(s);
				String promo = "default";
				if (prop.containsKey(GlobalConstants.PROMOTER)) {
					promo = prop.getProperty(GlobalConstants.PROMOTER);
				}
				prop.setProperty(GlobalConstants.NAME, "\"" + getInput(s) + " " + getArrow(s) + " "
						+ getOutput(s) + ", Promoter " + promo + "\"");
				for (Object propName : prop.keySet()) {
					if (propName.toString().equals("label")) {
						buffer.append("label=\"\"");
					}
					else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString() + ",");
					}
				}

				String type = "";
				if (!prop.containsKey("arrowhead")) {
					if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
						type = "vee";
					}
					else if (prop.getProperty(GlobalConstants.TYPE).equals(
							GlobalConstants.REPRESSION)) {
						type = "tee";
					}
					else {
						type = "dot";
					}
					buffer.append("arrowhead=" + type + "");
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
			}
			for (String s : components.keySet()) {
				Properties prop = components.get(s);
				for (Object propName : prop.keySet()) {
					if (!propName.toString().equals("gcm")
							&& !propName.toString().equals(GlobalConstants.ID)) {
						buffer.append(s + " -> " + prop.getProperty(propName.toString()).toString()
								+ " [port=" + propName.toString());
						buffer.append(", arrowhead=none]\n");
					}
				}
			}
			buffer.append("}\nGlobal {\n");
			for (String s : defaultParameters.keySet()) {
				if (globalParameters.containsKey(s)) {
					String value = globalParameters.get(s);
					buffer.append(s + "=" + value + "\n");
				}
			}
			buffer.append("}\nPromoters {\n");
			for (String s : promoters.keySet()) {
				buffer.append(s + " [");
				Properties prop = promoters.get(s);
				for (Object propName : prop.keySet()) {
					if (propName.toString().equals(GlobalConstants.NAME)) {
						buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
								+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
					}
					else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString() + ",");
					}
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
			}
			buffer.append("}\nConditions {\n");
			for (String s : conditions) {
				buffer.append(s + "\n");
			}
			/*
			 * buffer.append("}\nComponents {\n"); for (String s :
			 * components.keySet()) { buffer.append(s + " ["); Properties prop =
			 * components.get(s); for (Object propName : prop.keySet()) { if
			 * (propName.toString().equals(GlobalConstants.ID)) { } else {
			 * buffer.append(checkCompabilitySave(propName.toString()) + "=" +
			 * prop.getProperty(propName.toString()).toString() + ","); } } if
			 * (buffer.charAt(buffer.length() - 1) == ',') {
			 * buffer.deleteCharAt(buffer.length() - 1); } buffer.append("]\n");
			 * }
			 */
			buffer.append("}\n");
			buffer.append(GlobalConstants.SBMLFILE + "=\"" + sbmlFile + "\"\n");
			if (bioAbs) {
				buffer.append(GlobalConstants.BIOABS + "=true\n");
			}
			if (dimAbs) {
				buffer.append(GlobalConstants.DIMABS + "=true\n");
			}
			p.print(buffer);
			p.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		StringBuffer data = new StringBuffer();
		loadDefaultParameters();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
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
		try {
			parseStates(data);
			parseInfluences(data);
			parseGlobal(data);
			parsePromoters(data);
			parseSBMLFile(data);
			parseBioAbs(data);
			parseDimAbs(data);
			parseConditions(data);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse GCM");
			// JOptionPane.showMessageDialog(null,
			// "Unable to parse model, creating a blank model.", "Error",
			// JOptionPane.ERROR_MESSAGE);
			// species = new HashMap<String, Properties>();
			// influences = new HashMap<String, Properties>();
			// promoters = new HashMap<String, Properties>();
			// globalParameters = new HashMap<String, String>();
		}
	}

	public void changePromoterName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			String newInfluenceName = "";
			if (influences.get(s).containsKey(GlobalConstants.PROMOTER)
					&& influences.get(s).get(GlobalConstants.PROMOTER).equals(oldName)) {
				newInfluenceName = input + " " + arrow + " " + output + ", Promoter " + newName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
				influences.get(newInfluenceName).setProperty(GlobalConstants.PROMOTER, newName);
			}
		}

		promoters.put(newName, promoters.get(oldName));
		promoters.remove(oldName);
	}

	public void changeSpeciesName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			boolean replaceInput = input.equals(oldName);
			boolean replaceOutput = output.equals(oldName);
			String newInfluenceName = "";
			if (replaceInput || replaceOutput) {
				if (replaceInput) {
					newInfluenceName = newInfluenceName + newName;
				}
				else {
					newInfluenceName = newInfluenceName + input;
				}
				if (replaceOutput) {
					newInfluenceName = newInfluenceName + " " + arrow + " " + newName;
				}
				else {
					newInfluenceName = newInfluenceName + " " + arrow + " " + output;
				}
				String promoterName = "default";
				if (influences.get(s).containsKey(GlobalConstants.PROMOTER)) {
					promoterName = influences.get(s).get(GlobalConstants.PROMOTER).toString();
				}
				newInfluenceName = newInfluenceName + ", Promoter " + promoterName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
			}
		}
		species.put(newName, species.get(oldName));
		species.remove(oldName);
	}

	public void changeComponentName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			boolean replaceInput = input.equals(oldName);
			boolean replaceOutput = output.equals(oldName);
			String newInfluenceName = "";
			if (replaceInput || replaceOutput) {
				if (replaceInput) {
					newInfluenceName = newInfluenceName + newName;
				}
				else {
					newInfluenceName = newInfluenceName + input;
				}
				if (replaceOutput) {
					newInfluenceName = newInfluenceName + " " + arrow + " " + newName;
				}
				else {
					newInfluenceName = newInfluenceName + " " + arrow + " " + output;
				}
				String promoterName = "default";
				if (influences.get(s).containsKey(GlobalConstants.PROMOTER)) {
					promoterName = influences.get(s).get(GlobalConstants.PROMOTER).toString();
				}
				newInfluenceName = newInfluenceName + ", Promoter " + promoterName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
			}
		}
		components.put(newName, components.get(oldName));
		components.remove(oldName);
	}

	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}

	public void addPromoter(String name, Properties properties) {
		promoters.put(name.replace("\"", ""), properties);
	}

	public void addComponent(String name, Properties properties) {
		components.put(name, properties);
	}
	
	public boolean addCondition(String condition) {
		boolean retval = true;
		for (String cond : condition.split("&&")) {
			if (cond.split("->").length > 2) {
				return false;
			}
			for (String part : cond.split("->")) {
				ArrayList<String> specs = new ArrayList<String>();
				ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
				for (String spec : species.keySet()) {
					specs.add(spec);
					ArrayList<String> level = new ArrayList<String>();
					level.add("0");
					conLevel.add(level.toArray());
				}
				ExprTree expr = new ExprTree(convertToLHPN(specs, conLevel));
				expr.token = expr.intexpr_gettok(part);
				if (!part.equals("")) {
					retval = (expr.intexpr_L(part) && retval);
				}
				else {
					expr = null;
					retval = false;
				}
			}
		}
		if (retval) {
			conditions.add(condition);
		}
		return retval;
	}
	
	public void removeCondition(String condition) {
		conditions.remove(condition);
	}
	
	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void addInfluences(String name, Properties property) {
		influences.put(name, property);
		// Now check to see if a promoter exists in the property
		if (property.containsKey("promoter")) {
			promoters.put(property.getProperty("promoter").replaceAll("\"", ""), new Properties());
		}

	}

	public void removeSpecies(String name) {
		if (name != null && species.containsKey(name)) {
			species.remove(name);
		}
	}

	public HashMap<String, Properties> getSpecies() {
		return species;
	}

	public HashMap<String, Properties> getComponents() {
		return components;
	}

	public String getComponentPortMap(String s) {
		String portmap = "(";
		Properties c = components.get(s);
		ArrayList<String> ports = new ArrayList<String>();
		for (Object key : c.keySet()) {
			if (!key.equals("gcm")) {
				ports.add((String) key);
			}
		}
		int i, j;
		String index;
		for (i = 1; i < ports.size(); i++) {
			index = ports.get(i);
			j = i;
			while ((j > 0) && ports.get(j - 1).compareToIgnoreCase(index) > 0) {
				ports.set(j, ports.get(j - 1));
				j = j - 1;
			}
			ports.set(j, index);
		}
		if (ports.size() > 0) {
			portmap += ports.get(0) + "->" + c.getProperty(ports.get(0));
		}
		for (int k = 1; k < ports.size(); k++) {
			portmap += ", " + ports.get(k) + "->" + c.getProperty(ports.get(k));
		}
		portmap += ")";
		return portmap;
	}

	public HashMap<String, Properties> getInfluences() {
		return influences;
	}

	/**
	 * Checks to see if removing influence is okay
	 * 
	 * @param name
	 *            influence to remove
	 * @return true, it is always okay to remove influence
	 */
	public boolean removeInfluenceCheck(String name) {
		return true;
	}

	/**
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	public boolean removeSpeciesCheck(String name) {
		for (String s : influences.keySet()) {
			if (s.contains(name)) {
				return false;
			}
		}
		return true;
	}

	public boolean removeComponentCheck(String name) {
		for (String s : influences.keySet()) {
			if ((" " + s + " ").contains(" " + name + " ")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks to see if removing promoter is okay
	 * 
	 * @param name
	 *            promoter to remove
	 * @return true if promoter is in no influences
	 */
	public boolean removePromoterCheck(String name) {
		for (Properties p : influences.values()) {
			if (p.containsKey(GlobalConstants.PROMOTER)
					&& p.getProperty(GlobalConstants.PROMOTER).equals(name)) {
				return false;
			}
		}
		return true;
	}

	public void removePromoter(String name) {
		if (name != null && promoters.containsKey(name)) {
			promoters.remove(name);
		}
	}

	public void removeComponent(String name) {
		if (name != null && components.containsKey(name)) {
			components.remove(name);
		}
	}

	public void removeInfluence(String name) {
		if (name != null && influences.containsKey(name)) {
			influences.remove(name);
		}
	}

	public String getInput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(2);
	}

	public String getArrow(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(3) + matcher.group(4);
	}

	public String getOutput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(5);
	}

	public String getPromoter(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(6);
	}

	public String[] getSpeciesAsArray() {
		String[] s = new String[species.size()];
		s = species.keySet().toArray(s);
		Arrays.sort(s);
		return s;
	}

	public String[] getPromotersAsArray() {
		String[] s = new String[promoters.size()];
		s = promoters.keySet().toArray(s);
		Arrays.sort(s);
		return s;
	}

	public HashMap<String, Properties> getPromoters() {
		return promoters;
	}

	public HashMap<String, String> getGlobalParameters() {
		return globalParameters;
	}

	public HashMap<String, String> getDefaultParameters() {
		return defaultParameters;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(String parameter) {
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		}
		else {
			return defaultParameters.get(parameter);
		}
	}

	public void setParameter(String parameter, String value) {
		globalParameters.put(parameter, value);
		parameters.put(parameter, value);
	}

	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
	}

	private void parseStates(StringBuffer data) {
		Pattern network = Pattern.compile(NETWORK);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		matcher.find();
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				}
				else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			// for backwards compatibility
			if (properties.containsKey("const")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.CONSTANT);
			}
			else if (!properties.containsKey(GlobalConstants.TYPE)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NORMAL);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals("constant")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.CONSTANT);
			}

			// for backwards compatibility
			if (properties.containsKey("label")) {
				properties.put(GlobalConstants.ID, properties.getProperty("label")
						.replace("\"", ""));
			}
			if (properties.containsKey("gcm")) {
				if (properties.containsKey(GlobalConstants.TYPE)) {
					properties.remove(GlobalConstants.TYPE);
				}
				properties.put("gcm", properties.getProperty("gcm").replace("\"", ""));
				components.put(name, properties);
			}
			else {
				species.put(name, properties);
			}
		}
	}

	private void parseSBMLFile(StringBuffer data) {
		Pattern pattern = Pattern.compile(SBMLFILE);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			sbmlFile = matcher.group(1);
		}
	}

	private void parseDimAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(DIMABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				dimAbs = true;
			}
		}
	}

	private void parseBioAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(BIOABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				bioAbs = true;
			}
		}
	}

	private void parseGlobal(StringBuffer data) {
		Pattern pattern = Pattern.compile(GLOBAL);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1);
			matcher = propPattern.matcher(s);
			while (matcher.find()) {
				if (matcher.group(3) != null) {
					globalParameters.put(matcher.group(1), matcher.group(3));
					parameters.put(matcher.group(1), matcher.group(3));
				}
				else {
					globalParameters.put(matcher.group(1), matcher.group(4));
					parameters.put(matcher.group(1), matcher.group(4));
				}
			}
		}
	}
	
	private void parseConditions(StringBuffer data) {
		Pattern pattern = Pattern.compile(CONDITION);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1).trim();
			for (String cond : s.split("\n")) {
				addCondition(cond.trim());
			}
		}
	}

	private void parsePromoters(StringBuffer data) {
		Pattern network = Pattern.compile(PROMOTERS_LIST);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		if (!matcher.find()) {
			return;
		}
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				}
				else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			promoters.put(name, properties);
		}
	}

	/*
	 * private void parseComponents(StringBuffer data) { Pattern network =
	 * Pattern.compile(COMPONENTS_LIST); Matcher matcher =
	 * network.matcher(data.toString()); Pattern pattern =
	 * Pattern.compile(STATE); Pattern propPattern = Pattern.compile(PROPERTY);
	 * if (!matcher.find()) { return; } matcher =
	 * pattern.matcher(matcher.group(1)); while (matcher.find()) { String name =
	 * matcher.group(2); Matcher propMatcher =
	 * propPattern.matcher(matcher.group(3)); Properties properties = new
	 * Properties(); while (propMatcher.find()) { if
	 * (propMatcher.group(3)!=null) { properties.put(propMatcher.group(1),
	 * propMatcher.group(3)); } else { properties.put(propMatcher.group(1),
	 * propMatcher.group(4)); } } components.put(name, properties); } }
	 */

	private void parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			Matcher propMatcher = propPattern.matcher(matcher.group(6));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties
							.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(3));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(3))) {
						promoters.put(propMatcher.group(3).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(3)
								.replace("\"", "")); // for backwards
														// compatibility
					}
				}
				else {
					properties
							.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(4));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(4))) {
						promoters.put(propMatcher.group(4).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(4)
								.replace("\"", "")); // for backwards
														// compatibility
					}
				}
			}

			if (properties.containsKey("port")) {
				components.get(matcher.group(2)).put(properties.get("port"), matcher.group(5));
			}
			else {
				String name = "";
				if (properties.containsKey("arrowhead")) {
					if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.ACTIVATION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " +> " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " -> " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("tee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.REPRESSION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " +| " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " -| " + matcher.group(5);
						}
					}
					else {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NOINFLUENCE);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " x> " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " x> " + matcher.group(5);
						}
					}
				}
				if (properties.getProperty(GlobalConstants.PROMOTER) != null) {
					name = name + ", Promoter " + properties.getProperty(GlobalConstants.PROMOTER);
				}
				else {
					name = name + ", Promoter " + "default";
				}
				if (!properties.containsKey("label")) {
					String label = properties.getProperty(GlobalConstants.PROMOTER);
					if (label == null) {
						label = "";
					}
					if (properties.containsKey(GlobalConstants.BIO)
							&& properties.get(GlobalConstants.BIO).equals("yes")) {
						label = label + "+";
					}
					properties.put("label", "\"" + label + "\"");
				}
				properties.put(GlobalConstants.NAME, name);
				influences.put(name, properties);
			}
		}
	}

	public void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();
		defaultParameters = new HashMap<String, String>();
		defaultParameters.put(GlobalConstants.KDECAY_STRING, biosimrc.get(
				"biosim.gcm.KDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KASSOCIATION_STRING, biosimrc.get(
				"biosim.gcm.KASSOCIATION_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBIO_STRING, biosimrc
				.get("biosim.gcm.KBIO_VALUE", ""));
		defaultParameters.put(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get(
				"biosim.gcm.COOPERATIVITY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KREP_STRING, biosimrc
				.get("biosim.gcm.KREP_VALUE", ""));
		defaultParameters.put(GlobalConstants.KACT_STRING, biosimrc
				.get("biosim.gcm.KACT_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_BINDING_STRING, biosimrc.get(
				"biosim.gcm.RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_STRING, biosimrc
				.get("biosim.gcm.RNAP_VALUE", ""));
		defaultParameters.put(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBASAL_STRING, biosimrc.get(
				"biosim.gcm.KBASAL_VALUE", ""));
		defaultParameters.put(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get(
				"biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		defaultParameters.put(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get(
				"biosim.gcm.STOICHIOMETRY_VALUE", ""));
		defaultParameters.put(GlobalConstants.ACTIVED_STRING, biosimrc.get(
				"biosim.gcm.ACTIVED_VALUE", ""));
		defaultParameters.put(GlobalConstants.MAX_DIMER_STRING, biosimrc.get(
				"biosim.gcm.MAX_DIMER_VALUE", ""));
		defaultParameters.put(GlobalConstants.INITIAL_STRING, biosimrc.get(
				"biosim.gcm.INITIAL_VALUE", ""));

		for (String s : defaultParameters.keySet()) {
			parameters.put(s, defaultParameters.get(s));
		}

	}

	public void setParameters(HashMap<String, String> parameters) {
		for (String s : parameters.keySet()) {
			defaultParameters.put(s, parameters.get(s));
			this.parameters.put(s, parameters.get(s));
		}
	}

	private String checkCompabilitySave(String key) {
		if (key.equals(GlobalConstants.MAX_DIMER_STRING)) {
			return "maxDimer";
		}
		return key;
	}

	private String checkCompabilityLoad(String key) {
		if (key.equals("maxDimer")) {
			return GlobalConstants.MAX_DIMER_STRING;
		}
		return key;
	}

	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) (\\-|\\+|x)(\\>|\\|) *([^ \n]*) *\\[([^\\]]*)]";

	// private static final String PARSE = "(^|\\n) *([^ \\n,]*) *\\-\\> *([^
	// \n,]*)";

	private static final String PARSE = "(^|\\n) *([^ \\n,]*) (\\-|\\+|x)(\\>|\\|) *([^ \n,]*), Promoter ([a-zA-Z\\d_]+)";

	private static final String PROPERTY = "([a-zA-Z\\ \\-]+)=(\"([^\"]*)\"|([^\\s,]+))";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";
	
	private static final String CONDITION = "Conditions\\s\\{([^}]*)\\s\\}";

	private static final String SBMLFILE = GlobalConstants.SBMLFILE + "=\"([^\"]*)\"";

	private static final String DIMABS = GlobalConstants.DIMABS + "=(true|false)";

	private static final String BIOABS = GlobalConstants.BIOABS + "=(true|false)";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";

	// private static final String COMPONENTS_LIST =
	// "Components\\s\\{([^}]*)\\s\\}";

	private String sbmlFile = "";

	private boolean dimAbs = false;

	private boolean bioAbs = false;

	private HashMap<String, Properties> species;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;

	private HashMap<String, Properties> components;
	
	private ArrayList<String> conditions;

	private HashMap<String, String> parameters;

	private HashMap<String, String> defaultParameters;

	private HashMap<String, String> globalParameters;
}
