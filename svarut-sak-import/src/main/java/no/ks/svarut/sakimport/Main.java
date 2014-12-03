package no.ks.svarut.sakimport;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.ks.svarut.sakimport.GI.Saksimporter;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String... args) throws IOException {
        SakImportConfig config = new SakImportConfig(args);
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(config);
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter importer = new Saksimporter();

        for (Forsendelse forsendelse : forsendelser) {
            System.out.println(forsendelse.getId());

            final Fil fil = nedlaster.hentForsendelseFil(forsendelse);

            final Journalpost journalpost = importer.importerJournalPost(forsendelse);
            System.out.println("Journalpost " + journalpost.getJournalpostnummer() + " laget for forsendelse " + forsendelse.getTittel());
            final Dokument dokument = importer.importerDokument(journalpost, forsendelse.getTittel(), fil.getFilename(), fil.getMimetype(), fil.getData(), true, forsendelse, () -> nedlaster.kvitterForsendelse(forsendelse));
            System.out.println("Dokument " + dokument.getDokumentnummer() + " laget for forsendelse " + forsendelse.getTittel());

            System.out.println("Forsendelse " + forsendelse.getTittel() + " ferdig mottat.");
        }
    }
}

