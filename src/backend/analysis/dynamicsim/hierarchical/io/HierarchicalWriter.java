package backend.analysis.dynamicsim.hierarchical.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState;

public abstract class HierarchicalWriter {

	protected BufferedWriter bufferedWriter;
	
	protected List<HierarchicalState> listOfStates;
	
	protected FileWriter            writer;
	
	protected boolean isSet;
	public HierarchicalWriter()
	{
		listOfStates = new ArrayList<HierarchicalState>();
		isSet = false;
	}
	
	public abstract void init(String filename) throws IOException;
	
	public abstract void print() throws IOException;
	
	public abstract void addVariable(String id, HierarchicalState state);
}
