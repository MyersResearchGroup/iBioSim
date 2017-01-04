package backend.learn.genenet;

public class Thresholds
{

	private double	Ta, Tr, Tv, Tt;

	public Thresholds()
	{
		Ta = 1.15;
		Tr = 0.75;
		Tv = 0.5;
		Tt = 0.025;
	}

	public Thresholds(double Ta, double Tr, double Tv)
	{
		this.Ta = Ta;
		this.Tr = Tr;
		this.Tv = Tv;
		this.Tt = 0.025;
	}

	public double getTa()
	{
		return Ta;
	}

	public double getTr()
	{
		return Tr;
	}

	public double getTv()
	{
		return Tv;
	}

	public void setTa(double Ta)
	{
		this.Ta = Ta;
	}

	public void setTr(double Tr)
	{
		this.Tr = Tr;
	}

	public void setTv(double Tv)
	{
		this.Tv = Tv;
	}

	public double getTt()
	{
		return Tt;
	}
}
