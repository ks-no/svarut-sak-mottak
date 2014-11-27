package no.ks.svarut.sakimport;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import no.ks.svarut.sakimport.util.MySSLSocketFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Forsendelsesnedlaster {

    String host = "localhost";
    String urlSti = "/tjenester/svarinn/mottaker/hentNyeForsendelser";
    int port = 9443;
    String[] args;
    private String protokoll = "https";

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

        DefaultHttpClient httpClient = getDefaultHttpClient(brukernavn, passord);
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

    private List<Forsendelse> hentForsendelser(DefaultHttpClient httpClient) {
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

    private DefaultHttpClient getDefaultHttpClient(String brukernavn, String passord) {
        DefaultHttpClient httpClient = settOppHttpKlientMedSSL();
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(this.host, this.port),
                new UsernamePasswordCredentials(brukernavn, passord));
        return httpClient;
    }

    public DefaultHttpClient settOppHttpKlientMedSSL(){
        if (this.protokoll == "http") {
            return new DefaultHttpClient();
        }
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, port));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
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
}
