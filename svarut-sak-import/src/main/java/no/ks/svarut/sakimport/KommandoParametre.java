package no.ks.svarut.sakimport;

public enum KommandoParametre {

    SVARUT_BRUKER("svarutbrukernavn"),
    SVARUT_PASSORD("svarutpassord"),
    SVARUT_URL("svaruturl"),
    HJELP_STR("help"),
    VERSJON_STR("version"),
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
