package it.senato.areatesti.ebook.scriba.misc.url;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.EbookEncodingUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Gets a web file pointed by an URL
 */
public class WebGet {

    /**
     * The URL of the resource to download
     */
    private final String urlString;

    /**
     * The declared URL resource encoding. If null it is loaded with the detected encoding
     */
    private String sourceDeclaredEnc;

    /**
     * The declared Mimetype. Used only for the FS download
     */
    private final String declaredUrlMimetype;

    /**
     * It is the mimetype of the downloaded resource
     */
    private String downloadResMimeType;

    /**
     * Constructor
     */
    public WebGet(String urlString, String urlMimetype) {
        this(urlString, urlMimetype, null);
    }

    /**
     * Constructor
     */
    public WebGet(String urlString, String urlMimetype, String sourceEncoding) {
        this.urlString = urlString.trim();
        this.declaredUrlMimetype = urlMimetype;
        this.sourceDeclaredEnc = sourceEncoding;
    }

    /**
     * @return the downloadResMimeType
     */
    public String getDownloadResMimeType() {
        return downloadResMimeType;
    }

    /**
     * Downloads the content
     * <p>
     * If the content is Text then the encoding is DEF (UTF8)
     *
     * @return the byte array outcome
     */
    public UrlDownloadResult getUrlContent() {
        URL url;
        try {
            url = new URL(urlString);
            //Context.getInstance().getLogger().debug("The URL is: "+urlString);
            UrlDownloadResult udr = new UrlDownloadResult(declaredUrlMimetype);

            if (url.getProtocol().equals("file")) {
                //Context.getInstance().getLogger().debug("Start--> Download file from LocalFileSystem: " + urlString);
                udr.setMainContent(getFile(url, declaredUrlMimetype));
                //Context.getInstance().getLogger().debug("End--> Download file from LocalFileSystem: " + urlString);
            } else {
                //Context.getInstance().getLogger().debug("Start--> Download file from external site: " + urlString);
                udr.setMainContent(httpDownload());
                //Context.getInstance().getLogger().debug("End--> Download file from external site: " + urlString);
            }
            return udr;

        } catch (IOException | URISyntaxException e) {
            Context.getInstance().getLogger()
                    .info(e.getMessage());
        }

        return null;
    }


    /**
     * Gets a file
     *
     * @param url the file URL
     */
    private byte[] getFile(URL url, String mimetype) throws IOException, URISyntaxException {

        Context.getInstance().getLogger().debug("Get file: "+url);
        File f = new File(url.toURI());
        byte[] bbuf = IOUtils.toByteArray(new FileInputStream(f));
        bbuf = convertBufferToDefEncoding(bbuf, mimetype, this.sourceDeclaredEnc);
        return bbuf;
    }


    public byte[] httpDownload()  {
        HttpGet httpRequest;

        try (CloseableHttpClient httpClient = HttpClients.custom().setRetryHandler(getRetryHandler(0)).build())
        {
            Context.getInstance().getLogger().debug("Get url: "+urlString);
            httpRequest = new HttpGet(urlString);
            ResponseHandler<byte[]> responseHandler = getResponseHandler(0);
            return httpClient.execute(httpRequest, responseHandler);

        } catch (IOException e) {
            Context.getInstance().getLogger().info("Problem in getting URL: " + urlString);
            return null;
        }
    }

    /**
     * The response handler
     * <p>
     * The content is returned in DEF encoding (typically UTF8) if it contains text and not binary
     */
    private ResponseHandler<byte[]> getResponseHandler(int executionCount) {
        return response ->
        {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            downloadResMimeType = ContentType.getOrDefault(entity).getMimeType();
            return convertBufferToDefEncoding( EntityUtils.toByteArray(entity), WebGet.this.downloadResMimeType, sourceDeclaredEnc);
        };
    }

    /**
     * Converts the buffer if needed
     */
    private byte[] convertBufferToDefEncoding(byte[] bbuf, String bufferMimeType, String bufferSourceEncoding) throws IOException {
        if (!bufferMimeType.equals(Context.PDF_MIMETYPE) &&
                !bufferMimeType.equals(Context.IMAGE_MIMETYPE)) {
            EbookEncodingUtils eu = new EbookEncodingUtils();
            byte[] bRes;

            // if it has to determine the source encoding
            if (bufferSourceEncoding == null) {
                bRes = eu.convertNativeEncoding(bbuf, Context.DEF_ENCODING);
                sourceDeclaredEnc = eu.getDetectedEncoding();
            }
            // if it is imposed the source encoding
            else {
                bRes = EbookEncodingUtils.convertDeclaredEncoding(bbuf, bufferSourceEncoding, Context.DEF_ENCODING);
            }
            return bRes;
        }
        return bbuf;
    }


    private HttpRequestRetryHandler getRetryHandler(int executionCount) {

        return (exception, executionCount1, context) -> {

            if (executionCount1 >= Context.MAX_WEBGET_RETRY) {
                // Does not retry if over max retry count
                Context.getInstance().getLogger().error("Retry max number reached: i won't retry!");
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                Context.getInstance().getLogger().error("NoHttpResponseException: i will retry!");
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                Context.getInstance().getLogger().error("SSLHandshakeException: i won't retry!");
                return false;
            }
            return true;

        };
    }

    public String getUrlString() {
        return urlString;
    }

    public String getSourceEncoding() {
        return sourceDeclaredEnc;
    }

}
