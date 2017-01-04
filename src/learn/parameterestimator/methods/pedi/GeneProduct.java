package learn.parameterestimator.methods.pedi;

public class GeneProduct
{
	private double			mRNADegradationConstant;
	private double			translationConstant;
	private double			proteinDegradationConstant;
	private double[]		unkownTranscriptionConstants;
	private GeneProduct[]	transcFactors;
	private double[]		getLowerBounds;
	private double[]		getUpperBounds;
	private double			mRNALevel;
	private double			proteinLevel;

	public GeneProduct()
	{

	}

	public double evaluateTranscriptionRate(double[] proteins)
	{
		return 0;
	}

	/**
	 * RNA degradation constants for each gene. NaN if the constant is unknown,
	 * value if the constant is known.
	 * 
	 * @return
	 */
	public double getmRNADegradationConstant()
	{
		return mRNADegradationConstant;
	}

	public void setmRNADegradationConstant(double mRNADegradationConstant)
	{
		this.mRNADegradationConstant = mRNADegradationConstant;
	}

	public double getTranslationConstant()
	{
		return translationConstant;
	}

	public void setTranslationConstant(double translationConstant)
	{
		this.translationConstant = translationConstant;
	}

	public double getProteinDegradationConstant()
	{
		return proteinDegradationConstant;
	}

	public void setProteinDegradationConstant(double proteinDegradationConstant)
	{
		this.proteinDegradationConstant = proteinDegradationConstant;
	}

	public double[] getUnkownTranscriptionConstants()
	{
		return unkownTranscriptionConstants;
	}

	public void setUnkownTranscriptionConstants(double[] unkownTranscriptionConstants)
	{
		this.unkownTranscriptionConstants = unkownTranscriptionConstants;
	}

	public GeneProduct[] getTranscFactors()
	{
		return transcFactors;
	}

	public void setTranscFactors(GeneProduct[] transcFactors)
	{
		this.transcFactors = transcFactors;
	}

	public double[] getGetLowerBounds()
	{
		return getLowerBounds;
	}

	public void setGetLowerBounds(double[] getLowerBounds)
	{
		this.getLowerBounds = getLowerBounds;
	}

	public double[] getGetUpperBounds()
	{
		return getUpperBounds;
	}

	public void setGetUpperBounds(double[] getUpperBounds)
	{
		this.getUpperBounds = getUpperBounds;
	}

	public double getmRNALevel()
	{
		return mRNALevel;
	}

	public void setmRNALevel(double mRNALevel)
	{
		this.mRNALevel = mRNALevel;
	}

	public double getProteinLevel()
	{
		return proteinLevel;
	}

	public void setProteinLevel(double proteinLevel)
	{
		this.proteinLevel = proteinLevel;
	}
}
