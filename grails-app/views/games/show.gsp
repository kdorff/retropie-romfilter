<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Show Details for ROM ${game?.path}</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="games/show.js" asset-defer="true" />
    <asset:stylesheet src="games/show.css"/>
</head>
<body>
    <p>Navigation:
        <g:link action="browse">Browse ROMs</g:link> |
        <g:link action="show" params="[hash: game.hash]">${game.name}</g:link>

    <table id="singleRomTable">
            <tr><th>System</th><td>${game?.system}</td></tr>
            <tr><th>Name</th><td>${game?.name}</td></tr>
            <tr><th>Filename</th><td>${game?.path}</td></tr>
            <tr><th>Size</th><td>${game?.size}</td></tr>
            <tr><th>Descruption</th><td>${game?.desc}</td></tr>
            <tr><th>Developer</th><td>${game?.developer}</td></tr>
            <tr><th>Publisher</th><td>${game?.publisher}</td></tr>
            <tr><th>Genre</th><td>${game?.genre}</td></tr>
            <tr><th>Players</th><td>${game?.players}</td></tr>
            <tr><th>Region</th><td>${game?.region}</td></tr>
            <tr><th>Rom Type</th><td>${game?.romtype}</td></tr>
            <tr><th>Release Date</th><td>${game?.releasedate}</td></tr>
            <tr><th>Rating</th><td>${game?.rating}</td></tr>
            <tr><th>Play Count</th><td>${game?.playcount}</td></tr>
            <tr><th>Last Played</th><td>${game?.lastplayed}</td></tr>
            <tr><th>Scrape Id</th><td>${game?.scrapeId}</td></tr>
            <tr><th>Scrape Source</th><td>${game?.scrapeSource}</td></tr>
            <tr><th>Image</th><td>
                <g:if test="${game && game.image && game.system && game.scrapeId}">
                    <img src='<g:createLink action="image" params="[hash: game.hash]"/>' />
                </g:if>
            </td></tr>
    </table>
</body>
</html>
