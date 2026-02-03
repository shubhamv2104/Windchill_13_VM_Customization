<%@ page import="ext.Customization.GetPartDetailsWithThumbnail" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
<title>WTPart Thumbnail & Details – 4 Part Compare</title>

<style>
    body {
        font-family: "Segoe UI", Roboto, Arial, sans-serif;
        background: linear-gradient(135deg, #e6ebf2, #f5f7fb);
        margin: 0;
        padding: 0;
    }

    .container {
        width: 1450px;
        margin: 30px auto;
        background: #ffffff;
        border-radius: 16px;
        box-shadow: 0 15px 40px rgba(0,0,0,0.15);
        overflow: hidden;
    }

    .header {
        background: linear-gradient(90deg, #1877f2, #145dbf);
        color: #fff;
        padding: 22px;
        font-size: 24px;
        font-weight: 700;
        text-align: center;
        letter-spacing: 0.5px;
    }

    .compare-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        padding: 25px;
    }

    .column {
        background: #f9fbff;
        border-radius: 14px;
        padding: 18px;
        text-align: center;
        box-shadow: 0 6px 15px rgba(0,0,0,0.08);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .column:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 25px rgba(0,0,0,0.15);
    }

    .column input {
        width: 100%;
        padding: 10px 12px;
        border-radius: 8px;
        border: 1px solid #cfd6e4;
        font-size: 14px;
        margin-bottom: 12px;
        transition: border-color 0.3s ease, box-shadow 0.3s ease;
    }

    .column input:focus {
        outline: none;
        border-color: #1877f2;
        box-shadow: 0 0 6px rgba(24,119,242,0.4);
    }

    .column h4 {
        margin: 10px 0;
        color: #1877f2;
        font-size: 16px;
        font-weight: 600;
    }

    .column img {
        max-width: 100%;
        border-radius: 10px;
        border: 1px solid #d8dde8;
        margin: 12px 0;
        background: #fff;
        transition: transform 0.3s ease;
    }

    .column img:hover {
        transform: scale(1.03);
    }

    table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 12px;
        background: #ffffff;
        border-radius: 10px;
        overflow: hidden;
    }

    table tr:nth-child(even) {
        background: #f4f7fc;
    }

    td {
        padding: 8px 10px;
        font-size: 13px;
        border-bottom: 1px solid #e2e6ef;
        color: #333;
    }

    td:first-child {
        font-weight: 600;
        width: 45%;
        color: #555;
    }

    .compare-btn {
        position: sticky;
        bottom: 0;
        background: #ffffff;
        padding: 18px;
        text-align: center;
        border-top: 1px solid #e0e4ee;
    }

    .compare-btn input {
        padding: 12px 30px;
        background: linear-gradient(90deg, #1877f2, #145dbf);
        border: none;
        color: #fff;
        font-size: 15px;
        font-weight: 600;
        border-radius: 10px;
        cursor: pointer;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .compare-btn input:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 15px rgba(0,0,0,0.25);
    }
</style>
</head>

<body>

<div class="container">
<div class="header">WTPart Thumbnail & Details Comparison</div>

<form method="get">

<%
    String fallbackImage =
        "/Windchill/netmarkets/images/ThumbnailwithDetails/Thumbnail1.jpg";

    String[] parts = {
        request.getParameter("part1"),
        request.getParameter("part2"),
        request.getParameter("part3"),
        request.getParameter("part4")
    };
%>

<div class="compare-grid">

<%
    for (int i = 0; i < 4; i++) {
        String partNumber = parts[i];
%>

<div class="column">

    <input type="text"
           name="part<%= (i + 1) %>"
           placeholder="WTPart <%= (i + 1) %>"
           value="<%= partNumber != null ? partNumber : "" %>" />

<%
    if (partNumber != null && !partNumber.trim().isEmpty()) {

        String thumbnailUrl =
            GetPartDetailsWithThumbnail.getThumbnailURL(partNumber);

        String imageToShow =
            (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty())
            ? thumbnailUrl
            : fallbackImage;

        Map<String, String> partDetails =
            GetPartDetailsWithThumbnail.getPartDetails(partNumber);
%>

    <h4><%= partNumber %></h4>

    <img src="<%= imageToShow %>" alt="WTPart Thumbnail"/>

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
