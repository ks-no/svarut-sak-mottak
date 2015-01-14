package no.ks.svarut.sakimport.util;

import java.io.InputStream;

public class FileLoadUtil {

    private FileLoadUtil(){}

    public static InputStream getInputStreamForFileFromClasspath(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

}