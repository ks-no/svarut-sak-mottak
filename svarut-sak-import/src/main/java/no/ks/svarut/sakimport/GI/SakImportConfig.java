package no.ks.svarut.sakimport.GI;

import no.ks.svarut.sakimport.KommandoParametre;
import no.ks.svarut.sakimport.SvarUtCommandLineParser;
import no.ks.svarut.sakimport.kryptering.Kryptering;
import org.apache.commons.cli.CommandLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
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
import ch.qos.logback.classic.Level;

public class SakImportConfig {
    private static Logger log = LoggerFactory.getLogger(SakImportConfig.class);

    private final String sakBrukernavn;
    private final String sakPassord;
    private final String sakUrl;
    private final String sakImportHostname;
    private final String sakDefaultSaksAar;
    private final String sakDefaultSaksnr;
    private final boolean debug;
    private final String journalStatus;
    private int port;
    private String urlSti;
    private String host;
    private String protokoll;
    private SSLContext sslContext;
    private String svarUtBrukernavn;
    private String svarUtPassord;
    private String privateKeyFil;
    private String sakInnsynUrl;
    private String sakKlientnavn;
    private String sakImportEksternPort;

    public SakImportConfig(String... args) {
        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(args);
        CommandLine cmdLine = parser.parse();
        String propertiesFilsti = settPropertiesFilsti(cmdLine);
        System.out.println(propertiesFilsti);
        final Properties properties = getDefaultProperties(propertiesFilsti);

        svarUtBrukernavn = hentConfig(properties, cmdLine, KommandoParametre.SVARUT_BRUKER);
        svarUtPassord = hentConfig(properties, cmdLine, KommandoParametre.SVARUT_PASSORD);
        String url = hentConfig(properties, cmdLine, KommandoParametre.SVARUT_URL);
        konfigurerSvarUt(url);
        sakBrukernavn = hentConfig(properties, cmdLine, KommandoParametre.SAK_BRUKERNAVN);
        sakPassord = hentConfig(properties, cmdLine, KommandoParametre.SAK_PASSORD);
        sakUrl = hentConfig(properties, cmdLine, KommandoParametre.SAK_URL);
        sakInnsynUrl = hentConfig(properties, cmdLine, KommandoParametre.SAK_INNSYN_URL);
        sakImportHostname = hentConfig(properties, cmdLine, KommandoParametre.SAK_IMPORT_HOSTNAME);
        sakImportEksternPort = hentConfig(properties, cmdLine, KommandoParametre.SAK_IMPORT_EKSTERN_PORT, "9977");
        sakDefaultSaksAar = hentConfig(properties, cmdLine, KommandoParametre.SAK_DEFAULT_SAKSAAR);
        sakDefaultSaksnr = hentConfig(properties, cmdLine, KommandoParametre.SAK_DEFAULT_SAKSNR);
        sakKlientnavn = hentConfig(properties, cmdLine, KommandoParametre.SAK_KLIENTNAVN, "SVARUT");
        privateKeyFil = hentConfig(properties, cmdLine, KommandoParametre.PRIVATE_KEY_FIL);
        journalStatus = hentConfig(properties, cmdLine, KommandoParametre.JOURNAL_STATUS);

        debug = cmdLine.hasOption(KommandoParametre.DEBUG.getValue());

        if(debug) {
            log.info("Setting \"no\" logger to debug");
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("no");
            root.setLevel(Level.DEBUG);
        }
    }

    private String hentConfig(Properties properties, CommandLine cmdLine, KommandoParametre kommandoParametre, String defaultVerdi) {
        String verdi = hentConfig(properties, cmdLine, kommandoParametre);
        if (verdi == null) {
            return defaultVerdi;
        }
        return verdi;
    }

    public String getJournalStatus() {
        return journalStatus;
    }

    public boolean harJournalStatus(){
        return journalStatus != null && !"".equals(journalStatus);
    }

    private String settPropertiesFilsti(CommandLine cmdLine) {
        if (cmdLine.hasOption(KommandoParametre.PROPERTIES_FILSTI.getValue())) {
            return cmdLine.getOptionValue(KommandoParametre.PROPERTIES_FILSTI.getValue());
        }
        return "config.properties";
    }

