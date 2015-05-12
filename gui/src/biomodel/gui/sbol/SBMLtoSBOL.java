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
import org.sbolstandard.core2.ComponentInstance.AccessType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.FunctionalComponent.DirectionType;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.MapsTo.RefinementType;
//import org.sbolstandard.core2.RefinementType; //OLD VERSION
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import uk.ac.ncl.intbio.core.io.CoreIoException;

import com.hp.hpl.jena.graph.query.Mapping;
import com.lowagie.text.pdf.codec.Base64.OutputStream;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;

public class SBMLtoSBOL {
	BioModel bioModel;
	String path;
	
	String VERSION 		 = "1.0";
	
	//------------URI CONSTANTS
	URI COLLECTION_ID ;
	URI LANGUAGE   	  = org.sbolstandard.core2.Model.SBML;
	URI FRAMEWORK  	  = SystemsBiologyOntology.DISCRETE_FRAMEWORK;
	
	public SBMLtoSBOL(String path,BioModel bioModel) 
	{
		this.path = path;
		this.bioModel = bioModel;
	}
	
	public void export() {

		// OR read existing 1.1 document in the project to get sequences etc.
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		SBOLDocument sbolDoc = new SBOLDocument();
		sbolDoc.setDefaultURIprefix("http://www.async.ece.utah.edu");
		sbolDoc.setComplete(true);
		
		String collection_id = "collection__" + bioModel.getSBMLDocument().getModel().getId();
		Collection collection = sbolDoc.createCollection(collection_id, VERSION);
		export_recurse("file:" + bioModel.getSBMLFile(),sbmlDoc,sbolDoc,collection); 
	    
		try 
	    {
			SBOLWriter.writeRDF(sbolDoc, (System.out));
	    }
		catch (XMLStreamException e) { e.printStackTrace(); }
		catch (FactoryConfigurationError e) { e.printStackTrace(); } 
	    catch (CoreIoException e) { e.printStackTrace(); }
	}
	
	// TODO: break into sub functions
	public void export_recurse(String source,SBMLDocument sbmlDoc,SBOLDocument sbolDoc,Collection collection) 
	{
		Model model    = sbmlDoc.getModel();
		String modelId = model.getId();
		URI sourceURI  = URI.create(source);
		
		org.sbolstandard.core2.Model sbolModel = sbolDoc.createModel(model.getId(), VERSION, sourceURI, LANGUAGE, FRAMEWORK);
		
//		Set<URI> roles = new HashSet<URI>();
//		roles.add(URI.create(SOME_ONTOLOGY + "ROLE"));
//		org.sbolstandard.core2.Model sbolModel = sbolDoc.createModel(model.getId(), "1.0", sourceURI, LANGUAGE, FRAMEWORK);
		
		
		collection.addMember(sbolModel.getIdentity());	
		
		String identityStr  = model.getId();
//		ModuleDefinition moduleDef = sbolDoc.createModuleDefinition(URI.create(identityStr));
		ModuleDefinition moduleDef = sbolDoc.createModuleDefinition(identityStr, VERSION);
		collection.addMember(moduleDef.getIdentity());
		
		int funcCompPrefix = 1;
		FunctionalComponent funcComp;
		
		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			// convert species to a component definition
			Species species = model.getSpecies(i);
			String compDef_identity =  model.getId() + "__" + species.getId();
			
			Set<URI> compDef_type = new HashSet<URI>();
			Set<URI> compDef_role = new HashSet<URI>();
			
			if (BioModel.isPromoterSpecies(species)) 
			{
				compDef_type.add(ComponentDefinition.DNA);
				compDef_role.add(SequenceOntology.PROMOTER);
			} 
			else 
			{
				compDef_type.add(ComponentDefinition.PROTEIN);
			}
			ComponentDefinition compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
			collection.addMember(compDef.getIdentity());
			
			AccessType access; 
			DirectionType direction;
			// create FunctionalComponents for these within the module
			String funcComp_identity =  species.getId();
			
			if (bioModel.isInput(species.getId())) 
			{
				access    = AccessType.PUBLIC;
				direction = DirectionType.INPUT;
			} 
			else if (bioModel.isOutput(species.getId())) 
			{
				access    = AccessType.PUBLIC;
				direction = DirectionType.OUTPUT;
			} 
			else 
			{
				access    = AccessType.PRIVATE;
				direction = DirectionType.NONE;
			}
			
			funcComp = moduleDef.createFunctionalComponent(funcComp_identity, access, compDef.getIdentity(), direction);
			//OLD VERSION
//				funcComp = moduleDef.createComponent(URI.create(funcComp_identity), 
//					access, URI.create(compDef_identity), direction);
		
		}
		
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			
			// convert reaction to an interaction
			String promoterId = ""; 
			
			// if production reaction, then you want to examine the modifiers, and create interactions for 
			// each modifier that is a repressor from this species to the promoter
			if(bioModel.isProductionReaction(reaction))
			{
				List<ModifierSpeciesReference> repressors = new ArrayList<ModifierSpeciesReference>();
				List<ModifierSpeciesReference> activators = new ArrayList<ModifierSpeciesReference>(); 
				
				for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
	
					if (BioModel.isPromoter(modifier)) 
					{
						promoterId = modifier.getSpecies(); 
					} 
					else if (BioModel.isRepressor(modifier)) 
					{
						repressors.add(modifier);
					} 
					else if (BioModel.isActivator(modifier)) 
					{
						activators.add(modifier);
					} 
					else if (BioModel.isRegulator(modifier)) 
					{
						repressors.add(modifier);
						activators.add(modifier);
					}
				}
				
