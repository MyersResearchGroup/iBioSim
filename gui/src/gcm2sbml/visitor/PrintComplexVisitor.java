package gcm2sbml.visitor;

import java.util.Collection;

import gcm2sbml.network.ComplexSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.PartSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species) {
		super(document);
		this.species = species;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface s : species) {
			s.accept(this);
		}
	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		this.kcomp = specie.getKc();
		Reaction r = Utility.Reaction("Complex_" + specie.getId());
		String expression = "";
		for (PartSpecies part : specie.getParts()) {
			SpeciesInterface s = part.getSpecies();
			double n = part.getStoich();
			r.addReactant(Utility.SpeciesReference(s.getId(), n));
			expression = expression + "*" + s.getId();
			if (n > 1)
				expression = expression + '^' + n;
		}
		expression = kcompString + expression;
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
        kl.addParameter(Utility.Parameter(kcompString, kcomp, 
        		GeneticNetwork.getMoleParameter(specie.getSize())));
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
		kl.setFormula("kr*" + expression + "-kr*" + specie.getId());
		Utility.addReaction(document, r);
	}
	
	private double kcomp;
	private String kcompString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KCOMPLEX_STRING);
	private Collection<SpeciesInterface> species;
}
