package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.NullSpecies;
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

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p) {
		super(document);
		this.promoter = p;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getActivators()) {
			specie.accept(this);
		}
	}

	@Override
	public void visitNullSpecies(NullSpecies specie) {
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
	}

	@Override
	public void visitDimer(DimerSpecies specie) {
		loadValues(specie);
		kdimer = specie.getKdimer();
		org.sbml.libsbml.Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actName = "";
		if (dimerizationAbstraction) {
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork
					.getMoleParameter((int) dimer)));
			kl.addParameter(Utility.Parameter(dimerString, dimer, "dimensionless"));
			actName = kdimerString + "*" + "(" + specie.getMonomer() + ")^"
					+ dimerString;
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(),
					dimer * coop));
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(actName, coop));
			PrintDimerizationVisitor visitor = new PrintDimerizationVisitor(
					document, specie, kdimer, dimer);
			visitor.run();
		}
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actName = "";
		if (biochemicalAbstraction) {
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), coop));
				names = names + "*" + s.getId();
			}

			kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork
					.getMoleParameter(specie.getInputs().size())));
			actName = kbioString + names;
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(actName, coop));
			PrintBiochemicalVisitor visitor = new PrintBiochemicalVisitor(document,
					specie, kbio);
			visitor.run();
		}
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.addParameter(Utility.Parameter(kactString, kact,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	private void loadValues(SpeciesInterface specie) {
		ArrayList<Reaction> rlist = promoter.getReactionMap().get(specie);
		Reaction r = rlist.get(0);
		if (!r.getType().equals("vee"))
			r = rlist.get(1);
		coop = r.getCoop();
		kact = r.getAct();
		dimer = r. getDimer();
		if (r.isBiochemical()) {
			kbio = r.getKbio();
		}
	}

	/**
	 * Generates a kinetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repMolecule
	 *            repressor molecule
	 * @return
	 */
	private String generateLaw(String specieName, String actMolecule) {
		String law = "kr*" + "Krnap*" + "(" + kactString + "*" + actMolecule + ")" + "^"
				+ coopString + "*RNAP" + "*" + promoter.getId() + "-kr*" + "RNAP_"
				+ promoter.getId() + "_" + specieName;
		return law;
	}

	private String kdimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String kbioString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KBIO_STRING);
	private String coopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String kactString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KACT_STRING);
	private String dimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.MAX_DIMER_STRING);

	private Promoter promoter;

	private double dimer;
	private double kdimer;
	private double kbio;
	private double coop;
	private double kact;
}
