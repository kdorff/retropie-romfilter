<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Show ROM Details for ${gamelistEntry?.name}</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="showRomForSystem.js" asset-defer="true" />
    <asset:stylesheet src="showRomForSystem.css"/>
</head>
<body>
    <p>Navigation:
        <g:link mapping="listSystems">Systems</g:link> |
        <g:link mapping="listRomsForSystem" params="[system: system]">${system}</g:link> |
        <g:link mapping="romForSystem" params="[system: system, hash: gamelistEntry.hash]">${gamelistEntry.name}</g:link>

    <table id="singleRomTable">
            <tr><th>System</th><td>${gamelistEntry?.system}</td></tr>
            <tr><th>Name</th><td>${gamelistEntry?.name}</td></tr>
            <tr><th>Filename</th><td>${gamelistEntry?.path}</td></tr>
            <tr><th>Descruption</th><td>${gamelistEntry?.desc}</td></tr>
            <tr><th>Developer</th><td>${gamelistEntry?.developer}</td></tr>
            <tr><th>Publisher</th><td>${gamelistEntry?.publisher}</td></tr>
            <tr><th>Genre</th><td>${gamelistEntry?.genre}</td></tr>
            <tr><th>Players</th><td>${gamelistEntry?.players}</td></tr>
            <tr><th>Region</th><td>${gamelistEntry?.region}</td></tr>
            <tr><th>Rom Type</th><td>${gamelistEntry?.romtype}</td></tr>
            <tr><th>Release Date</th><td>${gamelistEntry?.releasedate}</td></tr>
            <tr><th>Rating</th><td>${gamelistEntry?.rating}</td></tr>
            <tr><th>Play Count</th><td>${gamelistEntry?.playcount}</td></tr>
            <tr><th>Last Played</th><td>${gamelistEntry?.lastplayed}</td></tr>
            <tr><th>Scrape Id</th><td>${gamelistEntry?.scrapeId}</td></tr>
            <tr><th>Scrape Source</th><td>${gamelistEntry?.scrapeSource}</td></tr>
            <tr><th>Image</th><td>
                <g:if test="${gamelistEntry && gamelistEntry.image && gamelistEntry.system && gamelistEntry.scrapeId}">
                    <img src='<g:createLink mapping="showRomImageForSystem" params="[system: gamelistEntry.system, hash: gamelistEntry.hash]"/>' />
                </g:if>
            </td></tr>
    </table>
</body>
</html>
