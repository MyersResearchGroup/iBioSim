package biomodel.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

import biomodel.network.BaseSpecies;
import biomodel.network.DiffusibleConstitutiveSpecies;
import biomodel.network.DiffusibleSpecies;
import biomodel.network.GeneticNetwork;
import biomodel.network.Influence;
import biomodel.network.Promoter;
import biomodel.network.SpasticSpecies;
import biomodel.network.SpeciesInterface;
//import biomodel.network.SynthesisNode;
import biomodel.util.GlobalConstants;

/**
 * This class parses a genetic circuit model.
 * 
 * @author Nam Nguyen
 * 
 */
public class GCMParser {
	
	private String separator;

	public GCMParser(String filename) {
		this(filename, false);
	}

	public GCMParser(String filename, boolean debug) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		//this.debug = debug;
		biomodel = new BioModel(filename.substring(0, filename.length()
				- filename.split(separator)[filename.split(separator).length - 1]
						.length()));
		biomodel.load(filename);
		
		data = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}
	}
	
	public GCMParser(BioModel gcm, boolean debug) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		//this.debug = debug;
		this.biomodel = gcm;
	}
	
	public GeneticNetwork buildNetwork(SBMLDocument sbml) {		
		
		if (sbml == null) return null;

		speciesList = new HashMap<String, SpeciesInterface>();
		promoterList = new HashMap<String, Promoter>();
		complexMap = new HashMap<String, ArrayList<Influence>>();
		partsMap = new HashMap<String, ArrayList<Influence>>();

		// Need to first parse all species that aren't promoters before parsing promoters
		// since latter process refers to the species list
		for (long i=0; i<sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (!BioModel.isPromoterSpecies(species)) 
				parseSpeciesData(sbml,species);
		}
		for (long i=0; i<sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (BioModel.isPromoterSpecies(species))
				parsePromoterData(sbml,species);
		}
		
		GeneticNetwork network = new GeneticNetwork(speciesList, complexMap, partsMap, promoterList, biomodel);
		
		network.setSBMLFile(biomodel.getSBMLFile());
		if (sbml != null) {
			network.setSBML(sbml);
		}
		
		return network;		
	}

	public HashMap<String, SpeciesInterface> getSpecies() {
		return speciesList;
	}

	public void setSpecies(HashMap<String, SpeciesInterface> speciesList) {
		this.speciesList = speciesList;
	}

	public HashMap<String, Promoter> getPromoters() {
		return promoterList;
	}

	public void setPromoters(HashMap<String, Promoter> promoterList) {
		this.promoterList = promoterList;
	}

	private void parsePromoterData(SBMLDocument sbml, Species promoter) {
		Promoter p = new Promoter();
		p.setId(promoter.getId());
		promoterList.put(promoter.getId(), p);
		p.setInitialAmount(promoter.getInitialAmount());
		String component = "";
		String promoterId = promoter.getId();
		if (promoter.getId().contains("__")) {
			component = promoter.getId().substring(0,promoter.getId().lastIndexOf("__")+2);
			promoterId = promoterId.substring(promoterId.lastIndexOf("__")+2);
		}
		Reaction production = sbml.getModel().getReaction(component+"Production_"+promoterId);
		if (production != null) {
			p.setCompartment(production.getCompartment());
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING) != null) {
				p.setKact(production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING).getValue());
			} else {
				p.setKact(sbml.getModel().getParameter(GlobalConstants.ACTIVATED_STRING).getValue());
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING) != null) {
				p.setKbasal(production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING).getValue());
			} else {
				p.setKbasal(sbml.getModel().getParameter(GlobalConstants.KBASAL_STRING).getValue());
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING) != null) {
				p.setKoc(production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING).getValue());
			} else {
				p.setKoc(sbml.getModel().getParameter(GlobalConstants.OCR_STRING).getValue());
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING) != null) {
				p.setStoich(production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else {
				p.setStoich(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING)!=null &&
				production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING)!=null) {
				p.setKrnap(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING).getValue(),
						   production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING).getValue());
			} else {
				p.setKrnap(sbml.getModel().getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING).getValue(),
						   sbml.getModel().getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING).getValue());
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING)!=null &&
					production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING)!=null) {
					p.setKArnap(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING).getValue(),
							    production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING).getValue());
				} else {
					p.setKArnap(sbml.getModel().getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING).getValue(),
							    sbml.getModel().getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING).getValue());
				}
			for (long j = 0; j < production.getNumProducts(); j++) {
				SpeciesReference product = production.getProduct(j);
				if (!BioModel.isMRNASpecies(sbml.getModel().getSpecies(product.getSpecies())))
					p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
			}
			for (long i = 0; i < production.getNumModifiers(); i++) {
				ModifierSpeciesReference modifier = production.getModifier(i);
				if (BioModel.isRepressor(modifier) || BioModel.isRegulator(modifier)) {
					for (long j = 0; j < production.getNumProducts(); j++) {
						SpeciesReference product = production.getProduct(j);
						Influence infl = new Influence();		
						infl.generateName();
						infl.setType("tee");
						infl.setInput(modifier.getSpecies());
						if (BioModel.isMRNASpecies(sbml.getModel().getSpecies(product.getSpecies()))) {
							infl.setOutput("none");
						} else {
							infl.setOutput(product.getSpecies());
//							p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
						}
						p.addToReactionMap(modifier.getSpecies(), infl);
						p.addRepressor(modifier.getSpecies(), speciesList.get(modifier.getSpecies()));
						speciesList.get(modifier.getSpecies()).setRepressor(true);
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null &&
							production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null) {
							infl.setRep(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue());
						} else {
							infl.setRep(sbml.getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING).getValue());
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_r") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_r").getValue());
						} else {
							infl.setCoop(sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue());
						}
					}
				} 
				if (BioModel.isActivator(modifier) || BioModel.isRegulator(modifier)) {
					for (long j = 0; j < production.getNumProducts(); j++) {
						SpeciesReference product = production.getProduct(j);
						Influence infl = new Influence();		
						infl.generateName();
						infl.setType("vee");
						infl.setInput(modifier.getSpecies());
						if (BioModel.isMRNASpecies(sbml.getModel().getSpecies(product.getSpecies()))) {
							infl.setOutput("none");
						} else {
							infl.setOutput(product.getSpecies());
//							p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
						}
						p.addToReactionMap(modifier.getSpecies(), infl);
						p.addActivator(modifier.getSpecies(), speciesList.get(modifier.getSpecies()));
						speciesList.get(modifier.getSpecies()).setActivator(true);
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null &&
								production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null) {
							infl.setAct(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue());
						} else {
							infl.setAct(sbml.getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING).getValue());
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_a") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_a").getValue());
						} else {
							infl.setCoop(sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue());
						}
					}
				} 
			}
			sbml.getModel().removeReaction(production.getId());
		}
	}

	/**
	 * Parses the data and put it into the species
	 * 
	 * @param name
	 *            the name of the species
	 * @param properties
	 *            the properties of the species
	 */
	private void parseSpeciesData(SBMLDocument sbml,Species species) {
		
		SpeciesInterface speciesIF = null;

		String component = "";
		String speciesId = species.getId();
		if (species.getId().contains("__")) {
			component = species.getId().substring(0,species.getId().lastIndexOf("__")+2);
			speciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction degradation = sbml.getModel().getReaction(component+"Degradation_"+speciesId);
		Reaction diffusion = sbml.getModel().getReaction(component+"MembraneDiffusion_"+speciesId);
		Reaction constitutive = sbml.getModel().getReaction(component+"Constitutive_"+speciesId);
		Reaction complex = sbml.getModel().getReaction(component+"Complex_"+speciesId);
		
		/*if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.CONSTANT)) {
			speciesIF = new ConstantSpecies();
		} 
		else*/ 
		if (diffusion != null && constitutive != null) {
			speciesIF = new DiffusibleConstitutiveSpecies();
			speciesIF.setDiffusible(true);
		} 
		else if (diffusion != null) {
			speciesIF = new DiffusibleSpecies();
			speciesIF.setDiffusible(true);
		} 
		else if (constitutive != null) {
			speciesIF = new SpasticSpecies();
			speciesIF.setDiffusible(false);
		}
		else {
			speciesIF = new BaseSpecies();
			speciesIF.setDiffusible(false);
		}
		speciesList.put(species.getId(), speciesIF);
		
		speciesIF.setType(BioModel.getSpeciesType(sbml,species.getId()));
//		String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
//		String [] annotations = annotation.split(",");
//		for (int i=0;i<annotations.length;i++) 
//			if (annotations[i].startsWith(GlobalConstants.TYPE)) {
//				String [] type = annotations[i].split("=");
//				speciesIF.setType(type[1]);
//			}
//			} else if (annotations[i].startsWith(GlobalConstants.SBOL_RBS)) {
//				String [] type = annotations[i].split("=");
//				speciesIF.setRBS(type[1]);
//			} else if (annotations[i].startsWith(GlobalConstants.SBOL_ORF)) {
//				String [] type = annotations[i].split("=");
//				speciesIF.setORF(type[1]);
//			}  
//		}
		if (species.isSetInitialAmount()) {
			speciesIF.setInitialAmount(species.getInitialAmount());
		} else if (species.isSetInitialConcentration()) {
			speciesIF.setInitialConcentration(species.getInitialConcentration());
		}  
		
		if (constitutive != null) {
			if (constitutive.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING)!=null) {
				speciesIF.setKo(constitutive.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING).getValue());
			} else {
				speciesIF.setKo(sbml.getModel().getParameter(GlobalConstants.OCR_STRING).getValue());
			}
			if (constitutive.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)!=null) {
				speciesIF.setnp(constitutive.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else {
				speciesIF.setnp(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			}
			sbml.getModel().removeReaction(constitutive.getId());
		} else {
			speciesIF.setKo(0.0);
			speciesIF.setnp(0.0);
		}
		
		if (degradation != null) {
			if (degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING)!=null) {
				speciesIF.setDecay(degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING).getValue());
			} else {
				speciesIF.setDecay(sbml.getModel().getParameter(GlobalConstants.KDECAY_STRING).getValue());
			}
			
			if (degradation.getAnnotationString().contains("grid") == false)
				sbml.getModel().removeReaction(degradation.getId());
		} else {
			speciesIF.setDecay(0.0);
		}
		
		if (complex != null) {
			if (complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING)!=null &&
					complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING)!=null) {
				speciesIF.setKc(complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue(),
						complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue());
			} else {
				//Model model = sbml.getModel();
				//double kf = model.getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue();
				//double kr = model.getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue();
				speciesIF.setKc(sbml.getModel().getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue(),
						sbml.getModel().getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue());
			}
		} else {
			speciesIF.setKc(0.0,1.0);
		}
		
		if (diffusion != null) {
			if (diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING)!=null &&
				diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING)!=null) {
				speciesIF.setKmdiff(diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING).getValue(),
						diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING).getValue());
			} else {
				speciesIF.setKmdiff(sbml.getModel().getParameter(GlobalConstants.FORWARD_MEMDIFF_STRING).getValue(),
						sbml.getModel().getParameter(GlobalConstants.REVERSE_MEMDIFF_STRING).getValue());
			}
			//sbml.getModel().removeReaction(diffusion.getId());
		} else {
			speciesIF.setKmdiff(0.0,1.0);
		}
		/*
		if (property.containsKey(GlobalConstants.KASSOCIATION_STRING)) {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, property.getProperty(GlobalConstants.KASSOCIATION_STRING));
		} else {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, gcm.getParameter(GlobalConstants.KASSOCIATION_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDIFF_STRING)) {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, property.getProperty(GlobalConstants.KECDIFF_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, gcm.getParameter(GlobalConstants.KECDIFF_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDECAY_STRING)) {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, property.getProperty(GlobalConstants.KECDECAY_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, gcm.getParameter(GlobalConstants.KECDECAY_STRING));
		}
		*/
		
		speciesIF.setId(species.getId());
		speciesIF.setName(species.getName());
		speciesIF.setStateName(species.getId());
		
		if (complex != null) {
			for (long i = 0; i < complex.getNumReactants(); i++) {
				Influence infl = new Influence();		
				infl.generateName();		
				infl.setType("plus");
				infl.setCoop(complex.getReactant(i).getStoichiometry());
				String input = complex.getReactant(i).getSpecies();
				String output = complex.getProduct(0).getSpecies();
				infl.setInput(input);
				infl.setOutput(output);
				//Maps complex species to complex formation influences of which they're outputs
				ArrayList<Influence> complexInfluences = null;
				if (complexMap.containsKey(output)) {
					complexInfluences = complexMap.get(output);
				} else { 
					complexInfluences = new ArrayList<Influence>();
					complexMap.put(output, complexInfluences);
				}
				complexInfluences.add(infl);
				//Maps part species to complex formation influences of which they're inputs
				complexInfluences = null;
				if (partsMap.containsKey(input)) {
					complexInfluences = partsMap.get(input);
				} else { 
					complexInfluences = new ArrayList<Influence>();
					partsMap.put(input, complexInfluences);
				}
				complexInfluences.add(infl);
			} 
			sbml.getModel().removeReaction(complex.getId());
		}
	}
	
	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> speciesList;
	private HashMap<String, Promoter> promoterList;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;

	private BioModel biomodel = null;

	// A regex that matches information
	//private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	//private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[(.*)arrowhead=([^,\\]]*)(.*)";

	//private static final String PROPERTY_NUMBER = "([a-zA-Z]+)=\"([\\d]*[\\.\\d]?\\d+)\"";

	// private static final String PROPERTY_STATE = "([a-zA-Z]+)=([^\\s,.\"]+)";

	// private static final String PROPERTY_QUOTE =
	// "([a-zA-Z]+)=\"([^\\s,.\"]+)\"";

	//private static final String PROPERTY_STATE = "([a-zA-Z\\s\\-]+)=([^\\s,]+)";

	// Debug level
	//private boolean debug = false;
}
