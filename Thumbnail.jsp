<%@ page import="ext.Thumbnail.GetPartThumbnail" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>WTPart Thumbnail Viewer</title>
    <style>
        /* General Page Styles */
        body {
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background: #e9ebee;
            margin: 0;
            padding: 0;
        }

        /* Container */
        .container {
            width: 550px;
            margin: 50px auto;
            background: #fff;
            border-radius: 15px;
            box-shadow: 0 12px 25px rgba(0,0,0,0.15);
            overflow: hidden;
            transition: transform 0.3s ease;
        }

        .container:hover {
            transform: translateY(-5px);
        }

        /* Header */
        .header {
            background: linear-gradient(90deg, #1877f2, #145dbf);
            color: #fff;
            text-align: center;
            padding: 30px;
            font-size: 26px;
            font-weight: 700;
            letter-spacing: 1px;
            text-shadow: 1px 1px 3px rgba(0,0,0,0.2);
        }

        /* Form Section */
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
            transition: all 0.3s ease;
        }

        input[type="text"]:focus {
            outline: none;
            border-color: #1877f2;
            box-shadow: 0 0 8px rgba(24,119,242,0.4);
        }

        input[type="submit"] {
            padding: 12px 20px;
            font-size: 16px;
            color: #fff;
            background-color: #1877f2;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: 600;
        }

        input[type="submit"]:hover {
            background-color: #145dbf;
            box-shadow: 0 4px 10px rgba(0,0,0,0.2);
        }

        /* Thumbnail Card */
        .thumbnail-card {
            text-align: center;
            margin: 25px;
            padding: 20px;
            background: #f0f2f5;
            border-radius: 12px;
            box-shadow: 0 6px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .thumbnail-card:hover {
            transform: scale(1.02);
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
        }

        .thumbnail-card h3 {
            margin-bottom: 15px;
            color: #1877f2;
            font-size: 22px;
            font-weight: 600;
            text-shadow: 0 1px 2px rgba(0,0,0,0.1);
        }

        .thumbnail-card img {
            max-width: 100%;
            border-radius: 10px;
            border: 1px solid #dcdde1;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .thumbnail-card img:hover {
            transform: scale(1.05);
            box-shadow: 0 6px 15px rgba(0,0,0,0.2);
        }

        .error {
            color: #e84118;
            text-align: center;
            font-weight: bold;
            margin-top: 20px;
        }

    </style>
</head>
<body>
<div class="container">
    <div class="header">WTPart Thumbnail Viewer</div>

    <div class="form-section">
        <form method="get">
            <input type="text" name="partNumber" placeholder="Enter WTPart number (e.g. WCDS000288)" />
            <input type="submit" value="Show Thumbnail"/>
        </form>
    </div>

    <%
        String partNumber = request.getParameter("partNumber");
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            try {
                String thumbnailUrl = GetPartThumbnail.getThumbnailURL(partNumber);
    %>
                <div class="thumbnail-card">
                    <h3>WTPart: <%= partNumber %></h3>
                    <img src="<%= thumbnailUrl %>" alt="WTPart Thumbnail"/>
                </div>
    <%
            } catch (Exception e) {
    %>
                <p class="error">Error: <%= e.getMessage() %></p>
    <%
            }
        }
    %>

</div>
</body>
</html>
