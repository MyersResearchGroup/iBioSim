package synthesis.mapTechnology;

public class WeightedGraph
{

	private SBOLGraph graph;
	private double weight;

	public WeightedGraph(SBOLGraph g, double weight)
	{
		this.graph = g;
		this.weight = weight;

	}
	public SBOLGraph getSBOLGraph()
	{ 
		return this.graph;
	}
	public double getWeight()
	{
		return this.weight;

	}

}
