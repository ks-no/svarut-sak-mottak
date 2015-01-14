package no.ks.svarut.sakimport;

import no.ks.svarut.sakimport.GI.SakImportConfig;
import no.ks.svarut.sakimport.kryptering.Kryptering;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Fil implements AutoCloseable{
    private final String mimetype;
    private final String filename;
    private final InputStream inputStream;
    private final byte[] content;
    private HttpEntity entity;
    Kryptering kryptering;


    public Fil(HttpEntity entity, String mimetype, String filename, SakImportConfig config) throws IOException {
        this.entity = entity;
        this.inputStream = entity.getContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        this.content = outputStream.toByteArray();
        this.mimetype = mimetype;
        this.filename = filename;
        this.kryptering = new Kryptering(config);
    }

    public Fil(InputStream zis, String mimetype, String filename) {
        inputStream = zis;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(zis, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.content = outputStream.toByteArray();
        this.mimetype = mimetype;
        this.filename = filename;
    }

    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(kryptering.dekrypterForSvarUt(this.content));
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
