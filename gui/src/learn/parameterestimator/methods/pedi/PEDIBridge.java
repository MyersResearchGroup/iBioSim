package learn.parameterestimator.methods.pedi;

public interface PEDIBridge
{

	public double[][] simulate(GeneProduct[] genes, double startTime, double endTime, double printInterval);

}
