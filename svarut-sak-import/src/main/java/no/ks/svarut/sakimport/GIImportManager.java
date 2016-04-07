package no.ks.svarut.sakimport;

import no.geointegrasjon.rep.arkiv.dokument.xml_schema._2012_01.Dokument;
import no.geointegrasjon.rep.arkiv.kjerne.xml_schema._2012_01.Journalpost;
import no.geointegrasjon.rep.arkiv.oppdatering.xml_wsdl._2012_01_31.*;
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
    private static Logger forsendelseslog = LoggerFactory.getLogger("forsendelser");
    private final String[] args;


    public GIImportManager(String... args) {
        this.args = args;
    }

    public boolean importerNyeForsendelser() {
        log.info("Start import av forsendelser");
        try {
            SakImportConfig config = new SakImportConfig(args);
            Forsendelsesnedlaster nedlaster = new Forsendelsesnedlaster(config);
            List<Forsendelse> forsendelser = nedlaster.hentNyeForsendelser();

            Saksimporter importer = new Saksimporter(config);
            log.info("Importerer {} forsendelser", forsendelser != null ? forsendelser.size() : 0);
            forsendelseslog.info("{} nye forsendelser", forsendelser != null ? forsendelser.size() : 0);
            boolean failed = false;
            for (Forsendelse forsendelse : forsendelser) {
                log.info("Importerer forsendelse {} {}", forsendelse.getTittel(), forsendelse.getId());
                try {
                    importerEnForsendelse(nedlaster, importer, forsendelse);
                } catch(Exception e){
                    failed = true;
                }
            }
            log.info("Importering til sakssystem er ferdig.");
            DownloadHandler.es.shutdown();
            return failed;
        } catch (Exception e) {
            log.error("Noe gikk galt", e);
            return true;
        }
    }

    private void importerEnForsendelse(Forsendelsesnedlaster nedlaster, Saksimporter importer, Forsendelse forsendelse) throws IOException, ApplicationException, FinderException, OperationalException, ImplementationException, SystemException {
        try (final Fil fil = nedlaster.hentForsendelseFil(forsendelse)) {

            final Journalpost journalpost = importer.importerJournalPost(forsendelse);
            if(journalpost == null)
                log.error("Journalpost er null");
            if(journalpost != null)
                log.info("Laget journalpost {} for forsendelse {}", journalpost.getJournalnummer().getJournalaar() + "/" + journalpost.getJournalnummer().getJournalsekvensnummer(), forsendelse.getId());

            //fix for ephorte, eksternnøkkel blir ikke lagt inn
            importer.oppdaterEksternNoekkel(forsendelse, journalpost.getJournalnummer());

            if (fil.getMimetype().contains("application/zip")) {
                lagDokumentFraZipfil(importer, forsendelse, fil, journalpost);
                nedlaster.kvitterForsendelse(forsendelse);
            } else {
                final Dokument dokument = importer.importerDokument(journalpost, forsendelse.getTittel(), fil.getFilename(), fil.getMimetype(), fil.getKryptertData(), true, forsendelse, () -> nedlaster.kvitterForsendelse(forsendelse));
                log.info("Laget dokument {} for forsendelse {}", dokument.getDokumentnummer(), forsendelse.getId());
            }
            forsendelseslog.info("Importerte forsendelse med tittel {},id {}, saksnr: {} og journalpostnummer {}.", forsendelse.getTittel(), forsendelse.getId(), journalpost.getSaksnr().getSaksaar() +"/"+ journalpost.getSaksnr().getSakssekvensnummer(),  journalpost.getJournalnummer().getJournalaar() + "/" + journalpost.getJournalnummer().getJournalsekvensnummer());
        } catch (ValidationException e) {
            log.info("Forsendelse {} validerte ikke.", forsendelse.getId(), e);
        } catch (Exception e) {
            forsendelseslog.info("Import av forsendelse {} med tittel {} feilet.", forsendelse.getId(), forsendelse.getTittel());
            log.info("Forsendelse {} ble ikke lagret.", forsendelse.getId(), e);
            throw e;
        }
    }

    private void lagDokumentFraZipfil(Saksimporter importer, Forsendelse forsendelse, Fil fil, Journalpost journalpost) throws IOException {
        try (final ZipInputStream zis = new ZipInputStream(fil.getKryptertData())) {
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
