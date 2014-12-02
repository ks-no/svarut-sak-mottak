package no.ks.svarut.sakimport;

import no.ks.svarut.sakimport.GI.Saksimporter;

import java.util.List;

public class Main {

    public static void main(String... args) {
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(args);
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter importer = new Saksimporter();

        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());

            //importer.importerJournalPost(forsendelse);
            nedlaster.hentForsendelseFil(forsendelse);
        }
    }
}

