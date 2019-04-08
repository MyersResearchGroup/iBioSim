package edu.utah.ece.async.ibiosim.biomodels;

import java.util.List;

@SuppressWarnings("WeakerAccess,unused")
public class ModelFilesResponse {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<RepositoryFile> main;
    private List<RepositoryFile> additional;

    public static final class RepositoryFile {
        private String name;
        private String description;
        private int fileSize;
    }

    public String getMainFileName() {
        return main.get(0).name;
    }
}
