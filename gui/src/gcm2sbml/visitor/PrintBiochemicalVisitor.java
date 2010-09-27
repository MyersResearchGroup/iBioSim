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

public class PrintBiochemicalVisitor extends AbstractPrintVisitor {

	public PrintBiochemicalVisitor(SBMLDocument document,
			SpeciesInterface specie, double kbio) {
		super(document);
		this.kbio = kbio;
		this.specie = specie;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		specie.accept(this);
	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		
		Reaction r = Utility.Reaction("Biochemical_" + specie.getId());
		String multi = "kr*" + kbioString + "*";
		for (SpeciesInterface s : specie.getInputs()) {
			r.addReactant(Utility.SpeciesReference(s.getId(), 1));
			multi = multi + s.getId() + "*";
		}
		multi = multi.substring(0, multi.length() - 1);
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork.getMoleParameter(specie.getInputs().size())));
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
		kl.setFormula(multi + "-kr*" + specie.getId());
		Utility.addReaction(document, r);

	
	}
	
	private double kbio;
	
	private String kbioString = CompatibilityFixer.getSBMLName(GlobalConstants.KBIO_STRING);

	private SpeciesInterface specie;

}
