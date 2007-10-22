package reb2sac.core.gui;

import javax.swing.*;

/**
 * This is the Button_Enabling class. It contains methods that enable and
 * disable radio buttons and textfields.
 * 
 * @author Curtis Madsen
 */
public class Button_Enabling {

	/**
	 * This static method enables and disables the required fields for none and
	 * abstraction.
	 */
	public static void enableNoneOrAbs(JRadioButton ODE, JRadioButton monteCarlo,
			JRadioButton markov, JTextField seed, JLabel seedLabel,	JTextField runs,
			JLabel runsLabel, JLabel stepLabel, JTextField step, 
			JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JLabel intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JRadioButton none,
			JList intSpecies, JList species, JLabel spLabel, JLabel speciesLabel,
			JButton addIntSpecies, JButton removeIntSpecies, JTextField rapid1, JTextField rapid2,
			JTextField qssa, JTextField maxCon, JLabel rapidLabel1, JLabel rapidLabel2,
			JLabel qssaLabel, JLabel maxConLabel, JCheckBox usingSSA, JButton clearIntSpecies) {
		if (!usingSSA.isSelected()) {
			ODE.setEnabled(true);
		}
		monteCarlo.setEnabled(true);
		markov.setEnabled(false);
		if (none.isSelected()) {
			intSpecies.setEnabled(false);
			species.setEnabled(false);
			spLabel.setEnabled(false);
			speciesLabel.setEnabled(false);
			addIntSpecies.setEnabled(false);
			removeIntSpecies.setEnabled(false);
			clearIntSpecies.setEnabled(false);
			maxConLabel.setEnabled(false);
			maxCon.setEnabled(false);
			qssaLabel.setEnabled(false);
			qssa.setEnabled(false);
			rapidLabel1.setEnabled(false);
			rapid1.setEnabled(false);
			rapidLabel2.setEnabled(false);
			rapid2.setEnabled(false);
		} else {
			intSpecies.setEnabled(true);
			species.setEnabled(true);
			spLabel.setEnabled(true);
			speciesLabel.setEnabled(true);
			addIntSpecies.setEnabled(true);
			removeIntSpecies.setEnabled(true);
			clearIntSpecies.setEnabled(true);
			maxConLabel.setEnabled(true);
			maxCon.setEnabled(true);
			qssaLabel.setEnabled(true);
			qssa.setEnabled(true);
			rapidLabel1.setEnabled(true);
			rapid1.setEnabled(true);
			rapidLabel2.setEnabled(true);
			rapid2.setEnabled(true);
		}
		if (markov.isSelected()) {
			if (!usingSSA.isSelected()) {
				ODE.setSelected(true);
			} else {
				monteCarlo.setSelected(true);
			}
			monteCarlo.setSelected(false);
			markov.setSelected(false);
			seed.setEnabled(false);
			seedLabel.setEnabled(false);
			runs.setEnabled(false);
			runsLabel.setEnabled(false);
			stepLabel.setEnabled(true);
			step.setEnabled(true);
			errorLabel.setEnabled(false);
			absErr.setEnabled(false);
			limitLabel.setEnabled(true);
			limit.setEnabled(true);
			intervalLabel.setEnabled(true);
			interval.setEnabled(true);
			if (!usingSSA.isSelected()) {
				simulators.setEnabled(true);
				simulatorsLabel.setEnabled(true);
				explanation.setEnabled(true);
				description.setEnabled(true);
			}
			simulators.removeAllItems();
			simulators.addItem("euler");
			simulators.addItem("gear1");
			simulators.addItem("gear2");
			simulators.addItem("rk4imp");
			simulators.addItem("rk8pd");
			simulators.addItem("rkf45");
			simulators.setSelectedItem("rkf45");
		}
	}

