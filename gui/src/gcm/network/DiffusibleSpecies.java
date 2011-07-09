package gcm.network;

import gcm.util.GlobalConstants;
import gcm.visitor.SpeciesVisitor;

public class DiffusibleSpecies extends AbstractSpecies{
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleSpecies(this);
		
	}
}
