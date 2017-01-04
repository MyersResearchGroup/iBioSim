package backend.biomodel.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import frontend.main.Gui;

import org.sbml.jsbml.ModifierSpeciesReference;



/**
 * This is a utility class. The constructor is private so that only one instance
 * of the class exists at any time.
 * 
 * @author Nam
 * 
 */
public class Utility {
	private Utility() {
	}

	public static final Utility getInstance() {
		if (instance == null) {
			instance = new Utility();
		}
		return instance;
	}

	/**
	 * Creates a copy of a double array
	 * 
	 * @param toCopy
	 *            the array to copy
	 * @return a copy of a double array
	 */
	public static double[] createCopy(double[] toCopy) {
		double[] copy = new double[toCopy.length];
		System.arraycopy(toCopy, 0, copy, 0, toCopy.length);
		return copy;
	}

	public static String makeBindingReaction() {
		return "";
	}

/*	public static Compartment makeCompartment(String id) {
		Compartment c = new Compartment("default");
		c.setConstant(true);
		c.setSpatialDimensions(3);
		return c;
	}*/

	public static void createErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog(Gui.frame, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	public static Reaction Reaction(String id) {
		Reaction r = new Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		r.setId(id);
		r.setReversible(false);
		r.setFast(false);
		return r;
	}

	public static SpeciesReference SpeciesReference(String id, double stoichiometry) {
		SpeciesReference sr = new SpeciesReference(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		sr.setSpecies(id);
		sr.setStoichiometry(stoichiometry);
		sr.setConstant(true);
		return sr;
	}

	public static ModifierSpeciesReference ModifierSpeciesReference(String id) {
		ModifierSpeciesReference sr = new ModifierSpeciesReference(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		sr.setSpecies(id);
		return sr;
	}
	
	public static LocalParameter Parameter(String id, double value, String units) {
		LocalParameter p = new LocalParameter(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		p.setId(id);
		p.setValue(value);
		p.setUnits(units);
		return p;
	}
	
	public static LocalParameter Parameter(String id, double value) {
		LocalParameter p = new LocalParameter(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		p.setId(id);
		p.setValue(value);
		return p;
	}
	
	public static Species makeSpecies(String id, String compartment, double amount, double concentration) {
		Species specie = new Species(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		specie.setId(id);
		specie.setName(id);
		specie.setCompartment(compartment);
		if (amount < 0) {
			specie.setInitialConcentration(concentration);
		} else{
			specie.setInitialAmount(amount);	
		}
		specie.setHasOnlySubstanceUnits(true);
		specie.setConstant(false);
		specie.setBoundaryCondition(false);
		return specie;
	}

	public static boolean isValid(String toValidate, String repExp) {
		Pattern pattern = Pattern.compile(repExp);
		Matcher matcher = pattern.matcher(toValidate);
		boolean state = matcher.find();
		if (state) {
			state = matcher.group().equals(toValidate);
		}
		return state;
	}

	public static/* Create add/remove/edit panel */
	JPanel createPanel(ActionListener listener, String panelName,
			JList panelJList, JButton addButton, JButton removeButton,
			JButton editButton) {
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel addRem = new JPanel();
		if (addButton != null) {
			addButton.addActionListener(listener);
			addRem.add(addButton);
		}
		if (removeButton != null) {
			removeButton.addActionListener(listener);
			addRem.add(removeButton);
		}
		if (editButton != null) {
			addRem.add(editButton);
			editButton.addActionListener(listener);
		}

		JLabel panelLabel = new JLabel("List of " + panelName + ":");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(panelJList);

		if (listener instanceof MouseListener) {
			panelJList.addMouseListener((MouseListener) listener);
		}
		Panel.add(panelLabel, "North");
		Panel.add(scroll, "Center");
		Panel.add(addRem, "South");
		return Panel;
	}

	public static HashMap<String, double[]> readFile(String file) {
		HashMap<String, double[]> result = new HashMap<String, double[]>();
		HashMap<Integer, String> resultMap = new HashMap<Integer, String>();

		String allValues = "\\((.*)\\)";
		String oneValue = "\\(([^\\)\\(]+)\\)";
		String headerValue = "\"([^,\"]+)\"";
		Pattern speciesPattern = Pattern.compile(headerValue);
		String doubleValue = "[\\(,]*([\\d]+)[,\\)]";
		Pattern doublePattern = Pattern.compile(doubleValue);

		try {
			boolean headerFilled = false;
			BufferedReader in = new BufferedReader(new FileReader(file));
			StringBuffer data = new StringBuffer();
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();

			Pattern pattern = Pattern.compile(allValues);
			Matcher matcher = pattern.matcher(data.toString());
			matcher.find();
			String dataPoints = matcher.group(1);
			pattern = Pattern.compile(oneValue);
			matcher = pattern.matcher(dataPoints);
			// Setup and take care of headers
			int j = 0;
			while (matcher.find()) {
				Matcher speciesMatcher = speciesPattern.matcher(matcher
						.group(1));
				if (!headerFilled) {
					while (speciesMatcher.find()) {
						String t = speciesMatcher.group();
						result.put(t.replace("\"", ""), null);
						resultMap.put(Integer.valueOf(j), t.replace("\"", ""));
						j++;
					}
					int index = 0;
					int numGroups = 0;
					while (index != -1) {
						index = dataPoints.indexOf("(", index + 1);
						numGroups++;
					}
					for (String s : result.keySet()) {
						result.put(s, new double[numGroups - 1]);
					}
				}
				headerFilled = true;
				break;
			}
			int k = 0;
			while (matcher.find()) {
				Matcher valueMatcher = doublePattern.matcher(matcher.group());
				// Now start reading in values
				j = 0;
				while (valueMatcher.find()) {
					String t = valueMatcher.group(1);
					result.get(resultMap.get(Integer.valueOf(j)))[k] = Double
							.parseDouble(t);
					j++;
				}
				k++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
	
	public static String[] getTSDFiles(String folder) {
		File allFiles = new File(folder);
		String[] files = allFiles.list(Utility.getTSDFilter());
		return files;
	}
	
	public static String[] getFiles(String folder, String filter) {
		File allFiles = new File(folder);
		return allFiles.list(Utility.getFilter(filter));			
	}
	
	public static SBMLDocument openDocument(String filename) {
		SBMLDocument document = SBMLutilities.readSBML(filename);
		return document;
	}

	public static HashMap<String, double[]> calculateAverage(String folder) {
		String separator = Gui.separator;
		HashMap<String, double[]> result = new HashMap<String, double[]>();
		HashMap<String, double[]> average = null;
		String[] files = getTSDFiles(folder);
		for (int i = 0; i < files.length; i++) {
			result = readFile(folder+separator+files[i]);
			if (average == null) {
				average = result;
			} else {
				for (String s : result.keySet()) {
					double[] values = result.get(s);
					double[] averages = average.get(s);
					if (values.length != averages.length) {
						//System.out.println(folder+separator+files[i]);
					}
					for (int j = 0; j < values.length; j++) {
						averages[j] = averages[j] + values[j];
					}
				}
			}
		}

		if (average != null) {
			for (String s : average.keySet()) {
				double[] averages = average.get(s);
				for (int j = 0; j < averages.length; j++) {
					averages[j] = averages[j]/files.length;
				}
			}
		}

		return average;
	}

	public static FilenameFilter getTSDFilter() {
		final class TSDFilter implements java.io.FilenameFilter {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("tsd") && name.contains("run");
			}
		}

		filter = new TSDFilter();		
		return filter;
	}
	
	public static FilenameFilter getFilter(final String ext) {
		final class Filter implements java.io.FilenameFilter {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(ext);
			}
		}
				
		filter = new Filter();		
		return filter;
	}
	
	public static boolean addGlobalParameter(SBMLDocument document, Parameter p) {
		if (document.getModel().getParameter(p.getId()) == null) {
			document.getModel().addParameter(p);
			return true;
		}
		return false;
	}
	
	public static boolean addReaction(SBMLDocument document, Reaction r) {
		if (document.getModel().getReaction(r.getId()) == null) {
			document.getModel().addReaction(r);
			return true;
		}
		ListOf<Reaction> rr = document.getModel().getListOfReactions();
		for (int i = 0; i < document.getModel().getReactionCount(); i++) {
			if (rr.get(i).getId().equals(r.getId())) {
				rr.remove(i);
			}
		}
		document.getModel().addReaction(r);
		return true;
		//Give warning
	}
	
	public static boolean addSpecies(SBMLDocument document, Species species) {
		Species s = document.getModel().getSpecies(species.getId());
		if (s == null) {
			document.getModel().addSpecies(species);
			return true;
		}
		/* TODO: this is not quite right  */
		if (species.isSetInitialAmount() && species.getInitialAmount() >= 0) {
			s.setInitialAmount(species.getInitialAmount());
		} else if (species.isSetInitialConcentration() && species.getInitialConcentration() >= 0){
			s.setInitialConcentration(species.getInitialConcentration());
		}
		s.unsetSBOTerm();
		return true;
	}
	
	public static boolean addUnits() {
		return false;
	}
	
	public static boolean addCompartments(SBMLDocument document, String compartment) {
		if (document.getModel().getCompartment(compartment) == null) {
			Compartment comp = document.getModel().createCompartment();
			comp.setId(compartment);
			comp.setSpatialDimensions(3);
			comp.setConstant(true);
			return true;
		}
		ListOf<Compartment> c = document.getModel().getListOfCompartments();
		for (int i = 0; i < document.getModel().getCompartmentCount(); i++) {
			if (c.get(i).getId().equals(compartment)) {
				c.remove(i);
			}
		}
		Compartment comp = document.getModel().createCompartment();
		comp.setId(compartment);
		comp.setSpatialDimensions(3);
		comp.setConstant(true);
		return true;
		//Give warning
	}
	
	public static double[] getEquilibrium(String Keq) {
		if (Keq==null) {
			double[] params = new double[1];
			params[0]=-1;
			return params;
		}
		String[] props = Keq.split("/");
		
		double[] params = new double[2];
		
		if (props.length == 2) {
			
			params[0] = Double.parseDouble(props[0]);
			params[1] = Double.parseDouble(props[1]);
		}
		else if (props.length == 1) {
			params[0] = Double.parseDouble(props[0]);
			params[1] = 1.0;			
		}
		else {
			params[0] = 1.0;
			params[1] = 1.0;
		}
		
		return params;
	}
	
	public static String MD5(SBMLDocument document) {
		SBMLWriter writer = new SBMLWriter();
		String md5 = null;
		try {
			md5 = writer.writeSBMLToString(document);
		}
		catch (SBMLException e) {
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	private static FilenameFilter filter = null;

	//public static final Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");
	//public static final Pattern NUMpat = Pattern.compile("([\\d]*[\\.\\d]?\\d+)");
	private static Utility instance = null;
	
	//public static final String NUMBER = "[1-9]";
	//public static final String DIGIT = "(0|" + NUMBER + ")";
	//public static final String EXPONENT = "([eE][+-]?" + DIGIT + "+)";
	//public static final String LEADING = "(0|(" + NUMBER + DIGIT + "*))";
//	public static final String FLOAT = "(((" + LEADING + "\\." + DIGIT + "*)|(\\." + DIGIT + "+))" + EXPONENT + 
//	"?)|(" + LEADING + EXPONENT + ")";
	//public static final String FLOAT = LEADING + "\\." + DIGIT + "*";
//	[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?
	public static final String NUMstring = "([\\d]*[\\.\\d]?\\d+)([eE][-+]?[\\d]+)?";
	public static final String CONCstring = "\\[" + NUMstring + "\\]";
	public static final String SLASHstring = "(" + NUMstring + "/)?" + NUMstring;
	public static final String SWEEPformat = "\\(" + NUMstring + ",[[\\s]*]?" + NUMstring + ",[[\\s]*]?" + NUMstring +
		",[[\\s]*]?[12]\\)";
	public static final String SWEEPstring = "(" + SWEEPformat + ")|" + NUMstring;
	public static final String SLASHSWEEPstring = "(\\((" + NUMstring + "/)?" + NUMstring + ",[[\\s]*]?(" + NUMstring + "/)?" + 
		NUMstring + ",[[\\s]*]?(" + NUMstring + "/)?" + NUMstring + ",[[\\s]*]?[12]\\))|(" + NUMstring + "/)?" + NUMstring;
	public static final String PROPstring = "([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*";
	public static final String IDstring = "([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*";
	public static final String IDDimString = IDstring + "(\\[" + IDstring + "])*";
	public static final String FILEstring = "([a-zA-Z]|[0-9])([a-zA-Z]|[0-9]|(_[a-zA-Z0-9]))*(\\.)([a-zA-Z])+";
	//public static final String IDstring = "([a-zA-Z])([a-zA-Z]|[0-9]|_)*";
	public static final String ATACSIDstring = "([a-zA-Z]|_|\\?|!)([a-zA-Z]|[0-9]|_|\\?|!|\\.)*";
	public static final String NAMEstring = "(.)*";
	public static final String VALstring = "(.)+";
	public static final String SBOLFIELDstring = "(" + FILEstring + "/" + IDstring + "/" + IDstring + ")*";

//public static final String DECAY = ".0075";
//	public static final String KDIMER = ".5";
//	public static final String DIMER = "1";
	
	public static final String directory = "/home/shang/namphuon/nobackup/BiologyProjects/muller";
	
	public static final boolean OVERWRITE_WARNING = false;
}
