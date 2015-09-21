package analysis.dynamicsim.hierarchical.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.arrays.ArraysPair;

public abstract class ArraysState extends HierarchicalState
{

	private HashMap<String, List<ArraysPair>>	arrays;
	private HashSet<String>						arrayedObjects;
	private HashMap<String, String>				arraySizeToSBase;
	private Map<String, double[]>				arrayVariableToValue;

	public ArraysState(Map<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
		arrays = new HashMap<String, List<ArraysPair>>();
		arrayedObjects = new HashSet<String>();
		arraySizeToSBase = new HashMap<String, String>();
		arrayVariableToValue = new HashMap<String, double[]>();
	}

	public ArraysState(ArraysState state)
	{
		super(state);
		arrays = new HashMap<String, List<ArraysPair>>(state.arrays);
		arrayedObjects = new HashSet<String>(state.arrayedObjects);
		arrayVariableToValue = new HashMap<String, double[]>();
	}

	public void addArrayedObject(String id)
	{
		arrayedObjects.add(id);
	}

	public void addArraysPair(String id, ArraysPair pair)
	{
		if (!arrays.containsKey(id))
		{
			arrays.put(id, new ArrayList<ArraysPair>(1));
		}
		arrays.get(id).add(pair);
	}

	public void addSizeTrigger(String parameterSize, String id)
	{
		arraySizeToSBase.put(parameterSize, id);
	}

	public HashSet<String> getArrayedObjects()
	{
		return arrayedObjects;
	}

	public boolean isArrayedObject(String id)
	{
		return arrayedObjects.contains(id);
	}

	public void setArrayedObjects(HashSet<String> arrayedObjects)
	{
		this.arrayedObjects = arrayedObjects;
	}

	public HashMap<String, String> getArraySizeToSBase()
	{
		return arraySizeToSBase;
	}

	public void setArraySizeToSBase(HashMap<String, String> arraySizeToSBase)
	{
		this.arraySizeToSBase = arraySizeToSBase;
	}

	public Map<String, double[]> getArrayVariableToValue()
	{
		return arrayVariableToValue;
	}

	public void setArrayVariableToValue(Map<String, double[]> arrayVariableToValue)
	{
		this.arrayVariableToValue = arrayVariableToValue;
	}

	public HashMap<String, List<ArraysPair>> getArrays()
	{
		return arrays;
	}

	public void setArrays(HashMap<String, List<ArraysPair>> arrays)
	{
		this.arrays = arrays;
	}

}
