package no.ks.svarut.sakimport;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.ks.svarut.sakimport.GI.DownloadHandler;
import no.ks.svarut.sakimport.GI.Saksimporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private Main(){}

    public static void main(String... args) throws IOException {
        log.info("Start import av forsendelser");
        SakImportConfig config = new SakImportConfig(args);
        Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(config);
        List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

        Saksimporter importer = new Saksimporter(config);
        log.info("Importerer {} forsendelser", forsendelser != null ? forsendelser.size(): 0);
        for (Forsendelse forsendelse : forsendelser) {
            log.info("Importerer forsendelse {} {}", forsendelse.getTittel(), forsendelse.getId());

            try(final Fil fil = nedlaster.hentForsendelseFil(forsendelse)) {

                final Journalpost journalpost = importer.importerJournalPost(forsendelse);
                log.info("Laget journalpost {} for forsendelse {}", journalpost.getJournalpostnummer(), forsendelse.getId());

                if (fil.getMimetype().contains("application/zip")) {
                    try (final ZipInputStream zis = new ZipInputStream(fil.getData())) {
                        ZipEntry entry;
                        boolean first = true;
                        while ((entry = zis.getNextEntry()) != null) {

                            System.out.println("entry: " + entry.getName() + ", " + entry.getSize());
                            // fikse mimetype
                            final Fil zipfilEntry = new Fil(zis, findMimeType(entry.getName(), forsendelse.getFilmetadata()), entry.getName());
                            final Dokument dokument = importer.importerDokument(journalpost, zipfilEntry.getFilename(), zipfilEntry.getFilename(), zipfilEntry.getMimetype(), zipfilEntry.getData(), first, forsendelse, null);
                            first = false;
                            log.info("Laget dokument {} for forsendelse {}", dokument.getDokumentnummer(), forsendelse.getId());
                            zis.closeEntry();
                        }
                    }
                    nedlaster.kvitterForsendelse(forsendelse);
                } else {
                    final Dokument dokument = importer.importerDokument(journalpost, forsendelse.getTittel(), fil.getFilename(), fil.getMimetype(), fil.getData(), true, forsendelse, () -> nedlaster.kvitterForsendelse(forsendelse));
                    log.info("Laget dokument {} for forsendelse {}", dokument.getDokumentnummer(), forsendelse.getId());
                }
            }
            log.info("Forsendelse {} ferdig mottatt. {}", forsendelse.getTittel(), forsendelse.getId());
        }
        log.info("Importering til sakssystem er ferdig.");
        DownloadHandler.es.shutdown();
    }

    private static String findMimeType(String name, List<FilMetadata> filmetadata) {
        for (FilMetadata filMetadata : filmetadata) {
            if(name.equals(filMetadata.getFilnavn())) return filMetadata.getMimetype();
        }
        return "application/pdf";
    }
}

