package ext.Thumbnail;

import java.util.ArrayList;
import java.util.List;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.log4j.Logger;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representation;
import wt.util.WTException;
import com.ptc.wvs.common.ui.VisualizationHelper;
import wt.representation.Representable;

/**
 * PartRepresentationHelper - Fetch Part Representations & Thumbnails
 */
public class PartRepresentationHelper {

    private static final Logger LOGGER = (Logger) LogR.getLogger(PartRepresentationHelper.class.getName());

    /**
     * Holds part number and list of thumbnail URLs
     */
    public static class PartRep {
        private final String partNumber;
        private final List<String> thumbnailURLs = new ArrayList<>();

        public PartRep(String partNumber) {
            this.partNumber = partNumber;
        }

        public void addThumbnailURL(String url) {
            thumbnailURLs.add(url);
        }

        public List<String> getThumbnailURLs() {
            return thumbnailURLs;
        }

        public String getPartNumber() {
            return partNumber;
        }
    }

    /**
     * Fetch PartRep object for a given part number
     */
    public static PartRep getPartRepresentation(String partNumber) throws WTException {
        LOGGER.info("Fetching representation for part number: " + partNumber);

        WTPart part = findWTPartByNumber(partNumber);
        if (part == null) {
            LOGGER.warn("No WTPart found for part number: " + partNumber);
            return null;
        }

        PartRep partRep = new PartRep(partNumber);
        VisualizationHelper vizHelper = new VisualizationHelper();

        // Use findRepresentable() for safety; handles CAD linked parts too
        Representable repable = vizHelper.findRepresentable(part);
        if (repable == null) {
            LOGGER.warn("No representable found for part: " + partNumber);
            return partRep;
        }

        // Fetch default representation for the representable
        Representation rep = vizHelper.getRepresentation(repable);
        if (rep == null) {
            LOGGER.warn("No representation found for part: " + partNumber);
            return partRep;
        }

        LOGGER.info("Found representation for part: " + partNumber);

        // Fetch thumbnails from standard THUMBNAIL role
        extractThumbnailContents(rep, ContentRoleType.THUMBNAIL, partRep);

        // Also check other potential roles for images
        ContentRoleType[] otherRoles = {
            ContentRoleType.PRIMARY,
            ContentRoleType.SECONDARY
        };
        for (ContentRoleType role : otherRoles) {
            extractThumbnailContents(rep, role, partRep);
        }

        return partRep;
    }

    /**
     * Extracts thumbnail/image URLs from a representation for a given role
     */
    private static void extractThumbnailContents(Representation rep, ContentRoleType role, PartRep partRep) throws WTException {
        QueryResult contents = ContentHelper.service.getContentsByRole(rep, role);
        while (contents.hasMoreElements()) {
            Object obj = contents.nextElement();
            if (obj instanceof ApplicationData) {
                ApplicationData appData = (ApplicationData) obj;
                String fileName = appData.getFileName().toLowerCase();
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
                    fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                    String url = appData.getViewContentURL(rep).toString();
                    partRep.addThumbnailURL(url);
                    LOGGER.info("[IMAGE FOUND] Role: " + role + " - " + url);
                }
            }
        }
    }

    /**
     * Find WTPart by part number
     */
    private static WTPart findWTPartByNumber(String partNumber) throws WTException {
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, partNumber));
        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr.hasMoreElements()) {
            return (WTPart) qr.nextElement();
        }
        return null;
    }
}
