package no.ks.svarut.sakimport;

public enum KommandoParametre {

    BRUKER_STR("username"),
    PASSORD_STR("password"),
    HJELP_STR("help"),
    VERSJON_STR("version"),
    URL_STR("url"),
    SAK_URL("sakurl"),
    SAK_BRUKERNAVN("sakbrukernavn"),
    SAK_PASSORD("sakpassord"),
    SAK_IMPORT_HOSTNAME("hostname"),
    SAK_DEFAULT_SAKSAAR("saksaar"),
    SAK_DEFAULT_SAKSNR("saksnr"),
    PROPERTIES_FILSTI("konfigurasjonsfil");

    private final String value;

    KommandoParametre(String v) {
        this.value = v;
    }

    public String getValue() {
        return value;
    }

    public static KommandoParametre fromValue(String v) {
        if (v == null || v.trim().isEmpty())
            return null;
        for (KommandoParametre c : KommandoParametre.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
