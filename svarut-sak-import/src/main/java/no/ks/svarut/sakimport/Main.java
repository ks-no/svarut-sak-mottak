package no.ks.svarut.sakimport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;

public class Main {

    private Main() {
    }

    public static void main(String... args) throws IOException {
        Security.addProvider(new BouncyCastleProvider());

        GIImportManager manager = new GIImportManager(args);
        manager.importerNyeForsendelser();
    }
}

