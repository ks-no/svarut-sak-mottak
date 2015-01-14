package no.ks.sakimport.fake.ephorte;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EphorteFakeService extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getHeader("SOAPAction").contains("#NyJournalpost")) {
            response.setContentType("text/xml; charset=utf-8");

            String postedData = IOUtils.toString(request.getInputStream());
            if (postedData.contains("sakssekvensnummer>-1")) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("feilSaksnr.xml"), response.getOutputStream());
            } else {
                IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("nyjournalpost.xml"), response.getOutputStream());
            }
            return;
        } else if (request.getHeader("SOAPAction").contains("#NyDokument")) {
            response.setContentType("text/xml; charset=utf-8");
            String postedData = IOUtils.toString(request.getInputStream());
            downloadUrl(getDownloadUrl(postedData));
            downloadUrl(getKvitteringUrl(postedData));
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("nydokument.xml"), response.getOutputStream());
            return;
        }
    }

    private String getKvitteringUrl(String postedData) {
        final int start = postedData.indexOf("Uri>") + 4;
        return postedData.substring(start, postedData.indexOf("<", start));
    }

    private void downloadUrl(String downloadUrl) throws IOException {
        final CloseableHttpClient build = HttpClientBuilder.create().build();
        try (final CloseableHttpResponse execute = build.execute(new HttpGet(downloadUrl))) {
            EntityUtils.consume(execute.getEntity());
        }
    }

    private String getDownloadUrl(String postedData) {
        final int start = postedData.indexOf("uri>") + 4;
        return postedData.substring(start, postedData.indexOf("<", start));
    }
}
