package biomodel.annotation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbml.libsbml.CompSBMLDocumentPlugin;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.libsbml;

import biomodel.parser.BioModel;
import biomodel.util.Utility;

public class AnnotationUtility {

	public static void setSBOLAnnotation(SBase sbmlObject, SBOLAnnotation sbolAnnot) {
		if (sbmlObject.isSetAnnotation())
			removeSBOLAnnotation(sbmlObject);
		if (sbmlObject.appendAnnotation(sbolAnnot.toXMLString()) != libsbml.LIBSBML_OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating species " 
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
	
	public static LinkedList<URI> parseSBOLAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		LinkedList<URI> sbolURIs = new LinkedList<URI>();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return sbolURIs;
	}
	
	public static int[] parseGridAnnotation(SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString();
		Pattern gridPattern = Pattern.compile(GRID_ANNOTATION);
		Matcher gridMatcher = gridPattern.matcher(annotation);
		int[] gridSize = new int[2];
		gridSize[0]=0;
		gridSize[1]=0;
		if (gridMatcher.find()) {
			gridSize[0] = Integer.valueOf(gridMatcher.group(0));
			gridSize[1] = Integer.valueOf(gridMatcher.group(1));
		}
		return gridSize;
	}
	
//	public static LinkedList<URI> parseSubSBOLAnnotation(Submodel instantiation, BioModel bioModel) {
//		CompSBMLDocumentPlugin sbmlCompDoc = bioModel.getSBMLComp();
//		String sbmlSubFileID = sbmlCompDoc.getExternalModelDefinition(instantiation.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//		BioModel bioSubModel = new BioModel(bioModel.getPath());
//		bioSubModel.load(sbmlSubFileID);
//		Model sbmlSubModel = bioSubModel.getSBMLDocument().getModel();
//		return AnnotationUtility.parseSBOLAnnotation(sbmlSubModel);
//	}
	
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
	
}
