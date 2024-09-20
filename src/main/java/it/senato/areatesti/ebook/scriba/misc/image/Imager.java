package it.senato.areatesti.ebook.scriba.misc.image;

import com.mortennobel.imagescaling.AdvancedResizeOp.UnsharpenMask;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import it.senato.areatesti.ebook.scriba.Context;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * To manage Images
 */
public class Imager {
    private static final String PNG_MIMETYPE = "image/png";

    private static final String GIF_MIMETYPE = "image/gif";

    private static final String JPEG_MIMETYPE = "image/jpeg";

    private static final String PNG_EXT = "png";

    private static final String GIF_EXT = "gif";

    private static final String JPEG_EXT = "jpg";


    /**
     * Convert Mimetype to extension
     */
    public static String fromMimeType2Ext(String mimeType) {

        switch (mimeType)
        {
            case PNG_MIMETYPE:
                return PNG_EXT;

            case GIF_MIMETYPE:
                return GIF_EXT;

            case JPEG_MIMETYPE:
                return JPEG_EXT;

            default:
                return "none";
        }
    }

    public static String fromExt2MimeType(String ext) {
        switch(ext)
        {
            case PNG_EXT:
                return PNG_MIMETYPE;

            case GIF_EXT:
                return GIF_MIMETYPE;

            case JPEG_EXT:
                return JPEG_MIMETYPE;

            default:
                return "none";
        }
    }

    /**
     * Resizes an image
     *
     * @return the new fileName
     * @see "http://code.google.com/p/java-image-scaling/wiki/Getting_started"
     * @see "http://www.jhlabs.com/ip/filters/download.html"
     */
    public static String resizeImage(String imageFileName) throws IOException {
        //Context.getInstance().getLogger().debug("Resize of: "+imageFileName);
        int rw = Context.RESIZED_WIDTH;

        // XXX: JAI ImageIO patch? http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4821108
        BufferedImage image;

        int counter = 0;
        while (true) {
            try {
                if (counter > Context.MAX_RETRY_IMAGEIOO_READ) {
                    return imageFileName;
                }

                image = ImageIO.read(new File(imageFileName));
                break;
            } catch (IIOException e) {
                Context.getInstance().getLogger().error("Retrying to read the image (" + counter + ")");
                counter++;
            }
        }

        ResampleOp resampleOp = new ResampleOp(rw, (rw * image.getHeight()) / image.getWidth());

        resampleOp.setUnsharpenMask(UnsharpenMask.Normal);
        resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
        image = resampleOp.filter(image, null);

        File tmpFile = new File(imageFileName);
        ImageIO.write(image, Context.IMAGE_FORMAT, tmpFile);

        Context.getInstance().getLogger().debug("Resize of: " + imageFileName + " ended!");
        return imageFileName;
    }
}
