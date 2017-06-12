/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.xml.XMLAttributes;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

import org.sbml.jsbml.JSBML;

/**
 * Indicate the pattern of SBOL Annotation
 *
 * @author Nicholas Roehner
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AnnotationUtility {

	/**
	 * Annotate the given SBML object with SBOL annotation. If there exist any annotation to the given SBML object,
	 * this method will remove the existing annotation and replace it with the given SBOL Annotation.
	 * 
	 * @param sbmlObject - The SBML object to set the custom SBOL annotation to.
	 * @param sbolAnnot - The SBOL annotation to set the SBML object to. 
	 * @return True if the SBOL annotation was successfully annotated to the given SBML object. False otherwise.
	 */
	public static boolean setSBOLAnnotation(SBase sbmlObject, SBOLAnnotation sbolAnnot) {
		if (sbmlObject.isSetAnnotation())
			removeSBOLAnnotation(sbmlObject);
		if (SBMLutilities.appendAnnotation(sbmlObject, sbolAnnot.toXMLString()) != JSBML.OPERATION_SUCCESS)
		  return false;
		return true;
	}
	
	public static void removeSBOLAnnotation(SBase sbmlObject) {
		try {
			String annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern sbolPattern = Pattern.compile(SBOL_ANNOTATION);
			Matcher sbolMatcher = sbolPattern.matcher(annotation);
			while (sbolMatcher.find()) {
				String sbolAnnotation = sbolMatcher.group(0);
				annotation = annotation.replace(sbolAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}
	
	public static boolean hasSBOLAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
		}
		catch (XMLStreamException e) {
			return false;
		}
		Pattern sbolPattern = Pattern.compile(SBOL_ANNOTATION);
		Matcher sbolMatcher = sbolPattern.matcher(annotation);
		if (sbolMatcher.find()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Parse for SBOL annotation from the given SBML element.
	 * 
	 * @param sbmlObject - The SBML element that contains the SBOL annotation to be parsed.
	 * @param dnaCompURIs - A list of annotated SBOL ComponentDefinition URI that was retrieved from parsing the given SBML element.
	 * @return The annotated SBOL strand (+ or -) found within the given SBML element.
	 */
	public static String parseSBOLAnnotation(SBase sbmlObject, List<URI> dnaCompURIs) {
		return parseSBOLAnnotation(sbmlObject, dnaCompURIs, new HashMap<String, List<URI>>());
	}
	
	public static String parseSBOLAnnotation(SBase sbmlObject, HashMap<String, List<URI>> sbolElementURIs) {
		return parseSBOLAnnotation(sbmlObject, new LinkedList<URI>(), sbolElementURIs);
	}
	
	/**
	 * Parse for SBOL annotation from the given SBML element.
	 * 
	 * @param sbmlObject - The SBML element that contains the SBOL annotation to be parsed.
	 * @param dnaCompURIs - A list of annotated SBOL ComponentDefinition URI that was retrieved from parsing the given SBML element.
	 * @param sbolElementURIs
	 * @return The annotated SBOL strand (+ or -) found within the given SBML element.
	 */
	public static String parseSBOLAnnotation(SBase sbmlObject, List<URI> dnaCompURIs, HashMap<String, List<URI>> sbolElementURIs) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern sbolPattern = Pattern.compile(SBOL_ANNOTATION);
			Matcher sbolMatcher = sbolPattern.matcher(annotation);
			if (sbolMatcher.find()) {
				Pattern componentPattern = Pattern.compile(DNA_COMPONENTS_ELEMENT);
				Matcher componentMatcher = componentPattern.matcher(sbolMatcher.group(0));
				if (componentMatcher.find()) {
					Pattern uriPattern = Pattern.compile(URI_LIST_ELEMENT);
					Matcher uriMatcher = uriPattern.matcher(componentMatcher.group(0));
					while (uriMatcher.find())
						try {
							dnaCompURIs.add(new URI(uriMatcher.group(1)));
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
				}
				Pattern sbolElementPattern = Pattern.compile(SBOL_ELEMENTS);
				Matcher sbolElementMatcher = sbolElementPattern.matcher(sbolMatcher.group(0));
				while (sbolElementMatcher.find()) {
					String className = sbolElementMatcher.group(1);
					className = className.substring(0, className.length() - 1);
					if (!sbolElementURIs.containsKey(className)) {
						sbolElementURIs.put(className, new LinkedList<URI>());
					}
					Pattern uriPattern = Pattern.compile(URI_LIST_ELEMENT);
//					String blah = sbolElementMatcher.group(0);
					Matcher uriMatcher = uriPattern.matcher(sbolElementMatcher.group(0));
					while (uriMatcher.find())
						try {
							sbolElementURIs.get(className).add(new URI(uriMatcher.group(1)));
							// TODO: need to figure out how to handle this cleaner, likely need to get hashmap up to top and use that
							if (className.equals("ComponentDefinition")) {
								dnaCompURIs.clear();
								dnaCompURIs.add(new URI(uriMatcher.group(1)));
							}
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
				}
				Pattern strandPattern = Pattern.compile(STRAND_ELEMENT);
				Matcher strandMatcher = strandPattern.matcher(sbolMatcher.group(0));
				if (strandMatcher.find())
					return strandMatcher.group(1);
			}
			return GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static boolean setSweepAnnotation(SBase sbmlObject, String sweep) {
		if (sbmlObject.isSetAnnotation())
			removeSweepAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:sweep", sweep);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		return SBMLutilities.appendAnnotation(sbmlObject, node) == JSBML.OPERATION_SUCCESS;

	}
	
	public static void removeSweepAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern sweepPattern = Pattern.compile(SWEEP_ANNOTATION);
			Matcher sweepMatcher = sweepPattern.matcher(annotation);
			if (sweepMatcher.find()) {
				String sweepAnnotation = sweepMatcher.group(0);
				annotation = annotation.replace(sweepAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	
	}
	
	public static String parseSweepAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern sweepPattern = Pattern.compile(SWEEP_ANNOTATION);
			Matcher sweepMatcher = sweepPattern.matcher(annotation);
			if (sweepMatcher.find() && sweepMatcher.groupCount()==2) {
				if (sweepMatcher.group(1)!=null)
					return sweepMatcher.group(1);
				return sweepMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	
		return null;
	}
	
	public static boolean setDistributionAnnotation(SBase sbmlObject,String definition) {
		if (sbmlObject.isSetAnnotation())
			sbmlObject.unsetAnnotation();
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns", "http://sbml.org/annotations/distribution");
		attr.add("definition", definition);
		XMLNode node = new XMLNode(new XMLTriple("distribution","http://sbml.org/annotations/distribution",""), attr);
		return SBMLutilities.appendAnnotation(sbmlObject, node) == JSBML.OPERATION_SUCCESS;
		
	}
	
	public static boolean setArraySizeAnnotation(SBase sbmlObject, int size) {
		if (sbmlObject.isSetAnnotation())
			removeArraySizeAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:array", "http://www.fakeuri.com");
		attr.add("array:size", ""+size);
		XMLNode node = new XMLNode(new XMLTriple("array","http://www.fakeuri.com ","array"), attr);
		return (SBMLutilities.appendAnnotation(sbmlObject, node) == JSBML.OPERATION_SUCCESS);
	}
	
	public static void removeArraySizeAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern arraySizePattern = Pattern.compile(ARRAY_SIZE_ANNOTATION);
			Matcher arraySizeMatcher = arraySizePattern.matcher(annotation);
			if (arraySizeMatcher.find()) {
				String arraySizeAnnotation = arraySizeMatcher.group(0);
				annotation = annotation.replace(arraySizeAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}
	
	public static int parseArraySizeAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return -1;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern arraySizePattern = Pattern.compile(ARRAY_SIZE_ANNOTATION);
			Matcher arraySizeMatcher = arraySizePattern.matcher(annotation);
			if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==2) {
				if (arraySizeMatcher.group(1)!=null) {
					return Integer.valueOf(arraySizeMatcher.group(1));
				}
				return Integer.valueOf(arraySizeMatcher.group(2));
			}
			arraySizePattern = Pattern.compile(OLD_ARRAY_SIZE_ANNOTATION);
			arraySizeMatcher = arraySizePattern.matcher(annotation);
			if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==1) {
				annotation = annotation.replace("array:count", "array:size");
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));
				return Integer.valueOf(arraySizeMatcher.group(1));
			}
			arraySizePattern = Pattern.compile(OLD_ARRAY_RANGE_ANNOTATION);
			arraySizeMatcher = arraySizePattern.matcher(annotation);
			if (arraySizeMatcher.find() && arraySizeMatcher.groupCount()==2) {
				annotation = annotation.replace("array:min=\"0\" array:max","array:size");
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));
				return Integer.valueOf(arraySizeMatcher.group(2))+1;
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public static boolean setDynamicAnnotation(SBase sbmlObject, String dynamic) {
		if (sbmlObject.isSetAnnotation())
			removeDynamicAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:type", dynamic);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		return (SBMLutilities.appendAnnotation(sbmlObject, node) == JSBML.OPERATION_SUCCESS);
	}
	
	public static void removeDynamicAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern dynamicPattern = Pattern.compile(DYNAMIC_ANNOTATION);
			Matcher dynamicMatcher = dynamicPattern.matcher(annotation);
			if (dynamicMatcher.find()) {
				String dynamicAnnotation = dynamicMatcher.group(0);
				annotation = annotation.replace(dynamicAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	
	}
	
	public static String parseDynamicAnnotation(SBase sbmlObject) {
		if (sbmlObject==null) return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();

			Pattern dynamicPattern = Pattern.compile(DYNAMIC_ANNOTATION);
			Matcher dynamicMatcher = dynamicPattern.matcher(annotation);
			if (dynamicMatcher.find() && dynamicMatcher.groupCount()==2) {
				if (dynamicMatcher.group(1)!=null) 
					return dynamicMatcher.group(1);
				return dynamicMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean setGridAnnotation(SBase sbmlObject, int rows, int cols) {
		if (sbmlObject.isSetAnnotation())
			removeGridAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:grid", "(" + rows + "," + cols + ")");
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		return SBMLutilities.appendAnnotation(sbmlObject, node) == JSBML.OPERATION_SUCCESS;
	}
	
	public static void removeGridAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
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
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public static int[] parseGridAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			if (annotation==null) return null;
			Pattern gridPattern = Pattern.compile(GRID_ANNOTATION);
			Matcher gridMatcher = gridPattern.matcher(annotation);
			int[] gridSize = null;
			if (gridMatcher.find() && gridMatcher.groupCount()==4) {
				gridSize = new int[2];
				if (gridMatcher.group(1) != null && gridMatcher.group(2) != null) {
					gridSize[0] = Integer.valueOf(gridMatcher.group(1));
					gridSize[1] = Integer.valueOf(gridMatcher.group(2));
				}
				else {
					gridSize[0] = Integer.valueOf(gridMatcher.group(3));
					gridSize[1] = Integer.valueOf(gridMatcher.group(4));
				}
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
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static boolean setArrayAnnotation(SBase sbmlObject, String array) {
		if (sbmlObject.isSetAnnotation())
			removeArrayAnnotation(sbmlObject);
		String [] attributes = array.split(" ");
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:array", "http://www.fakeuri.com");
		for (int i = 0; i < attributes.length; i++) {
			attr.add("array:"+attributes[i].split("=")[0], attributes[i].split("=")[1].replace("\"", ""));
		}
		XMLNode node = new XMLNode(new XMLTriple("array","http://www.fakeuri.com ","array"), attr);
		return SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS;
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
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();

			Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
			Matcher arrayMatcher = arrayPattern.matcher(annotation);
			if (arrayMatcher.find()) {
				//String arrayAnnotation = arrayMatcher.group(0);
				annotation = annotation.replaceAll(" array:"+element+"=\"\\(\\d+,\\d+\\)\"", "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		/*
		//get rid of the component from the location-lookup array and the modelref array
		if (AnnotationUtility.parseArrayAnnotation(sbmlObject).length==2 &&
				(((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex("array:" + element)>=0 || 
				((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex("array:" + element, "http://www.fakeuri.com")>=0 || 
				((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex(element, "http://www.fakeuri.com")>=0)) {
			removeArrayAnnotation(sbmlObject);
			return true;
		} 
		((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).removeAttr(
				((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex("array:" + element));
		((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).removeAttr(
				((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex("array:" + element, "http://www.fakeuri.com"));
		((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).removeAttr(
				((XMLToken) sbmlObject.getAnnotation().getChildAt(0)).getAttrIndex(element, "http://www.fakeuri.com"));
		*/
		return false;
	}
	
	public static void removeArrayAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();

			Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
			Matcher arrayMatcher = arrayPattern.matcher(annotation);
			if (arrayMatcher.find()) {
				String arrayAnnotation = arrayMatcher.group(0);
				annotation = annotation.replace(arrayAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] parseArrayAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();

			Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
			Matcher arrayMatcher = arrayPattern.matcher(annotation);
			if (arrayMatcher.find()) {
				if (arrayMatcher.group(1) != null) {
					return arrayMatcher.group(1).replace("\"","").replace(" ","").split("array:");
				}
				return arrayMatcher.group(2).replace("\"","").replace(" ","").split("array:");
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int[] parseSpeciesArrayAnnotation(Species species) {
		String annotation;
		try {
			annotation = species.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern arrayPattern = Pattern.compile(SPECIES_ARRAY_ANNOTATION);
			Matcher arrayMatcher = arrayPattern.matcher(annotation);
			if (arrayMatcher.find()) {
				if (arrayMatcher.group(1)!=null && arrayMatcher.group(2)!=null &&
						arrayMatcher.group(3)!=null && arrayMatcher.group(4)!=null) {
					int[] result = new int[4];
					result[0] = Integer.parseInt(arrayMatcher.group(1));
					result[1] = Integer.parseInt(arrayMatcher.group(2));
					result[2] = Integer.parseInt(arrayMatcher.group(3));
					result[3] = Integer.parseInt(arrayMatcher.group(4));
					return result;
				}
			}
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] parseArrayAnnotation(org.sbml.libsbml.SBase sbmlObject) {
		String annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
		Pattern arrayPattern = Pattern.compile(ARRAY_ANNOTATION);
		Matcher arrayMatcher = arrayPattern.matcher(annotation);
		if (arrayMatcher.find()) {
			if (arrayMatcher.group(1) != null) {
				return arrayMatcher.group(1).replace("\"","").replace(" ","").split("array:");
			}
			return arrayMatcher.group(2).replace("\"","").replace(" ","").split("array:");
		}
		return null;
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
		try {
			return sbmlObject.isSetAnnotation() && 
					sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim().contains(annotation) &&
					!sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim().contains("rdf");
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void removeObsoleteAnnotation(SBase sbmlObject) {
		sbmlObject.unsetAnnotation();
	}
	
	private static final String XML_NAME_START_CHAR = "[:[A-Z]_[a-z][\\u00C0-\\u00D6][\\u00D8-\\u00F6]" +
			"[\\u00F8-\\u02FF][\\u0370-\\u037D][\\u037F-\\u1FFF][\\u200C-\\u200D][\\u2070-\\u218F][\\u2C00-\\u2FEF]" +
			"[\\u3001-\\uD7FF][\\uF900-\\uFDCF][\\uFDF0-\\uFFFD][\\u10000-\\uEFFFF]]";
	
	private static final String XML_NAME_CHAR = "[" + XML_NAME_START_CHAR + "[-\\.[0-9]\\u00B7[\\u0300-\\u036F][\\u203F-\\u2040]]]";
	
	private static final String XML_NAME = XML_NAME_START_CHAR + XML_NAME_CHAR + "*";
	
	// Current regular expression for component URI is \\S+ (any sequence of non-whitespace characters)
	// until we have a reason to prefer a certain form (e.g. a use case other than copying the URI over)
	private static final String URI_LIST_ELEMENT = "<rdf:li rdf:resource=\"(\\S+)\"/>";
	
	private static final String DNA_COMPONENTS_ELEMENT = 
			"<mts:DNAComponents>\\s*" +
				"<rdf:Seq>\\s*" +
					"(?:" + URI_LIST_ELEMENT + "\\s*)+" +
				"</rdf:Seq>\\s*" + 
			"</mts:DNAComponents>";
	
	private static final String STRAND_ELEMENT = "<mts:Strand>(\\" + GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND + "|" + GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND + ")</mts:Strand>";
	
	private static final String SBOL_ELEMENTS = 
			"<mts:(\\w+)>\\s*" +
				"<rdf:Bag>\\s*" +
					"(?:" + URI_LIST_ELEMENT + "\\s*)+" +
				"</rdf:Bag>\\s*" + 
			"</mts:\\w+>";
	
	private static final String SBOL_CONTENT = 
		"(?:" + 
			"(?:" + SBOL_ELEMENTS + "\\s*" + ")*" + "(?:" + DNA_COMPONENTS_ELEMENT + "\\s*" + ")?" + "(?:" + SBOL_ELEMENTS + "\\s*" + ")*" + "(?:" + STRAND_ELEMENT + "\\s*" + ")?" + "(?:" + SBOL_ELEMENTS + "\\s*" + ")*" + 
			"|" + "(?:" + SBOL_ELEMENTS + "\\s*" + ")*" + "(?:" + STRAND_ELEMENT + "\\s*" + ")?" + "(?:" + SBOL_ELEMENTS + "\\s*" + ")*" + "(?:" + DNA_COMPONENTS_ELEMENT + "\\s*" + ")?" + "(?:" + SBOL_ELEMENTS + "\\s*" + ")*" +
		")";
	
	private static final String SBOL_ANNOTATION = 
		"<ModelToSBOL xmlns=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">\\s*" +
			"(?:<rdf:RDF xmlns:rdf=\"http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#\" xmlns:mts=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">|" +
			"<rdf:RDF xmlns:mts=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\" xmlns:rdf=\"http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#\">)\\s*" +
				"<rdf:Description rdf:about=\"#" + XML_NAME + "\">\\s*" +
					SBOL_CONTENT + "\\s*" +
				"</rdf:Description>\\s*" +
			"</rdf:RDF>\\s*" +
		"</ModelToSBOL>";
	
	private static final String GRID_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:grid=\"\\((\\d),(\\d+)\\)\"/>" + "|" +
					"<ibiosim:ibiosim ibiosim:grid=\"\\((\\d+),(\\d+)\\)\" xmlns:ibiosim=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String SPECIES_ARRAY_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:rowsLowerLimit=\"(\\d+)\" array:colsLowerLimit=\"(\\d+)\" array:rowsUpperLimit=\"(\\d+)\" array:colsUpperLimit=\"(\\d+)\"/>" 
	+ "|" +	"<array:array array:rowsLowerLimit=\"(\\d+)\" array:colsLowerLimit=\"(\\d+)\" array:rowsUpperLimit=\"(\\d+)\" array:colsUpperLimit=\"(\\d+)\" xmlns:array=\"http://www\\.fakeuri\\.com\"/>"; 
	
	private static final String LAYOUT_GRID_ANNOTATION = "grid=\\((\\d+),(\\d+)\\)";
	
	private static final String OLD_GRID_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:type=\"grid\"/>";
	
	private static final String DYNAMIC_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:type=\"([\\w\\s]+)\"/>" + "|" +
					"<ibiosim:ibiosim ibiosim:type=\"([\\w\\s]+)\" xmlns:ibiosim=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String SWEEP_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:sweep=\"(\\S+)\"/>" + "|" +
					"<ibiosim:ibiosim ibiosim:sweep=\"(\\S+)\" xmlns:ibiosim=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String OLD_ARRAY_SIZE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:count=\"(\\d+)\"/>";
	
	private static final String OLD_ARRAY_RANGE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:min=\"(\\d+)\" array:max=\"(\\d+)\"/>";
	
	private static final String ARRAY_SIZE_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" array:size=\"(\\d+)\"/>" + "|" +
					"<array:array array:size=\"(\\d+)\" xmlns:array=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String ARRAY_ANNOTATION =
			"<array:array xmlns:array=\"http://www\\.fakeuri\\.com\" (.+)/>" + "|" +
					"<array:array (.+) xmlns:array=\"http://www\\.fakeuri\\.com\"/>";
	
}
