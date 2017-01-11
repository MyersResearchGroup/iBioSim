package backend.analysis.dynamicsim.hierarchical.states;


public class VectorWrapper {
  private double[] values;
  private double[] rates;
  private int[] submodels;
  private int size;
  
  public VectorWrapper()
  {
    this.size = 0;
    this.values = null;
    this.submodels = null;
    this.values = null;
  }
  
  public void setValues(double[] vector)
  {
    this.values = vector;
  }
  
  public double[] getValues()
  {
    return this.values;
  }
  
  public void setRates(double[] vector)
  {
    this.rates = vector;
  }
  
  public double[] getRates()
  {
    return this.rates;
  }
  
  public void setSize(int size)
  {
    this.size = size;
  }
  
  public int incrementSize()
  {
    return size++;
  }
  
  public int getSize()
  {
    return size;
  }
  
  
}
