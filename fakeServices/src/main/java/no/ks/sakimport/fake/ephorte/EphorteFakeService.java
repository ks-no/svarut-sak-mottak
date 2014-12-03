package no.ks.sakimport.fake.ephorte;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EphorteFakeService extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getHeader("SOAP-Action").contains("#NyJournalpost")){
            resp.setContentType("text/xml; charset=utf-8");
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("nyjournalpost.xml"), resp.getOutputStream());
            return;
        } else if(req.getHeader("SOAP-Action").contains("#NyDokument")) {
            resp.setContentType("text/xml; charset=utf-8");
            String postedData = IOUtils.toString(req.getInputStream());
            downloadUrl(getDownloadUrl(postedData));
            downloadUrl(getKvitteringUrl(postedData));
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("nydokument.xml"), resp.getOutputStream());
            return;
        }
    }

    private String getKvitteringUrl(String postedData) {
        final int start = postedData.indexOf("Uri>");
        return postedData.substring(start, postedData.indexOf("<", start));
    }

    private void downloadUrl(String downloadUrl) {

    }

    private String getDownloadUrl(String postedData) {
        final int start = postedData.indexOf("uri>");
        return postedData.substring(start, postedData.indexOf("<", start));
    }
}
