package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.Reaction;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
import java.util.ArrayList;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;

public class PrintRepressionBindingVisitor extends AbstractPrintVisitor {

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p) {
		super(document);
		this.promoter = p;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getRepressors()) {
			speciesName = "bound_" + promoter.getId() + "_" + specie.getId();
			reactionName = "R_repression_binding_" + promoter.getId() + "_"
					+ specie.getId();

			specie.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitDimer(DimerSpecies specie) {
		loadValues(specie);
		kdimer = specie.getKdimer();
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(krepString, krep,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		String repMolecule = "";
		if (dimerizationAbstraction) {
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork
					.getMoleParameter((int) dimer)));
			kl.addParameter(Utility.Parameter(dimerString, dimer, "dimensionless"));
			repMolecule = kdimerString + "*" + "(" + specie.getMonomer() + ")^"
					+ dimerString;
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(),
					dimer * coop));
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
			PrintDimerizationVisitor visitor = new PrintDimerizationVisitor(
					document, specie, kdimer, dimer);
			visitor.run();
		}
		kl.setFormula(generateLaw(speciesName, repMolecule));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(krepString, krep,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		String repMolecule = "";
		if (biochemicalAbstraction) {
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), coop));
				names = names + "*" + s.getId();
			}

			kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork
					.getMoleParameter(specie.getInputs().size())));
			repMolecule = kbioString + names;
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
			PrintBiochemicalVisitor visitor = new PrintBiochemicalVisitor(document,
					specie, kbio);
			visitor.run();
		}
		kl.setFormula(generateLaw(speciesName, repMolecule));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(krepString, krep,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(krepString, krep,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(krepString, krep,
				GeneticNetwork.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	/**
	 * Generates a kenetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repMolecule
	 *            repressor molecule
	 * @return
	 */
	private String generateLaw(String specieName, String repMolecule) {
		String law = "kr*" + "(" + krepString + "*" + repMolecule + ")" + "^" + coopString + "*"
				+ promoter.getId() + "-kr*" + specieName;
		return law;
	}

	private void loadValues(SpeciesInterface specie) {
		ArrayList<Reaction> rlist = promoter.getReactionMap().get(specie);
		Reaction r = rlist.get(0);
		if (!r.getType().equals("tee"))
			r = rlist.get(1);
		coop = r.getCoop();
		krep = r.getRep();
		dimer = r. getDimer();
		if (r.isBiochemical()) {
			kbio = r.getKbio();
		}
		
	}

	private Promoter promoter;

	private double kdimer;
	private double kbio;
	private double coop;
	private double krep;
	private double dimer;


	private String kdimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String kbioString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KBIO_STRING);
	private String coopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String krepString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KREP_STRING);
	private String dimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.MAX_DIMER_STRING);

	private String speciesName = "";

	private String reactionName = "";
}
