package no.ks.svarut.sakimport;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.cli.CommandLine;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Forsendelsesnedlaster {

    String host = "localhost";
    String urlSti = "/tjenester/svarinn/mottaker/hentNyeForsendelser";
    int port = 9443;
    String[] args;
    private String protokoll = "https";
    private SSLContext sslContext;

    public Forsendelsesnedlaster(String[] args) {
        this.args = args;
    }

    public List<Forsendelse> hentNyeForsendelser() {


        Properties properties = getDefaultProperties();


        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(args);
        CommandLine cmdLine = parser.parse();

        String brukernavn = settBrukernavn(properties, cmdLine);
        String passord = settPassord(properties, cmdLine);
        String url = settUrl(properties, cmdLine);
        konfigurerSvarUt(url);

        HttpClient httpClient = getDefaultHttpClient(brukernavn, passord);
        return hentForsendelser(httpClient);
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

    private List<Forsendelse> hentForsendelser(HttpClient httpClient) {
        HttpResponse response = null;
        try {
            HttpGet get = new HttpGet(protokoll + "://" + host + ":" + port + urlSti);



            response = httpClient.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new RuntimeException("Finner ikke tjenestesiden, sjekk at oppgitt url er riktig.");
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED || response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                System.out.println("Bruker har ikke tilgang til SvarUt eller bruker/passord er feil.");
                System.out.println(response);
                throw new RuntimeException("Bruker har ikke tilgang til SvarUt eller bruker/passord er feil.");
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                System.out.println("SvarUt er ikke tilgjengelig på dette tidspunkt.");
                System.out.println(response);
                System.exit(-1);
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                System.out.println("Noe gikk galt ved henting av filer.");
                throw new RuntimeException("noe gikk galt");
            }

            String json = EntityUtils.toString(response.getEntity());
            System.out.println("JSon " + json);
            List<Forsendelse> forsendelser = konverterTilObjekt(json);
            return forsendelser;
        } catch (Exception e) {
            throw new RuntimeException("feil under http get", e);
        } finally {
            if (response != null)
                EntityUtils.consumeQuietly(response.getEntity());
        }
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

    private List<Forsendelse> konverterTilObjekt(String result) {
        JsonDeserializer<DateTime> deser = new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) {
                return json == null ? null : new DateTime(json.getAsLong());
            }
        };

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, deser).create();

        Type listType = new TypeToken<ArrayList<Forsendelse>>() {
        }.getType();
        return gson.fromJson(result, listType);
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

    public void hentForsendelseFil(Forsendelse forsendelse) {
        final HttpGet httpGet = new HttpGet(forsendelse.getDownloadUrl());

    }
}
