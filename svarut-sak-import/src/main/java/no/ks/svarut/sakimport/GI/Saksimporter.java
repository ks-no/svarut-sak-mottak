package no.ks.svarut.sakimport.GI;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import no.ks.svarut.sakimport.*;
import no.ks.svarut.sakimport.util.DateTimeUtil;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
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
    private final Innsyn innsyn;
    private String url;
    private String username;
    private String password;

    private ArkivKontekst arkivKontekst = new ArkivKontekst();
    private SakArkivOppdateringPort service;
    private org.slf4j.Logger log = LoggerFactory.getLogger(Saksimporter.class);

    public Saksimporter(SakImportConfig sakImportConfig) {
        arkivKontekst.setKlientnavn(sakImportConfig.getSakKlientnavn());
        url = sakImportConfig.getSakUrl();
        username = sakImportConfig.getSakBrukernavn();
        password = sakImportConfig.getSakPassord();
        this.sakImportConfig = sakImportConfig;

        service = createSakarkivService();
        innsyn = new Innsyn(sakImportConfig, arkivKontekst);
    }

    public ArkivKontekst getArkivKontekst() {
        return arkivKontekst;
    }

    public Journalpost importerJournalPost(Forsendelse forsendelse) throws ValidationException {
        Journalpost generertJournalpost = lagJournalpost(forsendelse.getTittel());

        fyllInnKorrespondanseparter(forsendelse, generertJournalpost);

        settJournalStatus(generertJournalpost);

        generertJournalpost.setReferanseEksternNoekkel(lagEksternNoekkel(forsendelse.getId()));
        final Object o = finnSaksnummer(forsendelse);
        if(o instanceof Saksnummer)
            generertJournalpost.setSaksnr((Saksnummer) o);
        else if (o instanceof String){
            //hack for ephorte
            final SakSystemId value = new SakSystemId();
            final SystemID value1 = new SystemID();
            value1.setId((String) o);
            value.setSystemID(value1);
            generertJournalpost.setReferanseSakSystemID(value);
        }

        NoarkMetadataForImport metadataForImport = forsendelse.getMetadataForImport();
        fyllInnMetadata(generertJournalpost, metadataForImport);
        try {
            return opprettJournalpost(generertJournalpost, service);
        } catch (ValidationException e) {
            log.info("Klarte ikke å opprette journalpost med saksnr {}, prøver med default saksnummer {}", forsendelse.getMetadataForImport().getSakssekvensnummer(), sakImportConfig.getDefaultSaksnr());
            return opprettJournalPostMedDefaultSaksnr(generertJournalpost);
        }
    }

    private void settJournalStatus(Journalpost generertJournalpost) {
        if(sakImportConfig.harJournalStatus()){
            final Journalstatus value = new Journalstatus();
            value.setKodeverdi(sakImportConfig.getJournalStatus());
            generertJournalpost.setJournalstatus(value);
        }
    }

    private Merknad lagMerknadMedMottaker(Forsendelse forsendelse) {
        final Merknad mottaker = new Merknad();
        mottaker.setMerknadstype("SVARUT-MOT");
        mottaker.setMerknadstekst(lagAdresse(forsendelse.getMottaker()));
        return mottaker;
    }

    private String lagAdresse(Mottaker mottaker) {
        String str = "" + mottaker.getOrgnr() + "\n" + mottaker.getNavn() + "\n";
        if (mottaker.getAdresse1() != null && !"".equals(mottaker.getAdresse1())) {
            str += mottaker.getAdresse1() + "\n";
        }
        if (mottaker.getAdresse2() != null && !"".equals(mottaker.getAdresse2())) {
            str += mottaker.getAdresse2() + "\n";
        }
        if (mottaker.getAdresse3() != null && !"".equals(mottaker.getAdresse3())) {
            str += mottaker.getAdresse3() + "\n";
        }
        str += mottaker.getPostnr() + "\n";
        str += mottaker.getPoststed() + "\n";
        if (mottaker.getLand() != null && !"".equals(mottaker.getLand())) {
            str += mottaker.getLand();
        }
        return str;
    }

    private Merknad lagMerknadMedNoarkMetadata(Forsendelse forsendelse) {
        final Merknad noarkMetadata = new Merknad();
        noarkMetadata.setMerknadstype("SVARUT-MET");
        noarkMetadata.setMerknadstekst(lagDeresReferanse(forsendelse.getSvarSendesTil(), forsendelse.getMetadataFraAvleverendeSystem()));
        return noarkMetadata;
    }

    private void fyllInnMetadata(Journalpost generertJournalpost, NoarkMetadataForImport metadataForImport) {
        if (metadataForImport == null) return;
        if (metadataForImport.getDokumentetsDato() != null)
            generertJournalpost.setDokumentetsDato(DateTimeUtil.toGregorianCalendar(metadataForImport.getDokumentetsDato()));

        if (metadataForImport.getJournalposttype() != null && !"".equals(metadataForImport.getJournalposttype().trim())) {
            final Journalposttype value = new Journalposttype();
            value.setKodeverdi(metadataForImport.getJournalposttype());
            generertJournalpost.setJournalposttype(value);
        }
        if (metadataForImport.getJournalstatus() != null && !"".equals(metadataForImport.getJournalstatus().trim())) {
            final Journalstatus value = new Journalstatus();
            value.setKodeverdi(metadataForImport.getJournalstatus());
            generertJournalpost.setJournalstatus(value);
        }
    }

    private Journalpost opprettJournalPostMedDefaultSaksnr(Journalpost generertJournalpost) throws ValidationException {
        Saksnummer defaultSaksnr = new Saksnummer();
        defaultSaksnr.setSakssekvensnummer(new BigInteger(sakImportConfig.getDefaultSaksnr()));
        generertJournalpost.setSaksnr(defaultSaksnr);
        return opprettJournalpost(generertJournalpost, service);
    }

    public Dokument importerDokument(Journalpost journalpost, String tittel, String filnavn, String mimeType, InputStream dokumentData, boolean hoveddokument, Forsendelse forsendelse, Runnable kvittering) {
        Server server = null;
        try {
            final Dokument dokument = lagDokument(journalpost, tittel, filnavn, mimeType, hoveddokument, forsendelse.getId());
            server = startHttpServerForFileDownload(filnavn, mimeType, dokumentData, forsendelse.getId(), kvittering);
            log.info("Startet jetty for mottak av forsendelse");

            final Dokument nyDokument = service.nyDokument(dokument, false, getArkivKontekst());
            server.join();
            return nyDokument;
        } catch (Exception e) {
            if(server != null) try {
                server.stop();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
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

    private Journalpost opprettJournalpost(Journalpost generertJournalpost, SakArkivOppdateringPort service) throws ValidationException {
        Journalpost returnertJournalpost = null;
        try {
            returnertJournalpost = service.nyJournalpost(generertJournalpost, getArkivKontekst());
        } catch (Exception e) {
            log.info("Oppretting av journalpost feilet", e);
            throw new RuntimeException(e);
        }
        return returnertJournalpost;
    }

    private void fyllInnKorrespondanseparter(Forsendelse forsendelse, Journalpost generertJournalpost) {
        Korrespondansepart avsender = lagAvsender(forsendelse.getAvsender(), forsendelse.getSvarSendesTil(), forsendelse.getMetadataFraAvleverendeSystem(), forsendelse.getId());
        //Korrespondansepart mottaker = lagMottaker(forsendelse.getMottaker());

        KorrespondansepartListe korrespondansepartListe = new KorrespondansepartListe();
        korrespondansepartListe.getListe().add(avsender);
        //korrespondansepartListe.getListe().add(mottaker); //Mottaker skal være saksbehandler men vi prøver alikevel

        generertJournalpost.setKorrespondansepart(korrespondansepartListe);
    }

    Journalpost lagJournalpost(String tittel) {
        Journalpost journalpost = new Journalpost();

        Journalposttype journalPosttype = new Journalposttype();
        journalPosttype.setKodeverdi("I");
        journalpost.setJournalposttype(journalPosttype);
        String tittel2 = null;
        if(tittel.length() > 255){
            tittel2 = tittel.substring(0,255);
        } else {
            tittel2 = tittel;
        }
        journalpost.setTittel(tittel2);
        return journalpost;
    }


    Korrespondansepart lagAvsender(Avsender avsender, Mottaker svarSendesTil, NoarkMetadataFraAvleverendeSakssystem noarkMetadataFraAvleverendeSystem, String forsendelseid) {
        Korrespondansepart avsenderKorrespondent = new Korrespondansepart();
        if (svarSendesTil != null && svarSendesTil.getNavn() != null && !"".equals(svarSendesTil.getNavn().trim())) {
            brukSvarSendesTilSomAvsender(forsendelseid, avsenderKorrespondent, svarSendesTil.getNavn(), lagEnkelAdresse(svarSendesTil), svarSendesTil.getOrgnr(), svarSendesTil.getFnr());
        } else {
            brukAvsender(avsender, forsendelseid, avsenderKorrespondent);
        }

        return avsenderKorrespondent;
    }

    private void brukSvarSendesTilSomAvsender(String forsendelseid, Korrespondansepart avsenderKorrespondent, String navn, EnkelAdresse e, String orgnr, String fnr) {
        final Korrespondanseparttype korrespondanseparttype = new Korrespondanseparttype();
        korrespondanseparttype.setKodeverdi("Avsender");
        avsenderKorrespondent.setKorrespondanseparttype(korrespondanseparttype);

        //kommer fram på brevark i ephorte, må finne en bedre måte å legge den til på.
        //avsenderKorrespondent.setDeresReferanse(forsendelseid);
        if (orgnr != null && !"".equals(orgnr))
            avsenderKorrespondent.setKortnavn(orgnr);
        if (fnr != null && !"".equals(fnr))
            avsenderKorrespondent.setKortnavn(fnr);

        final Kontakt kontakt = new Kontakt();
        kontakt.setNavn(navn);

        final EnkelAdresseListe enkelAdresseListe = new EnkelAdresseListe();
        enkelAdresseListe.getListe().add(e);
        kontakt.setAdresser(enkelAdresseListe);
        avsenderKorrespondent.setKontakt(kontakt);
    }

    private void brukAvsender(Avsender avsender, String forsendelseid, Korrespondansepart avsenderKorrespondent) {
        final Korrespondanseparttype korrespondanseparttype = new Korrespondanseparttype();
        korrespondanseparttype.setKodeverdi("Avsender");
        avsenderKorrespondent.setKorrespondanseparttype(korrespondanseparttype);

        avsenderKorrespondent.setDeresReferanse(forsendelseid);
        //avsenderKorrespondent.setKortnavn("Bergen kommune");
        final Kontakt kontakt = new Kontakt();
        kontakt.setNavn(avsender.getNavn());
        final EnkelAdresseListe enkelAdresseListe = new EnkelAdresseListe();
        enkelAdresseListe.getListe().add(lagEnkelAdresse(avsender));
        kontakt.setAdresser(enkelAdresseListe);
        avsenderKorrespondent.setKontakt(kontakt);
    }

    String lagDeresReferanse(Mottaker svarSendesTil, NoarkMetadataFraAvleverendeSakssystem noarkMetadataFraAvleverendeSystem) {
        final DeresReferanse deresReferanse = new DeresReferanse(svarSendesTil, noarkMetadataFraAvleverendeSystem);
        final Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        String s = gson.toJson(deresReferanse);
        if (s.length() > 4000) {
            s = "Metdata lenger enn 4000 tegn sjekk svarut for metadata.";
        }
        return s;
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
        if(filnavn != null && "application/pdf".equals(mimeType) && !filnavn.toUpperCase().endsWith(".PDF")) {
            filnavn += ".pdf";
        }
        filinnhold.setFilnavn(filnavn);
        String nedlastingssti = "http://" + sakImportConfig.getSakImportHostname() + ":" + sakImportConfig.getSakImportEksternPort();
        filinnhold.setMimeType(mimeType);
        filinnhold.setUri(nedlastingssti + "/forsendelse/" + forsendelseId);
        filinnhold.setKvitteringUri(nedlastingssti + "/kvitter/" + forsendelseId);
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


    EksternNoekkel lagEksternNoekkel(String forsendelseid) {
        EksternNoekkel eksternNoekkel = new EksternNoekkel();
        eksternNoekkel.setFagsystem("SvarUt.Korrespondansepart.ConversationId");
        eksternNoekkel.setNoekkel(forsendelseid);
        return eksternNoekkel;
    }

    Object finnSaksnummer(Forsendelse forsendelse) {
        Saksnummer saksnummer = new Saksnummer();
        if (forsendelse.getMetadataForImport() != null && forsendelse.getMetadataForImport().getSakssekvensnummer() != 0 && forsendelse.getMetadataForImport().getSaksaar() != 0) {
            saksnummer.setSaksaar(BigInteger.valueOf(forsendelse.getMetadataForImport().getSaksaar()));
            saksnummer.setSakssekvensnummer(BigInteger.valueOf(forsendelse.getMetadataForImport().getSakssekvensnummer()));
            return saksnummer;
        } else if (forsendelse.getSvarPaForsendelse() != null && !"".equals(forsendelse.getSvarPaForsendelse())) {
            try {
                final JournalpostListe liste = innsyn.sok(forsendelse.getSvarPaForsendelse());
                if (liste.getListe().size() > 1) {
                    log.warn("Fann flere journalposter med id " + forsendelse.getSvarPaForsendelse() + " bruker første resultat");
                }
                if (liste.getListe().size() > 0) {
                    final Journalpost journalpost = liste.getListe().get(0);
                    log.info("fann liste med saker" + journalpost);
                    log.info("fann liste med saksnr" + journalpost.getSaksnr() );
                    log.info("Journalpost" + journalpost.getJournalnummer().getJournalaar() + "/" +  journalpost.getJournalnummer().getJournalsekvensnummer());
                    final Saksnummer saksnr = journalpost.getSaksnr();
                    if(saksnr == null && journalpost.getReferanseSakSystemID() != null && journalpost.getReferanseSakSystemID().getSystemID() != null) {
                        log.info("Saksnr er null, må bruke referanseSakSystemID {}", journalpost.getReferanseSakSystemID().getSystemID().getId());
                        return journalpost.getReferanseSakSystemID().getSystemID().getId();
                    }
                    log.info("Fann saksnr {} for forsendelse {} med svar på {}", saksnr.getSaksaar() + "/" + saksnr.getSakssekvensnummer(), forsendelse.getId(), forsendelse.getSvarPaForsendelse());
                    return saksnr;
                }
                if(liste.getListe().size() == 0){
                    log.info("Fann ikke saksnr for forsendelse {}, bruker fordelingsak", forsendelse.getSvarPaForsendelse());
                }
            } catch (Exception e) {
                log.warn("Klarte ikke å finne sak for forsendelseid " + forsendelse.getSvarPaForsendelse(), e);
            }
        }

        saksnummer.setSaksaar(new BigInteger(sakImportConfig.getDefaultSaksAar()));
        saksnummer.setSakssekvensnummer(new BigInteger(sakImportConfig.getDefaultSaksnr()));
        return saksnummer;
    }


    SakArkivOppdateringPort createSakarkivService() {

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SakArkivOppdateringPort.class);
        factory.setAddress(url);
        factory.setUsername(username);
        factory.setPassword(password);
        log.info("Creating sak arkiv service debug is {}", sakImportConfig.isDebug());
        if (sakImportConfig.isDebug()) {
            log.debug("Adding debug logging for cxf");
            LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
            loggingInInterceptor.setPrettyLogging(true);
            LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
            loggingOutInterceptor.setPrettyLogging(true);
            factory.getInInterceptors().add(loggingInInterceptor);
            factory.getOutInterceptors().add(loggingOutInterceptor);
        }
        SakArkivOppdateringPort serviceV3 = (SakArkivOppdateringPort) factory.create();
        Client proxy = ClientProxy.getClient(serviceV3);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(120000);
        httpClientPolicy.setReceiveTimeout(10 * 60 * 1000);
        conduit.setClient(httpClientPolicy);


        return serviceV3;
    }

    public Journalpost oppdaterEksternNoekkel(Forsendelse forsendelse, Journalnummer journalnummer) throws ApplicationException, ImplementationException, ValidationException, SystemException, FinderException, OperationalException {
        return service.oppdaterJournalpostEksternNoekkel(lagEksternNoekkel(forsendelse.getId()), journalnummer, arkivKontekst);
    }

    public void opprettMerknader(Forsendelse forsendelse, Journalpost journalpost) throws ApplicationException, ImplementationException, ValidationException, SystemException, FinderException, OperationalException {
        final MerknadListe merknader = new MerknadListe();
        merknader.getListe().add(lagMerknadMedNoarkMetadata(forsendelse));
        merknader.getListe().add(lagMerknadMedMottaker(forsendelse));
        service.nyJournalpostMerknad(merknader, journalpost.getJournalnummer(), arkivKontekst);
    }
}
