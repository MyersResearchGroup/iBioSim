 package biomodel.network;


import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lpn.parser.LhpnFile;
import lpn.parser.Translator;
import main.Gui;
import main.util.MutableString;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLTriple;
import org.sbml.libsbml.libsbml;

import com.lowagie.text.Document;

import biomodel.gui.Grid;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;
import biomodel.visitor.AbstractPrintVisitor;
import biomodel.visitor.PrintActivatedBindingVisitor;
import biomodel.visitor.PrintActivatedProductionVisitor;
import biomodel.visitor.PrintComplexVisitor;
import biomodel.visitor.PrintDecaySpeciesVisitor;
import biomodel.visitor.PrintRepressionBindingVisitor;
import biomodel.visitor.PrintSpeciesVisitor;




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
			HashMap<String, Promoter> promoters, BioModel gcm) {
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
		this.compartments = new HashMap<String,Properties>(); 
		for (long i=0; i < gcm.getSBMLDocument().getModel().getNumCompartments(); i++) {
			compartments.put(gcm.getSBMLDocument().getModel().getCompartment(i).getId(), null);
		}
		
		AbstractPrintVisitor.setGCMFile(gcm);
		
		buildComplexes();
		buildComplexInfluences();
	}
	
	public void buildTemplate(HashMap<String, SpeciesInterface> species,			
			HashMap<String, Promoter> promoters, String gcm, String filename) {
		
		BioModel file = new BioModel(currentRoot);
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
	public void loadProperties(BioModel gcm) {
		properties = gcm;
	}
	
	public void loadProperties(BioModel gcm, ArrayList<String> gcmAbstractions, String[] interestingSpecies, String property) {
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
			SBMLWriter writer = new SBMLWriter();
			
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
			
			PrintStream p = new PrintStream(new FileOutputStream(filename),true,"UTF-8");

			m.setName("Created from " + new File(filename).getName().replace("xml", "gcm"));
			m.setId(new File(filename).getName().replace(".xml", ""));			
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");
			if (property != null && !property.equals("")) {
				ArrayList<String> species = new ArrayList<String>();
				ArrayList<Object[]> levels = new ArrayList<Object[]>();
				for (String spec : properties.getSpecies()) {
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
			
			reformatArrayContent(document, filename);
			
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
			String compartment = p.getCompartment();
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
			if (Krnap[0] >= 0) {
				kl.addParameter(Utility.Parameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING, Krnap[0], 
						GeneticNetwork.getMoleTimeParameter(2)));
				if (Krnap.length == 2) {
					kl.addParameter(Utility.Parameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING, Krnap[1], 
							GeneticNetwork.getMoleTimeParameter(1)));
				} else {
					kl.addParameter(Utility.Parameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING, 1, GeneticNetwork.getMoleTimeParameter(1)));
				}
			}
			kl.setFormula(GlobalConstants.FORWARD_RNAP_BINDING_STRING + "*" + rnapName + "*" + p.getId() + "-"+ 
					GlobalConstants.REVERSE_RNAP_BINDING_STRING + "*" + p.getId() + "_RNAP");		
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
	 * does lots of formatting -- mostly to bring the model into compliance with JSBML and to replace
	 * the Type=Grid annotation with better information
	 * 
	 * @param document
	 */
	private void reformatArrayContent(SBMLDocument document, String filename) {
		
		ArrayList<Reaction> membraneDiffusionReactions = new ArrayList<Reaction>();
		
		//find all individual membrane diffusion reactions
		for (int i = 0; i < document.getModel().getNumReactions(); ++i) {
			
			if (document.getModel().getReaction(i).getId().contains("MembraneDiffusion"))
				membraneDiffusionReactions.add(document.getModel().getReaction(i));
		}
		
		HashSet<String> reactionsToRemove = new HashSet<String>();
		
		//turn the individual membrane diffusion reactions into arrays for each species
		for (Reaction membraneDiffusionReaction : membraneDiffusionReactions) {
			
			String reactionID = membraneDiffusionReaction.getId();
			
			ArrayList<String> validSubmodels = new ArrayList<String>();
			
			//loop through submodels to see if they have this same membrane diffusion reaction ID
			for (int submodelIndex = 0; submodelIndex < properties.getSBMLCompModel().getNumSubmodels(); ++submodelIndex) {
				
				SBMLReader sbmlReader = new SBMLReader();
				Model submodel = sbmlReader.readSBMLFromFile(properties.getPath() + 
						properties.getSBMLCompModel().getSubmodel(submodelIndex).getModelRef() + ".xml").getModel();
				
				if (submodel.getReaction(reactionID) != null)
					validSubmodels.add(submodel.getId());				
			}
			
			membraneDiffusionReaction.setAnnotation("");
			
			//now go through this list of valid submodels, find their locations, and add those to the reaction's annotation
			for (String validSubmodelID : validSubmodels) {
				
				if (properties.getSBMLDocument().getModel().getParameter(validSubmodelID + "__locations") != null) {
				
					validSubmodelID = validSubmodelID.replace("GRID__","");
					
					if (membraneDiffusionReaction.getAnnotationString().length() > 0)
						membraneDiffusionReaction.appendAnnotation(", " + 
							properties.getSBMLDocument().getModel().getParameter(validSubmodelID + "__locations").getAnnotationString()
							.replace("<annotation>","").replace("</annotation>",""));
					else 
						membraneDiffusionReaction.setAnnotation( 
								properties.getSBMLDocument().getModel().getParameter(validSubmodelID + "__locations").getAnnotationString()
								.replace("<annotation>","").replace("</annotation>",""));
				}
			}
			
			//fix the array annotation that was just created
					
			String reactionAnnotation = membraneDiffusionReaction.getAnnotationString();
			ArrayList<String> components = new ArrayList<String>();
			
			String[] splitAnnotation = reactionAnnotation.replace("\"","").split("array:");
			
			//find all components in the annotation
			for (int j = 2; j < splitAnnotation.length; ++j) {
					
				components.add(splitAnnotation[j].split("=")[0].trim());
			}
			
			
			//handle/fix non-grid membrane diffusion reactions
			
			//if it doesn't have a product then it's a non-grid reaction
			//so we need to see if the appropriate product is available
			//if it isn't, the reaction needs to be deleted as it shouldn't exist
			//unless the species exists on both sides of the membrane
			if (membraneDiffusionReaction.getNumProducts() == 0) {
				
				//take off the immediate compartment ID
				String[] splitReactantID = membraneDiffusionReaction.getReactant(0).getSpecies().split("__");
				String potentialProductID = "";
				
				for (int i = 1; i < splitReactantID.length; ++i)
					potentialProductID += splitReactantID[i];
				
				//if the potential product is there and is diffusible
				if (document.getModel().getSpecies(potentialProductID) != null 
						&& document.getModel().getReaction("MembraneDiffusion_" + potentialProductID) != null) {
					
					SpeciesReference newProduct = membraneDiffusionReaction.createProduct();
					newProduct.setSpecies(potentialProductID);
					newProduct.setStoichiometry(1.0);
					newProduct.setConstant(true);
					
					//add the product into the kinetic law
					membraneDiffusionReaction.getKineticLaw().setFormula(
							membraneDiffusionReaction.getKineticLaw().getFormula() + " * " + potentialProductID);
					
					//take off the annotation so it's not mistaken as a grid-based memdiff reaction
					membraneDiffusionReaction.setAnnotation("");
				}
				else 
					reactionsToRemove.add(membraneDiffusionReaction.getId());
			}
		}
		
		ArrayList<Event> dynamicEvents = new ArrayList<Event>();
		
		//look through all submodels for dynamic events
		for (int submodelIndex = 0; submodelIndex < properties.getSBMLCompModel().getNumSubmodels(); 
		++submodelIndex) {
			
			SBMLReader sbmlReader = new SBMLReader();
			Model submodel = sbmlReader.readSBMLFromFile(properties.getPath() + 
					properties.getSBMLCompModel().getSubmodel(submodelIndex).getModelRef() + ".xml").getModel();
			if (properties.getSBMLCompModel().getSubmodel(submodelIndex).getId().contains("GRID__") == false)
				submodel.setId(properties.getSBMLCompModel().getSubmodel(submodelIndex).getId());
			
			//find all individual dynamic events (within submodels)
			for (int i = 0; i < submodel.getNumEvents(); ++i) {
				
				Event event = submodel.getEvent(i);
				
				if (event.getAnnotationString().length() > 0 && (
						event.getAnnotationString().contains("Division") ||
						event.getAnnotationString().contains("Death"))) {
					
					if (event.getAnnotationString().contains("Symmetric Division"))
						event.setId(submodel.getId() + "__SymmetricDivision__" + event.getId());
					if (event.getAnnotationString().contains("Asymmetric Division"))
						event.setId(submodel.getId() + "__AsymmetricDivision__" + event.getId());
					else if (event.getAnnotationString().contains("Death"))
						event.setId(submodel.getId() + "__Death__" + event.getId());
					
					dynamicEvents.add(event);
				}
			}
		}
		
		//make arrays out of the dynamic events (for the top-level model)
		for (Event dynEvent : dynamicEvents) {
			
			Event e = dynEvent.cloneObject();
			e.setNamespaces(document.getNamespaces());
						
			document.getModel().addEvent(e);
			
			Event dynamicEvent = document.getModel().getEvent(dynEvent.getId());
			
			String[] splitID = dynamicEvent.getId().split("__");			
			String submodelID = splitID[0];
			
			//length of 3 indicates an array/grid
			boolean isGrid = (splitID.length == 3);
			
			if (isGrid)
				dynamicEvent.setId(dynamicEvent.getId().replace(submodelID + "__",""));
			
			if (properties.getSBMLDocument().getModel().getParameter(submodelID + "__locations") != null) {
				dynamicEvent.setAnnotation(", " + 
					properties.getSBMLDocument().getModel().getParameter(submodelID + "__locations").getAnnotationString()
					.replace("<annotation>","").replace("</annotation>","").trim());
			}
		
//			String reactionAnnotation = dynamicEvent.getAnnotationString();
//			ArrayList<String> components = new ArrayList<String>();
//			
//			String[] splitAnnotation = reactionAnnotation.replace("\"","").split("array:");
//			
//			//find all components in the annotation
//			for (int j = 2; j < splitAnnotation.length; ++j) {
//					
//				components.add(splitAnnotation[j].split("=")[0].trim());
//			}
//			
//			//replace the annotation with a better-formatted version (for jsbml)				
//			XMLAttributes attr = new XMLAttributes();				
//			attr.add("xmlns:array", "http://www.fakeuri.com");
//			
//			for (String componentID : components)
//				attr.add("array:" + componentID, "(" + properties.getSubmodelRow(componentID) + "," +
//						properties.getSubmodelCol(componentID) + ")");
//			
//			XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
//			
//			if (isGrid)
//				dynamicEvent.setAnnotation(node);
		}
		
		//replace all Type=Grid occurences with more complete information
		for (int i = 0; i < document.getModel().getNumReactions(); ++i) {
			
			if (document.getModel().getReaction(i).getAnnotationString() != null &&
					document.getModel().getReaction(i).getAnnotationString().contains("type=\"grid\"")) {
				
				document.getModel().getReaction(i).setAnnotation("");
			}
		}
		
		//replace all Type=Grid occurences with more complete information
		for (int i = 0; i < document.getModel().getNumSpecies(); ++i) {
			
			if (document.getModel().getSpecies(i).getAnnotationString() != null &&
					document.getModel().getSpecies(i).getAnnotationString().contains("type=\"grid\"")) {
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:rowsLowerLimit", "0");
				attr.add("array:colsLowerLimit", "0");
				attr.add("array:rowsUpperLimit", String.valueOf(properties.getGrid().getNumRows() - 1));
				attr.add("array:colsUpperLimit", String.valueOf(properties.getGrid().getNumCols() - 1));
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				document.getModel().getSpecies(i).setAnnotation(node);
			}
		}
		
		//get a list of all components
		//ArrayList<String> components = new ArrayList<String>();
		ArrayList<String> allComponents = new ArrayList<String>();
		
		//search through the parameter location arrays
		for (int i = 0; i < document.getModel().getNumParameters(); ++i) {
			
			Parameter parameter = document.getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				
				String[] splitAnnotation = parameter.getAnnotationString().replace("\"","").split("array:");
				
				for (int j = 2; j < splitAnnotation.length; ++j) {
					allComponents.add(splitAnnotation[j].split("=")[0].trim());
				}
				
//				//replace the locations arrays with correctly-formated versions				
//				XMLAttributes attr = new XMLAttributes();				
//				attr.add("xmlns:array", "http://www.fakeuri.com");
//				
//				for (String componentID : components)
//					attr.add("array:" + componentID, "(" + properties.getSubmodelRow(componentID) + "," +
//							properties.getSubmodelCol(componentID) + ")");
//				
//				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
//				parameter.setAnnotation(node);
			}
		}
		
//		//convert the compartment annotations so that they can be preserved in jsbml
//		for (int i = 0; i < document.getModel().getNumCompartments(); ++i) {
//			
//			if (document.getModel().getCompartment(i).getAnnotationString() != null &&
//					document.getModel().getCompartment(i).getAnnotationString().contains("EnclosingCompartment")) {
//								
//				XMLAttributes attr = new XMLAttributes();				
//				attr.add("xmlns:compartment", "http://www.fakeuri.com");				
//				attr.add("compartment:type", "enclosing");				
//				XMLNode node = new XMLNode(new XMLTriple("compartment","","compartment"), attr);
//				document.getModel().getCompartment(i).setAnnotation(node);
//			}		
//		}		
		
		for (String componentID : allComponents) {
			
			SBMLReader sbmlReader = new SBMLReader();
			Model compModel = sbmlReader.readSBMLFromFile(properties.getPath() + 
					properties.getModelFileName(componentID)).getModel();
			
			//update the kmdiff values for membrane diffusion reactions
			//takes rates from the internal model
			for (int j = 0; j < compModel.getNumReactions(); ++j) {
				
				if (compModel.getReaction(j).getId().contains("MembraneDiffusion")) {
					
					Reaction reaction = compModel.getReaction(j);
					
					LocalParameter kmdiff_r = reaction.getKineticLaw().getLocalParameter("kmdiff_r");
					LocalParameter kmdiff_f = reaction.getKineticLaw().getLocalParameter("kmdiff_f");
					String speciesID = reaction.getReactant(0).getSpecies();
					
					Parameter parameter_r = document.getModel().createParameter();
					Parameter parameter_f = document.getModel().createParameter();
					
					parameter_r.setId(componentID + "__" + speciesID + "__kmdiff_r");
					parameter_f.setId(componentID + "__" + speciesID + "__kmdiff_f");
					parameter_r.setName("Reverse membrane diffusion rate");
					parameter_f.setName("Forward membrane diffusion rate");
					parameter_r.setConstant(true);
					parameter_f.setConstant(true);
					
					if (kmdiff_r == null) {
						
						parameter_r.setValue(compModel.getParameter("kmdiff_r").getValue());
						parameter_f.setValue(compModel.getParameter("kmdiff_f").getValue());
					}
					else {
						parameter_r.setValue(kmdiff_r.getValue());
						parameter_f.setValue(kmdiff_f.getValue());
					}
				}
			}
		}
		
		for (String rtr : reactionsToRemove)
			document.getModel().removeReaction(rtr);
		
		SBMLWriter writer = new SBMLWriter();
		PrintStream p;
		try {
			p = new PrintStream(new FileOutputStream(filename), true, "UTF-8");
			p.print(writer.writeSBMLToString(document));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			String compartment = p.getCompartment();
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
					kl.addParameter(Utility.Parameter(kBasalString, p.getKbasal(), getMoleTimeParameter(1)));
					kl.setFormula(kBasalString + "*" + p.getId() + "_RNAP");

				} else {
					r.setId("R_constitutive_production_" + p.getId());
					kl.addParameter(Utility.Parameter(kOcString, p.getKoc(), getMoleTimeParameter(1)));
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
			String compartment = p.getCompartment();
			Species s = Utility.makeSpecies(p.getId(), compartment,	p.getInitialAmount(), -1);
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
			Species s = Utility.makeSpecies(p.getId(), document.getModel().getCompartment(0).getId(), p.getInitialAmount(), -1);
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
		this.compartments = new HashMap<String,Properties>(); 
		for (long i=0; i < document.getModel().getNumCompartments(); i++) {
			compartments.put(document.getModel().getCompartment(i).getId(), null);
		}
		for (String compartment : compartments.keySet()) {
			Properties prop = compartments.get(compartment);
			if (prop != null && prop.containsKey(GlobalConstants.RNAP_STRING)) {
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
				.getParameter(GlobalConstants.RNAP_STRING)));
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
		for (String complexId : complexMap.keySet()) {
			if (!partsMap.containsKey(complexId)) {
				HashSet<String> partsVisited = new HashSet<String>();
				checkComplex(complexId, partsVisited);
			}
		}
		for (String partId : partsMap.keySet()) {
			SpeciesInterface part = species.get(partId);
			if ((partsMap.get(partId).size() > 1 || part.isActivator() || part.isRepressor()) 
					&& !part.isSequesterAbstractable() && !part.isConvergent()) 
				checkSequester(partId, partId);
		}
	}
	

	//Marks complex as abstractable if it isn't genetic, diffusible, used in an event or rule, or used in a non-degradation reaction.
	//Otherwise unmarks all downstream complexes as abstractable.
	//Marks species as convergent if encountered on more than one downstream branch
	//Recursively checks all downstream complexes.
	private void checkComplex(String complexId, HashSet<String> partsVisited) {
		SpeciesInterface complex = species.get(complexId);
		if (!complex.isAbstractable()) {
			if (!isGenetic(complexId)
					&& !complex.isDiffusible()
					&& !SBMLutilities.variableInUse(document, complexId, false, false, true)
					&& !SBMLutilities.usedInNonDegradationReaction(document, complexId)) {
				complex.setAbstractable(true);
				for (Influence infl : complexMap.get(complexId)) {
					String partId = infl.getInput();
					if (partsVisited.contains(partId))
						species.get(partId).setConvergent(true);
					else
						partsVisited.add(partId);
					if (complexMap.containsKey(partId)) {
						checkComplex(partId, partsVisited);
					}
				}
			} else
				unAbstractDown(complexId, "");
		}
	}
	
	//Marks part as sequesterable after recursively checking that all upstream complexes are abstractable, have a binding stoichiometry of one, 
	//are formed from two or more unique parts, and do not have potentially sequesterable parts on other downstream branches.
	//Otherwise unmarks all upstream complexes as abstractable.
	//Regardless unmarks all downstream complexes as abstractable.
	private boolean checkSequester(String partId, String sequesterRoot) {
		HashSet<SpeciesInterface> abstractableComplexes = new HashSet<SpeciesInterface>();
		for (Influence infl : partsMap.get(partId)) {
			String complexId = infl.getOutput();
			SpeciesInterface complex = species.get(complexId);
			if (complex.isAbstractable() 
					&& infl.getCoop() == 1 
					&& complexMap.get(complexId).size() > 1 
					&& checkSequesterComplex(complexId, partId)
					&& (!partsMap.containsKey(complexId) || complex.isSequesterable() || checkSequester(complexId, sequesterRoot))) 
				abstractableComplexes.add(complex);
			else {
				if (partId.equals(sequesterRoot)) {
					unAbstractUp(partId);
					if (complexMap.containsKey(partId))
						unAbstractDown(partId, "");
				}
				return false;
			}
		}
		if (partId.equals(sequesterRoot)) {
			species.get(partId).setSequesterable(true);
			if (complexMap.containsKey(partId))
				unAbstractDown(partId, "");
		} 
		for (SpeciesInterface complex: abstractableComplexes) {
			complex.setSequesterable(false);
			complex.setSequesterAbstractable(true);
		}
		return true;
	}
	
	//Returns false if sequester complex has potentially sequesterable parts on downstream branches other than lastSequester.
	private boolean checkSequesterComplex(String complexId, String lastSequester) {
		for (Influence infl : complexMap.get(complexId)) {
			String partId = infl.getInput();
			SpeciesInterface part = species.get(partId);
			if (!partId.equals(lastSequester)) {
				if (partsMap.get(partId).size() > 1 
						|| part.isActivator() || part.isRepressor() 
						|| (complexMap.containsKey(partId) && !checkSequesterComplex(partId, "")))
					return false;
			}
		}
		return true;
	}
	
	//Recursively unmarks complexes as abstractable on downstream branches other than payNoMind.
	public void unAbstractDown(String complexId, String payNoMind) {
		species.get(complexId).setAbstractable(false);
		for (Influence infl : complexMap.get(complexId)) {
			String partId = infl.getInput();
			if (!partId.equals(payNoMind) && complexMap.containsKey(partId)) {
				unAbstractDown(partId, "");
			}
		}
	}
	
	//Recursively unmarks all upstream complexes as abstractable.
	public void unAbstractUp(String partId) {
		for (Influence infl : partsMap.get(partId)) {
			String complexId = infl.getOutput();
			unAbstractDown(complexId, partId);
			if (partsMap.containsKey(complexId))
				unAbstractUp(complexId);
			
		}
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
					species.get(id).getType().equals(GlobalConstants.OUTPUT))
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

	public BioModel getProperties() {
		return properties;
	}

	public void setProperties(BioModel properties) {
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

	private BioModel properties = null;
	
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
