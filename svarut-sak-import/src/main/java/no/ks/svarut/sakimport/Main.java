package no.ks.svarut.sakimport;

import java.io.IOException;

public class Main {

    private Main() {
    }

    public static void main(String... args) throws IOException {
        GIImportManager manager = new GIImportManager(args);
        manager.importerNyeForsendelser();
    }
}

