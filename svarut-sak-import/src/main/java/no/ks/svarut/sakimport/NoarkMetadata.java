package no.ks.svarut.sakimport;

import org.joda.time.DateTime;

public class NoarkMetadata {
    private int sakssekvensnummer;
    private int saksaar;
    private int journalaar;
    private int journalsekvensnummer;
    private int journalpostnummer;
    private String journalposttype;
    private String journalstatus;
    private DateTime journaldato;
    private DateTime dokumentetsDato;
    private String tittel;

    public NoarkMetadata() {}

    public NoarkMetadata(no.ks.svarut.sakimport.NoarkMetadata noarkMetadata) {
        if(noarkMetadata == null) return;
        setSakssekvensnummer(noarkMetadata.getSakssekvensnummer());
        setSaksaar(noarkMetadata.getSaksaar());
        setJournalaar(noarkMetadata.getJournalaar());
        setJournalsekvensnummer(noarkMetadata.getJournalsekvensnummer());
        setJournalpostnummer(noarkMetadata.getJournalpostnummer());
        setJournalposttype(noarkMetadata.getJournalposttype());
        setJournalstatus(noarkMetadata.getJournalstatus());
        setJournaldato(noarkMetadata.getJournaldato());
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

    public int getJournalaar() {
        return journalaar;
    }

    public void setJournalaar(int journalaar) {
        this.journalaar = journalaar;
    }

    public int getJournalsekvensnummer() {
        return journalsekvensnummer;
    }

    public void setJournalsekvensnummer(int journalsekvensnummer) {
        this.journalsekvensnummer = journalsekvensnummer;
    }

    public int getJournalpostnummer() {
        return journalpostnummer;
    }

    public void setJournalpostnummer(int journalpostnummer) {
        this.journalpostnummer = journalpostnummer;
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
    public DateTime getJournaldato() {
        return journaldato;
    }

    //@JsonDeserialize(using = JsonDateDeserializer.class)
    public void setJournaldato(DateTime journaldato) {
        this.journaldato = journaldato;
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
