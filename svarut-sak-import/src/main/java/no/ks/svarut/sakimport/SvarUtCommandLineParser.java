package no.ks.svarut.sakimport;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

public class SvarUtCommandLineParser {

    private String[] args;

    public SvarUtCommandLineParser(String[] args) {
        this.args = args;
    }

    public CommandLine parse() {
        Options generelleOptions = definerGenerelleKommandolinjeargumenter();
        parseGenerelleArgumenter(generelleOptions);

        Options options = definerKommandolinjeArgumenter();
        CommandLine commandLine = hentKommandolinjeArgumenter(options);

        return commandLine;
    }

    private void parseGenerelleArgumenter(Options options) {
        CommandLine commandLine = hentKommandolinjeArgumenter(options);

        if (commandLine.hasOption(KommandoParametre.HJELP_STR.getValue())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SvarUt Sak-import", options);
            System.exit(0);
        }

        if (commandLine.hasOption(KommandoParametre.VERSJON_STR.getValue())) {
            System.out.println("Versjon 0.1");
            System.exit(0);
        }
    }

    private CommandLine hentKommandolinjeArgumenter(Options options) {
        CommandLineParser cmdParser = new GnuParser();
        CommandLine commandLine = null;

        try {
            commandLine = cmdParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Kunne ikke lese kommandolinjeargumenter.");
            e.printStackTrace();
            System.exit(-1);
        }

        return commandLine;
    }

    private Options definerGenerelleKommandolinjeargumenter() {
        Options options = new Options();

        Option hjelp = OptionBuilder.withArgName(KommandoParametre.HJELP_STR.getValue())
                .withDescription("Printer oversikt over mulige kommandolinjeargumenter.")
                .withArgName("h")
                .withArgName("?")
                .create(KommandoParametre.HJELP_STR.getValue());

        Option versjon = OptionBuilder.withArgName(KommandoParametre.VERSJON_STR.getValue())
                .withDescription("Printer versjon av SvarUt Sak-import")
                .withArgName("v")
                .create(KommandoParametre.VERSJON_STR.getValue());


        options.addOption(hjelp);
        options.addOption(versjon);
        fellesoptions().stream().forEach((o) -> options.addOption(o));
        return options;
    }

    private Options definerKommandolinjeArgumenter() {
        Options options = new Options();
        fellesoptions().stream().forEach((o) -> options.addOption(o));
        return options;
    }

    private List<Option> fellesoptions(){
        final ArrayList<Option> options = new ArrayList<Option>();

        options.add(OptionBuilder.withArgName(KommandoParametre.SVARUT_BRUKER.getValue())
                .hasArg()
                .withDescription("brukernavn for svarut-pålogging")
                .create(KommandoParametre.SVARUT_BRUKER.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SVARUT_PASSORD.getValue())
                .hasArg()
                .withDescription("Passord for svarut-pålogging")
                .create(KommandoParametre.SVARUT_PASSORD.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SVARUT_URL.getValue())
                .hasArg()
                .withDescription("URL til SvarUt")
                .create(KommandoParametre.SVARUT_URL.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_URL.getValue())
                .hasArg()
                .withDescription("URL til GeointegrasjonWebservice i saksystemet")
                .create(KommandoParametre.SAK_URL.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_INNSYN_URL.getValue())
                .hasArg()
                .withDescription("URL til GeointegrasjonWebservice for innsyn i saksystemet")
                .create(KommandoParametre.SAK_INNSYN_URL.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_BRUKERNAVN.getValue())
                .hasArg()
                .withDescription("Brukernavn til GeointegrasjonWebservice i saksystemet")
                .create(KommandoParametre.SAK_BRUKERNAVN.getValue()));

        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_PASSORD.getValue())
                .hasArg()
                .withDescription("Passord til GeointegrasjonWebservice i saksystemet")
                .create(KommandoParametre.SAK_PASSORD.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_IMPORT_HOSTNAME.getValue())
                .hasArg()
                .withDescription("Hostnavn for server til svarut-sak-import som saksystem kan nå den på.")
                .create(KommandoParametre.SAK_IMPORT_HOSTNAME.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_DEFAULT_SAKSAAR.getValue())
                .hasArg()
                .withDescription("Saksår for importsak i saksystem. Brukes når ikke forsendelse kan legges direkte på korrekt sak.")
                .create(KommandoParametre.SAK_DEFAULT_SAKSAAR.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.SAK_DEFAULT_SAKSNR.getValue())
                .hasArg()
                .withDescription("Saksnr for importsak i saksystem. Brukes når ikke forsendelse kan legges direkte på korrekt sak.")
                .create(KommandoParametre.SAK_DEFAULT_SAKSNR.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.PROPERTIES_FILSTI.getValue())
                .hasArg()
                .withDescription("Angir filsti og filnavn til konfigurasjonsfilen")
                .create(KommandoParametre.PROPERTIES_FILSTI.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.PRIVATE_KEY_FIL.getValue())
        .hasArg()
        .withDescription("Sti til privat nøkkel for dekryptering av forsendelsefiler. Offentlig nøkkel som tilhører må være lagt inn i SvarUt.")
        .create(KommandoParametre.PRIVATE_KEY_FIL.getValue()));
        options.add(OptionBuilder.withArgName(KommandoParametre.DEBUG.getValue())
                .withDescription("Skru på debug logging")
                .create(KommandoParametre.DEBUG.getValue()));
        return options;
    }
}
