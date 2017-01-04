package backend.synthesis.mapTechnology;

import java.util.ArrayList;
import java.util.List;

public class StringNode
{
	private List<StringNode> children;
	private int stateNum; 
	private String weight; 
	private String nextState;
	
	public StringNode(int stateNum)
	{
		this.children = new ArrayList<StringNode>();
		this.stateNum = stateNum;
		this.weight = null; 
	}
	
	public void setWeight(String c)
	{
		
	}

}
