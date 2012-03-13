package biomodel.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

import biomodel.network.BaseSpecies;
import biomodel.network.ConstantSpecies;
import biomodel.network.DiffusibleConstitutiveSpecies;
import biomodel.network.DiffusibleSpecies;
import biomodel.network.GeneticNetwork;
import biomodel.network.Influence;
import biomodel.network.Promoter;
import biomodel.network.SpasticSpecies;
import biomodel.network.SpeciesInterface;
import biomodel.network.SynthesisNode;
import biomodel.util.GlobalConstants;

import sbol.SbolSynthesizer;

/**
 * This class parses a genetic circuit model.
 * 
 * @author Nam Nguyen
 * 
 */
public class GCMParser {
	
	private String separator;

	public GCMParser(String filename) {
		//this(filename, false);
	}

	public GCMParser(String filename, boolean debug) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		//this.debug = debug;
		gcm = new BioModel(filename.substring(0, filename.length()
				- filename.split(separator)[filename.split(separator).length - 1]
						.length()));
		gcm.load(filename);
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
		this.gcm = gcm;
	}

	public GeneticNetwork buildNetwork() {
		SBMLDocument sbml = gcm.flattenGCM();		
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
			if (!species.isSetAnnotation() || 
					!species.getAnnotationString().contains(GlobalConstants.TYPE+"="+GlobalConstants.PROMOTER))
				parseSpeciesData(sbml,species);
			
		}
		for (long i=0; i<sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (species.isSetAnnotation() && 
					species.getAnnotationString().contains(GlobalConstants.TYPE+"="+GlobalConstants.PROMOTER)) 
				parsePromoterData(sbml,species);
		}
		
		GeneticNetwork network = new GeneticNetwork(speciesList, complexMap, partsMap, promoterList, gcm);
		
		network.setSBMLFile(gcm.getSBMLFile());
		if (sbml != null) {
			network.setSBML(sbml);
		}
		
		return network;		
	}

	public void printFile() {
		System.out.println(data.toString());
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
//		String annotation = promoter.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
//		String [] annotations = annotation.split(",");
//		for (int i=0;i<annotations.length;i++) {
//			if (annotations[i].startsWith(GlobalConstants.SBOL_PROMOTER)) {
//				String [] type = annotations[i].split("=");
//				p.setSbolPromoter(type[1]);
//			} else if (annotations[i].startsWith(GlobalConstants.SBOL_TERMINATOR)) {
//				String [] type = annotations[i].split("=");
//				p.setTerminator(type[1]);
//			}  
//		}
		String component = "";
		if (promoter.getId().contains("__")) {
			component = promoter.getId().substring(0,promoter.getId().lastIndexOf("__")+2);
		}
		Reaction production = sbml.getModel().getReaction(component+"Production_"+promoter.getId());
		if (production != null) {
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
				if (!sbml.getModel().getSpecies(product.getSpecies())
						.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.MRNA)) 
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
						if (sbml.getModel().getSpecies(product.getSpecies())
								.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.MRNA)) {
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
						if (sbml.getModel().getSpecies(product.getSpecies())
								.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.MRNA)) {
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
		if (species.getId().contains("__")) {
			component = species.getId().substring(0,species.getId().lastIndexOf("__")+2);
		}
		Reaction degradation = sbml.getModel().getReaction(component+"Degradation_"+species.getId());
		Reaction diffusion = sbml.getModel().getReaction(component+"MembraneDiffusion_"+species.getId());
		Reaction constitutive = sbml.getModel().getReaction(component+"Constitutive_"+species.getId());
		Reaction complex = sbml.getModel().getReaction(component+"Complex_"+species.getId());
		
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
		if (constitutive != null) {
			sbml.getModel().removeReaction(constitutive.getId());
		}
		
		String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
		String [] annotations = annotation.split(",");
		for (int i=0;i<annotations.length;i++) 
			if (annotations[i].startsWith(GlobalConstants.TYPE)) {
				String [] type = annotations[i].split("=");
				speciesIF.setType(type[1]);
			}
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
		
		if (degradation != null) {
			if (degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING)!=null) {
				speciesIF.setDecay(degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING).getValue());
			} else {
				speciesIF.setDecay(sbml.getModel().getParameter(GlobalConstants.KDECAY_STRING).getValue());
			}
			
			if (degradation.getAnnotationString().contains("Grid") == false)
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
	
	public void parsePromoterSbol(Model sbmlModel, Species sbmlPromoter) {
		// Create synthesis node corresponding to sbml promoter 
		String sbolUri = "";
		String [] annotations = sbmlPromoter.getAnnotationString().replace("<annotation>","").replace("</annotation>","").split(",");
		for (int i=0; i < annotations.length; i++) 
			if (annotations[i].startsWith(GlobalConstants.SBOL_DNA_COMPONENT)) 
				sbolUri = annotations[i].split("=")[1];
		SynthesisNode synNode;
		if (!sbolUri.equals(""))
			synNode = new SynthesisNode(sbmlPromoter.getId(), sbolUri);
		else
			synNode = new SynthesisNode(sbmlPromoter.getId());
		synMap.put(sbmlPromoter.getId(), synNode);
		// Determine if promoter belongs to a component
		String component = "";
		if (sbmlPromoter.getId().contains("__")) {
			component = sbmlPromoter.getId().substring(0, sbmlPromoter.getId().lastIndexOf("__")+2);
		}
		// Connect synthesis node for promoter to synthesis nodes for its products
		// Remove promoter production reaction from sbml
		Reaction production = sbmlModel.getModel().getReaction(component + "Production_" + sbmlPromoter.getId());
		if (production != null) {
			for (long i = 0; i < production.getNumProducts(); i++) {
				synNode.addNextNode(synMap.get(production.getProduct(i).getSpecies()));
			}
			sbmlModel.getModel().removeReaction(production.getId());
		}
	}
	
	public void parseReactionSbol(Reaction sbmlReaction) {
		// Create synthesis node corresponding to sbml reaction
		String sbolUri = "";
		String [] annotations = sbmlReaction.getAnnotationString().replace("<annotation>","").replace("</annotation>","").split(",");
		for (int i = 0; i < annotations.length; i++) 
			if (annotations[i].startsWith(GlobalConstants.SBOL_DNA_COMPONENT)) 
				sbolUri = annotations[i].split("=")[1];
		SynthesisNode synNode;
		if (!sbolUri.equals(""))
			synNode = new SynthesisNode(sbmlReaction.getId(), sbolUri);
		else
			synNode = new SynthesisNode(sbmlReaction.getId());
		synMap.put(sbmlReaction.getId(), synNode);
		// Connect synthesis node for reaction to synthesis nodes for its products
		for (long i = 0; i < sbmlReaction.getNumReactants(); i++)
			synMap.get(sbmlReaction.getReactant(i).getSpecies()).addNextNode(synNode);
		// Connect synthesis nodes for reactants, modifiers to synthesis node for reaction
		for (long i = 0; i < sbmlReaction.getNumModifiers(); i++)
			synMap.get(sbmlReaction.getModifier(i).getSpecies()).addNextNode(synNode);
		for (long i = 0; i < sbmlReaction.getNumProducts(); i++) 
			synNode.addNextNode(synMap.get(sbmlReaction.getProduct(i).getSpecies()));
		// Map parameters to reactions in which they appear for use in connecting synthesis nodes
		KineticLaw kl = sbmlReaction.getKineticLaw();
		for (long i = 0; i < kl.getNumParameters(); i++) {
			if (!paramInputMap.containsKey(kl.getParameter(i).getName()))
				paramInputMap.put(kl.getParameter(i).getName(), new HashSet<String>());
			paramInputMap.get(kl.getParameter(i).getName()).add(sbmlReaction.getId());
		}
	}
	
	public void parseRuleSbol(Rule sbmlRule) {
		// Create synthesis node corresponding to sbml rule
		String sbolUri = "";
		String [] annotations = sbmlRule.getAnnotationString().replace("<annotation>","").replace("</annotation>","").split(",");
		for (int i=0; i < annotations.length; i++) 
			if (annotations[i].startsWith(GlobalConstants.SBOL_DNA_COMPONENT)) 
				sbolUri = annotations[i].split("=")[1];
		SynthesisNode synNode;
		if (!sbolUri.equals(""))
			synNode = new SynthesisNode(sbmlRule.getId(), sbolUri);
		else
			synNode = new SynthesisNode(sbmlRule.getId());
		// Connect synthesis nodes for input species to synthesis node for rule
		// or maps input parameters to rules in which they're inputs
		for (String input : parseRuleHelper(sbmlRule.getMath())) {
			if (synMap.containsKey(input))
				synMap.get(input).addNextNode(synNode);
			else {
				if (!paramInputMap.containsKey(input))
					paramInputMap.put(input, new HashSet<String>());
				paramInputMap.get(input).add(sbmlRule.getId());
			}
		}
		// Connects synthesis node for rule to synthesis node for its output species
		// or maps output parameter to rule
		String output = sbmlRule.getVariable();
		if (output != null) {
			if (synMap.containsKey(output))
				synNode.addNextNode(synMap.get(output));
			else {
				if (!paramOutputMap.containsKey(output))
					paramOutputMap.put(output, new HashSet<String>());
				paramOutputMap.get(output).add(sbmlRule.getId());
			}
		}
	}
	
	public LinkedList<String> parseRuleHelper(ASTNode astNode) {
		LinkedList<String> inputs = new LinkedList<String>();
		for (long i = 0; i < astNode.getNumChildren(); i++) {
			ASTNode childNode = astNode.getChild(i);
			if (!childNode.isOperator() && !childNode.isNumber())
				inputs.add(childNode.getName());
			inputs.addAll(parseRuleHelper(childNode));
		}
		return inputs;
	}
	
	public void parseSpeciesSbol(Model sbmlModel, Species sbmlSpecies) {
		// Create synthesis node corresponding to sbml species
		String sbolUri = "";
		String [] annotations = sbmlSpecies.getAnnotationString().replace("<annotation>","").replace("</annotation>","").split(",");
		for (int i=0; i < annotations.length; i++) 
			if (annotations[i].startsWith(GlobalConstants.SBOL_DNA_COMPONENT)) 
				sbolUri = annotations[i].split("=")[1];
		SynthesisNode synNode;
		if (!sbolUri.equals(""))
			synNode = new SynthesisNode(sbmlSpecies.getId(), sbolUri);
		else
			synNode = new SynthesisNode(sbmlSpecies.getId());
		synMap.put(sbmlSpecies.getId(), synNode);
		// Determine if species belongs to a gcm component
		String component = "";
		if (sbmlSpecies.getId().contains("__")) {
			component = sbmlSpecies.getId().substring(0, sbmlSpecies.getId().lastIndexOf("__") + 2);
		}
		// Remove species degradation reaction from sbml
		Reaction degradation = sbmlModel.getReaction(component + "Degradation_" + sbmlSpecies.getId());
		if (degradation != null)
			sbmlModel.removeReaction(degradation.getId());
		// Remove species diffusion reaction from sbml
		Reaction diffusion = sbmlModel.getReaction(component + "MembraneDiffusion_" + sbmlSpecies.getId());
		if (diffusion != null)
			sbmlModel.removeReaction(diffusion.getId());
		// Remove species constitutive production reaction from sbml
		Reaction constitutive = sbmlModel.getReaction(component + "Constitutive_" + sbmlSpecies.getId());
		if (constitutive != null)
			sbmlModel.removeReaction(constitutive.getId());
		// Build complex map for use in connecting synthesis nodes
		// Remove species complex formation reaction from sbml
		Reaction complexFormation = sbmlModel.getReaction(component + "Complex_" + sbmlSpecies.getId());
		if (complexFormation != null) {
			for (long i = 0; i < complexFormation.getNumReactants(); i++) {
				Influence infl = new Influence();		
				String input = complexFormation.getReactant(i).getSpecies();
				String output = complexFormation.getProduct(0).getSpecies();
				infl.setInput(input);
				//Maps complex species to complex formation influences of which they're outputs
				ArrayList<Influence> complexInfluences = null;
				if (complexMap.containsKey(output)) {
					complexInfluences = complexMap.get(output);
				} else { 
					complexInfluences = new ArrayList<Influence>();
					complexMap.put(output, complexInfluences);
				}
				complexInfluences.add(infl);
			} 
			sbmlModel.getModel().removeReaction(complexFormation.getId());
		}
	}
	
	public void connectMappedSynthesisNodes() {
		// Connect synthesis nodes for species that form complexes
		for (String complexId : complexMap.keySet())
			for (Influence infl : complexMap.get(complexId))
				synMap.get(infl.getInput()).addNextNode(synMap.get(complexId));
		// Connect synthesis nodes for rules to synthesis nodes for rules or reactions on the basis of shared parameters
		for (String param : paramInputMap.keySet())
			if (paramOutputMap.containsKey(param)) 
				for (String origin : paramOutputMap.get(param))
					for (String destination : paramInputMap.get(param))
						synMap.get(origin).addNextNode(synMap.get(destination));
	}
	
	public SbolSynthesizer buildSbolSynthesizer() {
		SBMLDocument sbml = gcm.flattenGCM();		
		if (sbml == null) return null;
		return buildTopLevelSbolSynthesizer(sbml);
	}
	
	public SbolSynthesizer buildTopLevelSbolSynthesizer(SBMLDocument sbml) {
		
		synMap = new HashMap<String, SynthesisNode>(); // initialize map of model element IDs to synthesis nodes
		complexMap = new HashMap<String, ArrayList<Influence>>();
		paramInputMap = new HashMap<String, Set<String>>();  // initialize map of parameters to reactions in which they appear
		paramOutputMap = new HashMap<String, Set<String>>(); // initialize map of parameters to rules for which they're outputs
		Model sbmlModel = sbml.getModel();

		// Create and connect synthesis nodes for species and promoters
		// Note that all non-promoter species must be parsed first since parsePromoterSbol may refer to them
		// when connecting synthesis nodes
		for (long i = 0 ; i < sbmlModel.getNumSpecies(); i++) {
			Species sbmlSpecies = sbmlModel.getSpecies(i);
			if (!sbmlSpecies.isSetAnnotation() || 
					!sbmlSpecies.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER)) 
				parseSpeciesSbol(sbmlModel, sbmlSpecies);
		}
		for (long i = 0 ; i < sbmlModel.getNumSpecies(); i++) {
			Species sbmlSpecies = sbmlModel.getSpecies(i);
			if (sbmlSpecies.isSetAnnotation() && 
					sbmlSpecies.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER)) 
				parsePromoterSbol(sbmlModel, sbmlSpecies);
		}
		// Create and connect synthesis nodes for reactions not auto-generated by iBioSim
		for (long i = 0 ; i < sbmlModel.getNumReactions(); i++) {
			parseReactionSbol(sbmlModel.getReaction(i));
		}
		for (long i = 0; i < sbmlModel.getNumRules(); i++) {
			parseRuleSbol(sbmlModel.getRule(i));
		}
		// Finishes connecting synthesis nodes in accordance with various maps (see above)
		connectMappedSynthesisNodes();
		SbolSynthesizer synthesizer = new SbolSynthesizer(synMap.values());
		return synthesizer;
	}

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
	private HashMap<String, SynthesisNode> synMap;
	private HashMap<String, Set<String>> paramInputMap;
	private HashMap<String, Set<String>> paramOutputMap;

	private BioModel gcm = null;

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