    private void konfigurerSvarUt(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            log.info("Prøvde å konfigurere SvarUt med url {} : {}", urlStr, e);
        }
        this.port = url.getPort();
        this.urlSti = url.getPath();
        this.host = url.getHost();
        this.protokoll = url.getProtocol();

    }

    public CloseableHttpClient httpClientForSvarUt() {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties();

        AuthScope authScope = new AuthScope(this.host, this.port);
        UsernamePasswordCredentials credentials = getSvarUtHttpCredentials();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, credentials);


        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);


        final CloseableHttpClient closeableHttpClient = settOppHttpKlient(clientBuilder);

        return closeableHttpClient;
    }

    private UsernamePasswordCredentials getSvarUtHttpCredentials() {
        return new UsernamePasswordCredentials(svarUtBrukernavn, svarUtPassord);
    }

    private Properties getDefaultProperties(String propertiesFilsti) {
        Properties properties = new Properties();
        InputStream input;

        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFilsti);
            properties.load(input);

        } catch (FileNotFoundException e) {
            log.info("Fant ikke properties-fil.", e);
        } catch (IOException e) {
            log.info("Klarte ikke lese properties-fil.", e);
        } catch (Exception e) {
            hentPropertiesfilMedSti(propertiesFilsti, properties);
        }
        return properties;
    }

    private void hentPropertiesfilMedSti(String propertiesFilsti, Properties properties) {
        InputStream input;
        try {
            input = new FileInputStream(propertiesFilsti);
            properties.load(input);
        } catch (FileNotFoundException e1) {
            log.info("Kunne ikke finne propertiesfil " + propertiesFilsti, e1);
        } catch (IOException e1) {
            log.info("Kunne ikke lese propertiesfil " + propertiesFilsti, e1);
        } catch (Exception e1) {
            log.info("Kunne ikke hente properties for " + propertiesFilsti, e1);
        }
    }

    public CloseableHttpClient settOppHttpKlient(HttpClientBuilder clientBuilder) {
        if (this.protokoll.equals("http")) {
            return clientBuilder.build();
        }
        try {
            return clientBuilder.build();
        } catch (Exception e) {
            log.info("Prøvde å sette opp httpklient: {}", e);
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


    private String hentConfig(Properties properties, CommandLine cmdLine, KommandoParametre kommandoParameter) {
        String result = properties.getProperty(kommandoParameter.getValue());
        if (cmdLine.hasOption(kommandoParameter.getValue())) {
            result = cmdLine.getOptionValue(kommandoParameter.getValue());
        }
        return result;
    }

    public String getSvarUtHost() {
        if (this.port != -1) {
            return protokoll + "://" + host + ":" + port;
        } else {
            return protokoll + "://" + host;
        }
    }

    public String getSakUrl() {
        return sakUrl;
    }

    public String getSakBrukernavn() {
        return sakBrukernavn;
    }

    public String getSakPassord() {
        return sakPassord;
    }

    public String getSakImportHostname() {
        return sakImportHostname;
    }

    public String getDefaultSaksAar() {
        return sakDefaultSaksAar;
    }

    public String getDefaultSaksnr() {
        return sakDefaultSaksnr;
    }

    public String getSakKlientnavn() {
        return sakKlientnavn;
    }

    public String getPrivateKeyFil() {
        return privateKeyFil;
    }

    public boolean harTilstrekkeligeParametre(CommandLine commandLine, Properties properties) {
        for (KommandoParametre parameter : KommandoParametre.values()) {
            if (parameter != KommandoParametre.HJELP_STR && parameter != KommandoParametre.VERSJON_STR && parameter != KommandoParametre.PROPERTIES_FILSTI && parameter != KommandoParametre.DEBUG && parameter != KommandoParametre.JOURNAL_STATUS) {
                boolean harParameter = harProperty(commandLine, properties, parameter.getValue());
                if (!harParameter) {
                    throw new RuntimeException("Kunne ikke finne parameter " + parameter.getValue() + " fra kommandolinje eller propertiesfil.");
                }
            }
        }
        return true;
    }

    public boolean harProperty(CommandLine commandLine, Properties properties, String propertyNokkel) {
        String propertiesVerdi = properties.getProperty(propertyNokkel);
        return commandLine.hasOption(propertyNokkel) || (propertiesVerdi != null);
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public String toString() {
        return "SakImportConfig{" +
                "sakBrukernavn='" + sakBrukernavn + '\'' +
                ", sakPassord='" + sakPassord + '\'' +
                ", sakUrl='" + sakUrl + '\'' +
                ", sakImportHostname='" + sakImportHostname + '\'' +
                ", sakImportEksternPort='" + sakImportEksternPort + '\'' +
                ", sakDefaultSaksAar='" + sakDefaultSaksAar + '\'' +
                ", sakDefaultSaksnr='" + sakDefaultSaksnr + '\'' +
                ", sakKlientnavn='" + sakKlientnavn + '\'' +
                ", debug=" + debug +
                ", port=" + port +
                ", urlSti='" + urlSti + '\'' +
                ", host='" + host + '\'' +
                ", protokoll='" + protokoll + '\'' +
                ", sslContext=" + sslContext +
                ", svarUtBrukernavn='" + svarUtBrukernavn + '\'' +
                ", svarUtPassord='" + svarUtPassord + '\'' +
                ", privateKeyFil='" + privateKeyFil + '\'' +
                '}';
    }

    public void sjekkConfig() {
        try{
            new Kryptering(this).getPrivateKey();
        } catch (Exception e) {
            log.error("Failed to load private key",e);
            System.exit(1);
        }
    }

    public String getSakInnsynUrl() {
        return sakInnsynUrl;
    }

    public void authenticate(HttpRequestBase request) {
        try {
            request.addHeader(new BasicScheme().authenticate(getSvarUtHttpCredentials(), request, null));
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSakImportEksternPort() {
        return sakImportEksternPort;
    }
}
