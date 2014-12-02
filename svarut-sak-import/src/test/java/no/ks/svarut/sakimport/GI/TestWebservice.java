package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Korrespondansepart;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.KorrespondansepartListe;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.SakArkivOppdateringPort;
import no.ks.svarut.sakimport.*;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class TestWebservice {

    @Ignore
    @Test
    public void testOpprettSak() throws Exception {

        Saksimporter importer = new Saksimporter();

        SakArkivOppdateringPort service = importer.createSakarkivService();
        Journalpost generertJournalpost = importer.lagJournalpost("TestHoveddokument");


        KorrespondansepartListe korrespondansepartListe = new KorrespondansepartListe();

        Avsender avsender = new Avsender();
        avsender.setNavn("Avsenders kontaktnavn");
        Korrespondansepart korrespondansepart1 = importer.lagAvsender(avsender);
        korrespondansepartListe.getListe().add(korrespondansepart1);

        Mottaker mottaker = new Mottaker();
        mottaker.setNavn("Mottakers navn");
        Korrespondansepart korrespondansepart = importer.lagMottaker(mottaker);
        korrespondansepartListe.getListe().add(korrespondansepart);

        generertJournalpost.setKorrespondansepart(korrespondansepartListe);

        generertJournalpost.setReferanseEksternNoekkel(importer.lagEksternNoekkel());
        generertJournalpost.setSaksnr(importer.lagSaksnummer());

        Journalpost returnertJournalpost = service.nyJournalpost(generertJournalpost, importer.getArkivKontekst());

        Dokument dokument = importer.lagDokument(returnertJournalpost, "test.pdf", "test.pdf", "applicaton/pdf", IOUtils.toByteArray(FileLoadUtil.loadPdfFromClasspath("small.pdf").getInputStream()), true);

        Dokument returnertDokument = service.nyDokument(dokument, true, importer.getArkivKontekst());

    }

    @Ignore
    @Test
    public void testOpprettSakMedForsendelse() throws Exception {

        Saksimporter importer = new Saksimporter();

        final Forsendelse forsendelse = new Forsendelse();
        forsendelse.setTittel("EnTittelForTesting");
        forsendelse.setAvsender(new Avsender());
        forsendelse.getAvsender().setNavn("AvsenderNavn");
        forsendelse.getAvsender().setPostnr("5003");
        forsendelse.getAvsender().setPoststed("Bergen");
        forsendelse.getAvsender().setAdresselinje1("Bergen");
        forsendelse.getAvsender().setAdresselinje2("Bergen");
        forsendelse.getAvsender().setAdresselinje3("Bergen");

        forsendelse.setMottaker(new Mottaker());
        forsendelse.getMottaker().setPostnr("5002");
        forsendelse.getMottaker().setPoststed("Loddefjord");
        forsendelse.getMottaker().setAdresse1("Loddefjord A1");
        forsendelse.getMottaker().setNavn("Mottakernavn");
        final Journalpost journalpost = importer.importerJournalPost(forsendelse);

        final byte[] dokumentData = IOUtils.toByteArray(FileLoadUtil.loadPdfFromClasspath("small.pdf").getInputStream());

        final Dokument dokument1 = importer.importerDokument(journalpost, "test.pdf", "test.pdf", "application/pdf", dokumentData, true);

    }


    @Test
    @Ignore
    public void testKanImportereForsendelse() {
        String brukernavn = "gyldigBruker";
        String passord = "EtGyldigPassord";
        String url = "http://localhost:8102/tjenester/svarut";

        String[] args = new String[6];
        args[0] = "-username";
        args[1] = brukernavn;
        args[2] = "-password";
        args[3] = passord;
        args[4] = "-url";
        args[5] = url;
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter saksimporter = new Saksimporter();

        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());
            //saksimporter.importerJournalPost(forsendelse);
        }
    }
}

