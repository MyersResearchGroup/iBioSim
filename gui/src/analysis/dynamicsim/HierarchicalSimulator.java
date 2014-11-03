package analysis.dynamicsim;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;



import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;

import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.ReplacedBy;
//import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
//import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import main.Gui;
import odk.lang.FastMath;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;

public abstract class HierarchicalSimulator {

	// Top Level Module
	protected ModelState topmodel; 
	// Submodel states
	protected HashMap<String, ModelState> submodels; 
	// Associates species, parameters with a value
	protected HashMap<String, Double> replacements; 
	// Initial replacement state for multiple runs
	protected HashMap<String, Double> initReplacementState;
	// Keeps track which model contains the replacing values
	// Number of submodel states
	protected int numSubmodels;
	// Total propensity including all model states
	protected boolean isGrid = false;
	//SBML Info.
	final protected int SBML_LEVEL = 3;
	final protected int SBML_VERSION = 1;
	//generates random numbers based on the xorshift method
	protected XORShiftRandom randomNumberGenerator = null;
	//file writing variables
	protected FileWriter TSDWriter = null;
	protected BufferedWriter bufferedTSDWriter = null;
	//compares two events based on fire time and priority
	protected EventComparator eventComparator = new EventComparator();
	//boolean flags
	protected boolean cancelFlag = false;
	protected boolean constraintFailureFlag = false;
	protected boolean sbmlHasErrorsFlag = false;
	protected boolean constraintFlag = true;
	// Simulation info
	protected double currentTime;
	protected String SBMLFileName;
	protected double timeLimit;
	protected double maxTimeStep;
	protected double minTimeStep;
	protected JProgressBar progress;
	protected double printInterval;
	protected int currentRun;
	protected String rootDirectory;
	protected String outputDirectory;
	protected String separator;
	protected boolean printConcentrations = false;
	protected HashSet<String> printConcentrationSpecies = new HashSet<String>();
	protected JFrame running = new JFrame();
	protected String[] interestingSpecies;
	PsRandom prng = new PsRandom();
	// Helper fields
	protected boolean stoichAmpBoolean = false;
	protected double stoichAmpGridValue = 1.0;
	// 
	private HashMap<String, Model> models;
	protected ArrayList<String> filesCreated = new ArrayList<String>();