				for(ModifierSpeciesReference r : repressors)
				{
					String inter_id = r.getSpecies() + "_rep_" + promoterId;
					
					Set<URI> types = new HashSet<URI>();
					types.add(SystemsBiologyOntology.GENETIC_SUPPRESSION);
					
					Interaction interaction = moduleDef.createInteraction(inter_id, types);
					
					Participation p1 = interaction.createParticipation(promoterId, promoterId);
					Participation p2 = interaction.createParticipation(r.getSpecies(), r.getSpecies());
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(SystemsBiologyOntology.PROMOTER);
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(SystemsBiologyOntology.INHIBITOR);
					
					p1.setRoles(roles1);
					p2.setRoles(roles2);
				}
				
				// Repeat same steps for the list of activators
				for(ModifierSpeciesReference a : activators)
				{
					String inter_id ="_act_" + a.getSpecies();
					
					Set<URI> types = new HashSet<URI>();
					types.add(SystemsBiologyOntology.GENETIC_ENHANCEMENT); 
					
					Interaction interaction = moduleDef.createInteraction(inter_id, types);
					
					Participation p1 = interaction.createParticipation(promoterId, promoterId);
					Participation p2 = interaction.createParticipation(a.getSpecies(), a.getSpecies());
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(SystemsBiologyOntology.PROMOTER);
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(SystemsBiologyOntology.STIMULATOR);
					
					p1.setRoles(roles1);
					p2.setRoles(roles2);
				}
				
				int prod_partPrefix = 1;
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					String i_id = promoterId + "_prod_" + product.getSpecies();
					
					Set<URI> type = new HashSet<URI>();
					type.add(SystemsBiologyOntology.GENETIC_PRODUCTION);
					
					Interaction interaction = moduleDef.createInteraction(i_id, type);
					Participation p1 = interaction.createParticipation(promoterId, promoterId);
					Participation p2 = interaction.createParticipation(product.getSpecies(), product.getSpecies());
					
					Set<URI> roles1 = new HashSet<URI>();
					roles1.add(SystemsBiologyOntology.PROMOTER);
					
					Set<URI> roles2 = new HashSet<URI>();
					roles2.add(SystemsBiologyOntology.PRODUCT);
					
					p1.setRoles(roles1);
					p2.setRoles(roles2);
				}
			}
			// if complex reaction, then create on interaction
			else if(bioModel.isComplexReaction(reaction))
			{	
				Set<URI> type = new HashSet<URI>();
				type.add(SystemsBiologyOntology.NON_COVALENT_BINDING);
				
				Interaction inter = moduleDef.createInteraction(reaction.getId(), type);
				
				int reac_partPrefix = 1;
				int prod_partPrefix = 1;
				
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					Participation p = inter.createParticipation(reactant.getSpecies(), reactant.getSpecies());
					
					Set<URI> roles_reac = new HashSet<URI>();
					roles_reac.add(SystemsBiologyOntology.REACTANT);
					
					p.setRoles(roles_reac);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					Participation p = inter.createParticipation(product.getSpecies(), product.getSpecies());
					
					Set<URI> roles_prod = new HashSet<URI>();
					roles_prod.add(SystemsBiologyOntology.PRODUCT);
					
					p.setRoles(roles_prod); 
				}
			}
			// if degradation reaction, then create an interaction
			else if(bioModel.isDegradationReaction(reaction))
			{
				Set<URI> types = new HashSet<URI>();
				types.add(SystemsBiologyOntology.DEGRADATION);
				
				Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
				
				for(SpeciesReference sp : reaction.getListOfReactants())
				{
					Set<URI> roles_sp = new HashSet<URI>();
					roles_sp.add(SystemsBiologyOntology.REACTANT);
					String p_id = sp.getSpecies();
					Participation p = inter.createParticipation(p_id, sp.getSpecies());
					p.setRoles(roles_sp);
				}
			}
			else 
			{
				Set<URI> types = new HashSet<URI>();
				types.add(SystemsBiologyOntology.BIOCHEMICAL_REACTION);
				
				Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
				
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.REACTANT);
					Participation p = inter.createParticipation(reactant.getSpecies(), reactant.getSpecies());
					p.setRoles(roles_r);
				}
				for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.MODIFIER);
					Participation p = inter.createParticipation(modifier.getSpecies(), modifier.getSpecies());
					p.setRoles(roles_r);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					
					Set<URI> roles_p = new HashSet<URI>();
					roles_p.add(SystemsBiologyOntology.PRODUCT);
					Participation p = inter.createParticipation(product.getSpecies(), product.getSpecies());
					p.setRoles(roles_p);
				}
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
				if (!comps.contains(extModel)) 
				{
					comps.add(extModel);
					export_recurse(sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
							.getModelRef()).getSource(),subDocument,sbolDoc,collection);
				}
				Module m = moduleDef.createModule(subModelId, subDocument.getModel().getId(), "1.0");
				
				for (int j = 0; j < model.getSpeciesCount(); j++) 
				{
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(j));
					for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) 
					{
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId();
					
								RefinementType refinement = RefinementType.USELOCAL;
								m.createMapsTo(mapId, refinement, model.getSpecies(j).getId(), 
										replacement.getPortRef().replace("output__","").replace("input__",""));
							}
						}
					}
					if (sbmlSBase.isSetReplacedBy()) 
					{
						ReplacedBy replacement = sbmlSBase.getReplacedBy();
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId(); 

								RefinementType refinement = RefinementType.USEREMOTE;
								m.createMapsTo(mapId, refinement, model.getSpecies(j).getId(), 
										replacement.getPortRef().replace("output__","").replace("input__",""));
							} 
						}
					}
				}
			}
		}
	}
}
