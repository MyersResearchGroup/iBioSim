package biomodel.gui.sbol;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;

import uk.ac.ncl.intbio.core.io.CoreIoException;

import com.lowagie.text.pdf.codec.Base64.OutputStream;

import biomodel.parser.BioModel;
import biomodel.util.SBMLutilities;

public class SBMLtoSBOL {
	BioModel bioModel;
	String path;
	
	//TODO: create a model in sbol that will point back to the model that is being created in sbml
	//TODO: all id set for each object may or may not be right...
	//TODO: make sure all URIs match with parent
	//TODO: make sure all ontology terms are defined as constant which for now start with http://some.ontology.org/
	//TODO: make sure all resource references point to complete URI
	
	public SBMLtoSBOL(String path,BioModel bioModel) 
	{
		this.path = path;
		this.bioModel = bioModel;
	}
	
	public void export() {
		SBOLDocument sbolDoc = new SBOLDocument();
		// OR read existing 1.1 document in the project to get sequences etc.
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		export_recurse(sbmlDoc,sbolDoc); 
	    try 
	    {
			SBOLWriter.writeRdf(sbolDoc, (System.out));
	    }
		catch (XMLStreamException e) { e.printStackTrace(); }
		catch (FactoryConfigurationError e) { e.printStackTrace(); } 
	    catch (CoreIoException e) { e.printStackTrace(); }
	}
	
	// TODO: break into sub functions
	public void export_recurse(SBMLDocument sbmlDoc,SBOLDocument sbolDoc) 
	{
		
		// TODO: create collection with id "collection__"+model.getId()
		// TODO: add every top level as a member of this collection
		
		String prefix = "http://www.async.ece.utah/edu/";
		
		URI modelId = URI.create(prefix + "modelId");
		URI source = URI.create("http://some.ontology.org/source");
		URI language = URI.create("http://co.mbine.org/standards/sbml/level-3/version-1/core/release-1");
		URI framework = URI.create("http://some.ontology.org/ODE");
		Set<URI> roles = new HashSet<URI>();
		roles.add(URI.create("http://some.ontology.org/ROLE"));
		sbolDoc.createModel(modelId, source, language, framework, roles);
		
		Model model = sbmlDoc.getModel();
		String identityStr = prefix + model.getId() + "/1.0";
		ModuleDefinition moduleDef = sbolDoc.createModuleDefinition(URI.create(identityStr), null);
		
		int funcCompPrefix = 1;
		
		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			// convert species to a component definition
			Species species = model.getSpecies(i);
			String compDef_identity =  prefix + species.getId() + "/1.0";
			
			Set<URI> compDef_type = new HashSet<URI>();
			Set<URI> compDef_role = new HashSet<URI>();
			
			if (BioModel.isPromoterSpecies(species)) 
			{
				// type is DNA 
				// role is Promoter
				compDef_type.add(URI.create("DNA"));
				compDef_role.add(URI.create("Promoter"));
			} 
			else 
			{
				// type is Protein
				// role is Transcription Factor
				compDef_type.add(URI.create("Protein"));
				compDef_role.add(URI.create("TranscriptionFactor"));
			}
			
			ComponentDefinition compDef = sbolDoc.createComponentDefinition(URI.create(compDef_identity), compDef_type, compDef_role);
			
			URI access = null; 
			URI direction = null; 
			// create FunctionalComponents for these within the module
			String funcComp_identity =  prefix + model.getId() + "/" + species.getId() + "/" + "funcComp" + funcCompPrefix++ +"/1.0";
			
			if (bioModel.isInput(species.getId())) 
			{
				// access is public
				// direction is input
				access = URI.create("public");
				direction = URI.create("input");
			} 
			else if (bioModel.isOutput(species.getId())) 
			{
				// access is public
				// direction is output
				access = URI.create("public");
				direction = URI.create("output");
			} 
			else 
			{
				// access is private
				// direction is none
				access = URI.create("private");
				direction = URI.create("none");
			}
			
			if(access != null && direction != null)
			{
				FunctionalComponent funcComp = moduleDef.createComponent(URI.create(funcComp_identity), 
					access, URI.create(compDef_identity), direction); //TODO: the compDef_identity might be wrong
			}
		
		}
		
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			
			// convert reaction to an interaction
			String promoterId = ""; 
			
