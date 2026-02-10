package ext.Customization;

import java.util.HashSet;
import java.util.Set;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.LineNumber;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.wip.WorkInProgressHelper;

public class MultiLevelBOMWithWhereUsed implements RemoteAccess {

    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(USERNAME);
            rms.setPassword(PASSWORD);

            runReport();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- REPORT ----------------
    public static void runReport() throws WTException {

        String partNumber = "WCDS000642";
        WTPart rootPart = getPartByNumber(partNumber);

        if (rootPart == null) {
            System.out.println("❌ Part not found: " + partNumber);
            return;
        }

        System.out.println("\n================================================================================================================");
        System.out.println("EBOM + WHERE USED REPORT");
        System.out.println("ROOT PART : " + rootPart.getNumber()
                + " (" + rootPart.getVersionIdentifier().getValue()
                + "." + rootPart.getIterationIdentifier().getValue() + ")");
        System.out.println("================================================================================================================");

        System.out.printf("%-6s %-20s %-20s %-8s %-8s %-60s\n",
                "LEVEL", "PARENT PART", "CHILD PART", "LINE", "QTY", "WHERE USED");

        System.out.println("================================================================================================================");

        Set<WTPart> visited = new HashSet<>();
        fetchChildrenRecursive(rootPart, visited, 0);

        System.out.println("================================================================================================================");
    }

    // ---------------- MULTI LEVEL EBOM ----------------
    private static void fetchChildrenRecursive(
            WTPart parent,
            Set<WTPart> visited,
            int level
    ) throws WTException {

        parent = (WTPart) VersionControlHelper.service.getLatestIteration(parent, true);
        if (visited.contains(parent)) return;
        visited.add(parent);

        QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(parent);

        while (qr.hasMoreElements()) {

            WTPartUsageLink usageLink = (WTPartUsageLink) qr.nextElement();
            WTPartMaster childMaster = usageLink.getUses();
            WTPart childPart = getLatestPartIteration(childMaster);

            LineNumber lineNum = usageLink.getLineNumber();
            long lineNumber = (lineNum != null) ? lineNum.getValue() : 0L;

            Quantity qty = usageLink.getQuantity();
            double qtyVal = (qty != null) ? qty.getAmount() : 0.0;

            String whereUsed = getWhereUsedLatest(childPart);

            System.out.printf("%-6d %-20s %-20s %-8d %-8.2f %-60s\n",
                    level,
                    parent.getNumber(),
                    childMaster.getNumber(),
                    lineNumber,
                    qtyVal,
                    whereUsed
            );

            if (childPart != null) {
                fetchChildrenRecursive(childPart, visited, level + 1);
            }
        }
    }

    // ---------------- WHERE USED (SUPPORTED) ----------------
    private static String getWhereUsedLatest(WTPart part) throws WTException {

        if (part == null) return "-";

        QueryResult qr = StructHelper.service.navigateUsedByToIteration(
                part,
                true,
                new LatestConfigSpec()   // ✅ correct constructor
        );

        if (!qr.hasMoreElements()) return "-";

        StringBuilder sb = new StringBuilder();

        while (qr.hasMoreElements()) {
            WTPart parent = (WTPart) qr.nextElement();

            boolean isWC = WorkInProgressHelper.isWorkingCopy(parent);

            sb.append(parent.getNumber())
              .append(" (")
              .append(parent.getVersionIdentifier().getValue())
              .append(".")
              .append(parent.getIterationIdentifier().getValue())
              .append(isWC ? " In-Work" : "")
              .append("), ");
        }

        return sb.substring(0, sb.length() - 2);
    }

    // ---------------- HELPERS ----------------
    private static WTPart getLatestPartIteration(WTPartMaster master) throws WTException {

        QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
        WTPart latest = null;

        while (qr.hasMoreElements()) {
            Iterated iter = (Iterated) qr.nextElement();
            if (iter instanceof WTPart) {
                latest = (WTPart) iter;
            }
        }
        return latest;
    }

    private static WTPart getPartByNumber(String number) throws WTException {

        WTPart wtpart = null;

        QuerySpec qspec = new QuerySpec(WTPart.class);
        qspec.appendWhere(
                (WhereExpression) new SearchCondition(
                        WTPart.class,
                        "master>number",
                        SearchCondition.EQUAL,
                        number),
                new int[]{0, 1}
        );

        QueryResult qr = PersistenceHelper.manager.find(qspec);

        if (qr.hasMoreElements()) {
            wtpart = (WTPart) qr.nextElement();
            wtpart = (WTPart) VersionControlHelper.service
                    .getLatestIteration((Iterated) wtpart, true);
        }
        return wtpart;
    }
}
