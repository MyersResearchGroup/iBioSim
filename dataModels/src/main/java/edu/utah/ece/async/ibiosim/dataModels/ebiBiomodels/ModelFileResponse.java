package edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Mihai Glon\u021b mglont@ebi.ac.uk>
 */
public class ModelFileResponse {
    private String fileContent;

    public static ModelFileResponse parse(BufferedReader reader) {
        ModelFileResponse response = new ModelFileResponse();

        StringWriter stringWriter = new StringWriter(8192);
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                writer.write(currentLine);
                writer.println();
            }
            response.fileContent = stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read model file contents", e);
        }

        return response;
    }

    public String getFileContent() {
        return fileContent;
    }
}
