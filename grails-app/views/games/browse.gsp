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
        <g:link controller="games" action="browse">Browse ROMs</g:link> |
        <g:link controller="jobs" action="index">Systems and Jobs</g:link>
    </p>
    <p>&nbsp;</p>
    <div id="fieldsSpan">
        <strong>All Fields (click to toggle visibility):</strong>
            <g:each status='i' var='column' in="${Game.GameColumn.values()}">
                <g:if test="${i}"> | </g:if>
                <g:if test="${i % 11 == 0}"><br/></g:if>
                <a class="toggle-vis" data-column="${column.number}">${column.field}</a><g:if test="${column.searchable}"><sup>s</sup></g:if><g:if test="${column.orderable}"><sup>o</sup></g:if>
            </g:each>
        </p>
    </div>

    <div id="deleteAllSpan">
        <g:form action='deleteAllConfirm' method="POST">
            <p>
                <input type='hidden' id='deleteAllQuery' name='query' value=''/>
                <button type="submit" id="deleteAllMatching">Delete ALL ROMs matching query</button>
            </p>
        </g:form>
    </div>
    <table id="romTable" class="display table" cellspacing="0" width="100%">
        <thead>
            <tr>
                <g:each status='i' var='column' in="${Game.GameColumn.values()}">
                    <th>${column.friendlyName}</th>
                </g:each>
            </tr>
        </thead>
    </table>
    <g:javascript>
        var romTable;
        $(document).ready(function() {
            romTable = $('#romTable').DataTable({
                // searchHighlight: true,
                processing: true,
                serverSide: true,
                deferRender: true,
                scroller: true,
                scrollY: 350,
                paging:   true,
                sDom: "frti",
                columns: [
                    <g:each status='i' var='column' in="${Game.GameColumn.values()}"><g:if test="${i > 0}">,</g:if> { name: '${column.field}',
                         data: ${column.number},
                         searchable: ${column.searchable},
                         orderable: ${column.orderable},
                         visible: ${column.initiallyVisible} }
                    </g:each>
                ],
                order: [
                    [ ${Game.GameColumn.NAME.number}, "asc" ],
                    [ ${Game.GameColumn.SYSTEM.number}, "asc" ]
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
