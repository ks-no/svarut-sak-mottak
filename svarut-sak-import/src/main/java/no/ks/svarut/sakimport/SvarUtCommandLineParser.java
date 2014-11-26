package no.ks.svarut.sakimport;

import org.apache.commons.cli.*;

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

        Option brukernavn = OptionBuilder.withArgName(KommandoParametre.BRUKER_STR.getValue())
                .hasArg()
                .withDescription("brukernavn for svarut-pålogging")
                .create(KommandoParametre.BRUKER_STR.getValue());

        Option passord = OptionBuilder.withArgName(KommandoParametre.PASSORD_STR.getValue())
                .hasArg()
                .withDescription("Passord for svarut-pålogging")
                .create(KommandoParametre.PASSORD_STR.getValue());

        Option url = OptionBuilder.withArgName(KommandoParametre.URL_STR.getValue())
                .hasArg()
                .withDescription("URL til SvarUt")
                .create(KommandoParametre.URL_STR.getValue());

        options.addOption(brukernavn);
        options.addOption(passord);
        options.addOption(hjelp);
        options.addOption(versjon);
        options.addOption(url);

        return options;
    }

    private Options definerKommandolinjeArgumenter() {
        Options options = new Options();

        Option brukernavn = OptionBuilder.withArgName(KommandoParametre.BRUKER_STR.getValue())
                .hasArg()
                .withDescription("brukernavn for svarut-pålogging")
                .create(KommandoParametre.BRUKER_STR.getValue());

        Option passord = OptionBuilder.withArgName(KommandoParametre.PASSORD_STR.getValue())
                .hasArg()
                .withDescription("Passord for svarut-pålogging")
                .create(KommandoParametre.PASSORD_STR.getValue());

        Option url = OptionBuilder.withArgName(KommandoParametre.URL_STR.getValue())
                .hasArg()
                .withDescription("URL til SvarUt")
                .create(KommandoParametre.URL_STR.getValue());

        options.addOption(brukernavn);
        options.addOption(passord);
        options.addOption(url);

        return options;
    }
}
