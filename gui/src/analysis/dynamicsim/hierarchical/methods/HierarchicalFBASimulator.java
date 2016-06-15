package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;
import analysis.fba.FluxBalanceAnalysis;

public class HierarchicalFBASimulator extends HierarchicalSimulation
{

	private FluxBalanceAnalysis		fba;
	private HashMap<String, Double>	values;
	private boolean					isInitialized;

	public HierarchicalFBASimulator(HierarchicalSimulation simulation)
	{
		super(simulation);
		values = new HashMap<String, Double>();
	}

	public void setFBA(Model model)
	{
		fba = new FluxBalanceAnalysis(model, 1e-9);
	}

	@Override
	public void simulate()
	{
		getState();
		fba.setBoundParameters(values);
		fba.PerformFluxBalanceAnalysis();
		retrieveFbaState();
	}

	private void retrieveFbaState()
	{
		Map<String, Double> flux = fba.getFluxes();
		ModelState topmodel = getTopmodel();
		for (String reaction : flux.keySet())
		{
			topmodel.getNode(reaction).setValue(flux.get(reaction));
		}

	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setupForNewRun(int newRun)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{

		if (!isInitialized)
		{
			getState();
		}

	}

	public void getState()
	{

		ModelState topmodel = getTopmodel();
		for (VariableNode value : topmodel.getVariables())
		{
			values.put(value.getName(), value.getValue());
		}
	}

}
