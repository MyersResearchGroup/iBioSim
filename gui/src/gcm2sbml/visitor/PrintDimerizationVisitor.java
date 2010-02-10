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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

public class PrintDimerizationVisitor extends AbstractPrintVisitor {

	public PrintDimerizationVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, double kdimer, double dimer) {
		super(document);
		this.defaultkdimer = kdimer;
		this.species = species;
		this.defaultdimer = dimer;
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
		loadValues(specie.getProperties());
		// Check if we are running abstraction, if not, then don't allow decay
		if (!dimerizationAbstraction) {
			Reaction r = Utility.Reaction("Dimerization_"+specie.getId());
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(), dimer));
			r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
			r.setReversible(true);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork.getMoleTimeParameter((int)dimer)));
			kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.setFormula(kdimerString+"*" + specie.getMonomer().getId() + " ^"+dimer+"-kr*"+specie.getId());
			
			Utility.addReaction(document, r);		
		}
	}

	public void visitBiochemical(BiochemicalSpecies specie) {
	}

	public void visitBaseSpecies(BaseSpecies specie) {
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
	}
	
	private void loadValues(Properties property) {
		kdimer = getProperty(GlobalConstants.KASSOCIATION_STRING, property, defaultkdimer);
		defaultdimer = getProperty(GlobalConstants.MAX_DIMER_STRING, property, defaultdimer);
	}

	private double kdimer = 1;
	private double defaultkdimer = 1;
	private double dimer = 2;
	private double defaultdimer = 2;
	
	private String kdimerString = CompatibilityFixer
	.getSBMLName(GlobalConstants.KASSOCIATION_STRING);


	private Collection<SpeciesInterface> species = null;

}
