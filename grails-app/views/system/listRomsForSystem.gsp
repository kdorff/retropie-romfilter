<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of Systems</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="listRomsForSystem.js" asset-defer="true" />
    <asset:stylesheet src="listRomsForSystem.css"/>
</head>
<body>
    <p>Listing roms for system ${system}
    <table id="romTable">
        <thead>
            <tr>
                <th>Filename /<br/>Scrape name</th>
                <th>Name</th>
                <th>Genre</th>
                <th>Desc</th>
            </tr>
        </thead>
        <tbody>
            <g:each var="filename" in="${gamelist}">
                <tr>
                    <% def gameDetails = filenameToDetails[filename] %>
                    <td>${filename}</td>
                    <td>${gameDetails?.name}</td>
                    <td>${gameDetails?.genre}</td>
                    <td>${gameDetails?.desc}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
</body>
</html>
