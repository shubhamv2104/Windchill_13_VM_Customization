package ext.Thumbnail;

import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.VersionReference;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;

public class GetWTPartVR implements RemoteAccess {

    public static void main(String[] args) {
        String username = "wcadmin";
        String password = "ptc";

        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(username);
            rms.setPassword(password);

            // ✅ Pass part number here
            String partNumber = "WCDS000288";

            rms.invoke(
                "printPartVR",
                GetWTPartVR.class.getName(),
                null,
                new Class[]{String.class},
                new Object[]{partNumber}
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printPartVR(String partNumber) throws Exception {
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, partNumber), null);

        QueryResult qr = wt.fc.PersistenceHelper.manager.find(qs);

        if (qr.hasMoreElements()) {
            WTPart part = (WTPart) qr.nextElement();

            VersionReference verRef = VersionReference.newVersionReference(part);
            ReferenceFactory rf = new ReferenceFactory();
            String refString = rf.getReferenceString(verRef);

            System.out.println("WTPart Number: " + part.getNumber());
            System.out.println("WTPart Name  : " + part.getName());
            System.out.println("VersionReference (VR): " + refString);
        } else {
            System.out.println("No WTPart found with number: " + partNumber);
        }
    }
}
