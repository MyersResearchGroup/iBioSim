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
		if (sweepMatcher.find() && sweepMatcher.groupCount()==1) {
			return sweepMatcher.group(1);
		}
		return null;
	}

	public static void setArraySizeAnnotation(SBase sbmlObject, int size) {
		if (sbmlObject.isSetAnnotation())
			removeArraySizeAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:array", "http://www.fakeuri.com");
		attr.add("array:size", ""+size);
		XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void removeArraySizeAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern arraySizePattern = Pattern.compile(ARRAY_SIZE_ANNOTATION);
		Matcher arraySizeMatcher = arraySizePattern.matcher(annotation);
		if (arraySizeMatcher.find()) {
			String arraySizeAnnotation = arraySizeMatcher.group(0);
			annotation = annotation.replace(arraySizeAnnotation, "");
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static int parseArraySizeAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return -1;
		String annotation = sbmlObject.getAnnotationString();
		Pattern arraySizePattern = Pattern.compile(ARRAY_SIZE_ANNOTATION);
		Matcher arraySizeMatcher = arraySizePattern.matcher(annotation);
		if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==1) {
			return Integer.valueOf(arraySizeMatcher.group(1));
		} else {
			arraySizePattern = Pattern.compile(OLD_ARRAY_SIZE_ANNOTATION);
			arraySizeMatcher = arraySizePattern.matcher(annotation);
			if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==1) {
				annotation = annotation.replace("array:count", "array:size");
				sbmlObject.setAnnotation(annotation);
				return Integer.valueOf(arraySizeMatcher.group(1));
			} else {
				arraySizePattern = Pattern.compile(OLD_ARRAY_RANGE_ANNOTATION);
				arraySizeMatcher = arraySizePattern.matcher(annotation);
				if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==2) {
					annotation = annotation.replace("array:min=\"0\" array:max","array:size");
					sbmlObject.setAnnotation(annotation);
					return Integer.valueOf(arraySizeMatcher.group(2))+1;
				}				
			}
		}
		return -1;
	}
	
	public static void setDynamicAnnotation(SBase sbmlObject, String dynamic) {
		if (sbmlObject.isSetAnnotation())
			removeDynamicAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:type", dynamic);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void removeDynamicAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern dynamicPattern = Pattern.compile(DYNAMIC_ANNOTATION);
		Matcher dynamicMatcher = dynamicPattern.matcher(annotation);
		if (dynamicMatcher.find()) {
			String dynamicAnnotation = dynamicMatcher.group(0);
			annotation = annotation.replace(dynamicAnnotation, "");
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static String parseDynamicAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return null;
		String annotation = sbmlObject.getAnnotationString();
		Pattern dynamicPattern = Pattern.compile(DYNAMIC_ANNOTATION);
		Matcher dynamicMatcher = dynamicPattern.matcher(annotation);
		if (dynamicMatcher.find() && dynamicMatcher.groupCount()==1) {
			return dynamicMatcher.group(1);
		}
		return null;
	}
	
	public static void setGridAnnotation(SBase sbmlObject, int rows, int cols) {
		if (sbmlObject.isSetAnnotation())
			removeGridAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:grid", "(" + rows + "," + cols + ")");
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void removeGridAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern gridPattern = Pattern.compile(GRID_ANNOTATION);
		Matcher gridMatcher = gridPattern.matcher(annotation);
		if (gridMatcher.find()) {
			String gridAnnotation = gridMatcher.group(0);
			annotation = annotation.replace(gridAnnotation, "");
		} else {
			gridPattern = Pattern.compile(OLD_GRID_ANNOTATION);
			gridMatcher = gridPattern.matcher(annotation);
			if (gridMatcher.find()) {
				String gridAnnotation = gridMatcher.group(0);
				annotation = annotation.replace(gridAnnotation, "");
			}
		}
		sbmlObject.setAnnotation(annotation);
	}
	
	public static int[] parseGridAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		if (annotation==null) return null;
		Pattern gridPattern = Pattern.compile(GRID_ANNOTATION);
		Matcher gridMatcher = gridPattern.matcher(annotation);
		int[] gridSize = null;
		if (gridMatcher.find() && gridMatcher.groupCount()==2) {
			gridSize = new int[2];
			gridSize[0] = Integer.valueOf(gridMatcher.group(1));
			gridSize[1] = Integer.valueOf(gridMatcher.group(2));
		} else {
			gridPattern = Pattern.compile(OLD_GRID_ANNOTATION);
			gridMatcher = gridPattern.matcher(annotation);
			if (gridMatcher.find()) {
				gridSize = new int[2];
				gridSize[0]=0;
				gridSize[1]=0;
			} else {
				gridPattern = Pattern.compile(LAYOUT_GRID_ANNOTATION);
				gridMatcher = gridPattern.matcher(annotation);
				if (gridMatcher.find() && gridMatcher.groupCount()==2) {
					gridSize = new int[2];
					gridSize[0] = Integer.valueOf(gridMatcher.group(1));
					gridSize[1] = Integer.valueOf(gridMatcher.group(2));
				}				
			}
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
			attr.add("array:"+attributes[i].split("=")[0], attributes[i].split("=")[1].replace("\"", ""));
		}
		XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
		if (sbmlObject.appendAnnotation(node) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ sbmlObject.getId());
	}
	
	public static void appendArrayAnnotation(SBase sbmlObject, String newElement) {
		String elements = newElement;
		String[] oldElements = AnnotationUtility.parseArrayAnnotation(sbmlObject);
		if (oldElements != null) {
			for (int i = 1; i < oldElements.length; i++) {
				elements += " " + oldElements[i]; 
			}
		}
		AnnotationUtility.setArrayAnnotation(sbmlObject, elements);
	}
	
	public static boolean removeArrayAnnotation(SBase sbmlObject, String element) {
		//get rid of the component from the location-lookup array and the modelref array
		if (AnnotationUtility.parseArrayAnnotation(sbmlObject).length==2 &&
				(sbmlObject.getAnnotation().getChild(0).getAttrIndex("array:" + element)>=0 || 
				sbmlObject.getAnnotation().getChild(0).getAttrIndex("array:" + element, "http://www.fakeuri.com")>=0 || 
				sbmlObject.getAnnotation().getChild(0).getAttrIndex(element, "http://www.fakeuri.com")>=0)) {
			removeArrayAnnotation(sbmlObject);
			return true;
		} 
		sbmlObject.getAnnotation().getChild(0).removeAttr(
				sbmlObject.getAnnotation().getChild(0).getAttrIndex("array:" + element));
		sbmlObject.getAnnotation().getChild(0).removeAttr(
				sbmlObject.getAnnotation().getChild(0).getAttrIndex("array:" + element, "http://www.fakeuri.com"));
		sbmlObject.getAnnotation().getChild(0).removeAttr(
				sbmlObject.getAnnotation().getChild(0).getAttrIndex(element, "http://www.fakeuri.com"));
		return false;
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
	
	public static String[] parseArrayAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
		Matcher arrayMatcher = arrayPattern.matcher(annotation);
		if (arrayMatcher.find()) {
			return arrayMatcher.group(1).replace("\"","").replace(" ","").split("array:");
		} else {
			return null;
		}
	}
	
	public static String parseArrayAnnotation(SBase sbmlObject,String element) {
		String[] elements = parseArrayAnnotation(sbmlObject);
		for (int i=1; i < elements.length; i++) {
			if (elements[i].startsWith(element + "=")) {
				return elements[i];
			}
		}
		return null;
	}
	
	public static boolean checkObsoleteAnnotation(SBase sbmlObject, String annotation) {
		return sbmlObject.isSetAnnotation() && sbmlObject.getAnnotationString().contains(annotation);
	}
	
	public static void removeObsoleteAnnotation(SBase sbmlObject) {
		sbmlObject.unsetAnnotation();
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
	
	private static final String LAYOUT_GRID_ANNOTATION = "grid=\\((\\d+),(\\d+)\\)";
	
	private static final String OLD_GRID_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:type=\"grid\"/>";
	
	private static final String DYNAMIC_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:type=\"([\\w\\s]+)\"/>";
	
	private static final String SWEEP_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:sweep=\"(\\S+)\"/>";
	
	private static final String OLD_ARRAY_SIZE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:count=\"(\\d+)\"/>";
	
	private static final String OLD_ARRAY_RANGE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:min=\"(\\d+)\" array:max=\"(\\d+)\"/>";
	
	private static final String ARRAY_SIZE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:size=\"(\\d+)\"/>";
	
	private static final String ARRAY_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" (.+)/>";
	
}
