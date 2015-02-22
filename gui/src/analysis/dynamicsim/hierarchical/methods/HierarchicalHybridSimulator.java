package analysis.dynamicsim.hierarchical.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Quantity;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;

import analysis.dynamicsim.hierarchical.HierarchicalArrayModels;
import analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.util.Evaluator;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringPair;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

public class HierarchicalHybridSimulator implements HierarchicalSimulation
{

	private final SBMLDocument								document;
	private final String									rootDirectory, outputDirectory,
			quantityType, abstraction;
	private String											separator;
	private final JProgressBar								progress;
	private final double									timeLimit, maxTimeStep, minTimeStep,
			printInterval, stoichAmpValue;
	private double											currentTime;
	private final JFrame									running;
	private final String[]									interestingSpecies;
	private BufferedWriter									bufferedTSDWriter;
	private final List<HierarchicalArrayModels>				sims;
	private final Map<String, Double>						values;
	private final Map<String, ASTNode>						listOfAssignmentRules;
	private final Map<String, List<HierarchicalStringPair>>	quantityToReplaces;
	private final Map<String, HierarchicalStringPair>		quantityToReplacedBy;
	private final Map<String, Integer>						idToIndex;
	private final Map<Integer, String>						indexToId;
	private final Map<String, Model>						idToModel;
	private FileWriter										tsdWriter;

	public HierarchicalHybridSimulator(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue,
			JFrame running, String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		this.rootDirectory = rootDirectory;
		this.outputDirectory = outputDirectory;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.stoichAmpValue = stoichAmpValue;
		this.running = running;
		this.interestingSpecies = interestingSpecies;
		this.quantityType = quantityType;
		this.abstraction = abstraction;
		this.document = SBMLReader.read(new File(SBMLFileName));
		this.sims = new ArrayList<HierarchicalArrayModels>();
		this.values = new HashMap<String, Double>();
		this.quantityToReplaces = new HashMap<String, List<HierarchicalStringPair>>();
		this.quantityToReplacedBy = new HashMap<String, HierarchicalStringPair>();
		this.listOfAssignmentRules = new HashMap<String, ASTNode>();
		this.idToModel = new HashMap<String, Model>();
		this.idToIndex = new HashMap<String, Integer>();
		this.indexToId = new HashMap<Integer, String>();
		this.currentTime = 0;

		if (File.separator.equals("\\"))
		{
			separator = "\\\\";
		}
		else
		{
			separator = File.separator;
		}

		initialize(randomSeed, 1);
	}

	private void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(
				CompConstants.namespaceURI);
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document
				.getPlugin(CompConstants.namespaceURI);

		for (Submodel submodel : sbmlCompModel.getListOfSubmodels())
		{
			if (sbmlComp.getListOfExternalModelDefinitions() != null
					&& sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
			{
				String source = sbmlComp.getListOfExternalModelDefinitions()
						.get(submodel.getModelRef()).getSource().replace("file:", "");

				String extDef = rootDirectory + separator + source;

				idToIndex.put(submodel.getId(), sims.size());
				indexToId.put(sims.size(), submodel.getId());

				SBMLDocument subDoc = SBMLReader.read(new File(extDef));
				idToModel.put(submodel.getId(), subDoc.getModel());
				sims.add(new HierarchicalSSADirectSimulator(extDef, rootDirectory, outputDirectory,
						timeLimit, maxTimeStep, minTimeStep, randomSeed, progress, printInterval,
						stoichAmpValue, running, interestingSpecies, quantityType, abstraction,
						false));

			}

		}

		setupSpecies(document.getModel());
		setupParameters(document.getModel());
		setupAssignmentRules(document.getModel());
		setupForOutput(randomSeed, runNumber);
		printSpeciesToTSD();

	}

	@Override
	public void simulate()
	{

		while (currentTime <= timeLimit)
		{
			performAssignmentRules();

			for (HierarchicalArrayModels sim : sims)
			{
				sim.setTimeLimit(currentTime + 1);
				sim.simulate();
			}

			update();
			try
			{
				printValueToTSD(currentTime);
			}
			catch (IOException e)
			{
				return;
			}
			currentTime = currentTime + 1;
		}

		try
		{
			bufferedTSDWriter.write(')');
			bufferedTSDWriter.flush();
		}
		catch (IOException e)
		{
		}

	}

	@Override
	public void cancel()
	{

	}

	@Override
	public void clear()
	{

	}

	@Override
	public void setupForNewRun(int newRun)
	{

	}

	private void performAssignmentRules()
	{
		boolean changed = true;

		while (changed)
		{
			changed = false;

			for (String key : listOfAssignmentRules.keySet())
			{
				double oldValue = values.get(key);
				double newValue = Evaluator.evaluate(listOfAssignmentRules.get(key), values);

				if (oldValue != newValue)
				{
					values.put(key, newValue);
					changed = true;
				}
			}
		}
	}

