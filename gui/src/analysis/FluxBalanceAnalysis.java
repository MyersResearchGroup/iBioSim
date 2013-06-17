package analysis;

import main.Gui;

import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.FluxBound;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;

public class FluxBalanceAnalysis {
	
	private String sbmlFileName;
	
	private SBMLDocument sbml;
	
	private FbcModelPlugin fbc;
	
	public FluxBalanceAnalysis(String sbmlFileName) {
		this.sbmlFileName = sbmlFileName;
	}
	
	public int PerformFluxBalanceAnalysis() {
		// Load the SBML file
		sbml = Gui.readSBML(sbmlFileName);
		fbc = (FbcModelPlugin)sbml.getModel().getPlugin("fbc");
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			System.out.println(species.getId());
		}
		for (long i = 0; i < fbc.getNumFluxBounds(); i++) {
			FluxBound bound = fbc.getFluxBound(i);
			System.out.println(bound.getReaction());
		}
		return 0;
	}
	
}
