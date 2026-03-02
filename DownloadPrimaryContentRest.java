package ext.Customization;

import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.query.QuerySpec;
import wt.query.SearchCondition;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

import org.json.JSONObject;

public class DownloadPrimaryContentRest {

    // Step 1: Get OR reference from WTDocument number
    public static String getOR(String docNumber) throws Exception {

        QuerySpec qs = new QuerySpec(WTDocument.class);
        qs.appendWhere(
                new SearchCondition(WTDocument.class,
                        WTDocument.NUMBER,
                        SearchCondition.EQUAL,
                        docNumber),
                null);

        QueryResult qr = PersistenceHelper.manager.find(qs);

        if (!qr.hasMoreElements()) {
            throw new Exception("No WTDocument found for number: " + docNumber);
        }

        WTDocument doc = (WTDocument) qr.nextElement();

        ReferenceFactory rf = new ReferenceFactory();
        return rf.getReferenceString(doc);  // OR:wt.doc.WTDocument:xxxx
    }

    // Step 2: Call OData to get PrimaryContent Redirect URL
    public static String getPrimaryContentURL(String docNumber) throws Exception {

        String or = getOR(docNumber);

        // 🔥 Proper encoding of OR string
        String encodedOR = URLEncoder.encode(or, "UTF-8");

        String username = "wcadmin";
        String password = "ptc";

        String odataUrl =
                "http://windchill-dev.srvlearning.net/Windchill/servlet/odata/v6/DocMgmt/Documents('"
                        + encodedOR
                        + "')?$expand=PrimaryContent";

        URL url = new URL(odataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {

            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line);
            }
            errorReader.close();

            throw new Exception("OData call failed. HTTP Code: "
                    + responseCode + " Response: " + errorMsg.toString());
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

        JSONObject primary = json.getJSONObject("PrimaryContent");
        JSONObject content = primary.getJSONObject("Content");

        return content.getString("URL");  // RedirectDownload URL
    }

    // Step 3: Download file to server location
    public static void downloadPrimaryContent(String docNumber) throws Exception {

        String fileUrl = getPrimaryContentURL(docNumber);

        String username = "wcadmin";
        String password = "ptc";

        String downloadPath =
                "C:\\ptc\\Windchill_13.0\\Windchill\\temp\\PrimaryContentDownloads";

        File dir = new File(downloadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        InputStream in = conn.getInputStream();

        // Extract filename safely
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
                .split("\\?")[0];

        File outputFile = new File(downloadPath + File.separator + fileName);
        FileOutputStream out = new FileOutputStream(outputFile);

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        in.close();
        out.close();

        System.out.println("Primary content downloaded at: "
                + outputFile.getAbsolutePath());
    }

    // Main method (Shell execution)
    public static void main(String[] args) {

        try {

            if (args.length == 0) {
                System.out.println("Please provide Document Number.");
                return;
            }

            downloadPrimaryContent(args[0]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}