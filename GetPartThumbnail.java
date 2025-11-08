package ext.Thumbnail;

import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.PersistenceHelper;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.vc.VersionReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetPartThumbnail {

    // Get VR from WTPart number
    public static String getVR(String partNumber) throws Exception {
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, partNumber), null);
        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (!qr.hasMoreElements()) {
            throw new Exception("No WTPart found for number: " + partNumber);
        }

        WTPart part = (WTPart) qr.nextElement();
        VersionReference verRef = VersionReference.newVersionReference(part);
        ReferenceFactory rf = new ReferenceFactory();
        return rf.getReferenceString(verRef); // VR:wt.part.WTPart:xxxx
    }

    // Fetch the thumbnail URL from OData
    public static String getThumbnailURL(String partNumber) throws Exception {
        String vr = getVR(partNumber);
        String username = "wcadmin";
        String password = "ptc";

        String odataUrl = "http://windchill-dev.srvlearning.net/Windchill/servlet/odata/ProdMgmt/Parts('"
                + vr
                + "')?$select=Identity&$expand=Thumbnails($select=FormatIcon,PTC.ApplicationData/Content)";

        // Call OData API
        URL url = new URL(odataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Basic Auth
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("OData API call failed with HTTP code: " + code + "\nURL tried: " + odataUrl);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JSONObject json = new JSONObject(sb.toString());
        JSONArray thumbs = json.getJSONArray("Thumbnails");
        if (thumbs.length() == 0) throw new Exception("No Thumbnails found for part: " + partNumber);

        JSONObject content = thumbs.getJSONObject(0).getJSONObject("Content");
        return content.getString("URL"); // full download URL
    }
}
