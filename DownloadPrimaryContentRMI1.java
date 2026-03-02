package ext.Customization;

import java.beans.PropertyVetoException;
import java.io.File;

import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;

public class DownloadPrimaryContentRMI1 implements RemoteAccess {

    /**
     * Server-side execution method
     */
    public static void downloadDocument(String docNumber) throws Exception {

        try {

            String downloadPath =
                    "C:\\ptc\\Windchill_13.0\\Windchill\\temp\\PrimaryContentDownloads";

            File folder = new File(downloadPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            WTDocument doc = getWTDocumentByNumber(docNumber);

            if (doc == null) {
                System.out.println("Error: WTDocument not found -> " + docNumber);
                return;
            }

            ApplicationData appData = getPrimaryContent(doc);

            if (appData == null) {
                System.out.println("Error: Primary content not found for -> " + docNumber);
                return;
            }

            String fullPath = downloadPath + File.separator + appData.getFileName();

            ContentServerHelper.service.writeContentStream(appData, fullPath);

            System.out.println("File downloaded successfully at: " + fullPath);

        } catch (Exception e) {
            System.out.println("Error while downloading document -> " + docNumber);
            e.printStackTrace();
        }
    }

    /**
     * Fetch WTDocument by Number
     */
    public static WTDocument getWTDocumentByNumber(String docNumber)
            throws WTException {

        QuerySpec qs = new QuerySpec(WTDocument.class);
        qs.appendWhere(
                new SearchCondition(WTDocument.class,
                        WTDocument.NUMBER,
                        SearchCondition.EQUAL,
                        docNumber),
                new int[]{0});

        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (qr.hasMoreElements()) {

            WTDocument doc = (WTDocument) qr.nextElement();

            doc = (WTDocument) VersionControlHelper.service
                    .getLatestIteration((Iterated) doc, false);

            return doc;
        }

        return null;
    }

    /**
     * Get Primary Content
     */
    public static ApplicationData getPrimaryContent(WTDocument doc)
            throws WTException, PropertyVetoException {

        FormatContentHolder holder =
                (FormatContentHolder) ContentHelper.service.getContents(doc);

        return (ApplicationData) ContentHelper.getPrimary(holder);
    }

    /**
     * Main Method (Shell Execution)
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please provide WTDocument Number as argument.");
            return;
        }

        String docNumber = args[0];

        try {

            if (!RemoteMethodServer.ServerFlag) {

                RemoteMethodServer rms = RemoteMethodServer.getDefault();

                rms.invoke(
                        "downloadDocument",
                        "ext.Customization.DownloadPrimaryContentRMI1",
                        null,
                        new Class[]{String.class},
                        new Object[]{docNumber}
                );

            } else {

                downloadDocument(docNumber);
            }

        } catch (Exception e) {
            System.out.println("RMI Invocation Error.");
            e.printStackTrace();
        }
    }
}