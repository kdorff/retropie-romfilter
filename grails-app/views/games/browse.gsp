<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of ROMs</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="gamesBrowse.js" asset-defer="true" />
    <asset:stylesheet src="gamesBrowse.css"/>
    <g:javascript>
       var gamesDataFeedUrl = '<g:createLink action="feed"/>';
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link action="browse">Browse ROMs</g:link>

    <table id="romTable" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Scrape Name<br/>Filename</th>
                <th>Name</th>
                <th>Genre</th>
                <th>Genre</th>
                <th>Desc</th>
                <th>Image</th>
            </tr>
        </thead>
    </table>
</body>
</html>
