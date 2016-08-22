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
    SAK_IMPORT_EKSTERN_PORT("eksternport"),
    SAK_DEFAULT_SAKSAAR("saksaar"),
    SAK_DEFAULT_SAKSNR("saksnr"),
    SAK_KLIENTNAVN("sakklientnavn"),
    PROPERTIES_FILSTI("konfigurasjonsfil"),
    PRIVATE_KEY_FIL("privatekeyfil"), DEBUG("debug"), SAK_INNSYN_URL("sakinnsynurl");

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
