package edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public final class GetModelResponseHandler extends AbstractResponseHandler<ModelResponse> {

    @Override
    protected Class<ModelResponse> getObjectMappingClass() {
        return ModelResponse.class;
    }
}
