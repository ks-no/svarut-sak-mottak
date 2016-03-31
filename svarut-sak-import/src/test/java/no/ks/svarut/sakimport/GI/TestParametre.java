package no.ks.svarut.sakimport.GI;

import no.ks.svarut.sakimport.KommandoParametre;
import no.ks.svarut.sakimport.SvarUtCommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestParametre {

    private String brukernavn;
    private String passord;
    private CommandLine commandLine;
    private Properties properties;
    private SakImportConfig konfig;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(lagArgs());
        commandLine = parser.parse();
        properties = new Properties();
        konfig = new SakImportConfig();
    }

    @Test
    public void testAlleParametreTilstedeGirIkkeFeilmelding() throws Exception {
        assertEquals(true, konfig.harTilstrekkeligeParametre(commandLine, properties));
    }

    @Test
    public void testPropertyPlukkesOppFraKommandolinje() {
        assertEquals(true, konfig.harProperty(commandLine, properties, KommandoParametre.SVARUT_BRUKER.getValue()));
    }

    @Test
    public void testBrukernavnPlukkesOpppFraPropertiesfil() throws Exception {
        String[] args = {"-svarutpassord", passord, "-svaruturl", "http://url.av.no/slag", "-sakurl", "http://en.annen.no/url", "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost", "-saksaar", "2014", "-saksnr", "211"};
        properties.setProperty(KommandoParametre.SVARUT_BRUKER.getValue(), "propertiesBrukernavn");
        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(args);
        commandLine = parser.parse();

        assertEquals(true, konfig.harProperty(commandLine, properties, KommandoParametre.SVARUT_BRUKER.getValue()));
    }

    @Test
    public void testManglendePropertyGirFeilmelding() throws Exception {
        String[] args = {"-svarutpassord", passord, "-svaruturl", "http://url.av.no/slag", "-sakurl", "http://en.annen.no/url", "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost", "-saksaar", "2014", "-saksnr", "211"};
        SvarUtCommandLineParser parser = new SvarUtCommandLineParser(args);
        commandLine = parser.parse();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("svarutbrukernavn");
        konfig.harTilstrekkeligeParametre(commandLine, properties);
    }

    private String[] lagArgs() {
        brukernavn = "gyldigBruker";
        passord = "EtGyldigPassord";
        String url = "http://localhost:8102/tjenester/svarut";
        String sakurl = "http://localhost:8102/EphorteFakeService/service";
        String sakinnsynurl = "http://localhost:8102/EphorteFakeService/innsynservice";

        return new String[]{"-svarutbrukernavn", brukernavn, "-svarutpassord", passord, "-svaruturl", url, "-sakurl", sakurl, "-sakinnsynurl", sakinnsynurl,
                "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost", "-saksaar", "2014",
                "-saksnr", "211", "-privatekeyfil", "sp-key.pem"};
    }

    @Test
    public void testDebugParameter() throws Exception {
        String[] args = {"-svarutpassord", passord, "-svaruturl", "http://url.av.no/slag", "-sakurl", "http://en.annen.no/url", "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost", "-saksaar", "2014", "-saksnr", "211", "-debug"};
        SakImportConfig sakImportConfig = new SakImportConfig(args);
        assertTrue(sakImportConfig.isDebug());
        String[] args2 = {"-svarutpassord", passord, "-svaruturl", "http://url.av.no/slag", "-sakurl", "http://en.annen.no/url", "-sakbrukernavn", "tull", "-sakpassord", "passord", "-hostname", "localhost", "-saksaar", "2014", "-saksnr", "211"};
        sakImportConfig = new SakImportConfig(args2);
        assertFalse(sakImportConfig.isDebug());
    }
}
