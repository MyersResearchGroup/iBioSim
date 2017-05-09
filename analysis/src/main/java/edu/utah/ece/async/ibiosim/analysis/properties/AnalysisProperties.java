package edu.utah.ece.async.ibiosim.analysis.properties;

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
  private int					con;
  private String				printer_id;
  private String				printer_track_quantity;
  private String				sim;
  private String				simProp;

  private AbstractionProperty absProproperty;
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


}
