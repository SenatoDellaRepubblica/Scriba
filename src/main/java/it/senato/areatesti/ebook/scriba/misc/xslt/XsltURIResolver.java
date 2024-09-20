package it.senato.areatesti.ebook.scriba.misc.xslt;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static it.senato.areatesti.ebook.scriba.packaging.PackageMaker.isValidUrl;

/**
 * Class to resolve inclusions in XSLT
 *
 * @author roberto.battistoni
 */
class XsltURIResolver implements URIResolver {

    private final String uriXsltToInclude;

    /**
     * It passes to the class the base path of the dir that includes the XSLT included in the first one
     */
    XsltURIResolver(String uriXsltToInclude) {
        super();
        this.uriXsltToInclude = uriXsltToInclude;
    }

    @Override
    public Source resolve(String href, String base) {

        InputStream inputStream = null;
            //InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("xslts/" + href);
            if (isValidUrl(this.uriXsltToInclude)) {
                URI uri = null;
                try {
                    uri = (new URL(FilenameUtils.concat(this.uriXsltToInclude, href))).toURI();
                } catch (URISyntaxException | MalformedURLException e) {
                    Context.getInstance().getLogger().error("URL problems for the XSLT: "+e.getMessage());
                }
                try {
                    inputStream = FileUtils.openInputStream(new File(uri));
                } catch (IOException e) {
                    Context.getInstance().getLogger().error("URL problems for the XSLT (I/O): "+e.getMessage());
                }
            }
            else
            {
                String path = FilenameUtils.concat(this.uriXsltToInclude, href);
                try {
                    inputStream = FileUtils.openInputStream(new File(path));
                } catch (IOException e) {
                    Context.getInstance().getLogger().error("Path problems for the XSLT (I/O): "+e.getMessage());
                }
            }
            return new StreamSource(inputStream);

    }
}