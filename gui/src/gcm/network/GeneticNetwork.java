 package gcm.network;

import gcm.gui.Grid;
import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.util.Utility;
import gcm.visitor.AbstractPrintVisitor;
import gcm.visitor.PrintActivatedBindingVisitor;
import gcm.visitor.PrintActivatedProductionVisitor;
import gcm.visitor.PrintComplexVisitor;
import gcm.visitor.PrintDecaySpeciesVisitor;
import gcm.visitor.PrintRepressionBindingVisitor;
import gcm.visitor.PrintSpeciesVisitor;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lpn.parser.LhpnFile;
import lpn.parser.Translator;
import main.Gui;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

import sbmleditor.SBMLutilities;
import util.MutableString;



/**
 * This class represents a genetic network
 * 
 * @author Nam
 * 
 */
public class GeneticNetwork {
	
	//private String separator;
	
	/**
	 * Constructor
	 * 
	 * @param species
	 *            a hashmap of species
	 * @param stateMap
	 *            a hashmap of statename to species name
	 * @param promoters
	 *            a hashmap of promoters
	 */
	public GeneticNetwork(HashMap<String, SpeciesInterface> species,
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap,
			HashMap<String, Promoter> promoters) {
		this(species, complexMap, partsMap, promoters, null);
	}
	
	/**
	 * Constructor 
	 */
	public GeneticNetwork() {
		/*
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		*/
	}

	/**
	 * Constructor
	 * 
	 * @param species
	 *            a hashmap of species
	 * @param stateMap
	 *            a hashmap of statename to species name
	 * @param promoters
	 *            a hashmap of promoters
	 * @param gcm
	 *            a gcm file containing extra information
	 */
	public GeneticNetwork(HashMap<String, SpeciesInterface> species, 
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap, 
			HashMap<String, Promoter> promoters, GCMFile gcm) {
		/*
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		*/
		this.species = species;
		interestingSpecies = new String[species.size()];
		this.promoters = promoters;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.properties = gcm;
		this.compartments = gcm.getCompartments();
		
		AbstractPrintVisitor.setGCMFile(gcm);
		
		buildComplexes();
		buildComplexInfluences();
	}
	
	public void buildTemplate(HashMap<String, SpeciesInterface> species,			
			HashMap<String, Promoter> promoters, String gcm, String filename) {
		
		GCMFile file = new GCMFile(currentRoot);
		file.load(currentRoot+gcm);
		AbstractPrintVisitor.setGCMFile(file);
		setSpecies(species);
		setPromoters(promoters);
		
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, "default");
		document.getModel().getCompartment("default").setSize(1);
		document.getModel().getCompartment("default").setConstant(true);
		
		SBMLWriter writer = new SBMLWriter();
		printSpecies(document);
		printOnlyPromoters(document);
		
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			m.setName("Created from " + gcm);
			m.setId(new File(filename).getName().replace(".xml", ""));	
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");
			p.print(writer.writeSBMLToString(document));
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Loads in a properties file
	 * 
	 * @param filename
	 *            the file to load
	 */
	public void loadProperties(GCMFile gcm) {
		properties = gcm;
	}
	
	public void loadProperties(GCMFile gcm, ArrayList<String> gcmAbstractions, String[] interestingSpecies, String property) {
		properties = gcm;
		for (String abstractionOption : gcmAbstractions) {
			if (abstractionOption.equals("complex-formation-and-sequestering-abstraction"))
				complexAbstraction = true;
			else if (abstractionOption.equals("operator-site-reduction-abstraction"))
				operatorAbstraction = true;
		}
		this.interestingSpecies = interestingSpecies;
		this.property = property;
	}
	
	public void setSBMLFile(String file) {
		sbmlDocument = file;
	}
	
	public void setSBML(SBMLDocument doc) {
		document = doc;
	}
	
	public String getSBMLFile() {
		return sbmlDocument;
	}
	
	public SBMLDocument getSBML() {
		return document;
	}

