package no.ks.svarut.sakimport;

import org.apache.commons.cli.CommandLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Properties;

public class SakImportConfig {

    private int port;
    private String urlSti;
    private String host;
    private String protokoll;
    private SSLContext sslContext;
    private String svarUtBrukernavn;
    private String svarUtPassord;
    private HttpClient svarUtHttpClient;

    public SakImportConfig(String... args) {
        final Properties properties = getDefaultProperties();
        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(args);
        CommandLine cmdLine = parser.parse();

        svarUtBrukernavn = settBrukernavn(properties, cmdLine);
        svarUtPassord = settPassord(properties, cmdLine);
        String url = settUrl(properties, cmdLine);
        konfigurerSvarUt(url);

        svarUtHttpClient = getDefaultHttpClient(svarUtBrukernavn, svarUtPassord);
    }

    private void konfigurerSvarUt(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.port = url.getPort();
        this.urlSti = url.getPath();
        this.host = url.getHost();
        this.protokoll = url.getProtocol();

    }

    public HttpClient httpClientForSvarUt(){
        return svarUtHttpClient;
    };

    private Properties getDefaultProperties() {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            properties.load(input);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Fant ikke properties-fil.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Klarte ikke lese properties-fil.");
        }
        return properties;
    }

    private HttpClient getDefaultHttpClient(String brukernavn, String passord) {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        AuthScope authScope = new AuthScope(this.host, this.port);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(brukernavn, passord);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, credentials);

        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return settOppHttpKlient(clientBuilder);

    }

    public CloseableHttpClient settOppHttpKlient(HttpClientBuilder clientBuilder){
        if (this.protokoll.equals("http")) {
            return clientBuilder.build();
        }
        try {
            KeyStore trustStore = lastKeyStore();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
            ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();

            Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", plainsf)
                    .register("https", sslsf)
                    .build();
            HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
            clientBuilder.setConnectionManager(cm);
            return clientBuilder.build();
        } catch (Exception e) {
            return clientBuilder.build();
        }
    }

    private KeyStore lastKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = hentSvarUtSertifikat(cf);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return keyStore;
    }

    private KeyStore hentSvarUtSertifikat(CertificateFactory cf) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("svarut-utvikling-cert.pem");
        Certificate certificate;
        try {
            certificate = cf.generateCertificate(inputStream);
        } finally {
            inputStream.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("svarut-utvikling", certificate);
        return keyStore;
    }


    private String settBrukernavn(Properties properties, CommandLine cmdLine) {
        String brukernavn = properties.getProperty(KommandoParametre.BRUKER_STR.getValue());
        if (cmdLine.hasOption(KommandoParametre.BRUKER_STR.getValue())) {
            brukernavn = cmdLine.getOptionValue(KommandoParametre.BRUKER_STR.getValue());
        }
        return brukernavn;
    }

    private String settPassord(Properties properties, CommandLine cmdLine) {
        String passord = properties.getProperty(KommandoParametre.PASSORD_STR.getValue());
        if (cmdLine.hasOption(KommandoParametre.PASSORD_STR.getValue())) {
            passord = cmdLine.getOptionValue(KommandoParametre.PASSORD_STR.getValue());
        }
        return passord;
    }

    private String settUrl(Properties properties, CommandLine cmdLine) {
        String url = properties.getProperty(KommandoParametre.URL_STR.getValue());
        if (cmdLine.hasOption(KommandoParametre.URL_STR.getValue())) {
            url = cmdLine.getOptionValue(KommandoParametre.URL_STR.getValue());
        }
        return url;
    }

    public String svarUtHost() {
        return protokoll + "://" + host + ":" + port;
    }
}
