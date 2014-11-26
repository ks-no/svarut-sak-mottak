package no.ks.svarut.sakimport;


public class Avsender {
    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String navn;
    private String poststed;
    private String postnr;

    public Avsender() {
    }

    public void setAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
    }

    public void setAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
    }

    public void setAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public void setPostnr(String postnr) {
        this.postnr = postnr;
    }

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public String getNavn() {
        return navn;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getPostnr() {
        return postnr;
    }
}
