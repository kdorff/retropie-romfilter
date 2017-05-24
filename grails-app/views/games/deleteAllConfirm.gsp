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
        <g:link action="browse">Browse ROMs</g:link> | Confirming: Delete all matching roms

    <p>Are you sure you want to delete (move to trash) all ROMs that match the following query?</p>
    <p>Query = <strong>${query}</strong></p>

    <g:form action='deleteAllConfirmed' method="POST">
        <p>
            <input type='hidden' name='query' value='${query}'/>
            <button type="submit" name="submitbutton">CONFIRMED: Delete ALL ROMs matching query</button>
        </p>
    </g:form>
</body>