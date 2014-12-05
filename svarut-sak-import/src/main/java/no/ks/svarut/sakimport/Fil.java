package no.ks.svarut.sakimport;

import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;

public class Fil implements AutoCloseable{
    private final String mimetype;
    private final String filename;
    private final InputStream inputStream;
    private HttpEntity entity;

    public Fil(HttpEntity entity, String mimetype, String filename) throws IOException {
        this.entity = entity;
        this.inputStream = entity.getContent();
        this.mimetype = mimetype;
        this.filename = filename;
    }

    public Fil(InputStream zis, String mimetype, String filename) {
        inputStream = zis;
        this.mimetype = mimetype;
        this.filename = filename;
    }

    public InputStream getData() throws IOException {
        return inputStream;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void close() {
        try {
            if (entity != null) entity.getContent().close();
            else if (inputStream != null) inputStream.close();
        } catch(Exception e){
            throw  new RuntimeException(e);
        }
    }
}
