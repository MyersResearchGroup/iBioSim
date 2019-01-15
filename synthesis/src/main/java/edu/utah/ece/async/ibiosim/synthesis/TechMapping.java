package edu.utah.ece.async.ibiosim.synthesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLFileManager;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.SynthesisGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping.Synthesizer;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SynthesisNode;

public class TechMapping {

	public static void main(String[] args) 
	{
		String sbolLibDir = "";
		String sbmlLibDir = "";
		String libDirectory = "";
		String specFile = "";
		String outFileName = "";
		String defaultPrefix = "";

		boolean sbmlTechMap = false;
		boolean sbolTechMap = false;
		boolean outSBML = false;
		boolean outSBOL = false;

		boolean greedySol = false;
		boolean exactSol = false;
		boolean branch_bound = false;
		
		boolean createDotFile = false;

		int num_sol = 0;
		int index = 0;

		for(; index< args.length; index++)
		{
			String flag = args[index];
			switch(flag)
			{
			case "-sbolLD":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				sbolLibDir = args[++index];
				break;
			case "-sbmlLD":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				sbmlLibDir = args[++index];
				break;
			case "-ld":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				libDirectory = args[++index];
				break;
			case "-sf":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				specFile = args[++index];
				break;
			case "-sVal":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				num_sol = Integer.parseInt(args[++index]);
				break;
			case "-o":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				outFileName = args[++index];
				break;
			case "-bs":
				branch_bound = true;
				break;
			case "-es":
				exactSol = true;
				break;
			case "-gs":
				greedySol = true;
				break;
			case "-sbml":
				sbmlTechMap = true;
				break;
			case "-sbol":
				sbolTechMap = true;
				break;
			case "-osbml":
				outSBML = true;
				break;
			case "-osbol":
				outSBOL = true;
				break;
			case "-p":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				defaultPrefix = args[++index];
				break;
			case "-dot":
				if(index+1 >= args.length || args[index+1].equals("-"))
				{
					usage();
				}
				createDotFile = true;
				break;
			default:
				usage();
			}
		}


		try 
		{
			if(sbolTechMap)
			{
				SBOLTechMapOptions setupOpts = new SBOLTechMapOptions();
				setupOpts.setLibraryFile(sbolLibDir);
				setupOpts.setSpecificationFile(specFile);
				
				Synthesis syn = SBOLTechMap.runSBOLTechMap(setupOpts.getSpeficationFile(), setupOpts.getLibraryFile());
				SBOLDocument sbolDoc_sol = syn.getSBOLfromTechMapping();
				if(outSBOL)
				{
					sbolDoc_sol.write(new File(outFileName));
				}
				else if (outSBML)
				{
					// TODO: Write solution to one SBML file
					for (ModuleDefinition moduleDef : sbolDoc_sol.getRootModuleDefinitions())
					{
//						List<BioModel> models = SBOL2SBML.generateModel(outFileName, moduleDef, sbolDoc_sol);
//						SBMLutilities.exportSBMLModels(models, outputDir, outFileName, false, true, true);
					}
				}
				else
				{
					// Write to command line
					sbolDoc_sol.write(System.out);
				}
			}
			else if (sbmlTechMap)
			{
				Set<String> sbolLibFiles = new HashSet<String>();
				for(String sbolGate : new File(sbolLibDir).list())
				{
					sbolLibFiles.add(sbolGate);
				}
				SBOLFileManager fileManager = new SBOLFileManager(sbolLibFiles, defaultPrefix);

				// Create a gate graph for each SBML library file that was read in
				Set<SynthesisGraph> graphlibrary = new HashSet<SynthesisGraph>();

				for (String gateFileID : new File(sbmlLibDir).list()) 
				{
					BioModel gateModel = new BioModel(libDirectory);
					try 
					{
						gateModel.load(gateFileID);
					} 
					catch (XMLStreamException e) 
					{
						System.err.println("ERROR: Unable to load SBML file(s) needed for library gates.");
						e.printStackTrace();
					} 
					catch (BioSimException e) 
					{
						System.err.println("ERROR: " + e.getMessage());
						e.printStackTrace();
					}
					graphlibrary.add(new SynthesisGraph(gateModel, fileManager));
				}

				String specFileName = new File(specFile).getName();
				BioModel specModel = new BioModel(specFile); 
				SynthesisGraph spec = new SynthesisGraph(specModel, fileManager);

				Properties synthProps = createDefaultSynthesisProperties(specFileName);
				Synthesizer synthesizer = new Synthesizer(graphlibrary, synthProps);
				List<List<SynthesisGraph>> solutions = synthesizer.mapSpecification(spec);
				List<String> solutionFileIDs;
				
				String synthFilePath = specFileName;
				try 
				{
					solutionFileIDs = importSolutions(solutions, spec, fileManager, synthFilePath, specFileName);
				} 
				catch (XMLStreamException e) 
				{
					System.err.println("ERROR: Invalid XML file.");
					e.printStackTrace();
				} 
				catch (BioSimException e) 
				{
					System.err.println("ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("ERROR: Unable to locate input file(s).");
			e.printStackTrace();
		} 
		catch (SBOLException e) 
		{
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (SBOLValidationException e) 
		{
			System.err.println("ERROR: Failed SBOL Validation when converting SBOL Tech. Map solution into SBOL data model.");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Unable to read or write SBOL file");
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			System.err.println("ERROR: Unable to convert input file to SBOL data format.");
			e.printStackTrace();
		} catch (SBOLTechMapException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Get default fields to set the Synthesis View panel by storing the fields in a property file.
	 * @param specFileID - ID of specification file
	 * @return 
	 */
	public static Properties createDefaultSynthesisProperties(String specFileID) 
	{
		Properties synthProps = new Properties();
		Preferences prefs = Preferences.userRoot();
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY, specFileID);
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_LIBS_PREFERENCE, ""));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY,
				prefs.get(GlobalConstants.SBOL_SYNTH_METHOD_PREFERENCE, 
						GlobalConstants.SBOL_SYNTH_EXHAUST_BB));
		synthProps.setProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY, 
				prefs.get(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PREFERENCE, "1"));
		return synthProps;
	}

	public static boolean compareModels(BioModel subModel1, BioModel subModel2) 
	{
		String hash1 = edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility.MD5(subModel1.getSBMLDocument());
		String hash2 = edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility.MD5(subModel2.getSBMLDocument());
		return hash1 == hash2;
	}

	public static List<String> importSolutions(List<List<SynthesisGraph>> solutions, SynthesisGraph spec, 
			SBOLFileManager fileManager, String synthFilePath, String specFile) throws XMLStreamException, IOException, BioSimException, SBOLValidationException, SBOLConversionException
	{
		List<BioModel> solutionModels = new LinkedList<BioModel>();
		Set<String> solutionFileIDs = new HashSet<String>();

		for (List<SynthesisGraph> solutionGraphs : solutions) 
		{
			solutionFileIDs.addAll(importSolutionSubModels(solutionGraphs, synthFilePath, specFile));
			solutionFileIDs.add(importSolutionDNAComponents(solutionGraphs, fileManager, synthFilePath, specFile));
		}

		int idIndex = 0;
		for (List<SynthesisGraph> solutionGraphs : solutions) 
		{
			System.out.println(solutionGraphs.toString());
			BioModel solutionModel = new BioModel(synthFilePath);
			solutionModel.createSBMLDocument("tempID_" + idIndex, false, false);	
			idIndex++;
			Synthesizer.composeSolutionModel(solutionGraphs, spec, solutionModel);
			solutionModels.add(solutionModel);
		}

		List<String> orderedSolnFileIDs = new LinkedList<String>();
		idIndex = 0;
		for (BioModel solutionModel : solutionModels) 
		{
			String solutionID = spec.getModelFileID().replace(".xml", "");
			do 
			{
				idIndex++;
			} 
			while (solutionFileIDs.contains(solutionID + "_" + idIndex + ".xml"));

			solutionModel.setSBMLFile(solutionID + "_" + idIndex + ".xml");
			solutionModel.getSBMLDocument().getModel().setId(solutionID + "_" + idIndex);

			solutionModel.save(synthFilePath + File.separator + solutionID + "_" + idIndex + ".xml");

			orderedSolnFileIDs.add(solutionID + "_" + idIndex + ".xml");
		}
		orderedSolnFileIDs.addAll(solutionFileIDs);
		return orderedSolnFileIDs;
	}

	private static Set<String> importSolutionSubModels(List<SynthesisGraph> solutionGraphs, String synthFilePath, String specFile) throws XMLStreamException, IOException, BioSimException 
	{
		HashMap<String, SynthesisGraph> solutionFileToGraph = new HashMap<String, SynthesisGraph>();
		Set<String> clashingFileIDs = new HashSet<String>();

		for (SynthesisGraph solutionGraph : solutionGraphs) 
		{
			BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());

			solutionSubModel.load(solutionGraph.getModelFileID());
			if (solutionFileToGraph.containsKey(solutionGraph.getModelFileID())) 
			{
				SynthesisGraph clashingGraph = solutionFileToGraph.get(solutionGraph.getModelFileID());
				BioModel clashingSubModel = new BioModel(clashingGraph.getProjectPath());

				clashingSubModel.load(clashingGraph.getModelFileID());

				if (!compareModels(solutionSubModel, clashingSubModel)) 
				{
					clashingFileIDs.add(solutionGraph.getModelFileID());
					solutionFileToGraph.remove(solutionGraph.getModelFileID());
					solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
							solutionGraph);
					solutionFileToGraph.put(flattenProjectIntoModelFileID(clashingSubModel, solutionFileToGraph.keySet()), 
							clashingGraph);
				}
			} 
			else if (clashingFileIDs.contains(solutionGraph.getModelFileID())) 
				solutionFileToGraph.put(flattenProjectIntoModelFileID(solutionSubModel, solutionFileToGraph.keySet()), 
						solutionGraph);
			else
				solutionFileToGraph.put(solutionGraph.getModelFileID(), solutionGraph);
		}

