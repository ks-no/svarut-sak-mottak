package no.ks.svarut.sakimport.GI;

import javax.activation.DataHandler;
import java.net.URL;

public class FileLoadUtil {

	public static DataHandler loadPdfFromClasspath(String resource){
		URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
		return new DataHandler(url);
	}
}
