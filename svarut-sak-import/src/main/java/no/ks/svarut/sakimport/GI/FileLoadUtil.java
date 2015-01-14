package no.ks.svarut.sakimport.GI;

import javax.activation.DataHandler;
import java.io.InputStream;
import java.net.URL;

public class FileLoadUtil {

	private FileLoadUtil(){}

	public static DataHandler loadPdfFromClasspath(String resource){
		URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
		return new DataHandler(url);
	}

	public static InputStream getInputStreamForFileFromClasspath(String resource){
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
	}
}