	/**
	 * Outputs the network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public SBMLDocument outputSBML(String filename) {
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, "default");
		document.getModel().getCompartment("default").setSize(1);
		return outputSBML(filename, document);
	}

	public SBMLDocument outputSBML(String filename, SBMLDocument document) {
		try {
			Model m = document.getModel();
			//checkConsistancy(document);
			SBMLWriter writer = new SBMLWriter();
			//printParameters(document);
			printSpecies(document);
			if (!operatorAbstraction) {
				if (promoters.size()>0) {
					printPromoters(document);
					printRNAP(document);
				}
			}
			printDecay(document);
			printPromoterProduction(document);
			if (!operatorAbstraction)
				printPromoterBinding(document);
			printComplexBinding(document);
			
			printDiffusion(document);
			
			PrintStream p = new PrintStream(new FileOutputStream(filename),true,"UTF-8");

			m.setName("Created from " + new File(filename).getName().replace("xml", "gcm"));
			m.setId(new File(filename).getName().replace(".xml", ""));			
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");
			if (property != null) {
				ArrayList<String> species = new ArrayList<String>();
				ArrayList<Object[]> levels = new ArrayList<Object[]>();
				for (String spec : properties.getSpecies().keySet()) {
					species.add(spec);
					levels.add(new Object[0]);
				}
				MutableString prop = new MutableString(property);
				LhpnFile lpn = properties.convertToLHPN(species, levels, prop);
				property = prop.getString();
				Translator.generateSBMLConstraints(document, property, lpn);
			}
			if (document != null) {
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
				long numErrors = document.checkConsistency();
				if (numErrors > 0) {
					String message = "";
					for (long i = 0; i < numErrors; i++) {
						String error = document.getError(i).getMessage(); // .replace(". ",
						// ".\n");
						message += i + ":" + error + "\n";
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(600, 600));
					scroll.setPreferredSize(new Dimension(600, 600));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scroll, "Generated SBML Has Errors",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			p.print(writer.writeSBMLToString(document));

			p.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}

	/**
	 * Merges an SBML file network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public SBMLDocument mergeSBML(String filename) {
		try {
			if (document == null) {
				if (sbmlDocument.equals("")) {
					return outputSBML(filename);
				}

				SBMLDocument document = Gui.readSBML(currentRoot + sbmlDocument);
				// checkConsistancy(document);
				currentDocument = document;
				return outputSBML(filename, document);
			}
			else {
				currentDocument = document;
				return outputSBML(filename, document);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}
	
	/**
	 * Merges an SBML file network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public SBMLDocument mergeSBML(String filename, SBMLDocument document) {
		try {
			currentDocument = document;
			return outputSBML(filename, document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}
	
	/**
	 * Prints each promoter binding
	 * 
	 * @param document
	 *            the SBMLDocument to print to
	 */
	private void printPromoterBinding(SBMLDocument document) {
		for (Promoter p : promoters.values()) {
			// First setup RNAP binding
			if (p.getOutputs().size()==0) continue;
			String compartment = checkCompartments(p.getId());
			String rnapName = "RNAP";
			if (!compartment.equals(document.getModel().getCompartment(0).getId()))
				rnapName = compartment + "__RNAP";
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			r.setCompartment(compartment); 
			r.setId("R_" + p.getId() + "_RNAP");
			r.addReactant(Utility.SpeciesReference(rnapName, 1));
			r.addReactant(Utility.SpeciesReference(p.getId(), 1));
			r.addProduct(Utility.SpeciesReference(p.getId() + "_RNAP", 1));
			r.setReversible(true);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			double[] Krnap = p.getKrnap();
			kl.addParameter(Utility.Parameter("kf_o", Krnap[0], GeneticNetwork
					.getMoleTimeParameter(2)));
			if (Krnap.length == 2) {
				kl.addParameter(Utility.Parameter("kr_o", Krnap[1], GeneticNetwork
						.getMoleTimeParameter(1)));
//				kl.addParameter(Utility.Parameter(krnapString, Krnap[0]/Krnap[1],
//						GeneticNetwork.getMoleParameter(2)));
			} else {
				kl.addParameter(Utility.Parameter("kr_o", 1, GeneticNetwork
						.getMoleTimeParameter(1)));
//				kl.addParameter(Utility.Parameter(krnapString, Krnap[0],
//						GeneticNetwork.getMoleParameter(2)));
			}
			kl.setFormula("kf_o*" + rnapName + "*" + p.getId() + "-kr_o*"
					+ p.getId() + "_RNAP");		
			Utility.addReaction(document, r);

			// Next setup activated binding
			PrintActivatedBindingVisitor v = new PrintActivatedBindingVisitor(
					document, p, species, compartment, complexMap, partsMap);
			v.setComplexAbstraction(complexAbstraction);
			v.run();

			// Next setup repression binding
			PrintRepressionBindingVisitor v2 = new PrintRepressionBindingVisitor(
					document, p, species, compartment, complexMap, partsMap);
			v2.setComplexAbstraction(complexAbstraction);
			v2.run();
		}

	}

	/**
	 * adds all of the diffusion stuff to the sbml document
	 * it adds the diffusible species' reactions to the model
	 * 
	 * @param document the sbml document with the model that is being augmented here
	 */
	private void printDiffusion(SBMLDocument document) {
		
		int gridRows = properties.getGrid().getNumRows();
		int gridCols = properties.getGrid().getNumCols();
		
		//grid-level compartment for the model (should be called "gridLevel")
		String topLevelCompartment = document.getModel().getCompartment(0).getId();

		//stores the names of the model's second-level compartments
		ArrayList<String> allCompartments = new ArrayList<String>();
		
		Grid grid = properties.getGrid();
		
		long numCompartments = document.getModel().getNumCompartments();
		
		//loop through every compartment in the model; get their IDs
		for (long i = 1; i < numCompartments; ++i) {
			
			String comp = document.getModel().getCompartment(i).getId();	
			allCompartments.add(comp);
		}
		
		//stores pairs of species and its decay rate
		//this is later used for setting the decay rate of new, outer species
		HashMap<String, Double> speciesDecayRates = new HashMap<String, Double>();
		HashMap<String, Double> speciesECDecayRates = new HashMap<String, Double>();
		
		//stores pairs of species and its membrance diffusion rate
		//this is later used for setting the membrane diffusion rates in the reaction
		HashMap<String, Double[]> speciesDiffusionRates = new HashMap<String, Double[]>();
		HashMap<String, Double> speciesECDiffusionRates = new HashMap<String, Double>();
		
		//unique underlying species are stored in this
		//these IDs have NO component information; it's just the species ID
		ArrayList<String> underlyingSpeciesIDs = new ArrayList<String>();
		
		//DIFFUSIBLE SPECIES BUSINESS
		//iterate through all the species in the model
		//make a list of diffusible species
		//and record rates and so on
		//NOTE: there are no non-user-specified species made here
		for (SpeciesInterface spec : species.values()) {
			
			//if it's a diffusible species
			if (spec.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)) {
				
				//the ID will have the component/compartment name that the "inner" species is in
				String isID = spec.getId();
				String[] ids = isID.split("__");
				
				speciesDecayRates.put(isID, spec.getDecay());
				
				double[] diffs = spec.getKmdiff();
				Double[] diffsd = new Double[2];
				diffsd[0] = diffs[0]; diffsd[1] = diffs[1];		
				speciesDiffusionRates.put(isID, diffsd);
				
				//this is the actual species name devoid of location
				String underlyingSpeciesID = ids[ids.length - 1];
				
				//add this species id to the list if it's not there already
				//add its decay rate to a hashmap for later access
				if (!underlyingSpeciesIDs.contains(underlyingSpeciesID)) {
					
					underlyingSpeciesIDs.add(underlyingSpeciesID);
					
					//the reason for adding a decay/diffusion rate using the underlying ID
					//is so there's an easy-to-find rate to use for created species that
					//won't have user-defined rates
					
					speciesDecayRates.put(underlyingSpeciesID, spec.getDecay());
					speciesECDecayRates.put(underlyingSpeciesID, spec.getKecdecay());
					speciesDiffusionRates.put(underlyingSpeciesID, diffsd);
					speciesECDiffusionRates.put(underlyingSpeciesID, spec.getKecdiff());
				}
			}
		}
		
