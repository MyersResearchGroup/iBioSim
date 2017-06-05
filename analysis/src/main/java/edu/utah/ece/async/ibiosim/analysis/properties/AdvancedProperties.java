package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.List;

public class AdvancedProperties {

  private List<String> preAbs, loopAbs, postAbs;
  

  private double        qss;
  private double        rap1;
  private double        rap2;
  

  private int         con;

  private double stoichAmp;
  
  /**
   * @return the con
   */
  public int getCon() {
    return con;
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
   * @return the stoichAmp
   */
  public double getStoichAmp() {
    return stoichAmp;
  }
  
  /**
   * @param con the con to set
   */
  public void setCon(int con) {
    this.con = con;
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
   * @param stoichAmp the stoichAmp to set
   */
  public void setStoichAmp(double stoichAmp) {
    this.stoichAmp = stoichAmp;
  }

  
}
