package ext.BomComparisionTool;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.method.RemoteMethodServer;

public class BOMReportUnified {

    private static JSONArray windchillJsonArray = new JSONArray();

    // Windchill credentials
    private static final String USERNAME = "wcadmin";
    private static final String PASSWORD = "ptc";

    // ------------------ CMD Testing ------------------
    public static void main(String[] args) {
        try {
            RemoteMethodServer rms = RemoteMethodServer.getDefault();
            rms.setUserName(USERNAME);
            rms.setPassword(PASSWORD);

            String partNumber = "WCDS000642";
            String sapExcelPath = "C:/custom/SAPBOM.xlsx";

            JSONArray report = generateBOMReport(partNumber, sapExcelPath);

            System.out.printf("%-10s %-12s %-15s %-8s %-12s %-15s %-8s %-20s\n",
                    "WC Line", "WC Parent", "WC Child", "WC Qty", "SAP Item", "SAP Component", "SAP Qty", "Result");
            System.out.println("----------------------------------------------------------------------------------------");

            for (int i = 0; i < report.length(); i++) {
                JSONObject row = report.getJSONObject(i);
                String wcLine = row.optString("WCLine");
                String wcParent = row.optString("WCParent");
                String wcChild = row.optString("WCChild");
                double wcQty = row.optDouble("WCQty");
                String sapItem = row.optString("SAPItem");
                String sapComponent = row.optString("SAPComponent");
                double sapQty = row.optDouble("SAPQty");
                String result = row.optString("Result");

                System.out.printf("%-10s %-12s %-15s %-8.2f %-12s %-15s %-8.2f %-20s\n",
                        wcLine, wcParent, wcChild, wcQty, sapItem, sapComponent, sapQty, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------ JSP Method ------------------
    public static JSONArray generateBOMReport(String partNumber, String sapExcelPath) throws Exception {
        runReport(partNumber);

        String windchillJson = getBOMAsJson();
        String sapJson = getSAPJsonFromExcel(sapExcelPath);

        JSONArray windchillArray = new JSONArray(windchillJson);
        JSONArray sapArray = new JSONArray(sapJson);

        JSONArray reportArray = new JSONArray();
        Set<String> matchedSAP = new HashSet<>();
        Set<String> printedRows = new HashSet<>();

        // WC → SAP
        for (int i = 0; i < windchillArray.length(); i++) {
            JSONObject wcObj = windchillArray.getJSONObject(i);
            String wcLine = String.valueOf(wcObj.optLong("LineNumber", 0));
            String wcParent = wcObj.optString("ParentPartNumber");
            String wcChild = wcObj.optString("ChildPartNumber");
            double wcQtyVal = wcObj.optDouble("Quantity", 0.0);

            String result = "Not Present in SAP";
            String sapItem = "-";
            String sapComponent = "-";
            double sapQtyVal = 0.0;

            for (int j = 0; j < sapArray.length(); j++) {
                JSONObject sapObj = sapArray.getJSONObject(j);
                String currentItem = sapObj.optString("Item").trim();
                if (currentItem.isEmpty()) currentItem = "0";
                String currentComponent = sapObj.optString("Component").trim();
                double currentQtyVal = sapObj.has("Quantity") && !sapObj.optString("Quantity").isEmpty()
                        ? Double.parseDouble(sapObj.optString("Quantity")) : 0.0;

                String key = currentItem + "-" + currentComponent;

                if (!wcLine.equals("0")) {
                    if (wcLine.equals(currentItem) && wcChild.equals(currentComponent)) {
                        result = (wcQtyVal == currentQtyVal) ? "Match" : "Quantity Mismatch";
                        sapItem = currentItem; sapComponent = currentComponent; sapQtyVal = currentQtyVal;
                        matchedSAP.add(key); break;
                    }
                    if (!wcLine.equals(currentItem) && wcChild.equals(currentComponent)) {
                        result = (wcQtyVal == currentQtyVal) ? "Line Number Mismatch" : "Quantity & Line Mismatch";
                        sapItem = currentItem; sapComponent = currentComponent; sapQtyVal = currentQtyVal;
                        matchedSAP.add(key); break;
                    }
                    if (wcLine.equals(currentItem) && !wcChild.equals(currentComponent)) {
                        result = "Part Number Mismatch";
                        sapItem = currentItem; sapComponent = currentComponent; sapQtyVal = currentQtyVal;
                        matchedSAP.add(key); break;
                    }
                } else {
                    if (wcChild.equals(currentComponent)) {
                        result = (wcQtyVal == currentQtyVal) ? "Match" : "Quantity Mismatch";
                        sapItem = currentItem; sapComponent = currentComponent; sapQtyVal = currentQtyVal;
                        matchedSAP.add(key); break;
                    }
                }
            }

            String rowKey = wcLine + "-" + wcChild + "-" + sapItem + "-" + sapComponent;
            if (!printedRows.contains(rowKey)) {
                JSONObject row = new JSONObject();
                row.put("WCLine", wcLine);
                row.put("WCParent", wcParent);
                row.put("WCChild", wcChild);
                row.put("WCQty", wcQtyVal);
                row.put("SAPItem", sapItem);
                row.put("SAPComponent", sapComponent);
                row.put("SAPQty", sapQtyVal);
                row.put("Result", result);
                reportArray.put(row);
                printedRows.add(rowKey);
            }
        }

        // SAP → WC
        for (int j = 0; j < sapArray.length(); j++) {
            JSONObject sapObj = sapArray.getJSONObject(j);
            String sapItem = sapObj.optString("Item").trim();
            if (sapItem.isEmpty()) sapItem = "0";
            String sapComponent = sapObj.optString("Component").trim();
            double sapQtyVal = sapObj.has("Quantity") && !sapObj.optString("Quantity").isEmpty()
                    ? Double.parseDouble(sapObj.optString("Quantity")) : 0.0;

            String key = sapItem + "-" + sapComponent;
            if (!matchedSAP.contains(key)) {
                JSONObject row = new JSONObject();
                row.put("WCLine", "-");
                row.put("WCParent", "-");
                row.put("WCChild", "-");
                row.put("WCQty", 0);
                row.put("SAPItem", sapItem);
                row.put("SAPComponent", sapComponent);
                row.put("SAPQty", sapQtyVal);
                row.put("Result", "Not Present in Windchill");
                reportArray.put(row);
            }
        }

        return reportArray;
    }

    // ------------------ Existing helper methods ------------------
    public static void runReport(String partNumber) throws Exception {
        windchillJsonArray = new JSONArray();
        WTPart rootPart = getPartByNumber(partNumber);
        if (rootPart == null) return;
        Set<WTPart> visited = new HashSet<>();
        fetchChildrenRecursive(rootPart, visited);
    }

    private static void fetchChildrenRecursive(WTPart parent, Set<WTPart> visited) throws Exception {
        parent = (WTPart) VersionControlHelper.service.getLatestIteration(parent, true);
        if (visited.contains(parent)) return;
        visited.add(parent);

        QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(parent);
        while (qr.hasMoreElements()) {
            WTPartUsageLink usageLink = (WTPartUsageLink) qr.nextElement();
            WTPartMaster childMaster = usageLink.getUses();
            WTPart childPart = getLatestPartIteration(childMaster);

            long orgLineNumber = usageLink.getLineNumber() != null ? usageLink.getLineNumber().getValue() : 0L;
            double qtyVal = usageLink.getQuantity() != null ? usageLink.getQuantity().getAmount() : 0.0;

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("ParentPartNumber", parent.getNumber());
            jsonObj.put("ChildPartNumber", childMaster.getNumber());
            jsonObj.put("LineNumber", orgLineNumber);
            jsonObj.put("Quantity", qtyVal);

            windchillJsonArray.put(jsonObj);

            if (childPart != null) fetchChildrenRecursive(childPart, visited);
        }
    }

    private static WTPart getLatestPartIteration(WTPartMaster master) throws Exception {
        QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
        WTPart latest = null;
        while (qr.hasMoreElements()) {
            Iterated iter = (Iterated) qr.nextElement();
            if (iter instanceof WTPart) latest = (WTPart) iter;
        }
        return latest;
    }

    private static WTPart getPartByNumber(String number) throws Exception {
        WTPart wtpart = null;
        QuerySpec qspecPart = new QuerySpec(WTPart.class);
        qspecPart.appendWhere((WhereExpression) new SearchCondition(WTPart.class, "master>number", "=", number), new int[]{0,1});
        QueryResult qrPart = PersistenceHelper.manager.find(qspecPart);
        if (qrPart.hasMoreElements()) wtpart = (WTPart) qrPart.nextElement();
        if (wtpart != null) wtpart = (WTPart) VersionControlHelper.service.getLatestIteration((Iterated) wtpart, true);
        return wtpart;
    }

    public static String getBOMAsJson() {
        return windchillJsonArray.toString();
    }

    public static String getSAPJsonFromExcel(String excelPath) throws Exception {
        JSONArray sapArray = new JSONArray();
        try (FileInputStream fis = new FileInputStream(new File(excelPath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.hasNext() ? rowIterator.next() : null;
            if (headerRow == null) return "[]";

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue().trim());

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                JSONObject jsonObj = new JSONObject();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = "";
                    switch (cell.getCellType()) {
                        case STRING: value = cell.getStringCellValue(); break;
                        case NUMERIC: value = String.valueOf((long) cell.getNumericCellValue()); break;
                        case BOOLEAN: value = String.valueOf(cell.getBooleanCellValue()); break;
                        default: value = "0"; break;
                    }
                    jsonObj.put(headers.get(i), value);
                }
                sapArray.put(jsonObj);
            }
        }
        return sapArray.toString();
    }
}
