<%@ page import="retropie.romfilter.indexed.Game" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Browse ROMs</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="games/browse.js" asset-defer="true" />
    <asset:stylesheet src="games/browse.css"/>
    <g:javascript>
       var gamesDataFeedUrl = '<g:createLink action="feed"/>';
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link action="browse">Browse ROMs</g:link> | Results of Delete all matching roms

    <p>Results of deleting all roms matching the following query</p>
    <p>Query = <strong>${query}</strong></p>

    <table>
        <thead>
            <tr><th>Path</th><th>Result</th></tr>
        </thead>
        <tbody>
        <g:each in="${deleteResult}" var="entry">
            <tr><td>${entry.key}</td><td>${entry.value}</td></tr>
        </g:each>
        </tbody>
    </table>
</body>
