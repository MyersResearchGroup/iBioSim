package edu.utah.ece.async.ibiosim.biomodels;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public class CuratedModelsResponseHandler extends AbstractResponseHandler<CuratedModelsResponse> {
    @Override
    protected Class<CuratedModelsResponse> getObjectMappingClass() {
        return CuratedModelsResponse.class;
    }
}
