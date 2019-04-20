package edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels;

import java.io.BufferedReader;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public class GetModelFileResponseHandler extends AbstractResponseHandler<ModelFileResponse> {
    @Override
    protected Class<ModelFileResponse> getObjectMappingClass() {
        return ModelFileResponse.class;
    }

    @Override
    protected ModelFileResponse unmarshallContent(BufferedReader reader, Class<ModelFileResponse> type) {
        return ModelFileResponse.parse(reader);
    }
}
