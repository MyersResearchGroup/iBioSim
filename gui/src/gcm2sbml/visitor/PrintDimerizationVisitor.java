package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintDimerizationVisitor extends AbstractPrintVisitor {

	public PrintDimerizationVisitor(SBMLDocument document,
			SpeciesInterface specie, double kdimer, double dimer) {
		super(document);
		this.kdimer = kdimer;
		this.specie = specie;
		this.dimer = dimer;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		specie.accept(this);
	}


	@Override
	public void visitDimer(DimerSpecies specie) {
		
		Reaction r = Utility.Reaction("Dimerization_"+specie.getId());
		r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(), dimer));
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork.getMoleParameter((int)dimer)));
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(dimerString, dimer, "dimensionless"));
		kl.setFormula("kr*" + kdimerString + "*" + specie.getMonomer().getId() +
					"^"+dimerString+"-kr*"+specie.getId());
		Utility.addReaction(document, r);		
	}

	private double kdimer;
	private double dimer;
	
	private String kdimerString = CompatibilityFixer
	.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String dimerString = CompatibilityFixer
	.getSBMLName(GlobalConstants.MAX_DIMER_STRING);


	private SpeciesInterface specie;

}
