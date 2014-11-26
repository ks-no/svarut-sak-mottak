package no.ks.svarut.sakimport;


import org.joda.time.DateTime;

public class Forsendelse implements Comparable<Forsendelse> {

    private String id;
    private String tittel;
    private DateTime date;
    private NoarkMetadata metadataFraAvleverendeSystem;
    private NoarkMetadata metadataIMottakendeSystem;
    private String serverUrl;
    private String status;
    private String downloadUrl;
    private Avsender avsender;
    private Mottaker mottaker;

    public Forsendelse() {
    }

    public Forsendelse(String id) {
        this.id = id;
    }

    public Forsendelse(String id, String tittel, DateTime dato, NoarkMetadata metadataFraAvleverendeSystem, NoarkMetadata metadataIMottakendeSystem, String serverUrl, String statusText, String downloadUrl) {
        this.id = id;
        this.tittel = tittel;
        this.date = dato;
        this.metadataFraAvleverendeSystem = metadataFraAvleverendeSystem;
        this.metadataIMottakendeSystem = metadataIMottakendeSystem;
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

    public NoarkMetadata getMetadataFraAvleverendeSystem() {
        return metadataFraAvleverendeSystem;
    }

    public void setMetadataFraAvleverendeSystem(NoarkMetadata metadataFraAvleverendeSystem) {
        this.metadataFraAvleverendeSystem = metadataFraAvleverendeSystem;
    }

    public NoarkMetadata getMetadataIMottakendeSystem() {
        return metadataIMottakendeSystem;
    }

    public void setMetadataIMottakendeSystem(NoarkMetadata metadataIMottakendeSystem) {
        this.metadataIMottakendeSystem = metadataIMottakendeSystem;
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
