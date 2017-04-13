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
package edu.utah.ece.async.backend.learn.parameterestimator.methods.sres;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Modelsettings
{

	public List<Double>	IC;
	public int			ngenes;
	public int			startp;
	public int			endp;
	double[]			lowerBounds;
	double[]			upperBounds;
	boolean				verbose;
	int					nums;

	public Modelsettings(List<Double> ic, int ngenes, int sp, int ep, double[] lowerbounds, double[] upperbounds, boolean verbose)
	{

		this.IC = ic;
		this.ngenes = ngenes;
		this.startp = sp;
		this.endp = ep;
		this.lowerBounds = lowerbounds;
		this.upperBounds = upperbounds;
		this.verbose = verbose;
	}

	public double[][] loaddata(String filename)
	{
		// StringBuffer sb=new StringBuffer();
		String tempstr = null;
		int lines = 0;
		int rows = 0;
		double[][] tmps = new double[100][100];
		try
		{
			// String path="/Users/mfan/Documents/program/data/model1.txt";
			String path = new String(filename);
			File file = new File(path);
			if (!file.exists())
			{
				throw new FileNotFoundException();
			}
			// BufferedReader br=new BufferedReader(new FileReader(file));
			// while((tempstr=br.readLine())!=null)
			// sb.append(tempstr);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			while ((tempstr = br.readLine()) != null)
			{
				String s[] = tempstr.split(" ");
				// System.out.println(s.length);
				for (int i = 0; i < s.length; ++i)
				{
					tmps[lines][i] = Double.parseDouble(s[i]);
				}
				lines = lines + 1;
				rows = s.length;
			}
			br.close();
			// double rows=tmps[0].length;

		}
		catch (IOException ex)
		{
			System.out.println(ex.getStackTrace());
		}
		double[][] data = new double[lines][rows];
		for (int i = 0; i < lines; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				data[i][j] = tmps[i][j];
			}
		}
		return data;
		// return sb.toString();
	}

}
