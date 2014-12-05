package no.ks.svarut.sakimport.GI;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadHandler extends AbstractHandler {

    public static ExecutorService es = Executors.newCachedThreadPool();
    private InputStream data;

    private final String mimeType;
    private final String filnavn;
    private String forsendelseId;
    private final Runnable kvittering;
    private org.slf4j.Logger log = LoggerFactory.getLogger(DownloadHandler.class);

    public DownloadHandler(InputStream data, String mimeType, String filnavn, String forsendelseId, Runnable kvittering) {
        this.data = data;
        this.mimeType = mimeType;
        this.filnavn = filnavn;
        this.forsendelseId = forsendelseId;
        this.kvittering = kvittering;
    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
        if (target.equals("/forsendelse/" + forsendelseId)) {
            leverData(response);
        } else if (target.equals("/kvitter/" + forsendelseId)) {
            kvittering(response);
        }
    }

    private void kvittering(HttpServletResponse response) throws IOException {
        if (kvittering != null) kvittering.run();
        response.setStatus(200);
        response.getWriter().println("Kvittering OK");
        response.flushBuffer();
        es.submit( () -> {
            try {
                getServer().stop();
            } catch (Exception e) {}
        });
        log.info("Sak system kvitterte for mottak av forsendelsedokument. {}", forsendelseId);
    }

    private void leverData(HttpServletResponse response) throws IOException {
        response.setContentType(mimeType);
        response.setHeader("Filename", filnavn);
        response.setStatus(HttpServletResponse.SC_OK);
        IOUtils.copy(data, response.getOutputStream());
        response.flushBuffer();
        log.info("Sak system lastet ned forsendelse {}", forsendelseId);
    }

}
