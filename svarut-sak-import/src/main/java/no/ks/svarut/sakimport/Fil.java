package no.ks.svarut.sakimport;

public class Fil {
    private final byte[] bytes;
    private final String mimetype;
    private final String filename;

    public Fil(byte[] bytes, String mimetype, String filename) {
        this.bytes = bytes;
        this.mimetype = mimetype;
        this.filename = filename;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getFilename() {
        return filename;
    }
}
