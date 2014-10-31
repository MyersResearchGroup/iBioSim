package biomodel.network;

import biomodel.visitor.SpeciesVisitor;

/**
 * This class is used to represent null species.  Null species
 * are a construct created to handle special reactions where
 * production/degradation reactions are not created.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class NullSpecies extends AbstractSpecies {

	public NullSpecies() {
		super();
		setId("Null");
		setStateName("Null");
	}
	
	@Override
	public void accept(SpeciesVisitor visitor) {
	}

}
