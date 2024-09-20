/* ******************************************************************************
 Scriba EBook Maker
 Copyright (C) Senato della Repubblica (http://www.senato.it/)

 Developer: Roberto Battistoni (r.battistoni@senato.it)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as publish+ed by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package it.senato.areatesti.ebook.scriba.plugin.base;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.packaging.PackageMaker;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * The plugin abstract class
 */
public abstract class AbstractPlugin implements IPlugin {
    protected String ownEncoding;
    protected EbookType eBookType;
    protected MetadataList metadataList;
    protected ContentItem currentContent;


    /**
     * Default Constructor
     */
    public AbstractPlugin(Object[] initArgs) {
        if (initArgs.length == 1 && initArgs[0] instanceof EbookType) {
            this.eBookType = (EbookType) initArgs[0];
        } else {
            Context.getInstance().getLogger().error("Plugin initialization failed for initArgs array arguments");
        }
    }

    /**
     * Cleans the text from the NBSP
     */
    protected String cleanNbspText(String text) {
        return text.trim().replace("&nbsp;", " ");
    }

    /**
     * Cleans the text from IMG tag
     */
    protected String cleanTagImg(String xhtml) {
        // replaces form
        String regex = "<img.*?>.*?</img>";
        String content = xhtml;

        content = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(content).replaceAll("");
        regex = "<img.*?/>";
        content = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(content).replaceAll("");

        return content;
    }

    /**
     * Translation of invalid Unicode references range to valid range.
     * These are Windows CP1252 specific characters.
     * They would look weird on non-Windows browsers.
     * If you've ever pasted text from MSWord, you'll understand.
     * <p>
     * You should not have to change this.
     */
    protected String htmlTransWinUni(String content) {
        return content.replace("&#128;", "&#8364;").replace( // the Euro sign
                "&#130;", "&#8218;").replace("&#131;", "&#402;")
                .replace("&#132;", "&#8222;").replace("&#133;", "&#8230;")
                .replace("&#134;", "&#8224;").replace("&#135;", "&#8225;")
                .replace("&#136;", "&#710;").replace("&#137;", "&#8240;")
                .replace("&#138;", "&#352;").replace("&#139;", "&#8249;")
                .replace("&#140;", "&#338;").replace("&#142;", "&#382;")
                .replace("&#145;", "&#8216;").replace("&#146;", "&#8217;")
                .replace("&#147;", "&#8220;").replace("&#148;", "&#8221;")
                .replace("&#149;", "&#8226;").replace("&#150;", "&#8211;")
                .replace("&#151;", "&#8212;").replace("&#152;", "&#732;")
                .replace("&#153;", "&#8482;").replace("&#154;", "&#353;")
                .replace("&#155;", "&#8250;").replace("&#156;", "&#339;")
                .replace("&#158;", "&#382;").replace("&#159;", "&#376;");
    }

    /**
     * Get an Error Page
     * (set the Tidy and XSL flag to false)
     */
    protected ContentItem setErrorPage(ContentItem contentItem) throws UnsupportedEncodingException {
        // sets the tidy and the xsl off
        contentItem.setNeededTidy(false);
        contentItem.setNeededXsl(false);
        contentItem.setByteContent(PackageMaker.makeErrorPage(contentItem).getBytes(Context.DEF_ENCODING));

        return contentItem;
    }

    /**
     * Get a Plugin Arg
     */
    protected String getPluginArg(String pluginArgName, String errorMessage) {
        if (this.currentContent.getPluginArgs() != null && this.currentContent.getPluginArgs().size() > 0) {
            return this.currentContent.getPluginArgs().get(pluginArgName);
        }

        return null;
    }

    /**
     * Read the contentUrl content
     */
    protected String readUrlContent(String contentUrl) throws URISyntaxException, IOException {
        File file;
        if (PackageMaker.isValidUrl(contentUrl))
            file = new File((new URL(contentUrl)).toURI());
        else
            file = new File(contentUrl);

        return FileUtils.readFileToString(file, Context.UTF8_ENCODING);
    }
}
