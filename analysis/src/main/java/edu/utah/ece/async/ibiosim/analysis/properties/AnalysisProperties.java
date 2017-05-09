package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.List;

import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

public class AnalysisProperties {

	private double				initialTime;
	private double				outputStartTime;
	private double				timeLimit;
	private double				printInterval;
	private double				minTimeStep;
	private double				timeStep;
	private double				absError;
	private double				relError;

	private long				rndSeed;
	private int					run;
	private String				outDir;
	private String[]			intSpecies;
	private double				rap1;
	private double				rap2;
	private double				qss;
	private double stoichAmp;
	private int					con;
	private String				printer_id;
	private String				printer_track_quantity;
	private String				sim;
	private String				simProp;
	private String genStats;

	private AbstractionProperty absProproperty;

	private List<String> preAbs, loopAbs, postAbs;

	private boolean none, expand, abs, nary;
	private boolean ode, ssa, markov, fba, sbml, dot, xhtml, lhpn;
	
	private int numPaths;
	private boolean mpde, meanPath, adaptive;
	
	private static enum UserInterval{
		PRINT_INTERVAL,
		MIN_PRINT_INTERVAL,
		NUM_STEPS;
	} ;
	
	private UserInterval userInterval;
	
	/**
	 * @return the absProproperty
	 */
	public AbstractionProperty getAbsProproperty() {
		return absProproperty;
	}
	/**
	 * @param absProproperty the absProproperty to set
	 */
	public void setAbsProproperty(AbstractionProperty absProproperty) {
		this.absProproperty = absProproperty;
	}
	/**
	 * @return the preAbs
	 */
	public List<String> getPreAbs() {
		return preAbs;
	}
	/**
	 * @param preAbs the preAbs to set
	 */
	public void setPreAbs(List<String> preAbs) {
		this.preAbs = preAbs;
	}
	/**
	 * @return the loopAbs
	 */
	public List<String> getLoopAbs() {
		return loopAbs;
	}
	/**
	 * @param loopAbs the loopAbs to set
	 */
	public void setLoopAbs(List<String> loopAbs) {
		this.loopAbs = loopAbs;
	}
	/**
	 * @return the postAbs
	 */
	public List<String> getPostAbs() {
		return postAbs;
	}
	/**
	 * @param postAbs the postAbs to set
	 */
	public void setPostAbs(List<String> postAbs) {
		this.postAbs = postAbs;
	}
	/**
	 * @return the none
	 */
	public boolean isNone() {
		return none;
	}
	/**
	 * @param none the none to set
	 */
	public void setNone(boolean none) {
		this.none = none;
	}
	/**
	 * @return the expand
	 */
	public boolean isExpand() {
		return expand;
	}
	/**
	 * @param expand the expand to set
	 */
	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	/**
	 * @return the abs
	 */
	public boolean isAbs() {
		return abs;
	}
	/**
	 * @param abs the abs to set
	 */
	public void setAbs(boolean abs) {
		this.abs = abs;
	}
	/**
	 * @return the nary
	 */
	public boolean isNary() {
		return nary;
	}
	/**
	 * @param nary the nary to set
	 */
	public void setNary(boolean nary) {
		this.nary = nary;
	}
	/**
	 * @return the ode
	 */
	public boolean isOde() {
		return ode;
	}
	/**
	 * @param ode the ode to set
	 */
	public void setOde(boolean ode) {
		this.ode = ode;
	}
	/**
	 * @return the ssa
	 */
	public boolean isSsa() {
		return ssa;
	}
	/**
	 * @param ssa the ssa to set
	 */
	public void setSsa(boolean ssa) {
		this.ssa = ssa;
	}
	/**
	 * @return the absProperty
	 */
	public AbstractionProperty getAbstractionProperty() {
		return absProproperty;
	}
	/**
	 * @param the absProperty
	 */
	public void setAbstractionProproperty(AbstractionProperty absProproperty) {
		this.absProproperty = absProproperty;
	}
	/**
	 * @return the initialTime
	 */
	public double getInitialTime() {
		return initialTime;
	}
	/**
	 * @param initialTime the initialTime to set
	 */
	public void setInitialTime(double initialTime) {
		this.initialTime = initialTime;
	}
	/**
	 * @return the outputStartTime
	 */
	public double getOutputStartTime() {
		return outputStartTime;
	}
	/**
	 * @param outputStartTime the outputStartTime to set
	 */
	public void setOutputStartTime(double outputStartTime) {
		this.outputStartTime = outputStartTime;
	}
	/**
	 * @return the timeLimit
	 */
	public double getTimeLimit() {
		return timeLimit;
	}
	/**
	 * @param timeLimit the timeLimit to set
	 */
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	/**
	 * @return the printInterval
	 */
	public double getPrintInterval() {
		return printInterval;
	}
	/**
	 * @param printInterval the printInterval to set
	 */
	public void setPrintInterval(double printInterval) {
		this.printInterval = printInterval;
	}
	/**
	 * @return the minTimeStep
	 */
	public double getMinTimeStep() {
		return minTimeStep;
	}
	/**
	 * @param minTimeStep the minTimeStep to set
	 */
	public void setMinTimeStep(double minTimeStep) {
		this.minTimeStep = minTimeStep;
	}
	/**
	 * @return the timeStep
	 */
	public double getTimeStep() {
		return timeStep;
	}
	/**
	 * @param timeStep the timeStep to set
	 */
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	/**
	 * @return the absError
	 */
	public double getAbsError() {
		return absError;
	}
	/**
	 * @param absError the absError to set
	 */
	public void setAbsError(double absError) {
		this.absError = absError;
	}
	/**
	 * @return the relError
	 */
	public double getRelError() {
		return relError;
	}
	/**
	 * @param relError the relError to set
	 */
	public void setRelError(double relError) {
		this.relError = relError;
	}
	/**
	 * @return the rndSeed
	 */
	public long getRndSeed() {
		return rndSeed;
	}
	/**
	 * @param rndSeed the rndSeed to set
	 */
	public void setRndSeed(long rndSeed) {
		this.rndSeed = rndSeed;
	}
	/**
	 * @return the run
	 */
	public int getRun() {
		return run;
	}
	/**
	 * @param run the run to set
	 */
	public void setRun(int run) {
		this.run = run;
	}
	/**
	 * @return the outDir
	 */
	public String getOutDir() {
		return outDir;
	}
	/**
	 * @param outDir the outDir to set
	 */
	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}
	/**
	 * @return the intSpecies
	 */
	public String[] getIntSpecies() {
		return intSpecies;
	}
	/**
	 * @param intSpecies the intSpecies to set
	 */
	public void setIntSpecies(String[] intSpecies) {
		this.intSpecies = intSpecies;
	}
	/**
	 * @return the rap1
	 */
	public double getRap1() {
		return rap1;
	}
	/**
	 * @param rap1 the rap1 to set
	 */
	public void setRap1(double rap1) {
		this.rap1 = rap1;
	}
	/**
	 * @return the rap2
	 */
	public double getRap2() {
		return rap2;
	}
	/**
	 * @param rap2 the rap2 to set
	 */
	public void setRap2(double rap2) {
		this.rap2 = rap2;
	}
	/**
	 * @return the qss
	 */
	public double getQss() {
		return qss;
	}
	/**
	 * @param qss the qss to set
	 */
	public void setQss(double qss) {
		this.qss = qss;
	}
	/**
	 * @return the con
	 */
	public int getCon() {
		return con;
	}
	/**
	 * @param con the con to set
	 */
	public void setCon(int con) {
		this.con = con;
	}
	/**
	 * @return the printer_id
	 */
	public String getPrinter_id() {
		return printer_id;
	}
	/**
	 * @param printer_id the printer_id to set
	 */
	public void setPrinter_id(String printer_id) {
		this.printer_id = printer_id;
	}
	/**
	 * @return the printer_track_quantity
	 */
	public String getPrinter_track_quantity() {
		return printer_track_quantity;
	}
	/**
	 * @param printer_track_quantity the printer_track_quantity to set
	 */
	public void setPrinter_track_quantity(String printer_track_quantity) {
		this.printer_track_quantity = printer_track_quantity;
	}
	/**
	 * @return the sim
	 */
	public String getSim() {
		return sim;
	}
	/**
	 * @param sim the sim to set
	 */
	public void setSim(String sim) {
		this.sim = sim;
	}
	/**
	 * @return the simProp
	 */
	public String getSimProp() {
		return simProp;
	}
	/**
	 * @param simProp the simProp to set
	 */
	public void setSimProp(String simProp) {
		this.simProp = simProp;
	}


	public boolean isPrintInterval()
	{
		return userInterval == UserInterval.PRINT_INTERVAL;
	}
	
	public boolean isMinPrintInterval()
	{
		return userInterval == UserInterval.MIN_PRINT_INTERVAL;
	}
	
	public boolean isNumSteps()
	{
		return userInterval == UserInterval.NUM_STEPS;
	}
	
	public void setPrintInterval()
	{
		userInterval = UserInterval.PRINT_INTERVAL;
	}
	
	public void setMinPrintInterval()
	{
		userInterval = UserInterval.MIN_PRINT_INTERVAL;
	}
	
	public void setNumSteps()
	{
		userInterval = UserInterval.NUM_STEPS;
	}
	/**
	 * @return the markov
	 */
	public boolean isMarkov() {
		return markov;
	}
	/**
	 * @param markov the markov to set
	 */
	public void setMarkov(boolean markov) {
		this.markov = markov;
	}
	/**
	 * @return the fba
	 */
	public boolean isFba() {
		return fba;
	}
	/**
	 * @param fba the fba to set
	 */
	public void setFba(boolean fba) {
		this.fba = fba;
	}
	/**
	 * @return the sbml
	 */
	public boolean isSbml() {
		return sbml;
	}
	/**
	 * @param sbml the sbml to set
	 */
	public void setSbml(boolean sbml) {
		this.sbml = sbml;
	}
	/**
	 * @return the dot
	 */
	public boolean isDot() {
		return dot;
	}
	/**
	 * @param dot the dot to set
	 */
	public void setDot(boolean dot) {
		this.dot = dot;
	}
	/**
	 * @return the xhtml
	 */
	public boolean isXhtml() {
		return xhtml;
	}
	/**
	 * @param xhtml the xhtml to set
	 */
	public void setXhtml(boolean xhtml) {
		this.xhtml = xhtml;
	}
	/**
	 * @return the lhpn
	 */
	public boolean isLhpn() {
		return lhpn;
	}
	/**
	 * @param lhpn the lhpn to set
	 */
	public void setLhpn(boolean lhpn) {
		this.lhpn = lhpn;
	}
	/**
	 * @return the numPaths
	 */
	public int getNumPaths() {
		return numPaths;
	}
	/**
	 * @param numPaths the numPaths to set
	 */
	public void setNumPaths(int numPaths) {
		this.numPaths = numPaths;
	}
	/**
	 * @return the mpde
	 */
	public boolean isMpde() {
		return mpde;
	}
	/**
	 * @param mpde the mpde to set
	 */
	public void setMpde(boolean mpde) {
		this.mpde = mpde;
	}
	/**
	 * @return the meanPath
	 */
	public boolean isMeanPath() {
		return meanPath;
	}
	/**
	 * @param meanPath the meanPath to set
	 */
	public void setMeanPath(boolean meanPath) {
		this.meanPath = meanPath;
	}
	/**
	 * @return the adaptive
	 */
	public boolean isAdaptive() {
		return adaptive;
	}
	/**
	 * @param adaptive the adaptive to set
	 */
	public void setAdaptive(boolean adaptive) {
		this.adaptive = adaptive;
	}
	/**
	 * @return the stoichAmp
	 */
	public double getStoichAmp() {
		return stoichAmp;
	}
	/**
	 * @param stoichAmp the stoichAmp to set
	 */
	public void setStoichAmp(double stoichAmp) {
		this.stoichAmp = stoichAmp;
	}
	/**
	 * @return the genStats
	 */
	public String getGenStats() {
		return genStats;
	}
	/**
	 * @param genStats the genStats to set
	 */
	public void setGenStats(String genStats) {
		this.genStats = genStats;
	}
}
