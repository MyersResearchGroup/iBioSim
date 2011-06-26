package gcm.network;

import gcm.util.GlobalConstants;
import gcm.visitor.SpeciesVisitor;

public class DiffusibleSpecies extends AbstractSpecies{
	
	public double getKmdiff() {
		return Double.parseDouble(getProperty(GlobalConstants.MEMDIFF_STRING));
	}
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleSpecies(this);
	}
}
