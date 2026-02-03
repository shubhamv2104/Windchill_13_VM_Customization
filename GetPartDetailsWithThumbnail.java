package ext.Customization;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.VersionReference;

import java.util.LinkedHashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetPartDetailsWithThumbnail {

    /* =======================================================
       1️⃣ Get WTPart (LATEST ITERATION ONLY)
       ======================================================= */
    public static WTPart getLatestWTPart(String partNumber) throws Exception {

        WTPart part = null;

        QuerySpec qs = new QuerySpec(WTPart.class);

        qs.appendWhere(
            (WhereExpression) new SearchCondition(
                WTPart.class,
                "master>number",
                SearchCondition.EQUAL,
                partNumber
            ),
            new int[]{0, 1}
        );

        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (!qr.hasMoreElements()) {
            throw new Exception("WTPart not found : " + partNumber);
        }

        part = (WTPart) qr.nextElement();

        // 🔥 KEY STEP – get latest iteration
        part = (WTPart) VersionControlHelper.service
                .getLatestIteration((Iterated) part, true);

        return part;
    }

    /* =======================================================
       2️⃣ Get Version Reference (LATEST ITERATION)
       ======================================================= */
    public static String getVR(String partNumber) throws Exception {

        WTPart part = getLatestWTPart(partNumber);

        VersionReference vr =
            VersionReference.newVersionReference(part);

        return new ReferenceFactory().getReferenceString(vr);
    }

    /* =======================================================
       3️⃣ Get OData Thumbnail (NO fallback here)
       ======================================================= */
    public static String getThumbnailURL(String partNumber) throws Exception {

        String vr = getVR(partNumber);

        String username = "wcadmin";
        String password = "ptc";

        String odataUrl =
            "http://windchill-dev.srvlearning.net/Windchill/servlet/odata/ProdMgmt/Parts('"
            + vr +
            "')?$expand=Thumbnails($select=PTC.ApplicationData/Content)";

        URL url = new URL(odataUrl);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        String auth = username + ":" + password;
        String encodedAuth =
            Base64.getEncoder().encodeToString(auth.getBytes());

        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            return null;
        }

        BufferedReader br =
            new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        JSONObject json = new JSONObject(sb.toString());
        JSONArray thumbs = json.getJSONArray("Thumbnails");

        if (thumbs.length() == 0) {
            return null;
        }

        JSONObject content =
            thumbs.getJSONObject(0).getJSONObject("Content");

        return content.getString("URL");
    }

    /* =======================================================
       4️⃣ Get Part Details (LATEST ITERATION, ORDERED)
       ======================================================= */
    public static Map<String, String> getPartDetails(String partNumber)
            throws Exception {

        WTPart part = getLatestWTPart(partNumber);

        Map<String, String> details = new LinkedHashMap<>();

        details.put("Number", part.getNumber());
        details.put("Name", part.getName());
        details.put(
            "Version",
            VersionControlHelper.getVersionIdentifier(part).getValue()
        );
        details.put(
            "Iteration",
            part.getIterationIdentifier().getValue()
        );
        details.put(
            "State",
            part.getLifeCycleState().toString()
        );
        details.put(
            "Created By",
            part.getCreatorName()
        );
        details.put(
            "Last Modified",
            part.getModifyTimestamp().toString()
        );

        return details;
    }
}
