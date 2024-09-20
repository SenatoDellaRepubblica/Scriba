package it.senato.areatesti.ebook.scriba.packaging;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static it.senato.areatesti.ebook.scriba.Context.DEF_ENCODING;

/**
 * Utils class for the Pdf (eBook) packager
 *
 * @author roberto.battistoni
 */
class PdfPackagerUtils {
    private final Document pdfDocument;
    private final PdfWriter pdfWriter;

    /**
     * Constructor
     */
    PdfPackagerUtils(Document pdfDocument, PdfWriter pdfWriter) {
        this.pdfDocument = pdfDocument;
        this.pdfWriter = pdfWriter;
    }

    /**
     * Get the pipeline for the XMLWorker
     */
    private Pipeline<?> getPipeline(String cssToUse) throws IOException {
        FontFactory.registerDirectories();

        CssFilesImpl cssFiles = new CssFilesImpl();
        cssFiles.add(XMLWorkerHelper.getCSS(IOUtils.toInputStream(cssToUse, DEF_ENCODING)));
        StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver(cssFiles);
        HtmlPipelineContext hpc = new HtmlPipelineContext(null);

        hpc.setAcceptUnknown(true).autoBookmark(false).setTagFactory(Tags.getHtmlTagProcessorFactory());
        HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(pdfDocument, pdfWriter));

        return new CssResolverPipeline(cssResolver, htmlPipeline);
    }

	/*
	private Pipeline<?> getFilterToDocument(String cssToUse)
	{
		FontFactory.registerDirectories();
	
		XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider();
		CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
		HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
		 
		//CSSResolver cssResolver = new StyleAttrCSSResolver();
		CSSResolver cssResolver =XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
		try
		{
			cssResolver.addCss(cssToUse, true);
			//Context.getInstance().getLogger().debug("CSS: "+Context.pdfCss);
		} catch (CssResolverException e)
		{
			Context.getInstance().getLogger().error(e);
		}
		
		
		Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
										new HtmlPipeline(htmlContext,
											new PdfWriterPipeline(pdfDocument, pdfWriter)));
	
		return pipeline;
	}
	*/

    /**
     * Get an XMLWorker instance
     */
    XMLWorker getXmlWorkerInstance(String css) throws IOException {
        return new XMLWorker(getPipeline(css), true);
    }

    /**
     * Adds new lines
     */
    void addManyNewLines(int n) throws DocumentException {
        for (int i = 0; i < n; i++)
            pdfDocument.add(Chunk.NEWLINE);
    }

}
