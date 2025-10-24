<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONArray, org.json.JSONObject"%>
<%@ page import="ext.BomComparisionTool.BOMReportUnified"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Windchill - SAP BOM Comparison</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #f4f6f9; margin:0; padding:0; }
        .navbar { background: linear-gradient(90deg,#004085,#0069d9); padding:15px 25px; color:white; font-size:22px; font-weight:bold; box-shadow:0 2px 5px rgba(0,0,0,0.2);}
        .container { max-width: 1200px; margin:auto; padding:25px; }
        h3 { text-align:center; font-size:28px; font-weight:700; margin-bottom:20px; color:#004085; }
        table { width:100%; border-collapse: collapse; background: #fff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.1);}
        th, td { padding:10px 12px; text-align:left; font-size:14px; border-bottom:1px solid #ddd; }
        th { background:#004085; color:white; cursor:pointer; position:relative; user-select:none; }
        th .arrow { font-size:10px; margin-left:5px; opacity:0.7; }
        tr:hover { background:#eef6ff; transition:0.2s; }
        .orange { color:orange; font-weight:bold; }
        .red { color:red; font-weight:bold; }
        .download-btn { padding:10px 18px; background: linear-gradient(90deg,#28a745,#218838); color:white; border:none; border-radius:5px; cursor:pointer; font-weight:bold; margin-bottom:10px; box-shadow:0 2px 4px rgba(0,0,0,0.2); }
        .download-btn:hover { background: linear-gradient(90deg,#218838,#1e7e34); }
    </style>
    <script>
        // ---------- Table Sorting ----------
        let sortDir = {};
        function sortTable(n) {
            var table = document.getElementById("bomTable");
            var rows = Array.from(table.tBodies[0].rows);
            var dir = sortDir[n] === "asc" ? "desc" : "asc";
            sortDir[n] = dir;

            rows.sort(function(a,b){
                var x = a.cells[n].innerText.toLowerCase();
                var y = b.cells[n].innerText.toLowerCase();
                if(!isNaN(x) && !isNaN(y)) { x = Number(x); y = Number(y); }
                return dir==="asc" ? (x>y?1:-1) : (x<y?1:-1);
            });

            rows.forEach(r => table.tBodies[0].appendChild(r));

            // Update arrows
            var headers = table.querySelectorAll("th");
            headers.forEach((th,i)=>{ th.querySelector(".arrow").innerHTML=""; });
            headers[n].querySelector(".arrow").innerHTML = dir==="asc" ? "▲" : "▼";
        }

        // ---------- Download CSV ----------
        function downloadCSV() {
            var table = document.getElementById("bomTable");
            var rows = table.querySelectorAll("tr");
            var csv = [];
            rows.forEach(r => {
                var cols = r.querySelectorAll("td,th");
                var row = [];
                cols.forEach(c => { row.push('"' + c.innerText.replace(/"/g,'""') + '"'); });
                csv.push(row.join(","));
            });
            var blob = new Blob([csv.join("\n")], {type:"text/csv"});
            var link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = "BOM_Comparison_Report.csv";
            link.click();
        }
    </script>
</head>
<body>
<div class="navbar">Windchill - SAP BOM Comparison</div>
<div class="container">
    <h3>BOM Comparison Report</h3>
    <button class="download-btn" onclick="downloadCSV()">⬇ Download Report</button>
    <table id="bomTable">
        <thead>
            <tr>
                <th onclick="sortTable(0)">WC Line <span class="arrow"></span></th>
                <th onclick="sortTable(1)">WC Parent <span class="arrow"></span></th>
                <th onclick="sortTable(2)">WC Child <span class="arrow"></span></th>
                <th onclick="sortTable(3)">WC Qty <span class="arrow"></span></th>
                <th onclick="sortTable(4)">SAP Item <span class="arrow"></span></th>
                <th onclick="sortTable(5)">SAP Component <span class="arrow"></span></th>
                <th onclick="sortTable(6)">SAP Qty <span class="arrow"></span></th>
                <th onclick="sortTable(7)">Result <span class="arrow"></span></th>
            </tr>
        </thead>
        <tbody>
        <%
            try {
                JSONArray report = BOMReportUnified.generateBOMReport("WCDS000642","C:/custom/SAPBOM.xlsx");
                for(int i=0;i<report.length();i++){
                    JSONObject row = report.getJSONObject(i);
                    String result = row.optString("Result");
        %>
            <tr>
                <td><%= row.optString("WCLine") %></td>
                <td><%= row.optString("WCParent") %></td>
                <td><%= row.optString("WCChild") %></td>
                <td><%= row.optDouble("WCQty") %></td>
                <td><%= row.optString("SAPItem") %></td>
                <td><%= row.optString("SAPComponent") %></td>
                <td><%= row.optDouble("SAPQty") %></td>
                <td class="<%= result.equals("Match") ? "orange" : "red" %>"><%= result %></td>
            </tr>
        <%
                }
            } catch(Exception e) {
                out.println("<tr><td colspan='8'>Error: "+e.getMessage()+"</td></tr>");
            }
        %>
        </tbody>
    </table>
</div>
</body>
</html>
