<%@ page import="ext.Customization.GetPartDetailsWithThumbnail" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
<title>WTPart Thumbnail & Details – 4 Part Compare</title>

<style>
    body {
        font-family: Segoe UI, Arial;
        background: #eef1f5;
    }

    .container {
        width: 1400px;
        margin: 30px auto;
        background: #ffffff;
        border-radius: 12px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.15);
    }

    .header {
        background: #1877f2;
        color: #fff;
        padding: 18px;
        font-size: 22px;
        font-weight: 600;
        text-align: center;
        border-radius: 12px 12px 0 0;
    }

    .compare-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 15px;
        padding: 20px;
    }

    .column {
        background: #f7f9fc;
        border-radius: 10px;
        padding: 15px;
        text-align: center;
    }

    .column input[type=text] {
        width: 95%;
        padding: 8px;
        margin-bottom: 10px;
        border-radius: 6px;
        border: 1px solid #ccc;
        font-size: 14px;
    }

    .column img {
        max-width: 100%;
        border-radius: 8px;
        border: 1px solid #ccc;
        margin: 10px 0;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 10px;
    }

    td {
        padding: 6px;
        border-bottom: 1px solid #ddd;
        font-size: 13px;
        text-align: left;
    }

    td:first-child {
        font-weight: 600;
        width: 45%;
    }

    .compare-btn {
        text-align: center;
        padding-bottom: 20px;
    }

    .compare-btn input {
        padding: 10px 22px;
        background: #1877f2;
        border: none;
        color: #fff;
        font-weight: 600;
        border-radius: 6px;
        cursor: pointer;
    }

    .error {
        color: red;
        font-weight: bold;
        margin-top: 10px;
    }
</style>
</head>

<body>

<div class="container">
    <div class="header">WTPart Thumbnail & Details Comparison</div>

<form method="get">

<div class="compare-grid">

<%
    String[] parts = {
        request.getParameter("part1"),
        request.getParameter("part2"),
        request.getParameter("part3"),
        request.getParameter("part4")
    };

    for (int i = 0; i < 4; i++) {
        String partNumber = parts[i];
%>

    <!-- COLUMN -->
    <div class="column">

        <input type="text" name="part<%= (i + 1) %>"
               placeholder="WTPart <%= (i + 1) %>"
               value="<%= partNumber != null ? partNumber : "" %>" />

<%
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            try {
                String thumbnailUrl =
                        GetPartDetailsWithThumbnail.getThumbnailURL(partNumber);

                Map<String, String> partDetails =
                        GetPartDetailsWithThumbnail.getPartDetails(partNumber);
%>

        <h4><%= partNumber %></h4>

        <img src="<%= thumbnailUrl %>" alt="WTPart Thumbnail"/>

        <table>
        <%
            for (Map.Entry<String, String> entry : partDetails.entrySet()) {
        %>
            <tr>
                <td><%= entry.getKey() %></td>
                <td><%= entry.getValue() %></td>
            </tr>
        <%
            }
        %>
        </table>

<%
            } catch (Exception e) {
%>
        <div class="error">Unable to load part</div>
<%
            }
        }
%>

    </div>

<%
    }
%>

</div>

<div class="compare-btn">
    <input type="submit" value="Compare Parts"/>
</div>

</form>

</div>

</body>
</html>
