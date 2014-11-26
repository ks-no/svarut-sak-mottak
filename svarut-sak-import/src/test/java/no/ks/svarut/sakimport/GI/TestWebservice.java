package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Korrespondansepart;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.KorrespondansepartListe;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.SakArkivOppdateringPort;
import no.ks.svarut.sakimport.Forsendelse;
import no.ks.svarut.sakimport.Forsendelsesnedlaster;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class TestWebservice {

    @Ignore
    @Test
    public void testOpprettSak() throws Exception {

        Saksimporter importer = new Saksimporter();

        SakArkivOppdateringPort service = importer.createSakarkivService();
        Journalpost generertJournalpost = importer.lagJournalpost("En tittel");


        KorrespondansepartListe korrespondansepartListe = new KorrespondansepartListe();

        Korrespondansepart avsender = importer.lagAvsender("Avsenders kontakt navn");
        korrespondansepartListe.getListe().add(avsender);

        Korrespondansepart mottaker = importer.lagMottaker();
        korrespondansepartListe.getListe().add(mottaker);

        generertJournalpost.setKorrespondansepart(korrespondansepartListe);

        generertJournalpost.setReferanseEksternNoekkel(importer.lagEksternNoekkel());
        generertJournalpost.setSaksnr(importer.lagSaksnummer());

        Journalpost returnertJournalpost = service.nyJournalpost(generertJournalpost, importer.getArkivKontekst());

        Dokument dokument = importer.lagDokument(returnertJournalpost);

        Dokument returnertDokument = service.nyDokument(dokument, false, importer.getArkivKontekst());


    }

    @Test
    @Ignore
    public void testKanImportereForsendelse() {
        //Brukernavn og passord fra itest SetUpDataForSakImport
        String brukernavn = "be3627dd-a0ff-455c-892e-642c266308ef";
        String passord = "djEg#j.&qx.EtF9}PIlxqTi~&fJ2TB{KJmWewViUDrcY~JLZ}4";

        String[] args = new String[4];
        args[0] = "-username";
        args[1] = brukernavn;
        args[2] = "-password";
        args[3] = passord;
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(args);
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());
        }
    }
}

