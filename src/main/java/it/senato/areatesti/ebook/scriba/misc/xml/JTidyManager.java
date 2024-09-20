package it.senato.areatesti.ebook.scriba.misc.xml;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


/**
 * The class to manage JTidy
 */
public class JTidyManager {
    private static String location = "";
    private static final Pattern pattern = Pattern.compile("<\\?xml[^?]*\\?>", Pattern.CASE_INSENSITIVE);

    /**
     * Transforms an HTML content to XHTML
     * (it applies the XML_DECL)
     *
     * @return the Xhtml resulting content
     */
    public static String fromHtmlToXhtml(String htmlText) {
        String result = applyTidyToString(htmlText);
        return Context.XML_DECL + result;
    }

    /**
     * Transforms a Not well-formed XHTML to good XHTML
     *
     * @return the Xhtml resulting content
     */
    public static String fromNotWellFormedXhtmlToXhtml(String htmlText) {
        htmlText = pattern.matcher(htmlText).replaceFirst("");
        return applyTidyToString(htmlText);
    }

    private static String applyTidyToString(String htmlText) {
        StringReader sr = new StringReader(htmlText);
        StringWriter sw = new StringWriter();

        Tidy tidy = new Tidy(); // obtains a new Tidy instance
        setTidyProp(tidy);

        StringWriter swerr;
        swerr = new StringWriter();
        tidy.setErrout(new PrintWriter(swerr));

        //Context.getInstance().getLogger().info("msg: "+htmlText);
        tidy.parse(sr, sw); // runs tidy, providing an input and output stream

        String result = sw.toString();

        if (Context.getInstance().jtidyLogActive) {
            Context.getInstance().getLogger().debug("Tidy computation output: " + swerr.getBuffer().toString());
        }

        return result;
    }

    /**
     * Set external location of JTidy properties file
     */
    public static void setTidyPropLoc(String loc) {
        location = loc;
    }

    /**
     * Sets the JTidy properties
     */
    private static void setTidyProp(Tidy tidy) {
        if (Context.getInstance().jtidyPropPosition.toLowerCase().equals("code")) {
            tidy.setMakeClean(true);
            tidy.setHideComments(true);
            tidy.setDocType("strict");
            tidy.setDropFontTags(true);
            tidy.setDropProprietaryAttributes(true);
            tidy.setLogicalEmphasis(true);
            tidy.setNumEntities(true);
            tidy.setXHTML(true);
            tidy.setIndentContent(true);
            tidy.setWraplen(0);
            tidy.setWrapAsp(false);
            tidy.setWrapJste(false);
            tidy.setWrapPhp(false);
            tidy.setTidyMark(false);
            tidy.setForceOutput(true);
            tidy.setOutputEncoding(Context.DEF_ENCODING);

        } else if (Context.getInstance().jtidyPropPosition.toLowerCase().equals("file")) {
            Properties properties = new Properties();

            try {

                if (location.isEmpty()) {

                    Context.getInstance().getLogger().info("Using default tidy property settings");

                    ResourceBundle resource = ResourceBundle.getBundle("jtidy");
                    Enumeration<String> keys = resource.getKeys();

                    while (keys.hasMoreElements()) {

                        String key = keys.nextElement();
                        Context.getInstance().getLogger().info("[Getting JTidy Property] " + key + ": " + resource.getString(key));
                        properties.put(key, resource.getString(key));
                    }
                } else {

                    Context.getInstance().getLogger().info("Using external tidy properties file");
                    InputStream in = new FileInputStream(location);

                    Reader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    properties.load(r);
                }

                tidy.setConfigurationFromProps(properties);
            } catch (Exception e) {
                Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            Context.getInstance().getLogger().error("JTidy properties configuration is not valid. Check the config.prop.xml!");
        }

    }

}
