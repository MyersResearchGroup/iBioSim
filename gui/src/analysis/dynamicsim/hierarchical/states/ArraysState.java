package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;

import org.sbml.jsbml.Model;

public class ArraysState extends HierarchicalState
{

	public ArraysState(HashMap<String, Model> models, String bioModel,
			String submodelID)
	{
		super(models, bioModel, submodelID);
	}

}
