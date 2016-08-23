package no.ks.svarut.sakimport.GI;


import no.ks.svarut.sakimport.Mottaker;
import no.ks.svarut.sakimport.NoarkMetadataFraAvleverendeSakssystem;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestSaksimporter {

    @Test
    public void testLagDeresReferanse() throws Exception {
        final Saksimporter saksimporter = new Saksimporter(new SakImportConfig());
        final NoarkMetadataFraAvleverendeSakssystem noarkMetadataFraAvleverendeSystem = new NoarkMetadataFraAvleverendeSakssystem();
        noarkMetadataFraAvleverendeSystem.setJournaldato(DateTime.parse("2016-08-23T11:58:28.572+02:00"));
        final String s = saksimporter.lagDeresReferanse(new Mottaker(), noarkMetadataFraAvleverendeSystem);
        assertTrue(s.contains("2016-08-23T11:58:28.572+02:00"));

    }
}
