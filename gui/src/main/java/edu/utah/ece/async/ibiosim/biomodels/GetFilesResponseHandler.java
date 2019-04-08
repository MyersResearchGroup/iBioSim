package edu.utah.ece.async.ibiosim.biomodels;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public class GetFilesResponseHandler extends AbstractResponseHandler<ModelFilesResponse> {
    @Override
    protected Class<ModelFilesResponse> getObjectMappingClass() {
        return ModelFilesResponse.class;
    }
}
