package no.ks.svarut.sakimport.util;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MySSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        return super.createSocket(params);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String host, int port, HttpParams params) throws IOException {
        return super.createLayeredSocket(socket, host, port, params);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return super.createLayeredSocket(socket, host, port, autoClose);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}