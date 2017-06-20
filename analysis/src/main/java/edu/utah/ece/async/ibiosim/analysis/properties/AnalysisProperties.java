package edu.utah.ece.async.ibiosim.analysis.properties;

public final class AnalysisProperties {

  
	private static enum UserInterval{
		MIN_PRINT_INTERVAL, NUM_STEPS, PRINT_INTERVAL;
	}	
	
	private static enum SimMethod{
	  ODE, SSA, MARKOV, FBA, SBML, DOT, XHTML, LHPN;
  } 
	
	 private static enum AbstractionMethod{
	    NONE, EXPAND, ABSTRACTION, NARY;
	  }

  private String filename, id, root, directory, outDir, modelFile, command;
 
  
	private SimMethod method;

	private AbstractionMethod abs;
	
	private final AdvancedProperties advProperties;
	private final IncrementalProperties incProperties;
	private final SimulationProperties simProperties;
	private final VerificationProperties verifProperties;
	private final OptionalProperties optProperties;
	
	
	private final boolean gui;
	
	private String				sim;
	private String				simProp;
	private String fileStem;
	
	private UserInterval userInterval;
	
	public AnalysisProperties(String id, String filename, String root, boolean isGui)
	{
	  this.id = id;
	  this.filename = filename;
	  this.root = root;
	  this.gui = isGui;
	  
	  this.method = SimMethod.ODE;
	  this.abs = AbstractionMethod.NONE;
	  this.userInterval = UserInterval.PRINT_INTERVAL;
	  
	  this.advProperties = new AdvancedProperties();
	  this.incProperties = new IncrementalProperties();
	  this.simProperties = new SimulationProperties();
	  this.verifProperties = new VerificationProperties();
	  this.optProperties = new OptionalProperties();
	}
   
   public AdvancedProperties getAdvancedProperties()
   {
     return advProperties;
   }
   
    public IncrementalProperties getIncrementalProperties()
     {
       return incProperties;
     }
   
    public SimulationProperties getSimulationProperties()
    {
      return simProperties;
    }
    
    public VerificationProperties getVerificationProperties()
    {
      return verifProperties;
    }
    
    public OptionalProperties getOptionalProperties()
    {
      return optProperties;
    }
	 
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	

	/**
	 * @return the outDir
	 */
	public String getOutDir() {
		return outDir;
	}

	
	
	
	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @return the sim
	 */
	public String getSim() {
		return sim;
	}
	/**
	 * @return the simProp
	 */
	public String getSimProp() {
		return simProp;
	}
	
	/**
	 * @return the abs
	 */
	public boolean isAbs() {
		return abs == AbstractionMethod.ABSTRACTION;
	}

	/**
	 * @return the dot
	 */
	public boolean isDot() {
		return method == SimMethod.DOT;
	}
	/**
	 * @return the expand
	 */
	public boolean isExpand() {
		return abs == AbstractionMethod.EXPAND;
	}
	/**
	 * @return the fba
	 */
	public boolean isFba() {
		return method == SimMethod.FBA;
	}
	
	public boolean isGui()
	{
	  return gui;
	}
	
	/**
	 * @return the lhpn
	 */
	public boolean isLhpn() {
		return method == SimMethod.LHPN;
	}
	/**
	 * @return the markov
	 */
	public boolean isMarkov() {
		return method == SimMethod.MARKOV;
	}

	public boolean isMinPrintInterval()
	{
		return userInterval == UserInterval.MIN_PRINT_INTERVAL;
	}

	/**
	 * @return the nary
	 */
	public boolean isNary() {
		return abs == AbstractionMethod.NARY;
	}
	/**
	 * @return the none
	 */
	public boolean isNone() {
		return abs == AbstractionMethod.NONE;
	}
	public boolean isNumSteps()
	{
		return userInterval == UserInterval.NUM_STEPS;
	}
	/**
	 * @return the ode
	 */
	public boolean isOde() {
		return method == SimMethod.ODE;
	}
	public boolean isPrintInterval()
	{
		return userInterval == UserInterval.PRINT_INTERVAL;
	}
	/**
	 * @return the sbml
	 */
	public boolean isSbml() {
		return method == SimMethod.SBML;
	}
	/**
	 * @return the ssa
	 */
	public boolean isSsa() {
		return method == SimMethod.SSA;
	}
	/**
	 * @return the xhtml
	 */
	public boolean isXhtml() {
		return method == SimMethod.XHTML;
	}
	/**
	 * @param abs the abs to set
	 */
	public void setAbs() {
		this.abs = AbstractionMethod.ABSTRACTION;
	}



	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * 
	 */
	public void setDot() {
	  method = SimMethod.DOT;
	}
	/**
	 * @param expand the expand to set
	 */
	public void setExpand() {
		this.abs = AbstractionMethod.EXPAND;
	}
	/**
	 */
	public void setFba() {
	  method = SimMethod.FBA;
	}





	/**
	 * @param lhpn the lhpn to set
	 */
	public void setLhpn() {
	  method = SimMethod.LHPN;
	}
	

	
	/**
	 *
	 */
	public void setMarkov() {
	  method = SimMethod.MARKOV;
	}
	

	
	public void setMinPrintInterval()
	{
		userInterval = UserInterval.MIN_PRINT_INTERVAL;
	}


	/**
	 * @param nary the nary to set
	 */
	public void setNary() {
		this.abs = AbstractionMethod.NARY;
	}
	/**
	 * @param none the none to set
	 */
	public void setNone() {
		this.abs = AbstractionMethod.NONE;
	}

	public void setNumSteps()
	{
		userInterval = UserInterval.NUM_STEPS;
	}
	/**
	 */
	public void setOde() {
	  method = SimMethod.ODE;
	}
	/**
	 * @param outDir the outDir to set
	 */
	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}
	
	public void setPrintInterval()
	{
		userInterval = UserInterval.PRINT_INTERVAL;
	}
	
	/**
	 */
	public void setSbml() {
	  method = SimMethod.SBML;
	}
	/**
	 * @param sim the sim to set
	 */
	public void setSim(String sim) {
		this.sim = sim;
	}
	/**
	 * @param simProp the simProp to set
	 */
	public void setSimProp(String simProp) {
		this.simProp = simProp;
	}
	/**
	 *
	 */
	public void setSsa() {
	  method = SimMethod.SSA;
	}

	
	/**
	 * @param xhtml the xhtml to set
	 */
	public void setXhtml() {
	  method = SimMethod.XHTML;
	}

  


  
  /**
   * @return the method
   */
  public SimMethod getMethod() {
    return method;
  }

  
  /**
   * @param method the method to set
   */
  public void setMethod(SimMethod method) {
    this.method = method;
  }

  
  /**
   * @return the userInterval
   */
  public UserInterval getUserInterval() {
    return userInterval;
  }

  
  /**
   * @param userInterval the userInterval to set
   */
  public void setUserInterval(UserInterval userInterval) {
    this.userInterval = userInterval;
  }

  
  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * @param root the root to set
   */
  public void setRoot(String root) {
    this.root = root;
  }

  
  /**
   * @return the modelFile
   */
  public String getModelFile() {
    return modelFile;
  }

  /**
   * @return the directory
   */
  public String getDirectory() {
    return directory;
  }
  
  /**
   * @param modelFile the modelFile to set
   */
  public void setModelFile(String modelFile) {
    this.modelFile = modelFile;
  }

  
  public String getFileStem() {
    return fileStem;
  }

  
  public void setFileStem(String fileStem) {
    this.fileStem = fileStem;
  }
  
  
  
  
}
