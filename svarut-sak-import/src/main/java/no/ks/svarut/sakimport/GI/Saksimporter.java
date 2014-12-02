package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Filinnhold;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Format;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.TilknyttetRegistreringSom;
import no.geointegrasjon.rep.arkiv.felles.xml_schema._2012_01.Saksnummer;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.*;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.*;
import no.geointegrasjon.rep.felles.adresse.xml_schema._2012_01.*;
import no.geointegrasjon.rep.felles.kontakt.xml_schema._2012_01.Kontakt;
import no.geointegrasjon.rep.felles.teknisk.xml_schema._2012_01.ArkivKontekst;
import no.ks.svarut.sakimport.Avsender;
import no.ks.svarut.sakimport.Forsendelse;
import no.ks.svarut.sakimport.Mottaker;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import java.io.IOException;
import java.math.BigInteger;

public class Saksimporter {


    private String url = "http://www.ephorte.com/ephorte5gi/Services/GeoIntegration/V1.1/SakArkivOppdateringService.svc";
    private String username = "GEO-SakArkivOppdatering-SVARUT";
    private String password = "SVARUT42";
    private ArkivKontekst arkivKontekst = new ArkivKontekst();
    private SakArkivOppdateringPort service;

    public Saksimporter() {
        arkivKontekst.setKlientnavn("SVARUT");
        service = createSakarkivService();
    }

    public ArkivKontekst getArkivKontekst() {
        return arkivKontekst;
    }

    public Journalpost importerJournalPost(Forsendelse forsendelse) {
        Journalpost generertJournalpost = lagJournalpost(forsendelse.getTittel());

        fyllInnKorrespondanseparter(forsendelse, generertJournalpost);

        generertJournalpost.setReferanseEksternNoekkel(lagEksternNoekkel());
        generertJournalpost.setSaksnr(lagSaksnummer());

        return opprettEphorteJournalpost(generertJournalpost, service);

    }

    public Dokument importerDokument(Journalpost journalpost, String tittel, String filnavn, String mimeType, byte[] dokumentData, boolean hoveddokument) {
        try {
            final Dokument dokument = lagDokument(journalpost, tittel, filnavn, mimeType, dokumentData, hoveddokument);
            return service.nyDokument(dokument, false, getArkivKontekst());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void sendDokumentTilEphorte(SakArkivOppdateringPort service, Dokument dokument) {
        try {
            Dokument returnertDokument = service.nyDokument(dokument, false, getArkivKontekst());
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (FinderException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (ImplementationException e) {
            e.printStackTrace();
        } catch (OperationalException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
        }
    }

    private Journalpost opprettEphorteJournalpost(Journalpost generertJournalpost, SakArkivOppdateringPort service) {
        Journalpost returnertJournalpost = null;
        try {
            returnertJournalpost = service.nyJournalpost(generertJournalpost, getArkivKontekst());
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (FinderException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (ImplementationException e) {
            e.printStackTrace();
        } catch (OperationalException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
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
        if(avsender.getAdresselinje3() == null || "".equals(avsender.getAdresselinje3().trim())){
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
        if(mottaker.getAdresse3() == null || "".equals(mottaker.getAdresse3().trim())){
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

    Dokument lagDokument(Journalpost returnertJournalpost, String tittel, String filnavn, String mimeType, byte[] dokumentData, boolean hoveddokument) throws IOException {
        final Dokument dokument = new Dokument();
        dokument.setTittel(tittel);

        final Format format = new Format();
        format.setKodeverdi("RA-PDF");
        dokument.setFormat(format); //nødvendig

        final Filinnhold filinnhold = new Filinnhold();
        filinnhold.setFilnavn(filnavn);
        filinnhold.setMimeType(mimeType);
        filinnhold.setBase64(dokumentData);
        dokument.setFil(filinnhold);
        final TilknyttetRegistreringSom value = new TilknyttetRegistreringSom();
        if(hoveddokument)
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

    Saksnummer lagSaksnummer() {
        Saksnummer saksnummer = new Saksnummer();
        saksnummer.setSaksaar(new BigInteger("2014"));
        saksnummer.setSakssekvensnummer(new BigInteger("211"));
        return saksnummer;
    }


    SakArkivOppdateringPort createSakarkivService() {

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SakArkivOppdateringPort.class);
        factory.setAddress(url);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        SakArkivOppdateringPort serviceV3 = (SakArkivOppdateringPort) factory.create();
        Client proxy = ClientProxy.getClient(serviceV3);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(120000);
        httpClientPolicy.setReceiveTimeout(120000);
        conduit.setClient(httpClientPolicy);


        return serviceV3;
    }
}
