package edu.utah.ece.async.ibiosim.dataModels.ebiBiomodels;

@SuppressWarnings("unused")
public class ModelSummary implements Comparable<ModelSummary> {
    private String id;
    private String name;
    private Publication publication;

    public static class Publication {
        private String link;
    }

    public String getPublicationLink() {
        return null == publication ? null : publication.link;
    }

    public String toString() {
        return id + ' ' + name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(ModelSummary o) {
        return id.compareTo(o.getId());
    }

    public boolean equals(Object o) {
        return (o instanceof ModelSummary) && ((ModelSummary) o).getId().equals(id);
    }
}
