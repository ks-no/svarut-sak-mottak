package no.ks.svarut.sakimport.GI;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.ValidationException;
import no.ks.svarut.sakimport.*;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestWebservice {

    private FakeServicesJettyRunner fakeServicesJettyRunner;

    @Before
    public void setUp() throws Exception {
        fakeServicesJettyRunner = new FakeServicesJettyRunner();
        fakeServicesJettyRunner.start();
        fakeServicesJettyRunner.waitTillRunning();
    }

    @After
    public void tearDown() throws Exception {
        fakeServicesJettyRunner.stop();
    }

    @Test
    @Ignore
    public void testOpprettSakMedForsendelse() throws Exception {

        Saksimporter importer = new Saksimporter(new SakImportConfig(lagArgs()));

        final Forsendelse forsendelse = new Forsendelse();
        forsendelse.setId("forsendelseID");
        forsendelse.setTittel("Prøver 240mb fil");
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

        final Dokument dokument1 = importer.importerDokument(journalpost, "test.pdf", "test.pdf", "application/pdf", FileLoadUtil.loadPdfFromClasspath("ouput240.pdf").getInputStream(), true, forsendelse, null);

    }

    @Test
    public void testHttpServerForFilDownload() throws Exception {
        final Saksimporter saksimporter = new Saksimporter(new SakImportConfig(lagArgs()));
        saksimporter.startHttpServerForFileDownload("filnavn.pdf", "application/pdf", FileLoadUtil.loadPdfFromClasspath("small.pdf").getInputStream(), "forsendelseid", null);
    }

    @Test
    public void testKanImportereForsendelse() throws ValidationException {
        String[] args = lagArgs();
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter saksimporter = new Saksimporter(new SakImportConfig(lagArgs()));

        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());
            saksimporter.importerJournalPost(forsendelse);
        }
    }

    @Test
    @Ignore
    public void testKanDekryptereNedlastetFil() throws Exception {
        String[] args = lagArgs();

        byte[] origPDF = IOUtils.toByteArray(FileLoadUtil.getInputStreamForFileFromClasspath("small.pdf"));

        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        Forsendelse forsendelse = forsendelser.get(0);
        System.out.println(forsendelse.getId());
        Fil fil = nedlaster.hentForsendelseFil(forsendelse);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(fil.getData(), outputStream);
        final byte[] dekrypterteData = outputStream.toByteArray();
        assertEquals(origPDF.length, dekrypterteData.length);
    }

    @Test
    public void testFeilMetadataFraFakeserviceTilEPhorteTest() throws Exception {
        String[] args = lagArgs();

        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter saksimporter = new Saksimporter(new SakImportConfig(args));

        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());
            saksimporter.importerJournalPost(forsendelse);
        }

    }

    private String[] lagArgs() {
        String brukernavn = "gyldigBruker";
        String passord = "EtGyldigPassord";
        String url = "http://localhost:8102/tjenester/svarut";

        String sakurl = "http://localhost:8102/EphorteFakeService/service";
        return new String[]{"-svarutbrukernavn", brukernavn, "-svarutpassord", passord, "-svaruturl", url, "-sakurl",
                sakurl, "-sakbrukernavn", "tull", "-sakpassord", "passord", "-saksnr", "211", "-saksaar", "2014",
                "-privatekeyfil", "sp-key.pem"};
    }
}

