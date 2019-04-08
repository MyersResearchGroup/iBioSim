package edu.utah.ece.async.ibiosim.biomodels;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
@SuppressWarnings("unused")
public class CuratedModelsResponse {
    private int matches;
    private Set<ModelSummary> models;

    public CuratedModelsResponse() {
    }

    public CuratedModelsResponse(Set<ModelSummary> models) {
        this.models = models;
        if (null == models) {
            matches = 0;
        } else {
            matches = models.size();
        }
    }

    public Stream<ModelSummary> getModelsStream() {
        return models.parallelStream();
    }

    public int getMatches() {
        return matches;
    }

    public Set<ModelSummary> getModels() {
        return models;
    }
}
