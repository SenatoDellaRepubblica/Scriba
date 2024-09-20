package it.senato.areatesti.ebook.scriba;

import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.misc.TempCleaner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// import org.apache.log4j.xml.DOMConfigurator;

/**
 * The Context of the application
 */
public class Context {
    /**
     * Place for numbers
     */
    public static final String _02D = "%02d";
    /**
     * Place for numbers
     */
    public static final String _04D = "%04d";
    /**
     * <dc:date>  placeholder in the OPF file
     */
    public static final CharSequence DC_DATE_PLACEHOLDER = "%date%";
    /**
     * Default Encoding
     */
    public final static String DEF_ENCODING = "UTF-8";
    /**
     * EPUB extension
     */
    public static final String EPUB_EXT = ".epub";
    /**
     * Prefix for the ID in the OPF file
     */
    public static final String ID_OPF_PREFIX = "id";
    /**
     * Image format for PDF thumb
     */
    public static final String IMAGE_FORMAT = "png";
    /**
     * PNG mimetype
     */
    public static final String IMAGE_MIMETYPE = "image/png";
    /**
     * ISO88591
     */
    public final static String ISO88591 = "ISO8859_1";
    /**
     * Max retry for ImageIOO problem
     */
    public static final int MAX_RETRY_IMAGEIOO_READ = 10;
    /**
     * Max number of retries
     */
    public static final int MAX_WEBGET_RETRY = 10;
    /**
     * newline
     */
    public final static String NEWLINE = System.getProperty("line.separator");
    /**
     * Length of the Normalized file name
     */
    public static final int NORMALIZED_FILE_LENGHT = 16;
    /**
     * path separator
     */
    public static final String PATH_SEP = "/";
    /**
     * PDF extension
     */
    public static final String PDF_EXT = ".pdf";
    /**
     * Mimetype for PDF in the OPS package
     */
    public static final String PDF_MIMETYPE = "application/pdf";
    /**
     * This is the optimal value to have a good PNG image resolution (especially for TIFF)
     */
    public static final int PDF2IMAGE_RESOLUTION = 300;
    /**
     * Pretty date placeholder
     */
    public static final CharSequence PRETTY_DATE_PLACEHOLDER = "%pretty_date%";
    /**
     * The main Property filename
     */
    private static final String PROP_CONFIG_PROP_XML = "prop/config.prop.xml";
    /**
     * The secrets property filename
     */
    private static final String PROP_SECRETS_PROP_XML = "prop/secrets.prop.xml";
    /**
     * the width of resized image
     */
    public static final int RESIZED_WIDTH = 1024;
    /**
     * Section label in the NCX
     */
    public static final String SECTION_LABEL = "SECTION_LABEL";
    /**
     * Common encoding for the encoding of the ebooks
     */
    public final static String UTF8_ENCODING = "UTF-8";
    /**
     * Windows 1252 Latin1
     */
    public final static String WINDOWS_1256_ARABIC = "Windows-1256";
    /**
     * Windows 1252 Latin1
     */
    public final static String WINDOWS_CP1252_LATIN1 = "Cp1252";
    /**
     * Xhtml extension
     */
    public static final String XHTML_EXT = ".xhtml";
    /**
     * Mimetype for Xhtml in the OPS package
     */
    public static final String XHTML_MIMETYPE = "application/xhtml+xml";
    /**
     * Xml Declaration
     */
    public static final String XML_DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    /**
     * ZIP extension
     */
    public static final String ZIP_EXT = ".zip";
    public static final String CMD_H = "h";
    public static final String CMD_V = "v";
    public static final String CMD_TP = "tp";
    public static final String CMD_X = "x";
    public static final String CMD_C = "c";
    public static final String CMD_T = "t";
    public static final String CMD_O = "o";
    public static final String CMD_CR = "cr";
    public static final String CMD_NIMG = "nimg";
    public static final String CMD_D = "d";
    public static final String CMD_DD = "dd";
    public static final String CMD_TR = "tr";
    private static final Context instance = new Context();
    /**
     * BaseUrl
     */
    public String baseUrl;
    /**
     * Placeholder for the CSS replace
     */
    public String cssPlaceholder;
    /**
     * Name of the CSS style to use
     */
    public String cssStyleName;
    /**
     * JTidy properties position: file or code
     */
    public String jtidyPropPosition;
    /**
     * The old URL to substitute
     */
    public String oldBaseUrl;
    /**
     * The CSS applied to the XHTML put inside the final PDF ebook
     */
    public String pdfCss;
    /**
     * PDF arrow image
     */
    public String pdfEbookArrowMetaName;
    /**
     * Servizio Informtica's logo for PDF
     */
    public String pdfEbookDptMetaName;
    /**
     * Senato's logo for PDF
     */
    public String pdfEbookLogoMetaName;
    /**
     * There isn't a password for the PDF encryption
     */
    public String pdfPassword;
    /**
     * Name of the Logo file to use
     */
    public String sectionlogoFileName;
    /**
     * Placeholder for the CSS replace
     */
    public String sectionlogoPlaceholder;
    /**
     * Version string to output when the program is called by cmdline
     */
    public String versionFootprint;
    /**
     * Default XSL to use
     */
    public String xslProp;
    /**
     * The XSLT engine to use:
     * <p>
     * ex: net.sf.saxon.TransformerFactoryImpl, org.apache.xalan.processor.TransformerFactoryImpl
     */
    public String xsltEngine;
    /**
     * The meta name for the Disclaimer
     */
    public String pdfDisclaimerMetaName;
    /**
     * The Doc Name Meta Name
     */
    public String pdfDocNameMetaName;
    /**
     * The Toc Name Meta Name
     */
    public String pdfTocNameMetaName;
    /**
     * The text for the Doc link Meta Name
     */
    public String pdfLinkDocTextMetaName;
    /**
     * Set the flags for the log of JTidy
     */
    public boolean jtidyLogActive;

