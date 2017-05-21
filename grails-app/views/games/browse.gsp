<%@ page import="retropie.romfilter.indexed.Game" %>
<%@ page import="retropie.romfilter.indexed.Game.GameColumn" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of ROMs</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="games/browse.js" asset-defer="true" />
    <asset:stylesheet src="games/browse.css"/>
    <g:javascript>
       var gamesDataFeedUrl = '<g:createLink action="feed"/>';
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link action="browse">Browse ROMs</g:link>

    <table id="romTable" class="display table" cellspacing="0" width="100%">
        <thead>
            <tr>
                <g:each status='i' var='column' in="${GameColumn}">
                    <th>${column.friendlyName}</th>
                </g:each>
            </tr>
        </thead>
    </table>
    <g:javascript>
        $(document).ready(function() {
            $('#romTable').DataTable({
                // searchHighlight: true,
                processing: true,
                serverSide: true,
                deferRender: true,
                scroller: true,
                scrollY: 400,
                paging:   true,
                sDom: "frti",
                // aoColumnDefs: [
                // {
                //     // Custom rendering for the first column
                //     // which is filename over scrape name
                //     "mRender": function ( data, type, row ) {
                //         if (row[1]) {
                //             return row[1] + '<br/>' + data;
                //         }
                //         else {
                //             return data;
                //     }
                //     },
                //     "aTargets": [ 0 ]
                // },
                // { "bVisible": false,  "aTargets": [ 1 ] }
                // ],
                columns: [
                    <g:each status='i' var='column' in="${GameColumn}"><g:if test="${i > 0}">,</g:if> { name: '${column.field}',
                         data: ${column.number},
                         searchable: ${column.searchable},
                         orderable: ${column.orderable},
                         visible: ${column.initiallyVisible} }
                    </g:each>
                ],
                ajax: {
                    url: gamesDataFeedUrl,
                    type: 'POST'
                }
            });
});
    </g:javascript>
</body>
</html>