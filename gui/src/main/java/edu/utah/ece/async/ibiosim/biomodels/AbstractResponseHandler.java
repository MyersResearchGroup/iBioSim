package edu.utah.ece.async.ibiosim.biomodels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

    @Override
    public T handleResponse(HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() == 307) {
            //noinspection unused
            final Header location = response.getFirstHeader("Location");
            // TODO follow the redirection if location is not null
        }
        return parseResponse(statusLine, entity, getObjectMappingClass());
    }

    protected abstract Class<T> getObjectMappingClass();

    protected T parseResponse(StatusLine statusLine, HttpEntity entity, Class<T> pojoClass)
            throws IOException {
        if (statusLine.getStatusCode() >= 400) {
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
        ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset() != null ? contentType.getCharset() :
                StandardCharsets.UTF_8;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(entity.getContent(), charset))) {
            return unmarshallContent(reader, pojoClass);
        }
    }

    protected T unmarshallContent(BufferedReader reader, Class<T> type) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(reader, type);
    }
}
