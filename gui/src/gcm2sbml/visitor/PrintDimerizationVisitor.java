package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.GlobalConstants;

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
			Collection<SpeciesInterface> species, double kdimer) {
		super(document);
		this.defaultkdimer = kdimer;
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
		loadValues(specie.getProperties());
		// Check if we are running abstraction, if not, then don't allow decay
		if (!dimerizationAbstraction) {
			double newkf = kdimer;
			if (!Double.isNaN(specie.getDimerizationConstant())) {
				newkf = specie.getDimerizationConstant();
			}
			Reaction r = new Reaction("Dimerization_"+specie.getName());
			r.addReactant(new SpeciesReference(specie.getMonomer().getName(), specie.getDimerizationValue()));
			r.addProduct(new SpeciesReference(specie.getName(), 1));
			r.setReversible(true);
			r.setFast(true);
			KineticLaw kl = new KineticLaw();
			kl.addParameter(new Parameter("kdimer", kdimer, GeneticNetwork.getMoleTimeParameter(specie.getDimerizationValue())));
			kl.addParameter(new Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.setFormula("kdimer*" + specie.getMonomer().getName() + " ^"+specie.getDimerizationValue()+"-kr*"+specie.getName());
			
			r.setKineticLaw(kl);
			document.getModel().addReaction(r);		
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
	}

	private double kdimer = 1;
	private double defaultkdimer = 1;

	private Collection<SpeciesInterface> species = null;

}
