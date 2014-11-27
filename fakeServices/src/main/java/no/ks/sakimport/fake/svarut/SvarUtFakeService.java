package no.ks.sakimport.fake.svarut;

import no.ks.sakimport.fake.AuthorizationUser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

public class SvarUtFakeService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger log = LoggerFactory.getLogger(SvarUtFakeService.class);
    private String gyldigBruker = "gyldigBruker";
    private String gyldigPassord = "EtGyldigPassord";

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setContentType("text/plain; charset=utf-8");

        System.out.println(request);
        System.out.println("auth " + request.getHeader("Authorization"));
        AuthorizationUser authInfo = getAuthorizationUser(request.getHeader("Authorization"));
        if (authInfo == null) {
            response.setHeader("WWW-Authenticate", "BASIC realm=\"KS Svarut mottaker login\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        String username = authInfo.getUsername();
        String password = authInfo.getPassword();

        if (username.equals(gyldigBruker) && password.equals(gyldigPassord)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("SvarUtForsendelse.response"), response.getOutputStream());
        }
        else {
            response.setStatus(401);
        }


    }

    private AuthorizationUser getAuthorizationUser(String authHeader) {

        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
                        log.debug("Credentials: " + credentials);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String login = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();
                            return new AuthorizationUser(login, password);
                        } else {
                            log.error("Invalid authentication token");
                        }
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Couldn't retrieve authentication", e);
                    }
                }
            }
        }
        return null;
    }
}
