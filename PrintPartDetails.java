package ext.LearnCustomization;

import wt.method.RemoteMethodServer;
import wt.method.RemoteAccess;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

public class PrintPartDetails implements RemoteAccess {

    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    // ---------------- CMD ENTRY ----------------
    public static void main(String[] args) {
        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(USERNAME);
            rms.setPassword(PASSWORD);

            String result = (String) rms.invoke(
                    "getPartDetails",
                    "ext.LearnCustomization.PrintPartDetails",
                    null,
                    new Class[]{ String.class },
                    new Object[]{ "0000030024" }
            );

            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- METHOD SERVER ----------------
    @SuppressWarnings("deprecation")
    public static String getPartDetails(String partNumber) throws Exception {

        StringBuilder output = new StringBuilder();

        // 1️⃣ Find part by number (any iteration)
        WTPart part = null;

        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(
                (WhereExpression) new SearchCondition(
                        WTPart.class,
                        "master>number",
                        SearchCondition.EQUAL,
                        partNumber
                ),
                new int[]{ 0, 1 }
        );

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (!qr.hasMoreElements()) {
            return "❌ Part not found : " + partNumber;
        }

        part = (WTPart) qr.nextElement();

        // 2️⃣ 🔥 GET LATEST ITERATION (SAME AS LatestIteration CLASS)
        part = (WTPart) VersionControlHelper.service
                .getLatestIteration((Iterated) part, true);

        // 3️⃣ PART DETAILS
        output.append("Part Found\n")
              .append("----------------------------\n")
              .append("Number      : ").append(part.getNumber()).append("\n")
              .append("Name        : ").append(part.getName()).append("\n")
              .append("Version     : ")
              .append(part.getVersionIdentifier().getValue())
              .append(".")
              .append(part.getIterationIdentifier().getValue())
              .append("\n")
              .append("State       : ")
              .append(part.getLifeCycleState().getDisplay())
              .append("\n")
              .append("Created By  : ")
              .append(part.getCreatorFullName())
              .append("\n\n");

        // 4️⃣ DESCRIBED BY DOCUMENTS
        output.append("Described By Documents\n")
              .append("----------------------------\n");

        QueryResult describedDocs =
                WTPartHelper.service.getDescribedByDocuments(part);

        if (!describedDocs.hasMoreElements()) {
            output.append("No described-by documents found.\n");
        }	

        while (describedDocs.hasMoreElements()) {
            WTDocument doc = (WTDocument) describedDocs.nextElement();

            output.append("Doc Number : ")
                  .append(doc.getNumber())
                  .append(", Name : ")
                  .append(doc.getName())
                  .append(", Version : ")
                  .append(doc.getVersionIdentifier().getValue())
                  .append(".")
                  .append(doc.getIterationIdentifier().getValue())
                  .append("\n");
        }

        output.append("\n");

     // ---------- REFERENCE DOCUMENTS ----------
        output.append("Reference Documents\n")
              .append("----------------------------\n");

        QueryResult refDocs =
                WTPartHelper.service.getReferencesWTDocumentMasters(part);

        if (!refDocs.hasMoreElements()) {
            output.append("No referenced documents found.\n");
        }

        while (refDocs.hasMoreElements()) {

            WTDocumentMaster docMaster =
                    (WTDocumentMaster) refDocs.nextElement();

            // 1️⃣ Get any document iteration from master
            QueryResult docQR =
                    VersionControlHelper.service.allIterationsOf(docMaster);

            if (!docQR.hasMoreElements()) {
                continue;
            }

            WTDocument doc = null;
            while (docQR.hasMoreElements()) {
                doc = (WTDocument) docQR.nextElement();
            }

            // 2️⃣ Ensure latest iteration (same logic as part)
            doc = (WTDocument) VersionControlHelper.service
                    .getLatestIteration(doc, true);

            // 3️⃣ Print details
            output.append("Doc Number : ")
                  .append(doc.getNumber())
                  .append(", Name : ")
                  .append(doc.getName())
                  .append(", Version : ")
                  .append(doc.getVersionIdentifier().getValue())
                  .append(".")
                  .append(doc.getIterationIdentifier().getValue())
                  .append("\n");
        }

        return output.toString();
    }
}
