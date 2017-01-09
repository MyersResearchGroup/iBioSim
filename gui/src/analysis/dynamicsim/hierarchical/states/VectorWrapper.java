package analysis.dynamicsim.hierarchical.states;


public class VectorWrapper {
  private double[] values;
  private double[] rates;
  private int[] submodels;
      
  public void setVector(double[] vector)
  {
    this.values = vector;
  }
  
  public double[] getVector()
  {
    return this.values;
  }
  
}