	/**
	 * This static method enables and disables the required fields for nary.
	 */
	public static void enableNary(JRadioButton ODE, JRadioButton monteCarlo, JRadioButton markov,
			JTextField seed, JLabel seedLabel, JTextField runs, JLabel runsLabel, JLabel stepLabel,
			JTextField step, JLabel errorLabel, JTextField absErr, 
                        JLabel limitLabel, JTextField limit, JLabel intervalLabel,
			JTextField interval, JComboBox simulators, JLabel simulatorsLabel, JLabel explanation,
			JLabel description, JList intSpecies, JList species, JLabel spLabel,
			JLabel speciesLabel, JButton addIntSpecies, JButton removeIntSpecies,
			JTextField rapid1, JTextField rapid2, JTextField qssa, JTextField maxCon,
			JLabel rapidLabel1, JLabel rapidLabel2, JLabel qssaLabel, JLabel maxConLabel,
			JCheckBox usingSSA, JButton clearIntSpecies) {
		ODE.setEnabled(false);
		monteCarlo.setEnabled(true);
		if (!usingSSA.isSelected()) {
			markov.setEnabled(true);
		}
		intSpecies.setEnabled(true);
		species.setEnabled(true);
		spLabel.setEnabled(true);
		speciesLabel.setEnabled(true);
		addIntSpecies.setEnabled(true);
		removeIntSpecies.setEnabled(true);
		clearIntSpecies.setEnabled(true);
		maxConLabel.setEnabled(true);
		maxCon.setEnabled(true);
		qssaLabel.setEnabled(true);
		qssa.setEnabled(true);
		rapidLabel1.setEnabled(true);
		rapid1.setEnabled(true);
		rapidLabel2.setEnabled(true);
		rapid2.setEnabled(true);
		if (ODE.isSelected()) {
			ODE.setSelected(false);
			monteCarlo.setSelected(true);
			markov.setSelected(false);
			seed.setEnabled(true);
			seedLabel.setEnabled(true);
			runs.setEnabled(true);
			runsLabel.setEnabled(true);
			stepLabel.setEnabled(false);
			step.setEnabled(false);
			errorLabel.setEnabled(false);
			absErr.setEnabled(false);
			limitLabel.setEnabled(true);
			limit.setEnabled(true);
			intervalLabel.setEnabled(true);
			interval.setEnabled(true);
			if (!usingSSA.isSelected()) {
				simulators.setEnabled(true);
				simulatorsLabel.setEnabled(true);
				explanation.setEnabled(true);
				description.setEnabled(true);
			}
			simulators.removeAllItems();
			simulators.addItem("gillespie");
			simulators.addItem("emc-sim");
			simulators.addItem("bunker");
			simulators.addItem("nmc");
		}
	}

	/**
	 * This static method enables and disables the required fields for ODE.
	 */
	public static void enableODE(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel stepLabel, JTextField step, 
                        JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JLabel intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA) {
		seed.setEnabled(false);
		seedLabel.setEnabled(false);
		runs.setEnabled(false);
		runsLabel.setEnabled(false);
		stepLabel.setEnabled(true);
		step.setEnabled(true);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(true);
		limit.setEnabled(true);
		intervalLabel.setEnabled(true);
		interval.setEnabled(true);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		simulators.addItem("euler");
		simulators.addItem("gear1");
		simulators.addItem("gear2");
		simulators.addItem("rk4imp");
		simulators.addItem("rk8pd");
		simulators.addItem("rkf45");
		simulators.setSelectedItem("rkf45");
	}

	/**
	 * This static method enables and disables the required fields for Monte
	 * Carlo.
	 */
	public static void enableMonteCarlo(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel stepLabel, JTextField step, 
                        JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JLabel intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA) {
		seed.setEnabled(true);
		seedLabel.setEnabled(true);
		runs.setEnabled(true);
		runsLabel.setEnabled(true);
		stepLabel.setEnabled(false);
		step.setEnabled(false);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(true);
		limit.setEnabled(true);
		intervalLabel.setEnabled(true);
		interval.setEnabled(true);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		simulators.addItem("gillespie");
		simulators.addItem("emc-sim");
		simulators.addItem("bunker");
		simulators.addItem("nmc");
	}

	/**
	 * This static method enables and disables the required fields for Markov.
	 */
	public static void enableMarkov(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel stepLabel, JTextField step, 
                        JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JLabel intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description, JCheckBox usingSSA) {
		seed.setEnabled(false);
		seedLabel.setEnabled(false);
		runs.setEnabled(false);
		runsLabel.setEnabled(false);
		stepLabel.setEnabled(false);
		step.setEnabled(false);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(false);
		limit.setEnabled(false);
		intervalLabel.setEnabled(false);
		interval.setEnabled(false);
		if (!usingSSA.isSelected()) {
			simulators.setEnabled(true);
			simulatorsLabel.setEnabled(true);
			explanation.setEnabled(true);
			description.setEnabled(true);
		}
		simulators.removeAllItems();
		simulators.addItem("ctmc-transient");
	}

	/**
	 * This static method enables and disables the required fields for sbml,
	 * dot, and xhtml.
	 */
	public static void enableSbmlDotAndXhtml(JTextField seed, JLabel seedLabel, JTextField runs,
			JLabel runsLabel, JLabel stepLabel, JTextField step, 
                        JLabel errorLabel, JTextField absErr, JLabel limitLabel,
			JTextField limit, JLabel intervalLabel, JTextField interval, JComboBox simulators,
			JLabel simulatorsLabel, JLabel explanation, JLabel description) {
		seed.setEnabled(false);
		seedLabel.setEnabled(false);
		runs.setEnabled(false);
		runsLabel.setEnabled(false);
		stepLabel.setEnabled(false);
		step.setEnabled(false);
		errorLabel.setEnabled(false);
		absErr.setEnabled(false);
		limitLabel.setEnabled(false);
		limit.setEnabled(false);
		intervalLabel.setEnabled(false);
		interval.setEnabled(false);
		simulators.setEnabled(false);
		simulatorsLabel.setEnabled(false);
		explanation.setEnabled(false);
		description.setEnabled(false);
	}
}
