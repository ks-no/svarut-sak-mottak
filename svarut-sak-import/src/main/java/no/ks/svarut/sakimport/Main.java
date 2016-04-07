package no.ks.svarut.sakimport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Security;

public class Main {

    private Main() {
    }

    public static void main(String... args) throws IOException {
        Logger errorlog = LoggerFactory.getLogger("errorlogger");

        if(run(args)){
            //run with errors
            errorlog.error("Kjørte med feil");
            System.exit(1);

        } else {
            // run without errors.
            System.exit(0);
        }
    }

    public static boolean run(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        final GIImportManager manager = new GIImportManager(args);
        return manager.importerNyeForsendelser();
    }
}

