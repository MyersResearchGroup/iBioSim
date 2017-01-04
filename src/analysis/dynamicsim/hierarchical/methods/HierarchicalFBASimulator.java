package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;

import analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.fba.FluxBalanceAnalysis;

public class HierarchicalFBASimulator extends HierarchicalSimulation
{

	private FluxBalanceAnalysis		fba;
	private HashMap<String, Double>	values;

	public HierarchicalFBASimulator(HierarchicalSimulation simulation, HierarchicalModel topmodel)
	{
		super(simulation);
		setTopmodel(topmodel);
		values = new HashMap<String, Double>();
	}

	public void setFBA(Model model)
	{
		for (Parameter parameter : model.getListOfParameters())
		{
			values.put(parameter.getId(), parameter.getValue());
		}
		fba = new FluxBalanceAnalysis(model, 1e-9);
	}

	@Override
	public void simulate()
	{
	  //TODO: check return value of fba
		getState();
		fba.setBoundParameters(values);
		fba.PerformFluxBalanceAnalysis();
		retrieveFbaState();
	}

	private void retrieveFbaState()
	{
		Map<String, Double> flux = fba.getFluxes();
		HierarchicalModel topmodel = getTopmodel();
		for (String reaction : flux.keySet())
		{
			topmodel.getNode(reaction).setValue(0, flux.get(reaction));
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

	}

	public void getState()
	{

		HierarchicalModel topmodel = getTopmodel();
		for (String name : values.keySet())
		{
			values.put(name, topmodel.getNode(name).getValue(0));
		}
	}

}
