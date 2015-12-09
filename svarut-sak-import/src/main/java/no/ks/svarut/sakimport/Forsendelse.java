package no.ks.svarut.sakimport;


import org.joda.time.DateTime;

import java.util.List;

public class Forsendelse implements Comparable<Forsendelse> {

    private String id;
    private String tittel;
    private DateTime date;
    private NoarkMetadataFraAvleverendeSakssystem metadataFraAvleverendeSystem;
    private NoarkMetadataForImport metadataForImport;
    private String serverUrl;
    private String status;
    private String downloadUrl;
    private Avsender avsender;
    private Mottaker mottaker;
    private List<FilMetadata> filmetadata;
    private Mottaker svarSendesTil;
    public Forsendelse() {
    }

    public Forsendelse(String id) {
        this.id = id;
    }

    public Forsendelse(String id, String tittel, DateTime dato, NoarkMetadataFraAvleverendeSakssystem metadataFraAvleverendeSystem, NoarkMetadataForImport metadataForImport, String serverUrl, String statusText, String downloadUrl) {
        this.id = id;
        this.tittel = tittel;
        this.date = dato;
        this.metadataFraAvleverendeSystem = metadataFraAvleverendeSystem;
        this.metadataForImport = metadataForImport;
        this.serverUrl = serverUrl;
        this.status = statusText;
        this.downloadUrl = downloadUrl;

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public DateTime getDate() {
        if (date == null) return new DateTime(0);
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public NoarkMetadataFraAvleverendeSakssystem getMetadataFraAvleverendeSystem() {
        return metadataFraAvleverendeSystem;
    }

    public void setMetadataFraAvleverendeSystem(NoarkMetadataFraAvleverendeSakssystem metadataFraAvleverendeSystem) {
        this.metadataFraAvleverendeSystem = metadataFraAvleverendeSystem;
    }

    public NoarkMetadataForImport getMetadataForImport() {
        return metadataForImport;
    }

    public void setMetadataForImport(NoarkMetadataForImport metadataForImport) {
        this.metadataForImport = metadataForImport;
    }

    public Avsender getAvsender() {
        return avsender;
    }

    public void setAvsender(Avsender avsender) {
        this.avsender = avsender;
    }

    public Mottaker getMottaker() {
        return mottaker;
    }

    public void setMottaker(Mottaker mottaker) {
        this.mottaker = mottaker;
    }

    public List<FilMetadata> getFilmetadata() {
        return filmetadata;
    }

    public void setFilmetadata(List<FilMetadata> filmetadata) {
        this.filmetadata = filmetadata;
    }

    public Mottaker getSvarSendesTil() {
        return svarSendesTil;
    }

    public void setSvarSendesTil(Mottaker svarSendesTil) {
        this.svarSendesTil = svarSendesTil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Forsendelse that = (Forsendelse) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Forsendelse forsendelse) {
        return getDate().compareTo(forsendelse.getDate());
    }

}
