package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;

import java.util.Collection;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintBiochemicalVisitor extends AbstractPrintVisitor {

	public PrintBiochemicalVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, double kbio) {
		super(document);
		this.kbio = kbio;
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
		// Check if we are running abstraction, if not, then don't allow decay
		if (!biochemicalAbstraction) {
			double newkf = kbio;
			if (specie.getRateConstant() != -1) {
				newkf = specie.getRateConstant();
			}
			Reaction r = new Reaction("Biochemical_" + specie.getName());
			String multi = "kbio*";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(new SpeciesReference(s.getName(), 1));
				multi = multi + s.getName() + "*";
			}
			multi = multi.substring(0, multi.length() - 1);

			r.addProduct(new SpeciesReference(specie.getName(), 1));
			r.setReversible(true);
			r.setFast(true);
			KineticLaw kl = new KineticLaw();
			kl.addParameter(new Parameter("kbio", newkf, GeneticNetwork.getMoleTimeParameter(specie.getInputs().size())));
			kl.addParameter(new Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.setFormula(multi + "-kr*" + specie.getName());

			r.setKineticLaw(kl);
			document.getModel().addReaction(r);
		}
	}

	public void visitBaseSpecies(BaseSpecies specie) {
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
	}

	private double kbio = 1;

	private Collection<SpeciesInterface> species = null;

}
