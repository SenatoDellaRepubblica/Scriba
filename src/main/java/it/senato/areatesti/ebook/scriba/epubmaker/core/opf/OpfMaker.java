package it.senato.areatesti.ebook.scriba.epubmaker.core.opf;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.epubmaker.core.OpsUtils;
import it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean.*;
import it.senato.areatesti.ebook.scriba.misc.xml.XmlUtils;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;

/**
 * Manages the OPF xml file
 *
 * http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm
 */
public class OpfMaker {
    private String uniqueId;

    public static String parseAndPrint(String opfXml) {
        try {
            Document doc = XmlUtils.parseXmlFile(opfXml);
            opfXml = XmlUtils.prettyFormatXml(doc);
            return opfXml;
        } catch (TransformerException e) {
            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * Creates an OPF file starting form the ContentsMap structure
     *
     * @return the Opf file
     */
    public String makeOpf(ContentList contentList, MetadataList metadataList) {
        // DC in the Metadata section
        ArrayList<OpfDcItem> diList = new ArrayList<>();
        for (IItem item : metadataList.getIntList()) {
            if (item instanceof MetadataItem) {
                MetadataItem m = (MetadataItem) item;
                if (m.getElemType().equals("dc")) {
                    OpfDcItem mi;
                    if (m.getDcId() != null || m.getRole() != null)
                        mi = new OpfDcItem(m.getElemName(), m.getElemVal(),
                                m.getDcId(), m.getRole());
                    else
                        mi = new OpfDcItem(m.getElemName(), m.getElemVal());

                    diList.add(mi);
                }
            }
        }

        // Meta in the Metadata section
        ArrayList<OpfNcxMetaItem> miList = OpsUtils.loadMetaItemList(metadataList, "opf");

        // Item in Manifest section
        ArrayList<OpfManifestItem> mfList = new ArrayList<>();
        for (IItem item : contentList.getIntList()) {
            if (item instanceof ContentItem) {
                ContentItem c = (ContentItem) item;

                // In the OPF file I have to escape the path
                String packagePath = c.getPackagePathEscaped();
                mfList.add(new OpfManifestItem(c.getPackageId(), packagePath
                        + c.getPackageFile(), c.getContentMediaType()));
            }
        }

        // Itemref in Spine section
        ArrayList<OpfSpineItem> spList = new ArrayList<>();
        for (IItem item : contentList.getIntList())
            if (item instanceof ContentItem) {
                ContentItem c = (ContentItem) item;
                if (c.isInSpine()) {
                    String linear = "yes";
                    if (!c.IsSpineLinear())
                        linear = "no";
                    spList.add(new OpfSpineItem(c.getPackageId(), linear));
                }
            }

        // Finds the cover reference
        String coverHref = null;
        for (IItem item : contentList.getIntList())
            if (item instanceof ContentItem) {
                ContentItem c = (ContentItem) item;
                if (c.isCover())
                    coverHref = c.getPackageFile();
            }

        // Guide section
        OpfGuideContent gc = null;
        if (coverHref != null)
            gc = new OpfGuideContent("Copertina", coverHref);

        // Creates the XML file
        String opfXml = createDocument(diList, miList, mfList, spList,
                contentList.getTocId(), gc);

        //Context.getInstance().getLogger().debug(opfXml);

        // Now pretty prints the XML string
        return parseAndPrint(opfXml);
    }

    /**
     * Creates the Opf structure
     */
    private String createDocument(ArrayList<OpfDcItem> dcBeanList,
                                  ArrayList<OpfNcxMetaItem> metaBeanList,
                                  ArrayList<OpfManifestItem> manifestItemList,
                                  ArrayList<OpfSpineItem> spineItemList, String tocId,
                                  OpfGuideContent guideContent) {
        String metadata = createMetadata(dcBeanList, metaBeanList);
        String manifest = createManifest(manifestItemList);
        String spine = createSpine(spineItemList, tocId);
        String guide = createGuide(guideContent);

        String pack = createPackage(metadata, manifest, spine, guide);

        return createDoc(pack);
    }

    private String createGuide(OpfGuideContent guideContent) {
        if (guideContent != null)
            return guideContent.toString();
        return "";
    }

    private String createSpine(ArrayList<OpfSpineItem> spineItemList,
                               String tocId) {
        StringBuilder sb = new StringBuilder();

        sb.append("<spine toc=\"").append(tocId).append("\">").append(Context.NEWLINE);
        for (OpfSpineItem spineItem : spineItemList)
            sb.append(spineItem.toString()).append(Context.NEWLINE);
        sb.append("</spine>").append(Context.NEWLINE);
        return sb.toString();
    }

    /**
     * Creates the manifest part
     */
    private String createManifest(ArrayList<OpfManifestItem> manifestItemList) {
        StringBuilder sb = new StringBuilder();

        sb.append("<manifest>").append(Context.NEWLINE);
        for (OpfManifestItem manifestItem : manifestItemList)
            sb.append(manifestItem.toString()).append(Context.NEWLINE);
        sb.append("</manifest>").append(Context.NEWLINE);
        return sb.toString();
    }

    /**
     * Creates the Metadata part
     */
    private String createMetadata(ArrayList<OpfDcItem> dcBeanList, ArrayList<OpfNcxMetaItem> metaBeanList) {
        StringBuilder sb = new StringBuilder();
        sb.append("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:opf=\"http://www.idpf.org/2007/opf\">");
        sb.append(Context.NEWLINE);

        for (OpfDcItem dcbean : dcBeanList) {
            if (dcbean.getIdAttributeVal() != null)
                this.uniqueId = dcbean.getIdAttributeVal();
            // the first ID I find it is the unique ID
            sb.append(dcbean.toString()).append(Context.NEWLINE);
        }

        for (OpfNcxMetaItem metabean : metaBeanList)
            sb.append(metabean.toString()).append(Context.NEWLINE);

        sb.append("</metadata>").append(Context.NEWLINE);

        return sb.toString();
    }

    /**
     * Creates the Package part
     */
    private String createPackage(String metadata, String manifest,
                                 String spine, String guide) {

        return ("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\""
                + this.uniqueId + "\" version=\"2.0\">") +
                Context.NEWLINE + Context.NEWLINE +
                metadata + Context.NEWLINE +
                manifest + Context.NEWLINE +
                spine + Context.NEWLINE +
                guide + Context.NEWLINE +
                "</package>";
    }

    private String createDoc(String pack) {

        return "<?xml version=\"1.0\" encoding=\"" + Context.DEF_ENCODING + "\"?>" +
                Context.NEWLINE +
                pack + Context.NEWLINE;
    }

}
