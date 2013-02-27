package biomodel.annotation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbml.libsbml.SBase;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLTriple;
import org.sbml.libsbml.libsbml;

import biomodel.util.Utility;

public class AnnotationUtility {

	public static void setSBOLAnnotation(SBase sbmlObject, SBOLAnnotation sbolAnnot) {
		if (sbmlObject.isSetAnnotation())
			removeSBOLAnnotation(sbmlObject);
		if (sbmlObject.appendAnnotation(sbolAnnot.toXMLString()) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void removeSBOLAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern sbolPattern = Pattern.compile(SBOL_ANNOTATION);
		Matcher sbolMatcher = sbolPattern.matcher(annotation);
		while (sbolMatcher.find()) {
			String sbolAnnotation = sbolMatcher.group(0);
			annotation = annotation.replace(sbolAnnotation, "");
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static List<URI> parseSBOLAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		List<URI> sbolURIs = new LinkedList<URI>();
		Pattern sbolPattern = Pattern.compile(SBOL_ANNOTATION);
		Matcher sbolMatcher = sbolPattern.matcher(annotation);
		Pattern componentPattern = Pattern.compile(DNA_COMPONENT_ANNOTATION);
		Matcher componentMatcher = componentPattern.matcher(annotation);
		while (sbolMatcher.find()) {
			annotation = sbolMatcher.group(0);
			while (componentMatcher.find()) 
				try {
					sbolURIs.add(new URI(componentMatcher.group(1)));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
		}
		return sbolURIs;
	}

	public static void setSweepAnnotation(SBase sbmlObject, String sweep) {
		if (sbmlObject.isSetAnnotation())
			removeSweepAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:sweep", sweep);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void removeSweepAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern sweepPattern = Pattern.compile(SWEEP_ANNOTATION);
		Matcher sweepMatcher = sweepPattern.matcher(annotation);
		if (sweepMatcher.find()) {
			String sweepAnnotation = sweepMatcher.group(0);
			annotation = annotation.replace(sweepAnnotation, "");
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static String parseSweepAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return null;
		String annotation = sbmlObject.getAnnotationString();
		Pattern sweepPattern = Pattern.compile(SWEEP_ANNOTATION);
		Matcher sweepMatcher = sweepPattern.matcher(annotation);
		//System.out.println("Parsing "+annotation);
		if (sweepMatcher.find() && sweepMatcher.groupCount()==1) {
			//System.out.println("Returning " + sweepMatcher.group(1));
			return sweepMatcher.group(1);
		}
		return null;
	}
	
	public static int[] parseGridAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern gridPattern = Pattern.compile(GRID_ANNOTATION);
		Matcher gridMatcher = gridPattern.matcher(annotation);
		int[] gridSize = new int[2];
		gridSize[0]=0;
		gridSize[1]=0;
		if (gridMatcher.find() && gridMatcher.groupCount()==2) {
			gridSize[0] = Integer.valueOf(gridMatcher.group(1));
			gridSize[1] = Integer.valueOf(gridMatcher.group(2));
		}
		return gridSize;
	}
	
	public static void setArrayAnnotation(SBase sbmlObject, String array) {
		if (sbmlObject.isSetAnnotation())
			removeArrayAnnotation(sbmlObject);
		String [] attributes = array.split(" ");
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:array", "http://www.fakeuri.com");
		for (int i = 0; i < attributes.length; i++) {
			attr.add(attributes[i].split("=")[0], attributes[i].split("=")[1].replace("\"", ""));
		}
		XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	
	public static void removeArrayAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
		Matcher arrayMatcher = arrayPattern.matcher(annotation);
		if (arrayMatcher.find()) {
			String arrayAnnotation = arrayMatcher.group(0);
			annotation = annotation.replace(arrayAnnotation, "");
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static String parseArrayAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
		Matcher arrayMatcher = arrayPattern.matcher(annotation);
		if (arrayMatcher.find()) {
			return arrayMatcher.group(1);
		} else {
			return "";
		}
	}
	
	private static final String XML_NAME_START_CHAR = "[:[A-Z]_[a-z][\\u00C0-\\u00D6][\\u00D8-\\u00F6]" +
			"[\\u00F8-\\u02FF][\\u0370-\\u037D][\\u037F-\\u1FFF][\\u200C-\\u200D][\\u2070-\\u218F][\\u2C00-\\u2FEF]" +
			"[\\u3001-\\uD7FF][\\uF900-\\uFDCF][\\uFDF0-\\uFFFD][\\u10000-\\uEFFFF]]";
	
	private static final String XML_NAME_CHAR = "[" + XML_NAME_START_CHAR + "[-\\.[0-9]\\u00B7[\\u0300-\\u036F][\\u203F-\\u2040]]]";
	
	private static final String XML_NAME = "(?:" + XML_NAME_START_CHAR + XML_NAME_CHAR + "*)";
	
	// Current regular expression for component URI is \\S+ (any sequence of non-whitespace characters)
	// until we have a reason to prefer a certain form (e.g. a use case other than copying the URI over)
	private static final String DNA_COMPONENT_ANNOTATION = "(?:<rdf:li rdf:resource=\"(\\S+)\"/>)";
	
	private static final String SBOL_ANNOTATION = 
		"<ModelToSBOL xmlns=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">\\s*" +
			"<rdf:RDF xmlns:rdf=\"http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#\" xmlns:mts=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">\\s*" +
				"<rdf:Description rdf:about=\"#" + XML_NAME + "\">\\s*" +
					"<mts:DNAComponents>\\s*" +
						"<rdf:Seq>\\s*" +
							"(?:" + DNA_COMPONENT_ANNOTATION + "\\s*)+" +
						"</rdf:Seq>\\s*" + 
					"</mts:DNAComponents>\\s*" +
				"</rdf:Description>\\s*" +
			"</rdf:RDF>\\s*" +
		"</ModelToSBOL>";
	
	private static final String GRID_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:grid=\"\\((\\d+),(\\d+)\\)\"/>";
	
	private static final String SWEEP_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:sweep=\"(\\S+)\"/>";
	
	private static final String ARRAY_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" (.+)/>";
	
}