	private void update()
	{
		for (String species : values.keySet())
		{
			HierarchicalStringPair replacedBy = quantityToReplacedBy.get(species);
			List<HierarchicalStringPair> replaces = quantityToReplaces.get(species);

			if (replacedBy != null)
			{
				String submodel = replacedBy.string1;
				String variable = replacedBy.string2;
				int index = idToIndex.get(submodel);
				values.put(species, sims.get(index).getTopLevelValue(variable));
			}

			if (replaces != null)
			{
				for (HierarchicalStringPair pair : replaces)
				{
					String submodel = pair.string1;
					String variable = pair.string2;
					double value = values.get(species);
					int index = idToIndex.get(submodel);
					sims.get(index).setTopLevelValue(variable, value);
				}
			}

		}
	}

	private void setupAssignmentRules(Model model)
	{
		for (Rule rule : model.getListOfRules())
		{
			if (rule.isAssignment())
			{
				AssignmentRule aRule = (AssignmentRule) rule;
				ASTNode math = inlineFormula(aRule.getMath());
				listOfAssignmentRules.put(aRule.getVariable(), math);
			}
		}
	}

	private void setupSpecies(Model model)
	{

		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(
				CompConstants.namespaceURI);

		for (Species species : model.getListOfSpecies())
		{
			setupReplacement(species, sbmlCompModel);
			values.put(species.getId(), species.getValue());
		}
	}

	private void setupParameters(Model model)
	{

		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(
				CompConstants.namespaceURI);

		for (Parameter parameter : model.getListOfParameters())
		{
			setupReplacement(parameter, sbmlCompModel);
			values.put(parameter.getId(), parameter.getValue());
		}
	}

	private void setupReplacement(Quantity quantity, CompModelPlugin sbmlCompModel)
	{
		if (sbmlCompModel == null)
		{
			return;
		}

		CompSBasePlugin sbmlSBase = (CompSBasePlugin) quantity
				.getExtension(CompConstants.namespaceURI);

		String id = quantity.getId();

		if (sbmlSBase != null)
		{
			if (sbmlSBase.getListOfReplacedElements() != null)
			{
				for (ReplacedElement element : sbmlSBase.getListOfReplacedElements())
				{
					String submodel = element.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin) idToModel.get(submodel).getPlugin(
							CompConstants.namespaceURI);
					if (element.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
						String replacing = port.getIdRef();
						if (!quantityToReplaces.containsKey(id))
						{
							quantityToReplaces.put(id, new ArrayList<HierarchicalStringPair>());
						}
						quantityToReplaces.get(id).add(
								new HierarchicalStringPair(submodel, replacing));
					}
				}
			}

			if (sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				String submodel = replacement.getSubmodelRef();
				sbmlCompModel = (CompModelPlugin) idToModel.get(submodel).getPlugin(
						CompConstants.namespaceURI);
				if (replacement.isSetPortRef())
				{
					Port port = sbmlCompModel.getListOfPorts().get(replacement.getPortRef());
					String replacedBy = port.getIdRef();
					quantityToReplacedBy.put(id, new HierarchicalStringPair(submodel, replacedBy));
				}
			}
		}
	}

	private void printSpeciesToTSD() throws IOException
	{
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

		for (int i = 0; i < sims.size(); i++)
		{
			HierarchicalArrayModels sim = sims.get(i);
			String submodel = indexToId.get(i);
			for (String speciesID : sim.getTopmodel().getSpeciesIDSet())
			{
				bufferedTSDWriter.write(",\"" + submodel + "_" + speciesID + "\"");
			}
		}

		bufferedTSDWriter.write("),\n");
	}

	private void printValueToTSD(double printTime) throws IOException
	{
		String commaSpace = "";

		bufferedTSDWriter.write("(");

		// print the current time
		bufferedTSDWriter.write(printTime + ",");

		// loop through the speciesIDs and print their current value to the file

		for (int i = 0; i < sims.size(); i++)
		{
			HierarchicalArrayModels sim = sims.get(i);

			for (String speciesID : sim.getTopmodel().getSpeciesIDSet())
			{

				bufferedTSDWriter.write(commaSpace
						+ sim.getTopmodel().getVariableToValue(sim.getReplacements(), speciesID));
				commaSpace = ",";

			}
		}

		bufferedTSDWriter.write(")\n");
	}

	private void setupForOutput(long randomSeed, int currentRun)
	{
		try
		{
			String extension = ".tsd";
			tsdWriter = new FileWriter(outputDirectory + "run-" + currentRun + extension);
			bufferedTSDWriter = new BufferedWriter(tsdWriter);
			bufferedTSDWriter.write('(');

		}
		catch (IOException e)
		{
		}
	}

	private ASTNode inlineFormula(ASTNode formula)
	{

		Model model = document.getModel();
		if (formula.isFunction() == false || formula.isLeaf() == false)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(formula.getChild(i)));// .clone()));
			}
		}

		if (formula.isFunction()
				&& document.getModel().getFunctionDefinition(formula.getName()) != null)
		{

			ASTNode inlinedFormula = model.getFunctionDefinition(formula.getName()).getBody()
					.clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName())
						.getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);
				if ((child.getChildCount() == 0) && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(),
							oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

}
