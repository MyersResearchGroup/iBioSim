package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

public final class AnalysisProperties {

  
	private static enum UserInterval{
		MIN_PRINT_INTERVAL, NUM_STEPS, PRINT_INTERVAL;
	}	
	
	private static enum SimMethod{
	  ODE, SSA, MARKOV, FBA, SBML, DOT, XHTML, LHPN;
  } 
	

  private final String filename, id, root;
  private final AbstractionProperty absProperty;
  
  
	private SimMethod method;
	
	private int					con;
	
	private String command, outDir;
	
	private String lpnProperty, constraintProperty, genStats;
	
	private List<String>			intSpecies;

	private boolean mpde, meanPath, adaptive;
	private boolean none, expand, abs, nary;

  
  private double        absError, relError;

	private int numPaths;
	
	private List<String> preAbs, loopAbs, postAbs;
	private String				printer_id;
	private String				printer_track_quantity;
	private double				qss;
	private double				rap1;
	private double				rap2;

	private long				rndSeed;

	private int					run;
	private int numSteps;
	
	private String				sim;
	private String				simProp;
	
	private double stoichAmp;
	private double				initialTime, outputStartTime, minTimeStep, printInterval, timeLimit, timeStep;
	
	private UserInterval userInterval;
	
	public AnalysisProperties(String id, String filename, String root, AbstractionProperty absProperty)
	{
	  this.id = id;
	  this.filename = filename;
	  this.root = root;
	  this.absProperty = absProperty;
	}
	
