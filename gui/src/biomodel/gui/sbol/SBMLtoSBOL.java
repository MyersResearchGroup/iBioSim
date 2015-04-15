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
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbolstandard.core2.Collection;
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
	
	String COMBINE 		 = "http://co.mbine.org/standards/sbml/level-3/version-1/core/release-1"; 
	String PREFIX 		 = "http://www.async.ece.utah.edu/"; 
	String SBOL 		 = "http://sbols.org/";
	String SOME_ONTOLOGY = "http://some.ontology.org/"; 
	String VERSION 		 = "/1.0";
	
	//TODO: make sure all URIs match with parent
	//TODO: make sure all ontology terms are defined as constants which for now start with http://some.ontology.org/
	//TODO: make sure all resource references point to complete URI
	//TODO: add every top level as a member of this collection
	
	public SBMLtoSBOL(String path,BioModel bioModel) 
	{
		this.path = path;
		this.bioModel = bioModel;
	}
	
	public void export() {

		// OR read existing 1.1 document in the project to get sequences etc.
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		SBOLDocument sbolDoc = new SBOLDocument();
		URI collection_id = URI.create(PREFIX + "collection__" + bioModel.getSBMLDocument().getModel().getId() + VERSION);
		Collection collection = sbolDoc.createCollection(collection_id);
		export_recurse("file:"+bioModel.getSBMLFile(),sbmlDoc,sbolDoc,collection); 
	    try 
	    {
			SBOLWriter.writeRdf(sbolDoc, (System.out));
	    }
		catch (XMLStreamException e) { e.printStackTrace(); }
		catch (FactoryConfigurationError e) { e.printStackTrace(); } 
	    catch (CoreIoException e) { e.printStackTrace(); }
	}
	
	// TODO: break into sub functions
	public void export_recurse(String source,SBMLDocument sbmlDoc,SBOLDocument sbolDoc,Collection collection) 
	{
		Model model 		= sbmlDoc.getModel();
		URI modelId    = URI.create(PREFIX + "model__"+model.getId() + VERSION);
		URI sourceURI     = URI.create(source);
		URI language   = URI.create(COMBINE);
		URI framework  = URI.create(SOME_ONTOLOGY + "ODE");
		Set<URI> roles = new HashSet<URI>();
		roles.add(URI.create(SOME_ONTOLOGY + "ROLE"));
		org.sbolstandard.core2.Model sbolModel = sbolDoc.createModel(modelId, sourceURI, language, framework, roles);
		collection.addMember(sbolModel.getIdentity());	
		
		String identityStr  = PREFIX + model.getId() + VERSION;
		ModuleDefinition moduleDef = sbolDoc.createModuleDefinition(URI.create(identityStr), null);
		collection.addMember(moduleDef.getIdentity());
		
		int funcCompPrefix = 1;
		FunctionalComponent funcComp;
		
		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			// convert species to a component definition
			Species species = model.getSpecies(i);
			String compDef_identity =  PREFIX + species.getId() + VERSION;
			
			Set<URI> compDef_type = new HashSet<URI>();
			Set<URI> compDef_role = new HashSet<URI>();
			
			if (BioModel.isPromoterSpecies(species)) 
			{
				// type is DNA 
				// role is Promoter
				compDef_type.add(URI.create(SOME_ONTOLOGY + "DNA"));
				compDef_role.add(URI.create(SOME_ONTOLOGY + "Promoter"));
			} 
			else 
			{
				// type is Protein
				// role is Transcription Factor
				compDef_type.add(URI.create(SOME_ONTOLOGY + "Protein"));
				compDef_role.add(URI.create(SOME_ONTOLOGY + "TranscriptionFactor"));
			}
			
			ComponentDefinition compDef = sbolDoc.createComponentDefinition(URI.create(compDef_identity), compDef_type, compDef_role);
			collection.addMember(compDef.getIdentity());
			
			URI access = null; 
			URI direction = null; 
			// create FunctionalComponents for these within the module
			String funcComp_identity =  PREFIX + model.getId() + "/" + species.getId() + VERSION;
			
			if (bioModel.isInput(species.getId())) 
			{
				// access is public
				// direction is input
				access = URI.create(SBOL + "public");
				direction = URI.create(SBOL + "input");
			} 
			else if (bioModel.isOutput(species.getId())) 
			{
				// access is public
				// direction is output
				access = URI.create(SBOL + "public");
				direction = URI.create(SBOL + "output");
			} 
			else 
			{
				// access is private
				// direction is none
				access = URI.create(SBOL + "private");
				direction = URI.create(SBOL + "none");
			}
			
			if(access != null && direction != null)
			{
				funcComp = moduleDef.createComponent(URI.create(funcComp_identity), 
					access, URI.create(compDef_identity), direction);
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
						promoterId = modifier.getSpecies(); 
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
				
				for(ModifierSpeciesReference r : repressors)
				{
					
					List<Participation> participations = new ArrayList<Participation>();
					String part_id = PREFIX
							+ model.getId() + "/" 
							+  r.getSpecies() + "_rep_" + promoterId +"/" + promoterId + VERSION;
					String part2_id = PREFIX 
							+ model.getId() + "/" 
							+ r.getSpecies() + "_rep_" + promoterId + "/" + r.getSpecies() + VERSION;
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create(SOME_ONTOLOGY + "repressed"));
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create(SOME_ONTOLOGY + "repressor"));
					
					Participation p1 = new Participation(URI.create(part_id), roles1, 
							URI.create(PREFIX + promoterId + VERSION));
					Participation p2 = new Participation(URI.create(part2_id), roles2, 
							URI.create(PREFIX + r.getSpecies() + VERSION));
					
					Set<URI> types = new HashSet<URI>();
					types.add(URI.create(SOME_ONTOLOGY + "repression"));
					
					participations.add(p1);
					participations.add(p2);
					
					String inter_id = PREFIX + model.getId() + "/" + r.getSpecies() + "_rep_" + promoterId + VERSION;
					Interaction interaction = moduleDef.createInteraction(URI.create(inter_id), types, participations);
				}
				
				// Repeat same steps for the list of activators
				for(ModifierSpeciesReference a : activators)
				{
					List<Participation> participations = new ArrayList<Participation>();
					String part_id = PREFIX 
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + a.getSpecies() + "/" + promoterId + VERSION;
					String part2_id = PREFIX
							+ model.getId() + "/" 
							+ promoterId + "_rep_" + a.getSpecies() + "/" + a.getSpecies() + VERSION;
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create(SOME_ONTOLOGY + "activated")); 
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create(SOME_ONTOLOGY + "activator")); 
					
					Participation p1 = new Participation(URI.create(part_id), roles1, 
							URI.create(PREFIX + promoterId + VERSION));
					Participation p2 = new Participation(URI.create(part2_id), roles2, 
							URI.create(PREFIX + a.getSpecies() + VERSION));
					
					Set<URI> types = new HashSet<URI>();
					types.add(URI.create(SOME_ONTOLOGY + "activation")); 
					
					participations.add(p1);
					participations.add(p2);
					
					String inter_id = PREFIX + model.getId() + "/" + "_act_" + a.getSpecies() + VERSION;
					Interaction interaction = moduleDef.createInteraction(URI.create(inter_id), types, participations);
				}
				
				int prod_partPrefix = 1;
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// add an interaction with participation from promoterId as type promoter
					// and participation from product.getSpecies() as type product
					String i_id = PREFIX + model.getId() + "/" + promoterId + "_prod_" + product.getSpecies() + VERSION;
					String p_id;
					
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(URI.create(SOME_ONTOLOGY + "promoter"));
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(URI.create(SOME_ONTOLOGY + "product"));
					
					p_id = PREFIX + model.getId() + "/" + promoterId + "_prod_" + product.getSpecies() + "/" 
							+ promoterId + VERSION;
					Participation p1 = new Participation(URI.create(p_id), roles1, 
							URI.create(PREFIX + promoterId + VERSION));
					
					p_id = PREFIX + model.getId() + "/" + promoterId + "_prod_" + product.getSpecies() + "/" 
							+ product.getSpecies() + VERSION;
					Participation p2 = new Participation(URI.create(p_id), roles2, 
							URI.create(PREFIX + product.getSpecies() + VERSION));
					
					Set<URI> type = new HashSet<URI>();
					type.add(URI.create(SOME_ONTOLOGY + "production"));
					
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
					roles_reac.add(URI.create(SOME_ONTOLOGY + "reactant"));
					
					String p_id = PREFIX+ model.getId() + "/" + reaction.getId() + "/" 
							+ reactant.getSpecies() + VERSION;
					Participation p = new Participation(URI.create(p_id), roles_reac, 
							URI.create(PREFIX + reactant.getSpecies() + VERSION));
					participations.add(p);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					
					Set<URI> roles_prod = new HashSet<URI>();
					roles_prod.add(URI.create(SOME_ONTOLOGY + "product"));
					
					String p_id = PREFIX + model.getId() + "/" + reaction.getId() + "/" 
							+ product.getSpecies() + VERSION;
					
					Participation p = new Participation(URI.create(p_id), roles_prod, 
							URI.create(PREFIX + product.getSpecies() + VERSION));
					participations.add(p);
				}
				// prefix + "/" + model.getId() + "/" + reaction.getId() + "/1.0"
				// type is ComplexFormation
				Set<URI> type = new HashSet<URI>();
				type.add(URI.create(SOME_ONTOLOGY + "ComplexFormation"));
				
				String i_id = PREFIX + model.getId() + "/" + reaction.getId() + VERSION; 
				
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
					roles_sp.add(URI.create(SOME_ONTOLOGY + "reactant"));
					
					String p_id = PREFIX + model.getId() + "/" + reaction.getId() + "/" 
							+ sp.getSpecies() + VERSION;
					
					// prefix + / + modelId + / species
					Participation p = new Participation(URI.create(p_id), roles_sp, 
							URI.create(PREFIX + sp.getSpecies() + VERSION));
					participations.add(p);
				}
				
				Set<URI> types = new HashSet<URI>();
				types.add(URI.create(SOME_ONTOLOGY + "Degradation"));
				
				String i_id = PREFIX + model.getId() + "/" + reaction.getId() + VERSION; 
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
					roles_r.add(URI.create(SOME_ONTOLOGY + "reactant"));
					
					String p_id = PREFIX + model.getId() + "/" + reaction.getId() + "/" + reactant.getSpecies() 
						 + VERSION;
					Participation p = new Participation(URI.create(p_id), roles_r, 
							URI.create(PREFIX + reactant.getSpecies() + VERSION));
					participations.add(p);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					
					Set<URI> roles_p = new HashSet<URI>();
					roles_p.add(URI.create(SOME_ONTOLOGY + "product"));
					
					String p_id = PREFIX + model.getId() + "/" + reaction.getId() + "/" + product.getSpecies() + VERSION;
					
					Participation p = new Participation(URI.create(p_id), roles_p, 
							URI.create(PREFIX + product.getSpecies() + VERSION));
					participations.add(p);
				}
				
				Set<URI> types = new HashSet<URI>();
				types.add(URI.create(SOME_ONTOLOGY + "Reaction"));
				
				String i_id = PREFIX + model.getId() + "/" + reaction.getId() + VERSION; 
				moduleDef.createInteraction(URI.create(i_id), types, participations);
			}
		}
		// TODO: Extract SBOL annotations to fill in more info about ComponentDefn.
		
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
				for (int j = 0; j < model.getSpeciesCount(); j++) 
				{
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(j));
					for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
						if (replacement.getSubmodelRef().equals(subModelId)) {
							if (replacement.isSetPortRef()) {
								// create mapping
								// if replacement - refinement is useLocal
								// local = URI for this species' functional component
								// remote = URI prefix + subDocument.getModel().getId() + replacement.getPortRef().replace("output__","").replace("input__","") + version
							}
						}
					}
					if (sbmlSBase.isSetReplacedBy()) {
						ReplacedBy replacement = sbmlSBase.getReplacedBy();
						if (replacement.getSubmodelRef().equals(subModelId)) {
							if (replacement.isSetPortRef()) {
								// create mapping
								// if replacedBy - refinement is useRemote
								// local = URI for this species' functional component
								// remote = URI prefix + subDocument.getModel().getId() + replacement.getPortRef().replace("output__","").replace("input__","") + version
							} 
						}
					}
				}
				if (!comps.contains(extModel)) {
					comps.add(extModel);
					export_recurse(sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
							.getModelRef()).getSource(),subDocument,sbolDoc,collection);
				}
			}
		}
	}

}
