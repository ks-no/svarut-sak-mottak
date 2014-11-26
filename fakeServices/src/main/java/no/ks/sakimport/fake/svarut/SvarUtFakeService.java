package no.ks.sakimport.fake.svarut;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SvarUtFakeService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger logger = LoggerFactory.getLogger(SvarUtFakeService.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=utf-8");

        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("SvarUtForsendelse.response"), response.getOutputStream());
    }

}
