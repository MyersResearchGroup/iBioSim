
package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;

public class CompartmentSetup
{
  public static void setupCompartments(HierarchicalModel modelstate,  StateType type, Model model, VectorWrapper wrapper)
  {
    for (Compartment compartment : model.getListOfCompartments())
    {
      if (modelstate.isDeletedBySId(compartment.getId()))
      {
        continue;
      }
      setupSingleCompartment(modelstate, compartment, type, wrapper);
    }
  }

  private static void setupSingleCompartment(HierarchicalModel modelstate, Compartment compartment, StateType type, VectorWrapper wrapper)
  {

    String compartmentID = compartment.getId();
    VariableNode node = new VariableNode(compartmentID);
    

    
    if (compartment.getConstant())
    {
      node.createState(StateType.SCALAR, wrapper);
      modelstate.addMappingNode(compartmentID, node);
    }
    else
    {
      node.createState(type, wrapper);
      modelstate.addVariable(node);
    }
    
    if (Double.isNaN(compartment.getSize()))
    {
      node.setValue(modelstate.getIndex(), 1);
    }
    else
    {
      node.setValue(modelstate.getIndex(), compartment.getSize());
    }
  }

}
