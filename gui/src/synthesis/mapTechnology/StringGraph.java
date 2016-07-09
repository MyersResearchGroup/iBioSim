package synthesis.mapTechnology;

import java.util.HashMap;
import java.util.Map;

public class StringGraph
{
	private Map<Integer, Integer> _nodes; 
	public int stateNum = 0;
	
	public StringGraph()
	{
		_nodes = new HashMap<Integer, Integer>();
	}
	
	public void createStringGraph()
	{ 
		StringNode n = new StringNode(stateNum);
		_nodes.put(stateNum, stateNum);
		stateNum++;
		
	}

}
