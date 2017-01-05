package backend.biomodel.network;

import backend.biomodel.visitor.SpeciesVisitor;

public class DiffusibleConstitutiveSpecies extends AbstractSpecies{
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleConstitutiveSpecies(this);
		
	}
}
