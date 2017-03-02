/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.learn.genenet;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
