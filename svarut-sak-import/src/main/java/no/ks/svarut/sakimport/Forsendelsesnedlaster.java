package no.ks.svarut.sakimport;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Forsendelsesnedlaster {

    public static final Logger log = LoggerFactory.getLogger(Forsendelsesnedlaster.class);

    String urlStiNyeForsendelser = "/tjenester/svarinn/mottaker/hentNyeForsendelser";
    String urlStiKvitterMottak = "/tjenester/svarinn/kvitterMottak/forsendelse/";

    private SakImportConfig config;

    public Forsendelsesnedlaster(SakImportConfig config) {
        this.config = config;
    }

    public List<Forsendelse> hentNyeForsendelser() {
        HttpClient httpClient = config.httpClientForSvarUt();
        HttpResponse response = null;

            HttpGet get = new HttpGet(config.getSvarUtHost() + urlStiNyeForsendelser);
            log.info("Henter " + get.getURI());
        try {
            response = httpClient.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.info("Fant ikke tjenestesiden.");
                throw new RuntimeException("Finner ikke tjenestesiden, sjekk at oppgitt url er riktig.");
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED || response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                log.info("Bruker har ikke tilgang til SvarUt eller bruker/passord er feil.");
                throw new RuntimeException("Bruker har ikke tilgang til SvarUt eller bruker/passord er feil.");
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                log.error("SvarUt er ikke tilgjengelig på dette tidspunkt.");
                System.exit(-1);
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.info("Noe gikk galt ved henting av filer.");
                throw new RuntimeException("noe gikk galt");
            }

            String json = EntityUtils.toString(response.getEntity());
            System.out.println("JSon " + json);
            List<Forsendelse> forsendelser = konverterTilObjekt(json);
            return forsendelser;
        } catch (Exception e) {
            log.info("Feil under http get på url: {}{}", config.getSvarUtHost(), urlStiNyeForsendelser);
            throw new RuntimeException("feil under http get på url: " + config.getSvarUtHost() + urlStiNyeForsendelser, e);
        } finally {
            if (response != null)
                EntityUtils.consumeQuietly(response.getEntity());
        }
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


    public Fil hentForsendelseFil(Forsendelse forsendelse) {
        final HttpGet httpGet = new HttpGet(forsendelse.getDownloadUrl());
        final HttpResponse response;
        try {
            response = config.httpClientForSvarUt().execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Klarte ikke å laste ned fil for forsendelse " + forsendelse.getId() + ". http status " + response.getStatusLine().getStatusCode() + " Download url: " + forsendelse.getDownloadUrl());
            }
            final String contentType = response.getEntity().getContentType().getValue();
            String filename = "dokument.pdf";
            final HeaderElement[] elements = response.getFirstHeader("Content-disposition").getElements();
            for (HeaderElement element : elements) {
                if (element.getName().equals("attachment")) {
                    filename = element.getParameterByName("filename").getValue();
                }
            }

            return new Fil(response.getEntity(), contentType, filename);
        } catch (IOException e) {
            throw new RuntimeException("Prøvde uten hell å laste ned " + forsendelse.getDownloadUrl(), e);
        }

    }

    public void kvitterForsendelse(Forsendelse forsendelse) {
        final HttpPost httpPost = new HttpPost(config.getSvarUtHost() + urlStiKvitterMottak + forsendelse.getId());
        try {
            final HttpResponse execute = config.httpClientForSvarUt().execute(httpPost);
            if (execute.getStatusLine().getStatusCode() == HttpStatus.SC_OK) return;
            throw new RuntimeException("Feil status " + execute.getStatusLine().getStatusCode() + " på kvittering av mottat forsendelse");
        } catch (IOException e) {
            log.info("Kvittering for forsendelse {} feilet.", forsendelse.getId(), e);
            throw new RuntimeException(e);
        }
    }
}
