<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of Systems</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
<p>Listing roms for system ${system}
    <table>
    <g:each var="filename" in="${gamelist}">
        <% def gameDetails = filenameToDetails[filename] %>
            <tr>
                <th>Filename</th>
                <td>${filename}</td>
            </tr>
            <g:if test="${gameDetails}">
                <tr>
                    <th width='10%'>Name</th>
                    <td>${gameDetails.name}</td>
                </tr>
                <tr>
                    <th>Size</th>
                    <td>${gameDetails.size}</td>
                </tr>
                <tr>
                    <th>Desc</th>
                    <td>${gameDetails.desc}</td>
                </tr>
                <tr>
                    <th>Genre</th>
                    <td>${gameDetails.genre}</td>
                </tr>
            </g:if>
    </g:each>
    </table>
</body>
</html>