		//only create grid species and grid diffusion reactions if there's a grid
		//if there isn't a grid, only membrane diffusion reactions should occur
		if (grid.isEnabled()) {
		
		//GRID SPECIES AND GRID DIFFUSION BUSINESS
		//iterate through all of the species
		//create outer species at each grid location
		//create degredation reactions for these new outer species
		//create grid/outside diffusion reactions for these new outer species
		//create membrane diffusion reactions between the new outer species and second-level compartments
		for (SpeciesInterface spec : species.values()) {
				
			//all reactions will be created from inner -> outer
			//this is the ID of the inner species
			String isID = spec.getId();
			String[] ids = isID.split("__");
			
			//this is the actual species name devoid of location
			String underlyingSpeciesID = ids[ids.length - 1];
			
			if (document.getModel().getSpecies(isID) == null) continue;
			
			//get the compartment of this inner species
			String isCompartment = document.getModel().getSpecies(isID).getCompartment();
			
			String[] compartmentParts = isCompartment.split("__");
			
			//if the species is more than one level below the grid or isn't diffusible, loop on
			if (compartmentParts.length > 2 || 
					!spec.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)) 
				continue;
			
			//CREATE OUTER SPECIES
			//add "outer" species at all grid locations
			//NOTE: these are the only extra-cellular species
			for (int row = 0; row < gridRows; ++row) {
				for (int col = 0; col < gridCols; ++col) {
					
					String osID = "ROW" + row + "_COL" + col + "__" + underlyingSpeciesID;
					String osComp = topLevelCompartment;
					
					//if the species already exists, move on
					if (document.getModel().getSpecies(osID) != null) continue;

					Map.Entry<String, Properties> componentAtLoc = grid.getComponentFromLocation(new Point(row, col));
					
					//if there is a component at this location
					//(if there isn't skip this and make an outer species)
					if (componentAtLoc != null) {
						
						String compoName = componentAtLoc.getKey();

						//create the hypothetical species name that might exist
						//if this location's component had this species inside
						String potentialID = compoName + "__" + underlyingSpeciesID;
						
						//find out if this species exists and is in a top-level compartment
						//if it is, that means we don't want to create an "outer" species
						//because there's no compartment there for membrane diffusion
						if (document.getModel().getSpecies(potentialID) != null &&
								document.getModel().getSpecies(potentialID).getCompartment() != null &&
								document.getModel().getSpecies(potentialID).getCompartment().split("__").length == 1)
							continue;
					}
					
					//SPECIES CREATION
					//new outer species
					Species s = Utility.makeSpecies(osID, osComp, 0, 0);
					s.setName("");
					s.setHasOnlySubstanceUnits(true);
					
					Utility.addSpecies(document, s);
					
					
					//REACTION CREATION
					//degredation of this new outer species
					//use the extracellular decay value
					String osDecayString = GlobalConstants.KECDECAY_STRING;
					double osDecay = speciesECDecayRates.get(underlyingSpeciesID);
						
					String osDecayUnitString = getMoleTimeParameter(1);
					
					Reaction r = Utility.Reaction("Degradation_" + osID);
					r.setCompartment(osComp);
					r.setReversible(false);
					r.setFast(false);
					KineticLaw kl = r.createKineticLaw();
					
					if (osDecay > 0) {
						
						//this is the mathematical expression for the decay
						String isDecayExpression = osDecayString + "*" + osID;

						r.addReactant(Utility.SpeciesReference(osID, 1));
						
						//parameter: id="kd" value=isDecay (usually 0.0075) units="u_1_second_n1" (inverse seconds)
						kl.addParameter(Utility.Parameter(osDecayString, osDecay, osDecayUnitString));
						
						//formula: kd * inner species
						kl.setFormula(isDecayExpression);
						Utility.addReaction(document, r);
					}
				}
			}
			
			
			//CREATE GRID DIFFUSION REACTIONS
			//INCLUDING COMPONENT SPECIES DIFFUSION ON THE GRID
			//this is not membrane diffusion, just diffusion of the "outer" species
			//from one grid location to another			
			String diffusionUnitString = getMoleTimeParameter(1);
			String diffusionString = GlobalConstants.KECDIFF_STRING;
			String diffComp = topLevelCompartment;
			double kecdiff = speciesECDiffusionRates.get(underlyingSpeciesID);
			
			//loop through all of the grid locations
			//create diffusion reactions between all eight neighbors if they exist
			for (int row = 0; row < gridRows; ++row) {
			for (int col = 0; col < gridCols; ++col) {
								
				//ID of the outer species
				//this will be modified if the outer species was originally in a component
				//which means a ROW/COL outer species was never created
				String osID = "ROW" + row + "_COL" + col + "__" + underlyingSpeciesID;
				
				//if the species doesn't exist, then change the ID of the outer species to the
				//ID of the component species
				//this means it wasn't created because there was already a top-level species
				//with this underlying species ID, probably from a top-level component
				if (document.getModel().getSpecies(osID) == null) {
					
					Map.Entry<String, Properties> componentAtLoc = grid.getComponentFromLocation(new Point(row, col));
					
					if (componentAtLoc != null) {
						
						String compoName = componentAtLoc.getKey();

						//create the hypothetical species name that might exist
						//if this location's component had this species inside
						String potentialID = compoName + "__" + underlyingSpeciesID;
						
						//find out if this species exists and is in the top-level compartment
						//if it is and is diffusible, that means we use this for diffusion and don't do membrane diffusion
						if (document.getModel().getSpecies(potentialID) != null && 
								species.get(potentialID) != null && 
								species.get(potentialID).getProperty(GlobalConstants.TYPE)
								.contains(GlobalConstants.DIFFUSIBLE))
							osID = potentialID;
					}
					else continue;
				}
				
				//loop through all neighboring locations
				for (int rowMod = -1; rowMod <= 1; ++rowMod) {
					for (int colMod = -1; colMod <= 1; ++colMod) {
						
						//don't diffuse with self
						if (colMod == 0 && rowMod == 0) continue;
						
						int neighborRow = row + rowMod;
						int neighborCol = col + colMod;
						
						String neighborID = "ROW" + neighborRow + "_COL" + 
							neighborCol + "__" + underlyingSpeciesID;
						
						//if this ROW/COL neighbor doesn't exist, try a component neighbor ID
						//at this location
						if (document.getModel().getSpecies(neighborID) == null) {
							
							//find a potential component at this neighboring location
							Map.Entry<String, Properties> componentAtLoc = 
								grid.getComponentFromLocation(new Point(neighborRow, neighborCol));
							
							if (componentAtLoc != null) {
								
								String compoName = componentAtLoc.getKey();

								//create the hypothetical species name that might exist
								//if this location's component had this species inside
								String potentialID = compoName + "__" + underlyingSpeciesID;
								
								//if this neighbor exists and is diffusible, use this one
								//if it's still not there, move on to another grid location
								if (document.getModel().getSpecies(potentialID) != null && 
										species.get(potentialID) != null && 
										species.get(potentialID).getProperty(GlobalConstants.TYPE)
										.contains(GlobalConstants.DIFFUSIBLE))
									neighborID = potentialID;
								else continue;
							}
							else continue;
						}
						
						//if the forward or reverse reaction already exists then skip this neighbor
						if (document.getModel().getReaction("Diffusion_" + osID + "_" + neighborID) != null ||
								document.getModel().getReaction("Diffusion_" + neighborID + "_" + osID) != null)
							continue;
						
						//REACTION
						//reversible between neighboring "outer" species
						//this is the diffusion across the "medium" if you will					
						Reaction r = Utility.Reaction("Diffusion_" + osID + "_" + neighborID);
						r.setCompartment(diffComp);
						r.setReversible(true);
						r.setFast(false);
						KineticLaw kl = r.createKineticLaw();
						
						String diffusionExpression = diffusionString + " * " + osID + " - " +
						diffusionString + " * " + neighborID;
	
						//reactant is current outer species; product is neighboring species
						r.addReactant(Utility.SpeciesReference(osID, 1));
						r.addProduct(Utility.SpeciesReference(neighborID, 1));
						
						//parameters: id="kecdiff"" value=kecdiff units="u_1_second_n1" (inverse seconds)
						kl.addParameter(Utility.Parameter(diffusionString, kecdiff, diffusionUnitString));
						kl.addParameter(Utility.Parameter(diffusionString, kecdiff, diffusionUnitString));
						
						kl.setFormula(diffusionExpression);
						Utility.addReaction(document, r);
					}
				}
			}
			} //end of the grid diffusion reaction creation
		}
		} //end isGridEnabled block
			
		
		//MEMBRANE DIFFUSION BUSINESS
		for (SpeciesInterface spec : species.values()) {
			
			//all reactions will be created from inner -> outer
			//this is the ID of the inner species
			String isID = spec.getId();
			String[] ids = isID.split("__");
			
			//this is the actual species name devoid of location
			String underlyingSpeciesID = ids[ids.length - 1];
			
			//if this species isn't diffusible, then keep looping
			if (!spec.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE))
				continue;
			
			//get the compartment of this inner species
			String isCompartment = document.getModel().getSpecies(isID).getCompartment();
			
			String[] compartmentParts = isCompartment.split("__");
			
			//this implies a species one level below the grid (or top level)
			if (compartmentParts.length == 2) {
				
				//create a reaction between the species and the outer grid species at this location
				//first, get the row and column of this compartment, so we know which grid-level
				//species to diffuse to
				Point rowCol = null;
				
				//if there's a grid, get the row and column of the component
				if (grid.isEnabled())
					rowCol = properties.getGrid().getLocationFromComponentID(compartmentParts[0]);
				
				
				//MEMBRANE DIFFUSION REACTION CREATION
				//between inner species and outer grid species
				
				//take the membrane diffusion rates from the inner species
				Double[] kmdiff = speciesDiffusionRates.get(isID);
				
				double kfmdiff = kmdiff[0];
				double krmdiff = kmdiff[1];
				
				String fDiffusionString = GlobalConstants.FORWARD_MEMDIFF_STRING;
				String rDiffusionString = GlobalConstants.REVERSE_MEMDIFF_STRING;
				String diffusionUnitString = getMoleTimeParameter(1);
				
				//species within the top-level compartment ("outer" species)
				String osID = "";
				
				if (grid.isEnabled()) {
					
					osID = "ROW" + rowCol.x + "_COL" + rowCol.y + "__" + underlyingSpeciesID;
					
					//if the species doesn't exist for some reason, continue
					if (document.getModel().getSpecies(osID) == null)
						continue;
				}
				else {
					
					//if there isn't a grid, the outer level is the top level, so
					//the potential species ID is the underlying ID
					osID = underlyingSpeciesID;
					
					//see if this species exists and is diffusible
					//if it doesn't exist or isn't diffusible, don't create the reaction and move on
					if (document.getModel().getSpecies(osID) == null || species.get(osID) == null ||
							(species.get(osID) != null &&
							!species.get(osID).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)))
						continue;
				}
				
				Reaction r = Utility.Reaction("Membrane_diffusion_" + isID + "_" + osID);
				
				//if the forward or reverse reactions already exist, then skip this compartment
				if (document.getModel().getReaction("Membrane_diffusion_" + isID + "_" + osID) != null ||
						document.getModel().getReaction("Membrane_diffusion_" + osID + "_" + isID) != null)
					continue;
				
				r.setCompartment(topLevelCompartment);
				r.setReversible(true);
				r.setFast(false);
				KineticLaw kl = r.createKineticLaw();
				
				if (kfmdiff > 0 || krmdiff > 0) {
					
					String diffusionExpression = fDiffusionString + " * " + isID + " - " +
						rDiffusionString + " * " + osID;

					//reactant is inner species; product is outer species
					r.addReactant(Utility.SpeciesReference(isID, 1));
					r.addProduct(Utility.SpeciesReference(osID, 1));
					
					//parameters: id="kfmdiff" or "krmdiff" value=kfmdiff or krmdiff
					//units="u_1_second_n1" (inverse seconds)
					kl.addParameter(Utility.Parameter(fDiffusionString, kfmdiff, diffusionUnitString));
					kl.addParameter(Utility.Parameter(rDiffusionString, krmdiff, diffusionUnitString));
					
					//formula: kfmdiff * inner species - krmdiff * outer species
					kl.setFormula(diffusionExpression);
					Utility.addReaction(document, r);
				}
			}
			//this means the species is more than one level below the grid
			else if (compartmentParts.length > 2) {
				
				//create a hypothetical species ID and see if it exists
				//this species ID is one level above the current compartment
				String outerCompartmentID = "";
				
				//trim off the last two compartment parts
				//to get the ID of the compartment one level above this one
				for (int i = 0; i < compartmentParts.length - 2; ++i)
					outerCompartmentID += compartmentParts[i];
				
				//this is the species that may exist one level above
				String osID = outerCompartmentID + "__" + underlyingSpeciesID;
				
				//if the species exists and is diffusible
				//then create a membrane diffusion reaction
				if (document.getModel().getSpecies(osID) != null && 
						species.get(osID).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)) {
					
					//MEMBRANE DIFFUSION REACTION CREATION
					//between inner species and outer species
					
					//take the membrane diffusion rates from the inner species
					Double[] kmdiff = speciesDiffusionRates.get(isID);
					
					double kfmdiff = kmdiff[0];
					double krmdiff = kmdiff[1];
					
					String fDiffusionString = GlobalConstants.FORWARD_MEMDIFF_STRING;
					String rDiffusionString = GlobalConstants.REVERSE_MEMDIFF_STRING;
					String diffusionUnitString = getMoleTimeParameter(1);
					
					Reaction r = Utility.Reaction("Membrane_diffusion_" + isID + "_" + osID);
					
					//if the forward or reverse reaction already exists, then skip
					if (document.getModel().getReaction("Membrane_diffusion_" + isID + "_" + osID) != null ||
							document.getModel().getReaction("Membrane_diffusion_" + osID + "_" + isID) != null)
						continue;
					
					r.setCompartment(isCompartment);
					r.setReversible(true);
					r.setFast(false);
					KineticLaw kl = r.createKineticLaw();
					
					if (kfmdiff > 0 || krmdiff > 0) {
						
						String diffusionExpression = fDiffusionString + " * " + isID + " - " +
							rDiffusionString + " * " + osID;

						//reactant is inner species; product is outer species
						r.addReactant(Utility.SpeciesReference(isID, 1));
						r.addProduct(Utility.SpeciesReference(osID, 1));
						
						//parameters: id="kfmdiff" or "krmdiff" value=kfmdiff or krmdiff
						//units="u_1_second_n1" (inverse seconds)
						kl.addParameter(Utility.Parameter(fDiffusionString, kfmdiff, diffusionUnitString));
						kl.addParameter(Utility.Parameter(rDiffusionString, krmdiff, diffusionUnitString));
						
						//formula: kfmdiff * inner species - krmdiff * outer species
						kl.setFormula(diffusionExpression);
						Utility.addReaction(document, r);
					}					
				}				
			}
			//if the length is 1, then it's at the grid level and has already been taken care of
			else continue;
		}//end looping through unique, underlying diffusible species
	}
	
	/**
	 * Prints each promoter production values
	 * 
	 * @param document
	 *            the SBMLDocument to print to
	 */
	private void printPromoterProduction(SBMLDocument document) {
		for (Promoter p : promoters.values()) {
			if (p.getOutputs().size()==0) continue;
			String compartment = checkCompartments(p.getId());
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			r.setCompartment(compartment);
			for (SpeciesInterface species : p.getOutputs()) {
				r.addProduct(Utility.SpeciesReference(species.getId(), p.getStoich()));
			}
			r.setReversible(false);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			if (operatorAbstraction) {
				r.setId("R_abstracted_production_" + p.getId());
				double rnap = 30;
				if (properties != null)
					rnap = Double.parseDouble(properties.getParameter(GlobalConstants.RNAP_STRING));
				AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, rnap, r, kl);
				kl.setFormula(e.abstractOperatorSite(p));
				Utility.addReaction(document, r);
			} else {
				r.addModifier(Utility.ModifierSpeciesReference(p.getId() + "_RNAP"));
				if (p.getActivators().size() > 0) {
					r.setId("R_basal_production_" + p.getId());
					kl.addParameter(Utility.Parameter(kBasalString, p.getKbasal(),
							getMoleTimeParameter(1)));
					kl.setFormula(kBasalString + "*" + p.getId() + "_RNAP");

				} else {
					r.setId("R_constitutive_production_" + p.getId());
					kl.addParameter(Utility.Parameter(kOcString, p.getKoc(),
							getMoleTimeParameter(1)));
					kl.setFormula(kOcString + "*" + p.getId() + "_RNAP");
				}
				Utility.addReaction(document, r);
				if (p.getActivators().size() > 0) {
					PrintActivatedProductionVisitor v = new PrintActivatedProductionVisitor(
							document, p, compartment);
					v.run();
				}
			}
		}
	}

	/**
	 * Prints the decay reactions
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printDecay(SBMLDocument document) {
		
		PrintDecaySpeciesVisitor visitor = new PrintDecaySpeciesVisitor(document, species, compartments, complexMap, partsMap);
		visitor.setComplexAbstraction(complexAbstraction);
		visitor.run();
	}

	/**
	 * Prints the species in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printSpecies(SBMLDocument document) {
		
		PrintSpeciesVisitor visitor = new PrintSpeciesVisitor(document, species, compartments);
		visitor.setComplexAbstraction(complexAbstraction);
		visitor.run();
	}
	
	private void printComplexBinding(SBMLDocument document) {
		PrintComplexVisitor v = new PrintComplexVisitor(document, species, compartments, complexMap, partsMap);
		v.setComplexAbstraction(complexAbstraction);
		v.run();
	}

	/**
	 * Prints the promoters in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printPromoters(SBMLDocument document) {
		
		for (Promoter p : promoters.values()) {
			if (p.getOutputs().size()==0) continue;
			// First print out the promoter, and promoter bound to RNAP
			// But first check if promoter belongs to a compartment other than default
			String compartment = checkCompartments(p.getId());
			Species s = Utility.makeSpecies(p.getId(), compartment,	p.getPcount(), -1);
		    if ((p.getProperties() != null) &&
		    	(p.getProperties().containsKey(GlobalConstants.NAME))) {
		    	s.setName(p.getProperty(GlobalConstants.NAME));
		    }
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
			s = Utility.makeSpecies(p.getId() + "_RNAP", compartment, 0, -1);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
			// Now cycle through all activators and repressors and add 
			// those species bound to promoter
			for (SpeciesInterface specie : p.getActivators()) {
				String activator = specie.getId();
				String[] splitted = activator.split("__");
				if (splitted.length > 1)
					activator = splitted[1];
				s = Utility.makeSpecies(p.getId() + "_" + activator + "_RNAP", compartment, 0, -1);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
			for (SpeciesInterface specie : p.getRepressors()) {
				String repressor = specie.getId();
				String[] splitted = repressor.split("__");
				if (splitted.length > 1)
					repressor = splitted[1];
				s = Utility.makeSpecies(p.getId() + "_"	+ repressor + "_bound", compartment, 0, -1);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
		}

	}
	
	/**
	 * Prints the promoters in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printOnlyPromoters(SBMLDocument document) {

		for (Promoter p : promoters.values()) {
			Species s = Utility.makeSpecies(p.getId(), document.getModel().getCompartment(0).getId(), p.getPcount(), -1);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
		}
	}
	
	/**
	 * Prints the RNAP molecule to the document
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printRNAP(SBMLDocument document) {
		double rnap = 30;
		if (properties != null) {
			rnap = Double.parseDouble(properties.getParameter(GlobalConstants.RNAP_STRING));
		}
		Species s = Utility.makeSpecies("RNAP", document.getModel().getCompartment(0).getId(), rnap, -1);		
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		//Adds RNA polymerase for compartments other than default
		for (String compartment : compartments.keySet()) {
			Properties prop = compartments.get(compartment);
			if (prop.containsKey(GlobalConstants.RNAP_STRING)) {
				rnap = Double.parseDouble((String)prop.get(GlobalConstants.RNAP_STRING));
			}
			Species sc = Utility.makeSpecies(compartment + "__RNAP", compartment, rnap, -1);
			sc.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, sc);
		}
	}
	
	/**
	 * Checks if species belongs in a compartment other than default
	 */
	private String checkCompartments(String species) {
		String compartment = document.getModel().getCompartment(0).getId();
		//String[] splitted = species.split("__");
		String component = species;
		while (component.contains("__")) {
			component = component.substring(0,component.lastIndexOf("__"));
			for (String compartmentName : compartments.keySet()) {
				if (compartmentName.equals(component))
					return compartmentName;					
				else if (compartmentName.contains("__") && compartmentName.substring(0, compartmentName.lastIndexOf("__"))
						.equals(component)) {
					return compartmentName;
				}
			}
		}
		/*
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
			*/
		return compartment;
	}

	public AbstractionEngine createAbstractionEngine() {
		return new AbstractionEngine(species, complexMap, partsMap, Double.parseDouble(properties
				.getParameters().get(GlobalConstants.RNAP_STRING)));
	}

	/**
	 * Builds the complex species
	 * 
	 */
	private void buildComplexes() {
		for (String complexId : complexMap.keySet()) {
			ComplexSpecies c = new ComplexSpecies(species.get(complexId));
			species.put(complexId, c);
		}
	}
	
	private void buildComplexInfluences() {
		for (Promoter p : promoters.values()) {
			for (Influence r : p.getActivatingInfluences()) {
				String inputId = r.getInput();
				if (complexMap.containsKey(inputId)) {
					p.addActivator(inputId, species.get(inputId));
					species.get(inputId).setActivator(true);
				}
			}
			for (Influence r : p.getRepressingInfluences()) {
				String inputId = r.getInput();
				if (complexMap.containsKey(inputId)) {
					p.addRepressor(inputId, species.get(inputId));
					species.get(inputId).setRepressor(true);
				}
			}
		}
	}
	
	public void markAbstractable() {
		for (Promoter p : promoters.values()) {
			//Checks if activators are sequesterable
			//Marks activators that are complexes as abstractable provided they're not parts/outputs 
			//Checks parts of complex activators provided the activators aren't outputs
			for (SpeciesInterface s : p.getActivators()) {
				if (partsMap.containsKey(s.getId())) {
					checkSequester(s.getId(), s.getId());
				}
				if (complexMap.containsKey(s.getId()) && !s.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT)) {
					checkComplex(s.getId(), "");
					if (!partsMap.containsKey(s.getId()) && !isGenetic(s.getId()) 
							&& !s.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)
							&& !SBMLutilities.variableInUse(document, s.getId(), false, false))
						s.setAbstractable(true);
				}
			}
			//Checks if repressors are sequesterable
			//Marks repressors that are complexes as abstractable provided they're not parts/outputs 
			//Checks parts of complex repressors provided the repressors aren't outputs
			for (SpeciesInterface s : p.getRepressors()) {
				if (partsMap.containsKey(s.getId())) {
					checkSequester(s.getId(), s.getId());
				}
				if (complexMap.containsKey(s.getId()) && !s.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT)) {
					checkComplex(s.getId(), "");
					if (!partsMap.containsKey(s.getId()) && !isGenetic(s.getId()) 
							&& !s.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)
							&& !SBMLutilities.variableInUse(document, s.getId(), false, false))
						s.setAbstractable(true);
				}
			}
		}	
		//Checks if parts of output complex species are abstractable or sequesterable
		for (String complexId : complexMap.keySet()) {
			if (species.get(complexId).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT))
				checkComplex(complexId, "");
		}
		//Removes abstractable marking from interesting species 
		unMarkInterestingSpecies(interestingSpecies);
	}
	
	//Checks if parts of given complex are sequesterable so long as the parts aren't activators or repressors
	//Marks parts that are complexes as abstractable so long as the parts aren't used elsewhere
	//Recursively checks parts of parts that are complexes provided the latter aren't activators, repressors, or outputs
	private boolean checkComplex(String complexId, String sequesterRoot) {
		for (Influence infl : complexMap.get(complexId)) {
			String partId = infl.getInput();
			if (!partId.equals(sequesterRoot)) {
				boolean sequesterable = false;
				if (partsMap.get(partId).size() > 1) {
					if (!sequesterRoot.equals(""))  //i.e. checkComplex called by checkSequester to determine if sequestering species are used elsewhere
						return false;
					if (!species.get(partId).isActivator() && !species.get(partId).isRepressor())
						sequesterable = checkSequester(partId, partId);
				}
				if (!sequesterRoot.equals("") && (species.get(partId).isActivator() || species.get(partId).isRepressor()))
					return false;
				if (complexMap.containsKey(partId) && !species.get(partId).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT)
						&& !species.get(partId).isActivator() && !species.get(partId).isRepressor()) {
					if (!sequesterRoot.equals("") && !checkComplex(partId, sequesterRoot))
						return false;
					if (!sequesterable && partsMap.get(partId).size() == 1 && !isGenetic(partId)
							&& !species.get(partId).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)
							&& !SBMLutilities.variableInUse(document, partId, false, false))
						species.get(partId).setAbstractable(true);
					if (sequesterRoot.equals(""))
						checkComplex(partId, "");
				} else if (!sequesterable && (partsMap.get(partId).size() > 1 || species.get(partId).isActivator() || species.get(partId).isRepressor()))
					species.get(complexId).setAbstractable(false);
			}
		}
		return true;
	}
	
	//Marks given species as sequesterable and its sequestering complexes as sequester-abstractable if (1) those complexes
	//and the sequestering species which make them up are not used elsewhere and (2) the sequestered species's stoichiometry of binding is one
	private boolean checkSequester(String partId, String sequesterRoot) {
		boolean sequesterable = false;
		ArrayList<String> abstractableComplexes = new ArrayList<String>();
		for (Influence infl : partsMap.get(partId)) {
			String complexId = infl.getOutput();
			if (infl.getCoop() == 1 
					&& ((!species.get(complexId).isActivator() && !species.get(complexId).isRepressor()) || (operatorAbstraction && !partsMap.containsKey(complexId))) 
					&& !species.get(complexId).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT) 
					&& (!partsMap.containsKey(complexId) || partsMap.get(complexId).size() == 1) && !isGenetic(complexId)
					&& !species.get(complexId).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)
					&& !SBMLutilities.variableInUse(document, complexId, false, false) 
					&& complexMap.get(complexId).size() > 1 && checkComplex(complexId, partId)) {
				if (partsMap.containsKey(complexId))
					checkSequester(complexId, sequesterRoot);
				abstractableComplexes.add(complexId);
				sequesterable = true;
			}
		}
		if (sequesterable) {
			if (partId.equals(sequesterRoot))
				species.get(partId).setSequesterable(true);
			for (String complexId : abstractableComplexes)
				species.get(complexId).setSequesterAbstractable(true);
		}
		return sequesterable;
	}
	
	//Unmarks complex species as abstractable (and their parts as sequesterable) if they are interesting
	public void unMarkInterestingSpecies(String[] interestingSpecies) {
		for (String interestingId : interestingSpecies) {
			if (species.containsKey(interestingId) && complexMap.containsKey(interestingId)) {
				species.get(interestingId).setAbstractable(false);
				species.get(interestingId).setSequesterAbstractable(false);
				for (Influence infl : complexMap.get(interestingId))
					species.get(infl.getInput()).setSequesterable(false);
			}
		}
	}
	
	//Returns default interesting species, i.e. those that are outputs or genetically produced
	public ArrayList<String> getInterestingSpecies() {
		ArrayList<String> interestingSpecies = new ArrayList<String>();
		for (String id : species.keySet()) {
			if (!complexMap.keySet().contains(id) || 
					species.get(id).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT))
				interestingSpecies.add(id);
		}
		return interestingSpecies;
	}
	
	public HashMap<String, SpeciesInterface> getSpecies() {
		return species;
	}

	public void setSpecies(HashMap<String, SpeciesInterface> species) {
		this.species = species;
	}

	public HashMap<String, Promoter> getPromoters() {
		return promoters;
	}
	
	public void setPromoters(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}

	public GCMFile getProperties() {
		return properties;
	}

	public void setProperties(GCMFile properties) {
		this.properties = properties;
	}
	
	public HashMap<String, ArrayList<Influence>> getComplexMap() {
		return complexMap;
	}
	
	private boolean isGenetic(String speciesId) {
		for (Promoter p : getPromoters().values())
			if (p.getOutputs().toString().contains(speciesId))
				return true;
		return false;
	}

	/**
	 * Checks the consistancy of the document
	 * 
	 * @param doc
	 *            the SBML document to check
	 */
	/*
	private void checkConsistancy(SBMLDocument doc) {
		if (doc.checkConsistency() > 0) {
			for (int i = 0; i < doc.getNumErrors(); i++) {
				System.out.println(doc.getError(i).getMessage());
			}
		}
	}
	*/
	
	private String sbmlDocument = "";
	
	private SBMLDocument document = null;

	private static SBMLDocument currentDocument = null;
	
	private static String currentRoot = "";

	private boolean operatorAbstraction = false;
	
	private boolean complexAbstraction = false;
	
	private String[] interestingSpecies;

	private HashMap<String, SpeciesInterface> species = null;

	private HashMap<String, Promoter> promoters = null;
	
	private HashMap<String, ArrayList<Influence>> complexMap;
	
	private HashMap<String, ArrayList<Influence>> partsMap;
	
	private HashMap<String, Properties> compartments;

	private GCMFile properties = null;
	
