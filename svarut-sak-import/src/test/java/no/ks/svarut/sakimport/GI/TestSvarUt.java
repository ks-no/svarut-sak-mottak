package no.ks.svarut.sakimport.GI;

import no.ks.svarut.sakimport.Forsendelse;
import no.ks.svarut.sakimport.Forsendelsesnedlaster;
import no.ks.svarut.sakimport.Main;
import no.ks.svarut.sakimport.SakImportConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSvarUt {


    private String brukernavn;
    private String passord;
    private String[] args;
    private FakeServicesJettyRunner fakeServicesJettyRunner;

    private String[] lagArgs() {
        brukernavn = "gyldigBruker";
        passord = "EtGyldigPassord";
        String url = "http://localhost:8102/tjenester/svarut";

        String sakurl = "http://localhost:8102/EphorteFakeService/service";
        return new String[]{"-username", brukernavn, "-password", passord, "-url", url, "-sakurl", sakurl, "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost"};
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("basedir", "/home/idar/work/svarut-sak-import/svarut-sak-import");
        args=lagArgs();
        fakeServicesJettyRunner = new FakeServicesJettyRunner();
        fakeServicesJettyRunner.start();
        fakeServicesJettyRunner.waitTillRunning();
        //fakeServicesJettyRunner.join();
    }

    @After
    public void tearDown() throws Exception {
        fakeServicesJettyRunner.stop();

    }

    @Test
    public void testRiktigBrukerOgPassordGirForsendelse() throws Exception {
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        assertEquals(1, forsendelser.size());
    }

    @Test
    public void testFeilBrukernavnGirUnauthorized() throws Exception {
        brukernavn = "ikkeGyldigBruker";
        args[1] = brukernavn;

        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));
        try {
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        } catch(RuntimeException e) {
            assertTrue(e.getCause().getMessage().contains("tilgang"));
        }
    }

    @Test
    public void testFeilURLGirForbidden() throws Exception {
        args[5] = "http://localhost:8102/feil/svarut";
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(new SakImportConfig(args));

        try {
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getMessage().contains("url"));
        }
    }

    @Test
    public void testNormalRun() throws Exception {
        Main.main(args);
    }
}
