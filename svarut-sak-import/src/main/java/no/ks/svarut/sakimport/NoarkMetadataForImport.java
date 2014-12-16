package no.ks.svarut.sakimport;

import org.joda.time.DateTime;

public class NoarkMetadataForImport {

    private int sakssekvensnummer;
    private int saksaar;
    private String journalposttype;
    private String journalstatus;
    private DateTime dokumentetsDato;
    private String tittel;

    public NoarkMetadataForImport() {}

    public NoarkMetadataForImport(NoarkMetadataForImport noarkMetadata) {
        if(noarkMetadata == null) return;
        setSakssekvensnummer(noarkMetadata.getSakssekvensnummer());
        setSaksaar(noarkMetadata.getSaksaar());
        setJournalposttype(noarkMetadata.getJournalposttype());
        setJournalstatus(noarkMetadata.getJournalstatus());
        setDokumentetsDato(noarkMetadata.getDokumentetsDato());
        setTittel(noarkMetadata.getTittel());
    }

    public int getSakssekvensnummer() {
        return sakssekvensnummer;
    }

    public void setSakssekvensnummer(int sakssekvensnummer) {
        this.sakssekvensnummer = sakssekvensnummer;
    }

    public int getSaksaar() {
        return saksaar;
    }

    public void setSaksaar(int saksaar) {
        this.saksaar = saksaar;
    }

    public String getJournalposttype() {
        return journalposttype;
    }

    public void setJournalposttype(String journalposttype) {
        this.journalposttype = journalposttype;
    }

    public String getJournalstatus() {
        return journalstatus;
    }

    public void setJournalstatus(String journalstatus) {
        this.journalstatus = journalstatus;
    }

    //@JsonSerialize(using = JsonDateSerializer.class)
    public DateTime getDokumentetsDato() {
        return dokumentetsDato;
    }

    //@JsonDeserialize(using = JsonDateDeserializer.class)
    public void setDokumentetsDato(DateTime dokumentetsDato) {
        this.dokumentetsDato = dokumentetsDato;
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }
}