	/**
	 * @return the absError
	 */
	public double getAbsError() {
		return absError;
	}
	/**
	 * @return the absProproperty
	 */
	public AbstractionProperty getAbsProproperty() {
		return absProperty;
	}
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * @return the con
	 */
	public int getCon() {
		return con;
	}
	/**
	 * @return the constraintProperty
	 */
	public String getConstraintProperty() {
		return constraintProperty;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @return the genStats
	 */
	public String getGenStats() {
		return genStats;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the initialTime
	 */
	public double getInitialTime() {
		return initialTime;
	}
	/**
	 * @return the intSpecies
	 */
	public List<String> getIntSpecies() {
		return intSpecies;
	}
	/**
	 * @return the loopAbs
	 */
	public List<String> getLoopAbs() {
		return loopAbs;
	}
	/**
	 * @return the lpnProperty
	 */
	public String getLpnProperty() {
		return lpnProperty;
	}
	/**
	 * @return the minTimeStep
	 */
	public double getMinTimeStep() {
		return minTimeStep;
	}
	/**
	 * @return the numPaths
	 */
	public int getNumPaths() {
		return numPaths;
	}
	/**
	 * @return the outDir
	 */
	public String getOutDir() {
		return outDir;
	}
	/**
	 * @return the outputStartTime
	 */
	public double getOutputStartTime() {
		return outputStartTime;
	}
	/**
	 * @return the postAbs
	 */
	public List<String> getPostAbs() {
		return postAbs;
	}
	/**
	 * @return the preAbs
	 */
	public List<String> getPreAbs() {
		return preAbs;
	}
	/**
	 * @return the printer_id
	 */
	public String getPrinter_id() {
		return printer_id;
	}
	/**
	 * @return the printer_track_quantity
	 */
	public String getPrinter_track_quantity() {
		return printer_track_quantity;
	}
	/**
	 * @return the printInterval
	 */
	public double getPrintInterval() {
		return printInterval;
	}
	/**
	 * @return the qss
	 */
	public double getQss() {
		return qss;
	}
	/**
	 * @return the rap1
	 */
	public double getRap1() {
		return rap1;
	}
	/**
	 * @return the rap2
	 */
	public double getRap2() {
		return rap2;
	}
	/**
	 * @return the relError
	 */
	public double getRelError() {
		return relError;
	}
	/**
	 * @return the rndSeed
	 */
	public long getRndSeed() {
		return rndSeed;
	}
	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}
	/**
	 * @return the run
	 */
	public int getRun() {
		return run;
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
	 * @return the stoichAmp
	 */
	public double getStoichAmp() {
		return stoichAmp;
	}
	/**
	 * @return the timeLimit
	 */
	public double getTimeLimit() {
		return timeLimit;
	}
	/**
	 * @return the timeStep
	 */
	public double getTimeStep() {
		return timeStep;
	}
	/**
	 * @return the abs
	 */
	public boolean isAbs() {
		return abs;
	}
	/**
	 * @return the adaptive
	 */
	public boolean isAdaptive() {
		return adaptive;
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
		return expand;
	}
	/**
	 * @return the fba
	 */
	public boolean isFba() {
		return method == SimMethod.FBA;
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
	/**
	 * @return the meanPath
	 */
	public boolean isMeanPath() {
		return meanPath;
	}
	public boolean isMinPrintInterval()
	{
		return userInterval == UserInterval.MIN_PRINT_INTERVAL;
	}
	/**
	 * @return the mpde
	 */
	public boolean isMpde() {
		return mpde;
	}
	/**
	 * @return the nary
	 */
	public boolean isNary() {
		return nary;
	}
	/**
	 * @return the none
	 */
	public boolean isNone() {
		return none;
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
	public void setAbs(boolean abs) {
		this.abs = abs;
	}
	/**
	 * @param absError the absError to set
	 */
	public void setAbsError(double absError) {
		this.absError = absError;
	}


	/**
	 * @param adaptive the adaptive to set
	 */
	public void setAdaptive(boolean adaptive) {
		this.adaptive = adaptive;
	}
	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	/**
	 * @param con the con to set
	 */
	public void setCon(int con) {
		this.con = con;
	}
	/**
	 * @param constraintProperty the constraintProperty to set
	 */
	public void setConstraintProperty(String constraintProperty) {
		this.constraintProperty = constraintProperty;
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
	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	/**
	 */
	public void setFba() {
	  method = SimMethod.FBA;
	}

	/**
	 * @param genStats the genStats to set
	 */
	public void setGenStats(String genStats) {
		this.genStats = genStats;
	}

	/**
	 * @param initialTime the initialTime to set
	 */
	public void setInitialTime(double initialTime) {
		this.initialTime = initialTime;
	}
	/**
	 * @param intSpecies the intSpecies to set
	 */
	public void setIntSpecies(List<String> intSpecies) {
		this.intSpecies = intSpecies;
	}


	/**
	 * @param lhpn the lhpn to set
	 */
	public void setLhpn() {
	  method = SimMethod.LHPN;
	}
	
	/**
	 * @param loopAbs the loopAbs to set
	 */
	public void setLoopAbs(List<String> loopAbs) {
		this.loopAbs = loopAbs;
	}
	
	/**
	 * @param lpnProperty the lpnProperty to set
	 */
	public void setLpnProperty(String lpnProperty) {
		this.lpnProperty = lpnProperty;
	}
	
	/**
	 *
	 */
	public void setMarkov() {
	  method = SimMethod.MARKOV;
	}
	
	/**
	 * @param meanPath the meanPath to set
	 */
	public void setMeanPath(boolean meanPath) {
		this.meanPath = meanPath;
	}
	
	public void setMinPrintInterval()
	{
		userInterval = UserInterval.MIN_PRINT_INTERVAL;
	}
	/**
	 * @param minTimeStep the minTimeStep to set
	 */
	public void setMinTimeStep(double minTimeStep) {
		this.minTimeStep = minTimeStep;
	}
	/**
	 * @param mpde the mpde to set
	 */
	public void setMpde(boolean mpde) {
		this.mpde = mpde;
	}
	/**
	 * @param nary the nary to set
	 */
	public void setNary(boolean nary) {
		this.nary = nary;
	}
	/**
	 * @param none the none to set
	 */
	public void setNone(boolean none) {
		this.none = none;
	}
	/**
	 * @param numPaths the numPaths to set
	 */
	public void setNumPaths(int numPaths) {
		this.numPaths = numPaths;
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
	/**
	 * @param outputStartTime the outputStartTime to set
	 */
	public void setOutputStartTime(double outputStartTime) {
		this.outputStartTime = outputStartTime;
	}
	/**
	 * @param postAbs the postAbs to set
	 */
	public void setPostAbs(List<String> postAbs) {
		this.postAbs = postAbs;
	}
	/**
	 * @param preAbs the preAbs to set
	 */
	public void setPreAbs(List<String> preAbs) {
		this.preAbs = preAbs;
	}
	/**
	 * @param printer_id the printer_id to set
	 */
	public void setPrinter_id(String printer_id) {
		this.printer_id = printer_id;
	}
	/**
	 * @param printer_track_quantity the printer_track_quantity to set
	 */
	public void setPrinter_track_quantity(String printer_track_quantity) {
		this.printer_track_quantity = printer_track_quantity;
	}
	public void setPrintInterval()
	{
		userInterval = UserInterval.PRINT_INTERVAL;
	}
	/**
	 * @param printInterval the printInterval to set
	 */
	public void setPrintInterval(double printInterval) {
		this.printInterval = printInterval;
	}
	/**
	 * @param qss the qss to set
	 */
	public void setQss(double qss) {
		this.qss = qss;
	}
	/**
	 * @param rap1 the rap1 to set
	 */
	public void setRap1(double rap1) {
		this.rap1 = rap1;
	}
	/**
	 * @param rap2 the rap2 to set
	 */
	public void setRap2(double rap2) {
		this.rap2 = rap2;
	}
	/**
	 * @param relError the relError to set
	 */
	public void setRelError(double relError) {
		this.relError = relError;
	}
	/**
	 * @param rndSeed the rndSeed to set
	 */
	public void setRndSeed(long rndSeed) {
		this.rndSeed = rndSeed;
	}
	/**
	 * @param run the run to set
	 */
	public void setRun(int run) {
		this.run = run;
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
	 * @param stoichAmp the stoichAmp to set
	 */
	public void setStoichAmp(double stoichAmp) {
		this.stoichAmp = stoichAmp;
	}
	/**
	 * @param timeLimit the timeLimit to set
	 */
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	/**
	 * @param timeStep the timeStep to set
	 */
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	
	/**
	 * @param xhtml the xhtml to set
	 */
	public void setXhtml() {
	  method = SimMethod.XHTML;
	}

  
  public int getNumSteps() {
    return numSteps;
  }

  
  public void setNumSteps(int numSteps) {
    this.numSteps = numSteps;
  }
}
