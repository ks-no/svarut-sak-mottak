package no.ks.svarut.sakimport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Security;

public class Main {

    private static Logger log= LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String... args) throws IOException {
        try {
            Logger errorlog = LoggerFactory.getLogger("errorlogger");

            if (run(args)) {
                //run with errors
                errorlog.error("Kjørte med feil");
                System.exit(1);

            } else {
                // run without errors.
                System.exit(0);
            }
        }catch(Throwable e){
            log.error("Run failed ", e);
            throw e;
        }
    }

    public static boolean run(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        final GIImportManager manager = new GIImportManager(args);
        return manager.importerNyeForsendelser();
    }
}

