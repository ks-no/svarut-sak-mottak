package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Filinnhold;
import no.geointegrasjon.rep.arkiv.felles.xml_schema._2012_01.Saksnummer;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.*;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.SakArkivOppdateringPort;
import no.geointegrasjon.rep.felles.kontakt.xml_schema._2012_01.Kontakt;
import no.geointegrasjon.rep.felles.teknisk.xml_schema._2012_01.ArkivKontekst;
import no.ks.svarut.sakimport.Forsendelse;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigInteger;

public class Saksimporter {


    private String url = "http://www.ephorte.com/ephorte5gi/Services/GeoIntegration/V1.1/SakArkivOppdateringService.svc";
    private String username = "GEO-SakArkivOppdatering-SVARUT";
    private String password = "SVARUT42";
    private ArkivKontekst arkivKontekst = new ArkivKontekst();

    public Saksimporter() {
        arkivKontekst.setKlientnavn("GEO-SakArkivOppdatering-SVARUT");
    }

    public ArkivKontekst getArkivKontekst() {
        return arkivKontekst;
    }

    public void importer(Forsendelse forsendelse) {
        Journalpost journalpost = lagJournalpost(forsendelse.getTittel());
        //Korrespondansepart avsender = lagAvsender(forsendelse.ge)
    }

    Journalpost lagJournalpost(String tittel) {
        Journalpost journalpost = new Journalpost();

        Journalposttype journalPosttype = new Journalposttype();
        journalPosttype.setKodeverdi("I");
        journalpost.setJournalposttype(journalPosttype);

        journalpost.setTittel(tittel);
        return journalpost;
    }

    Korrespondansepart lagAvsender(String navn) {
        Korrespondansepart avs = new Korrespondansepart();

        final Korrespondanseparttype korrespondanseparttype = new Korrespondanseparttype();
        korrespondanseparttype.setKodeverdi("Avsender");
        avs.setKorrespondanseparttype(korrespondanseparttype);
        //avs.setKortnavn("Bergen kommune");
        final Kontakt kontakt = new Kontakt();
        kontakt.setNavn(navn);
        avs.setKontakt(kontakt);
        return avs;
    }


    Korrespondansepart lagMottaker() {
        Korrespondansepart mott = new Korrespondansepart();
        final Korrespondanseparttype korrespondanseparttype2 = new Korrespondanseparttype();
        korrespondanseparttype2.setKodeverdi("Mottaker");
        mott.setKortnavn("Mottaker navn");
        final Kontakt value2 = new Kontakt();
        value2.setNavn("Mottakers kontakt navn");
        mott.setKontakt(value2);
        mott.setKorrespondanseparttype(korrespondanseparttype2);
        return mott;
    }

    Dokument lagDokument(Journalpost returnertJournalpost) throws IOException {
        final Dokument dokument = new Dokument();
        final Filinnhold value1 = new Filinnhold();
        value1.setFilnavn("test.pdf");
        value1.setMimeType("applicaton/pdf");
        value1.setBase64(IOUtils.toByteArray(FileLoadUtil.loadPdfFromClasspath("small.pdf").getInputStream()));
        dokument.setFil(value1);
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
