package dataModels.util;

import java.util.Observable;
import java.util.Observer;

public abstract class BioObserver implements Observer
{
  public abstract void update(Observable o, Object arg);
}
