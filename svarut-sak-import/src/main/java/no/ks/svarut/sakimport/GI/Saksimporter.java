package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Filreferanse;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.TilknyttetRegistreringSom;
import no.geointegrasjon.rep.arkiv.felles.xml_schema._2012_01.Saksnummer;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.*;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.*;
import no.geointegrasjon.rep.felles.adresse.xml_schema._2012_01.EnkelAdresse;
import no.geointegrasjon.rep.felles.adresse.xml_schema._2012_01.EnkelAdresseListe;
import no.geointegrasjon.rep.felles.adresse.xml_schema._2012_01.PostadministrativeOmraader;
import no.geointegrasjon.rep.felles.kontakt.xml_schema._2012_01.Kontakt;
import no.geointegrasjon.rep.felles.teknisk.xml_schema._2012_01.ArkivKontekst;
import no.ks.svarut.sakimport.Avsender;
import no.ks.svarut.sakimport.Forsendelse;
import no.ks.svarut.sakimport.Mottaker;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.eclipse.jetty.server.Server;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class Saksimporter {


    private final SakImportConfig sakImportConfig;
    private String url;
    private String username;
    private String password;

    private ArkivKontekst arkivKontekst = new ArkivKontekst();
    private SakArkivOppdateringPort service;
    private org.slf4j.Logger log = LoggerFactory.getLogger(Saksimporter.class);

    public Saksimporter(SakImportConfig sakImportConfig) {
        arkivKontekst.setKlientnavn("SVARUT");
        url = sakImportConfig.getSakUrl();
        username = sakImportConfig.getSakBrukernavn();
        password = sakImportConfig.getSakPassord();
        this.sakImportConfig = sakImportConfig;

        service = createSakarkivService();
    }

    public ArkivKontekst getArkivKontekst() {
        return arkivKontekst;
    }

    public Journalpost importerJournalPost(Forsendelse forsendelse) throws ValidationException {
        Journalpost generertJournalpost = lagJournalpost(forsendelse.getTittel());

        fyllInnKorrespondanseparter(forsendelse, generertJournalpost);

        generertJournalpost.setReferanseEksternNoekkel(lagEksternNoekkel());
        generertJournalpost.setSaksnr(lagSaksnummer(forsendelse.getMetadataIMottakendeSystem().getSakssekvensnummer()));

        try {
            return opprettEphorteJournalpost(generertJournalpost, service);
        } catch (ValidationException e) {
            log.info("Klarte ikke å opprette journalpost med saksnr {}, prøver med default saksnummer {}", forsendelse.getMetadataIMottakendeSystem().getSakssekvensnummer(), sakImportConfig.getDefaultSaksnr());
            return opprettEphorteJournalPostMedDefaultSaksnr(generertJournalpost);
        }
    }

    private Journalpost opprettEphorteJournalPostMedDefaultSaksnr(Journalpost generertJournalpost) throws ValidationException {
        Saksnummer defaultSaksnr = new Saksnummer();
        defaultSaksnr.setSakssekvensnummer(new BigInteger(sakImportConfig.getDefaultSaksnr()));
        generertJournalpost.setSaksnr(defaultSaksnr);
        return opprettEphorteJournalpost(generertJournalpost, service);
    }

    public Dokument importerDokument(Journalpost journalpost, String tittel, String filnavn, String mimeType, InputStream dokumentData, boolean hoveddokument, Forsendelse forsendelse, Runnable kvittering) {
        try {
            final Dokument dokument = lagDokument(journalpost, tittel, filnavn, mimeType, hoveddokument, forsendelse.getId());
            final Server server = startHttpServerForFileDownload(filnavn, mimeType, dokumentData, forsendelse.getId(), kvittering);
            log.info("Startet jetty for mottak av forsendelse");
            final Dokument nyDokument = service.nyDokument(dokument, false, getArkivKontekst());
            server.join();
            return nyDokument;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Server startHttpServerForFileDownload(String filnavn, String mimeType, InputStream dokumentData, String forsendelseId, Runnable kvittering) throws Exception {
        Server server = new Server(9977);
        server.setHandler(new DownloadHandler(dokumentData, mimeType, filnavn, forsendelseId, kvittering));
        server.setStopAtShutdown(true);
        server.setStopTimeout(15000);
        server.start();
        return server;
    }

    private Journalpost opprettEphorteJournalpost(Journalpost generertJournalpost, SakArkivOppdateringPort service) throws ValidationException {
        Journalpost returnertJournalpost = null;
        try {
            returnertJournalpost = service.nyJournalpost(generertJournalpost, getArkivKontekst());
        } catch (FinderException e) {
            log.info("Oppretting av journalpost feilet", e);
        } catch (SystemException e) {
            log.info("Oppretting av journalpost feilet", e);
        } catch (ImplementationException e) {
            log.info("Oppretting av journalpost feilet", e);
        } catch (OperationalException e) {
            log.info("Oppretting av journalpost feilet", e);
        } catch (ApplicationException e) {
            log.info("Oppretting av journalpost feilet", e);
        }
        return returnertJournalpost;
    }

    private void fyllInnKorrespondanseparter(Forsendelse forsendelse, Journalpost generertJournalpost) {
        Korrespondansepart avsender = lagAvsender(forsendelse.getAvsender());
        Korrespondansepart mottaker = lagMottaker(forsendelse.getMottaker());

        KorrespondansepartListe korrespondansepartListe = new KorrespondansepartListe();
        korrespondansepartListe.getListe().add(avsender);
        korrespondansepartListe.getListe().add(mottaker);

        generertJournalpost.setKorrespondansepart(korrespondansepartListe);
    }

    Journalpost lagJournalpost(String tittel) {
        Journalpost journalpost = new Journalpost();

        Journalposttype journalPosttype = new Journalposttype();
        journalPosttype.setKodeverdi("I");
        journalpost.setJournalposttype(journalPosttype);

        journalpost.setTittel(tittel);
        return journalpost;
    }


    Korrespondansepart lagAvsender(Avsender avsender) {
        Korrespondansepart avsenderKorrespondent = new Korrespondansepart();

        final Korrespondanseparttype korrespondanseparttype = new Korrespondanseparttype();
        korrespondanseparttype.setKodeverdi("Avsender");
        avsenderKorrespondent.setKorrespondanseparttype(korrespondanseparttype);
        //avsenderKorrespondent.setKortnavn("Bergen kommune");
        final Kontakt kontakt = new Kontakt();
        kontakt.setNavn(avsender.getNavn());
        final EnkelAdresseListe enkelAdresseListe = new EnkelAdresseListe();
        enkelAdresseListe.getListe().add(lagEnkelAdresse(avsender));
        kontakt.setAdresser(enkelAdresseListe);
        avsenderKorrespondent.setKontakt(kontakt);
        return avsenderKorrespondent;
    }

    private EnkelAdresse lagEnkelAdresse(Avsender avsender) {
        final EnkelAdresse result = new EnkelAdresse();
        result.setAdresselinje1(avsender.getAdresselinje1());
        result.setAdresselinje2(avsender.getAdresselinje2());
        if (avsender.getAdresselinje3() == null || "".equals(avsender.getAdresselinje3().trim())) {
            result.setAdresselinje2(result.getAdresselinje2() + " " + avsender.getAdresselinje3());
        }
        result.setPostadresse(new PostadministrativeOmraader());
        result.getPostadresse().setPostnummer(avsender.getPostnr());
        result.getPostadresse().setPoststed(avsender.getPoststed());
        return result;
    }

    private EnkelAdresse lagEnkelAdresse(Mottaker mottaker) {
        final EnkelAdresse result = new EnkelAdresse();
        result.setAdresselinje1(mottaker.getAdresse1());
        result.setAdresselinje2(mottaker.getAdresse2());
        if (mottaker.getAdresse3() == null || "".equals(mottaker.getAdresse3().trim())) {
            result.setAdresselinje2(result.getAdresselinje2() + " " + mottaker.getAdresse3());
        }
        result.setPostadresse(new PostadministrativeOmraader());
        result.getPostadresse().setPostnummer(mottaker.getPostnr());
        result.getPostadresse().setPoststed(mottaker.getPoststed());
        return result;
    }

    Korrespondansepart lagMottaker(Mottaker mottaker) {
        Korrespondansepart mottakerKorrespondent = new Korrespondansepart();
        final Korrespondanseparttype korrespondanseparttype = new Korrespondanseparttype();
        korrespondanseparttype.setKodeverdi("Mottaker");
        mottakerKorrespondent.setKortnavn(mottaker.getNavn());
        Kontakt kontakt = new Kontakt();
        kontakt.setNavn(mottaker.getNavn());
        final EnkelAdresseListe enkelAdresseListe = new EnkelAdresseListe();
        enkelAdresseListe.getListe().add(lagEnkelAdresse(mottaker));
        kontakt.setAdresser(enkelAdresseListe);
        mottakerKorrespondent.setKontakt(kontakt);
        mottakerKorrespondent.setKorrespondanseparttype(korrespondanseparttype);
        return mottakerKorrespondent;
    }

    Dokument lagDokument(Journalpost returnertJournalpost, String tittel, String filnavn, String mimeType, boolean hoveddokument, String forsendelseId) throws IOException {
        final Dokument dokument = new Dokument();
        dokument.setTittel(tittel);

        final Filreferanse filinnhold = new Filreferanse();
        filinnhold.setFilnavn(filnavn);
        filinnhold.setMimeType(mimeType);
        filinnhold.setUri("http://" + sakImportConfig.getSakImportHostname() + ":9977/forsendelse/" + forsendelseId);
        filinnhold.setKvitteringUri("http://" + sakImportConfig.getSakImportHostname() + ":9977/kvitter/" + forsendelseId);
        dokument.setFil(filinnhold);
        final TilknyttetRegistreringSom value = new TilknyttetRegistreringSom();
        if (hoveddokument)
            value.setKodeverdi("H");
        else
            value.setKodeverdi("V");

        dokument.setTilknyttetRegistreringSom(value);
        dokument.setReferanseJournalpostSystemID(returnertJournalpost.getSystemID());
        return dokument;
    }


    EksternNoekkel lagEksternNoekkel() {
        EksternNoekkel eksternNoekkel = new EksternNoekkel();
        eksternNoekkel.setFagsystem("SvarUt");
        eksternNoekkel.setNoekkel("SVARUT");
        return eksternNoekkel;
    }

    Saksnummer lagSaksnummer(int saksnr) {
        Saksnummer saksnummer = new Saksnummer();
        saksnummer.setSaksaar(new BigInteger(sakImportConfig.getDefaultSaksAar()));
        if (saksnr != 0) {
            saksnummer.setSakssekvensnummer(BigInteger.valueOf(saksnr));
        } else {
            saksnummer.setSakssekvensnummer(new BigInteger(sakImportConfig.getDefaultSaksnr()));
        }
        return saksnummer;
    }


    SakArkivOppdateringPort createSakarkivService() {

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SakArkivOppdateringPort.class);
        factory.setAddress(url);
        factory.setUsername(username);
        factory.setPassword(password);
        /*factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());*/
        SakArkivOppdateringPort serviceV3 = (SakArkivOppdateringPort) factory.create();
        Client proxy = ClientProxy.getClient(serviceV3);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(120000);
        httpClientPolicy.setReceiveTimeout(10 * 60 * 1000);
        conduit.setClient(httpClientPolicy);


        return serviceV3;
    }
}
