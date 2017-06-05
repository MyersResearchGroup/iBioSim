package edu.utah.ece.async.ibiosim.analysis.properties;


public class IncrementalProperties {

  private boolean mpde, meanPath, adaptive;

  private int numPaths;
  

  /**
   * @return the numPaths
   */
  public int getNumPaths() {
    return numPaths;
  }
  
  /**
   * @return the adaptive
   */
  public boolean isAdaptive() {
    return adaptive;
  }
  
  /**
   * @return the meanPath
   */
  public boolean isMeanPath() {
    return meanPath;
  }
  
  /**
   * @return the mpde
   */
  public boolean isMpde() {
    return mpde;
  }
  

  /**
   * @param adaptive the adaptive to set
   */
  public void setAdaptive(boolean adaptive) {
    this.adaptive = adaptive;
  }
  
  /**
   * @param meanPath the meanPath to set
   */
  public void setMeanPath(boolean meanPath) {
    this.meanPath = meanPath;
  }
  
  /**
   * @param mpde the mpde to set
   */
  public void setMpde(boolean mpde) {
    this.mpde = mpde;
  }
  
  /**
   * @param numPaths the numPaths to set
   */
  public void setNumPaths(int numPaths) {
    this.numPaths = numPaths;
  }
}
