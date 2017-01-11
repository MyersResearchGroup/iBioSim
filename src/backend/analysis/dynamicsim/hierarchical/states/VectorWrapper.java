package backend.analysis.dynamicsim.hierarchical.states;

import java.util.List;


public class VectorWrapper {
  private double[] values;
  private double[] rates;
  private int[] submodels;
  private boolean isSet;
  private int size;
  private List<Double> initValues;
  
  public VectorWrapper(List<Double> initValues)
  {
    this.size = 0;
    this.values = null;
    this.submodels = null;
    this.values = null;
    this.initValues = null;
    this.isSet = false;
    this.initValues = initValues;
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
    int index = size;
    
    size++;
    
    while(initValues.size() < size)
    {
      initValues.add(0.0);
    }
    
    return index;
  }

  public int getSize()
  {
    return size;
  }

  public boolean isSet()
  {
    return isSet;
  }
  
  
  public List<Double> getInitValues()
  {
    return initValues;
  }
  
  public void initStateValues()
  {
    this.values = new double[size];
    
    for(int i = 0; i < size; ++i)
    {
      values[i] = initValues.get(i);
    }
    
    isSet = true;
  }

}
