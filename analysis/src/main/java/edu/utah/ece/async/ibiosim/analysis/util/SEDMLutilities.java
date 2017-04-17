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
package edu.utah.ece.async.ibiosim.analysis.util;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jlibsedml.AbstractIdentifiableElement;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.AddXML;
import org.jlibsedml.Annotation;
import org.jlibsedml.Change;
import org.jlibsedml.ChangeAttribute;
import org.jlibsedml.ChangeXML;
import org.jlibsedml.ComputeChange;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.DataSet;
import org.jlibsedml.FunctionalRange;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Model;
import org.jlibsedml.OneStep;
import org.jlibsedml.Output;
import org.jlibsedml.Parameter;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Plot3D;
import org.jlibsedml.Range;
import org.jlibsedml.RemoveXML;
import org.jlibsedml.RepeatedTask;
import org.jlibsedml.Report;
import org.jlibsedml.SEDBase;
import org.jlibsedml.SedML;
import org.jlibsedml.SetValue;
import org.jlibsedml.Simulation;
import org.jlibsedml.SteadyState;
import org.jlibsedml.SubTask;
import org.jlibsedml.Surface;
import org.jlibsedml.Task;
import org.jlibsedml.UniformRange;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.VectorRange;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.jmathml.ASTNode;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SEDMLutilities {
	
	public static String getXPathForReactant(String reactionId,String reactantId) {
		SBMLSupport support = new SBMLSupport();
        return support.getXPathForReaction(reactionId)
                + "/sbml:listOfReactants" + "/sbml:speciesReference[@id='" + reactantId + "']";
	}
	
	public static String getXPathForProduct(String reactionId,String productId) {
		SBMLSupport support = new SBMLSupport();
        return support.getXPathForReaction(reactionId)
                + "/sbml:listOfProducts" + "/sbml:speciesReference[@id='" + productId + "']";
	}
	
	public static DataGenerator getDataGenerator(SedML sedml,String variableId,String variableName,
			String dataSet,String taskId,String type,String reactionId) {
		SBMLSupport support = new SBMLSupport();
		for (DataGenerator dataGenerator : sedml.getDataGenerators()) {
			if (dataGenerator.getListOfVariables().size()!=1) continue;
			String ds = SEDMLutilities.getSEDBaseAnnotation(dataGenerator, "dataSet", "dataset", "");
			Variable variable = dataGenerator.getListOfVariables().get(0);
			if (variable.getReference().equals(taskId) &&
					variable.getTarget() != null &&
					support.getIdFromXPathIdentifer(variable.getTarget()) != null &&
					support.getIdFromXPathIdentifer(variable.getTarget()).equals(variableId) &&
					ds.equals(dataSet)) {
				return dataGenerator;
			}
		}
		// TODO: can be timelimit for probablity analysis, not an id in the model at all
		String xpath = "";
		Variable variable;
		if (variableId.equals("time")) {
			variable = new Variable(variableId+"_"+taskId+"_"+dataSet+"_var",variableName,taskId,VariableSymbol.TIME);
		} else {
			if (type.equals("compartment")) {
				xpath = support.getXPathForCompartment(variableId);
			} else if (type.equals("species")) {
				xpath = support.getXPathForSpecies(variableId);
			} else if (type.equals("parameter")) {
				xpath = support.getXPathForGlobalParameter(variableId);
			} else if (type.equals("reaction")) {
				xpath = support.getXPathForReaction(variableId);
			} else if (type.equals("reactant")) {
				xpath = getXPathForReactant(reactionId,variableId);
			} else if (type.equals("product")) {
				xpath = getXPathForProduct(reactionId,variableId);
			}
			variable = new Variable(variableId+"_"+taskId+"_"+dataSet+"_var",variableName,taskId,xpath);
		}
		DataGenerator dataGen = sedml.getDataGeneratorWithId(variableId+"_"+taskId+"_"+dataSet+"_dg");
		if (dataGen != null) {
			sedml.removeDataGenerator(dataGen);
		}
		ASTNode math = Libsedml.parseFormulaString(variableId+"_"+taskId+"_"+dataSet+"_var");
		dataGen = new DataGenerator(variableId+"_"+taskId+"_"+dataSet+"_dg",variableId,math);
		dataGen.addVariable(variable);
		if (!dataSet.equals("")) {
			Element para = new Element("dataSet");
			para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
			para.setAttribute("dataset", "" + dataSet);
			Annotation ann = new Annotation(para);
			dataGen.addAnnotation(ann);
		}
		sedml.addDataGenerator(dataGen);
		return dataGen;
	}
	
	public static void removeDataGeneratorsByTaskId(SedML sedml,String taskId) {
		ArrayList<DataGenerator> remove = new ArrayList<DataGenerator>();
		for (DataGenerator dg : sedml.getDataGenerators()) {
			for (Variable var : dg.getListOfVariables()) {
				if (var.getReference().equals(taskId)) {
					remove.add(dg);
					break;
				}
			}
		}
		for (DataGenerator dg : remove) {
			sedml.removeDataGenerator(dg);
		}
	}
	
	public static void copyAnnotation(SEDBase sedBase1,SEDBase sedBase2) {
		Annotation annotation = sedBase1.getAnnotation();
		if (annotation!=null) {
			List<Element> elements = annotation.getAnnotationElementsList();
			for (Element element : elements) {
				if (!element.getName().equals("sbsi-editor")) {
					Annotation newAnnotation = new Annotation((Element)element.clone());
					sedBase2.setAnnotation(newAnnotation);
					// TODO: only copying first element
					break;
				}
			}
		}
	}
	
	public static Change copyChange(Change change) {
		if (change instanceof RemoveXML) {
			RemoveXML removeXML = (RemoveXML)change;
			RemoveXML newRemoveXML = new RemoveXML(removeXML.getTargetXPath());
			copyAnnotation(removeXML,newRemoveXML);
			return newRemoveXML;
		} else if (change instanceof AddXML) {
			AddXML addXML = (AddXML)change;
			AddXML newAddXML = new AddXML(addXML.getTargetXPath(),addXML.getNewXML());
			copyAnnotation(addXML,newAddXML);
			return newAddXML;
		} else if (change instanceof ChangeAttribute) {
			ChangeAttribute changeAttribute = (ChangeAttribute)change;
			ChangeAttribute newChangeAttribute = new ChangeAttribute(changeAttribute.getTargetXPath(),
					changeAttribute.getNewValue());
			copyAnnotation(changeAttribute,newChangeAttribute);
			return newChangeAttribute;
		} else if (change instanceof ChangeXML) {
			ChangeXML changeXML = (ChangeXML)change;
			ChangeXML newChangeXML = new ChangeXML(changeXML.getTargetXPath(),changeXML.getNewXML());
			copyAnnotation(changeXML,newChangeXML);
			return newChangeXML;
		} else if (change instanceof ComputeChange) {
			ComputeChange computeChange = (ComputeChange)change;
			ComputeChange newComputeChange = new ComputeChange(computeChange.getTargetXPath(),
					computeChange.getMath());
			for (Variable variable : computeChange.getListOfVariables()) {
				newComputeChange.addVariable(copyVariable(variable));
			}
			for (Parameter parameter : computeChange.getListOfParameters()) {
				newComputeChange.addParameter(copyParameter(parameter));
			}
			copyAnnotation(computeChange,newComputeChange);
			return newComputeChange;
		}
		return null;
	}
	
	public static Model copyModel(Model model,String id) {
		Model newModel = new Model(id,model.getName(),model.getLanguage(),model.getSource());
		for (Change change : model.getListOfChanges()) {
			newModel.addChange(copyChange(change));
		}
		copyAnnotation(model,newModel);
		return newModel;
	}
	
	public static Simulation copySimulation(Simulation simulation,String newId) {
		// TODO: really need to copy the algorithm?
		if (simulation instanceof UniformTimeCourse) {
			UniformTimeCourse utc = (UniformTimeCourse)simulation;
			UniformTimeCourse newUTC = new UniformTimeCourse(newId,utc.getName(),
					utc.getInitialTime(),utc.getOutputStartTime(),utc.getOutputEndTime(),
					utc.getNumberOfPoints(),utc.getAlgorithm());
			copyAnnotation(utc,newUTC);
			return newUTC;
		} else if (simulation instanceof SteadyState) {
			SteadyState ss = (SteadyState)simulation;
			SteadyState newSS = new SteadyState(newId,ss.getName(),ss.getAlgorithm());
			copyAnnotation(ss,newSS);
			return newSS;
		} else if (simulation instanceof OneStep) {
			OneStep os = (OneStep)simulation;
			OneStep newOS = new OneStep(newId,os.getName(),os.getAlgorithm(),
					os.getStep());
			copyAnnotation(os,newOS);
			return newOS;
		}
		return null;
	}
	
	public static SetValue copySetValue(SetValue setValue) {
		SetValue newSetValue = new SetValue(setValue.getTargetXPath(),setValue.getMath(),setValue.getRangeReference(),
				setValue.getModelReference());
		for (Variable variable : setValue.getListOfVariables()) {
			newSetValue.addVariable(copyVariable(variable));
		}
		for (Parameter parameter : setValue.getListOfParameters()) {
			newSetValue.addParameter(copyParameter(parameter));
		}
		copyAnnotation(setValue,newSetValue);
		return newSetValue;
	}
	
	
	public static Range copyRange(Range range) {
		if (range instanceof VectorRange) {
			VectorRange vectorRange = (VectorRange)range;
			VectorRange newVectorRange = new VectorRange(vectorRange.getId());
			for (int i = 0; i < vectorRange.getNumElements(); i++) {
				newVectorRange.addValue(vectorRange.getElementAt(i));
			}
			copyAnnotation(vectorRange,newVectorRange);
			return newVectorRange;
		} else if (range instanceof UniformRange) {
			UniformRange uniformRange = (UniformRange)range;
			UniformRange newUniformRange = new UniformRange(uniformRange.getId(),uniformRange.getStart(),
					uniformRange.getEnd(),uniformRange.getNumberOfPoints(),uniformRange.getType());
			copyAnnotation(uniformRange,newUniformRange);
			return newUniformRange;
		} else if (range instanceof FunctionalRange) {
			FunctionalRange functionalRange = (FunctionalRange)range;
			FunctionalRange newFunctionalRange = new FunctionalRange(functionalRange.getId(),
					functionalRange.getRange());
			for (AbstractIdentifiableElement variable : functionalRange.getVariables().values()) {
				newFunctionalRange.addVariable(copyVariable((Variable)variable));
			}
			for (AbstractIdentifiableElement parameter : functionalRange.getParameters().values()) {
				newFunctionalRange.addParameter(copyParameter((Parameter)parameter));
			}
			copyAnnotation(functionalRange,newFunctionalRange);
			return newFunctionalRange;
		}
		return null;
	}
	
	public static SubTask copySubTask(SubTask subTask) {
		SubTask newSubTask = new SubTask(subTask.getOrder(),subTask.getTaskId());
		//copyAnnotation(subTask,newSubTask);
		return newSubTask;
	}
	
	public static AbstractTask copyTask(AbstractTask abstractTask,String newId) {
		if (abstractTask instanceof Task) {
			Task task = (Task)abstractTask;
			Task newTask = new Task(newId,task.getName(),task.getModelReference(),task.getSimulationReference());
			copyAnnotation(task,newTask);
			return newTask;
		} else if (abstractTask instanceof RepeatedTask) {
			RepeatedTask task = (RepeatedTask)abstractTask;
			RepeatedTask newTask = new RepeatedTask(newId,task.getName(),task.getResetModel(),task.getRange());
			for (SetValue setValue : task.getChanges()) {
				newTask.addChange(copySetValue(setValue));
			}
			for (Range range : task.getRanges().values()) {
				newTask.addRange(copyRange(range));
			}
			for (SubTask subTask : task.getSubTasks().values()) {
				newTask.addSubtask(copySubTask(subTask));
			}
			copyAnnotation(task,newTask);
			return newTask;
		}
		return null;
	}
	
	public static Variable copyVariable(Variable variable) {
		Variable newVariable = null;
		if (variable.isVariable()) {
			newVariable = new Variable(variable.getId(),variable.getName(),variable.getReference(),
					variable.getTarget());
		} else {
			newVariable = new Variable(variable.getId(),variable.getName(),variable.getReference(),
					variable.getSymbol());
		}
		copyAnnotation(variable,newVariable);
		return newVariable;
	}
	
	public static Parameter copyParameter(Parameter parameter) {
		Parameter newParameter = new Parameter(parameter.getId(),parameter.getName(),parameter.getValue());
		copyAnnotation(parameter,newParameter);
		return newParameter;
	}
	
	public static DataGenerator copyDataGenerator(DataGenerator dataGenerator,String newId) {
		DataGenerator newDataGenerator = new DataGenerator(newId,
				dataGenerator.getName(),dataGenerator.getMath());
		// TODO: need to update parameter/variable ids
		for (Variable variable : dataGenerator.getListOfVariables()) {
			newDataGenerator.addVariable(copyVariable(variable));
		}
		for (Parameter parameter : dataGenerator.getListOfParameters()) {
			newDataGenerator.addParameter(copyParameter(parameter));
		}
		copyAnnotation(dataGenerator,newDataGenerator);
		return newDataGenerator;
	}
	
	public static Curve copyCurve(Curve curve,String newId) {
		Curve newCurve = new Curve(newId,curve.getName(),curve.getLogX(),curve.getLogY(),
				curve.getXDataReference(),curve.getYDataReference());
		copyAnnotation(curve,newCurve);
		return newCurve;
	}
	
	public static Surface copySurface(Surface surface,String newId) {
		Surface newSurface = new Surface(newId,surface.getName(),surface.getLogX(),surface.getLogY(),
				surface.getLogZ(),surface.getXDataReference(),surface.getYDataReference(),
				surface.getZDataReference());
		copyAnnotation(surface,newSurface);
		return newSurface;
	}

	public static DataSet copyDataSet(DataSet dataSet,String newId) {
		DataSet newDataSet = new DataSet(newId,dataSet.getName(),dataSet.getLabel(),
				dataSet.getDataReference());
		copyAnnotation(dataSet,newDataSet);
		return newDataSet;
	}
	
	public static Output copyOutput(Output output,String newId) {
		if (output instanceof Plot2D) {
			Plot2D plot2d = (Plot2D) output;
			Plot2D newPlot2d = new Plot2D(newId,plot2d.getName());
			for (Curve curve : plot2d.getListOfCurves()) {
				String newCurveId = curve.getId();
				if (!plot2d.getId().equals(newId)) {
					if (newCurveId.contains("_"+plot2d.getId()+"_")) {
						newCurveId = newCurveId.replace("_"+plot2d.getId()+"_","_"+newId+"_");
					} else {
						newCurveId = newCurveId + "_" + newId;
					}
				}
				newPlot2d.addCurve(copyCurve(curve,newCurveId));
			}
			copyAnnotation(plot2d,newPlot2d);
			return newPlot2d;
		} else if (output instanceof Plot3D) {
			Plot3D plot3d = (Plot3D) output;
			Plot3D newPlot3d = new Plot3D(newId,plot3d.getName());
			for (Surface surface : plot3d.getListOfSurfaces()) {
				String newSurfaceId = surface.getId();
				if (!plot3d.getId().equals(newId)) {
					if (newSurfaceId.contains("_"+plot3d.getId()+"_")) {
						newSurfaceId = newSurfaceId.replace("_"+plot3d.getId()+"_","_"+newId+"_");
					} else {
						newSurfaceId = newSurfaceId + "_" + newId;
					}
				}
				newPlot3d.addSurface(copySurface(surface,newSurfaceId));
			}
			copyAnnotation(plot3d,newPlot3d);
			return newPlot3d;
		} else if (output instanceof Report) {
			Report report = (Report) output;
			Report newReport = new Report(newId,report.getName());
			for (DataSet dataSet : report.getListOfDataSets()) {
				String newDataSetId = dataSet.getId();
				if (!report.getId().equals(newId)) {
					if (newDataSetId.contains("_"+report.getId()+"_")) {
						newDataSetId = newDataSetId.replace("_"+report.getId()+"_","_"+newId+"_");
					} else {
						newDataSetId = newDataSetId + "_" + newId;
					}
				}
				newReport.addDataSet(copyDataSet(dataSet,newDataSetId));
			}	
			copyAnnotation(report,newReport);
			return newReport;
		}
		return null;
	}

	public static String getReactionIdFromXPathIdentifer(String xPath) {
		if (!xPath.contains("reaction[@id")) return null;
		String reactionId = xPath.replace("/sbml:sbml/sbml:model/sbml:listOfReactions/sbml:reaction[@id='", "");
		reactionId = reactionId.substring(0,reactionId.indexOf("'"));
		return reactionId;
	}
	
	public static String getSEDBaseAnnotation(SEDBase sedBase, String name, String attribute, String defaultValue)
	{
		Annotation annotation = sedBase.getAnnotation();
		if (annotation!=null) {
			List<Element> elements = annotation.getAnnotationElementsList();
			for (Element element : elements) {
				if (!element.getNamespaceURI().equals("http://www.async.ece.utah.edu/iBioSim")) continue;
				if (element.getName().equals(name)) {
					if (element.getAttributeValue(attribute)!=null) {
						return element.getAttributeValue(attribute);
					} else {
						return defaultValue;
					}
				}
			}
		}
		return defaultValue;
	}

}
