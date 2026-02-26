

/* Created RMI utility to establish ConfigurableRevisionLink between DesignPart and DesignDocument using custom link subtype net.srvlearning.PartToRequirementLink. Includes latest iteration retrieval, duplicate validation, and transaction handling.

---------------------------------------------------------------------------------------------------
STEPS:

1.Site → Utilities → Type and Attribute Management

2.Created subtype under ConfigurableRevisionLink → net.srvlearning.PartToRequirementLink

3.Configured Roles:

	Role A → net.srvlearning.DesignPart

	Role B → net.srvlearning.DesignDocument

4.Set Relationship Constraints:

	Parent → DesignPart

	Child → DesignDocument

-------------------------------------------------------------------------------------------------------

OUTPUT:
Main method running...
Server Response: SUCCESS: Link created. OID = wt.configurablelink.ConfigurableRevisionLink:30197811

-------------------------------------------------------------------------------------------------------

VALIDATE RESULT VIA DATABASE:
	SELECT *
	FROM wind.ConfigurableRevisionLink
	WHERE IDA2A2 = 30197811;
	
-------------------------------------------------------------------------------------------------------

@Author - Shubham V

*/

package ext.Customization;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.VersionControlHelper;
import wt.configurablelink.ConfigurableRevisionLink;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.pom.Transaction;
import wt.util.WTException;

public class PartDocLinkTest implements RemoteAccess {

    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    private static final String LINK_TYPE =
            "wt.configurablelink.ConfigurableRevisionLink|net.srvlearning.PartToRequirementLink";

    public static void main(String[] args) throws Exception {

        RemoteMethodServer rms = RemoteMethodServer.getDefault();
        rms.setUserName(USERNAME);
        rms.setPassword(PASSWORD);

        System.out.println("Main method running...");

        Object result = rms.invoke("runTest",
                PartDocLinkTest.class.getName(),
                null,
                new Class[]{},
                new Object[]{});

        System.out.println("Server Response: " + result);
    }

    public static String runTest() {

        Transaction trx = new Transaction();

        try {
            trx.start();

            WTPart part = getLatestPart("D-0050026");
            WTDocument doc = getLatestDocument("0000170055");

            if (part == null) {
                return "ERROR: Part not found";
            }

            if (doc == null) {
                return "ERROR: Document not found";
            }


            TypeDefinitionReference typeRef =
                    TypedUtilityServiceHelper.service
                            .getTypeDefinitionReference(LINK_TYPE);

            if (typeRef == null) {
                return "ERROR: Link Type not found";
            }

            // Duplicate check
            QueryResult qr = PersistenceHelper.manager.navigate(
                    part,
                    ConfigurableRevisionLink.LINKED_FROM_ROLE,
                    ConfigurableRevisionLink.class,
                    false);

            while (qr.hasMoreElements()) {
                ConfigurableRevisionLink existing =
                        (ConfigurableRevisionLink) qr.nextElement();

                if (existing.getRoleBObject().equals(doc)) {
                    trx.commit();
                    return "Link already exists";
                }
            }

            // Create link
            ConfigurableRevisionLink link =
                    ConfigurableRevisionLink.newConfigurableRevisionLink(
                            part,
                            doc,
                            typeRef);

            link = (ConfigurableRevisionLink)
                    PersistenceHelper.manager.save(link);

            trx.commit();

            return "SUCCESS: Link created. OID = " +
                    link.getPersistInfo().getObjectIdentifier();

        } catch (Exception e) {
            trx.rollback();
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    private static WTPart getLatestPart(String number) throws Exception {

        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(
                WTPart.class,
                WTPart.NUMBER,
                SearchCondition.EQUAL,
                number), new int[]{0});

        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (qr.hasMoreElements()) {
            WTPart part = (WTPart) qr.nextElement();
            return (WTPart) VersionControlHelper.service
                    .getLatestIteration(part, true);
        }
        return null;
    }

    private static WTDocument getLatestDocument(String number) throws Exception {

        QuerySpec qs = new QuerySpec(WTDocument.class);
        qs.appendWhere(new SearchCondition(
                WTDocument.class,
                WTDocument.NUMBER,
                SearchCondition.EQUAL,
                number), new int[]{0});

        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (qr.hasMoreElements()) {
            WTDocument doc = (WTDocument) qr.nextElement();
            return (WTDocument) VersionControlHelper.service
                    .getLatestIteration(doc, true);
        }
        return null;
    }
}