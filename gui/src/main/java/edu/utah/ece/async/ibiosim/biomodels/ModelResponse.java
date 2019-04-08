package edu.utah.ece.async.ibiosim.biomodels;

import java.util.Optional;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public class ModelResponse {
    private String name;
    private String submissionId;
    private Publication publication;

    private class Publication {
        private String link;
    }

    public String getPublicationLink() {
        if (null == publication) return null;

        return publication.link;
    }

    @Override
    public String toString() {
        final StringBuilder response = new StringBuilder();

        response.append("Model id ")
                .append(submissionId)
                .append(" (")
                .append(name)
                .append(")");

        return response.toString();
    }
}
