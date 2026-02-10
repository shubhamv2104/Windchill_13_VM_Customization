package ext.BomComparisionTool;

import java.util.HashSet;
import java.util.Set;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.part.LineNumber;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

public class EBOMTestRunner1 implements RemoteAccess {
    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    public static void main(String[] args) throws Exception {
        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(USERNAME);
            rms.setPassword(PASSWORD);
            System.out.println("Main method running");
            runTest();
        } catch (Exception e) {
            System.out.println("In a catch block, exception is");
            e.printStackTrace();
        }
    }

    public static void runTest() throws WTException {
        System.out.println("In runTest Method");
        String partNumber = "WCDS000642"; //Enter your part number

        WTPart rootPart = getPartByNumber(partNumber);
        if (rootPart == null) {
            System.out.println("Part not found: " + partNumber);
            return;
        }

        System.out.println("\nEBOM for Part: " + rootPart.getNumber() + " (" + rootPart.getName() + ")\n");

        // Print header in required format
        System.out.println(String.format("%-20s %-30s %-10s %-10s %-30s %-20s %-12s %-10s",
                "Parent Part Number", "Parent Part Name", "Version", "Iteration",
                "Child Part Name", "Child Part Number", "Line Number", "Quantity"));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        Set<WTPart> visited = new HashSet<>();
        fetchChildrenRecursive(rootPart, visited);
    }

    private static void fetchChildrenRecursive(WTPart parent, Set<WTPart> visited) throws WTException {
        // Always resolve parent to latest iteration
        parent = (WTPart) VersionControlHelper.service.getLatestIteration(parent, true);

        if (visited.contains(parent)) return;
        visited.add(parent);

        QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(parent);
        while (qr.hasMoreElements()) {
            WTPartUsageLink usageLink = (WTPartUsageLink) qr.nextElement();
            WTPartMaster childMaster = usageLink.getUses();

            // Always resolve child to latest iteration
            WTPart childPart = getLatestPartIteration(childMaster);

            LineNumber lineNum = usageLink.getLineNumber();
            long orgLineNumber = (lineNum != null) ? lineNum.getValue() : 0L;

            Quantity qty = usageLink.getQuantity();
            double qtyVal = (qty != null) ? qty.getAmount() : 0.0;

            // Print in required tabular format
            System.out.println(String.format("%-20s %-30s %-10s %-10s %-30s %-20s %-12d %-10.2f",
                    parent.getNumber(),
                    parent.getName(),
                    parent.getVersionIdentifier().getValue(),
                    parent.getIterationIdentifier().getValue(),
                    (childPart != null ? childPart.getName() : "N/A"),
                    childMaster.getNumber(),
                    orgLineNumber,
                    qtyVal
            ));

            if (childPart != null) {
                fetchChildrenRecursive(childPart, visited);
            }
        }
    }

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
        try {
            QuerySpec qspecPart = new QuerySpec(WTPart.class);
            qspecPart.appendWhere((WhereExpression) new SearchCondition(WTPart.class, "master>number", "=", number),
                    new int[]{0, 1});
            QueryResult qrPart = PersistenceHelper.manager.find(qspecPart);
            if (qrPart.hasMoreElements()) {
                wtpart = (WTPart) qrPart.nextElement();
                // resolve latest iteration of the found part
                wtpart = (WTPart) VersionControlHelper.service.getLatestIteration((Iterated) wtpart, true);
                return wtpart;
            } else {
                System.out.println("PART DOES NOT EXIST!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in getPart: " + e);
        }
        return wtpart;
    }
}
