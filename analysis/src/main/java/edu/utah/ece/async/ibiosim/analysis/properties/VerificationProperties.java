package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

public class VerificationProperties 
{

  private AbstractionProperty absProperty;
  
  private String lpnProperty, constraintProperty, factorField, iterField;
  
  private ArrayList<String> abstractInteresting;

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
  
  public void addAbstractInteresting(String interesting)
  {
    if(abstractInteresting == null)
    {
      abstractInteresting = new ArrayList<String>();
    }
    
    abstractInteresting.add(interesting);
  }

  
  public AbstractionProperty getAbsProperty() {
    return absProperty;
  }

  
  public void setAbsProperty(AbstractionProperty absProperty) {
    this.absProperty = absProperty;
  }

  
  public String getFactorField() {
    return factorField;
  }

  
  public void setFactorField(String factorField) {
    this.factorField = factorField;
  }

  
  public ArrayList<String> getAbstractInteresting() {
    return abstractInteresting;
  }

  
  public void setAbstractInteresting(ArrayList<String> abstractInteresting) {
    this.abstractInteresting = abstractInteresting;
  }

  
  public String getIterField() {
    return iterField;
  }

  
  public void setIterField(String iterField) {
    this.iterField = iterField;
  }
  
}
