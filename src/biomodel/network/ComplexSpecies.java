package biomodel.network;


import biomodel.visitor.SpeciesVisitor;

public class ComplexSpecies extends AbstractSpecies {
	

	public ComplexSpecies(SpeciesInterface s) {
		id = s.getId();
		
		name = s.getName();
		
		type = s.getType();
		
		diffusible = s.isDiffusible();
		
		amount = s.getInitialAmount();
		
		concentration = s.getInitialConcentration();
		
//		RBS = s.getRBS();
//		
//		ORF = s.getORF();
		
		Kc = s.getKc();
		
		kd = s.getDecay();
		
		Kmdiff = s.getKmdiff();

		stateName = s.getStateName();
		
		isActivator = s.isActivator();
		
		isRepressor = s.isRepressor();
		
		isAbstractable = s.isAbstractable();
		
		isSequesterAbstractable = s.isSequesterAbstractable();
		
		isSequesterable = s.isSequesterable();
		
		isConvergent = s.isConvergent();
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public ComplexSpecies() {
		super();
	}
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitComplex(this);
	}
	
}