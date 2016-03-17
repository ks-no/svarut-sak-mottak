package no.ks.svarut.sakimport.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileLoadUtil {

    private FileLoadUtil() {
    }

    public static InputStream getInputStreamForFileOrResource(String resource) throws FileNotFoundException {
        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (resourceAsStream == null)
            return new FileInputStream(resource);
        else
            return resourceAsStream;
    }

}