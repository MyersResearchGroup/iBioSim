package dataModels.biomodel.util;

import org.sbml.jsbml.SBMLDocument;

public class Validation implements Runnable {

    private String file;
    private SBMLDocument doc;
    private boolean overdeterminedOnly;

    public Validation(String file, SBMLDocument doc, boolean overdeterminedOnly) {
    	this.file = file;
    	this.doc = doc;
    	this.overdeterminedOnly = overdeterminedOnly;
    }

    public void run() {
        SBMLutilities.check(file, doc, overdeterminedOnly);
    }
}
