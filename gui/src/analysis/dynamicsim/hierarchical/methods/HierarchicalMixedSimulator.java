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

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Quantity;
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

import analysis.dynamicsim.ParentSimulator;
import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public final class HierarchicalMixedSimulator implements ParentSimulator
{

	private final SBMLDocument								document;
	private final String									rootDirectory, outputDirectory, quantityType, abstraction;
	private String											separator;
	private final JProgressBar								progress;
	private final double									timeLimit, maxTimeStep, minTimeStep, printInterval, stoichAmpValue;
	private double											currentTime;
	private final JFrame									running;
	private final String[]									interestingSpecies;
	private BufferedWriter									bufferedTSDWriter;
	private final List<HierarchicalSimulation>				sims;
	private final Map<String, Double>						values;
	//private final Map<String, ASTNode>						listOfAssignmentRules;
	private final Map<String, List<HierarchicalStringPair>>	quantityToReplaces;
	private final Map<String, HierarchicalStringPair>		quantityToReplacedBy;
	private final Map<String, Integer>						idToIndex;
	private final Map<Integer, String>						indexToId;
	private final Map<String, Model>						idToModel;
	private FileWriter										tsdWriter;

	public HierarchicalMixedSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep,
			double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction) throws IOException, XMLStreamException
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
		this.sims = new ArrayList<HierarchicalSimulation>();
		this.values = new HashMap<String, Double>();
		this.quantityToReplaces = new HashMap<String, List<HierarchicalStringPair>>();
		this.quantityToReplacedBy = new HashMap<String, HierarchicalStringPair>();
		//this.listOfAssignmentRules = new HashMap<String, ASTNode>();
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
		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(CompConstants.namespaceURI);
		CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getPlugin(CompConstants.namespaceURI);

		for (Submodel submodel : sbmlCompModel.getListOfSubmodels())
		{
			if (sbmlComp.getListOfExternalModelDefinitions() != null
					&& sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
			{
				String source = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource().replace("file:", "");

				String extDef = rootDirectory + separator + source;

				idToIndex.put(submodel.getId(), sims.size());
				indexToId.put(sims.size(), submodel.getId());

				SBMLDocument subDoc = SBMLReader.read(new File(extDef));
				idToModel.put(submodel.getId(), subDoc.getModel());
				sims.add(new HierarchicalSSADirectSimulator(extDef, rootDirectory, outputDirectory, 1, timeLimit, maxTimeStep, minTimeStep,
						randomSeed, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, false));

			}

		}

		setupSpecies(document.getModel());
		setupParameters(document.getModel());
		setupForOutput(randomSeed, runNumber);
		printSpeciesToTSD();

	}

	@Override
	public void simulate()
	{

		while (currentTime <= timeLimit)
		{
			for (HierarchicalSimulation sim : sims)
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

	private void update()
	{
		for (String species : values.keySet())
		{
			HierarchicalStringPair replacedBy = quantityToReplacedBy.get(species);
			List<HierarchicalStringPair> replaces = quantityToReplaces.get(species);

			if (replacedBy != null)
			{
				String submodel = replacedBy.string1;
				idToIndex.get(submodel);
			}

			if (replaces != null)
			{
				for (HierarchicalStringPair pair : replaces)
				{
					String submodel = pair.string1;
					values.get(species);
					idToIndex.get(submodel);
				}
			}

		}
	}

	private void setupSpecies(Model model)
	{

		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(CompConstants.namespaceURI);

		for (Species species : model.getListOfSpecies())
		{
			setupReplacement(species, sbmlCompModel);
			values.put(species.getId(), species.getValue());
		}
	}

	private void setupParameters(Model model)
	{

		CompModelPlugin sbmlCompModel = (CompModelPlugin) document.getModel().getPlugin(CompConstants.namespaceURI);

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

		CompSBasePlugin sbmlSBase = (CompSBasePlugin) quantity.getExtension(CompConstants.namespaceURI);

		String id = quantity.getId();

		if (sbmlSBase != null)
		{
			if (sbmlSBase.getListOfReplacedElements() != null)
			{
				for (ReplacedElement element : sbmlSBase.getListOfReplacedElements())
				{
					String submodel = element.getSubmodelRef();
					sbmlCompModel = (CompModelPlugin) idToModel.get(submodel).getPlugin(CompConstants.namespaceURI);
					if (element.isSetPortRef())
					{
						Port port = sbmlCompModel.getListOfPorts().get(element.getPortRef());
						String replacing = port.getIdRef();
						if (!quantityToReplaces.containsKey(id))
						{
							quantityToReplaces.put(id, new ArrayList<HierarchicalStringPair>());
						}
						quantityToReplaces.get(id).add(new HierarchicalStringPair(submodel, replacing));
					}
				}
			}

			if (sbmlSBase.isSetReplacedBy())
			{
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				String submodel = replacement.getSubmodelRef();
				sbmlCompModel = (CompModelPlugin) idToModel.get(submodel).getPlugin(CompConstants.namespaceURI);
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

		for (String species : values.keySet())
		{
			bufferedTSDWriter.write(",\"" + species + "\"");
		}

		for (int i = 0; i < sims.size(); i++)
		{
			// HierarchicalSimulation sim = sims.get(i);
			// String submodel = indexToId.get(i);
			// for (String speciesID : sim.getTopmodel().getSpeciesIDSet())
			// {
			// bufferedTSDWriter.write(",\"" + submodel + "_" + speciesID +
			// "\"");
			// }
		}

		bufferedTSDWriter.write("),\n");
	}

	private void printValueToTSD(double printTime) throws IOException
	{
		bufferedTSDWriter.write("(");

		// print the current time
		bufferedTSDWriter.write(printTime + ",");

		// loop through the speciesIDs and print their current value to the file

		for (String species : values.keySet())
		{
			bufferedTSDWriter.write(",\"" + values.get(species) + "\"");
		}

		for (int i = 0; i < sims.size(); i++)
		{
			// HierarchicalSimulation sim = sims.get(i);
			//
			// for (String speciesID : sim.getTopmodel().getSpeciesIDSet())
			// {
			//
			// bufferedTSDWriter.write(commaSpace +
			// sim.getTopmodel().getVariableToValue(sim.getReplacements(),
			// speciesID));
			// commaSpace = ",";
			//
			// }
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

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

}