//	private String krnapString = GlobalConstants.RNAP_BINDING_STRING;
	
	private String kBasalString = GlobalConstants.KBASAL_STRING;
	
	private String kOcString = GlobalConstants.OCR_STRING;
	
	private String property;

	/**
	 * Returns the curent SBML document being built
	 * 
	 * @return the curent SBML document being built
	 */
	public static SBMLDocument getCurrentDocument() {
		return currentDocument;
	}
	
	/**
	 * Sets the current root
	 * @param root the root directory
	 */
	public static void setRoot(String root) {
		currentRoot = root;
	}	

	public static String getUnitString(ArrayList<String> unitNames,
			ArrayList<Integer> exponents, ArrayList<Integer> multiplier,
			Model model) {

		// First build the name of the unit and see if it exists, start by
		// sorting the units to build a unique string
		for (int i = 0; i < unitNames.size(); i++) {
			for (int j = i; j > 0; j--) {
				if (unitNames.get(j - 1).compareTo(unitNames.get(i)) > 0) {
					Integer tempD = multiplier.get(j);
					Integer tempI = exponents.get(j);
					String tempS = unitNames.get(j);

					multiplier.set(j, multiplier.get(j - 1));
					unitNames.set(j, unitNames.get(j - 1));
					exponents.set(j, exponents.get(j - 1));

					multiplier.set(j - 1, tempD);
					unitNames.set(j - 1, tempS);
					exponents.set(j - 1, tempI);
				}
			}
		}
		UnitDefinition t = new UnitDefinition(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		String name = "u_";
		for (int i = 0; i < unitNames.size(); i++) {
			String sign = "";
			if (exponents.get(i).intValue() < 0) {
				sign = "n";
			}
			name = name + multiplier.get(i) + "_" + unitNames.get(i) + "_"
					+ sign + Math.abs(exponents.get(i)) + "_";
			Unit u = t.createUnit();
			u.setKind(libsbml.UnitKind_forName(unitNames.get(i)));
			u.setExponent(exponents.get(i).intValue());
			u.setMultiplier(multiplier.get(i).intValue());
			u.setScale(0);
		}
		name = name.substring(0, name.length() - 1);
		t.setId(name);
		if (model.getUnitDefinition(name) == null) {
			model.addUnitDefinition(t);
		}
		return name;
	}

	/**
	 * Returns a unit name for a parameter based on the number of molecules
	 * involved
	 * 
	 * @param numMolecules
	 *            the number of molecules involved
	 * @return a unit name
	 */
	public static String getMoleTimeParameter(int numMolecules) {
		ArrayList<String> unitS = new ArrayList<String>();
		ArrayList<Integer> unitE = new ArrayList<Integer>();
		ArrayList<Integer> unitM = new ArrayList<Integer>();

		if (numMolecules > 1) {
			unitS.add("mole");
			unitE.add(new Integer(-(numMolecules - 1)));
			unitM.add(new Integer(1));
		}

		unitS.add("second");
		unitE.add(new Integer(-1));
		unitM.add(new Integer(1));

		return GeneticNetwork.getUnitString(unitS, unitE, unitM,
				currentDocument.getModel());
	}

	/**
	 * Returns a unit name for a parameter based on the number of molecules
	 * involved
	 * 
	 * @param numMolecules
	 *            the number of molecules involved
	 * @return a unit name
	 */
	public static String getMoleParameter(int numMolecules) {
		ArrayList<String> unitS = new ArrayList<String>();
		ArrayList<Integer> unitE = new ArrayList<Integer>();
		ArrayList<Integer> unitM = new ArrayList<Integer>();

		unitS.add("mole");
		unitE.add(new Integer(-(numMolecules - 1)));
		unitM.add(new Integer(1));

		return GeneticNetwork.getUnitString(unitS, unitE, unitM,
				currentDocument.getModel());
	}
	
	public static String getMoleParameter(String numMolecules) {
		return getMoleParameter(Integer.parseInt(numMolecules));
	}
}
