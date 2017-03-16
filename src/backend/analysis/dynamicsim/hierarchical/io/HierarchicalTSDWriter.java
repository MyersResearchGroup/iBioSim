package backend.analysis.dynamicsim.hierarchical.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState;

public class HierarchicalTSDWriter extends HierarchicalWriter{

	private StringBuilder header;
	
	public HierarchicalTSDWriter()
	{
		super();
		header = new StringBuilder();
	}
	@Override
	public void print() throws IOException {
		bufferedWriter.write(",\n(");
		for(HierarchicalState state : listOfStates)
		{
			bufferedWriter.write("," + state.getStateValue());
		}
		bufferedWriter.write(")");
		bufferedWriter.flush();
	}

	@Override
	public void addVariable(String id, HierarchicalState state) throws IOException  {
		if(header.length() == 0)
		{
			header.append("(\"" + id + "\"");
		}
		else
		{
			header.append(",\"" + id + "\"");
		}
		
		listOfStates.add(state);
	}
	@Override
	public void init(String filename) throws IOException {
		writer = new FileWriter(filename);
		bufferedWriter = new BufferedWriter(writer);
		bufferedWriter.write(header.toString());
		bufferedWriter.flush();
	}



}
