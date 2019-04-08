package edu.utah.ece.async.ibiosim.biomodels;

import org.apache.http.client.methods.HttpGet;

import java.util.Objects;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
@SuppressWarnings("WeakerAccess")
public final class Requests {
    // can also use the Caltech instance: http://biomodels.caltech.edu/
    public static final String BIOMODELS_EBI_BASE = "https://www.ebi.ac.uk/biomodels/";
    public static final String SEARCH_CMD =
            "search?query=curationstatus%3AManually%20curated&sort=id-desc";
    public static final String GET_FILES_CMD = "model/files/";
    public static final String DLD_MODEL_CMD = "model/download/";
    public static final int SEARCH_RESULTS_PER_PAGE = 100;

    public static HttpGet newGetModelRequest(String model) {
        final String url = String.format("%s%s", BIOMODELS_EBI_BASE,
                Objects.requireNonNull(model, "Model identifier (e.g. BIOMD0000000001) required"));
        return constructSignedJsonGetRequest(url);
    }

    public static HttpGet newCuratedModelSearchRequest() {
        return newCuratedModelSearchRequest(0);
    }

    public static HttpGet newCuratedModelSearchRequest(int offset) {
        String url = String.format("%s%s&offset=%d&numResults=%d", BIOMODELS_EBI_BASE,
                SEARCH_CMD, offset, SEARCH_RESULTS_PER_PAGE);
        return constructSignedJsonGetRequest(url);
    }

    public static HttpGet newGetModelFileRequest(String modelId, String fileName) {
        String url = String.format("%s%s%s?filename=%s", BIOMODELS_EBI_BASE, DLD_MODEL_CMD,
                Objects.requireNonNull(modelId, "The model identifier is required"),
                Objects.requireNonNull(fileName, "Model file name is required"));
        return constructSignedJsonGetRequest(url);
    }

    public static HttpGet newGetFilesRequest(String modelId) {
        String getFilesUrl = String.format("%s%s%s", BIOMODELS_EBI_BASE, GET_FILES_CMD,
                Objects.requireNonNull(modelId, "The model identifier is required"));
        return constructSignedJsonGetRequest(getFilesUrl);
    }

    private static HttpGet constructSignedJsonGetRequest(String uri) {
        final HttpGet request = new HttpGet(uri);
        request.setHeader("User-Agent", "iBioSim <https://github.com/MyersResearchGroup/iBioSim>");
        request.setHeader("Accept", "application/json");

        return request;
    }
}
