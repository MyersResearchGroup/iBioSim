package biomodel.network;

import biomodel.visitor.SpeciesVisitor;

public class DiffusibleConstitutiveSpecies extends AbstractSpecies{
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleConstitutiveSpecies(this);
		
	}
}
