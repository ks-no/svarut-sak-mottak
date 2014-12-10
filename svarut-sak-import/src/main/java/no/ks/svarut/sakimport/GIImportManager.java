package no.ks.svarut.sakimport;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.ks.svarut.sakimport.GI.DownloadHandler;
import no.ks.svarut.sakimport.GI.SakImportConfig;
import no.ks.svarut.sakimport.GI.Saksimporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GIImportManager {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private static Logger forsendelserOKlog = LoggerFactory.getLogger("forsendelserOK");
    private static Logger forsendelserFeiletlog = LoggerFactory.getLogger("forsendelserFeilet");
    private final String[] args;

    public GIImportManager(String... args) {
        this.args = args;
    }

    public void importerNyeForsendelser() {
        log.info("Start import av forsendelser");
        try {
            SakImportConfig config = new SakImportConfig(args);
            Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(config);
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

            Saksimporter importer = new Saksimporter(config);
            log.info("Importerer {} forsendelser", forsendelser != null ? forsendelser.size() : 0);
            for (Forsendelse forsendelse : forsendelser) {
                log.info("Importerer forsendelse {} {}", forsendelse.getTittel(), forsendelse.getId());
                importerEnForsendelse(nedlaster, importer, forsendelse);
            }
            log.info("Importering til sakssystem er ferdig.");
            DownloadHandler.es.shutdown();
        } catch (Exception e) {
            log.error("Noe gikk galt", e);
        }
    }

    private void importerEnForsendelse(Forsendelsesnedlaster nedlaster, Saksimporter importer, Forsendelse forsendelse) {
        try (final Fil fil = nedlaster.hentForsendelseFil(forsendelse)) {

            final Journalpost journalpost = importer.importerJournalPost(forsendelse);
            log.info("Laget journalpost {} for forsendelse {}", journalpost.getJournalpostnummer(), forsendelse.getId());

            if (fil.getMimetype().contains("application/zip")) {
                lagDokumentFraZipfil(importer, forsendelse, fil, journalpost);
                nedlaster.kvitterForsendelse(forsendelse);
                forsendelserOKlog.info("Importerte forsendelse med id {}", forsendelse.getId());
            } else {
                final Dokument dokument = importer.importerDokument(journalpost, forsendelse.getTittel(), fil.getFilename(), fil.getMimetype(), fil.getData(), true, forsendelse, () -> nedlaster.kvitterForsendelse(forsendelse));
                log.info("Laget dokument {} for forsendelse {}", dokument.getDokumentnummer(), forsendelse.getId());
                forsendelserOKlog.info("Importerte forsendelse med tittel {} og id {}", forsendelse.getTittel(), forsendelse.getId());
            }
        } catch (Exception e) {
            forsendelserFeiletlog.info("Import av forsendelse {} med tittel {} feilet.", forsendelse.getId(), forsendelse.getTittel());
            log.info("Forsendelse {} ble ikke lagret.", forsendelse.getId(), e);
        }
    }

    private void lagDokumentFraZipfil(Saksimporter importer, Forsendelse forsendelse, Fil fil, Journalpost journalpost) throws IOException {
        try (final ZipInputStream zis = new ZipInputStream(fil.getData())) {
            ZipEntry entry;
            boolean first = true;
            while ((entry = zis.getNextEntry()) != null) {

                log.info("entry: " + entry.getName() + ", " + entry.getSize());
                // fikse mimetype
                final Fil zipfilEntry = new Fil(zis, findMimeType(entry.getName(), forsendelse.getFilmetadata()), entry.getName());
                final Dokument dokument = importer.importerDokument(journalpost, zipfilEntry.getFilename(), zipfilEntry.getFilename(), zipfilEntry.getMimetype(), zipfilEntry.getData(), first, forsendelse, null);
                first = false;
                log.info("Laget dokument {} for forsendelse {}", dokument.getDokumentnummer(), forsendelse.getId());
                zis.closeEntry();
            }
        }
    }

    private String findMimeType(String name, List<FilMetadata> filmetadata) {
        for (FilMetadata filMetadata : filmetadata) {
            if (name.equals(filMetadata.getFilnavn())) return filMetadata.getMimetype();
        }
        return "application/pdf";
    }
}
