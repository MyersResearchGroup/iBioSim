package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import biomodelsim.BioSim;

/**
 * Used to launch the properties windows so that we don't have to pass
 * the required vars all around.
 * 
 * NOTE: It seems like the assumption that the biosim, gcmFile, propertyLists, and especially
 * paramsOnly being constant for the entire run of the program might not be valid. If that is
 * true, then it was a mistake to make this a singleton and it should be altered.
 * 
 * @author tyler
 *
 */
public class PropertiesLauncher {

	private BioSim biosim;
	private GCMFile gcmFile;
	PropertyList speciesList;
	PropertyList influencesList;
	PropertyList propertyList;
	boolean paramsOnly;
	
	// Singleton stuff
	private static PropertiesLauncher instance;
	public static PropertiesLauncher getInstance(){
		if(instance == null)
			instance = new PropertiesLauncher();
		return instance;
	}
	private PropertiesLauncher(){
		super();
	}
	
	public void initialize(BioSim biosim, GCMFile gcmFile, 
			PropertyList speciesList, PropertyList influencesList, 
			PropertyList propertyList, boolean paramsOnly){
		this.biosim = biosim;
		this.gcmFile = gcmFile;
		this.speciesList = speciesList;
		this.influencesList = influencesList;
		this.propertyList = propertyList;
		this.paramsOnly = paramsOnly;
		
		this.ensureInitialized();
	}
	
	private void ensureInitialized(){
		if(biosim == null || gcmFile == null || speciesList == null || influencesList == null || propertyList == null){
			throw(new Error("PropertiesLauncher was not initialized!"));
		}
	}
	
	public void launchSpeciesEditor(String selected){
		ensureInitialized();
		
		SpeciesPanel panel = new SpeciesPanel(selected, speciesList, influencesList, gcmFile, paramsOnly, biosim);
	}
	
	public void launchInfluencePanel(String selected){
		ensureInitialized();
//		public InfluencePanel(String selected, PropertyList list, GCMFile gcm, boolean paramsOnly, BioSim biosim)
		InfluencePanel panel = new InfluencePanel(selected, influencesList, gcmFile, paramsOnly, biosim);
	}
	
	public void addInfluenceToList(String id){
		influencesList.addItem(id);
		influencesList.setSelectedValue(id, true);
	}
	
	public void removeInfluenceFromList(String id){
		influencesList.removeItem(id);
	}
	
	public void removeSpeciesFromList(String id){
		speciesList.removeItem(id);
	}
}
