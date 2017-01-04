package backend.learn.parameterestimator.methods.sres;

public class EvolutionMethodSetting
{

	/*
	 * * @param verbose if set to true, will print the number of generations
	 * passed and other statistics to {@code stderr}
	 * 
	 * @param lambda solution set size
	 * 
	 * @param mu number of top-ranking solutions selected to produce new
	 * solution set at each generation
	 * 
	 * @param expectedConvergenceRate expected convergence rate
	 * 
	 * @param numberOfSweeps number of times stochastic ranking bubble-sort is
	 * applied to solution set
	 * 
	 * @param rankingPenalizationFactor constraint breaking penalization factor,
	 * should be in { [0, 1]};
	 */
	boolean	verbose;
	int		lambda;
	int		mu;
	// double tau;
	double	expectedConvergenceRate;
	int		numberOfSweeps;
	double	rankingPenalizationFactor;
	double	tauDash;
	int		numberOfgenerations;

	public EvolutionMethodSetting()
	{
		this(true, 200, 30, 1.0, 200, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose)
	{
		this(verbose, 200, 30, 1.0, 200, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda)
	{
		this(verbose, lambda, 30, 1.0, 200, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda, int mu)
	{
		this(verbose, lambda, mu, 1.0, 200, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, 200, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, numberOfSweeps, 1000, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps, int numberofgenerations)
	{
		this(verbose, lambda, mu, expectedConvergenceRate, numberOfSweeps, numberofgenerations, 0.45);
	}

	public EvolutionMethodSetting(boolean verbose, int lambda, int mu, double expectedConvergenceRate, int numberOfSweeps, int numberofgenerations, double rankingPenalizationFactor)
	{
		this.verbose = verbose;
		this.lambda = lambda;
		this.mu = mu;
		// this.tau = expectedConvergenceRate / Math.sqrt(2 *
		// Math.sqrt(numberOfFeatures));
		// this.tauDash = expectedConvergenceRate / Math.sqrt(2 *
		// numberOfFeatures);
		this.numberOfSweeps = numberOfSweeps;
		this.rankingPenalizationFactor = rankingPenalizationFactor;
		this.numberOfgenerations = numberofgenerations;
		this.expectedConvergenceRate = expectedConvergenceRate;
	}

	public boolean getverbose()
	{
		return verbose;
	}

	public int getlamda()
	{
		return lambda;
	}

	public int getmu()
	{
		return mu;
	}

	public double getexpectedConvergenceRate()
	{
		return expectedConvergenceRate;
	}

	public int getnumberOfSweeps()
	{
		return numberOfSweeps;
	}

	public double getrankingPenalizationFactor()
	{
		return rankingPenalizationFactor;
	}

	public double getnumberOfgenerations()
	{
		return numberOfgenerations;

	}
}
