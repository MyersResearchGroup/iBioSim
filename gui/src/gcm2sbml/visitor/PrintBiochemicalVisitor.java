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
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintBiochemicalVisitor extends AbstractPrintVisitor {

	public PrintBiochemicalVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, double kbio) {
		super(document);
		this.defaultkbio = kbio;
		this.species = species;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : species) {
			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		// Check if we are running abstraction, if not, then don't allow decay
		if (!biochemicalAbstraction) {
			double newkf = kbio;
			if (specie.getRateConstant() != -1) {
				newkf = specie.getRateConstant();
			}
			Reaction r = Utility.Reaction("Biochemical_" + specie.getId());
			String multi = kbioString+"*";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), 1));
				multi = multi + s.getId() + "*";
			}
			multi = multi.substring(0, multi.length() - 1);

			r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
			r.setReversible(true);
			r.setFast(true);
			KineticLaw kl = r.createKineticLaw();
			kl.addParameter(Utility.Parameter(kbioString, newkf, GeneticNetwork.getMoleTimeParameter(specie.getInputs().size())));
			kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.setFormula(multi + "-kr*" + specie.getId());

			Utility.addReaction(document, r);
		}
	}

	public void visitBaseSpecies(BaseSpecies specie) {
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
	}
	
	private void loadValues(Properties property) {
		kbio = getProperty(GlobalConstants.KBIO_STRING, property, defaultkbio);
	}
	

	private double kbio = 1;
	private double defaultkbio = 1;
	private String kbioString = CompatibilityFixer.getSBMLName(GlobalConstants.KBIO_STRING);

	private Collection<SpeciesInterface> species = null;

}
