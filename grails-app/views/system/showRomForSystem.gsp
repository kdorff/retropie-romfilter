<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Show ROM Details for ${gameDetails?.name}</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="showRomForSystem.js" asset-defer="true" />
    <asset:stylesheet src="showRomForSystem.css"/>
</head>
<body>
    <p>Navigation:
        <g:link mapping="listSystems">Systems</g:link> |
        <g:link mapping="listRomsForSystem" params="[system: system]">${system}</g:link> |
        <g:link mapping="romForSystem" params="[system: system, id: gameDetails.scrapeId]">${gameDetails.name}</g:link>

    <table id="singleRomTable">
            <tr><th>System</th><td>${gameDetails?.system}</td></tr>
            <tr><th>Name</th><td>${gameDetails?.name}</td></tr>
            <tr><th>Filename</th><td>${gameDetails?.path}</td></tr>
            <tr><th>Descruption</th><td>${gameDetails?.desc}</td></tr>
            <tr><th>Developer</th><td>${gameDetails?.developer}</td></tr>
            <tr><th>Publisher</th><td>${gameDetails?.publisher}</td></tr>
            <tr><th>Genre</th><td>${gameDetails?.genre}</td></tr>
            <tr><th>Players</th><td>${gameDetails?.players}</td></tr>
            <tr><th>Region</th><td>${gameDetails?.region}</td></tr>
            <tr><th>Rom Type</th><td>${gameDetails?.romtype}</td></tr>
            <tr><th>Release Date</th><td>${gameDetails?.releasedate}</td></tr>
            <tr><th>Rating</th><td>${gameDetails?.rating}</td></tr>
            <tr><th>Play Count</th><td>${gameDetails?.playcount}</td></tr>
            <tr><th>Last Played</th><td>${gameDetails?.lastplayed}</td></tr>
            <tr><th>Scrape Id</th><td>${gameDetails?.scrapeId}</td></tr>
            <tr><th>Scrape Source</th><td>${gameDetails?.scrapeSource}</td></tr>
            <tr><th>Image</th><td>
                <g:if test="${gameDetails && gameDetails.image && gameDetails.system && gameDetails.scrapeId}">
                    <img src='<g:createLink mapping="showRomImageForSystem" params="[system: gameDetails.system, id: gameDetails.id]"/>' />
                </g:if>
            </td></tr>
    </table>
</body>
</html>
