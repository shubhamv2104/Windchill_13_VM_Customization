package ext.Customization;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.VersionControlHelper;
import wt.vc.VersionReference;
import wt.fc.ReferenceFactory;

import java.util.HashMap;
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

    // Get WTPart by number
    public static WTPart getWTPart(String partNumber) throws Exception {
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(
            new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, partNumber),
            null
        );

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (!qr.hasMoreElements()) {
            throw new Exception("No WTPart found for number: " + partNumber);
        }
        return (WTPart) qr.nextElement();
    }

    // Get Version Reference string
    public static String getVR(String partNumber) throws Exception {
        WTPart part = getWTPart(partNumber);
        VersionReference vr = VersionReference.newVersionReference(part);
        ReferenceFactory rf = new ReferenceFactory();
        return rf.getReferenceString(vr);
    }

    // Fetch thumbnail URL via OData
    public static String getThumbnailURL(String partNumber) throws Exception {

        String vr = getVR(partNumber);

        String username = "wcadmin";
        String password = "ptc";

        String odataUrl =
            "http://windchill-dev.srvlearning.net/Windchill/servlet/odata/ProdMgmt/Parts('"
            + vr +
            "')?$expand=Thumbnails($select=PTC.ApplicationData/Content)";

        URL url = new URL(odataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new Exception("Failed to fetch thumbnail from OData");
        }

        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        JSONObject json = new JSONObject(sb.toString());
        JSONArray thumbs = json.getJSONArray("Thumbnails");

        if (thumbs.length() == 0) {
            throw new Exception("No thumbnail available for this part");
        }

        JSONObject content =
            thumbs.getJSONObject(0)
                  .getJSONObject("Content");

        return content.getString("URL");
    }

    // Get Part basic details
    public static Map<String, String> getPartDetails(String partNumber) throws Exception {

        WTPart part = getWTPart(partNumber);

        Map<String, String> details = new LinkedHashMap<>();

        // EXACT order as requested
        details.put("Number", part.getNumber());
        details.put("Name", part.getName());
        details.put("Version", VersionControlHelper.getVersionIdentifier(part).getValue());
        details.put("Iteration", part.getIterationIdentifier().getValue());
        details.put("State", part.getLifeCycleState().toString());
        details.put("Created By", part.getCreatorName());
        details.put("Last Modified", part.getModifyTimestamp().toString());

        return details;
    }

}
