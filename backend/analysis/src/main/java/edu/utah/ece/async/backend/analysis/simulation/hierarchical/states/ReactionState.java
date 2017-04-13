package edu.utah.ece.async.backend.analysis.simulation.hierarchical.states;


public class ReactionState {

  private boolean           hasEnoughMoleculesFd;
  private boolean           hasEnoughMoleculesRv;
  private double            forwardRateValue;
  private double            reverseRateValue;
  private double            initPropensity;
  private double            initForwardPropensity;
  
  public ReactionState()
  {
    hasEnoughMoleculesFd = false;
    hasEnoughMoleculesRv = false;
    forwardRateValue = 0;
    reverseRateValue = 0;
    initPropensity = 0;
    initForwardPropensity = 0;
  }

  
  public boolean hasEnoughMoleculesFd() {
    return hasEnoughMoleculesFd;
  }

  
  public void setHasEnoughMoleculesFd(boolean hasEnoughMoleculesFd) {
    this.hasEnoughMoleculesFd = hasEnoughMoleculesFd;
  }

  
  public boolean hasEnoughMoleculesRv() {
    return hasEnoughMoleculesRv;
  }

  
  public void setHasEnoughMoleculesRv(boolean hasEnoughMoleculesRv) {
    this.hasEnoughMoleculesRv = hasEnoughMoleculesRv;
  }

  
  public double getForwardRateValue() {
    return forwardRateValue;
  }

  
  public void setForwardRateValue(double forwardRateValue) {
    this.forwardRateValue = forwardRateValue;
  }

  
  public double getReverseRateValue() {
    return reverseRateValue;
  }

  
  public void setReverseRateValue(double reverseRateValue) {
    this.reverseRateValue = reverseRateValue;
  }


  
  public double getInitPropensity() {
    return initPropensity;
  }


  
  public void setInitPropensity(double initPropensity) {
    this.initPropensity = initPropensity;
  }


  
  public double getInitForwardPropensity() {
    return initForwardPropensity;
  }


  
  public void setInitForwardPropensity(double initForwardPropensity) {
    this.initForwardPropensity = initForwardPropensity;
  }


  @Override
  public String toString() {
    return "fw_rate =" + forwardRateValue;
  }
  
  
}