		for (String subModelFileID : solutionFileToGraph.keySet()) 
		{
			SynthesisGraph solutionGraph = solutionFileToGraph.get(subModelFileID);
			BioModel solutionSubModel = new BioModel(solutionGraph.getProjectPath());
			solutionSubModel.load(solutionGraph.getModelFileID());
			solutionSubModel.getSBMLDocument().getModel().setId(subModelFileID.replace(".xml", ""));
			solutionSubModel.save(synthFilePath + File.separator + subModelFileID);
			solutionGraph.setModelFileID(subModelFileID);
		}

		return solutionFileToGraph.keySet();
	}

	private static String flattenProjectIntoModelFileID(BioModel biomodel, Set<String> modelFileIDs) 
	{
		String[] splitPath = biomodel.getPath().split(File.separator);
		String flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
		int fileIndex = 0;
		while (modelFileIDs.contains(flatModelFileID)) 
		{
			fileIndex++;
			flatModelFileID = splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile().replace(".xml", "") 
					+ "_" + fileIndex + ".xml";
		}
		return splitPath[splitPath.length - 1] + "_" + biomodel.getSBMLFile();
	}

	private static String importSolutionDNAComponents(List<SynthesisGraph> solutionGraphs, SBOLFileManager fileManager, 
			String synthFilePath, String specFile) throws SBOLValidationException, FileNotFoundException, SBOLException, SBOLConversionException 
	{
		Set<URI> compURIs = new HashSet<URI>();
		for (SynthesisGraph solutionGraph : solutionGraphs) 
		{
			for (URI compURI : solutionGraph.getCompURIs())
				if (!compURIs.contains(compURI))
					compURIs.add(compURI);
		}
		String sbolFileID = specFile.replace(".xml", GlobalConstants.SBOL_FILE_EXTENSION);

		SBOLFileManager.saveDNAComponents(fileManager.resolveURIs(new LinkedList<URI>(compURIs)), 
				synthFilePath + File.separator + sbolFileID);
		return sbolFileID;
	}


	private static void usage() 
	{
		System.err.println("Required:");
		System.err.println("-lf full path to the library file containing set of genetic gates needed for technology mapping.");
		System.err.println("-sf full path to the specification file to performing technology mapping.");
		System.err.println("-sbml perform SBML Technology Mapping");
		System.err.println("-sbol perform SBOL Technology Mapping");
		System.err.println("-o name of output file along with full path of where output file will be written to.");
		System.err.println("-osbml produce solution for technology mapping in SBML format");
		System.err.println("-osbol produce solution for technology mapping in SBOL format");

		System.err.println("Options:");
		System.err.println("-p SBOL URI prefix needed to set the SBOLDocument when converting the technology mapping solution to the desired SBOL or SBML data format.");
		System.err.println("-ld: directory to multiple SBOL or SBML library files");
		System.err.println("-dot produced SBOL technology mapping solution in dot format.");
	}



}