			// if production reaction, then you want to examine the modifiers, and create interactions for 
			// each modifier that is a repressor from this species to the promoter
			if(bioModel.isProductionReaction(reaction))
			{
				// Create empty lists of repressors, activators
				List<ModifierSpeciesReference> repressors = new ArrayList<ModifierSpeciesReference>();
				List<ModifierSpeciesReference> activators = new ArrayList<ModifierSpeciesReference>(); 
				
				for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
	
					if (BioModel.isPromoter(modifier)) 
					{
						// Remember which species is the promoterId
						promoterId = modifier.getSpecies(); //TODO: will there be only one promoterId or a list of them?
					} 
					else if (BioModel.isRepressor(modifier)) 
					{
						// add to list of repressors
						repressors.add(modifier);
					} 
					else if (BioModel.isActivator(modifier)) 
					{
						// add to list of activators
						activators.add(modifier);
					} 
					else if (BioModel.isRegulator(modifier)) 
					{
						// add to list of repressors
						// add to list of activators
						activators.add(modifier);
						activators.add(modifier);
					}
				}
				
				// create an interaction for each repressor to the promoter
				// prefix + "/" + model.getId() + "/" + promoterId + "_rep_" + repressorId + "/1.0"
				// type = repression
				for(ModifierSpeciesReference r : repressors)
				{
					// create a participation for the promoter of type "repressed"
					// prefix + "/" + model.getId() + "/" + promoterId + "_rep_" + repressorId + "/" + promoterId "/1.0"
					// create a participation for the repressor of type "repressor"
					// prefix + "/" + model.getId() + "/" + promoterId + "_rep_" + repressorId + "/" + repressorId "/1.0"
					
					List<Participation> participations = new ArrayList<Participation>();
					String part_id = prefix + "/" 
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + r.getSpecies() + "/" + promoterId + "/1.0";
					String part2_id = prefix + "/" 
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + r.getSpecies() + "/" + r.getSpecies() + "/1.0";
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create("repressed"));
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create("repressor"));
					
					Participation p1 = new Participation(URI.create(part_id), roles1, URI.create(promoterId));
					Participation p2 = new Participation(URI.create(part2_id), roles2, URI.create(r.getSpecies()));
					
					Set<URI> types = new HashSet<URI>();
					types.add(URI.create("repression"));
					
					participations.add(p1);
					participations.add(p2);
					
					String inter_id = prefix + "/" + model.getId() + "/" + "_rep_" + r.getId() + "/1.0";
					Interaction interaction = moduleDef.createInteraction(URI.create(inter_id), types, participations);
				}
				
				// Repeat same steps for the list of activators
				for(ModifierSpeciesReference a : activators)
				{
					List<Participation> participations = new ArrayList<Participation>();
					String part_id = prefix + "/" 
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + a.getSpecies() + "/" + promoterId + "/1.0";
					String part2_id = prefix + "/" 
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + a.getSpecies() + "/" + a.getSpecies() + "/1.0";
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create("activated")); //TODO: ACTIVATED?
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create("activator")); //TODO: ACTIVATOR?
					
					Participation p1 = new Participation(URI.create(part_id), roles1, URI.create(promoterId));
					Participation p2 = new Participation(URI.create(part2_id), roles2, URI.create(a.getSpecies()));
					
					Set<URI> types = new HashSet<URI>();
					types.add(URI.create("activation")); //TODO: activation
					
					participations.add(p1);
					participations.add(p2);
					
					String inter_id = prefix + "/" + model.getId() + "/" + "_act_" + a.getId() + "/1.0";
					Interaction interaction = moduleDef.createInteraction(URI.create(inter_id), types, participations);
				}
				
				int prod_partPrefix = 1;
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// add an interaction with participation from promoterId as type promoter
					// and participation from product.getSpecies() as type product
					String i_id = prefix + "/" + model.getId() + "/" + promoterId + "_prod_" + product.getSpecies() + "/1.0";
					String p_id;
					
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create("promoter"));
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create("product"));
					
					p_id = prefix + "/" + model.getId() + "/" + "_prod_" + product.getId() + "/" 
							+ "p_id" + prod_partPrefix++ + "/1.0";
					Participation p1 = new Participation(URI.create(p_id), roles1, URI.create(promoterId));
					
					p_id = prefix + "/" + model.getId() + "/" + "_prod_" + product.getId() + "/" 
							+ "p_id" + prod_partPrefix++ + "/1.0";
					Participation p2 = new Participation(URI.create(p_id), roles2, URI.create(product.getSpecies()));
					
					Set<URI> type = new HashSet<URI>();
					type.add(URI.create("production"));
					
					List<Participation> participations = new ArrayList<Participation>();
					participations.add(p1);
					participations.add(p2);
					
					Interaction interaction = moduleDef.createInteraction(URI.create(i_id), type, participations);
				}
			}
			// if complex reaction, then create on interaction
			else if(bioModel.isComplexReaction(reaction))
			{	
				// create an empty list of participations
				List<Participation> participations = new ArrayList<Participation>();
				int reac_partPrefix = 1;
				int prod_partPrefix = 1;
				
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					// create participation from reactant.getSpecies() as type reactant
					
					Set<URI> roles_reac = new HashSet<URI>();
					roles_reac.add(URI.create("reactant"));
					
					String p_id = prefix + "/" + model.getId() + "/" + reaction.getId() + "/" 
							+ reactant.getSpecies() + "/1.0";
					Participation p = new Participation(URI.create(p_id), roles_reac, URI.create(reactant.getSpecies()));
					participations.add(p);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					
					Set<URI> roles_prod = new HashSet<URI>();
					roles_prod.add(URI.create("product"));
					
					String p_id = prefix + "/" + model.getId() + "/" + reaction.getId() + "/" 
							+ product.getSpecies() + "/1.0";
					
					Participation p = new Participation(URI.create(p_id), roles_prod, URI.create(product.getSpecies()));
					participations.add(p);
				}
				// prefix + "/" + model.getId() + "/" + reaction.getId() + "/1.0"
				// type is ComplexFormation
				Set<URI> type = new HashSet<URI>();
				type.add(URI.create("ComplexFormation"));
				
				String i_id = prefix + "/" + model.getId() + "/" + reaction.getId() + "/1.0";
				
				
				 moduleDef.createInteraction(URI.create(i_id), type, participations);
			}
			// if degradation reaction, then create an interaction
			else if(bioModel.isDegradationReaction(reaction))
			{
				// create a participation for the species in the reactant list, type reactant
				// prefix + "/" + model.getId() + "/" + reaction.getId() + "/1.0"
				// type is Degradation
				
				List<Participation> participations = new ArrayList<Participation>();
				int prod_partPrefix = 1;
				for(SpeciesReference sp : reaction.getListOfReactants())
				{
					Set<URI> roles_sp = new HashSet<URI>();
					roles_sp.add(URI.create("reactant"));
					
					String p_id = prefix + "/" + model.getId() + "/" + reaction.getId() + "/" 
							+ "p_id" + prod_partPrefix++ + "/1.0";
					
					// prefix + / + modelId + / species
					Participation p = new Participation(URI.create(p_id), roles_sp, URI.create(sp.getSpecies()));
					participations.add(p);
				}
				
				Set<URI> types = new HashSet<URI>();
				types.add(URI.create("Degradation"));
				
				String i_id = prefix + "/" + model.getId() + "/" + reaction.getId() + "/1.0";
				
				moduleDef.createInteraction(URI.create(i_id), types, participations);
			}
			else 
			{
			// skip diffusion, constitutive
			// if none of the above, create interaction
				// Same as complex formation, but change type to "Reaction", and loop through modifiers
				List<Participation> participations = new ArrayList<Participation>();
				int reac_partPrefix = 1;
				int prod_partPrefix = 1;
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					// create participation from reactant.getSpecies() as type reactant
					
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(URI.create("reactant"));
					
					String p_id = prefix + "/" + model.getId() + "/" + "_reac_" + reactant.getId() + "/" 
							+ "p_id" + reac_partPrefix++ + "/1.0";
					Participation p = new Participation(URI.create(p_id), roles_r, URI.create(reactant.getSpecies()));
					participations.add(p);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					
					Set<URI> roles_p = new HashSet<URI>();
					roles_p.add(URI.create("product"));
					
					String p_id = prefix + "/" + model.getId() + "/" + "_reac_" + product.getId() + "/" 
							+ "p_id" + prod_partPrefix++ + "/1.0";
					
					Participation p = new Participation(URI.create(p_id), roles_p, URI.create(product.getSpecies()));
					participations.add(p);
				}
				
				Set<URI> types = new HashSet<URI>();
				types.add(URI.create("Reaction"));
				
				String i_id = prefix + "/" + model.getId() + "/" + "_reac_" + reaction.getId() + "/1.0";
				
				moduleDef.createInteraction(URI.create(i_id), types, participations);
			}
		}
		// TODO: Extract SBOL annotations to fill in more info about ComponentDefn.
		// TODO: Walk the hierarchy to build module definitions out of others	
		ArrayList<String> comps = new ArrayList<String>();
		CompSBMLDocumentPlugin sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(sbmlDoc.getModel());
		if (sbmlCompModel.getListOfSubmodels().size()>0) {
			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				String subModelId = sbmlCompModel.getListOfSubmodels().get(i).getId();
				String extModel = sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
						.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
				SBMLDocument subDocument = SBMLutilities.readSBML(path + Gui.separator + extModel);
				// TODO: create a module, parent + "/" + subModelId + version
				// definition: prefix + / + subDocument.getModel().getId()
				// create mappings by looking at the replacements for the species
				if (!comps.contains(extModel)) {
					comps.add(extModel);
					export_recurse(subDocument,sbolDoc);
				}
			}
		}
	}
	
}
