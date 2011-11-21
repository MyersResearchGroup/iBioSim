package biomodel.network;

import biomodel.util.GlobalConstants;
import biomodel.visitor.SpeciesVisitor;

public class DiffusibleConstitutiveSpecies extends AbstractSpecies{
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleConstitutiveSpecies(this);
		
	}
}
