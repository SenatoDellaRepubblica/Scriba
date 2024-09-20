package it.senato.areatesti.ebook.scriba;

import it.senato.areatesti.ebook.scriba.batch.DirectoryWorker;
import it.senato.areatesti.ebook.scriba.misc.xml.JTidyManager;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * The Main Class for CMD line execution
 */
public class Main {

    /**
     * Main program
     */
    public static void main(String[] args) {
        try {
            FileUtils.forceMkdir(new File("./log"));
        } catch (IOException e) {
            System.out.println("Cannot create the log directory");
        }

        Options options = new Options();
        options.addOption(Context.CMD_C, "contentFile", true, "The file or the directory which defines Scriba contents (in the case of the dir, all the .scf.xml files)");
        options.addOption(Context.CMD_O, "outputFile", true, "The output file or directory");
        options.addOption(Context.CMD_CR, "color", true, "color palette for PDF converted images: gray/rgb");
        options.addOption(Context.CMD_NIMG, "noImgFromPdf", false, "do not extract images from the included PDF (only for the EPUB final ebook)");
        options.addOption(Context.CMD_T, "ebookType", true, "the type of ebook to make: EPUB, ZIP, ALL");
        options.addOption(Context.CMD_X, "xslEngine", true, "Class (and package) for the XSL engine [net.sf.saxon.TransformerFactoryImpl|org.apache.xalan.processor.TransformerFactoryImpl]");
        options.addOption(Context.CMD_D, "diffRun", false, "Differential Run for list of SCF files");
        options.addOption(Context.CMD_DD, "initDiffRun", false, "inits and runs the Differential engine");
        options.addOption(Context.CMD_TR, "thread", true, "number of threads for parallel computation");

        options.addOption(Context.CMD_V, "version", false, "the version of the program");
        options.addOption(Context.CMD_H, "help", false, "print the help");
        options.addOption(Context.CMD_TP, "tidyProperties", true, "set tidy properties file");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(Context.CMD_H)) {
                printHelp(options);
                printCommandLineArguments(args);
            }

            if (line.hasOption(Context.CMD_V)) {
                System.out.println(Context.getInstance().versionFootprint);
                System.exit(0);
            }


            if (line.hasOption(Context.CMD_C) &&
                    line.hasOption(Context.CMD_T)
                    && line.hasOption(Context.CMD_O)
            ) {

                if (line.hasOption(Context.CMD_TP)) {
                    JTidyManager.setTidyPropLoc(line.getOptionValue(Context.CMD_TP));
                }

                // The XSLT engine to use
                if (line.hasOption(Context.CMD_X)) {
                    Context.getInstance().xsltEngine = line.getOptionValue(Context.CMD_X);
                }
                Context.getInstance().getLogger().debug("using this XSLT engine: " + Context.getInstance().xsltEngine);

                // Checks the existence of the Directories and Files referenced
                File fi = new File(line.getOptionValue(Context.CMD_C));
                File fo = new File(line.getOptionValue(Context.CMD_O));
                boolean contentFileExist = fi.exists();

                if (contentFileExist) {
                    String ebookType = line.getOptionValue(Context.CMD_T);
                    EbookType etype = EbookType.valueOf(ebookType);

                    // if both parameters "-c" e "-o" are files
                    if (fi.isFile()) {
                        createTheEbook(line.getOptionValue(Context.CMD_C), line.getOptionValue(Context.CMD_O), line.getOptionValue(Context.CMD_CR), etype, line.hasOption(Context.CMD_NIMG));
                    } else if (fi.isDirectory() && fo.isDirectory()) {
                        boolean isDiff = false;
                        boolean isDiffToInit = false;

                        if (line.hasOption(Context.CMD_D)) {
                            isDiff = true;
                        }

                        if (line.hasOption(Context.CMD_DD)) {
                            isDiffToInit = true;
                        }

                        int numThread = 0;
                        if (line.hasOption(Context.CMD_TR)) {
                            Context.getInstance().getLogger().info(String.format("Thread activated: n.%s", line.getOptionValue(Context.CMD_TR)));
                            numThread = Integer.parseInt(line.getOptionValue(Context.CMD_TR));
                        }

                        DirectoryWorker dw = new DirectoryWorker(line, isDiff, isDiffToInit, numThread);
                        dw.execute(fi, etype);

                    } else {
                        System.out.println("Check the input and output arguments!");
                    }
                } else
                    System.out.println("Check the SCF file existance (use the -c option)!");

            } else {
                printHelp(options);
                printCommandLineArguments(args);
            }

        } catch (ParseException exp) {
            System.out.println("Command line error: " + exp.getMessage());

            printCommandLineArguments(args);
        }

    }

    /**
     * Creates a single ebook
     */
    public static boolean createTheEbook(String scfFileName, String outputFileName, String colorOption, EbookType ebookType, boolean noExtractImgFromPdf) {
        Context.getInstance().getLogger().info("*********************************************************************************");
        Context.getInstance().getLogger().info("*************************** MAIN RUN ******************************************");
        Context.getInstance().getLogger().info("*********************************************************************************");
        Context.getInstance().getLogger().info(Context.getInstance().versionFootprint);
        Context.getInstance().getLogger().info("Ebook creation started...");
        Context.getInstance().getLogger().info("Using this SCF file: " + scfFileName);
        Context.getInstance().getLogger().info("EBook type: " + ebookType);
        Context.getInstance().getLogger().info("Color option: " + colorOption);
        Context.getInstance().getLogger().info("Image option: " + noExtractImgFromPdf);

        ScribaEBookMaker maker = new ScribaEBookMaker(ebookType, new File(scfFileName), colorOption, noExtractImgFromPdf);
        boolean ret = maker.makeEBookAsFile(outputFileName);
        if (ret)
            Context.getInstance()
                    .getLogger()
                    .info("Ebook created with success.");
        else
            Context.getInstance().getLogger()
                    .error("Some problems in ebook creation happened!");

        return ret;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println(Context.getInstance().versionFootprint);
        formatter.printHelp("java -jar ScribaEngine.jar -c data.scf.xml -t EPUB -o book.epub", options);

    }

    private static void printCommandLineArguments(String[] args) {

        if (args.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cmd line arguments: ");
            for (String arg : args) sb.append(arg).append(" ");
            System.out.println(sb);
        }
    }

}
