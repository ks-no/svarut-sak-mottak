package no.ks.svarut.sakimport;

import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;

public class Fil {
    private final String mimetype;
    private final String filename;
    private final HttpEntity entity;

    public Fil(HttpEntity entity, String mimetype, String filename) {
        this.entity = entity;
        this.mimetype = mimetype;
        this.filename = filename;
    }

    public InputStream getData() throws IOException {
        return entity.getContent();
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getFilename() {
        return filename;
    }
}