    /**
     * The main log
     */
    private final Logger log;
    /**
     * The logger for the Batch part
     */
    private final Logger logBatch;
    /**
     * Extension of created temp files (is needed to locate and delete)
     */
    private final String tempExt;
    private final TempCleaner tempGarbageCleaner;

    private Context() {
        tempGarbageCleaner = new TempCleaner();
        setLog();
        this.log = LogManager.getLogger("it.senato.areatesti.ebook.scriba");
        this.logBatch = LogManager.getLogger("it.senato.areatesti.ebook.scriba.batch");

        // initializes the temporary extension
        tempExt = tempGarbageCleaner.getTempExtension();

        setConfigProperties();

    }

    /**
     * Calls the Garbage Collector
     */
    public static void gc() {
        gc(-1);
    }

    /**
     * Calls the Garbage Collector
     *
     */
    private static void gc(int num) {
        Context.getInstance().getLogger().debug("GC invoked");
        if (num > 0)
            gcP(num);
        else
            gcP();
        //Context.getInstance().getLogger().debug(String.format("Max/Total/Free Heap space: %s/%s/%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()),FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()),FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory())));
    }

    /**
     * This method guarantees that garbage collection is
     * done unlike <code>{@link System#gc()}</code>
     */
    private static void gcP() {
        System.gc();
    }

    /**
     * calls <code>{@link #gc()}</code> <code>count</code> times
     */
    private static void gcP(int count) {
        for (; count != 0; count--)
            gc();
    }

    /**
     * This method guarantees that garbage collection is
     * done after JVM shutdown is initialized
     */
    private static void gcPOnExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(Context::gc));
    }

    /**
     * The instance
     *
     */
    public static Context getInstance() {
        return instance;
    }

    /**
     * Get the main Config structure
     *
     */
    private Properties getConfig(String confPropFileName, boolean refresh) {
        Properties conf = null;
        if (refresh) {
            try {
                String confPropString = Misc.getContentProp(confPropFileName, Context.DEF_ENCODING);
                conf = new Properties();
                conf.loadFromXML(IOUtils.toInputStream(confPropString, Context.DEF_ENCODING));
            } catch (IOException e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
            }
        }
        return conf;
    }

    /**
     * Gets the current logger
     *
     */
    public Logger getLogger() {
        return this.log;
    }

    /**
     * Gets the batch logger
     *
     */
    public Logger getBatchLogger() {
        return this.logBatch;
    }

    /**
     * @return the tempExt
     */
    public String getTempExt() {
        return tempExt;
    }

    /**
     * returns the TempGarbage cleaner
     *
     * @return the instance of the class
     */
    public TempCleaner getTempGarbageCleaner() {
        return tempGarbageCleaner;
    }

    /**
     * Sets the config properties: it contains all the string values that could be changed by the users
     */
    private void setConfigProperties() {
        // General Props
        Properties confProp = getConfig(PROP_CONFIG_PROP_XML, true);
        /*
         * Cmd line arguments
         */
        Properties secProp = getConfig(PROP_SECRETS_PROP_XML, true);

        // Default xslt engine
        xsltEngine = "org.apache.xalan.processor.TransformerFactoryImpl";

        // Loads the general configuration data
        versionFootprint = (String) confProp.get("VERSION_FOOTPRINT");

        jtidyPropPosition = (String) confProp.get("JTIDY_DEFAULT_POSITION");
        jtidyLogActive = Boolean.parseBoolean((String) confProp.get("JTIDY_LOG"));
        cssPlaceholder = (String) confProp.get("CSS_PLACEHOLDER");
        oldBaseUrl = (String) confProp.get("OLD_BASE_URL");
        baseUrl = (String) confProp.get("BASE_URL");
        xslProp = (String) confProp.get("XSL_PROP");
        cssStyleName = (String) confProp.get("CSS_STYLE_NAME");

        sectionlogoPlaceholder = (String) confProp.get("LOGO_PLACEHOLDER");
        if (sectionlogoFileName == null)
            sectionlogoFileName = (String) confProp.get("SECTION_LOGO_FILE_NAME");

        // PDF EBooks props
        pdfEbookLogoMetaName = (String) confProp.get("PDF_EBOOK_LOGO_META");
        pdfEbookArrowMetaName = (String) confProp.get("PDF_EBOOK_ARROW_META");
        pdfEbookDptMetaName = (String) confProp.get("PDF_EBOOK_DPT_LOGO_META");
        pdfDisclaimerMetaName = ((String) confProp.get("PDF_DISCLAIMER_META"));
        pdfTocNameMetaName = ((String) confProp.get("PDF_TOCNAME_META"));
        pdfLinkDocTextMetaName = ((String) confProp.get("PDF_DOCLINK_META"));
        pdfDocNameMetaName = ((String) confProp.get("PDF_DOCNAME_META"));


        pdfCss = ((String) confProp.get("PDF_CSS"));

        // Secrets
        pdfPassword = (String) secProp.get("PDF_PASSWORD");

    }

    private void setLog() {
        InputStream url = Thread.currentThread().getContextClassLoader().getResourceAsStream("prop/log4j2.xml");

        Log4jConfigurator logConf = new Log4jConfigurator();
        logConf.setup(url);
    }

    private static class Log4jConfigurator {
        void setup(InputStream xmlFilename) {
            if (xmlFilename != null) {
                try {
                    ConfigurationSource source  = new ConfigurationSource(xmlFilename);
                    Configurator.initialize(null, source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                LogManager.getRootLogger().error(
                        "Log4j configuration not found");
        }
    }


}
