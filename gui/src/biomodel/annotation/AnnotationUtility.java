package biomodel.annotation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.xml.XMLAttributes;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLToken;
import org.sbml.jsbml.xml.XMLTriple;
import org.sbml.jsbml.JSBML;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.Utility;

public class AnnotationUtility {

	public static void setSBOLAnnotation(SBase sbmlObject, SBOLAnnotation sbolAnnot) {
		if (sbmlObject.isSetAnnotation())
			removeSBOLAnnotation(sbmlObject);
		if (SBMLutilities.appendAnnotation(sbmlObject, sbolAnnot.toXMLString()) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static String parseSBOLAnnotation(SBase sbmlObject, List<URI> sbolURIs) {
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
							sbolURIs.add(new URI(uriMatcher.group(1)));
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	
	}

	public static void setSweepAnnotation(SBase sbmlObject, String sweep) {
		if (sbmlObject.isSetAnnotation())
			removeSweepAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:sweep", sweep);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}
	
	public static void setVectorSizeAnnotation(SBase sbmlObject, String length)  {
		if (sbmlObject.isSetAnnotation())
			removeVectorSizeAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:vector", "http://www.fakeuri.com");
		attr.add("vector:size", ""+length);
		XMLNode node = new XMLNode(new XMLTriple("vector","http://www.fakeuri.com ","vector"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeVectorSizeAnnotation(SBase sbmlObject)  {

		try {
			String annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern vectorSizePattern = Pattern.compile(VECTOR_SIZE_ANNOTATION);
			Matcher vectorSizeMatcher = vectorSizePattern.matcher(annotation);
			if (vectorSizeMatcher.find()) {
				String vectorSizeAnnotation = vectorSizeMatcher.group(0);
				annotation = annotation.replace(vectorSizeAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));
			} 			
		}	
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		//}
	}
	
	public static String parseVectorSizeAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern vectorSizePattern = Pattern.compile(VECTOR_SIZE_ANNOTATION);
			Matcher vectorSizeMatcher = vectorSizePattern.matcher(annotation);
			if (vectorSizeMatcher.find() && vectorSizeMatcher.groupCount()==2) {
				if (vectorSizeMatcher.group(1)!=null) {
					return vectorSizeMatcher.group(1);
				}
				return vectorSizeMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void setMatrixSizeAnnotation(SBase sbmlObject, String numRows, String numCols) {
		if (sbmlObject.isSetAnnotation())
			removeMatrixSizeAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:matrix", "http://www.fakeuri.com");
		attr.add("matrix:size", "(" + numRows + "," + numCols + ")");
		XMLNode node = new XMLNode(new XMLTriple("matrix","http://www.fakeuri.com ","matrix"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeMatrixSizeAnnotation(SBase sbmlObject)  {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern marixSizePattern = Pattern.compile(MATRIX_SIZE_ANNOTATION);
			Matcher mateixSizeMatcher = marixSizePattern.matcher(annotation);
			if (mateixSizeMatcher.find()) {
				String matrixSizeAnnotation = mateixSizeMatcher.group(0);
				annotation = annotation.replace(matrixSizeAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static String[] parseMatrixSizeAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String [] ret = null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern matrixSizePattern = Pattern.compile(MATRIX_SIZE_ANNOTATION);
			Matcher matrixSizeMatcher = matrixSizePattern.matcher(annotation);
			if (matrixSizeMatcher.find() && matrixSizeMatcher.groupCount()==4) {
				ret = new String[2];
				if (matrixSizeMatcher.group(1) != null && matrixSizeMatcher.group(2) != null) {
					ret[0] = matrixSizeMatcher.group(1);
					ret[1] = matrixSizeMatcher.group(2);
				}
				else {
					ret[0] = matrixSizeMatcher.group(3);
					ret[1] = matrixSizeMatcher.group(4);
				}
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void setRowIndexAnnotation(SBase sbmlObject, String i) {
		if (sbmlObject.isSetAnnotation())
			removeRowIndexAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:rowIndex", "http://www.fakeuri.com");
		attr.add("rowIndex:eqn", ""+i);
		XMLNode node = new XMLNode(new XMLTriple("rowIndex","http://www.fakeuri.com ","rowIndex"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeRowIndexAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern rowIndexPattern = Pattern.compile(ROW_INDEX_ANNOTATION);
			Matcher rowIndexMatcher = rowIndexPattern.matcher(annotation);
			if (rowIndexMatcher.find()) {
				String rowIndexAnnotation = rowIndexMatcher.group(0);
				annotation = annotation.replace(rowIndexAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
			//}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String parseRowIndexAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern rowIndexPattern = Pattern.compile(ROW_INDEX_ANNOTATION);
			Matcher rowIndexMatcher = rowIndexPattern.matcher(annotation);
			if (rowIndexMatcher.find() && rowIndexMatcher.groupCount()==2) {
				if (rowIndexMatcher.group(1)!=null) {
					return rowIndexMatcher.group(1);
				}
				return rowIndexMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setColIndexAnnotation(SBase sbmlObject, String j) {
		if (sbmlObject.isSetAnnotation())
			removeColIndexAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:colIndex", "http://www.fakeuri.com");
		attr.add("colIndex:eqn", ""+j);
		XMLNode node = new XMLNode(new XMLTriple("colIndex","http://www.fakeuri.com ","colIndex"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeColIndexAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern colIndexPattern = Pattern.compile(COL_INDEX_ANNOTATION);
			Matcher colIndexMatcher = colIndexPattern.matcher(annotation);
			if (colIndexMatcher.find()) {
				String colIndexAnnotation = colIndexMatcher.group(0);
				annotation = annotation.replace(colIndexAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
			//}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String parseColIndexAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern colIndexPattern = Pattern.compile(COL_INDEX_ANNOTATION);
			Matcher colIndexMatcher = colIndexPattern.matcher(annotation);
			if (colIndexMatcher.find() && colIndexMatcher.groupCount()==2) {
				if (colIndexMatcher.group(1)!=null) {
					return colIndexMatcher.group(1);
				}
				return colIndexMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static void setConversionRowIndexAnnotation(SBase sbmlObject, String i) {
		if (sbmlObject.isSetAnnotation())
			removeConversionRowIndexAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:rowConvIndex", "http://www.fakeuri.com");
		attr.add("rowConvIndex:eqn", ""+i);
		XMLNode node = new XMLNode(new XMLTriple("rowConvIndex","http://www.fakeuri.com ","rowConvIndex"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeConversionRowIndexAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern rowConvIndexPattern = Pattern.compile(CONV_ROW_INDEX_ANNOTATION);
			Matcher rowConvIndexMatcher = rowConvIndexPattern.matcher(annotation);
			if (rowConvIndexMatcher.find()) {
				String rowIndexAnnotation = rowConvIndexMatcher.group(0);
				annotation = annotation.replace(rowIndexAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
			//}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String parseConversionRowIndexAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern rowConvIndexPattern = Pattern.compile(CONV_ROW_INDEX_ANNOTATION);
			Matcher rowConvIndexMatcher = rowConvIndexPattern.matcher(annotation);
			if (rowConvIndexMatcher.find() && rowConvIndexMatcher.groupCount()==2) {
				if (rowConvIndexMatcher.group(1)!=null) {
					return rowConvIndexMatcher.group(1);
				}
				return rowConvIndexMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setConversionColIndexAnnotation(SBase sbmlObject, String j) {
		if (sbmlObject.isSetAnnotation())
			removeConversionColIndexAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:colConvIndex", "http://www.fakeuri.com");
		attr.add("colConvIndex:eqn", ""+j);
		XMLNode node = new XMLNode(new XMLTriple("colConvIndex","http://www.fakeuri.com ","colConvIndex"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject)); 
	}

	public static void removeConversionColIndexAnnotation(SBase sbmlObject) {
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern colConvIndexPattern = Pattern.compile(CONV_COL_INDEX_ANNOTATION);
			Matcher colConvIndexMatcher = colConvIndexPattern.matcher(annotation);
			if (colConvIndexMatcher.find()) {
				String colIndexAnnotation = colConvIndexMatcher.group(0);
				annotation = annotation.replace(colIndexAnnotation, "");
			}
			if (annotation.equals("")) {
				sbmlObject.unsetAnnotation();
			} else {
				annotation = "<annotation>\n"+annotation+"\n</annotation>";
				sbmlObject.setAnnotation(new Annotation(annotation));				
			}
			//}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String parseConversionColIndexAnnotation(SBase sbmlObject) {
		if (sbmlObject==null)
			return null;
		String annotation;
		try {
			annotation = sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
			Pattern colConvIndexPattern = Pattern.compile(CONV_COL_INDEX_ANNOTATION);
			Matcher colConvIndexMatcher = colConvIndexPattern.matcher(annotation);
			if (colConvIndexMatcher.find() && colConvIndexMatcher.groupCount()==2) {
				if (colConvIndexMatcher.group(1)!=null) {
					return colConvIndexMatcher.group(1);
				}
				return colConvIndexMatcher.group(2);
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public static void setArraySizeAnnotation(SBase sbmlObject, int size) {
		if (sbmlObject.isSetAnnotation())
			removeArraySizeAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:array", "http://www.fakeuri.com");
		attr.add("array:size", ""+size);
		XMLNode node = new XMLNode(new XMLTriple("array","http://www.fakeuri.com ","array"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public static void setDynamicAnnotation(SBase sbmlObject, String dynamic) {
		if (sbmlObject.isSetAnnotation())
			removeDynamicAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:type", dynamic);
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setGridAnnotation(SBase sbmlObject, int rows, int cols) {
		if (sbmlObject.isSetAnnotation())
			removeGridAnnotation(sbmlObject);
		XMLAttributes attr = new XMLAttributes();
		attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
		attr.add("ibiosim:grid", "(" + rows + "," + cols + ")");
		XMLNode node = new XMLNode(new XMLTriple("ibiosim","http://www.fakeuri.com ","ibiosim"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
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
		XMLNode node = new XMLNode(new XMLTriple("array","http://www.fakeuri.com ","array"), attr);
		if (SBMLutilities.appendAnnotation(sbmlObject, node) != JSBML.OPERATION_SUCCESS)
			Utility.createErrorMessage("Invalid XML Operation", "Error occurred while annotating SBML element " 
					+ SBMLutilities.getId(sbmlObject));
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
			// TODO Auto-generated catch block
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
			return sbmlObject.isSetAnnotation() && sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim().contains(annotation);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
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
	
	private static final String SBOL_ELEMENT = 	
			"(?:" + DNA_COMPONENTS_ELEMENT + "\\s*" + 
					STRAND_ELEMENT + "|" + 
					STRAND_ELEMENT + "\\s*" + 
					DNA_COMPONENTS_ELEMENT + "|" + 
					DNA_COMPONENTS_ELEMENT + "|" +
					STRAND_ELEMENT + ")";
	
	private static final String SBOL_ANNOTATION = 
		"<ModelToSBOL xmlns=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">\\s*" +
			"(?:<rdf:RDF xmlns:rdf=\"http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#\" xmlns:mts=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\">|" +
			"<rdf:RDF xmlns:mts=\"http://sbolstandard\\.org/modeltosbol/1\\.0#\" xmlns:rdf=\"http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#\">)\\s*" +
				"<rdf:Description rdf:about=\"#" + XML_NAME + "\">\\s*" +
					SBOL_ELEMENT + "\\s*" +
				"</rdf:Description>\\s*" +
			"</rdf:RDF>\\s*" +
		"</ModelToSBOL>";
	
	private static final String GRID_ANNOTATION =
			"<ibiosim:ibiosim xmlns:ibiosim=\"http://www\\.fakeuri\\.com\" ibiosim:grid=\"\\((\\d),(\\d+)\\)\"/>" + "|" +
					"<ibiosim:ibiosim ibiosim:grid=\"\\((\\d+),(\\d+)\\)\" xmlns:ibiosim=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String VECTOR_SIZE_ANNOTATION =
			"<vector:vector xmlns:vector=\"http://www\\.fakeuri\\.com\" vector:size=\"([a-zA-Z]+[_a-zA-Z\\d]*)\"/>" + "|" +
					"<vector:vector vector:size=\"([a-zA-Z]+[_a-zA-Z\\d]*)\" xmlns:vector=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String MATRIX_SIZE_ANNOTATION =
			"<matrix:matrix xmlns:matrix=\"http://www\\.fakeuri\\.com\" matrix:size=\"\\(([a-zA-Z]+[_a-zA-Z\\d]*),([a-zA-Z]+[_a-zA-Z\\d]*)\\)\"/>" + "|" +
					"<matrix:matrix matrix:size=\"\\(([a-zA-Z]+[_a-zA-Z\\d]*),([a-zA-Z]+[_a-zA-Z\\d]*)\\)\" xmlns:matrix=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String ROW_INDEX_ANNOTATION =
			"<rowIndex:rowIndex xmlns:rowIndex=\"http://www\\.fakeuri\\.com\" rowIndex:eqn=\"([^:]+)\"/>" + "|" +
					"<rowIndex:rowIndex rowIndex:eqn=\"([^:]+)\" xmlns:rowIndex=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String COL_INDEX_ANNOTATION =
			"<colIndex:colIndex xmlns:colIndex=\"http://www\\.fakeuri\\.com\" colIndex:eqn=\"([^:]+)\"/>" + "|" +
					"<colIndex:colIndex colIndex:eqn=\"([^:]+)\" xmlns:colIndex=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String CONV_ROW_INDEX_ANNOTATION =
			"<rowConvIndex:rowConvIndex xmlns:rowConvIndex=\"http://www\\.fakeuri\\.com\" rowConvIndex:eqn=\"([^:]+)\"/>" + "|" +
					"<rowConvIndex:rowConvIndex rowConvIndex:eqn=\"([^:]+)\" xmlns:rowConvIndex=\"http://www\\.fakeuri\\.com\"/>";
	
	private static final String CONV_COL_INDEX_ANNOTATION =
			"<colConvIndex:colConvIndex xmlns:colConvIndex=\"http://www\\.fakeuri\\.com\" colConvIndex:eqn=\"([^:]+)\"/>" + "|" +
					"<colConvIndex:colConvIndex colConvIndex:eqn=\"([^:]+)\" xmlns:colConvIndex=\"http://www\\.fakeuri\\.com\"/>";
	
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
