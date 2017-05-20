<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of ROMS for ${system}</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="listGames.js" asset-defer="true" />
    <asset:stylesheet src="listGames.css"/>
    <g:javascript>
       var gamesDataFeed = '${raw(gamesDataFeed)}';
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link mapping="listGames">List Games</g:link>

    <table id="romTable" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Scrape Name<br/>Filename</th>
                <th>Name</th>
                <th>Genre</th>
                <th>Desc</th>
                <th>Image</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <th>Scrape Name<br/>Filename</th>
                <th>Name</th>
                <th>Genre</th>
                <th>Desc</th>
                <th>Image</th>
            </tr>
        </tfoot>
    </table>
</body>
</html>
