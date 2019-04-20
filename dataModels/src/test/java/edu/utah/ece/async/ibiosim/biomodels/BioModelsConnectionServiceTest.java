package edu.utah.ece.async.ibiosim.biomodels;

import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels.BioModelsConnectionService;
import edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels.CuratedModelsResponse;
import edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels.ModelFileResponse;
import edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels.ModelResponse;
import edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels.ModelSummary;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk
 */
public class BioModelsConnectionServiceTest {
    private BioModelsConnectionService service;

    @Before
    public void setUp() {
        service = new BioModelsConnectionService(HttpClients.createMinimal());
    }

    @After
    public void tearDown() throws Exception {
        Optional.ofNullable(service).ifPresent(BioModelsConnectionService::close);
    }

    @Test
    public void shouldRetrieveAModel() {
        String expected = "Model id MODEL6615119181 (Kholodenko2000 - " +
                "Ultrasensitivity and negative feedback bring oscillations in MAPK cascade)";
        ModelResponse response = service.getModel("MODEL6615119181");

        assertEquals("the correct model is retrieved", expected, response.toString());
    }

    @Test
    public void shouldGetCuratedModelSet() {
        CuratedModelsResponse response = service.getCuratedModelSet();
        assertNotNull("the response object is defined", response);
        assertTrue("Have at least 700 models", response.getMatches() > 700);
        assertTrue(response.getModels().size() == response.getMatches());

        String biomd738 = "BIOMD0000000738 Mouse Iron Distribution - Rich iron diet (No Tracer)";
        Optional<ModelSummary> match = response.getModelsStream()
                .filter(summary -> summary.toString().equals(biomd738))
                .findAny();
        assertTrue("BIOMD0000000738 is in the search results", match.isPresent());

        /*response.getModelsStream()
                .map(ModelSummary::getId)
                .forEachOrdered(System.out::println);*/
    }

    @Test
    public void shouldRetrieveModelFileNameForAKnownModel() {
        String name = service.getModelFileName("BIOMD0000000044");
        assertEquals("BIOMD0000000044_url.xml is the model file name", "BIOMD0000000044_url.xml", name);
    }

    @Test
    public void shouldRetrieveModelFileContents() {
        ModelFileResponse response = service.getModelFile("BIOMD0000000044");

        assertNotNull(response);
        String content = response.getFileContent();
        assertNotNull(content);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenRetrievingFilesForUndefinedModel() {
        service.getModelFileName(null);
    }
}