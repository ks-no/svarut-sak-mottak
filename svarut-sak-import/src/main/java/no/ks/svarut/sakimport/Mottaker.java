package no.ks.svarut.sakimport;

public class Mottaker {
    private String adresse1;
    private String adresse2;
    private String adresse3;
    private String postnr;
    private String poststed;
    private String navn;
    private String land;

    public Mottaker() {
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public void setAdresse2(String adresse2) {
        this.adresse2 = adresse2;
    }

    public void setAdresse3(String adresse3) {
        this.adresse3 = adresse3;
    }

    public void setPostnr(String postnr) {
        this.postnr = postnr;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public String getAdresse2() {
        return adresse2;
    }

    public String getAdresse3() {
        return adresse3;
    }

    public String getPostnr() {
        return postnr;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getNavn() {
        return navn;
    }

    public String getLand() {
        return land;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mottaker mottaker = (Mottaker) o;

        if (adresse1 != null ? !adresse1.equals(mottaker.adresse1) : mottaker.adresse1 != null) return false;
        if (adresse2 != null ? !adresse2.equals(mottaker.adresse2) : mottaker.adresse2 != null) return false;
        if (adresse3 != null ? !adresse3.equals(mottaker.adresse3) : mottaker.adresse3 != null) return false;
        if (land != null ? !land.equals(mottaker.land) : mottaker.land != null) return false;
        if (navn != null ? !navn.equals(mottaker.navn) : mottaker.navn != null) return false;
        if (postnr != null ? !postnr.equals(mottaker.postnr) : mottaker.postnr != null) return false;
        if (poststed != null ? !poststed.equals(mottaker.poststed) : mottaker.poststed != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = adresse1 != null ? adresse1.hashCode() : 0;
        result = 31 * result + (adresse2 != null ? adresse2.hashCode() : 0);
        result = 31 * result + (adresse3 != null ? adresse3.hashCode() : 0);
        result = 31 * result + (postnr != null ? postnr.hashCode() : 0);
        result = 31 * result + (poststed != null ? poststed.hashCode() : 0);
        result = 31 * result + (navn != null ? navn.hashCode() : 0);
        result = 31 * result + (land != null ? land.hashCode() : 0);
        return result;
    }
}
