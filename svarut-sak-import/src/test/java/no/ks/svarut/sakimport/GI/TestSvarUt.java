package no.ks.svarut.sakimport.GI;

import no.ks.svarut.sakimport.Forsendelse;
import no.ks.svarut.sakimport.Forsendelsesnedlaster;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSvarUt {


    private String brukernavn;
    private String passord;
    private String url;
    private String[] args = new String[6];

    @Before
    public void setUp() throws Exception {
        brukernavn = "gyldigBruker";
        passord = "EtGyldigPassord";
        url = "http://localhost:8102/tjenester/svarut";
        
        args[0] = "-username";
        args[1] = brukernavn;
        args[2] = "-password";
        args[3] = passord;
        args[4] = "-url";
        args[5] = url;

    }

    @Test
    public void testFeilBrukernavnGirUnauthorized() throws Exception {
        brukernavn = "ikkeGyldigBruker";
        args[1] = brukernavn;

        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(args);
        try {
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        } catch(RuntimeException e) {
            assertTrue(e.getCause().getMessage().contains("tilgang"));
        }
    }

    @Test
    public void testRiktigBrukerOgPassordGirForsendelse() throws Exception {
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(args);
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        assertEquals(1, forsendelser.size());
    }

    @Test
    public void testFeilURLGirForbidden() throws Exception {
        args[5] = "http://localhost:8102/feil/svarut";
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(args);

        try {
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getMessage().contains("url"));
        }


    }
}