	/**
	 * does lots of initialization
	 */
	public HierarchicalSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, 
			JFrame running, String[] interestingSpecies, String quantityType) throws IOException, XMLStreamException 
			{

		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.rootDirectory = rootDirectory;
		this.outputDirectory = outputDirectory;
		this.running = running;

		replacements = new HashMap<String,Double>();
		initReplacementState = new HashMap<String, Double>();
		models = new HashMap<String, Model>();

		this.interestingSpecies = interestingSpecies;
		if (quantityType != null)
		{
			String[] printConcentration = quantityType.replaceAll(" ", "").split(",");

			for(String s : printConcentration)
				printConcentrationSpecies.add(s);
		}

		if (stoichAmpValue <= 1.0)
			stoichAmpBoolean = false;
		else {
			stoichAmpBoolean = true;
			stoichAmpGridValue = stoichAmpValue;
		}

		SBMLDocument document = SBMLReader.read(new File(SBMLFileName));

		SBMLErrorLog errors = document.getErrorLog();

		//if the sbml document has errors, tell the user and don't simulate
		if (document.getErrorCount() > 0) 
		{	
			String errorString = "";

			for (int i = 0; i < errors.getErrorCount(); i++) {
				errorString += errors.getError(i);
			}

			JOptionPane.showMessageDialog(Gui.frame, 
					"The SBML file contains " + document.getErrorCount() + " error(s):\n" + errorString,
					"SBML Error", JOptionPane.ERROR_MESSAGE);

			sbmlHasErrorsFlag = true;
		}


		if (File.separator.equals("\\")) 
		{
			separator = "\\\\";
		}
		else 
		{
			separator = File.separator;
		}

		isGrid = checkGrid(document.getModel());
		models.put(document.getModel().getId(), document.getModel());
		topmodel = new ModelState(document.getModel().getId(), "topmodel");

		numSubmodels = (int)setupSubmodels(document);
		getComponentPortMap(document);
			
			}

	/**
	 * abstract simulate method
	 * each simulator needs a simulate method
	 */
	protected abstract void simulate();

	/**
	 * cancels the current run
	 */
	protected abstract void cancel();

	/**
	 * clears data structures for new run
	 */
	protected abstract void clear();

	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	protected abstract void setupForNewRun(int newRun);

	/**
	 * Get path to submodels xml files
	 */
	protected  String getPath(String path)
	{

		StringBuilder temp = new StringBuilder();
		String[] subPaths = path.split(separator);

		temp.append(subPaths[0]);

		for(int i = 1; i < subPaths.length-1;i++)
		{
			temp.append(separator + subPaths[i]);

		}

		return temp.toString();
	}



	private static boolean checkGrid(Model model)
	{
		if(model.getCompartment("Grid") != null)
			return true;
		return false;
	}

	protected void printAllToTSD(double printTime) throws IOException 
	{


		String commaSpace = "";

		bufferedTSDWriter.write("(");

		commaSpace = "";

		//print the current time
		bufferedTSDWriter.write(printTime + ",");

		//loop through the speciesIDs and print their current value to the file
		for (String speciesID : topmodel.speciesIDSet)
		{	

			bufferedTSDWriter.write(commaSpace + topmodel.getVariableToValue(speciesID));
			commaSpace = ",";


		}

		for (String noConstantParam : topmodel.variablesToPrint)
		{

			bufferedTSDWriter.write(commaSpace + topmodel.getVariableToValue(noConstantParam));
			commaSpace = ",";

		}

		for (ModelState models : submodels.values())
		{
			for (String speciesID : models.speciesIDSet)
			{	
				if(!models.isHierarchical.contains(speciesID))
				{
					bufferedTSDWriter.write(commaSpace + models.getVariableToValue(speciesID));
					commaSpace = ",";
				}
			}

			for (String noConstantParam : models.variablesToPrint)
			{	
				if(!models.isHierarchical.contains(noConstantParam))
				{
					bufferedTSDWriter.write(commaSpace + models.getVariableToValue(noConstantParam));
					commaSpace = ",";
				}
			}
		}

		bufferedTSDWriter.write(")");
		bufferedTSDWriter.flush();
	}

	protected void printInterestingToTSD(double printTime) throws IOException 
	{


		String commaSpace = "";

		bufferedTSDWriter.write("(");

		commaSpace = "";

		//print the current time
		bufferedTSDWriter.write(printTime + ",");

		double temp;
		//loop through the speciesIDs and print their current value to the file

		for(String s : interestingSpecies)
		{
			String id = s.replaceAll("__[\\w]+", "");
			String element = s.replace(id+"__", "");
			ModelState ms = (id.equals(s))?topmodel:submodels.get(id);
			if(printConcentrationSpecies.contains(s))
			{
				temp = ms.getVariableToValue(ms.speciesToCompartmentNameMap.get(element));
				bufferedTSDWriter.write(commaSpace + ms.getVariableToValue(element)/temp);
				commaSpace = ",";
			}
			else
			{
				bufferedTSDWriter.write(commaSpace + ms.getVariableToValue(element));
				commaSpace = ",";
			}
		}


		bufferedTSDWriter.write(")");
		bufferedTSDWriter.flush();
	}


	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException 
	 */
	protected void printToTSD(double printTime) throws IOException 
	{
		if(interestingSpecies.length == 0)
			printAllToTSD(printTime);
		else
			printInterestingToTSD(printTime);


	}
	/**
	 * opens output file and seeds rng for new run
	 * 
	 * @param randomSeed
	 * @param currentRun
	 * @throws IOException
	 */
	protected void setupForOutput(long randomSeed, int currentRun) {

		this.currentRun = currentRun;

		randomNumberGenerator = new XORShiftRandom(randomSeed);

		try {

			String extension = ".tsd";

			TSDWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(TSDWriter);
			bufferedTSDWriter.write('(');

			if (currentRun > 1) {

				bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private static boolean checkFileExists(String filename)
	{
		return new File(filename).exists();
	}

	protected void deleteFiles()
	{
		for(String f : filesCreated)
		{
			File file = new File(f);

			if(file.exists())
				file.delete();
		}

		filesCreated.clear();
	}
	/**
	 * Initializes the modelstate array
	 */
	protected long setupSubmodels(SBMLDocument document)
	{
		String path = rootDirectory;
		String alternativePath = getPath(SBMLFileName);
		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getExtension(CompConstants.namespaceURI);
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getExtension(CompConstants.namespaceURI);

		if(sbmlCompModel == null)
		{
			submodels = new HashMap<String, ModelState>(0);
			return 0;
		}

		submodels = new HashMap<String, ModelState>(sbmlCompModel.getListOfSubmodels().size());


		int index = 0;
		//HierarchicalUtilities.extractModelDefinitions(sbmlComp, sbmlCompModel, alternativePath, separator);
		for (Submodel submodel : sbmlCompModel.getListOfSubmodels()) {


			if(!models.containsKey(submodel.getModelRef()))
			{

				String filename = path+separator+submodel.getModelRef()+".xml";

				if(sbmlComp.getListOfExternalModelDefinitions() != null &&
						sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
				{
					SBMLDocument extDoc = null;
					try {
						if(checkFileExists(alternativePath+separator+sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource()))
						{
							extDoc = SBMLReader.read(new File(alternativePath+separator+sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource()));

						}
						else
						{
							extDoc = SBMLReader.read(new File(filename));
						}

					}
					catch (XMLStreamException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(extDoc.getModel());
					CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(extDoc);

					for (Submodel sub : documentCompModel.getListOfSubmodels())
						extractModelDefintion(path,  document,  sub,  sbmlComp);

					ArrayList<String> comps = new ArrayList<String>();

					for (int j=0; j < documentCompModel.getListOfSubmodels().size(); j++) {
						String subModelType = documentCompModel.getListOfSubmodels().get(j).getModelRef();
						if (!comps.contains(subModelType)) {
							ExternalModelDefinition extModel = documentComp.createExternalModelDefinition();
							extModel.setId(subModelType);
							extModel.setSource("file:" + subModelType + ".xml");
							comps.add(subModelType);
						}
					}
					while (documentComp.getListOfModelDefinitions().size() > 0) {
						documentComp.removeModelDefinition(0);
					}
					for (int i = 0; i < documentComp.getListOfExternalModelDefinitions().size(); i++) {
						ExternalModelDefinition extModel = documentComp.getListOfExternalModelDefinitions().get(i);
						if (extModel.isSetModelRef()) {
							String oldId = extModel.getId();
							extModel.setSource("file:" + extModel.getModelRef() + ".xml");
							extModel.setId(extModel.getModelRef());
							extModel.unsetModelRef();
							for (int j=0; j < sbmlCompModel.getListOfSubmodels().size(); j++) {
								Submodel sub = sbmlCompModel.getListOfSubmodels().get(j);
								if (sub.getModelRef().equals(oldId)) {
									sub.setModelRef(extModel.getId());
								}
							}
						}
					}
					SBMLWriter writer = new SBMLWriter();

					//updateReplacementsDeletions(extDoc, documentComp, documentCompModel, path);
					filename = path+separator+submodel.getModelRef()+"_new.xml";
					try {
						writer.writeSBMLToFile(extDoc, filename);
						filesCreated.add(filename);

					} catch (SBMLException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
				}
				else if(sbmlComp.getListOfModelDefinitions() != null &&
						sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
				{
					//extractModelDefinitions(path, document, submodel, sbmlComp, sbmlCompModel);
					extractModelDefintion(path,  document,  submodel,  sbmlComp);
				}

				Model flattenModel = HierarchicalUtilities.flattenModel(path, filename);

				models.put(submodel.getModelRef(), flattenModel);

				filesCreated.add(filename.replace(".xml", "__temp.xml"));
			}

			if(isGrid)
			{
				//String annotation = submodel.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim();
				//int copies = getArraySize(annotation);

				//int copies = biomodel.annotation.AnnotationUtility.parseArraySizeAnnotation(submodel);

				String[] ids = biomodel.annotation.AnnotationUtility.parseArrayAnnotation(document.getModel().getParameter(submodel.getModelRef()+ "__locations"));
				for(String s : ids)
				{
					if(s.isEmpty())
						continue;
					String getID = s.replaceAll("[=].*", "");
					submodels.put(getID, new ModelState(submodel.getModelRef(), getID));

				}

				/*LinkedList<String> ids = getArrayIDs(document.getModel().getParameter(submodel.getModelRef()+ "__locations").getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim());
				for(int i = 0; i < copies; i++)
				{
					submodels.put(ids.getFirst(), new ModelState(submodel.getModelRef(), ids.getFirst()));
					ids.removeFirst();
					index++;
				}*/
			}
			else
			{
				ModelState modelstate =  new ModelState(submodel.getModelRef(), submodel.getId());
				submodels.put(submodel.getId(), modelstate);
				performDeletions(modelstate, submodel);
				index++;
			}

		}


		//updateReplacementsDeletions(path, document, sbmlComp, sbmlCompModel);
		return index;
	}

	private void extractModelDefintion(String path, SBMLDocument document, Submodel submodel, CompSBMLDocumentPlugin sbmlComp)
	{
		ModelDefinition md = sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef());

		if(md == null)
			return;

		String extId = md.getId();
		org.sbml.jsbml.Model model = new org.sbml.jsbml.Model(md);

		model.getDeclaredNamespaces().clear();

		SBMLDocument newDoc = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);

		newDoc.setModel(model);
		newDoc.enablePackage(LayoutConstants.namespaceURI);
		newDoc.enablePackage(CompConstants.namespaceURI);
		newDoc.enablePackage(FBCConstants.namespaceURI);

		newDoc.enablePackage(ArraysConstants.namespaceURI);

		CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(newDoc);
		CompModelPlugin documentCompModel = SBMLutilities.getCompModelPlugin(model);

		model.unsetNamespace();
		newDoc.setModel(model);

		ArrayList<String> comps = new ArrayList<String>();
		for (int j=0; j < documentCompModel.getListOfSubmodels().size(); j++) {
			String subModelType = documentCompModel.getListOfSubmodels().get(j).getModelRef();
			if (!comps.contains(subModelType)) {
				ExternalModelDefinition extModel = documentComp.createExternalModelDefinition();
				extModel.setId(subModelType);
				extModel.setSource("file:" + subModelType + ".xml");
				comps.add(subModelType);
			}
		}
		// Make compartment enclosing
		if (document.getModel().getCompartmentCount()==0) {
			Compartment c = document.getModel().createCompartment();
			c.setId("default");
			c.setSize(1);
			c.setSpatialDimensions(3);
			c.setConstant(true);
		}
		//updateReplacementsDeletions(path, document, documentComp, documentCompModel);
		SBMLWriter writer = new SBMLWriter();

		try {
			writer.writeSBMLToFile(newDoc, path + separator + extId + ".xml");
			filesCreated.add(path + separator + extId + ".xml");

		} catch (SBMLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}


	}
	
	/**
	 * Stores replacing values in a global map
	 */
	private void setupSpeciesReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (int i = 0; i < topmodel.numSpecies; i++) {
			Species species = sbml.getModel().getSpecies(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getExtension(CompConstants.namespaceURI);

			String s = species.getId();
			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					double initVal = 0;
					if(species.isSetInitialAmount())
						initVal = species.getInitialAmount();
					else if(species.isSetInitialConcentration()){
						Compartment comp = sbml.getModel().getCompartment(species.getCompartment());
						initVal = species.getInitialConcentration() * comp.getValue();

					}

					replacements.put(s, initVal);	
					initReplacementState.put(s, initVal);


					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetIdRef())
						{
							String subSpecies = element.getIdRef();
							topmodel.isHierarchical.add(s);
							topmodel.replacementDependency.put(s, s);
							getModel(submodel).isHierarchical.add(subSpecies);
							getModel(submodel).replacementDependency.put(subSpecies, s);
						}
						else if(element.isSetPortRef())
						{
							Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());

							String subSpecies = port.getIdRef();


							topmodel.isHierarchical.add(s);
							topmodel.replacementDependency.put(s, s);
							getModel(submodel).isHierarchical.add(subSpecies);
							getModel(submodel).replacementDependency.put(subSpecies, s);
						}
						else
						{
							continue;
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					String submodel = replacement.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
					if(replacement.isSetIdRef())
					{
						String subSpecies = replacement.getIdRef();
						ModelState temp = getModel(submodel);
						if(replacement.isSetSBaseRef())
						{
							subSpecies = subSpecies + "__" + replacement.getSBaseRef().getIdRef();
						}
						double initVal = 0;

						Species subSpeciesRef = models.get(temp.model).getSpecies(replacement.getSBaseRef().getIdRef());
						if(subSpeciesRef.isSetInitialAmount())
							initVal = subSpeciesRef.getInitialAmount();
						else if(subSpeciesRef.isSetInitialConcentration())
							initVal = subSpeciesRef.getInitialConcentration() * models.get(temp.model).getCompartment(subSpeciesRef.getCompartment()).getSize();
						replacements.put(s,initVal);
						initReplacementState.put(s, initVal);
						topmodel.isHierarchical.add(s);
						topmodel.replacementDependency.put(s, s);
						getModel(submodel).isHierarchical.add(subSpecies);
						getModel(submodel).replacementDependency.put(subSpecies, s);
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subSpecies = port.getIdRef();
						ModelState temp = getModel(submodel);
						Species subSpeciesRef = models.get(temp.model).getModel().getSpecies(subSpecies);
						double initVal = 0;
						if(subSpeciesRef.isSetInitialAmount())
							initVal = subSpeciesRef.getInitialAmount();
						else if(subSpeciesRef.isSetInitialConcentration())
							initVal = subSpeciesRef.getInitialConcentration() * models.get(temp.model).getCompartment(subSpeciesRef.getCompartment()).getSize();
						replacements.put(s, initVal);
						initReplacementState.put(s, initVal);
						topmodel.isHierarchical.add(s);
						topmodel.replacementDependency.put(s, s);
						getModel(submodel).isHierarchical.add(subSpecies);
						getModel(submodel).replacementDependency.put(subSpecies, s);
					}
					else
					{
						continue;
					}
				}
			}
		}
	}
	
	private void setupParameterReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (int i = 0; i < topmodel.numParameters; i++) {
			Parameter parameter = sbml.getModel().getParameter(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)parameter.getExtension(CompConstants.namespaceURI);

			String p = parameter.getId();
			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					replacements.put(p, parameter.getValue());	
					initReplacementState.put(p, parameter.getValue());


					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetIdRef())
						{
							String subParameter = element.getIdRef();
							topmodel.isHierarchical.add(p);
							topmodel.replacementDependency.put(p, p);
							getModel(submodel).isHierarchical.add(subParameter);
							getModel(submodel).replacementDependency.put(subParameter, p);
						}
						if(element.isSetMetaIdRef())
						{
							String subParameter = element.getMetaIdRef();
							topmodel.isHierarchical.add(p);
							topmodel.replacementDependency.put(p, p);
							getModel(submodel).isHierarchical.add(subParameter);
							getModel(submodel).replacementDependency.put(subParameter, p);
						}
						else if(element.isSetPortRef())
						{
							Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
							String subParameter = port.getIdRef();
							if(port.isSetMetaIdRef() && subParameter.length() == 0)
								subParameter = port.getMetaIdRef();
							topmodel.isHierarchical.add(p);
							topmodel.replacementDependency.put(p, p);
							getModel(submodel).isHierarchical.add(subParameter);
							getModel(submodel).replacementDependency.put(subParameter, p);
						}
						else
						{
							continue;
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					String submodel = replacement.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
					if(replacement.isSetIdRef())
					{
						String subParameter = replacement.getIdRef();
						ModelState temp = getModel(submodel);
						Model sub = models.get(temp.model).getModel();
						replacements.put(p, sub.getParameter(subParameter).getValue());
						initReplacementState.put(p, models.get(temp.model).getModel().getParameter(subParameter).getValue());
						topmodel.isHierarchical.add(p);
						topmodel.replacementDependency.put(p, p);
						getModel(submodel).isHierarchical.add(subParameter);
						getModel(submodel).replacementDependency.put(subParameter, p);
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subParameter = port.getIdRef();
						ModelState temp = getModel(submodel);
						replacements.put(p, models.get(temp.model).getModel().getParameter(subParameter).getValue());
						initReplacementState.put(p, models.get(temp.model).getParameter(subParameter).getValue());
						topmodel.isHierarchical.add(p);
						topmodel.replacementDependency.put(p, p);
						getModel(submodel).isHierarchical.add(subParameter);
						getModel(submodel).replacementDependency.put(subParameter, p);
					}
					else
					{
						continue;
					}
				}
			}
		}
	}



	private void setupCompartmentReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{
		for (int i = 0; i < topmodel.numCompartments; i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getExtension(CompConstants.namespaceURI);

			String c = compartment.getId();
			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					replacements.put(c, compartment.getSize());	
					initReplacementState.put(c, compartment.getSize());


					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetIdRef())
						{
							String subCompartment = element.getIdRef();
							if(element.isSetSBaseRef())
							{
								subCompartment = subCompartment + "__" + element.getSBaseRef().getIdRef();
							}
							topmodel.isHierarchical.add(c);
							topmodel.replacementDependency.put(c, c);
							getModel(submodel).isHierarchical.add(subCompartment);
							getModel(submodel).replacementDependency.put(subCompartment, c);
						}
						else if(element.isSetPortRef())
						{
							Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
							String subCompartment = port.getIdRef();

							topmodel.isHierarchical.add(c);
							topmodel.replacementDependency.put(c, c);
							getModel(submodel).isHierarchical.add(subCompartment);
							getModel(submodel).replacementDependency.put(subCompartment, c);
						}
						else
						{
							continue;
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					String submodel = replacement.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
					if(replacement.isSetIdRef())
					{
						String subCompartment = replacement.getIdRef();
						ModelState temp = getModel(submodel);
						replacements.put(c, models.get(temp.model).getModel().getCompartment(subCompartment).getSize());
						initReplacementState.put(c, models.get(temp.model).getModel().getCompartment(subCompartment).getSize());
						topmodel.isHierarchical.add(c);
						topmodel.replacementDependency.put(c, c);
						getModel(submodel).isHierarchical.add(subCompartment);
						getModel(submodel).replacementDependency.put(subCompartment, c);
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						ModelState temp = getModel(submodel);
						replacements.put(c, models.get(temp.model).getModel().getCompartment(subCompartment).getSize());
						initReplacementState.put(c, models.get(temp.model).getCompartment(subCompartment).getSize());
						topmodel.isHierarchical.add(c);
						topmodel.replacementDependency.put(c, c);
						getModel(submodel).isHierarchical.add(subCompartment);
						getModel(submodel).replacementDependency.put(subCompartment, c);
					}
					else
					{
						continue;
					}
				}
			}
		}
	}

	private void setupReactionReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (int i = 0; i < topmodel.numReactions; i++) {
			Reaction reaction = sbml.getModel().getReaction(i);
			//setupLocalParameterReplacement(reaction.getKineticLaw(), sbml, sbmlCompModel);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)reaction.getExtension(CompConstants.namespaceURI);

			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						ModelState modelstate = submodels.get(submodel);
						CompModelPlugin subCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetPortRef())
						{
							Port port = subCompModel.getListOfPorts().get(element.getPortRef());
							String subRule = port.getMetaIdRef();
							modelstate.deletedElementsByMetaId.add(subRule);
						}
						else if(element.isSetMetaIdRef())
						{
							modelstate.deletedElementsByMetaId.add(element.getMetaIdRef());
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if(replacement.isSetIdRef())
					{
						topmodel.deletedElementsById.add(replacement.getIdRef());
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						topmodel.deletedElementsById.add(subCompartment);
					}
					else if(replacement.isSetMetaIdRef())
					{
						topmodel.deletedElementsByMetaId.add(replacement.getMetaIdRef());
					}	

				}
			}
		}
	}

	private void setupConstraintReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (int i = 0; i < topmodel.numConstraints; i++) {
			Constraint constraint = sbml.getModel().getConstraint(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)constraint.getExtension(CompConstants.namespaceURI);

			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						ModelState modelstate = submodels.get(submodel);
						CompModelPlugin subCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetPortRef())
						{
							Port port = subCompModel.getListOfPorts().get(element.getPortRef());
							String subRule = port.getMetaIdRef();
							modelstate.deletedElementsByMetaId.add(subRule);
						}
						else if(element.isSetMetaIdRef())
						{
							modelstate.deletedElementsByMetaId.add(element.getMetaIdRef());
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if(replacement.isSetIdRef())
					{
						topmodel.deletedElementsById.add(replacement.getIdRef());
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						topmodel.deletedElementsById.add(subCompartment);
					}
					else if(replacement.isSetMetaIdRef())
					{
						topmodel.deletedElementsByMetaId.add(replacement.getMetaIdRef());
					}	

				}
			}
		}
	}

	private void setupEventReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (int i = 0; i < topmodel.numEvents; i++) {
			Event event = sbml.getModel().getEvent(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)event.getExtension(CompConstants.namespaceURI);

			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						ModelState modelstate = submodels.get(submodel);
						CompModelPlugin subCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						if(element.isSetPortRef())
						{
							Port port = subCompModel.getListOfPorts().get(element.getPortRef());
							String subRule = port.getMetaIdRef();
							modelstate.deletedElementsByMetaId.add(subRule);
						}
						else if(element.isSetMetaIdRef())
						{
							modelstate.deletedElementsByMetaId.add(element.getMetaIdRef());
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if(replacement.isSetIdRef())
					{
						topmodel.deletedElementsById.add(replacement.getIdRef());
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						topmodel.deletedElementsById.add(subCompartment);
					}
					else if(replacement.isSetMetaIdRef())
					{
						topmodel.deletedElementsByMetaId.add(replacement.getMetaIdRef());
					}	

				}
			}
		}
	}

	private void setupRuleReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (Rule rule : sbml.getModel().getListOfRules()) {

			CompSBasePlugin sbmlSBase = (CompSBasePlugin)rule.getExtension(CompConstants.namespaceURI);

			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						ModelState modelstate = submodels.get(submodel);
						CompModelPlugin subCompModel = (CompModelPlugin)models.get(submodels.get(submodel).model).getExtension(CompConstants.namespaceURI);
						//						modelstate.deletedElementsByMetaId.add(rule.getMetaId());
						if(element.isSetPortRef())
						{
							Port port = subCompModel.getListOfPorts().get(element.getPortRef());
							String subRule = port.getMetaIdRef();
							modelstate.deletedElementsByMetaId.add(subRule);
						}
						else if(element.isSetMetaIdRef())
						{
							modelstate.deletedElementsByMetaId.add(element.getMetaIdRef());
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if(replacement.isSetIdRef())
					{
						topmodel.deletedElementsById.add(replacement.getIdRef());
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						topmodel.deletedElementsById.add(subCompartment);
					}
					else if(replacement.isSetMetaIdRef())
					{
						topmodel.deletedElementsByMetaId.add(replacement.getMetaIdRef());
					}	

				}
			}
		}
	}

	private void setupInitAssignmentReplacement(SBMLDocument sbml, CompModelPlugin sbmlCompModel)
	{

		for (InitialAssignment init : sbml.getModel().getListOfInitialAssignments()) {

			CompSBasePlugin sbmlSBase = (CompSBasePlugin)init.getExtension(CompConstants.namespaceURI);

			if(sbmlSBase != null)
			{
				if(sbmlSBase.getListOfReplacedElements() != null)
				{
					for(ReplacedElement element: sbmlSBase.getListOfReplacedElements())
					{
						String submodel = element.getSubmodelRef();
						ModelState modelstate = submodels.get(submodel);
						if(element.isSetIdRef())
						{
							modelstate.deletedElementsById.add(element.getIdRef());
						}
						else if(element.isSetPortRef())
						{
							Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
							String subCompartment = port.getIdRef();
							modelstate.deletedElementsById.add(subCompartment);
						}
						else if(element.isSetMetaIdRef())
						{
							modelstate.deletedElementsByMetaId.add(element.getMetaIdRef());
						}
					}
				}


				if(sbmlSBase.isSetReplacedBy())
				{
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if(replacement.isSetIdRef())
					{
						topmodel.deletedElementsById.add(replacement.getIdRef());
					}
					else if(replacement.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
						String subCompartment = port.getIdRef();
						topmodel.deletedElementsById.add(subCompartment);
					}
					else if(replacement.isSetMetaIdRef())
					{
						topmodel.deletedElementsByMetaId.add(replacement.getMetaIdRef());
					}	

				}
			}
		}
	}


	/**
	 * 
	 * @param sbml
	 */
	protected void getComponentPortMap(SBMLDocument sbml) 
	{
		CompModelPlugin sbmlCompModel = (CompModelPlugin)sbml.getModel().getExtension(CompConstants.namespaceURI);
		setupSpeciesReplacement(sbml, sbmlCompModel);	
		setupCompartmentReplacement(sbml, sbmlCompModel);	
		setupParameterReplacement(sbml, sbmlCompModel);
		setupReactionReplacement(sbml, sbmlCompModel); 
		setupConstraintReplacement(sbml, sbmlCompModel);
		setupEventReplacement(sbml, sbmlCompModel);
		setupRuleReplacement(sbml, sbmlCompModel);
		setupInitAssignmentReplacement(sbml, sbmlCompModel);
	}




	/**
	 * Gets state from index
	 */
	private ModelState getModel(String id)
	{
		if(id.equals("topmodel"))
			return topmodel;
		return submodels.get(id);
	}

	/**
	 * Returns the total propensity of all model states.
	 */
	protected double getTotalPropensity()
	{
		double totalPropensity = 0;
		totalPropensity += topmodel.propensity;

		for(ModelState model : submodels.values())
		{
			totalPropensity += model.propensity;
		}

		return totalPropensity;
	}


	/**
	 * Perform deletion on comp model
	 */
	private void performDeletions(ModelState modelstate, Submodel instance) {


		if (instance == null)
			return;

		for(Deletion deletion : instance.getListOfDeletions()){

			if (deletion.isSetPortRef()) 
			{
				ListOf<Port> ports = ((CompModelPlugin) models.get(modelstate.model).getExtension(CompConstants.namespaceURI)).getListOfPorts();
				Port port = ports.get(deletion.getPortRef());
				if (port!=null) 
				{
					if (port.isSetIdRef())
					{
						modelstate.deletedElementsById.add(port.getIdRef());
					}
					else if (port.isSetMetaIdRef()) 
					{
						modelstate.deletedElementsByMetaId.add(port.getMetaIdRef());
					}
					else if (port.isSetUnitRef())
					{
						modelstate.deletedElementsByUId.add(port.getIdRef());
					}
				}

			}
			else if (deletion.isSetIdRef()) {
				modelstate.deletedElementsById.add(deletion.getIdRef());
			}
			else if (deletion.isSetMetaIdRef()) 
			{
				modelstate.deletedElementsByMetaId.add(deletion.getMetaIdRef());
			}
			else if (deletion.isSetUnitRef()) 
			{
				modelstate.deletedElementsByUId.add(deletion.getUnitRef());
			}
		}
	}


	/**
	 * calculates an expression using a recursive algorithm
	 * 
	 * @param node the AST with the formula
	 * @return the evaluated expression
	 */
	protected double evaluateExpressionRecursive(ModelState modelstate, ASTNode node) {
		if (node.isBoolean()) {

			switch (node.getType()) {

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getLeftChild()))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = (node.getChildCount()==0) ? false:
					getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(0)));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter)));

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) == evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) != evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GEQ:
			{
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) >= evaluateExpressionRecursive(modelstate, node.getRightChild()));
			}
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) <= evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) > evaluateExpressionRecursive(modelstate, node.getRightChild()));

			case RELATIONAL_LT:
			{
				return getDoubleFromBoolean(
						evaluateExpressionRecursive(modelstate, node.getLeftChild()) < evaluateExpressionRecursive(modelstate, node.getRightChild()));			
			}

			default:
				return 0.0;

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;


			default:
				return 0.0;
			}
		}
		else if (node.isInteger())
			return node.getInteger();

		//if it's a number
		else if (node.isReal())
			return node.getReal();

		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {

			String name = node.getName().replace("_negative_","-");

			if (node.getType()== org.sbml.jsbml.ASTNode.Type.NAME_TIME) {

				return currentTime;
			}
			//if it's a reaction id return the propensity
			else if (modelstate.reactionToPropensityMap.keySet().contains(node.getName())) {
				return modelstate.reactionToPropensityMap.get(node.getName());
			}
			else {

				double value;

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(name) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(name) == false) {
					//value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
					value = (modelstate.getVariableToValue(name) / modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(name)));
				}
				else	
				{	
					value = modelstate.getVariableToValue(name);
				}
				return value;
			}
		}

		//operators/functions with two children
		else {

			//ASTNode leftChild = node.getLeftChild();
			//ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); childIter++)
					sum += evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case MINUS: {
				ASTNode leftChild = node.getLeftChild();
				double sum = evaluateExpressionRecursive(modelstate, leftChild);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateExpressionRecursive(modelstate, node.getChild(childIter));					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateExpressionRecursive(modelstate, node.getChild(childIter));

				return product;
			}

			case DIVIDE:
			{
				ASTNode leftChild = node.getLeftChild();
				ASTNode rightChild = node.getRightChild();

				return (evaluateExpressionRecursive(modelstate, leftChild) / evaluateExpressionRecursive(modelstate, rightChild));

			}
			case FUNCTION_POWER:
			{
				ASTNode leftChild = node.getLeftChild();
				ASTNode rightChild = node.getRightChild();

				return (FastMath.pow(evaluateExpressionRecursive(modelstate, leftChild), evaluateExpressionRecursive(modelstate, rightChild)));
			}
			case FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();

				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {

					double leftChildValue = evaluateExpressionRecursive(modelstate, node.getLeftChild());
					double rightChildValue = evaluateExpressionRecursive(modelstate, node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential")) {

					return prng.nextExponential(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("gamma")) {

					return prng.nextGamma(1, evaluateExpressionRecursive(modelstate, node.getLeftChild()), 
							evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("chisq")) {

					return prng.nextChiSquare((int) evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("lognormal")) {

					return prng.nextLogNormal(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 
							evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("laplace")) {

					//function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy")) {

					return prng.nextLorentzian(0, evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("poisson")) {

					return prng.nextPoissonian(evaluateExpressionRecursive(modelstate, node.getLeftChild()));
				}
				else if (nodeName.equals("binomial")) {

					return prng.nextBinomial(evaluateExpressionRecursive(modelstate, node.getLeftChild()),
							(int) evaluateExpressionRecursive(modelstate, node.getRightChild()));
				}
				else if (nodeName.equals("bernoulli")) {

					return prng.nextBinomial(evaluateExpressionRecursive(modelstate, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("normal")) {

					return prng.nextGaussian(evaluateExpressionRecursive(modelstate, node.getLeftChild()),
							evaluateExpressionRecursive(modelstate, node.getRightChild()));	
				}


				break;
			}

			case FUNCTION_ABS:
				return FastMath.abs(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpressionRecursive(modelstate, node.getChild(0)));				

			case FUNCTION_COS:
				return FastMath.cos(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COSH:
				return FastMath.cosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_EXP:
				return FastMath.exp(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_LN:
				return FastMath.log(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_LOG:
				double base = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(0)));
				double var = FastMath.log10(evaluateExpressionRecursive(modelstate, node.getChild(1)));
				return var/base;


			case FUNCTION_SIN:

				return FastMath.sin(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_SINH:
				return FastMath.sinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_TAN:
				return FastMath.tan(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_TANH:		
				return FastMath.tanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_PIECEWISE: {

				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3) {

					if ((childIter + 1) < node.getChildCount() && 
							getBooleanFromDouble(evaluateExpressionRecursive(modelstate, node.getChild(childIter + 1)))) {
						return evaluateExpressionRecursive(modelstate, node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getChildCount()) {
						return evaluateExpressionRecursive(modelstate, node.getChild(childIter + 2));
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpressionRecursive(modelstate, node.getRightChild()), 
						1 / evaluateExpressionRecursive(modelstate, node.getLeftChild()));

			case FUNCTION_SEC:
				return Fmath.sec(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_SECH:
				return Fmath.sech(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COT:
				return Fmath.cot(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_COTH:
				return Fmath.coth(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CSC:
				return Fmath.csc(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_CSCH:
				return Fmath.csch(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCCSCH:
			{
				double x = evaluateExpressionRecursive(modelstate, node.getChild(0));
				return FastMath.log(1/x + FastMath.sqrt(1 + 1/(x*x)));
			}
			//return Fmath.acsch(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			case FUNCTION_ARCSECH:
			{
				double x = evaluateExpressionRecursive(modelstate, node.getChild(0));
				return FastMath.log((1 + FastMath.sqrt(1 - x*x))/x);
			}


			default:
				return 0.0;

				//return Fmath.asech(evaluateExpressionRecursive(modelstate, node.getChild(0)));

			} //end switch

		}
		return 0.0;
	}




	/**
	 * updates reactant/product species counts based on their stoichiometries
	 * 
	 * @param selectedReactionID the reaction to perform
	 */
	protected void performReaction(ModelState modelstate, String selectedReactionID, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//these are sets of things that need to be re-evaluated or tested due to the reaction firing
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//loop through the reaction's reactants and products and update their amounts
		for (StringDoublePair speciesAndStoichiometry : modelstate.reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) 
		{

			double stoichiometry = speciesAndStoichiometry.doub;
			String speciesID = speciesAndStoichiometry.string;

			//this means the stoichiometry isn't constant, so look to the variableToValue map
			if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(selectedReactionID)) {

				for (StringStringPair doubleID : modelstate.reactionToNonconstantStoichiometriesSetMap.get(selectedReactionID)) {

					//string1 is the species ID; string2 is the speciesReference ID
					if (doubleID.string1.equals(speciesID)) {

						stoichiometry = modelstate.getVariableToValue(doubleID.string2);

						//this is to get the plus/minus correct, as the variableToValueMap has
						//a stoichiometry without the reactant/product plus/minus data
						stoichiometry *= (int)(speciesAndStoichiometry.doub/Math.abs(speciesAndStoichiometry.doub));
						break;
					}
				}
			}

			//update the species count if the species isn't a boundary condition or constant
			//note that the stoichiometries are earlier modified with the correct +/- sign
			boolean cond1 = modelstate.speciesToIsBoundaryConditionMap.get(speciesID);
			boolean cond2 = modelstate.variableToIsConstantMap.get(speciesID);
			if (!cond1 && !cond2) {

				double val = modelstate.getVariableToValue(speciesID) + stoichiometry;
				if(val >= 0)
					modelstate.setvariableToValueMap(speciesID, val);

			}

			//if this variable that was just updated is part of an assignment rule (RHS)
			//then re-evaluate that assignment rule
			if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(speciesID) == true)
				affectedAssignmentRuleSet.addAll(modelstate.variableToAffectedAssignmentRuleSetMap.get(speciesID));

			if (noConstraintsFlag == false && modelstate.variableToIsInConstraintMap.get(speciesID) == true)
				affectedConstraintSet.addAll(modelstate.variableToAffectedConstraintSetMap.get(speciesID));
		}

		if (affectedAssignmentRuleSet.size() > 0)
		{
			performAssignmentRules(modelstate, affectedAssignmentRuleSet);
		}

		if (affectedConstraintSet.size() > 0) 
			//if (testConstraints(modelstate, affectedConstraintSet) == false)
			//	constraintFailureFlag = true;
			//else
			constraintFlag = testConstraints(modelstate, affectedConstraintSet);

	}

	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList) {

		ASTNode child;
		long size = node.getChildCount();

		for (int i = 0; i < size; i++) {
			child = node.getChild(i);
			if (child.getChildCount() == 0)
				nodeChildrenList.add(child);
			else {
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}			
	}

	/**
	 * returns a set of all the reactions that the recently performed reaction affects
	 * "affect" means that the species updates will change the affected reaction's propensity
	 * 
	 * @param selectedReactionID the reaction that was recently performed
	 * @return the set of all reactions that the performed reaction affects the propensity of
	 */
	protected static HashSet<String> getAffectedReactionSet(ModelState modelstate, String selectedReactionID, boolean noAssignmentRulesFlag) {

		HashSet<String> affectedReactionSet = new HashSet<String>(20);
		affectedReactionSet.add(selectedReactionID);


		//loop through the reaction's reactants and products
		for (StringDoublePair speciesAndStoichiometry : modelstate.reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {

			String speciesID = speciesAndStoichiometry.string;
			affectedReactionSet.addAll(modelstate.speciesToAffectedReactionSetMap.get(speciesID));

			//if the species is involved in an assignment rule then it its changing may affect a reaction's propensity
			if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(speciesID)) {

				//this assignment rule is going to be evaluated, so the rule's variable's value will change
				for (AssignmentRule assignmentRule : modelstate.variableToAffectedAssignmentRuleSetMap.get(speciesID)) {
					if (modelstate.speciesToAffectedReactionSetMap.get(assignmentRule.getVariable())!=null) {
						affectedReactionSet.addAll(modelstate.speciesToAffectedReactionSetMap
								.get(assignmentRule.getVariable()));
					}
				}
			}
		}

		return affectedReactionSet;
	}

	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the double to be translated to a boolean
	 * @return the translated boolean value
	 */
	protected static boolean getBooleanFromDouble(double value) {

		if (value == 0.0) 
			return false;
		return true;
	}

	/**
	 * kind of a hack to mingle doubles and booleans for the expression evaluator
	 * 
	 * @param value the boolean to be translated to a double
	 * @return the translated double value
	 */
	protected static double getDoubleFromBoolean(boolean value) {

		if (value == true)
			return 1.0;
		return 0.0;
	}


	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	protected ASTNode inlineFormula(ModelState modelstate, ASTNode formula) {

		if (formula.isFunction() == false || formula.isLeaf() == false) {

			for (int i = 0; i < formula.getChildCount(); ++i)
				formula.replaceChild(i, inlineFormula(modelstate, formula.getChild(i)));//.clone()));
		}

		if (formula.isFunction()
				&& models.get(modelstate.model).getFunctionDefinition(formula.getName()) != null) {

			if (modelstate.ibiosimFunctionDefinitions.contains(formula.getName()))
				return formula;

			ASTNode inlinedFormula = models.get(modelstate.model).getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			this.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
				inlinedChildren.add(inlinedFormula);

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < models.get(modelstate.model).getFunctionDefinition(formula.getName()).getArgumentCount(); ++i) {
				inlinedChildToOldIndexMap.put(models.get(modelstate.model).getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i) {

				ASTNode child = inlinedChildren.get(i);
				//if ((child.getLeftChild() == null && child.getRightChild() == null) && child.isName()) {
				if ((child.getChildCount() == 0) && child.isName()) {

					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula,child.toFormula(), oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
						inlinedFormula = oldFormula.getChild(index);
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	/**
	 * This method is used to update the values involving a species in the global
	 * map. Without this method, species with rules involving replacing species
	 * would not be updated.
	 */
	protected void updateRules()
	{
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		for(ModelState model : submodels.values())
			for(String element : model.isHierarchical)
			{
				if (model.noRuleFlag == false && model.variableToIsInAssignmentRuleMap.get(element) == true)
					affectedAssignmentRuleSet.addAll(model.variableToAffectedAssignmentRuleSetMap.get(element));
				if (affectedAssignmentRuleSet.size() > 0)
					performAssignmentRules(model, affectedAssignmentRuleSet);
				if (model.noConstraintsFlag == false && model.variableToIsInConstraintMap.get(element) == true)
					affectedConstraintSet.addAll(model.variableToAffectedConstraintSetMap.get(element));
				if (affectedConstraintSet.size() > 0) 
					constraintFlag = testConstraints(model, affectedConstraintSet);
			}
	}

	/**
	 * this evaluates a set of constraints that have been affected by an event or reaction firing
	 * and returns the OR'd boolean result
	 * 
	 * @param affectedConstraintSet the set of constraints affected
	 * @return the boolean result of the constraints' evaluation
	 */
	protected boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet) {

		//check all of the affected constraints
		//if one evaluates to true, then the simulation halts
		for (ASTNode constraint : affectedConstraintSet) {
			//System.out.println("Node: " + libsbml.formulaToString(constraint));

			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, constraint)))
				return true;
		}

		return false;
	}

	/**
	 * replaceArgument() doesn't work when you're replacing a localParameter, so this
	 * does that -- finds the oldString within node and replaces it with the local parameter
	 * specified by newString
	 * 
	 * @param node
	 * @param reactionID
	 * @param oldString
	 * @param newString
	 */
	private void alterLocalParameter(ASTNode node, Reaction reaction, String oldString, String newString) 
	{
		//String reactionID = reaction.getId();
		if (node.isName() && node.getName().equals(oldString)) {
			node.setVariable(reaction.getKineticLaw().getLocalParameter(newString));
		}
		else {
			ASTNode childNode;
			for(int i = 0; i < node.getChildCount(); i++)
			{
				childNode = node.getChild(i);
				alterLocalParameter(childNode, reaction, oldString, newString);
			}
		}
	}

	public void replaceArgument(ASTNode formula,String bvar, ASTNode arg) {
		int n = 0;
		for (int i = 0; i < formula.getChildCount(); i++) {
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar)) {
				formula.replaceChild(n, arg.clone());
			} else if (child.getChildCount() > 0) {
				replaceArgument(child, bvar, arg);
			}
			n++;
		}
	}

	/**
	 * performs assignment rules that may have changed due to events or reactions firing
	 * 
	 * @param affectedAssignmentRuleSet the set of assignment rules that have been affected
	 */
	protected HashSet<String> performAssignmentRules(ModelState modelstate, HashSet<AssignmentRule> affectedAssignmentRuleSet) {


		HashSet<String> affectedVariables = new HashSet<String>();

		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {

			String variable = assignmentRule.getVariable();

			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (modelstate.variableToIsConstantMap.containsKey(variable) && modelstate.variableToIsConstantMap.get(variable) == false
					|| modelstate.variableToIsConstantMap.containsKey(variable) == false) {

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
					modelstate.setvariableToValueMap(variable, 
							evaluateExpressionRecursive(modelstate, assignmentRule.getMath()) * 
							//modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(variable)));
							modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
				}
				else {
					modelstate.setvariableToValueMap(variable, evaluateExpressionRecursive(modelstate, assignmentRule.getMath()));
				}

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

	/**
	 * updates the event queue and fires events and so on
	 * @param currentTime the current time in the simulation
	 */
	protected void handleEvents(ModelState modelstate) {

		HashSet<String> triggeredEvents = new HashSet<String>();

		//loop through all untriggered events
		//if any trigger, evaluate the fire time(s) and add them to the queue
		for (String untriggeredEventID : modelstate.untriggeredEventSet) {
			//if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(untriggeredEventID))) == true) {

				//skip the event if it's initially true and this is time == 0
				if (currentTime == 0.0 && modelstate.eventToTriggerInitiallyTrueMap.get(untriggeredEventID) == true)
					continue;

				//switch from false to true must happen
				if (modelstate.eventToPreviousTriggerValueMap.get(untriggeredEventID) == true)
					continue;

				triggeredEvents.add(untriggeredEventID);

				//if assignment is to be evaluated at trigger time, evaluate it and replace the ASTNode assignment
				if (modelstate.eventToUseValuesFromTriggerTimeMap.get(untriggeredEventID) == true)	{

					//temporary hashset of evaluated assignments
					HashSet<Object> evaluatedAssignments = new HashSet<Object>();

					for (Object evAssignment : modelstate.eventToAssignmentSetMap.get(untriggeredEventID)) {

						EventAssignment eventAssignment = (EventAssignment) evAssignment;
						evaluatedAssignments.add(new StringDoublePair(
								eventAssignment.getVariable(), evaluateExpressionRecursive(modelstate, eventAssignment.getMath())));
					}

					double fireTime = currentTime;

					if (modelstate.eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.eventToDelayMap.get(untriggeredEventID));

					modelstate.triggeredEventQueue.add(new EventToFire(modelstate.ID,
							untriggeredEventID, evaluatedAssignments, fireTime));
				}
				else {

					double fireTime = currentTime;

					if (modelstate.eventToHasDelayMap.get(untriggeredEventID) == true)
						fireTime += evaluateExpressionRecursive(modelstate, modelstate.eventToDelayMap.get(untriggeredEventID));
					modelstate.triggeredEventQueue.add(new EventToFire(modelstate.ID,
							untriggeredEventID, modelstate.eventToAssignmentSetMap.get(untriggeredEventID), fireTime));
				}			
			}
			else {

				modelstate.eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
			}
		}

		//remove recently triggered events from the untriggered set
		//when they're fired, they get put back into the untriggered set
		modelstate.untriggeredEventSet.removeAll(triggeredEvents);
	}


	/**
	 * 
	 */
	protected boolean isEventTriggered(ModelState modelstate, double t, double [] y, HashMap<String, Integer> variableToIndexMap) {

		if(checkModelTriggerEvent(topmodel, t, y, variableToIndexMap))
			return true;
		return false;
	}

	protected double evaluateStateExpressionRecursive(ModelState modelstate, ASTNode node, double t, double[] y, HashMap<String, Integer> variableToIndexMap) {
		if (node.isBoolean()) {

			switch (node.getType()) {

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap));

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) == evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) != evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_GEQ:
			{
				//System.out.println("Node: " + libsbml.formulaToString(node.getRightChild()) + " " + evaluateStateExpressionRecursive(modelstate, node.getRightChild()));
				//System.out.println("Node: " + evaluateStateExpressionRecursive(modelstate, node.getLeftChild()) + " " + evaluateStateExpressionRecursive(modelstate, node.getRightChild()));

				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) >= evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
			}
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) <= evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) > evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));

			case RELATIONAL_LT:
			{
				return getDoubleFromBoolean(
						evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap) < evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));			
			}


			default:
				return 0.0;

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;


			default:
				return 0.0;
			}
		}
		else if (node.isInteger())
			return node.getInteger();

		//if it's a number
		else if (node.isReal())
			return node.getReal();

		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {

			String name = node.getName().replace("_negative_","-");

			if (node.getType()== org.sbml.jsbml.ASTNode.Type.NAME_TIME) {

				return currentTime;
			}
			double value;
			int i, j;
			if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(name) &&
					modelstate.speciesToHasOnlySubstanceUnitsMap.get(name) == false) {
				//value = (modelstate.variableToValueMap.get(name) / modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(name)));
				//value = (modelstate.getVariableToValue(name) / modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(name)));
				i = variableToIndexMap.get(name);
				j = variableToIndexMap.get(modelstate.speciesToCompartmentNameMap.get(name));
				value =  y[i] / y[j];
			}
			else	
			{	
				i = variableToIndexMap.get(name);
				value = y[i];
			}
			return value;
		}

		//operators/functions with two children
		else {

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					sum += evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);					

				return sum;
			}

			case MINUS: {

				double sum = evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);

				return product;
			}

			case DIVIDE:
				return (evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap) / evaluateStateExpressionRecursive(modelstate, rightChild, t, y, variableToIndexMap));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateStateExpressionRecursive(modelstate, leftChild, t, y, variableToIndexMap), evaluateStateExpressionRecursive(modelstate, rightChild, t, y, variableToIndexMap)));

			case FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();

				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {

					double leftChildValue = evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap);
					double rightChildValue = evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap);
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential")) {

					return prng.nextExponential(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 1);
				}
				else if (nodeName.equals("gamma")) {

					return prng.nextGamma(1, evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("chisq")) {

					return prng.nextChiSquare((int) evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("lognormal")) {

					return prng.nextLogNormal(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("laplace")) {

					//function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy")) {

					return prng.nextLorentzian(0, evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("poisson")) {

					return prng.nextPoissonian(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("binomial")) {

					return prng.nextBinomial(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap),
							(int) evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));
				}
				else if (nodeName.equals("bernoulli")) {

					return prng.nextBinomial(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap), 1);
				}
				else if (nodeName.equals("normal")) {

					return prng.nextGaussian(evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap),
							evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap));	
				}


				break;
			}

			case FUNCTION_ABS:
				return FastMath.abs(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));				

			case FUNCTION_COS:
				return FastMath.cos(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COSH:
				return FastMath.cosh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_EXP:
				return FastMath.exp(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_LN:
				return FastMath.log(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_LOG:
				return FastMath.log10(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SIN:
				return FastMath.sin(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SINH:
				return FastMath.sinh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_TAN:
				return FastMath.tan(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_TANH:		
				return FastMath.tanh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_PIECEWISE: {

				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3) {

					if ((childIter + 1) < node.getChildCount() && 
							getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, node.getChild(childIter + 1), t, y, variableToIndexMap))) {
						return evaluateStateExpressionRecursive(modelstate, node.getChild(childIter), t, y, variableToIndexMap);
					}
					else if ((childIter + 2) < node.getChildCount()) {
						return evaluateStateExpressionRecursive(modelstate, node.getChild(childIter + 2), t, y, variableToIndexMap);
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateStateExpressionRecursive(modelstate, node.getRightChild(), t, y, variableToIndexMap), 
						1 / evaluateStateExpressionRecursive(modelstate, node.getLeftChild(), t, y, variableToIndexMap));

			case FUNCTION_SEC:
				return Fmath.sec(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_SECH:
				return Fmath.sech(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COT:
				return Fmath.cot(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_COTH:
				return Fmath.coth(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CSC:
				return Fmath.csc(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_CSCH:
				return Fmath.csch(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_DELAY:
				//NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			case FUNCTION_ARCSECH:
				return Fmath.asech(evaluateStateExpressionRecursive(modelstate, node.getChild(0), t, y, variableToIndexMap));

			default:
				return 0.0;
			} //end switch

		}
		return 0.0;
	}


	/**
	 * 
	 */
	protected boolean checkModelTriggerEvent(ModelState modelstate, double t, double[] y, HashMap<String, Integer> variableToIndexMap) {

		if(modelstate.noEventsFlag == true)
			return false;

		for (String untriggeredEventID : modelstate.untriggeredEventSet) 
		{
			//if the trigger evaluates to true
			if (getBooleanFromDouble(evaluateStateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(untriggeredEventID), t, y, variableToIndexMap)) == true) 
			{

				//skip the event if it's initially true and this is time == 0
				if (currentTime == 0.0 && modelstate.eventToTriggerInitiallyTrueMap.get(untriggeredEventID) == true)
					continue;

				//switch from false to true must happen
				if (modelstate.eventToPreviousTriggerValueMap.get(untriggeredEventID) == true)
					continue;

				return true;


			}
		}

		if(modelstate.triggeredEventQueue.peek() != null
				&& modelstate.triggeredEventQueue.peek().fireTime <= t)

			return true;

		return false;
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	protected HashSet<String> fireEvents(ModelState modelstate, String selector, final boolean noAssignmentRulesFlag, final boolean noConstraintsFlag) {

		//temporary set of events to remove from the triggeredEventQueue
		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		//loop through all triggered events
		//if the trigger is no longer true
		//remove from triggered queue and put into untriggered set
		for (EventToFire triggeredEvent : modelstate.triggeredEventQueue)
		{
			String triggeredEventID = triggeredEvent.eventID;

			//if the trigger evaluates to false
			if (modelstate.eventToTriggerPersistenceMap.get(triggeredEventID) == false && 
					getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {

				untriggeredEvents.add(triggeredEventID);
				modelstate.eventToPreviousTriggerValueMap.put(triggeredEventID, false);
			}

			if (modelstate.eventToTriggerPersistenceMap.get(triggeredEventID) == true &&
					getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {
				modelstate.untriggeredEventSet.add(triggeredEventID);
			}
		}

		//copy the triggered event queue -- except the events that are now untriggered
		//this is done because the remove function can't work with just a string; it needs to match events
		//this also re-evaluates the priorities in case they have changed
		//LinkedList<EventToFire> newTriggeredEventQueue = new LinkedList<EventToFire>();

		PriorityQueue<EventToFire> newTriggeredEventQueue = new PriorityQueue<EventToFire>((int)modelstate.numEvents, eventComparator);


		while (modelstate.triggeredEventQueue.size() > 0) {

			EventToFire event = modelstate.triggeredEventQueue.poll();

			EventToFire eventToAdd = new EventToFire(modelstate.ID, event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

			if (untriggeredEvents.contains(event.eventID) == false)
				newTriggeredEventQueue.add(eventToAdd);
			else
				modelstate.untriggeredEventSet.add(event.eventID);
		}

		modelstate.triggeredEventQueue = newTriggeredEventQueue;

		//loop through untriggered events
		//if the trigger is no longer true
		//set the previous trigger value to false
		for (String untriggeredEventID : modelstate.untriggeredEventSet) {

			if (modelstate.eventToTriggerPersistenceMap.get(untriggeredEventID) == false && 
					getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(untriggeredEventID))) == false)
				modelstate.eventToPreviousTriggerValueMap.put(untriggeredEventID, false);
		}

		//these are sets of things that need to be re-evaluated or tested due to the event firing
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		//set of fired events to add to the untriggered set
		HashSet<String> firedEvents = new HashSet<String>();


		//fire all events whose fire time is less than the current time	
		while (modelstate.triggeredEventQueue.size() > 0 &&
				modelstate.triggeredEventQueue.peek().fireTime <= currentTime) {

			EventToFire eventToFire = modelstate.triggeredEventQueue.poll();
			String eventToFireID = eventToFire.eventID;

			//System.err.println("firing " + eventToFireID);

			if (modelstate.eventToAffectedReactionSetMap.get(eventToFireID) != null)
				affectedReactionSet.addAll(modelstate.eventToAffectedReactionSetMap.get(eventToFireID));

			firedEvents.add(eventToFireID);
			//modelstate.eventToPreviousTriggerValueMap.put(eventToFireID, true);
			modelstate.eventToPreviousTriggerValueMap.put(eventToFireID, getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(eventToFireID))) == false);


			//execute all assignments for this event
			for (Object eventAssignment : eventToFire.eventAssignmentSet) {

				String variable;
				double assignmentValue;


				if (modelstate.eventToUseValuesFromTriggerTimeMap.get(eventToFireID) == true)	
				{
					variable = ((StringDoublePair) eventAssignment).string;
					assignmentValue = ((StringDoublePair) eventAssignment).doub;
				}
				else
				{
					variable = ((EventAssignment) eventAssignment).getVariable();
					assignmentValue = evaluateExpressionRecursive(modelstate, ((EventAssignment) eventAssignment).getMath());
				}

				variableInFiredEvents.add(variable);



				//update the species, but only if it's not a constant (bound. cond. is fine)
				if (modelstate.variableToIsConstantMap.get(variable) == false) {

					if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) && 
							modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false)
						modelstate.setvariableToValueMap(variable, 
								assignmentValue); //needs to fix this
					else		
						modelstate.setvariableToValueMap(variable, assignmentValue);
				}

				if (noAssignmentRulesFlag == false && modelstate.variableToIsInAssignmentRuleMap.get(variable) == true) 
					affectedAssignmentRuleSet.addAll(modelstate.variableToAffectedAssignmentRuleSetMap.get(variable));
				if (noConstraintsFlag == false && modelstate.variableToIsInConstraintMap.get(variable) == true)
					affectedConstraintSet.addAll(modelstate.variableToAffectedConstraintSetMap.get(variable));

			} //end loop through assignments

			//after an event fires, need to make sure the queue is updated
			untriggeredEvents.clear();

			//loop through all triggered events
			//if they aren't persistent and the trigger is no longer true
			//remove from triggered queue and put into untriggered set
			for (EventToFire triggeredEvent : modelstate.triggeredEventQueue) {

				String triggeredEventID = triggeredEvent.eventID;

				//if the trigger evaluates to false and the trigger isn't persistent
				if (modelstate.eventToTriggerPersistenceMap.get(triggeredEventID) == false  &&
						getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false) {

					untriggeredEvents.add(triggeredEventID);
					modelstate.eventToPreviousTriggerValueMap.put(triggeredEventID, false);
				}

				if (modelstate.eventToTriggerPersistenceMap.get(triggeredEventID) == true && 
						getBooleanFromDouble(evaluateExpressionRecursive(modelstate, modelstate.eventToTriggerMap.get(triggeredEventID))) == false)
					modelstate.untriggeredEventSet.add(triggeredEventID);
			}

			//copy the triggered event queue -- except the events that are now untriggered
			//this is done because the remove function can't work with just a string; it needs to match events
			//this also re-evaluates the priorities in case they have changed

			newTriggeredEventQueue = new PriorityQueue<EventToFire>((int)modelstate.numEvents, eventComparator);

			while (modelstate.triggeredEventQueue.size() > 0) {

				EventToFire event = modelstate.triggeredEventQueue.poll();

				EventToFire eventToAdd = 
				new EventToFire(modelstate.ID, event.eventID, (HashSet<Object>) event.eventAssignmentSet.clone(), event.fireTime);

				if (untriggeredEvents.contains(event.eventID) == false)
					newTriggeredEventQueue.add(eventToAdd);
				else
					modelstate.untriggeredEventSet.add(event.eventID);
			}

			modelstate.triggeredEventQueue = newTriggeredEventQueue;

			//some events might trigger after this
			handleEvents(modelstate);
		}//end loop through event queue

		//add the fired events back into the untriggered set
		//this allows them to trigger/fire again later


		modelstate.untriggeredEventSet.addAll(firedEvents);
		if(selector.equals("variable"))
			return variableInFiredEvents;
		return affectedReactionSet;
	}




	/**
	 * puts constraint-related information into data structures
	 */
	protected void setupConstraints(ModelState modelstate) {

		if (modelstate.numConstraints > 0)
			modelstate.noConstraintsFlag = false;

		//loop through all constraints to find out which variables affect which constraints
		//this is stored in a hashmap, as is whether the variable is in a constraint
		for (Constraint constraint : models.get(modelstate.model).getListOfConstraints()) {
			if(constraint.isSetMetaId() && modelstate.isDeletedByMetaID(constraint.getMetaId()))
				continue;
			constraint.setMath(inlineFormula(modelstate, constraint.getMath()));
			for (ASTNode constraintNode : constraint.getMath().getListOfNodes()) {

				if (constraintNode.isName()) {

					String nodeName = constraintNode.getName();					
					modelstate.variableToAffectedConstraintSetMap.put(nodeName, new HashSet<ASTNode>());
					modelstate.variableToAffectedConstraintSetMap.get(nodeName).add(constraint.getMath());
					modelstate.variableToIsInConstraintMap.put(nodeName, true);
				}
			}
		}
	}



	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private void setupSingleSpecies(ModelState modelstate, Species species, String speciesID) {
		double initValue = 0;
		if (modelstate.speciesIDSet.contains(speciesID))
			return;

		if (modelstate.numConstraints > 0)
			modelstate.variableToIsInConstraintMap.put(speciesID, false);

		if (species.isSetInitialAmount())
		{
			initValue = species.getInitialAmount();
			modelstate.setvariableToValueMap(speciesID, species.getInitialAmount());
		}

		else if (species.isSetInitialConcentration()) 
		{
			initValue = species.getInitialConcentration() 
					* models.get(modelstate.model).getCompartment(species.getCompartment()).getSize();
			modelstate.variableToValueMap.put(speciesID, initValue);
		}
		else
		{
			modelstate.variableToValueMap.put(speciesID, initValue);
		}

		if (species.getHasOnlySubstanceUnits() == false) {
			modelstate.speciesToCompartmentNameMap.put(speciesID, species.getCompartment());
		}	
		if (modelstate.numRules > 0)
			modelstate.variableToIsInAssignmentRuleMap.put(speciesID, false);


		modelstate.speciesToAffectedReactionSetMap.put(speciesID, new HashSet<String>(20));


		modelstate.speciesToIsBoundaryConditionMap.put(speciesID, species.getBoundaryCondition());
		modelstate.variableToIsConstantMap.put(speciesID, species.getConstant());
		modelstate.speciesToHasOnlySubstanceUnitsMap.put(speciesID, species.getHasOnlySubstanceUnits());
		modelstate.speciesIDSet.add(speciesID);
		//SpeciesWrapper spec = new SpeciesWrapper(species.getBoundaryCondition(), species.getConstant(), species.getHasOnlySubstanceUnits(), initValue);

	}

	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	protected void setupReplacingSpecies() 
	{
		
	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	protected void setupSpecies(ModelState modelstate) throws IOException {

		//add values to hashmap for easy access to species amounts
		Species species;
		long size = models.get(modelstate.model).getListOfSpecies().size();
		for (int i = 0; i < size; i++) 
		{

			species = models.get(modelstate.model).getSpecies(i);

			if(species.isSetId() && modelstate.isDeletedBySID(species.getId()))
				continue;
			else if(species.isSetMetaId() && modelstate.isDeletedByMetaID(species.getMetaId()))
				continue;

			setupSingleSpecies(modelstate, species, species.getId());
		}

	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	private void setupLocalParameters(ModelState modelstate, KineticLaw kineticLaw, Reaction reaction) {

		String reactionID = reaction.getId();
		reactionID = reactionID.replace("_negative_","-");

		for (int i = 0; i < kineticLaw.getLocalParameterCount(); i++) {

			LocalParameter localParameter = kineticLaw.getLocalParameter(i);

			if(localParameter.isSetId() && modelstate.isDeletedBySID(localParameter.getId()))
				continue;
			if(localParameter.isSetMetaId() && modelstate.isDeletedByMetaID(localParameter.getMetaId()))
				continue;

			String parameterID = "";

			//the parameters don't get reset after each run, so don't re-do this prepending
			if (localParameter.getId().contains(reactionID + "_") == false)					
				parameterID = reactionID + "_" + localParameter.getId();
			else 
				parameterID = localParameter.getId();

			String oldParameterID = localParameter.getId();
			modelstate.variableToValueMap.put(parameterID, localParameter.getValue());

			//alter the local parameter ID so that it goes to the local and not global value
			if (localParameter.getId() != parameterID) {
				localParameter.setId(parameterID);
				SBMLutilities.setMetaId(localParameter, parameterID);
			}
			alterLocalParameter(kineticLaw.getMath(), reaction, oldParameterID, parameterID);
		}
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private static void setupSingleParameter(ModelState modelstate, Parameter parameter, boolean vector, boolean matrix, int i, int j) {


		String parameterID = parameter.getId();

		if(vector)
			parameterID = parameterID + "__" + i;
		else if(matrix)
			parameterID = parameterID + "__" + i + "__" + j;

		modelstate.variableToValueMap.put(parameterID, parameter.getValue());
		modelstate.variableToIsConstantMap.put(parameterID, parameter.getConstant());
		if(!parameter.getConstant())
			modelstate.variablesToPrint.add(parameterID);
		if (parameter.getConstant() == false)
			modelstate.nonconstantParameterIDSet.add(parameterID);

		if (modelstate.numRules > 0)
			modelstate.variableToIsInAssignmentRuleMap.put(parameterID, false);

		if (modelstate.numConstraints > 0)
			modelstate.variableToIsInConstraintMap.put(parameterID, false);
	}




	/**
	 * puts parameter-related information into data structures
	 */
	protected void setupParameters(ModelState modelstate) {

		//add local parameters
		Reaction reaction;
		Parameter parameter;
		long size;


		size = modelstate.numReactions;
		for (int i = 0; i < size; i++) 
		{
			reaction = models.get(modelstate.model).getReaction(i);
			if (!reaction.isSetKineticLaw()) continue;
			KineticLaw kineticLaw = reaction.getKineticLaw();

			if(kineticLaw.isSetMetaId() && modelstate.isDeletedByMetaID(kineticLaw.getMetaId()))
				continue;
			setupLocalParameters(modelstate, kineticLaw, reaction);
		}

		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay

		size = models.get(modelstate.model).getListOfParameters().size();
		for (int i = 0; i < size; i++) 
		{
			parameter = models.get(modelstate.model).getParameter(i);

			if(parameter.isSetId() && modelstate.isDeletedBySID(parameter.getId()))
				continue;
			else if(parameter.isSetMetaId() && modelstate.isDeletedByMetaID(parameter.getMetaId()))
				continue;
			// Check if it is a vector 
			// TODO: This code is no longer valid
			/*
			String vSize = biomodel.annotation.AnnotationUtility.parseVectorSizeAnnotation(parameter);
			if(vSize != null)
			{

				int n = (int)models.get(modelstate.model).getParameter(vSize).getValue();

				for(int j = 0; j < n; j++)
				{
					setupSingleParameter(modelstate, parameter, true, false, j, 0);
				}

				continue;
			}
			// Check if it is a vector 
			String [] mSize = biomodel.annotation.AnnotationUtility.parseMatrixSizeAnnotation(parameter);
			if(mSize != null && mSize.length == 2)
			{
				int n = (int)models.get(modelstate.model).getParameter(mSize[0]).getValue();

				int m = (int)models.get(modelstate.model).getParameter(mSize[1]).getValue();

				for(int j = 0; j < m; j++)
				{
					for(int k = 0; k < n; k++)
					{

						setupSingleParameter(modelstate, parameter, false, true, k, j);
					}
				}
				continue;
			}
	*/
			setupSingleParameter(modelstate, parameter, false, false, 0, 0);
		}


		//add compartment sizes in
		size = models.get(modelstate.model).getCompartmentCount();
		for (int i = 0; i < size; i++) 
		{
			Compartment compartment = models.get(modelstate.model).getCompartment(i);
			String compartmentID = compartment.getId();

			if(compartment.isSetId() && modelstate.isDeletedBySID(compartment.getId()))
				continue;
			else if(compartment.isSetMetaId() && modelstate.isDeletedByMetaID(compartment.getMetaId()))
				continue;

			modelstate.compartmentIDSet.add(compartmentID);
			modelstate.variableToValueMap.put(compartmentID, compartment.getSize());

			if (Double.isNaN(compartment.getSize()))
				modelstate.setvariableToValueMap(compartmentID, 1.0);

			modelstate.variableToIsConstantMap.put(compartmentID, compartment.getConstant());

			if(!compartment.getConstant())
				modelstate.variablesToPrint.add(compartmentID);

			if (modelstate.numRules > 0)
				modelstate.variableToIsInAssignmentRuleMap.put(compartmentID, false);

			if (modelstate.numConstraints > 0)
				modelstate.variableToIsInConstraintMap.put(compartmentID, false);
		}
	}


	protected void setupNonConstantSpeciesReferences(ModelState modelstate)
	{

		//loop through all reactions and calculate their propensities
		Reaction reaction;

		for (int i = 0;  i < modelstate.numReactions; i++) 
		{
			reaction = models.get(modelstate.model).getReaction(i);

			for (SpeciesReference reactant : reaction.getListOfReactants()) 
			{
				if(reactant.isSetId() && modelstate.isDeletedBySID(reactant.getId()))
					continue;
				else if(reactant.isSetMetaId() && modelstate.isDeletedByMetaID(reactant.getMetaId()))
					continue;

				if(reactant.getId().length() > 0)
				{
					modelstate.variableToIsConstantMap.put(reactant.getId(), reactant.getConstant());
					if (reactant.getConstant() == false) {
						modelstate.variablesToPrint.add(reactant.getId());
						if(modelstate.variableToValueMap.containsKey(reactant.getId()) == false)
							modelstate.setvariableToValueMap(reactant.getId(), reactant.getStoichiometry());
					}
				}
			}

			for (SpeciesReference product : reaction.getListOfProducts()) 
			{
				if(product.isSetId() && modelstate.isDeletedBySID(product.getId()))
					continue;
				else if(product.isSetMetaId() && modelstate.isDeletedByMetaID(product.getMetaId()))
					continue;
				if(product.getId().length() > 0)
				{
					modelstate.variableToIsConstantMap.put(product.getId(), product.getConstant());
					if (product.getConstant() == false) {
						modelstate.variablesToPrint.add(product.getId());
						if(modelstate.variableToValueMap.containsKey(product.getId()) == false)
							modelstate.setvariableToValueMap(product.getId(), product.getStoichiometry());
					}
				}
			}
		}
	}


	/**
	 * calculates the initial propensity of a single reaction
	 * also does some initialization stuff
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	private void setupSingleReaction(ModelState modelstate, String reactionID, ASTNode reactionFormula, boolean reversible, ListOf<SpeciesReference> reactantsList,
			ListOf<SpeciesReference> productsList, ListOf<ModifierSpeciesReference> modifiersList) {
		reactionID = reactionID.replace("_negative_","-");

		long size;
		boolean notEnoughMoleculesFlagFd = false;
		boolean notEnoughMoleculesFlagRv = false;
		boolean notEnoughMoleculesFlag = false;


		//if it's a reversible reaction
		//split into a forward and reverse reaction (based on the minus sign in the middle)
		//and calculate both propensities
		if (reversible == true) {
			//distributes the left child across the parentheses

			if (reactionFormula.getType().equals(ASTNode.Type.TIMES)) {

				ASTNode distributedNode = new ASTNode();

				reactionFormula = inlineFormula(modelstate, reactionFormula);
				ASTNode temp = new ASTNode(1);
				if (reactionFormula.getChildCount() == 2 &&
						reactionFormula.getChild(1).getType().equals(ASTNode.Type.PLUS))
					distributedNode = ASTNode.sum(
							ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()), 
							ASTNode.times(new ASTNode(-1), reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));
				else if (reactionFormula.getChildCount() == 2 &&
						reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
					distributedNode = ASTNode.diff(
							ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getLeftChild()), 
							ASTNode.times(reactionFormula.getLeftChild(), reactionFormula.getRightChild().getRightChild()));

				else if(reactionFormula.getChildCount() >= 2)
				{
					for(ASTNode node : reactionFormula.getListOfNodes())
					{
						if (node.getChildCount() >= 2)
						{
							if(reactionFormula.getChild(1).getType().equals(ASTNode.Type.MINUS))
								distributedNode = ASTNode.sum(
										ASTNode.times(temp, node.getLeftChild()), 
										ASTNode.times(temp, node.getRightChild().getRightChild()));
							else
								distributedNode = ASTNode.diff(
										ASTNode.times(temp, node.getLeftChild()), 
										ASTNode.times(temp, node.getRightChild().getRightChild()));

						}
						else
						{
							temp = ASTNode.times(temp, node);
						}
					}
				}

				if(distributedNode.isUnknown())
					reactionFormula = temp;
				else
					reactionFormula = distributedNode;
			}

			else if (reactionFormula.getType().equals(ASTNode.Type.MINUS)) {

				ASTNode distributedNode = new ASTNode();
				ASTNode temp = new ASTNode(1);
				reactionFormula = inlineFormula(modelstate, reactionFormula);

				if(reactionFormula.getChildCount() == 1)
				{
					for(ASTNode node : reactionFormula.getChild(0).getListOfNodes())
					{
						if (node.getChildCount() >= 2)
						{
							if(reactionFormula.getChild(0).getType().equals(ASTNode.Type.MINUS))
								distributedNode = ASTNode.sum(
										ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()), 
										ASTNode.times(temp, node.getRightChild()));
							else
								distributedNode = ASTNode.diff(
										ASTNode.times(new ASTNode(-1), temp, node.getLeftChild()), 
										ASTNode.times(temp, node.getRightChild()));

						}
						else
						{
							temp = ASTNode.times(temp, node);
						}
					}


					reactionFormula = distributedNode;
				}

			}


			modelstate.reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			modelstate.reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());
			modelstate.reactionToReactantStoichiometrySetMap.put(reactionID + "_fd", new HashSet<StringDoublePair>());
			modelstate.reactionToReactantStoichiometrySetMap.put(reactionID + "_rv", new HashSet<StringDoublePair>());

			for (SpeciesReference reactant : reactantsList) {

				String reactantID = reactant.getSpecies().replace("_negative_","-");


				double reactantStoichiometry;

				//if there was an initial assignment for the reactant
				//this applies regardless of constancy of the reactant

				if (modelstate.variableToValueMap.contains(reactant.getId()))
					reactantStoichiometry = modelstate.getVariableToValue(reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();


				if(productsList.size() == 0)
				{
					modelstate.reactionToReactantStoichiometrySetMap.get(reactionID+"_fd").add(
							new StringDoublePair(reactantID, reactantStoichiometry));

				}
				else
				{
					modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(reactantID, -reactantStoichiometry));
					modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(reactantID, reactantStoichiometry));

					//not having a minus sign is intentional as this isn't used for calculations
					modelstate.reactionToReactantStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(reactantID, reactantStoichiometry));
				}
				//if there was not initial assignment for the reactant
				if (reactant.getConstant() == false &&
						reactant.getId().length() > 0) {

					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID + "_fd") == false)
						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID + "_fd", new HashSet<StringStringPair>());
					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID + "_rv") == false)
						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID + "_rv", new HashSet<StringStringPair>());


					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID + "_fd")
					.add(new StringStringPair(reactantID + "_fd", reactant.getId()));
					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID + "_rv")
					.add(new StringStringPair(reactantID + "_rv", reactant.getId()));
					if(modelstate.variableToValueMap.containsKey(reactant.getId()) == false)
						modelstate.setvariableToValueMap(reactant.getId(), reactantStoichiometry);
				}

				//				else if(modelstate.variableToIsConstantMap.get(reactantID) == false)
				//				{
				//
				//					modelstate.variableToIsConstantMap.put(reactant.getId(), false);
				//					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
				//						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
				//
				//					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
				//					.add(new StringStringPair(reactantID, "-" + reactant.getId()));
				//
				//					if(modelstate.variableToValueMap.containsKey(reactant.getId()) == false)
				//						modelstate.setvariableToValueMap(reactant.getId(), reactantStoichiometry);
				//				}

				//as a reactant, this species affects the reaction's propensity in the forward direction
				modelstate.speciesToAffectedReactionSetMap.get(reactantID).add(reactionID + "_fd");

				//make sure there are enough molecules for this species
				//(in the reverse direction, molecules aren't subtracted, but added)
				if (modelstate.getVariableToValue(reactantID) < reactantStoichiometry)
					notEnoughMoleculesFlagFd = true;
			}

			for (SpeciesReference product : productsList) {

				String productID = product.getSpecies().replace("_negative_","-");



				double productStoichiometry;

				//if there was an initial assignment
				if (modelstate.variableToValueMap.containsKey(product.getId()))
					productStoichiometry = modelstate.getVariableToValue(product.getId());
				else
					productStoichiometry = product.getStoichiometry();

				if(reactantsList.size() == 0)
				{
					modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID+"_fd").add(
							new StringDoublePair(productID, productStoichiometry));

				}
				else
				{
					modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_fd").add(
							new StringDoublePair(productID, productStoichiometry));
					modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(productID, -productStoichiometry));

					//not having a minus sign is intentional as this isn't used for calculations
					modelstate.reactionToReactantStoichiometrySetMap.get(reactionID + "_rv").add(
							new StringDoublePair(productID, productStoichiometry));
				}
				//if there wasn't an initial assignment
				if (product.getConstant() == false) 
				{

					if(product.getId().length() > 0 )
					{
						modelstate.variableToIsConstantMap.put(product.getId(), false);
						if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
							modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());

						modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
						.add(new StringStringPair(productID, product.getId()));

						if(modelstate.variableToValueMap.containsKey(product.getId()) == false)
							modelstate.setvariableToValueMap(product.getId(), productStoichiometry);
					}
				}
				//				else if(modelstate.variableToIsConstantMap.get(productID) == false)
				//				{
				//
				//					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
				//						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
				//
				//					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
				//					.add(new StringStringPair(productID, product.getId()));
				//
				//					if(modelstate.variableToValueMap.containsKey(product.getId()) == false)
				//						modelstate.setvariableToValueMap(product.getId(), productStoichiometry);
				//				}

				//as a product, this species affects the reaction's propensity in the reverse direction
				modelstate.speciesToAffectedReactionSetMap.get(productID).add(reactionID + "_rv");

				//make sure there are enough molecules for this species
				//(in the forward direction, molecules aren't subtracted, but added)
				if (modelstate.getVariableToValue(productID) < productStoichiometry)
					notEnoughMoleculesFlagRv = true;
			}

			for (ModifierSpeciesReference modifier : modifiersList) 
			{

				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_","-");

				String forwardString = "", reverseString = "";

				try {
					forwardString = ASTNode.formulaToString(reactionFormula.getLeftChild());
					reverseString = ASTNode.formulaToString(reactionFormula.getRightChild());
				} 
				catch (SBMLException e) {
					e.printStackTrace();
				}

				//check the kinetic law to see which direction the modifier affects the reaction's propensity
				if (forwardString.contains(modifierID))
					modelstate.speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_fd");

				if (reverseString.contains(modifierID))
					modelstate.speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_rv");
			}				

			double propensity;

			if(productsList.getChildCount() > 0 && reactantsList.getChildCount() > 0)
			{
				modelstate.reactionToFormulaMap.put(reactionID + "_rv", inlineFormula(modelstate, reactionFormula.getRightChild()));
				modelstate.reactionToFormulaMap.put(reactionID + "_fd", inlineFormula(modelstate, reactionFormula.getLeftChild()));

				//calculate forward reaction propensity
				if (notEnoughMoleculesFlagFd == true)
					propensity = 0.0;
				else {
					//the left child is what's left of the minus sign
					propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula.getLeftChild()));

					//stoichiometry amplification -- alter the propensity
					if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
						propensity *= (1.0 / stoichAmpGridValue);

					if ((propensity < modelstate.minPropensity) && (propensity > 0)) 
						modelstate.minPropensity = propensity;

					if (propensity > modelstate.maxPropensity)
						modelstate.maxPropensity = propensity;

					modelstate.propensity += propensity;
					//totalPropensity += propensity;
				}

				modelstate.reactionToPropensityMap.put(reactionID + "_fd", propensity);

				//calculate reverse reaction propensity
				if (notEnoughMoleculesFlagRv == true)
					propensity = 0.0;
				else {
					//the right child is what's right of the minus sign
					propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula.getRightChild()));

					if(propensity < 0.0)
						propensity = 0.0;

					if (propensity < modelstate.minPropensity && propensity > 0) 
						modelstate.minPropensity = propensity;

					if (propensity > modelstate.maxPropensity)
						modelstate.maxPropensity = propensity;

					modelstate.propensity += propensity;

					//totalPropensity += propensity;
				}

				modelstate.reactionToPropensityMap.put(reactionID + "_rv", propensity);
			}
			else
			{
				if(reactantsList.getChildCount() > 0)
				{
					modelstate.reactionToFormulaMap.put(reactionID+"_fd", inlineFormula(modelstate, reactionFormula));
					//calculate reverse reaction propensity
					if (notEnoughMoleculesFlagRv == true)
						propensity = 0.0;
					else {
						//the right child is what's right of the minus sign
						propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula));

						if(propensity < 0.0)
							propensity = 0.0;

						if (propensity < modelstate.minPropensity && propensity > 0) 
							modelstate.minPropensity = propensity;

						if (propensity > modelstate.maxPropensity)
							modelstate.maxPropensity = propensity;

						modelstate.propensity += propensity;

						//totalPropensity += propensity;
					}

					modelstate.reactionToPropensityMap.put(reactionID+"_fd", propensity);
				}
				else if(productsList.getChildCount() > 0)
				{
					modelstate.reactionToFormulaMap.put(reactionID+"_fd", inlineFormula(modelstate, reactionFormula));
					//calculate forward reaction propensity
					if (notEnoughMoleculesFlagFd == true)
						propensity = 0.0;
					else {
						//the left child is what's left of the minus sign
						propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula));

						//stoichiometry amplification -- alter the propensity
						if (reactionID.contains("_Diffusion_") && stoichAmpBoolean == true)
							propensity *= (1.0 / stoichAmpGridValue);

						if ((propensity < modelstate.minPropensity) && (propensity > 0)) 
							modelstate.minPropensity = propensity;

						if (propensity > modelstate.maxPropensity)
							modelstate.maxPropensity = propensity;

						modelstate.propensity += propensity;
						//totalPropensity += propensity;
					}

					modelstate.reactionToPropensityMap.put(reactionID+"_fd", propensity);
				}



			}
		}

		//if it's not a reversible reaction
		else 
		{
			modelstate.reactionToSpeciesAndStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());
			modelstate.reactionToReactantStoichiometrySetMap.put(reactionID, new HashSet<StringDoublePair>());

			//if(replacementSubModels.get(reactionID) != null && replacementSubModels.get(reactionID).contains(modelstate.ID))
			//	return;



			size = reactantsList.size();
			for (int i = 0; i < size; i++)
			{


				SpeciesReference reactant = reactantsList.get(i);



				String reactantID = reactant.getSpecies().replace("_negative_","-");


				double reactantStoichiometry;

				//if there was an initial assignment for the speciesref id
				if (modelstate.variableToValueMap.containsKey(reactant.getId()))
					reactantStoichiometry = modelstate.getVariableToValue(reactant.getId());
				else
					reactantStoichiometry = reactant.getStoichiometry();

				modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, -reactantStoichiometry));


				//as a reactant, this species affects the reaction's propensity
				modelstate.speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);
				modelstate.reactionToReactantStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(reactantID, reactantStoichiometry));
				//make sure there are enough molecules for this species
				if (modelstate.getVariableToValue(reactantID) < reactantStoichiometry)
					notEnoughMoleculesFlag = true;


				//if there was not initial assignment for the reactant
				if (reactant.getConstant() == false &&
						reactant.getId().length() > 0) {

					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());


					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
					.add(new StringStringPair(reactantID, "-" +  reactant.getId()));
					if(modelstate.variableToValueMap.containsKey(reactant.getId()) == false)
						modelstate.setvariableToValueMap(reactant.getId(), reactantStoichiometry);
				}

				//				else if(modelstate.variableToIsConstantMap.get(reactantID) == false)
				//				{
				//
				//					modelstate.variableToIsConstantMap.put(reactant.getId(), false);
				//					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
				//						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
				//
				//					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
				//					.add(new StringStringPair(reactantID, "-" + reactant.getId()));
				//
				//					if(modelstate.variableToValueMap.containsKey(reactant.getId()) == false)
				//						modelstate.setvariableToValueMap(reactant.getId(), reactantStoichiometry);
				//				}
			}

			size = productsList.size();
			for (int i = 0; i < size; i ++) {
				SpeciesReference product = productsList.get(i); 

				String productID = product.getSpecies().replace("_negative_","-");
				double productStoichiometry;

				//if there was an initial assignment for the speciesref id
				if (modelstate.variableToValueMap.containsKey(product.getId()))
					productStoichiometry = modelstate.getVariableToValue(product.getId());
				else
					productStoichiometry = product.getStoichiometry();

				modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reactionID).add(
						new StringDoublePair(productID, productStoichiometry));

				//if there wasn't an initial assignment
				if (product.getConstant() == false) 
				{

					if(product.getId().length() > 0 )
					{
						modelstate.variableToIsConstantMap.put(product.getId(), false);
						if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
							modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());

						modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
						.add(new StringStringPair(productID, product.getId()));

						if(modelstate.variableToValueMap.containsKey(product.getId()) == false)
							modelstate.setvariableToValueMap(product.getId(), productStoichiometry);
					}
				}
				//				else if(modelstate.variableToIsConstantMap.get(productID) == false)
				//				{
				//
				//					if (modelstate.reactionToNonconstantStoichiometriesSetMap.containsKey(reactionID) == false)
				//						modelstate.reactionToNonconstantStoichiometriesSetMap.put(reactionID, new HashSet<StringStringPair>());
				//
				//					modelstate.reactionToNonconstantStoichiometriesSetMap.get(reactionID)
				//					.add(new StringStringPair(productID, product.getId()));
				//
				//					if(modelstate.variableToValueMap.containsKey(product.getId()) == false)
				//						modelstate.setvariableToValueMap(product.getId(), productStoichiometry);
				//				}
				//
				//as a product, this species affects the reaction's propensity in the reverse direction
				modelstate.speciesToAffectedReactionSetMap.get(productID).add(reactionID);

				//make sure there are enough molecules for this species
				//(in the forward direction, molecules aren't subtracted, but added)
				if (modelstate.getVariableToValue(productID) < productStoichiometry)
					notEnoughMoleculesFlagRv = true;

			}
			for (ModifierSpeciesReference modifier : modifiersList) {

				String modifierID = modifier.getSpecies();
				modifierID = modifierID.replace("_negative_","-");

				//as a modifier, this species affects the reaction's propensity
				modelstate.speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
			}
			reactionFormula = inlineFormula(modelstate, reactionFormula);
			modelstate.reactionToFormulaMap.put(reactionID, reactionFormula);

			double propensity;


			if (notEnoughMoleculesFlag == true)
				propensity = 0.0;
			else {//calculate propensity
				//System.out.println("Node: " + libsbml.formulaToString(reactionFormula));
				//System.out.println("Node: " + evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula).getLeftChild()));

				propensity = evaluateExpressionRecursive(modelstate, inlineFormula(modelstate, reactionFormula));
				if(propensity < 0.0)
					propensity = 0.0;

				if (propensity < modelstate.minPropensity && propensity > 0) 
					modelstate.minPropensity = propensity;
				if (propensity > modelstate.maxPropensity)
					modelstate.maxPropensity = propensity;

				modelstate.propensity += propensity;

				//this.totalPropensity += propensity;
			}

			modelstate.reactionToPropensityMap.put(reactionID, propensity);
		}
	}

	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	protected void setupReactions(ModelState modelstate) {

		//loop through all reactions and calculate their propensities
		Reaction reaction;

		for (int i = 0;  i < modelstate.numReactions; i++) 
		{
			reaction = models.get(modelstate.model).getReaction(i);
			if(reaction.isSetId() && modelstate.isDeletedBySID(reaction.getId()))
				continue;
			else if(reaction.isSetMetaId() && modelstate.isDeletedByMetaID(reaction.getMetaId()))
				continue;
			if (!reaction.isSetKineticLaw()) continue;

			String reactionID = reaction.getId();

			String species = reactionID.replace(GlobalConstants.DEGRADATION + "_", "");

			if(reactionID.contains(GlobalConstants.DEGRADATION) && replacements.containsKey(species))
				if(modelstate.isHierarchical.contains(species) && !modelstate.ID.equals("topmodel"))
					continue;

			ASTNode reactionFormula = reaction.getKineticLaw().getMath();

			setupSingleReaction(modelstate, reactionID, reactionFormula, reaction.getReversible(), reaction.getListOfReactants(),
					reaction.getListOfProducts(), reaction.getListOfModifiers());
		}
	}

	/**
	 * sets up a single event
	 * 
	 * @param event
	 */
	protected void setupSingleEvent(ModelState modelstate, Event event) {

		String eventID = event.getId();


		if (event.isSetPriority())
			modelstate.eventToPriorityMap.put(eventID, inlineFormula(modelstate, event.getPriority().getMath()));

		if (event.isSetDelay()) {

			modelstate.eventToDelayMap.put(eventID, inlineFormula(modelstate, event.getDelay().getMath()));
			modelstate.eventToHasDelayMap.put(eventID, true);
		}
		else
			modelstate.eventToHasDelayMap.put(eventID, false);

		event.getTrigger().setMath(inlineFormula(modelstate, event.getTrigger().getMath()));

		modelstate.eventToTriggerMap.put(eventID, event.getTrigger().getMath());
		modelstate.eventToTriggerInitiallyTrueMap.put(eventID, event.getTrigger().getInitialValue());
		modelstate.eventToPreviousTriggerValueMap.put(eventID, event.getTrigger().getInitialValue());
		modelstate.eventToTriggerPersistenceMap.put(eventID, event.getTrigger().getPersistent());
		modelstate.eventToUseValuesFromTriggerTimeMap.put(eventID, event.getUseValuesFromTriggerTime());
		modelstate.eventToAssignmentSetMap.put(eventID, new HashSet<Object>());
		modelstate.eventToAffectedReactionSetMap.put(eventID, new HashSet<String>());

		modelstate.untriggeredEventSet.add(eventID);

		for (EventAssignment assignment : event.getListOfEventAssignments()) {


			String variableID = assignment.getVariable();

			assignment.setMath(inlineFormula(modelstate, assignment.getMath()));

			modelstate.eventToAssignmentSetMap.get(eventID).add(assignment);

			if (modelstate.variableToEventSetMap.containsKey(variableID) == false)
				modelstate.variableToEventSetMap.put(variableID, new HashSet<String>());

			modelstate.variableToEventSetMap.get(variableID).add(eventID);

			//if the variable is a species, add the reactions it's in
			//to the event to affected reaction hashmap, which is used
			//for updating propensities after an event fires
			if (modelstate.speciesToAffectedReactionSetMap.containsKey(variableID)) {

				modelstate.eventToAffectedReactionSetMap.get(eventID).addAll(
						modelstate.speciesToAffectedReactionSetMap.get(variableID));
			}					
		}
	}


	protected void setupInitialAssignments(ModelState modelstate)
	{
		HashSet<String> affectedVariables = new HashSet<String>();
		HashSet<AssignmentRule> allAssignmentRules = new HashSet<AssignmentRule>();

		//perform all assignment rules
		for (Rule rule : models.get(modelstate.model).getListOfRules()) 
		{
			if (rule.isAssignment())
				allAssignmentRules.add((AssignmentRule)rule);
		}

		long maxIterations = modelstate.numParameters + modelstate.numSpecies + modelstate.numCompartments;
		long numIterations = 0;
		double newResult = 0;
		boolean changed = true, temp = false;

		while(changed)
		{
			if(numIterations > maxIterations)
			{
				System.out.println("Error: circular dependency");
				return;
			}

			changed = false;
			numIterations++; 
			for (InitialAssignment initialAssignment : models.get(modelstate.model).getListOfInitialAssignments()) 
			{
				String variable = initialAssignment.getVariable().replace("_negative_","-");				
				initialAssignment.setMath(inlineFormula(modelstate, initialAssignment.getMath()));
				if(models.get(modelstate.model).containsSpecies(variable))
				{
					temp = calcSpeciesInitAssign(modelstate, variable, initialAssignment);
				}
				else if(models.get(modelstate.model).containsCompartment(variable))
				{
					temp = calcCompInitAssign(modelstate, variable, initialAssignment);
				}
				else if(models.get(modelstate.model).containsParameter(variable))
				{
					temp = calcParamInitAssign(modelstate, variable, initialAssignment);
				}
				else
				{
					newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath());
					if(newResult != modelstate.getVariableToValue(variable))
					{
						modelstate.setvariableToValueMap(variable, newResult);
						temp = true;
					}
				}

				changed |= temp;

				affectedVariables.add(variable);
			}

			changed |= calcAssignmentRules(modelstate, allAssignmentRules);
		}

	}

	protected boolean calcAssignmentRules(ModelState modelstate, HashSet<AssignmentRule> affectedAssignmentRuleSet) {

		boolean changed = false;
		boolean temp = false;
		double newResult, oldResult;
		for (AssignmentRule assignmentRule : affectedAssignmentRuleSet) {

			String variable = assignmentRule.getVariable();

			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (modelstate.variableToIsConstantMap.containsKey(variable) && modelstate.variableToIsConstantMap.get(variable) == false
					|| modelstate.variableToIsConstantMap.containsKey(variable) == false) {

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {

					oldResult = modelstate.getVariableToValue(variable);
					newResult = evaluateExpressionRecursive(modelstate, assignmentRule.getMath()) * 
							modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable));
					if(oldResult != newResult)
					{
						modelstate.setvariableToValueMap(variable, newResult);
						temp = true;
					}
				}
				else {
					oldResult = modelstate.getVariableToValue(variable);
					newResult = evaluateExpressionRecursive(modelstate, assignmentRule.getMath());

					if(oldResult != newResult)
					{
						modelstate.setvariableToValueMap(variable, newResult);
						temp = true;
					}
				}

				changed |= temp;
			}
		}

		return changed;
	}

	private boolean calcParamInitAssign(ModelState modelstate, String variable, InitialAssignment initialAssignment)
	{
		double newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath());
		double oldResult = modelstate.getVariableToValue(variable);

		//	if(Double.compare(newResult, oldResult) == 0)
		//return false;

		if(newResult != oldResult)
		{
			modelstate.setvariableToValueMap(variable, newResult);


			return true;
		}

		return false;
	}

	private boolean calcCompInitAssign(ModelState modelstate, String variable, InitialAssignment initialAssignment)
	{
		double newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath());
		double oldResult = modelstate.getVariableToValue(variable);
		//double speciesVal = 0;
		if(newResult != oldResult)
		{
			if(oldResult == Double.NaN)
				oldResult = 1.0;

			modelstate.setvariableToValueMap(variable, newResult);
			if (modelstate.numRules > 0) 
			{
				HashSet<AssignmentRule> rules = modelstate.variableToAffectedAssignmentRuleSetMap.get(variable);

				performAssignmentRules(modelstate, rules);
			}
			//TODO: NEED TO FIX THIS
			/*
			for(String species : modelstate.speciesIDSet)
			{


				if(modelstate.speciesToCompartmentNameMap.get(species).equals(variable))
					if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(species) &&
							modelstate.speciesToHasOnlySubstanceUnitsMap.get(species) == false) {
						speciesVal = modelstate.getVariableToValue(species);

						if(models.get(modelstate.model).getSpecies(species).isSetInitialConcentration())
							speciesVal = models.get(modelstate.model).getSpecies(species).getInitialConcentration();

						newResult = (speciesVal) * 
								modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(species));
						modelstate.setvariableToValueMap(species, newResult);
					}
			}
			 */
			return true;
		}

		return false;
	}

	private boolean calcSpeciesInitAssign(ModelState modelstate, String variable, InitialAssignment initialAssignment)
	{
		double newResult;
		if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
				modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {

			newResult = 
					evaluateExpressionRecursive(modelstate, initialAssignment.getMath()) * 
					modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable));
			if(newResult != modelstate.getVariableToValue(variable))
			{
				modelstate.setvariableToValueMap(variable, newResult);
				return true;
			}

		}
		else {
			newResult = evaluateExpressionRecursive(modelstate, initialAssignment.getMath());

			if(newResult != modelstate.getVariableToValue(variable))
			{
				modelstate.setvariableToValueMap(variable, newResult);
				return true;
			}
		}
		return false;
	}


	/**
	 * puts event-related information into data structures
	 */
	protected void setupEvents(ModelState modelstate) {

		//add event information to hashmaps for easy/fast access
		//this needs to happen after calculating initial propensities
		//so that the speciesToAffectedReactionSetMap is populated

		long size = models.get(modelstate.model).getEventCount();

		for (int i = 0; i < size; i++)
		{

			Event event = models.get(modelstate.model).getEvent(i);
			if(event.isSetId() && modelstate.isDeletedBySID(event.getId()))
				continue;
			else if(event.isSetMetaId() && modelstate.isDeletedByMetaID(event.getMetaId()))
				continue;
			setupSingleEvent(modelstate, event);
		}
	}

	protected void setupRules(ModelState modelstate)
	{


		//modelstate.numAssignmentRules = 0;
		//modelstate.numRateRules = 0;

		//NOTE: assignmentrules are performed in setupinitialassignments

		//loop through all assignment rules
		//store which variables (RHS) affect the rule variable (LHS)
		//so when those RHS variables change, we know to re-evaluate the rule
		//and change the value of the LHS variable
		long size = models.get(modelstate.model).getListOfRules().size();

		if(size > 0)
			modelstate.noRuleFlag = false;


		for(Rule rule : models.get(modelstate.model).getListOfRules())
		{


			if(rule.isSetMetaId() && modelstate.isDeletedByMetaID(rule.getMetaId()))
				continue;

			if (rule.isAssignment()) {

				//Rules don't have a getVariable method, so this needs to be cast to an ExplicitRule
				rule.setMath(inlineFormula(modelstate, rule.getMath()));
				AssignmentRule assignmentRule = (AssignmentRule) rule;

				//list of all children of the assignmentRule math
				ArrayList<ASTNode> formulaChildren = new ArrayList<ASTNode>();

				if (assignmentRule.getMath().getChildCount() == 0)
					formulaChildren.add(assignmentRule.getMath());
				else
					getAllASTNodeChildren(assignmentRule.getMath(), formulaChildren);

				for (ASTNode ruleNode : formulaChildren) {

					if (ruleNode.isName()) {

						String nodeName = ruleNode.getName();

						modelstate.variableToAffectedAssignmentRuleSetMap.put(nodeName, new HashSet<AssignmentRule>());
						modelstate.variableToAffectedAssignmentRuleSetMap.get(nodeName).add(assignmentRule);
						modelstate.variableToIsInAssignmentRuleMap.put(nodeName, true);
					}
				}

				//++numAssignmentRules;				
			}

			else if (rule.isRate()) {

				//Rules don't have a getVariable method, so this needs to be cast to an ExplicitRule
				RateRule rateRule = (RateRule) rule;			
				//String variable = rateRule.getVariable();

				modelstate.rateRulesList.add(rateRule);


				//++numRateRules;	
			}

		}


	}

	//STRING DOUBLE PAIR INNER CLASS	
	/**
	 * class to combine a string and a double
	 */
	protected class StringDoublePair {

		public String string;
		public double doub;

		StringDoublePair(String s, double d) {

			string = s;
			doub = d;
		}
	}

	//STRING STRING PAIR INNER CLASS	
	/**
	 * class to combine a string and a string
	 */
	protected class StringStringPair {

		public String string1;
		public String string2;

		StringStringPair(String s1, String s2) {

			string1 = s1;
			string2 = s2;
		}

	}

	//EVENT TO FIRE INNER CLASS
	/**
	 * easy way to store multiple data points for events that are firing
	 */
	protected class EventToFire {

		public String eventID = "";
		public HashSet<Object> eventAssignmentSet = null;
		public double fireTime = 0.0;
		public String modelID;

		public EventToFire(String modelID, String eventID, HashSet<Object> eventAssignmentSet, double fireTime) {

			this.eventID = eventID;
			this.eventAssignmentSet = eventAssignmentSet;
			this.fireTime = fireTime;	
			this.modelID = modelID;
		}
	}

	//EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the priority queue
	 */
	protected class EventComparator implements Comparator<EventToFire> {

		/**
		 * compares two events based on their fire times and priorities
		 */
		@Override
		public int compare(EventToFire event1, EventToFire event2) {

			if (event1.fireTime > event2.fireTime)
				return 1;
			else if (event1.fireTime < event2.fireTime)
				return -1;
			else {
				ModelState state1;
				ModelState state2;
				if(event1.modelID.equals("topmodel"))
					state1 = topmodel;
				else
					state1 = submodels.get(event1.modelID);
				if(event2.modelID.equals("topmodel"))
					state2 = topmodel;
				else
					state2 = submodels.get(event2.modelID);

				if (state1.eventToPriorityMap.get(event1.eventID) == null) {
					if (state2.eventToPriorityMap.get(event2.eventID) != null)
						return -1;
					if ((Math.random() * 100) > 50) {
						return -1;
					}
					return 1;
				}

				if (evaluateExpressionRecursive(state1, state1.eventToPriorityMap.get(event1.eventID)) >  
				evaluateExpressionRecursive(state2, state2.eventToPriorityMap.get(event2.eventID)))
					return -1;
				else if ( evaluateExpressionRecursive(state1, state1.eventToPriorityMap.get(event1.eventID)) <  
						evaluateExpressionRecursive(state2, state2.eventToPriorityMap.get(event2.eventID)))
					return 1;
				else {
					if ((Math.random() * 100) > 50) {
						return -1;
					}
					return 1;
				}
			}
		}
	}

	protected class ModelState
	{
		//protected Model model;
		protected String model;
		protected long numSpecies;
		protected long numParameters;
		protected long numReactions;
		protected int numInitialAssignments;
		protected int numRateRules;
		protected long numEvents;
		protected long numConstraints;
		protected long numRules;
		protected long numCompartments;
		protected String ID;
		protected boolean noEventsFlag = true;

		//allows for access to a propensity from a reaction ID
		protected TObjectDoubleHashMap<String> reactionToPropensityMap = null;

		//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
		//note that species and stoichiometries need to be thought of as unique for each reaction
		protected HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;

		//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
		protected HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;

		//allows for access to a kinetic formula tree from a reaction
		protected HashMap<String, ASTNode> reactionToFormulaMap = null;

		//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
		protected HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;

		//allows for access to species booleans from a species ID
		protected HashMap<String, Boolean> speciesToIsBoundaryConditionMap = null;
		protected HashMap<String, Boolean> speciesToHasOnlySubstanceUnitsMap = null;
		protected HashMap<String, String> speciesToCompartmentNameMap = null;

		//a linked (ordered) set of all species IDs, to allow easy access to their values via the variableToValue map
		protected LinkedHashSet<String> speciesIDSet = null;

		//allows for access to species and parameter values from a variable ID
		protected TObjectDoubleHashMap<String> variableToValueMap = null;

		protected HashMap<String, Boolean> variableToIsConstantMap = null;


		//hashmaps that allow for access to event information from the event's id
		protected HashMap<String, ASTNode> eventToPriorityMap = null;
		protected HashMap<String, ASTNode> eventToDelayMap = null;
		protected HashMap<String, Boolean> eventToHasDelayMap = null;
		protected HashMap<String, Boolean> eventToTriggerPersistenceMap = null;
		protected HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap = null;
		protected HashMap<String, ASTNode> eventToTriggerMap = null;
		protected HashMap<String, Boolean> eventToTriggerInitiallyTrueMap = null;
		protected HashMap<String, Boolean> eventToPreviousTriggerValueMap = null;
		protected HashMap<String, HashSet<Object> > eventToAssignmentSetMap = null;
		protected HashMap<String, HashSet<String> > variableToEventSetMap = null;

		//allows for access to the reactions whose propensity changes when an event fires
		protected HashMap<String, HashSet<String> > eventToAffectedReactionSetMap = null;

		protected HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();

		//propensity variables
		protected double propensity = 0.0;
		protected double minPropensity = Double.MAX_VALUE / 10.0;
		protected double maxPropensity = Double.MIN_VALUE / 10.0;

		//file writing variables
		protected boolean noConstraintsFlag = true;
		protected boolean noRuleFlag = true;

		//stores events in order of fire time and priority
		protected PriorityQueue<EventToFire> triggeredEventQueue;
		protected HashSet<String> untriggeredEventSet = null;


		//allows for access to the set of constraints that a variable affects
		protected HashMap<String, HashSet<ASTNode> > variableToAffectedConstraintSetMap = null;

		protected HashMap<String, Boolean> variableToIsInConstraintMap = null;

		//allows to access to whether or not a variable is in an assignment or rate rule rule (RHS)
		protected HashMap<String, Boolean> variableToIsInAssignmentRuleMap = null;
		protected HashMap<String, Boolean> variableToIsInRateRuleMap = null;

		//allows for access to the set of assignment rules that a variable (rhs) in an assignment rule affects
		protected HashMap<String, HashSet<AssignmentRule>> variableToAffectedAssignmentRuleSetMap = null;
		protected LinkedHashSet<String> nonconstantParameterIDSet;
		protected HashMap<String, HashSet<StringStringPair> > reactionToNonconstantStoichiometriesSetMap = null;


		//protected TObjectDoubleHashMap<String> speciesToCompartmentSizeMap = null;
		protected LinkedHashSet<String> compartmentIDSet = new LinkedHashSet<String>();

		protected List<RateRule> rateRulesList = new LinkedList<RateRule>();

		protected HashSet<String> nonConstantStoichiometry;

		protected HashSet<String> isHierarchical = null;

		protected HashMap<String, String> replacementDependency = null;

		protected HashSet<String> variablesToPrint; 
		protected HashSet<String> deletedElementsById; 
		protected HashSet<String> deletedElementsByMetaId; 
		protected HashSet<String> deletedElementsByUId; 

		public ModelState(String bioModel, String submodelID)
		{
			this.model = bioModel;
			this.ID = submodelID;

			setCountVariables(models.get(model));

			ibiosimFunctionDefinitions.add("uniform");
			ibiosimFunctionDefinitions.add("exponential");
			ibiosimFunctionDefinitions.add("gamma");
			ibiosimFunctionDefinitions.add("chisq");
			ibiosimFunctionDefinitions.add("lognormal");
			ibiosimFunctionDefinitions.add("laplace");
			ibiosimFunctionDefinitions.add("cauchy");
			ibiosimFunctionDefinitions.add("poisson");
			ibiosimFunctionDefinitions.add("binomial");
			ibiosimFunctionDefinitions.add("bernoulli");
			ibiosimFunctionDefinitions.add("normal");

			//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
			speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
			speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
			variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
			speciesToCompartmentNameMap = new HashMap<String, String>((int) numSpecies);
			speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
			variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);

			reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
			reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
			reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
			reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));


			variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int)numConstraints);		
			variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));

			nonConstantStoichiometry = new HashSet<String>();

			if (numEvents > 0) {
				noEventsFlag = false;
				triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
				untriggeredEventSet = new HashSet<String>((int) numEvents);
				eventToPriorityMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
				eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
				eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) numEvents);
				eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
				eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
				eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
				eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
				eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) numEvents);
				variableToEventSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
			}

			if (numRules > 0) {

				variableToAffectedAssignmentRuleSetMap = new HashMap<String, HashSet<AssignmentRule> >((int) numRules);
				variableToIsInAssignmentRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
				variableToIsInRateRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
			}
			nonconstantParameterIDSet = new LinkedHashSet<String>();
			reactionToNonconstantStoichiometriesSetMap = new HashMap<String, HashSet<StringStringPair> >();

			isHierarchical = new HashSet<String>();

			replacementDependency = new HashMap<String, String>();

			variablesToPrint = new HashSet<String>();

			deletedElementsById = new HashSet<String>();
			deletedElementsByMetaId = new HashSet<String>();
			deletedElementsByUId = new HashSet<String>();

		}

		protected void clear()
		{
			speciesToAffectedReactionSetMap.clear();
			speciesToIsBoundaryConditionMap.clear();
			variableToIsConstantMap.clear();
			speciesToHasOnlySubstanceUnitsMap.clear();
			speciesToCompartmentNameMap.clear();
			speciesIDSet.clear();
			variableToValueMap.clear();
			noConstraintsFlag = true;
			reactionToPropensityMap.clear();
			reactionToSpeciesAndStoichiometrySetMap.clear();
			reactionToReactantStoichiometrySetMap.clear();
			reactionToFormulaMap.clear();

			propensity = 0.0;
			minPropensity = Double.MAX_VALUE / 10.0;
			maxPropensity = Double.MIN_VALUE / 10.0;
		}

		protected void setCountVariables(Model model)
		{
			this.numSpecies = model.getSpeciesCount();
			this.numParameters = model.getParameterCount();
			this.numReactions = model.getReactionCount();
			this.numInitialAssignments = model.getInitialAssignmentCount();

			this.numEvents = model.getEventCount();
			this.numRules = model.getRuleCount();
			this.numConstraints= model.getConstraintCount();
			this.numCompartments = model.getCompartmentCount();
		}
		protected double getVariableToValue(String variable)
		{
			//if(replacements.containsKey(variable) && replacementSubModels.get(variable).contains(this.ID))
			//	return replacements.get(variable);
			//else
			if(isHierarchical.contains(variable))
			{
				String dep = replacementDependency.get(variable);
				return replacements.get(dep);
			}
			return variableToValueMap.get(variable);
		}

		protected void setvariableToValueMap(String variable, double value)
		{
			if(isHierarchical.contains(variable))
			{
				String dep = replacementDependency.get(variable);
				replacements.put(dep, value);
			}
			variableToValueMap.put(variable, value);
		}

		protected boolean isDeletedBySID(String sid)
		{
			if(deletedElementsById.contains(sid))
				return true;
			return false;
		}


		protected boolean isDeletedByMetaID(String metaid)
		{
			if(deletedElementsByMetaId.contains(metaid))
				return true;
			return false;
		}

		protected boolean isDeletedByUID(String uid)
		{
			if(deletedElementsByUId.contains(uid))
				return true;
			return false;
		}

		
	}
	
}



