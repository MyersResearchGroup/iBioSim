package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.List;

import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

public class VerificationProperties 
{

  private AbstractionProperty absProperty;
  
  private String lpnProperty, constraintProperty;
  

  /**
   * @return the absProproperty
   */
  public AbstractionProperty getAbsProproperty() {
    return absProperty;
  }
  
  /**
   * @return the constraintProperty
   */
  public String getConstraintProperty() {
    return constraintProperty;
  }
  
  /**
   * @return the lpnProperty
   */
  public String getLpnProperty() {
    return lpnProperty;
  }
  

  /**
   * @param constraintProperty the constraintProperty to set
   */
  public void setConstraintProperty(String constraintProperty) {
    this.constraintProperty = constraintProperty;
  }
  

  
  /**
   * @param lpnProperty the lpnProperty to set
   */
  public void setLpnProperty(String lpnProperty) {
    this.lpnProperty = lpnProperty;
  }
  
}
