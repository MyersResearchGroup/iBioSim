package edu.utah.ece.async.ibiosim.biomodels;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.utah.ece.async.ibiosim.biomodels.Requests.SEARCH_RESULTS_PER_PAGE;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
@SuppressWarnings("WeakerAccess")
public class BioModelsConnectionService implements AutoCloseable {
    private final CloseableHttpClient client;

    public BioModelsConnectionService() {
        this(HttpClients.createDefault());
    }

    public BioModelsConnectionService(CloseableHttpClient client) {
        this.client = Objects.requireNonNull(client, "The HttpClient cannot be null");
    }

    public ModelResponse getModel(String m) {
        final HttpGet request = Requests.newGetModelRequest(m);
        return performRequest(request, new GetModelResponseHandler());
    }

    public CuratedModelsResponse getCuratedModelSet() {
        final HttpGet request = Requests.newCuratedModelSearchRequest();
        CuratedModelsResponse firstPage = performRequest(request, new CuratedModelsResponseHandler());
        if (null == firstPage) return null;

        return doGetAllCuratedModels(firstPage);
    }

    public String getModelFileName(String model) {
        HttpGet filesRequest = Requests.newGetFilesRequest(model);

        ModelFilesResponse files = performRequest(filesRequest, new GetFilesResponseHandler());
        if (null == files) return null;

        return files.getMainFileName();
    }

    public ModelFileResponse getModelFile(String model) {
        String mainFileName = getModelFileName(model);
        if (mainFileName == null) return null;
        HttpGet modelFileRequest = Requests.newGetModelFileRequest(model, mainFileName);
        return performRequest(modelFileRequest, new GetModelFileResponseHandler());
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            System.err.printf("Could not close the HttpClient instance: %s%n", e);
        }
    }

    private CuratedModelsResponse doGetAllCuratedModels(CuratedModelsResponse firstPage) {
        int total = firstPage.getMatches();
        int pageCount = (int) Math.ceil(((double) total) / SEARCH_RESULTS_PER_PAGE);
        Stream<ModelSummary> remainder = IntStream.range(1, pageCount)
                .mapToObj(this::getSearchResultsPage)
                .filter(p -> p != null) // requests that threw exceptions return null
                .flatMap(CuratedModelsResponse::getModelsStream);

        Set<ModelSummary> responses = Stream.concat(firstPage.getModelsStream(), remainder)
                .collect(Collectors.toCollection(TreeSet::new));

        return new CuratedModelsResponse(responses);
    }

    private <T> T performRequest(HttpGet request, AbstractResponseHandler<T> responseHandler) {
        try {
            return client.execute(request, responseHandler);
        } catch (IOException e) {
            System.err.printf("I/O exception encountered while performing request %s: %s%n",
                    request.getURI().toString(), e);
        }
        return null;
    }

    private CuratedModelsResponse getSearchResultsPage(int page) {
        if (page < 1) throw new IllegalArgumentException(String.valueOf(page));

        final int offset = page * SEARCH_RESULTS_PER_PAGE;
        HttpGet request = Requests.newCuratedModelSearchRequest(offset);
        return performRequest(request, new CuratedModelsResponseHandler());
    }
}
