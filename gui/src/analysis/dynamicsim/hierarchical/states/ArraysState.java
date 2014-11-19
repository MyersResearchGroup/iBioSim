package analysis.dynamicsim.hierarchical.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.ArraysObject;

public class ArraysState extends HierarchicalState
{

	private HashMap<String, List<ArraysObject>>	dimensionObjects;
	private HashMap<String, ASTNode>			values;
	private HashSet<String>						arrayedObjects;

	public ArraysState(HashMap<String, Model> models, String bioModel,
			String submodelID)
	{
		super(models, bioModel, submodelID);
		dimensionObjects = new HashMap<String, List<ArraysObject>>();
		arrayedObjects = new HashSet<String>();
		values = new HashMap<String, ASTNode>();
	}

	public void addArrayedObject(String id)
	{
		arrayedObjects.add(id);
	}

	public void addValue(String id, double value, int... indices)
	{
		ASTNode vector = values.get(id);
		ASTNode child;
		if (vector == null)
		{
			vector = new ASTNode(ASTNode.Type.VECTOR);
			values.put(id, vector);
		}
		child = vector;
		for (int i = indices.length - 1; i >= 0; i--)
		{
			while (child.getChildCount() <= indices[i])
			{
				child.addChild(new ASTNode(ASTNode.Type.VECTOR));
			}
			child = child.getChild(indices[i]);
		}

		child.setValue(value);
	}

	public void addDimension(String id, String size, int arrayDim)
	{
		List<ArraysObject> list = dimensionObjects.get(id);
		if (list == null)
		{
			list = new ArrayList<ArraysObject>();
			dimensionObjects.put(id, list);
		}
		list.add(new ArraysObject(size, arrayDim));
	}

	public HashMap<String, List<ArraysObject>> getDimensionObjects()
	{
		return dimensionObjects;
	}

	public int getDimensionCount(String id)
	{
		return dimensionObjects.get(id) == null ? -1 : dimensionObjects.get(id)
				.size();
	}

	public void setDimensionObjects(
			HashMap<String, List<ArraysObject>> dimensionObjects)
	{
		this.dimensionObjects = dimensionObjects;
	}

	public HashSet<String> getArrayedObjects()
	{
		return arrayedObjects;
	}

	public void setArrayedObjects(HashSet<String> arrayedObjects)
	{
		this.arrayedObjects = arrayedObjects;
	}

	public HashMap<String, ASTNode> getValues()
	{
		return values;
	}

	public void setValues(HashMap<String, ASTNode> values)
	{
		this.values = values;
	}

}
