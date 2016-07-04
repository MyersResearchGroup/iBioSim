package analysis.main;

import java.util.List;

import org.jdom.Element;
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
import org.jlibsedml.Model;
import org.jlibsedml.OneStep;
import org.jlibsedml.Output;
import org.jlibsedml.Parameter;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Range;
import org.jlibsedml.RemoveXML;
import org.jlibsedml.RepeatedTask;
import org.jlibsedml.Report;
import org.jlibsedml.SEDBase;
import org.jlibsedml.SetValue;
import org.jlibsedml.Simulation;
import org.jlibsedml.SteadyState;
import org.jlibsedml.SubTask;
import org.jlibsedml.Task;
import org.jlibsedml.UniformRange;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VectorRange;

public class SEDMLutilities {
	
	public static void copyAnnotation(SEDBase sedBase1,SEDBase sedBase2) {
		List<Annotation> annotations = sedBase1.getAnnotation();
		for (int i = 0; i < annotations.size(); i++)
		{	
			Annotation annotation = annotations.get(0);
			List<Element> elements = annotation.getAnnotationElementsList();
			for (Element element : elements) {
				if (!element.getName().equals("sbsi-editor")) {
					Annotation newAnnotation = new Annotation((Element)element.clone());
					sedBase2.addAnnotation(newAnnotation);
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
		// TODO: need to handle Plot3D
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
		List<Annotation> annotations = sedBase.getAnnotation();
		for (int i = 0; i < annotations.size(); i++)
		{
			Annotation annotation = annotations.get(i);
			List<Element> elements = annotation.getAnnotationElementsList();
			for (Element element : elements) {
				// TODO: check namespace
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
