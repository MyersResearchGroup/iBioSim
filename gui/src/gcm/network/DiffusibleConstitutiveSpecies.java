package gcm.network;

import gcm.util.GlobalConstants;
import gcm.visitor.SpeciesVisitor;

public class DiffusibleConstitutiveSpecies extends AbstractSpecies{
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleConstitutiveSpecies(this);
		
	}
}
