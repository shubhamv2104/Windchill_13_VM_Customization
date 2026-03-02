<%@ page import="ext.Customization.DownloadPrimaryContentRest" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>WTDocument Primary Content Downloader</title>
    <style>
        body {
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background: #e9ebee;
            margin: 0;
            padding: 0;
        }

        .container {
            width: 550px;
            margin: 50px auto;
            background: #fff;
            border-radius: 15px;
            box-shadow: 0 12px 25px rgba(0,0,0,0.15);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(90deg, #1877f2, #145dbf);
            color: #fff;
            text-align: center;
            padding: 30px;
            font-size: 26px;
            font-weight: 700;
        }

        .form-section {
            padding: 25px;
            display: flex;
            flex-direction: column;
            gap: 15px;
        }

        input[type="text"] {
            padding: 12px 15px;
            font-size: 16px;
            border: 1px solid #ccd0d5;
            border-radius: 8px;
        }

        input[type="submit"] {
            padding: 12px 20px;
            font-size: 16px;
            color: #fff;
            background-color: #1877f2;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-weight: 600;
        }

        .success {
            color: green;
            text-align: center;
            font-weight: bold;
            margin: 20px;
        }

        .error {
            color: red;
            text-align: center;
            font-weight: bold;
            margin: 20px;
        }
    </style>
</head>
<body>

<div class="container">
    <div class="header">WTDocument Primary Content Downloader</div>

    <div class="form-section">
        <form method="post">
            <input type="text" name="docNumber" placeholder="Enter Document Number (e.g. 0000170058)" required />
            <input type="submit" value="Download Primary Content"/>
        </form>
    </div>

    <%
        String docNumber = request.getParameter("docNumber");

        if (docNumber != null && !docNumber.trim().isEmpty()) {
            try {
                DownloadPrimaryContentRest.downloadPrimaryContent(docNumber.trim());
    %>
                <div class="success">
                    Primary content downloaded successfully for Document: <%= docNumber %><br/>
                    Location: C:\ptc\Windchill_13.0\Windchill\temp\PrimaryContentDownloads
                </div>
    <%
            } catch (Exception e) {
    %>
                <div class="error">
                    Error: <%= e.getMessage() %>
                </div>
    <%
            }
        }
    %>

</div>

</body>
</html>