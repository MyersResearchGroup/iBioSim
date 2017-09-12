package edu.utah.ece.async.ibiosim.analysis.simulation;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

public abstract class AbstractSimulator extends CoreObservable implements ParentSimulator{

  protected final Message message = new Message();
  
}
