package backend.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetCon
{

	private Map<String, List<Connection>>	connections;

	public NetCon()
	{
		connections = new HashMap<String, List<Connection>>();
	}

	public boolean containEdge(String s)
	{
		return false;
	}

	public void addConnection(String child, String type, double score, String parent)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, type, parent));
	}

	public void addConnection(String child, Connection connection)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(connection);
	}

	public void addConnection(String child, double score, Connection... collections)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, collections));
	}

	public void removeConnection(String child, Connection connection)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(connection);
		}
	}

	public void removeConnectionByParent(String child, List<String> parents)
	{
		List<Connection> listOfConnections = connections.get(child);

		for (int i = listOfConnections.size() - 1; i >= 0; i--)
		{
			if (listOfConnections.get(i).equalParents(parents))
			{
				listOfConnections.remove(i);
			}
		}

	}

	public void removeConnection(String child, int index)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(index);
		}
	}

	public List<Connection> getListOfConnections(String s)
	{
		return connections.get(s);
	}

	public Map<String, List<Connection>> getConnections()
	{
		return connections;
	}

}
