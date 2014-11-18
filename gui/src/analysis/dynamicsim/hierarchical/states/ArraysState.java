package analysis.dynamicsim.hierarchical.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

public class ArraysState extends HierarchicalState
{
	
	private HashMap<String, HashMap<Integer, String>> dimensionToSize;
	private HashMap<String, ArrayList<Double>> arrayedValues;
	private HashSet<String> arrayedObjects;
	
	public ArraysState(HashMap<String, Model> models, String bioModel,
			String submodelID)
	{
		super(models, bioModel, submodelID);
		dimensionToSize = new HashMap<String, HashMap<Integer, String>>();
		arrayedObjects = new HashSet<String>();
		arrayedValues = new  HashMap<String, ArrayList<Double>>();
	}
	
	public void addArrayedObject(String id)
	{
		arrayedObjects.add(id);
	}
	
	public void addValue(String id, int index, double value)
	{
		ArrayList<Double> list = arrayedValues.get(id);
		if(list == null)
		{
			list = arrayedValues.put(id, new ArrayList<Double>());
		}
		
		list.add(index, value);
	}
	
	public void addDimension(String id, int arrayDim, String size)
	{
		HashMap<Integer, String> map = dimensionToSize.get(id);
		if(map == null)
		{
			map = dimensionToSize.put(id, new HashMap<Integer, String>());
		}
		
		map.put(arrayDim, size);
	}
	
	public boolean isArrayed(String id)
	{
		return arrayedObjects.contains(id);
	}
	
	/**
	 * @return the dimensionToSize
	 */
	public HashMap<String, HashMap<Integer, String>> getDimensionToSize() {
		return dimensionToSize;
	}

	/**
	 * @return the arrayedObjects
	 */
	public HashSet<String> getArrayedObjects() {
		return arrayedObjects;
	}

	/**
	 * @param dimensionToSize the dimensionToSize to set
	 */
	public void setDimensionToSize(
			HashMap<String, HashMap<Integer, String>> dimensionToSize) {
		this.dimensionToSize = dimensionToSize;
	}

	/**
	 * @param arrayedObjects the arrayedObjects to set
	 */
	public void setArrayedObjects(HashSet<String> arrayedObjects) {
		this.arrayedObjects = arrayedObjects;
	}

}
