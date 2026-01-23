package ext.LearnCustomization;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

public class LatestIteration implements RemoteAccess {

    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    // ---------------- CMD TEST ----------------
    public static void main(String[] args) {
        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(USERNAME);
            rms.setPassword(PASSWORD);

            String result = (String) rms.invoke(
                    "printLatestIteration",
                    "ext.LearnCustomization.LatestIteration",
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
    public static String printLatestIteration(String partNumber) throws Exception {

        // 1️⃣ Get part by number (any iteration)
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

        // 2️⃣ 🔥 THIS IS THE KEY (YOUR WORKING LOGIC)
        part = (WTPart) VersionControlHelper.service
                .getLatestIteration((Iterated) part, true);

        // 3️⃣ Print ONLY required details
        return "Part Found\n"
                + "----------------------------\n"
                + "Number    : " + part.getNumber() + "\n"
                + "Name      : " + part.getName() + "\n"
                + "Iteration : "
                + part.getVersionIdentifier().getValue() + "."
                + part.getIterationIdentifier().getValue();
    }
}
