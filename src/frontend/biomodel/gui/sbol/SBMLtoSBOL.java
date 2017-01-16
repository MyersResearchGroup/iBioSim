package frontend.biomodel.gui.sbol;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.sbolstandard.core2.TopLevel;

import com.clarkparsia.sbol.editor.Registries;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.versioning.PersonInfo;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.parser.BioModel;
import backend.biomodel.util.SBMLutilities;
import backend.sbol.util.SBOLUtility2;
import backend.util.GlobalConstants;
import frontend.main.Gui;

public class SBMLtoSBOL {
	BioModel bioModel;
	String path;
	SBOLDocument SBOLDOC;
	
	String VERSION = "";
	
	URI COLLECTION_ID ;
	URI LANGUAGE  = EDAMOntology.SBML;
	URI FRAMEWORK = SystemsBiologyOntology.DISCRETE_FRAMEWORK;
	
	public SBMLtoSBOL(Gui gui,String path,BioModel bioModel) 
	{
		this.path = path;
		this.bioModel = bioModel;
		SBOLDOC = new SBOLDocument();
		HashSet<String> sbolFilePaths = gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
		loadSBOLFiles(sbolFilePaths);
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFilePaths) 
	{
		for (String filePath : sbolFilePaths) 
		{
			SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
			if (sbolDoc != null) 
			{
				for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
				{
					if(SBOLDOC.getComponentDefinition(c.getIdentity()) == null) 
					{
						try {
							SBOLDOC.createCopy(c);
						}
						catch (SBOLValidationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				for(Sequence c : sbolDoc.getSequences())
				{
					if(SBOLDOC.getSequence(c.getIdentity()) == null) 
					{
						try {
							SBOLDOC.createCopy(c);
						}
						catch (SBOLValidationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			} 
			else
				return false;
		}
		return true;
	}
	
	public void saveAsSBOL(SBOLDocument sbolDoc) {
		sbolDoc.setTypesInURIs(true);
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		String collection_id = "collection__" + bioModel.getSBMLDocument().getModel().getId();
		Collection collection;
		try {
			collection = sbolDoc.getCollection(collection_id, VERSION);
			if (collection!=null) {
				sbolDoc.removeCollection(collection);
			}
			collection = sbolDoc.createCollection(collection_id, VERSION);
			export_recurse("file:" + bioModel.getSBMLFile(),sbmlDoc,sbolDoc,collection); 
		}
		catch (SBOLValidationException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Error saving SBOL file.", 
					"SBOL Save Error", JOptionPane.ERROR_MESSAGE);
		}
		sbolDoc.setTypesInURIs(false);
	}
	
	public void upload(String location) throws SBOLValidationException, StackException {
		SBOLDocument uploadDoc = new SBOLDocument();
		uploadDoc.setComplete(false);
		export(uploadDoc);
		StackFrontend stack = new StackFrontend(location);
		PersonInfo info = SBOLEditorPreferences.INSTANCE.getUserInfo();
		String email = info == null || info.getEmail() == null ? null : info.getEmail().getLocalName();
		String uri = info == null ? null : info.getURI().stringValue();
		if (email == null || email.equals("") || uri == null) {
			JOptionPane.showMessageDialog(Gui.frame, "Make sure your email and URI are both set and valid in preferences.",
					"Upload failed", JOptionPane.ERROR_MESSAGE);
		}
		String emailHash = Hashing.sha1().hashString(email, Charsets.UTF_8).toString();
		String userId = email.replace("@", "%40");
		// TODO: need to revise this when revised on stack
		String storename = "synbiohub_user_" + Hashing.sha1()
				.hashString("synbiohub_" + emailHash + "synbiohub_change_me", Charsets.UTF_8).toString();
		// TODO: uploadDoc should only include objects in your namespace.
		// filter for member collections works, but it ends up uploading extra
		// objects
		// TODO: would this ever be more than one root?
		for (ModuleDefinition cd : uploadDoc.getRootModuleDefinitions()) {
			// TODO: should ask the user for a submissionId and perhaps other
			// fields as done for synbiohub
			String submissionId = cd.getDisplayId();
			String submissionName = cd.isSetName() ? cd.getName() : cd.getDisplayId();
			String submissionDescription = cd.isSetDescription() ? cd.getDescription() : "";
			String submissionVersion = cd.isSetVersion() ? cd.getVersion() : "1";
			Collection collection = uploadDoc.createCollection(submissionId + "_collection", "1");
			collection.setName(submissionName + " " + "Collection");
			collection.setDescription(submissionDescription);
			collection.createAnnotation(new QName("http://synbiohub.org#", "uploadedBy", "synbiohub"), email);
			collection.createAnnotation(new QName("http://purl.org/dc/terms/", "creator", "dcterms"), info.getName());
			for (TopLevel topLevel : uploadDoc.getTopLevels()) {
				if (topLevel.getIdentity().equals(collection.getIdentity())) continue;
				if (!topLevel.getIdentity().toString().startsWith(uri))
					continue;
				collection.addMember(topLevel.getIdentity());
			}
			uploadDoc = uploadDoc.changeURIPrefixVersion("http://synbiohub.org/user/" + userId + "/" + submissionId + "/",
					submissionVersion);
			stack.upload(storename, uploadDoc);
		}

	}
	
	public void export(String exportFilePath,String fileType) {
		SBOLDocument sbolDoc = new SBOLDocument();
		//sbolDoc.setTypesInURIs(true);
		export(sbolDoc);
		//sbolDoc.setTypesInURIs(false);
		try 
		{
			if (fileType.equals("SBOL")) {	
				sbolDoc.write(exportFilePath,SBOLDocument.RDF);
			} else if (fileType.equals("SBOL1")) {	
				sbolDoc.write(exportFilePath,SBOLDocument.RDFV1);
			} else if (fileType.equals("GenBank")) {	
				sbolDoc.write(exportFilePath,SBOLDocument.GENBANK);
			} else if (fileType.equals("Fasta")) {	
				sbolDoc.write(exportFilePath,SBOLDocument.FASTAformat);
			} 
		} 
		catch (SBOLConversionException e)
		{
			JOptionPane.showMessageDialog(Gui.frame, "Error writing "+fileType+" file at " + exportFilePath + ".", 
					fileType+" Conversion Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Error writing "+fileType+" file at " + exportFilePath + ".", 
					fileType+" Write Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void export(SBOLDocument sbolDoc) {
		// TODO read existing 1.1 document in the project to get sequences etc.
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		Preferences biosimrc = Preferences.userRoot();
		sbolDoc.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
		sbolDoc.setComplete(false);
		//sbolDoc.setTypesInURIs(true);
		
		String collection_id = "collection__" + bioModel.getSBMLDocument().getModel().getId();
		Collection collection;
		try {
			collection = sbolDoc.createCollection(collection_id, VERSION);
			export_recurse("file:" + bioModel.getSBMLFile(),sbmlDoc,sbolDoc,collection); 
		}
		catch (SBOLValidationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Error export SBOL file.", 
					"SBOL Export Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void export_recurse(String source,SBMLDocument sbmlDoc,SBOLDocument sbolDoc,Collection collection) throws SBOLValidationException 
	{
		Model model    = sbmlDoc.getModel();
		//String modelId = model.getId();
		URI sourceURI  = URI.create(source);

		org.sbolstandard.core2.Model sbolModel = sbolDoc.getModel(model.getId()+"_model", VERSION);
		if (sbolModel!=null) {
			sbolDoc.removeModel(sbolModel);
		}
		sbolModel = sbolDoc.createModel(model.getId()+"_model", VERSION, sourceURI, LANGUAGE, FRAMEWORK);
		collection.addMember(sbolModel.getIdentity());	
		
		String identityStr  = model.getId();
		ModuleDefinition moduleDef = sbolDoc.getModuleDefinition(identityStr, VERSION);
		if (moduleDef!=null) {
			sbolDoc.removeModuleDefinition(moduleDef);
		}
		moduleDef = sbolDoc.createModuleDefinition(identityStr, VERSION);
		moduleDef.addModel(sbolModel);
		collection.addMember(moduleDef.getIdentity());

		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			// convert species to a component definition
			Species species = model.getSpecies(i);
			ComponentDefinition compDef = setComponentDefinition(sbolDoc, model, species, collection);
			collection.addMember(compDef.getIdentity());
			
			setFunctionalComponent(sbmlDoc, moduleDef, compDef, species);
		}
		
		for (int i = 0; i < model.getReactionCount(); i++) 
		{
			Reaction reaction = model.getReaction(i);

			// if production reaction, then you want to examine the modifiers, and create interactions for 
			// each modifier that is a repressor from this species to the promoter
			if(BioModel.isProductionReaction(reaction))
			{
				extractProductionReaction(moduleDef, reaction);
			}
			// if complex reaction, then create on interaction
			else if(BioModel.isComplexReaction(reaction))
			{	
				extractComplexReaction(moduleDef, reaction);
			}
			// if degradation reaction, then create an interaction
			else if(BioModel.isDegradationReaction(reaction))
			{
				extractDegradationReaction(moduleDef, reaction);
			}
			else 
			{
				Set<URI> types = new HashSet<URI>();
				types.add(SystemsBiologyOntology.BIOCHEMICAL_REACTION);
				
				Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
				
				int j = 0;
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.REACTANT);
					inter.createParticipation(reactant.getSpecies()+"_r"+j, reactant.getSpecies(),roles_r);
					j++;
				}
				j = 0;
				for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.MODIFIER);
					inter.createParticipation(modifier.getSpecies()+"_m"+j, modifier.getSpecies(),roles_r);
					j++;
				}
				j = 0;
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					Set<URI> roles_p = new HashSet<URI>();
					roles_p.add(SystemsBiologyOntology.PRODUCT);
					inter.createParticipation(product.getSpecies()+"_p"+j, product.getSpecies(),roles_p);
					j++;
				}
			}
		}
		extractSubModels(sbmlDoc, sbolDoc, collection, moduleDef, model);
	}
	
	public void recurseComponentDefinition(SBOLDocument sbolDoc,ComponentDefinition cd,Collection collection) throws SBOLValidationException {
		for (org.sbolstandard.core2.Component comp : cd.getComponents()) {
			if (sbolDoc.getComponentDefinition(comp.getDefinitionURI())==null) {
				ComponentDefinition compDef = comp.getDefinition();
				sbolDoc.createCopy(compDef);
				collection.addMember(compDef.getIdentity());
				for (Sequence sequence : compDef.getSequences()) {
					if (sbolDoc.getSequence(sequence.getIdentity())==null) {
						sbolDoc.createCopy(sequence);
						collection.addMember(sequence.getIdentity());
					}
				}
				recurseComponentDefinition(sbolDoc,compDef,collection);
			}
		}
	}
	
	public ComponentDefinition setComponentDefinition(SBOLDocument sbolDoc, Model model, Species species, 
			Collection collection) throws SBOLValidationException
	{
		String compDef_identity =  model.getId() + "__" + species.getId();
		
		Set<URI> compDef_type = new HashSet<URI>();
		Set<URI> compDef_role = new HashSet<URI>();
		ComponentDefinition compDef = null;
		
		if (BioModel.isPromoterSpecies(species)) 
		{
			List<URI> sbolURIs = new LinkedList<URI>();
			//String sbolStrand = 
			AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {
				// TODO: need to figure out what to do when size is greater than 1
				compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
				if (compDef!=null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
					}
					collection.addMember(compDef.getIdentity());
					for (Sequence sequence : compDef.getSequences()) {
						if (sbolDoc.getSequence(sequence.getIdentity())==null) {
							sbolDoc.createCopy(sequence);
							collection.addMember(sequence.getIdentity());
						}
					}
					recurseComponentDefinition(sbolDoc,compDef,collection);
					return compDef;
				}
			}
			Reaction production = BioModel.getProductionReaction(species.getId(),model);
			if (production!=null) {
				sbolURIs = new LinkedList<URI>();
				//sbolStrand = 
				AnnotationUtility.parseSBOLAnnotation(production, sbolURIs);
				if (sbolURIs.size()>0) {
					compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
					if (compDef!=null) {
						if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
							sbolDoc.createCopy(compDef);
						}
						collection.addMember(compDef.getIdentity());
						for (Sequence sequence : compDef.getSequences()) {
							if (sbolDoc.getSequence(sequence.getIdentity())==null) {
								sbolDoc.createCopy(sequence);
								collection.addMember(sequence.getIdentity());
							}
						}
						recurseComponentDefinition(sbolDoc,compDef,collection);
						return compDef;
					}
				}
			}
			compDef_type.add(ComponentDefinition.DNA);
			compDef_role.add(SequenceOntology.PROMOTER);
		} 
		// TODO: other cases for other SBO terms
		else 
		{
			List<URI> sbolURIs = new LinkedList<URI>();
			//String sbolStrand = 
			AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {
				// TODO: what if more than 1
				compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
				if (compDef!=null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
					}
					collection.addMember(compDef.getIdentity());
					for (Sequence sequence : compDef.getSequences()) {
						if (sbolDoc.getSequence(sequence.getIdentity())==null) {
							sbolDoc.createCopy(sequence);
							collection.addMember(sequence.getIdentity());
						}
					}
					recurseComponentDefinition(sbolDoc,compDef,collection);
					return compDef;
				}
			}
			if (species.isSetSBOTerm()) {
				if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA) ||
						species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
					compDef_type.add(ComponentDefinition.DNA);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_RNA) ||
						species.getSBOTermID().equals(GlobalConstants.SBO_RNA_SEGMENT)) {
					compDef_type.add(ComponentDefinition.RNA);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_PROTEIN)) {
					compDef_type.add(ComponentDefinition.PROTEIN);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_NONCOVALENT_COMPLEX) ||
						SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_NONCOVALENT_COMPLEX)) {
					compDef_type.add(ComponentDefinition.COMPLEX);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_SIMPLE_CHEMICAL) ||
						SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_SIMPLE_CHEMICAL)) {
					compDef_type.add(ComponentDefinition.SMALL_MOLECULE);
				} else {
					compDef_type.add(ComponentDefinition.PROTEIN);
				}
			} else {
				compDef_type.add(ComponentDefinition.PROTEIN);
			}
		}
		compDef = sbolDoc.getComponentDefinition(compDef_identity, VERSION);
		if (compDef==null) {
			compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
		} else if (!compDef.getTypes().containsAll(compDef_type)) {
			// TODO: if the type has changed, then replace it
			sbolDoc.removeComponentDefinition(compDef);
			compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
		}
		return compDef; 
	}
	
	public FunctionalComponent setFunctionalComponent(SBMLDocument sbmlDoc, ModuleDefinition moduleDef, ComponentDefinition compDef, Species species) throws SBOLValidationException
	{
		AccessType access; 
		DirectionType direction;
		// create FunctionalComponents for these within the module
		String funcComp_identity =  species.getId();
		
		if (SBMLutilities.isInput(sbmlDoc,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.IN;
		} 
		else if (SBMLutilities.isOutput(sbmlDoc,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.OUT;
		} 
		else 
		{
			access    = AccessType.PRIVATE; 
			direction = DirectionType.NONE;
		}
		
		return moduleDef.createFunctionalComponent(funcComp_identity, access, compDef.getIdentity(), direction);
	}
	
	public void extractProductionReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		List<ModifierSpeciesReference> repressors = new ArrayList<ModifierSpeciesReference>();
		List<ModifierSpeciesReference> activators = new ArrayList<ModifierSpeciesReference>(); 
		String promoterId = "";
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
			types.add(SystemsBiologyOntology.INHIBITION);
			
			Interaction interaction = moduleDef.createInteraction(inter_id, types);
			
			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.INHIBITED);
			interaction.createParticipation(r.getSpecies(), r.getSpecies(),SystemsBiologyOntology.INHIBITOR);
		}
		
		// Repeat same steps for the list of activators
		for(ModifierSpeciesReference a : activators)
		{
			String inter_id = a.getSpecies() + "_act_" + promoterId;
			
			Set<URI> types = new HashSet<URI>();
			types.add(SystemsBiologyOntology.STIMULATION); 
			
			Interaction interaction = moduleDef.createInteraction(inter_id, types);
			
			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.STIMULATED);
			interaction.createParticipation(a.getSpecies(), a.getSpecies(),SystemsBiologyOntology.STIMULATOR);
		}
		
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			String i_id = promoterId + "_prod_" + product.getSpecies();
			
			Set<URI> type = new HashSet<URI>();
			type.add(SystemsBiologyOntology.GENETIC_PRODUCTION);
			
			Interaction interaction = moduleDef.createInteraction(i_id, type);
			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.TEMPLATE);
			interaction.createParticipation(product.getSpecies(), product.getSpecies(),SystemsBiologyOntology.PRODUCT);
		}
	}
	
	public void extractComplexReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		Set<URI> type = new HashSet<URI>();
		type.add(SystemsBiologyOntology.NON_COVALENT_BINDING);
		
		Interaction inter = moduleDef.createInteraction(reaction.getId(), type);
		
		for(SpeciesReference reactant : reaction.getListOfReactants())
		{
			inter.createParticipation(reactant.getSpecies(), reactant.getSpecies(),SystemsBiologyOntology.REACTANT);
		}
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			inter.createParticipation(product.getSpecies(), product.getSpecies(),SystemsBiologyOntology.PRODUCT);
		}
	}
	
	public void extractDegradationReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		Set<URI> types = new HashSet<URI>();
		types.add(SystemsBiologyOntology.DEGRADATION);
		
		Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
		
		for(SpeciesReference sp : reaction.getListOfReactants())
		{
			String p_id = sp.getSpecies();
			inter.createParticipation(p_id, sp.getSpecies(),SystemsBiologyOntology.REACTANT);
		}
	}
	
	public void extractSubModels(SBMLDocument sbmlDoc, SBOLDocument sbolDoc, Collection collection, ModuleDefinition moduleDef, Model model) throws SBOLValidationException
	{
		ArrayList<String> comps = new ArrayList<String>();
		CompSBMLDocumentPlugin sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(sbmlDoc.getModel());

		if (sbmlCompModel.getListOfSubmodels().size()>0) 
		{
//			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				String subModelId = sbmlCompModel.getListOfSubmodels().get(i).getId();
				String extModel = sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
						.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
				SBMLDocument subDocument = SBMLutilities.readSBML(path + GlobalConstants.separator + extModel);
				if (!comps.contains(extModel)) 
				{
					comps.add(extModel);
					export_recurse(sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
							.getModelRef()).getSource(),subDocument,sbolDoc,collection);
				}
				Module m = moduleDef.createModule(subModelId, subDocument.getModel().getId(), VERSION);
				
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
