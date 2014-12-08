package no.ks.svarut.sakimport;

public class FilMetadata {
    private String filnavn;
    private String mimetype;

    public FilMetadata(String filnavn, String mimetype) {
        this.filnavn = filnavn;
        this.mimetype = mimetype;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setFilnavn(String filnavn) {
        this.filnavn = filnavn;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
}

