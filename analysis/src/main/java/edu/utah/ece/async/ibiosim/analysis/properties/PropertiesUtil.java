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
package edu.utah.ece.async.ibiosim.analysis.properties;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jlibsedml.Algorithm;
import org.jlibsedml.AlgorithmParameter;
import org.jlibsedml.Annotation;
import org.jlibsedml.modelsupport.KisaoOntology;
import org.jlibsedml.modelsupport.KisaoTerm;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.dataModels.util.SEDMLutilities;

/**
 * A set of helper methods that are used when reading and writing properties files.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public class PropertiesUtil {

	static Algorithm getAlgorithm(AnalysisProperties properties) {
		Algorithm algorithm = null;
		Element para = new Element("analysis");

		String sim = properties.getSim();
		para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
		if (properties.isOde()) {
			if (sim.contains("euler")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_EULER);
			} else if (sim.contains("rk8pd")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_PRINCE_DORMAND);
			} else if (sim.contains("rkf45") || sim.contains("Runge-Kutta-Fehlberg")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
				para.setAttribute("method", sim);
			} else {
				algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
				para.setAttribute("method", sim);
			}
		} else if (properties.isSsa()) {
			if (sim.equals("gillespie")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
			}

			else if (sim.contains("Mixed")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_DFBA_SOA);
			} else if (sim.contains("Hierarchical")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
			} else if (sim.contains("SSA-CR")) {
				algorithm = new Algorithm(GlobalConstants.KISAO_SSA_CR);
			} else {
				algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
				para.setAttribute("method", sim);
				if (sim.equals("iSSA")) {
					if (properties.getIncrementalProperties().isMpde()) {
						para.setAttribute("type", "mpde");
					} else if (properties.getIncrementalProperties().isMeanPath()) {
						para.setAttribute("type", "meanPath");
					} else if (properties.getIncrementalProperties().isMedianPath()) {
						para.setAttribute("type", "medianPath");
					}
					if (properties.getIncrementalProperties().isAdaptive()) {
						para.setAttribute("adaptive", "true");
					} else {
						para.setAttribute("adaptive", "false");
					}
					para.setAttribute("numPaths", String.valueOf(properties.getIncrementalProperties().getNumPaths()));
				}
			}
		} else if (properties.isFba()) {
			algorithm = new Algorithm(GlobalConstants.KISAO_FBA);
		} else {
			algorithm = new Algorithm(GlobalConstants.KISAO_GENERIC);
			if (properties.isSbml()) {
				para.setAttribute("method", "Model");
			} else if (properties.isDot()) {
				para.setAttribute("method", "Network");
			} else if (properties.isXhtml()) {
				para.setAttribute("method", "Browser");
			} else {
				para.setAttribute("method", sim);
			}
		}
		if (properties.isExpand()) {
			para.setAttribute("abstraction", "Expand Reactions");
		} else if (properties.isAbs()) {
			para.setAttribute("abstraction", "Reaction-based");
		} else if (properties.isNary()) {
			para.setAttribute("abstraction", "State-based");
		}
		para.setAttribute("rapid1", String.valueOf(properties.getAdvancedProperties().getRap1()));
		para.setAttribute("rapid2", String.valueOf(properties.getAdvancedProperties().getRap2()));
		para.setAttribute("qssa", String.valueOf(properties.getAdvancedProperties().getQss()));
		para.setAttribute("maxConc", String.valueOf(properties.getAdvancedProperties().getCon()));
		para.setAttribute("stoichAmp", String.valueOf(properties.getAdvancedProperties().getStoichAmp()));
		Annotation ann = new Annotation(para);
		algorithm.addAnnotation(ann);

		SimulationProperties simProperties = properties.getSimulationProperties();

		AlgorithmParameter ap = new AlgorithmParameter(GlobalConstants.KISAO_MINIMUM_STEP_SIZE, String.valueOf(simProperties.getMinTimeStep()));
		algorithm.addAlgorithmParameter(ap);
		ap = new AlgorithmParameter(GlobalConstants.KISAO_MAXIMUM_STEP_SIZE, String.valueOf(simProperties.getMaxTimeStep()));
		algorithm.addAlgorithmParameter(ap);
		ap = new AlgorithmParameter(GlobalConstants.KISAO_ABSOLUTE_TOLERANCE, String.valueOf(simProperties.getAbsError()));
		algorithm.addAlgorithmParameter(ap);
		ap = new AlgorithmParameter(GlobalConstants.KISAO_RELATIVE_TOLERANCE, String.valueOf(simProperties.getRelError()));
		algorithm.addAlgorithmParameter(ap);
		ap = new AlgorithmParameter(GlobalConstants.KISAO_SEED, String.valueOf(simProperties.getRndSeed()));
		algorithm.addAlgorithmParameter(ap);
		ap = new AlgorithmParameter(GlobalConstants.KISAO_NUMBER_OF_RUNS, String.valueOf(simProperties.getRun() + (simProperties.getStartIndex() - 1)));
		algorithm.addAlgorithmParameter(ap);
		return algorithm;
	}

	static void setAlgorithm(Algorithm algorithm, AnalysisProperties properties) {
		String kisaoId = algorithm.getKisaoID();
		KisaoTerm kt = KisaoOntology.getInstance().getTermById(kisaoId);
		String method = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "method", null);
		if (kisaoId.equals(GlobalConstants.KISAO_EULER)) {
			properties.setOde();
			properties.setSim("euler");
		} else if (kisaoId.equals(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG) || kisaoId.equals(GlobalConstants.KISAO_LSODA)) {
			properties.setOde();
			properties.setSim("rkf45");
			if (method != null) {
				properties.setSim(method);
			}
		} else if (kisaoId.equals(GlobalConstants.KISAO_RUNGE_KUTTA_PRINCE_DORMAND)) {
			properties.setOde();
			properties.setSim("rk8pd");
		} else if (kisaoId.equals(GlobalConstants.KISAO_GILLESPIE) || kisaoId.equals(GlobalConstants.KISAO_GILLESPIE_DIRECT)) {
			properties.setSsa();
			properties.setSim("gillespie");
			if (method != null) {
				properties.setSim(method);
				if (method.equals("iSSA")) {
					String type = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "type", "");
					if (type.equals("mpde")) {
						properties.getIncrementalProperties().setMpde(true);
					} else if (type.equals("meanPath")) {
						properties.getIncrementalProperties().setMeanPath(true);
					} else if (type.equals("medianPath")) {
						properties.getIncrementalProperties().setMedianPath(true);
					}
					String adaptive = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "adaptive", "");
					if (adaptive.equals("true")) {
						properties.getIncrementalProperties().setAdaptive(true);
					} else {
						properties.getIncrementalProperties().setAdaptive(false);
					}
					String numPaths = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "numPaths", "");
					if (!numPaths.equals("")) {
						properties.getIncrementalProperties().setNumPaths(Integer.parseInt(numPaths));
					}
				}
			}
		} else if (kisaoId.equals(GlobalConstants.KISAO_SSA_CR)) {
			properties.setSsa();
			properties.setSim("SSA-CR (Dynamic)");
		} else if (kisaoId.equals(GlobalConstants.KISAO_FBA)) {
			properties.setFba();
		} else if (kisaoId.equals(GlobalConstants.KISAO_DFBA) || kisaoId.equals(GlobalConstants.KISAO_DFBA_SOA)) {
			properties.setSsa();
			properties.setSim("Mixed-Hierarchical");
		} else if (kisaoId.equals(GlobalConstants.KISAO_GENERIC)) {
			if (method != null) {
				if (method.equals("Model")) {
					properties.setSbml();
				} else if (method.equals("Network")) {
					properties.setDot();
				} else if (method.equals("Browser")) {
					properties.setXhtml();
				} else {
					properties.setMarkov();
					if (method != null) {
						properties.setSim(method);
					}
				}
			}
		} else if (kt == null || kt.is_a(KisaoOntology.ALGORITHM_WITH_DETERMINISTIC_RULES)) {
			properties.setOde();
			properties.setSim("rkf45");
		} else {
			properties.setSsa();
			properties.setSim("gillespie");
		}
		String abstraction = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "abstraction", null);
		if (abstraction != null) {
			if (abstraction.equals("Expand Reactions")) {
				properties.setExpand();
			} else if (abstraction.equals("Reaction-based")) {
				properties.setAbs();
			} else if (abstraction.equals("State-based")) {
				properties.setNary();
			} else {
				properties.setNone();
			}
		} else {
			properties.setNone();
		}

		// Parse advanced properties
		properties.getAdvancedProperties().setRap1(PropertiesUtil.parseDouble(SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "rapid1", IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.rapid1"))));
		properties.getAdvancedProperties().setRap2(PropertiesUtil.parseDouble(SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "rapid2", IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.rapid2"))));
		properties.getAdvancedProperties().setQss(PropertiesUtil.parseDouble(SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "qssa", IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.qssa"))));
		properties.getAdvancedProperties().setCon(Integer.parseInt(SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "maxConc", IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.concentration"))));
		properties.getAdvancedProperties().setStoichAmp(PropertiesUtil.parseDouble(SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "stoichAmp", IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.amplification"))));

		for (AlgorithmParameter ap : algorithm.getListOfAlgorithmParameters()) {
			if (ap.getKisaoID().equals(GlobalConstants.KISAO_MINIMUM_STEP_SIZE)) {
				properties.getSimulationProperties().setMinTimeStep(PropertiesUtil.parseDouble(ap.getValue()));
			} else if (ap.getKisaoID().equals(GlobalConstants.KISAO_MAXIMUM_STEP_SIZE)) {
				properties.getSimulationProperties().setMaxTimeStep(PropertiesUtil.parseDouble(ap.getValue()));
			} else if (ap.getKisaoID().equals(GlobalConstants.KISAO_ABSOLUTE_TOLERANCE)) {
				properties.getSimulationProperties().setAbsError(PropertiesUtil.parseDouble(ap.getValue()));
			} else if (ap.getKisaoID().equals(GlobalConstants.KISAO_RELATIVE_TOLERANCE)) {
				properties.getSimulationProperties().setRelError(PropertiesUtil.parseDouble(ap.getValue()));
			} else if (ap.getKisaoID().equals(GlobalConstants.KISAO_SEED)) {
				properties.getSimulationProperties().setRndSeed(Long.parseLong(ap.getValue()));
			} else if (ap.getKisaoID().equals(GlobalConstants.KISAO_SAMPLES) || ap.getKisaoID().equals(GlobalConstants.KISAO_NUMBER_OF_RUNS)) {
				properties.getSimulationProperties().setRun(Integer.parseInt(ap.getValue()));
			}
		}
	}

	static double parseDouble(String num) {
		if (num.equals("inf")) {
			return Double.POSITIVE_INFINITY;
		} else if (num.equals("-inf")) {
			return Double.NEGATIVE_INFINITY;
		} else {
			return Double.parseDouble(num);
		}
	}

	/**
	 * Parses a double to string. If the number is infinity, refer to it as "inf".
	 * 
	 * @param the
	 *          number to be parsed.
	 * 
	 * @return the string for the given double.
	 */
	public static String parseDouble(double num) {
		if (num == Double.POSITIVE_INFINITY) {
			return "inf";
		} else if (num == Double.NEGATIVE_INFINITY) {
			return "-inf";
		} else {
			return String.valueOf(num);
		}
	}

}
