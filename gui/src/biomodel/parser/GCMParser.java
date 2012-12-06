package biomodel.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.Model;
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

	public GeneticNetwork buildNetwork() {
		SBMLDocument sbml = biomodel.flattenModel();		
		if (sbml == null) return null;
		return buildTopLevelNetwork(sbml);
	}
	
	public GeneticNetwork buildTopLevelNetwork(SBMLDocument sbml) {		
		
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
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getId()+"_"))!=null &&
							production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getId()+"_"))!=null) {
							infl.setRep(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getId()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getId()+"_")).getValue());
						} else {
							infl.setRep(sbml.getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING).getValue());
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getId() + "_r") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getId() + "_r").getValue());
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
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getId()+"_"))!=null &&
								production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getId()+"_"))!=null) {
							infl.setAct(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getId()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getId()+"_")).getValue());
						} else {
							infl.setAct(sbml.getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING).getValue());
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getId() + "_a") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getId() + "_a").getValue());
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
				Model model = sbml.getModel();
				double kf = model.getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue();
				double kr = model.getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue();
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
	
	
	
//	public SBOLSynthesizer buildSbolSynthesizer() {
//		LinkedList<URI> modelURIs = AnnotationUtility.parseSBOLAnnotation(biomodel.getSBMLDocument().getModel());
//		boolean build = false;
//		if (modelURIs.size() == 0)
//			build = true;
//		else {
//			Iterator<URI> iterate = modelURIs.iterator();
//			do {
//				URI modelURI = iterate.next();
//				if (modelURI.toString().endsWith("iBioSim") || modelURI.toString().endsWith("iBioSimPlaceHolder"))
//					build = true;
//			} while (iterate.hasNext() && !build);
//		}
//		if (build) {
//			boolean sbolDetected = false;
//			synMap = new LinkedHashMap<String, SynthesisNode>(); // initialize map of model element meta IDs to synthesis nodes
//			complexMap = new HashMap<String, ArrayList<Influence>>(); // initialize map of species meta IDs to complex influences 
//			paramInputMap = new HashMap<String, Set<String>>(); // initialize map of parameters to meta IDs for reactions in which they appear
//			paramOutputMap = new HashMap<String, Set<String>>(); // initialize map of parameters to meta IDs for rules in which they're outputs
//			speciesMetaMap = new HashMap<String, String>(); // initialize map of species IDs to meta IDs
//			SBMLDocument sbmlDoc;
//			if (detectSBOLReplacements(biomodel)) {
//				Preferences biosimrc = Preferences.userRoot();
//				if (biosimrc.get("biosim.general.flatten", "").equals("libsbml")) 
//					sbmlDoc = biomodel.newFlattenModel();
//				else 
//					sbmlDoc = flattenSubModelsWithSBOLReplacements(biomodel);
//			} else
//				sbmlDoc = biomodel.getSBMLDocument();
//			Model sbmlModel = sbmlDoc.getModel();
//			for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//				Species sbmlSpecies = sbmlModel.getSpecies(i);
//				speciesMetaMap.put(sbmlSpecies.getId(), sbmlSpecies.getMetaId());
//			}
//
//			// Create synthesis nodes for components (submodels)
//			// Must come before parseSpeciesSbol() since latter connects nodes for species to nodes for submodels
//			if (sbmlDoc.isPackageEnabled("comp") && parseSubModelSBOL(sbmlDoc))
//				sbolDetected = true;
//
//			// Create and connect synthesis nodes for species and promoters
//			// Note that all non-promoter species must be parsed first since parsePromoterSbol may refer to them
//			// when connecting synthesis nodes
//			for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//				Species sbmlSpecies = sbmlModel.getSpecies(i);
//				if (!BioModel.isPromoterSpecies(sbmlSpecies)) 
//					if (parseSpeciesSbol(sbmlModel, sbmlSpecies))
//						sbolDetected = true;
//			}
//			for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//				Species sbmlSpecies = sbmlModel.getSpecies(i);
//				if (BioModel.isPromoterSpecies(sbmlSpecies)) 
//					if (parsePromoterSbol(sbmlModel, sbmlSpecies))
//						sbolDetected = true;
//			}
//			// Create and connect synthesis nodes for reactions not auto-generated by iBioSim
//			for (long i = 0; i < sbmlModel.getNumReactions(); i++) {
//				Reaction r = sbmlModel.getReaction(i);
//				if (BioModel.isDegradationReaction(r)) continue;
//				if (BioModel.isDiffusionReaction(r)) continue;
//				if (BioModel.isProductionReaction(r)) continue;
//				if (BioModel.isComplexReaction(r)) continue;
//				if (BioModel.isConstitutiveReaction(r)) continue;
//				if (r.getAnnotationString().contains("grid")) continue;
//				if (parseReactionSbol(r))
//					sbolDetected = true;
//			}
//			// Create and connect synthesis nodes for rules
//			for (long i = 0; i < sbmlModel.getNumRules(); i++) {
//				if (parseRuleSbol(sbmlModel.getRule(i)))
//					sbolDetected = true;
//			}
//
//			// Finishes connecting synthesis nodes in accordance with various maps (see above)
//			// if no SBOL detected, then removes placeholder URI for iBioSim composite component instead
//			if (sbolDetected) {
//				connectMappedSynthesisNodes();
//				SBOLSynthesizer synthesizer = new SBOLSynthesizer(biomodel, modelURIs, synMap);
//				return synthesizer;
//			} else { 
//				int index = 0;
//				int removeIndex = -1;
//				for (URI modelURI : modelURIs) {
//					if (modelURI.toString().endsWith("iBioSimPlaceHolder"))
//						removeIndex = index;
//					index++;
//				}
//				if (removeIndex >= 0) {
//					modelURIs.remove(removeIndex);
//					Model originalSBMLModel = biomodel.getSBMLDocument().getModel();
//					if (modelURIs.size() > 0) {
//						SBOLAnnotation sbolAnnot = new SBOLAnnotation(originalSBMLModel.getMetaId(), modelURIs);
//						AnnotationUtility.setSBOLAnnotation(originalSBMLModel, sbolAnnot);
//					} else
//						AnnotationUtility.removeSBOLAnnotation(originalSBMLModel);
//				}
//			}
//		}
//		return null;
//	}
//	
//	private boolean detectSBOLReplacements(BioModel biomodel) {
//		CompModelPlugin compSBMLModel = biomodel.getSBMLCompModel();
//		CompSBMLDocumentPlugin compSBMLDoc = biomodel.getSBMLComp();
//		for (long i = 0; i < compSBMLModel.getNumSubmodels(); i++) {
//			Submodel submodel = compSBMLModel.getSubmodel(i);
//			if (submodel.getNumDeletions() > 0) {
//				String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//				BioModel subBioModel = new BioModel(biomodel.getPath());
//				subBioModel.load(subSBMLFileID);
//				Model subSBMLModel = subBioModel.getSBMLDocument().getModel();
//				CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//				for (long j = 0; j < submodel.getNumDeletions(); j++) {
//					String speciesID = subCompSBMLModel.getPort(submodel.getDeletion(j).getPortRef()).getIdRef();
//					if (AnnotationUtility.parseSBOLAnnotation(subSBMLModel.getSpecies(speciesID)).size() > 0) {
//						return true;
//					}
//				}
//			}
//		}
//		Model sbmlModel = biomodel.getSBMLDocument().getModel();
//		for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//			CompSBasePlugin compSBMLSpecies = (CompSBasePlugin) sbmlModel.getSpecies(i).getPlugin("comp");
//			for (long j = 0; j < compSBMLSpecies.getNumReplacedElements(); j++) {
//				ReplacedElement replacement = compSBMLSpecies.getReplacedElement(j);
//				if (AnnotationUtility.parseSBOLAnnotation(sbmlModel.getSpecies(i)).size() > 0) 
//					return true;
//				else {
//					Submodel submodel = compSBMLModel.getSubmodel(replacement.getSubmodelRef());
//					String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//					BioModel subBioModel = new BioModel(biomodel.getPath());
//					subBioModel.load(subSBMLFileID);
//					CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//					String speciesID = subCompSBMLModel.getPort(replacement.getPortRef()).getIdRef();
//					Model subSBMLModel = subBioModel.getSBMLDocument().getModel();
//					if (AnnotationUtility.parseSBOLAnnotation(subSBMLModel.getSpecies(speciesID)).size() > 0) 
//						return true;
//				}
//			}
//		}
//		return false;
//	}
//	
//	// Only flattens submodels with SBOL-annotated elements that are to be replaced/deleted
//	// or with elements that will be replaced by SBOL-annotated elements
//	private SBMLDocument flattenSubModelsWithSBOLReplacements(BioModel biomodel) {
//		Set<String> flatteningSubModelIDs;
//		do {
//			flatteningSubModelIDs = identifySubModelsWithSBOLReplacements(biomodel);
//			for (String subModelID : flatteningSubModelIDs) {
//				biomodel = biomodel.partiallyFlattenModel(subModelID);
//			}
//			Model sbmlModel = biomodel.getSBMLDocument().getModel();
//			for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//				CompSBasePlugin compSBMLSpecies = (CompSBasePlugin) sbmlModel.getSpecies(i).getPlugin("comp");
//				for (long j = compSBMLSpecies.getNumReplacedElements() - 1; j >= 0; j--) {
//					ReplacedElement replacement = compSBMLSpecies.getReplacedElement(j);
//					if (flatteningSubModelIDs.contains(replacement.getSubmodelRef()))
//						replacement.removeFromParentAndDelete();
//				}
//			}
//			CompModelPlugin compSBMLModel = biomodel.getSBMLCompModel();
//			for (String subModelID : flatteningSubModelIDs)
//				compSBMLModel.getSubmodel(subModelID).removeFromParentAndDelete();
//			CompSBMLDocumentPlugin compSBMLDoc = biomodel.getSBMLComp();
//			Set<String> remainingModelRefs = new HashSet<String>();
//			for (long i = 0; i < compSBMLModel.getNumSubmodels(); i++)
//				remainingModelRefs.add(compSBMLModel.getSubmodel(i).getModelRef());
//			for (long i = compSBMLDoc.getNumExternalModelDefinitions() - 1; i >= 0; i--) {
//				ExternalModelDefinition extDef = compSBMLDoc.getExternalModelDefinition(i);
//				if (!remainingModelRefs.contains(extDef.getId()))
//					extDef.removeFromParentAndDelete();
//			}
//		} while(biomodel.getSBMLCompModel().getNumSubmodels() > 0 && flatteningSubModelIDs.size() > 0);
//		return biomodel.getSBMLDocument();
//	}
//	
//	private Set<String> identifySubModelsWithSBOLReplacements(BioModel biomodel) {
//		Set<String> subModelIDs = new HashSet<String>();
//		CompModelPlugin compSBMLModel = biomodel.getSBMLCompModel();
//		CompSBMLDocumentPlugin compSBMLDoc = biomodel.getSBMLComp();
//		for (long i = 0; i < compSBMLModel.getNumSubmodels(); i++) {
//			Submodel submodel = compSBMLModel.getSubmodel(i);
//			if (submodel.getNumDeletions() > 0) {
//				String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//				BioModel subBioModel = new BioModel(biomodel.getPath());
//				subBioModel.load(subSBMLFileID);
//				Model subSBMLModel = subBioModel.getSBMLDocument().getModel();
//				CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//				for (long j = 0; j < submodel.getNumDeletions(); j++) {
//					String speciesID = subCompSBMLModel.getPort(submodel.getDeletion(j).getPortRef()).getIdRef();
//					if (AnnotationUtility.parseSBOLAnnotation(subSBMLModel.getSpecies(speciesID)).size() > 0) {
//						subModelIDs.add(submodel.getId());
//						j = submodel.getNumDeletions();
//					}
//				}
//			}
//		}
//		Model sbmlModel = biomodel.getSBMLDocument().getModel();
//		for (long i = 0; i < sbmlModel.getNumSpecies(); i++) {
//			CompSBasePlugin compSBMLSpecies = (CompSBasePlugin) sbmlModel.getSpecies(i).getPlugin("comp");
//			for (long j = 0; j < compSBMLSpecies.getNumReplacedElements(); j++) {
//				ReplacedElement replacement = compSBMLSpecies.getReplacedElement(j);
//				if (AnnotationUtility.parseSBOLAnnotation(sbmlModel.getSpecies(i)).size() > 0) 
//					subModelIDs.add(replacement.getSubmodelRef());
//				else {
//					Submodel submodel = compSBMLModel.getSubmodel(replacement.getSubmodelRef());
//					String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//					BioModel subBioModel = new BioModel(biomodel.getPath());
//					subBioModel.load(subSBMLFileID);
//					CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//					String speciesID = subCompSBMLModel.getPort(replacement.getPortRef()).getIdRef();
//					Model subSBMLModel = subBioModel.getSBMLDocument().getModel();
//					if (AnnotationUtility.parseSBOLAnnotation(subSBMLModel.getSpecies(speciesID)).size() > 0) 
//						subModelIDs.add(replacement.getSubmodelRef());
//				}
//			}
//		}
//		return subModelIDs;
//	}
//	
//	// Uses SBOL associated to submodel instantiation if present
//	// Otherwise constructs SBOL synthesizer for submodel or uses SBOL associated to submodel itself
//	private boolean parseSubModelSBOL(SBMLDocument sbmlDoc) {
//		CompModelPlugin sbmlCompModel = (CompModelPlugin) sbmlDoc.getModel().getPlugin("comp");
//		CompSBMLDocumentPlugin compSBMLDoc = (CompSBMLDocumentPlugin) sbmlDoc.getPlugin("comp");
//		boolean sbolDetected = false;
//		for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
//			Submodel submodel = sbmlCompModel.getSubmodel(i);
//			String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//			BioModel subBioModel = new BioModel(biomodel.getPath());
//			subBioModel.load(subSBMLFileID);
//			Model sbmlSubModel = subBioModel.getSBMLDocument().getModel();
//			
//			SynthesisNode synNode;
//			LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(submodel);
//			if (sbolURIs.size() > 0) {
//				synNode = new SynthesisNode(submodel.getId(), sbolURIs);
//				sbolDetected = true;
//			} else {
//				sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlSubModel);
//				if (sbolURIs.size() > 0) {
//					synNode = new SynthesisNode(submodel.getId(), sbolURIs);
//					sbolDetected = true;
//				} else {
//					GCMParser subParser = new GCMParser(subBioModel, false);
//					SBOLSynthesizer sbolSynth = subParser.buildSbolSynthesizer();
//					if (sbolSynth != null) {
//						synNode = new SynthesisNode(submodel.getId(), sbolURIs, sbolSynth);
//						sbolDetected = true;
//					} else
//						synNode = new SynthesisNode(submodel.getId(), sbolURIs);
//				}
//			}
//			synMap.put(synNode.getID(), synNode);
//		}
//		return sbolDetected; 
//	}
//	
//	private boolean parseSpeciesSbol(Model sbmlModel, Species sbmlSpecies) {
//		// Create synthesis node corresponding to sbml species
//		LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlSpecies);
//		SynthesisNode synNode = new SynthesisNode(sbmlSpecies.getMetaId(), sbolURIs);
//		
//		synMap.put(synNode.getID(), synNode);
//		// Determine if species belongs to a gcm component
//		String component = "";
//		String speciesId = sbmlSpecies.getId();
//		if (sbmlSpecies.getId().contains("__")) {
//			component = sbmlSpecies.getId().substring(0, sbmlSpecies.getId().lastIndexOf("__") + 2);
//			speciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
//		}
//		// Build complex map for use in connecting synthesis nodes
//		// Remove species complex formation reaction from sbml
//		Reaction complexFormation = sbmlModel.getReaction(component + "Complex_" + speciesId);
//		if (complexFormation != null) {
//			for (long i = 0; i < complexFormation.getNumReactants(); i++) {
//				Influence infl = new Influence();		
//				String inputMetaID = speciesMetaMap.get(complexFormation.getReactant(i).getSpecies());
//				String outputMetaID = speciesMetaMap.get(complexFormation.getProduct(0).getSpecies());
//				infl.setInput(inputMetaID);
//				//Maps complex species to complex formation influences for which they're outputs
//				ArrayList<Influence> complexInfluences = null;
//				if (complexMap.containsKey(outputMetaID)) {
//					complexInfluences = complexMap.get(outputMetaID);
//				} else { 
//					complexInfluences = new ArrayList<Influence>();
//					complexMap.put(outputMetaID, complexInfluences);
//				}
//				complexInfluences.add(infl);
//			} 
//		}
//		// Connect synthesis nodes of species to nodes of components for which they're inputs/outputs
//		CompModelPlugin compSBMLModel = biomodel.getSBMLCompModel();
//		CompSBMLDocumentPlugin compSBMLDoc = biomodel.getSBMLComp();
//		CompSBasePlugin compSBMLSpecies = (CompSBasePlugin) sbmlSpecies.getPlugin("comp");
//		for (long i = 0; i < compSBMLSpecies.getNumReplacedElements(); i++) {
//			ReplacedElement replacement = compSBMLSpecies.getReplacedElement(i);
//			Submodel submodel = compSBMLModel.getSubmodel(replacement.getSubmodelRef());
//			String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//			BioModel subBioModel = new BioModel(biomodel.getPath());
//			subBioModel.load(subSBMLFileID);
//			CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//			SynthesisNode modelSynNode = synMap.get(replacement.getSubmodelRef());
//			if (BioModel.isInputPort(subCompSBMLModel.getPort(replacement.getPortRef())))
//				synNode.addNextNode(modelSynNode);
//			else if (BioModel.isOutputPort(subCompSBMLModel.getPort(replacement.getPortRef())))
//				modelSynNode.addNextNode(synNode);
//		}
//		ReplacedBy replacedBy = compSBMLSpecies.getReplacedBy();
//		if (replacedBy != null) {
//			Submodel submodel = compSBMLModel.getSubmodel(replacedBy.getSubmodelRef());
//			String subSBMLFileID = compSBMLDoc.getExternalModelDefinition(submodel.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
//			BioModel subBioModel = new BioModel(biomodel.getPath());
//			subBioModel.load(subSBMLFileID);
//			CompModelPlugin subCompSBMLModel = subBioModel.getSBMLCompModel();
//			SynthesisNode modelSynNode = synMap.get(replacedBy.getSubmodelRef());
//			if (BioModel.isInputPort(subCompSBMLModel.getPort(replacedBy.getPortRef())))
//				synNode.addNextNode(modelSynNode);
//			else if (BioModel.isOutputPort(subCompSBMLModel.getPort(replacedBy.getPortRef())))
//				modelSynNode.addNextNode(synNode);
//		}
//		return sbolURIs.size() > 0;
//	}
//	
//	private boolean parsePromoterSbol(Model sbmlModel, Species sbmlPromoter) {
//		// Create synthesis node corresponding to sbml promoter 
//		LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlPromoter);
//		SynthesisNode synNode = new SynthesisNode(sbmlPromoter.getMetaId(), sbolURIs);
//
//		synMap.put(synNode.getID(), synNode);
//		// Determine if promoter belongs to a component
//		String component = "";
//		String promoterId = sbmlPromoter.getId();
//		if (sbmlPromoter.getId().contains("__")) {
//			component = sbmlPromoter.getId().substring(0, sbmlPromoter.getId().lastIndexOf("__")+2);
//			promoterId = promoterId.substring(promoterId.lastIndexOf("__")+2);
//		}
//		// Connect synthesis node for promoter to synthesis nodes for its products
//		// Remove promoter production reaction from sbml
//		Reaction production = sbmlModel.getModel().getReaction(component + "Production_" + promoterId);
//		if (production != null) {
//			for (long i = 0; i < production.getNumProducts(); i++) {
//				String productMetaID = speciesMetaMap.get(production.getProduct(i).getSpecies());
//				synNode.addNextNode(synMap.get(productMetaID));
//			}
//		}
//		return sbolURIs.size() > 0;
//	}
//	
//	private boolean parseReactionSbol(Reaction sbmlReaction) {
//		// Create synthesis node corresponding to sbml reaction
//		LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlReaction);
//		SynthesisNode synNode = new SynthesisNode(sbmlReaction.getMetaId(), sbolURIs);
//		
//		synMap.put(synNode.getID(), synNode);
//		// Connect synthesis nodes for reactants, modifiers to synthesis node for reaction
//		for (long i = 0; i < sbmlReaction.getNumReactants(); i++) {
//			String reactantMetaID = speciesMetaMap.get(sbmlReaction.getReactant(i).getSpecies());
//			synMap.get(reactantMetaID).addNextNode(synNode);
//		}
//		for (long i = 0; i < sbmlReaction.getNumModifiers(); i++) {
//			String modifierMetaID = speciesMetaMap.get(sbmlReaction.getModifier(i).getSpecies());
//			synMap.get(modifierMetaID).addNextNode(synNode);
//		}
//		// Connect synthesis node for reaction to synthesis nodes for its products
//		for (long i = 0; i < sbmlReaction.getNumProducts(); i++) {
//			String productMetaID = speciesMetaMap.get(sbmlReaction.getProduct(i).getSpecies());
//			synNode.addNextNode(synMap.get(productMetaID));
//		}
//		// Map global parameters to reactions in which they appear
//		KineticLaw kl = sbmlReaction.getKineticLaw();
//		Set<String> localParams = new HashSet<String>();
//		if (kl != null) {
//			for (long i = 0; i < kl.getNumParameters(); i++) {
//				localParams.add(kl.getParameter(i).getId());
//			}
//			for (String input : parseInputHelper(kl.getMath())) {
//				if (!speciesMetaMap.containsKey(input) && !localParams.contains(input)) {
//					if (!paramInputMap.containsKey(input))
//						paramInputMap.put(input, new HashSet<String>());;
//						paramInputMap.get(input).add(sbmlReaction.getMetaId());
//				}
//			}
//		}
//		return sbolURIs.size() > 0;
//	}
//	
//	private boolean parseRuleSbol(Rule sbmlRule) {
//		// Create synthesis node corresponding to sbml rule
//		LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlRule);
//		SynthesisNode synNode = new SynthesisNode(sbmlRule.getMetaId(), sbolURIs);
//		
//		synMap.put(synNode.getID(), synNode);
//		// Connect synthesis nodes for input species to synthesis node for rule
//		// or maps input parameters to rules
//		for (String input : parseInputHelper(sbmlRule.getMath())) {
//			if (speciesMetaMap.containsKey(input))
//				synMap.get(speciesMetaMap.get(input)).addNextNode(synNode);
//			else {
//				if (!paramInputMap.containsKey(input))
//					paramInputMap.put(input, new HashSet<String>());
//				paramInputMap.get(input).add(sbmlRule.getMetaId());
//			}
//		}
//		// Connects synthesis node for rule to synthesis node for its output species
//		// or maps output parameter to rule
//		String output = sbmlRule.getVariable();
//		if (output != null) {
//			if (speciesMetaMap.containsKey(output))
//				synNode.addNextNode(synMap.get(speciesMetaMap.get(output)));
//			else {
//				if (!paramOutputMap.containsKey(output))
//					paramOutputMap.put(output, new HashSet<String>());
//				paramOutputMap.get(output).add(sbmlRule.getMetaId());
//			}
//		}
//		return sbolURIs.size() > 0;
//	}
//	
//	private LinkedList<String> parseInputHelper(ASTNode astNode) {
//		LinkedList<String> inputs = new LinkedList<String>();
//		for (long i = 0; i < astNode.getNumChildren(); i++) {
//			ASTNode childNode = astNode.getChild(i);
//			if (!childNode.isOperator() && !childNode.isNumber())
//				inputs.add(childNode.getName());
//			inputs.addAll(parseInputHelper(childNode));
//		}
//		return inputs;
//	}
//	
//	private void connectMappedSynthesisNodes() {
//		// Connect synthesis nodes for species that form complexes
//		for (String complexId : complexMap.keySet())
//			for (Influence infl : complexMap.get(complexId))
//				synMap.get(infl.getInput()).addNextNode(synMap.get(complexId));
//		// Connect synthesis nodes for rules to synthesis nodes for rules or reactions on the basis of shared parameters
//		for (String param : paramInputMap.keySet())
//			if (paramOutputMap.containsKey(param)) 
//				for (String origin : paramOutputMap.get(param))
//					for (String destination : paramInputMap.get(param))
//						synMap.get(origin).addNextNode(synMap.get(destination));
//	}

	/*
	public void setParameters(HashMap<String, String> parameters) {
		gcm.setParameters(parameters);
	}
	*/
	
	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> speciesList;
	private HashMap<String, Promoter> promoterList;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;
//	private LinkedHashMap<String, SynthesisNode> synMap;
//	private HashMap<String, Set<String>> paramInputMap;
//	private HashMap<String, Set<String>> paramOutputMap;
//	private HashMap<String, String> speciesMetaMap;

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
