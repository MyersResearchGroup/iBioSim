package dataModels.biomodel.network;

/**
 * This class describes an abstract species class. Species are nodes in the
 * graph. All species are derived from this class.
 * 
 * @author Nam Nguyen
 * 
 */
public abstract class AbstractSpecies implements SpeciesInterface {

	/**
	 * Empty constructor.
	 * 
	 */
	public AbstractSpecies() {
		//properties = new Properties();
	}
	
	/**
	 * Returns the id of the species
	 * 
	 * @return the id of the species
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the species
	 * 
	 * @param id
	 *            the id of the species
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	@Override
	public String getStateName() {
		return stateName;
	}

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	@Override
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public String toString() {
		return getId();
	}
	
	/*
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();			
		}
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return null;
		}
		return properties.getProperty(key);
	}
	
	public boolean containsKey(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return false;
		}
		return true;
	}
	*/
	
	@Override
	public boolean isDiffusible() {
		return diffusible;
	}
	
	@Override
	public void setDiffusible(boolean diffusible) {
		this.diffusible = diffusible;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public double getInitialAmount() {
		/*
		if (getProperty(GlobalConstants.INITIAL_STRING) != null &&
			Utility.isValid(getProperty(GlobalConstants.INITIAL_STRING), Utility.NUMstring)) {
			return Double.parseDouble(getProperty(GlobalConstants.INITIAL_STRING));
		} 
		return -1;
		*/
		return amount;
	}
	
	@Override
	public void setInitialAmount(double amount) {
		this.amount = amount;
		this.concentration = -1;
	}
	
	@Override
	public double getInitialConcentration() {
		/*
		if (getProperty(GlobalConstants.INITIAL_STRING) != null &&
			Utility.isValid(getProperty(GlobalConstants.INITIAL_STRING), Utility.NUMstring)) {
			return -1;
		} else if (getProperty(GlobalConstants.INITIAL_STRING) != null) {
			String conc = getProperty(GlobalConstants.INITIAL_STRING);
			return Double.parseDouble(conc.substring(1,conc.length()-1));
		} else {
			return -1;
		}
			*/
		return concentration;
	}
	
	@Override
	public void setInitialConcentration(double concentration) {
		this.amount = -1;
		this.concentration = concentration;
	}
	
	@Override
	public double getDecay() {
		return kd;
	}
	
	@Override
	public void setDecay(double kd) {
		this.kd = kd;
	}
	
	@Override
	public double getKo() {
		return ko;
	}
	
	@Override
	public void setKo(double ko) {
		this.ko = ko;
	}
	
	@Override
	public double getnp() {
		return np;
	}
	
	@Override
	public void setnp(double np) {
		this.np = np;
	}
		
	@Override
	public double[] getKmdiff() {
		/*
		if (getProperty(GlobalConstants.MEMDIFF_STRING)==null) {
			double[] params = new double[1];
			params[0]=-1;
			return params;
		}
		String[] props = getProperty(GlobalConstants.MEMDIFF_STRING).split("/");
		
		double[] params = new double[2];
		
		if (props.length == 2) {
			
			params[0] = Double.parseDouble(props[0]);
			params[1] = Double.parseDouble(props[1]);
		}
		else if (props.length == 1) {
			params[0] = Double.parseDouble(props[0]);
			params[1] = 1.0;			
		}
		else {
			params[0] = 1.0;
			params[1] = 1.0;
		}
		
		return params;
		*/
		return Kmdiff;
	}
	
	@Override
	public void setKmdiff(double kmdiff_f,double kmdiff_r) {
		Kmdiff = new double[2];
		Kmdiff[0] = kmdiff_f;
		Kmdiff[1] = kmdiff_r;
	}
	
	@Override
	public double getKecdiff() {
		/*
		if (getProperty(GlobalConstants.KECDIFF_STRING)!=null) 
			return Double.parseDouble(getProperty(GlobalConstants.KECDIFF_STRING));
			*/
		return -1;
	}
	
	@Override
	public double getKecdecay() {
		/*
		if (getProperty(GlobalConstants.KECDECAY_STRING)!=null) 
			return Double.parseDouble(getProperty(GlobalConstants.KECDECAY_STRING));
			*/
		return -1;
	}
	
	@Override
	public double[] getKc() {
		/*
		if (getProperty(GlobalConstants.KCOMPLEX_STRING)==null) {
			double[] params = new double[1];
			params[0]=-1;
			return params;
		}
		String[] props = getProperty(GlobalConstants.KCOMPLEX_STRING).split("/");
		double[] params = new double[props.length];
		for (int i = 0; i < props.length; i++)
			params[i] = Double.parseDouble(props[i]);
		return params;
		*/
		return Kc;
	}
	
	@Override
	public void setKc(double kc_f,double kc_r) {
		Kc = new double[2];
		Kc[0] = kc_f;
		Kc[1] = kc_r;
	}
	
	@Override
	public boolean isActivator() {
		return isActivator;
	}
	
	@Override
	public void setActivator(boolean set) {
		isActivator = set;
	}
	
	@Override
	public boolean isRepressor() {
		return isRepressor;
	}
	
	@Override
	public void setRepressor(boolean set) {
		isRepressor = set;
	}
	
	@Override
	public boolean isAbstractable() {
		return isAbstractable;
	}
	
	@Override
	public void setAbstractable(boolean set) {
		isAbstractable = set;
	}
	
	@Override
	public boolean isSequesterAbstractable() {
		return isSequesterAbstractable;
	}
	
	@Override
	public void setSequesterAbstractable(boolean set) {
		isSequesterAbstractable = set;
	}
	
	@Override
	public boolean isSequesterable() {
		return isSequesterable;
	}
	
	@Override
	public void setSequesterable(boolean set) {
		isSequesterable = set;
	}
	
	@Override
	public boolean isConvergent() {
		return isConvergent;
	}
	
	@Override
	public void setConvergent(boolean set) {
		isConvergent = set;
	}
	
	//protected Properties properties;
	
	// The id of the species
	protected String id;

	// The name of the species
	protected String name;
	
	protected String type;
	
	protected boolean diffusible;
	
	protected double amount;
	
	protected double concentration;
	
	protected double[] Kc;
	
	protected double kd;
	
	protected double ko;
	
	protected double np;
	
	protected double[] Kmdiff;

	// The state associated with the species
	protected String stateName;
	
	protected boolean isActivator = false;
	
	protected boolean isRepressor = false;
	
//	protected boolean isInteresting = false;
	
	protected boolean isAbstractable = false;
	
	protected boolean isSequesterAbstractable = false;
	
	protected boolean isSequesterable = false;
	
	protected boolean isConvergent = false;
	
}
